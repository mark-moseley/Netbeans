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
package org.netbeans.modules.mercurial.ui.diff;

import java.io.File;
import java.util.Set;
import java.util.Vector;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.HgModuleConfig;

/**
 *
 * @author  Padraig O'Briain
 */
public class ExportDiffPanel extends javax.swing.JPanel implements ActionListener {

    private File                            repository;
    private RequestProcessor.Task           refreshViewTask;
    private Thread                          refreshViewThread;
    private static final RequestProcessor   rp = new RequestProcessor("MercurialExportDiff", 1);  // NOI18N

    private static final int HG_REVISION_TARGET_LIMIT = 100;

    /** Creates new form ExportDiffPanel */
    public ExportDiffPanel(File repo) {
        repository = repo;
        refreshViewTask = rp.create(new RefreshViewTask());
        initComponents();
        browseButton.addActionListener(this);
        refreshViewTask.schedule(0);
    }

    public String getSelectedRevision() {
        String revStr = (String) revisionsComboBox.getSelectedItem();
        revStr = revStr.substring(0, revStr.indexOf(" "));
        return revStr;
    }

    public String getOutputFileName() {
        return outputFileTextField.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        revisionsLabel = new javax.swing.JLabel();
        revisionsComboBox = new javax.swing.JComboBox();
        fileLabel = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();

        revisionsLabel.setLabelFor(revisionsComboBox);
        revisionsLabel.setText(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.revisionsLabel.text")); // NOI18N

        fileLabel.setLabelFor(outputFileTextField);
        fileLabel.setText(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.fileLabel.text")); // NOI18N

        browseButton.setText(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.browseButtonl.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(fileLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(outputFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 197, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(browseButton))
                    .add(layout.createSequentialGroup()
                        .add(revisionsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(revisionsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 297, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(revisionsLabel)
                    .add(revisionsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 27, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileLabel)
                    .add(outputFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .add(26, 26, 26))
        );
    }// </editor-fold>//GEN-END:initComponents
    

    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        // XXX attach Cancelable hook
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(ExportDiffPanel.class, "MSG_Refreshing_Revisions")); // NOI18N
        try {
            refreshViewThread = Thread.currentThread();
            Thread.interrupted();  // clear interupted status
            ph.start();

            refreshRevisions();
            getDefaultOutputFile();
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ph.finish();
                    refreshViewThread = null;
                }
            });
        }
    }

    private void getDefaultOutputFile() {
        outputFileTextField.setText(HgModuleConfig.getDefault().getExportFilename());
    }

    private void refreshRevisions() {
        java.util.List<String> targetRevsList = HgCommand.getAllRevisions(repository); 

        Set<String>  targetRevsSet = new LinkedHashSet<String>();

        int size;
        if( targetRevsList == null){
            size = 0;
            targetRevsSet.add(NbBundle.getMessage(ExportDiffPanel.class, "MSG_Revision_Default"));
        }else{
            size = targetRevsList.size();
            int i = 0 ;
            while( i < size && i < HG_REVISION_TARGET_LIMIT){
                targetRevsSet.add(targetRevsList.get(i));
                i++;
            }
        }
        ComboBoxModel targetsModel = new DefaultComboBoxModel(new Vector<String>(targetRevsSet));
        revisionsComboBox.setModel(targetsModel);

        if (targetRevsSet.size() > 0 ) {
            revisionsComboBox.setSelectedIndex(0);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == browseButton) {
            onBrowseClick();
        }
    }

    private void onBrowseClick() {
        File oldFile = null;
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(ExportDiffPanel.class, "ACSD_BrowseFolder"), oldFile);   // NO I18N
        fileChooser.setDialogTitle(NbBundle.getMessage(ExportDiffPanel.class, "Browse_title"));                                            // NO I18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        //fileChooser.addChoosableFileFilter(new FileFilter() {
        //    public boolean accept(File f) {
        //        return f.isDirectory();
        //    }
        //    public String getDescription() {
        //        return NbBundle.getMessage(ExportDiffPanel.class, "Folders");// NOI18N
        //    }
        //});
        //fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(this, NbBundle.getMessage(ExportDiffPanel.class, "OK_Button"));                                            // NO I18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            outputFileTextField.setText(f.getAbsolutePath());
        }
    }

    private class RefreshViewTask implements Runnable {
        public void run() {
            setupModels();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel fileLabel;
    final javax.swing.JTextField outputFileTextField = new javax.swing.JTextField();
    private javax.swing.JComboBox revisionsComboBox;
    private javax.swing.JLabel revisionsLabel;
    // End of variables declaration//GEN-END:variables
    
}
