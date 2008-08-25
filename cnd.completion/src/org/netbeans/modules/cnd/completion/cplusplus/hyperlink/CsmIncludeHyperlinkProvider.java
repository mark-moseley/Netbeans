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

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.NbBundle;

/**
 * Implementation of the hyperlink provider for java language.
 * <br>
 * The hyperlinks are constructed for #include directives.
 * <br>
 * The click action corresponds to performing the open file action.
 *
 * @author Vladimir Voskresensky
 */
public class CsmIncludeHyperlinkProvider extends CsmAbstractHyperlinkProvider {
    private static final boolean NEED_TO_TRACE_UNRESOLVED_INCLUDE = getBoolean("cnd.modelimpl.trace.failed.include", false); 
   
    
    /** Creates a new instance of CsmIncludeHyperlinkProvider */
    public CsmIncludeHyperlinkProvider() {
    }
    
    protected boolean isValidToken(Token<CppTokenId> token) {
        return isSupportedToken(token);
    }
    
    public static boolean isSupportedToken(Token<CppTokenId> token) {
        if (token != null) {
            switch (token.id()) {
                case PREPROCESSOR_INCLUDE:
                case PREPROCESSOR_INCLUDE_NEXT:
                case PREPROCESSOR_SYS_INCLUDE:
                case PREPROCESSOR_USER_INCLUDE:
                    return true;
            }
        }
        return false;
    }
    
    protected void performAction(final Document originalDoc, final JTextComponent target, final int offset) {
        goToInclude(originalDoc, target, offset);
    }
    
    public boolean goToInclude(Document doc, JTextComponent target, int offset) {
        if (!preJump(doc, target, offset, "opening-include-element")) { //NOI18N
            return false;
        }
        CsmOffsetable item = findTargetObject(doc, offset);
        return postJump(item, "goto_source_source_not_found", "cannot-open-include-element"); //NOI18N
    }

    /*package*/ CsmOffsetable findTargetObject(final Document doc, final int offset) {
        CsmInclude incl = findInclude(doc, offset);
        CsmOffsetable item = incl == null ? null : new IncludeTarget(incl);
        if (incl != null && NEED_TO_TRACE_UNRESOLVED_INCLUDE && incl.getIncludeFile() == null) {
            System.setProperty("cnd.modelimpl.trace.trace_now", "yes"); //NOI18N
            try {
                incl.getIncludeFile();
            } finally {
                System.setProperty("cnd.modelimpl.trace.trace_now", "no"); //NOI18N
            }
        }
        return item;
    }
    
    private CsmInclude findInclude(Document doc, int offset) {
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, true);
        if (csmFile != null) {
            return ReferencesSupport.findInclude(csmFile, offset);
        }
        return null;
    }

    private static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if( text != null ) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    } 

    private static final class IncludeTarget implements CsmOffsetable {
        private CsmInclude include;
        
        public IncludeTarget(CsmInclude include) {
            this.include = include;
        }
        
        public CsmFile getContainingFile() {
            return include.getIncludeFile();
        }
        
        public int getStartOffset() {
            // start of the file
            return DUMMY_POSITION.getOffset();
        }
        
        public int getEndOffset() {
            // DUMMY of the file
            return DUMMY_POSITION.getOffset();
        }
        
        public CsmOffsetable.Position getStartPosition() {
            return DUMMY_POSITION;
        }
        
        public CsmOffsetable.Position getEndPosition() {
            return DUMMY_POSITION;
        }
        
        public String getText() {
            return include.getIncludeName().toString();
        }
        
    }
    
    private static final CsmOffsetable.Position DUMMY_POSITION = new CsmOffsetable.Position() {
        public int getOffset() {
            return -1;
        }

        public int getLine() {
            return -1;
        }

        public int getColumn() {
            return -1;
        }
    };
    
    protected String getTooltipText(Document doc, Token token, int offset) {
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, true);
        CsmInclude target = null;
        if (csmFile != null) {
            target = ReferencesSupport.findInclude(csmFile, offset);
        }
        String msg = null;
        if (target != null) {
            CsmFile targetFile = target.getIncludeFile();
            if (targetFile != null) {
                CharSequence path = targetFile.getAbsolutePath();
                CsmProject targetPrj = targetFile.getProject();
                if (targetPrj.isArtificial() || csmFile.getProject().equals(targetPrj)) {
                    msg = NbBundle.getMessage(CsmIncludeHyperlinkProvider.class, "MSG_TOOLTIP_INCLUDE", path);  //NOI18N 
                } else {
                    msg = NbBundle.getMessage(CsmIncludeHyperlinkProvider.class, 
                            "MSG_TOOLTIP_INCLUDE_FILE_IN_PROJECT", path, targetPrj.getName());  //NOI18N 
                }
            } else {
                msg = NbBundle.getMessage(CsmIncludeHyperlinkProvider.class, 
                        "MSG_TOOLTIP_INCLUDE_UNRESOLVED", target.getText());  //NOI18N 
            }
        }
        return msg;
    }    
}
