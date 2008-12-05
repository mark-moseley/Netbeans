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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.hints;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.javascript.editing.JsTestBase;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.hints.infrastructure.JsAstRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsErrorRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsHintsProvider;

/**
 * Common utility methods for testing a hint
 *
 * @author Tor Norbye
 */
public abstract class HintTestBase extends JsTestBase {

    public HintTestBase(String testName) {
        super(testName);
    }

    @Override
    protected HintsProvider getHintsProvider() {
        return new JsHintsProvider();
    }
    
    @SuppressWarnings("unchecked")
    public void ensureRegistered(JsAstRule hint) throws Exception {
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(JsTokenId.JAVASCRIPT_MIME_TYPE);
        assertNotNull(language.getHintsProvider());
        GsfHintsManager hintsManager = language.getHintsManager();
        Map<Integer, List<JsAstRule>> hints = (Map)hintsManager.getHints();
        Set<Integer> kinds = hint.getKinds();
        for (Integer nodeType : kinds) {
            List<JsAstRule> rules = hints.get(nodeType);
            assertNotNull(rules);
            boolean found = false;
            for (JsAstRule rule : rules) {
                if (rule.getClass() == hint.getClass()) {
                    found  = true;
                    break;
                }
            }
            
            assertTrue(found);
        }
    }

    @SuppressWarnings("unchecked")
    public void ensureRegistered(JsErrorRule hint) throws Exception {
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(JsTokenId.JAVASCRIPT_MIME_TYPE);
        assertNotNull(language.getHintsProvider());
        GsfHintsManager hintsManager = language.getHintsManager();
        Map<Integer, List<JsErrorRule>> hints = (Map)hintsManager.getErrors();
        Set<String> kinds = hint.getCodes();
        for (String codes : kinds) {
            List<JsErrorRule> rules = hints.get(codes);
            assertNotNull(rules);
            boolean found = false;
            for (JsErrorRule rule : rules) {
                if (rule.getClass() == hint.getClass()) {
                    found  = true;
                    break;
                }
            }
            
            assertTrue(found);
        }
    }
}
