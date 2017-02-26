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
 * A Glk window.
 * <p>
 * Not all types of windows will require each of these methods; GlkJNI
 * will not call a method on the wrong type of window. The various
 * other {@code GlkXXXWindow} classes are abstract convenience classes which
 * provide do-nothing implementations of inapplicable methods.
 */
public interface GlkWindow {

    /**
     * Clears the window.
     */
    void clear();

    /**
     * Returns the size of the window in the {@code dim[]} array.
     * <ul>
     * <li><b>{@code dim[0]}</b> - the window width</li>
     * <li><b>{@code dim[1]}</b> - the window height</li>
     * <p>
     * Applies to: text, graphics, pair
     * 
     * @param dim
     *           The width and height of the window.<p>
     */
    void getSize(int[] dim);

    /**
     * Requests a mouse event for the window. This will not be called
     * if there is already a reqeust for mouse input in the window.
     * <p>
     * Applies to: text, graphics
     */
    void requestMouseEvent();

    /**
     * Cancels mouse events for the window. This method may be called
     * when there is no pending request for a mouse event.
     * <p>
     * Applies to: text, graphics
     */
    void cancelMouseEvent();

    /**
     * Changes the current output style of the window.
     * <p>
     * Applies to: text
     * 
     * @param val
     *           One of the constants in {@link GlkStyle}.<p>
     */
    void setStyle(int val);

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
     */
    int measureStyle(int styl, int hint);

    /**
     * Checks if two styles are visually distinguishable in the
     * window.
     * <p>
     * Applies to: text
     * 
     * @param styl1
     *           One of the constants in {@link GlkStyle}.<p>
     * @param styl2
     *           One of the constants in {@link GlkStyle}.<p>
     * @return
     *           True if the styles are visually distingushable.
     */
    boolean distinguishStyles(int styl1, int styl2);

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
     */
    void print(String str);

    /**
     * Requests character input in the window. This method will not be
     * called when there is a pending request for character or line input.
     * <p>
     * Applies to: text
     * @param unicode
     *           True if Unicode input is requested, false for Latin-1.
     */
    void requestCharEvent(boolean unicode);

    /**
     * Cancels character input for the text window. This method may be
     * called when there is no pending request for character input.
     * <p>
     * Applies to: text
     */
    void cancelCharEvent();

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
     */
    void requestLineEvent(ByteBuffer buf, int maxlen,
            int initlen);

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
     */
    void requestLineEventUni(IntBuffer buf, int maxlen,
            int initlen);

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
    void setLinkValue(int val);

    /**
     * Requests hyperlink input in the text window. This method will not
     * be called when there is a pending request for hyperlink input.
     * <p>
     * Applies to: text
     */
    void requestLinkEvent();

    /**
     * Cancels hyperlink input for the text window. This method may be
     * called when there is no pending request for hyperlink input.
     * <p>
     * Applies to: text
     */
    void cancelLinkEvent();

    /**
     * Cancels line input for the text window. This method may be called
     * when there is no pending request for line input.
     * <p>
     * Applies to: text
     *  
     * @return
     *           The number of characters, if any, already entered
     *           during an active line input request.
     */
    int cancelLineEvent();

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
     */
    void moveCursor(int xpos, int ypos);

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
     */
    void setArrangement(int method, int size,
            GlkWindow key);

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
    boolean drawInlineImage(BlorbResource bres, int alignment);

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
    boolean drawInlineImage(BlorbResource bres, int alignment, int width,
            int height);

    /**
     * Breaks the text in the window below a margin image.
     * <p>
     * Applies to: text buffer
     */
    void flowBreak();

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
    boolean drawImage(BlorbResource bres, int x, int y);

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
    boolean drawImage(BlorbResource bres, int x, int y, int width,
            int height);

    /**
     * Sets the background color of the window. This does not take effect
     * until the next clear or redraw.
     * <p>
     * Applies to: graphics
     *  
     * @param color
     *           The background color.<p>
     */
    void setBackgroundColor(int color);

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
    void eraseRect(int left, int top, int width,
            int height);

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
    void fillRect(int color, int left, int top, int width,
            int height);
    
    /**
     * Returns the window id of the window. This is the value of the {@code id}
     * parameter from the call to
     * {@link Glk#windowOpen(GlkWindow, int, int, int, int, GlkWindow[])}.
     * @return the window id of the window.
     */
    int getId();
    
    /**
     * Returns the pixel size of the given fixed constraint
     * @param constraint the requested size set by
     *     {@link Glk#windowOpen(GlkWindow, int, int, int, int, GlkWindow[])}
     * @param dir the split direction set by
     *     {@link Glk#windowOpen(GlkWindow, int, int, int, int, GlkWindow[])}
     * @return the pixel size of the constraint
     */
    int getSizeFromConstraint(int constraint, boolean vertical, int maxSize);
}
