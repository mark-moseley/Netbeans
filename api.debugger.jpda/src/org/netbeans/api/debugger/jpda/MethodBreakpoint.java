/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.api.debugger.jpda;

import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Breakpoint;

/**
 * Notifies about method entry events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (MethodBreakpoint.create (
 *        "examples.texteditor.Ted*",
 *        "<init>
 *    ));</pre>
 * This breakpoint stops when some initializer of class Ted or innercalsses is
 * called.
 *
 * @author Jan Jancura
 */
public class MethodBreakpoint extends JPDABreakpoint {

    /** Property name constant */
    public static final String          PROP_METHOD_NAME = "methodName"; // NOI18N
    /** Property name constant */
    public static final String          PROP_METHOD_SIGNATURE = "signature"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_BREAKPOINT_TYPE = "breakpointtType"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_FILTERS = "classFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_EXCLUSION_FILTERS = "classExclusionFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_INSTANCE_FILTERS = "instanceFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_THREAD_FILTERS = "threadFilters"; // NOI18N

    /** Breakpoint type property value constant. */
    public static final int             TYPE_METHOD_ENTRY = 1;
    /** Breakpoint type property value constant. */
    public static final int             TYPE_METHOD_EXIT = 2;

    /** Property variable. */
    private String[]                    classFilters = new String [0];
    private String[]                    classExclusionFilters = new String [0];
    private String                      methodName = "";
    private String                      methodSignature;
    private int                         breakpointType = TYPE_METHOD_ENTRY;
    private String                      condition = "";
    private Map<JPDADebugger,ObjectVariable[]> instanceFilters;
    private Map<JPDADebugger,JPDAThread[]> threadFilters;
    
    
    private MethodBreakpoint () {
    }
    
    /**
     * Creates a new method breakpoint for given parameters.
     *
     * @param className a class name filter
     * @param methodName a name of method
     * @return a new breakpoint for given parameters
     */
    public static MethodBreakpoint create (
        String className,
        String methodName
    ) {
        MethodBreakpoint b = new MethodBreakpointImpl ();
        b.setClassFilters (new String[] {className});
        b.setMethodName (methodName);
        return b;
    }
    
    /**
     * Creates a new method breakpoint.
     *
     * @return a new method breakpoint
     */
    public static MethodBreakpoint create (
    ) {
        MethodBreakpoint b = new MethodBreakpointImpl ();
        return b;
    }

    /**
     * Get name of method to stop on.
     *
     * @return name of method to stop on
     */
    public String getMethodName () {
        return methodName;
    }

    /**
     * Set name of method to stop on.
     *
     * @param mn a name of method to stop on
     */
    public void setMethodName (String mn) {
        if (mn != null) {
            mn = mn.trim();
        }
        if ( (mn == methodName) ||
             ((mn != null) && (methodName != null) && methodName.equals (mn))
        ) return;
        String old = methodName;
        methodName = mn;
        firePropertyChange (PROP_METHOD_NAME, old, mn);
    }
    
    /**
     * Get the JNI-style signature of the method to stop on.
     *
     * @return JNI-style signature of the method to stop on
     * @see com.sun.jdi.TypeComponent#signature
     */
    public String getMethodSignature () {
        return methodSignature;
    }

    /**
     * Set JNI-style signature of the method to stop on.
     *
     * @param signature the JNI-style signature of the method to stop on
     * @see com.sun.jdi.TypeComponent#signature
     */
    public void setMethodSignature (String signature) {
        if (signature != null) {
            signature = signature.trim();
        }
        if ((signature == methodSignature) ||
            ((signature != null) && signature.equals (methodSignature))) {
            
            return;
        }
        String old = methodSignature;
        methodSignature = signature;
        firePropertyChange (PROP_METHOD_SIGNATURE, old, signature);
    }
    
    /**
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition () {
        return condition;
    }
    
    /**
     * Sets condition.
     *
     * @param cond a c new condition
     */
    public void setCondition (String cond) {
        if (cond != null) {
            cond = cond.trim();
        }
        String old = condition;
        condition = cond;
        firePropertyChange (PROP_CONDITION, old, cond);
    }

    /**
     * Returns type of this breakpoint.
     *
     * @return type of this breakpoint
     */
    public int getBreakpointType () {
        return breakpointType;
    }

