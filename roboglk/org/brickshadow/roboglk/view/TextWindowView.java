/* This file is a part of roboglk.
 * Copyright (c) 2009 Edward McCardell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.brickshadow.roboglk.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;


public abstract class TextWindowView extends TextView {

	private int numLines;
	private int charsPerLine;
	
	public TextWindowView(Context context) {
		super(context);
		setText("", BufferType.EDITABLE);
		setBackgroundColor(Color.argb(0xFF, 0xFE, 0xFF, 0xCC)); // TODO: coordinate with prefs
		setTextColor(0xFF000000);		// TODO: coordinate with prefs
		setTextSize(14);                // TODO: coordinate with prefs
	}
	
	public TextWindowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TextWindowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Returns the visible line count. If this method is called outside the
	 * UI thread, it must be synchronized on this object.
	 */
	public int getNumLines() {
		return numLines;
	}
	
	/**
	 * Returns the width of a line, in characters. If this method is called
	 * outside the UI thread, it must be synchronized on this object.
	 */
	public int getCharsPerLine() {
		return charsPerLine;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      return false;
    }
	
	@Override
	public boolean onCheckIsTextEditor() {
		int kbd = getContext().getResources().getConfiguration().keyboard;
		return (kbd != Configuration.KEYBOARD_QWERTY);
	}
	
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		int kbd = getContext().getResources().getConfiguration().keyboard;
		if (kbd != Configuration.KEYBOARD_QWERTY) {
			return new RoboInputConnection(this, false);
		} else {
			return null;
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		synchronized(this) {
			numLines = getHeight() / getLineHeight();
			charsPerLine = (int) (getWidth() / getPaint().measureText("0"));
		}
	}
}
