/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.zmachine.state;

import russotto.zplet.zmachine.ZHeader;

class ZStateHeader extends ZHeader
{
	ZStateHeader (byte [] memory_image)
	{
		this.memory_image = memory_image;
	}

	/* yes, a kludge */
	public int file_length() {
		return 0;
	}
}

