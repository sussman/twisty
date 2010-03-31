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

import org.brickshadow.roboglk.io.StyleManager;
import org.brickshadow.roboglk.io.TextBufferIO;
import org.brickshadow.roboglk.view.TextBufferView;

import android.app.Activity;


/**
 * Smart enum type for the types of GlkWindows
 * @author gmadrid
 */
public abstract class GlkWinType {
	// You cannot create these!
	// Use the static values or getInstance().
	protected GlkWinType(int numericValue) {
		this.numericValue = numericValue;
	}
	
	public static GlkWinType getInstance(int numericValue) {
		return instances[numericValue];
	}
	
	public int getNumericValue() {
		return numericValue;
	}
	
	public abstract GlkWindow newWindow(GlkLayout glkLayout, int id); 
	
	private final int numericValue;
	
	public static final GlkWinType all = new GlkWinType(0) {
		@Override
		public GlkWindow newWindow(GlkLayout glkLayout, int id) {
			return null;
		}
		@Override public String toString() { return "ALL"; }
	};

	public static final GlkWinType pair = new GlkWinType(1) {
		@Override
		public GlkWindow newWindow(GlkLayout glkLayout, int id) {	
			return new GlkPairWindow();
		}
		@Override public String toString() { return "Pair"; }
	};
	
	public static final GlkWinType blank = new GlkWinType(2) {
		@Override 
		public GlkWindow newWindow(GlkLayout glkLayout, int id) {
			return new GlkBlankWindow();
		}
		@Override public String toString() { return "Blank"; }
	};
	
	public static final GlkWinType textBuffer = new GlkWinType(3) {
		@Override
		public GlkWindow newWindow(GlkLayout glkLayout, int id) {
			TextBufferView view = new TextBufferView(glkLayout.getContext());
			TextBufferIO io = 
				new TextBufferIO(view, new StyleManager(glkLayout.bufferStyles));
			
			// TODO(gmadrid-refactor): make sure this cast is kosher.
			GlkWindow result =  new GlkTextBufferWindow((Activity)glkLayout.getContext(),
					glkLayout.getQueue(), io, id);
			
			// TODO(gmadrid-refactor): figure out a better way to do this assignment.
			result.view = view;
			
			return result;
		}
		@Override public String toString() { return "TextBuffer"; }
	};
	
	public static final GlkWinType textGrid = new GlkWinType(4) {
		@Override
		public GlkWindow newWindow(GlkLayout glkLayout, int id) {
			// TODO(gmadrid-refactor): fix this.
			return null;
			//return new GlkTextGridWindow();
		}
		@Override public String toString() { return "TextGrid"; }
	};
	
	public static final GlkWinType graphics = new GlkWinType(5) {
		@Override
		public GlkWindow newWindow(GlkLayout glkLayout, int id) {
			return new GlkGraphicsWindow();
		}
		@Override public String toString() { return "Graphics"; }
	};
	
	// A map from the numeric values in glk.h to enum values.
	private static final GlkWinType[] instances = {
		all, pair, blank, textBuffer, textGrid, graphics
	};
}
