package chipkit

import chipkit.wrappers.COMMCTRL
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import chisel3._
import freechips.rocketchip.amba.ahb._
import protocol._

class LazyComm(implicit p: Parameters) extends LazyModule {

  def module: LazyModuleImpLike = new LazyCommImpl(this)

  val M = AHBMasterSourceNode(Seq(AHBMasterPortParameters(
    masters = Seq(AHBMasterParameters("CommAHBMaster"))
  )))
}

class LazyCommImpl(outer: LazyComm) extends LazyModuleImp(outer) {

  val top = IO(new COMMTopIO)
  val hm_sel = IO(Output(Bool()))

  val sm = Module(new COMMCTRL)

  top.elements foreach { case (name, dat) =>
    if (sm.io.elements.contains(name))
      dat <> sm.io.elements(name)
  }

  hm_sel := sm.io.HMSEL
  val ahb = outer.M.in(0)._1
  sm.io.M_HRDATA := ahb.hrdata
  sm.io.M_HREADY := ahb.hready
  sm.io.M_HRESP := ahb.hresp
  ahb.hwdata := sm.io.M_HWDATA
  ahb.haddr := sm.io.M_HADDR
  ahb.hwrite := sm.io.M_HWRITE
  ahb.htrans := sm.io.M_HTRANS
  ahb.hsize := sm.io.M_HSIZE
}