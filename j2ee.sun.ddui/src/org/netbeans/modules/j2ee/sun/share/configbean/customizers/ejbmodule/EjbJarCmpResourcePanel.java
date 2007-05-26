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
 * EjbJarCmpResourcePanel.java
 *
 * Created on May 25, 2006, 4:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SchemaGeneratorProperties;

import org.netbeans.modules.j2ee.sun.share.configbean.ASDDVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicPropertyPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyListMapping;


/**
 *
 * @author Peter Williams
 */
public class EjbJarCmpResourcePanel extends javax.swing.JPanel implements TableModelListener {

    private static final ResourceBundle ejbjarBundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle");	// NOI18N

    private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

    private EjbJarRootCustomizer masterPanel;

    // Table for editing CMP property entries
    private GenericTableModel cmpPropertiesModel;
    private GenericTablePanel cmpPropertiesPanel;

    // Table for editing CMP schema generator property entries
    private GenericTableModel schemaGeneratorPropertiesModel;
    private GenericTablePanel schemaGeneratorPropertiesPanel;

    
    /**
     * Creates new form EjbJarCmpResourcePanel
     */
    public EjbJarCmpResourcePanel(EjbJarRootCustomizer src) {
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

        cmpResourcePanel = new javax.swing.JPanel();
        jLblJndiName = new javax.swing.JLabel();
        jTxtJndiName = new javax.swing.JTextField();
        jLblVendorName = new javax.swing.JLabel();
        jTxtVendorName = new javax.swing.JTextField();
        tablePrefsPanel = new javax.swing.JPanel();
        jLblCreateOnDeploy = new javax.swing.JLabel();
        jCbxCreateOnDeploy = new javax.swing.JComboBox();
        jLblDropOnUndeploy = new javax.swing.JLabel();
        jCbxDropOnUndeploy = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(ejbjarBundle.getString("ACSN_EjbJarMessagesTab"));
        getAccessibleContext().setAccessibleDescription(ejbjarBundle.getString("ACSD_EjbJarMessagesTab"));
        cmpResourcePanel.setLayout(new java.awt.GridBagLayout());

        jLblJndiName.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Jndi_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        cmpResourcePanel.add(jLblJndiName, gridBagConstraints);

        jTxtJndiName.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cmp_Resource_Jndi_Name_Tool_Tip"));
        jTxtJndiName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtJndiNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        cmpResourcePanel.add(jTxtJndiName, gridBagConstraints);

        jLblVendorName.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Database_Vendor_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        cmpResourcePanel.add(jLblVendorName, gridBagConstraints);

        jTxtVendorName.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Database_Vendor_Name_Tool_Tip"));
        jTxtVendorName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtVendorNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        cmpResourcePanel.add(jTxtVendorName, gridBagConstraints);

        tablePrefsPanel.setLayout(new java.awt.GridBagLayout());

        jLblCreateOnDeploy.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Create_Table_At_Deploy_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        tablePrefsPanel.add(jLblCreateOnDeploy, gridBagConstraints);

        jCbxCreateOnDeploy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        jCbxCreateOnDeploy.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Create_Table_At_Deploy_Tool_Tip"));
        jCbxCreateOnDeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxCreateOnDeployActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        tablePrefsPanel.add(jCbxCreateOnDeploy, gridBagConstraints);

        jLblDropOnUndeploy.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Drop_Table_At_Undeploy_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        tablePrefsPanel.add(jLblDropOnUndeploy, gridBagConstraints);

        jCbxDropOnUndeploy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        jCbxDropOnUndeploy.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Drop_Table_At_Undeploy_Tool_Tip"));
        jCbxDropOnUndeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxDropOnUndeployActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        tablePrefsPanel.add(jCbxDropOnUndeploy, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        cmpResourcePanel.add(tablePrefsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(cmpResourcePanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void jCbxDropOnUndeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxDropOnUndeployActionPerformed
        EjbJarRoot theBean = masterPanel.getBean();
        if(theBean != null) {
            CmpResource cmpResource = theBean.getCmpResource();
            String newDropValue = (String) jCbxDropOnUndeploy.getSelectedItem();
            String oldDropValue = cmpResource.getDropTablesAtUndeploy();
        
            if(!Utils.strEquivalent(oldDropValue, newDropValue)) {
                if(Utils.notEmpty(newDropValue)) {
                    cmpResource.setDropTablesAtUndeploy(newDropValue);
                } else {
                    cmpResource.setDropTablesAtUndeploy(null);
                }

                theBean.firePropertyChange("cmpDropTables", oldDropValue, newDropValue); // NOI18N
                masterPanel.validateField(EjbJarRoot.FIELD_CMP_RESOURCE);
            }
        }
    }//GEN-LAST:event_jCbxDropOnUndeployActionPerformed

    private void jCbxCreateOnDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxCreateOnDeployActionPerformed
        EjbJarRoot theBean = masterPanel.getBean();
        if(theBean != null) {
            CmpResource cmpResource = theBean.getCmpResource();
            String newCreateValue = (String) jCbxCreateOnDeploy.getSelectedItem();
            String oldCreateValue = cmpResource.getCreateTablesAtDeploy();

            if(!Utils.strEquivalent(oldCreateValue, newCreateValue)) {
                if(Utils.notEmpty(newCreateValue)) {
                    cmpResource.setCreateTablesAtDeploy(newCreateValue);
                } else {
                    cmpResource.setCreateTablesAtDeploy(null);
                }

                theBean.firePropertyChange("cmpCreateTables", oldCreateValue, newCreateValue); // NOI18N
                masterPanel.validateField(EjbJarRoot.FIELD_CMP_RESOURCE);
            }
        }
    }//GEN-LAST:event_jCbxCreateOnDeployActionPerformed

    private void jTxtVendorNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtVendorNameKeyReleased
        EjbJarRoot theBean = masterPanel.getBean();
        if(theBean != null) {
            CmpResource cmpResource = theBean.getCmpResource();
            String newVendorName = jTxtVendorName.getText();
            String oldVendorName = cmpResource.getDatabaseVendorName();

            if(!Utils.strEquivalent(oldVendorName, newVendorName)) {
                if(Utils.notEmpty(newVendorName)) {
                    cmpResource.setDatabaseVendorName(newVendorName);
                } else {
                    cmpResource.setDatabaseVendorName(null);
                }

                theBean.firePropertyChange("cmpDBVendorName", oldVendorName, newVendorName); // NOI18N
                masterPanel.validateField(EjbJarRoot.FIELD_CMP_RESOURCE);
            }
        }
    }//GEN-LAST:event_jTxtVendorNameKeyReleased

    private void jTxtJndiNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtJndiNameKeyReleased
        EjbJarRoot theBean = masterPanel.getBean();
        if(theBean != null) {
            CmpResource cmpResource = theBean.getCmpResource();
            String newJndiName = jTxtJndiName.getText();
            String oldJndiName = cmpResource.getJndiName();

            if(!Utils.strEquivalent(oldJndiName, newJndiName)) {
                if(Utils.notEmpty(newJndiName)) {
                    cmpResource.setJndiName(newJndiName);
                } else {
                    cmpResource.setJndiName(null);
                }

                theBean.firePropertyChange("cmpJndiName", oldJndiName, newJndiName); // NOI18N
                masterPanel.validateField(EjbJarRoot.FIELD_CMP_RESOURCE);
            }
        }
    }//GEN-LAST:event_jTxtJndiNameKeyReleased
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cmpResourcePanel;
    private javax.swing.JComboBox jCbxCreateOnDeploy;
    private javax.swing.JComboBox jCbxDropOnUndeploy;
    private javax.swing.JLabel jLblCreateOnDeploy;
    private javax.swing.JLabel jLblDropOnUndeploy;
    private javax.swing.JLabel jLblJndiName;
    private javax.swing.JLabel jLblVendorName;
    private javax.swing.JTextField jTxtJndiName;
    private javax.swing.JTextField jTxtVendorName;
    private javax.swing.JPanel tablePrefsPanel;
    // End of variables declaration//GEN-END:variables

