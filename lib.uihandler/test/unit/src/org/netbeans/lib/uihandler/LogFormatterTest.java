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
package org.netbeans.lib.uihandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jindrich Sedek
 */
public class LogFormatterTest extends NbTestCase {
    
    public LogFormatterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testFormat() throws IOException {
        LogRecord rec = new LogRecord(Level.SEVERE, "PROBLEM");
        Throwable thrown = new NullPointerException("TESTING");
        thrown.initCause(new AssertionError("CAUSE PROBLEM"));
        rec.setThrown(thrown);
        String result = new LogFormatter().format(rec);
        assertTrue(result.contains("java.lang.NullPointerException: TESTING"));
        assertTrue(result.contains("<level>SEVERE</level>"));
        assertTrue(result.contains("<method>testFormat</method>"));
        assertTrue(result.contains("<message>java.lang.AssertionError: CAUSE PROBLEM</message>"));
        assertTrue(result.contains("<more>19</more>"));
        assertTrue(result.contains(" <class>junit.framework.TestSuite</class>"));
        assertTrue(result.contains("<class>sun.reflect.NativeMethodAccessorImpl</class>"));
        assertFalse(result.contains("<more>20</more>"));
    }
        
    
    public void testEasy() throws IOException {
        Throwable thrown = new NullPointerException("TESTING");
        thrown.initCause(new AssertionError("CAUSE PROBLEM"));
        formatAndScan(thrown);
    }
    
    public void testManyCausesFormat() throws IOException{
        try{
            generateIOException();
        }catch(IOException exc){
            formatAndScan(exc);
        }
    }
    
    /**
     * test whether the result of LogFormatter is the same as XMLFormatter 
     * if there is no nested exception
     */
    public void testXMLFormatterDifference(){
        LogRecord rec = new LogRecord(Level.SEVERE, "PROBLEM");
        LogFormatter logFormatter = new LogFormatter();
        XMLFormatter xmlFormatter = new XMLFormatter();
        String logResult = logFormatter.format(rec);
        String xmlResult = xmlFormatter.format(rec);
        assertEquals("WITHOUT THROWABLE", xmlResult, logResult);
        rec.setThrown(new NullPointerException("TESTING EXCEPTION"));
        rec.setResourceBundleName("MUJ BUNDLE");
        logResult = logFormatter.format(rec);
        //remove file names
        logResult = logResult.replaceAll("      <file>.*</file>\n", "");
        xmlResult = xmlFormatter.format(rec);
        assertEquals("WITH THROWABLE", xmlResult, logResult);
    }
    
    private void formatAndScan(Throwable thr) throws IOException{
        ByteArrayOutputStream oStream = new ByteArrayOutputStream(1000);
        LogRecord rec = new LogRecord(Level.SEVERE, "PROBLEM");
        rec.setThrown(thr);
        LogRecords.write(oStream, rec);//write to stream
        ByteArrayInputStream iStream = new ByteArrayInputStream(oStream.toByteArray());
        Formatter formatter = new LogFormatter();
        LogRecord readFromStream = new TestHandler(iStream).read();// read from stream
        //read by handler equals the writen by formatter
        assertEquals(formatter.format(readFromStream), formatter.format(rec));    
        oStream.reset();
        thr.printStackTrace(new PrintStream(oStream));
        String writen = oStream.toString();
        oStream.reset();
        rec.getThrown().printStackTrace(new PrintStream(oStream));
        String read = oStream.toString();
        assertEquals(writen, read);//both stacktraces are the same        
    }
    
    private void generateIOException()throws IOException{
        try{
            generateSQL();
        }catch(SQLException error){
            IOException except = new IOException("IO EXCEPTION");
            except.initCause(error);
            throw except;
        }
    }
            
    private void generateSQL() throws SQLException{
        try{
            generateClassNotFoundException();
        }catch(ClassNotFoundException exception){
            SQLException except = new SQLException("SQL TESTING EXCEPTION");
            except.initCause(exception);
            throw except;
        }
    }
    
    private void generateClassNotFoundException() throws ClassNotFoundException{
        java.lang.Class.forName("unknown name");
    }                  
}


