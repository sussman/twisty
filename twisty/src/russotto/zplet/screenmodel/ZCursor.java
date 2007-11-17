/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import com.google.twisty.zplet.Color;
import com.google.twisty.zplet.ZCanvas;
import com.google.twisty.zplet.ZGraphics;

 class ZCursor {
		Color cursorcolor, bgcolor;
		boolean shown;
		int t,l,w,h;
		ZCanvas parent;
		
		ZCursor(Color cursorcolor, Color bgcolor, ZCanvas parent) {
				shown = false;
				this.cursorcolor = cursorcolor;
				this.bgcolor = bgcolor;
				this.parent = parent;
		}

		ZCursor(ZCanvas parent) {
				this(Color.green, Color.yellow, parent);
		}

		ZCursor() {
				this(Color.green, Color.yellow, null);
		}

		synchronized void show() {
			if (!shown) {
				shown = true;
				if (parent != null) {
					redraw(parent.getGraphics());
					parent.repaint(l,t,w,h);
				}
			}
		}

		synchronized void hide() {
			if (shown) {
				shown = false;
				if (parent != null) {
					redraw(parent.getGraphics());
					parent.repaint(l, t, w, h);
				}
			}
		}

		synchronized void redraw(ZGraphics g) {
			if (g == null)
				return;
			if (shown) {
				g.fillRect(l, t, w, h, cursorcolor);
			} else {
				g.fillRect(l, t, w, h, bgcolor);			
			}
		}

		synchronized void move(int l, int t) {
			boolean wasshown = shown;

			if (wasshown)
					hide();
			this.l = l;
			this.t = t;
			if (wasshown)
					show();
		}

		synchronized void size(int w, int h) {
				boolean wasshown = shown;

				if (wasshown)
						hide();
				this.w = w;
				this.h = h;
				if (wasshown)
						show();
		}

//		synchronized void setGraphics(Graphics g) {
//				boolean wasshown = shown;
//
//				this.g = g;
//				g.setColor(cursorcolor);
//				g.setXORMode(bgcolor);
//				if (wasshown)
//						show();
//		}

		synchronized void setcolors(Color cursorcolor, Color bgcolor) {
				boolean wasshown = shown;

				if (wasshown)
					hide();
				this.cursorcolor = cursorcolor;
				this.bgcolor = bgcolor;
				if (wasshown)
					show();
		}
 }

