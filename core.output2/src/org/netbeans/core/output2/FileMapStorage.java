/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core.output2;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * An implementation of the Storage interface over a memory mapped file.
 *
 */
class FileMapStorage implements Storage {
    /** A file channel for writing the mapped file */
    protected FileChannel writeChannel;
    /** A file channel for reading the mapped file */
    private FileChannel readChannel;
    /** The base number of bytes to allocate when a getWriteBuffer for writing is
     * needed. */
    private static final int BASE_BUFFER_SIZE = 8196;
    /**
     * The byte getWriteBuffer that write operations write into.  Actual buffers are
     * provided for writing by calling master.slice(); this getWriteBuffer simply
     * pre-allocates a fairly large chunk of memory to reduce repeated
     * allocations.
     */
    private ByteBuffer master;
    /** A byte getWriteBuffer mapped to the contents of the output file, from which
     * content is read. */
    private ByteBuffer contents;
    /** The number of bytes from the file that have been are currently mapped
     * into the contents ByteBuffer.  This will be checked on calls that read,
     * and if more than the currently mapped bytes are requested, the
     * contents bufffer will be replaced by a larger one */
    private long mappedRange;
    /**
     * The currently in use buffer.
     */
    protected ByteBuffer buffer = null;
    /**
     * The number of bytes that have been written.
     */
    protected int bytesWritten = 0;
    /**
     * The file we are writing to.
     */
    private File outfile = null;

    public FileMapStorage() {
        init();
    }

    protected void init() {
        contents = null;
        mappedRange = -1;
        master = ByteBuffer.allocateDirect (BASE_BUFFER_SIZE);
        readChannel = null;
        writeChannel = null;
        buffer = null;
        bytesWritten = 0;
    }

    /**
     * Ensure that the output file exists.
     */
    private void ensureFileExists() throws IOException {
        if (outfile == null) {
            String outdir = System.getProperty("java.io.tmpdir"); //NOI18N
            if (!outdir.endsWith(File.separator)) {
                outdir += File.separator;
            }
            File dir = new File (outdir);
            if (!dir.exists() || !dir.canWrite()) {
                //Handle the (unlikely) case we cannot write to the system
                //temporary directory
                IllegalStateException ise = new IllegalStateException ("Cannot" + //NOI18N
                " write to " + outdir); //NOI18N
                ErrorManager.getDefault().annotate (ise,
                    NbBundle.getMessage (OutWriter.class,
                    "FMT_CannotWrite", //NOI18N
                    outdir));
                throw ise;
            }
            String fname = outdir + "output" + Long.toString(System.currentTimeMillis()); //NOI18N
            outfile = new File (fname);
            while (outfile.exists()) {
                fname += "1";
                outfile = new File(fname);
            }
            outfile.createNewFile();
            outfile.deleteOnExit();
        }
    }

    /**
     * Get the output file, creating it if necessary.
     */
    File getFile() throws IOException {
        ensureFileExists();
        return outfile;
    }

    /**
     * Get a FileChannel opened for writing against the output file.
     */
    private FileChannel writeChannel() {
        try {
            if (writeChannel == null) {
                ensureFileExists();
                FileOutputStream fos = new FileOutputStream(outfile, true);
                writeChannel = fos.getChannel();
            }
            return writeChannel;
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace(); //XXX
        } catch (IOException ioe) {
            ioe.printStackTrace(); //XXX
        }
        return null;
    }

