/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.zmachine;

import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import android.util.Log;

import com.google.code.twisty.zplet.ZMachineException;
import com.google.code.twisty.zplet.ZMachineInterrupted;

import russotto.zplet.screenmodel.ZScreen;
import russotto.zplet.screenmodel.ZStatus;
import russotto.zplet.screenmodel.ZWindow;
import russotto.zplet.zmachine.state.ZState;

public abstract class ZMachine {
	private final Thread thread;
	public ZWindow current_window;
	public int pc;
	public ZWindow window[];
	public ZHeader header;
	public ZScreen screen;
	public ZObjectTree objects;
	public ZDictionary zd;
	public ZState restart_state;
	protected ZStatus status_line;
	public byte memory_image[];
	public Stack<Object> zstack;
	public Random zrandom;
	protected int globals;
	public short locals[];
	protected int inputstream;
	protected boolean outputs[];
	protected int printmemory;
	protected int alphabet;
	protected short build_ascii;
	protected short built_ascii;
	protected short abbrev_mode;
	protected short checksum;
	protected ZInstruction zi;
	protected boolean status_redirect;
	protected String status_location;
	public boolean zmLog;
	private StringBuffer zmLogString;
	public CircularList<String> zmLogEntries;

	protected final String A2 = "0123456789.,!?_#\'\"/\\-:()";
	private InterruptState runState;

	public final static int OP_LARGE = 0;
	public final static int OP_SMALL = 1;
	public final static int OP_VARIABLE = 2;
	public final static int OP_OMITTED = 3;
	private static final String TAG = "ZMachine";
	// private static final String ZMLOG = "zmlog";

	/*
	 * UNSTARTED -start-> RUNNING -quit-------------> FINISHED
	 *                    ^ | |                      /
	 *                    |  \ \-abort-> ABORTING ->/
	 *              RESUMING  \
	 *                    ^    \-pause-> PAUSING
	 *                    |               |
	 *                 resume-- PAUSED <-/
	 */
	enum InterruptState {
		UNSTARTED,
		RUNNING,
		ABORTING,
		PAUSING,
		PAUSED,
		RESUMING,
		FINISHED
	}

	public ZMachine(ZScreen screen, ZStatus status_line, byte [] memory_image) {
		thread = new Thread() {
			@Override
			public void run() {
				runZM();
			}
		};
		this.screen = screen;
		this.status_line = status_line;
		this.memory_image = memory_image;
		locals = new short[0];
		zstack = new Stack<Object>();
		restart_state = new ZState(this);
		restart_state.save_current();
		zrandom = new Random(); /* starts in "random" mode */
		inputstream = 0;
		outputs = new boolean[5];
		outputs[1] = true;
		alphabet = 0;
		zmLog = false;
		zmLogString = new StringBuffer();
		zmLogEntries = new CircularList<String>(1000);
		runState = InterruptState.UNSTARTED;
	}

	public abstract void update_status_line();

	public byte get_input_byte(boolean buffered) {
		short code;
		if (inputstream == 0) {
			//					System.err.print("get_input_byte ");
			//					if (current_window == window[0])
			//								System.err.println("0");
			//					else if (current_window == window[1])
			//								System.err.println("1");
			//					else
			//								System.err.println("?");
			screen.set_input_window(current_window);
			if (buffered) {
				code = screen.read_buffered_code();
			}
			else {
				code = screen.read_code();
			}
		}
		else {
			/* TODO: Support other streams */
			fatal("Stream " + Integer.toString(inputstream,10) +
			" not supported.");
			code = 13;
		}
		return (byte)code;
	}

	public void print_ascii_string(String s)
	{
		int i;
		for (i = 0; i < s.length(); i++) 
		{
			print_ascii_char((short)s.charAt(i));
		}
	}

