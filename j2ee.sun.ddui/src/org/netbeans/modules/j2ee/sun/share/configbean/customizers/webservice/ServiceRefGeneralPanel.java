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
 * ServiceRefGeneralPanel.java
 *
 * Created on November 2, 2003, 10:25 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ArrayList;
import java.util.List;

import java.awt.GridBagConstraints;
import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty;

import org.netbeans.modules.j2ee.sun.share.configbean.ServiceRef;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicPropertyPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyListMapping;

/**
 *
 * @author Peter Williams
 */
public class ServiceRefGeneralPanel extends javax.swing.JPanel implements TableModelListener, PropertyChangeListener {

	private ServiceRefCustomizer masterPanel;
	private GenericTableModel callPropertiesModel;
	private GenericTablePanel callPropertiesPanel;

	/** Creates new form ServiceRefGeneralPanel */
	public ServiceRefGeneralPanel(ServiceRefCustomizer src) {
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

        jPnlServiceName = new javax.swing.JPanel();
        jLblName = new javax.swing.JLabel();
        jTxtName = new javax.swing.JTextField();
        jLblWsdlOverride = new javax.swing.JLabel();
        jTxtWsdlOverride = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(ServiceRefCustomizer.bundle.getString("ACSN_GeneralTab"));
        getAccessibleContext().setAccessibleDescription(ServiceRefCustomizer.bundle.getString("ACSD_GeneralTab"));
        jPnlServiceName.setLayout(new java.awt.GridBagLayout());

        jLblName.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLblName.setLabelFor(jTxtName);
        jLblName.setText(ServiceRefCustomizer.bundle.getString("LBL_ServiceReferenceName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPnlServiceName.add(jLblName, gridBagConstraints);

        jTxtName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPnlServiceName.add(jTxtName, gridBagConstraints);
        jTxtName.getAccessibleContext().setAccessibleName(ServiceRefCustomizer.bundle.getString("ACSN_ServiceReferenceName"));
        jTxtName.getAccessibleContext().setAccessibleDescription(ServiceRefCustomizer.bundle.getString("ACSD_ServiceReferenceName"));

        jLblWsdlOverride.setLabelFor(jTxtWsdlOverride);
        jLblWsdlOverride.setText(ServiceRefCustomizer.bundle.getString("LBL_WsdlOverride_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPnlServiceName.add(jLblWsdlOverride, gridBagConstraints);

        jTxtWsdlOverride.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtWsdlOverrideKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPnlServiceName.add(jTxtWsdlOverride, gridBagConstraints);
        jTxtWsdlOverride.getAccessibleContext().setAccessibleName(ServiceRefCustomizer.bundle.getString("ACSN_WsdlOverride"));
        jTxtWsdlOverride.getAccessibleContext().setAccessibleDescription(ServiceRefCustomizer.bundle.getString("ACSD_WsdlOverride"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(jPnlServiceName, gridBagConstraints);

    }//GEN-END:initComponents

	private void jTxtWsdlOverrideKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtWsdlOverrideKeyReleased
		// Add your handling code here:
        ServiceRef bean = masterPanel.getBean();
		if(bean != null) {
			try {
				bean.setWsdlOverride(jTxtWsdlOverride.getText());
			} catch(java.beans.PropertyVetoException exception) {
				jTxtWsdlOverride.setText(bean.getWsdlOverride());
			}
		}
	}//GEN-LAST:event_jTxtWsdlOverrideKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblWsdlOverride;
    private javax.swing.JPanel jPnlServiceName;
    private javax.swing.JTextField jTxtName;
    private javax.swing.JTextField jTxtWsdlOverride;
    // End of variables declaration//GEN-END:variables

	private void initUserComponents() {
		/** Add call properties table panel :
		 *  TableEntry list has two properties: Name, Value
		 */
		ArrayList tableColumns = new ArrayList(2);
		tableColumns.add(new GenericTableModel.ValueEntry("Name",				// NOI18N - property name
			ServiceRefCustomizer.bundle.getString("LBL_Name_Column"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.ValueEntry("Value",				// NOI18N - property name
			ServiceRefCustomizer.bundle.getString("LBL_Value_Column"), true));	// NOI18N

        callPropertiesModel = new GenericTableModel(callPropertyFactory, tableColumns);
        callPropertiesModel.addTableModelListener(this);
		callPropertiesPanel = new GenericTablePanel(callPropertiesModel,
			ServiceRefCustomizer.bundle, "CallProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_SERVICE_CALL_PROPERTY_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.SERVICE_REF_CALL_PROPERTIES));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
		add(callPropertiesPanel, gridBagConstraints);
	}

	/** Initialization of all the fields in this panel from the bean that
	 *  was passed in.
	 */
	public void initFields(ServiceRef bean) {
		jTxtName.setText(bean.getServiceRefName());
		jTxtWsdlOverride.setText(bean.getWsdlOverride());

		callPropertiesPanel.setModel(bean.getCallProperties());
	}
	
	protected void addListeners(ServiceRef bean) {
		bean.addPropertyChangeListener(this);
	}

	protected void removeListeners(ServiceRef bean) {
		bean.removePropertyChangeListener(this);
	}

	/**
	 * Implementation of the PropertyChangeListener interface
	 */
	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
		if(ServiceRef.SERVICE_REF_NAME.equals(propertyChangeEvent.getPropertyName())) {
			ServiceRef bean = masterPanel.getBean();
			if(bean != null) {
				jTxtName.setText(bean.getServiceRefName());
			}
		}
	}	

	/** -----------------------------------------------------------------------
	 *  Implementation of javax.swing.event.TableModelListener
	 */
	public void tableChanged(TableModelEvent e) {
		ServiceRef bean = masterPanel.getBean();
		if(bean != null) {
			try {
				bean.setCallProperties(callPropertiesModel.getData());

				// Force property change to be issued by the bean
				bean.setDirty();
			} catch(PropertyVetoException ex) {
				// FIXME undo whatever changed... how?
			}
		}
	}
    
    // New for migration to sun DD API model.  Factory instance to pass to generic table model
    // to allow it to create callProperty beans.
    private static GenericTableModel.ParentPropertyFactory callPropertyFactory =
        new GenericTableModel.ParentPropertyFactory() {
            public CommonDDBean newParentProperty() {
                return StorageBeanFactory.getDefault().createCallProperty();
            }
        };
}
