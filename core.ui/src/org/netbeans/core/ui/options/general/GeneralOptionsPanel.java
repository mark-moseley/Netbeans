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

package org.netbeans.core.ui.options.general;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.beaninfo.editors.HtmlBrowser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class GeneralOptionsPanel extends JPanel implements ActionListener {
    
    private boolean                 changed = false;
    private GeneralOptionsModel     model;
    private HtmlBrowser.FactoryEditor editor;
    private AdvancedProxyPanel advancedPanel;

    
    /** 
     * Creates new form GeneralOptionsPanel. 
     */
    public GeneralOptionsPanel () {
        initComponents ();
        
        loc (lWebBrowser, "Web_Browser");
        loc (lWebProxy, "Web_Proxy");
        loc (lProxyHost, "Proxy_Host");
        loc (lProxyPort, "Proxy_Port");
            
            
        cbWebBrowser.getAccessibleContext ().setAccessibleName (loc ("AN_Web_Browser"));
        cbWebBrowser.getAccessibleContext ().setAccessibleDescription (loc ("AD_Web_Browser"));
        tfProxyHost.getAccessibleContext ().setAccessibleName (loc ("AN_Host"));
        tfProxyHost.getAccessibleContext ().setAccessibleDescription (loc ("AD_Host"));
        tfProxyPort.getAccessibleContext ().setAccessibleName (loc ("AN_Port"));
        tfProxyPort.getAccessibleContext ().setAccessibleDescription (loc ("AD_Port"));
        rbNoProxy.addActionListener (this);
        rbUseSystemProxy.addActionListener (this);
        rbHTTPProxy.addActionListener (this);
        cbWebBrowser.addActionListener (this);
        tfProxyHost.addActionListener (this);
        tfProxyPort.addActionListener (this);
        
        ButtonGroup bgProxy = new ButtonGroup ();
        bgProxy.add (rbNoProxy);
        bgProxy.add (rbUseSystemProxy);
        bgProxy.add (rbHTTPProxy);
        loc (rbNoProxy, "No_Proxy");
        loc (rbUseSystemProxy, "Use_System_Proxy_Settings");
        loc (rbHTTPProxy, "Use_HTTP_Proxy");
        
        // if system proxy setting is not detectable, disable this radio
        // button
        if (System.getProperty("netbeans.system_http_proxy") == null) // NOI18N
            rbUseSystemProxy.setEnabled(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lWebBrowser = new javax.swing.JLabel();
        cbWebBrowser = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        lWebProxy = new javax.swing.JLabel();
        rbNoProxy = new javax.swing.JRadioButton();
        rbUseSystemProxy = new javax.swing.JRadioButton();
        rbHTTPProxy = new javax.swing.JRadioButton();
        lProxyHost = new javax.swing.JLabel();
        tfProxyHost = new javax.swing.JTextField();
        lProxyPort = new javax.swing.JLabel();
        tfProxyPort = new javax.swing.JTextField();
        bMoreProxy = new javax.swing.JButton();
        editBrowserButton = new javax.swing.JButton();

        lWebBrowser.setLabelFor(cbWebBrowser);
        org.openide.awt.Mnemonics.setLocalizedText(lWebBrowser, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.lWebBrowser.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lWebProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_lWebProxy")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rbNoProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.rbNoProxy.text")); // NOI18N
        rbNoProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rbUseSystemProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.rbUseSystemProxy.text")); // NOI18N
        rbUseSystemProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rbHTTPProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Use_HTTP_Proxy", new Object[] {})); // NOI18N
        rbHTTPProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        lProxyHost.setLabelFor(tfProxyHost);
        org.openide.awt.Mnemonics.setLocalizedText(lProxyHost, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Proxy_Host", new Object[] {})); // NOI18N

        tfProxyHost.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfProxyHostFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfProxyHostFocusLost(evt);
            }
        });

        lProxyPort.setLabelFor(tfProxyPort);
        org.openide.awt.Mnemonics.setLocalizedText(lProxyPort, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Proxy_Port", new Object[] {})); // NOI18N

        tfProxyPort.setColumns(4);
        tfProxyPort.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfProxyPortFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfProxyPortFocusLost(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bMoreProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_bMoreProxy")); // NOI18N
        bMoreProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bMoreProxyActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editBrowserButton, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.editBrowserButton.text")); // NOI18N
        editBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBrowserButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(lWebBrowser)
                        .add(18, 18, 18)
                        .add(cbWebBrowser, 0, 903, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editBrowserButton))
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1058, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lWebProxy)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rbNoProxy)
                            .add(rbUseSystemProxy)
                            .add(rbHTTPProxy)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(bMoreProxy)
                                    .add(layout.createSequentialGroup()
                                        .add(lProxyHost)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(tfProxyHost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
                                        .add(12, 12, 12)
                                        .add(lProxyPort)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(tfProxyPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lWebBrowser)
                    .add(cbWebBrowser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(editBrowserButton))
                .add(18, 18, 18)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(rbNoProxy)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rbUseSystemProxy)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rbHTTPProxy))
                    .add(lWebProxy))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lProxyHost)
                    .add(tfProxyPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tfProxyHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lProxyPort))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bMoreProxy)
                .addContainerGap(161, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void editBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBrowserButtonActionPerformed
    WebBrowsersOptionsModel wbModel = new WebBrowsersOptionsModel();
    WebBrowsersOptionsPanel wbPanel = new WebBrowsersOptionsPanel(wbModel);
    DialogDescriptor dialogDesc = new DialogDescriptor (wbPanel, loc("LBL_WebBrowsersPanel_Title"));
    DialogDisplayer.getDefault().createDialog(dialogDesc).setVisible(true);
    if (dialogDesc.getValue().equals(DialogDescriptor.OK_OPTION)) {
        wbModel.applyChanges();
        updateWebBrowsers();
        for (int i = 0, items = cbWebBrowser.getItemCount(); i < items; i++) {
            Object item = cbWebBrowser.getItemAt(i);
            if (item.equals(wbModel.getSelectedValue())) {
                cbWebBrowser.setSelectedItem(item);
                break;
            }
        }
    } else {
        wbModel.discardChanges();
    }
}//GEN-LAST:event_editBrowserButtonActionPerformed

