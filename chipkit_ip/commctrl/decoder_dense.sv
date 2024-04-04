// decoder.sv - Decoder of UART Controller
// HKL 01 2016

module decoder_dense
import comm_defs_pkg::*;
(
  // clock and reset
  input  logic clk,
  input  logic rstn,

  // Interface to frontend
  input logic                end_of_inst,          // Indicate the end of instruction
  input logic [IBUF_SZ-1:0][IBUF_DW-1:0] ibuf_dec, // Instruction to be decoded
  input logic [IBUF_AW-1:0]  ibuf_cnt_dec,         // Count of Instruction to be decoded

  // Interface to backend
  output logic [31:0]  addr_uart,       // Decoded Address
  output logic [31:0]  wrdata_uart,     // Decoded Write Data
  output logic         we_uart,         // Decoded Write Enable
  output logic         decode_err_uart, // Decode err
  output logic         sm_start_uart,   // Start signal for FSM in backend
  output logic [15:0]  err_code_uart    // err code
);

//---------------------------------------------------------
// ------ UPDATED FORMAT ----------
// name      |     valid values    |       byte range
// command   |     'r'/'R'/'w'/'W' |         [0:0]
// addr      |  binary bigendian   |         [4:1]
// data      |  binary bigendian   |         [8:5]
// handshake |     'CR'            |         [9:9]
// handshake |     'LF'            |         [10:10]
// Decode Address, Data, Write Enable, err
logic [31:0] addr, addr_nxt;
logic [31:0] wrdata, wrdata_nxt;
logic        we, we_nxt;
logic        decode_done;
logic        decode_err, decode_err_nxt;
logic [15:0] err_code, err_code_nxt;

// decode takes 1 cycle
`FF(end_of_inst,decode_done,clk,1'b1,rstn,1'b0);

// Bytes of instruction
logic wr_size_err, rd_size_err;
always_comb wr_size_err = (ibuf_cnt_dec!=IBUF_SZ);
always_comb rd_size_err = !((ibuf_cnt_dec>=14)&&(ibuf_cnt_dec<=IBUF_SZ));

// Line Breaks (CRLF/LF) for [] or [12,13]
logic crlf_err;
logic [7:0] last1;
always_comb last1 = ibuf_dec[ibuf_cnt_dec-1];
always_comb crlf_err = last1!=ASCII_LF;

// W/w or R/r
logic rw_err;
always_comb rw_err =
    !((ibuf_dec[0]==ASCII_W)||(ibuf_dec[0]==ASCII_w)||
     (ibuf_dec[0]==ASCII_R)||(ibuf_dec[0]==ASCII_r));

// Separator ( 0x)

// Read or Write Address [11:4]
logic [7:0] addr_3, addr_2, addr_1, addr_0;
always_comb begin
addr_3     = ibuf_dec[4];
addr_2     = ibuf_dec[3];
addr_1     = ibuf_dec[2];
addr_0     = ibuf_dec[1];
end

// Write Data [22:15]
logic [7:0] wrdata_3, wrdata_2, wrdata_1, wrdata_0;
always_comb begin
wrdata_3     = ibuf_dec[8];
wrdata_2     = ibuf_dec[7];
wrdata_1     = ibuf_dec[6];
wrdata_0     = ibuf_dec[5];
end

// Next values after decoded
// Write Enable
always_comb we_nxt = ((ibuf_dec[0]==ASCII_W)||(ibuf_dec[0]==ASCII_w));

// Address
always_comb addr_nxt = {addr_3,addr_2,addr_1,addr_0};

// Wrdata
always_comb wrdata_nxt = {wrdata_3,wrdata_2,wrdata_1,wrdata_0};

// Decode err
always_comb begin
if (we_nxt) begin // Write Decode err
decode_err_nxt =    crlf_err    |rw_err;
end
else begin // Read Decode err
decode_err_nxt =    crlf_err  |rw_err ;
end
end

// err Code
always_comb begin
if(we_nxt) begin // Write Decode err Code
err_code_nxt =
    (wr_size_err)  ? {ASCII_0,ASCII_1} : // CODE 10 : Write Instruction Size err
    (rw_err)       ? {ASCII_1,ASCII_1} : // CODE 12 : W/w err
    (crlf_err)     ? {ASCII_2,ASCII_2} : // CODE 22 : Line Break err
                     {ASCII_0,ASCII_0};  // No err
end
else begin  // Read Decode err Code
err_code_nxt =
    (rd_size_err)  ? {ASCII_0,ASCII_1} : // CODE 10 : Write Instruction Size err
    (rw_err)       ? {ASCII_1,ASCII_1} : // CODE 11 : R/r err
    (crlf_err)     ? {ASCII_2,ASCII_2} : // CODE 22 : Line Break err
                     {ASCII_0,ASCII_0};  // No err
end
end

// Address, Write_Data, Write_Enable
// Write Decode err, Read Decode err, err Code
`FF(addr_nxt,addr,clk,decode_done,rstn,32'd0);
`FF(wrdata_nxt,wrdata,clk,decode_done,rstn,32'd0);
`FF(we_nxt,we,clk,decode_done,rstn,1'b0);
`FF(decode_err_nxt,decode_err,clk,decode_done,rstn,1'b0);
`FF(err_code_nxt,err_code,clk,decode_done,rstn,22'd0);

// Generate a sm_start signal
// sm_start is a 1-cycle pulse
// this signal should be delayed with an extra cycle
// to sync up with other data
logic sm_start;
logic decode_done_reg, decode_done_reg_1;
`FF(decode_done,decode_done_reg,clk,1'b1,rstn,1'b0);
`FF(decode_done_reg,decode_done_reg_1,clk,1'b1,rstn,1'b0);
always_comb sm_start = decode_done_reg & (~decode_done_reg_1);


// Output Assignments
always_comb begin
addr_uart       = addr;
wrdata_uart     = wrdata;
we_uart         = we;
sm_start_uart   = sm_start;
decode_err_uart = decode_err;
err_code_uart   = err_code;
end

endmodule
