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

package org.netbeans.modules.extexecution.api;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ExternalProcessBuilderTest extends NbTestCase {

    public ExternalProcessBuilderTest(String name) {
        super(name);
    }

    public void testEnvironment() {
        ExternalProcessBuilder builder = new ExternalProcessBuilder("command");
        builder.addEnvironmentVariable("test1", "value1");
        builder.addEnvironmentVariable("test2", "value2");

        Map<String, String> env = new HashMap<String, String>(
                builder.buildEnvironment(Collections.<String, String>emptyMap()));
        assertEquals("value1", env.remove("test1"));
        assertEquals("value2", env.remove("test2"));
        assertTrue(env.isEmpty());
    }

    public void testPath() {
        ExternalProcessBuilder builder = new ExternalProcessBuilder("command");
        Map<String, String> original = new HashMap<String, String>();
        original.put("PATH", "original");

        Map<String, String> env = new HashMap<String, String>(
                builder.buildEnvironment(original));
        assertEquals("original", env.remove("PATH"));
        assertTrue(env.isEmpty());

        File addedPath = new File("addedPath");
        builder.addPath(addedPath);
        env = new HashMap<String, String>(builder.buildEnvironment(original));
        assertEquals(addedPath.getAbsolutePath() + File.pathSeparator + "original", env.remove("PATH"));
        assertTrue(env.isEmpty());

        File nextPath = new File("nextPath");
        builder.addPath(nextPath);
        env = new HashMap<String, String>(builder.buildEnvironment(original));
        assertEquals(
                nextPath.getAbsolutePath() + File.pathSeparator
                + addedPath.getAbsolutePath() + File.pathSeparator
                + "original", env.remove("PATH"));
        assertTrue(env.isEmpty());

        builder.pwdToPath(true);
        File pwd = new File(System.getProperty("user.dir"));
        env = new HashMap<String, String>(builder.buildEnvironment(original));
        assertEquals(
                pwd.getAbsolutePath() + File.pathSeparator
                + nextPath.getAbsolutePath() + File.pathSeparator
                + addedPath.getAbsolutePath() + File.pathSeparator
                + "original", env.remove("PATH"));
        assertTrue(env.isEmpty());
    }
}
