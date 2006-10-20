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


package org.netbeans.modules.bpel.design.model.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.geom.FPath;
import org.netbeans.modules.bpel.design.geom.FPoint;
import org.netbeans.modules.bpel.design.geom.FShape;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

public class Connection {
    
    
    private VisualElement source;
    private VisualElement target;

    /** 
     * directions can be changed in derived classes
     */
    private Direction sourceDirection;
    private Direction targetDirection;
    
    private Pattern pattern;
    
    private boolean paintArrow = true;
    private boolean paintSlash = false;
    private boolean paintDashed = false;
    private boolean paintCircle = false;
    private FPoint endPoint;
    private FPoint startPoint;
    
    private float x1 = 0, y1 = 0;
    private float dx, dy;
    

    private final int uid;
    
    private boolean needsRedraw = true;
    
    private FPath path;
    
    
    public Connection(Pattern pattern) {
        this.pattern = pattern;
        uid = uidCounter++;
        pattern.addConnection(this);
    }
    
    
    public void setPaintArrow(boolean b) { paintArrow = b; }
    public void setPaintCircle(boolean b) { paintCircle = b; }
    public void setPaintSlash(boolean b) { paintSlash = b; }
    public void setPaintDashed(boolean b) { paintDashed = b; }
    
    
    public boolean isPaintArrow() { return paintArrow; }
    public boolean isPaintCircle() { return paintCircle; }
    public boolean isPaintSlash() { return paintSlash; }
    public boolean isPaintDashed() { return paintDashed; }
    public FPoint getStartPoint() {return startPoint;}
    public FPoint getEndPoint() {return endPoint;}
    
    
    public void setStartAndEndPoints(FPoint startPoint, FPoint endPoint) {
        
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        
        float newX1 = startPoint.x;
        float newY1 = startPoint.y;
        float newX2 = endPoint.x;
        float newY2 = endPoint.y;
        
        float newDX = newX2 - newX1;
        float newDY = newY2 - newY1;
        
        float oldX1 = this.x1;
        float oldY1 = this.y1;
        
        float oldDX = this.dx;
        float oldDY = this.dy;
        
        final float epsilon = 0.33f;
        
        if (needsRedraw || newX1 != oldX1 || newY1 != oldY1 
                || newDX != oldDX || newDY != oldDY)
        {
            this.x1 = newX1;
            this.y1 = newY1;
            this.dx = newDX;
            this.dy = newDY;
            update();
        }
    }
    
    
    protected float findXStep(float x1, float dx) {
        return x1 + dx / 2;
    }
    
    
    protected float findYStep(float y1, float dy) {
        return y1 + dy / 2;
    }
    
    
    protected void update() {
        float dx = this.dx;
        float dy = this.dy;
        
        float cx = findXStep(x1, dx) - x1;
        float cy = findYStep(y1, dy) - y1;

        FPoint[] points = null;
        
        if (sourceDirection == targetDirection) {
            float t;
            switch (sourceDirection) {
                case TOP:
                    t = Math.min(0, dy) - BRACKET_SIZE;
                    points = new FPoint[] {
                        new FPoint(0, 0),
                        new FPoint(0, t),
                        new FPoint(dx, t),
                        new FPoint(dx, dy)
                    };
                    break;
                case BOTTOM:
                    t = Math.max(0, dy) + BRACKET_SIZE;
                    points = new FPoint[] {
                        new FPoint(0, 0),
                        new FPoint(0, t),
                        new FPoint(dx, t),
                        new FPoint(dx, dy)
                    };
                    break;
                case LEFT:
                    t = Math.min(0, dx) - BRACKET_SIZE;
                    points = new FPoint[] {
                        new FPoint(0, 0),
                        new FPoint(t, 0),
                        new FPoint(t, dy),
                        new FPoint(dx, dy)
                    };
                    break;
                case RIGHT:
                    t = Math.max(0, dx) + BRACKET_SIZE;
                    points = new FPoint[] {
                        new FPoint(0, 0),
                        new FPoint(t, 0),
                        new FPoint(t, dy),
                        new FPoint(dx, dy)
                    };
                    break;
            }
        } else if (sourceDirection.isVertical()) {
            if (targetDirection.isVertical()) {
                // source direction - vertical
                // target direction - vertical
                if (dy != 0) {
                    points = new FPoint[] {
                        new FPoint( 0,  0), 
                        new FPoint( 0, cy),
                        new FPoint(dx, cy),
                        new FPoint(dx, dy)
                    };
                }
            } else { 
                // source direction - vertical
                // target direction - horizontal 
                points = new FPoint[] {
                    new FPoint( 0,  0), 
                    new FPoint( 0, dy),
                    new FPoint(dx, dy)
                };
            }
        } else { 
            if (targetDirection.isVertical()) {
                // source direction - horizontal
                // target direction - vertical
                points = new FPoint[] {
                    new FPoint( 0,  0), 
                    new FPoint(dx,  0),
                    new FPoint(dx, dy)
                };
            } else {
                // source direction - horizontal
                // target direction - horizontal
                if (dx != 0) {
                    points = new FPoint[] {
                        new FPoint( 0,  0), 
                        new FPoint(cx,  0),
                        new FPoint(cx, dy),
                        new FPoint(dx, dy)
                    };
                }
            }
        }
        
        
        if (points != null) { 
            path = new FPath(points).round(2).moveTo(x1, y1);
        } else {
            path = null;
        }
        
        needsRedraw = false;        
    }
    
    
    public void connect(VisualElement source, Direction sourceDirection,
            VisualElement target, Direction targetDirection) 
    {
        setSource(source, sourceDirection);
        setTarget(target, targetDirection);
    }

    
    public void setSource(VisualElement newSource,  
            Direction newSourceDirection) 
    {
        assert newSource != null;
        
        VisualElement oldSource = this.source;
        Direction oldSourceDirection = this.sourceDirection;
        
        if (newSource != oldSource) {
            if (oldSource != null) {
                oldSource.removeOutputConnection(this);
            }
            newSource.addOutputConnection(this);
            this.source = newSource;
            needsRedraw = true;
        }
        
        if (newSourceDirection != oldSourceDirection) {
            this.sourceDirection = newSourceDirection;
            needsRedraw = true;
        }
    }
    
    
    public void setTarget(VisualElement newTarget,  
            Direction newTargetDirection) 
    {
        assert newTarget != null;
        
        VisualElement oldTarget = this.target;
        Direction oldTargetDirection = this.targetDirection;
        
        if (newTarget != oldTarget) {
            if (oldTarget != null) {
                oldTarget.removeInputConnection(this);
            }
            newTarget.addInputConnection(this);
            this.target = newTarget;
            needsRedraw = true;
        }
        
        if (oldTargetDirection != newTargetDirection) {
            this.targetDirection = newTargetDirection;
            needsRedraw = true;
        }
    }    
    
    
    public Direction getSourceDirection() { 
        return sourceDirection; 
    }
    
    
    public Direction getTargetDirection() {
        return targetDirection; 
    }
    
    
    public VisualElement getTarget() {
        return target; 
    }
    
    
    public VisualElement getSource() { 
        return source; 
    }
    
    
    public Pattern getPattern() {
        return pattern;
    }


