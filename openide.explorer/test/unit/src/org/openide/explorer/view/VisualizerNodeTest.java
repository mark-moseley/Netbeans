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

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/** VisualizerNode tests, mostly based on reported bugs.
 */
public class VisualizerNodeTest extends NbTestCase {

    public VisualizerNodeTest(String name) {
        super(name);
    }

    protected boolean runInEQ() {
        return true;
    }

    public void testIconIsProvidedEvenTheNodeIsBrokenIssue46727() {
        final boolean[] arr = new boolean[1];
        
        AbstractNode a = new AbstractNode(Children.LEAF) {
            public Image getIcon(int type) {
                arr[0] = true;
                return null;
            }
        };
        
        VisualizerNode v = VisualizerNode.getVisualizer(null, a);
        assertNotNull("Visualizer node", v);
        
        Icon icon = v.getIcon(false, false);
        assertNotNull("Cannot be null even the node's icon is null", icon);
        assertTrue("getIcon called", arr[0]);
    }
    
    public void testIndexOfProvidesResultsEvenIfTheVisualizerIsComputedViaDifferentMeans() throws Exception {
        AbstractNode a = new AbstractNode(new Children.Array());
        AbstractNode m = new AbstractNode(Children.LEAF);
        a.getChildren().add(new Node[] { Node.EMPTY.cloneNode(), m, Node.EMPTY.cloneNode() });
        
        TreeNode ta = Visualizer.findVisualizer(a);
        TreeNode tm = Visualizer.findVisualizer(m);
        
        assertEquals("Index is 1", 1, ta.getIndex(tm));
    }
    
    public void testIconsAreShared() {
        AbstractNode a1 = new AbstractNode(Children.LEAF);
        VisualizerNode v1 = VisualizerNode.getVisualizer(null, a1);
        Icon icon1 = v1.getIcon(false, false);
        
        AbstractNode a2 = new AbstractNode(Children.LEAF);
        VisualizerNode v2 = VisualizerNode.getVisualizer(null, a2);
        Icon icon2 = v2.getIcon(false, false);
        
        assertSame("Icon instances should be same", icon1, icon2);
    }
}
