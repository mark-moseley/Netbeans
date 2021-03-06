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

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;

/**
 * Test cases for {@link CommonAnnotationHelper}.
 * @author Tomas Mysik, Martin Adamek
 */
public class CommonAnnotationHelperTest extends CommonTestCase {

    public CommonAnnotationHelperTest(String testName) {
        super(testName);
    }

    public void testGetSecurityRoles() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Calculator.java",
                "package foo;" +
                "import javax.annotation.security.*;" +
                "@DeclareRoles(\"BusinessAdmin\")" +
                "public class Calculator {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/ListServlet.java",
                "package foo;" +
                "import javax.annotation.security.*;" +
                "@DeclareRoles({\"Visitor\", \"Administrator\"})" +
                "public class ListServlet {" +
                "}");
        createWebAppModel(false).runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
            public Void run(WebAppMetadata metadata) throws VersionNotSupportedException {
                checkSecurityRoles(metadata.getRoot().getSecurityRole());
                return null;
            }
        });
        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                checkSecurityRoles(metadata.getRoot().getSingleAssemblyDescriptor().getSecurityRole());
                return null;
            }
        });
    }

    private void checkSecurityRoles(SecurityRole[] securityRoles) {
        assertEquals(3, securityRoles.length);
        Set<String> rolesNames = new HashSet<String>();
        for (SecurityRole securityRole : securityRoles) {
            rolesNames.add(securityRole.getRoleName());
        }
        assertTrue(rolesNames.contains("BusinessAdmin"));
        assertTrue(rolesNames.contains("Visitor"));
        assertTrue(rolesNames.contains("Administrator"));
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>ResourceRef</tt>) for the whole classpath.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetResourceRefsOnClasspath() throws Exception {
        initClasses();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myDS", "yourDataSource"));
        createWebAppModel(false).runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
            public Void run(WebAppMetadata metadata) throws VersionNotSupportedException {
                assertResourceRefNames(resourceNames, metadata.getRoot().getResourceRef());
                return null;
            }
        });
        createAppClientModel().runReadAction(new MetadataModelAction<AppClientMetadata, Void>() {
            public Void run(AppClientMetadata metadata) throws Exception {
                assertResourceRefNames(resourceNames, metadata.getRoot().getResourceRef());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>ResourceRef</tt>) for one class.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetResourceRefsInClass() throws Exception {
        initClass();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myDS", "yourDataSource"));

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                MessageDriven messageDriven = (MessageDriven) getEjbByEjbName(
                        metadata.getRoot().getEnterpriseBeans().getMessageDriven(), "CustomerMDB");
                assertResourceRefNames(resourceNames, messageDriven.getResourceRef());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>ResourceEnvRef</tt>) for the whole classpath.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetResourceEnvRefsOnClasspath() throws Exception {
        initClasses();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myInteractionSpec", "yourClass"));
        createWebAppModel(false).runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
            public Void run(WebAppMetadata metadata) throws VersionNotSupportedException {
                assertResourceEnvRefNames(resourceNames, metadata.getRoot().getResourceEnvRef());
                return null;
            }
        });
        createAppClientModel().runReadAction(new MetadataModelAction<AppClientMetadata, Void>() {
            public Void run(AppClientMetadata metadata) throws Exception {
                assertResourceEnvRefNames(resourceNames, metadata.getRoot().getResourceEnvRef());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>ResourceEnvRef</tt>) for one class.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetResourceEnvRefsInClass() throws Exception {
        initClass();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myTransaction"));

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                MessageDriven messageDriven = (MessageDriven) getEjbByEjbName(
                        metadata.getRoot().getEnterpriseBeans().getMessageDriven(), "CustomerMDB");
                assertResourceEnvRefNames(resourceNames, messageDriven.getResourceEnvRef());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>EnvEntry</tt>) for the whole classpath.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetEnvEntriesOnClasspath() throws Exception {
        initClasses();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myString", "yourLong"));
        createWebAppModel(false).runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
            public Void run(WebAppMetadata metadata) throws VersionNotSupportedException {
                assertEnvEntryNames(resourceNames, metadata.getRoot().getEnvEntry());
                return null;
            }
        });
        createAppClientModel().runReadAction(new MetadataModelAction<AppClientMetadata, Void>() {
            public Void run(AppClientMetadata metadata) throws Exception {
                assertEnvEntryNames(resourceNames, metadata.getRoot().getEnvEntry());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>EnvEntry</tt>) for one class.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetEnvEntriesInClass() throws Exception {
        initClass();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myString", "yourLong"));

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                MessageDriven messageDriven = (MessageDriven) getEjbByEjbName(
                        metadata.getRoot().getEnterpriseBeans().getMessageDriven(), "CustomerMDB");
                assertEnvEntryNames(resourceNames, messageDriven.getEnvEntry());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>MessageDestinationRef</tt>) for the whole classpath.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetMessageDestinationRefsOnClasspath() throws Exception {
        initClasses();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myQueue"));
        createWebAppModel(false).runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
            public Void run(WebAppMetadata metadata) throws VersionNotSupportedException {
                assertMessageDestinationRefNames(resourceNames, metadata.getRoot().getMessageDestinationRef());
                return null;
            }
        });
        createAppClientModel().runReadAction(new MetadataModelAction<AppClientMetadata, Void>() {
            public Void run(AppClientMetadata metadata) throws Exception {
                assertMessageDestinationRefNames(resourceNames, metadata.getRoot().getMessageDestinationRef());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>MessageDestinationRef</tt>) for one class.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetMessageDestinationRefsInClass() throws Exception {
        initClass();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myTopic"));

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                MessageDriven messageDriven = (MessageDriven) getEjbByEjbName(
                        metadata.getRoot().getEnterpriseBeans().getMessageDriven(), "CustomerMDB");
                assertMessageDestinationRefNames(resourceNames, messageDriven.getMessageDestinationRef());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>ServiceRef</tt>) for the whole classpath.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetServiceRefsOnClasspath() throws Exception {
        initClasses();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("yourService"));
        createWebAppModel(false).runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
            public Void run(WebAppMetadata metadata) throws VersionNotSupportedException {
                assertServiceRefNames(resourceNames, metadata.getRoot().getServiceRef());
                return null;
            }
        });
        createAppClientModel().runReadAction(new MetadataModelAction<AppClientMetadata, Void>() {
            public Void run(AppClientMetadata metadata) throws Exception {
                assertServiceRefNames(resourceNames, metadata.getRoot().getServiceRef());
                return null;
            }
        });
    }
    
    /**
     * Test for getting {@link javax.annotation.Resource @Resource}s (as <tt>ServiceRef</tt>) for one class.
     * @throws java.lang.Exception if any error occurs.
     */
    public void testGetServiceRefsInClass() throws Exception {
        initClass();
        final Set<String> resourceNames = new HashSet<String>(Arrays.asList("myService"));

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                MessageDriven messageDriven = (MessageDriven) getEjbByEjbName(
                        metadata.getRoot().getEnterpriseBeans().getMessageDriven(), "CustomerMDB");
                assertServiceRefNames(resourceNames, messageDriven.getServiceRef());
                return null;
            }
        });
    }
    
    private void initClasses() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/MyClass.java",
                "package foo;" +
                "" +
                "import javax.annotation.Resource;" +
                "import javax.sql.DataSource;" +
                "" +
                "public class MyClass {" +
                "   @Resource" +
                "   private DataSource myDS;" +
                "" +
                "   @Resource" +
                "   private String myString;" +
                "" +
                "   @Resource" +
                "   private void setMyInteractionSpec(javax.resource.cci.InteractionSpec interactionSpec) {" +
                "   }" +
                "" +
                "   @Resource" +
                "   private void setMyQueue(javax.jms.Queue queue) {" +
                "   }" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/YourClass.java",
                "package foo;" +
                "" +
                "import javax.annotation.Resource;" +
                "import javax.sql.DataSource;" +
                "" +
                "@Resource(name=\"yourClass\", type=javax.transaction.UserTransaction)" +
                "public class YourClass {" +
                "   @Resource" +
                "   private void setYourDataSource(DataSource ds) {" +
                "   }" +
                "" +
                "   @Resource" +
                "   private void setYourLong(Long long) {" +
                "   }" +
                "" +
                "   @Resource" +
                "   private javax.xml.rpc.Service yourService;" +
                "}");
    }
    
    private void initClass() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerMDB.java",
                "package foo;" +
                "" +
                "import javax.ejb.*;" +
                "import javax.jms.*;" +
                "import javax.annotation.Resource;" +
                "import javax.sql.DataSource;" +
                "" +
                "@MessageDriven(mappedName = \"jms/CustomerMDB\", activationConfig =  {" +
                "        @ActivationConfigProperty(propertyName = \"acknowledgeMode\", propertyValue = \"Auto-acknowledge\")," +
                "        @ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"javax.jms.Queue\")" +
                "    })" +
                "public class CustomerMDB implements MessageListener {" +
                "   public void onMessage(Message message) {" +
                "   }" +
                "" +
                "   @Resource" +
                "   private DataSource myDS;" +
                "" +
                "   @Resource" +
                "   private String myString;" +
                "" +
                "   @Resource" +
                "   private javax.transaction.UserTransaction myTransaction;" +
                "" +
                "   @Resource" +
                "   private javax.xml.rpc.Service myService;" +
                "" +
                "   @Resource" +
                "   private javax.jms.Topic myTopic;" +
                "" +
                "   @Resource" +
                "   private void setYourDataSource(DataSource ds) {" +
                "   }" +
                "" +
                "   @Resource" +
                "   private void setYourLong(Long long) {" +
                "   }" +
                "}");
        // should not be used, just for verification
        TestUtilities.copyStringToFileObject(srcFO, "foo/YourClass.java",
                "package foo;" +
                "" +
                "import javax.annotation.Resource;" +
                "import javax.sql.DataSource;" +
                "import javax.xml.rpc.Service;" +
                "" +
                "@Resource(name=\"yourClass\", type=javax.transaction.UserTransaction)" +
                "public class YourClass {" +
                "   @Resource" +
                "   private void setYourDataSource(DataSource ds) {" +
                "   }" +
                "" +
                "   @Resource" +
                "   private void setYourLong(Long long) {" +
                "   }" +
                "" +
                "   @Resource" +
                "   private void setMyService(Service service) {" +
                "   }" +
                "}");
    }
    
    private void assertResourceRefNames(final Set<String> resourceNames, final ResourceRef[] resourceRefs) {
        assertEquals(resourceNames.size(), resourceRefs.length);
        Set<String> resourceNamesCopy = new HashSet<String>(resourceNames);
        for (ResourceRef resourceRef : resourceRefs) {
            assertTrue(resourceNamesCopy.contains(resourceRef.getResRefName()));
            resourceNamesCopy.remove(resourceRef.getResRefName());
        }
    }
    
    private void assertResourceEnvRefNames(final Set<String> resourceNames, final ResourceEnvRef[] resourceEnvRefs) {
        assertEquals(resourceNames.size(), resourceEnvRefs.length);
        Set<String> resourceNamesCopy = new HashSet<String>(resourceNames);
        for (ResourceEnvRef resourceEnvRef : resourceEnvRefs) {
            assertTrue(resourceNamesCopy.contains(resourceEnvRef.getResourceEnvRefName()));
            resourceNamesCopy.remove(resourceEnvRef.getResourceEnvRefName());
        }
    }
    
    private void assertEnvEntryNames(final Set<String> resourceNames, final EnvEntry[] envEntries) {
        assertEquals(resourceNames.size(), envEntries.length);
        Set<String> resourceNamesCopy = new HashSet<String>(resourceNames);
        for (EnvEntry envEntry : envEntries) {
            assertTrue(resourceNamesCopy.contains(envEntry.getEnvEntryName()));
            resourceNamesCopy.remove(envEntry.getEnvEntryName());
        }
    }
    
    private void assertMessageDestinationRefNames(final Set<String> resourceNames, final MessageDestinationRef[] messageDestinationRefs) {
        assertEquals(resourceNames.size(), messageDestinationRefs.length);
        Set<String> resourceNamesCopy = new HashSet<String>(resourceNames);
        for (MessageDestinationRef messageDestinationRef : messageDestinationRefs) {
            assertTrue(resourceNamesCopy.contains(messageDestinationRef.getMessageDestinationRefName()));
            resourceNamesCopy.remove(messageDestinationRef.getMessageDestinationRefName());
        }
    }
    
    private void assertServiceRefNames(final Set<String> resourceNames, final ServiceRef[] serviceRefs) {
        assertEquals(resourceNames.size(), serviceRefs.length);
        Set<String> resourceNamesCopy = new HashSet<String>(resourceNames);
        for (ServiceRef serviceRef : serviceRefs) {
            assertTrue(resourceNamesCopy.contains(serviceRef.getServiceRefName()));
            resourceNamesCopy.remove(serviceRef.getServiceRefName());
        }
    }
    
}
