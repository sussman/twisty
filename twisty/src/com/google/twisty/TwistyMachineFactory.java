package com.google.twisty;

import java.io.IOException;

import org.zmpp.io.IOSystem;
import org.zmpp.io.InputStream;
import org.zmpp.media.Resources;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineFactory;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;

public class TwistyMachineFactory extends MachineFactory<TwistyView> {

	public TwistyMachineFactory(java.io.InputStream storyfile, TwistyView parent) {
		this.storyfile = storyfile;
		this.parent = parent;
	}

	@Override
	protected IOSystem getIOSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected InputStream getKeyboardInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SaveGameDataStore getSaveGameDataStore() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ScreenModel getScreenModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected StatusLine getStatusLine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TwistyView getUI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TwistyView initUI(Machine machine) {
		parent.setupMachine(machine);
		return parent;
	}

	@Override
	protected Resources readResources() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected byte[] readStoryData() throws IOException {
		return suckstream(storyfile);
	}

	@Override
	protected void reportInvalidStory() {
		// TODO Auto-generated method stub
		
	}

    /** Convenience helper that turns a stream into a byte array */
    static byte[] suckstream(java.io.InputStream mystream) throws IOException {
        byte buffer[];
        byte oldbuffer[];
        int currentbytes = 0;
        int bytesleft;
        int got;
        int buffersize = 2048;

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

	final java.io.InputStream storyfile;
	final TwistyView parent;

}
