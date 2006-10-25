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

package org.netbeans.core.startup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.ModuleManager;
import org.netbeans.Util;
import org.netbeans.core.startup.layers.ModuleLayeredFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInstall;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.SharedClassObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.InstanceContent;
import org.xml.sax.SAXException;

/** Concrete implementation of the module installation functionality.
 * This class can pay attention to the details of manifest format,
 * details of how to install particular sections and layers and so on.
 * It may have a limited UI, i.e. notifying problems with localized
 * messages and the like.
 * @author Jesse Glick, Jan Pokorsky, Jaroslav Tulach, et al.
 */
final class NbInstaller extends ModuleInstaller {
    
    /** set of manifest sections for each module */
    private final Map<Module,Set<ManifestSection>> sections = new HashMap<Module,Set<ManifestSection>>(100);
    /** ModuleInstall classes for each module that declares one */
    private final Map<Module,Class<? extends ModuleInstall>> installs = new HashMap<Module,Class<? extends ModuleInstall>>(100);
    /** layer resources for each module that declares one */
    private final Map<Module,String> layers = new HashMap<Module,String>(100);
    /** exact use of this is hard to explain */
    private boolean initializedFolderLookup = false;
    /** where to report events to */
    private final Events ev;
    /** associated controller of module list; needed for handling ModuleInstall ser */
    private ModuleList moduleList;
    /** associated manager */
    private ModuleManager mgr;
    /** set of permitted core or package dependencies from a module */
    private final Map<Module,Set<String>> kosherPackages = new HashMap<Module,Set<String>>(100);
    /** Package prefixes passed as special system property. */
    private static String[] specialResourcePrefixes = null;
        
    /** Create an NbInstaller.
     * You should also call {@link #registerManager} and if applicable
     * {@link #registerList} to make the installer fully usable.
     * @param ev events to log
     */
    public NbInstaller(Events ev) {
        this.ev = ev;
    }
    
    /** Access from ModuleSystem. */
    void registerList(ModuleList list) {
        if (moduleList != null) throw new IllegalStateException();
        moduleList = list;
    }
    void registerManager(ModuleManager manager) {
        if (mgr != null) throw new IllegalStateException();
        mgr = manager;
    }

    // @SuppressWarnings("unchecked")
    public void prepare(Module m) throws InvalidException {
        ev.log(Events.PREPARE, m);
        Set<ManifestSection> mysections = null;
        Class<?> clazz = null;
        {
            // Find and load manifest sections.
            for (Map.Entry<String,Attributes> entry : m.getManifest().getEntries().entrySet()) {
                ManifestSection section = ManifestSection.create(entry.getKey(), entry.getValue(), m);
                if (section != null) {
                    if (mysections == null) {
                        mysections = new HashSet<ManifestSection>(25);
                    }
                    mysections.add(section);
                }
            }
        }
        String installClass = m.getManifest().getMainAttributes().getValue("OpenIDE-Module-Install"); // NOI18N
        if (installClass != null) {
            String installClassName;
            try {
                installClassName = Util.createPackageName(installClass);
            } catch (IllegalArgumentException iae) {
                InvalidException ie = new InvalidException(m, iae.toString());
                ie.initCause(iae);
                throw ie;
            }
            if (installClass.endsWith(".ser")) throw new InvalidException(m, "Serialized module installs not supported: " + installClass); // NOI18N
            try {
                // We could simply load the ModuleInstall right away in all cases.
                // However consider a ModuleInstall that has a static or instance
                // initializer or initialize() calling NbBundle.getBundle(String). In the
                // old module installer that would work and it was never specifically discouraged,
                // so that needs to continue working. So do not resolve in most cases;
                // if the module specifically has a validate method, then this is clearly
                // documented to be called before systemClassLoader is ready, so the onus
                // is on the module author to avoid anything dangerous there.
                clazz = Class.forName(installClassName, false, m.getClassLoader());
                if (clazz.getClassLoader() != m.getClassLoader()) {
                    ev.log(Events.WRONG_CLASS_LOADER, m, clazz, m.getClassLoader());
                }
                // Search for a validate() method; if there is none, do not resolve the class now!
                Class<?> c;
                for (c = clazz; c != ModuleInstall.class && c != Object.class; c = c.getSuperclass()) {
                    try {
                        // #13997: do not search in superclasses.
                        c.getDeclaredMethod("validate"); // NOI18N
                        // It has one. We are permitted to resolve the class, create
                        // the installer instance, and validate it.
                        ModuleInstall install = SharedClassObject.findObject(clazz.asSubclass(ModuleInstall.class), true);
                        // The following can throw IllegalStateException, which we would
                        // rethrow as InvalidException below:
                        install.validate();
                    } catch (NoSuchMethodException nsme) {
                        // OK, did not find it here, continue.
                    }
                }
                if (c == Object.class) throw new ClassCastException("Should extend ModuleInstall: " + clazz.getName()); // NOI18N
                // Did not find any validate() method, so remember the class and resolve later.
            } catch (Exception t) {
                InvalidException ie = new InvalidException(m, t.toString());
                ie.initCause(t);
                throw ie;
            } catch (LinkageError t) {
                InvalidException ie = new InvalidException(m, t.toString());
                ie.initCause(t);
                throw ie;
            }
        }
        // For layer & help set, validate only that the base-locale resource
        // exists, not its contents or anything.
        String layerResource = m.getManifest().getMainAttributes().getValue("OpenIDE-Module-Layer"); // NOI18N
        if (layerResource != null) {
            URL layer = m.getClassLoader().getResource(layerResource);
            if (layer == null) throw new InvalidException(m, "Layer not found: " + layerResource); // NOI18N
        }
        String helpSetName = m.getManifest().getMainAttributes().getValue("OpenIDE-Module-Description"); // NOI18N
        if (helpSetName != null) {
            Util.err.warning("Use of OpenIDE-Module-Description in " + m.getCodeNameBase() + " is deprecated.");
            Util.err.warning("(Please install help using an XML layer instead.)");
        }
        // We are OK, commit everything to our cache.
        if (mysections != null) {
            sections.put(m, mysections);
        }
        if (clazz != null) {
            installs.put(m, clazz.asSubclass(ModuleInstall.class));
        }
        if (layerResource != null) {
            layers.put(m, layerResource);
        }
    }
    
    public void dispose(Module m) {
        Util.err.fine("dispose: " + m);
        // Events probably not needed here.
        Set<ManifestSection> s = sections.remove(m);
        if (s != null) {
            for (ManifestSection sect : s) {
                sect.dispose();
            }
        }
        installs.remove(m);
        layers.remove(m);
        kosherPackages.remove(m);
    }
    
