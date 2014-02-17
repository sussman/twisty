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


import org.brickshadow.roboglk.GlkTextBufferWindow;
import org.brickshadow.roboglk.view.TextBufferView;

import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.view.KeyEvent;
import android.view.View;


public class TextBufferIO extends TextIO {

    private boolean morePrompt;
    private int linesSinceInput;
    private int moreLines;
    private int inputLineStart;
    
    private static int HISTORYLEN = 25;
    private char[][] history;
    private char[] historyTemp;
    private int historyPos;
    private int historyStart;
    private int historyEnd;
    
    public TextBufferIO(TextBufferView tv, StyleManager styleMan) {
    	super(tv, styleMan);
    	
    	history = new char[HISTORYLEN][];
    	historyPos = -1;
    }
    
    @Override
	public void doStyle(int style) {
    	styleMan.applyStyle(style, tv.getEditableText());
    }

    @Override
	public void doHyperlink(int linkval) {
    	styleMan.applyHyperlink(linkval, tv.getEditableText());
    }
    
    public void doReverseVideo(boolean reverse) {
		styleMan.applyStyle(reverse, tv.getEditableText());
	}
    
    protected boolean onViewKey(View v, int keyCode, KeyEvent event) {
        if (morePrompt && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            int viewLines = tv.getNumLines();
            int scrollLines =
                ((moreLines > viewLines) ? viewLines : moreLines);
            tv.scrollBy(0, scrollLines * tv.getLineHeight());
            moreLines -= scrollLines;
            
            if (moreLines == 0) {
                morePrompt = false; // TODO: hide the prompt!

                textBufPrint("");
            }
            return true;
        }
        
        return super.onViewKey(v, keyCode, event);
    }
    
    public void setWindow(GlkTextBufferWindow win) {
        this.win = win;
    }
    
    protected void cursorOff() {
    	Spannable text = (Spannable) tv.getText();
    	Selection.removeSelection(text);
    }
    
    protected void cursorToEnd() {
    	cursorToEnd(0);
    }
    
    protected void cursorToEnd(int back) {
    	Spannable text = (Spannable) tv.getText();
    	int len = text.length();
    	Selection.setSelection(text, len + back);
    }
    
    /* Prints text and decides if the MORE prompt is needed. */
    private void textBufPrint(CharSequence str) {
        
        /* If the cursor offset equals the length of the text, append()
         * will advance the cursor (and automatically scroll) whether
         * we want it to or not. So we back the cursor up until we know
         * if we should scroll to end or display the MORE prompt.  
         */
        cursorToEnd(-1);
        
        int oldLineCount = tv.getLineCount();
        tv.append(str);
        int linesAdded = tv.getLineCount() - oldLineCount;
        
        if (morePrompt) {
            moreLines += linesAdded;
            return;
        }
        
        if (needsMorePrompt(linesAdded)) {
            return;
        }
        
        cursorToEnd();
    }
    
    private boolean needsMorePrompt(int linesAdded) {
        linesSinceInput += linesAdded;
        
        int viewLines = tv.getNumLines();
        // TODO: maybe >= instead? or >= viewLines - 1 ?
        if (linesSinceInput > viewLines) {
            moreLines = linesSinceInput - viewLines;
            morePrompt = true;
            
            cursorOff();
            tv.scrollBy(0, inputLineStart * tv.getLineHeight());
            
            // TODO: actually show the prompt!
            
            return true;
        } else {
            inputLineStart -= linesAdded;
        }
        
        return false;
    }
    

    /* When echoing text, we don't have to worry about the MORE prompt,
     * so we always advance the cursor to the end of the text.
     */
    private void textBufEcho(CharSequence str) {
        tv.append(str);
        cursorToEnd();
    }
    
    @Override
    protected final void textEcho(CharSequence str) {
    	textBufEcho(str);    	
    }
    
    @Override
    protected final void textEchoNewline() {
    	textBufEcho("\n");
    }

    @Override
    public final void doLineInput(boolean unicode, int maxlen,
            char[] initialChars) {
        
        super.doLineInput(unicode, maxlen, initialChars);
        
        linesSinceInput = 0;
        inputLineStart = computeInputLineStart();
        
        if (historyTemp == null || historyTemp.length < maxlen) {
        	historyTemp = new char[maxlen];
        }
    }
    
    /* TODO: This will have to take clear() into account. */
    private int computeInputLineStart() {
        int lineCount = tv.getLineCount();
        int viewLines = tv.getNumLines();
        if (lineCount < viewLines) {
            return lineCount;
        } else {
            return viewLines - 1;
        }
    }

    @Override
    public final void doPrint(String str) {
        textBufPrint(str);
    }

    private boolean isLastHistory() {
    	char[] last = history[historyEnd];
    	if (last == null || last.length != currInputLength) {
    		return false;
    	}
    	int i;
    	for (i = 0; i < currInputLength; i++) {
    		if (last[i] != inputChars[i]) {
    			break;
    		}
    	}
    	return (i == currInputLength);
    }
    
    private void historyPull(char[] cmd) {
    	int cmdlen = (cmd.length > inputChars.length) ?
    			inputChars.length : cmd.length;
    	
    	Editable text = (Editable) tv.getText();
    	int textlen = text.length();
    	
    	if (cmdlen == 0) {
    		text.delete(textlen - currInputLength, textlen);
    	} else {
    		System.arraycopy(cmd, 0, inputChars, 0, cmdlen);
    		text.replace(textlen - currInputLength, textlen,
        			String.valueOf(inputChars), 0, cmdlen);
    	}
    	
    	currInputLength = cmdlen;
    }
    
	@Override
	protected void extendHistory() {
		historyPos = -1;
		if (isLastHistory() || currInputLength == 0) {
			return;
		}
		if (history[historyEnd] != null) {
			historyEnd += 1;
			if (historyEnd >= HISTORYLEN) {
				historyEnd -= HISTORYLEN;
			}
			if (historyEnd == historyStart) {
				historyStart += 1;
				if (historyStart >= HISTORYLEN) {
					historyStart -= HISTORYLEN;
				}
			}
		}
		history[historyEnd] = new char[currInputLength];
		System.arraycopy(inputChars, 0, history[historyEnd], 0,
				currInputLength);
	}

	@Override
	protected void historyNext() {
		if (historyPos == -1) {
			return;
		}
		if (historyPos == historyEnd) {
			historyPos = -1;
			historyPull(historyTemp);
		} else {
			historyPos += 1;
			if (historyPos >= HISTORYLEN) {
				historyPos -= HISTORYLEN;
			}
			historyPull(history[historyPos]);
		}
	}

	@Override
	protected void historyPrev() {
		if (historyPos == historyStart) {
			return;
		}
		if (historyPos == -1) {
			if (history[historyEnd] == null) {
				return;
			}
			historyTemp = new char[currInputLength];
			System.arraycopy(inputChars, 0, historyTemp, 0, currInputLength);
			historyPos = historyEnd;
		} else {
			historyPos -= 1;
			if (historyPos < 0) {
				historyPos = HISTORYLEN - 1;
			}
		}

		historyPull(history[historyPos]);
	}
	
	/*
     * The following may become abstract or move up to TextIO?.
     */

    @Override
    public void doClear() {}
}
