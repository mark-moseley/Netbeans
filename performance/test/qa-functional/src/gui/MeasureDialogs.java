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
import gui.window.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureDialogs  {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        // dialogs and windows which don't require any preparation
        suite.addTest(new About("measureTime", "About dialog open"));
        suite.addTest(new About_2("measureTime", "About details open"));
        suite.addTest(new SetupWizard("measureTime", "Setup Wizard open"));
        suite.addTest(new SetupWizard_2("measureTime", "Setup Wizard next open"));
        suite.addTest(new SetupWizard_3("measureTime", "Setup Wizard next next open"));
        suite.addTest(new KeyboardShortcuts("measureTime", "Keyboard Shortcut dialog open"));
        suite.addTest(new KeyboardShortcuts_2("measureTime", "Keyboard Shortcut shortcuts open"));
        suite.addTest(new Options("measureTime", "Options dialog open"));
        suite.addTest(new NewProjectDialog("measureTime", "New Project dialog open"));
        suite.addTest(new NewFileDialog("measureTime", "New File dialog open"));
        suite.addTest(new OpenProjectDialog("measureTime", "Open Project dialog open"));
        suite.addTest(new OpenFileDialog("measureTime", "Open File dialog open"));
        suite.addTest(new UpdateCenter("measureTime", "Update Center wizard open"));
        suite.addTest(new ProxyConfiguration("measureTime", "Proxy Configuration open"));
        suite.addTest(new VersioningManager("measureTime", "Versioning Manager open"));
        
        suite.addTest(new FilesWindow("measureTime", "Files window open"));
        suite.addTest(new ProjectsWindow("measureTime", "Projects window open"));
        suite.addTest(new FavoritesWindow("measureTime", "Projects window open"));
        suite.addTest(new RuntimeWindow("measureTime", "Runtime window open"));
        suite.addTest(new VersioningWindow("measureTime", "Versioning window open"));
        
//TODO it still fails in Promo D       suite.addTest(new OutputWindow("measureTime", "Output window open"));
        suite.addTest(new ToDoWindow("measureTime", "To Do window open"));
        suite.addTest(new HttpMonitorWindow("measureTime", "Http Monitor window open"));
        suite.addTest(new HelpContentsWindow("measureTime", "Help Contents window open"));
        suite.addTest(new PropertyEditorString("measureTime", "String Property Editor open"));
        suite.addTest(new PropertyEditorColor("measureTime", "Color Property Editor open"));
        suite.addTest(new AddJDBCDriverDialog("measureTime", "Add JDBC Driver dialog open"));
        suite.addTest(new NewDatabaseConnectionDialog("measureTime", "New Database Connection dialog open"));
        suite.addTest(new AddNewServerInstanceDialog("measureTime", "Add Server Instance dialog open"));
        suite.addTest(new SetDefaultServerDialog("measureTime", "Set Default Server dialog open"));
        suite.addTest(new MountXMLCatalogDialog("measureTime", "Mount XML Catalog dialog open"));
        suite.addTest(new FindInProjects("measureTime", "Find in Projects dialog open"));
        suite.addTest(new ProjectPropertiesWindow("measureTime", "Project Properties window open"));
 
        suite.addTest(new AttachDialog("measureTime", "Attach dialog open"));
        suite.addTest(new NewBreakpointDialog("measureTime", "New Breakpoint dialog open"));
        suite.addTest(new NewWatchDialog("measureTime", "New Watch dialog open"));

//TODO Javadoc Index Search isn't TopComponent        suite.addTest(new JavadocIndexSearch("measureTime", "Javadoc Index Search open"));
        
//TODO is doesn't work after refactoring merge        suite.addTest(new CodeCompletionDatabaseManager("measureTime", "Code Completion Database Manager open"));
        suite.addTest(new GotoClassDialog("measureTime", "Go To Class dialog open"));
        suite.addTest(new JavaPlatformManager("measureTime", "Java Platform Manager open"));
        suite.addTest(new LibrariesManager("measureTime", "Libraries Manager open"));
        
        // dialogs and windows which first open a file in the editor
        suite.addTest(new ImportManagementWizard("measureTime", "Import Management Tool open"));
        suite.addTest(new OverrideMethods("measureTime", "Override and Implement Methods dialog open"));
        suite.addTest(new GotoLineDialog("measureTime", "Go to Line dialog open"));
        suite.addTest(new AutoCommentWindow("measureTime", "Auto Comment Tool open"));
        suite.addTest(new EditorProperties("measureTime", "Editor Properties open"));
        suite.addTest(new FindInSourceEditor("measureTime", "Find in Source Editor dialog open"));
        suite.addTest(new InternationalizeDialog("measureTime", "Internationalize dialog open"));
        
        suite.addTest(new DocumentsDialog("measureTime", "Documents dialog open"));
        
        return suite;
    }
    
}
