/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.project.WebProject;

/**
 *
 * @author  tom
 */
public class CustomizerSources extends javax.swing.JPanel implements WebCustomizer.Panel, HelpCtx.Provider {
    
    private WebProjectProperties webProperties;
    private VisualPropertySupport vps;
    private VisualSourceRootsSupport sources;
    private VisualSourceRootsSupport tests;


    /** Creates new form CustomizerSources */
    public CustomizerSources (WebProjectProperties webProperties) {
        this.webProperties = webProperties;
        initComponents();
        this.vps = new VisualPropertySupport( webProperties );
        this.sources = new VisualSourceRootsSupport((WebProject)webProperties.getProject(),this.sourceRoots,this.addSourceRoot,
                this.removeSourceRoot, this.upSourceRoot, this.downSourceRoot,
                NbBundle.getMessage(CustomizerSources.class,"TXT_ProjectOwnedRoot"), NbBundle.getMessage(CustomizerSources.class,"TXT_AlreadyInTests"));
        this.tests = new VisualSourceRootsSupport((WebProject)webProperties.getProject(),this.testRoots,this.addTestRoot,
                this.removeTestRoot, this.upTestRoot, this.downTestRoot,
                NbBundle.getMessage(CustomizerSources.class,"TXT_ProjectOwnedRoot"), NbBundle.getMessage(CustomizerSources.class,"TXT_AlreadyInSources"));
        this.sources.setRelatedVisualSourceRootsSupport(this.tests);
        this.tests.setRelatedVisualSourceRootsSupport(this.sources);
    }

    public void initValues() {
        FileObject projectFolder = webProperties.getProject().getProjectDirectory();
        File pf = FileUtil.toFile( projectFolder );
        this.projectLocation.setText( pf == null ? "" : pf.getPath() ); // NOI18N
        vps.register(this.sources, WebProjectProperties.SOURCE_ROOTS );
        vps.register(this.tests, WebProjectProperties.TEST_ROOTS );
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (CustomizerSources.class);
    }        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
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

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EtchedBorder());
        jLabel1.setLabelFor(projectLocation);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_ProjectFolder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jLabel1, gridBagConstraints);

        projectLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(projectLocation, gridBagConstraints);

        sourceRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setLabelFor(sourceRoots);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_SourceRoots"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        sourceRootsPanel.add(jLabel2, gridBagConstraints);

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
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        sourceRootsPanel.add(jScrollPane1, gridBagConstraints);

        addSourceRoot.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_AddSourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        sourceRootsPanel.add(addSourceRoot, gridBagConstraints);

        removeSourceRoot.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_RemoveSourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(removeSourceRoot, gridBagConstraints);

        upSourceRoot.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_UpSourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        sourceRootsPanel.add(upSourceRoot, gridBagConstraints);

        downSourceRoot.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_DownSourceRoot"));
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
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(sourceRootsPanel, gridBagConstraints);

        testRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(testRoots);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_TestRoots"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        testRootsPanel.add(jLabel3, gridBagConstraints);

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
        gridBagConstraints.weighty = 1.0;
        testRootsPanel.add(jScrollPane2, gridBagConstraints);

        addTestRoot.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_AddTestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(addTestRoot, gridBagConstraints);

        removeTestRoot.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_RemoveTestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        testRootsPanel.add(removeTestRoot, gridBagConstraints);

        upTestRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("CTL_UpTestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(upTestRoot, gridBagConstraints);

        downTestRoot.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_DownTestRoot"));
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(testRootsPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRoot;
    private javax.swing.JButton addTestRoot;
    private javax.swing.JButton downSourceRoot;
    private javax.swing.JButton downTestRoot;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JButton removeSourceRoot;
    private javax.swing.JButton removeTestRoot;
    private javax.swing.JTable sourceRoots;
    private javax.swing.JPanel sourceRootsPanel;
    private javax.swing.JTable testRoots;
    private javax.swing.JPanel testRootsPanel;
    private javax.swing.JButton upSourceRoot;
    private javax.swing.JButton upTestRoot;
    // End of variables declaration//GEN-END:variables
    
}
