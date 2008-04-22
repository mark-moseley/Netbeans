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

package gui.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test create BPELmodule
 *
 * @author  rashid@netbeans.org, mrkam@netbeans.org
 */
public class CreateBPELmodule extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name, project_type;
    
    private int index;
    
    /**
     * Creates a new instance of CreateBPELmodule
     * @param testName the name of the test
     */
    public CreateBPELmodule(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of CreateProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateBPELmodule(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    @Override
    public void initialize(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.Bundle", "OpenIDE-Module-Display-Category"); // "SOA"
        project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle", "LBL_BPEL_Wizard_Title"); // "BPEL Module"
        project_type="BPELModule";
        index=1;
        
        runGC(2);
        
        MainWindowOperator.getDefault().maximize();
    }
    
    public void prepare(){
        NewProjectWizardOperator wizard;
        for(int attempt = 1; ; attempt++) {
            log("Attempt " + attempt + " to open New Project Wizard");
            new EventTool().waitNoEvent(3000);
            try {
                wizard = NewProjectWizardOperator.invoke();
                break;
            } catch (RuntimeException exc) {
                if (attempt < 5) {
                    log("Attempt failed with exception: " + exc);
                    exc.printStackTrace(getLog());
                    continue;
                }
                throw exc;
            }
        }   
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.move(0, 0);    
        new EventTool().waitNoEvent(1000);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir")+java.io.File.separator+"createdProjects";
        log("================= Destination directory={"+directory+"}");
      //  wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = project_type + "_" + (index++);
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
    }
    
    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    @Override
    public void close(){
        closeAllModal(); // This is necessary in case open failed
        ProjectSupport.closeProject(project_name);
        runGC(1);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateBPELmodule("measureTime"));
    }
}
