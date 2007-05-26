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
 * ConstraintFieldValueEntryPanel.java
 *
 * Created on February 4, 2004, 11:38 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;

/**
 *
 * @author Peter Williams
 */
public class ConstraintFieldValueEntryPanel extends JPanel implements GenericTableDialogPanelAccessor {

	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	private static final TextMapping [] matchTypes = ScopeMapping.getMatchExpressionMappings();

	// Field indices (maps to values[] handled by get/setValues()
	private static final int MATCH_EXPR_FIELD = 0;
	private static final int CACHEONMATCH_FIELD = 1;
	private static final int CACHEONMATCHFAILURE_FIELD = 2;
	private static final int MATCH_VALUE_FIELD = 3;
	private static final int NUM_FIELDS = 4;	// Number of objects expected in get/setValue methods.

	// Local storage for data entered by user
	private String matchExpression;
	private boolean cacheOnMatch;
	private boolean cacheOnMatchFailure;
	private String matchValue;

	// expression combo box model
	private DefaultComboBoxModel matchExpressionModel;


	/** Creates new form ConstraintFieldValueEntryPanel */
	public ConstraintFieldValueEntryPanel() {
		// Set defaults
		matchExpression = "equals";	// NOI18N
		cacheOnMatch = true;
		cacheOnMatchFailure = false;
		matchValue = "";	// NOI18N
		
		initComponents();
		initUserComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLblMatchExprReq = new javax.swing.JLabel();
        jLblMatchExpr = new javax.swing.JLabel();
        jCbxMatchExpr = new javax.swing.JComboBox();
        jLblFiller2 = new javax.swing.JLabel();
        jLblCacheOnMatch = new javax.swing.JLabel();
        jChkCacheOnMatch = new javax.swing.JCheckBox();
        jLblFiller3 = new javax.swing.JLabel();
        jLblCacheOnMatchFailure = new javax.swing.JLabel();
        jChkCacheOnMatchFailure = new javax.swing.JCheckBox();
        jLblFiller4 = new javax.swing.JLabel();
        jLblMatchFieldValue = new javax.swing.JLabel();
        jTxtMatchFieldValue = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLblMatchExprReq.setLabelFor(jCbxMatchExpr);
        jLblMatchExprReq.setText(commonBundle.getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblMatchExprReq, gridBagConstraints);
        jLblMatchExprReq.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_RequiredMark"));
        jLblMatchExprReq.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_RequiredMark"));

        jLblMatchExpr.setDisplayedMnemonic(webappBundle.getString("MNE_MatchExpression").charAt(0));
        jLblMatchExpr.setLabelFor(jCbxMatchExpr);
        jLblMatchExpr.setText(webappBundle.getString("LBL_MatchExpression_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblMatchExpr, gridBagConstraints);

        jCbxMatchExpr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxMatchExprActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jCbxMatchExpr, gridBagConstraints);
        jCbxMatchExpr.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_MatchExpression"));
        jCbxMatchExpr.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_MatchExpression"));

        jLblFiller2.setLabelFor(jChkCacheOnMatch);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblFiller2, gridBagConstraints);

        jLblCacheOnMatch.setDisplayedMnemonic(webappBundle.getString("MNE_CacheOnMatch").charAt(0));
        jLblCacheOnMatch.setLabelFor(jChkCacheOnMatch);
        jLblCacheOnMatch.setText(webappBundle.getString("LBL_CacheOnMatch_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblCacheOnMatch, gridBagConstraints);

        jChkCacheOnMatch.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkCacheOnMatchItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jChkCacheOnMatch, gridBagConstraints);
        jChkCacheOnMatch.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CacheOnMatch"));
        jChkCacheOnMatch.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CacheOnMatch"));

        jLblFiller3.setLabelFor(jChkCacheOnMatchFailure);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblFiller3, gridBagConstraints);

        jLblCacheOnMatchFailure.setDisplayedMnemonic(webappBundle.getString("MNE_CacheOnMatchFailure").charAt(0));
        jLblCacheOnMatchFailure.setLabelFor(jChkCacheOnMatchFailure);
        jLblCacheOnMatchFailure.setText(webappBundle.getString("LBL_CacheOnMatchFailure_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblCacheOnMatchFailure, gridBagConstraints);

        jChkCacheOnMatchFailure.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkCacheOnMatchFailureItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jChkCacheOnMatchFailure, gridBagConstraints);
        jChkCacheOnMatchFailure.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CacheOnMatchFailure"));
        jChkCacheOnMatchFailure.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CacheOnMatchFailure"));

        jLblFiller4.setLabelFor(jChkCacheOnMatchFailure);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblFiller4, gridBagConstraints);

        jLblMatchFieldValue.setDisplayedMnemonic(webappBundle.getString("MNE_MatchFieldValue").charAt(0));
        jLblMatchFieldValue.setLabelFor(jTxtMatchFieldValue);
        jLblMatchFieldValue.setText(webappBundle.getString("LBL_MatchFieldValue_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblMatchFieldValue, gridBagConstraints);

        jTxtMatchFieldValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtMatchFieldValueKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jTxtMatchFieldValue, gridBagConstraints);
        jTxtMatchFieldValue.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_MatchFieldValue"));
        jTxtMatchFieldValue.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_MatchFieldValue"));

    }// </editor-fold>//GEN-END:initComponents

	private void jTxtMatchFieldValueKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtMatchFieldValueKeyReleased
		matchValue = jTxtMatchFieldValue.getText();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jTxtMatchFieldValueKeyReleased

	private void jChkCacheOnMatchFailureItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkCacheOnMatchFailureItemStateChanged
		cacheOnMatchFailure = Utils.interpretCheckboxState(evt);
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jChkCacheOnMatchFailureItemStateChanged

	private void jChkCacheOnMatchItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkCacheOnMatchItemStateChanged
		cacheOnMatch = Utils.interpretCheckboxState(evt);
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jChkCacheOnMatchItemStateChanged

	private void jCbxMatchExprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxMatchExprActionPerformed
		TextMapping expr = (TextMapping) matchExpressionModel.getSelectedItem();
		matchExpression = expr.getXMLString();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jCbxMatchExprActionPerformed
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jCbxMatchExpr;
    private javax.swing.JCheckBox jChkCacheOnMatch;
    private javax.swing.JCheckBox jChkCacheOnMatchFailure;
    private javax.swing.JLabel jLblCacheOnMatch;
    private javax.swing.JLabel jLblCacheOnMatchFailure;
    private javax.swing.JLabel jLblFiller2;
    private javax.swing.JLabel jLblFiller3;
    private javax.swing.JLabel jLblFiller4;
    private javax.swing.JLabel jLblMatchExpr;
    private javax.swing.JLabel jLblMatchExprReq;
    private javax.swing.JLabel jLblMatchFieldValue;
    private javax.swing.JTextField jTxtMatchFieldValue;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		// Setup match expression combobox
		matchExpressionModel = new DefaultComboBoxModel();
		for(int i = 0; i < matchTypes.length; i++) {
			matchExpressionModel.addElement(matchTypes[i]);
		}
		jCbxMatchExpr.setModel(matchExpressionModel);		
	}
	
