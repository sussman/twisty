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

public class Color {
	public Color(int argb) {
		this.argb = argb;
	}
	
	public Color(int red, int green, int blue) {
		this(android.graphics.Color.argb(255, red, green, blue));
	}

	public int getARGB() {
		return argb;
	}
	
	public static final Color BLACK   = new Color(android.graphics.Color.BLACK);
	public static final Color RED     = new Color(android.graphics.Color.RED);
	public static final Color GREEN   = new Color(android.graphics.Color.GREEN);
	public static final Color BLUE    = new Color(android.graphics.Color.BLUE);
	public static final Color CYAN    = new Color(android.graphics.Color.CYAN);
	public static final Color MAGENTA = new Color(android.graphics.Color.MAGENTA);
	public static final Color YELLOW  = new Color(android.graphics.Color.YELLOW);
	public static final Color WHITE   = new Color(android.graphics.Color.WHITE);
	public static final Color GRAY    = new Color(android.graphics.Color.GRAY);
	public static final Color DARK_GRAY = new Color(android.graphics.Color.DKGRAY);
	
	private final int argb;

	public Color brighter() {
		int alpha = android.graphics.Color.alpha(argb);
		int red = android.graphics.Color.red(argb);
		int green = android.graphics.Color.green(argb);
		int blue = android.graphics.Color.blue(argb);
		red *= 2;
		if (red > 255)
			red = 255;
		if (green > 255)
			green = 255;
		if (blue > 255)
			blue = 255;
		return new Color(android.graphics.Color.argb(alpha, red, green, blue));
	}
}
