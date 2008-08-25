/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import russotto.zplet.ZColor;

import com.google.twisty.zplet.Font;

public class ZWindow implements java.io.Serializable {
		 final static int ROMAN = 0;
		 final static int REVERSE = 1;
		 final static int BOLD = 2;
		 final static int ITALIC = 4;
		 final static int FIXED = 8;

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

		 ZScreen myscreen;
		 int top, left, width, height;
		 int cursorx, cursory;
		 boolean buffer, wrap, scroll, transcriptmode;
		 int curzfont = NORMAL_FONT;
		 int curzstyle = ROMAN;
		 Font curfont;
		 String linebuffer;
		 int line_counter;
		 int zforeground, zbackground;
		 int residual;

		 public ZWindow(ZScreen screen) {
				 top = 0;
				 left = 0;
				 width = 10;
				 height = 10;
				 cursorx = 0;
				 cursory = 0;
				 line_counter = 0;
				 residual = 0;
				 myscreen = screen;
				 curfont = screen.fixedfont;
				 buffer = true;
				 wrap = true;
				 scroll = true;
				 transcriptmode = true;
				 linebuffer = "";
				 zforeground = screen.zforeground;
				 zbackground = screen.zbackground;
		 }

		 public void reset_line_count() {
				 line_counter = 0;
		 }

		 void count_line() {
				 line_counter++;
		 }
		
		 void check_for_more() {
				 String more  = "[MORE]";
				 String blank = "         ";

				 if (line_counter >= (height-1)) {
						 myscreen.settext(top + cursory, left, more.toCharArray(), 0, more.length(), true, myscreen.fixedfont);
						 myscreen.read_code();
						 myscreen.settext(top + cursory, left, blank.toCharArray(), 0, blank.length(), false, myscreen.fixedfont);
						 line_counter = 0;
				 }
				
		 }
		
