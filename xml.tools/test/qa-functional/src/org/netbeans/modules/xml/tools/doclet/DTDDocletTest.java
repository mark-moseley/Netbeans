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
package org.netbeans.modules.xml.tools.doclet;

import junit.textui.TestRunner;
import org.netbeans.modules.xml.core.DTDDataObject;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tax.TreeDTD;
import org.netbeans.tests.xml.XTest;


/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: XMLGenerator3Test
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 * DTDDocletTest checks 'Generate Documentation' action on DTD document. The action is
 * accesible from popup menu an all DTD document nodes.<BR>
 *
 * <BR><B>How it works:</B><BR>
 * Test opens DTD document, generates documentation in HTML for the document and writes
 * the documentation into output.<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * None
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * DTD documentation in HTML format.<BR>
 *
 * <BR><B>Possible reasons of failure:</B>
 * <UL>
 * <LI type="circle">
 * <I>None<BR></I>
 * </LI>
 * </UL>
 * <P>
 */

public class DTDDocletTest extends XTest {
    
    /** Creates new CoreSettingsTest */
    public DTDDocletTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        DTDDataObject dao = (DTDDataObject) TestUtil.THIS.findData("books.dtd");
        if (dao == null) {
            fail("\"data/books.dtd\" data object is not found!");
        }
        TreeEditorCookie cake = (TreeEditorCookie) dao.getCookie(TreeEditorCookie.class);
        TreeDTD dtd = (TreeDTD) cake.openDocumentRoot();
        DTDDoclet doclet = new DTDDoclet();
        String result = doclet.createDoclet(dtd);
        result = TestUtil.replaceString(result, "<!--", "-->", "<!-- REMOVED -->");
        ref(result);
        compareReferenceFiles();
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(DTDDocletTest.class);
    }
}
