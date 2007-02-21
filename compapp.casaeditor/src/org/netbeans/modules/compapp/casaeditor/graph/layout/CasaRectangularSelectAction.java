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
package org.netbeans.modules.compapp.casaeditor.graph.layout;

import org.netbeans.api.visual.action.RectangularSelectDecorator;
import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction.State;

/**
 * Modified to prevent the rectangular selection from being created in
 * negative coordinates (i.e. to the left or above the scene).
 *
 * @author David Kaspar
 */
public final class CasaRectangularSelectAction extends WidgetAction.LockedAdapter {

    private RectangularSelectDecorator decorator;
    private LayerWidget interractionLayer;
    private RectangularSelectProvider provider;

    private Widget selectionWidget;
    private Rectangle selectionSceneRectangle;

    
    public CasaRectangularSelectAction (RectangularSelectDecorator decorator, LayerWidget interractionLayer, RectangularSelectProvider provider) {
        this.decorator = decorator;
        this.interractionLayer = interractionLayer;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return selectionSceneRectangle != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            selectionWidget = decorator.createSelectionWidget ();
            assert selectionWidget != null;
            interractionLayer.addChild (selectionWidget);
            selectionSceneRectangle = new Rectangle (widget.convertLocalToScene (event.getPoint ()));
            move (widget, event.getPoint ());
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (selectionSceneRectangle != null) {
            move (widget, event.getPoint ());
            selectionWidget.getParentWidget ().removeChild (selectionWidget);
            provider.performSelection (selectionSceneRectangle);
            selectionSceneRectangle = null;
        }
        return State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        if (selectionSceneRectangle != null) {
            move (widget, event.getPoint ());
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    private void resolveSelectionWidgetLocationBounds () {
        selectionWidget.setPreferredLocation (selectionSceneRectangle.getLocation ());
        int w = selectionSceneRectangle.width;
        int h = selectionSceneRectangle.height;
        Rectangle rect = new Rectangle (w >= 0 ? 0 : w, h >= 0 ? 0 : h, w >= 0 ? w : -w, h >= 0 ? h : -h);
        selectionWidget.setPreferredBounds (rect);
    }

    private void move (Widget widget, Point newLocation) {
        Point sceneLocation = widget.convertLocalToScene (newLocation);
        // Do not allow the rectangle to extend into negative scene space.
        if (sceneLocation.x < 0) {
            sceneLocation.x = 0;
        }
        if (sceneLocation.y < 0) {
            sceneLocation.y = 0;
        }
        selectionSceneRectangle.width = sceneLocation.x - selectionSceneRectangle.x;
        selectionSceneRectangle.height = sceneLocation.y - selectionSceneRectangle.y;
        resolveSelectionWidgetLocationBounds ();
    }
}
