/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.lookup;

import org.netbeans.core.LoaderPoolNode;
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
public class InstanceDataObjectModuleTest4 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest4(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        //System.setProperty("org.netbeans.core.modules", "0");
        TestRunner.run(new NbTestSuite(InstanceDataObjectModuleTest4.class));
    }
    
    /** Currently fails (lookup gets a result not assignable to its template),
     * probably because this is not supported with *.instance (?).
     */
    public void testReloadDotInstanceSwitchesLookupByNewClass() throws Exception {
        twiddle(m1, TWIDDLE_ENABLE);
        try {
            ClassLoader l1 = m1.getClassLoader();
            Class c1 = l1.loadClass("test1.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1));
            ERR.log("Before reload");
            twiddle(m1, TWIDDLE_RELOAD);
            ERR.log("After reload");
            // Sleeping for a few seconds here does *not* help.
            ClassLoader l2 = m1.getClassLoader();
            assertTrue("ClassLoader really changed", l1 != l2);
            Class c2 = l2.loadClass("test1.SomeAction");
            assertTrue("Class really changed", c1 != c2);
            
            LoaderPoolNode.waitFinished();
            ERR.log("After waitFinished");
            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1));
            assertTrue("SomeAction<2> instance found after module reload",
                existsSomeAction(c2));
        } finally {
            ERR.log("Before disable");
            twiddle(m1, TWIDDLE_DISABLE);
        }
    }
    
    /** Though this works in test #5, seems to get "poisoned" here by running
     * in the same VM as the previous test.
     */
    public void testReloadSettingsSwitchesLookupByNewClass() throws Exception {
        assertTrue("There is initially nothing in lookup",
            !existsSomeAction(Action.class));
        twiddle(m2, TWIDDLE_ENABLE);
        try {
            ClassLoader l1 = m2.getClassLoader();
            Class c1 = l1.loadClass("test2.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1));
            
            ERR.log("Before reload");
            twiddle(m2, TWIDDLE_RELOAD);
            ERR.log("After reload");
            ClassLoader l2 = m2.getClassLoader();
            assertTrue("ClassLoader really changed", l1 != l2);
            Class c2 = l2.loadClass("test2.SomeAction");
            assertTrue("Class really changed", c1 != c2);
            // Make sure the changes take effect
            LoaderPoolNode.waitFinished();
            ERR.log("After waitFinished");
            
            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1));
            assertTrue("SomeAction<2> instance found after module reload",
                existsSomeAction(c2));
        } finally {
            ERR.log("Finally disable");
            twiddle(m2, TWIDDLE_DISABLE);
        }
    }
    
}
