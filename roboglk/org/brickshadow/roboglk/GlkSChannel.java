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
 * A Glk sound channel
 */
public interface GlkSChannel {
    
    /**
     * Sets the volume for the channel. A newly created channel is at
     * full volume (0x10000). Setting the volume to zero is not supposed
     * to stop playback.
     * 
     * @param vol
     *           The volume.<p>
     */
    void setVolume(int vol);
    
    /**
     * Plays a sound on the channel. If a sound was already playing
     * on the channel, it must be stopped without notification.
     * <p>
     * A sound notification event will be generated, if requested, after
     * the last repetition has finished.
     * 
     * @param bres A {@link BlorbResource} object
     * @param repeats the number of times to repeat the sound; it will be
     *                either -1 (meaning repeat forever) or a non-zero
     *                positive value
     * @param notify if non-zero, requests notification when the sound
     *               stops playing
     * @return {@code true} if the sound started playing
     */
    boolean play(BlorbResource bres, int repeats, int notify);
    
    /**
     * Stops the current sound without notification. If there is no
     * current sound, does nothing.
     */
    void stop();
}
