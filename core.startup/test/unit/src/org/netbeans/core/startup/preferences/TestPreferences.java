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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.startup.preferences;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
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
public class TestPreferences extends NbPreferencesTest.TestBasicSetup {
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
    

    /* We do not really care about enforcing these limits:
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
     */
    
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
    
    public void testPut2()  throws Exception {
        final Object sync = getEventQueueSync();
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        final List<Object> l = new ArrayList<Object>();
        assertNull(pref.get("key1", null));
        PreferenceChangeListener pl = new PreferenceChangeListener(){
            public void preferenceChange(PreferenceChangeEvent evt) {
                synchronized(sync) {
                    l.add(evt.getNewValue());
                    sync.notifyAll();
                }
            }
        };
        pref.addPreferenceChangeListener(pl);
        try {
            pref.put("key1", "value1");
            assertEquals("value1",pref.get("key1", null));            
            synchronized(sync) {
                sync.wait(5000);
                assertEquals(1, l.size());
            }
            assertEquals("value1",l.get(0));
            l.clear();
            
            pref.put("key1", "value2");
            assertEquals("value2",pref.get("key1", null));
            synchronized(sync) {
                sync.wait(5000);
                assertEquals(1, l.size());
            }
            assertEquals("value2",l.get(0));
            l.clear();
            
            pref.put("key1", "value2");
            assertEquals("value2",pref.get("key1", null));
            synchronized(sync) {
                sync.wait(5000);
                assertEquals(0, l.size());
            }
            l.clear();
            
            pref.put("key1", "value2");
            assertEquals("value2",pref.get("key1", null));
            synchronized(sync) {
                sync.wait(5000);
                assertEquals(0, l.size());
            }
            l.clear();
            
        } finally {
            pref.removePreferenceChangeListener(pl);
        }
    }
    
    private Object getEventQueueSync() {
        try {
            Field f = AbstractPreferences.class.getDeclaredField("eventQueue");
            f.setAccessible(true);
            return f.get(null);
            
        } catch (Exception ex) {
            Logger.getLogger("global").log(java.util.logging.Level.SEVERE,ex.getMessage(), ex);
        }
        return null;
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
    
    public void testIsPersistent()  throws BackingStoreException,InterruptedException {
        NbPreferences pref = (NbPreferences)getPreferencesNode();
        assertNotNull(pref);
        assertEquals(NbPreferences.UserPreferences.class, pref.getClass());
        pref.put("key", "value");
        assertEquals("value", pref.get("key", null));
        pref.sync();
        assertEquals("value", pref.get("key", null));
    }    
    
    protected int timeOut() {
        return 20000;
    }


}
