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
package org.netbeans.test.web;

import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Web Application" NbDialog.
 *
 * @author dkolar
 * @version 1.0
 */
public class NewWebProjectSpringFrameworkStepOperator extends NewProjectWizardOperator {

    /** Creates new NewWebProjectSpringFrameworkStepOperator that can handle it.
     */
    public NewWebProjectSpringFrameworkStepOperator() {
        super("New Web Application");
    }

    private JLabelOperator _lblFrameworks;
    private JLabelOperator _lblSelectTheFrameworksYouWantToUseInYourWebApplication;
    private JLabelOperator _lblSpringWebMVC25Configuration;
    private JTabbedPaneOperator _tbpJTabbedPane;
    private String _selectPageConfiguration = "Configuration";
    private JLabelOperator _lblDispatcherName;
    private JLabelOperator _lblDispatcherMapping;
    private JTextFieldOperator _txtJTextField;
    private JTextFieldOperator _txtJTextField2;
    private String _selectPageLibraries = "Libraries";
    private JCheckBoxOperator _cbIncludeJSTL;
    private JTableOperator _tabSelectTheFrameworksYouWantToUseInYourWebApplication;
    private JLabelOperator _lblWizardDescriptor$FixedHeightLabel;

    private JLabelOperator _lblTheNameEnteredContainsInvalidCharactersForAFilename;
    private JLabelOperator _lblTheDispatcherNameHasToBeEntered;
    private JLabelOperator _lblTheDispatcherMappingIsNotValid;
    private JLabelOperator _lblTheDispatcherMappingHasToBeEntered;    

    //******************************
    // Subcomponents definition part
    //******************************

    public boolean setSpringFrameworkCheckbox()
    {
        Integer springRow = tabSelectTheFrameworksYouWantToUseInYourWebApplication().findCellRow("org.netbeans.modules.spring.webmvc");
        if(springRow != -1)
        {
            tabSelectTheFrameworksYouWantToUseInYourWebApplication().clickOnCell(springRow, 0);
            return true;
        }
        else
        {
            System.err.println("No Spring framework found!");
            return false;
        }
    }

      /** Tries to find "The name entered contains invalid characters for a filename." WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheNameEnteredContainsInvalidCharactersForAFilename() {
        if (_lblTheNameEnteredContainsInvalidCharactersForAFilename==null) {
            _lblTheNameEnteredContainsInvalidCharactersForAFilename = new JLabelOperator(this, "The name entered contains invalid characters for a filename.");
        }
        return _lblTheNameEnteredContainsInvalidCharactersForAFilename;
    }
  
     /** Tries to find "The Dispatcher name has to be entered." WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheDispatcherNameHasToBeEntered() {
        if (_lblTheDispatcherNameHasToBeEntered==null) {
            _lblTheDispatcherNameHasToBeEntered = new JLabelOperator(this, org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.spring.webmvc.Bundle", "MSG_DispatcherNameIsEmpty"));
        }
        return _lblTheDispatcherNameHasToBeEntered;
    }
    
    /** Tries to find "The Dispatcher mapping is not valid." WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheDispatcherMappingIsNotValid() {
        if (_lblTheDispatcherMappingIsNotValid==null) {
            _lblTheDispatcherMappingIsNotValid = new JLabelOperator(this, org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.spring.webmvc.Bundle", "MSG_DispatcherMappingPatternIsNotValid"));
        }
        return _lblTheDispatcherMappingIsNotValid;
    }
    
    /** Tries to find "The Dispatcher mapping has to be entered." WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheDispatcherMappingHasToBeEntered() {
        if (_lblTheDispatcherMappingHasToBeEntered==null) {
            _lblTheDispatcherMappingHasToBeEntered = new JLabelOperator(this,org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.spring.webmvc.Bundle", "MSG_DispatcherMappingPatternIsEmpty"));
        }
        return _lblTheDispatcherMappingHasToBeEntered;
    }
   
    /** Tries to find "Frameworks" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFrameworks() {
        if (_lblFrameworks==null) {
            _lblFrameworks = new JLabelOperator(this, "Frameworks");
        }
        return _lblFrameworks;
    }

    /** Tries to find "Select the frameworks you want to use in your web application." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectTheFrameworksYouWantToUseInYourWebApplication() {
        if (_lblSelectTheFrameworksYouWantToUseInYourWebApplication==null) {
            _lblSelectTheFrameworksYouWantToUseInYourWebApplication = new JLabelOperator(this, "Select the frameworks you want to use in your web application.");
        }
        return _lblSelectTheFrameworksYouWantToUseInYourWebApplication;
    }

    /** Tries to find "Spring Web MVC 2.5 Configuration" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpringWebMVC25Configuration() {
        if (_lblSpringWebMVC25Configuration==null) {
            _lblSpringWebMVC25Configuration = new JLabelOperator(this, "Spring Web MVC 2.5 Configuration");
        }
        return _lblSpringWebMVC25Configuration;
    }

    /** Tries to find null JTabbedPane in this dialog.
     * @return JTabbedPaneOperator
     */
    public JTabbedPaneOperator tbpJTabbedPane() {
        if (_tbpJTabbedPane==null) {
            _tbpJTabbedPane = new JTabbedPaneOperator(this);
        }
        return _tbpJTabbedPane;
    }

