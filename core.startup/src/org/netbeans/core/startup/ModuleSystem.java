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

package org.netbeans.core.startup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.DuplicateException;
import org.netbeans.Events;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/** Controller of the IDE's whole module system.
 * Contains higher-level convenience methods to
 * access the basic functionality and procedural
 * stages of the module system.
 * The NbTopManager should hold a reference to one instance.
 * Methods are thread-safe.
 * @author Jesse Glick
 */
public final class ModuleSystem {
    private static final Logger LOG = Logger.getLogger(ModuleSystem.class.getName());
    private final ModuleManager mgr;
    private final NbInstaller installer;
    private final ModuleList list;
    private final Events ev;
    
    /** Initialize module system.
     * The system file system is needed as that holds the Modules/ folder.
     * Note if the systemFileSystem is read-only, no module list will be created,
     * so it is forbidden to call readList, scanForNewAndRestore, or installNew.
     */
    public ModuleSystem(FileSystem systemFileSystem) throws IOException {
        if (Boolean.getBoolean("org.netbeans.core.startup.ModuleSystem.CULPRIT")) Thread.dumpStack(); // NOI18N
        ev = Boolean.getBoolean("netbeans.modules.quiet") ? (Events)new QuietEvents() : new NbEvents();
        installer = new NbInstaller(ev);
        mgr = new ModuleManager(installer, ev);
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (ModuleManager.PROP_CLASS_LOADER.equals(ev.getPropertyName())) {
                    org.netbeans.core.startup.MainLookup.systemClassLoaderChanged(mgr.getClassLoader());
                }
            }
        };
        mgr.addPropertyChangeListener(l);
        
        // now initialize to core/* classloader, later we reassign to all modules
        org.netbeans.core.startup.MainLookup.systemClassLoaderChanged(installer.getClass ().getClassLoader ());
        // #28465: initialize module lookup early
        org.netbeans.core.startup.MainLookup.moduleLookupReady(mgr.getModuleLookup());
        if (systemFileSystem.isReadOnly()) {
            list = null;
        } else {
            FileObject root = systemFileSystem.getRoot();
            FileObject modulesFolder = root.getFileObject("Modules"); // NOI18N
            if (modulesFolder == null) {
                modulesFolder = root.createFolder("Modules"); // NOI18N
            }
            list = new ModuleList(mgr, modulesFolder, ev);
            installer.registerList(list);
            installer.registerManager(mgr);
        }
        ev.log(Events.CREATED_MODULE_SYSTEM);
    }
    
    /** Get the raw module manager.
     * Useful for pieces of the UI needing to directly affect the set of installed modules.
     * For example, the Modules node in the Options window may use this.
     */
    public ModuleManager getManager() {
        return mgr;
    }
    
    /** Get the event-logging handler.
     */
    public Events getEvents() {
        return ev;
    }
    
    /** Produce a list of JAR files including all installed modules,
     * their extensions, and enabled locale variants of both.
     * Will be returned in a classpath-like order.
     * Intended for use by the execution engine (though sort of deprecated).
     * @return list of module-related JARs/ZIPs
     */
    public List<File> getModuleJars () {
        mgr.mutexPrivileged().enterReadAccess();
        try {
            List<File> l = new ArrayList<File>();
            for (Module m: mgr.getEnabledModules()) {
                l.addAll(m.getAllJars());
            }
            return l;
        } finally {
            mgr.mutexPrivileged().exitReadAccess();
        }
    }

    /** We just make the modules now, restore them later
     * to optimize the layer merge.
     */
    private Set<Module> bootModules = null;
    
    /** Load modules found in the classpath.
     * Note that they might not satisfy all their dependencies, in which
     * case oh well...
     */
    public void loadBootModules() {
        // Keep a list of manifest URL bases which we know we do not need to
        // parse. Some of these manifests might be signed, and if so, we do not
        // want to touch them, as it slows down startup quite a bit.
        Set<File> ignoredJars = new HashSet<File>();
        String javaHome = System.getProperty("java.home"); // NOI18N
        if (javaHome != null) {
            File lib = new File(new File(javaHome).getParentFile(), "lib"); // NOI18N
            ignoredJars.add(new File(lib, "tools.jar")); // NOI18N
            ignoredJars.add(new File(lib, "dt.jar")); // NOI18N
        }
        for (String entry : System.getProperty("sun.boot.class.path", "").split(File.pathSeparator)) { // NOI18N
            ignoredJars.add(new File(entry));
        }
        LOG.log(Level.FINE, "Ignored JARs: {0}", ignoredJars);
            
        mgr.mutexPrivileged().enterWriteAccess();
        ev.log(Events.START_LOAD_BOOT_MODULES);
        try {
            bootModules = new HashSet<Module>(10);
            ClassLoader loader = ModuleSystem.class.getClassLoader();
            Enumeration<URL> e = loader.getResources("META-INF/MANIFEST.MF"); // NOI18N
            ev.log(Events.PERF_TICK, "got all manifests"); // NOI18N
            
            // There will be duplicates: cf. #32576.
            Set<URL> checkedManifests = new HashSet<URL>();
            MANIFESTS:
            while (e.hasMoreElements()) {
                URL manifestUrl = e.nextElement();
                if (!checkedManifests.add(manifestUrl)) {
                    // Already seen, ignore.
                    continue;
                }
                URL jarURL = FileUtil.getArchiveFile(manifestUrl);
                if (jarURL != null && jarURL.getProtocol().equals("file") &&
                        /* #121777 */ jarURL.getPath().startsWith("/")) {
                    LOG.log(Level.FINE, "Considering JAR: {0}", jarURL);
                try {
                        if (ignoredJars.contains(new File(jarURL.toURI()))) {
                        LOG.log(Level.FINE, "ignoring JDK/JRE manifest: {0}", manifestUrl);
                        continue MANIFESTS;
                    }
                } catch (URISyntaxException x) {
                    Exceptions.printStackTrace(x);
                }
                }
                LOG.log(Level.FINE, "Checking boot manifest: {0}", manifestUrl);
                
                InputStream is;
                try {
                    is = manifestUrl.openStream();
                } catch (IOException ioe) {
                    // Debugging for e.g. #32493 - which JAR was guilty?
                    Exceptions.attachMessage(ioe, "URL: " + manifestUrl); // NOI18N
                    throw ioe;
                }
                try {
                    Manifest mani = new Manifest(is);
                    Attributes attr = mani.getMainAttributes();
                    if (attr.getValue("OpenIDE-Module") == null) { // NOI18N
                        // Not a module.
                        continue;
                    }
                    bootModules.add(mgr.createFixed(mani, manifestUrl, loader));
                } finally {
                    is.close();
                }
            }
            if (list == null) {
                // Plain calling us, we have to install now.
                // Do it the simple way.
                mgr.enable(bootModules);
            }
            ev.log(Events.PERF_TICK, "added all classpath modules"); // NOI18N
	    
        } catch (IOException ioe) {
            // Note: includes also InvalidException's for malformed this and that.
            // Probably if a bootstrap module is corrupt we are in pretty bad shape
            // anyway, so don't bother trying to be fancy and install just some of
            // them etc.
            LOG.log(Level.WARNING, null, ioe);
        } catch (DuplicateException de) {
            LOG.log(Level.WARNING, null, de);
        } finally {
            // Not 100% accurate in this case:
            ev.log(Events.FINISH_LOAD_BOOT_MODULES);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Read disk settings and determine what the known modules are.
     */
    public void readList() {
        ev.log(Events.PERF_START, "ModuleSystem.readList"); // NOI18N
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            list.readInitial();
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
	ev.log(Events.PERF_END, "ModuleSystem.readList"); // NOI18N
    }
    
    /** Install read modules.
     */
    public void restore() {
	ev.log(Events.PERF_START, "ModuleSystem.restore"); // NOI18N
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Set<Module> toTrigger = new HashSet<Module>(bootModules/*Collections.EMPTY_SET*/);
            list.trigger(toTrigger);
            mgr.releaseModuleManifests();
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
	ev.log(Events.PERF_END, "ModuleSystem.restore"); // NOI18N	
    }
    
    /** Shut down the system: ask modules to shut down.
     * Some of them may refuse.
     */
    public boolean shutDown(Runnable midHook) {
        mgr.mutexPrivileged().enterWriteAccess();
        boolean res;
        try {
            res = mgr.shutDown(midHook);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        Stamps.getModulesJARs().shutdown();
        return res;
    }
    
    /** Load a module in test (reloadable) mode.
     * If there is an existing module with a different JAR, get
     * rid of it and load this one instead.
     * If it is already installed, disable it and reenable it
     * to reload its contents.
     * If other modules depend on it, disable them first and
     * then (try to) enable them again later.
     */
    final void deployTestModule(File jar) throws IOException {
        if (! jar.isAbsolute()) throw new IOException("Absolute paths only please"); // NOI18N
        mgr.mutexPrivileged().enterWriteAccess();
        ev.log(Events.START_DEPLOY_TEST_MODULE, jar);
        // For now, just print to stderr directly; could also go thru Events.
        // No need for I18N, module developers are expected to know English
        // well enough.
        System.err.println("Deploying test module " + jar + "..."); // NOI18N
        try {
            // The test module:
            Module tm = null;
            // Anything that needs to be turned back on later:
            Set<Module> toReenable = new HashSet<Module>();
            // First see if this refers to an existing module.
            // (If so, make sure it is reloadable.)
            for (Module m : mgr.getModules()) {
                if (m.getJarFile() != null) {
                    if (jar.equals(m.getJarFile())) {
                        // Hah, found it.
                        if (! m.isReloadable()) {
                            m.setReloadable(true);
                        }
                        turnOffModule(m, toReenable);
                        mgr.reload(m);
                        tm = m;
                        break;
                    }
                }
            }
            if (tm == null) {
                // This JAR not encountered before. Try to load it. If it is
                // a duplicate of an existing module in a different location,
                // kill the existing one and replace it with this one.
                try {
                    tm = mgr.create(jar, new ModuleHistory(jar.getAbsolutePath()), true, false, false);
                } catch (DuplicateException dupe) {
                    Module old = dupe.getOldModule();
                    System.err.println("Replacing old module in " + old); // NOI18N
                    turnOffModule(old, toReenable);
                    mgr.delete(old);
                    try {
                        tm = mgr.create(jar, new ModuleHistory(jar.getAbsolutePath()), true, false, false);
                    } catch (DuplicateException dupe2) {
                        // Should not happen.
                        throw (IOException) new IOException(dupe2.toString()).initCause(dupe2);
                    }
                }
            }
            // Try to turn on the test module. It might throw InvalidExc < IOExc.
            System.err.println("Enabling " + tm + "..."); // NOI18N
            if (!mgr.simulateEnable(Collections.singleton(tm)).contains(tm)) {
                throw new IOException("Cannot enable " + tm + "; problems: " + tm.getProblems());
            }
            mgr.enable(tm);
            // OK, so far so good; also try to turn on any other modules if
            // we can that were on before. Just try to turn them all on.
            // Don't get fancy; if some of them could not be turned on, the
            // developer will be told and can clean up the situation as needed.
            // Also any of them marked as reloadable, reload them now.
            if (! toReenable.isEmpty()) {
                System.err.println("Also re-enabling:"); // NOI18N
                for (Module m : toReenable) {
                    System.err.println("\t" + m.getDisplayName()); // NOI18N
                    if (m.isReloadable()) {
                        m.reload();
                    }
                }
                try {
                    mgr.enable(toReenable);
                } catch (IllegalArgumentException iae) {
                    // Strange new dependencies, etc.
                    throw new IOException(iae.toString());
                }
            }
            System.err.println("Done."); // NOI18N
        } finally {
            ev.log(Events.FINISH_DEPLOY_TEST_MODULE, jar);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    /** Make sure some module is disabled.
     * If there were any other non-autoload modules enabled
     * which depended on it, make note of them.
     */
    private void turnOffModule(Module m, Set<Module> toReenable) {
        if (! m.isEnabled()) {
            // Already done.
            return;
        }
        for (Module m2: mgr.simulateDisable(Collections.<Module>singleton(m))) {
            if (!m2.isAutoload() && !m2.isEager()) {
                toReenable.add(m2);
            }
        }
        try {
            System.err.println("Disabling " + m + "..."); // NOI18N
            // Don't mention the others, they will be mentioned later anyway.
            mgr.disable(toReenable);
        } finally {
            toReenable.remove(m);
        }
    }
    
    /** Get the effective "classpath" used by a module.
     * <p>This is a somewhat stretched notion, but should give something that looks
     * as much like a classpath as possible, i.e. a list of directories or JARs
     * separated by the standard separator, which roughly represents what resources
     * are visible to the module's classloader. May use special syntax to represent
     * situations in which only certain packages are available from a particular
     * "classpath" entry.
     * <p>Disabled modules have no classpath (empty string).
     * <p>Call within a mutex.
     * @param m the module to build a classpath for
     * @return an approximation of that module's classpath
     * @see "#22466"
     * @since org.netbeans.core/1 > 1.5
     */
    public String getEffectiveClasspath(Module m) {
        return installer.getEffectiveClasspath(m);
    }
    
    /** Dummy event handler that does not print anything.
     * Useful for test scripts where you do not really want to see
     * everything going by.
     */
    private static final class QuietEvents extends Events {
        QuietEvents() {}
        protected void logged(String message, Object[] args) {}
    }

}
