package org.brickshadow.modeltest;


import android.content.Context;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;


public class TwistyTextBufferView extends TextView {
    
    public TwistyTextBufferView(Context context) {
        super(context);
    }
    
    public TwistyTextBufferView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ScrollingMovementMethod.getInstance();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      return false;
    }
 
    /*
    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new RoboGlkInputConnection(this, false);
    }

    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }
    */
    
}
