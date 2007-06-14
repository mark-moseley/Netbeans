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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/** Checks that we can do proper logging.
 *
 * @author Jaroslav Tulach
 */
public class LoggingTest extends NbTestCase {
    private Throwable toThrow;
    
    public LoggingTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite();
//        // you can define order of test cases here
//        suite.addTest(new LoggingTest("testLogFileName"));
//        suite.addTest(new LoggingTest("testLogFileNameEqualsToNameOfTest"));
//        suite.addTest(new LoggingTest("testTrimLogFiles"));
        
        NbTestSuite suite = new NbTestSuite(LoggingTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    /** Used in testIOExceptionIsWrappedWithLogMsg. */
    public void throwIOThrowable() throws Throwable {
        Logger log = Logger.getLogger(getName());
        log.warning("Going to throw: " + toThrow);
        throw toThrow;
    }
    
    /** Used in testMyExceptionIsWrappedWithLogMsg. It has to have different
     *  name because it cannot delete log file on 64 bit machine. */
    public void throwMyThrowable() throws Throwable {
        throwIOThrowable();
    }
    
    public void testLogFileName() throws Exception {
        PrintStream ps = getLog("ahoj");
        File f = new File(getWorkDir(), "ahoj");
        assertEquals("Log file exists", true, f.exists());
    }
    
    public void testLogFileNameEqualsToNameOfTest() throws Exception {
        PrintStream ps = getLog();
        File f = new File(getWorkDir(), getName() + ".log");
        assertEquals("Log file exists", true, f.exists());
    }
    
    /** Test of NbTestCase#trimLogFiles method. It should trim size of all files
     * in test case workdir to 1 MB. The method is called at the end of
     * NbTestCase#run method.
     */
    public void testTrimLogFiles() throws IOException {
        StringBuffer buff = new StringBuffer(1024);
        for(int i=0;i<1024;i++) {
            buff.append('A');
        }
        String string1kB = buff.toString();
        for(int i=0;i<2024;i++) {
            log(string1kB);
            log("myLog", string1kB);
            ref(string1kB);
        }
        
        File trimmedDir = getWorkDir();
        String[] filenames = {"testTrimLogFiles.log", "testTrimLogFiles.ref", "myLog" };
        for(int i=0;i<filenames.length;i++) {
            File file = new File(trimmedDir, "TRIMMED_"+filenames[i]);
            assertTrue(file.getName()+" not exists.", file.exists());
            assertTrue(file.getName()+" not trimmed to 1 MB.", file.length() < 2097152L);
            file = new File(trimmedDir, filenames[i]);
            if(file.exists()) {
                // original file exists only if cannot be deleted. Then it has minimal size.
                assertTrue(file.getName()+" not trimmed." + file.length(), file.length() < 1024 * 1024);
            }
        }
    }


    protected Level logLevel() {
        return Level.WARNING;
    }

    public void testLoggingUtil() throws Exception {
        doLoggingUtil(false);
    }

    public void testLoggingUtilWithCleanOfUserDir() throws Exception {
        doLoggingUtil(true);
    }
    
    private void doLoggingUtil(boolean clean) throws Exception {
        Logger log = Logger.getLogger(getName());
        
        log.log(Level.SEVERE, "Ahoj");

        if (clean) {
            clearWorkDir();
            log.log(Level.SEVERE, "Ahojky");
        }
        
        log.log(Level.FINE, "Jardo");
 
        File f = new File(getWorkDir(), getName() + ".log");
        assertEquals("Log file exists", true, f.exists());

        String s = readFile(f);
        if (s.indexOf("Ahoj") == -1) {
            fail("There should be Ahoj\n" + s);
        }
        assertEquals("Not logged for FINE: " + s, -1, s.indexOf("Jardo"));
    }
    
    static String readFile(File f) throws IOException {
        byte[] arr = new byte[(int)f.length()];
        FileInputStream is = new FileInputStream(f);
        int l = is.read(arr);
        assertEquals(l, arr.length);
        
        String s = new String(arr);
        return s;
    }
    public void testFmting() throws Exception {
        Logger log = Logger.getLogger(getName());
        
        LogRecord rec = new LogRecord(Level.SEVERE, "LOG_SevereMsg");
        rec.setResourceBundle(ResourceBundle.getBundle("org.netbeans.junit.TestBundle"));
        rec.setParameters(new Object[] { "Very" });
        log.log(rec);

        File f = new File(getWorkDir(), getName() + ".log");
        assertEquals("Log file exists", true, f.exists());

        byte[] arr = new byte[(int)f.length()];
        FileInputStream is = new FileInputStream(f);
        int l = is.read(arr);
        assertEquals(l, arr.length);

        String s = new String(arr);
        if (s.indexOf("Important message Very") == -1) {
            fail("There should the message\n" + s);
        }
    }
    public void testIOExceptionIsWrappedWithLogMsg() throws Exception {
        
        LoggingTest inner = new LoggingTest("throwIOThrowable");
        inner.toThrow = new IOException("Ahoj");
        
        TestResult res = inner.run();
        assertEquals("One error", 1, res.errorCount());
        assertEquals("No failure", 0, res.failureCount());
        
        TestFailure f = (TestFailure)res.errors().nextElement();
        
        if (f.exceptionMessage().indexOf("Going to throw") == -1) {
            fail("There should be output of the log:\n" + f.exceptionMessage());
        }
    }
    public void testMyExceptionIsWrappedWithLogMsg() throws Exception {
        LoggingTest inner = new LoggingTest("throwMyThrowable");
        
        class MyEx extends Exception {
        }
        
        inner.toThrow = new MyEx();
        
        TestResult res = inner.run();
        assertEquals("One error", 1, res.errorCount());
        assertEquals("No failure", 0, res.failureCount());
        
        TestFailure f = (TestFailure)res.errors().nextElement();
        
        if (f.exceptionMessage() == null || f.exceptionMessage().indexOf("Going to throw") == -1) {
            fail("There should be output of the log:\n" + f.exceptionMessage());
        }
    }
}