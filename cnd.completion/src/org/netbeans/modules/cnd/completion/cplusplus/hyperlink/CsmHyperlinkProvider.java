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


package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.services.CsmFunctionDefinitionResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
 * Implementation of the hyperlink provider for C/C++ language.
 * <br>
 * The hyperlinks are constructed for identifiers.
 * <br>
 * The click action corresponds to performing the goto-declaration action.
 *
 * @author Jan Lahoda, Vladimir Voskresensky
 */
public final class CsmHyperlinkProvider extends CsmAbstractHyperlinkProvider {
    public CsmHyperlinkProvider() {
    }
    
    protected void performAction(final Document doc, final JTextComponent target, final int offset) {
        goToDeclaration(doc, target, offset);
    }
    
    protected boolean isValidToken(Token<CppTokenId> token) {
        return isSupportedToken(token);
    }
    
    public static boolean isSupportedToken(Token<CppTokenId> token) {
        if (token != null) {
            switch (token.id()) {
                case IDENTIFIER:
                case PREPROCESSOR_IDENTIFIER:
                case OPERATOR:
                    return true;
            }
        }
        return false;
    }
    
    public boolean goToDeclaration(Document doc, JTextComponent target, int offset) {
        if (!preJump(doc, target, offset, "opening-csm-element")) { //NOI18N
            return false;
        }
        Token<CppTokenId> jumpToken = getJumpToken();
        CsmOffsetable item = findTargetObject(target, doc, jumpToken, offset);
        return postJump(item, "goto_source_source_not_found", "cannot-open-csm-element"); //NOI18N
    }
    
    /*package*/ CsmOffsetable findTargetObject(final JTextComponent target, final Document doc, final Token jumpToken, final int offset) {
        CsmOffsetable item = null;
        assert jumpToken != null;
        CsmFile file = CsmUtilities.getCsmFile(doc, true);
        CsmObject csmObject = file == null ? null : ReferencesSupport.findDeclaration(file, doc, jumpToken, offset);
        if (csmObject != null) {
            // convert to jump object
            item = toJumpObject(csmObject, file, offset);
        }
        return item;
    }
    
    private CsmOffsetable toJumpObject(CsmObject csmObject, CsmFile csmFile, int offset) {
        CsmOffsetable item = null;
        if (CsmKindUtilities.isOffsetable(csmObject)) {
            item = (CsmOffsetable)csmObject;
            if (CsmKindUtilities.isFunctionDeclaration(csmObject)) {
                // check if we are in function definition name => go to declaration
                // else it is more useful to jump to definition of function
                CsmFunctionDefinition definition = ((CsmFunction)csmObject).getDefinition();
                if (definition != null) {
                    if (csmFile.equals(definition.getContainingFile()) &&
                            (definition.getStartOffset() <= offset &&
                            offset <= definition.getBody().getStartOffset())
                            ) {
                        // it is ok to jump to declaration
                        if (definition.getDeclaration() != null) {
                            item = definition.getDeclaration();
                        } else if (csmObject.equals(definition)) {
                            item = (CsmOffsetable)csmObject;
                        }
                    } else {
                        // it's better to jump to definition
                        item = definition;
                    }
                } else {
                    CsmReference ref = CsmFunctionDefinitionResolver.getDefault().getFunctionDefinition((CsmFunction)csmObject);
                    if (ref != null){
                        item = ref;
                    }
                }
            }else if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                CsmFunctionDefinition definition = (CsmFunctionDefinition)csmObject;
                if (csmFile.equals(definition.getContainingFile()) &&
                        (definition.getStartOffset() <= offset &&
                        offset <= definition.getBody().getStartOffset())
                        ) {
                    // it is ok to jump to declaration
                    if (definition.getDeclaration() != null) {
                        item = definition.getDeclaration();
                    } else if (csmObject.equals(definition)) {
                        item = (CsmOffsetable)csmObject;
                    }
                }                
            } else if (CsmKindUtilities.isVariableDeclaration(csmObject)) {
                // check if we are in variable definition name => go to declaration
                CsmVariableDefinition definition = ((CsmVariable)csmObject).getDefinition();
                if (definition != null) {
                    item = definition;
                    if (csmFile.equals(definition.getContainingFile()) &&
                            (definition.getStartOffset() <= offset &&
                            offset <= definition.getEndOffset())) {
                        item = (CsmVariable)csmObject;
                    }
                }
            }
        } else if (CsmKindUtilities.isNamespace(csmObject)) {
            // get all definitions of namespace, but prefer the definition in this file
            CsmNamespace nmsp = (CsmNamespace)csmObject;
            Collection<CsmNamespaceDefinition> defs = nmsp.getDefinitions();
            CsmNamespaceDefinition bestDef = null;
            for (CsmNamespaceDefinition def : defs) {
                if (bestDef == null) {
                    // first time initialization
                    bestDef = def;
                }
                CsmFile container = def.getContainingFile();
                if (csmFile.equals(container)) {
                    // this is the best choice
                    bestDef = def;
                    break;
                }
            }
            item = bestDef;
        }
        return item;
    }
}
