/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.uiapi;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public class ProjectTemplateAttributesProviderTest extends NbTestCase {

    private FileObject scratch;
    private FileObject folder;

    public ProjectTemplateAttributesProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        folder = scratch.createFolder("folder");
        MockLookup.setInstances(new FEQImpl());
        assertEquals(FEQImpl.ENCODING, FileEncodingQuery.getEncoding(folder).name());
    }

    @Override
    protected void tearDown() throws Exception {
        MockLookup.setInstances();
        super.tearDown();
    }

    public void testcheckProjectAttrs() throws Exception {
        Map<String, ? extends Object> checked = ProjectTemplateAttributesProvider.checkProjectAttrs(null, folder);
        assertAttribute("default", checked, "license");
        assertAttribute(FEQImpl.ENCODING, checked, "encoding");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");

        checked = ProjectTemplateAttributesProvider.checkProjectAttrs(map, folder);
        assertAttribute("default", checked, "license");
        assertAttribute(FEQImpl.ENCODING, checked, "encoding");
        assertEquals("bar", checked.get("foo"));

        map.put("project", Collections.emptyMap());
        checked = ProjectTemplateAttributesProvider.checkProjectAttrs(map, folder);
        assertAttribute("default", checked, "license");
        assertAttribute(FEQImpl.ENCODING, checked, "encoding");
        assertEquals("bar", checked.get("foo"));

        map.put("project", Collections.singletonMap("license", "gpl"));
        checked = ProjectTemplateAttributesProvider.checkProjectAttrs(map, folder);
        assertAttribute("gpl", checked, "license");
        assertAttribute(FEQImpl.ENCODING, checked, "encoding");
        assertEquals("bar", checked.get("foo"));

        Map<String, String> projectMap = new HashMap<String, String>();
        projectMap.put("license", "gpl");
        projectMap.put("encoding", "UTF-8");
        map.put("project", projectMap);
        checked = ProjectTemplateAttributesProvider.checkProjectAttrs(map, folder);
        assertAttribute("gpl", checked, "license");
        assertAttribute("UTF-8", checked, "encoding");
        assertEquals("bar", checked.get("foo"));
    }

    private static void assertAttribute(String expected, Map<String, ? extends Object> map, String attribute) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attrs = (Map<String, Object>) map.get("project");
        assertEquals(expected, attrs.get(attribute));
    }

    private final class FEQImpl extends FileEncodingQueryImplementation {

        public static final String ENCODING = "ISO-8859-1";

        @Override
        public Charset getEncoding(FileObject file) {
            if (file == folder) {
                return Charset.forName(ENCODING);
            }
            return null;
        }
    }
}