	public void print_ascii_char(short ch) {
		int nchars;
		if (zmLog) {
			if (ch >= 20 && ch <= 127) {
				zmLogString.append((char) ch);
			} else {
				zmLogString.append('<')
				.append(Integer.toHexString(ch))
				.append('>');
			}	
		}
		if (status_redirect) {
			status_location += (char)ch;
		}
		else if (outputs[3]) {
			nchars = ((memory_image[printmemory] << 8)&0xFF00) |
			(((int)(memory_image[printmemory+1]))&0xFF);
			if (ch > 255)
				memory_image[printmemory + nchars + 2] = (byte)'?';
			else if (ch == 10)
				memory_image[printmemory + nchars + 2] = (byte)13;
			else
				memory_image[printmemory + nchars + 2] = (byte)ch;
			nchars++;
			memory_image[printmemory] = (byte)(nchars >>> 8);
			memory_image[printmemory + 1] = (byte)(nchars&0xFF);
		}
		else {
			if (outputs[1]) {
				if ((ch == 13) || (ch == 10)) {
					current_window.newline();
				}					
				else
					current_window.printzascii(ch);
			}
			outputs[2] = header.transcripting();
			if (outputs[2] && current_window.transcripting()) {
				// TODO: android: send transcript somewhere
				/*
				if ((ch == 13) || (ch == 10)) {
					System.out.println();
				}					
				else
					System.out.print((char)ch);
				*/
			}
		}
	}

	public abstract int string_address(short addr);

	public abstract int routine_address(short addr);

	public short [] encode_word(int wordloc, int wordlen, int nwords) {
		short encword[] = new short[nwords];
		int zchars[] = new int[nwords * 3];
		int i;
		int zi;
		int ch;
		int a2index;

		zi = 0;
		for (i = 0; i < wordlen; i++) {
			ch = (int)memory_image[wordloc + i];
			if ((ch >= (int)'a') && (ch <= (int)'z')) {
				zchars[zi] = ch - (int)'a' + 6;
				if ((++zi) == (nwords*3))
					break;
			}
			else if ((ch >= (int)'A') && (ch <= (int)'Z')) {
				/* encode upper as lower.  Legal? */
				Log.e(TAG, "Tried to encode uppercase dictionary word");
				zchars[zi] = ch - (int)'A' + 6;
				if ((++zi) == (nwords*3))
					break;
			}
			else if ((a2index = A2.indexOf(ch)) != -1) {
				/* From A2 */
				zchars[zi] = 5;
				if ((++zi) == (nwords*3))
					break;
				zchars[zi] = a2index + 8;
				if ((++zi) == (nwords*3))
					break;
			}
			else { /* gotta do ascii */
				zchars[zi] = 5;
				if ((++zi) == (nwords*3))
					break;
				zchars[zi] = 6;
				if ((++zi) == (nwords*3))
					break;
				zchars[zi] = ch >> 5;
			if ((++zi) == (nwords*3))
				break;
			zchars[zi] = ch & 0x1F;
			if ((++zi) == (nwords*3))
				break;
			}
		}
		while (zi < (nwords * 3)) {
			zchars[zi++] = 5;
		}
		zi = 0;
		for (i = 0; i < nwords; i++) {
			encword[i] = (short)(zchars[zi++]<<10);
			encword[i] |= (short)(zchars[zi++]<<5);
			encword[i] |= (short)(zchars[zi++]);
		}
		encword[nwords-1] |= (short)0x8000;
		return encword;
	}

	protected short alphabet_lookup(byte zchar) {
		switch (alphabet) {
		case 0:
			return (short)((short)'a' + zchar - 6);
		case 1:
			return (short)((short)'A' + zchar - 6);
		case 2:
			if (zchar == 7) 
				return 13;
			else {
				return (short)(A2.charAt(zchar-8));
			}
		}
		fatal("Bad Alphabet");
		return -1;
	}

	void print_abbrev(int abbr_num) {
		int abbrev_index;
		int string_addr;

		abbrev_mode = -1;
		abbrev_index = header.abbrev_table() + 2 * abbr_num;
		string_addr = (((memory_image[abbrev_index]<<8)&0xFF00) |
				(((int)memory_image[abbrev_index + 1]) & 0xFF)) * 2;
		print_string(string_addr);
	}

