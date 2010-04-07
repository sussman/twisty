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

/**
 * Style hint constants. See {@link Glk#setStyleHint(int, int, int, int)}.
 */
public interface GlkStyleHint {
    
    /**
     * How much to indent lines of text in the given style.
     * <p>
     * The associated value may be negative to shift text left or
     * positive to shift text right. Its units are not defined, but
     * +1 can be assumed to mean the smallest visible amount of
     * indentation.
     */
    int Indentation = 0;
    
    /**
     * How much to indent the first line of each paragraph.
     * <p>
     * The associated value is as specified in {@link #Indentation}.
     */
    int ParaIndentation = 1;
    
    /**
     * How to justify the text.
     * <p>
     * The associated value must be one of the {@link GlkJustification}
     * constants.
     */
    int Justification = 2;
    
    
    /**
     * How much to increase or decrease the font size from the default
     * size.
     * <p>
     * The associated value is not an absolute number of points/pixels,
     * but a relative factor.
     */
    int Size = 3;
    
    /**
     * The weight for text.
     * <p>
     * The associated value will be 1 for bold, 0 for normal, -1 for light.
     */
    int Weight = 4;
    
    /**
     * The angle for text.
     * <p>
     * The associated value will be 1 for italic/slanted, 0 for normal.
     */
    int Oblique = 5;
    
    /**
     * The width of characters.
     * <p>
     * The associated value will be 1 for proportional width, 0 for
     * fixed width.
     */
    int Proportional = 6;
    
    /**
     * The foreground color of the text.
     * <p>
     * The associated value is a 24-bit big-endian RGB value.
     */
    int TextColor = 7;
    
    /**
     * The background color of the text.
     * <p>
     * The associated value is a 24-bit big-endian RGB value.
     */
    int BackColor = 8;
    
    /**
     * How to interpret foreground and background colors for text.
     * <p>
     * The associated value will be 1 to indicate that the colors should
     * be swapped, 0 for normal printing.
     */
    int ReverseColor = 9;
}
