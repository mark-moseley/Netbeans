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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.TopologicalSortException;
import org.openide.util.Union2;
import org.openide.util.Utilities;

/** Manages a collection of modules.
 * Must use {@link #mutex} to access its important methods.
 * @author Jesse Glick
 */
public final class ModuleManager {

    public static final String PROP_MODULES = "modules"; // NOI18N
    public static final String PROP_ENABLED_MODULES = "enabledModules"; // NOI18N
    public static final String PROP_CLASS_LOADER = "classLoader"; // NOI18N

    // JST-PENDING: Document in arch. used in org.netbeans.core.startup tests
    // For unit testing only:
    static boolean PRINT_TOPOLOGICAL_EXCEPTION_STACK_TRACES = !Boolean.getBoolean ("suppress.topological.exception"); // NOI18N

    // the modules being managed (not all need be installed)
    private final Set<Module> modules = new HashSet<Module>(100);
    // the same, indexed by code name base
    private final Map<String,Module> modulesByName = new HashMap<String,Module>(100);

    // for any module, set of known failed dependencies or problems,
    // or null if this has not been computed yet
    private final Map<Module,Set<Union2<Dependency,InvalidException>>> moduleProblemsWithoutNeeds = new HashMap<Module,Set<Union2<Dependency,InvalidException>>>(100);
    private final Map<Module,Set<Union2<Dependency,InvalidException>>> moduleProblemsWithNeeds = new HashMap<Module,Set<Union2<Dependency,InvalidException>>>(100);

    // modules providing a given requires token; set may never be empty
    private final Map<String,Set<Module>> providersOf = new HashMap<String,Set<Module>>(25);

    private final ModuleInstaller installer;
    private ModuleFactory moduleFactory;

    private SystemClassLoader classLoader;
    private List<File> classLoaderPatches;
    private final Object classLoaderLock = new String("ModuleManager.classLoaderLock"); // NOI18N

    private final Events ev;

    /** Create a manager, initially with no managed modules.
     * The handler for installing modules is given.
     * Also the sink for event messages must be given.
     */
    public ModuleManager(ModuleInstaller installer, Events ev) {
        this.installer = installer;
        this.ev = ev;
        String patches = System.getProperty("netbeans.systemclassloader.patches");
        if (patches != null) {
            // Probably temporary helper for XTest. By setting this system property
            // to a classpath (list of directories and JARs separated by the normal
            // path separator) you may append to the system class loader.
            System.err.println("System class loader patches: " + patches); // NOI18N
            classLoaderPatches = new ArrayList<File>();
            StringTokenizer tok = new StringTokenizer(patches, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                classLoaderPatches.add(new File(tok.nextToken()));
            }
        } else {
            // Normal case.
            classLoaderPatches = Collections.emptyList();
        }
        classLoader = new SystemClassLoader(classLoaderPatches, new ClassLoader[] {installer.getClass ().getClassLoader()}, Collections.<Module>emptySet());
        updateContextClassLoaders(classLoader, true);
        
        moduleFactory = Lookup.getDefault().lookup(ModuleFactory.class);
        if (moduleFactory == null) {
            moduleFactory = new ModuleFactory();
        } else {
            // Custom module factory might want to replace
            // the system classloader by its own.
            // If it does not want to replace it the following
            // call should not change anything since the system classloader
            // should still be set to ClassLoader.getSystemClassLoader() so
            // the following call will set it to the same value.
            classLoader.setSystemClassLoader(
                moduleFactory.getClasspathDelegateClassLoader(this, 
                    ClassLoader.getSystemClassLoader()));
        }
    }

    /** Access for ManifestSection.
     * @since JST-PENDING needed by ManifestSection
     */
    public final Events getEvents() {
        return ev;
    }

    private final Mutex.Privileged MUTEX_PRIVILEGED = new Mutex.Privileged();
    private final Mutex MUTEX = new Mutex(MUTEX_PRIVILEGED);
    /** Get a locking mutex for this module installer.
     * All calls other than adding or removing property change
     * listeners, or getting the module lookup, called on this
     * class must be done within the scope of this mutex
     * (with read or write access as appropriate). Methods
     * on ModuleInfo need not be called within it; methods
     * specifically on Module do need to be called within it
     * (read access is sufficient). Note that property changes
     * are fired with read access already held for convenience.
     * Please avoid entering the mutex from "sensitive" threads
     * such as the event thread, the folder recognizer/lookup
     * thread, etc., or with other locks held (such as the Children
     * mutex), especially when entering the mutex as a writer:
     * actions such as enabling modules in particular can call
     * arbitrary foreign module code which may do a number of
     * strange things (including consuming a significant amount of
     * time and waiting for other tasks such as lookup or data
     * object recognition). Use the request processor or the IDE's
     * main startup thread or the execution engine to be safe.
     */
    public final Mutex mutex() {
        return MUTEX;
    }
    /** Classes in this package can, if careful, use the privileged form.
     * @since JST-PENDING this had to be made public as the package is now split in two
     */
    public final Mutex.Privileged mutexPrivileged() {
        return MUTEX_PRIVILEGED;
    }
    // [PENDING] with improved API for Mutex, could throw
    // IllegalStateException if any thread attempts to call
    // a controlled method without holding the proper mutex lock

    /** Manages changes accumulating in this manager and fires them when ready.
     */
    private ChangeFirer firer = new ChangeFirer(this);
    /** True while firer is firing changes.
     */
    private boolean readOnly = false;

    /**
     * Release storage for all module manifests.
     * @see Module#releaseManifest
     */
    public void releaseModuleManifests() {
        for (Module m : modules) {
            m.releaseManifest();
        }
    }
    
    /** Sets the r/o flag. Access from ChangeFirer.
     * @param ro if true, cannot make any changes until set to false again
     */
    void readOnly(boolean ro) {
        readOnly = ro;
    }
    /** Assert that the current thread state permits writing.
     * Currently does not check that there is a write mutex!
     * (Pending #13352.)
     * But does check that I am not firing changes.
     * @throws IllegalThreadStateException if currently firing changes
     */
    void assertWritable() throws IllegalThreadStateException {
        if (readOnly) {
            throw new IllegalThreadStateException("You are attempting to make changes to " + this + " in a property change callback. This is illegal. You may only make module system changes while holding a write mutex and not inside a change callback. See #16328."); // NOI18N
        }
    }

    private PropertyChangeSupport changeSupport;

    /** Add a change listener.
     * Only the declared properties will be fired, and they are
     * not guaranteed to be fired synchronously with the change
     * (currently they are not in fact, for safety). The change
     * events are not guaranteed to provide an old and new value,
     * so you will need to use the proper
     * getter methods. When the changes are fired, you are inside
     * the mutex with read access.
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    /** Remove a change listener. */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    // Access from ChangeFirer:
    final void firePropertyChange(String prop, Object old, Object nue) {
        if (Util.err.isLoggable(Level.FINE)) {
            Util.err.fine("ModuleManager.propertyChange: " + prop + ": " + old + " -> " + nue);
        }
        if (changeSupport != null)
            changeSupport.firePropertyChange(prop, old, nue);
    }

    /** For access from Module. */
    final void fireReloadable(Module m) {
        firer.change(new ChangeFirer.Change(m, Module.PROP_RELOADABLE, null, null));
        firer.fire();
    }

