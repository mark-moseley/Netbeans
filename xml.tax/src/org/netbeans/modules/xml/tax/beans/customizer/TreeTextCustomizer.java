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
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeText;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeData;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeTextCustomizer extends AbstractTreeCustomizer {
    
    /** Serial Version UID */
    private static final long serialVersionUID =6854917766750212771L;
    
    
    //
    // init
    //
    
    /** */
    public TreeTextCustomizer () {
        super ();
        
        initComponents ();
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeText getText () {
        return (TreeText)getTreeObject ();
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
    protected final void updateTextData () {
        try {
            getText ().setData (dataPane.getText ());
        } catch (TreeException exc) {
            updateDataComponent ();
            Util.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateDataComponent () {
        dataPane.setText (getText ().getData ());
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateDataComponent ();
    }
    
    
    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        dataPane.setEditable (editable);
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
        dataPane = new javax.swing.JEditorPane();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(350, 230));
        dataPanel.setLayout(new java.awt.BorderLayout());

        dataPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                dataPaneFocusLost(evt);
            }
        });

        dataPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dataPaneKeyReleased(evt);
            }
        });

        dataScroll.setViewportView(dataPane);

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
    
    private void dataPaneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dataPaneKeyReleased
        // Add your handling code here:
        if ( applyKeyPressed (evt) ) {
            updateTextData ();
        }
    }//GEN-LAST:event_dataPaneKeyReleased
    
    private void dataPaneFocusLost(java.awt.event.FocusEvent event) {//GEN-FIRST:event_dataPaneFocusLost
        // Add your handling code here:
        updateTextData ();
    }//GEN-LAST:event_dataPaneFocusLost
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dataPanel;
    private javax.swing.JEditorPane dataPane;
    private javax.swing.JScrollPane dataScroll;
    // End of variables declaration//GEN-END:variables
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        this.getAccessibleContext ().setAccessibleDescription (Util.getString ("ACSD_TreeTextCustomizer"));
        dataPane.getAccessibleContext ().setAccessibleDescription (Util.getString ("ACSD_dataPanel3"));
        dataPane.getAccessibleContext ().setAccessibleName (Util.getString ("ACSN_dataPanel3"));
    }
}
