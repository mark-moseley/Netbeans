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

import java.util.List;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.MethodArgument;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;


/**
 * Tests JPDA expression stepping action.
 *
 * @author Martin Entlicher, Jan Jancura
 */
public class ExpressionStepTest extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    private String          sourceRoot = System.getProperty ("test.dir.src");
    private JPDASupport     support;

    public ExpressionStepTest (String s) {
        super (s);
    }

    public void testExpressionStep() throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                Utils.getURL(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/ExpressionStepApp.java"),
                30
            );
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                30, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                30, 14,
                "factorial",
                null,
                new Object[] { "10" }
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                31, 14,
                "factorial",
                new Object[] {"3628800"},
                new Object[] { "20" }
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                31, 30,
                "factorial",
                new Object[] {"2432902008176640000"},
                new Object[] { "30" }
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                32, 14,
                "factorial",
                new Object[] {"2432902008176640000", "-8764578968847253504"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                32, 34,
                "factorial",
                new Object[] {"-70609262346240000"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                33, 37,
                "<init>", // "ExpressionStepApp",
                new Object[] {"-70609262346240000", "-3258495067890909184"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                34, 20,
                "m2",
                null,
                new Object[] { "(int) x" }
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                34, 13,
                "m1",
                new Object[] {"-899453552"},
                new Object[] { "exs.m2((int) x)" }
            );
            
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 27,
                "m2",
                new Object[] {"-899453552", "-404600928"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 20,
                "m1",
                new Object[] {"497916032"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 27,
                "m1",
                new Object[] {"497916032", "684193024"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 13,
                "m3",
                new Object[] {"497916032", "684193024", "248958016"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 13,
                "intValue",
                new Object[] {"497916032", "684193024", "248958016", "933151070"}
            );
            
            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }

    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected,
        int column,
        String methodName
    ) {
        try {
            // We need to wait for all listeners to be notified and appropriate
            // actions to be enabled/disabled
            Thread.currentThread().sleep(10);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        support.step (stepType);
        assertEquals(
            "Execution stopped in wrong class", 
            clsExpected, 
            support.getDebugger ().getCurrentCallStackFrame ().getClassName ()
        );
        assertEquals (
            "Execution stopped at wrong line", 
            lineExpected, 
            support.getDebugger ().getCurrentCallStackFrame ().
                getLineNumber (null)
        );
        if (column > 0) {
            Operation op = support.getDebugger ().getCurrentCallStackFrame ().getCurrentOperation(null);
            assertNotNull(op);
            assertEquals("Execution stopped at a wrong column", column, op.getMethodStartPosition().getColumn());
        }
        if (methodName != null) {
            Operation op = support.getDebugger ().getCurrentCallStackFrame ().getCurrentOperation(null);
            assertNotNull(op);
            assertEquals("Execution stopped at a wrong method call", methodName, op.getMethodName());
        }
    }
    
    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected,
        int column,
        String methodName,
        Object[] returnValues
    ) {
        stepCheck(stepType, clsExpected, lineExpected, column, methodName);
        if (returnValues != null) {
            List<Operation> ops = support.getDebugger ().getCurrentThread().getLastOperations();
            assertEquals("Different count of last operations and expected return values.", returnValues.length, ops.size());
            for (int i = 0; i < returnValues.length; i++) {
                Variable rv = ops.get(i).getReturnValue();
                if (rv != null) {
                    assertEquals("Bad return value", returnValues[i], rv.getValue());
                }
            }
        }
    }
    
    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected,
        int column,
        String methodName,
        Object[] returnValues,
        Object[] opArguments
    ) {
        stepCheck(stepType, clsExpected, lineExpected, column, methodName, returnValues);
        Operation currentOp = support.getDebugger ().getCurrentThread().getCurrentOperation();
        MethodArgument[] arguments = getContext().getArguments(
                Utils.getURL(sourceRoot + "org/netbeans/api/debugger/jpda/testapps/ExpressionStepApp.java"),
                currentOp);
        assertEquals("Different count of operation arguments.", opArguments.length, arguments.length);
        for (int i = 0; i < opArguments.length; i++) {
            assertEquals("Bad method argument", opArguments[i], arguments[i].getName());
        }
    }
    
    private static EditorContext getContext () {
        List l = DebuggerManager.getDebuggerManager ().lookup 
            (null, EditorContext.class);
        EditorContext context = (EditorContext) l.get (0);
        return context;
    }
}
