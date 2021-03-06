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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery.QueryScope;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver.Result;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CompletionResolverImpl implements CompletionResolver {

    private static final boolean DEBUG_SUMMARY = Boolean.getBoolean("csm.utilities.trace.summary");
    private static final boolean TRACE = Boolean.getBoolean("csm.utilities.trace");
    private static final boolean DEBUG = TRACE | DEBUG_SUMMARY;

    //    public static final int RESOLVE_CLASS_ENUMERATORS       = 1 << 13;

    private int resolveTypes = RESOLVE_NONE;
    private int hideTypes = ~RESOLVE_NONE;

    private CsmFile file;
    private CsmContext context;

    Result result = EMPTY_RESULT;
    CsmProjectContentResolver contResolver = null;

    private boolean caseSensitive = false;
    private boolean naturalSort = false;
    private boolean sort = false;
    private QueryScope queryScope = QueryScope.GLOBAL_QUERY;
    private boolean inIncludeDirective = false;

    public boolean isSortNeeded() {
        return sort;
    }

    public void setSortNeeded(boolean sort) {
        this.sort = sort;
    }

    public void setResolveScope(QueryScope queryScope) {
        this.queryScope = queryScope;
    }

    public void setInIncludeDirective(boolean inIncludeDirective) {
        this.inIncludeDirective = inIncludeDirective;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isNaturalSort() {
        return naturalSort;
    }

    /** Creates a new instance of CompletionResolver */
    public CompletionResolverImpl(CsmFile file) {
        this(file, false, false, false);
    }

    public CompletionResolverImpl(CsmFile file, boolean caseSensitive, boolean sort, boolean naturalSort) {
        this(file, RESOLVE_CONTEXT, caseSensitive, sort, naturalSort);
    }

    public CompletionResolverImpl(CsmFile file, int resolveTypes, boolean caseSensitive, boolean sort, boolean naturalSort) {
        this.file = file;
        this.resolveTypes = resolveTypes;
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        this.sort = sort;
    }

    public void setResolveTypes(int resolveTypes) {
        this.resolveTypes = resolveTypes;
    }

    public boolean refresh() {
        result = EMPTY_RESULT;
        // update if file attached to invalid project
        if ((file != null) && (file.getProject() != null) && !file.getProject().isValid()) {
            file = CsmUtilities.getCsmFile(CsmUtilities.getFileObject(file), true);
        }
        context = null;
        // should be called last, because uses setting set above
        this.contResolver = null;
        if (file == null) {
            return false;
        }
        this.contResolver = createContentResolver(file.getProject());
        return true;
    }

    public boolean update(boolean caseSensitive, boolean naturalSort) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        return refresh();
    }

    public boolean resolve(int offset, String strPrefix, boolean match) {
        if (file == null) {
            return false;
        }
        context  = CsmOffsetResolver.findContext(file, offset);
        if (DEBUG) System.out.println("context for offset " + offset + " :\n" + context); //NOI18N
        initResolveMask(context, offset, strPrefix, match);
        this.hideTypes = initHideMask(context, offset, this.resolveTypes, this.queryScope, strPrefix, this.inIncludeDirective);
        resolveContext(context, offset, strPrefix, match);
        return file != null;
    }

    public Result getResult() {
        return this.result;
    }

    public static final boolean STAT_COMPLETION = Boolean.getBoolean("cnd.completion.stat");
    public static final boolean TIMING_COMPLETION = Boolean.getBoolean("cnd.completion.timing") || STAT_COMPLETION;
    public static final boolean USE_CACHE = true;

    private void resolveContext(CsmContext context, int offset, String strPrefix, boolean match) {
        long time = 0;
        if (TIMING_COMPLETION) {
            time = System.currentTimeMillis();
            System.err.println("Started resolving context");
        }
        CsmProject prj = file != null ? file.getProject() : null;
        if (prj == null) {
            return;
        }
        CacheEntry key = null;
        CsmFunction fun = CsmContextUtilities.getFunction(context, true);
        ResultImpl resImpl = new ResultImpl();
        boolean isLocalVariable = resolveLocalContext(prj, resImpl, fun, context, offset, strPrefix, match);
        if (USE_CACHE && isEnough(strPrefix, match)) {
            if (isLocalVariable){
                result = buildResult(context, resImpl);
                return;
            }
            if (fun != null) {
                CsmUID uid = fun.getUID();
                key = new CacheEntry(resolveTypes, hideTypes, strPrefix, uid);
                Result res = getCache().get(key);
                if (res != null) {
                    result = res;
                    return;
                } else {
                    Iterator<CacheEntry> it = getCache().keySet().iterator();
                    if (it.hasNext()) {
                        if (!it.next().function.equals(uid)){
                            getCache().clear();
                        }
                    }
                }
            } else if (CsmKindUtilities.isVariable(context.getLastObject())) {
                CsmVariable var = (CsmVariable) context.getLastObject();
                CsmUID uid = var.getUID();
                key = new CacheEntry(resolveTypes, hideTypes, strPrefix, uid);
                Result res = getCache().get(key);
                if (res != null) {
                    result = res;
                    return;
                } else {
                    Iterator<CacheEntry> it = getCache().keySet().iterator();
                    if (it.hasNext()) {
                        if (!it.next().function.equals(uid)){
                            getCache().clear();
                        }
                    }
                }
            }
        }
        //long timeStart = System.nanoTime();
        resolveContext(prj, resImpl, context, offset, strPrefix, match);
        result = buildResult(context, resImpl);
        if (key != null){
            getCache().put(key, result);
        }
        //long timeEnd = System.nanoTime();
        //System.out.println("get gesolve list time "+(timeEnd -timeStart)+" objects "+result.size()); //NOI18N
        //System.out.println("get global macro time "+(timeGlobMacroEnd -timeGlobMacroStart)+" objects "+ //NOI18N
        //        (globProjectMacros.size()+globLibMacros.size()));
        if (TIMING_COMPLETION) {
            time = System.currentTimeMillis() - time;
            System.err.println("Resolving context took " + time + "ms");
        }
    }

    private boolean isEnough(String strPrefix, boolean match){
        return match && strPrefix != null && strPrefix.length() > 0;
    }

    private boolean isEnough(String strPrefix, boolean match, Collection collection){
        if (isEnough(strPrefix, match) && collection != null){
            return collection.size()>0;
        }
        return false;
    }

    private boolean resolveLocalContext(CsmProject prj, ResultImpl resImpl, CsmFunction fun, CsmContext context, int offset, String strPrefix, boolean match) {
        boolean needVars = needLocalVars(context, offset);
        boolean needClasses = needLocalClasses(context, offset);
        if (needVars || needClasses) {
            List<CsmDeclaration> decls = contResolver.findFunctionLocalDeclarations(context, strPrefix, match);
            // separate local classes/structs/enums/unions and variables
            resImpl.localVars = new ArrayList<CsmVariable>(decls.size());
            for (CsmDeclaration elem : decls) {
                if (needVars && CsmKindUtilities.isVariable(elem)) {
                    resImpl.localVars.add((CsmVariable) elem);
                    if (isEnough(strPrefix, match)) return true;
                } if (needClasses && CsmKindUtilities.isClassifier(elem)) {
                    if (resImpl.classesEnumsTypedefs == null) {
                        resImpl.classesEnumsTypedefs = new ArrayList<CsmClassifier>();
                    }
                    resImpl.classesEnumsTypedefs.add((CsmClassifier) elem);
                    if (isEnough(strPrefix, match)) return true;
                } if (needVars && CsmKindUtilities.isEnumerator(elem)) {
                    if (resImpl.fileLocalEnumerators == null) {
                        resImpl.fileLocalEnumerators = new ArrayList<CsmEnumerator>();
                    }
                    resImpl.fileLocalEnumerators.add((CsmEnumerator) elem);
                    if (isEnough(strPrefix, match)) return true;
                }
            }
        }
        return false;
    }

    private boolean resolveContext(CsmProject prj, ResultImpl resImpl, CsmContext context, int offset, String strPrefix, boolean match) {
        CsmFunction fun = CsmContextUtilities.getFunction(context, true);
        if (needLocalVars(context, offset)) {
            resImpl.fileLocalEnumerators = contResolver.getFileLocalEnumerators(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.fileLocalEnumerators)) return true;
            boolean staticContext = fun == null ? true : CsmBaseUtilities.isStaticContext(fun);

            if (needClassElements(context, offset)) {
                //if (fun == null) System.err.printf("\nFunction is null. Offset: %d Context:\n%s \n", offset, context.toString());
                CsmClass clazz = (fun == null) ? null : CsmBaseUtilities.getFunctionClass(fun);
                clazz = clazz != null ? clazz : CsmContextUtilities.getClass(context, false, true);
                if (clazz != null) {
                    // get class variables visible in this method
                    resImpl.classFields = contResolver.getFields(clazz, fun, strPrefix, staticContext, match, true,false);
                    if (isEnough(strPrefix, match, resImpl.classFields)) return true;

                    // get class enumerators visible in this method
                    resImpl.classEnumerators = contResolver.getEnumerators(clazz, fun, strPrefix, match, true,false);
                    if (isEnough(strPrefix, match, resImpl.classEnumerators)) return true;

                    // get class methods visible in this method
                    resImpl.classMethods = contResolver.getMethods(clazz, fun, strPrefix, staticContext, match, true,false);
                    if (isEnough(strPrefix, match, resImpl.classMethods)) return true;
                    if (needNestedClassifiers(context, offset)) {
                        // get class nested classifiers visible in this context
                        resImpl.classesEnumsTypedefs = contResolver.getNestedClassifiers(clazz, fun, strPrefix, match, needClasses(context, offset));
                        if (isEnough(strPrefix, match, resImpl.classesEnumsTypedefs)) return true;
                    }
                }
            }
        } else if (needClassElements(context, offset)) {
            CsmClass clazz = fun == null ? null : CsmBaseUtilities.getFunctionClass(fun);
            clazz = clazz != null ? clazz : CsmContextUtilities.getClass(context, false, true);
            if (clazz != null) {
                boolean staticContext = false;
                // get class methods visible in this method
                CsmOffsetableDeclaration contextDeclaration = fun != null ? fun : clazz;
                if (needClassMethods(context, offset) && !CsmContextUtilities.isInType(context, offset)) {
                    if (clazz != null) {
                        resImpl.classMethods = contResolver.getMethods(clazz, contextDeclaration, strPrefix, staticContext, match, true,false);
                        if (isEnough(strPrefix, match, resImpl.classMethods)) return true;
                    }
                }
                if (needClassFields(context, offset) && !CsmContextUtilities.isInType(context, offset)) {
                    // get class variables visible in this context
                    resImpl.classFields = contResolver.getFields(clazz, contextDeclaration, strPrefix, staticContext, match, true,false);
                    if (isEnough(strPrefix, match, resImpl.classFields)) return true;
                }
                if (needClassEnumerators(context, offset) && !CsmContextUtilities.isInType(context, offset)) {
                    // get class enumerators visible in this context
                    resImpl.classEnumerators = contResolver.getEnumerators(clazz, contextDeclaration, strPrefix, match, true,false);
                    if (isEnough(strPrefix, match, resImpl.classEnumerators)) return true;
                }
                if (needNestedClassifiers(context, offset)) {
                    // get class nested classifiers visible in this context
                    resImpl.classesEnumsTypedefs = contResolver.getNestedClassifiers(clazz, contextDeclaration, strPrefix, match, true);
                    if (isEnough(strPrefix, match, resImpl.classesEnumsTypedefs)) return true;
                }
            }
        }
        if (needTemplateParameters(context, offset)) {
            resImpl.templateParameters = getTemplateParameters(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.templateParameters)) return true;
        }
        if (needClasses(context, offset)) {
            // list of classesEnumsTypedefs
            if (resImpl.classesEnumsTypedefs == null) {
                resImpl.classesEnumsTypedefs = new ArrayList<CsmClassifier>();
            }
            resImpl.classesEnumsTypedefs.addAll(getClassesEnums(context, prj, strPrefix, match, offset, false));
            if (isEnough(strPrefix, match, resImpl.classesEnumsTypedefs)) return true;
        } else if (needContextClasses(context, offset)) {
            if (resImpl.classesEnumsTypedefs == null) {
                resImpl.classesEnumsTypedefs = new ArrayList<CsmClassifier>();
            }
            resImpl.classesEnumsTypedefs.addAll(getClassesEnums(context, prj, strPrefix, match, offset, true));
            if (isEnough(strPrefix, match, resImpl.classesEnumsTypedefs)) return true;
        }
        if (needFileLocalMacros(context, offset)) {
            resImpl.fileLocalMacros = contResolver.getFileLocalMacros(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.fileLocalMacros)) return true;
        }
        if (needFileLocalFunctions(context, offset)) {
            resImpl.fileLocalFunctions = getFileLocalFunctions(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.fileLocalFunctions)) return true;
        }
        // file local variables
        if (needFileLocalVars(context, offset)) {
            resImpl.fileLocalVars = contResolver.getFileLocalVariables(context, strPrefix, match, queryScope == QueryScope.LOCAL_QUERY);
            if (isEnough(strPrefix, match, resImpl.fileLocalVars)) return true;
        }

        if (needFileIncludedMacros(context, offset)) {
            resImpl.fileProjectMacros = contResolver.getFileIncludedProjectMacros(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.fileProjectMacros)) return true;
        }
        if (needFileIncludedLibMacros(context, offset)) {
            resImpl.fileLibMacros = contResolver.getFileIncludeLibMacros(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.fileLibMacros)) return true;
        }
        if (needGlobalMacros(context, offset)) {
            resImpl.globProjectMacros = contResolver.getProjectMacros(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.globProjectMacros)) return true;
        }
        if (needGlobalLibMacros(context, offset)) {
            resImpl.globLibMacros = contResolver.getLibMacros(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.globLibMacros)) return true;
        }

        if (needGlobalVariables(context, offset)) {
            resImpl.globVars = getGlobalVariables(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.globVars)) return true;
        }
        if (needGlobalEnumerators(context, offset)) {
            resImpl.globEnumerators = getGlobalEnumerators(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.globEnumerators)) return true;
        }
        if (needGlobalFunctions(context, offset)) {
            resImpl.globFuns = getGlobalFunctions(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.globFuns)) return true;
        }
        if (needGlobalNamespaces(context, offset)) {
            resImpl.globProjectNSs = getGlobalNamespaces(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.globProjectNSs)) return true;
            resImpl.projectNsAliases = getProjectNamespaceAliases(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.projectNsAliases)) return true;
        }

        if (needLibClasses(context, offset)) {
            resImpl.libClasses = getLibClassesEnums(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libClasses)) return true;
        }
        if (needLibVariables(context, offset)) {
            resImpl.libVars = getLibVariables(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libVars)) return true;
        }
        if (needLibEnumerators(context, offset)) {
            resImpl.libEnumerators = getLibEnumerators(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libEnumerators)) return true;
        }
        if (needLibFunctions(context, offset)) {
            resImpl.libFuns = getLibFunctions(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libFuns)) return true;
        }
        if (needLibNamespaces(context, offset)) {
            resImpl.libNSs = getLibNamespaces(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libNSs)) return true;
