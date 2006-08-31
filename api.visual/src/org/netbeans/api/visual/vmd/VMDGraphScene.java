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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class VMDGraphScene extends GraphPinScene<String, String, String> {

    public static final String PIN_ID_DEFAULT_SUFFIX = "#default"; // NOI18N

    private LayerWidget backgroundLayer = new LayerWidget (this);
    private LayerWidget mainLayer = new LayerWidget (this);
    private LayerWidget connectionLayer = new LayerWidget (this);
    private LayerWidget upperLayer = new LayerWidget (this);

    private Router router;

    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction ();
    private WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction (new MyPopupMenuProvider ());
    private WidgetAction moveAction = ActionFactory.createMoveAction ();

    public VMDGraphScene () {
        addChild (backgroundLayer);
        addChild (mainLayer);
        addChild (connectionLayer);
        addChild (upperLayer);

        router = RouterFactory.createOrthogonalSearchRouter (mainLayer, connectionLayer);

        getActions ().addAction (ActionFactory.createZoomAction ());
        getActions ().addAction (ActionFactory.createPanAction ());
        getActions ().addAction (ActionFactory.createRectangularSelectAction (this, backgroundLayer));
    }

    protected Widget attachNodeWidget (String node) {
        VMDNodeWidget widget = new VMDNodeWidget (this);
        mainLayer.addChild (widget);

        widget.getActions ().addAction (createObjectHoverAction ());
        widget.getActions ().addAction (createSelectAction ());
        widget.getActions ().addAction (popupMenuAction);
        widget.getActions ().addAction (moveAction);

        return widget;
    }

    protected Widget attachPinWidget (String node, String pin) {
        if (pin.endsWith (PIN_ID_DEFAULT_SUFFIX))
            return null;

        VMDPinWidget widget = new VMDPinWidget (this);
        ((VMDNodeWidget) findWidget (node)).attachPinWidget (widget);
        widget.getActions ().addAction (createObjectHoverAction ());
        widget.getActions ().addAction (createSelectAction ());

        return widget;
    }

    protected Widget attachEdgeWidget (String edge) {
        ConnectionWidget connectionWidget = new ConnectionWidget (this);
        connectionWidget.setRouter (router);
        connectionWidget.setSourceAnchorShape (AnchorShape.TRIANGLE_OUT);
        connectionWidget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        connectionWidget.setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
        connectionWidget.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
        connectionLayer.addChild (connectionWidget);

        connectionWidget.getActions ().addAction (createObjectHoverAction ());
        connectionWidget.getActions ().addAction (createSelectAction ());
        connectionWidget.getActions ().addAction (moveControlPointAction);

        return connectionWidget;
    }

    protected void attachEdgeSourceAnchor (String edge, String oldSourcePin, String sourcePin) {
        ((ConnectionWidget) findWidget (edge)).setSourceAnchor (getPinAnchor (sourcePin));
    }

    protected void attachEdgeTargetAnchor (String edge, String oldTargetPin, String targetPin) {
        ((ConnectionWidget) findWidget (edge)).setTargetAnchor (getPinAnchor (targetPin));
    }

    private Anchor getPinAnchor (String pin) {
        VMDNodeWidget nodeWidget = (VMDNodeWidget) findWidget (getPinNode (pin));
        Widget pinMainWidget = findWidget (pin);
        Anchor anchor;
        if (pinMainWidget != null) {
            anchor = AnchorFactory.createDirectionalAnchor (pinMainWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL);
            anchor = nodeWidget.createAnchorPin (anchor);
        } else
            anchor = nodeWidget.getNodeAnchor ();
        return anchor;
    }

    private static class MyPopupMenuProvider implements PopupMenuProvider {

        public JPopupMenu getPopupMenu (Widget widget, Point localLocation) {
            JPopupMenu popupMenu = new JPopupMenu ();
            popupMenu.add (new JMenuItem ("Open " + ((VMDNodeWidget) widget).getNodeName ()));
            return popupMenu;
        }

    }

}
