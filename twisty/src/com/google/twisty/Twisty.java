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

package com.google.twisty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.zmpp.vm.Machine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.content.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.InputMethod;
import android.text.method.TextInputMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.twisty.zplet.Event;
import com.google.twisty.zplet.ZMachineException;
import com.google.twisty.zplet.ZScreen;
import com.google.twisty.zplet.ZWindow;

public class Twisty extends Activity {
	private static final int MENU_PICK_FILE = 101;
	private static final int MENU_STOP = 102;
	private static final int MENU_RESTART = 103;
	private static final int FILE_PICKED = 104;
	private static String TAG = "Twisty";
	private ZScreen screen;
	private Machine zm;
	// We use im and tb to allow for full text input, without any UI feedback
	private InputMethod im;
	private SpannableStringBuilder tb;
	private Handler handler;

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		handler = new Handler();
		setContentView(R.layout.twisty);
		im = TextInputMethod.getInstance(false,
                TextInputMethod.Capitalize.NONE);
        tb = new SpannableStringBuilder(" ");
        setViewVisibility(R.id.errors, View.GONE);

        View all = findViewById(R.id.all);
        all.setKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return onViewKey(v, keyCode, event);
            }
        });
        all.setFocusable(true);

        try {
            if (screen == null)
            	return;
            setupWelcomeMessage();
        } catch (Exception e) {
            fatal("Oops, an error occurred preparing to play");
            Log.e(TAG, "Failed to get prepare to play", e);
        }
    }
	
	private void setupWelcomeMessage() {
		// Display a temporary welcome message
		screen.reset();
		String msg = "Please wait...";
		int x = (screen.getchars() - msg.length()) / 2;
		if (x < 0)
			x = 0;
		int y = screen.getlines() / 2;
		screen.settext(y, x, msg.toCharArray(), 0, msg.length());
        // When the battery update comes in, we will print the
        // welcome message with the details
        monitorBatteryState();
	}

	private void printWelcomeMessage(final String details) {
		// Defer displaying the welcome message until all current
		// UI operations are complete (important at activity launch
		// time, when the view layout is incomplete).
		handler.post(new Runnable() {
			public void run() {
				if (zmIsRunning())
					return;
		        // TODO: Make this part be zcode and a little more interactive
		        // so a pedantic user could type "press menu key"
				screen.clear();
		        ZWindow w = new ZWindow(screen);
		        w.resize(screen.getchars(), screen.getlines());
		        w.bufferString("Twisty v0.03");
		        w.newline();
		        w.bufferString("Copyright (c) 2007 Google Inc.");
		        w.newline();
		        w.bufferString("Adapted from "
		        			 + "Zplet, a Z-Machine interpreter in Java, "
		        			 + "Copyright 1996, 2001 Matthew T. Russotto.");
		        w.newline();
		        w.newline();
		        // TODO: make this change depending on device features
		        w.bufferString("You are holding a modern-looking phone with a "
		        			 + "QWERTY keypad. "
		        			 + details
		        			 + "You feel an inexplicable urge to "
		        			 + "press the phone's \"menu\" key. ");
		        w.flush();
		        Log.i(TAG, "Welcome message printed; position = " 
		        		+ w.getx() + "," + w.gety());
			}
		});
	}
	
	private boolean zmIsRunning() {
        return (zm != null && zm.getCpu().isRunning());
	}

	private void monitorBatteryState() {
		IntentReceiver battReceiver = new IntentReceiver() {
			public void onReceiveIntent(Context context, Intent intent) {
				StringBuilder sb = new StringBuilder();
				
				context.unregisterReceiver(this);
				String sLevel = intent.getExtra("level", "").toString();
				String sScale = intent.getExtra("scale", "").toString();
				String state = intent.getExtra("state", "").toString();
				int level = -1;  // percentage, or -1 for unknown
				if (sLevel != null && sScale != null) {
					level = (Integer.parseInt(sLevel) * 100) /
							Integer.parseInt(sScale);
				}
	            sb.append("The phone");
				if ("Overheat".equals(state)) {
					sb.append("'s battery feels very hot!");
				} else if ("Unknown".equals(state)) {
					// old emulator; maybe also when plugged in with no battery
					sb.append(" has no battery.");
				} else if (level >= 100) {
					sb.append(" is fully charged.");
				} else if ("Discharging".equals(state)) {
					if (level == 0)
						sb.append(" needs charging right away.");
					else if (level > 0 && level <= 33)
						sb.append(" is about ready to be recharged.");
					else
						sb.append("'s battery discharges merrily.");
				} else {
					sb.append(" is " + level + "% charged.");
				}
				sb.append(' ');
				printWelcomeMessage(sb.toString());
			}
		};
		IntentFilter battFilter = new IntentFilter(Intent.BATTERY_CHANGED_ACTION);
		registerReceiver(battReceiver, battFilter);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
    		String data, Bundle extras) {
        switch(requestCode) {
        case FILE_PICKED:
            if (resultCode == RESULT_OK && data != null) {
                // File picker completed
                Log.i(TAG, "Opening user-picked file: " + data);
                startzm(data);
            }
            break;
        default:
            break;
        }
    }

    /** convenience helper to set text on a text view */
    void setItemText(final int id, final CharSequence text) {
        View v = findViewById(id);
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setText(text);
        }
    }

    /**
     * OnKeyListener for main TextView. Rather than duplicate key detection
     * fully, which would require knowledge of things like the Alt and Shift
     * states, we delegate this to a SpannableString and InputMethod. A bit
     * of a hack but it saves us from needing to care about keyboard layout
     * details.
     */
    public boolean onViewKey(View v, int keyCode, KeyEvent event) {
        if (screen != null) {
            if (tb.length() != 1)
                tb = new SpannableStringBuilder(" ");
            if (Selection.getSelectionEnd(tb) != 1 ||
            		Selection.getSelectionStart(tb) != 1)
                Selection.setSelection(tb, 1);
            switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                im.onKeyDown(v, tb, keyCode, event);
                break;
            case KeyEvent.ACTION_UP:
                im.onKeyUp(v, tb, keyCode, event);
                break;
            }
            switch (tb.length()) {
            case 0:  // delete
            	sendKeyEvent(Event.KEY_PRESS, '\b');
            	return true;
            case 2:  // insert one char
            	sendKeyEvent(Event.KEY_PRESS, tb.charAt(1));
            	return true;
            case 1:  // arrow, shift, click, etc
            	// Note that this requires our implementation of Event to use
            	// the same key code constants as android's KeyEvent
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                	sendKeyEvent(Event.KEY_ACTION, event.getKeyCode());
                break;
            }
        }
        return false;
    }
    
    void sendKeyEvent(int evt, int key_code) {
        Event e = new Event();
        e.id = evt;
        screen.keyDown(e, key_code);
    }

    /**
     * Start a zmachine, loading the program from the given file
     * @param filename Name of file to load
     */
    void startzm(String filename) {
        if (zmIsRunning())
            return;
        Log.i(TAG, "Loading file: " + filename);
        try {
            startzm(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            fatal("File not found: " + filename);
        }
    }

    /**
     * Start a zmachine, loading the program from the given resource
     * @param resource Identifier of resource to load
     */
    void startzm(int resource) {
        if (zmIsRunning())
            return;
        Log.i(TAG, "Loading resource: " + resource);
        Resources r = new Resources(getAssets(),
                new DisplayMetrics(),
                null);
        startzm(r.openRawResource(resource));
    }

    /** Convenience helper to set visibility of any view */
    void setViewVisibility(int id, int vis) {
        findViewById(id).setVisibility(vis);
    }

    /**
     * Start a zmachine, loading the program from the given stream
     * @param zstream Stream containing the program
     */
    void startzm(InputStream zstream) {
        setViewVisibility(R.id.errors, View.GONE);
        setViewVisibility(R.id.status, View.VISIBLE);
        View body = findViewById(R.id.body);
        TwistyView view = null;
        if (body instanceof TwistyView) {
        	view = (TwistyView)body;
	        TwistyMachineFactory factory;
	        factory = new TwistyMachineFactory(zstream, view);
	        
	        try {
	          
	          factory.buildMachine();
	          TwistyView frame = factory.getUI();      
	          frame.startMachine();
	        } catch (IOException ex) {
	          fatal("Could not read game.\nReason: " + Log.getStackTraceString(ex));
	        }
	    }
    }
    
    /** Displays a fatal error message */
    void fatal(String s) {
        setViewVisibility(R.id.status, View.VISIBLE);
        setItemText(R.id.statusL, "Error");
        setItemText(R.id.statusR, "");
        setViewVisibility(R.id.errors, View.VISIBLE);
        setItemText(R.id.errors, s);
    }

    /** Stops the currently running zmachine */
    public void stopzm() {
        if (zmIsRunning()) {
            zm.quit();
            // Some games need to be sent a key press or two before they will
            // actually quit.
            // TODO(mariusm): send one if the zmachine is not currently waiting
            // in screen.read_code() or similar; are a second and third really
            // ever needed?
            sendKeyEvent(Event.KEY_PRESS, '\n');
            sendKeyEvent(Event.KEY_PRESS, '\n');
            sendKeyEvent(Event.KEY_PRESS, '\n');
//            try {
//                zm.join();
//            } catch (InterruptedException e) {
//            }
        }
        zm = null;
        setViewVisibility(R.id.status, View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        if (!zmIsRunning()) {
            menu.add(0, R.raw.advent, "Adventure")
            .setShortcut(KeyEvent.KEYCODE_0, 0, KeyEvent.KEYCODE_A);
        	menu.add(0, R.raw.bronze, "Bronze")
            .setShortcut(KeyEvent.KEYCODE_1, 0, KeyEvent.KEYCODE_B);
        	menu.add(0, R.raw.curses, "Curses")
            .setShortcut(KeyEvent.KEYCODE_2, 0, KeyEvent.KEYCODE_C);
            menu.add(0, MENU_PICK_FILE, "Open file...")
            .setShortcut(KeyEvent.KEYCODE_5, 0, KeyEvent.KEYCODE_O);
        } else {
            menu.add(0, MENU_RESTART, "Restart")
            .setShortcut(KeyEvent.KEYCODE_1, 0, KeyEvent.KEYCODE_R);
            menu.add(0, MENU_STOP, "Stop")
            .setShortcut(KeyEvent.KEYCODE_9, 0, KeyEvent.KEYCODE_S);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(Menu.Item item)
    {
        switch (item.getId()) {
        case MENU_RESTART:
            zm.restart();
            // TODO(mariusm): only send this if the zmachine is not currently
            // waiting in screen.read_code() or similar.
            sendKeyEvent(Event.KEY_PRESS, '\n');
            break;
        case MENU_STOP:
            stopzm();
            // After the zmachine exits, the welcome message should show
            // again.
            break;
        case MENU_PICK_FILE:
            pickFile();
            break;
        default:
        	screen.clear();
        	startzm(item.getId());
        	break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Launch UI to pick a file to load and execute */
    private void pickFile() {
		// TODO(mariusm): allow for multiple unrelated directories
		// (typically we should allow the user to load games from both
    	// /sdcard and /data)
    	
    	// Until there's a system-provided file picker, we use our own
        Intent intent = new Intent(Intent.PICK_ACTION);
        intent.setClass(this, FileBrowser.class);
        intent.putExtra("file-filter", ".*\\.[Zz][358]");
        intent.putExtra("path-filter", "/sdcard(/.+)*");
        intent.putExtra("start-dir", "/sdcard");
        intent.putExtra("title", "Open game file (*.z3;*.z5;*.z8)");

        // Open the new activity
        startSubActivity(intent, FILE_PICKED);
        
        // When the new activity is done, onActivityResult will be called
    }

    /** Called from UI thread to request cleanup or whatever */
	public void onZmFinished(final ZMachineException e) {
		if (e != null) {
			// Report that an error occurred
			StringBuilder sb = new StringBuilder("Fatal error\n");
			if (e.getPc() >= 0) {
				sb.append("@ zmachine pc = 0x");
				sb.append(Integer.toHexString(e.getPc()));
				sb.append('\n');
			}
			sb.append(e.getMessage());
			if (e.getCause() != null) {
				StackTraceElement[] stack = e.getCause().getStackTrace();
				for (StackTraceElement frame: stack) {
					sb.append(frame.toString());
					sb.append('\n');
				}
			}
			// trim back trailing \n
			sb.deleteCharAt(sb.length() - 1);
			fatal(sb.toString());
		} else {
			// Normal ending, return to welcome screen
			setupWelcomeMessage();
		}
	}
}
