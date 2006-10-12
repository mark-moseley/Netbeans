/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.nbprefuse.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import prefuse.Constants;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.EdgeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.StrokeLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;


/**
 *  JL 2Mar2006
 *  ReferenceEdgeRenderer extends EdgeRenderer so that a hollow
 *  arrowhead can be used.  It has a hardcoded 12 pixel width and 14 pixel height.
 *  ReferenceEdgeRenderer uses a GeneralPath instead of the Polygon used by
 *  EdgeRenderer.  To see the modifications, do a find on "2MAR2006".
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @author Jeri Lockhart>
 */
public class ReferenceEdgeRenderer extends EdgeRenderer {

    protected GeneralPath m_arrowHead = new GeneralPath();  // 2Mar2006
    

    /**
     * Create a new EdgeRenderer.
     */
    public ReferenceEdgeRenderer() {
        super();        
        // wider and taller than default filled arrowhead 
        //  2Mar2006
        m_arrowHead.moveTo(-6, -14);
        m_arrowHead.lineTo(0,0);
        m_arrowHead.lineTo(6,-14);
    }
    
    
    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    protected Shape getRawShape(VisualItem item) {
        EdgeItem   edge = (EdgeItem)item;
        VisualItem item1 = edge.getSourceItem();
        VisualItem item2 = edge.getTargetItem();
        
        int type = m_edgeType;
        
        getAlignedPoint(m_tmpPoints[0], item1.getBounds(),
                m_xAlign1, m_yAlign1);
        getAlignedPoint(m_tmpPoints[1], item2.getBounds(),
                m_xAlign2, m_yAlign2);
        m_curWidth = (float)(m_width * getLineWidth(item));
        
        // create the arrow head, if needed
        EdgeItem e = (EdgeItem)item;
        if ( e.isDirected() && m_edgeArrow != Constants.EDGE_ARROW_NONE ) {
            // get starting and ending edge endpoints
            boolean forward = (m_edgeArrow == Constants.EDGE_ARROW_FORWARD);
            Point2D start = null, end = null;
            start = m_tmpPoints[forward?0:1];
            end   = m_tmpPoints[forward?1:0];
            
            // compute the intersection with the target bounding box
            VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();
            int i = GraphicsLib.intersectLineRectangle(start, end,
                    dest.getBounds(), m_isctPoints);
            if ( i > 0 ) end = m_isctPoints[0];
            
            // create the arrow head shape
            AffineTransform at = getArrowTrans(start, end, m_curWidth);
            m_curArrow = at.createTransformedShape(m_arrowHead);
            
            // Don't shorten the edge --  2Mar2006
            
            // update the endpoints for the edge shape
            // need to bias this by arrow head size
//            Point2D lineEnd = m_tmpPoints[forward?1:0];
//            lineEnd.setLocation(0, -m_arrowHeight);
//            at.transform(lineEnd, lineEnd);
        } else {
            m_curArrow = null;
        }
        
        // create the edge shape
        Shape shape = null;
        double n1x = m_tmpPoints[0].getX();
        double n1y = m_tmpPoints[0].getY();
        double n2x = m_tmpPoints[1].getX();
        double n2y = m_tmpPoints[1].getY();
        switch ( type ) {
            case Constants.EDGE_TYPE_LINE:
                m_line.setLine(n1x, n1y, n2x, n2y);
                shape = m_line;
                break;
            case Constants.EDGE_TYPE_CURVE:
                getCurveControlPoints(edge, m_ctrlPoints,n1x,n1y,n2x,n2y);
                m_cubic.setCurve(n1x, n1y,
                        m_ctrlPoints[0].getX(), m_ctrlPoints[0].getY(),
                        m_ctrlPoints[1].getX(), m_ctrlPoints[1].getY(),
                        n2x, n2y);
                shape = m_cubic;
                break;
            default:
                throw new IllegalStateException("Unknown edge type");
        }
        
        // return the edge shape
        return shape;
    }
    
    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
     */
    public void render(Graphics2D g, VisualItem item) {
        // call super.super.render = AbstractShapeRenderer.render()
        Shape shape = getShape(item);
        if (shape != null)
            drawShape(g, item, shape);
        
        // don't call super.render() (EdgeRenderer.render()
        // because it uses g.fill to render the default polygon arrowhead
        
        // render the edge arrow head
        if ( m_curArrow != null ) {
            //  2MAR2006
            // draw the arrowhead, don't fill it   2MAR2006
            g.setPaint(ColorLib.getColor(item.getFillColor()));
//            g.fill(m_curArrow);
            g.draw(m_curArrow);
        }
    }


    
    
} // end of class ReferenceEdgeRenderer
