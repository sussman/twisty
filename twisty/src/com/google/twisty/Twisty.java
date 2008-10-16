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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.twisty.zplet.Event;
import com.google.twisty.zplet.StatusLine;
import com.google.twisty.zplet.ZMachineException;
import com.google.twisty.zplet.ZViewOutput;

import com.google.twisty.TwistyMessage;

import russotto.zplet.screenmodel.ZScreen;
import russotto.zplet.screenmodel.ZStatus;
import russotto.zplet.screenmodel.ZWindow;
import russotto.zplet.zmachine.ZMachine;
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
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
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

public class Twisty extends Activity {
	private static final int MENU_PICK_FILE = 101;
	private static final int MENU_STOP = 102;
	private static final int MENU_RESTART = 103;
	private static final int FILE_PICKED = 104;
	private static String TAG = "Twisty";
	private static final String FIXED_FONT_NAME = "Courier";
	private static final String ROMAN_FONT_NAME = "Helvetica";
	private static final int FONT_SIZE = 12;
	private String savegame_dir = "";
	private String savefile_path = "";

	// Dialog boxes we manage
	private static final int DIALOG_ENTER_SAVEFILE = 1;
	private static final int DIALOG_ENTER_RESTOREFILE = 2;
	private static final int DIALOG_CHOOSE_ZGAME = 3;
	private static final int DIALOG_CANT_SAVE = 4;
	private static final int DIALOG_NO_SDCARD = 5;
	private static final int DIALOG_SCANNING_SDCARD = 6;

	// Messages we receive from the ZMachine thread
	public static final int PROMPT_FOR_SAVEFILE = 1;
	public static final int PROMPT_FOR_RESTOREFILE = 2;

