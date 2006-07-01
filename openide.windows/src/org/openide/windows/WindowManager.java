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

package org.openide.windows;

import java.awt.Frame;
import java.awt.Image;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Manages window system.
 * Allows the work with window system components, i.e. <code>Mode</code>s, <code>TopComponentGroup</code>s
 * and provides handling of operations provided over <code>TopComponent</code>s.
 * <p><p>
 * <b><font color="red"><em>Important note: Do not provide implementation of this abstract class unless you are window system provider!</em></font></b>
 *
 * @author Jaroslav Tulach
 */
public abstract class WindowManager extends Object implements Serializable {
    /** property change of workspaces.
     * @deprecated Do not use. Workspaces are not supported anymore. */
    public static final String PROP_WORKSPACES = "workspaces"; // NOI18N

    /** property change of current workspace.
     * @deprecated Do not use. Workspaces are not supported anymore.
     */
    public static final String PROP_CURRENT_WORKSPACE = "currentWorkspace"; // NOI18N

    /** Name of property for modes in the workspace.
     * @since 4.13 */
    public static final String PROP_MODES = "modes"; // NOI18N

    /** Instance of dummy window manager. */
    private static WindowManager dummyInstance;
    static final long serialVersionUID = -4133918059009277602L;

    /** The top component which is currently active */
    private TopComponent activeComponent;

    /** the registry */
    private TopComponent.Registry registry;

    /** Singleton instance accessor method for window manager. Provides entry
     * point for further work with window system API of the system.
     *
     * @return instance of window manager installed in the system
     * @since 2.10
     */
    public static final WindowManager getDefault() {
        WindowManager wmInstance = (WindowManager) Lookup.getDefault().lookup(WindowManager.class);

        return (wmInstance != null) ? wmInstance : getDummyInstance();
    }

    private static synchronized WindowManager getDummyInstance() {
        if (dummyInstance == null) {
            dummyInstance = new DummyWindowManager();
        }

        return dummyInstance;
    }

    /** Finds mode where specified name.
     * @return <code>Mode</code> whith the specified name is or <code>null</code>
     *          if there does not exist such <code>Mode</code> inside window system.
     * @since 4.13 */
    public abstract Mode findMode(String name);

    /** Finds mode which contains specified <code>TopComponent</code>.
     * @return <code>Mode</code> which contains specified <code>TopComponent</code> or <code>null</code>
     *          if the <code>TopComponent</code> is not added into any <code>Mode</code> inside window system.
     * @since 4.13 */
    public abstract Mode findMode(TopComponent tc);

    /** Gets set of all <code>Mode</code>S added into window system.
     * @since 4.13 */
    public abstract Set getModes();

    /**
     * Gets the NetBeans Main Window.
    * This should ONLY be used for:
    * <UL>
    *   <LI>using the Main Window as the parent for dialogs</LI>
    *   <LI>using the Main Window's position for preplacement of windows</LI>
    * </UL>
    * @return the Main Window
    */
    public abstract Frame getMainWindow();

    /** Called after a Look&amp;Feel change to update the NetBeans UI.
    * Should call {@link javax.swing.JComponent#updateUI} on all opened windows.
    */
    public abstract void updateUI();

    /** Create a component manager for the given top component.
    * @param c the component
    * @return the manager to handle opening, closing and selecting the component
    */
    protected abstract WindowManager.Component createTopComponentManager(TopComponent c);

    /** Access method for registry of all components in the system.
    * @return the registry
    */
    protected TopComponent.Registry componentRegistry() {
        return (TopComponent.Registry) org.openide.util.Lookup.getDefault().lookup(TopComponent.Registry.class);
    }

    /** Getter for component registry.
    * @return the registry
    */
    public synchronized TopComponent.Registry getRegistry() {
        if (registry != null) {
            return registry;
        }

        registry = componentRegistry();

        return registry;
    }

    /** Creates new workspace.
     * @param name the name of the workspace
     * @return new workspace
     * @deprecated Do not use. Workspaces are not supported anymore. */
    public final Workspace createWorkspace(String name) {
        return createWorkspace(name, name);
    }

