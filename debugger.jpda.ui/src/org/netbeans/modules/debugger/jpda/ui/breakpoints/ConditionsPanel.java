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

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.awt.Dimension;

import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;

import org.netbeans.modules.debugger.jpda.ui.FilteredKeymap;
import org.netbeans.modules.debugger.jpda.ui.WatchPanel;

import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Panel for breakpoint conditions
 * 
 * @author  Martin Entlicher
 */
public class ConditionsPanel extends javax.swing.JPanel {
    
    /** Creates new form ConditionsPanel */
    public ConditionsPanel() {
        initComponents();
        tfConditionFieldForUI = new javax.swing.JTextField();
        tfConditionFieldForUI.setEnabled(false);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(tfConditionFieldForUI, gridBagConstraints);
        
        classFilterCheckBoxActionPerformed(null);
        conditionCheckBoxActionPerformed(null);
        cbWhenHitCountActionPerformed(null);
        int preferredHeight = tfConditionFieldForUI.getPreferredSize().height;
        if (spCondition.getPreferredSize().height > preferredHeight) {
            preferredHeight = spCondition.getPreferredSize().height;
            tfConditionFieldForUI.setPreferredSize(new java.awt.Dimension(tfConditionFieldForUI.getPreferredSize().width, preferredHeight));
        }
        tfHitCountFilter.setPreferredSize(
                new Dimension(8*tfHitCountFilter.getFontMetrics(tfHitCountFilter.getFont()).charWidth('8'),
                              tfHitCountFilter.getPreferredSize().height));
        cbHitStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
            NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.equals"), // NOI18N
            NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.greater"), // NOI18N
            NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.multiple") // NOI18N
        }));
    }
    
    // Data Show:
    
    public void showCondition(boolean show) {
        conditionCheckBox.setVisible(show);
        if (show) {
            conditionCheckBoxActionPerformed(null);
        } else {
            spCondition.setVisible(show);
            tfCondition.setVisible(show);
            tfConditionFieldForUI.setVisible(show);
        }
    }
    
    public void showClassFilter(boolean show) {
        classFilterCheckBox.setVisible(show);
        classIncludeFilterLabel.setVisible(show);
        classIncludeFilterTextField.setVisible(show);
        classExcludeFilterLabel.setVisible(show);
        classExcludeFilterTextField.setVisible(show);
        classExcludeFilterCheckBox.setVisible(false);
    }
    
    public void showExclusionClassFilter(boolean show) {
        showClassFilter(false);
        if (show) {
            classExcludeFilterCheckBox.setVisible(show);
            classExcludeFilterTextField.setVisible(show);
        }
        classExcludeFilterCheckBoxActionPerformed(null);
    }
    
    // Data Set:
    
    public void setClassMatchFilter(String[] filter) {
        String filterStr = getFilterStr(filter);
        classIncludeFilterTextField.setText(filterStr);
        classFilterCheckBox.setSelected(filterStr.length() > 0);
    }
    
    public void setClassExcludeFilter(String[] filter) {
        String filterStr = getFilterStr(filter);
        classExcludeFilterTextField.setText(filterStr);
        if (classFilterCheckBox.isEnabled()) {
            classFilterCheckBox.setSelected(filterStr.length() > 0);
        }
        if (classExcludeFilterCheckBox.isEnabled()) {
            classFilterCheckBox.setSelected(filterStr.length() > 0);
        }
    }
    
    public void setCondition(String condition) {
        tfCondition.setText(condition);
        conditionCheckBox.setSelected(condition.length() > 0);
        conditionCheckBoxActionPerformed(null);
    }
    
    public void setHitCountFilteringStyle(HIT_COUNT_FILTERING_STYLE style) {
        cbHitStyle.setSelectedIndex((style != null) ? style.ordinal() : 0);
    }
    
    public void setHitCount(int hitCount) {
        if (hitCount != 0) {
            cbWhenHitCount.setSelected(true);
            tfHitCountFilter.setText(Integer.toString(hitCount));
        } else {
            cbWhenHitCount.setSelected(false);
            tfHitCountFilter.setText("");
        }
        cbWhenHitCountActionPerformed(null);
    }
    
    public void setupConditionPaneContext(DataObject dobj) {
        tfCondition.getDocument().putProperty(javax.swing.text.Document.StreamDescriptionProperty, dobj);
    }
    
    private String getFilterStr(String[] filter) {
        if (filter == null || filter.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filter.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(filter[i]);
        }
        return sb.toString();
    }
    
    private String[] getFilter(String filterStr) {
        if (filterStr == null || filterStr.length() == 0) {
            return new String[] {};
        }
        int numCommas = 0;
        for (int i = 0; i < filterStr.length(); i++) {
            if (filterStr.charAt(i) == ',') numCommas++;
        }
        String[] filter = new String[numCommas + 1];
        int i = 0;
        int s = 0;
        while (s < filterStr.length()) {
            int e = filterStr.indexOf(",", s);
            if (e < 0) e = filterStr.length();
            filter[i++] = filterStr.substring(s, e).trim();
            s = e + 1;
        }
        return filter;
    }
    

    
    // Data Retrieval:
    
    public String[] getClassMatchFilter() {
        String filterStr;
        if (classFilterCheckBox.isSelected()) {
            filterStr = classIncludeFilterTextField.getText().trim();
        } else {
            filterStr = "";
        }
        return getFilter(filterStr);
    }
    
    public String[] getClassExcludeFilter() {
        String filterStr;
        if (classFilterCheckBox.isEnabled() && classFilterCheckBox.isSelected() ||
            classExcludeFilterCheckBox.isEnabled() && classExcludeFilterCheckBox.isSelected()) {
            filterStr = classExcludeFilterTextField.getText().trim();
        } else {
            filterStr = "";
        }
        return getFilter(filterStr);
    }
    
    public String getCondition() {
        if (conditionCheckBox.isSelected()) {
            return tfCondition.getText().trim();
        } else {
            return "";
        }
    }
    
    public HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle() {
        if (!cbWhenHitCount.isSelected()) {
            return null;
        } else {
            return HIT_COUNT_FILTERING_STYLE.values()[cbHitStyle.getSelectedIndex()];
        }
    }
    
    public int getHitCount() {
        if (!cbWhenHitCount.isSelected()) {
            return 0;
        }
        String hcfStr = tfHitCountFilter.getText().trim();
        try {
            int hitCount = Integer.parseInt(hcfStr);
            return hitCount;
        } catch (NumberFormatException nfex) {
            return 0;
        }
    }
    
    public String valiadateMsg () {
        String hcfStr = tfHitCountFilter.getText().trim();
        if (cbWhenHitCount.isSelected()) {
            if (hcfStr.length() > 0) {
                int hitCountFilter;
                try {
                    hitCountFilter = Integer.parseInt(hcfStr);
                } catch (NumberFormatException e) {
                    return NbBundle.getMessage(ConditionsPanel.class, "MSG_Bad_Hit_Count_Filter_Spec", hcfStr);
                }
                if (hitCountFilter <= 0) {
                    return NbBundle.getMessage(ConditionsPanel.class, "MSG_NonPositive_Hit_Count_Filter_Spec");
                }
            } else {
                return NbBundle.getMessage(ConditionsPanel.class, "MSG_No_Hit_Count_Filter_Spec");
            }
        }
        if (conditionCheckBox.isSelected() && tfCondition.getText().trim().length() == 0) {
            return NbBundle.getMessage(ConditionsPanel.class, "MSG_No_Condition_Spec");
        }
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        classFilterCheckBox = new javax.swing.JCheckBox();
        classIncludeFilterLabel = new javax.swing.JLabel();
        classIncludeFilterTextField = new javax.swing.JTextField();
        classExcludeFilterLabel = new javax.swing.JLabel();
        classExcludeFilterCheckBox = new javax.swing.JCheckBox();
        classExcludeFilterTextField = new javax.swing.JTextField();
        conditionCheckBox = new javax.swing.JCheckBox();
        panelHitCountFilter = new javax.swing.JPanel();
        tfHitCountFilter = new javax.swing.JTextField();
        cbHitStyle = new javax.swing.JComboBox();
        cbWhenHitCount = new javax.swing.JCheckBox();
        spCondition = null;
        tfCondition = new javax.swing.JEditorPane();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "L_Conditions_Breakpoint_BorderTitle"))); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        classFilterCheckBox.setText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.classFilterCheckBox.text")); // NOI18N
        classFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classFilterCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        classFilterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classFilterCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(classFilterCheckBox, gridBagConstraints);

        classIncludeFilterLabel.setText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.classIncludeFilterLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(classIncludeFilterLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(classIncludeFilterTextField, gridBagConstraints);

        classExcludeFilterLabel.setText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.classExcludeFilterLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(classExcludeFilterLabel, gridBagConstraints);

        classExcludeFilterCheckBox.setText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.classExcludeFilterLabel.text")); // NOI18N
        classExcludeFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classExcludeFilterCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        classExcludeFilterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classExcludeFilterCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(classExcludeFilterCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(classExcludeFilterTextField, gridBagConstraints);

        conditionCheckBox.setText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.conditionCheckBox.text")); // NOI18N
        conditionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        conditionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        conditionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conditionCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(conditionCheckBox, gridBagConstraints);

        panelHitCountFilter.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        panelHitCountFilter.add(tfHitCountFilter, gridBagConstraints);

        cbHitStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "equals to", "is greater then", "is multiple of" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        panelHitCountFilter.add(cbHitStyle, gridBagConstraints);

        cbWhenHitCount.setText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.text")); // NOI18N
        cbWhenHitCount.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbWhenHitCount.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbWhenHitCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbWhenHitCountActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        panelHitCountFilter.add(cbWhenHitCount, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(panelHitCountFilter, gridBagConstraints);

        spCondition = WatchPanel.createScrollableLineEditor(tfCondition);
        spCondition.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.spCondition.toolTipText")); // NOI18N

        tfCondition.setContentType("text/x-java");
        tfCondition.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.tfCondition.toolTipText")); // NOI18N
        spCondition.setViewportView(tfCondition);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(spCondition, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void classExcludeFilterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classExcludeFilterCheckBoxActionPerformed
    classExcludeFilterTextField.setEnabled(classExcludeFilterCheckBox.isSelected());
}//GEN-LAST:event_classExcludeFilterCheckBoxActionPerformed

private void cbWhenHitCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbWhenHitCountActionPerformed
    boolean isSelected = cbWhenHitCount.isSelected();
    cbHitStyle.setEnabled(isSelected);
    tfHitCountFilter.setEnabled(isSelected);
}//GEN-LAST:event_cbWhenHitCountActionPerformed

private void conditionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conditionCheckBoxActionPerformed
    boolean isSelected = conditionCheckBox.isSelected();
    //System.err.println("Initial TF Background = "+tfConditionFieldForUI.getBackground());
    spCondition.setEnabled(isSelected);
    tfCondition.setEnabled(isSelected);
    
    if (isSelected) {
        tfCondition.setVisible(true);
        spCondition.setVisible(true);
        tfConditionFieldForUI.setVisible(false);
        if (spCondition.getPreferredSize().height > tfCondition.getPreferredSize().height) {
            final int shift = -(spCondition.getPreferredSize().height - tfCondition.getPreferredSize().height)/2;
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    spCondition.getViewport().setViewPosition(new java.awt.Point(0, shift));
                }
            });
        }
    } else {
        tfCondition.setVisible(false);
        spCondition.setVisible(false);
        tfConditionFieldForUI.setText(tfCondition.getText());
        tfConditionFieldForUI.setVisible(true);
    }
    revalidate();
    repaint();
    
    //tfConditionFieldForUI.setEnabled(isSelected);
    //System.err.println("TF Background = "+tfConditionFieldForUI.getBackground());
    //tfCondition.setBackground(tfConditionFieldForUI.getBackground());
    //spCondition.setBorder(tfConditionFieldForUI.getBorder());
}//GEN-LAST:event_conditionCheckBoxActionPerformed

private void classFilterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classFilterCheckBoxActionPerformed
    boolean classFilterEnabled = classFilterCheckBox.isSelected();
    classIncludeFilterTextField.setEnabled(classFilterEnabled);
    classExcludeFilterTextField.setEnabled(classFilterEnabled);
}//GEN-LAST:event_classFilterCheckBoxActionPerformed
    
    private javax.swing.JTextField tfConditionFieldForUI;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbHitStyle;
    private javax.swing.JCheckBox cbWhenHitCount;
    private javax.swing.JCheckBox classExcludeFilterCheckBox;
    private javax.swing.JLabel classExcludeFilterLabel;
    private javax.swing.JTextField classExcludeFilterTextField;
    private javax.swing.JCheckBox classFilterCheckBox;
    private javax.swing.JLabel classIncludeFilterLabel;
    private javax.swing.JTextField classIncludeFilterTextField;
    private javax.swing.JCheckBox conditionCheckBox;
    private javax.swing.JPanel panelHitCountFilter;
    private javax.swing.JScrollPane spCondition;
    private javax.swing.JEditorPane tfCondition;
    private javax.swing.JTextField tfHitCountFilter;
    // End of variables declaration//GEN-END:variables
    
}
