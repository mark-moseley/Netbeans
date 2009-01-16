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

package org.netbeans.performance.j2se.actions;

import java.io.IOException;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of Find Usages
 *
 * @author  mmirilovic@netbeans.org
 */
public class RefactorFindUsagesTest extends PerformanceTestCase {
    
    private static Node testNode;
    private String TITLE, ACTION, NEXT;
    private static NbDialogOperator refactorDialog;
    private static TopComponentOperator usagesWindow;
    
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public RefactorFindUsagesTest(String testName) {
        super(testName);
        expectedTime = 120000; // the action has progress indication and it is expected it will last
    }
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public RefactorFindUsagesTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 120000; // the action has progress indication and it is expected it will last
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(RefactorFindUsagesTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testSetupjEditProject() {
       CommonUtilities.jEditProjectOpen();
        try {
            this.openDataProjects("jEdit41");
        } catch (IOException ex) {
        }
    }

    public void testRefactorFindUsages(){
        doMeasurement();
    }    
    
    @Override
    public void initialize() {
        String BUNDLE = "org.netbeans.modules.refactoring.java.ui.Bundle";
        NEXT = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle","CTL_Find");  // "Next >"
        TITLE = Bundle.getStringTrimmed(BUNDLE,"LBL_WhereUsed");  // "Find Usages"
        ACTION = Bundle.getStringTrimmed(BUNDLE,"LBL_WhereUsedAction"); // "Find Usages..."
        testNode = new Node(new SourcePackagesNode("jEdit"),"org.gjt.sp.jedit|jEdit.java");
    }
    
    public void prepare() {
        testNode.performPopupAction(ACTION);
        refactorDialog = new NbDialogOperator(TITLE);
    }
    
    public ComponentOperator open() {

        new JButtonOperator(refactorDialog, NEXT).push();
        
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 360000);
        usagesWindow = new TopComponentOperator("Usages"); // NOI18N
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        
        return usagesWindow;
    }

    @Override
    public void close() {
        usagesWindow.close();
    }

    public void shutdown() {
        System.gc();
        System.gc();
        System.gc();
    }
}
