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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.remote.sync.SyncUtils;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.openide.util.NbBundle;

/*package*/ final class CreateHostVisualPanel3 extends JPanel {

    public CreateHostVisualPanel3(CreateHostData data) {
        this.data = data;
        initComponents();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CreateHostVisualPanel3.Title");//NOI18N
    }

    private final CreateHostData data;
    private CompilerSetManager compilerSetManager;

    void init() {
        textHostDisplayName.setText(data.getExecutionEnvironment().getDisplayName());
        compilerSetManager = data.getCacheManager().getCompilerSetManagerCopy(data.getExecutionEnvironment());
        labelPlatformValue.setText(PlatformTypes.toString(compilerSetManager.getPlatform()));
        labelUsernameValue.setText(data.getExecutionEnvironment().getUser());
        labelHostnameValue.setText(data.getExecutionEnvironment().getHost());
        cbDefaultToolchain.setModel(new DefaultComboBoxModel(compilerSetManager.getCompilerSets().toArray()));
        cbDefaultToolchain.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel out = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    out.setText(""); //NOI18N
                } else {
                    CompilerSet cset = (CompilerSet) value;
                    out.setText(cset.getDisplayName());
                }
                return out;
            }
        });
        cbDefaultToolchain.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                compilerSetManager.setDefault((CompilerSet) cbDefaultToolchain.getSelectedItem());
            }
        });
        List<CompilerSet> sets2 = compilerSetManager.getCompilerSets();
        StringBuilder st = new StringBuilder();
        for (CompilerSet set : sets2) {
            if (st.length() > 0) {
                st.append('\n'); //NOI18N
            }
            st.append(set.getName()).append(" (").append(set.getDirectory()).append(")");//NOI18N
        }
        jTextArea1.setText(st.toString());

        SyncUtils.arrangeComboBox(cbSyncMode, data.getExecutionEnvironment());
    }

    String getHostDisplayName() {
        return textHostDisplayName.getText();
    }

    RemoteSyncFactory getRemoteSyncFactory() {
        return (RemoteSyncFactory) cbSyncMode.getSelectedItem();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        syncButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        textHostDisplayName = new javax.swing.JTextField();
        labelPlatform = new javax.swing.JLabel();
        labelHostname = new javax.swing.JLabel();
        labelUsername = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cbDefaultToolchain = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        labelPlatformValue = new javax.swing.JLabel();
        labelHostnameValue = new javax.swing.JLabel();
        labelUsernameValue = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbSyncMode = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(534, 409));
        setRequestFocusEnabled(false);

        jLabel1.setLabelFor(textHostDisplayName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.jLabel1.text")); // NOI18N

        labelPlatform.setLabelFor(labelPlatformValue);
        labelPlatform.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelPlatform.text")); // NOI18N

        labelHostname.setLabelFor(labelHostnameValue);
        labelHostname.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelHostname.text")); // NOI18N

        labelUsername.setLabelFor(labelUsernameValue);
        labelUsername.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelUsername.text")); // NOI18N

        jLabel2.setLabelFor(jTextArea1);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.jLabel2.text")); // NOI18N

        jLabel3.setLabelFor(cbDefaultToolchain);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.jLabel3.text")); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(1);
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        org.openide.awt.Mnemonics.setLocalizedText(labelPlatformValue, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelPlatformValue.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelHostnameValue, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelHostnameValue.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelUsernameValue, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelUsernameValue.text")); // NOI18N

        jLabel4.setLabelFor(cbSyncMode);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.jLabel4.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textHostDisplayName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(labelPlatform)
                    .add(labelHostname)
                    .add(labelUsername))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(labelUsernameValue)
                    .add(labelHostnameValue)
                    .add(labelPlatformValue))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(jLabel2)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbSyncMode, 0, 424, Short.MAX_VALUE)
                    .add(cbDefaultToolchain, 0, 424, Short.MAX_VALUE)))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(textHostDisplayName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelPlatform)
                    .add(labelPlatformValue))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelHostname)
                    .add(labelHostnameValue))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelUsername)
                    .add(labelUsernameValue))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(cbDefaultToolchain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbSyncMode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .add(29, 29, 29))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbDefaultToolchain;
    private javax.swing.JComboBox cbSyncMode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel labelHostname;
    private javax.swing.JLabel labelHostnameValue;
    private javax.swing.JLabel labelPlatform;
    private javax.swing.JLabel labelPlatformValue;
    private javax.swing.JLabel labelUsername;
    private javax.swing.JLabel labelUsernameValue;
    private javax.swing.ButtonGroup syncButtonGroup;
    private javax.swing.JTextField textHostDisplayName;
    // End of variables declaration//GEN-END:variables
}

