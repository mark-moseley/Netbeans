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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

package org.netbeans.modules.settings.convertors;

import java.io.*;

import org.netbeans.junit.NbTestCase;


import org.netbeans.spi.settings.Convertor;
import org.netbeans.spi.settings.Saver;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/** JUnit tests
 *
 * @author  Jan Pokorsky
 */
public final class XMLPropertiesConvertorTest extends NbTestCase {
    private FileSystem fs;
    private FileSystem sfs;
    private FileObject root;
    
    /** Creates a new instance of XMLPropertiesConvertorTest */
    public XMLPropertiesConvertorTest(String name) {
        super(name);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
        java.net.URL layer = this.getClass().getResource("data/layer.xml");
        fs = new XMLFileSystem(layer);
        sfs = Repository.getDefault().getDefaultFileSystem();
        root = sfs.getRoot();
        assertNotNull("SFS root not found", root);
        
        FileObject serdata = sfs.findResource("xml/lookups/NetBeans/DTD_Session_settings_1_0.instance");
        assertNotNull("missing registration for serialdata format", serdata);
        Object attr = serdata.getAttribute("instanceCreate");
        assertNotNull("core's registration for serialdata format", attr);
        assertEquals(SerialDataConvertor.Provider.class, attr.getClass());
    }
    
    public void testReadWrite() throws Exception {
        FileObject dtdFO = Repository.getDefault().getDefaultFileSystem().
            findResource("/xml/lookups/NetBeans_org_netbeans_modules_settings_xtest/DTD_XML_FooSetting_1_0.instance");
        assertNotNull("Provider not found", dtdFO);
        Convertor c = XMLPropertiesConvertor.create(dtdFO);
        FooSetting foo = new FooSetting();
        foo.setProperty1("xxx");
        CharArrayWriter caw = new CharArrayWriter(1024);
        c.write(caw, foo);
        caw.flush();
        caw.close();
        CharArrayReader car = new CharArrayReader(caw.toCharArray());
        Object obj = c.read(car);
        assertEquals(foo, obj);
    }
    
    public void testRegisterUnregisterSaver() throws Exception {
        FileObject dtdFO = fs.findResource("/xml/lookups/NetBeans_org_netbeans_modules_settings/DTD_XML_FooSetting_1_0.instance");
        assertNotNull("Provider not found", dtdFO);
        Convertor c = XMLPropertiesConvertor.create(dtdFO);
        FooSetting foo = new FooSetting();
        SaverImpl s = new SaverImpl();
        c.registerSaver(foo, s);
        foo.setProperty1("xxx");
        assertEquals("Saver was not notified about the change.", SaverImpl.SAVE, s.state);
        c.unregisterSaver(foo, s);
        s.state = SaverImpl.NOT_CHANGED;
        foo.setProperty1("yyy");
        assertEquals("Saver was notified about the change.", SaverImpl.NOT_CHANGED, s.state);
        
        Object obj = new Object();
        c.registerSaver(obj, s);
        c.unregisterSaver(obj, s);
    }
    
    public void testSaverNotification() throws Exception {
        // test xmlproperties.ignoreChanges=aaa, property1
        FileObject dtdFO = fs.findResource("/xml/lookups/NetBeans_org_netbeans_modules_settings/DTD_XML_FooSetting1_1_0.instance");
        assertNotNull("Provider not found", dtdFO);
        Convertor c = XMLPropertiesConvertor.create(dtdFO);
        FooSetting foo = new FooSetting();
        SaverImpl s = new SaverImpl();
        c.registerSaver(foo, s);
        foo.setProperty1("xxx");
        assertEquals("Saver was notified about the ignored change.", SaverImpl.NOT_CHANGED, s.state);
        
        // test xmlproperties.ignoreChanges=all
        dtdFO = fs.findResource("/xml/lookups/NetBeans_org_netbeans_modules_settings/DTD_XML_FooSetting2_1_0.instance");
        assertNotNull("Provider not found", dtdFO);
        c = XMLPropertiesConvertor.create(dtdFO);
        foo = new FooSetting();
        s = new SaverImpl();
        c.registerSaver(foo, s);
        foo.setProperty1("xxx");
        assertEquals("Saver was notified about the ignored change.", SaverImpl.NOT_CHANGED, s.state);
        
        // test xmlproperties.preventStoring=true
        dtdFO = fs.findResource("/xml/lookups/NetBeans_org_netbeans_modules_settings/DTD_XML_FooSetting3_1_0.instance");
        assertNotNull("Provider not found", dtdFO);
        c = XMLPropertiesConvertor.create(dtdFO);
        foo = new FooSetting();
        s = new SaverImpl();
        c.registerSaver(foo, s);
        foo.setProperty1("xxx");
        assertEquals("Saver was not marked as dirty.", SaverImpl.DIRTY, s.state);
    }
    
