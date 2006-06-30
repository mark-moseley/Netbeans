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

package org.netbeans.modules.junit;

import org.openide.*;
import org.openide.src.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.netbeans.modules.java.*;

import junit.framework.*;
import org.netbeans.junit.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class TestCreatorTest extends NbTestCase implements DataLoader.RecognizedFiles {

    public TestCreatorTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestCreatorTest.class);
    }
    
    /** Test of createTestClass method, of class org.netbeans.modules.junit.TestCreator. */
    public void testCreateTestClass() throws Exception {//IOException, ClassNotFoundException, SourceException {
        System.out.println("testCreateTestClass");

        JUnitSettings   js = JUnitSettings.getDefault();

        setGenerateFlags(js, true);
        TestCreator.initialize();
        
        JavaDataLoader jdl = new JavaDataLoader();
        LocalFileSystem fsData = new LocalFileSystem();
        fsData.setRootDirectory(new File(appendSlash(m_pathData) + "CreateTestClass"));
        
        FileObject foSrc = fsData.findResource("src");
        FileObject foTrg = fsData.findResource("trg");
        FileObject foPass = fsData.findResource("pass");

        DataObject doTempl = jdl.findDataObject (fsData.findResource(CLASS_TEMPLATE), this);
        DataFolder doTrg = (DataFolder) DataFolder.find(foTrg);
   
        FileObject foList[] = foSrc.getChildren();
        for (int i = 0; i< foList.length; i++) {
            FileObject foEntry = foList[i];
            if (foEntry.getExt().equals("java")) {
                DataObject  doSrcEntry = null;
                DataObject  doTrgEntry = null;
                String      name = foEntry.getName() + "Test";
                File        fTmp = new File(appendSlash(fsData.getRootDirectory().getPath()) + appendSlash(foTrg.getNameExt()) + name + ".java");
                
                fTmp.delete();
                doSrcEntry = jdl.findDataObject (foEntry, this);
                doTrgEntry = doTempl.createFromTemplate(doTrg, name);
                
                ClassElement ceSrc = getClassElementFromDO(doSrcEntry);
                ClassElement ceTrg = getClassElementFromDO(doTrgEntry);
                TestCreator.createTestClass(ceSrc, ceTrg);
                SaveCookie sc = (SaveCookie) doTrgEntry.getCookie(SaveCookie.class);
                sc.save();

                assertFile(getFile(doTrgEntry.getPrimaryFile()), new File(getFile(foPass), name + ".java"), new File(System.getProperty("xresults")));
            }
        }
        
        DataObject      doSrcEntry = DataObject.find(fsData.findResource("src/TestClass001.java"));
        DataObject      doTrgEntry;
        ClassElement    ceSrc = getClassElementFromDO(doSrcEntry);
        ClassElement    ceTrg;

        setGenerateFlags(js, false);
        
        new File(appendSlash(fsData.getRootDirectory().getPath()) + "trg/TestPublic.java").delete();
        js.setMembersPublic(true); assertTrue(true == js.isMembersPublic());
        TestCreator.initialize();
        doTrgEntry = doTempl.createFromTemplate(doTrg, "TestPublic");
        ceTrg = getClassElementFromDO(doTrgEntry);
        TestCreator.createTestClass(ceSrc, ceTrg);
        ((SaveCookie) doTrgEntry.getCookie(SaveCookie.class)).save();
        js.setMembersPublic(false); assertTrue(false == js.isMembersPublic());
        assertFile(getFile(doTrgEntry.getPrimaryFile()), new File(getFile(foPass), "TestPublic.java"), new File(System.getProperty("xresults")));

        new File(appendSlash(fsData.getRootDirectory().getPath()) + "trg/TestProtected.java").delete();
        js.setMembersProtected(true); assertTrue(true == js.isMembersProtected());
        TestCreator.initialize();
        doTrgEntry = doTempl.createFromTemplate(doTrg, "TestProtected");
        ceTrg = getClassElementFromDO(doTrgEntry);
        TestCreator.createTestClass(ceSrc, ceTrg);
        ((SaveCookie) doTrgEntry.getCookie(SaveCookie.class)).save();
        js.setMembersProtected(false); assertTrue(false == js.isMembersProtected());
        assertFile(getFile(doTrgEntry.getPrimaryFile()), new File(getFile(foPass), "TestProtected.java"), new File(System.getProperty("xresults")));
        
        new File(appendSlash(fsData.getRootDirectory().getPath()) + "trg/TestPackage.java").delete();
        js.setMembersPackage(true); assertTrue(true == js.isMembersPackage());
        TestCreator.initialize();
        doTrgEntry = doTempl.createFromTemplate(doTrg, "TestPackage");
        ceTrg = getClassElementFromDO(doTrgEntry);
        TestCreator.createTestClass(ceSrc, ceTrg);
        ((SaveCookie) doTrgEntry.getCookie(SaveCookie.class)).save();
        js.setMembersPackage(false); assertTrue(false == js.isMembersPackage());
        assertFile(getFile(doTrgEntry.getPrimaryFile()), new File(getFile(foPass), "TestPackage.java"), new File(System.getProperty("xresults")));
        
        new File(appendSlash(fsData.getRootDirectory().getPath()) + "trg/TestBodyComments.java").delete();
        js.setMembersPublic(true); assertTrue(true == js.isMembersPublic());
        TestCreator.initialize();
        js.setBodyComments(true); assertTrue(true == js.isBodyComments());
        doTrgEntry = doTempl.createFromTemplate(doTrg, "TestBodyComments");
        ceTrg = getClassElementFromDO(doTrgEntry);
        TestCreator.createTestClass(ceSrc, ceTrg);
        ((SaveCookie) doTrgEntry.getCookie(SaveCookie.class)).save();
        js.setBodyComments(false); assertTrue(false == js.isBodyComments());
        assertFile(getFile(doTrgEntry.getPrimaryFile()), new File(getFile(foPass), "TestBodyComments.java"), new File(System.getProperty("xresults")));

        new File(appendSlash(fsData.getRootDirectory().getPath()) + "trg/TestBodyContent.java").delete();
        js.setBodyContent(true); assertTrue(true == js.isBodyContent());
        doTrgEntry = doTempl.createFromTemplate(doTrg, "TestBodyContent");
        ceTrg = getClassElementFromDO(doTrgEntry);
        TestCreator.createTestClass(ceSrc, ceTrg);
        ((SaveCookie) doTrgEntry.getCookie(SaveCookie.class)).save();
        js.setBodyContent(false); assertTrue(false == js.isBodyContent());
        assertFile(getFile(doTrgEntry.getPrimaryFile()), new File(getFile(foPass), "TestBodyContent.java"), new File(System.getProperty("xresults")));

        new File(appendSlash(fsData.getRootDirectory().getPath()) + "trg/TestJavaDoc.java").delete();
        js.setJavaDoc(true); assertTrue(true == js.isJavaDoc());
        doTrgEntry = doTempl.createFromTemplate(doTrg, "TestJavaDoc");
        ceTrg = getClassElementFromDO(doTrgEntry);
        TestCreator.createTestClass(ceSrc, ceTrg);
        ((SaveCookie) doTrgEntry.getCookie(SaveCookie.class)).save();
        js.setJavaDoc(false); assertTrue(false == js.isJavaDoc());
        assertFile(getFile(doTrgEntry.getPrimaryFile()), new File(getFile(foPass), "TestJavaDoc.java"), new File(System.getProperty("xresults")));
    }
    
    /** Test of createTestSuit method, of class org.netbeans.modules.junit.TestCreator. */
    public void testCreateTestSuit() throws Exception {
        System.out.println("testCreateTestSuit");

        JavaDataLoader jdl = new JavaDataLoader();
        LocalFileSystem fsData = new LocalFileSystem();
        fsData.setRootDirectory(new File(appendSlash(m_pathData) + "CreateTestSuite"));
        
        FileObject foPass = fsData.findResource(TESTSUITE + ".java.pass");
        DataObject doTempl = jdl.findDataObject (fsData.findResource(CLASS_TEMPLATE), this);

        File fTmp = new File(appendSlash(fsData.getRootDirectory().getPath()) + TESTSUITE + ".java");

        fTmp.delete();
        DataObject doTrgEntry = doTempl.createFromTemplate((DataFolder)DataFolder.find(fsData.getRoot()), TESTSUITE);
        ClassElement ceTrg = getClassElementFromDO(doTrgEntry);
        
        LinkedList lst = new LinkedList();
        lst.add("a.b.c.TestClass1");
        lst.add("a.b.c.TestClass2");
        lst.add("TestClass3");
        
        TestCreator.createTestSuit(lst, "some.test.pckg", ceTrg);
        SaveCookie sc = (SaveCookie) doTrgEntry.getCookie(SaveCookie.class);
        sc.save();
        
        assertFile(getFile(doTrgEntry.getPrimaryFile()), getFile(foPass), new File(System.getProperty("xresults")));
    }
    
    /** Test of initialize method, of class org.netbeans.modules.junit.TestCreator. */
    public void testInitialize() {
        System.out.println("testInitialize");
        
        // functionality of initialize method is verified in createTestClass method test
    }
    
    /** Test of isClassTestable method, of class org.netbeans.modules.junit.TestCreator. */
    public void testIsClassTestable() throws Exception {
        System.out.println("testIsClassTestable");

        FileObject          foClass;
        DataObject          doClass;
        ClassElement        clazz;
        JUnitSettings       js = JUnitSettings.getDefault();
        JavaDataLoader      jdl = new JavaDataLoader();
        LocalFileSystem     fsData = new LocalFileSystem();
        fsData.setRootDirectory(new File(appendSlash(m_pathData) + "IsClassTestable"));
        
        foClass = fsData.findResource("SimpleClass.java");
        doClass = jdl.findDataObject (foClass, this);
        clazz = getClassElementFromDO(doClass);
        assertTrue(true == TestCreator.isClassTestable(clazz));

        foClass = fsData.findResource("NonPublicClass.java");
        doClass = jdl.findDataObject (foClass, this);
        clazz = getClassElementFromDO(doClass);
        assertTrue(false == TestCreator.isClassTestable(clazz));
        
        foClass = fsData.findResource("AbstractClass.java");
        doClass = jdl.findDataObject (foClass, this);
        clazz = getClassElementFromDO(doClass);
        assertTrue(true == TestCreator.isClassTestable(clazz));
        
        foClass = fsData.findResource("SimpleInterface.java");
        doClass = jdl.findDataObject (foClass, this);
        clazz = getClassElementFromDO(doClass);
        assertTrue(false == TestCreator.isClassTestable(clazz));

        js.setGenerateExceptionClasses(false);
        assertTrue(false == js.isGenerateExceptionClasses());
        foClass = fsData.findResource("ExceptionClass.java");
        doClass = jdl.findDataObject (foClass, this);
        clazz = getClassElementFromDO(doClass);
        assertTrue(false == TestCreator.isClassTestable(clazz));
        
        js.setGenerateExceptionClasses(true);
        assertTrue(true == js.isGenerateExceptionClasses());
        assertTrue(true == TestCreator.isClassTestable(clazz));
    }

    // protected members
    protected static final String   PROP_CLASSPATH = "java.class.path";
    protected String    m_pathData = null;
    
    protected void setUp() {
        if (null == m_pathData)
            m_pathData = System.getProperty("xdata");
    }
    
    protected void tearDown() {
    }
    
    protected class FileExtFilter implements FileFilter {
        protected String extension = null;
        public FileExtFilter(String extension) {
            this.extension = extension;
        }
        public boolean accept(File pathname) {
            String  ext = null;
            int     i;
            
            if (-1 != (i = pathname.getPath().lastIndexOf('.')))
                ext = pathname.getPath().substring(i + 1);
            
            if (null == ext)
                return (null == extension);
            
            return extension.equals(ext);
        }
    }
    
    // private members
    private static final String CLASS_TEMPLATE = "ClassTemplate.java";
    private static final String TESTSUITE = "TestSuite";
    
    private String appendSlash(String path) {
        if (null == path)
            return new String();
        
        if (!path.endsWith("\\") && !path.endsWith("/"))
            return path + "\\";
        
        return path;
    }
    
   private ClassElement getClassElementFromDO(DataObject dO) {
        SourceCookie    sc;
        SourceElement   se;

        sc = (SourceCookie) dO.getCookie(SourceCookie.class);
        se = sc.getSource();
        return se.getClass(Identifier.create(dO.getPrimaryFile().getName()));
    }
    
    private File getFile(FileObject fo) throws FileStateInvalidException {
        StringBuffer name = new StringBuffer();
        LocalFileSystem lfs = (LocalFileSystem)fo.getFileSystem();
        
        name.append(lfs.getRootDirectory().getPath());
        if (0 != name.length())
            name.append("/");
        name.append(fo.getPackageNameExt('/', '.'));
        
        return new File(name.toString());
    }
    
    private void setGenerateFlags(JUnitSettings js, boolean flag) {
        js.setMembersPublic(flag); assertTrue(flag == js.isMembersPublic());
        js.setMembersProtected(flag); assertTrue(flag == js.isMembersProtected());
        js.setMembersPackage(flag); assertTrue(flag == js.isMembersPackage());
        js.setBodyComments(flag); assertTrue(flag == js.isBodyComments());
        js.setBodyContent(flag); assertTrue(flag == js.isBodyContent());
        js.setJavaDoc(flag); assertTrue(flag == js.isJavaDoc());
    }
    
    public void markRecognized(FileObject fo) {
    }
    
}
