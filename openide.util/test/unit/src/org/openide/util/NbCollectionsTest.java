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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.netbeans.junit.NbTestCase;

/**
 * @author Jesse Glick
 */
@SuppressWarnings("unchecked")
public class NbCollectionsTest extends NbTestCase {

    public NbCollectionsTest(String name) {
        super(name);
    }

    public void testCheckedSetByCopy() throws Exception {
        Set s = new HashSet();
        s.add(1);
        s.add(2);
        Set<Integer> checked = NbCollections.checkedSetByCopy(s, Integer.class, true);
        assertEquals(s, checked);
        s.add("three");
        try {
            NbCollections.checkedSetByCopy(s, Integer.class, true);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertEquals(checked, NbCollections.checkedSetByCopy(s, Integer.class, false));
        s.remove("three");
        s.add(null);
        checked = NbCollections.checkedSetByCopy(s, Integer.class, true);
        assertEquals("nulls preserved", s, checked);
        s.clear();
        s.add(5);
        assertEquals("modifications to original not reflected", 3, checked.size());
    }

    public void testCheckedListByCopy() throws Exception {
        doTestCheckedListByCopy(new ArrayList());
        doTestCheckedListByCopy(new LinkedList());
    }
    
    private void doTestCheckedListByCopy(List l) {
        l.add(1);
        l.add(2);
        List<Integer> checked = NbCollections.checkedListByCopy(l, Integer.class, true);
        assertEquals(l, checked);
        l.add("three");
        try {
            NbCollections.checkedListByCopy(l, Integer.class, true);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertEquals(checked, NbCollections.checkedListByCopy(l, Integer.class, false));
        l.remove("three");
        l.add(null);
        checked = NbCollections.checkedListByCopy(l, Integer.class, true);
        assertEquals("nulls preserved", l, checked);
        l.clear();
        l.add(5);
        assertEquals("modifications to original not reflected", 3, checked.size());
    }

    public void testCheckedMapByCopy() throws Exception {
        Map m = new HashMap();
        m.put(1, "hello");
        m.put(2, "goodbye");
        Map<Integer,String> checked = NbCollections.checkedMapByCopy(m, Integer.class, String.class, true);
        assertEquals(m, checked);
        m.put(2, new Object());
        try {
            NbCollections.checkedMapByCopy(m, Integer.class, String.class, true);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertEquals(Collections.singletonMap(1, "hello"), NbCollections.checkedMapByCopy(m, Integer.class, String.class, false));
        m.remove(2);
        Long three = 3L;
        m.put(three, "oops!");
        try {
            NbCollections.checkedMapByCopy(m, Integer.class, String.class, true);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertEquals(Collections.singletonMap(1, "hello"), NbCollections.checkedMapByCopy(m, Integer.class, String.class, false));
        m.remove(three);
        m.put(null, null);
        checked = NbCollections.checkedMapByCopy(m, Integer.class, String.class, true);
        assertEquals("nulls preserved", m, checked);
        m.clear();
        m.put(5, "new");
        assertEquals("modifications to original not reflected", 2, checked.size());
    }

    public void testCheckedIteratorByFilter() throws Exception {
        Iterator raw = Arrays.asList("one", 2, "three").iterator();
        Iterator<String> strings = NbCollections.checkedIteratorByFilter(raw, String.class, false);
        assertTrue(strings.hasNext());
        assertEquals("one", strings.next());
        assertTrue(strings.hasNext());
        assertEquals("three", strings.next());
        assertFalse(strings.hasNext());
        raw = Arrays.asList("one", 2, "three").iterator();
        strings = NbCollections.checkedIteratorByFilter(raw, String.class, true);
        try {
            while (strings.hasNext()) {
                strings.next();
            }
            fail();
        } catch (ClassCastException e) {/*OK*/}
        raw = Arrays.asList("one", "three").iterator();
        strings = NbCollections.checkedIteratorByFilter(raw, String.class, true);
        assertTrue(strings.hasNext());
        assertEquals("one", strings.next());
        assertTrue(strings.hasNext());
        assertEquals("three", strings.next());
        assertFalse(strings.hasNext());
        List l = new ArrayList(Arrays.asList(new Object[] {"one", 2, "three"}));
        raw = l.iterator();
        strings = NbCollections.checkedIteratorByFilter(raw, String.class, false);
        assertTrue(strings.hasNext());
        assertEquals("one", strings.next());
        strings.remove();
        assertEquals(2, l.size());
        assertTrue(strings.hasNext());
        assertEquals("three", strings.next());
        assertFalse(strings.hasNext());
    }

    public void testCheckedSetByFilter() throws Exception {
        Set s = new HashSet();
        s.add("hello");
        s.add("there");
        s.add(1);
        s.add("goodbye");
        s.add(2);
        Set<String> s2 = NbCollections.checkedSetByFilter(s, String.class, false);
        assertEquals(3, s2.size());
        assertEquals(new HashSet(Arrays.asList(new String[] {"hello", "there", "goodbye"})), s2);
        assertTrue(s2.contains("hello"));
        assertFalse(s2.contains("nowhere"));
        try {
            s2.contains(2);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        Iterator<String> it = s2.iterator();
        while (it.hasNext()) {
            if (it.next().equals("hello")) {
                it.remove();
            }
        }
        assertEquals(2, s2.size());
        assertEquals(new HashSet(Arrays.asList(new String[] {"there", "goodbye"})), s2);
        assertEquals(4, s.size());
        it = s2.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertEquals(0, s2.size());
        assertEquals(Collections.emptySet(), s2);
        assertEquals(new HashSet(Arrays.asList(new Integer[] {1, 2})), s);
        s.clear();
        s.add("new");
        assertEquals("modifications to original found", Collections.singleton("new"), s2);
        assertTrue(s2.add("additional"));
        assertEquals("original set modified too", new HashSet(Arrays.asList(new String[] {"new", "additional"})), s);
        try {
            ((Set) s2).add(13);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        // Other:
        assertEquals("preserved by serialization", s2, cloneBySerialization(s2));
        assertEquals("empty set filtered as empty", Collections.emptySet(), NbCollections.checkedSetByFilter(Collections.emptySet(), String.class, false));
        assertEquals("empty set from wholly wrong set", Collections.emptySet(), NbCollections.checkedSetByFilter(Collections.singleton(5), String.class, false));
        // Make sure iterator behaves fully acc. to contract:
        Set<Integer> s3 = NbCollections.checkedSetByFilter(new HashSet(Collections.singleton(1)), Integer.class, false);
        Iterator<Integer> it3 = s3.iterator();
        assertTrue(it3.hasNext());
        assertTrue(it3.hasNext());
        assertEquals(new Integer(1), it3.next());
        assertFalse(it3.hasNext());
        assertFalse(it3.hasNext());
        try {
            it3.next();
            fail();
        } catch (NoSuchElementException e) {/*OK*/}
        it3 = s3.iterator();
        try {
            it3.remove();
            fail();
        } catch (IllegalStateException e) {/*OK*/}
        it3 = s3.iterator();
        it3.next();
        it3.remove();
        try {
            it3.remove();
            fail();
        } catch (IllegalStateException e) {/*OK*/}
    }

    public void testCheckedSetByFilterStrict() throws Exception {
        Set s = new HashSet();
        s.add("hello");
        s.add("there");
        s.add(1);
        s.add("goodbye");
        s.add(2);
        Set<String> s2 = NbCollections.checkedSetByFilter(s, String.class, true);
        try {
            s2.size();
            fail();
        } catch (ClassCastException x) {/*OK*/}
        try {
            new HashSet<String>(s2);
            fail();
        } catch (ClassCastException x) {/*OK*/}
        s.remove(1);
        s.remove(2);
        assertEquals(3, s2.size());
        assertTrue(s2.contains("hello"));
        try {
            s2.contains(2);
            fail();
        } catch (ClassCastException e) {/*OK*/}
    }

    public void testCheckedMapByFilter() throws Exception {
        Map m = new HashMap();
        m.put(1, "one");
        m.put(2, "two");
        m.put("three", "three");
        m.put(4, 4);
        Map<Integer,String> m2 = NbCollections.checkedMapByFilter(m, Integer.class, String.class, false);
        assertEquals(2, m2.size());
        assertEquals("one", m2.get(1));
        try {
            m2.get("three");
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertEquals(null, m2.get(4));
        assertTrue(m2.containsKey(1));
        assertFalse(m2.containsKey(5));
        try {
            m2.containsKey("three");
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertFalse(m2.containsKey(4));
        assertTrue(m2.containsValue("one"));
        assertFalse(m2.containsValue("five"));
        try {
            m2.containsValue(3);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertFalse(m2.containsValue("three"));
        assertEquals(2, m2.entrySet().size());
        assertEquals(2, m2.keySet().size());
        assertEquals(2, m2.values().size());
        assertTrue(m2.keySet().contains(1));
        assertFalse(m2.keySet().contains(5));
        try {
            m2.keySet().contains("three");
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertFalse(m2.keySet().contains(4));
        assertTrue(m2.values().contains("one"));
        assertFalse(m2.values().contains("five"));
        try {
            m2.values().contains(4);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        assertFalse(m2.values().contains("three"));
        // Destructive operations:
        m2.put(1, "#one");
        assertEquals("#one", m2.get(1));
        assertEquals("#one", m.get(1));
        try {
            ((Map) m2).put("five", "five");
            fail();
        } catch (ClassCastException e) {/*OK*/}
        try {
            ((Map) m2).put(5, 5);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        m2.remove(1);
        assertEquals(Collections.singletonMap(2, "two"), m2);
        assertEquals(3, m.size());
        m2.entrySet().clear();
        assertTrue(m2.isEmpty());
        assertEquals(2, m.size());
        m.clear();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, 3);
        m.put("four", "four");
        assertTrue(m2.keySet().remove(1));
        assertFalse(m2.keySet().remove(3));
        assertFalse(m2.keySet().remove("four"));
        assertEquals(Collections.singletonMap(2, "two"), m2);
        m.put(1, "one");
        assertTrue(m2.values().remove("one"));
        assertFalse(m2.values().remove(3));
        assertFalse(m2.values().remove("four"));
        assertEquals(Collections.singletonMap(2, "two"), m2);
        assertEquals(3, m.size());
        // Other:
        assertEquals(m2, cloneBySerialization(m2));
        assertEquals(Collections.emptyMap(), NbCollections.checkedMapByFilter(Collections.emptyMap(), String.class, String.class, false));
        assertEquals(Collections.emptyMap(), NbCollections.checkedMapByFilter(Collections.singletonMap(1, "two"), String.class, String.class, false));
        assertEquals(Collections.emptyMap(), NbCollections.checkedMapByFilter(Collections.singletonMap("one", 2), String.class, String.class, false));
        // Misc. return values have to reflect nature of view:
        m.clear();
        m.put(1, 1);
        assertEquals(null, m2.put(1, "one"));
        m.put(1, 1);
        assertEquals(null, m2.remove(1));
        m.put(1, "one");
        assertEquals("one", m2.put(1, "#one"));
        assertEquals("#one", m2.remove(1));
    }

    public void testCheckedMapByFilterStrict() throws Exception {
        Map m = new HashMap();
        m.put(1, "one");
        m.put(2, "two");
        m.put("three", "three");
        m.put(4, 4);
        Map<Integer,String> m2 = NbCollections.checkedMapByFilter(m, Integer.class, String.class, true);
        try {
            m2.size();
            fail();
        } catch (ClassCastException e) {/*OK*/}
        try {
            new HashMap<Integer,String>(m2);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        try {
            m2.get("three");
            fail();
        } catch (ClassCastException e) {/*OK*/}
        m.remove("three");
        try {
            m2.size();
            fail();
        } catch (ClassCastException e) {/*OK*/}
        m.remove(4);
        m.put("three", "three");
        try {
            m2.size();
            fail();
        } catch (ClassCastException e) {/*OK*/}
        m.remove("three");
        assertEquals(2, m2.size());
        assertEquals("one", m2.get(1));
    }

    public void testCheckedEnumerationByFilter() throws Exception {
        Enumeration raw = Collections.enumeration(Arrays.asList("one", 2, "three"));
        Enumeration<String> strings = NbCollections.checkedEnumerationByFilter(raw, String.class, false);
        assertTrue(strings.hasMoreElements());
        assertEquals("one", strings.nextElement());
        assertTrue(strings.hasMoreElements());
        assertEquals("three", strings.nextElement());
        assertFalse(strings.hasMoreElements());
    }

    public void testCheckedEnumerationByFilterStrict() throws Exception {
        Enumeration raw = Collections.enumeration(Arrays.asList("one", 2, "three"));
        Enumeration<String> strings = NbCollections.checkedEnumerationByFilter(raw, String.class, true);
        try {
            Collections.list(strings);
            fail();
        } catch (ClassCastException e) {/*OK*/}
        raw = Collections.enumeration(Arrays.asList("one", "three"));
        strings = NbCollections.checkedEnumerationByFilter(raw, String.class, true);
        assertTrue(strings.hasMoreElements());
        assertEquals("one", strings.nextElement());
        assertTrue(strings.hasMoreElements());
        assertEquals("three", strings.nextElement());
        assertFalse(strings.hasMoreElements());
    }

    static Object cloneBySerialization(Object o) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return new ObjectInputStream(bais).readObject();
    }

}
