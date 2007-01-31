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
import org.netbeans.modules.cnd.apt.structure.APTUnknown;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * impl for unknown #-directive with it's content till end of line
 * @author Vladimir Voskresensky
 */
public final class APTUnknownNode extends APTStreamBaseNode 
                                    implements APTUnknown, Serializable {
    private static final long serialVersionUID = -6159626009326550770L;
    
    /** Copy constructor */
    /**package*/ APTUnknownNode(APTUnknownNode orig) {
        super(orig);
        assert (false) : "are you sure it's correct to make copy of unknown node?"; // NOI18N
    }
    
    /** constructor for serialization **/
    protected APTUnknownNode() {
    }
    
    /**
     * Creates a new instance of APTUnknownNode
     */
    public APTUnknownNode(Token token) {
        super(token);
    }
    
    public final int getType() {
        return APT.Type.PREPROC_UNKNOWN;
    }
    
    protected boolean validToken(Token t) {
        assert (t != null);
        int ttype = t.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE
        return !APTUtils.isEndDirectiveToken(ttype);
    }    
}
