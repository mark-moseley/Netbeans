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

package org.netbeans.swing.tabcontrol;

import java.awt.FocusTraversalPolicy;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A tabbed container similar to a JTabbedPane.  The tabbed container is a
 * simple container which contains two components - the tabs displayer, and the
 * content displayer.  The tabs displayer is the thing that actually draws the
 * tabs; the content displayer contains the components that are being shown.
 * <p>
 * The first difference from a JTabbedPane is that it is entirely model driven -
 * the tab contents are in a data model owned by the displayer.  It is not
 * strictly necessary for the contained components to even be installed in the
 * AWT hierarchy when not displayed.
 * <p>
 * Other differences are more flexibility in the way tabs are displayed by
 * completely separating the implementation and UI for that from that of
 * displaying the contents.
 * <p>
 * Other interesting aspects are the ability of TabDataModel to deliver complex,
 * granular events in a single pass with no information loss.  Generally, great
 * effort has been gone to to conflate nothing - that is, adding a component
 * does not equal selecting it does not equal changing focus, et. cetera,
 * leaving these decisions more in the hands of the user of the control.
 * <p>
 * It is possible to implement a subclass which provides the API of JTabbedPane,
 * making it a drop-in replacement.
 * <p>
 * There are several UI styles a <code>TabbedContainer</code> can have.  The type
 * is passed as an argument to the constructor (support for changing these on the
 * fly may be added in the future, but such a change is a very heavyweight operation,
 * and is only desirable to enable use of this component in its various permutations
 * inside GUI designers).  The following styles are supported:
 * <ul>
 * <li><b>TYPE_VIEW</b> - These are tabs such as the Explorer window has in NetBeans -
 * all tabs are always displayed, with the available space equally divided between them.</li>
 * <li><b>TYPE_EDITOR</b> - Scrolling tabs, coupled with control buttons and mouse wheel
 * support for scrolling the visible tabs, and a popup which displays a list of tabs.</li>
 * <li><b>TYPE_SLIDING</b> - Tabs which are displayed as buttons, and may provide a
 * fade or sliding effect when displayed.  For this style, a second click on the selected
 * tab will hide the selected tab (setting the selection model's selected index to -1).</li></ul>
 * <p>
 * <h4>Customizing the appearance of tabs</h4>
 * Tabs are customized by providing a different UI delegate for the tab displayer component,
 * via UIManager, in the same manner as any standard Swing component; for <code>TYPE_SLIDING</code>
 * tabs, simply implementing an alternate UI delegate for the buttons used to represent tabs
 * is all that is needed.
 *
 * <h4>Managing user events on tabs</h4>
 * When a user clicks a tab, the TabbedContainer will fire an action event to all of its listeners.
 * This action event will always be an instance of <code>TabActionEvent</code>, which can provide
 * the index of the tab that was pressed, and the command name of the action that was performed.
 * A client which wants to handle the event itself (for example, the asking a user if they want
 * to save data, and possibly vetoing the closing of a tab) may veto (or take full responsibility
 * for performing) the action by consuming the TabActionEvent.
 *
 *<h4>Indication of focus and the &quot;activated&quot; state</h4>
 * The property <code>active</code> is provided to allow a tabbed container to indicate that it
 * contains the currently focused component. However, no effort is made to track focus on the
 * part of the tabbed control - there is too much variability possible (for example, if
 * a component inside a tab opens a modal dialog, is the tab active or not?).  In fact, using
 * keyboard focus at all to manage the activated state of the component turns out to be a potent
 * source of hard-to-fix, hard-to-reproduce bugs (especially when components are being added
 * and removed, or hidden and shown or components which do not reliably produce focus events).
 * What NetBeans does to solve the problem in a reliable way is the following:
 * <ol>
 * <li>Use an AWT even listener to track mouse clicks, and when the mouse is clicked,
 *     <ul>
 *     <li>Find the ancestor that is a tabbed container (if any)</li>
 *     <li>Set the activated state appropriately on it and the previously active container</li>
 *     <li>Ensure that keyboard focus moves into that container</li>
 *     </ul>
 * <li>Block ctrl-tab style keyboard based focus traversal out of tabbed containers</li>
 * <li>Provide keyboard actions, with menu items, which will change to a different container,
 *     activating it</li>
 * </ol>
 * This may seem complicated, and it probably is overkill for a small application (as is this
 * tabbed control - it wasn't designed for a small application).  It's primary advantage is
 * that it works.
 *
 * @see TabDisplayer
 * @author Tim Boudreau, Dafe Simonek
 */
public class TabbedContainer extends JComponent {
    /**
     * UIManager key for the UI Delegate to be used by tabbed containers.
     */
    public static final String TABBED_CONTAINER_UI_CLASS_ID = "TabbedContainerUI"; //NOI18N

    /**
     * Creates a &quot;view&quot; style displayer; typically this will have a
     * fixed width and a single row of tabs which get smaller as more tabs are
     * added, as seen in NetBeans&rsquo; Explorer window.
     */
    public static final int TYPE_VIEW = 0;
    /**
     * Creates a &quot;editor&quot; style displayer; typically this uses a
     * scrolling tabs UI for the tab displayer.  This is the most scalable of the available
     * UI styles - it can handle a very large number of tabs with minimal overhead, and
     * the standard UI implementations of it use a cell-renderer model for painting.
     */
    public static final int TYPE_EDITOR = 1;
    
    /** Creates a &quot;sliding&quot; view, typically with tabs rendered as
     * buttons along the left, bottom or right edge, with no scrolling behavior for tabs.
     * Significant about this UI style is that re-clicking the selected tab will
     * cause the component displayed to be hidden.
     * <p>
     * This is the least scalable of the available UI types, and is intended primarily for
     * use with a small, fixed set of tabs.  By default, the position of the tab displayer
     * will be determined based on the proximity of the container to the edges of its
     * parent window.  This can be turned off by setting the client property
     * PROP_MANAGE_TAB_POSITION to Boolean.FALSE.
     */
    public static final int TYPE_SLIDING = 2;
    
    /**
     * Creates a Toolbar-style displayer (the style used by the NetBeans Form Editor's
     * Component Inspector and a few other places in NetBeans).
     */
    public static final int TYPE_TOOLBAR = 3;

    /**
     * Property fired when <code>setActive()</code> is called
     */
    public static final String PROP_ACTIVE = "active"; //NOI18N
    
    /** Client property applicable only to TYPE_SLIDING tabs.  If set to 
     * Boolean.FALSE, the UI will not automatically try to determine a 
     * correct position for the tab displayer.
     */
    public static final String PROP_MANAGE_TAB_POSITION = "manageTabPosition";

    /**
     * Action command indicating that the action event signifies the user
     * clicking the Close button on a tab.
     */
    public static final String COMMAND_CLOSE = "close"; //NOI18N

    /**
     * Action command indicating that the action event fired signifies the user
     * selecting a tab
     */
    public static final String COMMAND_SELECT = "select"; //NOI18N

    /** Action command indicating that a popup menu should be shown */
    public static final String COMMAND_POPUP_REQUEST = "popup"; //NOI18N

    /** Command indicating a maximize (double-click) request */
    public static final String COMMAND_MAXIMIZE = "maximize"; //NOI18N

    public static final String COMMAND_CLOSE_ALL = "closeAll"; //NOI18N

    public static final String COMMAND_CLOSE_ALL_BUT_THIS = "closeAllButThis"; //NOI18N

    public static final String COMMAND_ENABLE_AUTO_HIDE = "enableAutoHide"; //NOI18N

    public static final String COMMAND_DISABLE_AUTO_HIDE = "disableAutoHide"; //NOI18N
    
    //XXX support supressing close buttons
    
    /**
     * The data model which contains information about the tabs, such as the
     * corresponding component, the icon and the tooltip.  Currently this is
     * assigned in the constructor and cannot be modified later, though this
     * could be supported in the future (with substantial effort).
     *
     * @see TabData
     * @see TabDataModel
     */
    private TabDataModel model;

    /**
     * The type of this container, which determines what UI delegate is used for
     * the tab displayer
     */
    private final int type;

    /**
     * Holds the value of the active property, determining if the displayer
     * should be painted with the focused or unfocused colors
     */
    private boolean active = false;

    /**
     * Flag used to block the call to updateUI() from the superclass constructor
     * - at that time, none of our instance fields are set, so the UI can't yet
     * set up the tab displayer correctly
     */
    private boolean initialized = false;

    /**
     * Utility field holding list of ActionListeners.
     */
    private transient List actionListenerList;

    /**
     * Content policy in which all components contained in the data model should immediately
     * be added to the AWT hierarchy at the time they appear in the data model.
     *
     * @see #setContentPolicy
     */
    public static final int CONTENT_POLICY_ADD_ALL = 1;
    /**
     * Content policy by which components contained in the data model are added to the AWT
     * hierarchy the first time they are shown, and remain their thereafter unless removed
     * from the data model.
     *
     * @see #setContentPolicy
     */
    public static final int CONTENT_POLICY_ADD_ON_FIRST_USE = 2;
    /**
     * Content policy by which components contained in the data model are added to the AWT
     * hierarchy the when they are shown, and removed immediately when the user changes tabs.
     */
    public static final int CONTENT_POLICY_ADD_ONLY_SELECTED = 3;

    private int contentPolicy = DEFAULT_CONTENT_POLICY ;

    /** The default content policy, currently CONTENT_POLICY_ADD_ALL.  To facilitate experimentation with
     * different settings application-wide, set the system property &quot;nb.tabcontrol.contentpolicy&quot;
     * to 1, 2 or 3 for ADD_ALL, ADD_ON_FIRST_USE or ADD_ONLY_SELECTED, respectively (note other values
     * will throw an <code>Error</code>).  Do not manipulate this value at runtime, it will likely become
     * a final field in a future release.  It is a protected field only to ensure its inclusion in documentation.
     *
     * @see #setContentPolicy
     */
    protected static int DEFAULT_CONTENT_POLICY = CONTENT_POLICY_ADD_ALL;
    
    /** The component converter which will tranlate TabData's from the model into
     * components. */
    private ComponentConverter converter = null;
    
    /** Info about global positioning of this tab control 
     * or null if no global location info is needed */
    private LocationInformer locationInformer = null;


    /**
     * Create a new pane with the default model and tabs displayer
     */
    public TabbedContainer() {
        this(null, TYPE_VIEW);
    }

    /**
     * Create a new pane with asociated model and the default tabs displayer
     */
    public TabbedContainer(TabDataModel model) {
        this(model, TYPE_VIEW);
    }
    
    public TabbedContainer(int type) {
        this (null, type);
    }
    
    /**
     * Create a new pane with the specified model and displayer type
     *
     * @param model The model
     */
    public TabbedContainer(TabDataModel model, int type) {
        this (model, type, null);
    }
    

    /**
     * Create a new pane with the specified model and displayer type
     *
     * @param model The model
     */
    public TabbedContainer(TabDataModel model, int type, LocationInformer locationInformer) {
        switch (type) {
            case TYPE_VIEW:
            case TYPE_EDITOR:
            case TYPE_SLIDING:
            case TYPE_TOOLBAR:
                break;
            default :
                throw new IllegalArgumentException("Unknown UI type: " + type); //NOI18N
        }
        if (model == null) {
            model = new DefaultTabDataModel();
        }
        this.model = model;
        this.type = Boolean.getBoolean("nb.tabcontrol.alltoolbar") ? TYPE_TOOLBAR : type;
        this.locationInformer = locationInformer;
        initialized = true;
        updateUI();
        //A few borders and such will check this
        //@see org.netbeans.swing.plaf.gtk.AdaptiveMatteBorder
        putClientProperty ("viewType", new Integer(type)); //NOI18N
        setFocusCycleRoot(true);
        setFocusable(true);
        setFocusTraversalPolicy(new TCFTP());
    }

    public void updateUI() {
        if (!initialized) {
            //Block the superclass call to updateUI(), which comes before the
            //field is set to tell if we are a view tab control or an editor
            //tab control - the UI won't be able to set up the tab displayer
            //correctly until this is set.
            return;
        }
        TabbedContainerUI ui = null;
        if (UIManager.get(getUIClassID()) != null) { //Avoid a stack trace
            try {
                ui = (TabbedContainerUI) UIManager.getUI(this);
            } catch (Error e) {
                //do nothing
            }
        }
        if (ui != null) {
            setUI(ui);
        } else {
            setUI(DefaultTabbedContainerUI.createUI(this));
        }
    }
    
    /** Overridden to work around jdk bug 4924516 on jdk 1.4 */
    public void addNotify() {
        super.addNotify();
        jdk14bug4924516HackShowingEvent();
    }
    
    /** Overridden to work around jdk bug 4924516 on jdk 1.4 */
    public void removeNotify() {
        super.removeNotify();
        jdk14bug4924516HackShowingEvent();
    }
    
    private void jdk14bug4924516HackShowingEvent() {
        if (System.getProperty("java.version").indexOf("1.4") >= 0) {
            HierarchyEvent evt = new HierarchyEvent (this, 
                HierarchyEvent.SHOWING_CHANGED, this, getParent());
            getUI().jdk14bug4924516Hack(evt);
        }
    }

    /**
     * Get the type of this displayer - it is either TYPE_EDITOR or TYPE_VIEW.
     * This property is set in the constructor and is immutable
     */
    public final int getType() {
        return type;
    }

    /**
     * Returns <code>TabbedContainer.TABBED_CONTAINER_UI_CLASS_ID</code>
     */
    public String getUIClassID() {
        return TABBED_CONTAINER_UI_CLASS_ID;
    }

    /** Get the ui delegate for this component */
    public TabbedContainerUI getUI() {
        return (TabbedContainerUI) ui;
    }
    
    /**
     * Set the converter that converts user objects in the data model into
     * components to display.  If set to null (the default), the user object
     * at the selected index in the data model will be cast as an instance
     * of JComponent when searching for what to show for a given tab.  
     * <p>
     * For use cases where a single component is to be displayed for more
     * than one tab, just reconfigured when the selection changes, simply
     * supply a ComponentConverter.Fixed with the component that should be
     * used for all tabs.
     */
    public final void setComponentConverter (ComponentConverter cc) {
        ComponentConverter old = converter;
        converter = cc;
        if (old instanceof ComponentConverter.Fixed && cc instanceof ComponentConverter.Fixed) {
            List l = getModel().getTabs();
            if (!l.isEmpty()) {
                TabData[] td = (TabData[]) l.toArray (new TabData[0]);
                getModel().setTabs (new TabData[0]);
                getModel().setTabs(td);
            }
        }
    }
    
    /** Get the component converter which is used to fetch a component
     * corresponding to an element in the data model.  If the value has
     * not been set, it will use ComponentConverter.DEFAULT, which simply
     * delegates to TabData.getComponent(). 
     */
    public final ComponentConverter getComponentConverter() {
        if (converter != null) {
            return converter;
        }
        return ComponentConverter.DEFAULT;
    }

    /** Experimental property - alter the policy by which the components in
     * the model are added to the container.  This may not remain suppported.
     * If used, it should be called before populating the data model.
     */
    public final void setContentPolicy(int i) {
        switch (i) {
            case CONTENT_POLICY_ADD_ALL :
            case CONTENT_POLICY_ADD_ON_FIRST_USE :
            case CONTENT_POLICY_ADD_ONLY_SELECTED :
                break;
            default :
                throw new IllegalArgumentException ("Unknown content policy: " 
                    + i);
        }
        
        if (i != contentPolicy) {
            int old = contentPolicy;
            contentPolicy = i;
            firePropertyChange ("contentPolicy", old, i); //NOI18N
        }
    }

    /** Determine the policy by which components are added to the container.
     * There are various pros and cons to each:
     * <ul>
     * <li>CONTENT_POLICY_ADD_ALL - All components in the data model are
     * automatically added to the container, and whenever the model changes,
     * components are added and removed as need be.  This is less scalable,
     * but absolutely reliable</li>
     * <li>CONTENT_POLICY_ADD_ON_FIRST_USE - Components are not added to the
     * container until the first time they are used, and then they remain in
     * the AWT hierarchy until their TabData elements are removed from the 
     * model.  This is more scalable, and probably has some startup time 
     * benefits</li>
     * <li>CONTENT_POLICY_ADD_ONLY_SELECTED - The only component that will
     * ever be in the AWT hierarchy is the one that is being displayed.  This
     * is safest in the case that heavyweight AWT components may be used </li>
     * </ul>
     */
    public int getContentPolicy() {
        return contentPolicy;
    }
    
    public boolean isValidateRoot() {
        return true;
    }

    public boolean isPaintingOrigin() {
        return true;
    }
    

    public void setToolTipTextAt(int index, String toolTip) {
        //Do this quietly - no notification is needed
        TabData tabData = getModel().getTab(index);
        if (tabData != null) {
            tabData.tip = toolTip;
        }
    }

    /**
     * Get the data model that represents the tabs this component has.  All
     * programmatic manipulation of tabs should be done via the data model.
     *
     * @return The model
     */
    public final TabDataModel getModel() {
        return model;
    }

    /**
     * Get the selection model.  The selection model tracks the index of the
     * selected component, modifying this index appropriately when tabs are
     * added or removed.
     *
     * @return The model
     */
    public final SingleSelectionModel getSelectionModel() {
        return getUI().getSelectionModel();
    }
    
    /**
     * Fetch the rectangle of the tab for a given index, in the coordinate space
     * of this component, by reconfiguring the passed rectangle object
     */
    public final Rectangle getTabRect(int index, final Rectangle r) {
        return getUI().getTabRect(index, r);
    }

    /** Gets the index of the tab at point p, or -1 if no tab is there */
    public int tabForCoordinate (Point p) {
        return getUI().tabForCoordinate(p);
    }

    /**
     * Set the &quot;active&quot; state of this tab control - this affects the
     * way the tabs are displayed, to indicate focus.  Note that this method
     * will <i>never</i> be called automatically in stand-alone use of
     * TabbedContainer. While one would expect a component gaining keyboard
     * focus to be a good determinant, it actually turns out to be a potent
     * source of subtle and hard-to-fix bugs.
     * <p/>
     * NetBeans uses an AWTEventListener to track mouse clicks, and allows
     * components to become activated only via a mouse click or via a keyboard
     * action or menu item which activates the component.  This approach is far
     * more robust and is the recommended usage pattern.
     */
    public final void setActive(boolean active) {
        if (active != this.active) {
            this.active = active;
            firePropertyChange(PROP_ACTIVE, !active, active);
        }
    }

    /**
     * Determine if this component thinks it is &quot;active&quot;, which
     * affects how the tabs are painted - typically used to indicate that
     * keyboard focus is somewhere within the component
     */
    public final boolean isActive() {
        return active;
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
     * Remove an action listener.
     *
     * @param listener The listener to remove.
     */
    public final synchronized void removeActionListener(
            ActionListener listener) {
        if (actionListenerList != null) {
            actionListenerList.remove(listener);
            if (actionListenerList.isEmpty()) {
                actionListenerList = null;
            }
        }
    }

    /**
     * Used by the UI to post action events for selection and close operations.
     * If the event is consumed, the UI should take no action to change the
     * selection or close the tab, and will presume that the receiver of the
     * event is handling performing whatever action is appropriate.
     *
     * @param event The event to be fired
     */
    protected final void postActionEvent(TabActionEvent event) {
        List list;
        synchronized (this) {
            if (actionListenerList == null)
                return;
            list = Collections.unmodifiableList(actionListenerList);
        }
        for (int i = 0; i < list.size(); i++) {
            ((ActionListener) list.get(i)).actionPerformed(event);
        }
    }

    public void setIconAt(int index, Icon icon) {
        getModel().setIcon(index, icon);
    }

    public void setTitleAt(int index, String title) {
        getModel().setText(index, title);
    }

    /** Create an image of a single tab, suitable for use in drag and drop operations */
    public Image createImageOfTab(int idx) {
        return getUI().createImageOfTab (idx);
    }

    /** Get the number of tabs.  Equivalent to <code>getModel().size()</code> */
    public int getTabCount() {
        return getModel().size();
    }

    /** Get the index of a component */
    public int indexOf (Component comp) {
        int max = getModel().size();
        TabDataModel mdl = getModel();
        for (int i=0; i < max; i++) {
            if (getComponentConverter().getComponent(mdl.getTab(i)) == comp) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Fetch the command that will be performed if the default (left) mouse buttton is clicked on this point.
     * This will typically be <code>COMMAND_CLOSE</code> or <code>COMMAND_SELECT</code>.  Client code can
     * use this method to determine if it is safe to, for instance, initiate a drag and drop action for
     * a mouse event - if the return value is <code>COMMAND_SELECT</code>, it is.
     * <p>
     * Alternate UIs can implement their own commands which are passed to client code via action events
     * in this way.  With the exception (XXX should it be an exception?) of COMMAND_SELECT, an action
     * event will be fired with the return value if the mouse button is actually pressed over this point.
     * The event may then either be consumed by the client, meaning that client code should handle it, or not,
     * in which case there is (presumably) some default behavior that the UI will do (like closing a tab).
     * <p>
     * Note, should we support firing events for COMMAND_SELECT in the future, the correct way to detect
     * selection changes is to listen on the selection model - no action events are fired for programmatic
     * changes to the selection model.
     *
     * @param p A point in the coordinate space of the container
     * @return A command string, typically COMMAND_CLOSE or COMMAND_SELECT, or null if not over a tab.
     *
     */
    public String getCommandAtPoint (Point p) {
        return getUI().getCommandAtPoint (p);
    }



    /** The index at which a tab should be inserted if a drop operation
     * occurs at this point.
     *
     * @param location A point anywhere on the TabbedContainer
     * @return A tab index, or -1
     */
    public int dropIndexOfPoint (Point location) {
        return getUI().dropIndexOfPoint(location);
    }

    /**
     * Get a shape appropriate for drawing on the window's glass pane to indicate
     * where a component should appear in the tab order if it is dropped here.
     *
     * @param dragged An object being dragged, or null. The object may be an instance
     *        of <code>TabData</code> or <code>Component</code>, in which case a check
     *        will be done of whether the dragged object is already in the data model,
     *        so that attempts to drop the object over the place it already is in the
     *        model will always return the exact indication of that tab's position.
     *
     * @param location A point
     * @return
     */
    public Shape getDropIndication(Object dragged, Point location) {
        int ix;
        if (dragged instanceof Component) {
            ix = indexOf((Component)dragged);
        } else if (dragged instanceof TabData) {
            ix = getModel().indexOf((TabData) dragged);
        } else {
            ix = -1;
        }

        int over = dropIndexOfPoint(location);

        if(over < 0) { // XXX PENDING The tab is not found, later simulate the last one.
            Rectangle r = getBounds();
            r.setLocation(0, 0);
            return r;
        }
        /*
        if (over == ix || (over == ix + 1 && ix != -1 && over < getModel().size())) { //+1 - dropping on the next tab will put it in the same place
            return getUI().getExactTabIndication(over);
        } else {
            return getUI().getInsertTabIndication(over);
        }
        */
        if (over == ix && ix != -1) {
            return getUI().getExactTabIndication(over);
        } else {
            return getUI().getInsertTabIndication(over);
        }
    }
    
    public LocationInformer getLocationInformer() {
        return locationInformer;
    }
    

    static {
        //Support for experimenting with different content policies in NetBeans
        String s = System.getProperty("nb.tabcontrol.contentpolicy"); //NOI18N
        if (s != null) {
            try {
                DEFAULT_CONTENT_POLICY = Integer.parseInt (s);
                switch (DEFAULT_CONTENT_POLICY) {
                    case CONTENT_POLICY_ADD_ALL :
                    case CONTENT_POLICY_ADD_ON_FIRST_USE :
                    case CONTENT_POLICY_ADD_ONLY_SELECTED :
                        System.err.println("Using custom content policy: " + DEFAULT_CONTENT_POLICY);
                        break;
                    default :
                        throw new Error ("Bad value for default content " +
                                "policy: " + s + " only values 1, 2 or 3" +
                                "are meaningful"); //NOI18N
                }
                System.err.println ("Default content policy is " + DEFAULT_CONTENT_POLICY);
            } catch (Exception e) {
                System.err.println ("Error parsing default content " +
                    "policy: \"" + s + "\""); //NOI18N
            }
        }
    }
    
    /**
     * simple traversal policy..
     */
    private final class TCFTP extends FocusTraversalPolicy {
        private Component getSel() {
            if (getModel().size() == 0 || getSelectionModel().getSelectedIndex() == -1) {
                return null;
            }
            Component sel = getComponentConverter().getComponent(getModel().getTab(getSelectionModel().getSelectedIndex()));
            if (sel != null) {
                return sel;
            }
            return null;
        }
        
        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            return getSel();
        }

        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            return getSel();
        }

        public Component getDefaultComponent(Container focusCycleRoot) {
            return getSel();
        }

        public Component getFirstComponent(Container focusCycleRoot) {
            return getSel();
        }

        public Component getLastComponent(Container focusCycleRoot) {
            return getSel();
        }
    }

}
