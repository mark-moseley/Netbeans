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

package org.netbeans.test.ide;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.PluginsOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jellytools.actions.CompileAction;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.CutAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jellytools.actions.PasteAction;
import org.netbeans.jellytools.actions.ViewAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugAction;
import org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction;
import org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.jemmy.util.PNGEncoder;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.xtest.plugin.ide.BlacklistedClassesHandler;

/**
 * Overall validation suite for IDE.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class IDEValidation extends JellyTestCase {
    
    /** Need to be defined because of JUnit */
    public IDEValidation(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new IDEValidation("testInitGCProjects"));
        suite.addTest(new IDEValidation("testMainMenu"));
        suite.addTest(new IDEValidation("testHelp"));
        suite.addTest(new IDEValidation("testOptions"));
        suite.addTest(new IDEValidation("testOptionsClassicView"));
        suite.addTest(new IDEValidation("testNewProject"));
        // sample project must exist before testShortcuts
        suite.addTest(new IDEValidation("testShortcuts"));
        suite.addTest(new IDEValidation("testNewFile"));
        suite.addTest(new IDEValidation("testCVSLite"));
        suite.addTest(new IDEValidation("testProjectsView"));
        suite.addTest(new IDEValidation("testFilesView"));
        suite.addTest(new IDEValidation("testEditor"));
        suite.addTest(new IDEValidation("testBuildAndRun"));
        suite.addTest(new IDEValidation("testDebugging"));
        suite.addTest(new IDEValidation("testJUnit"));
        suite.addTest(new IDEValidation("testXML"));
        suite.addTest(new IDEValidation("testDb"));
        suite.addTest(new IDEValidation("testWindowSystem"));
        suite.addTest(new IDEValidation("testPlugins"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new IDEValidation("testMainMenu"));
    }
    
    /** Setup called before every test case. */
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        // Close help window if any - it should not stay open between test cases.
        // Otherwise it can break next tests.
        closeHelpWindow();
    }
    
    /** Tear down called after every test case. */
    @Override
    public void tearDown() {
    }
    
    // name of sample project
    private static final String SAMPLE_PROJECT_NAME = "SampleProject"; // NOI18N
    // name of first sample package
    private static final String SAMPLE1_PACKAGE_NAME = "sample1"; //NOI18N
    // name of sample class
    private static final String SAMPLE1_CLASS_NAME = "SampleClass1"; // NOI18N
    // name of sample file
    private static final String SAMPLE1_FILE_NAME = SAMPLE1_CLASS_NAME+".java"; // NOI18N
    // name of sample class 2
    private static final String SAMPLE2_CLASS_NAME = "SampleClass2"; // NOI18N
    // name of sample file 2
    private static final String SAMPLE2_FILE_NAME = SAMPLE2_CLASS_NAME+".java"; // NOI18N

    /** Test creation of java project. 
     * - open New Project wizard from main menu (File|New Project)
     * - select Java Application project from Standard category
     * - in the next panel type project name and project location in
     * - finish the wizard
     * - wait until project appears in projects view
     * - wait classpath scanning finished
     */
    public void testNewProject() {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Standard"
        String standardLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
        npwo.selectCategory(standardLabel);
        // "Java Application"
        String javaApplicationLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/emptyJ2SE.xml");
        npwo.selectProject(javaApplicationLabel);
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.btFinish().pushNoBlock();
        npnlso.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        npnlso.waitClosed();
        // Opening Projects
        String openingProjectsTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_Opening_Projects_Progress");
        try {
            // wait at most 120 second until progress dialog dismiss
            NbDialogOperator openingOper = new NbDialogOperator(openingProjectsTitle);
            openingOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
            openingOper.waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
        // wait project appear in projects view
        new ProjectsTabOperator().getProjectRootNode(SAMPLE_PROJECT_NAME);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
    }
    
    /** Test new file wizard. 
     * - open New File wizard from main menu (File|New File)
     * - select sample project as target
     * - select Java Classes|Java Package file type
     * - in the next panel type package name in
     * - finish the wizard
     * - open New File wizard from context menu on created package node (New|File)
     * - select Java Classes|Java Main Class file type
     * - in the next panel type class name in
     * - finish the wizard
     * - check class is open in editor and close all opened documents
     */
    public void testNewFile() {
        // create a new package
        // "Java Classes"
        String javaClassesLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes");
        // "Java Package"
        String packageLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Package");
        NewFileWizardOperator.create(SAMPLE_PROJECT_NAME, javaClassesLabel, packageLabel, null, SAMPLE1_PACKAGE_NAME);
        // wait package node is created
        Node sample1Node = new Node(new SourcePackagesNode(SAMPLE_PROJECT_NAME), SAMPLE1_PACKAGE_NAME);
        
        // create a new classes
        
        // "Java Main Class"
        String mainClassLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Main.java"); // NOI18N
        NewFileWizardOperator.invoke(sample1Node, javaClassesLabel, mainClassLabel);
        NewFileNameLocationStepOperator nameStepOper = new NewFileNameLocationStepOperator();
        nameStepOper.setObjectName(SAMPLE1_CLASS_NAME);
        nameStepOper.finish();
        // check class is opened in Editor
        new EditorOperator(SAMPLE1_FILE_NAME);
        NewFileWizardOperator.invoke(sample1Node, javaClassesLabel, mainClassLabel);
        nameStepOper = new NewFileNameLocationStepOperator();
        nameStepOper.setObjectName(SAMPLE2_CLASS_NAME);
        nameStepOper.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator(SAMPLE2_FILE_NAME).closeAllDocuments();
    }
    
    /** Test Projects view 
     * - expand source hierarchy and find sample class (SampleClass1.java)
     * - copy sample class and paste it to the same package
     * - confirm refactoring dialog
     * - verify creation of NewClass.java node
     * - cut NewClass.java
     * - paste it to another package
     * - confirm refactoring dialog
     * - delete NewClass.java node
     */
    public void testProjectsView() {
        ProjectsTabOperator.invoke();
        // needed for slower machines
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); // NOI18N
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(SAMPLE_PROJECT_NAME);
        Node sample1Node = new Node(sourcePackagesNode, SAMPLE1_PACKAGE_NAME);
        Node sampleClass1Node = new Node(sample1Node, SAMPLE1_FILE_NAME);
        // test pop-up menu actions
        // "Copy"
        CopyAction copyAction = new CopyAction();
        copyAction.perform(sampleClass1Node);
        // "Paste"
        PasteAction pasteAction = new PasteAction();
        // "Refactor"
        String refactorItem = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_Action");
        // "Copy..."
        String copyItem = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_CopyAction");
        new ActionNoBlock(null, pasteAction.getPopupPath()+"|"+refactorItem+" "+copyItem).perform(sample1Node);
        
        String copyClassTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_CopyClass");
        NbDialogOperator copyClassDialog = new NbDialogOperator(copyClassTitle);
        // "Refactor"
        String refactorLabel = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Finish");
        new JButtonOperator(copyClassDialog, refactorLabel).push();
        
        Node newClassNode = new Node(sample1Node, "SampleClass11"); // NOI18N
        newClassNode.select();
        // "Cut"
        CutAction cutAction = new CutAction();
        cutAction.perform(newClassNode);
        // package created by default when the sample project was created
        Node sampleProjectPackage = new Node(sourcePackagesNode, SAMPLE_PROJECT_NAME.toLowerCase());
        // "Move..."
        String moveItem = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_MoveAction");
        new ActionNoBlock(null, pasteAction.getPopupPath()+"|"+refactorItem+" "+moveItem).perform(sampleProjectPackage);
        // confirm refactoring
        // "Move Class"
        String moveClassTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_MoveClass");
        NbDialogOperator moveClassDialog = new NbDialogOperator(moveClassTitle);
        new JButtonOperator(moveClassDialog, refactorLabel).push();
        // "Delete"
        newClassNode = new Node(sampleProjectPackage, "SampleClass11"); // NOI18N
        new DeleteAction().perform(newClassNode);
        // "Safe Delete"
        String safeDeleteTitle = Bundle.getString("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_SafeDel"); // NOI18N
        NbDialogOperator safeDeleteOper = new NbDialogOperator(safeDeleteTitle);
        try {
            safeDeleteOper.ok();
        } catch (TimeoutExpiredException e) {
            // It is "classpath scanning in progress" dialog, wait until it dismiss,
            // and then wait for regular Safe Delete dialog
            safeDeleteOper.waitClosed();
            safeDeleteOper = new NbDialogOperator(safeDeleteTitle);
            safeDeleteOper.ok();
        }
        safeDeleteOper.waitClosed();
    }

    /** Test Files view 
     * - expand files hierarchy and find sample class (SampleClass1.java) 
     * and select main method node.
     */
    public void testFilesView() {
        FilesTabOperator filesTabOper = FilesTabOperator.invoke();
        // needed for slower machines
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); // NOI18N
        Node sourcePackagesNode = new Node(filesTabOper.getProjectNode(SAMPLE_PROJECT_NAME), "src"); // NOI18N
        Node sample1Node = new Node(sourcePackagesNode, SAMPLE1_PACKAGE_NAME); // NOI18N
        Node sampleClass1Node = new Node(sample1Node, SAMPLE1_FILE_NAME);
        // It is possible to test also pop-up menu actions as in testProjectsView, but
        // it is redundant IMO
    }

    
    /** Test of DB module.
     * It only tests whether the Databases node is present in Runtime view, 
     * Add Driver action is enabled on Drivers node and Connect action is available
     * on default JDBC-ODBC Bridge node.
     * - find Databases|Drivers node in Runtime tab
     * - open and close Add Driver dialog from context menu on Drivers node
     * - open and close Connect Using dialog on JDBC-ODBC Bridge node
     */
    public void testDb() {
        // "Databases"
        String databasesLabel = Bundle.getString("org.netbeans.modules.db.resources.Bundle", "NDN_Databases");
        Node databasesNode = new Node(RuntimeTabOperator.invoke().getRootNode(), databasesLabel);
        // "Please wait..."
        String waitNodeLabel = Bundle.getString("org.netbeans.modules.db.resources.Bundle", "WaitNode");
        // wait until the wait node dismiss and after that start waiting for Drivers node
        // (see issue http://www.netbeans.org/issues/show_bug.cgi?id=43910 - Creation of 
        // children under Databases node is not properly synchronized)
        try {
            databasesNode.waitChildNotPresent(waitNodeLabel);
        } catch (JemmyException e) {
            // Ignore and try to continue. Sometimes it happens "Please, wait" node
            // is still available (maybe some threading issue).
            log("Timeout expired: "+e.getMessage());
        }
        // "Drivers"
        String driversLabel = Bundle.getString("org.netbeans.modules.db.resources.Bundle", "NDN_Drivers");
        Node driversNode = new Node(RuntimeTabOperator.invoke().getRootNode(), databasesLabel+"|"+driversLabel);
        // "Add Driver ..."
        String addDriverItem = Bundle.getString("org.netbeans.modules.db.resources.Bundle", "AddNewDriver");
        // open a dialog to add a new JDBC driver
        new ActionNoBlock(null, addDriverItem).perform(driversNode);
        String addDriverTitle = Bundle.getString("org.netbeans.modules.db.resources.Bundle", "AddDriverDialogTitle");
        new NbDialogOperator(addDriverTitle).cancel();
        
        // wait until the wait node dismiss and after that start waiting for JDBC_ODBC Bridge node
        // (see issue http://www.netbeans.org/issues/show_bug.cgi?id=43910 - Creation of 
        // children under Databases node is not properly synchronized)
        try {
            driversNode.waitChildNotPresent(waitNodeLabel);
        } catch (JemmyException e) {
            // Ignore and try to continue. Sometimes it happens "Please, wait" node
            // is still available (maybe some threading issue).
            log("Timeout expired: "+e.getMessage());
        }
        if(System.getProperty("os.name").toLowerCase().indexOf("mac") == -1) { // NOI18N
            // node JDBC-ODBC Bridge should be present always but not on mac
            Node jdbcOdbcNode = new Node(driversNode, "JDBC-ODBC Bridge"); // NOI18N
            // "Connect Using ..."
            String connectUsingItem = Bundle.getString("org.netbeans.modules.db.resources.Bundle", "ConnectUsing");
            // open a dialog to create a new connection
            new ActionNoBlock(null, connectUsingItem).perform(jdbcOdbcNode);
            String newDatabaseConnectionTitle = Bundle.getString("org.netbeans.modules.db.resources.Bundle", "NewConnectionDialogTitle");
            new NbDialogOperator(newDatabaseConnectionTitle).cancel();
        }
    }
    
    /** Test Help 
     * - open Help window from main menu (Help|Help Contents)
     */
    public void testHelp() {
        // increasing time because opening of help window can last longer on slower machines
        JemmyProperties.setCurrentTimeout("JMenuOperator.PushMenuTimeout", 60000);
        // open "Help|Contents"
        HelpOperator helpOper = HelpOperator.invoke();
        // check help window opened
        // title is "Help - All"
        helpOper.close();
    }
    
   /** Test Main Menu 
     * - close Welcome screen to not harm menu actions
     * - open and close New Project wizard (main menu item File|New Project...)
     * - open and close Javadoc Index Search top component (main menu item Tools|Javadoc Index Search)
     */
    public void testMainMenu() {
        // close Welcome screen to not harm menu actions
        // "Welcome
        String welcomeTitle = Bundle.getString("org.netbeans.modules.welcome.Bundle", "LBL_Tab_Title");
        new TopComponentOperator(welcomeTitle).close();
        // open and close New Project wizard
        int oldDispatchingModel = JemmyProperties.getCurrentDispatchingModel();
        try {
            NewProjectWizardOperator.invoke().close();
        } catch (TimeoutExpiredException e) {
            // if not succed try it second time in Robot mode
            // push Escape key to ensure there is no open menu
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
            NewProjectWizardOperator.invoke().close();
        } finally {
            // set previous dispatching model
            JemmyProperties.setCurrentDispatchingModel(oldDispatchingModel);
        }
        /*
        // open Tools|Javadoc Index Search
        String toolsItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools"); // NOI18N
        String javadocItem = Bundle.getStringTrimmed("org.netbeans.modules.javadoc.search.Bundle", "CTL_SEARCH_MenuItem");
        new Action(toolsItem+"|"+javadocItem, null).perform();
        // "Javadoc Index Search"
        String javadocTitle = Bundle.getString("org.netbeans.modules.javadoc.search.Bundle", "CTL_SEARCH_WindowTitle");
        new TopComponentOperator(javadocTitle).close();
         */
    }
    
    /** Test global shortcuts. 
     * - open and close new file wizard (CTRL+N)
     * - open and close Javadoc Index Search top component (Shift+F1)
     * - open and close new breakpoint dialog (Ctrl+Shift+F8)
     */
    public void testShortcuts() {
        // test global shortcuts
        // open new wizard (Ctrl+N)
        Node node = new SourcePackagesNode(SAMPLE_PROJECT_NAME);
        // push Escape key to ensure there is no thing blocking shortcut execution
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        NewFileAction newFileAction = new NewFileAction();
        try {
            newFileAction.performShortcut(node);
            new NewFileWizardOperator().close();
            // On some linux it may happen autorepeat is activated and it 
            // opens dialog multiple times. So, we need to close all modal dialogs.
            // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
            closeAllModal();
        } catch (TimeoutExpiredException e) {
            // need to be realiable test => repeat action once more to be sure it is problem in IDE
            // this time use events instead of Robot
            node.select();
            MainWindowOperator.getDefault().pushKey(
                    newFileAction.getKeyStrokes()[0].getKeyCode(),
                    newFileAction.getKeyStrokes()[0].getModifiers());
            new NewFileWizardOperator().close();
        }
        // open Javadoc Index Search (Shift+F1)
        // "Javadoc Index Search"
        String javadocTitle = Bundle.getString("org.netbeans.modules.javadoc.search.Bundle", 
                                               "CTL_SEARCH_WindowTitle");
        Action searchAction = new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.SHIFT_MASK));
        try {
            searchAction.perform(MainWindowOperator.getDefault());
            new TopComponentOperator(javadocTitle).close();
        } catch (TimeoutExpiredException e) {
            // need to be realiable test => repeat action once more to be sure it is problem in IDE
            // this time use events instead of Robot
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_F1, KeyEvent.SHIFT_MASK);
            new TopComponentOperator(javadocTitle).close();
        }
        // open new breakpoint dialog (Ctrl+Shift+F8)
        String newBreakpointTitle = Bundle.getString("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Breakpoint_Title");
        NewBreakpointAction newBreakpointAction = new NewBreakpointAction();
        try {
            newBreakpointAction.performShortcut(MainWindowOperator.getDefault());
            new NbDialogOperator(newBreakpointTitle).close();
            // On some linux it may happen autorepeat is activated and it 
            // opens dialog multiple times. So, we need to close all modal dialogs.
            // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
            closeAllModal();
        } catch (TimeoutExpiredException e) {
            // need to be realiable test => repeat action once more to be sure it is problem in IDE
            // this time use events instead of Robot
            MainWindowOperator.getDefault().pushKey(
                    newBreakpointAction.getKeyStrokes()[0].getKeyCode(),
                    newBreakpointAction.getKeyStrokes()[0].getModifiers());
            new NbDialogOperator(newBreakpointTitle).close();
        }
    }
    
    /** Test Source Editor
     * - opens sample class in Editor (context menu Open on the node)
     * - type abbreviation 'sout' into main method and then 'Hello'
     * - verify it is written 'System.out.println("Hello");'
     * - select the text and call copy from editor's context menu
     * - insert dummy text at next line , select it and paste the text in the clipboard
     * - select second 'Hello' and delete it by context menu
     * - insert 'Good bye' instead
     * - select fourth line, cut it and paste it at line 3
     */
    public void testEditor() {
        // open sample file in Editor
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(SAMPLE_PROJECT_NAME);
        Node sample1Node = new Node(sourcePackagesNode, SAMPLE1_PACKAGE_NAME);
        JavaNode sampleClass1Node = new JavaNode(sample1Node, SAMPLE1_FILE_NAME);
        sampleClass1Node.open();
        // find open file in editor
        EditorOperator eo = new EditorOperator(SAMPLE1_FILE_NAME);
        eo.setCaretPosition("public static void main", true);
        int insertLine = eo.getLineNumber()+2;
        eo.insert("\n", insertLine, 1); // NOI18N
        // Need to disable verification because shortcut "sout" is replaced
        // by "System.out.println("");" and "sout" is not found in Editor
        eo.setCaretPositionToLine(insertLine);
        eo.txtEditorPane().setVerification(false);
        eo.txtEditorPane().typeText("sout"); // NOI18N
        eo.txtEditorPane().typeKey('\t');
        eo.txtEditorPane().setVerification(true);
        eo.insert("Hello"); // NOI18N
        //eo.insert("System.out.println(\"Hello\");\n", insertLine+1, 1); // NOI18N
        final String textToCopy = "System.out.println(\"Hello\");"; // NOI18N
        eo.select(textToCopy);
        int oldDispatchingModel = JemmyProperties.getCurrentDispatchingModel();
        // "Copy"
        CopyAction copyAction = new CopyAction();
        try {
            copyAction.perform(eo);
        } catch (TimeoutExpiredException e) {
            // if not succed try it second time in Robot mode
            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
            copyAction.perform(eo);
        } finally {
            // set previous dispatching model
            JemmyProperties.setCurrentDispatchingModel(oldDispatchingModel);
        }
        // wait until clipboard contains text to copy
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    try {
                        String text = Toolkit.getDefaultToolkit().getSystemClipboard().
                                        getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
                        return textToCopy.equals(text) ? Boolean.TRUE:null;
                    } catch (UnsupportedFlavorException e) {
                        // The following exception can be thrown when clipboard is empty.
                        // java.awt.datatransfer.UnsupportedFlavorException: Unicode String
                        // at org.openide.util.datatransfer.ExTransferable$Empty.getTransferData(ExTransferable.java:461)
                        // Ignore this exception.
                        return null;
                    } catch (IOException ioe) {
                        throw new JemmyException("Failed getting clipboard content.", ioe);
                    }
                }
                public String getDescription() {
                    return("Clipboard contains "+textToCopy); // NOI18N
                }
            }).waitAction(null);
        } catch (Exception ie) {
            throw new JemmyException("Interrupted.", ie);
        }
        eo.insert("int xxxx;\n", insertLine+1, 1); // NOI18N
        eo.select("int xxxx;"); // NOI18N
        PasteAction pasteAction = new PasteAction();
        try {
            pasteAction.perform(eo);
        } catch (TimeoutExpiredException e) {
            // if not succed try it second time in Robot mode
            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
            pasteAction.perform(eo);
        } finally {
            // set previous dispatching model
            JemmyProperties.setCurrentDispatchingModel(oldDispatchingModel);
        }
        eo.select("Hello", 1); // NOI18N
        // "Delete"
        DeleteAction deleteAction = new DeleteAction();
        deleteAction.performMenu(eo);
        // wait Hello is deleted
        eo.txtEditorPane().waitText("System.out.println(\"\");"); // NOI18N
        eo.insert("Good bye"); // NOI18N
        // test cut action
        eo.select(3);
        // "Cut"
        CutAction cutAction = new CutAction();
        try {
            cutAction.perform(eo);
        } catch (TimeoutExpiredException e) {
            // if not succed try it second time in Robot mode
            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
            cutAction.perform(eo);
        } finally {
            // set previous dispatching model
            JemmyProperties.setCurrentDispatchingModel(oldDispatchingModel);
        }
        // need to wait a little until editor content is refreshed after cut action
        new EventTool().waitNoEvent(500);
        // select from column 1 to 2 at line 3 
        eo.select(2, 1, 2);
        try {
            pasteAction.perform(eo);
        } catch (TimeoutExpiredException e) {
            // if not succed try it second time in Robot mode
            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
            pasteAction.perform(eo);
        } finally {
            // set previous dispatching model
            JemmyProperties.setCurrentDispatchingModel(oldDispatchingModel);
        }
    }
    
    /** Test build and run.
     * - select sample class node and call "Build|Compile "SampleClass1.java"" main menu item
     * - wait until compilation finishes (track status bar)
     * - select sample class node and call "Run|Run File|Run "SampleClass1.java"" main menu item
     * - wait until run finishes
     * - from context menu set sample project as main project
     * - call "Build|Build Main Project" main menu item
     * - wait until build finishes
     * - call "Run|Run Main Project" main menu item
     * - wait until run finishes
     */
    public void testBuildAndRun() {
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(SAMPLE_PROJECT_NAME);
        Node sample1Node = new Node(sourcePackagesNode, SAMPLE1_PACKAGE_NAME);
        JavaNode sampleClass1Node = new JavaNode(sample1Node, SAMPLE1_FILE_NAME);
        // increase timeout to 60 seconds
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        // start to track Main Window status bar
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        // call Build|Compile main menu item
        new CompileAction().perform(sampleClass1Node);
        // "SampleProject (compile-single)"
        String compileSingleTarget = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "TITLE_output_target",
                new Object[] {SAMPLE_PROJECT_NAME, null, "compile-single"});  // NOI18N
        // "Finished building SampleProject (compile-single)"
        String finishedCompileSingleLabel = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "FMT_finished_target_status",
                new String[] {compileSingleTarget});
        // wait message "Finished building SampleProject (compile-single)"
        stt.waitText(finishedCompileSingleLabel);
        
        // "Run" 
        String runItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
        // "Run File"
        String runOtherItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject/RunOther");
        // "Run File"
        String runFileItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", 
                                                     "LBL_RunSingleAction_Name",
                                                     new Object[] {new Integer(1), SAMPLE1_FILE_NAME});
        // call "Run|Run File|Run "SampleClass1.java""
        new Action(runItem+"|"+runOtherItem+"|"+runFileItem, null).perform(sampleClass1Node);
        // "SampleProject (run-single)"
        String runSingleTarget = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "TITLE_output_target",
                new Object[] {SAMPLE_PROJECT_NAME, null, "run-single"});  // NOI18N
        // "Finished building SampleProject (run-single)"
        String finishedRunSingleLabel = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "FMT_finished_target_status",
                new String[] {runSingleTarget});
        // wait message "Finished building SampleProject (run-single)"
        stt.waitText(finishedRunSingleLabel); // NOI18N
        // check Hello and Good bye was printed out to the output window

        OutputTabOperator outputOper = new OutputTabOperator("run-single"); //NOI18N
        outputOper.waitText("Hello"); //NOI18N
        outputOper.waitText("Good bye"); //NOI18N
        
        // "Set as Main Project"
        String setAsMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetAsMainProjectAction_Name");
        new Action(null, setAsMainProjectItem).perform(new ProjectsTabOperator().getProjectRootNode(SAMPLE_PROJECT_NAME));
        // "Build"
        String buildItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Build");
        // "Build Main Project"
        String buildMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_BuildMainProjectAction_Name");
        // call "Build|Build Main Project" main menu item
        new Action(buildItem+"|"+buildMainProjectItem, null).perform();
        // "SampleProject (jar)"
        String jarTarget = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "TITLE_output_target",
                new Object[] {SAMPLE_PROJECT_NAME, null, "jar"});  // NOI18N
        // "Finished building SampleProject (jar)"
        String finishedJarLabel = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "FMT_finished_target_status",
                new String[] {jarTarget});
        // wait message "Finished building SampleProject (jar)"
        stt.waitText(finishedJarLabel);
        
        // Run Main Project
        String runMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_RunMainProjectAction_Name");
        // call "Run|Run Main Project" main menu item
        new Action(runItem+"|"+runMainProjectItem, null).perform();
        // "SampleProject (run)"
        String runTarget = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "TITLE_output_target",
                new Object[] {SAMPLE_PROJECT_NAME, null, "run"});  // NOI18N
        // "Finished building SampleProject (run)"
        String finishedRunLabel = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "FMT_finished_target_status",
                new String[] {runTarget});
        // wait message "Finished building SampleProject (run)"
        stt.waitText(finishedRunLabel);

        stt.stop();
    }
    
    /** Test JUnit support
     * - add methods to sample class
     * - from context menu on sample class node call "Tools|Create JUnit Tests" item
     * - select "JUnit 3.x" in "Select JUnit Version" dialog
     * - click "Select" button to confirm dialog
     * - confirm Create Tests dialog
     * - find generated test under "Test Packages"
     * - check whether test was open in editor and if includes test of public, 
     * protected, default but not private methods
     * - close the test
     * - select sample class
     * - call "Navigate|Go to Test" main menu item
     * - check test class is opened in editor
     * - run single test from main menu "Run|Run File|Test SampleClass2.java"
     * - check status bar that test was executed
     * - run test project from main menu "Run|Test SampleProject"
     * - check status bar that test was executed
     * - from context menu on sample package call "Tools|Create JUnit Tests" item
     * - confirm Create Tests dialog
     * - find generated suite under "Test Packages"
     * - open the suite class and check if contains generated test class
     * - close all documents in editor
     */
    public void testJUnit() throws InterruptedException {
        // open sample file in Editor
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(SAMPLE_PROJECT_NAME);
        Node sample1Node = new Node(sourcePackagesNode, SAMPLE1_PACKAGE_NAME);
        JavaNode sampleClass2Node = new JavaNode(sample1Node, SAMPLE2_FILE_NAME);
        sampleClass2Node.open();
        // find open sample file in editor
        EditorOperator eo = new EditorOperator(SAMPLE2_FILE_NAME);
        eo.setCaretPosition("public static void main", true);
        int insertLine = eo.getLineNumber()+3;
        // add methods declarations to sample file
        String publicMethod = "\n    public void publicMethod() {\n    }\n";  // NOI18N
        eo.insert(publicMethod, insertLine, 1);
        String privateMethod = "\n    private void privateMethod() {\n    }\n";  //NOI18N
        eo.insert(privateMethod, insertLine+3, 1);
        String protectedMethod = "\n    protected void protectedMethod() {\n    }\n";  //NOI18N
        eo.insert(protectedMethod, insertLine+3, 1);
        String defaultMethod = "\n    void defaultMethod() {\n    }\n";  //NOI18N
        eo.insert(defaultMethod, insertLine+3, 1);
        eo.save();
        // need to wait until new methods are parsed
        ProjectSupport.waitScanFinished();

        // "Tools"
        String toolsItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools"); // NOI18N
        // "Create JUnit Tests"
        String createTestsItem = Bundle.getString("org.netbeans.modules.junit.Bundle", "LBL_Action_CreateTest"); // NOI18N
        ActionNoBlock createTestsAction = new ActionNoBlock(null, toolsItem+"|"+createTestsItem);
        createTestsAction.perform(sampleClass2Node);
        // "Select JUnit Version"
        String selectJUnitVersionTitle = Bundle.getString("org.netbeans.modules.junit.Bundle", "LBL_title_select_generator");
        NbDialogOperator selectVersionOper = new NbDialogOperator(selectJUnitVersionTitle);
        // "JUnit 3.x"
        String version3Label = Bundle.getStringTrimmed("org.netbeans.modules.junit.Bundle", "LBL_JUnit3_generator");
        new JRadioButtonOperator(selectVersionOper, version3Label).push();
        // "Select"
        String selectLabel = Bundle.getStringTrimmed("org.netbeans.modules.junit.Bundle", "LBL_Select");
        new JButtonOperator(selectVersionOper, selectLabel).pushNoBlock();
        // "Create Tests"
        String createTestsTitle = Bundle.getString("org.netbeans.modules.junit.Bundle", "JUnitCfgOfCreate.Title");
        new NbDialogOperator(createTestsTitle).ok();
        
        // wait until test node is created
        // "Test Packages"
        String testPackagesLabel = Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_test.src.dir");
        Node testPackagesNode = new Node(new ProjectsTabOperator().getProjectRootNode(SAMPLE_PROJECT_NAME), testPackagesLabel);
        // Test Packages|sample1|SampleClass1Test.java
        new JavaNode(testPackagesNode, SAMPLE1_PACKAGE_NAME+"|"+SAMPLE2_CLASS_NAME+"Test.java"); // NOI18N

        // check default, protected and public method tests created and private is not created
        EditorOperator eoTest = new EditorOperator(SAMPLE2_CLASS_NAME+"Test.java");    // NOI18N
        // wait code is generated
        eoTest.txtEditorPane().waitText("testDefaultMethod"); // NOI18N
        eoTest.txtEditorPane().waitText("testProtectedMethod"); // NOI18N
        eoTest.txtEditorPane().waitText("testPublicMethod"); // NOI18N
        assertFalse("Created test should not include test of private method.", eoTest.contains("testPrivateMethod")); // NOI18N
        eoTest.close();
        
        // go to test
        // "Navigate"
        String navigateItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/GoTo"); // NOI18N
        // "Go to Test"
        String goToTestItem = Bundle.getString("org.netbeans.modules.junit.Bundle", "LBL_Action_GoToTest");  // NOI18N
        // go to test ("Navigate|Go to Test") - main menu action
        Action gotoTestAction = new Action(navigateItem+"|"+goToTestItem, null);
        gotoTestAction.perform(sampleClass2Node);
        // wait until test is opened in editor
        new EditorOperator(SAMPLE2_CLASS_NAME+"Test.java");    // NOI18N
        
        // run generated test
        
        // "Run" 
        String runItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
        // "Run File"
        String runOtherItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject/RunOther");
        // "Test File"
        String testFileItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", 
                                                     "LBL_TestSingleAction_Name",
                                                     new Object[] {new Integer(1), SAMPLE2_FILE_NAME});
        // "Test Project"
        String testProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", 
                                                     "LBL_TestProjectAction_Name",
                                                     new Object[] {new Integer(1), SAMPLE_PROJECT_NAME});
                                                     
        // increase timeout to 60 seconds
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000); // NOI18N
        // start to track Main Window status bar
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
         // call "Run|Run File|Test "SampleClass2.java""
        new Action(runItem+"|"+runOtherItem+"|"+testFileItem, null).perform(sampleClass2Node);
     
        // check status line
        // "SampleProject (test-single)"
        String testSingleTarget = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "TITLE_output_target",
                new Object[] {SAMPLE_PROJECT_NAME, null, "test-single"});  // NOI18N
        String testTarget = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle",
                "TITLE_output_target",
                new Object[] {SAMPLE_PROJECT_NAME, null, "test"});  // NOI18N
        // "Build of SampleProject (test-single) failed."
        String failedMessage = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", 
                "FMT_target_failed_status",
                new Object[] {testSingleTarget});
        // wait message "Build of SampleProject (test-single) failed."
        stt.waitText(failedMessage);

         // call "Run|Test "SampleProject""
        new Action(runItem+"|"+testProjectItem, null).perform(sampleClass2Node);
        // "Build of SampleProject (test) failed."
        failedMessage = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", 
                "FMT_target_failed_status",
                new Object[] {testTarget});
        // wait message "Build of SampleProject (test) failed."
        stt.waitText(failedMessage);
        stt.stop();
        
        // test JUnit on folder
        createTestsAction.perform(sample1Node);
        new NbDialogOperator(createTestsTitle).ok();
        // wait until test node is created
        // Test Packages|sample1|Sample1Suite.java
        JavaNode suiteNode = new JavaNode(testPackagesNode, SAMPLE1_PACKAGE_NAME+"|Suite.java"); // NOI18N
        suiteNode.open();
        // check suite is open in editor and it conteins generated test class
        assertTrue("Created suite should include test of public method.", 
                   new EditorOperator("Suite").contains(SAMPLE2_CLASS_NAME+"Test")); // NOI18N
        EditorOperator.closeDiscardAll();
    }

    /** Test Debugging
     * - find sample class in editor
     * - select text 'System.out.println("Hello");' and push Shift+F8 to toggle breakpoint
     * - select text 'System.out.println("Good bye");' and call 'Toggle Breakpoint" context menu item
     * - run debugger from main menu ("Run "SampleClass1.java" in  Debugger")
     * - wait until first breakpoint is reached and call Continue from main menu
     * - wait until second breakpoint is reached and check 'Hello' is printed to output
     * - finish debugger by main menu action (Finish Debugger Session)
     * - delete sample class
     */
    public void testDebugging() throws Throwable {
        // Status bar tracer
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(SAMPLE_PROJECT_NAME);
        JavaNode sampleClass1Node = new JavaNode(sourcePackagesNode, SAMPLE1_PACKAGE_NAME+"|"+SAMPLE1_FILE_NAME);
        try {
            // find sample file in Editor
            EditorOperator eo = new EditorOperator(SAMPLE1_FILE_NAME);
            eo.setCaretPosition("public static void main", true);
            final int insertLine = eo.getLineNumber()+2;

            // if file not contains brpText from previous test cases, insert it
            String brpText = "System.out.println(\"Hello\");"; // NOI18N
            if(!eo.contains(brpText)) {
                eo.insert(brpText+"\n", insertLine, 1);
            }
            eo.select(brpText);
            
            ToggleBreakpointAction toggleBreakpointAction = new ToggleBreakpointAction();
            // toggle breakpoint via Shift+F8
            toggleBreakpointAction.performShortcut(eo);

            // wait breakpoint established
            new Waiter(new Waitable() {
                public Object actionProduced(Object editorOper) {
                    Object[] annotations = ((EditorOperator)editorOper).getAnnotations(insertLine);
                    for (int i = 0; i < annotations.length; i++) {
                        if("Breakpoint".equals(EditorOperator.getAnnotationType(annotations[i]))) { // NOI18N
                            return Boolean.TRUE;
                        }
                    }
                    return null;
                }
                public String getDescription() {
                    return("Wait breakpoint established on line "+insertLine); // NOI18N
                }
            }).waitAction(eo);
            
            // if file not contains second brpText from previous test cases, insert it
            brpText = "System.out.println(\"Good bye\");"; // NOI18N
            if(!eo.contains(brpText)) {
                eo.insert(brpText+"\n", insertLine+1, 1);
            }
            eo.select(brpText);
            // toggle breakpoint via pop-up menu
            // clickForPopup(0, 0) used in the past sometimes caused that menu
            // was opened outside editor area because editor roll up after 
            // text was selected
            toggleBreakpointAction.perform(eo.txtEditorPane());
            // wait second breakpoint established
            new Waiter(new Waitable() {
                public Object actionProduced(Object editorOper) {
                    Object[] annotations = ((EditorOperator)editorOper).getAnnotations(insertLine+1);
                    for (int i = 0; i < annotations.length; i++) {
                        if("Breakpoint".equals(EditorOperator.getAnnotationType(annotations[i]))) { // NOI18N
                            return Boolean.TRUE;
                        }
                    }
                    return null;
                }
                public String getDescription() {
                    return("Wait breakpoint established on line "+(insertLine+1)); // NOI18N
                }
            }).waitAction(eo);
            // start to track Main Window status bar
            stt.start();
            // start debugging
            new DebugAction().performMenu(sampleClass1Node);
            // check the first breakpoint reached
            // wait status text "Thread main stopped at SampleClass1.java:"
            // increase timeout to 60 seconds
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
            String labelLine = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle",
                    "CTL_Thread_stopped",
                    new String[] {"main", SAMPLE1_FILE_NAME, null, String.valueOf(insertLine)}); // NOI18N
            stt.waitText(labelLine);
            // continue debugging
            new ContinueAction().perform();
            // check the second breakpoint reached
            // wait status text "Thread main stopped at SampleClass1.java:"
            String labelLine1 = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle",
                    "CTL_Thread_stopped",
                    new String[] {"main", SAMPLE1_FILE_NAME, null, String.valueOf(insertLine)}); // NOI18N
            stt.waitText(labelLine1);
            // check "Hello" was printed out in Output
            OutputTabOperator oto = new OutputTabOperator("debug-single"); // NOI18N
            // wait until text Hello is not written in to the Output
            oto.waitText("Hello"); // NOI18N
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        } finally {
            // finish debugging
            new FinishDebuggerAction().perform();
            // check status line
            // "SampleProject (debug-single)"
            String outputTarget = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle",
                    "TITLE_output_target",
                    new Object[] {SAMPLE_PROJECT_NAME, null, "debug-single"});  // NOI18N
            // "Finished building SampleProject (debug-single)"
            String finishedMessage = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle",
                    "FMT_finished_target_status",
                    new Object[] {outputTarget});
            stt.waitText(finishedMessage);
            stt.stop();
            // delete sample class
            sampleClass1Node.delete();
            String confirmTitle = Bundle.getString("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_SafeDel"); // NOI18N
            String confirmButton = UIManager.getDefaults().get("OptionPane.okButtonText").toString(); // NOI18N
            // "Confirm Object Deletion"
            new JButtonOperator(new NbDialogOperator(confirmTitle), confirmButton).push();
        }
    }

     /** Test Options  
      * - open Options window from main menu Tools|Options
      * - select General category
      * - pick HTTP Proxy
      * - set Proxy Host to webcache
      * - set Proxy Port to 8080
      * - click OK to confirm and close Options window
      */
    public void testOptions() {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectGeneral();
        // "HTTP Proxy"
        String hTTPProxyLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Use_HTTP_Proxy");
        new JRadioButtonOperator(optionsOper, hTTPProxyLabel).push();
        // "Proxy Host:"
        String proxyHostLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Proxy_Host");
        JLabelOperator jloHost = new JLabelOperator(optionsOper, proxyHostLabel);
        new JTextFieldOperator((JTextField)jloHost.getLabelFor()).typeText("webcache"); // NOI18N
        // "Proxy Port"
        String proxyPortLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Proxy_Port");
        JLabelOperator jloPort = new JLabelOperator(optionsOper, proxyPortLabel);
        new JTextFieldOperator((JTextField)jloPort.getLabelFor()).setText("8080"); // NOI18N
        optionsOper.ok();
    }

     /** Test Advanced Options (former Classic view). Options in Classic view should be gradually 
      * replaced by basic options (former Modern view). When it happens this test case
      * can be removed.
      * - open Options window from main menu Tools|Options
      * - select IDE Configuration|System|Print Settings node
      * - set Page Footer Alignment to "LEFT"
      * - set Line Ascent Correction to "2.0"
      * - set Wrap Lines to "false"
      * - close Options window
      */
    public void testOptionsClassicView() {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.switchToClassicView();
        // set exact comparator because in Japanese there is conflict with Filesystem settings
        optionsOper.treeTable().tree().setComparator(new Operator.DefaultStringComparator(true, true));
        // "IDE Configuration|System|Print Settings"
        String printSettingsPath = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration") + "|" +
                                   Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/System")+"|"+
                                   Bundle.getString("org.netbeans.core.Bundle", "Services/org-openide-text-PrintSettings.settings");
        optionsOper.selectOption(printSettingsPath);
        PropertySheetOperator pso = new PropertySheetOperator(optionsOper);
        // "Page Footer Alignment"
        String footerALLabel = Bundle.getString("org.openide.text.Bundle", "PROP_FOOTER_ALIGNMENT");
        // "LEFT"
        String leftLabel = Bundle.getString("org.openide.util.Bundle", "LEFT");
        new Property(pso, footerALLabel).setValue(leftLabel);
        // "Line Ascent Correction"
        String lineACLabel = Bundle.getString("org.openide.text.Bundle", "PROP_LINE_ASCENT_CORRECTION");
        new Property(pso, lineACLabel).setValue("2.0");
        // "Wrap Lines"
        String wrapLinesLabel = Bundle.getString("org.openide.text.Bundle", "PROP_WRAP");
        new Property(pso, wrapLinesLabel).setValue("false");
        optionsOper.close();
    }
    
    /** Test CVS Lite
     * - from main menu invoke "Versioning|CVS|Checkout"
     * - wait for Checkout dialog and close it
     * TODO - when better support for local repository implemented, we can add more tests
     */
    public void testCVSLite() {
        // "Versioning"
        String versioningItem = Bundle.getStringTrimmed("org.netbeans.modules.versioning.Bundle", "Menu/Window/Versioning");
        // "CVS"
        String cvsItem = Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.Bundle",
                "CTL_MenuItem_CVSCommands_Label");
        // "Checkout..."
        String checkoutItem = Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.checkout.Bundle",
                "CTL_MenuItem_Checkout_Label");
        new ActionNoBlock(versioningItem+"|"+cvsItem+"|"+checkoutItem, null).perform();

        String checkoutTitle = Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle",
                "BK0007");
        NbDialogOperator checkoutOper = new NbDialogOperator(checkoutTitle);
        checkoutOper.close();
    }
    
    /** Test XML
     * - open "Tools|DTDs and XML Schemas"
     * - select "NetBeans Catalog|-//DTD XMLCatalog//EN"
     * - call "View" on it
     * - check it is opened in editor and close it
     * - close "DTDs and XML Schemas" dialog
     * - create XML file
     * - call "Check XML" on xml node
     * - find and close output tab
     * - call "Validate XML" on xml node
     * - find and close output tab
     * - call "Generate DTD" on xml node
     * - set name and confirm the dialog
     * - wait until dtd is opened in editor and close it
     * - call "Check DTD" on dtd node
     * - find and close output tab
     */
    public void testXML() {
        // check XML Entity Catalogs
        
        // "Tools"
        String toolsItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools"); // NOI18N
        // "DTDs and XML Schemas"
        String dtdsItem = Bundle.getStringTrimmed("org.netbeans.modules.xml.catalog.Bundle", "LBL_CatalogAction_Name");
        new Action(toolsItem+"|"+dtdsItem, null).perform();
        // "DTDs and XML Schemas"
        String dtdsTitle = Bundle.getString("org.netbeans.modules.xml.catalog.Bundle", "LBL_CatalogPanel_Title");
        NbDialogOperator dtdsOper = new NbDialogOperator(dtdsTitle);
        
        // "NetBeans Catalog"
        String netbeansCatalogLabel = Bundle.getString("org.netbeans.modules.xml.catalog.impl.Bundle", "NAME_system_catalog");
        String publicID = "-//DTD XMLCatalog//EN";
        Node catalogNode = new Node(new JTreeOperator(dtdsOper), 
                                    netbeansCatalogLabel+"|"+
                                    publicID);
        // view and close it
        new ViewAction().perform(catalogNode);
        new EditorOperator(publicID).close();
        dtdsOper.close();
  
        // create an XML file

        // create xml package
        // select Source Packages to not create xml folder in Test Packages
        new SourcePackagesNode(SAMPLE_PROJECT_NAME).select();
        // "Java Classes"
        String javaClassesLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes");
        // "Java Package"
        String packageLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Package");
        NewFileWizardOperator.create(SAMPLE_PROJECT_NAME, javaClassesLabel, packageLabel, null, "xml"); // NOI18N
        Node xmlNode = new Node(new SourcePackagesNode(SAMPLE_PROJECT_NAME), "xml"); //NOI18N
        // "XML"
        String xmlCategory = Bundle.getString("org.netbeans.api.xml.resources.Bundle", "Templates/XML");
        // "XML Document"
        String xmlDocument = Bundle.getString("org.netbeans.modules.xml.resources.Bundle", "Templates/XML/XMLDocument.xml");
        NewFileWizardOperator.invoke(xmlNode, xmlCategory, xmlDocument);
        NewFileNameLocationStepOperator nameStepOper = new NewFileNameLocationStepOperator();
        nameStepOper.setObjectName("XMLDocument");  // NOI18N
        nameStepOper.next();
        nameStepOper.finish();
        // wait node is present
        Node xmlDocumentNode = new Node(xmlNode, "XMLDocument.xml"); // NOI18N
        // wait xml document is open in editor
        new EditorOperator("XMLDocument.xml").close();  // NOI18N

        // "Check XML"
        
        String checkXMLItem = Bundle.getStringTrimmed("org.netbeans.modules.xml.tools.actions.Bundle", "NAME_Check_XML");
        // invoke context action to check xml
        new Action(null, checkXMLItem).perform(xmlDocumentNode);
        // "XML check"
        String xmlCheckTitle = Bundle.getString("org.netbeans.modules.xml.actions.Bundle", "TITLE_XML_check_window");
        // find and close an output with the result of xml check
        new OutputTabOperator(xmlCheckTitle).close();
        
        // "Validate XML"
        
        String validateItem = Bundle.getStringTrimmed("org.netbeans.modules.xml.tools.actions.Bundle", "NAME_Validate_XML");
        // invoke context action to validate xml
        new Action(null, validateItem).perform(xmlDocumentNode);
        // find and close an output with the result of xml validation
        new OutputTabOperator(xmlCheckTitle).close();
        
        // "Generate DTD..."
        
        String generateDTDItem = Bundle.getStringTrimmed("org.netbeans.modules.xml.tools.generator.Bundle", "PROP_GenerateDTD");
        new ActionNoBlock(null, generateDTDItem).perform(xmlDocumentNode);
        // "Select File Name"
        String selectTitle = Bundle.getString("org.netbeans.modules.xml.tools.generator.Bundle", "PROP_fileNameTitle");
        NbDialogOperator selectDialog = new NbDialogOperator(selectTitle);
        // name has to be set because of issue http://www.netbeans.org/issues/show_bug.cgi?id=46049
        new JTextFieldOperator(selectDialog).setText("DTD");
        String oKLabel = Bundle.getString("org.netbeans.core.windows.services.Bundle", "OK_OPTION_CAPTION");
        new JButtonOperator(selectDialog, oKLabel).push();
        // wait DTD is open in editor
        new EditorOperator("DTD.dtd").close();  // NOI18N
        Node dtdNode = new Node(xmlNode, "DTD.dtd"); // NOI18N
        
        // "Check DTD"
        
        String checkDTDItem = Bundle.getStringTrimmed("org.netbeans.modules.xml.tools.actions.Bundle", "NAME_Validate_DTD");
        new Action(null, checkDTDItem).perform(dtdNode);
        // find and close an output with the result of dtd check
        new OutputTabOperator(xmlCheckTitle).close();

        // "Generate DOM Tree Scanner"
        /* Not available because of http://www.netbeans.org/issues/show_bug.cgi?id=90174
        String generateScannerItem = Bundle.getStringTrimmed("org.netbeans.modules.xml.tools.generator.Bundle", "PROP_GenerateDOMScanner");
        new ActionNoBlock(null, generateScannerItem).perform(dtdNode);
        selectDialog = new NbDialogOperator(selectTitle);
        new JButtonOperator(selectDialog, oKLabel).push();
        // wait Scanner is open in editor
        new EditorOperator("DTDScanner.java").close();  // NOI18N
        Node scannerNode = new Node(xmlNode, "DTDScanner.java"); // NOI18N
        */
    }

    /** Test Window System 
     * - open Favorites top component from main menu Window|Favorites
     * - attach Favorites as last tab to output mode
     * - attach Favorites to top of Projects tab
     * - attach Favorites to right of output mode
     * - attach Favorites as last tab to explorer mode (next to Projects)
     * - close Favorites
     * - open sample1|SampleClass2.java file
     * - maximize opened editor by menu item "Maximize Window" on its tab
     * - restore editor by menu item "Restore Window" on its tab
     * - close all open editors
     */
    public void testWindowSystem() {
        final ProjectsTabOperator projectsOper = ProjectsTabOperator.invoke();
        final FavoritesOperator favoritesOper = FavoritesOperator.invoke();
        
        // test attaching
        favoritesOper.attachTo(new OutputOperator(), AttachWindowAction.AS_LAST_TAB);
        favoritesOper.attachTo(projectsOper, AttachWindowAction.TOP);
        favoritesOper.attachTo(new OutputOperator(), AttachWindowAction.RIGHT);
        favoritesOper.attachTo(projectsOper, AttachWindowAction.AS_LAST_TAB);
        // wait until TopComponent is in new location and is showing
        final TopComponent projectsTc = (TopComponent)projectsOper.getSource();
        final TopComponent favoritesTc = (TopComponent)favoritesOper.getSource();
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object tc) {
                    // run in dispatch thread
                    Mode mode1 = (Mode)projectsOper.getQueueTool().invokeSmoothly(new QueueTool.QueueAction("findMode") {    // NOI18N
                        public Object launch() {
                            return WindowManager.getDefault().findMode(projectsTc);
                        }
                    });
                    Mode mode2 = (Mode)favoritesOper.getQueueTool().invokeSmoothly(new QueueTool.QueueAction("findMode") {    // NOI18N
                        public Object launch() {
                            return WindowManager.getDefault().findMode(favoritesTc);
                        }
                    });
                    return (mode1==mode2 && favoritesTc.isShowing()) ? Boolean.TRUE : null;
                }
                public String getDescription() {
                    return("Favorites TopComponent is next to Projects TopComponent."); // NOI18N
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e); // NOI18N
        }
        favoritesOper.close();

        // test maximize/restore
        // open sample file in Editor
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(SAMPLE_PROJECT_NAME);
        Node sample1Node = new Node(sourcePackagesNode, SAMPLE1_PACKAGE_NAME);
        JavaNode sampleClass2Node = new JavaNode(sample1Node, SAMPLE2_FILE_NAME);
        sampleClass2Node.open();
        // find open file in editor
        EditorOperator eo = new EditorOperator(SAMPLE2_FILE_NAME);
        eo.maximize();
        eo.restore();
        EditorOperator.closeDiscardAll();
    }

    /** Test Plugins
     * - open Plugins window from main menu Tools|Plugins
     * - wait until tabbed pane is enabled and the Installed tab is enabled
     * - click "Reload Catalog" button
     * - wait until "Available Plugins" tab is enabled
     * - switch to "Available Plugins" tab
     * - type "Source Browser" into Search field
     * - wait until table contains "Source Browser" module in the first row
     * - select that row
     * - click check box for the module
     * - click Install button
     * - in "NetBeans IDE Installer" dialog click Next
     * - click "I accept..." radio button
     * - click Install button
     * - wait until the module is turned on (message in main window status bar)
     * - click Finish button to dismiss the dialog
     * - switch to Installed tab
     * - select "netbeans.org Source Browser" plugin
     * - click Deactivate button
     * - wait for "NetBeans IDE Installer" dialog
     * - click Cancel
     * - Cancel deactivation because it requires restart (it can be fixed in future releases)
     * - close Plugins dialog
     */
    public void testPlugins() {
        final String SOURCE_BROWSER_LABEL = "netbeans.org Source Browser"; //NOI18N
        PluginsOperator pluginsOper = null;
        try {
            pluginsOper = PluginsOperator.invoke();
            
            // Install
            
            pluginsOper.selectAvailablePlugins();
            pluginsOper.search(SOURCE_BROWSER_LABEL);
            pluginsOper.install(SOURCE_BROWSER_LABEL);

            // Deactivate

            pluginsOper.selectInstalled();
            pluginsOper.selectPlugin(SOURCE_BROWSER_LABEL);
            pluginsOper.deactivate();
            pluginsOper.installer().cancel();
            
            /* Because it needs restart to deactivate module (probably can be changed in 
             * future releases), we skip this part.
             * 
            // check Status line
            // "Turning off modules...done."
            String turningOffLabel = Bundle.getString("org.netbeans.core.startup.Bundle", "MSG_finish_disable_modules");
            // increase timeout to 120 seconds
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 120000);
            MainWindowOperator.getDefault().waitStatusText(turningOffLabel);
            // click Finish button
            String finishLabel = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.wizards.Bundle", "InstallUnitWizardModel_Buttons_Finish");
            new JButtonOperator(installerOper, finishLabel).push();

            // Activate module

            pluginsOper.selectInstalled();
            pluginsOper.selectPlugin(SOURCE_BROWSER_LABEL);
            pluginsOper.activate();
            // "Activate"
            String activateInDialogLabel = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.wizards.Bundle", "UninstallUnitWizardModel_Buttons_TurnOn");
            new JButtonOperator(pluginsOper.installer(), activateInDialogLabel).pushNoBlock();
            // check Status line
            // "Turning on modules...done."
            String turningOnLabel = Bundle.getString("org.netbeans.core.startup.Bundle", "MSG_finish_enable_modules");
            // increase timeout to 120 seconds
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 120000);
            MainWindowOperator.getDefault().waitStatusText(turningOnLabel);
            // click Finish button
            pluginsOper.installer().finish();
            */
            
            pluginsOper.close();
        } catch (JemmyException e) {
            // close possible error dialog
            // "Error"
            String errorTitle = Bundle.getString("org.netbeans.modules.autoupdate.ui.Bundle", "CTL_Error");
            if(JDialogOperator.findJDialog(errorTitle, true, true) != null) {
                new NbDialogOperator(errorTitle).close();
            }
            // close Plugins dialog
            if(pluginsOper != null) {
                pluginsOper.close();
            }
            throw e;
        }
    }

    public void testInitGCProjects() throws Exception {
        WatchProjects.initialize();
    }
    
    
    public void testGCProjects() throws Exception {
        WatchProjects.assertProjects();
    }

    public void testBlacklistedClassesHandler() throws Exception {
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandler.getBlacklistedClassesHandler();
        assertNotNull("BlacklistedClassesHandler should be available", bcHandler);
        try {
            assertTrue(bcHandler.listViolations(), bcHandler.noViolations());
        } finally {
            bcHandler.remove();
        }        
    }
    
    /** Closes help window if any. It should not stay open between test cases.
     *  Otherwise it can break next tests.
     */
    private static void closeHelpWindow() {
        Window helpWindow = WindowOperator.findWindow(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                WindowOperator winOper = new WindowOperator((Window)comp);
                winOper.setOutput(TestOut.getNullOutput());
                return  null != winOper.findSubComponent(new ComponentChooser() {
                    public boolean checkComponent(Component comp) {
                        return comp.getClass().getName().startsWith("javax.help.JHelp"); //NOI18N
                    }
                    public String getDescription() {
                        return("any javax.help");  //NOI18N
                    }
                });
            }
            public String getDescription() {
                return "containing any javax.help.JHelp component";  //NOI18N
            }
        });
        if(helpWindow != null) {
            new WindowOperator(helpWindow).close();
        }
    }
}
