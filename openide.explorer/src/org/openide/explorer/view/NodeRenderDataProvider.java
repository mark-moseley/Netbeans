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

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 *
 * @author David Strupl
 */
class NodeRenderDataProvider implements RenderDataProvider {
    
    private JTable table;
    
    /** Creates a new instance of NodeRenderDataProvider */
    public NodeRenderDataProvider(JTable table) {
        this.table = table;
    }

    public java.awt.Color getBackground(Object o) {
        return table.getBackground();
    }

    public String getDisplayName(Object o) {
        Node n = Visualizer.findNode(o);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + o + " of class " + o.getClass().getName());
        }
        return n.getDisplayName();
    }

    public java.awt.Color getForeground(Object o) {
        return table.getForeground();
    }

    public javax.swing.Icon getIcon(Object o) {
        Node n = Visualizer.findNode(o);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + o + " of class " + o.getClass().getName());
        }
        boolean expanded = false;
        if (o instanceof TreeNode) {
            TreeNode tn = (TreeNode)o;
            ArrayList al = new ArrayList();
            while (tn != null) {
                al.add(tn);
                tn = tn.getParent();
            }
            Collections.reverse(al);
            TreePath tp = new TreePath(al.toArray());
            AbstractLayoutCache layout = ((Outline)table).getLayoutCache();
            expanded = layout.isExpanded(tp);
        }
        java.awt.Image image = null;
        if (expanded) {
            image = n.getOpenedIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        } else {
            image = n.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        }
        return new ImageIcon(image);
    }

    public String getTooltipText(Object o) {
        Node n = Visualizer.findNode(o);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + o + " of class " + o.getClass().getName());
        }
        return n.getShortDescription();
    }

    public boolean isHtmlDisplayName(Object o) {
        return false;
    }
    
}
