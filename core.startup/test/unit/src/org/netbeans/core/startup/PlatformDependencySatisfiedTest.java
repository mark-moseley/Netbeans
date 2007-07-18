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
import org.openide.util.Utilities;

/** Checks whether a module with generated
 * @author Jaroslav Tulach
 */
public class PlatformDependencySatisfiedTest extends SetupHid {
    private File moduleJarFile;

    public PlatformDependencySatisfiedTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        Main.getModuleSystem (); // init module system
        
        File tmp = File.createTempFile ("PlatformDependencySatisfiedModule", ".jar");
        moduleJarFile = tmp;

        // clean the operatingSystem field
        java.lang.reflect.Field f;
        f = org.openide.util.Utilities.class.getDeclaredField("operatingSystem");
        f.setAccessible(true);
        f.set(null, new Integer(-1));
    }
    
    public void testWindows2000() throws Exception {
        System.setProperty("os.name", "Windows 2000");
        assertTrue("We are on windows", org.openide.util.Utilities.isWindows());
        
        assertEnableModule("org.openide.modules.os.Windows", true);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", false);
        assertEnableModule("org.openide.modules.os.PlainUnix", false);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
    }
    
    public void testMacOSX() throws Exception {
        System.setProperty("os.name", "Mac OS X");
        assertTrue("We are on mac", (org.openide.util.Utilities.getOperatingSystem() & org.openide.util.Utilities.OS_MAC) != 0);
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", true);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", false);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
    }

    public void testDarwin() throws Exception {
        System.setProperty("os.name", "Darwin");
        assertTrue("We are on mac", (org.openide.util.Utilities.getOperatingSystem() & org.openide.util.Utilities.OS_MAC) != 0);
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", true);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", false);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
    }
    
    public void testLinux() throws Exception {
        System.setProperty("os.name", "Fedora Linux");
        assertTrue("We are on linux", (org.openide.util.Utilities.getOperatingSystem() & org.openide.util.Utilities.OS_LINUX) != 0);
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", true);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
        assertEnableModule("org.openide.modules.os.Linux", true);
        assertEnableModule("org.openide.modules.os.Solaris", false);
    }

    public void testSolaris() throws Exception {
        System.setProperty("os.name", "SunOS");
        assertTrue("We are on Solaris", (Utilities.getOperatingSystem() & Utilities.OS_SOLARIS) != 0);
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", true);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
        assertEnableModule("org.openide.modules.os.Linux", false);
        assertEnableModule("org.openide.modules.os.Solaris", true);
    }

    public void testBSD() throws Exception {
        System.setProperty("os.name", "FreeBSD X1.4");
        assertTrue("We are on unix", org.openide.util.Utilities.isUnix());
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", true);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
    }

    public void testOS2() throws Exception {
        System.setProperty("os.name", "OS/2");
        assertEquals ("We are on os/2", org.openide.util.Utilities.OS_OS2, org.openide.util.Utilities.getOperatingSystem());
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", false);
        assertEnableModule("org.openide.modules.os.PlainUnix", false);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", true);
    }
    
    /**  */
    private void assertEnableModule(String req, boolean enable) throws Exception {
        Manifest man = new Manifest ();
        man.getMainAttributes ().putValue ("Manifest-Version", "1.0");
        man.getMainAttributes ().putValue ("OpenIDE-Module", "org.test.PlatformDependency/1");
        man.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");
        
        man.getMainAttributes ().putValue ("OpenIDE-Module-Requires", req);
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (moduleJarFile), man);
        os.putNextEntry (new JarEntry ("empty/test.txt"));
        os.close ();
        
        
        final FakeEvents ev = new FakeEvents();
        org.netbeans.core.startup.NbInstaller installer = new org.netbeans.core.startup.NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        ModuleFormatSatisfiedTest.addOpenideModules(mgr);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(moduleJarFile, null, false, false, false);
            
            
            if (enable) {
                assertEquals(Collections.EMPTY_SET, m1.getProblems());
                mgr.enable(m1);
                mgr.disable(m1);
            } else {
                assertFalse("We should not be able to enable the module", m1.getProblems().isEmpty());
            }
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
