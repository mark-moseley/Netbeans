/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import java.beans.*;

/**
 * @author  phrebejk
 */
public class Bean21341Hidden extends Object implements java.io.Serializable {
    
    /** Holds value of property indexedProperty. */
    private String[] indexedProperty;    
    
    /** Creates new BadBeanHidden */
    public Bean21341Hidden() {
        
    }
    
    /** Indexed getter for property indexedProperty.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public String getIndexedProperty(int index) {
        return this.indexedProperty[index];
    }    
    
    /** Getter for property karel.
     * @return Value of property karel.
     */
    public int getKarel() {
        return 3;
    }
    
    /** Setter for property karel.
     * @param karel New value of property karel.
     */
    public void setKarel(int karel) {
    }
    
    /** Indexed setter for property indexedProperty.
     * @param index Index of the property.
     * @param indexedProperty New value of the property at <CODE>index</CODE>.
     
    public void setIndexedProperty(int index, String indexedProperty) {
        this.indexedProperty[index] = indexedProperty;
    }
    */    
    
}
