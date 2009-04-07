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
package org.netbeans.jellytools;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jemmy.JemmyProperties;

/**
 * Test of org.netbeans.jellytools.NewPHPProjectNameLocationStepOperator
 * @author mrkam@netbeans.org
 */
public class NewPHPProjectNameLocationStepOperatorTest extends JellyTestCase {
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(NewPHPProjectNameLocationStepOperatorTest.class);
    }
    
    @Override
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewPHPProjectNameLocationStepOperatorTest(String testName) {
        super(testName);
    }
    
    public void testPHPApplicationInQueueMode() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        doTest();
    }

    public void testPHPApplicationInRobotMode() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        doTest();
    }

    public void testPHPApplicationInSmoothRobotMode() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.SHORTCUT_MODEL_MASK);
        doTest();
    }

    public void testPHPApplicationInShortcutMode() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.SMOOTH_ROBOT_MODEL_MASK);
        doTest();
    }

    public void doTest() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory("PHP");
        // PHP Application
        String webApplicationLabel = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "Templates/Project/PHP/PHPProject.php");
        op.selectProject(webApplicationLabel);
        op.next();
        
        NewPHPProjectNameLocationStepOperator lsop = new NewPHPProjectNameLocationStepOperator();

        String project_name = "NewPHPProject";
        lsop.typeProjectName(project_name);
        assertEquals(project_name, lsop.getProjectName());

        lsop.browseSourceFolder();
        String selectSourceFolder = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_SelectSourceFolderTitle");
        new NbDialogOperator(selectSourceFolder).cancel(); // I18N
        
        String folder = lsop.getSelectedSourcesFolder() + File.separator + "test";
        lsop.typeSourcesFolder(folder);
        assertEquals(folder, lsop.getSelectedSourcesFolder());
        
        String encoding = NewPHPProjectNameLocationStepOperator.ENCODING_KOI8R;
        lsop.selectDefaultEncoding(encoding);
        assertEquals(encoding, lsop.getSelectedDefaultEncoding());
        
        lsop.checkPutNetBeansMetadataIntoASeparateDirectory(true);
        assertEquals(true, lsop.cbPutNetBeansMetadataIntoASeparateDirectory().isSelected());
        
        folder = lsop.getMetadataFolder() + File.separator + "test";
        lsop.setMetadataFolder(folder);
        assertEquals(folder, lsop.getMetadataFolder());

        lsop.browseMetadataFolder();

        String selectProjectFolder = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_SelectProjectFolder");
        new NbDialogOperator(selectProjectFolder).cancel(); // I18N
        
        lsop.cancel();
    }
}
