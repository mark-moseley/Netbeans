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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Completable;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPCodeCompletion implements Completable {

    private final static String[] PHP_KEYWORDS = {"__FILE__", "exception",
        "__LINE__", "array()", "class", "const", "continue", "die()", "empty()", "endif",
        "eval()", "exit()", "for", "foreach", "function", "global", "if",
        "include()", "include_once()", "isset()", "list()", "new",
        "print()", "require()", "require_once()", "return()", "static",
        "switch", "unset()", "use", "var", "while",
        "__FUNCTION__", "__CLASS__", "__METHOD__", "final", "php_user_filter",
        "interface", "implements", "extends", "public", "private",
        "protected", "abstract", "clone", "try", "catch", "throw"
    };
    
    private boolean caseSensitive;

    public List<CompletionProposal> complete(CompilationInfo info, int caretOffset, String prefix, NameKind kind, QueryType queryType, boolean caseSensitive, HtmlFormatter formatter) {
        this.caseSensitive = caseSensitive;
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        PHPParseResult result = (PHPParseResult) info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, caretOffset);
        result.getProgram();

        CompletionRequest request = new CompletionRequest();
        request.anchor = caretOffset - prefix.length();
        request.formatter = formatter;

        // KEYWORDS

        for (String keyword : PHP_KEYWORDS) {
            if (startsWith(keyword, prefix)) {
                proposals.add(new KeywordItem(keyword, request));
            }
        }

        // FUNCTIONS
        
        PHPIndex index = PHPIndex.get(info.getIndex(PHPLanguage.PHP_MIME_TYPE));
        
        for (IndexedFunction function : index.getFunctions(result, prefix, NameKind.PREFIX)){
            proposals.add(new FunctionItem(function, request));
        }
        
        // CONSTANTS
        
        for (IndexedConstant constant : index.getConstants(result, prefix, NameKind.PREFIX)){
            proposals.add(new ConstantItem(constant, request));
        }
        
        // LOCAL VARIABLES
        
        proposals.addAll(getLocalVariableProposals(result, prefix));

        return proposals;
    }
    
    private Collection<CompletionProposal> getLocalVariableProposals(PHPParseResult result, String prefix){
        Collection<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        
        for (Statement statement : result.getProgram().getStatements()){
            if (statement instanceof ExpressionStatement){
                Expression expr = ((ExpressionStatement)statement).getExpression();
                
                if (expr instanceof Assignment){
                    System.err.println("" + ((Assignment)expr).getLeftHandSide().getClass());
                }
            }
        }
        
        return proposals;
    }

    public String document(CompilationInfo info, ElementHandle element) {
        return null;
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument) info.getDocument();

           // TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);
            doc.readLock(); // Read-lock due to token hierarchy use

            try {
                int lineBegin = Utilities.getRowStart(doc, caretOffset);
                if (lineBegin != -1) {
                    int lineEnd = Utilities.getRowEnd(doc, caretOffset);
                    String line = doc.getText(lineBegin, lineEnd - lineBegin);
                    int lineOffset = caretOffset - lineBegin;
                    int start = lineOffset;
                    if (lineOffset > 0) {
                        for (int i = lineOffset - 1; i >= 0; i--) {
                            char c = line.charAt(i);
                            if (!Character.isJavaIdentifierPart(c)) {
                                break;
                            } else {
                                start = i;
                            }
                        }
                    }

                    // Find identifier end
                    String prefix;
                    if (upToOffset) {
                        prefix = line.substring(start, lineOffset);
                    } else {
                        if (lineOffset == line.length()) {
                            prefix = line.substring(start);
                        } else {
                            int n = line.length();
                            int end = lineOffset;
                            for (int j = lineOffset; j < n; j++) {
                                char d = line.charAt(j);
                                // Try to accept Foo::Bar as well
                                if (!Character.isJavaIdentifierPart(d)) {
                                    break;
                                } else {
                                    end = j + 1;
                                }
                            }
                            prefix = line.substring(start, end);
                        }
                    }

                    if (prefix.length() > 0) {
                        if (prefix.endsWith("::")) {
                            return "";
                        }

                        if (prefix.endsWith(":") && prefix.length() > 1) {
                            return null;
                        }

                        // Strip out LHS if it's a qualified method, e.g.  Benchmark::measure -> measure
                        int q = prefix.lastIndexOf("::");

                        if (q != -1) {
                            prefix = prefix.substring(q + 2);
                        }

                        // The identifier chars identified by JsLanguage are a bit too permissive;
                        // they include things like "=", "!" and even "&" such that double-clicks will
                        // pick up the whole "token" the user is after. But "=" is only allowed at the
                        // end of identifiers for example.
                        if (prefix.length() == 1) {
                            char c = prefix.charAt(0);
                            if (!(Character.isJavaIdentifierPart(c) || c == '@' || c == '$' || c == ':')) {
                                return null;
                            }
                        } else {
                            for (int i = prefix.length() - 2; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?

                                char c = prefix.charAt(i);
                                if (i == 0 && c == ':') {
                                    // : is okay at the begining of prefixes
                                } else if (!(Character.isJavaIdentifierPart(c) || c == '@' || c == '$')) {
                                    prefix = prefix.substring(i + 1);
                                    break;
                                }
                            }
                        }
                    }
                    return prefix;
                }
            } finally {
                doc.readUnlock();
            }
        // Else: normal identifier: just return null and let the machinery do the rest
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
    }

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return null;
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return null;
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        return null;
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private class KeywordItem extends PHPCompletionItem {
        private String description = null;
        private String keyword = null;

        KeywordItem(String keyword, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }
        
        @Override
        public String getRhsHtml() {
            if (description != null) {
                return description;
            } else {
                return null;
            }
        }
    }
    
    private class ConstantItem extends PHPCompletionItem {
        private String description = null;
        private IndexedConstant constant = null;

        ConstantItem(IndexedConstant constant, CompletionRequest request) {
            super(constant, request);
            this.constant = constant;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.GLOBAL;
        }
    }
    
    private class FunctionItem extends PHPCompletionItem {

        FunctionItem(IndexedFunction function, CompletionRequest request) {
            super(function, request);
        }
        
        public IndexedFunction getFunction(){
            return (IndexedFunction)getElement();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }
        
        @Override
        public String getInsertPrefix() {
            return getName() + "(" + getParamsStr() + ")";
        }
        
        @Override public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            
//            boolean emphasize = true; //!function.isInherited();
//            if (emphasize) {
//                formatter.emphasis(true);
//            }
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);
            
