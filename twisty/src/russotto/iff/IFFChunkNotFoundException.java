/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */

package russotto.iff;

public
class IFFChunkNotFoundException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5761452412858846702L;

	/**
     * Constructs an IFFChunkNotFoundException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public IFFChunkNotFoundException() {
		super();
    }

    /**
     * Constructs an IFFChunkNotFoundException with the specified detail message.
     * A detail message is a String that describes this particular exception.
     * @param s the detail message
     */
    public IFFChunkNotFoundException(String s) {
		super(s);
    }
}
