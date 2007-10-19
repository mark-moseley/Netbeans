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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.templates;

import freemarker.ext.beans.BeansWrapper;
import java.awt.Color;
import java.awt.Panel;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jaroslav Tulach
 */
public class ProcessorTest extends TestCase {
    FileObject root;
    
    public ProcessorTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        return new NbTestSuite(ProcessorTest.class);
        //return new ProcessorTest("testCanImportSubpkgOfParentPkg");
    }

    protected void setUp() throws Exception {
        root = Repository.getDefault().getDefaultFileSystem().getRoot();
        for (FileObject f : root.getChildren()) {
            f.delete();
        }
    }

    protected void tearDown() throws Exception {
    }

    public void testApply() throws Exception {
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${title}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("title", "Nazdar");
        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>Nazdar</h1></html>";
        assertEquals(exp, w.toString());
    }

    public void testCanHandleComplexData() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);
        
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${panel.foreground.red} ${panel.foreground.green} ${panel.foreground.blue}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("panel", p);
        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>0 0 255</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testCanImportSubpkgOfParentPkg() throws Exception {
        FileObject imp = FileUtil.createData(root, "Templates/Licenses/gpl.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "GPL";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"*/Licenses/gpl.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>GPL</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testCanImportRelative() throws Exception {
        FileObject imp = FileUtil.createData(root, "Templates/Licenses/gpl.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "GPL";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"../Licenses/gpl.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>GPL</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testImportHonorsFEQ() throws Exception {
        doImportHonorsFEQ("cp1250");
    }
    public void testImportHonorsFEQIso() throws Exception {
        doImportHonorsFEQ("iso-8859-2");
    }
    
    private void doImportHonorsFEQ(String enc) throws Exception {
        MockServices.setServices(FEQI.class);
        
        //String kun = "Žluťoučký kůň";
        String kun = "\u017Dlu\u0165ou\u010Dky k\u016F\u0148";
        
        FileObject imp = FileUtil.createData(root, "Templates/Licenses/gpl.txt");
        {
            OutputStream os = imp.getOutputStream();
            os.write(kun.getBytes(enc));
            os.close();
            imp.setAttribute("enc", enc);
        }
        
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"../Licenses/gpl.txt\"></h1></html>";
            os.write(txt.getBytes("utf-8"));
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>" + kun + "</h1></html>";
        assertEquals(exp, w.toString());
    }

    public void testMissingVariablesAreJustLogged() throws Exception {
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1>${title}</h1></html>";
            os.write(txt.getBytes());
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        if (!w.toString().matches("<html><h1>.*</h1></html>")) {
            fail("should survive the missing variable:\n" + w.toString());
        }
        if (w.toString().indexOf("title") == -1) {
            fail("There should be a note about title variable:\n" + w);
        }
    }

    public void testMissingImportsAreJustLogged() throws Exception {
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"*/Licenses/gpl.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        if (!w.toString().matches("<html><h1>.*</h1></html>")) {
            fail("should survive the missing variable:\n" + w.toString());
        }
        if (w.toString().indexOf("gpl.txt") == -1) {
            fail("There should be a note about gpl include:\n" + w);
        }
    }
    
    public void testCanHandleImport() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${panel.foreground.blue}";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"import.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>255</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testImportCanInheritVariable() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<#assign prefix = \"#\">" +
                         "<#include \"import.txt\">";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    public void testImportCanInheritVariableInSubFolder() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "sub/import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "sub/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<#assign prefix=\"#\">" +
                         "<#include \"import.txt\">";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    public void testAbilityToSendOwnTemplate() throws Exception {
        Map<String,Object> myValues = new HashMap<String, Object>();
        myValues.put("prefix", "#");

        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("prefix", " * ");
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w, myValues);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    public void testShowItIsPossibleToPassInBeansWrappedObject() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "<#if (classInfo.getMethods().size() > 0) >The size is greater than 0.</#if>";
        os.write(txt.getBytes());
        os.close();       
        
        
        StringWriter w = new StringWriter();
                        
        Map<String,Object> parameters = Collections.<String,Object>singletonMap(
            "classInfo", BeansWrapper.getDefaultInstance().wrap(new ClassInfo())
        );
        apply(fo, w, parameters);
        assertEquals("The size is greater than 0.", w.toString());
    }
    public void testShowHowToGetSizeOfASequenceWithoutWrapper() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "<#if (classInfo.getMethods()?size > 0) >The size is greater than 0.</#if>";
        os.write(txt.getBytes());
        os.close();       
        
        
        StringWriter w = new StringWriter();
                        
        Map<String,Object> parameters = Collections.<String,Object>singletonMap(
            "classInfo", new ClassInfo()
        );
        apply(fo, w, parameters);
        assertEquals("The size is greater than 0.", w.toString());
    }
    public void testMissingClassInfoSimple() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
       // String txt = "<#if (classInfo.getMethods().size() > 0) >The size is greater than 0.</#if>";
        String txt = "<#if (classInfo.getMethodsCount() > 0) >The size is greater than 0.</#if>";
        os.write(txt.getBytes());
        os.close();       
        
        
        StringWriter w = new StringWriter();
                        
        Map<String,ClassInfo> parameters = Collections.singletonMap("classInfo", new ClassInfo());
        apply(fo, w, parameters);
        assertEquals("The size is greater than 0.", w.toString());
    }

    public void testChangeOfTemplate() throws Exception {
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${title}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("title", "Nazdar");
        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>Nazdar</h1></html>";
        assertEquals(exp, w.toString());
        
        os = template.getOutputStream();
        txt = "<html><h2>${title}</h2></html>";
        os.write(txt.getBytes());
        os.close();
      
        w = new StringWriter();
        apply(template, w);
        
        exp = "<html><h2>Nazdar</h2></html>";
        assertEquals("Second run", exp, w.toString());
    }

    public void testChangeOfParams() throws Exception {
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<${html}><h1>${title}</h1></${html}>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("title", "Nazdar");
        
        StringWriter w = new StringWriter();
        
        Map<String,Object> param = new HashMap<String, Object>();
        param.put("html", "html");
        apply(template, w, param);
        
        String exp = "<html><h1>Nazdar</h1></html>";
        assertEquals(exp, w.toString());
        
        param.put("html", "xml");
        w = new StringWriter();
        apply(template, w, param);
        
        exp = "<xml><h1>Nazdar</h1></xml>";
        assertEquals("Second run", exp, w.toString());
    }
    
    
    static void apply(FileObject template, Writer w) throws Exception {
        apply(template, w, Collections.<String,Object>emptyMap());
    }
    
    static void apply(FileObject template, Writer w, Map<String,? extends Object> values) throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine eng = mgr.getEngineByName("freemarker");
        assertNotNull("We do have such engine", eng);
        eng.getContext().setWriter(w);
        eng.getContext().setAttribute(FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE);
        eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE).putAll(values);
        eng.eval(new InputStreamReader(template.getInputStream()));
    }

    public static final class FEQI extends FileEncodingQueryImplementation {
        @Override
        public Charset getEncoding(FileObject file) {
            Object obj = file.getAttribute("enc");
            if (obj instanceof String) {
                return Charset.forName((String)obj);
            }
            return null;
        }
        
    }
}
