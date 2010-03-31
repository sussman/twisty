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

import android.util.Log;
import android.view.View;


/**
 * A Glk window.
 * <p>
 * This is the base class for all other {@code GlkWindow} types. It provides
 * stubs for all of the window methods which will optionally log warnings if
 * called.
 * <p>
 * Concrete classes derived from {@code GlkWindow} implement various window
 * types described in Section 3, Windows, of the GLK spec. Not all derived
 * classes will require all of these methods. {@code GlkJNI} will not call a
 * method on the wrong type of window. 
 * <p>
 * In general, the name of the method will be the same as the C function
 * defined in the spec, converted to camel case with the {@code 'glk_window_'}
 * prefix removed.
 */
public class GlkWindow {
	// 0 = nothing, 1 = methodName, 2 = methodName and stack trace
	static private int log_level;
	
	/**
	 * Debugging helper to log when a method on the base class is called.
	 * TODO(gmadrid): get rid of this at some point
	 * @param methodName
	 */
	static private void MaybeLogUsage(String methodName) {
		if (log_level == 0) return;
		if (log_level == 1) Log.w("GlkWindow", methodName + " unimplemented.");
		if (log_level == 2) 
			Log.w("GlkWindow", methodName + " unimplemented", new Exception());
	}

    /**
     * Returns the size of the window in the {@code dim[]} array.
     * <ul>
     * <li><b>{@code dim[0]}</b> - the window width</li>
     * <li><b>{@code dim[1]}</b> - the window height</li>
     * </ul>
     * Applies to: text, graphics, pair
     * 
     * @param dim
     *           Two element array in which to return the 
     *           width and height of the window.<p>
     * @see See GLK spec Section 3.3, Changing Window Constraints
     */
    public void getSize(int[] dim) {
    	MaybeLogUsage("getSize");
    }

    /**
     * Rearranges the children of the window.
     * <p>
     * Applies to: pair
     * 
     * @param method
     *           The new split method.<p>
     * @param size
     *           The new size constraint.<p>
     * @param key
     *           The new key window.<p>
     * @see GLK spec Section 3.3, Changing Window Constraints
     */
    public void setArrangement(int method, int size, GlkWindow key) {
    	MaybeLogUsage("setArrangement");
    }

    /**
     * Positions the cursor in the window.
     * <p>
     * Applies to: text grid
     * 
     * @param xpos
     *           The x position of the cursor. If this is past the end of
     *           a line, the cursor moves to the beginning of the next
     *           line.<p>
     * @param ypos
     *           The y position of the cursor. If this is greater than the
     *           height of the window, the cursor goes "off screen" and
     *           further printing has no effect.
     * @see See GLK spec Section 3.5.4, Text Grid Windows
     */
    public void moveCursor(int xpos, int ypos) {
    	MaybeLogUsage("moveCursor");
    }

    /**
     * Clears the window.
     * @see see GLK spec Section 3.7, Other Window Functions.
     */
    public void clear() {
    	MaybeLogUsage("clear");
    }

    /**
     * Requests character input in the window. This method will not be
     * called when there is a pending request for character or line input.
     * <p>
     * Applies to: text
     * @param unicode
     *           True if Unicode input is requested, false for Latin-1.
     * @see see GLK spec Section 4.1, Character Input Events
     */
    public void requestCharEvent(boolean unicode) {
    	MaybeLogUsage("requestCharEvent");
    }

    /**
     * Cancels character input for the text window. This method may be
     * called when there is no pending request for character input.
     * <p>
     * Applies to: text
     * @see see GLK spec Section 4.1, Character Input Events
     */
    public void cancelCharEvent() {
    	MaybeLogUsage("cancelCharEvent");
    }

    /**
     * Requests Latin-1 line input in the text window.
     * <p>
     * The {@code buf} parameter should be saved somewhere, and filled
     * with appropriate keycode values as the user presses keys. If the
     * {@code initlen} parameter is non-zero, the contents of the buffer
     * up to that length should be displayed as if the player had
     * typed them.
     * <p>
     * Applies to: text
     * 
     * @param buf
     *           A buffer for storing the Latin-1 bytes of the user's
     *           input.<p>
     * @param maxlen
     *           The available length of the buffer.<p>
     * @param initlen
     *           If non-zero, the length of pre-existing data in the
     *           buffer.<p>
     * @see see GLK spec Section 4.2, Line Input Events
     */
    public void requestLineEvent(ByteBuffer buf, int maxlen, int initlen) {
    	MaybeLogUsage("requestLineEvent");
    }

    /**
     * Requests Unicode line input in the text window.
     * <p>
     * The {@code buf} parameter should be saved somewhere, and filled
     * with appropriate keycode values as the user presses keys. If the
     * {@code initlen} parameter is non-zero, the contents of the buffer
     * up to that length should be displayed as if the player had
     * typed them.
     * <p>
     * Applies to: text
     *  
     * @param buf
     *           A buffer for storing the Unicode codepoints of the user's
     *           input.<p>
     * @param maxlen
     *           The available length of the buffer.<p>
     * @param initlen
     *           If non-zero, the length of pre-existing data in the
     *           buffer.<p>
     * @see see GLK spec Section 4.2, Line Input Events
     */
    public void requestLineEventUni(IntBuffer buf, int maxlen, int initlen) {
    	MaybeLogUsage("requestLineEventUni");
    }

