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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class SessionGeneratorTest extends TestBase {
    
    public SessionGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateJavaEE14() throws IOException {
        TestModule testModule = createEjb21Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE14");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE14");

        // Stateless EJB in Java EE 1.4
        
        SessionGenerator sessionGenerator = new SessionGenerator("TestStatelessLR", packageFileObject, true, true, Session.SESSION_TYPE_STATELESS, false, false, true, true);
        sessionGenerator.generate();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Session session = (Session) enterpriseBeans.findBeanByName(EnterpriseBeans.SESSION, Session.EJB_NAME, "TestStatelessLRBean");

        assertNotNull(session);
        assertEquals("TestStatelessLRSB", session.getDefaultDisplayName());
        assertEquals("TestStatelessLRBean", session.getEjbName());
        assertEquals("testGenerateJavaEE14.TestStatelessLRRemoteHome", session.getHome());
        assertEquals("testGenerateJavaEE14.TestStatelessLRRemote", session.getRemote());
        assertEquals("testGenerateJavaEE14.TestStatelessLRLocalHome", session.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestStatelessLRLocal", session.getLocal());
        assertEquals("testGenerateJavaEE14.TestStatelessLRBean", session.getEjbClass());
        assertEquals("Stateless", session.getSessionType());
        assertEquals("Container", session.getTransactionType());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRRemote.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRRemoteHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRRemoteHome.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Stateful EJB in Java EE 1.4
        
        sessionGenerator = new SessionGenerator("TestStatefulLR", packageFileObject, false, true, Session.SESSION_TYPE_STATEFUL, false, false, true, true);
        sessionGenerator.generate();
        session = (Session) enterpriseBeans.findBeanByName(EnterpriseBeans.SESSION, Session.EJB_NAME, "TestStatefulLRBean");

        assertNotNull(session);
        assertEquals("TestStatefulLRSB", session.getDefaultDisplayName());
        assertEquals("TestStatefulLRBean", session.getEjbName());
        assertNull(session.getHome());
        assertNull(session.getRemote());
        assertEquals("testGenerateJavaEE14.TestStatefulLRLocalHome", session.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestStatefulLRLocal", session.getLocal());
        assertEquals("testGenerateJavaEE14.TestStatefulLRBean", session.getEjbClass());
        assertEquals("Stateful", session.getSessionType());
        assertEquals("Container", session.getTransactionType());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLRBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLRBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLRLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLRLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLRLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLRLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestStatefulLRRemote.java"));
        assertNull(packageFileObject.getFileObject("TestStatefulLRRemoteHome.java"));
    }
    
    public void testGenerateJavaEE50() throws IOException {
        TestModule testModule = createEjb30Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE50");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE50");

        // Stateless EJB in Java EE 5.0 defined in annotations
        
        SessionGenerator sessionGenerator = new SessionGenerator("TestStateless", packageFileObject, true, true, Session.SESSION_TYPE_STATELESS, true, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessBean.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatelessBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLocal.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatelessLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessRemote.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatelessRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Stateful EJB in Java EE 5.0 defined in annotations
        
        sessionGenerator = new SessionGenerator("TestStateful", packageFileObject, true, false, Session.SESSION_TYPE_STATEFUL, true, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulBean.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatefulBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulRemote.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatefulRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestStatefulLocal.java"));
    }

    public void testGenerateJavaEE60() throws IOException {
        TestModule testModule = createEjb31Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE60");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE60");

        // Stateless EJB in Java EE 6.0 defined in annotations

        SessionGenerator sessionGenerator = new SessionGenerator("TestStateless", packageFileObject, true, true, Session.SESSION_TYPE_STATELESS, true, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessBean.java")),
                getGoldenFile("testGenerateJavaEE60/TestStatelessBean.java"),
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLocal.java")),
                getGoldenFile("testGenerateJavaEE60/TestStatelessLocal.java"),
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessRemote.java")),
                getGoldenFile("testGenerateJavaEE60/TestStatelessRemote.java"),
                FileUtil.toFile(packageFileObject)
                );

        // Stateful EJB in Java EE 6.0 defined in annotations

        sessionGenerator = new SessionGenerator("TestStateful", packageFileObject, true, false, Session.SESSION_TYPE_STATEFUL, true, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulBean.java")),
                getGoldenFile("testGenerateJavaEE60/TestStatefulBean.java"),
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulRemote.java")),
                getGoldenFile("testGenerateJavaEE60/TestStatefulRemote.java"),
                FileUtil.toFile(packageFileObject)
                );

        assertNull(packageFileObject.getFileObject("TestStatefulLocal.java"));

        // Singleton EJB in Java EE 6.0 defined in annotations

        sessionGenerator = new SessionGenerator("TestSingleton", packageFileObject, false, false, Session.SESSION_TYPE_SINGLETON, true, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestSingletonBean.java")),
                getGoldenFile("testGenerateJavaEE60/TestSingletonBean.java"),
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestSingletonLocal.java"));
        assertNull(packageFileObject.getFileObject("TestSingletonRemote.java"));

    }

}
