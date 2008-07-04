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

package org.netbeans.modules.extexecution;

import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class InputOutputManagerTest extends NbTestCase {

    public InputOutputManagerTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        InputOutputManager.clear();
        super.tearDown();
    }

    public void testGet() {
        InputOutput io = IOProvider.getDefault().getIO("test", true);
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", null, null, null));

        InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput("test", false, null);
        assertEquals("test", data.getDisplayName());
        assertEquals(io, data.getInputOutput());
        assertNull(data.getRerunAction());
        assertNull(data.getStopAction());

        data = InputOutputManager.getInputOutput("test", true, null);
        assertNull(data);

        data = InputOutputManager.getInputOutput("test", false, null);
        assertNull(data);

        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", null, null, null));
        data = InputOutputManager.getInputOutput("test", false, null);
        assertNotNull(data);
    }

    public void testGetActions() {
        StopAction stopAction = new StopAction();
        RerunAction rerunAction = new RerunAction();

        InputOutput io = IOProvider.getDefault().getIO("test", new Action[] {rerunAction, stopAction});
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", stopAction, rerunAction, null));

        InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput("test", false, null);
        assertNull(data);

        data = InputOutputManager.getInputOutput("test", true, null);
        assertEquals("test", data.getDisplayName());
        assertEquals(io, data.getInputOutput());
        assertEquals(rerunAction, data.getRerunAction());
        assertEquals(stopAction, data.getStopAction());

        data = InputOutputManager.getInputOutput("test", true, null);
        assertNull(data);

        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", stopAction, rerunAction, null));
        data = InputOutputManager.getInputOutput("test", true, null);
        assertNotNull(data);
    }

    public void testGetRequired() {
        InputOutput io = IOProvider.getDefault().getIO("test", true);
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", null, null, null));

        InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput(io);
        assertEquals("test", data.getDisplayName());
        assertEquals(io, data.getInputOutput());
        assertNull(data.getRerunAction());
        assertNull(data.getStopAction());

        data = InputOutputManager.getInputOutput(io);
        assertNull(data);

        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", null, null, null));
        data = InputOutputManager.getInputOutput(io);
        assertNotNull(data);
    }

    public void testOrder() {
        InputOutput firstIO = IOProvider.getDefault().getIO("test", true);
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(firstIO, "test", null, null, null));
        InputOutput secondIO = IOProvider.getDefault().getIO("test #1", true);
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(secondIO, "test #1", null, null, null));

        InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput("test", false, null);
        assertEquals("test", data.getDisplayName());
        assertEquals(firstIO, data.getInputOutput());

        data = InputOutputManager.getInputOutput("test", false, null);
        assertEquals("test #1", data.getDisplayName());
        assertEquals(secondIO, data.getInputOutput());

        data = InputOutputManager.getInputOutput("test", false, null);
        assertNull(data);
    }
}
