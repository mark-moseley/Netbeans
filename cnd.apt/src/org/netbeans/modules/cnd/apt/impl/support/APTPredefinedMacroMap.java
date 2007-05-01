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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import antlr.TokenStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.TokenBasedTokenStream;

/**
 *
 * @author alms
 */
public class APTPredefinedMacroMap implements APTMacroMap {
       
    private static String []preMacro = new String [] { 
         "__FILE__", "__LINE__", "__DATE__", "__TIME__"  // NOI18N
    };
    
    public APTPredefinedMacroMap() {
    }

    public APTMacroMap.State getState() {
        return null;
    }
   
    public boolean isDefined(Token token) {
        int i;
        String tokenText = token.getText();
        
        if (tokenText.length() < 2 || tokenText.charAt(0) != '_' || tokenText.charAt(1) != '_') {
            return false;
        }
                    
        for (i = 0; i < preMacro.length; i++) {
            if(preMacro[i].equals(tokenText)) {
                return true;
            }                
        }        
        return false;
    }

    public APTMacro getMacro(Token token) { 
        if (isDefined(token)) {
            return new APTPredefinedMacroImpl(token);        
        }
        return null;
    }
    

    public void setState(APTMacroMap.State state) {
        APTUtils.LOG.log(Level.SEVERE, "setState is not supported", new IllegalAccessException()); // NOI18N
    }

    public void define(Token name, List value) {
        APTUtils.LOG.log(Level.SEVERE, "define is not supported", new IllegalAccessException()); // NOI18N
    }

    public void define(Token name, Collection params, List value) {
        APTUtils.LOG.log(Level.SEVERE, "define is not supported", new IllegalAccessException()); // NOI18N
    }

    public void undef(Token name) {
        APTUtils.LOG.log(Level.SEVERE, "undef is not supported", new IllegalAccessException()); // NOI18N
    }
   
    protected APTMacro createMacro(Token name, Collection params, List/*<Token>*/ value) {
        return new APTMacroImpl(name, params, value, true);
    }
    
    public boolean pushExpanding(Token token) {
        APTUtils.LOG.log(Level.SEVERE, "pushExpanding is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }

    public void popExpanding() {
        APTUtils.LOG.log(Level.SEVERE, "popExpanding is not supported", new IllegalAccessException()); // NOI18N
    }

    public boolean isExpanding(Token token) {
        APTUtils.LOG.log(Level.SEVERE, "isExpanding is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }     
    
    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        return new APTMacroMapSnapshot(parent);
    }
    
    
    private static class APTPredefinedMacroImpl implements APTMacro {   
        private Token macro;
        
        public APTPredefinedMacroImpl(Token macro) {
            this.macro =  macro;           
        }

        public boolean isSystem() {
            return false;
        }

        public boolean isFunctionLike() {
            return false;
        }

        public Token getName() {            
            return macro;
        }

        public Collection getParams() {
            return Collections.EMPTY_LIST;
        }

        public TokenStream getBody() {
            Token tok = APTUtils.createAPTToken(macro, APTTokenTypes.STRING_LITERAL);            
            
            if (!macro.getText().equals("__LINE__")) { // NOI18N
                tok.setType(APTTokenTypes.STRING_LITERAL);
            }
            else {
                tok.setType(APTTokenTypes.DECIMALINT);
                tok.setText("" + macro.getLine()); // NOI18N
            }
                        
            return new TokenBasedTokenStream(tok);
        }    
        
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("<P>"); // NOI18N     
            retValue.append(getName());                       
            return retValue.toString(); 
        }
    }
    
}
