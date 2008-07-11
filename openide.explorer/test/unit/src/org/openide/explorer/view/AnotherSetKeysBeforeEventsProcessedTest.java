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

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children.Keys;
import org.openide.util.Exceptions;

/**
 *
 * @author Holy
 */
public class AnotherSetKeysBeforeEventsProcessedTest extends NbTestCase {

    public AnotherSetKeysBeforeEventsProcessedTest(String name) {
        super(name);
    }
    private static class StrKeys extends Keys<String> {

        public StrKeys() {
            //super(true);
        }

        @Override
        protected Node[] createNodes(String key) {
            if (key.contains("Empty")) {
                return null;
            } else {
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key);
                return new Node[]{n};
            }
        }
        void doSetKeys(String[] keys) {
            setKeys(keys);
        }
    }
    
    private static class Panel extends JPanel
            implements ExplorerManager.Provider {
        private ExplorerManager em = new ExplorerManager();
        
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
    StrKeys children = new StrKeys();
    AbstractNode root = new AbstractNode(children);
    AtomicBoolean ab = new AtomicBoolean(false);

    class AwtRun implements Runnable {

        VisualizerNode visNode;
        Panel p = new Panel();
        BeanTreeView btv = new BeanTreeView();
        JFrame f = new JFrame();
        JTree tree = btv.tree;
        
        public void run() {
            try {
                root.setName("test root");
                p.getExplorerManager().setRootContext(root);
                p.add(BorderLayout.CENTER, btv);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.getContentPane().add(BorderLayout.CENTER, p);
                f.setVisible(true);
                ab.set(true);
                while (ab.get() == true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                Node[] nodes = children.getNodes();
                try {
                    p.getExplorerManager().setSelectedNodes(new Node[]{nodes[2]});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
                TreePath[] paths = tree.getSelectionPaths();
            } finally {
                ab.set(true);
            }
        }
    }
    
    public void test() throws InterruptedException {
        children.doSetKeys(new String[] {"1", "2"});
        Node[] nodes = children.getNodes();
        AwtRun run = new AwtRun();
        SwingUtilities.invokeLater(run);
        while (ab.get() == false) {
            Thread.sleep(50);
        }
        children.doSetKeys(new String[] {"1", "3", "2"});
        children.doSetKeys(new String[] {"3", "2", "1"});
        ab.set(false);
        while (ab.get() == false) {
            Thread.sleep(50);
        }
        VisualizerNode vn = (VisualizerNode) Visualizer.findVisualizer(nodes[0]);
    }
}