/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pfiala
 */
public class SectionNode extends AbstractNode {

    protected final Object key;
    private boolean expanded = false;
    private SectionNodePanel sectionPanel = null;
    private final String iconBase;
    private final SectionNodeView sectionNodeView;
    private final List allChildren = new LinkedList();

    public SectionNode(SectionNodeView sectionNodeView, boolean isLeaf, Object key, String title, String iconBase) {
        this(sectionNodeView, isLeaf ? Children.LEAF : new Children.Array(), key, title, iconBase);
    }

    public SectionNode(SectionNodeView sectionNodeView, Object key, String title, String iconBase) {
        this(sectionNodeView, false, key, title, iconBase);
    }

    /**
     * Create a new section node with a given child set.
     *
     * @param children
     * @param key
     * @param title
     */
    protected SectionNode(SectionNodeView sectionNodeView, Children children, Object key, String title,
                          String iconBase) {
        super(children);
        this.sectionNodeView = sectionNodeView;
        this.key = key;
        super.setDisplayName(title);
        super.setIconBase(iconBase);
        this.iconBase = iconBase;
        sectionNodeView.registerNode(this);
    }

    public SectionNodeView getSectionNodeView() {
        return sectionNodeView;
    }

    public Object getKey() {
        return key;
    }

    public void addChild(SectionNode node) {
        allChildren.add(node);
        if (!(node instanceof SectionInnerNode)) {
            getChildren().add(new Node[]{node});
        }
    }

    public SectionInnerPanel createInnerPanel() {
        Children children = getChildren();
        if (children.getNodesCount() == 0) {
            return createNodeInnerPanel();
        } else {
            BoxPanel boxPanel = new BoxPanel(sectionNodeView);
            SectionInnerPanel nodeInnerPanel = createNodeInnerPanel();
            if (nodeInnerPanel != null) {
                boxPanel.add(nodeInnerPanel);
            }
            for (Iterator it = allChildren.iterator(); it.hasNext();) {
                SectionNode sectionNode = (SectionNode) it.next();
                if (sectionNode instanceof SectionInnerNode) {
                    boxPanel.add(sectionNode.createInnerPanel());
                } else {
                    boxPanel.add(sectionNode.getSectionNodePanel());
                }
            }
            return boxPanel;
        }
    }

    public boolean canDestroy() {
        return true;
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        return null;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public SectionNodePanel getSectionNodePanel() {
        if (sectionPanel == null) {
            sectionPanel = new SectionNodePanel(this);
            //sectionNodeView.mapSection(this, sectionPanel);
        }
        return sectionPanel;
    }

    public String getIconBase() {
        return iconBase;
    }

    public boolean equals(Object obj) {
        if (getClass() == obj.getClass()) {
            if (key.equals(((SectionNode) obj).key)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return key.hashCode();
    }

    public void dataFileChanged() {
        if (sectionPanel != null) {
            sectionPanel.dataFileChanged();
        }
        Children children = getChildren();
        if (children != null) {
            Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof SectionNode) {
                    ((SectionNode) node).dataFileChanged();
                }
            }
        }
    }
}
