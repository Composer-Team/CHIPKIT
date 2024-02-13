package chipkit.wrappers

import chipkit.init_ip_repo
import chisel3._
import protocol.COMMIO

class COMMCTRL extends BlackBox {
  init_ip_repo() // make sure the repo with .sv IPs exists
  val io = IO(new COMMIO)
}
