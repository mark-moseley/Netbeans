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

/** Checks whether a modules are provided with ModuleFormat1 token.
 *
 * @author Jaroslav Tulach
 */
public class ModuleFormatSatisfiedTest extends SetupHid {
    private File moduleJarFile;

    public ModuleFormatSatisfiedTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        
        File tmp = File.createTempFile ("ModuleFormatTest", ".jar");
        moduleJarFile = tmp;
        
        Manifest man = new Manifest ();
        man.getMainAttributes ().putValue ("Manifest-Version", "1.0");
        man.getMainAttributes ().putValue ("OpenIDE-Module", "org.test.FormatDependency/1");
        
        
        String req = "org.openide.modules.ModuleFormat1";
        
        man.getMainAttributes ().putValue ("OpenIDE-Module-Requires", req);
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (tmp), man);
        os.putNextEntry (new JarEntry ("empty/test.txt"));
        os.close ();
    }
    
    /**  */
    public void testTryToInstallTheModule () throws Exception {
        Main.getModuleSystem (); // init module system
        
        
        
        
        final FakeEvents ev = new FakeEvents();
        org.netbeans.core.startup.NbInstaller installer = new org.netbeans.core.startup.NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            addOpenideModules(mgr);
            Module m1 = mgr.create(moduleJarFile, null, false, false, false);
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            mgr.enable(m1);
            mgr.disable(m1);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    
    static void addOpenideModules (ModuleManager mgr) throws Exception {
        ClassLoader l = SetupHid.class.getClassLoader();
        String openide =
"Manifest-Version: 1.0\n" +
"OpenIDE-Module: org.openide.modules\n" +
"OpenIDE-Module-Localizing-Bundle: org/openide/modules/Bundle.properties\n" +
"Specification-Title: NetBeans\n" +
"OpenIDE-Module-Specification-Version: 6.2\n" +
"\n" +
"Name: /org/openide/modules/\n" +
"Package-Title: org.openide.modules\n";
               
        Manifest mani = new Manifest (new java.io.ByteArrayInputStream (openide.getBytes ()));
        mgr.enable(mgr.createFixed(mani, null, l));
     }

    
}
