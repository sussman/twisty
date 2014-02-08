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

package org.brickshadow.roboglk.util;


import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.brickshadow.roboglk.Glk;
import org.brickshadow.roboglk.GlkEventType;
import org.brickshadow.roboglk.GlkSChannel;
import org.brickshadow.roboglk.GlkWindow;

import android.os.Message;


/**
 * An event queue which follows the requirements of the Glk spec, It
 * assumes that {@link #select()} and {@link #poll()} will only be
 * called from the interpreter thread, and that {@link #putEvent(Message)}
 * will only be called from the UI thread.
 * <p>
 * For efficiency, events are represented as {@code Message} objects;
 * if this is undesirable it can be easily changed, since clients
 * of this class should only be using the messages as opaque references.
 * <p>
 * See {@link GlkEventType}, {@link Glk#select(int[])} and
 * {@link Glk#poll(int[])} for more information.
 * <p>
 * This implementation coalesces multiple enqueued timer events into
 * a single event as per the Glk specification.
 */
public class GlkEventQueue {
    private LinkedList<Message> selectQueue = new LinkedList<Message>();
    private LinkedList<Message> pollQueue = new LinkedList<Message>();
    private final Timer timer = new Timer();
    private long timerMillisecs = 0;
    private volatile boolean hasTimerEvent = false;
    private UISync uiWait = UISync.getInstance();
    
    /**
     * Translates an event message into the form required by
     * {@link Glk#select(int[])} and {@link Glk#poll(int[])}.
     * 
     * @param msg an event message
     * @param event a four-element array to store the event details
     */
    public static void translateEvent(Message msg, int[] event) {
        if (msg == null) {
            event[0] = 0;
            event[1] = 0;
            event[2] = 0;
            event[3] = 0;
        } else {
            event[0] = msg.what;
            Object obj = msg.obj;
            if (obj == null) {
                event[1] = 0;
            } else {
                GlkWindow win = (GlkWindow) obj;
                event[1] = win.getId();
            }
            event[2] = msg.arg1;
            event[3] = msg.arg2;
        }
    }
    
    /**
     * Creates a new character-input event message.
     * 
     * @param win the window that receieved the input
     * @param c the Unicode code point of the character
     * @return a new character-input event message
     */
    public static Message newCharInputEvent(GlkWindow win, int c) {
        Message msg = Message.obtain();
        msg.what = GlkEventType.CharInput;
        msg.obj = win;
        msg.arg1 = c;
        msg.arg2 = 0;
        return msg;
    }
    
    /**
     * Creates a new line-input event message.
     * 
     * @param win the window that received the input
     * @param len the length of the input line
     * @return a new line-input event message
     */
    public static Message newLineInputEvent(GlkWindow win, int len) {
        Message msg = Message.obtain();
        msg.what = GlkEventType.LineInput;
        msg.obj = win;
        msg.arg1 = len;
        msg.arg2 = 0;
        return msg;
    }
    
    /**
     * Creates a new timer event message.
     * 
     * @return a new timer event message
     */
    public static Message newTimerEvent() {
        Message msg = Message.obtain();
        msg.what = GlkEventType.Timer;
        msg.obj = null;
        msg.arg1 = 0;
        msg.arg2 = 0;
        return msg;
    }
    
    /**
     * Creates a new window arrangement event message
     * 
     * @param win see {@link GlkEventType#Arrange}
     * @return a new window arrangement event message
     */
    public static Message newArrangeEvent(GlkWindow win) {
        Message msg = Message.obtain();
        msg.what = GlkEventType.Arrange;
        msg.obj = win;
        msg.arg1 = 0;
        msg.arg2 = 0;
        return msg;
    }
    
    /**
     * Creates a new graphics window redraw event message
     * 
     * @param win see {@link GlkEventType#Redraw}
     * @return a new graphics window redraw event message
     */
    public static Message newRedrawEvent(GlkWindow win) {
        Message msg = Message.obtain();
        msg.what = GlkEventType.Redraw;
        msg.obj = win;
        msg.arg1 = 0;
        msg.arg2 = 0;
        return msg;
    }
    
