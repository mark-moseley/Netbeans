/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.mbeanwizard;
import java.io.File;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.JellyToolsHelper;
import org.netbeans.modules.jmx.test.helpers.JellyConstants;
import org.netbeans.modules.jmx.test.helpers.MBean;
import java.util.ArrayList;
import java.util.Properties;



/**
 *
 * @author an156382
 */
public class CreateOperationWrapperMBean extends JellyTestCase {
    
    /** Need to be defined because of JUnit */
    public CreateOperationWrapperMBean(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateOperationWrapperMBean("createClass"));
        suite.addTest(new CreateOperationWrapperMBean("constructTest15MBean"));
        suite.addTest(new CreateOperationWrapperMBean("constructTest16MBean"));
        
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public void setUp() {
        JellyToolsHelper.grabProjectNode(JellyConstants.PROJECT_NAME);
    }
    
    public void tearDown() {
        
    }
    
    public void createClass() {
        createMissingClass(JellyConstants.PROJECT_NAME, 
                JellyConstants.OPCLASS_TO_WRAP, JellyConstants.PACKAGE_NAME);
    }
    
    public void constructTest15MBean() {
        
        MBean wrapper3 = createThirdWrapperMBean();
        wizardExecution(wrapper3);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest16MBean() {
        
        MBean wrapper4 = createFourthWrapperMBean();
        wizardExecution(wrapper4);
        
        //TODO Diff between generated and master files
        
    }
    
  //========================= WizardExecution =================================//
    
    private void wizardExecution(MBean mbean) {
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init(JellyConstants.CATEGORY, 
                mbean.getMBeanName(), mbean.getMBeanPackage());
        nfwo.next();
        
        ArrayList<String> fileNames = optionStep(nfwo, mbean);
        nfwo.next();
        nfwo.next();
        
        operationStep(nfwo, mbean);
        nfwo.next();
      
        nfwo.next();
      
        ArrayList<String> unitFileNames = junitStep(nfwo, mbean);
        nfwo.finish();
        
        assertTrue(JellyToolsHelper.diffOK(fileNames, unitFileNames, mbean)); 
    }
   
    //========================= Class Name generation ===========================//
    
    private MBean createThirdWrapperMBean() {
        return new MBean(
                JellyConstants.MBEAN_FIFTEEN, 
                JellyConstants.PACKAGE_NAME, 
                JellyConstants.MBEAN_FIFTEEN_COMMENT,
                JellyConstants.PACKAGE_NAME+JellyConstants.PT+
                JellyConstants.OPCLASS_TO_WRAP);
    }
    
    private MBean createFourthWrapperMBean() {
        return new MBean(
                JellyConstants.MBEAN_SIXTEEN, 
                JellyConstants.PACKAGE_NAME, 
                JellyConstants.MBEAN_SIXTEEN_COMMENT,
                JellyConstants.PACKAGE_NAME+JellyConstants.PT+
                JellyConstants.OPCLASS_TO_WRAP);
    }
   
     //========================= Utility class generation  ===========================//
     
     public void createMissingClass(String projectName, String fileName, 
            String packageName) {
        //creates a reference on the current wizard
        NewFileWizardOperator nfwoForFile = NewFileWizardOperator.invoke();
        
        //select the config wizard
        nfwoForFile.selectProject(projectName);
        nfwoForFile.selectCategory(JellyConstants.JAVA_FILE_CATEG);
        nfwoForFile.selectFileType(JellyConstants.JAVA_FILE_TYPE);
        nfwoForFile.next();
        
        NewFileNameLocationStepOperator nfnlsoForFile =
                new NewFileNameLocationStepOperator();
        
        //sets the file name
        nfnlsoForFile.setObjectName(fileName);
        JTextField folder = JellyToolsHelper.findFolderJTextField(fileName, 
                nfnlsoForFile.getContentPane());
        folder.setText(packageName);
        
        nfwoForFile.finish();
        
        //grabs project node
        ProjectsTabOperator pto = new ProjectsTabOperator();
        
        JTreeOperator tree = pto.tree();
        ProjectRootNode prn = pto.getProjectRootNode(JellyConstants.PROJECT_NAME);
        
        prn.select();
        Node node = new Node(prn, JellyConstants.SRC_PKG + packageName + 
                JellyConstants.PIPE + fileName);
        
        node.select();
        
        EditorOperator eo = new EditorOperator(fileName);
        eo.deleteLine(16);
        eo.deleteLine(16);
        eo.deleteLine(16);
        eo.deleteLine(16);
        eo.deleteLine(16);
        eo.setCaretPositionToLine(16);
  
        eo.insert(createWrapperClass(fileName));
        eo.save();
    }
     
    private String createWrapperClass(String fileName) {
        return "public class " + fileName + " extends Super" +fileName+ " { \n" +
                "\t" + operation0() +
                "\t" + operation1() +
                "\t" + operation2() +
                "} \n \n" +
               createWrapperSuperClass("Super"+fileName)+ "\n ";
    }
    
    private String createWrapperSuperClass(String fileName) {
        return  "class " +fileName+ " { \n" +
                "\t public Integer op2(Object[] s, int t) { \n" +
                "\t return new Integer(0); \n" +
                "\t } \n"+
                "} \n";
    }
    
    private String operation0() {
        return "public void op0() throws java.lang.IllegalStateException {} \n";
    }
    
    private String operation1() {
        return  "public boolean op1(java.util.List l) { \n" +
                "\t return false; \n" +
                "\t } \n";
    }
    
    private String operation2() {
        return  "public Integer op2(String[] s, int t) { \n" +
                "\t return new Integer(0); \n" +
                "\t } \n";
    }
     
    //========================= Class Name generation ===========================//
    
    private String getCompleteGeneratedFileName(NewFileWizardOperator nfwo) {
        return JellyToolsHelper.getTextFieldContent(JellyConstants.GENFILE_TXT, nfwo);
    }
    
    private String getClassName(String completeFileName, String mbeanName) {
        return JellyToolsHelper.replaceMBeanClassName(completeFileName, mbeanName+
                JellyConstants.JAVA_EXT);
    }
    
    private String getInterfaceName(String completeFileName) {
        String itfWithExtension = completeFileName.substring(
                completeFileName.lastIndexOf(File.separatorChar)+1);
        return itfWithExtension.substring(0, itfWithExtension.lastIndexOf('.'));
    }
    
    //========================= Panel discovery ==================================//
    
    private ArrayList<String> optionStep(NewFileWizardOperator nfwo, MBean mbean) {
        
        ArrayList<String> fileNames = new ArrayList<String>();
        
        JellyToolsHelper.changeCheckBoxSelection(JellyConstants.EXISTINGCLASS_CBX, nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.EXISTINGCLASS_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled(JellyConstants.EXISTINGCLASS_TXT, nfwo));
        JellyToolsHelper.tempo(1000);
        JellyToolsHelper.setTextFieldContent(JellyConstants.EXISTINGCLASS_TXT, nfwo, mbean.getClassToWrap());
        JellyToolsHelper.tempo(1000);
        assertEquals(mbean.getClassToWrap(), 
                JellyToolsHelper.getTextFieldContent(JellyConstants.EXISTINGCLASS_TXT, nfwo));
        
        assertTrue(JellyToolsHelper.verifyRadioButtonSelected(mbean.getMBeanType(), nfwo));
        assertTrue(checkMBeanTypeButtons(nfwo, mbean));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.IMPLEMMBEAN_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.IMPLEMMBEAN_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.PREREGPARAM_CBX, nfwo));
        JellyToolsHelper.tempo(1000);
        JellyToolsHelper.setTextFieldContent(JellyConstants.MBEANDESCR_TXT, nfwo,
                mbean.getMBeanComment());
        JellyToolsHelper.tempo(1000);
        assertEquals(mbean.getMBeanComment(), JellyToolsHelper.getTextFieldContent(
                JellyConstants.MBEANDESCR_TXT, nfwo));
        
