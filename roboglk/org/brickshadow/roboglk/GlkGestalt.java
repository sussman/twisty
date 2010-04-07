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
 * Values for the {@code sel} argument of
 * {@link Glk#gestalt(int, int, int[])}.
 */
public interface GlkGestalt {
    
    /**
     * Asks if a certain character can be used for single-character
     * input.
     * <ul>
     * <li>{@code val}: either a Unicode code point or one of the
     *     {@link GlkKeycode} constants.</li>
     * <li>{@code arr}: can be ignored.</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     * <b>Note:</b> A Glk library is required to support the ASCII
     * characters 32-126, and GlkJNI will return 1 ({@code true})
     * without forwarding to this method.
     */
    int CharInput = 1;
    
    /**
     * Asks if a certain character can be used for line input.
     * <ul>
     * <li>{@code val}: a Unicode code point (it will never be one of
     *     the characters 0-31 or 127-159)</li>
     * <li>{@code arr}: can be ignored.</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul> 
     */
    int LineInput = 2;
    
    /**
     * Asks if a character can be displayed.
     * <ul>
     * <li>{@code val}: a Unicode code point (it will never be one of
     *     the characters 0-9, 11-31, or 127-159)</li>
     * <li>{@code arr}: if arr is not {@code null}, set {@code arr[0]}
     *     to the number of glyphs required to display the character</li>
     * <li>return: one of the {@link GlkCharOutput} constants</li>
     * </ul>
     */
    int CharOutput = 3;
    
    /**
     * Asks if mouse/touch input is supported in a type of window.
     * <ul>
     * <li>{@code val}: one of {@link GlkWinType#Graphics},
     *     {@link GlkWinType#TextBuffer} or {@link GlkWinType#TextGrid</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int MouseInput = 4;
    
    /**
     * Asks if timer events are supported.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int Timer = 5;
    
    /**
     * Asks if graphics windows can be created.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int Graphics = 6;
    
    /**
     * Asks if images can be drawn in a type of window.
     * <ul>
     * <li>{@code val}: one of {@link GlkWinType#Graphics} or
     *     {@link GlkWinType#TextBuffer}</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     * <b>Note:</> returning 1 implies support for both JPEG and PNG
     * images.
     */
    int DrawImage = 7;
    
    /**
     * Asks if sounds can be played.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     * <b>Note:</b> returning 1 implies support for at least AIFF sound
     * resources; support for MOD sound resources is tested by
     * {@link GlkGestalt#SoundMusic}.
     */
    int Sound = 8;
    
    /** 
     * Asks if the volume of sounds can be changed.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int SoundVolume = 9;
    
    /**
     * Asks if sound notification events are supported.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     * See {@link GlkSChannel} for details.
     */
    int SoundNotify = 10;
    
    /**
     * Asks if hyperlinks are supported in general.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int Hyperlinks = 11;
    
    /**
     * Asks if hyperlinks are supported in a type of window.
     * <ul>
     * <li>{@code val}: one of {@link GlkWinType#TextGrid} or
     *     {@link GlkWinType#TextBuffer}</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int HyperlinkInput = 12;
    
    /**
     * Asks if "music" (MOD sound resources) can be played.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int SoundMusic = 13;
    
    /**
     * Asks if transparent PNG images are supported.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int GraphicsTransparency = 14;
    
    /**
     * Asks if Unicode input and output is supported.
     * <ul>
     * <li>{@code val}: can be ignored</li>
     * <li>{@code arr}: can be ignored</li>
     * <li>return: 1 for {@code true}, 0 for {@code false}</li>
     * </ul>
     */
    int Unicode = 15;
}