    /**
     * Creates a new mouse input event message.
     * 
     * @param win the window that received the input
     * @param x the x coordinate of the event
     * @param y the y coordinate of the event
     * @return a new mouse input event message
     */
    public static Message newMouseInputEvent(GlkWindow win, int x, int y) {
        Message msg = Message.obtain();
        msg.what = GlkEventType.MouseInput;
        msg.obj = win;
        msg.arg1 = x;
        msg.arg2 = y;
        return msg;
    }
    
    /**
     * Creates a new sound notification event message.
     * @param resNum the sound resource number
     * @param notifyVal the {@code notify} argument passed to the
     *                  corresponding call to
     *                  {@link GlkSChannel#play(int, int, int)};
     * @return a new sound notification event message
     */
    public static Message newSoundNotifyMessage(int resNum, int notifyVal) {
        Message msg = Message.obtain();
        msg.what = GlkEventType.SoundNotify;
        msg.obj = null;
        msg.arg1 = resNum;
        msg.arg2 = notifyVal;
        return msg;
    }
    
    public static Message newHyperlinkMessage(GlkWindow win, int linkval) {
        Message msg = Message.obtain();
        msg.what = GlkEventType.HyperLink;
        msg.obj = win;
        msg.arg1 = linkval;
        msg.arg2 = 0;
        return msg;
    }
    
    /**
     * Returns an event message if one is immediately available, or
     * {code null}. See {@link Glk#poll(int[])}.
     * 
     * @return an event message or {@code null}
     */
    public Message poll() {
    	synchronized(uiWait) {
    		Message msg = pollQueue.poll();
    		if (msg != null && msg.what == GlkEventType.Timer) {
    			hasTimerEvent = false;
    			scheduleNextTimer();
    		}
    		return msg;
    	}
    }
    
    /**
     * Blocks until an event message is available and returns it.
     * See {@link Glk#select(int[])}.
     * 
     * @return an event message 
     */
    public Message select() {
    	synchronized(uiWait) {
    		try {
    			while (selectQueue.isEmpty() && pollQueue.isEmpty()) {
    				//Log.w("SELECT", "before wait");
    				uiWait.wait();
    				//Log.w("SELECT", "after wait");
    			}

    			Message msg;
    			msg = selectQueue.poll();
    			if (msg == null) {
    				msg = pollQueue.poll();
    			}

    			if (msg != null && (msg.what == GlkEventType.Timer)) {
    				hasTimerEvent = false;
        			scheduleNextTimer();
    			}
            
    			return msg;
    		} catch (InterruptedException e) {
    			Thread.currentThread().interrupt();
    			return null;
    		}
    	}
    }
    
    /**
     * Adds an event message to the queue.
     * 
     * @param msg an event message.
     */
    public void putEvent(Message msg) {
    	synchronized(uiWait) {
    		boolean wasEmpty = selectQueue.isEmpty() && pollQueue.isEmpty();
        
    		switch (msg.what) {
    		case GlkEventType.Arrange:
    		case GlkEventType.Redraw:
    		case GlkEventType.SoundNotify:
    			pollQueue.add(msg);
    			break;
    		case GlkEventType.Timer:
    			if (!hasTimerEvent) {
    				pollQueue.add(msg);
    				hasTimerEvent = true;
    			} else {
    				return;
    			}
    			break;
    		default:
    			selectQueue.add(msg);
    			break;
    		}
        
    		if (wasEmpty) {
    			uiWait.notify();
    		}
    	}
    }
    
    public void cancelTimer() {
    	timer.cancel();
    	timerMillisecs = 0;
    }
    
    public void requestTimer(int millisecs) {
    	timerMillisecs = millisecs;
    	scheduleNextTimer();
    }
    
    private void scheduleNextTimer() {
    	if (timerMillisecs == 0) {
    		return;
    	}
    	timer.schedule(new TimerTask() {
			@Override
			public void run() {
				GlkEventQueue.this.putEvent(GlkEventQueue.newTimerEvent());
			}
		}, timerMillisecs);
    }
}
