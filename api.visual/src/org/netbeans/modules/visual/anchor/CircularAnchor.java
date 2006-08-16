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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class CircularAnchor extends Anchor {

    private int radius;

    public CircularAnchor (Widget widget, int radius) {
        super (widget);
//        assert widget != null;
        this.radius = radius;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        double angle = Math.atan2 (oppositeLocation.y - relatedLocation.y, oppositeLocation.x - relatedLocation.x);

        Point location = new Point (relatedLocation.x + (int) (radius * Math.cos (angle)), relatedLocation.y + (int) (radius * Math.sin (angle)));
        return new Anchor.Result (location, Anchor.DIRECTION_ANY); // TODO - resolve direction
    }

}
