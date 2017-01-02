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
 * File usage constants. The file type constants can be used to display
 * an appropriate prompt when requesting a file name from the player.
 */
public final class GlkFileUsage {
    
    /** A generic data file. */
    public static final int Data = 0x00;
    
    /** A saved game file. */
    public static final int SavedGame = 0x01;
    
    /** A game transcript file. */
    public static final int Transcript = 0x02;
    
    /** A command record file. */
    public static final int InputRecord = 0x03;
    
    private static final int TypeMask = 0x0f;
    
    /** A file that should be opened in text mode. */
    public static final int TextMode = 0x100;
    
    /** A file that should be opened in binary mode. */
    public static final int BinaryMode = 0x000;

    /**
     * Returns true if a usage mask indicates text mode.
     * 
     * @param usage
     *           A usage mask.<p>
     * @return
     *           True if the mask indicates text mode.
     */
    public static boolean isTextMode(int usage) {
        return ((usage & TextMode) != 0);
    }

    /**
     * Extracts the file type from a usage mask.
     * 
     * @param usage
     *           A usage mask.<p>
     * @return
     *           The file type.
     */
    public static int usageType(int usage) {
        int type = usage & TypeMask;
        if (type > InputRecord) {
            type = Data;
        }
        return type;
    }

    private GlkFileUsage() {}
}
