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

package org.netbeans.core.windows;


import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.WeakSet;


/** Implementstion of registry of top components. This implementation
 * receives information about top component changes from the window
 * manager implementation, to which is listening to.
 *
 * @author Peter Zavadsky
 */
public final class RegistryImpl extends Object implements TopComponent.Registry {
    
    // fields
    /** Activated top component */
    private TopComponent activatedTopComponent;
    /** Set of opened TopComponents */
    private final Set openSet = new WeakSet(30);
    /** Currently selected nodes. */
    private Node[] currentNodes;
    /** Last non-null value of current nodes. (If null -> it means they are
     * not initialized and weren't fired yet. */
    private Node[] activatedNodes;
    /** PropertyChange support */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    

    /** Creates new RegistryImpl */
    public RegistryImpl() {
    }
    
    /** Get all opened componets in the system.
     *
     * @return immutable set of {@link TopComponent}s
     */
    public synchronized Set getOpened() {
        return java.util.Collections.unmodifiableSet(openSet);
    }
    
    /** Get the currently selected element.
     * @return the selected top component, or <CODE>null</CODE> if there is none
     */
    public TopComponent getActivated() {
        return activatedTopComponent;
    }
    
    /** Getter for the currently selected nodes.
     * @return array of nodes or null if no component activated. */
    public Node[] getCurrentNodes() {
        return currentNodes;
    }
    
    /** Getter for the lastly activated nodes. Comparing
     * to previous method it always remembers the selected nodes
     * of the last component that had ones.
     *
     * @return array of nodes (not null)
     */
    public Node[] getActivatedNodes() {
        return activatedNodes == null ? new Node[0] : activatedNodes;
    }
    
    /** Add a property change listener.
     * @param l the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }
    
    /** Remove a property change listener.
     * @param l the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    
    //////////////////////////////////////////////////////
    /// notifications of changes from window manager >>>>>
    /** Called when a TopComponent is activated.
     *
     * @param ev TopComponentChangedEvent
     */
    void topComponentActivated(TopComponent tc) {
        if(activatedTopComponent == tc
        && activatedNodes != null) { // When null it means were not inited yet.
            return;
        }
        
        TopComponent old = activatedTopComponent;
        activatedTopComponent = tc;
        
        Window w = tc == null ? null : SwingUtilities.windowForComponent(tc);
        cancelMenu(w);
        
        doFirePropertyChange(PROP_ACTIVATED, old, activatedTopComponent);

        selectedNodesChanged(activatedTopComponent,
            activatedTopComponent == null ? null : activatedTopComponent.getActivatedNodes());
    }
    
    
    
    /** Called when a TopComponent is opened. */
    synchronized void topComponentOpened(TopComponent tc) {
        if (openSet.contains(tc)) {
            return;
        }
        Set old = new HashSet(openSet);
        openSet.add(tc);
        doFirePropertyChange(PROP_OPENED, old, new HashSet(openSet));
    }
    
    /** Called when a TopComponent is closed. */
    synchronized void topComponentClosed(TopComponent tc) {
        if (!openSet.contains(tc)) {
            return;
        }

        Set old = new HashSet(openSet);
        openSet.remove(tc);
        doFirePropertyChange(PROP_OPENED, old, new HashSet(openSet));
    }
    
    /** Called when selected nodes changed. */
    public void selectedNodesChanged(TopComponent tc, Node[] newNodes) {
        Node[] oldNodes = currentNodes;
        
        //Fixed bug #8933 27 Mar 2001 by Marek Slama
        //Selected nodes event was processed for other than activated component.
        //Check if activatedTopComponent is the same as event source top component
        //If not ignore event
        if(tc != activatedTopComponent
        && activatedNodes != null) { // When null it means were not inited yet.
            return;
        }
        //End of bugfix #8933
        
        if(Arrays.equals(oldNodes, newNodes)
        && activatedNodes != null) { // When null it means were not inited yet.
            return;
        }

        currentNodes = newNodes == null ? null : (Node[])newNodes.clone();
        // fire immediatelly only if window manager in proper state
        tryFireChanges(oldNodes, currentNodes);
    }
    /// notifications of changes from window manager <<<<<
    //////////////////////////////////////////////////////

    
    
    /** Cancels the menu if it is not assigned to specified window.
     * @param window window that the menu should be checked against
     *    (if this window contains the menu, then the menu will not be closed)
     */
    /** Closes popup menu.
     */
    public static void cancelMenu(Window window) {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] path = msm.getSelectedPath();
        
        for (int i = 0; i < path.length; i++) {
            //      if (newPath[i] != path[i]) return;
            java.awt.Window w = SwingUtilities.windowForComponent(
                path[i].getComponent()
            );
            
            // we must check for null because windowForComponent above can return null
            if ((w != null) && (w == window || w.getOwner() == window)) {
                // ok, this menu can stay
                return;
            }
            
        }
        
        msm.clearSelectedPath();
    }
    
    
    /** If window manager in proper state, fire selected and
     * activated node changes */
    private void tryFireChanges(Node[] oldNodes, Node[] newNodes) {
        doFirePropertyChange(PROP_CURRENT_NODES, oldNodes, newNodes);
        
        if(newNodes == null && activatedNodes == null) {
            // Ensure activated nodes are going to be fired first time in session for this case.
            newNodes = new Node[0];
        }
        
        if (newNodes != null) {
            oldNodes = activatedNodes;
            activatedNodes = newNodes;
            doFirePropertyChange(PROP_ACTIVATED_NODES, oldNodes, activatedNodes);
        }
    }
    
    
    private void doFirePropertyChange(final String propName,
    final Object oldValue, final Object newValue) {
        debugLog(""); // NOI18N
        debugLog("Scheduling event firing: propName=" + propName); // NOI18N
        debugLog("\toldValue=" + (oldValue instanceof Object[] ? Arrays.asList((Object[])oldValue) : oldValue)); // NOI18N
        debugLog("\tnewValue=" + (newValue instanceof Object[] ? Arrays.asList((Object[])newValue) : newValue)); // NOI18N
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                support.firePropertyChange(propName, oldValue, newValue);
            }
        });
    }
    
    private static void debugLog(String message) {
        Debug.log(RegistryImpl.class, message);
    }
    
}
