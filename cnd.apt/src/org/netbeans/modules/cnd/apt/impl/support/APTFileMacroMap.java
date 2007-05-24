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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * macro map is created for each translation unit and
 * it has specified system predefined map where it delegates
 * requests about macros if not found in own macro map
 * @author Vladimir Voskresensky
 */
public class APTFileMacroMap extends APTBaseMacroMap {
    private APTMacroMap sysMacroMap;       
      
    public APTFileMacroMap() {        
    }
    
    /**
     * Creates a new instance of APTFileMacroMap
     */
    public APTFileMacroMap(APTMacroMap sysMacroMap) {
        if (sysMacroMap == null) {
            sysMacroMap = APTBaseMacroMap.EMPTY;
        }
        this.sysMacroMap = sysMacroMap;
    }
    
    public void setSysMacros(APTMacroMap sysMacroMap) {
        this.sysMacroMap = sysMacroMap;
    }
    
      
    public APTMacro getMacro(Token token) {
        // check own map
        APTMacro res = super.getMacro(token);
        // then check system map
     
        if (res == null && sysMacroMap != null) {
            res = sysMacroMap.getMacro(token);
        }        
        return res;
    }
    
    public void define(Token name, Collection<Token> params, List<Token> value) {
        if (sysMacroMap != null && sysMacroMap.isDefined(name)) {
            // TODO: report error about redefining system macros
        } else {
            super.define(name, params, value);
        }
    }
    
    public void undef(Token name) {
        if (sysMacroMap != null && sysMacroMap.isDefined(name)) {
            // TODO: report error about undefined system macros
        }
        super.undef(name);
    }
    
    protected APTMacro createMacro(Token name, Collection<Token> params, List<Token> value) {
        return new APTMacroImpl(name, params, value, false);
    }
    
    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        return new APTMacroMapSnapshot(parent);
    }
    
    public State getState() {
        //Create new snapshot instance in the tree
        changeActiveSnapshotIfNeeded();
        return new FileStateImpl(active.parent, sysMacroMap);
    }
    
    public void setState(State state) {
        active = makeSnapshot(((StateImpl)state).snap);
        if (state instanceof FileStateImpl) {
            sysMacroMap = ((FileStateImpl)state).sysMacroMap;
        }
    }
    
    public static class FileStateImpl extends StateImpl {
        public final APTMacroMap sysMacroMap;
        
        public FileStateImpl(APTMacroMapSnapshot snap, APTMacroMap sysMacroMap) {
            super(snap);
            this.sysMacroMap = sysMacroMap;
        }

        private FileStateImpl(FileStateImpl state, boolean cleanedState) {
            super(state, cleanedState);
            this.sysMacroMap = state.sysMacroMap;
        }
        
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("FileState\n"); // NOI18N
            retValue.append("Snapshot\n"); // NOI18N
            retValue.append(super.toString());
            retValue.append("\nSystem MacroMap\n"); // NOI18N
            retValue.append(sysMacroMap);
            return retValue.toString();
        }
        
        ////////////////////////////////////////////////////////////////////////
        // persistence support

        public void write(DataOutput output) throws IOException {
            super.write(output);
            APTSerializeUtils.writeSystemMacroMap(this.sysMacroMap, output);
        }

        public FileStateImpl(DataInput input) throws IOException {
            super(input);
            this.sysMacroMap = APTSerializeUtils.readSystemMacroMap(input);
        }  
        
        public StateImpl copyCleaned() {
            return new FileStateImpl(this, true);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // manage macro expanding stack
    
    private Stack<String> expandingMacros = new Stack<String>();
    
    public boolean pushExpanding(Token token) {
        assert (token != null);
        if (!isExpanding(token)) {
            expandingMacros.push(APTUtils.getTokenTextKey(token));
            return true;
        }
        return false;
    }
    
    public void popExpanding() {
        Object curMacro = null;
        try {
            curMacro = expandingMacros.pop();
        } catch (ArrayIndexOutOfBoundsException ex) {
            assert (false) : "why pop from empty stack?"; // NOI18N
        }
//        return curMacro;
    }
    
    public boolean isExpanding(Token token) {
        try {
            return expandingMacros.contains(APTUtils.getTokenTextKey(token));
        } catch (ArrayIndexOutOfBoundsException ex) {
            assert (false) : "why ask empty stack?"; // NOI18N
        }
        return false;
    }
    
    //////////////////////////////////////////////////////////////////////////
    // implementation details
    /*public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        boolean retValue = false;
        if (obj instanceof APTFileMacroMap) {
            retValue = super.equals(obj);
            if (retValue) {
                // use '==' as we share system maps
                retValue = (this.sysMacroMap == ((APTFileMacroMap)obj).sysMacroMap);
            }
        }
        return retValue;
    }*/

    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("Own Map:\n"); // NOI18N
        retValue.append(super.toString());
        retValue.append("System Map:\n"); // NOI18N
        retValue.append(sysMacroMap);
        return retValue.toString();
    }
}
