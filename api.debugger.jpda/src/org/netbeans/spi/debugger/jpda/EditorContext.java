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
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2007 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
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
package org.netbeans.spi.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.Variable;

/**
 * Defines bridge to editor and src hierarchy. It allows to use different
 * source viewer for debugger (like some UML view).
 *
 * @author Jan Jancura
 */
public abstract class EditorContext {

    /** Annotation type constant. */
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE = "DisabledBreakpoint";
    /** Annotation type constant. */
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "CondBreakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "DisabledCondBreakpoint";
    /** Annotation type constant. */
    public static final String FIELD_BREAKPOINT_ANNOTATION_TYPE = "FieldBreakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE = "DisabledFieldBreakpoint";
    /** Annotation type constant. */
    public static final String METHOD_BREAKPOINT_ANNOTATION_TYPE = "MethodBreakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE = "DisabledMethodBreakpoint";
    /** Annotation type constant. */
    public static final String CURRENT_LINE_ANNOTATION_TYPE = "CurrentPC";
    /** Annotation type constant. */
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE = "CallSite";
    /** Annotation type constant. */
    public static final String CURRENT_LAST_OPERATION_ANNOTATION_TYPE = "LastOperation";
    /** Annotation type constant. */
    public static final String CURRENT_OUT_OPERATION_ANNOTATION_TYPE = "StepOutOperation";
    /** Annotation type constant. */
    public static final String CURRENT_EXPRESSION_SECONDARY_LINE_ANNOTATION_TYPE = "CurrentExpression";
    /** Annotation type constant. */
    public static final String CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE = "CurrentExpressionLine";
    /** Annotation type constant. */
    public static final String OTHER_THREAD_ANNOTATION_TYPE = "OtherThread";

    /** Property name constant. */
    public static final String PROP_LINE_NUMBER = "lineNumber";


    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    public abstract boolean showSource (
        String url,
        int lineNumber, 
        Object timeStamp
    );

    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    public abstract void createTimeStamp (Object timeStamp);

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    public abstract void disposeTimeStamp (Object timeStamp);
    
    /**
     * Updates timeStamp for gived url.
     *
     * @param timeStamp time stamp to be updated
     * @param url an url
     */
    public abstract void updateTimeStamp (Object timeStamp, String url);

    /**
     * Adds annotation to given url on given line.
     *
     * @param url a url of source annotation should be set into
     * @param lineNumber a number of line annotation should be set into
     * @param annotationType a type of annotation to be set
     * @param timeStamp a time stamp to be used
     *
     * @return annotation or <code>null</code>, when the annotation can not be
     *         created at the given URL or line number.
     */
    public abstract Object annotate (
        String url, 
        int lineNumber, 
        String annotationType,
        Object timeStamp
    );

    public Object annotate (
        String url, 
        int lineNumber, 
        String annotationType,
        Object timeStamp,
        JPDAThread thread
    ) {
        return null;
    }

    /**
     * Adds annotation to given url on given character range.
     *
     * @param url a url of source annotation should be set into
     * @param startPosition the offset of the starting position of the annotation
     * @param endPosition the offset of the ending position of the annotation
     * @param annotationType a type of annotation to be set

     * @return annotation or <code>null</code>, when the annotation can not be
     *         created at the given URL or line number.
     */
    public Object annotate (
        String url, 
        int startPosition, 
        int endPosition, 
        String annotationType,
        Object timeStamp
    ) {
        return null;
    }

    /**
     * Returns line number given annotation is associated with.
     *
     * @param annotation a annotation
     * @param timeStamp a time stamp to be used
     *
     * @return line number given annotation is associated with
     */
    public abstract int getLineNumber (
        Object annotation,
        Object timeStamp
    );

    /**
     * Removes given annotation.
     */
    public abstract void  removeAnnotation (
        Object annotation
    );

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public abstract int getCurrentLineNumber ();

    /**
     * Returns name of class currently selected in editor or empty string.
     *
     * @return name of class currently selected in editor or empty string
     */
    public abstract String getCurrentClassName ();

    /**
     * Returns URL of source currently selected in editor or empty string.
     *
     * @return URL of source currently selected in editor or empty string
     */
    public abstract String getCurrentURL ();

    /**
     * Returns name of method currently selected in editor or empty string.
     *
     * @return name of method currently selected in editor or empty string
     */
    public abstract String getCurrentMethodName ();

    /**
     * Returns name of field currently selected in editor or <code>null</code>.
     *
     * @return name of field currently selected in editor or <code>null</code>
     */
    public abstract String getCurrentFieldName ();

    /**
     * Returns identifier currently selected in editor or <code>null</code>.
     *
     * @return identifier currently selected in editor or <code>null</code>
     */
    public abstract String getSelectedIdentifier ();

