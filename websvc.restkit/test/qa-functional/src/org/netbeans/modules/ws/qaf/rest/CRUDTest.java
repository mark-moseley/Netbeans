/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for New REST web services from Entity Classes wizard
 *
 * Duration of this test suite: aprox. 3min
 * 
 * @author lukas
 */
public class CRUDTest extends RestTestBase {

    private static final Logger LOGGER = Logger.getLogger(CRUDTest.class.getName());

    /** Default constructor.
     * @param testName name of particular test case
     */
    public CRUDTest(String name) {
        super(name);
    }

    protected String getProjectName() {
        return "FromEntities"; //NOI18N
    }

    protected String getRestPackage() {
        return "o.n.m.ws.qaf.rest.crud"; //NOI18N
    }

    /**
     * Create new web project with entity classes from sample database
     * (jdbc/sample), create new RESTful web services from created entities
     * and deploy the project
     */
    public void testRfE() {
        copyDBSchema();
        //Persistence
        String persistenceLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.ui.resources.Bundle", "Templates/Persistence");
        //Entity Classes from Database
        String fromDbLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "Templates/Persistence/RelatedCMP");
        createNewFile(getProject(), persistenceLabel, fromDbLabel);
        WizardOperator wo = prepareEntityClasses(new WizardOperator(fromDbLabel), true);
        //Finish
        //new JButtonOperator(wo, 7).pushNoBlock();
        //only finish the wizard
        wo.finish();
        String generationTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "TXT_EntityClassesGeneration");
        waitDialogClosed(generationTitle);
        new EventTool().waitNoEvent(1500);


        //RESTful Web Services from Entity Classes
        String restLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromEntities");
        createNewWSFile(getProject(), restLabel);
        wo = new WizardOperator(restLabel);
        //have to wait until "retrieving message dissapers (see also issue 122802)
        new EventTool().waitNoEvent(2500);
        //Add All >>
        String addAllLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_AddAll");
        new JButtonOperator(wo, addAllLabel).pushNoBlock();
        wo.next();
        //Resource Package
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 2);
        jcbo.clearText();
        jcbo.typeText(getRestPackage() + ".service"); //NOI18N
        //Converter Package
        jcbo = new JComboBoxOperator(wo, 1);
        jcbo.clearText();
        jcbo.typeText(getRestPackage() + ".converter"); //NOI18N
        wo.finish();
        waitGorGenerationProgress();
        Set<File> files = getFiles(getRestPackage() + ".service"); //NOI18N
        files.addAll(getFiles(getRestPackage() + ".converter")); //NOI18N
        assertEquals("Some files were not generated", 30, files.size()); //NOI18N
        checkFiles(files);
        //make sure all REST services nodes are visible in project log. view
        assertEquals("missing nodes?", 14, getRestNode().getChildren().length);
    }

    /**
     * Test creation of RESTful web service from an entity class which
     * uses property based access. Also tests functionality of the new RESTful
     * web service from entity classes wizard (buttons, updating model
     * in the wizard)
     */
    public void testPropAccess() throws IOException {
        //copy entity class into a project
        FileObject fo = FileUtil.toFileObject(new File(getRestDataDir(), "Person.java.gf")); //NOI18N
        FileObject targetDir = getProject().getProjectDirectory().getFileObject("src/java"); //NOI18N
        fo.copy(targetDir.createFolder("entity"), "Person", "java"); //NOI18N
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
        }
        //RESTful Web Services from Entity Classes
        String restLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromEntities");
        createNewWSFile(getProject(), restLabel);
        WizardOperator wo = new WizardOperator(restLabel);
        //have to wait until "retrieving message dissapers (see also issue 130835)
        new EventTool().waitNoEvent(2500);
        JListOperator availableEntities = new JListOperator(wo, 1);
        JListOperator selectedEntities = new JListOperator(wo, 2);

        //XXX - workaround for: http://www.netbeans.org/issues/show_bug.cgi?id=130835
        //Add All >>
        String addAllLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_AddAll");
        new JButtonOperator(wo, addAllLabel).push();
        //<< Remove All
        String removeAllLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RemoveAll");
        new JButtonOperator(wo, removeAllLabel).push();
        //XXX - end

        availableEntities.selectItem("Customer"); //NOI18N
        //Add >
        String addLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_Add");
        new JButtonOperator(wo, addLabel).push();
        assertEquals("add failed in selected", 6, selectedEntities.getModel().getSize()); //NOI18N
        assertEquals("add failed in available", 2, availableEntities.getModel().getSize()); //NOI18N
        selectedEntities.selectItem("Product"); //NOI18N
        //< Remove
        String removeLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_Remove");
        new JButtonOperator(wo, removeLabel).push();
        assertEquals("remove failed in selected", 5, selectedEntities.getModel().getSize()); //NOI18N
        assertEquals("remove failed in available", 3, availableEntities.getModel().getSize()); //NOI18N
