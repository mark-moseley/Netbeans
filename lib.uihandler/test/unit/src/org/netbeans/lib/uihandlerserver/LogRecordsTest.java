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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandlerserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.lib.uihandler.LogRecords;

/**
 *
 * @author Jaroslav Tulach
 */
public class LogRecordsTest extends NbTestCase {
    private Logger LOG;
    
    public LogRecordsTest(String testName) {
        super(testName);
    }
    
    
    public static Test suite() {
        return new NbTestSuite(LogRecordsTest.class);
//        return new LogRecordsTest("testReadException");
    }
    
    protected Level logLevel() {
        return Level.FINEST;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected void tearDown() throws Exception {
    }

    public void testParamsGetCleared() throws Exception {
        String r = 
            "<record>" +
            "<date>2006-11-17T10:16:14</date>" +
            "<millis>1163729774285</millis>" +
            "<sequence>20</sequence>" +
            "<level>INFO</level>" +
            "<thread>12</thread>" +
            "<message>MSG</message>" +
            "<key>MSG</key>" +
            "<catalog>a.bundle.somewhere</catalog>" +
            "<param>1</param>" +
            "</record>" +
            
            "<record>" +
            "<date>2006-11-17T10:16:14</date>" +
            "<millis>1163729774285</millis>" +
            "<sequence>20</sequence>" +
            "<level>INFO</level>" +
            "<thread>12</thread>" +
            "<message>MSG</message>" +
            "<key>MSG</key>" +
            "<catalog>a.bundle.somewhere</catalog>" +
            "<param>2</param>" +
            "</record>";
        
        class H extends Handler {
            int cnt;
            
            public void publish(LogRecord arg0) {
                cnt++;
                    assertNotNull("We have params " + cnt, arg0.getParameters());
                assertEquals("One argument for " + cnt + "th record", 1, arg0.getParameters().length);
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        H h = new H();
        
        LogRecords.scan(new ByteArrayInputStream(r.getBytes()), h);

        assertEquals("Two records", 2, h.cnt);
    }
    
    public void testWriteAndRead() throws Exception {
        doWriteAndReadTest(System.currentTimeMillis());
    }
    
    public void testNewFailureOn1165572711706() throws Exception {
        doWriteAndReadTest(1165572711706L);
    }
    
    public void testFailureOn1159804485342() throws Exception {
        doWriteAndReadTest(1159804485342L);
    }
    
    public void testMakeSureItIsReadable() throws Exception {
        InputStream is = getClass().getResourceAsStream("NB1216449736.xml");
        int cnt = 0;
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = LogRecords.read(is);
            if (r == null) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
    }
    
    public void testReadException() throws Exception {
        String what = "NB1216449736.xml";
        
        InputStream is = getClass().getResourceAsStream(what);
        int cnt = 0;
        
        class H extends Handler {
            int cnt;
            LogRecord first;
            
            public void publish(LogRecord record) {
                if (cnt == 0) {
                    first = record;
                }
                cnt++;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        LogRecord first = null;
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = LogRecords.read(is);
            if (r == null) {
                break;
            }
            if (first == null) {
                first = r;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
        
        H h = new H();
        is = getClass().getResourceAsStream(what);
        LogRecords.scan(is, h);
        is.close();
        
        assertNotNull(first);
        assertNotNull(h.first);
        assertEquals("Same message", first.getMessage(), h.first.getMessage());
        assertNotNull("exception from read", first.getThrown());
        assertNotNull("exception from scan", h.first.getThrown());
        assertEquals("Same exception message", first.getThrown().getMessage(), h.first.getThrown().getMessage());
        
        StackTraceElement[] arr1 = first.getThrown().getStackTrace();
        StackTraceElement[] arr2 = h.first.getThrown().getStackTrace();
        
        assertEquals("Same length", arr1.length, arr2.length);

        for (int i = 0; i < arr1.length; i++) {
            if (!arr1[i].equals(arr2[i])) {
                fail(i + " th stack differ: " + arr1[i] + " != " + arr2[i] + "\nline: " + arr1[i].getLineNumber() + " and " + arr2[i].getLineNumber());
            }
        }
        
        
        assertEquals("The same amount of records", cnt, h.cnt);
        
    }
    
    public void testCanReadEmpty() throws Exception {
        InputStream is = getClass().getResourceAsStream("Empty.xml");
        int cnt = 0;
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = LogRecords.read(is);
            if (r == null) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
        
        assertEquals("No records", 0, cnt);
    }
    public void testMakeSureItIsScannable() throws Exception {
        InputStream is = getClass().getResourceAsStream("NB1216449736.xml");
        int cnt = 0;
        
        class H extends Handler {
            int cnt;
            
            public void publish(LogRecord record) {
                cnt++;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = LogRecords.read(is);
            if (r == null) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
        
        H h = new H();
        is = getClass().getResourceAsStream("NB1216449736.xml");
        LogRecords.scan(is, h);
        is.close();
        
        assertEquals("The same amount of records", cnt, h.cnt);
    }
    public void testBadUserIsBad() throws Exception {
        String what = "baduser.xml";
        InputStream is = getClass().getResourceAsStream(what);
        int cnt = 0;
        
        class H extends Handler {
            int cnt;
            
            public void publish(LogRecord record) {
                cnt++;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = LogRecords.read(is);
            if (r == null) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
        
        H h = new H();
        is = getClass().getResourceAsStream(what);
        LogRecords.scan(is, h);
        is.close();
        
        assertEquals("The same amount of records", cnt, h.cnt);
    }
    
    

    public void testDoesNotAskForWrongBunles() throws Exception {
        LogRecord rec = new LogRecord(Level.FINER, "UI_ACTION_BUTTON_PRESS"); // NOI18N
        rec.setParameters(new Object[] { "0", "1" });
        rec.setResourceBundle(ResourceBundle.getBundle(LogRecordsTest.class.getPackage().getName() + ".Props"));
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LogRecords.write(os, rec);
        os.close();
        
        
        class H extends Handler {
            int cnt;
            
            public void publish(LogRecord arg0) {
                cnt++;
                assertNotNull("We have params " + cnt, arg0.getParameters());
                assertEquals("Two argument for " + cnt + "th record", 2, arg0.getParameters().length);
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        H h = new H();
        
        CharSequence log = Log.enable("", Level.INFO);
        LogRecords.scan(new ByteArrayInputStream(os.toByteArray()), h);

        assertEquals("One record", 1, h.cnt);
        
        if (log.toString().indexOf("Cannot find resource") < 0) {
            fail(log.toString());
        }
    }
    
    public void testScanEmpty91974() throws Exception {
        String what = "uigestures-iz91974.xml";
        InputStream is = getClass().getResourceAsStream(what);
        int cnt = 0;
        
        class H extends Handler {
            int cnt;
            
            public void publish(LogRecord record) {
                cnt++;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = LogRecords.read(is);
            if (r == null) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
        
        H h = new H();
        is = getClass().getResourceAsStream(what);
        LogRecords.scan(is, h);
        is.close();
        
        assertEquals("The same amount of records", cnt, h.cnt);
    }
    public void testNotFinishedFiles() throws Exception {
        String what = "eof.xml";
        InputStream is = getClass().getResourceAsStream(what);
        int cnt = 0;
        
        class H extends Handler {
            int cnt;
            
            public void publish(LogRecord record) {
                cnt++;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r;
            try {
                r = LogRecords.read(is);
            } catch (EOFException ex) {
                assertNull("Next read is null", LogRecords.read(is));
                break;
            }
            if (r == null) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
        
        H h = new H();
        is = getClass().getResourceAsStream(what);
        LogRecords.scan(is, h);
        is.close();
        
        assertEquals("The same amount of records", cnt, h.cnt);
    }
    public void testScanFileThatClaimsTohaveWrongUTF8Char() throws Exception {
        InputStream is = getClass().getResourceAsStream("wrongutfchar.xml");
        int cnt = 0;
        
        class H extends Handler {
            int cnt;
            
            public void publish(LogRecord record) {
                cnt++;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = LogRecords.read(is);
            if (r == null) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
        
        H h = new H();
        is = getClass().getResourceAsStream("wrongutfchar.xml");
        LogRecords.scan(is, h);
        is.close();
        
        assertEquals("The same amount of records", cnt, h.cnt);
    }
    
    private void doWriteAndReadTest(long seed) throws Exception {
        Logger.getAnonymousLogger().info("seed is: " + seed);
        
        File file = new File(getWorkDir(), "feed.txt");
        Random r = new Random(seed);
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        
        
        int cnt = r.nextInt(500);
        final LogRecord[] arr = new LogRecord[cnt];
        for (int i = 0; i < cnt; i++) {
            LogRecord rec = generateLogRecord(r);
            arr[i] = rec;
            LogRecords.write(out, rec);
        }
        out.close();
        

        {
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            for (int i = 0; i < cnt; i++) {
                LogRecord rec = LogRecords.read(in);
                assertLog(i + "-th record is the same", rec, arr[i]);
            }
            in.close();
        }
        
        class H extends Handler {
            int cnt;
            
            public void publish(LogRecord rec) {
                try {
                    assertLog(cnt + "-th record is the same", rec, arr[cnt]);
                } catch(Exception ex) {
                    throw (RuntimeException)new RuntimeException().initCause(ex);
                }
                cnt++;
            }

            public void flush() {
                assertEquals("All read", cnt, arr.length);
                cnt = -1;
            }

            public void close() throws SecurityException {
            }
        }
        
        H h = new H();
        {
            LOG.info("Scanning " + file);
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            LogRecords.scan(in, h);
            in.close();
        }
        assertEquals("Cleared", -1, h.cnt);
    }
    
    private LogRecord generateLogRecord(Random r) throws UnsupportedEncodingException {
        LogRecord rec = new LogRecord(randomLevel(r), randomString(r));
        return rec;
    }

    private void assertLog(String string, LogRecord r1, LogRecord r2) throws Exception {
        if (r1 == null && r2 != null) {
            fail("r1: null r2 not: " + r(r2));
        }
        if (r1 != null && r2 == null) {
            fail("r2: null r1 not: " + r(r2));
        }
        
        for (Method m : LogRecord.class.getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                Object o1 = m.invoke(r1);
                Object o2 = m.invoke(r2);
                
                if (o1 == null && o2 == null) {
                    continue;
                }
                if (o1 == null || o2 == null || !o1.equals(o2)) {
                    assertEquals(
                        "Logs differ in result of " + m.getName() + "\nrec1: " + r(r1) + "\nrec2: " + r(r2),
                        o1, o2
                    );
                }
            }
        }
    }
    
    private static String r(LogRecord r) {
        return r.getMessage();
    }

    private static Level randomLevel(Random r) {
        int lev = r.nextInt(1100);
        if (lev >= Level.SEVERE.intValue()) return Level.SEVERE;
        if (lev >= Level.WARNING.intValue()) return Level.WARNING;
        if (lev >= Level.INFO.intValue()) return Level.INFO;
        if (lev >= Level.CONFIG.intValue()) return Level.CONFIG;
        if (lev >= Level.FINE.intValue()) return Level.FINE;
        if (lev >= Level.FINER.intValue()) return Level.FINER;
        if (lev >= Level.FINEST.intValue()) return Level.FINEST;
        return Level.OFF;
    }

    private static String randomString(Random r) throws UnsupportedEncodingException {
        int len = r.nextInt(50);
        byte[] arr = new byte[len];
        for (int i = 0; i < arr.length; i++) {
            int ch = r.nextInt(256);
            if (ch < 32) {
                ch = 32;
            }
            if (ch > 'z') {
                ch = 'z';
            }
            arr[i] = (byte)ch;
        }
        return new String(new String(arr, "utf-8").getBytes(), "utf-8");
    }
}
