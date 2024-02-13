package protocol

import chisel3._

class SCANSlave extends Bundle {
  val SCLK1 = Input(Clock())
  val SCLK2 = Input(Clock())
  val SCEN = Input(Bool())
  val SHIFTIN = Input(Bool())
  val SHIFTOUT = Output(Bool())
}
