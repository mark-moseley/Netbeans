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

package org.netbeans.modules.cnd.debugger.gdb.event;

import java.util.EventObject;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.Variable;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;

/**
 * GdbBreakpoint event notification.
 */
public final class GdbBreakpointEvent extends EventObject {

    /** Condition result constant. */
    public static final int CONDITION_NONE = 0;

    /** Condition result constant. */
    public static final int CONDITION_TRUE = 1;

    /** Condition result constant. */
    public static final int CONDITION_FALSE = 2;

    /** Condition result constant. */
    public static final int CONDITION_FAILED = 3;
    
    
    private int             conditionResult = CONDITION_FAILED;
    private Throwable       conditionException = null;
    private GdbDebugger	    debugger;
    //private CndThread	    thread;
    //private ReferenceType   referenceType;
//    private Variable        variable;
    private boolean         resume = false;
    

    /**
     * Creates a new instance of GdbBreakpointEvent. This method should be
     * called from debuggergdb module only. Do not create a new instances
     * of this class!
     * 
     * @param sourceBreakpoint  a breakpoint
     * @param debugger          a debugger this
     * @param conditionResult   a result of condition
     * @param variable          a context variable
     */
    public GdbBreakpointEvent(GdbBreakpoint sourceBreakpoint, GdbDebugger debugger,
                    int conditionResult, Variable variable) {
        super(sourceBreakpoint);
        this.conditionResult = conditionResult;
        this.debugger = debugger;
//        this.variable = variable;
    }
    
    /**
     * Creates a new instance of GdbBreakpointEvent.
     * 
     * @param sourceBreakpoint a breakpoint
     * @param conditionException result of condition
     * @param debugger          a debugger this
     * @param variable          a context variable
     */
    public GdbBreakpointEvent(GdbBreakpoint sourceBreakpoint, GdbDebugger debugger,
                    Throwable conditionException, Variable variable) {
        super(sourceBreakpoint);
        this.conditionResult = CONDITION_FAILED;
        this.conditionException = conditionException;
        this.debugger = debugger;
//        this.variable = variable;
    }
    
    /**
     * Returns result of condition evaluation.
     *
     * @return result of condition evaluation
     */
    public int getConditionResult() {
        return conditionResult;
    }
    
    /**
     * Returns result of condition evaluation.
     *
     * @return result of condition evaluation
     */
    public Throwable getConditionException() {
        return conditionException;
    }
    
    /**
     * Returns GdbDebugger instance this breakpoint has been reached in.
     * 
     * @return GdbDebugger instance this breakpoint has been reached in
     */
    public GdbDebugger getDebugger() {
        return debugger;
    }
    
    /**
     * Call this method to resume debugger after all events have been notified.
     * You should not call GdbDebugger.resume() during breakpoint event 
     * evaluation!
     */
    public void resume() {
        resume = true;
    }
    
    /**
     * Returns resume value.
     */
    public boolean getResume() {
        return resume;
    }
}
