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
 * WebAppClassloaderPanel.java
 *
 * Created on November 5, 2003, 4:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.ResourceBundle;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyVetoException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

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
public class WebAppClassloaderPanel extends javax.swing.JPanel implements TableModelListener {
	
	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	private WebAppRootCustomizer masterPanel;

	// Table for editing Property web classloaderProperties
	private GenericTableModel classloaderPropertiesModel;
	private GenericTablePanel classloaderPropertiesPanel;

    // true if AS 8.1+ fields are visible.
    private boolean as81FeaturesVisible;
    
	/** Creates new form WebAppClassloaderPanel */
	public WebAppClassloaderPanel(WebAppRootCustomizer src) {
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

        jChkClassLoader = new javax.swing.JCheckBox();
        jLblExtraClassPath = new javax.swing.JLabel();
        jTxtExtraClassPath = new javax.swing.JTextField();
        jLblDynamicReloadInterval = new javax.swing.JLabel();
        jTxtDynamicReloadInterval = new javax.swing.JTextField();
        jLblDelegate = new javax.swing.JLabel();
        jChkDelegate = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_WebAppGeneralTab"));
        getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_WebAppGeneralTab"));
        jChkClassLoader.setText(webappBundle.getString("LBL_EnableClassLoader"));
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
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(jChkClassLoader, gridBagConstraints);
        jChkClassLoader.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ClassLoader"));
        jChkClassLoader.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ClassLoader"));

        jLblExtraClassPath.setLabelFor(jTxtExtraClassPath);
        jLblExtraClassPath.setText(webappBundle.getString("LBL_ExtraClassPath_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblExtraClassPath, gridBagConstraints);

        jTxtExtraClassPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtExtraClassPathKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtExtraClassPath, gridBagConstraints);
        jTxtExtraClassPath.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ExtraClassPath"));
        jTxtExtraClassPath.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ExtraClassPath"));

        jLblDynamicReloadInterval.setLabelFor(jTxtDynamicReloadInterval);
        jLblDynamicReloadInterval.setText(webappBundle.getString("LBL_DynamicReloadInterval_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblDynamicReloadInterval, gridBagConstraints);

        jTxtDynamicReloadInterval.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtDynamicReloadIntervalKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtDynamicReloadInterval, gridBagConstraints);
        jTxtDynamicReloadInterval.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_DynamicReloadInterval"));
        jTxtDynamicReloadInterval.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_DynamicReloadInterval"));

        jLblDelegate.setLabelFor(jChkDelegate);
        jLblDelegate.setText(webappBundle.getString("LBL_ClassLoaderDelegate_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblDelegate, gridBagConstraints);

        jChkDelegate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkDelegateItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 5, 5);
        add(jChkDelegate, gridBagConstraints);
        jChkDelegate.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_ClassLoaderDelegate"));
        jChkDelegate.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_ClassLoaderDelegate"));

    }// </editor-fold>//GEN-END:initComponents

    private void jTxtDynamicReloadIntervalKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtDynamicReloadIntervalKeyReleased
        WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			try {
				bean.setDynamicReloadInterval(jTxtDynamicReloadInterval.getText());
			} catch(java.beans.PropertyVetoException exception) {
				jTxtDynamicReloadInterval.setText(bean.getDynamicReloadInterval());
			}
		}
    }//GEN-LAST:event_jTxtDynamicReloadIntervalKeyReleased

	private void jChkDelegateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkDelegateItemStateChanged
        WebAppRoot bean = masterPanel.getBean();
		boolean state = Utils.interpretCheckboxState(evt);
		if(bean != null) {
			try {
				bean.setDelegate(state);
			} catch(java.beans.PropertyVetoException exception) {
				jChkDelegate.setSelected(bean.isDelegate());
			}
		}
	}//GEN-LAST:event_jChkDelegateItemStateChanged

