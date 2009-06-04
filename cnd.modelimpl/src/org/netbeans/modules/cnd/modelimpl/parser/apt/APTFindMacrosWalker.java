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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTElif;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTIf;
import org.netbeans.modules.cnd.apt.structure.APTIfdef;
import org.netbeans.modules.cnd.apt.structure.APTIfndef;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;


/**
 * Walker to find macros used in file for semantic highlighting
 *
 * @author Sergey Grinev
 * @author Vladimir Voskresensky
 */
public final class APTFindMacrosWalker extends APTSelfWalker {
    private final List<CsmReference> references = new ArrayList<CsmReference>();
    private final CsmFile csmFile;
    public APTFindMacrosWalker(APTFile apt, CsmFile csmFile, APTPreprocHandler preprocHandler, APTFileCacheEntry cacheEntry) {
        super(apt, preprocHandler, cacheEntry);
        this.csmFile = csmFile;
    }

    @Override
    protected void onDefine(APT apt) {
        APTDefine defineNode = (APTDefine) apt;
        int index = references.size();
        analyzeList(defineNode.getBody());
        super.onDefine(apt);
        APTToken name = defineNode.getName();
        if (name != null) {
            APTMacro m = getMacroMap().getMacro(name);
            if (m != null) {
                MacroReference mr = new MacroReference(csmFile, name, m, CsmReferenceKind.DECLARATION);
                if (references.size() == index) {
                    references.add(mr);
                } else {
                    references.add(index, mr);
                }
            }
        }
    }

    @Override
    protected boolean onIf(APT apt) {
        analyzeStream(((APTIf) apt).getCondition(), false);
        return super.onIf(apt);
    }