    public void load(List<Module> modules) {
        ev.log(Events.START_LOAD, modules);
        
        // we need to update the classloader as otherwise we might not find
        // all the needed classes
        if (mgr != null) { // could be null during tests
            MainLookup.systemClassLoaderChanged(/* #61107: do not use Thread.cCL here! */mgr.getClassLoader());
        }
        ev.log(Events.PERF_TICK, "META-INF/services/ additions registered"); // NOI18N
        
        for (Module m: modules) {
            checkForDeprecations(m);
            openideModuleEnabled(m);
        }
        
        loadLayers(modules, true);
        ev.log(Events.PERF_TICK, "layers loaded"); // NOI18N
	
        ev.log(Events.PERF_START, "NbInstaller.load - sections"); // NOI18N
        ev.log(Events.LOAD_SECTION);
        CoreBridge.conditionallyLoaderPoolTransaction(true);
        try {
            for (Module m: modules) {
                try {
                    loadSections(m, true);
                } catch (Exception t) {
                    Util.err.log(Level.SEVERE, null, t);
                } catch (LinkageError le) {
                    Util.err.log(Level.SEVERE, null, le);
                }
                ev.log(Events.PERF_TICK, "sections for " + m.getCodeName() + " loaded"); // NOI18N
            }
        } finally {
            CoreBridge.conditionallyLoaderPoolTransaction(false);
        }
        ev.log(Events.PERF_END, "NbInstaller.load - sections"); // NOI18N

        // Yarda says to put this here.
        if (! initializedFolderLookup) {
            Util.err.fine("modulesClassPathInitialized");
            MainLookup.modulesClassPathInitialized();
            initializedFolderLookup = true;
        }
        
        // we need to initialize UI before we let modules run ModuleInstall.restore
        Main.initUICustomizations();

        ev.log(Events.PERF_START, "NbInstaller.load - ModuleInstalls"); // NOI18N
        for (Module m: modules) {
            try {
                loadCode(m, true);
            } catch (Exception t) {
                Util.err.log(Level.SEVERE, null, t);
            } catch (LinkageError le) {
                Util.err.log(Level.SEVERE, null, le);
            } catch (AssertionError e) {
                Util.err.log(Level.SEVERE, null, e);
            }
	    ev.log(Events.PERF_TICK, "ModuleInstall for " + m.getCodeName() + " called"); // NOI18N
        }
        ev.log(Events.PERF_END, "NbInstaller.load - ModuleInstalls"); // NOI18N

        ev.log(Events.FINISH_LOAD, modules);
        
        maybeSaveManifestCache();
        
        if (Boolean.getBoolean("netbeans.preresolve.classes")) {
            preresolveClasses(modules);
        }
    }
    
    public void unload(List<Module> modules) {
        ev.log(Events.START_UNLOAD, modules);
        for (Module m: modules) {
            try {
                loadCode(m, false);
            } catch (Exception t) {
                Util.err.log(Level.SEVERE, null, t);
            } catch (LinkageError le) {
                Util.err.log(Level.SEVERE, null, le);
            }
        }
        CoreBridge.conditionallyLoaderPoolTransaction(true);
        try {
            for (Module m: modules) {
                try {
                    loadSections(m, false);
                } catch (Exception t) {
                    Util.err.log(Level.SEVERE, null, t);
                } catch (LinkageError le) {
                    Util.err.log(Level.SEVERE, null, le);
                }
            }
        } finally {
            try {
                CoreBridge.conditionallyLoaderPoolTransaction(false);
            } catch (RuntimeException e) {
                Util.err.log(Level.SEVERE, null, e);
            }
        }
        loadLayers(modules, false);
        ev.log(Events.FINISH_UNLOAD, modules);
    }
    
    /** Load/unload installer code for a module. */
    @SuppressWarnings("deprecation") // old ModuleInstall methods we have to call
    private void loadCode(Module m, boolean load) throws Exception {
        Class<? extends ModuleInstall> instClazz = installs.get(m);
        if (instClazz != null) {
            ModuleInstall inst = SharedClassObject.findObject(instClazz, true);
            if (load) {
                if (moduleList != null) {
                    moduleList.installPrepare(m, inst);
                }
                // restore, install, or upgrade as appropriate
                Object history = m.getHistory();
                if (history instanceof ModuleHistory) {
                    ModuleHistory h = (ModuleHistory)history;
                    if (h.isPreviouslyInstalled()) {
                        // Check whether we have changed versions.
                        SpecificationVersion oldSpec = h.getOldSpecificationVersion();
                        SpecificationVersion nueSpec = m.getSpecificationVersion();
                        if (m.getCodeNameRelease() != h.getOldMajorVersion() ||
                                (oldSpec == null ^ nueSpec == null) ||
                                (oldSpec != null && nueSpec != null && oldSpec.compareTo(nueSpec) != 0)) {
                            // Yes, different version; upgrade from the old one.
                            ev.log(Events.UPDATE, m);
                            inst.updated(h.getOldMajorVersion(), oldSpec == null ? null : oldSpec.toString());
                        } else {
                            // Same version as before.
                            ev.log(Events.RESTORE, m);
                            inst.restored();
                        }
                    } else {
                        // First time.
                        ev.log(Events.INSTALL, m);
                        inst.installed();
                    }
                } else {
                    // Probably fixed module. Just restore.
                    ev.log(Events.RESTORE, m);
                    inst.restored();
                }
                if (moduleList != null) {
                    moduleList.installPostpare(m, inst);
                }
            } else {
                ev.log(Events.UNINSTALL, m);
                inst.uninstalled();
                if (m.getHistory() instanceof ModuleHistory) {
                    ((ModuleHistory)m.getHistory()).resetHistory();
                }
            }
        }
    }
    
    /** Load/unload all manifest sections for a given module. */
    @SuppressWarnings("deprecation") // old ManifestSection.* we have to interpret
    private void loadSections(Module m, boolean load) throws Exception {
        Set<ManifestSection> s = sections.get(m);
        if (s == null) {
            return;
        }
        // Whether we yet had occasion to attach to the module actions list.
        boolean attachedToMA = false;
        try {
	    ev.log(Events.LOAD_SECTION);
            for (ManifestSection sect : s) {
                if (sect instanceof ManifestSection.ActionSection) {
                    if (! attachedToMA) {
                        // First categorize the actions we will add.
                        Object category = m.getLocalizedAttribute("OpenIDE-Module-Display-Category"); // NOI18N
                        if (category == null) {
                            // uncategorized modules group by themselves
                            category = m.getCodeNameBase();
                        }
                        CoreBridge.getDefault().attachToCategory(category);
                        attachedToMA = true;
                    }
                    CoreBridge.getDefault ().loadActionSection((ManifestSection.ActionSection)sect, load);
                } else if (sect instanceof ManifestSection.ClipboardConvertorSection) {
                    loadGenericSection(sect, load);
                } else if (sect instanceof ManifestSection.DebuggerSection) {
                    loadGenericSection(sect, load);
                } else if (sect instanceof ManifestSection.LoaderSection) {
                    CoreBridge.getDefault().loadLoaderSection((ManifestSection.LoaderSection)sect, load);
                } else {
                    assert false : sect;
                }
            }
        } finally {
            if (attachedToMA) {
                CoreBridge.getDefault ().attachToCategory (null);
            }
        }
    }
    
