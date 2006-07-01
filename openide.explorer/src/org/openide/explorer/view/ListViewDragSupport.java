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
package org.openide.explorer.view;

import org.openide.nodes.Node;

import java.awt.Point;
import java.awt.dnd.*;

import javax.swing.JList;


/**
*
* @author Dafe Simonek, Jiri Rechtacek
*/
class ListViewDragSupport extends ExplorerDragSupport {
    // Attributes

    /** Holds selected indices - it's here only
    * as a workaround for sun's bug */

    /*int[] oldSelection;
    int[] curSelection;*/

    // Associations

    /** The view that manages viewing the data in a tree. */
    protected ListView view;

    /** The tree which we are supporting (our client) */
    protected JList list;

    // Operations

    /** Creates new TreeViewDragSupport, initializes gesture */
    public ListViewDragSupport(ListView view, JList list) {
        this.comp = list;
        this.view = view;
        this.list = list;
    }

    int getAllowedDropActions() {
        return view.getAllowedDropActions();
    }

    protected int getAllowedDragActions() {
        return view.getAllowedDragActions();
    }

    /** Initiating the drag */
    public void dragGestureRecognized(DragGestureEvent dge) {
        super.dragGestureRecognized(dge);
    }

    /** Utility method. Returns either selected nodes in the list
    * (if cursor hotspot is above some selected node) or the node
    * the cursor points to.
    * @return Node array or null if position of the cursor points
    * to no node.
    */
    Node[] obtainNodes(DragGestureEvent dge) {
        Point dragOrigin = dge.getDragOrigin();
        int index = list.locationToIndex(dge.getDragOrigin());
        Object obj = list.getModel().getElementAt(index);

        if (obj instanceof VisualizerNode) {
            obj = ((VisualizerNode) obj).node;
        }

        // check conditions
        if ((index < 0)) {
            return null;
        }

        if (!(obj instanceof Node)) {
            return null;
        }

        Node[] result = null;

        if (list.isSelectedIndex(index)) {
            // cursor is above selection, so return all selected indices
            Object[] selected = list.getSelectedValues();
            result = new Node[selected.length];

            for (int i = 0; i < selected.length; i++) {
                if (selected[i] instanceof VisualizerNode) {
                    result[i] = ((VisualizerNode) selected[i]).node;
                } else {
                    if (!(selected[i] instanceof Node)) {
                        return null;
                    }

                    result[i] = (Node) selected[i];
                }
            }
        } else {
            // return only the node the cursor is above
            result = new Node[] { (Node) obj };
        }

        return result;
    }
}
 // end of ListViewDragSupport
