/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.nodes;

import java.awt.Toolkit;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.ide.ProjectSupport;

/** Test of org.netbeans.jellytools.nodes.FolderNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class FolderNodeTest extends org.netbeans.jellytools.JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FolderNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new org.netbeans.junit.NbTestSuite();
        // Cannot test because folder at different view has different items. */
        // suite.addTest(new FolderNodeTest("testVerifyPopup"));
        // Explore from here is used on web services node but to create such
        // a node you need application server installed. For now we skip this test.
        //suite.addTest(new FolderNodeTest("testExploreFromHere"));
        suite.addTest(new FolderNodeTest("testFind"));
        suite.addTest(new FolderNodeTest("testCompile"));
        suite.addTest(new FolderNodeTest("testCut"));
        suite.addTest(new FolderNodeTest("testCopy"));
        suite.addTest(new FolderNodeTest("testPaste"));
        suite.addTest(new FolderNodeTest("testDelete"));
        suite.addTest(new FolderNodeTest("testRename"));
        suite.addTest(new FolderNodeTest("testProperties"));
        suite.addTest(new FolderNodeTest("testNewFile"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test case setup. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Test verifyPopup method.
     * Currently folder at differetn view has different items. */
    public void testVerifyPopup() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.verifyPopup();
    }
    
    /** Test find method. */
    public void testFind() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.find();
        new FindInFilesOperator().close();
    }
    
    // name of sample web application project
    private static final String SAMPLE_WEB_PROJECT_NAME = "SampleWebProject";  //NOI18N
    // name of sample web service
    private static final String SAMPLE_WEB_SERVICE_NAME = "SampleWebService";  //NOI18N

    /** Test exploreFromHere. */
    public void testExploreFromHere() {
        // create new web application project
        
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Web"
        String webLabel = Bundle.getString("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        npwo.selectCategory(webLabel);
        // "Web Application"
        String webApplicationLabel = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web/emptyWeb.xml");
        npwo.selectProject(webApplicationLabel);
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_WEB_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.finish();
        // wait project appear in projects view
        Node projectRootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        // wait index.jsp is opened in editor
        EditorOperator editor = new EditorOperator("index.jsp"); // NOI18N
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        
        // create a web service

        // "Web Services"
        String webServicesLabel = Bundle.getString(
                "org.netbeans.modules.websvc.dev.wizard.Bundle", "Templates/WebServices");
        // "Web Service"
        String webServiceLabel = org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.websvc.dev.wizard.Bundle", "Templates/WebServices/WebService");
        NewFileWizardOperator.invoke(projectRootNode, webServicesLabel, webServiceLabel);
        NewFileNameLocationStepOperator nameStepOper = new NewFileNameLocationStepOperator();
        nameStepOper.setPackage("dummy"); // NOI18N
        nameStepOper.setObjectName(SAMPLE_WEB_SERVICE_NAME);
        nameStepOper.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator(SAMPLE_WEB_SERVICE_NAME).closeAllDocuments();

        // "Web Services"
        String webServicesNodeLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.webservices.ui.Bundle", "LBL_WebServices");
        FolderNode wsNode = new FolderNode(projectRootNode, webServicesNodeLabel+"|"+SAMPLE_WEB_SERVICE_NAME);
        wsNode.exploreFromHere();
        new TopComponentOperator(SAMPLE_WEB_SERVICE_NAME).close();  // NOI18N
    }

    /** Test compile. */
    public void testCompile() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        folderNode.compile();
        // wait status text "Building SampleProject (compile-single)"
        statusTextTracer.waitText("compile-single", true); // NOI18N
        // wait status text "Finished building SampleProject (compile-single).
        statusTextTracer.waitText("compile-single", true); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test paste. */
    public void testPaste() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "src|sample1"); // NOI18N
        FolderNode sample2Node = new FolderNode(sample1Node, "sample2"); // NOI18N
        sample2Node.copy();
        sample1Node.paste();
        new FolderNode(sample1Node, "sample2_1").delete();  // NOI18N
        // "Safe Delete"
        String safeDeleteTitle = Bundle.getString("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_SafeDel"); // NOI18N
        new NbDialogOperator(safeDeleteTitle).ok();
    }
    
    /** Test cut. */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy. */
    public void testCopy() {
        final Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.copy();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test delete. */
    public void testDelete() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.delete();
        Utils.closeSafeDeleteDialog();
    }
    
    /** Test rename */
    public void testRename() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "nbproject"); // NOI18N
        sample1Node.rename();
        Utils.closeRenameDialog();
    }
    
    /** Test properties */
    public void testProperties() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "src|sample1"); // NOI18N
        sample1Node.properties();
        Utils.closeProperties("sample1"); //NOI18N
    }
    
    /** Test newFile */
    public void testNewFile() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.newFile();
        new NewFileWizardOperator().close();
    }
}
