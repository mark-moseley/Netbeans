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
 *
 */
package org.netbeans.modules.vmd.midp.converter.wizard;

import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Kaspar
 */
public final class ConvertPanel extends javax.swing.JPanel implements ActionListener, Runnable {
    
    private DialogDescriptor descriptor = new DialogDescriptor (this, "Convert old Visual Mobile Designer file");
    private JButton startButton = new JButton ("Start");
    private JButton finishButton = new JButton ("Close");
    private FileObject inputPrimaryFile;
    private FileObject inputSecondaryFile;

    /** Creates new form ConvertPanel */
    public ConvertPanel() {
        initComponents();
        startButton.setDefaultCapable(true);
        startButton.addActionListener(this);
        finishButton.setDefaultCapable(true);
        descriptor.setClosingOptions (new Object[] { finishButton, DialogDescriptor.CANCEL_OPTION });
    }

    public DialogDescriptor getDialogDescriptor () {
        return descriptor;
    }
    
    public void switchToShown (FileObject inputPrimaryFile, FileObject inputSecondaryFile) {
        this.inputPrimaryFile = inputPrimaryFile;
        this.inputSecondaryFile = inputSecondaryFile;
        this.inputFileName.setText (inputPrimaryFile.getName ());
        outputFileName.setText ("Converted" + inputPrimaryFile.getName ());
        outputFileName.setEditable(true);
        progress.setIndeterminate(false);
        finishMessage.setVisible(false);
        startButton.setEnabled(true);
        descriptor.setOptions(new Object[] { startButton, DialogDescriptor.CANCEL_OPTION });
        outputFileName.requestFocusInWindow ();
    }
    
    public void switchToStarted () {
        outputFileName.setEditable(false);
        progress.setIndeterminate(true);
        startButton.setEnabled(false);
    }
    
    public void switchToFinished () {
        progress.setIndeterminate(false);
        finishMessage.setVisible(true);
        descriptor.setOptions(new Object[] { finishButton });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        inputFileName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        outputFileName = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        progress = new javax.swing.JProgressBar();
        finishMessage = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(400, 300));

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel2.text")); // NOI18N

        inputFileName.setEditable(false);
        inputFileName.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.inputFileName.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel1.text")); // NOI18N

        outputFileName.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.outputFileName.text")); // NOI18N

        progress.setMaximum(0);

        finishMessage.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.finishMessage.text")); // NOI18N
        finishMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, finishMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inputFileName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 237, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 127, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(outputFileName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, progress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(inputFileName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(outputFileName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(progress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(finishMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel finishMessage;
    private javax.swing.JTextField inputFileName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField outputFileName;
    private javax.swing.JProgressBar progress;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed (ActionEvent e) {
        switchToStarted ();
        RequestProcessor.getDefault ().post (this);
    }

    public void run () {
        Converter.convert (inputPrimaryFile, inputSecondaryFile, outputFileName.getText ());
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                switchToFinished ();
            }
        });
    }

}
