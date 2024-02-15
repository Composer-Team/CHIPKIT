// AHB_MASTER_MUX.sv - Simple mux for up to four AHB masters
// PNW 12 2015

// TODO:
// - Add assertions.
// - Add an option to have it self-arbitrating (or make this another module).


module AHB_MASTER_MUX
#(
  parameter M0_ENABLE = 1'b1,
  parameter M1_ENABLE = 1'b1,
  parameter M2_ENABLE = 1'b1,
  parameter M3_ENABLE = 1'b1
) (
  input logic HCLK, HRESETn,
  input logic [1:0] HMSEL,
  output logic [1:0] HMASTER,
  
  // SINK
  // Address, Control & Write Data
  output M0_HREADY,
  input [AW-1:0] M0_HADDR,
  input [1:0] M0_HTRANS,
  input M0_HWRITE,
  input [2:0] M0_HSIZE,
  input [DW-1:0] M0_HWDATA,
  // Transfer Response & Read Data
  output [DW-1:0] M0_HRDATA,
  output M0_HRESP,

  output M1_HREADY,
  input [AW-1:0] M1_HADDR,
  input [1:0] M1_HTRANS,
  input M1_HWRITE,
  input [2:0] M1_HSIZE,
  input [DW-1:0] M1_HWDATA,
  // Transfer Response & Read Data
  output [DW-1:0] M1_HRDATA,
  output M1_HRESP,

  output M2_HREADY,
  input [AW-1:0] M2_HADDR,
  input [1:0] M2_HTRANS,
  input M2_HWRITE,
  input [2:0] M2_HSIZE,
  input [DW-1:0] M2_HWDATA,
  // Transfer Response & Read Data
  output [DW-1:0] M2_HRDATA,
  output M2_HRESP,

  output M3_HREADY,
  input [AW-1:0] M3_HADDR,
  input [1:0] M3_HTRANS,
  input M3_HWRITE,
  input [2:0] M3_HSIZE,
  input [DW-1:0] M3_HWDATA,
  // Transfer Response & Read Data
  output [DW-1:0] M3_HRDATA,
  output M3_HRESP,

  input MOUT_HREADY,
  output [AW-1:0] MOUT_HADDR,
  output [1:0] MOUT_HTRANS,
  output MOUT_HWRITE,
  output [2:0] MOUT_HSIZE,
  output [DW-1:0] MOUT_HWDATA,
  // Transfer Response & Read Data
  input [DW-1:0] MOUT_HRDATA,
  input MOUT_HRESP,
);

// Use bus clock only in this module
logic clk, rstn;
always_comb clk = HCLK;
always_comb rstn = HRESETn;


// start out by stalling both masters with a low HREADY
// pipeline response switching back to correct master

//------------------------------------------------------------------------------
// Tie-off unused ports
//------------------------------------------------------------------------------

logic m0_hresp, m1_hresp, m2_hresp, m3_hresp;
logic m0_hready, m1_hready, m2_hready, m3_hready;
logic [31:0] m0_hrdata, m1_hrdata, m2_hrdata, m3_hrdata;

generate if(!M0_ENABLE)
always_comb {M0_HRESP, M0_HREADY, M0_HRDATA[31:0]} = '0;
else
always_comb {M0_HRESP, M0_HREADY, M0_HRDATA[31:0]} = {m0_hresp, m0_hready, m0_hrdata[31:0]};
endgenerate

generate if(!M1_ENABLE)
always_comb {M1_HRESP, M1_HREADY, M1_HRDATA[31:0]} = '0;
else
always_comb {M1_HRESP, M1_HREADY, M1_HRDATA[31:0]} = {m1_hresp, m1_hready, m1_hrdata[31:0]};
endgenerate

generate if(!M2_ENABLE)
always_comb {M2_HRESP, M2_HREADY, M2_HRDATA[31:0]} = '0;
else
always_comb {M2_HRESP, M2_HREADY, M2_HRDATA[31:0]} = {m2_hresp, m2_hready, m2_hrdata[31:0]};
endgenerate

generate if(!M3_ENABLE)
always_comb {M3_HRESP, M3_HREADY, M3_HRDATA[31:0]} = '0;
else
always_comb {M3_HRESP, M3_HREADY, M3_HRDATA[31:0]} = {m3_hresp, m3_hready, m3_hrdata[31:0]};
endgenerate