    //////////////////////////////////////////////////////////
    // Tests on SFS
    //////////////////////////////////////////////////////////
    
    
    /** Checks whether the instance is the same.
     */
    public void testSame() throws Exception {
        FileObject tsFO = root.createFolder("testSame");
        assertNotNull("folder 'testSame' is not created!", tsFO);
        DataFolder folder = (DataFolder) DataObject.find(tsFO).getCookie(DataFolder.class);
        assertNotNull("missing data folder" + folder);
        
        FooSetting ser = new FooSetting("A");
        
        InstanceDataObject i = InstanceDataObject.create (folder, null, ser, null);
        
        InstanceCookie.Of ic = (InstanceCookie.Of) i.getCookie(InstanceCookie.Of.class);
        assertNotNull (i + " does not contain instance cookie", ic);
        
        assertTrue("instanceOf failed", ic.instanceOf(ser.getClass()));
        assertEquals("instanceClass", ser.getClass(), ic.instanceClass());
        
        Object n = ic.instanceCreate ();
        assertEquals("Value is different from stored one", System.identityHashCode(ser), System.identityHashCode(n));
        
        ser.setProperty1("B");
        ic = (InstanceCookie.Of) i.getCookie(InstanceCookie.Of.class);
        assertEquals("Value is different from stored one", ser, ic.instanceCreate());
        
        for (int j = 0; j <100; j++) {
            ser.setProperty1(String.valueOf(j));
        }
        ic = (InstanceCookie.Of) i.getCookie(InstanceCookie.Of.class);
        assertEquals("Value is different from stored one", ser, ic.instanceCreate());
        
        i.delete();
    }
    
    /** Test whether instances survive garbage collection.
     */
    public void testSameWithGC () throws Exception {
        FileObject tsFO = root.createFolder("testSameWithGC");
        assertNotNull("folder 'testSameWithGC' is not created!", tsFO);
        DataFolder folder = (DataFolder) DataObject.find(tsFO).getCookie(DataFolder.class);
        assertNotNull("missing data folder" + folder);
        
        FooSetting ser = new FooSetting("testSameWithGC");
        
        FileObject prim = InstanceDataObject.create (folder, "MyName", ser, null).getPrimaryFile ();
        String name = prim.getName ();
        String ext = prim.getExt ();
        prim = null;

        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        
        FileObject fo = folder.getPrimaryFile ().getFileObject (name, ext);
        assertNotNull ("MyName.settings not found", fo);
        
        DataObject obj = DataObject.find (fo);
        
        InstanceCookie.Of ic = (InstanceCookie.Of) obj.getCookie(InstanceCookie.Of.class);
        assertNotNull (obj + " does not contain instance cookie", ic);
        
        assertTrue("instanceOf failed", ic.instanceOf(ser.getClass()));
        assertEquals("instanceClass", ser.getClass(), ic.instanceClass());
        
        Object value = ic.instanceCreate ();
        assertEquals("Value is different from stored one", System.identityHashCode(ser), System.identityHashCode(value));
    }
    
    public void testLookupSetting() throws Exception {
        Object obj = Lookup.getDefault().lookup(FooSetting.class);
        assertNotNull("setting not found via the lookup api", obj);
        assertEquals(FooSetting.class, obj.getClass());
        
        FooSetting foo = (FooSetting) obj;
        assertEquals("localhost", foo.getProperty1());
    }

    /* default instance in serial data format -> xml properties format
     */
    public void testUpgradeSetting() throws Exception {
        String res = "Settings/org-netbeans-modules-settings-convertors-FooSettingSerialData.settings";
        FileObject fo = sfs.findResource(res);
        assertNotNull(res, fo);
        long last = fo.lastModified().getTime();
        
        DataObject dobj = DataObject.find (fo);
        InstanceCookie.Of ic = (InstanceCookie.Of) dobj.getCookie(InstanceCookie.Of.class);
        assertNotNull (dobj + " does not contain instance cookie", ic);
        assertTrue("instanceOf failed", ic.instanceOf(FooSetting.class));
        assertEquals("instanceClass failed", FooSetting.class, ic.instanceClass());
        
        FooSetting foo = (FooSetting) ic.instanceCreate();
        assertEquals("too early upgrade", last, fo.lastModified().getTime());
        
        foo.setProperty1("A");
        Thread.sleep(3000);
        assertTrue("upgrade failed", last != fo.lastModified().getTime());
    }
    
    /* object of deprecated class persisted in serial data format -> new object
     * persisted in xml properties format
     */
    public void testUpgradeSetting2() throws Exception {
        String res = "Settings/testUpgradeSetting2/ObsoleteClass.settings";
        FileObject fo = sfs.findResource(res);
        assertNotNull(res, fo);
        long last = fo.lastModified().getTime();
        
        DataObject dobj = DataObject.find(fo);
        InstanceCookie.Of ic = (InstanceCookie.Of) dobj.getCookie(InstanceCookie.Of.class);
        assertNotNull (dobj + " does not contain instance cookie", ic);
        assertTrue("instanceOf failed", ic.instanceOf(FooSetting.class));
        assertEquals("instanceClass failed", FooSetting.class, ic.instanceClass());
        
        FooSetting foo = (FooSetting) ic.instanceCreate();
        assertEquals("too early upgrade", last, fo.lastModified().getTime());
        
        foo.setProperty1("A");
        Thread.sleep(3000);
        assertTrue("upgrade failed", last != fo.lastModified().getTime());
    }
    
