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

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.project.ui.test.ProjectSupport;


/**
 * Test Add New Bpel Process
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class AddNewEnterprise extends PerformanceTestCase {
    protected NewProjectNameLocationStepOperator projectName_wizardLocation; // wizard_location
    protected NewFileNameLocationStepOperator fileName_wizardLocation; // location
    
    protected static final String BUNDLE = "org.netbeans.modules.bpel.project.wizards.Bundle";
    // TODO FIXME
    //protected static final String PROJECT_CATEGORY = Bundle.getStringTrimmed(BUNDLE,"Templates/Project/SOA"); // "Service Oriented Architecture"
    //protected static final String PROJECT_TYPE = Bundle.getStringTrimmed(BUNDLE,"Templates/Project/SOA/emptyBpelpro.xml"); // "BPEL Module"
    protected static final String PROJECT_CATEGORY = "SOA";
    protected static final String PROJECT_TYPE = "BPEL Module";
    
    protected String project_name, file_category, file_type, file_name;
    protected int index=0;
    
    /**
     * Creates a new instance of AddNewBpelProcess
     * @param testName the name of the test
     */
    public AddNewEnterprise(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of AddNewBpelProcess
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddNewEnterprise(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public void testAddNewBpelProcess(){
        project_name = "BPELModule";
        file_category = "Service Oriented Architecture"; // NOI18N
        file_type = "BPEL Process"; // NOI18N
        file_name = "BPELProcess";
        doMeasurement();
    }
    
    public void testAddNewWSDLDocument(){
        project_name = "BPELModule";
        file_category = "XML"; // NOI18N
        file_type = "WSDL Document"; // NOI18N
        file_name = "WSDLDoc";
        doMeasurement();
    }
    
    public void testAddNewXMLDocument(){
        project_name = "BPELModule";
        file_category = "XML"; // NOI18N
        file_type = "XML Document"; // NOI18N
        file_name = "XMLDoc";
        doMeasurement();
    }
    
    public void testAddNewXMLSchema(){
        project_name = "BPELModule";
        file_category = "XML"; // NOI18N
        file_type = "XML Schema"; // NOI18N
        file_name = "XMLSchema";
        doMeasurement();
    }
    
    @Override
    protected void initialize() {
        //create bpel project
        NewProjectWizardOperator wizardP = NewProjectWizardOperator.invoke();
        wizardP.selectCategory(PROJECT_CATEGORY);
        wizardP.selectProject(PROJECT_TYPE);
        wizardP.next();
        projectName_wizardLocation = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir")+java.io.File.separator+"createdProjects";
        log("================= Destination directory={"+directory+"}");
        
        new EventTool().waitNoEvent(1000);
        projectName_wizardLocation.txtProjectLocation().setText(directory);
        
        log("================= Project name="+project_name+"}");
        projectName_wizardLocation.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        projectName_wizardLocation.txtProjectName().typeText(project_name);
        new EventTool().waitNoEvent(1000);
        projectName_wizardLocation.finish();
        //bpel end
    }
    
    public void prepare(){
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        wizard.selectCategory(file_category);
        wizard.selectFileType(file_type);
        
        wizard.next();
        
        new EventTool().waitNoEvent(1000);
        fileName_wizardLocation = new NewFileNameLocationStepOperator();
        fileName_wizardLocation.txtObjectName().setText(file_name + "_" + (index++));
    }
    
    public ComponentOperator open(){
        fileName_wizardLocation.finish();
        return null;
    }
    
    @Override
    public void close(){
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    @Override
    protected void shutdown() {
        ProjectSupport.closeProject(project_name);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(AddNewEnterprise.class)
            .addTest("measureTime")
            .enableModules(".*")
            .clusters(".*")
        );    
    }
}
