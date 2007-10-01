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

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.modules.java.freeform.JavaProjectNature;
import org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Wizard panel which lets user select source and test package roots and source level.
 * Also shows some other info.
 * @author  David Konecny
 */
public class SourceFoldersPanel extends JPanel implements HelpCtx.Provider, ListSelectionListener {
    
    private SourcesModel sourceFoldersModel;
    private SourcesModel testFoldersModel;
    private ChangeListener listener;
    private boolean isWizard;
    private ProjectModel model;

    /** Creates new form SourceFoldersPanel */
    public SourceFoldersPanel() {
        this(true);
    }
    
    public SourceFoldersPanel(boolean isWizard) {
        this.isWizard = isWizard;
        initComponents();
        sourceFoldersModel = new SourcesModel(false);
        sourceFolders.setModel(sourceFoldersModel);
        sourceFolders.getSelectionModel().addListSelectionListener(this);
        testFoldersModel = new SourcesModel(true);
        testFolders.setModel(testFoldersModel);
        testFolders.getSelectionModel().addListSelectionListener(this);
        sourceFolders.getTableHeader().setReorderingAllowed(false);
        sourceFolders.setDefaultRenderer(String.class, new ToolTipRenderer ());
        testFolders.getTableHeader().setReorderingAllowed(false);
        testFolders.setDefaultRenderer(String.class, new ToolTipRenderer ());
        initSourceLevel();
        jLabel1.setVisible(isWizard);
        projectFolderLabel.setVisible(!isWizard);
        projectContentLabel.setVisible(!isWizard);
        buildScriptLabel.setVisible(!isWizard);
        projectFolder.setVisible(!isWizard);
        contentFolder.setVisible(!isWizard);
        buildScript.setVisible(!isWizard);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( SourceFoldersPanel.class );
    }
    