	private TextMapping getExpressionMapping(String xmlKey) {
		TextMapping result = matchTypes[0]; // Default to EQUALS
		if(xmlKey == null) {
			xmlKey = ""; // NOI18N
		}
		for(int i = 0; i < matchTypes.length; i++) {
			if(matchTypes[i].getXMLString().compareTo(xmlKey) == 0) {
				result = matchTypes[i];
				break;
			}
		}
		
		return result;
	}
	
	/** -----------------------------------------------------------------------
	 *  Implementation of GenericTableDialogPanelAccessor interface
	 */	
	public void init(ASDDVersion asVersion, int preferredWidth, java.util.List entries, Object data) {
		setPreferredSize(new Dimension(preferredWidth, getPreferredSize().height));
	}
	
	public Object[] getValues() {
		Object [] result = new Object[NUM_FIELDS];
		
		result[MATCH_EXPR_FIELD] = matchExpression;
		result[CACHEONMATCH_FIELD] = Boolean.toString(cacheOnMatch);
		result[CACHEONMATCHFAILURE_FIELD] = Boolean.toString(cacheOnMatchFailure);
		result[MATCH_VALUE_FIELD] = (matchValue != null) ? matchValue : ""; // NOI18N
		
		return result;
	}	
	
	public void setValues(Object[] values) {
		if(values != null && values.length == NUM_FIELDS) {
			matchExpression = (String) values[MATCH_EXPR_FIELD];
			cacheOnMatch = Utils.booleanValueOf((String) values[CACHEONMATCH_FIELD]);
			cacheOnMatchFailure = Utils.booleanValueOf((String) values[CACHEONMATCHFAILURE_FIELD]);
			matchValue = (String) values[MATCH_VALUE_FIELD];
		} else {
			if(values != null) {
				assert (values.length == NUM_FIELDS);	// Should fail
			}
			
			// default values
			matchExpression = "equals";	// NOI18N
			cacheOnMatch = true;
			cacheOnMatchFailure = false;
			matchValue = "";	// NOI18N
		}

		setComponentValues();
	}
	
	private void setComponentValues() {
		matchExpressionModel.setSelectedItem(getExpressionMapping(matchExpression));
		jChkCacheOnMatch.setSelected(cacheOnMatch);
		jChkCacheOnMatchFailure.setSelected(cacheOnMatchFailure);
		jTxtMatchFieldValue.setText(matchValue);
	}	

	public Collection getErrors(ValidationSupport validationSupport) {
		// No validation for this panel.  All is done via UI elements.
		return new ArrayList();
	}	
	
	public boolean requiredFieldsFilled() {
		return (matchValue != null && matchValue.length() > 0);
	}
}
