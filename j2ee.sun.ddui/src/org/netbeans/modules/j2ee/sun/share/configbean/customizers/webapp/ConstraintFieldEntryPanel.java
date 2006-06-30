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
 * ConstraintFieldEntryPanel.java
 *
 * Created on January 9, 2004, 12:15 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.beans.PropertyVetoException;

import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanListMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;

/**
 *
 * @author Peter Williams
 */
public class ConstraintFieldEntryPanel extends JPanel implements GenericTableDialogPanelAccessor, TableModelListener {

    private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
	
    private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	private static final TextMapping [] scopeTypes = ScopeMapping.getScopeMappings();
	
	// Field indices (maps to values[] handled by get/setValues()
	private static final int NAME_FIELD = 0;
	private static final int SCOPE_FIELD = 1;
	private static final int CACHEONMATCH_FIELD = 2;
	private static final int CACHEONMATCHFAILURE_FIELD = 3;
	private static final int VALUES_FIELD = 4;
	private static final int NUM_FIELDS = 5;	// Number of objects expected in get/setValue methods.
	
	// Local storage for data entered by user
	private String constraintFieldName;
	private String constraintFieldScope;
	private boolean cacheOnMatch;
	private boolean cacheOnMatchFailure;
	private List constraintValues;
	private ConstraintField constraintValuesBean;
	
	// Table for editing default helper web properties
	private GenericTableModel constraintFieldValueModel;
	private GenericTablePanel constraintFieldValuePanel;	
	
	private DefaultComboBoxModel constraintFieldScopeModel;
	
	private Dimension basicPreferredSize;
	
