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
 * WebAppGeneralPanel.java
 *
 * Created on November 5, 2003, 4:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.ResourceBundle;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.beans.PropertyVetoException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;

import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicPropertyPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyListMapping;


/**
 *
 * @author Peter Williams
 */
public class WebAppGeneralPanel extends javax.swing.JPanel implements TableModelListener {
	
	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	private WebAppRootCustomizer masterPanel;

	// Table for editing JspConfig web properties
	private GenericTableModel jspConfigModel;
	private GenericTablePanel jspConfigPanel;

	// Table for editing Property web properties
	private GenericTableModel propertiesModel;
	private GenericTablePanel propertiesPanel;

    // Table for editing Property idempotent url patterns
    private GenericTableModel idempotentUrlPatternModel;
    private GenericTablePanel idempotentUrlPatternPanel;

	/** Creates new form WebAppGeneralPanel */
	public WebAppGeneralPanel(WebAppRootCustomizer src) {
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

        jLblContextRoot = new javax.swing.JLabel();
        jTxtContextRoot = new javax.swing.JTextField();
        jPnlWebApp = new javax.swing.JPanel();
        jChkClassLoader = new javax.swing.JCheckBox();
        jLblExtraClassPath = new javax.swing.JLabel();
        jTxtExtraClassPath = new javax.swing.JTextField();
        jChkDelegate = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_WebAppGeneralTab"));
        getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_WebAppGeneralTab"));
        jLblContextRoot.setLabelFor(jTxtContextRoot);
        jLblContextRoot.setText(webappBundle.getString("LBL_ContextRoot_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 4);
        add(jLblContextRoot, gridBagConstraints);

        jTxtContextRoot.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtContextRootKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jTxtContextRoot, gridBagConstraints);
        jTxtContextRoot.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ContextRoot"));
        jTxtContextRoot.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ContextRoot"));

        jPnlWebApp.setLayout(new java.awt.GridBagLayout());

        jChkClassLoader.setText(webappBundle.getString("LBL_ClassLoader"));
        jChkClassLoader.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkClassLoaderItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        jPnlWebApp.add(jChkClassLoader, gridBagConstraints);
        jChkClassLoader.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ClassLoader"));
        jChkClassLoader.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ClassLoader"));

        jLblExtraClassPath.setLabelFor(jTxtExtraClassPath);
        jLblExtraClassPath.setText(webappBundle.getString("LBL_ExtraClassPath_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipady = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 0, 4);
        jPnlWebApp.add(jLblExtraClassPath, gridBagConstraints);

        jTxtExtraClassPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtExtraClassPathKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPnlWebApp.add(jTxtExtraClassPath, gridBagConstraints);
        jTxtExtraClassPath.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ExtraClassPath"));
        jTxtExtraClassPath.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ExtraClassPath"));

        jChkDelegate.setText(webappBundle.getString("LBL_ClassLoaderDelegate_1"));
        jChkDelegate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkDelegateItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 4);
        jPnlWebApp.add(jChkDelegate, gridBagConstraints);
        jChkDelegate.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ClassLoaderDelegate"));
        jChkDelegate.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ClassLoaderDelegate"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jPnlWebApp, gridBagConstraints);

    }//GEN-END:initComponents

	private void jChkDelegateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkDelegateItemStateChanged
		// Add your handling code here:
        WebAppRoot bean = masterPanel.getBean();
		boolean state = interpretCheckboxState(evt);
		if(bean != null) {
			try {
				bean.setDelegate(state);
			} catch(java.beans.PropertyVetoException exception) {
				jChkDelegate.setSelected(bean.isDelegate());
			}
		}
	}//GEN-LAST:event_jChkDelegateItemStateChanged

	private void jChkClassLoaderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkClassLoaderItemStateChanged
		// Add your handling code here:
        WebAppRoot bean = masterPanel.getBean();
		boolean state = interpretCheckboxState(evt);
		if(bean != null) {
			try {
				bean.setClassLoader(state);
			} catch(java.beans.PropertyVetoException exception) {
				jChkClassLoader.setSelected(bean.isClassLoader());
			}
		}
		enableClassLoaderFields(state);
	}//GEN-LAST:event_jChkClassLoaderItemStateChanged