private void bMoreProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMoreProxyActionPerformed
    assert model != null : "Model found when AdvancedProxyPanel is created";
    if (advancedPanel == null) {
        advancedPanel = new AdvancedProxyPanel (model);
    }
    DialogDescriptor dd = new DialogDescriptor (advancedPanel, loc ("LBL_AdvancedProxyPanel_Title"));
    advancedPanel.update (tfProxyHost.getText (), tfProxyPort.getText ());
    DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
    if (DialogDescriptor.OK_OPTION.equals (dd.getValue ())) {
        advancedPanel.applyChanges ();
        tfProxyHost.setText (model.getHttpProxyHost ());
        tfProxyPort.setText (model.getHttpProxyPort ());
        isChanged ();
    }    
}//GEN-LAST:event_bMoreProxyActionPerformed

    private void tfProxyPortFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyPortFocusLost
        tfProxyPort.select (0, 0);
    }//GEN-LAST:event_tfProxyPortFocusLost

    private void tfProxyHostFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyHostFocusLost
        tfProxyHost.select (0, 0);
    }//GEN-LAST:event_tfProxyHostFocusLost

    private void tfProxyPortFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyPortFocusGained
        tfProxyPort.setCaretPosition (0);
        tfProxyPort.selectAll ();        
    }//GEN-LAST:event_tfProxyPortFocusGained

    private void tfProxyHostFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyHostFocusGained
        tfProxyHost.setCaretPosition (0);
        tfProxyHost.selectAll ();
    }//GEN-LAST:event_tfProxyHostFocusGained
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bMoreProxy;
    private javax.swing.JComboBox cbWebBrowser;
    private javax.swing.JButton editBrowserButton;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lProxyHost;
    private javax.swing.JLabel lProxyPort;
    private javax.swing.JLabel lWebBrowser;
    private javax.swing.JLabel lWebProxy;
    private javax.swing.JRadioButton rbHTTPProxy;
    private javax.swing.JRadioButton rbNoProxy;
    private javax.swing.JRadioButton rbUseSystemProxy;
    private javax.swing.JTextField tfProxyHost;
    private javax.swing.JTextField tfProxyPort;
    // End of variables declaration//GEN-END:variables
    
    
    private static String loc (String key) {
        return NbBundle.getMessage (GeneralOptionsPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc ("CTL_" + key)
            );
        } else {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc ("CTL_" + key)
            );
        }
    }
    
    void update () {
        model = new GeneralOptionsModel ();
        
        // proxy settings
        switch (model.getProxyType ()) {
            case 0:
                rbNoProxy.setSelected (true);
                tfProxyHost.setEnabled (false);
                tfProxyPort.setEnabled (false);
                bMoreProxy.setEnabled (false);
                break;
            case 1:
                rbUseSystemProxy.setSelected (true);
                tfProxyHost.setEnabled (false);
                tfProxyPort.setEnabled (false);
                bMoreProxy.setEnabled (false);
                break;
            default:
                rbHTTPProxy.setSelected (true);
                tfProxyHost.setEnabled (true);
                tfProxyPort.setEnabled (true);
                bMoreProxy.setEnabled (true);
                break;
        }
        tfProxyHost.setText (model.getHttpProxyHost ());
        tfProxyPort.setText (model.getHttpProxyPort ());

        updateWebBrowsers();
        
        changed = false;
    }
    
    private void updateWebBrowsers() {
        if (editor == null) {
            editor = Lookup.getDefault().lookup(HtmlBrowser.FactoryEditor.class);
        }
        cbWebBrowser.removeAllItems ();
        String[] tags = editor.getTags ();
        int i, k = tags.length;
        for (i = 0; i < k; i++) {
            cbWebBrowser.addItem (tags [i]);
        }
        cbWebBrowser.setSelectedItem (editor.getAsText ());
    }
    
    void applyChanges () {
        // listening on JTextFields dont work!
        // if (!changed) return; 
        
        if (model == null) return;
        
        // proxy settings
        if (rbNoProxy.isSelected ()) {
            model.setProxyType (0);
        } else
        if (rbUseSystemProxy.isSelected ()) {
            model.setProxyType (1);
        } else {
            model.setProxyType (2);
        }
        
        model.setHttpProxyHost (tfProxyHost.getText ());
        model.setHttpProxyPort (tfProxyPort.getText ());

        // web browser settings
        if (editor == null) {
            editor = Lookup.getDefault().lookup(HtmlBrowser.FactoryEditor.class);
        }
        editor.setAsText ((String) cbWebBrowser.getSelectedItem ());
    }
    
    void cancel () {
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        if (model == null) return false;
        if (!tfProxyHost.getText ().equals (model.getHttpProxyHost ())) return true;
        if (!tfProxyPort.getText ().equals (model.getHttpProxyPort ())) return true;
        return changed;
    }
    
    public void actionPerformed (ActionEvent e) {
        changed = true;
        tfProxyHost.setEnabled (rbHTTPProxy.isSelected ());
        tfProxyPort.setEnabled (rbHTTPProxy.isSelected ());
        bMoreProxy.setEnabled (rbHTTPProxy.isSelected ());
    }
}
