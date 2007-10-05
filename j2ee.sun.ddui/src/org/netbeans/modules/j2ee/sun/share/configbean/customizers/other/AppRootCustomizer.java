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
 * AppRootCustomizer.java
 *
 * Created on September 4, 2003, 5:28 PM
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.other;

import java.beans.Customizer;
import java.beans.PropertyVetoException;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;

import org.netbeans.modules.j2ee.sun.share.configbean.AppRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.ErrorMessageDB;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerErrorPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;


/**
 *
 * @author Peter Williams
 */
public class AppRootCustomizer extends BaseCustomizer {
	
	/** Resource bundle */
	private final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.other.Bundle"); // NOI18N
        
	/** xml <--> ui mapping for pass by reference combo box */
	private final TextMapping [] passByReferenceValues = {
		new TextMapping("", ""),
		new TextMapping("true", commonBundle.getString("LBL_True")),
		new TextMapping("false", commonBundle.getString("LBL_False"))
	};
	
	private AppRoot theBean;
	private DefaultComboBoxModel passByReferenceModel;
	
	/** Creates new form AppRootCustomizer */
	public AppRootCustomizer() {
		initComponents();
		initUserComponents();
	}
	
	public AppRoot getBean() {
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
        jLblPassByReference = new javax.swing.JLabel();
        jCbxPassByReference = new javax.swing.JComboBox();
        jLblRealm = new javax.swing.JLabel();
        jTxtRealm = new javax.swing.JTextField();
        jLblSecurityHelpText = new javax.swing.JLabel();
        jPnlModuleMaps = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPnlGeneral.setLayout(new java.awt.GridBagLayout());

        jLblPassByReference.setDisplayedMnemonic(bundle.getString("MNC_Pass_By_Reference").charAt(0));
        jLblPassByReference.setLabelFor(jCbxPassByReference);
        jLblPassByReference.setText(bundle.getString("LBL_PassByReference_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPnlGeneral.add(jLblPassByReference, gridBagConstraints);
        jLblPassByReference.getAccessibleContext().setAccessibleName(bundle.getString("PassByReference_Acsbl_Name"));
        jLblPassByReference.getAccessibleContext().setAccessibleDescription(bundle.getString("PassByReference_Acsbl_Desc"));

        jCbxPassByReference.setPrototypeDisplayValue("");
        jCbxPassByReference.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxPassByReferenceActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPnlGeneral.add(jCbxPassByReference, gridBagConstraints);
        jCbxPassByReference.getAccessibleContext().setAccessibleName(bundle.getString("PassByReference_Acsbl_Name"));
        jCbxPassByReference.getAccessibleContext().setAccessibleDescription(bundle.getString("PassByReference_Acsbl_Desc"));

        jLblRealm.setDisplayedMnemonic(bundle.getString("MNC_Realm").charAt(0));
        jLblRealm.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLblRealm.setLabelFor(jTxtRealm);
        jLblRealm.setText(bundle.getString("LBL_Realm_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPnlGeneral.add(jLblRealm, gridBagConstraints);
        jLblRealm.getAccessibleContext().setAccessibleName(bundle.getString("Realm_Acsbl_Name"));
        jLblRealm.getAccessibleContext().setAccessibleDescription(bundle.getString("Realm_Acsbl_Desc"));

        jTxtRealm.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtRealmKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.75;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlGeneral.add(jTxtRealm, gridBagConstraints);
        jTxtRealm.getAccessibleContext().setAccessibleName(bundle.getString("Realm_Acsbl_Name"));
        jTxtRealm.getAccessibleContext().setAccessibleDescription(bundle.getString("Realm_Acsbl_Desc"));

        jLblSecurityHelpText.setText(bundle.getString("LBL_SecurityRoleMappingHelp"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPnlGeneral.add(jLblSecurityHelpText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 5);
        add(jPnlGeneral, gridBagConstraints);

        jPnlModuleMaps.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jPnlModuleMaps, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

	private void jTxtRealmKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtRealmKeyReleased
		// Add your handling code here:
		try {
			theBean.setRealm(jTxtRealm.getText());
		} catch(PropertyVetoException ex) {
			jTxtRealm.setText(theBean.getRealm());
		}		
	}//GEN-LAST:event_jTxtRealmKeyReleased

	private void jCbxPassByReferenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxPassByReferenceActionPerformed
		// Add your handling code here:
		TextMapping choice = (TextMapping) passByReferenceModel.getSelectedItem();
		
		if(theBean != null) {
			try {
				theBean.setPassByReference(choice.getXMLString());
			} catch(PropertyVetoException ex) {
				passByReferenceModel.setSelectedItem(
					getPassByReferenceMapping(theBean.getPassByReference()));
			}
		}		
	}//GEN-LAST:event_jCbxPassByReferenceActionPerformed
		
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jCbxPassByReference;
    private javax.swing.JLabel jLblPassByReference;
    private javax.swing.JLabel jLblRealm;
    private javax.swing.JLabel jLblSecurityHelpText;
    private javax.swing.JPanel jPnlGeneral;
    private javax.swing.JPanel jPnlModuleMaps;
    private javax.swing.JTextField jTxtRealm;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		// Add title panel
		addTitlePanel(bundle.getString("LBL_SunApplication")); //NOI18N
		
		// Set up pass-by-reference combobox
		passByReferenceModel = new DefaultComboBoxModel();
		for(int i = 0; i < passByReferenceValues.length; i++) {
			passByReferenceModel.addElement(passByReferenceValues[i]);
		}
		jCbxPassByReference.setModel(passByReferenceModel);
		
		// Add error panel
		addErrorPanel();		
	}
	
	/** Initialization of all the fields in the customizer from the bean that
	 *  was passed in.
	 */
	protected void initFields() {
		passByReferenceModel.setSelectedItem(
			getPassByReferenceMapping(theBean.getPassByReference()));
		jTxtRealm.setText(theBean.getRealm());
	}
	
	private TextMapping getPassByReferenceMapping(String xmlKey) {
		TextMapping result = null;
		if(xmlKey == null) {
			xmlKey = ""; // NOI18N
		}
		
		for(int i = 0; i < passByReferenceValues.length; i++) {
			if(passByReferenceValues[i].getXMLString().compareTo(xmlKey) == 0) {
				result = passByReferenceValues[i];
				break;
			}
		}
		
		return result;
	}	
	
	protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);
		
		if(bean instanceof AppRoot) {
			theBean = (AppRoot) bean;
			result = true;
		} else {
			// if bean is not an AppRoot, then it shouldn't have passed Base either.
			assert (result == false) : 
				"AppRootCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N
				
			theBean = null;
			result = false;
		}
		
		return result;
	}	
	
	public String getHelpId() {
		return "AS_CFG_Application";
	}
}
