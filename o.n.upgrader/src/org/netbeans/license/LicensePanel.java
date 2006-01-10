/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.license;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/**
 * This class displays license during first start of IDE.
 *
 * @author  Marek Slama
 */

final class LicensePanel extends javax.swing.JPanel {
    
    /** Creates new form LicensePanel */
    public LicensePanel(URL url) {
        this.url = url;
        initComponents();
        initAccessibility();
        try {
            jEditorPane1.setPage(url);
        } catch (IOException exc) {
            //Problem with locating file
            System.err.println("Exception: " + exc.getMessage()); //NOI18N
            exc.printStackTrace();
        }
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleName
        (bundle.getString("ACSN_LicensePanel"));
        this.getAccessibleContext().setAccessibleDescription
        (bundle.getString("ACSD_LicensePanel"));
        
        jEditorPane1.getAccessibleContext().setAccessibleName
        (bundle.getString("ACSN_EditorPane"));
        jEditorPane1.getAccessibleContext().setAccessibleDescription
        (bundle.getString("ACSD_EditorPane"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jTextAreaTop = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jTextAreaBottom = new javax.swing.JTextArea();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 11));
        jTextAreaTop.setBackground(getBackground());
        jTextAreaTop.setColumns(20);
        jTextAreaTop.setEditable(false);
        jTextAreaTop.setFont(new java.awt.Font("Dialog", 1, 12));
        jTextAreaTop.setLineWrap(true);
        jTextAreaTop.setRows(1);
        jTextAreaTop.setText(bundle.getString("MSG_LicenseDlgLabelTop"));
        jTextAreaTop.setWrapStyleWord(true);
        jTextAreaTop.setFocusable(false);
        jTextAreaTop.setMargin(new java.awt.Insets(0, 0, 2, 0));
        jTextAreaTop.setRequestFocusEnabled(false);
        add(jTextAreaTop);

        jEditorPane1.setEditable(false);
        jEditorPane1.setPreferredSize(new java.awt.Dimension(500, 500));
        jScrollPane1.setViewportView(jEditorPane1);

        add(jScrollPane1);

        jTextAreaBottom.setBackground(getBackground());
        jTextAreaBottom.setColumns(20);
        jTextAreaBottom.setEditable(false);
        jTextAreaBottom.setFont(new java.awt.Font("Dialog", 1, 12));
        jTextAreaBottom.setLineWrap(true);
        jTextAreaBottom.setRows(2);
        jTextAreaBottom.setText(bundle.getString("MSG_LicenseDlgLabelBottom"));
        jTextAreaBottom.setWrapStyleWord(true);
        jTextAreaBottom.setFocusable(false);
        jTextAreaBottom.setRequestFocusEnabled(false);
        add(jTextAreaBottom);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaBottom;
    private javax.swing.JTextArea jTextAreaTop;
    // End of variables declaration//GEN-END:variables
    private URL url;
    private static final ResourceBundle bundle = NbBundle.getBundle(LicensePanel.class);
}
