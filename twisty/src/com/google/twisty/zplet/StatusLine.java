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

package com.google.twisty.zplet;

import com.google.twisty.R;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class StatusLine {
	public StatusLine(Activity owner) {
		left = new StatusLabel(owner, R.id.statusL);
		right = new StatusLabel(owner, R.id.statusR);
	}
	
	public Label getLeft() {
		return left;
	}
	
	public Label getRight() {
		return right;
	}
	
	private final StatusLabel left;
	private final StatusLabel right;
	
	public class ViewNotFoundException extends RuntimeException {
		private static final long serialVersionUID = -3749596598377367432L;
		ViewNotFoundException(int id) {
			super();
		}
	}
	
	private class StatusLabel implements Label {
		public StatusLabel(Activity owner, int id) {
	        View v = owner.findViewById(id);
	        if (v instanceof TextView) {
	        	view = (TextView)v;
	        } else {
	        	throw new ViewNotFoundException(id);
	        }
	        handler = new Handler();
		}
		public void setText(final String value) {
			handler.post(new Runnable() {
				public void run() {
		            view.setText(value);				
				}
			});
		}
		
		private final Handler handler;
		private final TextView view;
	}
}
