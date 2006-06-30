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

package examples.texteditor;

import java.awt.FileDialog;
import java.io.*;

/** This class is an entry point of the simple text editor.
 * It creates and shows the main application frame.
 */
public class Ted extends javax.swing.JFrame {

    /** Ted constructor.
     * It initializes all GUI components [menu bar, menu items, editor pane, etc.].
     */
    public Ted() {
        initComponents();
        setSize(500,300);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        textScrollPane = new javax.swing.JScrollPane();
        textBox = new javax.swing.JTextArea();
        tedMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        findMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setTitle("Ted");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        getAccessibleContext().setAccessibleName("Ted Frame");
        getAccessibleContext().setAccessibleDescription("Ted frame.");
        textScrollPane.setViewportView(textBox);
        textBox.getAccessibleContext().setAccessibleName("Text Box");
        textBox.getAccessibleContext().setAccessibleDescription("Text box.");

        getContentPane().add(textScrollPane, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");
        newMenuItem.setMnemonic('n');
        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setText("New");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newMenuItem);
        newMenuItem.getAccessibleContext().setAccessibleName("New Menu Item");
        newMenuItem.getAccessibleContext().setAccessibleDescription("New menu item.");

        fileMenu.add(jSeparator1);

        openMenuItem.setMnemonic('o');
        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setText("Open ...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);
        openMenuItem.getAccessibleContext().setAccessibleName("Open Menu Item");
        openMenuItem.getAccessibleContext().setAccessibleDescription("Open menu item.");

        saveMenuItem.setMnemonic('s');
        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveMenuItem);
        saveMenuItem.getAccessibleContext().setAccessibleName("Save Menu Item");
        saveMenuItem.getAccessibleContext().setAccessibleDescription("Save menu item.");

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);
        saveAsMenuItem.getAccessibleContext().setAccessibleName("Save As Menu Item");
        saveAsMenuItem.getAccessibleContext().setAccessibleDescription("Save As menu item.");

        fileMenu.add(jSeparator2);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);
        exitMenuItem.getAccessibleContext().setAccessibleName("Exit Menu Item");
        exitMenuItem.getAccessibleContext().setAccessibleDescription("Exit menu item.");

        tedMenuBar.add(fileMenu);
        fileMenu.getAccessibleContext().setAccessibleName("File Menu");
        fileMenu.getAccessibleContext().setAccessibleDescription("File menu.");

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");
        findMenuItem.setMnemonic('f');
        findMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        findMenuItem.setText("Find ...");
        findMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(findMenuItem);
        findMenuItem.getAccessibleContext().setAccessibleName("Find Menu Item");
        findMenuItem.getAccessibleContext().setAccessibleDescription("Find menu item.");

        tedMenuBar.add(editMenu);
        editMenu.getAccessibleContext().setAccessibleName("Edit Menu");
        editMenu.getAccessibleContext().setAccessibleDescription("Edit menu.");

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");
        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About ...");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutMenuItem);
        aboutMenuItem.getAccessibleContext().setAccessibleName("About Menu Item");
        aboutMenuItem.getAccessibleContext().setAccessibleDescription("About menu item.");

        tedMenuBar.add(helpMenu);
        helpMenu.getAccessibleContext().setAccessibleName("Help Menu");
        helpMenu.getAccessibleContext().setAccessibleDescription("Help menu.");

        setJMenuBar(tedMenuBar);
        tedMenuBar.getAccessibleContext().setAccessibleName("Ted Menu Bar");
        tedMenuBar.getAccessibleContext().setAccessibleDescription("Ted menu bar.");

    }//GEN-END:initComponents

    /** This method is called when File -> Save menu item is invoked.
     * It saves the current opened file.
     * @param evt ActionEvent instance passed from actionPerformed event.
     */
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        if ("".equals(fileName))
            doSaveAs();
        else
            doSave(fileName);
    }//GEN-LAST:event_saveMenuItemActionPerformed

    /** This method is called when File -> Exit menu item is invoked.
     * It closes the application.
     * @param evt ActionEvent instance passed from actionPerformed event.
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /** This method is called when Edit -> Find menu item is invoked.
     * It creates and shows the Finder frame to allow the user to search in the text.
     * @param evt ActionEvent instance passed from actionPerformed event.
     */
    private void findMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findMenuItemActionPerformed
        new Finder(this, textBox).show();
    }//GEN-LAST:event_findMenuItemActionPerformed

    /** This method is called when Help -> About menu item is invoked.
     * It creates and shows the About dialog.
     * @param evt ActionEvent instance passed from actionPerformed event.
     */
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        new About(this). show();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    /** This method is called when File -> Save as menu item is invoked.
     * It asks for a new file name, then saves the file.
     * @param evt ActionEvent instance passed from actionPerformed event.
     */
    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        doSaveAs();
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    /** This method is called when File -> Open menu item is invoked.
     * It displays a dialog to choose the file to be opened and edited.
     * @param evt ActionEvent instance passed from actionPerformed event.
     */
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        FileDialog fileDialog = new FileDialog(this, "Open...", FileDialog.LOAD);
        fileDialog.show();
        if (fileDialog.getFile() == null)
            return;
        fileName = fileDialog.getDirectory() + File.separator + fileDialog.getFile();

        FileInputStream fis = null;
        String str = null;
        try {
            fis = new FileInputStream(fileName);
            int size = fis.available();
            byte[] bytes = new byte [size];
            fis.read(bytes);
            str = new String(bytes);
        } catch (IOException e) {
        } finally {
            try {
                fis.close();
            } catch (IOException e2) {
            }
        }

        if (str != null)
            textBox.setText(str);
    }//GEN-LAST:event_openMenuItemActionPerformed

    /** This method is called when File -> New menu item is invoked.
     * It clears the editor pane.
     * @param evt ActionEvent instance passed from actionPerformed event.
     */
    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        fileName = "";
        textBox.setText("");
    }//GEN-LAST:event_newMenuItemActionPerformed

    /** This method is called when the application frame is closed.
     * @param evt WindowEvent instance passed from windowClosing event.
     */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    /** Saves the current content of editor pane to the file.
     * @param fileName Name of the file.
     */
    private void doSave(String fileName) {
        FileOutputStream fos = null;
        String str = textBox.getText();
        try {
            fos = new FileOutputStream(fileName);
            fos.write(str.getBytes());
        } catch (IOException e) {
        } finally {
            try {
                fos.close();
            } catch (IOException e2) {
            }
        }
    }

    /** Asks for a file name. then saves the current content of editor pane to the file.
     */
    private void doSaveAs() {
        FileDialog fileDialog = new FileDialog(this, "Save As...", FileDialog.SAVE);
        fileDialog.show();
        if (fileDialog.getFile() == null)
            return;
        fileName = fileDialog.getDirectory() + File.separator + fileDialog.getFile();

        doSave(fileName);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem findMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuBar tedMenuBar;
    private javax.swing.JTextArea textBox;
    private javax.swing.JScrollPane textScrollPane;
    // End of variables declaration//GEN-END:variables

    private String fileName = "";


    /** Starts the application.
     * @param args Application arguments.
     */    
    public static void main(java.lang.String[] args) {
        new Ted().show();
    }

}
