/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * @author  phrebejk
 */
public class CustomizerRun extends JPanel implements J2SECustomizer.Panel, HelpCtx.Provider {
    
    // Helper for storing properties
    private J2SEProjectProperties j2seProperties;
    private VisualPropertySupport vps;
    private VisualClasspathSupport vcs;
    private VisualMainClassSupport vms;
    
    /** Creates new form CustomizerCompile */
    public CustomizerRun( J2SEProjectProperties j2seProperties ) {
        initComponents();                
        this.j2seProperties = j2seProperties;
        vps = new VisualPropertySupport( j2seProperties );
        vcs = new VisualClasspathSupport(
            j2seProperties.getProject(),
            jListClasspath,
            jButtonAddJar,
            jButtonAddLibrary,
            jButtonAddArtifact,
            jButtonEdit,
            jButtonRemove,
            jButtonMoveUp,
            jButtonMoveDown );
        Project p = j2seProperties.getProject ();

        // better way to obtain the source directory
        // j2seProperties.get(SRC_DIR) returns non-evaluated
        // property for a project with existing sources
        // which cannot be used for construct sources root
        FileObject sourceRoot = J2SEProjectUtil.getProjectSourceDirectory (p);
        vms = new VisualMainClassSupport (jTextFieldMainClass, jButtonMainClass, sourceRoot);
    }
    
    
    public void initValues() {
        
        vps.register (vms, J2SEProjectProperties.MAIN_CLASS);
        vps.register( jTextFieldArgs, J2SEProjectProperties.APPLICATION_ARGS );
        vps.register( vcs, J2SEProjectProperties.RUN_CLASSPATH );
        vps.register( jTextVMOptions, J2SEProjectProperties.RUN_JVM_ARGS);
        vps.register( jTextWorkingDirectory, J2SEProjectProperties.RUN_WORK_DIR);
   
        jButtonMainClass.setVisible( true );
        
        // XXX Probably remove the button
        jButtonEdit.setVisible( false );
    } 
        
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerRun.class );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelMainClass = new javax.swing.JLabel();
        jTextFieldMainClass = new javax.swing.JTextField();
        jButtonMainClass = new javax.swing.JButton();
        jLabelArgs = new javax.swing.JLabel();
        jTextFieldArgs = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabelRunClasspath = new javax.swing.JLabel();
        jScrollClasspath = new javax.swing.JScrollPane();
        jListClasspath = new javax.swing.JList();
        jButtonAddArtifact = new javax.swing.JButton();
        jButtonAddLibrary = new javax.swing.JButton();
        jButtonAddJar = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        jButtonMoveUp = new javax.swing.JButton();
        jButtonMoveDown = new javax.swing.JButton();
        jLabelWorkingDirectory = new javax.swing.JLabel();
        jTextWorkingDirectory = new javax.swing.JTextField();
        jButtonWorkingDirectoryBrowse = new javax.swing.JButton();
        jLabelVMOptions = new javax.swing.JLabel();
        jTextVMOptions = new javax.swing.JTextField();
        jLabelVMOptionsExample = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EtchedBorder());
        jLabelMainClass.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 0);
        add(jLabelMainClass, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 0);
        add(jTextFieldMainClass, gridBagConstraints);
        jTextFieldMainClass.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jTextFieldMainClass"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 5, 12);
        add(jButtonMainClass, gridBagConstraints);
        jButtonMainClass.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jButtonMainClass"));

        jLabelArgs.setLabelFor(jTextFieldArgs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jLabelArgs, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jTextFieldArgs, gridBagConstraints);
        jTextFieldArgs.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jTextFieldArgs"));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabelRunClasspath, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_RunClasspath_JLabel"));
        jLabelRunClasspath.setLabelFor(jScrollClasspath);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 2, 0);
        jPanel1.add(jLabelRunClasspath, gridBagConstraints);

        jScrollClasspath.setViewportView(jListClasspath);
        jListClasspath.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jListClasspath"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        jPanel1.add(jScrollClasspath, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifact, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeCompile_Classpath_AddArtifact_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        jPanel1.add(jButtonAddArtifact, gridBagConstraints);
        jButtonAddArtifact.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jButtonAddArtifact"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibrary, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeCompile_Classpath_AddLibrary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        jPanel1.add(jButtonAddLibrary, gridBagConstraints);
        jButtonAddLibrary.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jButtonAddLibrary"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJar, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeCompile_Classpath_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        jPanel1.add(jButtonAddJar, gridBagConstraints);
        jButtonAddJar.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jButtonAddJar"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEdit, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeCompile_Classpath_Edit_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        jPanel1.add(jButtonEdit, gridBagConstraints);
        jButtonEdit.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jButtonEdit"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemove, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeCompile_Classpath_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        jPanel1.add(jButtonRemove, gridBagConstraints);
        jButtonRemove.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jButtonRemove"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUp, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeCompile_Classpath_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        jPanel1.add(jButtonMoveUp, gridBagConstraints);
        jButtonMoveUp.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jButtonMoveUp"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDown, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeCompile_Classpath_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        jPanel1.add(jButtonMoveDown, gridBagConstraints);
        jButtonMoveDown.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("AD_jButtonMoveDown"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        jLabelWorkingDirectory.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelWorkingDirectory, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jLabelWorkingDirectory, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jTextWorkingDirectory, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonWorkingDirectoryBrowse, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory_Browse"));
        jButtonWorkingDirectoryBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWorkingDirectoryBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 12);
        add(jButtonWorkingDirectoryBrowse, gridBagConstraints);

        jLabelVMOptions.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptions, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jLabelVMOptions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jTextVMOptions, gridBagConstraints);

        jLabelVMOptionsExample.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jLabelVMOptionsExample, gridBagConstraints);

    }//GEN-END:initComponents

    private void jButtonWorkingDirectoryBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWorkingDirectoryBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = jTextWorkingDirectory.getText();
        if (workDir.equals("")) {
            workDir = FileUtil.toFile(j2seProperties.getProject().getProjectDirectory()).toString();
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory_Browse_Title"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextWorkingDirectory.setText(file.toString());
        }
    }//GEN-LAST:event_jButtonWorkingDirectoryBrowseActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddArtifact;
    private javax.swing.JButton jButtonAddJar;
    private javax.swing.JButton jButtonAddLibrary;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonMainClass;
    private javax.swing.JButton jButtonMoveDown;
    private javax.swing.JButton jButtonMoveUp;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JButton jButtonWorkingDirectoryBrowse;
    private javax.swing.JLabel jLabelArgs;
    private javax.swing.JLabel jLabelMainClass;
    private javax.swing.JLabel jLabelRunClasspath;
    private javax.swing.JLabel jLabelVMOptions;
    private javax.swing.JLabel jLabelVMOptionsExample;
    private javax.swing.JLabel jLabelWorkingDirectory;
    private javax.swing.JList jListClasspath;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollClasspath;
    private javax.swing.JTextField jTextFieldArgs;
    private javax.swing.JTextField jTextFieldMainClass;
    private javax.swing.JTextField jTextVMOptions;
    private javax.swing.JTextField jTextWorkingDirectory;
    // End of variables declaration//GEN-END:variables
    
    
    
}
