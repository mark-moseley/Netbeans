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
package org.netbeans.api.visual.border;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.visual.border.*;

import java.awt.*;

/**
 * This class is a factory of all built-in implementation of borders.
 * Instances of built-in borders can be shared by multiple widgets.
 *
 * @author David Kaspar
 */
// TODO - check insets values
public final class BorderFactory {

    private static final Border BORDER_EMPTY = new EmptyBorder (0, 0, 0, 0, false);
    private static final Border BORDER_LINE = createLineBorder (1);

    private BorderFactory () {
    }

    /**
     * Creates an default empty border with 0px layout.
     * The instance can be shared by multiple widgets.
     * @return the empty border
     */
    public static Border createEmptyBorder () {
        return BORDER_EMPTY;
    }

    /**
     * Creates an empty border with specific thickness.
     * The instance can be shared by multiple widgets.
     * @param thickness the border thickness
     * @return the empty border
     */
    public static Border createEmptyBorder (int thickness) {
        return thickness > 0 ? createEmptyBorder (thickness, thickness, thickness, thickness) : BORDER_EMPTY;
    }

    /**
     * Creates an empty border with specific thickness.
     * The instance can be shared by multiple widgets.
    * @param horizontal the horizontal thickness
     * @param vertical the vertical thickness
     * @return the empty border
     */
    public static Border createEmptyBorder (int horizontal, int vertical) {
        return createEmptyBorder (vertical, horizontal, vertical, horizontal);
    }

    /**
     * Creates an empty border with specific thickness.
     * The instance can be shared by multiple widgets.
     * @param top the top inset
     * @param left the left inset
     * @param bottom the bottom inset
     * @param right the right inset
     * @return the empty border
     */
    public static Border createEmptyBorder (int top, int left, int bottom, int right) {
        return new EmptyBorder (top, left, bottom, right, false);
    }

    /**
     * Creates an opaque border with specific thickness.
     * The instance can be shared by multiple widgets.
     * @param top the top inset
     * @param left the left inset
     * @param bottom the bottom inset
     * @param right the right inset
     * @return the empty border
     */
    public static Border createOpaqueBorder (int top, int left, int bottom, int right) {
        return new EmptyBorder (top, left, bottom, right, true);
    }

    /**
     * Creates a composite border that consists of a list of specified borders - one embedded to another.
     * The instance can be shared by multiple widgets.
     * @param borders the list of borders
     * @return the composite border
     */
    public static Border createCompositeBorder (Border... borders) {
        return new CompositeBorder (borders);
    }

    /**
     * Creates a layout from a Swing border.
     * The instance can be shared by multiple widgets but cannot be used in multiple scenes.
     * @param scene the scene where the border is used.
     * @param border the Swing border
     * @return the border
     */
    public static Border createSwingBorder (Scene scene, javax.swing.border.Border border) {
        assert scene != null && scene.getView () != null && border != null;
        return new SwingBorder (scene, border);
    }

    /**
     * Creates a line border with default style.
     * The instance can be shared by multiple widgets.
     * @return the line border
     */
    public static Border createLineBorder () {
        return BORDER_LINE;
    }

    /**
     * Creates a line border with specific thickness. The line is still one pixel but the layout insets are calculated from thickness.
     * The instance can be shared by multiple widgets.
     * @param thickness the border thickness
     * @return the line border
     */
    public static Border createLineBorder (int thickness) {
        return createLineBorder (thickness, null);
    }

    /**
     * Creates a line border with specific thickness and color. The line is still one pixel but the layout insets are calculated from thickness.
     * The instance can be shared by multiple widgets.
     * @param thickness the border thickness
     * @param color     the line color
     * @return the line border
     */
    public static Border createLineBorder (int thickness, Color color) {
        return new LineBorder (thickness, thickness, thickness, thickness, color != null ? color : Color.BLACK);
    }

    /**
     * Creates a line border with specific insets and color. The line is still one pixel but the layout insets are specified.
     * The instance can be shared by multiple widgets.
     * @param top the top inset
     * @param left the left inset
     * @param bottom the bottom inset
     * @param right the right inset
     * @param color the line color
     * @return the line border
     */
    public static Border createLineBorder (int top, int left, int bottom, int right, Color color) {
        return new LineBorder (top, left, bottom, right, color != null ? color : Color.BLACK);
    }

    /**
     * Creates a bevel border.
     * The instance can be shared by multiple widgets.
     * @param raised if true, then it is a raised-bevel border; if false, then it is a lowered-bevel layout
     * @return the bevel border
     */
    public static Border createBevelBorder (boolean raised) {
        return createBevelBorder (raised, null);
    }

