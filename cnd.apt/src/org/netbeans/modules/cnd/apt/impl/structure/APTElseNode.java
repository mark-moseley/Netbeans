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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTElse;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * #else directive implementation
 * @author Vladimir Voskresensky
 */
public final class APTElseNode extends APTTokenAndChildBasedNode 
                                implements APTElse, Serializable {
    private static final long serialVersionUID = 8350237885177577182L;
    
    /** Copy constructor */
    /**package*/APTElseNode(APTElseNode orig) {
        super(orig);
    }
    
    /** Constructor for serialization */
    protected APTElseNode() {
    }
    
    /** Creates a new instance of APTElseNode */
    public APTElseNode(Token token) {
        super(token);
    }
    
    public final int getType() {
        return APT.Type.ELSE;
    }
    
    public boolean accept(Token token) {
        assert (token != null);
        int ttype = token.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE
        return !APTUtils.isEndDirectiveToken(ttype);
    }
    
}
