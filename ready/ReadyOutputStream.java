package ready;

/**
 * The ReadyOutputStream class is a version of OutputStream that is 
 * used to send all standard output and error messages to a C program
 * that displays them on a GUI console.
 * <p>
 * @author Tom West
 * @version 1.0 2000/06/06
 */

import java.io.*;

class ReadyOutputStream extends OutputStream
{
    static
    {
	System.loadLibrary ("jsdll");
    }
    
    private native void writeBytes (byte b[], int off, int len) 
	throws IOException;
    
    /**
     * Writes the specified byte to this output stream. 
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an 
     * implementation for this method. 
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public native void write(int b) throws IOException;

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this output stream. 
     * <p>
     * The <code>write</code> method of <code>OutputStream</code> calls 
     * the write method of one argument on each of the bytes to be 
     * written out. Subclasses are encouraged to override this method and 
     * provide a more efficient implementation. 
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public void write(byte b[], int off, int len) throws IOException {
	writeBytes (b, off, len);
    }
} // ReadyInputStream class
