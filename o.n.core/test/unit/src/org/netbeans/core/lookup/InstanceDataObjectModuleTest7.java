/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.lookup;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

import java.io.File;
import org.netbeans.core.modules.Module;
import org.netbeans.core.modules.ModuleManager;
import org.netbeans.core.NbTopManager;
import org.netbeans.core.modules.ModuleHistory;
import org.openide.util.Lookup;
import javax.swing.Action;
import java.util.Iterator;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Mutex;
import org.openide.cookies.InstanceCookie;
import org.openide.util.MutexException;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import java.io.IOException;
import java.util.Properties;
import org.openide.filesystems.FileUtil;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest7 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest7(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        //System.setProperty("org.netbeans.core.modules", "0");
        TestRunner.run(new NbTestSuite(InstanceDataObjectModuleTest7.class));
    }
    
    protected void setUp() throws Exception {
        // Use MemoryFileSystem:
        Properties p = System.getProperties();
        p.remove("system.dir");
        System.setProperties(p);
        super.setUp();
    }
    
    public void testFixedSettingsChangeInstanceAfterSlowReload() throws Exception {
        twiddle(m2, TWIDDLE_ENABLE);
        DataObject obj1;
        try {
            obj1 = findIt("Services/Misc/inst-2.settings");
            assertEquals("No saved state for inst-2.settings", null, FileUtil.toFile(obj1.getPrimaryFile()));
            InstanceCookie inst1 = (InstanceCookie)obj1.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance from " + obj1, inst1);
            Action a1 = (Action)inst1.instanceCreate();
            assertTrue("Old version of action", a1.isEnabled());
            // Make some change which should cause it to be written to disk:
            a1.setEnabled(false);
            // Cf. InstanceDataObject.SettingsInstance.SAVE_DELAY = 2000:
            Thread.sleep(3000);
            /*
            File saved = new File(new File(new File(systemDir, "Services"), "Misc"), "inst-2.settings");
            assertTrue("Wrote to disk: " + saved, saved.isFile());
             */
            /*
            File saved = FileUtil.toFile(obj1.getPrimaryFile());
            assertNotNull("Wrote to disk; expecting: " + new File(new File(new File(systemDir, "Services"), "Misc"), "inst-2.settings"),
                saved);
             */
            twiddle(m2, TWIDDLE_DISABLE);
            // Just in case it is needed:
            Thread.sleep(1000);

            // Yarda's patch:
            InstanceCookie.Of notExists = (InstanceCookie.Of)obj1.getCookie (InstanceCookie.class);
            if (notExists != null && notExists.instanceOf(Action.class)) {
                fail ("Module is disabled, so " + obj1 + " should have no instance cookie " + notExists + " with " + notExists.instanceClass());
            }
            // it is OK for there to be an instance of BrokenSettings...

            twiddle(m2, TWIDDLE_ENABLE);
            // Make sure there is time for changes to take effect:
            Thread.sleep(2000);
            DataObject obj2 = findIt("Services/Misc/inst-2.settings");
            assertSame ("same data object", obj1, obj2);
            InstanceCookie inst2 = (InstanceCookie)obj2.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance", inst2);
            assertTrue("InstanceCookie changed", inst1 != inst2);
            Action a2 = (Action)inst2.instanceCreate();
            assertTrue("Action changed", a1 != a2);
            assertTrue("Correct action", "SomeAction".equals(a2.getValue(Action.NAME)));
            assertTrue("New version of action", !a2.isEnabled());
        } finally {
            if (m2.isEnabled()) {
                twiddle(m2, TWIDDLE_DISABLE);
            }
        }
        // Now make sure it has no cookie.
        Thread.sleep(1000);
        DataObject obj3 = findIt("Services/Misc/inst-2.settings");
        assertTrue("same data object", (obj1 == obj3));
        InstanceCookie inst3 = (InstanceCookie)obj3.getCookie(InstanceCookie.class);
        assertNull("Had instance", inst3);
    }
    
}
