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


import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;

/**
 *
 * @author mkhramov@netbeans.org
 */

public class EditorMenuPopup extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    public static final String suiteName="Scripting UI Responsiveness Actions suite";
    protected Node fileToBeOpened;
    protected String testProject;
    protected String docName; 
    protected String pathName;
    protected static ProjectsTabOperator projectsTab = null;    
    private EditorOperator editorOperator;
    
    private JPopupMenuOperator pom;
    
    private int XPopup;
    private int YPopup;
    
    public EditorMenuPopup(String testName) {
        super(testName);
        
        expectedTime = UI_RESPONSE;           
        WAIT_AFTER_PREPARE = 1000;
        
    }
    public EditorMenuPopup(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;           
        WAIT_AFTER_PREPARE = 1000;        
    }
    
    protected Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = ScriptingUtilities.invokePTO();
        
        return projectsTab.getProjectRootNode(projectName);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        log("::initialize");
        String path = pathName+docName;
        fileToBeOpened = new Node(getProjectNode(testProject),path);        
    }
    
    @Override
    public void prepare() {
        log("::prepare"); 
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(docName);  
    }

    @Override
    public ComponentOperator open() {
        log("::open");
        editorOperator.clickForPopup();
        pom = new JPopupMenuOperator();
        
        return null;
    }
    
    @Override
    public void close()
    {
        log("::close");
        editorOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }    
        
    @Override
    public void shutdown(){
        log("::shutdown");
    }    
    
    public void test_RB_EditorPopup() {
        testProject = Projects.RUBY_PROJECT;
        pathName = "Source Files"+"|";
        docName = "ruby20kb.rb"; 
        doMeasurement();
    } 
    public void test_RHTML_EditorPopup() {
        testProject = Projects.RAILS_PROJECT;
        pathName = "Views"+"|";
        docName = "rhtml20kb.rhtml"; 
        doMeasurement();        
    }
    public void test_PHP_EditorPopup() {
        testProject = Projects.PHP_PROJECT;
        pathName = "Source Files"+"|";
        docName = "php20kb.php"; 
        doMeasurement();         
    }
    public void test_JS_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages"+"|";
        docName = "javascript20kb.js"; 
        doMeasurement();        
    }
    public void test_JSON_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages"+"|";
        docName = "json20kb.json"; 
        doMeasurement();        
    }
    public void test_CSS_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages"+"|";
        docName = "css20kb.css";
        doMeasurement();        
    }
    public void test_YML_EditorPopup() {
        testProject = Projects.RAILS_PROJECT;
        pathName = "Configuration"+"|";
        docName = "yaml20kb.yml"; 
        doMeasurement();        
    }
    public void test_BAT_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages"+"|";
        docName = "bat20kb.bat"; 
        doMeasurement();        
    }
    public void test_DIFF_EditorPopup() {   
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages"+"|";
        docName = "diff20kb.diff"; 
        doMeasurement();        
    }
    public void test_MANIFEST_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages"+"|";
        docName = "manifest20kb.mf";  
        doMeasurement();        
    }
    public void test_SH_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages"+"|";
        docName = "sh20kb.sh";            
        doMeasurement();        
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        return suite;
    }

    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }     

}
