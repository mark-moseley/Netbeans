/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol;

import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.WinClassicViewTabDisplayerUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A Component which displays tabs supplied by a TabDataModel.  This is 
 * essentially the upper (or lower) portion of a tabbed pane, without the
 * part that displays components.  It can be used to provide tab-like
 * selection over a data model containing anything, not just components.
 * <p>
 * It has a three display modes (more fully described in the overview for
 * <a href="TabbedContainer.html">TabbedContainer</a>), to provide different
 * styles of tab display, such as scrolling tabs and others.
 * <p>
 * TabDisplayer is completely model driven - the class itself is little more
 * than an aggregation point for a data model, a selection model, and so forth.
 * The logic that allows it to operate is implemented in the UI delegates,
 * which are installed by (and can be replaced via) the standard Swing 
 * UIManager mechanisms.
 * <p>
 * Some TabDisplayer UI's support an <i>orientation</i> property, which is provided
 * via the client property <code>PROP_ORIENTATION</code>.
 *
 * @author Tim Boudreau
 */
public final class TabDisplayer extends JComponent {
    
    private boolean initialized = false;
    private TabDataModel model;
    private SingleSelectionModel sel = null;
    private boolean active;
    private final int type;

    /**
     * Displayer type for view tabs, which do not scroll and simply divide the
     * available space between themselves.  The value of this field is mapped to
     * TabbedContainer.TYPE_VIEW
     */
    public static final int TYPE_VIEW = TabbedContainer.TYPE_VIEW;
    /**
     * Displayer type for editor tabs, which scroll (typically - depends on what
     * the UI does).  The value of this field is mapped to
     * TabbedContainer.TYPE_EDITOR
     */
    public static final int TYPE_EDITOR = TabbedContainer.TYPE_EDITOR;
    
    public static final int TYPE_SLIDING = TabbedContainer.TYPE_SLIDING;
    
    /**
     * Property indicating the tab displayer should be painted as
     * &quot;active&quot;. This is typically used to indicate keyboard focus.
     * The valud of this field is mapped to TabbedContainer.PROP_ACTIVE
     */
    public static final String PROP_ACTIVE = TabbedContainer.PROP_ACTIVE;


    /**
     * Action command indicating that the action event signifies the user
     * clicking the Close button on a tab.
     */
    public static final String COMMAND_CLOSE = TabbedContainer.COMMAND_CLOSE;

    /**
     * Action command indicating that the action event fired signifies the user
     * selecting a tab
     */
    public static final String COMMAND_SELECT = TabbedContainer.COMMAND_SELECT;

    /**
     * Action command indicating that the action event fired signifies the user
     * requesting a popup menu over a tab
     */
    public static final String COMMAND_POPUP_REQUEST = TabbedContainer.COMMAND_POPUP_REQUEST;

    /**
     * Action command indicating that the action event fired signifies the user
     * has double clicked a tab
     */
    public static final String COMMAND_MAXIMIZE = TabbedContainer.COMMAND_MAXIMIZE;

    /**
     * Action command indicating that the action event fired signifies the user
     * has shift-clicked the close button on a tab
     */
    public static final String COMMAND_CLOSE_ALL = TabbedContainer.COMMAND_CLOSE_ALL; //NOI18N

    /**
     * Action command indicating that the action event fired signifies the user
     * has alt-clicked the close button on a tab
     */
    public static final String COMMAND_CLOSE_ALL_BUT_THIS = TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS; //NOI18N

    /**
     * Action command indicating that the action event signifies the user
     * clicking the Pin button on a tab.
     */
    public static final String COMMAND_ENABLE_AUTO_HIDE = TabbedContainer.COMMAND_ENABLE_AUTO_HIDE; //NOI18N

    /**
     * Action command indicating that the action event signifies the user
     * clicking the Pin button on a tab.
     */
    public static final String COMMAND_DISABLE_AUTO_HIDE = TabbedContainer.COMMAND_DISABLE_AUTO_HIDE; //NOI18N

    /**
     * UIManager key for the UI Delegate to be used for &quot;editor&quot; style TabbedContainers
     */
    public static final String EDITOR_TAB_DISPLAYER_UI_CLASS_ID = "EditorTabDisplayerUI"; //NOI18N

