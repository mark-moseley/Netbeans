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

/**
 * base implementation for APTContainer
 * - manage children
 * @author Vladimir Voskresensky
 */
public abstract class APTContainerNode extends APTBaseNode implements Serializable {
    private static final long serialVersionUID = 8306600642459659574L;
    transient private APT child;   

    /** Copy constructor */
    /**package*/ APTContainerNode(APTContainerNode orig) {
        super(orig);
        // clear tree structure information
        this.child = null;
    }
    
    protected APTContainerNode() {
    }
    
    public APT getFirstChild() {
        return child;
    }     

    public boolean accept(Token t) {
        return false;
    }    
    
    public Token getToken() {
        return null;
    } 
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    public final void setFirstChild(APT child) {
        assert (child != null) : "why added null child?"; // NOI18N
        assert (this.child == null) : "why do you change immutable APT?"; // NOI18N
        this.child = child;
    }    
}
