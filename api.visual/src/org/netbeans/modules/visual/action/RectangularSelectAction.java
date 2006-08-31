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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.RectangularSelectDecorator;
import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
public final class RectangularSelectAction extends WidgetAction.LockedAdapter {

    private RectangularSelectDecorator decorator;
    private LayerWidget interractionLayer;
    private RectangularSelectProvider provider;

    private Widget selectionWidget;
    private Rectangle selectionSceneRectangle;

    public RectangularSelectAction (RectangularSelectDecorator decorator, LayerWidget interractionLayer, RectangularSelectProvider provider) {
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
        selectionWidget.setPreferredBounds (new Rectangle (w >= 0 ? 0 : w, h >= 0 ? 0 : h, w >= 0 ? w : -w, h >= 0 ? h : -h));
    }

    private void move (Widget widget, Point newLocation) {
        Point sceneLocation = widget.convertLocalToScene (newLocation);
        selectionSceneRectangle.width = sceneLocation.x - selectionSceneRectangle.x;
        selectionSceneRectangle.height = sceneLocation.y - selectionSceneRectangle.y;
        resolveSelectionWidgetLocationBounds ();
    }

}
