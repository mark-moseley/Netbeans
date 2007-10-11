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

package org.netbeans.modules.j2ee.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sherold
 */
public class UtilTest extends NbTestCase {
    
    /** Creates a new instance of UtilTest */
    public UtilTest(String testName) {
        super(testName);
    }
    
    
    public void testContainsClass() throws IOException {
        File dataDir = getDataDir();
        File[] classpath1 = new File[] { 
            new File(dataDir, "testcp/libs/org.netbeans.nondriver.jar"),
            new File(dataDir, "testcp/libs/org.netbeans.test.dbdriver.jar") 
        }; 
        File[] classpath2 = new File[] { 
            new File(dataDir, "testcp/libs/org.netbeans.nondriver.jar"),
            new File(dataDir, "testcp/classes") ,
            new File(dataDir, "testcp/shared/classes"), 
        };
        
        List<URL> urlClasspath1 = new LinkedList<URL>();
        urlClasspath1.add(FileUtil.getArchiveRoot(classpath1[0].toURL()));
        urlClasspath1.add(FileUtil.getArchiveRoot(classpath1[1].toURL()));
        
        List<URL> urlClasspath2 = new LinkedList<URL>();
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[0].toURL()));
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[1].toURL()));
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[2].toURL()));
        
        
        assertFalse(Util.containsClass(Arrays.asList(classpath1), "com.mysql.Driver"));
        assertFalse(Util.containsClass(Arrays.asList(classpath2), "com.mysql.Driver"));
        assertFalse(Util.containsClass(urlClasspath1, "com.mysql.Driver"));
        assertFalse(Util.containsClass(urlClasspath2, "com.mysql.Driver"));
        
        // the driver is in the jar file
        assertTrue(Util.containsClass(Arrays.asList(classpath1), "org.netbeans.test.db.driver.TestDriver"));
        assertTrue(Util.containsClass(urlClasspath1, "org.netbeans.test.db.driver.TestDriver"));
        // the driver is among the classes
        assertTrue(Util.containsClass(Arrays.asList(classpath2), "org.netbeans.test.db.driver.TestDriver"));
        assertTrue(Util.containsClass(urlClasspath2, "org.netbeans.test.db.driver.TestDriver"));
    } 
    
    public void testGetJ2eeSpecificationLabel() {
        assertNotNull(Util.getJ2eeSpecificationLabel(J2eeModule.J2EE_13));
        assertNotNull(Util.getJ2eeSpecificationLabel(J2eeModule.J2EE_14));
        assertNotNull(Util.getJ2eeSpecificationLabel(J2eeModule.JAVA_EE_5));
        
        try {
            Util.getJ2eeSpecificationLabel("nothing");
            fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            //expected
        }
                     
        try {
            Util.getJ2eeSpecificationLabel(null);
            fail("Expected exception not thrown");
        } catch (NullPointerException ex) {
            //expected
        }
    }
}
