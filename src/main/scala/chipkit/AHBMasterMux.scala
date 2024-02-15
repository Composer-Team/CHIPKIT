package chipkit

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import freechips.rocketchip.amba.ahb._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util.BundleField

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

    val sm = Module(new wrappers.AHBMasterMux(node.in(0)._1.params, nDevices))

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
        sm.io.elements(f"M${idx}").asInstanceOf[AHBSlaveBundle].elements(name.toLowerCase) <> dat
      }
    }

//    (node.in.length until 4) foreach { case idx =>
//      sm.io.elements foreach { case (name, dat) =>
//        if (name.substring(0, 3).equals(f"M${idx}_")) {
//          dat <> DontCare
//        }
//      }

//    }

    node.out(0)._1.elements.foreach { case (name, dat) =>
      dat <> sm.io.elements(f"MOUT").asInstanceOf[AHBSlaveBundle].elements(name.toLowerCase)
    }
  }
}

