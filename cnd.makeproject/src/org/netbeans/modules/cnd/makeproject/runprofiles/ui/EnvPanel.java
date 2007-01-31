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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.runprofiles.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JTable;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class EnvPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener {
    private RunProfile currentProfile;

    private ListTableModel envvarModel = null;
    private JTable envvarTable = null;

    private Env env;
    private PropertyEditorSupport editor;
    
    /** Creates new form EnvPanel */
    public EnvPanel(Env env, PropertyEditorSupport editor, PropertyEnv propenv) {
        initComponents();
	this.env = env;
        this.editor = editor;
	envvarScrollPane.getViewport().setBackground(java.awt.Color.WHITE);

	// Environment Variables
	envvarModel = new ListTableModel(getString("EnvName"), getString("EnvValue"));
	envvarTable = new JTable(envvarModel);
	envvarModel.setTable(envvarTable);
	envvarScrollPane.setViewportView(envvarTable);

	initValues(env);
        
        propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        propenv.addPropertyChangeListener(this);
        
        // Accessibility
        environmentLabel.setLabelFor(envvarTable);
        envvarTable.getAccessibleContext().setAccessibleDescription(getString("ACSD_ENV_VAR_TABLE"));   
        addButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_ADD_BUTTON"));
        removeButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_REMOVE_BUTTON"));
    }

    public void initValues(Env env) {
	// Environment variables
	String[][] envvars = env.getenvAsPairs();
	if (envvars != null) {
	    int n = envvars.length;
	    ArrayList col0 = new ArrayList(n+3); // Leave slop for inserts
	    ArrayList col1 = new ArrayList(n+3);
	    for (int i = 0; i < n; i++) {
		col0.add(envvars[i][0]);
		col1.add(envvars[i][1]);
	    }
	    envvarModel.setData(n, col0, col1);
	}

	initFocus();
    }

    public void initFocus() {
    }

    public HelpCtx getHelpCtx() {
	return new HelpCtx("Environment"); // NOI18N
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        environmentPanel = new javax.swing.JPanel();
        environmentLabel = new javax.swing.JLabel();
        envvarScrollPane = new javax.swing.JScrollPane();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        environmentPanel.setLayout(new java.awt.GridBagLayout());

        environmentLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/runprofiles/ui/Bundle").getString("ENVIRONMENT_MNE").charAt(0));
        environmentLabel.setLabelFor(envvarScrollPane);
        environmentLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/runprofiles/ui/Bundle").getString("ENVIRONMENT_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        environmentPanel.add(environmentLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        environmentPanel.add(envvarScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/runprofiles/ui/Bundle").getString("ADD_BUTTON_MNE").charAt(0));
        addButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/runprofiles/ui/Bundle").getString("ADD_BUTTON"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        buttonPanel.add(addButton, gridBagConstraints);

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/runprofiles/ui/Bundle").getString("REMOVE_BUTTON_MNE").charAt(0));
        removeButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/runprofiles/ui/Bundle").getString("REMOVE_BUTTON"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        buttonPanel.add(removeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        environmentPanel.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 12);
        add(environmentPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
	int[] selRows = envvarTable.getSelectedRows();
	if ((selRows != null) && (selRows.length > 0)) {
	    envvarModel.removeRows(selRows);
	}
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
	envvarModel.addRow();
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JPanel environmentPanel;
    private javax.swing.JScrollPane envvarScrollPane;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    private Object getPropertyValue() throws IllegalStateException {
	env.removeAll();
	int numRows = envvarModel.getRowCount();
	if (numRows > 0) {
	    for (int j = 0; j < numRows; j++) {
		String name = (String)envvarModel.getValueAt(j, 0);
		if (name.length() == 0)
		    continue;
		String value = (String)envvarModel.getValueAt(j, 1);
		env.putenv(name, value);
	    }
	}
	return env;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(EnvPanel.class);
	}
	return bundle.getString(s);
    }
}
