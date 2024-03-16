package chipkit

import chipkit.wrappers.AHB_MASTER_MUX
import chipsalliance.rocketchip.config.Parameters
import chisel3._
import freechips.rocketchip.amba.ahb._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util.BundleField
import protocol.AHBBasicSlaveBundle

class AHBMasterMux(nDevices: Int)(implicit p: Parameters) extends LazyModule {
  // Combine multiple Slaves into one logical Slave (suitable to attach to an Arbiter)

  val node = new AHBFanoutNode(
    masterFn = { a: Seq[AHBMasterPortParameters] => a.head },
    slaveFn  = { seq =>
      seq(0).copy(
        slaves = seq.flatMap(_.slaves),
        requestKeys    = seq.flatMap(_.requestKeys).distinct,
        responseFields = BundleField.union(seq.flatMap(_.responseFields))) }
  ){
    override def circuitIdentity = outputs == 1 && inputs == 1
  }

//  val masters_in = AHBMasterSinkNode(Seq(AHBSlavePortParameters(
//    slaves = Seq.fill(nDevices)(AHBSlaveParameters(
//      address = Seq(AddressSet(0x0, 0xFFFFFFFFL)),
//      supportsWrite = TransferSizes(4), // TODO CONFIRM
//      supportsRead = TransferSizes(4), // TODO CONFIRM
//      regionType = RegionType.UNCACHED
//    )),
//    beatBytes = 4,
//    lite=false
//  )))
//
//  val master_out = AHBSlaveSourceNode(Seq(AHBMasterPortParameters(
//    masters = Seq(AHBMasterParameters("AHB Master Mux Out"))
//  )))

  override def module: LazyModuleImp = new LazyModuleImp(this) {

    val sm = Module(new AHB_MASTER_MUX(node.in(0)._1.params.copy(addrBits = 32), nDevices))

    val HCLK = IO(Input(Bool()))
    val HMSEL = IO(Input(UInt(2.W)))
    val HMASTER = IO(Output(UInt(2.W)))

    sm.io.HCLK := HCLK.asClock
    sm.io.HRESETn := (!reset.asBool)
    sm.io.HMSEL := HMSEL
    HMASTER := sm.io.HMASTER

    sm.io.elements foreach { case (_, dat) => dat <> DontCare }

    node.in.zipWithIndex foreach { case (io, idx) =>
      io._1.elements foreach { case (name, dat) =>
        sm.io.elements(f"M${idx}").asInstanceOf[AHBBasicSlaveBundle].elements.get(name.toUpperCase()) match {
          case Some(d) => dat <> d
          case None => dat := DontCare
        }
      }
    }

    val on = node.out(0)._1
    // Master goes nowhere...
    on.haddr <> sm.io.MOUT.HADDR
    on.hready <> sm.io.MOUT.HREADY
    on.hresp <> sm.io.MOUT.HRESP
    on.hsize <> sm.io.MOUT.HSIZE
    on.hsel := true.B
    dontTouch(on.hsel)
    dontTouch(on)
    sm.io.MOUT.HTRANS <> on.htrans
    sm.io.MOUT.HWRITE <> on.hwrite
    sm.io.MOUT.HRDATA <> on.hrdata
    sm.io.MOUT.HWDATA <> on.hwdata
  }
}