	/** Creates new form ConstraintFieldEntryPanel */
	public ConstraintFieldEntryPanel() {
		constraintFieldName = "";	// NOI18N
		constraintFieldScope = "";	// NOI18N
		cacheOnMatch = true;
		cacheOnMatchFailure = false;
		constraintValuesBean = StorageBeanFactory.getDefault().createConstraintField();
		
		initComponents();
		initUserComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLblNameReqFlag = new javax.swing.JLabel();
        jLblName = new javax.swing.JLabel();
        jTxtName = new javax.swing.JTextField();
        jLblFiller1 = new javax.swing.JLabel();
        jLblScope = new javax.swing.JLabel();
        jCbxScope = new javax.swing.JComboBox();
        jLblFiller2 = new javax.swing.JLabel();
        jLblCacheOnMatch = new javax.swing.JLabel();
        jChkCacheOnMatch = new javax.swing.JCheckBox();
        jLblFiller3 = new javax.swing.JLabel();
        jLblCacheOnMatchFailure = new javax.swing.JLabel();
        jChkCacheOnMatchFailure = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jLblNameReqFlag.setLabelFor(jTxtName);
        jLblNameReqFlag.setText(commonBundle.getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jLblNameReqFlag, gridBagConstraints);
        jLblNameReqFlag.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_RequiredMark"));
        jLblNameReqFlag.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_RequiredMark"));

        jLblName.setDisplayedMnemonic(webappBundle.getString("MNE_ConstraintFieldName").charAt(0));
        jLblName.setLabelFor(jTxtName);
        jLblName.setText(webappBundle.getString("LBL_ConstraintFieldName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        add(jLblName, gridBagConstraints);

        jTxtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jTxtName, gridBagConstraints);
        jTxtName.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ConstraintFieldName"));
        jTxtName.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ConstraintFieldName"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jLblFiller1, gridBagConstraints);

        jLblScope.setDisplayedMnemonic(webappBundle.getString("MNE_ConstraintFieldScope").charAt(0));
        jLblScope.setLabelFor(jCbxScope);
        jLblScope.setText(webappBundle.getString("LBL_ConstraintFieldScope_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        add(jLblScope, gridBagConstraints);

        jCbxScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxScopeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jCbxScope, gridBagConstraints);
        jCbxScope.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ConstraintFieldScope"));
        jCbxScope.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ConstraintFieldScope"));

        jLblFiller2.setLabelFor(jChkCacheOnMatch);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jLblFiller2, gridBagConstraints);

        jLblCacheOnMatch.setDisplayedMnemonic(webappBundle.getString("MNE_CacheOnMatch").charAt(0));
        jLblCacheOnMatch.setLabelFor(jChkCacheOnMatch);
        jLblCacheOnMatch.setText(webappBundle.getString("LBL_CacheOnMatch_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        add(jLblCacheOnMatch, gridBagConstraints);

        jChkCacheOnMatch.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkCacheOnMatchItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jChkCacheOnMatch, gridBagConstraints);
        jChkCacheOnMatch.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CacheOnMatch"));
        jChkCacheOnMatch.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CacheOnMatch"));

        jLblFiller3.setLabelFor(jChkCacheOnMatchFailure);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jLblFiller3, gridBagConstraints);

        jLblCacheOnMatchFailure.setDisplayedMnemonic(webappBundle.getString("MNE_CacheOnMatchFailure").charAt(0));
        jLblCacheOnMatchFailure.setLabelFor(jChkCacheOnMatchFailure);
        jLblCacheOnMatchFailure.setText(webappBundle.getString("LBL_CacheOnMatchFailure_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        add(jLblCacheOnMatchFailure, gridBagConstraints);

        jChkCacheOnMatchFailure.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkCacheOnMatchFailureItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jChkCacheOnMatchFailure, gridBagConstraints);
        jChkCacheOnMatchFailure.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CacheOnMatchFailure"));
        jChkCacheOnMatchFailure.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CacheOnMatchFailure"));

    }//GEN-END:initComponents

	private void jChkCacheOnMatchFailureItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkCacheOnMatchFailureItemStateChanged
		cacheOnMatchFailure = interpretCheckboxState(evt);
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jChkCacheOnMatchFailureItemStateChanged

	private void jChkCacheOnMatchItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkCacheOnMatchItemStateChanged
		cacheOnMatch = interpretCheckboxState(evt);
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jChkCacheOnMatchItemStateChanged

	private void jCbxScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxScopeActionPerformed
		TextMapping scope = (TextMapping) constraintFieldScopeModel.getSelectedItem();
		constraintFieldScope = scope.getXMLString();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jCbxScopeActionPerformed

	private void jTxtNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtNameKeyReleased
		constraintFieldName = jTxtName.getText();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jTxtNameKeyReleased
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jCbxScope;
    private javax.swing.JCheckBox jChkCacheOnMatch;
    private javax.swing.JCheckBox jChkCacheOnMatchFailure;
    private javax.swing.JLabel jLblCacheOnMatch;
    private javax.swing.JLabel jLblCacheOnMatchFailure;
    private javax.swing.JLabel jLblFiller1;
    private javax.swing.JLabel jLblFiller2;
    private javax.swing.JLabel jLblFiller3;
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblNameReqFlag;
    private javax.swing.JLabel jLblScope;
    private javax.swing.JTextField jTxtName;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		/* Save preferred size before adding table.  We have our own width and
		 * will add a constant of our own choosing for the height in init(), below.
		 */
		basicPreferredSize = getPreferredSize();
		
		/** Add constraint field value table panel :
		 *  TableEntry list has four properties: Match Expression, Cache on Match,
		 *  Cache on Match Failure, and Value.
		 */
		ArrayList tableColumns = new ArrayList(4);
		tableColumns.add(new GenericTableModel.AttributeEntry(ConstraintField.CONSTRAINT_FIELD_VALUE, "MatchExpr",				// NOI18N
			webappBundle.getString("LBL_MatchExpression"), true));		// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(ConstraintField.CONSTRAINT_FIELD_VALUE, "CacheOnMatch",			// NOI18N
			webappBundle.getString("LBL_CacheOnMatch"), false));			// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(ConstraintField.CONSTRAINT_FIELD_VALUE, "CacheOnMatchFailure",	// NOI18N
			webappBundle.getString("LBL_CacheOnMatchFailure"), false));	// NOI18N
		tableColumns.add(new GenericTableModel.ValueEntry(ConstraintField.CONSTRAINT_FIELD_VALUE, ConstraintField.CONSTRAINT_FIELD_VALUE, 
			webappBundle.getString("LBL_MatchFieldValue"), true));	// NOI18N
		
		// add key fields table
		constraintFieldValueModel = new ConstraintFieldValueTableModel(tableColumns);
		constraintFieldValueModel.addTableModelListener(this);
		constraintFieldValuePanel = new GenericTablePanel(constraintFieldValueModel, 
			webappBundle, "ConstraintFieldValue",	// NOI18N - property name
			ConstraintFieldValueEntryPanel.class, HelpContext.HELP_CACHE_MAPPING_CONSTRAINT_VALUE_POPUP);
		constraintFieldValuePanel.setHeadingMnemonic(webappBundle.getString("MNE_ConstraintFieldValue").charAt(0));	// NOI18N
		
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		add(constraintFieldValuePanel, gridBagConstraints);
		
		// Setup refresh field scope combobox
		constraintFieldScopeModel = new DefaultComboBoxModel();
		for(int i = 0; i < scopeTypes.length; i++) {
			constraintFieldScopeModel.addElement(scopeTypes[i]);
		}
		jCbxScope.setModel(constraintFieldScopeModel);		
	}
	
	private boolean interpretCheckboxState(ItemEvent e) {
		boolean state = false;

		if(e.getStateChange() == ItemEvent.SELECTED) {
			state = true;
		} else if(e.getStateChange() == ItemEvent.DESELECTED) {
			state = false;
		}

		return state;
	}
	
	private TextMapping getScopeMapping(String xmlKey) {
		TextMapping result = null;
		if(xmlKey == null) {
			xmlKey = ""; // NOI18N
		}
		for(int i = 0; i < scopeTypes.length; i++) {
			if(scopeTypes[i].getXMLString().compareTo(xmlKey) == 0) {
				result = scopeTypes[i];
				break;
			}
		}
		return result;
	}	
	
	/** -----------------------------------------------------------------------
	 *  Implementation of TableModelListener interface
	 */
	public void tableChanged(TableModelEvent e) {
		// constraint values were stored as indexed children in model, so get
		// base bean to get them back.
//		constraintValues = constraintFieldValueModel.getData();
		ConstraintField bean = (ConstraintField) constraintFieldValueModel.getDataBaseBean();
		if(constraintValuesBean == bean) {
		}
	}
	
	/** -----------------------------------------------------------------------
	 *  Implementation of GenericTableDialogPanelAccessor interface
	 */
	public void init(int preferredWidth, List entries, Object data) {
		/* Set preferred size to pre-table saved height plus constant, width is
		 * precalculated to be 3/4 of width of parent table.
		 */
		setPreferredSize(new Dimension(preferredWidth, basicPreferredSize.height + 148));

		/* Initialize value table model with our bean.  We'll fill this bean with
		 * any existing rows in setValues() (!PW or however I figure this out)
		 */
		constraintFieldValuePanel.setModelBaseBean(constraintValuesBean);
	}
	
	public Object[] getValues() {
		Object [] result = new Object[NUM_FIELDS];
		
		result[NAME_FIELD] = constraintFieldName;
		result[SCOPE_FIELD] = constraintFieldScope;
		result[CACHEONMATCH_FIELD] = Boolean.toString(cacheOnMatch);
		result[CACHEONMATCHFAILURE_FIELD] = Boolean.toString(cacheOnMatchFailure);
		result[VALUES_FIELD] = new BeanListMapping(constraintValuesBean, ConstraintField.CONSTRAINT_FIELD_VALUE);
		
		return result;
	}

	public void setValues(Object[] values) {
		if(values != null && values.length == NUM_FIELDS) {
			constraintFieldName = (String) values[NAME_FIELD];
			constraintFieldScope = (String) values[SCOPE_FIELD];
			cacheOnMatch = Utils.booleanValueOf((String) values[CACHEONMATCH_FIELD]);
			cacheOnMatchFailure = Utils.booleanValueOf((String) values[CACHEONMATCHFAILURE_FIELD]);
			constraintValuesBean = (ConstraintField) ((BeanListMapping) values[VALUES_FIELD]).getBean();
		} else {
			if(values != null) {
				assert (values.length == NUM_FIELDS);	// Should fail
			}
			
			// default values
			constraintFieldName = "";	// NOI18N
			constraintFieldScope = "";	// NOI18N
			cacheOnMatch = true;
			cacheOnMatchFailure = false;
			constraintValuesBean = StorageBeanFactory.getDefault().createConstraintField();
		}

		setComponentValues();
	}
	
	private void setComponentValues() {
		jTxtName.setText(constraintFieldName);
		constraintFieldScopeModel.setSelectedItem(getScopeMapping(constraintFieldScope));
		jChkCacheOnMatch.setSelected(cacheOnMatch);
		jChkCacheOnMatchFailure.setSelected(cacheOnMatchFailure);
		constraintFieldValuePanel.setModelBaseBean(constraintValuesBean);
	}	

	public Collection getErrors(ValidationSupport validationSupport) {
		ArrayList errorList = new ArrayList();
		
		if(!Utils.notEmpty(constraintFieldName)) {
			Object [] args = new Object [1];
			args[0] = webappBundle.getString("LBL_ConstraintFieldName");	// NOI18N
			errorList.add(MessageFormat.format(commonBundle.getString("ERR_SpecifiedFieldIsEmpty"), args));	// NOI18N
		} else if(!Utils.isJavaIdentifier(constraintFieldName)) {
			Object [] args = new Object [1];
			args[0] = constraintFieldName;
			errorList.add(MessageFormat.format(commonBundle.getString("ERR_NotValidIdentifier"), args));	// NOI18N
		}
		
		return errorList;
	}
	
	public boolean requiredFieldsFilled() {
		return (constraintFieldName != null && constraintFieldName.length() > 0);
	}
	
	private static class ConstraintFieldValueTableModel extends GenericTableModel {
		public ConstraintFieldValueTableModel(List tableColumns) {
			super(ConstraintField.CONSTRAINT_FIELD_VALUE, tableColumns);
		}
		
		public boolean alreadyExists(Object[] values) {
			boolean exists = false;
			
			Object bean = getDataBaseBean();
			if(bean instanceof ConstraintField) {
				ConstraintField parentField = (ConstraintField) bean;
				for(int i = 0, limit = parentField.sizeConstraintFieldValue(); i < limit; i++) {
					if(match((String) values[0], parentField.getConstraintFieldValueMatchExpr(i)) &&
					   match((String) values[3], parentField.getConstraintFieldValue(i))) {
						exists = true;
						break;
					}
				}
			}

			return exists;
		}
		
		private final boolean match(String a, String b) {
			boolean result = false;
			
			if(a == b) {
				result = true;
			} else if(a != null && b != null && a.equals(b)) {
				result = true;
			}
			
			return result;
		}

		public boolean alreadyExists(String keyPropertyValue) {
			// FIXME we can't actually support this API properly with the current
			// design so just have it fail.
			return false;
		}
	}
}
