/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.awt.Component;
import java.awt.EventQueue;
import javax.accessibility.AccessibleContext;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import org.openide.awt.HtmlRenderer;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 *
 * @author Marian Petras
 */
final class ResultTreeView extends BeanTreeView implements Runnable {
    
    /** */
    private final TreeCellRenderer defaultTreeCellRenderer;
    /** */
    private final TreeCellRenderer noIconTreeCellRenderer;
    
    /** Creates a new instance of ResultTree */
    ResultTreeView() {
        super();
        defaultTreeCellRenderer = tree.getCellRenderer();
        noIconTreeCellRenderer = createNoIconTreeCellRenderer();
        tree.setCellRenderer(new DelegatingTreeCellRenderer());
        
        initAccessibility();
    }
    
    /**
     */
    private void initAccessibility() {
        AccessibleContext accessibleContext = tree.getAccessibleContext();
        accessibleContext.setAccessibleName(
              NbBundle.getMessage(getClass(), "ACSN_ResultPanelTree")); //NOI18N
        accessibleContext.setAccessibleDescription(
              NbBundle.getMessage(getClass(), "ACSD_ResultPanelTree")); //NOI18N
    }
    
    /**
     *
     */
    private final class DelegatingTreeCellRenderer implements TreeCellRenderer {

        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            boolean isResultRootNode =
                            (value instanceof TreeNode)
                            && (((TreeNode) value).getParent() == null);
            TreeCellRenderer renderer = isResultRootNode
                                        ? noIconTreeCellRenderer
                                        : defaultTreeCellRenderer;
            return renderer.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);
        }

    }
    
    /**
     */
    private TreeCellRenderer createNoIconTreeCellRenderer() {
        HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();
        renderer.setHtml(false);
        renderer.setIcon(null);
        renderer.setIconTextGap(0);
        renderer.setIndent(2);
        return renderer;
    }
    
    /**
     */
    void expandReportNode(TestsuiteNode node) {
        final boolean wasScrollsOnExpand = tree.getScrollsOnExpand();
        
        tree.setScrollsOnExpand(false);
        try {
            expandNode(node);
        } finally {
            if (wasScrollsOnExpand) {
                
                /*
                 * We must post the scrolling-enabling routine to the end of the
                 * event queue, after all the requests for expansion of nodes:
                 */
                EventQueue.invokeLater(this);
            }
        }
    }
    
    /**
     */
    public void run() {
        tree.setScrollsOnExpand(true);
    }
    
}
