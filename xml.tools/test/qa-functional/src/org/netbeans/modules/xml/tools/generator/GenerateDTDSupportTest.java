/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.lang.reflect.Method;
import junit.textui.TestRunner;
import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.tax.TreeDocument;
import org.netbeans.tax.TreeElement;
import org.netbeans.tests.xml.XTest;
import org.openide.filesystems.FileObject;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: GenerateDTDSupportTest
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 * GenerateDTDSupportTest checks Generate DTD action on XML document without DTD. The action is
 * accesible from popup menu on all element nodes.<BR>
 *
 * <BR><B>How it works:</B><BR>
 * Test opens XML document, generates DTD for document root element and writes the DTD into log.<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * None
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * DTD for the XML document.<BR>
 * <BR><B>Possible reasons of failure:</B>
 * <UL>
 * <LI type="circle">
 * <I>None<BR></I>
 * </LI>
 * </UL>
 * <BR><B>To Do:</B>
 * <UL>
 * <LI type="circle">
 * Test Generate DTD action on XML document with DTD (regenerate DTD).<BR>
 * </LI>
 * <LI type="circle">
 * Test Generate DTD action on different elements (no only on root element). <BR>
 * </LI>
 * </UL>
 * <P>
 */

public class GenerateDTDSupportTest extends XTest {
    
    /** Creates new GenerateDTDSupportTest */
    public GenerateDTDSupportTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        XMLDataObject dao = (XMLDataObject) TestUtil.THIS.findData("Node00.xml");
        if (dao == null) {
            fail("\"data/Node00.xml\" data object is not found!");
        }
        TreeElement element = ((TreeDocument) dao.getTreeDocument()).getDocumentElement();
        FileObject primFile = dao.getPrimaryFile();
        String name = primFile.getName() + "_" + element.getQName();
        FileObject folder = primFile.getParent();
        String encoding = null;
        try {
            encoding = element.getOwnerDocument().getEncoding();
        } catch (NullPointerException e) { /* NOTHING */ }
        
        GenerateDTDSupport gen = new GenerateDTDSupport(dao);
        // Original: String result = gen.xml2dtd (element, name, encoding);
        Method m = gen.getClass().getDeclaredMethod("xml2dtd", new Class[] {TreeElement.class, String.class, String.class});
        m.setAccessible(true);
        String result = (String) m.invoke(gen, new Object[] {element, name, encoding});
        
        // first comment contains variable informations - remove it
        result = TestUtil.replaceString(result, "<!--", "-->", "<!-- REMOVED -->");
        ref(result);
        compareReferenceFiles();
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(GenerateDTDSupportTest.class);
    }
}
