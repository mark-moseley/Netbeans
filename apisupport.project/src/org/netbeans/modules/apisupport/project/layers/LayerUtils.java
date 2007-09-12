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

package org.netbeans.modules.apisupport.project.layers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.modules.xml.tax.parser.XMLParsingSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeObject;
import org.netbeans.tax.io.TreeStreamResult;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Task;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Misc support for dealing with layers.
 * @author Jesse Glick
 */
public class LayerUtils {
    
    private LayerUtils() {}
    
    /**
     * Translates nbres: into nbrescurr: for internal use.
     * Returns an array of one or more URLs.
     * May just be original URL, but will try to produce URLs corresponding to real files.
     * If there is a suffix, may produce several, most specific first.
     */
    static URL[] currentify(URL u, String suffix, ClassPath cp) {
        if (cp == null) {
            return new URL[] {u};
        }
        try {
            if (u.getProtocol().equals("nbres")) { // NOI18N
                String path = u.getFile();
                if (path.startsWith("/")) path = path.substring(1); // NOI18N
                FileObject fo = cp.findResource(path);
                if (fo != null) {
                    return new URL[] {fo.getURL()};
                }
            } else if (u.getProtocol().equals("nbresloc")) { // NOI18N
                List<URL> urls = new ArrayList<URL>();
                String path = u.getFile();
                if (path.startsWith("/")) path = path.substring(1); // NOI18N
                int idx = path.lastIndexOf('/');
                String folder;
                String nameext;
                if (idx == -1) {
                    folder = ""; // NOI18N
                    nameext = path;
                } else {
                    folder = path.substring(0, idx + 1);
                    nameext = path.substring(idx + 1);
                }
                idx = nameext.lastIndexOf('.');
                String name;
                String ext;
                if (idx == -1) {
                    name = nameext;
                    ext = ""; // NOI18N
                } else {
                    name = nameext.substring(0, idx);
                    ext = nameext.substring(idx);
                }
                List<String> suffixes = new ArrayList<String>(computeSubVariants(suffix));
                suffixes.add(suffix);
                Collections.reverse(suffixes);
                for (String trysuffix : suffixes) {
                    String trypath = folder + name + trysuffix + ext;
                    FileObject fo = cp.findResource(trypath);
                    if (fo != null) {
                        urls.add(fo.getURL());
                    }
                }
                if (!urls.isEmpty()) {
                    return urls.toArray(new URL[urls.size()]);
                }
            }
        } catch (FileStateInvalidException fsie) {
            Util.err.notify(ErrorManager.WARNING, fsie);
        }
        return new URL[] {u};
    }
    
    // E.g. for name 'foo_f4j_ce_ja', should produce list:
    // 'foo', 'foo_ja', 'foo_f4j', 'foo_f4j_ja', 'foo_f4j_ce'
    // Will actually produce:
    // 'foo', 'foo_ja', 'foo_ce', 'foo_ce_ja', 'foo_f4j', 'foo_f4j_ja', 'foo_f4j_ce'
    // since impossible to distinguish locale from branding reliably.
    private static List<String> computeSubVariants(String name) {
        int idx = name.indexOf('_');
        if (idx == -1) {
            return Collections.emptyList();
        } else {
            String base = name.substring(0, idx);
            String suffix = name.substring(idx);
            List<String> l = computeSubVariants(base, suffix);
            return l.subList(0, l.size() - 1);
        }
    }
    private static List<String> computeSubVariants(String base, String suffix) {
        int idx = suffix.indexOf('_', 1);
        if (idx == -1) {
            List<String> l = new LinkedList<String>();
            l.add(base);
            l.add(base + suffix);
            return l;
        } else {
            String remainder = suffix.substring(idx);
            List<String> l1 = computeSubVariants(base, remainder);
            List<String> l2 = computeSubVariants(base + suffix.substring(0, idx), remainder);
            List<String> l = new LinkedList<String>(l1);
            l.addAll(l2);
            return l;
        }
    }
    
    // XXX needs to hold a strong ref only when modified, probably?
    private static final Map<Project,LayerHandle> layerHandleCache = new WeakHashMap<Project,LayerHandle>();
    
    /**
     * Gets a handle for one project's XML layer.
     */
    public static LayerHandle layerForProject(Project project) {
        LayerHandle handle = layerHandleCache.get(project);
        if (handle == null) {
            handle = new LayerHandle(project);
            layerHandleCache.put(project, handle);
        }
        return handle;
    }

