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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.java.gui;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.operators.*;

import java.io.File;
import javax.swing.tree.TreeModel;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.test.java.Utilities;

//import org.netbeans.junit.ide.ProjectSupport;

/**
 * Common utilities for GUI tests.
 * @author Roman Strobl
 */
public class GuiUtilities {
    
    private static final String JAVA_BUNDLE_PATH = "org.netbeans.modules.java.project.Bundle";

    /**
     * Creates a testing project.
     * @param projectName name of project
     * @param workDir working directory for project
     * @return path to project directory
     */
    public static String createProject(String projectName, String workDir) {
        NewProjectWizardOperator opc = NewProjectWizardOperator.invoke();
        
        // close project if it exits (it was not closed during last test)
        ProjectsTabOperator pto = new ProjectsTabOperator();
        JTreeOperator tree = pto.tree();        
    
        TreeModel tm = tree.getModel();
        Object root = tm.getRoot();
        for (int i=0; i<tm.getChildCount(root); i++) {
            // if project is opened, close it, it may be opened several times
            // by mistake, so go through all cases
            if ((tm.getChild(root, i).toString().equals(projectName)) ||
            (tm.getChild(root, i).toString().equals(projectName+" [Main]"))) {
                    Node pn = new ProjectsTabOperator().getProjectRootNode(
                            tm.getChild(root, i).toString());
                    pn.select();
                    pn.performPopupAction(
                            org.netbeans.jellytools.Bundle.getString(
                            "org.openide.nodes.Bundle", "Button_close"));
                    i--; 
                    Utilities.takeANap(1000);
            }            
        }
        
        // delete workdir if it exists (it was not deleted during last test)
        File f = new File(workDir);
        if (f.exists()) {
            Utilities.deleteDirectory(f);
        }
        
        // wait till all fields are loaded
        JDialogOperator jdo = new JDialogOperator(
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewProjectWizard_Title"));
        JTreeOperator jto = new JTreeOperator(jdo, 0);
        
        boolean exitLoop = false;
        System.out.println("Waiting for 'General'");
        for (int i=0; i<20; i++) {
            System.out.println("Round "+i);
            Utilities.takeANap(2000);
            for (int j=0; j<jto.getChildCount(jto.getRoot()); j++) {
                if (jto.getChild(jto.getRoot(), j).toString() == Bundle.getString(
                        "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                        "Templates/Project/Standard")) {
                    exitLoop = true;
                    System.out.println("General found");
                    break;
                }
            }
            if (exitLoop) break;
        }
        
        new Node(jto, Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                "Templates/Project/Standard")).select();
        // java project
        opc.selectProject(Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                "Templates/Project/Standard/emptyJ2SE.xml"));
        opc.next();
        
        // set project name, no main class, created in workdir
        NewProjectNameLocationStepOperator npnlso = new
                NewProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(projectName);
        npnlso.cbCreateMainClass().setSelected(false);
        npnlso.txtProjectLocation().setText(workDir);
        npnlso.finish();
        
        String projectDir = workDir+"/"+projectName;
        
        //ProjectSupport.waitScanFinished();
        
        // "Scanning Project Classpaths" - NO MORE IN 4.2
        /* String titleScanning = Bundle.getString(
                "org.netbeans.modules.javacore.Bundle",
                "TXT_ApplyingPathsTitle");
        NbDialogOperator scanningDialogOper = new NbDialogOperator(titleScanning);
        
        // scanning can last for a long time => wait max. 5 minutes
        scanningDialogOper.getTimeouts().setTimeout(
                "ComponentOperator.WaitStateTimeout", 300000);
        scanningDialogOper.waitClosed(); */
        
        // wait project appear in projects view
        new EventTool().waitNoEvent(3000);
        new ProjectsTabOperator().getProjectRootNode(projectName);
        
        return projectDir;
    }
    
    
    /**
     * Creates a testing package.
     * @param projectName name of project
     * @param packageName name of created package
     */
    public static void createPackage(String projectName, String packageName) {       
        // create a new package
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                projectName);
        pn.select();
        
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
                        Bundle.getString("org.netbeans.modules.java.project.Bundle",
                        "Templates/Classes")) {
                    exitLoop = true;
                    break;
                }
            }
            if (exitLoop) break;
            Utilities.takeANap(1000);
        }
        
        op.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes"));
        op.selectFileType(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes/Package"));
        op.next();
        
        JTextFieldOperator tfp = new JTextFieldOperator(op, 0);
        tfp.setText(packageName);
        
        // set package name
        NewFileNameLocationStepOperator nfnlso =
                new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().setText(packageName);
        for (int i=0; i<10; i++) {
            if (nfnlso.btFinish().isEnabled()) break;
            Utilities.takeANap(1000);
        }
        nfnlso.finish();
        
        // wait for package to appear
        Node sample1Node = new Node(new SourcePackagesNode(projectName),
                packageName);
    }
    
    /**
     * Creates a testing class.
     * @param projectName name of project
     * @param packageName name of package
     * @param className name of created class
     */
    public static void createClass(String projectName, String packageName,
            String className) {
        Node pn2 = new ProjectsTabOperator().getProjectRootNode(
                projectName);
        pn2.select();
        
        // create testing class
        NewFileWizardOperator op2 = NewFileWizardOperator.invoke();
        
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
        
        op2.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes"));
        op2.selectFileType(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes/Class.java"));
        op2.next();
        
        JTextFieldOperator tf = new JTextFieldOperator(op2);
        tf.setText(className);
        
        op2.finish();
        
        // wait for class to appear
        Node sample2Node = new Node(new Node(new SourcePackagesNode(
                projectName), packageName), className);
    }

    /**
     * Creates a testing interface.
     * @param projectName name of project
     * @param packageName name of package
     * @param ifaceName name of interface
     */
    public static void createInterface(String projectName, String packageName,
            String ifaceName) {
        Node pn2 = new ProjectsTabOperator().getProjectRootNode(
                projectName);
        pn2.select();
        
        // create testing class
        NewFileWizardOperator op2 = NewFileWizardOperator.invoke();
        
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
        
        op2.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes"));
        op2.selectFileType(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes/Interface.java"));
        op2.next();
        
        JTextFieldOperator tf = new JTextFieldOperator(op2);
        tf.setText(ifaceName);
        
        op2.finish();
        
        // wait for class to appear
        Node sample2Node = new Node(new Node(new SourcePackagesNode(
                projectName), packageName), ifaceName);
    }
    
    /**
     * Creates a testing project, package and class.
     * @param projectName name of created project
     * @param packageName name of created package
     * @param className name of created class
     * @param workDir working directory for project
     * @return path to project directory
     */
    public static String createProjectAndPackageAndClass(String projectName,
            String packageName, String className, String workDir) {
        String projectDir = createProject(projectName, workDir);
        createPackage(projectName, packageName);
        createClass(projectName, packageName, className);
        return projectDir;
    }
    
    /**
     * Deletes testing project including files on hard drive.
     * @param projectName name of project to delete
     * @param className name of class to close
     * @param workDir working directory of project
     * @param closeFile should opened class be closed
     */
    public static void deleteProject(String projectName, String className,
            String workDir, boolean closeFiles) {
        
        if (closeFiles) {
            EditorOperator.closeDiscardAll();
        }
                
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                projectName);
        pn.select();
        
        pn.performPopupAction(org.netbeans.jellytools.Bundle.getString(
                "org.openide.nodes.Bundle", "Button_close"));
        
        /* delete project - commented out because it causes exceptions
        File f = new File(workDir);
        Utilities.deleteDirectory(f);*/
    }
    
    /**
     * Waits for a child node to be shown in the IDE. Needed for test
     * stabilization on slow machines.
     * @param parentPath full path for parent, | used as a delimiter
     * @param childName name of the child node
     * @param projectName name of project
     */
    public static void waitForChildNode(String projectName, String parentPath,
            String childName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        Node parent = new Node(prn, parentPath);
        final String finalFileName = childName;
        try {
            // wait for max. 30 seconds for the file node to appear
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 30000);
            new Waiter(new Waitable() {
                public Object actionProduced(Object parent) {
                    return ((Node)parent).isChildPresent(finalFileName) ?
                        Boolean.TRUE: null;
                }
                public String getDescription() {
                    return("Waiting for the tree to load.");
                }
            }).waitAction(parent);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }
    
}
