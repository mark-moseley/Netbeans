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

import java.util.*;

/**
 * Tests information about stack call stacks.
 *
 * @author Maros Sandor
 */
public class CallStackTest extends DebuggerJPDAApiTestBase {

    private JPDASupport     support;
    private JPDADebugger    debugger;

    public CallStackTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        super.setUp();
        support = JPDASupport.listen("basic.CallStackApp");
        debugger = support.getDebugger();
    }

    public void testInstanceCallStackInfo() throws Exception {

        support.stepOver();
        support.stepInto();
        support.stepOver();
        support.stepInto();
        support.stepOver();

        CallStackFrame sf = debugger.getCurrentCallStackFrame();

        List strata = sf.getAvailableStrata();
        assertEquals("Available strata", 1, strata.size());
        assertEquals("Java stratum is not available", "Java", strata.get(0));
        assertEquals("Java stratum is not default", "Java", sf.getDefaultStratum());

        assertEquals("Wrong class name", "basic.CallStackApp", sf.getClassName());
        assertEquals("Wrong line number", 43, sf.getLineNumber(null));

        LocalVariable [] vars = sf.getLocalVariables();
        assertEquals("Wrong number of local variables", 1, vars.length);
        assertEquals("Wrong info about local variables", "im2", vars[0].getName());

        assertEquals("Wrong info about current method", "m2", sf.getMethodName());
        assertNotNull("Wrong info about this object", sf.getThisVariable());
        assertFalse("Wrong info about obsolete method", sf.isObsolete());

        JPDAThread thread = sf.getThread();
        assertEquals("Callstack and Thread info mismatch", thread.getCallStack()[0], sf);
        assertEquals("Callstack and Thread info mismatch", thread.getClassName(), sf.getClassName());
        assertEquals("Callstack and Thread info mismatch", thread.getMethodName(), sf.getMethodName());
        assertEquals("Callstack and Thread info mismatch", thread.getSourceName(null), sf.getSourceName(null));

        support.doFinish();
    }

    public void testStaticCallStackInfo() throws Exception {

        CallStackFrame sf = debugger.getCurrentCallStackFrame();

        List strata = sf.getAvailableStrata();
        assertEquals("Available strata", 1, strata.size());
        assertEquals("Java stratum is not available", "Java", strata.get(0));
        assertEquals("Java stratum is not default", "Java", sf.getDefaultStratum());

        assertEquals("Wrong class name", "basic.CallStackApp", sf.getClassName());
        assertEquals("Wrong line number", 24, sf.getLineNumber(null));

        LocalVariable [] vars = sf.getLocalVariables();
        assertEquals("Wrong number of local variables", 1, vars.length);
        assertEquals("Wrong info about local variables", "args", vars[0].getName());

        assertEquals("Wrong info about current method", "main", sf.getMethodName());
        assertNull("Wrong info about this object", sf.getThisVariable());
        assertFalse("Wrong info about obsolete method", sf.isObsolete());

        JPDAThread thread = sf.getThread();
        assertEquals("Callstack and Thread info mismatch", thread.getCallStack()[0], sf);
        assertEquals("Callstack and Thread info mismatch", thread.getClassName(), sf.getClassName());
        assertEquals("Callstack and Thread info mismatch", thread.getMethodName(), sf.getMethodName());
        assertEquals("Callstack and Thread info mismatch", thread.getSourceName(null), sf.getSourceName(null));

        support.doFinish();
    }
}
