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

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.ObjectVariable;


/**
 * @author   Jan Jancura
 */
public class ObjectFieldVariable extends FieldVariable 
implements ObjectVariable {
        
    ObjectFieldVariable (
        LocalsTreeModel model, 
        ObjectReference value, 
        String className,
        Field field,
        String parentID
    ) {
        super (
            model, 
            value, 
            className,
            field, 
            parentID
        );
    }

    
    // other methods ...........................................................

    public String toString () {
        return "ObjectFieldVariable " + field.name ();
    }
}
