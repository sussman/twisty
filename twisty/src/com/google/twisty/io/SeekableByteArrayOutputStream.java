package com.google.twisty.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Trivial wrapper around java.io.ByteArrayOutputStream that allows for seeking
 * @author Marius Milner
 *
 */
public class SeekableByteArrayOutputStream extends ByteArrayOutputStream
implements Seekable {
	protected int maxCount;
	
	public SeekableByteArrayOutputStream() {
		super();
	}

	public SeekableByteArrayOutputStream(int size) {
		super(size);
	}

	@Override
	public synchronized void reset() {
		super.reset();
		maxCount = 0;
	}

	@Override
	public int size() {
		if (count > maxCount)
			return count;
		else
			return maxCount;
	}

	@Override
	public synchronized byte[] toByteArray() {
		// Temporarily swap in maxCount so super method isn't confused
		int c = count;
		if (count > maxCount)
			maxCount = count;
		else
			count = maxCount;
		byte[] ba = super.toByteArray();
		count = c;
		return ba;
	}

	public synchronized long getFilePointer() throws IOException {
		return count;
	}

	public synchronized void seek(long pos) throws IOException {
		if (count > maxCount)
			maxCount = count;
		if (pos > maxCount) {
			throw new IllegalArgumentException(
					"Cannot seek past end of written data: seek=" + pos +
					", pos=" + pos + " of " + maxCount);
		}
		count = (int) pos;
	}

}
