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

package org.netbeans.performance.languages.setup;


import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingSetup extends JellyTestCase {
    public static final String suiteName="Scripting UI Responsiveness Setup suite";
    private String workdir;
    
    public ScriptingSetup(String testName) {
        super(testName);
        workdir = System.getProperty("nbjunit.workdir");
        try {
            workdir = new File(workdir + "/../../../../../../../nbextra/data/").getCanonicalPath();
        } catch (IOException ex) {
            System.err.println("Exception: "+ex);
        }        
    }

    public void testCloseWelcome() {
        CommonUtilities.closeWelcome();
    }

    public void testCloseAllDocuments() {
        CommonUtilities.closeAllDocuments();
    }

    public void testCloseMemoryToolbar() {
        CommonUtilities.closeMemoryToolbar();
    }

    public void testOpenRubyProject() {
        //ScriptingUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+ java.io.File.separator +Projects.RUBY_PROJECT);
        //ScriptingUtilities.waitForPendingBackgroundTasks();   
        openProject(Projects.RUBY_PROJECT);
    }
    public void testOpenRailsProject() {
        //ScriptingUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+ java.io.File.separator +Projects.RAILS_PROJECT);
        //ScriptingUtilities.waitForPendingBackgroundTasks();
        openProject(Projects.RAILS_PROJECT);
    }
    
    public void testOpenScriptingProject() {
        String projectPath = workdir+ java.io.File.separator +Projects.SCRIPTING_PROJECT;
        ProjectSupport.openProject(projectPath);
/*        closeAllModal();
        ScriptingUtilities.waitScanFinished();
        ScriptingUtilities.waitForPendingBackgroundTasks();
        closeAllModal();
        ScriptingUtilities.verifyAndResolveMissingWebServer(Projects.SCRIPTING_PROJECT, "GlassFish V2");
        ScriptingUtilities.waitForPendingBackgroundTasks();        */
    }

/* TODO create PHPProject
    public void testOpenPHPProject() {
        openProject(Projects.PHP_PROJECT);
    }
*/    
    private void openProject(String projectName) {
        String projectsDir = workdir + File.separator+projectName;
        Object prj=ProjectSupport.openProject(projectsDir);
        assertNotNull(prj);
        CommonUtilities.waitProjectTasksFinished();        
    }

}
