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


import org.brickshadow.roboglk.GlkJustification;
import org.brickshadow.roboglk.GlkStyle;
import org.brickshadow.roboglk.GlkStyleHint;

import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;


public class StyleManager {

	public static final int NUM_STYLES = GlkStyle.User2 + 1;
	
	public static final int BLACK = 0xFF000000;
	public static final int WHITE = 0xFFFFFFFF;
	
	private static final int TAP_TYPE = 0;
	private static final int FGC_TYPE = 1;
	private static final int BGC_TYPE = 2;
	private static final int RS_TYPE = 3;
	private static final int INDENT_TYPE = 4;
	private static final int JUSTIFY_TYPE = 5;
	private static final int NUM_SPAN_TYPES = 6;
	
	public static Style[] newDefaultStyles() {
		Style[] styles = new Style[NUM_STYLES];
		for (int s = 0; s < NUM_STYLES; s++) {
			styles[s] = new Style();
		}
		// Nothing for Normal.
		styles[GlkStyle.Emphasized].setHint(GlkStyleHint.Oblique, 1);
		styles[GlkStyle.Preformatted].setHint(GlkStyleHint.Proportional, 0);
		styles[GlkStyle.Header].setHint(GlkStyleHint.Justification, GlkJustification.Centered);
		styles[GlkStyle.Header].setHint(GlkStyleHint.Size, 3);
		styles[GlkStyle.Subheader].setHint(GlkStyleHint.Size, 1);
		styles[GlkStyle.Subheader].setHint(GlkStyleHint.Weight, 1);
		styles[GlkStyle.Alert].setHint(GlkStyleHint.Size, 1);
		styles[GlkStyle.Alert].setHint(GlkStyleHint.Oblique, 1);
		styles[GlkStyle.Alert].setHint(GlkStyleHint.Weight, 1);
		// Nothing for Note.
		styles[GlkStyle.BlockQuote].setHint(GlkStyleHint.Weight, 1);
		styles[GlkStyle.BlockQuote].setHint(GlkStyleHint.Indentation, 2);
		styles[GlkStyle.Input].setHint(GlkStyleHint.Weight, 1);
		// Nothing for User1.
		// Nothing for User2.
		return styles;
	}
	
	public static class Style {
		boolean proportional = true;
		int size = 0;           // TODO: coordinate with prefs
		int foreColor = BLACK;	// TODO: coordinate with prefs
		int backColor = WHITE;	// TODO: coordinate with prefs
		boolean reverse = false;
		int face = Typeface.NORMAL;
		Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;
		int indentation = 0;
		
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
					&& this.reverse == that.reverse
					&& this.align == that.align
					&& this.indentation == that.indentation);
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
			case INDENT_TYPE:
				return (this.indentation != other.indentation);
			case JUSTIFY_TYPE:
				return (this.align != other.align);
			default:
				throw new IllegalArgumentException();
			}
		}
		
		final Object getSpan(int type, boolean fakeReverse) {
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
			case INDENT_TYPE:
			    if (indentation == 0) {
			    	return null;
			    }
			    // TODO: Should this be based on baseline font size?
			    return new LeadingMarginSpan.Standard(indentation * 14);
			case JUSTIFY_TYPE:
			    if (align == Layout.Alignment.ALIGN_NORMAL) {
			    	return null;
			    }
				return new AlignmentSpan.Standard(align);
			default:
				throw new IllegalArgumentException();
			}
		}
		
		final float convertedSize() {
			// TODO: choose a "good" relative scale
			return 1.0f + (size / 8.0f);
		}
		
		public final void setHint(int hint, int val) {
			switch (hint) {
			case GlkStyleHint.BackColor:
				backColor = 0xFF000000 | val;
				break;
			case GlkStyleHint.Indentation:
				indentation = val;
				break;
			case GlkStyleHint.Justification:
				switch (val) {
				case GlkJustification.Centered:
					align = Layout.Alignment.ALIGN_CENTER;
					break;
				case GlkJustification.RightFlush:
					align = Layout.Alignment.ALIGN_OPPOSITE;
					break;
				case GlkJustification.LeftFlush:
				case GlkJustification.LeftRight:  // TODO: support LeftRight.
					align = Layout.Alignment.ALIGN_NORMAL;
					break;
				}
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
			case GlkStyleHint.Indentation:
				return indentation;
			case GlkStyleHint.Justification:
				switch (align) {
				case ALIGN_NORMAL:
					return GlkJustification.LeftFlush;
				case ALIGN_OPPOSITE:
					return GlkJustification.RightFlush;
				case ALIGN_CENTER:
					return GlkJustification.Centered;
				default:
					throw new StyleMeasurementException();
				}
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
	private Object[] oldSpans = new Object[NUM_SPAN_TYPES];
	private int[] oldStarts = new int[NUM_SPAN_TYPES];

	private Object oldLinkSpan = null;
	private int oldLinkStart = 0;
	private int currentLinkVal = 0;
	
	public StyleManager() {
		this.styles = newDefaultStyles();
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

	// TODO(jmegq): Consider merging hyperlink handling into applyStyle; they
	// share a lot of structure. Merge the hyperlink fields into the
	// oldSpans[] and oldStarts[] arrays as well.
	public void applyHyperlink(int newLinkVal, Spannable text) {
		if (newLinkVal == currentLinkVal) {
			return;
		}

		final int textLen = text.length();
		Object newSpan = null;
		
		if (oldLinkSpan != null) {
			text.setSpan(oldLinkSpan, oldLinkStart, textLen,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		if (newLinkVal != 0) {
			newSpan = new URLSpan(Integer.toString(newLinkVal));
			text.setSpan(newSpan, textLen, textLen,
					Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}
		oldLinkSpan = newSpan;
		oldLinkStart = textLen;
		currentLinkVal = newLinkVal;
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
		Object newSpan = null;
		boolean changeSpan = false;
		Style currentStyle =
			(currentStyleNum == -1 ? null : styles[currentStyleNum]);
		Style newStyle = styles[newStyleNum];
		for (int s = 0; s < NUM_SPAN_TYPES; s++) {
			changeSpan = (currentStyle == null
					|| currentStyle.isDifferent(newStyle, s, fakeReverse));
			if (!changeSpan && (s == FGC_TYPE || s == BGC_TYPE)) {
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
