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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.DirectionalAnchor;
import org.netbeans.api.visual.anchor.ProxyAnchor;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.EmptyBorder;
import org.netbeans.api.visual.border.ImageBorder;
import org.netbeans.api.visual.layout.SerialLayout;
import org.netbeans.api.visual.widget.*;
import org.netbeans.api.visual.model.ObjectState;
import org.openide.util.Utilities;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public class VMDNodeWidget extends Widget {

    private static Border BORDER_SHADOW_NORMAL = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_normal.png")); // NOI18N
    private static Border BORDER_SHADOW_HOVERED = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_hovered.png")); // NOI18N
    private static Border BORDER_SHADOW_SELECTED = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_selected.png")); // NOI18N

    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;
    private Widget pinsWidget;
    private VMDGlyphSetWidget glyphSetWidget;
    private VMDMinimizeWidget minimizeWidget;
    private Anchor nodeAnchor = new DirectionalAnchor (this, DirectionalAnchor.Kind.VERTICAL);

    public VMDNodeWidget (Scene scene) {
        super (scene);

        setBackground (Color.WHITE);
        setOpaque (true);
        setBorder (BORDER_SHADOW_NORMAL);
        setCursor (new Cursor (Cursor.MOVE_CURSOR));

        final Widget mainLayer = new Widget (scene);
        mainLayer.setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL));
        addChild (mainLayer);

        Widget header = new Widget (scene);
        header.setBorder (new EmptyBorder (4));
        header.setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL, SerialLayout.Alignment.CENTER, 0));
        mainLayer.addChild (header);

        imageWidget = new ImageWidget (scene);
        header.setBorder (new EmptyBorder (4));
        header.addChild (imageWidget);

        Widget desc = new Widget (scene);
        desc.setBorder (new EmptyBorder (4));
        desc.setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL));
        header.addChild (desc);

        nameWidget = new LabelWidget (scene);
        nameWidget.setFont (scene.getDefaultFont ().deriveFont (Font.BOLD));
        desc.addChild (nameWidget);

        typeWidget = new LabelWidget (scene);
        typeWidget.setForeground (Color.GRAY);
        desc.addChild (typeWidget);

        glyphSetWidget = new VMDGlyphSetWidget (scene);
        desc.addChild (glyphSetWidget);

        Widget pinsSeparator = new SeparatorWidget (scene, SeparatorWidget.Orientation.HORIZONTAL);
        mainLayer.addChild (pinsSeparator);

//        Widget inner = new Widget (scene);
//        inner.setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL));
//        inner.setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL));
//        addChild (inner);

//        SeparatorWidget separator1 = new SeparatorWidget (scene, SeparatorWidget.Orientation.VERTICAL);
//        separator1.setBorder (new EmptyBorder (8));
//        inner.addChild (separator1);

        pinsWidget = new Widget (scene);
        pinsWidget.setBorder (new EmptyBorder (8, 4));
        pinsWidget.setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL, SerialLayout.Alignment.JUSTIFY, 0));
//        inner.addChild (pinsWidget);
        mainLayer.addChild (pinsWidget);

//        SeparatorWidget separator2 = new SeparatorWidget (scene, SeparatorWidget.Orientation.VERTICAL);
//        separator2.setBorder (new EmptyBorder (8));
//        inner.addChild (separator2);


        Widget topLayer = new Widget (scene);
        addChild (topLayer);

        minimizeWidget = new VMDMinimizeWidget (scene, mainLayer, Arrays.asList (pinsSeparator, pinsWidget));
        topLayer.addChild (minimizeWidget);
    }

    /**
     * Check the minimized state.
     *
     * @see VMDMinimizeWidget#isMinimized()
     */
    public boolean isMinimized() {
        return minimizeWidget.isMinimized();
    }

    /**
     * Set the minimized state.  This method will add/remove child
     * Widgets from the this Widget and switch Anchors.<br><br>
     *
     * When the Widget is minimized, new Edges can't be added to the
     * children of the Widget since they don't exist on the 
     * parent anymore.
     *
     * @see VMDMinimizeWidget#setMinimized(boolean minimized)
     */
    public void setMinimized(boolean minimized) {
        minimizeWidget.setMinimized(minimized);
    }

    /**
     * Change the minimized state to !{@link #isMinimized()}.
     *
     * @see VMDMinimizeWidget#toggleMinimized()
     */
    public void toggleMinimized() {
        minimizeWidget.toggleMinimized();
    }

    protected void notifyStateChanged (ObjectState state) {
        if (state.isHovered ())
            setBorder (BORDER_SHADOW_HOVERED);
        else if (state.isSelected ())
            setBorder (BORDER_SHADOW_SELECTED);
        else
            setBorder (BORDER_SHADOW_NORMAL);
    }

    public void setNodeImage (Image image) {
        imageWidget.setImage (image);
        revalidate ();
    }

    public String getNodeName () {
        return nameWidget.getLabel ();
    }

    public void setNodeName (String nodeName) {
        nameWidget.setLabel (nodeName);
    }

    public void setNodeType (String nodeType) {
        typeWidget.setLabel ("[" + nodeType + "]");
    }

    public void addPin (VMDPinWidget widget) {
        pinsWidget.addChild (widget);
    }

    public void setGlyphs (List<Image> glyphs) {
        glyphSetWidget.setGlyphs (glyphs);
    }
    
    public Anchor getNodeAnchor () {
        return nodeAnchor;
    }
    
    public ProxyAnchor createAnchorPin (Anchor anchor) {
        ProxyAnchor proxyAnchor = new ProxyAnchor (anchor, nodeAnchor);
        minimizeWidget.addProxyAnchor (proxyAnchor);
        return proxyAnchor;
    }

}
