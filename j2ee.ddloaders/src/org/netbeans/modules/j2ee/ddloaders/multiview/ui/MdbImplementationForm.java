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
 * @author pfiala
 */
public class MdbImplementationForm extends SectionInnerPanel {

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
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        beanClassTextField = new javax.swing.JTextField();
        changeImplClassButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        beanClassTextField.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(beanClassTextField, gridBagConstraints);

        changeImplClassButton.setText("Change implementation class");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(changeImplClassButton, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField beanClassTextField;
    private javax.swing.JButton changeImplClassButton;
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

    public JButton getChangeImplClassButton() {
        return changeImplClassButton;
    }
}
