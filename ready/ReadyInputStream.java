package ready;

/**
 * The ReadyInputStream class is a version of InputStream that is 
 * used to read all keyboard input from a C file using native
 * methods.  The C file then reads a line from a GUI console.
 * <p>
 * @author Tom West
 * @version 1.0 2000/06/01
 */

import java.io.*;

class ReadyInputStream extends InputStream
{
    static
    {
	System.loadLibrary ("jsdll");
    }
    
    public native int readBytes (byte b[], int off, int len) throws IOException;
    
    /**
     * Returns the next character read in.  If at the end of the line and
     * there is no input waiting, it blocks.  Newlines are returned as a
     * byte value of 10.
     * <P>
     * If the user hits Ctrl+D/Ctrl+Z, this throws and EOFException.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public native int read() throws IOException;
    
    /**
     * Returns the next line of input.  If at the end of the line and
     * there is no input waiting, it blocks.  Newlines are returned at
     * the end of the buffer as a byte value of 10.
     * <P>
     * If the user hits Ctrl+D/Ctrl+Z, this throws and EOFException.
     * <P>
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.InputStream#read()
     */
    public int read(byte b[], int off, int len) throws IOException {
	return readBytes (b, off, len);
    }
} // ReadyInputStream class