    /**
     * UIManager key for the UI Delegate to be used for &quot;view&quot; style TabbedContainers
     */
    public static final String VIEW_TAB_DISPLAYER_UI_CLASS_ID = "ViewTabDisplayerUI"; //NOI18N
    
    /**
     * UIManager key for the UI delegate to be used in &quot;sliding&quot; style
     * containers */
    public static final String SLIDING_TAB_DISPLAYER_UI_CLASS_ID = "SlidingTabDisplayerUI"; //NOI18N
    
    /** Client property to indicate the orientation, which determines what
     * side the tabs are displayed on.  Currently this is only honored by
     * the sliding tabs ui delegate. */
    public static final String PROP_ORIENTATION = "orientation"; //NOI18N

    /** Client property value to display tabs on the left side of the control.
     */
    public static final Object ORIENTATION_EAST = "east"; //NOI18N
    /** Client property value to display tabs on the right side of the control 
     */
    public static final Object ORIENTATION_WEST = "west"; //NOI18N
    /** Client property value to display tabs on the top edge of the control 
     */
    public static final Object ORIENTATION_NORTH = "north"; //NOI18N
    /** Client property value to display tabs on the bottom edge of the control 
     */
    public static final Object ORIENTATION_SOUTH = "south"; //NOI18N
    /** Client property value for pin button to have neutral orientation 
     */
    public static final Object ORIENTATION_CENTER = "center"; //NOI18N
    
    /**
     * Utility field holding list of ActionListeners.
     */
    private transient List actionListenerList;
    
    /** Info about global positioning of this tab control 
     * or null if no global location info is needed */
    private LocationInformer locationInformer = null;

    
    public TabDisplayer () {
        this (new DefaultTabDataModel(), TYPE_VIEW);
    }
    
    /**
     * Creates a new instance of TabDisplayer
     */
    public TabDisplayer(TabDataModel model, int type) {
        this (model, type, null);
    }
    
    /**
     * Creates a new instance of TabDisplayer
     */
    public TabDisplayer(TabDataModel model, int type, LocationInformer locationInformer) {
        switch (type) {
            case TYPE_VIEW:
            case TYPE_EDITOR:
            case TYPE_SLIDING:
                break;
            default :
                throw new IllegalArgumentException("Unknown UI type: " + type); //NOI18N
        }
        this.model = model;
        this.type = type;
        this.locationInformer = locationInformer;
        putClientProperty (PROP_ORIENTATION, ORIENTATION_NORTH);
        initialized = true;
        updateUI();
        setFocusable(false);
    }

    public final TabDisplayerUI getUI() {
        return (TabDisplayerUI) ui;
    }

    /** Overridden to block the call from the superclass constructor, which
     * comes before the <code>type</code> property is initialized.  Provides
     * a reasonable fallback UI for use on unknown look and feels.
     */
    public final void updateUI() {
        if (!initialized) {
            return;
        }
        
        ComponentUI ui = null;
        try {
            ui = UIManager.getUI(this);
        } catch (Error error) {
            System.err.println("Could not load a UI for " + getUIClassID() + 
                " - missing class?");
        }
        if (ui == null) {
            ui = getType() == TYPE_VIEW ?
                    WinClassicViewTabDisplayerUI.createUI(this) :
                    WinClassicEditorTabDisplayerUI.createUI(this);
        }
        setUI((TabDisplayerUI) ui);
    }

    /** Returns an different UIClassID depending on the value of the <code>type</code>
     * property. */
    public String getUIClassID() {
        switch (getType()) {
            case TYPE_VIEW : return VIEW_TAB_DISPLAYER_UI_CLASS_ID;
            case TYPE_EDITOR : return EDITOR_TAB_DISPLAYER_UI_CLASS_ID;
            case TYPE_SLIDING : return SLIDING_TAB_DISPLAYER_UI_CLASS_ID;
            default :
                throw new IllegalArgumentException ("Unknown UI type: " + 
                    getType());
        }
    }

    /**
     * Returns whether this control uses the view tab look or the scrolling
     * editor tab look.  This is set in the constructor.
     */
    public final int getType() {
        return type;
    }

    public final Dimension getPreferredSize() {
        return getUI().getPreferredSize(this);
    }

    public final Dimension getMinimumSize() {
        return getUI().getMinimumSize(this);
    }

