package com.google.twisty.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SeekableByteArrayInputStream extends ByteArrayInputStream
implements Seekable {

	public SeekableByteArrayInputStream(byte[] buf) {
		super(buf);
	}

	public SeekableByteArrayInputStream(byte[] buf, int offset, int length) {
		super(buf, offset, length);
	}

	public synchronized long getFilePointer() throws IOException {
		return pos;
	}

	public synchronized void seek(long where) throws IOException {
		if (pos > buf.length) {
			throw new IllegalArgumentException(
					"Cannot seek past end of data: seek=" + where +
					", pos=" + pos + " of " + buf.length);
		}
		pos = (int) where;
	}

}
