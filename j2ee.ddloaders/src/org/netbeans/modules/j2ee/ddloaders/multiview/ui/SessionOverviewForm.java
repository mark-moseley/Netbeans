/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview.ui;

import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import javax.swing.*;

/**
 *
 * @author  pfiala
 */
public class SessionOverviewForm extends SectionInnerPanel {
    
    /** Creates new form SessionOverviewForm */
    public SessionOverviewForm(SectionNodeView sectionView) {
        super(sectionView);
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        sessionTypeButtonGroup = new javax.swing.ButtonGroup();
        transactionTypeButtonGroup = new javax.swing.ButtonGroup();
        sessionUnmatchedRadioButton = new javax.swing.JRadioButton();
        transactionUnmatchedRadioButton = new javax.swing.JRadioButton();
        nameLabel = new javax.swing.JLabel();
        ejbNameTextField = new javax.swing.JTextField();
        sessionTypeLabel = new javax.swing.JLabel();
        statelessRadioButton = new javax.swing.JRadioButton();
        statefulRadioButton = new javax.swing.JRadioButton();
        beanRadioButton = new javax.swing.JRadioButton();
        containerRadioButton = new javax.swing.JRadioButton();
        transactionTypeLabel = new javax.swing.JLabel();
        layoutHelperLabel = new javax.swing.JLabel();

        sessionTypeButtonGroup.add(sessionUnmatchedRadioButton);
        sessionUnmatchedRadioButton.setText("null");
        transactionTypeButtonGroup.add(transactionUnmatchedRadioButton);
        transactionUnmatchedRadioButton.setSelected(true);
        transactionUnmatchedRadioButton.setText("null");

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setText("Name (ejb-name):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(nameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(ejbNameTextField, gridBagConstraints);

        sessionTypeLabel.setText("Session Type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(sessionTypeLabel, gridBagConstraints);

        sessionTypeButtonGroup.add(statelessRadioButton);
        statelessRadioButton.setText("Stateless");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(statelessRadioButton, gridBagConstraints);

        sessionTypeButtonGroup.add(statefulRadioButton);
        statefulRadioButton.setText("Stateful");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(statefulRadioButton, gridBagConstraints);

        transactionTypeButtonGroup.add(beanRadioButton);
        beanRadioButton.setText("Bean");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(beanRadioButton, gridBagConstraints);

        transactionTypeButtonGroup.add(containerRadioButton);
        containerRadioButton.setText("Container");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(containerRadioButton, gridBagConstraints);

        transactionTypeLabel.setText("Transaction Type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(transactionTypeLabel, gridBagConstraints);

        layoutHelperLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(layoutHelperLabel, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton beanRadioButton;
    private javax.swing.JRadioButton containerRadioButton;
    private javax.swing.JTextField ejbNameTextField;
    private javax.swing.JLabel layoutHelperLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.ButtonGroup sessionTypeButtonGroup;
    private javax.swing.JLabel sessionTypeLabel;
    private javax.swing.JRadioButton sessionUnmatchedRadioButton;
    private javax.swing.JRadioButton statefulRadioButton;
    private javax.swing.JRadioButton statelessRadioButton;
    private javax.swing.ButtonGroup transactionTypeButtonGroup;
    private javax.swing.JLabel transactionTypeLabel;
    private javax.swing.JRadioButton transactionUnmatchedRadioButton;
    // End of variables declaration//GEN-END:variables

    public JTextField getEjbNameTextField() {
        return ejbNameTextField;
    }

    public ButtonGroup getSessionTypeButtonGroup() {
        return sessionTypeButtonGroup;
    }

    public ButtonGroup getTransactionTypeButtonGroup() {
        return transactionTypeButtonGroup;
    }

    public JComponent getErrorComponent(String errorId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(JComponent source, Object value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
