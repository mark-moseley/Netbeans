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
package org.netbeans.modules.subversion.options;

/**
 *
 * @author  Tomas Stupka
 */
public class AnnotationSettingsPanel extends javax.swing.JPanel {
    
    /** Creates new form AnnotationSettingsPanel */
    public AnnotationSettingsPanel() {
        initComponents();
        initModel();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        resetButton.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.resetButton.text")); // NOI18N

        tableLabel.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.tableLabel.text")); // NOI18N

        upButton.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.upButton.text")); // NOI18N

        downButton.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.downButton.text")); // NOI18N

        newButton.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.newButton.text")); // NOI18N

        removeButton.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.removeButton.text")); // NOI18N

        expressionsPane.setBackground(new java.awt.Color(255, 255, 255));
        expressionsPane.setViewportView(expresionsTable);

        warningLabel.setForeground(new java.awt.Color(255, 0, 0));
        warningLabel.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.warningLabel.text")); // NOI18N

        wizardButton.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.wizardButton.text")); // NOI18N

        editButton.setText(org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.editButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(expressionsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(tableLabel)
                                .add(270, 270, 270)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(upButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                            .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                            .add(editButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(wizardButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(newButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                            .add(downButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(resetButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(warningLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tableLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(newButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(wizardButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                        .add(upButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(downButton))
                    .add(expressionsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(resetButton)
                    .add(warningLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void initModel() {
        expresionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"", ""}
            },
            new String [] {
                org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.expresionsTable.column1.name"),
                org.openide.util.NbBundle.getMessage(AnnotationSettingsPanel.class, "AnnotationSettingsPanel.expresionsTable.column2.name")
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton downButton = new javax.swing.JButton();
    final javax.swing.JButton editButton = new javax.swing.JButton();
    final javax.swing.JTable expresionsTable = new javax.swing.JTable();
    final javax.swing.JScrollPane expressionsPane = new javax.swing.JScrollPane();
    final javax.swing.JButton newButton = new javax.swing.JButton();
    final javax.swing.JButton removeButton = new javax.swing.JButton();
    final javax.swing.JButton resetButton = new javax.swing.JButton();
    final javax.swing.JLabel tableLabel = new javax.swing.JLabel();
    final javax.swing.JButton upButton = new javax.swing.JButton();
    final javax.swing.JLabel warningLabel = new javax.swing.JLabel();
    final javax.swing.JButton wizardButton = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables
    
}
