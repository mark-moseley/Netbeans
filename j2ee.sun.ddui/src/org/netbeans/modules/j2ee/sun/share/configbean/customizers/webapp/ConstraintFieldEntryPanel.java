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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * ConstraintFieldEntryPanel.java
 *
 * Created on January 9, 2004, 12:15 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
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
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 */
public class ConstraintFieldEntryPanel extends JPanel implements GenericTableDialogPanelAccessor, TableModelListener {

    private final ResourceBundle webappBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
	
    private final ResourceBundle commonBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	private static final TextMapping [] scopeTypes = ScopeMapping.getScopeMappings();
	
	// Field indices (maps to values[] handled by get/setValues()
	private static final int NAME_FIELD = 0;
	private static final int SCOPE_FIELD = 1;
	private static final int CACHEONMATCH_FIELD = 2;
	private static final int CACHEONMATCHFAILURE_FIELD = 3;
	private static final int VALUES_FIELD = 4;
	private static final int NUM_FIELDS = 5;	// Number of objects expected in get/setValue methods.
	
	// Appserver version current referenced
	private ASDDVersion appServerVersion;
    
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
//		constraintValuesBean = null; // initialized in init()
		
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
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblNameReqFlag, gridBagConstraints);
        jLblNameReqFlag.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_RequiredMark"));
        jLblNameReqFlag.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_RequiredMark"));

        jLblName.setDisplayedMnemonic(webappBundle.getString("MNE_ConstraintFieldName").charAt(0));
        jLblName.setLabelFor(jTxtName);
        jLblName.setText(webappBundle.getString("LBL_ConstraintFieldName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtName, gridBagConstraints);
        jTxtName.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ConstraintFieldName"));
        jTxtName.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ConstraintFieldName"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblFiller1, gridBagConstraints);

        jLblScope.setDisplayedMnemonic(webappBundle.getString("MNE_ConstraintFieldScope").charAt(0));
        jLblScope.setLabelFor(jCbxScope);
        jLblScope.setText(webappBundle.getString("LBL_ConstraintFieldScope_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblScope, gridBagConstraints);

        jCbxScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxScopeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jCbxScope, gridBagConstraints);
        jCbxScope.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ConstraintFieldScope"));
        jCbxScope.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ConstraintFieldScope"));

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
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblFiller3, gridBagConstraints);

        jLblCacheOnMatchFailure.setDisplayedMnemonic(webappBundle.getString("MNE_CacheOnMatchFailure").charAt(0));
        jLblCacheOnMatchFailure.setLabelFor(jChkCacheOnMatchFailure);
        jLblCacheOnMatchFailure.setText(webappBundle.getString("LBL_CacheOnMatchFailure_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblCacheOnMatchFailure, gridBagConstraints);

        jChkCacheOnMatchFailure.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkCacheOnMatchFailureItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jChkCacheOnMatchFailure, gridBagConstraints);
        jChkCacheOnMatchFailure.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CacheOnMatchFailure"));
        jChkCacheOnMatchFailure.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CacheOnMatchFailure"));

    }// </editor-fold>//GEN-END:initComponents

	private void jChkCacheOnMatchFailureItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkCacheOnMatchFailureItemStateChanged
		cacheOnMatchFailure = Utils.interpretCheckboxState(evt);
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jChkCacheOnMatchFailureItemStateChanged

	private void jChkCacheOnMatchItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkCacheOnMatchItemStateChanged
		cacheOnMatch = Utils.interpretCheckboxState(evt);
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
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
		add(constraintFieldValuePanel, gridBagConstraints);
		
		// Setup refresh field scope combobox
		constraintFieldScopeModel = new DefaultComboBoxModel();
		for(int i = 0; i < scopeTypes.length; i++) {
			constraintFieldScopeModel.addElement(scopeTypes[i]);
		}
		jCbxScope.setModel(constraintFieldScopeModel);		
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
	public void init(ASDDVersion asVersion, int preferredWidth, List entries, Object data) {
		/* Cache appserver version for use in setComponentValues.
		 */
		appServerVersion = asVersion;
        
		/* Set preferred size to pre-table saved height plus constant, width is
		 * precalculated to be 3/4 of width of parent table.
		 */
		setPreferredSize(new Dimension(preferredWidth, basicPreferredSize.height + 148));

		/* Initialize value table model with our bean.  We'll fill this bean with
		 * any existing rows in setValues() (!PW or however I figure this out)
		 */
		constraintValuesBean = StorageBeanFactory.getStorageBeanFactory(appServerVersion).createConstraintField();
		constraintFieldValuePanel.setModelBaseBean(constraintValuesBean, asVersion);
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
			constraintValuesBean = StorageBeanFactory.getStorageBeanFactory(appServerVersion).createConstraintField();
		}

		setComponentValues();
	}
	
	private void setComponentValues() {
		jTxtName.setText(constraintFieldName);
		constraintFieldScopeModel.setSelectedItem(getScopeMapping(constraintFieldScope));
		jChkCacheOnMatch.setSelected(cacheOnMatch);
		jChkCacheOnMatchFailure.setSelected(cacheOnMatchFailure);
		constraintFieldValuePanel.setModelBaseBean(constraintValuesBean, appServerVersion);
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