    public void remove() {
        if (source != null) {
            source.removeOutputConnection(this);
            source = null;
        }
        
        if (target != null) {
            target.removeInputConnection(this);
            target = null;
        }
        
        pattern.removeConnection(this);
    }
    
    
    public String toString() {
        return "Connection: " + getClass().getName() + 
                ", belongs to " + pattern + 
                ", from "+ getSource() + 
                ", to " + getTarget();
    }
    


    public FPath getPath() {
        return path;
    }

    
    public FPath[] getSegmentsForPattern(CompositePattern pattern) {
        
        Pattern sp = source.getPattern();
        Pattern tp = target.getPattern();
        
        boolean substructFromTarget = false;
        boolean substructAll = false;
        
        List<Pattern> sParents = new ArrayList<Pattern>();
        for (Pattern p = sp; p != null; p = p.getParent()) {
            sParents.add(p);
        }
        
        Pattern cp = null; // common pattern
        for (Pattern p = tp; p != null; p = p.getParent()) {
            if (p == pattern) substructFromTarget = true;
            
            if (sParents.contains(p)) {
                cp = p;
                break;
            }
        }
        
        if (cp == pattern) {
            substructAll = true;
        }


        FPath result = path.intersect(pattern.getBorder().getShape());
        
        FShape targetBorder = null;
        for (Pattern p = tp; (p != cp) && (p != pattern); p = p.getParent()) {
            if (!(p instanceof CompositePattern)) continue;
            if (((CompositePattern) p).getBorder() == null) continue;
            targetBorder = ((CompositePattern) p).getBorder().getShape();
        }
        
        FShape sourceBorder = null;
        for (Pattern p = sp; (p != cp) && (p != pattern); p = p.getParent()) {
            if (!(p instanceof CompositePattern)) continue;
            if (((CompositePattern) p).getBorder() == null) continue;
            sourceBorder = ((CompositePattern) p).getBorder().getShape();
        }
        
        
        if (substructAll) {
            if (sourceBorder != null) {
                result = result.subtract(sourceBorder);
            }
            if (targetBorder != null) {
                result = result.subtract(targetBorder);
            }
        } else if (substructFromTarget) {
            if (targetBorder != null) {
                result = result.subtract(targetBorder);
            }
        } else {
            if (sourceBorder != null) {
                result = result.subtract(sourceBorder);
            }
        }
        
        return result.split();
    }
    
    

    
    public int getUID() {
        return uid;
    }


