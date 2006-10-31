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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Servers for generating new NetBeans Modules templates.
 *
 * @author Martin Krauskopf
 */
public class NbModuleProjectGenerator {
    
    public static final String PLATFORM_PROPERTIES_PATH =
            "nbproject/platform.properties"; // NOI18N
    
    /** Use static factory methods instead. */
    private NbModuleProjectGenerator() {/* empty constructor*/}
    
    /** Generates standalone NetBeans Module. */
    public static void createStandAloneModule(final File projectDir, final String cnb,
            final String name, final String bundlePath,
            final String layerPath, final String platformID) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    final FileObject dirFO = FileUtil.createFolder(projectDir);
                    if (ProjectManager.getDefault().findProject(dirFO) != null) {
                        throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
                    }
                    createProjectXML(dirFO, cnb, NbModuleTypeProvider.STANDALONE);
                    createPlatformProperties(dirFO, platformID);
                    createManifest(dirFO, cnb, bundlePath, layerPath);
                    if (bundlePath != null) {
                        createBundle(dirFO, bundlePath, name);
                    }
                    if (layerPath != null) {
                        createLayerInSrc(dirFO, layerPath);
                    }
                    createEmptyTestDir(dirFO);
                    ModuleList.refresh();
                    ProjectManager.getDefault().clearNonProjectCache();
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /** Generates suite component NetBeans Module. */
    public static void createSuiteComponentModule(final File projectDir, final String cnb,
            final String name, final String bundlePath,
            final String layerPath, final File suiteDir) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    final FileObject dirFO = FileUtil.createFolder(projectDir);
                    if (ProjectManager.getDefault().findProject(dirFO) != null) {
                        throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
                    }
                    createProjectXML(dirFO, cnb, NbModuleTypeProvider.SUITE_COMPONENT);
                    createSuiteProperties(dirFO, suiteDir);
                    createManifest(dirFO, cnb, bundlePath, layerPath);
                    if (bundlePath != null) {
                        createBundle(dirFO, bundlePath, name);
                    }
                    if (layerPath != null) {
                        createLayerInSrc(dirFO, layerPath);
                    }
                    createEmptyTestDir(dirFO);
                    ModuleList.refresh();
                    ProjectManager.getDefault().clearNonProjectCache();
                    appendToSuite(cnb, dirFO, suiteDir);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /** Generates suite component Library Wrapper NetBeans Module. */
    public static void createSuiteLibraryModule(final File projectDir, final String cnb,
            final String name, final String bundlePath, final File suiteDir,
            final File license, final File[] jars) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    final FileObject dirFO = FileUtil.createFolder(projectDir);
                    if (ProjectManager.getDefault().findProject(dirFO) != null) {
                        throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
                    }
                    
                    EditableProperties props = new EditableProperties(true);
                    props.put(SingleModuleProperties.IS_AUTOLOAD, "true"); // NOI18N
                    SortedSet<String> packageList = new TreeSet();
                    Map classPathExtensions = new HashMap();
                    
