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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.queries.CollocationQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 * @author  Tomas Zezula
 */
public class CustomizerSources extends javax.swing.JPanel implements HelpCtx.Provider {
    private String originalEncoding;
    private EjbJarProjectProperties uiProperties;
    private File projectFld;
    
    public CustomizerSources( EjbJarProjectProperties uiProperties ) {
        initComponents();
        jScrollPane1.getViewport().setBackground( sourceRoots.getBackground() );
        jScrollPane2.getViewport().setBackground( testRoots.getBackground() );
        
        sourceRoots.setModel( uiProperties.SOURCE_ROOTS_MODEL );
        testRoots.setModel( uiProperties.TEST_ROOTS_MODEL );
        sourceRoots.getTableHeader().setReorderingAllowed(false);
        testRoots.getTableHeader().setReorderingAllowed(false);
        
        FileObject projectFolder = uiProperties.getProject().getProjectDirectory();
        File pf = FileUtil.toFile( projectFolder );
        this.projectLocation.setText( pf == null ? "" : pf.getPath() ); // NOI18N
        this.projectFld = pf;
        
        jTextFieldConfigFilesFolder.setDocument(uiProperties.META_INF_MODEL);
        
        EjbJarSourceRootsUi.EditMediator emSR = EjbJarSourceRootsUi.registerEditMediator(
                (EjbJarProject)uiProperties.getProject(),
                ((EjbJarProject)uiProperties.getProject()).getSourceRoots(),
                sourceRoots,
                addSourceRoot,
                removeSourceRoot,
                upSourceRoot,
                downSourceRoot);
        
        EjbJarSourceRootsUi.EditMediator emTSR = EjbJarSourceRootsUi.registerEditMediator(
                (EjbJarProject)uiProperties.getProject(),
                ((EjbJarProject)uiProperties.getProject()).getTestSourceRoots(),
                testRoots,
                addTestRoot,
                removeTestRoot,
                upTestRoot,
                downTestRoot);
        
        emSR.setRelatedEditMediator( emTSR );
        emTSR.setRelatedEditMediator( emSR );
        this.sourceLevel.setModel(uiProperties.JAVAC_SOURCE_MODEL);
        this.sourceLevel.setRenderer(uiProperties.JAVAC_SOURCE_RENDERER);
        uiProperties.JAVAC_SOURCE_MODEL.addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
                enableSourceLevel();
            }
            
            public void intervalRemoved(ListDataEvent e) {
                enableSourceLevel();
            }
            
