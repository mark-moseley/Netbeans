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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.Util;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// XXX various methods need to acquire read or write mutex!

/**
 * Support class for implementing Ant-based projects.
 * @author Jesse Glick
 */
public final class AntProjectHelper {
    
    /**
     * Relative path from project directory to the customary shared properties file.
     */
    public static final String PROJECT_PROPERTIES_PATH = "nbproject/project.properties"; // NOI18N
    
    /**
     * Relative path from project directory to the customary private properties file.
     */
    public static final String PRIVATE_PROPERTIES_PATH = "nbproject/private/private.properties"; // NOI18N
    
    /**
     * Relative path from project directory to the required shared project metadata file.
     */
    public static final String PROJECT_XML_PATH = AntBasedProjectFactorySingleton.PROJECT_XML_PATH;
    
    /**
     * Relative path from project directory to the required private project metadata file.
     */
    public static final String PRIVATE_XML_PATH = "nbproject/private/private.xml"; // NOI18N
    
    /**
     * XML namespace of Ant projects.
     */
    static final String PROJECT_NS = AntBasedProjectFactorySingleton.PROJECT_NS;
    
    /**
     * XML namespace of private component of Ant projects.
     */
    static final String PRIVATE_NS = "http://www.netbeans.org/ns/project-private/1"; // NOI18N
    
    static {
        AntBasedProjectFactorySingleton.HELPER_CALLBACK = new AntBasedProjectFactorySingleton.AntProjectHelperCallback() {
            public AntProjectHelper createHelper(FileObject dir, Document projectXml, ProjectState state, AntBasedProjectType type) {
                return new AntProjectHelper(dir, projectXml, state, type);
            }
            public void save(AntProjectHelper helper) throws IOException {
                helper.save();
            }
        };
    }
    
    /**
     * Project base directory.
     */
    private final FileObject dir;
    
    /**
     * State object permitting modifications.
     */
    private final ProjectState state;
    
    /**
     * Ant-based project type factory.
     */
    private final AntBasedProjectType type;
    
    /**
     * Cached project.xml parse (null if not loaded).
     */
    private Document projectXml;
    
    /**
     * Cached private.xml parse (null if not loaded).
     */
    private Document privateXml;
    
    /**
     * Set of relative paths to metadata files which have been modified
     * and which need to be saved.
     */
    private final Set/*<String>*/ modifiedMetadataPaths = new HashSet();
    
    /**
     * Registered listeners.
     * Access must be directly synchronized.
     */
    private final List/*<AntProjectListener>*/ listeners = new ArrayList();
    
    /**
     * List of loaded properties.
     */
    private final ProjectProperties properties;
    
    // XXX lock any loaded XML files while the project is modified, to prevent manual editing,
    // and reload any modified files if the project is unmodified
    
    private AntProjectHelper(FileObject dir, Document projectXml, ProjectState state, AntBasedProjectType type) {
        this.dir = dir;
        assert dir != null && FileUtil.toFile(dir) != null;
        this.state = state;
        assert state != null;
        this.type = type;
        assert type != null;
        this.projectXml = projectXml;
        assert projectXml != null;
        properties = new ProjectProperties(dir);
    }
    
    /**
     * Get the corresponding Ant-based project type factory.
     */
    AntBasedProjectType getType() {
        return type;
    }

    /**
     * Retrieve project.xml or private.xml, loading from disk as needed.
     * private.xml is created as a skeleton on demand.
     */
    private Document getConfigurationXml(boolean shared) {
        Document xml = shared ? projectXml : privateXml;
        if (xml == null) {
            String path = shared ? PROJECT_XML_PATH : PRIVATE_XML_PATH;
            xml = loadXml(path);
            if (xml == null) {
                // Missing or broken; create a skeleton.
                String element = shared ? "project" : "private"; // NOI18N
                String ns = shared ? PROJECT_NS : PRIVATE_NS;
                xml = XMLUtil.createDocument(element, ns, null, null);
            }
            if (shared) {
                projectXml = xml;
            } else {
                privateXml = xml;
            }
        }
        assert xml != null;
        return xml;
    }
    
