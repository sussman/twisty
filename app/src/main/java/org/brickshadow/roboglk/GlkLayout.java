package org.brickshadow.roboglk;


import org.brickshadow.roboglk.io.StyleManager;
import org.brickshadow.roboglk.io.TextBufferIO;
import org.brickshadow.roboglk.util.GlkEventQueue;
import org.brickshadow.roboglk.util.UISync;
import org.brickshadow.roboglk.view.TextBufferView;

import android.app.Activity;
import android.view.View;
import android.widget.AbsoluteLayout;


@SuppressWarnings("deprecation")
public class GlkLayout extends AbsoluteLayout {

	protected class Group implements WindowNode {
		public final GlkWindow win;
		public final View view;
		
		private PairWin parentNode;
		
		public Group(GlkWindow win, View view) {
			this.win = win;
			this.view = view;
		}
		
		@Override
		public LayoutRect getLayoutRect() {
			LayoutRect myLayout = new LayoutRect();
			LayoutParams params = (LayoutParams) view.getLayoutParams();
			myLayout.l = params.x;
			myLayout.t = params.y;
			int width = params.width;
			if (width == LayoutParams.FILL_PARENT) {
				width = ((View) getParent()).getWidth();
			}
			int height = params.height;
			if (height == LayoutParams.FILL_PARENT) {
				height = ((View) getParent()).getHeight();
			}
			myLayout.r = params.x + width;
			myLayout.b = params.y + height;
			return myLayout;
		}
		
		@Override
		public void setLayoutRect(int l, int t, int r, int b) {
			int width = r - l;
			int height = b - t;
			if (width == 0 || height == 0) {
				view.setVisibility(GONE);
			} else {
				if (view.getVisibility() == GONE) {
					view.setVisibility(VISIBLE);
				}
			}
			view.setLayoutParams(
					new AbsoluteLayout.LayoutParams(width, height, l, t));
		}
		
		@Override
		public WindowNode getNodeForWin(GlkWindow win) {
			if (win == this.win) {
				return this;
			} else {
				return null;
			}
		}
		
		@Override
		public PairWin getParentNode() {
			return parentNode;
		}
		
		@Override
		public void setParentNode(PairWin newParent) {
			parentNode = newParent;
		}
		
		@Override
		public void close(GlkLayout glkLayout) {
			if (parentNode != null) {
				parentNode.removeChild(this);
			}

			view.setVisibility(GONE);
			glkLayout.removeViewInLayout(view);
		}
	}
	
	protected final Activity activity;
	protected GlkEventQueue queue;
	
	protected StyleManager.Style[] bufferStyles;
	protected StyleManager.Style[] gridStyles;
	
	public GlkLayout(Activity activity) {
		super(activity);
		this.activity = activity;
		setFocusableInTouchMode(true);
		uiWait = UISync.getInstance();
	}
	
