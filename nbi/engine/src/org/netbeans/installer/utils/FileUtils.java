/*
 * FileUtils.java
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Vector;
import java.util.zip.CRC32;
import org.netbeans.installer.utils.log.LogManager;
import java.io.*;
import java.security.MessageDigest;

/**
 *
 *
 *
 *
 * @author ks152834
 */
public class FileUtils {
    /**
     * Reads a file into a string.
     *
     * @param file The file to read
     * @throws java.io.IOException if an I/O error occurs
     * @return the contents of the file
     */
    public static String readFile(File file) throws IOException {
        String contents = "";
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        char[] buffer = new char[1024];
        while (reader.ready()) {
            contents += new String(buffer, 0, reader.read(buffer));
        }
        reader.close();
        
        return contents;
    }
    
    /**
     * Writes the given contents to the file, overwriting its current contents.
     *
     * @param file the file to write to
     * @param string the string to write
     * @throws java.io.IOException if an I/O error occurs
     */
    public static void writeFile(File file, String string) throws IOException {
        writeFile(file, string, false);
    }
    
    /**
     * Writes the given contents to the file, wither overwriting or appending to
     * its current contents.
     *
     * @param file the file to write to
     * @param string the string to write
     * @param append whether to overwrite the current contents or append to them
     * @throws java.io.IOException if an I/O error occurs
     */
    public static void writeFile(File file, String string, boolean append) throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        
        String newContents = "";
        
        if (append) {
            newContents += readFile(file);
        }
        newContents += string;
        
        FileOutputStream outputStream = new FileOutputStream(file);
        
        outputStream.write(newContents.getBytes());
        
