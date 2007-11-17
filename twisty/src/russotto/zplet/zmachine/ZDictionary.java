/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.zmachine;


public abstract class ZDictionary
{
	public abstract void tokenise(int textloc, int textlength, int parseloc);
	public abstract boolean parse_word(int textloc, int wordloc, int wordlength, int parseloc);

}

