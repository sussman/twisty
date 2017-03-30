package org.brickshadow.roboglk;


import org.brickshadow.roboglk.io.StyleManager;
import org.brickshadow.roboglk.io.TextBufferIO;
import org.brickshadow.roboglk.io.TextGridIO;
import org.brickshadow.roboglk.util.GlkEventQueue;
import org.brickshadow.roboglk.util.UISync;
import org.brickshadow.roboglk.view.TextBufferView;
import org.brickshadow.roboglk.view.TextGridView;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.google.code.twisty.R;

import java.util.HashMap;

public class GlkLayout extends ViewGroup {
    private WindowNode root;
    private HashMap<GlkWindow, WindowNode> windows;
    private volatile UISync uiWait;
    private volatile Window tempWindow;
    private GlkEventQueue queue;
    private Activity activity;

    private StyleManager.Style[] bufferStyles;
    private StyleManager.Style[] gridStyles;

    public GlkLayout(Activity activity) {
        super(activity);
        this.activity = activity;
        this.root = null;
        this.uiWait = UISync.getInstance();
        this.windows = new HashMap<>();

        // Enables onDraw override
        this.setWillNotDraw(false);
    }

    public void initialize(GlkEventQueue queue) {
        this.queue = queue;
        this.bufferStyles = StyleManager.newDefaultStyles();
        this.gridStyles = StyleManager.newDefaultStyles();
        if (root != null) {
            root.close();
            root = null;
        }
    }

    public GlkWindow[] addGlkWindow(final GlkWindow splitwin, final int method, final int size, final int wintype, final int id) {
        uiWait.waitFor(new Runnable() {
            public void run() {
                tempWindow = addGlkWindowInternal(splitwin, method, size, wintype, id);
                uiWait.stopWaiting(null);
            }
        });

        if (tempWindow == null)
            return new GlkWindow[] { null, null };

        return new GlkWindow[] { tempWindow.window, tempWindow.parent };
    }

    private Window addGlkWindowInternal(GlkWindow splitwin, int method, int size, final int wintype, final int id) {
        // Not specifying a split window when windows exist is not allowed
        if (splitwin == null && root != null)
            return null;

        Window newWindow = createWindow(wintype, id);
        if (newWindow == null)
            return null;

        if (splitwin == null) {
            root = newWindow;
        }
        else {
            WindowNode splitNode = windows.get(splitwin);
            if (splitNode == root) {
                PairWindow newPairWindow = createPairWindow(splitNode, newWindow, method, size);
                root = newPairWindow;
            }
            else {
                //	Replace the specified window with a new pair window containing the old and new window
                PairWindow oldParent = splitNode.getParent();
                PairWindow newPairWindow = createPairWindow(splitNode, newWindow, method, size);
                oldParent.replaceWindow(splitNode, newPairWindow);
            }
        }

        return newWindow;
    }

    private Window createWindow(int wintype, int id) {
        Window newWindow = null;
        switch (wintype) {
            case GlkWinType.TextBuffer:
                TextBufferView tbview = new TextBufferView(getContext());
                GlkTextBufferWindow tbwin = new GlkTextBufferWindow(
                        activity, queue,
                        new TextBufferIO(tbview, new StyleManager(bufferStyles)),
                        id);
                newWindow = new Window(tbwin, tbview);
                addView(tbview);
                windows.put(tbwin, newWindow);
                break;
            case GlkWinType.TextGrid:
                TextGridView tgview = new TextGridView(getContext());
                TextGridIO tgio = new TextGridIO(tgview, new StyleManager(gridStyles));

                // TextIO classes currently have a strong dependency on their corresponding TextView
                // classes. When the layout manager decides TextGridView needs to resize, TextGridIO
                // needs to update the contents of the grid by padding or removing values as defined
                // in the Glk spec. Hence this circular dependency.
                tgview.setIO(tgio);

                GlkTextGridWindow tgwin = new GlkTextGridWindow(activity, queue, tgio, id);
                newWindow = new Window(tgwin, tgview);
                addView(tgview);
                windows.put(tgwin, newWindow);
                break;
            default:
                // TODO: change when other window types added
                break;
        }

        return newWindow;
    }

    private PairWindow createPairWindow(WindowNode splitNode, Window newWindow, int method, int size) {
        PairWindow newPairWindow = new PairWindow(splitNode, newWindow, method, size);
        windows.put(newPairWindow, newPairWindow);
        return newPairWindow;
    }

    private void removeWindow(Window window) {
        windows.remove(window.getGlkWindow());
        removeView(window.getView());
    }

