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

import org.netbeans.junit.*;
import junit.textui.TestRunner;

import java.io.File;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.NbTopManager;
import org.netbeans.core.startup.ModuleHistory;
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

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest3 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest3(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        //System.setProperty("org.netbeans.core.modules", "0");
        TestRunner.run(new NbTestSuite(InstanceDataObjectModuleTest3.class));
    }
    
    public void testReloadChangesInstance() throws Exception {
        twiddle(m1, TWIDDLE_ENABLE);
        try {
            DataObject obj1 = findIt("Services/Misc/inst-1.instance");
            InstanceCookie inst1 = (InstanceCookie)obj1.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance", inst1);
            Action a1 = (Action)inst1.instanceCreate();
            twiddle(m1, TWIDDLE_RELOAD);
            // Make sure the changes take effect?
            Thread.sleep(2000);
            DataObject obj2 = findIt("Services/Misc/inst-1.instance");
            //System.err.println("obj1 == obj2: " + (obj1 == obj2)); // OK either way
            InstanceCookie inst2 = (InstanceCookie)obj2.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance", inst2);
            assertTrue("InstanceCookie changed", inst1 != inst2);
            Action a2 = (Action)inst2.instanceCreate();
            assertTrue("Action changed", a1 != a2);
            assertTrue("Correct action", "SomeAction".equals(a2.getValue(Action.NAME)));
            assertTrue("Old obj invalid or has no instance",
                !obj1.isValid() || obj1.getCookie(InstanceCookie.class) == null);
        } finally {
            twiddle(m1, TWIDDLE_DISABLE);
        }
    }
    
}
