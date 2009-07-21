package com.google.code.twisty;


import org.brickshadow.roboglk.GlkStyle;
import org.brickshadow.roboglk.window.StandardTextBufferIO;
import org.brickshadow.roboglk.window.TextBufferView;

import android.text.Spannable;


public class TwistyTextBufferIO extends StandardTextBufferIO {

	private int currentStyle = GlkStyle.Normal;
	private boolean isReverse = false;
	
	public TwistyTextBufferIO(TextBufferView tv) {
		super(tv);
		
		// TODO: read these from a resource
		tv.setBackgroundColor(0xFFFFFFFF);
		tv.setTextColor(0xFF000000);
		
		applyStyle();
	}

	@Override
	public void doStyle(int style) {
		if (style == currentStyle) {
			return;
		}
		
		currentStyle = style;
		applyStyle();
	}
	
	private void applyStyle() {
		Spannable text = (Spannable) tv.getText();
		int len = text.length();
		TwistyStyle spans = TwistyStyles.getStyleSpan(currentStyle);
		
		text.setSpan(spans.getStyle(isReverse),
				len, len, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		
		text.setSpan(spans.getBg(isReverse),
				len, len, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
	}
	
	public void doReverseVideo(boolean reverse) {
		if (reverse == isReverse) {
			return;
		}
		isReverse = reverse;
		applyStyle();
	}

}
