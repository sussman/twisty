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

import com.google.twisty.zplet.ZMachineException;

import russotto.zplet.screenmodel.ZScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class TwistyView extends View {
	private static String TAG = "TwistyView";

	private ZScreen screen;
	private Handler handler;
	private static TwistyView last_created;

	@SuppressWarnings("unchecked")
	public TwistyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		screen = null;
		handler = new Handler();
		last_created = this;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Bitmap bitmap = screen.getBitmap();
		canvas.drawBitmap(bitmap, 0, 0, null);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.i(TAG, "size change: now " + w + " x " + h);
		if (screen != null) {
			screen.reshape(getLeft(), getTop(), getWidth(), getHeight());
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	synchronized public void setScreen(ZScreen newscreen) {
		screen = newscreen;
		if (screen != null) {
			screen.reshape(getLeft(), getTop(), getWidth(), getHeight());
		}
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
}