	private void initUserComponents() {
		// Column definitions for both property tables.
		ArrayList tableColumns = new ArrayList(2);
		tableColumns.add(new GenericTableModel.ValueEntry(
			PropertyElement.NAME, commonBundle.getString("LBL_Name"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.ValueEntry(
			PropertyElement.VALUE, commonBundle.getString("LBL_Value"), true));	// NOI18N

		// add CMP properties table
        cmpPropertiesModel = new GenericTableModel(CmpResource.PROPERTY,
            cmpPropertyFactory, tableColumns);
		cmpPropertiesPanel = new GenericTablePanel(cmpPropertiesModel, 
			ejbjarBundle, "CmpProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_EJBJAR_CMP_PROPERTY_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.EJBJAR_CMP_PROPERTIES));
		
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(6, 6, 0, 5);
		add(cmpPropertiesPanel, gridBagConstraints);        
        
		// add schema generator properties table
        schemaGeneratorPropertiesModel = new GenericTableModel(SchemaGeneratorProperties.PROPERTY,
            cmpPropertyFactory, tableColumns);
		schemaGeneratorPropertiesPanel = new GenericTablePanel(schemaGeneratorPropertiesModel, 
			ejbjarBundle, "SchemaGeneratorProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_EJBJAR_SCHEMA_PROPERTY_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.EJBJAR_CMP_SCHEMA_PROPERTIES));
		
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
		add(schemaGeneratorPropertiesPanel, gridBagConstraints);        
	}
	
	public void addListeners() {
        cmpPropertiesModel.addTableModelListener(this);
        schemaGeneratorPropertiesModel.addTableModelListener(this);
    }

    public void removeListeners() {
        schemaGeneratorPropertiesModel.removeTableModelListener(this);
        cmpPropertiesModel.removeTableModelListener(this);
    }

    /** Initialization of all the fields in this panel from the bean that
     *  was passed in.
     */
    public void initFields(EjbJarRoot bean) {
        CmpResource cmpResource = bean.getCmpResource();

        jTxtJndiName.setText(cmpResource.getJndiName());
        jTxtVendorName.setText(cmpResource.getDatabaseVendorName());
        
        jCbxCreateOnDeploy.setSelectedItem(cmpResource.getCreateTablesAtDeploy());
        jCbxDropOnUndeploy.setSelectedItem(cmpResource.getDropTablesAtUndeploy());
        
        cmpPropertiesPanel.setModel(cmpResource, bean.getAppServerVersion());		
        SchemaGeneratorProperties sgp = cmpResource.getSchemaGeneratorProperties();
        if(sgp == null) {
            sgp = cmpResource.newSchemaGeneratorProperties();
        }
        schemaGeneratorPropertiesPanel.setModel(sgp, bean.getAppServerVersion());		
	}
	
    
    /** ----------------------------------------------------------------------- 
     *  Implementation of javax.swing.event.TableModelListener
     */
    public void tableChanged(TableModelEvent e) {
        EjbJarRoot bean = masterPanel.getBean();
        if(bean != null) {
            CmpResource cmpResource = bean.getCmpResource();
            Object eventSource = e.getSource();
            
            // TODO send event on what row actually changed.
            if(eventSource == cmpPropertiesModel) {
                List rows = cmpPropertiesModel.getData();
                bean.firePropertyChange("cmpProperties", null, rows);
                masterPanel.validateField(EjbJarRoot.FIELD_CMP_RESOURCE);
            } else if(eventSource == schemaGeneratorPropertiesModel) {
                SchemaGeneratorProperties sgp = 
                        (SchemaGeneratorProperties) schemaGeneratorPropertiesModel.getDataBaseBean();
                if(sgp.sizePropertyElement() > 0) {
                    cmpResource.setSchemaGeneratorProperties(sgp);
                } else {
                    cmpResource.setSchemaGeneratorProperties(null);
                }
                bean.firePropertyChange("schemaGeneratorProperties", null, sgp);
                masterPanel.validateField(EjbJarRoot.FIELD_CMP_RESOURCE);
            }
        }
    }

    // New for migration to sun DD API model.  Factory instance to pass to generic table model
    // to allow it to create cmp property beans.
	static GenericTableModel.ParentPropertyFactory cmpPropertyFactory =
        new GenericTableModel.ParentPropertyFactory() {
            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
                return StorageBeanFactory.getStorageBeanFactory(asVersion).createPropertyElement();
            }
        };
}
