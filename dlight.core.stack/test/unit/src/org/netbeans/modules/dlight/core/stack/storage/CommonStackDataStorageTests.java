/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.core.stack.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.api.storage.types.Time;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public abstract class CommonStackDataStorageTests {

    private StackDataStorage db;

    protected abstract StackDataStorage createStorage();

    protected abstract void flush(StackDataStorage db);

    @Before
    public void setUp() {
        db = createStorage();
        assertNotNull(db);
    }

    @Test
    public void testSimple() {
        db.putStack(Arrays.<CharSequence>asList("func1"), 10l);
        db.putStack(Arrays.<CharSequence>asList("func1", "func2"), 10l);
        db.putStack(Arrays.<CharSequence>asList("func1", "func2", "func3"), 10l);
        db.putStack(Arrays.<CharSequence>asList("func1", "func2", "func3", "func4"), 10l);
        flush(db);

        List<FunctionCall> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, 10);
        assertEquals(4, hotSpots.size());

        assertEquals("func1", hotSpots.get(0).getFunction().getName());
        assertTimeEquals(40, hotSpots.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func2", hotSpots.get(1).getFunction().getName());
        assertTimeEquals(30, hotSpots.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func3", hotSpots.get(2).getFunction().getName());
        assertTimeEquals(20, hotSpots.get(2).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(2).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func4", hotSpots.get(3).getFunction().getName());
        assertTimeEquals(10, hotSpots.get(3).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(3).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        List<FunctionCall> callees = db.getCallees(new FunctionCall[] {hotSpots.get(0)}, true);
        assertEquals(1, callees.size());
        assertEquals("func2", callees.get(0).getFunction().getName());

        List<FunctionCall> callers = db.getCallers(new FunctionCall[] {hotSpots.get(3)}, true);
        assertEquals(1, callers.size());
        assertEquals("func3", callers.get(0).getFunction().getName());
    }

    @Test
    public void testCallersCallees() {
        db.putStack(Arrays.<CharSequence>asList("func1", "func1"), 10l);
        db.putStack(Arrays.<CharSequence>asList("func2", "func1"), 10l);
        db.putStack(Arrays.<CharSequence>asList("func1", "func2", "func3"), 10l);
        db.putStack(Arrays.<CharSequence>asList("func3", "func2", "func1"), 10l);
        flush(db);

        List<FunctionCall> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, 10);
        assertEquals(3, hotSpots.size());

        assertEquals("func1", hotSpots.get(0).getFunction().getName());
        assertTimeEquals(40, hotSpots.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(30, hotSpots.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func2", hotSpots.get(1).getFunction().getName());
        assertTimeEquals(30, hotSpots.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, hotSpots.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func3", hotSpots.get(2).getFunction().getName());
        assertTimeEquals(20, hotSpots.get(2).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(2).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        List<FunctionCall> callees = db.getCallees(new FunctionCall[] {hotSpots.get(0)}, true);
        assertEquals(2, callees.size());
        Collections.sort(callees, new FunctionCallComparator());

        assertEquals("func1", callees.get(0).getFunction().getName());
        assertTimeEquals(10, callees.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, callees.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func2", callees.get(1).getFunction().getName());
        assertTimeEquals(10, callees.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, callees.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        List<FunctionCall> callers = db.getCallers(new FunctionCall[] {hotSpots.get(0)}, true);
        assertEquals(2, callers.size());
        Collections.sort(callers, new FunctionCallComparator());

        assertEquals("func1", callers.get(0).getFunction().getName());
        assertTimeEquals(20, callers.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, callers.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func2", callers.get(1).getFunction().getName());
        assertTimeEquals(20, callers.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, callers.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
    }

    @Test
    public void testDeepCallers() {
        db.putStack(Arrays.<CharSequence>asList("x", "a", "b", "c", "x"), 10l);
        db.putStack(Arrays.<CharSequence>asList("x", "a", "b", "x"), 10l);
        db.putStack(Arrays.<CharSequence>asList("a", "b", "c"), 10l);
        db.putStack(Arrays.<CharSequence>asList("x", "x", "a", "b", "c"), 10l);
        db.putStack(Arrays.<CharSequence>asList("x", "b", "c"), 10l);
        flush(db);

        List<FunctionCall> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, 10);
        assertEquals(4, hotSpots.size());

        FunctionCall c = find(hotSpots, "c");
        List<FunctionCall> cCallers = db.getCallers(new FunctionCall[] {c}, true);
        assertEquals(1, cCallers.size());

        FunctionCall b = cCallers.get(0);
        assertEquals("b", b.getFunction().getName());
        assertTimeEquals(50, b.getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, b.getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
        List<FunctionCall> bCallers = db.getCallers(new FunctionCall[] {b, c}, true);
        assertEquals(2, bCallers.size());

        FunctionCall a = find(bCallers, "a");
        assertNotNull(a);
        assertTimeEquals(40, a.getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, a.getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
        List<FunctionCall> aCallers = db.getCallers(new FunctionCall[] {a, b, c}, true);
        assertEquals(1, aCallers.size());

        FunctionCall x = aCallers.get(0);
        assertTimeEquals(50, x.getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, x.getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
    }

    @Test
    public void testDeepCallees() {
        db.putStack(Arrays.<CharSequence>asList("a", "b", "c", "d", "e"), 10l);
        db.putStack(Arrays.<CharSequence>asList("a", "b", "c", "d", "f"), 10l);
        db.putStack(Arrays.<CharSequence>asList("b", "c", "d", "e"), 10l);
        db.putStack(Arrays.<CharSequence>asList("b", "c", "d", "f"), 10l);
        db.putStack(Arrays.<CharSequence>asList("c", "d", "e"), 10l);
        db.putStack(Arrays.<CharSequence>asList("c", "d", "f"), 10l);
        flush(db);

        List<FunctionCall> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, 10);
        assertEquals(6, hotSpots.size());

        FunctionCall b = find(hotSpots, "b");
        assertNotNull(b);
        List<FunctionCall> bCallees = db.getCallees(new FunctionCall[] {b}, true);
        assertEquals(1, bCallees.size());
        assertEquals("c", bCallees.get(0).getFunction().getName());

        FunctionCall c = bCallees.get(0);
        List<FunctionCall> cCallees = db.getCallees(new FunctionCall[] {b, c}, true);
        assertEquals(1, cCallees.size());
        assertEquals("d", cCallees.get(0).getFunction().getName());

        FunctionCall d = cCallees.get(0);
        List<FunctionCall> dCallees = db.getCallees(new FunctionCall[] {b, c, d}, true);
        assertEquals(2, dCallees.size());
        Collections.sort(dCallees, new FunctionCallComparator());

        assertEquals("e", dCallees.get(0).getFunction().getName());
        assertTimeEquals(20, dCallees.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(20, dCallees.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("f", dCallees.get(1).getFunction().getName());
        assertTimeEquals(20, dCallees.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(20, dCallees.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
    }

    private static void assertTimeEquals(long nanos, Object obj) {
        assertTrue(obj instanceof Time);
        assertEquals(nanos, ((Time)obj).getNanos());
    }

    private static FunctionCall find(List<FunctionCall> list, String name) {
        for (FunctionCall call : list) {
            if (call.getFunction().getName().equals(name)) {
                return call;
            }
        }
        return null;
    }

    private static class FunctionCallComparator implements Comparator<FunctionCall> {
        public int compare(FunctionCall c1, FunctionCall c2) {
            return c1.getFunction().getName().compareTo(c2.getFunction().getName());
        }
    }

}
