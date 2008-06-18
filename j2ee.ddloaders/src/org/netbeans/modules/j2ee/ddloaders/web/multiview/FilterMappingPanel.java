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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.openide.util.NbBundle;

/**
 * FilterMappingPanel.java
 * Panel for adding/editing filter mapping data
 *
 * Created on January 24, 2005, 4:22 PM
 * @author  mkuchtiak
 */
public class FilterMappingPanel extends javax.swing.JPanel {
    private FilterMapping fm;
    private boolean hasFilterNames=true;
    private boolean hasServletNames=true;
    private static final String[] dispatcherTypes = {
        "REQUEST","FORWARD", "INCLUDE", "ERROR" //NOI18N
    };
    
    /** Creates new form FilterMappingPanel */
    public FilterMappingPanel(FilterMapping fm, String[] filterNames, String[] servletNames) {
        this.fm=fm;
        initComponents();
        jCheckBox1.setText(dispatcherTypes[0]);
        jCheckBox2.setText(dispatcherTypes[1]);
        jCheckBox3.setText(dispatcherTypes[2]);
        jCheckBox4.setText(dispatcherTypes[3]);
        jCheckBox1.setMnemonic(dispatcherTypes[0].charAt(0)); 
        jCheckBox2.setMnemonic(dispatcherTypes[1].charAt(0));
        jCheckBox3.setMnemonic(dispatcherTypes[2].charAt(0));
        jCheckBox4.setMnemonic(dispatcherTypes[3].charAt(0));
        
        // fill CB1 with filter names
        if(filterNames == null || filterNames.length == 0) {
	    filterNames = new String[1]; 
	    filterNames[0] = NbBundle.getMessage(FilterMappingPanel.class,"LBL_no_filters");
	    hasFilterNames = false; 
	}
        for (int i=0;i<filterNames.length;i++) filterNameCB.addItem(filterNames[i]);
        
        String filterName = fm.getFilterName();
        if (filterName != null) {
            filterNameCB.setSelectedItem(filterName);
        }
        
        // fill CB2 with servlet names
	if(servletNames == null || servletNames.length == 0) {
	    servletNames = new String[1]; 
	    servletNames[0] = NbBundle.getMessage(FilterMappingPanel.class,"LBL_no_servlets");
	    hasServletNames = false; 
	}
        for (int i=0;i<servletNames.length;i++) servletNameCB.addItem(servletNames[i]);
        if (!hasServletNames) {
            jRadioButton2.setEnabled(false);
        }
        
        String servletName = fm.getServletName();
        if (servletName != null) {
            jRadioButton2.setSelected(true); 
            urlTF.setEnabled(false);
            servletNameCB.setSelectedItem(servletName);
        } else {
	    jRadioButton1.setSelected(true);
	    urlTF.setText(fm.getUrlPattern());
            servletNameCB.setEnabled(false);
        }
        
        try {
            String[] dispTypes = fm.getDispatcher();
            for (int i=0;i<dispTypes.length;i++) {
                if (dispatcherTypes[0].equals(dispTypes[i])) jCheckBox1.setSelected(true);
                else if (dispatcherTypes[1].equals(dispTypes[i])) jCheckBox2.setSelected(true);
                else if (dispatcherTypes[2].equals(dispTypes[i])) jCheckBox3.setSelected(true);
                else if (dispatcherTypes[3].equals(dispTypes[i])) jCheckBox4.setSelected(true);
            }
        } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {}
    }
    
    javax.swing.JTextField getUrlTF() {
        return urlTF;
    }
    javax.swing.JRadioButton getUrlRB() {
        return jRadioButton1;
    }
    javax.swing.JRadioButton getServletNameRB() {
        return jRadioButton2;
    }
    String getUrlPattern() {
        return (jRadioButton1.isSelected()?urlTF.getText().trim():null);
    }
    String getServletName() {
        return (jRadioButton2.isSelected()?(String)servletNameCB.getSelectedItem():null);
    }
    String getFilterName() {
        return (hasFilterNames?(String)filterNameCB.getSelectedItem():null);
    }
    String[] getDispatcherTypes() {
        java.util.List list = new java.util.ArrayList();
        if (jCheckBox1.isSelected()) list.add(dispatcherTypes[0]);
        if (jCheckBox2.isSelected()) list.add(dispatcherTypes[1]);
        if (jCheckBox3.isSelected()) list.add(dispatcherTypes[2]);
        if (jCheckBox4.isSelected()) list.add(dispatcherTypes[3]);
        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }
    
    boolean hasFilterNames() {
        return hasFilterNames;
    }
    boolean hasServletNames() {
        return hasServletNames;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        filterNameCB = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        servletNameCB = new javax.swing.JComboBox();
        urlTF = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_filterName_mnem").charAt(0));
        jLabel1.setLabelFor(filterNameCB);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_filterName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(filterNameCB, gridBagConstraints);
        filterNameCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_filter_name")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_dispatcherTypes")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel4, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(jCheckBox1);
        jCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_req_box")); // NOI18N

        jPanel1.add(jCheckBox2);
        jCheckBox2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_fw_box")); // NOI18N

        jPanel1.add(jCheckBox3);
        jCheckBox3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_inc_box")); // NOI18N

        jPanel1.add(jCheckBox4);
        jCheckBox4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_err_box")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jPanel1, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
        jPanel2.add(servletNameCB, gridBagConstraints);

        urlTF.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 5);
        jPanel2.add(urlTF, gridBagConstraints);
        urlTF.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_filter_mapping_url_text_field")); // NOI18N
        urlTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_filter_mapping_url_text_field")); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setMnemonic(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_urlPattern_mnem").charAt(0));
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_urlPattern")); // NOI18N
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 0);
        jPanel2.add(jRadioButton1, gridBagConstraints);
        jRadioButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_url_pattern")); // NOI18N

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setMnemonic(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_servletName_mnem1").charAt(0));
        jRadioButton2.setText(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_servletName")); // NOI18N
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 0);
        jPanel2.add(jRadioButton2, gridBagConstraints);
        jRadioButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_servlet_name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jPanel2, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_applyTo")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
            urlTF.setEnabled(false);
            servletNameCB.setEnabled(true);
            jRadioButton2.requestFocus();
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
            urlTF.setEnabled(true);
            servletNameCB.setEnabled(false);
            urlTF.requestFocus();
    }//GEN-LAST:event_jRadioButton1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox filterNameCB;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JComboBox servletNameCB;
    private javax.swing.JTextField urlTF;
    // End of variables declaration//GEN-END:variables
    
}
