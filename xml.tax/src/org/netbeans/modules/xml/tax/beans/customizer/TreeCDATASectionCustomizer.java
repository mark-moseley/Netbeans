/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeCDATASection;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeData;

import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeCDATASectionCustomizer extends AbstractTreeCustomizer {
    
    /** Serial Version UID */
    private static final long serialVersionUID =-7665482206368732519L;
    
    
    //
    // init
    //
    
    /** */
    public TreeCDATASectionCustomizer () {
        super ();
        
        initComponents ();
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeCDATASection getCDATASection () {
        return (TreeCDATASection)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeData.PROP_DATA)) {
            updateDataComponent ();
        }
    }
    
    /**
     */
    protected final void updateCDATASectionData () {
        try {
            getCDATASection ().setData (dataTextArea.getText ());
        } catch (TreeException exc) {
            updateDataComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateDataComponent () {
        dataTextArea.setText (getCDATASection ().getData ());
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateDataComponent ();
    }
    
    
    /**
     */
    protected final void updateReadOnlyStatus (boolean editable) {
        dataTextArea.setEditable (editable);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        dataPanel = new javax.swing.JPanel();
        dataScroll = new javax.swing.JScrollPane();
        dataTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(350, 230));
        dataPanel.setLayout(new java.awt.BorderLayout());

        dataTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dataTextAreaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                dataTextAreaFocusLost(evt);
            }
        });

        dataTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dataTextAreaKeyPressed(evt);
            }
        });

        dataScroll.setViewportView(dataTextArea);

        dataPanel.add(dataScroll, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(dataPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void dataTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dataTextAreaFocusGained
        if ("new".equals(getClientProperty("xml-edit-mode"))) {  // NOI18N
            dataTextArea.selectAll();
        }
    }//GEN-LAST:event_dataTextAreaFocusGained

    private void dataTextAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dataTextAreaKeyPressed
        // Add your handling code here:
        if ( applyKeyPressed (evt) ) {
            updateCDATASectionData ();
        }
    }//GEN-LAST:event_dataTextAreaKeyPressed

    private void dataTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dataTextAreaFocusLost
        // Add your handling code here:
        updateCDATASectionData ();
    }//GEN-LAST:event_dataTextAreaFocusLost
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea dataTextArea;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JScrollPane dataScroll;
    // End of variables declaration//GEN-END:variables
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeCDATASectionCustomizer"));
        dataTextArea.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_dataPanel2"));
        dataTextArea.getAccessibleContext ().setAccessibleName (Util.THIS.getString ("ACSN_dataPanel2"));
    }
}
