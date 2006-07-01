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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownServiceException;
import java.security.Permission;

/** Special URL connection directly accessing an internal file object.
*
* @author Ales Novak, Petr Hamernik, Jan Jancura, Jaroslav Tulach
*/
final class FileURL extends URLConnection {
    /** Protocol name for this type of URL. */
    public static final String PROTOCOL = "nbfs"; // NOI18N

    /** Default implemenatation of handler for this type of URL.
     */
    static URLStreamHandler HANDLER = new URLStreamHandler() {
            /**
            * @param u - URL to open connection to.
            * @return new URLConnection.
            */
            public URLConnection openConnection(URL u)
            throws IOException {
                return new FileURL(u);
            }

            protected synchronized InetAddress getHostAddress(URL u) {
                return null;
            }
        };

    /** 1 URLConnection == 1 InputSteam*/
    InputStream iStream = null;

    /** 1 URLConnection == 1 OutputSteam*/
    OutputStream oStream = null;

    /** FileObject that we want to connect to. */
    private FileObject fo;

    /**
    * Create a new connection to a {@link FileObject}.
    * @param u URL of the connection. Please use {@link #encodeFileObject(FileObject)} to create the URL.
    */
    private FileURL(URL u) {
        super(u);
    }

    /** Provides a URL to access a file object.
    * @param fo the file object
    * @return a URL using the correct syntax and {@link #PROTOCOL protocol}
    * @exception FileStateInvalidException if the file object is not valid (typically, if its filesystem is inconsistent or no longer present)
    */
    public static URL encodeFileObject(FileObject fo) throws FileStateInvalidException {
        return NbfsUtil.getURL(fo);
    }

    /** Retrieves the file object specified by an internal URL.
    * @param u the url to decode
    * @return the file object that is represented by the URL, or <code>null</code> if the URL is somehow invalid or the file does not exist
    */
    public static FileObject decodeURL(URL u) {
        return NbfsUtil.getFileObject(u);
    }

    /* A method for connecting to a FileObject.
    */
    public void connect() throws IOException {
        if (fo != null) {
            return;
        }

        fo = decodeURL(url);

        if (fo == null) {
            throw new FileNotFoundException("Cannot find: " + url); // NOI18N
        }
    }

    /*
    * @return InputStream or given FileObject.
    */
    public InputStream getInputStream() throws IOException, UnknownServiceException {
        connect();

        if (iStream == null) {
            try {
                if (fo.isFolder()) {
                    iStream = new FIS(fo);
                } else {
                    iStream = fo.getInputStream();
                }
            } catch (FileNotFoundException e) {
                ExternalUtil.exception(e);
                throw e;
            }
        }

        return iStream;
    }

    /*
    * @return OutputStream for given FileObject.
    */
    public OutputStream getOutputStream() throws IOException, UnknownServiceException {
        connect();

        if (fo.isFolder()) {
            throw new UnknownServiceException();
        }

        if (oStream == null) {
            FileLock flock = fo.lock();
            oStream = new LockOS(fo.getOutputStream(flock), flock);
        }

        return oStream;
    }

    /*
    * @return length of FileObject.
    */
    public int getContentLength() {
        try {
            connect();

            return (int) fo.getSize();
        } catch (IOException ex) {
            return 0;
        }
    }

    /** Get a header field (currently, content type only).
    * @param name the header name. Only <code>content-type</code> is guaranteed to be present.
    * @return the value (i.e., MIME type)
    */
    public String getHeaderField(String name) {
        if (name.equalsIgnoreCase("content-type")) { // NOI18N

            try {
                connect();

                if (fo.isFolder()) {
                    return "text/html"; // NOI18N
                } else {
                    return fo.getMIMEType();
                }
            } catch (IOException e) {
            }
        }

        return super.getHeaderField(name);
    }

    // #13038: URLClassPath is going to check this.
    // Better not return AllPermission!
    // SocketPermission on localhost might also work.
    public Permission getPermission() throws IOException {
        // Note this is normally called by URLClassPath with an unconnected
        // URLConnection, so the fo will probably be null anyway.
        if (fo != null) {
            File f = FileUtil.toFile(fo);

            if (f != null) {
                return new FilePermission(f.getAbsolutePath(), "read"); // NOI18N
            }

            try {
                FileSystem fs = fo.getFileSystem();

                if (fs instanceof JarFileSystem) {
                    return new FilePermission(((JarFileSystem) fs).getJarFile().getAbsolutePath(), "read"); // NOI18N
                }

                // [PENDING] could do XMLFileSystem too...
            } catch (FileStateInvalidException fsie) {
                // ignore
            }
        }

        // fallback
        return new FilePermission("<<ALL FILES>>", "read"); // NOI18N
    }

    /** Stream that also closes the lock, if closed.
     */
    private static class LockOS extends java.io.BufferedOutputStream {
        /** lock */
        private FileLock flock;

        /**
        * @param os is an OutputStream for writing in
        * @param lock is a lock for the stream
        */
        public LockOS(OutputStream os, FileLock lock) throws IOException {
            super(os);
            flock = lock;
        }

        /** overriden */
        public void close() throws IOException {
            flock.releaseLock();
            super.close();
        }
    }

    /** The class allows reading of folder via URL. Because of html
    * oriented user interface the document has html format.
    *
    * @author Ales Novak
    * @version 0.10 May 15, 1998
    */
    private static final class FIS extends InputStream {
        /** delegated reader that reads the document */
        private StringReader reader;

        /**
        * @param folder is a folder
        */
        public FIS(FileObject folder) throws IOException {
            reader = new StringReader(createDocument(folder));
        }

        /** creates html document as string */
        private String createDocument(FileObject folder)
        throws IOException {
            StringBuffer buff = new StringBuffer(150);
            StringBuffer lit = new StringBuffer(15);
            FileObject[] fobia = folder.getChildren();
            String name;

            buff.append("<HTML>\n"); // NOI18N
            buff.append("<BODY>\n"); // NOI18N

            FileObject parent = folder.getParent();

            if (parent != null) {
                // lit.setLength(0);
                // lit.append('/').append(parent.getPackageName('/'));
                buff.append("<P>"); // NOI18N
                buff.append("<A HREF=").append("..").append(">").append("..").append("</A>").append("\n"); // NOI18N
                buff.append("</P>"); // NOI18N
            }

            for (int i = 0; i < fobia.length; i++) {
                lit.setLength(0);
                lit.append(fobia[i].getNameExt());
                name = lit.toString();

                if (fobia[i].isFolder()) {
                    lit.append('/'); // NOI18N
                }

                buff.append("<P>"); // NOI18N
                buff.append("<A HREF=").append((Object) lit).append(">").append(name).append("</A>").append("\n"); // NOI18N
                buff.append("</P>"); // NOI18N
            }

            buff.append("</BODY>\n"); // NOI18N
            buff.append("</HTML>\n"); // NOI18N

            return buff.toString();
        }

        //************************************** stream methods **********
        public int read() throws IOException {
            return reader.read();
        }

        public int read(byte[] b, int off, int len) throws IOException {
            char[] ch = new char[len];
            int r = reader.read(ch, 0, len);

            for (int i = 0; i < r; i++)
                b[off + i] = (byte) ch[i];

            return r;
        }

        public long skip(long skip) throws IOException {
            return reader.skip(skip);
        }

        public void close() throws IOException {
            reader.close();
        }

        public void reset() throws IOException {
            reader.reset();
        }

        public boolean markSupported() {
            return false;
        }
    }
     // end of FIS
}
