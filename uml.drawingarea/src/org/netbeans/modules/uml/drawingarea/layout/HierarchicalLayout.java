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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.uml.drawingarea.layout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author Thomas Wuerthinger
 */
public class HierarchicalLayout extends GraphLayout<IPresentationElement, IPresentationElement> {

    public static final int SWEEP_ITERATIONS = 3;
    public static final int CROSSING_ITERATIONS = 3;    // Options default settings 
    public static final int DUMMY_WIDTH = 1;
    public static final int X_OFFSET = 20;
    public static final int LAYER_OFFSET = 30;    // Options
    private int dummyWidth;
    private int xOffset;
    private int yOffset;
    private int layerCount;    // Variables
    private UniversalGraph<IPresentationElement, IPresentationElement> graph;
    private List<LayoutNode> nodes;
    private Collection<IPresentationElement> nodesSubset = null;
    private HashMap<IPresentationElement, LayoutNode> vertexToLayoutNode;
    private Set<IPresentationElement> reversedLinks;
    private List<LayoutNode>[] layers;
    private boolean animate = false;
    private boolean invert = true;
    private DesignerScene scene;

    public HierarchicalLayout(boolean inverted, int xOffset, int yOffset) {

        dummyWidth = DUMMY_WIDTH;

        // scene is not used yet. It will be used when the container agnostic feature
        // is put into the NBVL

        if (xOffset > 0) {
            this.xOffset = xOffset;
        } else {
            this.xOffset = X_OFFSET;
        }

        if (yOffset > 0) {
            this.yOffset = yOffset;
        } else {
            this.yOffset = LAYER_OFFSET;
        }
        
        this.invert = inverted;
    }

    public HierarchicalLayout(boolean inverted) {
        this(inverted, X_OFFSET, LAYER_OFFSET);
    }

    public HierarchicalLayout() {
        this(false);
    }


    private class LayoutNode {

        public Integer x0local;
        public Integer y0local;
        public Integer x0RootContainer;
        public Integer y0RootContainer;
        public Integer x0Scene;
        public Integer y0Scene;

        public int x;
        public int y;
        public int width;
        public int height;
        public int layer = -1;
        public int xOffset;
        public int yOffset;
        public int bottomYOffset;
        public IPresentationElement vertex; // Only used for non-dummy nodes, otherwise null
        public List<LayoutEdge> preds = new ArrayList<LayoutEdge>();
        public List<LayoutEdge> succs = new ArrayList<LayoutEdge>();
        public int pos = -1; // Position within layer
        public float crossingNumber;

        @Override
        public String toString() {
            return "Node " + vertex;
        }
    }

    private class LayoutEdge {

        public LayoutNode from;
        public LayoutNode to;
        public int relativeFrom;
        public int relativeTo;
        public IPresentationElement link;
    }

    private abstract class AlgorithmPart {

        public void start() {
            run();
        }

        protected abstract void run();
    }

    @Override
    protected void performGraphLayout(UniversalGraph<IPresentationElement, IPresentationElement> graph) {

        this.graph = graph;

        vertexToLayoutNode = new HashMap<IPresentationElement, LayoutNode>();
        reversedLinks = new HashSet<IPresentationElement>();
        nodes = new ArrayList<LayoutNode>();

        // #############################################################
        // Step 1: Build up data structure
        new BuildDatastructure().start();

        // #############################################################
        // STEP 2: Reverse edges, handle backedges
        new ReverseEdges().start();

        // #############################################################
        // STEP 3: Assign layers
        new AssignLayers().start();

        // #############################################################
        // STEP 4: Create dummy nodes
        new CreateDummyNodes().start();

        // #############################################################
        // STEP 5: Crossing Reduction
        new CrossingReduction().start();

        // #############################################################
        // STEP 7: Assign X coordinates
        //new AssignXCoordinates().start();
        new AssignXCoordinates().start();

        // #############################################################
        // STEP 6: Assign Y coordinates
        new AssignYCoordinates().start();

        // #############################################################
        // STEP 8: Write back to interface
        new WriteResult().start();
    }

    @Override
    protected void performNodesLayout(UniversalGraph<IPresentationElement, IPresentationElement> arg0, Collection<IPresentationElement> arg1) {

        this.nodesSubset = arg1;
        this.performGraphLayout(arg0);

    }

