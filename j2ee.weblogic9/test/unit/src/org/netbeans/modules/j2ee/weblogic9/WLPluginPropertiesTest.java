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

package org.netbeans.modules.j2ee.weblogic9;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author sherold
 */
public class WLPluginPropertiesTest extends NbTestCase {
    
    public WLPluginPropertiesTest(String testName) {
        super(testName);
    }
    
    public void testIsSupportedVersion() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "server/lib");
        libFolder.mkdirs();
        File file = new File(libFolder, "weblogic.jar");
        createJar(file, "Implementation-Version: 10.0.0.0");
        assertTrue(WLPluginProperties.isSupportedVersion(baseFolder));
        createJar(file, "Implementation-Version: 9.0.0.0");
        assertTrue(WLPluginProperties.isSupportedVersion(baseFolder));
        createJar(file, "Implementation-Version: 8.0.0.0");
        assertFalse(WLPluginProperties.isSupportedVersion(baseFolder));
        createJar(file, "Missing-Implementation-Version: 10.0.0.0");
        assertFalse(WLPluginProperties.isSupportedVersion(baseFolder));
    }
    
    private void createJar(File file, String manifestLine) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Manifest-Version: 1.0\n");
        stringBuilder.append(manifestLine + "\n");
        InputStream is = new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"));
        try {
            new JarOutputStream(new FileOutputStream(file), new Manifest(is)).close();
        } finally {
            is.close();
        }
    }

}