    @Override
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        analyzeStream(((APTElif) apt).getCondition(), false);
        return super.onElif(apt, wasInPrevBranch);
    }

    @Override
    protected boolean onIfndef(APT apt) {
        analyzeToken(((APTIfndef) apt).getMacroName(), false);
        return super.onIfndef(apt);
    }

    @Override
    protected boolean onIfdef(APT apt) {
        analyzeToken(((APTIfdef) apt).getMacroName(), false);
        return super.onIfdef(apt);
    }

    @Override
    protected void onUndef(APT apt) {
        analyzeToken(((APTUndefine) apt).getName(), false);
        super.onUndef(apt);
    }

    @Override
    protected void onInclude(APT apt) {
        analyzeStream(((APTInclude)apt).getInclude(), true);
        super.onInclude(apt);
    }

    @Override
    protected void onIncludeNext(APT apt) {
        analyzeStream(((APTIncludeNext)apt).getInclude(), true);
        super.onIncludeNext(apt);
    }

    public List<CsmReference> collectMacros() {
        TokenStream ts = super.getTokenStream();
        analyzeStream(ts, true);
        return references;
    }

    private CsmReference analyzeToken(APTToken token, boolean addOnlyIfNotFunLikeMacro) {
        CsmReference mf = null;
        boolean funLike = false;
        if (token != null && !APTUtils.isEOF(token)) {
            APTMacro m = getMacroMap().getMacro(token);
            if (m != null) {
                // macro either doesn't need params or has "(" after name
                funLike = m.isFunctionLike();
                switch(m.getKind()){
                    case DEFINED:
                        mf = new MacroReference(csmFile, token, m, CsmReferenceKind.DIRECT_USAGE);
                        break;
                    case COMPILER_PREDEFINED:
                    case POSITION_PREDEFINED:
                    case USER_SPECIFIED:
                    default:
                        mf = new SysMacroReference(csmFile, token, m);
                        break;
                }
            }
        }
        if (mf != null) {
            // add any not fun-like macro
            // or add all if specified by input parameter
            if (!funLike || !addOnlyIfNotFunLikeMacro) {
                references.add(mf);
                // clear return value, because already added
                mf = null;
            }
        }
        return mf;
    }

    private void analyzeList(List<APTToken> tokens) {
        if (tokens != null) {
            for (APTToken token : tokens) {
                analyzeToken(token, false);
            }
        }
    }

    private void analyzeStream(TokenStream ts, boolean checkFunLikeMacro) {
        if (ts != null) {
            try {
                for (APTToken token = (APTToken) ts.nextToken(); !APTUtils.isEOF(token); ) {
                    CsmReference mr = analyzeToken(token, checkFunLikeMacro);
                    token = (APTToken) ts.nextToken();
                    if (mr != null) {
                        // it is fun-like macro candidate
                        assert checkFunLikeMacro;
                        // add only if next token is "("
                        if (token.getType() == APTTokenTypes.LPAREN) {
                            references.add(mr);
                        }
                    }
                }
            } catch (TokenStreamException ex) {
		DiagnosticExceptoins.register(ex);
            }
        }
    }

    private static final class SysMacroReference extends OffsetableBase implements CsmReference {

        private final CsmObject ref;
        private final CharSequence text;
        public SysMacroReference(CsmFile file, APTToken token, APTMacro macro) {
            super(file, token.getOffset(), token.getEndOffset());
            text = token.getTextID();
            CsmMacro.Kind kind;
            switch(macro.getKind()) {
                case COMPILER_PREDEFINED:
                    kind = CsmMacro.Kind.COMPILER_PREDEFINED;
                    break;
                case POSITION_PREDEFINED:
                    kind = CsmMacro.Kind.POSITION_PREDEFINED;
                    break;
                case DEFINED:
                    kind = CsmMacro.Kind.DEFINED;
                    break;
                case USER_SPECIFIED:
                    kind = CsmMacro.Kind.USER_SPECIFIED;
                    break;
                default:
                    System.err.println("unexpected kind in macro " + macro);
                    kind = CsmMacro.Kind.USER_SPECIFIED;
                    break;
            }
            ref = MacroImpl.createSystemMacro(token.getTextID(), APTUtils.stringize(macro.getBody(), false), ((ProjectBase) file.getProject()).getUnresolvedFile(), kind);
        }

        public CsmObject getReferencedObject() {
            return ref;
        }

        public CsmObject getOwner() {
            return getContainingFile();
        }

        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DIRECT_USAGE;
        }

        @Override
        public CharSequence getText() {
            return text;
        }        
    }

    private static final class MacroReference extends OffsetableBase implements CsmReference {

        private volatile CsmMacro ref = null;
        private final CharSequence macroName;
        private final APTMacro macro;
        private final CsmReferenceKind kind;
        public MacroReference(CsmFile macroUsageFile, APTToken macroUsageToken, APTMacro macro, CsmReferenceKind kind) {
            super(macroUsageFile, macroUsageToken.getOffset(), macroUsageToken.getEndOffset());
            this.macroName = macroUsageToken.getTextID();
            assert macroName != null;
            this.macro = macro;
            this.kind = kind;
        }

        public CsmObject getReferencedObject() {
            CsmMacro refObj = ref;
            if (refObj == null && macro != null) {
                synchronized (this) {
                    refObj = ref;
                    if (refObj == null) {
                        int macroStartOffset = macro.getDefineNode().getOffset();
                        CsmFile target = getTargetFile();
                        if (target != null) {
                            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(macroName, true, true, false);
                            for (Iterator<CsmMacro> it = CsmSelect.getMacros(target, filter); it.hasNext();) {
                                CsmMacro targetFileMacro = it.next();
                                if (targetFileMacro!=null && macroStartOffset == targetFileMacro.getStartOffset()) {
                                    refObj = targetFileMacro;
                                    break;
                                }
                            }
                            if (refObj == null) {
                                // reference was made so it was macro during APTFindMacrosWalker's walk. Parser missed this variance of header and
                                // we have to create MacroImpl for skipped filepart on the spot (see IZ#130897)
                                if (target instanceof Unresolved.UnresolvedFile) {
                                    refObj = MacroImpl.createSystemMacro(macroName, "", target, CsmMacro.Kind.USER_SPECIFIED);
                                } else {
                                    refObj = new MacroImpl(macroName, null, "", target, new OffsetableBase(target, macroStartOffset, macroStartOffset + macroName.length()), CsmMacro.Kind.DEFINED);
                                    org.netbeans.modules.cnd.modelimpl.csm.core.Utils.setSelfUID(refObj);
                                }
                            }
                        }
                        ref = refObj;
                    }
                }
            }
            return refObj;
        }

        private CsmFile getTargetFile() {
            CsmFile current = this.getContainingFile();
            CsmFile target;
            if (kind == CsmReferenceKind.DECLARATION) {
                target = current;
            } else {
                target = null;
                CharSequence macroContainerFile = macro.getFile();
                if (current != null && macroContainerFile.length() > 0) {
                    ProjectBase targetPrj = ((ProjectBase) current.getProject()).findFileProject(macroContainerFile);
                    if (targetPrj != null) {
                        target = targetPrj.findFile(macroContainerFile);
                    }
                    // try full model?
                    if (target == null) {
                        target = CsmModelAccessor.getModel().findFile(macroContainerFile);
                    }
                    if (target == null && targetPrj != null) {
                        target = targetPrj.getUnresolvedFile();
                    }
                }
            }
            return target;
        }

        public CsmObject getOwner() {
            return (kind == CsmReferenceKind.DECLARATION) ? getContainingFile() : getReferencedObject();
        }
        
        public CsmReferenceKind getKind() {
            return kind;
        }

        @Override
        public CharSequence getText() {
            return macroName;
        }
    }
}
