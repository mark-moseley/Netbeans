/*
 * RevertModificationsOperator.java
 *
 * Created on 18/05/06 17:09
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.subversion.operators.actions.RevertAction;

/** Class implementing all necessary methods for handling "Revert Modifications" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class RevertModificationsOperator extends NbDialogOperator {

    /**
     * Creates new RevertModificationsOperator that can handle it.
     */
    public RevertModificationsOperator() {
        super("Revert Modifications");
    }
    
    /** Selects nodes and call revert action on them.
     * @param nodes an array of nodes
     * @return RevertModificationsOperator instance
     */
    public static RevertModificationsOperator invoke(Node[] nodes) {
        new RevertAction().perform(nodes);
        return new RevertModificationsOperator();
    }
    
    /** Selects node and call switch action on it.
     * @param node node to be selected
     * @return SwitchOperator instance
     */
    public static RevertModificationsOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }

    private JLabelOperator _lblStartWithRevision;
    private JLabelOperator _lblEndWithRevision;
    private JTextFieldOperator _txtStartRevision;
    private JTextFieldOperator _txtEndRevision;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD;
    private JButtonOperator _btSearch;
    private JButtonOperator _btSearch2;
    private JCheckBoxOperator _cbInclusive;
    private JLabelOperator _lblRevertModificationsFrom;
    private JRadioButtonOperator _rbPreviousCommits;
    private JRadioButtonOperator _rbLocalChanges;
    private JButtonOperator _btRevert;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Start with Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblStartWithRevision() {
        if (_lblStartWithRevision==null) {
            _lblStartWithRevision = new JLabelOperator(this, "Start with Revision:");
        }
        return _lblStartWithRevision;
    }

    /** Tries to find "End with Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEndWithRevision() {
        if (_lblEndWithRevision==null) {
            _lblEndWithRevision = new JLabelOperator(this, "End with Revision:");
        }
        return _lblEndWithRevision;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtStartRevision() {
        if (_txtStartRevision==null) {
            _txtStartRevision = new JTextFieldOperator(this);
        }
        return _txtStartRevision;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtEndRevision() {
        if (_txtEndRevision==null) {
            _txtEndRevision = new JTextFieldOperator(this, 1);
        }
        return _txtEndRevision;
    }

    /** Tries to find "(empty means repository HEAD)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEmptyMeansRepositoryHEAD() {
        if (_lblEmptyMeansRepositoryHEAD==null) {
            _lblEmptyMeansRepositoryHEAD = new JLabelOperator(this, "(empty means repository HEAD)");
        }
        return _lblEmptyMeansRepositoryHEAD;
    }

    /** Tries to find "Search..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSearch() {
        if (_btSearch==null) {
            _btSearch = new JButtonOperator(this, "Search...");
        }
        return _btSearch;
    }

    /** Tries to find "Search..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSearch2() {
        if (_btSearch2==null) {
            _btSearch2 = new JButtonOperator(this, "Search...", 1);
        }
        return _btSearch2;
    }

    /** Tries to find "inclusive" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbInclusive() {
        if (_cbInclusive==null) {
            _cbInclusive = new JCheckBoxOperator(this, "inclusive");
        }
        return _cbInclusive;
    }

    /** Tries to find "Revert Modifications from:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRevertModificationsFrom() {
        if (_lblRevertModificationsFrom==null) {
            _lblRevertModificationsFrom = new JLabelOperator(this, "Revert Modifications from:");
        }
        return _lblRevertModificationsFrom;
    }

    /** Tries to find "Previous Commit(s)" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbPreviousCommits() {
        if (_rbPreviousCommits==null) {
            _rbPreviousCommits = new JRadioButtonOperator(this, "Previous Commit(s)");
        }
        return _rbPreviousCommits;
    }

    /** Tries to find "Local Changes" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbLocalChanges() {
        if (_rbLocalChanges==null) {
            _rbLocalChanges = new JRadioButtonOperator(this, "Local Changes");
        }
        return _rbLocalChanges;
    }

    /** Tries to find "Revert" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRevert() {
        if (_btRevert==null) {
            _btRevert = new JButtonOperator(this, "Revert");
        }
        return _btRevert;
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

    /**
     * gets text for txtStartRevision
     * 
     * @return String text
     */
    public String getTxtStartRevision() {
        return txtStartRevision().getText();
    }

    /**
     * sets text for txtStartRevision
     * 
     * @param text String text
     */
    public void setStartRevision(String text) {
        txtStartRevision().clearText();
        txtStartRevision().typeText(text);
    }

    /**
     * gets text for txtEndRevision
     * 
     * @return String text
     */
    public String getEndRevision() {
        return txtEndRevision().getText();
    }

    /**
     * sets text for txtEndRevision
     * 
     * @param text String text
     */
    public void setEndRevision(String text) {
        txtEndRevision().clearText();
        txtEndRevision().typeText(text);
    }

    /** clicks on "Search..." JButton
     */
    public void search() {
        btSearch().push();
    }

    /** clicks on "Search..." JButton
     */
    public void search2() {
        btSearch2().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkInclusive(boolean state) {
        if (cbInclusive().isSelected()!=state) {
            cbInclusive().push();
        }
    }

    /** clicks on "Previous Commit(s)" JRadioButton
     */
    public void previousCommits() {
        rbPreviousCommits().push();
    }

    /** clicks on "Local Changes" JRadioButton
     */
    public void localChanges() {
        rbLocalChanges().push();
    }

    /** clicks on "Revert" JButton
     */
    public void revert() {
        btRevert().push();
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
     * Performs verification of RevertModificationsOperator by accessing all its components.
     */
    public void verify() {
        lblStartWithRevision();
        lblEndWithRevision();
        txtStartRevision();
        txtEndRevision();
        lblEmptyMeansRepositoryHEAD();
        btSearch();
        btSearch2();
        cbInclusive();
        lblRevertModificationsFrom();
        rbPreviousCommits();
        rbLocalChanges();
        btRevert();
        btCancel();
        btHelp();
    }
}