    private static final Set<String> XML_LIKE_TYPES = new HashSet<String>();
    static {
        XML_LIKE_TYPES.add(".settings"); // NOI18N
        XML_LIKE_TYPES.add(".wstcref"); // NOI18N
        XML_LIKE_TYPES.add(".wsmode"); // NOI18N
        XML_LIKE_TYPES.add(".wsgrp"); // NOI18N
        XML_LIKE_TYPES.add(".wsmgr"); // NOI18N
    }
    /**
     * Find the name of the external file that will be generated for a given
     * layer path if it is created with contents.
     * @param parent parent folder, or null
     * @param layerPath full path in layer
     * @return a simple file name
     */
    public static String findGeneratedName(FileObject parent, String layerPath) {
        Matcher m = Pattern.compile("(.+/)?([^/.]+)(\\.[^/]+)?").matcher(layerPath); // NOI18N
        if (!m.matches()) {
            throw new IllegalArgumentException(layerPath);
        }
        String base = m.group(2);
        String ext = m.group(3);
        if (ext == null) {
            ext = "";
        } else if (ext.equals(".java")) { // NOI18N
            ext = "_java"; // NOI18N
        } else if (XML_LIKE_TYPES.contains(ext)) {
            String upper = ext.substring(1,2).toUpperCase(Locale.ENGLISH);
            base = base + upper + ext.substring(2);
            ext = ".xml"; // NOI18N
        }
        String name = base + ext;
        if (parent == null || parent.getFileObject(name) == null) {
            return name;
        } else {
            for (int i = 1; true; i++) {
                name = base + '_' + i + ext;
                if (parent.getFileObject(name) == null) {
                    return name;
                }
            }
        }
    }
    
    /**
     * Representation of in-memory TAX tree which can be saved upon request.
     */
    interface SavableTreeEditorCookie extends TreeEditorCookie {
        
        /** property change fired when dirty flag changes */
        String PROP_DIRTY = "dirty"; // NOI18N
        
        /** true if there are in-memory mods */
        boolean isDirty();
        
        /** try to save any in-memory mods to disk */
        void save() throws IOException;
        
    }
    
