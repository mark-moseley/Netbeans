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
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.text.Collator;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous
 */
public class CustomizerRun extends JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = -5494488817914071L;
    private final ConfigComboBoxModel comboModel;
    private final ConfigManager manager;
    private final RunAsPanel.InsidePanel[] insidePanels;

    public CustomizerRun(PhpProjectProperties properties, final Category category) {
        manager = new ConfigManager(properties);
        insidePanels = new RunAsPanel.InsidePanel[] {
            new RunAsLocalWeb(properties.getProject(), manager, category),
            new RunAsRemoteWeb(properties.getProject(), manager, category),
            new RunAsScript(properties.getProject(), manager, category),
        };
        initComponents();
        comboModel = new ConfigComboBoxModel();
        configCombo.setModel(comboModel);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        configCombo.setSelectedItem(manager.currentConfiguration().getName());
    }

    private Configuration configurationFor(String configName) {
        return manager.configurationFor(configName);
    }

    private void selectCurrentItem() {
        final Configuration config = manager.currentConfiguration();
        configCombo.setSelectedItem(config.getName());
        configDel.setEnabled(!config.isDefault());
    }

    private class ConfigComboBoxModel extends DefaultComboBoxModel {
        private static final long serialVersionUID = -2086330612256611127L;

        public ConfigComboBoxModel() {
            Set<String> alphaConfigs = new TreeSet<String>(getComparator());
            alphaConfigs.addAll(manager.configurationNames());
            for (String config : alphaConfigs) {
                this.addElement(config);
            }
        }

        private Comparator<String> getComparator() {
            return new Comparator<String>() {
                Collator coll = Collator.getInstance();

                public int compare(String s1, String s2) {
                    String lbl1 = configurationFor(s1).getDisplayName();
                    String lbl2 = configurationFor(s2).getDisplayName();
                    return coll.compare(lbl1, lbl2);
                }
            };
        }
    }

    private final class ConfigListCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        private static final long serialVersionUID = 21963218553211553L;

        public ConfigListCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            String config = (String) value;
            //String label = (config != null) ? configurationFor(config).getDisplayName() : null;
            String label = configurationFor(config).getDisplayName();
            setText(label);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        configNew = new javax.swing.JButton();
        configDel = new javax.swing.JButton();
        separator = new javax.swing.JSeparator();
        runPanel = new RunAsPanel(insidePanels);

        configLabel.setLabelFor(configCombo);
        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_Configuration")); // NOI18N

        configCombo.setRenderer(new ConfigListCellRenderer());
        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_New")); // NOI18N
        configNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configNewActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configDel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_Delete")); // NOI18N
        configDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDelActionPerformed(evt);
            }
        });

        runPanel.setLayout(new java.awt.CardLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, runPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, separator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(configLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configCombo, 0, 142, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configNew)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configDel)))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(configLabel)
                    .add(configCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(configNew)
                    .add(configDel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(separator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(runPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        String config = (String) configCombo.getSelectedItem();
        manager.markAsCurrentConfiguration(config == null || config.length() == 0 ? null : config);
        selectCurrentItem();
    }//GEN-LAST:event_configComboActionPerformed

    private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(CustomizerRun.class, "LBL_ConfigurationName"),
                NbBundle.getMessage(CustomizerRun.class, "LBL_CreateNewConfiguration"));

        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            String name = d.getInputText();
        String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N

            if (manager.exists(config)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(CustomizerRun.class, "MSG_ConfigurationExists", config),
                        NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
            Configuration cfg = manager.createNew(config, name);
            comboModel.addElement(config);
            manager.markAsCurrentConfiguration(config);
            selectCurrentItem();
        }
    }//GEN-LAST:event_configNewActionPerformed

    private void configDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDelActionPerformed
        String config = (String) configCombo.getSelectedItem();
        assert config != null;
        comboModel.removeElement(config);
        configurationFor(config).delete();
        selectCurrentItem();
    }//GEN-LAST:event_configDelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox configCombo;
    private javax.swing.JButton configDel;
    private javax.swing.JLabel configLabel;
    private javax.swing.JButton configNew;
    private javax.swing.JPanel runPanel;
    private javax.swing.JSeparator separator;
    // End of variables declaration//GEN-END:variables

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }
}
