package object chipkit {
  def init_ip_repo(): Unit = {
    os.write.over(os.pwd / "ip.tgz", (os.resource / "ip.tgz").toSource)
    os.proc(Seq("tar", "-xzf", (os.pwd / "ip.tgz").toString())).call(cwd = os.pwd)
  }

  val sources = Seq(
    "rtl_inc/RTL.svh",
    "commctrl/comm_defs_pkg.sv",
    "ahb/ahb_intf.sv",
    "ahb/AHB_BUS.sv",
//    "ahb/AHB_MASTER_MUX.sv",
    "ahb/AHB_MEM.sv",
    "commctrl/backend.sv",
    "commctrl/baudmux.sv",
    "commctrl/decoder_dense.sv",
    "commctrl/frontmux.sv",
    "commctrl/level_to_pulse.sv",
    "commctrl/scanfront.sv",
    "commctrl/uart.sv",
//    "commctrl/uart_intf.sv",
    "commctrl/uartfront.sv",
    "commctrl/COMMCTRL.sv"
  ).map(a => os.pwd / "chipkit_ip" / os.RelPath(a))
}
