/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.zmachine.zmachine3;

import russotto.zplet.screenmodel.ZScreen;
import russotto.zplet.screenmodel.ZStatus;
import russotto.zplet.screenmodel.ZWindow;
import russotto.zplet.zmachine.ZInstruction;
import russotto.zplet.zmachine.ZMachine;

public class ZMachine3 extends ZMachine {

	static final String[] opnames3 = new String[] {
		"", "je", "jl", "jg", "dec_chk", "inc_chk", "jin", "test", "or", "and",
		"test_attr", "set_attr", "clear_attr", "store", "insert_obj", "loadw",
		"loadb", "get_prop", "get_prop_addr", "get_next_prop", "add", "sub",
		"mul", "div", "mod", "call_2s", "call_2n", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "",
		"jz", "get_sibling", "get_child", "get_parent", "get_prop_len", "inc",
		"dec", "print_addr", "call_1s", "remove_obj", "print_obj", "ret",
		"jump", "print_paddr", "load", "not", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "",
		"rtrue", "rfalse", "print", "print_ret", "nop", "save", "restore",
		"restart", "ret_popped", "pop/catch", "quit", "new_line",
		"show_status", "verify", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "",
		"call", "storew", "storeb", "put_prop", "sread", "print_char",
		"print_num", "random", "push", "pull", "split_window", "set_window",
		"call_vs2", "erase_window", "erase_line", "set_cursor", "get_cursor",
		"set_text_style", "buffer_mode", "output_stream", "input_stream"
	};
	
	public ZMachine3(ZScreen screen, ZStatus status_line, byte [] memory_image) {
		super(screen, status_line, memory_image);

		header = new ZHeader3(memory_image);
		objects = new ZObjectTree3(this);
		zd = new ZDictionary3(this);
		globals = header.global_table();
		window = new ZWindow[2];
		window[0] = new ZWindow(screen, 0);
		window[1] = new ZWindow(screen, 1);
		window[1].set_transcripting(false);
		current_window = window[0];
		zi = new ZInstruction(this);
	}

	public void update_status_line() {
		ZHeader3 header = (ZHeader3)this.header;
		
		status_redirect = true;
		status_location = "";
		print_string(objects.short_name_addr(get_variable((short)16)));
		status_redirect = false;
		if (header.time_game())
			status_line.update_time_line(status_location,
										 get_variable((short)17),
										 get_variable((short)18));
		else
			status_line.update_score_line(status_location,
										  get_variable((short)17),
										  get_variable((short)18));
		status_location = null;
	}

	public int string_address(short addr) {
		return (((int)addr)&0xFFFF) << 1;
	}

	public int routine_address(short addr) {
		return (((int)addr)&0xFFFF) << 1;
	}

	public void restart() {

		super.restart();

		window[0].moveto(0,0);
		window[1].moveto(0,0);
		window[0].resize(screen.getchars(), -1);
		window[1].resize(0,0);
		window[0].movecursor(-1);
	}

	public void set_header_flags() { /* at start, restart, restore */
		ZHeader3 header = (ZHeader3)this.header;

		super.set_header_flags();
		header.set_status_unavailable(false);
		header.set_splitting_available(true);
		header.set_variable_default(true);
	}

	@Override
	public String[] getOpnames() {
		return opnames3;
	}
}
