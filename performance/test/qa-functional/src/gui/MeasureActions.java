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

package gui;


import org.netbeans.junit.NbTestSuite;
import gui.action.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureActions  {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTest(new ExpandNodesProjectsView("testExpandProjectNode", "Expand Project node"));
        suite.addTest(new ExpandNodesProjectsView("testExpandSourcePackagesNode", "Expand Source Packages node"));
        suite.addTest(new ExpandNodesProjectsView("testExpandFolderWith50JavaFiles", "Expand folder with 50 java files"));
        suite.addTest(new ExpandNodesProjectsView("testExpandFolderWith100JavaFiles", "Expand folder with 100 java files"));
        suite.addTest(new ExpandNodesProjectsView("testExpandFolderWith100TxtFiles", "Expand folder with 100 txt files"));
        suite.addTest(new ExpandNodesProjectsView("testExpandFolderWith100XmlFiles", "Expand folder with 100 xml files"));

        suite.addTest(new OpenFiles("testOpening20kBJavaFile", "Open Java file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBJavaFile", "Open Java file (20kB) if Editor opened"));

        suite.addTest(new OpenFiles("testOpening20kBTxtFile", "Open Txt file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBTxtFile", "Open Txt file (20kB) if Editor opened"));
        
        suite.addTest(new OpenFiles("testOpening20kBXmlFile", "Open Xml file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBXmlFile", "Open Xml file (20kB) if Editor opened"));
        
//TODO Rises NPE now        suite.addTest(new OpenJspFile("testOpening20kBJSPFile", "Open JSP file"));
//TODO Rises NPE now        suite.addTest(new OpenJspFileWithOpenedEditor("testOpening20kBJSPFile", "Open JSP file if Editor opened"));
        
        suite.addTest(new OpenFilesNoCloneableEditor("testOpening20kBPropertiesFile", "Open Properties file (20kB)"));
        suite.addTest(new OpenFilesNoCloneableEditorWithOpenedEditor("testOpening20kBPropertiesFile", "Open Properties file (20kB) if Editor opened"));
        
        suite.addTest(new OpenFilesNoCloneableEditor("testOpening20kBPictureFile", "Open Picture file (20kB)"));
        suite.addTest(new OpenFilesNoCloneableEditorWithOpenedEditor("testOpening20kBPictureFile", "Open Picture file (20kB) if Editor opened"));
        
//TODO Form designer isn't TopComponent        suite.addTest(new OpenFormFile("testOpening20kBFormFile", "Open Form file (20kB)"));
//TODO Form designer isn't TopComponent        suite.addTest(new OpenFormFileWithOpenedEditor("testOpening20kBFormFile", "Open Form file (20kB) if Editor opened"));        

        suite.addTest(new PasteInEditor("measureTime", "Paste in the editor"));
        suite.addTest(new PageUpPageDownInEditor("measureTime", "Press Page Up in the editor", true));
        suite.addTest(new PageUpPageDownInEditor("measureTime", "Press Page Down in the editor", false));
        
        suite.addTest(new JavaCompletionInEditor("measureTime", "Invoke Code Completion dialog in Editor"));
        suite.addTest(new TypingInEditor("measureTime", "Type a character in Editor"));

        suite.addTest(new CloseEditor("testClosing20kBJavaFile", "Close Java file (20kB)"));
//TODO Form designer isn't TopComponent        suite.addTest(new CloseEditor("testClosing20kBFormFile", "Close Form file (20kB)"));
        suite.addTest(new CloseAllEditors("testClosingAllJavaFiles", "Close All Documents if 10 Java files opened"));
        
        suite.addTest(new CloseEditorTab("testClosingTab", "Close on tab from Editor window"));
        
        suite.addTest(new CloseEditorModified("testClosingModifiedJavaFile", "Close modified Java file"));
        
        suite.addTest(new SaveModifiedFile("testSaveModifiedJavaFile", "Save modified Java file"));

//TODO List of tempaltes is divided to two panels        suite.addTest(new ExpandNodesInNewFile("testExpandJavaClasses","Expand node Java Classes in New File"));
        suite.addTest(new ExpandNodesInNewFile("testExpandGUIForms","Expand node GUI Forms in New File"));
//TODO List of tempaltes is divided to two panels        suite.addTest(new ExpandNodesInNewFile("testExpandXML","Expand node XML in New File"));
//TODO List of tempaltes is divided to two panels        suite.addTest(new ExpandNodesInNewFile("testExpandOther","Expand node Other in New File"));

        suite.addTest(new ExpandNodesInOptions("testExpandEditorSettings", "Expand node Editor Settings in Tools | Options"));
        
        suite.addTest(new ExpandNodesInComponentInspector("testExpandContainerJFrame","Expand Container node in Component Inspector"));

        suite.addTest(new RefreshFolder("testRefreshFolderWith50JavaFiles", "Refresh folder with 50 java files"));
        suite.addTest(new RefreshFolder("testRefreshFolderWith100JavaFiles", "Refresh folder with 100 java files"));
        suite.addTest(new RefreshFolder("testRefreshFolderWith100XmlFiles", "Refresh folder with 100 xml files"));
        suite.addTest(new RefreshFolder("testRefreshFolderWith100TxtFiles", "Refresh folder with 100 txt files"));
        
        suite.addTest(new DeleteFolder("testDeleteFolderWith50JavaFiles", "Delete folder with 50 java files"));
        suite.addTest(new DeleteFolder("testDeleteFolderWith100JavaFiles", "Delete folder with 100 java files"));
        
//TODO    suite.addTest(new CreateProject("testCreateJavaApplicationProject", "Create Java Application project"));
//TODO    suite.addTest(new CreateProject("testCreateJavaLibraryProject", "Create Java Library project"));
//TODO    suite.addTest(new CreateProject("testCreateWebApplicationProject", "Create Web Application project"));
//TODO    suite.addTest(new CreateProject("testCreateJavaProjectWithExistingSources", "Create Java Project with Existing sources"));
        
//TODO    suite.addTest(new OpenProject("testOpenJavaApplicationProject", "Open Java Application project"));
//TODO    suite.addTest(new OpenProject("testOpenJavaLibraryProject", "Open Java Library project"));
//TODO    suite.addTest(new OpenProject("testOpenWebApplicationProject", "Open Web Application project"));
//TODO    suite.addTest(new OpenProject("testOpenJavaProjectWithExistingSources", "Open Java Project with Existing sources"));
        
//TODO    suite.addTest(new CloseProject("testCloseJavaApplicationProject", "Close Java Application project"));
        
//TODO    suite.addTest(new SetMainProject("testSetMainProject", "Set Main project"));
        
//TODO    suite.addTest(new BuildProject("testBuildHelloWorld", "Build Hello World project"));
//TODO    suite.addTest(new CleanAndBuildProject("testCleanAndBuildHelloWorld", "Clean and Build Hello World project"));
//TODO    suite.addTest(new RunProject("testRunHelloWorld", "Run Hello World project"));
        
//TODO    suite.addTest(new CreateNewJavaFile("testCreateNewJavaFile", "Create New Java file"));
//TODO    suite.addTest(new CreateNewJavaFileIfEditorOpened("testCreateNewJavaFileIfEditorOpened", "Create New Java file if Editor opened"));
//TODO    suite.addTest(new CreateNewPackage("testCreateNewPackage", "Create New Package"));
        
//TODO    suite.addTest(new SelectDocumentIn("testSelectDocumentInProjects", "Select Document In Projects"));
//TODO    suite.addTest(new SelectDocumentIn("testSelectDocumentInFiles", "Select Document In Files"));
//TODO    suite.addTest(new SelectDocumentIn("testSelectDocumentInAllFiles", "Select Document In All Files"));
        
//TODO    suite.addTest(new SwitchToFile("testSwitchEditorJavaToJava", "Switch editor Java to Java file"));
//TODO    suite.addTest(new SwitchToFile("testSwitchEditorJavaToForm", "Switch editor Java to Form file"));
//TODO    suite.addTest(new SwitchToFile("testSwitchEditorFormToJava", "Switch editor Form to Java file"));

//TODO    suite.addTest(new SwitchView("testSwitchToProjects", "Switch to Projects view"));
//TODO    suite.addTest(new SwitchView("testSwitchToFiles", "Switch to Files view"));
//TODO    suite.addTest(new SwitchView("testSwitchToAllFiles", "Switch to All Files view"));
//TODO    suite.addTest(new SwitchView("testSwitchToFavoriteFolders", "Switch to Favorite Folders view"));
        
//TODO    suite.addTest(new SlidingWindows("testMinimizeProjects", "Minimize Projects window"));
//TODO    suite.addTest(new SlidingWindows("testSlideProjects", "Slide Projects window"));
//TODO    suite.addTest(new SlidingWindows("testAutoHideProjects", "Auto Hide Projects window"));

        
        
//TODO    suite.addTest(new CompileFile("testCompileFile", "Compile Java file"));
        
//TODO    suite.addTest(new AddToFavorites("testAddToFavoritesFolders", "Add to Favorites folders"));

        
        return suite;
    }
    
}
