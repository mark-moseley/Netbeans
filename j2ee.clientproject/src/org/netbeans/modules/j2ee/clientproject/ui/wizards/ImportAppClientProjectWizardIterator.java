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

package org.netbeans.modules.j2ee.clientproject.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.Utils;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.ui.FoldersListSettings;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Application Client project for an existing one.
 * @author Pavel Buzek
 */
public class ImportAppClientProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
//    private boolean imp = true;

    // Make sure list of steps is accurate.
    private static final String[] STEPS = new String[]{
        NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_IW_ImportTitle"), //NOI18N
        NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LAB_ConfigureSourceRoots") //NOI18N
    };
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ImportLocation(),
            new PanelSourceFolders.Panel()
        };
    }

    public Set<FileObject> instantiate() throws IOException/*, IllegalStateException*/ {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String) wiz.getProperty(WizardProperties.NAME);
//        File dirSrcF = (File) wiz.getProperty (WizardProperties.SOURCE_ROOT);
        File[] sourceFolders = (File[]) wiz.getProperty(WizardProperties.JAVA_ROOT);
        File[] testFolders = (File[]) wiz.getProperty(WizardProperties.TEST_ROOT);
        File configFilesFolder = (File) wiz.getProperty(WizardProperties.CONFIG_FILES_FOLDER);
        File libName = (File) wiz.getProperty(WizardProperties.LIB_FOLDER);
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        
        AntProjectHelper h = AppClientProjectGenerator.importProject(dirF, name, sourceFolders, testFolders, configFilesFolder, libName, j2eeLevel, serverInstanceID);
        FileObject dir = FileUtil.toFileObject (dirF);

        Project earProject = (Project) wiz.getProperty(WizardProperties.EAR_APPLICATION);
        AppClientProject createdAppClientProject = (AppClientProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdAppClientProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addCarModule(createdAppClientProject.getAPICar());
            }
        }
        
        resultSet.add (dir);

        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);    
        }
        
        // remember last used server
        FoldersListSettings.getDefault().setLastUsedServer(serverInstanceID);
        
        // downgrade the Java platform or src level to 1.4
        String platformName = (String)wiz.getProperty(WizardProperties.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(WizardProperties.SOURCE_LEVEL);
        if (platformName != null || sourceLevel != null) {
            AppClientProjectGenerator.setPlatform(h, platformName, sourceLevel);
        }
                        
        return resultSet;
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        Utils.setSteps(panels, STEPS);
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardProperties.PROJECT_DIR, null);
        this.wiz.putProperty(WizardProperties.NAME, null);
        this.wiz.putProperty (WizardProperties.SOURCE_ROOT, null);
        this.wiz.putProperty(WizardProperties.JAVA_ROOT, null);
        this.wiz.putProperty(WizardProperties.TEST_ROOT, null);
        this.wiz.putProperty(WizardProperties.CONFIG_FILES_FOLDER, null);
        this.wiz.putProperty(WizardProperties.LIB_FOLDER, null);
        this.wiz.putProperty(WizardProperties.SERVER_INSTANCE_ID, null);
        this.wiz.putProperty(WizardProperties.J2EE_LEVEL, null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(
                NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_WizardStepsCount"),
                index + 1, panels.length);
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
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
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}

