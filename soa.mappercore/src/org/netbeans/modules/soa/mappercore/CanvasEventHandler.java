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
package org.netbeans.modules.soa.mappercore;

import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;

/**
 *
 * @author anjeleevich
 */
public class CanvasEventHandler extends AbstractMapperEventHandler {

    private MouseEvent initialEvent = null;
    private AutoSelectionCanvas  autoSelection;

    public CanvasEventHandler(Canvas canvas) {
        super(canvas.getMapper(), canvas);
        new AutoSelectionCanvas(canvas);
    }

    private void reset() {
        initialEvent = null;

    }

    public void mouseReleased(MouseEvent e) {
        CanvasSearchResult searchResult = getCanvas().find(e.getX(), e.getY());
        if (searchResult != null) {
            SelectionModel selectionModel = getSelectionModel();
            if (e.isControlDown()) {
                if (searchResult != null) {
                    selectionModel.switchSelected(
                            searchResult.getTreePath(),
                            searchResult.getGraphItem());
                }
            } else {
                if (selectionModel.isSelected(searchResult.getTreePath(), 
                        searchResult.getGraphItem()))
                {
                    selectionModel.setSelected(
                            searchResult.getTreePath(), 
                            searchResult.getGraphItem());
                }
            }
        }
        reset();
    }

    public void mousePressed(MouseEvent e) {
        reset();

        Canvas canvas = getCanvas();
        
        if (!canvas.hasFocus()) {
            canvas.requestFocusInWindow();
        }

        SelectionModel selectionModel = getSelectionModel();

        int y = e.getY();
        int x = e.getX();

        CanvasSearchResult searchResult = getCanvas().find(x, y);
        if (searchResult != null) {
            TreePath selectedTreePath = searchResult.getTreePath();
            GraphItem selectedItem = searchResult.getGraphItem();

            if (selectedItem != null) {
                if (!e.isControlDown()) {
                    if (!selectionModel.isSelected(selectedTreePath, selectedItem)) {
                        selectionModel.setSelected(selectedTreePath, selectedItem);
                    }
                }
            } else {
                if (!e.isControlDown()) {
                    selectionModel.setSelected(selectedTreePath);
                }
            }

        }
        this.initialEvent = e;
    }

    public void mouseDragged(MouseEvent e) {
        if ((initialEvent != null) && (initialEvent.getPoint().distance(e.getPoint()) >= 5)) {

            LinkTool linkTool = getMapper().getLinkTool();
            MoveTool moveTool = getMapper().getMoveTool();
            Transferable transferable = null;
            CanvasSearchResult result = getCanvas().find(initialEvent.getX(), initialEvent.getY());

            if (result.getPinItem() instanceof Vertex) {
                Vertex vertex = (Vertex) result.getPinItem();
                Link link = vertex.getOutgoingLink();
                
                if (link == null) {
                    transferable = linkTool.activateOutgoing(
                            result.getTreePath(), vertex);
                } else {
                    TargetPin targetPin = link.getTarget();
                    if (targetPin instanceof VertexItem) {
                        transferable = linkTool.activateIngoing(
                                result.getTreePath(), (VertexItem) targetPin);
                    } else if (targetPin instanceof Graph) {
                        transferable = linkTool.activateIngoing(
                                result.getTreePath(), (Graph) targetPin, link);
                    }
                }
            } else if (result.getPinItem() instanceof VertexItem) {
                VertexItem vertexItem = (VertexItem) result.getPinItem();
                Link oldLink = vertexItem.getIngoingLink();
                
                if (oldLink == null) {
                    transferable = linkTool.activateIngoing(
                            result.getTreePath(),
                            (VertexItem) result.getPinItem());
                } else {
                    SourcePin sourcePin = oldLink.getSource();
                    if (sourcePin instanceof Vertex) {
                        transferable = linkTool.activateOutgoing(
                                result.getTreePath(), (Vertex) sourcePin);
                    } else if (sourcePin instanceof TreeSourcePin) {
                        transferable = linkTool.activateOutgoing(
                                (TreeSourcePin) sourcePin, oldLink, 
                                result.getTreePath());
                    }
                }
            } else if (result.getGraphItem() instanceof Vertex) {

                transferable = moveTool.getMoveTransferable(
                        getSelectionModel().getSelectedSubset());
            }
            if (transferable != null) {
                startDrag(initialEvent, transferable, MOVE);
            }

            reset();
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int x = e.getX();
            int y = e.getY();

            CanvasSearchResult searchResult = getCanvas().find(x, y);
            GraphItem item = (searchResult == null) ? null
                    : searchResult.getGraphItem();

            if (item instanceof VertexItem) {
                getCanvas().startEdit(searchResult.getTreePath(),
                        (VertexItem) item);
            }
            Mapper mapper = getMapper();
            MapperNode node = mapper.getNodeAt(y);
            if (node != null  && node.getGraph() != null) {
                if (node.isGraphCollapsed()) {
                    mapper.setExpandedGraphState(node.getTreePath(), true);
                } else if (searchResult == null ||
                        searchResult.getGraphItem() == null) {
                    mapper.setExpandedGraphState(node.getTreePath(), false);
                }
                getLinkTool().dragDone();
            }
        }
    }
}
