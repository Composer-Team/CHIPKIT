package chipkit.wrappers

import chipkit.wrappers.AHBMasterMux.bool2Int
import chisel3._
import freechips.rocketchip.amba.ahb.{AHBBundleParameters, AHBMasterBundle, AHBSlaveBundle}

object AHBMasterMux {
  def bool2Int(a: Boolean): String = if (a) "1'b1" else "1'b0"
}

class AHBMasterMux(ahbbp: AHBBundleParameters, nDevices: Int) extends BlackBox(
  Map.from(Seq(
    ("M0_ENABLE", f"1'b${bool2Int(nDevices <= 1)}"),
    ("M1_ENABLE", f"1'b${bool2Int(nDevices <= 2)}"),
    ("M2_ENABLE", f"1'b${bool2Int(nDevices <= 3)}"),
    ("M3_ENABLE", f"1'b${bool2Int(nDevices <= 4)}")
  ))) {
  val io = IO(new Bundle {
    val M0 = new AHBSlaveBundle(ahbbp)
    val M1 = new AHBSlaveBundle(ahbbp)
    val M2 = new AHBSlaveBundle(ahbbp)
    val M3 = new AHBSlaveBundle(ahbbp)
    val MOUT = Flipped(new AHBSlaveBundle(ahbbp))

    val HCLK = Input(Clock())
    val HRESETn = Input(Reset())
    val HMSEL = Input(UInt(2.W))
    val HMASTER = Output(UInt(2.W))
  })
}
