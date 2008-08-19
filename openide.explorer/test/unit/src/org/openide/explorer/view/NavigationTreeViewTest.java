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

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/** Check the behaviour of the view when navigating in it.
 *
 * @author  Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #1187
public class NavigationTreeViewTest extends NbTestCase {
    
    private TreeView treeView;
    private ExplorerWindow testWindow;
    private CharSequence log;
    
    public NavigationTreeViewTest(String testName) {
        super(testName);
    }

    protected boolean lazy() {
        return false;
    }

    @Override
    protected void runTest() throws Throwable {
        log = Log.enable(VisualizerNode.LOG.getName(), Level.FINEST);
        super.runTest();
        if (log.length() > 0 && log.toString().indexOf("Children.MUTEX") >= 0) {
            fail("something has been logged:\n" + log);
        }
    }

    @Override
    protected void setUp() throws Exception {
        assertFalse("Cannot run in AWT thread", EventQueue.isDispatchThread());
        treeView = new BeanTreeView();
        testWindow = new ExplorerWindow(treeView);
        testWindow.pack();
        testWindow.setVisible(true);

        for (int i = 0; i < 10; i++) {
            if (testWindow.isShowing()) {
                break;
            }
            Thread.sleep(200);
        }

        assertTrue("Tree is visible", testWindow.isShowing());
    }

    @Override
    protected void tearDown() throws Exception {
        testWindow.setVisible(false);
    }



    public void testStructureFullOfFormFiles() throws Exception {
        Children ch = new Children.Array();
        Node root = new AbstractNode(ch);
        root.setName(getName());

        ch.add(nodeWith("A", "-A", "-B", "B"));
        ch.add(nodeWith("X", "Y", "Z"));

        Node first = ch.getNodes()[0];

        ExplorerManager em = testWindow.getExplorerManager();
        em.setRootContext(root);
        em.setSelectedNodes(new Node[] { first });

        sendKey(KeyEvent.VK_RIGHT);
        sendKey(KeyEvent.VK_DOWN);

        assertEquals("Explored context is N0", first, em.getExploredContext());
        assertEquals("Selected node is A", 1, em.getSelectedNodes().length);
        assertEquals("Selected node is A", "A", em.getSelectedNodes()[0].getName());

        sendKey(KeyEvent.VK_ENTER);

        Keys keys = (Keys)first.getChildren();
        assertEquals("One invocation", 1, keys.actionPerformed);
        assertFalse("No write access", keys.writeAccess);
        assertFalse("No read access", keys.readAccess);
    }

    private void sendKey(final int keyCode) throws Exception {
        class Process implements Runnable {
            int i = 0;
            Component owner = null;

            public void run() {
                Component o = FocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                for (Component t = o; t != null; t = t.getParent()) {
                    if (t == treeView) {
                        owner = o;
                    }
                }
                if (owner == null && i < 20) {
                    i++;
                    return;
                }


                assertNotNull("Focus owner: " + owner + "\nis not under: " + treeView, owner);

                KeyEvent ke = new KeyEvent(
                    owner, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    0, keyCode, KeyEvent.CHAR_UNDEFINED,
                    KeyEvent.KEY_LOCATION_STANDARD
                );
                owner.dispatchEvent(ke);
            }
        }
        Process processEvent = new Process();

        while (processEvent.owner == null) {
            SwingUtilities.invokeAndWait(processEvent);
        }
    }
    
    private int cnt;
    private Node[] nodeWith(String... arr) {
        AbstractNode an = new AbstractNode(new Keys(arr));
        an.setName("N" + cnt++);
        return new Node[] { an };
    }


    /** Sample keys.
    */
    private class Keys extends Children.Keys<String> {
        public int actionPerformed;
        public boolean writeAccess;
        public boolean readAccess;

        /** Constructor.
         */
        public Keys (String... args) {
            super(lazy());
            if (args != null && args.length > 0) {
                setKeys (args);
            }
        }

        /** Changes the keys.
         */
        public void keys (String... args) {
            super.setKeys (args);
        }

        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *   nodes for this key
         */
        protected Node[] createNodes(String key) {
            if (key.startsWith("-")) {
                return null;
            }

            class An extends AbstractNode implements Action {
                public An() {
                    super(Children.LEAF);
                }

                @Override
                public Action getPreferredAction() {
                    return this;
                }

                public void putValue(String key, Object value) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                public void setEnabled(boolean b) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                public boolean isEnabled() {
                    return true;
                }

                public void actionPerformed(ActionEvent e) {
                    actionPerformed++;
                    readAccess = Children.MUTEX.isReadAccess();
                    writeAccess = Children.MUTEX.isWriteAccess();
                }
            }
            AbstractNode an = new An();
            an.setName (key.toString ());

            return new Node[] { an };
        }

    }
    private static final class ExplorerWindow extends JFrame
                               implements ExplorerManager.Provider {
        
        private final ExplorerManager explManager = new ExplorerManager();
        
        ExplorerWindow(JComponent content) {
            super("TreeView test"); //NOI18N
            getContentPane().add(content, BorderLayout.CENTER);
        }
        
        public ExplorerManager getExplorerManager() {
            return explManager;
        }
        
    } // end of ExplorerManager
    
}
