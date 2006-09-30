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
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.modules.visual.laf.DefaultLookFeel;
import org.netbeans.modules.visual.widget.SatelliteComponent;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * The scene is a widget which also controls and represents whole rendered area.
 * <p>
 * After all changes in a scene is done, the validate method have to be called for validating changed
 * and calculating new locations and boundaries of all modified widgets.
 * <p>
 * The scene allows to create a view JComponent which can be used anywhere in Swing based application. Only one view
 * can be created using the createView method.
 * The scene allows to create multiple satellite views using the createSatelliteView method. The satellite view is just
 * showing the scene and allows quick navigator and panning using a mouse.
 * <p>
 * The scene contains additional scene-specific properties like lookFeel, activeTool, defaultFont, animator.
 * <p>
 * It is able to create a widget-specific hover action.
 *
 * @author David Kaspar
 */
// TODO - take SceneComponent dimension and correct Scene.resolveBounds
// TODO - remove SuppressWarnings
public class Scene extends Widget {

    private double zoomFactor = 1.0;
    private SceneAnimator sceneAnimator;

    private JComponent component;
    private Graphics2D graphics;
    private boolean paintEverything = true;

    private Font defaultFont;
    private Rectangle repaintRegion = null;
    private HashSet<Widget> repaintWidgets = new HashSet<Widget> ();
    private LookFeel lookFeel = new DefaultLookFeel ();
    private String activeTool;

    private final ArrayList<SceneListener> sceneListeners = new ArrayList<SceneListener> ();

    private WidgetAction widgetHoverAction;

    /**
     * Creates a scene.
     */
    public Scene () {
        super (null);
        defaultFont = Font.decode (null);
        resolveBounds (new Point (), new Rectangle ());
        setOpaque(true);
        setFont (defaultFont);
        setBackground (lookFeel.getBackground ());
        setForeground (lookFeel.getForeground ());
        sceneAnimator = new SceneAnimator (this);
    }

    /**
     * Creates a view. This method could be called once only. Call the getView method for getting created instance of a view.
     * @return the created view
     */
    public JComponent createView () {
        assert component == null;
        component = new SceneComponent (this);
        component.addAncestorListener (new AncestorListener() {
            public void ancestorAdded (AncestorEvent event) {
                repaintSatellite ();
            }
            public void ancestorRemoved (AncestorEvent event) {
                repaintSatellite ();
            }
            public void ancestorMoved (AncestorEvent event) {
                repaintSatellite ();
            }
        });
        return component;
    }

    /**
     * Returns an instance of created view
     * @return the instance of created view; null if no view is created yet
     */
    public JComponent getView () {
        return component;
    }

    /**
     * Creates a satellite view.
     * @return the satellite view
     */
    public JComponent createSatelliteView () {
        return new SatelliteComponent (this);
    }

    /**
     * Returns an instance of Graphics2D which is used for calculating boundaries and rendering all widgets in the scene.
     * @return the instance of Graphics2D
     */
    public final Graphics2D getGraphics () {
        return graphics;
    }

    // HACK - used by SceneComponent and ConvolutionWidget
    final void setGraphics (Graphics2D graphics) {
        this.graphics = graphics;
    }

    /**
     * Paints the whole scene into the graphics instance. The method calls validate before rendering.
     * @param graphics the Graphics2D instance where the scene is going to be painted
     */
    public final void paint (Graphics2D graphics) {
        validate ();
        Graphics2D prevoiusGraphics = getGraphics ();
        setGraphics (graphics);
        paint ();
        setGraphics (prevoiusGraphics);
    }

    /**
     * Returns a default font of the scene.
     * @return the default font
     */
    public Font getDefaultFont () {
        return defaultFont;
    }

    /**
     * Returns whether the whole scene is validated and there is no widget or region that has to be revalidated.
     * @return true, if the whole scene is validated
     */
    public boolean isValidated () {
        return super.isValidated ()  &&  repaintRegion == null  &&  repaintWidgets.isEmpty ();
    }

    /**
     * Returns whether the layer widget requires to repainted after revalidation.
     * @return always false
     */
    protected boolean isRepaintRequiredForRevalidating () {
        return false;
    }

