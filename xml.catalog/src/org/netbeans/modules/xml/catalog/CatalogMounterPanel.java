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
package org.netbeans.modules.xml.catalog;

import java.awt.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Panel for selecting catalog providers showing customizer for selected one.
 *
 * @author  Petr Kuzel
 */
public class CatalogMounterPanel extends javax.swing.JPanel implements ChangeListener {

    /** Serial Version UID */
    private static final long serialVersionUID =-1208422697106159058L;    

    private CatalogMounterModel model;
    
    /** Creates new form CatalogMounterPanel */
    public CatalogMounterPanel(CatalogMounterModel model) {
        this.model = model;
        initComponents();
        this.catalogLabel.setDisplayedMnemonic(Util.getChar("CatalogMounterPanel.catalogLabel.mne")); // NOI18N
                
        catalogComboBox.setModel(model.getCatalogComboBoxModel());
        updateCatalogPanel();
        
        model.setChangeListener(this);
    }

    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        catalogLabel = new javax.swing.JLabel();
        catalogComboBox = new javax.swing.JComboBox();
        parentPanel = new javax.swing.JPanel();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(4, 4, 4, 4)));
        setPreferredSize(new java.awt.Dimension(380, 100));
        setMinimumSize(new java.awt.Dimension(380, 100));
        catalogLabel.setText(Util.getString("CatalogMounterPanel.catalogLabel.text"));
        catalogLabel.setLabelFor(catalogComboBox);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(catalogLabel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints1.weightx = 1.0;
        add(catalogComboBox, gridBagConstraints1);
        
        parentPanel.setLayout(new java.awt.BorderLayout());
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(parentPanel, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void updateCatalogPanel() {
        Customizer cust = model.getCatalogCustomizer();
        cust.setObject(model.getCatalog());
        parentPanel.removeAll();
        Component catalogPanel = (Component) cust;        
        parentPanel.add(catalogPanel, BorderLayout.CENTER);
    }
    
    public void stateChanged(ChangeEvent e) {
        updateCatalogPanel();
        revalidate();        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel catalogLabel;
    private javax.swing.JComboBox catalogComboBox;
    private javax.swing.JPanel parentPanel;
    // End of variables declaration//GEN-END:variables

}