    private class BuildDatastructure extends AlgorithmPart {

        protected void run() {
            // Set up nodes
            Collection<IPresentationElement> vertices;
            if (nodesSubset == null) {
                vertices = graph.getNodes();
            } else {
                vertices = nodesSubset;
            }
            for (IPresentationElement v : vertices) {
                LayoutNode node = new LayoutNode();
                Widget w = graph.getScene().findWidget(v);
                assert w != null;
                Rectangle r = w.getBounds();
                if (r == null) {
                    r = w.getPreferredBounds();
                }
                Dimension size = r.getSize();
                node.width = (int) size.getWidth();
                node.height = (int) size.getHeight();
                node.vertex = v;
                nodes.add(node);
                vertexToLayoutNode.put(v, node);
            }

            // Set up edges
            Collection<IPresentationElement> links = graph.getEdges();
            for (IPresentationElement l : links) {
                LayoutEdge edge = new LayoutEdge();
                assert vertexToLayoutNode.containsKey(graph.getEdgeSource(l));
                assert vertexToLayoutNode.containsKey(graph.getEdgeTarget(l));

                if (invert) {
                    edge.to = vertexToLayoutNode.get(graph.getEdgeSource(l));
                    edge.from = vertexToLayoutNode.get(graph.getEdgeTarget(l));
                } else {
                    edge.from = vertexToLayoutNode.get(graph.getEdgeSource(l));
                    edge.to = vertexToLayoutNode.get(graph.getEdgeTarget(l));
                }

                Widget w = graph.getScene().findWidget(graph.getEdgeSource(l));

                assert w != null;
                Rectangle r = w.getBounds();
                if (r == null) {
                    r = w.getPreferredBounds();
                }
                Dimension size = r.getSize();
                edge.relativeFrom = size.width / 2;

                w = graph.getScene().findWidget(graph.getEdgeTarget(l));
                assert w != null;
                r = w.getBounds();
                if (r == null) {
                    r = w.getPreferredBounds();
                }
                size = r.getSize();
                edge.relativeTo = size.width / 2;
                edge.link = l;

                edge.from.succs.add(edge);
                edge.to.preds.add(edge);

            }
        }
    }

    private class ReverseEdges extends AlgorithmPart {

        private HashSet<LayoutNode> visited;
        private HashSet<LayoutNode> active;

        protected void run() {

            // Remove self-edges, TODO: Special treatment
            for (LayoutNode node : nodes) {
                ArrayList<LayoutEdge> succs = new ArrayList<LayoutEdge>(node.succs);
                for (LayoutEdge e : succs) {
                    assert e.from == node;
                    if (e.to == node) {
                        node.succs.remove(e);
                        node.preds.remove(e);
                    }
                }
            }

            // Start DFS and reverse back edges
            visited = new HashSet<LayoutNode>();
            active = new HashSet<LayoutNode>();
            for (LayoutNode node : nodes) {
                DFS(node);
            }
        }

        private void DFS(LayoutNode startNode) {
            if (visited.contains(startNode)) {
                return;
            }
            Stack<LayoutNode> stack = new Stack<LayoutNode>();
            stack.push(startNode);

            while (!stack.empty()) {
                LayoutNode node = stack.pop();

                if (visited.contains(node)) {
                    // Node no longer active
                    active.remove(node);
                    continue;
                }

                // Repush immediately to know when no longer active
                stack.push(node);
                visited.add(node);
                active.add(node);

                ArrayList<LayoutEdge> succs = new ArrayList<LayoutEdge>(node.succs);
                for (LayoutEdge e : succs) {
                    if (active.contains(e.to)) {
                        assert visited.contains(e.to);
                        // Encountered back edge
                        reverseEdge(e);
                    } else if (!visited.contains(e.to)) {
                        stack.push(e.to);
                    }
                }
            }
        }

        private void reverseEdge(LayoutEdge e) {
            assert !reversedLinks.contains(e.link);
            reversedLinks.add(e.link);

            LayoutNode oldFrom = e.from;
            LayoutNode oldTo = e.to;
            int oldRelativeFrom = e.relativeFrom;
            int oldRelativeTo = e.relativeTo;

            e.from = oldTo;
            e.to = oldFrom;
            e.relativeFrom = oldRelativeTo;
            e.relativeTo = oldRelativeFrom;

            oldFrom.succs.remove(e);
            oldFrom.preds.add(e);
            oldTo.preds.remove(e);
            oldTo.succs.add(e);
        }
    }

