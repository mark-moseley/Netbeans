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

package org.netbeans.performance.visualweb.actions;

import java.awt.event.KeyEvent;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

import org.netbeans.performance.visualweb.setup.VisualWebSetup;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 *
 * @author mkhramov@netbeans.org
 */

public class PasteCSSTextTest extends PerformanceTestCase {

    protected Node fileToBeOpened;
    protected Node textFileToOpen;
    
    protected String fileName; 
    protected String textFile;
    
    private EditorOperator editorOperator;
    private int caretBlinkRate;
    private static String CONFIG_NODE;

    
    /** Creates a new instance of PasteCSSText */
    public PasteCSSTextTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;       
        WAIT_AFTER_OPEN=200;
    }
    
    /** Creates a new instance of PasteCSSText */    
    public PasteCSSTextTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = UI_RESPONSE;        
        WAIT_AFTER_OPEN=200;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(VisualWebSetup.class)
             .addTest(PasteCSSTextTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testPasteCSSText() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        CONFIG_NODE = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_Config");
        openAndCopy();
        
        fileName = "stylesheet.css";
        fileToBeOpened = new Node(new WebPagesNode("UltraLargeWA"), "resources|" + fileName);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }
    
    @Override
    public void prepare() {
        openCSSFile();
        
        caretBlinkRate =  editorOperator.txtEditorPane().getCaret().getBlinkRate();
        editorOperator.txtEditorPane().getCaret().setBlinkRate(0);
        editorOperator.makeComponentVisible();
        editorOperator.setCaretPosition(8, 1);
    }

    @Override
    public ComponentOperator open() {
        pasteText();
        return null;
    }
    
    @Override
    public void close() {
        editorOperator.txtEditorPane().getCaret().setBlinkRate(caretBlinkRate);
        EditorOperator.closeDiscardAll();        
    }
    
    public void shutdown() {
        repaintManager().resetRegionFilters();
    }

    private void openCSSFile() {
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);        
    }
    
    private void pasteText() {
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_V, KeyEvent.CTRL_MASK)).perform(editorOperator);
    }
    
    private void openAndCopy() {
        textFile = "MANIFEST.MF";
        Node projectRoot = new ProjectsTabOperator().getProjectRootNode("UltraLargeWA");
        textFileToOpen = new Node(projectRoot,CONFIG_NODE +"|"+textFile);
        
        new OpenAction().performAPI(textFileToOpen);        
        EditorOperator textFileEditorOperator = EditorWindowOperator.getEditor(textFile);
        textFileEditorOperator.makeComponentVisible();
        textFileEditorOperator.setCaretPosition(1,1);
        textFileEditorOperator.select(1,1,16);
        
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_C, KeyEvent.CTRL_MASK)).perform(textFileEditorOperator);
        waitNoEvent(1000);
        
        textFileEditorOperator.closeDiscard();
    }

}
