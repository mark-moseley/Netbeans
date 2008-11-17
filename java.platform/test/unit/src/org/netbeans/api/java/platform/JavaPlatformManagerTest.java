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

package org.netbeans.api.java.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 * @author Tomas Zezula, Jesse Glick
 */
public class JavaPlatformManagerTest extends NbTestCase {

    public JavaPlatformManagerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new TestJavaPlatformProvider());
    }

    public void testGetDefaultPlatform() {
        MockLookup.setInstances(); // make sure we are using pure defaults
        JavaPlatformManager manager = JavaPlatformManager.getDefault ();
        assertNotNull (manager);
        JavaPlatform p = manager.getDefaultPlatform();
        assertNotNull("some platform registered by default", p);
        ClassPath cp = p.getBootstrapLibraries();
        assertNotNull("is 1.5+ JRE: " + cp, cp.findResource("java/lang/StringBuilder.class"));
        assertFalse(p.getInstallFolders().isEmpty());
        //assertNotNull(p.findTool("javac"));
        assertNotNull(p.getDisplayName());
        assertNotNull(p.getSystemProperties().get("java.home"));
        assertNotNull(p.getSourceFolders());
        assertNotNull(p.getJavadocFolders());
        cp = p.getStandardLibraries();
        assertNotNull("contains test CP: " + cp, cp.findResource(JavaPlatformManager.class.getName().replace('.', '/') + ".class"));
        assertNotNull(p.getProperties());
        assertNotNull(p.getVendor());
        Specification spec = p.getSpecification();
        assertNotNull(spec);
        assertNotNull(spec.getName());
        // can be null: assertNotNull(spec.getProfiles());
        assertTrue(spec.getVersion().compareTo(new SpecificationVersion("1.5")) >= 0);
    }

    public void testGetInstalledPlatforms() {
        JavaPlatformManager manager = JavaPlatformManager.getDefault();
        assertNotNull (manager);
        TestJavaPlatformProvider provider = TestJavaPlatformProvider.getDefault ();
        assertNotNull (provider);
        JavaPlatform[] platforms = manager.getInstalledPlatforms();
        assertNotNull (platforms);
        assertTrue (platforms.length == 0);
        JavaPlatform platform = new TestJavaPlatform ("Testing Platform",
            new Specification("j2se", new SpecificationVersion ("1.5")));
        provider.addPlatform (platform);
        platforms = manager.getInstalledPlatforms();
        assertNotNull (platforms);
        assertTrue (platforms.length == 1);
        assertTrue (platforms[0]==platform);
        provider.removePlatform(platform);
        platforms = manager.getInstalledPlatforms();
        assertNotNull (platforms);
        assertTrue (platforms.length == 0);
    }

    public void testGetPlatforms() {
        JavaPlatformManager manager = JavaPlatformManager.getDefault();
        assertNotNull (manager);
        TestJavaPlatformProvider provider = TestJavaPlatformProvider.getDefault ();
        assertNotNull (provider);
        JavaPlatform p1 = new TestJavaPlatform ("P1", new Specification("P1",new SpecificationVersion ("1.4")));
        JavaPlatform p2 = new TestJavaPlatform ("P2", new Specification("P2",new SpecificationVersion ("1.4")));
        JavaPlatform p3 = new TestJavaPlatform ("P3", new Specification("P3",new SpecificationVersion ("1.4")));
        JavaPlatform p4 = new TestJavaPlatform ("P4", new Specification("P4",new SpecificationVersion ("1.5")));
        JavaPlatform p5 = new TestJavaPlatform ("P5", new Specification("CDC",new SpecificationVersion("1.0"), new Profile[] {
            new Profile ("PersonalJava", new SpecificationVersion ("1.0")),
            new Profile ("RMI", new SpecificationVersion ("1.0")),
        }));
        JavaPlatform p6 = new TestJavaPlatform ("P6", new Specification("CDC", new SpecificationVersion("1.0")));
        JavaPlatform p7 = new TestJavaPlatform ("P7", new Specification("CDC",new SpecificationVersion("1.0"), new Profile[] {
            new Profile ("PersonalJava", new SpecificationVersion ("1.0"))
        }));
        JavaPlatform p8 = new TestJavaPlatform ("P8", new Specification("CDC",new SpecificationVersion("1.0"), new Profile[] {
            new Profile ("PersonalJava", new SpecificationVersion ("1.0")),
            new Profile ("JNI", new SpecificationVersion ("1.0")),
            new Profile ("GIOP", new SpecificationVersion ("1.0"))
        }));
        provider.addPlatform (p1);
        provider.addPlatform (p2);
        provider.addPlatform (p3);
        provider.addPlatform (p4);
        provider.addPlatform (p5);
        provider.addPlatform (p6);
        provider.addPlatform (p7);
        provider.addPlatform (p8);
        assertNotNull (manager.getInstalledPlatforms());
        assertTrue (manager.getInstalledPlatforms().length == 8);
        JavaPlatform[] r = manager.getPlatforms("P1",null);
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms("P1", new Specification ("P1", new SpecificationVersion("1.4")));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms("P1", new Specification ("P1", null));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms(null, new Specification ("P1", null));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms(null, new Specification ("P1", new SpecificationVersion("1.4")));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms(null, new Specification (null,new SpecificationVersion("1.4")));
        assertNotNull (r);
        assertTrue (r.length == 3);
        assertEquivalent (r, new JavaPlatform[]{p1,p2,p3});
        // Test of profiles
        r = manager.getPlatforms (null, new Specification ("CDC", new SpecificationVersion("1.0")));        //Any CDC
        assertNotNull (r);
        assertTrue (r.length == 4);
        assertEquivalent (r, new JavaPlatform[] {p5, p6, p7, p8});
        r = manager.getPlatforms (null, new Specification ("CDC", null, new Profile[] {                     // CDC with PersonalJava/* and RMI/*
            new Profile ("PersonalJava",null),
            new Profile ("RMI",null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0]==p5);
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       // CDC with any existing profile
            new Profile (null,null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 3);
        assertEquivalent (r, new JavaPlatform[] {p5,p7,p8});
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       // CDC with PersonalJava/* and */*
            new Profile ("PersonalJava",null),
            new Profile (null,null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 3);
        assertEquivalent (r, new JavaPlatform[] {p5,p7,p8});
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       //CDC with PersonalJava/*
            new Profile ("PersonalJava",null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p7);
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       //CDC with RMI/* and */*
            new Profile ("RMI",null),
            new Profile (null,null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p5);
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       //CDC with Gateway/* and */*
            new Profile ("Gateway",null),
            new Profile (null, null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 0);
        r = manager.getPlatforms(null,null);                                                              //All platforms
        assertNotNull(r);
        assertTrue (r.length == 8);
        assertEquivalent (r, new JavaPlatform[] {p1,p2,p3,p4,p5,p6,p7, p8});

        //Done, clean up
        provider.removePlatform (p1);
        provider.removePlatform (p2);
        provider.removePlatform (p3);
        provider.removePlatform (p4);
        provider.removePlatform (p5);
        provider.removePlatform (p6);
        provider.removePlatform (p7);
        provider.removePlatform (p8);
        assertTrue (manager.getInstalledPlatforms().length == 0);
    }


    private static void assertEquivalent (JavaPlatform[] a, JavaPlatform[] b) {
        assertTrue (a.length == b.length);
        List l = Arrays.asList(a);
        for (int i=0; i < b.length; i++) {
            if (!l.contains(b[i])) {
                assertTrue (false);
            }
        }
    }

    private static class TestJavaPlatform extends JavaPlatform {

        private String id;
        private Specification spec;

        public TestJavaPlatform (String id, Specification spec) {
            this.id = id;
            this.spec = spec;
        }

        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

        public String getDisplayName() {
            return this.id;
        }

        public Collection<FileObject> getInstallFolders() {
            return Collections.emptyList();
        }

        public List<URL> getJavadocFolders() {
            return Collections.emptyList();
        }

        public Map getProperties() {
            return Collections.EMPTY_MAP;
        }

        public ClassPath getSourceFolders() {
            return ClassPathSupport.createClassPath(Collections.EMPTY_LIST);
        }

        public Specification getSpecification() {
            return this.spec;
        }

        public ClassPath getStandardLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

        public String getVendor() {
            return "Me";
        }

        public FileObject findTool(String name) {
            return null;
        }

    }

    public static class TestJavaPlatformProvider implements JavaPlatformProvider {

        private ArrayList listeners = new ArrayList ();
        private List platforms = new ArrayList ();


        static TestJavaPlatformProvider getDefault () {
            return (TestJavaPlatformProvider) Lookup.getDefault ().lookup (TestJavaPlatformProvider.class);
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            assertNotNull (listener);
            this.listeners.add (listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            assertNotNull (listener);
            this.listeners.remove (listener);
        }

        public JavaPlatform[] getInstalledPlatforms() {
            return (JavaPlatform[]) this.platforms.toArray (new JavaPlatform[platforms.size()]);
        }

        void addPlatform (JavaPlatform platform) {
            this.platforms.add (platform);
            this.firePropertyChange ();
        }

        void removePlatform (JavaPlatform platform) {
            this.platforms.remove (platform);
            this.firePropertyChange ();
        }

        private void firePropertyChange () {
            Iterator it;
            synchronized (this) {
                it = ((Collection)this.listeners.clone()).iterator();
            }
            PropertyChangeEvent event = new PropertyChangeEvent (this, PROP_INSTALLED_PLATFORMS, null, null);
            while (it.hasNext()) {
                ((PropertyChangeListener)it.next()).propertyChange(event);
            }
        }

        public JavaPlatform getDefaultPlatform() {
            if (platforms.size()>0)
                return (JavaPlatform) platforms.get(0);
            else
                return null;
        }

    }

}
