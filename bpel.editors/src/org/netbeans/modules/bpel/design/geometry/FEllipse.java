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

package org.netbeans.modules.bpel.design.geometry;

import org.netbeans.modules.bpel.design.geometry.FShape.EmptyPathIterator;
import org.netbeans.modules.bpel.design.geometry.FShape.LinePathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;


public class FEllipse extends FShape {


    public FEllipse(double size) {
        super(0.0, 0.0, size, size);
    }
    
    
    public FEllipse(double width, double height) {
        super(0.0, 0.0, width, height);
    }
    
    
    public FEllipse(double x, double y, double width, double height) {
        super(x, y, width, height);
    }
    
    
    public FEllipse enlarge(double v) {
        double v2 = v * 2;
        return new FEllipse(x - v, y - v, width + v2, height + v2);
    }
    
    
    public FEllipse reshape(double x, double y, double w, double h) {
        return new FEllipse(x, y, w, h);
    }
    
    
    public boolean intersect(FIntersector intersector) {
        double rx = (double) width / 2.0;
        double ry = (double) height / 2.0;
        double cx = (double) x + rx;
        double cy = (double) y + ry;
        
        intersector.intersectByEllipse(cx, cy, rx, ry);
        
        return intersector.ok();
    }    
    
    
    public boolean contains(double px, double py) {
        if (!super.contains(px, py)) return false;
        
        double rx = (double) width / 2.0;
        double ry = (double) height / 2.0;
        
        px -= (double) x + rx;
        py -= (double) y + ry;

        px *= px;
        py *= py;
        
        rx *= rx;
        ry *= ry;

        if ((float) ry == 0.0f) {
            if ((float) rx == 0.0f) {
                return ((float) px == 0.0f) && ((float) py == 0.0f);
            } else {
                return ((float) py == 0.0f) && (px <= ry);
            }
        } else if ((float) rx == 0.0f) {
            return ((float) px == 0.0f) && (py <= ry);
        }
        
        return px / rx + py / ry <= 1.0;
    }
    
    
    
    public PathIterator getPathIterator(AffineTransform at) {
        if (width == 0.0f) {
            return (height == 0.0f) 
                    ? new EmptyPathIterator() 
                    : new LinePathIterator(at);
        } else if (height == 0.0f) {
            return new LinePathIterator(at);
        }

        return new EllipsePathIterator(at);
    }
    
    
    private class EllipsePathIterator implements PathIterator {
        
        private AffineTransform at;
        private int index = 0;
        
        
        public EllipsePathIterator(AffineTransform at) {
            this.at = at;
        }
        
        
        public int currentSegment(double[] coords) {
            if (index >= TYPE.length) {
                return PathIterator.SEG_CLOSE;
            }
            
            if (index == 0) {
                coords[0] = x + width / 2.0;
                coords[1] = y;
                if (at != null) {
                    at.transform(coords, 0, coords, 0, 1);
                }
                return PathIterator.SEG_LINETO;
            }

            double[] k = K[index];
           
            double x = FEllipse.this.x;
            double y = FEllipse.this.y;
            
            double w = width;
            double h = height;
            
            coords[0] = x + k[0] * w;
            coords[1] = y + k[1] * h;
            coords[2] = x + k[2] * w;
            coords[3] = y + k[3] * h;
            coords[4] = x + k[4] * w;
            coords[5] = y + k[5] * h;
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 3);
            }
            
            return PathIterator.SEG_CUBICTO;
        }

        
        public int currentSegment(float[] coords) {
            if (index >= TYPE.length) {
                return PathIterator.SEG_CLOSE;
            }
            
            if (index == 0) {
                coords[0] = x + width / 2.0f;
                coords[1] = y;
                if (at != null) {
                    at.transform(coords, 0, coords, 0, 1);
                }
                return PathIterator.SEG_MOVETO;
            }

            double[] k = K[index];
           
            double x = FEllipse.this.x;
            double y = FEllipse.this.y;
            
            double w = width;
            double h = height;
            
            coords[0] = (float) (x + k[0] * w);
            coords[1] = (float) (y + k[1] * h);
            coords[2] = (float) (x + k[2] * w);
            coords[3] = (float) (y + k[3] * h);
            coords[4] = (float) (x + k[4] * w);
            coords[5] = (float) (y + k[5] * h);
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 3);
            }
            
            return PathIterator.SEG_CUBICTO;
        }

        
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }

        
        public boolean isDone() {
            return index > TYPE.length;
        }

        
        public void next() {
            index++;
        }
    }
    
    
    private static final int TYPE[] = {
        PathIterator.SEG_MOVETO,
        PathIterator.SEG_CUBICTO,
        PathIterator.SEG_CUBICTO,
        PathIterator.SEG_CUBICTO,
        PathIterator.SEG_CUBICTO,
    };
    
    
    private static final double V = 0.5 - 2.0 * (Math.sqrt(2.0) - 1.0) / 3.0;
    
    private static final double K[][] = {
        { 0.5, 0.0 },
        { 1.0 - V, 0.0, 1.0, V, 1.0, 0.5},
        { 1.0, 1.0 - V, 1.0 - V, 1.0, 0.5, 1.0 },
        { V, 1.0, 0.0, 1.0 - V, 0.0, 0.5 },
        { 0.0, V, V, 0.0, 0.5, 0.0 }
    };    
}
