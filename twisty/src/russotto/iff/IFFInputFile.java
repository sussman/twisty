/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */

package russotto.iff;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import com.google.twisty.io.SeekableByteArrayInputStream;

public class IFFInputFile
extends IFFFile
{
	private Stack<Long> openchunkends;
	private final DataInput input;
	private final Method reader;
	private final Object readerObject;

	public IFFInputFile(File f) throws IOException
	{
		super(f, "r");
		input = file;
		readerObject = file;
		reader = getReadMethod(readerObject);
		openchunkends = new Stack<Long>();
	}

	public IFFInputFile(String name) throws IOException
	{
		super(name, "r");
		input = file;
		readerObject = file;
		reader = getReadMethod(readerObject);
		openchunkends = new Stack<Long>();
	}

	public IFFInputFile(SeekableByteArrayInputStream bais) throws IOException {
		super(bais);
		input = new DataInputStream(bais);
		readerObject = bais;
		reader = getReadMethod(readerObject);
		openchunkends = new Stack<Long>();
	}

	private static Method getReadMethod(Object o) {
		try {
			return o.getClass().getMethod("read", byte[].class, int.class, int.class);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized IFFChunkInfo readChunkInfo() throws IOException {
		IFFChunkInfo result = new IFFChunkInfo();
		byte chunktype[] = new byte[4];
		long chunkbegin;

		read(chunktype, 0, 4);
		chunkbegin = getFilePointer();
		result.chunktype = new String(chunktype, "US-ASCII");
		result.chunklength = readInt();
		openchunks.push(new Long(chunkbegin));
		openchunkends.push(new Long(getFilePointer() + result.chunklength));

		return result;
	}

	public synchronized IFFChunkInfo skipToChunk(String type) throws IOException, IFFChunkNotFoundException {
		IFFChunkInfo chunkinfo;

		if (getFilePointer() >= (openchunkends.peek()).longValue())
			throw new IFFChunkNotFoundException("Chunk " + type + " not found at current level");
		chunkinfo = readChunkInfo();
		while (!chunkinfo.chunktype.equals(type)) {
			closeChunk();
			if (getFilePointer() >= (openchunkends.peek()).longValue())
				throw new IFFChunkNotFoundException("Chunk " + type + " not found at current level");
			chunkinfo = readChunkInfo();
		}
		return chunkinfo;
	}

	public synchronized String readFORM() throws IOException {
		IFFChunkInfo formchunkinfo;
		byte subtype[] = new byte[4];

		formchunkinfo = readChunkInfo();
		if (formchunkinfo.chunktype.equals("FORM")) {
			read(subtype, 0, 4);
		}
		else {
			// throw new Exception("That's not a FORM!");
		}
		return new String(subtype, "US-ASCII");
	}

	public synchronized void closeChunk() throws IOException {
		long chunkend;

		chunkend = ((openchunkends.pop()).longValue() + 1) & ~1L;
		openchunks.pop();
		// doing the seek last ensures exceptions leave stacks consistent
		seek(chunkend);
	}

	public synchronized void close() throws IOException
	{
		while (!openchunks.empty()) {
			try {
				closeChunk();
			}
			catch(IOException ioexcpt)
			{
				// Ignore seek errors probably caused by opening a bad chunk
			}
		}
		super.close();
	}

	/**
	 * Uses reflection to read from the underlying input. We do it this way
	 * because ByteArrayInputStream and RandomAccessFile share identical
	 * read() methods but not a common interface.
	 */
	public int read(byte[] buffer, int offset, int count) throws IOException {
		try {
			Object r = reader.invoke(readerObject, buffer, offset, count);
			if (r instanceof Integer)
				return ((Integer) r).intValue();
			throw new RuntimeException("Incorrect return type from read()");
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof IOException)
				throw (IOException) e.getCause();
			throw new RuntimeException(e);
		}
	}

	// Selectively wrap DataInput
	public byte readByte() throws IOException { return input.readByte(); }
	public short readShort() throws IOException { return input.readShort(); }
	public int readInt() throws IOException { return input.readInt(); }
}