    // Load or unload various possible manifest sections.
    
    /** Simple section that can just be passed to lookup.
     * The lookup sees the real object, not the section.
     * You tell it whether to convert the result to the real
     * instance, or just register the section itself.
     */
    private void loadGenericSection(ManifestSection s, boolean load) {
        CoreBridge.getDefault().loadDefaultSection(s, convertor, load);
    }
    
    private final InstanceContent.Convertor<ManifestSection,Object> convertor = new Convertor();
    private final class Convertor implements InstanceContent.Convertor<ManifestSection,Object> { // or <ManifestSection,SharedClassObject>?
        Convertor() {}
        public Object convert(ManifestSection s) {
            try {
                return s.getInstance();
            } catch (Exception e) {
                Util.err.log(Level.WARNING, null, e);
                // Try to remove it from the pool so it does not continue
                // to throw errors over and over. Hopefully it is kosher to
                // do this while it is in the process of converting! I.e.
                // hopefully InstanceLookup is well-synchronized.
                loadGenericSection(s, false);
                return null;
            }
        }
        public Class<?> type(ManifestSection s) {
            return s.getSuperclass();
        }
        
        /** Computes the ID of the resulted object.
         * @param obj the registered object
         * @return the ID for the object
         */
        public String id(ManifestSection obj) {
            return obj.toString ();
        }
        
        /** The human presentable name for the object.
         * @param obj the registered object
         * @return the name representing the object for the user
         */
        public String displayName(ManifestSection obj) {
            return obj.toString ();
        }
        
    }
    
    /** Either load or unload the layer, if any, for a set of modules.
     * If the parameter load is true, load it, else unload it.
     * Locale/branding variants are likewise loaded or unloaded.
     * If a module has no declared layer, does nothing.
     */
    private void loadLayers(List<Module> modules, boolean load) {
        ev.log(load ? Events.LOAD_LAYERS : Events.UNLOAD_LAYERS, modules);
        // #23609: dependent modules should be able to override:
        modules = new ArrayList<Module>(modules);
        Collections.reverse(modules);
        Map<ModuleLayeredFileSystem,List<URL>> urls = new HashMap<ModuleLayeredFileSystem,List<URL>>(5);
        for (Module m: modules) {
            String s = layers.get(m);
            if (s != null) {
                Util.err.fine("loadLayer: " + s + " load=" + load);
                // Actually add a sequence of layers, in locale order.
                String base, ext;
                int idx = s.lastIndexOf('.'); // NOI18N
                if (idx == -1) {
                    base = s;
                    ext = ""; // NOI18N
                } else {
                    base = s.substring(0, idx);
                    ext = s.substring(idx);
                }
                ClassLoader cl = m.getClassLoader();
                Iterator<String> locit = NbBundle.getLocalizingSuffixes();
                ModuleLayeredFileSystem host;
                // #19458: only put reloadables into the "session layer"
                // (where they will not have their layers cached). All others
                // should go into "installation layer" (so that they can mask
                // layers according to cross-dependencies).
                if (m.isReloadable()) {
                    host = ModuleLayeredFileSystem.getUserModuleLayer();
                } else {
                    host = ModuleLayeredFileSystem.getInstallationModuleLayer();
                }
                List<URL> theseurls = urls.get(host);
                if (theseurls == null) {
                    theseurls = new ArrayList<URL>(100);
                    urls.put(host, theseurls);
                }
                boolean foundSomething = false;
                while (locit.hasNext()) {
                    String suffix = locit.next();
                    String resource = base + suffix + ext;
                    URL u = cl.getResource(resource);
                    if (u != null) {
                        theseurls.add(u);
                        foundSomething = true;
                    }
                }
                if (! foundSomething) {
                    // Should never happen (we already checked in prepare() for base layer)...
                    Util.err.fine("Module layer not found: " + s);
                    continue;
                }
            }
        }
        // Now actually do it.
        for (Map.Entry<ModuleLayeredFileSystem,List<URL>> entry: urls.entrySet()) {
            ModuleLayeredFileSystem host = entry.getKey();
            List<URL> theseurls = entry.getValue();
            Util.err.fine("Adding/removing layer URLs: host=" + host + " urls=" + theseurls);
            try {
                if (load) {
                    host.addURLs(theseurls);
                } else {
                    host.removeURLs(theseurls);
                }
            } catch (Exception e) {
                Util.err.log(Level.WARNING, null, e);
            }
        }
    }
    
    /** Scan a (nondeprecated) module for direct dependencies on deprecated modules.
     * Deprecated modules can quietly depend on other deprecated modules.
     * And if the module is not actually enabled, it does not matter.
     * Indirect dependencies are someone else's problem.
     * Provide-require dependencies do not count either.
     * @param m the module which is now being turned on
     */
    private void checkForDeprecations(Module m) {
        if (!Boolean.valueOf((String)m.getAttribute("OpenIDE-Module-Deprecated")).booleanValue()) { // NOI18N
            for (Dependency dep : m.getDependencies()) {
                if (dep.getType() == Dependency.TYPE_MODULE) {
                    String cnb = (String) Util.parseCodeName(dep.getName())[0];
                    Module o = mgr.get(cnb);
                    if (o == null) throw new IllegalStateException("No such module: " + cnb); // NOI18N
                    if (Boolean.parseBoolean((String) o.getAttribute("OpenIDE-Module-Deprecated"))) { // NOI18N
                        String message = (String)o.getLocalizedAttribute("OpenIDE-Module-Deprecation-Message"); // NOI18N
                        // XXX use NbEvents? I18N?
                        // For now, assume this is a developer-oriented message that need not be localized
                        // or displayed in a pretty fashion.
                        if (message != null) {
                            Util.err.warning("the module " + m.getCodeNameBase() + " uses " + cnb + " which is deprecated: " + message); // NOI18N
                        } else {
                            Util.err.warning("the module " + m.getCodeNameBase() + " uses " + cnb + " which is deprecated."); // NOI18N
                        }
                    }
                }
            }
        }
    }
        
