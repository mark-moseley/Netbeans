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

package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.Rule;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.PHPParseResult;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPHintsProvider implements HintsProvider {
    public static final String FIRST_PASS_HINTS = "1st pass"; //NOI18N
    public static final String SECOND_PASS_HINTS = "2nd pass"; //NOI18N

    public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
        Map<String, List> allHints = (Map) manager.getHints(false, context);
        CompilationInfo info = context.compilationInfo;

        PHPVerificationVisitor visitor = new PHPVerificationVisitor((PHPRuleContext)context, allHints.get(FIRST_PASS_HINTS));
        
        for (PHPParseResult parseResult : ((List<PHPParseResult>) info.getEmbeddedResults(PHPLanguage.PHP_MIME_TYPE))) {
            if (parseResult.getProgram() != null) {
                parseResult.getProgram().accept(visitor);
            }
            
            hints.addAll(visitor.getResult());
        }
        
        assert allHints.get(SECOND_PASS_HINTS).size() == 1;
        UnusedVariableRule unusedVariableRule = (UnusedVariableRule) allHints.get(SECOND_PASS_HINTS).get(0);
        unusedVariableRule.check((PHPRuleContext) context, hints);
    }

    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
        
    }

    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {
        
    }

    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        ParserResult parserResult = context.parserResult;
        if (parserResult != null) {
            List<Error> errors = parserResult.getDiagnostics();
            unhandled.addAll(errors);
        }
    }

    public void cancel() {
        
    }

    public List<Rule> getBuiltinRules() {
        return Collections.<Rule>emptyList();
    }

    public RuleContext createRuleContext() {
        return new PHPRuleContext();
    }
}
