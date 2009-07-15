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

import com.google.code.twisty.zplet.ZMachineException;
import com.google.code.twisty.TwistyInputConnection;

import android.content.Context;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	  return false;
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
