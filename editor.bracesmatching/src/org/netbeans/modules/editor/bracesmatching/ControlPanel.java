/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bracesmatching;

import javax.swing.text.JTextComponent;

/**
 *
 * @author  Vita Stejskal
 */
public class ControlPanel extends javax.swing.JPanel {

    private static String [][] SEARCH_DIRECTIONS = new String [][] {
        new String [] { MasterMatcher.D_BACKWARD, "Backward Preferred" }, //NOI18N
        new String [] { MasterMatcher.D_FORWARD, "Forward Preferred" }, //NOI18N
    };
    
    private static String [][] CARET_BIAS = new String [][] {
        new String [] { MasterMatcher.B_BACKWARD, "Backward (before caret)" }, //NOI18N
        new String [] { MasterMatcher.B_FORWARD, "Forward (after caret)" }, //NOI18N
    };
    
    private JTextComponent component;
    
    /** Creates new form ControlPanel */
    public ControlPanel(JTextComponent component) {
        this.component = component;
        
        initComponents();
        
        this.backwardLookahead.setText(getBwdLookahead(component));
        this.forwardLookahead.setText(getFwdLookahead(component));
        this.searchDirection.setSelectedItem(getSearchDirection(component));
        this.caretBias.setSelectedItem(getCaretBias(component));
        this.showParameters.setSelected(getShowParameters(component));
    }

    public void applyChanges() {
        setBwdLookahead(component, backwardLookahead.getText());
        setFwdLookahead(component, forwardLookahead.getText());
        setSearchDirection(component, (String)searchDirection.getSelectedItem());
        setCaretBias(component, (String)caretBias.getSelectedItem());
        setShowParameters(component, showParameters.isSelected());
    }
    
    private static String getBwdLookahead(JTextComponent component) {
        Object value = component.getClientProperty(MasterMatcher.PROP_MAX_BACKWARD_LOOKAHEAD);
        return value == null ? "" : value.toString();
    }

    private static void setBwdLookahead(JTextComponent component, String value) {
        if (value == null || value.trim().length() == 0) {
            component.putClientProperty(MasterMatcher.PROP_MAX_BACKWARD_LOOKAHEAD, null);
        } else {
            component.putClientProperty(MasterMatcher.PROP_MAX_BACKWARD_LOOKAHEAD, value);
        }
    }
    
    private static String getFwdLookahead(JTextComponent component) {
        Object value = component.getClientProperty(MasterMatcher.PROP_MAX_FORWARD_LOOKAHEAD);
        return value == null ? "" : value.toString();
    }

    private static void setFwdLookahead(JTextComponent component, String value) {
        if (value == null || value.trim().length() == 0) {
            component.putClientProperty(MasterMatcher.PROP_MAX_FORWARD_LOOKAHEAD, null);
        } else {
            component.putClientProperty(MasterMatcher.PROP_MAX_FORWARD_LOOKAHEAD, value);
        }
    }
    
    private static String getSearchDirection(JTextComponent component) {
        Object value = component.getClientProperty(MasterMatcher.PROP_SEARCH_DIRECTION);
        if (value != null) {
            String s = value.toString();
            for (String [] pair : SEARCH_DIRECTIONS) {
                if (pair[0].equals(s)) {
                    return pair[1];
                }
            }
        }
        return ""; //NOI18N
    }

    private static void setSearchDirection(JTextComponent component, String value) {
        String s = null;
        if (value != null) {
            for (String [] pair : SEARCH_DIRECTIONS) {
                if (pair[1].equals(value)) {
                    s = pair[0];
                    break;
                }
            }
        }
        component.putClientProperty(MasterMatcher.PROP_SEARCH_DIRECTION, s);
    }

    private static String getCaretBias(JTextComponent component) {
        Object value = component.getClientProperty(MasterMatcher.PROP_CARET_BIAS);
        if (value != null) {
            String s = value.toString();
            for (String [] pair : CARET_BIAS) {
                if (pair[0].equals(s)) {
                    return pair[1];
                }
            }
        }
        return ""; //NOI18N
    }

    private static void setCaretBias(JTextComponent component, String value) {
        String s = null;
        if (value != null) {
            for (String [] pair : CARET_BIAS) {
                if (pair[1].equals(value)) {
                    s = pair[0];
                    break;
                }
            }
        }
        component.putClientProperty(MasterMatcher.PROP_CARET_BIAS, s);
    }

    private static boolean getShowParameters(JTextComponent component) {
        return Boolean.valueOf((String) component.getClientProperty(MasterMatcher.PROP_SHOW_SEARCH_PARAMETERS));
    }
    
    private static void setShowParameters(JTextComponent component, boolean show) {
        component.putClientProperty(MasterMatcher.PROP_SHOW_SEARCH_PARAMETERS, Boolean.toString(show));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        backwardLookahead = new javax.swing.JTextField();
        forwardLookahead = new javax.swing.JTextField();
        searchDirection = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        caretBias = new javax.swing.JComboBox();
        showParameters = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ControlPanel.class, "jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ControlPanel.class, "jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(ControlPanel.class, "jLabel3.text")); // NOI18N

        backwardLookahead.setText(org.openide.util.NbBundle.getMessage(ControlPanel.class, "backwardLookahead.text")); // NOI18N

        forwardLookahead.setText(org.openide.util.NbBundle.getMessage(ControlPanel.class, "forwardLookahead.text")); // NOI18N

        searchDirection.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "Backward Preferred", "Forward Preferred" }));

        jLabel4.setText(org.openide.util.NbBundle.getMessage(ControlPanel.class, "jLabel4.text")); // NOI18N

        caretBias.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "Backward (before caret)", "Forward (after caret)" }));

        showParameters.setText(org.openide.util.NbBundle.getMessage(ControlPanel.class, "jCheckBox1.text_1")); // NOI18N
        showParameters.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showParameters.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel5.setText(org.openide.util.NbBundle.getMessage(ControlPanel.class, "jLabel5.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(backwardLookahead, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(forwardLookahead, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchDirection, 0, 181, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(showParameters)
                            .add(caretBias, 0, 181, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(backwardLookahead, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(forwardLookahead, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(searchDirection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(caretBias, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(showParameters)
                    .add(jLabel5))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField backwardLookahead;
    private javax.swing.JComboBox caretBias;
    private javax.swing.JTextField forwardLookahead;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JComboBox searchDirection;
    private javax.swing.JCheckBox showParameters;
    // End of variables declaration//GEN-END:variables
    
}