    public boolean closing(List<Module> modules) {
        Util.err.fine("closing: " + modules);
	for (Module m: modules) {
            Class<? extends ModuleInstall> instClazz = installs.get(m);
            if (instClazz != null) {
                try {
                    ModuleInstall inst = SharedClassObject.findObject(instClazz, true);
                    if (! inst.closing()) {
                        Util.err.fine("Module " + m + " refused to close");
                        return false;
                    }
                } catch (RuntimeException re) {
                    Util.err.log(Level.SEVERE, null, re);
                    // continue, assume it is trash
                } catch (LinkageError le) {
                    Util.err.log(Level.SEVERE, null, le);
                }
            }
        }
        return true;
    }
    
    public void close(List<Module> modules) {
        Util.err.fine("close: " + modules);
        ev.log(Events.CLOSE);
        // [PENDING] this may need to write out changed ModuleInstall externalized
        // forms...is that really necessary to do here, or isn't it enough to
        // do right after loading etc.? Currently these are only written when
        // a ModuleInstall has just been restored etc. which is probably fine.
        for (Module m : modules) {
            Class<? extends ModuleInstall> instClazz = installs.get(m);
            if (instClazz != null) {
                try {
                    ModuleInstall inst = SharedClassObject.findObject(instClazz, true);
                    if (inst == null) throw new IllegalStateException("Inconsistent state: " + instClazz); // NOI18N
                    inst.close();
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    // Catch even the heavy stuff here, we are going away.
                    Util.err.log(Level.SEVERE, null, t);
                    // oh well
                }
            }
        }
    }
    
    private AutomaticDependencies autoDepsHandler = null;
    
    /** Overridden to perform automatic API upgrades.
     * That is, should do nothing on new modules, but for older ones will
     * automatically make them depend on things they need.
     * This is now all handled from declarative configuration files:
     * in the system filesystem, ModuleAutoDeps/*.xml may be added
     * according to the DTD "-//NetBeans//DTD Module Automatic Dependencies 1.0//EN".
     */
    public void refineDependencies(Module m, Set<Dependency> dependencies) {
        /* JST-PENDING just tring to comment this out
        // All modules implicitly depend on the APIs somehow.
        if (!m.getCodeNameBase().equals("org.openide") &&
                Util.getModuleDep(dependencies, "org.openide") == null) {
            dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide/1 > 0")); // NOI18N
        }
         */
        if (Boolean.getBoolean("org.netbeans.core.modules.NbInstaller.noAutoDeps")) {
            // Skip them all - useful for unit tests.
            return;
        }
        if (autoDepsHandler == null) {
            FileObject depsFolder = Repository.getDefault().getDefaultFileSystem().findResource("ModuleAutoDeps");
            if (depsFolder != null) {
                FileObject[] kids = depsFolder.getChildren();
                List<URL> urls = new ArrayList<URL>(Math.max(kids.length, 1));
                for (FileObject kid : kids) {
                    if (kid.hasExt("xml")) { // NOI18N
                        try {
                            urls.add(kid.getURL());
                        } catch (FileStateInvalidException e) {
                            Util.err.log(Level.WARNING, null, e);
                        }
                    }
                }
                try {
                    autoDepsHandler = AutomaticDependencies.parse(urls.toArray(new URL[urls.size()]));
                } catch (IOException e) {
                    Util.err.log(Level.WARNING, null, e);
                } catch (SAXException e) {
                    Util.err.log(Level.WARNING, null, e);
                }
            }
            if (autoDepsHandler == null) {
                // Parsing failed, or no files.
                autoDepsHandler = AutomaticDependencies.empty();
            }
            if (Util.err.isLoggable(Level.FINE)) {
                Util.err.fine("Auto deps: " + autoDepsHandler);
            }
        }
        AutomaticDependencies.Report rep = autoDepsHandler.refineDependenciesAndReport(m.getCodeNameBase(), dependencies);
        if (rep.isModified()) {
            Util.err.warning("had to upgrade dependencies for module " + m.getCodeNameBase() + ": added = " + rep.getAdded() + " removed = " + rep.getRemoved() + "; details: " + rep.getMessages());
        }
    }
    
    public String[] refineProvides (Module m) {
        if (m.getCodeNameBase ().equals ("org.openide.modules")) { // NOI18N
            List<String> arr = new ArrayList<String>(4);
            
            boolean isMac = (org.openide.util.Utilities.getOperatingSystem () & org.openide.util.Utilities.OS_MAC) != 0;
            boolean isOS2 = (org.openide.util.Utilities.getOperatingSystem () & org.openide.util.Utilities.OS_OS2) != 0;
            
            if (org.openide.util.Utilities.isUnix ()) {
                arr.add ("org.openide.modules.os.Unix"); // NOI18N
                if (!isMac) {
                    arr.add("org.openide.modules.os.PlainUnix"); // NOI18N
                }
            } 
            
            if (
                org.openide.util.Utilities.isWindows ()
            ) {
                arr.add ("org.openide.modules.os.Windows"); // NOI18N
            } 
            
            if (isMac) {
                arr.add ("org.openide.modules.os.MacOSX"); // NOI18N
            }
            if (isOS2) {
                arr.add ("org.openide.modules.os.OS2"); // NOI18N
            }
            
            // module format is now 1
            arr.add ("org.openide.modules.ModuleFormat1"); // NOI18N
            
            return arr.toArray (new String[0]);
        }
        return null;
    }
    
    public boolean shouldDelegateResource(Module m, Module parent, String pkg) {
        //Util.err.fine("sDR: m=" + m + " parent=" + parent + " pkg=" + pkg);
        // Cf. #19622:
        if (parent == null) {
            // Application classpath checks.
            for (String cppkg : CLASSPATH_PACKAGES) {
                if (pkg.startsWith(cppkg) && !findKosher(m).contains(cppkg)) {
                    // Undeclared use of a classpath package. Refuse it.
                    if (Util.err.isLoggable(Level.FINE)) {
                        Util.err.fine("Refusing to load classpath package " + pkg + " for " + m.getCodeNameBase() + " without a proper dependency"); // NOI18N
                    }
                    return false;
                }
            }
        }
        return true;
    }
    
    private static final String[] CLASSPATH_PACKAGES = new String[] {
        // core.jar shall be inaccessible
        "org/netbeans/core/startup/",
        // Java language infrastructure bundled with IDE; do not want clashes with JDK 6:
        "com/sun/tools/javac/",
        "com/sun/tools/javadoc/",
        "com/sun/javadoc/",
        "com/sun/source/",
        "javax/annotation/",
        "javax/lang/model/",
        "javax/tools/"
    };
    
