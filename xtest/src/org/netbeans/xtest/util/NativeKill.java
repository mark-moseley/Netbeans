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

/*
 * NativeKill.java
 *
 * methods in this class are able to run and execute native kill utility on all platforms 
 * (on windows only with supplied exe utility)
 * Created on April 16, 2002, 6:10 PM
 */

package org.netbeans.xtest.util;

import java.io.*;

/**
 *
 * @author  mb115822
 */
public class NativeKill {
    
    /** Creates a new instance of NativeKill */
    public NativeKill() {
    }
    
    private static final String UNIX = "unix";
    private static final String WINDOWS = "windows";
    private static final String UNKNOWN = "unknown";
    
    private static final String [][] SUPPORTED_PLATFORMS = { 
        {"Linux,i386",UNIX},
        {"SunOS,sparc",UNIX},
        {"Windows_NT,x86",WINDOWS},
        {"Windows_2000,x86",WINDOWS},
        {"Windows_XP,x86",WINDOWS},
        {"Windows_95,x86",WINDOWS},
        {"Windows_98,x86",WINDOWS},        
        {"Windows_Me,x86",WINDOWS} 
    };
    
    
    // get platform on which the code is executed 
    private static String getPlatform() {
        
        String platformString=(System.getProperty("os.name","")+","+
                        /*
                        System.getProperty("os.version","")+","+
                         */
                        System.getProperty("os.arch","")).replace(' ','_');        
        for (int i=0; i<SUPPORTED_PLATFORMS.length; i++) {
            if (platformString.equalsIgnoreCase(SUPPORTED_PLATFORMS[i][0])) {
                return SUPPORTED_PLATFORMS[i][1];
            }
        } 
        return UNKNOWN;
    }
    
    // execute kill 
    private static boolean executeKillCommand(String killCommand) throws IOException {
        try {
            Process kill = Runtime.getRuntime().exec(killCommand);
            kill.waitFor();
            int exitValue = kill.exitValue();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException ie) {
            System.out.println("InterruptedException when killing:"+ie);
        }
        return false;
    }
    
    // call kill -9 pid on unixes
    private static boolean killOnUnix(long pid) throws IOException {
        // need to do !!!
        // should be kill on a default path ?
        // yes - otherwise it will not work
        String killCommand = "kill -9 "+pid;
        return executeKillCommand(killCommand);
    }
    
    // call kill.exe pid utilitity supplied with xtest on windows
    private static boolean killOnWindows(long pid) throws IOException {
        // need to do !!!
        String xtestHome = System.getProperty("xtest.home");
        if (xtestHome != null) {
            File killFile = new File(xtestHome,"lib/kill.exe");
            //if (killFile.isFile()) {
                String killPath = killFile.getAbsolutePath();
                String killCommand = killPath+" "+pid;
                return executeKillCommand(killCommand);
            //}
        } else {
            throw new IOException("xtest.home system property not set - cannot find kill distributed with XTest on windows");
        }
    }
    
    /*
     * kills process with given pid 
     */
    public static boolean killProcess(long pid) {
        String platform = getPlatform();
        try {
            if (platform.equals(UNIX)) {
                return killOnUnix(pid);
                } else {
                if (platform.equals(WINDOWS)) {
                    return killOnWindows(pid);
                }
            }
        } catch (IOException ioe) {
            System.out.println("Kill command not found on your computer");
            System.out.println(ioe);
        }
        // not supported platform - let's return just false
        // but I should evaluate throwing an Exception !!!
        return false;
        
    }   

}
