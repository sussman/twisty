/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */

package russotto.iff;
import java.io.*;

import com.google.twisty.io.SeekableByteArrayOutputStream;

public class IFFOutputFile
extends IFFFile
{
	private final DataOutput output;

	public IFFOutputFile(File file) throws IOException
	{
		super(file, "rw");
		output = this.file;
	}

	public IFFOutputFile(File file, String type) throws IOException
	{
		this(file);
		openChunk("FORM");
		write(getOSType(type), 0, 4);
	}

	public IFFOutputFile(String name) throws IOException
	{
		super(name, "rw");
		output = this.file;
	}

	public IFFOutputFile(String name, String type) throws IOException
	{
		this(name);
		openChunk("FORM");
		write(getOSType(type), 0, 4);
	}

	public IFFOutputFile(SeekableByteArrayOutputStream baos, String type) throws IOException {
		super(baos);
		output = new DataOutputStream(baos);
		openChunk("FORM");
		write(getOSType(type), 0, 4);
	}

	private byte[] getOSType(String s)
	{
		byte[] bytes = s.getBytes();
		if (bytes.length == 4)
			return bytes;
		byte result[] = new byte[4];
		System.arraycopy(bytes, 0, result, 0, 4);
		return result;
	}

	public synchronized void openChunk(String type) throws IOException
	{
		write(getOSType(type), 0, 4);
		openchunks.push(new Long(getFilePointer()));
		writeInt(0);
	}

	public synchronized void closeChunk() throws IOException
	{
		long location, currentlocation;
		int chunklength;

		currentlocation = getFilePointer();
		chunklength = getChunkPointer();
		location = ((Long)openchunks.pop()).longValue();
		seek(location);
		writeInt(chunklength);
		seek(currentlocation);
		if ((chunklength & 1) == 1) {
			writeByte(0);
		}
	}

	public synchronized void close() throws IOException
	{
		while (!openchunks.empty())
			closeChunk();
		super.close();
	}

	public void writeByte(int val) throws IOException { output.writeByte(val); }
	public void writeShort(int val) throws IOException { output.writeShort(val); }
	public void writeInt(int val) throws IOException { output.writeInt(val); }
	public void write(byte[] buffer) throws IOException { output.write(buffer); }
	public void write(byte[] buffer, int offset, int count) throws IOException {
		output.write(buffer, offset, count);
	}
}
