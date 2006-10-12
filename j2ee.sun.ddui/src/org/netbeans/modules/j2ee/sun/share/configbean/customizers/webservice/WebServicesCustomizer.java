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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.share.configbean.WebServices;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;


/**
 *
 * @author Peter Williams
 */
public class WebServicesCustomizer extends BaseCustomizer {

    static final ResourceBundle bundle = ResourceBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"); // NOI18N

	/** The bean currently being customized, or null if there isn't one
	 */
	private WebServices theBean;

	/** Creates new form ServiceRefCustomizer */
	public WebServicesCustomizer() {
		initComponents();
		initUserComponents();
	}

	public WebServices getBean() {
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

        jLblWebServicesDescription = new javax.swing.JLabel();
        jPnlFiller = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLblWebServicesDescription.setText(bundle.getString("LBL_WebServiceCustomizerDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 5);
        add(jLblWebServicesDescription, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPnlFiller, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblWebServicesDescription;
    private javax.swing.JPanel jPnlFiller;
    // End of variables declaration//GEN-END:variables

	private void initUserComponents() {
		// Add title panel
		addTitlePanel(bundle.getString("TITLE_WebServices"));
		getAccessibleContext().setAccessibleName(bundle.getString("ACSN_WebServices"));	// NOI18N
		getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_WebServices"));	// NOI18N

        // insert user defined panels here.
        
		// Add error panel
		addErrorPanel();
	}

	protected void initFields() {
	}

	protected void addListeners() {
		super.addListeners();
	}
	
	protected void removeListeners() {
		super.removeListeners();
	}	
	
	protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);

		if(bean instanceof WebServices) {
			theBean = (WebServices) bean;
			result = true;
		} else {
			// if bean is not a WebServices, then it shouldn't have passed Base either.
			assert (result == false) :
				"WebServicesCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N

			theBean = null;
			result = false;
		}

		return result;
	}

	public String getHelpId() {
		return "AS_CFG_WebServices";    // NOI18N
	}
}
