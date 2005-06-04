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

package org.netbeans.core.startup;

import java.io.*;
import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.jar.*;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.filesystems.Repository;

/** Checks whether a module with generated
 * @author Jaroslav Tulach
 */
public class PlatformDependencySatisfiedTest extends SetupHid {
    private File moduleJarFile;
    
    public PlatformDependencySatisfiedTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(PlatformDependencySatisfiedTest.class));
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        
        File tmp = File.createTempFile ("PlatformDependencySatisfiedModule", ".jar");
        moduleJarFile = tmp;
        
        Manifest man = new Manifest ();
        man.getMainAttributes ().putValue ("Manifest-Version", "1.0");
        man.getMainAttributes ().putValue ("OpenIDE-Module", "org.test.PlatformDependency/1");
        
        String req = null;
        if (org.openide.util.Utilities.isWindows ()) {
            req = "org.openide.modules.os.Windows";
        } else if ((org.openide.util.Utilities.getOperatingSystem () & org.openide.util.Utilities.OS_MAC) != 0) {
            req = "org.openide.modules.os.MacOSX";
        } else if (org.openide.util.Utilities.isUnix ()) {
            req = "org.openide.modules.os.Unix";
        }
        assertNotNull ("This test is supposed to run on windows, unix and macosx, fail otherwise on this line", req);
        
        man.getMainAttributes ().putValue ("OpenIDE-Module-Requires", req);
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (tmp), man);
        os.putNextEntry (new JarEntry ("empty/test.txt"));
        os.close ();
    }
    
    /**  */
    public void testTryToInstallTheModuleWhichRequiresTheOS () throws Exception {
        Main.getModuleSystem (); // init module system
        final FakeEvents ev = new FakeEvents();
        org.netbeans.core.startup.NbInstaller installer = new org.netbeans.core.startup.NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        ModuleFormatSatisfiedTest.addOpenideModules(mgr);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(moduleJarFile, null, false, false, false);
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            mgr.enable(m1);
            mgr.disable(m1);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
