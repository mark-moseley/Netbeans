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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.cnd.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CppLexer extends CndLexer {

    private final Filter<CppTokenId> lexerFilter;
    @SuppressWarnings("unchecked")
    public CppLexer(Filter<CppTokenId> defaultFilter, LexerRestartInfo<CppTokenId> info) {
        super(info);
        Filter<CppTokenId> filter = (Filter<CppTokenId>) info.getAttributeValue("lexer-filter"); // NOI18N
        this.lexerFilter = filter != null ? filter : defaultFilter;
    }
        
    @SuppressWarnings("fallthrough")
    @Override
    protected Token<CppTokenId> finishSharp() {
        // one prerpocessor directive block
        while (true) {
            switch (read(true)) {
                case '\r': 
                    consumeNewline(); 
                    // nobreak
                case '\n':
                case EOF:
                    return token(CppTokenId.PREPROCESSOR_DIRECTIVE);
            }
        }
    }

    @Override
    protected CppTokenId getKeywordOrIdentifierID(CharSequence text) {
        CppTokenId id = lexerFilter.check(text);
        return id != null ? id : CppTokenId.IDENTIFIER;
    }
}
