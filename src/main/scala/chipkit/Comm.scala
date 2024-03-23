package chipkit

import chipkit.wrappers.COMMCTRL
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import chisel3._
import freechips.rocketchip.amba.ahb._
import protocol._

class LazyComm(implicit p: Parameters) extends LazyModule {

  lazy val module = new LazyCommImpl(this)

  val M = AHBSlaveSourceNode(Seq(AHBMasterPortParameters(
    masters = Seq(AHBMasterParameters("CommAHBMaster"))
  )))
}

class PROM_UART extends Bundle {
  val uart_rxd = Input(Bool())
  val uart_txd = Output(Bool())
}

class LazyCommImpl(outer: LazyComm) extends LazyModuleImp(outer) {

  val top = IO(new COMMTopIO)
  val hm_sel = IO(Output(Bool()))

  val sm = Module(new COMMCTRL)

  sm.io.clk := clock
  sm.io.rstn := !reset.asBool

  top.elements foreach { case (name, dat) =>
    if (sm.io.elements.contains(name))
      dat <> sm.io.elements(name)
  }

  hm_sel := sm.io.HMSEL
  val ahb = outer.M.out(0)._1
  sm.io.M_HRDATA := ahb.hrdata
  sm.io.M_HREADY := ahb.hready
  sm.io.M_HRESP := ahb.hresp
  ahb.hwdata := sm.io.M_HWDATA
  ahb.haddr := sm.io.M_HADDR
  ahb.hwrite := sm.io.M_HWRITE
  ahb.htrans := sm.io.M_HTRANS
  ahb.hsize := sm.io.M_HSIZE

  ahb.hsel := ahb.htrans =/= 0.U
  ahb.hready := true.B
  sm.io.M_HREADY := true.B
}