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

import org.netbeans.api.visual.action.WidgetAction;

import javax.swing.*;
import javax.accessibility.AccessibleContext;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Map;

/**
 * @author David Kaspar
 */
final class SceneComponent extends JComponent implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener,FocusListener, DropTargetListener {

    private Scene scene;
    private Widget lockedWidget;
    private WidgetAction lockedAction;
    private long eventIDcounter = 0;
    private AccessibleContext accessibleContext;

    public SceneComponent (Scene scene) {
        this.scene = scene;
        setOpaque (false);
        setDoubleBuffered (true);
        setLayout (null);
        addMouseListener (this);
        addMouseMotionListener (this);
        addMouseWheelListener (this);
        addKeyListener (this);
        setDropTarget (new DropTarget (this, DnDConstants.ACTION_COPY_OR_MOVE, this));
        setAutoscrolls (true);
        setRequestFocusEnabled (true);
        setFocusable (true);
        setFocusTraversalKeysEnabled (false);
    }

    public void addNotify () {
        super.addNotify ();
        scene.setGraphics ((Graphics2D) getGraphics ());
        scene.revalidate ();
        scene.setViewShowing (true);
        scene.validate ();
    }

    public void removeNotify () {
        super.removeNotify ();
        scene.setViewShowing (false);
    }

    public AccessibleContext getAccessibleContext () {
        return accessibleContext;
    }

    private void setAccessibleContext (AccessibleContext accessibleContext) {
        this.accessibleContext = accessibleContext;
    }

    public void setBounds (int x, int y, int width, int height) {
        super.setBounds (x, y, width, height);


        Rectangle bounds = scene.getBounds ();
        double zoomFactor = scene.getZoomFactor();
        if (bounds != null  &&  width == (int) (bounds.width * zoomFactor) && height == (int) (bounds.height * zoomFactor))
            return;

        scene.revalidate ();
        scene.validate ();
    }

    public void paint (Graphics g) {
//        System.out.println ("CLIP: " + g.getClipBounds ());
//        long s = System.currentTimeMillis ();
        Graphics2D gr = (Graphics2D) g;

        Object props = Toolkit.getDefaultToolkit ().getDesktopProperty ("awt.font.desktophints"); // NOI18N
        if (props instanceof Map)
            gr.addRenderingHints ((Map) props);
        gr.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        scene.setGraphics (gr);

        AffineTransform previousTransform = gr.getTransform ();
        double zoomFactor = scene.getZoomFactor ();
        gr.scale (zoomFactor, zoomFactor);
        scene.setPaintEverything (false);
        scene.paint ();
        scene.setPaintEverything (true);
        gr.setTransform (previousTransform);

        g.setColor (Color.BLACK);
        super.paint (g);
//        System.out.println ("PAINT Time: " + (System.currentTimeMillis () - s));
    }

    public void focusGained(FocusEvent e) {
        processOperator (Operator.FOCUS_GAINED, new WidgetAction.WidgetFocusEvent (++ eventIDcounter, e));
    }

    public void focusLost(FocusEvent e) {
        processOperator (Operator.FOCUS_LOST, new WidgetAction.WidgetFocusEvent (++ eventIDcounter, e));
    }

