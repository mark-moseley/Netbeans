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

package org.openide.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

public class EditablePropertiesTest extends NbTestCase {

    public EditablePropertiesTest(String name) {
        super(name);
    }
    
    public void testLoad() throws Exception {
        Map<String,String> content = new HashMap<String,String>();
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
        
        for (Map.Entry<String,String> entry : content.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String epValue = ep.getProperty(key);
            assertEquals("Expected value for key "+key+" is different", value, epValue);
        }
        int count = 0;
        for (Map.Entry<String,String> entry : ep.entrySet()) {
            if (entry.getKey() != null) {
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
        
        EditableProperties ep2 = ep.cloneProperties();
        String dest = getWorkDirPath()+File.separatorChar+"new2.properties";
        saveProperties(ep2, dest);
        assertFile("Saved cloned properties must be the same as original one", filenameOfTestProperties(), dest, (String)null);
        
        EditableProperties ep3 = (EditableProperties)ep.clone();
        dest = getWorkDirPath()+File.separatorChar+"new3.properties";
        saveProperties(ep3, dest);
        assertFile("Saved cloned properties must be the same as original one", filenameOfTestProperties(), dest, (String)null);
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
            "key="+System.getProperty("line.separator"); // #45061
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
        
        ep = new EditableProperties(false);
        ep.setProperty("a", "val-a");
        ep.setProperty("c", "val-c");
        ep.put("b", "val-b");
        output = getAsString(ep);
        expected = "a=val-a"+System.getProperty("line.separator")+"c=val-c"+
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
        Iterator<Map.Entry<String,String>> it1 = ep.entrySet().iterator();
        while (it1.hasNext()) {
            it1.next();
        }
        Iterator<String> it2 = ep.keySet().iterator();
        while (it2.hasNext()) {
            it2.next();
        }
        it2 = ep.keySet().iterator();
        while (it2.hasNext()) {
            it2.next();
            it2.remove();
        }
        ep.put("a", "aval");
        ep.remove("a");
        ep = loadTestProperties();
        it1 = ep.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry<String,String> entry = it1.next();
            assertNotNull("Property key cannot be null", entry.getKey());
            assertNotNull("Property value cannot be null", entry.getValue());
            entry.setValue(entry.getValue()+"-something-new");
        }
        it1 = ep.entrySet().iterator();
        while (it1.hasNext()) {
            it1.next();
            it1.remove();
        }
    }
    
    // test that syntax errors are survived
    public void testInvalidPropertiesFile() throws Exception {
        String invalidProperty = "key=value without correct end\\";
        ByteArrayInputStream is = new ByteArrayInputStream(invalidProperty.getBytes());
        EditableProperties ep = new EditableProperties(false);
        ep.load(is);
        assertEquals("Syntax error should be resolved", 1, ep.keySet().size());
        assertEquals("value without correct end", ep.getProperty("key"));
    }
    
    public void testNonLatinComments() throws Exception {
        // #60249.
        String lsep = System.getProperty("line.separator");
        EditableProperties p = new EditableProperties(false);
        p.setProperty("k", "v");
        p.setComment("k", new String[] {"# \u0158ekni koment teda!"}, false);
        String expected = "# \\u0158ekni koment teda!" + lsep + "k=v" + lsep;
        assertEquals("Storing non-Latin chars in comments works", expected, getAsString(p));
        p = new EditableProperties(false);
        p.load(new ByteArrayInputStream(expected.getBytes("ISO-8859-1")));
        assertEquals("Reading non-Latin chars in comments works", Collections.singletonList("# \u0158ekni koment teda!"), Arrays.asList(p.getComment("k")));
        p.setProperty("k", "v2");
        expected = "# \\u0158ekni koment teda!" + lsep + "k=v2" + lsep;
        assertEquals("Reading and re-writing non-Latin chars in comments works", expected, getAsString(p));
    }

    
    // helper methods:
    
    
    private String filenameOfTestProperties() {
        // #50987: never use URL.path for this purpose...
        return new File(URI.create(EditablePropertiesTest.class.getResource("data/test.properties").toExternalForm())).getAbsolutePath();
    }
    
    private EditableProperties loadTestProperties() throws IOException {
        URL u = EditablePropertiesTest.class.getResource("data/test.properties");
        EditableProperties ep = new EditableProperties(false);
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

    private String getAsString(EditableProperties ep) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ep.store(os);
        os.close();
        return os.toString("ISO-8859-1");
    }
    
}
