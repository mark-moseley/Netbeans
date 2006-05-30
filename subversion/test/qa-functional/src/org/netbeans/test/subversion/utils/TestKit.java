/*
 * TestKit.java
 *
 * Created on 10 May 2006, 15:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author peter
 */
public final class TestKit {
    public final static String MODIFIED_COLOR = "#0000FF";
    public final static String NEW_COLOR = "#008000";
    public final static String CONFLICT_COLOR = "#FF0000";
    public final static String IGNORED_COLOR = "#999999";
    
    public final static String MODIFIED_STATUS = "[Modified]";
    public final static String NEW_STATUS = "[New]";
    public final static String CONFLICT_STATUS = "[Conflict]";
    public final static String IGNORED_STATUS = "[Ignored]";
    public final static String UPTODATE_STATUS = "";
    
    public static File prepareProject(String category, String project, String project_name) throws Exception {
        //create temporary folder for test
        String folder = "work";
        File file = new File("/tmp", folder); // NOI18N
        file.mkdirs();
        RepositoryMaintenance.deleteFolder(file);
        file.mkdirs();
        //PseudoVersioned project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory(category);
        npwo.selectProject(project);
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso, 1).setText(file.getAbsolutePath()); // NOI18N
        new JTextFieldOperator(npnlso, 0).setText(project_name); // NOI18N
        //new JTextFieldOperator(npnlso, 2).setText(folder); // NOI18N
        new NewProjectWizardOperator().finish();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(project_name);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        return file;
    }
    
    public static String getColor(String nodeHtmlDisplayName) {
        
        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1)
            return "";
        int hashPos = nodeHtmlDisplayName.indexOf('#');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(hashPos);
        hashPos = nodeHtmlDisplayName.indexOf('"');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(0, hashPos);
        return nodeHtmlDisplayName;
    }
    
    public static String getStatus(String nodeHtmlDisplayName) {
        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1)
            return "";
        String status;
        int pos1 = nodeHtmlDisplayName.indexOf('[');
        int pos2 = nodeHtmlDisplayName.indexOf(']');
        if ((pos1 != -1) && (pos2 != -1))
            status = nodeHtmlDisplayName.substring(pos1, pos2 + 1);
        else 
            status = "";
        return status;
    }

    public static void removeAllData(String projectName) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        rootNode.performPopupActionNoBlock("Delete Project");
        NbDialogOperator ndo = new NbDialogOperator("Delete");
        JCheckBoxOperator cb = new JCheckBoxOperator(ndo, "Also");
        cb.setSelected(true);
        ndo.yes();
        ndo.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        ndo.waitClosed(); 
        //TestKit.deleteRecursively(file);
    }
    
    public static int compareThem(Object[] expected, Object[] actual, boolean sorted) {
        int result = 0;
        if (expected == null || actual == null)
            return -1;
        if (sorted) {
            if (expected.length != actual.length) {
                return -1;
            }
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
        } else {
            if (expected.length > actual.length) {
                return -1;
            }
            boolean found = false;
            for (int i = 0; i < expected.length; i++) {
                for (int j = 0; j < actual.length; j++) {
                    if (((String) expected[i]).equals((String) actual[j])) {
                        result++;
                        j = actual.length;
                    }
                }
            }
            return result;
        }
        return result; 
    }
    
    public static void createNewElements(String projectName, String packageName, String name) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().typeText(packageName);
        nfnlso.finish();
        
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().typeText(name);
        nfnlso.selectPackage(packageName);
        nfnlso.finish();
    }  
    
    public static void createNewPackage(String projectName, String packageName) { 
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().typeText(packageName);
        nfnlso.finish();
    }    
    
    public static void createNewElement(String projectName, String packageName, String name) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().typeText(name);
        nfnlso.selectPackage(packageName);
        nfnlso.finish();
    }    
    
    public static void copyTo(String source, String destination) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source)); 
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination));
            boolean available = true;
            byte[] buffer = new byte[1024];
            int size;
            try {    
                while (available) {
                    size = bis.read(buffer);
                    if (size != -1) {
                        bos.write(buffer, 0, size);
                    } else {
                        available = false;
                    }                      
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bos.flush();
                bos.close();
                bis.close();
            }   
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
    
    public static void printLogStream(PrintStream stream, String message) {
        if (stream != null) {
            stream.println(message);
        }
    }
}
