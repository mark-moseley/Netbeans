/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * OutWriterTest.java
 * JUnit based test
 *
 * Created on March 21, 2004, 9:50 PM
 */

package org.netbeans.core.output2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.*;
import org.openide.util.Mutex;
import org.openide.windows.OutputWriter;

/** Tests the OutWriter class
 *
 * @author Tim Boudreau
 */
public class OutWriterTest extends TestCase {
    private static final byte[] lineSepBytes = OutWriter.lineSepBytes;
    
    public OutWriterTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(OutWriterTest.class);
        return suite;
    }
    
   
    public void testPositionOfLine() {
        System.out.println("testPositionOfLine");

        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        int pos = ow.positionOfLine(0);
        
        assertTrue ("First line position should be 0 but is " + pos, pos == 0);
        
        int expectedPosition = first.length() + lineSepBytes.length - 1;
        pos = ow.positionOfLine(1);
        
        assertTrue ("Second line position should be length of first (" + first.length() + ") + line " +
            "separator length (" + lineSepBytes.length + "), which should be " + 
            expectedPosition + " but is " + pos, 
            pos == expectedPosition);
        
        pos = ow.positionOfLine (2);
        int targetPos = first.length() + second.length() + (lineSepBytes.length * 2) - 2;
        
        assertTrue ("Third line position should be " + targetPos + " but is " +
            pos, pos == targetPos);
    }
    
    
    public void testPosition() {
        System.out.println("testPosition");
        
        OutWriter ow = new OutWriter();

        
        String first = "This is the first string";
        String second ="This is the second string";
        String third = "This is the third string";
        
        assertTrue (ow.lineCount() == 0);
        
        ow.println(first);
        
        assertTrue (ow.lineCount() == 1);
        
        ow.println (second);
        
        assertTrue (ow.lineCount() == 2);
        
        int targetLength = first.length() + second.length() + 2;

        assertTrue ( 
            "After printing strings with length " + first.length() + " and " + 
            second.length() + " outfile position should be " + targetLength +
            " not " + ow.charsWritten(),
            ow.charsWritten() == targetLength);
        
        ow.println (third);
        
        targetLength = first.length() + second.length() + third.length() + 
            3;
        
        assertTrue ("Length should be " + targetLength + " but position is "
            + ow.charsWritten(), targetLength == ow.charsWritten());        
    }
    
    public void testLine() {
        System.out.println("testLine");
        
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        assertTrue ("After writing 3 lines, linecount should be 3, not " + 
            ow.lineCount(), ow.lineCount() == 3);
        
        String firstBack = null;
        String secondBack = null;
        String thirdBack = null;
        try {
            firstBack = ow.line(0);
            secondBack = ow.line(1);
            thirdBack = ow.line(2);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail (ioe.getMessage());
        }
        
        String firstExpected = first + "\n";
        String secondExpected = second + "\n";
        String thirdExpected = third + "\n";
        
        assertEquals("First string should be \"" + firstExpected + "\" but was \"" + firstBack + "\"",
            firstBack, firstExpected);
        
        assertEquals("Second string should be \"" + secondExpected + "\" but was \"" + secondBack + "\"",
            secondBack, secondExpected);

        assertEquals("Third string should be \"" + thirdExpected + "\" but was \"" + thirdBack + "\"",
            thirdBack, thirdExpected);
        
    }
     
    public void testLineForPosition() {
        System.out.println("testLineForPosition");
        
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        int line = ow.lineForPosition (first.length() / 2);
        
        assertTrue ("Position halfway through first line should map to line 0," +
            " not " + line,
            line == 0);
        
        line = ow.lineForPosition (first.length() + lineSepBytes.length + 
            (second.length() / 2));
        
        assertTrue ("Position halfway through line 1 should map to line 1, not " +
            line,
            line == 1);
        
        //XXX do some more tests here for very large buffers, to ensure no
        //off-by-ones
        
    }
    
    public void testLineCount() {
        System.out.println("testLineCount");
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();
        try {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                System.currentTimeMillis();
             }
        });
        } catch (Exception e) {}
        Thread.currentThread().yield();
        
        assertTrue ("Linecount should be 3 after printing 3 lines, not " +
            ow.lineCount(), ow.lineCount()==3);
    }
    
    public void testAddChangeListener() {
        System.out.println("testAddChangeListener");
        OutWriter ow = new OutWriter ();
        
        CL cl = new CL();
        try {
            ow.addChangeListener (cl);
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Caught exception " + e);
        }
        
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();        
        
        cl.assertChanged();
        
    }
    
    public void testMultilineText() {
        System.out.println("testMultilineText");
        OutWriter ow = new OutWriter ();
        String threeLines = "This is\nthree lines of\nText";
        ow.println(threeLines);
        assertTrue ("Line count should be 3, not " + ow.lineCount(), ow.lineCount() == 3);
        ow.println("This is another line");
        assertTrue ("Line count should be 4, not " + ow.lineCount(), ow.lineCount() == 4);
        ow.println(threeLines);
        assertTrue ("Line count should be 7, not " + ow.lineCount(), ow.lineCount() == 7);
    }
    
    public void testRemoveChangeListener() {
        System.out.println("testRemoveChangeListener");
        
        
        
        OutWriter ow = new OutWriter ();
        
        CL cl = new CL();
        try {
            ow.addChangeListener (cl);
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Caught exception " + e);
        }
        
        
        ow.removeChangeListener (cl);
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();        
        
        cl.assertNoChange();
    }
    
    public void testCheckDirty() {
        System.out.println("testCheckDirty");
        
        
        OutWriter ow = new OutWriter ();
        
        boolean dirty = ow.checkDirty();
        
        String first = "This is the a test";
        
        ow.println(first);
        
        
        //plan to delete checkDirty
    }
    
    public void testSubstring() {
        System.out.println("testSubstring");
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        //First test intra-line substrings
        
        String expected = first.substring(5, 15);
        String gotten = ow.substring (5, 15);
        System.err.println("\nGot " + gotten + "\n");
        
        assertEquals ("Should have gotten string \"" + expected + "\" but got \"" + gotten + "\"", expected, gotten);
        
        
    }    
    
    public void testPrintln() {
        System.out.println("testPrintln");

        try {
            OutWriter ow = new OutWriter ();

            String first = "This is a test string";

            ow.println(first);
            ow.flush();
            
            String firstExpected = first + "\n";
            String firstReceived = ow.line(0);
            
            assertEquals ("First line should be \"" + firstExpected + "\" but was \"" + firstReceived + "\"", firstExpected, firstReceived);
        
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
        
    }
    
    public void testReset() {
        System.out.println("testReset");
        
    }
    
    public void testFlush() {
        System.out.println("testFlush");
        
    }
    
    public void testClose() {
        System.out.println("testClose");
        
    }

    public void testCheckError() {
        System.out.println("testCheckError");
        
    }

    public void testSetError() {
        System.out.println("testSetError");
        
    }

    public void testWrite() {
        System.out.println("testWrite");
        
    }
    


   
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
    private class CL implements ChangeListener {
        
        public void assertChanged () {
            ChangeEvent oldCE = ce;
            ce = null;
            assertTrue ("No change happened", oldCE != null);
        }
        
        public void assertNoChange() {
            ChangeEvent oldCE = ce;
            ce = null;
            assertFalse ("Change happened", oldCE != null);
        }
        
        private ChangeEvent ce = null;
        public void stateChanged(ChangeEvent changeEvent) {
            ce = changeEvent;
        }
        
    }
     
    
}
