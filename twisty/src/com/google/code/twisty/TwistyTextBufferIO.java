package com.google.code.twisty;


import org.brickshadow.roboglk.GlkStyle;
import org.brickshadow.roboglk.window.StandardTextBufferIO;
import org.brickshadow.roboglk.window.TextBufferView;

import android.text.Spannable;


public class TwistyTextBufferIO extends StandardTextBufferIO {

	private int currentStyle = 0xFFFFFFFF;
	private boolean isReverse = false;
	
	public TwistyTextBufferIO(TextBufferView tv) {
		super(tv);
		
		// TODO: read these from a resource
		tv.setBackgroundColor(0xFFFFFFFF);
		tv.setTextColor(0xFF000000);
		
		doStyle(GlkStyle.Normal);
	}

	@Override
	public void doStyle(int style) {
		if (style == currentStyle) {
			return;
		}
		
		applyStyle(style);
		currentStyle = style;
	}
	
	private void applyStyle(int style) {
		Spannable text = (Spannable) tv.getText();
		int len = text.length();
		TwistyStyle spans = TwistyStyles.getStyleSpan(style);
		
		text.setSpan(spans.getStyle(isReverse),
				len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		
		/* TODO: don't apply this span if isReverse == false
		 *       and the background color == window background
		 */
		text.setSpan(spans.getBg(isReverse),
				len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
	}
	
	public void doReverseVideo(boolean reverse) {
		if (reverse == isReverse) {
			return;
		}
		isReverse = reverse;
		applyStyle(currentStyle);
	}

}
