/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.lookup;

import javax.swing.Action;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest7 extends InstanceDataObjectModuleTestHid 
implements FileChangeListener {
    private boolean instanceSaved;

    public InstanceDataObjectModuleTest7(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        Repository.getDefault ().getDefaultFileSystem ().addFileChangeListener (this);
        assertNotNull("have org-netbeans-modules-settings.jar in CP", InstanceDataObjectModuleTest7.class.getResource("/org/netbeans/modules/settings/resources/Bundle.properties"));
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        Repository.getDefault ().getDefaultFileSystem ().removeFileChangeListener (this);
        super.tearDown ();
    }
    
    public void testFixedSettingsChangeInstanceAfterSlowReload() throws Exception {
        twiddle(m2, TWIDDLE_ENABLE);
        assertTrue ("m2 is enabled", m2.isEnabled ());
        DataObject obj1;
            obj1 = findIt("Services/Misc/inst-2.settings");
            assertEquals("No saved state for inst-2.settings", null, FileUtil.toFile(obj1.getPrimaryFile()));
            org.openide.ErrorManager.getDefault ().log ("BEFORE THE COOKIE QUERY");
            InstanceCookie inst1 = (InstanceCookie)obj1.getCookie(InstanceCookie.class);
            org.openide.ErrorManager.getDefault ().log ("AFTER THE COOKIE QUERY");
            {
                int debug = 5;
                while (inst1 == null && debug-- > 0) {
                    Thread.sleep (300);
                    inst1 = (InstanceCookie)obj1.getCookie(InstanceCookie.class);
                    org.openide.ErrorManager.getDefault ().log ("  SLEEP[300ms]: " + inst1);
                }
            }
            assertNotNull("Had an instance from " + obj1, inst1);
            Action a1 = (Action)inst1.instanceCreate();
            assertTrue("Old version of action", a1.isEnabled());
            // Make some change which should cause it to be written to disk:
            synchronized (this) {
                a1.setEnabled(false);
                // Cf. InstanceDataObject.SettingsInstance.SAVE_DELAY = 2000:
                wait (60000);
                assertTrue ("Really was saved", instanceSaved);
            }
            twiddle(m2, TWIDDLE_DISABLE);
            // Just in case it is needed:
            Thread.sleep(1000);
            
            assertTrue ("Data object is still valid", obj1.isValid ());

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
            assertSameDataObject ("same data object", obj1, obj2);
            InstanceCookie inst2 = (InstanceCookie)obj2.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance", inst2);
            assertTrue("InstanceCookie changed", inst1 != inst2);
            Action a2 = (Action)inst2.instanceCreate();
            assertTrue("Action changed", a1 != a2);
            assertTrue("Correct action", "SomeAction".equals(a2.getValue(Action.NAME)));
            assertTrue("New version of action", !a2.isEnabled());
            assertTrue("module still enabled", m2.isEnabled());
            twiddle(m2, TWIDDLE_DISABLE);
        // Now make sure it has no cookie.
        Thread.sleep(1000);
        DataObject obj3 = findIt("Services/Misc/inst-2.settings");
        assertSameDataObject ("same data object3", obj1, obj3);
        InstanceCookie inst3 = (InstanceCookie)obj3.getCookie(InstanceCookie.class);
        assertNull("Had instance", inst3);
    }
    
    public void fileAttributeChanged(FileAttributeEvent fe) {}
    
    public synchronized void fileChanged(FileEvent fe) {
        if ("inst-2.settings".equals (fe.getFile ().getNameExt ())) {
            instanceSaved = true;
            notifyAll ();
        }
    }
    
    public void fileDataCreated(FileEvent fe) {}
    
    public void fileDeleted(FileEvent fe) {
        if ("inst-2.settings".equals (fe.getFile ().getNameExt ())) {
            FileObject isThere = Repository.getDefault ().getDefaultFileSystem ().findResource (fe.getFile ().getPath ());
            
            fail ("File " + fe.getFile () + " should not be deleted as this will discard the data object. Moreover it is expected that similar file is still there: " + isThere);
        }
    }
    
    public void fileFolderCreated(FileEvent fe) {}
    
    public void fileRenamed(FileRenameEvent fe) {}
    
}
