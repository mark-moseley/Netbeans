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
package org.netbeans.modules.visualweb.insync;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationListener;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.WeakListeners;

import org.netbeans.modules.visualweb.classloaderprovider.CommonClassloaderProvider;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectClassPathExtender;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

//NB60 import org.netbeans.modules.visualweb.insync.faces.refactoring.MdrInSyncSynchronizer;
import org.netbeans.modules.visualweb.insync.models.ConfigModel;

/**
 * A ModelSet is a collection of Models that are organized together in a single project. The
 * ModelSet serves to coordinate the Models with each other and with the containing project. <p/>
 * The Models are divided into two groups: regular or source Models with many instances, and
 * specific configuration Models with one instance per type. <p/>There is always exactly one
 * ModelSet per project. <p/>
 *
 * @author cquinn
 */
public abstract class ModelSet implements FileChangeListener {

    /**
     * Utility that will be instantiated and registed on WindowManager in order to process changes to the
     * TopComponent.Registry.PROP_CURRENT_NODES, which indicates which nodes are losing and gaining
     * focus.
     * !EAT TODO: Find a way to not need this !!!
     */
    protected static class WindowManagerPropertyRegistry implements PropertyChangeListener {
        protected TopComponent previousTopComponent;
        /**
         * Go through nodes and see if any of them map to a model we know about.  If so, then
         * hasFocus indicates whether the nodes have focus or not.  If not, then assume they just
         * lost focus and process accordingly.
         *
         * @param nodes
         * @param old
         */
        protected void processNodes(Node[] nodes, boolean makeActive) {
            if (nodes == null)
                return;
            for (int i=0; i < nodes.length; i++) {
                Node node = nodes[i];
                DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
                if (dataObject != null) {
                    FileObject fileObject = dataObject.getPrimaryFile();
                    ModelSet modelSet = ModelSet.getInstance(fileObject, null);
                    if (modelSet != null) {
                        // Get the model corresponding to the fileObject
                        Model model = modelSet.getModel(fileObject);
                        // Well, if its not a model, lets not forget that ModeSet breaks up config models and "document" models
                        if (model == null)
                            model = modelSet.getConfigModel(fileObject);
                        // sync model if the top component is losing focus
                        if (model != null && !makeActive) {
                            model.sync();
                        }
                    }
                }
            }
        }
        
        public void propertyChange(PropertyChangeEvent event) {
            if (TopComponent.Registry.PROP_CURRENT_NODES.equals(event.getPropertyName())) {
                TopComponent topComponent = getActiveTopComponent();
                // If the same top component is active, then ignore the event as it means that focus has not really changed
                // There seems to be some spurious events being fired off
                if (topComponent == previousTopComponent)
                    return;
                previousTopComponent = topComponent;
                boolean isTopComponentEditor = topComponent instanceof CloneableEditor;
                Node[] nodes;
                nodes = (Node[]) event.getOldValue();
                processNodes(nodes, false);
                nodes = (Node[]) event.getNewValue();
                // It seems that when the Java button is selected the very first time, that we get notified
                // of 0 nodes selected.  Lets treat this case as if there were no previous component to force
                // the topComponent == previousTopcomponent to fail
                if (nodes == null || nodes.length ==0)
                    previousTopComponent = null;
                // Only if the new top component is an editor do we want to set the model active
                // When the top component is an editor, we need to prevent sync() to occur while
                // the user is typing or making edits manually.  So tell the model it is active and therefore
                // apt to receive edits that it does not need to sync.  A sync() will occur once the node
                // loses focus.
                if (isTopComponentEditor) {
                    processNodes(nodes, true);
                }
                return;
            }
        }

        // <HACK> XXX NB#49507 Hack until multiview will be able to provide its selected (all) components
        protected TopComponent getActiveTopComponent() {
            TopComponent topComponent = TopComponent.getRegistry().getActivated();
            topComponent = getMultiViewTopComponent(topComponent);
            return topComponent;
        }
        
        protected TopComponent getMultiViewTopComponent(TopComponent topComponent) {
            if(isMultiViewTopComponent(topComponent)) {
                TopComponent selected = getSelectedMultiView(topComponent);
                if(selected == null) {
                    return null;
                }
                topComponent = selected;
            }
            return topComponent;
        }
        
