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

package gui;

import java.io.File;
import java.io.IOException;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RepositoryTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


public class NewMulticastEventSource extends JellyTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
            
    private static final String sampleDir = Utilities.findFileSystem("src").getDisplayName();
    
        
    /** Need to be defined because of JUnit */
    public NewMulticastEventSource(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new NewMulticastEventSource("testGenerateEmpty"));        
        suite.addTest(new NewMulticastEventSource("testGenerateArrayListImplementation"));        
        suite.addTest(new NewMulticastEventSource("testGenerateEventListenerListImplementation"));        
        suite.addTest(new NewMulticastEventSource("testGenerateEventFiringMethods"));        
        suite.addTest(new NewMulticastEventSource("testPassEventAsParameter"));                                
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new BeansTemplates("testJavaBean"));
    }
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        
        FileObject testFile = Repository.getDefault().findResource("gui/data/" + NAME_TEST_FILE + ".java");
        FileObject destination = Repository.getDefault().findFileSystem(sampleDir.replace('\\', '/')).getRoot();
        
        try {
            DataObject.find(testFile).copy(DataFolder.findFolder(destination));
        } catch (IOException e) {
            fail(e);
        }
        new PropertiesAction().perform();
    }
    
    /** tearDown method */
    public void tearDown() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
        
        Utilities.delete(NAME_TEST_FILE + ".java");
    }

     public void testGenerateEmpty() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_emptyRadioButton"));
        jRadioButtonOperator.push();        

        new EventTool().waitNoEvent(2000);
                               
        nbDialogOperator.ok();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

    
     public void testGenerateArrayListImplementation() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton"));
        jRadioButtonOperator.push();        

        new EventTool().waitNoEvent(2000);
                               
        nbDialogOperator.ok();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

     public void testGenerateEventListenerListImplementation() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton"));
        jRadioButtonOperator.push();        

        new EventTool().waitNoEvent(3000);
                               
        nbDialogOperator.ok();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }
     
     public void testGenerateEventFiringMethods() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton"));
        jRadioButtonOperator.push();        
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();

        new EventTool().waitNoEvent(3000);
                               
        nbDialogOperator.ok();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

     public void testPassEventAsParameter() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton"));
        jRadioButtonOperator.push();        
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();

        new EventTool().waitNoEvent(3000);
                               
        nbDialogOperator.ok();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

}