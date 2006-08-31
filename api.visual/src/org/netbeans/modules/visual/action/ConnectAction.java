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

import org.netbeans.api.visual.action.ConnectDecorator;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Override at least isTargetWidget and createConnection methods. isSourceWidget is always called before isTargetWidget.
 *
 * @author David Kaspar
 */
public class ConnectAction extends WidgetAction.LockedAdapter {

    private static final int MIN_DIFFERENCE = 5;

    private ConnectDecorator decorator;
    private Widget interractionLayer;
    private ConnectProvider provider;

    private ConnectionWidget connectionWidget = null;
    private Widget sourceWidget = null;
    private Widget targetWidget = null;
    private Point startingPoint = null;

    public ConnectAction (ConnectDecorator decorator, Widget interractionLayer, ConnectProvider provider) {
        this.decorator = decorator;
        this.interractionLayer = interractionLayer;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return sourceWidget != null;
    }

    public WidgetAction.State mousePressed (Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1 && event.getClickCount () == 1) {
            if (provider.isSourceWidget (widget)) {
                sourceWidget = widget;
                targetWidget = null;
                startingPoint = new Point (event.getPoint ());
                connectionWidget = decorator.createConnectionWidget (interractionLayer.getScene ());
                assert connectionWidget != null;
                connectionWidget.setSourceAnchor (decorator.createSourceAnchor (widget));
                interractionLayer.addChild (connectionWidget);
                return WidgetAction.State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    public WidgetAction.State mouseReleased (Widget widget, WidgetAction.WidgetMouseEvent event) {
        Point point = event.getPoint ();
        boolean state = move (widget, point);
        if (state) {
            if (Math.abs (startingPoint.x - point.x) >= MIN_DIFFERENCE  ||  Math.abs (startingPoint.y - point.y) >= MIN_DIFFERENCE)
                provider.createConnection (sourceWidget, targetWidget);
            sourceWidget = null;
            targetWidget = null;
            startingPoint = null;
            interractionLayer.removeChild (connectionWidget);
            connectionWidget = null;
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    public WidgetAction.State mouseDragged (Widget widget, WidgetAction.WidgetMouseEvent event) {
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean move (Widget widget, Point point) {
        if (sourceWidget != widget)
            return false;

        Point targetSceneLocation = widget.convertLocalToScene (point);
        targetWidget = resolveTargetWidgetCore (interractionLayer.getScene (), targetSceneLocation);
        Anchor targetAnchor = null;
        if (targetWidget != null)
            targetAnchor = decorator.createTargetAnchor (targetWidget);
        if (targetAnchor == null)
            targetAnchor = decorator.createFloatAnchor (targetSceneLocation);
        connectionWidget.setTargetAnchor (targetAnchor);

        return true;
    }

    private Widget resolveTargetWidgetCore (Scene scene, Point sceneLocation) {
        if (provider != null)
            if (provider.hasCustomTargetWidgetResolver (scene))
                return provider.resolveTargetWidget (scene, sceneLocation);
        Point sceneOrigin = scene.getLocation ();
        sceneLocation = new Point (sceneLocation.x + sceneOrigin.x, sceneLocation.y + sceneOrigin.y);
        Widget[] result = new Widget[]{null};
        resolveTargetWidgetCoreDive (result, scene, sceneLocation);
        return result[0];
    }

    private boolean resolveTargetWidgetCoreDive (Widget[] result, Widget widget, Point parentLocation) {
        if (interractionLayer.equals (widget))
            return false;
        Point widgetLocation = widget.getLocation ();
        Point location = new Point (parentLocation.x - widgetLocation.x, parentLocation.y - widgetLocation.y);

        if (! widget.getBounds ().contains (location))
            return false;

        java.util.List<Widget> children = widget.getChildren ();
        for (int i = children.size () - 1; i >= 0; i --) {
            if (resolveTargetWidgetCoreDive (result, children.get (i), location))
                return true;
        }

        if (! widget.isHitAt (location))
            return false;

        ConnectorState state = provider.isTargetWidget (sourceWidget, widget);
        if (state == ConnectorState.REJECT)
            return false;
        if (state == ConnectorState.ACCEPT)
            result[0] = widget;
        return true;
    }

}