    private final Util.ModuleLookup lookup = new Util.ModuleLookup();
    /** Retrieve set of modules in Lookup form.
     * The core top manager should install this into the set of
     * available lookups. Will fire lookup events when the
     * set of modules changes (not for enabling/disabling/etc.).
     * No other subsystem should make any attempt to provide an instance of
     * ModuleInfo via lookup, so an optimization could be to jump
     * straight to this lookup when ModuleInfo/Module is requested.
     */
    public Lookup getModuleLookup() {
        return lookup;
    }
    // Access from ChangeFirer:
    final void fireModulesCreatedDeleted(Set created, Set deleted) {
        Util.err.fine("lookup created: " + created + " deleted: " + deleted);
        lookup.changed();
    }

    /** Get a set of {@link Module}s being managed.
     * No two contained modules may at any time share the same code name base.
     * @see #PROP_MODULES
     */
    public Set<Module> getModules() {
        return new HashSet<Module>(modules);
    }

    /** Get a set of modules managed which are currently enabled.
     * Convenience method only.
     * @see #PROP_ENABLED_MODULES
     */
    public final Set<Module> getEnabledModules() {
        Set<Module> s = new HashSet<Module>(modules);
        Iterator<Module> it = s.iterator();
        while (it.hasNext()) {
            Module m = it.next();
            if (! m.isEnabled()) {
                it.remove();
            }
        }
        return s;
    }

    /** Convenience method to find a module by name.
     * Returns null if there is no such managed module.
     */
    public final Module get(String codeNameBase) {
        return modulesByName.get(codeNameBase);
    }

    /**
     * Get a set of modules depended upon or depending on this module.
     * <p>Note that provide-require/need dependencies are listed alongside direct
     * dependencies; a module with a required token is considered to depend on
     * <em>all</em> modules providing that token (though in fact only one is needed
     * to enable it).
     * <p>Illegal cyclic dependencies are omitted.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @param reverse if true, find modules depending on this module; if false, find
     *                modules this module depends upon
     * @param transitive if true, these dependencies are considered transitively as well
     * @return a set (possibly empty) of modules managed by this manager, never including m
     * @since org.netbeans.core/1 > 1.17
     */
    public Set<Module> getModuleInterdependencies(Module m, boolean reverse, boolean transitive) {
        return Util.moduleInterdependencies(m, reverse, transitive, modules, modulesByName, providersOf);
    }

    /** Get a classloader capable of loading from any
     * of the enabled modules or their declared extensions.
     * Should be used as the result of TopManager.systemClassLoader.
     * Thread-safe.
     * @see #PROP_CLASS_LOADER
     */
    public ClassLoader getClassLoader() {
        // #16265: should not require mutex to get at. Many pieces of the IDE
        // require the correct result immediately.
        synchronized (classLoaderLock) {
            return classLoader;
        }
    }

    /** Mark the current class loader as invalid and make a new one. */
    private void invalidateClassLoader() {
        synchronized (classLoaderLock) {
            classLoader.destroy(); // probably has no effect, but just in case...
        }
        // Set, not List, because if we have >1 bootstrap module (using Plain),
        // it is likely that some of these classloaders will overlap.
        Set<ClassLoader> foundParents = new HashSet<ClassLoader>(modules.size() * 4 / 3 + 2);
        List<ClassLoader> parents = new ArrayList<ClassLoader>(modules.size() + 1);
        ClassLoader base = ModuleManager.class.getClassLoader();
        foundParents.add(base);
        parents.add(base);
        for (Module m : modules) {
            if (! m.isEnabled()) {
                continue;
            }
            if (foundParents.add(m.getClassLoader())) {
                parents.add(m.getClassLoader());
            }
        }
        if (moduleFactory.removeBaseClassLoader()) {
            parents.remove(base);
        }
        ClassLoader[] parentCLs = parents.toArray(new ClassLoader[parents.size()]);
        SystemClassLoader nue;
        try {
            nue = new SystemClassLoader(classLoaderPatches, parentCLs, modules);
        } catch (IllegalArgumentException iae) {
            Util.err.log(Level.WARNING, null, iae);
            nue = new SystemClassLoader(classLoaderPatches, new ClassLoader[] {ModuleManager.class.getClassLoader()}, Collections.<Module>emptySet());
        }
        synchronized (classLoaderLock) {
            classLoader = nue;
            updateContextClassLoaders(classLoader, false);
        }
        firer.change(new ChangeFirer.Change(this, PROP_CLASS_LOADER, null, null));
    }
    private static void updateContextClassLoaders(ClassLoader l, boolean force) {
        // See #20663.
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        while (g.getParent() != null) g = g.getParent();
        // Now g is the master thread group, hopefully.
        // See #4097747 for an explanation of the convoluted logic.
        while (true) {
            int s = g.activeCount() + 1;
            Thread[] ts = new Thread[s];
            int x = g.enumerate(ts, true);
            if (x < s) {
                // We got all of the threads, good.
                for (int i = 0; i < x; i++) {
                    // The first time when we make the manager, set all of the
                    // threads to have this context classloader. Let's hope no
                    // threads needing a special context loader have been started
                    // yet! On subsequent occasions, when the classloader
                    // changes, we update all threads for which setContextClassLoader
                    // has not been called with some other special classloader.
                    if (force || (ts[i].getContextClassLoader() instanceof SystemClassLoader)) {
                        //Util.err.fine("Setting ctxt CL on " + ts[i].getName() + " to " + l);
                        ts[i].setContextClassLoader(l);
                    } else {
                        Util.err.fine("Not touching context class loader " + ts[i].getContextClassLoader() + " on thread " + ts[i].getName());
                    }
                }
                Util.err.fine("Set context class loader on " + x + " threads");
                break;
            } else {
                Util.err.fine("Race condition getting all threads, restarting...");
                continue;
            }
        }
    }

    /** A classloader giving access to all the module classloaders at once. */
    private static final class SystemClassLoader extends JarClassLoader {

        private final PermissionCollection allPermissions;
        private boolean empty = true;
        int size;

        public SystemClassLoader(List<File> files, ClassLoader[] parents, Set<Module> modules) throws IllegalArgumentException {
            super(files, parents, false);
            allPermissions = new Permissions();
            allPermissions.add(new AllPermission());
            allPermissions.setReadOnly();
            size = modules.size();
        }

        protected @Override void finalize() throws Throwable {
            super.finalize();
            Util.err.fine("Collected system class loader");
        }

        public @Override String toString() {
            return "SystemClassLoader[" + size + " modules]";
        }

        /** Provide all permissions for any code loaded from the files list
         * (i.e. with netbeans.systemclassloader.patches).
         */
        protected @Override PermissionCollection getPermissions(CodeSource cs) {
            return allPermissions;
        }

    }

    /** @see #create(File,Object,boolean,boolean,boolean)
     * @deprecated since org.netbeans.core/1 1.3
     */
    @Deprecated
    public Module create(File jar, Object history, boolean reloadable, boolean autoload) throws IOException, DuplicateException {
        return create(jar, history, reloadable, autoload, false);
    }

