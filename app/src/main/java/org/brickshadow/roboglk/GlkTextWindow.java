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

package org.brickshadow.roboglk;


import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.brickshadow.roboglk.io.TextIO;
import org.brickshadow.roboglk.util.GlkEventQueue;
import org.brickshadow.roboglk.util.UISync;

import android.app.Activity;
import android.view.KeyEvent;


public abstract class GlkTextWindow extends GlkWindow {

	private final TextIO io;
	private final GlkEventQueue queue;
    private final Activity activity;

	// Whether glk is expecting Unicode or Latin-1 input.
    private boolean inputIsUnicode;
    
    // The maximum input line length that glk is prepared to accept.
    private int maxInputLength;
    
    // Buffers for line input.
    private ByteBuffer latinBuffer;
    private IntBuffer unicodeBuffer;
    
    private final UISync uiWait;
    private volatile int currInputLength;

    private final int id;
    
	public GlkTextWindow(Activity activity, GlkEventQueue queue, TextIO io,
			int id) {

		this.io = io;
		this.queue = queue;
		this.activity = activity;
		this.id = id;
		uiWait = UISync.getInstance();
		io.setWindow(this);
	}
	
	public void recordKey(char c) {
		if (c == '\n') {
            processKey(GlkKeycode.Return);
        } else {
            if (!inputIsUnicode && c > 0xFF) {
                processKey('?');
            } else {
                processKey(c);
            }
        }
	}

	public void recordKey(int c) {
		switch (c) {
        case KeyEvent.KEYCODE_DEL:
            processKey(GlkKeycode.Delete);
            break;
        case KeyEvent.KEYCODE_DPAD_UP:
            processKey(GlkKeycode.Up);
            break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            processKey(GlkKeycode.Down);
            break;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            processKey(GlkKeycode.Left);
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            processKey(GlkKeycode.Right);
            break;
        
        /* These values taken from zplet.Event */
        case 100033: // F1
            processKey(GlkKeycode.Func1);
            break;
        case 100034: // F2
            processKey(GlkKeycode.Func2);
            break;
        case 100035: // F3
            processKey(GlkKeycode.Func3);
            break;
        case 100036: // F4
            processKey(GlkKeycode.Func4);
            break;
        case 100037: // F5
            processKey(GlkKeycode.Func5);
            break;
        case 100038: // F6
            processKey(GlkKeycode.Func6);
            break;
        case 100039: // F7
            processKey(GlkKeycode.Func7);
            break;
        case 100040: // F8
            processKey(GlkKeycode.Func8);
            break;
        case 100041: // F9
            processKey(GlkKeycode.Func9);
            break;
        case 100042: // F10
            processKey(GlkKeycode.Func10);
            break;
        case 100043: // F11
            processKey(GlkKeycode.Func11);
            break;
        case 100044: // F12
            processKey(GlkKeycode.Func12);
            break;
        default:
            processKey(GlkKeycode.Unknown);
            break;
        }
	}
	
	private void processKey(int c) {
        queue.putEvent(GlkEventQueue.newCharInputEvent(this, c));
    }

	public void recordLine(char[] line, int len, boolean isEvent) {
		int inputLen = (len > maxInputLength ?
                maxInputLength : len);
        if (inputIsUnicode) {
            for (int i = 0; i < inputLen; i++) {
                unicodeBuffer.put(i, line[i]);
            }
        } else {
            for (int i = 0; i < inputLen; i++) {
                char c = (line[i] > 0xFF ? '?' : line[i]);
                latinBuffer.put(i, (byte) c);
            }
        }
        if (isEvent) {
            queue.putEvent(GlkEventQueue.newLineInputEvent(this, inputLen));
        }
	}

