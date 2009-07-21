package com.google.code.twisty;

import android.content.res.ColorStateList;
import android.text.style.BackgroundColorSpan;
import android.text.style.TextAppearanceSpan;

public class TwistyStyle {
	private final TextAppearanceSpan normalText;
	private final TextAppearanceSpan reverseText;
	private final BackgroundColorSpan normalBg;
	private final BackgroundColorSpan reverseBg;
	
	/**
	 * Creates a new {@code TwistyStyle}
	 * 
	 * @param normal
	 *           the {@code TextAppearanceSpan} for normal printing
	 * @param normalBg
	 *           the {@code ColorStateList} for the normal background
	 * @param reverseBg
	 *           the {@code ColorStateList} for reverse background
	 */
	public TwistyStyle(TextAppearanceSpan normal,
			int normalBgColor, int reverseBgColor) {
		
		normalText = normal;
		normalBg = new BackgroundColorSpan(normalBgColor);
		reverseBg = new BackgroundColorSpan(reverseBgColor);
		reverseText = new TextAppearanceSpan(
				normal.getFamily(),
				normal.getTextStyle(),
				normal.getTextSize(),
				ColorStateList.valueOf(normalBgColor),
				ColorStateList.valueOf(normalBgColor));
	}
	
	// TODO: A constructor that reads the data from a resource
	
	public TextAppearanceSpan getStyle(boolean reverse) {
		return (reverse ? reverseText : normalText);
	}
	
	public BackgroundColorSpan getBg(boolean reverse) {
		return (reverse ? reverseBg : normalBg);
	}
}
