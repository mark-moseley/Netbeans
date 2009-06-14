/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.refactoring;

import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;

public class JavaCardXMLRefactoringSupport {

    private Document doc;

    private JavaCardXMLRefactoringSupport(Document doc) {
        this.doc = doc;
    }

    public Document getDocument() {
        return doc;
    }

    public static JavaCardXMLRefactoringSupport fromFile(File file) {
        try {
            return new JavaCardXMLRefactoringSupport(
                    docBuilder.parse(file));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public NodeList getDynamicallyLoadedClassElements() {
        try {
            return (NodeList) dynamicallyLoadedClassXPression.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            return null;
        }
    }

    public NodeList getShareableInterfaceClassElements() {
        try {
            return (NodeList) shareableInterfaceClassXPression.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            return null;
        }
    }
    private static DocumentBuilder docBuilder;
    

    {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        try {
            docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Internal error: failed to obtain" + " DocumentBuilder instance.", e);
        }
    }
    private XPathFactory xpFactory = XPathFactory.newInstance();
    private XPath xPath = xpFactory.newXPath();
    private XPathExpression dynamicallyLoadedClassXPression;
    private XPathExpression shareableInterfaceClassXPression;
    

    {
        try {
            dynamicallyLoadedClassXPression =
                    xPath.compile("/javacard-app/dynamically-loaded-classes/class/@name");
            shareableInterfaceClassXPression =
                    xPath.compile("/javacard-app/shareable-interface-classes/class/@name");
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Internal initialization failure", e);
        }
    }
}
