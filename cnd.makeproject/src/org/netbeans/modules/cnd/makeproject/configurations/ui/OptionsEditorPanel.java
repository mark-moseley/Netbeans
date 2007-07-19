/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

public class OptionsEditorPanel extends javax.swing.JPanel implements PropertyChangeListener {
    private BooleanConfiguration inheritValues;
    private PropertyEditorSupport editor;

    /** Creates new form CommandLineEditorPanel */
    public OptionsEditorPanel(String[] texts, BooleanConfiguration inheritValues, PropertyEditorSupport editor, PropertyEnv env) {
	this.inheritValues = inheritValues;
        this.editor = editor;
        initComponents();
        // The following line was copied from the generated code (which was reset to default)
        // so I can add an NOI18N comment
        allOptionsTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveBackground")); // NOI18N
        
	additionalLabel.setText(texts[2]);
	allLabel.setText(texts[3]);
        setPreferredSize(new java.awt.Dimension(400, 300));
        IpeUtils.requestFocus(additionalOptionsTextArea);
	if (inheritValues != null) {
	    inheritCheckBox.setSelected(inheritValues.getValue());
	}
	else {
	    remove(inheritCheckBox);
	}
        
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
        
        // Accessibility
        additionalOptionsTextArea.getAccessibleContext().setAccessibleDescription(getString("ADDITIONAL_OPTIONS_AD"));
        inheritCheckBox.getAccessibleContext().setAccessibleDescription(getString("INHERIT_AD"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        allLabel = new javax.swing.JLabel();
        allOptionsScrollPane = new javax.swing.JScrollPane();
        allOptionsTextArea = new javax.swing.JTextArea();
        inheritCheckBox = new javax.swing.JCheckBox();
        additionalLabel = new javax.swing.JLabel();
        additionalOptionsScrollPane = new javax.swing.JScrollPane();
        additionalOptionsTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        allLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("ALL_OPTIONS_MN").charAt(0));
        allLabel.setLabelFor(allOptionsTextArea);
        allLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("ALL_OPTIONS_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 2, 12);
        add(allLabel, gridBagConstraints);

        allOptionsTextArea.setEditable(false);
        allOptionsTextArea.setLineWrap(true);
        allOptionsTextArea.setWrapStyleWord(true);
        allOptionsScrollPane.setViewportView(allOptionsTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(allOptionsScrollPane, gridBagConstraints);

        inheritCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("INHERIT_MN").charAt(0));
        inheritCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("INHERIT_TXT"));
        inheritCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inheritCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
        add(inheritCheckBox, gridBagConstraints);

        additionalLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("ADDITIONAL_OPTIONS_MN").charAt(0));
        additionalLabel.setLabelFor(additionalOptionsTextArea);
        additionalLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("ADDITIONAL_OPTIONS_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 2, 12);
        add(additionalLabel, gridBagConstraints);

        additionalOptionsTextArea.setLineWrap(true);
        additionalOptionsTextArea.setWrapStyleWord(true);
        additionalOptionsScrollPane.setViewportView(additionalOptionsTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(additionalOptionsScrollPane, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void inheritCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inheritCheckBoxActionPerformed
	inheritValues.setValue(inheritCheckBox.isSelected());
    }//GEN-LAST:event_inheritCheckBoxActionPerformed
    
    public String getAdditionalOptions() {
        return additionalOptionsTextArea.getText();
    }
    
    public void setAdditionalOptions(String txt) {
        additionalOptionsTextArea.setText(txt);
    }
    
    public String getAllOptions() {
        return allOptionsTextArea.getText();
    }
    
    public void setAllOptions(String txt) {
        allOptionsTextArea.setText(txt);
    }

    private Object getPropertyValue() throws IllegalStateException {
	// FIXUP: clean for tabs and new lines
	return getAdditionalOptions();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel additionalLabel;
    private javax.swing.JScrollPane additionalOptionsScrollPane;
    private javax.swing.JTextArea additionalOptionsTextArea;
    private javax.swing.JLabel allLabel;
    private javax.swing.JScrollPane allOptionsScrollPane;
    private javax.swing.JTextArea allOptionsTextArea;
    private javax.swing.JCheckBox inheritCheckBox;
    // End of variables declaration//GEN-END:variables
    
    private static String getString(String s) {
        return NbBundle.getBundle(OptionsEditorPanel.class).getString(s);
    }
}
