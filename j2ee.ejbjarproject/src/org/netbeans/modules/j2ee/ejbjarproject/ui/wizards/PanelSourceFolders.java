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

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.project.FileOwnerQuery;

//XXX There should be a way how to add nonexistent test dir

/**
 * Sets up name and location for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelSourceFolders extends SettingsPanel implements PropertyChangeListener {

    private Panel firer;
    private WizardDescriptor wizardDescriptor;

    /** Creates new form PanelSourceFolders */
    public PanelSourceFolders (Panel panel) {
        this.firer = panel;
        initComponents();
        this.setName(NbBundle.getMessage(PanelConfigureProjectVisual.class,"LAB_ConfigureSourceRoots"));
        this.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelSourceFolders.class,"TXT_JavaExtSourcesProjectLocation")); // NOI18N
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PanelSourceFolders.class,"AN_PanelSourceFolders"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelSourceFolders.class,"AD_PanelSourceFolders"));
        this.sourcePanel.addPropertyChangeListener (this);
        this.testsPanel.addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (FolderList.PROP_FILES.equals(evt.getPropertyName())) {
            this.dataChanged();
        }
    }

    private void dataChanged () {
        this.firer.fireChangeEvent();
    }


    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        File[] srcRoot = (File[]) settings.getProperty (WizardProperties.JAVA_ROOT);      //NOI18N
        if (srcRoot!=null) {
            ((FolderList)this.sourcePanel).setFiles(srcRoot);
        }
        File[] testRoot = (File[]) settings.getProperty (WizardProperties.TEST_ROOT);       //NOI18N
        if (testRoot != null) {
            ((FolderList)this.testsPanel).setFiles (testRoot);
        }
    }

    void store (WizardDescriptor settings) {
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        settings.putProperty (WizardProperties.JAVA_ROOT,sourceRoots);    //NOI18N
        settings.putProperty(WizardProperties.TEST_ROOT,testRoots);      //NOI18N
    }
    
    boolean valid (WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty (WizardProperties.PROJECT_DIR);  //NOI18N
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        String result = checkValidity (projectLocation, sourceRoots, testRoots);
        if (result == null) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage","");   //NOI18N
            return true;
        }
        else {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage",result);       //NOI18N
            return false;
        }
    }

    static String checkValidity (final File projectLocation, final File[] sources, final File[] tests ) {
        String ploc = projectLocation.getAbsolutePath ();
        if (sources.length ==0) {
            return "";  //NOI18N
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
            if (FileOwnerQuery.getOwner(sources[i].toURI())!=null) {
                return MessageFormat.format (NbBundle.getMessage(PanelSourceFolders.class,"TXT_AlreadyContainedRoot"),
                    new Object[] {sources[i].getAbsolutePath()});
            }
        }
        for (int i=0; i<tests.length; i++) {
            String tloc = tests[i].getAbsolutePath();
            if (ploc.equals(tloc) || ploc.startsWith(tloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder");
            }
            for (int j=0; j<sources.length; j++) {
                String sloc = sources[j].getAbsolutePath ();
                if (tloc.equals(sloc) || tloc.startsWith(sloc + File.separatorChar) || sloc.startsWith(tloc + File.separatorChar)) {
                    return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalTests");
                }
            }
            if (FileOwnerQuery.getOwner(tests[i].toURI())!=null) {
                return MessageFormat.format (NbBundle.getMessage(PanelSourceFolders.class,"TXT_AlreadyContainedRoot"),
                    new Object[] {tests[i].getAbsolutePath()});
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
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        sourcePanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_SourceRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddSourceRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddSourceFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveSourceFolder"));
        testsPanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_TestRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddTestRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddTestFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveTestFolder"));

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourcePanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(testsPanel, gridBagConstraints);

    }//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
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

}
