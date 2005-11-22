/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.javacvs;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.modules.javacvs.actions.CommitAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;


/** Class implementing all necessary methods for handling "Commit" dialog.
 * <br>
 * Usage:<br>
 * <pre>
 *      new CommitAction().perform(node);
 *      CommitOperator co = new CommitOperator();
 *      co.setCommitMessage("Commit message.");
 *      co.selectCommitAction("MyFile", "Exclude from Commit");
 *      co.commit();
 * </pre>
 *
 * @see VersioningOperator
 * @see org.netbeans.jellytools.modules.javacvs.actions.CommitAction
 * @see org.netbeans.jellytools.modules.javacvs.actions.ShowChangesAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class CommitOperator extends NbDialogOperator {

    /** Waits for "Commit" dialog. It can have title "Commit - <object>" 
     * or "Commit files" if there is no file to commit.
     */
    public CommitOperator() {
        super(Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.commit.Bundle",
                "CTL_CommitOption_Commit"));
    }

    /** Selects nodes and call commit action on them.
     * @param nodes an array of nodes
     * @return CommitOperator instance
     */
    public static CommitOperator invoke(Node[] nodes) {
        new CommitAction().perform(nodes);
        return new CommitOperator();
    }
    
    /** Selects node and call commit action on it.
     * @param node node to be selected
     * @return CommitOperator instance
     */
    public static CommitOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JLabelOperator _lblCommitMessage;
    private JTextAreaOperator _txtCommitMessage;
    private JLabelOperator _lblFilesToCommit;
    private JTableOperator _tabFiles;
    private JButtonOperator _btCommit;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Commit Message:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCommitMessage() {
        if (_lblCommitMessage==null) {
            _lblCommitMessage = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.commit.Bundle",
                    "CTL_CommitForm_Message"));
        }
        return _lblCommitMessage;
    }

    /** Tries to find "Commit Message:" TextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtCommitMessage() {
        if (_txtCommitMessage==null) {
            _txtCommitMessage = new JTextAreaOperator(this);
        }
        return _txtCommitMessage;
    }

    /** Tries to find "Files to Commit:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFilesToCommit() {
        if (_lblFilesToCommit==null) {
            _lblFilesToCommit = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.commit.Bundle",
                    "CTL_CommitForm_FilesToCommit"));
        }
        return _lblFilesToCommit;
    }

    /** Tries to find files JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabFiles() {
        if (_tabFiles==null) {
            _tabFiles = new JTableOperator(this);
        }
        return _tabFiles;
    }

    /** Tries to find "Commit" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCommit() {
        if (_btCommit==null) {
            _btCommit = new JButtonOperator(this, Bundle.getString(
                    "org.netbeans.modules.versioning.system.cvss.ui.syncview.Bundle",
                    "CTL_CommitForm_Action_Commit"));
        }
        return _btCommit;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtCommitMessage
     * @return String text
     */
    public String getCommitMessage() {
        return txtCommitMessage().getText();
    }

    /** sets text for txtCommitMessage
     * @param text String text
     */
    public void setCommitMessage(String text) {
        txtCommitMessage().clearText();
        txtCommitMessage().typeText(text);
    }

    /** clicks on "Commit" JButton
     */
    public void commit() {
        btCommit().push();
    }

    /** Selects specified commit action for given row.
     * @param rowIndex index of row to be selected
     * @param action name of action to be selected
     */
    public void selectCommitAction(int rowIndex, String action) {
        tabFiles().clickOnCell(rowIndex, 2);
        JComboBoxOperator combo = new JComboBoxOperator(tabFiles());
        combo.selectItem(action);
    }

    /** Selects specified commit action for given row.
     * @param filename name of file to be selected
     * @param action name of action to be selected
     */
    public void selectCommitAction(String filename, String action) {
        selectCommitAction(tabFiles().findCellRow(filename), action);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CommitOperator by accessing all its components.
     */
    public void verify() {
        lblCommitMessage();
        txtCommitMessage();
        lblFilesToCommit();
        tabFiles();
        btCommit();
    }
}
