/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.util.Properties;
import javax.swing.JComponent;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.spi.debugger.ui.BreakpointType;

import org.netbeans.modules.debugger.jpda.ui.Context;

import org.openide.util.NbBundle;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ClassBreakpointType extends BreakpointType {
    
    public String getCategoryDisplayName () {
        return NbBundle.getMessage (
            ClassBreakpointType.class,
            "CTL_Java_breakpoint_events_cathegory_name"
        );
    }
    
    public JComponent getCustomizer () {
        return new ClassBreakpointPanel ();
    }
    
    public String getTypeDisplayName () {
        return NbBundle.getMessage (
            ClassBreakpointType.class, 
            "CTL_Class_event_type_name"
        );
    }
    
    public boolean isDefault () {
        return Context.getDefaultType () == Context.CLASS;
    }
}