//        //<< Remove All
//        String removeAllLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RemoveAll");
        new JButtonOperator(wo, removeAllLabel).push();
        assertEquals("remove all failed in selected", 0, selectedEntities.getModel().getSize()); //NOI18N
        assertEquals("remove all failed in available", 8, availableEntities.getModel().getSize()); //NOI18N
        availableEntities.selectItem("Person"); //NOI18N
        new JButtonOperator(wo, addLabel).push();
        assertEquals("add in selected", 1, selectedEntities.getModel().getSize()); //NOI18N
        assertEquals("add in available", 7, availableEntities.getModel().getSize()); //NOI18N
        wo.next();
        wo.finish();
        waitGorGenerationProgress();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
        }
        Set<File> files = getFiles("service"); //NOI18N
        files.addAll(getFiles("converter")); //NOI18N
        assertEquals("Some files were not generated", 6, files.size()); //NOI18N
        checkFiles(files);
        //make sure all REST services nodes are visible in project log. view
        assertEquals("missing nodes?", 16, getRestNode().getChildren().length); //NOI18N
    }

    public void testCreateRestClient() throws IOException {
        // not display browser on run
        // open project properties
        getProjectRootNode().properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Run" category
        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
        String displayBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
        new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
        // confirm properties dialog
        propertiesDialogOper.ok();
        String testRestActionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.projects.Bundle", "LBL_TestRestBeansAction_Name");
        getProjectRootNode().performPopupAction(testRestActionName);
    }

    protected void copyDBSchema() {
        //copy dbschema file to the project
        FileObject fo = FileUtil.toFileObject(new File(getRestDataDir(), "sampleDB.dbschema")); //NOI18N
        FileObject targetDir = getProject().getProjectDirectory().getFileObject("src/conf"); //NOI18N
        try {
            fo.copy(targetDir, fo.getName(), fo.getExt());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "db schema not copied", ex);
        }
    }

    /**
     * Go through given from DB wizard and return WizardOperator from the last panel
     * of the wizard
     *
     * @param wo wizard to go through
     * @return last step in the wizard
     */
    protected WizardOperator prepareEntityClasses(WizardOperator wo, boolean createPU) {
        //Add all >>
        String lbl = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_AddAll");
        JButtonOperator allLbl = new JButtonOperator(wo, lbl);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        allLbl.pushNoBlock();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        wo.next();
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 0);
        jcbo.clearText();
        jcbo.typeText(getRestPackage());
        if (createPU) {
            //Create persistence unit
            String btnLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.Bundle", "LBL_CreatePersistenceUnit");
            new JButtonOperator(wo, btnLabel).pushNoBlock();
            //Create Persistence Unit
            String puDlgTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "LBL_CreatePersistenceUnit");
            NbDialogOperator ndo = new NbDialogOperator(puDlgTitle);
            //Create
            btnLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.unit.Bundle", "LBL_Create");
            new JButtonOperator(ndo, btnLabel).pushNoBlock();
            //end create pu dialog
        }
        return wo;
    }

    protected Set<File> getFiles(String pkg) {
        Set<File> files = new HashSet<File>();
        File fo = FileUtil.toFile(getProject().getProjectDirectory());
        File pkgRoot = new File(fo, "src/java/" + pkg.replace('.', '/') + "/"); //NOI18N
        if (pkgRoot.listFiles() != null) {
            files.addAll(Arrays.asList(pkgRoot.listFiles()));
        }
        return files;
    }

    protected void waitGorGenerationProgress() {
        //Generating RESTful Web Services from Entity Classes
        String restGenTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RestSevicicesFromEntitiesProgress");
        waitDialogClosed(restGenTitle);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return NbModuleSuite.create(addServerTests(Server.GLASSFISH, NbModuleSuite.createConfiguration(CRUDTest.class),
                "testRfE", //NOI18N
                "testPropAccess", //NOI18N
                "testDeploy", //NOI18N
                "testCreateRestClient", //NOI18N
                "testUndeploy" //NOI18N
                ).enableModules(".*").clusters(".*")); //NOI18N
    }
}
