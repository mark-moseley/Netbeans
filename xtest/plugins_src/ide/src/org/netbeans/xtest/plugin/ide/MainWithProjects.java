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
package org.netbeans.xtest.plugin.ide;

import org.openide.ErrorManager;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

/**
 * Portion of Main that needs to run with access to Projects API & impl.
 * @author Jan Chalupa, Jesse Glick, Jiri Skrivanek
 */
public class MainWithProjects implements Main.MainWithProjectsInterface {
    
    /** Opens project on specified path.
     * @param projectPath path to a directory with project to open
     */
    public void openProject(String projectPath) {
        openProject(new File(projectPath));
    }
    
    /** Opens project in specified directory.
     * @param projectDir a directory with project to open
     */
    public void openProject(File projectDir) {
        try {
            /* not needed because there should always be something mounted
            // mount project path to FS repository
            Repository repo = Repository.getDefault();
            LocalFileSystem lfs = new LocalFileSystem();
            lfs.setRootDirectory(projectDir);
            repo.addFileSystem(lfs);
             */         
            // open project
            FileObject[] fos = FileUtil.fromFile(projectDir);
            if(fos.length == 0) {
                throw new IllegalArgumentException("Invalid value of xtest.ide.open.project property: "+projectDir.getAbsolutePath());
            }
            Project project = ProjectManager.getDefault().findProject(fos[0]);
            OpenProjectList.getDefault().open(project);
            // Set main? Probably user should do this if he wants.
            // OpenProjectList.getDefault().setMainProject(project);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }        
    }
    
    /** Creates an empty Java project in specified directory and opens it. 
     * Its name is XTestProject. It is cleaned
     * after each test bag if the property test.reuse.ide is not set to true.
     * @param projectDir directory where to create XTestProject subdirectory and
     * new project structure in that subdirectory.
     */
    public void createProject(String projectDir) {
        File dir = new File(projectDir, "XTestProject");
        String codename = "XTestProject";
        String displayName = "XTestProject";
        String mainClass = null;
        try {
            J2SEProjectGenerator.createProject(dir, codename, displayName, mainClass);
            openProject(dir);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }
}