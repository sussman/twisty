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
 * Style constants for text output. It is entirely up to the Java frontend
 * to decide how to display text in each of these styles--style hints
 * requested by the program can be ignored--or whether to distinguish
 * between them at all.
 * <p>
 * Z-machine interpreters will probably only use a small subset of these
 * styles.
 */
public final class GlkStyle {
    /**
     * The style of normal or body text.
     */
    public static final int Normal = 0;
    
    /**
     * Text which is emphasized.
     */
    public static final int Emphasized = 1;
    
    /**
     * Text which has a particular arrangement of characters. It should
     * be displayed using a fixed-width font if possible.
     */
    public static final int Preformatted = 2;
    
    /**
     * Text which introduces a large section.
     */
    public static final int Header = 3;
    
    /**
     * Text which introduces a smaller section within a large section.
     */
    public static final int Subheader = 4;
    
    /**
     * Text which warns of a dangerous condition, or one which the player
     * should pay attention to.
     */
    public static final int Alert = 5;
    
    /**
     * Text which notifies of an interesting condition. 
     */
    public static final int Note = 6;
    
    /**
     * Text which forms a quotation or otherwise abstracted text. 
     */
    public static final int BlockQuote = 7;
    
    /**
     * Text which the player has entered.
     */
    public static final int Input = 8;
    
    /**
     * Text with no predefined meaning.
     */
    public static final int User1 = 9;
    
    /**
     * Text with no predefined meaning.
     */
    public static final int User2 = 10;
}
