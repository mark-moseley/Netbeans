/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.openide.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.util.test.AnnotationProcessorTestUtils;

public class ServiceProviderProcessorTest extends NbTestCase {

    public ServiceProviderProcessorTest(String n) {
        super(n);
    }

    private static List<Class<?>> classesOf(Iterable<?> objects) {
        List<Class<?>> cs = new ArrayList<Class<?>>();
        for (Object o : objects) {
            cs.add(o.getClass());
        }
        return cs;
    }

    private static List<Class<?>> classesOfLookup(Class<?> xface) {
        return classesOf(Lookup.getDefault().lookupAll(xface));
    }

    private static List<Class<?>> sortClassList(List<Class<?>> classes) {
        List<Class<?>> sorted = new ArrayList<Class<?>>(classes);
        Collections.sort(sorted, new Comparator<Class<?>>() {
            public int compare(Class<?> c1, Class<?> c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        return sorted;
    }

    public void testBasicUsage() throws Exception {
        assertEquals(Collections.singletonList(Implementation.class), classesOfLookup(Interface.class));
    }
    public interface Interface {}
    @ServiceProvider(service=Interface.class)
    public static class Implementation implements Interface {}

    public void testPosition() throws Exception {
        assertEquals(Arrays.<Class<?>>asList(OrderedImpl3.class, OrderedImpl2.class, OrderedImpl1.class), classesOfLookup(OrderedInterface.class));
    }
    public interface OrderedInterface {}
    @ServiceProvider(service=OrderedInterface.class)
    public static class OrderedImpl1 implements OrderedInterface {}
    @ServiceProvider(service=OrderedInterface.class, position=200)
    public static class OrderedImpl2 implements OrderedInterface {}
    @ServiceProvider(service=OrderedInterface.class, position=100)
    public static class OrderedImpl3 implements OrderedInterface {}

    public void testPath() throws Exception {
        assertEquals(Collections.singletonList(PathImplementation.class), classesOf(Lookups.forPath("some/path").lookupAll(Interface.class)));
    }
    @ServiceProvider(service=Interface.class, path="some/path")
    public static class PathImplementation implements Interface {}

    public void testSupersedes() throws Exception {
        assertEquals(Arrays.<Class<?>>asList(Overrider.class, Unrelated.class), sortClassList(classesOfLookup(CancellableInterface.class)));
    }
    public interface CancellableInterface {}
    @ServiceProvider(service=CancellableInterface.class)
    public static class Overridden implements CancellableInterface {}
    @ServiceProvider(service=CancellableInterface.class, supersedes="org.netbeans.modules.openide.util.ServiceProviderProcessorTest$Overridden")
    public static class Overrider implements CancellableInterface {}
    @ServiceProvider(service=CancellableInterface.class)
    public static class Unrelated implements CancellableInterface {}

    public void testMultipleRegistrations() throws Exception {
        assertEquals(Collections.singletonList(Multitasking.class), classesOfLookup(Interface1.class));
        assertEquals(Collections.singletonList(Multitasking.class), classesOfLookup(Interface2.class));
    }
    public interface Interface1 {}
    public interface Interface2 {}
    @ServiceProviders({@ServiceProvider(service=Interface1.class), @ServiceProvider(service=Interface2.class)})
    public static class Multitasking implements Interface1, Interface2 {}

    public void testErrorReporting() throws Exception {
        clearWorkDir();
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        String xfaceName = Interface.class.getCanonicalName();

        AnnotationProcessorTestUtils.makeSource(src, "p.C1",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class C1 implements " + xfaceName + " {}");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C1", dest, null, baos));

        AnnotationProcessorTestUtils.makeSource(src, "p.C2",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "class C2 implements " + xfaceName + " {}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C2", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("public"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C3",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class C3 implements " + xfaceName + " {",
                "public C3(boolean x) {}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C3", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("constructor"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C4",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class C4 implements " + xfaceName + " {",
                "C4() {}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C4", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("constructor"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C5",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public abstract class C5 implements " + xfaceName + " {}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C5", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("abstract"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C6",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class C6 {}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C6", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("assignable"));
    }

}
