package com.google.code.twisty;

import android.content.res.ColorStateList;
import android.text.style.BackgroundColorSpan;
import android.text.style.TextAppearanceSpan;

public class TwistyStyle {
	private final String family;
	private final int style;
	private final int size;
	private final int foreColor;
	private final int backColor;
	
	private ColorStateList foreState;
	private ColorStateList backState;

	// TODO: ColorStateList for hyperlinks
	
	public TwistyStyle(String family, int style, int size,
			int foreColor, int backColor) {
		this.family = family;
		this.style = style;
		this.size = size;
		this.foreColor = foreColor;
		this.backColor = backColor;
		
		foreState = ColorStateList.valueOf(foreColor);
		backState = ColorStateList.valueOf(backColor);
	}
	
	// TODO: A constructor that reads the data from a resource
	
	public int getStyle() {
		return style;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getForeColor() {
		return foreColor;
	}
	
	public int getBackColor() {
		return backColor;
	}
	public TextAppearanceSpan getStyle(boolean reverse) {
		return new TextAppearanceSpan(
				family,
				style,
				size,
				(reverse ? backState : foreState),
				null);
	}
	
	public BackgroundColorSpan getBg(boolean reverse) {
		return new BackgroundColorSpan(
				(reverse ? foreColor : backColor)
				);
	}
}
