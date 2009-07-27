package com.google.code.twisty;

import org.brickshadow.roboglk.GlkStyle;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.style.TextAppearanceSpan;
import android.util.Log;

/**
 * A utility class that maps Glk styles to Twisty styles.
 * @author sean
 *
 */
public class TwistyStyles {

	private static int MAX_STYLE = GlkStyle.User2;
	
	private static TwistyStyle[] styles;
	
	static {
		styles = new TwistyStyle[MAX_STYLE + 1];
		
		/* For now, all styles are black on white. */
		int black = 0xFF000000;
		int white = 0xFFFFFFFF;
		
		for (int style = 0; style <= MAX_STYLE; style++) {
			String family =
				(style == GlkStyle.Preformatted ? "Courier" : "Helvetica");
			int typeface =
				(style == GlkStyle.Subheader ? Typeface.BOLD : Typeface.NORMAL);
			styles[style] = new TwistyStyle(
						family,
						typeface,
						12,
						black,
						white);
		}
	}

	public static TwistyStyle getStyleSpan(int style) {
		if (style > MAX_STYLE || style < 0) {
			return styles[GlkStyle.Normal];
		}
		return styles[style];
	}
	
	private TwistyStyles() {}
}