        // get the generated file name for campare with master files
        String completeGeneratedFileName = getCompleteGeneratedFileName(nfwo);
        String className = getClassName(completeGeneratedFileName, mbean.getMBeanName());
        String itfName = getInterfaceName(completeGeneratedFileName);
        
        fileNames.add(completeGeneratedFileName);
        fileNames.add(className);
        fileNames.add(itfName);
        
        return fileNames;
    }
    
    private void operationStep(NewFileWizardOperator nfwo, MBean mbean) {
      
        assertTrue(JellyToolsHelper.verifyTableEnabled(JellyConstants.W_OPER_TBL,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.W_OPER_REM_BTN,nfwo));
        JTable opTable = JellyToolsHelper.getPanelTable(JellyConstants.W_OPER_TBL, nfwo);
        JTableOperator jto = JellyToolsHelper.getPanelTableOperator(JellyConstants.W_OPER_TBL, nfwo);
        
        if (mbean.getMBeanName().equals(JellyConstants.MBEAN_SIXTEEN)) {
           // check/uncheck attributes to keep
            JTableMouseDriver mouseDriver = new JTableMouseDriver(); 
            uncheckOperation(mouseDriver, jto, JellyConstants.VOID_TYPE);
            uncheckOperation(mouseDriver, jto, JellyConstants.BOOL_TYPE);
            uncheckOperation(mouseDriver, jto, JellyConstants.INT_TYPE_FULL);
            
        } else {
            int op0Line = verifyOperationChecked(jto, JellyConstants.VOID_TYPE);
            int op1Line = verifyOperationChecked(jto, JellyConstants.BOOL_TYPE);
            int op2Line = verifyOperationChecked(jto, JellyConstants.INT_TYPE_FULL);
            
            // comments on parameter table popups
            jto.editCellAt(op1Line, JellyConstants.PARAM_COL);
            JellyToolsHelper.fillWrappedParameterComment(jto, opTable, 0, JellyConstants.W_PA_COMMENT1);
            jto.editCellAt(op2Line, JellyConstants.PARAM_COL);
            JellyToolsHelper.fillWrappedParameterComment(jto, opTable, 0,JellyConstants.W_PA_COMMENT2);
            jto.editCellAt(op2Line, JellyConstants.PARAM_COL);
            JellyToolsHelper.fillWrappedParameterComment(jto, opTable, 1,JellyConstants.W_PA_COMMENT3);
            
            // comments on exception table popups
            jto.editCellAt(op0Line, JellyConstants.EXCEP_COL);
            JellyToolsHelper.fillWrappedExceptionComment(jto, opTable, 0, JellyConstants.W_EX_COMMENT1);
        }
    }
    
    private void uncheckOperation(JTableMouseDriver mouseDriver, JTableOperator jto, String name) {
        int index = jto.findCellRow(name);
        mouseDriver.selectCell(jto, index, JellyConstants.EXPOSE_COL); 
        assertFalse(JellyToolsHelper.getTableCheckBoxValue(jto, index, JellyConstants.EXPOSE_COL));
    }
    
    private int verifyOperationChecked(JTableOperator jto, String name) {
        int index = jto.findCellRow(name);
        assertTrue(JellyToolsHelper.getTableCheckBoxValue(jto, index, JellyConstants.EXPOSE_COL));
        
        return index;
    }
    
    private ArrayList<String> junitStep(NewFileWizardOperator nfwo, MBean mbean) {
        
        ArrayList<String> unitFileNames = new ArrayList<String>();
        
        JellyToolsHelper.changeCheckBoxSelection(JellyConstants.JU_CBX, nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.JU_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.JU_CBX, nfwo));
        
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled(JellyConstants.CLASSTOTEST_TXT, nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable(JellyConstants.CLASSTOTEST_TXT, nfwo));
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled(JellyConstants.TESTCLASS_TXT, nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable(JellyConstants.TESTCLASS_TXT, nfwo));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.DEFMETHBODY_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.DEFMETHBODY_CBX, nfwo));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.JAVADOC_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.JAVADOC_CBX, nfwo));
        
        // this txt filed contains the full path of the junit test test file to be created
        String completeGeneratedTestFileName = JellyToolsHelper.getTextFieldContent(
                JellyConstants.GENUNITFILE_TXT,nfwo);
        
        // this string contains only the junit test file name with extension
        String junitFileNameWithExtension = completeGeneratedTestFileName.substring(
                completeGeneratedTestFileName.lastIndexOf(File.separatorChar)+1);
        
        // same as junitFileNameWithExtension but without extension .java
        String junitFileName = junitFileNameWithExtension.substring(0, junitFileNameWithExtension.lastIndexOf('.'));
        
        unitFileNames.add(completeGeneratedTestFileName);
        unitFileNames.add(junitFileName);
      
        return unitFileNames;
    }
    
    private boolean checkMBeanTypeButtons(NewFileWizardOperator nfwo, MBean mbean) {
        String type = mbean.getMBeanType();
           if (type.equals(JellyConstants.STDMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.EXTSTDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.DYNMBEAN, nfwo));
                return true;
           } else if (type.equals(JellyConstants.EXTSTDMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.STDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.DYNMBEAN, nfwo));
                return true;
        } else if (type.equals(JellyConstants.DYNMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.STDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.EXTSTDMBEAN, nfwo));
                return true;
        }
        return false;
    }
     
}
