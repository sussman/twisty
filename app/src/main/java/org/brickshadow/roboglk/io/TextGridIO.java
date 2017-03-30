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
    // Cursor location. Valid values are [0, width - 1] for cx and [0, height - 1] for cy. If the
    // cursor is moved past the end of the grid, it will have the values cx=0 and cy=height.
    private int cx;
    private int cy;

    private int previousWidth;
    private int previousHeight;

    // Contains the grid's contents as well as newlines to break the lines for the TextGridView
    private StringBuilder text;

    public TextGridIO(TextGridView tv, StyleManager styleMan) {
        super(tv, styleMan);
        this.cx = 0;
        this.cy = 0;
        this.previousWidth = 0;
        this.previousHeight = 0;
        this.text = new StringBuilder();
    }

    public void moveCursor(int x, int y) {
        final int width = tv.getCharsPerLine();
        final int height = tv.getNumLines();

        if (y >= height) {
            // Cursor was positioned past the end of the grid
            this.cx = 0;
            this.cy = height;
        }
        else if(x >= width) {
            if (y <= height - 2) {
                // Cursor was positioned past the end of a line, move to the next line
                this.cx = 0;
                this.cy = y + 1;
            }
            else {
                // The next line is past the end of the grid
                this.cx = 0;
                this.cy = height;
            }
        }
        else {
            // The input was fine
            this.cx = x;
            this.cy = y;
        }
    }

    // Check if the window size has changed and if so update the contents. Called by onLayout in
    // TextGridView.
    public void refresh() {
        final int width = tv.getCharsPerLine();
        final int height = tv.getNumLines();

        if (width == previousWidth && height == previousHeight)
            return;

        StringBuilder newText = new StringBuilder();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (y >= previousHeight || x >= previousWidth)
                    newText.append(" ");
                else
                    newText.append(text.charAt(getIndex(x, y, previousWidth)));
            }
            newText.append("\n");
        }

        text = newText;
        tv.setText(text);

        previousWidth = width;
        previousHeight = height;
        cx = Math.min(cx, width - 1);
        cy = Math.min(cy, height - 1);
    }

    // Advances cursor and returns the corresponding index into this.text
    private int advanceCursor() {
        final int width = tv.getCharsPerLine();

        if (cx < width - 1)
            cx += 1;
        else {
            cx = 0;
            cy += 1;
        }

        return currentIndex();
    }

    // Advances cursor to the next line and returns the corresponding index into this.text
    private int advanceCursorToNextLine() {
        cx = 0;
        cy += 1;

        return currentIndex();
    }

    private int currentIndex() {
        final int width = tv.getCharsPerLine();
        return getIndex(cx, cy, width);
    }

    // The first index value past the last valid one. All indices greater than or equal to this
    // are invalid.
    private int endIndex() {
        final int width = tv.getCharsPerLine();
        final int height = tv.getNumLines();
        return getIndex(0, height, width);
    }

    private int getIndex(int x, int y, int width) {
        // Our internal representation of the text grid has newline characters added at the end
        // of each line. The '+ y' accounts for this. We never want a Glk call to be able to touch
        // the newlines.
        return x + (y * width) + y;
    }

    @Override
    public void doClear() {
        final int width = tv.getCharsPerLine();
        final int height = tv.getNumLines();

        StringBuilder newText = new StringBuilder();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                newText.append(" ");
            }
            newText.append("\n");
        }

        text = newText;
        tv.setText(text);

        cx = 0;
        cy = 0;
    }

    @Override
    public void doPrint(String str) {
        int index = currentIndex();

        // Don't print anything if the cursor position is outside the window
        if (index >= endIndex())
            return;

        // Print the text, but don't go outside the window
        for (int i = 0; i < str.length() && index < endIndex(); ++i) {
            char c = str.charAt(i);
            if(c == '\n') {
                // A newline in the Glk string causes the cursor to advance to the next line
                index = advanceCursorToNextLine();
            }
            else {
                text.setCharAt(index, c);
                index = advanceCursor();
            }
        }
        tv.setText(text);
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
