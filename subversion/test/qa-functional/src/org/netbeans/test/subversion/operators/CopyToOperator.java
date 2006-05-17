/*
 * CopyToOperator.java
 *
 * Created on 16/05/06 11:11
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.subversion.operators.actions.CopyAction;

/** Class implementing all necessary methods for handling "Copy to..." NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class CopyToOperator extends NbDialogOperator {

    /**
     * Creates new CopyToOperator that can handle it.
     */
    public CopyToOperator() {
        super("Copy");
    }
    
    /** Selects nodes and call copy action on them.
     * @param nodes an array of nodes
     * @return CopyToOperator instance
     */
    public static CopyToOperator invoke(Node[] nodes) {
        new CopyAction().perform(nodes);
        return new CopyToOperator();
    }
    
    /** Selects node and call copy action on it.
     * @param node node to be selected
     * @return CommitOperator instance
     */
    public static CopyToOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }

    private JTextAreaOperator _txtJTextArea;
    private JLabelOperator _lblDescribeTheCopyPurpose;
    private JLabelOperator _lblRepositoryFolder;
    private JComboBoxOperator _cboJComboBox;
    private JButtonOperator _btBrowse;
    private JCheckBoxOperator _cbSwitchToCopy;
    private JLabelOperator _lblWarningMessage;
    private JButtonOperator _btCopy;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }

    /** Tries to find "Describe the copy purpose:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDescribeTheCopyPurpose() {
        if (_lblDescribeTheCopyPurpose==null) {
            _lblDescribeTheCopyPurpose = new JLabelOperator(this, "Describe the copy purpose:");
        }
        return _lblDescribeTheCopyPurpose;
    }

    /** Tries to find "Repository Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryFolder() {
        if (_lblRepositoryFolder==null) {
            _lblRepositoryFolder = new JLabelOperator(this, "Repository Folder:");
        }
        return _lblRepositoryFolder;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboJComboBox==null) {
            _cboJComboBox = new JComboBoxOperator(this);
        }
        return _cboJComboBox;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }

    /** Tries to find "Switch to Copy" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSwitchToCopy() {
        if (_cbSwitchToCopy==null) {
            _cbSwitchToCopy = new JCheckBoxOperator(this, "Switch to Copy");
        }
        return _cbSwitchToCopy;
    }

    /** Tries to find "Warning - there are localy modified files!" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWarningMessage() {
        if (_lblWarningMessage==null) {
            _lblWarningMessage = new JLabelOperator(this, 2);
        }
        return _lblWarningMessage;
    }

    /** Tries to find "Copy" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCopy() {
        if (_btCopy==null) {
            _btCopy = new JButtonOperator(this, "Copy");
        }
        return _btCopy;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getCopyPurpose() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setCopyPurpose(String text) {
        txtJTextArea().clearText();
        txtJTextArea().typeText(text);
    }

    
    /** returns selected item for cboJComboBox
     * @return String item
     */
    public String getSelectedRepositoryFolder() {
        return cboJComboBox().getSelectedItem().toString();
    }

    /** selects item for cboJComboBox
     * @param item String item
     */
    public void selectJComboBox(String item) {
        cboJComboBox().selectItem(item);
    }

    /** types text for cboJComboBox
     * @param text String text
     */
    public void setRepositoryFolder(String text) {
        cboJComboBox().clearText();
        cboJComboBox().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public RepositoryBrowserImpOperator browseRepository() {
        btBrowse().pushNoBlock();
        return new RepositoryBrowserImpOperator();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkSwitchToCopy(boolean state) {
        if (cbSwitchToCopy().isSelected()!=state) {
            cbSwitchToCopy().push();
        }
    }

    /** clicks on "Copy" JButton
     */
    public void copy() {
        btCopy().push();
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


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of CopyToOperator by accessing all its components.
     */
    public void verify() {
        txtJTextArea();
        lblDescribeTheCopyPurpose();
        lblRepositoryFolder();
        cboJComboBox();
        btBrowse();
        cbSwitchToCopy();
        btCopy();
        btCancel();
        btHelp();
    }
}

