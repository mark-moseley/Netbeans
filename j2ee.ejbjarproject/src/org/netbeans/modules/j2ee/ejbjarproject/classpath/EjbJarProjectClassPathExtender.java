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
package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.AntArtifactChooser;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.openide.util.RequestProcessor;

public class EjbJarProjectClassPathExtender implements ProjectClassPathExtender,  PropertyChangeListener {
    
    private static final String DEFAULT_CLASS_PATH = EjbJarProjectProperties.JAVAC_CLASSPATH;
    private static final String DEFAULT_INCLUDED_LIBS_ELEMENT = ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES;

    private Project project;
    private UpdateHelper helper;
    private ReferenceHelper refHelper;
    private PropertyEvaluator eval;
    
    private ClassPathSupport cs;
    
    private volatile boolean projectDeleted;

    public EjbJarProjectClassPathExtender (Project project, UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper) {
        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.refHelper = refHelper;
        
        this.cs = new ClassPathSupport( eval, refHelper, helper.getAntProjectHelper(), 
                                        EjbJarProjectProperties.WELL_KNOWN_PATHS, 
                                        EjbJarProjectProperties.LIBRARY_PREFIX, 
                                        EjbJarProjectProperties.LIBRARY_SUFFIX, 
                                        EjbJarProjectProperties.ANT_ARTIFACT_PREFIX );   
        
        eval.addPropertyChangeListener(this); //listen for changes of libraries list
        registerLibraryListeners();
    }

    public boolean addLibrary(final Library library) throws IOException {
        return addLibraries(DEFAULT_CLASS_PATH, new Library[] { library }, DEFAULT_INCLUDED_LIBS_ELEMENT);
    }
    
    public boolean addLibraries(final String classPathId, final Library[] libraries, final String includedLibrariesElement) throws IOException {
        assert libraries != null : "Parameter cannot be null";       //NOI18N
        try {
            return ((Boolean)ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction () {
                        public Object run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathId);
                            List resources = cs.itemsList( raw, includedLibrariesElement );
                            boolean added = false;
                            for (int i = 0; i < libraries.length; i++) {
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( libraries[i], null, includedLibrariesElement != null );
                                if (!resources.contains(item)) { 
                                    resources.add (item);
                                    added = true;
                                }
                            }
                            if (added) {
                                String itemRefs[] = cs.encodeToStrings( resources.iterator(), includedLibrariesElement );                                
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                                props.setProperty(classPathId, itemRefs);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                
                                //update lib references in private properties
                                EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                                ArrayList l = new ArrayList ();
                                l.addAll(resources);
                                EjbJarProjectProperties.storeLibrariesLocations(l.iterator(), privateProps);
                                helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);

                                ProjectManager.getDefault().saveProject(project);
                                return Boolean.TRUE;
                            }
                            return Boolean.FALSE;
                        }
                    }
            )).booleanValue();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                Exception t = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
            }
        }
    }

    public boolean addArchiveFile(final FileObject archiveFile) throws IOException {
        return addArchiveFiles(DEFAULT_CLASS_PATH, new FileObject[] { archiveFile }, DEFAULT_INCLUDED_LIBS_ELEMENT);
    }

    public boolean addArchiveFiles(final String classPathId, final FileObject[] archiveFiles, final String includedLibrariesElement) throws IOException {
        assert archiveFiles != null : "Parameter cannot be null";       //NOI18N
        try {
            return ((Boolean)ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction () {
                        public Object run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathId);                            
                            List resources = cs.itemsList( raw, includedLibrariesElement );
                            boolean added = false;
                            for (int i = 0; i < archiveFiles.length; i++) {
                                File f = FileUtil.toFile (archiveFiles[i]);
                                if (f == null ) {
                                    throw new IllegalArgumentException ("The file must exist on disk");     //NOI18N
                                }
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( f, null, includedLibrariesElement != null );
                                if (!resources.contains(item)) {
                                    resources.add (item);
                                    added = true;
                                }
                            }
                            if (added) {
                                String itemRefs[] = cs.encodeToStrings( resources.iterator(), includedLibrariesElement );
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                                props.setProperty(classPathId, itemRefs);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return Boolean.TRUE;
                            }
                            return Boolean.FALSE;
                        }
                    }
            )).booleanValue();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                Exception t = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
            }
        }
    }
    
    // TODO: AB: AntArtifactItem should not be in LibrariesChooser
    
    public boolean addAntArtifact(AntArtifact artifact, URI artifactElement) throws IOException {
        return addAntArtifacts(DEFAULT_CLASS_PATH, new AntArtifactChooser.ArtifactItem[] { new AntArtifactChooser.ArtifactItem(artifact, artifactElement) }, DEFAULT_INCLUDED_LIBS_ELEMENT);
    }

    public boolean addAntArtifacts(final String classPathId, final AntArtifactChooser.ArtifactItem[] artifactItems, final String includedLibrariesElement) throws IOException {
        assert artifactItems != null : "Parameter cannot be null";       //NOI18N
        try {
            return ((Boolean)ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction () {
                        public Object run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty (classPathId);
                            List resources = cs.itemsList( raw, includedLibrariesElement );
                            boolean added = false;
                            for (int i = 0; i < artifactItems.length; i++) {
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( artifactItems[i].getArtifact(), artifactItems[i].getArtifactURI(), null, includedLibrariesElement != null );                            
                                if (!resources.contains(item)) {
                                    resources.add (item);
                                    added = true;
                                }
                            }
                            if (added) {
                                String itemRefs[] = cs.encodeToStrings( resources.iterator(), includedLibrariesElement );                                
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                                props.setProperty (classPathId, itemRefs);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return Boolean.TRUE;
                            }
                            return Boolean.FALSE;
                        }
                    }
            )).booleanValue();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                Exception t = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
            }
        }
    }
    
    public ClassPathSupport getClassPathSupport() {
         return this.cs;
    }

    private void registerLibraryListeners () {
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
        Library libs [] = LibraryManager.getDefault().getLibraries();
        for (int i = 0; i < libs.length; i++) {
            libs [i].removePropertyChangeListener(this);
        }
        Iterator i = cs.itemsIterator(props.getProperty(EjbJarProjectProperties.JAVAC_CLASSPATH),  ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES);
        while (i.hasNext()) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)i.next();
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY && !item.isBroken()) {
                item.getLibrary().addPropertyChangeListener(this);
            }
        }
    }
    
    public void propertyChange (PropertyChangeEvent e) {
        if (projectDeleted) {
            return;
        }
        if (e.getSource().equals(eval) && (e.getPropertyName().equals(EjbJarProjectProperties.JAVAC_CLASSPATH))) {
            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH); //Reread the properties, PathParser changes them
            String javacCp = props.getProperty(EjbJarProjectProperties.JAVAC_CLASSPATH);
            if (javacCp != null) {
                registerLibraryListeners();
                storeLibLocations();
            }
        } else if (e.getPropertyName().equals(Library.PROP_CONTENT)) {
            storeLibLocations();
        }
    }
    
    private void storeLibLocations() {
        ProjectManager.mutex().postWriteRequest(new Runnable () {
            public void run() {
                EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                //update lib references in private properties
                EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                List wmLibs = cs.itemsList(props.getProperty(EjbJarProjectProperties.JAVAC_CLASSPATH),  ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES);
                cs.encodeToStrings(wmLibs.iterator(), ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES);
                EjbJarProjectProperties.storeLibrariesLocations(wmLibs.iterator(), privateProps);
                helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);

                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                });
            }
        });
    }
    
    public void notifyDeleting() {
        projectDeleted = true;
        eval.removePropertyChangeListener(this);
    }
}
