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

package org.netbeans.modules.project.uiapi;

import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.util.Lookup;

/** Utility methods for the projectUI module.
 * @author Petr Hrebejk
 */
public class Utilities {
    
    private Utilities() {}
    
    /** Gets action factory from the global Lookup.
     */
    public static ActionsFactory getActionsFactory() {        
        return (ActionsFactory)Lookup.getDefault().lookup( ActionsFactory.class );        
    }
    
    /** Gets the projectChooser fatory from the global Lookup
     */
    public static ProjectChooserFactory getProjectChooserFactory() {
        return (ProjectChooserFactory)Lookup.getDefault().lookup( ProjectChooserFactory.class );
    }
    
    /** Gets an object the OpenProjects can delegate to
     */
    public static OpenProjectsTrampoline getOpenProjectsTrampoline() {
        return (OpenProjectsTrampoline)Lookup.getDefault().lookup( OpenProjectsTrampoline.class );
    }    
}
