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
package org.openide.filesystems;

import org.openide.util.*;

import java.io.*;

import java.util.*;


/**
 * This class keeps info about streams (these streams are registered) that was
 * not closed yet. Also for every issued stream is hold stracktrace.
 * Sometimes there is necessary to know who didn`t close stream.
 *
 * @author  rmatous
 */
final class StreamPool extends Object {
    /** Whether to keep the stack traces. By default, don't - too expensive. */
    private static final boolean ANNOTATE_UNCLOSED_STREAMS = Boolean.getBoolean(
            "org.openide.filesystems.annotateUnclosedStreams"
        ); // NOI18N
    private static Map<FileObject, StreamPool> fo2StreamPool = new WeakHashMap<FileObject, StreamPool>();
    private static Map<FileSystem, StreamPool> fs2StreamPool = new WeakHashMap<FileSystem, StreamPool>();
    private Set<InputStream> iStreams;
    private Set<OutputStream> oStreams;

    /** Creates new StreamPool */
    private StreamPool() {
    }

    /**
     * This method creates subclassed  NotifyInputStream (extends InputStream).
     * NotifyInputStream saves stacktrace in constrcuctor (creates new Exception) that
     * is used in method annotate.
     * This method also register this NotifyInputStream as
     * mapping (AbstractFolder, NotifyInputStream) and
     * mapping (AbstractFolder.getFileSystem(), NotifyInputStream).
     * If NotifyInputStream is closed then registration is freed.
     * For fo is also created StreamPool unless it exists yet.
     * @param fo FileObject that issues is
     * @return subclassed InputStream that is registered as mentioned above */
    public static InputStream createInputStream(final AbstractFolder fo)
    throws FileNotFoundException {
        InputStream retVal = null;

        synchronized (StreamPool.class) {
            try {
                get(fo).waitForOutputStreamsClosed(2000);
                retVal = new NotifyInputStream(fo);
                get(fo).iStream().add(retVal);
                get(fo.getFileSystem()).iStream().add(retVal);
            } catch (InterruptedException e) {
                ExternalUtil.annotate(e, fo.getPath());
                ExternalUtil.exception(e);
            }
        }

        if ((retVal != null) && (retVal instanceof NotifyInputStream)) {
            AbstractFileSystem abstractFileSystem = ((AbstractFileSystem) fo.getFileSystem());
            ((NotifyInputStream) retVal).setOriginal(abstractFileSystem.info.inputStream(fo.getPath()));
        } else {
            retVal = new InputStream() {
                        public int read() throws IOException {
                            FileAlreadyLockedException alreadyLockedEx = new FileAlreadyLockedException(fo.getPath());
                            get(fo).annotate(alreadyLockedEx);
                            throw alreadyLockedEx;
                        }
                    };
        }

        return retVal;
    }

    /** This method creates subclassed  NotifyOutputStream (extends OutputStream).
     * NotifyOutputStream saves stacktrace in constrcuctor (creates new Exception) that
     * is used in method annotate.
     * This method also register this NotifyOutputStream as
     * mapping (AbstractFolder, NotifyOutputStream) and
     * mapping (AbstractFolder.getFileSystem(), NotifyOutputStream).
     * If NotifyOutputStream is closed then registration is freed.
     * For fo is also created StreamPool unless it exists yet.
     * @return subclassed OutputStream that is registered as mentioned above
     * @param fireFileChanged defines if should be fired fileChanged event after close of stream
     * @param fo FileObject that issues is
     * */
    public static OutputStream createOutputStream(final AbstractFolder fo, boolean fireFileChanged)
    throws IOException {
        OutputStream retVal = null;

        synchronized (StreamPool.class) {
            try {
                get(fo).waitForInputStreamsClosed(2000);
                get(fo).waitForOutputStreamsClosed(2000);

                retVal = new NotifyOutputStream(fo, fireFileChanged);
                get(fo).oStream().add(retVal);
                get(fo.getFileSystem()).oStream().add(retVal);
            } catch (InterruptedException e) {
                ExternalUtil.annotate(e, fo.getPath());
                ExternalUtil.exception(e);
            }
        }

        if ((retVal != null) && (retVal instanceof NotifyOutputStream)) {
            AbstractFileSystem abstractFileSystem = ((AbstractFileSystem) fo.getFileSystem());
            ((NotifyOutputStream) retVal).setOriginal(abstractFileSystem.info.outputStream(fo.getPath()));
        } else {
            retVal = new OutputStream() {
                        public void write(int b) throws IOException {
                            FileAlreadyLockedException alreadyLockedEx = new FileAlreadyLockedException(fo.getPath());
                            get(fo).annotate(alreadyLockedEx);
                            throw alreadyLockedEx;
                        }
                    };
        }

        return retVal;
    }

