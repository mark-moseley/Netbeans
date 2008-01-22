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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.mapper.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentGroup;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.bpel.mapper.palette.Palette;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.soa.mappercore.model.Graph;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * The default implementation of the MapperModel interface for the BPEL Mapper.
 * 
 * @author nk160297
 */
public class BpelMapperModel implements MapperModel, MapperTcContext.Provider {

    public final Graph STUB_GRAPH;
    
    private MapperTcContext mMapperTcContext;
    private GraphChangeProcessor mChangeProcessor;
    private MapperSwingTreeModel mLeftTreeModel;
    private MapperSwingTreeModel mRightTreeModel;
    
    // Maps a TreePath to a Graph
    private Map<TreePath, Graph> mPathGraphMap = new HashMap<TreePath, Graph>();

    public BpelMapperModel(MapperTcContext mapperTcContext, 
            GraphChangeProcessor changeProcessor, 
            MapperTreeModel leftModel, MapperTreeModel rightModel) {
        //
        mMapperTcContext = mapperTcContext;
        mChangeProcessor = changeProcessor;
        //
        mLeftTreeModel = new MapperSwingTreeModel(mMapperTcContext, leftModel);
        //
        mRightTreeModel = new MapperSwingTreeModel(mMapperTcContext, rightModel);
        //
        STUB_GRAPH = new Graph(this);
    }

    public MapperTcContext getMapperTcContext() {
        return mMapperTcContext;
    }

    public MapperSwingTreeModel getRightTreeModel() {
        return mRightTreeModel;
    }

    public MapperSwingTreeModel getLeftTreeModel() {
        return mLeftTreeModel;
    }

    //==========================================================================
    //  Gentral graph methods
    //==========================================================================
    
    public TreeSourcePin getTreeSourcePin(TreePath treePath) {
        return new TreeSourcePin(treePath);
    }

    public Graph getGraph(TreePath treePath) {
        Graph result = mPathGraphMap.get(treePath);
        return (result == null) ? STUB_GRAPH : result;
    }

    public boolean searchGraphsInside(TreePath path) {
        Object parent = path.getLastPathComponent();

        for (TreePath treePath : mPathGraphMap.keySet()) {
            while (true) {
                // The last path object is skipped here
                treePath = treePath.getParentPath();
                if (treePath == null) {
                    break;
                }
                Object pathItem = treePath.getLastPathComponent();
                if (pathItem == parent) {
                    return true;
                }
            }
        }
        //
        return false;
    }

    public void addGraph(Graph newGraph, TreePath treePath) {
        mPathGraphMap.put(treePath, newGraph);
        // fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
    }

    public void removeGraph(TreePath treePath) {
        Graph graph = mPathGraphMap.get(treePath);
        boolean modified = false;
        if (graph != null) {
            //
            List<Vertex> vertexList = graph.getVerteces();
            for (Vertex vertex : vertexList) {
                graph.removeVertex(vertex);
                modified = true;
            }
            //
            List<Link> linksList = graph.getLinks();
            for (Link link : linksList) {
                graph.removeLink(link);
                modified = true;
            }
            //
            if (modified) {
                fireGraphChanged(treePath);
            }
            //
            mPathGraphMap.remove(treePath);
        }
    }
    
    /**
     * Takes an existing graph or creates a new one and registers it in this mapper.
     * @param treePath - the graph's location.
     * @return required graph.
     */
    public Graph graphRequired(TreePath treePath) {
        Graph graph = getGraph(treePath);
        if (graph == null || graph == STUB_GRAPH) {
            graph = new Graph(this);
            mPathGraphMap.put(treePath, graph);
        }
        return graph;
    }

    public Map<TreePath, Graph> getGraphsInside(TreePath root) {
        if (root == null || 
                root.getLastPathComponent() == getRightTreeModel().getRoot()) {
            return mPathGraphMap;
        }
        //
        HashMap<TreePath, Graph> result = new HashMap<TreePath, Graph>();
        for (TreePath tPath : mPathGraphMap.keySet()) {
            if (root.isDescendant(tPath)) {
                Graph graph = mPathGraphMap.get(tPath);
                result.put(tPath, graph);
            }
        }
        return result;
    }
    
    //==========================================================================
    //   Modification methods
    //==========================================================================

