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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.java.j2seproject.ui.FoldersListSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.text.MessageFormat;
import org.netbeans.spi.project.support.ant.PropertyUtils;

import org.openide.filesystems.FileUtil;

/**
 *
 * @author  tom
 */
public class PanelSourceFolders extends SettingsPanel {

    private PanelConfigureProject firer;
    private WizardDescriptor wizardDescriptor;

    /** Creates new form PanelSourceFolders */
    public PanelSourceFolders (PanelConfigureProject panel) {
        this.firer = panel;
        initComponents();
        DocumentListener pl = new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                dataChanged ();
            }

            public void insertUpdate(DocumentEvent e) {
                dataChanged ();
            }

            public void removeUpdate(DocumentEvent e) {
                dataChanged ();
            }
        };
        this.sources.getDocument().addDocumentListener(pl);
        this.tests.getDocument().addDocumentListener(pl);
        pl = new DocumentListener (){
            public void changedUpdate(DocumentEvent e) {
                calculateProjectFolder ();
                dataChanged ();
            }

            public void insertUpdate(DocumentEvent e) {
                calculateProjectFolder ();
                dataChanged ();
            }

            public void removeUpdate(DocumentEvent e) {
                calculateProjectFolder ();
                dataChanged ();
            }
        };
        this.projectName.getDocument().addDocumentListener (pl);        
        this.projectLocation.getDocument().addDocumentListener(pl);
    }

    private void calculateProjectFolder () {
        File f = new File (this.projectLocation.getText());
        this.projectFolder.setText (f.getAbsolutePath() + File.separator + 
        this.projectName.getText());
    }

    private void dataChanged () {
        this.firer.fireChangeEvent();
    }


    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        String path = null;
        File srcRoot = (File) settings.getProperty ("sourceRoot");      //NOI18N
        if (srcRoot!=null) {
            path = srcRoot.getAbsolutePath();
        }
        else {
            path = FoldersListSettings.getDefault().getLastExternalSourceRoot();
        }
        if (path!=null) {
            this.sources.setText (path);
        }
        File testRoot = (File) settings.getProperty ("testRoot");       //NOI18N
        if (testRoot != null) {
            path = testRoot.getAbsolutePath();
        }
        else {
            path = FoldersListSettings.getDefault().getLastExternalTestRoot();
        }
        if (path!=null) {
            this.tests.setText (path);
        }
        File projectLocation = (File) settings.getProperty ("projdir");  //NOI18N
        if (projectLocation == null) {
            projectLocation = ProjectChooser.getProjectsFolder();
        }
        else {
            projectLocation = projectLocation.getParentFile();
            //Workaround of "bad" (web) template iterators which set projdir to illegal value
            if (projectLocation == null) {
                projectLocation = ProjectChooser.getProjectsFolder();
            }
        }
        this.projectLocation.setText (projectLocation.getAbsolutePath());
        String projectName = (String) settings.getProperty ("displayName"); //NOI18N
        if (projectName == null) {
            int index = FoldersListSettings.getDefault().getNewProjectCount();
            String formater = NbBundle.getMessage(PanelSourceFolders.class,"TXT_JavaProject");
            File file;
            do {
                index++;                            
                projectName = MessageFormat.format (formater, new Object[]{new Integer (index)});                
                file = new File (projectLocation, projectName);                
            } while (file.exists());                                
            settings.putProperty (NewJ2SEProjectWizardIterator.PROP_NAME_INDEX, new Integer(index));                        
        }
        this.projectName.setText (projectName);                
        this.sources.selectAll ();
    }

    void store (WizardDescriptor settings) {
        File srcRoot = null;
        File testRoot = null;
        String srcPath = this.sources.getText();
        if (srcPath.length() > 0) {
            srcRoot = FileUtil.normalizeFile(new File(srcPath));
            FoldersListSettings.getDefault().setLastExternalSourceRoot (srcPath);
        }
        String testPath = this.tests.getText();
        if (testPath.length()>0) {
            testRoot = FileUtil.normalizeFile(new File(testPath));
            FoldersListSettings.getDefault().setLastExternalTestRoot (testPath);
        }
        settings.putProperty ("sourceRoot",srcRoot);    //NOI18N
        settings.putProperty("testRoot",testRoot);      //NOI18N
        settings.putProperty ("name",this.projectName.getText()); // NOI18N
        settings.putProperty ("projdir",new File (this.projectFolder.getText())); // NOI18N
        File projectsDir = new File(this.projectLocation.getText());
        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder (projectsDir);
        }
    }
    
    boolean valid (WizardDescriptor settings) {
        if ( projectName.getText().length() == 0 ) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectName"));
            return false; // Display name not specified
        }
        
        File destFolder = new File( projectFolder.getText() );
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(PanelSourceFolders.class,"MSG_ProjectFolderExists"));
            return false;
        }                        
        String fileName = sources.getText();
        if (fileName.length()==0) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", "");  //NOI18N
            return false;
        }
        File f = new File (fileName);        
        if (!f.isDirectory() || !f.canRead()) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalSources"));
            return false;
        }
        fileName = tests.getText ();
        if (fileName.length()>0) {
            File tf = new File (fileName);
            if (f.equals(tf)) {
                wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalTests"));
                return false;
            }
        }
        wizardDescriptor.putProperty( "WizardPanel_errorMessage", "");  //NOI18N
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        sources = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        tests = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_PanelSourceFolders"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_PanelSourceFolders"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_SourceDirectoriesLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSN_jLabel3"));
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSD_jLabel3"));

        jLabel1.setLabelFor(sources);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "CTL_SourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_jLabel1"));
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_jLabel1"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(sources, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_BrowseLocation_Button1"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseSourceRoot(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_browseButton"));
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_browseButton"));

        jLabel2.setLabelFor(tests);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "CTL_TestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_jLabel2"));
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_jLabel2"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(tests, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_BrowseLocation_Button2"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseTestRoot(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(jButton2, gridBagConstraints);
        jButton2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_browseButton"));
        jButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_browseButton"));

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(jPanel2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_ProjectNameAndLocationLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_jLabel4"));
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_jLabel4"));

        jLabel5.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_ProjectName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel2.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_projectNameLabel"));
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_projectNameLabel"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(projectName, gridBagConstraints);

        jLabel6.setLabelFor(projectLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_ProjectLocation_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel2.add(jLabel6, gridBagConstraints);
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_projectLocationLabel"));
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_projectLocationLabel"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(projectLocation, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_BrowseLocation_Button3"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectLocation(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(jButton3, gridBagConstraints);
        jButton3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_browseButton"));
        jButton3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_browseButton"));

        jLabel7.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_CreatedProjectFolder_Lablel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabel7, gridBagConstraints);
        jLabel7.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_createdFolderLabel"));
        jLabel7.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_projectNameLabel"));

        projectFolder.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        jPanel2.add(projectFolder, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(jPanel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
        jPanel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_jPanel1"));
        jPanel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_jPanel1"));

    }//GEN-END:initComponents

    private void browseProjectLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocation
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(PanelSourceFolders.class,"LBL_NWP1_SelectProjectLocation"));
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String path = this.projectLocation.getText();
        if (path.length() > 0) {
            File f = new File (path);
            if (f.exists()) {
                chooser.setSelectedFile (f);
            }
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                this.projectLocation.setText (file.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseProjectLocation

    private void browseTestRoot(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseTestRoot
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(PanelSourceFolders.class,"CTL_SelectTestsFolder"));
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String path = this.tests.getText();
        if (path.length() > 0) {
            File f = new File (path);        
            if (f.exists()) {
                chooser.setSelectedFile (f);
            }
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                this.tests.setText (file.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseTestRoot


    private void browseSourceRoot(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseSourceRoot
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(PanelSourceFolders.class,"CTL_SelectSourceFolder"));
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String path = this.sources.getText();
        if (path.length() > 0) {
            File f = new File (path);
            if (f.exists()) {
                chooser.setSelectedFile (f);
            }
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                this.sources.setText (file.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseSourceRoot
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JTextField projectName;
    private javax.swing.JTextField sources;
    private javax.swing.JTextField tests;
    // End of variables declaration//GEN-END:variables


}
