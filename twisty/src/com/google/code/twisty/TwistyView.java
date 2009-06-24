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

package com.google.code.twisty;

import com.google.code.twisty.zplet.ZMachineException;
import com.google.code.twisty.TwistyInputConnection;

import android.content.Context;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;

public class TwistyView extends TextView {
	private static String TAG = "TwistyView";

	private final Twisty activity;
	private static TwistyView last_created;

	public TwistyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (context instanceof Twisty) {
			activity = (Twisty) context;
		} else {
			Log.e(TAG, "TwistyView running in non-Twisty context");
			activity = null;
		}
		last_created = this;
		// TODO: enable draggable scroll etc
	}

	@Override
	public boolean onCheckIsTextEditor() {
	  return true;
	}
	
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
	  return new TwistyInputConnection(this, false);
	}
	
	@Override
	protected boolean getDefaultEditable() {
		return true;
	}

	@Override
	protected MovementMethod getDefaultMovementMethod() {
		return ArrowKeyMovementMethod.getInstance();
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, BufferType.EDITABLE);
	}

	public static TwistyView getLastCreated() {
		return last_created;
	}

	/** Convenience wrapper around Activity.runOnUiThread() */
	public void runOnUiThread(Runnable action) {
		activity.runOnUiThread(action);
	}

	/**
	 * Called from the zmachine's thread just before it exits, for per-view
	 * cleanup
	 * @param e an exception that caused the exit, or null for normal exit
	 */
	public void onZmFinished(ZMachineException e) {
	}

	/**
	 * Called from the zmachine's thread just before it exits, for activity
	 * cleanup.
	 * @param e an exception that caused the exit, or null for normal exit
	 */
	public void tellOwnerZmFinished(final ZMachineException e) {
		runOnUiThread(new Runnable() {
			public void run() {
				activity.onZmFinished(e);
			}
		});
	}

	public void showMore(final boolean show) {
		runOnUiThread(new Runnable() {
			public void run() {
				activity.showMore(show);
			}
		});
	}
}