    public void testUpgradeSettingWithUnknownClass() throws Exception {
        String res = "Settings/org-netbeans-modules-settings-convertors-FooSettingSerialDataUnknownClass.settings";
        FileObject fo = sfs.findResource(res);
        assertNotNull(res, fo);
        long last = fo.lastModified().getTime();
        
        DataObject dobj = DataObject.find (fo);
        InstanceCookie.Of ic = (InstanceCookie.Of) dobj.getCookie(InstanceCookie.Of.class);
        assertNotNull (dobj + " does not contain instance cookie", ic);
        assertTrue("instanceOf failed", ic.instanceOf(FooSetting.class));
        assertEquals("instanceClass failed", FooSetting.class, ic.instanceClass());
        
        FooSetting foo = (FooSetting) ic.instanceCreate();
        assertEquals("too early upgrade", last, fo.lastModified().getTime());
        
        foo.setProperty1("A");
        Thread.sleep(3000);
        
        DataInputStream dis = new DataInputStream(new BufferedInputStream(fo.getInputStream(), 1024));
        StringBuffer sb = new StringBuffer(dis.readLine());
        sb.append(dis.readLine());
        sb.append(dis.readLine());
        dis.close();
        String line = sb.toString();
        
        assertTrue("upgrade failed: " + line, line.indexOf("properties") > 0);
//        assertTrue("upgrade failed", last != fo.lastModified().getTime());
        
    }
    
    public void testDeleteSettings() throws Exception {
        FileObject root = sfs.getRoot();
        DataFolder folder = DataFolder.findFolder(root);
        
        String filename = "testDeleteSettings";
        FooSetting obj = new FooSetting();
        InstanceDataObject ido = InstanceDataObject.create(folder, filename, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        obj.setProperty1("testDeleteSettings");
        Thread.sleep(500);
        ido.delete();
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
        assertEquals("Listener not deregistered", 0, obj.getListenerCount());
        Thread.sleep(3000);
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
    }
    
    public void testModuleDisabling() throws Exception {
        FileObject dtd = sfs.findResource("xml/lookups/NetBeans_org_netbeans_modules_settings_testModuleDisabling/DTD_XML_FooSetting_1_0.instance");
        assertNotNull(dtd);
        FileObject xml = sfs.findResource("Settings/org-netbeans-modules-settings-convertors-testModuleDisabling.settings");
        assertNotNull(xml);
        DataObject dobj = DataObject.find(xml);
        InstanceCookie cookie = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
        assertNotNull(cookie);
        cookie = null;
        
        FileObject folder = sfs.findResource("xml/lookups/NetBeans_org_netbeans_modules_settings_testModuleDisabling");
        assertNotNull(folder);
        // this simulate the disabling of a module; the layer containing the dtd
        // registration is removed
        folder.delete();
        cookie = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
        assertNull("" + cookie, cookie);
    }
   
    public void testCorruptedSettingsFile() throws Exception {
        final FileObject corrupted = sfs.findResource("/Settings/org-netbeans-modules-settings-convertors-FooSettingXMLPropCorruptedTest.settings");
        assertNotNull(corrupted);
        
        DataObject ido = DataObject.find(corrupted);
        InstanceCookie ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        assertNotNull("Missing InstanceCookie", ic);
        
        Object obj = null;
        try {
            obj = ic.instanceCreate();
        } catch (IOException ex) {
        }
        assertNull("corrupted .settings file cannot provide an object", obj);
        
        final FileObject valid = sfs.findResource("/Services/org-netbeans-modules-settings-convertors-FooSetting.settings");
        assertNotNull(valid);
        
        // simulate revert to default of a corrupted setting object
        corrupted.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileLock l = null;
                OutputStream os = null;
                try {
                    l = corrupted.lock();
                    os = corrupted.getOutputStream(l);
                    FileUtil.copy(valid.getInputStream(), os);
                    os.flush();
                } finally {
                    if (os != null) try { os.close(); } catch (IOException ex) {}
                    if (l != null) l.releaseLock();
                }
            }
        });
        
        ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        assertNotNull("Missing InstanceCookie", ic);
        assertNotNull("the persisted object cannot be read", ic.instanceCreate());
    }
    
    final class SaverImpl implements Saver {
        static final int NOT_CHANGED = 0;
        static final int DIRTY = 1;
        static final int SAVE = 2;
        int state = NOT_CHANGED;
        public void markDirty() {
            state = DIRTY;
        }
        public void requestSave() throws java.io.IOException {
            state = SAVE;
        }
    }
    
    
}
