/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.java.source;

import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.ElementHandle;

/**
 *
 * @author Tomas Zezula
 */
public abstract class ElementHandleAccessor {

    public static ElementHandleAccessor INSTANCE;

    static {
        Class c = ElementHandle.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Creates a new instance of ElementHandleAccessor */
    protected ElementHandleAccessor() {
    }
    
    
    public abstract ElementHandle create (ElementKind kind, String... descriptors);
        
    
}
