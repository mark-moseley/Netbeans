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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006Sun
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

package gui.action;

import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.performance.test.guitracker.LoggingRepaintManager;
import org.netbeans.test.web.performance.WebPerformanceTestCase;

/**
 * Test of java completion in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class JavaCompletionInJspEditor extends WebPerformanceTestCase {
    private String text;
    private EditorOperator editorOperator;    
    protected LoggingRepaintManager.RegionFilter COMPLETION_DIALOG_FILTER =
        new LoggingRepaintManager.RegionFilter() {
            public boolean accept(JComponent comp) {
                return comp.getClass().getName().startsWith("org.netbeans.editor.ext.");
            }

            public String getFilterName() {
                return "Completion Dialog Filter (accepts only componenets from" +
                        "'org.netbeans.editor.ext.**' packages";
            }
        };
    
    /** Creates a new instance of JavaCompletionInEditor */
    public JavaCompletionInJspEditor(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        init();
    }
    
    /** Creates a new instance of JavaCompletionInEditor */
    public JavaCompletionInJspEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        init();
    }
    
    protected void init() {
        super.init();
        WAIT_AFTER_OPEN = 6000;
    }
        
    public void testScriptletCC() {
        text = "<% ";
        measureTime();
    }
    
    public void testExpressionCC() {
        text = "<%= request.";
        measureTime();
    }
    
    public void testDeclarationCC() {
        text = "<%! java.";
        measureTime();
    }
    
    public void testAllTags() {
        text = "<";
        measureTime();
    }
    
    public void testTagAttribute1() {
        text = "<%@page ";
        measureTime();
    }
    
    public void testTagAttribute2() {
        text = "<jsp:useBean ";
        measureTime();
    }
    
    public void testAttributeValue1() {
        text = "<%@page import=\"";
        measureTime();
    }
    
    public void testAttributeValue2() {
        text = "<%@include file=\"";
        measureTime();
    }
    
    public void testAttributeValue3() {
        text = "<jsp:useBean id=\"bean\" scope=\"";
        measureTime();
    }
    
    public void testAttributeValue4() {
        text = "<jsp:useBean id=\"beanInstanceName\" scope=\"session\" class=\"";
        measureTime();
    }
    
    public void testAttributeValue5() {
        text = "<jsp:getProperty name=\"bean\" property=\"";
        measureTime();
    }
    
    public void testAttributeValue6() {
        text = "<%@taglib prefix=\"d\" tagdir=\"";
        measureTime();
    }
    
    protected void initialize() {
        jspOptions().setCaretBlinkRate(0);
        // delay between the caret stops and the update of his position in status bar
        jspOptions().setStatusBarCaretDelay(0);
//        jspOptions().setCodeFoldingEnable(false);
        jspOptions().setCompletionAutoPopupDelay(0);
        jspOptions().setJavaDocAutoPopup(false);
        javaOptions().setCompletionAutoPopupDelay(0);
        javaOptions().setJavaDocAutoPopup(false);
        // turn off the error hightlighting feature
        /* TODO doesn't work after retouche integration
        javaSettings().setParsingErrors(0);
        */ 
        
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().
            getProjectRootNode("TestWebProject"),"Web Pages|index.jsp"));
        editorOperator = EditorWindowOperator.getEditor("index.jsp");
        eventTool().waitNoEvent(1000);
        waitNoEvent(2000);
    }
    
    public void prepare() {
        // scroll to the place where we start
        editorOperator.makeComponentVisible();
        clearTestLine();
        editorOperator.setCaretPositionToLine(8);
        // insert the initial text
        editorOperator.insert(text);
        // wait
        eventTool().waitNoEvent(500);
    }
    
    public ComponentOperator open(){
        KeyStroke ctrlSpace = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK);
        repaintManager().addRegionFilter(COMPLETION_DIALOG_FILTER);
        // invoke the completion dialog
        new ActionNoBlock(null, null, ctrlSpace).perform(editorOperator);
        return null;
    }
    
    public void close() {
        //repaintManager().setRegionFilter(null);
         repaintManager().resetRegionFilters();
       
        new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)).
            perform(editorOperator);
        clearTestLine();
    }
    
    protected void shutdown() {
        super.shutdown();
        editorOperator.closeDiscard();
    }
    
    private void clearTestLine() {
        int linelength = editorOperator.getText(8).length();
        if (linelength > 1)
            editorOperator.delete(8,1,linelength-1);
    }
}