//            if (strike) {
//                formatter.deprecated(false);
//            }
//            
            formatter.appendHtml("("); // NOI18N
            formatter.parameters(true);
            formatter.appendText(getParamsStr());
            formatter.parameters(false);
            formatter.appendHtml(")"); // NOI18N

//            if (getFunction().getType() != null && 
//                    getFunction().getKind() != ElementKind.CONSTRUCTOR) {
//                formatter.appendHtml(" : ");
//                formatter.appendText(getFunction().getType());
//            }
            
            return formatter.getText();
        }
        
        private String getParamsStr(){
            StringBuilder builder = new StringBuilder();
            Collection<String> parameters = getFunction().getParameters();
            
            if ((parameters != null) && (parameters.size() > 0)) {
                Iterator<String> it = parameters.iterator();

                while (it.hasNext()) { // && tIt.hasNext()) {
                    String param = it.next();
                    builder.append("$" + param);

                    if (it.hasNext()) {
                        builder.append(", "); // NOI18N
                    }
                }
            }
            
            return builder.toString();
        }
    }

    private static class PHPCompletionItem implements CompletionProposal {

        protected final CompletionRequest request;
        private final ElementHandle element;

        PHPCompletionItem(ElementHandle element, CompletionRequest request) {
            this.request = request;
            this.element = element;
        }

        public int getAnchorOffset() {
            return request.anchor;
        }

        public ElementHandle getElement() {
            return element;
        }

        public String getName() {
            return element.getName();
        }

        public String getInsertPrefix() {
            return getName();
        }

        public String getSortText() {
            return getName();
        }

        public String getLhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            formatter.appendText(getName());
            return formatter.getText();
        }

        public ElementKind getKind() {
            return null;
        }

        public ImageIcon getIcon() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return null;
        }

        public boolean isSmart() {
            return false;
        }

        public String getCustomInsertTemplate() {
            return null;
        }

        public List<String> getInsertParams() {
            return null;
        }

        public String[] getParamListDelimiters() {
            return new String[] { "(", ")" }; // NOI18N
        }
        
        public String getRhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            
            if (element.getIn() != null) {
                formatter.appendText(element.getIn());
                return formatter.getText();
            } else if (element instanceof IndexedElement) {
                IndexedElement ie = (IndexedElement)element;
                String filename = ie.getFilenameUrl();
                if (filename != null) {
                    int index = filename.lastIndexOf('/');
                    if (index != -1) {
                        filename = filename.substring(index + 1);
                    }

                    formatter.appendText(filename);
                    return formatter.getText();
                }
            }
            
            return null;
        }
    }

    private static class CompletionRequest {
        private HtmlFormatter formatter;
        private int anchor;
    }
}
