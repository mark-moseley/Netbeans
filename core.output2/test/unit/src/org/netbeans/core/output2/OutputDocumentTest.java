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
 * OutputDocumentTest.java
 * JUnit based test
 *
 * Created on March 23, 2004, 5:34 PM
 */

package org.netbeans.core.output2;

import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import junit.framework.*;

/**
 *
 * @author tim
 */
public class OutputDocumentTest extends TestCase {
    
    public OutputDocumentTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(OutputDocumentTest.class);
        return suite;
    }

    
    public void testAddDocumentListener() {
        System.out.println("testAddDocumentListener");

        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        ODListener od = new ODListener();
        doc.addDocumentListener(od);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
    
        od.assertChanged();
    }
    
    public void testDocumentEvents() throws Exception {
        System.out.println("testDocumentEvents");

        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        ODListener od = new ODListener();
        doc.addDocumentListener(od);
        
        for (int i=0; i < 100; i++) {
            ow.println("This is string " + i);
        }
        
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run(){
                System.currentTimeMillis();
            }
        });
        Thread.currentThread().sleep(1500);
        
        DocumentEvent de = od.getEvent();
        assertTrue ("Event should have been fired", de != null);
        
        int elCount = doc.getElementCount();
        
        assertTrue ("Element count should be 100 after printing 100 lines, " +
            "not " + elCount, 
            elCount == 100);
        
        DocumentEvent.ElementChange ec = de.getChange(doc);
        
        Element[] added = ec.getChildrenAdded();
        assertNotNull("Children added should not be null", added);
