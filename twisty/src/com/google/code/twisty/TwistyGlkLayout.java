package com.google.code.twisty;


import org.brickshadow.roboglk.GlkWinType;
import org.brickshadow.roboglk.window.GlkLayout;
import org.brickshadow.roboglk.window.RoboTextBufferWindow;
import org.brickshadow.roboglk.window.TextBufferView;

import android.app.Activity;


public class TwistyGlkLayout extends GlkLayout {
	
	public TwistyGlkLayout(Activity activity) {
		super(activity);
	}

	@Override
	protected Group createGroup(int wintype, int id) {
		// TODO: change when other window types added
		if (wintype != GlkWinType.TextBuffer) {
			return null;
		}
		
		TextBufferView view = new TextBufferView(this.getContext());
		RoboTextBufferWindow win = new RoboTextBufferWindow(
				activity, queue, new TwistyTextBufferIO(view), id);
		return new Group(win, view);
	}

}
