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

package org.netbeans.upgrade;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 * @author Jiri Rechtacek
 */
final class AutoUpgradePanel extends JPanel {

    public static void main(String args[]) {
        // display dialog
        DialogDescriptor descriptor = new DialogDescriptor (
            new AutoUpgradePanel("<directory>"), // NOI18N
            bundle.getString("MSG_Confirmation_Title") // NOI18N
        );
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.show();
        dialog.dispose();
    }
    
    String source;

    /** Creates new form UpgradePanel */
    public AutoUpgradePanel (String directory) {
        this.source = directory;
        initComponents();
        initAccessibility(); 
        
    }

    /** Remove a listener to changes of the panel's validity.
     * @param l the listener to remove
     */
    void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    /** Add a listener to changes of the panel's validity.
     * @param l the listener to add
     * @see #isValid
     */
    void addChangeListener(ChangeListener l) {
        if (!changeListeners.contains(l)) {
            changeListeners.add(l);
        }
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_Confirmation")); // NOI18N
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        txtVersions = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setMaximumSize(new java.awt.Dimension(123456, 123456));
        setMinimumSize(new java.awt.Dimension(500, 279));
        setName(bundle.getString("LBL_UpgradePanel_Name"));
        txtVersions.setColumns(50);
        txtVersions.setLineWrap(true);
        txtVersions.setRows(3);
        txtVersions.setText(NbBundle.getMessage (AutoUpgradePanel.class, "MSG_Confirmation", source));
        txtVersions.setWrapStyleWord(true);
        txtVersions.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtVersions.setDoubleBuffered(true);
        txtVersions.setMinimumSize(new java.awt.Dimension(100, 50));
        txtVersions.setEnabled(false);
        txtVersions.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(txtVersions, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea txtVersions;
    // End of variables declaration//GEN-END:variables

    private static final ResourceBundle bundle = NbBundle.getBundle(AutoUpgradePanel.class);
    private ArrayList changeListeners = new ArrayList(1);
    
}
