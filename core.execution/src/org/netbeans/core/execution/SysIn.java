/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.execution;

import java.io.IOException;
import java.io.InputStream;

/** demutiplexes in-requests to task specific window
*
* @author Ales Novak
* @version 0.10 Dec 04, 1997
*/
final class SysIn extends InputStream {

    public SysIn() {
    }

    /** reads one char */
    public int read() throws IOException {
        return ExecutionEngine.getTaskIOs().getIn().read ();
    }

    /** reads an array of bytes */
    public int read(byte[] b, int off, final int len) throws IOException {
        char[] b2 = new char[len];
        int ret = ExecutionEngine.getTaskIOs().getIn().read(b2, 0, len);
        for (int i = 0; i < len; i++) {
            b[off + i] = (byte) b2[i];
        }
        return ret;
    }

    /** closes the stream */
    public void close() throws IOException {
        ExecutionEngine.getTaskIOs().getIn().close();
    }

    /** marks position at position <code>x</code> */
    public void mark(int x) {
        try {
            ExecutionEngine.getTaskIOs().getIn().mark(x);
        } catch (IOException e) {
            // [TODO]
        }
    }

    /** resets the stream */
    public void reset() throws IOException {
        ExecutionEngine.getTaskIOs().getIn().reset();
    }

    /**
    * @return true iff mark is supported false otherwise
    */
    public boolean markSupported() {
        return ExecutionEngine.getTaskIOs().getIn().markSupported();
    }

    /** skips <code>l</code> bytes
    * @return number of skipped bytes
    */
    public long skip(long l) throws IOException {
        return ExecutionEngine.getTaskIOs().getIn().skip(l);
    }
}