    /**
     * This method finds StreamPool assiciated with fo or null. This StreamPool is
     * created by means of createInputStream or createOutputStream.
     * @param fo FileObject whose StreamPool is looked for
     * @return  StreamPool or null*/
    public static synchronized StreamPool find(FileObject fo) {
        return fo2StreamPool.get(fo);
    }

    /**
     * This method finds StreamPool assiciated with fs or null. This StreamPool is
     * created by means of createInputStream or createOutputStream.
     * @param fs FileSystem whose StreamPool is looked for
     * @return  StreamPool or null*/
    public static synchronized StreamPool find(FileSystem fs) {
        return fs2StreamPool.get(fs);
    }

    /**
     * Annotates ex with all exceptions of unclosed streams.
     * @param ex that should be annotated */
    public void annotate(Exception ex) {
        if (!ANNOTATE_UNCLOSED_STREAMS) {
            return;
        }

        synchronized (StreamPool.class) {
            if (iStreams != null) {
                Iterator itIs = iStreams.iterator();
                NotifyInputStream nis;

                while (itIs.hasNext()) {
                    nis = (NotifyInputStream) itIs.next();

                    Exception annotation = nis.getException();

                    if (annotation != null) {
                        ExternalUtil.annotate(ex, annotation);
                    }
                }
            }

            if (oStreams != null) {
                Iterator itOs = oStreams.iterator();
                NotifyOutputStream nos;

                while (itOs.hasNext()) {
                    nos = (NotifyOutputStream) itOs.next();

                    Exception annotation = nos.getException();

                    if (annotation != null) {
                        ExternalUtil.annotate(ex, annotation);
                    }
                }
            }
        }
    }

    /**
     * @return  true if there is any InputStream that was not closed yet  */
    public boolean isInputStreamOpen() {
        return (iStreams != null) && !iStreams.isEmpty();
    }

    private void waitForInputStreamsClosed(int timeInMs)
    throws InterruptedException {
        synchronized (StreamPool.class) {
            if (isInputStreamOpen()) {
                StreamPool.class.wait(timeInMs);

                if (isInputStreamOpen()) {
                    throw new InterruptedException();
                }
            }
        }
    }

    private void waitForOutputStreamsClosed(int timeInMs)
    throws InterruptedException {
        synchronized (StreamPool.class) {
            if (isOutputStreamOpen()) {
                StreamPool.class.wait(timeInMs);

                if (isOutputStreamOpen()) {
                    throw new InterruptedException();
                }
            }
        }
    }

    /**
     * @return  true if there is any OutputStream that was not closed yet  */
    public boolean isOutputStreamOpen() {
        return (oStreams != null) && !oStreams.isEmpty();
    }

    /** All next methods are private (Not visible outside this class)*/
    private static StreamPool get(FileObject fo) {
        StreamPool strPool = fo2StreamPool.get(fo);

        if (strPool == null) {
            fo2StreamPool.put(fo, strPool = new StreamPool());
        }

        return strPool;
    }

    private static StreamPool get(FileSystem fs) {
        StreamPool strPool = fs2StreamPool.get(fs);

        if (strPool == null) {
            fs2StreamPool.put(fs, strPool = new StreamPool());
        }

        return strPool;
    }

    private Set<InputStream> iStream() {
        if (iStreams == null) {
            iStreams = new WeakSet<InputStream>();
        }

        return iStreams;
    }

    private Set<OutputStream> oStream() {
        if (oStreams == null) {
            oStreams = new WeakSet<OutputStream>();
        }

        return oStreams;
    }