    private void removeWindow(PairWindow window) {
        windows.remove(window);
    }

    public final void removeGlkWindow(final GlkWindow win) {
        uiWait.waitFor(new Runnable() {
            public void run() {
                WindowNode window = windows.get(win);
                PairWindow parent = window.getParent();
                window.close();

                if (window == root) {
                    root = null;
                }
                else {
                    // Replace the parent with its remaining child
                    windows.remove(parent);
                    parent.removeKeyWindow();
                    WindowNode otherWindow = parent.getOther(window);

                    if (parent == root)
                        root = otherWindow;
                    else
                        parent.getParent().replaceWindow(parent, otherWindow);
                }

                uiWait.stopWaiting(null);
            }
        });
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


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (root != null) {
            root.setLayoutParams(new GlkLayout.LayoutParams(widthSize, heightSize, 0, 0));
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                GlkLayout.LayoutParams lp =	(GlkLayout.LayoutParams) child.getLayoutParams();

                int childLeft = lp.x;
                int childTop = lp.y;
                child.layout(childLeft, childTop,
                        childLeft + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (root != null) {
            root.drawDividers(canvas);
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof GlkLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new GlkLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new GlkLayout.LayoutParams(p);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        // GlkLayout doesn't scroll, so return false
        return false;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int x;
        public int y;

        public LayoutParams(int width, int height, int x, int y) {
            super(width, height);
            this.x = x;
            this.y = y;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(GlkLayout.LayoutParams source) {
            super(source);
            this.x = source.x;
            this.y = source.y;
        }
    }

    private interface WindowNode {
        PairWindow getParent();
        void setParent(PairWindow parent);
        void setLayoutParams(GlkLayout.LayoutParams params);
        void close();
        void drawDividers(Canvas canvas);
    }

    private class Window implements WindowNode {
        private GlkWindow window;
        private View view;
        private PairWindow parent;
        // If we're a pair window's key window, keyParent refers to that pair window
        private PairWindow keyParent;

        Window(GlkWindow window, View view) {
            this.window = window;
            this.view = view;
            this.parent = null;
            this.keyParent = null;
        }

        GlkWindow getGlkWindow() {
            return window;
        }

        View getView() {
            return view;
        }

        @Override
        public PairWindow getParent() {
            return this.parent;
        }

        @Override
        public void setParent(PairWindow parent) {
            this.parent = parent;
        }

        public void setKeyParent(PairWindow keyParent) {
            this.keyParent = keyParent;
        }

        @Override
        public void setLayoutParams(GlkLayout.LayoutParams params) {
            view.measure(MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
            view.setLayoutParams(params);
        }

        @Override
        public void close() {
            GlkLayout.this.removeWindow(this);

            if(keyParent != null)
                keyParent.removeKeyWindow();
        }

        @Override
        public void drawDividers(Canvas canvas) {
            // A single window has no divider
        }
    }

    private class PairWindow extends GlkPairWindow implements WindowNode {
        private WindowNode firstWindow;
        private WindowNode secondWindow;
        private Window keyWindow;
        private PairWindow parent;
        private int method;
        private int size;
        private Drawable divider;
        private final int DIVIDER_SIZE;

        PairWindow(WindowNode splitWindow, Window keyWindow, int method, int size) {
            this.firstWindow = splitWindow;
            this.secondWindow = keyWindow;
            this.keyWindow = keyWindow;
            this.keyWindow.setKeyParent(this);

            this.firstWindow.setParent(this);
            this.secondWindow.setParent(this);

            this.parent = null;

            this.method = method;
            this.size = size;

            // What to draw on the area separating both windows
            this.divider = getResources().getDrawable(R.drawable.divider);
            this.DIVIDER_SIZE = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        }

        void replaceWindow(WindowNode oldWindow, WindowNode newWindow) {
            if (firstWindow == oldWindow) {
                firstWindow = newWindow;
                firstWindow.setParent(this);
            }
            else if (secondWindow == oldWindow) {
                secondWindow = newWindow;
                secondWindow.setParent(this);
            }
        }

        WindowNode getOther(WindowNode node) {
            if(node == firstWindow)
                return secondWindow;
            else if(node == secondWindow)
                return firstWindow;
            else
                return null;
        }

        void removeKeyWindow() {
            if (keyWindow != null) {
                keyWindow.setKeyParent(null);
                keyWindow = null;
            }
        }

        void setKeyWindow(Window keyWindow) {
            this.keyWindow = keyWindow;
            if (keyWindow != null)
                keyWindow.setKeyParent(this);
        }

        @Override
        public PairWindow getParent() {
            return this.parent;
        }

        @Override
        public void setParent(PairWindow parent) {
            this.parent = parent;
        }

        @Override
        public void setArrangement(final int method, final int size, final GlkWindow key) {
            uiWait.waitFor(new Runnable() {
                public void run() {
                    PairWindow.this.method = method;
                    PairWindow.this.size = size;
                    PairWindow.this.removeKeyWindow();

                    Window keyWindow = (Window) GlkLayout.this.windows.get(key);
                    PairWindow.this.setKeyWindow(keyWindow);

                    GlkLayout.this.requestLayout();

                    uiWait.stopWaiting(null);
                }
            });
        }

        @Override
        public int getId() {
            // This should never be called. Note that this isn't the window's rock by the way,
            // but a separate id generated by the C library. The rock is currently not passed
            // to the Java side.
            return 0;
        }

        @Override
        public int getSizeFromConstraint(int constraint, boolean vertical, int maxSize) {
            // This should never be called because a pair window can't be a key window
            return 0;
        }

        @Override
        public void setLayoutParams(GlkLayout.LayoutParams params) {
            if(firstWindow != null && secondWindow == null) {
                firstWindow.setLayoutParams(new GlkLayout.LayoutParams(params));
            }
            else if(firstWindow != null && secondWindow != null) {
                GlkLayout.LayoutParams firstParams = new GlkLayout.LayoutParams(params);
                GlkLayout.LayoutParams secondParams = new GlkLayout.LayoutParams(params);

                int keySize = getKeySize(params.width, params.height);

                if (keySize == 0) {
                    /* NOTE: technically we should only set one dimension
                     * to zero, based on the split method.
                     */
                    secondParams.height = 0;
                    secondParams.width = 0;
                }
                else {
                    int splitDir = GlkWinMethod.dir(this.method);
                    switch (splitDir) {
                        case GlkWinMethod.Above:
                            secondParams.height = keySize;

                            firstParams.y = params.y + keySize + DIVIDER_SIZE;
                            firstParams.height = params.height - (keySize + DIVIDER_SIZE);

                            divider.setBounds(params.x, firstParams.y - DIVIDER_SIZE, params.x + params.width, firstParams.y);
                            break;
                        case GlkWinMethod.Below:
                            firstParams.height = params.height - (keySize + DIVIDER_SIZE);

                            secondParams.y = (params.y + params.height) - keySize;
                            secondParams.height = keySize;

                            divider.setBounds(params.x, secondParams.y - DIVIDER_SIZE, params.x + params.width, secondParams.y);
                            break;
                        case GlkWinMethod.Left:
                            secondParams.width = keySize;

                            firstParams.x = params.x + keySize + DIVIDER_SIZE;
                            firstParams.width = params.width - (keySize + DIVIDER_SIZE);

                            divider.setBounds(firstParams.x - DIVIDER_SIZE, params.y, firstParams.x, params.y + params.height);
                            break;
                        case GlkWinMethod.Right:
                            firstParams.width = params.width - (keySize + DIVIDER_SIZE);

                            secondParams.x = (params.x + params.width) - keySize;
                            secondParams.width = keySize;

                            divider.setBounds(secondParams.x - DIVIDER_SIZE, params.y, secondParams.x, params.y + params.height);
                            break;
                    }
                }

                firstWindow.setLayoutParams(firstParams);
                secondWindow.setLayoutParams(secondParams);
            }
        }

        int getKeySize(int width, int height) {
            int keySize;
            int maxSize = 0;
            boolean vertical = false;
            int splitDir = GlkWinMethod.dir(this.method);
            switch (splitDir) {
                case GlkWinMethod.Above:
                case GlkWinMethod.Below:
                    vertical = true;
                    maxSize = height;
                    break;
                case GlkWinMethod.Left:
                case GlkWinMethod.Right:
                    vertical = false;
                    maxSize = width;
                    break;
            }

            int splitDivision = GlkWinMethod.division(this.method);
            if (splitDivision == GlkWinMethod.Fixed) {
                if (keyWindow != null)
                    keySize = keyWindow.getGlkWindow().getSizeFromConstraint(this.size, vertical, maxSize);
                else
                    keySize = 0;
            } else {
                keySize = (int) ((size * maxSize) / 100.0);
            }
            return keySize;
        }

        @Override
        public void close() {
            GlkLayout.this.removeWindow(this);

            firstWindow.close();
            secondWindow.close();
        }

        @Override
        public void drawDividers(Canvas canvas) {
            divider.draw(canvas);
            firstWindow.drawDividers(canvas);
            secondWindow.drawDividers(canvas);
        }
    }
}
