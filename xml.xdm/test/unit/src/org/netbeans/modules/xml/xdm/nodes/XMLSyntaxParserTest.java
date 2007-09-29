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

/*
 * XMLSyntaxParserTest.java
 * JUnit based test
 *
 * Created on September 26, 2005, 12:38 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import java.util.List;
import junit.framework.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.netbeans.modules.xml.xdm.Util;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class XMLSyntaxParserTest extends TestCase {
    
    public XMLSyntaxParserTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XMLSyntaxParserTest.class);
        
        return suite;
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParse() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/test.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        Document doc = parser.parse(basedoc);
        assertNotNull("Document can not be null", doc);
        FlushVisitor fv = new FlushVisitor();
        String docBuf = fv.flushModel(doc);
        assertEquals("The document should be unaltered",basedoc.getText(0,basedoc.getLength()),docBuf);
    }
	
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalid() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalid.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token") && ex.getMessage().contains("sss"));
        }
    }	
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalidtag.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token") && ex.getMessage().contains("sss"));
        }
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag2() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalidtag2.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token '</a' does not end with '>'"));
        }
    }   
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag3() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalidtag3.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token '<' found in document"));
        }
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag4() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalidtag4.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token '</b' does not end with '>'"));
        }
    }    
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseValidTag() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/validtag.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);            
        } catch(Exception ex) {
            assertTrue("Should not come here", false);
        }
    }    

    public void testParsePI() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("resources/PI_after_prolog.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();

        Document doc = parser.parse(basedoc);            
        List<Token> tokens = doc.getTokens();
        assertEquals(12, tokens.size());
        assertEquals(TokenType.TOKEN_PI_START_TAG, tokens.get(0).getType());
        assertEquals(TokenType.TOKEN_PI_END_TAG, tokens.get(4).getType());
        assertEquals(TokenType.TOKEN_PI_START_TAG, tokens.get(6).getType());
        assertEquals(TokenType.TOKEN_PI_NAME, tokens.get(7).getType());
        assertEquals("Siebel-Property-Set", tokens.get(7).getValue());
        assertEquals(TokenType.TOKEN_PI_VAL, tokens.get(9).getType());
        NodeList nl = doc.getChildNodes();
        assertEquals(2, nl.getLength());    
    }    

    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     * Test the parsing of doctype
     */
    public void testParseDoctype() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/testDoctype.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        Document doc = parser.parse(basedoc);
        assertNotNull("Document can not be null", doc);
        FlushVisitor fv = new FlushVisitor();
        String docBuf = fv.flushModel(doc);
        assertEquals("The document should be unaltered",basedoc.getText(0,basedoc.getLength()),docBuf);
    }
	
}
