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

package org.netbeans.spi.project.ui.support;

import javax.swing.Action;
import org.netbeans.modules.project.uiapi.Utilities;

/**
 * Factory for commonly needed generic project actions.
 * @author Jesse Glick, Petr Hrebejk
 */
public class CommonProjectActions {
    
    private CommonProjectActions() {}
        
    /**
     * Create an action "Set As Main Project".
     * It should be invoked with an action context containing
     * one {@link Project}.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action setAsMainProjectAction() {
        return Utilities.getActionsFactory().setAsMainProjectAction();
    }
    
    /**
     * Create an action "Customize Project".
     * It should be invoked with an action context containing
     * one {@link Project}.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action customizeProjectAction() {
        return Utilities.getActionsFactory().customizeProjectAction();
    }
    
    /**
     * Create an action "Open Subprojects".
     * It should be invoked with an action context containing
     * one or more {@link Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action openSubprojectsAction() {
        return Utilities.getActionsFactory().openSubprojectsAction();
    }
    
    /**
     * Create an action "Close Project".
     * It should be invoked with an action context containing
     * one or more {@link Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action closeProjectAction() {
        return Utilities.getActionsFactory().closeProjectAction();
    }
    
    /**
     * Create an action project dependent "New File" action.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action newFileAction() {
        return Utilities.getActionsFactory().newFileAction();
    }
    
}
