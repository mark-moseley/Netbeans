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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;


/**
 * @author   Jan Jancura
 */
class FieldVariable extends AbstractVariable implements 
org.netbeans.api.debugger.jpda.Field {
        
    protected Field field;
    //private String className;
    private ObjectReference objectReference;
    

    FieldVariable (
        LocalsTreeModel model,
        Value value,
    //    String className,
        Field field,
        String parentID,
        ObjectReference objectReference
    ) {
        super (
            model, 
            value, 
            parentID + '.' + field.name () +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.field = field;
        //this.className = className;
        this.objectReference = objectReference;
    }

    FieldVariable (
        LocalsTreeModel model,
        Value value,
       // String className,
        Field field,
        String parentID,
        String genericSignature
    ) {
        super(model, value, genericSignature, parentID + '.' + field.name () +
                (value instanceof ObjectReference ? "^" : ""));
        this.field = field;
       // this.className = className;
    }
    
    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        return field.name ();
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        return field.declaringType ().name (); //className;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        return field.typeName ();
    }

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic () {
        return field.isStatic ();
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            objectReference.setValue (field, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }

    
    // other methods ...........................................................

    public String toString () {
        return "FieldVariable " + field.name ();
    }
}

