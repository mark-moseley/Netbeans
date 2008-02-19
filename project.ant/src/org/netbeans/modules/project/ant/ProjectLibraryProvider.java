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

package org.netbeans.modules.project.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.libraries.ArealLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Supplier of libraries declared in open projects.
 * @see "issue #44035"
 */
public class ProjectLibraryProvider implements ArealLibraryProvider<ProjectLibraryProvider.ProjectLibraryArea,ProjectLibraryProvider.ProjectLibraryImplementation>, PropertyChangeListener, AntProjectListener {

    private static final String NAMESPACE = "http://www.netbeans.org/ns/ant-project-libraries/1"; // NOI18N
    private static final String EL_LIBRARIES = "libraries"; // NOI18N
    private static final String EL_DEFINITIONS = "definitions"; // NOI18N

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private AntProjectListener apl;

    public static ProjectLibraryProvider INSTANCE;
    
    private volatile boolean listening = true;
    private final Map<ProjectLibraryArea,Reference<LP>> providers = new HashMap<ProjectLibraryArea,Reference<LP>>();
    
    /**
     * Default constructor for lookup.
     */
    public ProjectLibraryProvider() {
        INSTANCE = this;
    }

    public Class<ProjectLibraryArea> areaType() {
        return ProjectLibraryArea.class;
    }

    public Class<ProjectLibraryImplementation> libraryType() {
        return ProjectLibraryImplementation.class;
    }

    @Override
    public String toString() {
        return "ProjectLibraryProvider"; // NOI18N
    }

