/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;

/**
 * Test of opening files if Editor is already opened.
 * OpenFiles is used as a base for tests of opening files
 * when editor is already opened.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFilesWithOpenedEditor extends OpenFiles {
    
    /** Name of file to pre-open */
    public static String fileName_preopen;
    
    /**
     * Creates a new instance of OpenFilesWithOpenedEditor
     * @param testName the name of the test
     */
    public OpenFilesWithOpenedEditor(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of OpenFilesWithOpenedEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesWithOpenedEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public void testOpening20kBJavaFile(){
        WAIT_AFTER_OPEN = 1500;
        setJavaEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        fileName_preopen = "Main.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void testOpening20kBTxtFile(){
        WAIT_AFTER_OPEN = 1000;
        setPlainTextEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "textfile20kB.txt";
        fileName_preopen = "textfile.txt";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void testOpening20kBXmlFile(){
        WAIT_AFTER_OPEN = 1000;
        setXMLEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "xmlfile20kB.xml";
        fileName_preopen = "xmlfile.xml";
        menuItem = EDIT;
        doMeasurement();
    }
    
    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    public void initialize(){
        super.initialize();
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|" + fileName_preopen));
    }
    
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenFilesWithOpenedEditor("testOpening20kBTxtFile"));
    }
    
}