    private class AssignLayers extends AlgorithmPart {

        protected void run() {
            HashSet<LayoutNode> set = new HashSet<LayoutNode>();
            for (LayoutNode n : nodes) {
                if (n.preds.size() == 0) {
                    set.add(n);
                    n.layer = 0;
                }
            }

            int z = 1;
            HashSet<LayoutNode> newSet = new HashSet<LayoutNode>();
            HashSet<LayoutNode> failed = new HashSet<LayoutNode>();
            while (!set.isEmpty()) {

                newSet.clear();
                failed.clear();

                for (LayoutNode n : set) {

                    for (LayoutEdge se : n.succs) {
                        LayoutNode s = se.to;
                        if (!newSet.contains(s) && !failed.contains(s)) {
                            boolean ok = true;
                            for (LayoutEdge pe : s.preds) {
                                LayoutNode p = pe.from;
                                if (p.layer == -1) {
                                    ok = false;
                                    break;
                                }
                            }

                            if (ok) {
                                newSet.add(s);
                            } else {
                                failed.add(s);
                            }
                        }
                    }

                }

                for (LayoutNode n : newSet) {
                    n.layer = z;
                }

                // Swap sets
                HashSet<LayoutNode> tmp = set;
                set = newSet;
                newSet = tmp;
                z += 1;
            }

            optimize(set);

            layerCount = z - 1;
        }

        public void optimize(HashSet<LayoutNode> set) {

            for (LayoutNode n : set) {
                if (n.preds.size() == 0 && n.succs.size() > 0) {
                    int minLayer = n.succs.get(0).to.layer;
                    for (LayoutEdge e : n.succs) {
                        minLayer = Math.min(minLayer, e.to.layer);
                    }

                    n.layer = minLayer - 1;
                }
            }

        }
    }

    private class CreateDummyNodes extends AlgorithmPart {

        protected void run() {
            ArrayList<LayoutNode> currentNodes = new ArrayList<LayoutNode>(nodes);
            for (LayoutNode n : currentNodes) {
                for (LayoutEdge e : n.succs) {
                    processSingleEdge(e);
                }
            }
        }

        private void processSingleEdge(LayoutEdge e) {
            LayoutNode n = e.from;
            if (e.to.layer > n.layer + 1) {
                LayoutEdge last = e;
                for (int i = n.layer + 1; i < last.to.layer; i++) {
                    last = addBetween(last, i);
                }
            }
        }

