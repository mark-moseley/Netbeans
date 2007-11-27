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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;

/**
 *
 * @author Petr Hejl
 */
public class InstancePropertiesTest extends ServerRegistryTestBase {

    private static final String TEST_URL_PREFIX = "fooservice:";

    private static final String TEST_USERNAME = "username";

    private static final String TEST_PASSWORD = "password";

    private static final String TEST_DISPLAY_NAME = "name";

    public InstancePropertiesTest(String name) {
        super(name);
    }

    public void testCreateProperties() throws InstanceCreationException {
        String url = TEST_URL_PREFIX + "createProperties";

        Map<String, String> expected = new HashMap<String, String>();
        expected.put(InstanceProperties.URL_ATTR, url);
        expected.put(InstanceProperties.USERNAME_ATTR, TEST_USERNAME);
        expected.put(InstanceProperties.PASSWORD_ATTR, TEST_PASSWORD);
        expected.put(InstanceProperties.DISPLAY_NAME_ATTR, TEST_DISPLAY_NAME);

        InstanceListener listener = new TestInstanceListener(url, expected);
        ServerRegistry.getInstance().addInstanceListener(listener);
        try {
            InstanceProperties props = InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
            assertPropertiesEquals(expected, props);
        } finally {
            ServerRegistry.getInstance().removeInstanceListener(listener);
        }

        try {
            InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
            fail("Duplicate instance created");
        } catch (InstanceCreationException ex) {
            // expected
        }
    }

    public void testCreatePropertiesWithDefaults() throws InstanceCreationException {
        Map<String, String> defaults = new HashMap<String, String>();
        defaults.put("property1", "value1");
        defaults.put("property2", "value2");
        defaults.put("property3", "value3");
        defaults.put(InstanceProperties.URL_ATTR, "ignored");
        defaults.put(InstanceProperties.USERNAME_ATTR, "ignored");
        defaults.put(InstanceProperties.PASSWORD_ATTR, "ignored");
        defaults.put(InstanceProperties.DISPLAY_NAME_ATTR, "ignored");

        String url = TEST_URL_PREFIX + "createPropertiesExtended";

        Map<String, String> expected = new HashMap<String, String>();
        expected.put(InstanceProperties.URL_ATTR, url);
        expected.put(InstanceProperties.USERNAME_ATTR, TEST_USERNAME);
        expected.put(InstanceProperties.PASSWORD_ATTR, TEST_PASSWORD);
        expected.put(InstanceProperties.DISPLAY_NAME_ATTR, TEST_DISPLAY_NAME);
        expected.put("property1", "value1");
        expected.put("property2", "value2");
        expected.put("property3", "value3");

        InstanceListener listener = new TestInstanceListener(url, expected);
        ServerRegistry.getInstance().addInstanceListener(listener);
        try {
            InstanceProperties props = InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME, defaults);

            assertPropertiesEquals(expected, props);
        } finally {
            ServerRegistry.getInstance().removeInstanceListener(listener);
        }

        try {
            InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
            fail("Duplicate instance created"); // NOI18N
        } catch (InstanceCreationException ex) {
            // expected
        }
    }

    private static void assertPropertiesEquals(Map<String, String> expected, InstanceProperties props) {
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            assertEquals(entry.getValue(), props.getProperty(entry.getKey()));
        }
    }

    private static class TestInstanceListener implements InstanceListener {

        private final String name;

        private final Map<String, String> expected = new HashMap<String, String>();

        public TestInstanceListener(String name, Map<String, String> expected) {
            this.name = name;
            this.expected.putAll(expected);
        }

        public void instanceAdded(String serverInstanceID) {
            if (name.equals(serverInstanceID)) {
                InstanceProperties props = InstanceProperties.getInstanceProperties(serverInstanceID);
                assertNotNull(props);

                assertPropertiesEquals(expected, props);
            }
        }

        public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
        }

        public void instanceRemoved(String serverInstanceID) {
        }

    }
}
