package chipkit

import chisel3._
import chisel3.util._

// // Master Interface provided in Chipkit SV RTL
//  output M0_HREADY,
//  input [AW-1:0] M0_HADDR,
//  input [1:0] M0_HTRANS,
//  input M0_HWRITE,
//  input [2:0] M0_HSIZE,
//  input [DW-1:0] M0_HWDATA,
//  // Transfer Response & Read Data
//  output [DW-1:0] M0_HRDATA,
//  output M0_HRESP,

class AHBChipKitIO(aw: Int, dw: Int) extends Bundle {
  val HREADY = Output(Bool())
  val HADDR = Input(UInt(aw.W))
  val HTRANS = Input(UInt(2.W))
  val HWRITE = Input(Bool())
  val HSIZE = Input(UInt(3.W))
  val HWDATA = Input(UInt(dw.W))
  val HRDATA = Output(UInt(dw.W))
  val HRESP = Output(Bool())
}
class AHBMasterMux(nMasters: Int,
                   aw: Int, dw: Int) extends Module{
  val io = IO(new Bundle{
    val masterSelect = Input(UInt(log2Ceil(nMasters).W))
    val masters = Vec(nMasters, new AHBChipKitIO(aw, dw))
    val slave = Flipped(new AHBChipKitIO(aw, dw))
  })
  // tie all masters low
  io.masters.foreach { m =>
    m.HREADY := false.B
    m.HRDATA := DontCare
    m.HRESP := false.B
  }

  // connect the selected master to the slave (which will override previous tie)
  io.slave <> io.masters(io.masterSelect)
}