        outputStream.close();
    }
    
    public static String[] readStringList(File file) throws IOException {
        return readStringList(file.toURL());
    }
    
    public static String[] readStringList(URL url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("The supplied URL cannot be null");
        }
        
        LogManager.getInstance().log("    reading string list from URL: " + url);
        
        BufferedReader reader = null;
        Vector<String> lines  = new Vector<String>();
        
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            
            while (reader.ready()) {
                String line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        
        return lines.toArray(new String[lines.size()]);
    }
    
    public static Date getLastModified(String fname) {
        return (fname==null) ? null : getLastModified(new File(fname));
    }
    
    public static Date getLastModified(File f) {
        if(!f.exists()) {
            return null;
        }
        Date date=null;
        try {
            long modif = f.lastModified();
            date = new Date(modif);
        } catch (SecurityException ex) {
            ex=null;
        }
        return date;
    }
    
    /**
     * Returns size of <b>file</b>.
     *
     * @param file
     *      File
     * @return
     *  -1 if
     *         <b>file</b> is <i>null</i><br> or
     *         <b>file</b> is a directory<br> or
     *         <b>file</b> doesn`t exist<br><br>
     *  size of file, otherwise
     */
    public static long getFileSize(File file) {
        if(file==null || file.isDirectory() || !file.exists()) {
            return -1;
        }
        try {
            return file.length();
        } catch(SecurityException ex) {
            return -1;
        }
    }
    
    public static long getFileSize(String filename) {
        return (filename==null) ? -1 : getFileSize(new File(filename));
    }
    
    public static long getFreeSpace(File file) {
        return -1;
    }
    
    //get file CRC32 checksum
    public static long getFileCRC32(File file) throws IOException {
        CRC32 crc = new CRC32();
        
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            
            for (int i = input.read(); i != -1; i = input.read()) {
                crc.update((byte) i);
            }
        } finally {
            if (input != null) {
                input.close();
            }
        }
        
        return crc.getValue();
    }
    
    public static String getFileCRC32String(File file) throws IOException {
        return Long.toString(getFileCRC32(file));
    }
    
    public static byte[] getFileDigest(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            
            byte[] buffer = new byte[10240];
            
            while (input.available() > 0) {
                md.update(buffer, 0, input.read(buffer));
            }
        } finally {
            if (input != null) {
                input.close();
            }
        }
        
        return md.digest();
    }
    
    public static byte[] getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        return getFileDigest(file, "MD5");
    }
    
    public static String getFileMD5String(File file) throws IOException, NoSuchAlgorithmException {
        return StringUtils.asHexString(getFileMD5(file));
    }
    
    /**
     * Load strings from file
     */
    public static Vector <String> getStrings(File file) throws IOException {
        Vector <String> vector = new Vector <String> ();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(file)));
            
            String string;
            while ((string = reader.readLine()) != null) {
                vector.add(string);
            }
            reader.close();
        } catch (IOException ex) {
            throw ex;
        }
        
        return vector;
    }
    
    /**
     * Deteles a file.
     *
     * @param file The file to delete
     */
    public static void deleteFile(File file) {
        deleteFile(file, true);
    }
    
    /**
     * Deletes a file taking into account that it can be a symlink.
     *
     * @param file The file to delete
     * @param followLinks to follow symlinks or to just delete the link
     */
    public static void deleteFile(File file, boolean followLinks) {
        String type = "";
        if (file.isDirectory()) {
            if (followLinks) {
                File[] children = file.listFiles();
                
                for (File child: children) {
                    deleteFile(child);
                }
            }
            
            type = "directory"; //NOI18N
        } else {
            type = "file"; //NOI18N
        }
        
        //LogUtils.log("    deleting " + type + ": " + file); //NOI18N
        
        if (!file.exists()) {
            //LogUtils.log("    ... " + type + " does not exist"); //NOI18N
        }
        
        file.delete();
        file.deleteOnExit();
    }
    
    /**
     * Deletes a file if its name matches a given mask. If the given file is a
     * directory it is recursively traversed.
     *
     * @param file The file to delete
     * @param mask The mask for the filename
     */
    public static void deleteFile(File file, String mask) {
        if (file.isDirectory()) {
            File[] children = file.listFiles(new MaskFileFilter(mask));
            
            for (File child: children) {
                deleteFile(child, mask);
            }
        } else {
            if (file.getName().matches(mask)) {
                deleteFile(file);
            }
        }
    }
    
    /**
     *  Write strings list to the file
     *   @param stringList list of strings to save
     *   @param file File to be saved in
     *   @param append If true then append string list to the end of file
     */
    public static void writeList(Vector stringList,File file,boolean append) {
        if(stringList == null) {
            return;
        }
        //save unzipped and unpacked file list to the file
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file,append)));
            for(Object s: stringList.toArray()) {
                writer.write((String) s);
                writer.newLine();
            }
            writer.close();
        } catch (IOException ex) {
            LogManager.getInstance().log("Can`t write string list to " + file); //NOI18N
        }
    }
    
    /**
     *  Write strings list to the file
     *   @param stringList list of strings to save
     *   @param filename File name to be saved in
     *   @param append If true then append string list to the end of file
     */
    public static void writeList(Vector stringList,String filename,boolean append) {
        writeList(stringList,new File(filename),append);
    }
    
    public static File createTempFile() throws IOException {
        File file = File.createTempFile("nbi-", ".tmp");
        
        file.deleteOnExit();
        
        return file;
    }
    
    public static File createTempFile(File parent) throws IOException {
        File file = File.createTempFile("nbi-", ".tmp", parent);
        
        file.deleteOnExit();
        
        return file;
    }
    
    /**
     * A file filter which accepts all common configuration files.
     *
     * @author Kirill Sorokin
     */
    private static class ConfigFilesFilter implements FileFilter {
        /**
         * {@inheritDoc}
         */
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            
            if (file.getName().endsWith(".xml") ||            //NOI18N
                    file.getName().endsWith(".bat") ||        //NOI18N
                    file.getName().endsWith(".sh") ||         //NOI18N
                    file.getName().endsWith(".conf") ||       //NOI18N
                    file.getName().endsWith(".properties") || //NOI18N
                    file.getName().endsWith(".html") ||       //NOI18N
                    file.getName().endsWith(".txt") ||        //NOI18N
                    (file.getName().indexOf(".") == -1))      //NOI18N
            {
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * A file filter which accepts files whose names match a given mask.
     *
     * @author Kirill Sorokin
     */
    private static class MaskFileFilter implements FileFilter {
        private String mask = ".*";                 //NOI18N
        
        /**
         * Creates a new instance of MaskFileFilter.
         */
        public MaskFileFilter(String maskValue) {
            this.mask = maskValue;
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            
            if (file.getName().matches(mask)) {
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * A file filter which accepts directories.
     *
     * @author Kirill Sorokin
     */
    private static class DirectoryFileFilter implements FileFilter {
        /**
         * {@inheritDoc}
         */
        public boolean accept(File file) {
            return (file.isDirectory()) ? true : false;
        }
    }
}
