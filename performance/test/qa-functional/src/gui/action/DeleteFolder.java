/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import java.io.File;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

//TODO temporary
import gui.action.RefreshFolderAction;
//TODO import org.netbeans.jellytools.actions.RefreshFolderAction;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.modules.java.settings.JavaSettings;

/**
 * Test of Delete nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class DeleteFolder extends testUtilities.PerformanceTestCase {
    
    /** Projects tab */
    private static ProjectsTabOperator projectTab;
    
    /** Name of the folder which test creates and expands */
    private static String folderToBeDeleted;
    
    /** Fodlers node represantation*/
    private static Node foldersNode;
    
    /** Turn On/Off prescan sources */
    private boolean prescanSources;
    
    /** Node represantation of the folder which test creates and delete */
    private static Node nodeToBeDeleted;
    
    /** File represantation of the folder which test creates and delete */
    private static File folderToBeDeletedFile;
    
    
    /**
     * Creates a new instance of DeleteFolder
     * @param testName the name of the test
     */
    public DeleteFolder(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of DeleteFolder
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public DeleteFolder(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    
    public void testDeleteFolderWith50JavaFiles(){
        WAIT_AFTER_PREPARE = 10000;
        WAIT_AFTER_OPEN = 10000;
        folderToBeDeleted = "javaFolder50";
        doMeasurement();
    }
    
    public void testDeleteFolderWith100JavaFiles(){
        WAIT_AFTER_PREPARE = 15000;
        WAIT_AFTER_OPEN = 15000;
        folderToBeDeleted = "javaFolder100";
        doMeasurement();
    }
    
    public void initialize(){
        org.netbeans.junit.ide.ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/"+"PerformanceTestFoldersData");
        org.netbeans.junit.ide.ProjectSupport.waitScanFinished();
        projectTab = new ProjectsTabOperator();
        projectTab.maximize();
        
        projectTab.getProjectRootNode("PerformanceTestData").collapse();
        projectTab.getProjectRootNode("jEdit").collapse();
        
        addClassNameToLookFor("explorer.view");
        setPrintClassNames(true);
        turnOff();
    }
        
    
    
    public ComponentOperator open(){
        new DeleteAction().performPopup(nodeToBeDeleted);
        new NbDialogOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.explorer.Bundle","MSG_ConfirmDeleteObjectTitle")).yes();
        return null;
    }
    
    public void close(){
        //do nothing
    }
    
    public void prepare(){
        try {
            String projectPath = System.getProperty("xtest.tmpdir")+"/"+"PerformanceTestFoldersData";
            log("========== Projects path =" + projectPath);
            
            File foldersDir = new File(projectPath + "/" + "src" + "/" + "folders");
            log("========== Folders path ="+foldersDir.getPath());
            
            
            File originalDir = new File(foldersDir, folderToBeDeleted);
            File copyDeleteDir = new File(foldersDir, folderToBeDeleted + "_delete");
            copyDeleteDir.mkdir();
            
            log("========== Original Dir path =" + originalDir.getPath());
            log("========== Copy Delete Dir path =" + copyDeleteDir.getPath());

            gui.Utilities.copyFile(new File (foldersDir, "Test.java"),new File (copyDeleteDir, "Test.java"));
            waitNoEvent(1000);
            
            ProjectRootNode projectNode = projectTab.getProjectRootNode("PerformanceTestFoldersData");
            
            foldersNode = new Node(projectNode, gui.Utilities.SOURCE_PACKAGES + "|folders");
            
            new RefreshFolderAction().perform(foldersNode); // foldersNode.performPopupAction("Refresh Folder");
            waitNoEvent(1000);

            nodeToBeDeleted = new Node(projectNode, gui.Utilities.SOURCE_PACKAGES + "|folders." + folderToBeDeleted + "_delete");
            
            File[] files = originalDir.listFiles();
            log("=============== There is [" + files.length + "] number of files in directory = "+ originalDir.getPath());
            
            File copyFile;
            
            for(int i=0; i<files.length; i++) {
                copyFile = new File(copyDeleteDir,files[i].getName());
                log("================== Create file ="+copyFile.getPath());
                
                gui.Utilities.copyFile(files[i], copyFile);
            }
            
            new RefreshFolderAction().perform(nodeToBeDeleted); // nodeToBeDeleted.performPopupAction("Refresh Folder");
            nodeToBeDeleted.expand();
            
        }catch(Exception exc){
            log("========================================= \nImpossible create files: "+exc.getMessage());
            exc.printStackTrace(getLog());
            log("\n=========================================");
        }
    }

    public void shutdown() {
        org.netbeans.junit.ide.ProjectSupport.closeProject("PerformanceTestFoldersData");
        projectTab.restore();
        setPrintClassNames(false);
        turnBack();
    }
    
    /**
     * turn off prescanning of sources
     */
    protected void turnOff() {
        prescanSources = JavaSettings.getDefault().getPrescanSources();
        JavaSettings.getDefault().setPrescanSources(false);
    }

    /**
     * turn back on prescanning of sources
     */
    protected void turnBack() {
        JavaSettings.getDefault().setPrescanSources(prescanSources);
    }
    
}
