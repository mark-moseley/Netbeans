/*
 * NewWebProjectStrutsFrameworkStepOperator.java
 *
 * Created on 5/6/08 3:23 PM
 */
package org.netbeans.test.web;

import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Web Application" NbDialog.
 *
 * @author dkolar
 * @version 1.0
 */
public class NewWebProjectStrutsFrameworkStepOperator extends NewProjectWizardOperator {

    /** Creates new NewWebProjectStrutsFrameworkStepOperator that can handle it.
     */
    public NewWebProjectStrutsFrameworkStepOperator() {
        super("New Web Application");
    }

    private JLabelOperator _lblActionServletName;
    private JTextFieldOperator _txtActionServletName;
    private JLabelOperator _lblActionURLPattern;
    private JComboBoxOperator _cboActionURLPattern;
    public static final String ITEM_DO1 = "*.do";
    public static final String ITEM_DO2 = "/do/*";
    private JLabelOperator _lblApplicationResource;
    private JTextFieldOperator _txtApplicationResource;
    private JCheckBoxOperator _cbAddStrutsTLDs;
    private JTableOperator _tabSelectTheFrameworksYouWantToUseInYourWebApplication;
    private JLabelOperator _lblTheURLPatternHasToBeEntered;
    private JLabelOperator _lblTheURLPatternIsNotValid;//Struts as well

    //******************************
    // Subcomponents definition part
    //******************************

     /*
     * Selects a Struts Framework to be added
     */

    public boolean setStrutsFrameworkCheckbox()
    {
        Integer strutsRow = tabSelectTheFrameworksYouWantToUseInYourWebApplication().findCellRow("org.netbeans.modules.web.struts");
        if(strutsRow != -1)
        {
            tabSelectTheFrameworksYouWantToUseInYourWebApplication().clickOnCell(strutsRow, 0);
            return true;
        }
        else
        {
            System.err.println("No Struts framework found!");
            return false;
        }
        
    }
    
    /** Tries to find "Action Servlet Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblActionServletName() {
        if (_lblActionServletName==null) {
            _lblActionServletName = new JLabelOperator(this, "Action Servlet Name:");
        }
        return _lblActionServletName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtActionServletName() {
        if (_txtActionServletName==null) {
            _txtActionServletName = new JTextFieldOperator(this);
        }
        return _txtActionServletName;
    }

    /** Tries to find "Action URL Pattern:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblActionURLPattern() {
        if (_lblActionURLPattern==null) {
            _lblActionURLPattern = new JLabelOperator(this, "Action URL Pattern:");
        }
        return _lblActionURLPattern;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboActionURLPattern() {
        if (_cboActionURLPattern==null) {
            _cboActionURLPattern = new JComboBoxOperator(this);
        }
        return _cboActionURLPattern;
    }

    /** Tries to find "Application Resource:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblApplicationResource() {
        if (_lblApplicationResource==null) {
            _lblApplicationResource = new JLabelOperator(this, "Application Resource:");
        }
        return _lblApplicationResource;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtApplicationResource() {
        if (_txtApplicationResource==null) {
            _txtApplicationResource = new JTextFieldOperator(this, 2);
        }
        return _txtApplicationResource;
    }

    /** Tries to find "Add Struts TLDs" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbAddStrutsTLDs() {
        if (_cbAddStrutsTLDs==null) {
            _cbAddStrutsTLDs = new JCheckBoxOperator(this, "Add Struts TLDs");
        }
        return _cbAddStrutsTLDs;
    }

    /** Tries to find null JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabSelectTheFrameworksYouWantToUseInYourWebApplication() {
        if (_tabSelectTheFrameworksYouWantToUseInYourWebApplication==null) {
            _tabSelectTheFrameworksYouWantToUseInYourWebApplication = new JTableOperator(this);
        }
        return _tabSelectTheFrameworksYouWantToUseInYourWebApplication;
    }

    /** Tries to find "The URL Pattern has to be entered. " WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheURLPatternHasToBeEntered() {
        if (_lblTheURLPatternHasToBeEntered==null) {
            _lblTheURLPatternHasToBeEntered = new JLabelOperator(this, "The URL Pattern has to be entered.");
        }
        return _lblTheURLPatternHasToBeEntered;
    }

        /** Tries to find "The URL Pattern is not valid." WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheURLPatternIsNotValid() {
        if (_lblTheURLPatternIsNotValid==null) {
            _lblTheURLPatternIsNotValid = new JLabelOperator(this, "The URL Pattern is not valid.");
        }
        return _lblTheURLPatternIsNotValid;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtActionServletName
     * @return String text
     */
    public String getActionServletName() {
        return txtActionServletName().getText();
    }

    /** sets text for txtActionServletName
     * @param text String text
     */
    public void setActionServletName(String text) {
        txtActionServletName().setText(text);
    }

    /** types text for txtActionServletName
     * @param text String text
     */
    public void typeActionServletName(String text) {
        txtActionServletName().typeText(text);
    }

    /** returns selected item for cboActionURLPattern
     * @return String item
     */
    public String getSelectedActionURLPattern() {
        return cboActionURLPattern().getSelectedItem().toString();
    }

    /** selects item for cboActionURLPattern
     * @param item String item
     */
    public void selectActionURLPattern(String item) {
        cboActionURLPattern().selectItem(item);
    }

    /** types text for cboActionURLPattern
     * @param text String text
     */
    public void typeActionURLPattern(String text) {
        cboActionURLPattern().typeText(text);
    }

    /** gets text for txtApplicationResource
     * @return String text
     */
    public String getApplicationResource() {
        return txtApplicationResource().getText();
    }

    /** sets text for txtApplicationResource
     * @param text String text
     */
    public void setApplicationResource(String text) {
        txtApplicationResource().setText(text);
    }

    /** types text for txtApplicationResource
     * @param text String text
     */
    public void typeApplicationResource(String text) {
        txtApplicationResource().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkAddStrutsTLDs(boolean state) {
        if (cbAddStrutsTLDs().isSelected()!=state) {
            cbAddStrutsTLDs().push();
        }
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NewWebProjectStrutsFrameworkStepOperator by accessing all its components.
     */
    public void verify() {
        lblActionServletName();
        txtActionServletName();
        lblActionURLPattern();
        cboActionURLPattern();
        lblApplicationResource();
        txtApplicationResource();
        cbAddStrutsTLDs();
        tabSelectTheFrameworksYouWantToUseInYourWebApplication();
        lblTheURLPatternHasToBeEntered();
    }

    /** Performs simple test of NewWebProjectStrutsFrameworkStepOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new NewWebProjectStrutsFrameworkStepOperator().verify();
        System.out.println("NewWebProjectStrutsFrameworkStepOperator verification finished.");
    }
}