    /**
     * Cancels line input for the text window. This method may be called
     * when there is no pending request for line input.
     * <p>
     * Applies to: text
     *  
     * @return
     *           The number of characters, if any, already entered
     *           during an active line input request.
     * @see see GLK spec Section 4.2, Line Input Events
     */
    public int cancelLineEvent() {
    	MaybeLogUsage("cancelLineEvent");
    	return 0;
    }
    /**
     * Requests a mouse event for the window. This will not be called
     * if there is already a request for mouse input in the window.
     * <p>
     * Applies to: text, graphics
     * @see See GLK spec Section 4.3, Mouse Input Events
     */
    public void requestMouseEvent() {
    	MaybeLogUsage("requestMouseEvent");	
    }
    
    /**
     * Cancels mouse events for the window. This method may be called
     * when there is no pending request for a mouse event.
     * <p>
     * Applies to: text, graphics
     * @see See GLK spec Section 4.3, Mouse Input Events
     */
    public void cancelMouseEvent() {
    	MaybeLogUsage("cancelMouseEvent");
    }
    
    /**
     * Returns the current value of a style attribute in the window.
     * <p>
     * Applies to: text
     * 
     * @param styl
     *           One of the constants in {@link GlkStyle}.<p>
     * @param hint
     *           One of the constants in {@link GlkStyleHint}.<p>
     * @throws RuntimeException
     *           if the style attribute cannot be measured.
     * @return
     *           The current value of the style attribute.
     * TODO(gmadrid): change the name of this to conform with standard
     * TODO(gmadrid): allow for possibility that it returns NULL
     * @see See GLK spec Section 5.5.2, Testing the Appearance of Styles
     */
    public int measureStyle(int styl, int hint) throws RuntimeException {
    	MaybeLogUsage("measureStyle");
    	throw new RuntimeException();
    }

    /**
     * Checks if two styles are visually distinguishable in the window.
     * <p>
     * Applies to: text
     * 
     * @param styl1
     *           One of the constants in {@link GlkStyle}.<p>
     * @param styl2
     *           One of the constants in {@link GlkStyle}.<p>
     * @return
     *           True if the styles are visually distinguishable.
     * TODO(gmadrid): change name to conform with standard
     * @see See GLK spec Section 5.5.2, Testing the Appearance of Styles
     */
    public boolean distinguishStyles(int styl1, int styl2) {
    	MaybeLogUsage("distinguishStyles");
    	return false;
    }

    /**
     * Requests hyperlink input in the text window. This method will not
     * be called when there is a pending request for hyperlink input.
     * <p>
     * Applies to: text
     * TODO(gmadrid): change the name to match the standard
     * @see See GLK spec Section 9.2, Accepting Hyperlink Events
     */
    public void requestLinkEvent() {
    	MaybeLogUsage("requestLinkEvent");
    }

    /**
     * Cancels hyperlink input for the text window. This method may be
     * called when there is no pending request for hyperlink input.
     * <p>
     * Applies to: text
     * TODO(gmadrid): change the name to match the standard
     * @see See GLK spec Section 9.2, Accepting Hyperlink Events
     */
    public void cancelLinkEvent() {
    	MaybeLogUsage("cancelLinkEvent");
    }

    /**
     * Prints a string in the window. It is possible for this method
     * to be called twice in immediate succession, and since the
     * underlying C library does not manage scrolling/pausing, this
     * method may be called while the Java frontend has paused output.
     * <p>
     * Applies to: text
     * <p>
     * <b>Note:</b> GlkJNI buffers text output until the next call to
     * {@code glk_select}, {@code glk_poll}, {@code glk_set_style},
     * {@code glk_set_hyperlink}, {@code glk_request_line_input},
     * {@code glk_window_move_cursor}, or {@code glk_exit}.
     * 
     * @param str
     *           The string to print.<p>
     * TODO(gmadrid): figure out what this does and where it is used.
     * 				  This function is not in the spec that I can find.
     */
    public void print(String str) {
    	MaybeLogUsage("print");
    }

    /**
     * Sets the link value for the current text position in the window.
     * If {@code val} is non-zero, all text output from now until this
     * method is called with a different value makes up a single hyperlink.
     * <p>
     * Applies to: text 
     * 
     * @param val
     *           The link value.<p>
     */
    public void setLinkValue(int val) {
    	MaybeLogUsage("setLinkStyle");	
    }
    
    /**
     * Draws an image in the window.
     * <p>
     * Applies to: text buffer
     * 
     * @param bres
     *           A {@link BlorbResource}.<p>
     * @param alignment
     *           One of the constants in {@link GlkImageAlign}.<p>
     * @return
     *           True if the image was drawn.
     */
    public boolean drawInlineImage(BlorbResource bres, int alignment) {
    	MaybeLogUsage("drawInlineImage");
    	return false;
    }

