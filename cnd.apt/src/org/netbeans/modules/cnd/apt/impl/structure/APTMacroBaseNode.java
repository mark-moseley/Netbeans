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
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base class for #define/#undef impl
 * @author Vladimir Voskresensky
 */
public abstract class APTMacroBaseNode extends APTTokenBasedNode
                                        implements Serializable {
    private static final long serialVersionUID = 1315417078059538898L;
    private Token macroName = EMPTY_NAME;
    
    /** Copy constructor */
    /**package*/APTMacroBaseNode(APTMacroBaseNode orig) {
        super(orig);
        this.macroName = orig.macroName;
    }
    
    /** Constructor for serialization **/
    protected APTMacroBaseNode() {
    }
    
    /** Creates a new instance of APTMacroBaseNode */
    public APTMacroBaseNode(Token token) {
        super(token);
    }

    public APT getFirstChild() {
        // #define/#undef doesn't have subtree
        return null;
    }

    public void setFirstChild(APT child) {
        // do nothing
        assert (false) : "define/undef doesn't support children"; // NOI18N        
    }

    public boolean accept(Token token) {
        if (APTUtils.isID(token)) {
            if (macroName != EMPTY_NAME) {
                // init macro name only once
                if (DebugUtils.STANDALONE) {
                    System.err.printf("line %d: warning: extra tokens at end of %s directive\n", // NOI18N
                            getToken().getLine(), getToken().getText().trim());
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "line {0}: warning: extra tokens at end of {1} directive", // NOI18N
                            new Object[] {getToken().getLine(), getToken().getText().trim()} );
                }
            } else {
                this.macroName = token;
            }
        }
        // eat all till END_PREPROC_DIRECTIVE
        return !APTUtils.isEndDirectiveToken(token.getType());
    }

    public String getText() {
        assert (getToken() != null) : "must have valid preproc directive"; // NOI18N
        assert (getName() != null) : "must have valid macro"; // NOI18N
        String retValue = super.getText();
        if (getName() != null) {
            retValue += " MACRO{" + getName() + "}"; // NOI18N
        }
        return retValue;
    }
    
    public Token getName() {
        return macroName;
    }
    
    private static final NotHandledMacroName EMPTY_NAME = new NotHandledMacroName();
    
    //TODO: what about Serializable
    private static class NotHandledMacroName extends APTTokenAbstact {
        public NotHandledMacroName() {
        }
        
        public String getText() {
            return "<<DUMMY>>"; // NOI18N
        }        
    };    
}
