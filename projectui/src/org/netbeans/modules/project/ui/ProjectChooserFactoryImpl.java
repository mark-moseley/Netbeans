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

package org.netbeans.modules.project.ui;

import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

import java.io.File;

/**
 * Factory to be implemented bu the ui implementation
 * @author Petr Hrebejk
 */
public class ProjectChooserFactoryImpl implements ProjectChooserFactory {
            
    public ProjectChooserFactoryImpl() {}
    
    public JFileChooser createProjectChooser() {
        ProjectManager.getDefault().clearNonProjectCache(); // #41882
        return ProjectChooserAccessory.createProjectChooser( false );
    }

    public WizardDescriptor.Panel createSimpleTargetChooser( Project project, SourceGroup[] folders, WizardDescriptor.Panel bottomPanel ) {
        return new SimpleTargetChooserPanel( project, folders, bottomPanel, false );
    }

    public File getProjectsFolder() {
        return OpenProjectListSettings.getInstance().getProjectsFolder();
    }

    public void setProjectsFolder(File file) {
        OpenProjectListSettings.getInstance().setProjectsFolder (file);
    }

}
