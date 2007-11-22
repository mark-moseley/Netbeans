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
package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.completion.cplusplus.utils.TokenUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
 *
 * @author Sergey Grinev
 */
public class FileReferencesImpl extends CsmFileReferences  {

    public FileReferencesImpl() {
        /*System.err.println("FileReferencesImpl registered");
        CsmModelAccessor.getModel().addProgressListener(new CsmProgressAdapter() {

            @Override
            public void fileParsingStarted(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }
            
            public @Override void fileInvalidated(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }
        });*/
    }
    
//    private final Map<CsmFile, List<CsmReference>> cache = new HashMap<CsmFile, List<CsmReference>>();

    public void accept(CsmFile csmFile, CsmReferenceVisitor visitor) {
        for (CsmReference ref : getIdentifierReferences(csmFile)) {
            visitor.visit(ref);
        }
    }
    
    private List<CsmReference> getIdentifierReferences(CsmFile csmFile) {
        List<CsmReference> out = new ArrayList<CsmReference>();
        BaseDocument doc = ReferencesSupport.getDocument(csmFile);
        assert doc != null;
        List<Token> tokens = TokenUtilities.getTokens(doc);
        for (Token token : tokens) {
            if (token.getTokenID() == CCTokenContext.IDENTIFIER) {
                ReferenceImpl ref = ReferencesSupport.createReferenceImpl(csmFile, doc, token.getStartOffset(), token);
                out.add(ref);
            }
        }
        return out;
    }
}
