/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.uml.propertysupport.options.panels;

import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author  krichard
 */
public class ShowMeDialogsListPanel extends javax.swing.JPanel {
    
    
    /** Creates new form ShowMeDialogsListPanel */
    public ShowMeDialogsListPanel() {
        initComponents();
        
    }
    
    /**
     * Setting all the ui elements to match their respective prefences.
     * This is called in the corresponding UMLOptionsPanel's update method.
     */
    public void load() {
        
        String s = "";
        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);
       
        s = prefs.get("UML_ShowMe_Allow_Lengthy_Searches", PSK_ASK);
        allowLengthySearchesCB.setSelectedItem(s);
        
        s = prefs.get("UML_ShowMe_Automatically_Create_Classifiers", PSK_ASK);
        autoCreateCB.setSelectedItem(s);
        
        s = prefs.get("UML_ShowMe_Delete_Combined_Fragment_Messages", PSK_ASK);
        deleteCombFragCB.setSelectedItem(s);
        
        s = prefs.get("UML_ShowMe_Delete_Connector_Messages", PSK_ASK);
        deleteConnectorCB.setSelectedItem(s);
        
        s = prefs.get("UML_ShowMe_Delete_File_when_Deleting_Artifacts", PSK_ASK);
        deleteFileCB.setSelectedItem(s);
        
        s = prefs.get("UML_ShowMe_Dont_Show_Filter_Warning_Dialog", PSK_ASK);
        filterWarningCB.setSelectedItem(s);
        
        s = prefs.get("UML_ShowMe_Modify_Redefined_Operations", PSK_ASK);
        modifyCB.setSelectedItem(s);
                
        s = prefs.get("UML_ShowMe_Move_Invoked_Operation", PSK_ASK);
        moveInvokedCB.setSelectedItem(s);
        
        s = prefs.get("UML_ShowMe_Overwrite_Existing_Participants", PSK_ASK);
        overwriteCB.setSelectedItem(s);
        
        s = prefs.get("UML_ShowMe_Transform_When_Elements_May_Be_Lost", PSK_ASK);
        transformCB.setSelectedItem(s);
        
        
    }
    
    public void store() {
        
        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);
        int index = -1;
        
        index = allowLengthySearchesCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Allow_Lengthy_Searches", mappedChoices[index]);
        index = autoCreateCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Automatically_Create_Classifiers", mappedChoices[index]);
        index = deleteCombFragCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Delete_Combined_Fragment_Messages", mappedChoices[index]);
        index = deleteConnectorCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Delete_Connector_Messages", mappedChoices[index]);
        index = deleteFileCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Delete_File_when_Deleting_Artifacts", mappedChoices[index]);
        index = filterWarningCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Dont_Show_Filter_Warning_Dialog", mappedChoices[index]);
        index = modifyCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Modify_Redefined_Operations", mappedChoices[index]);
        index = moveInvokedCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Move_Invoked_Operation", mappedChoices[index]);
        index = overwriteCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Overwrite_Existing_Participants", mappedChoices[index]);
        index = transformCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Transform_When_Elements_May_Be_Lost", mappedChoices[index]);
        
    }
    
    public void cancel() {
        //do nothing ;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        mainLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        deleteFileCB = new JComboBox (displayChoices);
        filterWarningCB = new JComboBox (displayChoices);
        transformCB = new JComboBox (displayChoices);
        modifyCB = new JComboBox (displayChoices);
        overwriteCB = new JComboBox (displayChoices);
        deleteConnectorCB = new JComboBox (displayChoices);
        autoCreateCB = new JComboBox (displayChoices);
        deleteCombFragCB = new JComboBox (displayChoices);
        moveInvokedCB = new JComboBox (displayChoices);
        allowLengthySearchesCB = new JComboBox (displayChoices);
        jLabel10 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        mainLabel.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "mainLabel.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel1.text_3")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel2.text_2")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel3.text_2")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel4.text_2")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel5.text_2")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel6.text_2")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel7.text_2")); // NOI18N

        jLabel8.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel8.text_2")); // NOI18N

        jLabel9.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel9.text_1")); // NOI18N

        jLabel10.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel10.text_2")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel8)
                    .add(jLabel9)
                    .add(jLabel10)
                    .add(jLabel7)
                    .add(jLabel6)
                    .add(jLabel5)
                    .add(jLabel4)
                    .add(jLabel3)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(deleteFileCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(filterWarningCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(transformCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(modifyCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(overwriteCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(deleteConnectorCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(autoCreateCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(deleteCombFragCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(moveInvokedCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(allowLengthySearchesCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 181, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deleteFileCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterWarningCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(transformCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(modifyCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(overwriteCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deleteConnectorCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(autoCreateCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deleteCombFragCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(moveInvokedCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(allowLengthySearchesCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainLabel)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(89, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(mainLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(124, 124, 124))
        );

        add(jPanel2, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    
    private String PSK_ASK = "PSK_ASK" ;
    private String PSK_ALWAYS = "PSK_ALWAYS" ;
    private String PSK_NEVER = "PSK_NEVER" ;
    
    private String ASK = NbBundle.getMessage(ShowMeDialogsListPanel.class, "ASK") ;
    private String ALWAYS = NbBundle.getMessage(ShowMeDialogsListPanel.class, "ALWAYS") ;
    private String NEVER = NbBundle.getMessage(ShowMeDialogsListPanel.class, "NEVER") ;
    
    private String[] displayChoices = {ASK, ALWAYS, NEVER} ;
    private String[] mappedChoices = {PSK_ASK, PSK_ALWAYS, PSK_NEVER} ;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox allowLengthySearchesCB;
    private javax.swing.JComboBox autoCreateCB;
    private javax.swing.JComboBox deleteCombFragCB;
    private javax.swing.JComboBox deleteConnectorCB;
    private javax.swing.JComboBox deleteFileCB;
    private javax.swing.JComboBox filterWarningCB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel mainLabel;
    private javax.swing.JComboBox modifyCB;
    private javax.swing.JComboBox moveInvokedCB;
    private javax.swing.JComboBox overwriteCB;
    private javax.swing.JComboBox transformCB;
    // End of variables declaration//GEN-END:variables
    
}
