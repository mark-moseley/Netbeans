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
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Kaspar
 */
public final class ConvertPanel extends javax.swing.JPanel implements ActionListener, Runnable, DocumentListener {
    
    private DialogDescriptor descriptor = new DialogDescriptor (this, NbBundle.getMessage (ConvertPanel.class, "TITLE_ConvertPanel")); // NOI18N
    private JButton startButton = new JButton (NbBundle.getMessage (ConvertPanel.class, "DISP_Start")); // NOI18N
    private JButton finishButton = new JButton (NbBundle.getMessage (ConvertPanel.class, "DISP_Close")); // NOI18N
    private FileObject inputPrimaryFile;
    private FileObject inputSecondaryFile;
    private FileObject outputDirectory;

    /** Creates new form ConvertPanel */
    public ConvertPanel() {
        initComponents();
        ImageIcon warningMessage = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/vmd/midp/resources/warning.gif"));
        finishIcon.setIcon (warningMessage); // NOI18N
        message.setIcon (warningMessage); // NOI18N
        startButton.setDefaultCapable(true);
        startButton.addActionListener(this);
        finishButton.setDefaultCapable(true);
        descriptor.setClosingOptions (new Object[] { finishButton, DialogDescriptor.CANCEL_OPTION });
    }

    public DialogDescriptor getDialogDescriptor () {
        return descriptor;
    }
    
    public void switchToShown (FileObject inputPrimaryFile, FileObject inputSecondaryFile, FileObject outputDirectory) {
        this.inputPrimaryFile = inputPrimaryFile;
        this.inputSecondaryFile = inputSecondaryFile;
        this.outputDirectory = outputDirectory;
        this.inputFileName.setText (inputPrimaryFile.getName ());
        progress.setIndeterminate(false);
        finishIcon.setVisible (false);
        finishMessage.setText(NbBundle.getMessage (ConvertPanel.class, "MSG_ShownMessage")); // NOI18N
        startButton.setEnabled(true);
        descriptor.setOptions(new Object[] { startButton, DialogDescriptor.CANCEL_OPTION });

        outputFileName.getDocument ().removeDocumentListener (this);
        outputFileName.getDocument ().addDocumentListener (this);
        outputFileName.setText ("Converted" + inputPrimaryFile.getName ()); // NOI18N
        outputFileName.setEditable(true);
        outputFileName.selectAll ();
        outputFileName.requestFocus ();
    }
    
    public void switchToStarted () {
        outputFileName.getDocument ().removeDocumentListener (this);
        outputFileName.setEditable(false);
        
        progress.setIndeterminate(true);
        finishMessage.setText(NbBundle.getMessage (ConvertPanel.class, "MSG_StartMessage")); // NOI18N
        startButton.setEnabled(false);
        descriptor.setOptions(new Object[0]);
    }
    
    public void switchToFinished () {
        progress.setIndeterminate(false);
        finishIcon.setVisible (true);
        finishMessage.setText(NbBundle.getMessage (ConvertPanel.class, "MSG_FinishMessage")); // NOI18N
        descriptor.setOptions(new Object[] { finishButton });
    }

    public void insertUpdate (DocumentEvent e) {
        checkErrors ();
    }

    public void removeUpdate (DocumentEvent e) {
        checkErrors ();
    }

    public void changedUpdate (DocumentEvent e) {
        checkErrors ();
    }

    private void checkErrors () {
        boolean exists = outputDirectory.getFileObject (outputFileName.getText (), "java") != null // NOI18N
            || outputDirectory.getFileObject (outputFileName.getText (), "vmd") != null; // NOI18N
        message.setVisible(exists);
        startButton.setEnabled (! exists);
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
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        progress = new javax.swing.JProgressBar();
        finishMessage = new javax.swing.JLabel();
        finishIcon = new javax.swing.JLabel();
        message = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(500, 400));

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/converter/wizard/Bundle").getString("ConvertPanel.jLabel1.mnemonic").charAt(0));
        jLabel2.setLabelFor(inputFileName);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel2.text")); // NOI18N

        inputFileName.setEditable(false);
        inputFileName.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.inputFileName.text")); // NOI18N

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/converter/wizard/Bundle").getString("ConvertPanel.jLabel2.mnemonic").charAt(0));
        jLabel1.setLabelFor(outputFileName);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel1.text")); // NOI18N

        outputFileName.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.outputFileName.text")); // NOI18N

        progress.setMaximum(0);

        finishMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        finishIcon.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.finishIcon.text")); // NOI18N

        message.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.message.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, progress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(inputFileName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, outputFileName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, finishIcon)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, finishMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                            .add(message, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE))))
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(finishIcon)
                    .add(finishMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(message)
                .addContainerGap())
        );

        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel2.AccessibleContext.accessibleDescription")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ACC_NAME_ConvertPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ACC_DESC_ConvertPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel finishIcon;
    private javax.swing.JLabel finishMessage;
    private javax.swing.JTextField inputFileName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel message;
    private javax.swing.JTextField outputFileName;
    private javax.swing.JProgressBar progress;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed (ActionEvent e) {
        switchToStarted ();
        RequestProcessor.getDefault ().post (this);
    }

    public void run () {
        try {
            Converter.convert (inputPrimaryFile, inputSecondaryFile, outputFileName.getText ());
        } catch (Exception e) {
            Exceptions.printStackTrace (e);
        }
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                switchToFinished ();
            }
        });
    }

}
