package chipkit

import chipsalliance.rocketchip.config.Parameters
import chisel3.{Bool, Clock, IO, Input, Reset}
import composer.Generation.ComposerBuild
import composer.Protocol.FrontBus.FrontBusProtocol
import freechips.rocketchip.amba.ahb.AHBToTL
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.tilelink.TLIdentityNode
import protocol.COMMTopIO

class ChipkitFrontBusProtocol(generator: Parameters => M0Abstract) extends FrontBusProtocol {

  /**
   * Expose top-level IOs: Chipkit IOs (uart for dma to prom and dram), Chipkit ASPSEL (slave select for Chipkit), and
   * stdio uarts for the ARM core
   */
  override def deriveTopIOs(tlChainObj: Any, withClock: Clock, withActiveHighReset: Reset)(implicit p: Parameters): Unit = {
    chipkit.sources foreach ComposerBuild.addSource
    val CHIP = IO(new COMMTopIO)
    val STDUART = IO(new PROM_UART)
    val CHIP_ASPSEL = IO(Input(Bool()))
    val (moa, lzc, slave_select) = tlChainObj.asInstanceOf[(M0Abstract, LazyComm, TLSlaveMux)]
    lzc.module.top <> CHIP
    slave_select.module.slave_select := CHIP_ASPSEL
    STDUART <> moa.module.uart
    moa.module.reset := withActiveHighReset.asBool
  }

  /**
   * Derive the TL sources for the ChipKit platform. The return value is a tuple that allows you to implement any sort
   * of custom logic for your platform.
   * The first parameter contains any of the lazy modules that you want to use in your platform that need to be connected
   *   to raw chisel IOs (e.g., top-level IOs).
   * The second parameter is the TLIdentityNode that communicates commands to the accelerator system
   * The third parameter is an optional TLIdentityNode that communicates commands to the DMA system (optional)
   */
  override def deriveTLSources(implicit p: Parameters): (Any, TLIdentityNode, Option[TLIdentityNode]) = {
    // top-level ChipKit COMM module
    val chipKitCOMM = LazyModule(new LazyComm)
    // select the slave: sram or dram
    val select = LazyModule(new TLSlaveMux())
    select.in := AHBToTL() := chipKitCOMM.M
    // instantiate the ARM M0 core
    val m0 = generator(p)
    m0.program_sram := select.out_sram
    val tl_dma = TLIdentityNode()
    tl_dma := select.out_dma

    val tl_node = TLIdentityNode()
    tl_node := AHBToTL() := m0.node
    ((m0, chipKitCOMM, select), tl_node, Some(tl_dma))
  }
}
