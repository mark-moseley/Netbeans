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
package org.netbeans.modules.css.test;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator;
import static org.netbeans.modules.css.test.operator.StyleBuilderOperator.Panes.*;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator.FontPaneOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class TestIssues extends CSSTest {
    
    /** Creates new CSS Test */
    public TestIssues(String testName) {
        super(testName);
    }
    
    public void test105562(){/*move end bracket, 105574 end semicolon should be added*/
        String insertion = "h2{font-size: 10px}\n";
        EditorOperator eop = openFile(newFileName);
        eop.setCaretPositionToLine(1);
        eop.insert(insertion);
        eop.setCaretPositionToLine(1);
        StyleBuilderOperator styleOper= new StyleBuilderOperator().invokeBuilder();
        FontPaneOperator fontPane = (FontPaneOperator) styleOper.setPane(FONT);
        JListOperator fontFamilies = fontPane.fontFamilies();
        fontFamilies.selectItem(3);
        waitUpdate();
        String selected = fontFamilies.getSelectedValue().toString();
        String text = eop.getText();
        assertFalse("END BRACKET IS MOVED",text.contains(insertion));
        String rule = text.substring(0, text.indexOf('}'));
        assertTrue("SEMICOLON ADDED", rule.contains("font-size: 10px;"));
        assertTrue("FONT FAMILY SOULD BE GENERATED INSIDE RULE",rule.contains("font-family: "+selected));
        eop.closeDiscardAll();
    }     
    
    public void test105568(){
        String insertion = "h1{\ntext-decoration    : overline;\n}";
        EditorOperator eop = openFile(newFileName);
        eop.setCaretPositionToLine(1);
        eop.insert(insertion);
        eop.setCaretPositionToLine(1);
        StyleBuilderOperator styleOper= new StyleBuilderOperator();
        waitUpdate();
        FontPaneOperator fontPane = (FontPaneOperator) styleOper.setPane(FONT);
        assertTrue(fontPane.isOverline());
        eop.closeDiscardAll();
    }
    
}