                    File releaseDir = new File(projectDir, "release/modules/ext"); //NOI18N
                    if (!releaseDir.mkdirs()) {
                        //TODO report error
                        Util.err.log("cannot create release directory.");
                    }
                    FileObject relDirFo = FileUtil.toFileObject(releaseDir);
                    for (int i = 0; i < jars.length; i++) {
                        FileObject orig = FileUtil.toFileObject(FileUtil.normalizeFile(jars[i]));
                        if (orig != null) {
                            JarFile jf = null;
                            try {
                                FileUtil.copyFile(orig, relDirFo, orig.getName());
                                jf = new JarFile(jars[i]);
                                Enumeration en = jf.entries();
                                while (en.hasMoreElements()) {
                                    JarEntry entry = (JarEntry)en.nextElement();
                                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) { // NOI18N
                                        String nm = entry.getName();
                                        if (!Util.isValidJavaFQN(nm.substring(0, nm.length() - 6).replace('/', '.'))) {
                                            continue; // #72669
                                        }
                                        int index = nm.lastIndexOf('/');
                                        if (index > -1) {
                                            String path = nm.substring(0, index);
                                            packageList.add(path.replace('/', '.'));
                                        }
                                    }
                                }
                                classPathExtensions.put("ext/" + orig.getNameExt(), "release/modules/ext/" + orig.getNameExt()); // NOI18N
                            } catch (IOException e) {
                                //TODO report
                                Util.err.notify(e);
                            } finally {
                                if (jf != null) {
                                    try {
                                        jf.close();
                                    } catch (IOException e) {
                                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                                    }
                                }
                            }
                        }
                    }
                    
                    if (license != null && license.exists()) {
                        FileObject fo = FileUtil.toFileObject(license);
                        try {
                            FileUtil.copyFile(fo, dirFO, fo.getName());
                            props.put(SingleModuleProperties.LICENSE_FILE, "${basedir}/" + fo.getNameExt()); // NOI18N
                            //TODO set the nbm.license property
                        } catch (IOException e) {
                            //TODO report
                            Util.err.notify(e);
                        }
                        
                    }
                    ProjectXMLManager.generateLibraryModuleTemplate(
                            createFileObject(dirFO, AntProjectHelper.PROJECT_XML_PATH),
                            cnb, NbModuleTypeProvider.SUITE_COMPONENT, packageList, classPathExtensions);
                    createSuiteProperties(dirFO, suiteDir);
                    createManifest(dirFO, cnb, bundlePath, null);
                    createBundle(dirFO, bundlePath, name);
                    
                    // write down the nbproject/properties file
                    FileObject bundleFO = createFileObject(
                            dirFO, "nbproject/project.properties"); // NOI18N
                    Util.storeProperties(bundleFO, props);
                    
                    ModuleList.refresh();
                    ProjectManager.getDefault().clearNonProjectCache();
                    appendToSuite(cnb, dirFO, suiteDir);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Generates NetBeans Module within the netbeans.org CVS tree.
     */
    public static void createNetBeansOrgModule(final File projectDir, final String cnb,
            final String name, final String bundlePath, final String layerPath) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    File nborg = ModuleList.findNetBeansOrg(projectDir);
                    if (nborg == null) {
                        throw new IllegalArgumentException(projectDir + " doesn't " + // NOI18N
                                "point to directory within the netbeans.org CVS tree"); // NOI18N
                    }
                    final FileObject dirFO = FileUtil.createFolder(projectDir);
                    if (ProjectManager.getDefault().findProject(dirFO) != null) {
                        throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
                    }
                    createNetBeansOrgBuildXML(dirFO, cnb, nborg);
                    createProjectXML(dirFO, cnb, NbModuleTypeProvider.NETBEANS_ORG);
                    createManifest(dirFO, cnb, bundlePath, layerPath);
                    createBundle(dirFO, bundlePath, name);
                    createLayerInSrc(dirFO, layerPath);
                    createEmptyTestDir(dirFO);
                    ModuleList.refresh();
                    ProjectManager.getDefault().clearNonProjectCache();
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Creates basic <em>nbbuild/project.xml</em> or whatever
     * <code>AntProjectHelper.PROJECT_XML_PATH</code> is pointing to for
     * <em>standalone</em> or <em>module in suite</em> module.
     */
    private static void createProjectXML(FileObject projectDir,
            String cnb, NbModuleTypeProvider.NbModuleType type) throws IOException {
        ProjectXMLManager.generateEmptyModuleTemplate(
                createFileObject(projectDir, AntProjectHelper.PROJECT_XML_PATH),
                cnb, type);
    }
    
    /**
     * Creates basic <em>build.xml</em> or whatever
     * <code>GeneratedFilesHelper.BUILD_XML_PATH</code> is pointing to.
     */
    private static void createNetBeansOrgBuildXML(FileObject projectDir, String cnb,
            File nborg) throws IOException {
        FileObject buildScript = NbModuleProjectGenerator.createFileObject(
                projectDir, GeneratedFilesHelper.BUILD_XML_PATH);
        Document prjDoc = XMLUtil.createDocument("project", null, null, null); // NOI18N
        Element prjEl = prjDoc.getDocumentElement();
        prjEl.setAttribute("name", PropertyUtils.relativizeFile(nborg, // NOI18N
                FileUtil.toFile(projectDir)));
        prjEl.setAttribute("default", "netbeans"); // NOI18N
        prjEl.setAttribute("basedir", "."); // NOI18N
        
        Element el = prjDoc.createElement("description"); // NOI18N
        el.appendChild(prjDoc.createTextNode("Builds, tests, and runs the " + // NOI18N
                "project " + cnb)); // NOI18N
        prjEl.appendChild(el);
        
        el = prjDoc.createElement("import"); // NOI18N
        el.setAttribute("file", PropertyUtils.relativizeFile(FileUtil.toFile(projectDir), // NOI18N
                new File(nborg, "nbbuild/templates/projectized.xml"))); // NOI18N
        prjEl.appendChild(el);
        
        // store document to disk
        FileLock lock = buildScript.lock();
        try {
            OutputStream os = buildScript.getOutputStream(lock);
            try {
                XMLUtil.write(prjDoc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Detects whether <code>projectDir</code> is relative to
     * <code>suiteDir</code> and creates <em>nbproject/suite.properties</em> or
     * <em>nbproject/private/suite-private.properties</em> with
     * <em>suite.dir</em> appropriately set.
     */
    public static void createSuiteProperties(FileObject projectDir, File suiteDir) throws IOException {
        File projectDirF = FileUtil.toFile(projectDir);
        String suiteLocation;
        String suitePropertiesLocation;
        if (CollocationQuery.areCollocated(projectDirF, suiteDir)) {
            suiteLocation = "${basedir}/" + PropertyUtils.relativizeFile(projectDirF, suiteDir); // NOI18N
            suitePropertiesLocation = "nbproject/suite.properties"; // NOI18N
        } else {
            suiteLocation = suiteDir.getAbsolutePath();
            suitePropertiesLocation = "nbproject/private/suite-private.properties"; // NOI18N
        }
        EditableProperties props = new EditableProperties(true);
        props.setProperty("suite.dir", suiteLocation); // NOI18N
        FileObject suiteProperties = createFileObject(projectDir, suitePropertiesLocation);
        Util.storeProperties(suiteProperties, props);
    }
    
    /**
     * Appends currently created project in the <code>projectDir<code> to a
     * suite project contained in the <code>suiteDir</code>. Also intelligently
     * decides whether an added project is relative to a destination suite or
     * absolute and uses either <em>nbproject/project.properties</em> or
     * <em>nbproject/private/private.properties</em> appropriately.
     */
    private static void appendToSuite(String cnb, FileObject projectDir, File suiteDir) throws IOException {
        File projectDirF = FileUtil.toFile(projectDir);
        File suiteGlobalPropsFile = new File(suiteDir, "nbproject/project.properties"); // NOI18N
        FileObject suiteGlobalPropFO;
        if (suiteGlobalPropsFile.exists()) {
            suiteGlobalPropFO = FileUtil.toFileObject(suiteGlobalPropsFile);
        } else {
            suiteGlobalPropFO = createFileObject(suiteGlobalPropsFile);
        }
        EditableProperties globalProps = Util.loadProperties(suiteGlobalPropFO);
        String projectPropKey = "project." + cnb; // NOI18N
        if (CollocationQuery.areCollocated(projectDirF, suiteDir)) {
            globalProps.setProperty(projectPropKey,
                    PropertyUtils.relativizeFile(suiteDir, projectDirF));
        } else {
            File suitePrivPropsFile = new File(suiteDir, "nbproject/private/private.properties"); // NOI18N
            FileObject suitePrivPropFO;
            if (suitePrivPropsFile.exists()) {
                suitePrivPropFO = FileUtil.toFileObject(suitePrivPropsFile);
            } else {
                suitePrivPropFO = createFileObject(suitePrivPropsFile);
            }
            EditableProperties privProps= Util.loadProperties(suitePrivPropFO);
            privProps.setProperty(projectPropKey, projectDirF.getAbsolutePath());
            Util.storeProperties(suitePrivPropFO, privProps);
        }
        String modulesProp = globalProps.getProperty("modules"); // NOI18N
        if (modulesProp == null) {
            modulesProp = "";
        }
        if (modulesProp.length() > 0) {
            modulesProp += ":"; // NOI18N
        }
        modulesProp += "${" + projectPropKey + "}"; // NOI18N
        globalProps.setProperty("modules", modulesProp.split("(?<=:)", -1)); // NOI18N
        Util.storeProperties(suiteGlobalPropFO, globalProps);
    }
    
    private static void createPlatformProperties(FileObject projectDir, String platformID) throws IOException {
        FileObject plafPropsFO = createFileObject(
                projectDir, NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH);
        EditableProperties props = new EditableProperties(true);
        props.put("nbplatform.active", platformID); // NOI18N
        Util.storeProperties(plafPropsFO, props);
    }
    
    private static void createManifest(FileObject projectDir, String cnb,
            String bundlePath, String layerPath) throws IOException {
        FileObject manifestFO = createFileObject(
                projectDir, "manifest.mf"); // NOI18N
        ManifestManager.createManifest(manifestFO, cnb, "1.0", bundlePath, layerPath); // NOI18N
    }
    
    private static void createBundle(FileObject projectDir, String bundlePath,
            String name) throws IOException {
        String pathToBundle = "src/" + bundlePath.replace('\\','/'); // NOI18N
        FileObject bundleFO = createFileObject(projectDir, pathToBundle);
        EditableProperties props = new EditableProperties(true);
        props.put(LocalizedBundleInfo.NAME, name);
        Util.storeProperties(bundleFO, props);
    }
    
    private static void createLayerInSrc(FileObject projectDir, String layerPath) throws IOException {
        createLayer(projectDir, "src/" + layerPath); // NOI18N
    }
    
    public static FileObject createLayer(FileObject projectDir, String layerPath) throws IOException {
        FileObject layerFO = createFileObject(projectDir, layerPath); // NOI18N
        FileLock lock = layerFO.lock();
        try {
            InputStream is = NbModuleProjectGenerator.class.getResourceAsStream("ui/resources/layer_template.xml"); // NOI18N
            try {
                OutputStream os = layerFO.getOutputStream(lock);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        } finally {
            lock.releaseLock();
        }
        return layerFO;
    }
    
    private static void createEmptyTestDir(FileObject projectDir) throws IOException {
        FileUtil.createFolder(projectDir, "test/unit/src"); // NOI18N
    }
    
    /**
     * Creates a new <code>FileObject</code>.
     * Throws <code>IllegalArgumentException</code> if such an object already
     * exists. Throws <code>IOException</code> if creation fails.
     */
    private static FileObject createFileObject(FileObject dir, String relToDir) throws IOException {
        FileObject createdFO = dir.getFileObject(relToDir);
        if (createdFO != null) {
            throw new IllegalArgumentException("File " + createdFO + " already exists."); // NOI18N
        }
        createdFO = FileUtil.createData(dir, relToDir);
        return createdFO;
    }
    
    /**
     * Creates a new <code>FileObject</code>.
     * Throws <code>IllegalArgumentException</code> if such an object already
     * exists. Throws <code>IOException</code> if creation fails.
     */
    private static FileObject createFileObject(File fileToCreate) throws IOException {
        File parent = fileToCreate.getParentFile();
        if (parent == null) {
            throw new IllegalArgumentException("Cannot create: " + fileToCreate); // NOI18N
        }
        if (!parent.exists()) {
            parent.mkdirs();
        }
        return createFileObject(
                FileUtil.toFileObject(parent), fileToCreate.getName());
    }
    
}
