//   Copyright 2007 Google Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.google.twisty;

import java.util.Map;

import org.zmpp.swingui.ColorTranslator;
import org.zmpp.swingui.DisplaySettings;
import org.zmpp.swingui.GameThread;
import org.zmpp.swingui.LineEditor;
import org.zmpp.swingui.LineEditorImpl;
import org.zmpp.swingui.TextViewport;
import org.zmpp.swingui.Viewport;
import org.zmpp.swingui.Viewport6;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ScreenModel;

import com.google.twisty.zplet.ZMachineException;
import com.google.twisty.zplet.ZScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class TwistyView extends View {
	private static String TAG = "TwistyView";

	private Handler handler;
	private static TwistyView last_created;
	private Machine machine;
	private ScreenModel screen;
	private Viewport view;
	private LineEditor editor;
	private DisplaySettings settings;
	private GameThread game_thread;

	@SuppressWarnings("unchecked")
	public TwistyView(Context context, AttributeSet attrs, Map inflateParams) {
		super(context, attrs, inflateParams);
		machine = null;
		handler = new Handler();
		settings = getSettings();
		last_created = this;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// TODO(marius) draw stuff
//		Bitmap bitmap = screen.getBitmap();
//		canvas.drawBitmap(bitmap, 0, 0, null);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.i(TAG, "size change: now " + w + " x " + h);
		// TODO resize
/*		if (screen != null) {
			screen.reshape(getLeft(), getTop(), getWidth(), getHeight());
		} */
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	public void startMachine() {
		game_thread = new GameThread(machine, screen);
		game_thread.start();
	}

	public void invalidateSoon() {
		handler.post(new Runnable() {
			public void run() {
				invalidate();
			}
		});
	}

	public void invalidateSoon(final int l, final int t, final int w,
			final int h) {
		handler.post(new Runnable() {
			public void run() {
				invalidate(l, t, w, h);
			}
		});
	}
	
	public static TwistyView getLastCreated() {
		return last_created;
	}

	public static DisplaySettings getSettings() {
	    int stdfontsize = 10;
	    int fixedfontsize = 10;
	    int foreground = ColorTranslator.UNDEFINED;
	    int background = ColorTranslator.UNDEFINED;
	    boolean antialias = true;
	    
	    return new DisplaySettings(stdfontsize, fixedfontsize, background,
	                               foreground, antialias);		
	}
	
	/**
	 * Called from the zmachine's thread just before it exits
	 * @param machineException
	 */
	public void onZmFinished(final ZMachineException e) {
		handler.post(new Runnable() {
			public void run() {
				Twisty activity = (Twisty)getContext();
				activity.onZmFinished(e);
			}
		});
	}

	public void setupMachine(Machine machine) {
		this.machine = machine;
	    editor = new LineEditorImpl(machine.getGameData().getStoryFileHeader(),
	            machine.getGameData().getZsciiEncoding());

	}

	public void fixupUI() {
		if (machine.getGameData().getStoryFileHeader().getVersion() ==  6) {
			view = new Viewport6(machine, editor, settings);
			screen = (ScreenModel) view;
		} else {
			view = new TextViewport(machine, editor, settings);
			screen = (ScreenModel) view;
		}

	}
}
