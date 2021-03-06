/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.util;

import org.netbeans.modules.nativeexecution.support.*;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 *
 * @author ak119685
 */
public class MacroExpanderFactoryTest {

    public MacroExpanderFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getExpander method, of class MacroExpanderFactory.
     */
//    @Test
    public void testGetExpander_ExecutionEnvironment_String() {
        System.out.println("getExpander");
//        ExecutionEnvironment execEnv = new ExecutionEnvironment("ak119685", "brighton.russia.sun.com");
        ExecutionEnvironment execEnv = new ExecutionEnvironment();
//        ConnectionManager.getInstance().getConnectToAction(execEnv, null).invoke();
        MacroExpander expander = MacroExpanderFactory.getExpander(execEnv, "SunStudio");

        Map<String, String> myenv = new HashMap<String, String>();
        try {
            myenv.put("PATH", expander.expandMacros("/bin:$PATH", myenv));
            myenv.put("PATH", expander.expandMacros("/usr/bin:$platform:$PATH", myenv));
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println(myenv.toString());
        try {
            System.out.println("$osname-${platform}$_isa -> " + expander.expandPredefinedMacros("$osname-$platform$_isa"));
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void testPath() {
        ExecutionEnvironment execEnv = new ExecutionEnvironment("test", "localhost");
        NativeProcessBuilder npb = new NativeProcessBuilder(
                execEnv, "/bin/env").addEnvironmentVariable(
                "PATH", "/firstPath:$PATH:${ZZZ}_${platform}").addEnvironmentVariable("PATH", "$PATH:/secondPath").addEnvironmentVariable("XXX", "It WORKS!");

        StringWriter result = new StringWriter();
        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL).outProcessorFactory(new InputRedirectorFactory(result));
        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "test"); // NOI18N

        Future<Integer> res = execService.run();

        try {
            res.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("Output is " + result.toString());

    }
}