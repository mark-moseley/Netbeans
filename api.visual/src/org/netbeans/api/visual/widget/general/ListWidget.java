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
package org.netbeans.api.visual.widget.general;

import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.*;

import java.awt.*;

/**
 * This class represents a general list widget which is rendered as a rectangle with a header on top and list item widgets
 * underneath.
 * <p>
 * Note: This class is meant to be redesigned later.
 *
 * @author David Kaspar
 */
public class ListWidget extends Widget {

    private Widget header;
    private ImageWidget imageWidget;
    private LabelWidget labelWidget;

    /**
     * Creates a list widget.
     * @param scene the scene
     */
    public ListWidget (Scene scene) {
        super (scene);

        LookFeel lookFeel = scene.getLookFeel ();
        setOpaque (true);
        setBackground (lookFeel.getBackground ());
        setBorder (BorderFactory.createLineBorder ());
        setLayout (LayoutFactory.createVerticalLayout ());

        header = new Widget (scene);
        header.setLayout (LayoutFactory.createHorizontalLayout (LayoutFactory.SerialAlignment.CENTER, 0));
        header.addChild (imageWidget = new ImageWidget (scene));
        header.addChild (labelWidget = new LabelWidget (scene));
        addChild (header);

        addChild (new SeparatorWidget (scene, SeparatorWidget.Orientation.HORIZONTAL));

        setState (ObjectState.createNormal ());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        LookFeel lookFeel = getScene ().getLookFeel ();
        header.setBorder (BorderFactory.createCompositeBorder (BorderFactory.createEmptyBorder (2), lookFeel.getBorder (state)));
        labelWidget.setForeground (lookFeel.getForeground (state));
    }

    /**
     * Sets an image used in the list header.
     * @param image the image
     */
    public final void setImage (Image image) {
        imageWidget.setImage (image);
    }

    /**
     * Sets a label used in the list header.
     * @param label the label
     */
    public final void setLabel (String label) {
        labelWidget.setLabel (label);
    }

    /**
     * Returns a header widget.
     * @return the header widget
     */
    public final Widget getHeader () {
        return header;
    }

    /**
     * Returns an image widget in the header.
     * @return the image widget
     */
    public final ImageWidget getImageWidget () {
        return imageWidget;
    }

    /**
     * Returns a label widget in the header.
     * @return the label widget
     */
    public final LabelWidget getLabelWidget () {
        return labelWidget;
    }

}
