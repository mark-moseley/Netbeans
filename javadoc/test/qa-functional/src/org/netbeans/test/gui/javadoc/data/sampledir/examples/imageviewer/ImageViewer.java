/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package examples.imageviewer;

public class ImageViewer extends javax.swing.JFrame {

    /** Creates new form ImageViewer */
    public ImageViewer() {
        initComponents ();
        pack ();
        setBounds( 100, 100, 400, 400 );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        desktop = new javax.swing.JDesktopPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();

        setTitle("Image Viewer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        getContentPane().add(desktop, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);
        fileMenu.add(jSeparator1);
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);
        jMenuBar1.add(fileMenu);
        setJMenuBar(jMenuBar1);

    }//GEN-END:initComponents

    private void exitMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        // Add your handling code here:
        System.exit( 0 );
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void openMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        // Add your handling code here:
        java.awt.FileDialog fd = new java.awt.FileDialog (this);
        fd.show ();
        if (fd.getFile () == null) return;
        ImageFrame ifr = new ImageFrame (fd.getDirectory () + fd.getFile ());
        desktop.add (ifr, javax.swing.JLayeredPane.DEFAULT_LAYER);

        ifr.setVisible( true );
        ifr.setSize (200, 200);
        ifr.setLocation (0, 0);
    }//GEN-LAST:event_openMenuItemActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit (0);
    }//GEN-LAST:event_exitForm

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        new ImageViewer ().show ();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem openMenuItem;
    // End of variables declaration//GEN-END:variables

}
