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


import java.awt.Rectangle;
import java.awt.Image;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;

import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;


/** This class is an implementation of Mode interface.
 * It designates 'place' on screen, at wich TopComponent can occure.
 *
 * @author Peter Zavadsky
 */
public final class ModeImpl implements Mode {

    /** Name constant as a base for nonamed modes. */
    private static final String MODE_ANONYMOUS_NAME = "anonymousMode"; // NOI18N
    
    /** asociated property change support for firing property changes */
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    
    /** Construct new mode with given properties */
    private ModeImpl(String name, int state, int kind, boolean permanent) {
        getCentral().createModeModel(this, name, state, kind, permanent);
    }
    
    
    /** Factory method which creates <code>ModeImpl</code> instances. */
    public static ModeImpl createModeImpl(String name, int state, int kind, boolean permanent) {
        // PENDING Validate values.
        if(state == Constants.MODE_STATE_SEPARATED
        && kind == Constants.MODE_KIND_EDITOR) {
            // PENDING Repairing. Editor can be only in split now.
            state = Constants.MODE_STATE_JOINED;
        }
        return new ModeImpl(name, state, kind, permanent);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    // Start of org.openide.windows.Mode interface implementation.
    ///////////////////////////////////////////////////////////////////
    /** Gets the programmatic name of this mode.
     * This name should be unique, as it is used to find modes etc.
     * Implements <code>Mode</code> interface method.
     * @return programmatic name of this mode */
    public String getName () {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        return getCentral().getModeName(this);
    }
    
    /** Gets display name of this mode.
     ** Implements <code>Mode</code> interface method.
     * @return Human presentable name of this mode implementation
     * @deprecated It is not used anymore. This impl delegated to {@link #getName} method.  */
    public String getDisplayName () {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        return getName();
    }

    /** Gets icon for this mode.
     * Implements <code>Mode</code> interface method. 
     * @return null
     * @deprecated It is not used anymore. */
    public Image getIcon () {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        return null;
    }

    /** Indicates whether specified <code>TopComponent</code> can be docked
     * into this <code>Mode</code>.
     * Implements <code>Mode</code> interface method. 
     * @return <code>true</code> */
    public boolean canDock(TopComponent tc) {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        return true;
    }
    
    /** Attaches a component to a mode for this workspace.
     * If the component is in different mode on this desktop, it is 
     * removed from the original and moved to this one.
     * Implements <code>Mode</code> interface method.
     *
     * @param tc top component to dock into this mode
     * @return true if top component was succesfully docked to this
     * mode, false otherwise */
    public boolean dockInto(TopComponent tc) {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        return dockIntoImpl(tc, true);
    }
    
    /** Sets bounds of this mode.
     * Implements <code>Mode</code> interface method.
     * @param rect bounds for the mode */
    public void setBounds (Rectangle bounds) {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        getCentral().setModeBounds(this, bounds);
    }

    /** Getter for current bounds of the mode.
     * Implements <code>Mode</code> interface method.
     * @return the bounds of the mode
     */
    public Rectangle getBounds () {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        return getCentral().getModeBounds(this);
    }
    

    /** Getter for asociated workspace.
     * Implements <code>Mode</code> interface method.
     * @return The workspace instance to which is this mode asociated.
     * @deprecated XXX Don't use anymore.
     */
    public Workspace getWorkspace () {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        // Here is the only fake workspace.
        return WindowManagerImpl.getInstance();
    }
    
    /** Gets array of <code>TopComponent</code>S in this mode.
     * Implements <code>Mode</code> interface method.
     * @return array of top components which are currently
     * docked in this mode. May return empty array if no top component
     * is docked in this mode.
     */
    public TopComponent[] getTopComponents() {
        WindowManagerImpl.assertEventDispatchThreadWeak();
        
        return (TopComponent[])getCentral().getModeTopComponents(this).toArray(new TopComponent[0]);
    }

    /** Adds listener to the property changes.
     * Implements <code>Mode</code> interface support. */
    public void addPropertyChangeListener (PropertyChangeListener pchl) {
        changeSupport.addPropertyChangeListener(pchl);
    }

    /** Removes listener to the property changes.
     * Implements <code>Mode</code> interface method. */
    public void removePropertyChangeListener (PropertyChangeListener pchl) {
        changeSupport.removePropertyChangeListener(pchl);
    }
    ///////////////////////////////////////////////////////////////////
    // End of org.openide.windows.Mode interface implementation.
    ///////////////////////////////////////////////////////////////////

    
    /** Actually performs the docking operation.
     * @param tc top component to dock into this mode
     * @param orderWeight weight for ordering. Smaller weight number means
     * smaller position index, which means closer to the top or start in
     * visual representations 
     * @param select <code>true</code> if the docked <code>TopComponent</code>
     * will be selected afterwards
     * @return true if top component was succesfully docked to this */
    private boolean dockIntoImpl(final TopComponent tc, final boolean select) {
        Debug.log(ModeImpl.class, "Docking tc=" + tc.getName() + " into mode=" + this); // NOI18N
        Debug.dumpStack(ModeImpl.class);
        
        // PENDING
        // Preferably all in one step.
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        if(mode != null && mode != this) {
            // XXX if only closin (mode.close(tc)) there could happen,
            // there is the same TopComponent as closed in two modes. Revise.
            mode.removeTopComponent(tc);
        }
        
        addClosedTopComponent(tc);
        return true;
    }
    
    /** Closes given top component. */
    public void close(TopComponent tc) {
        if(!getOpenedTopComponents().contains(tc)) {
            return;
        }
        
        if(getKind() == Constants.MODE_KIND_EDITOR) {
            removeTopComponent(tc);
        } else {
            addClosedTopComponent(tc);
        }
    }

    /** Gets list of opened TopComponentS. */
    public List getOpenedTopComponents() {
        return getCentral().getModeOpenedTopComponents(this);
    }
    
    /** Sets selected TopComponent. */
    public void setSelectedTopComponent(TopComponent tc) {
        if(!getOpenedTopComponents().contains(tc)) {
            return;
        }
        
        TopComponent old = getSelectedTopComponent();
        if(tc == old) {
            return;
        }
        
        getCentral().setModeSelectedTopComponent(this, tc);
    }
    
    /** Gets selected TopComponent. */
    public TopComponent getSelectedTopComponent() {
        WindowManagerImpl.assertEventDispatchThread();
        
        return getCentral().getModeSelectedTopComponent(this);
    }
    
    public void addOpenedTopComponent(TopComponent tc) {
        getCentral().addModeOpenedTopComponent(this, tc);
    }
    
    public void addClosedTopComponent(TopComponent tc) {
        getCentral().addModeClosedTopComponent(this, tc);
    }
    
    public void addUnloadedTopComponent(String tcID) {
        getCentral().addModeUnloadedTopComponent(this, tcID);
    }
    
    public void setUnloadedSelectedTopComponent(String tcID) {
        getCentral().setUnloadedSelectedTopComponent(this, tcID);
    }
    
    // XXX
    public List getOpenedTopComponentsIDs() {
        return getCentral().getModeOpenedTopComponentsIDs(this);
    }
    // XXX
    public List getClosedTopComponentsIDs() {
        return getCentral().getModeClosedTopComponentsIDs(this);
    }
    
    /** Sets and updates the state of associated frame, if frame exists.
     * Otherwise remembers state for futher use
     */
    public void setFrameState(int state) {
        getCentral().setModeFrameState(this, state);
    }
    
    /** @return state of the frame
     * If frame exists, its real state is returned. 
     * Last remembered frame state is returned if frame currently
     * doesn't exist. FrameType.NORMAL is returned as default if state cannot be
     * obtained by mentioned procedures.
     */
    public int getFrameState () {
        return getCentral().getModeFrameState(this);
    }
    
    /** Indicates whether this mode is permanent, it means it is kept in model
     * even in case it becomes empty. */
    public boolean isPermanent () {
        return getCentral().isModePermanent(this);
    }
    
    /** Indicates whether this mode has no TopComponents. */
    public boolean isEmpty() {
        return getCentral().isModeEmpty(this);
    }

    public boolean containsTopComponent(TopComponent tc) {
        return getCentral().containsModeTopComponent(this, tc);
    }
    
    /** Gets state of mode. Either split or separate. */
    public int getState() {
        return getCentral().getModeState(this);
    }
    
    /** Gets kind, either editor or view. */
    public int getKind() {
        return getCentral().getModeKind(this);
    }
    
    // Contstraints and split weights are saved in split structure at wm model level.
    /** Sets constraints for mode. */
    public void setConstraints(SplitConstraint[] constraints) {
        WindowManagerImpl.getInstance().setModeConstraints(this, constraints);
    }

    /** @return Current constraints of this mode, null by default */
    public SplitConstraint[] getConstraints() {
        return WindowManagerImpl.getInstance().getModeConstraints(this);
    }
    
    /** Removes TopComponent from this mode. */
    public void removeTopComponent(TopComponent tc) {
        getCentral().removeModeTopComponent(this, tc);
    }
    
    public void removeTopComponents(Set topComponentSet) {
        for(Iterator it = topComponentSet.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            removeTopComponent(tc);
        }
    }
    
    // XXX Only use for yet unloaded components, for PersistenceHandler only.
    public void removeClosedTopComponentID(String tcID) {
        getCentral().removeModeClosedTopComponentID(this, tcID);
    }
    
    // XXX It is used for user actions only, to prohibit mixing
    // of view and editor components.
    /** Indicates whether this mode can contain specified TopComponent. */
    public boolean canContain(TopComponent tc) {
        if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
        || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(tc)) {
            return true;
        }
        
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        if(mode == null) {
            return true;
        }
        
        return mode.getKind() == getKind();
    }
    
    void doFirePropertyChange(final String propName,
    final Object oldValue, final Object newValue) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                changeSupport.firePropertyChange(propName, oldValue, newValue);
            }
        });
    }
    
    /** @return string description of this mode */
    public String toString () {
        return super.toString () + "[" + getName () + "]"; // NOI18N
    }
    
    /** Accessor to central unit. Helper method. */
    private static Central getCentral() {
        return WindowManagerImpl.getInstance().getCentral();
    }
    
    
    ////////////////////
    // Utility methods>>
    /*private*/ static String getUnusedModeName() {
        String base = MODE_ANONYMOUS_NAME;
        
        // don't allow base to be too long, because will act as file name too
        // PENDING Maximal length is 20.
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }
        
        // add numbers to the name
        String result;
        int modeNumber = 1;
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        while(wm.findMode(result = base + "_" + modeNumber) != null) { // NOI18N
            modeNumber++;
        }
        return result;
    }
    // Utility methods<<
    ////////////////////

    
}

