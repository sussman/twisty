package com.google.twisty.zplet;

import android.graphics.Bitmap;

public interface ZScreen {

	void reset();

	int getchars();

	int getlines();

	void settext(int y, int x, char[] charArray, int i, int length);

	void clear();

	void reshape(int left, int top, int width, int height);

	Bitmap getBitmap();

	void keyDown(Event e, int key_code);

	void removeBufferedCodes();

}
