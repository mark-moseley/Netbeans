/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

/**
 * Object wrapper which allows to assign a name to an object.
 */
public class NamedObject {
    
    /** name of the object */
    public String  name;
    /** object wrapper wrapped by this <code>NamedObject</code> */
    public Object  object;
    
    /**
     * Creates an instance of <code>NamedObject</code>
     *
     * @param  object  object to be wrapped by this object
     * @param  name    name of this object
     */
    public NamedObject(Object object, String name) {
        this.object = object;
        this.name = name;
    }
    
    /**
     * Returns a string representation of this object.
     *
     * @return  name of the object
     */
    public String toString() {
        return name;
    }
}
