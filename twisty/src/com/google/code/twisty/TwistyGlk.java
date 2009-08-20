// Copyright 2009 Google Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.google.code.twisty;

import java.io.File;

import org.brickshadow.roboglk.BlorbResource;
import org.brickshadow.roboglk.Glk;
import org.brickshadow.roboglk.GlkFileMode;
import org.brickshadow.roboglk.GlkLayout;
import org.brickshadow.roboglk.GlkSChannel;
import org.brickshadow.roboglk.GlkWinType;
import org.brickshadow.roboglk.GlkWindow;
import org.brickshadow.roboglk.util.GlkEventQueue;

import android.app.Activity;
import android.os.Message;
import android.os.Handler;


public class TwistyGlk implements Glk {

    private final GlkEventQueue eventQueue;
    
    private final Activity activity;
    private final Twisty myTwisty;
    private final Handler twistyHandler;
    
    private GlkWindow mainWin;
    private final GlkLayout glkLayout;
    
    public TwistyGlk(Activity activity, GlkLayout glkLayout, Handler msgHandler) {
        this.activity = activity;
        this.myTwisty = (Twisty) activity;
        this.twistyHandler = msgHandler;
        eventQueue = new GlkEventQueue();
        this.glkLayout = glkLayout;
        glkLayout.initialize(eventQueue);
    }
    
    @Override
    public void cancelTimer() {}

    @Override
    public void clearStyleHint(int wintype, int styl, int hint) {}

    @Override
    public GlkSChannel createChannel() {
        return null;
    }

    @Override
    public void destroyChannel(GlkSChannel schan) {}

    @Override
    public int gestalt(int sel, int val, int[] arr) {
        return 0;
    }

    @Override
    public boolean getImageInfo(BlorbResource bres, int[] dim) {
    	return false;
    }

    @Override
    public File namedFile(String filename, int usage) {
        return null;
    }

    @Override
    public void poll(int[] event) {
        Message msg = eventQueue.poll();
        GlkEventQueue.translateEvent(msg, event);
    }

    @Override
    public File promptFile(int usage, int fmode) {
    	// This "terp thread" isn't allowed to inflate dialogs directly;
    	// only the main Twisty UI thread can do that.  So we use the twistyHandler
    	// to send a TwistyMessage to Twisty, and let it do the prompting.
    	TwistyMessage msg = new TwistyMessage();
        msg.path = "";

        // Ask Twisty to prompt the user, then block until we're notify()'d.
        // (We're blocking on the glkLayout just because it's a sharde object.)
        if ((fmode & GlkFileMode.Read) == GlkFileMode.Read) {
        	 synchronized (glkLayout) {
             	try {
             		Message.obtain(twistyHandler, Twisty.PROMPT_FOR_READFILE, msg).sendToTarget();
             		glkLayout.wait();
             	}
             	catch (Exception e) {
             		return null; //  failure
             	}
             }
        }
        else if ((fmode & GlkFileMode.Write) == GlkFileMode.Write) {
       	 synchronized (glkLayout) {
            	try {
            		Message.obtain(twistyHandler, Twisty.PROMPT_FOR_WRITEFILE, msg).sendToTarget();
            		glkLayout.wait();
            	}
            	catch (Exception e) {
            		return null; //  failure
            	}
            }
        }
        else {
        	return null;
        }
        
        // Twisty should have modified our TwistyMessage object, and
        // then called notify() to wake us up.
        return new File(msg.path);
    }

    @Override
    public void requestTimer(int millisecs) {}

    @Override
    public void select(int[] event) {
        Message msg = eventQueue.select();
        GlkEventQueue.translateEvent(msg, event);
    }

    @Override
    public void setSoundLoadHint(BlorbResource bres, boolean flag) {}

    @Override
    public void setStyleHint(int wintype, int styl, int hint, int val) {
    	glkLayout.setStyleHint(wintype, styl, hint, val);
    }

    @Override
    public void windowClose(GlkWindow win) {
        glkLayout.removeGlkWindow(win);
    }
    
    @Override
    public void windowOpen(GlkWindow splitwin, int method, int size,
            int wintype, int id, GlkWindow[] wins) {
    	
    	if (splitwin != null || mainWin != null) {
    		return;
    	}
    	if (wintype != GlkWinType.TextBuffer) {
    		return;
    	}

        GlkWindow[] newWins =
        	glkLayout.addGlkWindow(splitwin, method, size, wintype, id);
        
        wins[0] = newWins[0];
        wins[1] = newWins[1];
    }

}
