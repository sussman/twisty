package com.google.twisty.zplet;

import russotto.zplet.ZColor;

import com.google.twisty.TwistyView;

import android.graphics.Rect;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView.BufferType;

/**
 * Interface from zmachine's thread into the view thread for all screen
 * operations.
 * @author Marius Milner
 */
public class ZViewOutput {
	private final TwistyView view;
	private int zbackground;
	private int zforeground;
	private Color background;
	private Color foreground;
	private Color spanbackground;
	private Color spanforeground;
	private Font font;
	private static final String TAG = "ZViewOutput";
	private int xpos, ypos, curpos, maxrows;

	public ZViewOutput(TwistyView view) {
		this.view = view;
		xpos = 0;
		ypos = 0;
		curpos = 0;
	}

	private Editable getEditable() {
		CharSequence seq = view.getText();
		if (seq instanceof Editable) {
			return (Editable) seq;
		} else {
			return null;
		}
	}

	public void setColors(int zfg, int zbg)
	{
		if (zforeground != zfg) {
			zforeground = zfg;
			foreground = ZColor.getcolor(zfg);
		}
		if (zbackground != zbg) {
			zbackground = zbg;
			background = ZColor.getcolor(zbg);
		}
	}

	private void setFont(Font f) {
		if (f == null || f.equals(font))
			return;
		Editable edit = getEditable();
		if (edit != null) {
			Log.i(TAG, "Font: " + f.toString());
			getEditable().setSpan(new TextAppearanceSpan(f.getName(), f.getStyle(), 
					f.getSize(), null, null),
					curpos, curpos, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}
		font = f;
	}

	private void setBackground(Color color) {
		if (spanbackground == color) {
			return;
		}
		Editable edit = getEditable();
		if (edit != null) {
			Log.i(TAG, "Background: " + color.toString());
			getEditable().setSpan(new BackgroundColorSpan(color.getARGB()),
					curpos, curpos, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}
		spanbackground = color;
	}

	private void setForeground(Color color) {
		if (spanforeground == color) {
			return;
		}
		Editable edit = getEditable();
		if (edit != null) {
			Log.i(TAG, "Foreground: " + color.toString());
			getEditable().setSpan(new ForegroundColorSpan(color.getARGB()),
					curpos, curpos, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}
		spanforeground = color;
	}

	/**
	 * Called from the zmachine's thread just before it exits, for per-view
	 * cleanup
	 * @param e an exception that caused the exit, or null for normal exit
	 */
	public void onZmFinished(ZMachineException e) {
		view.onZmFinished(e);
	}

	/**
	 * Called from the zmachine's thread just before it exits, for activity
	 * cleanup
	 * @param e an exception that caused the exit, or null for normal exit
	 */
	public void tellOwnerZmFinished(ZMachineException e) {
		view.tellOwnerZmFinished(e);
	}

	private static int nextEOL(CharSequence s, int pos) {
		int length = s.length();
		while (pos < length) {
			if (s.charAt(pos) == '\n')
				return pos;
			++pos;
		}
		return -1;
	}

	// TODO: limit scrolling to page size, do --MORE-- stuff
	private void scrollBottomIntoView() {
		Layout l = view.getLayout();
		if (l == null)
			return;
		int line = l.getLineCount() - 1;
		if (line < 0)
			line = 0;
		Rect bounds = new Rect();
		l.getLineBounds(line, bounds);
		int view_bottom = view.getScrollY() + view.getHeight();
		if (view_bottom < bounds.bottom) {
			// the bottom of the line is off the bottom of the view.
			view.scrollBy(0, bounds.bottom - view_bottom);
		}
	}

	/**
	 * Actually moves the cursor. Needs to run from the UI thread.
	 * @param position character in the buffer before which the cursor should
	 * appear.
	 */
	private synchronized void doMoveCursor(int position) {
		Editable e = getEditable();
		int length = e.length();
		if (position > length || position < 0)
			position = length;
		curpos = position;
		Selection.setSelection(e, position);
	}

	/**
	 * In the rare case where we move the cursor by index into the editable,
	 * calculate the corresponding x and y values
	 */
	private synchronized void calcXY() {
		Editable e = getEditable();
		int lastbegin = 0;
		int y = 0;
		for (int i = 0; i < curpos; i++) {
			if (e.charAt(i) == '\n') {
				++y;
				lastbegin = i;
			}
		}
		xpos = curpos - lastbegin;
		ypos = y;
	}

	public void moveCursor(final int position) {
		view.runOnUiThread(new Runnable() {
			public void run() {
				doMoveCursor(position);
				calcXY();
			}
		});
	}

	private synchronized void doMoveCursor(int row, int col) {
		xpos = col;
		ypos = row;
		doMoveCursor(ensureMinColumns(row, col));
	}

	/**
	 * Make sure that the requested row/column combination exists 
	 * @param row
	 * @param col
	 * @return the text position of the new location
	 */
	private int ensureMinColumns(int row, int col) {
		Editable e = getEditable();
		int pos = 0;
		int r = 1;
		while (r < row) {
			int eol = nextEOL(e, pos + 1);
			if (eol < 0) {
				// Last line: append newlines until we have enough
				pos = e.length();
				if (r < row) {
					Log.i(TAG, "writing " + (row - r) + " empty lines to reach " + row);
					while (r++ < row) {
						e.insert(pos, "\n");
						++pos;
					}
				}
				break;
			}
			pos = eol + 1;
			++r;
		}
		// Now pos is at the start of the right row
		int eol = nextEOL(e, pos);
		if (eol < 0)
			eol = e.length();
		int needed = col - (eol - pos);
		if (needed > 0) {
			Log.i(TAG, "writing " + needed + " spaces on line " + row + " to reach " + col);
			while (needed-- > 0) {
				e.insert(eol, " ");
				++eol;
			}
		}
		return pos + col;
	}

	public void moveCursor(final int row, final int col) {
		view.runOnUiThread(new Runnable() {
			public void run() {
				doMoveCursor(row, col);
			}
		});
	}

	public void showCursor(final boolean show) {
		view.runOnUiThread(new Runnable() {
			public void run() {
				view.setCursorVisible(show);    
			}
		});
	}

	public void showMore(boolean show) {
		view.showMore(show);
	}

	private synchronized void doClear() {
		view.setText("", BufferType.EDITABLE);
		xpos = 0;
		ypos = 0;
		moveCursor(view.length());
		spanbackground = null;
		spanforeground = null;
	}

	public void clear() {
		view.runOnUiThread(new Runnable() {
			public void run() {
				doClear();
			}
		});
	}

	private synchronized void doEraseToEOL() {
		Editable e = getEditable();
		int pos = curpos;
		int len = e.length();
		while (pos < len && e.charAt(pos) != '\n') {
			e.replace(pos, pos + 1, " ");
			++pos;
		}
	}

	public void eraseToEOL() {
		view.runOnUiThread(new Runnable() {
			public void run() {
				doEraseToEOL();
			}
		});
	}

	/**
	 * Output text at the current cursor position (and move the cursor forward)
	 * @param chars
	 * @param firstchar
	 * @param length
	 * @param reverse
	 * @param font
	 */
	public void output(char[] chars, int offset, int length, boolean reverse,
			Font font) {
		output(new String(chars, offset, length), reverse, font);
	}

	private synchronized void doOutput(CharSequence buf, boolean reverse, Font font) {
		setFont(font);
		setBackground(reverse ? foreground : background);
		setForeground(reverse ? background : foreground);
		// Overwrite the characters on this line.
		Editable e = getEditable();
		int eol = nextEOL(e, curpos);
		if (eol < 0)
			eol = e.length();
		int end = curpos + buf.length();
		if (end > eol)
			end = eol;
		getEditable().replace(curpos, end, buf);
		xpos += buf.length();
		curpos += buf.length();
		scrollBottomIntoView();
	}

	public void output(final CharSequence buf, final boolean reverse,
			final Font font) {
		if (buf.length() == 0) {
			return;
		}
		if (nextEOL(buf, 0) >= 0) {
			throw new RuntimeException("output may not contain newlines");
		}
		view.runOnUiThread(new Runnable() {
			public void run() {
				doOutput(buf, reverse, font);
			}
		});
	}

	/**
	 * Output a newline at the current cursor position
	 */
	private synchronized void doNewline() {
		Editable e = getEditable();
		int nextline = nextEOL(e, curpos);
		if (nextline < 0) {
			curpos = e.length();
			e.insert(curpos, "\n");
			++curpos;
		} else {
			doMoveCursor(nextline + 1);
		}
		xpos = 0;
		++ypos;
		if (maxrows > 0) {
			while (ypos >= maxrows) {
				int eol = nextEOL(e, 0);
				if (eol < 0)
					return;
				e.delete(0, eol + 1);
				curpos -= eol + 1;
				--ypos;
			}
		}
	}

	/**
	 * Output a newline at the current cursor position
	 */
	public void newline() {
		view.runOnUiThread(new Runnable() {
			public void run() {
				doNewline();
			}
		});
	}

	private synchronized void doEraseCharAtCursor() {
		Editable e = getEditable();
		// Don't allow erase across line endings
		if (xpos < 1)
			return;
		e.delete(curpos - 1, curpos);
		--xpos;
		--curpos;
	}

	public void backspace() {
		view.runOnUiThread(new Runnable() {
			public void run() {
				doEraseCharAtCursor();
			}
		});
	}

	public synchronized int getPosX() {
		// TODO: block until UI thread has caught up with everything
		return xpos;
	}

	public synchronized int getPosY() {
		// TODO: block until UI thread has caught up with everything
		return ypos;
	}

	private void doResize(int columns, int rows) {
		Log.i(TAG, "Resizing to " + rows + "x" + columns + " chars");
		if (rows == 0) {
			maxrows = 0;
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
			if (rows > 0) {
				view.setMinLines(rows);
				// trim the content too
				int r = 1;
				int pos = 0;
				Editable e = getEditable();
				while (r < rows) {
					pos = nextEOL(e, pos);
					if (pos == -1)
						break;
					++pos;
					++r;
				}
				if (pos-- > 0) {
					e.delete(pos, e.length());
					if (curpos > pos) {
						curpos = pos;
						calcXY();
					}
				}
				maxrows = rows;
			} else {
				view.setMinLines(0);
				maxrows = 0;
			}
		}
	}

	public void resize(final int columns, final int rows) {
		view.runOnUiThread(new Runnable() {
			public void run() {
				doResize(columns, rows);
			}
		});
	}

	public void moveto(int newleft, int newtop) {
		// TODO: rearrange the layout to be absolute, if needed
		if (newleft > 0 || newtop > 0) {
			Log.e(TAG, "Unimplemented: moveto(" + newleft + ", " + newtop + ")");
		}
	}

	public void autoScroll() {
		// auto-scroll to bottom of screen, showing --MORE-- prompt if needed.
		// example:
		/*
      if (myview.needmore()) {
        showMore(true);
        myscreen.read_code();
        showMore(false);
        line_counter = 0;
      }
		 */
		view.runOnUiThread(new Runnable() {
			public void run() {
				scrollBottomIntoView();
			}
		});
	}

	public void resetAutoScroll() {
		// TODO: reset the threshold for the --MORE-- prompt
	}

	public void setWrappable(final boolean wrapmode) {
		view.runOnUiThread(new Runnable() {
			public void run() {
				view.setHorizontallyScrolling(!wrapmode);
			}
		});
	}
}