    public void mouseClicked (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_CLICKED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mousePressed (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_PRESSED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseReleased (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_RELEASED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseEntered (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_ENTERED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseExited (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_EXITED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseDragged (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_DRAGGED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseMoved (MouseEvent e) {
        MouseContext context = new MouseContext ();
        Point point = scene.convertViewToScene (e.getPoint ());
//        Point sceneLocation = scene.getLocation ();
//        point.translate (sceneLocation.x, sceneLocation.y);
        resolveContext (scene, point, context);
        context.commit (this);
        processLocationOperator (Operator.MOUSE_MOVED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseWheelMoved (MouseWheelEvent e) {
        processLocationOperator (Operator.MOUSE_WHEEL, new WidgetAction.WidgetMouseWheelEvent (++ eventIDcounter, e));
    }

    public void keyTyped (KeyEvent e) {
        WidgetAction.State state = processKeyOperator (Operator.KEY_TYPED, new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e));
        if (state.isConsumed ())
            e.consume ();
    }

    public void keyPressed (KeyEvent e) {
        WidgetAction.State state = processKeyOperator (Operator.KEY_PRESSED, new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e));
        if (state.isConsumed ())
            e.consume ();
    }

    public void keyReleased (KeyEvent e) {
        WidgetAction.State state = processKeyOperator (Operator.KEY_RELEASED, new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e));
        if (state.isConsumed ())
            e.consume ();
    }

    public void dragEnter (DropTargetDragEvent e) {
        WidgetAction.State state = processLocationOperator (Operator.DRAG_ENTER, new WidgetAction.WidgetDropTargetDragEvent (++ eventIDcounter, e));
        if (! state.isConsumed ())
            e.rejectDrag ();
    }

    public void dragOver (DropTargetDragEvent e) {
        WidgetAction.State state = processLocationOperator (Operator.DRAG_OVER, new WidgetAction.WidgetDropTargetDragEvent (++ eventIDcounter, e));
        if (! state.isConsumed ())
            e.rejectDrag ();
    }

    public void dropActionChanged (DropTargetDragEvent e) {
        WidgetAction.State state = processLocationOperator (Operator.DROP_ACTION_CHANGED, new WidgetAction.WidgetDropTargetDragEvent (++ eventIDcounter, e));
        if (! state.isConsumed ())
            e.rejectDrag ();
    }

    public void dragExit (DropTargetEvent e) {
        processOperator (Operator.DRAG_EXIT, new WidgetAction.WidgetDropTargetEvent (++ eventIDcounter, e));
    }

    public void drop (DropTargetDropEvent e) {
        WidgetAction.State state = processLocationOperator (Operator.DROP, new WidgetAction.WidgetDropTargetDropEvent (++ eventIDcounter, e));
        if (! state.isConsumed ())
            e.rejectDrop ();
        else
            e.dropComplete (true);
    }

    private WidgetAction.State processLocationOperator (Operator operator, WidgetAction.WidgetLocationEvent event) {
        event.setPoint (scene.convertViewToScene (event.getPoint ()));

        WidgetAction.State state;
        Point location;
        String tool = scene.getActiveTool ();

        WidgetAction.Chain priorActions = scene.getPriorActions ();
        if (! priorActions.getActions ().isEmpty ()) {
            location = scene.getLocation ();
            event.translatePoint (location.x, location.y);
            if (operator.operate (priorActions, scene, event).isConsumed ())
                return WidgetAction.State.CONSUMED;
            event.translatePoint (- location.x, - location.y);
        }

        if (lockedAction != null) {
            location = lockedWidget.convertSceneToLocal (new Point ());
            event.translatePoint (location.x, location.y);
            state = operator.operate (lockedAction, lockedWidget, event);
            event.translatePoint (- location.x, - location.y);

            if (! state.isConsumed ()) {
                location = scene.getLocation ();
                event.translatePoint (location.x, location.y);
                state = processLocationOperator (operator, tool, scene, event);
            }
        } else {
            location = scene.getLocation ();
            event.translatePoint (location.x, location.y);
            state = processLocationOperator (operator, tool, scene, event);
        }

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedWidget != null)
            scrollRectToVisible (scene.convertSceneToView (lockedWidget.convertLocalToScene (lockedWidget.getBounds ())));

        return state;
    }

    private WidgetAction.State processLocationOperator (Operator operator, String tool, Widget widget, WidgetAction.WidgetLocationEvent event) {
        if (! widget.isVisible ()  ||  ! widget.isEnabled ())
            return WidgetAction.State.REJECTED;

        Point location = widget.getLocation ();
        event.translatePoint (- location.x, - location.y);

        Rectangle bounds = widget.getBounds ();
        assert bounds != null : Widget.MESSAGE_NULL_BOUNDS;
        if (bounds.contains (event.getPoint ())) {
            WidgetAction.State state;

            List<Widget> children = widget.getChildren ();
            Widget[] childrenArray = children.toArray (new Widget[children.size ()]);

            for (int i = childrenArray.length - 1; i >= 0; i --) {
                Widget child = childrenArray[i];
                state = processLocationOperator (operator, tool, child, event);
                if (state.isConsumed ())
                    return state;
            }

            if (widget.isHitAt (event.getPoint ())) {
                WidgetAction.Chain actions;
                actions = widget.getActions ();
                state = operator.operate (actions, widget, event);
                if (state.isConsumed ())
                    return state;

                actions = widget.getActions (tool);
                if (actions != null) {
                    state = operator.operate (actions, widget, event);
                    if (state.isConsumed ())
                        return state;
                }
            }
        }

        event.translatePoint (location.x, location.y);
        return WidgetAction.State.REJECTED;
    }

    private WidgetAction.State processOperator (Operator operator, WidgetAction.WidgetEvent event) {
        WidgetAction.State state;
        String tool = scene.getActiveTool ();

        WidgetAction.Chain priorActions = scene.getPriorActions ();
        if (! priorActions.getActions ().isEmpty ())
            if (operator.operate (priorActions, scene, event).isConsumed ())
                return WidgetAction.State.CONSUMED;

        if (lockedAction != null) {
            state = operator.operate (lockedAction, lockedWidget, event);
            if (! state.isConsumed ())
                state = processOperator (operator, tool, scene, event);
        } else
            state = processOperator (operator, tool, scene, event);

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedWidget != null)
            scrollRectToVisible (scene.convertSceneToView (lockedWidget.convertLocalToScene (lockedWidget.getBounds ())));

        return state;
    }

    private WidgetAction.State processOperator (Operator operator, String tool, Widget widget, WidgetAction.WidgetEvent event) {
        if (! widget.isVisible ()  ||  ! widget.isEnabled ())
            return WidgetAction.State.REJECTED;

        WidgetAction.State state;

        List<Widget> children = widget.getChildren ();
        Widget[] childrenArray = children.toArray (new Widget[children.size ()]);

        for (int i = childrenArray.length - 1; i >= 0; i --) {
            Widget child = childrenArray[i];
            state = processOperator (operator, tool, child, event);
            if (state.isConsumed ())
                return state;
        }

        state = operator.operate (widget.getActions (), widget, event);
        if (state.isConsumed ())
            return state;

        WidgetAction.Chain actions = widget.getActions (tool);
        if (actions != null) {
            state = operator.operate (actions, widget, event);
            if (state.isConsumed ())
                return state;
        }

        return WidgetAction.State.REJECTED;
    }

    private WidgetAction.State processSingleOperator (Operator operator, String tool, Widget widget, WidgetAction.WidgetEvent event) {
        WidgetAction.State state;

        state = operator.operate (widget.getActions (), widget, event);
        if (state.isConsumed ())
            return state;

        WidgetAction.Chain actions = widget.getActions (tool);
        if (actions != null) {
            state = operator.operate (actions, widget, event);
            if (state.isConsumed ())
                return state;
        }

        return WidgetAction.State.REJECTED;
    }

    private WidgetAction.State processParentOperator (Operator operator, String tool, Widget widget, WidgetAction.WidgetKeyEvent event) {
        while (widget != null) {
            WidgetAction.State state;

            state = operator.operate (widget.getActions (), widget, event);
            if (state.isConsumed ())
                return state;

            WidgetAction.Chain actions = widget.getActions (tool);
            if (actions != null) {
                state = operator.operate (actions, widget, event);
                if (state.isConsumed ())
                    return state;
            }

            widget = widget.getParentWidget ();
        }

        return WidgetAction.State.REJECTED;
    }

    private Widget resolveTopMostDisabledWidget (Widget widget) {
        Widget disabledWidget = null;
        Widget tempWidget = widget;
        while (tempWidget != null) {
            if (! tempWidget.isVisible ()  ||  ! tempWidget.isEnabled ())
                disabledWidget = tempWidget;
            tempWidget = tempWidget.getParentWidget ();
        }
        return disabledWidget;
    }

    private WidgetAction.State processKeyOperator (Operator operator, WidgetAction.WidgetKeyEvent event) {
        WidgetAction.State state;
        String tool = scene.getActiveTool ();

        WidgetAction.Chain priorActions = scene.getPriorActions ();
        if (! priorActions.getActions ().isEmpty ())
            if (operator.operate (priorActions, scene, event).isConsumed ())
                return WidgetAction.State.CONSUMED;

        if (lockedAction != null) {
            state = operator.operate (lockedAction, lockedWidget, event);
            if (! state.isConsumed ())
                state = processKeyOperator (operator, tool, scene, event);
        } else
            state = processKeyOperator (operator, tool, scene, event);

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedWidget != null)
            scrollRectToVisible (scene.convertSceneToView (lockedWidget.convertLocalToScene (lockedWidget.getBounds ())));

        return state;
    }

    private WidgetAction.State processKeyOperator (Operator operator, String tool, Scene scene, WidgetAction.WidgetKeyEvent event) {
        Widget focusedWidget = scene.getFocusedWidget ();
        WidgetAction.State state;
        Widget disabledWidget;
        switch (scene.getKeyEventProcessingType ()) {
            case ALL_WIDGETS:
                return processOperator (operator, tool, scene, event);
            case FOCUSED_WIDGET_AND_ITS_PARENTS:
                disabledWidget = resolveTopMostDisabledWidget (focusedWidget);
                return processParentOperator (operator, tool, disabledWidget != null ? disabledWidget.getParentWidget () : focusedWidget, event);
            case FOCUSED_WIDGET_AND_ITS_CHILDREN:
                disabledWidget = resolveTopMostDisabledWidget (focusedWidget);
                if (disabledWidget != null)
                    return WidgetAction.State.REJECTED;
                state = processSingleOperator (operator, tool, focusedWidget, event);
                if (state.isConsumed ())
                    return state;
                return processOperator (operator, tool, focusedWidget, event);
            case FOCUSED_WIDGET_AND_ITS_CHILDREN_AND_ITS_PARENTS:
                disabledWidget = resolveTopMostDisabledWidget (focusedWidget);
                if (disabledWidget == null) {
                    state = processSingleOperator (operator, tool, focusedWidget, event);
                    if (state.isConsumed ())
                        return state;
                    state = processOperator (operator, tool, focusedWidget, event);
                    if (state.isConsumed ())
                        return state;
                }
                return processParentOperator (operator, tool, disabledWidget != null ? disabledWidget.getParentWidget () : focusedWidget.getParentWidget (), event);
            default:
                throw new IllegalStateException ();
        }
    }

    private boolean resolveContext (Widget widget, Point point, MouseContext context) {
//        Point location = widget.getLocation ();
//        point.translate (- location.x, - location.y);

        if (widget.getBounds ().contains (point)) {
            List<Widget> children = widget.getChildren ();
            for (int i = children.size () - 1; i >= 0; i --) {
                Widget child = children.get (i);
                Point location = child.getLocation ();
                point.translate (- location.x, - location.y);
                boolean resolved = resolveContext (child, point, context);
                point.translate (location.x, location.y);
                if (resolved)
                    return true;
            }
            if (widget.isHitAt (point))
                context.update (widget);
        }

//        point.translate (location.x, location.y);
        return false;
    }


    private interface Operator {

        public static final Operator MOUSE_CLICKED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseClicked (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_PRESSED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mousePressed (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_RELEASED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseReleased (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_ENTERED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseEntered (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_EXITED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseExited (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_DRAGGED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseDragged (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_MOVED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseMoved (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_WHEEL = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseWheelMoved (widget, (WidgetAction.WidgetMouseWheelEvent) event);
            }
        };

        public static final Operator KEY_TYPED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.keyTyped (widget, (WidgetAction.WidgetKeyEvent) event);
            }
        };

        public static final Operator KEY_PRESSED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.keyPressed (widget, (WidgetAction.WidgetKeyEvent) event);
            }
        };

        public static final Operator KEY_RELEASED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.keyReleased (widget, (WidgetAction.WidgetKeyEvent) event);
            }
        };

        public static final Operator FOCUS_GAINED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.focusGained (widget, (WidgetAction.WidgetFocusEvent) event);
            }
        };