    /**
     * Accessor only for TabDisplayerUI when installing the UI
     */
    void setSelectionModel(SingleSelectionModel sel) {
        this.sel = sel;
    }

    /** Get the selection model, which determines which tab is selected.
     * To change the selection, get the selection model and call 
     * setSelectedIndex(). */
    public SingleSelectionModel getSelectionModel() {
        return sel;
    }

    /** Get the data model that defines the contents which are displayed */
    public final TabDataModel getModel() {
        return model;
    }

    /** Set the active state of the component */
    public final void setActive(boolean active) {
        if (active != this.active) {
            this.active = active;
            firePropertyChange(PROP_ACTIVE, !active, active); //NOI18N
        }
    }

    /** Gets the &quot;active&quot; state of this component.  If the component
     * is active, most UIs will paint the selected tab differently to indicate
     * that focus is somewhere in the container */
    public final boolean isActive() {
        return active;
    }

    /**
     * Gets tooltip for the tab corresponding to the mouse event, or if no
     * tab, delegates to the default implementation.
     */
    public final String getToolTipText(MouseEvent event) {
        if (ui != null) {
            Point p = event.getPoint();
            if (event.getSource() != this) {
                Component c = (Component) event.getSource();
                p = SwingUtilities.convertPoint(c, p, this);
            }
            int index = getUI().tabForCoordinate(p);
            if (index != -1) {
                return getModel().getTab(index).tip;
            }
        }
        return super.getToolTipText(event);
    }

    /** Make a tab visible.  In the case of scrolling UIs, a tab is not
     * always visible.  This call will make it scroll into view */
    public final void makeTabVisible(int index) { //XXX needed?
        getUI().makeTabVisible(index);
    }

    /** Get the rectangle that a given tab occupies */
    public final Rectangle getTabRect(int tab, Rectangle dest) {
        if (dest == null) {
            dest = new Rectangle();
        }
        getUI().getTabRect(tab, dest);
        return dest;
    }

    public final Image getDragImage(int index) {
        return getUI().createImageOfTab(index);
    }

    /**
     * Register an ActionListener.  TabbedContainer and TabDisplayer guarantee
     * that the type of event fired will always be TabActionEvent.  There are
     * two special things about TabActionEvent: <ol> <li>There are methods on
     * TabActionEvent to find the index of the tab the event was performed on,
     * and if present, retrieve the mouse event that triggered it, for clients
     * that wish to provide different handling for different mouse buttons</li>
     * <li>TabActionEvents can be consumed.  If a listener consumes the event,
     * the UI will take no action - the selection will not be changed, the tab
     * will not be closed.  Consuming the event means taking responsibility for
     * doing whatever would normally happen automatically.  This is useful for,
     * for example, showing a dialog and possibly aborting closing a tab if it
     * contains unsaved data, for instance.</li> </ol> Action events will be
     * fired <strong>before</strong> any action has been taken to alter the
     * state of the control to match the action, so that they may be vetoed or
     * modified by consuming the event.
     *
     * @param listener The listener to register.
     */
    public final synchronized void addActionListener(ActionListener listener) {
        if (actionListenerList == null) {
            actionListenerList = new ArrayList();
        }
        actionListenerList.add(listener);
    }

    /**
     * Removes ActionListener from the list of listeners.
     *
     * @param listener The listener to remove.
     */
    public final synchronized void removeActionListener(ActionListener listener) {
        if (actionListenerList != null) {
            actionListenerList.remove(listener);
        }
    }

    public String getCommandAtPoint(Point p) {
        return getUI().getCommandAtPoint (p);
    }
    
    public void registerShortcuts(JComponent comp) {
        getUI().registerShortcuts(comp);
    }
    
    public void unregisterShortcuts(JComponent comp) {
        getUI().unregisterShortcuts(comp);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    protected final void postActionEvent(TabActionEvent event) {
        List list;
        synchronized (this) {
            if (actionListenerList == null) {
                return;
            }
            list = Collections.unmodifiableList(actionListenerList);
        }
        for (int i = 0; i < list.size(); i++) {
            ((ActionListener) list.get(i)).actionPerformed(event);
        }
    }

    public int tabForCoordinate(Point p) {
        return getUI().tabForCoordinate(p);
    }
    
    public LocationInformer getLocationInformer() {
        return locationInformer;
    }
    
}
