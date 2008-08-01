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

package org.netbeans;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.*;
import org.openide.modules.*;

/** Static utility methods for use within this package.
 * @author Jesse Glick
 */
public abstract class Util {

    // Prevent accidental subclassing.
    private Util() {}

    /** Log everything happening in the module system. */
    public static final Logger err = Logger.getLogger("org.netbeans.core.modules"); // NOI18N

    /**
     * Make a temporary copy of a JAR file.
     */
    static File makeTempJar(File moduleFile) throws IOException {
        String prefix = moduleFile.getName();
        if (prefix.endsWith(".jar") || prefix.endsWith(".JAR")) { // NOI18N
            prefix = prefix.substring(0, prefix.length() - 4);
        }
        if (prefix.length() < 3) prefix += '.';
        if (prefix.length() < 3) prefix += '.';
        if (prefix.length() < 3) prefix += '.';
        String suffix = "-test.jar"; // NOI18N
        File physicalModuleFile = File.createTempFile(prefix, suffix);
        physicalModuleFile.deleteOnExit();
        InputStream is = new FileInputStream(moduleFile);
        try {
            OutputStream os = new FileOutputStream(physicalModuleFile);
            try {
                byte[] buf = new byte[4096];
                int i;
                while ((i = is.read(buf)) != -1) {
                    os.write(buf, 0, i);
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
        err.fine("Made " + physicalModuleFile);
        return physicalModuleFile;
    }

    /**
     * Find existing locale variants of f, in search order.
     */
    static List<File> findLocaleVariantsOf(File f) {
        List<FileWithSuffix> result = findLocaleVariantsWithSuffixesOf(f);
        List<File> l = new ArrayList<File>(result.size());
        for (FileWithSuffix fws : result) {
            l.add(fws.file);
        }
        return l;
    }
    static final class FileWithSuffix {
        public final File file;
        public final String suffix;
        FileWithSuffix(File file, String suffix) {
            this.file = file;
            this.suffix = suffix;
        }
    }
    /**
     * Find existing locale variants of f, in search order.
     */
    static List<FileWithSuffix> findLocaleVariantsWithSuffixesOf(File f) {
        if (! f.isFile()) {
            return Collections.emptyList();
        }
        String logicalDir = null;
        {
            // #34069: we have to consider that e.g. modules/locale/foo_branding.jar might be
            // located in a different root of ${netbeans.dirs}, so need to use IFL. Here the
            // logical path would be "modules/foo.jar" for the base module.
            String logicalPath = findLogicalPath(f);
            if (logicalPath != null) {
                int slash = logicalPath.lastIndexOf('/');
                if (slash != -1) {
                    logicalDir = logicalPath.substring(0, slash + 1) + "locale/"; // NOI18N
                } else {
                    logicalDir = "locale/"; // NOI18N
                }
            }
        }
        List<FileWithSuffix> l = new ArrayList<FileWithSuffix>(7);
        String nameExt = f.getName();
        int idx = nameExt.lastIndexOf('.'); // NOI18N
        String name, ext;
        if (idx != -1) {
            name = nameExt.substring(0, idx);
            ext = nameExt.substring(idx);
        } else {
            name = nameExt;
            ext = ""; // NOI18N
        }
        if (logicalDir != null) {
            for (String suffix : getLocalizingSuffixesFast()) {
                String path = logicalDir + name + suffix + ext;
                File v = InstalledFileLocator.getDefault().locate(path, null, false);
                if (v != null) {
                    l.add(new FileWithSuffix(v, suffix));
                }
            }
        } else {
            File dir = new File(f.getParentFile(), "locale"); // NOI18N
            if (dir.exists()) {
                for (String suffix : getLocalizingSuffixesFast()) {
                    File v = new File(dir, name + suffix + ext);
                    if (v.isFile()) {
                        l.add(new FileWithSuffix(v, suffix));
                    }
                }
            }
        }
        return l;
    }
    
    /** Similar to {@link NbBundle#getLocalizingSuffixes} but optimized. 
     * @since JST-PENDING: Called from InstalledFileLocatorImpl
     */
    public static synchronized String[] getLocalizingSuffixesFast() {
        if (suffixes == null ||
                Locale.getDefault() != lastLocale ||
                !Utilities.compareObjects(NbBundle.getBranding(), lastBranding)) {
            List<String> _suffixes = new ArrayList<String>();
            Iterator<String> it = NbBundle.getLocalizingSuffixes();
            while (it.hasNext()) {
                _suffixes.add(it.next());
            }
            suffixes = _suffixes.toArray(new String[_suffixes.size()]);
            lastLocale = Locale.getDefault();
            lastBranding = NbBundle.getBranding();
        }
        return suffixes;
    }
    private static String[] suffixes = null;
    private static Locale lastLocale = null;
    private static String lastBranding = null;
    
    /**
     * Find a path such that InstalledFileLocator.getDefault().locate(path, null, false)
     * yields the given file. Only guaranteed to work in case the logical path is a suffix of
     * the file's absolute path (after converting path separators); otherwise there is no
     * general way to invert locate(...) so this heuristic may fail. However for the IFL
     * implementation used in a plain NB installation (i.e.
     * org.netbeans.core.modules.InstalledFileLocatorImpl), this condition will in fact hold.
     * @return the inverse of locate(...), or null if there is no such path
     * @see "#34069"
     */
    private static String findLogicalPath(File f) {
        InstalledFileLocator l = InstalledFileLocator.getDefault();
        String path = f.getName();
        File parent = f.getParentFile();
        while (parent != null) {
            File probe = l.locate(path, null, false);
            //System.err.println("Util.fLP: f=" + f + " parent=" + parent + " probe=" + probe + " f.equals(probe)=" + f.equals(probe));
            if (f.equals(probe)) {
                return path;
            }
            path = parent.getName() + '/' + path;
            parent = parent.getParentFile();
        }
        return null;
    }
    
    // XXX ought to be some way to get localized messages for these...

    /** Check whether a simple dependency is met.
     * Only applicable to Java dependencies.
     */
    static boolean checkJavaDependency(Dependency dep) throws IllegalArgumentException {
        // Note that "any" comparison is not possible for this type.
        if (dep.getType() == Dependency.TYPE_JAVA) {
            if (dep.getName().equals(Dependency.JAVA_NAME)) {
                if (dep.getComparison() == Dependency.COMPARE_SPEC) {
                    return new SpecificationVersion(dep.getVersion()).compareTo(Dependency.JAVA_SPEC) <= 0;
                } else {
                    return dep.getVersion().equals(Dependency.JAVA_IMPL);
                }
            } else {
                if (dep.getComparison() == Dependency.COMPARE_SPEC) {
                    return new SpecificationVersion(dep.getVersion()).compareTo(Dependency.VM_SPEC) <= 0;
                } else {
                    return dep.getVersion().equals(Dependency.VM_IMPL);
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /** Check whether a package dependency is met.
     * A classloader must be supplied to check in.
     * @param dep a module dependency
     * @param cl a package-accessible classloader
     * @return true if a package dependency is met
     * @throws IllegalArgumentException 
     * @since 2.14
     */
    public static boolean checkPackageDependency(Dependency dep, ClassLoader cl) throws IllegalArgumentException {
        if (dep.getType() != Dependency.TYPE_PACKAGE) {
            throw new IllegalArgumentException("Not a package dependency"); // NOI18N
        }
        if (! (cl instanceof Util.PackageAccessibleClassLoader) && cl != Util.class.getClassLoader()) {
            throw new IllegalArgumentException("Not a package-accessible classloader: " + cl); // NOI18N
        }
        String name = dep.getName();
        String version = dep.getVersion();
        int comparison = dep.getComparison();
        String packageName, sampleName;
        int idx = name.indexOf('[');
        if (idx == -1) {
            packageName = name;
            sampleName = null;
        } else if (idx == 0) {
            packageName = null;
            sampleName = name.substring(1, name.length() - 1);
        } else {
            packageName = name.substring(0, idx);
            sampleName = name.substring(idx + 1, name.length() - 1);
            if (sampleName.indexOf('.') == -1) {
                // Unqualified class name; prefix it automatically.
                sampleName = packageName + '.' + sampleName;
            }
        }
        if (sampleName != null) {
            try {
                cl.loadClass(sampleName);
            } catch (ClassNotFoundException cnfe) {
                if (packageName == null) {
                    // This was all we were relying on, so it is an error.
                    err.log(Level.FINE, null, cnfe);
                    err.fine("Probed class could not be found");
                    return false;
                }
                // Else let the regular package check take care of it;
                // this was only run to enforce that the package defs were loaded.
            } catch (RuntimeException e) {
                // SecurityException, etc. Package exists but is corrupt.
                err.log(Level.WARNING, null, e);
                err.fine("Assuming package " + packageName + " is corrupt");
                return false;
            } catch (LinkageError le) {
                // NoClassDefFoundError, etc. Package exists but is corrupt.
                err.log(Level.WARNING, null, le);
                err.fine("Assuming package " + packageName + " is corrupt");
                return false;
            }
        }
        if (packageName != null) {
            Package pkg;
            if (cl instanceof Util.PackageAccessibleClassLoader) {
                pkg = ((Util.PackageAccessibleClassLoader)cl).getPackageAccessibly(packageName);
            } else {
                pkg = Package.getPackage(packageName);
            }
            if (pkg == null) {
                err.fine("No package with the name " + packageName + " found");
                return false;
            }
            if (comparison == Dependency.COMPARE_ANY) {
                return true;
            } else if (comparison == Dependency.COMPARE_SPEC) {
                if (pkg.getSpecificationVersion() == null) {
                    err.fine("Package " + packageName + " did not give a specification version");
                    return false;
                } else {
                    try {
                        SpecificationVersion versionSpec = new SpecificationVersion(version);
                        SpecificationVersion pkgSpec = new SpecificationVersion(pkg.getSpecificationVersion().trim());
                        if (versionSpec.compareTo(pkgSpec) <= 0) {
                            return true;
                        } else {
                            err.fine("Loaded package " + packageName + " was only of version " + pkgSpec + " but " + versionSpec + " was requested");
                            return false;
                        }
                    } catch (NumberFormatException nfe) {
                        err.log(Level.WARNING, null, nfe);
                        err.fine("Will not honor a dependency on non-numeric package spec version");
                        return false;
                    }
                }
            } else {
                // COMPARE_IMPL
                if (pkg.getImplementationVersion() == null) {
                    err.fine("Package " + packageName + " had no implementation version");
                    return false;
                } else if (! pkg.getImplementationVersion().trim().equals(version)) {
                    err.fine("Package " + packageName + " had the wrong impl version: " + pkg.getImplementationVersion());
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            // Satisfied sample class.
            return true;
        }
    }

    /**
     * Interface to permit a couple of methods in ClassLoader to be made public. 
     * @since 2.1
     */
    public interface PackageAccessibleClassLoader {
        /** @see ClassLoader#getPackage */
        Package getPackageAccessibly (String name);
        /** @see ClassLoader#getPackages */
        Package[] getPackagesAccessibly ();
    }

    /** 
     * Interface for a classloader to declare that it comes from a module. 
     * @since 2.1
     */
    public interface ModuleProvider {
        Module getModule();
    }
    
    /**
     * Enumerate (direct) interdependencies among a set of modules.
     * If used in a topological sort, the result will be a reverse-order
     * list of modules (suitable for disabling; reverse for enabling).
     * @param modules some modules
     * @param modulesByName map from module cnbs to modules (may contain unrelated modules)
     * @param providersOf map from tokens to sets of modules providing them (may mention unrelated modules)
     * @return a map from modules to lists of modules they depend on
     * @see Utilities#topologicalSort
     * JST-PENDING needed from tests
     */
    public static Map<Module,List<Module>> moduleDependencies(Collection<Module> modules, Map<String,Module> modulesByName, Map<String,Set<Module>> _providersOf) {
        Set<Module> modulesSet = (modules instanceof Set) ? (Set<Module>)modules : new HashSet<Module>(modules);
        Map<String,List<Module>> providersOf = new HashMap<String,List<Module>>(_providersOf.size() * 2 + 1);
        for (Map.Entry<String, Set<Module>> entry: _providersOf.entrySet()) {
            Set<Module> providers = entry.getValue();
            if (providers != null) {
                List<Module> availableProviders = new LinkedList<Module>(providers);
                availableProviders.retainAll(modulesSet);
                if (!availableProviders.isEmpty()) {
                    providersOf.put(entry.getKey(), availableProviders);
                }
            }
        }
        Map<Module,List<Module>> m = new HashMap<Module,List<Module>>();
	for (Module m1: modules) {
            List<Module> l = null;
            for (Dependency dep : m1.getDependenciesArray()) {
                if (dep.getType() == Dependency.TYPE_REQUIRES) {
                    List<Module> providers = providersOf.get(dep.getName());

                    if (providers != null) {
                        if (l == null) {
                            l = new LinkedList<Module>();
                        }
                        l.addAll(providers);
                    }
                }
                else if (dep.getType() == Dependency.TYPE_MODULE) {
                    String cnb = (String) parseCodeName(dep.getName())[0];
                    Module m2 = modulesByName.get(cnb);

                    if (m2 != null && modulesSet.contains(m2)) {
                        if (l == null) {
                            l = new LinkedList<Module>();
                        }
                        l.add(m2);
                    }
                }
            }
            if (l != null) {
                m.put(m1, l);
            }
        }
        return m;
    }
    
    /**
     * Get dependencies forward or backwards starting from one module.
     * @see #moduleDependencies
     * @see ModuleManager#getModuleInterdependencies
     */
    static Set<Module> moduleInterdependencies(Module m, boolean reverse, boolean transitive,
                                       Set<Module> modules, Map<String,Module> modulesByName, Map<String,Set<Module>> providersOf) {
        // XXX these algorithms could surely be made faster using standard techniques
        // for now the speed is not critical however
        if (reverse) {
            Set<Module> s = new HashSet<Module>();
            for (Module m2: modules) {
                if (m2 == m) {
                    continue;
                }
                if (moduleInterdependencies(m2, false, transitive, modules, modulesByName, providersOf).contains(m)) {
                    s.add(m2);
                }
            }
            return s;
        } else {
            Set<Module> s = new HashSet<Module>();
            for (Dependency dep : m.getDependenciesArray()) {
                if (dep.getType() == Dependency.TYPE_REQUIRES || dep.getType() == Dependency.TYPE_NEEDS) {
                    Set<Module> providers = providersOf.get(dep.getName());
                    if (providers != null) {
                        s.addAll(providers);
                    }
                } else if (dep.getType() == Dependency.TYPE_MODULE) {
                    String cnb = (String)parseCodeName(dep.getName())[0];
                    Module m2 = modulesByName.get(cnb);
                    if (m2 != null) {
                        s.add(m2);
                    }
                }
            }
            s.remove(m);
            if (transitive) {
                Set<Module> toAdd;
                do {
                    toAdd = new HashSet<Module>();
                    for (Module m2: s) {
                        Set<Module> s2 = moduleInterdependencies(m2, false, false, modules, modulesByName, providersOf);
                        s2.remove(m);
                        s2.removeAll(s);
                        toAdd.addAll(s2);
                    }
                    s.addAll(toAdd);
                } while (!toAdd.isEmpty());
            }
            return s;
        }
    }
    
    /** Get a filter for JAR files. */
    static FilenameFilter jarFilter() {
        return new JarFilter();
    }
    private static final class JarFilter implements FilenameFilter {
        JarFilter() {}
        public boolean accept(File dir, String name) {
            String n = name.toLowerCase(Locale.US);
            return n.endsWith(".jar"); // NOI18N
        }
    }
    
    /** Convert a class file name to a resource name suitable for Beans.instantiate.
    * @param name resource name of class file
    * @return class name without the <code>.class</code>/<code>.ser</code> extension, and using dots as package separator
    * @throws IllegalArgumentException if the name did not have a valid extension, or originally contained dots outside the extension, etc.
     * @since JST-PENDING: used from NbInstaller
    */
    public static String createPackageName(String name) throws IllegalArgumentException {
        String clExt = ".class"; // NOI18N
        if (!name.endsWith(clExt)) {
            // try different extension
            clExt = ".ser"; // NOI18N
        }
        if (name.endsWith(clExt)) {
            String bareName = name.substring(0, name.length() - clExt.length());
            if (bareName.length() == 0) { // ".class" // NOI18N
                throw new IllegalArgumentException("Bad class file name: " + name); // NOI18N
            }
            if (bareName.charAt(0) == '/') { // "/foo/bar.class" // NOI18N
                throw new IllegalArgumentException("Bad class file name: " + name); // NOI18N
            }
            if (bareName.charAt(bareName.length() - 1) == '/') { // "foo/bar/.class" // NOI18N
                throw new IllegalArgumentException("Bad class file name: " + name); // NOI18N
            }
            if (bareName.indexOf('.') != -1) { // "foo.bar.class" // NOI18N
                throw new IllegalArgumentException("Bad class file name: " + name); // NOI18N
            }
            return bareName.replace('/', '.'); // NOI18N
        } else { // "foo/bar" or "foo.bar" // NOI18N
            throw new IllegalArgumentException("Bad class file name: " + name); // NOI18N
        }
    }

    /** A lookup implementation specialized for modules.
     * Its primary advantage over e.g. AbstractLookup is that
     * it is possible to add modules to the set at one time and
     * fire changes in the set of modules later on. ModuleManager
     * uses this to add modules immediately in create() and destroy(),
     * but only fire lookup events later and asynchronously, from the
     * read mutex.
     */
    static final class ModuleLookup extends Lookup {
        ModuleLookup() {}
        private final Set<Module> modules = new HashSet<Module>(100);
        private final Set<ModuleResult> results = new WeakSet<ModuleResult>(10);
        /** Add a module to the set. */
        public void add(Module m) {
            synchronized (modules) {
                modules.add(m);
            }
        }
        /** Remove a module from the set. */
        public void remove(Module m) {
            synchronized (modules) {
                modules.remove(m);
            }
        }
        /** Fire changes to all result listeners. */
        public void changed() {
            synchronized (results) {
                Iterator it = results.iterator();
                while (it.hasNext()) {
                    ((ModuleResult)it.next()).changed();
                }
            }
        }
        public <T> T lookup(Class<T> clazz) {
            if ((clazz == Module.class || clazz == ModuleInfo.class || clazz == Object.class || clazz == null)
                    && ! modules.isEmpty()) {
                synchronized (modules) {
                    return clazz.cast(modules.iterator().next());
                }
            } else {
                return null;
            }
        }
	@SuppressWarnings("unchecked")
        public <T> Lookup.Result<T> lookup(Lookup.Template<T> t) {
            Class<T> clazz = t.getType();
            if (clazz == Module.class || clazz == ModuleInfo.class ||
                clazz == Object.class || clazz == null) {
                return (Lookup.Result<T>)(Object) new ModuleResult((Lookup.Template<Module>) t);
            }
            else {
                return Lookup.EMPTY.lookup(t);
            }
        }
        public @Override String toString() {
            synchronized (modules) {
                return "ModuleLookup" + modules; // NOI18N
            }
        }
        private final class ModuleResult extends Lookup.Result<Module> {
            private final Lookup.Template<? super Module> t;
            private final Set<LookupListener> listeners = new HashSet<LookupListener>(10);
            public ModuleResult(Lookup.Template<? super Module> t) {
                this.t = t;
                synchronized (results) {
                    results.add(this);
                }
            }
            public void addLookupListener(LookupListener l) {
                synchronized (listeners) {
                    listeners.add(l);
                }
            }
            public void removeLookupListener(LookupListener l) {
                synchronized (listeners) {
                    listeners.remove(l);
                }
            }
            public void changed() {
                LookupListener[] _listeners;
                synchronized (listeners) {
                    if (listeners.isEmpty()) {
                        return;
                    }
                    _listeners = listeners.toArray(new LookupListener[listeners.size()]);
                }
                LookupEvent ev = new LookupEvent(this);
                for (int i = 0; i < _listeners.length; i++) {
                    _listeners[i].resultChanged(ev);
                }
            }
            public Collection<Module> allInstances() {
                synchronized (modules) {
                    String id = t.getId();
                    Object inst = t.getInstance();
                    if (id != null) {
                        Iterator<Module> it = modules.iterator();
                        while (it.hasNext()) {
                            Module m = it.next();
                            if (id.equals(ModuleItem.PREFIX + m.getCodeNameBase())) {
                                if (inst == null || inst == m) {
                                    return Collections.<Module>singleton(m);
                                }
                            }
                        }
                        return Collections.<Module>emptySet();
                    } else if (inst != null) {
                        return modules.contains(inst) ? Collections.<Module>singleton(Module.class.cast(inst)) : Collections.<Module>emptySet();
                    } else {
                        // Regular lookup based on type.
                        return new HashSet<Module>(modules);
                    }
                }
            }
            public @Override Set<Class<? extends Module>> allClasses() {
                return Collections.<Class<? extends Module>>singleton(Module.class);
            }
            public @Override Collection<? extends Lookup.Item<Module>> allItems() {
                Collection<Module> insts = allInstances();
                ArrayList<ModuleItem> list = new ArrayList<ModuleItem>(Math.max(1, insts.size()));
                for (Module m: insts) {
                    list.add(new ModuleItem(m));
                }
                return list;
            }
            public @Override String toString() {
                return "ModuleResult:" + t; // NOI18N
            }
        }
        private static final class ModuleItem extends Lookup.Item<Module> {
            public static final String PREFIX = "Module["; // NOI18N
            private final Module item;
            public ModuleItem(Module item) {
                this.item = item;
            }
            public Module getInstance() {
                return item;
            }
            public Class<? extends Module> getType() {
                return Module.class;
            }
            public String getId() {
                return PREFIX + item.getCodeNameBase();
            }
            public String getDisplayName() {
                return item.getDisplayName();
            }
        }
    }
    
    // OK to not release this memory; module deletion is rare: holds 45kB for 173 modules (June 2005)
    private static final Map<String,Object[]> codeNameParseCache = new HashMap<String,Object[]>(200); // Map<String,[String,int]>
    /** Find the code name base and major release version from a code name.
     * Caches these parses. Thread-safe (i.e. OK from read mutex).
     * @return an array consisting of the code name base (String) followed by the release version (Integer or null)
     *         followed by another end-range version (Integer or null)
     * @throws NumberFormatException if the release version is mangled
     * @since JST-PENDING: used from NbInstaller
     */
    public static Object[] parseCodeName(String cn) throws NumberFormatException {
        synchronized (codeNameParseCache) {
            Object[] r = codeNameParseCache.get(cn);
            if (r == null) {
                r = new Object[3];
                int i = cn.lastIndexOf('/');
                if (i == -1) {
                    r[0] = cn;
                } else {
                    r[0] = cn.substring(0, i).intern();
                    String end = cn.substring(i + 1);
                    int j = end.indexOf('-');
                    if (j == -1) {
                        r[1] = new Integer(end);
                    } else {
                        r[1] = new Integer(end.substring(0, j));
                        r[2] = new Integer(end.substring(j + 1));
                    }
                }
                codeNameParseCache.put(cn.intern(), r);
            }
            return r;
        }
    }

    /** Get API module dependency, if any, for a module.
     * @param dependencies module dependencies
     * @param cnb code name base of API module
     * @return a fake spec version (0.x.y if x.y w/ no major release, else r.x.y); or null if no dep
     * @since JST-PENDING: used from NbInstaller
     */
    public static SpecificationVersion getModuleDep(Set<Dependency> dependencies, String cnb) {
        for (Dependency d : dependencies) {
            if (d.getType() == Dependency.TYPE_MODULE &&
                    d.getComparison() == Dependency.COMPARE_SPEC) {
                try {
                    Object[] p = parseCodeName(d.getName());
                    if (!p[0].equals(cnb)) {
                        continue;
                    }
                    int rel = ((Integer)p[1]).intValue(); // ignore any end range, consider only start
                    if (rel == -1) rel = 0; // XXX will this lead to incorrect semantics?
                    return new SpecificationVersion("" + rel + "." + d.getVersion()); // NOI18N
                } catch (NumberFormatException nfe) {
                    Util.err.log(Level.WARNING, null, nfe);
                    return null;
                }
            }
        }
        return null;
    }
    
    /**
     * Transitively fill out a set of modules with all of its module dependencies.
     * Dependencies on missing modules are silently ignored, but dependencies
     * on present but uninstallable (problematic) modules are included.
     * @param mgr the manager
     * @param modules a mutable set of modules
     * @since JST-PENDING: used from NbInstaller
     */
    public static void transitiveClosureModuleDependencies(ModuleManager mgr, Set<Module> modules) {
        Set<Module> nue = null; // Set of newly appended modules
        while (nue == null || !nue.isEmpty()) {
            nue = new HashSet<Module>();
            for (Module m: modules) {
                for (Dependency dep : m.getDependenciesArray()) {
                    if (dep.getType() != Dependency.TYPE_MODULE) {
                        continue;
                    }
                    Module other = mgr.get((String)parseCodeName(dep.getName())[0]);
                    if (other != null && !modules.contains(other)) {
                        nue.add(other);
                    }
                }
            }
            modules.addAll(nue);
        }
    }
    
}
