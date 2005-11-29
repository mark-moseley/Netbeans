/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * WebAppSessionConfigPanel.java
 *
 * Created on October 22, 2003, 4:48 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.List;

import java.util.ResourceBundle;

import java.beans.Customizer;
import java.beans.PropertyVetoException;

import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.dd.api.web.ManagerProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.StoreProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.SessionProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.CookieProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;

import org.netbeans.modules.j2ee.sun.share.configbean.ValidationError;
import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.SessionConfigSubBean;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicPropertyPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyListMapping;

/**
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class WebAppSessionConfigPanel extends JPanel implements TableModelListener {

	/** resource bundle */
	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
	/** xml <--> ui mapping for persistence types combo box */
	private static final TextMapping [] persistenceTypes = {
		new TextMapping("", ""), // NOI18N
		new TextMapping("memory", webappBundle.getString("MEMORY_PERSISTENCE_TYPE")),	// NOI18N
		new TextMapping("file", webappBundle.getString("FILE_PERSISTENCE_TYPE")),	// NOI18N
		new TextMapping("ha", webappBundle.getString("HA_PERSISTENCE_TYPE")),		// NOI18N
		new TextMapping("mmap", webappBundle.getString("MMAP_PERSISTENCE_TYPE")),	// NOI18N
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
    private void initComponents() {//GEN-BEGIN:initComponents
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
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 4);
        sessionMgrPanel.add(jLblSessionManager, gridBagConstraints);

        jLblPersistenceType.setLabelFor(jComboPersistenceType);
        jLblPersistenceType.setText(webappBundle.getString("LBL_PersistenceType_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 4, 4);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
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

    }//GEN-END:initComponents

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
        propertyTabbedPanel.addTab(webappBundle.getString("TAB_ManagerProperties"),	// NOI18N	
			mgrPropertiesPanel);

		// add Store Properties table
		storePropertiesModel = new GenericTableModel(StoreProperties.PROPERTY, WebAppRootCustomizer.webPropertyFactory, tableColumns);
		storePropertiesPanel = new GenericTablePanel(storePropertiesModel, 
			webappBundle, "StoreProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_SESSIONCONFIG_STORE_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.CONFIG_STORE_PROPERTIES));
        propertyTabbedPanel.addTab(webappBundle.getString("TAB_StoreProperties"),	// NOI18N	
			storePropertiesPanel);
		
		// add Session Properties table
		sessionPropertiesModel = new GenericTableModel(SessionProperties.PROPERTY, WebAppRootCustomizer.webPropertyFactory, tableColumns);
		sessionPropertiesPanel = new GenericTablePanel(sessionPropertiesModel, 
			webappBundle, "SessionProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_SESSIONCONFIG_SESSION_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.CONFIG_SESSION_PROPERTIES));
        propertyTabbedPanel.addTab(webappBundle.getString("TAB_SessionProperties"),	// NOI18N	
			sessionPropertiesPanel);
		
		// add Cookie Properties table
		cookiePropertiesModel = new GenericTableModel(CookieProperties.PROPERTY, WebAppRootCustomizer.webPropertyFactory, tableColumns);
		cookiePropertiesPanel = new GenericTablePanel(cookiePropertiesModel, 
			webappBundle, "CookieProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_SESSIONCONFIG_COOKIE_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.CONFIG_COOKIE_PROPERTIES));
        propertyTabbedPanel.addTab(webappBundle.getString("TAB_CookieProperties"),	// NOI18N	
			cookiePropertiesPanel);
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
		SessionConfigSubBean sessonConfigBean = theBean.getSessionConfigBean();
		persistenceTypeModel.setSelectedItem(
			getPersistenceTypeMapping(sessonConfigBean.getPersistenceType()));
		
		mgrPropertiesPanel.setModel(sessonConfigBean.getManagerProperties());
		storePropertiesPanel.setModel(sessonConfigBean.getStoreProperties());
		sessionPropertiesPanel.setModel(sessonConfigBean.getSessionProperties());
		cookiePropertiesPanel.setModel(sessonConfigBean.getCookieProperties());
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