	public void initialize(GlkEventQueue queue) {
		this.queue = queue;
		bufferStyles = StyleManager.newDefaultStyles();
		gridStyles = StyleManager.newDefaultStyles();
		if (root != null) {
			root.close(this);
		}
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 0, 0);
	}

	WindowNode root;
	private volatile Group newGroup;
	private volatile UISync uiWait;
	
	public static int[] recorder = new int[100];
	public static int recordPos = 0;
	
	public final GlkWindow[] addGlkWindow(final GlkWindow splitwin,
			final int method, final int size, final int wintype,
			final int id) {
		
		uiWait.waitFor(new Runnable() {
			public void run() {
				newGroup = makeNewGroup(splitwin, method, size, wintype, id);
				recorder[recordPos++] = newGroup.win.getId();
				uiWait.stopWaiting(null);
			}
		});

		if (newGroup == null) {
			return new GlkWindow[] { null, null };
		}

		return new GlkWindow[] { newGroup.win, newGroup.parentNode };
	}
	
	private Group makeNewGroup(GlkWindow splitwin, int method,
			int size, final int wintype, final int id) {

		Group group = createGroup(wintype, id);
		if (group != null) {
			if (splitwin != null) {
				WindowNode splitNode = root.getNodeForWin(splitwin);
				PairWin newPair = new PairWin(
						activity, this, method, size,
						splitNode.getParentNode(), group, splitNode,
						id + 1, group.win);
				LayoutRect rect = splitNode.getLayoutRect();
				newPair.setLayoutRect(rect.l, rect.t, rect.r, rect.b);
				if (splitNode == root) {
					root = newPair;
				}
			} else {
				root = group;
			}
			addView(group.view);
		}
		return group;
	}
	
	public final void removeGlkWindow(final GlkWindow win) {
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
				recorder[recordPos++] = 100 + win.getId();
			}
		});
	}
	
	protected Group createGroup(int wintype, int id) {
		switch (wintype) {
		case GlkWinType.TextBuffer:
			TextBufferView tbview = new TextBufferView(getContext());
			//tbview.requestFocus();
			GlkTextBufferWindow tbwin = new GlkTextBufferWindow(
					activity, queue,
					new TextBufferIO(tbview, new StyleManager(bufferStyles)),
					id);
			return new Group(tbwin, tbview);
		default:
			// TODO: change when other window types added
			return null;
		}
	}

	public void setStyleHint(int wintype, int styl, int hint, int val) {
		switch (wintype) {
		case GlkWinType.AllTypes:
			setStyleHint(bufferStyles, styl, hint, val);
			setStyleHint(gridStyles, styl, hint, val);
			break;
		case GlkWinType.TextBuffer:
			setStyleHint(bufferStyles, styl, hint, val);
			break;
		case GlkWinType.TextGrid:
			setStyleHint(gridStyles, styl, hint, val);
			break;
		default:
			throw new IllegalArgumentException();
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

class LayoutRect {
	public int l;
	public int t;
	public int r;
	public int b;
}

interface WindowNode {
	void setLayoutRect(int l, int t, int r, int b);
	LayoutRect getLayoutRect();
	WindowNode getNodeForWin(GlkWindow win);
	PairWin getParentNode();
	void setParentNode(PairWin newParent);
	void close(GlkLayout glkLayout);
}

class PairWin extends GlkPairWindow implements WindowNode {

	private PairWin parentNode;
	WindowNode firstChild, secondChild;
	
	private GlkWindow keyWin;
	
	// The node that contains the key, and the node that doesn't.
	private WindowNode keyNode, otherNode;
	
	private int splitDir, splitDivision;
	
	private int size;
	
	private final int id;

	private LayoutRect myLayout;
	
	private final Activity activity;
	private final GlkLayout glkLayout;
	
	PairWin(Activity activity, GlkLayout glkLayout, int method,
			int size, PairWin parentNode,
			WindowNode newNode, WindowNode splitNode,
			int id, GlkWindow keyWin) {
		
		this.activity = activity;
		this.glkLayout = glkLayout;
		this.keyWin = keyWin;
		this.size = size;
		this.id = id;
		splitDir = GlkWinMethod.dir(method);
		splitDivision = GlkWinMethod.division(method);
		this.parentNode = parentNode;
		newNode.setParentNode(this);
		splitNode.setParentNode(this);
		firstChild = newNode;
		keyNode = newNode;
		secondChild = splitNode;
		otherNode = splitNode;
		myLayout = new LayoutRect();
		
		if (parentNode != null) {
			parentNode.replaceChild(splitNode, this);
		}
	}
	
	@Override
	public int getSizeFromConstraint(int constraint, boolean vertical, int maxSize) {
		GlkLayout.Group keyGroup =
			(GlkLayout.Group) keyNode.getNodeForWin(keyWin);

		if (keyGroup != null) {
			return keyWin.getSizeFromConstraint(constraint, vertical, maxSize);
		} else {
			return 0;
		}
	}

	// NOTE: this method doesn't and shouldn't mess with keyNode/otherNode
	void replaceChild(WindowNode oldNode, WindowNode newNode) {
		if (firstChild == oldNode) {
			firstChild = newNode;
		} else {
			secondChild = newNode;
		}
		newNode.setParentNode(this);
	}
	
	@Override
	public LayoutRect getLayoutRect() {
		return myLayout;
	}
	
	@Override
	public void setLayoutRect(int l, int t, int r, int b) {
		// The key window node is always a group (non-pair) node. 
		GlkLayout.Group keyGroup =
			(GlkLayout.Group) keyNode.getNodeForWin(keyWin);
		
		myLayout.l = l;
		myLayout.t = t;
		myLayout.r = r;
		myLayout.b = b;
		
		int keySize = 0;
		
		if (keyGroup != null) {
			int maxSize = 0;
			boolean vertical = false;
			switch (splitDir) {
			case GlkWinMethod.Above:
			case GlkWinMethod.Below:
				vertical = true;
				maxSize = b - t;
				break;
			case GlkWinMethod.Left:
			case GlkWinMethod.Right:
				vertical = false;
				maxSize = r - l;
				break;
			}
			if (splitDivision == GlkWinMethod.Fixed) {
				keySize =
					keyWin.getSizeFromConstraint(size, vertical, maxSize);
			} else {
				keySize = (int) ((size * maxSize) / 100.0);
			}
		} else {
			keyWin = null;
		}
		
		if (keySize == 0) {
			/* NOTE: technically we should only set one dimension
			 * to zero, based on the split method.
			 */
			keyNode.setLayoutRect(l, t, l, t);
			otherNode.setLayoutRect(l, t, r, b);
		} else {
			switch (splitDir) {
			case GlkWinMethod.Above:
				int keyBottom = t + keySize;
				keyNode.setLayoutRect(l, t, r, keyBottom);
				otherNode.setLayoutRect(l, keyBottom + 1, r, b);
				break;
			case GlkWinMethod.Below:
				int keyTop = b - keySize;
				otherNode.setLayoutRect(l, t, r, keyTop - 1);
				keyNode.setLayoutRect(l, keyTop, r, b);
				break;
			case GlkWinMethod.Left:
				int keyRight = l + keySize;
				keyNode.setLayoutRect(l, t, keyRight, b);
				otherNode.setLayoutRect(keyRight + 1, t, r, b);
				break;
			case GlkWinMethod.Right:
				int keyLeft = r - keySize;
				otherNode.setLayoutRect(l, t, keyLeft - 1, b);
				keyNode.setLayoutRect(keyLeft, t, r, b);
				break;
			}
		}
	}
	
	void removeChild(WindowNode child) {
		WindowNode preserveNode = null;
		if (child == firstChild) {
			preserveNode = secondChild;
			firstChild = null;
		} else {
			preserveNode = firstChild;
			secondChild = null;
		}
		if (parentNode != null) {
			parentNode.replaceChild(this, preserveNode);
		}
		/* When parentNode == null (this node is root), we handle
		 * replacement in GlkLayout#removeGlkWindow.
		 */
	}
	
	@Override
	public void close(GlkLayout glkLayout) {
		if (parentNode != null) {
			parentNode.removeChild(this);
		}
		
		firstChild.close(glkLayout);
		secondChild.close(glkLayout);
		
		keyWin = null;
		firstChild = null;
		secondChild = null;
		keyNode = null;
		otherNode = null;
	}
	
	@Override
	public WindowNode getNodeForWin(GlkWindow win) {
		WindowNode node = firstChild.getNodeForWin(win);
		if (node == null) {
			node = secondChild.getNodeForWin(win);
		}
		return node;
	}
	
	@Override
	public PairWin getParentNode() {
		return parentNode;
	}

	@Override
	public void setParentNode(PairWin newParent) {
		parentNode = newParent;
	}
	
	/**
	 * Returns 0. A pair window's id should never be accessed anyway.
	 */
	@Override
	public int getId() {
		return 0;
	}
	
	@Override
	public void setArrangement(final int method, final int size,
			final GlkWindow key) {
	
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (keyWin != key) {
					keyWin = key;
					if (firstChild.getNodeForWin(key) != null) {
						keyNode = firstChild;
						otherNode = secondChild;
					} else {
						keyNode = secondChild;
						otherNode = firstChild;
					}
				}
				
				splitDir = GlkWinMethod.dir(method);
				splitDivision = GlkWinMethod.division(method);
				PairWin.this.size = size;
				
				LayoutRect rect = getLayoutRect();
				setLayoutRect(rect.l, rect.t, rect.r, rect.b);
				glkLayout.requestLayout();
				glkLayout.invalidate();
			}
		});
	}
	
}
