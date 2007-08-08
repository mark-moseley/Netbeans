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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDTextFieldEditorModel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceEnvRefNode;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/**
 *
 * @author Peter Williams
 */
public class ResourceEnvRefPanel extends BaseSectionNodeInnerPanel {
	
    // data model & version
    private ResourceEnvRefNode resourceEnvRefNode;
    
    public ResourceEnvRefPanel(SectionNodeView sectionNodeView, final ResourceEnvRefNode resourceEnvRefNode, 
            final ASDDVersion version) {
        super(sectionNodeView, version);
        this.resourceEnvRefNode = resourceEnvRefNode;

        initComponents();
        initUserComponents(sectionNodeView);
    }

    private void initUserComponents(SectionNodeView sectionNodeView) {
        SunDescriptorDataObject dataObject = (SunDescriptorDataObject) sectionNodeView.getDataObject();
        XmlMultiViewDataSynchronizer synchronizer = dataObject.getModelSynchronizer();
        addRefreshable(new ItemEditorHelper(jTxtName, new ResourceEnvRefTextFieldEditorModel(synchronizer, ResourceEnvRef.RESOURCE_ENV_REF_NAME)));
        addRefreshable(new ItemEditorHelper(jTxtJndiName, new ResourceEnvRefTextFieldEditorModel(synchronizer, ResourceEnvRef.JNDI_NAME)));
            
        jTxtName.setEditable(!resourceEnvRefNode.getBinding().isBound());
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

        setAlignmentX(LEFT_ALIGNMENT);
        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLblName.setLabelFor(jTxtName);
        jLblName.setText(customizerBundle.getString("LBL_ResourceEnvReferenceName_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(jLblName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(jTxtName, gridBagConstraints);
        jTxtName.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_ResourceEnvReferenceName")); // NOI18N
        jTxtName.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_ResourceEnvReferenceName")); // NOI18N

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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jTxtJndiName, gridBagConstraints);
        jTxtJndiName.getAccessibleContext().setAccessibleName(customizerBundle.getString("ACSN_JNDIName")); // NOI18N
        jTxtJndiName.getAccessibleContext().setAccessibleDescription(customizerBundle.getString("ACSD_JNDIName")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 5);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblJndiName;
    private javax.swing.JLabel jLblName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTxtJndiName;
    private javax.swing.JTextField jTxtName;
    // End of variables declaration//GEN-END:variables

    public String getHelpId() {
        return "AS_CFG_ResourceEnvRef";	// NOI18N
    }
    
    // Model class for handling updates to the text fields
    private class ResourceEnvRefTextFieldEditorModel extends DDTextFieldEditorModel {

        public ResourceEnvRefTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName) {
            super(synchronizer, propertyName);
        }
        
        protected CommonDDBean getBean() {
            return resourceEnvRefNode.getBinding().getSunBean();
        }
        
        @Override
        protected void setValue(String value) {
            super.setValue(value);

            // If this was a virtual bean, commit it to the graph.
            if(resourceEnvRefNode.addVirtualBean()) {
                // update if necessary
            }
        }
    }
}