    private static int uidCounter = 0;
    
    
    
    public void paint(Graphics2D g2) {
        assert (path != null): "Invalid connection(path is null) found on diagram: " + this;
        paintConnection(g2, path, isPaintDashed(), isPaintArrow(), 
                isPaintSlash(), isPaintCircle(), null);
    }
    
    // Rendering constants
    private static final Color COLOR = new Color(0xE68B2C);
    private static final Color CIRCLE_FILL = new Color(0xFFFFFF);

    
    
    public static void paintConnection(Graphics2D g2, FPath path,
            boolean paintDashed, 
            boolean paintArrow, 
            boolean paintSlash, 
            boolean paintCircle,
            Color color) 
    {
        paintConnection(g2, path, paintDashed, paintArrow, paintSlash, 
            paintCircle, 1, color);
    }
    
    public static void paintConnection(Graphics2D g2, FPath path,
            boolean paintDashed, 
            boolean paintArrow, 
            boolean paintSlash, 
            boolean paintCircle,
            float width,
            Color color) 
    {
        if (path == null) return;
        if (path.length() <= 0.0f) return;
        
        if (color == null) {
            color = COLOR;
        }
        
        GUtils.setPaint(g2, color);
        if (paintDashed) {
            GUtils.setDashedStroke(g2, width, 3);
        } else {
            GUtils.setSolidStroke(g2, width);
        }
        
        GUtils.draw(g2, GUtils.convert(path), true);
        
        if (paintDashed) {
            GUtils.setSolidStroke(g2, width);
        }

        if (paintArrow) {
            FPoint t = path.tangent(1);
            FPoint n = path.normal(1);
            FPoint end = path.point(1);

            float x1 = end.x - t.x * 4;
            float y1 = end.y - t.y * 4;

            Shape arrowShape = GUtils.getTriangle(end.x, end.y, 
                    x1 + n.x * 2, y1 + n.y * 2, 
                    x1 - n.x * 2, y1 - n.y * 2);
            
            GUtils.fill(g2, arrowShape);
            GUtils.draw(g2, arrowShape, true);
        }
        
        if (paintSlash) {
            FPoint t = path.tangent(0);
            FPoint n = path.normal(0);
            FPoint start = path.point(0);
            
            float x1 = start.x + t.x * 5 + n.x * 4;
            float y1 = start.y + t.y * 5 + n.y * 4;

            float x2 = start.x + t.x * 11 - n.x * 4;
            float y2 = start.y + t.y * 11 - n.y * 4;

            GUtils.draw(g2, new Line2D.Float(x1, y1, x2, y2), true);
        }
        
        
        if (paintCircle) {
            FPoint t = path.tangent(0);
            FPoint start = path.point(0);

            float cx = start.x + t.x * 2;
            float cy = start.y + t.y * 2;
     
            Shape s = new Ellipse2D.Float(cx - 2, cy - 2, 4, 4);
            
            GUtils.setPaint(g2, CIRCLE_FILL);
            GUtils.fill(g2, s);
            GUtils.setPaint(g2, color);
            GUtils.draw(g2, s, true);
        }
    }
    
    
    public static final float BRACKET_SIZE = 16;
}
