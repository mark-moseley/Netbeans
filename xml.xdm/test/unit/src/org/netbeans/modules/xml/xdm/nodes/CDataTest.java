/*
 * TextTest.java
 * JUnit based test
 *
 * Created on October 21, 2005, 2:21 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import java.io.IOException;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.visitor.PrintVisitor;
import org.w3c.dom.NodeList;

/**
 *
 * @author ajit
 */
public class CDataTest extends TestCase {
    
    public CDataTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        baseDocument = Util.getResourceAsDocument("nodes/cdata.xml");
        xmlModel = Util.loadXDMModel(baseDocument);
        text = getCDataNode();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(CDataTest.class);
        
        return suite;
    }

    /**
     * Test of getNodeValue method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeValue() {
        String expResult = " function match(a,b) if (a > 0 && b < 7) <a/> ";
        String result = text.getNodeValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeType() {
        short expResult = org.w3c.dom.Node.CDATA_SECTION_NODE;
        short result = text.getNodeType();
        assertEquals("getNodeType must return CDATA_SECTION_NODE",expResult, result);
    }

    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeName() {
        String expResult = "#cdata-section";
        String result = text.getNodeName();
        assertEquals("getNodeName must return #cdata-section",expResult, result);
    }

    /**
     * Test of getNamespaceURI method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNamespaceURI() {
        String result = text.getNamespaceURI();
        assertNull(result);
    }

    public void testMultiLineCData() {
	CData c = getMultiLineCData();
	final String expectedValue = "\n<!--line1-->\n<!--line2-->\n";
	assertEquals(c.getData(),expectedValue);
    }
    
    public void testGetData() {
	testGetNodeValue();
    }
    
    public void testSetData() {
	String tValue = "<xslice>embedded values </xslice>";
	try {
	    text.setData(tValue);
	    fail("node not cloned");
	} catch (Exception e) {
	    
	}
	CData clone = (CData) text.cloneNode(true);
	clone.setData(tValue);
	assertEquals(tValue, clone.getData());
	xmlModel.modify(text,clone);
	xmlModel.flush();
	try {
	    xmlModel.sync();
	} catch (IOException ex) {
	    fail("sync threw exception");
	}
	assertEquals(tValue, getCDataNode().getNodeValue());
	assertEquals(3,getCDataNode().getTokens().size());
    }
    
     private CData getMultiLineCData() {
	Element root = (Element) xmlModel.getDocument().getChildNodes().item(0);
	org.w3c.dom.Node multiLineComment = root.getElementsByTagName("multi-line-cdata").item(0);
	NodeList nl = multiLineComment.getChildNodes();
	CData cdata = null;
	for (int i = 0; i < nl.getLength(); i++) {
	    org.w3c.dom.Node n = nl.item(i);
	    if (n instanceof CData) {
		cdata = (CData) n;
		break;
	    }
	}
	return cdata;
    }
    
    private CData getCDataNode() {
	Element root = (Element) xmlModel.getDocument().getChildNodes().item(0);
	Element script = (Element) root.getElementsByTagName("cdata").item(0);
	NodeList nl = script.getChildNodes();
	CData cdataSection = null;
	for (int i = 0; i < nl.getLength(); i++) {
	    org.w3c.dom.Node n = nl.item(i);
	    if (n instanceof CData) {
		cdataSection = (CData)n;
		break;
	    }
	}
	return cdataSection;
    }
    
    private XDMModel xmlModel;
    private CData text;
    private javax.swing.text.Document baseDocument;
}
