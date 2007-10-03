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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDTextFieldEditorModel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.TextItemEditorModel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceRefNode;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/**
 *
 * @author Peter Williams
 */
public class ResourceRefPanel extends BaseSectionNodeInnerPanel {
	
    // data model & version
    private ResourceRefNode resourceRefNode;
    
    public ResourceRefPanel(SectionNodeView sectionNodeView, final ResourceRefNode resourceRefNode, 
            final ASDDVersion version) {
        super(sectionNodeView, version);
        this.resourceRefNode = resourceRefNode;
        
        initComponents();
        initUserComponents(sectionNodeView);
    }

    private void initUserComponents(SectionNodeView sectionNodeView) {
        SunDescriptorDataObject dataObject = (SunDescriptorDataObject) sectionNodeView.getDataObject();
        XmlMultiViewDataSynchronizer synchronizer = dataObject.getModelSynchronizer();
        addRefreshable(new ItemEditorHelper(jTxtName, new ResourceRefTextFieldEditorModel(synchronizer, ResourceRef.RES_REF_NAME)));
        addRefreshable(new ItemEditorHelper(jTxtJndiName, new ResourceRefTextFieldEditorModel(synchronizer, ResourceRef.JNDI_NAME)));
        addRefreshable(new ItemEditorHelper(jTxtPrincipalName, new DRPTextFieldEditorModel(synchronizer, DefaultResourcePrincipal.NAME)));
        addRefreshable(new ItemEditorHelper(jTxtPrincipalPassword, new DRPTextFieldEditorModel(synchronizer, DefaultResourcePrincipal.PASSWORD)));
        
        jTxtName.setEditable(!resourceRefNode.getBinding().isBound());
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
        jLblJndiName = new javax.swing.JLabel();
        jTxtJndiName = new javax.swing.JTextField();
        jLblPrincipalName = new javax.swing.JLabel();
        jTxtPrincipalName = new javax.swing.JTextField();
        jLblPrincipalPassword = new javax.swing.JLabel();
        jTxtPrincipalPassword = new javax.swing.JTextField();

        setAlignmentX(LEFT_ALIGNMENT);
        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLblName.setLabelFor(jTxtName);
        jLblName.setText(customizerBundle.getString("LBL_ResourceReferenceName_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(jLblName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jTxtName, gridBagConstraints);
        jTxtName.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_ResourceReferenceName")); // NOI18N
        jTxtName.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_ResourceReferenceName")); // NOI18N

        jLblJndiName.setLabelFor(jTxtJndiName);
        jLblJndiName.setText(customizerBundle.getString("LBL_JNDIName_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(jLblJndiName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jTxtJndiName, gridBagConstraints);
        jTxtJndiName.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_JNDIName")); // NOI18N
        jTxtJndiName.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_JNDIName")); // NOI18N

        jLblPrincipalName.setLabelFor(jTxtPrincipalName);
        jLblPrincipalName.setText(customizerBundle.getString("LBL_ResPrincipalName_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(jLblPrincipalName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jTxtPrincipalName, gridBagConstraints);
        jTxtPrincipalName.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_ResPrincipalName")); // NOI18N
        jTxtPrincipalName.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_ResPrincipalName")); // NOI18N

        jLblPrincipalPassword.setLabelFor(jTxtPrincipalPassword);
        jLblPrincipalPassword.setText(customizerBundle.getString("LBL_ResPrincipalPassword_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(jLblPrincipalPassword, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jTxtPrincipalPassword, gridBagConstraints);
        jTxtPrincipalPassword.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_ResPrincipalPassword")); // NOI18N
        jTxtPrincipalPassword.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_ResPrincipalPassword")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblJndiName;
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblPrincipalName;
    private javax.swing.JLabel jLblPrincipalPassword;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTxtJndiName;
    private javax.swing.JTextField jTxtName;
    private javax.swing.JTextField jTxtPrincipalName;
    private javax.swing.JTextField jTxtPrincipalPassword;
    // End of variables declaration//GEN-END:variables
	
    public String getHelpId() {
        return "AS_CFG_ResourceRef";	// NOI18N
    }
    
    // Model class for handling updates to the text fields
    private class ResourceRefTextFieldEditorModel extends DDTextFieldEditorModel {

        public ResourceRefTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName) {
            super(synchronizer, propertyName);
        }
        
        protected CommonDDBean getBean() {
            return resourceRefNode.getBinding().getSunBean();
        }
        
        @Override
        protected void setValue(String value) {
            super.setValue(value);

            // If this was a virtual bean, commit it to the graph.
            if(resourceRefNode.addVirtualBean()) {
                // update if necessary
            }
        }
    }

    // Model class for handling updates to the text fields
    private class DRPTextFieldEditorModel extends TextItemEditorModel {

        private String propertyName;
        
        public DRPTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName) {
            super(synchronizer, true, true);
            
            this.propertyName = propertyName;
        }
        
        protected String getValue() {
            ResourceRef resourceRef = (ResourceRef) resourceRefNode.getBinding().getSunBean();
            DefaultResourcePrincipal drp = resourceRef.getDefaultResourcePrincipal();
            return (drp != null) ? (String) drp.getValue(propertyName) : null;
        }
        
        protected void setValue(String value) {
            ResourceRef resourceRef = (ResourceRef) resourceRefNode.getBinding().getSunBean();
            DefaultResourcePrincipal drp = resourceRef.getDefaultResourcePrincipal();
            if(drp == null) {
                drp = resourceRef.newDefaultResourcePrincipal();
                resourceRef.setDefaultResourcePrincipal(drp);
            }

            drp.setValue(propertyName, value);

            if(isEmpty(drp)) {
                resourceRef.setDefaultResourcePrincipal(null);
            }
            
            // If this was a virtual bean, commit it to the graph.
            if(resourceRefNode.addVirtualBean()) {
                // update if necessary
            }
        }
    }
    
    private static boolean isEmpty(DefaultResourcePrincipal drp) {
        return Utils.strEmpty(drp.getName()) && 
                Utils.strEmpty(drp.getPassword());
    }
}
