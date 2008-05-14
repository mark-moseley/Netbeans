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
package org.netbeans.modules.uml.diagrams.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import org.netbeans.api.visual.border.Border;
import java.awt.geom.RoundRectangle2D;

/**
 * @author thuy
 */
public class UMLRoundedBorder implements Border {

    public static final BasicStroke DEFAULT_DASH = new BasicStroke (1, 
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 
            BasicStroke.JOIN_MITER, new float[] { 10, 10 }, 0);
    private int arcWidth;
    private int arcHeight;
    private int insetWidth;
    private int insetHeight;
    private Paint fillColor;
    private Paint drawColor;
    private Stroke stroke;

    /**
     * Creates a rounded rectangle border with specified arc width and height using the specified color
     * @param arcWidth the width of the arc of this rounded rectangle border.
     * @param arcHeight the height of the arc of this rounded rectangle border.
     * @param drawColor the color to draw the border. If null, Black color is used by default.
     */
    public UMLRoundedBorder (int arcWidth, int arcHeight, Paint drawColor) {
        this(arcWidth, arcHeight, 0, 0, null, drawColor, null);
    }
    
    /**
     * Creates a rounded rectangle border with specified arc width and height using the specified color and stroke
     * @param arcWidth the width of the arc of this rounded rectangle border.
     * @param arcHeight the height of the arc of this rounded rectangle border.
     * @param drawColor the color to draw the border.  If null, Black color is used by default.
     * @param stroke the stroke to draw the border.  If null, the current stroke is used.
     */
    public UMLRoundedBorder (int arcWidth, int arcHeight, Paint drawColor, Stroke stroke) {
        this(arcWidth, arcHeight, 0, 0, null, drawColor, stroke);
    }
    
    /**
     *  Creates a rounded rectangle border with specified arc width and height using the specified fill color, draw color and stroke
     * @param arcWidth the width of the arc of this rounded rectangle border.
     * @param arcHeight the height of the arc of this rounded rectangle border.
     * @param fillColor the color to fill the object; if null, the object is not filled.
     * @param drawColor the color to draw the border.  If null, Black color is used by default.
     * @param stroke the stroke to draw the border.  If null, the current stroke is used.
     */
    public UMLRoundedBorder (int arcWidth, int arcHeight, Paint fillColor, Paint drawColor, Stroke stroke) {
        this(arcWidth, arcHeight, 0, 0, fillColor, drawColor, stroke);
    }
    
    /**
     * Creates a rounded rectangle border with specified attributes and the current stroke
     * @param arcWidth the width of the arc of this rounded rectangle border.
     * @param arcHeight the height of the arc of this rounded rectangle border.
     * @param insetWidth
     * @param insetHeight
     * @param fillColor the color to fill the object; if null, the object is not filled.
     * @param drawColor the color to draw the border.  If not set, Black color is used by default.
     */
    public UMLRoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, Paint fillColor, Paint drawColor) {
        this(arcWidth, arcHeight, insetWidth, insetHeight, fillColor, drawColor, null);
    }
    
    /**
     * Creates a rounded rectangle border with specified attributes
     * @param arcWidth the width of the arc of this rounded rectangle border.
     * @param arcHeight the height of the arc of this rounded rectangle border.
     * @param insetWidth
     * @param insetHeight
     * @param fillColor the color to fill the object; if null, the object is not filled.
     * @param drawColor the color to draw the border.  If not set, Black color is used by default.
     * @param stroke the stroke to draw the border.  If null, the current stroke is used.
     */
    public UMLRoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, Paint fillColor, Paint drawColor, Stroke stroke) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        this.fillColor = fillColor;
        this.drawColor =  drawColor != null ? drawColor : Color.BLACK;
        this.stroke = stroke;
    }
    
    public Insets getInsets () {
        return new Insets (insetHeight, insetWidth, insetHeight, insetWidth);
    }
    
    public void setFillColor (Paint val)
    {
        this.fillColor = val;
    }
    
    public Paint getFillColor ()
    {
        return this.fillColor;
    }

    public void paintBackground (Graphics2D gr, Rectangle bounds) 
    {
        Paint previousPaint = gr.getPaint();
        Insets insets = this.getInsets();
        if (fillColor != null) {
            gr.setPaint(fillColor);
            gr.fill (new RoundRectangle2D.Float (
                    bounds.x + insets.left, 
                    bounds.y + insets.top, 
                    bounds.width - insets.left - insets.right, 
                    bounds.height - insets.top - insets.bottom,
                    arcWidth, arcHeight));
            
            if (previousPaint != gr.getPaint())
            {
                gr.setPaint(previousPaint);
            }
        }
    }
    
    public void paint (Graphics2D gr, Rectangle bounds) 
    {
        // paint the border
        Paint previousPaint = gr.getPaint();
        Stroke previousStroke = gr.getStroke();
        if (drawColor != null) {
            gr.setPaint(drawColor);
            if ( stroke != null ) {
                gr.setStroke(stroke);
            }
            gr.draw (new RoundRectangle2D.Float (
                    bounds.x + 0.5f, bounds.y + 0.5f, 
                    bounds.width - 1, bounds.height - 1, 
                    arcWidth, arcHeight));
        }
        // pain the back ground
        paintBackground(gr, bounds);
        
        // reset to the previous paint and stroke
        if (previousPaint != gr.getPaint())
        {
            gr.setPaint(previousPaint);
        }
        
        if (previousStroke != gr.getStroke())
        {
            gr.setStroke(previousStroke);
        }
        
    }

    public boolean isOpaque () {
        return false;
    }
}
