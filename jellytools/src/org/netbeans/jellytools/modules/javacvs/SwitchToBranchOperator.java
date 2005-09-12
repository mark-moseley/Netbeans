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
import org.netbeans.jellytools.modules.javacvs.actions.SwitchToBranchAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling "Switch to Branch" dialog.
 * It is opened from main menu or from popup on versioned file.
 * <br>
 * Usage:<br>
 * <pre>
 *      Node node = new Node(new SourcePackagesNode("MyProject"), "mypackage|MyFile");
 *      SwitchToBranchOperator stbo = SwitchToBranchOperator.invoke(node);
 *      stbo.switchToBranch();
 *      BrowseTagsOperator bto = stbo.browse();
 *      bto.selectBranch("mybranch");
 *      bto.ok();
 *      // or stbo.setBranch("mybranch");
 *      stbo.pushSwitch();
 *</pre>
 *
 * @see BrowseTagsOperator
 * @see org.netbeans.jellytools.modules.javacvs.actions.SwitchToBranchAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class SwitchToBranchOperator extends NbDialogOperator {
    
    /** Waits for dialog with title "Switch to Branch". */
    public SwitchToBranchOperator() {
        super(Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                "CTL_SwitchBranchDialog_Title"));
    }
    
    /** Selects nodes and call switch to branch action on them.
     * @param nodes an array of nodes
     * @return SwitchToBranchOperator instance
     */
    public static SwitchToBranchOperator invoke(Node[] nodes) {
        new SwitchToBranchAction().perform(nodes);
        return new SwitchToBranchOperator();
    }
    
    /** Selects node and call switch to branch action on it.
     * @param node node to be selected
     * @return SwitchToBranchOperator instance
     */
    public static SwitchToBranchOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JRadioButtonOperator _rbSwitchToTrunk;
    private JRadioButtonOperator _rbSwitchToBranch;
    private JTextFieldOperator _txtJTextField;
    private JButtonOperator _btBrowse;
    private JButtonOperator _btSwitch;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Switch to Trunk" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbSwitchToTrunk() {
        if (_rbSwitchToTrunk==null) {
            _rbSwitchToTrunk = new JRadioButtonOperator(this, org.netbeans.jellytools.Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_SwitchBranchForm_ToTrunk"));
        }
        return _rbSwitchToTrunk;
    }
    
    /** Tries to find "Switch to Branch:" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbSwitchToBranch() {
        if (_rbSwitchToBranch==null) {
            _rbSwitchToBranch = new JRadioButtonOperator(this, org.netbeans.jellytools.Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_SwitchBranchForm_ToBranch"));
        }
        return _rbSwitchToBranch;
    }
    
    /** Tries to find Switch to branch text field in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtSwitchToBranch() {
        if (_txtJTextField==null) {
            _txtJTextField = new JTextFieldOperator(this);
        }
        return _txtJTextField;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, org.netbeans.jellytools.Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_SwitchBranchForm_BrowseBranch"));
        }
        return _btBrowse;
    }
    
    /** Tries to find "Switch" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSwitch() {
        if (_btSwitch==null) {
            _btSwitch = new JButtonOperator(this, org.netbeans.jellytools.Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_SwitchBranchDialog_Action_Switch"));
        }
        return _btSwitch;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** clicks on "Switch to Trunk" JRadioButton
     */
    public void switchToTrunk() {
        rbSwitchToTrunk().push();
    }
    
    /** clicks on "Switch to Branch:" JRadioButton
     */
    public void switchToBranch() {
        rbSwitchToBranch().push();
    }
    
    /** gets text from Switch to branch text field
     * @return String text
     */
    public String getBranch() {
        return txtSwitchToBranch().getText();
    }
    
    /** sets text for Switch to Branch text field
     * @param text String text
     */
    public void setBranch(String text) {
        txtSwitchToBranch().clearText();
        txtSwitchToBranch().typeText(text);
    }
    
    /** clicks on "Browse..." JButton and return BrowseTagsOperator.
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browse() {
        btBrowse().pushNoBlock();
        return new BrowseTagsOperator();
    }
    
    /** clicks on "Switch" JButton
     */
    public void pushSwitch() {
        btSwitch().push();
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of SwitchToBranchOperator by accessing all its components.
     */
    public void verify() {
        rbSwitchToTrunk();
        rbSwitchToBranch();
        txtSwitchToBranch();
        btBrowse();
        btSwitch();
    }
}