    /** Creates new workspace with I18N support.
     * Note that it will not be displayed until {@link #setWorkspaces} is called
     * with an array containing the new workspace.
     * @param name the code name (used for internal purposes)
     * @param displayName the display name
     * @return the new workspace
     * @deprecated Do not use. Workspaces are not supported anymore. */
    public abstract Workspace createWorkspace(String name, String displayName);

    /** Finds workspace given its name.
     * @param name the (code) name of workspace to find
     * @return workspace or null if not found
     * @deprecated Do not use. Workspaces are not supported anymore. */
    public abstract Workspace findWorkspace(String name);

    /**
     * Gets a list of all workspaces.
     * @return an array of all known workspaces
     * @deprecated Do not use. Workspaces are not supported anymore. */
    public abstract Workspace[] getWorkspaces();

    /** Sets new array of workspaces.
     * In conjunction with {@link #getWorkspaces}, this may be used to reorder
     * workspaces, or add or remove workspaces.
     * @param workspaces An array consisting of new workspaces.
     * @deprecated Do not use. Workspaces are not supported anymore. */
    public abstract void setWorkspaces(Workspace[] workspaces);

    /**
     * Gets the current workspace.
     * @return the currently active workspace
     * @see Workspace#activate
     * @deprecated Do not use. Workspaces are not supported anymore. */
    public abstract Workspace getCurrentWorkspace();

    /** Finds <code>TopComponentGroup</code> of given name.
     * @return instance of TopComponetnGroup or null
     * @since 4.13 */
    public abstract TopComponentGroup findTopComponentGroup(String name);

    //
    // You can add implementation to this class (+firePropertyChange), or implement it in subclass
    // Do as you want.
    //

    /**
     * Attaches a listener for changes in workspaces.
     * @param l the new listener
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes a listener for changes in workspaces.
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener l);

    /** Finds top component manager for given top component.
     * @param tc top component to find manager for.
     * @return component manager for given top component.
     * @deprecated Do not use anymore.
     * See {@link WindowManager.Component} deprecation.
     */
    protected static final Component findComponentManager(TopComponent tc) {
        return null;
    }

    /** Activate a component. The top component containers should inform
    * the top component that it is active via a call to this method through
    * derived window manager implementation.
    * @param tc the top component to activate;
    * or <code>null</code> to deactivate all top components
    */
    protected void activateComponent(TopComponent tc) {
        // check
        if (activeComponent == tc) {
            return;
        }

        // deactivate old if possible
        if (activeComponent != null) {
            try {
                activeComponent.componentDeactivated();
            } catch (RuntimeException re) {
                IllegalStateException ise = new IllegalStateException(
                        "[Winsys] TopComponent " + activeComponent // NOI18N
                         +" throws runtime exception from its componentDeactivated() method. Repair it!"
                    ); // NOI18N
                ise.initCause(re);
                Logger.global.log(Level.WARNING, null, ise);
            }
        }

        activeComponent = tc;

        if (activeComponent != null) {
            try {
                activeComponent.componentActivated();
            } catch (RuntimeException re) {
                IllegalStateException ise = new IllegalStateException(
                        "[Winsys] TopComponent " + activeComponent // NOI18N
                         +" throws runtime exception from its componentActivated() method. Repair it!"
                    ); // NOI18N
                ise.initCause(re);
                Logger.global.log(Level.WARNING, null, ise);
            }
        }
    }

    /** Notifies component that it was opened (and wasn't opened on any
     * workspace before). Top component manager that implements Component
     * inner interface of this class should send open notifications via
     * calling this method
     * @param tc the top component to be notified
     */
    protected void componentOpenNotify(TopComponent tc) {
        try {
            tc.componentOpened();
        } catch (RuntimeException re) {
            IllegalStateException ise = new IllegalStateException(
                    "[Winsys] TopComponent " + tc // NOI18N
                     +" throws runtime exception from its componentOpened() method. Repair it!"
                ); // NOI18N
            ise.initCause(re);
            Logger.global.log(Level.WARNING, null, ise);
        }
    }

