/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;

import org.netbeans.modules.web.project.ProjectWebModule;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.openide.util.HelpCtx;

public class CustomizerRun extends JPanel implements WebCustomizer.Panel, DocumentListener, HelpCtx.Provider {
    
    // Helper for storing properties
    private VisualPropertySupport vps;
    private ProjectWebModule wm;
    private Document doc;

    String[] serverInstanceIDs;
    String[] serverNames;
    
    /** Creates new form CustomizerCompile */
    public CustomizerRun(WebProjectProperties webProperties, ProjectWebModule wm) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACS_CustomizeRun_A11YDesc")); //NOI18N

        this.wm = wm;        
        vps = new VisualPropertySupport(webProperties);
    }
    
    public void initValues() {
        Deployment deployment = Deployment.getDefault ();
        serverInstanceIDs = deployment.getServerInstanceIDs ();
        serverNames = new String[serverInstanceIDs.length];
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            serverNames[i] = deployment.getServerDisplayName (deployment.getServerID (serverInstanceIDs [i])) 
             + " (" + deployment.getServerInstanceDisplayName (serverInstanceIDs [i]) + ")"; //NOI18N
        }

        vps.register(jCheckBoxDisplayBrowser, WebProjectProperties.DISPLAY_BROWSER);
        vps.register(jTextFieldRelativeURL, WebProjectProperties.LAUNCH_URL_RELATIVE);
        vps.register(jTextFieldFullURL, WebProjectProperties.LAUNCH_URL_FULL);
        vps.register(jComboBoxServer, serverNames, serverInstanceIDs, WebProjectProperties.J2EE_SERVER_INSTANCE);

        jTextFieldContextPath.setText(wm.getContextPath());
        
        jTextFieldRelativeURL.setEditable(jCheckBoxDisplayBrowser.isSelected());
        doc = jTextFieldContextPath.getDocument();
        doc.addDocumentListener(this);
        jTextFieldRelativeURL.getDocument().addDocumentListener(this);
    } 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();
        jCheckBoxDisplayBrowser = new javax.swing.JCheckBox();
        jLabelContextPathDesc = new javax.swing.JLabel();
        jLabelRelativeURL = new javax.swing.JLabel();
        jTextFieldRelativeURL = new javax.swing.JTextField();
        jLabelFullURL = new javax.swing.JLabel();
        jTextFieldFullURL = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EtchedBorder());
        jLabelContextPath.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_ContextPath_LabelMnemonic").charAt(0));
        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        jLabelContextPath.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPath_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 0);
        add(jLabelContextPath, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 11, 11, 11);
        add(jTextFieldContextPath, gridBagConstraints);
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_ContextPath_A11YDesc"));

        jLabelServer.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_Server_LabelMnemonic").charAt(0));
        jLabelServer.setLabelFor(jComboBoxServer);
        jLabelServer.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(jLabelServer, gridBagConstraints);

        jComboBoxServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServerActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(jComboBoxServer, gridBagConstraints);
        jComboBoxServer.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_Server_A11YDesc"));

        jCheckBoxDisplayBrowser.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_DisplayBrowser_LabelMnemonic").charAt(0));
        jCheckBoxDisplayBrowser.setSelected(true);
        jCheckBoxDisplayBrowser.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DisplayBrowser_JCheckBox"));
        jCheckBoxDisplayBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisplayBrowserActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(jCheckBoxDisplayBrowser, gridBagConstraints);
        jCheckBoxDisplayBrowser.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_DisplayBrowser_A11YDesc"));

        jLabelContextPathDesc.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPathDesc_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 11, 11);
        add(jLabelContextPathDesc, gridBagConstraints);

        jLabelRelativeURL.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_RelativeURL_LabelMnemonic").charAt(0));
        jLabelRelativeURL.setLabelFor(jTextFieldRelativeURL);
        jLabelRelativeURL.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_RelativeURL_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 11, 0);
        add(jLabelRelativeURL, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 11);
        add(jTextFieldRelativeURL, gridBagConstraints);
        jTextFieldRelativeURL.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_RelativeURL_A11YDesc"));

        jLabelFullURL.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_FullURL_LabelMnemonic").charAt(0));
        jLabelFullURL.setLabelFor(jTextFieldFullURL);
        jLabelFullURL.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_FullURL_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 0, 0);
        add(jLabelFullURL, gridBagConstraints);

        jTextFieldFullURL.setEditable(false);
        jTextFieldFullURL.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        add(jTextFieldFullURL, gridBagConstraints);
        jTextFieldFullURL.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_FullURL_A11YDesc"));

    }//GEN-END:initComponents

    private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed
        if (jComboBoxServer.getSelectedIndex() == -1)
            return;
        setFullURL();
    }//GEN-LAST:event_jComboBoxServerActionPerformed

    private void jCheckBoxDisplayBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayBrowserActionPerformed
        jTextFieldRelativeURL.setEditable(jCheckBoxDisplayBrowser.isSelected());
    }//GEN-LAST:event_jCheckBoxDisplayBrowserActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxDisplayBrowser;
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelContextPathDesc;
    private javax.swing.JLabel jLabelFullURL;
    private javax.swing.JLabel jLabelRelativeURL;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JTextField jTextFieldFullURL;
    private javax.swing.JTextField jTextFieldRelativeURL;
    // End of variables declaration//GEN-END:variables
    
    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        if (e.getDocument() == doc)
            setContextPath();
        
        setFullURL();
    }
    
    public void insertUpdate(DocumentEvent e) {
        if (e.getDocument() == doc)
            setContextPath();
        
        setFullURL();
    }
    
    public void removeUpdate(DocumentEvent e) {
        if (e.getDocument() == doc)
            setContextPath();

        setFullURL();
    }
    // End if implementation of DocumentListener -------------------------------

    private void setFullURL() {
        int index = jComboBoxServer.getSelectedIndex();
        StringBuffer fullURL = new StringBuffer();
        fullURL.append(serverNames[index]);
        if (jTextFieldContextPath.getText().startsWith("/")) //NOI18N
            fullURL.append(jTextFieldContextPath.getText().trim().substring(1));
        else
            fullURL.append(jTextFieldContextPath.getText().trim());
        fullURL.append("/");
        fullURL.append(jTextFieldRelativeURL.getText().trim());
        jTextFieldFullURL.setText(fullURL.toString());
    }
    
    private void setContextPath() {
        wm.setContextPath(jTextFieldContextPath.getText().trim());
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }

}
