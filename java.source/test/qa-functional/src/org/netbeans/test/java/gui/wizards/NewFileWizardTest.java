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

package org.netbeans.test.java.gui.wizards;


import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.java.Utilities;
import org.netbeans.test.java.gui.GuiUtilities;
import java.io.*;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.java.Common;

/**
 * Tests the New File Wizard.
 * @author Roman Strobl
 */
public class NewFileWizardTest extends JellyTestCase {
    
    // default path to bundle file
    private static final String JAVA_BUNDLE_PATH = "org.netbeans.modules.java.project.Bundle";
    
    // default timeout for actions in miliseconds
    private static final int ACTION_TIMEOUT = 1000;
    
    // name of sample project
    private static final String TEST_PROJECT_NAME = "TestProject";
    
    // name of sample package
    private static final String TEST_PACKAGE_NAME = "test";
    
    // name of sample class
    private static final String TEST_CLASS_NAME = "TestClass";
    
    // name of invalid package
    private static final String TEST_PACKAGE_NAME_INVALID = "a/b";
    
    /**
     * error log
     */
    protected static PrintStream err;
    /**
     * standard log
     */
    protected static PrintStream log;
    
    // workdir, default /tmp, changed to NBJUnit workdir during test
    private String workDir = "/tmp";
    
    static String projectDir;
    
