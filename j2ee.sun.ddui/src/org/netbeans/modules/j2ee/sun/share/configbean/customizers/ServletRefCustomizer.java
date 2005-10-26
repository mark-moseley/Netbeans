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
 * ServletRefCustomizer.java
 *
 * Created on September 4, 2003, 5:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;
import java.util.ResourceBundle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JLabel;

import org.netbeans.modules.j2ee.sun.share.configbean.ServletRef;
import org.netbeans.modules.j2ee.sun.share.configbean.ServletVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;


/**
 *
 * @author Peter Williams
 */
public class ServletRefCustomizer extends BaseCustomizer implements 
	PropertyChangeListener {
	
	private static final ResourceBundle customizerBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.Bundle");	// NOI18N

	private ServletRef theBean;
	private boolean servlet24FeaturesVisible;

    // Web service endpoint help text to point user to web service specific 
    // customizers for endpoint editing.
    private JLabel jLblEndpointHelp;
    
	// Panel to fill in for when the webservice endpoint panel is missing.
	// This keeps the layout manager happy and doing what we want it to do.
	private JPanel fillerPanel;
	
	/** Creates new form ServletRefCustomizer */
	public ServletRefCustomizer() {
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

        jPanel1 = new javax.swing.JPanel();
        jLblName = new javax.swing.JLabel();
        jTxtName = new javax.swing.JTextField();
        jLblRoleUsageDescription = new javax.swing.JLabel();
        jLblRunAsRoleName = new javax.swing.JLabel();
        jTxtRunAsRoleName = new javax.swing.JTextField();
        jLblPrincipalName = new javax.swing.JLabel();
        jTxtPrincipalName = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLblName.setLabelFor(jTxtName);
        jLblName.setText(customizerBundle.getString("LBL_ServletName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jLblName, gridBagConstraints);

        jTxtName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jTxtName, gridBagConstraints);
        jTxtName.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_ServletName"));
        jTxtName.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_ServletName"));

        jLblRoleUsageDescription.setText(customizerBundle.getString("LBL_ServletRunAsDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 4, 4);
        jPanel1.add(jLblRoleUsageDescription, gridBagConstraints);

        jLblRunAsRoleName.setLabelFor(jTxtRunAsRoleName);
        jLblRunAsRoleName.setText(customizerBundle.getString("LBL_RunAsRole_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jLblRunAsRoleName, gridBagConstraints);

        jTxtRunAsRoleName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jTxtRunAsRoleName, gridBagConstraints);
        jTxtRunAsRoleName.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_RunAsRole"));
        jTxtRunAsRoleName.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_RunAsRole"));

        jLblPrincipalName.setLabelFor(jTxtPrincipalName);
        jLblPrincipalName.setText(customizerBundle.getString("LBL_UserInRole_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jLblPrincipalName, gridBagConstraints);

        jTxtPrincipalName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtPrincipalNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jTxtPrincipalName, gridBagConstraints);
        jTxtPrincipalName.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_UserInRole"));
        jTxtPrincipalName.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_UserInRole"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents

	private void jTxtPrincipalNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtPrincipalNameKeyReleased
		// Add your handling code here:
		if(theBean != null) {
			try {
				theBean.setPrincipalName(jTxtPrincipalName.getText());
			} catch(java.beans.PropertyVetoException exception) {
				jTxtPrincipalName.setText(theBean.getPrincipalName());
			}
		}		
	}//GEN-LAST:event_jTxtPrincipalNameKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblPrincipalName;
    private javax.swing.JLabel jLblRoleUsageDescription;
    private javax.swing.JLabel jLblRunAsRoleName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTxtName;
    private javax.swing.JTextField jTxtPrincipalName;
    private javax.swing.JTextField jTxtRunAsRoleName;
    // End of variables declaration//GEN-END:variables

	private void initUserComponents() {
		// Add title panel
		addTitlePanel(customizerBundle.getString("TITLE_Servlet"));	// NOI18N
		getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_Servlet"));	// NOI18N
		getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_Servlet"));	// NOI18N

        // Endpoint help text
        jLblEndpointHelp = new JLabel();
        jLblEndpointHelp.setText(customizerBundle.getString("LBL_EndpointHelp"));
        jLblEndpointHelp.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_EndpointHelp"));
        jLblEndpointHelp.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_EndpointHelp"));
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		add(jLblEndpointHelp, gridBagConstraints);
		servlet24FeaturesVisible = true;

        // Filler panel to push everything to the top.
		fillerPanel = new JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		add(fillerPanel, gridBagConstraints);
		
		// Add error panel
		addErrorPanel();
	}

	protected void initFields() {

		jTxtName.setText(theBean.getServletName());
		
		handleRoleFields();
		
		if(theBean.getJ2EEModuleVersion().compareTo(ServletVersion.SERVLET_2_4) >= 0) {
			showWebServiceEndpointInformation();
		} else {
			hideWebServiceEndpointInformation();
		}
	}
	
	private void handleRoleFields() {
		String runAsRole = theBean.getRunAsRoleName();
		if(runAsRole != null) {
			jLblRunAsRoleName.setEnabled(true);
			jTxtRunAsRoleName.setText(runAsRole);
			jLblPrincipalName.setEnabled(true);
			jTxtPrincipalName.setEditable(true);
			jTxtPrincipalName.setEnabled(true);
			jTxtPrincipalName.setText(theBean.getPrincipalName());
		} else {
			jLblRunAsRoleName.setEnabled(false);
			jTxtRunAsRoleName.setText("");	// NOI18N
			jLblPrincipalName.setEnabled(false);
			jTxtPrincipalName.setEditable(false);
			jTxtPrincipalName.setEnabled(false);
			jTxtPrincipalName.setText("");	// NOI18N
		}
	}
	
	private void showWebServiceEndpointInformation() {
		if(!servlet24FeaturesVisible) {
            jLblEndpointHelp.setVisible(true);
			servlet24FeaturesVisible = true;
		}
	}
	
	private void hideWebServiceEndpointInformation() {
		if(servlet24FeaturesVisible) {
            jLblEndpointHelp.setVisible(false);
			servlet24FeaturesVisible = false;
		}
	}
	
	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
		String eventName = propertyChangeEvent.getPropertyName();
		
		if(ServletRef.SERVLET_NAME.equals(eventName)) {
			jTxtName.setText(theBean.getServletName());
		} else if(ServletRef.RUN_AS_ROLE_NAME.equals(eventName)) {
			handleRoleFields();
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
		
		if(bean instanceof ServletRef) {
			theBean = (ServletRef) bean;
			result = true;
		} else {
			// if bean is not a ServletRef, then it shouldn't have passed Base either.
			assert (result == false) : 
				"ServletRefCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N
				
			theBean = null;
			result = false;
		}
		
		return result;
	}	
	
	public String getHelpId() {
		return "AS_CFG_Servlet";	// NOI18N
	}
}
