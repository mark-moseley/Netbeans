/*
 * WSDLComponentBaseTest.java
 * JUnit based test
 *
 * Created on March 25, 2006, 5:23 AM
 */

package org.netbeans.modules.xml.wsdl.model.spi;

import java.util.Map;
import javax.xml.namespace.QName;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author nn136682
 */
public class WSDLComponentBaseTest extends TestCase {
    
    public WSDLComponentBaseTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(WSDLComponentBaseTest.class);
        
        return suite;
    }

    public void testRemoveDocumentation() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/HelloService.wsdl");
        Definitions definitions = model.getDefinitions();
        Types types = definitions.getTypes();
        assertEquals("testing remove documentation", types.getDocumentation().getContentFragment());
        model.startTransaction();
        types.setDocumentation(null);
        model.endTransaction();
        assertNull(types.getDocumentation());
    }
    
    public void testGetAttributeMap() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/HelloService.wsdl");
        Definitions definitions = model.getDefinitions();
        
        Map<QName,String> map = definitions.getAttributeMap();
        assertEquals(2, map.keySet().size());
        assertEquals( "HelloService", map.get(new QName("name")));
        assertEquals("urn:HelloService/wsdl", map.get(new QName("targetNamespace")));
    }
}
