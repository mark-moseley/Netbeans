/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package examples.texteditor;

public class About extends javax.swing.JDialog {

    /** About constructor.
     * It creates modal dialog and displays it.
     */
    public About(java.awt.Frame parent) {
        super (parent, true);
        initComponents ();
        pack ();
        setLocationRelativeTo(parent);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jTextField1 = new javax.swing.JTextField();

        setTitle("About");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        getAccessibleContext().setAccessibleName("About Dialog");
        getAccessibleContext().setAccessibleDescription("About dialog.");
        jTextField1.setEditable(false);
        jTextField1.setText("Ted the Text Editor.");
        getContentPane().add(jTextField1, java.awt.BorderLayout.CENTER);
        jTextField1.getAccessibleContext().setAccessibleName("About Text");
        jTextField1.getAccessibleContext().setAccessibleDescription("About text.");

    }//GEN-END:initComponents


    /** This method is called when the dialog is closed.
     * @param evt WindowEvent instance passed from windowClosing event.
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible (false);
        dispose ();
    }//GEN-LAST:event_closeDialog


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables



}
