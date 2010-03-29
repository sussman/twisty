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

package org.brickshadow.roboglk.io;


import org.brickshadow.roboglk.GlkStyle;
import org.brickshadow.roboglk.GlkStyleHint;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;


public class StyleManager {

	public static final int NUM_STYLES = GlkStyle.User2 + 1;
	
	public static final int BLACK = 0xFF000000;
	public static final int WHITE = 0xFFFFFFFF;
	
	private static final int TAP_TYPE = 0;
	private static final int FGC_TYPE = 1;
	private static final int BGC_TYPE = 2;
	private static final int RS_TYPE = 3;
	
	public static Style[] newDefaultStyles() {
		Style[] styles = new Style[NUM_STYLES];
		for (int s = 0; s < NUM_STYLES; s++) {
			styles[s] = new Style();
		}
		styles[GlkStyle.Preformatted].setHint(GlkStyleHint.Proportional, 0);
		return styles;
	}
	
	public static class Style {
		boolean proportional = true;
		int size = 0;           // TODO: coordinate with prefs
		int foreColor = BLACK;	// TODO: coordinate with prefs
		int backColor = WHITE;	// TODO: coordinate with prefs
		boolean reverse = false;
		int face = Typeface.NORMAL;
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Style)) {
				return false;
			}
			Style that = (Style) o;
			return (this.proportional == that.proportional
					&& this.size == that.size
					&& this.face == that.face
					&& this.foreColor == that.foreColor
					&& this.backColor == that.backColor
					&& this.reverse == that.reverse);
		}
		
		final boolean isDifferent(Style other, int type, boolean fakeReverse) {
			boolean otherReverse = other.reverse || fakeReverse;
			switch (type) {
			case TAP_TYPE:
				return (this.face != other.face
						|| this.proportional != other.proportional);
			case FGC_TYPE:
				return (this.foreColor != other.foreColor
						|| this.reverse != otherReverse);
			case BGC_TYPE:
				return (this.backColor != other.backColor
						|| this.reverse != otherReverse);
			case RS_TYPE:
				return (this.size != other.size);
			default:
				throw new IllegalArgumentException();
			}
		}
		
		final CharacterStyle getSpan(int type, boolean fakeReverse) {
			switch (type) {
			case TAP_TYPE:
				if (proportional && face == Typeface.NORMAL) {
					return null;
				}
				return new TextAppearanceSpan(
						(proportional ? "sans-serif" : "monospace"),
						face, -1, null, null);
			case FGC_TYPE:
				if (reverse || fakeReverse) {
					return new ForegroundColorSpan(backColor);
				}
				if (foreColor == BLACK) {
					return null;
				}
				return new ForegroundColorSpan(foreColor);
			case BGC_TYPE:
				if (reverse || fakeReverse) {
					return new BackgroundColorSpan(foreColor);
				}
				if (backColor == WHITE) {
					return null;
				}
				return new BackgroundColorSpan(backColor);
			case RS_TYPE:
				if (size == 0) {
					return null;
				}
				return new RelativeSizeSpan(convertedSize());
			default:
				throw new IllegalArgumentException();
			}
		}
		
		final float convertedSize() {
			// TODO: convert to relative scale
			return 1.0f;
		}
		
		public final void setHint(int hint, int val) {
			switch (hint) {
			case GlkStyleHint.BackColor:
				backColor = 0xFF000000 | val;
				break;
			case GlkStyleHint.Oblique:
				switch (face) {
				case Typeface.NORMAL:
					if (val == 1) {
						face = Typeface.ITALIC;
					}
					break;
				case Typeface.BOLD:
					if (val == 1) {
						face = Typeface.BOLD_ITALIC;
					}
					break;
				case Typeface.ITALIC:
					if (val == 0) {
						face = Typeface.NORMAL;
					}
					break;
				case Typeface.BOLD_ITALIC:
					if (val == 0) {
						face = Typeface.BOLD;
					}
				}
				break;
			case GlkStyleHint.Proportional:
				proportional = (val == 1);
				break;
			case GlkStyleHint.ReverseColor:
				reverse = (val == 1);
				break;
			case GlkStyleHint.Size:
				// TODO: convert -4/4 to correct scale factors
				size = val;
				break;
			case GlkStyleHint.TextColor:
				foreColor = 0xFF000000 | val;
				break;
			case GlkStyleHint.Weight:
				switch (face) {
				case Typeface.NORMAL:
					if (val == 1) {
						face = Typeface.BOLD;
					}
					break;
				case Typeface.BOLD:
					if (val != 1) {
						face = Typeface.NORMAL;
					}
					break;
				case Typeface.ITALIC:
					if (val == 1) {
						face = Typeface.BOLD_ITALIC;
					}
					break;
				case Typeface.BOLD_ITALIC:
					if (val != 1) {
						face = Typeface.ITALIC;
					}
				}
				break;
			}
		}
		
		int measureStyle(int hint) {
			switch (hint) {
			case GlkStyleHint.BackColor:
				return backColor & 0x00FFFFFF;
			case GlkStyleHint.Oblique:
				if (face == Typeface.BOLD_ITALIC
						|| face == Typeface.ITALIC) {
					return 1;
				} else {
					return 0;
				}
			case GlkStyleHint.Proportional:
				return (proportional ? 1 : 0);
			case GlkStyleHint.ReverseColor:
				return (reverse ? 1 : 0);
			case GlkStyleHint.Size:
				// TODO: make sure value is in range
				throw new StyleMeasurementException();
			case GlkStyleHint.TextColor:
				return foreColor & 0x00FFFFFF;
			case GlkStyleHint.Weight:
				if (face == Typeface.BOLD || face == Typeface.BOLD_ITALIC) {
					return 1;
				} else {
					return 0;
				}
			default:
				throw new StyleMeasurementException();
			}
		}
	}
	
	private Style[] styles;
	private int currentStyleNum = -1;
	private boolean currentReverse = false;
	private CharacterStyle[] oldSpans = new CharacterStyle[4];
	private int[] oldStarts = new int[4];
	
	public StyleManager() {
		styles = new Style[NUM_STYLES];
		for (int s = 0; s < NUM_STYLES; s++) {
			styles[s] = new Style();
		}
		styles[GlkStyle.Preformatted].setHint(GlkStyleHint.Proportional, 0);
	}
	
	public StyleManager(Style[] styles) {
		this.styles = styles;
	}
	
	public boolean distinguishStyles(int style1, int style2) {
		return !(styles[style1].equals(styles[style2])); 
	}
	
	public int measureStyle(int styleNum, int hint) {
		Style style = styles[styleNum];
		return style.measureStyle(hint);
	}
	
	public void applyStyle(int newStyleNum, Spannable text) {
		applyStyle(newStyleNum, currentReverse, text);
	}
	
	public void applyStyle(boolean fakeReverse, Spannable text) {
		applyStyle(currentStyleNum, fakeReverse, text);
	}
	
	public void applyStyle(int newStyleNum, boolean fakeReverse,
			Spannable text) {

		if (newStyleNum == currentStyleNum && fakeReverse == currentReverse) {
			return;
		}
		
		int textLen = text.length();
		CharacterStyle newSpan = null;
		boolean changeSpan = false;
		Style currentStyle =
			(currentStyleNum == -1 ? null : styles[currentStyleNum]);
		Style newStyle = styles[newStyleNum];
		for (int s = 0; s < 4; s++) {
			changeSpan = (currentStyle == null
					|| currentStyle.isDifferent(newStyle, s, fakeReverse));
			if (!changeSpan && (s == 1 || s == 2)) {
				changeSpan = fakeReverse != currentReverse;
			}
			if (changeSpan) {
				if (oldSpans[s] != null) {
					text.setSpan(oldSpans[s], oldStarts[s], textLen,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				newSpan = newStyle.getSpan(s, fakeReverse);
				if (newSpan != null) {
					text.setSpan(newSpan, textLen, textLen,
							Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				}
				oldSpans[s] = newSpan;
				oldStarts[s] = textLen;
			}
		}
		currentStyleNum = newStyleNum;
		currentReverse = fakeReverse;
	}
}
