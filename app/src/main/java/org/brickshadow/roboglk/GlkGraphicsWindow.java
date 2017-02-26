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
 * The base class for graphics windows. It provides do-nothing
 * implementations of window methods not applicable to graphics windows.
 */
public abstract class GlkGraphicsWindow implements GlkWindow {
	
	@Override
    public final void cancelCharEvent() {}
	
	@Override
    public final int cancelLineEvent() {
        return 0;
    }
	
	@Override
    public final void cancelLinkEvent() {}
	
	@Override
    public final boolean distinguishStyles(int styl1, int styl2) {
        return false;
    }
	
	@Override
    public final boolean drawInlineImage(BlorbResource bres, int alignment) {
        return false;
    }
	
	@Override
    public final boolean drawInlineImage(BlorbResource bres, int alignment,
            int width, int height) {
        return false;
    }
	
	@Override
    public final void flowBreak() {}
	
	@Override
    public final int measureStyle(int styl, int hint) {
        throw new RuntimeException();
    }
	
	@Override
    public final void moveCursor(int xpos, int ypos) {}
	
	@Override
    public final void print(String str) {}
	
	@Override
    public final void requestCharEvent(boolean unicode) {}
	
	@Override
    public final void requestLineEvent(ByteBuffer buf, int maxlen,
            int initlen) {}
	
	@Override
    public final void requestLineEventUni(IntBuffer buf, int maxlen,
            int initlen) {}
	
	@Override
    public final void requestLinkEvent() {}
	
	@Override
    public final void setArrangement(int method, int size,
            GlkWindow key) {}
	
	@Override
    public final void setLinkValue(int val) {}
	
	@Override
    public final void setStyle(int val) {}
}
