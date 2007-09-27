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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphLink;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IHighlightable;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IToolBar;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphActionDelegator;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.property.CollaborationGraphNode;

import org.netbeans.modules.etl.ui.property.JoinNode;
import org.netbeans.modules.etl.ui.property.RuntimeInputNode;
import org.netbeans.modules.etl.ui.property.RuntimeOutputNode;
import org.netbeans.modules.etl.ui.property.SourceTableNode;
import org.netbeans.modules.etl.ui.property.TargetTableNode;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLRuntimeInputArea;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLRuntimeOutputArea;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLSourceTableArea;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLTargetTableArea;
import org.netbeans.modules.sql.framework.ui.view.join.JoinViewGraphNode;
import org.netbeans.modules.sql.framework.ui.view.join.SQLJoinTableArea;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * Extension of JGoView to implement IGraphView interface.
 *
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public abstract class GraphView extends JGoView implements IGraphView {
    private static final int STANDARD_SCALE = 1;
    private static final int VIEW_SCALE = 2;
    private static final int PAGE_SCALE = 3;
    
    private static final String NETBEANS_DBTABLE_MIMETYPE = "application/x-java-netbeans-dbexplorer-table;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table";
    
    protected static DataFlavor[] mDataFlavorArray = new DataFlavor[2];
    
    private IGraphController graphController;
    
    private JGoLayeredDigraphAutoLayout layout;
    
    private Object graphViewContainer;
    
    private Object graphModel;
    
    private ETLDataObject mObj;
    
    private Object graphFactory;
    
    private IToolBar toolBar;
    
    private List graphActions;
    
    protected JPopupMenu popUpMenu;
    
    private int printScale = STANDARD_SCALE;
    
    protected Point mousePoint = null;
    
    private IOperatorXmlInfo opXmlInfo = null;
    
    // current object under mouse
    private static JGoObject currentObj = null;
    
    public static JGoObject selectedObject = null;
    
    private BirdsEyeView satelliteView;
    
    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
            
            mDataFlavorArray[1] = new DataFlavor(NETBEANS_DBTABLE_MIMETYPE);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    /** Creates a new instance of BasicGraphView */
    public GraphView() {
        // super();
        setDropEnabled(true);
        //set default primary and secondary selection colors
        resetSelectionColors();
        satelliteView = new BirdsEyeView();
        //set GraphDocument
        this.setDocument(new GraphDocument());
        setObserved(this);
    }
    
    /**
     * computeAcceptableDrop
     *
     * @param e - DropTargetDragEvent
     * @return - true/false
     */
    public int computeAcceptableDrop(DropTargetDragEvent e) {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }
    
    /**
     * isDropFlavorAcceptable
     *
     * @param e - DropTargetDragEvent
     * @return - true/false
     */
    public boolean isDropFlavorAcceptable(DropTargetDragEvent e) {
        for (int i = 0; i < mDataFlavorArray.length; i++) {
            if (e.isDataFlavorSupported(mDataFlavorArray[i])) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Drop
     *
     * @param e - DropTargetDropEvent
     */
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        try {
            Point viewCoord = e.getLocation();
            Point docCoord = viewToDocCoords(viewCoord);
            //sets location as doc coordinates
            e.getLocation().setLocation(docCoord);
            
            if (e.isDataFlavorSupported(mDataFlavorArray[0])) {
                Transferable tr = e.getTransferable();
                
                /*if (!(tr.getTransferData(mDataFlavorArray[0]) instanceof IOperatorXmlInfo)) {
                    if (graphController != null) {
                        graphController.handleDrop(e);
                    }
                 
                    return;
                }*/
                
                IOperatorXmlInfo xmlInfo = null;
                try{
                    xmlInfo = (IOperatorXmlInfo)tr.getTransferData(mDataFlavorArray[0]);
                }catch(Exception ex){
                    xmlInfo = getXMLInfo();
                }
                
                if (!(xmlInfo instanceof IOperatorXmlInfo)) {
                    if (graphController != null) {
                        graphController.handleDrop(e);
                    }
                    return;
                }
                
                //    IOperatorXmlInfo xmlInfo = (IOperatorXmlInfo) tr.getTransferData(mDataFlavorArray[0]);
                
                if (graphController != null) {
                    graphController.handleNodeAdded(xmlInfo, docCoord);
                } else {
                    addXmlInfoNode(xmlInfo, docCoord);
                }
                
                // Must wait to accept drop until all other execution paths that might
                // throw an exception have been executed.
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            } else if (graphController != null) {
                graphController.handleDrop(e);
            } else {
                e.rejectDrop();
            }
            
            //            //also call graph controller' handle drop
            //            if (graphController != null) {
            //                graphController.handleDrop(e);
            //            }
        } catch (Exception ex) {
            e.rejectDrop();
            ex.printStackTrace();
            StringBuilder msgBuf = new StringBuilder("Cannot create node in the canvas");
            if (ex.getMessage() != null) {
                msgBuf.append(": ").append(ex.getMessage());
            } else {
                msgBuf.append(".");
            }
            
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msgBuf.toString(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    /**
     * @see com.nwoods.jgo.JGoView#doMouseDown
     */
    public boolean doMouseDown(int modifiers, Point dc, Point vc) {
        mousePoint = dc;
        return super.doMouseDown(modifiers, dc, vc);
    }
    
    /**
     * Handles key event
     *
     * @param evt Description of the Parameter
     */
    public void onKeyEvent(KeyEvent evt) {
        int t = evt.getKeyCode();
        
        if (t == KeyEvent.VK_DELETE) {
            if (getDocument().isModifiable()) {
                deleteNodesAndLinks();
            }
        } else if (t == KeyEvent.VK_A && evt.isControlDown()) {
            this.selectAll();
        }
    }
    
    /**
     * Delete an object
     *
     * @param node node to be deleted
     */
    public void deleteNode(IGraphNode node) {
        //delete object
        deleteLinks(node);
        if (graphController != null) {
            graphController.handleNodeRemoved(node);
        }
    }
    
    private void deleteLinks(IGraphNode node) {
        List list = node.getAllLinks();
        deleteLinks(list);
    }
    
    /**
     * Delete a collection of links
     *
     * @param links - links
     */
    public void deleteLinks(Collection links) {
        Iterator it = links.iterator();
        while (it.hasNext()) {
            IGraphLink link = (IGraphLink) it.next();
            if (graphController != null) {
                graphController.handleLinkDeleted(link);
            }
        }
    }
    
    private void deleteNodesAndLinks() {
        //get all selected nodes
        Collection nodes = this.getSelectedNodes();
        //get all selected links
        Collection links = this.getSelectedLinks();
        //no nodes are selected but one or more link is selected by user
        if (nodes.size() == 0 && links.size() > 0) {
            String title = NbBundle.getMessage(GraphView.class, "MSG_DeleteConfirmation");
            String msg = NbBundle.getMessage(GraphView.class, "MSG_DeleteQuestion");
            msg = msg.replaceFirst("000", "All selected links");
            
            int option = UIUtil.showYesAllDialog(this, msg, title);
            if (option == JOptionPane.YES_OPTION) {
                this.deleteLinks(links);
            }
            
            return;
        }
        //some node and zero or more links area selected by user
        deleteNodesAndLinks(nodes, links);
    }
    
    private void deleteNodesAndLinks(Collection nodes, Collection links) {
        //keep a list of links which are getting deleted as a result of deleting a node
        ArrayList nodeDeletedLinks = new ArrayList();
        boolean delete = false;
        //now delete them
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            IGraphNode node = (IGraphNode) it.next();
            if (!node.isDeleteAllowed()) {
                continue;
            }
            
            //links.removeAll(node.getAllLinks());
            if (!delete) {
                String title = NbBundle.getMessage(GraphView.class, "MSG_DeleteConfirmation");
                String msg = NbBundle.getMessage(GraphView.class, "MSG_DeleteQuestion");
                SQLObject sqlObject = (SQLObject) node.getDataObject();
                if (nodes.size() > 1) {
                    msg = msg.replaceFirst("000", "All selected components");
                } else {
                    msg = msg.replaceFirst("000", sqlObject.getDisplayName());
                }
                int option = UIUtil.showYesAllDialog(this, msg, title);
                if (option == JOptionPane.YES_OPTION) {
                    delete = true;
                } else if (option == JOptionPane.NO_OPTION) {
                    break;
                }
            }
            if (delete) {
                //delete links associated with a given graph node, links associated with
                //a node may not have been selected by user but since we are deleting a
                //node we should delete the links associated with it
                deleteNodeLinks(node.getAllLinks(), nodeDeletedLinks);
                //            deleteLinks(node);
                graphController.handleNodeRemoved(node);
            }
        }
        
        if (delete) {
            //now delete all selected links
            //remove the links which are already deleted as a result of deleting a node
            links.removeAll(nodeDeletedLinks);
            //now remove the links if any which are selected but their source and
            // destination nodes are not selected
            this.deleteLinks(links);
        }
        //        Iterator it = links.iterator();
        //        while (it.hasNext()) {
        //            IGraphLink link = (IGraphLink) it.next();
        //            graphController.handleLinkDeleted(link);
        //        }
    }
    
    private void deleteNodeLinks(Collection nodeLinks, Collection deletedLinks) {
        Iterator it = nodeLinks.iterator();
        while (it.hasNext()) {
            IGraphLink link = (IGraphLink) it.next();
            //if link is not already deleted then delete it
            if (!deletedLinks.contains(link)) {
                //add link to node deleted link
                deletedLinks.add(link);
                graphController.handleLinkDeleted(link);
            }
        }
        
    }
    
    public boolean doMouseMove(int modifiers, Point dc, Point vc) {
        super.doMouseMove(modifiers, dc, vc);
        
        //handle mouse event and delegate to child object under mouse point
        if (getState() != MouseStateCreateLinkFrom && getState() != MouseStateCreateLink && getState() != MouseStateDragBoxSelection)
            doMouseHandling(modifiers, dc, vc);
        return true;
    }
    
    private boolean doMouseHandling(int modifiers, Point dc, Point vc) {
        boolean returnStatus = false;
        
        // if we're over a port, start drawing a new link
        JGoObject obj = pickPort(dc);
        
        if (obj == null) {
            obj = pickDocObject(dc, false);
        }
        
        //if(obj != null)
        //System.out.println(" JGoView doMouseEntered " + obj.getClass().getName());
        //obj = getSelection().selectObject(obj);
        
        while (obj != null) {
            if (obj instanceof GraphPort && ((GraphPort) obj).doMouseEntered(modifiers, dc, vc, this)) {
                returnStatus = true;
                break;
            } else if (obj instanceof IHighlightable) {
                ((IHighlightable) obj).setHighlighted(true);
                returnStatus = true;
                break;
            } else {
                obj = obj.getParent();
            }
        }
        
        //System.out.println("old current obj "+ currentObj + "new Object" + obj);
        //fire mouseExisted event
        if (currentObj instanceof GraphPort && currentObj != null && currentObj != obj) {
            ((GraphPort) currentObj).doMouseExited(modifiers, dc, vc, this);
        } else if (currentObj instanceof IHighlightable && null != currentObj && obj != currentObj) {
            ((IHighlightable) currentObj).setHighlighted(false);
        }
        
        // this is the current obj where mouse has entered
        currentObj = obj;
        
        return returnStatus;
    }
    
    /**
     * Called to create a new link from the from port to the to port.
     *
     * @param from source JGoPort
     * @param to destination JGoPort
     */
    public void newLink(JGoPort from, JGoPort to) {
        if (graphController != null) {
            graphController.handleLinkAdded((IGraphPort) from, (IGraphPort) to);
        } else {
            super.newLink(from, to);
        }
    }
    
    /**
     * Called when link creation fails.
     *
     * @param from source JGoPort
     * @param to destination JGoPort
     */
    public void noNewLink(JGoPort from, JGoPort to) {
    }
    
    /**
     * Called when link reconnection fails.
     *
     * @param oldlink JGoLink to be reconnected
     * @param from source JGoPort
     * @param to destination JGoPort
     */
    public void noReLink(JGoLink oldlink, JGoPort from, JGoPort to) {
    }
    
    /**
     * Adds a link from the given source port to the given destination port.
     *
     * @param from source IGraphPort
     * @param to destination IGraphPort
     */
    public void addLink(IGraphPort from, IGraphPort to) {
        GraphLink link = new GraphLink(from, to);
        this.getDocument().addObjectAtTail(link);
    }
    
    public void addLink(IGraphLink link) {
        this.getDocument().addObjectAtTail((JGoObject) link);
    }
    
    /**
     * Adds the given IGraphNode to the view.
     *
     * @param node new IGraphNode to add
     */
    public void addNode(IGraphNode node) {
        node.setGraphView(this);
        
        JGoObject obj = (JGoObject) node;
        
        if (obj.getLeft() == -1) {
            obj.setLeft(50);
        }
        
        if (obj.getTop() == -1) {
            obj.setTop(50);
        }
        
        //check for overlapping
        avoidOverlap(obj);
        
        JGoObject jgoObj = (JGoObject) node;
        
        //add this node to document
        this.getDocument().addObjectAtTail(jgoObj);
        satelliteView.getDocument().addObjectAtTail(jgoObj);
        
        //now select this node
        this.getSelection().selectObject(jgoObj);
        
        //and have focus in this view
        satelliteView.requestFocus();
        this.requestFocus();
    }
    
    /**
     * Creates and adds a new operator node at the given location, as specified by the
     * given operator descriptor.
     *
     * @param xmlInfo descriptor specifying operator configuration information
     * @param location Point at which to create new operator node
     * @return new IGraphNode representing operator in question
     */
    public IGraphNode addXmlInfoNode(IOperatorXmlInfo xmlInfo, Point location) {
        OperatorGraphNode graphNode = new OperatorGraphNode(xmlInfo);
        graphNode.setLocation(location);
        addNode(graphNode);
        return graphNode;
    }
    
    /**
     * Ensures given JGoObject does not overlap other JGoObjects in the view.
     *
     * @param guiInfo JGoObject whose positioning should not overlap other objects.
     */
    public void avoidOverlap(JGoObject guiInfo) {
        Point p = new Point(guiInfo.getLeft(), guiInfo.getTop());
        int width = guiInfo.getWidth();
        int height = guiInfo.getHeight();
        JGoSelection selection = this.getSelection();
        selection.clearSelection();
        
        JGoObject obj = null;
        
        Point tmpPoint = new Point(guiInfo.getLeft(), guiInfo.getTop());
        int selY = -1;
        
        int i = p.x;
        
        //scanning from topleft to topright
        while (i <= (p.x + width)) {
            i = i + 10;
            tmpPoint.setLocation(i, p.y);
            obj = getObjectInModel(tmpPoint, false);
            if (obj != null) {
                selection.extendSelection(obj);
                if (selY == -1 || selY > obj.getTop()) {
                    selY = obj.getTop();
                }
            }
        }
        
        i = p.y;
        //scanning from topright to bottomright
        while (i <= (p.y + height)) {
            i = i + 10;
            tmpPoint.setLocation(p.x + width, p.y);
            obj = getObjectInModel(tmpPoint, true);
            if (obj != null) {
                selection.extendSelection(obj);
                
                if (selY == -1 || selY > obj.getTop()) {
                    selY = obj.getTop();
                }
            }
        }
        
        i = p.x + width;
        //scanning from bottomright to bottomleft
        while (i >= p.x) {
            i = i - 10;
            tmpPoint.setLocation(i, p.y + height);
            obj = getObjectInModel(tmpPoint, true);
            if (obj != null) {
                selection.extendSelection(obj);
                
                if (selY == -1 || selY > obj.getTop()) {
                    selY = obj.getTop();
                }
            }
        }
        
        i = p.y + height;
        //scanning from bottomleft to topleft
        while (i >= p.y) {
            i = i - 10;
            tmpPoint.setLocation(p.x, i);
            obj = getObjectInModel(tmpPoint, true);
            if (obj != null) {
                selection.extendSelection(obj);
                
                if (selY == -1 || selY > obj.getTop()) {
                    selY = obj.getTop();
                }
            }
        }
        
        int ydiff = p.y - selY;
        
        int yOffset = 0;
        if (p.y > selY) {
            yOffset = height + ydiff;
        } else {
            yOffset = height - ydiff;
        }
        
        //now move the selection down
        if (!selection.isEmpty()) {
            JGoDocument jgoModel = this.getDocument();
            JGoListPosition pos = null;
            JGoObject jgoObj = null;
            
            for (pos = jgoModel.getFirstObjectPos(); pos != null; pos = jgoModel.getNextObjectPosAtTop(pos)) {
                
                jgoObj = jgoModel.getObjectAtPos(pos);
                int top = jgoObj.getTop();
                if (top > p.y) {
                    selection.extendSelection(jgoObj);
                }
            }
            
            this.moveSelection(selection, -1, 0, yOffset + 10, JGoView.MouseStateSelection);
            selection.clearSelection();
        }
        
    }
    
    /**
     * Gets the canvas node, if any, at the given position
     *
     * @param loc point in canvas document
     * @param flag if true check only the objects which are selectable
     * @return the object, if any, found at given location
     */
    public JGoObject getObjectInModel(Point loc, boolean flag) {
        return this.pickDocObject(loc, flag);
    }
    
    /**
     * Removes the link, if any, between the given ports.
     *
     * @param from source IGraphPort of link to be removed
     * @param to destination IGraphPort of link to be removed
     */
    public void removeLink(IGraphPort from, IGraphPort to) {
        JGoDocument jgoModel = this.getDocument();
        JGoListPosition pos = null;
        JGoObject jgoObj = null;
        
        for (pos = jgoModel.getFirstObjectPos(); pos != null; pos = jgoModel.getNextObjectPos(pos)) {
            
            jgoObj = jgoModel.getObjectAtPos(pos);
            if (jgoObj instanceof GraphLink) {
                GraphLink link = (GraphLink) jgoObj;
                if (link.getFromGraphPort().equals(from) && link.getToGraphPort().equals(to)) {
                    this.removeObject(link);
                }
            }
        }
    }
    
    /**
     * Removes the given IGraphNode from the view.
     *
     * @param node new IGraphNode to remove
     */
    public void removeNode(IGraphNode node) {
        this.getDocument().removeObject((JGoObject) node);
    }
    
    /**
     * Sets the graph controller of this view.
     *
     * @param controller new graph controller
     */
    public void setGraphController(IGraphController controller) {
        this.graphController = controller;
    }
    
    /**
     * Gets the graph controller of this view.
     *
     * @return current graph controller
     */
    public IGraphController getGraphController() {
        return this.graphController;
    }
    
    /**
     * Expands all graph objects in this view.
     */
    public void expandAll() {
        expandORCollapseAll(true);
    }
    
    /**
     * Collapses all graph objects in this view.
     */
    public void collapseAll() {
        expandORCollapseAll(false);
    }
    
    /**
     * autolayout all the graph objects
     */
    public void autoLayout() {
        layout = new JGoLayeredDigraphAutoLayout(this.getDocument());
        layout.setDirectionOption(JGoLayeredDigraphAutoLayout.LD_DIRECTION_RIGHT);
        layout.setColumnSpacing(20);
        layout.setLayerSpacing(20);
        layout.performLayout();
    }
    
    private void expandORCollapseAll(boolean expand) {
        JGoDocument jgoModel = this.getDocument();
        JGoListPosition pos = null;
        JGoObject jgoObj = null;
        
        for (pos = jgoModel.getFirstObjectPos(); pos != null; pos = jgoModel.getNextObjectPos(pos)) {
            
            jgoObj = jgoModel.getObjectAtPos(pos);
            if (!(jgoObj instanceof JGoLink) && jgoObj instanceof IGraphNode) {
                IGraphNode graphNode = (IGraphNode) jgoObj;
                graphNode.expand(expand);
            }
        }
    }
    
    /**
     * Retrieves a collection of currently selected links
     *
     * @return Collection of selected links in this view.
     */
    public Collection getSelectedLinks() {
        List list = new ArrayList();
        JGoSelection selection = this.getSelection();
        if (selection == null) {
            return list;
        }
        
        JGoObject obj = null;
        JGoListPosition pos = selection.getFirstObjectPos();
        while (pos != null) {
            obj = selection.getObjectAtPos(pos);
            if (obj instanceof IGraphLink) {
                list.add(obj);
            }
            
            pos = selection.getNextObjectPos(pos);
        }
        
        return list;
    }
    
    /**
     * Retrieves a collection of currently selected nodes
     *
     * @return Collection of selected nodes in this view.
     */
    protected Collection getSelectedNodes() {
        List list = new ArrayList();
        JGoSelection selection = this.getSelection();
        if (selection == null) {
            return list;
        }
        
        JGoObject obj = null;
        JGoListPosition pos = selection.getFirstObjectPos();
        while (pos != null) {
            obj = selection.getObjectAtPos(pos);
            if (obj instanceof IGraphNode) {
                list.add(obj);
            }
            
            pos = selection.getNextObjectPos(pos);
        }
        
        return list;
    }
    
    /**
     * Retrieves a collection of all links
     *
     * @return -
     */
    public Collection getAllGraphLinks() {
        List list = new ArrayList();
        JGoDocument document = this.getDocument();
        JGoObject obj = null;
        JGoListPosition pos = document.getFirstObjectPos();
        while (pos != null) {
            obj = document.getObjectAtPos(pos);
            if (obj instanceof IGraphLink) {
                list.add(obj);
            }
            
            pos = document.getNextObjectPos(pos);
        }
        return list;
        
    }
    
    /**
     * Reset selection colors
     */
    public void resetSelectionColors() {
        setDefaultPrimarySelectionColor(new Color(73, 117, 183));
        setDefaultSecondarySelectionColor(new Color(73, 117, 183));
        
    }
    
    /**
     * get graph view container which can manage this view
     *
     * @return graph view manager
     */
    public Object getGraphViewContainer() {
        return this.graphViewContainer;
    }
    
    /**
     * set the graph view container which this view can refer to
     *
     * @param mgr graph view manager
     */
    public void setGraphViewContainer(Object mgr) {
        this.graphViewContainer = mgr;
    }
    
    /**
     * Describe <code>getPrintScale</code> method here.
     *
     * @param g2 a <code>Graphics2D</code> value
     * @param pf a <code>PageFormat</code> value
     * @return a <code>double</code> value
     */
    public double getPrintScale(Graphics2D g2, PageFormat pf) {
        switch (printScale) {
        case VIEW_SCALE:
            return getScale();
        case PAGE_SCALE:
            Rectangle2D.Double pageRect = getPrintPageRect(g2, pf);
            java.awt.Dimension docSize = getPrintDocumentSize();
            // make sure it doesn't get scaled too much! (especially if no objects in
            // document)
            docSize.width = Math.max(docSize.width, 50);
            docSize.height = Math.max(docSize.height, 50);
            double hratio = pageRect.width / docSize.width;
            double vratio = pageRect.height / docSize.height;
            return Math.min(hratio, vratio);
        default:
            return 1.0d;
        }
    }
    
    /**
     * Print the view
     */
    public void printView() {
        String option1 = "Print at standard scale";
        String option2 = "Print using view's currently selected scale";
        String option3 = "Scale to fit page";
        Object[] options = { option1, option2, option3};
        Object selectedValue = JOptionPane.showInputDialog(this, "Choose a print option", "Print Options", JOptionPane.INFORMATION_MESSAGE, null,
                options, options[0]);
        if (selectedValue != null) {
            if (selectedValue.equals(option2)) {
                this.printScale = VIEW_SCALE;
            } else if (selectedValue.equals(option3)) {
                this.printScale = PAGE_SCALE;
            } else {
                this.printScale = STANDARD_SCALE;
            }
            print();
        }
    }
    
    public boolean doMouseUp(int modifiers, java.awt.Point dc, java.awt.Point vc) {
        selectedObject  = getCurrentObject();        
        if(getCurrentObject() instanceof SQLJoinTableArea){
            SQLJoinTableArea joinTblArea = (SQLJoinTableArea) getCurrentObject();
            SourceTable srcTable = (SourceTable) joinTblArea.getDataObject();
            WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{new SourceTableNode(srcTable)});
        } else if(getCurrentObject() instanceof SQLTargetTableArea){
            SQLTargetTableArea targetTableArea = (SQLTargetTableArea) getCurrentObject();
            TargetTable tgtTable = (TargetTable) targetTableArea.getDataObject();
            WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{new TargetTableNode(tgtTable)});
        } else if(getCurrentObject() instanceof SQLSourceTableArea){
            SQLSourceTableArea srcTableArea = (SQLSourceTableArea) getCurrentObject();
            SourceTable srcTable = (SourceTable) srcTableArea.getDataObject();
            WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{new SourceTableNode(srcTable)});
        } else if(getCurrentObject() instanceof JoinViewGraphNode){
            JoinViewGraphNode gn = (JoinViewGraphNode)getCurrentObject();
            SQLJoinView joinView = (SQLJoinView)gn.getDataObject();
            WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{new JoinNode(joinView.getRootJoin())});
        } else if(getCurrentObject() instanceof SQLRuntimeInputArea){
            SQLRuntimeInputArea runIn = (SQLRuntimeInputArea)getCurrentObject();
            RuntimeInput runInArea = (RuntimeInput)runIn.getDataObject();
            WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{new RuntimeInputNode(runInArea)});           

        } else if(getCurrentObject() instanceof SQLRuntimeOutputArea){
            SQLRuntimeOutputArea runOut = (SQLRuntimeOutputArea)getCurrentObject();
            RuntimeOutput runOutArea = (RuntimeOutput)runOut.getDataObject();
            WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{new RuntimeOutputNode((RuntimeOutput)runOutArea)});
        }else{
            mObj = DataObjectProvider.getProvider().getActiveDataObject();
            WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{new CollaborationGraphNode((ETLDataObject) mObj)});            
        }
        resetSelectionColors();
        
        boolean mClick = super.doMouseUp(modifiers, dc, vc);
        
        if (this.pickDocObject(dc, false) != null) {
            return mClick;
        }
        
        //if popup menu is null the create it and populate it with actions
        //set on this graph view
        if (popUpMenu == null) {
            buildPopUpMenu();
        }
        
        int onmask = java.awt.event.InputEvent.BUTTON3_MASK;
        
        if ((modifiers & onmask) != 0 && popUpMenu != null) {
            //if element is not checked out then ask user to check it out before
            // modifiying it
            
            if (!canEdit()) {
                return false;
            }
            
            if (popUpMenu != null) {
                popUpMenu.show(this, vc.x, vc.y);
                
                return true;
            }
        }
        
        return false;
    }
    
    private void registerAccelerator(Action action) {
        Object actionName = action.getValue(Action.NAME);
        Action oldAction = getActionMap().get(actionName);
        if (oldAction == null) {
            getActionMap().put(actionName, action);
        }
        
        KeyStroke ks = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        getInputMap().put(ks, actionName);
    }
    
    private void buildPopUpMenu() {
        popUpMenu = new JPopupMenu();
        
        List actions = this.getGraphActions();
        if (actions == null) {
            return;
        }
        
        Iterator it = actions.iterator();
        
        while (it.hasNext()) {
            Action action = (Action) it.next();
            GraphActionDelegator gaDelegator = new GraphActionDelegator(this, action);
            if (action != null) {
                if (action.getValue(Action.ACCELERATOR_KEY) != null) {
                    registerAccelerator(gaDelegator);
                    JMenuItem mi = new JMenuItem(gaDelegator);
                    popUpMenu.add(mi);
                } else {
                    popUpMenu.add(gaDelegator);
                }
            } else {
                popUpMenu.addSeparator();
            }
        }
    }
    
    /**
     * set the graph model
     *
     * @param model graph model
     */
    public void setGraphModel(Object model) {
        this.graphModel = model;
    }
    
    /**
     * get graph model
     *
     * @return graph model
     */
    public Object getGraphModel() {
        return this.graphModel;
    }
    
    /**
     * get the graph actions that need to be shown in popup menu
     *
     * @return a list of GraphAction, null in list represents a seperator
     */
    public List getGraphActions() {
        return graphActions;
    }
    
    /**
     * set graph actions on this view
     *
     * @param actions list of GraphAction
     */
    public void setGraphActions(List actions) {
        this.graphActions = actions;
    }
    
    /**
     * can this graph be edited
     *
     * @return true if graph is edited
     */
    public boolean canEdit() {
        return true;
    }
    
    public Object getGraphFactory() {
        return this.graphFactory;
    }
    
    /**
     * set the graph factory which is used for creating nodes in this graph
     *
     * @param gFactory graph node factory
     */
    public void setGraphFactory(Object gFactory) {
        this.graphFactory = gFactory;
    }
    
    /**
     * get a action based on class name
     *
     * @param actionClass
     * @return action
     */
    public Action getAction(Class actionClass) {
        List actions = this.getGraphActions();
        if (actions == null) {
            return null;
        }
        
        Iterator it = actions.iterator();
        
        while (it.hasNext()) {
            Action act = (Action) it.next();
            if (act != null && act.getClass().getName().equals(actionClass.getName())) {
                return act;
            }
        }
        
        return null;
    }
    
    /**
     * set the toolbar
     *
     * @param tBar
     */
    public void setToolBar(IToolBar tBar) {
        this.toolBar = tBar;
    }
    
    /**
     * get the toolbar
     *
     * @return toolbar
     */
    public IToolBar getToolBar() {
        return this.toolBar;
    }
    
    /**
     * remove all the view and document objects
     */
    public void clearAll() {
        this.getDocument().deleteContents();
        JGoListPosition pos = null;
        JGoObject jgoObj = null;
        
        for (pos = getFirstObjectPos(); pos != null; pos = getNextObjectPos(pos)) {
            
            jgoObj = getObjectAtPos(pos);
            this.removeObject(jgoObj);
        }
    }
    
    public void setModifiable(boolean b) {
        this.getDocument().setModifiable(b);
        this.setKeyEnabled(b);
        this.setMouseEnabled(b);
        this.setDragDropEnabled(b);
        if (this.getToolBar() != null) {
            this.getToolBar().enableToolBar(b);
        }
    }
    
    /**
     * check if this graph view is modifiable
     *
     * @return modifiable
     */
    public boolean isModifiable() {
        return this.getDocument().isModifiable();
    }
    
    /**
     * get the canvas node which holds IDataObject
     *
     * @param obj IDataObject
     * @return -
     */
    public IGraphNode findGraphNode(Object obj) {
        
        JGoDocument jgoModel = this.getDocument();
        JGoListPosition pos = null;
        JGoObject jgoObj = null;
        
        for (pos = jgoModel.getFirstObjectPos(); pos != null; pos = jgoModel.getNextObjectPos(pos)) {
            
            jgoObj = jgoModel.getObjectAtPos(pos);
            
            if (!(jgoObj instanceof IGraphNode)) {
                continue;
            }
            IGraphNode canvasNode = (IGraphNode) jgoObj;
            if (canvasNode.getDataObject() == obj) {
                return canvasNode;
            }
        }
        return null;
    }
    
    public void highlightInvalidNode(Object dataObj, boolean createSel) {
        
        if (createSel) {
            resetSelectionColors();
        }
        
        JGoSelection sel = this.getSelection();
        
        JGoObject obj = (JGoObject) findGraphNode(dataObj);
        if (obj == null) {
            return;
        }
        
        if (createSel) {
            //set invalid node selection color
            setDefaultPrimarySelectionColor(Color.RED);
            setDefaultSecondarySelectionColor(Color.RED);
            
            sel.clearSelection();
            sel.selectObject(obj);
        } else {
            sel.extendSelection(obj);
        }
        
        this.scrollRectToVisible(obj.getBoundingRect());
    }
    
    public void clearSelection() {
        JGoSelection sel = this.getSelection();
        sel.clearSelection();
    }
    
    public void setXMLInfo(IOperatorXmlInfo xmlInfo){
        this.opXmlInfo = xmlInfo;
    }
    
    public IOperatorXmlInfo getXMLInfo(){
        return opXmlInfo;
    }
    
    public void setSelectedObject(JGoObject obj){
        this.selectedObject = obj;
    }
    
    public JGoObject getSelectedObject(){
        return this.selectedObject;
    }
    
   public void setObserved(JGoView observed) {
       satelliteView.setObserved(this);
       satelliteView.requestFocus();
}
   public BirdsEyeView getSatelliteView(){
       return satelliteView;
   }
   
   public JGoView getObserved(){
       return satelliteView.getObserved();
   }
}
