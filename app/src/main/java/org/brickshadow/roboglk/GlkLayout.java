package org.brickshadow.roboglk;


import org.brickshadow.roboglk.io.StyleManager;
import org.brickshadow.roboglk.util.GlkEventQueue;
import org.brickshadow.roboglk.util.UISync;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;


/**
 * GlkLayout is a ViewGroup responsible for display of GlkWindows.
 * GlkLayout does a delicate dance coordinating glk_ callbacks from the "terp"
 * thread, the thread where the native interpreter is running, and the "ui"
 * thread, the thread that does all of the Android UI. 
 *  
 * @author gmadrid
 */
public class GlkLayout extends LinearLayout {
	private static final String TAG = "GlkLayout"; 
	
	/**
	 * Create a new GlkLayout view. It is not usable until you call initialize.
	 * <br>
	 * This <em>must</em> be called from the UI thread.
	 * @param context
	 */
	public GlkLayout(Context context) {
		super(context);
		setFocusableInTouchMode(true);
		uiSync = UISync.getInstance();
	}
	
	/**
	 * Initialize the GlkLayout by providing it with an event queue.
	 * You can only call this once.
	 * <br>
	 * Called from the UI thread.
	 * @param queue
	 */
	// TODO(gmadrid-refactor): Figure out if you can get rid of this and
	//   replace it with a setEventQueue call.
	public void initialize(GlkEventQueue queue) {
		this.queue = queue;
		
		// TODO(gmadrid-refactor): What are these for? Why can't they be initialized in the ctor?
		bufferStyles = StyleManager.newDefaultStyles();
		gridStyles = StyleManager.newDefaultStyles();
	}

	/**
	 * Entry point for glk_window_open.
	 * <br>
	 * This is called from the terp thread.
	 *** TODO(gmadrid-refactor): fix these docs.
	 * @param splitwin The window that we will be splitting.
	 * @param method Bitfield of GlkWinMethod values describing how the split
	 * 	             should be performed. 
	 * @param size Desired size of the <em>new</em> window. Interpretation of
	 * this values depends on the values of <em>method</em> and
	 * <em>wintype</em>.
	 * @param wintype GlkWinType value. 
	 * @param id the id passed from the jni layer with the id of the new window.
	 * @return an array of two new windows. The first is the new window. The
	 * second is the GlkPairWindow that is created to describe the new window's
	 * position in the window tree.
	 */
	public final GlkWindow[] addGlkWindow(
			final GlkWindow splitWin,
			final GlkWinDirection direction,
			final GlkWinDivision sizeMethod,
			final int size,
			final GlkWinType winType,
			final int id) {

		class MyRunnable implements Runnable {
			public GlkWindow[] newWins;
			public void run() {
				newWins = makeNewWindow(splitWin, direction, sizeMethod, size,
						winType, id);
				uiSync.stopWaiting(null);
			}
		}
		MyRunnable runner = new MyRunnable();
		uiSync.waitFor(runner);
		return runner.newWins;
	}
	
	
	private GlkWindow[] makeNewWindow(GlkWindow splitWin,
			GlkWinDirection direction, GlkWinDivision sizeMethod,
			int size, GlkWinType winType, final int id) {
		if (splitWin == null) {
			return makeNewRootWindow(winType, id);
		}
		
		// TODO(gmadrid-refactor): fix this when you move to multi windows.
		Log.d(TAG, "No child windows yet. SKIPPING.");
		return new GlkWindow[] { null, null };
		//return splitWin.makeNewChildWindow(direction, sizeMethod,
			//	size, winType, id);
	}
	
	private GlkWindow[] makeNewRootWindow(GlkWinType winType, int id) {
		if (rootWindow != null) {
			Log.e(TAG, "Creating a root window while another one exists.");
		}
		
		GlkWindow newWindow = winType.newWindow(this, id);
		
		rootWindow = newWindow;
		addView(newWindow.getView());
		
		return new GlkWindow[] { newWindow, null };
	}
	
	
	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		// We should always have exactly one child, and it should fill the 
		// entire view.
		return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}
	
	public GlkEventQueue getQueue() { return queue; }
	
	private GlkWindow rootWindow;
	
	// TODO(gmadrid-refactor): figure out what this is used for
	protected GlkEventQueue queue;
	
	// Used to synchronize tasks between other threads and the ui thread.
	private volatile UISync uiSync;
	
	// TODO(gmadrid-refactor): remove this
	/* ==========================  THE LINE  ===============================*/

	// TODO(gmadrid-refactor): You need to figure out what these style things are used for.
	protected StyleManager.Style[] bufferStyles;
	protected StyleManager.Style[] gridStyles;
	
	
	public final void removeGlkWindow(final GlkWindow win) {
		if (win == rootWindow) {
			Log.d(TAG, "We're removing the root window.");
			uiSync.waitFor(new Runnable() {
				@Override
				public void run() {
					removeAllViews();
					rootWindow = null;
					uiSync.stopWaiting(null);
				}
			});
			return;
		}
		Log.e(TAG, "WE SHOULD BE REMOVING SOME WINDOWS");
		/*
		 * TODO(gmadrid-refactor): make this work.
		activity.runOnUiThread(new Runnable() {
			public void run() {
				WindowNode node = root.getNodeForWin(win);
				node.close(GlkLayout.this);
				if (node == root) {
					root = null;
				} else {
					PairWin oldParent = node.getParentNode();
					LayoutRect rect = root.getLayoutRect();
					if (oldParent == root) {
						if (oldParent.firstChild == null) {
							root = oldParent.secondChild;
						} else {
							root = oldParent.firstChild;
						}
					}
					root.setLayoutRect(rect.l, rect.t, rect.r, rect.b);
				}
				requestLayout();
				invalidate();
			}
		});*/
	}
	
	public void setStyleHint(GlkWinType winType, int styl, int hint, int val) {
		if (winType == GlkWinType.textBuffer) setStyleHint(bufferStyles, styl, hint, val);
		else if (winType == GlkWinType.textGrid) setStyleHint(gridStyles, styl, hint, val);
		else {
			setStyleHint(bufferStyles, styl, hint, val);
			setStyleHint(gridStyles, styl, hint, val);
		}
	}
	
	private void setStyleHint(StyleManager.Style[] styles, int style,
			int hint, int val) {

		if (style >= StyleManager.NUM_STYLES || style == GlkStyle.Normal) {
			return;
		}
		if (style == GlkStyle.Preformatted
				&& hint == GlkStyleHint.Proportional) {
			return;
		}
		styles[style].setHint(hint, val);
	}
}
