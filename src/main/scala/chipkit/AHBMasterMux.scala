package chipkit

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import freechips.rocketchip.amba.ahb._
import freechips.rocketchip.diplomacy._

class AHBMasterMux(nDevices: Int)(implicit p: Parameters) extends LazyModule {
  val masters_in = AHBMasterSinkNode(Seq(AHBSlavePortParameters(
    slaves = Seq.fill(nDevices)(AHBSlaveParameters(
      address = Seq(AddressSet(0x0, 0xFFFFFFFFL)),
      supportsWrite = TransferSizes(4), // TODO CONFIRM
      supportsRead = TransferSizes(4), // TODO CONFIRM
      regionType = RegionType.UNCACHED
    )),
    beatBytes = 4,
    lite=false
  )))

  val master_out = AHBMasterSourceNode(Seq(AHBMasterPortParameters(
    masters = Seq(AHBMasterParameters("AHB Master Mux Out"))
  )))

  override def module: LazyModuleImp = new LazyModuleImp(this) {

    val sm = Module(new wrappers.AHBMasterMux(master_out.in(0)._1.params, nDevices))

    val HCLK = IO(Input(Bool()))
    val HMSEL = IO(Input(UInt(2.W)))
    val HMASTER = IO(Output(UInt(2.W)))

    sm.io.HCLK := HCLK
    sm.io.HRESETn := (!reset.asBool)
    sm.io.HMSEL := HMSEL
    HMASTER := sm.io.HMASTER

    masters_in.in.zipWithIndex foreach { case (io, idx) =>
      io._1.elements foreach { case (name, dat) =>
        sm.io.elements(f"M${idx}_${name.toUpperCase}") <> dat
      }
    }

    master_out.in(0)._1.elements.foreach { case (name, dat) =>
      dat <> sm.io.elements(f"MOUT_${name.toUpperCase}")
    }
  }
}

