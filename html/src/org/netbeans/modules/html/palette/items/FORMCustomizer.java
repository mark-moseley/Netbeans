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

package org.netbeans.modules.html.palette.items;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.html.palette.HTMLPaletteUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.html.palette.BrowseFolders;




/**
 *
 * @author  Libor Kotouc
 */
public class FORMCustomizer extends javax.swing.JPanel {
    
    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;

    FORM form;
    JTextComponent target;
            
    public FORMCustomizer(FORM form, JTextComponent target) {
        this.form = form;
        this.target = target;
        
        initComponents();
    }
    
    public boolean showDialog() {
        
        dialogOK = false;
        
        descriptor = new DialogDescriptor
                (this, "Insert Form", true,
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
		     }
		 } 
                );
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void evaluateInput() {
        
        String action = jTextField1.getText();
        form.setAction(action);

        if (jRadioButton1.isSelected())
            form.setMethod(FORM.METHOD_GET);
        if (jRadioButton2.isSelected())
            form.setMethod(FORM.METHOD_POST);

        if (jRadioButton3.isSelected())
            form.setEnc(FORM.ENC_URLENC);
        if (jRadioButton4.isSelected())
            form.setEnc(FORM.ENC_MULTI);
        
        String name = jTextField4.getText();
        form.setName(name);
        
    }
    
    private void setEncoding() {
        
        if (jRadioButton1.isSelected()) { // GET method
            jRadioButton3.setSelected(true);
            jRadioButton3.setEnabled(false);
            jRadioButton4.setEnabled(false);
        }
        else if (jRadioButton2.isSelected()) { // POST method
            jRadioButton3.setEnabled(true);
            jRadioButton4.setEnabled(true);
            jRadioButton3.setSelected(true);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser1 = new javax.swing.JFileChooser();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();

        jFileChooser1.setCurrentDirectory(null);

        setLayout(new java.awt.GridBagLayout());

        jLabel4.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 0);
        add(jLabel4, gridBagConstraints);

        jTextField1.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextField1, gridBagConstraints);

        jButton1.setFont(new java.awt.Font("Dialog", 0, 12));
        jButton1.setText("Browse...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jButton1, gridBagConstraints);

        jLabel1.setText("Action:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Method:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Encoding:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel3, gridBagConstraints);

        jTextField4.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 0);
        add(jTextField4, gridBagConstraints);

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("GET");
        jRadioButton1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton1ItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jRadioButton1, gridBagConstraints);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("POST");
        jRadioButton2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton2ItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jRadioButton2, gridBagConstraints);

        buttonGroup2.add(jRadioButton3);
        jRadioButton3.setSelected(true);
        jRadioButton3.setText("application/x-www-form-urlencoded");
        jRadioButton3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jRadioButton3.setEnabled(false);
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jRadioButton3, gridBagConstraints);

        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setText("multipart/form-data");
        jRadioButton4.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jRadioButton4.setEnabled(false);
        jRadioButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jRadioButton4, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jRadioButton2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton2ItemStateChanged
        setEncoding();
    }//GEN-LAST:event_jRadioButton2ItemStateChanged

    private void jRadioButton1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton1ItemStateChanged
        setEncoding();
    }//GEN-LAST:event_jRadioButton1ItemStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        Document targetDoc = target.getDocument();
        FileObject targetDocFO = NbEditorUtilities.getFileObject(targetDoc);
        SourceGroup[] sg = HTMLPaletteUtilities.getSourceGroups(targetDocFO);
        
        File file = null;
        if (sg.length > 0) {
            FileObject fo = BrowseFolders.showDialog(sg);
            if (fo != null)
                file = FileUtil.toFile(fo);
        }
        else {
            jFileChooser1.setCurrentDirectory(FileUtil.toFile(targetDocFO.getParent()));
            int returnVal = jFileChooser1.showOpenDialog(this);

            if (returnVal == jFileChooser1.APPROVE_OPTION)
                file = jFileChooser1.getSelectedFile();
        }
        
        if (file != null) {
            String path = file.getAbsolutePath();
            FileObject actionFO = FileUtil.toFileObject(file);
            try {
                String relPathToAction = HTMLPaletteUtilities.getRelativePath(targetDocFO, actionFO);
                if (relPathToAction.length() > 0)
                    path = relPathToAction;
            }
            catch (Exception e) {
                //eventual exceptions imply the absolute path to be used
            }
            
            jTextField1.setText(path);
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
    
}
