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

package org.netbeans.qa.form.refactoring;

import java.io.IOException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.qa.form.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.actions.CompileAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;


/**
 * Tests form refactoring : Refactoring custom component name, custom code and package name
 *
 * @author Jiri Vagner
 */
public class RenamePackageComponentAndCustomCodeTest extends ExtJellyTestCase {
    private String FORM_NAME = "CustomComponentForm"; // NOI18N
    private String OLD_COMPONENT_NAME = "CustomButton"; // NOI18N    
    private String NEW_COMPONENT_NAME = OLD_COMPONENT_NAME + "Renamed"; // NOI18N    
    private String OLD_PACKAGE_NAME = "data.components"; // NOI18N
    private String NEW_PACKAGE_NAME = "data.renamedcomponents"; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public RenamePackageComponentAndCustomCodeTest(String testName) {
        super(testName);
    }
    
    /** 
     * Opening default project
     */
    public void setUp() throws IOException{
        openProject(_testProjectName);
        
    }
    
    /**
     * Creates suite from particular test cases.
     * @return nb test suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(RenamePackageComponentAndCustomCodeTest.class)
                .addTest("testRefactoringComponentName")
                .enableModules(".*").clusters(".*").gui(true));
    }

    /** Runs refactoring  */
    public void testRefactoringComponentName() {
        Node compNode = getProjectFileNode(OLD_COMPONENT_NAME, OLD_PACKAGE_NAME);

        // custom component rename
        runNoBlockPopupOverNode("Refactor|Rename...", compNode); // NOI18N
        JDialogOperator dialog = new JDialogOperator("Rename"); // NOI18N
        new JTextFieldOperator(dialog).typeText(NEW_COMPONENT_NAME);
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        dialog.waitClosed();
        
        // custom component package rename
        Node node = getProjectFileNode(OLD_PACKAGE_NAME, true);
        runNoBlockPopupOverNode("Refactor|Rename...", node); // NOI18N
        
        // rename dialog ...
        dialog = new JDialogOperator("Rename  " + OLD_PACKAGE_NAME); // NOI18N
        new JTextFieldOperator(dialog).typeText(NEW_PACKAGE_NAME);
        //new JButtonOperator(dialog,"OK").clickMouse(); // NOI18N
        
        // ... refactoring dialog
        //dialog = new JDialogOperator("Rename"); // NOI18N
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        dialog.waitClosed();
        
        // compiling component to avoid load form error
        compNode = getProjectFileNode(NEW_COMPONENT_NAME, NEW_PACKAGE_NAME);
        new CompileAction().perform(compNode);
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        openFile(FORM_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(FORM_NAME);
        
        ArrayList<String> lines = new ArrayList<String>();
        
        // custom components refatoring
        lines.add("customButton1 = new data.renamedcomponents.CustomButtonRenamed();"); // NOI18N

        // custom code refactoring
        lines.add("jButton1 = data.renamedcomponents.CustomButtonRenamed.createButton();"); // NOI18N

        // custom component field refactoring
        lines.add("private data.renamedcomponents.CustomButtonRenamed customButton1;"); // NOI18N
        
        findInCode(lines, designer);
    }
}    
