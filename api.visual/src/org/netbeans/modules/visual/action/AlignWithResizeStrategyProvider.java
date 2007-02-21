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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.AlignWithWidgetCollector;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class AlignWithResizeStrategyProvider extends AlignWithSupport implements ResizeStrategy, ResizeProvider {

    public AlignWithResizeStrategyProvider (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator) {
        super (collector, interractionLayer, decorator);
    }

    public Rectangle boundsSuggested (Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
        Insets insets = widget.getBorder ().getInsets ();
        int minx = insets.left + insets.right;
        int miny = insets.top + insets.bottom;

        suggestedBounds = widget.convertLocalToScene (suggestedBounds);

        Point suggestedLocation, point;
        int tempx, tempy;

        switch (controlPoint) {
            case BOTTOM_CENTER:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width / 2, suggestedBounds.y + suggestedBounds.height);

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, false, true, false, false);

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);
                break;
            case BOTTOM_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y + suggestedBounds.height);

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);

                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case BOTTOM_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y + suggestedBounds.height);

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);

                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
            case CENTER_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y + suggestedBounds.height / 2);

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, false, false, false);

                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case CENTER_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y + suggestedBounds.height / 2);

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, false, false, false);

                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
            case TOP_CENTER:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width / 2, suggestedBounds.y);

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, false, true, false, false);

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;
                break;
            case TOP_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y);

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;

                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case TOP_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y);

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;

                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
        }
        return widget.convertSceneToLocal (suggestedBounds);
    }

    public void resizingStarted (Widget widget) {
        show ();
    }

    public void resizingFinished (Widget widget) {
        hide ();
    }

}
