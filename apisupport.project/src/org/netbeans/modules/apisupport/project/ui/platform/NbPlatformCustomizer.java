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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;
import java.text.MessageFormat;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardPanel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents customizer for managing NetBeans platforms.
 *
 * @author Martin Krauskopf
 */
public final class NbPlatformCustomizer extends JPanel {
    
    static final String CHOOSER_STEP = getMessage("MSG_ChoosePlatfrom"); // NOI18N
    static final String INFO_STEP = getMessage("MSG_PlatformName"); // NOI18N
    
    static final String PLAF_DIR_PROPERTY = "selectedPlafDir"; // NOI18N
    static final String PLAF_LABEL_PROPERTY = "selectedPlafLabel"; // NOI18N
    
    private NbPlatformCustomizerSources sourcesTab;
    private NbPlatformCustomizerModules modulesTab;
    private NbPlatformCustomizerJavadoc javadocTab;
    private NbPlatformCustomizerHarness harnessTab;
    
    public static void showCustomizer() {
        HarnessUpgrader.checkForUpgrade();
        NbPlatformCustomizer customizer = new NbPlatformCustomizer();
        JButton closeButton = new JButton();
        Mnemonics.setLocalizedText(closeButton,
                NbBundle.getMessage(NbPlatformCustomizer.class, "CTL_Close"));
        DialogDescriptor descriptor = new DialogDescriptor(
                customizer,
                getMessage("CTL_NbPlatformManager_Title"), // NOI18N
                true,
                new Object[] {closeButton},
                closeButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(NbPlatformCustomizer.class),
                null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setVisible(true);
        dlg.dispose();
    }
    
    /**
     * Creates new form NbPlatformCustomizer
     */
    private  NbPlatformCustomizer() {
        initComponents();
        initAccessibility();
        initTabs();
        platformsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                refreshPlatform();
            }
        });
        refreshPlatform();
    }
    
    private void initTabs() {
        if (platformsList.getModel().getSize() > 0) {
            platformsList.setSelectedIndex(0);
            sourcesTab = new NbPlatformCustomizerSources();
            modulesTab = new NbPlatformCustomizerModules();
            javadocTab = new NbPlatformCustomizerJavadoc();
            harnessTab = new NbPlatformCustomizerHarness();
            detailPane.addTab(getMessage("CTL_ModulesTab"), modulesTab); // NOI18N
            detailPane.addTab(getMessage("CTL_SourcesTab"), sourcesTab); // NOI18N
            detailPane.addTab(getMessage("CTL_JavadocTab"), javadocTab); // NOI18N
            detailPane.addTab(getMessage("CTL_HarnessTab"), harnessTab); // NOI18N
            Container window = this.getTopLevelAncestor();
            if (window != null && window instanceof Window) {
                ((Window) window).pack();
            }
        }
    }
    
    private void refreshPlatform() {
        NbPlatform plaf = (NbPlatform) platformsList.getSelectedValue();
        if (plaf == null) {
            removeButton.setEnabled(false);
            return;
        }
        plfNameValue.setText(plaf.getLabel());
        plfFolderValue.setText(plaf.getDestDir().getAbsolutePath());
        boolean isValid = plaf.isValid();
        if (isValid) {
            if (sourcesTab == null) {
                initTabs();
            }
            if (sourcesTab != null) {
                modulesTab.setPlatform(plaf);
                sourcesTab.setSourceRootsProvider(plaf);
                javadocTab.setPlatform(plaf);
                harnessTab.setPlatform(plaf);
            }
        } else {
            modulesTab.reset();
            detailPane.setSelectedIndex(0);
        }
        detailPane.setEnabledAt(0, isValid);
        detailPane.setEnabledAt(1, isValid);
        detailPane.setEnabledAt(2, isValid);
        removeButton.setEnabled(!plaf.isDefault());
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(NbPlatformCustomizer.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        platformLbl = new javax.swing.JLabel();
        platformsListSP = new javax.swing.JScrollPane();
        platformsList = PlatformComponentFactory.getNbPlatformsList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        infoPane = new javax.swing.JPanel();
        plfName = new javax.swing.JLabel();
        pflFolder = new javax.swing.JLabel();
        plfNameValue = new javax.swing.JTextField();
        plfFolderValue = new javax.swing.JTextField();
        detailPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        platformLbl.setLabelFor(platformsList);
        org.openide.awt.Mnemonics.setLocalizedText(platformLbl, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "LBL_Platforms"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(platformLbl, gridBagConstraints);

        platformsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        platformsListSP.setViewportView(platformsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 12, 6);
        add(platformsListSP, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "CTL_AddPlatform"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPlatform(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "CTL_RemovePlatfrom"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePlatform(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(removeButton, gridBagConstraints);

        infoPane.setLayout(new java.awt.GridBagLayout());

        plfName.setLabelFor(plfNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(plfName, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "LBL_PlatformName_N"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        infoPane.add(plfName, gridBagConstraints);

        pflFolder.setLabelFor(plfFolderValue);
        org.openide.awt.Mnemonics.setLocalizedText(pflFolder, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "LBL_PlatformFolder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        infoPane.add(pflFolder, gridBagConstraints);

        plfNameValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        infoPane.add(plfNameValue, gridBagConstraints);

        plfFolderValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        infoPane.add(plfFolderValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        infoPane.add(detailPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 12, 0);
        add(infoPane, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private PlatformComponentFactory.NbPlatformListModel getPlafListModel() {
        return (PlatformComponentFactory.NbPlatformListModel) platformsList.getModel();
    }
    
    private void removePlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePlatform
        NbPlatform plaf = (NbPlatform) platformsList.getSelectedValue();
        if (plaf != null) {
            getPlafListModel().removePlatform(plaf);
            platformsList.setSelectedValue(NbPlatform.getDefaultPlatform(), true);
            refreshPlatform();
        }
    }//GEN-LAST:event_removePlatform
    
    private void addPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPlatform
        PlatformChooserWizardPanel chooser = new PlatformChooserWizardPanel(null);
        PlatformInfoWizardPanel info = new PlatformInfoWizardPanel(null);
        WizardDescriptor wd = new WizardDescriptor(new BasicWizardPanel[] {chooser, info});
        initPanel(chooser, wd, 0);
        initPanel(info, wd, 1);
        wd.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wd);
        dialog.setTitle(getMessage("CTL_AddNetbeansPlatformTitle")); // NOI18N
        dialog.setVisible(true);
        dialog.toFront();
        if (wd.getValue() == WizardDescriptor.FINISH_OPTION) {
            String plafDir = (String) wd.getProperty(PLAF_DIR_PROPERTY);
            String plafLabel = (String) wd.getProperty(PLAF_LABEL_PROPERTY);
            String id = plafLabel.replace(' ', '_');
            NbPlatform plaf = getPlafListModel().addPlatform(id, plafDir, plafLabel);
            if (plaf != null) {
                platformsList.setSelectedValue(plaf, true);
                refreshPlatform();
            }
        }
    }//GEN-LAST:event_addPlatform
    
    private void initPanel(BasicWizardPanel panel, WizardDescriptor wd, int i) {
        panel.setSettings(wd);
        JComponent jc = (JComponent) panel.getComponent();
        jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] { // NOI18N
            CHOOSER_STEP, INFO_STEP
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTabbedPane detailPane;
    private javax.swing.JPanel infoPane;
    private javax.swing.JLabel pflFolder;
    private javax.swing.JLabel platformLbl;
    private javax.swing.JList platformsList;
    private javax.swing.JScrollPane platformsListSP;
    private javax.swing.JTextField plfFolderValue;
    private javax.swing.JLabel plfName;
    private javax.swing.JTextField plfNameValue;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_NbPlatformCustomizer"));        
        platformsList.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_platformsList"));
        plfFolderValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_plfFolderValue"));
        plfNameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_plfNameValue"));
        removeButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_removeButton"));
        addButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_addButton"));
    }
}
