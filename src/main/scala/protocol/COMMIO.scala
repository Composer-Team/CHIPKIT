package protocol

import chisel3._

class COMMIO extends Bundle {
  val clk = Input(Clock())
  val rstn = Input(Reset())

  val M_HRDATA = Input(UInt(32.W))
  val M_HREADY = Input(Bool())
  val M_HRESP = Input(Bool())
  val M_HWDATA = Output(UInt(32.W))
  val M_HADDR = Output(UInt(32.W))
  val M_HWRITE = Output(Bool())
  val M_HTRANS = Output(UInt(2.W))
  val M_HSIZE = Output(UInt(3.W))

  val FESEL = Input(Bool())
  val SCLK1 = Input(Clock())
  val SCLK2 = Input(Clock())
  val SHIFTIN = Input(Bool())
  val SCEN = Input(Bool())
  val SHIFTOUT = Output(Bool())

  val UART_M_BAUD_SEL = Input(UInt(4.W))
  val UART_M_RXD = Input(Bool())
  val UART_M_CTS = Input(Bool())
  val UART_M_RTS = Output(Bool())
  val UART_M_TXD = Output(Bool())
  val IRQ_COMMCTRL = Output(Bool())

  val HMSEL = Output(UInt(2.W))
}

class COMMTopIO extends Bundle {
  val FESEL = Input(Bool())
  val SCLK1 = Input(Clock())
  val SCLK2 = Input(Clock())
  val SHIFTIN = Input(Bool())
  val SCEN = Input(Bool())
  val SHIFTOUT = Output(Bool())

  val UART_M_BAUD_SEL = Input(UInt(4.W))
  val UART_M_RXD = Input(Bool())
  val UART_M_CTS = Input(Bool())
  val UART_M_RTS = Output(Bool())
  val UART_M_TXD = Output(Bool())
}
