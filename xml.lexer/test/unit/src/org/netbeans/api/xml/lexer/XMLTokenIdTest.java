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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.xml.lexer;

import junit.framework.*;

/**
 * The XMLTokenIdTest tests the parsing algorithm of XMLLexer.
 * Various tests include, sanity, regression, performance etc.
 * @author Samaresh (samaresh.panda@sun.com)
 */
public class XMLTokenIdTest extends AbstractTestCase {
    
    public XMLTokenIdTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLTokenIdTest("testTokens"));
        //regression tests on XMLLexer
        suite.addTest(new XMLTokenIdTest("testParse1"));
        suite.addTest(new XMLTokenIdTest("testParse2"));
        suite.addTest(new XMLTokenIdTest("testParse3"));
        //measure performace
        suite.addTest(new XMLTokenIdTest("testParsePerformance"));
        return suite;
    }
    
    /**
     * This test parses a xml/schema that was earlier failing.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=124731
     * See http://hg.netbeans.org/main?cmd=changeset;node=34612be91839
     */
    public void testParse1() throws Exception {
        javax.swing.text.Document document = getDocument("resources/UBL-CommonAggregateComponents-1.0.xsd");
        parse(document);
    }
    
    /**
     * This test parses a xml/schema that was earlier failing.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=125005
     * See http://hg.netbeans.org/main?cmd=changeset;node=dcd138bddc6c
     */
    public void testParse2() throws Exception {
        javax.swing.text.Document document = getDocument("resources/wsdl.xml");
        parse(document);
    }
    
    /**
     * This test parses a xml/schema that was earlier failing.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=139184
     */
    public void testParse3() throws Exception {
        javax.swing.text.Document document = getDocument("resources/test1.xml");
        parse(document);
    }
    
    /**
     * This test measures the performance of XMLLexer on healthcare schema.
     */
    public void testParsePerformance() throws Exception {
        javax.swing.text.Document document = getDocument("resources/fields.xsd");
        long start = System.currentTimeMillis();
        parse(document);
        long end = System.currentTimeMillis();
        System.out.println("Time taken to parse healthcare schema: " + (end-start) + "ms.");
    }
    
    /**
     * This test validates all tokens obtained by parsing test.xml against
     * an array of expected tokens.
     */
    public void testTokens() throws Exception {
        XMLTokenId[] expectedIds = {XMLTokenId.PI_START, XMLTokenId.PI_TARGET, XMLTokenId.WS, XMLTokenId.PI_CONTENT,
            XMLTokenId.PI_END, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.ARGUMENT, XMLTokenId.OPERATOR, XMLTokenId.VALUE,
            XMLTokenId.WS, XMLTokenId.ARGUMENT, XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG,
            XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.BLOCK_COMMENT, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.TAG,
            XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.TAG, XMLTokenId.TEXT};
        
        javax.swing.text.Document document = getDocument("resources/test.xml");
        assertTokenSequence(document, expectedIds);
    }    
}