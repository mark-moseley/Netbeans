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

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * Represents one NetBeans platform, i.e. installation of the NB platform or IDE
 * or some derivative product.
 * Has a code id and can have associated sources and Javadoc, just like e.g. Java platforms.
 *
 * @author Jesse Glick
 */
public final class NbPlatform {
    
    private static final String PLATFORM_PREFIX = "nbplatform."; // NOI18N
    private static final String PLATFORM_DEST_DIR_SUFFIX = ".netbeans.dest.dir"; // NOI18N
    private static final String PLATFORM_LABEL_SUFFIX = ".label"; // NOI18N
    private static final String PLATFORM_SOURCES_SUFFIX = ".sources"; // NOI18N
    private static final String PLATFORM_JAVADOC_SUFFIX = ".javadoc"; // NOI18N
    private static final String PLATFORM_HARNESS_DIR_SUFFIX = ".harness.dir"; // NOI18N
    public static final String PLATFORM_ID_DEFAULT = "default"; // NOI18N
    
    public static final String PROP_SOURCE_ROOTS = "sourceRoots"; // NOI18N
    
    private static Set<NbPlatform> platforms;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    // should proceed in chronological order so we can do compatibility tests with >=
    /** Unknown version - platform might be invalid, or just predate any 5.0 release version. */
    public static final int HARNESS_VERSION_UNKNOWN = 0;
    /** Harness version found in 5.0. */
    public static final int HARNESS_VERSION_50 = 1;
    /** Harness version found in 5.0 update 1 and 5.5. */
    public static final int HARNESS_VERSION_50u1 = 2;
    /** Harness version found in 5.5 update 1. */
    public static final int HARNESS_VERSION_55u1 = 3;
    /** Harness version found in 6.0. */
    public static final int HARNESS_VERSION_60 = 4;
    /** Harness version found in 6.1. */
    public static final int HARNESS_VERSION_61 = 5;
    
    /**
     * Reset cached info so unit tests can start from scratch.
     */
    public static void reset() {
        platforms = null;
    }
    
    /**
     * Get a set of all registered platforms.
     */
    public static synchronized Set<NbPlatform> getPlatforms() {
        return new HashSet<NbPlatform>(getPlatformsInternal());
    }

