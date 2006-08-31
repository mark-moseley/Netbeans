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

import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class SnapToGridMoveStrategy implements MoveStrategy {

    private int horizontalGridSize;
    private int verticalGridSize;

    public SnapToGridMoveStrategy (int horizontalGridSize, int verticalGridSize) {
        assert horizontalGridSize > 0 && verticalGridSize > 0;
        this.horizontalGridSize = horizontalGridSize;
        this.verticalGridSize = verticalGridSize;
    }

    public Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation) {
        return new Point (suggestedLocation.x - suggestedLocation.x % horizontalGridSize, suggestedLocation.y - suggestedLocation.y % verticalGridSize);
    }

}