//------------------------------------------------------------------------------
// Control
//------------------------------------------------------------------------------

// The master should only be switched after any pending address phase completes.

// Transaction done when HREADY goes high
logic trans_done;
always_comb trans_done = MOUT.HREADY;

// Sample incoming mux value
logic [1:0] hmsel_aphase, hmsel_dphase;
`FF(HMSEL[1:0],hmsel_aphase[1:0],clk,trans_done,rstn,'0);
`FF(hmsel_aphase[1:0],hmsel_dphase[1:0],clk,trans_done,rstn,'0);

// TODO give warning if switch to unused port
// infact, probably shouldn't be able to switch to unused port - go to default instead

always_comb HMASTER[1:0] = hmsel_aphase[1:0];

//------------------------------------------------------------------------------
// Address Phase Signals
//------------------------------------------------------------------------------

// Slave -> Master signals

// If a master does is not granted, stall with HREADY
// otherwise, give the real HREADY from slave
always_comb begin
m0_hready = (hmsel_aphase[1:0] == 2'b00) ? MOUT.HREADY : 1'b0;
m1_hready = (hmsel_aphase[1:0] == 2'b01) ? MOUT.HREADY : 1'b0;
m2_hready = (hmsel_aphase[1:0] == 2'b10) ? MOUT.HREADY : 1'b0;
m3_hready = (hmsel_aphase[1:0] == 2'b11) ? MOUT.HREADY : 1'b0;
end

// Address phase signals (master -> slave)
always_comb 
case(hmsel_aphase[1:0])
  2'h0 : // M0
  {MOUT.HTRANS[1:0],MOUT.HWRITE,MOUT.HSIZE[2:0],MOUT.HADDR[31:0]} =
  {M0_HTRANS[1:0],M0_HWRITE,M0_HSIZE[2:0],M0_HADDR[31:0]};
  2'h1 : // M1
  {MOUT.HTRANS[1:0],MOUT.HWRITE,MOUT.HSIZE[2:0],MOUT.HADDR[31:0]} =
  {M1_HTRANS[1:0],M1_HWRITE,M1_HSIZE[2:0],M1_HADDR[31:0]};
  2'h2 : // M2
  {MOUT.HTRANS[1:0],MOUT.HWRITE,MOUT.HSIZE[2:0],MOUT.HADDR[31:0]} =
  {M2_HTRANS[1:0],M2_HWRITE,M2_HSIZE[2:0],M2_HADDR[31:0]};
  2'h3 : // M3
  {MOUT.HTRANS[1:0],MOUT.HWRITE,MOUT.HSIZE[2:0],MOUT.HADDR[31:0]} =
  {M3_HTRANS[1:0],M3_HWRITE,M3_HSIZE[2:0],M3_HADDR[31:0]};
  default : // M0
  {MOUT.HTRANS[1:0],MOUT.HWRITE,MOUT.HSIZE[2:0],MOUT.HADDR[31:0]} =
  {M0_HTRANS[1:0],M0_HWRITE,M0_HSIZE[2:0],M0_HADDR[31:0]};
endcase

//------------------------------------------------------------------------------
// Data Phase Signals
//------------------------------------------------------------------------------

// Data phase signals (slave -> master)
always_comb begin
  {m0_hresp, m0_hrdata[31:0]} = {MOUT.HRESP, MOUT.HRDATA[31:0]};
  {m1_hresp, m1_hrdata[31:0]} = {MOUT.HRESP, MOUT.HRDATA[31:0]};
  {m2_hresp, m2_hrdata[31:0]} = {MOUT.HRESP, MOUT.HRDATA[31:0]};
  {m3_hresp, m3_hrdata[31:0]} = {MOUT.HRESP, MOUT.HRDATA[31:0]};
end

// Data phase signals (master -> slave)
always_comb
case(hmsel_dphase[1:0])
  2'h0 : MOUT.HWDATA[31:0] = M0_HWDATA[31:0];
  2'h1 : MOUT.HWDATA[31:0] = M1_HWDATA[31:0];
  2'h2 : MOUT.HWDATA[31:0] = M2_HWDATA[31:0];
  2'h3 : MOUT.HWDATA[31:0] = M3_HWDATA[31:0];
  default : MOUT.HWDATA[31:0] = M0_HWDATA[31:0]; 
endcase


endmodule