    private Set<String> findKosher(Module m) {
        Set<String> s = kosherPackages.get(m);
        if (s == null) {
            s = new HashSet<String>();
            Set<Dependency> deps = m.getDependencies();
            SpecificationVersion openide = Util.getModuleDep(deps, "org.openide"); // NOI18N
            boolean pre27853 = (openide == null || openide.compareTo(new SpecificationVersion("1.3.12")) < 0); // NOI18N
            for (Dependency dep : deps) {
                // Extend this for other classpath modules:
                if (dep.getType() == Dependency.TYPE_MODULE &&
                        dep.getName().equals("org.netbeans.core.startup/1")) { // NOI18N
                    // Various modules (incl. o.n.core) dep on this as friends and need to access it.
                    s.add("org/netbeans/core/startup/"); // NOI18N
                } else if (pre27853 && dep.getType() == Dependency.TYPE_MODULE) {
                    // Module dependency. If a package was kosher for A and B depends
                    // on A, we let B use it undeclared. Cf. javacvs -> vcscore & RE.
                    // But #27853: only do this for old modules.
                    String name = dep.getName();
                    int idx = name.indexOf('/');
                    if (idx != -1) {
                        name = name.substring(0, idx);
                    }
                    Module other = mgr.get(name);
                    if (other == null) throw new IllegalStateException("Should have found dep " + dep + " from " + m); // NOI18N
                    s.addAll(findKosher(other));
                } else if (dep.getType() == Dependency.TYPE_PACKAGE) {
                    String depname = dep.getName();
                    String req;
                    int idx = depname.indexOf('['); // NOI18N
                    if (idx == -1) {
                        // depname = org.apache.xerces.parsers
                        // req = org/apache/xerces/parsers/
                        req = depname.replace('.', '/').concat("/"); // NOI18N
                    } else if (idx == 0) {
                        // depname = [org.apache.xerces.parsers.DOMParser]
                        // req = org/apache/xerces/parsers/
                        int idx2 = depname.lastIndexOf('.');
                        req = depname.substring(1, idx2).replace('.', '/').concat("/"); // NOI18N
                    } else {
                        // depname = org.apache.xerces.parsers[DOMParser]
                        // req = org/apache/xerces/parsers/
                        req = depname.substring(0, idx).replace('.', '/').concat("/"); // NOI18N
                    }
                    for (String cppkg : CLASSPATH_PACKAGES) {
                        if (req.startsWith(cppkg)) {
                            // Module requested this exact package or some subpackage or
                            // a class in one of these packages; it is kosher.
                            s.add(cppkg);
                        }
                    }
                }
            }
            if (s.isEmpty()) s = Collections.<String>emptySet();
            kosherPackages.put(m, s);
        }
        return s;
    }

    /** true if optimizations of openide classloading shall be disabled.
     */
    private static boolean withoutOptimizations;
    /** Whenever an openide module is enabled, it is checked to be 
     * on for whose we provide optimizations.
     */
    static void openideModuleEnabled(Module module) {
        String m = module.getCodeNameBase();
        if (!m.startsWith("org.openide.")) {
            return;
        }
        
        if ("org.openide.util".equals(m)) return; // NOI18N
        if ("org.openide.actions".equals(m)) return; // NOI18N
        if ("org.openide.awt".equals(m)) return; // NOI18N
        if ("org.openide.modules".equals(m)) return; // NOI18N
        if ("org.openide.nodes".equals(m)) return; // NOI18N
        if ("org.openide.windows".equals(m)) return; // NOI18N
        if ("org.openide.explorer".equals(m)) return; // NOI18N
        if ("org.openide.util.enumerations".equals(m)) return; // NOI18N
        if ("org.openide.execution".equals(m)) return; // NOI18N
        if ("org.openide.options".equals(m)) return; // NOI18N
        if ("org.openide.execution".equals(m)) return; // NOI18N
        if ("org.openide.loaders".equals(m)) return; // NOI18N
        if ("org.openide.dialogs".equals(m)) return; // NOI18N
        if ("org.openide.filesystems".equals(m)) return; // NOI18N
        if ("org.openide.io".equals(m)) return; // NOI18N
        if ("org.openide.text".equals(m)) return; // NOI18N
        if ("org.openide.src".equals(m)) return; // NOI18N
        
        Util.err.warning("Disabling openide load optimizations due to use of " + m); // NOI18N
        
        withoutOptimizations = true;
    }
    
    /** Information about contents of some JARs on the startup classpath (both lib/ and lib/ext/).
     * The first item in each is a prefix for JAR filenames.
     * For a JAR matching such a prefix, the remaining items are entries
     * from CLASSPATH_PACKAGES.
     * <p>In this case, the JAR is only on a module's effective classpath in case
     * {@link #findKosher} reports that at least one of the packages is requested -
     * in which case that package and any descendants, but not other packages in the JAR,
     * are included in the effective classpath.
     * <p>JARs which are not listed here but are in lib/ or lib/ext/ are assumed to be
     * in the effective classpath of every module, automatically.
     * <p>Note that updater.jar is *not* on the startup classpath and is explicitly
     * added to autoupdate.jar via Class-Path, so it need not concern us here -
     * it should appear on the effective classpath of autoupdate only.
     * @see "#22466"
     */
    private static final String[][] CLASSPATH_JARS = {
        {"core", "org/netbeans/core/", "org/netbeans/beaninfo/"}, // NOI18N
        // No one ought to be using boot.jar:
        {"boot"}, // NOI18N
    };
    
