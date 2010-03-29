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



/**
 * A base class for pair windows and blank windows. It provides
 * default do-nothing implementations for nearly all of the window
 * methods.
 */
abstract class GlkInvisibleWindow implements GlkWindow {
	
	@Override
    /** Does nothing. */
    public final void clear() {}
    
	@Override
    /** Does nothing. */
    public final void getSize(int[] dim) {}
    
	@Override
    /** Does nothing. */
    public final void print(String str) {}
    
    @Override
    /** Does nothing. */
    public final void requestCharEvent(boolean unicode) {}
    
    @Override
    /** Does nothing. */
    public final void cancelCharEvent() {}
    
    @Override
    /** Does nothing. */
    public final void requestLineEvent(ByteBuffer buf, int maxlen,
            int initlen) {}
    
    @Override
    /** Does nothing. */
    public final void requestLineEventUni(IntBuffer buf, int maxlen,
            int initlen) {}
    
    @Override
    /** Does nothing. */
    public final int cancelLineEvent() {
        return 0;
    }
    
    @Override
    /** Does nothing. */
    public final void requestLinkEvent() {}
    
    @Override
    /** Does nothing. */
    public final void cancelLinkEvent() {}
    
    @Override
    /** Does nothing. */
    public final void setLinkValue(int val) {}
    
    @Override
    /** Does nothing. */
    public final int measureStyle(int styl, int hint) {
        throw new RuntimeException();
    }
    
    @Override
    /** Does nothing. */
    public final void setStyle(int val) {}
    
    @Override
    /** Does nothing. */
    public final boolean distinguishStyles(int styl1, int styl2) {
        return false;
    }
    
    @Override
    /** Does nothing. */
    public final void moveCursor(int xpos, int ypos) {}

    @Override
    /** Does nothing. */
    public final void requestMouseEvent() {}
    
    @Override
    /** Does nothing. */
    public final void cancelMouseEvent() {}

    @Override
    /** Does nothing. */
    public final boolean drawInlineImage(BlorbResource bres, int alignment) {
        return false;
    }
    
    @Override
    /** Does nothing. */
    public final boolean drawInlineImage(BlorbResource bres, int alignment,
            int width, int height) {
        return false;
    }
    
    @Override
    /** Does nothing. */
    public final void flowBreak() {}

    @Override
    /** Does nothing. */
    public final boolean drawImage(BlorbResource bres, int x, int y) {
        return false;
    }
    
    @Override
    /** Does nothing. */
    public final boolean drawImage(BlorbResource bres, int x, int y, int width,
            int height) {
        return false;
    }
    
    @Override
    /** Does nothing. */
    public final void setBackgroundColor(int color) {}
    
    @Override
    /** Does nothing. */
    public final void eraseRect(int left, int top, int width,
            int height) {}
    
    @Override
    /** Does nothing. */
    public final void fillRect(int color, int left, int top, int width,
            int height) {}
}
