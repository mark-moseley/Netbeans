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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;


/**
 * Paints a gradient in the given rectangle.
 * @author Josh Sandusky
 */
public class GradientRectangleDrawer {
    
    private static final int ARC_WIDTH  =  4;
    private static final int ARC_HEIGHT =  4;
    private static final int BORDER_PERIMETER_WIDTH = 2;
    

    public static void paintGradientBackground(
            Graphics2D graphics, 
            Rectangle bounds, 
            boolean isVertical, 
            boolean isRounded,
            GradientRectangleColorScheme colors,
            boolean isBorderShown,
            CustomPainter customPainter)
    {
        Shape previousClip = graphics.getClip();
        if (isRounded) {
            graphics.clip(new RoundRectangle2D.Float(
                    bounds.x, bounds.y, 
                    bounds.width, bounds.height, 
                    ARC_WIDTH, ARC_HEIGHT));
        } else {
            graphics.clip(new Rectangle2D.Float(
                    bounds.x, bounds.y, 
                    bounds.width, bounds.height));
        }
        
        int offset  = bounds.y;
        int span    = 0;
        int maxSpan = isVertical ? bounds.height : bounds.width;
        
        span = (int) (maxSpan * 0.3f);
        offset = drawGradient(graphics, bounds, colors.getColor1(), colors.getColor2(), offset, span, isVertical);
        
        span = (int) (maxSpan * 0.464f);
        offset = drawGradient(graphics, bounds, colors.getColor2(), colors.getColor3(), offset, span, isVertical);
        
        span = (int) (maxSpan * 0.163f);
        offset = drawGradient(graphics, bounds, colors.getColor3(), colors.getColor4(), offset, span, isVertical);
        
        span = (int) (maxSpan - offset);
        offset = drawGradient(graphics, bounds, colors.getColor4(), colors.getColor5(), offset, span, isVertical);
        
        if (isBorderShown) {
            // Draw the border line.
            Stroke originalStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke(BORDER_PERIMETER_WIDTH));
            graphics.setColor(colors.getBorderColor());
            if (isRounded) {
                graphics.draw(new RoundRectangle2D.Float(
                        bounds.x + 0.5f, 
                        bounds.y + 0.5f, 
                        bounds.width - 1, 
                        bounds.height - 1, 
                        ARC_WIDTH, 
                        ARC_HEIGHT));
            } else {
                graphics.draw(new Rectangle2D.Float(
                        bounds.x, 
                        bounds.y, 
                        bounds.width, 
                        bounds.height));
            }
            graphics.setStroke(originalStroke);
        }
        
        if (customPainter != null) {
            customPainter.paint(graphics);
        }
        
        graphics.setClip(previousClip);
    }

    private static int drawGradient(
            Graphics2D graphics, 
            Rectangle bounds, 
            Color color1, 
            Color color2, 
            int offset, 
            int span,
            boolean isVertical)
    {
        if (isVertical) {
            graphics.setPaint(new GradientPaint(bounds.x, offset, color1, bounds.x, offset + span, color2));
            graphics.fill(new Rectangle(bounds.x, offset, bounds.x + bounds.width, offset + span));
        } else {
            graphics.setPaint(new GradientPaint(offset, bounds.y, color1, offset + span, bounds.y, color2));
            graphics.fill(new Rectangle(offset, bounds.y, offset + span, bounds.y + bounds.height));
        }
        
        return offset + span;
    }
    
    
    static abstract class CustomPainter {
        public abstract void paint(Graphics g);
    }
}
