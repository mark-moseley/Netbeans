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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.io.File;
import java.io.IOException;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.RepositoryTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;

import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


public class CreateNewIndexedProperty extends JellyTestCase {

    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_INDEX_PROPERTY = "indexProperty";
    private static final String NAME_WRONG = "123";
    private static final String TYPE_WRONG = "+++";

    private static final String sampleDir = Utilities.findFileSystem("src").getDisplayName();
    
    /** Need to be defined because of JUnit */
    public CreateNewIndexedProperty(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateNewIndexedProperty("testName"));        
        suite.addTest(new CreateNewIndexedProperty("testType"));        
        suite.addTest(new CreateNewIndexedProperty("testMode"));        
        suite.addTest(new CreateNewIndexedProperty("testBound"));        
        suite.addTest(new CreateNewIndexedProperty("testConstrained"));        
        suite.addTest(new CreateNewIndexedProperty("testGenerateField"));        
        suite.addTest(new CreateNewIndexedProperty("testGenerateReturnStatement"));        
        suite.addTest(new CreateNewIndexedProperty("testGenerateSetStatement"));     
        suite.addTest(new CreateNewIndexedProperty("testGenerateNonIndexedGetterWithReturnStatement"));     
        suite.addTest(new CreateNewIndexedProperty("testGenerateIndexedSetter"));             
        suite.addTest(new CreateNewIndexedProperty("testGeneratePropertyChangeSupport"));        
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
        // redirect jemmy trace and error output to a log
        System.out.println("########  "+getName()+"  #######");

        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectOption(Bundle.getString("org.netbeans.core.Bundle", "UI/Services/Editing")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Menu"));
        PropertySheetOperator propertySheetTabOperator = new PropertySheetOperator(optionsOperator);
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "MSG_Option_Gen_This"));
        
        FileObject testFile = Repository.getDefault().findResource("gui/data/" + NAME_TEST_FILE + ".java");
        FileObject destination = Repository.getDefault().findFileSystem(sampleDir.replace('\\', '/')).getRoot();
        optionsOperator.close();
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

    
    /** testName method */
    public void testName() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);

       
        jTextFieldOperator.typeText(NAME_WRONG);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("String");
        
        nbDialogOperator.ok();

        new EventTool().waitNoEvent(3000);

        new NbDialogOperator("Error").ok();
                              
        jTextFieldOperator.clearText();
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);
        
        jComboBoxOperator.setSelectedItem("String");
                       
        nbDialogOperator.ok();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();               
    }

    /** testType method */
    public void testType() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText(TYPE_WRONG);
        
        nbDialogOperator.ok();

        new EventTool().waitNoEvent(3000);

        new NbDialogOperator("Error").ok();
                              
        jTextFieldOperator.clearText();
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);
        jComboBoxOperator.clearText();
        jComboBoxOperator.setSelectedItem("Double");
                       
        nbDialogOperator.ok();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                       
    }
    
    /** testMode method */
    public void testMode() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText("first");        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("int");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read Only");
        nbDialogOperator.ok();
        
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));        
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText("second");        

        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("double");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Write Only");
        nbDialogOperator.ok();

        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));        
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText("third");        

        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("long");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        nbDialogOperator.ok();
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                       
    }
    
    /** testBound method */
    public void testBound() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, 0);
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        nbDialogOperator.ok();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                       
    }

    /** testConstrained method */
    public void testConstrained() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        nbDialogOperator.ok();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                       
    }

    /** testGenerateField method */
    public void testGenerateField() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        nbDialogOperator.ok();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                       
    }
    
    /** testGenerateReturnStatement method */
    public void testGenerateReturnStatement() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        nbDialogOperator.ok();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                       
    }

    /** testGenerateSetStatement method */
    public void testGenerateSetStatement() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        nbDialogOperator.ok();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                        
    }

     /** testGenerateNonIndexedGetterWithReturnStatement method */
    public void testGenerateNonIndexedGetterWithReturnStatement() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niGetterCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niReturnCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        nbDialogOperator.ok();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                        
    }
   
    /** testGenerateIndexedSetter method */
    public void testGenerateIndexedSetter() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetterCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(3000);
        nbDialogOperator.ok();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
                
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                        
    }
                
    /** testGeneratePropertyChangeSupport method */
    public void testGeneratePropertyChangeSupport() {
//
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);        
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add"));
        
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1000);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_supportCheckBox"));
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

