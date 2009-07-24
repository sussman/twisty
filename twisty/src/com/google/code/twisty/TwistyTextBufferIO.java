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
import org.brickshadow.roboglk.window.StandardTextBufferIO;

import org.brickshadow.roboglk.window.TextBufferView;

import android.os.Handler;
import android.text.Spannable;


public class TwistyTextBufferIO extends StandardTextBufferIO {

	private int currentStyle = GlkStyle.Normal;
	private boolean isReverse = false;
	
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
		tv.setBackgroundColor(style.getBackColor());
		/* 
		 * TODO: remember that when style hints are supported, this
		 *       size change will have to be made when style_Normal is
		 *       assigned a new size.
		 */
		tv.setTextSize(style.getSize());
		
		applyStyle();
		
		tv.requestFocus();
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

	@Override
	public int[] getWindowSize() {
		return super.getWindowSize();
	}
}
