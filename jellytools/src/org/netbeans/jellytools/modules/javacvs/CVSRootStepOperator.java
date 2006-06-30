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
package org.netbeans.jellytools.modules.javacvs;

import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JPasswordFieldOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling "CVS Root" panel of
 * Checkout or Import wizard.
 * <br>
 * Usage:<br>
 * <pre>
 *      CheckoutWizardOperator.invoke();
 *      CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
 *      cvsRootOper.setPassword("password");
 *      cvsRootOper.setCVSRoot(":pserver:user@host:repository");
 *      cvsRootOper.next();
 *      ModuleToCheckoutStepOperator moduleOper = new ModuleToCheckoutStepOperator();
 *      moduleOper.setModule("module");
 *      moduleOper.setBranch("branch");
 *      moduleOper.setLocalFolder("/tmp");
 *      moduleOper.finish();
 * </pre>
 * @see CheckoutWizardOperator
 * @see ImportWizardOperator
 * @see EditCVSRootOperator
 * @see ProxyConfigurationOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class CVSRootStepOperator extends WizardOperator {

    /** Waits for CVS Root panel. */
    public CVSRootStepOperator() {
        // It can be in either Checkout or Import Project Options wizards
        super("");
        stepsWaitSelectedValue(Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", 
                "BK0006"));
   }

    private JLabelOperator _lblCVSRoot;
    private JComboBoxOperator _cboCVSRoot;
    /** :fork: item. */
    public static final String ITEM_FORK = ":fork:"; // NOI18N
    /** :local: item. */
    public static final String ITEM_LOCAL = ":local:"; // NOI18N
    private JButtonOperator _btEdit;
    private JLabelOperator _lblPassword;
    private JPasswordFieldOperator _txtPassword;
    private JButtonOperator _btProxyConfiguration;
    // for "ext" CVS root
    private JRadioButtonOperator _rbUseInternalSSH;
    private JCheckBoxOperator _cbRememberPassword;
    private JRadioButtonOperator _rbUseExternalShell;
    private JLabelOperator _lblSSHCommand;
    private JTextFieldOperator _txtSSHCommand;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "CVS Root:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCVSRoot() {
        if (_lblCVSRoot==null) {
            _lblCVSRoot = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK0002"));
        }
        return _lblCVSRoot;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboCVSRoot() {
        if (_cboCVSRoot==null) {
            _cboCVSRoot = new JComboBoxOperator(this);
        }
        return _cboCVSRoot;
    }

    /** Tries to find "Edit..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btEdit() {
        if (_btEdit==null) {
            _btEdit = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1105"));
        }
        return _btEdit;
    }

    /** Tries to find "Password:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPassword() {
        if (_lblPassword==null) {
            _lblPassword = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK0003"));
        }
        return _lblPassword;
    }

    /** Tries to find null JPasswordField in this dialog.
     * @return JPasswordFieldOperator
     */
    public JPasswordFieldOperator txtPassword() {
        if (_txtPassword==null) {
            _txtPassword = new JPasswordFieldOperator(this);
        }
        return _txtPassword;
    }

    /** Tries to find "Proxy Configuration..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btProxyConfiguration() {
        if (_btProxyConfiguration==null) {
            _btProxyConfiguration = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK0005"));
        }
        return _btProxyConfiguration;
    }

    //**********  ext CVS root ********************

    /** Tries to find "Use Internal SSH" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbUseInternalSSH() {
        if (_rbUseInternalSSH==null) {
            _rbUseInternalSSH = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1100"));
        }
        return _rbUseInternalSSH;
    }

    /** Tries to find "Remember Password" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbRememberPassword() {
        if (_cbRememberPassword==null) {
            _cbRememberPassword = new JCheckBoxOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1012"));
        }
        return _cbRememberPassword;
    }

    /** Tries to find "Use External Shell" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbUseExternalShell() {
        if (_rbUseExternalShell==null) {
            _rbUseExternalShell = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1101"));
        }
        return _rbUseExternalShell;
    }

    /** Tries to find "SSH Command:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSSHCommand() {
        if (_lblSSHCommand==null) {
            _lblSSHCommand = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1013"));
        }
        return _lblSSHCommand;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtSSHCommand() {
        if (_txtSSHCommand==null) {
            _txtSSHCommand = new JTextFieldOperator(
                    (JTextField)lblSSHCommand().getLabelFor());
        }
        return _txtSSHCommand;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** returns selected item for cboCVSRoot
     * @return String item
     */
    public String getCVSRoot() {
        return cboCVSRoot().getSelectedItem().toString();
    }

    /** selects item for cboCVSRoot
     * @param item String item
     */
    public void selectCVSRoot(String item) {
        cboCVSRoot().selectItem(item);
    }

    /** types text for cboCVSRoot
     * @param text String text
     */
    public void setCVSRoot(String text) {
        cboCVSRoot().clearText();
        cboCVSRoot().typeText(text);
        // value typed in combo box editor is not taken into account otherwise
        EditCVSRootOperator editOper = edit();
        // confirm when enabled otherwise cancel
        if(editOper.btOK().isEnabled()) {
            editOper.ok();
        } else {
            editOper.cancel();
        }
    }

    /** clicks on "Edit..." JButton and returns instance of EditCVSRootOperator.
     * @return instance of EditCVSRootOperator
     */
    public EditCVSRootOperator edit() {
        btEdit().pushNoBlock();
        return new EditCVSRootOperator();
    }

    /** sets text for txtPassword
     * @param text String text
     */
    public void setPassword(String text) {
        txtPassword().clearText();
        txtPassword().typeText(text);
    }

    /** clicks on "Proxy Configuration..." JButton and returns ProxyConfigurationOperator.
     * @return instance of ProxyConfigurationOperator
     */
    public ProxyConfigurationOperator proxyConfiguration() {
        btProxyConfiguration().pushNoBlock();
        return new ProxyConfigurationOperator();
    }

    //************* for ext CVS root ***************************

    /** clicks on "Use Internal SSH" JRadioButton
     */
    public void useInternalSSH() {
        rbUseInternalSSH().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkRememberPassword(boolean state) {
        if (cbRememberPassword().isSelected()!=state) {
            cbRememberPassword().push();
        }
    }

    /** clicks on "Use External Shell" JRadioButton
     */
    public void useExternalShell() {
        rbUseExternalShell().push();
    }

    /** types text for txtSSHCommand
     * @param text String text
     */
    public void setSSHCommand(String text) {
        txtSSHCommand().clearText();
        txtSSHCommand().typeText(text);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of Checkout by accessing all its components.
     */
    public void verify() {
        lblCVSRoot();
        cboCVSRoot();
        btEdit();
        lblPassword();
        txtPassword();
        btProxyConfiguration();
    }
}

