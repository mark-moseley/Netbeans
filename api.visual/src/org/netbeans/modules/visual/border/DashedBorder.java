/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visual.border;

import org.netbeans.api.visual.border.Border;

import java.awt.*;

/**
 * @author alex_grk
 */
public class DashedBorder implements Border {

    private static final BasicStroke BASIC_STROKE = new BasicStroke ();

    protected int thickness = 1;
    protected Color color;

    private BasicStroke stroke = BASIC_STROKE;

    public DashedBorder (Color color, float l1, float l2) {
        this (color, new float[] { l1, l2 }, 1);
    }

    public DashedBorder (Color color, float[] dash, int thickness) {
        if (thickness < 1) {
            throw new IllegalArgumentException ("Invalid thickness: " + thickness);
        }
        this.thickness = thickness;
        this.color = color != null ? color : Color.BLACK;
        stroke = new BasicStroke (thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER, dash, 0);
    }

    public Insets getInsets () {
        return new Insets (thickness, thickness, thickness, thickness);
    }

    public void paint (Graphics2D g, Rectangle bounds) {
        Stroke s = g.getStroke ();
        g.setColor (color);
        g.setStroke (stroke);
        g.drawRect (bounds.x, bounds.y, bounds.width - thickness, bounds.height - thickness);
        g.setStroke (s);
    }

    public boolean isOpaque () {
        return true;
    }

}
