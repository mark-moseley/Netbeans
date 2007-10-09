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

/*
 * J2MEProjectModuleInstallTest.java
 * JUnit based test
 *
 * Created on 06 February 2006, 18:55
 */
package org.netbeans.modules.mobility.project;

import java.io.IOException;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 * @author lukas
 */
public class J2MEProjectModuleInstallTest extends NbTestCase {
    
    public J2MEProjectModuleInstallTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(J2MEProjectModuleInstallTest.class);
        
        return suite;
    }
    
    /**
     * Test of restored method, of class org.netbeans.modules.mobility.project.J2MEProjectModuleInstall.
     */
    public void testRestored() throws IOException {
        System.out.println("restored");
        
        System.setProperty("netbeans.user","test/tiredTester");
        J2MEProjectModuleInstall instance = new J2MEProjectModuleInstall();
        
        EditableProperties pr1=PropertyUtils.getGlobalProperties();
        instance.restored();
        EditableProperties pr2=PropertyUtils.getGlobalProperties();
        assertEquals(pr1,pr2);
    }
}
