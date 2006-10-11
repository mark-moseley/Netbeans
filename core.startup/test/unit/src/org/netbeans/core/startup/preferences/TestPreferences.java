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

package org.netbeans.core.startup.preferences;

import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 *
 * @author Radek Matous
 */
public class TestPreferences extends NbPreferencesTest.BasicSetupTest {
    public TestPreferences(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        //if diabled - all tests should pass with default impl. of Preferences API
        assertSame(new NbPreferencesFactory().userRoot(), Preferences.userRoot());
        
        Preferences.userRoot().flush();
    }

   
    private Preferences getPreferencesNode() {
        return getUserPreferencesNode();
    }
    
    private Preferences getUserPreferencesNode() {
        return Preferences.userNodeForPackage(NbPreferencesTest.class).node(getName());
    }
    
    private Preferences getSystemPreferencesNode() {
        return Preferences.systemNodeForPackage(NbPreferencesTest.class).node(getName());
    }
    
    
    public void testUserRoot() throws Exception {
        try {
            Preferences.userRoot().removeNode();
            fail();
        } catch (UnsupportedOperationException ex) {
        }
    }
    
    
    public void testKeyExceededSize() throws Exception {
        Preferences pref = getPreferencesNode();
        StringBuffer sb = new StringBuffer();
        for (; sb.toString().length() < Preferences.MAX_KEY_LENGTH + 1; ) {
            sb.append("1234567890");
        }
        assertTrue(sb.toString().length() > Preferences.MAX_KEY_LENGTH);
        try {
            pref.put(sb.toString(),"sss");
            fail();
        } catch (IllegalArgumentException iax) {
        }
    }
    
    public void testValueExceededSize() throws Exception {
        Preferences pref = getPreferencesNode();
        StringBuffer sb = new StringBuffer();
        for (; sb.toString().length() < Preferences.MAX_VALUE_LENGTH + 1; ) {
            sb.append("1234567890");
        }
        assertTrue(sb.toString().length() > Preferences.MAX_VALUE_LENGTH);
        try {
            pref.put("sss", sb.toString());
            fail();
        } catch (IllegalArgumentException iax) {
        }
    }
    
    public void testNameExceededSize() throws Exception {
        StringBuffer sb = new StringBuffer();
        for (; sb.toString().length() < Preferences.MAX_NAME_LENGTH + 1; ) {
            sb.append("1234567890");
        }
        
        assertTrue(sb.toString().length() > Preferences.MAX_NAME_LENGTH);
        try {
            Preferences pref =
                    getPreferencesNode().node(sb.toString());
            fail();
        } catch (IllegalArgumentException iax) {
        }
    }
    
    public void testNullParameter() throws Exception {
        Preferences pref = getPreferencesNode();
        try {
            pref.get(null, "value");
            fail();
        } catch(NullPointerException npe) {
        }
        
        try {
            //null permited here
            pref.get("key", null);
        } catch(NullPointerException npe) {
            fail();
        }
        
        try {
            pref.node(null);
            fail();
        } catch(NullPointerException npe) {
        }
        
        try {
            pref.node("node2/");
            fail();
        } catch(IllegalArgumentException iax) {
        }
        
    }
    
    public void testIsUserNode() {
        Preferences upref = getUserPreferencesNode();
        Preferences spref = getSystemPreferencesNode();
        assertTrue(upref.isUserNode());
        assertFalse(spref.isUserNode());
    }
    
    public void testNode() throws BackingStoreException {
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        assertTrue(pref.nodeExists(""));
        assertFalse(pref.nodeExists("sub1"));
        assertFalse(Arrays.asList(pref.childrenNames()).contains("sub1"));
        
        Preferences sub1 =pref.node("sub1");
        assertTrue(pref.nodeExists("sub1"));
        assertTrue(Arrays.asList(pref.childrenNames()).contains("sub1"));
    }
    
    public void testChildrenNames() throws Exception {
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        assertEquals(0, pref.childrenNames().length);
        
        assertFalse(pref.nodeExists("sub1"));
        Preferences sub1 =pref.node("sub1");
        assertNotNull(sub1);
        assertTrue(pref.nodeExists("sub1"));
        assertEquals(1, pref.childrenNames().length);
        
        
        assertFalse(pref.nodeExists("sub2"));
        Preferences sub2 =pref.node("sub2");
        assertNotNull(sub2);
        assertTrue(pref.nodeExists("sub2"));
        assertEquals(2, pref.childrenNames().length);
        
        sub1.removeNode();
        assertEquals(1, pref.childrenNames().length);
        sub2.removeNode();
        assertEquals(0, pref.childrenNames().length);
    }
    
