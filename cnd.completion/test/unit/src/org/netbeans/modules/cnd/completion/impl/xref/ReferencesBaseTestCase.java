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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppAbstractTokenProcessor;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ReferencesBaseTestCase extends ProjectBasedTestCase {

    public ReferencesBaseTestCase(String testName) {
        super(testName);
    }  
    
    protected final void performTest(String source) throws Exception {
        File testSourceFile = getDataFile(source);
        CsmFile csmFile = getCsmFile(testSourceFile);
        BaseDocument doc = getBaseDocument(testSourceFile);
        log("creating list of references:");
        MyTP tp = new MyTP(csmFile, doc);
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, 0);
        CndLexerUtilities.processTokens(tp, cppTokenSequence, 0, doc.getLength());
        log("end of references list");
        log("start resolving referenced objects");
        for (ReferenceImpl ref : tp.references) {
            CsmObject owner = ref.getOwner();
            ref(ref.toString());
            ref("--OWNER:\n    " + CsmTracer.toString(owner));
            CsmObject out = ref.getReferencedObject();
            ref("--RESOLVED TO:\n    " + CsmTracer.toString(out));
            ref("==============================================================");
        }
        log("end of resolving referenced objects");
        compareReferenceFiles();
    }

    protected static boolean supportReference(CppTokenId tokenID) {
        assert tokenID != null;
        switch (tokenID) {
            case IDENTIFIER:
            case PREPROCESSOR_IDENTIFIER:
            case PREPROCESSOR_USER_INCLUDE:
            case PREPROCESSOR_SYS_INCLUDE:
                return true;
        }
        return false;
    }

    private final class MyTP extends CppAbstractTokenProcessor {
        final List<ReferenceImpl> references = new ArrayList<ReferenceImpl>();
        private final CsmFile csmFile;
        private final BaseDocument doc;

        MyTP(CsmFile csmFile, BaseDocument doc) {
            this.csmFile = csmFile;
            this.doc = doc;
        }

        @Override
        public void token(Token<CppTokenId> token, int tokenOffset) {
            if (supportReference(token.id())) {
                ReferenceImpl ref = ReferencesSupport.createReferenceImpl(csmFile, doc, tokenOffset, token);
                assertNotNull("reference must not be null for valid token " + token, ref);
                references.add(ref);
                log(ref.toString());
            }
        }
    }
}
