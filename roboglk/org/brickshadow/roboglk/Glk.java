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


import java.io.File;

import org.brickshadow.roboglk.util.GlkEventQueue;



/**
 * General bridge methods. See also {@link GlkWindow} and
 * {@link GlkSChannel}.
 */
public interface Glk {
    
    /**
     * Returns information about UI capabilities. See {@link GlkGestalt}
     * for descriptions of the meanings of {@code sel} and {@code val}.
     * 
     * @param sel one of the {@link GlkGestalt} constants representing
     *            a capability
     * @param val additional information for specifying a capability 
     * @param arr can be used to return supplemental information about
     *            certain capabilities
     * @return information about a UI capability. 
     */
    int gestalt(int sel, int val, int[] arr);
    
    /**
     * Waits until an event is available and returns it in the
     * {@code event} argument.
     * <p>
     * See {@link GlkEventType} for the details
     * of the different kinds of events; see {@link GlkEventQueue} for
     * an implementation of an event queue that follows the Glk spec.
     * <p>
     * When {@code GlkEventQueue} is used, this method could be implemented
     * as:
     * <pre>
     *   public void select(int[] event) {
     *       Message msg = eventQueue.select();
     *       GlkEventQueue.translateEvent(msg, event);
     *   }
     * </pre>
     * 
     * @param event the details of an event.
     */
    void select(int[] event);
    
    /**
     * Immediately returns an event of a certain type, or null if no
     * such events are available. The events that this function returns
     * must be of type {@link GlkEventType#Arrange},
     * {@link GlkEventType#Timer}, or {@link GlkEventType#SoundNotify}.
     * <p>
     * See {@link GlkEventType} for the details
     * of the different kinds of events; see {@link GlkEventQueue} for
     * an implementation of an event queue that follows the Glk spec.
     * <p>
     * When {@code GlkEventQueue} is used, this method could be implemented
     * as:
     * <pre>
     *   public void poll(int[] event) {
     *       Message msg = eventQueue.poll();
     *       GlkEventQueue.translateEvent(msg, event);
     *   }
     * </pre>
     * 
     * @param event the details of an event.
     */
    void poll(int[] event);
    
    /**
     * Returns a {@code File} representing the name that should be used,
     * or {@code null} to indicate failure.
     * 
     * @param filename
     *           The name requested by the program.<p>
     * @param usage
     *           The purpose of the file; it can be interpreted using the
     *           methods in {@link GlkFileUsage}.<p>
     * @return
     *           A {@code File} object representing the name to be used,
     *           or {@code null} to indicate failure.
     */
    File namedFile(String filename, int usage);

    /**
     * Forwarded from {@code glk_fileref_create_by_prompt}.
     * <p>
     * Requests a filename from the user.
     * 
     * @param usage
     *           The purpose of the file; it can be interpreted using the
     *           methods in {@link GlkFileUsage}.<p>
     * @param fmode
     *           One of the constants in {@link GlkFileMode}.<p>
     * @return
     *           A {@code File} representing the name entered by the user,
     *           or {@code null} to indicate failure.
     */
    File promptFile(int usage, int fmode);
    
    /**
     * Asks for timer events to be generated at a certain interval. This
     * may be called several times, with varying values for
     * {@code millisecs}, without an intervening call to
     * {@link #cancelTimer()}. 
     * 
     * @param millisecs the non-zero elapsed time between events.
     */
    void requestTimer(int millisecs);

    /**
     * Asks that timer events cease to be generated. This may be
     * called even when timer events are not currently being generated.
     */
    void cancelTimer();
    
