package chipkit

import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.amba.ahb.{AHBMasterParameters, AHBMasterPortParameters, AHBSlaveSourceNode}
import freechips.rocketchip.diplomacy.{AddressSet, LazyModule, LazyModuleImp, TransferSizes}
import freechips.rocketchip.tilelink.{TLManagerNode, TLSlaveParameters, TLSlavePortParameters}

/**
 * We can't put the ARM IP in a public repository so we just have a wrapper with all the necessary IOs but no impl.
 */
abstract class M0Abstract(implicit p: Parameters) extends LazyModule {
  // The ARM M0 core has an AHB interface for reading instructions from SRAM and communicating with accelerators.
  // The SRAM requests get filtered out before making it to this node and are sent to the internal SRAM.
  // The rest are accesses to the accelerator system that come through this node.
  val node = AHBSlaveSourceNode(
    portParams = Seq(AHBMasterPortParameters(
      masters = Seq(AHBMasterParameters(
        name = "M0_AHB"
      )))))

  // The program ROM is initialized through a write-only port here.
  val program_sram = TLManagerNode(
    portParams = Seq(TLSlavePortParameters.v1(
      managers = Seq(TLSlaveParameters.v1(
        address = Seq(AddressSet(0, 0xFFFF)),
        supportsPutFull = TransferSizes(4),
      )), beatBytes = 4, endSinkId = 0
    ))
  )

  override def module: LazyModuleImp with HasM0BasicInterfaces
}
