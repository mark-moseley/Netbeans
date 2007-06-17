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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.designer.jsf;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author  pbuzek
 */
public class JsfDesignerAdvancedOptionsPanel extends javax.swing.JPanel {
    private ResolutionModel resolutionModel = new ResolutionModel();
    
    /** Creates new form JsfDesignerAdvancedOptionsPanel */
    public JsfDesignerAdvancedOptionsPanel() {
        initComponents();
        resolutionModel.setValue(0);
        resolution.setModel(resolutionModel);
    }
    
    int getResolution() {
        return resolutionModel.getValue();
    }
    void setResolution(int res) {
        resolutionModel.setValue(res);
    }
    
    int getDefaultFontSize() {
        return ((Integer)defaultFontSize.getValue()).intValue();
    }
    void setDefaultFontSize(int i) {
        defaultFontSize.setValue(Integer.valueOf(i));
    }
    int getGridHeight() {
        return ((Integer)gridHeight.getValue()).intValue();
    }
    void setGridHeight(int i) {
        gridHeight.setValue(Integer.valueOf(i));
    }
    int getGridWidth() {
        return ((Integer)gridWidth.getValue()).intValue();
    }
    void setGridWidth(int i) {
        gridWidth.setValue(Integer.valueOf(i));
    }
    boolean isShowGrid() {
        return showGrid.isSelected();
    }
    void setShowGrid(boolean b) {
        showGrid.setSelected(b);
    }
    boolean isSnapToGrid() {
        return snapToGrid.isSelected();
    }
    void setSnapToGrid(boolean b) {
        snapToGrid.setSelected(b);
    }
    String getDataProviderSuffix() {
        return dataProviderSuffix.getText();
    }
    void setDataProviderSuffix(String suffix) {
        dataProviderSuffix.setText(suffix);
    }
    String getRowsetSuffix() {
        return rowsetSuffix.getText();
    }
    void setRowsetSuffix(String suffix) {
        rowsetSuffix.setText(suffix);
    }
    boolean isRowsetInSession() {
        return rowsetInSession.isSelected();
    }
    void setRowsetInSession(boolean b) {
        rowsetInSession.setSelected(b);
    }
    boolean isRowsetDuplicate() {
        return checkRowsetDuplicate.isSelected();
    }
    void setRowsetDuplicate(boolean b) {
        checkRowsetDuplicate.setSelected(b);
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        defaultFontSize = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        gridHeight = new javax.swing.JSpinner();
        gridWidth = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        resolution = new javax.swing.JComboBox();
        showGrid = new javax.swing.JCheckBox();
        snapToGrid = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        rowsetSuffix = new javax.swing.JTextField();
        rowsetInSession = new javax.swing.JCheckBox();
        checkRowsetDuplicate = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        dataProviderSuffix = new javax.swing.JTextField();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.jLabel4.text")); // NOI18N

        resolution.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        showGrid.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.showGrid.text")); // NOI18N
        showGrid.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showGrid.setMargin(new java.awt.Insets(0, 0, 0, 0));

        snapToGrid.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.snapToGrid.text")); // NOI18N
        snapToGrid.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        snapToGrid.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel6.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.jLabel6.text")); // NOI18N

        rowsetSuffix.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.rowsetSuffix.text")); // NOI18N

        rowsetInSession.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.rowsetInSession.text")); // NOI18N
        rowsetInSession.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rowsetInSession.setMargin(new java.awt.Insets(0, 0, 0, 0));

        checkRowsetDuplicate.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.checkRowsetDuplicate.text")); // NOI18N
        checkRowsetDuplicate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkRowsetDuplicate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel5.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.jLabel5.text")); // NOI18N

        dataProviderSuffix.setText(org.openide.util.NbBundle.getMessage(JsfDesignerAdvancedOptionsPanel.class, "JsfDesignerAdvancedOptionsPanel.dataProviderSuffix.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jLabel4))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel1))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel2))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel3))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel5))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel6)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(checkRowsetDuplicate)
                        .addContainerGap())
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(rowsetInSession)
                            .addContainerGap())
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(snapToGrid)
                                .addContainerGap())
                            .add(layout.createSequentialGroup()
                                .add(showGrid)
                                .addContainerGap())
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, rowsetSuffix, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, dataProviderSuffix, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                                    .add(resolution, 0, 166, Short.MAX_VALUE))
                                .add(14, 14, 14))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, gridWidth)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, gridHeight)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, defaultFontSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE))
                                .addContainerGap(136, Short.MAX_VALUE))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resolution, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(defaultFontSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(gridHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(gridWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showGrid)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(snapToGrid)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(dataProviderSuffix, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(rowsetSuffix, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rowsetInSession)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkRowsetDuplicate)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkRowsetDuplicate;
    private javax.swing.JTextField dataProviderSuffix;
    private javax.swing.JSpinner defaultFontSize;
    private javax.swing.JSpinner gridHeight;
    private javax.swing.JSpinner gridWidth;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox resolution;
    private javax.swing.JCheckBox rowsetInSession;
    private javax.swing.JTextField rowsetSuffix;
    private javax.swing.JCheckBox showGrid;
    private javax.swing.JCheckBox snapToGrid;
    // End of variables declaration//GEN-END:variables

    private class ResolutionModel implements ComboBoxModel {
        ResolutionEditor editor = new ResolutionEditor();

        public int getValue() {
            return ((Integer)editor.getValue()).intValue();
        }
        
        public void setValue(int value) {
            editor.setValue(Integer.valueOf(value));
        }
        
        public void setSelectedItem(Object anItem) {
            editor.setAsText((String)anItem);
        }

        public Object getSelectedItem() {
            return editor.getAsText();
        }

        public int getSize() {
            return editor.getTags().length;
        }

        public Object getElementAt(int index) {
            return editor.getTags()[index];
        }

        public void addListDataListener(ListDataListener l) {
        }

        public void removeListDataListener(ListDataListener l) {
        }
        
    }
}
