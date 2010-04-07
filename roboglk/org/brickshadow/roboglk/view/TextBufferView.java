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

package org.brickshadow.roboglk.view;



import android.content.Context;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;


/**
 * A TextView class for text-buffer windows. The main reason for this
 * class is to override {@link #getDefaultMovementMethod()} to return
 * a {@code ScrollingMovementMethod}; this is important for correct 
 * handling of the MORE prompt.
 */
public class TextBufferView extends TextWindowView {

    public TextBufferView(Context context) {
        super(context);
    }
    
    public TextBufferView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public TextBufferView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ScrollingMovementMethod.getInstance();
    }

}
