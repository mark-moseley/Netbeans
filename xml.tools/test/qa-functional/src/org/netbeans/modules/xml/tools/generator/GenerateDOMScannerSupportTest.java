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
import org.netbeans.modules.xml.core.DTDDataObject;
import org.netbeans.tests.xml.XTest;

//import org.openide.*;
import org.openide.filesystems.FileObject;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: XMLGenerator2Test
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 * XMLGenerator2Test checks 'Generate DOM Tree Scanner' action on DTD document. The action is
 * accesible from popup menu an all DTD document nodes.<BR>
 *
 * <BR><B>How it works:</B><BR>
 * Test opens DTD document, generates Java source text for the document and writes it into output.<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * None
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * Scanner of DOM tree as Java source text.<BR>
 *
 * <BR><B>Possible reasons of failure:</B>
 * <UL>
 * <LI type="circle">
 * <I>None<BR></I>
 * </LI>
 * </UL>
 * <P>
 */

public class GenerateDOMScannerSupportTest extends XTest {
    
    /** Creates new CoreSettingsTest */
    public GenerateDOMScannerSupportTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        DTDDataObject dao = (DTDDataObject) TestUtil.THIS.findData("books.dtd");
        if (dao == null) {
            fail("\"data/books.dtd\" data object is not found!");
        }
        FileObject primFile = dao.getPrimaryFile();
        String rawName = primFile.getName();
        String name = rawName.substring(0,1).toUpperCase() + rawName.substring(1) + "Scanner";
        FileObject folder = primFile.getParent();
        String packageName = folder.getPackageName('.');
        GenerateDOMScannerSupport gen = new GenerateDOMScannerSupport(dao);
        // prepareDOMScanner() is private at GenerateDOMScannerSupport.class
        Method m = gen.getClass().getDeclaredMethod("prepareDOMScanner", new Class[] {String.class, String.class, FileObject.class});
        m.setAccessible(true);
        String result = (String) m.invoke(gen, new Object[] {name, packageName, primFile});
        // first comment contains variable informations - remove it
        result = TestUtil.replaceString(result, "/*", "*/", "/* REMOVED */");
        ref(result);
        compareReferenceFiles();
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(GenerateDOMScannerSupportTest.class);
    }
}
