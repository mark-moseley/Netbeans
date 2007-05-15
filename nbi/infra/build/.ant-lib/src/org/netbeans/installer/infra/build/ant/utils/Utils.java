/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.infra.build.ant.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.Project;

/**
 * A collection of utility methods used throughout the custom tasks classes.
 *
 * @author Kirill Sorokin
 */
public final class Utils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    /**
     * The current ant project. Some of its methods will get called in process of
     * the executions of some of the utility procedures. Thus the ant tasks using the
     * class are streongly encouraged to call the {@link #setProject(Project)}
     * method prior to suing any other functionality.
     */
    private static Project project = null;
    
    /**
     * Setter for the 'project' property.
     *
     * @param project New value for the 'project' property.
     */
    public static void setProject(final Project project) {
        Utils.project = project;
    }
    
    /**
     * Calculates the MD5 checksum for the given file.
     *
     * @param file File for which the checksum should be calculated.
     * @return The MD5 checksum of the file.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static String getMd5(final File file) throws IOException {
        return getDigest(file, MD5);
    }
    
    /**
     * Checks whether the given file is a directory.
     *
     * @param file File to check for being a directory.
     * @return <code>true</code> if the file is a directory, <code>false</code>
     *      otherwise.
     */
    public static boolean isEmpty(final File file) {
        if (file.listFiles().length == 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Checks whether the given file is a jar archive.
     *
     * @param file File to check for being a jar archive.
     * @return <code>true</code> if the file is a jar archive, <code>false</code>
     *      otherwise.
     */
    public static boolean isJarFile(final File file) {
        if (file.getName().endsWith(JAR_EXTENSION)) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return false;
        }
    }
    
    /**
     * Checks whether the given file is a signed jar archive.
     *
     * @param file File to check for being a signed jar archive.
     * @return <code>true</code> if the file is a signedjar archive,
     *      <code>false</code> otherwise.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static boolean isSigned(final File file) throws IOException {
        JarFile jar = new JarFile(file);
        
        try {
            if (jar.getEntry(SUN_MICR_RSA) == null) {
                return false;
            }
            if (jar.getEntry(SUN_MICR_SF) == null) {
                return false;
            }
            return true;
        } finally {
            jar.close();
        }
    }
    
    /**
     * Packs the given jar archive using the pack200 utility.
     *
     * @param source Jar archive to pack.
     * @param target File which should become the packed jar archive.
     * @return The target file, i.e. the packed jar archive.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static boolean pack(
            final File source,
            final File target) throws IOException {
        boolean result = false;
        
        final String packer;
        if (System.getProperty("os.name").contains("Windows")) {
            packer = System.getProperty("java.home") + "\\bin\\pack200.exe";
        } else {
            packer = System.getProperty("java.home") + "/bin/pack200";
        }
        
        final String xmx = ARG_PREFIX + XMX_ARG +
                project.getProperty("pack200.xmx");
        final String permSize = ARG_PREFIX + PERM_SIZE_ARG +
                project.getProperty("pack200.perm.size");
        final String maxPermSize = ARG_PREFIX + MAX_PERM_SIZE_ARG +
                project.getProperty("pack200.max.perm.size");
        
        Results results = run(
                packer,
                xmx,
                permSize,
                maxPermSize,
                target.getAbsolutePath(),
                source.getAbsolutePath());
        
        if (results.getExitcode() == 0) {
            result = true;
        } else {
            System.out.println(results.getStdout());
            System.out.println(results.getStderr());
            System.out.println(results.getExitcode());
            result = false;
        }
        
        if (result == true) {
            target.setLastModified(source.lastModified());
        }
        
        return result;
    }
    
    /**
     * Unpacks the given packed jar archive using the unpack200 utility.
     *
     * @param source Packe jar archive to unpack.
     * @param target File to which the unpacked archive should be saved.
     * @return The target file, i.e. the unpacked jar archive.
     * @throws java.io.IOException if an I/O errors occurs.
     */
    public static boolean unpack(
            final File source,
            final File target) throws IOException {
        boolean result = false;
        
        String unpacker = null;
        if (System.getProperty("os.name").contains("Windows")) {
            unpacker = System.getProperty("java.home") + "\\bin\\unpack200.exe";
        } else {
            unpacker = System.getProperty("java.home") + "/bin/unpack200";
        }
        
        Results results = run(
                unpacker,
                ARG_PREFIX + XMX_ARG + project.getProperty("pack200.xmx"),
                ARG_PREFIX + PERM_SIZE_ARG + project.getProperty("pack200.perm.size"),
                ARG_PREFIX + MAX_PERM_SIZE_ARG + project.getProperty("pack200.max.perm.size"),
                source.getAbsolutePath(),
                target.getAbsolutePath());
        
        if (results.getExitcode() == 0) {
            result = true;
        } else {
            System.out.println(results.getStdout());
            System.out.println(results.getStderr());
            System.out.println(results.getExitcode());
            result = false;
        }
        
        if (result == true) {
            target.setLastModified(source.lastModified());
        }
        
        return result;
    }
    
    /**
     * Verifies that the jar archive is correct. This method tries to access all
     * jar archive entries and to load all the classes.
     *
     * @param file Jar archive to check.
     * @return <code>true</code> is the archive is correct, <code>false</code>
     *      otherwise.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static boolean verify(final File file) throws IOException {
        Results results = runClass(VERIFIER_CLASSNAME, file.getAbsolutePath());
        
        if (results.getExitcode() == 0) {
            return true;
        } else {
            System.out.println(results.getStdout());
            System.out.println(results.getStderr());
            System.out.println(results.getExitcode());
            return false;
        }
    }
    
    /**
     * Fully reads an input stream into a character sequence using the system's
     * default encoding.
     *
     * @param in Input sream to read.
     * @return The read data as a character sequence.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static CharSequence read(final InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        byte[] buffer = new byte[1024];
        while (in.available() > 0) {
            int read = in.read(buffer);
            
            String readString = new String(buffer, 0, read);
            for(String string : readString.split(NEWLINE_REGEXP)) {
                builder.append(string).append(File.separator);
            }
        }
        
        return builder;
    }
    
    /**
     * Fully transfers the given input stream to the given output stream.
     * @param in Input stream to read and transfer.
     * @param out Output stream to transfer data to.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void copy(
            final InputStream in,
            final OutputStream out) throws IOException {
        byte[] buffer = new byte[102400];
        
        while (in.available() > 0) {
            out.write(buffer, 0, in.read(buffer));
        }
    }
    
    /**
     * Unzips a zip archive to the specified directory.
     *
     * @param file Zip archive to extract.
     * @param directory Directory which will be the target for the extraction.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void unzip(
            final File file,
            final File directory) throws IOException {
        ZipFile zip = new ZipFile(file);
        
        if (directory.exists() && directory.isFile()) {
            throw new IOException("Directory is an existing file, cannot unzip.");
        }
        
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Cannot create directory");
        }
        
        Enumeration<? extends ZipEntry> entries =
                (Enumeration<? extends ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            
            File entryFile = new File(directory, entry.getName());
            
            InputStream  in;
            OutputStream out;
            if (entry.getName().endsWith(SLASH)) {
                entryFile.mkdirs();
            } else {
                in = zip.getInputStream(entry);
                out = new FileOutputStream(entryFile);
                
                copy(in, out);
                
                in.close();
                out.close();
            }
            
            entryFile.setLastModified(entry.getTime());
        }
        
        zip.close();
    }
    
    public static void nativeUnzip(
            final File file,
            final File directory) throws IOException {
        final String[] command;
        if (System.getProperty(OS_NAME).contains(WINDOWS)) {
            command = new String[] {
                "unzip.exe",
                file.getAbsolutePath(),
                "-d",
                directory.getAbsolutePath()};
        } else {
            command = new String[] {
                "unzip",
                file.getAbsolutePath(),
                "-d",
                directory.getAbsolutePath()};
        }
        
        if (project != null) {
            project.log("            running command: " + Arrays.asList(command));
        }
        
        final Results results = run(command);
        
        if (results.getExitcode() != 0) {
            System.out.println(results.getStdout());
            System.out.println(results.getStderr());
            throw new IOException();
        }
    }
    
    /**
     * Deletes a file. If the file is a directory its contents are recursively
     * deleted.
     *
     * @param file File to be deleted.
     */
    public static void delete(final File file) {
        if (file.isDirectory()) {
            for (File child: file.listFiles()) {
                delete(child);
            }
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }
    
    /**
     * Measures the size of a file. If the file is a directory, its size would be
     * equal to the sum of sizes of all its files and subdirectories.
     *
     * @param file File whose size should be measured.
     * @return The size of file.
     */
    public static long size(final File file) {
        long size = 0;
        
        if (file.isDirectory()) {
            for (File child: file.listFiles()) {
                size += size(child);
            }
        }
        
        return size + file.length();
    }
    
    /**
     * Converts the given string to its java-style ASCII equivalent, escaping
     * non ASCII characters with their \\uXXXX sequences.
     *
     * @param string String to escape.
     * @return The escaped string.
     */
    public static String toAscii(final String string) {
        Properties properties = new Properties();
        
        properties.put(UBERKEY, string);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            properties.store(outputStream, "");
        } catch (IOException e) {
            e.printStackTrace();
            return string;
        }
        
        Matcher matcher = Pattern.compile(UBERKEY_REGEXP, Pattern.MULTILINE).matcher(outputStream.toString());
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return string;
        }
    }
    
    /**
     * Writes the given character sequence to the given file.
     *
     * @param file File to which the character sequence should be written.
     * @param chars Character sequence which should be written to the file.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void write(
            final File file,
            final CharSequence chars) throws IOException {
        OutputStreamWriter writer =
                new OutputStreamWriter(new FileOutputStream(file), UTF8);
        writer.write(chars.toString());
        writer.close();
    }
    
    /**
     * Copies the contents of a file to the given output stream.
     *
     * @param source File whose contents should be copied.
     * @param target Output stream to which the file's contents should be
     *      transferred.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void copy(
            final File source,
            final OutputStream target) throws IOException {
        FileInputStream input = new FileInputStream(source);
        copy(input, target);
        input.close();
    }
    
    /**
     * Copies one file to another.
     *
     * @param source File to be copied.
     * @param target File which should be come the copy of the source one.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void copy(
            final File source,
            final File target) throws IOException {
        FileOutputStream out = new FileOutputStream(target);
        copy(source, out);
        out.close();
    }
    
    /**
     * Sends an HTTP POST request to the given URL. The supplied parameters will be
     * passed as part of the request body according to
     * {@link http://www.faqs.org/rfcs/rfc1945.html}.
     *
     * @param url URL to shich the POST request should be sent.
     * @param args Request parameters.
     * @return The first line of the server response, e.g. "HTTP/1.x 200 OK".
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static String post(
            final String url,
            final Map<String, Object> args) throws IOException {
        final String boundary     = "---------------" + Math.random();
        final byte[] realBoundary = ("--" + boundary).getBytes("UTF-8");
        final byte[] endBoundary  = ("--" + boundary + "--").getBytes("UTF-8");
        final byte[] crlf         = new byte[]{13, 10};
        
        final HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        
        connection.connect();
        final OutputStream out = connection.getOutputStream();
        
        final Iterator<String> iterator = args.keySet().iterator();
        while (iterator.hasNext()) {
            String key   = iterator.next();
            Object value = args.get(key);
            
            out.write(realBoundary);
            out.write(crlf);
            
            if (value instanceof File) {
                File file = (File) value;
                
                out.write(("Content-Disposition: form-data; name=\"" +
                        key + "\"; filename=\"" +
                        file.getName() + "\"").getBytes("UTF-8"));
                out.write(crlf);
                
                out.write(("Content-Type: " +
                        "application/octet-stream").getBytes("UTF-8"));
                out.write(crlf);
                out.write(crlf);
                
                copy(file, out);
            }
            
            if (value instanceof String) {
                String string = (String) value;
                
                out.write(("Content-Disposition: form-data; " +
                        "name=\"" + key + "\"").getBytes("UTF-8"));
                out.write(crlf);
                out.write(crlf);
                
                out.write(string.getBytes("UTF-8"));
            }
            
            out.write(crlf);
        }
        
        out.write(endBoundary);
        out.close();
        
        return "" + connection.getResponseCode() + " " + connection.getResponseMessage();
    }
    
    /**
     * Signs the given jar file using the data provided in the given keystore.
     *
     * @param file Jar archive which will be signed.
     * @param keystore Path to the keystore file.
     * @param alias Keystore alias.
     * @param password Keystore password.
     * @return The results of executing the <code>jarsigner</code> utility.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static Results sign(
            final File file,
            final String keystore,
            final String alias,
            final String password) throws IOException {
        String executable = System.getProperty(JAVA_HOME);
        if (System.getProperty(OS_NAME).contains(WINDOWS)) {
            executable += "\\..\\bin\\jarsigner.exe";
        } else {
            executable += "/../bin/jarsigner";
        }
        
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        int exitcode = 0;
        
        List<String> command = new ArrayList<String>();
        
        command.add(executable);
        command.add("-keystore");
        command.add(keystore);
        command.add(file.getAbsolutePath());
        command.add(alias);
        
        Process process = new ProcessBuilder(command).start();
        
        process.getOutputStream().write(password.getBytes());
        
        long running;
        for (running = 0; running < MAX_EXECUTION_TIME; running += DELAY) {
            CharSequence string;
            
            string = read(process.getInputStream());
            if (string.length() > 0) {
                stdout.append(string);
            }
            
            string = read(process.getErrorStream());
            if (string.length() > 0) {
                stderr.append(string);
            }
            
            try {
                exitcode = process.exitValue();
                break;
            } catch (IllegalThreadStateException e) {
                ; // do nothing - the process is still running
            }
            
            try {
                Thread.sleep(DELAY);
            }  catch (InterruptedException e) {
                // do nothing - this may happen every now and then
            }
        }
        
        process.destroy();
        
        return new Results(stdout, stderr, exitcode);
    }
    
    public static int getPermissions(final File file) {
        try {
            final Results results;
            if (System.getProperty(OS_NAME).contains(WINDOWS)) {
                results = run("ls.exe", "-ld", file.getAbsolutePath());
            } else {
                results = run("ls", "-ld", file.getAbsolutePath());
            }
            
            final String output = results.getStdout().toString().trim();
            
            if (project != null) {
                project.log("            " + output);
            } else {
                System.out.println(output);
            }
            
            int permissions = 0;
            for (int i = 0; i < 9; i++) {
                char character = output.charAt(i + 1);
                
                if (i % 3 == 0) {
                    permissions *= 10;
                }
                
                if (character == '-') {
                    continue;
                } else if ((i % 3 == 0) && (character == 'r')) {
                    permissions += 4;
                } else if ((i % 3 == 1) && (character == 'w')) {
                    permissions += 2;
                } else if ((i % 3 == 2) && (character == 'x')) {
                    permissions += 1;
                } else {
                    return -1;
                }
            }
            
            return permissions;
        } catch (IOException e) {
            return -1;
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Calculates the digital digest of the given file's contents using the
     * supplied algorithm.
     *
     * @param file File for which the digest should be calculated.
     * @param algorithm Algorithm which should be used for calculating the digest.
     * @return The calculated digest as a string.
     * @throws java.io.IOException if an I/O error occurs.
     */
    private static String getDigest(
            final File file,
            final String algorithm) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.reset();
            
            InputStream input = null;
            try {
                input = new FileInputStream(file);
                
                byte[] buffer = new byte[10240];
                
                while (input.available() > 0) {
                    md.update(buffer, 0, input.read(buffer));
                }
            }  finally {
                if (input != null) {
                    input.close();
                }
            }
            
            byte[] bytes = md.digest();
            
            StringBuilder builder = new StringBuilder();
            
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                
                String byteHex = Integer.toHexString(b);
                if (byteHex.length() == 1) {
                    byteHex = "0" + byteHex;
                }
                if (byteHex.length() > 2) {
                    byteHex = byteHex.substring(byteHex.length() - 2);
                }
                
                builder.append(byteHex);
            }
            
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Could not find the aglorithm");
        }
    }
    
    /**
     * Runs the given class in a separate JVM.
     *
     * @param clazz Classname of the class which should be run.
     * @param args Command-line arguments for the class.
     * @return Results of executing the command (exitcode, stdout and stderr
     *      contents).
     * @throws java.io.IOException if an I/O error occurs.
     */
    private static Results runClass(
            final String clazz,
            final String... args) throws IOException {
        final String classPath = project.getProperty(CLASSPATH_VALUE_PROPERTY);
        
        final String java;
        if (System.getProperty(OS_NAME).contains(WINDOWS)) {
            java = System.getProperty(JAVA_HOME) + File.separator + JAVA_EXE;
        } else {
            java = System.getProperty(JAVA_HOME) + File.separator + JAVA;
        }
        
        final List<String> command = new ArrayList<String>();
        
        command.add(java);
        command.add(CLASSPATH_ARG);
        command.add(classPath);
        command.add(clazz);
        command.addAll(Arrays.asList(args));
        
        return run(command.toArray(new String[command.size()]));
    }
    
    /**
     * Runs the specified command using the <code>ProcessBuilder</code> class.
     *
     * @param command Path to the executable and its arguments.
     * @return Results of executing the command (exitcode, stdout and stderr
     *      contents).
     * @throws java.io.IOException if an I/O error occurs.
     */
    private static Results run(final String... command) throws IOException {
        StringBuilder processStdOut = new StringBuilder();
        StringBuilder processStdErr = new StringBuilder();
        int errorCode = 0;
        
        Process process = new ProcessBuilder(command).start();
        
        boolean doRun = true;
        long running;
        for (running = 0; doRun && (running < MAX_EXECUTION_TIME); running += DELAY) {
            try {
                Thread.sleep(DELAY);
            }  catch (InterruptedException e) {
                // do nothing - this may happen every now and then
            }
            
            try {
                errorCode = process.exitValue();
                doRun = false;
            } catch (IllegalThreadStateException e) {
                ; // do nothing - the process is still running
            }
            
            CharSequence string = read(process.getInputStream());
            if (string.length() > 0) {
                processStdOut.append(string);
            }
            
            string = read(process.getErrorStream());
            if (string.length() > 0) {
                processStdErr.append(string);
            }
        }
        
        process.destroy();
        
        return new Results(processStdOut, processStdErr, errorCode);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The private default constructor which prevents the class from being
     * instantiated.
     */
    private Utils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     * This class is a container for the results of executing a process. It keeps
     * the values of <code>&lt;stdout&gt;</code>, <code>&lt;stderr&gt;</code> and
     * the exitcode.
     *
     * @author Kirill Sorokin
     */
    public static class Results {
        /**
         * Value of <code>&lt;stdout&gt;</code>.
         */
        private CharSequence stdout;
        
        /**
         * Value of <code>&lt;stdout&gt;</code>.
         */
        private CharSequence stderr;
        
        /**
         * Value of the exitcode.
         */
        private int exitcode;
        
        /**
         * Creates a new instance of <code>Results</code>. The constructor simply
         * initializes the class properties with the passed-in values.
         *
         * @param stdout Contents of the process's <code>&lt;stdout&gt;</code>.
         * @param stderr Contents of the process's <code>&lt;stderr&gt;</code>.
         * @param exitcode The process's exitcode.
         */
        public Results(
                final CharSequence stdout,
                final CharSequence stderr,
                final int exitcode) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitcode = exitcode;
        }
        
        /**
         * Getter for the 'stdout' property.
         *
         * @return Value of the 'stdout' property.
         */
        public CharSequence getStdout() {
            return stdout;
        }
        
        /**
         * Getter for the 'stderr' property.
         *
         * @return Value of the 'stderr' property.
         */
        public CharSequence getStderr() {
            return stderr;
        }
        
        /**
         * Getter for the 'exitcode' property.
         *
         * @return Value of the 'exitcode' property.
         */
        public int getExitcode() {
            return exitcode;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Maximum allowed execution time for a process.
     */
    public static final int MAX_EXECUTION_TIME =
            300000; // NOMAGI
    
    /**
     * Deplay (in milliseconds) which to wait between cheking the process state.
     */
    public static final int DELAY =
            500; // NOMAGI
    
    /**
     * Prefix for JVM command-line arguments.
     */
    public static final String ARG_PREFIX =
            "-J"; // NOI18N
    
    /**
     * Maximum heap size command-line argument prefix.
     */
    public static final String XMX_ARG =
            "-Xmx"; // NOI18N
    
    /**
     * PermSize command-line argument prefix.
     */
    public static final String PERM_SIZE_ARG =
            "-XX:PermSize="; // NOI18N
    
    /**
     * MacPermSize command-line argument prefix.
     */
    public static final String MAX_PERM_SIZE_ARG =
            "-XX:MaxPermSize="; // NOI18N
    
    /**
     * Classpath command-line argument prefix.
     */
    public static final String CLASSPATH_ARG =
            "-cp"; // NOI18N
    
    /**
     * Classname of the class which should be called to verify the unpacked jar
     * file.
     */
    public static final String VERIFIER_CLASSNAME =
            "org.netbeans.installer.infra.build.ant.utils.VerifyFile"; // NOI18N
    
    /**
     * Name of the ant project's property which contains the classpath which should
     * be used for running classes.
     */
    public static final String CLASSPATH_VALUE_PROPERTY =
            "custom.tasks.cls"; // NOI18N
    
    /**
     * MD5 digital digest algorithm code name.
     */
    public static final String MD5 =
            "MD5"; // NOI18N
    
    /**
     * Extension of jar files.
     */
    public static final String JAR_EXTENSION =
            ".jar"; // NOI18N
    
    /**
     * Marker file which indicated that the jar file is signed.
     */
    public static final String SUN_MICR_RSA =
            "META-INF/SUN_MICR.RSA"; // NOI18N
    
    /**
     * Marker file which indicated that the jar file is signed.
     */
    public static final String SUN_MICR_SF =
            "META-INF/SUN_MICR.SF"; // NOI18N
    
    /**
     * A regular expression which matches any line separator.
     */
    public static final String NEWLINE_REGEXP =
            "(?:\n\r|\r\n|\n|\r)"; // NOI18N
    
    /**
     * Forward slash.
     */
    public static final String SLASH =
            "/"; // NOI18N
    
    /**
     * An artificial key name used in converting a string to ASCII.
     */
    public static final String UBERKEY =
            "uberkey"; // NOI18N
    
    /**
     * An artificial regular expresion used in converting a string to ASCII.
     */
    public static final String UBERKEY_REGEXP =
            "uberkey=(.*)$"; // NOI18N
    
    /**
     * Name of the UTF-8 encoding.
     */
    public static final String UTF8 =
            "UTF-8"; // NOI18N
    
    
    /**
     * Name of the system property which contains the operating system name.
     */
    public static final String OS_NAME =
            "os.name"; // NOI18N
    
    /**
     * Name of the windows operationg system.
     */
    public static final String WINDOWS =
            "Windows"; // NOI18N
    
    /**
     * Name of the system property which contains the current java home.
     */
    public static final String JAVA_HOME =
            "java.home"; // NOI18N
    
    /**
     * Path to the java executable on non-windows platforms. Relative to the java
     * home.
     */
    public static final String JAVA =
            "bin/java"; // NOI18N
    
    /**
     * Path to the java executable on windows platforms. Relative to the java
     * home.
     */
    public static final String JAVA_EXE =
            "bin\\java.exe"; // NOI18N
}
