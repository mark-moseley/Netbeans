/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.Utils;
import org.openide.nodes.Node;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author pfiala
 *         <p/>
 *         The SectionNodePanel shows data of related SectionNode object
 *         which contains all information about the section
 */
public class SectionNodePanel extends SectionPanel {

    public SectionNodePanel(final SectionNode node) {
        super(node.getSectionNodeView(), node, node.getDisplayName(), node);
        if (node.getKey() instanceof SectionView) {
            // the section corresponding to the top level node is always expanded
            setInnerViewMode();
        } else if (node.isExpanded()) {
            setExpandedViewMode();
        }
        node.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (Node.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                    setTitle(node.getDisplayName());
                }
            }
        });
    }

    /**
     * The expanded viev mode shows only title bar and border around inner panel,
     * The inner panel is always visible and the section cannot be collapsed
     */
    protected void setExpandedViewMode() {
        getTitleButton().setVisible(true);
        getFoldButton().setVisible(false);
        getHeaderSeparator().setVisible(false);
        Border emptyBorder = new EmptyBorder(0, 4, 4, 4);
        Border lineBorder;
        lineBorder = new JTextField().getBorder();

        setBorder(new CompoundBorder(emptyBorder, new CompoundBorder(lineBorder, emptyBorder)));
        openInnerPanel();
        getFillerLine().setVisible(false);
        getFillerEnd().setVisible(false);
    }

    /**
     * The inner view mode shows only inner panel.
     * The inner panel is always visible and the section cannot be collapsed
     */
    protected void setInnerViewMode() {
        getTitleButton().setVisible(false);
        getFoldButton().setVisible(false);
        getHeaderSeparator().setVisible(false);
        openInnerPanel();
        getFillerLine().setVisible(false);
        getFillerEnd().setVisible(false);
    }

    /**
     * Creation of inner panel using related SectionNode object
     *
     * @return newly created inner panel
     */
    protected SectionInnerPanel createInnerpanel() {
        SectionInnerPanel innerPanel = ((SectionNode) getNode()).createInnerPanel();
        if (innerPanel == null) {
            // This case arises only if the inner panel has not been implemented yet.
            // Then we show empty panel.
            innerPanel = new BoxPanel(((SectionNode) getNode()).getSectionNodeView());
        }
        return innerPanel;
    }

    protected void openInnerPanel() {
        super.openInnerPanel();
        Node[] childNodes = ((SectionNode) getNode()).getChildren().getNodes();
        if (childNodes != null && childNodes.length > 0) {
            final SectionNodePanel panel = ((SectionNode) childNodes[0]).getSectionNodePanel();
            panel.getFoldButton().setSelected(true);
            panel.openInnerPanel();
        }
    }

    protected void closeInnerPanel() {
        if (getFoldButton().isVisible()) {
            if (getSectionView().getActivePanel() == this) {
                Container parent = getParent();
                while (parent != null) {
                    if (parent instanceof SectionPanel) {
                        final SectionPanel sectionPanel = (SectionPanel) parent;
                        Utils.runInAwtDispatchThread(new Runnable() {
                            public void run() {
                                sectionPanel.setActive(true);
                            }
                        });
                        break;
                    } else {
                        parent = parent.getParent();
                    }
                }
            }
            super.closeInnerPanel();
        }
    }

    /**
     * Method of NodeSectionPanel interface
     */
    public void open() {
        Node parentNode = getNode().getParentNode();
        if (parentNode instanceof SectionNode) {
            ((SectionNode) parentNode).getSectionNodePanel().open();
        }
        if (getInnerPanel() == null) {
            super.open();
        }
    }
}
