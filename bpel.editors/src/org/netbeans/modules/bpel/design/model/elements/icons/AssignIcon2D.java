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

package org.netbeans.modules.bpel.design.model.elements.icons;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author anjeleevich
 */
public class AssignIcon2D extends Icon2D {
    
    private AssignIcon2D() {}

    public void paint(Graphics2D g2) {
        g2.setStroke(STROKE);
        g2.setPaint(COLOR);
        g2.draw(SHAPE);
    }

    public static final Icon2D INSTANCE = new AssignIcon2D();
    
    private static final Shape SHAPE;
    
    static {
        GeneralPath gp = new GeneralPath(new RoundRectangle2D
                .Float(-12, -6, 24, 12, 4, 4));
        gp.moveTo(-5, -2);
        gp.lineTo(5, -2);
        gp.moveTo(-5, 2);
        gp.lineTo(5, 2);
        
        SHAPE = gp;
    }
}
