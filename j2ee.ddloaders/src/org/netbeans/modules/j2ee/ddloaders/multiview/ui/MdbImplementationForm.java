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

package org.netbeans.modules.j2ee.ddloaders.multiview.ui;

import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import javax.swing.*;

/**
 * @author pfiala
 */
public class MdbImplementationForm extends SectionNodeInnerPanel {

    /**
     * Creates new form MdbImplementationForm
     *
     * @param sectionNodeView enclosing SectionNodeView object
     */
    public MdbImplementationForm(SectionNodeView sectionNodeView) {
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

        jLabel1 = new javax.swing.JLabel();
        beanClassTextField = new javax.swing.JTextField();
        spacerLabel = new javax.swing.JLabel();
        beanClassLinkButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(MdbImplementationForm.class, "LBL_BeanClass")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jLabel1, gridBagConstraints);

        beanClassTextField.setColumns(35);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        add(beanClassTextField, gridBagConstraints);

        spacerLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spacerLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(beanClassLinkButton, org.openide.util.NbBundle.getMessage(MdbImplementationForm.class, "LBL_GoToSource")); // NOI18N
        beanClassLinkButton.setBorderPainted(false);
        beanClassLinkButton.setContentAreaFilled(false);
        beanClassLinkButton.setFocusPainted(false);
        beanClassLinkButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        add(beanClassLinkButton, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton beanClassLinkButton;
    private javax.swing.JTextField beanClassTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel spacerLabel;
    // End of variables declaration//GEN-END:variables

    public void setValue(JComponent source, Object value) {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    public JTextField getBeanClassTextField() {
        return beanClassTextField;
    }

    public JButton getBeanClassLinkButton() {
        return beanClassLinkButton;
    }

}
