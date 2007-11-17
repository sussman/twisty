/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.zmachine.zmachine5;
import russotto.zplet.screenmodel.ZScreen;

public class ZMachine8 extends ZMachine5 {
		public ZMachine8(ZScreen screen, byte [] memory_image) {
				super(screen, memory_image);
		}

		public int string_address(short addr) {
				return (((int)addr)&0xFFFF) << 3;
		}

		public int routine_address(short addr) {
				return (((int)addr)&0xFFFF) << 3;
		}
}

