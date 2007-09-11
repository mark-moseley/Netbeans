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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import java.io.*;
import java.util.*;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

/** Test Utilities.topologicalSort.
 * @author Jesse Glick
 */
public class UtilitiesTopologicalSortTest extends NbTestCase {

    public UtilitiesTopologicalSortTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(UtilitiesTopologicalSortTest.class));
    }

    /**
     * @see "#27286"
     */
    public void testTopologicalSort() throws Exception {
        Collection c = Arrays.asList(new String[] {"a", "b", "c", "d", "e", "f"});
        Map m = new HashMap();
        m.put("f", Collections.singletonList("a"));
        List l = Utilities.topologicalSort(c, m);
        assertNotNull(l);
        assertTrue(l.indexOf("f") < l.indexOf("a"));
        m.put("e", Collections.singletonList("b"));
        l = Utilities.topologicalSort(c, m);
        assertNotNull(l);
        assertTrue(l.indexOf("f") < l.indexOf("a"));
        assertTrue(l.indexOf("e") < l.indexOf("b"));
        m.put("b", Collections.singletonList("a"));
        l = Utilities.topologicalSort(c, m);
        assertNotNull(l);
        assertTrue(l.indexOf("f") < l.indexOf("a"));
        assertTrue(l.indexOf("e") < l.indexOf("b"));
        assertTrue(l.indexOf("b") < l.indexOf("a"));
        // Test that it is modifiable:
        l.add("foo");
        Collections.reverse(l);
        // Test cycles:
        m.put("a", Collections.singletonList("e"));
        try {
            l = Utilities.topologicalSort(c, m);
            fail ("Should throw an exception");
        } catch (TopologicalSortException ex) {
        }
    }
    
    public void testTopologicalSortError() throws Exception {
        Collection c = Arrays.asList(new String[] {"first", "a", "b", "c", "d", "e", "f", "last"});
        Map m = new HashMap();
        // to make sure that not whole visited graph will be in the cycle
        m.put("first", Collections.singletonList ("f"));
        m.put("last", Collections.singletonList ("f"));
        
        
        m.put("f", Collections.singletonList("a"));
        Utilities.topologicalSort (c, m); // does not throw an error
        m.put("e", Collections.singletonList("b"));
        Utilities.topologicalSort (c, m); // does not throw an error
        m.put("b", Collections.singletonList("a"));
        Utilities.topologicalSort (c, m); // does not throw an error
        m.put("a", Collections.singletonList("e"));
        
        try {
            Utilities.topologicalSort (c, m); 
            fail ("Should throw an error");
        } catch (TopologicalSortException ex) {
            Set[] sets = ex.topologicalSets();
            
            assertEquals ("There is one cycle of size 3, all other objects are sortable", 6, sets.length);
            
            Set cycle = null;
            for (int i = 0; i < sets.length; i++) {
                if (sets[i].size () > 1) {
                    assertNull ("There is just one unsortable component", cycle);
                    cycle = sets[i];
                } else {
                    assertEquals ("Size is 1", 1, sets[i].size ());
                }
            }
            
            assertTrue ("a is there", cycle.contains ("a"));
            assertTrue ("b is there", cycle.contains ("b"));
            assertTrue ("e is there", cycle.contains ("e"));
            assertEquals ("Three vertexes are in the cycle", 3, cycle.size ());
            
            assertBefore ("first", "f", sets);
            assertBefore ("last", "f", sets);
            assertBefore ("f", "a", sets);
            assertBefore ("f", "b", sets);
            assertBefore ("f", "e", sets);
            
            List partial = ex.partialSort ();
            assertEquals ("Has the same size as the original", c.size (), partial.size ());
            assertBefore ("first", "f", partial);
            assertBefore ("last", "f", partial);
            assertBefore ("f", "a", partial);
            assertBefore ("f", "b", partial);
            assertBefore ("f", "e", partial);
        }
    }
    
    public void testMoreCycles () throws Exception {
        Collection c = Arrays.asList(new String[] {"first", "a", "b", "c", "d", "e", "f", "last"});
        Map m = new HashMap();
        // to make sure that not whole visited graph will be in the cycle
        
        m.put ("a", Collections.singletonList ("a")); // self cycle
        
        // cycle 2
        m.put ("b", Collections.singletonList ("c"));
        m.put ("c", Collections.singletonList ("b"));
        
        // cycle 3
        m.put ("d", Collections.singletonList ("e"));
        m.put ("e", Collections.singletonList ("f"));
        m.put ("f", Collections.singletonList ("d"));
        
        Collection sizes = new ArrayList ();
        
        try {
            Utilities.topologicalSort (c, m); 
            fail ("Should throw an error");
        } catch (TopologicalSortException ex) {
            Set[] sets = ex.topologicalSets();
            for (int i = 0; i < sets.length; i++) {
                sizes.add (new Integer (sets[i].size ()));
            }
            
            assertEquals ("There were three cycles plus first+last", 5, sizes.size ());
            assertTrue ("One of size 1", sizes.contains (new Integer (1)));
            assertTrue ("One of size 2", sizes.contains (new Integer (2)));
            assertTrue ("One of size 3", sizes.contains (new Integer (3)));
            
            sets = ex.unsortableSets();
            assertEquals ("Three cycles", 3, sets.length);
        }
    }
    
    public void testDetectSelfCycle () throws Exception {
        Collection c = Arrays.asList(new String[] {"a", "b" });
        Map m = new HashMap();
        
        m.put ("a", Arrays.asList (new String[] { "a" })); 
        m.put ("b", Arrays.asList (new String[] { "a" })); 
        
        try {
            Utilities.topologicalSort (c, m); 
            fail ("Definitively there is a cycle");
        } catch (TopologicalSortException ex) {
            Set[] sets = ex.unsortableSets ();
            
            assertEquals ("One cycle", 1, sets.length);
            assertEquals ("Contains one item", 1, sets[0].size ());
            assertTrue ("Contains a", sets[0].contains ("a"));
        }
    }

    public void testFindLongestCycle () throws Exception {
        Collection c = Arrays.asList(new String[] {"a", "b", "c", "d", "e", "f" });
        Map m = new HashMap();
        // to make sure that not whole visited graph will be in the cycle
        
        m.put ("a", Arrays.asList (new String[] { "b", "c" })); 
        m.put ("b", Arrays.asList (new String[] { "a", "d" })); 
        m.put ("c", Arrays.asList (new String[] { "a", "d" })); 
        m.put ("d", Arrays.asList (new String[] { "b", "c", "e" })); 
        m.put ("f", Arrays.asList (new String[] { "a" }));
        
        try {
            Utilities.topologicalSort (c, m); 
            fail ("Definitively there is a cycle");
        } catch (TopologicalSortException ex) {
            Set[] sets = ex.topologicalSets();
            
            assertEquals ("There is one cycle and e+f", 3, sets.length);
            
            assertBefore ("f", "a", sets);
            assertBefore ("f", "b", sets);
            assertBefore ("f", "c", sets);
            assertBefore ("f", "d", sets);
            assertBefore ("f", "e", sets);
            
            assertBefore ("a", "e", sets);
            assertBefore ("b", "e", sets);
            assertBefore ("c", "e", sets);
            assertBefore ("d", "e", sets);
            
            assertEquals ("set of f", 1, sets[0].size ());
            assertEquals ("set of a,b,c,d", 4, sets[1].size ());
            assertEquals ("set of e", 1, sets[2].size ());
        }
    }
    
    public void testStability() throws Exception {
        Collection c = Arrays.asList(new String[] {"a", "b", "c", "d", "e", "f"});
        assertEquals(c, Utilities.topologicalSort(c, Collections.EMPTY_MAP));
        Map m = new HashMap();
        m.put("e", Collections.singletonList("d"));
        List l = Utilities.topologicalSort(c, m);
        assertNotNull(l);
        assertEquals(Arrays.asList(new String[] {"a", "b", "c"}), l.subList(0, 3));
        m = new HashMap();
        m.put("c", Collections.singletonList("a"));
        l = Utilities.topologicalSort(c, m);
        assertNotNull(l);
        assertEquals(Arrays.asList(new String[] {"d", "e", "f"}), l.subList(3, 6));
        m = new HashMap();
        m.put("a", Collections.singletonList("f"));
        assertEquals(c, Utilities.topologicalSort(c, m));
        m.put("a", Collections.singletonList("b"));
        assertEquals(c, Utilities.topologicalSort(c, m));
        m.put("b", Collections.singletonList("f"));
        assertEquals(c, Utilities.topologicalSort(c, m));
        m.put("c", Collections.singletonList("e"));
        assertEquals(c, Utilities.topologicalSort(c, m));
        m.put("a", Collections.singletonList("e"));
        assertEquals(c, Utilities.topologicalSort(c, m));
        /* Does not work - oh well:
        c = Arrays.asList(new String[] {"a", "b", "c", "d", "e", "x", "y"});
        m = new HashMap();
        m.put("c", Arrays.asList(new String[] {"x", "y"}));
        m.put("x", Collections.singletonList("d"));
        m.put("y", Collections.singletonList("d"));
        System.err.println("-----");
        l = Utilities.topologicalSort(c, m);
        assertNotNull(l);
        System.err.println(l);
        assertEquals(Arrays.asList(new String[] {"a", "b", "c"}), l.subList(0, 3));
        assertEquals(Arrays.asList(new String[] {"d", "e"}), l.subList(5, 7));
        */
    }
    public void testTopologicalSortCompile() throws Exception {
        Collection<String> c = new ArrayList<String>();
        Map<Object, Collection<String>> edges = new HashMap<Object,Collection<String>>();
        
        List<String> result = Utilities.topologicalSort(c, edges);
    }
    
    public void testTopologicalSortCompile2() throws Exception  {
        Collection<String> c = new ArrayList<String>();
        Map<Object, List<String>> edges = new HashMap<Object,List<String>>();
        
        List<String> result = Utilities.topologicalSort(c, edges);
    }
    public void testTopologicalSortCompile3() throws Exception {
        Collection<Number> c = new ArrayList<Number>();
        Map<Object, List<Integer>> edges = new HashMap<Object,List<Integer>>();
        
        List<Number> result = Utilities.topologicalSort(c, edges);
    }    
    
    public void testTopologicalSortIssue101820() throws Exception {
        Object a = "a";
        Object b = "b";
        
        Map<Object, Collection<Object>> deps = new HashMap<Object, Collection<Object>>();
        
        deps.put(a, Arrays.asList(b));
        deps.put(b, Arrays.asList());
        
        assertEquals(Arrays.asList(a), Utilities.topologicalSort(Arrays.asList(a), deps));
     }    

    public void testErrorReporting () throws Exception {
        Collection c = Arrays.asList(new String[] {"a", "b", "c", "d", "e", "f"});
        Map m = new HashMap ();
        m.put ("a", Arrays.asList (new String[] { "a" })); 
        m.put ("b", Arrays.asList (new String[] { "c" })); 
        m.put ("c", Arrays.asList (new String[] { "b" })); 
        
        try {
            Utilities.topologicalSort(c, m);
            fail ("Unsortable");
        } catch (TopologicalSortException ex) {
            StringWriter w = new StringWriter ();
            ex.printStackTrace (new PrintWriter (w, true));
            
            ByteArrayOutputStream s = new ByteArrayOutputStream ();
            ex.printStackTrace (new java.io.PrintStream (s, true));
            
            byte[] warr = w.toString().getBytes("utf-8");
            byte[] sarr = s.toByteArray();
            
            assertTrue ("Both messages should be the same", Arrays.equals(warr, sarr));
            
            {
                String msg = w.toString();

                int cnt = 0;
                int indx = -1;
                for (;;) {
                    indx = msg.indexOf("Conflict #", indx + 1);
                    if (indx == -1) break;
                    cnt++;
                }

                assertEquals(
                    "There is the same number of lines with " +
                    "'Conflict #' as unsortable sets", 
                    cnt, ex.unsortableSets().length
                );
            }
            
            {
                String msg = ex.getMessage();

                int cnt = 0;
                int indx = -1;
                for (;;) {
                    indx = msg.indexOf("Conflict #", indx + 1);
                    if (indx == -1) break;
                    cnt++;
                }

                assertEquals(
                    "There is the same number of lines with " +
                    "'Conflict #' as unsortable sets", 
                    cnt, ex.unsortableSets().length
                );
            }
            
        }
        
        
        
    }

    private static void assertBefore (String first, String second, Collection c) {
        assertBefore (first, second, c, false);
    }
    
    private static void assertBefore (String first, String second, Set[] sets) {
        assertBefore (first, second, Arrays.asList (sets), true);
    }
    
    private static void assertBefore (String first, String second, Collection c, boolean useSets) {
        Iterator it = c.iterator();
        boolean wasFirst = false;
        boolean wasSecond = false;
        while (it.hasNext ()) {
            Object obj = it.next ();
            
            if (useSets ? ((Set)obj).contains (second) : obj == second) {
                assertTrue ("[" + second + "] found before [" + first + "] in " + c, wasFirst);
                wasSecond = true;
            }
            
            if (useSets ? ((Set)obj).contains (first) : obj == first) {
                wasFirst = true;
            }
        }
        
        assertTrue ("[" + first + "] not found", wasFirst);
        assertTrue ("[" + second + "] not found", wasSecond);
    }
}