	// TODO:  see issue 9 -- eventually pass this to Context.openFileOutput()
	// private static String FROZEN_GAME_FILE = "frozengame";
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
	private ProgressDialog scanningdialog;
	// A persistent map of button-ids to zgames found on the sdcard (absolute paths)
	private HashMap<Integer, String> zgame_paths = new HashMap<Integer, String>();

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
			setupWelcomeMessage();
		} catch (Exception e) {
			fatal("Oops, an error occurred preparing to play");
			Log.e(TAG, "Failed to get prepare to play", e);
		}
	}

	/** Called when the activity is paused for any reason. */
	@Override
	public void onPause() {
		super.onPause();
	}

	/** Called when activity is about to begin execution. */
	@Override
	public void onResume() {
		super.onResume();
	}

	private void setupWelcomeMessage() {
		setViewVisibility(R.id.errors, View.GONE);
		setViewVisibility(R.id.more, View.GONE);
		setViewVisibility(R.id.status_v3, View.GONE);
		setViewVisibility(R.id.status_v5, View.GONE);
		// Get the battery status in order to print the
		// welcome message with the details
		fetchBatteryState();
	}

	private void printWelcomeMessage(final String details) {
		if (zmIsRunning()) {
			Log.e(TAG, "Called printWelcomeMessage with ZM running");
			return;
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
		w.bufferString("  Twisty v0.11, (C) 2008 Google Inc.  ");
		w.newline();
		w.bufferString("                                      ");
		w.newline();
		w.set_text_style(ZWindow.ROMAN);
		w.bufferString("Adapted from:");
		w.newline();
		w.bufferString("    Zplet, a Z-Machine interpreter in Java:");
		w.newline();
		w.bufferString("    Copyright 1996, 2001 Matthew T. Russotto.");
		w.newline();
		w.bufferString("This is open source software:");
		w.newline();
		w.bufferString("    see http://code.google.com/p/twisty");
		w.newline();
		w.newline();
		// TODO: make this change depending on device features
		w.bufferString("You are holding a modern-looking phone with a "
				+ "QWERTY keypad. "
				+ details
				+ "You feel an inexplicable urge to "
				+ "press the phone's \"menu\" key. ");
		w.flush();
	}

	private boolean zmIsRunning() {
		return (zm != null && zm.isAlive());
	}

	private void fetchBatteryState() {
		IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent intent = registerReceiver(null, battFilter);
		StringBuilder sb = new StringBuilder();

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
		sb.append(' ');
		printWelcomeMessage(sb.toString());
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
				//zm.zmlog = true;  // Kills game performance. Debugging only.
				zm.start();
			}
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

	/** Stops the currently running zmachine */
	public void stopzm() {
		if (zmIsRunning()) {
			zm.abort();
			// Some games need to be sent a key press or two before they will
			// actually quit.
			// TODO(mariusm): send one if the zmachine is not currently waiting
			// in screen.read_code() or similar; are a second and third really
			// ever needed?
			sendKeyEvent(Event.KEY_PRESS, '\n');
			sendKeyEvent(Event.KEY_PRESS, '\n');
			sendKeyEvent(Event.KEY_PRESS, '\n');
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
			menu.add(Menu.NONE, R.raw.bronze, 1, "Bronze").setShortcut('1', 'b');
			menu.add(Menu.NONE, R.raw.curses, 2, "Curses").setShortcut('2', 'c');
			menu.add(Menu.NONE, MENU_PICK_FILE, 3, "Open file...").setShortcut('5', 'o');
		} else {
			menu.add(Menu.NONE, MENU_RESTART, 0, "Restart").setShortcut('7', 'r');
			menu.add(Menu.NONE, MENU_STOP, 1, "Stop").setShortcut('9', 's');
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
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
		startzm(item.getItemId());
		break;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Launch UI to pick a file to load and execute */
	private void pickFile() {
		String storagestate = android.os.Environment.getExternalStorageState();
		if (!storagestate.equals(android.os.Environment.MEDIA_MOUNTED))
			showDialog(DIALOG_NO_SDCARD); // no sdcard to scan
		else
			showDialog(DIALOG_CHOOSE_ZGAME);
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

	// Return the path to the saved-games directory.
	// If SDcard not present, or if /sdcard/twisty is a file, return null.
	private String ensureSavedGamesDir() {
		String storagestate = android.os.Environment.getExternalStorageState();
		if (!storagestate.equals(android.os.Environment.MEDIA_MOUNTED)) {
			return null;
		}
		String sdpath = android.os.Environment.getExternalStorageDirectory().getPath();
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
			if (child.isFile() && child.getName().matches(".*\\.z[1-8]"))
				list.add(child.getPath());
			else
				scanDir(child, list);
		}
	}

	// Recursively can the sdcard for z-games.  Return an array of absolute paths,
	// or null if no sdcard is available.
	private String[] scanForZGames() {
		String storagestate = android.os.Environment.getExternalStorageState();
		if (!storagestate.equals(android.os.Environment.MEDIA_MOUNTED)) {
			return null;
		}
		File sdroot = android.os.Environment.getExternalStorageDirectory();
		ArrayList<String> zgamelist = new ArrayList<String>();
		showDialog(DIALOG_SCANNING_SDCARD);
		scanDir(sdroot, zgamelist);
		dismissDialog(DIALOG_SCANNING_SDCARD);
		return zgamelist.toArray(new String[0]);
	}

	private void promptForSavefile() {
		String dir = ensureSavedGamesDir();
		if (dir == null) {
			showDialog(DIALOG_CANT_SAVE);
			return;
		}
		savegame_dir = dir;	
		showDialog(DIALOG_ENTER_SAVEFILE);
	}

	private void promptForRestorefile() {
		String dir = ensureSavedGamesDir();
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
		String[] zgames = scanForZGames();
		for (String path : zgames) {
			RadioButton rb = new RadioButton(Twisty.this);
			rb.setText(new File(path).getName());
			rg.addView(rb);
			id = rb.getId();
			zgame_paths.put(id, path);
		}
		rg.check(id); // by default, check the last item
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
			android.widget.Button zokbutton = (Button) choosezgamedialog.findViewById(R.id.zgameokbutton);
			zokbutton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					android.widget.RadioGroup zrg = (RadioGroup) choosezgamedialog.findViewById(R.id.zgame_radiomenu);
					int checkedid = zrg.getCheckedRadioButtonId();
					dismissDialog(DIALOG_CHOOSE_ZGAME);
					String path = (String) zgame_paths.get(checkedid);
					if (path != null) {
						stopzm();
						startzm(path);
					}
				}
			});
			android.widget.Button zcancelbutton = (Button) choosezgamedialog.findViewById(R.id.zgamecancelbutton);
			zcancelbutton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					dismissDialog(DIALOG_CHOOSE_ZGAME);
				}
			});
			return choosezgamedialog;

		case DIALOG_SCANNING_SDCARD:
			scanningdialog = new ProgressDialog(Twisty.this);
			scanningdialog.setIndeterminate(true);
			scanningdialog.setTitle("Scanning sdcard...");
			scanningdialog.setMessage("Looking for z-games");
			return scanningdialog;

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
}
