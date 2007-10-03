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
 * CachePolicyPanel.java
 *
 * Created on January 13, 2004, 5:06 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import java.beans.PropertyVetoException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping;
import org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.InputDialog;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;


/**
 *
 * @author Peter Williams
 */
public class CachePolicyPanel extends JPanel implements TableModelListener {
	
	/** resource bundle */
	private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private static final TextMapping [] scopeTypes = ScopeMapping.getScopeMappings();
	private static final TextMapping [] keyScopeTypes = ScopeMapping.getKeyScopeMappings();
	
	private CacheMapping theCacheMapping;
	
	// Track changes so we know if to enable save afterwards.
	private boolean dataChanged;
	private boolean timeoutEnabled;
	private boolean refreshFieldEnabled;
	
	private DefaultComboBoxModel timeoutScopeModel;
	private DefaultComboBoxModel refreshFieldScopeModel;
	
	// temporary storage until errors are all clear and we can save to mapping
	private CacheMapping newCacheMapping;
	private List httpMethods;
	private List constraints;
	
	// Table for editing dispatcher entries
	private GenericTableModel dispatcherModel;
	private GenericTablePanel dispatcherPanel;
	
	// Table for editing key fields
	private GenericTableModel keyFieldsModel;
	private GenericTablePanel keyFieldsPanel;

	// Table for editing constraint fields
	private GenericTableModel constraintFieldsModel;
	private GenericTablePanel constraintFieldsPanel;

	// true if AS 8.1+ fields are visible.
	private boolean as81FeaturesVisible;

    // disable listeners during field initialization.
    private boolean initializingFields;
    
