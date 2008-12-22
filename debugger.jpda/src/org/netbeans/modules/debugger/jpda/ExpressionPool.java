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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import com.sun.jdi.VirtualMachine;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.openide.util.Exceptions;

/**
 * The pool of operations, which are used for expression stepping.
 * 
 * @author Martin Entlicher
 */
public class ExpressionPool {
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.step"); // NOI18N
    
    private Map<ExpressionLocation, Expression> expressions = new HashMap<ExpressionLocation, Expression>();
    
    /**
     * Creates a new instance of ExpressionPool
     */
    ExpressionPool() {
    }
    
    public synchronized Expression getExpressionAt(Location loc, String url) {
        try {
            ExpressionLocation exprLocation = new ExpressionLocation(LocationWrapper.method(loc), LocationWrapper.lineNumber(loc));
            if (!expressions.containsKey(exprLocation)) {
                Expression expr = createExpressionAt(loc, url);
                expressions.put(exprLocation, expr);
            }
            return expressions.get(exprLocation);
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }
    }
    
    // TODO: Clean unnecessray expressions:
    /*
    public synchronized void removeExpressionAt(Location loc) {
        expressions.remove(new ExpressionLocation(loc.method(), loc.lineNumber()));
    }
     */
    public void cleanUnusedExpressions(ThreadReference thr) {
        synchronized (this) {
            if (expressions.size() == 0) {
                return ;
            }
        }
        List<StackFrame> stackFrames;
        try {
            stackFrames = ThreadReferenceWrapper.frames(thr);
            synchronized (this) {
                for (Iterator<ExpressionLocation> locIt = expressions.keySet().iterator(); locIt.hasNext(); ) {
                    ExpressionLocation exprLoc = locIt.next();
                    // TODO: Check the correct thread.
                    Method method = exprLoc.getMethod();
                    //int line = exprLoc.getLine();
                    for (Iterator<StackFrame> it = stackFrames.iterator(); it.hasNext(); ) {
                        StackFrame sf = it.next();
                        if (method.equals(LocationWrapper.method(StackFrameWrapper.location(sf)))) {
                            //&& line == sf.location().lineNumber()) {
                            method = null;
                            break;
                        }
                    }
                    if (method != null) {
                        locIt.remove();
                    }
                }
            }
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
            // Ignore
        }
    }

    private Expression createExpressionAt(Location loc, String url) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        VirtualMachine vm = MirrorWrapper.virtualMachine(loc);
        if (!VirtualMachineWrapper.canGetBytecodes(vm)) {
            // Can not analyze expressions without bytecode
            return null;
        }
        ReferenceType clazzType = LocationWrapper.declaringType(loc);
        final Method method = LocationWrapper.method(loc);
        final byte[] bytecodes = MethodWrapper.bytecodes(method);
        byte[] constantPool = null;
        if (JPDAUtils.IS_JDK_16 && VirtualMachineWrapper.canGetConstantPool(vm)) {
            constantPool = ReferenceTypeWrapper.constantPool(clazzType);
        }
        final byte[] theConstantPool = constantPool;
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        final String language = currentSession == null ? null : currentSession.getCurrentLanguage();
        
        int line = LocationWrapper.lineNumber(loc, language);
        
        final List<Location> methodLocations;
        try {
            methodLocations = MethodWrapper.allLineLocations(method, language, null);
        } catch (AbsentInformationException aiex) {
            logger.log(Level.FINE, aiex.getLocalizedMessage());
            return null;
        }
        