    /**
     * Creates a new Glk window. See the Glk spec for more information
     * about window creation.
     * 
     * @param splitwin
     *           The {@code GlkWindow} object for the window that is being
     *           split, or {@code null}; any type of window is legal.<p>
     * @param method
     *           The requested direction and division for the split; it
     *           can be interpreted using the methods in
     *           {@link GlkWinMethod}.<p>
     * @param size
     *           The requested size for the new window. Its meaning
     *           depends on the values of {@code method} and
     *           {@code wintype}:
     *           <ul>
     *           <li>For {@code GlkWinMethod.Proportional}, the size is
     *               a percentage</li>
     *           <li>For {@code GlkWinMethod.Fixed}, the size is a value in
     *               the measurement system of the new window (lines of
     *               text for text windows, pixels for graphics windows). It
     *               may be greater than the split window's actual size.
     *           </ul>
    *           In any case, {@code size} will never be negative.<p>
    * @param wintype
    *           The type of the new window. It will be one of the constants
    *           {@code GlkWinType.TextBuffer}, {@code GlkWinType.TextGrid},
    *           {@code GlkWinType.Graphics}, or {@code GlkWinType.Blank}.<p>
    * @param id
    *           A unique id generated by GlkJNI for the new window.<p>
    * @param wins
    *           Used to return references to newly-created
    *           {@code GlkWindow}s back to GlkJNI. {@code win[0]} must
    *           refer to the new window or be {@code null} to indicate
    *           inability to open the window, and if necessary {@code win[1]}
    *           must refer to the newly-created pair window.<p>
    */
   void windowOpen(GlkWindow splitwin, int method, int size, int wintype,
           int id, GlkWindow[] wins);
   
   /**
    * Closes a window.
    * 
    * @param win
    *           The window to close.
    */
   void windowClose(GlkWindow win);
   
   /**
    * Requests that a style hint be set for windows of a certain type.
    * A Java frontend is free to handle this any way it likes, even by
    * ignoring hint requests.
    * 
    * @param wintype
    *           The type of the window. It will be one of the constants
    *           {@code GlkWinType.AllTypes}, {@code GlkWinType.TextGrid},
    *           or {@code GlkWinType.TextBuffer}.<p>
    * @param styl
    *           One of the constants in {@link GlkStyle}.<p>
    * @param hint
    *           One of the constants in {@link GlkStyleHint}.<p>
    * @param val
    *           A value appropriate for the {@code hint}.<p>
    */
   void setStyleHint(int wintype, int styl, int hint, int val);

   /**
    * Requests that a style hint be cleared for windows of a certain
    * type. A Java frontend is free to handle this any way it likes,
    * even by ignoring hint requests.
    * 
    * @param wintype
    *           The type of the window. It will be one of the constants
    *           {@code GlkWinType.AllTypes}, {@code GlkWinType.TextGrid},
    *           or {@code GlkWinType.TextBuffer}.<p>
    * @param styl
    *           One of the constants in {@link GlkStyle}.<p>
    * @param hint
    *           One of the constants in {@link GlkStyleHint}.<p>
    */
   void clearStyleHint(int wintype, int styl, int hint);
   
   /**
    * Creates a new {@link GlkSChannel}.
    * 
    * @return a new {@code GlkSChannel} or {@code null}.
    */
   GlkSChannel createChannel();

   /**
    * Destroys a sound channel. If a sound was playing on the channel,
    * it will be stopped without generating a sound notification event.
    * 
    * @param schan
    *           The channel to destroy.
    */
   void destroyChannel(GlkSChannel schan);
   
   /**
    * Requests that a sound be preloaded (if {@code flag} is
    * {@code true}) or unloaded. A Java frontend is free to ignore
    * this request.
    * 
    * @param bres
    *           A {@link BlorbResource} object
    * @param flag
    *           True to request preloading; false to request
    *           unloading.<p>
    */
   void setSoundLoadHint(BlorbResource bres, boolean flag);
   
   /**
    * Gets the size of an image.
    * 
    * @param bres
    *           A {@link BlorbResource} object
    * @return
    *           True if the image information could be determined.
    */
   boolean getImageInfo(BlorbResource bres, int[] dim);
}
