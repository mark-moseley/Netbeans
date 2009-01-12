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

package org.netbeans.core.startup;

import java.io.File;
import java.util.Collections;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.openide.filesystems.FileUtil;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest5 extends SetupHid {

    public NbInstallerTest5(String name) {
        super(name);
    }

    /** Test #21173/#23609: overriding layers by module dependencies.
     * Version 2: modules loaded piece by piece.
     * Exercises different logic in XMLFileSystem as well as ModuleLayeredFileSystem.
     */
    public void testDependencyLayerOverrides2() throws Exception {
        Main.getModuleSystem (); // init module system
        System.err.println("Module Info->"+org.openide.util.Lookup.getDefault()
                .lookup(org.openide.modules.ModuleInfo.class)); // TEMP
        final FakeEvents ev = new FakeEvents();
        org.netbeans.core.startup.NbInstaller installer = new org.netbeans.core.startup.NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "override-layer-mod.jar"), null, false, false, false);
            
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            assertEquals(null, slurp("foo/file1.txt"));
            assertEquals(null, slurp("foo/file3.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            mgr.enable(m1);
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals("base contents", slurp("foo/file3.txt"));
            assertEquals("base contents", slurp("foo/file4.txt"));
            assertEquals("someval", FileUtil.getConfigFile("foo/file5.txt").getAttribute("myattr"));
            mgr.enable(m2);
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            assertEquals("customized contents", slurp("foo/file3.txt"));
            assertEquals("someotherval", FileUtil.getConfigFile("foo/file5.txt").getAttribute("myattr"));
            mgr.disable(m2);
            assertEquals("base contents", slurp("foo/file3.txt"));
            mgr.disable(m1);
            assertEquals(null, slurp("foo/file3.txt"));
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
