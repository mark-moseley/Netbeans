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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTInclude;

/**
 * #include directive implementation
 * @author Vladimir Voskresensky
 */
public final class APTIncludeNode extends APTIncludeBaseNode 
                                    implements APTInclude, Serializable {
    private static final long serialVersionUID = 4130883993489751800L;
    
    /** Copy constructor */
    /**package*/ APTIncludeNode(APTIncludeNode orig) {
        super(orig);
    }
    
    /** Constructor for serialization */
    protected APTIncludeNode() {
    }
    
    /** Creates a new instance of APTIncludeNode */
    public APTIncludeNode(Token token) {
        super(token);
    }    
    
    public final int getType() {
        return APT.Type.INCLUDE;
    }
}
