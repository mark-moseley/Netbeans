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

package org.netbeans.modules.compapp.casaeditor.graph.awt;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Paints a gradient in the given rectangle.
 * @author Josh Sandusky
 */
public class GradientRectanglePainter extends RectangularChainPainter {
    
    
    public GradientRectanglePainter(RectangularPaintProvider provider, Painter nextPainter) {
        super(provider, nextPainter);
    }
    
    
    public void chainPaint(Graphics2D graphics)
    {
        GradientRectangleProvider provider = (GradientRectangleProvider) getProvider();
        Rectangle bounds = provider.getClipRect();
        GradientRectangleColorScheme colorScheme = provider.getGradientColorScheme();
        boolean isVertical = provider.isVertical();
        
        int offset  = bounds.y;
        int span    = 0;
        int maxSpan = isVertical ? bounds.height : bounds.width;
        
        span = (int) (maxSpan * 0.3f);
        offset = drawGradient(graphics, bounds, colorScheme.getColor1(), colorScheme.getColor2(), offset, span, isVertical);
        
        span = (int) (maxSpan * 0.464f);
        offset = drawGradient(graphics, bounds, colorScheme.getColor2(), colorScheme.getColor3(), offset, span, isVertical);
        
        span = (int) (maxSpan * 0.163f);
        offset = drawGradient(graphics, bounds, colorScheme.getColor3(), colorScheme.getColor4(), offset, span, isVertical);
        
        span = (int) (maxSpan - offset);
        offset = drawGradient(graphics, bounds, colorScheme.getColor4(), colorScheme.getColor5(), offset, span, isVertical);
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
}
