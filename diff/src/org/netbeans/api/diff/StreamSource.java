/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.diff;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import org.openide.util.io.ReaderInputStream;

/**
 * This class provides streams and information about them to be used by diff
 * and merge services.
 *
 * @author  Martin Entlicher
 */
public abstract class StreamSource extends Object {
    
    /**
     * Get the name of the source.
     */
    public abstract String getName();
    
    /**
     * Get the title of the source.
     */
    public abstract String getTitle();
    
    /**
     * Get the MIME type of the source.
     */
    public abstract String getMIMEType();
    
    /**
     * Create a reader, that reads the source.
     */
    public abstract Reader createReader() throws IOException ;
    
    /**
     * Create a writer, that writes to the source.
     * @param conflicts The list of conflicts remaining in the source.
     *                  Can be <code>null</code> if there are no conflicts.
     * @return The writer or <code>null</code>, when no writer can be created.
     */
    public abstract Writer createWriter(Difference[] conflicts) throws IOException ;
    
    /**
     * Notify, that this stream source is no longer needed. This method, is called
     * when this object will never be asked for the streams any more and thus can
     * release it's resources in this method.
     */
    public void notifyClosed() {
    }
    
    /**
     * Create the default implementation of <code>StreamSource</code>, that has
     * just reader and no writer.
     */
    public static StreamSource createSource(String name, String title, String MIMEType, Reader r) {
        return new Impl(name, title, MIMEType, r);
    }
    
    /**
     * Create the default implementation of <code>StreamSource</code>, that has
     * just reader and writer from/to a file.
     */
    public static StreamSource createSource(String name, String title, String MIMEType, File file) {
        return new Impl(name, title, MIMEType, file);
    }
    
    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {
        
        private String name;
        private String title;
        private String MIMEType;
        private Reader r;
        private File readerSource;
        private Writer w;
        private File file;
        
        Impl(String name, String title, String MIMEType, Reader r) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.r = r;
            this.readerSource = null;
            this.w = null;
            this.file = null;
        }
        
        Impl(String name, String title, String MIMEType, File file) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.readerSource = null;
            this.w = null;
            this.file = file;
        }
        
        private File createReaderSource(Reader r) throws IOException {
            File tmp = null;
            tmp = File.createTempFile("ss", "tmp");
            tmp.deleteOnExit();
            tmp.createNewFile();
            org.openide.filesystems.FileUtil.copy(new ReaderInputStream(r), new FileOutputStream(tmp));
            return tmp;
        }
        
        public String getName() {
            return name;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getMIMEType() {
            return MIMEType;
        }
        
        public Reader createReader() throws IOException {
            if (file != null) return new BufferedReader(new FileReader(file));
            else {
                synchronized (this) {
                    if (r != null) {
                        readerSource = createReaderSource(r);
                        r = null;
                    }
                }
                return new BufferedReader(new FileReader(readerSource));
            }
        }
        
        public Writer createWriter(Difference[] conflicts) throws IOException {
            if (conflicts != null && conflicts.length > 0) return null;
            if (file != null) return new BufferedWriter(new FileWriter(file));
            else return w;
        }
        
        public void notifyClosed() {
        }
    
    }
}
