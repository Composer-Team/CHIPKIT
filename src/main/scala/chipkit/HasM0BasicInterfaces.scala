package chipkit

import chisel3.IO

trait HasM0BasicInterfaces {
  val uart = IO(new PROM_UART)
}
