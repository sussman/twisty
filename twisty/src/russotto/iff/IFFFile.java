/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */

package russotto.iff;
import java.io.*;
import java.util.*;

import com.google.twisty.io.Seekable;
import com.google.twisty.io.SeekableFactory;

public class IFFFile
{
	protected Stack<Long> openchunks;
	protected final RandomAccessFile file;
	protected final Seekable seeker;

	public IFFFile(String name, String mode) throws IOException
	{
		file = new RandomAccessFile(name, mode);
		seeker = SeekableFactory.fromRandomAccessFile(file);
		openchunks = new Stack<Long>();
	}

	public IFFFile(File file, String mode) throws IOException
	{
		this.file = new RandomAccessFile(file, mode);
		seeker = SeekableFactory.fromRandomAccessFile(this.file);
		openchunks = new Stack<Long>();
	}
	
	public IFFFile(Seekable seeker) {
		file = null;
		this.seeker = seeker;
		openchunks = new Stack<Long>();
	}

	public void chunkSeek(int offset) throws IOException
	{
		seek((openchunks.peek()).longValue() + 4 + offset);
	}

	public int getChunkPointer() throws IOException
	{
		return (int)getFilePointer() - (int)(openchunks.peek()).longValue() - 4;
	}

	public void close() throws IOException {
		if (file != null)
			file.close();
	}

	public long getFilePointer() throws IOException {
		return seeker.getFilePointer();
	}

	public void seek(long pos) throws IOException {
		seeker.seek(pos);
	}
}