	public void print_zchar(byte zchar)
	{
		if (build_ascii > 0) {
			//					System.err.print("building ascii stage ");
			//					System.err.println(build_ascii);
			built_ascii = (short)((built_ascii<<5) | zchar);
			build_ascii++;
			if (build_ascii == 3) {
				//								System.err.println("built ascii: " + built_ascii);
				print_ascii_char(built_ascii);
				build_ascii = 0;
				built_ascii = 0;
			}
			alphabet = 0;
		}
		else if (abbrev_mode > 0) {
			print_abbrev(32*(abbrev_mode - 1) + zchar);
			abbrev_mode = 0;
			build_ascii = 0;
			alphabet = 0;
		}
		else {
			switch (zchar) {
			case 0: 
				print_ascii_char((short)' ');
				break;
			case 1:
			case 2:
			case 3:
				if (abbrev_mode != 0)
					fatal("Abbreviation in abbreviation");
				abbrev_mode = zchar;
				alphabet = 0;
				break;
			case 4:
				alphabet = (alphabet + 1) % 3;
				break;
			case 5:
				alphabet = (alphabet + 2) % 3;
				break;
			case 6:
				if (alphabet == 2) {
					build_ascii = 1;
					alphabet = 0;
					break;
				}
			default:
				print_ascii_char(alphabet_lookup(zchar));
			alphabet = 0;
			break;
			}				
		}
	}

	public int print_string(int addr) {
		/* returns # bytes processed.  ADDR is a byte address (hence an int) */
		int nbytes;
		byte zchars[];
		int zseq;
		int i;

		zmLogString.setLength(0);
		nbytes = 0;
		build_ascii = 0;
		alphabet = 0;
		abbrev_mode = 0;
		zchars = new byte[3];
		do {
			zseq = ((memory_image[addr++] << 8) & 0xFF00) |
			(((int)memory_image[addr++])&0xFF);
			zchars[0] = (byte)((zseq>>10) & 0x1F);
			zchars[1] = (byte)((zseq>>5) & 0x1F);
			zchars[2] = (byte)(zseq&0x001F);
			for (i = 0; i < 3; i++)
				print_zchar(zchars[i]);
			nbytes+=2;
		}
		while ((zseq & 0x8000) == 0);
		logPrintedString();
		return nbytes;
	}

	public void start() {
		changeState(InterruptState.RUNNING);
		screen.clear();
		restart();
		header.set_transcripting(false);
		thread.start();
	}
	
	public void join() throws InterruptedException {
		thread.join();
	}
	
	public abstract String[] getOpnames();
	
	/**
	 * Block until the state is anything but other
	 * @param other the state we don't want to be in
	 * @return the new state (or other if we timed out or were interrupted)
	 */
	private synchronized InterruptState waitForStateChange(InterruptState other) {
		try {
			while (runState == other) {
				wait();
			}
		} catch (InterruptedException e) {
			Log.w(TAG, "Untested condition: interrupted in waitForStateChange/" + other.toString());
			// TODO: figure out exactly when this can happen, and what to do
		}
		return runState;
	}

	private synchronized InterruptState changeState(InterruptState newState) {
		Log.i(TAG, "Changing " + runState.toString() + " -> " + newState.toString());
		InterruptState oldState = runState;
		runState = newState;
		notifyAll();
		return oldState;
	}

	private void mainLoop(ProfileStats timers) {
		while (true) {
			// reset interrupted status
			if (Thread.interrupted()) {
				Log.w(TAG, "Was interrupted - but not any more...");
			}
			InterruptState s = waitForStateChange(InterruptState.PAUSED);
			switch (s) {
			case ABORTING:
				changeState(InterruptState.FINISHED);
				return;
			case PAUSING:
				changeState(InterruptState.PAUSED);
				break;
			case RESUMING:
				changeState(InterruptState.RUNNING);
				// ** fall through **
			case RUNNING:
				int oldpc = pc;
				try {
					// TODO handle PAUSING: save previous pc
					long t1 = System.nanoTime();
					zi.decode_instruction();
					zi.execute();
					long t2 = System.nanoTime();
					timers.add(zi.opnum, 0.000001 * (t2 - t1));
				} catch (ZMachineInterrupted zi) {
					// the current instruction did not complete, so rerun it
					// (this only affects input instructions, should be safe)
					pc = oldpc;
				}
				break;
			case FINISHED:
				// happens on a normal op_quit cycle
				return;
			case PAUSED:
				// Should not happen. Recoverable bug.
				Log.e(TAG, "State should never go PAUSED -> PAUSED");
				break;
			default:
				throw new ZMachineException(pc, "Unexpected state: " + s);
			}
		}
	}

