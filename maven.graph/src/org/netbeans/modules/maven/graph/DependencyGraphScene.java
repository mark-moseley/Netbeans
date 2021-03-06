/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint 
 */
public class DependencyGraphScene extends GraphScene<ArtifactGraphNode, ArtifactGraphEdge> {
    
    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    private ArtifactGraphNode rootNode;
    private final AllActionsProvider allActionsP = new AllActionsProvider();
    
//    private GraphLayout layout;
    private WidgetAction moveAction = ActionFactory.createMoveAction(null, allActionsP);
    private WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction(allActionsP);
    private WidgetAction zoomAction = ActionFactory.createMouseCenteredZoomAction(1.1);
    private WidgetAction panAction = ActionFactory.createPanAction();
    private WidgetAction editAction = ActionFactory.createEditAction(allActionsP);

    Action sceneZoomToFitAction = new SceneZoomToFitAction();
    Action highlitedZoomToFitAction = new HighlitedZoomToFitAction();

    private FruchtermanReingoldLayout layout;
    private int maxDepth = 0;
    private final MavenProject project;
    private final Project nbProject;
    private final DependencyGraphTopComponent tc;
    private FitToViewLayout fitViewL;

    private static Set<ArtifactGraphNode> EMPTY_SELECTION = new HashSet<ArtifactGraphNode>();
    
    /** Creates a new instance ofla DependencyGraphScene */
    DependencyGraphScene(MavenProject prj, Project nbProj, DependencyGraphTopComponent tc) {
        project = prj;
        nbProject = nbProj;
        this.tc = tc;
        mainLayer = new LayerWidget(this);
        addChild(mainLayer);
        connectionLayer = new LayerWidget(this);
        addChild(connectionLayer);
        getActions().addAction(this.createObjectHoverAction());
        getActions().addAction(ActionFactory.createSelectAction(allActionsP));
        getActions().addAction(zoomAction);
        getActions().addAction(panAction);
        getActions().addAction(editAction);
        getActions().addAction(popupMenuAction);
    }


    void cleanLayout(JScrollPane panel) {
//        GraphLayout layout = GraphLayoutFactory.createHierarchicalGraphLayout(this, true, false);
//        layout.layoutGraph(this);
        layout =  new FruchtermanReingoldLayout(this, panel);
        layout.invokeLayout();
    }
    
    ArtifactGraphNode getRootGraphNode() {
        return rootNode;
    }

    int getMaxNodeDepth() {
        return maxDepth;
    }

    Project getNbProject () {
        return nbProject;
    }

    MavenProject getMavenProject () {
        return project;
    }

    boolean isAnimated () {
        return true;
    }

    ArtifactGraphNode getGraphNodeRepresentant(DependencyNode node) {
        for (ArtifactGraphNode grnode : getNodes()) {
            if (grnode.represents(node)) {
                return grnode;
            }
        }
        throw new IllegalStateException();
    }
    
    protected Widget attachNodeWidget(ArtifactGraphNode node) {
        if (node.getPrimaryLevel() > maxDepth) {
            maxDepth = node.getPrimaryLevel();
        }
        ArtifactWidget root = new ArtifactWidget(this, node);
        mainLayer.addChild(root);
        node.setWidget(root);
        if (rootNode == null) {
            rootNode = node;
        }
        root.setOpaque(true);
        
        root.getActions().addAction(this.createObjectHoverAction());
        root.getActions().addAction(this.createSelectAction());
        root.getActions().addAction(moveAction);
        root.getActions().addAction(editAction);
        root.getActions().addAction(popupMenuAction);
        
        return root;
    }
    
    protected Widget attachEdgeWidget(ArtifactGraphEdge edge) {
        EdgeWidget connectionWidget = new EdgeWidget(this, edge);
        connectionLayer.addChild(connectionWidget);
        return connectionWidget;
    }
    