    /** Tries to find "Dispatcher Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDispatcherName() {
        if (_lblDispatcherName==null) {
            _lblDispatcherName = new JLabelOperator(selectPageConfiguration(), "Dispatcher Name:");
        }
        selectPageConfiguration();
        return _lblDispatcherName;
    }

    /** Tries to find "Dispatcher Mapping:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDispatcherMapping() {
        if (_lblDispatcherMapping==null) {
            _lblDispatcherMapping = new JLabelOperator(selectPageConfiguration(), "Dispatcher Mapping:");
        }
        selectPageConfiguration();
        return _lblDispatcherMapping;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField() {
        if (_txtJTextField==null) {
            _txtJTextField = new JTextFieldOperator(selectPageConfiguration());
        }
        selectPageConfiguration();
        return _txtJTextField;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField2() {
        if (_txtJTextField2==null) {
            _txtJTextField2 = new JTextFieldOperator(selectPageConfiguration(), 1);
        }
        selectPageConfiguration();
        return _txtJTextField2;
    }

    /** Tries to find "Include JSTL" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncludeJSTL() {
        if (_cbIncludeJSTL==null) {
            _cbIncludeJSTL = new JCheckBoxOperator(selectPageLibraries(), "Include JSTL");
        }
        selectPageLibraries();
        return _cbIncludeJSTL;
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

    /** Tries to find " " WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWizardDescriptor$FixedHeightLabel() {
        if (_lblWizardDescriptor$FixedHeightLabel==null) {
            _lblWizardDescriptor$FixedHeightLabel = new JLabelOperator(this, " ", 2);
        }
        return _lblWizardDescriptor$FixedHeightLabel;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** changes current selected tab
     * @param tabName String tab name */
    public void selectJTabbedPanePage(String tabName) {
        tbpJTabbedPane().selectPage(tabName);
    }

    /** changes current selected tab to "Configuration"
     * @return JTabbedPaneOperator of parent tabbed pane
     */
    public JTabbedPaneOperator selectPageConfiguration() {
        tbpJTabbedPane().selectPage(_selectPageConfiguration);
        return tbpJTabbedPane();
    }

    /** gets text for txtJTextField
     * @return String text
     */
    public String getJTextField() {
        return txtJTextField().getText();
    }

    /** sets text for txtJTextField
     * @param text String text
     */
    public void setJTextField(String text) {
        txtJTextField().setText(text);
    }

    /** types text for txtJTextField
     * @param text String text
     */
    public void typeJTextField(String text) {
        txtJTextField().typeText(text);
    }

    /** gets text for txtJTextField2
     * @return String text
     */
    public String getJTextField2() {
        return txtJTextField2().getText();
    }

    /** sets text for txtJTextField2
     * @param text String text
     */
    public void setJTextField2(String text) {
        txtJTextField2().setText(text);
    }

    /** types text for txtJTextField2
     * @param text String text
     */
    public void typeJTextField2(String text) {
        txtJTextField2().typeText(text);
    }

    /** changes current selected tab to "Libraries"
     * @return JTabbedPaneOperator of parent tabbed pane
     */
    public JTabbedPaneOperator selectPageLibraries() {
        tbpJTabbedPane().selectPage(_selectPageLibraries);
        return tbpJTabbedPane();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncludeJSTL(boolean state) {
        if (cbIncludeJSTL().isSelected()!=state) {
            cbIncludeJSTL().push();
        }
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NewWebProjectSpringFrameworkStepOperator by accessing all its components.
     */
    public void verify() {
        lblFrameworks();
        lblSelectTheFrameworksYouWantToUseInYourWebApplication();
        lblSpringWebMVC25Configuration();
        tbpJTabbedPane();
        lblDispatcherName();
        lblDispatcherMapping();
        txtJTextField();
        txtJTextField2();
        cbIncludeJSTL();
        tabSelectTheFrameworksYouWantToUseInYourWebApplication();
        lblWizardDescriptor$FixedHeightLabel();
    }

    /** Performs simple test of NewWebProjectSpringFrameworkStepOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new NewWebProjectSpringFrameworkStepOperator().verify();
        System.out.println("SpringA verification finished.");
    }
}

