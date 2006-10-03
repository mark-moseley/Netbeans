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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.*;
import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * This class represents a node widget. It implements the minimize ability. It allows to add pin widgets into the widget
 * using attachPinWidget method.
 *
 * @author David Kaspar
 */
public class VMDNodeWidget extends Widget implements StateModel.Listener, VMDMinimizeAbility {

    private static final Border BORDER_NODE = new VMDNodeBorder ();
    private static final Color BORDER_CATEGORY_BACKGROUND = new Color (0xCDDDF8);
    public static final Color COLOR_SELECTED = new Color (0x748CC0);
//    static final Border BORDER_LINE = BorderFactory.createLineBorder (4, VMDNodeBorder.COLOR_BORDER);
    static final Border BORDER = BorderFactory.createOpaqueBorder (2, 2, 2, 2);
    static final Border BORDER_HOVERED = BorderFactory.createLineBorder (2, Color.BLACK);

    private Widget header;
    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;
    private VMDGlyphSetWidget glyphSetWidget;

    private SeparatorWidget pinsSeparator;

    private HashMap<String, Widget> pinCategoryWidgets = new HashMap<String, Widget> ();
    private Font fontPinCategory = getScene ().getFont ().deriveFont (10.0f);

    private StateModel stateModel = new StateModel (2);
    private Anchor nodeAnchor = new VMDNodeAnchor (this);

    /**
     * Creates a node widget.
     * @param scene the scene
     */
    public VMDNodeWidget (Scene scene) {
        super (scene);

        setOpaque (false);
        setBorder (BORDER_NODE);
        setLayout (LayoutFactory.createVerticalLayout ());
        setMinimumBounds (new Rectangle (0, 0, 128, 0));

        header = new Widget (scene);
        header.setBorder (BORDER);
        header.setBackground (COLOR_SELECTED);
        header.setOpaque (false);
        header.setLayout (LayoutFactory.createHorizontalLayout (LayoutFactory.SerialAlignment.CENTER, 8));
        addChild (header);

        imageWidget = new ImageWidget (scene);
        header.addChild (imageWidget);

//        Widget desc = new Widget (scene);
//        desc.setLayout (LayoutFactory.createVerticalLayout ());
//        header.addChild (desc);

        nameWidget = new LabelWidget (scene);
        nameWidget.setFont (scene.getDefaultFont ().deriveFont (Font.BOLD));
//        desc.addChild (nameWidget);
        header.addChild (nameWidget);

        typeWidget = new LabelWidget (scene);
        typeWidget.setForeground (Color.BLACK);
//        desc.addChild (typeWidget);
        header.addChild (typeWidget);

        glyphSetWidget = new VMDGlyphSetWidget (scene);
//        desc.addChild (glyphSetWidget);
        header.addChild (glyphSetWidget);

        Widget minimizeWidget = new ImageWidget (scene, Utilities.loadImage ("org/netbeans/modules/visual/resources/minimize.png"));
        minimizeWidget.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        minimizeWidget.getActions ().addAction (new ToggleMinimizedAction ());
//        header.addChild (minimizeWidget);
        header.addChild (0, minimizeWidget);

        pinsSeparator = new SeparatorWidget (scene, SeparatorWidget.Orientation.HORIZONTAL);
        pinsSeparator.setForeground (BORDER_CATEGORY_BACKGROUND);
        addChild (pinsSeparator);

        Widget topLayer = new Widget (scene);
        addChild (topLayer);

        stateModel = new StateModel ();
        stateModel.addListener (this);

        notifyStateChanged (ObjectState.createNormal (), ObjectState.createNormal ());
    }

    /**
     * Check the minimized state.
     * @return true, if minimized
     */
    public boolean isMinimized () {
        return stateModel.getBooleanState ();
    }

    /**
     * Set the minimized state. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     * @param minimized if true, then the widget is going to be minimized
     */
    public void setMinimized (boolean minimized) {
        stateModel.setBooleanState (minimized);
    }

    /**
     * Toggles the minimized state. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     */
    public void toggleMinimized () {
        stateModel.toggleBooleanState ();
    }

    /**
     * Called when a minimized state is changed. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     */
    public void stateChanged () {
        Rectangle rectangle = stateModel.getBooleanState () ? new Rectangle () : null;
        for (Widget widget : getChildren ())
            if (widget != header  &&  widget != pinsSeparator)
                getScene ().getSceneAnimator ().animatePreferredBounds (widget, rectangle);
    }

    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (! previousState.isSelected ()  &&  state.isSelected ())
            bringToFront ();
        else if (! previousState.isHovered ()  &&  state.isHovered ())
            bringToFront ();

