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

package org.netbeans.core.api.multiview;

import java.awt.Image;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.openide.util.HelpCtx;

/** 
 * Description of multi view element. 
 *
 * @author Milos Kleint
 */
public final class MultiViewPerspective {

    static {
        AccessorImpl.createAccesor();
    }
    
    private MultiViewDescription description;
    
    MultiViewPerspective(MultiViewDescription desc) {
        description = desc;
    }
    // package private, access through Accessor
    MultiViewDescription getDescription() {
        return description;
    }
    
    
    /** Gets persistence type of multi view element, the TopComponent will decide
     * on it's onw persistenceType based on the sum of all it's elements.
     * {@link org.openide.windows.TopComponent#PERSISTENCE_ALWAYS} has higher priority than {@link org.openide.windows.TopComponent#PERSISTENCE_ONLY_OPENED}
     * and {@link org.openide.windows.TopComponent#PERSISTENCE_NEVER} has lowest priority.
     * The {@link org.openide.windows.TopComponent} will be stored only if at least one element requesting persistence
     * was made visible.
     */
    public int getPersistenceType() {
        return description.getPersistenceType();
    }

    /** 
     * Gets localized display name of multi view element. Will be placed on the Element's toggle button.
     * @return the display name for this view element.
     */
    public String getDisplayName() {
        return description.getDisplayName();
    }
    
    /** 
     * Icon for the multi view component. Will be shown as {@link org.openide.windows.TopComponent}'s icon
     * when this element is selected.
     * @return The icon of multi view element */
    public Image getIcon () {
        return description.getIcon();
    }

    /** Get the help context of multi view element.
    */
    public HelpCtx getHelpCtx () {
        return description.getHelpCtx();
    }
    
    /**
     * A Description's contribution 
     * to unique TopComponent's Id returned by getID. Returned value is used as starting
     * value for creating unique TopComponent ID for whole enclosing multi view
     * component.
     */
    public String preferredID() {
        return description.preferredID();
    }
    
    
}