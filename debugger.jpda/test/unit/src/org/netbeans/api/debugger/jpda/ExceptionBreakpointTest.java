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

package org.netbeans.api.debugger.jpda;

/**
 * Tests exception breakpoints.
 *
 * @author Maros Sandor
 */
public class ExceptionBreakpointTest extends DebuggerJPDAApiTestBase {

    private JPDASupport     support;
    private JPDADebugger    debugger;

    private static final String CLASS_NAME = "org.netbeans.api.debugger.jpda.testapps.ExceptionBreakpointApp";

    public ExceptionBreakpointTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMethodBreakpoints() throws Exception {
        try {
            ExceptionBreakpoint eb1 = ExceptionBreakpoint.create("org.netbeans.api.debugger.jpda.testapps.ExceptionTestException", ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED);
            TestBreakpointListener tbl = new TestBreakpointListener("org.netbeans.api.debugger.jpda.testapps.ExceptionTestException", eb1, 1);
            eb1.addJPDABreakpointListener(tbl);
            dm.addBreakpoint(eb1);

            support = JPDASupport.attach (CLASS_NAME);
            debugger = support.getDebugger();

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) break;
                support.doContinue();
            }
            tbl.assertFailure();

            dm.removeBreakpoint(eb1);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private String              exceptionClass;
        private ExceptionBreakpoint bpt;
        private int                 expectedHitCount;

        public TestBreakpointListener(String exceptionClass, ExceptionBreakpoint bpt, int expectedHitCount) {
            this.exceptionClass = exceptionClass;
            this.bpt = bpt;
            this.expectedHitCount = expectedHitCount;
        }

        public void breakpointReached(JPDABreakpointEvent event) {
            try {
                checkEvent(event);
            } catch (AssertionError e) {
                failure = e;
            } catch (Throwable e) {
                failure = new AssertionError(e);
            }
        }

        private void checkEvent(JPDABreakpointEvent event) {
            assertEquals("Breakpoint event: Bad exception location", CLASS_NAME, event.getReferenceType().name());
            assertEquals("Breakpoint event: Bad exception thrown", exceptionClass, event.getVariable ().getType ());
            assertSame("Breakpoint event: Bad event source", bpt, event.getSource());
            assertEquals("Breakpoint event: Condition evaluation failed", JPDABreakpointEvent.CONDITION_NONE, event.getConditionResult());
            assertNotNull("Breakpoint event: Context thread is null", event.getThread());
            hitCount++;
        }

        public void assertFailure() {
            if (failure != null) throw failure;
            assertEquals("Breakpoint hit count mismatch", expectedHitCount, hitCount);
        }
    }
}
