/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

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
        // XXX check also evaluateAll
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
        File d1s = new File(d1, "s");
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
        assertEquals("d1s from d1", "s", PropertyUtils.relativizeFile(d1, d1s));
        assertEquals("d1sf from d1", "s/f", PropertyUtils.relativizeFile(d1, d1sf));
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
        assertEquals("existing d1s from d1", "s", PropertyUtils.relativizeFile(d1, d1s));
        assertEquals("existing d1sf from d1", "s/f", PropertyUtils.relativizeFile(d1, d1sf));
        assertEquals("existing d11 from d1", "../d11", PropertyUtils.relativizeFile(d1, d11));
        // XXX also try to test nonrelativizable files... only works on Windows though!
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
    
}
