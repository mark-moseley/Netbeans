/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.impl;

import java.beans.*;

/**
 * Customizer for Netbeans IDE catalog is read only because the catalog
 * can be modified just by modules using OpenIDE API.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class SystemCatalogCustomizer extends javax.swing.JPanel implements Customizer {

    /** Serial Version UID */
    private static final long serialVersionUID = -7117054881250295623L;    

    /** Creates new form CatalogCustomizer */
    public SystemCatalogCustomizer() {
        initComponents ();
        this.getAccessibleContext().setAccessibleDescription(Util.getString("ACSD_SystemCatalogCustomizer"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jTextArea1 = new javax.swing.JTextArea(){
            public boolean isFocusTraversable(){
                return false;
            }
        };

        setLayout(new java.awt.GridBagLayout());

        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setLineWrap(true);
        jTextArea1.setEditable(false);
        jTextArea1.setForeground(new java.awt.Color(102, 102, 153));
        jTextArea1.setFont(javax.swing.UIManager.getFont ("Label.font"));
        jTextArea1.setText(Util.getString ("SystemCatalogCustomizer.readOnly.text"));
        jTextArea1.setDisabledTextColor(javax.swing.UIManager.getColor ("Label.foreground"));
        jTextArea1.setPreferredSize(new java.awt.Dimension(300, 50));
        jTextArea1.setBorder(null);
        jTextArea1.setEnabled(false);
        jTextArea1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jTextArea1, gridBagConstraints);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables


    public void setObject(final java.lang.Object peer) {
    }
    
    public void addPropertyChangeListener(final java.beans.PropertyChangeListener p1) {
    }
    
    public void removePropertyChangeListener(final java.beans.PropertyChangeListener p1) {
    }
}