	private void runZM()
	{
		Log.i(TAG, "runZM() starting");
		// Track timing for each opcode named above
		String[] opnames = getOpnames();
		ProfileStats timers = new ProfileStats(opnames.length);
		try {
			mainLoop(timers);
			timers.dump("opcode", opnames);
			Log.i(TAG, "runZM() finishing without error");
			screen.onZmFinished(null);
		}
		catch (ZMachineException e) {
			changeState(InterruptState.FINISHED);
			Log.e(TAG, "runZM() finishing with ZM exception", e);
			screen.onZmFinished(e);
		}
		catch (RuntimeException e) {
			changeState(InterruptState.FINISHED);
			Log.e(TAG, "runZM() finishing with runtime exception", e);
			screen.onZmFinished(new ZMachineException(pc, e));
		}
	}

	public synchronized void abort() {
		changeState(InterruptState.ABORTING);
		thread.interrupt();  // cancel key input
		pc = -1;
	}

	public synchronized void quit() {
		changeState(InterruptState.FINISHED);
	}

	public synchronized boolean pauseZM() {
		if (runState != InterruptState.RUNNING)
			return false;
		changeState(InterruptState.PAUSING);
		thread.interrupt();  // cancel key input
		// Block until the thread picks up the change
		return (InterruptState.PAUSED == waitForStateChange(InterruptState.PAUSING));
	}

	public synchronized boolean resumeZM() {
		if (runState != InterruptState.PAUSED)
			return false;
		changeState(InterruptState.RESUMING);
		// Block until the thread picks up the change
		return (InterruptState.RUNNING == waitForStateChange(InterruptState.RESUMING));
	}

	void calculate_checksum() {
		int filesize = header.file_length();
		int i;

		checksum = 0;
		if (filesize <= memory_image.length) {
			for (i = 0x40; i < filesize; i++) {
				checksum += memory_image[i]&0xFF;
			}
		}
	}

	public void restart() {
		restart_state.header.set_transcripting(header.transcripting());
		restart_state.restore_saved();
		set_header_flags();
		pc = header.initial_pc();
		calculate_checksum();
	}

	public void restore(ZState zs) {
		zs.header.set_transcripting(header.transcripting());
		restart();
		zs.restore_saved();
	}

	public void set_header_flags() { /* at start, restart, restore */
		header.set_revision(0,2);
	}

	public void fatal(String s) {
		throw new ZMachineException(pc, s);
	}

	public short get_variable(short varnum) {
		short result;

		varnum &= 0xFF;
		if (varnum == 0) { /* stack */
			try {
				result = (short)(((Integer)zstack.pop()).intValue() & 0xFFFF);
			}
			catch (EmptyStackException booga) {
				fatal("Empty Stack");
				result = -1; /* not reached */
			}
		}
		else if (varnum >= 0x10) { /* globals */
			result = (short)(((memory_image[globals+((varnum - 0x10)<<1)]<<8)&0xFF00) |
					(memory_image[globals + ((varnum - 0x10)<<1) + 1] & 0xFF));
			//	if (varnum == 0xa1)
			//		System.err.println("Got global # " + Integer.toString(varnum-0x10, 16) +
			//			" = " + Integer.toString(result, 16));
		}
		else { /* locals */
			result = locals[varnum-1];
		}
		return result;
	}

	public void set_variable(short varnum, short value) {

		varnum &= 0xFF;
		if (varnum == 0) { /* stack */
			zstack.push(new Integer(value));
		}
		else if (varnum >= 0x10) { /* globals */
			memory_image[globals + ((varnum - 0x10)<<1)] = (byte)(value >>> 8);
			memory_image[globals + ((varnum - 0x10)<<1) + 1] = (byte)(value & 0xFF);
		}
		else { /* locals */
			locals[varnum-1] = value;
		}
	}

	public byte get_code_byte() {
		return memory_image[pc++];
	}

	public short get_operand(int optype) {
		switch (optype) {
		case OP_SMALL:
			return (short)(get_code_byte() & 0xFF);
		case OP_LARGE:
			return (short)(((get_code_byte() << 8)&0xFF00)
					| (get_code_byte() & 0xFF));
		case OP_VARIABLE:
			return get_variable(get_code_byte());
		}
		/* crash */
		fatal("Invalid operand type " + optype);
		return -1;
	}

	static class ProfileStats {
		double[] count;
		double[] sum_val;
		double[] sum_val2;
		final int limit;

		ProfileStats(int limit) {
			this.limit = limit;
			count = new double[limit + 1];
			sum_val = new double[limit + 1];
			sum_val2 = new double[limit + 1];
		}