    /** Create a module from a JAR and add it to the managed set.
     * <p>Will initially be disabled.
     * To make sure all available eager modules get enabled, just call:
     * {@link #enable}({@link Collections#emptySet})
     * <p>May throw an IOException if the JAR file cannot be opened
     * for some reason, or is malformed.
     * <p>If there is already a module of the same name managed,
     * throws a duplicate exception. In this case you may wish
     * to delete the original and try again.
     * <p>You must give it some history object which can be used
     * to provide context for where the module came from and
     * whether it has been here before.
     * <p>You cannot request that a module be both autoload and eager.
     */
    public Module create(File jar, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException, DuplicateException {
        assertWritable();
        ev.log(Events.START_CREATE_REGULAR_MODULE, jar);
        Module m = moduleFactory.create(jar.getAbsoluteFile(),
                        history, reloadable, autoload, eager, this, ev);
        ev.log(Events.FINISH_CREATE_REGULAR_MODULE, jar);
        subCreate(m);
        return m;
    }

    /** Create a fixed module (e.g. from classpath).
     * Will initially be disabled.
     */
    public Module createFixed(Manifest mani, Object history, ClassLoader loader) throws InvalidException, DuplicateException {
        return createFixed(mani, history, loader, false, false);
    }

    /**
     * Create a fixed module (e.g. from classpath) with optional autoload and eager flags.
     * Will initially be disabled.
     * @since 2.7
     */
    public Module createFixed(Manifest mani, Object history, ClassLoader loader, boolean autoload, boolean eager) throws InvalidException, DuplicateException {
        assertWritable();
        if (mani == null || loader == null) throw new IllegalArgumentException("null manifest or loader"); // NOI18N
        ev.log(Events.START_CREATE_BOOT_MODULE, history);
        Module m = moduleFactory.createFixed(mani, history, loader, autoload, eager, this, ev);
        ev.log(Events.FINISH_CREATE_BOOT_MODULE, history);
        subCreate(m);
        return m;
    }

    /** Used by Module to communicate with the ModuleInstaller re. dependencies. */
    void refineDependencies(Module m, Set<Dependency> dependencies) {
        installer.refineDependencies(m, dependencies);
    }
    /** Allows the installer to add provides (used to provide name of platform we run on)
     */
    String[] refineProvides (Module m) {
        return installer.refineProvides (m);
    }
    /** Used by Module to communicate with the ModuleInstaller re. classloader. */
    public void refineClassLoader(Module m, List parents) {
        // #27853:
        installer.refineClassLoader(m, parents);
    }
    /** Use by OneModuleClassLoader to communicate with the ModuleInstaller re. masking. */
    public boolean shouldDelegateResource(Module m, Module parent, String pkg) {
        // Cf. #19621:
        Module.PackageExport[] exports = (parent == null) ? null : parent.getPublicPackages();
        if (exports != null) {
            //Util.err.fine("exports=" + Arrays.asList(exports));
            // Packages from parent are restricted: #19621.
            boolean exported = false;
            if (parent.isDeclaredAsFriend(m)) { // meaning public to all, or at least to me
                for (int i = 0; i < exports.length; i++) {
                    if (exports[i].recursive ? pkg.startsWith(exports[i].pkg) : pkg.equals(exports[i].pkg)) {
                        //Util.err.fine("matches " + exports[i]);
                        exported = true;
                        break;
                    }
                }
            }
            if (!exported) {
                // This package is not public. m must have a direct impl-version
                // dependency on parent or it has no right to use this package.
                boolean impldep = false;
                Dependency[] deps = m.getDependenciesArray();
                for (int i = 0; i < deps.length; i++) {
                    if (deps[i].getType() == Dependency.TYPE_MODULE &&
                            deps[i].getComparison() == Dependency.COMPARE_IMPL &&
                            deps[i].getName().equals(parent.getCodeName())) {
                        impldep = true;
                        //Util.err.fine("impldep in " + deps[i]);
                        break;
                    }
                }
                if (!impldep) {
                    // This module cannot use the package, sorry! It's private.
                    //Util.err.fine("forbidden");
                    if (Util.err.isLoggable(Level.FINE)) {
                        // Note that this is usually harmless. Typical case: Introspector.getBeanInfo
                        // is called on some module-supplied class; this looks in the module's classloader
                        // for org.netbeans.beaninfo.ModuleClassBeanInfo, which of course would not be
                        // found anyway.
                        Util.err.fine("Refusing to load non-public package " + pkg + " for " + m + " from parent module " + parent + " without an impl dependency");
                    }
                    return false;
                }
                //Util.err.fine("impl dep");
            }
            //Util.err.fine("exported");
        }
        if (pkg.startsWith("META-INF/")) { // NOI18N
            // Modules should not make direct reference to metainfo dirs of
            // other modules. Don't bother logging it, however.
            return false;
        }
        // The installer can perform additional checks:
        return installer.shouldDelegateResource(m, parent, pkg);
    }
    // Again, access from Module to ModuleInstaller:
    Manifest loadManifest(File jar) throws IOException {
        return installer.loadManifest(jar);
    }

    private void subCreate(Module m) throws DuplicateException {
        Util.err.fine("created: " + m);
        Module old = get(m.getCodeNameBase());
        if (old != null) {
            throw new DuplicateException(old, m);
        }
        modules.add(m);
        modulesByName.put(m.getCodeNameBase(), m);
        possibleProviderAdded(m);
        lookup.add(m);
        firer.created(m);
        firer.change(new ChangeFirer.Change(this, PROP_MODULES, null, null));
        // It might have been that some other modules were thought to be missing
        // dependencies only because they needed this one. And other modules still
        // might have depended on this one, etc. So forget any cached info about
        // problems arising from inter-module dependencies.
        clearProblemCache();
        firer.fire();
    }
    private void possibleProviderAdded(Module m) {
        String[] provides = m.getProvides();
        for (int i = 0; i < provides.length; i++) {
            Set<Module> providing = providersOf.get(provides[i]);
            if (providing == null) {
                providing = new HashSet<Module>(16);
                providersOf.put(provides[i], providing);
            }
            providing.add(m);
        }
    }

    /** Remove a module from the managed set.
     * Must be disabled first.
     * Must not be a "fixed" module.
     */
    public void delete(Module m) throws IllegalArgumentException {
        assertWritable();
        if (m.isFixed()) throw new IllegalArgumentException("fixed module: " + m); // NOI18N
        if (m.isEnabled()) throw new IllegalArgumentException("enabled module: " + m); // NOI18N
        ev.log(Events.DELETE_MODULE, m);
        modules.remove(m);
        modulesByName.remove(m.getCodeNameBase());
        possibleProviderRemoved(m);
        lookup.remove(m);
        firer.deleted(m);
        firer.change(new ChangeFirer.Change(this, PROP_MODULES, null, null));
        firer.change(new ChangeFirer.Change(m, Module.PROP_VALID, Boolean.TRUE, Boolean.FALSE));
        // #14561: some other module might now be uninstallable as a result.
        clearProblemCache();
        m.destroy();
        firer.fire();
    }
    private void possibleProviderRemoved(Module m) {
        for (String token : m.getProvides()) {
            Set<Module> providing = providersOf.get(token);
            if (providing != null) {
                providing.remove(m);
                if (providing.isEmpty()) {
                    providersOf.remove(token);
                }
            } else {
                // Else we called reload and m.reload threw IOException, so
                // it has already removed its provider list
            }
        }
    }

    /** Reload a module.
     * This could make a fresh copy of its JAR file preparing
     * to enable it with different contents; at least it will
     * rescan the manifest.
     * It must currently be disabled and not "fixed", and it will
     * stay disabled after this call; to actually reinstall it
     * requires a separate call.
     * It may or may not actually be marked "reloadable", but
     * for greatest reliability it should be.
     * Besides actually reloading the contents, any cached information
     * about failed dependencies or runtime problems with the module
     * is cleared so it may be tried again.
     */
    public void reload(Module m) throws IllegalArgumentException, IOException {
        assertWritable();
        // No Events, not a user- nor performance-interesting action.
        Util.err.fine("reload: " + m);
        if (m.isFixed()) throw new IllegalArgumentException("reload fixed module: " + m); // NOI18N
        if (m.isEnabled()) throw new IllegalArgumentException("reload enabled module: " + m); // NOI18N
        possibleProviderRemoved(m);
        try {
            m.reload();
        } catch (IOException ioe) {
            // Module is trash, remove it from our list and pass on the exception.
            delete(m);
            throw ioe;
        }
        possibleProviderAdded(m);
        firer.change(new ChangeFirer.Change(m, Module.PROP_MANIFEST, null, null));
        // Some problem with this module may now have gone away. In turn, some
        // other modules may now no longer have problems. So clear the cache
        // of "soft" problems (interdependencies between modules).
        // Also clear any "hard" problems associated with this module, as they
        // may now have been fixed.
        moduleProblemsWithoutNeeds.remove(m);
        moduleProblemsWithNeeds.remove(m);
        firer.change(new ChangeFirer.Change(m, Module.PROP_PROBLEMS, null, null));
        clearProblemCache();
        firer.fire();
    }

    /** Enable a single module.
     * Must have satisfied its dependencies.
     * Must not be an autoload module, when supported.
     * @see #enable(Set)
     */
    public final void enable(Module m) throws IllegalArgumentException, InvalidException {
        enable(Collections.singleton(m));
    }

    /** Disable a single module.
     * Must not be required by any enabled modules.
     * Must not be an autoload module, when supported.
     * @see #disable(Set)
     */
    public final void disable(Module m) throws IllegalArgumentException {
        disable(Collections.singleton(m));
    }

    /** Enable a set of modules together.
     * <p>Must have satisfied their dependencies
     * (possibly with one another).
     * <p>Must not contain autoload nor eager modules.
     * Might contain fixed modules (they can only be installed once of course).
     * Other modules may become enabled automatically according to {@link #simulateEnable}.
     * <p>It is permissible to pass in modules which in fact at runtime cannot
     * satisfy their package dependencies, or which {@link ModuleInstaller#prepare}
     * rejects on the basis of missing contents. In such a case {@link InvalidException}
     * will be thrown and nothing will be installed. The {@link InvalidException} in such
     * a case should contain a reference to the offending module.
     */
    public void enable(Set<Module> modules) throws IllegalArgumentException, InvalidException {
        assertWritable();
        Util.err.fine("enable: " + modules);
        /* Consider eager modules:
        if (modules.isEmpty()) {
            return;
        }
         */
        ev.log(Events.PERF_START, "ModuleManager.enable"); // NOI18N
        // Basic problems will be caught here, and we also get the autoloads:
        List<Module> toEnable = simulateEnable(modules);
        ev.log(Events.PERF_TICK, "checked the required ordering and autoloads"); // NOI18N

        Util.err.fine("enable: toEnable=" + toEnable); // NOI18N
        {
            // Verify that we are cool as far as basic dependencies go.
            Set<Module> testing = new HashSet<Module>(toEnable);
            if (! testing.containsAll(modules)) {
                Set<Module> bogus = new HashSet<Module>(modules);
                bogus.removeAll(testing);
                throw new IllegalArgumentException("Not all requested modules can be enabled: " + bogus); // NOI18N
            }
            for (Module m : testing) {
                if (!modules.contains(m) && !m.isAutoload() && !m.isEager()) {
                    throw new IllegalArgumentException("Would also need to enable " + m); // NOI18N
                }
            }
        }
        Util.err.fine("enable: verified dependencies");
        ev.log(Events.PERF_TICK, "verified dependencies"); // NOI18N

        ev.log(Events.START_ENABLE_MODULES, toEnable);
        {
            // Actually turn on the listed modules.
            // List of modules that need to be "rolled back".
            LinkedList<Module> fallback = new LinkedList<Module>();
            // Whether we were attempting to bring a classloader up.
            // This affects whether we need to rollback that change on the
            // problem module or not.
            boolean tryingClassLoaderUp = false;
            // If a failure due to package dep occurs, store it here.
            Dependency failedPackageDep = null;
            try {
                ev.log(Events.PERF_START, "module preparation" ); // NOI18N
                for (Module m: toEnable) {
                    fallback.addFirst(m);
                    Util.err.fine("enable: bringing up: " + m);
                    ev.log(Events.PERF_START, "bringing up classloader on " + m.getCodeName() ); // NOI18N
                    try {
                        // Calculate the parents to initialize the classloader with.
                        Dependency[] dependencies = m.getDependenciesArray();
                        Set<Module> parents = new HashSet<Module>(dependencies.length * 4 / 3 + 1);
                        for (int i = 0; i < dependencies.length; i++) {
                            Dependency dep = dependencies[i];
                            if (dep.getType() != Dependency.TYPE_MODULE) {
                                // Token providers do *not* go into the parent classloader
                                // list. The providing module must have been turned on first.
                                // But you cannot automatically access classes from it.
                                continue;
                            }
                            String name = (String)Util.parseCodeName(dep.getName())[0];
                            Module parent = get(name);
                            // Should not happen:
                            if (parent == null) throw new IOException("Parent " + name + " not found!"); // NOI18N
                            parents.add(parent);
                        }
                        m.classLoaderUp(parents);
                    } catch (IOException ioe) {
                        tryingClassLoaderUp = true;
                        InvalidException ie = new InvalidException(m, ioe.toString());
                        ie.initCause(ioe);
                        throw ie;
                    }
                    m.setEnabled(true);
                    ev.log(Events.PERF_END, "bringing up classloader on " + m.getCodeName() ); // NOI18N
                    // Check package dependencies.
//                    ev.log(Events.PERF_START, "package dependency check on " + m.getCodeName() ); // NOI18N
                    Util.err.fine("enable: checking package dependencies for " + m);
                    Dependency[] dependencies = m.getDependenciesArray();
                    for (int i = 0; i < dependencies.length; i++) {
                        Dependency dep = dependencies[i];
                        if (dep.getType() != Dependency.TYPE_PACKAGE) {
                            continue;
                        }
                        if (! Util.checkPackageDependency(dep, m.getClassLoader())) {
                            failedPackageDep = dep;
                            throw new InvalidException(m, "Dependency failed on " + dep); // NOI18N
                        }
                        Util.err.fine("Successful check for: " + dep);
                    }
//                    ev.log(Events.PERF_END, "package dependency check on " + m.getCodeName() ); // NOI18N
                    // Prepare to load it.
                    ev.log(Events.PERF_START, "ModuleInstaller.prepare " + m.getCodeName() ); // NOI18N
                    installer.prepare(m);
                    ev.log(Events.PERF_END, "ModuleInstaller.prepare " + m.getCodeName() ); // NOI18N
                }
                ev.log(Events.PERF_END, "module preparation" ); // NOI18N

            } catch (InvalidException ie) {
                // Remember that there was a problem with this guy.
                Module bad = ie.getModule();
                if (bad == null) throw new IllegalStateException("Problem with no associated module: " + ie); // NOI18N
                Set<Union2<Dependency,InvalidException>> probs = moduleProblemsWithNeeds.get(bad);
                if (probs == null) throw new IllegalStateException("Were trying to install a module that had never been checked: " + bad); // NOI18N
                if (! probs.isEmpty()) throw new IllegalStateException("Were trying to install a module that was known to be bad: " + bad); // NOI18N
                // Record for posterity.
                if (failedPackageDep != null) {
                    // Structured package dependency failed, track this.
                    probs.add(Union2.<Dependency,InvalidException>createFirst(failedPackageDep));
                } else {
                    // Some other problem (exception).
                    probs.add(Union2.<Dependency,InvalidException>createSecond(ie));
                }
                // Other modules may have depended on this one and now will not be OK.
                // So clear all "soft" problems from the cache.
                // Remember, the problem we just added will be left alone, only
                // inter-module dependencies will be cleared.
                clearProblemCache();
                // #14560: this one definitely changed its set of problems.
                firer.change(new ChangeFirer.Change(bad, Module.PROP_PROBLEMS, Collections.EMPTY_SET, Collections.singleton("something"))); // NOI18N
                // Rollback changes made so far before rethrowing.
                Util.err.fine("enable: will roll back from: " + ie);
                for (Module m : fallback) {
                    if (m.isFixed()) {
                        // cannot disable fixed modules
                        continue;
                    }
                    m.setEnabled(false);
                    if (tryingClassLoaderUp) {
                        // OK, taken into account for first module, others are up.
                        tryingClassLoaderUp = false;
                    } else {
                        m.classLoaderDown();
                        System.gc();
                        System.runFinalization();
                        m.cleanup();
                    }
                }
                firer.fire();
                throw ie;
            }
            // They all were OK so far; add to system classloader and install them.
            if (classLoader != null) {
                Util.err.fine("enable: adding to system classloader");
                LinkedList<ClassLoader> nueclassloaders = new LinkedList<ClassLoader>();
                if (moduleFactory.removeBaseClassLoader()) {
                    ClassLoader base = ModuleManager.class.getClassLoader();
                    nueclassloaders.add(moduleFactory.getClasspathDelegateClassLoader(this, base));
                    for (Module m : toEnable) {
                        ClassLoader c1 = m.getClassLoader();
                        if (c1 != base) {
                            nueclassloaders.add(c1);
                        }
                    }
                } else {
                    for (Module m : toEnable) {
                        if (m.getClassLoader() == ClassLoader.getSystemClassLoader()) {
                            nueclassloaders.addFirst(m.getClassLoader());
                        } else {
                            nueclassloaders.add(m.getClassLoader());
                        }
                    }
                }
                classLoader.append((nueclassloaders.toArray(new ClassLoader[nueclassloaders.size()])));
                classLoader.size += toEnable.size();
            } else {
                Util.err.fine("enable: no class loader yet, not appending");
            }
            Util.err.fine("enable: continuing to installation");
            installer.load(toEnable);
        }
        {
            // Take care of notifying various changes.
            Util.err.fine("enable: firing changes");
            firer.change(new ChangeFirer.Change(this, PROP_ENABLED_MODULES, null, null));
            // The class loader does not actually change as a result of this.
            for (Module m : toEnable) {
                firer.change(new ChangeFirer.Change(m, ModuleInfo.PROP_ENABLED, Boolean.FALSE, Boolean.TRUE));
                if (! m.isFixed()) {
                    firer.change(new ChangeFirer.Change(m, Module.PROP_CLASS_LOADER, null, null));
                }
            }
        }
        ev.log(Events.FINISH_ENABLE_MODULES, toEnable);
        firer.fire();
    }

    /** Disable a set of modules together.
     * <p>Must not be required by any enabled
     * modules (except one another).
     * <p>Must not contain autoload nor eager modules.
     * Must not contain fixed modules.
     * Other modules may become disabled automatically according to {@link #simulateDisable}.
     */
    public void disable(Set<Module> modules) throws IllegalArgumentException {
        assertWritable();
        Util.err.fine("disable: " + modules);
        if (modules.isEmpty()) {
            return;
        }
        // Checks for invalid items, plus includes autoloads to turn off.
        List<Module> toDisable = simulateDisable(modules);
        Util.err.fine("disable: toDisable=" + toDisable);
        {
            // Verify that dependencies are OK.
            for (Module m: toDisable) {
                if (!modules.contains(m) && !m.isAutoload() && !m.isEager()) {
                    throw new IllegalArgumentException("Would also need to disable: " + m); // NOI18N
                }
            }
        }
        Util.err.fine("disable: verified dependencies");
        ev.log(Events.START_DISABLE_MODULES, toDisable);
        {
            // Actually turn off all modules.
            installer.unload(toDisable);
            for (Module m : toDisable) {
                installer.dispose(m);
                m.setEnabled(false);
                m.classLoaderDown();
            }
            System.gc(); // hope OneModuleClassLoader.finalize() is called...
            System.runFinalization();
            // but probably it won't be. See #4405807.
            for (Module m : toDisable) {
                m.cleanup();
            }
        }
        Util.err.fine("disable: finished, will notify changes");
        {
            // Notify various changes.
            firer.change(new ChangeFirer.Change(this, PROP_ENABLED_MODULES, null, null));
            // Class loader will change as a result.
            invalidateClassLoader();
            for (Module m : toDisable) {
                firer.change(new ChangeFirer.Change(m, ModuleInfo.PROP_ENABLED, Boolean.TRUE, Boolean.FALSE));
                firer.change(new ChangeFirer.Change(m, Module.PROP_CLASS_LOADER, null, null));
            }
        }
        ev.log(Events.FINISH_DISABLE_MODULES, toDisable);
        firer.fire();
    }

    /** Simulate what would happen if a set of modules were to be enabled.
     * None of the listed modules may be autoload modules, nor eager, nor currently enabled,
     * though they may be fixed (if they have not yet been enabled).
     * It may happen that some of them do not satisfy their dependencies.
     * It may also happen that some of them require other, currently disabled,
     * modules to be enabled in order for them to be enabled.
     * It may further happen that some currently disabled eager modules could
     * be enabled as a result of these modules being enabled.
     * The returned set is the set of all modules that actually could be enabled.
     * It will include the requested modules, minus any that cannot satisfy
     * their dependencies (even on each other), plus any managed but currently
     * disabled modules that would need to be enabled (including autoload modules
     * required by some listed module but not by any currently enabled module),
     * plus any eager modules which can be enabled with the other enablements
     * (and possibly any autoloads needed by those eager modules).
     * Where a requested module requires some token, either it will not be included
     * in the result (in case the dependency cannot be satisfied), or it will, and
     * all modules providing that token which can be included will be included, even
     * if it would suffice to choose only one - unless a module providing that token
     * is already enabled or in the requested list,
     * in which case just the requested module will be listed.
     * Modules are returned in an order in which they could be enabled (where
     * base modules are always enabled before dependent modules).
     * Note that the returned list might include modules which in fact cannot be
     * enabled either because some package dependencies (which are checked only
     * on a live classloader) cannot be met; or {@link ModuleInstaller#prepare}
     * indicates that the modules are not in a valid format to install; or
     * creating the module classloader fails unexpectedly.
     */
    public List<Module> simulateEnable(Set<Module> modules) throws IllegalArgumentException {
        /* Not quite, eager modules may change this:
        if (modules.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
         */
        // XXX also optimize for modules.size == 1
        Set<Module> willEnable = new HashSet<Module>(modules.size() * 2 + 1);
        for (Module m: modules) {
            if (m.isAutoload()) throw new IllegalArgumentException("Cannot simulate enabling an autoload: " + m); // NOI18N
            if (m.isEager()) throw new IllegalArgumentException("Cannot simulate enabling an eager module: " + m); // NOI18N
            if (m.isEnabled()) throw new IllegalArgumentException("Already enabled: " + m); // NOI18N
            if (!m.isValid()) throw new IllegalArgumentException("Not managed by me: " + m + " in " + m); // NOI18N
            maybeAddToEnableList(willEnable, modules, m, true);
        }
        // XXX clumsy but should work:
        Set<Module> stillDisabled = new HashSet<Module>(this.modules);
        Iterator<Module> it = stillDisabled.iterator();
        while (it.hasNext()) {
            Module m = it.next();
            if (m.isEnabled() || willEnable.contains(m)) {
                it.remove();
            }
        }
        while (searchForPossibleEager(willEnable, stillDisabled, modules)) {/* search again */}
        Map<Module,List<Module>> deps = Util.moduleDependencies(willEnable, modulesByName, providersOf);
        try {
            List<Module> l = Utilities.topologicalSort(willEnable, deps);
            Collections.reverse(l);
            return l;
        } catch (TopologicalSortException ex) {
            // Some kind of cycle involving prov-req deps. Should be extremely rare.
            // Example (from random failures of MMT.testProvReqCycles):
            // m1 => {m2 | m3}
            // m2 => {m1 | m4}
            // m3 => {m1}
            // m4 => {}
            // Now consider:
            // sE(m2) = ?
            // [m4, m2] is fine.
            // [m4, m2, m1] would be OK too, but will result in TSE.
            // Do not know what to do here, actually, so give up.
            if (PRINT_TOPOLOGICAL_EXCEPTION_STACK_TRACES) {
                Util.err.log(Level.WARNING, null, ex);
            }
            Util.err.warning("Cyclic module dependencies, will refuse to enable: " + deps); // NOI18N
            return Collections.<Module>emptyList();
        }
    }
    private void maybeAddToEnableList(Set<Module> willEnable, Set<Module> mightEnable, Module m, boolean okToFail) {
        if (! missingDependencies(m).isEmpty()) {
            assert okToFail : "Module " + m + " had unexpected problems: " + missingDependencies(m) + " (willEnable: " + willEnable + " mightEnable: " + mightEnable + ")";
            // Cannot satisfy its dependencies, exclude it.
            return;
        }
        if (!willEnable.add(m)) {
            // Already there, done.
            return;
        }
        // Also add anything it depends on, if not already there,
        // or already enabled.
        for (Dependency dep : m.getDependenciesArray()) {
            if (dep.getType() == Dependency.TYPE_MODULE) {
                String codeNameBase = (String)Util.parseCodeName(dep.getName())[0];
                Module other = get(codeNameBase);
                // Should never happen:
                if (other == null) throw new IllegalStateException("Should have found module: " + codeNameBase); // NOI18N
                if (! other.isEnabled()) {
                    maybeAddToEnableList(willEnable, mightEnable, other, false);
                }
            } else if (
                dep.getType() == Dependency.TYPE_REQUIRES || 
                dep.getType() == Dependency.TYPE_NEEDS ||
                dep.getType() == Dependency.TYPE_RECOMMENDS
            ) {
                Set<Module> providers = providersOf.get(dep.getName());
                if (providers == null) {
                    assert dep.getType() == Dependency.TYPE_RECOMMENDS : "Should have found a provider of " + dep;
                    continue;
                }
                // First check if >= 1 is already enabled or will be soon. If so, great.
                boolean foundOne = false;
                for (Module other : providers) {
                    if (other.isEnabled() ||
                            (other.getProblems().isEmpty() && mightEnable.contains(other))) {
                        foundOne = true;
                        break;
                    }
                }
                if (foundOne) {
                    // OK, we are satisfied.
                    continue;
                }
                // All disabled. So add them all to the enable list.
                for (Module other : providers) {
                    // It is OK if one of them fails.
                    maybeAddToEnableList(willEnable, mightEnable, other, true);
                    // But we still check to ensure that at least one did not!
                    if (!foundOne && willEnable.contains(other)) {
                        foundOne = true;
                        // and continue with the others, try to add them too...
                    }
                }
                // Logic is that missingDependencies(m) should contain dep in this case.
                assert foundOne || dep.getType() == Dependency.TYPE_RECOMMENDS : "Should have found a nonproblematic provider of " + dep + " among " + providers + " with willEnable=" + willEnable + " mightEnable=" + mightEnable;
            }
            // else some other kind of dependency that does not concern us
        }
    }
    private boolean searchForPossibleEager(Set<Module> willEnable, Set<Module> stillDisabled, Set<Module> mightEnable) {
        // Check for any eagers in stillDisabled which could be enabled based
        // on currently enabled modules and willEnable. For any such, remove from
        // stillDisabled and add to willEnable (using maybeAddToEnableList, so that
        // autoloads needed by them are picked up too). If any were found, return true.
        boolean found = false;
        Iterator<Module> it = stillDisabled.iterator();
    FIND_EAGER:
        while (it.hasNext()) {
            Module m = it.next();
            if (willEnable.contains(m)) {
                // Presumably real module M1, eager M2 dep. on M1, eager M3 dep.
                // on M2; already called couldBeEnabledWithEagers(M3) and it
                // added M3 to willEnable (thus M2 also) but only removed M3
                // from willEnable, so we should skip it now.
                it.remove();
                continue;
            }
            if (m.isEager()) {
                if (couldBeEnabledWithEagers(m, willEnable, new HashSet<Module>())) {
                    // Go for it!
                    found = true;
                    it.remove();
                    maybeAddToEnableList(willEnable, mightEnable, m, false);
                }
            }
        }
        return found;
    }
    private boolean couldBeEnabledWithEagers(Module m, Set<Module> willEnable, Set<Module> recursion) {
        // True if a search of the dependencies of this module reveals
        // only modules which are currently enabled; in the willEnable
        // list; or are autoloads or eager modules for which this predicate
        // is recursively true.
        if (m.isEnabled() || willEnable.contains(m)) return true;
        if (!m.isAutoload() && !m.isEager()) return false;
        if (!m.getProblems().isEmpty()) return false;
        if (!recursion.add(m)) {
            // A cycle, they can enable one another...
            return true;
        }
        Dependency[] dependencies = m.getDependenciesArray();
        for (int i = 0; i < dependencies.length; i++) {
            Dependency dep = dependencies[i];
            if (dep.getType() == Dependency.TYPE_MODULE) {
                String codeNameBase = (String)Util.parseCodeName(dep.getName())[0];
                Module other = get(codeNameBase);
                // Should never happen:
                if (other == null) throw new IllegalStateException("Should have found module: " + codeNameBase); // NOI18N
                if (!couldBeEnabledWithEagers(other, willEnable, recursion)) return false;
            } else if (dep.getType() == Dependency.TYPE_REQUIRES) {
                Set<Module> providers = providersOf.get(dep.getName());
                if (providers == null) throw new IllegalStateException("Should have found a provider of: " + dep.getName()); // NOI18N
                // Just need *one* to match.
                boolean foundOne = false;
                for (Module other : providers) {
                    if (couldBeEnabledWithEagers(other, willEnable, recursion)) {
                        foundOne = true;
                        break;
                    }
                }
                if (!foundOne) return false;
            }
            // else some other dep type
        }
        return true;
    }

    /** Simulate what would happen if a set of modules were to be disabled.
     * None of the listed modules may be autoload modules, nor eager, nor currently disabled, nor fixed.
     * The returned set will list all modules that would actually be disabled,
     * meaning the listed modules, plus any currently enabled but unlisted modules
     * (including autoloads) that require some listed modules, plus any autoloads
     * which would no longer be needed as they were only required by modules
     * otherwise disabled.
     * Provide-require pairs count for purposes of disablement: if the set of
     * requested modules includes all remaining enabled providers of some token,
     * and modules requiring that token will need to be disabled as well.
     * Modules are returned in an order in which they could be disabled (where
     * dependent modules are always disabled before base modules).
     */
    public List<Module> simulateDisable(Set<Module> modules) throws IllegalArgumentException {
        if (modules.isEmpty()) {
            return Collections.<Module>emptyList();
        }
        // XXX also optimize for modules.size == 1
        // Probably not a very efficient algorithm. But it probably does not need to be.
        Set<Module> willDisable = new HashSet<Module>(20);
        for (Module m : modules) {
            if (m.isAutoload()) throw new IllegalArgumentException("Cannot disable autoload: " + m); // NOI18N
            if (m.isEager()) throw new IllegalArgumentException("Cannot disable eager module: " + m); // NOI18N
            if (m.isFixed()) throw new IllegalArgumentException("Cannot disable fixed module: " + m); // NOI18N
            if (! m.isEnabled()) throw new IllegalArgumentException("Already disabled: " + m); // NOI18N
            addToDisableList(willDisable, m);
        }
        Set<Module> stillEnabled = new HashSet<Module>(getEnabledModules());
        stillEnabled.removeAll(willDisable);
        while (searchForUnusedAutoloads(willDisable, stillEnabled)) {/* search again */}
        Map<Module,List<Module>> deps = Util.moduleDependencies(willDisable, modulesByName, providersOf);
        try {
            return Utilities.topologicalSort(willDisable, deps);
        } catch (TopologicalSortException ex) {
            // Again, don't know what to do exactly, so give up and just turn them off.
            if (PRINT_TOPOLOGICAL_EXCEPTION_STACK_TRACES) {
                Util.err.log(Level.WARNING, null, ex);
            }
            Util.err.warning("Cyclic module dependencies, will turn them off in a random order: " + deps); // NOI18N
            return new ArrayList<Module>(willDisable);
        }
    }
    private void addToDisableList(Set<Module> willDisable, Module m) {
        if (willDisable.contains(m)) {
            // E.g. if original set had A then B, B depends on A.
            return;
        }
        willDisable.add(m);
        // Find any modules depending on this one which are currently enabled.
        // (And not already here.)
        // If there are any, add them.
        for (Module other : modules) {
            if (other.isFixed() || ! other.isEnabled() || willDisable.contains(other)) {
                continue;
            }
            Dependency[] depenencies = other.getDependenciesArray();
            for (int i = 0; i < depenencies.length; i++) {
                Dependency dep = depenencies[i];
                if (dep.getType() == Dependency.TYPE_MODULE) {
                    if (dep.getName().equals(m.getCodeName())) {
                        // Need to disable this one too.
                        addToDisableList(willDisable, other);
                        // No need to scan the rest of its dependencies.
                        break;
                    }
                } else if (
                    dep.getType() == Dependency.TYPE_REQUIRES || 
                    dep.getType() == Dependency.TYPE_NEEDS
                ) {
                    if (m.provides(dep.getName())) {
                        // Careful. There may be some third module still enabled which
                        // provides this same token too.
                        boolean foundOne = false;
			for (Module third: getEnabledModules()) {
                            if (third.isEnabled() &&
                                    !willDisable.contains(third) &&
                                    third.provides(dep.getName())) {
                                foundOne = true;
                                break;
                            }
                        }
                        if (!foundOne) {
                            // Nope, we were the only/last one to provide it.
                            addToDisableList(willDisable, other);
                            break;
                        }
                    }
                }
                // else some other kind of dependency, we do not care
            }
        }
    }
    private boolean searchForUnusedAutoloads(Set<Module> willDisable, Set<Module> stillEnabled) {
        // Check for any autoloads in stillEnabled which are not used by anything else
        // in stillEnabled. For each such, remove it from stillEnabled and add
        // to willDisable. If any were found, return true.
        boolean found = false;
        Iterator<Module> it = stillEnabled.iterator();
    FIND_AUTOLOADS:
        while (it.hasNext()) {
            Module m = it.next();
            if (m.isAutoload()) {
                for (Module other: stillEnabled) {
                    Dependency[] dependencies = other.getDependenciesArray();
                    for (int i = 0; i < dependencies.length; i++) {
                        Dependency dep = dependencies[i];
                        if (dep.getType() == Dependency.TYPE_MODULE) {
                            if (Util.parseCodeName(dep.getName())[0].equals(m.getCodeNameBase())) {
                                // Still used, skip it.
                                continue FIND_AUTOLOADS;
                            }
                        } else if (
                            dep.getType() == Dependency.TYPE_REQUIRES ||
                            dep.getType() == Dependency.TYPE_NEEDS ||
                            dep.getType() == Dependency.TYPE_RECOMMENDS
                        ) {
                            // Here we play it safe and leave autoloads on if they provide
                            // something used by some module - even if technically it would
                            // be possible to turn off the autoload because there is another
                            // enabled module providing the same thing. Leave it on anyway.
                            if (m.provides(dep.getName())) {
                                continue FIND_AUTOLOADS;
                            }
                        }
                        // else some other type
                    }
                }
                // Nobody uses it!
                found = true;
                it.remove();
                willDisable.add(m);
            }
        }
        return found;
    }

    // dummy object to be placed in the problem set while recursive checking is in progress
    private static final Union2<Dependency,InvalidException> PROBING_IN_PROCESS = Union2.createSecond(new InvalidException("PROBING_IN_PROCESS"));
    // Access from Module.getProblems, q.v.
    // The probed module must not be currently enabled or fixed.
    Set<Union2<Dependency,InvalidException>> missingDependencies(Module probed) {
        return missingDependencies(probed, true);
    }
    private Set<Union2<Dependency,InvalidException>> missingDependencies(Module probed, boolean withNeeds) {
        // We need to synchronize here because though this method may be called
        // only within a read mutex, it can write to moduleProblems. Other places
        // where moduleProblems are used are write-mutex only and so do not have
        // to worry about contention.
        synchronized (moduleProblemsWithNeeds) {
            Map<Module,Set<Union2<Dependency,InvalidException>>> mP = (withNeeds ? moduleProblemsWithNeeds : moduleProblemsWithoutNeeds);
            Set<Union2<Dependency,InvalidException>> probs = mP.get(probed);
            if (probs == null) {
                probs = new HashSet<Union2<Dependency,InvalidException>>(8);
                if (withNeeds) {
                    probs.addAll(missingDependencies(probed, false));
                }
                probs.add(PROBING_IN_PROCESS);
                mP.put(probed, probs);
                for (Dependency dep : probed.getDependenciesArray()) {
                    if (dep.getType() == Dependency.TYPE_PACKAGE) {
                        // Can't check it in advance. Assume it is OK; if not
                        // a problem will be indicated during an actual installation
                        // attempt.
                    } else if (dep.getType() == Dependency.TYPE_MODULE) {
                        // Look for the corresponding module.
                        Object[] depParse = Util.parseCodeName(dep.getName());
                        String codeNameBase = (String)depParse[0];
                        int relVersionMin = (depParse[1] != null) ? ((Integer)depParse[1]).intValue() : -1;
                        int relVersionMax = (depParse[2] != null) ? ((Integer)depParse[2]).intValue() : relVersionMin;
                        Module other = get(codeNameBase);
                        if (other == null) {
                            // No such module, bad.
                            probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                            continue;
                        }
                        if (relVersionMin == relVersionMax) {
                            // Non-ranged dep.
                            if (relVersionMin != other.getCodeNameRelease()) {
                                // Wrong major version, bad.
                                probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                                continue;
                            }
                            if (dep.getComparison() == Dependency.COMPARE_IMPL &&
                                    ! Utilities.compareObjects(dep.getVersion(),
                                          other.getImplementationVersion())) { // NOI18N
                                // Wrong impl version, bad.
                                probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                                continue;
                            }
                            if (dep.getComparison() == Dependency.COMPARE_SPEC &&
                                    new SpecificationVersion(dep.getVersion()).compareTo(
                                        other.getSpecificationVersion()) > 0) {
                                // Spec version not high enough, bad.
                                probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                                continue;
                            }
                        } else if (relVersionMin < relVersionMax) {
                            // Ranged dep.
                            int otherRel = other.getCodeNameRelease();
                            if (otherRel < relVersionMin || otherRel > relVersionMax) {
                                // Major version outside of range, bad.
                                probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                                continue;
                            }
                            if (dep.getComparison() == Dependency.COMPARE_IMPL) {
                                throw new IllegalStateException("No such thing as ranged impl dep"); // NOI18N
                            }
                            if (dep.getComparison() == Dependency.COMPARE_SPEC &&
                                    // Spec comparisons only apply to the earliest major rel.
                                    otherRel == relVersionMin &&
                                    new SpecificationVersion(dep.getVersion()).compareTo(
                                        other.getSpecificationVersion()) > 0) {
                                // Spec version not high enough, bad.
                                probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                                continue;
                            }
                        } else {
                            throw new IllegalStateException("Upside-down rel vers range"); // NOI18N
                        }
                        if (! other.isEnabled()) {
                            // Need to make sure the other one is not missing anything either.
                            // Nor that it depends (directly on indirectly) on this one.
                            if ((!withNeeds && !missingDependencies(other, false).isEmpty()) ||
                                    (withNeeds && !isAlmostEmpty(missingDependencies(other, true)))) {
                                // This is a little subtle. Either the other module had real
                                // problems, in which case our dependency on it is not legit.
                                // Or, the other actually depends cyclically on this one. In
                                // that case, *it* would wind up calling missingDependencies
                                // on this module, but this module has already put a nonempty
                                // set in the mapping (containing at least the element
                                // PROBING_IN_PROCESS), causing the other module to fail and
                                // return a dependency on this module, causing this module to
                                // also fail with a dependency on that module. In the process,
                                // both modules get marked permanently bogus (unless you reload
                                // them both of course).
                                probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                                continue;
                            }
                            // If the other module is thought to be OK, assume we can depend
                            // on it if we need it.
                        }
                        // Already-installed modules are of course fine.
                    } else if (dep.getType() == Dependency.TYPE_REQUIRES || (withNeeds && dep.getType() == Dependency.TYPE_NEEDS)) {
                        // Works much like a regular module dependency. However it only
                        // fails if there are no satisfying modules with no problems.
                        String token = dep.getName();
                        Set<Module> providers = providersOf.get(token);
                        if (providers == null) {
                            // Nobody provides it. This dep failed.
                            probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                        } else {
                            // We have some possible providers. Check that at least one is good.
                            boolean foundOne = false;
                            for (Module other : providers) {
                                if (foundOne) {
                                    break;
                                }
                                if (other.isEnabled()) {
                                    foundOne = true;
                                } else {
                                    if ((!withNeeds && missingDependencies(other, false).isEmpty()) ||
                                            (withNeeds && isAlmostEmpty(missingDependencies(other, true)))) {
                                        // See comment above for regular module deps
                                        // re. use of PROBING_IN_PROCESS.
                                        foundOne = true;
                                    }
                                }
                            }
                            if (!foundOne) {
                                // Nobody can provide it, fail.
                                probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                            }
                        }
                    } else if (dep.getType() == Dependency.TYPE_JAVA) {
                        // Java dependency. Fixed for whole VM session, safe to check once and keep.
                        if (! Util.checkJavaDependency(dep)) {
                            // Bad.
                            probs.add(Union2.<Dependency,InvalidException>createFirst(dep));
                        }
                    }
                }
                probs.remove(PROBING_IN_PROCESS);
            }
            return probs;
        }
    }
    private static boolean isAlmostEmpty(Set<Union2<Dependency,InvalidException>> probs) {
        return probs.isEmpty() || probs.equals(Collections.singleton(PROBING_IN_PROCESS));
    }

    /** Forget about any possible "soft" problems there might have been.
     * Next time anyone asks, recompute them.
     * Currently enabled modules are left alone (no problems).
     * Otherwise, any problems which are "hard" (result from failed
     * Java/IDE/package dependencies, runtime errors, etc.) are left alone;
     * "soft" problems of inter-module dependencies are cleared
     * so they will be recomputed next time, and corresponding
     * changes are fired (since the next call to getProblem might
     * return a different result).
     */
    private void clearProblemCache() {
        clearProblemCache(moduleProblemsWithoutNeeds);
        clearProblemCache(moduleProblemsWithNeeds);
    }
    private void clearProblemCache(Map<Module,Set<Union2<Dependency,InvalidException>>> mP) {
        Iterator<Map.Entry<Module,Set<Union2<Dependency,InvalidException>>>> it = mP.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Module,Set<Union2<Dependency,InvalidException>>> entry = it.next();
            Module m = entry.getKey();
            if (! m.isEnabled()) {
                Set<Union2<Dependency,InvalidException>> s = entry.getValue();
                if (s != null) {
                    boolean clear = false;
                    for (Union2<Dependency,InvalidException> problem : s) {
                        if (problem.hasSecond()) {
                            // Hard problem, skip this one.
                            continue;
                        }
                        Dependency dep = problem.first();
                        if (dep.getType() != Dependency.TYPE_MODULE &&
                            dep.getType() != Dependency.TYPE_REQUIRES && 
                            dep.getType() != Dependency.TYPE_NEEDS &&
                            dep.getType() != Dependency.TYPE_RECOMMENDS 
                        ) {
                            // Also a hard problem.
                            continue;
                        }
                        // Some soft problems found, i.e. module deps. Clear them all.
                        // #76917: Even clear any hard problems.
                        clear = true;
                        break;
                    }
                    if (clear || s.isEmpty()) { // leave alone only if all hard problems
                        it.remove();
                        firer.change(new ChangeFirer.Change(m, Module.PROP_PROBLEMS, null, null));
                    }
                }
                // if we never computed anything, make no change now
            }
            // enabled modules are definitely OK, no change there
        }
    }

