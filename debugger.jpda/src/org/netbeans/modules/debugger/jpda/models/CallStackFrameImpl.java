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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.*;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.spi.viewmodel.NoInformationException;


/**
* Class representating one line of callstack.
*/
public class CallStackFrameImpl implements CallStackFrame {
    
    private ThreadReference thread;
    private int index;
    private CallStackTreeModel ctm;
    private String id;
    
    
    public CallStackFrameImpl (
        StackFrame sf,
        CallStackTreeModel ctm,
        String id
    ) {
        try {
            this.thread = sf.thread();
            this.index = thread.frames().indexOf(sf);
        } catch (Exception e) {
            // this stack frame is invalid
        }
        this.ctm = ctm;
        this.id = id;
    }
    
    
    // public interface ........................................................
        
    /**
    * Returns line number of this frame in this callstack.
    *
    * @return Returns line number of this frame in this callstack.
    */
    public int getLineNumber (String struts) {
        try {
            return getStackFrame().location ().lineNumber (struts);
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
//            ex.printStackTrace ();
        }
        return 0;
    }

    /**
    * Returns method name of this frame in this callstack.
    *
    * @return Returns method name of this frame in this callstack.
    */
    public String getMethodName () {
        try {
            return getStackFrame().location ().method ().name ();
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
//            ex.printStackTrace ();
        }
        return "";
    }

    /**
    * Returns class name of this frame in this callstack.
    *
    * @return class name of this frame in this callstack
    */
    public String getClassName () {
        try {
            return getStackFrame().location ().declaringType ().name ();
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
//            ex.printStackTrace ();
        }
        return "";
    }

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public String getDefaultStratum () {
        try {
            return getStackFrame().location ().declaringType ().defaultStratum ();
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
        }
        return "";
    }

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public List getAvailableStrata () {
        try {
            return getStackFrame().location ().declaringType ().availableStrata ();
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
        }
        return new ArrayList ();
    }

    /**
    * Returns name of file of this frame.
    *
    * @return name of file of this frame
    * @throws NoInformationException if informations about source are not included or some other error
    *   occurres.
    */
    public String getSourceName (String stratum) throws NoInformationException {
        try {
            return getStackFrame().location ().sourceName (stratum);
        } catch (AbsentInformationException ex) {
            throw new NoInformationException (ex.getMessage ());
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
//            ex.printStackTrace ();
        }
        return "";
    }
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public String getSourcePath (String stratum) throws NoInformationException {
        try {
            return getStackFrame().location ().sourcePath (stratum);
        } catch (AbsentInformationException ex) {
            throw new NoInformationException (ex.getMessage ());
        } catch (Exception ex) {
        // this stack frame is not available or information in it is not available
//            ex.printStackTrace ();
        }
        return "";
    }
    
    public void makeCurrent () {
        ctm.getDebugger ().setCurrentCallStackFrame (this);
    }
    
    public org.netbeans.api.debugger.jpda.LocalVariable[] getLocalVariables () 
    throws NoInformationException {
        LocalsTreeModel ltm = ctm.getLocalsTreeModel ();
        AbstractVariable vs[] = ltm.getLocalVariables (getStackFrame(), false);
        org.netbeans.api.debugger.jpda.LocalVariable[] var = new
            org.netbeans.api.debugger.jpda.LocalVariable [vs.length];
        System.arraycopy (vs, 0, var, 0, vs.length);
        return var;
    }
    
    public This getThisVariable () {
        ObjectReference thisR = getStackFrame().thisObject ();
        if (thisR == null) return null;
        LocalsTreeModel ltm = ctm.getLocalsTreeModel ();
        return ltm.getThis (thisR, "");
    }

    public StackFrame getStackFrame() {
        try {
            return thread.frame(index);
        } catch (Exception e) {
            return null;
        }
    }

    // other methods............................................................

    public boolean equals (Object o) {
        return  (o instanceof CallStackFrameImpl) &&
                (id.equals (((CallStackFrameImpl) o).id));
    }
    
    public int hashCode () {
        return id.hashCode ();
    }
}

