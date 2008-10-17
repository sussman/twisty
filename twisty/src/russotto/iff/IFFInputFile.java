/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */

package russotto.iff;
import java.io.*;
import java.util.*;

public class IFFInputFile
extends IFFFile
{
	private Stack<Long> openchunkends;
	private final DataInput input;

	public IFFInputFile(File file) throws IOException
	{
		super(file, "r");
		input = this.file;
		openchunkends = new Stack<Long>();
	}

	public IFFInputFile(String name) throws IOException
	{
		super(name, "r");
		input = this.file;
		openchunkends = new Stack<Long>();
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

	// Selectively wrap DataInput
	public byte readByte() throws IOException { return input.readByte(); }
	public short readShort() throws IOException { return input.readShort(); }
	public int readInt() throws IOException { return input.readInt(); }
}