    /** These packages have been refactored into several modules.
     * Disable the domain cache for them.
     */
    public boolean isSpecialResource(String pkg) {
        // JST-PENDING here is experimental enumeration of shared packages in openide
        // maybe this will speed up startup, but it is not accurate as if
        // someone enables some long time deprecated openide jar list will
        // get wrong.
        // Probably I need to watch for list of modules that export org.openide
        // or subclass and if longer than expected, disable this optimization
        
        // the old good way:
        if (pkg.startsWith("org/openide/")) {
            
            // util & dialogs
            if ("org/openide/".equals (pkg)) return true; // NOI18N
            // loaders, actions, and who know what else
            if ("org/openide/actions/".equals (pkg)) return true; // NOI18N
            // loaders & awt
            if ("org/openide/awt/".equals (pkg)) return true; // NOI18N
            // loaders, nodes, text...
            if ("org/openide/cookies/".equals (pkg)) return true; // NOI18N

            // some are provided by java/srcmodel
            if ("org/openide/explorer/propertysheet/editors/".equals (pkg)) return true; // NOI18N

            // windows & io 
            if ("org/openide/windows/".equals (pkg)) return true; // NOI18N

            // text & loaders
            if ("org/openide/text/".equals (pkg)) return true; // NOI18N

            // util & nodes
            if ("org/openide/util/actions/".equals (pkg)) return true; // NOI18N

            if (withoutOptimizations) {
                // these should be removed as soon as we get rid of org-openide-compat
                if ("org/openide/explorer/".equals (pkg)) return true; // NOI18N
                if ("org/openide/util/".equals (pkg)) return true; // NOI18N
            }
        }

        if (isSpecialResourceFromSystemProperty(pkg)) {
            return true;
        }
        
        // Some classes like DOMError are only in xerces.jar, not in JDK:
        if (pkg.equals("org/w3c/dom/")) return true; // NOI18N
        // #36578: JDK 1.5 has DOM3
        if (pkg.equals("org/w3c/dom/ls/")) return true; // NOI18N
        return super.isSpecialResource(pkg);
    }
    
    /**
     * Checks the passed in package for having a prefix one
     * of the strings specified in the system property 
     * "org.netbeans.core.startup.specialResource".
     */
    private boolean isSpecialResourceFromSystemProperty(String pkg) {
        for (String prefix : getSpecialResourcePrefixes()) {
            if (pkg.startsWith(prefix)) {
                return true;
            }
        }
        return false; 
    }
    
    /**
     * Reads the system property 
     * "org.netbeans.core.startup.specialResource"
     * and extracts the comma separated entries in it.
     */
    private static String[] getSpecialResourcePrefixes() {
        if (specialResourcePrefixes == null) {
            String sysProp = System.getProperty("org.netbeans.core.startup.specialResource");
            if (sysProp != null) {
                specialResourcePrefixes = sysProp.split(",");
            } else {
                specialResourcePrefixes = new String[0];
            }
        }
        return specialResourcePrefixes;
    }
    
    /** Get the effective "classpath" used by a module.
     * Specific syntax: classpath entries as usual, but
     * they may be qualified with packages, e.g.:
     * <code>/path/to/api-module.jar[org.netbeans.api.foo.*,org.netbeans.spi.foo.**]:/path/to/wide-open.jar</code>
     * @see ModuleSystem#getEffectiveClasspath
     * @see "#22466"
     */
    String getEffectiveClasspath(Module m) {
        if (!m.isEnabled()) {
            // For disabled modules, we do not know for sure what it can load.
            return ""; // NOI18N
        }
        // The classpath entries - each is a filename possibly followed by package qualifications.
        List<String> l = new ArrayList<String>(100);
        // Start with boot classpath.
        createBootClassPath(l);
        // Move on to "startup classpath", qualified by applicable package deps etc.
        // Fixed classpath modules don't get restricted in this way.
        Set<String> kosher = m.isFixed() ? null : findKosher(m);
        StringTokenizer tok = new StringTokenizer(System.getProperty("java.class.path", ""), File.pathSeparator);
        while (tok.hasMoreTokens()) {
            addStartupClasspathEntry(new File(tok.nextToken()), l, kosher);
        }
        // See org.netbeans.Main for actual computation of the dynamic classpath.
        tok = new StringTokenizer(System.getProperty("netbeans.dynamic.classpath", ""), File.pathSeparator);
        while (tok.hasMoreTokens()) {
            addStartupClasspathEntry(new File(tok.nextToken()), l, kosher);
        }
        // Finally include this module and its dependencies recursively.
        // Modules whose direct classpath has already been added to the list:
        Set<Module> modulesConsidered = new HashSet<Module>(50);
        // Code names of modules on which this module has an impl dependency
        // (so can use any package):
        Set<String> implDeps = new HashSet<String>(10);
        for (Dependency dep : m.getDependencies()) {
            // Remember, provide-require deps do not affect classpath!
            if (dep.getType() == Dependency.TYPE_MODULE && dep.getComparison() == Dependency.COMPARE_IMPL) {
                // We can assume the impl dep has the correct version;
                // otherwise this module could not have been enabled to begin with.
                implDeps.add(dep.getName());
            }
        }
        SpecificationVersion openide = Util.getModuleDep(m.getDependencies(), "org.openide"); // NOI18N
        boolean pre27853 = (openide == null || openide.compareTo(new SpecificationVersion("1.3.12")) < 0); // NOI18N
        // #27853: only make recursive for old modules.
        addModuleClasspathEntries(m, m, modulesConsidered, implDeps, l, pre27853 ? Integer.MAX_VALUE : 1);
        // Done, package it all up as a string.
        StringBuilder buf = new StringBuilder(l.size() * 100 + 1);
        for (String s: l) {
            if (buf.length() > 0) {
                buf.append(File.pathSeparatorChar);
            }
            buf.append(s);
        }
        return buf.toString();
    }
    
