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
package org.netbeans.jellytools.nodes;

import java.awt.Toolkit;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.nodes.URLNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class URLNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public URLNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new URLNodeTest("testVerifyPopup"));
        suite.addTest(new URLNodeTest("testCut"));
        suite.addTest(new URLNodeTest("testCopy"));
        suite.addTest(new URLNodeTest("testDelete"));
        suite.addTest(new URLNodeTest("testRename"));
        suite.addTest(new URLNodeTest("testSaveAsTemplate"));
        suite.addTest(new URLNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    protected static URLNode urlNode = null;
    
    /** Finds node before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        // find node
        if(urlNode == null) {
            urlNode = new URLNode(new SourcePackagesNode("SampleProject"), "sample1|url.url");  // NOI18N
        }
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        urlNode.verifyPopup();
    }

    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        urlNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy  */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        urlNode.copy();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test delete  */
    public void testDelete() {
        urlNode.delete();
        Utils.closeConfirmDialog();
    }
    
    /** Test rename  */
    public void testRename() {
        urlNode.rename();
        Utils.closeRenameDialog();
    }
    
    /** Test properties  */
    public void testProperties() {
        urlNode.properties();
        Utils.closeProperties("url"); //NOI18N
    }
    
    /** Test saveAsTemplate  */
    public void testSaveAsTemplate() {
        urlNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
    
}
