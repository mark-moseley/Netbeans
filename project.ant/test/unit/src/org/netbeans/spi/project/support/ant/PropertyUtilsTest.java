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

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Test functionality of PropertyUtils.
 * @author Jesse Glick
 */
public class PropertyUtilsTest extends NbTestCase {
    
    public PropertyUtilsTest(String name) {
        super(name);
    }
    
    static {
        PropertyUtilsTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("netbeans.user", System.getProperty("java.io.tmpdir"));
        assertEquals("correct build.properties location",
            new File(System.getProperty("java.io.tmpdir"), "build.properties"),
            PropertyUtils.USER_BUILD_PROPERTIES);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        if (PropertyUtils.USER_BUILD_PROPERTIES.exists() && !PropertyUtils.USER_BUILD_PROPERTIES.delete()) {
            throw new IOException("Deleting: " + PropertyUtils.USER_BUILD_PROPERTIES);
        }
    }
    
    public void testEvaluate() throws Exception {
        // XXX check override order, property name evaluation, $$ escaping, bare or final $,
        // cyclic errors, undef'd property substitution, no substs in predefs, etc.
        Map/*<String,String>*/ m1 = Collections.singletonMap("y", "val");
        Map/*<String,String>*/ m2 = new HashMap();
        m2.put("x", "${y}");
        m2.put("y", "y-${x}");
        List/*<Map<String,String>>*/ m1m2 = Arrays.asList(new Map/*<String,String>*/[] {m1, m2});
        assertEquals("x evaluates to former y", "val", PropertyUtils.evaluate("x", Collections.EMPTY_MAP, m1m2));
        assertEquals("first y defines it", "val", PropertyUtils.evaluate("y", Collections.EMPTY_MAP, m1m2));
        assertEquals("circularity error", null, PropertyUtils.evaluate("x", Collections.EMPTY_MAP, Collections.singletonList(m2)));
        assertEquals("circularity error", null, PropertyUtils.evaluate("y", Collections.EMPTY_MAP, Collections.singletonList(m2)));
        m2.clear();
        m2.put("y", "yval_${z}");
        m2.put("x", "xval_${y}");
        m2.put("z", "zval");
        Map all = PropertyUtils.evaluateAll(Collections.EMPTY_MAP, Collections.singletonList(m2));
        assertNotNull("no circularity error", all);
        assertEquals("have three properties", 3, all.size());
        assertEquals("double substitution", "xval_yval_zval", all.get("x"));
        assertEquals("single substitution", "yval_zval", all.get("y"));
        assertEquals("no substitution", "zval", all.get("z"));
        // Yuck. But it failed once, so check it now.
        Properties p = new Properties();
        p.load(new ByteArrayInputStream("project.mylib=../mylib\njavac.classpath=${project.mylib}/build/mylib.jar\nrun.classpath=${javac.classpath}:build/classes".getBytes("US-ASCII")));
        all = PropertyUtils.evaluateAll(Collections.EMPTY_MAP, Collections.singletonList(p));
        assertNotNull("no circularity error", all);
        assertEquals("javac.classpath correctly substituted", "../mylib/build/mylib.jar", all.get("javac.classpath"));
        assertEquals("run.classpath correctly substituted", "../mylib/build/mylib.jar:build/classes", all.get("run.classpath"));
    }
    
