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
package org.netbeans.modules.visual.vmd;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.vmd.*;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class VMDNetBeans60ColorScheme extends VMDColorScheme {

    public static final Color COLOR60_SELECT = new Color (0xFF8500);
    public static final Color COLOR60_HOVER = new Color (0x5B67B0);
    public static final Color COLOR60_HOVER_BACKGROUND = new Color (0xB0C3E1);

    private static final Border BORDER_MINIMIZE = BorderFactory.createRoundedBorder (2, 2, null, VMDOriginalColorScheme.COLOR_NORMAL);
    private static final Border BORDER_MINIMIZE_HOVER = BorderFactory.createRoundedBorder (2, 2, null, COLOR60_HOVER);

    private static final Border BORDER60 = VMDFactory.createVMDNodeBorder (VMDOriginalColorScheme.COLOR_NORMAL, 2, VMDOriginalColorScheme.COLOR1, VMDOriginalColorScheme.COLOR2, VMDOriginalColorScheme.COLOR3, VMDOriginalColorScheme.COLOR4, VMDOriginalColorScheme.COLOR5);
    private static final Border BORDER60_SELECT = VMDFactory.createVMDNodeBorder (COLOR60_SELECT, 2, VMDOriginalColorScheme.COLOR1, VMDOriginalColorScheme.COLOR2, VMDOriginalColorScheme.COLOR3, VMDOriginalColorScheme.COLOR4, VMDOriginalColorScheme.COLOR5);
    private static final Border BORDER60_HOVER = VMDFactory.createVMDNodeBorder (COLOR60_HOVER, 2, VMDOriginalColorScheme.COLOR1, VMDOriginalColorScheme.COLOR2, VMDOriginalColorScheme.COLOR3, VMDOriginalColorScheme.COLOR4, VMDOriginalColorScheme.COLOR5);

    private static final Border BORDER60_PIN_SELECT = BorderFactory.createCompositeBorder (BorderFactory.createLineBorder (0, 1, 0, 1, COLOR60_SELECT), BorderFactory.createLineBorder (2, 7, 2, 7, COLOR60_SELECT));
//        private static final Border BORDER60_PIN_HOVER = BorderFactory.createLineBorder (2, 8, 2, 8, COLOR60_HOVER);

    public void installUI (VMDNodeWidget widget) {
        widget.setBorder (BORDER60);

        Widget header = widget.getHeader ();
        header.setBackground (COLOR60_HOVER_BACKGROUND);
        header.setBorder (VMDOriginalColorScheme.BORDER_PIN);

        Widget pinsSeparator = widget.getPinsSeparator ();
        pinsSeparator.setForeground (VMDOriginalColorScheme.BORDER_CATEGORY_BACKGROUND);
    }

    public void updateUI (VMDNodeWidget widget, ObjectState previousState, ObjectState state) {
        if (! previousState.isSelected ()  &&  state.isSelected ())
            widget.bringToFront ();

        boolean hover = state.isHovered () || state.isFocused ();
        widget.getHeader ().setOpaque (hover);

        if (state.isSelected ())
            widget.setBorder (BORDER60_SELECT);
        else if (state.isHovered ())
            widget.setBorder (BORDER60_HOVER);
        else if (state.isFocused ())
            widget.setBorder (BORDER60_HOVER);
        else
            widget.setBorder (BORDER60);

        Widget minimize = widget.getMinimizeButton ();
        minimize.setBorder (hover ? BORDER_MINIMIZE_HOVER : BORDER_MINIMIZE);
    }

    public void installUI (VMDConnectionWidget widget) {
        widget.setSourceAnchorShape (AnchorShape.NONE);
        widget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        widget.setPaintControlPoints (true);
    }

    public void updateUI (VMDConnectionWidget widget, ObjectState previousState, ObjectState state) {
        if (state.isSelected ())
            widget.setForeground (COLOR60_SELECT);
        else if (state.isHighlighted ())
            widget.setForeground (VMDOriginalColorScheme.COLOR_HIGHLIGHTED);
        else if (state.isHovered ()  ||  state.isFocused ())
            widget.setForeground (COLOR60_HOVER);
        else
            widget.setForeground (VMDOriginalColorScheme.COLOR_NORMAL);

        if (state.isSelected ()  ||  state.isHovered ()) {
            widget.setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
            widget.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
            widget.setControlPointCutDistance (0);
        } else {
            widget.setControlPointShape (PointShape.NONE);
            widget.setEndPointShape (VMDOriginalColorScheme.POINT_SHAPE_IMAGE);
            widget.setControlPointCutDistance (5);
        }
    }

    public void installUI (VMDPinWidget widget) {
        widget.setBorder (VMDOriginalColorScheme.BORDER_PIN);
        widget.setBackground (COLOR60_HOVER_BACKGROUND);
    }

    public void updateUI (VMDPinWidget widget, ObjectState previousState, ObjectState state) {
        widget.setOpaque (state.isHovered ()  ||  state.isFocused ());
        if (state.isSelected ())
            widget.setBorder (BORDER60_PIN_SELECT);
        else
            widget.setBorder (VMDOriginalColorScheme.BORDER_PIN);
    }

    public boolean isNodeMinimizeButtonOnRight (VMDNodeWidget widget) {
        return true;
    }

    public Widget createPinCategoryWidget (VMDNodeWidget widget, String categoryDisplayName) {
        return VMDFactory.getOriginalScheme ().createPinCategoryWidget (widget, categoryDisplayName);
    }

}
