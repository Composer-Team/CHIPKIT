package protocol

import chisel3._

class UARTSlave extends Bundle {
  val BAUDSEL = Input(UInt(4.W))
  val CTS = Input(Bool())
  val RTS = Output(Bool())
  val RXD = Input(Bool())
  val TXD = Output(Bool())
}
