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

package org.netbeans.qa.form;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Bean Form" NbDialog.
 * Most parts of code are generated by jelly tools.
 *
 * @author Jiri Vagner
 */
public class NewBeanFormOperator extends JDialogOperator {

    /** Creates new NewBeanForm that can handle it.
     */
    public NewBeanFormOperator() {
        super("New Bean Form"); // NOI18N
    }

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lblNameAndLocation;
    private JLabelOperator _lblClassName;
    private JTextFieldOperator _txtClassName;
    private JLabelOperator _lblProject;
    private JTextFieldOperator _txtProject;
    private JLabelOperator _lblLocation;
    private JComboBoxOperator _cboLocation;
    private JLabelOperator _lblPackage;
    private JComboBoxOperator _cboPackage;
    private JLabelOperator _lblCreatedFile;
    private JTextFieldOperator _txtCreatedFile;
    private JLabelOperator _lblWizardDescriptor$FixedHeightLabel;
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btFinish;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;

    public static final String ITEM_3 = "3."; // NOI18N
    public static final String ITEM_SOURCES = "sources"; // NOI18N
    public static final String ITEM_COMPATIBILITY = "compatibility"; // NOI18N
    public static final String ITEM_DATA = "data"; // NOI18N
    public static final String ITEM_FORMUTILITIES = "formUtilities"; // NOI18N
    public static final String ITEM_PALETTE = "palette"; // NOI18N
    public static final String ITEM_UNDOREDO = "undoredo"; // NOI18N


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps==null) {
            _lblSteps = new JLabelOperator(this, "Steps"); // NOI18N
        }
        return _lblSteps;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstSteps() {
        if (_lstSteps==null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "Name and Location" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblNameAndLocation() {
        if (_lblNameAndLocation==null) {
            _lblNameAndLocation = new JLabelOperator(this, "Name and Location"); // NOI18N
        }
        return _lblNameAndLocation;
    }

    /** Tries to find "Class Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblClassName() {
        if (_lblClassName==null) {
            _lblClassName = new JLabelOperator(this, "Class Name:"); // NOI18N
        }
        return _lblClassName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtClassName() {
        if (_txtClassName==null) {
            _txtClassName = new JTextFieldOperator(this);
        }
        return _txtClassName;
    }

    /** Tries to find "Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if (_lblProject==null) {
            _lblProject = new JLabelOperator(this, "Project:"); // NOI18N
        }
        return _lblProject;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtProject() {
        if (_txtProject==null) {
            _txtProject = new JTextFieldOperator(this, 1);
        }
        return _txtProject;
    }

    /** Tries to find "Location:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if (_lblLocation==null) {
            _lblLocation = new JLabelOperator(this, "Location:"); // NOI18N
        }
        return _lblLocation;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboLocation() {
        if (_cboLocation==null) {
            _cboLocation = new JComboBoxOperator(this);
        }
        return _cboLocation;
    }

    /** Tries to find "Package:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPackage() {
        if (_lblPackage==null) {
            _lblPackage = new JLabelOperator(this, "Package:"); // NOI18N
        }
        return _lblPackage;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboPackage() {
        if (_cboPackage==null) {
            _cboPackage = new JComboBoxOperator(this, 1);
        }
        return _cboPackage;
    }

    /** Tries to find "Created File:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCreatedFile() {
        if (_lblCreatedFile==null) {
            _lblCreatedFile = new JLabelOperator(this, "Created File:"); // NOI18N
        }
        return _lblCreatedFile;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCreatedFile() {
        if (_txtCreatedFile==null) {
            _txtCreatedFile = new JTextFieldOperator(this, 3);
        }
        return _txtCreatedFile;
    }

    /** Tries to find " " WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWizardDescriptor$FixedHeightLabel() {
        if (_lblWizardDescriptor$FixedHeightLabel==null) {
            _lblWizardDescriptor$FixedHeightLabel = new JLabelOperator(this, " ", 3); // NOI18N
        }
        return _lblWizardDescriptor$FixedHeightLabel;
    }

    /** Tries to find "< Back" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBack() {
        if (_btBack==null) {
            _btBack = new JButtonOperator(this, "< Back"); // NOI18N
        }
        return _btBack;
    }

    /** Tries to find "Next >" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            _btNext = new JButtonOperator(this, "Next >"); // NOI18N
        }
        return _btNext;
    }

    /** Tries to find "Finish" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFinish() {
        if (_btFinish==null) {
            _btFinish = new JButtonOperator(this, "Finish"); // NOI18N
        }
        return _btFinish;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel"); // NOI18N
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help"); // NOI18N
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtClassName
     * @return String text
     */
    public String getClassName() {
        return txtClassName().getText();
    }

    /** sets text for txtClassName
     * @param text String text
     */
    public void setClassName(String text) {
        txtClassName().setText(text);
    }

    /** types text for txtClassName
     * @param text String text
     */
    public void typeClassName(String text) {
        txtClassName().typeText(text);
    }

    /** gets text for txtProject
     * @return String text
     */
    public String getProject() {
        return txtProject().getText();
    }

    /** sets text for txtProject
     * @param text String text
     */
    public void setProject(String text) {
        txtProject().setText(text);
    }

    /** types text for txtProject
     * @param text String text
     */
    public void typeProject(String text) {
        txtProject().typeText(text);
    }

    /** returns selected item for cboLocation
     * @return String item
     */
    public String getSelectedLocation() {
        return cboLocation().getSelectedItem().toString();
    }

    /** selects item for cboLocation
     * @param item String item
     */
    public void selectLocation(String item) {
        cboLocation().selectItem(item);
    }

    /** returns selected item for cboPackage
     * @return String item
     */
    public String getSelectedPackage() {
        return cboPackage().getSelectedItem().toString();
    }

    /** selects item for cboPackage
     * @param item String item
     */
    public void selectPackage(String item) {
        cboPackage().selectItem(item);
    }

    /** types text for cboPackage
     * @param text String text
     */
    public void typePackage(String text) {
        cboPackage().typeText(text);
    }

    /** gets text for txtCreatedFile
     * @return String text
     */
    public String getCreatedFile() {
        return txtCreatedFile().getText();
    }

    /** sets text for txtCreatedFile
     * @param text String text
     */
    public void setCreatedFile(String text) {
        txtCreatedFile().setText(text);
    }

    /** types text for txtCreatedFile
     * @param text String text
     */
    public void typeCreatedFile(String text) {
        txtCreatedFile().typeText(text);
    }

    /** clicks on "< Back" JButton
     */
    public void back() {
        btBack().push();
    }

    /** clicks on "Next >" JButton
     */
    public void next() {
        btNext().push();
    }

    /** clicks on "Finish" JButton
     */
    public void finish() {
        btFinish().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
}

