/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet;

import com.google.twisty.zplet.Color;

public class ZColor {
	public final static int Z_CURRENT = 0;
	public final static int Z_DEFAULT = 1;
	public final static int Z_BLACK = 2;
	public final static int Z_RED = 3;
	public final static int Z_GREEN = 4;
	public final static int Z_YELLOW = 5;
	public final static int Z_BLUE = 6;
	public final static int Z_MAGENTA = 7;
	public final static int Z_CYAN = 8;
	public final static int Z_WHITE = 9;

	public static Color getcolor(int number) {
		switch (number) {
		case Z_BLACK:
			return Color.black;
		case Z_RED:
			return Color.red;
		case Z_GREEN:
			return Color.green;
		case Z_YELLOW:
			return Color.yellow;
		case Z_BLUE:
			return Color.blue;
		case Z_MAGENTA:
			return Color.magenta;
		case Z_CYAN:
			return Color.cyan;
		case Z_WHITE:
			return Color.white;
		}
		return Color.gray;
	}

	public static Color getcolor(String name) {
		if (name.equalsIgnoreCase("black"))
			return Color.black;
		if (name.equalsIgnoreCase("red"))
			return Color.red;
		if (name.equalsIgnoreCase("green"))
			return Color.green;
		if (name.equalsIgnoreCase("yellow"))
			return Color.yellow;
		if (name.equalsIgnoreCase("blue"))
			return Color.blue;
		if (name.equalsIgnoreCase("magenta"))
			return Color.magenta;
		if (name.equalsIgnoreCase("cyan"))
			return Color.cyan;
		if (name.equalsIgnoreCase("white"))
			return Color.white;
		return Color.gray;
	}

	public static int getcolornumber(String name) {
		if (name.equalsIgnoreCase("black"))
			return Z_BLACK;
		if (name.equalsIgnoreCase("red"))
			return Z_RED;
		if (name.equalsIgnoreCase("green"))
			return Z_GREEN;
		if (name.equalsIgnoreCase("yellow"))
			return Z_YELLOW;
		if (name.equalsIgnoreCase("blue"))
			return Z_BLUE;
		if (name.equalsIgnoreCase("magenta"))
			return Z_MAGENTA;
		if (name.equalsIgnoreCase("cyan"))
			return Z_CYAN;
		if (name.equalsIgnoreCase("white"))
			return Z_WHITE;
		return Z_BLACK;
	}
}

