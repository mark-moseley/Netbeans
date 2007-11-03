/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bracesmatching;

import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class MasterMatcherTest extends NbTestCase {

    public MasterMatcherTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testContext() throws Exception {
        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.EMPTY, new TestMatcher());
        
        AttributeSet EAS = SimpleAttributeSet.EMPTY;
        JEditorPane c = new JEditorPane();
        Document d = c.getDocument();
        OffsetsBag bag = new OffsetsBag(d);
        d.insertString(0, "text text { text } text", null);

        c.putClientProperty(MasterMatcher.PROP_MAX_BACKWARD_LOOKAHEAD, 3);
        c.putClientProperty(MasterMatcher.PROP_MAX_FORWARD_LOOKAHEAD, 4);
        c.putClientProperty(MasterMatcher.PROP_SEARCH_DIRECTION, MasterMatcher.D_FORWARD);
        c.putClientProperty(MasterMatcher.PROP_CARET_BIAS, MasterMatcher.B_FORWARD);
        
        TestMatcher.origin = new int [] { 7, 7 };
        MasterMatcher.get(c).highlight(d, 7, bag, EAS, EAS);
        Thread.sleep(300);
        {
        TestMatcher tm = TestMatcher.lastMatcher;
        assertNotNull("No matcher created", tm);
        assertNotNull("No context passed to the matcher", tm.context);
        assertSame("Wrong document", d, tm.context.getDocument());
        assertEquals("Wrong caret offset", 7, tm.context.getSearchOffset());
        assertFalse("Wrong search direction", tm.context.isSearchingBackward());
        assertEquals("Wrong lookahead", 4, tm.context.getSearchLookahead());
        }        
        
        TestMatcher.lastMatcher = null;
        TestMatcher.origin = new int [] { 11, 11 };
        c.putClientProperty(MasterMatcher.PROP_SEARCH_DIRECTION, MasterMatcher.D_BACKWARD);
        c.putClientProperty(MasterMatcher.PROP_CARET_BIAS, MasterMatcher.B_BACKWARD);
        
        MasterMatcher.get(c).highlight(d, 11, bag, EAS, EAS);
        Thread.sleep(300);
        {
        TestMatcher tm = TestMatcher.lastMatcher;
        assertNotNull("No matcher created", tm);
        assertNotNull("No context passed to the matcher", tm.context);
        assertSame("Wrong document", d, tm.context.getDocument());
        assertEquals("Wrong caret offset", 11, tm.context.getSearchOffset());
        assertTrue("Wrong search direction", tm.context.isSearchingBackward());
        assertEquals("Wrong lookahead", 3, tm.context.getSearchLookahead());
        }        
    }
    
    public void testAreas() throws Exception {
        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.EMPTY, new TestMatcher());
        
        AttributeSet EAS = SimpleAttributeSet.EMPTY;
        JEditorPane c = new JEditorPane();
        Document d = c.getDocument();
        OffsetsBag bag = new OffsetsBag(d);
        d.insertString(0, "text text { text } text", null);

        c.putClientProperty(MasterMatcher.PROP_MAX_BACKWARD_LOOKAHEAD, 256);
        c.putClientProperty(MasterMatcher.PROP_MAX_FORWARD_LOOKAHEAD, 256);
        
        TestMatcher.origin = new int [] { 2, 3 };
        TestMatcher.matches = new int [] { 10, 11 };
        
        MasterMatcher.get(c).highlight(d, 7, bag, EAS, EAS);
        Thread.sleep(300);
        {
        TestMatcher tm = TestMatcher.lastMatcher;
        assertNotNull("No matcher created", tm);
        
        HighlightsSequence hs = bag.getHighlights(0, Integer.MAX_VALUE);
        assertTrue("Wrong number of highlighted areas", hs.moveNext());
        assertEquals("Wrong origin startOfset", 2, hs.getStartOffset());
        assertEquals("Wrong origin endOfset", 3, hs.getEndOffset());
        
        assertTrue("Wrong number of highlighted areas", hs.moveNext());
        assertEquals("Wrong match startOfset", 10, hs.getStartOffset());
        assertEquals("Wrong match endOfset", 11, hs.getEndOffset());
        }        
    }
    
    public void testBlockingByForLoop() throws Exception {
        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.EMPTY, new BlockingMatcher());
        
        AttributeSet EAS = SimpleAttributeSet.EMPTY;
        JEditorPane c = new JEditorPane();
        Document d = c.getDocument();
        OffsetsBag bag = new OffsetsBag(d);
        d.insertString(0, "text text { text } text", null);

        c.putClientProperty(MasterMatcher.PROP_MAX_BACKWARD_LOOKAHEAD, 256);
        c.putClientProperty(MasterMatcher.PROP_MAX_FORWARD_LOOKAHEAD, 256);
        
        BlockingMatcher.origin = new int [] { 2, 3 };
        BlockingMatcher.matches = new int [] { 10, 11 };

        {
        BlockingMatcher.blockInFindOrigin = true;
        
        MasterMatcher.get(c).highlight(d, 7, bag, EAS, EAS);
        Thread.sleep(300);
        BlockingMatcher first = BlockingMatcher.lastMatcher;
        assertNotNull("No first matcher", first);
        assertTrue("Should be blocking", first.blocking);
        
        MasterMatcher.get(c).highlight(d, 8, bag, EAS, EAS);
        Thread.sleep(2000);
        BlockingMatcher second = BlockingMatcher.lastMatcher;
        assertNotNull("No second matcher", second);
        
        assertFalse("First blocking matcher was not interrupted", first.blocking);
        assertFalse("There should be no highlights", bag.getHighlights(0, Integer.MAX_VALUE).moveNext());
        
        second.breakOutFromTheLoop = true; // stop the matchers loop
        }
        
        {
        BlockingMatcher.blockInFindOrigin = false;
        
        MasterMatcher.get(c).highlight(d, 7, bag, EAS, EAS);
        Thread.sleep(300);
        BlockingMatcher first = BlockingMatcher.lastMatcher;
        assertNotNull("No first matcher", first);
        assertTrue("First matcher should be blocking", first.blocking);
        
        MasterMatcher.get(c).highlight(d, 8, bag, EAS, EAS);
        Thread.sleep(2000);
        BlockingMatcher second = BlockingMatcher.lastMatcher;
        assertNotNull("No second matcher", second);
        assertTrue("Second matcher should be blocking", second.blocking);
        
        assertFalse("First blocking matcher was not interrupted", first.blocking);
        assertFalse("There should be no highlights", bag.getHighlights(0, Integer.MAX_VALUE).moveNext());

        second.breakOutFromTheLoop = true; // stop the matchers loop
        }
    }

    public void testThreadResultsGCed() throws Exception {
        testContext();
        testAreas();
        testBlockingByForLoop();
        Thread.sleep(1000);
        assertEquals("There should be no threads in the threadResults map", 0, MasterMatcher.THREAD_RESULTS.size());
    }
    
    private static final class TestMatcher implements BracesMatcher, BracesMatcherFactory {

        public static TestMatcher lastMatcher = null; 
        public static int [] origin = null;
        public static int [] matches = null;
        
        public final MatcherContext context;
        
        public TestMatcher() {
            this(null);
        }
        
        private TestMatcher(MatcherContext context) {
            this.context = context;
        }
        
        public int[] findOrigin() throws InterruptedException, BadLocationException {
            return origin;
        }

        public int[] findMatches() throws InterruptedException, BadLocationException {
            return matches;
        }

        public BracesMatcher createMatcher(MatcherContext context) {
            lastMatcher = new TestMatcher(context);
            return lastMatcher;
        }
    }

    private static final class BlockingMatcher implements BracesMatcher, BracesMatcherFactory {

        public static BlockingMatcher lastMatcher = null; 
        
        public static boolean blockInFindOrigin = false;
        
        public static int [] origin = null;
        public static int [] matches = null;
        
        public final MatcherContext context;
        
        public volatile boolean breakOutFromTheLoop = false;
        public volatile boolean blocking;
        
        public BlockingMatcher() {
            this(null);
        }
        
        private BlockingMatcher(MatcherContext context) {
            this.context = context;
        }
        
        public int[] findOrigin() throws InterruptedException, BadLocationException {
            if (blockInFindOrigin) {
                block();
            }
            return origin;
        }

        public int[] findMatches() throws InterruptedException, BadLocationException {
            if (!blockInFindOrigin) {
                block();
            }
            return matches;
        }

        private void block() throws InterruptedException {
            blocking = true;
            try {
                //System.out.println("!!! Blocking: " + this + ", offset = " + context.getCaretOffset());
                for( ; !breakOutFromTheLoop; ) {
                    if (MatcherContext.isTaskCanceled()) {
                        return;
                    }
                }
            } finally {
                //System.out.println("!!! Not Blocking: " + this + ", offset = " + context.getCaretOffset());
                blocking = false;
            }
        }
        
        public BracesMatcher createMatcher(MatcherContext context) {
            lastMatcher = new BlockingMatcher(context);
            return lastMatcher;
        }
    }
}