    // ---- management of areas ----

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public Set<ProjectLibraryArea> getOpenAreas() {
        synchronized (this) { // lazy init of OpenProjects-related stuff is better for unit testing
            if (apl == null) {
                apl = WeakListeners.create(AntProjectListener.class, this, null);
                OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OpenProjects.getDefault()));
            }
        }
        Set<ProjectLibraryArea> areas = new HashSet<ProjectLibraryArea>();
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            AntProjectHelper helper = AntBasedProjectFactorySingleton.getHelperFor(p);
            if (helper == null) {
                // Not an Ant-based project; ignore.
                continue;
            }
            helper.removeAntProjectListener(apl);
            helper.addAntProjectListener(apl);
            Definitions def = findDefinitions(helper);
            if (def != null) {
                areas.add(new ProjectLibraryArea(def.mainPropertiesFile));
            }
        }
        return areas;
    }

    public ProjectLibraryArea createArea() {
        JFileChooser jfc = new JFileChooser();
        jfc.setApproveButtonText(NbBundle.getMessage(ProjectLibraryProvider.class, "ProjectLibraryProvider.open_or_create"));
        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || (f.getName().endsWith(".properties") && !f.getName().endsWith("-private.properties")); // NOI18N
            }
            public String getDescription() {
                return NbBundle.getMessage(ProjectLibraryProvider.class, "ProjectLibraryProvider.properties_files");
            }
        };
        jfc.setFileFilter(filter);
        FileUtil.preventFileChooserSymlinkTraversal(jfc, null); // XXX remember last-selected dir
        while (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            if (filter.accept(f)) {
                return new ProjectLibraryArea(f);
            }
            // Else bad filename, reopen dialog. XXX would be better to just disable OK button, but not sure how...?
        }
        return null;
    }

    public ProjectLibraryArea loadArea(URL location) {
        if (location.getProtocol().equals("file") && location.getPath().endsWith(".properties")) { // NOI18N
            try {
                return new ProjectLibraryArea(new File(location.toURI()));
            } catch (URISyntaxException x) {
                Exceptions.printStackTrace(x);
            }
        }
        return null;
    }

    public void propertyChange(PropertyChangeEvent ev) {
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(ev.getPropertyName())) {
            pcs.firePropertyChange(ArealLibraryProvider.PROP_OPEN_AREAS, null, null);
        }
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        pcs.firePropertyChange(ArealLibraryProvider.PROP_OPEN_AREAS, null, null);
    }

    public void propertiesChanged(AntProjectEvent ev) {}

    // ---- management of libraries ----


    private final class LP implements LibraryProvider<ProjectLibraryImplementation>, FileChangeSupportListener {

        private final ProjectLibraryArea area;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final Map<String,ProjectLibraryImplementation> libraries;

        LP(ProjectLibraryArea area) {
            this.area = area;
            libraries = calculate(area);
            Definitions defs = new Definitions(area.mainPropertiesFile);
            FileChangeSupport.DEFAULT.addListener(this, defs.mainPropertiesFile);
            FileChangeSupport.DEFAULT.addListener(this, defs.privatePropertiesFile);
        }

        public synchronized ProjectLibraryImplementation[] getLibraries() {
            return libraries.values().toArray(new ProjectLibraryImplementation[libraries.size()]);
        }

        ProjectLibraryImplementation getLibrary(String name) {
            return libraries.get(name);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        public void fileCreated(FileChangeSupportEvent event) {
            recalculate();
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            recalculate();
        }

        public void fileModified(FileChangeSupportEvent event) {
            recalculate();
        }

        private void recalculate() {
            boolean fire;
            synchronized (this) {
                fire = delta(libraries, calculate(area));
            }
            if (fire) {
                pcs.firePropertyChange(LibraryProvider.PROP_LIBRARIES, null, null);
            }
        }

    }

    public synchronized LP getLibraries(ProjectLibraryArea area) {
        Reference<LP> rlp = providers.get(area);
        LP lp = rlp != null ? rlp.get() : null;
        if (lp == null) {
            lp = new LP(area);
            providers.put(area, new WeakReference<LP>(lp));
        }
        return lp;
    }

    public ProjectLibraryImplementation createLibrary(String type, String name, ProjectLibraryArea area, Map<String,List<URL>> contents) throws IOException {
        File f = area.mainPropertiesFile;
        assert listening;
        listening = false;
        try {
            if (type.equals("j2se")) { // NOI18N
                replaceProperty(f, true, "libs." + name + ".classpath", ""); // NOI18N
            } else {
                replaceProperty(f, false, "libs." + name + ".type", type); // NOI18N
            }
        } finally {
            listening = true;
        }
        LP lp = getLibraries(area);
        lp.recalculate();
        ProjectLibraryImplementation impl = lp.getLibrary(name);
        assert impl != null : name + " not found in " + f;
        for (Map.Entry<String,List<URL>> entry : contents.entrySet()) {
            impl.setContent(entry.getKey(), entry.getValue());
        }
        return impl;
    }

    public void remove(ProjectLibraryImplementation pli) throws IOException {
        String prefix = "libs." + pli.name + "."; // NOI18N
        // XXX run atomically to fire changes just once:
        for (File f : new File[] {pli.mainPropertiesFile, pli.privatePropertiesFile}) {
            for (String k : loadProperties(f).keySet()) {
                if (k.startsWith(prefix)) {
                    replaceProperty(f, false, k);
                }
            }
        }
    }

    /** one definitions entry */
    private static final class Definitions {
        /** may or may not exist; in case you need to listen to it */
        final File mainPropertiesFile;
        /** similar to {@link #mainPropertiesFile} but for *-private.properties; null if main is not *.properties */
        final File privatePropertiesFile;
        private Map<String,String> properties;
        Definitions(File mainPropertiesFile) {
            this.mainPropertiesFile = mainPropertiesFile;
            String suffix = ".properties"; // NOI18N
            String name = mainPropertiesFile.getName();
            if (name.endsWith(suffix)) {
                privatePropertiesFile = new File(mainPropertiesFile.getParentFile(), name.substring(0, name.length() - suffix.length()) + "-private" + suffix); // NOI18N
            } else {
                privatePropertiesFile = null;
            }
        }
        /** with ${base} resolved according to resolveBase; may be empty or have junk defs */
        synchronized Map<String,String> properties(boolean resolveBase) {
            if (properties == null) {
                properties = new HashMap<String,String>();
                String basedir = mainPropertiesFile.getParent();
                for (Map.Entry<String,String> entry : loadProperties(mainPropertiesFile).entrySet()) {
                    String value = entry.getValue();
                    if (resolveBase) {
                        value = value.replace("${base}", basedir); // NOI18N
                    }
                    properties.put(entry.getKey(), value.replace('/', File.separatorChar));
                }
                if (privatePropertiesFile != null) {
                    for (Map.Entry<String,String> entry : loadProperties(privatePropertiesFile).entrySet()) {
                        String value = entry.getValue();
                        if (resolveBase) {
                            value = value.replace("${base}", basedir); // NOI18N
                        }
                        properties.put(entry.getKey(), value.replace('/', File.separatorChar));
                    }
                }
            }
            return properties;
        }
    }

    private static Definitions findDefinitions(AntProjectHelper helper) {
        String text = getLibrariesLocationText(helper.createAuxiliaryConfiguration());
        if (text != null) {
            File mainPropertiesFile = helper.resolveFile(text);
            if (mainPropertiesFile.getName().endsWith(".properties")) { // NOI18N
                return new Definitions(mainPropertiesFile);
            }
        }
        return null;
    }

    public static File getLibrariesLocation(AuxiliaryConfiguration aux, File projectFolder) {
        String text = getLibrariesLocationText(aux);
        if (text != null) {
            return PropertyUtils.resolveFile(projectFolder, text);
        }
        return null;
    }
    
    /**
     * Returns libraries location as text.
     */
    public static String getLibrariesLocationText(AuxiliaryConfiguration aux) {
        Element libraries = aux.getConfigurationFragment(EL_LIBRARIES, NAMESPACE, true);
        if (libraries != null) {
            for (Element definitions : Util.findSubElements(libraries)) {
                assert definitions.getLocalName().equals(EL_DEFINITIONS) : definitions;
                String text = Util.findText(definitions);
                assert text != null : aux;
                return text;
            }
        }
        return null;
    }
    
    private static Map<String,String> loadProperties(File f) {
        if (!f.isFile()) {
            return Collections.emptyMap();
        }
        Properties p = new Properties();
        try {
            InputStream is = new FileInputStream(f);
            try {
                p.load(is);
            } finally {
                is.close();
            }
            return NbCollections.checkedMapByFilter(p, String.class, String.class, true);
        } catch (IOException x) {
            Exceptions.attachMessage(x, "Loading: " + f); // NOI18N
            Exceptions.printStackTrace(x);
            return Collections.emptyMap();
        }
    }

    //non private for test usage
    static final Pattern LIBS_LINE = Pattern.compile("libs\\.([^${}]+)\\.([^${}.]+)"); // NOI18N
    
    private static Map<String,ProjectLibraryImplementation> calculate(ProjectLibraryArea area) {
        Map<String,ProjectLibraryImplementation> libs = new HashMap<String,ProjectLibraryImplementation>();
        Definitions def = new Definitions(area.mainPropertiesFile);
        Map<String,Map<String,String>> data = new HashMap<String,Map<String,String>>();
        for (Map.Entry<String,String> entry : def.properties(false).entrySet()) {
            Matcher match = LIBS_LINE.matcher(entry.getKey());
            if (!match.matches()) {
                continue;
            }
            String name = match.group(1);
            Map<String,String> subdata = data.get(name);
            if (subdata == null) {
                subdata = new HashMap<String,String>();
                data.put(name, subdata);
            }
            subdata.put(match.group(2), entry.getValue());
        }
        for (Map.Entry<String,Map<String,String>> entry : data.entrySet()) {
            String name = entry.getKey();
            String type = "j2se"; // NOI18N
            String description = null;
            Map<String,List<URL>> contents = new HashMap<String,List<URL>>();
            for (Map.Entry<String,String> subentry : entry.getValue().entrySet()) {
                String k = subentry.getKey();
                if (k.equals("type")) { // NOI18N
                    type = subentry.getValue();
                } else if (k.equals("name")) { // NOI18N
                    // XXX currently overriding display name is not supported
                } else if (k.equals("description")) { // NOI18N
                    description = subentry.getValue();
                } else {
                    String[] path = PropertyUtils.tokenizePath(subentry.getValue());
                    List<URL> volume = new ArrayList<URL>(path.length);
                    for (String component : path) {
                        String jarFolder = null;
                        // "!/" was replaced in def.properties() with "!"+File.separatorChar
                        int index = component.indexOf("!"+File.separatorChar); //NOI18N
                        if (index != -1) {
                            jarFolder = component.substring(index+2);
                            component = component.substring(0, index);
                        }
                        String f = component.replace('/', File.separatorChar).replace('\\', File.separatorChar).replace("${base}"+File.separatorChar, "");
                        File normalizedFile = FileUtil.normalizeFile(new File(component.replace('/', File.separatorChar).replace('\\', File.separatorChar).replace("${base}", area.mainPropertiesFile.getParent())));
                        try {
                            URL u = LibrariesSupport.convertFilePathToURL(f);
                            if (FileUtil.isArchiveFile(normalizedFile.toURI().toURL())) {
                                u = FileUtil.getArchiveRoot(u);
                                if (jarFolder != null) {
                                    u = appendJarFolder(u, jarFolder);
                                }
                            } else if (!u.toExternalForm().endsWith("/")) {
                                u = new URL(u.toExternalForm() + "/");
                            }
                            volume.add(u);
                        } catch (MalformedURLException x) {
                            Exceptions.printStackTrace(x);
                        }
                    }
                    contents.put(k, volume);
                }
            }
            libs.put(name, new ProjectLibraryImplementation(def.mainPropertiesFile, def.privatePropertiesFile, type, name, description, contents));
        }
        return libs;
    }

    private boolean delta(Map<String,ProjectLibraryImplementation> libraries, Map<String,ProjectLibraryImplementation> newLibraries) {
        if (!listening) {
            return false;
        }
        Set<String> added = new HashSet<String>(newLibraries.keySet());
        added.removeAll(libraries.keySet());
        Set<String> removed = new HashSet<String>();
        for (Map.Entry<String,ProjectLibraryImplementation> entry : libraries.entrySet()) {
            String name = entry.getKey();
            ProjectLibraryImplementation old = entry.getValue();
            ProjectLibraryImplementation nue = newLibraries.get(name);
            if (nue == null) {
                removed.add(name);
                continue;
            }
            if (!old.type.equals(nue.type)) {
                // Cannot fire this.
                added.add(name);
                removed.add(name);
                libraries.put(name, nue);
                continue;
            }
            assert old.name.equals(nue.name);
            if (!Utilities.compareObjects(old.description, nue.description)) {
                old.description = nue.description;
                old.pcs.firePropertyChange(LibraryImplementation.PROP_DESCRIPTION, null, null);
            }
            if (!old.contents.equals(nue.contents)) {
                old.contents = nue.contents;
                old.pcs.firePropertyChange(LibraryImplementation.PROP_CONTENT, null, null);
            }
        }
        for (String name : added) {
            libraries.put(name, newLibraries.get(name));
        }
        for (String name : removed) {
            libraries.remove(name);
        }
        return !added.isEmpty() || !removed.isEmpty();
    }

    /** for jar url this method returns path wihtin jar or null*/
    private static String getJarFolder(URL url) {
        assert "jar".equals(url.getProtocol()) : url;
        String u = url.toExternalForm();
        int index = u.indexOf("!/"); //NOI18N
        if (index != -1 && index + 2 < u.length()) {
            return u.substring(index+2);
        }
        return null;
    }
    
    /** append path to given jar root url */
    private static URL appendJarFolder(URL u, String jarFolder) {
        assert "jar".equals(u.getProtocol()) && u.toExternalForm().endsWith("!/") : u;
        try {
            return new URL(u + jarFolder.replace('\\', '/')); //NOI18N
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
    static final class ProjectLibraryImplementation implements LibraryImplementation {

        final File mainPropertiesFile, privatePropertiesFile;
        final String type;
        String name;
        String description;
        Map<String,List<URL>> contents;
        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        ProjectLibraryImplementation(File mainPropertiesFile, File privatePropertiesFile, String type, String name, String description, Map<String,List<URL>> contents) {
            this.mainPropertiesFile = mainPropertiesFile;
            this.privatePropertiesFile = privatePropertiesFile;
            this.type = type;
            this.name = name;
            this.description = description;
            this.contents = contents;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getLocalizingBundle() {
            return null;
        }

        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            List<URL> content = contents.get(volumeType);
            if (content == null) {
                content = Collections.emptyList();
            }
            return content;
        }

        public void setName(String name) {
            this.name = name;
            pcs.firePropertyChange(LibraryImplementation.PROP_NAME, null, null);
            throw new UnsupportedOperationException(); // XXX will anyone call this?
        }

        public void setDescription(String text) {
            throw new UnsupportedOperationException(); // XXX will anyone call this?
        }

        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            if (path.equals(getContent(volumeType))) {
                return;
            }
            List<String> value = new ArrayList<String>();
            for (URL entry : path) {
                String jarFolder = null;
                if ("jar".equals(entry.getProtocol())) { // NOI18N
                    jarFolder = getJarFolder(entry);
                    entry = FileUtil.getArchiveFile(entry);
                } else if (!"file".equals(entry.getProtocol())) { // NOI18N
                    value.add(entry.toString());
                    Logger.getLogger(ProjectLibraryProvider.class.getName()).fine("Setting url=" + entry + " as content for library volume type: " + volumeType);
                    continue;
                }
                String p = LibrariesSupport.convertURLToFilePath(entry);
                File f = new File(p);
                // store properties always separated by '/' for consistency
                StringBuilder s = new StringBuilder();
                if (f.isAbsolute()) {
                    s.append(f.getAbsolutePath().replace('\\', '/')); //NOI18N
                } else {
                    s.append("${base}/" + p.replace('\\', '/')); // NOI18N
                }
                if (jarFolder != null) {
                    s.append("!/"); // NOI18N
                    s.append(jarFolder);
                }
                if (value.size()+1 != path.size()) {
                    s.append(File.pathSeparatorChar);
                }
                value.add(s.toString());
            }
            String key = "libs." + name + "." + volumeType; // NOI18N
            try {
                replaceProperty(mainPropertiesFile, true, key, value.toArray(new String[value.size()]));
            } catch (IOException x) {
                throw new IllegalArgumentException(x);
            }
        }

        public void setLocalizingBundle(String resourceName) {
            throw new UnsupportedOperationException();
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        @Override
        public String toString() {
            return "ProjectLibraryImplementation[name=" + name + ",file=" + mainPropertiesFile + ",contents=" + contents + "]"; // NOI18N
        }

    }

    private static void replaceProperty(File propfile, boolean classPathLikeValue, String key, String... value) throws IOException {
        EditableProperties ep = new EditableProperties();
        if (propfile.isFile()) {
            InputStream is = new FileInputStream(propfile);
            try {
                ep.load(is);
            } finally {
                is.close();
            }
        }
        if (Utilities.compareObjects(value, ep.getProperty(key))) {
            return;
        }
        if (value.length > 0) {
            if (classPathLikeValue) {
                ep.setProperty(key, value);
            } else {
                assert value.length == 1 : Arrays.asList(value);
                ep.setProperty(key, value[0]);
            }
        } else {
            ep.remove(key);
        }
        FileObject fo = FileUtil.createData(propfile);
        OutputStream os = fo.getOutputStream();
        try {
            ep.store(os);
        } finally {
            os.close();
        }
    }

    static final class ProjectLibraryArea implements LibraryStorageArea {

        final File mainPropertiesFile;

        ProjectLibraryArea(File mainPropertiesFile) {
            assert mainPropertiesFile.getName().endsWith(".properties") : mainPropertiesFile;
            this.mainPropertiesFile = mainPropertiesFile;
        }

        public String getDisplayName() {
            return mainPropertiesFile.getAbsolutePath();
        }

        public URL getLocation() {
            try {
                return mainPropertiesFile.toURI().toURL();
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            }
        }

        public boolean equals(Object obj) {
            return obj instanceof ProjectLibraryArea && ((ProjectLibraryArea) obj).mainPropertiesFile.equals(mainPropertiesFile);
        }

        public int hashCode() {
            return mainPropertiesFile.hashCode();
        }

        @Override
        public String toString() {
            return "ProjectLibraryArea[" + mainPropertiesFile + "]"; // NOI18N
        }

    }

    /**
     * Used from {@link AntProjectHelper#getProjectLibrariesPropertyProvider}.
     * @param helper a project
     * @return a provider of project library definition properties
     */
    public static PropertyProvider createPropertyProvider(final AntProjectHelper helper) {
        class PP implements PropertyProvider, FileChangeSupportListener, AntProjectListener {
            final ChangeSupport cs = new ChangeSupport(this);
            final Set<File> listeningTo = new HashSet<File>();
            {
                helper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, helper));
            }
            private void listenTo(File f, Set<File> noLongerListeningTo) {
                if (f != null) {
                    noLongerListeningTo.remove(f);
                    if (listeningTo.add(f)) {
                        FileChangeSupport.DEFAULT.addListener(this, f);
                    }
                }
            }
            public synchronized Map<String,String> getProperties() {
                Map<String,String> m = new HashMap<String,String>();
                // XXX add an AntProjectListener
                Set<File> noLongerListeningTo = new HashSet<File>(listeningTo);
                Definitions def = findDefinitions(helper);
                if (def != null) {
                    m.putAll(def.properties(true));
                    listenTo(def.mainPropertiesFile, noLongerListeningTo);
                    listenTo(def.privatePropertiesFile, noLongerListeningTo);
                }
                for (File f : noLongerListeningTo) {
                    listeningTo.remove(f);
                    FileChangeSupport.DEFAULT.removeListener(this, f);
                }
                return m;
            }
            public void addChangeListener(ChangeListener l) {
                cs.addChangeListener(l);
            }
            public void removeChangeListener(ChangeListener l) {
                cs.removeChangeListener(l);
            }
            public void fileCreated(FileChangeSupportEvent event) {
                fireChangeNowOrLater();
            }
            public void fileDeleted(FileChangeSupportEvent event) {
                fireChangeNowOrLater();
            }
            public void fileModified(FileChangeSupportEvent event) {
                fireChangeNowOrLater();
            }
            void fireChangeNowOrLater() {
                // See PropertyUtils.FilePropertyProvider.
                if (!cs.hasListeners()) {
                    return;
                }
                final Mutex.Action<Void> action = new Mutex.Action<Void>() {
                    public Void run() {
                        cs.fireChange();
                        return null;
                    }
                };
                if (ProjectManager.mutex().isWriteAccess() || FIRE_CHANGES_SYNCH) {
                    ProjectManager.mutex().readAccess(action);
                } else if (ProjectManager.mutex().isReadAccess()) {
                    action.run();
                } else {
                    RP.post(new Runnable() {
                        public void run() {
                            ProjectManager.mutex().readAccess(action);
                        }
                    });
                }
            }
            public void configurationXmlChanged(AntProjectEvent ev) {
                cs.fireChange();
            }
            public void propertiesChanged(AntProjectEvent ev) {}
        }
        return new PP();
    }
    private static final RequestProcessor RP = new RequestProcessor("ProjectLibraryProvider.RP"); // NOI18N
    public static boolean FIRE_CHANGES_SYNCH = false; // used by tests

    /**
     * Is this library reachable from this project? Returns true if given library
     * is defined in libraries location associated with this project.
     */
    public static boolean isReachableLibrary(Library library, AntProjectHelper helper) {
        URL location = library.getManager().getLocation();
        if (location == null) {
            return false;
        }
        ProjectLibraryArea area = INSTANCE.loadArea(location);
        if (area == null) {
            return false;
        }
        ProjectLibraryImplementation pli = INSTANCE.getLibraries(area).getLibrary(library.getName());
        if (pli == null) {
            return false;
        }
        Definitions def = findDefinitions(helper);
        if (def == null) {
            return false;
        }
        return def.mainPropertiesFile.equals(pli.mainPropertiesFile);
    }
    
    /**
     * Create element for shared libraries to store in project.xml.
     * 
     * @param doc XML document
     * @param location project relative or absolute OS path; cannot be null
     * @return element
     */
    public static Element createLibrariesElement(Document doc, String location) {
        Element libraries = doc.createElementNS(NAMESPACE, EL_LIBRARIES);
        libraries.appendChild(libraries.getOwnerDocument().createElementNS(NAMESPACE, EL_DEFINITIONS)).
            appendChild(libraries.getOwnerDocument().createTextNode(location));
        return libraries;
    }

    /**
     * Used from {@link ReferenceHelper#getProjectLibraryManager}.
     */
    public static LibraryManager getProjectLibraryManager(AntProjectHelper helper) {
        Definitions defs = findDefinitions(helper);
        if (defs != null) {
            try {
                return LibraryManager.forLocation(defs.mainPropertiesFile.toURI().toURL());
            } catch (MalformedURLException x) {
                Exceptions.printStackTrace(x);
            }
        }
        return null;
    }

    /**
     * Stores given libraries location in given project.
     */
    public static void setLibrariesLocation(AntProjectHelper helper, String librariesDefinition) {
        //TODO do we need to create new auxiliary configuration instance? feels like a hack, we should be
        // using the one from the project's lookup.  
        if (librariesDefinition == null) {
            helper.createAuxiliaryConfiguration().removeConfigurationFragment(EL_LIBRARIES, NAMESPACE, true);
            return;
        }
        Element libraries = helper.createAuxiliaryConfiguration().getConfigurationFragment(EL_LIBRARIES, NAMESPACE, true);
        if (libraries == null) {
            libraries = XMLUtil.createDocument("dummy", null, null, null).createElementNS(NAMESPACE, EL_LIBRARIES); // NOI18N
        } else {
            List<Element> elements = Util.findSubElements(libraries);
            if (elements.size() == 1) {
                libraries.removeChild(elements.get(0));
            }
        }
        libraries.appendChild(libraries.getOwnerDocument().createElementNS(NAMESPACE, EL_DEFINITIONS)).
            appendChild(libraries.getOwnerDocument().createTextNode(librariesDefinition));
        helper.createAuxiliaryConfiguration().putConfigurationFragment(libraries, true);
    }

    /**
     * Used from {@link org.netbeans.spi.project.support.ant.SharabilityQueryImpl}.
     */
    public static List<String> getUnsharablePathsWithinProject(AntProjectHelper helper) {
        List<String> paths = new ArrayList<String>();
        Definitions defs = findDefinitions(helper);
        if (defs != null) {
            if (defs.privatePropertiesFile != null) {
                paths.add(defs.privatePropertiesFile.getAbsolutePath());
            }
        }
        return paths;
    }

    public static final class SharabilityQueryImpl implements SharabilityQueryImplementation {

        /** Default constructor for lookup. */
        public SharabilityQueryImpl() {}

        public int getSharability(File file) {
            if (file.getName().endsWith("-private.properties")) { // NOI18N
                return SharabilityQuery.NOT_SHARABLE;
            } else {
                return SharabilityQuery.UNKNOWN;
            }
        }

    }

    /**
     * Used from {@link org.netbeans.spi.project.support.ant.ReferenceHelper}.
     */
    public static Library copyLibrary(final Library lib, final URL location, 
            final boolean generateLibraryUniqueName) throws IOException {
        assert LibrariesSupport.isAbsoluteURL(location);
        final File libBaseFolder = new File(LibrariesSupport.convertURLToFilePath(location)).getParentFile();
        FileObject sharedLibFolder;
        try {
            sharedLibFolder = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<FileObject>() {
                public FileObject run() throws IOException {
                    FileObject lf = FileUtil.toFileObject(libBaseFolder);
                    return lf.createFolder(getUniqueName(lf, lib.getName(), null));
                }
            });
        } catch (MutexException ex) {
            throw (IOException)ex.getException();
        }
        final Map<String, List<URL>> content = new HashMap<String, List<URL>>();
        String[] volumes = LibrariesSupport.getLibraryTypeProvider(lib.getType()).getSupportedVolumeTypes();
        for (String volume : volumes) {
            List<URL> volumeContent = new ArrayList<URL>();
            for (URL libEntry : lib.getContent(volume)) {
                String jarFolder = null;
                if ("jar".equals(libEntry.getProtocol())) { // NOI18N
                    jarFolder = getJarFolder(libEntry);
                    libEntry = FileUtil.getArchiveFile(libEntry);
                }
                FileObject libEntryFO = URLMapper.findFileObject(libEntry);
                if (libEntryFO == null) {
                    if (!"file".equals(libEntry.getProtocol()) && // NOI18N
                        !"nbinst".equals(libEntry.getProtocol())) { // NOI18N
                        Logger.getLogger(ProjectLibraryProvider.class.getName()).info("copyLibrary is ignoring entry "+libEntry);
                        //this is probably exclusively urls to maven poms.
                        continue;
                    } else {
                        Logger.getLogger(ProjectLibraryProvider.class.getName()).warning("Library '"+lib.getDisplayName()+ // NOI18N
                            "' contains entry ("+libEntry+") which does not exist. This entry is ignored and will not be copied to sharable libraries location."); // NOI18N
                        continue;
                    }
                }
                URL u;
                if (CollocationQuery.areCollocated(libBaseFolder, FileUtil.toFile(libEntryFO))) {
                    // if the jar/folder is in relation to the library folder (parent+child/same vcs)
                    // don't replicate it but reference the original file.
                    u = libEntry;
                } else {
                    FileObject newFO;
                    String name;
                    if (libEntryFO.isFolder()) {
                        newFO = FileChooserAccessory.copyFolderRecursively(libEntryFO, sharedLibFolder);
                        name = sharedLibFolder.getNameExt()+File.separatorChar+newFO.getName()+File.separatorChar;
                    } else {
                        String libEntryName = getUniqueName(sharedLibFolder, libEntryFO.getName(), libEntryFO.getExt());
                        newFO = FileUtil.copyFile(libEntryFO, sharedLibFolder, libEntryName);
                        name = sharedLibFolder.getNameExt()+File.separatorChar+newFO.getNameExt();
                    }
                    u = LibrariesSupport.convertFilePathToURL(name);
                    if (FileUtil.isArchiveFile(newFO)) {
                        u = FileUtil.getArchiveRoot(u);
                    }
                    if (jarFolder != null) {
                        u = appendJarFolder(u, jarFolder);
                    }
                }
                volumeContent.add(u);
            }
            content.put(volume, volumeContent);
        }
        final LibraryManager man = LibraryManager.forLocation(location);
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Library>() {
                public Library run() throws IOException {
                    String name = lib.getName();
                    if (generateLibraryUniqueName) {
                        int index = 2;
                        while (man.getLibrary(name) != null) {
                            name = lib.getName() + "-" + index;
                            index++;
                        }
                    }
                    return man.createLibrary(lib.getType(), name, content);
                }
            });
        } catch (MutexException ex) {
            throw (IOException)ex.getException();
        }
    }

    /**
     * Generate unique file name for the given folder, base name and optionally extension.
     * @param baseFolder folder to generate new file name in
     * @param nameFileName file name without extension
     * @param extension can be null for folder
     * @return new file name without extension
     */
    private static String getUniqueName(FileObject baseFolder, String nameFileName, String extension) {
        int suffix = 2;
        String name = nameFileName;  //NOI18N
        while (baseFolder.getFileObject(name + (extension != null ? "." + extension : "")) != null) {
            name = nameFileName + "-" + suffix; // NOI18N
            suffix++;
        }
        return name;
    }
    
}
