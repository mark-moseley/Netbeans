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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.other;

import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.client.JavaWebStartAccess;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.TextItemEditorModel;
import org.netbeans.modules.xml.multiview.ItemCheckBoxHelper;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/**
 *
 * @author Peter Williams
 */
public class AppClientJWSPanel extends BaseSectionNodeInnerPanel {
	
    private final ResourceBundle otherBundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.configbean.customizers.other.Bundle"); // NOI18N

    private SunApplicationClient sunAppClient;

    public AppClientJWSPanel(SectionNodeView sectionNodeView, final SunApplicationClient sunAppClient, final ASDDVersion version) {
        super(sectionNodeView, version);
        this.sunAppClient = sunAppClient;

        initComponents();
        initUserComponents(sectionNodeView);
    }

    private void initUserComponents(SectionNodeView sectionNodeView) {
        showAS90Fields(as90FeaturesVisible);

        SunDescriptorDataObject dataObject = (SunDescriptorDataObject) sectionNodeView.getDataObject();
        XmlMultiViewDataSynchronizer synchronizer = dataObject.getModelSynchronizer();
        if(as90FeaturesVisible) {
            addRefreshable(new ItemEditorHelper(jTxtContextRoot, new SunAppClientTextFieldEditorModel(synchronizer, JavaWebStartAccess.CONTEXT_ROOT)));
            addRefreshable(new ItemEditorHelper(jTxtVendor, new SunAppClientTextFieldEditorModel(synchronizer, JavaWebStartAccess.VENDOR)));
            addRefreshable(new EligibleCheckboxHelper(synchronizer, jChkEligible));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPnlJws = new javax.swing.JPanel();
        jLblContextRoot = new javax.swing.JLabel();
        jTxtContextRoot = new javax.swing.JTextField();
        jLblVendor = new javax.swing.JLabel();
        jTxtVendor = new javax.swing.JTextField();
        jLblEligible = new javax.swing.JLabel();
        jChkEligible = new javax.swing.JCheckBox();

        setAlignmentX(LEFT_ALIGNMENT);
        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        jPnlJws.setOpaque(false);
        jPnlJws.setLayout(new java.awt.GridBagLayout());

        jLblContextRoot.setLabelFor(jTxtContextRoot);
        jLblContextRoot.setText(otherBundle.getString("LBL_ContextRoot_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPnlJws.add(jLblContextRoot, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPnlJws.add(jTxtContextRoot, gridBagConstraints);
        jTxtContextRoot.getAccessibleContext().setAccessibleName(otherBundle.getString("ContextRoot_Acsbl_Name")); // NOI18N
        jTxtContextRoot.getAccessibleContext().setAccessibleDescription(otherBundle.getString("ContextRoot_Acsbl_Desc")); // NOI18N

        jLblVendor.setLabelFor(jTxtVendor);
        jLblVendor.setText(otherBundle.getString("LBL_Vendor_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPnlJws.add(jLblVendor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlJws.add(jTxtVendor, gridBagConstraints);
        jTxtVendor.getAccessibleContext().setAccessibleName(otherBundle.getString("ASCN_Vendor")); // NOI18N
        jTxtVendor.getAccessibleContext().setAccessibleDescription(otherBundle.getString("ASCD_Vendor")); // NOI18N

        jLblEligible.setLabelFor(jChkEligible);
        jLblEligible.setText(otherBundle.getString("LBL_Eligible_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPnlJws.add(jLblEligible, gridBagConstraints);

        jChkEligible.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlJws.add(jChkEligible, gridBagConstraints);
        jChkEligible.getAccessibleContext().setAccessibleName(otherBundle.getString("ASCN_Eligible")); // NOI18N
        jChkEligible.getAccessibleContext().setAccessibleDescription(otherBundle.getString("ASCD_Eligible")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jPnlJws, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
		
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jChkEligible;
    private javax.swing.JLabel jLblContextRoot;
    private javax.swing.JLabel jLblEligible;
    private javax.swing.JLabel jLblVendor;
    private javax.swing.JPanel jPnlJws;
    private javax.swing.JTextField jTxtContextRoot;
    private javax.swing.JTextField jTxtVendor;
    // End of variables declaration//GEN-END:variables

    private void showAS90Fields(boolean visible) {
        jPnlJws.setVisible(visible);
    }
    
    public String getHelpId() {
        return "AS_CFG_AppClient";
    }

    // Model class for handling updates to the text fields
    private class SunAppClientTextFieldEditorModel extends TextItemEditorModel {

        private String propertyName;
        
        public SunAppClientTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName) {
            super(synchronizer, true, true);
            
            this.propertyName = propertyName;
        }
        
        protected String getValue() {
            String result = null;
            
            try {
                JavaWebStartAccess jws = sunAppClient.getJavaWebStartAccess();
                result = (jws != null) ? (String) jws.getValue(propertyName) : null;
            } catch (VersionNotSupportedException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
            return result;
        }
        
        protected void setValue(String value) {
            try {
                JavaWebStartAccess jws = sunAppClient.getJavaWebStartAccess();
                if(jws == null) {
                    jws = sunAppClient.newJavaWebStartAccess();
                    sunAppClient.setJavaWebStartAccess(jws);
                }
                
                jws.setValue(propertyName, value);

                if(isEmpty(jws)) {
                    sunAppClient.setJavaWebStartAccess(null);
                }
            } catch (VersionNotSupportedException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    private class EligibleCheckboxHelper extends ItemCheckBoxHelper {

        public EligibleCheckboxHelper(XmlMultiViewDataSynchronizer synchronizer, JCheckBox component) {
            super(synchronizer, component);
        }

        public boolean getItemValue() {
            try {
                JavaWebStartAccess jws = sunAppClient.getJavaWebStartAccess();
                return (jws != null) ? Utils.booleanValueOf(jws.getEligible()) : false;
            } catch(VersionNotSupportedException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return false;
        }

        public void setItemValue(boolean value) {
            try {
                JavaWebStartAccess jws = sunAppClient.getJavaWebStartAccess();
                if(jws == null) {
                    jws = sunAppClient.newJavaWebStartAccess();
                    sunAppClient.setJavaWebStartAccess(jws);
                }
                
                jws.setEligible(Boolean.toString(value));

                if(isEmpty(jws)) {
                    sunAppClient.setJavaWebStartAccess(null);
                }
            } catch(VersionNotSupportedException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    private static boolean isEmpty(JavaWebStartAccess jws) {
        String eligible = jws.getEligible();
        return Utils.strEmpty(jws.getContextRoot()) && 
                Utils.strEmpty(jws.getVendor()) && 
                (Utils.strEmpty(eligible) || !Utils.booleanValueOf(eligible));
    }
}
