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
import android.graphics.Typeface;
import android.util.AttributeSet;

import org.brickshadow.roboglk.io.TextGridIO;


public class TextGridView extends TextWindowView {
    private TextGridIO io;

    public TextGridView(Context context) {
        super(context);
        setTypeface(Typeface.MONOSPACE);
        this.io = null;
    }

    public TextGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        io.refresh();
    }

    public void setIO(TextGridIO io) {
        this.io = io;
    }
}
