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

package folders.javaFolder1000;

import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 * Test of popup menu on nodes in Files View.
 * @author  mmirilovic@netbeans.org
 */
public class FilesViewPopupMenuTest_2 extends PerformanceTestCase {

    private static FilesTabOperator filesTab = null;
    private Node node;
    protected static Node dataObjectNode;
    
    /** Creates a new instance of FilesViewPopupMenu */
    public FilesViewPopupMenuTest_2(String testName) {
        super(testName);
    }

    /** Creates a new instance of FilesViewPopupMenu */
    public FilesViewPopupMenuTest_2(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(FilesViewPopupMenuTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testProjectNodePopupMenuFiles() {
        node=getProjectNode();
        doMeasurement();
    }
    
    public void testPackagePopupMenuFiles(){
        node=new Node(getProjectNode(), "src|org|netbeans|test|performance");
        doMeasurement();
    }
    
    public void testbuildXmlFilePopupMenuFiles(){
        node=new Node(getProjectNode(), "build.xml");
        doMeasurement();
    }
    
    private Node getProjectNode() {
        if(filesTab==null)
            filesTab = new FilesTabOperator();
        
        return filesTab.getProjectNode("PerformanceTestData");
    }

    //@Override
    public void prepare() {
    
    }

    public void close() {
        node.tree().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    //@Override
    public ComponentOperator open() {
        node.callPopup();
        return null;
    }

}
