/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.highlight.error;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.openide.util.NbBundle;

/**
 * Provides information about unresolved identifiers.
 *
 * @author Alexey Vladykin
 */
public class IdentifierErrorProvider extends CsmErrorProvider {

    private static final boolean ENABLED =
            getBoolean("cnd.identifier.error.provider", true); //NOI18N

    @Override
    public Collection<CsmErrorInfo> getErrors(BaseDocument doc, CsmFile file) {
        final Collection<CsmErrorInfo> result = new ArrayList<CsmErrorInfo>();
        if (ENABLED && file.isParsed()) {
            CsmFileReferences.getDefault().accept(file, new CsmFileReferences.Visitor() {
                public void visit(CsmReference ref) {
                    if (ref.getReferencedObject() == null) {
                        result.add(new IdentifierErrorInfo(ref.getStartOffset(),
                                ref.getEndOffset(), ref.getText().toString()));
                    }
                }
            }, CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
        }
        return result;
    }

    private static class IdentifierErrorInfo implements CsmErrorInfo {

        private final int startOffset;
        private final int endOffset;
        private final String message;

        public IdentifierErrorInfo(int startOffset, int endOffset, String name) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.message = NbBundle.getMessage(IdentifierErrorProvider.class,
                    "HighlightProvider_IdentifierMissed", name); //NOI18N
        }

        public String getMessage() {
            return message;
        }

        public Severity getSeverity() {
            return Severity.ERROR;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

    }
    
    private static boolean getBoolean(String name, boolean result) {
        String value = System.getProperty(name);
        if (value != null) {
            result = Boolean.parseBoolean(value);
        }
        return result;
    }

}
