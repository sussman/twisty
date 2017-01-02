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

import org.brickshadow.roboglk.view.TextGridView;


public class TextGridIO extends TextIO {

	public TextGridIO(TextGridView tv, StyleManager styleMan) {
		super(tv, styleMan);
	}

	@Override
	public void doClear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doPrint(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void textEcho(CharSequence str) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Does nothing. Newlines after input are not echoed into a text grid
	 * window.
	 */
	@Override
	protected final void textEchoNewline() {}

	@Override
	public void doStyle(int style) {}
	
	@Override
	public void doHyperlink(int linkval) {}

	/**
	 * Does nothing. History is not supported in text grid windows.
	 */
	@Override
	protected void extendHistory() {}

	/**
	 * Does nothing. History is not supported in text grid windows.
	 */
	@Override
	protected void historyNext() {}

	/**
	 * Does nothing. History is not supported in text grid windows.
	 */
	@Override
	protected void historyPrev() {}

}