        protected void processTopComponent(TopComponent topComponent, boolean activated) {
            if (topComponent == null)
                return;
            // XXX NB#49507 Hack until multiview will be able to provide its selected (all) components
            topComponent = getMultiViewTopComponent(topComponent);
            // If it is an editor, then assume that if any of its nodes are attached to models,
            // that these models are activated.
            if (topComponent instanceof CloneableEditor) {
                Node[] nodes = topComponent.getActivatedNodes();
                processNodes(nodes, activated);
            }
        }
        
        // XXX NB#49507 Hack until multiview will be able to provide its selected (all) components
        protected boolean isMultiViewTopComponent(TopComponent tc) {
            return tc != null
                && "org.netbeans.core.multiview.MultiViewCloneableTopComponent".equals(tc.getClass().getName()); // NOI18N
        }
        
        // XXX Ugly hack, demand NB to provide API -> NB #49507
        protected TopComponent getSelectedMultiView(TopComponent tc) {
            java.awt.Component[] cs = findDescendantsOfClass(tc, TopComponent.class);
            for(int i = 0; i < cs.length; i++) {
                if(cs[i].isVisible()) { // XXX Means is selected in that multiview (terrible hack)
                    return (TopComponent)cs[i];
                }
            }
            return null;
        }

        // XXX NB#49507 Hack until multiview will be able to provide its selected (all) components
        protected Component[] findDescendantsOfClass(Container parent, Class clazz) {
            List list = new ArrayList();
            Component[] children = parent.getComponents();
            for(int i = 0; i < children.length; i++) {
                Component child = children[i];
                if(clazz.isAssignableFrom(child.getClass())) {
                    list.add(child);
                    continue;
                }
                if(child instanceof Container) {
                    list.addAll(java.util.Arrays.asList(findDescendantsOfClass((Container)child, clazz)));
                }
            }
            return (Component[])list.toArray(new Component[0]);
        }
        // </HACK>
    }
    