        public static final Operator FOCUS_LOST = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.focusLost (widget, (WidgetAction.WidgetFocusEvent) event);
            }
        };

        public static final Operator DRAG_ENTER = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.dragEnter (widget, (WidgetAction.WidgetDropTargetDragEvent) event);
            }
        };

        public static final Operator DRAG_OVER = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.dragOver (widget, (WidgetAction.WidgetDropTargetDragEvent) event);
            }
        };

        public static final Operator DROP_ACTION_CHANGED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.dropActionChanged (widget, (WidgetAction.WidgetDropTargetDragEvent) event);
            }
        };

        public static final Operator DRAG_EXIT = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.dragExit (widget, (WidgetAction.WidgetDropTargetEvent) event);
            }
        };

        public static final Operator DROP = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.drop (widget, (WidgetAction.WidgetDropTargetDropEvent) event);
            }
        };

        public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event);

    }

    private static final class MouseContext {

        private String toolTipText;
        private Cursor cursor;
        private AccessibleContext accessibleContext;

        public boolean update (Widget widget) {
            if (cursor == null)
                cursor = widget.getCursor ();
            if (toolTipText == null)
                toolTipText = widget.getToolTipText ();
            if (accessibleContext == null)
                accessibleContext = widget.getAccessibleContext ();
            return cursor == null  ||  toolTipText == null  ||  accessibleContext == null;
        }

        public void commit (SceneComponent component) {
            component.setToolTipText (toolTipText);
            component.setCursor (cursor);
            component.setAccessibleContext (accessibleContext);
        }

    }

}
