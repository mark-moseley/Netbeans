/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * Storage.java
 *
 * Created on May 14, 2004, 1:40 PM
 */

package org.netbeans.core.output2;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Storage abstraction for output writer - plan is to eventually do both
 * heap-based and mapped file based storages.
 *
 * @author  Tim Boudreau, Jesse Glick
 */
interface Storage {
    /**
     * Get a ByteBuffer for reading over the storage, starting at the specified byte position and containing the
     * specified number of bytes
     *
     * @param start The start byte
     * @param length How many bytes
     * @return A byte buffer
     * @throws IOException if there is a problem reading or allocating the buffer
     */
    public ByteBuffer getReadBuffer (int start, int length) throws IOException;
    /**
     * Get a buffer for <strong>appending</strong> <code>length</code> bytes to the stored data.  Note that
     * writing into the returned buffer does not automatically write to the file - the returned buffer should
     * be passed to <code>write(ByteBuffer b)</code> to be saved once it has been filled.
     *
     * @param length The number of bytes to write
     * @return
     * @throws IOException
     */
    public ByteBuffer getWriteBuffer (int length) throws IOException;
    /**
     * Write a ByteBuffer (presumably obtained from getWriteBuffer()) to persistent storage.  The buffer
     * may be underfilled; data will be written to the <code>limit</code> of the ByteBuffer, disregarding any
     * additional capacity.
     *
     * @param buf A ByteBuffer with data to write
     * @param addNewLine true if the storage should append a newline after writing the buffer
     * @return The byte position of the <strong>start</strong> of data written
     * @throws IOException if there is a problem writing the data
     */
    public int write (ByteBuffer buf, boolean addNewLine) throws IOException;

    /**
     * Dispose of this storage, deleting all associated resources, files, data storage, etc.  This should only
     * be called after it is absolutely certain that nothing will try to write further data to the storage.
     *
     */
    public void dispose ();

    /**
     * The number of bytes currently occupied by written data
     *
     * @return A byte count
     */
    public int size();

    /**
     * For storages that implement a lazy writing scheme, force any pending data to be written to the storage.
     *
     * @throws IOException If there is a problem writing the data
     */
    public void flush() throws IOException;

    /**
     * Close the storage for <strong>writing</strong> disposing of any resources used for writing to the storage,
     * but leaving it in a state where it may be read, calling <code>flush()</code> if necessary.  Subsequent calls
     * to write methods may reopen the storage for writing as needed.
     *
     * @throws IOException If there is a problem writing to the persistent storage
     */
    public void close() throws IOException;

    /**
     * Determine if close() has been called on this storage.  Primarily used for cases where the gui should
     * display some status information if a process is still writing to the storage.
     *
     * @return true if the storage has been closed
     */
    public boolean isClosed();
}
