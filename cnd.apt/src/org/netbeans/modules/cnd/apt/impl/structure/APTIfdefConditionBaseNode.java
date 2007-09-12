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
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * #ifndef/#ifdef directives base implementation
 * @author Vladimir Voskresensky
 */
public abstract class APTIfdefConditionBaseNode extends APTTokenAndChildBasedNode 
                                                implements Serializable {
    private static final long serialVersionUID = -5900095440680811076L;
    private Token macroName;
    private int endOffset;
    
    /** Copy constructor */
    /**package*/ APTIfdefConditionBaseNode(APTIfdefConditionBaseNode orig) {
        super(orig);
        this.macroName = orig.macroName;
    }
    
    /** Constructor for serialization */
    protected APTIfdefConditionBaseNode() {
    }
    
    /** Creates a new instance of APTIfdefConditionBaseNode */
    protected APTIfdefConditionBaseNode(Token token) {
        super(token);
    }

    public boolean accept(Token token) {
        /** base implementation of #ifdef/#ifndef */        
        if (APTUtils.isID(token)) {
            if (macroName != null) {
                // init macro name only once
                if (DebugUtils.STANDALONE) {
                    System.err.printf("line %d: extra tokens after %s at end of %s directive\n", // NOI18N
                            getToken().getLine(), macroName.getText(), getToken().getText().trim());
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "line {0}: extra tokens after {1} at end of {2} directive", // NOI18N
                            new Object[] {getToken().getLine(), macroName.getText(), getToken().getText().trim()} );
                }
            } else {
                this.macroName = token;
            }
        } else if (token.getType() == APTTokenTypes.DEFINED) {
            // "defined" cannot be used as a macro name
            if (DebugUtils.STANDALONE) {
                System.err.printf("line %d: \"defined\" cannot be used as a macro name\n", // NOI18N
                                    getToken().getLine());
            } else {
                APTUtils.LOG.log(Level.SEVERE, "line {0}: \"defined\" cannot be used as a macro name", // NOI18N
                        new Object[] {getToken().getLine()} );
            }            
        }
        // eat all till END_PREPROC_DIRECTIVE     
        if (APTUtils.isEndDirectiveToken(token.getType())) {
            endOffset = ((APTToken)token).getOffset();
            if (macroName == null) {
                if (DebugUtils.STANDALONE) {
                    System.err.printf("line %d: no macro name given in %s directive\n", // NOI18N
                        getToken().getLine(), getToken().getText().trim());
                } else {                
                    APTUtils.LOG.log(Level.SEVERE, "line {0}: no macro name given in {1} directive ", // NOI18N
                            new Object[] {getToken().getLine(), getToken().getText().trim()} );                
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public int getEndOffset() {
        return endOffset;
    }
    
    public String getText() {
        assert (getToken() != null) : "must have valid preproc directive"; // NOI18N
        // macro name could be null for incorrect constructions
        // assert (getMacroName() != null) : "must have valid macro"; // NOI18N
        String retValue = super.getText();
        if (getMacroName() != null) {
            retValue += " MACRO{" + getMacroName() + "}"; // NOI18N
        }
        return retValue;
    }

    /** base implementation for #ifdef/#ifndef */
    public Token getMacroName() {
        return macroName;
    }    

}
