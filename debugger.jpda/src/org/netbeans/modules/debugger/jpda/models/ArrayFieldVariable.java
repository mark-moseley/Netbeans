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
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class ArrayFieldVariable extends AbstractVariable implements 
org.netbeans.api.debugger.jpda.Field {

    private final ArrayReference array;
    private int index;
    private int maxIndexLog;
    private String declaredType;

    ArrayFieldVariable (
        JPDADebuggerImpl debugger, 
        Value value,
        String declaredType,
        ArrayReference array,
        int index,
        int maxIndex,
        String parentID
    ) {
        super (
            debugger, 
            value, 
            parentID + '.' + index +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.index = index;
        this.maxIndexLog = log10(maxIndex);
        this.declaredType = declaredType;
        this.array = array;
    }

    
    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        int num0 = maxIndexLog - log10(index);
        if (num0 > 0) {
            return "[" + zeros(num0) + index + "]";
        } else {
            return "[" + index + "]";
        }
    }
    
    static int log10(int n) {
        int l = 1;
        while ((n = n / 10) > 0) l++;
        return l;
    }
    
    //private static final String ZEROS = "000000000000"; // NOI18N
    private static final String ZEROS = "            "; // NOI18N
    
    static String zeros(int n) {
        if (n < ZEROS.length()) {
            return ZEROS.substring(0, n);
        } else {
            String z = ZEROS;
            while (z.length() < n) z += " "; // NOI18N
            return z;
        }
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        return getType ();
    }

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic () {
        return false;
    }
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        return declaredType;
    }

    /**
     * Sets new value of this variable.
     * 
     * @param value ne value
     * @throws InvalidExpressionException if the value is invalid
     */ 
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            array.setValue(index, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }
    
    // other methods ...........................................................

    public String toString () {
        return "FieldVariable " + getName ();
    }
}

