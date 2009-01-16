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

package org.netbeans.modules.php.editor;

import java.io.IOException;
import java.util.Collections;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;
import static org.netbeans.modules.php.editor.CompletionContextFinder.CompletionContext;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPCodeTemplateFilter extends UserTask implements CodeTemplateFilter  {
    private boolean accept = false;
    private int caretOffset;
    private CompletionContext context;

    public PHPCodeTemplateFilter(JTextComponent component, int offset) {
        this.caretOffset = offset;
        try {
            ParserManager.parse(Collections.singleton(Source.create(component.getDocument())), this);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    public boolean accept(CodeTemplate template) {
        if (template.getContexts().contains("php-code")) //NOI18N
        {
            if (context == CompletionContext.CLASS_CONTEXT_KEYWORDS) {
                return template.getAbbreviation().equals("fnc");//NOI18N
            }
            return accept;
        }
        return true;
    }

    @Override
    public void run(ResultIterator resultIterator) throws Exception {
        ParserResult parameter = (ParserResult) resultIterator.getParserResult();
        BaseDocument document = (BaseDocument) parameter.getSnapshot().getSource().getDocument(false);
        document.readLock();

        try {
            context = CompletionContextFinder.findCompletionContext(parameter, caretOffset);
            switch (context) {
                case EXPRESSION:
                    accept = true;
                    break;
                case CLASS_CONTEXT_KEYWORDS:
                    accept = true;
                    break;
            }
        } finally {
            document.readUnlock();
        }
    }

    public static final class Factory implements CodeTemplateFilter.Factory {

        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new PHPCodeTemplateFilter(component, offset);
        }
    }
}
