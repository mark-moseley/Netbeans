/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility methods for the module.
 * @author Jesse Glick
 */
public final class Util {
    
    private Util() {}
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project"); // NOI18N
    
    private static final String SFS_VALID_PATH_RE = "(\\p{Alnum}|\\/|_)+"; // NOI18N
    
    // COPIED FROM org.netbeans.modules.project.ant:
    // (except for namespace == null support in findElement)
    // (and support for comments in findSubElements)
    
    /**
     * Search for an XML element in the direct children of a parent.
     * DOM provides a similar method but it does a recursive search
     * which we do not want. It also gives a node list and we want
     * only one result.
     * @param parent a parent element
     * @param name the intended local name
     * @param namespace the intended namespace (or null)
     * @return the one child element with that name, or null if none or more than one
     */
    public static Element findElement(Element parent, String name, String namespace) {
        Element result = null;
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element)l.item(i);
                if ((namespace == null && name.equals(el.getTagName())) ||
                        (namespace != null && name.equals(el.getLocalName()) &&
                        namespace.equals(el.getNamespaceURI()))) {
                    if (result == null) {
                        result = el;
                    } else {
                        return null;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Extract nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    public static String findText(Element parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }
    
    /**
     * Find all direct child elements of an element.
     * More useful than {@link Element#getElementsByTagNameNS} because it does
     * not recurse into recursive child elements.
     * Children which are all-whitespace text nodes or comments are ignored; others cause
     * an exception to be thrown.
     * @param parent a parent element in a DOM tree
     * @return a list of direct child elements (may be empty)
     * @throws IllegalArgumentException if there are non-element children besides whitespace
     */
    public static List/*<Element>*/ findSubElements(Element parent) throws IllegalArgumentException {
        NodeList l = parent.getChildNodes();
        List/*<Element>*/ elements = new ArrayList(l.getLength());
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element)n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text)n).getNodeValue();
                if (text.trim().length() > 0) {
                    throw new IllegalArgumentException("non-ws text encountered in " + parent + ": " + text); // NOI18N
                }
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // OK, ignore
            } else {
                throw new IllegalArgumentException("unexpected non-element child of " + parent + ": " + n); // NOI18N
            }
        }
        return elements;
    }
    
    // CANDIDATES FOR FileUtil (#59311):
    
    /**
     * Creates a URL for a directory on disk.
     * Works correctly even if the directory does not currently exist.
     */
    public static URL urlForDir(File dir) {
        try {
            URL u = FileUtil.normalizeFile(dir).toURI().toURL();
            String s = u.toExternalForm();
            if (s.endsWith("/")) { // NOI18N
                return u;
            } else {
                return new URL(s + "/"); // NOI18N
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Creates a URL for the root of a JAR on disk.
     */
    public static URL urlForJar(File jar) {
        try {
            return FileUtil.getArchiveRoot(FileUtil.normalizeFile(jar).toURI().toURL());
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Creates a URL for a directory on disk or the root of a JAR.
     * Works correctly whether or not the directory or JAR currently exists.
     * Detects whether the file is supposed to be a directory or a JAR.
     */
    public static URL urlForDirOrJar(File location) {
        try {
            URL u = FileUtil.normalizeFile(location).toURI().toURL();
            if (FileUtil.isArchiveFile(u)) {
                u = FileUtil.getArchiveRoot(u);
            } else {
                String us = u.toExternalForm();
                if (!us.endsWith("/")) { // NOI18N
                    u = new URL(us + "/"); // NOI18N
                }
            }
            return u;
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Tries to find {@link Project} in the given directory. If succeeds
     * delegates to {@link ProjectInformation#getDisplayName}. Returns {@link
     * FileUtil#getFileDisplayName} otherwise.
     */
    public static String getDisplayName(FileObject projectDir) {
        try {
            Project p = ProjectManager.getDefault().findProject(projectDir);
            return ProjectUtils.getInformation(p).getDisplayName();
        } catch (IOException e) {
            return FileUtil.getFileDisplayName(projectDir);
        }
    }
    
    /**
     * Normalizes the given value to a regular dotted code name base.
     * @param value to be normalized
     */
    public static String normalizeCNB(String value) {
        StringTokenizer tk = new StringTokenizer(value.toLowerCase(), ".", true); //NOI18N
        StringBuffer normalizedCNB = new StringBuffer();
        boolean delimExpected = false;
        while (tk.hasMoreTokens()) {
            String namePart = tk.nextToken();
            if (!delimExpected) {
                if (namePart.equals(".")) { //NOI18N
                    continue;
                }
                for (int i = 0; i < namePart.length(); i++) {
                    char c = namePart.charAt(i);
                    if (i == 0) {
                        if (!Character.isJavaIdentifierStart(c)) {
                            continue;
                        }
                    } else {
                        if (!Character.isJavaIdentifierPart(c)) {
                            continue;
                        }
                    }
                    normalizedCNB.append(c);
                }
            } else {
                if (namePart.equals(".")) { //NOI18N
                    normalizedCNB.append(namePart);
                }
            }
            delimExpected = !delimExpected;
        }
        // also be sure there is no '.' left at the end of the cnb
        return normalizedCNB.toString().replaceAll("\\.$", ""); // NOI18N
    }
    
    // Stolen/Inspired from java/project PackageViewChildren.isValidPackageName()
    public static boolean isValidCodeNameBase(String name) {
        if (name.length() == 0) {
            return false;
        }
        StringTokenizer tk = new StringTokenizer(name,".",true); //NOI18N
        boolean delimExpected = false;
        while (tk.hasMoreTokens()) {
            String namePart = tk.nextToken();
            if (!delimExpected) {
                if (namePart.equals(".")) { //NOI18N
                    return false;
                }
                for (int i=0; i< namePart.length(); i++) {
                    char c = namePart.charAt(i);
                    if (i == 0) {
                        if (!Character.isJavaIdentifierStart(c)) {
                            return false;
                        }
                    } else {
                        if (!Character.isJavaIdentifierPart(c)) {
                            return false;
                        }
                    }
                }
            } else {
                if (!namePart.equals(".")) { //NOI18N
                    return false;
                }
            }
            delimExpected = !delimExpected;
        }
        return delimExpected;
    }
    
    /**
     * Search for an appropriate localized bundle (i.e.
     * OpenIDE-Module-Localizing-Bundle) entry in the given
     * <code>manifest</code> taking into account branding and localization
     * (using {@link NbBundle#getLocalizingSuffixes}) and returns an
     * appropriate <em>valid</em> {@link LocalizedBundleInfo} instance. By
     * <em>valid</em> it's meant that a found localized bundle contains at
     * least a display name. If <em>valid</em> bundle is not found
     * <code>null</code> is returned.
     *
     * @param sourceDir source directory to be used for as a <em>searching
     *        path</em> for the bundle
     * @param manifest manifest the bundle's path should be extracted from
     * @return localized bundle info for the given project or <code>null</code>
     */
    public static LocalizedBundleInfo findLocalizedBundleInfo(FileObject sourceDir, Manifest manifest) {
        String locBundleResource =
                ManifestManager.getInstance(manifest, false).getLocalizingBundle();
        try {
            if (locBundleResource != null) {
                List/*<FileObject>*/ bundleFOs = new ArrayList();
                for (Iterator it = getPossibleResources(locBundleResource); it.hasNext(); ) {
                    String resource = (String) it.next();
                    FileObject bundleFO = sourceDir.getFileObject(resource);
                    if (bundleFO != null) {
                        bundleFOs.add(bundleFO);
                    }
                }
                if (!bundleFOs.isEmpty()) {
                    Collections.reverse(bundleFOs);
                    return LocalizedBundleInfo.load((FileObject[]) bundleFOs.toArray(new FileObject[bundleFOs.size()]));
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    /**
     * Actually deletages to {@link #findLocalizedBundleInfo(FileObject, Manifest)}.
     */
    public static LocalizedBundleInfo findLocalizedBundleInfo(File projectDir) {
        FileObject sourceDir = FileUtil.toFileObject(new File(projectDir, "src")); // NOI18N
        FileObject manifestFO = FileUtil.toFileObject(new File(projectDir, "manifest.mf")); // NOI18N
        
        LocalizedBundleInfo locInfo = null;
        Manifest mf = getManifest(manifestFO);
        if (sourceDir != null && mf != null) {
            locInfo = findLocalizedBundleInfo(sourceDir, mf);
        }
        return locInfo;
    }
    
    /**
     * The same as {@link #findLocalizedBundleInfo(FileObject, Manifest)} but
     * searching in the given JAR representing a NetBeans module.
     */
    public static LocalizedBundleInfo findLocalizedBundleInfoFromJAR(File binaryProject) {
        try {
            JarFile main = new JarFile(binaryProject);
            try {
                Manifest mf = main.getManifest();
                String locBundleResource =
                        ManifestManager.getInstance(mf, false).getLocalizingBundle();
                if (locBundleResource != null) {
                    List/*<InputStream>*/ bundleISs = new ArrayList();
                    Collection/*<JarFile>*/ extraJarFiles = new ArrayList();
                    try {
                        // Look for locale variant JARs too.
                        // XXX the following could be simplified with #29580:
                        String name = binaryProject.getName();
                        int dot = name.lastIndexOf('.');
                        if (dot == -1) {
                            dot = name.length();
                        }
                        String base = name.substring(0, dot);
                        String suffix = name.substring(dot);
                        Iterator it = NbBundle.getLocalizingSuffixes();
                        while (it.hasNext()) {
                            String infix = (String) it.next();
                            File variant = new File(binaryProject.getParentFile(), "locale" + File.separatorChar + base + infix + suffix); // NOI18N
                            if (variant.isFile()) {
                                JarFile jf = new JarFile(variant);
                                extraJarFiles.add(jf);
                                addBundlesFromJar(jf, bundleISs, locBundleResource);
                            }
                        }
                        // Add main last, since we are about to reverse it:
                        addBundlesFromJar(main, bundleISs, locBundleResource);
                        if (!bundleISs.isEmpty()) {
                            Collections.reverse(bundleISs);
                            return LocalizedBundleInfo.load((InputStream[]) bundleISs.toArray(new InputStream[bundleISs.size()]));
                        }
                    } finally {
                        Iterator it = bundleISs.iterator();
                        while (it.hasNext()) {
                            ((InputStream) it.next()).close();
                        }
                        it = extraJarFiles.iterator();
                        while (it.hasNext()) {
                            ((JarFile) it.next()).close();
                        }
                    }
                }
            } finally {
                main.close();
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    private static void addBundlesFromJar(JarFile jf, List/*<InputStream>*/ bundleISs, String locBundleResource) throws IOException {
        for (Iterator it = getPossibleResources(locBundleResource); it.hasNext(); ) {
            String resource = (String) it.next();
            ZipEntry entry = jf.getEntry(resource);
            if (entry != null) {
                InputStream bundleIS = jf.getInputStream(entry);
                bundleISs.add(bundleIS);
            }
        }
    }
    
    /**
     * Convenience method for loading {@link EditableProperties} from a {@link
     * FileObject}. New items will alphabetizied by key.
     *
     * @param propsFO file representing properties file
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        InputStream propsIS = propsFO.getInputStream();
        EditableProperties props = new EditableProperties(true);
        try {
            props.load(propsIS);
        } finally {
            propsIS.close();
        }
        return props;
    }
    
    /**
     * Convenience method for storing {@link EditableProperties} into a {@link
     * FileObject}.
     *
     * @param propsFO file representing where properties will be stored
     * @param props properties to be stored
     * @exception IOException if properties cannot be written to the file
     */
    public static void storeProperties(FileObject propsFO, EditableProperties props) throws IOException {
        FileLock lock = propsFO.lock();
        try {
            OutputStream os = propsFO.getOutputStream(lock);
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Convenience method for loading {@link EditableManifest} from a {@link
     * FileObject}.
     *
     * @param manifestFO file representing manifest
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static EditableManifest loadManifest(FileObject manifestFO) throws IOException {
        InputStream mfIS = manifestFO.getInputStream();
        try {
            EditableManifest mf = new EditableManifest(mfIS);
            return mf;
        } finally {
            mfIS.close();
        }
    }
    
    /**
     * Convenience method for storing {@link EditableManifest} into a {@link
     * FileObject}.
     *
     * @param manifestFO file representing where manifest will be stored
     * @param em manifest to be stored
     * @exception IOException if manifest cannot be written to the file
     */
    public static void storeManifest(FileObject manifestFO, EditableManifest em) throws IOException {
        FileLock lock = manifestFO.lock();
        try {
            OutputStream os = manifestFO.getOutputStream(lock);
            try {
                em.write(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Find javadoc URL for NetBeans.org modules. May return <code>null</code>.
     */
    public static URL findJavadocForNetBeansOrgModules(final ModuleDependency dep) {
        ModuleEntry entry = dep.getModuleEntry();
        File destDir = entry.getDestDir();
        File nbOrg = null;
        if (destDir.getParent() != null) {
            nbOrg = destDir.getParentFile().getParentFile();
        }
        if (nbOrg == null) {
            throw new IllegalArgumentException("ModuleDependency " + dep +  // NOI18N
                    " doesn't represent nb.org module"); // NOI18N
        }
        File builtJavadoc = new File(nbOrg, "nbbuild/build/javadoc"); // NOI18N
        URL[] javadocURLs = null;
        if (builtJavadoc.exists()) {
            File[] javadocs = builtJavadoc.listFiles();
            javadocURLs = new URL[javadocs.length];
            for (int i = 0; i < javadocs.length; i++) {
                javadocURLs[i] = Util.urlForDirOrJar(javadocs[i]);
            }
        }
        return javadocURLs == null ? null : findJavadocURL(
                dep.getModuleEntry().getCodeNameBase().replace('.', '-'), javadocURLs);
    }
    
    /**
     * Find javadoc URL for the given module dependency using javadoc roots of
     * the given platform. May return <code>null</code>.
     */
    public static URL findJavadoc(final ModuleDependency dep, final NbPlatform platform) {
        String cnbdashes = dep.getModuleEntry().getCodeNameBase().replace('.', '-');
        URL[] roots = platform.getJavadocRoots();
        return roots == null ? null : findJavadocURL(cnbdashes, roots);
    }
    
    public static boolean isValidSFSPath(final String path) {
        return path.matches(SFS_VALID_PATH_RE);
    }
    
    /**
     * Delegates to {@link #addDependency(NbModuleProject, String, int,
     * SpecificationVersion, boolean)}.
     */
    public static void addDependency(final NbModuleProject target,
            final NbModuleProject dependency) throws IOException {
        Util.addDependency(target, dependency.getCodeNameBase(), -1, null, true);
    }
    
    /**
     * Delegates to {@link #addDependency(NbModuleProject, String, int,
     * SpecificationVersion, boolean)}.
     */
    public static void addDependency(final NbModuleProject target,
            final String codeNameBase) throws IOException {
        Util.addDependency(target, codeNameBase, -1, null, true);
    }
    
    /**
     * Makes <code>target</code> project to be dependend on the given
     * <code>dependency</code> project. I.e. adds new &lt;module-dependency&gt;
     * element into target's <em>project.xml</em>. If such a dependency already
     * exists the method does nothing.
     * <p>
     * Note that the method does <strong>not</strong> save the
     * <code>target</code> project. You need to do so explicitly (see {@link
     * ProjectManager#saveProject}).
     *
     * @param codeNameBase codename base.
     * @param releaseVersion release version, if -1 will be taken from the
     *        entry found in platform.
     * @param version {@link SpecificationVersion specification version}, if
     *        <code>null</code>, will be taken from the entry found in the
     *        module's target platform.
     * @param useInCompiler whether this this module needs a
     *        <code>dependency</code> module at a compile time.
     */
    public static void addDependency(final NbModuleProject target,
            final String codeNameBase, final int releaseVersion,
            final SpecificationVersion version, final boolean useInCompiler) throws IOException {
        
        ModuleEntry me = target.getModuleList().getEntry(codeNameBase);
        assert me != null : "Cannot find module with the given codeNameBase (" + // NOI18N
                codeNameBase + ") in the project's universe"; // NOI18N
        
        ProjectXMLManager pxm = new ProjectXMLManager(target);
        
        // firstly check if the dependency is already not there
        Set currentDeps = pxm.getDirectDependencies();
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if (codeNameBase.equals(md.getModuleEntry().getCodeNameBase())) {
                Util.err.log(ErrorManager.INFORMATIONAL, codeNameBase + " already added"); // NOI18N
                return;
            }
        }
        
        ModuleDependency md = new ModuleDependency(me,
                releaseVersion == -1 ? me.getReleaseVersion() : String.valueOf(releaseVersion),
                version == null ? me.getSpecificationVersion() : version.toString(),
                useInCompiler, false);
        pxm.addDependency(md);
    }
    
    private static URL findJavadocURL(final String cnbdashes, final URL[] roots) {
        URL indexURL = null;
        for (int i = 0; i < roots.length; i++) {
            URL root = roots[i];
            try {
                indexURL = Util.normalizeURL(new URL(root, cnbdashes + "/index.html")); // NOI18N
                if (indexURL == null && (root.toExternalForm().indexOf(cnbdashes) != -1)) {
                    indexURL = Util.normalizeURL(new URL(root, "index.html")); // NOI18N
                }
            } catch (MalformedURLException ex) {
                // ignore - let the indexURL == null
            }
            if (indexURL != null) {
                break;
            }
        }
        return indexURL;
    }
    
    private static URL normalizeURL(URL url) {
        // not sure - in some private tests it seems that input
        // jar:file:/home/..../NetBeansAPIs.zip!/..../index.html result in:
        // http://localhost:8082/..../index.html
//        URL resolvedURL = null;
//        FileObject fo = URLMapper.findFileObject(url);
//        if (fo != null) {
//            resolvedURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
//        }
        return URLMapper.findFileObject(url) == null ? null : url;
    }
    
    private static Iterator getPossibleResources(String locBundleResource) {
        String locBundleResourceBase, locBundleResourceExt;
        int idx = locBundleResource.lastIndexOf('.');
        if (idx != -1 && idx > locBundleResource.lastIndexOf('/')) {
            locBundleResourceBase = locBundleResource.substring(0, idx);
            locBundleResourceExt = locBundleResource.substring(idx);
        } else {
            locBundleResourceBase = locBundleResource;
            locBundleResourceExt = "";
        }
        Collection/*<String>*/ resources = new LinkedHashSet();
        for (Iterator it = NbBundle.getLocalizingSuffixes(); it.hasNext(); ) {
            String suffix = (String) it.next();
            String resource = locBundleResourceBase + suffix + locBundleResourceExt;
            resources.add(resource);
            resources.add(resource);
        }
        return resources.iterator();
    }
    
    private static Manifest getManifest(FileObject manifestFO) {
        if (manifestFO != null) {
            try {
                InputStream is = manifestFO.getInputStream();
                try {
                    return new Manifest(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }
    
    /**
     * Property provider which computes one or more properties based on some properties coming
     * from an intermediate evaluator, and is capable of firing changes correctly.
     */
    public static abstract class ComputedPropertyProvider implements PropertyProvider, PropertyChangeListener {
        private final PropertyEvaluator eval;
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        protected ComputedPropertyProvider(PropertyEvaluator eval) {
            this.eval = eval;
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        }
        /** get properties based on the incoming properties */
        protected abstract Map/*<String,String>*/ getProperties(Map/*<String,String>*/ inputPropertyValues);
        /** specify interesting input properties */
        protected abstract Set/*<String>*/ inputProperties();
        public final Map/*<String,String>*/ getProperties() {
            Map/*<String,String>*/ vals = new HashMap();
            Iterator it = inputProperties().iterator();
            while (it.hasNext()) {
                String k = (String) it.next();
                vals.put(k, eval.getProperty(k));
            }
            return getProperties(vals);
        }
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
        public final void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p != null && !inputProperties().contains(p)) {
                return;
            }
            ChangeEvent ev = new ChangeEvent(this);
            Iterator it;
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    return;
                }
                it = new HashSet(listeners).iterator();
            }
            while (it.hasNext()) {
                ((ChangeListener) it.next()).stateChanged(ev);
            }
        }
    }
    
    public static final class UserPropertiesFileProvider implements PropertyProvider, PropertyChangeListener, ChangeListener {
        private final PropertyEvaluator eval;
        private final File basedir;
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        private PropertyProvider delegate;
        public UserPropertiesFileProvider(PropertyEvaluator eval, File basedir) {
            this.eval = eval;
            this.basedir = basedir;
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
            computeDelegate();
        }
        private void computeDelegate() {
            if (delegate != null) {
                delegate.removeChangeListener(this);
            }
            String buildS = eval.getProperty("user.properties.file"); // NOI18N
            if (buildS != null) {
                delegate = PropertyUtils.propertiesFilePropertyProvider(PropertyUtils.resolveFile(basedir, buildS));
                delegate.addChangeListener(this);
            } else {
                /* XXX what should we do?
                delegate = null;
                 */
                delegate = PropertyUtils.globalPropertyProvider();
                delegate.addChangeListener(this);
            }
        }
        public Map getProperties() {
            if (delegate != null) {
                return delegate.getProperties();
            } else {
                return Collections.EMPTY_MAP;
            }
        }
        public void addChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
        public void removeChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
        }
        public void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p == null || p.equals("user.properties.file")) { // NOI18N
                computeDelegate();
                fireChange();
            }
        }
        public void stateChanged(ChangeEvent e) {
            fireChange();
        }
        private void fireChange() {
            ChangeEvent ev = new ChangeEvent(this);
            Iterator it;
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    return;
                }
                it = new HashSet(listeners).iterator();
            }
            while (it.hasNext()) {
                ((ChangeListener) it.next()).stateChanged(ev);
            }
        }
    }
    
    /**
     * Order projects by display name.
     */
    public static Comparator/*<Project>*/ projectDisplayNameComparator() {
        return new Comparator() {
            private final Collator LOC_COLLATOR = Collator.getInstance();
            public int compare(Object o1, Object o2) {
                ProjectInformation i1 = ProjectUtils.getInformation((Project) o1);
                ProjectInformation i2 = ProjectUtils.getInformation((Project) o2);
                int result = LOC_COLLATOR.compare(i1.getDisplayName(), i2.getDisplayName());
                if (result != 0) {
                    return result;
                } else {
                    result = i1.getName().compareTo(i2.getName());
                    if (result != 0) {
                        return result;
                    } else {
                        return System.identityHashCode(o1) - System.identityHashCode(o2);
                    }
                }
            }
        };
    }
    
    /**
     * Returns {@link NbModuleTypeProvider.NbModuleType} from a project's lookup.
     */
    public static NbModuleTypeProvider.NbModuleType getModuleType(final NbModuleProject project) {
        NbModuleTypeProvider provider = (NbModuleTypeProvider) project.getLookup().lookup(NbModuleTypeProvider.class);
        assert provider != null : "has NbModuleTypeProvider in the lookup";
        return provider.getModuleType();
    }
    
}
