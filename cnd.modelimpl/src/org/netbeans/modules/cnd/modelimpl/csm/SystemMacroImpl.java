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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * Implementation of system macros and user-defined (in project properties) ones.
 *
 * @author Sergey Grinev
 */
public class SystemMacroImpl implements CsmMacro {
    
    private String macroName;
    private String macroBody;
    private boolean isUserDefined;
    private final List<? extends CharSequence> params;
    private CsmFile containingFile;
    private CsmUID<CsmMacro> uid;

    public SystemMacroImpl(String macroName, String macroBody, List<String> macroParams, CsmFile containingFile, boolean isUserDefined) {
        this.macroName = macroName;
        this.macroBody = macroBody;
        this.isUserDefined = isUserDefined;
        if (macroParams != null) {
            this.params = Collections.unmodifiableList(macroParams);
        } else {
            this.params = null;
        }
        assert containingFile instanceof Unresolved.UnresolvedFile;
        this.containingFile = containingFile;
        uid = new SelfUID<CsmMacro>(this);
    }
    
    public List<? extends CharSequence> getParameters() {
        return params;
    }

    public CharSequence getBody() {
        return macroBody;
    }

    public boolean isSystem() {
        return true;
    }

    public CharSequence getName() {
        return macroName;
    }

    public CsmFile getContainingFile() {
        return containingFile;
    }

    public int getStartOffset() {
        return 0;
    }

    public int getEndOffset() {
        return 0;
    }

    public Position getStartPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Position getEndPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CharSequence getText() {
        return ""; //NOI18N
    }

    public CsmUID<CsmMacro> getUID() {
        return uid;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof SystemMacroImpl)) {
            retValue = false;
        } else {
            SystemMacroImpl other = (SystemMacroImpl)obj;
            retValue = CharSequenceKey.Comparator.compare(getName(), other.getName()) == 0;
        }
        return retValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.macroName != null ? this.macroName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("#define '"); // NOI18N
        retValue.append(getName());
        if (getParameters() != null) {
            retValue.append("["); // NOI18N
            for (Iterator<? extends CharSequence> it = getParameters().iterator(); it.hasNext();) {
                CharSequence param = it.next();
                retValue.append(param);
                if (it.hasNext()) {
                    retValue.append(", "); // NOI18N
                }                
            }
            retValue.append("]"); // NOI18N
        }
        if (getBody().length() > 0) {
            retValue.append("'='"); // NOI18N
            retValue.append(getBody());
        }
        retValue.append("' [" + (isUserDefined ? "user defined" : "system") + "]"); // NOI18N
        return retValue.toString();
    }

    private static final class SelfUID<T> implements CsmUID<T> {
        private final T element;
        SelfUID(T element) {
            this.element = element;
        }
        public T getObject() {
            return this.element;
        }
    }    
    
}