    /**
     * Draws a scaled image in the text buffer window.
     * <p>
     * Applies to: text buffer
     *  
     * @param bres
     *           A {@link BlorbResource}.<p>
     * @param alignment
     *           One of the constants in {@link GlkImageAlign}.<p>
     * @param width
     *           The display width of the image.<p>
     * @param height
     *           The display height of the image.<p>
     * @return
     *           True if the image was drawn.
     */
    public boolean drawInlineImage(BlorbResource bres, int alignment, int width,
            int height) {
    	MaybeLogUsage("drawInlineImage");
    	return false;
    }

    /**
     * Breaks the text in the window below a margin image.
     * <p>
     * Applies to: text buffer
     */
    public void flowBreak() {
    	MaybeLogUsage("flowBreak");
    }

    /**
     * Draws an image in the window.
     * <p>
     * Applies to: graphics
     * 
     * @param num
     *           The number of the image resource.<p>
     * @param x
     *           The x coordinate of the image.<p>
     * @param y
     *           The y coordinate of the image.<p>
     * @return
     *           True if the image was drawn.
     */
    public boolean drawImage(BlorbResource bres, int x, int y) {
    	MaybeLogUsage("drawImage");
    	return false;
    }

    /**
     * Draws an image in the window, scaled to a certain size. The x and
     * y coordinate may lie outside the window; width and height will
     * be non-zero and positive.
     * <p>
     * Applies to: graphics
     * 
     * @param bres
     *           A {@link BlorbResource}.<p>
     * @param x
     *           The x coordinate of the image.<p>
     * @param y
     *           The y coordinate of the image.<p>
     * @param width
     *           The display width of the image.<p>
     * @param height
     *           The display height of the image.<p>
     * @return
     *           True if the image was drawn.
     */
    public boolean drawImage(BlorbResource bres, int x, int y, int width,
            int height) {
    	MaybeLogUsage("drawImage");
    	return false;
    }

    /**
     * Sets the background color of the window. This does not take effect
     * until the next clear or redraw.
     * <p>
     * Applies to: graphics
     *  
     * @param color
     *           The background color.<p>
     */
    public void setBackgroundColor(int color) {
    	MaybeLogUsage("setBackgroundColor");
    }

    /**
     * Clears a rectangle with the window's background color. The
     * dimensions of the rectangle might not lie entirely within the
     * window; the width and height will be non-zero and positive.
     * <p>
     * Applies to: graphics 
     * 
     * @param left
     *           The leftmost x coordinate of the rectangle.<p>
     * @param top
     *           The topmost y coordinate of the rectangle.<p>
     * @param width
     *           The width of the rectangle.<p>
     * @param height
     *           The height of the rectangle.<p>
     */
    public void eraseRect(int left, int top, int width, int height) {
    	MaybeLogUsage("eraseRect");
    }

    /**
     * Fills a rectangle with a certain color.  The dimensions of the
     * rectangle might not lie entirely within the window; the width and
     * height will be non-zero and positive.
     * <p>
     * Applies to: graphics
     * 
     * @param color
     *           The fill color.<p>
     * @param left
     *           The leftmost x coordinate of the rectangle.<p>
     * @param top
     *           The topmost y coordinate of the rectangle.<p>
     * @param width
     *           The width of the rectangle.<p>
     * @param height
     *           The height of the rectangle.<p>
     */
    public void fillRect(int color, int left, int top, int width, int height) {
    	MaybeLogUsage("fillRect");
    }
    
    /**
     * Returns the window id of the window. This is the value of the {@code id}
     * parameter from the call to
     * {@link Glk#windowOpen(GlkWindow, int, int, int, int, GlkWindow[])}.
     * @return the window id of the window.
     */
    public int getId() { 
    	MaybeLogUsage("getId");
    	return 0; 
    }
    
    /**
     * Returns the pixel size of the given fixed constraint
     * @param constraint the requested size set by
     *     {@link Glk#windowOpen(GlkWindow, int, int, int, int, GlkWindow[])}
     * @param dir the split direction set by
     *     {@link Glk#windowOpen(GlkWindow, int, int, int, int, GlkWindow[])}
     * @return the pixel size of the constraint
     */
    public int getSizeFromConstraint(int constraint, boolean vertical, int maxSize) {
    	MaybeLogUsage("getSizeFromConstraint");
    	return 0;
    }
    
    /**
     * Changes the current output style of the window.
     * <p>
     * Applies to: text
     * 
     * @param val
     *           One of the constants in {@link GlkStyle}.<p>
     */
    public void setStyle(int val) {
    	MaybeLogUsage("setStyle");
    }
    
    // TODO(gmadrid-refactor): clean up this public access.
	public  View view;
	
	/**
	 * @return the view
	 */
	public View getView() {
		return view;
	}


}