    // TODO - maybe it could improve the perfomance, if bounds != null then do nothing
    // WARNING - you have to asure that there will be no component/widget will change its location/bounds between this and validate method calls
    final void revalidateWidget (Widget widget) {
        Rectangle widgetBounds = widget.getBounds ();
        if (widgetBounds != null) {
            Rectangle sceneBounds = widget.convertLocalToScene (widgetBounds);
            if (repaintRegion == null)
                repaintRegion = sceneBounds;
            else
                repaintRegion.add (sceneBounds);
        }
        repaintWidgets.add (widget);
    }

    // TODO - requires optimalization while changing preferred size and calling revalidate/repaint
    private void layoutScene () {
        layout (false);
        justify ();

        Rectangle rect = null;
        for (Widget widget : getChildren ()) {
            Point location = widget.getLocation ();
            Rectangle bounds = widget.getBounds ();
            bounds.translate (location.x, location.y);
            if (rect == null)
                rect = bounds;
            else
                rect.add (bounds);
        }
        if (rect != null) {
            Insets insets = getBorder ().getInsets ();
            rect.x -= insets.left;
            rect.y -= insets.top;
            rect.width += insets.left + insets.right;
            rect.height += insets.top + insets.bottom;
        }

        Point preLocation = getLocation ();
        Rectangle preBounds = getBounds ();
        resolveBounds (rect != null ? new Point (- rect.x, - rect.y) : new Point (), rect);

        Dimension preferredSize = rect != null ? rect.getSize () : new Dimension ();
        preferredSize = new Dimension ((int) (preferredSize.width * zoomFactor), (int) (preferredSize.height * zoomFactor));
        if (! preferredSize.equals (component.getPreferredSize ())) {
            component.setPreferredSize (preferredSize);
            component.revalidate ();
//            repaintSatellite ();
        }

        Dimension componentSize = component.getSize ();
        componentSize.width = (int) (componentSize.width / zoomFactor);
        componentSize.height = (int) (componentSize.height / zoomFactor);
        Rectangle bounds = getBounds ();

        boolean sceneResized = false;
        if (bounds.width < componentSize.width) {
            bounds.width = componentSize.width;
            sceneResized = true;
        }
        if (bounds.height < componentSize.height) {
            bounds.height = componentSize.height;
            sceneResized = true;
        }
        if (sceneResized)
            resolveBounds (getLocation (), bounds);

        if (! getLocation ().equals (preLocation)  ||  ! bounds.equals (preBounds)) {
            Rectangle rectangle = convertLocalToScene (getBounds ());
            if (repaintRegion == null)
                repaintRegion = rectangle;
            else
                repaintRegion.add (rectangle);
        }
    }

    /**
     * Validates all widget in the whole scene. The validation is done repeatively until there is no invalid widget
     * in the scene after validating process. It also schedules invalid regions in the view for repainting.
     */
    @SuppressWarnings("unchecked")
    public final void validate () {
        if (graphics == null)
            return;

        while (! isValidated ()) {
            SceneListener[] ls;
            synchronized (sceneListeners) {
                ls = sceneListeners.toArray (new SceneListener[sceneListeners.size ()]);
            }

            for (SceneListener listener : ls)
                listener.sceneValidating ();

            layoutScene ();

            for (Widget widget : repaintWidgets) {
                Rectangle repaintBounds = widget.getBounds ();
                if (repaintBounds == null)
                    continue;
                repaintBounds = widget.convertLocalToScene (repaintBounds);
                if (repaintRegion != null)
                    repaintRegion.add (repaintBounds);
                else
                    repaintRegion = repaintBounds;
            }
            repaintWidgets.clear ();
    //        System.out.println ("r = " + r);

            // NOTE - maybe improves performance when component.repaint will be called for all widgets/rectangles separately
            if (repaintRegion != null) {
                Rectangle r = convertSceneToView (repaintRegion);
                r.grow (1, 1);
                component.repaint (r);
                repaintSatellite ();
                repaintRegion = null;
            }
    //        System.out.println ("time: " + System.currentTimeMillis ());

            for (SceneListener listener : ls)
                listener.sceneValidated ();
        }
    }

    private void repaintSatellite () {
        SceneListener[] ls;
        synchronized (sceneListeners) {
            ls = sceneListeners.toArray (new SceneListener[sceneListeners.size ()]);
        }

        for (SceneListener listener : ls)
            listener.sceneRepaint ();
    }

    boolean isPaintEverything () {
        return paintEverything;
    }

    void setPaintEverything (boolean paintEverything) {
        this.paintEverything = paintEverything;
    }