    /**
     * Fetch a FileChannel for readin the file.
     */
    private FileChannel readChannel() {
        //TODO may be better to use RandomAccessFile and a single bidirectional
        //FileChannel rather than maintaining two separate ones.
        if (readChannel == null) {
            try {
                ensureFileExists();
                FileInputStream fis = new FileInputStream (outfile);
                readChannel = fis.getChannel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return readChannel;
    }

    /**
     * Fetch a getWriteBuffer of a specified size to use for appending new data to the
     * end of the file.
     */
    public synchronized ByteBuffer getWriteBuffer (int size) throws IOException {
        if (master.capacity() - master.position() < size) {
            int newSize = Math.max (BASE_BUFFER_SIZE * 2, 
                size + BASE_BUFFER_SIZE);
            
            master = ByteBuffer.allocateDirect (newSize);
        }

        if (buffer == null) {
            buffer = master.slice();
        } else {
            int charsRemaining = OutWriter.toCharIndex(buffer.capacity() - buffer.position());

            if (charsRemaining < size) {
                buffer.flip();
                buffer = master.slice();
            }
        }
        outstandingBufferCount++;
        return buffer;
    }
    private int outstandingBufferCount = 0;

    /**
     * Dispose of a ByteBuffer which has been acquired for writing by one of
     * the write methods, writing its contents to the file.
     */
    public int write (ByteBuffer bb) throws IOException {
        if (bb == buffer) {
            buffer = null;
        }
        int position = size();
        int byteCount = bb.position();
        bb.flip();
        writeChannel().write (bb);
        writeChannel().write(ByteBuffer.wrap(OutWriter.lineSepBytes));
        synchronized (this) {
            bytesWritten += byteCount + OutWriter.lineSepBytes.length;
            outstandingBufferCount--;
        }
        return position;
    }

    public synchronized void dispose() {
        if (writeChannel != null && writeChannel.isOpen()) {
            try {
                writeChannel.close();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        if (readChannel != null && readChannel.isOpen()) {
            try {
                readChannel.close();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        if (outfile != null && outfile.exists()) {
            try {
                outfile.delete();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        outfile = null;
        readChannel = null;
        buffer = null;
        contents = null;
    }

    /**
     * Get a byte buffer representing the a subrange of the contents of the
     * output file.  This is optimized to possibly map more of the output file
     * into memory if it is not already mapped.
     */
    public ByteBuffer getReadBuffer(int start, int byteCount) throws IOException {
        ByteBuffer contents = null;
        synchronized (this) {
            //XXX Some optimizations possible here:
            // - Don't map the entire file, just what is requested (perhaps if the mapped
            //    start - currentlyMappedStart > someThreshold
            // - Use RandomAccessFile and use one buffer for reading and writing (this may
            //    cause contention problems blocking repaints)
            contents = this.contents;
            if (contents == null || start + byteCount > mappedRange) {
                FileChannel ch = readChannel();
                long prevMappedRange = mappedRange;
                mappedRange = ch.size();
                try {
                    try {
                        contents = ch.position(0).map(FileChannel.MapMode.READ_ONLY,
                            0, mappedRange);
                        this.contents = contents;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        ErrorManager.getDefault().log("Failed to memory map output file for " + //NOI18N
                                "reading.  Trying to read it normally."); //NOI18N
                        //If a lot of processes have crashed with mapped files (generally when testing),
                        //this exception may simply be that the memory cannot be allocated for mapping.
                        //Try to do it non-mapped
                        contents = ByteBuffer.allocate((int) mappedRange);
                        ch.position(0).read(contents);
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault().log("Failed to read output file. Start:" + start + " bytes reqd=" + //NOI18N
                        byteCount + " mapped range=" + mappedRange + //NOI18N
                        " previous mapped range=" + prevMappedRange + //NOI18N
                        " channel size: " + ch.size()); //NOI18N
                    throw ioe;
                }
            }
            contents.position (start);
        }
        int limit = Math.min(contents.limit(), byteCount);
        try {
            return (ByteBuffer) contents.slice().limit(limit);
        } catch (Exception e) {
            throw new IllegalStateException ("Error setting limit to " + limit //NOI18N
            + " contents size = " + contents.limit() + " requested: read " + //NOI18N
            "buffer from " + start + " to be " + byteCount + " bytes"); //NOI18N
        }
    }

    public synchronized int size() {
        return bytesWritten;
    }

    public void flush() throws IOException {
        if (buffer != null) {
            write (buffer);
            writeChannel.force(false);
            buffer = null;
        }
    }

    public void close() throws IOException {
        if (writeChannel != null) {
            flush();
            writeChannel.close();
            writeChannel = null;
            if (Controller.log) Controller.log("FileMapStorage closed.  Outstanding buffer count: " + outstandingBufferCount);
        }
    }

    public boolean isClosed() {
        return writeChannel == null;
    }
}
