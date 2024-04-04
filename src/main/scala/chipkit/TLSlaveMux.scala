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
        supportsPutFull = TransferSizes(4),
        regionType = RegionType.IDEMPOTENT,
      )), beatBytes = 4, endSinkId = 0
    )
  ))

  val out_sram = TLClientNode(Seq(TLMasterPortParameters.v1(
    clients = Seq(TLMasterParameters.v1(
      name = "program_sram",
      sourceId = IdRange(0, 1),
      supportsGet = TransferSizes(4),
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
  val slave_select = IO(Input(Bool()))
  val sram = outer.out_sram.out(0)._1
  val is_sram = RegNext(slave_select === 1.U)
  sram.a.valid := outer.in.in(0)._1.a.valid && is_sram
  sram.a.bits := outer.in.in(0)._1.a.bits

  val is_prom = RegNext(slave_select === 0.U)
  val dma = outer.out_dma.out(0)._1
  dma.a.valid := outer.in.in(0)._1.a.valid && is_prom
  dma.a.bits := outer.in.in(0)._1.a.bits

  when (is_sram) {
    outer.in.in(0)._1.a.ready := sram.a.ready
  } .otherwise {
    outer.in.in(0)._1.a.ready := dma.a.ready
  }

  sram.d.ready := outer.in.in(0)._1.d.ready && is_sram
  dma.d.ready := outer.in.in(0)._1.d.ready && is_prom
  when (is_sram) {
    outer.in.in(0)._1.d.valid := sram.d.valid
    outer.in.in(0)._1.d.bits := sram.d.bits
  } .elsewhen (is_prom) {
    outer.in.in(0)._1.d.valid := dma.d.valid
    outer.in.in(0)._1.d.bits := dma.d.bits
  } .otherwise {
    outer.in.in(0)._1.d.valid := false.B
    outer.in.in(0)._1.d.bits := DontCare
  }
}