        header.setOpaque (state.isSelected ());
        header.setBorder (state.isHovered () ? VMDNodeWidget.BORDER_HOVERED : VMDNodeWidget.BORDER);
    }

    /**
     * Sets a node image.
     * @param image the image
     */
    public void setNodeImage (Image image) {
        imageWidget.setImage (image);
        revalidate ();
    }

    /**
     * Returns a node name.
     * @return the node name
     */
    public String getNodeName () {
        return nameWidget.getLabel ();
    }

    /**
     * Sets a node name.
     * @param nodeName the node name
     */
    public void setNodeName (String nodeName) {
        nameWidget.setLabel (nodeName);
    }

    /**
     * Sets a node type (secondary name).
     * @param nodeType the node type
     */
    public void setNodeType (String nodeType) {
        typeWidget.setLabel ("[" + nodeType + "]");
    }

    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public void attachPinWidget (Widget widget) {
        widget.setCheckClipping (true);
        addChild (widget);
        if (stateModel.getBooleanState ())
            widget.setPreferredBounds (new Rectangle ());
    }

    /**
     * Sets node glyphs.
     * @param glyphs the list of images
     */
    public void setGlyphs (List<Image> glyphs) {
        glyphSetWidget.setGlyphs (glyphs);
    }

    /**
     * Sets all node properties at once.
     * @param image the node image
     * @param nodeName the node name
     * @param nodeType the node type (secondary name)
     * @param glyphs the node glyphs
     */
    public void setNodeProperties (Image image, String nodeName, String nodeType, List<Image> glyphs) {
        setNodeImage (image);
        setNodeName (nodeName);
        setNodeType (nodeType);
        setGlyphs (glyphs);
    }

    /**
     * Returns a node name widget.
     * @return the node name widget
     */
    public LabelWidget getNodeNameWidget () {
        return nameWidget;
    }

    /**
     * Returns a node anchor.
     * @return the node anchor
     */
    public Anchor getNodeAnchor () {
        return nodeAnchor;
    }

    /**
     * Creates an extended pin anchor with an ability of reconnecting to the node anchor when the node is minimized.
     * @param anchor the original pin anchor from which the extended anchor is created
     * @return the extended pin anchor
     */
    public Anchor createAnchorPin (Anchor anchor) {
        return AnchorFactory.createProxyAnchor (stateModel, anchor, nodeAnchor);
    }

    /**
     * Returns a list of pin widgets attached to the node.
     * @return the list of pin widgets
     */
    private List<Widget> getPinWidgets () {
        ArrayList<Widget> pins = new ArrayList<Widget> (getChildren ());
        pins.remove (header);
        pins.remove (pinsSeparator);
        return pins;
    }

    /**
     * Sorts and assigns pins into categories.
     * @param pinsCategories the map of category name as key and a list of pin widgets as value
     */
    public void sortPins (HashMap<String, List<Widget>> pinsCategories) {
        List<Widget> previousPins = getPinWidgets ();
        ArrayList<Widget> unresolvedPins = new ArrayList<Widget> (previousPins);

        for (Iterator<Widget> iterator = unresolvedPins.iterator (); iterator.hasNext ();) {
            Widget widget = iterator.next ();
            if (pinCategoryWidgets.containsValue (widget))
                iterator.remove ();
        }

        ArrayList<String> unusedCategories = new ArrayList<String> (pinCategoryWidgets.keySet ());

        ArrayList<String> categoryNames = new ArrayList<String> (pinsCategories.keySet ());
        Collections.sort (categoryNames);

        ArrayList<Widget> newWidgets = new ArrayList<Widget> ();
        for (String categoryName : categoryNames) {
            if (categoryName == null)
                continue;
            unusedCategories.remove (categoryName);
            newWidgets.add (createPinCategoryWidget (categoryName));
            List<Widget> widgets = pinsCategories.get (categoryName);
            for (Widget widget : widgets)
                if (unresolvedPins.remove (widget))
                    newWidgets.add (widget);
        }

        if (! unresolvedPins.isEmpty ())
            newWidgets.addAll (0, unresolvedPins);

        for (String category : unusedCategories)
            pinCategoryWidgets.remove (category);

        removeChildren (previousPins);
        addChildren (newWidgets);
    }

    private Widget createPinCategoryWidget (String categoryDisplayName) {
        Widget w = pinCategoryWidgets.get (categoryDisplayName);
        if (w != null)
            return w;
        LabelWidget label = new LabelWidget (getScene (), categoryDisplayName);
        label.setOpaque (true);
        label.setBackground (BORDER_CATEGORY_BACKGROUND);
        label.setForeground (Color.GRAY);
        label.setFont (fontPinCategory);
        label.setAlignment (LabelWidget.Alignment.CENTER);
        label.setCheckClipping (true);
        if (stateModel.getBooleanState ())
            label.setPreferredBounds (new Rectangle ());
        pinCategoryWidgets.put (categoryDisplayName, label); 
        return label;
    }

    /**
     * Collapses the widget.
     */
    public void collapseWidget () {
        stateModel.setBooleanState (true);
    }

    /**
     * Expands the widget.
     */
    public void expandWidget () {
        stateModel.setBooleanState (false);
    }

    /**
     * Returns a header widget.
     * @return the header widget
     */
    public Widget getHeader () {
        return header;
    }

    private final class ToggleMinimizedAction extends WidgetAction.Adapter {

        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            if (event.getButton () == MouseEvent.BUTTON1 || event.getButton () == MouseEvent.BUTTON2) {
                stateModel.toggleBooleanState ();
                return State.CONSUMED;
            }
            return State.REJECTED;
        }
    }

}