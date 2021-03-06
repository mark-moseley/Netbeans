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
package org.netbeans.modules.mercurial.ui.merge;

import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.ui.update.*;
import java.io.File;
import java.util.Set;
import java.util.Vector;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author  Padraig O'Briain
 */
public class MergeRevisionsPanel extends javax.swing.JPanel {

    private File                            repository;
    private RequestProcessor.Task           refreshViewTask;
    private Thread                          refreshViewThread;
    private static final RequestProcessor   rp = new RequestProcessor("MercurialMerge", 1);  // NOI18N

    private static final int HG_HEAD_TARGET_LIMIT = 100;

    /** Creates new form ReverModificationsPanel */
     public MergeRevisionsPanel(File repo) {
        repository = repo;
        refreshViewTask = rp.create(new RefreshViewTask());
        initComponents();
        refreshViewTask.schedule(0);
    }

    public String getSelectedRevision() {
        String revStr = (String) revisionsComboBox.getSelectedItem();
        if( revStr != null){
            int end = revStr.indexOf(" ("); // NOI18N
            if( end > 0) 
                revStr = revStr.substring(0, end);
            else
                revStr = null;
        }
        
        return revStr;
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

        revisionsLabel.setText(org.openide.util.NbBundle.getMessage(MergeRevisionsPanel.class, "MSG_MERGE_CHOOSE_REVISION")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(42, 42, 42)
                .add(revisionsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 51, Short.MAX_VALUE)
                .add(revisionsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(revisionsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(revisionsLabel))
                .addContainerGap(42, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    

    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        // XXX attach Cancelable hook
        final ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(MergeRevisionsPanel.class, "MSG_MERGE_REFRESHING_HEAD_VERSIONS")); // NOI18N
        try {
            refreshViewThread = Thread.currentThread();
            Thread.interrupted();  // clear interupted status
            ph.start();

            refreshRevisions();
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
        try {
            java.util.List<java.lang.String> targetRevsList = HgCommand.getHeadChangeSetIds(repository);

            java.util.Set<java.lang.String> targetRevsSet = new java.util.LinkedHashSet<java.lang.String>();

            int size;
            if (targetRevsList == null) {
                size = 0;
                targetRevsSet.add(NbBundle.getMessage(MergeRevisionsPanel.class, "MSG_MERGE_HEAD_REVISION_DEFAULT"));   // NOI18N          
            } else {
                size = targetRevsList.size();
                int i = 0;
                while (i < size && i < HG_HEAD_TARGET_LIMIT) {
                    targetRevsSet.add(targetRevsList.get(i));
                    i++;
                }
            }
            javax.swing.ComboBoxModel targetsModel = new javax.swing.DefaultComboBoxModel(new java.util.Vector<java.lang.String>(targetRevsSet));
            revisionsComboBox.setModel(targetsModel);

            if (targetRevsSet.size() > 0) {
                revisionsComboBox.setSelectedIndex(0);
            }
        } catch (HgException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private class RefreshViewTask implements Runnable {
        public void run() {
            setupModels();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox revisionsComboBox;
    private javax.swing.JLabel revisionsLabel;
    // End of variables declaration//GEN-END:variables
    
}
