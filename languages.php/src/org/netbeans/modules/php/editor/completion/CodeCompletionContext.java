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

package org.netbeans.modules.php.editor.completion;

import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Completable.QueryType;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.model.SourceElement;

/**
 *
 * @author Victor G. Vasilyev
 */
public class CodeCompletionContext {
    
    private SourceElement sourceElement;
    private CompilationInfo compilationInfo;
    private int caretOffset;
    private String prefix;
    private NameKind nameKind;
    private QueryType queryType;
    private boolean caseSensitive;
    private HtmlFormatter formatter;
    
    
   private SourceElement currentSourceElement;
    
    /**
     * Creates code completion context.
     * @param sourceElement current model element  (under caret offset)
     * @param compilationInfo
     * @param caretOffset
     * @param prefix
     * @param nameKind
     * @param queryType
     * @param caseSensitive
     * @param formatter
     */
    public CodeCompletionContext(SourceElement sourceElement,
            CompilationInfo compilationInfo, int caretOffset, String prefix,
            NameKind nameKind, QueryType queryType, boolean caseSensitive,
            HtmlFormatter formatter) {
        this.sourceElement = sourceElement;
        this.compilationInfo = compilationInfo;
        this.caretOffset = caretOffset;
        this.prefix = prefix;
        this.nameKind = nameKind;
        this.queryType = queryType;
        this.caseSensitive = caseSensitive;
        this.formatter = formatter;
        
    }

    public SourceElement getSourceElement() {
        return sourceElement;
    }

    public CompilationInfo getCompilationInfo() {
        return compilationInfo;
    }

    public int getCaretOffset() {
        return caretOffset;
    }

    public String getPrefix() {
        return prefix;
    }

    public NameKind getNameKind() {
        return nameKind;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public HtmlFormatter getFormatter() {
        return formatter;
    }

    public SourceElement getCurrentSourceElement() {
        return currentSourceElement;
    }

    public void setCurrentSourceElement(SourceElement currentSourceElement) {
        this.currentSourceElement = currentSourceElement;
    }
    
    public boolean isEmptyPrefix() {
        return prefix==null || prefix.trim().length() == 0;
    } 

    public int getInsertOffset() {
        return isEmptyPrefix() ? caretOffset : caretOffset - prefix.length();
    }
    
    public static CodeCompletionContext changePrefix(CodeCompletionContext c,
            String newPrefix) {
        CodeCompletionContext newContext = 
                new CodeCompletionContext(c.sourceElement, c.compilationInfo, 
                c.caretOffset, newPrefix, c.nameKind, c.queryType, 
                c.caseSensitive, c.formatter);
        newContext.setCurrentSourceElement(c.currentSourceElement);
        return newContext;
    }
}
