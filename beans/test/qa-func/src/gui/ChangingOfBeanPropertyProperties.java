/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui;

import java.io.*;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.TextFieldProperty;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;

import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


public class ChangingOfBeanPropertyProperties  extends JellyTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_INDEX_PROPERTY     = "indexProperty";
    private static final String NAME_NON_INDEX_PROPERTY = "nonIndexProperty";
    
    private static final String sampleDir = Utilities.findFileSystem("src").getDisplayName();
    
    /** Need to be defined because of JUnit */
    public ChangingOfBeanPropertyProperties(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ChangingOfBeanPropertyProperties("testChangePropertyNameAndType"));
        suite.addTest(new ChangingOfBeanPropertyProperties("testChangeMode"));
        suite.addTest(new ChangingOfBeanPropertyProperties("testDeleteAnyPropertiesAndEvents"));
        suite.addTest(new ChangingOfBeanPropertyProperties("testChangeSourceCode"));
        suite.addTest(new ChangingOfBeanPropertyProperties("testChangeOfStyleOfDeclaredVariable"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new ChangingOfBeanPropertyProperties("testChangeMode"));
    }
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        if (!getName().equals("testChangeSourceCode") && !getName().equals("testDeleteAnyPropertiesAndEvents")) {
            
            FileObject testFile = Repository.getDefault().findResource("gui/data/" + NAME_TEST_FILE + ".java");
            FileObject destination = Repository.getDefault().findFileSystem(sampleDir.replace('\\', '/')).getRoot();
            
            try {
                DataObject.find(testFile).copy(DataFolder.findFolder(destination));
            } catch (IOException e) {
                fail(e);
            }
        }
    }
    
    /** tearDown method */
    public void tearDown() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
        
        Utilities.delete(NAME_TEST_FILE + ".java");
    }
    
    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = this.property_Value
     *  - add a new property
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = _property_Value
     *  - add a new property
     */
    public void testChangeOfStyleOfDeclaredVariable() {
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
        mainWindowOper.switchToEditingWorkspace();
        
        
        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectOption(Bundle.getString("org.netbeans.core.Bundle", "UI/Services/Editing")+ "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Menu"));
        PropertySheetTabOperator propertySheetTabOperator = new PropertySheetTabOperator(optionsOperator);
        new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "MSG_Option_Gen_This"));
        
        new EventTool().waitNoEvent(3000);
        
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("firstName");
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("int");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(2000);
        nbDialogOperator.ok();
        
        new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "MSG_Option_Gen_Undescored"));
        new EventTool().waitNoEvent(3000);
        //////////////////
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("secondName");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(2000);
        nbDialogOperator.ok();
        optionsOperator.close();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        new EventTool().waitNoEvent(500);
        ref(eo.getText());
        compareReferenceFiles();
        
        
    }
    
    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = 0
     *  - add a new property with an initial value
     *  - change of property type a name
     */
    public void testChangePropertyNameAndType() {
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
        mainWindowOper.switchToEditingWorkspace();
        
        
        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectOption(Bundle.getString("org.netbeans.core.Bundle", "UI/Services/Editing")+ "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Menu"));
        PropertySheetTabOperator propertySheetTabOperator = new PropertySheetTabOperator(optionsOperator);
        new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue("this.property_Value");
        
        new EventTool().waitNoEvent(3000);
        
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("initialName");
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("initialType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(2000);
        nbDialogOperator.ok();
        
        new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue("_property_Value");
        new EventTool().waitNoEvent(3000);
        optionsOperator.close();
        /////////////////
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"initialName");
        patternsNode.select();

        propertySheetTabOperator = new PropertySheetTabOperator(explorerOperator);
        new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_name")).setValue("requiredName");

        String questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        new EventTool().waitNoEvent(1500);
        nbDialogOperator.yes();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();
        
    }
    
    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = this.property_Value
     *  - add a new property
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = _property_Value
     *  - Add a new property
     *  - Change of the first property mode to Read Only
     *  - Change of the second property mode to Write Only
     */
    public void testChangeMode() {
        //
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
        mainWindowOper.switchToEditingWorkspace();
        
        
        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectOption(Bundle.getString("org.netbeans.core.Bundle", "UI/Services/Editing")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Menu"));
        PropertySheetTabOperator propertySheetTabOperator = new PropertySheetTabOperator(optionsOperator);
        new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue("this.property_Value");
        
        new EventTool().waitNoEvent(3000);
        
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("firstName");
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("int");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1000);
        nbDialogOperator.ok();
        
        new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue("_property_Value");
        new EventTool().waitNoEvent(1000);
        optionsOperator.close();
        //////////////////
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("secondName");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1000);
        nbDialogOperator.ok();
        ////
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"firstName");
        patternsNode.select();
        new EventTool().waitNoEvent(1000);
        propertySheetTabOperator = new PropertySheetTabOperator(explorerOperator);
        //new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadOnlyMODE"));
        new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).setValue(1);
        new EventTool().waitNoEvent(1000);
        String questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        new EventTool().waitNoEvent(1000);
        nbDialogOperator.yes();
        
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"secondName");
        patternsNode.select();
        new EventTool().waitNoEvent(1000);
        propertySheetTabOperator = new PropertySheetTabOperator(explorerOperator);
        //new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_WriteOnlyMODE"));
        new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).setValue(2);
        new EventTool().waitNoEvent(1000);
        questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        new EventTool().waitNoEvent(1000);
        nbDialogOperator.yes();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();
        
        ////
        
    }
    
    public void testChangeSourceCode() {
        
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans/Bean.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_TEST_FILE);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.setCaretPosition(1,1);
        eo.insert("    private static final String PROP_MY_PROPERTY = \"MyProperty\";\n", 16, 1);
        new EventTool().waitNoEvent(500);
        
        eo.insert("    private String myProperty;\n", 19, 1);
        new EventTool().waitNoEvent(500);
        
        eo.insert("    public String getMyProperty() {\n", 38, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("        return myProperty;\n", 39, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("    }\n", 40, 1);
        new EventTool().waitNoEvent(500);
        
        eo.insert("    public void setMyProperty(String value) {\n", 42, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("        String oldValue = myProperty;\n", 43, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("        myProperty = value;\n", 44, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("        propertySupport.firePropertyChange(PROP_MY_PROPERTY, oldValue, myProperty);\n", 45, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("    }\n", 46, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("\n", 47, 1);
        new EventTool().waitNoEvent(500);
        
        explorerOperator.selectPageFilesystems();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"myProperty");
        patternsNode.select();
        new EventTool().waitNoEvent(1000);
        PropertySheetTabOperator propertySheetTabOperator = new PropertySheetTabOperator(explorerOperator);
        
        assertEquals("Estimated Field" ,"String myProperty",new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_estimatedField")).getValue());
        assertEquals("Getter" ,"getMyProperty ()",new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_getter")).getValue());
        assertEquals("Mode" ,Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE") ,new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).getValue());
        assertEquals("Name of Property","myProperty",new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_name")).getValue());
        assertEquals("Setter","setMyProperty (String)",new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_setter")).getValue());
        assertEquals("Type","String",new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_type")).getValue());
    }
    
    
    private void createContent() {
        // Start - NonIndexProperty
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        // End - NonIndexProperty
        // Start - IndexProperty
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetterCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niGetterCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niReturnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        // End - IndexProperty
        // Start - UnicastEventSource
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");
        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_implRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        
        new EventTool().waitNoEvent(1500);
        
        nbDialogOperator.ok();
        // End - UnicastEventSource
        // Start - MulticastEventSourceArrayListImpl
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ItemListener");
        
        jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        
        new EventTool().waitNoEvent(1500);
        
        nbDialogOperator.ok();
        // End - MulticastEventSourceArrayListImpl
        // Start - MulticastEventSourceEventListenerListImpl
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.FocusListener");
        
        jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        
    }
    
    public void testDeleteAnyPropertiesAndEvents() {
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans/Bean.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_TEST_FILE);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,6);
        new DeleteAction().performAPI(eo);
        new EventTool().waitNoEvent(1500);
        eo.select(3,6);
        
        new DeleteAction().performAPI(eo);
        //        ref(eo.getText());
        //        compareReferenceFiles();
        try {
            File workDir = getWorkDir();
            (new File(workDir,"testDeleteAnyPropertiesAndEventsInitial.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testDeleteAnyPropertiesAndEventsInitial.ref")));
            out.print(eo.getText());
            out.close();
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        compareReferenceFiles("testDeleteAnyPropertiesAndEventsInitial.ref", "testDeleteAnyPropertiesAndEventsInitial.pass", "testDeleteAnyPropertiesAndEventsInitial.diff");
        
        createContent();
        
        // Delete nonIndexProperty
        
        JavaNode patternsNode = new JavaNode(sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+NAME_NON_INDEX_PROPERTY);
        patternsNode.select();
        patternsNode.delete();
        
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        String questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        NbDialogOperator nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();
        
         patternsNode.waitNotPresent();
        // Delete indexProperty
         JavaNode patternsNode2 = new JavaNode(sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+NAME_INDEX_PROPERTY);
         patternsNode2.select();
         patternsNode2.delete();
 
        confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        new EventTool().waitNoEvent(1500);
        questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();
        new EventTool().waitNoEvent(2500);
        
        patternsNode2.waitNotPresent();
        // Delete action listener
        JavaNode patternsNode3 = new JavaNode(sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"actionListener");
        patternsNode3.select();
        patternsNode3.delete();
        
        confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        new EventTool().waitNoEvent(1500);
        questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();
        new EventTool().waitNoEvent(2500);

        patternsNode3.waitNotPresent();
        // Delete focus listener
        JavaNode patternsNode4 = new JavaNode(sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"focusListener");
        patternsNode4.select();
        patternsNode4.delete();

        confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        new EventTool().waitNoEvent(1500);
        questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();
        new EventTool().waitNoEvent(2500);
        patternsNode4.waitNotPresent();
        try {
            File workDir = getWorkDir();
            (new File(workDir,"testDeleteAnyPropertiesAndEventsModified.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testDeleteAnyPropertiesAndEventsModified.ref")));
            out.print(eo.getText());
            out.close();
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        compareReferenceFiles("testDeleteAnyPropertiesAndEventsModified.ref", "testDeleteAnyPropertiesAndEventsModified.pass", "testDeleteAnyPropertiesAndEventsModified.diff");
        
    }
    
}