    public boolean canConnect(TreePath treePath, SourcePin source, 
            TargetPin target, TreePath oldTreePath, Link oldLink) 
    {
        if (oldLink != null) return false;
        if (target instanceof Graph) {
            if (!mRightTreeModel.isConnectable(treePath)) {
                return false;
            }
            //
            if (((Graph) target).hasOutgoingLinks()) {
                // The target tree node already has a connected link
                return false;
            }
        }
        //
        if (source instanceof TreeSourcePin) {
            TreePath sourceTreePath = ((TreeSourcePin) source).getTreePath();
            if (!mLeftTreeModel.isConnectable(sourceTreePath)) {
                return false;
            }
        }
        //
        // Check there is only one outgoing link
        if (source instanceof Vertex) {
            Link outgoingLink = ((Vertex) source).getOutgoingLink();
            if (outgoingLink != null) {
                return false;
            }
        }
        //
        if (target instanceof VertexItem) {
            //
            // Check if the target vertex item has a value
            Object value = ((VertexItem) target).getValue();
            if (value != null) {
                return false;
            }
            //
            // Check the item doesn't have incoming link yet
            Link ingoingLink = ((VertexItem) target).getIngoingLink();
            if (ingoingLink != null) {
                return false;
            }
            //
            // Check connection 2 vertexes 
            if (source instanceof Vertex) {
                //
                // Trying connect the vertex to itself isn't allowed
                Vertex targetVertex = ((VertexItem) target).getVertex();
                if (targetVertex == source) {
                    return false;
                }
                // Check cyclic dependences
                if (BpelMapperUtils.areVertexDependent((Vertex) source, targetVertex)) {
                    return false;
                }
            }
        }
        //
        return true;
    }

    public boolean canCopy(TreePath treePath, GraphSubset graphSubset) {
        return true;
    }

    public boolean canMove(TreePath treePath, GraphSubset graphSubset) {
        return true;
    }

    public void connect(TreePath treePath, SourcePin source, TargetPin target,
            TreePath oldTreePath, Link oldLink) 
    {
        if (oldLink != null) return;
        
        Graph graph = getGraph(treePath);
        //
        Graph resultGraph;
        if (graph == STUB_GRAPH) {
            //
            // Add the new Graph
            resultGraph = new Graph(this);
            mPathGraphMap.put(treePath, resultGraph);
            target = resultGraph;
        } else {
            resultGraph = graph;
        }
        //
        // Process the case when link is drawn to a hairline vertex item
        if (target instanceof VertexItem) {
            VertexItem vItem = (VertexItem) target;
            if (vItem.isHairline()) {
                Vertex vertex = vItem.getVertex();
                Object dataObject = vItem.getDataObject();
                int index = vertex.getItemIndex(vItem);
                if (dataObject instanceof ArgumentDescriptor) {
                    //
                    // A new real vertex item has to be inserted after the hairline item
                    VertexItem newRealVItem = VertexFactory.constructVItem(
                            vertex, (ArgumentDescriptor)dataObject);
                    vertex.addItem(newRealVItem, index + 1);
                    //
                    // A new hairline item has to be inserted after the real vertex item
                    VertexItem newHirelineVItem = 
                            VertexFactory.constructHairline(vertex, dataObject);
                    vertex.addItem(newHirelineVItem, index + 2);
                    //
                    // Eventually 2 new vertex item is added: real and additional hairline
                    target = newRealVItem;
                } else if (dataObject instanceof ArgumentGroup) {
                    List<VertexItem> itemsList = VertexFactory.getInstance().
                            createGroupItems(vertex, (ArgumentGroup) dataObject);
                    //
                    // Insert new vertex items in the back direction
                    //
                    // A new hairline item will appear just after the group's items.
                    VertexItem newHirelineVItem = 
                            VertexFactory.constructHairline(vertex, dataObject);
                    vertex.addItem(newHirelineVItem, index + 1);
                    //
                    // Insert a sequence of vertex items to the position next to 
                    // the initial hairline. The items are inserted in the 
                    // back direction but in the same place, so previous item move 
                    // down when the next is inserted. 
                    ListIterator<VertexItem> backItr =
                            itemsList.listIterator(itemsList.size());
                    while (backItr.hasPrevious()) {
                        VertexItem vertItem = backItr.previous();
                        vertex.addItem(vertItem, index + 1);
                    }
                    //
                    // Looking for the item to which the link has to be connected
                    VertexItem newTargetVItem = null;
                    XPathType sourceType =
                            BpelMapperUtils.calculateXPathSourcePinType(source);
                    if (sourceType != null) {
                        newTargetVItem = BpelMapperUtils.
                                findBestFittedItem(itemsList, sourceType);
                    }
                    //
                    if (newTargetVItem != null) {
                        target = newTargetVItem;
                    }
                }
            }
        }
        //
        Link newLink = new Link(source, target);
        resultGraph.addLink(newLink);
        //
        fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
    }