    /**
     * Try to load a config XML file from a named path.
     * If the file does not exist, or there is any load error, return null.
     */
    private Document loadXml(String path) {
        FileObject xml = dir.getFileObject(path);
        if (xml == null || !xml.isData()) {
            return null;
        }
        File f = FileUtil.toFile(xml);
        assert f != null;
        try {
            return XMLUtil.parse(new InputSource(f.toURI().toString()), false, true, /*XXX*/null, null);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (SAXException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private void saveXml(Document doc, String path) throws IOException {
        FileObject xml = FileUtil.createData(dir, path);
        FileLock lock = xml.lock();
        try {
            OutputStream os = xml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Get the <code>&lt;configuration&gt;</code> element of project.xml
     * or the document element of private.xml.
     * Beneath this point you can load and store configuration fragments.
     * @param shared if true, use project.xml, else private.xml
     * @return the data root
     */
    private Element getConfigurationDataRoot(boolean shared) {
        Document doc = getConfigurationXml(shared);
        if (shared) {
            Element project = doc.getDocumentElement();
            Element config = Util.findElement(project, "configuration", PROJECT_NS); // NOI18N
            assert config != null;
            return config;
        } else {
            return doc.getDocumentElement();
        }
    }

    /**
     * Add a listener to changes in the project configuration.
     * <p>Thread-safe.
     * @param listener a listener to add
     */
    public void addAntProjectListener(AntProjectListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener to changes in the project configuration.
     * <p>Thread-safe.
     * @param listener a listener to remove
     */
    public void removeAntProjectListener(AntProjectListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Fire a change to all listeners.
     * Must be called from write access; enters read access while firing.
     * @param path path to the changed file (XML or properties)
     * @param expected true if the result of an API-initiated change, false if from external causes
     */
    private void fireChange(String path, boolean expected) {
        //assert ProjectManager.mutex().canWrite();
        final AntProjectListener[] _listeners;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = (AntProjectListener[])listeners.toArray(new AntProjectListener[listeners.size()]);
        }
        final AntProjectEvent ev = new AntProjectEvent(this, path, expected);
        final boolean xml = path.equals(PROJECT_XML_PATH) || path.equals(PRIVATE_XML_PATH);
        ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                for (int i = 0; i < _listeners.length; i++) {
                    if (xml) {
                        _listeners[i].configurationXmlChanged(ev);
                    } else {
                        _listeners[i].propertiesChanged(ev);
                    }
                }
                return null;
            }
        });
    }
    
    /**
     * Call when explicitly modifying some piece of metadata.
     */
    private void modifying(String path) {
        state.markModified();
        modifiedMetadataPaths.add(path);
        fireChange(path, true);
    }
    
    /**
     * Get the configured code name of the project.
     * @return the code name, or null if it could not be determined (parse error)
     */
    public String getName() {
        Document doc = getConfigurationXml(true);
        Element project = doc.getDocumentElement();
        Element name = Util.findElement(project, "name", PROJECT_NS); // NOI18N
        if (name != null) {
            return Util.findText(name);
        } else {
            return null;
        }
    }
    
    /**
     * Change the project code name.
     * If the new name differs from the old, the project will be marked as modified
     * and a change will be fired in {@link #PROJECT_XML_PATH}.
     * @param name the new code name
     */
    public void setName(String name) {
        if (name.equals(getName())) {
            // Unchanged, do nothing
            return;
        }
        Document doc = getConfigurationXml(true);
        Element project = doc.getDocumentElement();
        Element nameEl = Util.findElement(project, "name", PROJECT_NS); // NOI18N
        if (nameEl == null) {
            // Missing for some reason.
            nameEl = doc.createElementNS(PROJECT_NS, "name"); // NOI18N
            // XXX better to put it right before <display-name>
            project.appendChild(nameEl);
        } else {
            // Clear existing text node child.
            NodeList l = nameEl.getChildNodes();
            while (l.getLength() > 0) {
                nameEl.removeChild(l.item(0));
            }
        }
        nameEl.appendChild(doc.createTextNode(name));
        modifying(PROJECT_XML_PATH);
    }
    
    /**
     * Get the configured display name of the project.
     * @return the display name, or null if it could not be determined (parse error)
     */
    public String getDisplayName() {
        Document doc = getConfigurationXml(true);
        Element project = doc.getDocumentElement();
        Element name = Util.findElement(project, "display-name", PROJECT_NS); // NOI18N
        if (name != null) {
            return Util.findText(name);
        } else {
            return null;
        }
    }
    
    /**
     * Change the project display name.
     * If the new name differs from the old, the project will be marked as modified
     * and a change will be fired in {@link #PROJECT_XML_PATH}.
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
        if (displayName.equals(getDisplayName())) {
            // Unchanged, do nothing
            return;
        }
        Document doc = getConfigurationXml(true);
        Element project = doc.getDocumentElement();
        Element name = Util.findElement(project, "display-name", PROJECT_NS); // NOI18N
        if (name == null) {
            // Missing for some reason.
            name = doc.createElementNS(PROJECT_NS, "display-name"); // NOI18N
            // XXX better to put it right after <name>
            project.appendChild(name);
        } else {
            // Clear existing text node child.
            NodeList l = name.getChildNodes();
            while (l.getLength() > 0) {
                name.removeChild(l.item(0));
            }
        }
        name.appendChild(doc.createTextNode(displayName));
        modifying(PROJECT_XML_PATH);
    }
    
    /**
     * Get the top-level project directory.
     * @return the project directory beneath which everything in the project lies
     */
    public FileObject getProjectDirectory() {
        return dir;
    }
    
    /**
     * Mark this project as being modified without actually changing anything in it.
     * Should only be called from {@link ProjectGenerator#createProject}.
     */
    void markModified() {
        state.markModified();
        // To make sure projectXmlSaved is called:
        modifiedMetadataPaths.add(PROJECT_XML_PATH);
    }
    
    /**
     * Check whether this project is currently modified including modifications
     * to <code>project.xml</code>.
     * Access from GeneratedFilesHelper.
     */
    boolean isProjectXmlModified() {
        return modifiedMetadataPaths.contains(PROJECT_XML_PATH);
    }
    
    /**
     * Save all cached project metadata.
     * If <code>project.xml</code> was one of the modified files, then
     * {@link AntBasedProjectType#projectXmlSaved} is called, presumably
     * creating <code>build-impl.xml</code> and/or <code>build.xml</code>.
     */
    private void save() throws IOException {
        assert !modifiedMetadataPaths.isEmpty();
        ProjectXmlSavedHook hook = null;
        if (modifiedMetadataPaths.contains(PROJECT_XML_PATH)) {
            // Saving project.xml so look for that hook.
            Project p = AntBasedProjectFactorySingleton.getProjectFor(this);
            hook = (ProjectXmlSavedHook)p.getLookup().lookup(ProjectXmlSavedHook.class);
            // might still be null
        }
        Iterator it = modifiedMetadataPaths.iterator();
        while (it.hasNext()) {
            String path = (String)it.next();
            if (path.equals(PROJECT_XML_PATH)) {
                assert projectXml != null;
                saveXml(projectXml, path);
            } else if (path.equals(PRIVATE_XML_PATH)) {
                assert privateXml != null;
                saveXml(privateXml, path);
            } else {
                // All else is assumed to be a properties file.
                properties.write(path);
            }
            // As metadata files are saved, take them off the modified list.
            it.remove();
        }
        if (hook != null) {
            hook.projectXmlSaved();
        }
    }
    
    /**
     * Load a property file from some location in the project.
     * The returned object may be edited but you must call {@link #putProperties}
     * to save any changes you make.
     * If the file does not (yet) exist or could not be loaded for whatever reason,
     * an empty properties list is returned instead.
     * @param path a relative URI in the project directory, e.g.
     *             {@link #PROJECT_PROPERTIES_PATH} or {@link #PRIVATE_PROPERTIES_PATH}
     * @return a set of properties
     */
    public EditableProperties getProperties(String path) {
        if (path.equals(AntProjectHelper.PROJECT_XML_PATH) || path.equals(AntProjectHelper.PRIVATE_XML_PATH)) {
            throw new IllegalArgumentException("Attempt to load properties from a project XML file"); // NOI18N
        }
        return properties.getProperties(path);
    }
    
    /**
     * Store a property file to some location in the project.
     * A clone will be made of the supplied properties file so as to snapshot it.
     * The new properties are not actually stored to disk immediately, but the project
     * is marked modified so that they will be later.
     * You can store to a path that does not yet exist and the file will be created
     * if and when the project is saved.
     * If the old value is the same as the new, nothing is done.
     * Otherwise an expected properties change event is fired.
     * @param path a relative URI in the project directory, e.g.
     *             {@link #PROJECT_PROPERTIES_PATH} or {@link #PRIVATE_PROPERTIES_PATH}
     * @param props a set of properties to store, or null to delete any existing properties file there
     */
    public void putProperties(String path, EditableProperties props) {
        if (path.equals(AntProjectHelper.PROJECT_XML_PATH) || path.equals(AntProjectHelper.PRIVATE_XML_PATH)) {
            throw new IllegalArgumentException("Attempt to store properties from a project XML file"); // NOI18N
        }
        if (properties.putProperties(path, props)) {
            modifying(path);
        }
    }
    
    /**
     * Get a property provider that works with loadable project properties.
     * Its current values should match {@link #getProperties}, and calls to
     * {@link #putProperties} should cause it to fire changes.
     * @param path a relative URI in the project directory, e.g.
     *             {@link #PROJECT_PROPERTIES_PATH} or {@link #PRIVATE_PROPERTIES_PATH}
     * @return a property provider implementation
     */
    public PropertyProvider getPropertyProvider(String path) {
        if (path.equals(AntProjectHelper.PROJECT_XML_PATH) || path.equals(AntProjectHelper.PRIVATE_XML_PATH)) {
            throw new IllegalArgumentException("Attempt to store properties from a project XML file"); // NOI18N
        }
        return properties.getPropertyProvider(path);
    }
    
    /**
     * Get the primary configuration data for this project.
     * The returned element will be named according to
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementName} and
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementNamespace}.
     * The project may read this document fragment to get custom information
     * from <code>nbproject/project.xml</code> and <code>nbproject/private/private.xml</code>.
     * The fragment will have no parent node and while it may be modified, you must
     * use {@link #putPrimaryConfigurationData} to store any changes.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return the configuration data that is available
     */
    public Element getPrimaryConfigurationData(boolean shared) {
        String name = type.getPrimaryConfigurationDataElementName(shared);
        assert name.indexOf(':') == -1;
        String namespace = type.getPrimaryConfigurationDataElementNamespace(shared);
        assert namespace != null && namespace.length() > 0;
        Element el = getConfigurationFragment(name, namespace, shared);
        if (el != null) {
            return el;
        } else {
            // No such data, corrupt file.
            return getConfigurationXml(shared).createElementNS(namespace, name);
        }
    }
    
    /**
     * Store the primary configuration data for this project.
     * The supplied element must be named according to
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementName} and
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementNamespace}.
     * The project may save this document fragment to set custom information
     * in <code>nbproject/project.xml</code> and <code>nbproject/private/private.xml</code>.
     * The fragment will be cloned and so further modifications will have no effect.
     * @param data the desired new configuration data
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @throws IllegalArgumentException if the element is not correctly named
     */
    public void putPrimaryConfigurationData(Element data, boolean shared) throws IllegalArgumentException {
        String name = type.getPrimaryConfigurationDataElementName(shared);
        assert name.indexOf(':') == -1;
        String namespace = type.getPrimaryConfigurationDataElementNamespace(shared);
        assert namespace != null && namespace.length() > 0;
        if (!name.equals(data.getLocalName()) || !namespace.equals(data.getNamespaceURI())) {
            throw new IllegalArgumentException();
        }
        putConfigurationFragment(data, shared);
    }
    
    /**
     * Get a piece of the configuration subtree by name.
     * @param elementName the simple XML element name expected
     * @param namespace the XML namespace expected
     * @param shared to use project.xml vs. private.xml
     * @return (a clone of) the named configuration fragment, or null if it does not exist
     */
    Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
        Element root = getConfigurationDataRoot(shared);
        Element data = Util.findElement(root, elementName, namespace);
        if (data != null) {
            // XXX should also perhaps set ownerDocument to a dummy empty document?
            // or create a new document with this as a root?
            return (Element)data.cloneNode(true);
        } else {
            return null;
        }
    }
    
    /**
     * Store a piece of the configuration subtree by name.
     * @param fragment a piece of the subtree to store (overwrite or add)
     * @param shared to use project.xml vs. private.xml
     */
    void putConfigurationFragment(Element fragment, boolean shared) {
        Element root = getConfigurationDataRoot(shared);
        Element existing = Util.findElement(root, fragment.getLocalName(), fragment.getNamespaceURI());
        // XXX first compare to existing and return if the same
        if (existing != null) {
            root.removeChild(existing);
        }
        // the children are alphabetize: find correct place to insert new node
        Node ref = null;
        NodeList list = root.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            Node node  = list.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            int comparison = node.getNodeName().compareTo(fragment.getNodeName());
            if (comparison == 0) {
                comparison = node.getNamespaceURI().compareTo(fragment.getNamespaceURI());
            }
            if (comparison > 0) {
                ref = node;
                break;
            }
        }
        root.insertBefore(root.getOwnerDocument().importNode(fragment, true), ref);
        modifying(shared ? PROJECT_XML_PATH : PRIVATE_XML_PATH);
    }
    
    /**
     * Remove a piece of the configuration subtree by name.
     * @param elementName the simple XML element name expected
     * @param namespace the XML namespace expected
     * @param shared to use project.xml vs. private.xml
     * @return true if anything was actually removed
     */
    boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) {
        Element root = getConfigurationDataRoot(shared);
        Element data = Util.findElement(root, elementName, namespace);
        if (data != null) {
            root.removeChild(data);
            modifying(shared ? PROJECT_XML_PATH : PRIVATE_XML_PATH);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Create an object permitting this project to store auxiliary configuration.
     * Would be placed into the project's lookup.
     * @return an auxiliary configuration provider object suitable for the project lookup
     */
    public AuxiliaryConfiguration createAuxiliaryConfiguration() {
        return new ExtensibleMetadataProviderImpl(this);
    }
    
    /**
     * Create an object permitting this project to expose a cache directory.
     * Would be placed into the project's lookup.
     * @return a cache directory provider object suitable for the project lookup
     */
    public CacheDirectoryProvider createCacheDirectoryProvider() {
        return new ExtensibleMetadataProviderImpl(this);
    }
    
    /** @deprecated Use the variant that accepts a property evaluator instead. */
    public FileBuiltQueryImplementation createGlobFileBuiltQuery(String[] from, String[] to) throws IllegalArgumentException {
        return createGlobFileBuiltQuery(getStandardPropertyEvaluator(), from, to);
    }
    
    /**
     * Create an implementation of {@link FileBuiltQuery} that works with files
     * within the project based on simple glob pattern mappings.
     * <p>
     * It is intended to be
     * placed in {@link org.netbeans.api.project.Project#getLookup}.
     * <p>
     * It will return status objects for any files in the project matching a source
     * glob pattern - this must include exactly one asterisk (<code>*</code>)
     * representing a variable portion of a source file path (always slash-separated
     * and relative to the project directory) and may include some Ant property
     * references which will be resolved as per the property evaluator.
     * A file is considered out of date if there is no file represented by the
     * matching target pattern (which has the same format), or the target file is older
     * than the source file, or the source file is modified as per
     * {@link DataObject#isModified}.
     * An attempt is made to fire changes from the status object whenever the result
     * should change from one call to the next.
     * <p>
     * The source pattern must be a relative path inside the project directory.
     * The target pattern currently must also, though this restriction may
     * be relaxed in the future.
     * </p>
     * <div class="nonnormative">
     * <p>
     * A typical set of source and target patterns would be:
     * </p>
     * <ol>
     * <li><samp>${src.dir}/*.java</samp>
     * <li><samp>${test.src.dir}/*.java</samp>
     * </ol>
     * <ol>
     * <li><samp>${build.classes.dir}/*.class</samp>
     * <li><samp>${test.build.classes.dir}/*.class</samp>
     * </ol>
     * </div>
     * @param a property evaluator to interpret the patterns with
     * @param from a list of glob patterns for source files
     * @param to a matching list of glob patterns for built files
     * @return a query implementation
     * @throws IllegalArgumentException if either from or to patterns
     *                                  have zero or multiple asterisks,
     *                                  or the arrays are not of equal lengths
     */
    public FileBuiltQueryImplementation createGlobFileBuiltQuery(PropertyEvaluator eval, String[] from, String[] to) throws IllegalArgumentException {
        return new GlobFileBuiltQuery(this, eval, from, to);
    }

    /** @deprecated Use the variant that accepts a PropertyEvaluator instead. */
    public AntArtifact createSimpleAntArtifact(String type, String locationProperty, String targetName, String cleanTargetName) {
        return createSimpleAntArtifact(type, locationProperty, getStandardPropertyEvaluator(), targetName, cleanTargetName);
    }
    
    /**
     * Create a basic implementation of {@link AntArtifact} which assumes everything of interest
     * is in a fixed location under a standard Ant-based project.
     * @param type the type of artifact, e.g. {@link AntArtifact#TYPE_JAR}
     * @param locationProperty an Ant property name giving the project-relative
     *                         location of the artifact, e.g. <samp>dist.jar</samp>
     * @param eval a way to evaluate the location property (e.g. {@link #getStandardPropertyEvaluator})
     * @param targetName the name of an Ant target which will build the artifact,
     *                   e.g. <samp>jar</samp>
     * @param cleanTargetName the name of an Ant target which will delete the artifact
     *                        (and maybe other build products), e.g. <samp>clean</samp>
     * @return an artifact
     */
    public AntArtifact createSimpleAntArtifact(String type, String locationProperty, PropertyEvaluator eval, String targetName, String cleanTargetName) {
        return new SimpleAntArtifact(this, type, locationProperty, eval, targetName, cleanTargetName);
    }
    
    /** @deprecated Use the variant that takes a property evaluator instead. */
    public SharabilityQueryImplementation createSharabilityQuery(String[] sourceRoots, String[] buildDirectories) {
        return createSharabilityQuery(getStandardPropertyEvaluator(), sourceRoots, buildDirectories);
    }
    
    /**
     * Create an implementation of the file sharability query.
     * You may specify a list of source roots to include that should be considered sharable,
     * as well as a list of build directories that should not be considered sharable.
     * <p>
     * The project directory itself is automatically included in the list of sharable directories
     * so you need not explicitly specify it.
     * Similarly, the <code>nbproject/private</code> subdirectory is automatically excluded
     * from VCS, so you do not need to explicitly specify it.
     * </p>
     * <p>
     * Any file (or directory) mentioned (explicitly or implicity) in the source
     * directory list but not in any of the build directory lists, and not containing
     * any build directories inside it, will be given as sharable. If a directory itself
     * is sharable but some directory inside it is not, it will be given as mixed.
     * A file or directory inside some build directory will be listed as not sharable.
     * A file or directory matching neither the source list nor the build directory list
     * will be treated as of unknown status, but in practice such a file should never
     * have been passed to this implementation anyway - {@link SharabilityQuery} will
     * normally only call an implementation in project lookup if the file is owned by
     * that project.
     * </p>
     * <p>
     * Each entry in either list should be a string evaluated first for Ant property
     * escapes (if any), then treated as a file path relative to the project directory
     * (or it may be absolute).
     * </p>
     * <p>
     * It is permitted, and harmless, to include items that overlap others. For example,
     * you can have both a directory and one of its children in the include list.
     * </p>
     * <div class="nonnormative">
     * <p>
     * Typical usage would be:
     * </p>
     * <pre>
     * helper.createSharabilityQuery(helper.getStandardPropertyEvaluator(),
     *                               new String[] {"${src.dir}", "${test.src.dir}"},
     *                               new String[] {"${build.dir}", "${dist.dir}"})
     * </pre>
     * <p>
     * A quick rule of thumb is that the include list should contain any
     * source directories which <em>might</em> reside outside the project directory;
     * and the exclude list should contain any directories which you would want
     * to add to a <samp>.cvsignore</samp> file if using CVS (for example).
     * </p>
     * <p>
     * Note that in this case <samp>${src.dir}</samp> and <samp>${test.src.dir}</samp>
     * may be relative paths inside the project directory; relative paths pointing
     * outside of the project directory; or absolute paths (generally outside of the
     * project directory). If they refer to locations inside the project directory,
     * including them does nothing but is harmless - since the project directory itself
     * is always treated as sharable. If they refer to external locations, you will
     * need to also make sure that {@link FileOwnerQuery} actually maps files in those
     * directories to this project, or else {@link SharabilityQuery} will never find
     * this implementation in your project lookup and may return <code>UNKNOWN</code>.
     * </p>
     * </div>
     * @param eval a property evaluator to interpret paths with
     * @param sourceRoots a list of additional paths to treat as sharable
     * @param buildDirectories a list of paths to treat as not sharable
     * @return a sharability query implementation suitable for the project lookup
     * @see Project#getLookup
     */
    public SharabilityQueryImplementation createSharabilityQuery(PropertyEvaluator eval, String[] sourceRoots, String[] buildDirectories) {
        String[] includes = new String[sourceRoots.length + 1];
        System.arraycopy(sourceRoots, 0, includes, 0, sourceRoots.length);
        includes[sourceRoots.length] = ""; // NOI18N
        String[] excludes = new String[buildDirectories.length + 1];
        System.arraycopy(buildDirectories, 0, excludes, 0, buildDirectories.length);
        excludes[buildDirectories.length] = "nbproject/private"; // NOI18N
        return new SharabilityQueryImpl(this, eval, includes, excludes);
    }
        
    /**
     * Convenience method to evaluate one Ant property in this project.
     * Searches in this order, with Ant substitutions as appropriate:
     * <ol>
     * <li><code>basedir</code> is set according to the project directory
     * <li>all system properties in the current VM
     * <li><code>private.properties</code>
     * <li><code><i>${netbeans.user}</i>/build.properties</code>
     *     (may be overridden by the property <code>user.properties.file</code> in private.properties)
     * <li><code>project.properties</code>
     * </ol>
     * @param prop a property name to evaluate
     * @return its value if known, or null if it cannot be determined
     * @see PropertyUtils#evaluate
     * @see System#getProperties
     * @see #PRIVATE_PROPERTIES_PATH
     * @see PropertyUtils#getGlobalProperties
     * @see #PROJECT_PROPERTIES_PATH
     * @deprecated Please use {@link #getStandardPropertyEvaluator} instead.
     */
    public String evaluate(String prop) {
        return getStandardPropertyEvaluator().getProperty(prop);
    }
    
    /**
     * Convenience method to evaluate all Ant properties in this project.
     * Like {@link #evaluate} but produces all the values at once.
     * @return values for all defined properties, or null if a circularity error was detected
     * @see PropertyUtils#evaluateAll
     * @see System#getProperties
     * @see #PRIVATE_PROPERTIES_PATH
     * @see PropertyUtils#getGlobalProperties
     * @see #PROJECT_PROPERTIES_PATH
     * @deprecated Please use {@link #getStandardPropertyEvaluator} instead.
     */
    public Map/*<String,String>*/ evaluateAll() {
        return getStandardPropertyEvaluator().getProperties();
    }
    
    /**
     * Convenience method to evaluate a string with substitutions from properties in this project.
     * Like {@link #evaluate} but works on a block of text rather than a property name.
     * @param text some string of text possibly containing Ant-style property references
     * @return the substituted text, or null if a circularity error was detected
     * @see PropertyUtils#evaluateString
     * @see System#getProperties
     * @see #PRIVATE_PROPERTIES_PATH
     * @see PropertyUtils#getGlobalProperties
     * @see #PROJECT_PROPERTIES_PATH
     * @deprecated Please use {@link #getStandardPropertyEvaluator} instead.
     */
    public String evaluateString(String text) {
        return getStandardPropertyEvaluator().evaluate(text);
    }
    
    private PropertyProvider stockPropertyPreprovider = null;
    
    /**
     * Get a property provider which defines <code>basedir</code> according to
     * the project directory and also copies all system properties in the current VM.
     * @return a stock property provider for initial Ant-related definitions
     * @see PropertyUtils#sequentialPropertyEvaluator
     */
    public PropertyProvider getStockPropertyPreprovider() {
        if (stockPropertyPreprovider == null) {
            Map/*<String,String>*/ m = new HashMap();
            Properties p = System.getProperties();
            synchronized (p) {
                m.putAll(p);
            }
            m.put("basedir", FileUtil.toFile(dir).getAbsolutePath()); // NOI18N
            stockPropertyPreprovider = PropertyUtils.fixedPropertyProvider(m);
        }
        return stockPropertyPreprovider;
    }
    
    private PropertyEvaluator standardPropertyEvaluator = null;
    
    /**
     * Get a property evaluator that can evaluate properties according to the default
     * file layout for Ant-based projects.
     * First, {@link #getStockPropertyPreprovider stock properties} are predefined.
     * Then {@link #PRIVATE_PROPERTIES_PATH} is loaded via {@link #getPropertyProvider},
     * then global definitions from {@link PropertyUtils#globalPropertyProvider}
     * (though these may be overridden using the property <code>user.properties.file</code>
     * in <code>private.properties</code>), then {@link #PROJECT_PROPERTIES_PATH}.
     * @return a standard property evaluator
     */
    public PropertyEvaluator getStandardPropertyEvaluator() {
        if (standardPropertyEvaluator == null) {
            PropertyEvaluator findUserPropertiesFile = PropertyUtils.sequentialPropertyEvaluator(
                getStockPropertyPreprovider(),
                new PropertyProvider[] {
                    getPropertyProvider(PRIVATE_PROPERTIES_PATH),
                }
            );
            String userPropertiesFile = findUserPropertiesFile.getProperty("user.properties.file"); // NOI18N
            PropertyProvider globalProperties;
            if (userPropertiesFile != null) {
                // XXX listen to changes in this, maybe...
                // (both in findUserPropertiesFile and in this file on disk)
                Properties globprops = new Properties();
                File f = resolveFile(userPropertiesFile);
                if (f.isFile() && f.canRead()) {
                    try {
                        InputStream is = new FileInputStream(f);
                        try {
                            globprops.load(is);
                        } finally {
                            is.close();
                        }
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
                globalProperties = PropertyUtils.fixedPropertyProvider(globprops);
            } else {
                globalProperties = PropertyUtils.globalPropertyProvider();
            }
            standardPropertyEvaluator = PropertyUtils.sequentialPropertyEvaluator(
                getStockPropertyPreprovider(),
                new PropertyProvider[] {
                    getPropertyProvider(PRIVATE_PROPERTIES_PATH),
                    globalProperties,
                    getPropertyProvider(PROJECT_PROPERTIES_PATH),
                }
            );
        }
        return standardPropertyEvaluator;
    }
    
    /**
     * Find an absolute file path from a possibly project-relative path.
     * @param filename a pathname which may be project-relative or absolute and may
     *                 use / or \ as the path separator
     * @return an absolute file corresponding to it
     */
    public File resolveFile(String filename) {
        return PropertyUtils.resolveFile(FileUtil.toFile(dir), filename);
    }
    
    /**
     * Same as {@link #resolveFile}, but produce a <code>FileObject</code> if possible.
     * @param filename a pathname according to Ant conventions
     * @return a file object it represents, or null if there is no such file object in known filesystems
     */
    public FileObject resolveFileObject(String filename) {
        if (filename == null) {
            throw new NullPointerException("Must pass a non-null filename"); // NOI18N
        }
        return PropertyUtils.resolveFileObject(dir, filename);
    }
    
    /**
     * Take an Ant-style path specification and convert it to a platform-specific absolute path.
     * The path separator characters are converted to the local convention, and individual
     * path components are resolved and cleaned up as for {@link #resolveFile}.
     * @param path an Ant-style abstract path
     * @return an absolute, locally usable path
     */
    public String resolvePath(String path) {
        if (path == null) {
            throw new NullPointerException("Must pass a non-null path"); // NOI18N
        }
        // XXX consider memoizing results since this is probably called a lot
        return PropertyUtils.resolvePath(FileUtil.toFile(dir), path);
    }

}
