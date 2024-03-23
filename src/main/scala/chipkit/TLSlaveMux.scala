package chipkit

import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import chisel3._

class TLSlaveMux(implicit p: Parameters) extends LazyModule {
  val in = TLManagerNode(portParams = Seq(
    TLSlavePortParameters.v1(
      managers = Seq(TLSlaveParameters.v1(
        address = Seq(AddressSet(0, 0xFFFFFFFFL)),
        supportsGet = TransferSizes(4),
        supportsPutFull = TransferSizes(4)
      )), beatBytes = 4, endSinkId = 0
    )
  ))

  val out_sram = TLClientNode(Seq(TLMasterPortParameters.v1(
    clients = Seq(TLMasterParameters.v1(
      name = "program_sram",
      sourceId = IdRange(0, 1),
      supportsPutFull = TransferSizes(4),
      supportsProbe = TransferSizes(4)
    )))))

  val out_dma = TLClientNode(Seq(TLMasterPortParameters.v1(
    clients = Seq(TLMasterParameters.v1(
      name = "dma",
      sourceId = IdRange(0, 1),
      supportsGet = TransferSizes(4),
      supportsPutFull = TransferSizes(4),
      supportsProbe = TransferSizes(4)
    )))))

  lazy val module = new TLSlaveMuxImp(this)
}

class TLSlaveMuxImp(outer: TLSlaveMux) extends LazyModuleImp(outer) {
  val slave_select = IO(Input(UInt(1.W)))
//   connect outer.in to outer.out_sram
  val dma = outer.out_dma.out(0)._1
  dma.a.valid := false.B
  dma.a.bits := DontCare
  dma.d.ready := false.B
  val sram = outer.out_sram.out(0)._1
  sram.a.valid := false.B
  sram.a.bits := DontCare
  sram.d.ready := false.B
  when (slave_select === 0.U) {
    outer.in.in(0)._1 <> sram
  }.elsewhen (slave_select === 1.U) {
    outer.in.in(0)._1 <> dma
  }
}