	@Override
	public void cancelCharEvent() {
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                io.stopCharInput();
            }
        });
	}

	@Override
	public int cancelLineEvent() {
		uiWait.waitFor(new Runnable() {
            @Override
            public void run() {
                currInputLength = io.stopLineInputAndGetLength();
                uiWait.stopWaiting(null);
            }
        });
        
        return currInputLength;
	}

	@Override
	public void cancelLinkEvent() {
		// TODO: hyperlink support
	}

	@Override
	public void cancelMouseEvent() {
		// TODO: mouse/touch support
	}

	@Override
	public void clear() {
		activity.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			io.doClear();
    		}
    	});
	}

	@Override
	public boolean distinguishStyles(int styl1, int styl2) {
		return io.distinguishStyles(styl1, styl2);
	}

	@Override
	public abstract boolean drawInlineImage(BlorbResource bres, int alignment);

	@Override
	public abstract boolean drawInlineImage(BlorbResource bres, int alignment,
			int width, int height);

	@Override
	public abstract void flowBreak();

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void getSize(int[] dim) {
		int[] size = io.getWindowSize();
		dim[0] = size[0];
		dim[1] = size[1];
	}

	@Override
	public int getSizeFromConstraint(int constraint, boolean vertical,
			int maxSize) {

		if (vertical) {
			return io.getLinesSize(constraint, maxSize);
		} else {
			return io.getCharsSize(constraint, maxSize);
		}
	}

	@Override
	public int measureStyle(int styl, int hint) {
		return io.measureStyle(styl, hint);
	}

	@Override
	public abstract void moveCursor(int xpos, int ypos);

	@Override
	public void print(final String str) {
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                io.doPrint(str);
            }
        });
	}

	@Override
	public void requestCharEvent(final boolean unicode) {
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputIsUnicode = unicode;
                io.doCharInput();
            } 
        });
	}

	@Override
	public void requestLineEvent(ByteBuffer buf, int maxlen, int initlen) {
		lineRequest(buf, null, maxlen, initlen);
	}

	@Override
	public void requestLineEventUni(IntBuffer buf, int maxlen, int initlen) {
		lineRequest(null, buf, maxlen, initlen);
	}
	
	private void lineRequest(final ByteBuffer lbuf, final IntBuffer ubuf,
			final int maxlen, final int initlen) {
	        
		final char[] iChars =
			getInitialChars(lbuf, ubuf, maxlen, initlen);
	        
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				latinBuffer = lbuf;
				unicodeBuffer = ubuf;
				maxInputLength = maxlen;
				inputIsUnicode = (ubuf != null);
				io.doLineInput(inputIsUnicode, maxlen, iChars);
			}
		});
	}
	
	private char[] getInitialChars(ByteBuffer lbuf, IntBuffer ubuf, int maxlen,
			int initlen) {

		char[] initialChars = null;
        
        /*
         * I think that very few games make use of the "initial input"
         * feature, so this code should not have too great an impact
         * on performance. It would also be possible but tedious to
         * push this logic down into C code in glkjni.
         */
        if (initlen != 0) {
            if (lbuf != null) {
                initialChars = new char[initlen];
                for (int i = 0; i < initlen; i++) {
                    initialChars[i] = (char) lbuf.get(i);
                }
            } else {
                StringBuilder initialString = new StringBuilder(initlen);
                for (int i = 0; i < initlen; i++) {
                    int c = ubuf.get(i);
                    /*
                     * It is unlikely but legal for a glk program to use
                     * Unicode characters outside the BMP.
                     */
                    if (c > 0xFFFF) {
                        c -= 0x10000;
                        int surr1 = 0xD800 | (c >> 10);
                        int surr2 = 0xDC00 | (c & 0x3FF);
                        initialString.append((char) surr1);
                        initialString.append((char) surr2);
                    } else {
                        initialString.append((char) c);
                    }
                }
                int len = initialString.length();
                if (len > maxlen) {
                    len = maxlen;
                }
                initialChars = new char[len];
                initialString.getChars(0, len, initialChars, 0);
            }
        }
        
        return initialChars;
	}

	@Override
	public void requestLinkEvent() {
		// TODO: hyperlink support
	}

	@Override
	public void requestMouseEvent() {
		// TODO: mouse/touch support
	}

	@Override
	public void setLinkValue(final int linkval) {
		activity.runOnUiThread(new Runnable() {
        	@Override
        	public void run() {
        		io.doHyperlink(linkval);
        	}
        });
	}

	@Override
	public void setStyle(final int val) {
		activity.runOnUiThread(new Runnable() {
        	@Override
        	public void run() {
        		io.doStyle(val);
        	}
        });
	}

}
