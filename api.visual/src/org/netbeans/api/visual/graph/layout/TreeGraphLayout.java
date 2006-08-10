/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.graph.layout;

import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.*;

/**
 * @author David Kaspar
 */
public class TreeGraphLayout<N, E> {

    private GraphScene<N, E> scene;
    private int originX;
    private int originY;
    private int verticalGap;
    private int horizontalGap;
    private boolean vertical;

    public TreeGraphLayout (GraphScene<N, E> scene, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        this.scene = scene;
        this.originX = originX;
        this.originY = originY;
        this.verticalGap = verticalGap;
        this.horizontalGap = horizontalGap;
        this.vertical = vertical;
    }

    public final void layout (N rootNode) {
        if (rootNode == null)
            return;
        Collection<N> allNodes = scene.getNodes ();
        ArrayList<N> nodesToResolve = new ArrayList<N> (allNodes);

        HashSet<N> loadedSet = new HashSet<N> ();
        Node root = new Node (rootNode, loadedSet);
        nodesToResolve.removeAll (loadedSet);
        if (vertical) {
            root.allocateHorizontally ();
            root.resolveVertically (originX, originY);
        } else {
            root.allocateVertically ();
            root.resolveHorizontally (originX, originY);
        }

        final HashMap<N, Point> resultPosition = new HashMap<N, Point> ();
        root.upload (resultPosition);

        for (N node : nodesToResolve) {
            Point position = new Point ();
            // TODO - resolve others
            resultPosition.put (node, position);
        }

        for (Map.Entry<N, Point> entry : resultPosition.entrySet ())
            scene.findWidget (entry.getKey ()).setPreferredLocation (entry.getValue ());
    }

    protected Collection<N> resolveChildren (N node) {
        Collection<E> edges = scene.findNodeEdges (node, false, true);
        HashSet<N> nodes = new HashSet<N> ();
        for (E edge : edges)
            nodes.add (scene.getEdgeTarget (edge));
        return nodes;
    }

    private class Node {

        private N node;
        private ArrayList<Node> children;

        private Rectangle relativeBounds;
        private int space;
        private int totalSpace;
        private Point point;

        public Node (N node, HashSet<N> loadedSet) {
            this.node = node;
            loadedSet.add (node);

            Collection<N> list = resolveChildren (node);
            children = new ArrayList<Node> ();
            for (N child : list)
                if (! loadedSet.contains (child))
                    children.add (new Node (child, loadedSet));
        }

        public int allocateHorizontally () {
            Widget widget = scene.findWidget (node);
            widget.getLayout ().layout (widget);
            relativeBounds = widget.getPreferredBounds ();
            space = 0;
            for (int i = 0; i < children.size (); i++) {
                if (i > 0)
                    space += horizontalGap;
                space += children.get (i).allocateHorizontally ();
            }
            totalSpace = Math.max (space, relativeBounds.width);
            return totalSpace;
        }

        public void resolveVertically (int x, int y) {
            point = new Point (x + totalSpace / 2, y - relativeBounds.y);
            x += (totalSpace - space) / 2;
            y += relativeBounds.height + verticalGap;
            for (Node child : children) {
                child.resolveVertically (x, y);
                x += child.totalSpace + horizontalGap;
            }
        }

        public int allocateVertically () {
            Widget widget = scene.findWidget (node);
            widget.getLayout ().layout (widget);
            relativeBounds = widget.getPreferredBounds ();
            space = 0;
            for (int i = 0; i < children.size (); i++) {
                if (i > 0)
                    space += verticalGap;
                space += children.get (i).allocateVertically ();
            }
            totalSpace = Math.max (space, relativeBounds.height);
            return totalSpace;
        }

        public void resolveHorizontally (int x, int y) {
            point = new Point (x - relativeBounds.x, y + totalSpace / 2);
            x += relativeBounds.width + horizontalGap;
            y += (totalSpace - space) / 2;
            for (Node child : children) {
                child.resolveHorizontally (x, y);
                y += child.totalSpace + verticalGap;
            }
        }

        public void upload (HashMap<N, Point> result) {
            result.put (node, point);
            for (Node child : children)
                child.upload (result);
        }
    }

}
