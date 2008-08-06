/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.test.j2ee.addmethod;

import java.io.IOException;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;

/**
 *
 * @author lm97939
 */
public class AddSelectMethodTest extends AddMethodTest {

    protected String ejbql = null;
    private String toSearchFile;

    /** Creates a new instance of AddMethodTest */
    public AddSelectMethodTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run only selected test case
        junit.textui.TestRunner.run(new AddSelectMethodTest("testAddSelectMethod1InEB"));
    }

    public void testAddSelectMethod1InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddSelectMethodAction");
        methodName = "ejbSelectByTest1";
        returnType = "int";
        parameters = null;
        ejbql = null;
        //toSearchFile = beanName+"LocalHome.java";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    public void testAddSelectMethod2InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddSelectMethodAction");
        methodName = "ejbSelectByTest2";
        returnType = "int";
        parameters = new String[][]{{"java.lang.String", "a"}};
        ejbql = null;
        //toSearchFile = beanName+"LocalHome.java";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    @Override
    protected void addMethod() throws IOException {
        EditorOperator editor = EditorWindowOperator.getEditor(beanName + "Bean.java");
        editor.select(11);

        // invoke Add Business Method dialog
        GenerateCodeOperator.openDialog(dialogTitle, editor);
        NbDialogOperator dialog = new NbDialogOperator(dialogTitle);
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(methodName);
        if (returnType != null) {
            new JTextFieldOperator(dialog, 1).setText(returnType);
        }
        fillParameters(dialog);
        if (ejbql != null) {
            new JTextAreaOperator(dialog).setText(ejbql);
        }
        dialog.ok();
        if (saveFile) {
            editor.save();
        }
        if (toSearchFile != null) {
            waitForEditorText(editor, methodName);
        }
        compareFiles();
    }
}
