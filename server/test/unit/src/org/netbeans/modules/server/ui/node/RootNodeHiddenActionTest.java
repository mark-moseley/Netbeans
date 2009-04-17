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

package org.netbeans.modules.server.ui.node;

import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RootNodeHiddenActionTest extends NbTestCase {

    public RootNodeHiddenActionTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }



    public void testGetActions() throws Exception {
        RootNode rn = RootNode.getInstance();
        FileObject fo = FileUtil.getConfigFile("Servers/Actions");
        assertNotNull("Folder for actions precreated", fo);
        FileObject x = fo.createData(MyAction.class.getName().replace('.', '-') + ".instance");
        x.setAttribute("position", 37);
        Action[] arr = rn.getActions(true);
        assertEquals("Just one action and two separators found: " + Arrays.asList(arr), 3, arr.length);
        MyAction a = MyAction.get(MyAction.class);
        if (a == arr[0] || a == arr[1] || a == arr[2]) {
            fail("My action shall not be present as it is hidden: " + arr[0] + " 2nd: " + arr[1] + " 3rd: " + arr[2]);
        }
    }

    public static final class MyAction extends CallableSystemAction {
        static int cnt;

        @Override
        protected void initialize() {
            super.initialize();
            putValue("serverNodeHidden", Boolean.TRUE);
        }

        @Override
        public void performAction() {
            cnt++;
        }

        @Override
        public String getName() {
            return "My";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

    }
}