    private static Set<NbPlatform> getPlatformsInternal() {
        if (platforms == null) {
            platforms = new HashSet<NbPlatform>();
            Map<String,String> p = PropertyUtils.sequentialPropertyEvaluator(null, PropertyUtils.globalPropertyProvider()).getProperties();
            if (p == null) { // #115909
                p = Collections.emptyMap();
            }
            boolean foundDefault = false;
            for (Map.Entry<String,String> entry : p.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(PLATFORM_PREFIX) && key.endsWith(PLATFORM_DEST_DIR_SUFFIX)) {
                    String id = key.substring(PLATFORM_PREFIX.length(), key.length() - PLATFORM_DEST_DIR_SUFFIX.length());
                    String label = p.get(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX);
                    String destdir = entry.getValue();
                    String harnessdir = p.get(PLATFORM_PREFIX + id + PLATFORM_HARNESS_DIR_SUFFIX);
                    String sources = p.get(PLATFORM_PREFIX + id + PLATFORM_SOURCES_SUFFIX);
                    String javadoc = p.get(PLATFORM_PREFIX + id + PLATFORM_JAVADOC_SUFFIX);
                    File destdirF = FileUtil.normalizeFile(new File(destdir));
                    File harness;
                    if (harnessdir != null) {
                        harness = FileUtil.normalizeFile(new File(harnessdir));
                    } else {
                        harness = findHarness(destdirF);
                    }
                    platforms.add(new NbPlatform(id, label, destdirF, harness, findURLs(sources), findURLs(javadoc)));
                    foundDefault |= id.equals(PLATFORM_ID_DEFAULT);
                }
            }
            if (!foundDefault) {
                File loc = defaultPlatformLocation();
                if (loc != null) {
                    platforms.add(new NbPlatform(PLATFORM_ID_DEFAULT, null, loc, findHarness(loc), new URL[0], new URL[0]));
                }
            }
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Util.err.log("NbPlatform initial list: " + platforms);
            }
        }
        return platforms;
    }
    
    /**
     * Get the default platform.
     * @return the default platform, if there is one (usually should be)
     */
    public static NbPlatform getDefaultPlatform() {
        return NbPlatform.getPlatformByID(PLATFORM_ID_DEFAULT);
    }
    
    /**
     * Get the location of the default platform, or null.
     */
    public static File defaultPlatformLocation() {
        // XXX cache the result?
        // Semi-arbitrary platform* component.
        File bootJar = InstalledFileLocator.getDefault().locate("core/core.jar", "org.netbeans.core.startup", false); // NOI18N
        if (bootJar == null) {
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Util.err.log("no core/core.jar");
            }
            return null;
        }
        // Semi-arbitrary harness component.
        File harnessJar = InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-apisupport-harness.jar", "org.netbeans.modules.apisupport.harness", false); // NOI18N
        if (harnessJar == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot resolve default platform. " + // NOI18N
                    "Probably either \"org.netbeans.modules.apisupport.harness\" module is missing or is corrupted."); // NOI18N
            return null;
        }
        File loc = harnessJar.getParentFile().getParentFile().getParentFile();
        try {
            if (!loc.getCanonicalFile().equals(bootJar.getParentFile().getParentFile().getParentFile().getCanonicalFile())) {
                // Unusual installation structure, punt.
                if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    Util.err.log("core.jar & harness.jar locations do not match: " + bootJar + " vs. " + harnessJar);
                }
                return null;
            }
        } catch (IOException x) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, x);
        }
        // Looks good.
        return FileUtil.normalizeFile(loc);
    }
    
    /**
     * Get any sources which should by default be associated with the default platform.
     */
    private static URL[] defaultPlatformSources(File loc) {
        if (loc.getName().equals("netbeans") && loc.getParentFile().getName().equals("nbbuild")) { // NOI18N
            try {
                return new URL[] {loc.getParentFile().getParentFile().toURI().toURL()};
            } catch (MalformedURLException e) {
                assert false : e;
            }
        }
        return new URL[0];
    }
    
    /**
     * Get any Javadoc which should by default be associated with the default platform.
     */
    private static URL[] defaultPlatformJavadoc() {
        File apidocsZip = InstalledFileLocator.getDefault().locate("docs/NetBeansAPIs.zip", "org.netbeans.modules.apisupport.apidocs", true); // NOI18N
        if (apidocsZip != null) {
            return new URL[] {FileUtil.urlForArchiveOrDir(apidocsZip)};
        } else {
            return new URL[0];
        }
    }
    
    /**
     * Find a platform by its ID.
     * @param id an ID (as in {@link #getID})
     * @return the platform with that ID, or null
     */
    public static synchronized NbPlatform getPlatformByID(String id) {
        for (NbPlatform p : getPlatformsInternal()) {
            if (p.getID().equals(id)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Find a platform by its installation directory.
     * If there is a registered platform for that directory, returns it.
     * Otherwise will create an anonymous platform ({@link #getID} will be null).
     * An anonymous platform might have sources associated with it;
     * currently this will be true in case the dest dir is nbbuild/netbeans/ inside a netbeans.org checkout.
     * @param the installation directory (as in {@link #getDestDir})
     * @return the platform with that destination directory
     */
    public static synchronized NbPlatform getPlatformByDestDir(File destDir) {
        for (NbPlatform p : getPlatformsInternal()) {
            if (p.getDestDir().equals(destDir)) {
                return p;
            }
        }
        URL[] sources = new URL[0];
        if (destDir.getName().equals("netbeans")) { // NOI18N
            File parent = destDir.getParentFile();
            if (parent != null && parent.getName().equals("nbbuild")) { // NOI18N
                File superparent = parent.getParentFile();
                if (superparent != null && ModuleList.isNetBeansOrg(superparent)) {
                    sources = new URL[] {FileUtil.urlForArchiveOrDir(superparent)};
                }
            }
        }
        // XXX might also check OpenProjectList for NbModuleProject's and/or SuiteProject's with a matching
        // dest dir and look up property 'sources' to use; TBD whether Javadoc could also be handled in a
        // similar way
        return new NbPlatform(null, null, destDir, findHarness(destDir), sources, new URL[0]);
    }
    
    /**
     * Find the location of the harness inside a platform.
     * Guaranteed to be a child directory (but might not exist yet).
     */
    private static File findHarness(File destDir) {
        File[] kids = destDir.listFiles();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                if (isHarness(kids[i])) {
                    return kids[i];
                }
            }
        }
        return new File(destDir, "harness"); // NOI18N
    }
    
    /**
     * Check whether a given directory is really a valid harness.
     */
    public static boolean isHarness(File dir) {
        return new File(dir, "modules" + File.separatorChar + "org-netbeans-modules-apisupport-harness.jar").isFile(); // NOI18N
    }
    
    /**
     * Returns whether the platform within the given directory is already
     * registered.
     */
    public static synchronized boolean contains(File destDir) {
        boolean contains = false;
        for (NbPlatform p : getPlatformsInternal()) {
            if (p.getDestDir().equals(destDir)) {
                contains = true;
                break;
            }
        }
        return contains;
    }
    
    /**
     * Register a new platform.
     * @param id unique ID string for the platform
     * @param destdir destination directory (i.e. top-level directory beneath which there are clusters)
     * @param label display label
     * @return the created platform
     * @throws IOException in case of problems (e.g. destination directory does not exist)
     */
    public static NbPlatform addPlatform(final String id, final File destdir, final String label) throws IOException {
        return addPlatform(id, destdir, findHarness(destdir), label);
    }
    
    /**
     * Register a new platform.
     * @param id unique ID string for the platform
     * @param destdir destination directory (i.e. top-level directory beneath which there are clusters)
     * @param harness harness directory
     * @param label display label
     * @return the created platform
     * @throws IOException in case of problems (e.g. destination directory does not exist)
     */
    public static NbPlatform addPlatform(final String id, final File destdir, final File harness, final String label) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    if (getPlatformByID(id) != null) {
                        throw new IOException("ID " + id + " already taken");
                    }
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    String plafDestDir = PLATFORM_PREFIX + id + PLATFORM_DEST_DIR_SUFFIX;
                    props.setProperty(plafDestDir, destdir.getAbsolutePath());
                    if (!destdir.isDirectory()) {
                        throw new FileNotFoundException(destdir.getAbsolutePath());
                    }
                    storeHarnessLocation(id, destdir, harness, props);
                    props.setProperty(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX, label);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        NbPlatform plaf = new NbPlatform(id, label, FileUtil.normalizeFile(destdir), harness,
                findURLs(null), findURLs(null));
        synchronized (NbPlatform.class) {
            getPlatformsInternal().add(plaf);
        }
        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            Util.err.log("NbPlatform added: " + plaf);
        }
        return plaf;
    }
    
    private static void storeHarnessLocation(String id, File destdir, File harness, EditableProperties props) {
        String harnessDirKey = PLATFORM_PREFIX + id + PLATFORM_HARNESS_DIR_SUFFIX;
        if (harness.equals(findHarness(destdir))) {
            // Common case.
            String plafDestDir = PLATFORM_PREFIX + id + PLATFORM_DEST_DIR_SUFFIX;
            props.setProperty(harnessDirKey, "${" + plafDestDir + "}/" + harness.getName()); // NOI18N
        } else if (getDefaultPlatform() != null && harness.equals(getDefaultPlatform().getHarnessLocation())) {
            // Also common.
            props.setProperty(harnessDirKey, "${" + PLATFORM_PREFIX + PLATFORM_ID_DEFAULT + PLATFORM_HARNESS_DIR_SUFFIX + "}"); // NOI18N
        } else {
            // Some random location.
            props.setProperty(harnessDirKey, harness.getAbsolutePath());
        }
    }
    
    public static void removePlatform(final NbPlatform plaf) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_DEST_DIR_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_HARNESS_DIR_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_LABEL_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_SOURCES_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_JAVADOC_SUFFIX);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        synchronized (NbPlatform.class) {
            getPlatformsInternal().remove(plaf);
        }
        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            Util.err.log("NbPlatform removed: " + plaf);
        }
    }
    
    private final String id;
    private String label;
    private File nbdestdir;
    private File harness;
    private URL[] sourceRoots;
    private URL[] javadocRoots;
    private List<ModuleList> listsForSources;
    private int harnessVersion = -1;
    
    private NbPlatform(String id, String label, File nbdestdir, File harness, URL[] sources, URL[] javadoc) {
        this.id = id;
        this.label = label;
        this.nbdestdir = nbdestdir;
        this.harness = harness;
        this.sourceRoots = sources;
        this.javadocRoots = javadoc;
    }
    
    static URL[] findURLs(final String path) {
        if (path == null) {
            return new URL[0];
        }
        String[] pieces = PropertyUtils.tokenizePath(path);
        URL[] urls = new URL[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            // XXX perhaps also support http: URLs somehow?
            urls[i] = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(new File(pieces[i])));
        }
        return urls;
    }
    
    /**
     * Get a unique ID for this platform.
     * Used e.g. in <code>nbplatform.active</code> in <code>platform.properties</code>.
     * @return a unique ID, or <code>null</code> for <em>anonymous</em>
     *         platforms (see {@link #getPlatformByDestDir}).
     */
    public String getID() {
        return id;
    }
    
    /**
     * Check if this is the default platform.
     * @return true for the one default platform
     */
    public boolean isDefault() {
        return PLATFORM_ID_DEFAULT.equals(id);
    }
    
    /**
     * Get a display label suitable for the user.
     * If not set, {@link #computeDisplayName} is used.
     * The {@link #isDefault default platform} is specially marked.
     * @return a display label
     */
    public String getLabel() {
        if (label == null) {
            try {
                label = isValid() ? computeDisplayName(nbdestdir) :
                    NbBundle.getMessage(NbPlatform.class, "MSG_InvalidPlatform",  // NOI18N
                        getDestDir().getAbsolutePath());
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                label = nbdestdir.getAbsolutePath();
            }
        }
        if (isDefault()) {
            return NbBundle.getMessage(NbPlatform.class, "LBL_default_platform", label);
        } else {
            return label;
        }
    }
    
    /**
     * Get the installation directory.
     * @return the installation directory
     */
    public File getDestDir() {
        return nbdestdir;
    }
    
    public void setDestDir(File destdir) {
        this.nbdestdir = destdir;
        // XXX write build.properties too
    }
    
    /**
     * Get associated source roots for this platform.
     * Each root could be a netbeans.org source checkout or a module suite project directory.
     * @return a list of source root URLs (may be empty but not null)
     */
    public URL[] getSourceRoots() {
        if (sourceRoots.length == 0 && isDefault()) {
            return defaultPlatformSources(getDestDir());
        } else {
            return sourceRoots;
        }
    }
    
    private void maybeUpdateDefaultPlatformSources() {
        if (sourceRoots.length == 0 && isDefault()) {
            sourceRoots = defaultPlatformSources(getDestDir());
            pcs.firePropertyChange(PROP_SOURCE_ROOTS, null, null);
        }
    }
    
    /**
     * Add given source root to the current source root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    public void addSourceRoot(URL root) throws IOException {
        maybeUpdateDefaultPlatformSources();
        URL[] newSourceRoots = new URL[sourceRoots.length + 1];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[sourceRoots.length] = root;
        setSourceRoots(newSourceRoots);
    }
    
    /**
     * Remove given source roots from the current source root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    public void removeSourceRoots(URL[] urlsToRemove) throws IOException {
        maybeUpdateDefaultPlatformSources();
        Collection<URL> newSources = new ArrayList<URL>(Arrays.asList(sourceRoots));
        newSources.removeAll(Arrays.asList(urlsToRemove));
        URL[] sources = new URL[newSources.size()];
        setSourceRoots(newSources.toArray(sources));
    }
    
    public void moveSourceRootUp(int indexToUp) throws IOException {
        maybeUpdateDefaultPlatformSources();
        if (indexToUp <= 0) {
            return; // nothing needs to be done
        }
        URL[] newSourceRoots = new URL[sourceRoots.length];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[indexToUp - 1] = sourceRoots[indexToUp];
        newSourceRoots[indexToUp] = sourceRoots[indexToUp - 1];
        setSourceRoots(newSourceRoots);
    }
    
    public void moveSourceRootDown(int indexToDown) throws IOException {
        maybeUpdateDefaultPlatformSources();
        if (indexToDown >= (sourceRoots.length - 1)) {
            return; // nothing needs to be done
        }
        URL[] newSourceRoots = new URL[sourceRoots.length];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[indexToDown + 1] = sourceRoots[indexToDown];
        newSourceRoots[indexToDown] = sourceRoots[indexToDown + 1];
        setSourceRoots(newSourceRoots);
    }
    
    public void setSourceRoots(URL[] roots) throws IOException {
        putGlobalProperty(
                PLATFORM_PREFIX + getID() + PLATFORM_SOURCES_SUFFIX,
                urlsToAntPath(roots));
        sourceRoots = roots;
        pcs.firePropertyChange(PROP_SOURCE_ROOTS, null, null);
        listsForSources = null;
    }
    
    /**
     * Get associated Javadoc roots for this platform.
     * Each root may contain some Javadoc sets in the usual format as subdirectories,
     * where the subdirectory is named acc. to the code name base of the module it
     * is documenting (using '-' in place of '.').
     * @return a list of Javadoc root URLs (may be empty but not null)
     */
    public URL[] getJavadocRoots() {
        if (javadocRoots.length == 0 && isDefault()) {
            return defaultPlatformJavadoc();
        } else {
            return javadocRoots;
        }
    }
    
    private void maybeUpdateDefaultPlatformJavadoc() {
        if (javadocRoots.length == 0 && isDefault()) {
            javadocRoots = defaultPlatformJavadoc();
        }
    }
    
    /**
     * Add given javadoc root to the current javadoc root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    public void addJavadocRoot(URL root) throws IOException {
        maybeUpdateDefaultPlatformJavadoc();
        URL[] newJavadocRoots = new URL[javadocRoots.length + 1];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[javadocRoots.length] = root;
        setJavadocRoots(newJavadocRoots);
    }
    
    /**
     * Remove given javadoc roots from the current javadoc root list and save
     * the result into the global properties in the <em>userdir</em> (see
     * {@link PropertyUtils#putGlobalProperties})
     */
    public void removeJavadocRoots(URL[] urlsToRemove) throws IOException {
        maybeUpdateDefaultPlatformJavadoc();
        Collection<URL> newJavadocs = new ArrayList<URL>(Arrays.asList(javadocRoots));
        newJavadocs.removeAll(Arrays.asList(urlsToRemove));
        URL[] javadocs = new URL[newJavadocs.size()];
        setJavadocRoots(newJavadocs.toArray(javadocs));
    }
    
    public void moveJavadocRootUp(int indexToUp) throws IOException {
        maybeUpdateDefaultPlatformJavadoc();
        if (indexToUp <= 0) {
            return; // nothing needs to be done
        }
        URL[] newJavadocRoots = new URL[javadocRoots.length];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[indexToUp - 1] = javadocRoots[indexToUp];
        newJavadocRoots[indexToUp] = javadocRoots[indexToUp - 1];
        setJavadocRoots(newJavadocRoots);
    }
    
    public void moveJavadocRootDown(int indexToDown) throws IOException {
        maybeUpdateDefaultPlatformJavadoc();
        if (indexToDown >= (javadocRoots.length - 1)) {
            return; // nothing needs to be done
        }
        URL[] newJavadocRoots = new URL[javadocRoots.length];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[indexToDown + 1] = javadocRoots[indexToDown];
        newJavadocRoots[indexToDown] = javadocRoots[indexToDown + 1];
        setJavadocRoots(newJavadocRoots);
    }
    
    public void setJavadocRoots(URL[] roots) throws IOException {
        putGlobalProperty(
                PLATFORM_PREFIX + getID() + PLATFORM_JAVADOC_SUFFIX,
                urlsToAntPath(roots));
        javadocRoots = roots;
    }
    
    /**
     * Test whether this platform is valid or not. See
     * {@link #isPlatformDirectory}
     */
    public boolean isValid() {
        return NbPlatform.isPlatformDirectory(getDestDir());
    }
    
    static String urlsToAntPath(final URL[] urls) {
        return ClassPathSupport.createClassPath(urls).toString(ClassPath.PathConversionMode.WARN);
    }
    
    private void putGlobalProperty(final String key, final String value) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    if ("".equals(value)) { // NOI18N
                        props.remove(key);
                    } else {
                        props.setProperty(key, value);
                    }
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Find sources for a module JAR file contained in this destination directory.
     * @param jar a JAR file in the destination directory
     * @return the directory of sources for this module (a project directory), or null
     */
    public File getSourceLocationOfModule(File jar) {
        if (listsForSources == null) {
            List<ModuleList> _listsForSources = new ArrayList<ModuleList>();
            for (URL u : getSourceRoots()) {
                if (!u.getProtocol().equals("file")) { // NOI18N
                    continue;
                }
                File dir = new File(URI.create(u.toExternalForm()));
                if (dir.isDirectory()) {
                    try {
                        if (ModuleList.isNetBeansOrg(dir)) {
                            _listsForSources.add(ModuleList.findOrCreateModuleListFromNetBeansOrgSources(dir));
                        } else {
                            _listsForSources.add(ModuleList.findOrCreateModuleListFromSuiteWithoutBinaries(dir));
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
            listsForSources = _listsForSources;
        }
        for (ModuleList l : listsForSources) {
            for (ModuleEntry entry : l.getAllEntriesSoft()) {
                // XXX should be more strict (e.g. compare also clusters)
                if (!entry.getJarLocation().getName().equals(jar.getName())) {
                    continue;
                }
                File src = entry.getSourceLocation();
                if (src != null && src.isDirectory()) {
                    return src;
                }
            }
            for (ModuleEntry entry : l.getAllEntries()) {
                if (!entry.getJarLocation().getName().equals(jar.getName())) {
                    continue;
                }
                File src = entry.getSourceLocation();
                if (src != null && src.isDirectory()) {
                    return src;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns (naturally sorted) array of all module entries pertaining to
     * <code>this</code> NetBeans platform. This is just a convenient delegate
     * to the {@link ModuleList#findOrCreateModuleListFromBinaries}.
     */
    public ModuleEntry[] getModules() {
        try {
            SortedSet<ModuleEntry> set = new TreeSet<ModuleEntry>(
                    ModuleList.findOrCreateModuleListFromBinaries(getDestDir()).getAllEntriesSoft());
            ModuleEntry[] entries = new ModuleEntry[set.size()];
            set.toArray(entries);
            return entries;
        } catch (IOException e) {
            Util.err.notify(e);
            return new ModuleEntry[0];
        }
    }
    
    /**
     * Gets a module from the platform by name.
     */
    public ModuleEntry getModule(String cnb) {
        try {
            return ModuleList.findOrCreateModuleListFromBinaries(getDestDir()).getEntry(cnb);
        } catch (IOException e) {
            Util.err.notify(e);
            return null;
        }
    }
    
    private static File findCoreJar(File destdir) {
        File[] subdirs = destdir.listFiles();
        if (subdirs != null) {
            for (int i = 0; i < subdirs.length; i++) {
                if (!subdirs[i].isDirectory()) {
                    continue;
                }
                if (!subdirs[i].getName().startsWith("platform")) { // NOI18N
                    continue;
                }
                File coreJar = new File(subdirs[i], "core" + File.separatorChar + "core.jar"); // NOI18N
                if (coreJar.isFile()) {
                    return coreJar;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether a given directory on disk is a valid destdir as per {@link #getDestDir}.
     * @param destdir a candidate directory
     * @return true if it can be used as a platform
     */
    public static boolean isPlatformDirectory(File destdir) {
        return findCoreJar(destdir) != null;
    }
    
    public static boolean isSupportedPlatform(File destdir) {
        boolean valid = false;
        File coreJar = findCoreJar(destdir);
        if (coreJar != null) {
            String platformDir = coreJar.getParentFile().getParentFile().getName();
            assert platformDir.startsWith("platform"); // NOI18N
            int version = Integer.parseInt(platformDir.substring(8)); // 8 == "platform".length
            valid = version >= 6;
        }
        return valid;
    }
    
    /**
     * Find a display name for a NetBeans platform on disk.
     * @param destdir a dir passing {@link #isPlatformDirectory}
     * @return a display name
     * @throws IllegalArgumentException if {@link #isPlatformDirectory} was false
     * @throws IOException if its labelling info could not be read
     */
    public static String computeDisplayName(File destdir) throws IOException {
        File coreJar = findCoreJar(destdir);
        if (coreJar == null) {
            throw new IllegalArgumentException(destdir.getAbsolutePath());
        }
        String currVer, implVers;
        JarFile jf = new JarFile(coreJar);
        try {
            currVer = findCurrVer(jf, "");
            if (currVer == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
            implVers = jf.getManifest().getMainAttributes().getValue("OpenIDE-Module-Implementation-Version"); // NOI18N
            if (implVers == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
        } finally {
            jf.close();
        }
        // Also check in localizing bundles for 'currentVersion', since it may be branded.
        // We do not know what the runtime branding will be, so look for anything.
        File[] clusters = destdir.listFiles();
        BRANDED_CURR_VER: if (clusters != null) {
            for (int i = 0; i < clusters.length; i++) {
                File coreLocaleDir = new File(clusters[i], "core" + File.separatorChar + "locale"); // NOI18N
                if (!coreLocaleDir.isDirectory()) {
                    continue;
                }
                String[] kids = coreLocaleDir.list();
                if (kids != null) {
                    for (int j = 0; j < kids.length; j++) {
                        String name = kids[j];
                        String prefix = "core"; // NOI18N
                        String suffix = ".jar"; // NOI18N
                        if (!name.startsWith(prefix) || !name.endsWith(suffix)) {
                            continue;
                        }
                        String infix = name.substring(prefix.length(), name.length() - suffix.length());
                        int uscore = infix.lastIndexOf('_');
                        if (uscore == -1) {
                            // Malformed.
                            continue;
                        }
                        String lastPiece = infix.substring(uscore + 1);
                        if (Arrays.asList(Locale.getISOCountries()).contains(lastPiece) ||
                                (!lastPiece.equals("nb") && Arrays.asList(Locale.getISOLanguages()).contains(lastPiece))) { // NOI18N
                            // Probably a localization, not a branding... so skip it. (We want to show English only.)
                            // But hardcode support for branding 'nb' since this is also Norwegian Bokmal, apparently!
                            // XXX should this try to use Locale.getDefault() localization if possible?
                            continue;
                        }
                        jf = new JarFile(new File(coreLocaleDir, name));
                        try {
                            String brandedCurrVer = findCurrVer(jf, infix);
                            if (brandedCurrVer != null) {
                                currVer = brandedCurrVer;
                                break BRANDED_CURR_VER;
                            }
                        } finally {
                            jf.close();
                        }
                    }
                }
            }
        }
        return MessageFormat.format(currVer, new Object[] {implVers});
    }
    private static String findCurrVer(JarFile jar, String infix) throws IOException {
        // first try to find the Bundle for 5.0+ (after openide split)
        ZipEntry bundle = jar.getEntry("org/netbeans/core/startup/Bundle" + infix + ".properties"); // NOI18N
        if (bundle == null) {
            // might be <5.0 (before openide split)
            bundle = jar.getEntry("org/netbeans/core/Bundle" + infix + ".properties"); // NOI18N
        }
        if (bundle == null) {
            return null;
        }
        Properties props = new Properties();
        InputStream is = jar.getInputStream(bundle);
        try {
            props.load(is);
        } finally {
            is.close();
        }
        return props.getProperty("currentVersion"); // NOI18N
    }
    
    /**
     * Returns whether the given label (see {@link #getLabel}) is valid.
     * <em>Valid</em> label must be non-null and must not be used by any
     * already defined platform.
     */
    public static boolean isLabelValid(String supposedLabel) {
        if (supposedLabel == null) {
            return false;
        }
        for (NbPlatform p : NbPlatform.getPlatforms()) {
            String label = p.getLabel();
            if (supposedLabel.equals(label)) {
                return false;
            }
        }
        return true;
    }
    
    public @Override String toString() {
        return "NbPlatform[" + getID() + ":" + getDestDir() + ";sources=" + Arrays.asList(getSourceRoots()) + ";javadoc=" + Arrays.asList(getJavadocRoots()) + "]"; // NOI18N;
    }
    
    /**
     * Get the version of this platform's harness.
     */
    public int getHarnessVersion() {
        if (harnessVersion != -1) {
            return harnessVersion;
        }
        if (!isValid()) {
            return harnessVersion = HARNESS_VERSION_UNKNOWN;
        }
        File harnessJar = new File(harness, "modules" + File.separatorChar + "org-netbeans-modules-apisupport-harness.jar"); // NOI18N
        if (harnessJar.isFile()) {
            try {
                JarFile jf = new JarFile(harnessJar);
                try {
                    String spec = jf.getManifest().getMainAttributes().getValue(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION);
                    if (spec != null) {
                        SpecificationVersion v = new SpecificationVersion(spec);
                        if (v.compareTo(new SpecificationVersion("1.11")) >= 0) { // NOI18N
                            return harnessVersion = HARNESS_VERSION_61;
                        } else if (v.compareTo(new SpecificationVersion("1.10")) >= 0) { // NOI18N
                            return harnessVersion = HARNESS_VERSION_60;
                        } else if (v.compareTo(new SpecificationVersion("1.9")) >= 0) { // NOI18N
                            return harnessVersion = HARNESS_VERSION_55u1;
                        } else if (v.compareTo(new SpecificationVersion("1.7")) >= 0) { // NOI18N
                            return harnessVersion = HARNESS_VERSION_50u1;
                        } else if (v.compareTo(new SpecificationVersion("1.6")) >= 0) { // NOI18N
                            return harnessVersion = HARNESS_VERSION_50;
                        } // earlier than beta2? who knows...
                    }
                } finally {
                    jf.close();
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            } catch (NumberFormatException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return harnessVersion = HARNESS_VERSION_UNKNOWN;
    }
    
    /**
     * Get the current location of this platform's harness
     */
    public File getHarnessLocation() {
        return harness;
    }
    
    /**
     * Get the location of the harness bundled with this platform.
     */
    public File getBundledHarnessLocation() {
        return findHarness(nbdestdir);
    }
    
    /**
     * Set a new location for this platform's harness.
     */
    public void setHarnessLocation(final File harness) throws IOException {
        if (harness.equals(this.harness)) {
            return;
        }
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    storeHarnessLocation(id, nbdestdir, harness, props);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        this.harness = harness;
        harnessVersion = -1;
    }
    
    /**
     * Gets a quick display name for the <em>version</em> of a harness.
     * @param {@link #HARNESS_VERSION_50} etc.
     * @return a short display name
     */
    public static String getHarnessVersionDisplayName(int version) {
        switch (version) {
            case HARNESS_VERSION_50:
                return NbBundle.getMessage(NbPlatform.class, "LBL_harness_version_5.0");
            case HARNESS_VERSION_50u1:
                return NbBundle.getMessage(NbPlatform.class, "LBL_harness_version_5.0u1");
            case HARNESS_VERSION_55u1:
                return NbBundle.getMessage(NbPlatform.class, "LBL_harness_version_5.5u1");
            case HARNESS_VERSION_60:
                return NbBundle.getMessage(NbPlatform.class, "LBL_harness_version_6.0");
            case HARNESS_VERSION_61:
                return NbBundle.getMessage(NbPlatform.class, "LBL_harness_version_6.1");
            default:
                assert version == HARNESS_VERSION_UNKNOWN;
                return NbBundle.getMessage(NbPlatform.class, "LBL_harness_version_unknown");
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
}
