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

package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.actions.ActionNoBlock
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class ActionNoBlockTest extends JellyTestCase {

    public static String[] tests = {
        "testPerformMenu",
        "testPerformPopupOnNodes",
        "testPerformPopupOnComponent",
        "testPerformAPI",
        "testPerformShortcut"
    };
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ActionNoBlockTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ActionNoBlockTest("testPerformMenu"));
        suite.addTest(new ActionNoBlockTest("testPerformPopupOnNodes"));
        suite.addTest(new ActionNoBlockTest("testPerformPopupOnComponent"));
        suite.addTest(new ActionNoBlockTest("testPerformAPI"));
        suite.addTest(new ActionNoBlockTest("testPerformShortcut"));
        return suite;
         */
        return createModuleTest(ActionNoBlockTest.class, tests);
    }
    
    public void setUp() throws IOException {
        openDataProjects("SampleProject");
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** simple test case
     */
    public void testPerformMenu() {
        /** File|New Project..." main menu path. */
        String menuPath = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/File")
                          + "|"
                          + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewProjectAction_Name");

        new ActionNoBlock(menuPath, null).perform();
        new NewProjectWizardOperator().close();
    }
    
    /** simple test case
     */
    public void testPerformPopupOnNodes() {
        SourcePackagesNode sourceNode = new SourcePackagesNode("SampleProject");
        Node nodes[] = {
            new Node(sourceNode, "sample1|SampleClass1.java"),  // NOI18N
            new Node(sourceNode, "sample1.sample2|SampleClass2.java")   // NOI18N
        };
        String deletePopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
        new ActionNoBlock(null, deletePopup).perform(nodes);
        // "Safe Delete"
        String safeDeleteTitle = Bundle.getString("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_SafeDel"); // NOI18N
        new NbDialogOperator(safeDeleteTitle).cancel();
        String openPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        new ActionNoBlock(null, openPopup).perform(nodes[0]);
    }
    
    /** simple test case
     */
    public void testPerformPopupOnComponent() {
        EditorOperator eo = new EditorOperator("SampleClass");// NOI18N
        // "New Watch..."
        String newWatchItem = Bundle.getString("org.netbeans.editor.Bundle", "add-watch");
        new ActionNoBlock(null, newWatchItem).perform(eo);
        // "New Watch"
        String newWatchTitle = Bundle.getString("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title");
        new NbDialogOperator(newWatchTitle).close();
        eo.closeDiscard();
    }
    
    /** simple test case
     */
    public void testPerformAPI() {
        new NewFileAction().performAPI();
        new NewFileWizardOperator().close();
    }
    
    /** simple test case
     */
    public void testPerformShortcut() {
        new ActionNoBlock(null, null, null, new Action.Shortcut(KeyEvent.VK_N, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK)).perform();
        new NewProjectWizardOperator().close();
        // On some linux it may happen autorepeat is activated and it 
        // opens dialog multiple times. So, we need to close all modal dialogs.
        // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
        closeAllModal();
    }
    
}
