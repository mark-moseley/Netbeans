/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.model;


import org.netbeans.core.windows.*;
import org.openide.ErrorManager;

import java.util.*;


/**
 * Sub-model of split (n-branch) tree, which represents structure
 * of split components. It's used in ModesModel as representation
 * of split modes, and also as a representation of editor area, 
 * which is in fact the same, just it is inside the enclosed splits.
 *
 * @author  Peter Zavadsky
 */
class SplitSubModel {

    /** Parent model instance. */
    protected final Model parentModel;
    
    /** Maps modes to nodes of this n-branch tree model. */
    private final Map modes2nodes = new WeakHashMap(20);
    
    /** Root <code>Node</code> which represents the split panes structure
     * with modes as leaves. */
    protected Node root;
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(SplitSubModel.class);

    
    /** Creates a new instance of SplitModel */
    public SplitSubModel(Model parentModel) {
        this.parentModel = parentModel;
    }

    
    private ModeNode getModeNode(ModeImpl mode) {
        synchronized(modes2nodes) {
            ModeNode node = (ModeNode)modes2nodes.get(mode);
            if(node == null) {
                node = new ModeNode(mode);
                modes2nodes.put(mode, node);
            }
        
            return node;
        }
    }
    
    public SplitConstraint[] getModelElementConstraints(ModelElement element) {
        if(element instanceof Node) {
            Node node = (Node)element;
            if(!isInTree(node)) {
                return null;
            }
            return node.getNodeConstraints();
        }
        
        return null;
    }
    
    public SplitConstraint[] getModeConstraints(ModeImpl mode) {
        ModeNode modeNode = getModeNode(mode);
        return modeNode.getNodeConstraints();
    }
    
    public boolean addMode(ModeImpl mode, SplitConstraint[] constraints) {
        return addMode(mode, constraints, false);
    }
    
    /** Adds mode which is <code>Node</code>
     * with specified constraints designating the path in model.
     * <em>Note: It is important to know that adding of mode can affect the structure
     * the way, it can change constraints of already added modes (they could
     * be moved in that tree)</em> */
    public boolean addMode(ModeImpl mode, SplitConstraint[] constraints, boolean adjustToAllWeights) {
        // PENDING do we support empty constraints?
        if(mode == null || constraints == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalArgumentException("Mode=" + mode
                    + " constraints=" + constraints));
            return false;
        }

        
        Node modeNode = getModeNode(mode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to tree=" + mode); // NOI18N
            debugLog("constraints=" + Arrays.asList(constraints)); // NOI18N
            debugLog("modeNode=" + modeNode); // NOI18N
        }

