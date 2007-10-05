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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;

import org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 */
public class WebServiceDescriptorCustomizer extends BaseCustomizer implements 
    /*TableModelListener,*/ PropertyChangeListener {

    final ResourceBundle bundle = NbBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"); // NOI18N

    final ResourceBundle customizerBundle = NbBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.Bundle"); // NOI18N

	/** The bean currently being customized, or null if there isn't one
	 */
	private WebServiceDescriptor theBean;

    // For managing endpoint subpanel
	private SelectedEndpointPanel selectedEndpointPanel;
	private DefaultComboBoxModel endpointModel;
    
    
	/** Creates new form WebServiceDescriptorCustomizer */
	public WebServiceDescriptorCustomizer() {
		initComponents();
		initUserComponents();
	}

	public WebServiceDescriptor getBean() {
		return theBean;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLblName = new javax.swing.JLabel();
        jTxtName = new javax.swing.JTextField();
        jLblWsdlPublishLocation = new javax.swing.JLabel();
        jTxtWsdlPublishLocation = new javax.swing.JTextField();
        jLblPortDescription = new javax.swing.JLabel();
        jLblPortComponentName = new javax.swing.JLabel();
        jCbxPortSelector = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLblName.setLabelFor(jTxtName);
        jLblName.setText(bundle.getString("LBL_WebServiceDescriptionName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(jLblName, gridBagConstraints);
        jLblName.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_WebServiceDescriptionName"));
        jLblName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_WebServiceDescriptionName"));

        jTxtName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(jTxtName, gridBagConstraints);

        jLblWsdlPublishLocation.setLabelFor(jTxtWsdlPublishLocation);
        jLblWsdlPublishLocation.setText(bundle.getString("LBL_WsdlPublishLocation_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(jLblWsdlPublishLocation, gridBagConstraints);
        jLblWsdlPublishLocation.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_WsdlPublishLocation"));
        jLblWsdlPublishLocation.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_WsdlPublishLocation"));

        jTxtWsdlPublishLocation.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtWsdlPublishLocationKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jTxtWsdlPublishLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 5);
        add(jPanel1, gridBagConstraints);

        jLblPortDescription.setText(bundle.getString("LBL_ServiceEndpointDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jLblPortDescription, gridBagConstraints);

        jLblPortComponentName.setLabelFor(jCbxPortSelector);
        jLblPortComponentName.setText(bundle.getString("LBL_PortComponentName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblPortComponentName, gridBagConstraints);

        jCbxPortSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxPortSelectorActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jCbxPortSelector, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void jCbxPortSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxPortSelectorActionPerformed
		EndpointMapping endpointMapping = (EndpointMapping) endpointModel.getSelectedItem();

		if(endpointMapping != null) {
			selectedEndpointPanel.setEndpointMapping(endpointMapping);
		} else {
			selectedEndpointPanel.setEndpointMapping(null);
		}
    }//GEN-LAST:event_jCbxPortSelectorActionPerformed

    private void jTxtWsdlPublishLocationKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtWsdlPublishLocationKeyReleased
        if(theBean != null) {
            String oldWsdlPublishLocation = theBean.getWsdlPublishLocation();
            try {
                String newWsdlPublishLocation = jTxtWsdlPublishLocation.getText().trim();
                if(!newWsdlPublishLocation.equals(oldWsdlPublishLocation)) {
                    theBean.setWsdlPublishLocation(newWsdlPublishLocation);
                    validateField(WebServiceDescriptor.FIELD_WSDL_PUBLISH_LOCATION);
                }
            } catch(java.beans.PropertyVetoException ex) {
                jTxtWsdlPublishLocation.setText(oldWsdlPublishLocation);
            }
        }
    }//GEN-LAST:event_jTxtWsdlPublishLocationKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jCbxPortSelector;
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblPortComponentName;
    private javax.swing.JLabel jLblPortDescription;
    private javax.swing.JLabel jLblWsdlPublishLocation;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTxtName;
    private javax.swing.JTextField jTxtWsdlPublishLocation;
    // End of variables declaration//GEN-END:variables

	private void initUserComponents() {
		// Add title panel
		addTitlePanel(bundle.getString("TITLE_WebServiceDescriptor"));
		getAccessibleContext().setAccessibleName(bundle.getString("ACSN_WebServiceDescriptor"));	// NOI18N
		getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_WebServiceDescriptor"));	// NOI18N

		/** Add selected endpoint panel */
		selectedEndpointPanel = new SelectedEndpointPanel(this);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(0, 6, 0, 5);
        add(selectedEndpointPanel, gridBagConstraints);
        
		// Add error panel
		addErrorPanel();
	}

	protected void initFields() {
		jTxtName.setText(theBean.getWebServiceDescriptionName());
        jTxtWsdlPublishLocation.setText(theBean.getWsdlPublishLocation());
        
        // endpoint combo & panel
		// FIXME set combobox to proper value
		endpointModel = new DefaultComboBoxModel();
		List endpointList = theBean.getWebServiceEndpoints();
		if(endpointList != null) {
			Iterator iter = endpointList.iterator();
			while(iter.hasNext()) {
				WebserviceEndpoint endpoint = (WebserviceEndpoint) iter.next();
				endpointModel.addElement(new EndpointMapping(endpoint));
			}
		}

        // do endpoint panel enabling here, before we initialize the data.
        // Otherwise, this method will undo certain selective enabling that is
        // done on endpoint initialization.
        enableButtonsAndPanels();
        
		jCbxPortSelector.setModel(endpointModel);

		if(endpointModel.getSize() > 0) {
			jCbxPortSelector.setSelectedIndex(0);
		}
	}
    
	private void enableButtonsAndPanels() {
		boolean enabled = (endpointModel.getSize() > 0);
		jCbxPortSelector.setEnabled(enabled);
		selectedEndpointPanel.setContainerEnabled(selectedEndpointPanel, enabled);
	}

	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
		String eventName = propertyChangeEvent.getPropertyName();
		
		if(WebServiceDescriptor.WEBSERVICE_DESCRIPTION_NAME.equals(eventName)) {
			jTxtName.setText(theBean.getWebServiceDescriptionName());
		} else if(WebServiceDescriptor.ENDPOINT_SECURITY_BINDING.equals(eventName)) {
            selectedEndpointPanel.reloadEndpointMapping();
        }
	}
	
	protected void addListeners() {
		super.addListeners();
		theBean.addPropertyChangeListener(this);
	}
	
	protected void removeListeners() {
		super.removeListeners();
		theBean.removePropertyChangeListener(this);
	}	
	
	protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);

		if(bean instanceof WebServiceDescriptor) {
			theBean = (WebServiceDescriptor) bean;
			result = true;
		} else {
			// if bean is not a WebServiceDescriptor, then it shouldn't have passed Base either.
			assert (result == false) :
				"WebServiceDescriptorCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N

			theBean = null;
			result = false;
		}

		return result;
	}

	public String getHelpId() {
		return "AS_CFG_WebServiceDescriptor";    // NOI18N
    }
}
