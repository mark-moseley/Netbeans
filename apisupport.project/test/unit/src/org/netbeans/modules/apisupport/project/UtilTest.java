/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 * Tests {@link Util}.
 *
 * @author Martin Krauskopf
 */
public class UtilTest extends TestBase {
    
    public UtilTest(String name) {
        super(name);
    }
    
    public void testNormalizeCNB() throws Exception {
        assertEquals("space test", "spacetest", Util.normalizeCNB("space test"));
        assertEquals("slash test", "slashtest", Util.normalizeCNB("slash\\test"));
        assertEquals("lowercase test", "org.capital.test", Util.normalizeCNB("org.Capital.test"));
        assertEquals("dot-space test", "org.example.package", Util.normalizeCNB("org...example   ... package..."));
        assertEquals("org.example.hmmmm.misc.test339", Util.normalizeCNB("org.example.hmMMm.misc. TEst3*3=9"));
    }
    
    public void testFindLocalizedBundleInfoFromNetBeansOrgModule() throws Exception {
        FileObject dir = nbroot.getFileObject("apisupport/project");
        assertNotNull("have apisupport/project checked out", dir);
        NbModuleProject apisupport = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        LocalizedBundleInfo info = Util.findLocalizedBundleInfo(
                apisupport.getSourceDirectory(), apisupport.getManifest());
        assertApiSupportInfo(info);
    }
    
    public void testFindLocalizedBundleInfoFromSourceDirectory() throws Exception {
        LocalizedBundleInfo info = Util.findLocalizedBundleInfo(file("apisupport/project"));
        assertApiSupportInfo(info);
    }
    
    public void testFindLocalizedBundleInfoFromSourceDirectory1() throws Exception {
        LocalizedBundleInfo info = Util.findLocalizedBundleInfo(file(extexamplesF, "suite3/dummy-project"));
        assertNull(info);
    }
    
    public void testFindLocalizedBundleInfoFromBinaryModule() throws Exception {
        File apisupportF = file("nbbuild/netbeans/ide6/modules/org-netbeans-modules-apisupport-project.jar");
        assertApiSupportInfo(Util.findLocalizedBundleInfoFromJAR(apisupportF));
    }
    
    private void assertApiSupportInfo(LocalizedBundleInfo info) {
        assertNotNull("info loaded", info);
        // XXX ignore this for now, but be careful when editing the module's properties :)
        assertEquals("display name", "NetBeans Module Projects", info.getDisplayName());
        assertEquals("category", "Developing NetBeans", info.getCategory());
        assertEquals("short description", "Defines an Ant-based project type for NetBeans modules.", info.getShortDescription());
        assertEquals("long description", "Defines a project type for NetBeans " +
                "modules, useful for developing plug-in extensions to NetBeans. " +
                "Provides the logical view for modules, supplies the classpath " +
                "used for code completion, integrates with the NetBeans build " +
                "system (using Ant), etc.", info.getLongDescription());
    }
    
    /** cf. #64782 */
    public void testFindLocalizedBundleInfoLocalization() throws Exception {
        Locale orig = Locale.getDefault();
        Locale.setDefault(Locale.JAPAN);
        try {
            clearWorkDir();
            File dir = getWorkDir();
            Manifest mani = new Manifest();
            mani.getMainAttributes().putValue("Manifest-Version", "1.0");
            mani.getMainAttributes().putValue("OpenIDE-Module-Localizing-Bundle", "pack/age/Bundle.properties");
            // Start with an unlocalized source project.
            File src = new File(dir, "src");
            File f = new File(src, "pack/age/Bundle.properties".replace('/', File.separatorChar));
            f.getParentFile().mkdirs();
            TestBase.dump(f, "OpenIDE-Module-Name=Foo\nOpenIDE-Module-Display-Category=Foo Stuff\nOpenIDE-Module-Short-Description=short\nOpenIDE-Module-Long-Description=Long...");
            // XXX test also Util.findLocalizedBundleInfo(File)?
            LocalizedBundleInfo info = Util.findLocalizedBundleInfo(FileUtil.toFileObject(src), mani);
            assertEquals("Foo", info.getDisplayName());
            assertEquals("Foo Stuff", info.getCategory());
            assertEquals("short", info.getShortDescription());
            assertEquals("Long...", info.getLongDescription());
            // Now add some locale variants.
            f = new File(src, "pack/age/Bundle_ja.properties".replace('/', File.separatorChar));
            TestBase.dump(f, "OpenIDE-Module-Long-Description=Long Japanese text...");
            f = new File(src, "pack/age/Bundle_ja_JP.properties".replace('/', File.separatorChar));
            TestBase.dump(f, "OpenIDE-Module-Name=Foo Nihon");
            info = Util.findLocalizedBundleInfo(FileUtil.toFileObject(src), mani);
            assertEquals("Foo Nihon", info.getDisplayName());
            assertEquals("Foo Stuff", info.getCategory());
            assertEquals("short", info.getShortDescription());
            assertEquals("Long Japanese text...", info.getLongDescription());
            // Now try it on JAR files.
            f = new File(dir, "noloc.jar");
            writeJar(f, Collections.singletonMap("pack/age/Bundle.properties", "OpenIDE-Module-Name=Foo"), mani);
            info = Util.findLocalizedBundleInfoFromJAR(f);
            assertEquals("Foo", info.getDisplayName());
            assertNull(info.getShortDescription());
            f = new File(dir, "internalloc.jar");
            Map/*<String,String>*/ contents = new HashMap();
            contents.put("pack/age/Bundle.properties", "OpenIDE-Module-Name=Foo\nOpenIDE-Module-Short-Description=short");
            contents.put("pack/age/Bundle_ja_JP.properties", "OpenIDE-Module-Name=Foo Nihon");
            writeJar(f, contents, mani);
            info = Util.findLocalizedBundleInfoFromJAR(f);
            assertEquals("Foo Nihon", info.getDisplayName());
            assertEquals("short", info.getShortDescription());
            f = new File(dir, "externalloc.jar");
            writeJar(f, Collections.singletonMap("pack/age/Bundle.properties", "OpenIDE-Module-Name=Foo\nOpenIDE-Module-Short-Description=short"), mani);
            File f2 = new File(dir, "locale" + File.separatorChar + "externalloc_ja.jar");
            f2.getParentFile().mkdirs();
            writeJar(f2, Collections.singletonMap("pack/age/Bundle_ja.properties", "OpenIDE-Module-Short-Description=short Japanese"), new Manifest());
            info = Util.findLocalizedBundleInfoFromJAR(f);
            assertEquals("Foo", info.getDisplayName());
            assertEquals("the meat of #64782", "short Japanese", info.getShortDescription());
        } finally {
            Locale.setDefault(orig);
        }
    }
    
