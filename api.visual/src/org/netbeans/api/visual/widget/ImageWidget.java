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
package org.netbeans.api.visual.widget;

import java.awt.*;

/**
 * A widget representing image. The origin of the widget is at its top-left corner.
 * @author David Kaspar
 */
// TODO - alignment
public class ImageWidget extends Widget {

    private Image image;
    private int width, height;

    /**
     * Creates an image widget.
     * @param scene the scene
     */
    public ImageWidget (Scene scene) {
        super (scene);
    }

    /**
     * Creates an image widget.
     * @param scene the scene
     * @param image the image
     */
    public ImageWidget (Scene scene, Image image) {
        super (scene);
        setImage (image);
    }

    /**
     * Returns an image.
     * @return the image
     */
    public Image getImage () {
        return image;
    }

    /**
     * Sets an image
     * @param image the image
     */
    public void setImage (Image image) {
        if (this.image == image)
            return;
        int oldWidth = width;
        int oldHeight = height;

        this.image = image;
        width = image != null ? image.getWidth (null) : 0;
        height = image != null ? image.getHeight (null) : 0;

        if (oldWidth == width  &&  oldHeight == height)
            repaint ();
        else
            revalidate ();
    }

    /**
     * Calculates a client area of the image
     * @return the calculated client area
     */
    protected Rectangle calculateClientArea () {
        if (image != null)
            return new Rectangle (0, 0, width, height);
        return super.calculateClientArea ();
    }

    /**
     * Paints the image widget.
     */
    protected void paintWidget () {
        if (image == null)
            return;
        Graphics2D gr = getGraphics ();
        if (image != null)
            gr.drawImage (image, 0, 0, null);
    }

}