    // vlv
    public GraphSubset getGraphSubset(Transferable transferable) {
//out();
        for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
            try {
//out("see: " + transferable.getTransferData(flavor));
                Object[] objects = (Object[]) transferable.getTransferData(flavor);
                myHandler = (ItemHandler) objects[0];
                GraphSubset graph = myHandler.createGraphSubset();
                Palette palette = (Palette) objects[1];
//out("graph: " + graph);

                if (graph != null) {
                    palette.hideMenu();
                    return graph;
                }
            } catch (IOException e) {
                continue;
            } catch (UnsupportedFlavorException e) {
                continue;
            }
        }
        return null;
    }
    private ItemHandler myHandler;

    private boolean isConnectable(TreePath treePath) {
        if (treePath == null) {
            return false;
        }
        return mRightTreeModel.isConnectable(treePath);
    }

    public boolean add(TreePath treePath, ItemHandler handler, int x, int y) {
        myHandler = handler;
        return doCopy(treePath, null, x, y);
    }

    public void copy(TreePath treePath, GraphSubset graphSubset, int x, int y) {
        doCopy(treePath, graphSubset, x, y);
    }

    private boolean doCopy(TreePath treePath, GraphSubset graphSubset, int x, int y) {
        if (!isConnectable(treePath)) {
            return false;
        }
        if (myHandler != null) {
            if (myHandler.canAddGraphSubset()) {
                graphSubset = myHandler.createGraphSubset();
            } else {
                graphSubset = null;
            }
        }
        if (graphSubset == null) {
            return false;
        }
        Graph graph = graphRequired(treePath);

        for (int i = graphSubset.getVertexCount() - 1; i >= 0; i--) {
            Vertex vertex = graphSubset.getVertex(i);
            vertex.setLocation(x, y);
            graph.addVertex(vertex);
        }
        for (int i = 0; i < graphSubset.getLinkCount(); i++) {
            Link link = graphSubset.getLink(i);
            graph.addLink(link);
        }
        fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
        //
        return true;
    }

    public void move(TreePath treePath, GraphSubset graphSubset, int dx, int dy) {
        if (!isConnectable(treePath)) {
            return;
        }

        if (graphSubset == null) {
            return;
        }
        Graph graph = graphRequired(treePath);

        for (int i = graphSubset.getVertexCount() - 1; i >= 0; i--) {
            Vertex vertex = graphSubset.getVertex(i);
            //FIXME Only moving inside the same graph
            if (vertex.getGraph() == null) {
                graph.addVertex(vertex);
            }

            if (graph == vertex.getGraph()) {
                int newX = vertex.getX() + dx;
                int newY = vertex.getY() + dy;
                vertex.setLocation(newX, newY);

            }
        }
        //
        fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
    }

    public void fireGraphChanged(TreePath treePath) {
        if (mChangeProcessor != null) {
            mChangeProcessor.processChanges(treePath);
        }
    }

    public void fireGraphsChanged(List<TreePath> treePathList) {
        if (mChangeProcessor != null) {
            mChangeProcessor.processChanges(treePathList);
        }
    }
    
    //==========================================================================
    //   Right tree methods
    //==========================================================================

    public Object getRoot() {
        return mRightTreeModel.getRoot();
    }

    public Object getChild(Object parent, int index) {
        return mRightTreeModel.getChild(parent, index);
    }

    public int getChildCount(Object parent) {
        return mRightTreeModel.getChildCount(parent);
    }

    public boolean isLeaf(Object node) {
        return mRightTreeModel.isLeaf(node);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        mRightTreeModel.valueForPathChanged(path, newValue);
    }

    public int getIndexOfChild(Object parent, Object child) {
        return mRightTreeModel.getIndexOfChild(parent, child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        mRightTreeModel.addTreeModelListener(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        mRightTreeModel.removeTreeModelListener(l);
    }

    public void valueChanged(TreePath treePath, VertexItem vertexItem, 
            Object newValue) 
    {
        vertexItem.setValue(newValue);
        fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
    }
    
    //===================================================================
    // Predicates support methods
    //===================================================================


    /**
     * Looks for a list of graphs which depends on the specified data object.
     * The data object relates to the left tree item. 
     * @param leftTreeItemDO
     * @return
     */
    public List<TreePath> getDependentGraphs(Object leftTreeItemDO) {
        ArrayList<TreePath> result = new ArrayList<TreePath>();
        //
        Map<TreePath, Graph> graphs = getGraphsInside(null);
        for (TreePath path : graphs.keySet()) {
            Graph graph = graphs.get(path);
            List<Link> connectedLinksList = 
                    graph.getConnectedIngoingLinks(new ArrayList<Link>());
            for (Link link : connectedLinksList) {
                SourcePin sourcePin = link.getSource();
                assert sourcePin instanceof TreeSourcePin;
                TreePath leftTreePath = ((TreeSourcePin)sourcePin).getTreePath();
                if (MapperSwingTreeModel.containsDataObject(
                        leftTreePath, leftTreeItemDO)) {
                    result.add(path);
                    break;
                }
            }
        }
        //
        return result;
    }
 
    /**
     * Remove links which go from the leftNodePath (left tree)
     * @param treePath
     */
    public void removeIngoingLinks(TreePath graphPath, TreePath leftNodePath) {
        //
        Graph graph = getGraph(graphPath);
        List<Link> ingoingLinks = graph.getIngoingLinks();
        for (Link link : ingoingLinks) {
            SourcePin sourcePin = link.getSource();
            assert sourcePin instanceof TreeSourcePin;
            TreePath path = ((TreeSourcePin)sourcePin).getTreePath();
            if (path == null || path.equals(leftNodePath)) {
                graph.removeLink(link);
            }
        }
    }

    public boolean canEditInplace(VertexItem vItem) {
        return vItem.getIngoingLink() == null;
    }
    
}
