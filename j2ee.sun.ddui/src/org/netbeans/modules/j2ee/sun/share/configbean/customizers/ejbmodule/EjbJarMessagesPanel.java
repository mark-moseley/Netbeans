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
 * EjbJarMessagesPanel.java
 *
 * Created on May 25, 2006, 4:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.ResourceBundle;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyVetoException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;

import org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;


/**
 *
 * @author Peter Williams
 */
public class EjbJarMessagesPanel extends javax.swing.JPanel implements TableModelListener {
	
	private static final ResourceBundle ejbjarBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle");	// NOI18N
	
	private EjbJarRootCustomizer masterPanel;
	
	// Table for editing MessageDestination entries
	private GenericTableModel messageDestinationModel;
	private GenericTablePanel messageDestinationPanel;

	// Table for editing MessageDestinationRef entries
//	private GenericTableModel messageDestinationRefModel;
//	private GenericTablePanel messageDestinationRefPanel;
	
    // true if AS 9.0+ fields are visible.
    private boolean as90FeaturesVisible;
    
	/**
     * Creates new form EjbJarMessagesPanel
     */
	public EjbJarMessagesPanel(EjbJarRootCustomizer src) {
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

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(ejbjarBundle.getString("ACSN_EjbJarMessagesTab"));
        getAccessibleContext().setAccessibleDescription(ejbjarBundle.getString("ACSD_EjbJarMessagesTab"));
    }// </editor-fold>//GEN-END:initComponents
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

	private void initUserComponents() {
        
		as90FeaturesVisible = true;
        
		/* Add message destination table panel :
		 * TableEntry list has two properties: destination name, jndi name
		 */
		ArrayList tableColumns = new ArrayList(2);
		tableColumns.add(new GenericTableModel.ValueEntry(null, MessageDestination.MESSAGE_DESTINATION_NAME, 
			ejbjarBundle, "MessageDestinationName", true, true));	// NOI18N - property name
		tableColumns.add(new GenericTableModel.ValueEntry(null, MessageDestination.JNDI_NAME,
			ejbjarBundle, "JNDIName", true, false));	// NOI18N - property name
		
		messageDestinationModel = new GenericTableModel(messageDestinationFactory, tableColumns);
		messageDestinationPanel = new GenericTablePanel(messageDestinationModel, 
			ejbjarBundle, "MessageDestination",	// NOI18N - property name
			HelpContext.HELP_WEBAPP_MESSAGE_DESTINATION_POPUP);
		
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(6, 6, 0, 5);
		add(messageDestinationPanel, gridBagConstraints);		
        
		/* Add message destination ref table panel :
		 * TableEntry list has two properties: destination ref name, jndi name
		 */
//		tableColumns = new ArrayList(2);
//		tableColumns.add(new GenericTableModel.ValueEntry(null, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME, 
//			webappBundle, "MessageDestinationRefName", true, true));	// NOI18N - property name
//		tableColumns.add(new GenericTableModel.ValueEntry(null, MessageDestinationRef.JNDI_NAME,
//			webappBundle, "JNDIName", true, false));	// NOI18N - property name
//		
//		messageDestinationRefModel = new GenericTableModel(messageDestinationRefFactory, tableColumns);
//		messageDestinationRefPanel = new GenericTablePanel(messageDestinationRefModel, 
//			webappBundle, "MessageDestinationRef",	// NOI18N - property name
//			HelpContext.HELP_WEBAPP_MESSAGE_DESTINATION_REF_POPUP);
//		
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
//		add(messageDestinationRefPanel, gridBagConstraints);		
	}
	
	public void addListeners() {
		messageDestinationModel.addTableModelListener(this);
//		messageDestinationRefModel.addTableModelListener(this);
	}
	
	public void removeListeners() {
//		messageDestinationRefModel.removeTableModelListener(this);
		messageDestinationModel.removeTableModelListener(this);
	}
	
	/** Initialization of all the fields in this panel from the bean that
	 *  was passed in.
	 */
	public void initFields(EjbJarRoot bean) {
		messageDestinationPanel.setModel(bean.getMessageDestinations(), bean.getAppServerVersion());		
        
        if(ASDDVersion.SUN_APPSERVER_9_0.compareTo(bean.getAppServerVersion()) <= 0) {
            showAS90Fields();
//    		messageDestinationRefPanel.setModel(bean.getMessageDestinationRefs(), bean.getAppServerVersion());		
        }
	}
	
    private void showAS90Fields() {
        if(!as90FeaturesVisible) {
            as90FeaturesVisible = true;
//            messageDestinationRefPanel.setVisible(true);
        }
    }
    
    private void hideAS90Fields() {
        if(as90FeaturesVisible) {
            as90FeaturesVisible = false;
//            messageDestinationRefPanel.setVisible(false);
        }
    }
    
	/** ----------------------------------------------------------------------- 
	 *  Implementation of javax.swing.event.TableModelListener
	 */
	public void tableChanged(TableModelEvent e) {
		EjbJarRoot bean = masterPanel.getBean();
		if(bean != null) {
			try {
				Object eventSource = e.getSource();
				if(eventSource == messageDestinationModel) {
					bean.setMessageDestinations(messageDestinationModel.getData());
					bean.firePropertyChange("messageDestination", null, messageDestinationModel.getData());
//				} else if(eventSource == messageDestinationRefModel) {
//					bean.setMessageDestinationRefs(messageDestinationRefModel.getData());
//					bean.firePropertyChange("messageDestinationRef", null, messageDestinationRefModel.getData());
				}
			} catch(PropertyVetoException ex) {
				// FIXME undo whatever changed... how?
			}
		}
	}
    
    // New for migration to sun DD API model.  Factory instance to pass to generic table model
    // to allow it to create messageDestination beans.
	static GenericTableModel.ParentPropertyFactory messageDestinationFactory =
        new GenericTableModel.ParentPropertyFactory() {
            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
                return StorageBeanFactory.getStorageBeanFactory(asVersion).createMessageDestination();
            }
        };
        
//	static GenericTableModel.ParentPropertyFactory messageDestinationRefFactory =
//        new GenericTableModel.ParentPropertyFactory() {
//            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
//                return StorageBeanFactory.getStorageBeanFactory(asVersion).createMessageDestinationRef();
//            }
//        };
}