    /** Notifies component that it was closed (and is not opened on any
     * workspace anymore). Top component manager that implements Component
     * inner interface of this class should send close notifications via
     * calling this method
     * @param tc the top component to be notified
     */
    protected void componentCloseNotify(TopComponent tc) {
        try {
            tc.componentClosed();
        } catch (RuntimeException re) {
            IllegalStateException ise = new IllegalStateException(
                    "[Winsys] TopComponent " + tc // NOI18N
                     +" throws runtime exception from its componentClosed() method. Repair it!"
                ); // NOI18N
            ise.initCause(re);
            Logger.global.log(Level.WARNING, null, ise);
        }

        if (tc == activeComponent) {
            activateComponent(null);
        }
    }

    /** Notifies <code>TopComponent</code> it is about to be shown.
     * @param tc <code>TopComponent</code> to be notified
     * @see TopComponent#componentShowing
     * @since 2.18 */
    protected void componentShowing(TopComponent tc) {
        try {
            tc.componentShowing();
        } catch (RuntimeException re) {
            IllegalStateException ise = new IllegalStateException(
                    "[Winsys] TopComponent " + tc // NOI18N
                     +" throws runtime exception from its componentShowing() method. Repair it!"
                ); // NOI18N
            ise.initCause(re);
            Logger.global.log(Level.WARNING, null, ise);
        }
    }

    /** Notifies <code>TopComponent</code> it was hidden.
     * @param tc <code>TopComponent</code> to be notified
     * @see TopComponent#componentHidden
     * @since 2.18 */
    protected void componentHidden(TopComponent tc) {
        try {
            tc.componentHidden();
        } catch (RuntimeException re) {
            IllegalStateException ise = new IllegalStateException(
                    "[Winsys] TopComponent " + tc // NOI18N
                     +" throws runtime exception from its componentHidden() method. Repair it!"
                ); // NOI18N
            ise.initCause(re);
            Logger.global.log(Level.WARNING, null, ise);
        }
    }

    /** Provides opening of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> to open
     * @since 4.13 */
    protected abstract void topComponentOpen(TopComponent tc);

    /** Provides closing of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> to close
     * @since 4.13 */
    protected abstract void topComponentClose(TopComponent tc);

    /** Provides activation of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> to activate
     * @since 4.13 */
    protected abstract void topComponentRequestActive(TopComponent tc);

    /** Provides selection of specfied <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> to set visible (select)
     * @since 4.13 */
    protected abstract void topComponentRequestVisible(TopComponent tc);

    /** Informs about change of display name of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which display name has changed
     * @param displayName newly changed display name value
     * @since 4.13 */
    protected abstract void topComponentDisplayNameChanged(TopComponent tc, String displayName);
    
