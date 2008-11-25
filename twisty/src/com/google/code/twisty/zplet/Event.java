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

package com.google.code.twisty.zplet;

import android.view.KeyEvent;

public class Event {
	public static final int UP = KeyEvent.KEYCODE_DPAD_UP;
	public static final int DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	public static final int LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	public static final int RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	public static final int F1 = 100033;
	public static final int F2 = 100034;
	public static final int F3 = 100035;
	public static final int F4 = 100036;
	public static final int F5 = 100037;
	public static final int F6 = 100038;
	public static final int F7 = 100039;
	public static final int F8 = 100040;
	public static final int F9 = 100041;
	public static final int F10 = 100042;
	public static final int F11 = 100043;
    public static final int F12 = 100044;
	public static final int KEY_PRESS = KeyEvent.ACTION_DOWN;
	public static final int KEY_ACTION = KeyEvent.ACTION_DOWN + 10;

	public int id;
}
