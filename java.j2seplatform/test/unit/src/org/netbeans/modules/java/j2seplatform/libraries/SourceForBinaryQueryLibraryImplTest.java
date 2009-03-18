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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seplatform.platformdefinition.JavaPlatformProviderImpl;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 * J2SELibrarySourceForBinaryQuery test
 *
 */
public class SourceForBinaryQueryLibraryImplTest extends NbTestCase {
    
    public SourceForBinaryQueryLibraryImplTest(String testName) {
        super(testName);
        MockLookup.setLookup(
                Lookups.fixed(
                    LibraryProviderImpl.getDefault(),
                    new JavaPlatformProviderImpl(),
                    new ArchiveURLMapper(),
                    new J2SELibrarySourceForBinaryQuery(),
                    new MasterURLMapper()
                    ));
    }
    
    private String getBase() throws Exception {
        File dir = getWorkDir();
        if (Utilities.isWindows()) {
            dir = new File(dir.getCanonicalPath());
        }
        return dir.toString();
    }
    
    protected @Override void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath()); 
        super.setUp();
        clearWorkDir();        
    }
    
    private void setupLibraries() throws Exception {
        File dir = new File(getBase());
        
        // create library1:
        String libPath = dir.toString() + "/library1";
        File library = createJar(new File(libPath), "library1.jar", new String[]{"Main.class"});
        File src = new File(libPath+"/src1");
        src.mkdir();
        registerLibrary("library1", library, src);
        
        // create library2:
        libPath = dir.toString() + "/library2";
        library = createJar(new File(libPath), "library2.jar", new String[]{"Main.class"});
        src = createJar(new File(libPath), "library2src.jar", new String[]{"Main.java"});
        registerLibrary("library2", library, src);
        
        // create library3:
        libPath = dir.toString() + "/library3";
        library = new File(libPath+"/library3");
        library.mkdirs();
        src = new File(libPath+"/src3");
        src.mkdirs();
        registerLibrary("library3", library, src);
                                
        // refresh FS
        FileUtil.toFileObject(dir).getFileSystem().refresh(false);
    }
    
    private void setupLibraryForListeningTest () throws Exception {
        // create library4:
        File dir = new File(getBase());
        String libPath = dir.toString() + "/library4";
        File library = new File(libPath,"library4");
        library.mkdirs();
        File src = new File(libPath+"/src4");
        src.mkdirs();
        registerLibrary("library4", library, null);
        // refresh FS
        FileUtil.toFileObject(dir).getFileSystem().refresh(false);
    }
    
    private File createJar(File folder, String name, String resources[]) throws Exception {
        folder.mkdirs();
        File f = new File(folder,name);
        if (!f.exists()) {
            f.createNewFile();
        }
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
        for (int i = 0; i < resources.length; i++) {
            jos.putNextEntry(new ZipEntry(resources[i]));
        }
        jos.close();
        return f;
    }
    
    private void registerLibrary(final String libName, final File cp, final File src) throws Exception {
        DefaultLibraryImplementation lib;
        lib = new DefaultLibraryImplementation("j2se", new String[]{"classpath", "src"});
        lib.setName(libName);
        List<URL> l = new ArrayList<URL>();
        URL u = cp.toURI().toURL();
        if (cp.getPath().endsWith(".jar")) {
            u = FileUtil.getArchiveRoot(u);
        }
        l.add(u);
        lib.setContent("classpath", l);
        if (src != null) {
            l = new ArrayList<URL>();
            u = src.toURI().toURL();
            if (src.getPath().endsWith(".jar")) {
                u = FileUtil.getArchiveRoot(u);
            }
            l.add(u);
            lib.setContent("src", l);
        }
        LibraryProviderImpl prov = LibraryProviderImpl.getDefault();
        prov.addLibrary(lib);
    }
    
    private LibraryImplementation getLibrary (String name) {
        LibraryProviderImpl prov = LibraryProviderImpl.getDefault();
        LibraryImplementation[] impls = prov.getLibraries();
        for (int i=0; i< impls.length; i++) {
            if (impls[i].getName().equals (name)) {
                return impls[i];
            }
        }
        return null;
    }
    
    
    public void testQuery() throws Exception {
        setupLibraries();
        
        // library1: test that folder with javadoc is found for the jar
        File f = new File(getBase()+"/library1/library1.jar");
        URL u = f.toURI().normalize().toURL();
        u = FileUtil.getArchiveRoot(u);
        FileObject[] fos = SourceForBinaryQuery.findSourceRoots(u).getRoots();
        assertEquals(1, fos.length);
        String base = new File(getBase()).toURI().toString();
        assertEquals(base+"library1/src1/", fos[0].getURL().toExternalForm());
        
        // library2: test that jar with javadoc is found for the class from library jar
        f = new File(getBase()+"/library2/library2.jar");
        String us = f.toURI().normalize().toString();
        us = "jar:" + us + "!/";
        u = new URL(us);
        fos = SourceForBinaryQuery.findSourceRoots(u).getRoots();
        assertEquals(1, fos.length);
        assertEquals("jar:"+base+"library2/library2src.jar!/", fos[0].getURL().toExternalForm());
        
        // library2: test that folder with javadoc is found for the classpath root from the library
        f = new File(getBase()+"/library3/library3");
        u = f.toURI().normalize().toURL();
        fos = SourceForBinaryQuery.findSourceRoots(u).getRoots();
        assertEquals(1, fos.length);
        assertEquals(base+"library3/src3/", fos[0].getURL().toExternalForm());
    }
    
    public void testListening () throws Exception {
        setupLibraryForListeningTest();
        File f = new File(getBase()+"/library4");
        f = new File (f, "library4");
        URL u = f.toURI().normalize().toURL();
        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(u);
        assertEquals(result.getRoots().length,0);
        SFBQResultListener l = new SFBQResultListener ();        
        result.addChangeListener(l);
        LibraryImplementation impl = getLibrary("library4");
        List<URL> srcList = new ArrayList<URL>();
        File baseDir = new File(getBase());
        File libDir = new File(baseDir,"library4");
        File srcDir = new File(libDir,"src4");
        srcList.add (srcDir.toURI().toURL());
        impl.setContent("src", srcList);
        ChangeEvent[] events = l.getEvents();
        assertEquals(1,events.length);
        l.clearEventQueue();
        assertEquals(result.getRoots().length,1);
        String base = new File(getBase()).toURI().toString();
        assertEquals(base+"library4/src4/",result.getRoots()[0].getURL().toExternalForm());
    }
    
    private static class SFBQResultListener implements ChangeListener {
        
        private final List<ChangeEvent> queue = new ArrayList<ChangeEvent>();

        public SFBQResultListener() {}
        
        public void clearEventQueue () {
            this.queue.clear();
        }

        public void stateChanged(ChangeEvent event) {
            this.queue.add (event);
        }
        
        public ChangeEvent[] getEvents () {
            return queue.toArray(new ChangeEvent[queue.size()]);
        }                                    
    }
    
}

