/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import russotto.zplet.ZColor;

import com.google.code.twisty.zplet.Font;
import com.google.code.twisty.zplet.ZViewOutput;

public class ZWindow {
	public final static int ROMAN = 0;
	public final static int REVERSE = 1;
	public final static int BOLD = 2;
	public final static int ITALIC = 4;
	public final static int FIXED = 8;

	final static char FIRST_STYLE = '\u8000';
	final static char BUF_ROMAN = (char)(FIRST_STYLE + ROMAN);
	final static char BUF_REVERSE = (char)(FIRST_STYLE + REVERSE);
	final static char BUF_BOLD = (char)(FIRST_STYLE + BOLD);
	final static char BUF_ITALIC = (char)(FIRST_STYLE + ITALIC);
	final static char BUF_FIXED = (char)(FIRST_STYLE + FIXED);
	final static char LAST_STYLE = '\u800f';

	final static int NORMAL_FONT = 1;
	final static int PICTURE_FONT = 2;
	final static int GRAPHICS_FONT = 3;
	final static int FIXED_FONT = 4;

	final static char FIRST_FONT = '\u8010';
	final static char BUF_NORMAL_FONT = '\u8010';
	final static char BUF_PICTURE_FONT = '\u8011';
	final static char BUF_GRAPHICS_FONT = '\u8012';
	final static char BUF_FIXED_FONT = '\u8013';
	final static char LAST_FONT = '\u8013';

	final ZViewOutput myview;
	final ZScreen myscreen;
	boolean buffer, wrap, scroll, transcriptmode;
	int curzfont = FIXED_FONT;
	int curzstyle = FIXED;
	Font curfont;
	StringBuilder linebuffer;
	int zforeground, zbackground;

	public ZWindow(ZScreen screen, int index) {
		myscreen = screen;
		myview = screen.getView(index);
		curfont = new Font("", 0, 0);
		buffer = true;
		wrap = true;
		scroll = true;
		transcriptmode = true;
		linebuffer = new StringBuilder();
		zforeground = screen.getZForeground();
		zbackground = screen.getZBackground();
	}

	/**
	 * if arg = 1: erase from cursor to EOL in current window;
	 * else: do nothing.
	 */
	public void erase_line(short arg) {
		if (arg == 1)
			myview.eraseToEOL();
	}

	public void setbuffermode(boolean buffermode)
	{
		if (buffer && !buffermode) {
			flush();
		}
		buffer = buffermode;
	}

	public void setwrapmode(boolean wrapmode)
	{
		if (buffer) {
			flush();
		}
		myview.setWrappable(wrapmode);
	}

	public void set_transcripting(boolean transcriptmode)
	{
		this.transcriptmode = transcriptmode;
	}

	public boolean transcripting()
	{
		return transcriptmode;
	}

	public void setscroll(boolean newscroll)
	{
		scroll = newscroll;
	}

	/** Moves this window onscreen to the requested position */
	public void moveto(int newleft, int newtop)
	{
		myview.moveto(newleft, newtop);
	}

	public void movecursor(int pos)
	{
		flush();
		myview.moveCursor(pos);
	}

	public void printzascii(short ascii) {
		printzascii(new short[] { ascii });
	}

	public void printzascii(short ascii[]) {
		// short because the Z-machine can put out 10-bit codes
		StringBuffer sb = new StringBuffer(ascii.length);

		for (short c : ascii) {
			sb.append(ZScreen.zascii_to_unicode(c));
		}

		if (buffer) {
			bufferString(sb.toString());
		} else {
			drawchars(sb);
		}
	}

	public void flush() {
		if (linebuffer.length() > 0) {
			drawchars(linebuffer);
			linebuffer.setLength(0);
		}
	}

	public void newline() {
		newline(true);
	}

	protected void newline(boolean flushbuffer) {
		if (flushbuffer) {
			myview.setColors(zforeground, zbackground);
			flush();
		}
		myview.newline();
		if (scroll) {
			myview.autoScroll();
		}
	}

	public void bufferString(String s) {
		bufferchars(s.toCharArray());
	}

	public synchronized void bufferchars(char chars[]) {
		linebuffer.append(chars);
	}

	public void clear() {
		myview.clear();
	}

	private void calculate_font()
	{
		Font basefont;
		int style = Font.PLAIN;

		if ((curzstyle & FIXED) != 0)
			basefont = myscreen.fixedfont;
		else
			basefont = myscreen.variablefont;
		if ((curzstyle & BOLD) != 0)
			style |= Font.BOLD;
		if ((curzstyle & ITALIC) != 0)
			style |= Font.ITALIC;

		curfont = new Font(basefont.getName(), style, basefont.getSize());
	}

	public void set_color(int foreground, int background) {
		flush();
		if (foreground != ZColor.Z_CURRENT)
			zforeground = foreground;
		if (background != ZColor.Z_CURRENT)
			zbackground = background;
	}

	public void set_text_style(int style) {
		set_text_style(style, false);
	}

	protected void set_text_style(int style, boolean immediate) {
		char thecode[] = new char[1];

		if (immediate || !buffer) {
			if (style == ROMAN)
				curzstyle = ROMAN;
			else
				curzstyle |= style;
			calculate_font();
		}
		else {
			thecode[0] = (char)(style | BUF_ROMAN);
			bufferchars(thecode);
		}
	}

	private boolean is_control(char ch)
	{
		return (ch >= FIRST_STYLE);
	}

	private void parse_control(char control) {
		if ((control >= FIRST_STYLE) && (control <= LAST_STYLE)) {
			set_text_style(control & ~BUF_ROMAN, true);
		}
	}

	private void drawchars(CharSequence cs) {
		drawchars(cs.toString().toCharArray(), 0, cs.length());
	}

	public void drawchars(char chars[], int offset, int length) {
		int firstchar;
		int runlength;
		int i;
		char control = 0;

		// System.err.println("offset length top left cy cx " + offset + " " +
				//	   length + " " + top + " " + left + " " +
				//	   cursory + " " + cursorx);
		if (length != 0) {
			myview.setColors(zforeground, zbackground);

			if (scroll) {
				myview.autoScroll();
			}

			firstchar = offset;
			runlength = 0;
			while (firstchar < (offset + length)) {
				for (i = (firstchar - offset); i < length; i++, runlength++) {
					if (is_control(chars[offset + i])) {
						control = chars[offset + i];
						break;
					}
				}
				myview.output(chars, 
						firstchar, runlength,
						(curzstyle & REVERSE) == REVERSE,
						curfont);
				parse_control(control);
				control = 0;
				firstchar += runlength + 1;
				runlength = 0;
			}
			// System.err.print(new String(chars, offset, length));
		}
	}

	/** backspace: move cursor left, erase intervening character  */
	public void backspace() {
		myview.backspace();
	}

	public void reset_line_count() {
		myview.resetAutoScroll();
	}

	/** Called by v5+ to get cursor position */
	public int getx() {
		return myview.getPosX();
	}

	/** Called by v5+ to get cursor position */
	public int gety() {
		return myview.getPosY();
	}

	/** Called by v5+ to set cursor position */
	public void movecursor(int x, int y) {
		myview.moveCursor(y, x); 
	}

	/** show or hide the cursor */
	public void showCursor(boolean show) {
		myview.showCursor(show);
	}

	/** Called by zmachine to request a specific window size */
	public void resize(int columns, int rows) {
		myview.resize(columns, rows);
	}

}