    public void testPut()  {
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        
        assertNull(pref.get("key1", null));
        pref.put("key1", "value1");
        assertEquals("value1",pref.get("key1", null));
    }
    
    
    public void testRemove() {
        testPut();
        Preferences pref = getPreferencesNode();
        assertEquals("value1",pref.get("key1", null));
        pref.remove("key1");
        assertNull(pref.get("key1", null));
    }
    
    public void testClear() throws Exception {
        testKeys();
        Preferences pref = getPreferencesNode();
        pref.clear();
        assertEquals(0,pref.keys().length);
        assertNull(pref.get("key1", null));
        assertNull(pref.get("key2", null));
    }
    
    public void testKeys() throws Exception {
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        assertEquals(0,pref.keys().length);
        pref.put("key1", "value1");
        pref.put("key2", "value2");
        assertEquals(2,pref.keys().length);
    }
    
    
    public void testParent() {
        Preferences pref = getPreferencesNode();
        Preferences pref2 = pref.node("1/2/3");
        assertNotSame(pref, pref2);
        
        for (int i = 0; i < 3; i++) {
            pref2 = pref2.parent();
        }
        
        assertSame(pref2.absolutePath(), pref, pref2);
    }
    
    public void testNodeExists() throws Exception {
        Preferences pref = getPreferencesNode();
        Preferences pref2 = pref.node("a/b/c");
        while(pref2 != Preferences.userRoot()) {
            assertTrue(pref2.nodeExists(""));
            Preferences parent = pref2.parent();
            pref2.removeNode();
            assertFalse(pref2.nodeExists(""));
            pref2 = parent;
        }
        
        assertNotNull(getPreferencesNode().node("a/b/c/d"));
        assertTrue(getPreferencesNode().node("a/b/c/d").nodeExists(""));
    }
    
    
    public void testName() {
        Preferences pref = getPreferencesNode();
        assertEquals("myname",pref.node("myname").name());
    }
    
    public void testAbsolutePath() {
        String validPath = "/a/b/c/d";
        Preferences pref = Preferences.userRoot().node(validPath);
        assertEquals(validPath, pref.absolutePath());
        
        //relative path
        assertSame(pref, pref.parent().node("d"));
        
        String invalidPath = "/a/b/c/d/";
        try {
            pref = Preferences.userRoot().node(invalidPath);
            fail();
        } catch(IllegalArgumentException iax) {}
        
    }
    
    public void testAddPreferenceChangeListener() throws BackingStoreException, InterruptedException {
        final Preferences pref = getPreferencesNode();
        PreferenceChangeListener l = new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent evt) {
                synchronized (TestPreferences.class) {
                    //assertionerrors cause deadlock here
                    assertSame(pref, evt.getNode());
                    assertEquals("key", evt.getKey());
                    assertEquals(evt.getNewValue(),pref.get(evt.getKey(),null), evt.getNewValue());
                    TestPreferences.class.notifyAll();
                }
            }
        };
        pref.addPreferenceChangeListener(l);
        try {
            synchronized (TestPreferences.class) {
                pref.put("key","AddPreferenceChangeListener");
                pref.flush();
                TestPreferences.class.wait();
            }
            
            synchronized (TestPreferences.class) {
                pref.remove("key");
                pref.flush();
                TestPreferences.class.wait();
            }
            
            synchronized (TestPreferences.class) {
                pref.put("key","AddPreferenceChangeListener2");
                pref.flush();
                TestPreferences.class.wait();
            }
        } finally {
            pref.removePreferenceChangeListener(l);
        }
    }
    
    public void testAddNodeChangeListener() throws BackingStoreException, InterruptedException {
        final Preferences pref = getPreferencesNode();
        NodeChangeListener l = new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) {
                synchronized (TestPreferences.class){
                    //assertionerrors cause deadlock here
                    assertSame(pref, evt.getParent());
                    assertEquals("added",evt.getChild().name());
                    TestPreferences.class.notifyAll();
                }
            }
            
            public void childRemoved(NodeChangeEvent evt) {
                synchronized (TestPreferences.class) {
                    //assertionerrors cause deadlock here
                    assertSame(pref, evt.getParent());
                    assertEquals("added",evt.getChild().name());
                    TestPreferences.class.notifyAll();
                }
            }
        };
        pref.addNodeChangeListener(l);
        try {
            Preferences added;
            synchronized (TestPreferences.class) {
                added = pref.node("added");
                TestPreferences.class.wait();
            }
            
            synchronized (TestPreferences.class) {
                added.removeNode();
                TestPreferences.class.wait();
            }
            
        } finally {
            pref.removeNodeChangeListener(l);
        }
    }

    protected int timeOut() {
        return 20000;
    }


}
