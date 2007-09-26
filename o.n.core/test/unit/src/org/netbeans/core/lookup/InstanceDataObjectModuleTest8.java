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

package org.netbeans.core.lookup;

import javax.swing.Action;
import org.netbeans.core.LoaderPoolNode;
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
public class InstanceDataObjectModuleTest8 extends InstanceDataObjectModuleTestHid
implements FileChangeListener {
    
    /*
    static {
        // Turn on verbose logging while developing tests:
        System.setProperty("org.netbeans.core.modules", "0");
    }
     */
    
    private boolean instanceSaved;

    public InstanceDataObjectModuleTest8(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        Repository.getDefault ().getDefaultFileSystem ().addFileChangeListener (this);
        assertNotNull("have org-netbeans-modules-settings.jar in CP", InstanceDataObjectModuleTest7.class.getResource("/org/netbeans/modules/settings/resources/Bundle.properties"));
        super.setUp();
    }
     
    protected void tearDown () throws java.lang.Exception {
        Repository.getDefault ().getDefaultFileSystem ().removeFileChangeListener (this);
        super.tearDown ();
    }
    
    /** Currently fails.
     * Same as #7, but reloading is done quickly (one write mutex, no pause).
     */
    public void testFixedSettingsChangeInstanceAfterFastReload() throws Exception {
        twiddle(m2, TWIDDLE_ENABLE);
        DataObject obj1;
        try {
            obj1 = findIt("Services/Misc/inst-8.settings");
            ERR.log("Found obj1: " + obj1);
            assertEquals("No saved state for inst-8.settings", null, FileUtil.toFile(obj1.getPrimaryFile()));
            InstanceCookie inst1 = (InstanceCookie)obj1.getCookie(InstanceCookie.class);
            ERR.log("There is a cookie: " + inst1);
            assertNotNull("Had an instance", inst1);
            Action a1 = (Action)inst1.instanceCreate();
            assertEquals("Correct action class", "test2.SomeAction", a1.getClass().getName());
            assertTrue("Old version of action", a1.isEnabled());
            
            ERR.log("Action created" + a1);
            
            // Make some change which should cause it to be written to disk:
            synchronized (this) {
                ERR.log("In sync block");
                a1.setEnabled(false);
                ERR.log("setEnabled(false)");
                // Cf. InstanceDataObject.SettingsInstance.SAVE_DELAY = 2000:
                ERR.log("Waiting");
                wait (60000);
                ERR.log("Waiting done");
                assertTrue ("Really was saved", instanceSaved);
            }
            /*
            File saved = new File(new File(new File(systemDir, "Services"), "Misc"), "inst-8.settings");
            assertTrue("Wrote to disk: " + saved, saved.isFile());
             */
            /*
            File saved = FileUtil.toFile(obj1.getPrimaryFile());
            assertNotNull("Wrote to disk; expecting: " + new File(new File(new File(systemDir, "Services"), "Misc"), "inst-8.settings"),
                saved);
             */
            ERR.log("Twidle reload");
            twiddle(m2, TWIDDLE_RELOAD);
            ERR.log("TWIDDLE_RELOAD done");
            LoaderPoolNode.waitFinished();
            ERR.log("pool refeshed");
            DataObject obj2 = findIt("Services/Misc/inst-8.settings");
            ERR.log("Data object for inst-8: " + obj2);
            assertSameDataObject ("same data object", obj1, obj2);
            InstanceCookie inst2 = (InstanceCookie)obj2.getCookie(InstanceCookie.class);
            ERR.log("Cookie from the object: " + inst2);
            assertNotNull("Had an instance", inst2);
            assertTrue("InstanceCookie changed", inst1 != inst2);
            Action a2 = (Action)inst2.instanceCreate();
            ERR.log("Action2 created: " + a2);
            assertTrue("Action changed", a1 != a2);
            assertTrue("Correct action", "SomeAction".equals(a2.getValue(Action.NAME)));
            assertTrue("New version of action", !a2.isEnabled());
        } finally {
            ERR.log("Final disable");
            twiddle(m2, TWIDDLE_DISABLE);
            ERR.log("Final disable done");
        }
        // Now make sure it has no cookie.
        LoaderPoolNode.waitFinished();
        ERR.log("loader pool node refreshed");
        DataObject obj3 = findIt("Services/Misc/inst-8.settings");
        ERR.log("Third data object: " + obj3);
        assertSameDataObject ("same data object2", obj1, obj3);
        InstanceCookie inst3 = (InstanceCookie)obj3.getCookie(InstanceCookie.class);
        ERR.log("Cookie is here: " + inst3);
        assertNull("Had instance", inst3);
    }
    
    
    public void fileAttributeChanged(FileAttributeEvent fe) {}
    
    public synchronized void fileChanged(FileEvent fe) {
        if ("inst-8.settings".equals (fe.getFile ().getNameExt ())) {
            instanceSaved = true;
            notifyAll ();
        }
    }
    
    public void fileDataCreated(FileEvent fe) {}
    
    public void fileDeleted(FileEvent fe) {
        if ("inst-8.settings".equals (fe.getFile ().getNameExt ())) {
            FileObject isThere = Repository.getDefault ().getDefaultFileSystem ().findResource (fe.getFile ().getPath ());
            fail ("File " + fe.getFile () + " should not be deleted as this will discard the data object. Moreover it is expected that similar file is still there: " + isThere);
        }
    }
    
    public void fileFolderCreated(FileEvent fe) {}
    
    public void fileRenamed(FileRenameEvent fe) {}
    
}
