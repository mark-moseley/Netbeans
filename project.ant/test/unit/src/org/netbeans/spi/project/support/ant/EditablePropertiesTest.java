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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

public class EditablePropertiesTest extends NbTestCase {

    public EditablePropertiesTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLoad() throws Exception {
        HashMap content = new HashMap();
        for (int i=1; i<=26; i++) {
            content.put("key"+i, "value"+i);
        }
        content.put("@!#$%^keyA", "valueA!@#$%^&*(){}");
        content.put(" =:keyB", "valueB =:");
        content.put(""+(char)0x1234+"keyC", "valueC"+(char)0x9876);
        content.put("keyD", "");
        content.put("keyE", "");
        content.put("keyF", "");
        content.put("keyG", "");
        content.put("keyH", "value#this is not comment");
        content.put("keyI", "incorrect end: \\u123");
        // #46234: does not handle bad Unicode escapes well
        content.put("keyJ", "malformed Unicode escape: \\uabyz");
        
        EditableProperties ep = loadTestProperties();
        
        Iterator it = content.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            String value = (String)content.get(key);
            String epValue = ep.getProperty(key);
            assertEquals("Expected value for key "+key+" is different", value, epValue);
        }
        int count = 0;
        it = ep.entrySet().iterator();
        while (it.hasNext()) {
            if (((Map.Entry)it.next()).getKey() != null) {
                count++;
            }
        }
        assertEquals("Number of items in property file", content.keySet().size(), count);
    }
    
    /* Doesn't work; java.util.Properties throws IAE for malformed Unicode escapes:
    public void testJavaUtilPropertiesEquivalence() throws Exception {
        Properties p = loadTestJavaUtilProperties();
        EditableProperties ep = loadTestProperties();
        Iterator it = p.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            assertEquals("right value for " + key, val, ep.getProperty(key));
        }
        assertEquals("right number of items", p.size(), ep.size());
    }
     */

    public void testSave() throws Exception {
        clearWorkDir();
        EditableProperties ep = loadTestProperties();
        String dest = getWorkDirPath()+File.separatorChar+"new.properties";
        saveProperties(ep, dest);
        assertFile("Saved properties must be the same as original one", filenameOfTestProperties(), dest, (String)null);
    }
    
    public void testClonability() throws Exception {
        clearWorkDir();
        EditableProperties ep = loadTestProperties();
        
        EditableProperties ep2 = new EditableProperties(ep);
        String dest = getWorkDirPath()+File.separatorChar+"new2.properties";
        saveProperties(ep2, dest);
        assertFile("Saved cloned properties must be the same as original one", filenameOfTestProperties(), dest, (String)null);
        
        EditableProperties ep3 = (EditableProperties)ep.clone();
        dest = getWorkDirPath()+File.separatorChar+"new3.properties";
        saveProperties(ep3, dest);
        assertFile("Saved cloned properties must be the same as original one", filenameOfTestProperties(), dest, (String)null);
    }

    // test that modifications changes only necessary parts
    public void testVersionability() throws Exception {
        clearWorkDir();
        
        EditableProperties ep = loadTestProperties();
        
        EditableProperties ep2 = new EditableProperties(ep);
        ep2.setProperty("key24", "new value of key 24");
        String dest = getWorkDirPath()+File.separatorChar+"mod1.properties";
        saveProperties(ep2, dest);
        int res[] = compare(filenameOfTestProperties(), dest);
        assertEquals("One line modified", 1, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = new EditableProperties(ep);
        ep2.setProperty("key23", "new value of key23");
        dest = getWorkDirPath()+File.separatorChar+"mod2.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("Four lines modified", 4, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = new EditableProperties(ep);
        ep2.put("newkey", "new value");
        dest = getWorkDirPath()+File.separatorChar+"mod3.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("No lines modified", 0, res[0]);
        assertEquals("One line added", 1, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = new EditableProperties(ep);
        ep2.remove("key14");
        dest = getWorkDirPath()+File.separatorChar+"mod4.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("No lines modified", 0, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("Two lines removed", 2, res[2]);
        
        ep2 = new EditableProperties(ep);
        ep2.setProperty("key21", new String[]{"first line;", "second line;", "third line"});
        dest = getWorkDirPath()+File.separatorChar+"mod5.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("Four lines modified", 4, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        ep2.setProperty("key21", "first line;second line;third line");
        String dest2 = getWorkDirPath()+File.separatorChar+"mod6.properties";
        saveProperties(ep2, dest2);
        res = compare(dest, dest2);
        assertEquals("Four lines modified", 4, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
    }

    // test that array values are stored correctly
    public void testArrayValues() throws Exception {
        EditableProperties ep = new EditableProperties(false);
        ep.setProperty("key1", new String[]{"1. line;", "2. line;", "3. line"});
        ep.setProperty("key2", "1. line;2. line;3. line");
        String output = getAsString(ep);
        String expected = 
            "key1=\\"+System.getProperty("line.separator")+
            "    1. line;\\"+System.getProperty("line.separator")+
            "    2. line;\\"+System.getProperty("line.separator")+
            "    3. line"+System.getProperty("line.separator")+
            "key2=1. line;2. line;3. line"+System.getProperty("line.separator");
        assertEquals(expected, output);
        assertEquals(ep.getProperty("key1"), "1. line;2. line;3. line");
        assertEquals(ep.getProperty("key2"), "1. line;2. line;3. line");
        ep.setProperty("key1", "one; two; three");
        output = getAsString(ep);
        expected = 
            "key1=one; two; three"+System.getProperty("line.separator")+
            "key2=1. line;2. line;3. line"+System.getProperty("line.separator");
        assertEquals(expected, output);
        assertEquals(ep.getProperty("key1"), "one; two; three");
        assertEquals(ep.getProperty("key2"), "1. line;2. line;3. line");
        ep.setProperty("key2", new String[]{"1. line;", "2. line;", "3. line", "one;", "more;", "line;"});
        ep.setProperty("key", new String[0]);
        output = getAsString(ep);
        expected = 
            "key1=one; two; three"+System.getProperty("line.separator")+
            "key2=\\"+System.getProperty("line.separator")+
            "    1. line;\\"+System.getProperty("line.separator")+
            "    2. line;\\"+System.getProperty("line.separator")+
            "    3. line\\"+System.getProperty("line.separator")+
            "    one;\\"+System.getProperty("line.separator")+
            "    more;\\"+System.getProperty("line.separator")+
            "    line;"+System.getProperty("line.separator")+
            "key=\\"+System.getProperty("line.separator")+
            ""+System.getProperty("line.separator");
        assertEquals(expected, output);
        assertEquals(ep.getProperty("key1"), "one; two; three");
        assertEquals(ep.getProperty("key2"), "1. line;2. line;3. lineone;more;line;");
        assertEquals(ep.getProperty("key"), "");
    }
        
    public void testSorting() throws Exception {
        EditableProperties ep = new EditableProperties(false);
        ep.setProperty("a", "val-a");
        ep.setProperty("c", "val-c");
        ep.put("b", "val-b");
        String output = getAsString(ep);
        String expected = "a=val-a"+System.getProperty("line.separator")+"c=val-c"+
                System.getProperty("line.separator")+"b=val-b"+
                System.getProperty("line.separator");
        assertEquals(expected, output);
        
        ep = new EditableProperties(true);
        ep.setProperty("a", "val-a");
        ep.setProperty("c", "val-c");
        ep.put("b", "val-b");
        output = getAsString(ep);
        expected = "a=val-a"+System.getProperty("line.separator")+"b=val-b"+
                System.getProperty("line.separator")+"c=val-c"+
                System.getProperty("line.separator");
        assertEquals(expected, output);
    }

    // test that changing comments work and modify only comments
    public void testComment() throws Exception {
        clearWorkDir();
        
        EditableProperties ep = loadTestProperties();
        
        EditableProperties ep2 = new EditableProperties(ep);
        ep2.setComment("key10", new String[]{"# this is new comment for property key 10"}, false);
        String dest = getWorkDirPath()+File.separatorChar+"comment1.properties";
        saveProperties(ep2, dest);
        int res[] = compare(filenameOfTestProperties(), dest);
        assertEquals("No lines modified", 0, res[0]);
        assertEquals("One line added", 1, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = new EditableProperties(ep);
        ep2.setComment("key1", new String[]{"# new comment", "# new comment second line"}, true);
        dest = getWorkDirPath()+File.separatorChar+"comment2.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("No lines modified", 0, res[0]);
        assertEquals("Two lines added", 2, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = new EditableProperties(ep);
        ep2.setComment("key26", new String[]{"# changed comment"}, false);
        dest = getWorkDirPath()+File.separatorChar+"comment3.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("One line modified", 1, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = new EditableProperties(ep);
        ep2.setComment("key25", new String[]{"# one line comment"}, false);
        dest = getWorkDirPath()+File.separatorChar+"comment4.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("Two lines modified", 2, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = new EditableProperties(ep);
        ep2.setComment("key26", ep2.getComment("key26"), true);
        dest = getWorkDirPath()+File.separatorChar+"comment5.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("No line modified", 0, res[0]);
        assertEquals("One line added", 1, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
    }

    // test that misc chars are correctly escaped, unicode encoded, etc.
    public void testEscaping() throws Exception {
        String umlaut = "" + (char)252;
        EditableProperties ep = new EditableProperties(false);
        ep.setProperty("a a", "a space a");
        ep.setProperty("b"+(char)0x4567, "val"+(char)0x1234);
        ep.setProperty("@!#$%^\\", "!@#$%^&*(){}\\");
        ep.setProperty("d\nd", "d\nnewline\nd");
        ep.setProperty("umlaut", umlaut);
        ep.setProperty("_a a", new String[]{"a space a"});
        ep.setProperty("_b"+(char)0x4567, new String[]{"val"+(char)0x1234});
        ep.setProperty("_@!#$%^\\", new String[]{"!@#$%^&*\\", "(){}\\"});
        ep.setProperty("_d\nd", new String[]{"d\nnew","line\nd", "\n", "end"});
        ep.setProperty("_umlaut", new String[]{umlaut, umlaut});
        String output = getAsString(ep);
        String expected = "a\\ a=a space a"+System.getProperty("line.separator")+
                "b\\u4567=val\\u1234"+System.getProperty("line.separator")+
                "@!#$%^\\\\=!@#$%^&*(){}\\\\"+System.getProperty("line.separator")+
                "d\\nd=d\\nnewline\\nd"+System.getProperty("line.separator")+
                "umlaut=\\u00fc"+System.getProperty("line.separator")+
                "_a\\ a=\\"+System.getProperty("line.separator")+"    a space a"+System.getProperty("line.separator")+
                "_b\\u4567=\\"+System.getProperty("line.separator")+"    val\\u1234"+System.getProperty("line.separator")+
                "_@!#$%^\\\\=\\"+System.getProperty("line.separator")+"    !@#$%^&*\\\\\\"+System.getProperty("line.separator")+
                    "    (){}\\\\"+System.getProperty("line.separator")+
                "_d\\nd=\\"+System.getProperty("line.separator")+"    d\\nnew\\"+System.getProperty("line.separator")+
                    "    line\\nd\\"+System.getProperty("line.separator")+
                    "    \\n\\"+System.getProperty("line.separator")+
                    "    end"+System.getProperty("line.separator")+
                "_umlaut=\\" +System.getProperty("line.separator")+"    \\u00fc\\"+System.getProperty("line.separator")+
                    "    \\u00fc"+System.getProperty("line.separator");
        assertEquals(expected, output);
        assertEquals("a space a", ep.getProperty("a a"));
        assertEquals("val"+(char)0x1234, ep.getProperty("b"+(char)0x4567));
        assertEquals("!@#$%^&*(){}\\", ep.getProperty("@!#$%^\\"));
        assertEquals("d\nnewline\nd", ep.getProperty("d\nd"));
        assertEquals(umlaut, ep.getProperty("umlaut"));
        assertEquals("a space a", ep.getProperty("_a a"));
        assertEquals("val"+(char)0x1234, ep.getProperty("_b"+(char)0x4567));
        assertEquals("!@#$%^&*\\(){}\\", ep.getProperty("_@!#$%^\\"));
        assertEquals("d\nnewline\nd\nend", ep.getProperty("_d\nd"));
        assertEquals(umlaut+umlaut, ep.getProperty("_umlaut"));
    }
    
    // test that iterator implementation is OK
    public void testIterator() throws Exception {
        EditableProperties ep = loadTestProperties();
        Iterator it = ep.entrySet().iterator();
        while (it.hasNext()) {
            it.next();
        }
        it = ep.keySet().iterator();
        while (it.hasNext()) {
            it.next();
        }
        it = ep.keySet().iterator();
        while (it.hasNext()) {
            ep.remove(it.next());
        }
        ep = loadTestProperties();
        it = ep.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            assertNotNull("Property key cannot be null", entry.getKey());
            assertNotNull("Property value cannot be null", entry.getValue());
            entry.setValue(entry.getValue()+"-something-new");
        }
        it = ep.entrySet().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }
    
    // test that syntax errors are survived
    public void testInvalidPropertiesFile() throws Exception {
        String invalidProperty = "key=value without correct end\\";
        ByteArrayInputStream is = new ByteArrayInputStream(invalidProperty.getBytes());
        EditableProperties ep = new EditableProperties();
        ep.load(is);
        assertEquals("Syntax error should be resolved", 1, ep.keySet().size());
        assertEquals("value without correct end", ep.getProperty("key"));
    }

    
    // helper methods:
    
    
    private String filenameOfTestProperties() throws Exception {
        return EditablePropertiesTest.class.getResource("data/test.properties").getPath();
    }
    
    private EditableProperties loadTestProperties() throws IOException {
        URL u = EditablePropertiesTest.class.getResource("data/test.properties");
        EditableProperties ep = new EditableProperties();
        InputStream is = u.openStream();
        try {
            ep.load(is);
        } finally {
            is.close();
        }
        return ep;
    }
    
    /*
    private Properties loadTestJavaUtilProperties() throws IOException {
        URL u = EditablePropertiesTest.class.getResource("data/test.properties");
        Properties p = new Properties();
        InputStream is = u.openStream();
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return p;
    }
     */
    
    private void saveProperties(EditableProperties ep, String path) throws Exception {
        OutputStream os = new FileOutputStream(path);
        try {
            ep.store(os);
        } finally {
            os.close();
        }
    }

    private int[] compare(String f1, String f2) throws Exception {
        Reader r1 = null;
        Reader r2 = null;
        try {
            r1 = new InputStreamReader(new FileInputStream(f1), "ISO-8859-1");
            r2 = new InputStreamReader(new FileInputStream(f2), "ISO-8859-1");
            return AntBasedTestUtil.countTextDiffs(r1, r2);
        } finally {
            if (r1 != null) {
                r1.close();
            }
            if (r2 != null) {
                r2.close();
            }
        }
    }
    
    private String getAsString(EditableProperties ep) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ep.store(os);
        os.close();
        return os.toString();
    }
    
}