    /** WizardDescriptor.Panel can set one change listener 
     * to be notified about changes in the panel. */
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }
    
    private void initSourceLevel() {
        sourceLevel.addItem(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_JDK13")); // NOI18N
        sourceLevel.addItem(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_JDK14")); // NOI18N
        sourceLevel.addItem(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_JDK15")); // NOI18N
    }
    
    private void updateButtons() {
        removeFolder.setEnabled(sourceFolders.getSelectedRowCount()>0);
        removeTestFolder.setEnabled(testFolders.getSelectedRowCount()>0);
        updateUpDownButtons();
    }
    
    public void valueChanged(ListSelectionEvent e) {
        updateButtons();
    }

    private void updateUpDownButtons() {
        int first = sourceFolders.getSelectionModel().getMinSelectionIndex();
        int last = sourceFolders.getSelectionModel().getMaxSelectionIndex();
        upFolder.setEnabled(first > 0);
        downFolder.setEnabled(last > -1 && last < sourceFoldersModel.getRowCount()-1);
        first = testFolders.getSelectionModel().getMinSelectionIndex();
        last = testFolders.getSelectionModel().getMaxSelectionIndex();
        upTestFolder.setEnabled(first > 0);
        downTestFolder.setEnabled(last > -1 && last < testFoldersModel.getRowCount()-1);
    }
    
    private void updateSourceLevelCombo(String sourceLevelValue) {
        if (sourceLevelValue.equals("1.3")) { // NOI18N
            sourceLevel.setSelectedIndex(0);
        } else if (sourceLevelValue.equals("1.4")) { // NOI18N
            sourceLevel.setSelectedIndex(1);
        } else if (sourceLevelValue.equals("1.5")) { // NOI18N
            sourceLevel.setSelectedIndex(2);
        } else {
            // user specified some other value in project.xml
            sourceLevel.addItem(sourceLevelValue);
            sourceLevel.setSelectedIndex(3);
        }
    }
    
    private void updateEncodingCombo() {
        encodingComboBox.setModel(new EncodingModel());
    }
    
    private String getSourceLevelValue(int index) {
        switch (index) {
            case 0: return "1.3"; // NOI18N
            case 1: return "1.4"; // NOI18N
            case 2: return "1.5"; // NOI18N
            default: return null;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        sourceLevel = new javax.swing.JComboBox();
        addFolder = new javax.swing.JButton();
        removeFolder = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceFolders = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        testFolders = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        addTestFolder = new javax.swing.JButton();
        removeTestFolder = new javax.swing.JButton();
        upFolder = new javax.swing.JButton();
        downFolder = new javax.swing.JButton();
        downTestFolder = new javax.swing.JButton();
        upTestFolder = new javax.swing.JButton();
        projectFolderLabel = new javax.swing.JLabel();
        projectContentLabel = new javax.swing.JLabel();
        buildScriptLabel = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        contentFolder = new javax.swing.JTextField();
        buildScript = new javax.swing.JTextField();
        includesExcludesButton = new javax.swing.JButton();
        encodingLabel = new javax.swing.JLabel();
        encodingComboBox = new javax.swing.JComboBox();

        setMinimumSize(new java.awt.Dimension(265, 375));
        setPreferredSize(new java.awt.Dimension(800, 500));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_jLabel1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_jLabel1")); // NOI18N

        jLabel2.setLabelFor(sourceFolders);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_jLabel2")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_jLabel2")); // NOI18N

        jLabel3.setLabelFor(sourceLevel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_jLabel3")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceLevel_Label")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_jLabel3")); // NOI18N

        sourceLevel.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceLevelItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(sourceLevel, gridBagConstraints);
        sourceLevel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceLevel_Name")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/java/freeform/ui/Bundle"); // NOI18N
        sourceLevel.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceFoldersPanel_sourceLevel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_addFolder")); // NOI18N
        addFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(addFolder, gridBagConstraints);
        addFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_addFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_removeFolder")); // NOI18N
        removeFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(removeFolder, gridBagConstraints);
        removeFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_removeFolder")); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setViewportView(sourceFolders);
        sourceFolders.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_sourceFolders")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_jScrollPanel1")); // NOI18N

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setViewportView(testFolders);
        testFolders.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_SourceFoldersPanel_testFolders")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jScrollPane2, gridBagConstraints);
        jScrollPane2.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceFoldersPanel_jScrollPane2")); // NOI18N

        jLabel4.setLabelFor(testFolders);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_TestSourceFoldersPanel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addTestFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_addTestFolder")); // NOI18N
        addTestFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTestFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(addTestFolder, gridBagConstraints);
        addTestFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceFoldersPanel_addTestFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeTestFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_removeTestFolder")); // NOI18N
        removeTestFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTestFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(removeTestFolder, gridBagConstraints);
        removeTestFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceFoldersPanel_removeTestFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_upFolder")); // NOI18N
        upFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(upFolder, gridBagConstraints);
        upFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceFoldersPanel_upFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(downFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_downFolder")); // NOI18N
        downFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(downFolder, gridBagConstraints);
        downFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceFoldersPanel_downFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(downTestFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_downTestFolder")); // NOI18N
        downTestFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downTestFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(downTestFolder, gridBagConstraints);
        downTestFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceFoldersPanel_downTestFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upTestFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_upTestFolder")); // NOI18N
        upTestFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upTestFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(upTestFolder, gridBagConstraints);
        upTestFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceFoldersPanel_upTestFolder")); // NOI18N

        projectFolderLabel.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_ProjFolderLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(projectFolderLabel, gridBagConstraints);

        projectContentLabel.setLabelFor(contentFolder);
        org.openide.awt.Mnemonics.setLocalizedText(projectContentLabel, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_ProjContentLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(projectContentLabel, gridBagConstraints);

        buildScriptLabel.setLabelFor(buildScript);
        org.openide.awt.Mnemonics.setLocalizedText(buildScriptLabel, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_BuildScriptLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        add(buildScriptLabel, gridBagConstraints);

        projectFolder.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(projectFolder, gridBagConstraints);
        projectFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_SourceFoldersPanel_projectFolder")); // NOI18N

        contentFolder.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(contentFolder, gridBagConstraints);
        contentFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_SourceFoldersPanel_contentFolder")); // NOI18N

        buildScript.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(buildScript, gridBagConstraints);
        buildScript.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_SourceFoldersPanel_buildScript")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(includesExcludesButton, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "SourceFoldersPanel.includesExcludesButton.text")); // NOI18N
        includesExcludesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includesExcludesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        add(includesExcludesButton, gridBagConstraints);
        includesExcludesButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_Includes-Excludes_Name")); // NOI18N
        includesExcludesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_Includes-Excludes_Desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_Encoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
        add(encodingLabel, gridBagConstraints);
        encodingLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_Encoding_Label_Name")); // NOI18N
        encodingLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_Encoding_Label_Desc")); // NOI18N

        encodingComboBox.setRenderer(new EncodingRenderer());
        encodingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encodingComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(encodingComboBox, gridBagConstraints);
        encodingComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_Encoding_Name")); // NOI18N
        encodingComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_Encoding_Desc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void encodingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encodingComboBoxActionPerformed
        Object selItem = encodingComboBox.getModel().getSelectedItem();
        if (selItem instanceof Charset) {
            model.setEncoding(((Charset) selItem).name());
            // XXX Should we set this here?
            FileEncodingQuery.setDefaultEncoding((Charset) selItem);
        } else {
            model.setEncoding(selItem.toString());
        }
}//GEN-LAST:event_encodingComboBoxActionPerformed

private void includesExcludesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includesExcludesButtonActionPerformed
        // Assumes they all use the same include/exclude patterns. Close enough.
        IncludeExcludeVisualizer v = new IncludeExcludeVisualizer();
        List<File> roots = new ArrayList<File>();
        for (JavaProjectGenerator.SourceFolder folder : model.getSourceFolders()) {
            v.setIncludePattern(folder.includes != null ? folder.includes : "**");
            v.setExcludePattern(folder.excludes != null ? folder.excludes : "");
            roots.add(Util.resolveFile(model.getEvaluator(), model.getNBProjectFolder(), folder.location));
        }
        v.setRoots(roots.toArray(new File[roots.size()]));
        DialogDescriptor dd = new DialogDescriptor(v.getVisualizerPanel(),
                NbBundle.getMessage(SourceFoldersPanel.class, "SourceFoldersPanel.title.includeExclude"));
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd))) {
            for (JavaProjectGenerator.SourceFolder folder : model.getSourceFolders()) {
                folder.includes = v.getIncludePattern().equals("**") ? null : v.getIncludePattern();
                folder.excludes = v.getExcludePattern().equals("") ? null : v.getExcludePattern();
            }
        }
    }//GEN-LAST:event_includesExcludesButtonActionPerformed

    private void downTestFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downTestFolderActionPerformed
        int[] indeces = testFolders.getSelectedRows();
        if (indeces.length == 0) {
            return;
        }        
        for (int i=indeces.length-1; i>=0; i--) {
            int fromIndex = calcRealSourceIndex(indeces[i], true);
            model.moveSourceFolder(fromIndex, fromIndex+1);            
        }
        testFoldersModel.fireTableDataChanged();        
        testFolders.getSelectionModel().clearSelection();
        for (int i=0; i<indeces.length; i++) {
            testFolders.getSelectionModel().addSelectionInterval (indeces[i]+1, indeces[i]+1);
        }
    }//GEN-LAST:event_downTestFolderActionPerformed

    private void upTestFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upTestFolderActionPerformed
        int[] indeces = testFolders.getSelectedRows();
        if (indeces.length == 0) {
            return;
        }
        for (int i=0; i < indeces.length; i++) {
            int fromIndex = calcRealSourceIndex(indeces[i], true);
            model.moveSourceFolder(fromIndex, fromIndex-1);
        }        
        testFoldersModel.fireTableDataChanged();
        testFolders.getSelectionModel().clearSelection();
        for (int i=0; i<indeces.length; i++) {
            testFolders.getSelectionModel().addSelectionInterval (indeces[i]-1, indeces[i]-1);
        }
    }//GEN-LAST:event_upTestFolderActionPerformed

    private void downFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downFolderActionPerformed
        int[] indeces = sourceFolders.getSelectedRows();
        if (indeces.length == 0) {
            return;
        }
        for (int i=indeces.length-1; i>=0; i--) {
            int fromIndex = calcRealSourceIndex(indeces[i], false);
            model.moveSourceFolder(fromIndex, fromIndex+1);
        }
        sourceFoldersModel.fireTableDataChanged();
        sourceFolders.getSelectionModel().clearSelection();
        for (int i=0; i<indeces.length; i++) {
            sourceFolders.getSelectionModel().addSelectionInterval (indeces[i]+1, indeces[i]+1);
        }
    }//GEN-LAST:event_downFolderActionPerformed

    private void upFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upFolderActionPerformed
        int[] indeces = sourceFolders.getSelectedRows();
        if (indeces.length == 0) {
            return;
        }
        for (int i=0; i < indeces.length; i++) {
            int fromIndex = calcRealSourceIndex(indeces[i], false);
            model.moveSourceFolder(fromIndex, fromIndex-1);
        }        
        sourceFoldersModel.fireTableDataChanged();
        sourceFolders.getSelectionModel().clearSelection();
        for (int i=0; i<indeces.length; i++) {
            sourceFolders.getSelectionModel().addSelectionInterval (indeces[i]-1, indeces[i]-1);
        }
    }//GEN-LAST:event_upFolderActionPerformed

    private void removeTestFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTestFolderActionPerformed
        int[] indeces = testFolders.getSelectedRows();
        if (indeces.length == 0) {
            return;
        }
        for (int i = indeces.length-1; i>=0; i--) {
            String location = getItem(indeces[i], true).location;
            model.removeSourceFolder(calcRealSourceIndex(indeces[i], true));
        }
        testFoldersModel.fireTableDataChanged();
        if (listener != null) {
            listener.stateChanged(null);
        }
        updateButtons();
    }//GEN-LAST:event_removeTestFolderActionPerformed

    private void addTestFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTestFolderActionPerformed
        doAddFolderActionPerformed(evt, true);
    }//GEN-LAST:event_addTestFolderActionPerformed

    private void sourceLevelItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sourceLevelItemStateChanged
        if (sourceLevel.getSelectedIndex() != -1 && model != null) {
            String sl = getSourceLevelValue(sourceLevel.getSelectedIndex());
            if (sl == null) {
                sl = (String)sourceLevel.getSelectedItem();
            }
            model.setSourceLevel(sl);
        }
    }//GEN-LAST:event_sourceLevelItemStateChanged

    private void removeFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFolderActionPerformed
        int[] indeces = sourceFolders.getSelectedRows();
        if (indeces.length == 0) {
            return;
        }
        for (int i = indeces.length - 1; i>=0; i--) {
            String location = getItem(indeces[i], false).location;
            model.removeSourceFolder(calcRealSourceIndex(indeces[i], false));
        }
        sourceFoldersModel.fireTableDataChanged();
        if (listener != null) {
            listener.stateChanged(null);
        }
        updateButtons();
    }//GEN-LAST:event_removeFolderActionPerformed

    private void addFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFolderActionPerformed
        doAddFolderActionPerformed(evt, false);
    }//GEN-LAST:event_addFolderActionPerformed

    private void doAddFolderActionPerformed(java.awt.event.ActionEvent evt, boolean isTests) {                                          
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (model.getBaseFolder() != null) {
            File files[] = model.getBaseFolder().listFiles();
            if (files != null && files.length > 0) {
                chooser.setSelectedFile(files[0]);
            } else {
                chooser.setSelectedFile(model.getBaseFolder());
            }
        }
        if (isTests) {
            chooser.setDialogTitle(NbBundle.getMessage(SourceFoldersPanel.class, "LBL_Browse_Test_Folder"));
        } else {
            chooser.setDialogTitle(NbBundle.getMessage(SourceFoldersPanel.class, "LBL_Browse_Source_Folder"));
        }
        chooser.setMultiSelectionEnabled(true);
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File files[] = chooser.getSelectedFiles();
            model.setEncoding(encodingComboBox.getModel().getSelectedItem().toString());
            Set<File> invalidRoots = processRoots(model, files, isTests, isWizard);
            if (isTests) {
                testFoldersModel.fireTableDataChanged();
            } else {
                sourceFoldersModel.fireTableDataChanged();
            }
            if (listener != null) {
                listener.stateChanged(null);
            }
            updateButtons();
            if (invalidRoots.size()>0) {
                showInvalidRootsWarning (invalidRoots);
            }
        }
    }
    
    /*package private for tests*/static Set<File> processRoots(ProjectModel model, File[] files, boolean isTests, boolean isWizard) {
        Set<File> invalidRoots = new HashSet<File>();
        
        FILE: for (File file : files) {
            File sourceLoc = FileUtil.normalizeFile(file);
            String location = Util.relativizeLocation(model.getBaseFolder(), model.getNBProjectFolder(), sourceLoc);
            Project p, thisProject = isWizard ? null : FileOwnerQuery.getOwner(model.getNBProjectFolder().toURI());
            if ((p = FileOwnerQuery.getOwner(sourceLoc.toURI())) != null && (thisProject == null || !thisProject.equals(p)) && !isParentOf(model.getNBProjectFolder(), sourceLoc) && !isParentOf(model.getBaseFolder(), sourceLoc)) {
                invalidRoots.add(sourceLoc);
            } else {
                List<JavaProjectGenerator.SourceFolder> sourceFolders = model.getSourceFolders();
                for (JavaProjectGenerator.SourceFolder sf : sourceFolders) {
                    if (location.equals(sf.location)) {
                        if ((isTests && !model.isTestSourceFolder(sf))
                        ||  (!isTests && model.isTestSourceFolder(sf))) {
                            invalidRoots.add(sourceLoc);
                        }
                        continue FILE;
                    }
                }
                
                JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
                sf.location = location;
                sf.type = ProjectModel.TYPE_JAVA;
                sf.style = JavaProjectNature.STYLE_PACKAGES;
                sf.label = getDefaultLabel(sf.location, isTests);
                if (!sourceFolders.isEmpty()) {
                    JavaProjectGenerator.SourceFolder prototype = sourceFolders.iterator().next();
                    sf.includes = prototype.includes;
                    sf.excludes = prototype.excludes;
                }
                sf.encoding = model.getEncoding();
                model.addSourceFolder(sf, isTests);
            }
        }
        
        return invalidRoots;
    }
    
    private static boolean isParentOf(File parent, File child) {
        while (child != null && !child.equals(parent))
            child = child.getParentFile();
        
        return child != null && child.equals(parent);
    }
    
    private void showInvalidRootsWarning (Set<File> invalidRoots) {
        JButton closeOption = new JButton (NbBundle.getMessage(SourceFoldersPanel.class,"CTL_SourceFolderPanel_Close"));
        closeOption.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(SourceFoldersPanel.class,"AD_SourceFolderPanel_Close"));        
        JPanel warning = new WarningDlg (invalidRoots);                
        String message = NbBundle.getMessage(SourceFoldersPanel.class,"MSG_InvalidRoot");
        JOptionPane optionPane = new JOptionPane (new Object[] {message, warning},
            JOptionPane.WARNING_MESSAGE,
            0, 
            null, 
            new Object[0], 
            null);
        optionPane.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(SourceFoldersPanel.class,"AD_InvalidRootDlg"));
        DialogDescriptor dd = new DialogDescriptor (optionPane,
            NbBundle.getMessage(SourceFoldersPanel.class,"TITLE_InvalidRoot"),
            true,
            new Object[] {
                closeOption,
            },
            closeOption,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            null);                
        DialogDisplayer.getDefault().notify(dd);
    }
    
    static String getDefaultLabel(String location, boolean isTests) {
        if (location.equals(".") || ProjectConstants.PROJECT_LOCATION_PREFIX.equals(location + "/")) { // NOI18N
            // #54428 - src dir *is* project dir, so use a more reasonable name.
            return isTests ?
                NbBundle.getMessage(SourceFoldersPanel.class, "LBL_default_test_packages") :
                NbBundle.getMessage(SourceFoldersPanel.class, "LBL_default_source_packages");
        }
        // #47386 - remove "${project.dir}/" from label
        String relloc = location;
        if (relloc.startsWith(ProjectConstants.PROJECT_LOCATION_PREFIX)) {
            relloc = relloc.substring(ProjectConstants.PROJECT_LOCATION_PREFIX.length());
        }
        return relloc.replace('/', File.separatorChar); // NOI18N // Fix for #45816 sourceLoc.getName();
    }
    
    /**
     * Convert given string value (e.g. "${project.dir}/src" to a file
     * and try to relativize it.
     */
    public static String getLocationDisplayName(PropertyEvaluator evaluator, File base, String val) {
        File f = Util.resolveFile(evaluator, base, val);
        if (f == null) {
            return val;
        }
        String location = f.getAbsolutePath();
        if (CollocationQuery.areCollocated(base, f)) {
            location = PropertyUtils.relativizeFile(base, f).replace('/', File.separatorChar); // NOI18N
        }
        return location;
    }
    
    private JavaProjectGenerator.SourceFolder getItem(int index, boolean tests) {
        return model.getSourceFolder(calcRealSourceIndex(index, tests));
    }

    private int calcRealSourceIndex(int index, boolean tests) {
        int realIndex = 0;
        for (int i=0; i<model.getSourceFoldersCount(); i++) {
            JavaProjectGenerator.SourceFolder sf = model.getSourceFolder(i);
            boolean isTest = model.isTestSourceFolder(sf);
            if (tests && !isTest) {
                continue;
            }
            if (!tests && isTest) {
                continue;
            }
            if (index == realIndex) {
                return i;
            }
            realIndex++;
        }
        throw new ArrayIndexOutOfBoundsException("index="+index+" tests="+tests+" realIndex="+realIndex);
    }

    
    public boolean hasSomeSourceFolder() {
        return model.getSourceFoldersCount() > 0;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFolder;
    private javax.swing.JButton addTestFolder;
    private javax.swing.JTextField buildScript;
    private javax.swing.JLabel buildScriptLabel;
    private javax.swing.JTextField contentFolder;
    private javax.swing.JButton downFolder;
    private javax.swing.JButton downTestFolder;
    private javax.swing.JComboBox encodingComboBox;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JButton includesExcludesButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel projectContentLabel;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JButton removeFolder;
    private javax.swing.JButton removeTestFolder;
    private javax.swing.JTable sourceFolders;
    private javax.swing.JComboBox sourceLevel;
    private javax.swing.JTable testFolders;
    private javax.swing.JButton upFolder;
    private javax.swing.JButton upTestFolder;
    // End of variables declaration//GEN-END:variables

    public void setModel(ProjectModel model, AntProjectHelper projectHelper) {
        this.model = model;
        updateSourceLevelCombo(model.getSourceLevel());
        updateEncodingCombo();
        updateButtons();
        sourceFoldersModel.fireTableDataChanged();
        if (!isWizard) {
            projectFolder.setText(FileUtil.getFileDisplayName(projectHelper.getProjectDirectory()));
            contentFolder.setText(model.getBaseFolder().getAbsolutePath());
            FileObject fo = getAntScript(projectHelper, model.getEvaluator());
            if (fo != null) {
                buildScript.setText(FileUtil.getFileDisplayName(fo));
            }
        }
    }

    // XXX: this is copy of FreeformProjectGenerator.getAntScript
    private static FileObject getAntScript(AntProjectHelper helper, PropertyEvaluator ev) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        String antScript = ev.getProperty(ProjectConstants.PROP_ANT_SCRIPT);
        if (antScript != null) {
            File f= helper.resolveFile(antScript);
            if (!f.exists()) {
                return null;
            }
            FileObject fo = FileUtil.toFileObject(f);
            return fo;
        } else {
            FileObject fo = helper.getProjectDirectory().getFileObject("build.xml"); // NOI18N
            return fo;
        }
    }       

    private class SourcesModel extends AbstractTableModel {
        
        private boolean tests;
        
        public SourcesModel(boolean tests) {
            this.tests = tests;
        }
        
        public int getColumnCount() {
            return isWizard ? 1 : 2;
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0: return org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_Package");
                default: return org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_Label");
            }
        }
        
        public int getRowCount() {
            if (model == null)
                return 0;
            int count = 0;
            for (int i=0; i<model.getSourceFoldersCount(); i++) {
                JavaProjectGenerator.SourceFolder sf = model.getSourceFolder(i);
                boolean isTest = model.isTestSourceFolder(sf);
                if (tests && isTest) {
                    count++;
                }
                if (!tests && !isTest) {
                    count++;
                }
            }
            return count;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                String loc = getItem(rowIndex, tests).location;
                loc = getLocationDisplayName(model.getEvaluator(), model.getNBProjectFolder(), loc);
                return loc;
            } else {
                return getItem(rowIndex, tests).label;
            }
        }
        
        public boolean isCellEditable(int row, int column) {
            if (column == 1) {
                return true;
            } else {
                return false;
            }
        }
        
        public Class getColumnClass(int column) {
            return String.class;
        }
        
        public void setValueAt(Object val, int rowIndex, int columnIndex) {
            JavaProjectGenerator.SourceFolder sf = getItem(rowIndex, tests);
            sf.label = (String)val;
            if (sf.label.length() == 0) {
                sf.label = getDefaultLabel(sf.location, tests);
            }
        }
        
    }
    
    private class EncodingModel extends DefaultComboBoxModel {
        public EncodingModel() {
            Object selEnc = null;
            if (model.getEncoding() == null) {
                addElement(ProjectModel.NO_ENCODING);
                selEnc = ProjectModel.NO_ENCODING;
            }
            for (Charset cset : Charset.availableCharsets().values()) {
                if (cset.name().equals(model.getEncoding())) {
                    selEnc = cset;
                }
                addElement(cset);
            }
            if (isWizard) {
                setSelectedItem(FileEncodingQuery.getDefaultEncoding());
            } else {
                if (selEnc != null) {
                    setSelectedItem(selEnc);
                }
            }
        }
    }
    
    private static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public EncodingRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            setName("ComboBox.listRenderer"); // NOI18N
            String dispName = null;
            if (value instanceof Charset) {
                dispName = ((Charset) value).displayName();
            } else {
                dispName = value.toString();
            }
            setText(dispName);
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
        
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
        
    }
    
    private class ToolTipRenderer extends DefaultTableCellRenderer { 
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JComponent) {
                ((JComponent) c).setToolTipText ((String)value);
            }
            return c;
        }
        
    }
    
    private static class WarningDlg extends JPanel {

        public WarningDlg (Set invalidRoots) {            
            this.initGui (invalidRoots);
        }

        private void initGui (Set invalidRoots) {
            setLayout( new GridBagLayout ());                        
            JLabel label = new JLabel ();
            org.openide.awt.Mnemonics.setLocalizedText(label, NbBundle.getMessage(SourceFoldersPanel.class,"LBL_InvalidRoot"));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.insets = new Insets (12,0,6,0);
            ((GridBagLayout)this.getLayout()).setConstraints(label,c);
            this.add (label);            
            JList roots = new JList (invalidRoots.toArray());
            roots.setCellRenderer (new InvalidRootRenderer(true));
            JScrollPane p = new JScrollPane (roots);
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = c.weighty = 1.0;
            c.insets = new Insets (0,0,12,0);
            ((GridBagLayout)this.getLayout()).setConstraints(p,c);
            this.add (p);
            label.setLabelFor(roots);
            roots.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(SourceFoldersPanel.class,"AD_InvalidRoot"));
            JLabel label2 = new JLabel ();
            label2.setText (NbBundle.getMessage(SourceFoldersPanel.class,"MSG_InvalidRoot2"));
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.insets = new Insets (0,0,0,0);
            ((GridBagLayout)this.getLayout()).setConstraints(label2,c);
            this.add (label2);            
        }

        private static class InvalidRootRenderer extends DefaultListCellRenderer {

            private boolean projectConflict;

            public InvalidRootRenderer (boolean projectConflict) {
                this.projectConflict = projectConflict;
            }

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                File f = (File) value;
                String message = f.getAbsolutePath();
                if (projectConflict) {
                    Project p = FileOwnerQuery.getOwner(f.toURI());
                    if (p!=null) {
                        ProjectInformation pi = ProjectUtils.getInformation(p);
                        String projectName = pi.getDisplayName();
                        message = NbBundle.getMessage(SourceFoldersPanel.class,"TXT_RootOwnedByProject", message, projectName);
                    }
                }
                return super.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
            }
        }
    }
    
}
