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

package org.netbeans.core.output2;

import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Supplies Output Window implementation through Lookup.
 * @author Jesse Glick, Tim Boudreau
 */
public final class NbIOProvider extends IOProvider {
    static final PairMap namesToIos = new PairMap();
    
    private static final String STDOUT = NbBundle.getMessage(NbIOProvider.class,
        "LBL_STDOUT"); //NOI18N
    
    /** 
     * Default standard out instance
     */
    static NbIO stdOut = null;
    
    
    public OutputWriter getStdOut() {
        if (stdOut != null) {
            stdOut = new NbIO (STDOUT, null);
        }
        //XXX this will cause clearing stdout on each call.  Good? bad?
        Controller.ensureViewInDefault (stdOut, true);
        return stdOut.getOut();
    }
    
    
    public InputOutput getIO(String name, boolean newIO) {
        return getIO (name, newIO, null);
    }
    
    
    public InputOutput getIO(String name, boolean newIO, Action[] toolbarActions) {
        if (Controller.log) {
            Controller.log("GETIO: " + name + " new:" + newIO);
        }
        NbIO result = (NbIO) namesToIos.get(name);

        if (result == null || newIO) {
            result = new NbIO(name, toolbarActions);
            namesToIos.add (name, result);
            Controller.ensureViewInDefault (result, newIO);
        }
        return result;
    }
    
    
    static void dispose (NbIO io) {
        namesToIos.remove (io);
    }
    
    /**
     * Called when the output window is hidden.  Switches the caching of IOs
     * to weak references, so that only those still alive will be shown the 
     * next time the output window is opened.
     */
    static void setWeak(boolean value) {
        namesToIos.setWeak(value);
    }
}