    /**
     * Sets type of this breakpoint (TYPE_METHOD_ENTRY or TYPE_METHOD_EXIT).
     *
     * @param breakpointType a new value of breakpoint type property
     */
    public void setBreakpointType (int breakpointType) {
        if (breakpointType == this.breakpointType) return;
        if ((breakpointType & (TYPE_METHOD_ENTRY | TYPE_METHOD_EXIT)) == 0)
            throw new IllegalArgumentException  ();
        int old = this.breakpointType;
        this.breakpointType = breakpointType;
        firePropertyChange (PROP_BREAKPOINT_TYPE, new Integer (old), new Integer (breakpointType));
    }

    /**
     * Get list of class filters to stop on.
     *
     * @return list of class filters to stop on
     */
    public String[] getClassFilters () {
        return classFilters;
    }

    /**
     * Set list of class filters to stop on.
     *
     * @param classFilters a new value of class filters property
     */
    public void setClassFilters (String[] classFilters) {
        if (classFilters == this.classFilters) return;
        Object old = this.classFilters;
        this.classFilters = classFilters;
        firePropertyChange (PROP_CLASS_FILTERS, old, classFilters);
    }

    /**
     * Get list of class exclusion filters to stop on.
     *
     * @return list of class exclusion filters to stop on
     */
    public String[] getClassExclusionFilters () {
        return classExclusionFilters;
    }

    /**
     * Set list of class exclusion filters to stop on.
     *
     * @param classExclusionFilters a new value of class exclusion filters property
     */
    public void setClassExclusionFilters (String[] classExclusionFilters) {
        if (classExclusionFilters == this.classExclusionFilters) return;
        Object old = this.classExclusionFilters;
        this.classExclusionFilters = classExclusionFilters;
        firePropertyChange (PROP_CLASS_EXCLUSION_FILTERS, old, classExclusionFilters);
    }
    
    /**
     * Get the instance filter for a specific debugger session.
     * @return The instances or <code>null</code> when there is no instance restriction.
     */
    public ObjectVariable[] getInstanceFilters(JPDADebugger session) {
        if (instanceFilters != null) {
            return instanceFilters.get(session);
        } else {
            return null;
        }
    }
    
    /**
     * Set the instance filter for a specific debugger session. This restricts
     * the breakpoint to specific instances in that session.
     * @param session the debugger session
     * @param instances the object instances or <code>null</code> to unset the filter.
     */
    public void setInstanceFilters(JPDADebugger session, ObjectVariable[] instances) {
        if (instanceFilters == null) {
            instanceFilters = new WeakHashMap<JPDADebugger, ObjectVariable[]>();
        }
        if (instances != null) {
            instanceFilters.put(session, instances);
        } else {
            instanceFilters.remove(session);
        }
        firePropertyChange(PROP_INSTANCE_FILTERS, null,
                instances != null ?
                    new Object[] { session, instances } : null);
    }

    /**
     * Get the thread filter for a specific debugger session.
     * @return The thread or <code>null</code> when there is no thread restriction.
     */
    public JPDAThread[] getThreadFilters(JPDADebugger session) {
        if (threadFilters != null) {
            return threadFilters.get(session);
        } else {
            return null;
        }
    }
    
    /**
     * Set the thread filter for a specific debugger session. This restricts
     * the breakpoint to specific threads in that session.
     * @param session the debugger session
     * @param threads the threads or <code>null</code> to unset the filter.
     */
    public void setThreadFilters(JPDADebugger session, JPDAThread[] threads) {
        if (threadFilters == null) {
            threadFilters = new WeakHashMap<JPDADebugger, JPDAThread[]>();
        }
        if (threads != null) {
            threadFilters.put(session, threads);
        } else {
            threadFilters.remove(session);
        }
        firePropertyChange(PROP_THREAD_FILTERS, null,
                threads != null ?
                    new Object[] { session, threads } : null);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "MethodBreakpoint " + java.util.Arrays.asList(classFilters).toString() + "." + methodName +
                ((methodSignature != null) ? " '"+methodSignature+"'" : "");
    }
    
    private static final class MethodBreakpointImpl extends MethodBreakpoint implements ChangeListener {
        
        public void stateChanged(ChangeEvent chev) {
            Object source = chev.getSource();
            if (source instanceof Breakpoint.VALIDITY) {
                setValidity((Breakpoint.VALIDITY) source, chev.toString());
            } else {
                throw new UnsupportedOperationException(chev.toString());
            }
        }
    }
}