    private static void writeJar(File jar, Map/*<String,String>*/ contents, Manifest manifest) throws IOException {
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = new JarOutputStream(os, manifest);
            Iterator it = contents.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String path = (String) entry.getKey();
                byte[] data = ((String) entry.getValue()).getBytes("UTF-8");
                JarEntry je = new JarEntry(path);
                je.setSize(data.length);
                CRC32 crc = new CRC32();
                crc.update(data);
                je.setCrc(crc.getValue());
                jos.putNextEntry(je);
                jos.write(data);
            }
            jos.close();
        } finally {
            os.close();
        }
    }
    
    public void testLoadProperties() throws Exception {
        File props = file(getWorkDir(), "testing.properties");
        OutputStream propsOS = new FileOutputStream(props);
        PrintWriter pw = new PrintWriter(propsOS);
        try {
            pw.println("property1=some value");
            pw.println("property2=other value");
        } finally {
            pw.close();
        }
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(props));
        assertEquals("property1", "some value", ep.getProperty("property1"));
        assertEquals("property2", "other value", ep.getProperty("property2"));
        try {
            File notFile = file(getWorkDir(), "i_am_not_file");
            notFile.mkdir();
            Util.loadProperties(FileUtil.toFileObject(notFile));
            fail("FileNotFoundException should be thrown");
        } catch (FileNotFoundException fnfe) {
            // fine expected exception has been thrown
        }
    }
    
    public void testStoreProperties() throws Exception {
        FileObject propsFO = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "testing.properties");
        EditableProperties props = Util.loadProperties(propsFO);
        assertTrue("empty props", props.isEmpty());
        props.setProperty("property1", "some value");
        Util.storeProperties(propsFO, props);
        
        BufferedReader reader = new BufferedReader(
                new FileReader(file(getWorkDir(), "testing.properties")));
        try {
            assertEquals("stored property", "property1=some value", reader.readLine());
        } finally {
            reader.close();
        }
    }
    
    // XXX testLoadManifest()
    // XXX testStoreManifest()
    
    public void testFindJavadoc() throws Exception {
        File oneModuleDoc = new File(getWorkDir(), "org-example-module1");
        assertTrue(oneModuleDoc.mkdir());
        File index = new File(oneModuleDoc, "index.html");
        assertTrue(index.createNewFile());
        
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        NbPlatform platform = project.getPlatform();
        URL oneModuleDocURL = Util.urlForDir(oneModuleDoc);
        platform.addJavadocRoot(oneModuleDocURL);
        ModuleDependency md = new ModuleDependency(project.getModuleList().getEntry(project.getCodeNameBase()));
        
        URL url = Util.findJavadoc(md, platform);
        assertNotNull("url was found", url);
        
        File nbDoc = new File(getWorkDir(), "nbDoc");
        File moduleDoc = new File(nbDoc, "org-example-module1");
        assertTrue(moduleDoc.mkdirs());
        index = new File(moduleDoc, "index.html");
        assertTrue(index.createNewFile());
        
        platform.addJavadocRoot(Util.urlForDir(nbDoc));
        platform.removeJavadocRoots(new URL[] {oneModuleDocURL});
        url = Util.findJavadoc(md, platform);
        assertNotNull("url was found", url);
    }
    
}