		public void add(int stat, double val) {
			if (stat < 0 || stat >= limit)
				stat = limit;
			count[stat] += 1;
			sum_val[stat] += val;
			sum_val2[stat] += val * val;
		}

		public void dump(String label, String[] names) {
			StringBuilder sb = new StringBuilder(",");
			sb.append(label);
			sb.append(",");
			int start = sb.length();
			sb.append("item,name,count,sum,mean,sd");
			// This format makes it easy to pull data out of 'adb logcat'
			// and load into a spreadsheet as CSV data
			for (int i = 0; i <= limit; i++) {
				if (count[i] == 0)
					continue;
				sb.delete(start, sb.length());
				if (i < limit)
					sb.append(i);
				else
					sb.append("other");
				sb.append(",");
				if (names != null)
					sb.append(names[i]);
				sb.append(",");
				sb.append(count[i]);
				sb.append(",");
				sb.append(sum_val[i]);
				sb.append(",");
				sb.append(mean(sum_val[i], count[i]));
				sb.append(",");
				sb.append(sd(sum_val2[i], sum_val[i], count[i]));
				Log.i("ProfileStats", sb.toString());
			}
		}

		static double mean(double sum, double count) {
			if (count <= 0.0)
				return 0.0;
			return sum / count;
		}

		static double sd(double sum2, double sum, double count) {
			if (count <= 0.0)
				return 0.0;
			return Math.sqrt((sum * sum) - sum2) / (count * count);
		}
	}

	public class CircularList<T extends Object> {
		private static final long serialVersionUID = -4894945721696387127L;
		private final int limit;
		private final LinkedList<T> contents;

		CircularList(int limit) {
			this.limit = limit;
			contents = new LinkedList<T>();
		}

		public void add(T object) {
			contents.addLast(object);
			while (contents.size() > limit)
				contents.removeFirst();
		}
		
		public T[] toArray(T[] t) {
			return contents.toArray(t);
		}
		
		public void clear() {
			contents.clear();
		}
	}

	public void logPrintedString() {
		if (!zmLog)
			return;
		StringBuffer sb = new StringBuffer(Integer.toHexString(pc))
		.append("# ");
		if (zmLogString.length() > 80) {
			// Trim middle so whole length is 80
			zmLogString.replace(37, zmLogString.length() - 37, " .... ");
		}
		sb.append(zmLogString);
		zmLogString.setLength(0);
		String s = sb.toString();
		zmLogEntries.add(s);
		// Log.v(ZMLOG, s);
	}

	public void logInstruction(ZInstruction zi) {
		if (!zmLog)
			return;
		String opname = getOpnames()[zi.opnum];
		StringBuffer sb = new StringBuffer();
		for (int i = zstack.size(); i > 0; --i)
			sb.append(' ');
		sb.append(Integer.toHexString(pc))
		.append(": ")
		.append(opname);
		switch(zi.opnum) {
		case ZInstruction.OP_PRINT_CHAR:
			if (zi.operands[0] >= 32 && zi.operands[0] <= 127) {
				sb.append(" \'")
				.append((char) zi.operands[0])
				.append('\'');
				break;
			}
			// ** fall through **
		default:
			for (int i = 0; i < zi.count; i++) {
				int o = zi.operands[i];
				if (o < 0) {
					sb.append(" -")
					.append(Integer.toHexString(-o));
				} else {
					sb.append(' ')
					.append(Integer.toHexString(o));
				}
			}
			if (zi.isbranch()) {
				int o = zi.branchoffset;
				if (o < 0) {
					sb.append(" b=-")
					.append(Integer.toHexString(-o));
				} else {
					sb.append(" b=")
					.append(Integer.toHexString(o));
				}
			}
			if (zi.isstore()) {
				int o = zi.storevar;
				if (o < 0) {
					sb.append(" s=-")
					.append(Integer.toHexString(-o));
				} else {
					sb.append(" s=")
					.append(Integer.toHexString(o));
				}
			}
		}
		String s = sb.toString();
		zmLogEntries.add(s);
		// Log.v(ZMLOG, s);
	}

	/**
	 * Is the zmachine in a state where it could execute instructions soon?
	 */
	public synchronized boolean isRunning() {
		switch (runState) {
		case ABORTING:
		case FINISHED:
			return false;
		default:
			return true;
		}
	}
}
