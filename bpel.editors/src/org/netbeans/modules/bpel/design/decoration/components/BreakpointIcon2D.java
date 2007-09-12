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
