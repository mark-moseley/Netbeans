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

package org.netbeans.modules.php.editor.codegen;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public class PHPCodeTemplateProcessor implements CodeTemplateProcessor {

    private final static String NEW_VAR_NAME = "newVarName"; // NOI18N

    private final CodeTemplateInsertRequest request;
    // @GuardedBy("this")
    private CompilationInfo info;

    public PHPCodeTemplateProcessor(CodeTemplateInsertRequest request) {
        this.request = request;
    }

    public void updateDefaultValues() {
        for (CodeTemplateParameter param : request.getMasterParameters()) {
            String value = getProposedValue(param);
            if (value != null && !value.equals(param.getValue())) {
                param.setValue(value);
            }
        }
    }

    public void parameterValueChanged(CodeTemplateParameter masterParameter, boolean typingChange) {
        // No op.
    }

    public void release() {
        // No op.
    }

    private String getProposedValue(CodeTemplateParameter param) {
        for (Entry<String, String> entry : param.getHints().entrySet()) {
            String hintName = entry.getKey();
            if (NEW_VAR_NAME.equals(hintName)) {
                return newVarName(param.getValue());
            }
        }
        return null;
    }

    private String newVarName(final String proposed) {
        if (!initParsing()) {
            return null;
        }
        final int caretOffset = request.getComponent().getCaretPosition();
        int suffix = 0;
        final String[] nue = { null };
        synchronized (this) {
            for (;;) {
                nue[0] = proposed + (suffix > 0 ? String.valueOf(suffix) : "");
                Set<String> varInScope = ASTNodeUtilities.getVariablesInScope(info, caretOffset, new ASTNodeUtilities.VariableAcceptor() {
                    public boolean acceptVariable(String variableName) {
                        return nue[0].equals(variableName);
                    }
                });
                if (varInScope.isEmpty()) {
                    break;
                }
                ++suffix;
            }
        }
        return nue[0];
    }

    private synchronized boolean initParsing() {
        if (info != null) {
            return true;
        }
        Document doc = request.getComponent().getDocument();
        FileObject file = NavUtils.getFile(doc);
        if (file == null) {
            return false;
        }
        SourceModel model = SourceModelFactory.getInstance().getModel(file);
        final String[] nue = { null };
        try {
            model.runUserActionTask(new CancellableTask<CompilationInfo>() {
                public void cancel() {
                }
                public void run(CompilationInfo info) throws IOException {
                    PHPCodeTemplateProcessor.this.info = info;
                }
            }, false);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            info = null;
            return false;
        }
        return true;
    }

    public static final class Factory implements CodeTemplateProcessorFactory {

        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            return new PHPCodeTemplateProcessor(request);
        }
    }
}
