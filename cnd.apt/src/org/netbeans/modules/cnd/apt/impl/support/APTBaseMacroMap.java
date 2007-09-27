/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.structure.APTDefineNode;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * APTMacroMap base implementation
 * support collection of macros and saving/restoring this collection
 * @author Vladimir Voskresensky
 */
public abstract class APTBaseMacroMap implements APTMacroMap {

    protected APTMacroMapSnapshot active;
    
    private static final String DEFINE_PREFIX="#define "; // NOI18N
    private static final List<Token> DEF_MACRO_BODY;
    static {
        int type = APTTokenTypes.NUMBER;
        APTToken token = APTUtils.createAPTToken(type);
        token.setType(type);
        token.setText("1"); // NOI18N
        DEF_MACRO_BODY = new ArrayList<Token>();
        DEF_MACRO_BODY.add(token);
    }
    
    /**
     * Creates a new instance of APTBaseMacroMap
     */    
    public APTBaseMacroMap() {
        active = makeSnapshot(null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // manage define/undef macros

    
    public void fill(List<String> macros) {
        // update callback with user macros information
        for (Iterator<String> it = macros.iterator(); it.hasNext();) {
            String macro = it.next();
            if (APTTraceFlags.TRACE_APT) {
                System.err.println("adding macro in map " + macro); // NOI18N
            }
            define(macro);
        }           
    }
    
    /** 
     * analyze macroText string with structure "macro=value" and put in map
     */
    private void define(String macroText) {
        macroText = DEFINE_PREFIX + macroText;
        TokenStream stream = APTTokenStreamBuilder.buildTokenStream(macroText);
        try {
            Token next = stream.nextToken();
            // use define node to initialize #define directive from stream
            APTDefineNode defNode = new APTDefineNode(next);
            boolean look4Equal = true;
            do {
                next = stream.nextToken();
                if (look4Equal && (next.getType() == APTTokenTypes.ASSIGNEQUAL)) {
                    // skip the first equal token, it's delimeter
                    look4Equal = false;
                    next = stream.nextToken();
                }
            } while (defNode.accept(next)); 
            // special check for macros without values, we must set it to be 1
            List<Token> body = defNode.getBody();
            if (body == APTUtils.EMPTY_STREAM) {
                body = DEF_MACRO_BODY;
            }
            defineImpl(defNode.getName(), defNode.getParams(), body);
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, 
                    "error on lexing macros {0}\n\t{1}", // NOI18N
                    new Object[] {macroText, ex.getMessage()});
        }
    }
    
    public final void define(Token name, List<Token> value) {
        defineImpl(name, value);
    }
    
    protected final void defineImpl(Token name, List<Token> value) {
        define(name, null, value);
    }

    public void define(Token name, Collection<Token> params, List<Token> value) {
        defineImpl(name, params, value);
    }
    
    protected void defineImpl(Token name, Collection<Token> params, List<Token> value) {
        active.macros.put(APTUtils.getTokenTextKey(name), createMacro(name, params, value));
    }
    
    public void undef(Token name) {
        active.macros.put(APTUtils.getTokenTextKey(name), APTMacroMapSnapshot.UNDEFINED_MACRO);
    }
    
    /** method to implement in children */
    protected abstract APTMacro createMacro(Token name, Collection<Token> params, List<Token> value);
    
    ////////////////////////////////////////////////////////////////////////////
    // manage macro access

    public boolean isDefined(Token token) {
        return getMacro(token) != null;
    } 

    public APTMacro getMacro(Token token) {
        return active.getMacro(token);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // manage state (save/restore)
    
    protected abstract APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent);
    
    public State getState() {
        //Create new snapshot instance in the tree
        changeActiveSnapshotIfNeeded();
        return new StateImpl(active.parent);
    }
    
    protected void changeActiveSnapshotIfNeeded () {
        // do not use isEmpty approach yet.
        // not everything is clear yet, how clean of states is working in this case
        // some memory could remain and it's not good.
        // TODO: Needs additional investigation
        if (true || !active.isEmtpy()) {
            active = makeSnapshot(active);
        }
    }
    
    public void setState(State state) {
        active = makeSnapshot(((StateImpl)state).snap);
    }
    
    public static class StateImpl implements State {
        public final APTMacroMapSnapshot snap;
        
        public StateImpl(APTMacroMapSnapshot snap) {
            this.snap = snap;
        }
        
        protected StateImpl(StateImpl state, boolean cleanedState) {
            this.snap = cleanedState ? getFirstSnapshot(state.snap) : state.snap;
        }
        
        public String toString() {
            return snap != null ? snap.toString() : "<no snap>"; // NOI18N
        }

        public StateImpl copyCleaned() {
            return new StateImpl(this, true);
        }        
        ////////////////////////////////////////////////////////////////////////
        // persistence support

        public void write(DataOutput output) throws IOException {
            APTSerializeUtils.writeSnapshot(this.snap, output);
        }

        public StateImpl(DataInput input) throws IOException {
            this.snap = APTSerializeUtils.readSnapshot(input);
        }    
        
        ////////////////////////////////////////////////////////////////////////
        private APTMacroMapSnapshot getFirstSnapshot(APTMacroMapSnapshot snap) {
            if (snap != null) {
                while (snap.parent != null) {
                    snap = snap.parent;
                }
            }
            return snap;
        }        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details    

    public String toString() {
        Map<String, APTMacro> tmpMap = new HashMap<String, APTMacro>();
        APTMacroMapSnapshot.addAllMacros(active, tmpMap);
        return APTUtils.macros2String(tmpMap);
    }
    
    /*public boolean equals(Object obj) {
        boolean retValue = false;
        if (obj == null) {
            return false;
        }
        if (obj instanceof APTBaseMacroMap) {
            retValue = equals(this, (APTBaseMacroMap)obj);
        }
        return retValue;
    }

    /*private static boolean equals(APTBaseMacroMap map1, APTBaseMacroMap map2) {
        boolean equals = true;
        List macrosSorted1 = new ArrayList(map1.defined_macros.keySet());
        List macrosSorted2 = new ArrayList(map2.defined_macros.keySet());
        if (macrosSorted1.size() != macrosSorted2.size()) {
            return false;
        }
        Collections.sort(macrosSorted1);
        Collections.sort(macrosSorted2);            
        for (Iterator it1 = macrosSorted1.iterator(), it2 = macrosSorted2.iterator(); equals && it1.hasNext();) {
            String key1 = (String) it1.next();
            String key2 = (String) it2.next();
            equals &= key1.equalsIgnoreCase(key2);
        }         
        return equals;
    }
    
    public int hashCode() {
        int retValue;
        
        retValue = defined_macros.keySet().hashCode();
        return retValue;
    }*/
    
    protected static final APTMacroMap EMPTY = new EmptyMacroMap();
    private static final class EmptyMacroMap implements APTMacroMap {
        private EmptyMacroMap() {
        }
        
        protected APTMacro createMacro(Token name, Token[] params, List value) {
            return null;
        }

        public boolean pushExpanding(Token token) {
            return false;
        }

        public void popExpanding() {
//            return null;
        }

        public boolean isExpanding(Token token) {
            return false;
        }    
        
        public boolean isDefined(Token token) {
            return false;
        }

        public APTMacro getMacro(Token token) {
            return null;
        }      

        public void define(Token name, Collection params, List value) {
        }

        public void define(Token name, List value) {
        }

        public void undef(Token name) {
        }

        public void setState(State state) {
        }

        public State getState() {
            return new StateImpl((APTMacroMapSnapshot )null);
        }               
    };    
}
