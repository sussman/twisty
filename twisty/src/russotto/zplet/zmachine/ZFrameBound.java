/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.zmachine;


public class ZFrameBound
{
	private boolean store;
	public static ZFrameBound FALSE = new ZFrameBound(false);
	public static ZFrameBound TRUE = new ZFrameBound(true);
	
	private ZFrameBound(boolean store) {
		this.store = store;
	}

	public static ZFrameBound get(boolean store) {
		return store ? TRUE : FALSE;
	}

	public boolean isstore() {
		return store;
	}
}
