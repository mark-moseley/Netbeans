/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * This class represents a node widget in the VMD visualization style. It implements the minimize ability.
 * It allows to add pin widgets into the widget using <code>attachPinWidget</code> method.
 * <p>
 * The node widget consists of a header (with an image, a name, secondary name and a glyph set) and the content.
 * The content contains pin widgets. Pin widgets can be organized in pin-categories defined by calling <code>sortPins</code> method.
 * The <code>sortPins</code> method has to be called refresh the order after adding a pin widget.
 *
 * @author David Kaspar
 */
public class VMDNodeWidget extends Widget implements StateModel.Listener, VMDMinimizeAbility {

    private Widget header;
    private ImageWidget minimizeWidget;
    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;
    private VMDGlyphSetWidget glyphSetWidget;

    private SeparatorWidget pinsSeparator;

    private HashMap<String, Widget> pinCategoryWidgets = new HashMap<String, Widget> ();

    private StateModel stateModel = new StateModel (2);
    private Anchor nodeAnchor;
    private VMDColorScheme scheme;

    private WeakHashMap<Anchor,Anchor> proxyAnchorCache = new WeakHashMap<Anchor, Anchor> ();

    /**
     * Creates a node widget.
     * @param scene the scene
     */
    public VMDNodeWidget (Scene scene) {
        this (scene, VMDFactory.getOriginalScheme ());
    }

    /**
     * Creates a node widget with a specific color scheme.
     * @param scene the scene
     * @param scheme the color scheme
     */
    public VMDNodeWidget (Scene scene, VMDColorScheme scheme) {
        super (scene);
        assert scheme != null;
        this.scheme = scheme;

        nodeAnchor = new VMDNodeAnchor (this, true, scheme);
        
        setLayout (LayoutFactory.createVerticalFlowLayout ());
        setMinimumSize (new Dimension (128, 8));

        header = new Widget (scene);
        header.setLayout (LayoutFactory.createHorizontalFlowLayout (LayoutFactory.SerialAlignment.CENTER, 8));
        addChild (header);

        boolean right = scheme.isNodeMinimizeButtonOnRight (this);

        minimizeWidget = new ImageWidget (scene, scheme.getMinimizeWidgetImage (this));
        minimizeWidget.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        minimizeWidget.getActions ().addAction (new ToggleMinimizedAction ());
        if (! right)
            header.addChild (minimizeWidget);

        imageWidget = new ImageWidget (scene);
        header.addChild (imageWidget);

        nameWidget = new LabelWidget (scene);
        nameWidget.setFont (scene.getDefaultFont ().deriveFont (Font.BOLD));
        header.addChild (nameWidget);

        typeWidget = new LabelWidget (scene);
        typeWidget.setForeground (Color.BLACK);
        header.addChild (typeWidget);

        glyphSetWidget = new VMDGlyphSetWidget (scene);
        header.addChild (glyphSetWidget);

        if (right) {
            Widget widget = new Widget (scene);
            widget.setOpaque (false);
            header.addChild (widget, 1000);
            header.addChild (minimizeWidget);
        }

        pinsSeparator = new SeparatorWidget (scene, SeparatorWidget.Orientation.HORIZONTAL);
        addChild (pinsSeparator);

        Widget topLayer = new Widget (scene);
        addChild (topLayer);

        stateModel = new StateModel ();
        stateModel.addListener (this);

        scheme.installUI (this);
        notifyStateChanged (ObjectState.createNormal (), ObjectState.createNormal ());
    }

    /**
     * Called to check whether a particular widget is minimizable. By default it returns true.
     * The result have to be the same for whole life-time of the widget. If not, then the revalidation has to be invoked manually.
     * An anchor (created by <code>VMDNodeWidget.createPinAnchor</code> is not affected by this method.
     * @param widget the widget
     * @return true, if the widget is minimizable; false, if the widget is not minimizable
     */
    protected boolean isMinimizableWidget (Widget widget) {
        return true;
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
        boolean minimized = stateModel.getBooleanState ();
        Rectangle rectangle = minimized ? new Rectangle () : null;
        for (Widget widget : getChildren ())
            if (widget != header  &&  widget != pinsSeparator) {
                getScene ().getSceneAnimator ().animatePreferredBounds (widget, minimized  && isMinimizableWidget (widget) ? rectangle : null);
            }
        minimizeWidget.setImage (scheme.getMinimizeWidgetImage (this));
    }

    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
        scheme.updateUI (this, previousState, state);
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
        typeWidget.setLabel (nodeType != null ? "[" + nodeType + "]" : null);
    }

    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public void attachPinWidget (Widget widget) {
        widget.setCheckClipping (true);
        addChild (widget);
        if (stateModel.getBooleanState ()  && isMinimizableWidget (widget))
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
     * @return the extended pin anchor, the returned anchor is cached and returns a single extended pin anchor instance of each original pin anchor
     */
    public Anchor createAnchorPin (Anchor anchor) {
        Anchor proxyAnchor = proxyAnchorCache.get (anchor);
        if (proxyAnchor == null) {
            proxyAnchor = AnchorFactory.createProxyAnchor (stateModel, anchor, nodeAnchor);
            proxyAnchorCache.put (anchor, proxyAnchor);
        }
        return proxyAnchor;
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
        Widget label = scheme.createPinCategoryWidget (this, categoryDisplayName);
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

    /**
     * Returns a minimize button widget.
     * @return the miminize button widget
     */
    public Widget getMinimizeButton () {
        return minimizeWidget;
    }

    /**
     * Returns a pins separator.
     * @return the pins separator
     */
    public Widget getPinsSeparator () {
        return pinsSeparator;
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