    /** Try to shut down the system.
     * First all modules are asked if they wish to close, in the proper order.
     * Assuming they say yes, then they are informed of the close.
     * Returns true if they all said yes.
     */
    public boolean shutDown() {
        return shutDown(null);
    }

    /**
     * Try to shut down the system.
     * First all modules are asked if they wish to close, in the proper order.
     * Assuming they say yes, a hook is run, then they are informed of the close.
     * If they did not agree to close, the hook is not run.
     * @param midHook a hook to run before closing modules if they agree to close
     * @return true if they all said yes and the module system is now shut down
     * @since org.netbeans.core/1 1.11
     */
    public boolean shutDown(Runnable midHook) {
        assertWritable();
        Set<Module> unorderedModules = getEnabledModules();
        Map<Module,List<Module>> deps = Util.moduleDependencies(unorderedModules, modulesByName, providersOf);
        List<Module> sortedModules;
        try {
            sortedModules = Utilities.topologicalSort(unorderedModules, deps);
        } catch (TopologicalSortException ex) {
            // Once again, weird situation.
            if (PRINT_TOPOLOGICAL_EXCEPTION_STACK_TRACES) {
                Util.err.log(Level.WARNING, null, ex);
            }
            Util.err.warning("Cyclic module dependencies, will not shut down cleanly: " + deps); // NOI18N
            return true;
        }
        if (! installer.closing(sortedModules)) {
            return false;
        }
        if (midHook != null) {
            try {
                midHook.run();
            } catch (RuntimeException e) {
                Util.err.log(Level.WARNING, null, e);
            } catch (LinkageError e) {
                Util.err.log(Level.WARNING, null, e);
            }
        }
        installer.close(sortedModules);
        return true;
    }
}