        return addNodeToTree(modeNode, constraints, adjustToAllWeights);
    }
    
    // XXX
    public boolean addModeToSide(ModeImpl mode, ModeImpl attachMode, String side) {
        if(mode == null || mode.getState() == Constants.MODE_STATE_SEPARATED) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalArgumentException("Mode=" + mode));
            return false;
        }

        Node modeNode = getModeNode(mode);
        Node attachModeNode = getModeNode(attachMode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to between=" + mode); // NOI18N
            debugLog("attachMode=" + attachMode); // NOI18N
            debugLog("side=" + side); // NOI18N
        }

        return addNodeToTreeToSide(modeNode, attachModeNode, side);
    }
    
    // XXX
    public boolean addModeBetween(ModeImpl mode, ModelElement firstElement, ModelElement secondElement) {
        if(mode == null || mode.getState() == Constants.MODE_STATE_SEPARATED) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalArgumentException("Mode=" + mode));
            return false;
        }

        Node modeNode = getModeNode(mode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to between=" + mode); // NOI18N
            debugLog("firstElement=" + firstElement); // NOI18N
            debugLog("secondElement=" + secondElement); // NOI18N
        }

        return addNodeToTreeBetween(modeNode, firstElement, secondElement);
    }

    // XXX
    public boolean addModeAround(ModeImpl mode, String side) {
        if(mode == null || mode.getState() == Constants.MODE_STATE_SEPARATED) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalArgumentException("Mode=" + mode));
            return false;
        }

        Node modeNode = getModeNode(mode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to around=" + mode); // NOI18N
            debugLog("side=" + side); // NOI18N
        }

        return addNodeToTreeAround(modeNode, side);
    }
    
    // XXX
    public boolean addModeAroundEditor(ModeImpl mode, String side) {
        if(mode == null || mode.getState() == Constants.MODE_STATE_SEPARATED) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalArgumentException("Mode=" + mode));
            return false;
        }

        Node modeNode = getModeNode(mode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to around=" + mode); // NOI18N
            debugLog("side=" + side); // NOI18N
        }

        return addNodeToTreeAroundEditor(modeNode, side);
    }

    private boolean isInTree(Node descendant) {
        if(root == null) {
            return false;
        }
        
        if(descendant == root) {
            return true;
        }
        
        Node parent = descendant.getParent();
        while(parent != null) {
            if(parent == root) {
                return true;
            }
            parent = parent.getParent();
        }
        
        return false;
    }

    /** Adds node into the tree structure if there isn't yet. */
    protected boolean addNodeToTree(Node addingNode, SplitConstraint[] constraints,
    boolean adjustToAllWeights) {
        if(isInTree(addingNode)) {
            return false;
        }
        
        // Find starting split.
        SplitNode splitNode;
        // First solve root.
        if(root == null) {
            if(constraints.length == 0) {
                root = addingNode;
                return true;
            }
            
            // There is nothing, create split.
            splitNode = new SplitNode(constraints[0].orientation);
            root = splitNode;
        } else if(root instanceof SplitNode) {
            splitNode = (SplitNode)root;
        } else {
            // All other nodes (ModeNode, and EditorNode in subclass).
            splitNode = new SplitNode(0); // Default orientation when splitting root?
            splitNode.setChildAt(-1, 0.5D, root);
            root = splitNode;
        }

        // Traverse the structure.
        for(int level = 0; level < constraints.length; level++) {
            int orientation   = constraints[level].orientation;

            // First solve orientation
            if(orientation != splitNode.getOrientation()) {
                // Orientation doesn't fit, create new split.
                SplitNode newSplit = new SplitNode(orientation);
                if(splitNode == root) {
                    // Creating new branch.
                    newSplit.setChildAt(-1, 0.5D, splitNode);
                    root = newSplit;
                } else {
                    SplitNode parent = splitNode.getParent();
                    int   oldIndex       = parent.getChildIndex(splitNode);
                    double oldSplitWeight = parent.getChildSplitWeight(splitNode);
                    // move the original split as child of new one and newSplit put under parent.
                    parent.removeChild(splitNode);

                    // Creating new branch.
                    newSplit.setChildAt(-1, 0.5D, splitNode);
                    parent.setChildAt(oldIndex, oldSplitWeight, newSplit);
                }
                
                splitNode = newSplit;
            }
            
            // Then solve next position (together with splitWeight).
            // But if this is the last iteration, don't do anything the adding will be done after loop.
            if(level < constraints.length - 1) {
                int index         = constraints[level].index;
                double splitWeight = constraints[level].splitWeight;

                Node child = splitNode.getChildAt(index);
                if(child instanceof SplitNode) {
                    // Traverse to split.
                    // Possible wrong orientation solves next iteration (see above).
                    splitNode = (SplitNode)child;
                } else {
                    // There is some leaf node or null, just create new split that way.
                    SplitNode newSplit = new SplitNode(constraints[level + 1].orientation);
                    splitNode.setChildAt(index, splitWeight, newSplit);
                    splitNode = newSplit;
                }
            }
        }
        
        // Finally add the node into tree.
        if(constraints.length == 0) {
            splitNode.setChildAt(-1, 0.5D, addingNode, adjustToAllWeights);
        } else {
            splitNode.setChildAt(
                constraints[constraints.length - 1].index,
                constraints[constraints.length - 1].splitWeight,
                addingNode,
                adjustToAllWeights
            );
        }
        
        verifyNode(root);
        
        return true;
    }

    // XXX
    private boolean addNodeToTreeToSide(Node addingNode, Node attachNode, String side) {
        if(isInTree(addingNode)) {
            return false;
        }

        if(!isInTree(attachNode)) {
            return false;
        }
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("Inserting to side="+side); // NOI18N
        }

        // Update
        if(attachNode == root) {
            int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
            int oldIndex = addingIndex == 0 ? -1 : 0;
            // Create new branch.
            int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
            SplitNode newSplit = new SplitNode(orientation);
            newSplit.setChildAt(addingIndex, Constants.DROP_TO_SIDE_RATIO, addingNode);
            newSplit.setChildAt(oldIndex, 1D - Constants.DROP_TO_SIDE_RATIO, attachNode);
            root = newSplit;
        } else {
            SplitNode parent = attachNode.getParent();
            if(parent == null) {
                return false;
            }

            int attachIndex = parent.getChildIndex(attachNode);
            double attachWeight = parent.getChildSplitWeight(attachNode);
            // Create new branch.
            int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
            SplitNode newSplit = new SplitNode(orientation);
            parent.removeChild(attachNode);
            int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
            int oldIndex = addingIndex == 0 ? -1 : 0;
            newSplit.setChildAt(addingIndex, Constants.DROP_TO_SIDE_RATIO, addingNode);
            newSplit.setChildAt(oldIndex, 1D - Constants.DROP_TO_SIDE_RATIO, attachNode);
            parent.setChildAt(attachIndex, attachWeight, newSplit);
        }
        
        return true;
    }
    
    // XXX
    private boolean addNodeToTreeBetween(Node addingNode,
    ModelElement firstElement, ModelElement secondElement) {
        if(isInTree(addingNode)) {
            return false;
        }
        
        if(!(firstElement instanceof Node)
        || !(secondElement instanceof Node)) {
            return false;
        }
        
        Node first = (Node)firstElement;
        Node second = (Node)secondElement;
        
        if(!isInTree(first) || !isInTree(second)) {
            return false;
        }
        
        //  Adjust first and second to common parent.
        List firstParents = new ArrayList();
        Node node = first;
        while(node.getParent() != null) {
            Node firstParent = node.getParent();
            firstParents.add(firstParent);
            node = firstParent;
        }
        node = second;
        while(node.getParent() != null) {
            Node secondParent = node.getParent();
            if(firstParents.contains(secondParent)) {
                second = node;
                int index = firstParents.indexOf(secondParent);
                if(index > 0) {
                    first = (Node)firstParents.get(index - 1);
                }
                break;
            } else {
                node = secondParent;
            }
        }
        if(first.getParent() != second.getParent()) {
            return false;
        }
        SplitNode parent = first.getParent();
        
        // Update

        List visibleChildren = parent.getVisibleChildren();
        // Prepare how to distribute the rest of weights.
        double visibleResizeWeights = 0D;
        for(Iterator it = visibleChildren.iterator(); it.hasNext(); ) {
            Node next = (Node)it.next();
            visibleResizeWeights += next.getResizeWeight();
        }
        
        List invisibleChildren = parent.getChildren();
        invisibleChildren.removeAll(visibleChildren);
        double invisibleWeights = 0D;
        for(Iterator it = invisibleChildren.iterator(); it.hasNext(); ) {
            Node next = (Node)it.next();
            invisibleWeights += parent.getChildSplitWeight(next);
        }

        // Get the refined weights to work with.
        Map visibleChild2refinedWeight = new HashMap();
        for(Iterator it = visibleChildren.iterator(); it.hasNext(); ) {
            Node next = (Node)it.next();
            double refinedWeight;
            if(visibleResizeWeights > 0D) {
                refinedWeight = parent.getChildSplitWeight(next) + ((next.getResizeWeight() / visibleResizeWeights) * invisibleWeights);
            } else {
                refinedWeight = parent.getChildSplitWeight(next);
            }
            visibleChild2refinedWeight.put(next, new Double(refinedWeight));
        }
        
        double firstWeight = ((Double)visibleChild2refinedWeight.get(first)).doubleValue();
        double secondWeight = ((Double)visibleChild2refinedWeight.get(second)).doubleValue();
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("Inserting between"); // NOI18N
            debugLog("firstWeight="+firstWeight); // NOI18N
            debugLog("secondWeight="+secondWeight); // NOI18N
        }
        
        int index = parent.getChildIndex(second);
        double newWeight = firstWeight * Constants.DROP_BETWEEN_RATIO + secondWeight * Constants.DROP_BETWEEN_RATIO;

        parent.setChildSplitWeight(first, parent.getChildSplitWeight(first) - firstWeight * Constants.DROP_BETWEEN_RATIO);
        parent.setChildSplitWeight(second, parent.getChildSplitWeight(second) - secondWeight * Constants.DROP_BETWEEN_RATIO);
        parent.setChildAt(index, newWeight, addingNode);
        
        return true;
    }

    // XXX
    private boolean addNodeToTreeAround(Node addingNode, String side) {
        Node top = root;
        
        if(top instanceof SplitNode) {
            SplitNode parent = (SplitNode)top;
            
            if((parent.getOrientation() == Constants.VERTICAL
                && (side == Constants.TOP || side == Constants.BOTTOM))
            || (parent.getOrientation() == Constants.HORIZONTAL
                && (side == Constants.LEFT || side == Constants.RIGHT))) {
                    // Has the needed orientation (no new branch).
                double splitWeights = 0D;
                for(Iterator it = parent.getChildren().iterator(); it.hasNext(); ) {
                    Node next = (Node)it.next();
                    splitWeights += parent.getChildSplitWeight(next);
                }

                double addingSplitWeight = splitWeights * Constants.DROP_AROUND_RATIO;
                int index = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
                
                parent.setChildAt(index, addingSplitWeight, addingNode);
                if(addingSplitWeight > 1D) {
                    double ratio = 1D/addingSplitWeight;
                    parent.normalizeWeights(ratio);
                }
                return true;
            } else {
                // Create new branch.
                int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
                SplitNode newSplit = new SplitNode(orientation);
                int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
                int oldIndex = addingIndex == 0 ? -1 : 0;
                newSplit.setChildAt(addingIndex, Constants.DROP_AROUND_RATIO, addingNode);
                newSplit.setChildAt(oldIndex, 1D - Constants.DROP_AROUND_RATIO, parent);
                root = newSplit;
                return true;
            }
        }
        
        SplitConstraint[] newConstraints; // Adding constraint to new mode.
        if(side == Constants.TOP) {
            newConstraints = new SplitConstraint[] {new SplitConstraint(Constants.VERTICAL, 0, Constants.DROP_AROUND_RATIO)};
        } else if(side == Constants.BOTTOM) {
            newConstraints = new SplitConstraint[] {new SplitConstraint(Constants.VERTICAL, -1, Constants.DROP_AROUND_RATIO)};
        } else if(side == Constants.LEFT) {
            newConstraints = new SplitConstraint[] {new SplitConstraint(Constants.HORIZONTAL, 0, Constants.DROP_AROUND_RATIO)};
        } else if(side == Constants.RIGHT) {
            newConstraints = new SplitConstraint[] {new SplitConstraint(Constants.HORIZONTAL, -1, Constants.DROP_AROUND_RATIO)};
        } else {
            // XXX wrong side
            return false;
        }

        return addNodeToTree(addingNode, newConstraints, false);
    }
    
    // XXX
    protected boolean addNodeToTreeAroundEditor(Node addingNode, String side) {
        // XXX No op here, it's impelmented in editor split subclass.
        return false;
    }

    
    /** Removes specified mode as <code>Node</code> from this model. */
    public boolean removeMode(ModeImpl mode) {
        if(mode == null) {
            throw new NullPointerException("Cannot remove null mode!");
        }

        return removeNodeFromTree(getModeNode(mode));
    }

    /** Removes node from this tree. */
    protected boolean removeNodeFromTree(Node node) {
        if(!isInTree(node)) {
            return false;
        }
        
        SplitNode parent = node.getParent();
        if(parent == null && node != root) {
            // PENDING incorrect state?
            return false;
        } 

        if(node == root) {
            root = null;
        } else {
            parent.removeChild(node);

            if(parent.getChildren().isEmpty()) {
                // Parent split is empty, remove it too.
                if(parent == root) {
                    root = null;
                } else {                
                    SplitNode grandParent = parent.getParent();
                    grandParent.removeChild(parent);
                }
            }
        }

        verifyNode(root);
        
        return true;
    }

    // PENDING Currently verifies parent-child links only.
    /** Verifies the tree structure. */
    private /*static*/ void verifyNode(Node node) {
        if(node instanceof SplitNode) {
            SplitNode splitNode = (SplitNode)node;
            for(Iterator it = splitNode.getChildren().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();

                if(child.getParent() != splitNode) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                        new IllegalStateException("Node->"+child
                            +" has wrong parent->"+child.getParent()
                            + " is has to be->"+splitNode
                            + " \nModel: " + toString()));
                    // Repair model.
                    child.setParent(splitNode);
                }

                verifyNode(child);
            }
        }
    }
    
    /** Resets model. Removes all nodes. */
    public void reset() {
        detachNodes(root);
        root = null;
    }

    /** Detaches nodes tree from itself. */
    private static void detachNodes(Node node) {
        if(node instanceof SplitNode) {
            SplitNode splitNode = (SplitNode)node;
            
            for(Iterator it = splitNode.getChildren().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();
                splitNode.removeChild(child);
                detachNodes(child);
            }
        }
    }

    public boolean setSplitWeights(ModelElement firstElement, double firstSplitWeight,
    ModelElement secondElement, double secondSplitWeight) {
        Node firstNode = (Node)firstElement;
        Node secondNode = (Node)secondElement;
        
        if(firstNode == null || secondNode == null
        || (firstNode.getParent() != secondNode.getParent())) {
            return false;
        }
        
        SplitNode parent = firstNode.getParent();
        
        if(parent == null || !isInTree(parent)) {
            return false;
        }
       
        parent.setChildSplitWeight(firstNode, firstSplitWeight);
        parent.setChildSplitWeight(secondNode, secondSplitWeight);
        
        return true;
    }

    /** */
    public ModeStructureSnapshot.ElementSnapshot createSplitSnapshot() {
        return root == null ? null : root.createSnapshot();
    }
    
    public Set createSeparateSnapshots() {
        return findSeparateModeSnapshots(root);
    }
    
    private Set findSeparateModeSnapshots(Node node) {
        Set s = new HashSet();
        if(node instanceof ModeNode) {
            ModeNode modeNode = (ModeNode)node;
            if(modeNode.isVisibleSeparate()) {
                s.add(modeNode.createSnapshot());
            }
        } else if(node instanceof SplitNode) {
            SplitNode splitNode = (SplitNode)node;
            for(Iterator it = splitNode.getChildren().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();
                s.addAll(findSeparateModeSnapshots(child));
            }
        }
        
        return s;
    }
    
    /** Overrides superclass method, adds dump of this model tree. */
    public String toString() {
        // PENDING Better method name, some refinements possible of the dump.
        return dumpNode(root, 0, null);
    }
    
    /** Recursively dump tree content */
    private static String dumpNode(Node node, int ind, String state) {
        ind++;
        if (node == null) {
            return "NULL NODE\n";
        }
        StringBuffer buffer = new StringBuffer();
        if(state == null) {
            buffer.append("\n");
        }
        StringBuffer sb = getOffset(ind);
        if(node instanceof ModeNode) {
            buffer.append(sb);
            buffer.append("<mode-node"); // NOI18N
            buffer.append(" [" + Integer.toHexString(System.identityHashCode(node)) + "]"); // NOI18N
            buffer.append(" index=\""); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildIndex(node));
            }
            buffer.append(" splitWeight="); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildSplitWeight(node));
            }
            buffer.append("\""); // NOI18N
            buffer.append(" state=\""); // NOI18N
            buffer.append(state);
            buffer.append("\""); // NOI18N
            buffer.append(" name=\"" + ((ModeNode)node).getMode().getName() + "\""); // NOI18N
            buffer.append(" parent="); // NOI18N
            buffer.append(node.getParent() == null ? null : "["+Integer.toHexString(node.getParent().hashCode())+"]");
            buffer.append(" constraints=\'" + java.util.Arrays.asList(node.getNodeConstraints()) + "\"");
            buffer.append("</mode-node>\n"); // NOI18N
        } else if(node instanceof SplitNode) {
            buffer.append(sb);
            buffer.append("<split-node"); // NOI18N
            buffer.append(" [" + Integer.toHexString(System.identityHashCode(node)) + "]"); // NOI18N
            buffer.append(" index=\""); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildIndex(node));
            }
            buffer.append(" splitWeight="); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildSplitWeight(node));
            }
            buffer.append("\""); // NOI18N
            SplitNode split = (SplitNode) node;
            buffer.append(" state=\""); // NOI18N
            buffer.append(state);
            buffer.append("\" orientation=\""); // NOI18N
            buffer.append(split.getOrientation());
            buffer.append("\">\n");
            int j = 0;
            for(Iterator it = split.getChildren().iterator(); it.hasNext(); j++ ) {
                Node child = (Node)it.next();
                buffer.append(dumpNode(child, ind, "child["+j+"]"));
            }
            buffer.append(sb);
            buffer.append("</split-node>\n"); // NOI18N
        } else {
            // supposing it's editor mode.
            buffer.append(sb);
            buffer.append("<editor-node"); // NOI18N
            buffer.append(" [" + Integer.toHexString(System.identityHashCode(node)) + "]"); // NOI18N
            buffer.append(" index=\""); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildIndex(node));
            }
            buffer.append("\""); // NOI18N
            buffer.append(" splitWeight="); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildSplitWeight(node));
            }
            buffer.append(" parent="); // NOI18N
            buffer.append(node.getParent() == null ? null : "["+Integer.toHexString(node.getParent().hashCode())+"]");
            buffer.append("</editor-node>\n"); // NOI18N
        }
        return buffer.toString();
    }
    
    private static StringBuffer getOffset (int ind) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ind - 1; i++) {
            sb.append("\t"); // NOI18N
        }
        return sb;
    }

    ///////////////////////////////
    // Controller updates >>
    
    public ModeImpl getModeForOriginator(ModelElement originator) {
        if(originator instanceof ModeNode) {
            return ((ModeNode)originator).getMode();
        }
        
        return null;
    }
    
    // Controller updates <<
    ///////////////////////////////
    
    private static void debugLog(String message) {
        Debug.log(SplitSubModel.class, message);
    }
    
    ////////////////////////////////////////
    /// Nodes of this tree model
    ////////////////////////////////////////
    /** Class representing one node in SplitSubModel.  */
    protected static abstract class Node implements ModelElement {
        /** Reference to parent node. */
        private SplitNode parent;

        /** Creates a new instance of TreeNode. */
        public Node() {
        }

        /** Overrides superclass method, adds info about parent node. */
        public String toString() {
            return super.toString()
                + "[parent=" + (parent == null // NOI18N
                    ? null
                    : (parent.getClass() + "@" // NOI18N
                            + Integer.toHexString(parent.hashCode())))
                + "]"; // NOI18N
        }

        /** Setter of parent property. */
        public void setParent(SplitNode parent) {
            if(this.parent == parent) {
                return;
            }

            this.parent = parent;
        }

        /** Getter of parent property. */
        public SplitNode getParent() {
            return parent;
        }

        public abstract double getResizeWeight();
        
        /** Gets constraints of this <code>Node</code>, designating
         * the path in the model */
        public SplitConstraint[] getNodeConstraints() {
            Node node = this;
            List conList = new ArrayList(5);
            do {
                SplitConstraint item = getConstraintForNode(node);
                if(item != null) {
                    conList.add(item);
                }
                
                node = node.getParent();
            } while(node != null);

            Collections.reverse(conList);
            return (SplitConstraint[])conList.toArray(new SplitConstraint[0]);
        }

        /** Gets constraint of this <code>Node</code> from parent. */
        private static SplitConstraint getConstraintForNode(Node node) {
            SplitNode parent = node.getParent();
            if(parent != null) {
                return  new SplitConstraint(
                    parent.getOrientation(),
                    parent.getChildIndex(node),
                    parent.getChildSplitWeight(node)
                );
            }

            return null;
        }
        
        //////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////

        /** Indicates whether component represented by this node is visible or not. */
        public boolean isVisibleInSplit() {
            return false;
        }

        /** Indicates whether there is at least one visible descendant. */
        public boolean hasVisibleDescendant() {
            return isVisibleInSplit();
        }

        /** Creates snapshot of this node. */
        public abstract ModeStructureSnapshot.ElementSnapshot createSnapshot();
        
    } // End of nested Node class.

    
    /** Class representing one split in SplitSubModel. The split is n-branched, i.e.
     * it can have more than two children. */
    protected static class SplitNode extends Node {

        /** Constraint of first node (VERTICAL or HORIZONTAL). */
        private final int orientation;
        
        // XXX some better structure needed? List is not enough since the indices may
        // not be continuous (like 0, 1, 2, 3) but even like (0, 3, 8, 9).
        /** Maps index to child node, while keeps ordering according to keys (indices). */
        private final TreeMap index2child = new TreeMap();

        /** Maps child node to its splitWeight. */
        private final Map child2splitWeight = new HashMap();

        /** Creates a new instance of SplitNode */
        public SplitNode(int orientation) {
            this.orientation = orientation;
        }


        /** Overrides superclass method. Adds info about dividePos, orientation,
         * first and second sub-nodes. */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            
            for(Iterator it = index2child.keySet().iterator(); it.hasNext(); ) {
                Integer index = (Integer)it.next();
                Node child = (Node)index2child.get(index);
                sb.append("child[" + index.intValue() +"]=" + child.getClass()
                    + "@" + Integer.toHexString(child.hashCode())); // NOI18N
            }
            
            return sb.toString();
        }

        /** Getter of orientation property. */
        public int getOrientation() {
            return orientation;
        }
        
        public void setChildAt(int index, double splitWeight, Node child) {
            setChildAt(index, splitWeight, child, false);
        }

        // XXX
        /** @param adjustToAllWeights means that the adding weight is relative to 
         *                          total weight already under this split. Thererfor
         *                          the added weight has to be adjusted accordingly
         *                          and possible also all the weights normalized
         *                          (so there is none bigger than 1). */
        public void setChildAt(int index, double splitWeight, Node child, boolean adjustToAllWeights) {
            // XXX -1 means, put it at the end.
            if(index == -1) {
                if(index2child.isEmpty()) {
                    index = 0;
                } else {
                    index = ((Integer)index2child.lastKey()).intValue() + 1;
                }
            }
            
            Integer ind = new Integer(index);
            
            Node oldChild = (Node)index2child.get(ind);
            // There are some other nodes at the index, shift them first.
            for(int i = ind.intValue() + 1; oldChild != null; i++) {
                oldChild = (Node)index2child.put(new Integer(i), oldChild);
            }

            // Finally add the new node.
            index2child.put(ind, child);
            // Also add it to child2splitWeight map
            setChildSplitWeightImpl(child, splitWeight, adjustToAllWeights);
            child.setParent(this);
            
            verifyChildren();
        }
        
        public Node getChildAt(int index) {
            return (Node)index2child.get(new Integer(index));
        }
        
        private void verifyChildren() {
            for(Iterator it = index2child.values().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();
                if(child.getParent() != this) {
                    ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Node " + child // NOI18N
                            + " is a child in split " + this // NOI18N
                            + " but his parent is " + child.getParent()
                            + ". Repairing")); // NOI18N
                    // Repair.
                    child.setParent(this);
                }
            }
        }

        public double getChildSplitWeight(Node child) {
            Double db = (Double)child2splitWeight.get(child);
            if(db != null) {
                return db.doubleValue();
            }
            
            return -1D;
        }
        
        public void setChildSplitWeight(Node child, double weight) {
            if(child == null || !child2splitWeight.keySet().contains(child)) {
                return;
            }
            
            setChildSplitWeightImpl(child, weight, false);
        }
        
        private void setChildSplitWeightImpl(Node child, double weight, boolean adjustToAllWeights) {
            if(adjustToAllWeights) {
                adjustAndAddToAllWeights(child, weight);
            } else {
                child2splitWeight.put(child, new Double(weight));
            }
        }
        
        private void adjustAndAddToAllWeights(Node child, double weight) {
            double total = 0D;
            for(Iterator it = child2splitWeight.values().iterator(); it.hasNext(); ) {
                total += ((Double)it.next()).doubleValue();
            }
            if(total > 0D && weight < 1D) {
                weight = weight * total / (1D - weight);
            }
            
            if(weight > 1D) {
                // Normalize weights
                double ratio = 1D / weight;
                normalizeWeights(ratio);
                weight = 1D;
            }
            child2splitWeight.put(child, new Double(weight));
        }
        
        private void normalizeWeights(double ratio) {
            for(Iterator it = child2splitWeight.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                double w = ((Double)entry.getValue()).doubleValue();
                w = ratio * w;
                entry.setValue(new Double(w));
            }
        }
        
        public int getChildIndex(Node child) {
            for(Iterator it = index2child.keySet().iterator(); it.hasNext(); ) {
                Object key = it.next();
                if(child == index2child.get(key)) {
                    return ((Integer)key).intValue();
                }
            }
            
            return -1;
        }
        
        public List getChildren() {
            return new ArrayList(index2child.values());
        }
        
        public List getVisibleChildren() {
            List l = getChildren();
            for(Iterator it = l.iterator(); it.hasNext(); ) {
                Node node = (Node)it.next();
                if(!node.hasVisibleDescendant()) {
                    it.remove();
                }
            }
            
            return l;
        }
        
        protected boolean removeChild(Node child) {
            boolean result = index2child.values().remove(child);
            child2splitWeight.remove(child);
            child.setParent(null);
            
            return result;
        }

        /** Indicates whether component represented by this node is visible or not. */
        public boolean isVisibleInSplit() {
            int count = 0;
            for(Iterator it = index2child.values().iterator(); it.hasNext(); ) {
                Node node = (Node)it.next();
                if(node.hasVisibleDescendant()) {
                    count++;
                    // At leas two are needed so the split is showing.
                    if(count >= 2) {
                        return true;
                    }
                }
            }
            
            return false;
        }

        /** Indicates whether there is at least one visible descendant. */
        public boolean hasVisibleDescendant() {
            for(Iterator it = index2child.values().iterator(); it.hasNext(); ) {
                Node node = (Node)it.next();
                if(node.hasVisibleDescendant()) {
                    return true;
                }
            }
            
            return false;
        }
        
        public double getResizeWeight() {
            List children = getVisibleChildren();
            double max = 0D;
            for(Iterator it = children.iterator(); it.hasNext(); ) {
                double resizeWeight = ((Node)it.next()).getResizeWeight();
                max = Math.max(max, resizeWeight);
            }
            
            return max;
        }

        public ModeStructureSnapshot.ElementSnapshot createSnapshot() {
            List childSnapshots = new ArrayList();
            Map childSnapshot2splitWeight = new HashMap();
            for(Iterator it = getChildren().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();
                ModeStructureSnapshot.ElementSnapshot childSnapshot = child.createSnapshot();
                childSnapshots.add(childSnapshot);
                childSnapshot2splitWeight.put(childSnapshot, child2splitWeight.get(child));
            }
            
            ModeStructureSnapshot.SplitSnapshot splitSnapshot = new ModeStructureSnapshot.SplitSnapshot(this, null,
                getOrientation(), childSnapshots, childSnapshot2splitWeight, getResizeWeight());
            
            // Set parent for children.
            for(Iterator it = childSnapshots.iterator(); it.hasNext(); ) {
                ModeStructureSnapshot.ElementSnapshot snapshot = (ModeStructureSnapshot.ElementSnapshot)it.next();
                snapshot.setParent(splitSnapshot);
            }
            
            return splitSnapshot;
        }
    } // End of nested SplitNode class.


    /** Class representing leaf node in SplitSubModel which corresponds to Mode. */
    protected static class ModeNode extends Node {

        private final ModeImpl mode;
        

        /** Creates a new instance of ModeNode */
        public ModeNode(ModeImpl mode) {
            this.mode = mode;
        }

        public ModeImpl getMode() {
            return mode;
        }

        public boolean isVisibleInSplit() {
            if(mode.getOpenedTopComponents().isEmpty()) {
                return false;
            }

            if(mode.getKind() == Constants.MODE_KIND_VIEW
            && mode.getState() == Constants.MODE_STATE_SEPARATED) {
                return false;
            }

            return true;
        }
        
        public boolean isVisibleSeparate() {
            if(mode.getOpenedTopComponents().isEmpty()) {
                return false;
            }
            
            if(mode.getKind() == Constants.MODE_KIND_EDITOR
            || mode.getState() == Constants.MODE_STATE_JOINED) {
                return false;
            }
            
            return true;
        }
        
        public double getResizeWeight() {
            return 0D;
        }
        
        public ModeStructureSnapshot.ElementSnapshot createSnapshot() {
            return new ModeStructureSnapshot.ModeSnapshot(this, null, mode, getResizeWeight());
        }
    } // End of nested ModeNode class.


}