    /**
     * Creates a bevel border.
     * The instance can be shared by multiple widgets.
     * @param raised if true, then it is a raised-bevel layout; if false, then it is a lowered-bevel border
     * @param color the border color
     * @return the bevel border
     */
    public static Border createBevelBorder (boolean raised, Color color) {
        return new BevelBorder (raised, color != null ? color : Color.GRAY);
    }

    /**
     * Creates an image layout. The border is painted using a supplied Image. The image is split into 3x3 regions defined by insets.
     * The middle regions are tiled for supplying variable width and height of border. Central region is not painted.
     * The instance can be shared by multiple widgets.
     * @param insets the border insets
     * @param image the border image
     * @return the image border
     */
    public static Border createImageBorder (Insets insets, Image image) {
        return createImageBorder (insets, insets, image);
    }

    /**
     * Creates an image layout. The border is painted using a supplied Image. The image is split into 3x3 regions defined by imageInsets.
     * The middle regions are tiled for supplying variable width and height of border. Central region is not painted.
     * The insets of the border is specified by borderInsets.
     * The instance can be shared by multiple widgets.
     * @param borderInsets the border insets
     * @param imageInsets the image insets
     * @param image the border image
     * @return the image border
     */
    public static Border createImageBorder (Insets borderInsets, Insets imageInsets, Image image) {
        assert borderInsets != null  &&  imageInsets != null  &&  image != null;
        return new ImageBorder (borderInsets, imageInsets, image);
    }

    /**
     * Creates an rounded-rectangle border with a specified style. Insets are calculated from arcWidth and arcHeight.
     * The instance can be shared by multiple widgets.
     * @param arcWidth the arc width
     * @param arcHeight the arc height
     * @param fillColor the fill color
     * @param drawColor the draw color
     * @return the rounded border
     */
    public static Border createRoundedBorder (int arcWidth, int arcHeight, Color fillColor, Color drawColor) {
        return createRoundedBorder (arcWidth, arcHeight, arcWidth, arcHeight, fillColor, drawColor);
    }

    /**
     * Creates an rounded-rectangle border with a specified style.
     * The instance can be shared by multiple widgets.
     * @param arcWidth the arc width
     * @param arcHeight the arc height
     * @param insetWidth the inset width
     * @param insetHeight the inset height
     * @param fillColor the fill color
     * @param drawColor the draw color
     * @return the rounded border
     */
    public static Border createRoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, Color fillColor, Color drawColor) {
        return new RoundedBorder (arcWidth, arcHeight, insetWidth, insetHeight, fillColor, drawColor);
    }

    /**
     * Creates a resize border. Usually used as resizing handles for ResizeAction. It renders a bounding rectangle with 8-direction squares.
     * The instance can be shared by multiple widgets.
     * @param thickness the thickness of the border
     * @return the resize border
     */
    public static Border createResizeBorder (int thickness) {
        return createResizeBorder (thickness, null, false);
    }

    /**
     * Creates a resize border. Usually used as resizing handles for ResizeAction. It renders a bounding rectangle with 8-direction squares.
     * The instance can be shared by multiple widgets.
     * @param thickness the thickness of the border
     * @param color the border color
     * @param outer if true, then the rectangle encapsulate the squares too; if false, then the rectangle encapsulates the widget client area only
     * @return the resize border
     */
    public static Border createResizeBorder (int thickness, Color color, boolean outer) {
        return new ResizeBorder (thickness, color != null ? color : Color.BLACK, outer);
    }

    /**
     * Creates a resize border rendered with dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param color  the border color
     * @param width  the inset width
     * @param height the inset height
     * @param squares the
     * @return the dashed border
     */
    public static Border createDashedBorder (Color color, int width, int height, boolean squares) {
        if (squares)
            return new DashedBorder (color != null ? color : Color.BLACK, width, height);
        else
            return new FancyDashedBorder (color != null ? color : Color.BLACK, width, height);
    }

    /**
     * Creates a resize border rendered with dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param color the border color
     * @param width the inset width
     * @param height the inset height
     * @return the dashed border
     * @deprecated use createDashedBorder (color, width, height, squares) method instead
     */
    public static Border createDashedBorder (Color color, int width, int height) {
        return new DashedBorder (color != null ? color : Color.BLACK, width, height);
    }

    /**
     * Creates a resize border rendered with fancy dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param color the border color
     * @param width the inset width
     * @param height the inset height
     * @return the fancy dashed border
     * @deprecated use createDashedBorder (color, width, height, squares) method instead
     */
    public static Border createFancyDashedBorder (Color color, int width, int height) {
        return new FancyDashedBorder (color != null ? color : Color.BLACK, width, height);
    }

}
