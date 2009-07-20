package com.google.code.twisty;


import org.brickshadow.roboglk.window.RoboTextBufferWindow;
import org.brickshadow.roboglk.window.TextBufferIO;
import org.brickshadow.roboglk.window.TextBufferView;

import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.view.KeyEvent;
import android.view.View;


public class TwistyTextBufferIO extends TextBufferIO {

    private boolean charInput;
    private boolean lineInput;
    
    private final TextBufferView tv;
    
    private final TextKeyListener listener;
    private SpannableStringBuilder tb;
    
    private boolean morePrompt;
    private int linesSinceInput;
    private int moreLines;
    private int inputLineStart;
    
    public TwistyTextBufferIO(final TextBufferView tv) {
        this.tv = tv;
        
        listener = TextKeyListener.getInstance(false, 
                TextKeyListener.Capitalize.NONE);
        tb = new SpannableStringBuilder(" ");
        
        tv.setOnKeyListener(new View.OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int action = event.getAction();
                
                if (morePrompt && (action == KeyEvent.ACTION_DOWN)) {
                    int viewLines = getViewLines();
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
                
                if (!charInput && !lineInput) {
                    return false;
                }
                
                if (tb.length() != 1) {
                    tb = new SpannableStringBuilder(" ");
                }
                if (Selection.getSelectionEnd(tb) != 1 ||
                        Selection.getSelectionStart(tb) != 1) {
                    Selection.setSelection(tb, 1);
                }
                
                switch (action) {
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
                    return processLineKey(keyCode);
                }
            }
        });
    }
    
    /*
     * TODO: do not consume HOME/BACK/MENU keypress.
     */
    private boolean processSingleKey(int keyCode) {
        charInput = false;
        switch (tb.length()) {
        case 0: // delete
            sendKeyToGlk(KeyEvent.KEYCODE_DEL);
            return true;
        case 1: // special key
            sendKeyToGlk(keyCode);
            return true;
        case 2: // normal char
            sendCharToGlk(tb.charAt(1));
            return true;
        default:
            return false;
        }
    }
    
    private boolean processLineKey(int keyCode) {
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
            /* TODO: basic line editing/cursor movement */
            return true;
            
        case 2: // normal char
            char c = tb.charAt(1);
            
            if (c == '\n') {
                lineInput = false;
                textBufEcho("\n");
                sendLineToGlk();
                return true;
            }
            
            if (currInputLength == inputChars.length) {
                return true;
            }
            
            inputChars[currInputLength++] = c;
            
            textBufEcho(tb.subSequence(1, 2));
            return true;
            
        default:
            return false;
        }
    }
    
    public void setWindow(RoboTextBufferWindow win) {
        this.win = win;
    }
    
    /* TODO: account for padding? */
    private int getViewLines() {
        return tv.getHeight() / tv.getLineHeight();
    }
    
    private boolean needsMorePrompt(int linesAdded) {
        linesSinceInput += linesAdded;
        
        int viewLines = getViewLines();
        // TODO: maybe >= instead? or >= viewLines - 1 ?
        if (linesSinceInput > viewLines) {
            moreLines = linesSinceInput - viewLines;
            
            morePrompt = true; // TODO: show the prompt!
            
            tv.scrollBy(0, inputLineStart * tv.getLineHeight());
            return true;
        } else {
            inputLineStart -= linesAdded;
        }
        
        return false;
    }
    
    /* Prints text and decides if the MORE prompt is needed. */
    private void textBufPrint(CharSequence str) {
        
        /* If the cursor offset equals the length of the text, append()
         * will advance the cursor (and automatically scroll) whether
         * we want it to or not. So we back the cursor up until we know
         * if we should scroll to end or display the MORE prompt.  
         */
        Spannable text = (Spannable) tv.getText();
        Selection.setSelection(text, text.length() - 1);
        
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
        
        text = (Spannable) tv.getText();
        Selection.setSelection(text, text.length());
    }

    /* When echoing text, we don't have to worry about the MORE prompt,
     * so we always advance the cursor to the end of the text.
     */
    private void textBufEcho(CharSequence str) {
        tv.append(str);
        
        Spannable text = (Spannable) tv.getText();
        Selection.setSelection(text, text.length());
    }
    
    @Override
    public void doCharInput() {
        charInput = true;
        lineInput = false;
    }

    @Override
    public void doLineInput(boolean unicode, int maxlen,
            char[] initialChars) {
        
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
        
        linesSinceInput = 0;
        
        inputLineStart = computeInputLineStart();
    }
    
    /* TODO: This will have to take clear() into account. */
    private int computeInputLineStart() {
        int lineCount = tv.getLineCount();
        int viewLines = getViewLines();
        if (lineCount < viewLines) {
            return lineCount;
        } else {
            return viewLines;
        }
    }

    @Override
    public void doPrint(String str) {
        textBufPrint(str);
    }

    @Override
    public int[] getWindowSize() {
        /* TODO: return the real size. */
        return new int[] { 0, 0 };
    }

    @Override
    public void stopCharInput() {
        charInput = false;
    }

    @Override
    public void stopLineInput() {
        lineInput = false;
    }

}
