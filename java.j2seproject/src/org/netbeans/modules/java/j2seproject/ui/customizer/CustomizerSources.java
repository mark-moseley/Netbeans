/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Deve1loper of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.io.File;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.java.j2seproject.J2SEProject;

/**
 *
 * @author  Tomas Zezula
 */
public class CustomizerSources extends javax.swing.JPanel implements HelpCtx.Provider {
    
    public CustomizerSources( J2SEProjectProperties uiProperties ) {
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
        
        
        J2SESourceRootsUi.EditMediator emSR = J2SESourceRootsUi.registerEditMediator(
            (J2SEProject)uiProperties.getProject(),
            sourceRoots,
            addSourceRoot,
            removeSourceRoot, 
            upSourceRoot, 
            downSourceRoot,
            NbBundle.getMessage(CustomizerSources.class,"TXT_ProjectOwnedRoot"), 
            NbBundle.getMessage(CustomizerSources.class,"TXT_AlreadyInTests") );
        
        J2SESourceRootsUi.EditMediator emTSR = J2SESourceRootsUi.registerEditMediator(
            (J2SEProject)uiProperties.getProject(),
            testRoots,
            addTestRoot,
            removeTestRoot, 
            upTestRoot, 
            downTestRoot,
            NbBundle.getMessage(CustomizerSources.class,"TXT_ProjectOwnedRoot"), 
            NbBundle.getMessage(CustomizerSources.class,"TXT_AlreadyInSources"));
        
        emSR.setRelatedEditMediator( emTSR );
        emTSR.setRelatedEditMediator( emSR );
        this.sourceLevel.setModel(uiProperties.JAVAC_SOURCE_MODEL);        
        uiProperties.JAVAC_SOURCE_MODEL.addListDataListener(new ListDataListener () {
            public void intervalAdded(ListDataEvent e) {
                enableSourceLevel ();
            }

            public void intervalRemoved(ListDataEvent e) {
                enableSourceLevel ();
            }

            public void contentsChanged(ListDataEvent e) {
                enableSourceLevel ();
            }                                    
        });
        enableSourceLevel ();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (CustomizerSources.class);
    }
    
    private void enableSourceLevel () {
        this.sourceLevel.setEnabled(sourceLevel.getItemCount()>0);
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
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        sourceLevel = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_ProjectFolder").charAt(0));
        jLabel1.setLabelFor(projectLocation);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_ProjectFolder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel1, gridBagConstraints);

        projectLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(projectLocation, gridBagConstraints);
        projectLocation.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_projectLocation"));

        sourceRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_SourceRoots").charAt(0));
        jLabel2.setLabelFor(sourceRoots);
        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_SourceRoots"));
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
        sourceRoots.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_sourceRoots"));

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

        addSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_AddSourceRoot").charAt(0));
        addSourceRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_AddSourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        sourceRootsPanel.add(addSourceRoot, gridBagConstraints);
        addSourceRoot.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_addSourceRoot"));

        removeSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_RemoveSourceRoot").charAt(0));
        removeSourceRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_RemoveSourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(removeSourceRoot, gridBagConstraints);
        removeSourceRoot.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_removeSourceRoot"));

        upSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_UpSourceRoot").charAt(0));
        upSourceRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_UpSourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        sourceRootsPanel.add(upSourceRoot, gridBagConstraints);
        upSourceRoot.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_upSourceRoot"));

        downSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_DownSourceRoot").charAt(0));
        downSourceRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_DownSourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(downSourceRoot, gridBagConstraints);
        downSourceRoot.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_downSourceRoot"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourceRootsPanel, gridBagConstraints);

        testRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_TestRoots").charAt(0));
        jLabel3.setLabelFor(testRoots);
        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_TestRoots"));
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
        testRoots.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_testRoots"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        testRootsPanel.add(jScrollPane2, gridBagConstraints);

        addTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_AddTestRoot").charAt(0));
        addTestRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_AddTestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(addTestRoot, gridBagConstraints);
        addTestRoot.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_addTestRoot"));

        removeTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_RemoveTestRoot").charAt(0));
        removeTestRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_RemoveTestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        testRootsPanel.add(removeTestRoot, gridBagConstraints);
        removeTestRoot.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_removeTestRoot"));

        upTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_UpTestRoot").charAt(0));
        upTestRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_UpTestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(upTestRoot, gridBagConstraints);
        upTestRoot.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_upTestRoot"));

        downTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_DownTestRoot").charAt(0));
        downTestRoot.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_DownTestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        testRootsPanel.add(downTestRoot, gridBagConstraints);
        downTestRoot.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_CustomizerSources_downTestRoot"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(testRootsPanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel4.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("MNE_SourceLevel").charAt(0));
        jLabel4.setLabelFor(sourceLevel);
        jLabel4.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("TXT_SourceLevel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanel1.add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.75;
        jPanel1.add(sourceLevel, gridBagConstraints);
        sourceLevel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AN_SourceLevel"));
        sourceLevel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_SourceLevel"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRoot;
    private javax.swing.JButton addTestRoot;
    private javax.swing.JButton downSourceRoot;
    private javax.swing.JButton downTestRoot;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
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
    
}