    protected static class OpenProjectsListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent event) {
            // The list of open projects has changed, clean up any old projects I may be holding
            // on to.
            // TODO: Should we look for opened ones as well, dont think so, better to create it
            // when needed ?
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(event.getPropertyName())) {
                Project[] openProjectsArray = OpenProjects.getDefault().getOpenProjects();
                IdentityHashMap openProjects = new IdentityHashMap();
                for (int i=0; i < openProjectsArray.length; i++)
                    openProjects.put(openProjectsArray[i], openProjectsArray[i]);
                ArrayList toRemove = new ArrayList();
                synchronized (sets) {
                    for (Iterator i=sets.keySet().iterator(); i.hasNext(); ) {
                        Project project = (Project) i.next();
                        if (!openProjects.containsKey(project)) {
                            ModelSet modelSet = (ModelSet) sets.get(project);
                            toRemove.add(modelSet);
                        }
                    }
                }
                for (Iterator i=toRemove.iterator(); i.hasNext(); ) {
                    ModelSet modelSet = (ModelSet) i.next();
                    modelSet.destroy();
                    fireModelSetRemoved(modelSet);
                }
            }
        }
        
    }
    
    // The following code was cloned from WriteLockUtils.hasActiveLockFileSigns
    // When NB fixes ISSUE #59514, we can get rid of this code
    static final String WriteLock_PREFIX = ".LCK"; //NOI18N    
    static final String WriteLock_SUFFIX = "~"; //NOI18N

    public static boolean hasActiveLockFileSigns(FileObject fileObject) {
        String name = fileObject.getNameExt();
        boolean hasSigns = name.endsWith(WriteLock_SUFFIX) && name.startsWith(WriteLock_PREFIX);
        return hasSigns;
    }

    static {
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(new WindowManagerPropertyRegistry());
        OpenProjects.getDefault().addPropertyChangeListener(new OpenProjectsListener());
    }

    protected static ArrayList modelSetsListeners = new ArrayList();
    
    public static void addModelSetsListener(ModelSetsListener listener) {
        modelSetsListeners.add(listener);
    }
    
    public static void removeModelSetsListener(ModelSetsListener listener) {
        modelSetsListeners.remove(listener);
    }
    
    protected static void fireModelSetAdded(ModelSet modelSet) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        Object[] listeners = modelSetsListeners.toArray();
        for (int i = 0; i < listeners.length; i++) {
            ModelSetsListener listener = (ModelSetsListener) listeners[i];
            listener.modelSetAdded(modelSet);
        }
    }
    
    protected static void fireModelSetRemoved(ModelSet modelSet) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        Object[] listeners = modelSetsListeners.toArray();
        for (int i = 0; i < listeners.length; i++) {
            ModelSetsListener listener = (ModelSetsListener) listeners[i];
            listener.modelSetRemoved(modelSet);
        }
    }
    
    //------------------------------------------------------------------------------ Model Factories

    protected static IdentityHashMap sets = new IdentityHashMap();

    protected static java.util.Collection getFactories() {
        return Lookup.getDefault().lookup(new Lookup.Template(Model.Factory.class)).allInstances();
    }

    protected static ModelSet getInstance(FileObject file, Class ofType) {
        Project project = FileOwnerQuery.getOwner(file);
        return getInstance(project, ofType);
    }

    protected static ModelSet getModelSet(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        ModelSet set = null;
        synchronized (sets) {
            set = (ModelSet)sets.get(project);
        }
        return set;
    }
    
    /**
     * Helper method for sub-classes to be able to get access to a ModelSet of their own specific type.
     * @param project
     * @return
     */
    synchronized protected static ModelSet getInstance(Project project, Class ofType) {
        if (project == null)
            return null;
        ModelSet set = null;
        synchronized (sets) {
            set = (ModelSet) sets.get(project);
        }
        if (set == null && ofType != null) {
            try {
                Constructor constructor = ofType.getConstructor(new Class[] {Project.class});
                set = (ModelSet) constructor.newInstance(new Object[] {project});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return set;
    }

    //--------------------------------------------------------------------------------- Construction

    protected final ClassLoader parentClassLoader;  // classloader to parent the project classloader to
    protected final Project project;
    protected URLClassLoader classLoader;   // current derived project classloader
    protected ClassPath classPath; // needed since we add ourseleves as a dependent
    protected ClassPathListener classPathListener;
    protected FileSystem fileSystem;

    protected final IdentityHashMap configModels = new IdentityHashMap();  // specialized configuration models
    protected final IdentityHashMap models = new IdentityHashMap();        // general models
    protected final IdentityHashMap modelSetListeners = new IdentityHashMap();

    private final OperationListener operationListener = new ModelSetOperationListener();
    
    /**
     * Construct a ModelSet for a given project.
     * <p>
     * This will throw a <code>RuntimeException</code> if a suitable Common ClassLoader Provider is not found.
     *
     * @param project The project that this ModelSet is to be associated with,
     */
    protected ModelSet(Project project) {
        synchronized (sets) {
            sets.put(project, this);
        }
        this.project = project;
        
        CommonClassloaderProvider commonClassloaderProvider = null;
        
        Properties capabilities = new Properties();
        capabilities.put(CommonClassloaderProvider.J2EE_PLATFORM, JsfProjectUtils.getJ2eePlatformVersion(project));
        Result result = Lookup.getDefault().lookup(new Lookup.Template(CommonClassloaderProvider.class));
        for (Iterator iterator = result.allInstances().iterator(); iterator.hasNext();) {
            CommonClassloaderProvider aCommonClassloaderProvider = (CommonClassloaderProvider) iterator.next();
            if (aCommonClassloaderProvider.isCapableOf(capabilities)) {
                commonClassloaderProvider = aCommonClassloaderProvider;
                break;       		
            }
        }

        if (commonClassloaderProvider == null) {
            throw new RuntimeException("No Common Classloader Provider found."); // TODO I18N
        }
        
        parentClassLoader = commonClassloaderProvider.getClassLoader();
		
        // Run thru all the items and create models for those which are ours
        if (project != null) {
            getProjectClassLoader();
            assert Trace.trace("insync.model", "MS.ModelSet ModelCreateVisitor visiting project items in " + Thread.currentThread());
            ModelCreateVisitor visitor = new ModelCreateVisitor();
            for (Iterator i=getSourceRoots().iterator(); i.hasNext(); ) {
                FileObject root = (FileObject) i.next();
                visitor.traverse(root);
            }
            try {
                fileSystem = project.getProjectDirectory().getFileSystem();
            } catch (FileStateInvalidException e) {
            }
            if (fileSystem != null) {
                fileSystem.addFileChangeListener(this);
            }
        }
        // XXX NB issue #81746.
 	DataLoaderPool.getDefault().addOperationListener(
 	(OperationListener)WeakListeners.create(OperationListener.class, operationListener, DataLoaderPool.getDefault()));
        fireModelSetAdded(this);
    }

    protected List getSourceRoots() {
        ArrayList list = new ArrayList();
        FileObject root = JsfProjectUtils.getDocumentRoot(project);
        if (root != null)
            list.add(root);
        root = JsfProjectUtils.getPageBeanRoot(project);
        if (root != null)
            list.add(root);
        return list;
    }
    
    /**
     * Destroy this ModelSet and all its contained Models and release their resources. This ModelSet
     * and the contained Models must never be used after destroy is called.
     */
    public void destroy() {
        if (fileSystem != null) {
            fileSystem.removeFileChangeListener(this);
        }
        releaseProjectClassLoader();
        Model[] ms = getModels();
        models.clear();
        // Make sure that none of the models are valid, such that if outline or any other view
        // wishes to update, they will get an empty list of contexts
//        for (int i = 0; i < ms.length; i++)
//            ms[i].resetOwner();
        for (int i = 0; i < ms.length; i++)
            ms[i].destroy();

        ConfigModel[] cms = getConfigModels();
        configModels.clear();
        for (int i = 0; i < cms.length; i++)
            cms[i].destroy();

        synchronized (sets) {
            sets.remove(project);
        }
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Get the project that this ModelSet is associated with.
     * 
     * @return The project that this ModelSet is associated with.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Get the per-project class loader for this ModelSet. The class loader may change as project
     * settings and libraries are changed by the user.
     * 
     * @return The class loader.
     */
    public URLClassLoader getProjectClassLoader() {
        if (classLoader == null) {
            List urls1List = new ArrayList();

			// Add design time jars from COMPLIBS
            JsfProjectClassPathExtender jsfProjectClassPathExtender = (JsfProjectClassPathExtender) 
                project.getLookup().lookup(JsfProjectClassPathExtender.class);
            
            LibraryManager libraryManager = LibraryManager.getDefault();
            Library[] libraries = libraryManager.getLibraries();
            
            for (int i = 0; i < libraries.length; i++) {
                Library library = libraries[i];
                if (jsfProjectClassPathExtender.hasLibraryReference(library,
                        JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN)) {
                    // TODO The following hardcoded constants are defined in
                    // org.netbeans.modules.visualweb.project.jsf.libraries.provider.ComponentLibraryTypeProvider
                    // However this class is not part of a public package.
                    if (library.getType().equals("complib")) { // NOI18N
                        List urls = library.getContent("visual-web-designtime");
                        List normalizedUrls = new ArrayList();
                        
                        for (Iterator it = urls.iterator(); it.hasNext();) {
                            URL url = (URL) it.next();
                            FileObject fileObject = URLMapper.findFileObject (url);
                            
                            //file inside library is broken
                            if (fileObject == null)
                                continue;
                            
                            if ("jar".equals(url.getProtocol())) {  //NOI18N
                                fileObject = FileUtil.getArchiveFile (fileObject);
                            }
                            File f = FileUtil.toFile(fileObject);                            
                            if (f != null) {
                                try {
                                    URL entry = f.toURI().toURL();
                                    if (FileUtil.isArchiveFile(entry)) {
                                        entry = FileUtil.getArchiveRoot(entry);
                                    } else if (!f.exists()) {
                                        // if file does not exist (e.g. build/classes folder
                                        // was not created yet) then corresponding File will
                                        // not be ended with slash. Fix that.
                                        assert !entry.toExternalForm().endsWith("/") : f; // NOI18N
                                        entry = new URL(entry.toExternalForm() + "/"); // NOI18N
                                    }
                                    normalizedUrls.add(entry);
                                } catch (MalformedURLException mue) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
                                }
                            }
                        }
                        urls1List.addAll(normalizedUrls); // NOI18N
                    }
                }
            }
            
            // !EAT TODO: Is this really the correct way to build the class loader ???
            FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(project);
            classPath = ClassPath.getClassPath(pageBeanRoot, ClassPath.COMPILE);
            FileObject docRoot = JsfProjectUtils.getDocumentRoot(project);
            URLClassLoader projectClassLoader = (URLClassLoader) classPath.getClassLoader(true);
            URL urls[] = projectClassLoader.getURLs();
            
            urls1List.addAll(Arrays.asList(urls));
            
            //Add <project>\build\web\WEB-INF\classes directory into project 
            //classloader classpath.
            //TODO: We need to consider a better approach to achieve this 
            //This may not be required if insync models all the source code
            File docPath = FileUtil.toFile(docRoot);
            File buildClassPath = new File(docPath.getParentFile(), "build" + File.separator + 
                    "web" + File.separator + "WEB-INF" + File.separator + "classes");
            URL buildClassURL = null;
            try {
                buildClassURL = buildClassPath.toURI().toURL();
                urls1List.add(buildClassURL);
            } catch(MalformedURLException mue) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
            }
            
            URL [] urls1 = (URL[]) urls1List.toArray(new URL[0]);
                
//            classLoader = new URLClassLoader(urls1, parentClassLoader);
            classLoader = new ProjectClassLoader(urls1, parentClassLoader);
            classPathListener = new ClassPathListener();
            classPath.addPropertyChangeListener(classPathListener);
        }
        return classLoader;
    }
    
    // XXX To be able to distinguish our specific project classloader during debugging.
    private static class ProjectClassLoader extends URLClassLoader {
        private final URL[] urls;
        
        public ProjectClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.urls = urls;
        }
        
        public String toString() {
            return super.toString() + "[urls=" + (urls == null ? null : Arrays.asList(urls)) + "]"; // NOI18N
        }
    }

    /**
     * Get an array of all of the source Models in this set.
     * @return An array of all of the source Models in this set.
     */
    public Model[] getModels() {
        return (Model[]) getModelsMap().values().toArray(Model.EMPTY_ARRAY);
    }
    
    protected Map getModelsMap() {
        return models;
    }

    /**
     * Get the corresponding source model for a NB file object.
     * 
     * @param file  The NB file object
     * @return The corresponding model.
     */
    public Model getModel(FileObject file) {
        Model model = (Model) getModelsMap().get(file);
        return model;
    }
    
    /**
     * Get an array of all of the configuration models in this set.
     * @return An array of all of the configuration Models in this set.
     */
    public ConfigModel[] getConfigModels() {
        return (ConfigModel[]) getConfigModelsMap().values().toArray(ConfigModel.EMPTY_ARRAY);
    }
    
    protected Map getConfigModelsMap() {
        return configModels;
    }

    /**
     * Get the corresponding configuration model for a NB file object.
     * 
     * @param file  The NB file object
     * @return The corresponding model.
     */
    public ConfigModel getConfigModel(FileObject file) {
        return (ConfigModel) getConfigModelsMap().get(file);
    }

    /**
     * Get the single instance of a configuration model given its type.
     * 
     * @param type  The model type to get
     * @return  The single model instance of the given type.
     */
    public ConfigModel getConfigModel(Class type) {
        for (Iterator i = getConfigModelsMap().values().iterator(); i.hasNext(); ) {
            Model model = (Model)i.next();
            if (type.isInstance(model))
                return (ConfigModel)model;
        }
        return null;
    }

    public void addModelSetListener(ModelSetListener listener) {
        modelSetListeners.put(listener, "");
    }
    
    public void removeModelSetListener(ModelSetListener listener) {
        modelSetListeners.remove(listener);
    }
    
    protected void fireModelAdded(Model model) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        for (Iterator iterator = modelSetListeners.keySet().iterator(); iterator.hasNext(); ) {
            ModelSetListener listener = (ModelSetListener) iterator.next();
            listener.modelAdded(model);
        }
    }
    
    protected void fireModelChanged(Model model) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        for (Iterator iterator = modelSetListeners.keySet().iterator(); iterator.hasNext(); ) {
            ModelSetListener listener = (ModelSetListener) iterator.next();
            listener.modelChanged(model);
        }
    }
    
    protected void fireModelProjectChanged() {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        for (Iterator iterator = modelSetListeners.keySet().iterator(); iterator.hasNext(); ) {
            ModelSetListener listener = (ModelSetListener) iterator.next();
            listener.modelProjectChanged();
        }
    }
    
    protected void fireModelRemoved(Model model) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        for (Iterator iterator = modelSetListeners.keySet().iterator(); iterator.hasNext(); ) {
            ModelSetListener listener = (ModelSetListener) iterator.next();
            listener.modelRemoved(model);
        }
    }
    
    //---------------------------------------------------------------------------------------- Model

    /**
     * Add a new file/model pair to the correct map.
     * @param file  The source or config file object
     * @param m  The source or config model
     */
    protected void addModel(FileObject file, Model m) {
        if (m instanceof ConfigModel)
            configModels.put(file, m);
        else
            models.put(file, m);
        fireModelAdded(m);
    }
    
    private Set modelsToSync;
    
    void addToModelsToSync(Model model) {
        if (modelsToSync == null) {
            modelsToSync = new HashSet();
        }
        modelsToSync.add(model);
    }
    
    public void removeFromModelsToSync(Model model) {
        if (modelsToSync != null) {
            modelsToSync.remove(model);
        }
    }

    /**
     * Synchronize all source and config models with their underlying buffers.
     */
    protected void syncAll() {
        ArrayList errorAccumulator = new ArrayList();
        if (modelsToSync == null) {
            modelsToSync = new HashSet();
            // Due to the fact that there is some resetting of errors and such happening on each sync,
            // we need to gather up the errors and present them at the end
            for (Iterator i = getConfigModelsMap().values().iterator(); i.hasNext(); ) {
                Model model = (Model)i.next();
                if (model.isValid()) {
                    model.sync();
                    ParserAnnotation[] errors = model.getErrors();
                    if (errors.length > 0) {
                        for (int j=0, max=errors.length; j < max; j++) {
                            errorAccumulator.add(errors[j]);
                        }
                    }
                }
            }
            Collection orderedModels = evalOrderModels(getModelsMap().values());
            for (Iterator i = orderedModels.iterator(); i.hasNext(); ) {
                Model model = (Model)i.next();
                if (model.isValid()) {
                    model.sync();
                    ParserAnnotation[] errors = model.getErrors();
                    if (errors.length > 0) {
                        for (int j=0, max=errors.length; j < max; j++) {
                            errorAccumulator.add(errors[j]);
                        }
                    }
                }
            }
        }else {
            for (Iterator it = modelsToSync.iterator(); it.hasNext();) {
                Model model = (Model)it.next();
                it.remove();
                model.sync();
                ParserAnnotation[] errors = model.getErrors();
                if (errors.length > 0) {
                    for (int j=0, max=errors.length; j < max; j++) {
                        errorAccumulator.add(errors[j]);
                    }
                }
            }
        }
        if (errorAccumulator.size() > 0) {
            showSyncErrors(errorAccumulator, true);
        }
    }

    protected void releaseProjectClassLoader() {
        if (classLoader != null) {
            classLoader = null;
        }
        if (classPath != null) {
            classPath.removePropertyChangeListener(classPathListener);
            classPathListener = null;
            classPath = null;
        }
    }
    
    protected void showSyncErrors(ArrayList errors, boolean printPreface) {
        if (errors.size() ==0)
            return;
        if (printPreface) {
            InSyncServiceProvider.get().getRaveErrorHandler().displayError(
                    NbBundle.getMessage(ModelSet.class, "TXT_ErrorsOnOpenProject1")); // NOI18N
            InSyncServiceProvider.get().getRaveErrorHandler().displayError(
                    NbBundle.getMessage(ModelSet.class, "TXT_ErrorsOnOpenProject2")); // NOI18N
        }
        for (Iterator i = errors.iterator(); i.hasNext();) {
            StringBuffer sb = new StringBuffer(200);
            final ParserAnnotation err = (ParserAnnotation) i.next();
            // TODO We should find out why err.getFileObject() returns null, but Tor needs this to fix bug 6349268, even though I could
            // not reproduce to identify the source of the null :(
            if (err.getFileObject() == null) {
                sb.append("unknown");
            } else {
                sb.append(err.getFileObject().getNameExt());
            }
            sb.append(':');
            sb.append(err.getLine());
            sb.append(':');
            sb.append(err.getColumn());
            sb.append(':');
            sb.append(' ');
            sb.append(err.getMessage());
//            // XXX Todo - add output listener suitable for this location
//            OutputListener listener = new OutputListener() {
//                    public void outputLineSelected(OutputEvent ev) {
//                    }
//                    public void outputLineAction(OutputEvent ev) {
//                        // <markup_separation>
////                        Util.show(null, err.getFileObject(), err.getLine(),
////                                  0, true);
//                        // ====
//                        MarkupService.show(err.getFileObject(), err.getLine(), 0, true);
//                        // </markup_separation>
//                    }
//                    public void outputLineCleared (OutputEvent ev) {
//                    }
//                };
//            MarkupService.displayError(sb.toString(), listener);
            InSyncServiceProvider.get().getRaveErrorHandler().displayErrorForFileObject(sb.toString(), err.getFileObject(), err.getLine(), err.getColumn());
        }
        InSyncServiceProvider.get().getRaveErrorHandler().selectErrors();
    }

    /**
     * Flush all source and config models to their underlying buffers.
     */
    protected void flushAll() {
        for (Iterator i = getModelsMap().values().iterator(); i.hasNext(); )
            ((Model)i.next()).flush();
        for (Iterator i = getConfigModelsMap().values().iterator(); i.hasNext(); )
            ((Model)i.next()).flush();
    }
    
    /**
     * Save all source and config models to their underlying buffers.
     */
    protected void saveAll() {
        for (Iterator i = getModelsMap().values().iterator(); i.hasNext(); )
            ((Model)i.next()).saveUnits();
        for (Iterator i = getConfigModelsMap().values().iterator(); i.hasNext(); )
            ((Model)i.next()).saveUnits();
    }
    

    class ClassPathListener  implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            classPathChanged();
        }
    }
    
    class ModelCreateVisitor extends FileObjectVisitor {
        protected ArrayList modelsAdded = new ArrayList();
        
        protected void visitImpl(FileObject file) {
            if (!configModels.containsKey(file) && !models.containsKey(file)) {
                for (Iterator i = getFactories().iterator(); i.hasNext(); ) {
                    Model.Factory mf = (Model.Factory)i.next();
                    Model m = mf.newInstance(ModelSet.this, file);
                    if (m != null) {
                        //FacesModel may be created because of a java file during
                        //CVS update, therefore use primary file from the model
                        //to add into models map
                        addModel(m.getFile(), m);
                        modelsAdded.add(m);
                        break;
                    }
                }
            }
        }
        
        public List getModelsAdded() {
            return modelsAdded;
        }
    }

    /**
     * Respond to changes in the project class path by updating the models.
     *
     * @see com.sun.rave.project.model.ProjectContentChangeListener#classPathChanged(com.sun.rave.project.model.ProjectContentChangeEvent)
     */
    public void classPathChanged() {
        releaseProjectClassLoader();
        getProjectClassLoader();
    }   

    /**
     * Provide models in such a way as to cause higher scoped models to be ordered first.
     * This only works if we assume that lower scoped models references values from higher scoped models.
     * If higher scoped models references lower ones, then this will not help much.
     * 
     * @param modelsToOrder
     * @return
     */
    protected Collection evalOrderModels(Collection modelsToOrder) {
        return modelsToOrder;
    }
    
    protected FileObject getLocalFileObject(FileObject fileObject) {
        if(fileObject == null) {
            return null;
        }
        // What does virtual actually mean, should I handle these as well ?
        if (fileObject.isVirtual())
            return null;
            
        //Check if the file is non sharable
        if (SharabilityQuery.getSharability(FileUtil.toFile(fileObject)) == 
                SharabilityQuery.NOT_SHARABLE) {
            return null;
        }
        if (hasActiveLockFileSigns(fileObject)) {
            return null;
        }
        FileObject projectRoot = getProject().getProjectDirectory();
        if (projectRoot == null || !FileUtil.isParentOf(projectRoot, fileObject)) {
            return null;
        }
        return fileObject;
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }
    
    public void fileFolderCreated (FileEvent fe) {
        // Dont really care about folders do we ?
    }

    public void fileChanged (FileEvent fe) {
        // Do we need to listen to these, dont think so ?
    }

    public void fileDataCreated(FileEvent fe) {
        FileObject fileObject = getLocalFileObject(fe.getFile());
        if (fileObject == null){
            return;
        }
        
        if (fileObject.getAttribute("NBIssue81746Workaround") == Boolean.TRUE) { // NOI18N
            try {
                fileObject.setAttribute("NBIssue81746Workaround", null); // NOI18N
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
            // XXX NB issue #81746
            // This will be handled in the createFromTemplate listener, see the issue.
            return;
        }
        
        processFileDataCreated(fileObject);
    }
    
    private FileObject getOurFileObject(FileObject fileObject) {
        fileObject = getLocalFileObject(fileObject);
        if (fileObject == null)
            return null;
        // we should create Model only if the file is under document root or source root
        if (!FileUtil.isParentOf(JsfProjectUtils.getDocumentRoot(getProject()), fileObject) &&
                !FileUtil.isParentOf(JsfProjectUtils.getSourceRoot(getProject()), fileObject)) {
            return null;
        }
        return fileObject;
    }
    // XXX NB issue #
    private void processFileDataCreated(final FileObject fileObject) {  
        // we should create Model only if the file is under document root or source root
        if (!FileUtil.isParentOf(JsfProjectUtils.getDocumentRoot(getProject()), fileObject) &&
               !FileUtil.isParentOf(JsfProjectUtils.getSourceRoot(getProject()), fileObject)) {
            return;
        }
        
        // Do this outside of refactoring session, as we cannot guarantee when the Java or the JSP file will be
        // "added", this way we wait until everything is done and we have all the files already moved prior
        // to building the models
/*//NB6.0
        MdrInSyncSynchronizer.get().doOutsideOfRefactoringSession(new Runnable() {
            public void run() {
                ModelCreateVisitor visitor = new ModelCreateVisitor();
                visitor.visit(fileObject);
                Collection modelsAdded = visitor.getModelsAdded();
                for (Iterator i = modelsAdded.iterator(); i.hasNext(); ) {
                    Model model = (Model) i.next();
                    // We do a sync here to make sure that the model REALLY is a valid one
                    // The visitor above can create models that should not really be models
                    // but we can only find out once we perform a sync.  If as a result
                    // of the sync, the model has no owner, this indicates that sync destroy'ed
                    // the model and that it should not be a model after all
                    model.sync();
                    if (model.isValid()) {
                        model.saveUnits();
                    } else {
                        models.remove(fileObject);
                        model = null;
                    }
                }
            }
        });
//*/
    }
    
    public void fileDeleted (final FileEvent event) {
        final FileObject fileObject = getLocalFileObject(event.getFile());
        if (fileObject == null) {
            return;
        }
        Model model = getModel(fileObject);
        if (model == null) {
            model = (Model) configModels.get(fileObject);
            if (model == null)
                return;
            configModels.remove(model.getFile());
        } else {
            models.remove(model.getFile());
        }
        final Model finalModel = model;
/*//NB6.0
        MdrInSyncSynchronizer.get().doOutsideOfRefactoringSession(new Runnable() {
            public void run() {
                // There are some elements that get refreshed that assume they are on UI thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        removeModel(finalModel);
                    }
                });
            }
        });
 //*/
    }
    
    public void fileRenamed(final FileRenameEvent event) {
        final FileObject fileObject = event.getFile();
        final String oldName = event.getName();
        final String newName = fileObject.getName();
        final String extension = fileObject.getExt();
        //If the file is renamed to non sharable file(for example during cvs conflicts)
        //it is necessary to remove the model
        final boolean needToRemove = getLocalFileObject(fileObject) == null ? true : false;
        final Model[] models = getModels();
        final Model[] configModels = getConfigModels();
/*//NB6.0
        MdrInSyncSynchronizer.get().doOutsideOfRefactoringSession(new Runnable() {
            public void run() {
                for (int i=0; i < models.length; i++) {
                    Model model = models[i];
                    model.fileRenamed(oldName, newName, extension, fileObject, needToRemove);
                }
                for (int i=0; i < configModels.length; i++) {
                    Model model = configModels[i];
                    model.fileRenamed(oldName, newName, extension, fileObject, needToRemove);
                }
            }
        });
//*/
    }
    
    private /*static*/ class ModelSetOperationListener implements OperationListener {
        public void operationPostCreate(OperationEvent ev) {}
        
        public void operationCopy(OperationEvent.Copy ev) {}
        
        public void operationMove(OperationEvent.Move ev) {}
        
        public void operationDelete(OperationEvent ev) {}
        
        public void operationRename(OperationEvent.Rename ev) {}
        
        public void operationCreateShadow(OperationEvent.Copy ev) {}
        
        public void operationCreateFromTemplate(OperationEvent.Copy ev) {
            FileObject fileObject = getOurFileObject(ev.getObject().getPrimaryFile());
            if (fileObject == null) {
                return;
            }
            
            // XXX NB issue #81746.
            processFileDataCreated(fileObject);
        }
    } // End of ModelSetOperationListener.

    public void removeModel(Model model) {
        FileObject fileObject = model.getFile();
        if (model instanceof ConfigModel)
            configModels.remove(fileObject);
        else
            models.remove(fileObject);
        fireModelRemoved(model);
        model.destroy();
    }

}
