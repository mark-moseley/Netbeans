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
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.modules.java.settings.JavaSettings;


/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ExpandNodesProjectsView extends testUtilities.PerformanceTestCase {
    
    /** Name of the folder which test creates and expands */
    private static String project;
    
    /** Path to the folder which test creates and expands */
    private static String pathToFolderNode;
    
    /** Node represantation of the folder which test creates and expands */
    private static Node nodeToBeExpanded;
    
    /** Projects tab */
    private static ProjectsTabOperator projectTab;
    
    /** Turn On/Off prescan sources */
    private boolean prescanSources;
    
    /** Project with data for these tests */
    private static String testDataProject = "PerformanceTestFoldersData";
    
    /**
     * Creates a new instance of ExpandNodesInExplorer
     * @param testName the name of the test
     */
    public ExpandNodesProjectsView(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of ExpandNodesInExplorer
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandNodesProjectsView(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    

    public void testExpandProjectNode(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = "jEdit";
        pathToFolderNode = "";
        doMeasurement();
    }

    public void testExpandSourcePackagesNode(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = "jEdit";
        pathToFolderNode = "Source Packages";
        doMeasurement();
    }
    
    public void testExpandFolderWith50JavaFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = testDataProject;
        pathToFolderNode = "Source Packages|javaFolder50";
        doMeasurement();
    }
    
    public void testExpandFolderWith100JavaFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = testDataProject;
        pathToFolderNode = "Source Packages|javaFolder100";
        doMeasurement();
    }
    
    public void testExpandFolderWith100XmlFiles(){
        WAIT_AFTER_OPEN = 2000;
        WAIT_AFTER_PREPARE = 500;
        project = testDataProject;
        pathToFolderNode = "Source Packages|xmlFolder100";
        doMeasurement();
    }
    
    public void testExpandFolderWith100TxtFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 500;
        project = testDataProject;
        pathToFolderNode = "Source Packages|txtFolder100";
        doMeasurement();
    }
    
    
    public void initialize(){
        org.netbeans.junit.ide.ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/"+testDataProject);
        org.netbeans.junit.ide.ProjectSupport.waitScanFinished();
        projectTab = new ProjectsTabOperator();
        projectTab.maximize();
        
        projectTab.getProjectRootNode("jEdit").collapse();
        projectTab.getProjectRootNode("PerformanceTestFoldersData").collapse();
        
        turnOff();
        addClassNameToLookFor("explorer.view");
        setPrintClassNames(true);
    }
        
        
    public void prepare() {
        nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
    }
    
    public ComponentOperator open(){
        nodeToBeExpanded.tree().clickOnPath(nodeToBeExpanded.getTreePath(), 2);
//        nodeToBeExpanded.tree().clickMouse(2);
//        nodeToBeExpanded.waitExpanded();
        nodeToBeExpanded.expand();
        return null;
    }
    
    public void close(){
        nodeToBeExpanded.collapse();
    }
    
    protected void shutdown() {
        turnBack();
        org.netbeans.junit.ide.ProjectSupport.closeProject(testDataProject);
        projectTab.restore();
    }

    protected void turnOff() {
        // turn off prescanning of sources
        prescanSources = JavaSettings.getDefault().getPrescanSources();
        JavaSettings.getDefault().setPrescanSources(false);
    }
    
    protected void turnBack() {
        // turn back on prescanning of sources
        JavaSettings.getDefault().setPrescanSources(prescanSources);
    }
    
}
