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

package org.netbeans.lib.lexer;

import java.util.ArrayList;
import junit.framework.*;
import java.util.List;

/**
 *
 * @author mmetelka
 */
public class LAStateTest extends TestCase {

    public LAStateTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testLAStateClassTypes() throws Exception {
        LAState laState = LAState.empty();
        laState = laState.add(1, null); // should remain NoState
        assertEquals(laState.getClass(), LAState.NoState.class);

        laState = LAState.empty();
        laState = laState.add(127, null);
        assertEquals(laState.getClass(), LAState.NoState.class);

        laState = LAState.empty();
        laState = laState.add(127, new Integer(127));
        assertEquals(laState.getClass(), LAState.ByteState.class);

        laState = LAState.empty();
        laState = laState.add(128, null);
        assertEquals(laState.getClass(), LAState.LargeState.class);

        laState = LAState.empty();
        laState = laState.add(0, new Object());
        assertEquals(laState.getClass(), LAState.LargeState.class);
    }

    public void testLAState() {
        List<Object> expected = new ArrayList<Object>();
        LAState laState = LAState.empty();
        laState = add(expected, laState, 0, null);
        laState = add(expected, laState, 1, null);
        laState = add(expected, laState, 0, new Object());
        laState = add(expected, laState, 127, null);
        laState = add(expected, laState, 127, new Integer(-1));
        remove(expected, laState, 1, 3);

        List<Object> expectedInner = expected;
        LAState laStateInner = laState;

        expected = new ArrayList<Object>();
        laState = laState.empty();
        laState = add(expected, laState, 1, null);
        laState = add(expected, laState, 7, null);
        laState = add(expected, laState, 5, null);
        laState = addAll(expected, laState, 1, expectedInner, laStateInner);
        laState = addAll(expected, laState, laState.size(), expectedInner, laStateInner);
        remove(expected, laState, 4, 3);
        laState = addAll(expected, laState, 0, expectedInner, laStateInner);
    }

    private static LAState add(List<Object> expectedLAState, LAState laState, int lookahead, Object state) {
        expectedLAState.add(Integer.valueOf(lookahead));
        expectedLAState.add(state);
        laState = laState.add(lookahead, state);
        consistencyCheck(expectedLAState, laState);
        return laState;
    }

    private static LAState addAll(List<Object> expectedLAState, LAState laState, int index,
    List<Object> expectedLAStateToAdd, LAState laStateToAdd) {
        expectedLAState.addAll(index << 1, expectedLAStateToAdd);
        laState = laState.addAll(index, laStateToAdd);
        consistencyCheck(expectedLAState, laState);
        return laState;
    }

    private static void remove(List<Object> expectedLAState, LAState laState, int index, int count) {
        for (int i = count << 1; i > 0; i--) {
            expectedLAState.remove(index << 1);
        }
        laState.remove(index, count);
        consistencyCheck(expectedLAState, laState);
    }

    private static void consistencyCheck(List<Object> expectedLAState, LAState laState) {
        // Ensure the test laState class is equal to expected one
        assertEquals(expectedLAState.size() & 1, 0);
        assertEquals("Invalid size", expectedLAState.size() >> 1, laState.size());
        for (int i = 0; i < expectedLAState.size(); i++) {
            assertEquals("Invalid lookahead", ((Integer)expectedLAState.get(i)).intValue(), laState.lookahead(i >> 1));
            i++;
            assertEquals("Invalid state", expectedLAState.get(i), laState.state(i >> 1));
        }
    }

}
