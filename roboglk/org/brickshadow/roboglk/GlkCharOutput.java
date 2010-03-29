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
 * Return values for {@link Glk#gestalt(int, int, int[])} when
 * the {@code sel} argument is {code GlkGestalt.CharOutput}.
 * <p>
 * It it probably safe to return {@code ExactPrint} for all printable
 * characters.
 */
public interface GlkCharOutput {
    /**
     * Indicates that a character cannot be printed.
     */
    int CannotPrint = 0;
    
    /**
     * Indicates that an approximation of a character will be printed.
     * For example, an accented character may be printed without the
     * accent.
     */
    int ApproxPrint = 1;
    
    /**
     * Indicates that a character can be printed exactly.
     */
    int ExactPrint = 2;
}