//        assertTrue("Number of children added should be 100, not " + added.length, added.length == 100);
//        assertTrue ("Index of change should be 0, not " + ec.getIndex(), ec.getIndex() == 0);
    }
    
    public void testCreatePosition() {
        System.out.println("testCreatePosition");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        Position pos = null;
        try {
            pos = doc.createPosition (15);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
            fail ("Unexpected BadLocationException: " + ble.getMessage());
        }
        assertNotNull (pos);
        
        assertTrue (pos.getOffset() == 15);
        
        BadLocationException ble = null;
        try {
            pos = doc.createPosition (65543);
        } catch (BadLocationException e) {
            ble = e;
        }
        assertNotNull("Exception should have been thrown when creating a bogus location", ble);
        
        try {
            pos = doc.createPosition (-2);
        } catch (BadLocationException e) {
            ble = e;
        }
        assertNotNull("Exception should have been thrown when creating a bogus negative location", ble);
    }
    
    public void testGetDefaultRootElement() {
        System.out.println("testGetDefaultRootElement");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        Element el = doc.getDefaultRootElement();
        assertNotNull ("Root element should not be null", el);
        
        assertTrue ("Root offset should be 0", el.getStartOffset() == 0);
        assertTrue ("Root ending char should be count of written chars", el.getEndOffset() == ow.charsWritten());
        assertTrue ("Wrong document object from default root element's getDocument method", el.getDocument() == doc);
        assertTrue ("Element count of the root element should be the line count", el.getElementCount() == ow.lineCount());
        
        
    }
    
    public void testGetEndPosition() {
        System.out.println("testGetEndPosition");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.flush();

        Position pos = doc.getEndPosition();
        
        int offset = pos.getOffset();
        
        assertTrue ("End offset should match number of characters written", offset == ow.charsWritten());
        
        ow.println (third);
        ow.flush();

        assertTrue ("Document end offset should change after writing more data", offset != pos.getOffset());
        assertTrue ("End offset should match number of characters written", pos.getOffset() == ow.charsWritten());
        
    }
    
    public void testGetLength() {
        System.out.println("testGetLength");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        int expectedLength = first.length() + second.length() + third.length() + 3;
        int receivedLength = doc.getLength();
        
        assertTrue ("Number of characters counting carriage returns should be " 
            + expectedLength + " but was " + receivedLength, expectedLength == receivedLength);
        
    }
    
    public void testGetRootElements() {
        System.out.println("testGetRootElements");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        Element[] el = doc.getRootElements();
        assertTrue ("Should be only one root element", el.length == 1);
        assertTrue ("Root element should be the document", el[0] == doc);
        
    }
    
    public void testGetStartPosition() {
        System.out.println("testGetStartPosition");
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        Position p = doc.getStartPosition();
        
        assertTrue ("Start position should be start offset", p.getOffset() == doc.getStartOffset());
        assertTrue ("Start offset should be 0", p.getOffset() == 0);
        
    }
    
    public void testGetText() {
        System.out.println("testGetText");
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        ow.println(first);
        ow.flush();
        String received = null;
        
        try  {
            received = doc.getText(3, 7);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
            fail ("Unexpected BadLocationException: " + ble.getMessage());
        }
        String expected = first.substring (3, 10);
        
        assertEquals ("getText() returned \"" + received + "\" but should have returned \"" + expected + "\"", expected, received);
       
        try {
            received = doc.getText (0, first.length());
        } catch (BadLocationException e) {
            e.printStackTrace();
            fail ("Unexpected BadLocationException thrown: " + e.getMessage());
        }
        assertEquals ("getText for the full first string printed should return \"" + first + "\" but got \"" + received + "\"", first, received);
        
        String second = "Lets try a different string this time";
        ow.println(second);
        ow.flush();
        try {
            received = doc.getText (0, doc.getLength());
        } catch (BadLocationException ble) {
            ble.printStackTrace();
            fail ("Unexpected BadLocationException: " + ble.getMessage());
        }
        expected = first + "\n" + second + "\n";
        
        assertEquals ("getText for first two strings should be \"" + expected + "\" but was \"" + received + "\"", expected, received);
    }
    
    public void testInsertString() {
        System.out.println("testInsertString");
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        ow.println(first);
        ow.flush();
        
        UnsupportedOperationException uoe = null;
        try {
            doc.insertString(5, "Foo", null);
        } catch (UnsupportedOperationException e) {
            uoe = e;
        } catch (BadLocationException e) {
            fail ("Bad location exception thrown - should have been UnsupportedOperationException");
        }
        assertNotNull ("insertString should throw an unsupportedOperationException ", uoe);
        
    }

     
    public void testRender() {
        System.out.println("testRender");

        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        ow.println(first);
        ow.flush();

        RunIt r = new RunIt();
        doc.render (r);
        r.assertWasRun();

    }
    
    private static class RunIt implements Runnable {
        private boolean hasBeenRun = false;
        
        public void assertWasRun() {
            assertTrue (hasBeenRun);
        }
        
        public void run() {
            hasBeenRun = true;
        }
    }
    
    public void testGetDocument() {
        System.out.println("testGetDocument");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        ow.println(first);
        ow.flush();

        
        assertTrue(doc.getDocument() == doc);

    }
    
    public void testGetElement() {
        System.out.println("testGetElement");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();        
        
        Element el = doc.getElement(0);
        assertTrue (el.getStartOffset() == 0);
        assertTrue ("End offset should be length of string + separator length (" 
            + (first.length() + 1) + " but was " + el.getEndOffset(), 
            el.getEndOffset() == first.length() + 1);
        
        el = doc.getElement(1);
    }
    
    public void testGetElementCount() {
        System.out.println("testGetElementCount");
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();    
        assertTrue ("Element count for document should match line count", doc.getElementCount() == ow.lineCount());
    }
    
    public void testGetElementIndex() {
        System.out.println("testGetElementIndex");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();    
        
        int idx = doc.getElementIndex(5);
        assertTrue ("Element index five characters into the first string should be element 0, not " + idx, idx == 0);
        
        idx = doc.getElementIndex (first.length() + 1 + 5);
        assertTrue ("Element index five characters into the second string should be 1, not " + idx,  idx == 1);
        
        
        idx = doc.getElementIndex(first.length() + 1 + second.length() + 1 + (third.length() / 2));
        assertTrue ("Element index halfway through the third string should be 2, not " + idx,
            idx == 2);
    }
    
    public void testGetEndOffset() {
        System.out.println("testGetEndOffset");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();    
        
        assertTrue ("End offset should be chars printed", doc.getEndOffset() == ow.charsWritten());
    }
    
    public void testGetParentElement() {
        System.out.println("testGetParentElement");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        assertNull ("Document parent element should be null ", doc.getParentElement());
        
        Element el = doc.getElement(1);
        assertSame ("Parent element of line element should be the document", el.getParentElement(), doc);
        
    }
    
    public void testGetStartOffset() {
        System.out.println("testGetStartOffset");
        
    }
    
    public void testIsLeaf() {
        System.out.println("testIsLeaf");
        
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        
        
        assertTrue ("Document should be leaf if no text has been written", doc.isLeaf());
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        assertFalse("Document should not be leaf if text has been written", doc.isLeaf());
    }

    
    public void testDocumentEventSimilarity() throws Exception {
        DefaultStyledDocument styled = new DefaultStyledDocument();
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        
        ODListener docListener = new ODListener(doc);
        ODListener styListener = new ODListener(styled);
        
        
        
        
        String s = "This is a string I will append";
        
        styled.insertString(styled.getLength(), s + "\n", SimpleAttributeSet.EMPTY);
        ow.println (s);
        ow.flush();
        docListener.assertChanged();
        styListener.assertChanged();
        
        styled.insertString(styled.getLength(), s + "\n", SimpleAttributeSet.EMPTY);
        ow.println (s);
        ow.flush();
        
        //Wait for async event firing from output document
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                System.currentTimeMillis();
            }
        });
        Thread.currentThread().sleep (1000);
        
        int styLen = styled.getLength();
        int docLen = doc.getLength();
        
        assertTrue ("Length should be same, but OutputDocument length is " +
            docLen + " and similar StyledDocument length is " + styLen, 
            doc.getLength() == styled.getLength());
        
        DocumentEvent docEvent = docListener.getEvent();
        DocumentEvent styEvent = styListener.getEvent();

        assertNotNull("StyledDocument should have fired an event", styEvent);
        assertNotNull("OutputDocument should have fired an event", docEvent);
        
        assertEventsIdentical (styled, doc, styEvent, docEvent);
        
        //Stress test it to ensure no off-by-ones that show up only when the file is large
        for (int i = 0; i < 10; i++) {
            for (int j=0; j < STRINGS.length; j++) {
                styled.insertString(styled.getLength(), s + "\n", SimpleAttributeSet.EMPTY);
                ow.println (s);
                ow.flush();

                //Wait for async event firing from output document
                SwingUtilities.invokeAndWait (new Runnable() {
                    public void run() {
                        System.currentTimeMillis();
                    }
                });
                SwingUtilities.invokeAndWait (new Runnable() {
                    public void run() {
                        System.currentTimeMillis();
                    }
                });
                Thread.currentThread().sleep (500);

                styLen = styled.getLength();
                docLen = doc.getLength();

                assertTrue ("Length should be same, but OutputDocument length is " +
                    docLen + " and similar StyledDocument length is " + styLen, 
                    doc.getLength() == styled.getLength());

                docEvent = docListener.getEvent();
                styEvent = styListener.getEvent();
                
                assertEventsIdentical (styled, doc, styEvent, docEvent);

            }
        }
        
    }
    
    private void assertEventsIdentical (Document styled, OutputDocument doc, DocumentEvent styEvent, DocumentEvent docEvent) throws Exception {
        int docOffset = docEvent.getOffset();
        int styOffset = styEvent.getOffset();
        
        int docLength = docEvent.getLength();
        int styLength = styEvent.getLength();
        
        assertTrue ("OutputDocument event offset is " + docOffset + " but " + 
            "offset of identical change to a StyledDocument is " + styOffset,
            docOffset == styOffset);
        
        assertTrue ("OutputDocument event length is " + docLength + " but " +
            "length from identical change to a StyledDocument is " + 
            styLength, styLength == docLength);
        
        DocumentEvent.ElementChange docEc = docEvent.getChange(doc);
        DocumentEvent.ElementChange styEc = styEvent.getChange(styled.getDefaultRootElement());
        
        int docIndex = docEc.getIndex();
        int styIndex = styEc.getIndex();
        
        Element[] docAdded = docEc.getChildrenAdded();
        Element[] styAdded = styEc.getChildrenAdded();
        
        assertTrue ("Index of change in OutputDocument was " + docIndex + " but" +
        " an identical change on a StyledDocument returns " + styIndex,
        styIndex == docIndex);
        
        assertTrue ("OutputDocument returned an array of " + docAdded.length +
            " affected elements, but an identical change on a StyledDocument " +
            "produces an array of " + styAdded.length, styAdded.length == 
            docAdded.length);

        for (int i=0; i < docAdded.length; i++) {
            int docStartOffset = docAdded[i].getStartOffset();
            int styStartOffset = styAdded[i].getStartOffset();
            assertTrue ("Start offset of element " + i + " from " +
                "OutputDocument.ODDEvent.EC is " + docStartOffset + " but " +
                "offset from identical change in a StyledDocument is " + 
                styStartOffset, styStartOffset == docStartOffset);
            
            int docEndOffset = docAdded[i].getEndOffset();
            int styEndOffset = styAdded[i].getEndOffset();
            assertTrue ("End offset of element " + i + " from " +
                "OutputDocument.ODDEvent.EC is " + docStartOffset + " but " +
                "offset from identical change in a StyledDocument is " + 
                styEndOffset, styEndOffset == docEndOffset);
            
            String styTxt = styled.getText(styAdded[i].getStartOffset(), styAdded[i].getEndOffset() - styAdded[i].getStartOffset());
            String docTxt = styled.getText(styAdded[i].getStartOffset(), styAdded[i].getEndOffset() - styAdded[i].getStartOffset());
            assertEquals("Element " + i + " text from styled document is " + 
                styTxt + " but OutputDocument return " + docTxt + " for the " +
                "same indices", styTxt, docTxt);
        }
    }
    
    private String[] STRINGS = new String[] {
        "Okay, we need some content for this test.  I wonder what it should " +
            "be?  We should probably have some seriously long strings in here, " +
            "just to make sure everythings okay.  After all, we wouldn't want to" +
            "lose a byte or two of output one day, that would be bad.  But if " +
            "everything works out, this thing might just be pretty cool, and " +
            "efficient, and nifty, and all that stuff.  Wouldn't that be nice?" +
            " Of course it would be silly!  Good heavens, I need a vacation.",
        "This is a short, non-editorializing string",
        "Should you get a chance to read it, David McCullough's biography of " +
        "John Adams is quite good.",
        "Short string",
        "Okay, let's just go ahead and write another long string - after all, " +
        "what else was I going to do with my time anyway?  Did you ever consider" +
        "what it would be like to *be* an output window?  I mean really, whatever" +
        "any body prints, you just display it on the front of you.  Never " +
        "complain, never get to be creative, never get to, say, write some " +
        "POETRY, or turn that exception message into a nice haiku.  I mean, " +
        "once in a while it would be nice to let your hair down, after all - " +
        "all this build failed stuff is just a collossal bore.  C'mon, gimme some" +
        "GOOD output for once",
        "I'm sure there's an explanation for all of this.  I just don't know" +
        "what it is"
    };
    
    /*
    public void testMultithreadedWrites() throws Exception {
        System.err.println("testMultithreadedWrites");
        DefaultStyledDocument styled = new DefaultStyledDocument();
        OutWriter ow = new OutWriter ();
        OutputDocument doc = new OutputDocument (ow);
        
        ODListener docListener = new ODListener(doc);
        ODListener styListener = new ODListener(styled);
        
        String s = "This is a string I will append";
        
        threadCount = 5;
        bangers = new Thread[threadCount];
        System.err.println("Starting 15 threads");
        for (int i=0; i < threadCount; i++) {
            Banger b = new Banger (i, doc, styled, ow);
            bangers[i] = new Thread(b);
            bangers[i].start();
        }
        System.err.println("notifying start lock");
        synchronized (START_LOCK) {
            START_LOCK.notifyAll();
        }
        System.err.println("Waiting on stop lock");
        synchronized (STOP_LOCK) {
            STOP_LOCK.wait();
            System.err.println("Stop lock notified");
        }
        System.err.println("Finished with the madness");
        
        int len1 = doc.getLength();
        int len2 = styled.getLength();
        assertTrue ("Document and styled document should have same length " +
            " but OutputDocument is " + len1 + " and StyledDocument " + len2,
            len1 == len2);
        
        int ct1 = doc.getElementCount();
        int ct2 = styled.getDefaultRootElement().getElementCount();
        
        assertTrue ("Document and styled document should " +
            "have same number of elements " +
            " but OutputDocument is " + ct1 + " and StyledDocument " + ct2,
            ct1 == ct2);
        
        
    }
    
    private static Thread[] bangers = null;
    private static final Object START_LOCK = new Object();
    private static final Object WRITE_LOCK = new Object();
    private static final Object STOP_LOCK = new Object();
    private static int threadCount = 0;
    private class Banger implements Runnable {
        private int index;
        private OutputDocument doc;
        private StyledDocument sty;
        private OutWriter ow;
       
        public Banger (int index, OutputDocument doc, StyledDocument sty, OutWriter ow) {
            this.index = index;
            this.doc = doc;
            this.sty = sty;
            this.ow = ow;
        }
        public void run() {
            synchronized (START_LOCK) {
                try {
                    System.err.println("Banger thread " + index + " waiting to start");
                    START_LOCK.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Interrupted");
                }
            }
            System.err.println("Banger thread " + index + " started ");
            for (int i=0; i < 10; i++) {
                String s = "Hello " + i + " from banger thread " + index;
//                synchronized (WRITE_LOCK) {
                System.err.println(s);
                    try {
                        sty.insertString(sty.getLength(), s + "\n", SimpleAttributeSet.EMPTY);
                        ow.println (s);
//                        ow.flush();                        
                    } catch (Exception e) {
                        for (int j=0; j < bangers.length; j++) {
                            if (bangers[j] != Thread.currentThread()) {
                                bangers[j].stop();
                            }
                            e.printStackTrace();
                            fail (e.getMessage());
                        }
                    }
                Thread.currentThread().yield();
                }
//            }
            synchronized (START_LOCK) {
                threadCount--;
            }
            if (threadCount == 0) {
                synchronized (STOP_LOCK) {
                    ow.flush();
                    STOP_LOCK.notifyAll();
                }
            }
        }
    }
     */
    
    
    final class ODListener implements DocumentListener {
        DocumentEvent evt = null;
        
        public ODListener(){}
        
        public ODListener (Document d) {
            d.addDocumentListener(this);
        }

        public void assertChanged() {
            DocumentEvent e = evt;
            evt = null;
            //Trigger consumed on OutputDocument.DO
            e.getLength();
            assertTrue ("No event received", e != null);
        }
        
        public void assertNoChange() {
            DocumentEvent e = evt;
            evt = null;
            assertTrue ("Unexpected event was received " + e, e == null);
        }
        
        public DocumentEvent getEvent() {
            return evt;
        }
        
        
        public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
            evt = documentEvent;
        }
        
        public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
            evt = documentEvent;
        }
        
        public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
            evt = documentEvent;
        }
        
    }
    
}
