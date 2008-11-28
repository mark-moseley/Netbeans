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

package org.netbeans.performance.languages.actions;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewPHPProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mrkam@netbeans.org
 */
public class SavingPHPProjectPropertiesTest  extends PerformanceTestCase {

    public String category, project, projectName, projectType,  editorName;
    private Node testNode;
    private String title;
    private JButtonOperator okButton;

    public SavingPHPProjectPropertiesTest(String testName) {
        super(testName);        
        expectedTime = 1000;
    }
    
    public SavingPHPProjectPropertiesTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 1000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ScriptingSetup.class)
             .addTest(SavingPHPProjectPropertiesTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize(){
        closeAllModal();
        createProject();
        title = Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.customizer.Bundle", "LBL_Customizer_Title", new String[]{projectName});
        testNode = (Node) new ProjectsTabOperator().getProjectRootNode(projectName);
    }

    private void createProject() {
        NewPHPProjectNameLocationStepOperator wizard_location;
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewPHPProjectNameLocationStepOperator();
        projectName = projectType + "_" + System.currentTimeMillis();
        wizard_location.typeProjectName(projectName);
        String directory = CommonUtilities.getTempDir() + "createdProjects" 
                + java.io.File.separator + projectName;
        wizard_location.typeSourcesFolder(directory);
        wizard.next();
        wizard_location.finish();
        wizard_location.waitClosed();
        TopComponentOperator.findTopComponent(editorName, 0);
    }

    @Override
    public void prepare(){
        new PropertiesAction().performPopup(testNode);
        NbDialogOperator propertiesDialog = new NbDialogOperator(title);
        new JCheckBoxOperator(propertiesDialog, Bundle.getStringTrimmed(
                "org.netbeans.modules.php.project.ui.customizer.Bundle",
                "CustomizerSources.shortTagsCheckBox.AccessibleContext.accessibleName"))
                .clickMouse();
        okButton = new JButtonOperator(propertiesDialog, 
                Bundle.getStringTrimmed("org.netbeans.modules.project.uiapi.Bundle",
                "LBL_Customizer_Ok_Option"));
    }

    public ComponentOperator open(){
        okButton.push();
        return null;
    }

    public void testSavingPhpProjectProperties() {
        category = "PHP";
        project = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "Templates/Project/PHP/PHPProject.php");
        projectType = "PHPApplication";
        editorName = "index.php";
        doMeasurement();        
    }

}
