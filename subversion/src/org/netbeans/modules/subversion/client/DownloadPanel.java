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

/*
 * DownloadPanel.java
 *
 * Created on Jul 9, 2008, 5:10:32 PM
 */

package org.netbeans.modules.subversion.client;

/**
 *
 * @author tomas
 */
public class DownloadPanel extends javax.swing.JPanel {

    /** Creates new form DownloadPanel */
    public DownloadPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setMinimumSize(new java.awt.Dimension(500, 300));

        progressLabel.setText(org.openide.util.NbBundle.getMessage(DownloadPanel.class, "DownloadPanel.progressLabel.text")); // NOI18N

        progressBarPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout progressPanelLayout = new org.jdesktop.layout.GroupLayout(progressPanel);
        progressPanel.setLayout(progressPanelLayout);
        progressPanelLayout.setHorizontalGroup(
            progressPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, progressPanelLayout.createSequentialGroup()
                .add(progressLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(progressBarPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 236, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        progressPanelLayout.setVerticalGroup(
            progressPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(progressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(progressPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, progressBarPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, progressLabel))
                .addContainerGap())
        );

        licenseTextPane.setText("null");
        jScrollPane1.setViewportView(licenseTextPane);

        acceptCheckBox.setText(org.openide.util.NbBundle.getMessage(DownloadPanel.class, "DownloadPanel.acceptCheckBox.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DownloadPanel.class, "DownloadPanel.jLabel1.text")); // NOI18N

        jScrollPane2.setBorder(null);

        jTextArea1.setBackground(jLabel1.getBackground());
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(DownloadPanel.class, "DownloadPanel.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea1);

        org.jdesktop.layout.GroupLayout licensePanelLayout = new org.jdesktop.layout.GroupLayout(licensePanel);
        licensePanel.setLayout(licensePanelLayout);
        licensePanelLayout.setHorizontalGroup(
            licensePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(licensePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(licensePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(licensePanelLayout.createSequentialGroup()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                        .add(342, 342, 342))
                    .add(licensePanelLayout.createSequentialGroup()
                        .add(acceptCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                        .add(126, 126, 126))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE))
                .addContainerGap())
        );
        licensePanelLayout.setVerticalGroup(
            licensePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(licensePanelLayout.createSequentialGroup()
                .add(jLabel1)
                .add(4, 4, 4)
                .add(jScrollPane2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(acceptCheckBox)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, licensePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(progressPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(licensePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(157, 157, 157)
                    .add(progressPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(123, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JCheckBox acceptCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    final javax.swing.JPanel licensePanel = new javax.swing.JPanel();
    final javax.swing.JTextPane licenseTextPane = new javax.swing.JTextPane();
    final javax.swing.JPanel progressBarPanel = new javax.swing.JPanel();
    final javax.swing.JLabel progressLabel = new javax.swing.JLabel();
    final javax.swing.JPanel progressPanel = new javax.swing.JPanel();
    // End of variables declaration//GEN-END:variables

}
