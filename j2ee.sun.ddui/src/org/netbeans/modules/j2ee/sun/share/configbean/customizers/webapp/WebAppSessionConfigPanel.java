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
 * WebAppSessionConfigPanel.java
 *
 * Created on October 22, 2003, 4:48 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import java.util.ResourceBundle;
import java.beans.PropertyVetoException;

import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.web.ManagerProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.StoreProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.SessionProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.CookieProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;

import org.netbeans.modules.j2ee.sun.share.configbean.ValidationError;
import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.SessionConfigSubBean;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicPropertyPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyListMapping;
import org.openide.util.NbBundle;

/**
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class WebAppSessionConfigPanel extends JPanel implements TableModelListener {

	/** resource bundle */
	private final ResourceBundle webappBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private final ResourceBundle commonBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
	/** xml <--> ui mapping for persistence types combo box */
	private final TextMapping [] persistenceTypes = {
		new TextMapping("", ""), // NOI18N
		new TextMapping("memory", webappBundle.getString("MEMORY_PERSISTENCE_TYPE")),	// NOI18N
		new TextMapping("file", webappBundle.getString("FILE_PERSISTENCE_TYPE")),	// NOI18N
		new TextMapping("ha", webappBundle.getString("HA_PERSISTENCE_TYPE")),		// NOI18N
		new TextMapping("mmap", webappBundle.getString("MMAP_PERSISTENCE_TYPE")),	// NOI18N
		new TextMapping("replicated", webappBundle.getString("REPLICATED_PERSISTENCE_TYPE")),	// NOI18N
		new TextMapping("s1ws60", webappBundle.getString("S1WS60_PERSISTENCE_TYPE")),	// NOI18N
		new TextMapping("custom", webappBundle.getString("CUSTOM_PERSISTENCE_TYPE")),	// NOI18N
	};
	
	private WebAppRootCustomizer masterPanel;

	private DefaultComboBoxModel persistenceTypeModel;
	
	// Table for editing Manager Properties web properties
	private GenericTableModel mgrPropertiesModel;
	private GenericTablePanel mgrPropertiesPanel;
	
	// Table for editing Store Properties web properties
	private GenericTableModel storePropertiesModel;
	private GenericTablePanel storePropertiesPanel;
	
	// Table for editing Session Properties web properties
	private GenericTableModel sessionPropertiesModel;
	private GenericTablePanel sessionPropertiesPanel;
	
	// Table for editing Cookie Properties web properties
	private GenericTableModel cookiePropertiesModel;
	private GenericTablePanel cookiePropertiesPanel;
	
	/** Creates new form WebAppSessionConfigPanel */
	public WebAppSessionConfigPanel(WebAppRootCustomizer src) {
		masterPanel = src;
		
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

        sessionMgrPanel = new javax.swing.JPanel();
        jLblSessionManager = new javax.swing.JLabel();
        jLblPersistenceType = new javax.swing.JLabel();
        jComboPersistenceType = new javax.swing.JComboBox();
        propertyTabbedPanel = new javax.swing.JTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_WebAppSessionConfigTab"));
        getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_WebAppSessionConfigTab"));
        sessionMgrPanel.setLayout(new java.awt.GridBagLayout());

        jLblSessionManager.setText(webappBundle.getString("LBL_SessionManager"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        sessionMgrPanel.add(jLblSessionManager, gridBagConstraints);

        jLblPersistenceType.setLabelFor(jComboPersistenceType);
        jLblPersistenceType.setText(webappBundle.getString("LBL_PersistenceType_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        sessionMgrPanel.add(jLblPersistenceType, gridBagConstraints);

        jComboPersistenceType.setPrototypeDisplayValue("Ay");
        jComboPersistenceType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboPersistenceTypeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        sessionMgrPanel.add(jComboPersistenceType, gridBagConstraints);
        jComboPersistenceType.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_PersistenceType"));
        jComboPersistenceType.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_PersistenceType"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(sessionMgrPanel, gridBagConstraints);

        propertyTabbedPanel.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(propertyTabbedPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

	private void jComboPersistenceTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboPersistenceTypeActionPerformed
		// Add your handling code here:
		TextMapping type = (TextMapping) persistenceTypeModel.getSelectedItem();
		
		WebAppRoot theBean = masterPanel.getBean();
		if(theBean != null) {
			SessionConfigSubBean sessonConfigBean = theBean.getSessionConfigBean();
			try {
				sessonConfigBean.setPersistenceType(type.getXMLString());
			} catch(PropertyVetoException ex) {
				persistenceTypeModel.setSelectedItem(
					getPersistenceTypeMapping(sessonConfigBean.getPersistenceType()));
			}
		}
	}//GEN-LAST:event_jComboPersistenceTypeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboPersistenceType;
    private javax.swing.JLabel jLblPersistenceType;
    private javax.swing.JLabel jLblSessionManager;
    private javax.swing.JTabbedPane propertyTabbedPanel;
    private javax.swing.JPanel sessionMgrPanel;
    // End of variables declaration//GEN-END:variables
	
	void initUserComponents() {
		// Setup persistence type
		persistenceTypeModel = new DefaultComboBoxModel();
		for(int i = 0; i < persistenceTypes.length; i++) {
			persistenceTypeModel.addElement(persistenceTypes[i]);
		}
		jComboPersistenceType.setModel(persistenceTypeModel);
		
		/** Table column description for a table to edit a WebProperty, used by
		 *  all of the panels on this customizer.
		 *  TableEntry list has three properties: Name, Value, Description
		 */
		ArrayList tableColumns = new ArrayList(3);
		tableColumns.add(new GenericTableModel.AttributeEntry(WebProperty.NAME,
			commonBundle.getString("LBL_Name"), true));		// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(WebProperty.VALUE,
			commonBundle.getString("LBL_Value"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.ValueEntry(WebProperty.DESCRIPTION,
			commonBundle.getString("LBL_Description")));	// NOI18N
        
		// add Manager Properties table
		mgrPropertiesModel = new GenericTableModel(ManagerProperties.PROPERTY, WebAppRootCustomizer.webPropertyFactory, tableColumns);
		mgrPropertiesPanel = new GenericTablePanel(mgrPropertiesModel, 
			webappBundle, "ManagerProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_SESSIONCONFIG_MANAGER_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.CONFIG_MANAGER_PROPERTIES));

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(6, 6, 0, 5);
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new GridBagLayout());
		holderPanel.add(mgrPropertiesPanel, gridBagConstraints);

		propertyTabbedPanel.addTab(webappBundle.getString("TAB_ManagerProperties"),	// NOI18N	
			holderPanel);

		// add Store Properties table
		storePropertiesModel = new GenericTableModel(StoreProperties.PROPERTY, WebAppRootCustomizer.webPropertyFactory, tableColumns);
		storePropertiesPanel = new GenericTablePanel(storePropertiesModel, 
			webappBundle, "StoreProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_SESSIONCONFIG_STORE_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.CONFIG_STORE_PROPERTIES));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(6, 6, 0, 5);
		holderPanel = new JPanel();
		holderPanel.setLayout(new GridBagLayout());
		holderPanel.add(storePropertiesPanel, gridBagConstraints);

        propertyTabbedPanel.addTab(webappBundle.getString("TAB_StoreProperties"),	// NOI18N	
			holderPanel);
		
		// add Session Properties table
		sessionPropertiesModel = new GenericTableModel(SessionProperties.PROPERTY, WebAppRootCustomizer.webPropertyFactory, tableColumns);
		sessionPropertiesPanel = new GenericTablePanel(sessionPropertiesModel, 
			webappBundle, "SessionProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_SESSIONCONFIG_SESSION_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.CONFIG_SESSION_PROPERTIES));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(6, 6, 0, 5);
		holderPanel = new JPanel();
		holderPanel.setLayout(new GridBagLayout());
		holderPanel.add(sessionPropertiesPanel, gridBagConstraints);

        propertyTabbedPanel.addTab(webappBundle.getString("TAB_SessionProperties"),	// NOI18N	
			holderPanel);
		
		// add Cookie Properties table
		cookiePropertiesModel = new GenericTableModel(CookieProperties.PROPERTY, WebAppRootCustomizer.webPropertyFactory, tableColumns);
		cookiePropertiesPanel = new GenericTablePanel(cookiePropertiesModel, 
			webappBundle, "CookieProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_SESSIONCONFIG_COOKIE_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.CONFIG_COOKIE_PROPERTIES));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(6, 6, 0, 5);
		holderPanel = new JPanel();
		holderPanel.setLayout(new GridBagLayout());
		holderPanel.add(cookiePropertiesPanel, gridBagConstraints);

        propertyTabbedPanel.addTab(webappBundle.getString("TAB_CookieProperties"),	// NOI18N	
			holderPanel);
	}
    
	public void addListeners() {
		mgrPropertiesModel.addTableModelListener(this);
		storePropertiesModel.addTableModelListener(this);
		sessionPropertiesModel.addTableModelListener(this);
		cookiePropertiesModel.addTableModelListener(this);
	}
	
	public void removeListeners() {
		mgrPropertiesModel.removeTableModelListener(this);
		storePropertiesModel.removeTableModelListener(this);
		sessionPropertiesModel.removeTableModelListener(this);
		cookiePropertiesModel.removeTableModelListener(this);
	}	
	
	/** Initialization of all the fields in the customizer from the bean that
	 *  was passed in.
	 */
	public void initFields(WebAppRoot theBean) {
        ASDDVersion asVersion = theBean.getAppServerVersion();
        
		SessionConfigSubBean sessonConfigBean = theBean.getSessionConfigBean();
		persistenceTypeModel.setSelectedItem(
			getPersistenceTypeMapping(sessonConfigBean.getPersistenceType()));
		
		mgrPropertiesPanel.setModel(sessonConfigBean.getManagerProperties(), asVersion);
		storePropertiesPanel.setModel(sessonConfigBean.getStoreProperties(), asVersion);
		sessionPropertiesPanel.setModel(sessonConfigBean.getSessionProperties(), asVersion);
		cookiePropertiesPanel.setModel(sessonConfigBean.getCookieProperties(), asVersion);
	}
	
	private TextMapping getPersistenceTypeMapping(String xmlKey) {
		TextMapping result = persistenceTypes[0];	// Default to blank version
		if(xmlKey == null) {
			xmlKey = ""; // NOI18N
		}
		
		for(int i = 0; i < persistenceTypes.length; i++) {
			if(persistenceTypes[i].getXMLString().compareTo(xmlKey) == 0) {
				result = persistenceTypes[i];
				break;
			}
		}
		
		return result;
	}
	
	
	/** ----------------------------------------------------------------------- 
	 *  Implementation of javax.swing.event.TableModelListener
	 */
	public void tableChanged(TableModelEvent e) {
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			bean.setDirty();
		}
	}

	
	/** Returns the help ID for the selected tab within the session configuration
	 *  tab of sun-web-app.
	 *
	 * @return String representing the current active help ID for this tabbed panel.
	 */
	public String getHelpId() {
		String result = "AS_CFG_SessionConfigurationManager";	// NOI18N
		
		// Determine which tab has focus and return help context for that tab.
		switch(propertyTabbedPanel.getSelectedIndex()) {
			case 3:
				result = "AS_CFG_SessionConfigurationCookie";	// NOI18N
				break;
			case 2:
				result = "AS_CFG_SessionConfigurationSession";	// NOI18N
				break;
			case 1:
				result = "AS_CFG_SessionConfigurationStore";	// NOI18N
				break;
		}
		
		return result;
	}
	
	/** Retrieve the partition that should be associated with the current 
	 *  selected tab.
	 *
	 *  @return ValidationError.Partition
	 */
	public ValidationError.Partition getPartition() {
		switch(propertyTabbedPanel.getSelectedIndex()) {
			case 3:
				return ValidationError.PARTITION_SESSION_COOKIE;
			case 2:
				return ValidationError.PARTITION_SESSION_SESSION;
			case 1:
				return ValidationError.PARTITION_SESSION_STORE;
			default:
				return ValidationError.PARTITION_SESSION_MANAGER;
		}
	}	
}
