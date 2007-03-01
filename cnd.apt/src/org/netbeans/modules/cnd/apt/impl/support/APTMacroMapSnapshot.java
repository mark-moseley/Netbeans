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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import antlr.TokenStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 * @author gorrus
 */
public final class APTMacroMapSnapshot {
    protected final Map<String/*getTokenTextKey(token)*/, APTMacro> macros = new HashMap<String, APTMacro>();
    protected final APTMacroMapSnapshot parent;

    public APTMacroMapSnapshot(APTMacroMapSnapshot parent) {
        this.parent = parent;
    }
    
    public final APTMacro getMacro(Token token) {
        Object key = APTUtils.getTokenTextKey(token);
        APTMacroMapSnapshot currentSnap = this;
        while (currentSnap != null) {
            APTMacro macro = currentSnap.macros.get(key);
            if (macro != null) {
                // If UNDEFINED_MACRO is found then the requested macro is undefined, return null
                return (macro != UNDEFINED_MACRO) ? (APTMacro)macro : null;
            }
            currentSnap = currentSnap.parent;
        }
        return null;
    }
    
    public String toString() {
        Map tmpMap = new HashMap();
        addAllMacros(this, tmpMap);
        return APTUtils.macros2String(tmpMap);
    }
    
    public static void addAllMacros(APTMacroMapSnapshot snap, Map out) {
        if (snap.parent != null) {
            addAllMacros(snap.parent, out);
        }
        for (Iterator iter=snap.macros.entrySet().iterator(); iter.hasNext();) {
            Map.Entry cur = (Map.Entry)iter.next();
            if (cur.getValue() != UNDEFINED_MACRO) {
                out.put(cur.getKey(), cur.getValue());
            } else {
                out.remove(cur.getKey());
            }
        }
    }    
    
    public boolean isEmtpy() {
        return macros.isEmpty();
    }

    ////////////////////////////////////////////////////////////////////////////
    // persistence support
    
    public void write(DataOutput output) throws IOException {
        APTSerializeUtils.writeSnapshot(this.parent, output);
        APTSerializeUtils.writeStringToMacroMap(this.macros, output);
    }
    
    public APTMacroMapSnapshot(DataInput input) throws IOException {
        this.parent = APTSerializeUtils.readSnapshot(input);
        APTSerializeUtils.readStringToMacroMap(this.macros, input);
    }  
        
    //This is a single instance of a class to indicate that macro is undefined,
    //not a child of APTMacro to track errors more easily
    public static final UndefinedMacro UNDEFINED_MACRO = new UndefinedMacro();
    private static class UndefinedMacro implements APTMacro {
        public String toString() {
            return "Macro undefined"; // NOI18N
        }

        public boolean isSystem() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        public boolean isFunctionLike() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        public Token getName() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        public Collection getParams() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        public TokenStream getBody() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }
    }
}
