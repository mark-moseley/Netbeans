/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics.debugger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.ide.ergonomics.fod.Feature2LayerMapping;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.ModulesInstaller;
import org.netbeans.modules.ide.ergonomics.fod.ProgressMonitor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 * Provider for fake web module extenders. Able to download and enable the proper module
 * as well as delegate to the proper configuration panel.
 *
 * @author Tomas Mysik
 * @author Pavel Flaska
 */
public class DebuggerConfigurationPanel extends JPanel {
    private static final long serialVersionUID = 27938464212508L;

    final ProgressMonitor progressMonitor = new DownloadProgressMonitor();
    private AttachTypeProxy proxy;
    private String attachTypeName;
    private FeatureInfo featureInfo;

    DebuggerConfigurationPanel(final AttachTypeProxy proxy) {
        initComponents();
        this.proxy = proxy;
        this.attachTypeName = proxy.getTypeDisplayName();
        String lblMsg = null;
        String btnMsg = null;
        featureInfo = null;
        for (FeatureInfo info : Feature2LayerMapping.featureTypesLookup().lookupAll(FeatureInfo.class)) {
            if (attachTypeName.equals(info.getAttachTypeName())) {
                featureInfo = info;
                break;
            }
        }
        if (featureInfo != null && featureInfo.isPresent()) { // TODO: do the proper checking
            lblMsg = NbBundle.getMessage(DebuggerConfigurationPanel.class, "LBL_EnableInfo", attachTypeName);
            btnMsg = NbBundle.getMessage(DebuggerConfigurationPanel.class, "LBL_Enable");
        } else {
            lblMsg = NbBundle.getMessage(DebuggerConfigurationPanel.class, "LBL_DownloadInfo", attachTypeName);
            btnMsg = NbBundle.getMessage(DebuggerConfigurationPanel.class, "LBL_Download");
        }

        infoLabel.setText(lblMsg);
        downloadButton.setText(btnMsg);
        setError(" "); // NOI18N
        this.proxy = proxy;
    }

    void setError(String msg) {
        assert SwingUtilities.isEventDispatchThread ();
        errorLabel.setText(msg);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {



        errorLabel = new JLabel();
        infoLabel = new JLabel();
        downloadButton = new JButton();
        progressPanel = new JPanel();

        errorLabel.setForeground(UIManager.getDefaults().getColor("nb.errorForeground"));
        Mnemonics.setLocalizedText(errorLabel, "dummy");
        Mnemonics.setLocalizedText(infoLabel, "dummy");
        Mnemonics.setLocalizedText(downloadButton, "dummy");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.PAGE_AXIS));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(progressPanel, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .add(infoLabel)
                    .add(downloadButton)
                    .add(errorLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(errorLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(infoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(downloadButton)
                .add(18, 18, 18)
                .add(progressPanel, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void downloadButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        downloadButton.setEnabled(false);
        final boolean[] success = new boolean[1];
        final FeatureInfo info = featureInfo;
        Task task = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                success[0] = ModulesInstaller.installModules(progressMonitor, info);
            }
        });
        task.addTaskListener(new TaskListener() {

            public void taskFinished(org.openide.util.Task task) {
                if (success[0]) {
                    proxy.invalidate();
                    SwingUtilities.invokeLater(new Runnable() {
                        private String msg;

                        public void run() {
                            DebuggerConfigurationPanel.this.removeAll();
                            DebuggerConfigurationPanel.this.setLayout(new BorderLayout());
                            DebuggerConfigurationPanel.this.add(proxy.getAttachType().getCustomizer(), BorderLayout.CENTER);
                            DebuggerConfigurationPanel.this.invalidate();
                            DebuggerConfigurationPanel.this.revalidate();
                            DebuggerConfigurationPanel.this.repaint();
                            if (featureInfo.isPresent()) {
                                msg = NbBundle.getMessage(DebuggerConfigurationPanel.class, "MSG_EnableFailed");
                            } else {
                                msg = NbBundle.getMessage(DebuggerConfigurationPanel.class, "MSG_DownloadFailed");
                            }
                            setError(msg);
                            downloadButton.setEnabled(true);
                            progressPanel.removeAll();
                            progressPanel.revalidate();
                            progressPanel.repaint();
                        }
                    });
                }
            }
        });
        task.schedule(0);
    }//GEN-LAST:event_downloadButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton downloadButton;
    private JLabel errorLabel;
    private JLabel infoLabel;
    private JPanel progressPanel;
    // End of variables declaration//GEN-END:variables

    private final class DownloadProgressMonitor implements ProgressMonitor {

        public void onDownload(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onValidate(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onInstall(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onEnable(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        private void updateProgress(final ProgressHandle progressHandle) {
            final JLabel tmpMainLabel = ProgressHandleFactory.createMainLabelComponent(progressHandle);
            final JComponent tmpProgressPanel = ProgressHandleFactory.createProgressComponent(progressHandle);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressPanel.removeAll();
                    progressPanel.add(tmpMainLabel);
                    progressPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    progressPanel.add(tmpProgressPanel);
                    progressPanel.revalidate();
                    progressPanel.repaint();
                }
            });
        }
    }
}
