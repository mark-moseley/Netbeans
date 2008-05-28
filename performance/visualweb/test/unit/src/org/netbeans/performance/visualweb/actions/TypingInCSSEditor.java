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

import java.awt.Font;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class TypingInCSSEditor extends  org.netbeans.modules.performance.utilities.PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String fileName;    
    private EditorOperator editorOperator;
    private int caretBlinkRate;
    private Font font;
    
    /** Creates a new instance of TypingInCSSEditor */
    public TypingInCSSEditor(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;       
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations        
    }
    /** Creates a new instance of TypingInCSSEditor */
    public TypingInCSSEditor(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = UI_RESPONSE;  
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations        
    }
   
    public void testTypingInCSSEditor() {
        doMeasurement();
    }
    
    @Override
    protected void initialize() {
        fileName = "stylesheet.css";
        fileToBeOpened = new Node(new WebPagesNode("VisualWebProject"), "resources|" + fileName);                
    }
    
    public void testCSSEditor() {
        doMeasurement();
    }
    
    public void prepare() {
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        
        caretBlinkRate =  editorOperator.txtEditorPane().getCaret().getBlinkRate();

        editorOperator.txtEditorPane().getCaret().setBlinkRate(0);

        editorOperator.setCaretPosition(8, 1);        
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        waitNoEvent(2000);
        
    }

    public ComponentOperator open() {        
        editorOperator.typeKey('z');
        return null;
        
    }
    @Override
    public void close() {
        editorOperator.txtEditorPane().getCaret().setBlinkRate(caretBlinkRate);
        repaintManager().resetRegionFilters();        
        EditorOperator.closeDiscardAll();
        
    }
}