    public void testTokenizePath() throws Exception {
        assertEquals("basic tokenization works on ':'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("foo:bar")));
            assertEquals("basic tokenization works on ';'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("foo;bar")));
            assertEquals("Unix paths work",
                Arrays.asList(new String[] {"/foo/bar", "baz/quux"}),
                Arrays.asList(PropertyUtils.tokenizePath("/foo/bar:baz/quux")));
            assertEquals("empty components are stripped with ':'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath(":foo::bar:")));
            assertEquals("empty components are stripped with ';'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath(";foo;;bar;")));
            assertEquals("DOS paths are recognized with ';'",
                Arrays.asList(new String[] {"c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:\\foo;D:\\\\bar")));
            assertEquals("DOS paths are recognized with ':'",
                Arrays.asList(new String[] {"c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:\\foo:D:\\\\bar")));
            assertEquals("a..z can be drive letters",
                Arrays.asList(new String[] {"a:\\foo", "z:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("a:\\foo:z:\\\\bar")));
            assertEquals("A..Z can be drive letters",
                Arrays.asList(new String[] {"A:\\foo", "Z:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("A:\\foo:Z:\\\\bar")));
            assertEquals("non-letters are not drives with ';'",
                Arrays.asList(new String[] {"1", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("1;\\foo;D:\\\\bar")));
            assertEquals("non-letters are not drives with ':'",
                Arrays.asList(new String[] {"1", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("1:\\foo:D:\\\\bar")));
            assertEquals(">1 letters are not drives with ';'",
                Arrays.asList(new String[] {"ab", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("ab;\\foo;D:\\\\bar")));
            assertEquals(">1 letters are not drives with ':'",
                Arrays.asList(new String[] {"ab", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("ab:\\foo:D:\\\\bar")));
            assertEquals("drives use ':'",
                Arrays.asList(new String[] {"c", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c;\\foo;D:\\\\bar")));
            assertEquals("drives use only one ':'",
                Arrays.asList(new String[] {"c", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c::\\foo;D:\\\\bar")));
            assertEquals("drives use only one drive letter",
                Arrays.asList(new String[] {"c", "c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:c:\\foo;D:\\\\bar")));
            assertEquals("DOS paths start with '\\'",
                Arrays.asList(new String[] {"c", "foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:foo;D:\\\\bar")));
            assertEquals("empty path handled",
                Collections.EMPTY_LIST,
                Arrays.asList(PropertyUtils.tokenizePath("")));
            assertEquals("effectively empty path handled",
                Collections.EMPTY_LIST,
                Arrays.asList(PropertyUtils.tokenizePath(":;:;")));
    }
    
    public void testRelativizeFile() throws Exception {
        clearWorkDir();
        File tmp = getWorkDir();
        File d1 = new File(tmp, "d1");
        File d1f = new File(d1, "f");
        File d1s = new File(d1, "s p a c e");
        File d1sf = new File(d1s, "f");
        File d2 = new File(tmp, "d2");
        File d2f = new File(d2, "f");
        // Note that "/tmp/d11".startsWith("/tmp/d1"), hence this being interesting:
        File d11 = new File(tmp, "d11");
        // Note: none of these dirs/files exist yet.
        assertEquals("d1f from d1", "f", PropertyUtils.relativizeFile(d1, d1f));
        assertEquals("d2f from d1", "../d2/f", PropertyUtils.relativizeFile(d1, d2f));
        assertEquals("d1 from d1", ".", PropertyUtils.relativizeFile(d1, d1));
        assertEquals("d2 from d1", "../d2", PropertyUtils.relativizeFile(d1, d2));
        assertEquals("d1s from d1", "s p a c e", PropertyUtils.relativizeFile(d1, d1s));
        assertEquals("d1sf from d1", "s p a c e/f", PropertyUtils.relativizeFile(d1, d1sf));
        assertEquals("d11 from d1", "../d11", PropertyUtils.relativizeFile(d1, d11));
        // Now make them and check that the results are the same.
        assertTrue("made d1s", d1s.mkdirs());
        assertTrue("made d1f", d1f.createNewFile());
        assertTrue("made d1sf", d1sf.createNewFile());
        assertTrue("made d2", d2.mkdirs());
        assertTrue("made d2f", d2f.createNewFile());
        assertEquals("existing d1f from d1", "f", PropertyUtils.relativizeFile(d1, d1f));
        assertEquals("existing d2f from d1", "../d2/f", PropertyUtils.relativizeFile(d1, d2f));
        assertEquals("existing d1 from d1", ".", PropertyUtils.relativizeFile(d1, d1));
        assertEquals("existing d2 from d1", "../d2", PropertyUtils.relativizeFile(d1, d2));
        assertEquals("existing d1s from d1", "s p a c e", PropertyUtils.relativizeFile(d1, d1s));
        assertEquals("existing d1sf from d1", "s p a c e/f", PropertyUtils.relativizeFile(d1, d1sf));
        assertEquals("existing d11 from d1", "../d11", PropertyUtils.relativizeFile(d1, d11));
        // XXX: the below code should pass on Unix too I guess.
        if (Utilities.isWindows()) {
            // test Windows drives:
            File f1 = new File("C:\\folder\\one");
            File f2 = new File("D:\\t e m p\\two");
            assertNull("different drives cannot be relative", PropertyUtils.relativizeFile(f1, f2));
            f1 = new File("D:\\folder\\one");
            f2 = new File("D:\\t e m p\\two");
            assertEquals("relativization failed for Windows absolute paths", "../../t e m p/two", PropertyUtils.relativizeFile(f1, f2));
        }
    }
    
    public void testGlobalProperties() throws Exception {
        assertFalse("no build.properties yet", PropertyUtils.USER_BUILD_PROPERTIES.exists());
        assertEquals("no properties to start", Collections.EMPTY_MAP, PropertyUtils.getGlobalProperties());
        EditableProperties p = new EditableProperties();
        p.setProperty("key1", "val1");
        p.setProperty("key2", "val2");
        PropertyUtils.putGlobalProperties(p);
        assertTrue("now have build.properties", PropertyUtils.USER_BUILD_PROPERTIES.isFile());
        p = PropertyUtils.getGlobalProperties();
        assertEquals("two definitions now", 2, p.size());
        assertEquals("key1 correct", "val1", p.getProperty("key1"));
        assertEquals("key2 correct", "val2", p.getProperty("key2"));
        Properties p2 = new Properties();
        InputStream is = new FileInputStream(PropertyUtils.USER_BUILD_PROPERTIES);
        try {
            p2.load(is);
        } finally {
            is.close();
        }
        assertEquals("two definitions now from disk", 2, p2.size());
        assertEquals("key1 correct from disk", "val1", p2.getProperty("key1"));
        assertEquals("key2 correct from disk", "val2", p2.getProperty("key2"));
        // Test the property provider too.
        PropertyProvider gpp = PropertyUtils.globalPropertyProvider();
        TestCL l = new TestCL();
        gpp.addChangeListener(l);
        p = PropertyUtils.getGlobalProperties();
        assertEquals("correct initial definitions", p, gpp.getProperties());
        p.setProperty("key3", "val3");
        assertEquals("still have 2 defs", 2, gpp.getProperties().size());
        assertFalse("no changes yet", l.changed);
        PropertyUtils.putGlobalProperties(p);
        assertTrue("got a change", l.changed);
        l.changed = false;
        assertEquals("now have 3 defs", 3, gpp.getProperties().size());
        assertEquals("right val", "val3", gpp.getProperties().get("key3"));
        assertFalse("no spurious changes", l.changed);
        // XXX try modifying build.properties using Filesystems API
    }
    
    public void testEvaluateString() throws Exception {
        Map predefs = new HashMap();
        predefs.put("homedir", "/home/me");
        Map defs1 = new HashMap();
        defs1.put("outdirname", "foo");
        defs1.put("outdir", "${homedir}/${outdirname}");
        Map defs2 = new HashMap();
        defs2.put("outdir2", "${outdir}/subdir");
        assertEquals("correct evaluated string",
            "/home/me/foo/subdir is in /home/me",
            PropertyUtils.evaluateString("${outdir2} is in ${homedir}", predefs,
                                         Arrays.asList(new Map[] {defs1, defs2})));
    }
    
    public void testFixedPropertyProvider() throws Exception {
        Map defs = new HashMap();
        defs.put("key1", "val1");
        defs.put("key2", "val2");
        PropertyProvider pp = PropertyUtils.fixedPropertyProvider(defs);
        assertEquals(defs, pp.getProperties());
    }
    
    public void testSequentialEvaluatorBasic() throws Exception {
        Map defs1 = new HashMap();
        defs1.put("key1", "val1");
        defs1.put("key2", "val2");
        defs1.put("key5", "5=${key1}");
        defs1.put("key6", "6=${key3}");
        Map defs2 = new HashMap();
        defs2.put("key3", "val3");
        defs2.put("key4", "4=${key1}:${key3}");
        defs2.put("key7", "7=${undef}");
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[] {
            PropertyUtils.fixedPropertyProvider(defs1),
            PropertyUtils.fixedPropertyProvider(defs2),
        });
        String[] vals = {
            "val1",
            "val2",
            "val3",
            "4=val1:val3",
            "5=val1",
            "6=${key3}",
            "7=${undef}",
        };
        Map all = eval.getProperties();
        assertEquals("right # of props", vals.length, all.size());
        for (int i = 1; i <= vals.length; i++) {
            assertEquals("key" + i + " is correct", vals[i - 1], eval.getProperty("key" + i));
            assertEquals("key" + i + " is correct in all properties", vals[i - 1], all.get("key" + i));
        }
        assertEquals("evaluate works", "5=val1 x ${undef}", eval.evaluate("${key5} x ${undef}"));
        // And test the preprovider...
        Map predefs = Collections.singletonMap("key3", "preval3");
        eval = PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.fixedPropertyProvider(predefs), new PropertyProvider[] {
            PropertyUtils.fixedPropertyProvider(defs1),
            PropertyUtils.fixedPropertyProvider(defs2),
        });
        vals = new String[] {
            "val1",
            "val2",
            "preval3",
            "4=val1:preval3",
            "5=val1",
            "6=preval3",
            "7=${undef}",
        };
        all = eval.getProperties();
        assertEquals("right # of props", vals.length, all.size());
        for (int i = 1; i <= vals.length; i++) {
            assertEquals("key" + i + " is correct", vals[i - 1], eval.getProperty("key" + i));
            assertEquals("key" + i + " is correct in all properties", vals[i - 1], all.get("key" + i));
        }
        assertEquals("evaluate works", "4=val1:preval3 x ${undef} x preval3", eval.evaluate("${key4} x ${undef} x ${key3}"));
    }
    
    public void testSequentialEvaluatorChanges() throws Exception {
        TestMutablePropertyProvider predefs = new TestMutablePropertyProvider(new HashMap());
        TestMutablePropertyProvider defs1 = new TestMutablePropertyProvider(new HashMap());
        TestMutablePropertyProvider defs2 = new TestMutablePropertyProvider(new HashMap());
        predefs.defs.put("x", "xval1");
        predefs.defs.put("y", "yval1");
        defs1.defs.put("a", "aval1");
        defs1.defs.put("b", "bval1=${x}");
        defs1.defs.put("c", "cval1=${z}");
        defs2.defs.put("m", "mval1");
        defs2.defs.put("n", "nval1=${x}:${b}");
        defs2.defs.put("o", "oval1=${z}");
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefs, new PropertyProvider[] {
            defs1,
            defs2,
        });
        TestPCL l = new TestPCL();
        eval.addPropertyChangeListener(l);
        Map/*<String,String>*/ result = new HashMap();
        result.put("x", "xval1");
        result.put("y", "yval1");
        result.put("a", "aval1");
        result.put("b", "bval1=xval1");
        result.put("c", "cval1=${z}");
        result.put("m", "mval1");
        result.put("n", "nval1=xval1:bval1=xval1");
        result.put("o", "oval1=${z}");
        assertEquals("correct initial vals", result, eval.getProperties());
        assertEquals("no changes yet", Collections.EMPTY_SET, l.changed);
        // Change predefs.
        predefs.defs.put("x", "xval2");
        predefs.mutated();
        Map/*<String,String>*/ oldvals = new HashMap();
        oldvals.put("x", result.get("x"));
        oldvals.put("b", result.get("b"));
        oldvals.put("n", result.get("n"));
        Map/*<String,String>*/ newvals = new HashMap();
        newvals.put("x", "xval2");
        newvals.put("b", "bval1=xval2");
        newvals.put("n", "nval1=xval2:bval1=xval2");
        result.putAll(newvals);
        assertEquals("some changes", newvals.keySet(), l.changed);
        assertEquals("right old values", oldvals, l.oldvals);
        assertEquals("right new values", newvals, l.newvals);
        assertEquals("right total values now", result, eval.getProperties());
        l.reset();
        // Change some other defs.
        defs1.defs.put("z", "zval1");
        defs1.defs.remove("b");
        defs1.mutated();
        defs2.defs.put("m", "mval2");
        defs2.mutated();
        oldvals.clear();
        oldvals.put("b", result.get("b"));
        oldvals.put("c", result.get("c"));
        oldvals.put("m", result.get("m"));
        oldvals.put("n", result.get("n"));
        oldvals.put("o", result.get("o"));
        oldvals.put("z", result.get("z"));
        newvals.clear();
        newvals.put("b", null);
        newvals.put("c", "cval1=zval1");
        newvals.put("m", "mval2");
        newvals.put("n", "nval1=xval2:${b}");
        newvals.put("o", "oval1=zval1");
        newvals.put("z", "zval1");
        result.putAll(newvals);
        result.remove("b");
        assertEquals("some changes", newvals.keySet(), l.changed);
        assertEquals("right old values", oldvals, l.oldvals);
        assertEquals("right new values", newvals, l.newvals);
        assertEquals("right total values now", result, eval.getProperties());
        l.reset();
    }
    
    private static final String ILLEGAL_CHARS = " !\"#$%&'()*+,/:;<=>?@[\\]^`{|}~";
    
    public void testIsUsablePropertyName() throws Exception {
        for (int i=0; i<ILLEGAL_CHARS.length(); i++) {
            String s = ILLEGAL_CHARS.substring(i, i+1);
            assertFalse("Not a valid property name: "+s, PropertyUtils.isUsablePropertyName(s));
        }
        for (int i=127; i<256; i++) {
            String s = ""+(char)i;
            assertFalse("Not a valid property name: "+s+" - "+i, PropertyUtils.isUsablePropertyName(s));
        }
        assertFalse("Not a valid property name", PropertyUtils.isUsablePropertyName(ILLEGAL_CHARS));
        for (int i=32; i<127; i++) {
            String s = ""+(char)i;
            if (ILLEGAL_CHARS.indexOf((char)i) == -1) {
                assertTrue("Valid property name: "+s, PropertyUtils.isUsablePropertyName(s));
            }
        }
    }
    
    public void testGetUsablePropertyName() throws Exception {
        StringBuffer bad = new StringBuffer();
        StringBuffer good = new StringBuffer();
        for (int i=0; i<ILLEGAL_CHARS.length(); i++) {
            bad.append(ILLEGAL_CHARS.substring(i, i+1));
            bad.append("x");
            good.append("_");
            good.append("x");
        }
        assertEquals("Corrected property name does match", good.toString(), PropertyUtils.getUsablePropertyName(bad));
    }
    
    private static final class TestMutablePropertyProvider implements PropertyProvider {
        
        public final Map/*<String,String>*/ defs;
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        
        public TestMutablePropertyProvider(Map/*<String,String>*/ defs) {
            this.defs = defs;
        }
        
        public void mutated() {
            ChangeEvent ev = new ChangeEvent(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((ChangeListener)it.next()).stateChanged(ev);
            }
        }
        
        public Map getProperties() {
            return defs;
        }
        
        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
    }
    
    private static final class TestPCL implements PropertyChangeListener {
        
        public final Set/*<String>*/ changed = new HashSet();
        public final Map/*<String,String*/ newvals = new HashMap();
        public final Map/*<String,String*/ oldvals = new HashMap();
        
        public TestPCL() {}
        
        public void reset() {
            changed.clear();
            newvals.clear();
            oldvals.clear();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            String nue = (String)evt.getNewValue();
            String old = (String)evt.getOldValue();
            changed.add(prop);
            if (prop != null) {
                newvals.put(prop, nue);
                oldvals.put(prop, old);
            } else {
                assertNull("null prop name -> null new value", nue);
                assertNull("null prop name -> null old value", old);
            }
        }
        
    }
    
    static final class TestCL implements ChangeListener {
        
        public boolean changed = false;
        
        public TestCL() {}
        
        public void stateChanged(ChangeEvent e) {
            changed = true;
        }
        
    }
    
}