    /** fireFileChange defines if should be fired fileChanged event after close of stream*/
    private static void closeOutputStream(AbstractFolder fo, OutputStream os, boolean fireFileChanged) {
        StreamPool foPool = find(fo);
        StreamPool fsPool = find(fo.getFileSystem());
        Set foSet = (foPool != null) ? foPool.oStreams : null;
        Set fsSet = (fsPool != null) ? fsPool.oStreams : null;

        removeStreams(fsSet, foSet, os);
        removeStreamPools(fsPool, foPool, fo);
        fo.outputStreamClosed(fireFileChanged);
    }

    private static void closeInputStream(AbstractFolder fo, InputStream is) {
        StreamPool foPool = find(fo);
        StreamPool fsPool = find(fo.getFileSystem());
        Set foSet = (foPool != null) ? foPool.iStreams : null;
        Set fsSet = (fsPool != null) ? fsPool.iStreams : null;

        removeStreams(fsSet, foSet, is);
        removeStreamPools(fsPool, foPool, fo);
    }

    private static synchronized void removeStreams(Set fsSet, Set foSet, Object stream) {
        if (foSet != null) {
            foSet.remove(stream);
        }

        if (fsSet != null) {
            fsSet.remove(stream);
        }
    }

    private static synchronized void removeStreamPools(StreamPool fsPool, StreamPool foPool, AbstractFolder fo) {
        boolean isIStreamEmpty = ((foPool == null) || (foPool.iStreams == null) || foPool.iStreams.isEmpty());
        boolean isOStreamEmpty = ((foPool == null) || (foPool.oStreams == null) || foPool.oStreams.isEmpty());

        if (isIStreamEmpty && isOStreamEmpty) {
            fo2StreamPool.remove(fo);
        }

        isIStreamEmpty = ((fsPool == null) || (fsPool.iStreams == null) || fsPool.iStreams.isEmpty());
        isOStreamEmpty = ((fsPool == null) || (fsPool.oStreams == null) || fsPool.oStreams.isEmpty());

        if (isIStreamEmpty && isOStreamEmpty) {
            fs2StreamPool.remove(fo.getFileSystem());
        }
    }

    private static final class NotifyOutputStream extends FilterOutputStream {
        private static final OutputStream emptyOs = new ByteArrayOutputStream();
        private Exception ex;
        private boolean closed = false;
        AbstractFolder fo;

        /** defines if should be fired fileChanged event after close of stream */
        private boolean fireFileChanged;

        public NotifyOutputStream(AbstractFolder fo, boolean fireFileChanged) {
            super(emptyOs);
            this.fo = fo;

            if (ANNOTATE_UNCLOSED_STREAMS) {
                ex = new Exception();
            }

            this.fireFileChanged = fireFileChanged;
        }

        private void setOriginal(OutputStream os) {
            out = os;
        }

        /** Faster implementation of writing than is implemented in
         * the filter output stream.
         */
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        public void close() throws IOException {
            if (!closed) {
                closed = true;
                ex = null;
                super.out.flush();
                super.close();
                closeOutputStream(fo, this, fireFileChanged);

                synchronized (StreamPool.class) {
                    StreamPool.class.notifyAll();
                }
            }
        }

        public Exception getException() {
            return ex;
        }
    }

    private static final class NotifyInputStream extends FilterInputStream {
        private static final InputStream emptyIs = new ByteArrayInputStream(new byte[0]);
        private Exception ex;
        AbstractFolder fo;
        private boolean closed = false;

        public NotifyInputStream(AbstractFolder fo) {
            super(emptyIs);
            this.fo = fo;

            if (ANNOTATE_UNCLOSED_STREAMS) {
                ex = new Exception();
            }
        }

        private void setOriginal(InputStream is) {
            in = is;
        }

        public void close() throws IOException {
            if (!closed) {
                closed = true;
                ex = null;
                super.close();
                closeInputStream(fo, this);

                synchronized (StreamPool.class) {
                    if (!StreamPool.get(fo).isInputStreamOpen()) {
                        StreamPool.class.notifyAll();
                    }
                }
            }
        }

        public Exception getException() {
            return ex;
        }
    }
}
