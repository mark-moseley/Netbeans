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

package org.netbeans.modules.beans;


/** Property editor for the listener type property
*
* @author Martin Matula
*/
public final class EventTypeEditor extends PropertyTypeEditor {

    /**
    * @return The tag values for this property.
    */
    public String[] getTags () {
        return EventSetPattern.WELL_KNOWN_LISTENERS;
    }
}
