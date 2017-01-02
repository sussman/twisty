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


import android.app.Activity;


public class UISync {
	private static UISync INSTANCE;
	
	public static void setInstance(Activity activity) {
		INSTANCE = new UISync(activity);
	}
	
	public static UISync getInstance() {
		return INSTANCE;
	}
	
	private final Activity activity;
	private volatile boolean isWaiting;
	
	private UISync(Activity activity) {
		this.activity = activity;
	}
	
	public void waitFor(Runnable r) {
		synchronized(this) {
			isWaiting = true;
		
			if (r != null) {
				activity.runOnUiThread(r);
			}
		
            try {        
                while (isWaiting) {
                    wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
	}
	
	public void stopWaiting(Runnable r) {
		synchronized(this) {
			boolean wasWaiting = isWaiting;
			if (r != null) {
				r.run();
			}
			isWaiting = false;
			if (wasWaiting) {
				notify();
			}
		}
	}
}
