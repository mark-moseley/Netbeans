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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Samaresh
 */
public class BasicCompletionTest extends AbstractTestCase {
    
    static final String PO_INSTANCE_DOCUMENT = "resources/PO.xml";
    static final String TEST_INSTANCE_DOCUMENT = "resources/Test.xml";
    static final String PROJECT_INSTANCE_DOCUMENT = "resources/project.xml";
    
    public BasicCompletionTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        //suite.addTest(new BasicCompletionTest("testAttributes1"));
        suite.addTest(new BasicCompletionTest("testNoNamespaceCompletion"));
        suite.addTest(new BasicCompletionTest("testPurchaseOrder"));
        suite.addTest(new BasicCompletionTest("testPurchaseOrder1"));
        suite.addTest(new BasicCompletionTest("testPurchaseOrder2"));
        suite.addTest(new BasicCompletionTest("testCompletionFilter1"));
        suite.addTest(new BasicCompletionTest("testCompletionFilter2"));
        suite.addTest(new BasicCompletionTest("testEmptyTag1"));
        suite.addTest(new BasicCompletionTest("testEmptyTag2"));
        suite.addTest(new BasicCompletionTest("testEmptyTag3"));
        suite.addTest(new BasicCompletionTest("testEmptyTag4"));
        suite.addTest(new BasicCompletionTest("testEndtagCompletion1"));
        suite.addTest(new BasicCompletionTest("testEndtagCompletion2"));
        suite.addTest(new BasicCompletionTest("testEndtagCompletion3"));
        suite.addTest(new BasicCompletionTest("testSchemaFromRuntimeCatalog"));
        //suite.addTest(new BasicCompletionTest("testCompletionUsingSchemaFromCatalog"));
        suite.addTest(new BasicCompletionTest("testWildcard1"));
        suite.addTest(new BasicCompletionTest("testWildcard2"));
        suite.addTest(new BasicCompletionTest("testWildcard3"));
        suite.addTest(new BasicCompletionTest("testWildcard4"));
        suite.addTest(new BasicCompletionTest("testChildren1"));
        suite.addTest(new BasicCompletionTest("testChildren2"));
        suite.addTest(new BasicCompletionTest("testReadNamespace"));
        suite.addTest(new BasicCompletionTest("testImport1"));
        suite.addTest(new BasicCompletionTest("testInclude1"));
        return suite;
    }
    
    /**
     * Queries elements from schema with no namespace.
     */
    public void testNoNamespaceCompletion() throws Exception {
        setupCompletion("resources/NoTNS.xml", null);
        List<CompletionResultItem> items = query(157);
        String[] expectedResult = {"NNSChild1", "NNSChild2"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from a PO schema.
     */
    public void testPurchaseOrder() throws Exception {
        setupCompletion("resources/PO.xml", null);
        List<CompletionResultItem> items = query(227);
        String[] expectedResult = {"po:shipTo", "po:billTo", "po:comment", "po:items"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from a PO schema.
     */
    public void testPurchaseOrder1() throws Exception {
        setupCompletion("resources/PO1.xml", null);
        List<CompletionResultItem> items = query(237);
        String[] expectedResult = null;
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from a PO schema.
     * Issue: http://www.netbeans.org/issues/show_bug.cgi?id=117841
     */
    public void testPurchaseOrder2() throws Exception {
        setupCompletion("resources/PO2.xml", null);
        List<CompletionResultItem> items = query(237);
        String[] expectedResult = null;
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests completion filtering.
     */
    public void testCompletionFilter1() throws Exception {
        setupCompletion("resources/PO3.xml", null);
        List<CompletionResultItem> items = query(228);
        String[] expectedResult = {};
        assertResult(items, expectedResult);
    }
    
    
    /**
     * Tests completion filtering.
     */
    public void testCompletionFilter2() throws Exception {
        setupCompletion("resources/PO4.xml", null);
        List<CompletionResultItem> items = query(230);
        String[] expectedResult = {"po:shipTo"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests end tag completion.
     */
    public void testEndtagCompletion1() throws Exception {
        setupCompletion("resources/PO5.xml", null);
        List<CompletionResultItem> items = query(240);
        String[] expectedResult = null;
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests end tag completion.
     */
    public void testEndtagCompletion2() throws Exception {
        setupCompletion("resources/PO6.xml", null);
        List<CompletionResultItem> items = query(274);
        String[] expectedResult = {};
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests end tag completion.
     */
    public void testEndtagCompletion3() throws Exception {
        setupCompletion("resources/PO7.xml", null);
        List<CompletionResultItem> items = query(261);
        String[] expectedResult = {};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries an empty tag. If cursor is between < and A31 in <A31 />
     * we should show all qualifying elements that are children of the parent.
     */
    public void testEmptyTag1() throws Exception {
        setupCompletion("resources/EmptyTag.xml", null);
        List<CompletionResultItem> items = query(220);
        String[] expectedResult = {"A:A31", "A:A32"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries an empty tag. If cursor is after 'A' in <A31 />
     * we should show only A31 as the qualifying element.
     */
    public void testEmptyTag2() throws Exception {
        setupCompletion("resources/EmptyTag.xml", null);
        List<CompletionResultItem> items = query(223);
        String[] expectedResult = {"A:A31"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries attributes inside an empty tag.
     */
    public void testEmptyTag3() throws Exception {
        setupCompletion("resources/EmptyTag.xml", null);
        List<CompletionResultItem> items = query(226);
        String[] expectedResult = {"attrA31", "attrA32"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Issue: 108634, shouldn't show attributes after empty tag.
     */
    public void testEmptyTag4() throws Exception {
        setupCompletion("resources/EmptyTag.xml", null);
        List<CompletionResultItem> items = query(229);
        String[] expectedResult = null;
        assertResult(items, expectedResult);
    }
    
    public void testSchemaFromRuntimeCatalog() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<persistence xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=67
        buffer.append("  xmlns=\"http://java.sun.com/xml/ns/persistence\"\n"); //offset=49
        buffer.append("  xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd\">\n"); //offset=122
        buffer.append("  <persistence-unit>\n"); //offset=21
        buffer.append("  <\n"); //offset=06
        buffer.append("  <persistence-unit>\n"); //offset=21
        buffer.append("</persistence>");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(304);
//        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2", "A:rootA1",
//            "A:rootA2", "A:rootA3", "A:rootA3", "A:A21", "A:A22"};
//        assertResult(items, expectedResult);
    }
    
    public void testCompletionUsingSchemaFromCatalog() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<web-app version=\"2.5\"\n"); //offset=23
        buffer.append("    xmlns=\"http://java.sun.com/xml/ns/javaee\"\n"); //offset=46
        buffer.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=58
        buffer.append("    xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\">\n"); //offset=110
        buffer.append("    <\n"); //offset=6
        buffer.append("</web-app>");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(280);
//        String[] expectedResult = {"A:rootA1", "A:rootA2", "A:rootA3", "A:A11", "A:A12"};
//        assertResult(items, expectedResult);
    }
    
    public void testWildcard1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\">\n"); //offset=66
        buffer.append("  <\n"); //offset=4
        buffer.append("</A:rootA1>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(221);
        String[] expectedResult = {"A:rootA1", "A:rootA2", "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\n"); //offset=64
        buffer.append("  http://xml.netbeans.org/schema/TNSB B.xsd\">\n"); //offset=46
        buffer.append("  <\n");
        buffer.append("</A:rootA1>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(265);
        String[] expectedResult = {"ns1:rootB1", "ns1:rootB2", "A:rootA1", "A:rootA2",
        "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard3() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xmlns:B=\"http://xml.netbeans.org/schema/TNSB\"\n"); //offset=48
        buffer.append("  xmlns:C=\"http://xml.netbeans.org/schema/TNSC\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\n"); //offset=64
        buffer.append("  http://xml.netbeans.org/schema/TNSB B.xsd\n"); //offset=44
        buffer.append("  http://xml.netbeans.org/schema/TNSC C.xsd\">\n"); //offset=46
        buffer.append("  <\n"); //offset=04
        buffer.append("</A:rootA1>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(405);
        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2",
        "A:rootA1", "A:rootA2", "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xmlns:B=\"http://xml.netbeans.org/schema/TNSB\"\n"); //offset=48
        buffer.append("  xmlns:C=\"http://xml.netbeans.org/schema/TNSC\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\n"); //offset=64
        buffer.append("  http://xml.netbeans.org/schema/TNSB B.xsd\n"); //offset=44
        buffer.append("  http://xml.netbeans.org/schema/TNSC C.xsd\">\n"); //offset=46
        buffer.append("  <\n"); //offset=04
        buffer.append("</A:rootA2>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(405);
        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2", "A:rootA1",
            "A:rootA2", "A:rootA3", "A:rootA3", "A:A21", "A:A22"};
        assertResult(items, expectedResult);
    }
            
    public void testChildren1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<ns0:component xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=69
        buffer.append("  xmlns:ns0=\"http://xml.netbeans.org/schema/newXMLSchema\"\n"); //offset=58
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/newXMLSchema Test.xsd\">\n"); //offset=77
        buffer.append("  <ns0:uninstallList>\n"); //offset 22
        buffer.append("  <\n"); //offset 4
        buffer.append("  </ns0:uninstallList>\n");
        buffer.append("</ns0:component>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(269);
        String[] expectedResult = {"ns0:uninstallSteps"};
        assertResult(items, expectedResult);
    }

    public void testChildren2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<ns0:component xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=69
        buffer.append("  xmlns:ns0=\"http://xml.netbeans.org/schema/newXMLSchema\"\n"); //offset=58
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/newXMLSchema Test.xsd\">\n"); //offset=77
        buffer.append("  <ns0:installList/>\n"); //offset 20
        buffer.append("  <\n"); //offset 4
        buffer.append("</ns0:component>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(267);
        String[] expectedResult = {"ns0:installList","ns0:uninstallList"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Reads the namespaces specified in an instance document like project.xml.
     */
    public void testReadNamespace() throws Exception {
        setupCompletion(PROJECT_INSTANCE_DOCUMENT, null);
        String[] results = CompletionUtil.getDeclaredNamespaces(FileUtil.toFile(instanceFileObject));
        String[] expectedResult = {"http://www.netbeans.org/ns/project/1","http://www.netbeans.org/ns/nb-module-project/3"};
        assertResult(results, expectedResult);
    }

    public void testImport1() throws Exception {
        setupCompletion("resources/Import.xml", null);
        List<CompletionResultItem> items = query(246);
        String[] expectedResult = {"ns1:A1","ns1:A2"};
        assertResult(items, expectedResult);
    }
    
    public void testInclude1() throws Exception {
        setupCompletion("resources/Include.xml", null);
        List<CompletionResultItem> items = query(233);
        String[] expectedResult = {"ns:M1","ns:M2"};
        assertResult(items, expectedResult);
    }

    /**
     * Query attributes at offset 217. See Attr1.xml.
     * Should fetch five attributes.
     */
    public void testAttributes1() throws Exception {
        setupCompletion("resources/Attr1.xml", null);
        List<CompletionResultItem> items = query(217);
        String[] expectedResult = {"attrA1", "attrA2", "attrA3", "attrA4", "attrA5"};
        assertResult(items, expectedResult);
    }

}
