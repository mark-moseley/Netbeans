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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.openide.awt;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import org.netbeans.junit.NbTestCase;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/** Test of behaviour of manager listening for ActionMap in a lookup.
 *
 * @author Jaroslav Tulach
 */
public class GlobalManagerTest extends NbTestCase {
    
    public GlobalManagerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testFindManager() {
        doFindManager(true);
    }
    public void testFindManagerNoSurvive() {
        doFindManager(false);
    }
    
    private void doFindManager(boolean survive) {
        Lookup context = new AbstractLookup(new InstanceContent());
        
        GlobalManager r1 = GlobalManager.findManager(context, survive);
        assertNotNull("Need an instace", r1);
        GlobalManager r2 = GlobalManager.findManager(context, survive);
        assertEquals("Caches", r1, r2);

        Lookup c3 = new AbstractLookup(new InstanceContent());
        GlobalManager r3 = GlobalManager.findManager(c3, survive);
        if (r3 == r2) {
            fail("Need next manager for new lookup: " + r2 + " e: " + r3);
        }
        
        r1 = null;
        WeakReference<?> ref = new WeakReference<GlobalManager>(r2);
        r2 = null;
        assertGC("Disappers", ref);
        
        WeakReference<?> lookupRef = new WeakReference<Lookup>(c3);
        c3 = null;
        r3 = null;
        
        assertGC("Lookup can also disappear", lookupRef);
    }

    public void testActionsCanHoldOnLookup() {
        class TopComponent extends JPanel implements Lookup.Provider {
            Lookup l;

            void associateLookup(Lookup f) {
                l = f;
            }

            public Lookup getLookup() {
                return l;
            }
        }
        TopComponent tc = new TopComponent();
        class CAA extends AbstractAction implements
                ContextAwareAction {
            public void actionPerformed(ActionEvent arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Action createContextAwareInstance(Lookup actionContext) {
                return this;
            }

        }
        tc.associateLookup(Lookups.fixed(tc.getActionMap(), tc));
        ContextAwareAction del = new CAA();
        ContextAwareAction context = Actions.context(Integer.class, true, true, del, null, "DisplayName", null, true);
        Action a = context.createContextAwareInstance(tc.getLookup());
        tc.getActionMap().put("key", a);

        WeakReference<Object> ref = new WeakReference<Object>(tc);
        tc = null;
        a = null;
        del = null;
        context = null;
        assertGC("Can the component GC?", ref);

    }
}