	private void jChkClassLoaderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkClassLoaderItemStateChanged
        WebAppRoot bean = masterPanel.getBean();
		boolean state = Utils.interpretCheckboxState(evt);
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
        WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			try {
				bean.setExtraClassPath(jTxtExtraClassPath.getText());
			} catch(java.beans.PropertyVetoException exception) {
				jTxtExtraClassPath.setText(bean.getExtraClassPath());
			}
		}
	}//GEN-LAST:event_jTxtExtraClassPathKeyReleased
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jChkClassLoader;
    private javax.swing.JCheckBox jChkDelegate;
    private javax.swing.JLabel jLblDelegate;
    private javax.swing.JLabel jLblDynamicReloadInterval;
    private javax.swing.JLabel jLblExtraClassPath;
    private javax.swing.JTextField jTxtDynamicReloadInterval;
    private javax.swing.JTextField jTxtExtraClassPath;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
            
		as81FeaturesVisible = true;
        
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
		
		// add classloaderProperties table
        classloaderPropertiesModel = new GenericTableModel(WebAppRootCustomizer.webPropertyFactory, tableColumns);
		classloaderPropertiesPanel = new GenericTablePanel(classloaderPropertiesModel, 
			webappBundle, "WebProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_WEBAPP_CLASSLOADER_PROPERTY_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.WEBAPP_CLASSLOADER_PROPERTIES));
		
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
		add(classloaderPropertiesPanel, gridBagConstraints);
	}
	
	public void addListeners() {
		classloaderPropertiesModel.addTableModelListener(this);
	}
	
	public void removeListeners() {
		classloaderPropertiesModel.removeTableModelListener(this);
	}
	
	private void enableClassLoaderFields(boolean enabled) {
		jLblExtraClassPath.setEnabled(enabled);
		jTxtExtraClassPath.setEnabled(enabled);
		jTxtExtraClassPath.setEditable(enabled);
        jLblDynamicReloadInterval.setEnabled(enabled);
        jTxtDynamicReloadInterval.setEnabled(enabled);
        jTxtDynamicReloadInterval.setEditable(enabled);
        jLblDelegate.setEnabled(enabled);
		jChkDelegate.setEnabled(enabled);
        
        setContainerEnabled(classloaderPropertiesPanel, enabled);
	}
    
	public void setContainerEnabled(Container container, boolean enabled) {
		Component [] components = container.getComponents();
		for(int i = 0; i < components.length; i++) {
			components[i].setEnabled(enabled);
			if(components[i] instanceof Container) {
				setContainerEnabled((Container) components[i], enabled);
			}
		}
	}
	
	/** Initialization of all the fields in this panel from the bean that
	 *  was passed in.
	 */
	public void initFields(WebAppRoot bean) {
        ASDDVersion asVersion = bean.getAppServerVersion();
		jChkClassLoader.setSelected(bean.isClassLoader());
		jTxtExtraClassPath.setText(bean.getExtraClassPath());
        jTxtDynamicReloadInterval.setText(bean.getDynamicReloadInterval());
		jChkDelegate.setSelected(bean.isDelegate());
		classloaderPropertiesPanel.setModel(bean.getClassLoaderProperties(), asVersion);

        if(ASDDVersion.SUN_APPSERVER_8_1.compareTo(bean.getAppServerVersion()) <= 0) {
            showAS81Fields();
        } else {
            hideAS81Fields();
        }
        
		enableClassLoaderFields(bean.isClassLoader());
	}	
        
    // TODO after 5.0, generalize version based field display for multiple (> 2)
    // appserver versions.
    private void showAS81Fields() {
        if(!as81FeaturesVisible) {
            jLblDynamicReloadInterval.setVisible(true);
            jTxtDynamicReloadInterval.setVisible(true);
            classloaderPropertiesPanel.setVisible(true);
            
            as81FeaturesVisible = true;
        }
    }
    
    private void hideAS81Fields() {
        if(as81FeaturesVisible) {
            jLblDynamicReloadInterval.setVisible(false);
            jTxtDynamicReloadInterval.setVisible(false);
            classloaderPropertiesPanel.setVisible(false);
            
            as81FeaturesVisible = false;
        }
    }
	
	/** ----------------------------------------------------------------------- 
	 *  Implementation of javax.swing.event.TableModelListener
	 */
	public void tableChanged(TableModelEvent e) {
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			try {
				Object eventSource = e.getSource();
				if(eventSource == classloaderPropertiesModel) {
					bean.setClassLoaderProperties(classloaderPropertiesModel.getData());
					bean.firePropertyChange("classloaderProperties", null, classloaderPropertiesModel.getData());
				}
			} catch(PropertyVetoException ex) {
				// FIXME undo whatever changed... how?
			}
		}
	}
}