        private LayoutEdge addBetween(LayoutEdge e, int layer) {
            LayoutNode n = new LayoutNode();
            n.width = dummyWidth;
            n.height = 0;
            n.layer = layer;
            n.preds.add(e);
            nodes.add(n);
            LayoutEdge result = new LayoutEdge();
            n.succs.add(result);
            result.from = n;
            result.relativeFrom = n.width / 2;
            result.to = e.to;
            result.relativeTo = e.relativeTo;
            e.relativeTo = n.width / 2;
            e.to.preds.remove(e);
            e.to.preds.add(result);
            e.to = n;
            return result;
        }
    }
    private Comparator<LayoutNode> crossingNodeComparator = new Comparator<LayoutNode>() {

        public int compare(LayoutNode n1, LayoutNode n2) {
            float f = n1.crossingNumber - n2.crossingNumber;
            if (f < 0) {
                return -1;
            } else if (f > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    private class CrossingReduction extends AlgorithmPart {

        protected void run() {

            layers = new List[layerCount];

            for (int i = 0; i < layerCount; i++) {
                layers[i] = new ArrayList<LayoutNode>();
            }


            // Generate initial ordering
            HashSet<LayoutNode> visited = new HashSet<LayoutNode>();
            for (LayoutNode n : nodes) {
                if (n.layer == 0) {
                    layers[0].add(n);
                    visited.add(n);
                } else if (n.preds.size() == 0) {
                    layers[n.layer].add(n);
                    visited.add(n);
                }
            }

            for (int i = 0; i < layers.length - 1; i++) {
                for (LayoutNode n : layers[i]) {
                    for (LayoutEdge e : n.succs) {
                        if (!visited.contains(e.to)) {
                            visited.add(e.to);
                            layers[i + 1].add(e.to);
                        }
                    }
                }
            }


            updatePositions();

            // Optimize
            for (int i = 0; i < CROSSING_ITERATIONS; i++) {
                downSweep();
                upSweep();
            }
        }

        private void updatePositions() {

            for (int i = 0; i < layers.length; i++) {
                int z = 0;
                for (LayoutNode n : layers[i]) {
                    n.pos = z;
                    z++;
                }
            }
        }

        private void downSweep() {

            // Downsweep
            for (int i = 1; i < layerCount; i++) {

                for (LayoutNode n : layers[i]) {

                    float sum = 0.0f;
                    for (LayoutEdge e : n.preds) {
                        float cur = e.from.pos;
                        if (e.from.width != 0 && e.relativeFrom != 0) {
                            cur += (float) e.relativeFrom / (float) (e.from.width);
                        }

                        sum += cur;
                    }

                    if (n.preds.size() > 0) {
                        sum /= n.preds.size();
                        n.crossingNumber = sum;
                    //if(n.vertex == null) n.crossingNumber += layers[i].size();
                    }
                }

                Collections.sort(layers[i], crossingNodeComparator);

                int z = 0;
                for (LayoutNode n : layers[i]) {
                    n.pos = z;
                    z++;
                }
            }
        }

        private void upSweep() {
            // Upsweep
            for (int i = layerCount - 1; i >= 0; i--) {

                for (LayoutNode n : layers[i]) {

                    float sum = 0.0f;
                    for (LayoutEdge e : n.succs) {
                        float cur = e.to.pos;
                        if (e.to.width != 0 && e.relativeTo != 0) {
                            cur += (float) e.relativeTo / (float) (e.to.width);
                        }

                        sum += cur;
                    }

                    if (n.succs.size() > 0) {
                        sum /= n.succs.size();
                        n.crossingNumber = sum;
                    //if(n.vertex == null) n.crossingNumber += layers[i].size();
                    }

                }

                Collections.sort(layers[i], crossingNodeComparator);

                int z = 0;
                for (LayoutNode n : layers[i]) {
                    n.pos = z;
                    z++;
                }
            }
        }
    }
    private final Comparator<LayoutNode> nodePositionComparator = new Comparator<LayoutNode>() {

        public int compare(LayoutNode n1, LayoutNode n2) {
            return n1.pos - n2.pos;
        }
    };
    private final Comparator<LayoutNode> nodeProcessingDownComparator = new Comparator<LayoutNode>() {

        public int compare(LayoutNode n1, LayoutNode n2) {
            if (n1.vertex == null) {
                return -1;
            }
            if (n2.vertex == null) {
                return 1;
            }
            return n1.preds.size() - n2.preds.size();
        }
    };
    private final Comparator<LayoutNode> nodeProcessingUpComparator = new Comparator<LayoutNode>() {

        public int compare(LayoutNode n1, LayoutNode n2) {
            if (n1.vertex == null) {
                return -1;
            }
            if (n2.vertex == null) {
                return 1;
            }
            return n1.succs.size() - n2.succs.size();
        }
    };

    private class AssignXCoordinates extends AlgorithmPart {

        private ArrayList<Integer>[] space;
        private ArrayList<LayoutNode>[] downProcessingOrder;
        private ArrayList<LayoutNode>[] upProcessingOrder;

        private void initialPositions() {
            for (LayoutNode n : nodes) {
                n.x = space[n.layer].get(n.pos);
            }
        }

        protected void run() {

            space = new ArrayList[layers.length];
            downProcessingOrder = new ArrayList[layers.length];
            upProcessingOrder = new ArrayList[layers.length];

            for (int i = 0; i < layers.length; i++) {
                space[i] = new ArrayList<Integer>();
                downProcessingOrder[i] = new ArrayList<LayoutNode>();
                upProcessingOrder[i] = new ArrayList<LayoutNode>();

                int curX = 0;
                for (LayoutNode n : layers[i]) {
                    space[i].add(curX);
                    curX += n.width + xOffset;
                    downProcessingOrder[i].add(n);
                    upProcessingOrder[i].add(n);
                }

                Collections.sort(downProcessingOrder[i], nodeProcessingDownComparator);
                Collections.sort(upProcessingOrder[i], nodeProcessingUpComparator);
            }

            initialPositions();
            for (int i = 0; i < SWEEP_ITERATIONS; i++) {
                sweepDown();
                sweepUp();
            }

            sweepDown();
            sweepUp();
        }

        private int calculateOptimalDown(LayoutNode n) {

            List<Integer> values = new ArrayList<Integer>();
            if (n.preds.size() == 0) {
                return n.x;
            }
            for (LayoutEdge e : n.preds) {
                int cur = e.from.x + e.relativeFrom - e.relativeTo;
                values.add(cur);
            }
            return median(values);
        }

        private int calculateOptimalUp(LayoutNode n) {

            List<Integer> values = new ArrayList<Integer>();
            if (n.succs.size() == 0) {
                return n.x;
            }
            for (LayoutEdge e : n.succs) {
                int cur = e.to.x + e.relativeTo - e.relativeFrom;
                values.add(cur);
            }
            return median(values);
        }

        private int median(List<Integer> values) {
            Collections.sort(values);
            if (values.size() % 2 == 0) {
                return (values.get(values.size() / 2 - 1) + values.get(values.size() / 2)) / 2;
            } else {
                return values.get(values.size() / 2);
            }
        }

        private void sweepUp() {
            for (int i = layers.length - 2; i >= 0; i--) {
                NodeRow r = new NodeRow(space[i]);
                for (LayoutNode n : upProcessingOrder[i]) {
                    int optimal = calculateOptimalUp(n);
                    r.insert(n, optimal);
                }
            }
        }

        private void sweepDown() {
            for (int i = 1; i < layers.length; i++) {
                NodeRow r = new NodeRow(space[i]);
                for (LayoutNode n : downProcessingOrder[i]) {
                    int optimal = calculateOptimalDown(n);
                    r.insert(n, optimal);
                }
            }
        }
    }

    private class NodeRow {

        private TreeSet<LayoutNode> treeSet;
        private ArrayList<Integer> space;

        public NodeRow(ArrayList<Integer> space) {
            treeSet = new TreeSet<LayoutNode>(nodePositionComparator);
            this.space = space;
        }

        public int offset(LayoutNode n1, LayoutNode n2) {
            int v1 = space.get(n1.pos) + n1.width;
            int v2 = space.get(n2.pos);
            return v2 - v1;
        }

        public void insert(LayoutNode n, int pos) {

            SortedSet<LayoutNode> headSet = treeSet.headSet(n);
            SortedSet<LayoutNode> tailSet = treeSet.tailSet(n);

            LayoutNode leftNeighbor = null;
            int minX = Integer.MIN_VALUE;
            if (!headSet.isEmpty()) {
                leftNeighbor = headSet.last();
                minX = leftNeighbor.x + leftNeighbor.width + offset(leftNeighbor, n);
            }

            LayoutNode rightNeighbor = null;
            int maxX = Integer.MAX_VALUE;
            if (!tailSet.isEmpty()) {
                rightNeighbor = tailSet.first();
                maxX = rightNeighbor.x - offset(n, rightNeighbor) - n.width;
            }

            assert minX <= maxX;

            if (pos >= minX && pos <= maxX) {
                n.x = pos;
            } else if (Math.abs((long) pos - (long) minX) < Math.abs((long) pos - (long) maxX)) {
                assert minX != Integer.MIN_VALUE;
                n.x = minX;
            } else {
                assert maxX != Integer.MAX_VALUE;
                n.x = maxX;
            }

            treeSet.add(n);
        }
    }

    private class AssignYCoordinates extends AlgorithmPart {

        protected void run() {
            int curY = 0;
            for (int i = 0; i < layers.length; i++) {
                int maxHeight = 0;
                int baseLine = 0;
                int bottomBaseLine = 0;
                for (LayoutNode n : layers[i]) {
                    maxHeight = Math.max(maxHeight, n.height - n.yOffset - n.bottomYOffset);
                    baseLine = Math.max(baseLine, n.yOffset);
                    bottomBaseLine = Math.max(bottomBaseLine, n.bottomYOffset);
                }

                for (LayoutNode n : layers[i]) {
                    if (n.vertex == null) {
                        // Dummy node => set height to line height
                        n.y = curY;
                        n.height = maxHeight + baseLine + bottomBaseLine;
                    } else {
                        n.y = curY + baseLine + (maxHeight - (n.height - n.yOffset - n.bottomYOffset)) / 2 - n.yOffset;
                    }
                }

                curY += maxHeight + baseLine + bottomBaseLine;
                curY += yOffset;
            }
        }
    }

    private class WriteResult extends AlgorithmPart {

        private int pointCount;

        protected void run() {

            HashMap<IPresentationElement, Point> vertexPositions = new HashMap<IPresentationElement, Point>();
            HashMap<IPresentationElement, List<Point>> linkPositions = new HashMap<IPresentationElement, List<Point>>();
            for (IPresentationElement v : graph.getNodes()) {
                LayoutNode n = vertexToLayoutNode.get(v);
                assert !vertexPositions.containsKey(v);
                vertexPositions.put(v, new Point(n.x + n.xOffset, n.y + n.yOffset));
            }

            for (LayoutNode n : nodes) {

                for (LayoutEdge e : n.succs) {
                    if (e.link != null) {
                        IPresentationElement link = e.link;
                        ArrayList<Point> points = new ArrayList<Point>();

                        Point p = new Point(e.from.x + e.relativeFrom, e.from.y + e.from.height - e.from.bottomYOffset);
                        points.add(p);

                        LayoutNode cur = e.to;
                        LayoutEdge curEdge = e;
                        while (cur.vertex == null && cur.succs.size() != 0) {

                            points.add(new Point(cur.x + cur.width / 2, cur.y));

                            points.add(new Point(cur.x + cur.width / 2, cur.y + cur.height));
                            if (cur.succs.size() == 0) {
                                break;
                            }
                            assert cur.succs.size() == 1;
                            curEdge = cur.succs.get(0);
                            cur = curEdge.to;
                        }

                        p = new Point(cur.x + curEdge.relativeTo, cur.y + cur.yOffset);
                        points.add(p);

                        if (reversedLinks.contains(link)) {
                            Collections.reverse(points);
                        }

                        linkPositions.put(e.link, points);

                        // No longer needed!
                        e.link = null;
                    }
                }
            }

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            for (IPresentationElement v : vertexPositions.keySet()) {
                Point p = vertexPositions.get(v);
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
            }

            for (IPresentationElement l : linkPositions.keySet()) {
                List<Point> points = linkPositions.get(l);
                for (Point p : points) {
                    if (p != null) {
                        minX = Math.min(minX, p.x);
                        minY = Math.min(minY, p.y);
                    }
                }

            }

            for (IPresentationElement v : vertexPositions.keySet()) {
                Point p = vertexPositions.get(v);
                p.x -= minX;
                p.y -= minY;
                Widget w = graph.getScene().findWidget(v);
                if (animate) {
                    graph.getScene().getSceneAnimator().animatePreferredLocation(w, p);
                } else {
                    w.setPreferredLocation(p);
                }
            }

            for (IPresentationElement l : linkPositions.keySet()) {
                List<Point> points = linkPositions.get(l);

                for (Point p : points) {
                    if (p != null) {
                        p.x -= minX;
                        p.y -= minY;
                    }
                }

                //Kris - this is a hack to reverse the order of the control points
                // that were created by the algorithm. This is used when the graph
                // is inverted.
                if (invert && points.size() > 3) {
                    int numPoints = points.size();
                    ArrayList<Point> invertedPoints = new ArrayList<Point>(numPoints);

                    invertedPoints.add(points.get(0));

                    for (int i = numPoints - 2; i > 0; i--) {
                        invertedPoints.add(points.get(i));
                    }

                    invertedPoints.add(points.get(numPoints - 1));

                    points = invertedPoints;
                }

                Widget w = graph.getScene().findWidget(l);
                if (w instanceof ConnectionWidget) {
                    ConnectionWidget cw = (ConnectionWidget) w;
                    cw.setControlPoints(points, true);
                }

            }
            
            graph.getScene().validate();
            graph.getScene().repaint();
            graph.getScene().revalidate();
        }
    }
}
