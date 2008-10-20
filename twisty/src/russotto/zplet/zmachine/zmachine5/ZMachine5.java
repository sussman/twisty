/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.zmachine.zmachine5;

import russotto.zplet.screenmodel.ZScreen;
import russotto.zplet.screenmodel.ZWindow;
import russotto.zplet.zmachine.ZMachine;
import russotto.zplet.zmachine.state.ZState;

public class ZMachine5 extends ZMachine {
	static final String[] opnames5 = new String[] {
		"", "je", "jl", "jg", "dec_chk", "inc_chk", "jin", "test", "or", "and",
		"test_attr", "set_attr", "clear_attr", "store", "insert_obj", "loadw",
		"loadb", "get_prop", "get_prop_addr", "get_next_prop", "add", "sub",
		"mul", "div", "mod", "call_2s", "call_2n", "set_colour", "throw", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "",
		"jz", "get_sibling", "get_child", "get_parent", "get_prop_len", "inc",
		"dec", "print_addr", "call_1s", "remove_obj", "print_obj", "ret",
		"jump", "print_paddr", "load", "call_1n", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "",
		"rtrue", "rfalse", "print", "print_ret", "nop", "save", "restore",
		"restart", "ret_popped", "catch", "quit", "new_line",
		"show_status", "verify", "(extended)", "piracy", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "",
		"call", "storew", "storeb", "put_prop", "aread", "print_char",
		"print_num", "random", "push", "pull", "split_window", "set_window",
		"call_vs2", "erase_window", "erase_line", "set_cursor", "get_cursor",
		"set_text_style", "buffer_mode", "output_stream", "input_stream",
		"sound_effect", "read_char", "scan_table", "not", "call_vn",
		"call_vn2", "tokenise", "encode_text", "copy_table", "print_table",
		"check_arg_count", "save", "restore", "log_shift", "art_shift",
		"set_font", "draw_picture", "picture_data", "erase_picture",
		"set_margins", "save_undo", "restore_undo", "print_unicode",
		"check_unicode", "", "", "",
		"move_window", "window_size", "window_style", "get_wind_prop",
		"scroll_window", "pop_stack", "read_mouse", "mouse_window",
		"push_stack", "put_wind_prop", "print_form", "make_menu",
		"picture_table"
	};

		public short argcount;
		ZState undo_state = null;

		public ZMachine5(ZScreen screen, byte [] memory_image) {
				super(screen, null, memory_image);
				header = new ZHeader5(memory_image);
				objects = new ZObjectTree5(this);
				zd = new ZDictionary5(this);
				globals = header.global_table();
				window = new ZWindow[2];
				window[0] = new ZWindow(screen, 0);
				window[1] = new ZWindow(screen, 1);
				window[1].setscroll(false);
				window[1].setbuffermode(false);
				window[1].setwrapmode(false);
				window[1].set_transcripting(false);
				current_window = window[0];
				zi = new ZInstruction5(this);
				argcount = 0;
		}

		public void update_status_line() {
		}

		public int string_address(short addr) {
				return (((int)addr)&0xFFFF) << 2;
		}

		public int routine_address(short addr) {
				return (((int)addr)&0xFFFF) << 2;
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
				ZHeader5 header = (ZHeader5)this.header;
				
				super.set_header_flags();
				header.set_revision(0,0);

				/* screen model flags */
				header.set_colors_available(true);
				header.set_bold_available(true);
				header.set_italic_available(true);
				header.set_fixed_font_available(true);
				header.set_timed_input_available(false);
				header.set_graphics_font_available(false);

				/* other flags (is mouse part of screen model?) */
				header.set_undo_available(true);
				header.set_mouse_available(false);
				header.set_sound_available(false);
				header.set_interpreter_number(ZHeader5.INTERP_MSDOS);
				header.set_interpreter_version((int)'J');
				header.set_screen_height_lines(screen.getlines());
				header.set_screen_width_characters(screen.getchars());

				/* TODO -- units */
				header.set_screen_height_units(screen.getlines());
				header.set_screen_width_units(screen.getchars());
				header.set_font_height_units(1);
				header.set_font_width_units(1);
				
				header.set_default_background_color(screen.getZBackground());
				header.set_default_foreground_color(screen.getZForeground());
		}

		int restore_undo() {
				if (undo_state != null) {
						undo_state.header.set_transcripting(header.transcripting());
						undo_state.restore_saved();
						set_header_flags();
						return 2;
				}
				return 0;
		}

		int save_undo() {
				if (undo_state == null) {
						undo_state = new ZState(this);
				}
				undo_state.save_current();
				return 1;
		}

		@Override
		public String[] getOpnames() {
			return opnames5;
		}
}

