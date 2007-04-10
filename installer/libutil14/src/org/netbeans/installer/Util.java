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

package org.netbeans.installer;

import com.installshield.util.LocalizedStringResolver;
import com.installshield.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.io.FileFilter;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

public class Util {
    
    // Operating System
    
    public static boolean isWindowsOS() {
        return System.getProperty("os.name").startsWith("Windows");
    }
    public static boolean isWindowsVista() {
        return System.getProperty("os.name").startsWith("Windows Vista");
    }
    public static boolean isWindowsXP() {
        return System.getProperty("os.name").startsWith("Windows XP");
    }
    public static boolean isWindowsNT() {
        return System.getProperty("os.name").startsWith("Windows NT");
    }
    public static boolean isWindowsME() {
        return System.getProperty("os.name").startsWith("Windows ME");
    }
    public static boolean isWindows2K() {
        return System.getProperty("os.name").startsWith("Windows 2000");
    }
    public static boolean isWindows98() {
        return System.getProperty("os.name").startsWith("Windows 98");
    }
    public static boolean isWindows95() {
        return System.getProperty("os.name").startsWith("Windows 95");
    }
    public static boolean isUnixOS() {
        return isLinuxOS() || isSunOS() || isAixOS() || isHpuxOS() || isIrixOS() || isDigitalOS() || isMacOSX();
    }
    public static boolean isLinuxOS() {
        return System.getProperty("os.name").startsWith("Lin");
    }
    public static boolean isSunOS() {
        return System.getProperty("os.name").startsWith("Sun");
    }
    public static boolean isSolarisSparc () {
        if (System.getProperty("os.name").startsWith("Sun")) {
            if (System.getProperty("os.arch").startsWith("sparc")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public static boolean isSolarisX86 () {
        if (System.getProperty("os.name").startsWith("Sun")) {
            if (System.getProperty("os.arch").startsWith("x86")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public static boolean isAixOS() {
        return System.getProperty("os.name").startsWith("AIX");
    }
    public static boolean isHpuxOS() {
        return System.getProperty("os.name").startsWith("HP-UX");
    }
    public static boolean isIrixOS() {
        return System.getProperty("os.name").startsWith("Irix");
    }
    public static boolean isDigitalOS() {
        return System.getProperty("os.name").startsWith("Dig");
    }
    public static boolean isOS2OS() {
        return System.getProperty("os.name").startsWith("OS/2");
    }
    public static boolean isOpenVMSOS() {
        return System.getProperty("os.name").startsWith("Open");
    }
    public static boolean isMacOSX() {
        return System.getProperty("os.name").startsWith("Mac OS X");
    }
    public static boolean isDarwinOS() {
        return System.getProperty("os.name").startsWith("Darwin");
    }
    
    //Installer types
    public static boolean isASBundle() {
        return System.getProperty(Names.INSTALLER_TYPE).equals(Names.INSTALLER_AS_BUNDLE);
    }
    
    public static String getBackupDir() {
        return getStringPropertyValue("backupDir");
    }
    
    public static void setBackupDir(String backupDir) {
        setStringPropertyValue("backupDir", backupDir);
    }
    
    public static String getNbInstallDir() {
        return getStringPropertyValue("nbInstallDir");
    }
    
    public static void setNbInstallDir(String nbInstallDir) {
        setStringPropertyValue("nbInstallDir", nbInstallDir);
    }
    
    public static String getASInstallDir() {
        return getStringPropertyValue("asInstallDir");
    }
    
    public static void setASInstallDir(String asInstallDir) {
        setStringPropertyValue("asInstallDir", asInstallDir);
    }
    
    public static Vector getNbInstallList() {
        return (Vector) System.getProperties().get("nbInstallList");
    }
    
    public static void setNbInstallList(Vector nbInstallList) {
        System.getProperties().put("nbInstallList", nbInstallList);
    }
    
    // Installation Attributes
    
    public static boolean isAdmin() {
        return getBooleanPropertyValue("isAdmin");
    }
    
    public static void setAdmin(boolean isAdmin) {
        setBooleanPropertyValue("isAdmin", isAdmin);
    }
    
    public static boolean isSkipAdmin() {
        return getBooleanPropertyValue("skipAdmin");
    }
    
    public static void setSkipAdmin(boolean skipAdmin) {
        setBooleanPropertyValue("skipAdmin", skipAdmin);
    }
    
    public static boolean isContinueInstallation() {
        return getBooleanPropertyValue("continueInstallation");
    }
    
    public static void setContinueInstallation(boolean continueInstallation) {
        setBooleanPropertyValue("continueInstallation", continueInstallation);
    }
    
    public static boolean isAssociateJava() {
        return getBooleanPropertyValue("associateJava");
    }
    
    public static void setAssociateJava(boolean associateJava) {
        setBooleanPropertyValue("associateJava", associateJava);
    }
    
    public static boolean isAssociateNBM() {
        return getBooleanPropertyValue("associateNBM");
    }
    
    public static void setAssociateNBM(boolean associateNBM) {
        setBooleanPropertyValue("associateNBM", associateNBM);
    }
    
    // Installed Product
    
    public static String getProductName() {
        return System.getProperty("productName");
    }
    
    public static void setProductName(String productName) {
        setStringPropertyValue("productName", productName);
    }
    
    ////////////////////////////////////////////////////////////
    public static String getInstalledJdk() {
        return getStringPropertyValue("installedJdk");
    }
    
    public static void setInstalledJdk(String value) {
        setStringPropertyValue("installedJdk", value);
    }
    
    public static boolean isJDKAlreadyInstalled() {
        return getBooleanPropertyValue("jdkAlreadyInstalled");
    }
    
    public static void setJDKAlreadyInstalled(boolean value) {
        setBooleanPropertyValue("jdkAlreadyInstalled", value);
    }
    
    ////////////////////////////////////////////////////////////
    public static String getInstalledJre() {
        return getStringPropertyValue("installedJre");
    }
    
    public static void setInstalledJre(String value) {
        setStringPropertyValue("installedJre", value);
    }
    
    public static boolean isJREAlreadyInstalled() {
        return getBooleanPropertyValue("jreAlreadyInstalled");
    }
    
    public static void setJREAlreadyInstalled(boolean value) {
        setBooleanPropertyValue("jreAlreadyInstalled", value);
    }
    
    ////////////////////////////////////////////////////////////
    public static boolean isBelowRecommendedJDK() {
        return getBooleanPropertyValue("isBelowRecommendedJDK");
    }
    
    public static void setBelowRecommendedJDK(boolean value) {
        setBooleanPropertyValue("isBelowRecommendedJDK", value);
    }
    
    public static String getJVMName() {
        if (isWindowsOS()) {
            return "java.exe";
        } else {
            return "java";
        }
    }
    
    public static String getJ2SEInstallDir() {
        return getStringPropertyValue("j2seInstallDir");
    }
    
    public static void setJ2SEInstallDir(String value) {
        setStringPropertyValue("j2seInstallDir", value);
    }
    
    public static String getJdkHome() {
        return getStringPropertyValue("jdkHome");
    }
    
    public static void setJdkHome(String value) {
        setStringPropertyValue("jdkHome", value);
    }

    /** Used to transfer value of system property os.arch from JDK selected
     *  in JDKSelectionPanel. Selected JDK can be different from value from JDK used
     *  to run installer.
     *  Value of os.arch property can be for example on AMD64 machine "i386" on 32bit
     *  JDK and "amd64" on 64bit JDK on both Linux and Windows.
     */
    public static String getOSArch () {
        return getStringPropertyValue("os_arch");
    }
    
    public static void setOSArch (String value) {
        setStringPropertyValue("os_arch",value);
    }
    
    public static String getCurrentJDKHome() {
        return getStringPropertyValue("currentJDKHome");
    }
    
    public static void setCurrentJDKHome(String value) {
        setStringPropertyValue("currentJDKHome", value);
    }
    
    public static Vector getJdkHomeList() {
        return (Vector) System.getProperties().get("jdkHomeList");
    }
    
    public static void setJdkHomeList(Vector jdkHomeList) {
        System.getProperties().put("jdkHomeList", jdkHomeList);
    }
    
    // JDS
    private static final String PLATFORM_LINUX  = "linuxPlatform";
    private static final String PLATFORM_JDS    = "JDS";
    private static final String PLATFORM_SUSE   = "SuSE";
    private static final String PLATFORM_REDHAT = "RedHat";
    
    public static boolean isSunJDS() {
        return PLATFORM_JDS.equals(getStringPropertyValue(PLATFORM_LINUX));
    }
    
    public static boolean isSuSELinux() {
        return PLATFORM_SUSE.equals(getStringPropertyValue(PLATFORM_LINUX));
    }
    
    public static boolean isRedHatLinux() {
        return PLATFORM_REDHAT.equals(getStringPropertyValue(PLATFORM_LINUX));
    }
    
    public static void setLinuxPlatform() {
        String line = findInFile("/etc/sun-release", "Sun Java Desktop");
        if (line != null) {
            setStringPropertyValue(PLATFORM_LINUX, PLATFORM_JDS);
        }
        else {
            line = findInFile("/etc/SuSE-release", "SuSE");
            if (line != null) {
                setStringPropertyValue(PLATFORM_LINUX, PLATFORM_SUSE);
            }
            else {
                line = findInFile("/etc/redhat-release", "Red Hat");
                if (line != null) {
                    setStringPropertyValue(PLATFORM_LINUX, PLATFORM_REDHAT);
                }
            }
        }
    }
    
    public static String findInFile(String path, String key) {
        File file = new File(path);
        if (file.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(key)) {
                        return line;
                    }
                }
                
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                if (reader != null) {
                    try {reader.close();} catch (Exception ignore) {}
                }
            }
        }
        return null;
    }
    
    public static String getSystemPropertiesFileName(String nbInstallDir) {
        return nbInstallDir + File.separator + "_uninst" + File.separator + "install.properties";
    }
    
    // Utilities
    
    public static void deleteDirectory(File dir) {
        Util.deleteDirectory(dir, null);
    }
    
    public static String getStringPropertyValue(String key) {
        Object obj = System.getProperties().get(key);
        return (obj != null) ? (String) obj : null;
    }
    
    public static String getPreviousStringPropertyValue(String key) {
        Object obj = System.getProperties().get("_previous_"+key);
        return (obj != null) ? (String) obj : null;
    }
    
    public static void setStringPropertyValue(String key, String value) {
        System.getProperties().put(key, value);
    }
    
    /* Return the boolean value from a Boolean object property */
    public static boolean getBooleanPropertyValue(String key) {
        Object obj = System.getProperties().get(key);
        return (obj != null) ? ((Boolean) obj).booleanValue() : false;
    }
    
    public static void setBooleanPropertyValue(String key, boolean value) {
        System.getProperties().put(key, (value)?Boolean.TRUE:Boolean.FALSE);
    }
    
    /** Returns temporary dir. First it checks system property temp.dir which is
     * set by native launcher. If temp.dir is not set value of system property
     * java.io.tmpdir is returned.
     */
    public static String getTmpDir () {
        String tmpDir = System.getProperty("temp.dir");
        if (tmpDir == null) {
            tmpDir = System.getProperty("java.io.tmpdir");
        }
        return tmpDir;
    }
    
    /*deletes the whole directory with the given filter*/
    public static void deleteDirectory(File dir, FileFilter filter) {
        if(!dir.exists()) {
            return;
        }
        if (!dir.delete()) {
            if (dir.isDirectory()) {
                java.io.File[] list;
                if (filter == null)
                    list = dir.listFiles();
                else
                    list = dir.listFiles(filter);
                for (int i=0; i < list.length ; i++) {
                    deleteDirectory(list[i]);
                }
            }
            dir.delete();
        }
    }
    
    /** returns the size of the specified file in bytes*/
    public static long getFileSize(File filepath) {
        long size = 0;
        if (!filepath.exists()) return size;
        File[] list = filepath.listFiles();
        if ((list == null) || (list.length == 0)) return size;
        for (int i = 0; i<list.length; i++) {
            if (list[i].isDirectory()) {
                size += getFileSize(list[i]);
            }
            else {
                size += list[i].length();
            }
        }
        return size;
    }
    
    /** converts the array to String separated by delimiter */
    public static String arrayToString(Object[] array, String delimiter ) {
        try {
            if (array == null) return null;
            
            StringBuffer buf = new StringBuffer();
            buf.append(array[0]);
            for (int i = 1; i< array.length; i++) {
                buf.append(delimiter);
                buf.append(array[i]);
            }
            return buf.toString();
        } catch (Exception ex) {
            return array.toString();
        }
    }
    
    /** Returns a String holding the stack trace information printed by printStackTrace() */
    public static String getStackTrace (Throwable t) {
        StringWriter sw = new StringWriter(500);
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
    
    /** Logs the stack trace information printed by printStackTrace() */
    public static void logStackTrace (Log log, Throwable t) {
        String trace = getStackTrace(t);
        log.logEvent(log, Log.DBG, trace);
        log.logEvent(log, Log.ERROR, trace);
    }
    
    /** Logs OS and JVM info */
    public static void logSystemInfo (Log log) {
        log.logEvent(log, Log.DBG, "OS Info: " 
        + System.getProperty("os.name","Unknown")
        + " version " + System.getProperty("os.version","unknown")
        + " running on " + System.getProperty("os.arch","unknown"));
        log.logEvent(log, Log.DBG, "JVM Info: " 
        + System.getProperty("java.version","Unknown")
        + "; " + System.getProperty("java.vm.name","unknown")
        + " "  + System.getProperty("java.vm.version","unknown")
        + "; " + System.getProperty("java.vendor","unknown"));
        log.logEvent(log, Log.DBG, "Java Home: " 
        + System.getProperty("java.home","Unknown"));
        log.logEvent(log, Log.DBG, "System Locale: " + Locale.getDefault().toString());
    }
    
    /** A simple method to copy files. */
    public static void copyFile(File src, File dest) throws Exception {
        try {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dest);
            int c;
            
            while ((c = in.read()) != -1)
                out.write(c);
            
            in.close();
            out.close();
        } catch (FileNotFoundException notFound) {
            throw new Exception("Source or Destination file not found: " + notFound);
        } catch (IOException ioerr) {
            throw new Exception("IO Error copying file " + src.getName());
        }
    }
    
    public static boolean isAboveOrEqualMinimumVersion(String minVersion, String version) {
        if (minVersion == null || minVersion.length() < 3)
            return false;
        if (version == null || version.length() < 3)
            return false;
        Character mv = new Character(minVersion.charAt(0));
        Character v = new Character(version.charAt(0));
        if (v.compareTo(mv)<0)
            return false;
        if (minVersion.charAt(1) != version.charAt(1))
            return false;
        mv = new Character(minVersion.charAt(2));
        v = new Character(version.charAt(2));
        if (v.compareTo(mv)<0)
            return false;
        return true;
    }
    
    /** Check JDK installed by jdkbundle installer. */
    public static boolean checkJdkHome(String jdkHome) {
        File jreDir = new File(jdkHome,File.separator + "jre");
        File jvmJREFile = new File(jreDir, File.separator + "bin" +
                          File.separator + getJVMName());
        File jvmFile = new File(jdkHome, File.separator + "bin" +
                       File.separator + getJVMName());
                                                                                                                                         
        if (!jvmFile.exists() || !jvmJREFile.exists()) {
            return false;
        }
                                                                                                                                         
        RunCommand runCommand = new RunCommand();
        String [] cmdArr = new String[2];
        cmdArr[0] = jvmFile.getAbsolutePath();
        cmdArr[1] = "-version";
        runCommand.execute(cmdArr);
        runCommand.waitFor();
        
        //Look for line starting with "java version"
        String line = "", s = "";
        while (s != null) {
            s = runCommand.getErrorLine();
            if (s.startsWith("java version")) {
                line = s;
                break;
            }
        }
        
        if (line.length() > 0) {
            StringTokenizer st = new StringTokenizer(line.trim());
            String version="";
            while (st.hasMoreTokens()) {
                version = st.nextToken();
            }
            String jdkVersion = LocalizedStringResolver.resolve("org.netbeans.installer.Bundle","JDK.version");
            if (version.equals("\"" + jdkVersion + "\"")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /** Check public JRE installed by jdkbundle installer.
     * Used only on Windows where public JRE is in different directory. */
    public static boolean checkJreHome(String jreHome) {
        File jvmFile = new File(jreHome, File.separator + "bin" + File.separator + getJVMName());
        if (!jvmFile.exists()) {
            return false;
        }
                                                                                                                                         
        RunCommand runCommand = new RunCommand();
        String [] cmdArr = new String[2];
        cmdArr[0] = jvmFile.getAbsolutePath();
        cmdArr[1] = "-version";
        runCommand.execute(cmdArr);
        runCommand.waitFor();
        
        //Look for line starting with "java version"
        String line = "", s = "";
        while (s != null) {
            s = runCommand.getErrorLine();
            if (s.startsWith("java version")) {
                line = s;
                break;
            }
        }
        
        if (line.length() > 0) {
            StringTokenizer st = new StringTokenizer(line.trim());
            String version = "";
            while (st.hasMoreTokens()) {
                version = st.nextToken();
            }
            String jreVersion = LocalizedStringResolver.resolve("org.netbeans.installer.Bundle","JRE.version");
            if (version.equals("\"" + jreVersion + "\"")){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /** Delete given file/folder completely. It is called recursively when necessary.
     * @file - name of file/folder to be deleted
     */
    public static void deleteCompletely (File file, Log log) {
        deleteCompletely(file,true, log);
    }
    
    /** Delete given file/folder completely. It is called recursively when necessary.
     * @file - name of file/folder to be deleted
     * @firstCall - should be true
     */
    private static void deleteCompletely (File file, boolean firstCall, Log log) {
        if (file.isDirectory()) {
            //Delete content of folder
            File [] fileArr = file.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                if (fileArr[i].isDirectory()) {
                    deleteCompletely(fileArr[i],false, log);
                }
                log.logEvent(Util.class, Log.DBG,"Delete file: " + fileArr[i].getPath());
                if (fileArr[i].exists() && !fileArr[i].delete()) {
                    log.logEvent(Util.class, Log.DBG,"Cannot delete file: " + fileArr[i].getPath());
                }
            }
        }
        if (firstCall) {
            log.logEvent(Util.class, Log.DBG,"Delete file: " + file.getPath());
            if (file.exists() && !file.delete()) {
                log.logEvent(Util.class, Log.DBG,"Cannot delete file: " + file.getPath());
            }
        }
    }
                                                                                                                                         
}
