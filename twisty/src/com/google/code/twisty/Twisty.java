// Copyright 2009 Google Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.google.code.twisty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.code.twisty.TwistyMessage;
import com.google.code.twisty.zplet.Event;
import com.google.code.twisty.zplet.StatusLine;
import com.google.code.twisty.zplet.ZMachineException;
import com.google.code.twisty.zplet.ZViewOutput;


import russotto.zplet.screenmodel.ZScreen;
import russotto.zplet.screenmodel.ZStatus;
import russotto.zplet.screenmodel.ZWindow;
import russotto.zplet.zmachine.ZMachine;
import russotto.zplet.zmachine.state.ZState;
import russotto.zplet.zmachine.zmachine3.ZMachine3;
import russotto.zplet.zmachine.zmachine5.ZMachine5;
import russotto.zplet.zmachine.zmachine5.ZMachine8;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class Twisty extends Activity {
	private static final int MENU_PICK_FILE = 101;
	private static final int MENU_STOP = 102;
	private static final int MENU_RESTART = 103;
	private static final int FILE_PICKED = 104;
	private static final int DEBUG_ZM_DUMP = 105;
	private static final int DEBUG_PAUSE_RESUME = 106;
	private static final int MENU_SHOW_HELP = 107;
	
	private static String TAG = "Twisty";
	private static final String FIXED_FONT_NAME = "Courier";
	private static final String ROMAN_FONT_NAME = "Helvetica";
	private static final String RUNNING_FILE = "running_file";
	private static final String RUNNING_RESOURCE = "running_rsrc";
	private static final String FROZEN_GAME = "frozen_game";
	private static final int FONT_SIZE = 12;
	private String savegame_dir = "";
	private String savefile_path = "";

	// Dialog boxes we manage
	private static final int DIALOG_ENTER_SAVEFILE = 1;
	private static final int DIALOG_ENTER_RESTOREFILE = 2;
	private static final int DIALOG_CHOOSE_ZGAME = 3;
	private static final int DIALOG_CANT_SAVE = 4;
	private static final int DIALOG_NO_SDCARD = 5;

	// Messages we receive from external threads, via our Handler
	public static final int PROMPT_FOR_SAVEFILE = 1;
	public static final int PROMPT_FOR_RESTOREFILE = 2;
	public static final int PROMPT_FOR_ZGAME = 3;

	private ZScreen screen;
	private StatusLine status_line;
	private ZStatus status;
	private ZMachine zm;
	// We use listener and tb to allow for full text input without UI feedback
	private TextKeyListener listener;
	private SpannableStringBuilder tb;
	// Passed down to ZState, so ZMachine thread can send Messages back to this thread
	private Handler dialog_handler;
	private TwistyMessage dialog_message; // most recent Message received
	// Persistent dialogs created in onCreateDialog() and updated by onPrepareDialog()
	private Dialog restoredialog;
	private Dialog choosezgamedialog;
	// All z-games discovered when we last scanned the sdcard
	private String[] discovered_zgames;
	// A persistent map of button-ids to zgames found on the sdcard (absolute paths)
	private HashMap<Integer, String> zgame_paths = new HashMap<Integer, String>();
	private Object runningProgram;
	private Runnable preStartZM;

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog_handler = new Handler() { 
			public void handleMessage(Message m) {
				savegame_dir = "";
				savefile_path = "";
				if (m.what == PROMPT_FOR_SAVEFILE) {
					dialog_message = (TwistyMessage) m.obj;
					promptForSavefile();
				}
				else if (m.what == PROMPT_FOR_RESTOREFILE) {
					dialog_message = (TwistyMessage) m.obj;
					promptForRestorefile();
				}
				else if (m.what == PROMPT_FOR_ZGAME) {
					showDialog(DIALOG_CHOOSE_ZGAME);
				}
			} 
		};

		setContentView(R.layout.twisty);
		listener = TextKeyListener.getInstance(false, TextKeyListener.Capitalize.NONE);
		tb = new SpannableStringBuilder(" ");

		View all = findViewById(R.id.all);
		all.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return onViewKey(v, keyCode, event);
			}
		});
		all.setFocusable(true);

		try {
			InitZJApp();
			if (screen == null)
				return;
			if (!unfreezeZM(icicle)) {
				setupWelcomeMessage();
			}
		} catch (Exception e) {
			fatal("Oops, an error occurred preparing to play");
			Log.e(TAG, "Failed to get prepare to play", e);
		}
	}

	private void setupWelcomeMessage() {
		setViewVisibility(R.id.errors, View.GONE);
		setViewVisibility(R.id.more, View.GONE);
		setViewVisibility(R.id.status_v3, View.GONE);
		setViewVisibility(R.id.status_v5, View.GONE);
		printWelcomeMessage();
	}

	private void printWelcomeMessage() {
		if (zmIsRunning()) {
			Log.e(TAG, "Called printWelcomeMessage with ZM running");
			return;
		}
		
		// What version of Twisty is running?
		PackageInfo pkginfo = null;
		try {
			pkginfo = this.getPackageManager().getPackageInfo("com.google.code.twisty", 0);
		} catch (PackageManager.NameNotFoundException e) {
			fatal("Couldn't determine Twisty version.");
		}
			
		// TODO: Make this part be zcode and a little more interactive
		// so a pedantic user could type "press menu key"
		screen.clear();
		ZWindow w = new ZWindow(screen, 0);
		w.set_text_style(ZWindow.ROMAN);
		w.set_text_style(ZWindow.REVERSE);
		w.set_text_style(ZWindow.BOLD);
		w.bufferString("                                      ");
		w.newline();
		w.bufferString("  Twisty " + pkginfo.versionName 
				+ ", (C) Google Inc.         ");
		w.newline();
		w.bufferString("                                      ");
		w.newline();
		w.set_text_style(ZWindow.ROMAN);
		w.bufferString("Adapted from:");
		w.newline();
		w.bufferString("    Zplet, a Z-Machine interpreter in Java");
		w.newline();
		w.bufferString("    (C) Matthew T. Russotto.");
		w.newline();
		w.bufferString("This is open source software;");
		w.newline();
		w.bufferString("    see http://code.google.com/p/twisty");
		w.newline();
		w.newline();
		StringBuffer phoneBlurb = new StringBuffer();
		// TODO: make this change depending on device features
		phoneBlurb.append("You are holding a modern-looking phone with a "
				+ "QWERTY keypad. ");
		appendBatteryState(phoneBlurb);
		phoneBlurb.append("You feel an inexplicable urge to "
				+ "press the phone's \"menu\" key. ");
		w.bufferString(phoneBlurb.toString());
		w.flush();
	}
	
	private void printHelpMessage() {
		if (zmIsRunning()) {
			Log.e(TAG, "Called printHelpMessage with ZM running");
			return;
		}
		
		ZWindow w = new ZWindow(screen, 0);
		w.set_text_style(ZWindow.ROMAN);
		w.newline();
		w.newline();
		w.bufferString("-------------------------------------");
		w.newline();
		w.bufferString("Concepts stream into your mind:");
		w.newline();
		w.newline();
		w.bufferString("Interactive Fiction (IF) is its own genre of game: an artful crossing ");
		w.bufferString("of storytelling and puzzle-solving. ");
		w.bufferString("Read the Wikipedia entry on 'Interactive ");
		w.bufferString("Fiction' to learn more.");
		w.newline();
		w.newline();
		w.bufferString("You are the protagonist of the story. Your job is to explore the ");
		w.bufferString("environment, interact with people and things, solve puzzles, and move ");
		w.bufferString("the story forward.  The interpreter is limited to a small set of ");
		w.bufferString("vocabulary, typically of the form 'verb noun'.  To get started:");
		w.newline();
		w.newline();
		w.bufferString("  * north, east, up, down, enter...");
		w.newline();
		w.bufferString("  * look, look under rug, examine pen");
		w.newline();
		w.bufferString("  * take ball, drop hat, inventory");
		w.newline();
		w.bufferString("  * Janice, tell me about the woodshed");
		w.newline();
		w.bufferString("  * save, restore");
		w.newline();
		w.newline();
		w.bufferString("For a more detailed tutorial, type 'help' inside the Curses or Anchorhead games.");
		w.newline();
		w.newline();
		w.bufferString("Twisty comes with three built-in games, but if you visit sites like ");
		w.bufferString("www.ifarchive.org or ifdb.tads.org, you can download ");
		w.bufferString("more games that end with either .z3, .z5, or .z8, copy them to your sdcard, then open them in Twisty.");
		w.newline();
		w.newline();
		w.flush();
	}

	private boolean zmIsRunning() {
		return (zm != null && zm.isRunning());
	}

	private void appendBatteryState(StringBuffer sb) {
		IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent intent = registerReceiver(null, battFilter);

		int rawlevel = intent.getIntExtra("level", -1);
		int scale = intent.getIntExtra("scale", -1);
		int status = intent.getIntExtra("status", -1);
		int health = intent.getIntExtra("health", -1);
		int level = -1;  // percentage, or -1 for unknown
		if (rawlevel >= 0 && scale > 0) {
			level = (rawlevel * 100) / scale;
		}
		sb.append("The phone");
		if (BatteryManager.BATTERY_HEALTH_OVERHEAT == health) {
			sb.append("'s battery feels very hot!");
		} else {
			switch(status) {
			case BatteryManager.BATTERY_STATUS_UNKNOWN:
				// old emulator; maybe also when plugged in with no battery
				sb.append(" has no battery.");
				break;
			case BatteryManager.BATTERY_STATUS_CHARGING:
				sb.append("'s battery");
				if (level <= 33)
					sb.append(" is charging, and really ought to " +
					"remain that way for the time being.");
				else if (level <= 84)
					sb.append(" charges merrily.");
				else
					sb.append(" will soon be fully charged.");
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
				if (level == 0)
					sb.append(" needs charging right away.");
				else if (level > 0 && level <= 33)
					sb.append(" is about ready to be recharged.");
				else
					sb.append("'s battery discharges merrily.");
				break;
			case BatteryManager.BATTERY_STATUS_FULL:
				sb.append(" is fully charged up and ready to go on " +
				"an adventure of some sort.");
				break;
			default:
				sb.append("'s battery is indescribable!");
			break;
			}
		}
		sb.append(" ");
	}

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
				listener.onKeyDown(v, tb, keyCode, event);
				break;
			case KeyEvent.ACTION_UP:
				listener.onKeyUp(v, tb, keyCode, event);
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
		Log.i(TAG, "GOT KEYCODE: " + key_code);
		screen.keyDown(e, key_code);
	}

	/** Set up things so the zmachine can request screen updates */
	void InitZJApp() {
		status_line = new StatusLine(this);
		status = new ZStatus(status_line.getLeft(), status_line.getRight());
		int[] views = new int[] { R.id.body, R.id.status_v5 };
		ZViewOutput[] tva = new ZViewOutput[views.length];
		for (int i = 0; i < views.length; ++i) {
			View v = findViewById(views[i]);
			if (v instanceof TwistyView) {
				TwistyView tv = (TwistyView) v;
				tva[i] = new ZViewOutput(tv);
			} else {
				fatal("Internal error: View type should be TwistyView");
				return;
			}
		}
		screen = new ZScreen(tva, dialog_handler, FIXED_FONT_NAME, 
				ROMAN_FONT_NAME, FONT_SIZE);
	}

	/**
	 * Start a zmachine, loading the program from the given file
	 * @param filename Name of file to load
	 */
	void startzm(String filename) {
		if (zmIsRunning())
			return;
		runningProgram = filename;
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
		runningProgram = new Integer(resource);
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
		byte zmemimage[] = null;
		setViewVisibility(R.id.errors, View.GONE);
		try {
			zmemimage = suckstream(zstream);
		} catch (IOException e) {
			fatal("I/O Error:\n" + Log.getStackTraceString(e));
			// don't set failed, may want to retry
		}
		screen.clear();
		screen.clearInputQueues();
		if (zmemimage != null) {
			switch (zmemimage[0]) {
			case 3:
				setViewVisibility(R.id.status_v3, View.VISIBLE);
				setViewVisibility(R.id.status_v5, View.GONE);
				zm = new ZMachine3(screen, status, zmemimage);
				break;
			case 5:
				setViewVisibility(R.id.status_v3, View.GONE);
				setViewVisibility(R.id.status_v5, View.VISIBLE);
				zm = new ZMachine5(screen, zmemimage);
				break;
			case 8:
				setViewVisibility(R.id.status_v3, View.GONE);
				setViewVisibility(R.id.status_v5, View.VISIBLE);
				zm = new ZMachine8(screen, zmemimage);
				break;
			default:
				fatal("Not a valid V3, V5, or V8 story file (" +
						Integer.toString(zmemimage[0]) + ")");
			}
			if (zm != null) {
				prepareToStartZM();  // this can null out zm for failure
			}
			if (zm != null) {
				//zm.zmlog = true;  // Kills game performance. Debugging only.
				zm.start();
			}
		}
	}

	private void prepareToStartZM() {
		if (preStartZM != null) {
			preStartZM.run();
			preStartZM = null;
		}
	}

	/** Convenience helper that turns a stream into a byte array */
	byte[] suckstream(InputStream mystream) throws IOException {
		byte buffer[];
		byte oldbuffer[];
		int currentbytes = 0;
		int bytesleft;
		int got;
		int buffersize = 65536;

		buffer = new byte[buffersize];
		bytesleft = buffersize;
		got = 0;
		while (got != -1) {
			bytesleft -= got;
			currentbytes += got;
			if (bytesleft == 0) {
				oldbuffer = buffer;
				buffer = new byte[buffersize + currentbytes];
				System.arraycopy(oldbuffer, 0, buffer, 0, currentbytes);
				oldbuffer = null;
				bytesleft = buffersize;
			}
			got = mystream.read(buffer, currentbytes, bytesleft);
		}
		if (buffer.length != currentbytes) {
			oldbuffer = buffer;
			buffer = new byte[currentbytes];
			System.arraycopy(oldbuffer, 0, buffer, 0, currentbytes);
		}
		return buffer;
	}

	/** Displays a fatal error message */
	void fatal(String s) {
		setViewVisibility(R.id.status_v3, View.VISIBLE);
		setViewVisibility(R.id.status_v5, View.GONE);
		setItemText(R.id.statusL, "Error");
		setItemText(R.id.statusR, "");
		setViewVisibility(R.id.errors, View.VISIBLE);
		setItemText(R.id.errors, s);
	}

	/** Stops the currently running zmachine. */
	public void stopzm() {
		if (zmIsRunning()) {
			zm.abort();
			try {
				zm.join();
			} catch (InterruptedException e) {
			}
		}
		zm = null;
		setViewVisibility(R.id.status_v3, View.GONE);
		setViewVisibility(R.id.status_v5, View.GONE);
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
			menu.add(Menu.NONE, R.raw.advent, 0, "Adventure").setShortcut('0', 'a');
			menu.add(Menu.NONE, R.raw.anchor, 1, "Anchorhead").setShortcut('1', 'b');
			menu.add(Menu.NONE, R.raw.curses, 2, "Curses").setShortcut('2', 'c');
			menu.add(Menu.NONE, MENU_PICK_FILE, 3, "Open file...").setShortcut('5', 'o');
			menu.add(Menu.NONE, MENU_SHOW_HELP, 4, "Help!?").setShortcut('6', 'h');
		} else {
			menu.add(Menu.NONE, MENU_RESTART, 0, "Restart").setShortcut('7', 'r');
			menu.add(Menu.NONE, MENU_STOP, 1, "Stop").setShortcut('9', 's');
			// menu.add(Menu.NONE, DEBUG_ZM_DUMP, 2, "Dump");
			// menu.add(Menu.NONE, DEBUG_PAUSE_RESUME, 3, "Pause/Resume");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case MENU_RESTART:
			zm.restart();
			break;
		case MENU_STOP:
			stopzm();
			// After the zmachine exits, the welcome message should show
			// again.
			break;
		case MENU_PICK_FILE:
			pickFile();
			break;
		case MENU_SHOW_HELP:
			printHelpMessage();
			break;
		case DEBUG_PAUSE_RESUME:
			if (zm.pauseZM()) {
				zm.resumeZM();
			} else {
				fatal("pause failed");
			}
			break;
		case DEBUG_ZM_DUMP:
			String[] l = zm.zmLogEntries.toArray(new String[0]);
			zm.zmLogEntries.clear();
			ZViewOutput o = zm.screen.getView(0);
			for (String s : l) {
				o.output(s, false, null);
				o.newline();
			}
			zm.zmLog = true;
			break;
		default:
			screen.clear();
			startzm(item.getItemId());
		break;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Launch UI to pick a file to load and execute */
	private void pickFile() {
		String storagestate = Environment.getExternalStorageState();
		if (storagestate.equals(Environment.MEDIA_MOUNTED)
				|| storagestate.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			final ProgressDialog pd = ProgressDialog.show(Twisty.this,
					"Scanning Media", "Searching for Z-Games...", true);
			Thread t = new Thread() {
				public void run() {
					// populate our list of zgames:
					discovered_zgames = scanForZGames();
					pd.dismiss();
					Message msg = new Message();
			        msg.what = PROMPT_FOR_ZGAME;
			        dialog_handler.sendMessage(msg);
				}
			};
			t.start();
		}	
		else
			showDialog(DIALOG_NO_SDCARD); // no sdcard to scan
	}

	/** Called from UI thread to request cleanup or whatever */
	public void onZmFinished(final ZMachineException e) {
		zm = null;
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

	// Return the path to the saved-games directory.
	// If SDcard not present, or if /sdcard/twisty is a file, return null.
	private String ensureSavedGamesDir(boolean write) {
		String storagestate = Environment.getExternalStorageState();
		if (!storagestate.equals(Environment.MEDIA_MOUNTED) &&
				(write || storagestate.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
			return null;
		}
		String sdpath = Environment.getExternalStorageDirectory().getPath();
		File savedir = new File(sdpath + "/twisty");
		if (! savedir.exists()) {
			savedir.mkdirs();
		}
		else if (! savedir.isDirectory()) {
			return null;
		}
		return savedir.getPath();
	}

	// Walk DIR recursively, adding any file matching *.z[1-8] to LIST.
	private void scanDir(File dir, ArrayList<String> list) {
		File[] children = dir.listFiles();
		if (children == null)
			return;
		for (int count = 0; count < children.length; count++) {
			File child = children[count];
			if (child.isFile() && child.getName().matches("[^.].*\\.[Zz][1-8]"))
				list.add(child.getPath());
			else
				scanDir(child, list);
		}
	}

	// Recursively can the sdcard for z-games.  Return an array of absolute paths,
	// or null if no sdcard is available.
	private String[] scanForZGames() {
		String storagestate = Environment.getExternalStorageState();
		if (!storagestate.equals(Environment.MEDIA_MOUNTED) &&
				!storagestate.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			return null;
		}
		File sdroot = Environment.getExternalStorageDirectory();
		ArrayList<String> zgamelist = new ArrayList<String>();
		scanDir(sdroot, zgamelist);
		String[] files = zgamelist.toArray(new String[zgamelist.size()]);
		Arrays.sort(files);
		return files;
	}

	private void promptForSavefile() {
		String dir = ensureSavedGamesDir(true);
		if (dir == null) {
			showDialog(DIALOG_CANT_SAVE);
			return;
		}
		savegame_dir = dir;	
		showDialog(DIALOG_ENTER_SAVEFILE);
	}

	private void promptForRestorefile() {
		String dir = ensureSavedGamesDir(false);
		if (dir == null) {
			showDialog(DIALOG_CANT_SAVE);
			return;
		}
		savegame_dir = dir;
		showDialog(DIALOG_ENTER_RESTOREFILE);
	}

	// Used by 'Restore Game' dialog box;  scans /sdcard/twisty and updates
	// the list of radiobuttons.
	private void updateRestoreRadioButtons(RadioGroup rg) {
		rg.removeAllViews();
		int id = 0;
		String[] gamelist  = new File(savegame_dir).list();
		for (String filename : gamelist) {
			RadioButton rb = new RadioButton(Twisty.this);
			rb.setText(filename);
			rg.addView(rb);
			id = rb.getId();
		}
		rg.check(id); // by default, check the last item
	}

	// Used by 'Choose ZGame' dialog box;  scans all of /sdcard for zgames,
	// updates list of radiobuttons (and the zgame_paths HashMap).
	private void updateZGameRadioButtons(RadioGroup rg) {
		rg.removeAllViews();
		zgame_paths.clear();
		int id = 0;
		for (String path : discovered_zgames) {
			RadioButton rb = new RadioButton(Twisty.this);
			rb.setText(new File(path).getName());
			rg.addView(rb);
			id = rb.getId();
			zgame_paths.put(id, path);
		}
	}

	/** Have our activity manage and persist dialogs, showing and hiding them */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case DIALOG_ENTER_SAVEFILE:
			LayoutInflater factory = LayoutInflater.from(this);
			final View textEntryView = factory.inflate(R.layout.save_file_prompt, null);
			final EditText et = (EditText) textEntryView.findViewById(R.id.savefile_entry);
			return new AlertDialog.Builder(Twisty.this)
			.setTitle("Save Game")
			.setView(textEntryView)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					savefile_path = savegame_dir + "/" + et.getText().toString();
					// Directly modify the message-object passed to us by the z-machine thread:
					dialog_message.path = savefile_path;
					// Wake up the ZMachine thread again
					synchronized (screen) {
						screen.notify();
					}
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// This makes op_save() fail.
					dialog_message.path = "";
					// Wake up the ZMachine thread again
					synchronized (screen) {
						screen.notify();
					}
				}
			})
			.create();

		case DIALOG_ENTER_RESTOREFILE:
			restoredialog = new Dialog(Twisty.this);
			restoredialog.setContentView(R.layout.restore_file_prompt);
			restoredialog.setTitle("Restore Saved Game");
			android.widget.RadioGroup rg = (RadioGroup) restoredialog.findViewById(R.id.radiomenu);
			updateRestoreRadioButtons(rg);
			android.widget.Button okbutton = (Button) restoredialog.findViewById(R.id.restoreokbutton);
			okbutton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					android.widget.RadioGroup rg = (RadioGroup) restoredialog.findViewById(R.id.radiomenu);
					int checkedid = rg.getCheckedRadioButtonId();
					if (rg.getChildCount() == 0) {  // no saved games:  FAIL
						savefile_path = "";
					} else	if (checkedid == -1) { // no game selected
						RadioButton firstbutton = (RadioButton) rg.getChildAt(0); // default to first game
						savefile_path = savegame_dir + "/" + firstbutton.getText(); 
					} else {
						RadioButton checkedbutton = (RadioButton) rg.findViewById(checkedid);
						savefile_path = savegame_dir + "/" + checkedbutton.getText();
					}
					dismissDialog(DIALOG_ENTER_RESTOREFILE);
					// Return control to the z-machine thread
					dialog_message.path = savefile_path;
					synchronized (screen) {
						screen.notify();
					}
				}
			});
			android.widget.Button cancelbutton = (Button) restoredialog.findViewById(R.id.restorecancelbutton);
			cancelbutton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					dismissDialog(DIALOG_ENTER_RESTOREFILE);
					// Return control to the z-machine thread
					dialog_message.path = "";
					synchronized (screen) {
						screen.notify();
					}
				}
			});
			return restoredialog;

		case DIALOG_CHOOSE_ZGAME:
			choosezgamedialog = new Dialog(Twisty.this);
			choosezgamedialog.setContentView(R.layout.choose_zgame_prompt);
			choosezgamedialog.setTitle("Choose Game");
			android.widget.RadioGroup zrg = (RadioGroup) choosezgamedialog.findViewById(R.id.zgame_radiomenu);
			updateZGameRadioButtons(zrg);
			zrg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					dismissDialog(DIALOG_CHOOSE_ZGAME);
					String path = (String) zgame_paths.get(checkedId);
					if (path != null) {
						stopzm();
						startzm(path);
					}
				}
			});
			return choosezgamedialog;

		case DIALOG_CANT_SAVE:
			return new AlertDialog.Builder(Twisty.this)
			.setTitle("Cannot Access Saved Games")
			.setMessage("Saved-games folder is not available on external media.")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// A path of "" makes op_save() fail.
					dialog_message.path = "";
					// Wake up the ZMachine thread again
					synchronized (screen) {
						screen.notify();
					}
				}
			})
			.create();

		case DIALOG_NO_SDCARD:
			return new AlertDialog.Builder(Twisty.this)
			.setTitle("No External Media")
			.setMessage("Cannot find sdcard or other media.")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// do nothing
				}
			})
			.create();
		}
		return null;
	}


	/** Have our activity prepare dialogs before displaying them */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case DIALOG_ENTER_RESTOREFILE:
			android.widget.RadioGroup rg = (RadioGroup) restoredialog.findViewById(R.id.radiomenu);
			updateRestoreRadioButtons(rg);
			break;
		case DIALOG_CHOOSE_ZGAME:
			android.widget.RadioGroup zrg = (RadioGroup) choosezgamedialog.findViewById(R.id.zgame_radiomenu);
			updateZGameRadioButtons(zrg);
			break;
		}
	}

	public void showMore(boolean show) {
		setViewVisibility(R.id.more, show ? View.VISIBLE : View.GONE);
	}

