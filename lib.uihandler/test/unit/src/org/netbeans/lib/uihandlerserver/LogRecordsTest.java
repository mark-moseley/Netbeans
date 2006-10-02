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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.LogRecords;

/**
 *
 * @author Jaroslav Tulach
 */
public class LogRecordsTest extends NbTestCase {
    
    public LogRecordsTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.INFO;
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testWriteAndRead() throws Exception {
        doWriteAndReadTest(System.currentTimeMillis());
    }
    
    public void testFailureOn1159804485342() throws Exception {
        doWriteAndReadTest(1159804485342L);
    }

    private void doWriteAndReadTest(long seed) throws Exception {
        Logger.getAnonymousLogger().info("seed is: " + seed);
        
        File file = new File(getWorkDir(), "feed.txt");
        Random r = new Random(seed);
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        
        
        int cnt = r.nextInt(500);
        LogRecord[] arr = new LogRecord[cnt];
        for (int i = 0; i < cnt; i++) {
            LogRecord rec = generateLogRecord(r);
            arr[i] = rec;
            LogRecords.write(out, rec);
        }
        out.close();
        

        DataInputStream in = new DataInputStream(new FileInputStream(file));
        for (int i = 0; i < cnt; i++) {
            LogRecord rec = LogRecords.read(in);
            assertLog(i + "-th record is the same", rec, arr[i]);
        }
        in.close();
    }

    private LogRecord generateLogRecord(Random r) {
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
                    fail("Logs differ in result of " + m.getName() + "\nrec1: " + r(r1) + "\nrec2: " + r(r2));
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

    private static String randomString(Random r) {
        int len = r.nextInt(50);
        byte[] arr = new byte[len];
        r.nextBytes(arr);
        return new String(arr);
    }
}
