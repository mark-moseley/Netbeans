/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.configbean.templates;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;


/*
 *
 * @author Peter Williams
 */
public final class SunDDWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private int index;
    
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new SunDDWizardPanel()
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }
    
    public Set instantiate() throws IOException {
        Set result = Collections.EMPTY_SET;
        SunDDWizardPanel wizardPanel = (SunDDWizardPanel) panels[0];
        
        File configDir = wizardPanel.getSelectedLocation();
        if(!configDir.exists()) {
            configDir.mkdirs();
        }
        FileObject configFolder = FileUtil.toFileObject(configDir);
        Project project = wizardPanel.getProject();
        String sunDDFileName = wizardPanel.getFileName();

        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
        if(configFolder != null) {
            String resource = "org-netbeans-modules-j2ee-sun-ddui/" + sunDDFileName; // NOI18N
            FileObject sunDDTemplate = Repository.getDefault().getDefaultFileSystem().findResource(resource);
            if(sunDDTemplate != null) {
                FileSystem fs = configFolder.getFileSystem();
                XmlFileCreator creator = new XmlFileCreator(sunDDTemplate, configFolder, 
                        sunDDTemplate.getName(), sunDDTemplate.getExt());
                fs.runAtomicAction(creator);
                FileObject sunDDFO = creator.getResult();
                if(sunDDFO != null) {
                    SunONEDeploymentConfiguration config = 
                            SunONEDeploymentConfiguration.getConfiguration(FileUtil.toFile(sunDDFO));
                    if(config != null) {
                        // Set version of target configuration file we just saved to maximum supported version.
                        config.setAppServerVersion(config.getMaxASVersion());
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                NbBundle.getMessage(SunDDWizardIterator.class,"ERR_NoDeploymentConfiguration"), // NOI18N
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                    result = Collections.singleton(creator.getResult());
                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(
                            NbBundle.getMessage(SunDDWizardIterator.class,"ERR_FileCreationFailed", sunDDFileName), // NOI18N
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            } else {
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        NbBundle.getMessage(SunDDWizardIterator.class,"ERR_TemplateNotFound", resource), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(SunDDWizardIterator.class,"ERR_LocationNotFound", configDir.getAbsolutePath()), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        return result;
    }
    
    public static class XmlFileCreator implements FileSystem.AtomicAction {
        
        private final FileObject source;
        private final FileObject destFolder;
        private final String name;
        private final String ext;
        private FileObject result;
        
        public XmlFileCreator(final FileObject source, final FileObject destFolder, final String name, final String ext) {
            this.source = source;
            this.destFolder = destFolder;
            this.name = name;
            this.ext = ext;
            this.result = null;
        }
        
        public void run() throws IOException {
            result = FileUtil.copyFile(source, destFolder, name, ext);
        }
        
        public FileObject getResult() {
            return result;
        }
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
     */
    
    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData"); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        
        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }
        
        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }
}
