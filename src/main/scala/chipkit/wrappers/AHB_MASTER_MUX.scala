package chipkit.wrappers

import chipkit.wrappers.AHB_MASTER_MUX.bool2Int
import chisel3._
import freechips.rocketchip.amba.ahb.{AHBBundleParameters, AHBMasterBundle, AHBSlaveBundle}
import protocol.AHBBasicSlaveBundle

object AHB_MASTER_MUX {
  def bool2Int(a: Boolean): Int = if (a) 1 else 0
}

class AHB_MASTER_MUX(ahbbp: AHBBundleParameters, nDevices: Int) extends BlackBox(
  Map.from(Seq(
    ("M0_ENABLE", bool2Int(nDevices <= 1)),
    ("M1_ENABLE", bool2Int(nDevices <= 2)),
    ("M2_ENABLE", bool2Int(nDevices <= 3)),
    ("M3_ENABLE", bool2Int(nDevices <= 4))
  ))) {
  val io = IO(new Bundle {
    val M0 = new AHBBasicSlaveBundle(ahbbp.dataBits, ahbbp.addrBits)
    val M1 = new AHBBasicSlaveBundle(ahbbp.dataBits, ahbbp.addrBits)
    val M2 = new AHBBasicSlaveBundle(ahbbp.dataBits, ahbbp.addrBits)
    val M3 = new AHBBasicSlaveBundle(ahbbp.dataBits, ahbbp.addrBits)
    val MOUT = Flipped(new AHBBasicSlaveBundle(ahbbp.dataBits, ahbbp.addrBits))

    val HCLK = Input(Clock())
    val HRESETn = Input(Reset())
    val HMSEL = Input(UInt(2.W))
    val HMASTER = Output(UInt(2.W))
  })
}