	/** Creates new form CachePolicyPanel */
	public CachePolicyPanel(ASDDVersion asVersion, CacheMapping mapping) {
		theCacheMapping = mapping;
		newCacheMapping = (CacheMapping) mapping.clone();
		
		String [] methodArray = newCacheMapping.getHttpMethod();
		httpMethods = new ArrayList(methodArray.length);
		for(int i = 0; i < methodArray.length; i++) {
			httpMethods.add(methodArray[i]);
		}
		
		constraints = Utils.arrayToList(newCacheMapping.getConstraintField());
		
		dataChanged = false;
        initializingFields = false;
		
		initComponents();
		initUserComponents();
		initFields(asVersion);
		addListeners();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLblCachePolicy = new javax.swing.JLabel();
        jPnlTimeout = new javax.swing.JPanel();
        jLblTimeoutName = new javax.swing.JLabel();
        jTxtTimeoutName = new javax.swing.JTextField();
        jLblTimeoutValue = new javax.swing.JLabel();
        jTxtTimeoutValue = new javax.swing.JTextField();
        jLblTimeoutScope = new javax.swing.JLabel();
        jCbxTimeoutScope = new javax.swing.JComboBox();
        jPnlRefresh = new javax.swing.JPanel();
        jLblRefreshFieldName = new javax.swing.JLabel();
        jTxtRefreshFieldName = new javax.swing.JTextField();
        jLblRefreshScope = new javax.swing.JLabel();
        jCbxRefreshScope = new javax.swing.JComboBox();
        jPnlHttpMethods = new javax.swing.JPanel();
        jLblHttpMethods = new javax.swing.JLabel();
        jChkHttpGet = new javax.swing.JCheckBox();
        jChkHttpPost = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(600, 600));
        jLblCachePolicy.setText(webappBundle.getString("LBL_CachePolicy"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblCachePolicy, gridBagConstraints);

        jPnlTimeout.setLayout(new java.awt.GridBagLayout());

        jPnlTimeout.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLblTimeoutName.setDisplayedMnemonic(webappBundle.getString("MNE_TimeoutName").charAt(0));
        jLblTimeoutName.setLabelFor(jTxtTimeoutName);
        jLblTimeoutName.setText(webappBundle.getString("LBL_TimeoutName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlTimeout.add(jLblTimeoutName, gridBagConstraints);

        jTxtTimeoutName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtTimeoutNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        jPnlTimeout.add(jTxtTimeoutName, gridBagConstraints);
        jTxtTimeoutName.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_TimeoutName"));
        jTxtTimeoutName.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_TimeoutName"));

        jLblTimeoutValue.setDisplayedMnemonic(webappBundle.getString("MNE_Timeout").charAt(0));
        jLblTimeoutValue.setLabelFor(jTxtTimeoutValue);
        jLblTimeoutValue.setText(webappBundle.getString("LBL_Timeout_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        jPnlTimeout.add(jLblTimeoutValue, gridBagConstraints);

        jTxtTimeoutValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtTimeoutValueKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        jPnlTimeout.add(jTxtTimeoutValue, gridBagConstraints);
        jTxtTimeoutValue.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_Timeout"));
        jTxtTimeoutValue.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_Timeout"));

        jLblTimeoutScope.setDisplayedMnemonic(webappBundle.getString("MNE_TimeoutScope").charAt(0));
        jLblTimeoutScope.setLabelFor(jCbxTimeoutScope);
        jLblTimeoutScope.setText(webappBundle.getString("LBL_TimeoutScope_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        jPnlTimeout.add(jLblTimeoutScope, gridBagConstraints);

        jCbxTimeoutScope.setPrototypeDisplayValue("");
        jCbxTimeoutScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxTimeoutScopeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        jPnlTimeout.add(jCbxTimeoutScope, gridBagConstraints);
        jCbxTimeoutScope.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_TimeoutScope"));
        jCbxTimeoutScope.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_TimeoutScope"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 5);
        add(jPnlTimeout, gridBagConstraints);

        jPnlRefresh.setLayout(new java.awt.GridBagLayout());

        jPnlRefresh.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLblRefreshFieldName.setDisplayedMnemonic(webappBundle.getString("MNE_RefreshFieldName").charAt(0));
        jLblRefreshFieldName.setLabelFor(jTxtRefreshFieldName);
        jLblRefreshFieldName.setText(webappBundle.getString("LBL_RefreshFieldName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlRefresh.add(jLblRefreshFieldName, gridBagConstraints);

        jTxtRefreshFieldName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtRefreshFieldNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        jPnlRefresh.add(jTxtRefreshFieldName, gridBagConstraints);
        jTxtRefreshFieldName.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_RefreshFieldName"));
        jTxtRefreshFieldName.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_RefreshFieldName"));

        jLblRefreshScope.setDisplayedMnemonic(webappBundle.getString("MNE_RefreshScope").charAt(0));
        jLblRefreshScope.setLabelFor(jCbxRefreshScope);
        jLblRefreshScope.setText(webappBundle.getString("LBL_RefreshScope_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        jPnlRefresh.add(jLblRefreshScope, gridBagConstraints);

        jCbxRefreshScope.setPrototypeDisplayValue("");
        jCbxRefreshScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxRefreshScopeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        jPnlRefresh.add(jCbxRefreshScope, gridBagConstraints);
        jCbxRefreshScope.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_RefreshScope"));
        jCbxRefreshScope.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_RefreshScope"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jPnlRefresh, gridBagConstraints);

        jPnlHttpMethods.setLayout(new java.awt.GridBagLayout());

        jPnlHttpMethods.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLblHttpMethods.setText(webappBundle.getString("LBL_HttpMethods_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlHttpMethods.add(jLblHttpMethods, gridBagConstraints);

        jChkHttpGet.setMnemonic(webappBundle.getString("MNE_HttpGet").charAt(0));
        jChkHttpGet.setText(webappBundle.getString("LBL_HttpGet"));
        jChkHttpGet.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkHttpGetItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 0, 5);
        jPnlHttpMethods.add(jChkHttpGet, gridBagConstraints);
        jChkHttpGet.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_HttpGet"));
        jChkHttpGet.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_HttpGet"));

        jChkHttpPost.setMnemonic(webappBundle.getString("MNE_HttpPost").charAt(0));
        jChkHttpPost.setText(webappBundle.getString("LBL_HttpPost"));
        jChkHttpPost.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkHttpPostItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 5);
        jPnlHttpMethods.add(jChkHttpPost, gridBagConstraints);
        jChkHttpPost.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_HttpPost"));
        jChkHttpPost.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_HttpPost"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jPnlHttpMethods, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

	private void jChkHttpPostItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkHttpPostItemStateChanged
		handleHttpMethodSelection(evt, "POST");	// NOI18N
	}//GEN-LAST:event_jChkHttpPostItemStateChanged

	private void jChkHttpGetItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkHttpGetItemStateChanged
		handleHttpMethodSelection(evt, "GET");	// NOI18N
	}//GEN-LAST:event_jChkHttpGetItemStateChanged

	private void jCbxRefreshScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxRefreshScopeActionPerformed
        if(!initializingFields) {
    		TextMapping scope = (TextMapping) refreshFieldScopeModel.getSelectedItem();
        	newCacheMapping.setRefreshFieldScope(normalizeBlank(scope.getXMLString()));
            setDataChanged(true);
        }
	}//GEN-LAST:event_jCbxRefreshScopeActionPerformed

	private void jTxtRefreshFieldNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtRefreshFieldNameKeyReleased
		String refreshFieldName = jTxtRefreshFieldName.getText();
		newCacheMapping.setRefreshFieldName(refreshFieldName);
		
		if(refreshFieldEnabled != Utils.notEmpty(refreshFieldName)) {
			enableRefreshField(!refreshFieldEnabled);
		}
		
		setDataChanged(true);
	}//GEN-LAST:event_jTxtRefreshFieldNameKeyReleased

	private void jCbxTimeoutScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxTimeoutScopeActionPerformed
        if(!initializingFields) {
            TextMapping scope = (TextMapping) timeoutScopeModel.getSelectedItem();
    		newCacheMapping.setTimeoutScope(normalizeBlank(scope.getXMLString()));
        	setDataChanged(true);
        }
	}//GEN-LAST:event_jCbxTimeoutScopeActionPerformed

	private void jTxtTimeoutValueKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtTimeoutValueKeyReleased
		newCacheMapping.setTimeout(jTxtTimeoutValue.getText());		
		setDataChanged(true);
	}//GEN-LAST:event_jTxtTimeoutValueKeyReleased

	private void jTxtTimeoutNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtTimeoutNameKeyReleased
		String timeoutName = jTxtTimeoutName.getText();
		
		// !PW Work-around for schema2beans behavior (bug, IMHO).  If timeout is
		//     null, but has been previously set, timeoutname will not be saved.
		//
		if(Utils.notEmpty(timeoutName) && newCacheMapping.getTimeout() == null) {
			newCacheMapping.setTimeout("");	// NOI18N
		}
		
		newCacheMapping.setTimeoutName(timeoutName);
		
		if(timeoutEnabled != Utils.notEmpty(timeoutName)) {
			enableTimeout(!timeoutEnabled);
		}
		
		setDataChanged(true);
	}//GEN-LAST:event_jTxtTimeoutNameKeyReleased
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jCbxRefreshScope;
    private javax.swing.JComboBox jCbxTimeoutScope;
    private javax.swing.JCheckBox jChkHttpGet;
    private javax.swing.JCheckBox jChkHttpPost;
    private javax.swing.JLabel jLblCachePolicy;
    private javax.swing.JLabel jLblHttpMethods;
    private javax.swing.JLabel jLblRefreshFieldName;
    private javax.swing.JLabel jLblRefreshScope;
    private javax.swing.JLabel jLblTimeoutName;
    private javax.swing.JLabel jLblTimeoutScope;
    private javax.swing.JLabel jLblTimeoutValue;
    private javax.swing.JPanel jPnlHttpMethods;
    private javax.swing.JPanel jPnlRefresh;
    private javax.swing.JPanel jPnlTimeout;
    private javax.swing.JTextField jTxtRefreshFieldName;
    private javax.swing.JTextField jTxtTimeoutName;
    private javax.swing.JTextField jTxtTimeoutValue;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		as81FeaturesVisible = true;
        
		// Setup timeout scope combobox
		timeoutScopeModel = new DefaultComboBoxModel();
		for(int i = 0; i < scopeTypes.length; i++) {
			timeoutScopeModel.addElement(scopeTypes[i]);
		}
		jCbxTimeoutScope.setModel(timeoutScopeModel);		
		
		// Setup refresh field scope combobox
		refreshFieldScopeModel = new DefaultComboBoxModel();
		for(int i = 0; i < keyScopeTypes.length; i++) {
			refreshFieldScopeModel.addElement(keyScopeTypes[i]);
		}
		jCbxRefreshScope.setModel(refreshFieldScopeModel);

        /** Add dispatcher panel :
         *  TableEntry list has one property, Dispatcher.
         */
        ArrayList tableColumns = new ArrayList(1);
        tableColumns.add(new GenericTableModel.ValueEntry(CacheMapping.DISPATCHER, "Dispatcher",	// NOI18N 
            webappBundle, "Dispatcher", true, false));		// NOI18N

        // add dispatcher table
        dispatcherModel = new GenericTableModel(CacheMapping.DISPATCHER, tableColumns);
        dispatcherPanel = new GenericTablePanel(dispatcherModel, 
            webappBundle, "Dispatcher",	// NOI18N - property name
            HelpContext.HELP_CACHE_MAPPING_DISPATCHER_POPUP);
        dispatcherPanel.setHeadingMnemonic(webappBundle.getString("MNE_Dispatcher").charAt(0));	// NOI18N
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
        add(dispatcherPanel, gridBagConstraints);
        
		/** Add key fields table panel :
		 *  TableEntry list has two properties: Name, Scope.
		 */
		tableColumns = new ArrayList(2);
		tableColumns.add(new GenericTableModel.AttributeEntry(CacheMapping.KEY_FIELD, "Name",	// NOI18N 
			webappBundle.getString("LBL_KeyFieldName"), true));		// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(CacheMapping.KEY_FIELD, "Scope",	// NOI18N
			webappBundle.getString("LBL_KeyFieldScope"), false));	// NOI18N
		
		// add key fields table
		keyFieldsModel = new GenericTableModel(CacheMapping.KEY_FIELD, tableColumns);
		keyFieldsPanel = new GenericTablePanel(keyFieldsModel, 
			webappBundle, "KeyField",	// NOI18N - property name
			KeyFieldEntryPanel.class,
			HelpContext.HELP_CACHE_MAPPING_KEYFIELD_POPUP);
		keyFieldsPanel.setHeadingMnemonic(webappBundle.getString("MNE_KeyField").charAt(0));	// NOI18N
		
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
		add(keyFieldsPanel, gridBagConstraints);
		
		/** Add constraint fields table panel :
		 *  TableEntry list has five properties: Name, Scope, cache-on-match, 
		 *  cache-on-failure, and constraint-field-value (which is itself a table).
		 */
		tableColumns = new ArrayList(5);
		tableColumns.add(new GenericTableModel.AttributeEntry(ConstraintField.NAME, 
			webappBundle.getString("LBL_ConstraintFieldName"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(ConstraintField.SCOPE, 
			webappBundle.getString("LBL_ConstraintFieldScope")));		// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(ConstraintField.CACHEONMATCH, 
			webappBundle.getString("LBL_CacheOnMatch")));				// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(ConstraintField.CACHEONMATCHFAILURE, 
			webappBundle.getString("LBL_CacheOnMatchFailure")));		// NOI18N
		tableColumns.add(new ConstraintFieldValueEntry());
		
