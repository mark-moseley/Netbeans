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

package org.netbeans.modules.project.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Singleton {@link ProjectFactory} implementation which handles all Ant-based
 * projects by delegating some functionality to registered Ant project types.
 * @author Jesse Glick
 */
public final class AntBasedProjectFactorySingleton implements ProjectFactory {
    
    public static final String PROJECT_XML_PATH = "nbproject/project.xml"; // NOI18N

    public static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1"; // NOI18N
    
    /** Construct the singleton. */
    public AntBasedProjectFactorySingleton() {}
    
    private static final Map/*<Project,AntProjectHelper>*/ project2Helper = new WeakHashMap();
    private static final Map/*<AntProjectHelper,Project>*/ helper2Project = new WeakHashMap();
    private static final Lookup.Result/*<AntBasedProjectType>*/ antBasedProjectTypes;
    private static Map/*<String,AntBasedProjectType>*/ antBasedProjectTypesByType = null;
    static {
        antBasedProjectTypes = Lookup.getDefault().lookup(new Lookup.Template(AntBasedProjectType.class));
        antBasedProjectTypes.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                synchronized (AntBasedProjectFactorySingleton.class) {
                    antBasedProjectTypesByType = null;
                }
            }
        });
    }
    
    private static synchronized AntBasedProjectType findAntBasedProjectType(String type) {
        if (antBasedProjectTypesByType == null) {
            Iterator it = new ArrayList(antBasedProjectTypes.allInstances()).iterator();
            // No need to synchronize similar calls since this is called only inside
            // ProjectManager.mutex. However dkonecny says that allInstances can
            // trigger a LookupEvent which would clear antBasedProjectTypesByType,
            // so need to initialize that later; and who knows then Lookup changes
            // might be fired.
            antBasedProjectTypesByType = new HashMap();
            while (it.hasNext()) {
                AntBasedProjectType abpt = (AntBasedProjectType)it.next();
                antBasedProjectTypesByType.put(abpt.getType(), abpt);
            }
        }
        return (AntBasedProjectType)antBasedProjectTypesByType.get(type);
    }
    
    public boolean isProject(FileObject dir) {
        File dirF = FileUtil.toFile(dir);
        if (dirF == null) {
            return false;
        }
        // Just check whether project.xml exists. Do not attempt to parse it, etc.
        // Do not use FileObject.getFileObject since that may load other sister files.
        File projectXmlF = new File(new File(dirF, "nbproject"), "project.xml"); // NOI18N
        return projectXmlF.isFile();
    }
    
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (FileUtil.toFile(projectDirectory) == null) {
            return null;
        }
        FileObject projectFile = projectDirectory.getFileObject(PROJECT_XML_PATH);
        if (projectFile == null || !projectFile.isData()) {
            return null;
        }
        File projectDiskFile = FileUtil.toFile(projectFile);
        assert projectDiskFile != null;
        Document projectXml;
        try {
            projectXml = XMLUtil.parse(new InputSource(projectDiskFile.toURI().toString()), false, true, Util.defaultErrorHandler(), null);
        } catch (SAXException e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
        Element projectEl = projectXml.getDocumentElement();
        if (!"project".equals(projectEl.getLocalName()) || !PROJECT_NS.equals(projectEl.getNamespaceURI())) { // NOI18N
            return null;
        }
        Element typeEl = Util.findElement(projectEl, "type", PROJECT_NS); // NOI18N
        if (typeEl == null) {
            return null;
        }
        String type = Util.findText(typeEl);
        if (type == null) {
            return null;
        }
        AntBasedProjectType provider = findAntBasedProjectType(type);
        if (provider == null) {
            return null;
        }
        AntProjectHelper helper = HELPER_CALLBACK.createHelper(projectDirectory, projectXml, state, provider);
        Project project = provider.createProject(helper);
        project2Helper.put(project, helper);
        helper2Project.put(helper, project);
        return project;
    }
    
    public void saveProject(Project project) throws IOException, ClassCastException {
        AntProjectHelper helper = (AntProjectHelper)project2Helper.get(project);
        if (helper == null) {
            throw new ClassCastException(project.getClass().getName());
        }
        HELPER_CALLBACK.save(helper);
    }
    
    /**
     * Get the project corresponding to a helper.
     * For use from {@link AntProjectHelper}.
     * @param helper an Ant project helper object
     * @return the corresponding project
     */
    public static Project getProjectFor(AntProjectHelper helper) {
        Project p = (Project)helper2Project.get(helper);
        assert p != null;
        return p;
    }
    
    /**
     * Get the helper corresponding to a project.
     * For use from {@link ProjectGenerator}.
     * @param project an Ant-based project
     * @return the corresponding Ant project helper object, or null if it is unknown
     */
    public static AntProjectHelper getHelperFor(Project p) {
        return (AntProjectHelper)project2Helper.get(p);
    }
    
    /**
     * Callback to create and access AntProjectHelper objects from outside its package.
     */
    public interface AntProjectHelperCallback {
        AntProjectHelper createHelper(FileObject dir, Document projectXml, ProjectState state, AntBasedProjectType type);
        void save(AntProjectHelper helper) throws IOException;
    }
    /** Defined in AntProjectHelper's static initializer. */
    public static AntProjectHelperCallback HELPER_CALLBACK;
    static {
        Object ignore = AntProjectHelper.class;
        assert HELPER_CALLBACK != null;
    }
    
}
