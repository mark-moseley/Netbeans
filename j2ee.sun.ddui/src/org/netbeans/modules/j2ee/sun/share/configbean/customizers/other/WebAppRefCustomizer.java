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
 * WebAppRefCustomizer.java
 *
 * Created on September 4, 2003, 5:28 PM
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.other;

import java.beans.Customizer;
import java.beans.PropertyVetoException;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRef;
import org.netbeans.modules.j2ee.sun.share.configbean.ErrorMessageDB;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerErrorPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;


/**
 *
 * @author Peter Williams
 */
public class WebAppRefCustomizer extends BaseCustomizer {
	
	/** Resource bundle */
	private static final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.other.Bundle"); // NOI18N

	private WebAppRef theBean;

	/** Creates new form WebAppRefCustomizer */
	public WebAppRefCustomizer() {
		initComponents();
		initUserComponents();
	}

	public WebAppRef getBean() {
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

        jPnlGeneral = new javax.swing.JPanel();
        jLblWebUri = new javax.swing.JLabel();
        jTxtWebUri = new javax.swing.JTextField();
        jLblContextRoot = new javax.swing.JLabel();
        jTxtContextRoot = new javax.swing.JTextField();
        jBtnDefault = new javax.swing.JButton();
        jPnlPlaceHolder = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPnlGeneral.setLayout(new java.awt.GridBagLayout());

        jLblWebUri.setDisplayedMnemonic(bundle.getString("MNC_WebUri").charAt(0));
        jLblWebUri.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLblWebUri.setLabelFor(jTxtWebUri);
        jLblWebUri.setText(bundle.getString("LBL_WebURI_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPnlGeneral.add(jLblWebUri, gridBagConstraints);
        jLblWebUri.getAccessibleContext().setAccessibleName(bundle.getString("WebURI_Acsbl_Name"));
        jLblWebUri.getAccessibleContext().setAccessibleDescription(bundle.getString("WebURI_Acsbl_Desc"));

        jTxtWebUri.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPnlGeneral.add(jTxtWebUri, gridBagConstraints);
        jTxtWebUri.getAccessibleContext().setAccessibleName(bundle.getString("WebURI_Acsbl_Name"));
        jTxtWebUri.getAccessibleContext().setAccessibleDescription(bundle.getString("WebURI_Acsbl_Desc"));

        jLblContextRoot.setDisplayedMnemonic(bundle.getString("MNC_ContextRoot").charAt(0));
        jLblContextRoot.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLblContextRoot.setLabelFor(jTxtContextRoot);
        jLblContextRoot.setText(bundle.getString("LBL_ContextRoot_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPnlGeneral.add(jLblContextRoot, gridBagConstraints);
        jLblContextRoot.getAccessibleContext().setAccessibleName(bundle.getString("ContextRoot_Acsbl_Name"));
        jLblContextRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("ContextRoot_Acsbl_Desc"));

        jTxtContextRoot.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtContextRootKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlGeneral.add(jTxtContextRoot, gridBagConstraints);
        jTxtContextRoot.getAccessibleContext().setAccessibleName(bundle.getString("ContextRoot_Acsbl_Name"));
        jTxtContextRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("ContextRoot_Acsbl_Desc"));

        jBtnDefault.setMnemonic(bundle.getString("MNC_Default_ContextRoot").charAt(0));
        jBtnDefault.setText(bundle.getString("LBL_ResetToDefault"));
        jBtnDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDefaultActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlGeneral.add(jBtnDefault, gridBagConstraints);
        jBtnDefault.getAccessibleContext().setAccessibleName(bundle.getString("ResetToDefault_Acsbl_Name"));
        jBtnDefault.getAccessibleContext().setAccessibleDescription(bundle.getString("ResetToDefault_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 5);
        add(jPnlGeneral, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jPnlPlaceHolder, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

	private void jBtnDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDefaultActionPerformed
		// Add your handling code here:
		try {
			theBean.setContextRoot(null);
		} catch(PropertyVetoException ex) {
		}
		
		jTxtContextRoot.setText(theBean.getContextRoot());
	}//GEN-LAST:event_jBtnDefaultActionPerformed

	private void jTxtContextRootKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtContextRootKeyReleased
		// Add your handling code here:
		try {
			theBean.setContextRoot(jTxtContextRoot.getText());
		} catch(PropertyVetoException ex) {
			jTxtContextRoot.setText(theBean.getContextRoot());
		}
	}//GEN-LAST:event_jTxtContextRootKeyReleased
		
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnDefault;
    private javax.swing.JLabel jLblContextRoot;
    private javax.swing.JLabel jLblWebUri;
    private javax.swing.JPanel jPnlGeneral;
    private javax.swing.JPanel jPnlPlaceHolder;
    private javax.swing.JTextField jTxtContextRoot;
    private javax.swing.JTextField jTxtWebUri;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		// Add title panel
		addTitlePanel(bundle.getString("LBL_SunWebModuleRef")); //NOI18N
		
		// Add error panel
		addErrorPanel();
	}		
	
	/** Initialization of all the fields in the customizer from the bean that
	 *  was passed in.
	 */
	protected void initFields() {
		jTxtWebUri.setText(theBean.getWebUri());
		jTxtContextRoot.setText(theBean.getContextRoot());
		
		///jTxtIdentity.setText(theBean.getIdentity());
		///jTxtRefIdentity.setText(theBean.getRefIdentity());
	}
	
	protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);
		
		if(bean instanceof WebAppRef) {
			theBean = (WebAppRef) bean;
			result = true;
		} else {
			// if bean is not a WebAppRef, then it shouldn't have passed Base either.
			assert (result == false) : 
				"WebAppRefCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N
				
			theBean = null;
			result = false;
		}
		
		return result;
	}	
	
	public String getHelpId() {
		return "AS_CFG_WebAppRef";
	}
}
