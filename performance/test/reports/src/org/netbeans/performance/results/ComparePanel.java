/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.results;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author  radim
 */
public class ComparePanel extends javax.swing.JPanel {

    private static File wd;

    DefaultListModel refFilesModel = new DefaultListModel();
    DefaultListModel newFilesModel = new DefaultListModel();

    /** Creates new form ComparePanel */
    public ComparePanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        txtRefFile = new javax.swing.JTextField();
        btnRefFileBrowse = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstRefFiles = new javax.swing.JList();
        btnRefFileAdd = new javax.swing.JButton();
        btnRefFileRemove = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtNewFile = new javax.swing.JTextField();
        btnNewFileBrowse = new javax.swing.JButton();
        btnNewFileAdd = new javax.swing.JButton();
        btnNewFileRemove = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstNewFiles = new javax.swing.JList();
        btnCompare = new javax.swing.JButton();

        jLabel1.setText("Reference results:");

        btnRefFileBrowse.setText("Browse");
        btnRefFileBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefFileBrowseActionPerformed(evt);
            }
        });

        lstRefFiles.setModel(refFilesModel);
        jScrollPane1.setViewportView(lstRefFiles);

        btnRefFileAdd.setText("Add");
        btnRefFileAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefFileAddActionPerformed(evt);
            }
        });

        btnRefFileRemove.setText("Remove");
        btnRefFileRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefFileRemoveActionPerformed(evt);
            }
        });

        jLabel2.setText("Compared results:");

        btnNewFileBrowse.setText("Browse");
        btnNewFileBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewFileBrowseActionPerformed(evt);
            }
        });

        btnNewFileAdd.setText("Add");
        btnNewFileAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewFileAddActionPerformed(evt);
            }
        });

        btnNewFileRemove.setText("Remove");
        btnNewFileRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewFileRemoveActionPerformed(evt);
            }
        });

        lstNewFiles.setModel(newFilesModel);
        jScrollPane2.setViewportView(lstNewFiles);

        btnCompare.setText("Compare");
        btnCompare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompareActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                            .add(txtRefFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txtNewFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, btnRefFileAdd, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, btnRefFileBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(layout.createSequentialGroup()
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(btnRefFileRemove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                                .add(layout.createSequentialGroup()
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(btnNewFileAdd)
                                        .add(btnNewFileBrowse))))
                            .add(btnNewFileRemove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, btnCompare))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {btnNewFileAdd, btnNewFileBrowse, btnNewFileRemove, btnRefFileAdd, btnRefFileBrowse, btnRefFileRemove}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnRefFileBrowse)
                    .add(txtRefFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(btnRefFileAdd)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRefFileRemove))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtNewFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnNewFileBrowse))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(btnNewFileAdd)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnNewFileRemove))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 7, Short.MAX_VALUE)
                .add(btnCompare)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCompareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompareActionPerformed

        Object [] files = refFilesModel.toArray();
        Set<File> refFiles = new TreeSet<File>();
        for (Object s: files) {
            refFiles.add(new File((String)s));
        }
        files = newFilesModel.toArray();
        Set<File> newFiles = new TreeSet<File>();
        for (Object s: files) {
            newFiles.add(new File((String)s));
        }
        try {
            ReportUtils.doCompare(refFiles, newFiles);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnCompareActionPerformed

    private void btnNewFileRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewFileRemoveActionPerformed
        int idx = lstNewFiles.getSelectedIndex();
        if (idx != -1) {
            newFilesModel.remove(idx);
        }
    }//GEN-LAST:event_btnNewFileRemoveActionPerformed

    private void btnNewFileAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewFileAddActionPerformed
        String f = txtNewFile.getText();
        if (f != null && !"".equals(f)) {
            newFilesModel.add(0, f);
        }
    }//GEN-LAST:event_btnNewFileAddActionPerformed

    private void btnNewFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewFileBrowseActionPerformed
        JFileChooser fc = new JFileChooser(wd);
        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith(".xml");
            }

            public String getDescription() {
                return "XML files";
            }

        };
        fc.setFileFilter(filter);
        int result = fc.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            txtNewFile.setText (fc.getSelectedFile().getPath());
            wd = fc.getCurrentDirectory();
        }
    }//GEN-LAST:event_btnNewFileBrowseActionPerformed

    private void btnRefFileRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefFileRemoveActionPerformed

        int idx = lstRefFiles.getSelectedIndex();
        if (idx != -1) {
            refFilesModel.remove(idx);
        }
    }//GEN-LAST:event_btnRefFileRemoveActionPerformed

    private void btnRefFileAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefFileAddActionPerformed

        String f = txtRefFile.getText();
        if (f != null && !"".equals(f)) {
            refFilesModel.add(0, f);
        }
    }//GEN-LAST:event_btnRefFileAddActionPerformed

    private void btnRefFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefFileBrowseActionPerformed

        JFileChooser fc = new JFileChooser(wd);
        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith(".xml");
            }

            public String getDescription() {
                return "XML files";
            }

        };
        fc.setFileFilter(filter);
        int result = fc.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            txtRefFile.setText (fc.getSelectedFile().getPath());
            wd = fc.getCurrentDirectory();
        }
    }//GEN-LAST:event_btnRefFileBrowseActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCompare;
    private javax.swing.JButton btnNewFileAdd;
    private javax.swing.JButton btnNewFileBrowse;
    private javax.swing.JButton btnNewFileRemove;
    private javax.swing.JButton btnRefFileAdd;
    private javax.swing.JButton btnRefFileBrowse;
    private javax.swing.JButton btnRefFileRemove;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lstNewFiles;
    private javax.swing.JList lstRefFiles;
    private javax.swing.JTextField txtNewFile;
    private javax.swing.JTextField txtRefFile;
    // End of variables declaration//GEN-END:variables
    
}
