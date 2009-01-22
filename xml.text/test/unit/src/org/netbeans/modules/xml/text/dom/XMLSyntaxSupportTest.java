/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.text.dom;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.AbstractTestCase;

/**
 *
 * @author Samaresh
 */
public class XMLSyntaxSupportTest extends AbstractTestCase {

    public XMLSyntaxSupportTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLSyntaxSupportTest("testParseForeward"));
        suite.addTest(new XMLSyntaxSupportTest("testParseBackward"));
        suite.addTest(new XMLSyntaxSupportTest("testTokens"));
        return suite;
    }

    /**
     * Parses a valid xml documents and reads one node at a time.
     */
    public void testParseForeward() throws Exception {
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(getDocument("syntax/test.xml"));
        SyntaxElement se = support.getElementChain(1);
        StringBuilder actualResult = new StringBuilder();
        while( se != null) {
            actualResult.append("Class: " + se.getClass().getSimpleName() + " Offset: " + se.getElementOffset() + " Length: "+ se.getElementLength());
            se = se.getNext();
        }
        assert(getExpectedResultAsString("dom/result1.txt").equals(actualResult.toString()));
    }
    
    public void testParseBackward() throws Exception {
        BaseDocument doc = getDocument("syntax/test.xml");
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
        SyntaxElement se = support.getElementChain(doc.getLength()-1);
        StringBuilder actualResult = new StringBuilder();
        while( se != null) {
            actualResult.append("Class: " + se.getClass().getSimpleName() + " Offset: " + se.getElementOffset() + " Length: "+ se.getElementLength());
            se = se.getPrevious();
        }
        assert(getExpectedResultAsString("dom/result2.txt").equals(actualResult.toString()));
    }

    public void testTokens() throws Exception {
        BaseDocument doc = getDocument("syntax/test.xml");
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
        Token token = support.getPreviousToken(30);
        System.out.println("Token: " + token.id() + " Text: " + token.text());
        token = support.getPreviousToken(31);
        System.out.println("Token: " + token.id() + " Text: " + token.text());
        token = support.getPreviousToken(32);
        System.out.println("Token: " + token.id() + " Text: " + token.text());
    }

}