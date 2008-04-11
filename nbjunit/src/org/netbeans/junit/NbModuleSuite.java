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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * Wraps a test class with proper NetBeans Runtime Container environment.
 * This allows to execute tests in a very similar environment to the 
 * actual invocation in the NetBeans IDE. To use write your test as
 * you are used to and add suite static method:
 * <pre>
 * public class YourTest extends NbTestCase {
 *   public YourTest(String s) { super(s); }
 * 
 *   public static Test suite() {
 *     return NbModuleSuite.create(YourTest.class);
 *   }
 * 
 *   public void testXYZ() { ... }
 *   public void testABC() { ... }
 * }
 * </pre>
 *
 * @since 1.46
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuite {

    private NbModuleSuite() {}

    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. 
     * Wraps the provided class into a test that set ups properly the
     * testing environment. The set of enabled modules is going to be
     * determined from the actual classpath of a module, which is common
     * when in all NetBeans tests. All other modules are kept disabled.
     * In addition,it allows one limit the clusters that shall be made available.
     * For example <code>ide.*|java.*</code> will start the container just
     * with platform, ide and java clusters.
     * 
     * 
     * @param clazz the class with bunch of testXYZ methods
     * @param clustersRegExp regexp to apply to name of cluster to find out if it is supposed to be included
     *    in the runtime container setup or not
     * @param moduleRegExp by default all modules on classpath are turned on,
     *    however this regular expression can specify additional ones. If not
     *    null, the specified cluster will be searched for modules with such
     *    codenamebase and those will be turned on
     * @return runtime container ready test
     */
    public static Test create(Class<? extends Test> clazz, String clustersRegExp, String moduleRegExp) {
        return new S(clazz, clustersRegExp, moduleRegExp);
    }

    static final class S extends NbTestSuite {
        private final Class<?> clazz;
        private final String clusterRegExp;
        private final String moduleRegExp;

        public S(Class<?> aClass, String clusterRegExp, String moduleRegExp) {
            super();
            this.clazz = aClass;
            this.clusterRegExp = clusterRegExp;
            this.moduleRegExp = moduleRegExp;
        }

        @Override
        public void run(TestResult result) {
            try {
                runInRuntimeContainer(result);
            } catch (Exception ex) {
                result.addError(this, ex);
            }
        }

        private void runInRuntimeContainer(TestResult result) throws Exception {
            File platform = findPlatform();
            File[] boot = new File(platform, "lib").listFiles();
            List<URL> bootCP = new ArrayList<URL>();
            for (int i = 0; i < boot.length; i++) {
                URL u = boot[i].toURL();
                if (u.toExternalForm().endsWith(".jar")) {
                    bootCP.add(u);
                }
            }
            
            File tools = new File(new File(new File(System.getProperty("java.home")).getParentFile(), "lib"), "tools.jar");
            Assert.assertTrue(tools.exists());
            bootCP.add(tools.toURL());
            
            // loader that does not see our current classloader
            ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
            JUnitLoader junit = new JUnitLoader(parent, NbModuleSuite.class.getClassLoader());
            URLClassLoader loader = new URLClassLoader(bootCP.toArray(new URL[0]), junit);
            Class<?> main = loader.loadClass("org.netbeans.Main"); // NOI18N
            Assert.assertEquals("Loaded by our classloader", loader, main.getClassLoader());
            Method m = main.getDeclaredMethod("main", String[].class); // NOI18N

            System.setProperty("java.util.logging.config", "-");
            System.setProperty("netbeans.logger.console", "true");
            System.setProperty("netbeans.home", platform.getPath());

            File ud = new File(new File(Manager.getWorkDirPath()), "userdir");
            ud.mkdirs();
            NbTestCase.deleteSubFiles(ud);

            System.setProperty("netbeans.user", ud.getPath());

            TreeSet<String> modules = new TreeSet<String>();
            modules.addAll(findEnabledModules(NbTestSuite.class.getClassLoader()));
            modules.add("org.openide.filesystems");
            modules.add("org.openide.modules");
            modules.add("org.openide.util");
            modules.remove("org.netbeans.insane");
            modules.add("org.netbeans.core.startup");
            modules.add("org.netbeans.bootstrap");
            turnModules(ud, modules, moduleRegExp, platform);

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (File f : findClusters()) {
                turnModules(ud, modules, moduleRegExp, f);
                sb.append(sep);
                sb.append(f.getPath());
                sep = File.pathSeparator;
            }
            System.setProperty("netbeans.dirs", sb.toString());

            System.setProperty("netbeans.security.nocheck", "true");
            
            List<String> args = new ArrayList<String>();
            args.add("--nosplash");
            m.invoke(null, (Object)args.toArray(new String[0]));

            ClassLoader global = Thread.currentThread().getContextClassLoader();
            Assert.assertNotNull("Global classloader is initialized", global);

            URL[] testCP = preparePath(clazz);
            ClassLoader testLoader = new URLClassLoader(testCP, global);
            try {
                testLoader.loadClass("junit.framework.Test");
                testLoader.loadClass("org.netbeans.junit.NbTestSuite");
                testLoader.loadClass("org.netbeans.jellytools.JellyTestCase");
                @SuppressWarnings("unchecked")
                Class<? extends TestCase> sndClazz = (Class<? extends TestCase>) testLoader.loadClass(clazz.getName());
                new NbTestSuite(sndClazz).run(result);
            } catch (ClassNotFoundException ex) {
                result.addError(this, ex);
            } catch (NoClassDefFoundError ex) {
                result.addError(this, ex);
            }
            
            Class<?> lifeClazz = global.loadClass("org.openide.LifecycleManager"); // NOI18N
            Method getDefault = lifeClazz.getMethod("getDefault"); // NOI18N
            Method exit = lifeClazz.getMethod("exit");
            Object life = getDefault.invoke(null);
            if (!life.getClass().getName().startsWith("org.openide.LifecycleManager")) { // NOI18N
                System.setProperty("netbeans.close.no.exit", "true"); // NOI18N
                exit.invoke(life);
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                    }
                });
            }
        }

        private URL[] preparePath(Class<?>... classes) {
            Collection<URL> cp = new LinkedHashSet<URL>();
            for (Class c : classes) {
                URL test = c.getProtectionDomain().getCodeSource().getLocation();
                Assert.assertNotNull("URL found for " + c, test);
                cp.add(test);
            }
            return cp.toArray(new URL[0]);
        }


        private File findPlatform() {
            try {
                Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
                File util = new File(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
                Assert.assertTrue("Util exists: " + util, util.exists());

                return util.getParentFile().getParentFile();
            } catch (Exception ex) {
                Assert.fail("Cannot find utilities JAR");
                return null;
            }
        }

        private File[] findClusters() {
            if (clusterRegExp == null) {
                return new File[0];
            }
            
            List<File> clusters = new ArrayList<File>();
            File plat = findPlatform();

            for (File f : plat.getParentFile().listFiles()) {
                if (f.equals(plat)) {
                    continue;
                }
                if (!f.getName().matches(clusterRegExp)) {
                    continue;
                }
                File m = new File(new File(f, "config"), "Modules");
                if (m.exists()) {
                    clusters.add(f);
                }
            }
            return clusters.toArray(new File[0]);
        }

        private static Pattern CODENAME = Pattern.compile("OpenIDE-Module: *([^/$ \n\r]*)[/]?[0-9]*", Pattern.MULTILINE);
        /** Looks for all modules on classpath of given loader and builds 
         * their list from them.
         */
        static Set<String> findEnabledModules(ClassLoader loader) throws IOException {
            Set<String> cnbs = new TreeSet<String>();

            Enumeration<URL> en = loader.getResources("META-INF/MANIFEST.MF");
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                String manifest = asString(url.openStream(), true);
                Matcher m = CODENAME.matcher(manifest);
                if (m.find()) {
                    cnbs.add(m.group(1));
                }
            }

            return cnbs;
        }

        private static String asString(InputStream is, boolean close) throws IOException {
            byte[] arr = new byte[is.available()];
            int len = is.read(arr);
            if (len != arr.length) {
                throw new IOException("Not fully read: " + arr.length + " was " + len);
            }
            if (close) {
                is.close();
            }
            return new String(arr, "UTF-8"); // NOI18N
        }

        private static final class JUnitLoader extends ClassLoader {
            private final ClassLoader junit;

            public JUnitLoader(ClassLoader parent, ClassLoader junit) {
                super(parent);
                this.junit = junit;
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (isUnit(name)) {
                    return junit.loadClass(name);
                }
                return super.findClass(name);
            }

            @Override
            public URL findResource(String name) {
                if (isUnit(name)) {
                    return junit.getResource(name);
                }
                return super.findResource(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                if (isUnit(name)) {
                    return junit.getResources(name);
                }
                return super.findResources(name);
            }

            private final boolean isUnit(String res) {
                if (res.startsWith("junit")) {
                    return true;
                }
                if (res.startsWith("org.junit") || res.startsWith("org/junit")) {
                    return true;
                }
                if (res.startsWith("org.netbeans.junit") || res.startsWith("org/netbeans/junit")) {
                    return true;
                }
                return false;
            }
        }

        private static Pattern ENABLED = Pattern.compile("<param name=[\"']enabled[\"']>([^<]*)</param>", Pattern.MULTILINE);

        private static void turnModules(File ud, TreeSet<String> modules, String regExp, File... clusterDirs) throws IOException {
            File config = new File(new File(ud, "config"), "Modules");
            config.mkdirs();

            Pattern modPattern = regExp == null ? null : Pattern.compile(regExp);
            for (File c : clusterDirs) {
                File modulesDir = new File(new File(c, "config"), "Modules");
                for (File m : modulesDir.listFiles()) {
                    String n = m.getName();
                    if (n.endsWith(".xml")) {
                        n = n.substring(0, n.length() - 4);
                    }
                    n = n.replace('-', '.');

                    String xml = asString(new FileInputStream(m), true);
                    Matcher matcherEnabled = ENABLED.matcher(xml);
                 //   Matcher matcherEager = EAGER.matcher(xml);

                    boolean found = matcherEnabled.find();
                    boolean contains = modules.contains(n);
                    if (!contains && modPattern != null) {
                        contains = modPattern.matcher(n).matches();
                    }
                    boolean enabled = found && "true".equals(matcherEnabled.group(1));
                    if (contains == enabled) {
                        continue;
                    }

                    if (found) {
                        assert matcherEnabled.groupCount() == 1 : "Groups: " + matcherEnabled.groupCount() + " for:\n" + xml;

                        try {
                            String out = 
                                xml.substring(0, matcherEnabled.start(1)) +
                                (contains ? "true" : "false") +
                                xml.substring(matcherEnabled.end(1));
                            writeModule(new File(config, m.getName()), out);
                        } catch (IllegalStateException ex) {
                            throw (IOException)new IOException("Unparsable:\n" + xml).initCause(ex);
                        }
                    }
                }
            }
        }

        private static void writeModule(File file, String xml) throws IOException {
            FileOutputStream os = new FileOutputStream(file);
            os.write(xml.getBytes("UTF-8"));
            os.close();
        }
    } // end of S
}
