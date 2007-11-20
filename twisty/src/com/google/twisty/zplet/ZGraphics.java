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

/** Rather like java.awt.Graphics but restricted to what's needed by ZPlet,
 * with simplifications
 */
public interface ZGraphics {

	void fillRect(int l, int t, int w, int h, Color color);

	void setFont(Font textfont);

	void copyArea(int i, int j, int width, int k, int l, int m);

	void drawChars(char[] newtext, int offset, int length, int tx, int ty, Color color);

	FontMetrics getFontMetrics(Font font);

	void drawImage(BufferedImage imageBuffer, int i, int j,
			Object viewport);

	void drawString(String str, int x, int y, Color color);

	void drawLine(int x0, int y0, int x1, int y1, Color color);

	void setClip(int left, int top, int width, int height);

	void drawImage(BufferedImage image, int x, int y, int width, int height,
			ImageObserver observer);

}
