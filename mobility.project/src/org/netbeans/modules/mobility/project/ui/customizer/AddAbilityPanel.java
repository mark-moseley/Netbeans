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

/*
 * AddAttributePanel.java
 *
 * Created on April 22, 2004, 5:05 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.event.ActionListener;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Adam Sotona
 */
public class AddAbilityPanel extends javax.swing.JPanel implements ActionListener, DocumentListener {
    
    private DialogDescriptor dd;
    private boolean editing;
    private Set<String> usedNames;
    private String originalKey;
    
    /** Creates new form AddAttributePanel */
    public AddAbilityPanel() {
        initComponents();
        initAccessibility();
    }
    
    public void init(final boolean editing, final Vector allAbilities, final Set<String> usedNames, final String key, final String value) {
        this.usedNames = usedNames;
        this.editing = editing;
        this.originalKey = key;
        cKey.setModel(new DefaultComboBoxModel(allAbilities));
        if (key != null) cKey.getEditor().setItem(key);
        tValue.setText(value == null ? "" : value);
        final Object comp = cKey.getEditor().getEditorComponent();
        if (comp instanceof JTextComponent)
            ((JTextComponent) comp).getDocument().addDocumentListener(this);
        tValue.getDocument().addDocumentListener(this);
        isValid();
    }
    
    public String getKey() {
        return cKey.getEditor().getItem().toString();
    }
    
    public String getValue() {
        return tValue.getText();
    }
    
    public void setDialogDescriptor(final DialogDescriptor desc) {
        this.dd = desc;
        dd.setHelpCtx(new HelpCtx(AddAbilityPanel.class));
        actionPerformed(null);
    }
    
    public boolean isValid() {
        final String key = getKey();
        if (!isValidAbility(key)) {
            errorPanel.setErrorBundleMessage("ERR_AddAbil_MustBeJavaIdentifier"); //NOI18N
            return false;
        }
        if (!(editing && key.equals(originalKey)) && usedNames.contains(key)) {
            errorPanel.setErrorBundleMessage("ERR_AddAbil_AlreadyExists"); //NOI18N
            return false;
        }
        errorPanel.setErrorMessage(null);
        return true;
    }
    
    private boolean isValidAbility(final String s) {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) return false;
        for (int i=1; i<s.length(); i++) {
            final char c = s.charAt(i);
            if (!Character.isJavaIdentifierPart(c) && c != '.' && c != '/' && c != '\\') return false;
        }
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        cKey = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        tValue = new javax.swing.JTextField();
        errorPanel = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 110));
        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(AddAbilityPanel.class).getString("MNM_AddAbility_Name").charAt(0));
        jLabel1.setLabelFor(cKey);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddAbilityPanel.class, "LBL_AddAbility_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 5);
        add(jLabel1, gridBagConstraints);

        cKey.setEditable(true);
        cKey.setPreferredSize(new java.awt.Dimension(300, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(cKey, gridBagConstraints);

        jLabel2.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(AddAbilityPanel.class).getString("MNM_AddAbility_Value").charAt(0));
        jLabel2.setLabelFor(tValue);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(AddAbilityPanel.class, "LBL_AddAbility_Value"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 5);
        add(jLabel2, gridBagConstraints);

        tValue.setPreferredSize(new java.awt.Dimension(300, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(tValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(errorPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddAbilityPanel.class, "ACSN_AddAbility"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddAbilityPanel.class, "ACSD_AddAbility"));
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final java.awt.event.ActionEvent e) {
        dd.setValid(isValid());
    }
    
    public void changedUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    public void insertUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    public void removeUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cKey;
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField tValue;
    // End of variables declaration//GEN-END:variables
    
}
