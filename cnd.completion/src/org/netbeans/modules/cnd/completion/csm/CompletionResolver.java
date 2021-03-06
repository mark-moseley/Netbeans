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

package org.netbeans.modules.cnd.completion.csm;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmVariable;

/**
 * completion resolver for the file
 * file should be passed somehow in constructor
 * using of resolver:
 *  resolver = createResolver(file);
 * if reusing => resolver.refresh();
 *  if (resolver.resolve(...)) {
 *   result = resolver.getResult();
 *  }
 *
 * @author vv159170
 */
public interface CompletionResolver {
    // flags indicating what we plan to resolve using this resolver
    public static final int RESOLVE_NONE                   = 0;
    
    public static final int RESOLVE_CONTEXT                = 1 << 0;

    public static final int RESOLVE_CLASSES                = 1 << 1;
    
    public static final int RESOLVE_GLOB_VARIABLES         = 1 << 2;

    public static final int RESOLVE_GLOB_FUNCTIONS         = 1 << 3;

    public static final int RESOLVE_CLASS_FIELDS           = 1 << 4;

    public static final int RESOLVE_CLASS_METHODS          = 1 << 5;
    
    public static final int RESOLVE_LOCAL_VARIABLES        = 1 << 6;

    public static final int RESOLVE_FILE_LOCAL_VARIABLES   = 1 << 7;

    public static final int RESOLVE_LIB_CLASSES            = 1 << 8;
    
    public static final int RESOLVE_LIB_VARIABLES          = 1 << 9;

    public static final int RESOLVE_LIB_FUNCTIONS          = 1 << 10;
    
    public static final int RESOLVE_LIB_ENUMERATORS        = 1 << 11;
    
    public static final int RESOLVE_GLOB_ENUMERATORS       = 1 << 12;

    public static final int RESOLVE_FILE_LOCAL_MACROS      = 1 << 13;
    
    public static final int RESOLVE_FILE_PRJ_MACROS        = 1 << 14;

    public static final int RESOLVE_FILE_LIB_MACROS        = 1 << 15;

    public static final int RESOLVE_GLOB_MACROS            = 1 << 16;

    public static final int RESOLVE_LIB_MACROS             = 1 << 17;
    
    public static final int RESOLVE_GLOB_NAMESPACES        = 1 << 18;
    
    public static final int RESOLVE_LIB_NAMESPACES         = 1 << 19;
    
    public static final int RESOLVE_CLASS_ENUMERATORS      = 1 << 20;

    public static final int RESOLVE_CLASS_NESTED_CLASSIFIERS= 1 << 21;
    
    public static final int RESOLVE_MACROS                 = RESOLVE_FILE_LOCAL_MACROS | RESOLVE_FILE_PRJ_MACROS | RESOLVE_FILE_LIB_MACROS |
                                                                RESOLVE_GLOB_MACROS | RESOLVE_LIB_MACROS;

    public static final int RESOLVE_FUNCTIONS              = RESOLVE_GLOB_FUNCTIONS | RESOLVE_LIB_FUNCTIONS | RESOLVE_CLASS_METHODS;

    public static final int RESOLVE_VARIABLES              = RESOLVE_GLOB_VARIABLES | RESOLVE_LIB_VARIABLES | RESOLVE_CLASS_FIELDS;
    /**
     * specify what to resolve by this resolver
     */
    public void setResolveTypes(int resolveTypes);
    
    /**
     * init resolver before using
     * or reinit
     */
    public boolean refresh();

    /**
     * resolve code completion on specified position
     * items should start with specified prefix
     * or must exactly correspond to input prefix string
     */
    public boolean resolve(int offset, String strPrefix, boolean match);

    /**
     * get result of resolving
     */
    public Result getResult();
    
    public interface Result {
        public Collection<CsmVariable> getLocalVariables();

        public Collection<CsmField> getClassFields();

        public Collection<CsmEnumerator> getClassEnumerators();

        public Collection<CsmMethod> getClassMethods();

        public Collection<CsmClassifier> getProjectClassesifiersEnums();

        public Collection<CsmVariable> getFileLocalVars();

        public Collection<CsmEnumerator> getFileLocalEnumerators();

        public Collection<CsmMacro> getFileLocalMacros();

        public Collection<CsmMacro> getInFileIncludedProjectMacros();

        public Collection<CsmVariable> getGlobalVariables();

        public Collection<CsmEnumerator> getGlobalEnumerators();

        public Collection<CsmMacro> getGlobalProjectMacros();

        public Collection<CsmFunction> getGlobalProjectFunctions();

        public Collection<CsmNamespace> getGlobalProjectNamespaces();

        public Collection<CsmNamespaceAlias> getProjectNamespaceAliases();

        public Collection<CsmClassifier> getLibClassifiersEnums();

        public Collection<CsmMacro> getInFileIncludedLibMacros();

        public Collection<CsmMacro> getLibMacros();

        public Collection<CsmVariable> getLibVariables();

        public Collection<CsmEnumerator> getLibEnumerators();

        public Collection<CsmFunction> getLibFunctions();

        public Collection<CsmNamespace> getLibNamespaces();
        
        public Collection<CsmNamespaceAlias> getLibNamespaceAliases();

        public Collection addResulItemsToCol(Collection orig);
        
        public int size();
    }
}
