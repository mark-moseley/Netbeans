
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

package org.netbeans.performance.enterprise.actions;

import org.netbeans.performance.enterprise.XMLSchemaComponentOperator;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
/**
 *
 * @author mkhramov@netbeans.org
 */
public class SchemaViewSwitchTest extends PerformanceTestCase  {

    private XMLSchemaComponentOperator schema;
    private static String category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA"); // "Service Oriented Architecture";
    private static String project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA/emptyBpelpro.xml"); // "BPEL Module"
    private static String testProjectName = "TestProject2";
    private static String testSchemaName = "XMLTestSchema2";    
    
    
    /** Creates a new instance of SchemaViewSwitch */
    public SchemaViewSwitchTest(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;           
    }
    
    public SchemaViewSwitchTest(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;                
    }    

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(SchemaViewSwitchTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize(){
        log(":: initialize");
        String ParentPath = CommonUtilities.getTempDir() + "createdProjects";

        createProject(ParentPath,testProjectName);
        addSchemaDoc(testProjectName,testSchemaName);
    }

    private void createProject(String path, String projectName) {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        log(":: selecting category: "+category);
        wizard.selectCategory(category);

        log(":: selecting project: "+project);
        wizard.selectProject(project);

        wizard.next();

        NewProjectNameLocationStepOperator wizard_location = new NewProjectNameLocationStepOperator();

        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(projectName);
        new EventTool().waitNoEvent(1000);
        
        wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().typeText(path);
        
        new EventTool().waitNoEvent(1000);

        wizard_location.finish();
    }     

    private void addSchemaDoc( String projectName, String SchemaName) {
        Node pfn =  new Node(new ProjectsTabOperator().getProjectRootNode(projectName), org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources"));
        pfn.select();
        // Workaround for issue 143497
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        wizard.selectCategory("XML");
        wizard.selectFileType("XML Schema");

        wizard.next();

        NewFileNameLocationStepOperator location = new NewFileNameLocationStepOperator();
        location.setObjectName(SchemaName);
        location.finish();
    }    

    public void prepare() {
        log(":: prepare");
        String schemaDocPath = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources")+"|"+testSchemaName+".xsd";
        Node schemaNode = new Node(new ProjectsTabOperator().getProjectRootNode(testProjectName),schemaDocPath);        
        schemaNode.performPopupActionNoBlock("Open");      
        schema = XMLSchemaComponentOperator.findXMLSchemaComponentOperator(testSchemaName+".xsd");
    }

    public ComponentOperator open() {
        schema.getDesignButton().pushNoBlock();
        return schema;
    }

    @Override
    public void close(){
        schema.getSchemaButton().pushNoBlock();
    }

    @Override
    protected void shutdown() {
        log("::shutdown");
        
        new CloseAllDocumentsAction().performAPI();        

//        ProjectSupport.closeProject(testProjectName);
    }
   
}