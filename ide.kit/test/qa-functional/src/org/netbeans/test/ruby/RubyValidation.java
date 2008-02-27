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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.test.ruby;

import javax.swing.JTextField;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 * Overall validation suite for ruby cluster.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class RubyValidation extends JellyTestCase {
    
    /** Need to be defined because of JUnit */
    public RubyValidation(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new RubyValidation("testCreateRubyProject"));
        suite.addTest(new RubyValidation("testRunRubyFile"));
        suite.addTest(new RubyValidation("testCreateRailsProject"));
        suite.addTest(new RubyValidation("testRailsGenerate"));
        suite.addTest(new RubyValidation("testIrbShell"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new RubyValidation("testCreateRubyProject"));
    }
    
    /** Setup before every test case. */
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Teardown after every test case. */
    @Override
    public void tearDown() {
    }
    
    // name of sample projects
    private static final String SAMPLE_RUBY_PROJECT_NAME = "SampleRubyApplication";  //NOI18N
    private static final String SAMPLE_RAILS_PROJECT_NAME = "SampleRailsApplication";  //NOI18N

    
    /** Test IRB shell
     * - open IRB shell window 
     * - close it
     */
    public void testIrbShell() throws Exception{
        String rubyproject_packagename = "org.netbeans.modules.ruby.rubyproject.Bundle";
        String irb_action = Bundle.getStringTrimmed(rubyproject_packagename, "CTL_IrbAction");
        String irb_TC = Bundle.getString(rubyproject_packagename, "CTL_IrbTopComponent");
        new Action("Window|Other|" + irb_action, null).perform(); //NOI18N
        TopComponentOperator tco = new TopComponentOperator(irb_TC);
        tco.close();
    }   


    /** Test Ruby Application
     * - open new project wizard
     * - choose Ruby|Ruby Application
     * - click Next
     * - type name and location and finish the wizard
     * - wait until project is in Projects view
     * - wait classpath scanning finished
     */
    public void testCreateRubyProject() {
        // create new web application project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Ruby"
        String rubyLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "Templates/Project/Ruby");
        npwo.selectCategory(rubyLabel);
        // "Ruby Application"
        String rubyApplicationLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "TXT_NewJavaApp");
        npwo.selectProject(rubyApplicationLabel);
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_RUBY_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.finish();
        // wait project appear in projects view
        // wait 30 second
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); // NOI18N
        new ProjectsTabOperator().getProjectRootNode(SAMPLE_RUBY_PROJECT_NAME);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
    }
    
    /** Test run Ruby file
     * - find main.rb in editor
     * - call "Run File" popup action in editor
     * - wait for main.rb output tab
     * - check "Hello World" is printed out
     */
    public void testRunRubyFile() {
        // wait main.rb is opened in editor
        EditorOperator editor = new EditorOperator("main.rb"); // NOI18N
        // "Run File"
        String runFileItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_RunSingleAction_Name", new Integer[] {0});
        // call "Run File" in editor
        new Action(null, runFileItem).perform(editor);
        // check message in output tab
        new OutputTabOperator("main.rb").waitText("Hello World"); // NOI18N
    }
    
    /** Test Ruby on Rails Application
     * - create new Ruby on Rails Application project
     * - wait until project is in Projects view
     * - wait classpath scanning finished
     */
    public void testCreateRailsProject() {
        // create new web application project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Ruby"
        String rubyLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "Templates/Project/Ruby");
        npwo.selectCategory(rubyLabel);
        // "Ruby on Rails Application"
        String railsApplicationLabel = Bundle.getString("org.netbeans.modules.ruby.railsprojects.ui.wizards.Bundle", "Templates/Project/Ruby/railsApp.xml");
        npwo.selectProject(railsApplicationLabel);
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_RAILS_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.finish();
        // wait project appear in projects view
        // wait 30 second
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); // NOI18N
        new ProjectsTabOperator().getProjectRootNode(SAMPLE_RAILS_PROJECT_NAME);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
    }
    
    /** Test Rails Generator
     * - call "Generate..." action on project node
     * - wait for Rails Generator dialog
     * - type "myapp" in Name text field
     * - type "myview" in Views: text field
     * - click OK button in dialog
     * - check files myapp_controller.rb, myapp_helper.rb, myview.rhtml, 
     * myapp_controller_test.rb are opened in editor and available in Projects view
     */
    public void testRailsGenerate() {
        ProjectRootNode projectRootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_RAILS_PROJECT_NAME);

        // "Generate..."
        String generateItem = Bundle.getStringTrimmed("org.netbeans.modules.ruby.railsprojects.Bundle", "rails-generator");
        new ActionNoBlock(null, generateItem).perform(projectRootNode);
        // "Rails Generator"
        String generatorTitle = Bundle.getStringTrimmed("org.netbeans.modules.ruby.railsprojects.Bundle", "RailsGenerator");
        NbDialogOperator generatorOper = new NbDialogOperator(generatorTitle);
        // "Name:"
        String nameLabel = Bundle.getStringTrimmed("org.netbeans.modules.ruby.railsprojects.Bundle", "Name");
        JTextFieldOperator nameOper = new JTextFieldOperator((JTextField)new JLabelOperator(generatorOper, nameLabel).getLabelFor());
        nameOper.setText("myapp");  // NOI18N
        // "Views:"
        String viewsTextFieldLabel = Bundle.getStringTrimmed("org.netbeans.modules.ruby.railsprojects.Bundle", "Views");
        JTextFieldOperator viewsOper = new JTextFieldOperator((JTextField)new JLabelOperator(generatorOper, viewsTextFieldLabel).getLabelFor());
        viewsOper.setText("myview");
        generatorOper.ok();
        
        // wait 60 second
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 60000);
        
        String filename = "myapp_controller.rb"; // NOI18N
        new EditorOperator(filename);
        // "Controllers"
        String controllersLabel = Bundle.getString("org.netbeans.modules.ruby.railsprojects.Bundle", "app_controllers");
        new Node(projectRootNode, controllersLabel+"|"+filename);
        
        filename = "myapp_helper.rb"; // NOI18N
        new EditorOperator(filename);
        // "Helpers"
        String helpersLabel = Bundle.getString("org.netbeans.modules.ruby.railsprojects.Bundle", "app_helpers");
        new Node(projectRootNode, helpersLabel+"|"+filename);
        
        filename = "myview.html.erb"; // NOI18N
        new EditorOperator(filename);
        // "Views"
        String viewsLabel = Bundle.getString("org.netbeans.modules.ruby.railsprojects.Bundle", "app_views");
        new Node(projectRootNode, viewsLabel+"|myapp|"+filename);
        
        filename = "myapp_controller_test.rb"; // NOI18N
        new EditorOperator(filename);
        // "Functional Tests"
        String functionalTestsLabel = Bundle.getString("org.netbeans.modules.ruby.railsprojects.Bundle", "test_functional");
        new Node(projectRootNode, functionalTestsLabel+"|"+filename);
    }
}