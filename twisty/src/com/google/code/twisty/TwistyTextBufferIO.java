// Copyright 2009 Google Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.google.code.twisty;

import org.brickshadow.roboglk.GlkStyle;
import org.brickshadow.roboglk.window.TextBufferIO;

import org.brickshadow.roboglk.window.TextBufferView;

import android.os.Handler;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.TextAppearanceSpan;


public class TwistyTextBufferIO extends TextBufferIO {

	private int currentStyle = GlkStyle.Normal;
	private int backColor = -1;
	private boolean isReverse = false;
	private TextAppearanceSpan lastStyle;
	private int lastStyleStart;
	private BackgroundColorSpan lastBg;
	private int lastBgStart;
	
	public TwistyTextBufferIO(final TextBufferView tv) {
		super(tv);
		
		Handler handler = tv.getHandler();
		if (handler == null) {
			initView();
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					initView();
				}
			});
		}
	}
	
	private void initView() {
		TwistyStyle style = TwistyStyles.getStyleSpan(GlkStyle.Normal);
		backColor = style.getBackColor();
		tv.setBackgroundColor(backColor);
		/* 
		 * TODO: remember that when style hints are supported, this
		 *       size change will have to be made when style_Normal is
		 *       assigned a new size.
		 */
		tv.setTextSize(style.getSize());
		
		applyStyle();
		
		// TODO: figure out if new windows automatically get focus?
		tv.requestFocus();
	}

	@Override
	public boolean doDistinguishStyles(int styl1, int styl2) {
		/* TODO: this is a hack until full style support */
		if (styl1 == GlkStyle.Preformatted
				|| styl2 == GlkStyle.Preformatted
				|| styl1 == GlkStyle.Subheader
				|| styl2 == GlkStyle.Subheader) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void doStyle(int style) {
		if (style == currentStyle) {
			return;
		}

		int prevStyle = currentStyle;
		currentStyle = style;
		if (doDistinguishStyles(prevStyle, currentStyle)) {
			applyStyle();
		}
	}
	
	private void applyStyle() {
		Spannable text = (Spannable) tv.getText();
		int len = text.length();
		TwistyStyle spans = TwistyStyles.getStyleSpan(currentStyle);
		
		TextAppearanceSpan style = spans.getStyle(isReverse);
		if (lastStyle != null) {
			text.setSpan(lastStyle,
					lastStyleStart, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		text.setSpan(style,
				len, len, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		lastStyle = style;
		lastStyleStart = len;
		
		BackgroundColorSpan bg = spans.getBg(isReverse);
		int newBackColor = bg.getBackgroundColor();
		if (newBackColor != backColor) {
			backColor = newBackColor;
			if (lastBg != null) {
				text.setSpan(lastBg,
						lastBgStart, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			text.setSpan(bg,
					len, len, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			lastBg = bg;
			lastBgStart = len;
		}
	}
	
	public void doReverseVideo(boolean reverse) {
		if (reverse == isReverse) {
			return;
		}
		isReverse = reverse;
		applyStyle();
	}

	@Override
	public int[] getWindowSize() {
		return super.getWindowSize();
	}
}