/*
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore saved view state
		super.onRestoreInstanceState(savedInstanceState);
		// TODO: restore zmachine state
	}
*/

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		// Save zmachine state, if any
		if (zm != null && runningProgram != null && zm.pauseZM()) {
			if (runningProgram instanceof String) {
				// running a game file
				bundle.putString(RUNNING_FILE, (String) runningProgram);
			} else if (runningProgram instanceof Integer) {
				// running a resource
				bundle.putInt(RUNNING_RESOURCE, ((Integer) runningProgram).intValue());
			}
			// TODO(marius): Context.openFileOutput() or byte array in bundle
			// FileOutputStream fos = openFileOutput("frozen", 0);
			Log.i(TAG, "Saving zm state to memory");
			ZState zs = new ZState(zm);
			byte[] result = zs.mem_save(zm.pc);
			if (result == null) {
				Log.e(TAG, "Saving zm state failed");
			} else {
				Log.i(TAG, "Saving zm state done: result = " + result.toString() + ", size = " + result.length);
				bundle.putByteArray(FROZEN_GAME, result);
			}
			zm.resumeZM();
		}
		// Hopefully this will save all the view states:
		super.onSaveInstanceState(bundle);
	}

	private boolean unfreezeZM(Bundle icicle) {
		if (icicle == null)
			return false;
		String filename = icicle.getString(RUNNING_FILE);
		int rsrc = icicle.getInt(RUNNING_RESOURCE, -1);
		final byte[] frozen_game = icicle.getByteArray(FROZEN_GAME);
		if (frozen_game == null)
			return false;
		Log.i(TAG, "unfreezing running game");
		preStartZM = new Runnable() {
			public void run() {
				Log.i(TAG, "unfreeze: restoring");
				ZState zs = new ZState(zm);
				if (zs.restore_from_mem(frozen_game)) {
					zm.restore(zs);
					Log.i(TAG, "unfreeze: successful");
				} else {
					Log.w(TAG, "unfreeze: restore failed");
					zm = null;
					setupWelcomeMessage();
				}
			}
		};
		boolean result = false;
		if (rsrc != -1) {
			startzm(rsrc);
			result = true;
		} else if (filename != null){
			startzm(filename);
			result = true;
		}
		preStartZM = null;
		Log.i(TAG, "unfreeze: finished");
		return result;
	}
}
