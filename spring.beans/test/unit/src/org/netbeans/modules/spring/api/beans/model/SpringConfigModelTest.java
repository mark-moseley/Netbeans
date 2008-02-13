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

package org.netbeans.modules.spring.api.beans.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.WriteContext;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.TestUtils;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;

/**
 *
 * @author Andrei Badea
 */
public class SpringConfigModelTest extends ConfigFileTestCase {

    public SpringConfigModelTest(String testName) {
        super(testName);
    }

    public void testRunReadAction() throws Exception {
        ConfigFileGroup group = ConfigFileGroup.create(Collections.<File>emptyList());
        SpringConfigModel model = new SpringConfigModel(group);
        final boolean[] actionRun = { false };
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans springBeans) {
                actionRun[0] = true;
            }
        });
        assertTrue(actionRun[0]);
    }

    public void testExceptionPropagation() throws IOException {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);
        ConfigFileGroup group = ConfigFileGroup.create(Collections.singletonList(configFile));
        SpringConfigModel model = new SpringConfigModel(group);
        try {
            model.runReadAction(new Action<SpringBeans>() {
                public void run(SpringBeans parameter) {
                    throw new RuntimeException();
                }
            });
            fail();
        } catch (RuntimeException e) {
            // OK.
        }
        try {
            model.runWriteAction(new Action<WriteContext>() {
                public void run(WriteContext parameter) {
                    throw new RuntimeException();
                }
            });
            fail();
        } catch (RuntimeException e) {
            // OK.
        }
    }

    public void testWriteActionInvocation() throws IOException {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);
        File configFile2 = createConfigFileName("dispatcher-servlet.xml");
        TestUtils.copyStringToFile(contents, configFile2);
        ConfigFileGroup group = ConfigFileGroup.create(Arrays.asList(configFile, configFile2));
        SpringConfigModel model = new SpringConfigModel(group);
        final Set<File> invokedForFiles = new HashSet<File>();
        model.runWriteAction(new Action<WriteContext>() {
            public void run(WriteContext context) {
                invokedForFiles.add(context.getFile());
            }
        });
        assertEquals(2, invokedForFiles.size());
        assertTrue(invokedForFiles.contains(configFile));
        assertTrue(invokedForFiles.contains(configFile2));
    }

    public void testWriteAccessDocumentWrite() throws IOException {
        String contents = TestUtils.createXMLConfigText("<bean id='foo' class='org.example.Foo'/>");
        TestUtils.copyStringToFile(contents, configFile);
        ConfigFileGroup group = ConfigFileGroup.create(Collections.singletonList(configFile));
        SpringConfigModel model = new SpringConfigModel(group);
        model.runWriteAction(new Action<WriteContext>() {
            public void run(WriteContext context) {
                int offset = context.getSpringBeans().findBean("foo").getLocation().getOffset();
                try {
                    String expected = "<bean id='foo'";
                    assertEquals(expected, context.getDocument().getText(offset, expected.length()));
                } catch (BadLocationException e) {
                    fail();
                }
            }
        });
    }
}
