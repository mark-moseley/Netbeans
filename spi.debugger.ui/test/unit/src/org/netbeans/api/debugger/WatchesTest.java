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

package org.netbeans.api.debugger;

import org.netbeans.api.debugger.test.TestDebuggerManagerListener;
import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * Tests DebuggerManager's Watches management.
 *
 * @author Maros Sandor
 */
public class WatchesTest extends DebuggerApiTestBase {

    public WatchesTest(String s) {
        super(s);
    }

    public void testWatches() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();

        dm.addDebuggerListener(dml);

        initWatches(dm, dml);
        Watch w1 = addWatch(dm, dml);
        Watch w2 = addWatch(dm, dml);
        Watch w3 = addWatch(dm, dml);
        removeWatch(dm, w2, dml);
        removeWatch(dm, w3, dml);
        Watch w4 = addWatch(dm, dml);
        removeWatch(dm, w1, dml);
        Watch w5 = addWatch(dm, dml);
        removeWatch(dm, w5, dml);
        removeWatch(dm, w4, dml);

        dm.removeDebuggerListener(dml);
    }

    private void initWatches(DebuggerManager dm, TestDebuggerManagerListener dml) {
        dm.getWatches();    // trigger the "watchesInit" property change
        TestDebuggerManagerListener.Event event;
        List events = dml.getEvents();
        assertEquals("Wrong PCS", 1, events.size());
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watchesInit", pce.getPropertyName());
    }

    private void removeWatch(DebuggerManager dm, Watch w, TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;
        Watch [] watches = dm.getWatches();

        dm.removeWatch(w);
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new TestDebuggerManagerListener.Event("watchRemoved", w)));
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watches", pce.getPropertyName());
        Watch [] newWatches = dm.getWatches();
        for (int i = 0; i < newWatches.length; i++) {
            assertNotSame("Watch was not removed", newWatches[i], w);
        }
        assertEquals("Wrong number of installed watches", watches.length - 1, newWatches.length);
    }

    private Watch addWatch(DebuggerManager dm, TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;

        int watchesSize = dm.getWatches().length;
        Watch newWatch = dm.createWatch("watch");
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new TestDebuggerManagerListener.Event("watchAdded", newWatch)));
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watches", pce.getPropertyName());
        Watch [] watches = dm.getWatches();
        assertEquals("Wrong number of installed watches", watchesSize + 1, watches.length);
        return newWatch;
    }

}
