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

package org.netbeans.modules.beans;

/** Names of properties of patterns.
*
*
* @author Petr Hrebejk
*/
public interface PatternProperties {
    /** Name of type property for all {@link PropertyPattern}s.
    */
    public static final String PROP_TYPE = "type"; // NOI18N

    public static final String PROP_MODE = "mode"; // NOI18N

    public static final String PROP_NAME = "name"; // NOI18N

    public static final String PROP_GETTER = "getter"; // NOI18N

    public static final String PROP_SETTER = "setter"; // NOI18N

    public static final String PROP_ESTIMATEDFIELD = "estimatedField"; // NOI18N

    public static final String PROP_INDEXEDTYPE = "indexedType"; // NOI18N

    public static final String PROP_INDEXEDGETTER = "indexedGetter"; // NOI18N

    public static final String PROP_INDEXEDSETTER = "indexedSetter"; // NOI18N

    public static final String PROP_ADDLISTENER = "addListener"; // NOI18N

    public static final String PROP_REMOVELISTENER = "removeListener"; // NOI18N

    public static final String PROP_ISUNICAST = "isUnicast"; // NOI18N
}
