/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.debugger;

import java.awt.Component;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.debugger.actions.BreakpointsWindowAction;
import org.netbeans.jellytools.modules.debugger.actions.DeleteAllBreakpointsAction;
import org.netbeans.jemmy.ComponentChooser;

/**
 * Provides access to the Breakpoints window.
 * <p>
 * Usage:<br>
 * <pre>
 *      BreakpointsWindowOperator bwo = new BreakpointsWindowOperator().invoke();
 *      bwo.deleteAll();
 *      bwo.close();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 * @see org.netbeans.jellytools.OutputTabOperator
 */
public class BreakpointsWindowOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new BreakpointsWindowAction();
    
    /** Waits for Breakpoints window top component and creates a new operator
     * for it. */
    public BreakpointsWindowOperator() {
        super(waitTopComponent(null, 
                Bundle.getStringTrimmed ("org.netbeans.modules.debugger.ui.views.Bundle",
                                         "CTL_Breakpoints_view"),
                0, viewSubchooser));
    }
    
    /**
     * Opens Breakpoints window from main menu Window|Debugging|Breakpoints and
     * returns BreakpointsWindowOperator.
     * @return instance of BreakpointsWindowOperator
     */
    public static BreakpointsWindowOperator invoke() {
        invokeAction.perform();
        return new BreakpointsWindowOperator();
    }
    
    /********************************** Actions ****************************/
    
    /** Performs Delete All action on active tab. */
    public void deleteAll() {
        new DeleteAllBreakpointsAction().perform(this);
    }
    
    /**
     * Performs verification of BreakpointsWindowOperator by accessing its
     * components.
     */
    public void verify() {    
        // TBD
    }
    
    /** SubChooser to determine OutputWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser viewSubchooser = new ComponentChooser() {
        private static final String CLASS_NAME="org.netbeans.modules.debugger.ui.views.View";
        
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith(CLASS_NAME);
        }
        
        public String getDescription() {
            return "component instanceof "+CLASS_NAME;// NOI18N
        }
    };
}
