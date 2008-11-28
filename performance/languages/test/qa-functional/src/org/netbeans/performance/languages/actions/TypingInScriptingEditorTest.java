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

package org.netbeans.performance.languages.actions;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Administrator
 */
public class TypingInScriptingEditorTest extends PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName; 
    protected String nodePath;
    private EditorOperator editorOperator;
    protected static ProjectsTabOperator projectsTab = null;
    private int caretBlinkRate;
    
    public TypingInScriptingEditorTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;       
        WAIT_AFTER_OPEN=200;
    }

    public TypingInScriptingEditorTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = UI_RESPONSE;       
        WAIT_AFTER_OPEN=200;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ScriptingSetup.class)
             .addTest(TypingInScriptingEditorTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize() {
        String path = nodePath+"|"+fileName;
        fileToBeOpened = new Node(getProjectNode(testProject),path);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        caretBlinkRate =  editorOperator.txtEditorPane().getCaret().getBlinkRate();
        editorOperator.txtEditorPane().getCaret().setBlinkRate(0);
    }
    
    @Override
    public void prepare() {
        editorOperator.setCaretPosition(8, 1);        
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
    }

    @Override
    public ComponentOperator open() {
        editorOperator.typeKey('z');        
        return null;
    }

    @Override
    public void close() {
    }

    public void shutdown() {
        editorOperator.txtEditorPane().getCaret().setBlinkRate(caretBlinkRate);
        repaintManager().resetRegionFilters();
        EditorOperator.closeDiscardAll();
    }

    protected Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = ScriptingUtilities.invokePTO();
        
        return projectsTab.getProjectRootNode(projectName);
    }
    
    public void test_RB_EditorTyping() {
        testProject = Projects.RUBY_PROJECT;
        fileName = "ruby20kb.rb";
        nodePath = "Source Files";
        doMeasurement();
    }

    public void test_RHTML_EditorTyping() {
        testProject = Projects.RAILS_PROJECT;
        fileName = "rhtml20kb.rhtml";
        nodePath = "Unit Tests";
        doMeasurement();
    }

    public void test_JScript_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "javascript20kb.js";
        nodePath = "Web Pages";
        doMeasurement();        
    }

    public void test_PHP_EditorTyping() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        doMeasurement();
    }
    
    public void test_JSON_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "json20kB.json";
        nodePath = "Web Pages";
        doMeasurement();
    }

    public void test_CSS_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "css20kB.css";
        nodePath = "Web Pages";
        doMeasurement();
    }

    public void test_YML_EditorTyping() {
        testProject = Projects.RAILS_PROJECT;
        fileName = "yaml20kB.yml";
        nodePath = "Unit Tests";
        doMeasurement();
    }

    public void test_BAT_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "bat20kB.bat";
        nodePath = "Web Pages";
        doMeasurement();
    }

    public void test_DIFF_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "diff20kB.diff";
        nodePath = "Web Pages";
        doMeasurement();
    }

    public void test_MANIFEST_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "manifest20kB.mf";
        nodePath = "Web Pages";
        doMeasurement();
    }

    public void test_SH_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "sh20kB.sh";
        nodePath = "Web Pages";
        doMeasurement();
    }
 
}
