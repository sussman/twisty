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

import org.brickshadow.roboglk.io.TextIO;
import org.brickshadow.roboglk.util.GlkEventQueue;

import android.app.Activity;


public class GlkTextGridWindow extends GlkTextWindow {
    
    public GlkTextGridWindow(Activity activity, GlkEventQueue queue, TextIO io,
			int id) {
		super(activity, queue, io, id);
	}

	/** Does nothing. */
	@Override
    public final boolean drawInlineImage(BlorbResource bres, int alignment) {
        return false;
    }
    
    /** Does nothing. */
	@Override
    public final boolean drawInlineImage(BlorbResource bres, int alignment,
            int width, int height) {
        return false;
    }
    
    /** Does nothing. */
	@Override
    public final void flowBreak() {}

	@Override
	public void moveCursor(int xpos, int ypos) {
		// TODO Auto-generated method stub
	}
}