            public void contentsChanged(ListDataEvent e) {
                enableSourceLevel();
            }
        });
        enableSourceLevel();
        this.originalEncoding = ((EjbJarProject)uiProperties.getProject()).evaluator().getProperty(EjbJarProjectProperties.SOURCE_ENCODING);
        if (this.originalEncoding == null) {
            this.originalEncoding = Charset.defaultCharset().name();
        }
        
        this.encoding.setModel(new EncodingModel(this.originalEncoding));
        this.encoding.setRenderer(new EncodingRenderer());
        
        
        this.encoding.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                handleEncodingChange();
            }
        });
        
        this.uiProperties = uiProperties;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerSources.class);
    }
    
    private void enableSourceLevel() {
        this.sourceLevel.setEnabled(sourceLevel.getItemCount()>0);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        jLabelConfigFilesFolder = new javax.swing.JLabel();
        jTextFieldConfigFilesFolder = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        sourceRootsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceRoots = new javax.swing.JTable();
        addSourceRoot = new javax.swing.JButton();
        removeSourceRoot = new javax.swing.JButton();
        upSourceRoot = new javax.swing.JButton();
        downSourceRoot = new javax.swing.JButton();
        testRootsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        testRoots = new javax.swing.JTable();
        addTestRoot = new javax.swing.JButton();
        removeTestRoot = new javax.swing.JButton();
        upTestRoot = new javax.swing.JButton();
        downTestRoot = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        sourceLevel = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_ProjectFolder").charAt(0));
        jLabel1.setLabelFor(projectLocation);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("CTL_ProjectFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jLabel1, gridBagConstraints);

        projectLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(projectLocation, gridBagConstraints);

        jLabelConfigFilesFolder.setDisplayedMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_ConfigFilesFolder").charAt(0));
        jLabelConfigFilesFolder.setLabelFor(jTextFieldConfigFilesFolder);
        jLabelConfigFilesFolder.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_ConfigFilesFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabelConfigFilesFolder, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jTextFieldConfigFilesFolder, gridBagConstraints);
        jTextFieldConfigFilesFolder.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "ACSD_CustomizerSources_ConfigFilesFolder")); // NOI18N

        jButtonBrowse.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_ConfigFilesFolderBrowse").charAt(0));
        jButtonBrowse.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Browse_JButton")); // NOI18N
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonBrowse, gridBagConstraints);
        jButtonBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "ACSD_CustomizerSources_ConfigFilesFolderBrowse")); // NOI18N

        sourceRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_SourceRoots").charAt(0));
        jLabel2.setLabelFor(sourceRoots);
        jLabel2.setText(bundle.getString("CTL_SourceRoots")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        sourceRootsPanel.add(jLabel2, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(450, 150));

        sourceRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(sourceRoots);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        sourceRootsPanel.add(jScrollPane1, gridBagConstraints);

        addSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_AddSourceRoot").charAt(0));
        addSourceRoot.setText(bundle.getString("CTL_AddSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        sourceRootsPanel.add(addSourceRoot, gridBagConstraints);

        removeSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_RemoveSourceRoot").charAt(0));
        removeSourceRoot.setText(bundle.getString("CTL_RemoveSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(removeSourceRoot, gridBagConstraints);

        upSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_UpSourceRoot").charAt(0));
        upSourceRoot.setText(bundle.getString("CTL_UpSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        sourceRootsPanel.add(upSourceRoot, gridBagConstraints);

        downSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_DownSourceRoot").charAt(0));
        downSourceRoot.setText(bundle.getString("CTL_DownSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(downSourceRoot, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourceRootsPanel, gridBagConstraints);

        testRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_TestRoots").charAt(0));
        jLabel3.setLabelFor(testRoots);
        jLabel3.setText(bundle.getString("CTL_TestRoots")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        testRootsPanel.add(jLabel3, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(450, 150));

        testRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(testRoots);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        testRootsPanel.add(jScrollPane2, gridBagConstraints);

        addTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_AddTestRoot").charAt(0));
        addTestRoot.setText(bundle.getString("CTL_AddTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(addTestRoot, gridBagConstraints);

        removeTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_RemoveTestRoot").charAt(0));
        removeTestRoot.setText(bundle.getString("CTL_RemoveTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        testRootsPanel.add(removeTestRoot, gridBagConstraints);

        upTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_UpTestRoot").charAt(0));
        upTestRoot.setText(bundle.getString("CTL_UpTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(upTestRoot, gridBagConstraints);

        downTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_DownTestRoot").charAt(0));
        downTestRoot.setText(bundle.getString("CTL_DownTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        testRootsPanel.add(downTestRoot, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(testRootsPanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel4.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "MNE_SourceLevel").charAt(0));
        jLabel4.setLabelFor(sourceLevel);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_SourceLevel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanel1.add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.75;
        jPanel1.add(sourceLevel, gridBagConstraints);
        sourceLevel.getAccessibleContext().setAccessibleName(bundle.getString("AN_SourceLevel")); // NOI18N
        sourceLevel.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceLevel")); // NOI18N

        jLabel5.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_Encoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        jPanel1.add(jLabel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel1.add(encoding, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File fileName = new File(jTextFieldConfigFilesFolder.getText());
        File configFiles = fileName.isAbsolute() ? fileName : new File(projectFld, fileName.getPath());
        if (configFiles.isAbsolute()) {
            chooser.setSelectedFile(configFiles);
        } else {
            chooser.setSelectedFile(projectFld);
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
            String newConfigFiles;
            if (CollocationQuery.areCollocated(projectFld, selected)) {
                newConfigFiles = PropertyUtils.relativizeFile(projectFld, selected);
            } else {
                newConfigFiles = selected.getPath();
            }
            jTextFieldConfigFilesFolder.setText(newConfigFiles);
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed
    
    private void handleEncodingChange() {
        Charset enc = (Charset) encoding.getSelectedItem();
        String encName;
        if (enc != null) {
            encName = enc.name();
        } else {
            encName = originalEncoding;
        }
        this.uiProperties.putAdditionalProperty(EjbJarProjectProperties.SOURCE_ENCODING, encName);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRoot;
    private javax.swing.JButton addTestRoot;
    private javax.swing.JButton downSourceRoot;
    private javax.swing.JButton downTestRoot;
    private javax.swing.JComboBox encoding;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelConfigFilesFolder;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextFieldConfigFilesFolder;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JButton removeSourceRoot;
    private javax.swing.JButton removeTestRoot;
    private javax.swing.JComboBox sourceLevel;
    private javax.swing.JTable sourceRoots;
    private javax.swing.JPanel sourceRootsPanel;
    private javax.swing.JTable testRoots;
    private javax.swing.JPanel testRootsPanel;
    private javax.swing.JButton upSourceRoot;
    private javax.swing.JButton upTestRoot;
    // End of variables declaration//GEN-END:variables
    
    private static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public EncodingRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof Charset;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((Charset) value).displayName());
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
    
    private static class EncodingModel extends DefaultComboBoxModel {
        
        public EncodingModel(String originalEncoding) {
            Charset defEnc = null;
            for (Charset c : Charset.availableCharsets().values()) {
                if (c.name().equals(originalEncoding)) {
                    defEnc = c;
                }
                addElement(c);
            }
            if (defEnc == null) {
                //Create artificial Charset to keep the original value
                //May happen when the project was set up on the platform
                //which supports more encodings
                try {
                    defEnc = new UnknownCharset(originalEncoding);
                    addElement(defEnc);
                } catch (java.nio.charset.IllegalCharsetNameException e) {
                    //The source.encoding property is completely broken
                    Logger.getLogger(this.getClass().getName()).info("IllegalCharsetName: " + originalEncoding);
                }
            }
            if (defEnc == null) {
                defEnc = Charset.defaultCharset();
            }
            setSelectedItem(defEnc);
        }
    }
    
    private static class UnknownCharset extends Charset {
        
        UnknownCharset(String name) {
            super(name, new String[0]);
        }
        
        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }
        
        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }
        
        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
    }
}
