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
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTSystemMacroMap extends APTBaseMacroMap implements APTMacroMap {
    
    private APTMacroMap preMacroMap;
    
//    /** Creates a new instance of APTSystemMacroMap */
    protected APTSystemMacroMap() {           
        preMacroMap = new APTPredefinedMacroMap();
    }
    
    public APTSystemMacroMap(List<String> sysMacros) {
        this();
        fill(sysMacros);
    }
    
    protected APTMacro createMacro(Token name, Collection<Token> params, List<Token> value) {
        return new APTMacroImpl(name, params, value, true);
    }
    
    public boolean pushExpanding(Token token) {
        APTUtils.LOG.log(Level.SEVERE, "pushExpanding is not supported", new IllegalAccessException());// NOI18N
        return false;
    }

    public void popExpanding() {
        APTUtils.LOG.log(Level.SEVERE, "popExpanding is not supported", new IllegalAccessException());// NOI18N
//        return null;
    }

    public boolean isExpanding(Token token) {
        APTUtils.LOG.log(Level.SEVERE, "isExpanding is not supported", new IllegalAccessException());// NOI18N
        return false;
    }  
    
    @Override
    public APTMacro getMacro(Token token) {
        APTMacro res = super.getMacro(token);
        
        if(res == null) {
            res = preMacroMap.getMacro(token);
        }
        // If UNDEFINED_MACRO is found then the requested macro is undefined, return null
        return (res != APTMacroMapSnapshot.UNDEFINED_MACRO) ? res : null;
    }
    
    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        assert parent == null : "parent must be null";
        return new APTMacroMapSnapshot(parent);
    }

    @Override
    public void define(Token name, Collection<Token> params, List<Token> value) {
        throw new UnsupportedOperationException("Can not modify immutable System macro map"); // NOI18N
    }

    @Override
    public void undef(Token name) {
        throw new UnsupportedOperationException("Can not modify immutable System macro map"); // NOI18N
    }
}
