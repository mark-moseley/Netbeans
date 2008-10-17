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

package org.netbeans.modules.groovy.editor.hints.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.api.GroovyCompilerErrorID;
import org.netbeans.modules.groovy.editor.api.parser.GroovyError;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.Rule;
import org.netbeans.modules.gsf.api.RuleContext;

/**
 *
 * @author schmidtm
 */
public class GroovyHintsProvider implements HintsProvider {
    
    public static final Logger LOG = Logger.getLogger(GroovyHintsProvider.class.getName()); // NOI18N
    private boolean cancelled;

    public RuleContext createRuleContext() {
        return new GroovyRuleContext();
    }
    
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
        return;
    }

    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
        return;
    }

    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> result, int start, int end) {
        cancelled = false;
        ParserResult parserResult = context.parserResult;
        if (parserResult == null) {
            return;
        }
        
        ASTNode root = AstUtilities.getRoot(parserResult);

        if (root == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<GroovySelectionRule> hints = (List)manager.getSelectionHints();

        if (hints.isEmpty()) {
            return;
        }
        
        if (isCancelled()) {
            return;
        }
        
        applyRules(context, hints, start, end, result);
    }

    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> result, List<Error> unhandled) {
        // Return all the errors we -haven't- added custom error hints for:
        
        // LOG.setLevel(Level.FINEST);
        LOG.log(Level.FINEST, "@@@ computeErrors()");

        CompilationInfo info = context.compilationInfo;
        GroovyParserResult rpr = AstUtilities.getParseResult(info);
        
        if (rpr == null) {
            return;
        }

        List<Error> errors = rpr.getDiagnostics();
        LOG.log(Level.FINEST, "@@@ errors.size() : {0}", errors.size());

        if (errors == null || errors.size() == 0) {
            return;
        }

        cancelled = false;
        
        @SuppressWarnings("unchecked")
        Map<GroovyCompilerErrorID,List<GroovyErrorRule>> hints = (Map)manager.getErrors();

        if (hints.isEmpty() || isCancelled()) {
            unhandled.addAll(errors);
            return;
        }
        LOG.log(Level.FINEST, "@@@ hints.size() : {0}", hints.size());
        
        for (Error error : errors) {
            if (error instanceof GroovyError) {
                LOG.log(Level.FINEST, "@@@ ----------------------------------------------------\n");
                LOG.log(Level.FINEST, "@@@ error.getDescription()   : {0}\n", error.getDescription());
                LOG.log(Level.FINEST, "@@@ error.getKey()           : {0}\n", error.getKey());
                LOG.log(Level.FINEST, "@@@ error.getDisplayName()   : {0}\n", error.getDisplayName());
                LOG.log(Level.FINEST, "@@@ error.getStartPosition() : {0}\n", error.getStartPosition());
                LOG.log(Level.FINEST, "@@@ error.getEndPosition()   : {0}\n", error.getEndPosition());
                if (!applyRules((GroovyError) error, context, hints, result)) {
                    LOG.log(Level.FINEST, "@@@ Adding error to unhandled");
                    unhandled.add(error);
                }
            }
        }
        LOG.log(Level.FINEST, "@@@ result.size() =  {0}", result.size());
    }

    public void cancel() {
        return;
    }

    public List<Rule> getBuiltinRules() {
        return null;
    }

    private boolean isCancelled() {
        return cancelled;
    }
    
    private void applyRules(RuleContext context, List<GroovySelectionRule> rules, int start, int end, 
            List<Hint> result) {

        GroovyRuleContext groovyContext = (GroovyRuleContext)context;
        
        for (GroovySelectionRule rule : rules) {
            if (!rule.appliesTo(context)) {
                continue;
            }

            rule.run(groovyContext, result);
        }
    }

    /** Apply error rules and return true iff somebody added an error description for it */
    private boolean applyRules(GroovyError error, RuleContext context, Map<GroovyCompilerErrorID,List<GroovyErrorRule>> hints,
            List<Hint> result) {
        
       // LOG.setLevel(Level.FINEST);
       LOG.log(Level.FINEST, "applyRules(...)");
        
        GroovyCompilerErrorID code = error.getId();
        if (code != null) {
            List<GroovyErrorRule> rules = hints.get(code);

            if (rules != null) {
                int countBefore = result.size();
                GroovyRuleContext groovyContext = (GroovyRuleContext)context;
                
                for (GroovyErrorRule rule : rules) {
                    if (!rule.appliesTo(context)) {
                        continue;
                    }
                    rule.run(groovyContext, error, result);
                }
                
                return countBefore < result.size();
            }
        }
        
        return false;
    }    
}
