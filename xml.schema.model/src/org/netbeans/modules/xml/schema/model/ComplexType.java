/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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


package org.netbeans.modules.xml.schema.model;

import java.util.Collection;

/**
 * This interface represents the common capabilities between global complex
 * types and locally defined complex types.
 * @author Chris Webster
 */
public interface ComplexType extends LocalAttributeContainer {
    
    public static final String MIXED_PROPERTY = "mixed";
    public static final String DEFINITION_PROPERTY = "definition"; 
    
    Boolean isMixed();
    void setMixed(Boolean mixed);
    boolean getMixedDefault();
    boolean getMixedEffective();
    
    ComplexTypeDefinition getDefinition();
    void setDefinition(ComplexTypeDefinition content);
}
