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

import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import javax.swing.*;

/**
 * @author pfiala
 */
public class MessageDrivenOverviewForm extends SectionNodeInnerPanel {

    /**
     * Creates new form MessageDrivenOverviewForm
     *
     * @param sectionNodeView enclosing SectionNodeView object
     */
    public MessageDrivenOverviewForm(SectionNodeView sectionNodeView) {
        super(sectionNodeView);
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        transactionTypeButtonGroup = new javax.swing.ButtonGroup();
        acknowledgeModeButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        spacerLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        beanTransactionTypeRadioButton = new javax.swing.JRadioButton();
        containerTransactionTypeRadioButton = new javax.swing.JRadioButton();
        messageSelectorTextField = new javax.swing.JTextField();
        autoAckModeRadioButton = new javax.swing.JRadioButton();
        dupsOkAckModeRadioButton = new javax.swing.JRadioButton();
        destinationTypeComboBox = new javax.swing.JComboBox();
        durabilityComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(MessageDrivenOverviewForm.class, "LBL_EjbName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        add(jLabel1, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(MessageDrivenOverviewForm.class, "LBL_TransactionType"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        add(jLabel2, gridBagConstraints);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(MessageDrivenOverviewForm.class, "LBL_MessageSelector"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        add(jLabel3, gridBagConstraints);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(MessageDrivenOverviewForm.class, "LBL_AcknowledgeMode"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        add(jLabel4, gridBagConstraints);

        jLabel5.setText(org.openide.util.NbBundle.getMessage(MessageDrivenOverviewForm.class, "LBL_DestinationType"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        add(jLabel5, gridBagConstraints);

        jLabel6.setText(org.openide.util.NbBundle.getMessage(MessageDrivenOverviewForm.class, "LBL_Durability"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        add(jLabel6, gridBagConstraints);

        spacerLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spacerLabel, gridBagConstraints);

        nameTextField.setColumns(25);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(nameTextField, gridBagConstraints);

        transactionTypeButtonGroup.add(beanTransactionTypeRadioButton);
        beanTransactionTypeRadioButton.setText("Bean");
        beanTransactionTypeRadioButton.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(beanTransactionTypeRadioButton, gridBagConstraints);

        transactionTypeButtonGroup.add(containerTransactionTypeRadioButton);
        containerTransactionTypeRadioButton.setText("Container");
        containerTransactionTypeRadioButton.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(containerTransactionTypeRadioButton, gridBagConstraints);

        messageSelectorTextField.setColumns(25);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(messageSelectorTextField, gridBagConstraints);

        acknowledgeModeButtonGroup.add(autoAckModeRadioButton);
        autoAckModeRadioButton.setText(org.openide.util.NbBundle.getMessage(MessageDrivenOverviewForm.class, "LBL_Auto"));
        autoAckModeRadioButton.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(autoAckModeRadioButton, gridBagConstraints);
        autoAckModeRadioButton.getAccessibleContext().setAccessibleName("Auto-acknowledge");

        acknowledgeModeButtonGroup.add(dupsOkAckModeRadioButton);
        dupsOkAckModeRadioButton.setText(org.openide.util.NbBundle.getMessage(MessageDrivenOverviewForm.class, "LBL_DupsOk"));
        dupsOkAckModeRadioButton.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(dupsOkAckModeRadioButton, gridBagConstraints);
        dupsOkAckModeRadioButton.getAccessibleContext().setAccessibleName("Dups-ok-acknowledge");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(destinationTypeComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(durabilityComboBox, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup acknowledgeModeButtonGroup;
    private javax.swing.JRadioButton autoAckModeRadioButton;
    private javax.swing.JRadioButton beanTransactionTypeRadioButton;
    private javax.swing.JRadioButton containerTransactionTypeRadioButton;
    private javax.swing.JComboBox destinationTypeComboBox;
    private javax.swing.JRadioButton dupsOkAckModeRadioButton;
    private javax.swing.JComboBox durabilityComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField messageSelectorTextField;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel spacerLabel;
    private javax.swing.ButtonGroup transactionTypeButtonGroup;
    // End of variables declaration//GEN-END:variables

    public void setValue(JComponent source, Object value) {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    public ButtonGroup getAcknowledgeModeButtonGroup() {
        return acknowledgeModeButtonGroup;
    }

    public JRadioButton getAutoAckModeRadioButton() {
        return autoAckModeRadioButton;
    }

    public JRadioButton getBeanTransactionTypeRadioButton() {
        return beanTransactionTypeRadioButton;
    }

    public JRadioButton getContainerTransactionTypeRadioButton() {
        return containerTransactionTypeRadioButton;
    }

    public JComboBox getDestinationTypeComboBox() {
        return destinationTypeComboBox;
    }

    public JRadioButton getDupsOkAckModeRadioButton() {
        return dupsOkAckModeRadioButton;
    }

    public JComboBox getDurabilityComboBox() {
        return durabilityComboBox;
    }

    public JTextField getMessageSelectorTextField() {
        return messageSelectorTextField;
    }

    public JTextField getNameTextField() {
        return nameTextField;
    }

    public ButtonGroup getTransactionTypeButtonGroup() {
        return transactionTypeButtonGroup;
    }
}
