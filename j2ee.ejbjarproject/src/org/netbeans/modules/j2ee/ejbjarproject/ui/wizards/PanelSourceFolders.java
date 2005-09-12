/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.ui.support.ProjectChooser;

//XXX There should be a way how to add nonexistent test dir

/**
 * Sets up name and location for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelSourceFolders extends SettingsPanel implements PropertyChangeListener {

    private Panel firer;
    private WizardDescriptor wizardDescriptor;
    private File oldProjectLocation;
    
    private DocumentListener configFilesDocumentListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
            configFilesChanged();
        }
        
        public void insertUpdate(DocumentEvent e) {
            configFilesChanged();
        }
        
        public void removeUpdate(DocumentEvent e) {
            configFilesChanged();
        }
    };

    /** Creates new form PanelSourceFolders */
    public PanelSourceFolders (Panel panel) {
        this.firer = panel;
        initComponents();
        this.setName(NbBundle.getMessage(PanelConfigureProjectVisual.class,"LAB_ConfigureSourceRoots"));
        this.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelSourceFolders.class,"TXT_ImportEJBModule")); // NOI18N
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PanelSourceFolders.class,"AN_PanelSourceFolders"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelSourceFolders.class,"AD_PanelSourceFolders"));
        this.sourcePanel.addPropertyChangeListener (this);
        this.testsPanel.addPropertyChangeListener(this);
        ((FolderList)this.sourcePanel).setRelatedFolderList((FolderList)this.testsPanel);
        ((FolderList)this.testsPanel).setRelatedFolderList((FolderList)this.sourcePanel);        
        this.jTextFieldConfigFiles.getDocument().addDocumentListener(configFilesDocumentListener);
    }

    public void initValues(FileObject fo) {
        ((FolderList) this.sourcePanel).setLastUsedDir(FileUtil.toFile(fo));
        ((FolderList) this.testsPanel).setLastUsedDir(FileUtil.toFile(fo));
        
        String configFiles = FileUtil.toFile(FileSearchUtility.guessConfigFilesPath(fo)).getAbsolutePath();
        jTextFieldConfigFiles.setText(configFiles);
        FileObject librariesFO = FileSearchUtility.guessLibrariesFolder(fo);
        if (librariesFO != null) {
            String libraries = FileUtil.toFile(librariesFO).getAbsolutePath();
            jTextFieldLibraries.setText(libraries);
        }
    }

    
    public void propertyChange(PropertyChangeEvent evt) {
        if (FolderList.PROP_FILES.equals(evt.getPropertyName())) {
            this.dataChanged();
        } else if (FolderList.PROP_LAST_USED_DIR.equals (evt.getPropertyName())) {
            if (evt.getSource() == this.sourcePanel) {                
                ((FolderList)this.testsPanel).setLastUsedDir 
                        ((File)evt.getNewValue());
            }
            else if (evt.getSource() == this.testsPanel) {
                ((FolderList)this.sourcePanel).setLastUsedDir 
                        ((File)evt.getNewValue());
            }
        }
    }

    private void dataChanged () {
        this.firer.fireChangeEvent();
    }


    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        
        // #56706: only reinitialize the locations on the panel if the user changed the project location
        File projectLocation = (File) settings.getProperty(WizardProperties.SOURCE_ROOT);
        ((FolderList)this.sourcePanel).setProjectFolder(projectLocation);
        ((FolderList)this.testsPanel).setProjectFolder(projectLocation);
        if (!projectLocation.equals(oldProjectLocation)) {
            File[] srcRoot = (File[]) settings.getProperty (WizardProperties.JAVA_ROOT);      //NOI18N
            if (srcRoot!=null) {
                ((FolderList)this.sourcePanel).setFiles(srcRoot);
            }
            File[] testRoot = (File[]) settings.getProperty (WizardProperties.TEST_ROOT);       //NOI18N
            if (testRoot != null) {
                ((FolderList)this.testsPanel).setFiles (testRoot);
            }
            initValues(FileUtil.toFileObject(projectLocation));
            oldProjectLocation = projectLocation;
        }
    }

    void store (WizardDescriptor settings) {
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        settings.putProperty (WizardProperties.JAVA_ROOT,sourceRoots);    //NOI18N
        settings.putProperty(WizardProperties.TEST_ROOT,testRoots);      //NOI18N
        settings.putProperty(WizardProperties.CONFIG_FILES_FOLDER, new File(jTextFieldConfigFiles.getText().trim()));
        String libPath = jTextFieldLibraries.getText().trim();
        if (libPath != null && !libPath.equals("")) {
            settings.putProperty(WizardProperties.LIB_FOLDER, new File(libPath));
        }
    }
    
    boolean valid (WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty (WizardProperties.PROJECT_DIR);  //NOI18N
        File configFilesLocation = jTextFieldConfigFiles.getText().trim().length() > 0 ? getConfigFiles() : null;
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        String result = checkValidity (projectLocation, configFilesLocation, sourceRoots, testRoots);
        if (result == null) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage"," ");   //NOI18N
            return true;
        }
        else {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage",result);       //NOI18N
            return false;
        }
    }

    static String checkValidity (final File projectLocation, final File configFilesLocation, final File[] sources, final File[] tests ) {
        String ploc = projectLocation.getAbsolutePath ();
        if (configFilesLocation != null) {
            FileObject fo = FileUtil.toFileObject(configFilesLocation);
            FileObject ejbJarXml = null;
            if (fo != null) {
                ejbJarXml = fo.getFileObject("ejb-jar.xml"); // NOI18N
            }
            if (ejbJarXml == null || !ejbJarXml.isData()) 
                return NbBundle.getMessage(PanelSourceFolders.class, "MSG_NoEjbJarXml");
        }
        if (sources.length ==0) {
            return " ";  //NOI18N
        }
        for (int i=0; i<sources.length;i++) {
            if (!sources[i].isDirectory() || !sources[i].canRead()) {
                return MessageFormat.format(NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalSources"),
                        new Object[] {sources[i].getAbsolutePath()});
            }
            String sloc = sources[i].getAbsolutePath ();
            if (ploc.equals (sloc) || ploc.startsWith (sloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder");
            }
        }
        for (int i=0; i<tests.length; i++) {
            if (!tests[i].isDirectory() || !tests[i].canRead()) {
                return MessageFormat.format(NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalTests"),
                        new Object[] {sources[i].getAbsolutePath()});
            }            
            String tloc = tests[i].getAbsolutePath();
            if (ploc.equals(tloc) || ploc.startsWith(tloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder");
            }
        }
        return null;
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // sources root
        searchClassFiles (((FolderList)this.sourcePanel).getFiles());
        // test root, not asked in issue 48198
        //searchClassFiles (FileUtil.toFileObject (FileUtil.normalizeFile(new File (tests.getText ()))));
    }
    
    private void searchClassFiles (File[] folders) throws WizardValidationException {
        boolean found = false;
        for (int i=0; i<folders.length; i++) {
            FileObject folder = FileUtil.toFileObject(folders[i]);
            if (folder != null) {
                Enumeration en = folder.getData (true);
                while (!found && en.hasMoreElements ()) {
                    Object obj = en.nextElement ();
                    assert obj instanceof FileObject : "Instance of FileObject: " + obj; // NOI18N
                    FileObject fo = (FileObject) obj;
                    found = "class".equals (fo.getExt ()); // NOI18N
                }
            }
        }
        if (found) {
            Object DELETE_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_DeleteOption"); // NOI18N
            Object KEEP_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_KeepOption"); // NOI18N
            Object CANCEL_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_CancelOption"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor (
                    NbBundle.getMessage (PanelSourceFolders.class, "MSG_FoundClassFiles"), // NOI18N
                    NbBundle.getMessage (PanelSourceFolders.class, "MSG_FoundClassFiles_Title"), // NOI18N
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] {DELETE_OPTION, KEEP_OPTION, CANCEL_OPTION},
                    null
                    );
            Object result = DialogDisplayer.getDefault().notify(desc);
            if (DELETE_OPTION.equals (result)) {
                deleteClassFiles (folders);
            } else if (!KEEP_OPTION.equals (result)) {
                // cancel, back to wizard
                throw new WizardValidationException (this.sourcePanel, "", ""); // NOI18N
            }
        }
    }
    
    private void deleteClassFiles (File[] folders) {
        for (int i=0; i<folders.length; i++) {
            FileObject folder = FileUtil.toFileObject(folders[i]);
            Enumeration en = folder.getData (true);
            while (en.hasMoreElements ()) {
                Object obj = en.nextElement ();
                assert obj instanceof FileObject : "Instance of FileObject: " + obj;
                FileObject fo = (FileObject) obj;
                try {
                    if ("class".equals (fo.getExt ())) { // NOI18N
                        fo.delete ();
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault ().notify (ioe);
                }
            }
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

        jLabel3 = new javax.swing.JLabel();
        sourcePanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_SourceRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddSourceRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddSourceFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveSourceFolder"));
        testsPanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_TestRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddTestRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddTestFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveTestFolder"));
        jLabel1 = new javax.swing.JLabel();
        jTextFieldConfigFiles = new javax.swing.JTextField();
        jButtonConfigFilesLocation = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldLibraries = new javax.swing.JTextField();
        jButtonLibraries = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_PanelSourceFolders"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_PanelSourceFolders"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_SourceDirectoriesLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSN_jLabel3"));
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSD_jLabel3"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourcePanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(testsPanel, gridBagConstraints);

        jLabel1.setLabelFor(jTextFieldConfigFiles);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_IW_ConfigFilesFolder_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jTextFieldConfigFiles, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonConfigFilesLocation, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_NWP1_BrowseLocation_Button"));
        jButtonConfigFilesLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConfigFilesLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonConfigFilesLocation, gridBagConstraints);

        jLabel2.setLabelFor(jTextFieldLibraries);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_IW_LibrariesLocation_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jTextFieldLibraries, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonLibraries, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_NWP1_BrowseLocation_Button"));
        jButtonLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLibrariesActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonLibraries, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jButtonLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLibrariesActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (jTextFieldLibraries.getText().length() > 0 && getLibraries().exists()) {
            chooser.setSelectedFile(getLibraries());
        } else {
            chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File configFilesDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextFieldLibraries.setText(configFilesDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonLibrariesActionPerformed

    private void jButtonConfigFilesLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfigFilesLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (jTextFieldConfigFiles.getText().length() > 0 && getConfigFiles().exists()) {
            chooser.setSelectedFile(getConfigFiles());
        } else {
            chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File configFilesDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextFieldConfigFiles.setText(configFilesDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonConfigFilesLocationActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConfigFilesLocation;
    private javax.swing.JButton jButtonLibraries;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextFieldConfigFiles;
    private javax.swing.JTextField jTextFieldLibraries;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JPanel testsPanel;
    // End of variables declaration//GEN-END:variables

    
    static class Panel implements WizardDescriptor.ValidatingPanel {
        
        private ArrayList listeners;        
        private PanelSourceFolders component;
        private WizardDescriptor settings;
        
        public synchronized void removeChangeListener(ChangeListener l) {
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(l);
        }

        public void addChangeListener(ChangeListener l) {
            if (this.listeners == null) {
                this.listeners = new ArrayList ();
            }
            this.listeners.add (l);
        }

        public void readSettings(Object settings) {
            this.settings = (WizardDescriptor) settings;
            this.component.read (this.settings);
            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = component.getClientProperty ("NewProjectWizard_Title"); // NOI18N
            if (substitute != null) {
                this.settings.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
            }
        }

        public void storeSettings(Object settings) {
            this.component.store (this.settings);
        }
        
        public void validate() throws WizardValidationException {
            this.component.validate(this.settings);
        }
                
        public boolean isValid() {
            return this.component.valid (this.settings);
        }

        public synchronized java.awt.Component getComponent() {
            if (this.component == null) {
                this.component = new PanelSourceFolders (this);
            }
            return this.component;
        }

        public HelpCtx getHelp() {
            return new HelpCtx (PanelSourceFolders.class);
        }        
        
        private void fireChangeEvent () {
           Iterator it = null;
           synchronized (this) {
               if (this.listeners == null) {
                   return;
               }
               it = ((ArrayList)this.listeners.clone()).iterator();
           }
           ChangeEvent event = new ChangeEvent (this);
           while (it.hasNext()) {
               ((ChangeListener)it.next()).stateChanged(event);
           }
        }
                
    }

    private File getAsFile(String filename) {
        return FileUtil.normalizeFile(new File(filename));
    }

    public File getConfigFiles() {
        return getAsFile(jTextFieldConfigFiles.getText());
    }

    public File getLibraries() {
        return getAsFile(jTextFieldLibraries.getText());
    }
    
    private void configFilesChanged() {
        dataChanged();
    }
}