    /**
     * Adds tests into the test suite.
     * @return suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new NewFileWizardTest("testCreateProject"));
        // test requires an opened project
        suite.addTest(new NewFileWizardTest("testCreatePackage"));
        // test requires an opened project and created package
        suite.addTest(new NewFileWizardTest("testDeletePackage"));
        // test requires an opened project
        suite.addTest(new NewFileWizardTest("testDeleteProject"));
        suite.addTest(new NewFileWizardTest("testNewFileWizardComplex"));
        suite.addTest(new NewFileWizardTest("testCreatePackageFailure"));
        suite.addTest(new NewFileWizardTest("testCreateClass"));
        suite.addTest(new NewFileWizardTest("testCreateInterface"));
        suite.addTest(new NewFileWizardTest("testCreateAnnotation"));
        suite.addTest(new NewFileWizardTest("testCreateEnum"));
        suite.addTest(new NewFileWizardTest("testCreateException"));
        suite.addTest(new NewFileWizardTest("testCreateJApplet"));
        suite.addTest(new NewFileWizardTest("testCreateEmptyFile"));
        suite.addTest(new NewFileWizardTest("testCreateMainClass"));
        suite.addTest(new NewFileWizardTest("testCreatePackageInfo"));
        suite.addTest(new NewFileWizardTest("testInvalidName"));
        suite.addTest(new NewFileWizardTest("testExistingName"));
        
        return suite;
    }
    
    /**
     * Main method for standalone execution.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Sets up logging facilities.
     */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        err = getLog();
        log = getRef();
        JemmyProperties.getProperties().setOutput(new TestOut(null,
                new PrintWriter(err, true), new PrintWriter(err, false), null));
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        } catch (IOException e) { }
    }
    
    /**
     * Creates a new instance of Main
     * @param testName name of test
     */
    public NewFileWizardTest(String testName) {
        super(testName);
    }
    
    public void testCreateProject() {
        projectDir = GuiUtilities.createProject(TEST_PROJECT_NAME, workDir);
    }
    
    /**
     * Tests creating a project.
     */
    public void testCreateProject(String projectName) {
        projectDir = GuiUtilities.createProject(projectName, workDir);
    }
    
    public void testDeleteProject() {
        GuiUtilities.deleteProject(TEST_PROJECT_NAME, null, projectDir, false);
    }
    
    /**
     * Tests deleting a project including files on hard drive.
     */
    public void testDeleteProject(String projectName) {
        GuiUtilities.deleteProject(projectName, null, projectDir, false);
    }
    
    /**
     * Tests creating of a package.
     */
    public void testCreatePackage() {
        GuiUtilities.createPackage(TEST_PROJECT_NAME,TEST_PACKAGE_NAME);
    }
    
    public void testCreatePackage(String projName,String packName) {
        GuiUtilities.createPackage(projName,packName);
    }
    
    /**
     * Tests deleting of a package.
     */
    public void testDeletePackage() {
        // delete a package
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME);
        n.select();
        n.performPopupAction(org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.core.projects.Bundle", "LBL_action_delete"));
        
        // confirm
        new NbDialogOperator(Bundle.getString("org.openide.explorer.Bundle",
                "MSG_ConfirmDeleteObjectTitle")).yes();
        
    }
    
    /**
     * Tests New File wizard.
     * - create test project
     * - create test package
     * - create test class through New File wizard (core of the test)
     * - close opened file and project
     * - delete the project incl. all files on disc
     */
    public void testNewFileWizardComplex() {
        // create test project
        testCreateProject(TEST_PROJECT_NAME);
        
        // create test package
        testCreatePackage(TEST_PROJECT_NAME,TEST_PACKAGE_NAME);
        
        // select project node
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        // create test class
        NewFileWizardOperator op = NewFileWizardOperator.invoke();
        
        op.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes"));
        op.selectFileType(Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Class.java"));
        op.next();
        
        JTextFieldOperator tf = new JTextFieldOperator(op);
        tf.setText(TEST_CLASS_NAME);
        
        op.finish();
        
        // check generated source
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        String text = editor.getText();
        
        // check if class name is generated 4 times in the source code
        int oldIndex = 0;
        for (int i=0; i<2; i++) {
            oldIndex = text.indexOf(TEST_CLASS_NAME, oldIndex);
            if (oldIndex>-1) oldIndex++;
        }        
        assertTrue("Error in generated class "+TEST_CLASS_NAME+".java.",oldIndex!=-1);        
        editor.close();
        
        // delete test package
        testDeletePackage();
        
        // delete test project
        testDeleteProject(TEST_PROJECT_NAME);
        
    }
    
    /**
     * Negative test for creating of a package.
     */
    public void testCreatePackageFailure() {
        NewFileWizardOperator op = NewFileWizardOperator.invoke();
        
        // wait till all fields are loaded
        JDialogOperator jdo = new JDialogOperator(
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewFileWizard_Title"));
        JTreeOperator jto = new JTreeOperator(jdo, 0);
        boolean exitLoop = false;
        for (int i=0; i<10; i++) {
            for (int j=0; j<jto.getChildCount(jto.getRoot()); j++) {
                if (jto.getChild(jto.getRoot(), j).toString()==
                        Bundle.getString(JAVA_BUNDLE_PATH,
                        "Templates/Classes")) {
                    exitLoop = true;
                    break;
                }
            }
            if (exitLoop) break;
            Utilities.takeANap(1000);
        }
        
        // choose package
        op.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes"));
        op.selectFileType(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes/Package"));
        op.next();
        
        // try to set an invalid name
        JTextFieldOperator tfp = new JTextFieldOperator(op, 0);
        tfp.setText(TEST_PACKAGE_NAME_INVALID);
        //for (int i=0; i<10; i++) {
        //    JButtonOperator jbo = new JButtonOperator(op,
        //        Bundle.getString("org.openide.Bundle", "CTL_FINISH"));
        //    if (!jbo.isEnabled()) break;
        //    Utilities.takeANap(1000);
        //}
        Utilities.takeANap(1000);
        
        // check finish button
        //JButtonOperator jbo = new JButtonOperator(op,
        //        Bundle.getString("org.openide.Bundle", "CTL_FINISH"));
        
        //this should be replaced with line above
        JButtonOperator jbo = new JButtonOperator(op, "Finish");
        
        assertFalse("Finish button should be disabled for package with "
                +"invalid name.", jbo.isEnabled());
        
        new NbDialogOperator(Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewProjectWizard_Subtitle")+" "
                +Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes/Package")).cancel();
    }
    
    public void testCreateInterface() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyIface" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Interface.java"),
                Common.unify(expected),true);
        
    }
    
    public void testCreateAnnotation() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyAnnot" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/AnnotationType.java"),
                Common.unify(expected),true);
    }
    
    public void testCreateEnum() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyEnum" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Enum.java"),
                Common.unify(expected),true);
    }
    
    public void testCreateException() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyExp" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Exception.java"),
                Common.unify(expected),true);
    }
    
    
    public void testCreateJApplet() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyJApplet" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/JApplet.java"),
                Common.unify(expected),true);
    }
    
    public void testCreateEmptyFile() {
        String expected = getContentOfGoldenFile();
        createAndVerify("Empty" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Empty.java"),
                Common.unify(expected),true);
    }
    
    public void testCreateMainClass() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyMain" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Main.java"),
                Common.unify(expected),true);
    }
    
    public void testCreatePackageInfo() {
        String expected = getContentOfGoldenFile();
        createAndVerify("package-info" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/package-info.java"),
                Common.unify(expected),true);
    }
    
    public void testCreateClass() {
        String expected = getContentOfGoldenFile();
        createAndVerify("JavaClass" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Class.java"),
                Common.unify(expected),true);
    }
    
    
    
    public void testInvalidName() {
        String expected = "";
        createAndVerify("Name.invalid" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Class.java"),
                Common.unify(expected),false);
    }
    
    public void testExistingName() {
        String expected = "";
        createAndVerify("MyMain" ,
                Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes/Class.java"),
                Common.unify(expected),false);
    }
    
    private void createIfNotOpened(String projName,String packName) {
        Node pn = null;
        try {
            pn = new ProjectsTabOperator().getProjectRootNode(projName);
        } catch(TimeoutExpiredException tee) {
            System.out.println("Project is not opened, creating new one");
            testCreateProject(projName);
            pn = new ProjectsTabOperator().getProjectRootNode(projName);
        }
        Node n = null;
        try {
            n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                    "org.netbeans.modules.java.j2seproject.Bundle",
                    "NAME_src.dir")+"|"+packName);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Expected package not present, creating new one");
            testCreatePackage(projName,packName);
            n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                    "org.netbeans.modules.java.j2seproject.Bundle",
                    "NAME_src.dir")+"|"+packName);
        }
        n.select();
        
    }
    
    /**
     * Method for creation file from new file wizard
     *
     * @param name Name of new file
     * @param type String expression of new file type
     * @param expectedContent Expected content of the new file
     * @param shouldPass Indicated it there is expected error in the wizard
     */
    private void createAndVerify(String name, String type, String expectedContent, boolean shouldPass) {
        createIfNotOpened(TEST_PROJECT_NAME, TEST_PACKAGE_NAME);
        // select project node
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME);
        n.select();
        // create test class
        NewFileWizardOperator op = NewFileWizardOperator.invoke();
        
        op.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes"));
        op.selectFileType(type);
        op.next();
        JTextFieldOperator tf = new JTextFieldOperator(op);
        tf.setText(name);
        if(!shouldPass) {
            Utilities.takeANap(1000);
            JButtonOperator jbo = new JButtonOperator(op,"Finish");
            assertFalse("Finish button should be disabled", jbo.isEnabled());
            // closing wizard
            new NbDialogOperator(Bundle.getString(
                    "org.netbeans.modules.project.ui.Bundle",
                    "LBL_NewProjectWizard_Subtitle")+" "
                    +type).cancel();
            return;
        }
        op.finish();
        // check generated source
        EditorOperator editor = null;
        try {
            editor = new EditorOperator(name);
            String text = Common.unify(editor.getText());
            assertEquals("File doesnt have expected content",expectedContent,text);
        } finally {
            if(editor!=null) editor.close();
        }
        
        
    }
    
    /**
     *
     * @return
     */
    public String getContentOfGoldenFile() {
        try {
            File golden = getGoldenFile();
            BufferedReader br = new BufferedReader(new FileReader(golden));
            StringBuffer res = new StringBuffer();
            String line = "";
            while((line = br.readLine())!=null) {
                res.append(line);
                res.append("\n");
            }
            return res.toString();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
        return null;
    }
    
}
