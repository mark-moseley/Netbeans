/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mercurial.ui.rollback;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author  Padraig O'Briain
 */
public class BackoutPanel extends javax.swing.JPanel {

    private File                            repository;
    private RequestProcessor.Task           refreshViewTask;
    private static final RequestProcessor   rp = new RequestProcessor("MercurialBackout", 1);  // NOI18N
    private Thread                          refreshViewThread;
    private RepositoryRevision              repoRev;
    private HgLogMessage[] messages;
    private int fetchRevisionLimit = Mercurial.HG_NUMBER_TO_FETCH_DEFAULT;
    private boolean bGettingRevisions = false;
    File [] roots;

    /** Creates new form ReverModificationsPanel */
     public BackoutPanel(File repo, RepositoryRevision repoRev, File [] roots) {
        this.repoRev = repoRev;
        this.roots = roots;
        repository = repo;
        refreshViewTask = rp.create(new RefreshViewTask());
        initComponents();
        revisionsComboBox.setMaximumRowCount(Mercurial.HG_MAX_REVISION_COMBO_SIZE);
        commitMsgField.setText(
                NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.commitMsgField.text") +  // NOI18N
                BackoutAction.HG_BACKOUT_REVISION); // NOI18N
        refreshViewTask.schedule(0);
        if(repoRev != null){
            org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, 
                    org.openide.util.NbBundle.getMessage(BackoutPanel.class, "CTL_ChoosenRevision")); // NOI18N
        }
    }

    public String getSelectedRevision() {
        String revStr = (String) revisionsComboBox.getSelectedItem();
        if(revStr != null){
            if (revStr.equals(NbBundle.getMessage(Backout.class, "MSG_Revision_Default")) || // NOI18N
                revStr.equals(NbBundle.getMessage(Backout.class, "MSG_Fetching_Revisions"))) { // NOI18N
                revStr = null;
            } else {
                revStr = revStr.substring(0, revStr.indexOf(" ")); // NOI18N
            }
        }
        return revStr;
    }

    public String getCommitMessage() {
        return commitMsgField.getText();
    }

    public boolean isMergeRequested() {
        return doMergeChxBox.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        doMergeChxBox = new javax.swing.JCheckBox();
        revisionsLabel = new javax.swing.JLabel();
        revisionsComboBox = new javax.swing.JComboBox();
        infoLabel = new javax.swing.JLabel();
        infoLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        commitMsgField = new javax.swing.JTextField();
        commitLabel = new javax.swing.JLabel();
        changesetPanel1 = new org.netbeans.modules.mercurial.ui.repository.ChangesetPanel();

        doMergeChxBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(doMergeChxBox, org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.doMergeChxBox.text")); // NOI18N
        doMergeChxBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doMergeChxBoxActionPerformed(evt);
            }
        });

        revisionsLabel.setLabelFor(revisionsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.revisionsLabel.text")); // NOI18N

        revisionsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revisionsComboBoxActionPerformed(evt);
            }
        });

        infoLabel.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.infoLabel.text")); // NOI18N

        infoLabel2.setForeground(new java.awt.Color(153, 153, 153));
        org.openide.awt.Mnemonics.setLocalizedText(infoLabel2, org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.infoLabel2.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BackoutPanel.class, "StripPanel.jPanel1.border.title"))); // NOI18N

        commitMsgField.setText(org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.commitMsgField.text")); // NOI18N

        commitLabel.setLabelFor(commitMsgField);
        org.openide.awt.Mnemonics.setLocalizedText(commitLabel, org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.commitLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(commitLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(commitMsgField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(commitLabel)
                    .add(commitMsgField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        commitMsgField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BackoutPanel.class, "ACSD_commitMsgField")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, infoLabel2)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, infoLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(40, 40, 40)
                        .add(revisionsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(revisionsComboBox, 0, 223, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(changesetPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(infoLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(infoLabel2)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(revisionsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(revisionsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(changesetPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        revisionsComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BackoutPanel.class, "ACSD_revisionsComboBoxBackout")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void doMergeChxBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doMergeChxBoxActionPerformed
        if( doMergeChxBox.isSelected()){
            commitMsgField.setEnabled(true);
            commitMsgField.setEditable(true);
            commitLabel.setEnabled(true);
        }else{
            commitMsgField.setEnabled(false);
            commitMsgField.setEditable(false);
            commitLabel.setEnabled(false);
        }
    }//GEN-LAST:event_doMergeChxBoxActionPerformed

private void revisionsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revisionsComboBoxActionPerformed
    int index = revisionsComboBox.getSelectedIndex();
    if(getMore((String) revisionsComboBox.getSelectedItem())) return;
    
    if(messages != null && index >= 0 && index < messages.length ){
        changesetPanel1.setInfo(messages[index]);
    }
}//GEN-LAST:event_revisionsComboBoxActionPerformed
    
    private boolean getMore(String revStr) {
        if (bGettingRevisions) return false;
        boolean bGetMore = false;
        int limit = -1;

        if (revStr != null && revStr.equals(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_20_Revisions"))) {
            bGetMore = true;
            limit = Mercurial.HG_FETCH_20_REVISIONS;
        } else if (revStr != null && revStr.equals(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_50_Revisions"))) {
            bGetMore = true;
            limit = Mercurial.HG_FETCH_50_REVISIONS;
        } else if (revStr != null && revStr.equals(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_All_Revisions"))) {
            bGetMore = true;
            limit = Mercurial.HG_FETCH_ALL_REVISIONS;
        }
        if (bGetMore && !bGettingRevisions) {
            fetchRevisionLimit = limit;
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
            HgProgressSupport hgProgressSupport = new HgProgressSupport() {
                public void perform() {
                    changesetPanel1.clearInfo();
                    refreshRevisions();
                }
            };
            hgProgressSupport.start(rp, repository.getAbsolutePath(),
                    org.openide.util.NbBundle.getMessage(Mercurial.class, "MSG_Fetching_Revisions")); // NOI18N
        }
        return bGetMore;
    }

    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        // XXX attach Cancelable hook
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(Backout.class, "MSG_Refreshing_Backout_Versions")); // NOI18N
        try {
            Set<String> initialRevsSet = new LinkedHashSet<String>();
            ComboBoxModel targetsModel;
            if (repoRev != null) {
                initialRevsSet.add(repoRev.getLog().getRevision() + " (" + repoRev.getLog().getCSetShortID() + ")"); // NOI18N
                targetsModel = new DefaultComboBoxModel(new Vector<String>(initialRevsSet));
                changesetPanel1.setInfo(repoRev.getLog());
                revisionsComboBox.setModel(targetsModel);
                revisionsComboBox.setEditable(false);
                refreshViewThread = Thread.currentThread();
                Thread.interrupted();  // clear interupted status
                ph.start();
            } else {
                initialRevsSet.add(NbBundle.getMessage(Backout.class, "MSG_Fetching_Revisions")); // NOI18N
                targetsModel = new DefaultComboBoxModel(new Vector<String>(initialRevsSet));
                revisionsComboBox.setModel(targetsModel);
                refreshViewThread = Thread.currentThread();
                Thread.interrupted();  // clear interupted status
                ph.start();

                refreshRevisions();
            }
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ph.finish();
                    refreshViewThread = null;
                }
            });
        }
    }

    private void refreshRevisions() {
        bGettingRevisions = true;
        OutputLogger logger = OutputLogger.getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
        Set<File> setRoots = new HashSet<File>(Arrays.asList(roots));        
        messages = HgCommand.getLogMessagesNoFileInfo(repository.getAbsolutePath(), setRoots, fetchRevisionLimit, logger);

        Set<String>  targetRevsSet = new LinkedHashSet<String>();

        int size;
        if( messages == null){
            size = 0;
            targetRevsSet.add(NbBundle.getMessage(Backout.class, "MSG_Revision_Default")); // NOI18N
        }else{
            size = messages.length;
            int i = 0 ;
            while(i < size){
                targetRevsSet.add(messages[i].getRevision() + " (" + messages[i].getCSetShortID() + ")"); // NOI18N
                i++;
            }
        }
        if(targetRevsSet.size() > 0){
            targetRevsSet.add(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_20_Revisions"));
            targetRevsSet.add(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_50_Revisions"));
            targetRevsSet.add(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_All_Revisions"));
        }
        ComboBoxModel targetsModel = new DefaultComboBoxModel(new Vector<String>(targetRevsSet));
        revisionsComboBox.setModel(targetsModel);

        if (targetRevsSet.size() > 0 ) {
            revisionsComboBox.setSelectedIndex(0);
        }
        bGettingRevisions = false;
    }

    private class RefreshViewTask implements Runnable {
        public void run() {
            setupModels();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mercurial.ui.repository.ChangesetPanel changesetPanel1;
    private javax.swing.JLabel commitLabel;
    private javax.swing.JTextField commitMsgField;
    private javax.swing.JCheckBox doMergeChxBox;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel infoLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox revisionsComboBox;
    private javax.swing.JLabel revisionsLabel;
    // End of variables declaration//GEN-END:variables
    
}