    /**
     * Returns method name currently selected in editor or empty string.
     *
     * @return method name currently selected in editor or empty string
     */
    public abstract String getSelectedMethodName ();
    
    /**
     * Returns line number of given field in given class.
     *
     * @param url the url of source file the class is deined in
     * @param className the name of class (or innerclass) the field is 
     *                  defined in
     * @param fieldName the name of field
     *
     * @return line number or -1
     */
    public abstract int getFieldLineNumber (
        String url, 
        String className, 
        String fieldName
    );
    
    /**
     * Returns line number of given method in given class.
     *
     * @param url the url of source file the class is deined in
     * @param className the name of class (or innerclass) the method is 
     *                  defined in
     * @param methodName the name of the method
     * @param methodSignature the JNI-style signature of the method.
     *        If <code>null</code>, then the first method found is returned.
     *
     * @return line number or -1
     */
    public int getMethodLineNumber (
        String url, 
        final String className, 
        final String methodName,
        final String methodSignature
    ) {
        return -1;
    }
    
    
    /**
     * Returns name and signature of method declaration currently selected in editor,
     * or <code>null</code>.
     *
     * @return name and signature of the method, or <code>null</code>.
     */
    public String[] getCurrentMethodDeclaration() {
        return null;
    }

    
    /**
     * Returns class name for given url and line number or null.
     *
     * @param url a url
     * @param lineNumber a line number
     *
     * @return class name for given url and line number or null
     */
    public abstract String getClassName (
        String url, 
        int lineNumber
    );
    
    /**
     * Returns list of imports for given source url.
     *
     * @param url the url of source file
     *
     * @return list of imports for given source url
     */
    public abstract String[] getImports (String url);
    
    /**
     * Creates an operation which is determined by starting and ending position.
     *
    protected final Operation createOperation(Position startPosition,
                                              Position endPosition,
                                              int bytecodeIndex) {
        return new Operation(startPosition, endPosition, bytecodeIndex);
    }
     */
    
    /**
     * Creates a method operation.
     * @param startPosition The starting position of the operation
     * @param endPosition The ending position of the operation
     * @param methodStartPosition The starting position of the method name
     * @param methodEndPosition The ending position of the method name
     * @param methodName The string representation of the method name
     * @param methodClassType The class type, which defines this method
     * @param bytecodeIndex The bytecode index of this method call
     */
    protected final Operation createMethodOperation(Position startPosition,
                                                    Position endPosition,
                                                    Position methodStartPosition,
                                                    Position methodEndPosition,
                                                    String methodName,
                                                    String methodClassType,
                                                    int bytecodeIndex) {
        return new Operation(startPosition, endPosition,
                             methodStartPosition, methodEndPosition,
                             methodName, methodClassType, bytecodeIndex);
    }
    
    /**
     * Assign a next operation, concatenates operations.
     * @param operation The first operation
     * @param next The next operation
     */
    protected final void addNextOperationTo(Operation operation, Operation next) {
        operation.addNextOperation(next);
    }
    
    /**
     * Creates a new {@link Position} object.
     * @param offset The offset
     * @param line The line number
     * @param column The column number
     */
    protected final Position createPosition(
            int offset, int line, int column) {
        
        return new Position(offset, line, column);
    }
    
    /**
     * Get the list of operations that are in expression(s) located at the given line.
     * @param url The file's URL
     * @param lineNumber The line number
     * @param bytecodeProvider The provider of method bytecodes.
     */
    public Operation[] getOperations(String url, int lineNumber,
                                     BytecodeProvider bytecodeProvider) {
        throw new UnsupportedOperationException("This method is not implemented.");
    }
    
    /**
     * Get a list of arguments to the given operation.
     * @param url The URL of the source file with the operation
     * @param operation The operation
     */
    public MethodArgument[] getArguments(String url, Operation operation) {
        throw new UnsupportedOperationException("This method is not implemented.");
    }
    
