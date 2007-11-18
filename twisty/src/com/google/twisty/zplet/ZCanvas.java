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

import com.google.twisty.TwistyView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Behaves just a tiny bit like java.awt.Canvas, but with
 * some modifications to simplify things.
 * TODO: This file is all very ugly and we should factor it away.
 */

public class ZCanvas {
	private static String TAG = "ZCanvas";
	
	private class ImageGraphics implements ZGraphics {

		private Bitmap bitmap;
		private Canvas canvas;
		private Paint paint;

		public ImageGraphics(Bitmap b) {
			bitmap = b;
			if (bitmap != null) {
				canvas = new Canvas(bitmap);
				paint = new Paint();
			}
		}

		/** Used for scrolling. Only ever seen with dx=0, dy<0. */
		public void copyArea(int x, int y, int width, int height, int dx, int dy) {
			// sanity check before copying
			if (x < 0) {
				Log.w(TAG, "copy area fixing up args: x=0, was " + x);
				width += x;
				x = 0;
			}
			if (y < 0) {
				Log.w(TAG, "copy area fixing up args: y=0, was " + y);
				height += y;
				y = 0;
			}
			if (x + width > bitmap.width()) {
				Log.w(TAG, "copy area fixing up args: width was " + width);
				width = bitmap.width() - x;
			}
			if (y + height > bitmap.height()) {
				Log.w(TAG, "copy area fixing up args: height was " + height);
				height = bitmap.height() - y;
			}
			// Copy to a temporary bitmap, then back again
            Bitmap copy = Bitmap.createBitmap(bitmap, x, y, width, height);
            canvas.drawBitmap(copy, x+dx, y+dy, null);
		}

		public void drawChars(char[] text, int index, int count, int x, int y, Color color) {
			paint.setColor(color.getARGB());
			canvas.drawText(text, index, count, x, y, paint);
		}

		public void fillRect(int l, int t, int w, int h, Color color) {
            canvas.save();  // save clipping
            canvas.clipRect(l, t, l+w, t+h);
            canvas.drawColor(color.getARGB());
            canvas.restore();  // restore clipping
		}

		public void setFont(Font font) {
			setupPaintFromFont(paint, font);
		}

	}

	class ImageGlue implements Image {
		private Bitmap bitmap;
		private ImageGraphics ig;

		public ImageGlue(int width, int height) {
			// TODO(mariusm): Maybe handle differently when created with 0 size
			if (width <= 0)
				width = 1;
			if (height <= 0)
				height = 1;
			
			if (width > 0 && height > 0) {
				bitmap = Bitmap.createBitmap(width, height, false);
				ig = new ImageGraphics(bitmap);
			}
		}

		public ZGraphics getGraphics() {
			return ig;
		}
		
		public Bitmap getBitmap() {
			return bitmap;
		}
	}

	public class AndroidFontMetrics implements FontMetrics {
		public AndroidFontMetrics(Font font) {
			paint = new Paint();
			setupPaintFromFont(paint, font);
			Paint.FontMetricsInt fmi = paint.getFontMetricsInt();
			descent = fmi.bottom;
			height = fmi.bottom - fmi.top;
		}

		public int charWidth(char c) {
			String text = new String(new char[] { c });
			return (int) paint.measureText(text);
		}

		public int stringWidth(char[] chars, int index, int count) {
			return (int) paint.measureText(chars, index, count);
		}

		public int getDescent() {
			return descent;
		}

		public int getHeight() {
			return height;
		}

		private final Paint paint;
		private final int descent;
		private final int height;
	}

	public ZCanvas(TwistyView v) {
		image = new ImageGlue(v.getWidth(), v.getHeight());
		view = v;
	}

	static void setupPaintFromFont(Paint paint, Font font) {
		int style = 0;
		if ((font.getStyle() & Font.BOLD) != 0)
			style |= Typeface.BOLD;
		if ((font.getStyle() & Font.ITALIC) != 0)
			style |= Typeface.ITALIC;
		paint.setTextSize(font.getSize());
		paint.setTypeface(Typeface.create(font.getName(), style));
		paint.setAntiAlias(true);
	}

	public Dimension size() {
		return new Dimension(view.getWidth(), view.getHeight());
	}
	
	public FontMetrics getFontMetrics(Font f) {
		return new AndroidFontMetrics(f);
	}
	
	private ImageGlue image;
	private TwistyView view;

	public ZGraphics getGraphics() {
		return image.getGraphics();
	}

	public void repaint(int left, int top, int width, int height) {
		view.invalidateSoon(left, top, left + width, top + height);
	}
	
	private Color background;
	
	public void setBackground(Color color) {
		background = color;
	}
	
	public Color getBackground() {
		return background;
	}

	private Color foreground;
	
	public void setForeground(Color color) {
		foreground = color;
	}

	public Color getForeground() {
		return foreground;
	}

	public void reshape(int x, int y, int width, int height) {
	}
	
	public Image createImage(int width, int height) {
		// WARNING! This only works if you only ever draw on
		// exactly one image at a time (which is the case for Zplet)
		Log.i(TAG, "Creating new image " + width + " x " + height);
		ImageGlue newimage = new ImageGlue(width, height);
		// Copy the old display onto it (and hopefully it will get
		// cleared or redrawn or something, depending on what's using it)
		Canvas newcanvas = new Canvas(newimage.getBitmap());
		newcanvas.drawBitmap(image.getBitmap(), 0, 0, null);
		image = newimage;
		return image;
	}
	
	public void repaint() {
		view.invalidateSoon();
	}
	
	public Bitmap getBitmap() {
		return image.getBitmap();
	}


	public void onZmFinished(ZMachineException machineException) {
		view.onZmFinished(machineException);
	}
}
