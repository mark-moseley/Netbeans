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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.modules.websvc.design.view.layout.LeftRightLayout;

/**
 * @author Ajit Bhate
 */
public abstract class AbstractTitledWidget extends AbstractMouseActionsWidget implements ExpandableWidget {
    
    public static final Color BORDER_COLOR = new Color(169, 197, 235);
    public static final Color TITLE_COLOR = new Color(184, 215, 255);
    public static final Color TITLE_COLOR_BRIGHT = new Color(241, 245, 252);
    public static final Color TITLE_COLOR_PARAMETER = new Color(235,255,255);
    public static final Color TITLE_COLOR_OUTPUT = new Color(240,240,240);
    public static final Color TITLE_COLOR_FAULT = new Color(245,227,225);
    public static final Color TITLE_COLOR_DESC = new Color(240,240,240);
    public static final Color BORDER_COLOR_BLACK = Color.BLACK;
    public static final int RADIUS = 12;
    
    private Color borderColor = BORDER_COLOR;
    private int radius = RADIUS;
    private int hgap = RADIUS;
    private int cgap = RADIUS;
    private int depth = radius/3;
    
    private boolean expanded;
    private transient HeaderWidget headerWidget;
    private transient Widget seperatorWidget;
    private transient Widget contentWidget;
    private transient ExpanderWidget expander;
    
    /**
     * Creates a new instance of RoundedRectangleWidget
     * with default rounded radius and gap and no gradient color
     * @param scene scene this widget belongs to
     * @param radius of the rounded arc
     * @param color color of the border and gradient title
     */
    public AbstractTitledWidget(Scene scene, int radius, Color color) {
        this(scene,radius,radius,radius,color);
    }
    /**
     * Creates a new instance of RoundedRectangleWidget
     * with default rounded radius and gap and no gradient color
     * @param scene scene this widget belongs to
     * @param radius of the rounded arc
     * @param gap for header widget
     * @param gap for content widget
     * @param color color of the border and gradient title
     */
    public AbstractTitledWidget(Scene scene, int radius, int hgap, int cgap, Color color) {
        super(scene);
        this.radius = radius;
        this.borderColor = color;
        this.hgap = hgap;
        this.cgap = cgap;
        depth = radius/3;
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        setBorder(new RoundedBorder3D(radius, depth, 0, 0, borderColor));
        headerWidget = new HeaderWidget(getScene(), this);
        headerWidget.setBorder(BorderFactory.createEmptyBorder(hgap, hgap/2));
        headerWidget.setLayout(new LeftRightLayout(32));
        addChild(headerWidget);
        seperatorWidget = new SeparatorWidget(getScene(),SeparatorWidget.Orientation.HORIZONTAL);
        seperatorWidget.setForeground(borderColor);
        if(isExpandable()) {
            contentWidget = createContentWidget();
            expanded = ExpanderWidget.isExpanded(this, true);
            if(expanded) {
                expandWidget();
            } else {
                collapseWidget();
            }
            expander = new ExpanderWidget(getScene(), this, expanded);
        }
    }
    
    protected Widget getContentWidget() {
        return contentWidget;
    }
    
    protected final Widget createContentWidget() {
        Widget widget = new Widget(getScene());
        widget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, cgap));
        return widget;
    }

    protected final Widget createHeaderWidget() {
        Widget widget = new Widget(getScene());
        widget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, radius));
        return widget;
    }

    protected HeaderWidget getHeaderWidget() {
        return headerWidget;
    }
    
    protected ExpanderWidget getExpanderWidget() {
        return expander;
    }
    
    protected final void paintWidget() {
        Rectangle bounds = getClientArea();
        Graphics2D g = getGraphics();
        Paint oldPaint = g.getPaint();
        RoundRectangle2D rect = new RoundRectangle2D.Double(bounds.x + 0.75f,
                bounds.y+ 0.75f, bounds.width - 1.5f, bounds.height - 1.5f, radius, radius);
        if(isExpanded()) {
            int titleHeight = headerWidget.getBounds().height;
            Area titleArea = new Area(rect);
            titleArea.subtract(new Area(new Rectangle(bounds.x,
                    bounds.y + titleHeight, bounds.width, bounds.height)));
            g.setPaint(getTitlePaint(titleArea.getBounds()));
            g.fill(titleArea);
            if(isOpaque()) {
                Area bodyArea = new Area(rect);
                bodyArea.subtract(titleArea);
                g.setPaint(getBackground());
                g.fill(bodyArea);
            }
        } else {
            g.setPaint(getTitlePaint(bounds));
            g.fill(rect);
        }
        g.setPaint(oldPaint);
    }

    protected Paint getTitlePaint(Rectangle bounds) {
        return new GradientPaint(bounds.x, bounds.y, TITLE_COLOR_BRIGHT,
                    bounds.x, bounds.y + bounds.height, TITLE_COLOR);
    }
    
    protected void collapseWidget() {
        if(seperatorWidget.getParentWidget()!=null) {
            removeChild(seperatorWidget);
        }
        if(getContentWidget().getParentWidget()!=null) {
            removeChild(getContentWidget());
        }
    }
    
    protected void expandWidget() {
        if(seperatorWidget.getParentWidget()==null) {
            addChild(seperatorWidget);
        }
        if(getContentWidget().getParentWidget()==null) {
            addChild(getContentWidget());
        }
    }
    
    public Object hashKey() {
        return null;
    }
    
    public void setExpanded(boolean expanded) {
        if(!isExpandable()) return;
        if(this.expanded != expanded) {
            this.expanded = expanded;
            if(expanded) {
                expandWidget();
            } else {
                collapseWidget();
            }
            expander.setExpanded(expanded);
        }
    }
    
    public boolean isExpanded() {
        return expanded;
    }
    
    protected boolean isExpandable() {
        return true;
    }
}