	private void jTxtExtraClassPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtExtraClassPathKeyReleased
		// Add your handling code here:
        WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			try {
				bean.setExtraClassPath(jTxtExtraClassPath.getText());
			} catch(java.beans.PropertyVetoException exception) {
				jTxtExtraClassPath.setText(bean.getExtraClassPath());
			}
		}		
	}//GEN-LAST:event_jTxtExtraClassPathKeyReleased

	private void jTxtContextRootKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtContextRootKeyReleased
		// Add your handling code here:
        WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			try {
				bean.setContextRoot(jTxtContextRoot.getText());
				
//				masterPanel.validateField(WebAppRoot.FIELD_CONTEXT_ROOT);
			} catch(java.beans.PropertyVetoException exception) {
				jTxtContextRoot.setText(bean.getContextRoot());
			}
		}
	}//GEN-LAST:event_jTxtContextRootKeyReleased
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jChkClassLoader;
    private javax.swing.JCheckBox jChkDelegate;
    private javax.swing.JLabel jLblContextRoot;
    private javax.swing.JLabel jLblExtraClassPath;
    private javax.swing.JPanel jPnlWebApp;
    private javax.swing.JTextField jTxtContextRoot;
    private javax.swing.JTextField jTxtExtraClassPath;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		/** Add call properties table panel :
		 *  TableEntry list has three properties: Name, Value, Description
		 */
		ArrayList tableColumns = new ArrayList(3);
		tableColumns.add(new GenericTableModel.AttributeEntry(
			WebProperty.NAME, commonBundle.getString("LBL_Name"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(
			WebProperty.VALUE, commonBundle.getString("LBL_Value"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.ValueEntry(
			WebProperty.DESCRIPTION, commonBundle.getString("LBL_Description")));	// NOI18N		
		
		// add JspConfig table
		jspConfigModel = new GenericTableModel(JspConfig.PROPERTY, WebAppRootCustomizer.webPropertyFactory, tableColumns);
		jspConfigPanel = new GenericTablePanel(jspConfigModel, 
			webappBundle, "JspConfigProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_WEBAPP_JSPCONFIG_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.WEBAPP_JSPCONFIG_PROPERTIES));
		
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		add(jspConfigPanel, gridBagConstraints);		
		
		// add Properties table
        propertiesModel = new GenericTableModel(WebAppRootCustomizer.webPropertyFactory, tableColumns);
		propertiesPanel = new GenericTablePanel(propertiesModel, 
			webappBundle, "WebProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_WEBAPP_PROPERTY_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.WEBAPP_PROPERTIES));
		
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		add(propertiesPanel, gridBagConstraints);
                
        // add idempotentUrlPattern table
        tableColumns = new ArrayList(2);
        tableColumns.add(new GenericTableModel.AttributeEntry(SunWebApp.IDEMPOTENT_URL_PATTERN, "UrlPattern", // NOI18N 
            webappBundle, "UrlPattern", true, false)); // NOI18N
        tableColumns.add(new GenericTableModel.AttributeEntry(SunWebApp.IDEMPOTENT_URL_PATTERN, "NumOfRetries", // NOI18N 
            webappBundle, "NumOfRetries", true, false)); // NOI18N
        
        idempotentUrlPatternModel = new GenericTableModel(SunWebApp.IDEMPOTENT_URL_PATTERN, tableColumns);
        idempotentUrlPatternPanel = new GenericTablePanel(idempotentUrlPatternModel, 
            webappBundle, "IdempotentUrlPatterns", // NOI18N - property name
            HelpContext.HELP_WEBAPP_IDEMPOTENTURLPATTERN_POPUP);
        idempotentUrlPatternPanel.setHeadingMnemonic(webappBundle.getString("MNE_IdempotentUrlPatterns").charAt(0)); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        add(idempotentUrlPatternPanel, gridBagConstraints);		
	}
	
	public void addListeners() {
		jspConfigModel.addTableModelListener(this);
        propertiesModel.addTableModelListener(this);
        idempotentUrlPatternModel.addTableModelListener(this);
	}
	
	public void removeListeners() {
		jspConfigModel.removeTableModelListener(this);
        propertiesModel.removeTableModelListener(this);
        idempotentUrlPatternModel.removeTableModelListener(this);
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
	
	private void enableClassLoaderFields(boolean enabled) {
		jLblExtraClassPath.setEnabled(enabled);
		jTxtExtraClassPath.setEnabled(enabled);
		jTxtExtraClassPath.setEditable(enabled);
		jChkDelegate.setEnabled(enabled);
	}
	
	/** Initialization of all the fields in this panel from the bean that
	 *  was passed in.
	 */
	public void initFields(WebAppRoot bean) {
		jTxtContextRoot.setText(bean.getContextRoot());
		jChkClassLoader.setSelected(bean.isClassLoader());
		jTxtExtraClassPath.setText(bean.getExtraClassPath());
		jChkDelegate.setSelected(bean.isDelegate());

		jspConfigPanel.setModel(bean.getJspConfig());
		propertiesPanel.setModel(bean.getProperties());
                idempotentUrlPatternPanel.setModelBaseBean(bean.getIdempotentUrlPattern());

		enableClassLoaderFields(bean.isClassLoader());
	}	
	
	/** ----------------------------------------------------------------------- 
	 *  Implementation of javax.swing.event.TableModelListener
	 */
	public void tableChanged(TableModelEvent e) {
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			try {
				Object eventSource = e.getSource();
				if(eventSource == jspConfigModel) {
					// This statement will not produce a change event because
					// of the way we handle property storage for properties like
					// this one.  (The JspConfig we're modifying already is the
					// one owned by the bean).
					//
//					bean.setJspConfig((JspConfig) jspConfigModel.getDataBaseBean());
				} else if(eventSource == propertiesModel) {
					bean.setProperties(propertiesModel.getData());
				} else if(eventSource == idempotentUrlPatternModel) {
                                    // Nothing to do, same deal as JspConfig
                                }
				
				// Force property change to be issued by the bean
				bean.setDirty();
			} catch(PropertyVetoException ex) {
				// FIXME undo whatever changed... how?
			}
		}
	}
}