        Operation[] ops = EditorContextBridge.getContext().getOperations(
                url, line, new EditorContext.BytecodeProvider() {
            public byte[] constantPool() {
                return theConstantPool;
            }

            public byte[] byteCodes() {
                return bytecodes;
            }

            public int[] indexAtLines(int startLine, int endLine) {
                return getIndexesAtLines(methodLocations, language, startLine, endLine, bytecodes.length);
            }
            
        });
        if (ops == null) {
            logger.log(Level.FINE, "Unsuccessfull bytecode matching.");
            return null;
        }
        if (ops.length == 0) { // No operations - do a line step instead
            return null;
        }
        Location[] locations = new Location[ops.length];
        for (int i = 0; i < ops.length; i++) {
            int codeIndex = ops[i].getBytecodeIndex();
            locations[i] = MethodWrapper.locationOfCodeIndex(method, codeIndex);
            if (locations[i] == null) {
                logger.log(Level.FINE, "Location of the operation not found.");
                return null;
            }
        }
        Expression expr = new Expression(new ExpressionLocation(method, line), ops, locations);
        return expr;
    }
    
    private static int[] getIndexesAtLines(List<Location> allLocations, String language, int startLine, int endLine, int methodEndIndex) {
        Location startLocation;
        int startlocline = 0;
        int endlocline;
        int firstLine;
        try {
            firstLine = LocationWrapper.lineNumber(allLocations.get(0), language);
            do {
                startLocation = getLocationOfLine(allLocations, language, startLine - startlocline++);
            } while (startLocation == null && (startLine - (startlocline - 1)) >= firstLine);
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        } catch (InternalExceptionWrapper e) {
            return null;
        }
        if (endLine > startLine - (startlocline - 1)) {
            endlocline = 0;
        } else {
            endlocline = 1;
        }
        startLine -= (startlocline - 1);
        endLine += endlocline;
        List<int[]> indexes = new ArrayList<int[]>();
        int startIndex = -1;
        try {
            for (Location l : allLocations) {
                int line = LocationWrapper.lineNumber(l, language);
                if (startIndex == -1 && startLine <= line && line < endLine) {
                    startIndex = (int) LocationWrapper.codeIndex(l);
                } else if (startIndex >= 0) {
                    indexes.add(new int[] { startIndex, (int) LocationWrapper.codeIndex(l) });
                    startIndex = -1;
                }
            }
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        } catch (InternalExceptionWrapper e) {
            return null;
        }
        if (indexes.size() == 0) {
            if (startIndex >= 0) {
                // End of the method
                return new int[] { startIndex, methodEndIndex };
            }
            return null;
        } else if (indexes.size() == 1) {
            return indexes.get(0);
        } else {
            int[] arr = new int[2*indexes.size()];
            for (int i = 0; i < indexes.size(); i++) {
                arr[2*i] = indexes.get(i)[0];
                arr[2*i + 1] = indexes.get(i)[1];
            }
            return arr;
        }
    }
    
    private static Location getLocationOfLine(List<Location> allLocations, String language, int line) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        for (Location l : allLocations) {
            if (LocationWrapper.lineNumber(l, language) == line) {
                return l;
            }
        }
        return null;
    }
    
    //private int[] singleIndexHolder = new int[1]; // Perf. optimization only
    
    public static final class Expression {
        
        private ExpressionLocation location;
        private Operation[] operations;
        private Location[] locations;
        
        Expression(ExpressionLocation location, Operation[] operations, Location[] locations) {
            this.location = location;
            this.operations = operations;
            this.locations = locations;
        }
        
        public Operation[] getOperations() {
            return operations;
        }
        
        public Location[] getLocations() {
            return locations;
        }
        
        public int findNextOperationIndex(int codeIndex) {
            for (int i = 0; i < operations.length; i++) {
                int operationIndex = operations[i].getBytecodeIndex();
                if (operationIndex > codeIndex) {
                    return i;
                }
            }
            return -1;
        }
        
        int[] findNextOperationIndexes(int codeIndex) {
            for (int i = 0; i < operations.length; i++) {
                int operationIndex = operations[i].getBytecodeIndex();
                if (operationIndex == codeIndex) {
                    List<Operation> nextOperations = operations[i].getNextOperations();
                    if (!nextOperations.isEmpty()) {
                        int l = nextOperations.size();
                        int[] indexes = new int[l];
                        for (int ni = 0; ni < l; ni++) {
                            Operation op = nextOperations.get(ni);
                            int j;
                            for (j = 0; j < operations.length; j++) {
                                if (op == operations[j]) break;
                            }
                            if (j < operations.length) {
                                indexes[ni] = j;
                            } else {
                                indexes[ni] = -1;
                            }
                        }
                        return indexes;
                    }
                }
                if (operationIndex > codeIndex) {
                    return new int[] { i };
                }
            }
            return null;
        }
        
        OperationLocation[] findNextOperationLocations(int codeIndex) {
            for (int i = 0; i < operations.length; i++) {
                int operationIndex = operations[i].getBytecodeIndex();
                if (operationIndex == codeIndex) {
                    List<Operation> nextOperations = operations[i].getNextOperations();
                    if (!nextOperations.isEmpty()) {
                        int l = nextOperations.size();
                        OperationLocation[] opLocations = new OperationLocation[l];
                        for (int ni = 0; ni < l; ni++) {
                            Operation op = nextOperations.get(ni);
                            int j;
                            for (j = 0; j < operations.length; j++) {
                                if (op == operations[j]) break;
                            }
                            if (j < operations.length) {
                                opLocations[ni] = //locations[j];
                                        new OperationLocation(operations[j], locations[j], j);
                            } else {
                                int ci = op.getBytecodeIndex();
                                Location loc;
                                try {
                                    loc = MethodWrapper.locationOfCodeIndex(location.getMethod(), ci);
                                } catch (InternalExceptionWrapper ex) {
                                    return null;
                                } catch (VMDisconnectedExceptionWrapper ex) {
                                    return null;
                                }
                                if (loc == null) {
                                    logger.log(Level.FINE, "Location of the operation not found.");
                                    return null;
                                }
                                opLocations[ni] = //loc;
                                        new OperationLocation(op, loc, -1);
                            }
                        }
                        return opLocations;
                    }
                }
                if (operationIndex > codeIndex) {
                    return new OperationLocation[] { new OperationLocation(
                                operations[i],
                                locations[i],
                                i
                            ) };
                }
            }
            return null;
        }
        
    }

    public static final class ExpressionLocation {

        private Method method;
        private int line;

        public ExpressionLocation(Method method, int line) {
            this.method = method;
            this.line = line;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public int getLine() {
            return line;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ExpressionLocation)) {
                return false;
            }
            return ((ExpressionLocation) obj).line == line && ((ExpressionLocation) obj).method.equals(method);
        }

        public int hashCode() {
            return method.hashCode() + line;
        }

    }
    
    public static final class OperationLocation {
        
        private Operation op;
        private Location loc;
        private int index;
        
        OperationLocation(Operation op, Location loc, int index) {
            this.op = op;
            this.loc = loc;
            this.index = index;
        }

        public Operation getOperation() {
            return op;
        }

        public Location getLocation() {
            return loc;
        }
        
        public int getIndex() {
            return index;
        }

    }
        
}