    /**
     * Get a list of arguments passed to method located at the given line.
     * @param url The URL of the source file
     * @param methodLineNumber The line number of the method header
     */
    public MethodArgument[] getArguments(String url, int methodLineNumber) {
        throw new UnsupportedOperationException("This method is not implemented.");
    }
    
    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);
    
    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
    
    /**
     * Adds a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    );
    
    /**
     * Removes a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    );
    
    /**
     * A provider of method bytecode information.
     */
    public interface BytecodeProvider {
        
        /**
         * Retrieve the class' constant pool.
         */
        byte[] constantPool();
        
        /**
         * Retrieve the bytecodes of the method.
         */
        byte[] byteCodes();
        
        /**
         * Get an array of bytecode indexes of operations between the starting
         * and ending line.
         * @param startLine The starting line
         * @param endLine The ending line
         */
        int[] indexAtLines(int startLine, int endLine);
        
    }
    
    /**
     * The operation definition.
     */
    public static final class Operation {
        
        private final Position startPosition;
        private final Position endPosition;
        private final int bytecodeIndex;

        private Position methodStartPosition;
        private Position methodEndPosition;
        private String methodName;
        private String methodClassType;
        private Variable returnValue;
        
        private List<Operation> nextOperations;
        
        /*
        Operation(Position startPosition, Position endPosition,
                  int bytecodeIndex) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.bytecodeIndex = bytecodeIndex;
        }
         */
        
        /**
         * Creates a new method operation.
         */
        Operation(Position startPosition, Position endPosition,
                  Position methodStartPosition, Position methodEndPosition,
                  String methodName, String methodClassType,
                  int bytecodeIndex) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.bytecodeIndex = bytecodeIndex;
            this.methodStartPosition = methodStartPosition;
            this.methodEndPosition = methodEndPosition;
            this.methodName = methodName;
            this.methodClassType = methodClassType;
        }
        
        synchronized void addNextOperation(Operation next) {
            if (nextOperations == null) {
                nextOperations = new ArrayList<Operation>();
            }
            nextOperations.add(next);
        }
        
        /**
         * Get the starting position of this operation.
         */
        public Position getStartPosition() {
            return startPosition;
        }
        
        /**
         * Get the ending position of this operation.
         */
        public Position getEndPosition() {
            return endPosition;
        }
        
        /**
         * Get the starting position of the method call of this operation.
         */
        public Position getMethodStartPosition() {
            return methodStartPosition;
        }

        /**
         * Get the ending position of the method call of this operation.
         */
        public Position getMethodEndPosition() {
            return methodEndPosition;
        }

        /**
         * Get the method name.
         */
        public String getMethodName() {
            return methodName;
        }
        
        /**
         * Get the class type declaring the method.
         */
        public String getMethodClassType() {
            return methodClassType;
        }
        
        /**
         * Get the bytecode index of this operation.
         */
        public int getBytecodeIndex() {
            return bytecodeIndex;
        }

        /**
         * Set the return value of this operation.
         */
        public void setReturnValue(Variable returnValue) {
            this.returnValue = returnValue;
        }
        
        /**
         * Get the return value of this operation.
         */
        public Variable getReturnValue() {
            return returnValue;
        }
        
        /**
         * Get the list of following operations.
         */
        public List<Operation> getNextOperations() {
            if (nextOperations == null) {
                return Collections.emptyList();
            } else {
                synchronized (this) {
                    return Collections.unmodifiableList(nextOperations);
                }
            }
        }

        public boolean equals(Object obj) {
            if (obj instanceof Operation) {
                Operation op2 = (Operation) obj;
                return bytecodeIndex == op2.bytecodeIndex &&
                        ((startPosition == null) ?
                            op2.startPosition == null :
                            startPosition.equals(op2.startPosition)) &&
                        ((endPosition == null) ?
                            op2.endPosition == null :
                            endPosition.equals(op2.endPosition));
            }
            return false;
        }

        public int hashCode() {
            return bytecodeIndex;
        }

    }
    
    /**
     * Representation of a position in a source code.
     */
    public static final class Position {

        private final int offset;
        private final int line;
        private final int column;

        Position(int offset, int line, int column) {
            this.offset = offset;
            this.line = line;
            this.column = column;
        }

        /**
         * Get the offset of this position.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * Get the line number of this position.
         */
        public int getLine() {
            return line;
        }

        /**
         * Get the column number of this position.
         */
        public int getColumn() {
            return column;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Position) {
                Position pos = (Position) obj;
                return pos.offset == offset;
            }
            return false;
        }

        public int hashCode() {
            return offset;
        }
        
    }
    
    /**
     * Representation of an argument to a method.
     * @since 2.10
     */
    public static final class MethodArgument {
        
        private String name;
        private String type;
        private Position startPos;
        private Position endPos;
        
        /**
         * Creates a new argument.
         * @param name The argument name
         * @param type The declared type of the argument
         * @param startPos Starting position of the argument in the source code
         * @param endPos Ending position of the argument in the source code
         */
        public MethodArgument(String name, String type, Position startPos, Position endPos) {
            this.name = name;
            this.type = type;
            startPos = startPos;
            endPos = endPos;
        }
        
        /**
         * Get the name of this argument.
         * @return The name.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get the declared type of this argument.
         * @return The declared type.
         */
        public String getType() {
            return type;
        }
        
        /**
         * Get the starting position of this argument in the source code.
         * @return The starting position.
         */
        public Position getStartPosition() {
            return startPos;
        }
        
        /**
         * Get the ending position of this argument in the source code.
         * @return The ending position.
         */
        public Position getEndPosition() {
            return endPos;
        }
        
    }

}