    private static final class CookieImpl implements SavableTreeEditorCookie, FileChangeListener {
        private TreeDocumentRoot root;
        private boolean dirty;
        private Exception problem;
        private final FileObject f;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private boolean saving;
        public CookieImpl(FileObject f) {
            //System.err.println("new CookieImpl for " + f);
            this.f = f;
            f.addFileChangeListener(FileUtil.weakFileChangeListener(this, f));
        }
        public TreeDocumentRoot getDocumentRoot() {
            return root;
        }
        public int getStatus() {
            if (problem != null) {
                return TreeEditorCookie.STATUS_ERROR;
            } else if (root != null) {
                return TreeEditorCookie.STATUS_OK;
            } else {
                return TreeEditorCookie.STATUS_NOT;
            }
        }
        public TreeDocumentRoot openDocumentRoot() throws IOException, TreeException {
            if (root == null) {
                try {
                    //System.err.println("openDocumentRoot: really opening");
                    boolean oldDirty = dirty;
                    int oldStatus = getStatus();
                    root = new XMLParsingSupport().parse(new InputSource(f.getURL().toExternalForm()));
                    problem = null;
                    dirty = false;
                    pcs.firePropertyChange(PROP_DIRTY, oldDirty, false);
                    pcs.firePropertyChange(PROP_STATUS, oldStatus, TreeEditorCookie.STATUS_OK);
                    //pcs.firePropertyChange(PROP_DOCUMENT_ROOT, null, root);
                } catch (IOException e) {
                    problem = e;
                    throw e;
                } catch (TreeException e) {
                    problem = e;
                    throw e;
                }
                ((TreeObject) root).addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        //System.err.println("tree modified");
                        modified();
                    }
                });
            }
            return root;
        }
        public Task prepareDocumentRoot() {
            throw new UnsupportedOperationException();
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        private void modified() {
            //System.err.println("modified(): dirty=" + dirty + " in " + Thread.currentThread().getName() + " for " + this);
            if (!dirty) {
                dirty = true;
                pcs.firePropertyChange(PROP_DIRTY, false, true);
            }
        }
        public boolean isDirty() {
            return dirty;
        }
        public synchronized void save() throws IOException {
            //System.err.println("save(): dirty=" + dirty + " in " + Thread.currentThread().getName() + " for " + this);
            if (root == null || !dirty) {
                return;
            }
            assert !saving;
            saving = true;
            //System.err.println("saving");
            try {
                FileLock lock = f.lock();
                try {
                    OutputStream os = f.getOutputStream(lock);
                    try {
                        new TreeStreamResult(os).getWriter(root).writeDocument();
                    } catch (TreeException e) {
                        throw (IOException) new IOException(e.toString()).initCause(e);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            } finally {
                saving = false;
                //System.err.println("!saving in " + Thread.currentThread().getName() + " for " + this);
            }
            dirty = false;
            pcs.firePropertyChange(PROP_DIRTY, true, false);
        }
        public void fileChanged(FileEvent fe) {
            changed();
        }
        public void fileDeleted(FileEvent fe) {
            changed();
        }
        public void fileRenamed(FileRenameEvent fe) {
            changed();
        }
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        public void fileFolderCreated(FileEvent fe) {
            assert false;
        }
        public void fileDataCreated(FileEvent fe) {
            assert false;
        }
        private void changed() {
            //System.err.println("changed on disk; saving=" + saving + " in " + Thread.currentThread().getName() + " for " + this);
            synchronized (this) {
                if (saving) {
                    return;
                }
                problem = null;
                dirty = false;
                root = null;
            }
            pcs.firePropertyChange(PROP_DOCUMENT_ROOT, null, null);
        }
    }
    
    static SavableTreeEditorCookie cookieForFile(FileObject f) {
        return new CookieImpl(f);
    }
    
    /**
     * Manages one project's XML layer.
     */
    public static final class LayerHandle {
        
        private final Project project;
        private FileSystem fs;
        private SavableTreeEditorCookie cookie;
        private boolean autosave;
        
        LayerHandle(Project project) {
            //System.err.println("new LayerHandle for " + project);
            this.project = project;
        }
        
        /**
         * Get the layer as a structured filesystem.
         * You can make whatever Filesystems API calls you like to it.
         * Just call {@link #save} when you are done so the modified XML document is saved
         * (or the user can save it explicitly if you don't).
         * @param create if true, and there is no layer yet, create it now; if false, just return null
         */
        public synchronized FileSystem layer(boolean create) {
            if (fs == null) {
                FileObject xml = getLayerFile();
                if (xml == null) {
                    if (!create) {
                        return null;
                    }
                    try {
                        NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
                        // Check to see if the manifest entry is already specified.
                        String layerSrcPath = ManifestManager.getInstance(Util.getManifest(module.getManifestFile()), false).getLayer();
                        if (layerSrcPath == null) {
                            layerSrcPath = newLayerPath();
                            FileObject manifest = module.getManifestFile();
                            EditableManifest m = Util.loadManifest(manifest);
                            m.setAttribute(ManifestManager.OPENIDE_MODULE_LAYER, layerSrcPath, null);
                            Util.storeManifest(manifest, m);
                        }
                        xml = NbModuleProjectGenerator.createLayer(project.getProjectDirectory(), module.getResourceDirectoryPath(false) + '/' + newLayerPath());
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        return fs = FileUtil.createMemoryFileSystem();
                    }
                }
                try {
                    fs = new WritableXMLFileSystem(xml.getURL(), cookie = cookieForFile(xml), /*XXX*/null);
                } catch (FileStateInvalidException e) {
                    throw new AssertionError(e);
                }
                cookie.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        //System.err.println("changed in mem");
                        if (autosave && SavableTreeEditorCookie.PROP_DIRTY.equals(evt.getPropertyName())) {
                            //System.err.println("  will save...");
                            try {
                                save();
                            } catch (IOException e) {
                                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                            }
                        }
                    }
                });
            }
            return fs;
        }
        
        /**
         * Save the layer, if it was in fact modified.
         * Note that nonempty layer entries you created will already be on disk.
         */
        public void save() throws IOException {
            if (cookie == null) {
                throw new IOException("Cannot save a nonexistent layer"); // NOI18N
            }
            cookie.save();
        }
        
        /**
         * Find the XML layer file for this project, if it exists.
         * @return the layer, or null
         */
        public FileObject getLayerFile() {
            NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
            Manifest mf = Util.getManifest(module.getManifestFile());
            if (mf == null) {
                return null;
            }
            String path = ManifestManager.getInstance(mf, false).getLayer();
            if (path == null) {
                return null;
            }
            return Util.getResourceDirectory(project).getFileObject(path);
        }
        
        /**
         * Set whether to automatically save changes to disk.
         * @param true to save changes immediately, false to save only upon request
         */
        public void setAutosave(boolean autosave) {
            this.autosave = autosave;
            if (autosave && cookie != null) {
                try {
                    cookie.save();
                } catch (IOException e) {
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        /**
         * Check whether this handle is currently in autosave mode.
         */
        public boolean isAutosave() {
            return autosave;
        }
        
        /**
         * Resource path in which to make a new XML layer.
         */
        private String newLayerPath() {
            NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
            return module.getCodeNameBase().replace('.', '/') + "/layer.xml"; // NOI18N
        }

        public @Override String toString() {
            FileObject layer = getLayerFile();
            if (layer != null) {
                return FileUtil.getFileDisplayName(layer);
            } else {
                return FileUtil.getFileDisplayName(project.getProjectDirectory());
            }
        }
        
    }
    
    /**
     * Get a filesystem that will look like what this project would "see".
     * <p>There are four possibilities:</p>
     * <ol>
     * <li><p>For a standalone module project, the filesystem will include all the XML
     * layers from all modules in the selected platform, plus this module's XML layer
     * as the writable layer (use {@link LayerHandle#save} to save changes as needed).</p></li>
     * <li><p>For a module suite project, the filesystem will include all the XML layers
     * from all modules in the selected platform which are not excluded in the current
     * suite configuration, plus the XML layers for modules in the suite (currently all
     * read-only, i.e. the filesystem is read-only).</p></li>
     * <li><p>For a suite component module project, the filesystem will include all XML
     * layers from non-excluded platform modules, plus the XML layers for modules in the
     * suite, with this module's layer being writable.</p></li>
     * <li><p>For a netbeans.org module, the filesystem will include all XML layers
     * from all netbeans.org modules that are not in the <code>extra</code> cluster,
     * plus the layer from this module (if it is in the <code>extra</code> cluster,
     * with this module's layer always writable.</p></li>
     * </ol>
     * <p>Does not currently attempt to cache the result,
     * though that could be attempted later as needed.</p>
     * <p>Will try to produce pleasant-looking display names and/or icons for files.</p>
     * <p>Note that parsing XML layers is not terribly fast so it would be wise to show
     * a "please wait" label or some other simple progress indication while this
     * is being called, if blocking the UI.</p>
     * @param project a project of one of the three types enumerated above
     * @return the effective system filesystem seen by that project
     * @throws IOException if there were problems loading layers, etc.
     * @see "#62257"
     */
    public static FileSystem getEffectiveSystemFilesystem(Project p) throws IOException {
        
            NbModuleProvider.NbModuleType type = Util.getModuleType(p);
            FileSystem projectLayer = layerForProject(p).layer(false);
            if (type == NbModuleProvider.STANDALONE) {
                Set<File> jars = 
                        getPlatformJarsForStandaloneProject(p);
                FileSystem[] platformLayers = getPlatformLayers(jars);
                ClassPath cp = createLayerClasspath(Collections.singleton(p), jars);
                return mergeFilesystems(projectLayer, platformLayers, cp);
            } else if (type == NbModuleProvider.SUITE_COMPONENT) {
                SuiteProject suite = SuiteUtils.findSuite(p);
                if (suite == null) {
                    throw new IOException("Could not load suite for " + p); // NOI18N
                }
                List<FileSystem> readOnlyLayers = new ArrayList<FileSystem>();
                Set<NbModuleProject> modules = SuiteUtils.getSubProjects(suite);
                for (NbModuleProject sister : modules) {
                    if (sister == p) {
                        continue;
                    }
                    LayerHandle handle = layerForProject(sister);
                    FileSystem roLayer = handle.layer(false);
                    if (roLayer != null) {
                        readOnlyLayers.add(roLayer);
                    }
                }
                Set<File> jars = getPlatformJarsForSuiteComponentProject(p, suite);
                readOnlyLayers.addAll(Arrays.asList(getPlatformLayers(jars)));
                ClassPath cp = createLayerClasspath(modules, jars);
                return mergeFilesystems(projectLayer, readOnlyLayers.toArray(new FileSystem[readOnlyLayers.size()]), cp);
            } else if (type == NbModuleProvider.NETBEANS_ORG) {
                //it's safe to cast to NbModuleProject here.
                NbModuleProject nbprj = p.getLookup().lookup(NbModuleProject.class);
                Set<NbModuleProject> projects = getProjectsForNetBeansOrgProject(nbprj);
                List<URL> otherLayerURLs = new ArrayList<URL>();
                
                for (NbModuleProject p2 : projects) {
                    if (p2.getManifest() == null) {
                        //profiler for example.
                        continue;
                    }
                    ManifestManager mm = ManifestManager.getInstance(p2.getManifest(), false);
                    String layer = mm.getLayer();
                    if (layer == null) {
                        continue;
                    }
                    FileObject src = p2.getSourceDirectory();
                    if (src == null) {
                        continue;
                    }
                    FileObject layerXml = src.getFileObject(layer);
                    if (layerXml == null) {
                        continue;
                    }
                    otherLayerURLs.add(layerXml.getURL());
                }
                XMLFileSystem xfs = new XMLFileSystem();
                try {
                    xfs.setXmlUrls(otherLayerURLs.toArray(new URL[otherLayerURLs.size()]));
                } catch (PropertyVetoException ex) {
                    assert false : ex;
                }
                ClassPath cp = createLayerClasspath(projects, Collections.<File>emptySet());
                return mergeFilesystems(projectLayer, new FileSystem[] {xfs}, cp);
            } else {
                throw new AssertionError(type);
            }
    }
    
    /**
     * Get the platform JARs associated with a standalone module project.
     */
    public static Set<File> getPlatformJarsForStandaloneProject(Project project) {
        NbModuleProvider mod = project.getLookup().lookup(NbModuleProvider.class);
        //TODO create a utility method to do this if needed in more places
        NbPlatform platform = null;
        File platformDir = mod.getActivePlatformLocation();
        if (platformDir != null) {
            platform = NbPlatform.getPlatformByDestDir(platformDir);
        }
        if (platform == null || !platform.isValid()) {
            platform = NbPlatform.getDefaultPlatform();
        }
        return getPlatformJars(platform, null, null, null);
    }
    
    public static Set<File> getPlatformJarsForSuiteComponentProject(Project project, SuiteProject suite) {
        NbPlatform platform = suite.getPlatform(true);
        PropertyEvaluator eval = suite.getEvaluator();
        String[] includedClusters = SuiteProperties.getArrayProperty(eval, SuiteProperties.ENABLED_CLUSTERS_PROPERTY);
        String[] excludedClusters = SuiteProperties.getArrayProperty(eval, SuiteProperties.DISABLED_CLUSTERS_PROPERTY);
        String[] excludedModules = SuiteProperties.getArrayProperty(eval, SuiteProperties.DISABLED_MODULES_PROPERTY);
        return getPlatformJars(platform, includedClusters, excludedClusters, excludedModules);
    }
    
    public static Set<NbModuleProject> getProjectsForNetBeansOrgProject(NbModuleProject project) throws IOException {
        ModuleList list = project.getModuleList();
        Set<NbModuleProject> projects = new HashSet<NbModuleProject>();
        projects.add(project);
        for (ModuleEntry other : list.getAllEntriesSoft()) {
            if (other.getClusterDirectory().getName().equals("extra")) { // NOI18N
                continue;
            }
            File root = other.getSourceLocation();
            assert root != null : other;
            NbModuleProject p2 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(root));
            if (p2 == null) {
                continue;
            }
            projects.add(p2);
        }
        return projects;
    }
    
    /**
     * Finds all the module JARs in the platform.
     * Can optionally pass non-null lists of cluster names and module CNBs to exclude, as per suite properties.
     */
    private static Set<File> getPlatformJars(NbPlatform platform, String[] includedClusters, String[] excludedClusters, String[] excludedModules) {
        if (platform == null) {
            return Collections.emptySet();
        }
        Set<String> includedClustersS = (includedClusters != null) ? new HashSet<String>(Arrays.asList(includedClusters)) : Collections.<String>emptySet();
        Set<String> excludedClustersS = (excludedClusters != null) ? new HashSet<String>(Arrays.asList(excludedClusters)) : Collections.<String>emptySet();
        Set<String> excludedModulesS = (excludedModules != null) ? new HashSet<String>(Arrays.asList(excludedModules)) : Collections.<String>emptySet();
        ModuleEntry[] entries = platform.getModules();
        Set<File> jars = new HashSet<File>(entries.length);
        for (ModuleEntry entry : entries) {
            if (!includedClustersS.isEmpty() && !includedClustersS.contains(entry.getClusterDirectory().getName())) {
                continue;
            }
            if (includedClustersS.isEmpty() && excludedClustersS.contains(entry.getClusterDirectory().getName())) {
                continue;
            }
            if (excludedModulesS.contains(entry.getCodeNameBase())) {
                continue;
            }
            jars.add(entry.getJarLocation());
        }
        return jars;
    }

    /**
     * Constructs a list of filesystems representing the XML layers of the supplied platform module JARs.
     */
    private static FileSystem[] getPlatformLayers(Set<File> platformJars) throws IOException {
        List<FileSystem> layers = new ArrayList<FileSystem>();
        JAR: for (File jar : platformJars) {
            ManifestManager mm = ManifestManager.getInstanceFromJAR(jar);
            for (String tok : mm.getRequiredTokens()) {
                if (tok.startsWith("org.openide.modules.os.")) { // NOI18N
                    // Best to exclude platform-specific modules, e.g. ide/applemenu, as they can cause confusion.
                    continue JAR;
                }
            }
            String layer = mm.getLayer();
            if (layer != null) {
                URL u = new URL("jar:" + jar.toURI() + "!/" + layer);
                try {
                    // XXX nbres: and such URL protocols may not work in platform layers
                    // (cf. org.openide.filesystems.ExternalUtil.findClass)
                    FileSystem xfs = new XMLFileSystem(u);
                    boolean hasMasks = false;
                    Enumeration e = xfs.getRoot().getChildren(true);
                    while (e.hasMoreElements()) {
                        FileObject f = (FileObject) e.nextElement();
                        if (f.getNameExt().endsWith("_hidden")) { // NOI18N
                            // #63295: put it at the beginning. Not as good as following module deps but probably close enough.
                            hasMasks = true;
                            break;
                        }
                    }
                    if (hasMasks) {
                        layers.add(0, xfs);
                    } else {
                        layers.add(xfs);
                    }
                } catch (SAXException e) {
                    throw (IOException) new IOException(e.toString()).initCause(e);
                }
            }
        }
        return layers.toArray(new FileSystem[layers.size()]);
    }
    
    /**
     * Creates a classpath representing the source roots and platform binary JARs for a project/suite.
     */
    static ClassPath createLayerClasspath(Set<? extends Project> moduleProjects, Set<File> platformJars) throws IOException {
        List<URL> roots = new ArrayList<URL>();
        for (Project p : moduleProjects) {
            NbModuleProvider mod = p.getLookup().lookup(NbModuleProvider.class);
            FileObject src = mod.getSourceDirectory();
            if (src != null) {
                roots.add(src.getURL());
            }
        }
        for (File jar  : platformJars) {
            roots.add(FileUtil.getArchiveRoot(jar.toURI().toURL()));
            File locale = new File(jar.getParentFile(), "locale"); // NOI18N
            if (locale.isDirectory()) {
                String n = jar.getName();
                int x = n.lastIndexOf('.');
                if (x == -1) {
                    x = n.length();
                }
                String base = n.substring(0, x);
                String ext = n.substring(x);
                String[] variants = locale.list();
                if (variants != null) {
                    for (int i = 0; i < variants.length; i++) {
                        if (variants[i].startsWith(base) && variants[i].endsWith(ext) && variants[i].charAt(x) == '_') {
                            roots.add(FileUtil.getArchiveRoot(new File(locale, variants[i]).toURI().toURL()));
                        }
                    }
                }
            }
        }
        // XXX in principle, could add CP extensions from modules... but probably not necessary
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }

    /**
     * Create a merged filesystem from one writable layer (may be null) and some read-only layers.
     * You should also pass a classpath that can be used to look up resource bundles and icons.
     */
    private static FileSystem mergeFilesystems(FileSystem writableLayer, FileSystem[] readOnlyLayers, final ClassPath cp) {
        if (writableLayer == null) {
            writableLayer = new XMLFileSystem();
        }
        final FileSystem[] layers = new FileSystem[readOnlyLayers.length + 1];
        layers[0] = writableLayer;
        System.arraycopy(readOnlyLayers, 0, layers, 1, readOnlyLayers.length);
        class BadgingMergedFileSystem extends MultiFileSystem {
            private final BadgingSupport status;
            public BadgingMergedFileSystem() {
                super(layers);
                status = new BadgingSupport(this);
                status.setClasspath(cp);
                status.setSuffix("_" + Locale.getDefault());
                // XXX listening?
            }
            public FileSystem.Status getStatus() {
                return status;
            }
        }
        return new BadgingMergedFileSystem();
    }
    
}
