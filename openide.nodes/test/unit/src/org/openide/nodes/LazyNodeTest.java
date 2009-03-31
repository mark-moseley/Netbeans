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

package org.openide.nodes;

import java.util.HashMap;
import javax.swing.Action;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LazyNodeTest {

    public LazyNodeTest() {
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

    @Test
    public void testCreateOriginalAfterNodeExpansion() {
        doCreateOriginal(true);
    }

    @Test
    public void testCreateOriginalAfterGetActions() {
        doCreateOriginal(false);
    }

    private void doCreateOriginal(boolean askForChildren) {
        AbstractNode realNode = new AbstractNode(new Children.Array()) {
            @Override
            public Action[] getActions(boolean context) {
                return getActions();
            }

            @SuppressWarnings("deprecation")
            @Override
            public SystemAction[] getActions() {
                return new SystemAction[] {
                    A1.get(A1.class),
                    A2.get(A2.class),
                    A3.get(A3.class)
                };
            }
        };
        realNode.setName("RealNode");
        realNode.setDisplayName("Real Node");
        realNode.setShortDescription("Real Node for Test");
        realNode.getChildren().add(new Node[] {
            new AbstractNode(Children.LEAF),
            new AbstractNode(Children.LEAF),
            new AbstractNode(Children.LEAF),
        });

        CntHashMap chm = new CntHashMap("original");
        chm.put("name", "ANode");
        chm.put("displayName", "A Node");
        chm.put("shortDescription", "A Node for Test");
        chm.put("iconResource", "org/openide/nodes/beans.gif");
        chm.put("original", realNode);

        Node instance = NodeOp.factory(chm);
        assertEquals("ANode", instance.getName());
        assertEquals("A Node", instance.getDisplayName());
        assertEquals("A Node for Test", instance.getShortDescription());
        assertEquals("No real node queried yet", 0, chm.cnt);

        if (askForChildren) {
            Node[] arr = instance.getChildren().getNodes(true);
            assertEquals("Three children", 3, arr.length);
        } else {
            Action[] arr = instance.getActions(true);
            assertEquals("Three actions", 3, arr.length);
        }
        
        assertEquals("Real node queried now", 1, chm.cnt);

        assertEquals("RealNode", instance.getName());
        assertEquals("Real Node", instance.getDisplayName());
        assertEquals("Real Node for Test", instance.getShortDescription());
    }

    private static class CntHashMap extends HashMap<Object,Object> {
        private final Object keyToWatch;
        int cnt;

        public CntHashMap(Object keyToWatch) {
            this.keyToWatch = keyToWatch;
        }

        @Override
        public Object get(Object key) {
            if (keyToWatch.equals(key)) {
                cnt++;
            }
            return super.get(key);
        }


    }

    public static class A1 extends CallbackSystemAction {
        @Override
        public String getName() {
            return "A1";
        }

        @Override
        public HelpCtx getHelpCtx() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    public static final class A2 extends A1 {
        @Override
        public String getName() {
            return "A2";
        }
    }
    public static final class A3 extends A1 {
        @Override
        public String getName() {
            return "A3";
        }
    }
}