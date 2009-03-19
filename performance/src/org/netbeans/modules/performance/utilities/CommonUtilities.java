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

package org.netbeans.modules.performance.utilities;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.net.*;
import java.util.zip.*;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import javax.swing.tree.TreePath;

import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.PluginsOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;


/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class CommonUtilities {
    
    public static final String SOURCE_PACKAGES;// = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir");
    public static final String TEST_PACKAGES;// = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_test.src.dir");
    private static PerformanceTestCase test = null;
    
    private static int size=0;
    private static DocumentBuilderFactory dbf=null;
    private static DocumentBuilder db=null;
    private static Document allPerfDoc=null;
    private static Element testResultsTag, testTag, perfDataTag, testSuiteTag=null;
    private static String projectsDir; // <nbextra>/data/
    private static String tempDir; // <nbjunit.workdir>/tmpdir/
    
    static {
        SOURCE_PACKAGES = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir");
        TEST_PACKAGES = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_test.src.dir");
        String workDir = System.getProperty("nbjunit.workdir");
        if (workDir != null) {
            projectsDir = workDir + File.separator;
            try {
                projectsDir = new File(projectsDir + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + "nbextra" + File.separator + "data")
                        .getCanonicalPath() + File.separator;
            } catch (IOException ex) {
                System.err.println("Exception: " + ex);
            }

            tempDir = workDir + File.separator;
            try {
                File dir = new File(tempDir + File.separator + "tmpdir");
                tempDir = dir.getCanonicalPath() + File.separator;
                dir.mkdirs();
            } catch (IOException ex) {
                System.err.println("Exception: " + ex);
            }
        }
    }
    
    /**
     * Returns data directory path ending with file.separator
     * @return &lt;nbextra&gt;/data/
     */
    public static String getProjectsDir() {
        return projectsDir;
    }

    /**
     * Returns temprorary directory path ending with file.separator
     * @return &lt;nbjunit.workdir&gt;/tmpdir/
     */
    public static String getTempDir() {
        return tempDir;
    }
    
    public static void cleanTempDir() throws IOException {
        File dir = new File(tempDir);
        deleteFile(dir);
        dir.mkdirs();
    }

    // private method for deleting a file/directory (and all its subdirectories/files)
    public static void deleteFile(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
            
        }
        // file is a File :-)
        boolean result = file.delete();
        if (result == false ) {
            // a problem has appeared
            throw new IOException("Cannot delete file, file = " + file.getPath());
        }
    }
    
    /** Creates a new instance of Utilities */
    public CommonUtilities() {
    }

    public static String getTimeIndex() {
        return new SimpleDateFormat("HHmmssS",Locale.US).format(new Date());
    }
    
    /**
     * Close BluePrints.
     */
    public static void closeBluePrints(){
        new TopComponentOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.blueprints.Bundle","LBL_Tab_Title")).close();
    }
    
    /**
     * Close All Documents.
     */
    public static void closeAllDocuments(){
	if ( new Action("Window|Close All Documents",null).isEnabled() )
	        new CloseAllDocumentsAction().perform();
    }
    
    /**
     * Close Memory Toolbar.
     */
    public static void closeMemoryToolbar(){
        // View|Toolbars|Memory
        closeToolbar(Bundle.getStringTrimmed("org.openide.actions.Bundle","View") + "|" +
                Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle", "CTL_ToolbarsListAction") + "|" +
                "Memory");
    }
    
    public static void closeTaskWindow() {
        waitProjectTasksFinished();
        TopComponentOperator tco = new TopComponentOperator("Tasks");
        tco.close();
    }
    
    public static void installPlugin(String name) {

       PluginsOperator po = PluginsOperator.invoke();

       po.install(name);
       po.close();
    }   

    private static void closeToolbar(String menu){
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menuBar = new JMenuBarOperator(mainWindow.getJMenuBar());
        JMenuItemOperator menuItem = menuBar.showMenuItem(menu,"|");
        
        if(menuItem.isSelected())
            menuItem.push();
        else {
            menuItem.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
            mainWindow.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        }
    }
    
    /**
     * Work around issue 35962 (Main menu popup accidentally rolled up)
     * Issue has been fixed for JDK 1.5, so we will use it only for JDK 1.4.X
     */
    public static void workarroundMainMenuRolledUp() {
        if(System.getProperty("java.version").indexOf("1.4") != -1) {
            String helpMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Help") + "|" + Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle" , "About");
            String about = Bundle.getStringTrimmed("org.netbeans.core.Bundle_nb", "CTL_About_Title");
            
            new ActionNoBlock(helpMenu, null).perform();
            new NbDialogOperator(about).close();
        }
    }

    public static String jEditProjectOpen() {

/* Temporary solution - download jEdit from internal location */

        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        int BUFFER = 2048;

        try {
            URL url = new URL("http://spbweb.russia.sun.com/~ok153203/jEdit41.zip");

            out = new BufferedOutputStream(new FileOutputStream(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip"));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }

        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(new File(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip"));
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName()).mkdir();
                    continue;
                }
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip";
    }


    /**
     * Open files
     *
     * @param project project which will be used as source for files to be opened
     * @param files_path path to the files to be opened
     */
    public static void openFiles(String project, String[][] files_path){
        Node[] openFileNodes = new Node[files_path.length];
        
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(project);
        
        for(int i=0; i<files_path.length; i++) {
            openFileNodes[i] = new Node(sourcePackagesNode, files_path[i][0] + '|' + files_path[i][1]);
            
            // open file one by one, opening all files at once causes never ending loop (java+mdr)
            // new OpenAction().performAPI(openFileNodes[i]);
        }
        
        // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
        // it doesn't finish in the real-time -> hard to reproduced by hand
        new OpenAction().performAPI(openFileNodes);
    }
    
    /**
     * Copy file f1 to f2
     * @param f1 file 1
     * @param f2 file 2
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void copyFile(java.io.File f1, java.io.File f2) throws java.io.FileNotFoundException, java.io.IOException{
        int data;
        java.io.InputStream fis = new java.io.BufferedInputStream(new java.io.FileInputStream(f1));
        java.io.OutputStream fos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(f2));
        
        while((data=fis.read())!=-1){
            fos.write(data);
        }
    }
    
    /**
     * Invoke open action on file and wait for editor
     * @param filename
     * @param waitforeditor
     * @return
     */
    public static EditorOperator openFile(Node fileNode, String filename, boolean waitforeditor) {
        new OpenAction().performAPI(fileNode);
        
        if (waitforeditor) {
            EditorOperator editorOperator = new EditorOperator(filename);
            return editorOperator;
        } else
            return null;
    }
    
    
    public static EditorOperator openFile(String project, String filepackage, String filename, boolean waitforeditor) {
        return openFile(new Node(new SourcePackagesNode(project), filepackage + "|" + filename), filename, waitforeditor);
    }
    
    /**
     * Invoke Edit Action on file and wait for editor
     * @param project
     * @param filepackage
     * @param filename
     * @return
     */
    public static EditorOperator editFile(String project, String filepackage, String filename) {
        Node filenode = new Node(new SourcePackagesNode(project), filepackage + "|" + filename);
        new EditAction().performAPI(filenode);
        EditorOperator editorOperator = new EditorOperator(filename);
        return editorOperator;
    }
    
    
    /**
     * open small form file in the editor
     * @return Form Designer
     */
    public static FormDesignerOperator openSmallFormFile(){
        Node openFile = new Node(new SourcePackagesNode("PerformanceTestData"),"org.netbeans.test.performance|JFrame20kB.java");
        new OpenAction().performAPI(openFile);
        return new FormDesignerOperator("JFrame20kB");
        
    }
    
    
    /**
     * Edit file and type there a text
     * @param filename file that will be eddited
     * @param line line where put the text
     * @param text write the text
     * @param save save at the and
     */
    public static void insertToFile(String filename, int line, String text, boolean save) {
        EditorOperator editorOperator = new EditorOperator(filename);
        editorOperator.setCaretPositionToLine(line);
        editorOperator.insert(text);
        
        if (save)
            editorOperator.save();
    }
    
    /**
     * Create project
     * @param category project's category
     * @param project type of the project
     * @param wait wait for background tasks
     * @return name of recently created project
     */
    public static String createproject(String category, String project, boolean wait) {
        // select Projects tab
        ProjectsTabOperator.invoke();
        
        // create a project
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        
        NewProjectNameLocationStepOperator wizard_location = new NewProjectNameLocationStepOperator();
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(getTempDir());
        String pname = wizard_location.txtProjectName().getText() + System.currentTimeMillis();
        wizard_location.txtProjectName().clearText();
        wizard_location.txtProjectName().typeText(pname);
        
//        // if the project exists, try to generate new name
//        for (int i = 0; i < 5 && !wizard.btFinish().isEnabled(); i++) {
//            pname = pname+"1";
//            wizard_location.txtProjectName().clearText();
//            wizard_location.txtProjectName().typeText(pname);
//        }
        wizard.finish();
        
        // wait 10 seconds
        waitForProjectCreation(10000, wait);
        
        return pname;
    }
    
    
    protected static void waitForProjectCreation(int delay, boolean wait){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException exc) {
            exc.printStackTrace(System.err);
        }
        
        // wait for classpath scanning finish
        if (wait) {
//            waitScanFinished();
            waitForPendingBackgroundTasks();
        }
    }
    
    
    /**
     * Delete project
     * @param project project to be deleted
     */
    public static void deleteProject(String project) {
        deleteProject(project, false);
    }
    
    
    public static void deleteProject(String project, boolean waitStatus) {
        new DeleteAction().performAPI(ProjectsTabOperator.invoke().getProjectRootNode(project));
        
        //delete project
        NbDialogOperator deleteProject = new NbDialogOperator("Delete Project"); // NOI18N
        JCheckBoxOperator delete_sources = new JCheckBoxOperator(deleteProject);
        
        if(delete_sources.isEnabled())
            delete_sources.changeSelection(true);
        
        deleteProject.yes();
        
        waitForPendingBackgroundTasks();
        
        if(waitStatus)
            MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (clean)"); // NOI18N
        
        try {
            //sometimes dialog rises
            new NbDialogOperator("Question").yes(); // NOI18N
        }catch(Exception exc){
            System.err.println("No Question dialog rises - no problem this is just workarround!");
            exc.printStackTrace(System.err);
        }
        
    }
    
    
    
    /**
     * Build project and wait for finish
     * @param project
     */
    public static void buildProject(String project) {
        ProjectRootNode prn = ProjectsTabOperator.invoke().getProjectRootNode(project);
        prn.buildProject();
        StringComparator sc = MainWindowOperator.getDefault().getComparator();        
        MainWindowOperator.getDefault().setComparator(new Operator.DefaultStringComparator(false, true));
        MainWindowOperator.getDefault().waitStatusText("Finished building "); // NOI18N
        MainWindowOperator.getDefault().setComparator(sc);
    }
    
    /**
     * Invoke action on project node from popup menu
     * @param project
     * @param pushAction
     */
    public static void actionOnProject(String project, String pushAction) {
        ProjectRootNode prn = ProjectsTabOperator.invoke().getProjectRootNode(project);
        prn.callPopup().pushMenuNoBlock(pushAction);
    }
    
    /**
     * Run project
     * @param project
     */
    public static void runProject(String project) {
        actionOnProject(project,"Run Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("run"); // NOI18N
    }
    
    /**
     * Debug project
     * @param project
     */
    public static void debugProject(String project) {
        actionOnProject(project,"Debug Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("debug"); // NOI18N
    }
    
    
    /**
     * Test project
     * @param project
     */
    public static void testProject(String project) {
        actionOnProject(project, "Test Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("test"); // NOI18N
    }
    
    /**
     * Deploy project and wait for finish
     * @param project
     */
    public static void deployProject(String project) {
        actionOnProject(project, "Deploy Project"); // NOI18N
        waitForPendingBackgroundTasks();
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (run-deploy)"); // NOI18N
    }
    
    /**
     * Verify project and wait for finish
     * @param project
     */
    public static void verifyProject(String project) {
        actionOnProject(project, "Verify Project"); // NOI18N
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (verify)"); // NOI18N
    }
    
    
    /**
     * Open project and wait until it's scanned
     * @param projectFolder Project's location
     */
    public static void waitProjectOpenedScanFinished(String projectFolder){
        //ProjectSupport.openProject(projectFolder);
//        waitScanFinished();
    }
    
    public static void waitForPendingBackgroundTasks() {
 //       waitForPendingBackgroundTasks(5);
    }
    
/*    public static void waitForPendingBackgroundTasks(int n) {
        // wait maximum n minutes
        for (int i=0; i<n*60; i++) {
            if (org.netbeans.progress.module.Controller.getDefault().getModel().getSize()==0)
                return;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                return;
            }
        }
    }*/
    
    /**
     * Adds GlassFish V2 using path from com.sun.aas.installRoot property
     */
    public static void addApplicationServer() {
        
        String appServerPath = System.getProperty("com.sun.aas.installRoot");
        
        if (appServerPath == null) {
            throw new Error("Can't add application server. com.sun.aas.installRoot property is not set.");
        }

        String addServerMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); // Add Server...
        String addServerInstanceDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"
        //String glassFishV3ListItem = Bundle.getStringTrimmed("org.netbeans.modules.glassfish.common.nodes.Bundle", "TXT_GlassfishInstanceNode");
        String nextButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT");
        String finishButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH");

        RuntimeTabOperator rto = RuntimeTabOperator.invoke();        
        JTreeOperator runtimeTree = rto.tree();
        runtimeTree.setComparator(new Operator.DefaultStringComparator(true, true));
        
        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 6000);
        
        TreePath path = runtimeTree.findPath("Servers");
        runtimeTree.selectPath(path);
        
        try {
            //log("Let's check whether GlassFish V2 is already added");
            runtimeTree.findPath("Servers|GlassFish V2");
        } catch (TimeoutExpiredException tee) {
            //log("There is no GlassFish V2 node so we'll add it");
            
            new JPopupMenuOperator(runtimeTree.callPopupOnPath(path)).pushMenuNoBlock(addServerMenuItem);

            NbDialogOperator addServerInstanceDialog = new NbDialogOperator(addServerInstanceDialogTitle);

            new JListOperator(addServerInstanceDialog, 1).selectItem("GlassFish V2");

            new JButtonOperator(addServerInstanceDialog,nextButtonCaption).push();

            new JTextFieldOperator(addServerInstanceDialog).enterText(appServerPath);

            new JButtonOperator(addServerInstanceDialog,finishButtonCaption).push();
        }
        
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);
    }

    public static Node getTomcatServerNode(){
        RuntimeTabOperator rto = RuntimeTabOperator.invoke();

        TreePath path = null;

        JTreeOperator runtimeTree = rto.tree();
        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 6000);
        try {
            path = runtimeTree.findPath("Servers");
            runtimeTree.selectPath(path);
            path = runtimeTree.findPath("Servers|Tomcat"); // NOI18N
            runtimeTree.selectPath(path);
        } catch (Exception exc) {
            exc.printStackTrace(System.err);
            throw new Error("Cannot find Tomcat Server Node", exc);
        }
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);

        return new Node(runtimeTree,path);
    }

    public static Node getApplicationServerNode(){
        RuntimeTabOperator rto = RuntimeTabOperator.invoke();
        
        TreePath path = null;
        
        // create exactly (full match) and case sensitively comparing comparator
//        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(false, false);
//        StringComparator previousComparator = rto.tree().getComparator();
//        rto.setComparator(comparator);
        JTreeOperator runtimeTree = rto.tree();
        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 6000);
        try {
            log("Looking path = Servers");
            path = runtimeTree.findPath("Servers");
            runtimeTree.selectPath(path);
            log("Looking path = Servers|GlassFish V2");
            path = runtimeTree.findPath("Servers|GlassFish V2"); // NOI18N
            runtimeTree.selectPath(path);
        } catch (Exception exc) {
            exc.printStackTrace(System.err);
            throw new Error("Cannot find Application Server Node", exc);
        }
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);
//        rto.setComparator(previousComparator);
        return new Node(runtimeTree,path);
    }
    
    
    public static Node startApplicationServer() {
        Node node = performApplicationServerAction("Start", "Starting");  // NOI18N
        new EventTool().waitNoEvent(10000);
        return node;
    }
    
    public static Node stopApplicationServer() {
        Node node = performApplicationServerAction("Stop", "Stopping");  // NOI18N
        new EventTool().waitNoEvent(10000);
        return node;
    }

    public static Node startTomcatServer() {
        Node node = performTomcatServerAction("Start");  // NOI18N
        new EventTool().waitNoEvent(10000);
        return node;
    }

    public static Node stopTomcatServer() {
        Node node = performTomcatServerAction("Stop");  // NOI18N
        new EventTool().waitNoEvent(10000);
        return node;
    }


        public static void addTomcatServer() {

        String appServerPath = System.getProperty("tomcat.installRoot");
        
        if (appServerPath == null) {
            throw new Error("Can't add tomcat server. tomcat.installRoot property is not set.");
        }
        
        String addServerMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); // Add Server...
        String addServerInstanceDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"
        String serverItem = "Tomcat 6.0";
        String nextButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT");
        String finishButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH");

        RuntimeTabOperator rto = RuntimeTabOperator.invoke();        
        JTreeOperator runtimeTree = rto.tree();
        runtimeTree.setComparator(new Operator.DefaultStringComparator(true, true));
        
        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 6000);
        
        TreePath path = runtimeTree.findPath("Servers");
        runtimeTree.selectPath(path);
        
        try {
            runtimeTree.findPath("Servers|Tomcat 6.0");
        } catch (TimeoutExpiredException tee) {
            
            new JPopupMenuOperator(runtimeTree.callPopupOnPath(path)).pushMenuNoBlock(addServerMenuItem);
            NbDialogOperator addServerInstanceDialog = new NbDialogOperator(addServerInstanceDialogTitle);
            new JListOperator(addServerInstanceDialog,1).selectItem(serverItem);
            new JButtonOperator(addServerInstanceDialog,nextButtonCaption).push();
       
            JTextFieldOperator tfo=new JTextFieldOperator(addServerInstanceDialog,1);
            tfo.getFocus();
            tfo.enterText(appServerPath);
            new JCheckBoxOperator(addServerInstanceDialog,1).changeSelection(false);
            new JButtonOperator(addServerInstanceDialog,finishButtonCaption).push();
        }
        
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);

    }

    private static Node performTomcatServerAction(String action) {
        Node asNode = getTomcatServerNode();
        asNode.select();
        new EventTool().waitNoEvent(10000);
        String serverIDEName = asNode.getText();
        log("ServerNode name = "+serverIDEName);
        JPopupMenuOperator popup = asNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for Tomcat server node ");
        }
        boolean startEnabled = popup.showMenuItem(action).isEnabled();
        if(startEnabled) {
            popup.pushMenuNoBlock(action);
        }
        return asNode;
    }

    /**
     * Invoke action on Application server node (start/stop/...)
     * @param action Action to be invoked on the Application server node
     */
    private static Node performApplicationServerAction(String action, String message) {
        Node asNode = getApplicationServerNode();
        asNode.select();
        new EventTool().waitNoEvent(10000);
        String serverIDEName = asNode.getText();
        log("ServerNode name = "+serverIDEName);
        JPopupMenuOperator popup = asNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for Application server node ");
        }
        boolean startEnabled = popup.showMenuItem(action).isEnabled();
        if(startEnabled) {
            popup.pushMenuNoBlock(action);
        }
      
        return asNode;
    }
    

    public static void waitProjectTasksFinished() {
        String status=MainWindowOperator.getDefault().getStatusText();
        boolean tasks=true;
        
        for (int i=0;i<50;i++) {
            tasks=true;
            while(tasks) {
                if (status.equals("Indexing")||status.equals("Compiling")||status.equals("Collecting")||status.equals("Scanning")||status.equals("Opening"))
                {System.err.println("+++>"+status);}
                else {tasks=false;}
            }
          new QueueTool().waitEmpty(1000);  
        }    
    }
    
    /**
     * Wait finished scan - repeatedly
     */
    public static void waitScanFinished(){
        try {
            new QueueTool().waitEmpty();
        } catch (TimeoutExpiredException tee) {            
            getLog().println("The following exception is ignored");
            tee.printStackTrace(getLog());
        }
     }
    
    public static void initLog(PerformanceTestCase testCase) {
        test = testCase;
    }
    public static void closeLog() {
        test = null;
    }
    private static void log(String logMessage) {
        System.out.println("Utilities::"+logMessage);
        if( test != null  ) { test.log("Utilities::"+logMessage); }
    }
    private static PrintStream getLog() {
        if( test != null  ) { 
            return test.getLog(); 
        } else {
            return System.out;
        }
    }
    
    public static void killRunOnProject(String project) {
        killProcessOnProject(project, "run");
    }
    
    public static void killDebugOnProject(String project) {
        killProcessOnProject(project, "debug");
    }
    
    private static void killProcessOnProject(String project, String process) {
        // prepare Runtime tab
        RuntimeTabOperator runtime = RuntimeTabOperator.invoke();
        
        // kill the execution
        Node node = new Node(runtime.getRootNode(), "Processes|"+project+ " (" + process + ")");
        node.select();
        node.performPopupAction("Terminate Process");
    }
    
    public static void xmlTestResults(String path, String suite, String name, String classname, String sname, String unit, String pass, long threshold, long[] results, int repeat) {

        PrintStream out = System.out;

        System.out.println();
        System.out.println("#####  Results for "+name+"   #####");
        System.out.print("#####        [");
        for(int i=1;i<=repeat;i++)             
            System.out.print(results[i]+"ms, ");
        System.out.println("]");
        for (int i=1;i<=name.length()+27;i++)
            System.out.print("#");
        System.out.println();
        System.out.println();

        path=System.getProperty("nbjunit.workdir");
        File resGlobal=new File(path+File.separator+"allPerformance.xml");

        try {
            dbf=DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
         } catch (Exception ex) {
            ex.printStackTrace (  ) ;
        }

        if (!resGlobal.exists()) {
            try {
                resGlobal.createNewFile();
                out = new PrintStream(new FileOutputStream(resGlobal));
                out.print("<TestResults>\n");
                out.print("</TestResults>");
                out.close();
            } catch (IOException ex) {
            ex.printStackTrace (  ) ;
            }
         }

        try {
              allPerfDoc = db.parse(resGlobal);
            } catch (Exception ex) {
            ex.printStackTrace (  ) ;
            }
            
        testResultsTag=allPerfDoc.getDocumentElement();

        testTag=null;
        for (int i=0;i<allPerfDoc.getElementsByTagName("Test").getLength();i++) {
            if (("name=\""+name+"\"").equalsIgnoreCase( allPerfDoc.getElementsByTagName("Test").item(i).getAttributes().getNamedItem("name").toString() ) ) {
                testTag =(Element)allPerfDoc.getElementsByTagName("Test").item(i);
                break;
            }
        }

        if (testTag!=null) {
            for (int i=1;i<=repeat;i++) {
                perfDataTag=allPerfDoc.createElement("PerformanceData");
                if (i==1) perfDataTag.setAttribute("runOrder", "1");
                    else perfDataTag.setAttribute("runOrder", "2");
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }
        else {
            testTag=allPerfDoc.createElement("Test");
            testTag.setAttribute("name", name);
            testTag.setAttribute("unit", unit);
            testTag.setAttribute("results", pass);
            testTag.setAttribute("threshold", new Long(threshold).toString());
            testTag.setAttribute("classname", classname);
            for (int i=1;i<=repeat;i++) {
                perfDataTag=allPerfDoc.createElement("PerformanceData");
                if (i==1) perfDataTag.setAttribute("runOrder", "1");
                    else perfDataTag.setAttribute("runOrder", "2");
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }

            testSuiteTag=null;
            for (int i=0;i<allPerfDoc.getElementsByTagName("Suite").getLength();i++) {
                if (suite.equalsIgnoreCase(allPerfDoc.getElementsByTagName("Suite").item(i).getAttributes().getNamedItem("suitename").getNodeValue())) {
                    testSuiteTag =(Element)allPerfDoc.getElementsByTagName("Suite").item(i);
                    break;
                }
            }

            if (testSuiteTag==null) {
                testSuiteTag=allPerfDoc.createElement("Suite");
                testSuiteTag.setAttribute("name", sname);
                testSuiteTag.setAttribute("suitename", suite);
                testSuiteTag.appendChild(testTag);
            } else {
                testSuiteTag.appendChild(testTag);
            }

        testResultsTag.appendChild(testSuiteTag);


        try {
            out = new PrintStream(new FileOutputStream(resGlobal));
        } catch (FileNotFoundException ex) {
        }

        Transformer tr=null;
        try {
            tr = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
        }

        tr.setOutputProperty(OutputKeys.INDENT, "no");
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource docSrc = new DOMSource(allPerfDoc);
        StreamResult result = new StreamResult(out);

        try {
            tr.transform(docSrc, result);
        } catch (TransformerException ex) {
        }
        out.close();
    }

    public static void processUnitTestsResults(String className, PerformanceData pd) {
        long[] result=new long[2];
        result[1]=pd.value;
        CommonUtilities.xmlTestResults(System.getProperty("nbjunit.workdir"), "Unit Tests Suite", pd.name, className, className, pd.unit, "passed", 120000 , result, 1);
    }
}