//            libNsAliases = getLibNamespaceAliases(prj, strPrefix, match, offset);
        }
        return false;
    }

    private static int initHideMask(final CsmContext context, final int offset, final int resolveTypes,
            final QueryScope queryScope, final String strPrefix, boolean inIncludeDirective) {
        int hideTypes = inIncludeDirective ? RESOLVE_MACROS : ~RESOLVE_NONE;
        // do not provide libraries data and global data when just resolve context with empty prefix
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT && strPrefix.length() == 0) {
            hideTypes &= ~RESOLVE_LIB_ELEMENTS;
            // if not in exact file scope do not provide project globals
            if (!CsmKindUtilities.isFile(context.getLastScope())) {
                hideTypes &= ~RESOLVE_GLOB_MACROS;
            }
        }
        if (queryScope == QueryScope.LOCAL_QUERY || queryScope == QueryScope.SMART_QUERY) {
                // hide all lib context
                hideTypes &= ~RESOLVE_LIB_ELEMENTS;

                // hide some project context
                hideTypes &= ~RESOLVE_GLOB_MACROS;
                hideTypes &= ~RESOLVE_FILE_PRJ_MACROS;
                hideTypes &= ~RESOLVE_GLOB_NAMESPACES;
                hideTypes &= ~RESOLVE_CLASSES;
                hideTypes &= ~RESOLVE_GLOB_VARIABLES;
                hideTypes &= ~RESOLVE_GLOB_FUNCTIONS;
                hideTypes &= ~RESOLVE_GLOB_ENUMERATORS;
        }
        // for local query hide some more elements as well
        if (queryScope == QueryScope.LOCAL_QUERY) {
                hideTypes &= ~RESOLVE_CLASS_FIELDS;
                hideTypes &= ~RESOLVE_CLASS_METHODS;
                hideTypes &= ~RESOLVE_CLASS_NESTED_CLASSIFIERS;
                hideTypes &= ~RESOLVE_CLASS_ENUMERATORS;
        }
        return hideTypes;
    }
    private static Result buildResult(CsmContext context, ResultImpl out) {
        // local vars
        int fullSize = 0;
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.localVars, "Local variables");} //NOI18N
        // add class fields
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.classFields, "Class fields");} //NOI18N
        // add class enumerators
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.classEnumerators, "Class enumerators");} //NOI18N
        // add class methods
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.classMethods, "Class methods");} //NOI18N
        // add classesEnumsTypedefs
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.classesEnumsTypedefs, "Classes/Enums/Typedefs");} //NOI18N
        // add file local variables
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.fileLocalVars, "File Local Variables");} //NOI18N
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.fileLocalEnumerators, "File Local Enumerators");} //NOI18N
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.fileLocalMacros, "File Local Macros");} //NOI18N
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.fileLocalFunctions, "File Local Functions");} //NOI18N
        // remove local macros from project included macros
        remove(out.fileProjectMacros, out.fileLocalMacros);
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.fileProjectMacros, "File Included Project Macros");} //NOI18N
        // add global variables
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.globVars, "Global variables");} //NOI18N
        // add global enumerators, but remove file local ones
        remove(out.globEnumerators, out.fileLocalEnumerators);
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.globEnumerators, "Global enumerators");} //NOI18N
        // global macros
        // remove project included macros from all other macros
        remove(out.globProjectMacros, out.fileProjectMacros);
        // remove local macros from project macros
        remove(out.globProjectMacros, out.fileLocalMacros);
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.globProjectMacros, "Global Project Macros");} //NOI18N
        // add global functions
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.globFuns, "Global Project functions");} //NOI18N
        // add namespaces
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.globProjectNSs, "Global Project Namespaces");} //NOI18N
        // add namespace aliases
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.projectNsAliases, "Project Namespace Aliases");} //NOI18N
        // add libraries classesEnumsTypedefs
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.libClasses, "Library classes");} //NOI18N
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.fileLibMacros, "File Included Library Macros");} //NOI18N
        // remove file included lib macros from all other lib macros
        remove(out.globLibMacros, out.fileLibMacros);
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.globLibMacros, "Global Library Macros");} //NOI18N
        // add libraries variables
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.libVars, "Global Library variables");} //NOI18N
        // add libraries enumerators
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.libEnumerators, "Global Library enumerators");} //NOI18N
        // add libraries functions
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.libFuns, "Global Library functions");} //NOI18N
        // add libraries namespaces
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.libNSs, "Global Library Namespaces");} //NOI18N
        // add libraries namespace aliases
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(out.libNsAliases, "Global Library Namespace Aliases");} //NOI18N
        // all elements info
        if (DEBUG || STAT_COMPLETION) { trace(null, "There are " + fullSize + " resovled elements");} //NOI18N
        return out;
    }

    private static <T> Collection remove(Collection<T> dest, Collection<T> removeItems) {
        CsmUtilities.<T>removeAll(dest, removeItems);
        return dest;
    }

    protected CsmProjectContentResolver createContentResolver(CsmProject prj) {
        CsmProjectContentResolver resolver = new CsmProjectContentResolver(prj, isCaseSensitive(), isSortNeeded(), isNaturalSort());
        return resolver;
    }

    protected CsmProjectContentResolver createLibraryResolver(CsmProject lib) {
        CsmProjectContentResolver libResolver = new CsmProjectContentResolver(lib, isCaseSensitive(), isSortNeeded(), isNaturalSort());
        return libResolver;
    }

    @SuppressWarnings("unchecked")
    private static Collection merge(Collection orig, Collection newList) {
        return CsmUtilities.merge(orig, newList);
    }

    private Collection<CsmTemplateParameter> getTemplateParameters(CsmContext context, String strPrefix, boolean match) {
        Collection<CsmTemplateParameter> templateParameters = null;
        CsmFunction fun = CsmContextUtilities.getFunction(context, false);
        Collection<CsmTemplate> analyzeTemplates = new ArrayList<CsmTemplate>();
        if (fun == null && context.getLastObject() != null) {
            // Fix for IZ#138099: unresolved identifier for functions' template parameter.
            // We might be just before function name, where its template parameters
            // and type reside. Let's try a bit harder to find that function.
            CsmObject obj = context.getLastObject();
            if (CsmKindUtilities.isFunction(obj)) {
                fun = (CsmFunction)obj;
            } else {
                int offset = ((CsmOffsetable)context.getLastObject()).getEndOffset();
                obj = CsmDeclarationResolver.findInnerFileObject(file, offset, context);
                if (CsmKindUtilities.isFunction(obj)) {
                    fun = (CsmFunction)obj;
                } else if (CsmKindUtilities.isClassForwardDeclaration(obj)) {
                    if (CsmKindUtilities.isTemplate(obj)) {
                        analyzeTemplates.add((CsmTemplate)obj);
                    }
                }
            }
        }
        if (CsmKindUtilities.isTemplate(fun)) {
            analyzeTemplates.add((CsmTemplate)fun);
        }
        CsmClass clazz = fun == null ? null : CsmBaseUtilities.getFunctionClass(fun);
        clazz = clazz != null ? clazz : CsmContextUtilities.getClass(context, false, false);
        if (CsmKindUtilities.isTemplate(clazz)) {
            // We add template parameters to function parameters on function init,
            // so we dont need to add them to completion list again.
            if (!CsmKindUtilities.isTemplate(fun) || clazz.equals(CsmContextUtilities.getClass(context, false, false))) {
                analyzeTemplates.add((CsmTemplate)clazz);
            }
            CsmScope scope = clazz.getScope();
            while (CsmKindUtilities.isClass(scope)) {
                if (CsmKindUtilities.isTemplate(scope)) {
                    analyzeTemplates.add((CsmTemplate)scope);
                }
                scope = ((CsmClass)scope).getScope();
            }
        }
        if (!analyzeTemplates.isEmpty()) {
            templateParameters = new ArrayList<CsmTemplateParameter>();
            for (CsmTemplate csmTemplate : analyzeTemplates) {
                for (CsmTemplateParameter elem : csmTemplate.getTemplateParameters()) {
                    if (CsmSortUtilities.matchName(elem.getName(), strPrefix, match, caseSensitive)) {
                        templateParameters.add(elem);
                    }
                }
            }
        }
        return templateParameters;
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmClassifier> getClassesEnums(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset,boolean contextOnly) {
        if (prj == null) {
            return null;
        }
        // try to get elements from visible namespaces
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context,this.file, offset, strPrefix.length() == 0,contextOnly);
        LinkedHashSet<CsmClassifier> out = new LinkedHashSet<CsmClassifier>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmClassifier> res = contResolver.getNamespaceClassesEnums(ns, strPrefix, match, false);
            out.addAll(res);
        }
        CsmDeclaration.Kind kinds[] =	{
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF
        };
        if (!contextOnly) {
            Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
            out.addAll(usedDecls);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmVariable> getGlobalVariables(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context,this.file, offset, strPrefix.length() == 0,false);
        LinkedHashSet<CsmVariable> out = new LinkedHashSet<CsmVariable>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmVariable> res = contResolver.getNamespaceVariables(ns, strPrefix, match, false);
            out.addAll(res);
        }
        CsmDeclaration.Kind kinds[] =	{
            CsmDeclaration.Kind.VARIABLE
        };
        Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
        out.addAll(usedDecls);
        return out;
    }

    private Collection<CsmEnumerator> getGlobalEnumerators(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context,this.file, offset, strPrefix.length() == 0,false);
        LinkedHashSet<CsmEnumerator> out = new LinkedHashSet<CsmEnumerator>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmEnumerator> res = contResolver.getNamespaceEnumerators(ns, strPrefix, match, false);
            out.addAll(res);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmFunction> getGlobalFunctions(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context,this.file, offset, strPrefix.length() == 0,false);
        LinkedHashSet<CsmFunction> out = new LinkedHashSet<CsmFunction>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmFunction> res = contResolver.getNamespaceFunctions(ns, strPrefix, match, false);
            out.addAll(res);
        }
        CsmDeclaration.Kind kinds[] =	{
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
        out.addAll(usedDecls);
        return out;
    }

    private Collection<CsmFunction> getFileLocalFunctions(CsmContext context, String strPrefix, boolean match) {
        Collection<CsmFunction> res = contResolver.getFileLocalFunctions(context, strPrefix, match, queryScope == QueryScope.LOCAL_QUERY);
        return res;
    }

    private Collection<CsmNamespace> getGlobalNamespaces(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context,this.file, offset, strPrefix.length() == 0,false);
        LinkedHashSet<CsmNamespace> out = new LinkedHashSet<CsmNamespace>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmNamespace> res = contResolver.getNestedNamespaces(ns, strPrefix, match);
            out.addAll(res);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmNamespaceAlias> getProjectNamespaceAliases(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        CsmProject inProject = (strPrefix.length() == 0) ? prj : null;
        Collection aliases = CsmUsingResolver.getDefault().findNamespaceAliases(this.file, offset, inProject);
        Collection out;
        if (strPrefix.length() > 0) {
            out = filterDeclarations(aliases, strPrefix, match,
                    new CsmDeclaration.Kind[] { CsmDeclaration.Kind.NAMESPACE_ALIAS });
        } else {
            out = aliases;
        }
        return out;
    }

    private Collection<CsmClassifier> getLibClassesEnums(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibClassesEnums(strPrefix, match);
    }

    private Collection<CsmVariable> getLibVariables(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibVariables(strPrefix, match);
    }

    private Collection<CsmEnumerator> getLibEnumerators(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibEnumerators(strPrefix, match, true);
    }

    private Collection<CsmFunction> getLibFunctions(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibFunctions(strPrefix, match);
    }

    private Collection<CsmNamespace> getLibNamespaces(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibNamespaces(strPrefix, match);
    }

    private boolean needLocalClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LOCAL_CLASSES) == RESOLVE_LOCAL_CLASSES) {
            return true;
        }
        return false;
    }

    private boolean needClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASSES) == RESOLVE_CLASSES) {
            return true;
        }
        return false;
    }

    private boolean needContextClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CONTEXT_CLASSES) == RESOLVE_CONTEXT_CLASSES) {
            return true;
        }
        return false;
    }

    private void updateResolveTypesInFunction(final int offset, final CsmContext context, boolean match) {

        // always resolve local classes, not only when in type
        resolveTypes |= RESOLVE_LOCAL_CLASSES;

        boolean isInType = CsmContextUtilities.isInType(context, offset);
        if (!isInType) {
            resolveTypes |= RESOLVE_FILE_LOCAL_VARIABLES;
            resolveTypes |= RESOLVE_LOCAL_VARIABLES;
            resolveTypes |= RESOLVE_GLOB_VARIABLES;
            resolveTypes |= RESOLVE_GLOB_ENUMERATORS;
            resolveTypes |= RESOLVE_CLASS_FIELDS;
            resolveTypes |= RESOLVE_CLASS_ENUMERATORS;
            resolveTypes |= RESOLVE_LIB_ENUMERATORS;
        }
        if (CsmContextUtilities.isInFunctionBodyOrInitializerList(context, offset)) {
            if (!isInType || !match) {
                resolveTypes |= RESOLVE_LIB_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
                resolveTypes |= RESOLVE_FILE_LOCAL_FUNCTIONS;
                resolveTypes |= RESOLVE_LIB_FUNCTIONS;
                resolveTypes |= RESOLVE_CLASS_METHODS;
            }
            if(!match) {
                resolveTypes |= RESOLVE_FILE_LOCAL_VARIABLES;
                resolveTypes |= RESOLVE_LOCAL_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_VARIABLES;
                resolveTypes |= RESOLVE_CLASS_FIELDS;
                resolveTypes |= RESOLVE_CLASS_ENUMERATORS;
            }
        }
    }

    private boolean needFileLocalVars(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_VARIABLES) == RESOLVE_FILE_LOCAL_VARIABLES) {
            return true;
        }
        return false;
    }

    private boolean needLocalVars(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LOCAL_VARIABLES) == RESOLVE_LOCAL_VARIABLES) {
            return true;
        }
        return false;
    }

    private boolean needGlobalVariables(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_VARIABLES) == RESOLVE_GLOB_VARIABLES) {
            return true;
        }
        return false;
    }

    private boolean needGlobalEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_ENUMERATORS) == RESOLVE_GLOB_ENUMERATORS) {
            return true;
        }
        return false;
    }

    private boolean needGlobalFunctions(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_FUNCTIONS) == RESOLVE_GLOB_FUNCTIONS) {
            return true;
        }
        return false;
    }

    private boolean needGlobalNamespaces(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_NAMESPACES) == RESOLVE_GLOB_NAMESPACES) {
            return true;
        }
        return false;
    }

    private boolean needFunctionVars(CsmContext context, int offset) {
        return needLocalVars(context, offset);
    }

    private boolean needLibClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_CLASSES) == RESOLVE_LIB_CLASSES) {
            return true;
        }
        return false;
    }

    private boolean needLibVariables(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_VARIABLES) == RESOLVE_LIB_VARIABLES) {
            return true;
        }
        return false;
    }

    private boolean needLibEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_ENUMERATORS) == RESOLVE_LIB_ENUMERATORS) {
            return true;
        }
        return false;
    }

    private boolean needLibFunctions(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_FUNCTIONS) == RESOLVE_LIB_FUNCTIONS) {
            return true;
        }
        return false;
    }

    private boolean needLibNamespaces(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_NAMESPACES) == RESOLVE_LIB_NAMESPACES) {
            return true;
        }
        return false;
    }

    private boolean needFileLocalMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_MACROS) == RESOLVE_FILE_LOCAL_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needFileLocalFunctions(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_FUNCTIONS) == RESOLVE_FILE_LOCAL_FUNCTIONS) {
            return true;
        }
        return false;
    }

    private boolean needFileIncludedMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_PRJ_MACROS) == RESOLVE_FILE_PRJ_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needFileIncludedLibMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LIB_MACROS) == RESOLVE_FILE_LIB_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needGlobalMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_MACROS) == RESOLVE_GLOB_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needGlobalLibMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_MACROS) == RESOLVE_LIB_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needClassMethods(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_METHODS) == RESOLVE_CLASS_METHODS) {
            return true;
        }
        return false;
    }

    private boolean needClassFields(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_FIELDS) == RESOLVE_CLASS_FIELDS) {
            return true;
        }
        return false;
    }

    private boolean needClassEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_ENUMERATORS) == RESOLVE_CLASS_ENUMERATORS) {
            return true;
        }
        return false;
    }

    private boolean needNestedClassifiers(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_NESTED_CLASSIFIERS) == RESOLVE_CLASS_NESTED_CLASSIFIERS) {
            return true;
        }
        return false;
    }

    private boolean needClassElements(CsmContext context, int offset) {
        if (((hideTypes & resolveTypes & RESOLVE_CLASS_METHODS) == RESOLVE_CLASS_METHODS) ||
            ((hideTypes & resolveTypes & RESOLVE_CLASS_FIELDS) == RESOLVE_CLASS_FIELDS) ||
            ((hideTypes & resolveTypes & RESOLVE_CLASS_NESTED_CLASSIFIERS) == RESOLVE_CLASS_NESTED_CLASSIFIERS) ||
            ((hideTypes & resolveTypes & RESOLVE_CLASS_ENUMERATORS) == RESOLVE_CLASS_ENUMERATORS)) {
            return true;
        }
        return false;
    }

    private boolean needTemplateParameters(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_TEMPLATE_PARAMETERS) == RESOLVE_TEMPLATE_PARAMETERS) {
            return true;
        }
        return false;
    }


    // ====================== Debug support ===================================

    private static int trace(Collection/*<CsmObject*/ list, String msg) {
        System.err.println("\t" + msg + " [size - " + (list == null ? "null" : list.size()) +"]"); //NOI18N
        if (list == null) {
            return 0;
        }
        if (TRACE) {
            int i = 0;
            for (Object obj : list) {
                CsmObject elem = (CsmObject) obj;
                System.err.println("\t\t["+i+"]"+CsmUtilities.getCsmName(elem)); //NOI18N
                i++;
            }
        }
        return list.size();
    }

    private static final class ResultImpl implements Result {
        private Collection<CsmVariable> localVars;

        private Collection<CsmField> classFields;
        private Collection<CsmEnumerator> classEnumerators;
        private Collection<CsmMethod> classMethods;
        private Collection<CsmClassifier> classesEnumsTypedefs;

        private Collection<CsmVariable> fileLocalVars;
        private Collection<CsmEnumerator> fileLocalEnumerators;
        private Collection<CsmMacro>  fileLocalMacros;
        private Collection<CsmFunction> fileLocalFunctions;

        private Collection<CsmMacro> fileProjectMacros;

        private Collection<CsmVariable> globVars;
        private Collection<CsmEnumerator> globEnumerators;
        private Collection<CsmMacro> globProjectMacros;

        private Collection<CsmFunction> globFuns;
        private Collection<CsmNamespace> globProjectNSs;
        private Collection<CsmNamespaceAlias> projectNsAliases;

        private Collection<CsmClassifier> libClasses;
        private Collection<CsmMacro> fileLibMacros;
        private Collection<CsmMacro> globLibMacros;
        private Collection<CsmVariable> libVars;
        private Collection<CsmEnumerator> libEnumerators;
        private Collection<CsmFunction> libFuns;
        private Collection<CsmNamespace> libNSs;
        private Collection<CsmNamespaceAlias> libNsAliases;

        private Collection<CsmTemplateParameter> templateParameters;

        private ResultImpl(){
        }

        public Collection<CsmVariable> getLocalVariables() {
            return CompletionResolverImpl.<CsmVariable>maskNull(localVars);
        }

        public Collection<CsmField> getClassFields() {
            return CompletionResolverImpl.<CsmField>maskNull(classFields);
        }

        public Collection<CsmEnumerator> getClassEnumerators() {
            return CompletionResolverImpl.<CsmEnumerator>maskNull(classEnumerators);
        }

        public Collection<CsmMethod> getClassMethods() {
            return CompletionResolverImpl.<CsmMethod>maskNull(classMethods);
        }

        public Collection<CsmClassifier> getProjectClassesifiersEnums() {
            return CompletionResolverImpl.<CsmClassifier>maskNull(classesEnumsTypedefs);
        }

        public Collection<CsmVariable> getFileLocalVars() {
            return CompletionResolverImpl.<CsmVariable>maskNull(fileLocalVars);
        }

        public Collection<CsmEnumerator> getFileLocalEnumerators() {
            return CompletionResolverImpl.<CsmEnumerator>maskNull(fileLocalEnumerators);
        }

        public Collection<CsmMacro> getFileLocalMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(fileLocalMacros);
        }

        public Collection<CsmFunction> getFileLocalFunctions() {
            return CompletionResolverImpl.<CsmFunction>maskNull(fileLocalFunctions);
        }

        public Collection<CsmMacro> getInFileIncludedProjectMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(fileProjectMacros);
        }

        public Collection<CsmVariable> getGlobalVariables() {
            return CompletionResolverImpl.<CsmVariable>maskNull(globVars);
        }

        public Collection<CsmEnumerator> getGlobalEnumerators() {
            return CompletionResolverImpl.<CsmEnumerator>maskNull(globEnumerators);
        }

        public Collection<CsmMacro> getGlobalProjectMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(globProjectMacros);
        }

        public Collection<CsmFunction> getGlobalProjectFunctions() {
            return CompletionResolverImpl.<CsmFunction>maskNull(globFuns);
        }

        public Collection<CsmNamespace> getGlobalProjectNamespaces() {
            return CompletionResolverImpl.<CsmNamespace>maskNull(globProjectNSs);
        }

        public Collection<CsmNamespaceAlias> getProjectNamespaceAliases() {
            return CompletionResolverImpl.<CsmNamespaceAlias>maskNull(projectNsAliases);
        }

        public Collection<CsmClassifier> getLibClassifiersEnums() {
            return CompletionResolverImpl.<CsmClassifier>maskNull(libClasses);
        }

        public Collection<CsmMacro> getInFileIncludedLibMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(fileLibMacros);
        }

        public Collection<CsmMacro> getLibMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(globLibMacros);
        }

        public Collection<CsmVariable> getLibVariables() {
            return CompletionResolverImpl.<CsmVariable>maskNull(libVars);
        }

        public Collection<CsmEnumerator> getLibEnumerators() {
            return CompletionResolverImpl.<CsmEnumerator>maskNull(libEnumerators);
        }

        public Collection<CsmFunction> getLibFunctions() {
            return CompletionResolverImpl.<CsmFunction>maskNull(libFuns);
        }

        public Collection<CsmNamespace> getLibNamespaces() {
            return CompletionResolverImpl.<CsmNamespace>maskNull(libNSs);
        }

        public Collection<CsmNamespaceAlias> getLibNamespaceAliases() {
            return CompletionResolverImpl.<CsmNamespaceAlias>maskNull(libNsAliases);
        }

        public Collection<CsmTemplateParameter> getTemplateparameters() {
            return CompletionResolverImpl.<CsmTemplateParameter>maskNull(templateParameters);
        }

        @SuppressWarnings("unchecked")
        public Collection<? extends CsmObject> addResulItemsToCol(Collection<? extends CsmObject> orig) {
            assert orig != null;
            return appendResult(orig, this);
        }

        int size = -1;
        public int size() {
            if (size == -1) {
                size = 0;
                // init size value
                size += getLocalVariables().size();

                size += getClassFields().size();

                size += getClassEnumerators().size();

                size += getClassMethods().size();

                size += getProjectClassesifiersEnums().size();

                size += getFileLocalVars().size();

                size += getFileLocalEnumerators().size();

                size += getFileLocalMacros().size();

                size += getFileLocalFunctions().size();

                size += getInFileIncludedProjectMacros().size();

                size += getGlobalVariables().size();

                size += getGlobalEnumerators().size();

                size += getGlobalProjectMacros().size();

                size += getGlobalProjectFunctions().size();

                size += getGlobalProjectNamespaces().size();

                size += getLibClassifiersEnums().size();

                size += getInFileIncludedLibMacros().size();

                size += getLibMacros().size();

                size += getLibVariables().size();

                size += getLibEnumerators().size();

                size += getLibFunctions().size();

                size += getLibNamespaces().size();

                size += getTemplateparameters().size();
            }
            return size;
        }

    }

    private static final Result EMPTY_RESULT = new EmptyResultImpl();
    private static final class EmptyResultImpl implements Result {
        public Collection<CsmVariable> getLocalVariables() {
            return Collections.<CsmVariable>emptyList();
        }

        public Collection<CsmField> getClassFields() {
            return Collections.<CsmField>emptyList();
        }

        public Collection<CsmEnumerator> getClassEnumerators() {
            return Collections.<CsmEnumerator>emptyList();
        }

        public Collection<CsmMethod> getClassMethods() {
            return Collections.<CsmMethod>emptyList();
        }

        public Collection<CsmClassifier> getProjectClassesifiersEnums() {
            return Collections.<CsmClassifier>emptyList();
        }

        public Collection<CsmVariable> getFileLocalVars() {
            return Collections.<CsmVariable>emptyList();
        }

        public Collection<CsmEnumerator> getFileLocalEnumerators() {
            return Collections.<CsmEnumerator>emptyList();
        }

        public Collection<CsmMacro> getFileLocalMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        public Collection<CsmFunction> getFileLocalFunctions() {
            return Collections.<CsmFunction>emptyList();
        }

        public Collection<CsmMacro> getInFileIncludedProjectMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        public Collection<CsmVariable> getGlobalVariables() {
            return Collections.<CsmVariable>emptyList();
        }

        public Collection<CsmEnumerator> getGlobalEnumerators() {
            return Collections.<CsmEnumerator>emptyList();
        }

        public Collection<CsmMacro> getGlobalProjectMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        public Collection<CsmFunction> getGlobalProjectFunctions() {
            return Collections.<CsmFunction>emptyList();
        }

        public Collection<CsmNamespace> getGlobalProjectNamespaces() {
            return Collections.<CsmNamespace>emptyList();
        }

        public Collection<CsmClassifier> getLibClassifiersEnums() {
            return Collections.<CsmClassifier>emptyList();
        }

        public Collection<CsmMacro> getInFileIncludedLibMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        public Collection<CsmMacro> getLibMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        public Collection<CsmVariable> getLibVariables() {
            return Collections.<CsmVariable>emptyList();
        }

        public Collection<CsmEnumerator> getLibEnumerators() {
            return Collections.<CsmEnumerator>emptyList();
        }

        public Collection<CsmFunction> getLibFunctions() {
            return Collections.<CsmFunction>emptyList();
        }

        public Collection<CsmNamespace> getLibNamespaces() {
            return Collections.<CsmNamespace>emptyList();
        }

        public Collection<? extends CsmObject> addResulItemsToCol(Collection<? extends CsmObject> orig) {
            return orig;
        }

        public int size() {
            return 0;
        }

        public Collection<CsmNamespaceAlias> getProjectNamespaceAliases() {
            return Collections.<CsmNamespaceAlias>emptyList();
        }

        public Collection<CsmNamespaceAlias> getLibNamespaceAliases() {
            return Collections.<CsmNamespaceAlias>emptyList();
        }

        public Collection<CsmTemplateParameter> getTemplateparameters() {
            return Collections.<CsmTemplateParameter>emptyList();
        }
    }

    private static <T> Collection<T> maskNull(Collection<T> list) {
        return list != null ? list : Collections.<T>emptyList();
    }

    private static <T> Collection appendResult(Collection<T> dest, ResultImpl result) {
        assert dest != null;
        // local vars
        merge(dest, result.localVars);
        // add class fields
        merge(dest, result.classFields);
        // add class enumerators
        merge(dest, result.classEnumerators);
        // add class methods
        merge(dest, result.classMethods);
        // add classesEnumsTypedefs
        merge(dest, result.classesEnumsTypedefs);
        // add file local variables
        merge(dest, result.fileLocalVars);
        merge(dest, result.fileLocalEnumerators);
        merge(dest, result.fileLocalMacros);
        merge(dest, result.fileLocalFunctions);
        merge(dest, result.fileProjectMacros);
        // add global variables
        merge(dest, result.globVars);
        // add global enumerators
        merge(dest, result.globEnumerators);
        // global macros
        merge(dest, result.globProjectMacros);
        // add global functions
        merge(dest, result.globFuns);
        // add namespaces
        merge(dest, result.globProjectNSs);
        // add namespace aliases
        merge(dest, result.projectNsAliases);
        // add libraries classesEnumsTypedefs
        merge(dest, result.libClasses);
        merge(dest, result.fileLibMacros);
        merge(dest, result.globLibMacros);
        // add libraries variables
        merge(dest, result.libVars);
        // add libraries enumerators
        merge(dest, result.libEnumerators);
        // add libraries functions
        merge(dest, result.libFuns);
        // add libraries namespaces
        merge(dest, result.libNSs);
        // add libraries namespace aliases
        merge(dest, result.libNsAliases);

        return dest;
    }

    private void initResolveMask(final CsmContext context, int offset, final String strPrefix, boolean match) {
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            if (strPrefix.length() == 0) {
                resolveTypes |= RESOLVE_FILE_LOCAL_MACROS | RESOLVE_FILE_PRJ_MACROS;
            } else {
                resolveTypes |= RESOLVE_FILE_LOCAL_MACROS | RESOLVE_GLOB_MACROS | RESOLVE_LIB_MACROS;
            }

            // resolve classes always
            resolveTypes |= RESOLVE_CONTEXT_CLASSES;

            // namespaces and classes could be everywhere, hide should decide what to disable
            resolveTypes |= RESOLVE_CLASSES;
            resolveTypes |= RESOLVE_TEMPLATE_PARAMETERS;
            resolveTypes |= RESOLVE_GLOB_NAMESPACES;
            resolveTypes |= RESOLVE_LIB_CLASSES;
            resolveTypes |= RESOLVE_LIB_NAMESPACES;
            resolveTypes |= RESOLVE_CLASS_NESTED_CLASSIFIERS;
            resolveTypes |= RESOLVE_FILE_LOCAL_VARIABLES;

            // FIXUP: after we made static consts in headers belong to namespace,
            // in constuct below usage of globalVarUsedInArrayIndex became unresolved
            // const int globalVarUsedInArrayIndex;
            // struct UsingGlobalVarInArrayIndex {
            //     int data[globalVarUsedInArrayIndex];
            // };
            // TODO: solve this issue in a more elegant way
            resolveTypes |= RESOLVE_GLOB_VARIABLES;

            assert (context != null);
            if (CsmContextUtilities.isInFunction(context, offset)) {
                // for speed up remember result
                updateResolveTypesInFunction(offset, context, match);
            } else if (CsmContextUtilities.getClass(context, false, true) != null) {
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASS_FIELDS;
                resolveTypes |= RESOLVE_CLASS_METHODS;
                resolveTypes |= RESOLVE_CLASS_ENUMERATORS;
            } else {

                // resolve global context as well
                resolveTypes |= RESOLVE_GLOB_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_ENUMERATORS;
                resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
                resolveTypes |= RESOLVE_FILE_LOCAL_FUNCTIONS;
                resolveTypes |= RESOLVE_GLOB_NAMESPACES;
                resolveTypes |= RESOLVE_LIB_CLASSES;
                resolveTypes |= RESOLVE_LIB_VARIABLES;
                resolveTypes |= RESOLVE_LIB_ENUMERATORS;
                resolveTypes |= RESOLVE_LIB_FUNCTIONS;
                resolveTypes |= RESOLVE_LIB_NAMESPACES;
            }
        }
    }

    private Collection<CsmDeclaration> getUsedDeclarations(CsmFile file, int offset, String prefix, boolean match, CsmDeclaration.Kind[] kinds) {
        CsmProject prj = file.getProject();
        CsmProject inProject = prefix.length() == 0 ? prj : null;
        Collection<CsmDeclaration> usedDecls = CsmUsingResolver.getDefault().findUsedDeclarations(file, offset, inProject);
        Collection<CsmDeclaration> out = filterDeclarations(usedDecls, prefix, match, kinds);
        return out;
    }

    private Collection<CsmDeclaration> filterDeclarations(Collection<CsmDeclaration> orig, String prefix, boolean match,  CsmDeclaration.Kind[] kinds) {
        LinkedHashSet<CsmDeclaration> out = new LinkedHashSet<CsmDeclaration>(orig.size());
        contResolver.filterDeclarations(orig.iterator(), out, kinds, prefix, match, false);
        return out;
    }

    private Collection<CsmNamespace> getNamespacesToSearch(CsmContext context, CsmFile file, int offset, boolean onlyInProject,boolean contextOnly) {
        CsmProject prj = file.getProject();
        CsmProject inProject = (onlyInProject || contextOnly) ? prj : null;
        Collection<CsmNamespace> namespaces = new ArrayList<CsmNamespace>();
        if (!contextOnly) {
            namespaces.addAll(CsmUsingResolver.getDefault().findVisibleNamespaces(file, offset, inProject));
        }
        // add global namespace
        CsmNamespace globNS = prj.getGlobalNamespace();
        namespaces.add(globNS);
        // add all namespaces from context
        Collection<CsmNamespace> contextNSs = getContextNamespaces(context);
        namespaces.addAll(contextNSs);
        namespaces = filterNamespaces(namespaces, inProject);
        return namespaces;
    }

    private Collection<CsmNamespace> getContextNamespaces(CsmContext context) {
        CsmNamespace ns = CsmContextUtilities.getNamespace(context);
        Collection<CsmNamespace> out = new ArrayList<CsmNamespace>();
        while (ns != null && !ns.isGlobal()) {
            out.add(ns);
            ns = ns.getParent();
        }
        return out;
    }

    private Collection<CsmNamespace> filterNamespaces(Collection<CsmNamespace> orig, CsmProject prj) {
        LinkedHashSet<CsmNamespace> out = new LinkedHashSet<CsmNamespace>(orig.size());
        for (CsmNamespace ns : orig) {
            if (ns != null && (prj == null || ns.getProject() == prj)) {
                out.add(ns);
            }
        }
        return out;
    }

    //private static Map<CacheEntry, Result> cache = new ConcurrentHashMap<CacheEntry, Result>();
    private static ThreadLocal<Map<CacheEntry, Result>> threadCache = new ThreadLocal<Map<CacheEntry, Result>>();
    private static synchronized Map<CacheEntry, Result> getCache(){
        Map<CacheEntry, Result> cache = threadCache.get();
        if (cache == null) {
            cache = new HashMap<CacheEntry, Result>();
            threadCache.set(cache);
        }
        return cache;
    }
        
    private static class CacheEntry {
        private int resolve;
        private int hide;
        private String name;
        private CsmUID function;

        private CacheEntry(int resolve, int hide, String name, CsmUID function){
            this.resolve = resolve;
            this.hide = hide;
            this.name = name;
            this.function = function;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CacheEntry)){
                return false;
            }
            CacheEntry o = (CacheEntry) obj;
            return resolve == o.resolve && hide == o.hide &&
                   name.equals(o.name) && function.equals(o.function);
        }

        @Override
        public int hashCode() {
            return resolve + 17*(hide + 17*(name.hashCode()+17*function.hashCode()));
        }

    }
}
