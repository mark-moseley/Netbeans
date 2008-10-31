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

package org.netbeans.modules.cnd.remote.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 * A common base class for remote "unit" tests
 * @author Sergey Grinev
 */
public abstract class RemoteTestBase extends BaseTestCase {

    protected RemoteTestBase(String testName) {
        super(testName);
    }
    
    protected String getKey() throws Exception {
        return getUserName() + "@" + getHostName();
    }

    private static String userName = null;

    protected String getUserName() {
        if (userName == null) {
            String name = System.getProperty("cnd.remote.user.name");
            if( name == null ) {
                name = System.getenv("CND_REMOTE_USER_NAME");
            }
            userName = name;
        }
        return userName;
    }

    private static String hostName = null;

    protected String getHostName() throws Exception {
        if (hostName == null) {
            String host = System.getProperty("cnd.remote.host.name");
            if( host == null ) {
                host = System.getenv("CND_REMOTE_HOST_NAME");
            }
            hostName = host;
        }
        return hostName;
    }

    private static Boolean canTest = null;

    protected boolean canTest() throws Exception {
        if (canTest == null) {
            canTest = new Boolean(getUserName()!=null && getHostName()!=null);
        }
        return canTest.booleanValue();
    }

    public static class FakeCompilerSet extends CompilerSet {

        private List<Tool> tools = Collections.<Tool>singletonList(new FakeTool());

        public FakeCompilerSet() {
            super(PlatformTypes.getDefaultPlatform());
        }

        @Override
        public List<Tool> getTools() {
            return tools;
        }

        private static class FakeTool extends BasicCompiler {

            private List<String> fakeIncludes = new ArrayList<String>();

            private FakeTool() {
                super("fake", CompilerFlavor.getUnknown(PlatformTypes.getDefaultPlatform()), 0, "fakeTool", "fakeTool", "/usr/sfw/bin");
                fakeIncludes.add("/usr/include"); //NOI18N
                fakeIncludes.add("/usr/local/include"); //NOI18N
                fakeIncludes.add("/usr/sfw/include"); //NOI18N
                //fakeIncludes.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include");
            }

            @Override
            public List getSystemIncludeDirectories() {
                return fakeIncludes;
            }

            @Override
            public CompilerDescriptor getDescriptor() {
                return null;
            }
        }
    }
}