    /**
     * Returns a zoom factor.
     * @return the zoom factor
     */
    public final double getZoomFactor () {
        return zoomFactor;
    }

    /**
     * Sets a zoom factor for the scene.
     * @param zoomFactor the zoom factor
     */
    public final void setZoomFactor (double zoomFactor) {
        this.zoomFactor = zoomFactor;
        revalidate ();
    }

    /**
     * Returns a scene animator of the scene.
     * @return the scene animator
     */
    public final SceneAnimator getSceneAnimator () {
        return sceneAnimator;
    }

    /**
     * Returns a look'n'feel of the scene.
     * @return the look'n'feel
     */
    public final LookFeel getLookFeel () {
        return lookFeel;
    }

    /**
     * Sets a look'n'feel of the scene. This method does affect current state of the scene - already created components
     * will not be refreshed.
     * @param lookFeel the look'n'feel
     */
    public final void setLookFeel (LookFeel lookFeel) {
        assert lookFeel != null;
        this.lookFeel = lookFeel;
    }

    /**
     * Returns an active tool of the scene.
     * @return the active tool; if null, then only default action chain of widgets will be used
     */
    public final String getActiveTool () {
        return activeTool;
    }

    /**
     * Sets an active tool.
     * @param activeTool the active tool; if null, then the active tool is unset and only default action chain of widgets will be used
     */
    public void setActiveTool (String activeTool) {
        this.activeTool = activeTool;
    }

    /**
     * Registers a scene listener.
     * @param listener the scene listener
     */
    public final void addSceneListener (SceneListener listener) {
        assert listener != null;
        synchronized (sceneListeners) {
            sceneListeners.add (listener);
        }
    }

    /**
     * Unregisters a scene listener.
     * @param listener the scene listener
     */
    public final void removeSceneListener (SceneListener listener) {
        synchronized (sceneListeners) {
            sceneListeners.remove (listener);
        }
    }

    /**
     * Converts a location in the scene coordination system to the view coordination system.
     * @param sceneLocation the scene location
     * @return the view location
     */
    public final Point convertSceneToView (Point sceneLocation) {
        Point location = getLocation ();
        return new Point ((int) (zoomFactor * (location.x + sceneLocation.x)), (int) (zoomFactor * (location.y + sceneLocation.y)));
    }

    /**
     * Converts a rectangle in the scene coordination system to the view coordination system.
     * @param sceneRectangle the scene rectangle
     * @return the view rectangle
     */
    public final Rectangle convertSceneToView (Rectangle sceneRectangle) {
        Point location = getLocation ();
        return GeomUtil.roundRectangle (new Rectangle2D.Double (
                (double) (sceneRectangle.x + location.x) * zoomFactor,
                (double) (sceneRectangle.y + location.y) * zoomFactor,
                (double) sceneRectangle.width * zoomFactor,
                (double) sceneRectangle.height * zoomFactor));
    }

    /**
     * Converts a location in the view coordination system to the scene coordination system.
     * @param viewLocation the view location
     * @return the scene location
     */
    public Point convertViewToScene (Point viewLocation) {
        return new Point ((int) ((double) viewLocation.x / zoomFactor) - getLocation ().x, (int) ((double) viewLocation.y / zoomFactor) - getLocation ().y);
    }

    /**
     * Creates a widget-specific hover action.
     * @return the widget-specific hover action
     */
    public WidgetAction createWidgetHoverAction () {
        if (widgetHoverAction == null) {
            widgetHoverAction = ActionFactory.createHoverAction (new WidgetHoverAction ());
            getActions ().addAction (widgetHoverAction);
        }
        return widgetHoverAction;
    }

    private class WidgetHoverAction implements TwoStateHoverProvider {

        public void unsetHovering (Widget widget) {
            widget.setState (widget.getState ().deriveWidgetHovered (false));
        }

        public void setHovering (Widget widget) {
            widget.setState (widget.getState ().deriveWidgetHovered (true));
        }

    }

    /**
     * The scene listener which is notified about repainting, validating progress.
     */
    public interface SceneListener {

        /**
         * Called to notify that the whole scene was repainted.
         */
        void sceneRepaint ();

        /**
         * Called to notify that the scene is going to be validated.
         */
        void sceneValidating ();

        /**
         * Called to notify that the scene has been validated.
         */
        void sceneValidated ();

    }

}
