/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.model;

import org.netbeans.modules.cnd.api.model.util.TypeSafeEnum;

/**
 * Common ancestor for all declarations
 * @author Vladimir Kvashin
 */

//TODO: FINISH!

public interface CsmDeclaration extends CsmQualifiedNamedElement, CsmObject, CsmScopeElement {

    //TODO: fill in accordance to C++ standard

    public class Kind extends TypeSafeEnum {

        private Kind(String id) {
            super(id);
        }

        public static final Kind BUILT_IN = new Kind("Built-in");

        public static final Kind CLASS = new Kind("class");
        public static final Kind UNION = new Kind("union");
        public static final Kind STRUCT = new Kind("struct");
        
        public static final Kind ENUM = new Kind("Enum");
        
        public static final Kind VARIABLE = new Kind("VARIABLE");
        
        public static final Kind FUNCTION = new Kind("FUNCTION");
        public static final Kind FUNCTION_DEFINITION = new Kind("FUNCTION_DEFINITION");
        
        public static final Kind TEMPLATE_SPECIALIZATION = new Kind("TEMPLATE_SPECIALIZATION");
        public static final Kind TYPEDEF = new Kind("TYPEDEF");
        public static final Kind ASM = new Kind("ASM");
        public static final Kind TEMPLATE_DECLARATION = new Kind("TEMPLATE_DECLARATION");
        public static final Kind NAMESPACE_DEFINITION = new Kind("NAMESPACE_DEFINITION");
        
        public static final Kind NAMESPACE_ALIAS = new Kind("NAMESPACE_ALIAS");
        public static final Kind USING_DIRECTIVE = new Kind("USING_DIRECTIVE");
        public static final Kind USING_DECLARATION = new Kind("USING_DECLARATION");
        
        public static final Kind CLASS_FORWARD_DECLARATION = new Kind("CLASS_FORWARD_DECLARATION");
    }
    
    Kind getKind();
    
    /**
     * Gets the name, which unequely identifies the given declaration
     * within a projec.
     * For classes, enums and variables such names equals to their qualified name;
     * for functions the signature should be added
     */
    String getUniqueName();
}
