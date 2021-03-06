/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.apisupport.project;

import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
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
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility methods for the module.
 *
 * @author Jesse Glick, Martin Krauskopf
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
    public static List<Element> findSubElements(Element parent) throws IllegalArgumentException {
        NodeList l = parent.getChildNodes();
        List<Element> elements = new ArrayList<Element>(l.getLength());
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

    /**
     * Convert an XML fragment from one namespace to another.
     */
    public static Element translateXML(Element from, String namespace) {
        Element to = from.getOwnerDocument().createElementNS(namespace, from.getLocalName());
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nl.item(i);
            Node newNode;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                newNode = translateXML((Element) node, namespace);
            } else {
                newNode = node.cloneNode(true);
            }
            to.appendChild(newNode);
        }
        NamedNodeMap m = from.getAttributes();
        for (int i = 0; i < m.getLength(); i++) {
            Node attr = m.item(i);
            to.setAttribute(attr.getNodeName(), attr.getNodeValue());
        }
        return to;
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
        if (projectDir.isFolder()) {
            try {
                Project p = ProjectManager.getDefault().findProject(projectDir);
                if (p != null) {
                    return ProjectUtils.getInformation(p).getDisplayName();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return FileUtil.getFileDisplayName(projectDir);
    }
    
    /**
     * Normalizes the given value to a regular dotted code name base.
     * @param value to be normalized
     */
    public static String normalizeCNB(String value) {
        StringTokenizer tk = new StringTokenizer(value.toLowerCase(Locale.ENGLISH), ".", true); // NOI18N
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

    /**
     * Check whether a given name can serve as a legal <ol>
     * <li>Java class name
     * <li>Java package name
     * <li>NB module code name base
     * </ol>
     */
    public static boolean isValidJavaFQN(String name) {
        if (name.length() == 0) {
            return false;
        }
        StringTokenizer tk = new StringTokenizer(name,".",true); //NOI18N
        boolean delimExpected = false;
        while (tk.hasMoreTokens()) {
            String namePart = tk.nextToken();
            if (delimExpected ^ namePart.equals(".")) { // NOI18N
                return false;
            }
            if (!delimExpected && !Utilities.isJavaIdentifier(namePart)) {
                return false;
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
                List<FileObject> bundleFOs = new ArrayList<FileObject>();
                for (Iterator it = getPossibleResources(locBundleResource); it.hasNext(); ) {
                    String resource = (String) it.next();
                    FileObject bundleFO = sourceDir.getFileObject(resource);
                    if (bundleFO != null) {
                        bundleFOs.add(bundleFO);
                    }
                }
                if (!bundleFOs.isEmpty()) {
                    Collections.reverse(bundleFOs);
                    return LocalizedBundleInfo.load(bundleFOs.toArray(new FileObject[bundleFOs.size()]));
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
        FileObject projectDirFO = FileUtil.toFileObject(projectDir);
        if (projectDirFO == null) {
            return null;
        }
        NbModuleProject p;
        try {
            p = (NbModuleProject) ProjectManager.getDefault().findProject(projectDirFO);
        } catch (IOException e) {
            return null;
        }
        if (p == null) {
            return null;
        }
        String src = p.evaluator().getProperty("src.dir"); // NOI18N
        assert src != null : "Cannot evaluate src.dir property for " + p;
        File srcF = FileUtil.normalizeFile(new File(projectDir, src));
        FileObject sourceDir = FileUtil.toFileObject(srcF);
        FileObject manifestFO = FileUtil.toFileObject(FileUtil.normalizeFile(new File(projectDir, "manifest.mf"))); // NOI18N
        
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
                    List<InputStream> bundleISs = new ArrayList<InputStream>();
                    Collection<JarFile> extraJarFiles = new ArrayList<JarFile>();
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
                        Iterator<String> it = NbBundle.getLocalizingSuffixes();
                        while (it.hasNext()) {
                            String infix = it.next();
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
                            return LocalizedBundleInfo.load(bundleISs.toArray(new InputStream[bundleISs.size()]));
                        }
                    } finally {
                        for (InputStream bundleIS : bundleISs) {
                            bundleIS.close();
                        }
                        for (JarFile jarFile : extraJarFiles) {
                            jarFile.close();
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
    
    private static void addBundlesFromJar(JarFile jf, List<InputStream> bundleISs, String locBundleResource) throws IOException {
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
            return new EditableManifest(mfIS);
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
     * Find Javadoc URL for NetBeans.org modules. May return <code>null</code>.
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
     * Find Javadoc URL for the given module dependency using Javadoc roots of
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
     * Delegates to {@link #addDependency(NbModuleProject, String)}.
     */
    public static boolean addDependency(final NbModuleProject target,
            final NbModuleProject dependency) throws IOException {
        return addDependency(target, dependency.getCodeNameBase());
    }
    
    /**
     * Delegates to {@link Util#addDependency(NbModuleProject, String, String,
     * SpecificationVersion, boolean)}.
     */
    public static boolean addDependency(final NbModuleProject target,
            final String codeNameBase) throws IOException {
        return Util.addDependency(target, codeNameBase, null, null, true);
    }
    
    /**
     * Makes <code>target</code> project to be dependend on the given
     * <code>dependency</code> project. I.e. adds new &lt;module-dependency&gt;
     * element into target's <em>project.xml</em>. If such a dependency already
     * exists the method does nothing. If the given code name base cannot be
     * found in the module's universe the method logs informational message and
     * does nothing otherwise.
     * <p>
     * Note that the method does <strong>not</strong> save the
     * <code>target</code> project. You need to do so explicitly (see {@link
     * ProjectManager#saveProject}).
     *
     * @param codeNameBase codename base.
     * @param releaseVersion release version, if <code>null</code> will be taken from the
     *        entry found in platform.
     * @param version {@link SpecificationVersion specification version}, if
     *        <code>null</code>, will be taken from the entry found in the
     *        module's target platform.
     * @param useInCompiler whether this this module needs a
     *        <code>dependency</code> module at a compile time.
     * @return true if a dependency was successfully added; false otherwise
     *         (e.g. when such dependency already exists)
     */
    public static boolean addDependency(final NbModuleProject target,
            final String codeNameBase, final String releaseVersion,
            final SpecificationVersion version, final boolean useInCompiler) throws IOException {
        ModuleEntry me = target.getModuleList().getEntry(codeNameBase);
        if (me == null) { // ignore semi-silently (#72611)
            Util.err.log(ErrorManager.INFORMATIONAL, "Trying to add " + codeNameBase + // NOI18N
                    " which cannot be found in the module's universe."); // NOI18N
            return false;
        }
        
        ProjectXMLManager pxm = new ProjectXMLManager(target);
        
        // firstly check if the dependency is already not there
        Set currentDeps = pxm.getDirectDependencies();
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if (codeNameBase.equals(md.getModuleEntry().getCodeNameBase())) {
                Util.err.log(ErrorManager.INFORMATIONAL, codeNameBase + " already added"); // NOI18N
                return false;
            }
        }
        
        ModuleDependency md = new ModuleDependency(me,
                (releaseVersion == null) ?  me.getReleaseVersion() : releaseVersion,
                version == null ? me.getSpecificationVersion() : version.toString(),
                useInCompiler, false);
        pxm.addDependency(md);
        return true;
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
        Collection<String> resources = new LinkedHashSet<String>();
        for (Iterator<String> it = NbBundle.getLocalizingSuffixes(); it.hasNext(); ) {
            String suffix = it.next();
            String resource = locBundleResourceBase + suffix + locBundleResourceExt;
            resources.add(resource);
            resources.add(resource);
        }
        return resources.iterator();
    }
    
    public static Manifest getManifest(FileObject manifestFO) {
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
        private final ChangeSupport cs = new ChangeSupport(this);
        protected ComputedPropertyProvider(PropertyEvaluator eval) {
            this.eval = eval;
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        }
        /** get properties based on the incoming properties */
        protected abstract Map<String,String> getProperties(Map<String,String> inputPropertyValues);
        /** specify interesting input properties */
        protected abstract Set<String> inputProperties();
        public final Map<String,String> getProperties() {
            Map<String,String> vals = new HashMap<String, String>();
            for (String k : inputProperties()) {
                vals.put(k, eval.getProperty(k));
            }
            return getProperties(vals);
        }
        public final void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }
        public final void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        public final void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p != null && !inputProperties().contains(p)) {
                return;
            }
            cs.fireChange();
        }
    }
    
    public static final class UserPropertiesFileProvider implements PropertyProvider, PropertyChangeListener, ChangeListener {
        private final PropertyEvaluator eval;
        private final File basedir;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final ChangeListener listener = WeakListeners.change(this, null);
        private PropertyProvider delegate;
        public UserPropertiesFileProvider(PropertyEvaluator eval, File basedir) {
            this.eval = eval;
            this.basedir = basedir;
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
            computeDelegate();
        }
        private void computeDelegate() {
            if (delegate != null) {
                delegate.removeChangeListener(listener);
            }
            String buildS = eval.getProperty("user.properties.file"); // NOI18N
            if (buildS != null) {
                delegate = PropertyUtils.propertiesFilePropertyProvider(PropertyUtils.resolveFile(basedir, buildS));
            } else {
                /* XXX what should we do?
                delegate = null;
                 */
                delegate = PropertyUtils.globalPropertyProvider();
            }
            delegate.addChangeListener(listener);
        }
        public Map<String,String> getProperties() {
            if (delegate != null) {
                return delegate.getProperties();
            } else {
                return Collections.emptyMap();
            }
        }
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        public void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p == null || p.equals("user.properties.file")) { // NOI18N
                computeDelegate();
                changeSupport.fireChange();
            }
        }
        public void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }
    }
    
    /**
     * Order projects by display name.
     */
    public static Comparator<Project> projectDisplayNameComparator() {
        return new Comparator<Project>() {
            private final Collator LOC_COLLATOR = Collator.getInstance();
            public int compare(Project o1, Project o2) {
                ProjectInformation i1 = ProjectUtils.getInformation(o1);
                ProjectInformation i2 = ProjectUtils.getInformation(o2);
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
     * Returns {@link NbModuleProvider.NbModuleType} from a project's lookup.
     */
    public static NbModuleProvider.NbModuleType getModuleType(final Project project) {
        NbModuleProvider provider = project.getLookup().lookup(NbModuleProvider.class);
        assert provider != null : "has NbModuleProvider in the lookup";
        return provider.getModuleType();
    }
    
    /**
     * Finds all available packages in a given project directory. Found entries
     * are in the form of a regular java package (x.y.z).
     * 
     * @param prjDir directory containing project to be scanned
     * @return a set of found packages
     */
    public static SortedSet<String> scanProjectForPackageNames(final File prjDir) {
        NbModuleProject project = null;
        // find all available public packages in classpath extensions
        FileObject source = FileUtil.toFileObject(prjDir);
        if (source != null) { // ??
            try {
                project = (NbModuleProject) ProjectManager.getDefault().findProject(source);
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        if (project == null) {
            return new TreeSet<String>(Collections.<String>emptySet());
        }
        
        SortedSet<String> availablePublicPackages = new TreeSet<String>();
        // find all available public packages in a source root
        Set<FileObject> pkgs = new HashSet<FileObject>();
        FileObject srcDirFO = project.getSourceDirectory();
        Util.scanForPackages(pkgs, srcDirFO, "java"); // NOI18N
        for (FileObject pkg : pkgs) {
            if (srcDirFO.equals(pkg)) { // default package #71532
                continue;
            }
            String pkgS = PropertyUtils.relativizeFile(FileUtil.toFile(srcDirFO), FileUtil.toFile(pkg));
            availablePublicPackages.add(pkgS.replace('/', '.'));
        }
        
        String[] libsPaths = new ProjectXMLManager(project).getBinaryOrigins();
        for (int i = 0; i < libsPaths.length; i++) {
            scanJarForPackageNames(availablePublicPackages, project.getHelper().resolveFile(libsPaths[i]));
        }
        
        // #72669: remove invalid packages.
        Iterator it = availablePublicPackages.iterator();
        while (it.hasNext()) {
            String pkg = (String) it.next();
            if (!Util.isValidJavaFQN(pkg)) {
                it.remove();
            }
        }
        return availablePublicPackages;
    }
    
    /**
     * Scans a given jar file for all packages which contains at least one
     * .class file. Found entries are in the form of a regular java package
     * (x.y.z).
     * 
     * @param jarFile jar file to be scanned
     * @param packages a set into which found packages will be added
     */
    public static void scanJarForPackageNames(final Set<String> packages, final File jarFile) {
        FileObject jarFileFO = FileUtil.toFileObject(jarFile);
        if (jarFileFO == null) {
            // Broken classpath entry, perhaps.
            return;
        }
        FileObject root = FileUtil.getArchiveRoot(jarFileFO);
        if (root == null) {
            // Not really a JAR?
            return;
        }
        Set<FileObject> pkgs = new HashSet<FileObject>();
        Util.scanForPackages(pkgs, root, "class"); // NOI18N
        for (FileObject pkg : pkgs) {
            if (root.equals(pkg)) { // default package #71532
                continue;
            }
            String pkgS = pkg.getPath();
            packages.add(pkgS.replace('/', '.'));
        }
    }
    
    /**
     * Scan recursively through all folders in the given <code>dir</code> and
     * add every directory/package, which contains at least one file with the
     * given extension (probably class or java), into the given
     * <code>validPkgs</code> set. Added entries are in the form of regular java
     * package (x.y.z)
     */
    private static void scanForPackages(final Set<FileObject> validPkgs, final FileObject dir, final String ext) {
        if (dir == null) {
            return;
        }
        for (Enumeration en1 = dir.getFolders(false); en1.hasMoreElements(); ) {
            FileObject subDir = (FileObject) en1.nextElement();
            if (VisibilityQuery.getDefault().isVisible(subDir)) {
                scanForPackages(validPkgs, subDir, ext);
            }
        }
        for (Enumeration en2 = dir.getData(false); en2.hasMoreElements(); ) {
            FileObject kid = (FileObject) en2.nextElement();
            if (kid.hasExt(ext) && Utilities.isJavaIdentifier(kid.getName())) {
                // at least one class inside directory -> valid package
                validPkgs.add(dir);
                break;
            }
        }
    }
    
    /**
     * when ever there is need for non-java files creation or lookup,
     * use this method to get the right location for all projects. 
     * Eg. maven places resources not next to the java files.
     */ 
    public static FileObject getResourceDirectory(Project prj) {
        Sources srcs = ProjectUtils.getSources(prj);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (grps != null && grps.length > 0) {
            return grps[0].getRootFolder();
        }
        // fallback to sources..
        NbModuleProvider prov = prj.getLookup().lookup(NbModuleProvider.class);
        assert prov != null;
        return prov.getSourceDirectory();
    }
    
}
