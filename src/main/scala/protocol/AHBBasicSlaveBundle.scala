package protocol

import chisel3._

/**
 * The UART used in ChipKit diverges from the AHB used in Diplomacy.
 */
class AHBBasicSlaveBundle(dw: Int = 32, aw: Int = 32) extends Bundle {
  val HREADY = Output(Bool())
  val HADDR = Input(UInt(aw.W))
  val HTRANS = Input(UInt(2.W))
  val HWRITE = Input(Bool())
  val HSIZE = Input(UInt(2.W))
  val HWDATA = Input(UInt(dw.W))
  val HRDATA = Output(UInt(dw.W))
  val HRESP = Output(Bool())

//  output M0_HREADY,
//  input [AW-1:0] M0_HADDR,
//  input [1:0] M0_HTRANS,
//  input M0_HWRITE,
//  input [2:0] M0_HSIZE,
//  input [DW-1:0] M0_HWDATA,
////   Transfer Response & Read Data
//  output [DW-1:0] M0_HRDATA,
//  output M0_HRESP,

}
