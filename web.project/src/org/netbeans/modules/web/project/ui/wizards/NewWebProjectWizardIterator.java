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

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;

import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.modules.web.project.WebProjectGenerator;
import org.netbeans.modules.web.project.ui.FoldersListSettings;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick
 */
public class NewWebProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N

    /** Create a new wizard iterator. */
    public NewWebProjectWizardIterator() {}
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new PanelConfigureProject(),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName") //NOI18N
        };
    }
    
    public Set instantiate() throws IOException {
        Set resultSet = new HashSet();
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String sourceStructure = (String)wiz.getProperty(WizardProperties.SOURCE_STRUCTURE);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        String contextPath = (String) wiz.getProperty(WizardProperties.CONTEXT_PATH);
        
        AntProjectHelper h = WebProjectGenerator.createProject(dirF, name, sourceStructure, j2eeLevel, contextPath);
        try {
            FileObject webRoot = h.getProjectDirectory().getFileObject("web");//NOI18N
            FileObject indexJSPFo = getIndexJSPFO(webRoot, "index"); //NOI18N
            assert indexJSPFo != null : "webRoot: " + webRoot + ", defaultJSP: index";//NOI18N
            // Returning FileObject of main class, will be called its preferred action
            resultSet.add (indexJSPFo);
        } catch (Exception x) {
            //PENDING
        }
        
        FileObject dir = FileUtil.toFileObject(dirF);
        Project p = ProjectManager.getDefault().findProject(dir);

        Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        if(index != null) {
            FoldersListSettings.getDefault().setNewProjectCount(index.intValue());
        }
        wiz.putProperty(WizardProperties.NAME, null); // reset project name

        // save last project location
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);
        }

        resultSet.add(dir);
        
        // Returning set of FileObject of project diretory. 
        // Project will be open and set as main
        return resultSet;
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardProperties.PROJECT_DIR,null);
        this.wiz.putProperty(WizardProperties.NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_WizardStepsCount"), new String[] {(new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()}); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    // helper methods, finds indexJSP's FileObject
    private FileObject getIndexJSPFO(FileObject webRoot, String indexJSP) {
        // replace '.' with '/'
        indexJSP = indexJSP.replace ('.', '/'); // NOI18N
        
        // ignore unvalid mainClass ???
        
        return webRoot.getFileObject (indexJSP, "jsp"); // NOI18N
    }
}
