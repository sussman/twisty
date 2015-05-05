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
import org.brickshadow.roboglk.GlkTextWindow;
import org.brickshadow.roboglk.view.TextWindowView;

import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.view.KeyEvent;
import android.view.View;


public abstract class TextIO {

	/**
     * The glk window wrapper associated with a view.
     */
    protected GlkTextWindow win;
    
    protected final TextWindowView tv;
    
    /**
     * During line input, the characters entered so far. It is the
     * responsibility of concrete subclasses to initialize this upon a
     * request for line input.
     */
    protected char[] inputChars;
    
    /**
     * During line input, the number of characters entered so far.
     */
    protected int currInputLength;
    
    private SpannableStringBuilder tb;
    private boolean charInput;
    private boolean lineInput;
    private final TextKeyListener listener;
    
    /**
     * The style manager.
     */
    protected final StyleManager styleMan;
    
    TextIO(TextWindowView tv, StyleManager styleMan) {
    	this.tv = tv;
    	this.styleMan = styleMan;
    	currInputLength = 0;
    	
    	listener = TextKeyListener.getInstance(false, 
                TextKeyListener.Capitalize.NONE);
        tb = new SpannableStringBuilder(" ");
        
        tv.setOnKeyListener(new View.OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return onViewKey(v, keyCode, event);
            }
        });
        
        tv.setFocusableInTouchMode(true);
    }
	
	/**
     * Must return the width and height of the view. The width should
     * be the number of "0" (zero) characters that would fit on a line;
     * the height should be the number of lines of text that fit in the view.
     * Both measurements should be in terms of the normal font of the view.
     * 
     * @return a two-element array with the width and height of the window
     */
    public final int[] getWindowSize() {
    	synchronized(tv) {
    		return new int[] { tv.getNumLines(), tv.getCharsPerLine() };
    	}
    }
    
    public void setWindow(GlkTextWindow win) {
        this.win = win;
    }
    
    /**
     * Prepares a view for single-key input. After this method is called,
     * each key event should result in a call to {@link #sendCharToGlk(char)}
     * or {@link #sendKeyToGlk(int)}, without echoing the key to the screen.
     */
    public void doCharInput() {
    	requestInputFocus();
        charInput = true;
        lineInput = false;
    }
    
    /**
     * Sends a unicode char to the glk window wrapper.
     * <p>
     * This should not be called outside of a
     * {@code doCharInput()/stopCharInput()} series of events (but if it
     * is, glkjni will ignore any character input event that is generated
     * by the glk window wrapper).
     * 
     * @param c
     *           a character
     */
    public final void sendCharToGlk(char c) {
        /* 
         * This assumes that all special keys are indeed handled
         * by calls to sendKeyToGlk().
         */
        win.recordKey(c);
    }
    
    /**
     * Call this to send a non-printing keypress to the glk window wrapper.
     * <p>
     * This should not be called outside of a
     * {@code doCharInput()/stopCharInput()} series of events (but if it
     * is, glkjni will ignore any character input event that is generated
     * by the glk window wrapper).
     * 
     * @param keycode
     *           an Android keycode
     */
    public final void sendKeyToGlk(int keycode) {
        win.recordKey(keycode);
    }
    
    /**
     * Cancels single-key input in a view.
     */
    public void stopCharInput() {
        charInput = false;
        relinquishInputFocus();
    }
    
    /**
     * Prepares a view for line input. After this method is called, key
     * events should be handled as line input (echoed to the screen, and
     * perhaps with support for basic line editing).
     * <p>
     * While input is occurring, the current number of characters input
     * must be stored in {@link #currInputLength}, and the actual characters
     * must be placed, as they are entered, in {@link #inputChars}.
     * <p>
     * When input is finished, {@link #sendLineToGlk()} should be called.
     * <p>
     * Implementations should use the values of {@code maxlen}
     * and {@code unicode} to guide their behavior.
     * 
     * @param unicode
     *           if unicode input was requested
     * @param maxlen
     *           the maximum input length that glk is expecting
     * @param initialChars
     *           if non-null, text that should be displayed as though the
     *           player had typed it as the beginning of the input.
     */
    public void doLineInput(boolean unicode, int maxlen,
            char[] initialChars) {
    	
    	requestInputFocus();
    	
        inputChars = new char[maxlen];
        
        if (initialChars != null) {
            int len = initialChars.length;
            System.arraycopy(initialChars, 0, inputChars, 0, len);
            currInputLength = len;
        } else {
            currInputLength = 0;
        }
        
        lineInput = true;
        charInput = false;
    }
    
    /**
     * Sends a line of input to the glk window wrapper.
     * <p>
     * This should not be called outside of a
     * {@code doLineInput()/stopLineInput()} series of events (but if it
     * is, glkjni will ignore any line input event that is generated
     * by the glk window wrapper).
     * <p>
     * The input array may be modified by this method.
     * 
     * @param line a line of input
     */
    public final void sendLineToGlk() {
        win.recordLine(inputChars, currInputLength, true);
    }
    
    /**
     * Called to cancel line input. This is a wrapper which takes care
     * of communicating the current input length back to the glk window
     * wrapper and then calls {@link #stopLineInput()}.
     */
    public final int stopLineInputAndGetLength() {
        win.recordLine(inputChars, currInputLength, false);
        stopLineInput();
        return currInputLength;
    }

    /**
     * Cancels line input from a view.
     */
    public void stopLineInput() {
        lineInput = false;
        relinquishInputFocus();
    }
    
    private void requestInputFocus() {
    	if (!tv.hasFocus()) {
    		// TODO: remember which window does have focus
    		tv.requestFocus();
    	}
    }
    
    private void relinquishInputFocus() {
    	if (tv.hasFocus()) {
    		// TODO: restore focus to previous win if any
    	}
    }
    
    /**
     * Prints a string in the view. Implementations are responsible for
     * maintaining the cursor position and scrolling the text.
     * 
     * @param str the string to print.
     */
    public abstract void doPrint(String str);
    
    /**
     * Clears the view.
     */
    public abstract void doClear();
    
    /**
     * Changes the display style for newly-printed text. 
     * @param style one of the {@link GlkStyle} constants
     */
    public abstract void doStyle(int style);
    
	/**
	 * Sets the hyperlink value for newly-printed text.
	 * 
	 * @param linkval
	 *            an integer representing the target of the link, or 0 meaning
	 *            no link.
	 */
    public abstract void doHyperlink(int linkval);
    
    protected boolean onViewKey(View v, int keyCode, KeyEvent event) {
    	if (!charInput && !lineInput) {
            return true;
        }
        
        if (tb.length() != 1) {
            tb = new SpannableStringBuilder(" ");
        }
        if (Selection.getSelectionEnd(tb) != 1 ||
                Selection.getSelectionStart(tb) != 1) {
            Selection.setSelection(tb, 1);
        }
        
        switch (event.getAction()) {
        case KeyEvent.ACTION_DOWN:
            listener.onKeyDown(v, tb, keyCode, event);
            break;
        case KeyEvent.ACTION_UP:
            listener.onKeyUp(v, tb, keyCode, event);
            break;
        }
        
        if (charInput) {
            return processSingleKey(keyCode);
        } else {
            return processLineKey(keyCode, event.getAction());
        }
    }
    
    private void endLineInput() {
    	lineInput = false;
        textEchoNewline();
        sendLineToGlk();
        extendHistory();
    }
    
    private boolean processSingleKey(int keyCode) {
        charInput = false;
        switch (tb.length()) {
        case 0: // delete
            sendKeyToGlk(KeyEvent.KEYCODE_DEL);
            return true;
        case 1: // special key
        	if (keyCode == KeyEvent.KEYCODE_MENU) {
        		return false;
        	}
        	if (keyCode == KeyEvent.KEYCODE_BACK) {
        		return false;
        	}
            sendKeyToGlk(keyCode);
            return true;
        case 2: // normal char
            sendCharToGlk(tb.charAt(1));
            return true;
        default:
            return false;
        }
    }
    
    private boolean processLineKey(int keyCode, int action) {
        switch (tb.length()) {
        case 0: // delete
            if (currInputLength == 0) {
                return true;
            } else {
                Editable text = tv.getEditableText();
                int len = text.length();
                text.delete(len - 1, len);
                currInputLength -=1;
                return true;
            }
            
        case 1: // special key
        	switch (keyCode) {
        	case KeyEvent.KEYCODE_MENU:
        	case KeyEvent.KEYCODE_BACK:
        		return false;
        	case KeyEvent.KEYCODE_DPAD_UP:
        		if (action == KeyEvent.ACTION_DOWN) {
        			historyPrev();
        		}
        		break;
        	case KeyEvent.KEYCODE_DPAD_DOWN:
        		if (action == KeyEvent.ACTION_DOWN) {
        			historyNext();
        		}
        		break;
        	default:
        		break;
        	}
            /* TODO: basic line editing/cursor movement */
            return true;
            
        case 2: // normal char
            char c = tb.charAt(1);
            
            if (c == '\n') {
                endLineInput();
                return true;
            }
            
            if (currInputLength == inputChars.length) {
                return true;
            }
            
            inputChars[currInputLength++] = c;
            
            textEcho(tb.subSequence(1, 2));
            return true;
            
        default:
            return false;
        }
    }
    
    protected abstract void historyPrev();
    
    protected abstract void historyNext();
    
    protected abstract void extendHistory();
    
    protected abstract void textEcho(CharSequence str);
    
    protected abstract void textEchoNewline();
	
	public final int measureStyle(int style, int hint) {
		synchronized(styleMan) {
			return styleMan.measureStyle(style, hint);
		}
	}
	
	public final boolean distinguishStyles(int style1, int style2) {
		synchronized(styleMan) {
			return styleMan.distinguishStyles(style1, style2);
		}
	}

	public int getLinesSize(int numLines, int maxSize) {
		int lineHeight = tv.getLineHeight();
		while ((numLines * lineHeight) > maxSize) {
			numLines -= 1;
		}
		return numLines * lineHeight;
	}

	public int getCharsSize(int numChars, int maxSize) {
		// TODO: figure this out for when left/right splits
		//       are needed.
		return 0;
	}
}
