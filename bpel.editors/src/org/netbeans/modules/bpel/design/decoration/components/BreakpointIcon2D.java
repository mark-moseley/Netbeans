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

package org.netbeans.modules.bpel.design.decoration.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author aa160298
 */
public class BreakpointIcon2D extends Icon2D {
    private boolean enabled;
    public BreakpointIcon2D(boolean enabled){
        super();
        this.enabled = enabled;
    }
    
    protected void paint(Graphics2D g2) {
        g2.setPaint(enabled ? FILL_COLOR : FILL_COLOR.brighter());
        g2.fill(SHAPE);
        g2.setPaint(enabled ? STROKE_COLOR : STROKE_COLOR.brighter());
        g2.setStroke(STROKE);
        g2.draw(SHAPE);
    }

    protected double getDesignOriginX() { return DESIGN_SIZE / 2; }
    protected double getDesignOriginY() { return DESIGN_SIZE / 2; }    

    protected double getDesignWidth() { return DESIGN_SIZE; }
    protected double getDesignHeight() { return DESIGN_SIZE; }    


    private static final double DESIGN_SIZE = 10;

    private static final Color FILL_COLOR = new Color(0xFFA0A0);
    private static final Color STROKE_COLOR = new Color(0xFF0000);
    
    
    private static final BasicStroke STROKE = new BasicStroke(1, 
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final Shape SHAPE;
    
    static {
        float half = (float) (DESIGN_SIZE - STROKE.getLineWidth()) / 2;

        SHAPE = new Rectangle2D.Float(-half, -half, 2 * half, 2 * half);
    }
    
}
