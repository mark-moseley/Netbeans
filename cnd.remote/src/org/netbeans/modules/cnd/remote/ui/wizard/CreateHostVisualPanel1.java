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
package org.netbeans.modules.cnd.remote.ui.wizard;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbBundle;

/*package*/ final class CreateHostVisualPanel1 extends JPanel {

    private final HostsListTableModel tableModel = new HostsListTableModel();
    private final CreateHostData data;

    public CreateHostVisualPanel1(CreateHostData data, final ChangeListener listener) {
        this.data = data;
        initComponents();
        textPort.setText(Integer.toString(ExecutionEnvironmentFactory.DEFAULT_PORT));
        textHostname.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }

            public void removeUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }

            public void changedUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }
        });
        pbarStatusPanel.removeAll();
        pbarStatusPanel.add(ProgressHandleFactory.createProgressComponent(tableModel.getProgressHandle()), BorderLayout.CENTER);
        pbarStatusPanel.validate();

        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                tableModel.start(new Runnable() {
                    public void run() {
                        pbarStatusPanel.setVisible(false);
                    }
                });
            }
            public void ancestorRemoved(AncestorEvent event) {
                tableModel.stop();
            }
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    void init() {
        textPort.setText(Integer.toString(data.getPort()));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CreateHostVisualPanel1.Title");
    }

    public String getHostname() {
        return textHostname.getText();
    }

    public Integer getPort() {
        try {
            return Integer.valueOf(Integer.parseInt(textPort.getText()));
        } catch(NumberFormatException e) {
            //return Integer.valueOf(ExecutionEnvironmentFactory.DEFAULT_PORT);
            return null;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        textHostname = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableHostsList = new javax.swing.JTable();
        pbarStatusPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        textPort = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(534, 409));
        setRequestFocusEnabled(false);

        jLabel1.setLabelFor(textHostname);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.jLabel1.text")); // NOI18N

        textHostname.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.textHostname.text")); // NOI18N

        jLabel2.setLabelFor(tableHostsList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.jLabel2.text")); // NOI18N

        tableHostsList.setModel(tableModel);
        tableHostsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableHostsListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tableHostsList);
        tableHostsList.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.tableHostsList.columnModel.title0")); // NOI18N
        tableHostsList.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.tableHostsList.columnModel.title1")); // NOI18N

        pbarStatusPanel.setMaximumSize(new java.awt.Dimension(2147483647, 10));
        pbarStatusPanel.setMinimumSize(new java.awt.Dimension(100, 10));
        pbarStatusPanel.setLayout(new java.awt.BorderLayout());

        jLabel3.setLabelFor(textPort);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.jLabel3.text")); // NOI18N

        textPort.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.textPort.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textHostname, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 45, Short.MAX_VALUE)
                .add(jLabel3)
                .add(4, 4, 4)
                .add(textPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(jLabel2)
                .addContainerGap(402, Short.MAX_VALUE))
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
            .add(pbarStatusPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(textHostname, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(textPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .add(18, 18, 18)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(pbarStatusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableHostsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableHostsListMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            int row = tableHostsList.rowAtPoint(evt.getPoint());
            textHostname.setText(((HostsListTableModel) tableHostsList.getModel()).getHostName(row));
        }
}//GEN-LAST:event_tableHostsListMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pbarStatusPanel;
    private javax.swing.JTable tableHostsList;
    private javax.swing.JTextField textHostname;
    private javax.swing.JTextField textPort;
    // End of variables declaration//GEN-END:variables
    }

