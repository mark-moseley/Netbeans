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

package org.netbeans.performance.visualweb.dialogs;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class ManageComponentLibrariesDialog  extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    
    private NbDialogOperator parent, dialog;
    private JButtonOperator manageComponentLibsButton;
    
    private Node openNode;
    
    /** Creates a new instance of ManageComponentLibrariesDialog */
    public ManageComponentLibrariesDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;            
    }
    
    /** Creates a new instance of ManageComponentLibrariesDialog */
    public ManageComponentLibrariesDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;        
    }
    
    public void testManageComponentLibrariesDialog() {
        doMeasurement();
    }
    
    @Override
    protected void initialize() {
        log("::initialize");
        Node projectRoot = null;
        try {
            projectRoot = new ProjectsTabOperator().getProjectRootNode("VisualWebProject");
            projectRoot.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
        String nodePath = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.displayName");
        log("Node path = "+nodePath);
        openNode = new Node(projectRoot,nodePath);
    }
    
    public void prepare() {
        log("::prepare");
        openNode.callPopup().pushMenuNoBlock(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.addComplibAction"));
        parent = new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.addComplibTitle"));
        manageComponentLibsButton = new JButtonOperator(parent,"Manage Component Libraries..."); // NOI18N
    }

    public ComponentOperator open() {
        log(":: open");
        manageComponentLibsButton.pushNoBlock();
        dialog = new NbDialogOperator("Component Libraries"); // NOI18N
        return null;
    }

    @Override
    public void close() {
        log("::close");
        dialog.close();
    }

    @Override
    protected void shutdown() {
        log(":: shutdown");
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new ManageComponentLibrariesDialog("measureTime","Measure Manage Component Libraries Dialog open time"));
    }

}