		 public void erase_line(short arg) {
				char spaces[] = new char[width];
				short i;
				
				for (i = 0; i < width; i++)
					spaces[i] = ' ';
				if (arg == 1)
					myscreen.settext(cursory, left + cursorx, spaces, 0, width - cursorx);
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
				 wrap = wrapmode;
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

		 public void moveto(int newleft, int newtop)
		 {
				 left = newleft;
				 top = newtop;
		 }

		 public void resize (int newwidth, int newheight)
		 {
		     //		     System.err.println("resize: " + newwidth + " " + newheight );
				 width = newwidth;
				 height = newheight;
				 if ((cursorx >= width) || (cursory >= height)) {
						 movecursor_noflush(0,0);
				 }
		 }

		 public int getLeft() {
				 return left;
		 }

		 public int getTop() {
				 return top;
		 }

		 public int getWidth() {
				 return width;
		 }

		 public int getHeight() {
				 return height;
		 }

		 public int getlines() {
				 return height;
		 }

		 public int getchars() {
				 return width;
		 }

		 public int getx() {
				 return cursorx;
		 }

		 public int gety() {
				 return cursory;
		 }

		 void movecursor_noflush(int x, int y)
		 {
				 cursorx = x;
				 cursory = y;
		 }

		 public void movecursor(int x, int y)
		 {
				 flush();
				 cursorx = x;
				 cursory = y;
		 }
		 
		 public void printzascii(short ascii) {
				 short zascii[] = new short[1];

				 zascii[0] = ascii;
				 printzascii(zascii);
		 }

		 public void printzascii(short ascii[]) {
				 /* short because the Z-machine can put out 10-bit codes */
				 char unicode[];
				 int i;
				
				 unicode = new char[ascii.length];

				 for (i = 0; i < unicode.length; i++) {
						 unicode[i] = ZScreen.zascii_to_unicode(ascii[i]);
				 }

				 if (buffer) {
						 bufferchars(unicode);
				 }
				 else {
						 drawchars(unicode,0, unicode.length);
				 }
		 }

		 public void flush() {
		 		residual = charsWidth(linebuffer.toCharArray(), 0, linebuffer.length());
				drawstring(linebuffer);
				linebuffer = "";
//				System.err.println();
//				System.err.println("linebuffer.length() = " + linebuffer.length());
		 }
 
		 public void newline() {
				 newline(true);
		 }

		 protected void newline(boolean flushbuffer) {
				 if (myscreen.zforeground != zforeground)
						 myscreen.setZForeground(zforeground);
				 if (myscreen.zbackground != zbackground)
						 myscreen.setZBackground(zbackground);

				 if (flushbuffer)
						 flush();
						 
				 residual = 0;

				 if (cursory == height-1) {
						 if (scroll)
								 myscreen.scrollLines(top, height, 1);
						 movecursor_noflush(0, cursory);
				 }
				 else {
						 movecursor_noflush(0, cursory+1);
				 }
				 count_line();
//				 System.err.println();
		 }

		 public void bufferString(String s) {
			 bufferchars(s.toCharArray());
		 }
		 
		 public synchronized void bufferchars(char chars[]) {
				 int last;
				 int space;
				 String printstring;

//				 linebuffer = linebuffer + chars;
				 linebuffer = new StringBuilder(linebuffer).append(chars).toString();

				 if (wrap) {
						 last = linebuffer.length();
						 while ((residual + charsWidth(linebuffer.toCharArray(), 0, last))
								 > (myscreen.size().width)) {
								 space = linebuffer.lastIndexOf(' ', last);
								 if (space == -1) {
										 while ((residual + charsWidth(linebuffer.toCharArray(), 0, last)) > 
														(width * myscreen.charwidth()))
												 last--;
										 drawchars(linebuffer.toCharArray(), 0, last);
										 linebuffer = linebuffer.substring(last);
										 newline(false);
										 last = linebuffer.length();
								 }
								 else if ((residual + charsWidth(linebuffer.toCharArray(), 0, space)) <=
												  (myscreen.size().width)) {
										 printstring = linebuffer.substring(0, space);
										 drawstring(printstring);
										 while ((space < linebuffer.length())
												 && (linebuffer.charAt(space) == ' ')) {
												 space++;
										 }
										 linebuffer = linebuffer.substring(space);
										 last = linebuffer.length();
										 newline(false);
								 }
								 else
										 last = space-1;
						 }
				}
		 }

		 public void clear() {
				 int i;
				 char spaces[] = new char[width];

				 for (i = 0; i < width; i++)
						 spaces[i] = ' ';
				
				 for (i = top; i < top + height; i++) {
						 myscreen.settext(i, left, spaces, 0, width);
				 }
		 }

		 private void calculate_font()
		 {
				 Font basefont;
				 int style = Font.PLAIN;

				 basefont = myscreen.fixedfont;

				 if ((curzstyle & BOLD) != 0)
						 style |= Font.BOLD;
				 if ((curzstyle & ITALIC) != 0)
						 style |= Font.ITALIC;

				 curfont = new Font(basefont.getName(), style, basefont.getSize());
		 }

		 public void set_color(int foreground, int background) {
//			System.err.println("fg = " + foreground + ", bg = " + background);
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

		 public void drawchars(char chars[], int offset, int length) {
				 int firstchar;
				 int runlength;
				 int i;
				 char control = 0;

//				 System.err.println("offset length top left cy cx " + offset + " " +
//												  length + " " + top + " " + left + " " +
//												  cursory + " " + cursorx);
				 if (length != 0) {
						 if (myscreen.zforeground != zforeground)
								 myscreen.setZForeground(zforeground);
						 if (myscreen.zbackground != zbackground)
								 myscreen.setZBackground(zbackground);

						 if (scroll && (cursorx == 0))
								 check_for_more();
						
						 firstchar = offset;
						 runlength = 0;
						 while (firstchar < (offset + length)) {
								 for (i = (firstchar - offset); i < length; i++, runlength++) {
										 if (is_control(chars[offset + i])) {
												 control = chars[offset + i];
												 break;
										 }
								 }
								 myscreen.settext(top + cursory, left + cursorx, chars, 
																  firstchar, runlength,
																  (curzstyle & REVERSE) == REVERSE,
																  curfont);
								 parse_control(control);
								 control = 0;
								 firstchar += runlength + 1;
								 cursorx += runlength;
								 runlength = 0;
						 }
//				 System.err.print(new String(chars, offset, length));
				 }
		 }

		 public void drawstring(String text)
		 {
				 drawchars(text.toCharArray(), 0, text.length());
		 }

		 public int charsWidth(char line[], int offset, int length) {
				 return myscreen.stringWidth(line, offset, length, curfont);
		 }
 }

