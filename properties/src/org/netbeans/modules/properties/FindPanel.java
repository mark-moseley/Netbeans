/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.openide.util.NbBundle;


/**
 * Find panel for Resource Bundles table view component. GUI represenation only.
 *
 * @author  Peter Zavadsky
 */
public class FindPanel extends javax.swing.JPanel {
    
    /** Creates new form FindPanel. */
    public FindPanel() {
        initComponents ();
        initAccessibility ();
    }

    private String getBundleString(String s){
        return NbBundle.getMessage(FindPanel.class, s);
    }

    // Accessor methods.
    
    /** Accessor to buttons. */
    public JButton[] getButtons() {
        return new JButton[] { findButton, cancelButton};
    }
    
    /** Accessor to combo box. */
    public JComboBox getComboBox() {
        return findCombo;
    }

    /** Accessor to highlight check box. */
    public JCheckBox getHighlightCheck() {
        return highlightCheck;
    }
    
    /** Accessor to match case check box. */
    public JCheckBox getMatchCaseCheck() {
        return matchCaseCheck;
    }
    
    /** Accessor to backward check box. */
    public JCheckBox getBackwardCheck() {
        return backwardCheck;
    }
    
    /** Accessor to wrap check box. */
    public JCheckBox getWrapCheck() {
        return wrapCheck;
    }
    
    /** Accessor to row check box. */
    public JCheckBox getRowCheck() {
        return rowCheck;
    }
    
    private void initAccessibility () {
        this.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_FindPanel"));
        
        findLabel.setDisplayedMnemonic((getBundleString("LBL_Find_Mnem")).charAt(0));
        findButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_Find"));
        rowCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_SearchByRows"));
        wrapCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_WrapSearch"));
        matchCaseCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_MatchCaseCheck"));
        cancelButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_Cancel"));
        backwardCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_BackwardCheck"));
        findCombo.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_FindCombo"));
        highlightCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_HighlightCheck"));
        
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        findLabel = new javax.swing.JLabel();
        findCombo = new javax.swing.JComboBox();
        findButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        matchCaseCheck = new javax.swing.JCheckBox();
        backwardCheck = new javax.swing.JCheckBox();
        wrapCheck = new javax.swing.JCheckBox();
        rowCheck = new javax.swing.JCheckBox();
        highlightCheck = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        findLabel.setLabelFor(findCombo);
        findLabel.setText(getBundleString("LBL_Find"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(findLabel, gridBagConstraints);

        findCombo.setEditable(true);
        findCombo.setNextFocusableComponent(highlightCheck);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 0);
        add(findCombo, gridBagConstraints);

        findButton.setText(getBundleString("CTL_Find"));
        findButton.setNextFocusableComponent(cancelButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 11);
        add(findButton, gridBagConstraints);

        cancelButton.setText(getBundleString("CTL_Cancel"));
        cancelButton.setNextFocusableComponent(findCombo);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 11);
        add(cancelButton, gridBagConstraints);

        matchCaseCheck.setMnemonic((getBundleString("CTL_MatchCaseCheck_Mnem")).charAt(0));
        matchCaseCheck.setText(getBundleString("CTL_MatchCaseCheck"));
        matchCaseCheck.setNextFocusableComponent(backwardCheck);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(matchCaseCheck, gridBagConstraints);

        backwardCheck.setMnemonic((getBundleString("CTL_BackwardCheck_Mnem")).charAt(0));
        backwardCheck.setText(getBundleString("CTL_BackwardCheck"));
        backwardCheck.setNextFocusableComponent(wrapCheck);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(backwardCheck, gridBagConstraints);

        wrapCheck.setMnemonic((getBundleString("CTL_WrapSearch_Mnem")).charAt(0));
        wrapCheck.setText(getBundleString("CTL_WrapSearch"));
        wrapCheck.setNextFocusableComponent(rowCheck);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(wrapCheck, gridBagConstraints);

        rowCheck.setMnemonic((getBundleString("CTL_SearchByRows_Mnem")).charAt(0));
        rowCheck.setText(getBundleString("CTL_SearchByRows"));
        rowCheck.setNextFocusableComponent(findButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(rowCheck, gridBagConstraints);

        highlightCheck.setMnemonic((getBundleString("CTL_HighlightCheck_Mnem")).charAt(0));
        highlightCheck.setText(getBundleString("CTL_HighlightCheck"));
        highlightCheck.setNextFocusableComponent(matchCaseCheck);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(highlightCheck, gridBagConstraints);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton findButton;
    private javax.swing.JLabel findLabel;
    private javax.swing.JCheckBox rowCheck;
    private javax.swing.JCheckBox wrapCheck;
    private javax.swing.JCheckBox matchCaseCheck;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox backwardCheck;
    private javax.swing.JComboBox findCombo;
    private javax.swing.JCheckBox highlightCheck;
    // End of variables declaration//GEN-END:variables

}
