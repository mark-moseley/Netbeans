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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.mbeanwizard;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JTable;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import org.netbeans.modules.jmx.test.helpers.JellyToolsHelper;
import org.netbeans.modules.jmx.test.helpers.JellyConstants;
import org.netbeans.modules.jmx.test.helpers.MBean;
import org.netbeans.modules.jmx.test.helpers.Notification;
import org.netbeans.modules.jmx.test.helpers.Operation;
import org.netbeans.modules.jmx.test.helpers.Parameter;
import java.util.ArrayList;
import java.util.Properties;


/**
 *
 * @author an156382
 */
public class CreateOneFeatureMBean extends JellyTestCase {
    
    /** Need to be defined because of JUnit */
    public CreateOneFeatureMBean(String name) {
        super(name);
    }
    
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateOneFeatureMBean("constructTest2MBean"));
        suite.addTest(new CreateOneFeatureMBean("constructTest6MBean"));
        suite.addTest(new CreateOneFeatureMBean("constructTest10MBean"));
        
        suite.addTest(new CreateOneFeatureMBean("constructTest3MBean"));
        suite.addTest(new CreateOneFeatureMBean("constructTest7MBean"));
        suite.addTest(new CreateOneFeatureMBean("constructTest11MBean"));
    
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
    
    public void constructTest2MBean() {
        
        MBean standardMBean = createStandardMBean(JellyConstants.MBEAN_TWO);
        wizardExecution(standardMBean);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest6MBean() {
        
        MBean extendedMBean = createExtendedStandardMBean(JellyConstants.MBEAN_SIX);
        wizardExecution(extendedMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    public void constructTest10MBean() {
        
        MBean dynamicMBean = createDynamicMBean(JellyConstants.MBEAN_TEN);
        wizardExecution(dynamicMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    public void constructTest3MBean() {
        
        MBean standardMBean = createStandardMBean(JellyConstants.MBEAN_THREE);
        wizardExecution(standardMBean);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest7MBean() {
        
        MBean extendedMBean = createExtendedStandardMBean(JellyConstants.MBEAN_SEVEN);
        wizardExecution(extendedMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    public void constructTest11MBean() {
        
        MBean dynamicMBean = createDynamicMBean(JellyConstants.MBEAN_ELEVEN);
        wizardExecution(dynamicMBean);
        
        //TODO Diff between generated and master files
        
    }
    
  //========================= WizardExecution =================================//
    
    private void wizardExecution(MBean mbean) {
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init(JellyConstants.CATEGORY, 
                mbean.getMBeanName(), mbean.getMBeanPackage());
        nfwo.next();
        
        ArrayList<String> fileNames = optionStep(nfwo, mbean);
        nfwo.next();
        
        attributeStep(nfwo, mbean);
        nfwo.next();
       
        operationStep(nfwo, mbean);
        nfwo.next();
        
        notificationStep(nfwo, mbean);
        nfwo.next();
      
        ArrayList<String> unitFileNames = junitStep(nfwo, mbean);
        nfwo.finish();
        
        assertTrue(JellyToolsHelper.diffOK(fileNames, unitFileNames, mbean)); 
    }
    
    //========================= Class Name generation ===========================//
    
    private MBean createStandardMBean(String mbeanNumber) {
        
        return new MBean(
                mbeanNumber, 
                JellyConstants.STDMBEAN, 
                JellyConstants.PACKAGE_NAME, 
                JellyConstants.MBEAN_TWO_COMMENT, 
                constructMBeanAttributes(), constructMBeanOperations(), 
                constructMBeanNotifications(mbeanNumber));
    }
    
    private MBean createExtendedStandardMBean(String mbeanNumber) {
        
        return new MBean(
                mbeanNumber, 
                JellyConstants.EXTSTDMBEAN, 
                JellyConstants.PACKAGE_NAME, 
                JellyConstants.MBEAN_SIX_COMMENT, 
                constructMBeanAttributes(), constructMBeanOperations(), 
                constructMBeanNotifications(mbeanNumber));
    }
    
    private MBean createDynamicMBean(String mbeanNumber) {
        
        return new MBean(
                mbeanNumber, 
                JellyConstants.DYNMBEAN, 
                JellyConstants.PACKAGE_NAME, 
                JellyConstants.MBEAN_TEN_COMMENT, 
                constructMBeanAttributes(), 
                constructMBeanOperations(), 
                constructMBeanNotifications(mbeanNumber));
    }
    
    private ArrayList<Attribute> constructMBeanAttributes() {
        
        Attribute mBeanAttribute = new Attribute(JellyConstants.ATTR1_NAME, 
                JellyConstants.INT_TYPE, JellyConstants.RO, 
                JellyConstants.ATTR1_DESCR);
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        attrs.add(mBeanAttribute);
        
       return attrs;
    }
    
    private ArrayList<Operation> constructMBeanOperations() {
        
        Parameter mBeanOperationParameter1 = new Parameter(JellyConstants.PARAM1_NAME, 
                JellyConstants.STR_TYPE,
                JellyConstants.PARAM1_DESCR);
        Parameter mBeanOperationParameter2 = new Parameter(JellyConstants.PARAM2_NAME, 
                JellyConstants.OBJNAME_TYPE,
                JellyConstants.PARAM2_DESCR);
        ArrayList<Parameter> params = new ArrayList<Parameter>();
        params.add(mBeanOperationParameter1);
        params.add(mBeanOperationParameter2);
        
        //operation construction
        Operation mBeanOperation = new Operation(JellyConstants.OP1_NAME, 
                JellyConstants.VOID_TYPE, params, null, 
                JellyConstants.OP1_DESCR);
        ArrayList<Operation> ops = new ArrayList<Operation>();
        ops.add(mBeanOperation);
        
        return ops;
    }
    
    private ArrayList<Notification> constructMBeanNotifications(String mbeanNumber) {
        Notification mBeanNotification;
        //Notification construction
        if (mbeanNumber.equals(JellyConstants.MBEAN_THREE) || 
                mbeanNumber.equals(JellyConstants.MBEAN_SEVEN) || 
                mbeanNumber.equals(JellyConstants.MBEAN_ELEVEN)) 
            mBeanNotification = new Notification(JellyConstants.NOTIF_CHANGE,   
                JellyConstants.NOTIF1_DESCR, null);
        else 
            mBeanNotification = new Notification(JellyConstants.NOTIF_,   
                JellyConstants.NOTIF1_DESCR, null);
        ArrayList<Notification> notifs = new ArrayList<Notification>();
        notifs.add(mBeanNotification);
        
        return notifs;
    }
    
    //========================= Class Name generation ===========================//
    
    private String getCompleteGeneratedFileName(NewFileWizardOperator nfwo) {
        return JellyToolsHelper.getTextFieldContent(JellyConstants.GENFILE_TXT, nfwo);
    }
    
    private String getClassName(String completeFileName, String mbeanName) {
        return JellyToolsHelper.replaceMBeanClassName(completeFileName, mbeanName+JellyConstants.JAVA_EXT);
    }
    
    private String getInterfaceName(String completeFileName) {
        String itfWithExtension = completeFileName.substring(
                completeFileName.lastIndexOf(File.separatorChar)+1);
        return itfWithExtension.substring(0, itfWithExtension.lastIndexOf('.'));
    }
    
    //========================= Panel discovery ==================================//
    
    private ArrayList<String> optionStep(NewFileWizardOperator nfwo, MBean mbean) {
        
        ArrayList<String> fileNames = new ArrayList<String>();
        
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.EXISTINGCLASS_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEnabled(JellyConstants.EXISTINGCLASS_TXT, nfwo));
        
        JellyToolsHelper.changeRadioButtonSelection(mbean.getMBeanType(), nfwo, true);
        assertTrue(JellyToolsHelper.verifyRadioButtonSelected(mbean.getMBeanType(), nfwo));
        assertTrue(checkMBeanTypeButtons(nfwo, mbean));
        
        String name = mbean.getMBeanName();
        if (name.equals(JellyConstants.MBEAN_ELEVEN) ||
                name.equals(JellyConstants.MBEAN_SEVEN) ||
                name.equals(JellyConstants.MBEAN_THREE)) {
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.IMPLEMMBEAN_CBX, nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.IMPLEMMBEAN_CBX, nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.PREREGPARAM_CBX, nfwo));
        } else {
            JellyToolsHelper.changeCheckBoxSelection(JellyConstants.IMPLEMMBEAN_CBX, nfwo, true);
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.IMPLEMMBEAN_CBX, nfwo));
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.PREREGPARAM_CBX, nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.PREREGPARAM_CBX, nfwo));
        }
        
        JellyToolsHelper.setTextFieldContent(JellyConstants.MBEANDESCR_TXT, nfwo, 
                mbean.getMBeanComment());
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
    
    private void attributeStep(NewFileWizardOperator nfwo, MBean mbean) {
        // attributes
        assertTrue(JellyToolsHelper.verifyTableEnabled(JellyConstants.ATTR_TBL,nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled(JellyConstants.ATTR_ADD_BTN,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.ATTR_REM_BTN,nfwo)); 
        
        JellyToolsHelper.fillMBeanAttributes(nfwo, mbean);
    }
 
    private void operationStep(NewFileWizardOperator nfwo, MBean mbean) {
      
        assertTrue(JellyToolsHelper.verifyTableEnabled(JellyConstants.OPER_TBL,nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled(JellyConstants.OPER_ADD_BTN,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.OPER_REM_BTN,nfwo));
        
        JellyToolsHelper.fillMBeanOperation(nfwo, mbean);
    }
   
    private void notificationStep(NewFileWizardOperator nfwo, MBean mbean) {
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.IMPLNOTIFEMIT_CBX, nfwo));
        JellyToolsHelper.changeCheckBoxSelection(JellyConstants.IMPLNOTIFEMIT_CBX, nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.IMPLNOTIFEMIT_CBX, nfwo));
        
        String name = mbean.getMBeanName();
        if (name.equals(JellyConstants.MBEAN_ELEVEN) ||
                name.equals(JellyConstants.MBEAN_SEVEN) ||
                name.equals(JellyConstants.MBEAN_THREE)) {
                
            JellyToolsHelper.changeCheckBoxSelection(JellyConstants.GENDELEG_CBX, nfwo, true);
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.GENDELEG_CBX, nfwo));
            JellyToolsHelper.changeCheckBoxSelection(JellyConstants.GENSEQNUM_CBX, nfwo, true);
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.GENSEQNUM_CBX, nfwo));
        } else {
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.GENDELEG_CBX, nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.GENSEQNUM_CBX, nfwo));
        }
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.GENDELEG_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.GENSEQNUM_CBX, nfwo));
        
        assertTrue(JellyToolsHelper.verifyTableEnabled(JellyConstants.NOTIF_TBL,nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled(JellyConstants.NOTIF_ADD_BTN,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.NOTIF_REM_BTN,nfwo));
        
        JellyToolsHelper.fillMBeanNotification(nfwo, mbean);
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
        
        String name = mbean.getMBeanName();
        if (name.equals(JellyConstants.MBEAN_ELEVEN) ||
                name.equals(JellyConstants.MBEAN_SEVEN) ||
                name.equals(JellyConstants.MBEAN_THREE)) {
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.DEFMETHBODY_CBX, nfwo));
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.DEFMETHBODY_CBX, nfwo));
            
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.JAVADOC_CBX, nfwo));
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.JAVADOC_CBX, nfwo));
        } else {
            JellyToolsHelper.changeCheckBoxSelection(JellyConstants.DEFMETHBODY_CBX, nfwo, false);
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.DEFMETHBODY_CBX, nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.DEFMETHBODY_CBX, nfwo));
            
            JellyToolsHelper.changeCheckBoxSelection(JellyConstants.JAVADOC_CBX, nfwo, false);
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.JAVADOC_CBX, nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.JAVADOC_CBX, nfwo));
        }
        
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
