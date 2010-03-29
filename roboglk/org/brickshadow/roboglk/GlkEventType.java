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
 * The event types and their structure, as returned by
 * {@link Glk#select(int[])} or {@link Glk#poll(int[])}.
 * <p>
 * {@code event[0]} should be the appropriate event type value.
 */
public interface GlkEventType {
    
    /**
     * No event. This type of event should only be returned by
     * {@link Glk#poll(int[])} when there are no events available.
     * The {@code event} array should be filled with zeroes.
     */
    int None = 0;
    
    /**
     * A timer event.
     * <ul>
     * <li>{@code event[1]}: 0</li>
     * <li>{@code event[2]}: 0</li>
     * <li>{@code event[3]}: 0</li>
     * </ul>
     */
    int Timer = 1;
    
    /**
     * A single-key character event.
     * <ul>
     * <li>{@code event[1]}: the window id of the window that received
     *     the event</li>
     * <li>{@code event[2]}: the Unicode code point of the character</li>
     * <li>{@code event[3]}: 0</li>
     * </ul>
     */
    int CharInput = 2;
    
    /**
     * A line input event.
     * <ul>
     * <li>{@code event[1]}: the window id of the window that received
     *     the event</li>
     * <li>{@code event[2]}: the length of the input line</li>
     * <li>{@code event[3]}:</li>
     * </ul>
     */
    int LineInput = 3;
    
    /**
     * A mouse/touch input event.
     * <ul>
     * <li>{@code event[1]}: the window id of the window that received
     *     the event</li>
     * <li>{@code event[2]}: the x-coordinate of the event</li>
     * <li>{@code event[3]}: the y-coordinate of the event</li>
     * </ul>
     * For text grid windows, the coordinates are character positions; for
     * graphics windows, they are pixels. 
     */
    int MouseInput = 4;
    
    /**
     * A window arrangement event. An event of this type may need to be
     * sent when windows are resized, when the screen orientation changes,
     * or when a change in display font size would cause fixed-size
     * windows (those which are supposed to be sized in terms of lines of
     * text) to change their display size.
     * <ul>
     * <li>{@code event[1]}: 0 if all windows are affected, or the
     *     window id of the pair window that contains
     *     all the windows that are affected, or the window id of the
     *     only affected window</li>
     * <li>{@code event[2]}: 0</li>
     * <li>{@code event[3]}: 0</li>
     * </ul>
     * <b>Note:</b> events of this type should be sent <i>after</i> the
     * windows have been rearranged onscreen.
     */
    int Arrange = 5;
    
    /**
     * A graphics window redraw event. An event of this type would only
     * need to be sent if a graphics window was unable to redisplay its
     * content after being obscured.
     * <ul>
     * <li>{@code event[1]}: 0 if all windows are affected, or the
     *     window id of the pair window that contains
     *     all the windows that are affected, or the window id of the
     *     only affected window</li>
     * <li>{@code event[2]}: 0</li>
     * <li>{@code event[3]}: 0</li>
     * </ul>
     * <b>Note:</b> if an event of this type needs to be sent, the
     * background of any affected windows should be cleared before sending
     * the event.
     */
    int Redraw = 6;
    
    /**
     * A sound notification event. See {@link GlkSChannel}.
     * <ul>
     * <li>{@code event[1]}: 0</li>
     * <li>{@code event[2]}: the number of the stopped sound resource</li>
     * <li>{@code event[3]}: the {@code notify} argument passed to the
     *     corresponding call to
     *     {@link GlkSChannel#play(int, int, int)}.</li>
     * </ul>
     */
    int SoundNotify = 7;
    
    /**
     * A hyperlink event.
     * <ul>
     * <li>{@code event[1]}: the window id of the window that received
     *     the event</li>
     * <li>{@code event[2]}: the link value of the hyperlink (see
     *     {@link GlkWindow#setLinkValue(int)}).</li>
     * <li>{@code event[3]}: 0</li>
     * </ul>
     */
    int HyperLink = 8;
}