    /** Informs about change of html display name of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which display name has changed
     * @param displayName newly changed html display name value
     * @since 6.4 */
    protected abstract void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName);

    /** Informs about change of tooltip of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which tooltip has changed
     * @param toolTip newly changed tooltip value
     * @since 4.13 */
    protected abstract void topComponentToolTipChanged(TopComponent tc, String toolTip);

    /** Informs about chagne of icon of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which icon has changed
     * @param icon newly chaned icon value
     * @since 4.13 */
    protected abstract void topComponentIconChanged(TopComponent tc, Image icon);

    /** Informs about change of activated nodes of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which activated nodes has chagned
     * @param activatedNodes newly chaged activated nodes value
     * @since 4.13 */
    protected abstract void topComponentActivatedNodesChanged(TopComponent tc, Node[] activatedNodes);

    /** Indicates whether specified <code>TopComponent</code> is opened.
     * @param tc specified <code>TopComponent</code>
     * @since 4.13 */
    protected abstract boolean topComponentIsOpened(TopComponent tc);

    /** Gets default list of actions which appear in popup menu of TopComponent.
     * The popup menu which is handled by window systsm implementation, typically at tab.
     * @param tc <code>TopComponent</code> for which the default actions to provide
     * @since 4.13 */
    protected abstract javax.swing.Action[] topComponentDefaultActions(TopComponent tc);

    /** Returns unique ID for specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> the component for which is ID returned
     * @param preferredID first approximation used for ID
     * @return unique <code>TopComponent</code> ID
     * @since 4.13 */
    protected abstract String topComponentID(TopComponent tc, String preferredID);

    /**
     * Cause this TopComponent's tab to flash or otherwise draw the users' attention
     * to it.
     * Note to WindowManager providers: This method not abstract for backward compatibility reasons,
     * please override and provide implementation.
     * @param tc A TopComponent
     * @since 5.1 */
    protected void topComponentRequestAttention(TopComponent tc) {
    }

    /**
     * Attempts to bring the parent <code>Window</code> of the given <code>TopComponent</code>
     * to front of other windows.
     * @see java.awt.Window#toFront()
     * @since 5.8
     */
    protected void topComponentToFront(TopComponent tc) {
        Window parentWindow = SwingUtilities.getWindowAncestor(tc);

        // be defensive, although w probably will always be non-null here
        if (null != parentWindow) {
            if (parentWindow instanceof Frame) {
                Frame parentFrame = (Frame) parentWindow;
                int state = parentFrame.getExtendedState();

                if ((state & Frame.ICONIFIED) > 0) {
                    parentFrame.setExtendedState(state & ~Frame.ICONIFIED);
                }
            }

            parentWindow.toFront();
        }
    }

    /**
     * Stop this TopComponent's tab from flashing if it is flashing.
     * Note to WindowManager providers: This method not abstract for backward compatibility reasons,
     * please override and provide implementation.
     *
     * @param tc A TopComponent
     * @since 5.1 */
    protected void topComponentCancelRequestAttention(TopComponent tc) {
    }

    /** Returns unique ID for specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> the component for which is ID returned
     * @return unique <code>TopComponent</code> ID
     * @since 4.13 */
    public String findTopComponentID(TopComponent tc) {
        return topComponentID(tc, tc.preferredID());
    }

    /** Returns <code>TopComponent</code> for given unique ID.
     * @param tcID unique <code>TopComponent</code> ID
     * @return <code>TopComponent</code> instance corresponding to unique ID
     * @since 4.15 */
    public abstract TopComponent findTopComponent(String tcID);

    /** A manager that handles operations on top components.
     * It is always attached to a {@link TopComponent}.
     * @deprecated Do not use anymore. This interface is replaced by bunch of protedcted methods
     * which name starts with topComponent prefix, i.e. {@link #topComponentOpen}, {@link #topComponentClose} etc. */
    protected interface Component extends java.io.Serializable {
        /**
         * Do not use.
         * @deprecated Only public by accident.
         */

        /* public static final */ long serialVersionUID = 0L;

        /** Open the component on current workspace */
        public void open();

        /**
         * Opens this component on a given workspace.
         * @param workspace the workspace on which to open it
         */
        public void open(Workspace workspace);

        /**
         * Closes this component on a given workspace.
         * @param workspace the workspace on which to close it
         */
        public void close(Workspace workspace);

        /** Called when the component requests focus. Moves it to be visible.
        */
        public void requestFocus();

        /** Set this component visible but not selected or focused if possible.
        * If focus is in other container (multitab) or other pane (split) in
        * the same container it makes this component only visible eg. it selects
        * tab with this component.
        * If focus is in the same container (multitab) or in the same pane (split)
        * it has the same effect as requestFocus().
        */
        public void requestVisible();

        /** Get the set of activated nodes.
        * @return currently activated nodes for this component
        */
        public Node[] getActivatedNodes();

        /** Set the set of activated nodes for this component.
        * @param nodes new set of activated nodes
        */
        public void setActivatedNodes(Node[] nodes);

        /** Called when the name of the top component changes.
        */
        public void nameChanged();

        /** Set the icon of the top component.
        * @param icon the new icon
        */
        public void setIcon(final Image icon);

        /**
         * Gets the icon associated with this component.
         * @return the icon
         */
        public Image getIcon();

        /**
         * Gets a list of workspaces where this component is currently open.
         * @return the set of workspaces where the managed component is open
         */
        public Set whereOpened();
    }
}
