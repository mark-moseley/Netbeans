/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TabbedContainerUI.java
 *
 * Created on March 14, 2004, 3:25 PM
 */

package org.netbeans.swing.tabcontrol;

import org.netbeans.swing.tabcontrol.event.TabActionEvent;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;

/**
 * Basic UI for tabbed containers.  Note this is distinct from the UI for the
 * embedded tab displayer component - that where all the interesting painting
 * logic is.
 * <p>
 * To provide an implementation of TabbedContainerUI, it is a far better idea to
 * subclass 
 * <a href="plaf/AbstractTabbedContainerUI.html">AbstractTabbedContainerUI</a>, 
 * <a href="plaf/BasicTabbedContainerUI.html">BasicTabbedContainerUI</a>, or 
 * <a href="plaf/BasicScrollingTabbedContainerUI.html">BasicScrollingTabbedContainerUI</a>.
 *
 * @author Tim Boudreau
 */
public abstract class TabbedContainerUI extends ComponentUI {
    /** The TabbedContainer this instance is acting as a ui delegate for.
     * <strong>do not alter the value in this field. </strong>
     */
    protected TabbedContainer container = null;

    /**
     * Creates a new instance of TabbedContainerUI
     */
    public TabbedContainerUI(TabbedContainer container) {
        this.container = container;
    }

    public void installUI(JComponent c) {
        assert c == container;
    }
    
    /**
     * This method is called if TabbedContainer.updateUI() gets called after
     * a UI delegate has been installed (in other words, the user did something
     * like switch look and feels or switch the Windows desktop theme).
     * <p>
     * Normally, the only UI delegate that exists for TabbedContainer is
     * DefaultTabbedContainerUI, so it makes no sense to replace one with
     * another, since they do the same thing.
     * <p>
     * However, this method can be used to update the tab displayer component's
     * UI.  Subclasses are expected to override this method to call 
     * updateUI() on the displayer, or do whatever is appropriate to ensure that
     * the UI will look right after the change - or to return true from this
     * method, in which the entire UI delegate for the tabbed container will
     * be replaced.
     * @return false
     */
    protected boolean uichange() {
        return false;
    }
    
    /**
     * Accessor method for TabbedContainer
     * @see uichange
     */
    final boolean shouldReplaceUI() {
        return uichange();
    }


    /** Get the bounds of a tab.  Note that for non-rectangular tabs
     * this may not correspond exactly to the area in which it will
     * respond to mouse clicks.
     *
     * @param tab A tab index
     * @param r A rectangle to configure with the information, or null
     * @return The passed rectangle, or a new one if null was passed
     */
    public abstract Rectangle getTabRect(int tab, Rectangle r);

    /** Get the tab at a given point in the coordinate space of the
     * container.
     *
     * @param p A point
     * @return The tab index at this point, or -1 if no tab
     */
    public abstract int tabForCoordinate (Point p);

    /** Make a tab visible.  No-op except in the case of scrolling tabs,
     * in which case the tab may be scrolled offscreen.
     *
     * @param index A tab index
     */
    public abstract void makeTabVisible (int index);

   /**
     * Allows ActionListeners attached to the container to determine if the
     * event should be acted on. Delegates to <code>displayer.postActionEvent()</code>.
     * This method will create a TabActionEvent with the passed string as an
     * action command, and cause the displayer to fire this event.  It will
     * return true if no listener on the displayer consumed the TabActionEvent;
     * consuming the event is the way a listener can veto a change, or provide
     * special handling for it.
     *
     * @param command The action command - this should be TabDisplayer.COMMAND_SELECT
     *                or TabDisplayer.COMMAND_CLOSE, but private contracts
     *                between custom UIs and components are also an option.
     * @param tab     The index of the tab upon which the action should act, or
     *                -1 if non-applicable
     * @param event   A mouse event which initiated the action, or null
     * @return true if the event posted was not consumed by any listener
     */
    protected final boolean shouldPerformAction(String command, int tab,
                                                MouseEvent event) {
        TabActionEvent evt = new TabActionEvent(container, command, tab, event);
        container.postActionEvent(evt);
        return !evt.isConsumed();
    }

    /** Get the action associated with a given point if clicked with the default mouse button.
     * This is used, for example, to not start a drag and drop operation if the user pressed
     * the mouse in a close button and is dragging the mouse away to abort closing.
     *
     * @param p A point in the coordinate space of the tabbed container
     * @return A string naming the action (constants defined in TabbedContainer/TabDisplayer), or null
     */
    public abstract String getCommandAtPoint (Point p);

    /**
     * Get the selection model that tracks and determines which tab is selected.
     *
     * @return The selection model (in the default implementation, this is the selection model of the
     *         embedded tab displayer)
     */
    public abstract SingleSelectionModel getSelectionModel();

    /** Create an image suitable for use in drag and drop operations, of a tab */
    public abstract Image createImageOfTab(int idx);

    /** Get a polygon matching the shape of the tab */
    public abstract Polygon getExactTabIndication (int idx);

    /** Get a polygon indicating the insertion of a tab before the passed
     * index, unless the index is equal to the model size, in which case
     * it will return an indication for inserting a tab at the end.
     *
     * @param idx A tab index
     * @return A shape representing the shape of the tab as it is displayed onscreen,
     *         in the coordinate space of the displayer
     */
    public abstract Polygon getInsertTabIndication (int idx);

    /** Get a rectangle matching the area in which content is displayed */
    public abstract Rectangle getContentArea ();

    /** Get a rectangle matching the area in which tabs are displayed */
    public abstract Rectangle getTabsArea ();
    
    /**
     * JDK bug 4924516 - on JDK 1.4, in some cases HierarchyEvents for 
     * components being shown are not propagated correctly.  On 1.4, this
     * method will be called, passing in a hierarchy event indicating the
     * component has been shown.
     * <p>
     * The default implementation does nothing; subclasses which use
     * HierarchyListeners should override it to pass the event to the 
     * appropriate listener, which should test if it has already received
     * an event in the ordinary way and only do something if necessary.
     */
    protected void jdk14bug4924516Hack(HierarchyEvent evt) {
        //do nothing
    }

    /**
     * Index at which a tab would be inserted if a suitable object were dropped
     * at this point.
     *
     * @param p A point
     * @return A tab index which may be equal to the size of the model (past the
     *         last tab index) if the tab should be inserted at the end.
     */
    public abstract int dropIndexOfPoint(Point p);
    
    public abstract void setShowCloseButton (boolean val);
    
    public abstract boolean isShowCloseButton ();

}