    // Copied from NbClassPath:
    private static void createBootClassPath(List<String> l) {
        // boot
        String boot = System.getProperty("sun.boot.class.path"); // NOI18N
        if (boot != null) {
            StringTokenizer tok = new StringTokenizer(boot, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                l.add(tok.nextToken());
            }
        }
        
        // std extensions
        String extensions = System.getProperty("java.ext.dirs"); // NOI18N
        if (extensions != null) {
            for (StringTokenizer st = new StringTokenizer(extensions, File.pathSeparator); st.hasMoreTokens();) {
                File dir = new File(st.nextToken());
                File[] entries = dir.listFiles();
                if (entries != null) {
                    for (File f : entries) {
                        String name = f.getName().toLowerCase(Locale.US);
                        if (name.endsWith(".zip") || name.endsWith(".jar")) { // NOI18N
                            l.add(f.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
    
    /** Add a classpath entry from the lib/ or lib/ext/ dirs, if appropriate.
     * @param entry a classpath entry; either a directory, or a JAR file which might be controlled
     * @param cp the classpath (<code>List&lt;String&gt;</code>) to add to
     * @param kosher known packages which may be accessed (<code>Set&lt;String&gt;</code>), or null for no restrictions
     * @see "#22466"
     */
    private static void addStartupClasspathEntry(File cpEntry, List<String> cp, Set<String> kosher) {
        if (cpEntry.isDirectory()) {
            cp.add(cpEntry.getAbsolutePath());
            return;
        }
        // JAR or ZIP. Check whether we can access it.
        String name = cpEntry.getName();
        for (String[] cpjar : CLASSPATH_JARS) {
            if (kosher != null && name.startsWith(cpjar[0])) {
                // Restricted JAR.
                StringBuffer entry = null; // will be set if there are any packages
                for (int k = 1; k < cpjar.length; k++) {
                    String pkg = cpjar[k];
                    if (kosher.contains(pkg)) {
                        // Module is permitted to use this package.
                        if (entry == null) {
                            entry = new StringBuffer(100);
                            entry.append(cpEntry.getAbsolutePath());
                            entry.append('['); // NOI18N
                        } else {
                            entry.append(','); // NOI18N
                        }
                        // pkg format: org/foo/bar/
                        entry.append(pkg.replace('/', '.')); // NOI18N
                        entry.append("**"); // NOI18N
                    }
                }
                if (entry != null) {
                    // Had >= 1 packages available from this JAR.
                    entry.append(']'); // NOI18N
                    cp.add(entry.toString());
                }
                return;
            }
        }
        // Did not match any, assumed to be on classpath.
        cp.add(cpEntry.getAbsolutePath());
    }

    /** Recursively build a classpath based on the module dependency tree.
     * @param m the current module whose JAR(s) might be added to the classpath
     * @param orig the module whose classpath we are ultimately trying to compute
     * @param considered modules we have already handled
     * @param implDeps module code names on which the orig module has an impl dep
     * @param cp the classpath to add to
     * @param depth the recursion depth to go to: 0 = only m's JAR, 1 = m + its parents, ..., MAX_INT = full recursion
     * @see "#22466"
     */
    private void addModuleClasspathEntries(Module m, Module orig, Set<Module> considered, Set<String> implDeps, List<String> cp, int depth) {
        // Head recursion so that baser modules are added to the front of the classpath:
        if (!considered.add(m)) return;
        for (Dependency dep : m.getDependencies()) {
            if (dep.getType() == Dependency.TYPE_MODULE) {
                String cnb = (String) Util.parseCodeName(dep.getName())[0];
                Module next = mgr.get(cnb);
                if (next == null) throw new IllegalStateException("No such module: " + cnb); // NOI18N
                if (depth > 0) {
                    addModuleClasspathEntries(next, orig, considered, implDeps, cp, depth - 1);
                }
            }
        }
        // Now add entries from m, if applicable.
        // We are friendly if we are considering the original module itself,
        // or if the original module has a (direct) impl dep on this module.
        boolean friend = (m == orig) || implDeps.contains(m.getCodeName());
        // Friend modules export everything to the classpath.
        // Otherwise, there may or may not be package restrictions.
        Module.PackageExport[] exports = friend ? null : m.getPublicPackages();
        String qualification = ""; // NOI18N
        if (exports != null) {
            if (exports.length == 0) {
                // Everything is blocked.
                return;
            }
            // Only certain packages are exported, so list them explicitly.
            StringBuffer b = new StringBuffer(100);
            b.append('['); // NOI18N
            for (int i = 0; i < exports.length; i++) {
                if (i > 0) {
                    b.append(','); // NOI18N
                }
                b.append(exports[i].pkg.replace('/', '.')); // NOI18N
                b.append(exports[i].recursive ? "**" : "*"); // NOI18N
            }
            b.append(']'); // NOI18N
            qualification = b.toString();
        }
        for (File jar : m.getAllJars()) {
            cp.add(jar.getAbsolutePath() + qualification);
        }
    }
    
    // Manifest caching: #26786.
    
    /** The actual file where the manifest cache is stored,
     * or null if it will not be used.
     * Binary format:
     * Sequence of records, one per cached manifest; no particular order.
     * Record:
     * 1. Absolute JAR path, UTF-8.
     * 2. Null byte.
     * 3. Last modification time of JAR, System.currentTimeMillis format, big-endian (8-byte long).
     * 4. The manifest body.
     * 5. Null byte.
     */
    private File manifestCacheFile;
    
    /** While true, try to use the manifest cache.
     * So (non-reloadable) JARs scanned during startup will have their manifests cached.
     * After the primary set of modules has been scanned, this will be set to false.
     * Initially true, unless -J-Dnetbeans.cache.manifests=false is specified,
     * or there is no available cache directory.
     */
    private boolean usingManifestCache;

    {
        usingManifestCache = Boolean.valueOf(System.getProperty("netbeans.cache.manifests", "true")).booleanValue();
        if (usingManifestCache) {
            String userdir = System.getProperty("netbeans.user");
            if (userdir != null) {
                manifestCacheFile = new File(new File(new File(new File (userdir), "var"), "cache"), "all-manifests.dat"); // NOI18N
                Util.err.fine("Using manifest cache in " + manifestCacheFile);
            } else {
                // Some special startup mode, e.g. with Plain.
                usingManifestCache = false;
                Util.err.fine("Not using any manifest cache; no user directory");
            }
        } else {
            Util.err.fine("Manifest cache disabled");
        }
    }
    
    /** Cache of known JAR manifests.
     * Initially null. If the cache is read, it may be used to quickly serve JAR manifests.
     * Each JAR file is mapped to a two-element array consisting of
     * its modification date when last read; and the manifest itself.
     */
    private Map<File,/*[Date,Manifest]*/Object[]> manifestCache = null;
    
    /** If true, at least one manifest has had to be read explicitly.
     * This might be because the cache did not initially exist;
     * or the JAR was not present in the cache;
     * or the JAR was present but did not match the cache timestamp.
     */
    private boolean manifestCacheDirty = false;
    
    // XXX consider logging using Events
    
    /** Overrides superclass method to keep a cache of module manifests,
     * so that their JARs do not have to be opened twice during startup.
     */
    public Manifest loadManifest(File jar) throws IOException {
        if (!usingManifestCache) {
            return super.loadManifest(jar);
        }
        if (manifestCache == null) {
            manifestCache = loadManifestCache(manifestCacheFile);
        }
        Object[] entry = manifestCache.get(jar);
        if (entry != null) {
            if (((Date)entry[0]).getTime() == jar.lastModified()) {
                // Cache hit.
                Util.err.fine("Found manifest for " + jar + " in cache");
                return (Manifest)entry[1];
            } else {
                Util.err.fine("Wrong timestamp for " + jar + " in manifest cache");
            }
        } else {
            Util.err.fine("No entry for " + jar + " in manifest cache");
        }
        // Cache miss.
        Manifest m = super.loadManifest(jar);
        // (If that threw IOException, we leave it out of the cache.)
        manifestCache.put(jar, new Object[] {new Date(jar.lastModified()), m});
        manifestCacheDirty = true;
        return m;
    }
    
    /** If the manifest cache had been in use, and is now dirty, write it to disk.
     */
    private void maybeSaveManifestCache() {
        if (usingManifestCache && manifestCacheDirty) {
            try {
                saveManifestCache(manifestCache, manifestCacheFile);
            } catch (IOException ioe) {
                Util.err.log(Level.WARNING, null, ioe);
            }
            usingManifestCache = false;
            manifestCacheDirty = false;
            manifestCache = null;
            manifestCacheFile = null;
        }
    }
    
    /** Really save the cache.
     * @see #manifestCacheFile
     */
    private void saveManifestCache(Map<File,Object[]> manifestCache, File manifestCacheFile) throws IOException {
        Util.err.fine("Saving manifest cache");
        manifestCacheFile.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(manifestCacheFile);
        try {
            try {
                os = new BufferedOutputStream(os);
                for (Map.Entry<File,Object[]> entry : manifestCache.entrySet()) {
                    File jar = entry.getKey();
                    Object[] v = entry.getValue();
                    long time = ((Date)v[0]).getTime();
                    Manifest m = (Manifest)v[1];
                    os.write(jar.getAbsolutePath().getBytes("UTF-8")); // NOI18N
                    os.write(0);
                    for (int i = 7; i >= 0; i--) {
                        os.write((int)((time >> (i * 8)) & 0xFF));
                    }
                    m.write(os);
                    os.write(0);
                }
            } finally {
                os.close();
            }
        } catch (IOException ioe) {
            // Do not leave behind a bogus half-written file.
            manifestCacheFile.delete();
            throw ioe;
        }
    }
    
    /** Load the cache if present.
     * If not present, or there are problems with it,
     * just create an empty cache.
     * @see #manifestCacheFile
     */
    private Map<File,Object[]> loadManifestCache(File manifestCacheFile) {
        if (!manifestCacheFile.canRead()) {
            Util.err.fine("No manifest cache found at " + manifestCacheFile);
            return new HashMap<File,Object[]>(200);
        }
        ev.log(Events.PERF_START, "NbInstaller - loadManifestCache"); // NOI18N
        try {
            InputStream is = new FileInputStream(manifestCacheFile);
            try {
                BufferedInputStream bis = new BufferedInputStream(is);
                Map<File,Object[]> m = new HashMap<File,Object[]>(200);
                ByteArrayOutputStream baos = new ByteArrayOutputStream((int)manifestCacheFile.length());
                FileUtil.copy(bis, baos);
                byte[] data = baos.toByteArray();
                readManifestCacheEntries(data, m);
                return m;
            } finally {
                is.close();
                ev.log(Events.PERF_END, "NbInstaller - loadManifestCache"); // NOI18N
            }
        } catch (IOException ioe) {
            Util.err.log(Level.WARNING, "While reading: " + manifestCacheFile, ioe); // NOI18N
            return new HashMap<File,Object[]>(200);
        }
    }
    
    private static int findNullByte(byte[] data, int start) {
        int len = data.length;
        for (int i = start; i < len; i++) {
            if (data[i] == 0) {
                return i;
            }
        }
        return -1;
    }
    
    private static void readManifestCacheEntries(byte[] data, Map<File,Object[]> m) throws IOException {
        int pos = 0;
        while (true) {
            if (pos == data.length) {
                return;
            }
            int end = findNullByte(data, pos);
            if (end == -1) throw new IOException("Could not find next manifest JAR name from " + pos); // NOI18N
            File jar = new File(new String(data, pos, end - pos, "UTF-8")); // NOI18N
            long time = 0L;
            if (end + 8 >= data.length) throw new IOException("Ran out of space for timestamp for " + jar); // NOI18N
            for (int i = 0; i < 8; i++) {
                long b = data[end + i + 1];
                if (b < 0) b += 256;
                int exponent = 7 - i;
                long addin = b << (exponent * 8);
                time |= addin;
                //System.err.println("i=" + i + " b=0x" + Long.toHexString(b) + " exponent=" + exponent + " addin=0x" + Long.toHexString(addin) + " time=0x" + Long.toHexString(time));
            }
            pos = end + 9;
            end = findNullByte(data, pos);
            if (end == -1) throw new IOException("Could not find manifest body for " + jar); // NOI18N
            Manifest mani;
            try {
                mani = new Manifest(new ByteArrayInputStream(data, pos, end - pos));
            } catch (IOException ioe) {
                Exceptions.attachMessage(ioe, "While in entry for " + jar);
                throw ioe;
            }
            m.put(jar, new Object[] {new Date(time), mani});
            if (Util.err.isLoggable(Level.FINE)) {
                Util.err.fine("Manifest cache entry: jar=" + jar + " date=" + new Date(time) + " codename=" + mani.getMainAttributes().getValue("OpenIDE-Module"));
            }
            pos = end + 1;
        }
    }
    
    /** Check all module classes to make sure there are no unresolvable compile-time
     * dependencies. Turn on this mode with
     * <code>-J-Dnetbeans.preresolve.classes=true</code>
     * May be more useful to run org.netbeans.core.ValidateClassLinkageTest.
     * @param modules a list of modules, newly enabled, to check; fixed modules will be ignored
     */
    private void preresolveClasses(List<Module> modules) {
        Util.err.info("Pre-resolving classes for all loaded modules...be sure you have not specified -J-Xverify:none in ide.cfg");
        for (Module m : modules) {
            if (m.isFixed()) continue;
            if (m.getJarFile() == null) continue;
            File jar = m.getJarFile();
            // Note: extension JARs not checked.
            try {
                JarFile j = new JarFile(jar, true);
                try {
                    Enumeration e = j.entries();
                    while (e.hasMoreElements()) {
                        JarEntry entry = (JarEntry)e.nextElement();
                        String name = entry.getName();
                        if (name.endsWith(".class")) { // NOI18N
                            String clazz = name.substring(0, name.length() - 6).replace('/', '.'); // NOI18N
                            Throwable t = null;
                            try {
                                Class.forName(clazz, false, m.getClassLoader());
                            } catch (ClassNotFoundException cnfe) { // e.g. "Will not load classes from default package" from ProxyClassLoader
                                t = cnfe;
                            } catch (LinkageError le) {
                                t = le;
                            } catch (RuntimeException re) { // e.g. IllegalArgumentException from package defs
                                t = re;
                            }
                            if (t != null) {
                                Util.err.log(Level.WARNING, "From " + clazz + " in " + m.getCodeNameBase() + " with effective classpath " + getEffectiveClasspath(m), t);
                            }
                        }
                    }
                } finally {
                    j.close();
                }
            } catch (IOException ioe) {
                Util.err.log(Level.WARNING, null, ioe);
            }
        }
    }

}