    protected void attachEdgeSourceAnchor(ArtifactGraphEdge edge,
            ArtifactGraphNode oldsource,
            ArtifactGraphNode source) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(findWidget(source)));
        
    }
    
    protected void attachEdgeTargetAnchor(ArtifactGraphEdge edge,
            ArtifactGraphNode oldtarget,
            ArtifactGraphNode target) {
        ArtifactWidget wid = (ArtifactWidget)findWidget(target);
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(wid));
    }

    void highlightRelated (ArtifactGraphNode node) {
        List<ArtifactGraphNode> importantNodes = new ArrayList<ArtifactGraphNode>();
        List<ArtifactGraphEdge> otherPathsEdges = new ArrayList<ArtifactGraphEdge>();
        List<ArtifactGraphEdge> primaryPathEdges = new ArrayList<ArtifactGraphEdge>();
        List<ArtifactGraphNode> childrenNodes = new ArrayList<ArtifactGraphNode>();
        List<ArtifactGraphEdge> childrenEdges = new ArrayList<ArtifactGraphEdge>();

        importantNodes.add(node);

        @SuppressWarnings("unchecked")
        List<DependencyNode> children = (List<DependencyNode>)node.getArtifact().getChildren();
        for (DependencyNode n : children) {
            childrenNodes.add(getGraphNodeRepresentant(n));
        }

        childrenEdges.addAll(findNodeEdges(node, true, false));

        // primary path
        addPathToRoot(node.getArtifact(), primaryPathEdges, importantNodes);

        // other important paths
        List<DependencyNode> representants = new ArrayList<DependencyNode>(node.getDuplicatesOrConflicts());
        for (DependencyNode curRep : representants) {
            addPathToRoot(curRep, otherPathsEdges, importantNodes);
        }

        EdgeWidget ew;
        for (ArtifactGraphEdge curE : getEdges()) {
            ew = (EdgeWidget) findWidget(curE);
            if (primaryPathEdges.contains(curE)) {
                ew.setState(EdgeWidget.HIGHLIGHTED_PRIMARY);
            } else if (otherPathsEdges.contains(curE)) {
                ew.setState(EdgeWidget.HIGHLIGHTED);
            } else if (childrenEdges.contains(curE)) {
                ew.setState(EdgeWidget.GRAYED);
            } else {
                ew.setState(EdgeWidget.DISABLED);
            }
        }

        ArtifactWidget aw;
        for (ArtifactGraphNode curN : getNodes()) {
            aw = (ArtifactWidget) findWidget(curN);
            if (importantNodes.contains(curN)) {
                aw.setPaintState(EdgeWidget.REGULAR);
                aw.setReadable(true);
            } else if (childrenNodes.contains(curN)) {
                aw.setPaintState(EdgeWidget.REGULAR);
                aw.setReadable(true);
            } else {
                aw.setPaintState(EdgeWidget.DISABLED);
                aw.setReadable(false);
            }
        }

    }

    private void addPathToRoot(DependencyNode depN, List<ArtifactGraphEdge> edges, List<ArtifactGraphNode> nodes) {
        DependencyNode parentDepN;
        ArtifactGraphNode grNode;
        while ((parentDepN = depN.getParent()) != null) {
            grNode = getGraphNodeRepresentant(parentDepN);
            edges.addAll(findEdgesBetween(grNode, getGraphNodeRepresentant(depN)));
            nodes.add(grNode);
            depN = parentDepN;
        }
    }

    private class AllActionsProvider implements PopupMenuProvider, 
            MoveProvider, EditProvider, SelectProvider {

        private Point moveStart;

/*        public void select(Widget wid, Point arg1, boolean arg2) {
            System.out.println("select called...");
            Widget w = wid;
            while (w != null) {
                ArtifactGraphNode node = (ArtifactGraphNode)findObject(w);
                if (node != null) {
                    setSelectedObjects(Collections.singleton(node));
                    System.out.println("selected object: " + node.getArtifact().getArtifact().getArtifactId());
                    highlightRelated(node);
                    ((ArtifactWidget)w).setSelected(true);
                    return;
                }
                w = w.getParentWidget();
            }
        }*/

        /*** PopupMenuProvider ***/

        @SuppressWarnings("unchecked")
        public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
            JPopupMenu popupMenu = new JPopupMenu();
            if (widget == DependencyGraphScene.this) {
                popupMenu.add(sceneZoomToFitAction);
            } else {
                popupMenu.add(highlitedZoomToFitAction);
                ArtifactGraphNode node = (ArtifactGraphNode)findObject(widget);
                Action a = CommonArtifactActions.createViewArtifactDetails(node.getArtifact().getArtifact(), project.getRemoteArtifactRepositories());
                a.putValue("PANEL_HINT", ArtifactViewer.HINT_GRAPH); //NOI18N
                popupMenu.add(a);
            }
            return popupMenu;
        }

        /*** MoveProvider ***/

        public void movementStarted (Widget widget) {
            widget.bringToFront();
            moveStart = widget.getLocation();
        }
        public void movementFinished (Widget widget) {
            // little hack to call highlightRelated on mouse click while leaving
            // normal move behaviour on real dragging
            Point moveEnd = widget.getLocation();
            if (moveStart.distance(moveEnd) < 5) {
                Object obj = DependencyGraphScene.this.findObject(widget);
                if (obj instanceof ArtifactGraphNode) {
                    DependencyGraphScene.this.highlightRelated((ArtifactGraphNode)obj);
                }
            }
        }
        public Point getOriginalLocation (Widget widget) {
            return widget.getPreferredLocation ();
        }
        public void setNewLocation (Widget widget, Point location) {
            widget.setPreferredLocation (location);
        }

        /*** EditProvider ***/

        public void edit(Widget widget) {
            if (DependencyGraphScene.this == widget) {
                sceneZoomToFitAction.actionPerformed(null);
            } else {
                highlitedZoomToFitAction.actionPerformed(null);
            }
        }

        public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }

        public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return true;
        }

        public void select(Widget widget, Point localLocation, boolean invertSelection) {
            setSelectedObjects(EMPTY_SELECTION);
            DependencyGraphScene.this.tc.depthHighlight();
        }
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);

        if (!previousState.isSelected() && state.isSelected()) {
            tc.depthHighlight();
        }
    }

    private FitToViewLayout getFitToViewLayout () {
        if (fitViewL == null) {
            fitViewL = new FitToViewLayout(this);
        }
        return fitViewL;
    }

    private static class FitToViewLayout extends SceneLayout {

        private List<? extends Widget> widgets = null;
        private DependencyGraphScene depScene;

        public FitToViewLayout(DependencyGraphScene scene) {
            super(scene);
            this.depScene = scene;
        }

        /** Sets list of widgets to fit or null for fitting whole scene */
        public void setWidgetsToFit (List<? extends Widget> widgets) {
            this.widgets = widgets;
        }

        @Override
        protected void performLayout() {
            Rectangle rectangle = null;
            List<? extends Widget> toFit = widgets != null ? widgets : depScene.getChildren();
            for (Widget widget : toFit) {
                if (rectangle == null) {
                    rectangle = widget.convertLocalToScene (widget.getBounds ());
                } else {
                    rectangle = rectangle.union (widget.convertLocalToScene (widget.getBounds ()));
                }
            }
            // margin around
            if (widgets == null) {
                rectangle.grow(5, 5);
            } else {
                rectangle.grow(25, 25);
            }
            Dimension dim = rectangle.getSize();
            Dimension viewDim = depScene.tc.getScrollPane().
                    getViewportBorderBounds ().getSize ();
            double zf = Math.min ((double) viewDim.width / dim.width, (double) viewDim.height / dim.height);
            if (depScene.isAnimated()) {
                if (widgets == null) {
                    depScene.getSceneAnimator().animateZoomFactor(zf);
                } else {
                    CenteredZoomAnimator cza = new CenteredZoomAnimator(depScene.getSceneAnimator());
                    cza.setZoomFactor(zf,
                            new Point((int)rectangle.getCenterX(), (int)rectangle.getCenterY()));
                }
            } else {
                depScene.setZoomFactor (zf);
            }
        }
    }

    private class SceneZoomToFitAction extends AbstractAction {

        public SceneZoomToFitAction() {
            putValue(NAME, NbBundle.getMessage(DependencyGraphScene.class, "ACT_ZoomToFit"));
        }

        public void actionPerformed(ActionEvent e) {
            FitToViewLayout ftvl = DependencyGraphScene.this.getFitToViewLayout();
            ftvl.setWidgetsToFit(null);
            ftvl.invokeLayout();
        }
    };

    private class HighlitedZoomToFitAction extends AbstractAction {

        public HighlitedZoomToFitAction() {
            putValue(NAME, NbBundle.getMessage(DependencyGraphScene.class, "ACT_ZoomToFit"));
        }

        public void actionPerformed(ActionEvent e) {
            @SuppressWarnings("unchecked")
            Collection<ArtifactGraphNode> grNodes = DependencyGraphScene.this.getNodes();
            List<ArtifactWidget> aws = new ArrayList<ArtifactWidget>();
            ArtifactWidget aw = null;
            int paintState;
            for (ArtifactGraphNode grNode : grNodes) {
                aw = grNode.getWidget();
                paintState = aw.getPaintState();
                if (paintState != EdgeWidget.DISABLED && paintState != EdgeWidget.GRAYED) {
                    aws.add(aw);
                }
            }

            FitToViewLayout ftvl = DependencyGraphScene.this.getFitToViewLayout();
            ftvl.setWidgetsToFit(aws);
            ftvl.invokeLayout();
        }
    };

}
