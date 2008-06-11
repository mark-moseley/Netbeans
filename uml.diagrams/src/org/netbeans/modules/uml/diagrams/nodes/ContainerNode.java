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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.ResizeProvider.ControlPoint;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.actions.ResizeStrategyProvider;
import org.netbeans.modules.uml.drawingarea.actions.WindowStyleResizeProvider;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.view.AlignWithMoveStrategyProvider;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.GraphSceneNodeAlignCollector;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;

/**
 *
 * @author treyspiva
 */
public abstract class ContainerNode extends UMLNodeWidget implements org.netbeans.modules.uml.drawingarea.widgets.ContainerNode
{
    private ContainerResizeProvider resizeProvider;
    
    public ContainerNode(Scene scene)
    {
        this(scene,false);
    }
    public ContainerNode(Scene scene,boolean useDefaultNodeResource)
    {
        super(scene, useDefaultNodeResource);
        
        if(scene instanceof DesignerScene)
        {
            DesignerScene designerScene = (DesignerScene)scene;
            AlignWithMoveDecorator decorator = new AlignWithMoveDecorator()
            {
                public ConnectionWidget createLineWidget(Scene scene)
                {
                    ConnectionWidget widget = new ConnectionWidget(scene);
                    widget.setStroke(DiagramEngine.ALIGN_STROKE);
                    widget.setForeground(Color.BLUE);
                    return widget;
                }
            };

            AlignWithMoveStrategyProvider moveProvider = 
                    new AlignWithMoveStrategyProvider(new GraphSceneNodeAlignCollector (designerScene),
                                                      designerScene.getInterractionLayer(),
                                                      designerScene.getMainLayer(),
                                                      decorator,
                                                      false)
            {
                // I do not want the calculateChildren to be called when the 
                // user simply selects the node.  When the user moves the node
                // more than two pixels then calculate will be set to true.
                // When the movement is done, calculate will be set back to 
                // false.
                private boolean calculate =  false;
                
                @Override
                public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation)
                {
                    Point retVal = super.locationSuggested(widget, originalLocation, suggestedLocation);
                    
                    if(Math.abs(originalLocation.x - retVal.x) > 2)
                    {
                        calculate = true;
                    }
                    
                    if(Math.abs(originalLocation.y - retVal.y) > 2)
                    {
                        calculate = true;
                    }
                    
                    return retVal;
                }
                
                @Override
                public void movementFinished (Widget widget)
                {   
                    super.movementFinished(widget);
                    if((getContainer() != null) && (calculate == true))
                    {
                        getContainer().calculateChildren(false);
                        calculate = false;
                    }
                }
            };
            
            addToLookup(moveProvider);
        }
    }
    
    @Override
    public ResizeStrategyProvider getResizeStrategyProvider()
    {
        if(resizeProvider==null)
        {
            resizeProvider=new ContainerResizeProvider(getResizeControlPoints());
        }
        return resizeProvider;
    }
    
    @Override
    public void addContainedChild(Widget widget)
    {
        widget.removeFromParent();
        getContainer().addChild(widget);
//        firePropertyChange(ContainerWidget.CHILDREN_CHANGED, null, null);
    }

    @Override
    protected void saveAnchorage(NodeWriter nodeWriter)
    {
        //write anchor info
        DesignerScene dScene = (DesignerScene)this.getScene();
        // First get only output edges (to have proper src node anchor assigned)
        Collection outEdgeList = dScene.findNodeEdges(this.getObject(), true, false); 
        for (Iterator it = outEdgeList.iterator(); it.hasNext();) {
            IPresentationElement pE = (IPresentationElement)it.next();
            Widget widget = dScene.findWidget(pE);
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget connectionWidget = (ConnectionWidget)(widget);
                Anchor srcAnchor = connectionWidget.getSourceAnchor();
                PersistenceUtil.addAnchor(srcAnchor); // this is to cross ref the anchor ID from the edge later on..                
                nodeWriter.addAnchorEdge(srcAnchor, PersistenceUtil.getPEID(connectionWidget));
            }            
        }
        // Now get all in edges (to have proper target anchor assigned)
        Collection inEdgeList = dScene.findNodeEdges(this.getObject(), false, true); 
        for (Iterator it = inEdgeList.iterator(); it.hasNext();) {
            IPresentationElement pE = (IPresentationElement)it.next();
            Widget widget = dScene.findWidget(pE);
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget connectionWidget = (ConnectionWidget)(widget);
                Anchor targetAnchor = connectionWidget.getTargetAnchor();
                PersistenceUtil.addAnchor(targetAnchor); // this is to cross ref the anchor ID from the edge later on..                
                nodeWriter.addAnchorEdge(targetAnchor, PersistenceUtil.getPEID(connectionWidget));
            }            
        }        
        nodeWriter.writeAnchorage();
        //done writing the anchoredgemap.. now time to clear it.
        nodeWriter.clearAnchorEdgeMap();

    }
    
    protected class ContainerResizeProvider extends WindowStyleResizeProvider
    {
        private ArrayList < Widget > children = new ArrayList < Widget >();
        private int prevIndex = -1;
        
        private double leftBounds = Double.MAX_VALUE;
        private double topBounds = Double.MAX_VALUE;
        private double rightBounds = Double.MIN_VALUE;
        private double bottomBounds = Double.MIN_VALUE;
        
        public ContainerResizeProvider()
        {
            
        }

        public ContainerResizeProvider(ControlPoint[] points)
        {
            super(points);
        }
        
        @Override
        public void resizingFinished(Widget widget)
        {
            super.resizingFinished(widget);
            
            Widget container = getContainer();
            if(container!=null)
            {
                for(Widget child : children)
                {
                    Point location = container.convertSceneToLocal(child.getBounds().getLocation());
                    child.setPreferredLocation(location);

                    child.removeFromParent();
                    container.addChild(child);
                }

                if(getContainer() != null)
                {
                    getContainer().calculateChildren(true);
                }

                // Now put the node back to its original index.  See resizeStarted
                // for more information.
                Widget parent = getParentWidget();
                parent.removeChild(ContainerNode.this);
                
                // If the new number of children is now less than before the 
                // resize then add the container node to the end.
                if(parent.getChildren().size() < prevIndex)
                {
                    parent.addChild(ContainerNode.this);
                }
                else
                {
                    parent.addChild(prevIndex, ContainerNode.this);
                }
                prevIndex = -1;

                children.clear();
            }
        }

        @Override
        public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
            
            suggestedBounds = super.boundsSuggested(widget, originalBounds, suggestedBounds, controlPoint);
            suggestedBounds = restrictBounds( widget, suggestedBounds);
            
            if(widget instanceof UMLNodeWidget)
            {
                UMLNodeWidget nw=(UMLNodeWidget) widget;
                if(nw.getContentBounds()!=null)
                {
                    //correct suggested bounds
                    suggestedBounds.add(nw.getContentBounds());//suggested should cover content
                }
            }
            return suggestedBounds;
        }

        @Override
        public void resizingStarted(Widget widget)
        {
            super.resizingStarted(widget);
            // Next remove the children.  That way they are above the container.
            Widget container = getContainer();
            if(container!=null)
            {
                // Fix for issue 12677.  The location of a child is relative to the
                // parents top left corner.  If you resize the node by using the 
                // top point, you change the location of the children as well.  
                //
                // To solve this issue I will first have to remove the children 
                // from the container, then add them to the containers node parent.
                // 
                // A second problem is that when resizing the container node, it 
                // should be below the nodes that it may contain.  Therefore add 
                // the node to at the 0 index, so it is shown below all other nodes.
                // After the resize is complete is should be added back to the same
                // index.
                Widget parent = getParentWidget();
                prevIndex = parent.getChildren().indexOf(ContainerNode.this);
                parent.removeChild(ContainerNode.this);
                parent.addChild(0, ContainerNode.this);

                children.addAll(container.getChildren());

                Widget containerParent = container.getParentWidget();
                Insets insets = null;
                if(containerParent.getBorder() != null)
                {
                    insets = containerParent.getBorder().getInsets();
                }
                
                Point containerLoc = containerParent.convertLocalToScene(container.getLocation());
                Point nodeLoc = getParentWidget().convertLocalToScene(getLocation());
                
                int dx = containerLoc.x - nodeLoc.x;
                if(insets != null)
                {
                    dx += insets.left;
                }
                
                int dy = containerLoc.y - nodeLoc.y;
                if(insets != null)
                {
                    dy += insets.top;
                }
                
                leftBounds = Double.MAX_VALUE;
                topBounds = Double.MAX_VALUE;
                rightBounds = Double.MIN_VALUE;
                bottomBounds = Double.MIN_VALUE;
        
                for(Widget child : children)
                {
                    Point location = child.getLocation();
                    Point childSceneLocation = child.getParentWidget().convertLocalToScene(location);
                    
                    double width = child.getClientArea().width;
                    double height = child.getClientArea().height;
                    
                    leftBounds = Math.min(leftBounds, childSceneLocation.getX() - dx);
                    topBounds = Math.min(topBounds, childSceneLocation.getY() - dy);
                    rightBounds = Math.max(rightBounds, childSceneLocation.getX() + dx + width);
                    bottomBounds = Math.max(bottomBounds, childSceneLocation.getY() + height + 
                                            (insets != null ? insets.top + insets.bottom : 0));

                    child.setPreferredLocation(childSceneLocation);
                    container.removeChild(child);
                    parent.addChild(child);
                }
            } 
        }
    
        private Rectangle restrictBounds(Widget widget, Rectangle suggestedBounds)
        {
            Rectangle testBounds = widget.convertLocalToScene(suggestedBounds);
            int x = testBounds.x;
            int y = testBounds.y;
            int width = testBounds.width;
            int height = testBounds.height;

            if (testBounds.x > leftBounds)
            {
                x = (int) leftBounds;
                width = (int) (testBounds.x + testBounds.width) - x;
            }

            if (testBounds.y > topBounds)
            {
                y = (int) topBounds;
                height = (int) (testBounds.y + testBounds.height) - y;
            }

            if ((testBounds.x + testBounds.width) < rightBounds)
            {
                width = (int) rightBounds - x;
            }

            if ((testBounds.y + testBounds.height) < bottomBounds)
            {
                height = (int) bottomBounds - y;
            }

            Point newLocation = convertSceneToLocal(new Point((int) x, (int) y));
            return new Rectangle(newLocation.x, newLocation.y, width, height);
        }
    }

    @Override
    public Dimension getDefaultMinimumSize() {
        //TBD later it may calculate actual content, but first realization same as in 6.1, reszie to content do nothing
        Dimension size=getBounds().getSize();
        return size;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Abstract Methods
    
    public abstract ContainerWidget getContainer();
    
}
