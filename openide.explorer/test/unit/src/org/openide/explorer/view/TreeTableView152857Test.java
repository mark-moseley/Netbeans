/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
import java.awt.Dialog;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Children.Keys;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/** Fixes fix of issue #152857: Sorting in TreeTableView should not be done on Visualizers
 *
 * @author  Jiri Rechtacek
 */
public class TreeTableView152857Test extends NbTestCase {

    private TTV view;

    public TreeTableView152857Test (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return false;
    }

    public void testRemoveNodeInTTV () throws InterruptedException {
        StringKeys children = new StringKeys (true);
        children.doSetKeys (new String [] {"1", "3", "2"});
        Node root = new TestNode (children, "root");
        view = new TTV (root);
        TreeNode ta = Visualizer.findVisualizer(root);

        DialogDescriptor dd = new DialogDescriptor (view, "", false, null);
        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);

        Thread.sleep (1000);
        ((StringKeys) root.getChildren ()).doSetKeys (new String [] {"1", "2"});
        Thread.sleep (1000);
        
        assertEquals ("Node on 0nd position is '1'", "1", ta.getChildAt (0).toString ());
        assertEquals ("Node on 1st position is '2'", "2", ta.getChildAt (1).toString ());

        d.setVisible (false);
    }

    public void testSetSelectedRow () throws PropertyVetoException, InterruptedException, InvocationTargetException {
        StringKeys children = new StringKeys (true);
        children.doSetKeys (new String [] {"1", "3", "2"});
        Node root = new TestNode (children, "root");
        Node aChild = root.getChildren ().getNodeAt (1);
        view = new TTV (root);
        DialogDescriptor dd = new DialogDescriptor (view, "", false, null);
        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);
        Thread.sleep (1000);
        SwingUtilities.invokeAndWait (new Runnable () {
            public void run () {
                view.view.tree.setSelectionRow (3);
            }
        });
        Thread.sleep (1000);
        Node [] selectedNodes = view.getExplorerManager ().getSelectedNodes ();
        assertNotNull ("A child found", selectedNodes);
        assertEquals ("Only once child", 1, selectedNodes.length);
        Node aSelectedChild = selectedNodes [0];
        assertEquals ("They are my children", aChild, aSelectedChild);
        d.setVisible (false);
    }

    public void testSelectedNodes () throws PropertyVetoException, InterruptedException, InvocationTargetException {
        StringKeys children = new StringKeys (true);
        children.doSetKeys (new String [] {"1", "3", "2"});
        Node root = new TestNode (children, "root");
        Node aChild = root.getChildren ().getNodeAt (1);
        view = new TTV (root);
        DialogDescriptor dd = new DialogDescriptor (view, "", false, null);
        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);
        Thread.sleep (1000);
        view.getExplorerManager ().setSelectedNodes (new Node [] { aChild });
        final int rows [] = new int [1];
        SwingUtilities.invokeAndWait (new Runnable () {

            public void run () {
                int [] selectedRows = view.view.tree.getSelectionRows ();
                assertNotNull ("Some rows are selected", selectedRows);
                assertEquals ("Only one selected row", 1, selectedRows.length);
                rows [0] = selectedRows [0];
            }
        });
        Thread.sleep (1000);
        assertEquals ("Child on 3rd position was selected", 3, rows [0]);
        d.setVisible (false);
    }

    private static class StringKeys extends Keys<String> {

        public StringKeys (boolean lazy) {
            super (lazy);
        }

        @Override
        protected Node[] createNodes (String key) {
            AbstractNode n = new TestNode (Children.LEAF, key);
            n.setName (key);
            return new Node[]{n};
        }

        void doSetKeys (String[] keys) {
            setKeys (keys);
        }
    }

    private class TTV extends JPanel implements ExplorerManager.Provider {

        private final ExplorerManager manager = new ExplorerManager ();
        private TreeTableView view;

        private TTV (Node rootNode) {
            setLayout (new BorderLayout ());
            manager.setRootContext (rootNode);

            Node.Property[] props = rootNode.getPropertySets ()[0].getProperties ();
            view = new TreeTableView ();
            view.setProperties (props);

            //view.setRootVisible (false);

            add (view, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager () {
            return manager;
        }

    }

    private static class TestNode extends AbstractNode {

        public TestNode (String name) {
            super (Children.LEAF);
            setName (name);
        }

        public TestNode (Children children, String name) {
            super (children);
            setName (name);
        }

        @Override
        protected Sheet createSheet () {
            Sheet s = super.createSheet ();
            Sheet.Set ss = s.get (Sheet.PROPERTIES);
            if (ss == null) {
                ss = Sheet.createPropertiesSet ();
                s.put (ss);
            }
            Property [] props = new Property [2];

            DummyProperty dp = new DummyProperty (getName ());
            dp.setValue ("ComparableColumnTTV", Boolean.TRUE);
            props [0] = dp;

            Property p_tree = new Node.Property<Boolean> (Boolean.class) {

                @Override
                public boolean canRead () {
                    return true;
                }

                @Override
                public Boolean getValue () throws IllegalAccessException, InvocationTargetException {
                    return Boolean.TRUE;
                }

                @Override
                public boolean canWrite () {
                    return false;
                }

                @Override
                public void setValue (Boolean val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    throw new UnsupportedOperationException ("Not supported yet.");
                }
            };

            p_tree.setValue ("TreeColumnTTV", Boolean.TRUE);
            p_tree.setValue ("ComparableColumnTTV", Boolean.TRUE);
            p_tree.setValue ("SortingColumnTTV", Boolean.TRUE);
            props [1] = p_tree;

            ss.put (props);

            return s;
        }

        private class DummyProperty extends Property<String> {

            public DummyProperty (String val) {
                super (String.class);
                setName ("unitTestPropName");
                try {
                    setValue (val);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace (ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace (ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace (ex);
                }
            }

            public boolean canRead () {
                return true;
            }

            public String getValue () throws IllegalAccessException,
                    InvocationTargetException {
                return (String) getValue ("unitTestPropName");
            }

            public boolean canWrite () {
                return true;
            }

            public void setValue (String val) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                setValue ("unitTestPropName", val);
            }
        }
    }
}
