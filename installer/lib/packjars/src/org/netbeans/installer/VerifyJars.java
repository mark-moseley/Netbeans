/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

/**
 *
 * This class is used to verify class files in jars after pack/unpack is performed.
 * All classes are loaded by custom class loader to make sure class files are not corrupted.
 * Verification is split into 2 separate runs. First run tests IDE jars. Second
 * run tests Jakarta Tomcat jars.
 *
 * @author Marek Slama
 *
 */

public class VerifyJars {

    List<File> jarList = new ArrayList<File>();

    URL [] classPath;

    private static File inputDir;

    private static int testType = 0;

    private static final int TEST_IDE_JARS    = 0;
    private static final int TEST_TOMCAT_JARS = 1;

    private static int inputDirLength;

    public VerifyJars () {
    }

    /**
     *  Input parameters:
     *  1. Directory to be scanned
     *  2. Type of test - 0 test IDE jars; 1 test Tomcat jars
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: 2 input parameters are required.");
            System.exit(1);
        }

        inputDir = new File(args[0]);

        inputDirLength = inputDir.getAbsolutePath().length();

        try {
            testType = Integer.parseInt(args[1]);
        } catch (NumberFormatException exc) {
            System.out.println("Error: Cannot parse 2nd input parameter '" + args[1] + "'");
        }

        VerifyJars scan = new VerifyJars();
        scan.scanDir(inputDir);
        try {
            scan.getClassPath();
        } catch (MalformedURLException exc) {
            exc.printStackTrace();
        }
        scan.verifyJars();
    }
    
    private void scanDir (File dir) {
        File [] arr = dir.listFiles();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].isDirectory()) {
                scanDir(arr[i]);
            } else {
                if (arr[i].getName().endsWith(".jar")) {
                    int pos = arr[i].getAbsolutePath().indexOf("enterprise4/jakarta-tomcat");
                    if (testType == TEST_IDE_JARS) {
                        //Test IDE jars only, exclude Tomcat jars
                        if (pos == -1) {
                            jarList.add(arr[i]);
                        }
                    } else {
                        //Test Tomcat jars only, exclude IDE jars
                        if (pos != -1) {
                            jarList.add(arr[i]);
                        }
                    }
                }
            }
        }
    }

    private void verifyJars () {
        ClassLoader parent = this.getClass().getClassLoader().getParent();
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(classPath, parent);
            int tChecked = 0, tPassed = 0, tError = 0;
            for (int i = 0; i < jarList.size(); i++) {
                JarFile jarFile = new JarFile(jarList.get(i));
                String s = jarList.get(i).getAbsolutePath();
                s = s.substring(inputDirLength + 1, s.length());
                System.out.println("Verifying: " + s);
                int iChecked = 0, iPassed = 0, iError = 0;
                for (Enumeration en = jarFile.entries(); en.hasMoreElements(); ) {
                    JarEntry je = (JarEntry) en.nextElement();
                    //System.out.println("je.name: " + je.getName());
                    if (je.getName().endsWith(".class")) {
                        String name = je.getName().substring(0,je.getName().length() - ".class".length());
                        //Hack for unusual jar structure in NB 5.5
                        if (jarList.get(i).getAbsolutePath().endsWith("ide7/modules/ext/jaxws20/jaxb-xjc.jar") &&
                            je.getName().startsWith("1.0")) {
                            System.out.println("++");
                            System.out.println("++ Skip entry: " + je.getName());
                            continue;
                        }
                        name = name.replace('/','.');
                        //System.out.println("Class: " + name);
                        try {
                            iChecked++;
                            tChecked++;
                            Class clazz = Class.forName(name,false,urlClassLoader);
                            iPassed++;
                            tPassed++;
                        } catch (NoClassDefFoundError exc) {
                            iError++;
                            tError++;
                            System.out.println("--");
                            System.out.println("-- NoClassDefFoundError: " + exc.getMessage());
                        } catch (IllegalAccessError exc) {
                            iError++;
                            tError++;
                            System.out.println("--");
                            System.out.println("-- IllegalAccessError: " + exc.getMessage());
                        } catch (IncompatibleClassChangeError exc) {
                            iError++;
                            tError++;
                            System.out.println("--");
                            System.out.println("-- IncompatibleClassChangeError: " + exc.getMessage());
                        } catch (SecurityException exc) {
                            iError++;
                            tError++;
                            System.out.println("--");
                            System.out.println("-- SecurityException: " + exc.getMessage());
                        } catch (ClassFormatError exc) {
                            System.out.println("FATAL ERROR");
                            System.out.println("-- ClassFormatError: " + exc.getMessage());
                            System.exit(2);
                        } catch (ClassNotFoundException exc) {
                            System.out.println("FATAL ERROR");
                            System.out.println("-- ClassNotFoundException: " + exc.getMessage());
                            System.exit(2);
                        }
                    }
                }
                System.out.println("Checked:" + iChecked + " Passed:" + iPassed + " Error:" + iError);
            }
            System.out.println("Total checked:" + tChecked + " Total passed:" + tPassed + " Total error:" + tError);
        } catch (IOException exc) {
            exc.printStackTrace();
            System.exit(2);
        }
    }

    private void getClassPath () throws MalformedURLException {
        classPath = new URL[jarList.size()];
        for (int i = 0; i < jarList.size(); i++) {
            String s = jarList.get(i).getAbsolutePath();
            //For Windows replace "\" in path by "/" to get correct URL.
            if (System.getProperty("os.name").startsWith("Windows")) {
                s = s.replace('\\','/');
            } else {
                s = s;
            }
            //Replace spaces in URL
            if (System.getProperty("os.name").startsWith("Windows")) {
                //On Windows path starts with disk like C: so we must add additional slash
                s = "file:///" + s.replaceAll(" ","%20");
            } else {
                s = "file://" + s.replaceAll(" ","%20");
            }
            classPath[i] = new URL(s);
        }
        //Dump class path
        for (int i = 0; i < classPath.length; i++) {
            System.out.println("cl[" + i + "]: " + classPath[i].toString());
        }
    }
    
}
