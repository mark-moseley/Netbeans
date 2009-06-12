/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectapi;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class LazyLookupProvidersTest extends NbTestCase {

    public LazyLookupProvidersTest(String n) {
        super(n);
    }

    public void testLazyProviders() throws Exception {
        TrackingLoader l = new TrackingLoader();
        Thread.currentThread().setContextClassLoader(l);
        MockLookup.setInstances(l);
        l.assertLoadedClasses();
        Lookup all = LookupProviderSupport.createCompositeLookup(Lookups.fixed("hello"), "Projects/x/Lookup");
        Lookup all2 = LookupProviderSupport.createCompositeLookup(Lookup.EMPTY, "Projects/x/Lookup");
        l.assertLoadedClasses();
        assertEquals("hello", all.lookup(String.class));
        l.assertLoadedClasses();
        Collection<?> svcs2 = all.lookupAll(l.loadClass(Service2.class.getName()));
        assertEquals(1, svcs2.size());
        assertEquals(ServiceImpl2.class.getName(), svcs2.iterator().next().getClass().getName());
        l.assertLoadedClasses("Service2", "ServiceImpl2");
        Collection<?> svcs1 = all.lookupAll(l.loadClass(Service1.class.getName()));
        l.assertLoadedClasses("MergedServiceImpl1", "Merger", "Service1", "Service2", "ServiceImpl1a", "ServiceImpl1b", "ServiceImpl2");
        assertEquals(svcs1.toString(), 1, svcs1.size());
        assertTrue(svcs1.toString(), svcs1.toString().contains("ServiceImpl1a@"));
        assertTrue(svcs1.toString(), svcs1.toString().contains("ServiceImpl1b@"));
        assertTrue(svcs1.toString(), svcs1.toString().contains("Merge["));
        // #166910: also test subsequent independent lookups (i.e. other projects)
        svcs1 = all2.lookupAll(l.loadClass(Service1.class.getName()));
        assertEquals(svcs1.toString(), 1, svcs1.size());
        assertTrue(svcs1.toString(), svcs1.toString().contains("ServiceImpl1a@"));
        assertTrue(svcs1.toString(), svcs1.toString().contains("ServiceImpl1b@"));
        assertTrue(svcs1.toString(), svcs1.toString().contains("Merge["));
    }

    public interface Service1 {}

    public interface Service2 {}

    @ProjectServiceProvider(projectType="x", service=Service1.class)
    public static class ServiceImpl1a implements Service1 {}

    public static class ServiceImpl1b implements Service1 {
        private ServiceImpl1b(boolean x) {assert x;}
        public void someUnrelatedMethod() {}
        @ProjectServiceProvider(projectType="x", service=Service1.class)
        public static Service1 makeService() {return new ServiceImpl1b(true);}
    }

    @ProjectServiceProvider(projectType="x", service=Service2.class)
    public static class ServiceImpl2 implements Service2 {
        public ServiceImpl2(Lookup base) {
            assertNotNull(base.lookup(String.class));
        }
    }

    @LookupMerger.Registration(projectType="x")
    public static class Merger implements LookupMerger<Service1> {
        public Class<Service1> getMergeableClass() {
            return Service1.class;
        }
        public Service1 merge(final Lookup lkp) {
            return new MergedServiceImpl1(lkp.lookupAll(Service1.class));
        }
    }
    private static class MergedServiceImpl1 implements Service1 {
        private final Collection<? extends Service1> delegates;
        MergedServiceImpl1(Collection<? extends Service1> delegates) {
            this.delegates = delegates;
        }
        public @Override String toString() {
            return "Merge" + delegates;
        }
    }

    public void testMultiplyImplementedService() throws Exception {
        TrackingLoader l = new TrackingLoader();
        Thread.currentThread().setContextClassLoader(l);
        MockLookup.setInstances(l);
        l.assertLoadedClasses();
        Lookup all = LookupProviderSupport.createCompositeLookup(Lookup.EMPTY, "Projects/y/Lookup");
        l.assertLoadedClasses();
        Collection<?> instances = all.lookupAll(l.loadClass(Service3.class.getName()));
        assertEquals(1, instances.size());
        l.assertLoadedClasses("Service3", "Service34Impl", "Service4");
        assertEquals(instances, all.lookupAll(l.loadClass(Service4.class.getName())));
        l.assertLoadedClasses("Service3", "Service34Impl", "Service4");
    }

    public interface Service3 {}

    public interface Service4 {}

    @ProjectServiceProvider(projectType="y", service={Service3.class, Service4.class})
    public static class Service34Impl implements Service3, Service4 {}

    /**
     * Cannot simply use static initializers to tell when classes are loaded;
     * these will not be run in case a service is loaded but not yet initialized.
     */
    private static class TrackingLoader extends URLClassLoader {
        private final Set<Class<?>> loadedClasses = new HashSet<Class<?>>();
        TrackingLoader() {
            super(new URL[] {LazyLookupProvidersTest.class.getProtectionDomain().getCodeSource().getLocation()},
                  LazyLookupProvidersTest.class.getClassLoader());
        }
        protected @Override synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith(LazyLookupProvidersTest.class.getName() + "$")) {
                Class c = findLoadedClass(name);
                if (c == null) {
                    // do not delegate to parent, i.e. be sure we have loaded it
                    c = findClass(name);
                    if (resolve) {
                        resolveClass(c);
                    }
                    loadedClasses.add(c);
                }
                return c;
            } else {
                return super.loadClass(name, resolve);
            }
        }
        void assertLoadedClasses(String... names) {
            SortedSet<String> actual = new TreeSet<String>();
            for (Class<?> clazz : loadedClasses) {
                actual.add(clazz.getName().replaceFirst("^\\Q" + LazyLookupProvidersTest.class.getName() + "$\\E", ""));
            }
            assertEquals(Arrays.toString(names), actual.toString());
        }
    }

}