		// add key fields table
		constraintFieldsModel = new GenericTableModel(constraintFieldFactory, tableColumns);
		constraintFieldsPanel = new GenericTablePanel(constraintFieldsModel, 
			webappBundle, "ConstraintField",	// NOI18N - property name
			ConstraintFieldEntryPanel.class,
			HelpContext.HELP_CACHE_MAPPING_CONSTRAINT_POPUP);
		constraintFieldsPanel.setHeadingMnemonic(webappBundle.getString("MNE_ConstraintField").charAt(0));	// NOI18N
		
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
		add(constraintFieldsPanel, gridBagConstraints);
	}
	
	/** Initialization of all the fields in this panel from the bean that
	 *  was passed in.
	 */
	public void initFields(ASDDVersion asVersion) {
        try {
            initializingFields = true;
            jTxtTimeoutName.setText(newCacheMapping.getTimeoutName());
            enableTimeout(Utils.notEmpty(newCacheMapping.getTimeoutName()));

            jTxtRefreshFieldName.setText(newCacheMapping.getRefreshFieldName());
            enableRefreshField(Utils.notEmpty(newCacheMapping.getRefreshFieldName()));

            jChkHttpGet.setSelected(httpMethods.contains("GET"));	// NOI18N
            jChkHttpPost.setSelected(httpMethods.contains("POST"));	// NOI18N

            keyFieldsPanel.setModelBaseBean(newCacheMapping, asVersion);
            constraintFieldsPanel.setModel(constraints, asVersion);

            if(ASDDVersion.SUN_APPSERVER_8_1.compareTo(asVersion) <= 0) {
                showAS81Fields();
                dispatcherPanel.setModelBaseBean(newCacheMapping, asVersion);
            } else {
                hideAS81Fields();
            }
        } finally {
            initializingFields = false;
        }
	}
    
    // TODO after 5.0, generalize version based field display for multiple (> 2)
    // appserver versions.
    private void showAS81Fields() {
        if(!as81FeaturesVisible) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 6, 5, 5);
            
            int keyFieldsIndex = BaseCustomizer.getComponentIndex(this, keyFieldsPanel);
            if(keyFieldsIndex != -1) {
                add(dispatcherPanel, gridBagConstraints, keyFieldsIndex);
            } else {
                add(dispatcherPanel, gridBagConstraints);
            }
            as81FeaturesVisible = true;
        }
    }
    
    private void hideAS81Fields() {
        if(as81FeaturesVisible) {
            remove(dispatcherPanel);
            as81FeaturesVisible = false;
        }
    }
    
	public void addListeners() {
		dispatcherModel.addTableModelListener(this);
		keyFieldsModel.addTableModelListener(this);
		constraintFieldsModel.addTableModelListener(this);
	}
	
	private void enableTimeout(boolean enabled) {
		timeoutEnabled = enabled;
		jCbxTimeoutScope.setEnabled(enabled);
		jTxtTimeoutValue.setEnabled(enabled);
		jTxtTimeoutValue.setEditable(enabled);
		
		if(!enabled) {
			newCacheMapping.setTimeout(null);
			newCacheMapping.setTimeoutScope(null);
		} else {
			String scope = newCacheMapping.getTimeoutScope();
			if(!Utils.notEmpty(scope)) {
				newCacheMapping.setTimeoutScope("request.attribute");	// NOI18N
			}
		}

		jTxtTimeoutValue.setText(newCacheMapping.getTimeout());
		jCbxTimeoutScope.setSelectedItem(
			getScopeMapping(newCacheMapping.getTimeoutScope(), scopeTypes));
	}
	
	private void enableRefreshField(boolean enabled) {
		refreshFieldEnabled = enabled;
		jCbxRefreshScope.setEnabled(enabled);
		if(!enabled) {
			newCacheMapping.setRefreshField(false);
			newCacheMapping.setRefreshFieldScope(null);
		} else {
			newCacheMapping.setRefreshField(true);
			String scope = newCacheMapping.getRefreshFieldScope();
			if(!Utils.notEmpty(scope)) {
				newCacheMapping.setRefreshFieldScope("request.parameter");	// NOI18N
			}
		}
		
		jCbxRefreshScope.setSelectedItem(
			getScopeMapping(newCacheMapping.getRefreshFieldScope(), keyScopeTypes));
	}
	
	private void handleHttpMethodSelection(ItemEvent evt, String method) {
        if(!initializingFields) {
            if(evt.getStateChange() == ItemEvent.SELECTED) {
                if(!httpMethods.contains(method)) {
                    httpMethods.add(method);
                }
            } else if(evt.getStateChange() == ItemEvent.DESELECTED) {
                if(httpMethods.contains(method)) {
                    httpMethods.remove(method);
                }
            }

            setDataChanged(true);
        }            
	}
	
	private TextMapping getScopeMapping(String xmlKey, final TextMapping [] scopeMap) {
		TextMapping result = null;
		if(xmlKey == null) {
			xmlKey = ""; // NOI18N
		}
		for(int i = 0; i < scopeMap.length; i++) {
			if(scopeMap[i].getXMLString().compareTo(xmlKey) == 0) {
				result = scopeMap[i];
				break;
			}
		}
		return result;
	}
	
	public boolean isDataChanged() {
		return dataChanged;
	}
	
	public void setDataChanged(boolean changed) {
		dataChanged = changed;
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);		
	}

    private String normalizeBlank(String value) {
        return (!value.equals("")) ? value : null;
    }
    
	/** ----------------------------------------------------------------------- 
	 *  Implementation of javax.swing.event.TableModelListener
	 */
	public void tableChanged(TableModelEvent e) {
		Object eventSource = e.getSource();
		if(eventSource == dispatcherModel) {
			// nothing for now
		} else if(eventSource == keyFieldsModel) {
			// nothing for now
		} else if(eventSource == constraintFieldsModel) {
			constraints = constraintFieldsModel.getData();
		}
		
		setDataChanged(true);
	}	
	
	private Collection getErrors() {
		ArrayList errorList = new ArrayList();
		
		String timeoutName = newCacheMapping.getTimeoutName();
		if(Utils.notEmpty(timeoutName)) {
			if(!Utils.isJavaIdentifier(timeoutName)) {
				Object [] args = new Object [2];
				args[0] = webappBundle.getString("LBL_TimeoutName");	// NOI18N
				args[1] = timeoutName;
				errorList.add(MessageFormat.format(
					commonBundle.getString("ERR_NotValidIdentifierForField"), args));	// NOI18N
			}
			
			String timeout = newCacheMapping.getTimeout();
			if(Utils.notEmpty(timeout)) {
				try {
					int value = Integer.parseInt(timeout);
					if(value < 0) {
						Object [] args = new Object[3];
						args[0] = webappBundle.getString("LBL_Timeout"); // NOI18N
						args[1] = timeout;
						args[2] = "0";	// NOI18N
						errorList.add(MessageFormat.format(
							commonBundle.getString("ERR_NumberTooLowForField"), args));	// NOI18N
					}
				} catch(NumberFormatException ex) {
					Object [] args = new Object[2];
					args[0] = webappBundle.getString("LBL_Timeout"); // NOI18N
					args[1] = timeout;
					errorList.add(MessageFormat.format(
						commonBundle.getString("ERR_NumberInvalidForField"), args));	// NOI18N
				}
			}
		}
		
		String refreshFieldName = newCacheMapping.getRefreshFieldName();
		if(Utils.notEmpty(refreshFieldName)) {
			if(!Utils.isJavaIdentifier(refreshFieldName)) {
				Object [] args = new Object [2];
				args[0] = webappBundle.getString("LBL_RefreshFieldName");	// NOI18N
				args[1] = refreshFieldName;
				errorList.add(MessageFormat.format(
					commonBundle.getString("ERR_NotValidIdentifierForField"), args));	// NOI18N
			}
		}
		
		return errorList;
	}
	
	public static boolean invokeAsPopup(JPanel parent, ASDDVersion asVersion, CacheMapping mapping) {
		CachePolicyPanel policyPanel = new CachePolicyPanel(asVersion, mapping);
		policyPanel.displayDialog(parent);
		return policyPanel.isDataChanged();
	}
	
	private void displayDialog(JPanel parent) {
		BetterInputDialog dialog = new BetterInputDialog(parent, 
			webappBundle.getString("CACHE_POLICY_TITLE"), this);	// NOI18N
		
        do {
            if(dialog.display() == dialog.OK_OPTION) {
                if(!dialog.hasErrors()) {
					// Timeout field
					if(Utils.notEmpty(newCacheMapping.getTimeoutName())) {
						theCacheMapping.setTimeout(newCacheMapping.getTimeout());
						theCacheMapping.setTimeoutName(newCacheMapping.getTimeoutName());
						theCacheMapping.setTimeoutScope(newCacheMapping.getTimeoutScope());
					} else {
						theCacheMapping.setTimeout(null);
					}
					
					// Refresh field
					if(Utils.notEmpty(newCacheMapping.getRefreshFieldName())) {
						theCacheMapping.setRefreshField(true);
						theCacheMapping.setRefreshFieldName(newCacheMapping.getRefreshFieldName());
						theCacheMapping.setRefreshFieldScope(newCacheMapping.getRefreshFieldScope());
					} else {
						theCacheMapping.setRefreshField(false);
					}
					
					// HTTP methods -- only member that uses intermediate storage.
					theCacheMapping.setHttpMethod((String []) httpMethods.toArray(new String [0]));
                                        
					// Dispatch fields
					try {
						theCacheMapping.setDispatcher(newCacheMapping.getDispatcher());
//						int numFields = newCacheMapping.sizeDispatcher();
//						theCacheMapping.setDispatcher(new String[numFields]);
//						for(int i = 0; i < numFields; i++) {
//							theCacheMapping.setDispatcher(i, newCacheMapping.getDispatcher(i));
//						}
					} catch(VersionNotSupportedException ex) {
						// !PW Should not happen.
					}

					// Key fields
					int numFields = newCacheMapping.sizeKeyField();
					theCacheMapping.setKeyField(new boolean [numFields]);
					for(int i = 0; i < numFields; i++) {
						theCacheMapping.setKeyField(i, true);
						theCacheMapping.setKeyFieldName(i, newCacheMapping.getKeyFieldName(i));
						theCacheMapping.setKeyFieldScope(i, newCacheMapping.getKeyFieldScope(i));
					}
					
					// Constraint fields
					theCacheMapping.setConstraintField((ConstraintField []) Utils.listToArray(constraints, ConstraintField.class));
                }
            } else {
				// no data change on cancel.
				setDataChanged(false);
				break;
			}
        } while(dialog.hasErrors());
	}
	
	/** !PW FIXME this class is also replicated in SecurityAddNamePanel.java --
	 *  There should be some way to either fix InputDialog directly, or make this
	 *  public and put it in common.
	 */
	private static class BetterInputDialog extends InputDialog {
		private final CachePolicyPanel dialogPanel;
		
		public BetterInputDialog(JPanel parent, String title, CachePolicyPanel childPanel) {
			super(parent, title);
		
			dialogPanel = childPanel;
//			childPanel.setPreferredSize(new Dimension(parent.getWidth()*3/4, 
//				childPanel.getPreferredSize().height));
			getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CachePolicy"));
			getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CachePolicy"));

			dialogPanel.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CachePolicy"));
			dialogPanel.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CachePolicy"));
			
			getContentPane().add(childPanel, BorderLayout.CENTER);
			addListeners();
			pack();
			setLocationInside(parent);
			handleErrorDisplay();
		}
		
		private void addListeners() {
			dialogPanel.addPropertyChangeListener(Constants.USER_DATA_CHANGED, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					handleErrorDisplay();
				}
			});
		}

		private void handleErrorDisplay() {
			ArrayList errors = new ArrayList();
			errors.addAll(dialogPanel.getErrors());
			setErrors(errors);
		}
		
		protected String getHelpId() {
			return HelpContext.HELP_CACHE_MAPPING_POLICY_POPUP;
		}
	}	
    
    // New for migration to sun DD API model.  Factory instance to pass to generic table model
    // to allow it to create constraintField beans.
	static GenericTableModel.ParentPropertyFactory constraintFieldFactory =
        new GenericTableModel.ParentPropertyFactory() {
            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
                return StorageBeanFactory.getStorageBeanFactory(asVersion).createConstraintField();
            }
        };}
