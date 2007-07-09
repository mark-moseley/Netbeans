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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectHelper;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.util.MyFileUtil;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jqian
 */
public class JbiProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private JbiProject project;
    
    /** Creates a new instance of JbiProjectOperations */
    public JbiProjectOperations(JbiProject project) {
        this.project = project;
    }
    
    
    private static void addFile(FileObject projectDirectory, String fileName, List/*<FileObject>*/ result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
    
    public List/*<FileObject>*/ getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List/*<FileObject>*/ files = new ArrayList();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, "build.xml", files); // NOI18N
        addFile(projectDirectory, "catalog.xml", files); // NOI18N
        
        return files;
    }
    
    public List/*<FileObject>*/ getDataFiles() {
        List/*<FileObject>*/ files = new ArrayList();
        
//        FileObject metaInf = project.getEjbModule().getMetaInf();
//        if (metaInf != null)
//            files.add(metaInf);
        //???
        
//        SourceRoots src = project.getSourceRoots();
//        FileObject[] srcRoots = src.getRoots();
//
//        for (int cntr = 0; cntr < srcRoots.length; cntr++) {
//            files.add(srcRoots[cntr]);
//        }
        FileObject srcDir = project.getSourceDirectory();
        if (srcDir != null) {
            files.add(srcDir);
        }
        
        PropertyEvaluator evaluator = project.evaluator();
        String prop = evaluator.getProperty(JbiProjectProperties.SOURCE_ROOT);
        if (prop != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject dir = project.getAntProjectHelper().resolveFileObject(prop);
            if (dir != null && projectDirectory != dir && !files.contains(dir))
                files.add(dir);
        }
        
//        SourceRoots test = project.getTestSourceRoots();
//        FileObject[] testRoots = test.getRoots();
//
//        for (int cntr = 0; cntr < testRoots.length; cntr++) {
//            files.add(testRoots[cntr]);
//        }
        FileObject testDir = project.getTestDirectory();
        if (testDir != null) {
            files.add(testDir);
        }
        
//        File resourceDir = project.getEjbModule().getEnterpriseResourceDirectory();
//        if (resourceDir != null) {
//            FileObject resourceFO = FileUtil.toFileObject(resourceDir);
//            if (resourceFO != null)
//                files.add(resourceFO);
//        }
        //???
        
        return files;
    }
    
    public void notifyDeleting() throws IOException {
        JbiActionProvider ap = (JbiActionProvider) project.getLookup().lookup(JbiActionProvider.class);
        
        assert ap != null;
        
        Lookup context = Lookups.fixed(new Object[0]);
        Properties p = new Properties();
        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, p);
        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() throws IOException {
        project.getAntProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        // do nothing.
        // This does copy the old distribution file over though, which is
        // probably OK because "ant clean" will clean it up.
    }
    
    public void notifyCopied(Project original, File originalPath, final String newName) {
        if (original == null) {
            // do nothing for the original project.
            return ;
        }
        
        project.getReferenceHelper().fixReferences(originalPath);
        
        String oldName = project.getName();
        project.setName(newName);
        fixOtherNameReferences(originalPath, oldName, newName);
    }
    
    public void notifyMoving() throws IOException {
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, final String newName) {
        if (original == null) {
            project.getAntProjectHelper().notifyDeleted();
            return ;
        }
        String oldName = project.getName();
        project.setName(newName);
        project.getReferenceHelper().fixReferences(originalPath);
        fixOtherNameReferences(originalPath, oldName, newName);
    }
    
    private void fixOtherNameReferences(final File originalPath, 
            final String oldName, final String newName) {
        final File projectDir = FileUtil.toFile(project.getProjectDirectory());
        
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                AntProjectHelper helper = project.getAntProjectHelper();
                EditableProperties props = 
                        helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                
                String path = props.getProperty(JbiProjectProperties.META_INF);
                if (path != null && path.startsWith(originalPath.getAbsolutePath())) {
                    String relative = PropertyUtils.relativizeFile(originalPath, new File(path));
                    String fixedPath = new File(projectDir, relative).getAbsolutePath();
                    props.setProperty(JbiProjectProperties.META_INF, fixedPath);
                }
                
                // Fix dist.jar
                String distJarName = (String) props.get(JbiProjectProperties.DIST_JAR);
                if (distJarName.endsWith("/" + oldName + ".zip")) { // NOI18N
                    int distJarNamePrefixLen = distJarName.length() - oldName.length() - 4;
                    String distJarNamePrefix = distJarName.substring(0, distJarNamePrefixLen);
                    props.put(JbiProjectProperties.DIST_JAR,
                            distJarNamePrefix + newName + ".zip"); // NOI18N
                }
                
                // Fix uuid
                JbiProjectHelper.setJbiProjectName(props, newName);
                
                // Fix SA description (TEMP)
                JbiProjectHelper.updateServiceAssemblyDescription(props, oldName, newName);
                
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                
                // Fix casa wsdl references inside casa:
                //     <link xlink:href="../jbiasa/SynchronousSampleApplication.wsdl#xpointer(
                FileObject casaDir = project.getProjectDirectory().getFileObject("src/conf"); // NOI18N
                FileObject oldCasaFO = casaDir.getFileObject(oldName + ".casa"); // NOI18N
                if (oldCasaFO != null) {
                    try {
                        MyFileUtil.replaceAll(oldCasaFO,
                                "../jbiasa/" + oldName + ".wsdl#xpointer(",  // NOI18N
                                "../jbiasa/" + newName + ".wsdl#xpointer(",  // NOI18N
                                false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                FileObject srcDirFO = project.getSourceDirectory();
                FileObject oldCompAppWsdlFO = srcDirFO.getFileObject(oldName + ".wsdl"); // NOI18N
                if (oldCompAppWsdlFO != null) {
                    // Fix casa wsdl target namespace:
                    //     "http://enterprise.netbeans.org/casa/SynchronousSampleApplication"
                    try {
                        MyFileUtil.replaceAll(oldCompAppWsdlFO,
                                "\"http://enterprise.netbeans.org/casa/" + oldName + "\"",  // NOI18N
                                "\"http://enterprise.netbeans.org/casa/" + newName + "\"",  // NOI18N
                                false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    // Rename <compapp>.wsdl
                    try {
                        FileUtil.moveFile(oldCompAppWsdlFO, srcDirFO, newName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                // Rename <Proj>.casa
                if (oldCasaFO != null) { 
                    try {
                        FileUtil.moveFile(oldCasaFO, casaDir, newName);
                        CasaHelper.registerCasaFileListener(project); // FIXME: need the new project.
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                
                JbiProjectProperties jbiProjectProperties = project.getProjectProperties();
                jbiProjectProperties.saveAssemblyInfo();    // update AssemblyInformation.xml
            }
        });
    }    
}
