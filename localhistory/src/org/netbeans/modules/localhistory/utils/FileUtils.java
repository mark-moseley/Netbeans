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

package org.netbeans.modules.localhistory.utils;

import java.io.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

/**
 * // XXX clean up
 * @author pkuzel
 */
public class FileUtils {

    /**
     * This utility class needs not to be instantiated anywhere.
     */
    private FileUtils() {
    }
    
    /**
     * Copies the specified sourceFile to the specified targetFile.
     * 
     * @param sourceFile
     * @param targetFile
     * 
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        if (sourceFile == null || targetFile == null) {
            throw new NullPointerException("sourceFile and targetFile must not be null"); // NOI18N
        }

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(sourceFile);            
            copy(inputStream, targetFile);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    /**
     * Copies the specified file to the specified outputstream.
     * It <b>closes</b> the output stream.
     * 
     * @param file
     * @param os
     * 
     */    
    public static void copy(File file, OutputStream os) throws IOException {
        if (file == null ) {
            throw new NullPointerException("file must not be null"); // NOI18N
        }        
        if (os == null ) {
            throw new NullPointerException("output stream must not be null"); // NOI18N
        }        
        InputStream is = null;
        try {
            is = createInputStream(file);            
            FileUtil.copy(is, os);
        } finally {
            if (is != null) { try { is.close(); } catch (IOException ex) { } }
            if (os != null) { try { os.close(); } catch (IOException ex) { } }
        }
    }
    
    /**
     * Copies the specified inputStream to the specified targetFile.
     * It <b>closes</b> the input stream.
     * 
     * @param inputStream
     * @param targetFile
     * 
     */
    private static void copy(InputStream inputStream, File targetFile) throws IOException {
        if (inputStream == null || targetFile == null) {
            throw new NullPointerException("sourcStream and targetFile must not be null"); // NOI18N
        }

        // ensure existing parent directories
        File directory = targetFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory '" + directory + "'"); // NOI18N
        }

        OutputStream outputStream = null;
        try {            
            outputStream = createOutputStream(targetFile);
            try {
                byte[] buffer = new byte[32768];
                for (int readBytes = inputStream.read(buffer);
                     readBytes > 0;
                     readBytes = inputStream.read(buffer)) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }
            catch (IOException ex) {
                targetFile.delete();
                throw ex;
            }
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    /**
     * Recursively deletes all files and directories under a given file/directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }
    
    /**
     * Do the best to rename the file.
     * @param orig regular file
     * @param dest regular file (if exists it's rewritten)
     */
    public static void renameFile(File orig, File dest) throws IOException {
        boolean destExists = dest.exists();
        if (destExists) {
            for (int i = 0; i<3; i++) {
                if (dest.delete()) {
                    destExists = false;
                    break;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        if (destExists == false) {
            for (int i = 0; i<3; i++) {
                if (orig.renameTo(dest)) {
                    return;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        // requires less permisions than renameTo
        FileUtils.copyFile(orig, dest);

        for (int i = 0; i<3; i++) {
            if (orig.delete()) {
                return;
            }
            try {
                Thread.sleep(71);
            } catch (InterruptedException e) {
            }
        }
        throw new IOException("Can not delete: " + orig.getAbsolutePath());  // NOI18N
    }
    
    private static BufferedInputStream createInputStream(File file) throws IOException {
        int retry = 0;
        while (true) {   
            try {
                return new BufferedInputStream(new FileInputStream(file));                
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }       
    }
    
    private static BufferedOutputStream createOutputStream(File file) throws IOException {
        int retry = 0;
        while (true) {            
            try {
                return new BufferedOutputStream(new FileOutputStream(file));                
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }       
    }
    
}
