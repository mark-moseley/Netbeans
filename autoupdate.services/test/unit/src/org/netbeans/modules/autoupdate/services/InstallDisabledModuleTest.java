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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallDisabledModuleTest extends OperationsTestImpl {

    public InstallDisabledModuleTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();        
        System.setProperty("netbeans.dirs", getWorkDirPath());
        
        final String fn = moduleCodeNameBaseForTest().replace('.', '-') + ".xml";
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "Modules/" + fn);
                OutputStream os = fo.getOutputStream();
                String cfg = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                        "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN' 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>\n" +
                        "<module name='com.sun.testmodule.cluster'>\n" +
                        "   <param name='autoload'>false</param>\n" +
                        "   <param name='eager'>false</param>\n" +
                        "   <param name='enabled'>false</param>\n" +
                        "   <param name='jar'>modules/com-sun-testmodule-cluster.jar</param>\n" +
                        "   <param name='reloadable'>false</param>\n" +
                        "   <param name='specversion'>1.0</param>\n" +
                        "</module>\n" +
                        "\n";
                os.write(cfg.getBytes("UTF-8"));
                os.close();
            }
        });

        assertNotNull("File exists", FileUtil.getConfigFile("Modules/" + fn));
    }

    @Override
    boolean incrementNumberOfModuleConfigFiles() {
        return false;
    }

    protected String moduleCodeNameBaseForTest() {
        return "com.sun.testmodule.cluster"; //NOI18N
    }

    public void testSelf() throws Exception {
        UpdateUnit install = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull("There is an NBM to install", install);
        installModule(install, null);
    }
}
