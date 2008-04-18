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
package gui.actions;

import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;


import gui.window.MIDletEditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.ide.ProjectSupport;
import java.io.File;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Test Create Visual MIDlet
 *
 * @author  rashid@netbeans.org
 */
public class CreateVisualMIDlet extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private NewFileNameLocationStepOperator location;
    private static String testProjectName = "MobileApplicationVisualMIDlet";
    private String midletName;

    /**
     * Creates a new instance of CreateVisualMIDlet
     * @param testName the name of the test
     */
    public CreateVisualMIDlet(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 4000;
    }

    /**
     * Creates a new instance of CreateVisualMIDlet
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateVisualMIDlet(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 4000;
    }

    @Override
    public void initialize() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir") + File.separator + testProjectName);
        new CloseAllDocumentsAction().performAPI();
    }

    public void prepare() {

        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        pNode.select();

        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        wizard.selectCategory("MIDP"); //NOI18N
        wizard.selectFileType("Visual MIDlet"); //NOI18N
        wizard.next();

        new EventTool().waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        midletName = "VisualMIDlet_" + System.currentTimeMillis();
        location.txtObjectName().setText(midletName);
    }

    public ComponentOperator open() {
        location.finish();
        return null;
    }

    @Override
    public void close() {
        new Thread("Question dialog discarder") {

            @Override
            public void run() {
                try {
                    new JButtonOperator(new JDialogOperator("Question"), "Discard").push();
                } catch (Exception e) {
                    // There is no need to care about this exception as this dialog is optional 
                    e.printStackTrace();
                }
            }
        }.start();
        MIDletEditorOperator.findMIDletEditorOperator(midletName + ".java").close();
    }

    @Override
    protected void shutdown() {
        ProjectSupport.closeProject(testProjectName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateVisualMIDlet("measureTime"));
    }
}
