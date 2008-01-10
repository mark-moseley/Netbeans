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

        suite.addTest(new SwitchToFile("testSwitchJavaToJava", "Switch from Java file to Java file"));
        suite.addTest(new SwitchToFile("testSwitchJavaToJSP", "Switch from Java file to JSP file"));
        suite.addTest(new SwitchToFile("testSwitchJSPToJSP", "Switch from JSP file to JSP file"));
        suite.addTest(new SwitchToFile("testSwitchJSPToXML", "Switch from JSP file to XML file"));
        suite.addTest(new SwitchToFile("testSwitchXMLToJSP", "Switch from XML file to JSP file"));

        suite.addTest(new SwitchView("testSwitchToProjects", "Switch to Projects view"));
        suite.addTest(new SwitchView("testSwitchToFiles", "Switch to Files view"));
        suite.addTest(new SwitchView("testSwitchToServices", "Switch to Services view"));
        suite.addTest(new SwitchView("testSwitchToFavorites", "Switch to Favorite Folders view"));
        
        suite.addTest(new OpenFiles("testOpening20kBJavaFile", "Open Java file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBJavaFile", "Open Java file (20kB) if Editor opened"));
        
        suite.addTest(new OpenFiles("testOpening20kBTxtFile", "Open Txt file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBTxtFile", "Open Txt file (20kB) if Editor opened"));
        
        suite.addTest(new OpenFiles("testOpening20kBXmlFile", "Open Xml file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBXmlFile", "Open Xml file (20kB) if Editor opened"));
        
//TODO no tomcat - see issue 101104         suite.addTest(new OpenJspFile("testOpening20kBJSPFile", "Open JSP file"));
//TODO no tomcat - see issue 101104         suite.addTest(new OpenJspFileWithOpenedEditor("testOpening20kBJSPFile", "Open JSP file if Editor opened"));
        
        suite.addTest(new OpenFilesNoCloneableEditor("testOpening20kBPropertiesFile", "Open Properties file (20kB)"));
        suite.addTest(new OpenFilesNoCloneableEditorWithOpenedEditor("testOpening20kBPropertiesFile", "Open Properties file (20kB) if Editor opened"));
        
        suite.addTest(new OpenFormFile("testOpening20kBFormFile", "Open Form file (20kB)"));
        suite.addTest(new OpenFormFileWithOpenedEditor("testOpening20kBFormFile", "Open Form file (20kB) if Editor opened"));
        
//TODO 6.0 still causes a lot of failures in the following tests        suite.addTest(new PasteInEditor("measureTime", "Paste in the editor"));
        suite.addTest(new PageUpPageDownInEditor("testPageUp", "Press Page Up in the editor"));
        suite.addTest(new PageUpPageDownInEditor("testPageDown", "Press Page Down in the editor"));
        
        suite.addTest(new JavaCompletionInEditor("measureTime", "Invoke Code Completion dialog in Editor"));
        
        suite.addTest(new TypingInEditor("testJavaEditor", "Type a character in Java Editor"));
        suite.addTest(new TypingInEditor("testTxtEditor", "Type a character in Txt Editor"));
//TODO no tomcat - see issue 101104        suite.addTest(new TypingInEditor("testJspEditor", "Type a character in Jsp Editor"));
        
        
        suite.addTest(new CloseEditor("testClosing20kBJavaFile", "Close Java file (20kB)"));
        suite.addTest(new CloseEditor("testClosing20kBFormFile", "Close Form file (20kB)"));
        
//TODO 5.0 still causes a lot of failures in the following tests        suite.addTest(new CloseAllEditors("testClosingAllJavaFiles", "Close All Documents if 10 Java files opened"));
        
        suite.addTest(new CloseEditorTab("measureTime", "Close on tab from Editor window"));
        
        suite.addTest(new CloseEditorModified("measureTime", "Close modified Java file"));
        
        suite.addTest(new SaveModifiedFile("measureTime", "Save modified Java file"));
        
        suite.addTest(new SelectCategoriesInNewFile("testSelectGUIForms","Select GUI Forms in New File"));
        suite.addTest(new SelectCategoriesInNewFile("testSelectXML","Select XML in New File"));
        suite.addTest(new SelectCategoriesInNewFile("testSelectOther","Select Other in New File"));
        
        //TODO    suite.addTest(new OpenProject("testOpenJavaApplicationProject", "Open Java Application project"));
        //TODO    suite.addTest(new OpenProject("testOpenJavaLibraryProject", "Open Java Library project"));
        //TODO    suite.addTest(new OpenProject("testOpenWebApplicationProject", "Open Web Application project"));
        //TODO    suite.addTest(new OpenProject("testOpenJavaProjectWithExistingSources", "Open Java Project with Existing sources"));

        suite.addTest(new AddToFavorites("testAddJavaFile", "Add to Favorites Java file"));
        
        suite.addTest(new CreateProject("testCreateJavaApplicationProject", "Create Java Application project"));
        suite.addTest(new CreateProject("testCreateJavaLibraryProject", "Create Java Library project"));
//TODO no tomcat - see issue 101104        
	suite.addTest(new CreateProject("testCreateWebApplicationProject", "Create Web Application project"));

//TODO    suite.addTest(new CreateProject("testCreateJavaProjectWithExistingSources", "Create Java Project with Existing sources"));
        
        suite.addTest(new CreateNBProject("testCreateModuleProject", "Create Module Project"));
        suite.addTest(new CreateNBProject("testCreateModuleSuiteProject", "Create Module Suite Project"));
        
        
//TODO 6.0 still causes a lot of failures in the following tests        suite.addTest(new DeleteFolder("testDeleteFolderWith50JavaFiles", "Delete folder with 50 java files"));
//TODO 6.0 still causes a lot of failures in the following tests        suite.addTest(new DeleteFolder("testDeleteFolderWith100JavaFiles", "Delete folder with 100 java files"));
        
        /*
        suite.addTest(new RefactorFindUsages("measureTime", "Refactor find usages"));

        
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
*/        


        suite.addTest(new OpenFiles("testGC", "GC of opened editors"));
        suite.addTest(new CreateProject("testGC", "GC of created projects"));
        
        return suite;
    }
    
}
