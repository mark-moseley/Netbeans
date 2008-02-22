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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.mozilla.javascript.Node;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Completable;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.html.editor.gsf.HtmlParserResult;
import org.netbeans.modules.javascript.editing.JsParser.Sanitize;
import org.netbeans.modules.javascript.editing.lexer.Call;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Code completion handler for JavaScript
 * 
 * @todo Do completion on element id's inside $() calls (prototype.js) and $$() calls for CSS rules.
 *   See http://www.sitepoint.com/article/painless-javascript-prototype
 * @todo Track logical classes and inheritance ("extend")
 * @todo Track global variables (these are vars which aren't local). Somehow cooperate work between
 *    semantic highlighter and structure analyzer. I need to only store a single instance of each
 *    global var in the index. The variable visitor should probably be part of the structure analyzer,
 *    since global variables also need to be tracked there. Another possibility is having the
 *    parser track variables - but that's trickier. Perhaps a second pass over the parse tree
 *    (where I set parent pointers) is where I can do this? I can even change node types to be
 *    more obvious...
 * @todo I should NOT include in queries functions that are known to be methods if you're not doing
 *    "unnown type" completion!
 * @todo Today's feature work:
 *    - this.-completion should do something useful
 *    - I need to model prototype inheritance, and then use it in code completion queries
 *    - Skip no-doc'ed methods
 *    - Improve type analysis:
 *        - known types (element, document, ...)
 *        - variable-name guessing (el, doc, etc ...)
 *        - return value tracking
 *    - Improve indexing:
 *        - store @-private, etc.
 *        - more efficient browser-compat flags
 *    - Fix case-sensitivity on index queries such that open type and other forms of completion
 *      work better!
 *  @todo Distinguish properties and globals and functions? Perhaps with attributes in the flags!
 *  @todo Display more information in parameter tooltips, such as type hints (perhaps do smart
 *    filtering Java-style?), and explanations for each parameter
 *  @todo Need preindexing support for unit tests - and separate files
 * 
 * @author Tor Norbye
 */
public class JsCodeCompletion implements Completable {
    private static ImageIcon keywordIcon;
    private boolean caseSensitive;
    private static final String[] REGEXP_WORDS =
        new String[] {
            // Dbl-space lines to keep formatter from collapsing pairs into a block

            // Literals
            
            "\\0", "The NUL character (\\u0000)",

            "\\t", "Tab (\\u0009)",
            
            "\\n", "Newline (\\u000A)",
            
            "\\v", "Vertical tab (\\u000B)",
            
            "\\f", "Form feed (\\u000C)",
            
            "\\r", "Carriage return (\\u000D)",
            
            "\\x", "\\x<i>nn</i>: The latin character in hex <i>nn</i>",
            
            "\\u", "\\u<i>xxxx</i>: The Unicode character in hex <i>xxxx</i>",
            
            "\\c", "\\c<i>X</i>: The control character ^<i>X</i>",
            
            

            // Character classes
            "[]", "Any one character between the brackets",
            
            "[^]", "Any one character not between the brackets",
            
            
            
            "\\w", "Any ASCII word character; same as [0-9A-Za-z_]",
            
            "\\W", "Not a word character; same as [^0-9A-Za-z_]",
            
            "\\s", "Unicode space character",
            
            "\\S", "Non-space character",
            
            "\\d", "Digit character; same as [0-9]",
            
            "\\D", "Non-digit character; same as [^0-9]",
            
            "[\\b]", "Literal backspace",
            
            
            
            // Match positions
            "^", "Start of line",
            
            "$", "End of line",
            
            "\\b", "Word boundary (if not in a range specification)",
            
            "\\B", "Non-word boundary",
            
            // According to JavaScript The Definitive Guide, the following are not supported
            // in JavaScript:
            // \\a, \\e, \\l, \\u, \\L, \\U, \\E, \\Q, \\A, \\Z, \\z, and \\G
            // 
            //"\\A", "Beginning of string",
            //"\\z", "End of string",
            //"\\Z", "End of string (except \\n)",
            
            "*", "Zero or more repetitions of the preceding",
            
            "+", "One or more repetitions of the preceding",
            
            "{m,n}", "At least m and at most n repetitions of the preceding",
            
            "?", "At most one repetition of the preceding; same as {0,1}",
            
            "|", "Either preceding or next expression may match",
            
            "()", "Grouping",
            
            //"[:alnum:]", "Alphanumeric character class",
            //"[:alpha:]", "Uppercase or lowercase letter",
            //"[:blank:]", "Blank and tab",
            //"[:cntrl:]", "Control characters (at least 0x00-0x1f,0x7f)",
            //"[:digit:]", "Digit",
            //"[:graph:]", "Printable character excluding space",
            //"[:lower:]", "Lowecase letter",
            //"[:print:]", "Any printable letter (including space)",
            //"[:punct:]", "Printable character excluding space and alphanumeric",
            //"[:space:]", "Whitespace (same as \\s)",
            //"[:upper:]", "Uppercase letter",
            //"[:xdigit:]", "Hex digit (0-9, a-f, A-F)",
        };

    // Strings section 7.8
    private static final String[] STRING_ESCAPES =
        new String[] {
        
            "\\0", "The NUL character (\\u0000)",

            "\\b", "Backspace (0x08)",
            
            "\\t", "Tab (\\u0009)",
            
            "\\n", "Newline (\\u000A)",
            
            "\\v", "Vertical tab (\\u000B)",
            
            "\\f", "Form feed (\\u000C)",
            
            "\\r", "Carriage return (\\u000D)",
            
            "\\\"", "Double Quote (\\u0022)",
            
            "\\'", "Single Quote (\\u0027)",
            
            "\\\\", "Backslash (\\u005C)",
            
            "\\x", "\\x<i>nn</i>: The latin character in hex <i>nn</i>",
            
            "\\u", "\\u<i>xxxx</i>: The Unicode character in hex <i>xxxx</i>",
            
            "\\", "\\<i>ooo</i>: The latin character in octal <i>ooo</i>",

            // PENDING: Is this supported?
            "\\c", "\\c<i>X</i>: The control character ^<i>X</i>",
            
        };
    
    public JsCodeCompletion() {
        
    }

    public List<CompletionProposal> complete(CompilationInfo info, int lexOffset, String prefix,
            NameKind kind, QueryType queryType, boolean caseSensitive, HtmlFormatter formatter) {
        if (prefix == null) {
            prefix = "";
        }
        this.caseSensitive = caseSensitive;

        final Document document;
        try {
            document = info.getDocument();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }
        final BaseDocument doc = (BaseDocument)document;

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        JsParseResult parseResult = AstUtilities.getParseResult(info);
        doc.readLock(); // Read-lock due to Token hierarchy use
        try {
            Node root = parseResult.getRootNode();
            final int astOffset = AstUtilities.getAstOffset(info, lexOffset);
            if (astOffset == -1) {
                return null;
            }
            final TokenHierarchy<Document> th = TokenHierarchy.get(document);
            final FileObject fileObject = info.getFileObject();

            // Carry completion context around since this logic is split across lots of methods
            // and I don't want to pass dozens of parameters from method to method; just pass
            // a request context with supporting info needed by the various completion helpers i
            CompletionRequest request = new CompletionRequest();
            request.result = parseResult;
            request.formatter = formatter;
            request.lexOffset = lexOffset;
            request.astOffset = astOffset;
            request.index = JsIndex.get(info.getIndex(JsMimeResolver.JAVASCRIPT_MIME_TYPE));
            request.doc = doc;
            request.info = info;
            request.prefix = prefix;
            request.th = th;
            request.kind = kind;
            request.queryType = queryType;
            request.fileObject = fileObject;
            request.anchor = lexOffset - prefix.length();
            request.call = Call.getCallType(doc, th, lexOffset);
            request.fqn = null; // TODO - compute

            Token<? extends TokenId> token = LexUtilities.getToken(doc, lexOffset);
            if (token == null) {
                return proposals;
            }
            
            TokenId id = token.id();
            if (id == JsTokenId.LINE_COMMENT) {
                // TODO - Complete symbols in comments?
                return proposals;
            } else if (id == JsTokenId.STRING_LITERAL || id == JsTokenId.STRING_END) {
                completeStrings(proposals, request);
                return proposals;
            } else if (id == JsTokenId.REGEXP_LITERAL || id == JsTokenId.REGEXP_END) {
                completeRegexps(proposals, request);
                return proposals;
            }
            
            if (root != null) {
                final AstPath path = new AstPath(root, astOffset);
                request.path = path;

                final Node closest = path.leaf();
                request.root = root;
                request.node = closest;
                
                addLocals(proposals, request);
            }

            completeKeywords(proposals, request);

            if (root == null) {
                return proposals;
            }
            // Try to complete "new" RHS
            if (completeNew(proposals, request)) {
               return proposals;
            }

            if (completeObjectMethod(proposals, request)) {
                return proposals;
            }

            // Try to complete methods
            if (completeFunctions(proposals, request)) {
               return proposals;
            }
        } finally {
            doc.readUnlock();
        }
        
        return proposals;
    }

    private void addLocals(List<CompletionProposal> proposals, CompletionRequest request) {
        Node node = request.node;
        String prefix = request.prefix;
        NameKind kind = request.kind;
        JsParseResult result = request.result;
        
        // TODO - find the scope!!!
        VariableVisitor v = result.getVariableVisitor();

        Map<String,List<Node>> localVars = v.getLocalVars(node);
        for (String name : localVars.keySet()) {
            if (((kind == NameKind.EXACT_NAME) && prefix.equals(name)) ||
                    ((kind != NameKind.EXACT_NAME) && startsWith(name, prefix))) {
                List<Node> nodeList = localVars.get(name);
                if (nodeList != null && nodeList.size() > 0) {
                    AstElement element = AstElement.getElement(request.info, nodeList.get(0));
                    proposals.add(new PlainItem(element, request));
                }
            }
        }
    }
    
    private void completeKeywords(List<CompletionProposal> proposals, CompletionRequest request) {
        // No keywords possible in the RHS of a call (except for "this"?)
        if (request.call.getLhs() != null) {
            return;
        }
        
        String prefix = request.prefix;
        
//        // Keywords
//        if (prefix.equals("$")) {
//            // Show dollar variable matches (global vars from the user's
//            // code will also be shown
//            for (int i = 0, n = Js_DOLLAR_VARIABLES.length; i < n; i += 2) {
//                String word = Js_DOLLAR_VARIABLES[i];
//                String desc = Js_DOLLAR_VARIABLES[i + 1];
//
//                KeywordItem item = new KeywordItem(word, desc, anchor, request);
//
//                if (isSymbol) {
//                    item.setSymbol(true);
//                }
//
//                proposals.add(item);
//            }
//        }
//
//        for (String keyword : Js_BUILTIN_VARS) {
//            if (startsWith(keyword, prefix)) {
//                KeywordItem item = new KeywordItem(keyword, null, anchor, request);
//
//                if (isSymbol) {
//                    item.setSymbol(true);
//                }
//
//                proposals.add(item);
//            }
//        }

        for (String keyword : JsUtils.JAVASCRIPT_KEYWORDS) {
            if (startsWith(keyword, prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, request);

                proposals.add(item);
            }
        }

        for (String keyword : JsUtils.JAVASCRIPT_RESERVED_WORDS) {
            if (startsWith(keyword, prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, request);

                proposals.add(item);
            }
        }
    }
    
    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                             : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    private boolean completeRegexps(List<CompletionProposal> proposals, CompletionRequest request) {
        String prefix = request.prefix;

        // Regular expression matching.  {
        for (int i = 0, n = REGEXP_WORDS.length; i < n; i += 2) {
            String word = REGEXP_WORDS[i];
            String desc = REGEXP_WORDS[i + 1];

            if (startsWith(word, prefix)) {
                KeywordItem item = new KeywordItem(word, desc, request);
                proposals.add(item);
            }
        }

        return true;
    }
    
    private boolean completeStrings(List<CompletionProposal> proposals, CompletionRequest request) {
        String prefix = request.prefix;

        // See if we're in prototype js functions, $() and $F(), and if so,
        // offer to complete the function ids
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(request.doc, request.lexOffset);
        assert ts != null; // or we wouldn't have been called in the first place
        Token<? extends JsTokenId> stringToken = ts.token();
        int stringOffset = ts.offset();

    tokenLoop:
        while (ts.movePrevious()) {
            Token<? extends JsTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == JsTokenId.IDENTIFIER) {
                String text = token.text().toString();
                if ("$".equals(text) || "$F".equals(text)) { // NOI18N
                    String HTML_MIME_TYPE = "text/html"; // NOI18N
                    ParserResult result = request.info.getEmbeddedResult(HTML_MIME_TYPE, 0);
                    if (result != null) {
                        HtmlParserResult htmlResult = (HtmlParserResult)result;
                        Set<SyntaxElement.TagAttribute> elementIds = htmlResult.elementsIds();
                        
                        if (elementIds.size() > 0) {
                            // Compute a custom prefix
                            int lexOffset = request.lexOffset;
                            if (lexOffset > stringOffset) {
                                try {
                                    prefix = request.doc.getText(stringOffset, lexOffset - stringOffset);
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            } else {
                                prefix = "";
                            }
                            
                            String filename = request.fileObject.getNameExt();

                            for (SyntaxElement.TagAttribute tag : elementIds) {
                                String elementId = tag.getValue();
                                // Strip "'s surrounding value, if any
                                if (elementId.length() > 2 && elementId.startsWith("\"") && // NOI18N
                                        elementId.endsWith("\"")) { // NOI18N
                                    elementId = elementId.substring(1, elementId.length()-1);
                                }

                                if (startsWith(elementId, prefix)) {
                                    TagItem item = new TagItem(elementId, filename, request);
                                    proposals.add(item);
                                }
                            }
                        }
                    }
                }

                return true;
            } else if (id == JsTokenId.STRING_BEGIN) {
                stringOffset = ts.offset() + token.length();
            } else if (!(id == JsTokenId.WHITESPACE ||
                    id == JsTokenId.STRING_LITERAL || id == JsTokenId.LPAREN)) {
                break tokenLoop;
            }
        }
        
        for (int i = 0, n = STRING_ESCAPES.length; i < n; i += 2) {
            String word = STRING_ESCAPES[i];
            String desc = STRING_ESCAPES[i + 1];

            if (startsWith(word, prefix)) {
                KeywordItem item = new KeywordItem(word, desc, request);
                proposals.add(item);
            }
        }

        return true;
    }


    /**
     * Compute an appropriate prefix to use for code completion.
     * In Strings, we want to return the -whole- string if you're in a
     * require-statement string, otherwise we want to return simply "" or the previous "\"
     * for quoted strings, and ditto for regular expressions.
     * For non-string contexts, just return null to let the default identifier-computation
     * kick in.
     */
    @SuppressWarnings("unchecked")
    public String getPrefix(CompilationInfo info, int lexOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument)info.getDocument();

            TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
            doc.readLock(); // Read-lock due to token hierarchy use
            try {
//            int requireStart = LexUtilities.getRequireStringOffset(lexOffset, th);
//
//            if (requireStart != -1) {
//                // XXX todo - do upToOffset
//                return doc.getText(requireStart, lexOffset - requireStart);
//            }

            TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, lexOffset);

            if (ts == null) {
                return null;
            }

            ts.move(lexOffset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return null;
            }

            if (ts.offset() == lexOffset) {
                // We're looking at the offset to the RIGHT of the caret
                // and here I care about what's on the left
                ts.movePrevious();
            }

            Token<?extends JsTokenId> token = ts.token();

            if (token != null) {
                TokenId id = token.id();

                
                if (id == JsTokenId.STRING_BEGIN || id == JsTokenId.STRING_END ||
                        id == JsTokenId.STRING_LITERAL || id == JsTokenId.REGEXP_LITERAL ||
                        id == JsTokenId.REGEXP_BEGIN || id == JsTokenId.REGEXP_END) {
                    if (lexOffset > 0) {
                        char prevChar = doc.getText(lexOffset-1, 1).charAt(0);
                        if (prevChar == '\\') {
                            return "\\";
                        }
                        return "";
                    }
                }
//                        
//                // We're within a String that has embedded Js. Drop into the
//                // embedded language and see if we're within a literal string there.
//                if (id == JsTokenId.EMBEDDED_RUBY) {
//                    ts = (TokenSequence)ts.embedded();
//                    assert ts != null;
//                    ts.move(lexOffset);
//
//                    if (!ts.moveNext() && !ts.movePrevious()) {
//                        return null;
//                    }
//
//                    token = ts.token();
//                    id = token.id();
//                }
//
//                String tokenText = token.text().toString();
//
//                if ((id == JsTokenId.STRING_BEGIN) || (id == JsTokenId.QUOTED_STRING_BEGIN) ||
//                        ((id == JsTokenId.ERROR) && tokenText.equals("%"))) {
//                    int currOffset = ts.offset();
//
//                    // Percent completion
//                    if ((currOffset == (lexOffset - 1)) && (tokenText.length() > 0) &&
//                            (tokenText.charAt(0) == '%')) {
//                        return "%";
//                    }
//                }
//            }
//
//            int doubleQuotedOffset = LexUtilities.getDoubleQuotedStringOffset(lexOffset, th);
//
//            if (doubleQuotedOffset != -1) {
//                // Tokenize the string and offer the current token portion as the text
//                if (doubleQuotedOffset == lexOffset) {
//                    return "";
//                } else if (doubleQuotedOffset < lexOffset) {
//                    String text = doc.getText(doubleQuotedOffset, lexOffset - doubleQuotedOffset);
//                    TokenHierarchy hi =
//                        TokenHierarchy.create(text, JsStringTokenId.languageDouble());
//
//                    TokenSequence seq = hi.tokenSequence();
//
//                    seq.move(lexOffset - doubleQuotedOffset);
//
//                    if (!seq.moveNext() && !seq.movePrevious()) {
//                        return "";
//                    }
//
//                    TokenId id = seq.token().id();
//                    String s = seq.token().text().toString();
//
//                    if ((id == JsStringTokenId.STRING_ESCAPE) ||
//                            (id == JsStringTokenId.STRING_INVALID)) {
//                        return s;
//                    } else if (s.startsWith("\\")) {
//                        return s;
//                    } else {
//                        return "";
//                    }
//                } else {
//                    // The String offset is greater than the caret position.
//                    // This means that we're inside the string-begin section,
//                    // for example here: %q|(
//                    // In this case, report no prefix
//                    return "";
//                }
//            }
//
//            int singleQuotedOffset = LexUtilities.getSingleQuotedStringOffset(lexOffset, th);
//
//            if (singleQuotedOffset != -1) {
//                if (singleQuotedOffset == lexOffset) {
//                    return "";
//                } else if (singleQuotedOffset < lexOffset) {
//                    String text = doc.getText(singleQuotedOffset, lexOffset - singleQuotedOffset);
//                    TokenHierarchy hi =
//                        TokenHierarchy.create(text, JsStringTokenId.languageSingle());
//
//                    TokenSequence seq = hi.tokenSequence();
//
//                    seq.move(lexOffset - singleQuotedOffset);
//
//                    if (!seq.moveNext() && !seq.movePrevious()) {
//                        return "";
//                    }
//
//                    TokenId id = seq.token().id();
//                    String s = seq.token().text().toString();
//
//                    if ((id == JsStringTokenId.STRING_ESCAPE) ||
//                            (id == JsStringTokenId.STRING_INVALID)) {
//                        return s;
//                    } else if (s.startsWith("\\")) {
//                        return s;
//                    } else {
//                        return "";
//                    }
//                } else {
//                    // The String offset is greater than the caret position.
//                    // This means that we're inside the string-begin section,
//                    // for example here: %q|(
//                    // In this case, report no prefix
//                    return "";
//                }
//            }
//
//            // Regular expression
//            int regexpOffset = LexUtilities.getRegexpOffset(lexOffset, th);
//
//            if ((regexpOffset != -1) && (regexpOffset <= lexOffset)) {
//                // This is not right... I need to actually parse the regexp
//                // (I should use my Regexp lexer tokens which will be embedded here)
//                // such that escaping sequences (/\\\\\/) will work right, or
//                // character classes (/[foo\]). In both cases the \ may not mean escape.
//                String tokenText = token.text().toString();
//                int index = lexOffset - ts.offset();
//
//                if ((index > 0) && (index <= tokenText.length()) &&
//                        (tokenText.charAt(index - 1) == '\\')) {
//                    return "\\";
//                } else {
//                    // No prefix for regexps unless it's \
//                    return "";
//                }
//
//                //return doc.getText(regexpOffset, offset-regexpOffset);
//            }
            }

            int lineBegin = Utilities.getRowStart(doc, lexOffset);
            if (lineBegin != -1) {
                int lineEnd = Utilities.getRowEnd(doc, lexOffset);
                String line = doc.getText(lineBegin, lineEnd-lineBegin);
                int lineOffset = lexOffset-lineBegin;
                int start = lineOffset;
                if (lineOffset > 0) {
                    for (int i = lineOffset-1; i >= 0; i--) {
                        char c = line.charAt(i);
                        if (!JsUtils.isIdentifierChar(c)) {
                            break;
                        } else {
                            start = i;
                        }
                    }
                }
                
                // Find identifier end
                String prefix;
                if (upToOffset ){
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
                            if (!JsUtils.isStrictIdentifierChar(d)) {
                                break;
                            } else {
                                end = j+1;
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
                        for (int i = prefix.length()-2; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?
                            char c = prefix.charAt(i);
                            if (i ==0 && c == ':') {
                                // : is okay at the begining of prefixes
                            } else if (!(Character.isJavaIdentifierPart(c) || c == '@' || c == '$')) {
                                prefix = prefix.substring(i+1);
                                break;
                            }
                        }
                    }

                    return prefix;
                }
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

        // Default behavior
        return null;
    }
    
    /** Determine if we're trying to complete the name for a "def" (in which case
     * we'd show the inherited methods).
     * This needs to be enhanced to handle "Foo." prefixes, e.g. def self.foo
     */
    private boolean completeFunctions(List<CompletionProposal> proposals, CompletionRequest request) {
        JsIndex index = request.index;
        String prefix = request.prefix;
        TokenHierarchy<Document> th = request.th;
        NameKind kind = request.kind;
        JsParseResult result = request.result;
        
        Set<IndexedFunction> functions = index.getFunctions(prefix, null, kind, JsIndex.ALL_SCOPE, result, false);

        for (IndexedFunction method : functions) {
            FunctionItem item = new FunctionItem(method, request);
            proposals.add(item);

        }

        return true;
    }

    /** Determine if we're trying to complete the name of a method on another object rather
     * than an inherited or local one. These should list ALL known methods, unless of course
     * we know the type of the method we're operating on (such as strings or regexps),
     * or types inferred through data flow analysis
     *
     * @todo Look for self or this or super; these should be limited to inherited.
     */
    private boolean completeObjectMethod(List<CompletionProposal> proposals, CompletionRequest request) {
        
        JsIndex index = request.index;
        String prefix = request.prefix;
        int astOffset = request.astOffset;
        int lexOffset = request.lexOffset;
        TokenHierarchy<Document> th = request.th;
        BaseDocument doc = request.doc;
        AstPath path = request.path;
        NameKind kind = request.kind;
        FileObject fileObject = request.fileObject;
        Node node = request.node;
        JsParseResult result = request.result;
        CompilationInfo info = request.info;
        
        String fqn = request.fqn;
        Call call = request.call;

        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, lexOffset);

        // Look in the token stream for constructs of the type
        //   foo.x^
        // or
        //   foo.^
        // and if found, add all methods
        // (no keywords etc. are possible matches)
        if ((index != null) && (ts != null)) {
            boolean skipPrivate = true;

            if ((call == Call.LOCAL) || (call == Call.NONE)) {
                return false;
            }

            // If we're not sure we're only looking for a method, don't abort after this
            boolean done = call.isMethodExpected();

            boolean skipInstanceMethods = call.isStatic();

            Set<IndexedFunction> methods = Collections.emptySet();

            String type = call.getType();
            String lhs = call.getLhs();

            if ((type == null) && (lhs != null) && (node != null) && call.isSimpleIdentifier()) {
                Node method = AstUtilities.findLocalScope(node, path);

                if (method != null) {
                    // TODO - if the lhs is "foo.bar." I need to split this
                    // up and do it a bit more cleverly
                    JsTypeAnalyzer analyzer = new JsTypeAnalyzer(info, /*request.info.getParserResult(),*/ index, method, node, astOffset, lexOffset, doc, fileObject);
                    type = analyzer.getType(lhs);
                }
            }

            // I'm not doing any data flow analysis at this point, so
            // I can't do anything with a LHS like "foo.". Only actual types.
            if ((type != null) && (type.length() > 0)) {
//                if ("self".equals(lhs)) {
//                    type = fqn;
//                    skipPrivate = false;
//                } else if ("super".equals(lhs)) {
//                    skipPrivate = false;
//
//                    IndexedClass sc = index.getSuperclass(fqn);
//
//                    if (sc != null) {
//                        type = sc.getFqn();
//                    } else {
//                        ClassNode cls = AstUtilities.findClass(path);
//
//                        if (cls != null) {
//                            type = AstUtilities.getSuperclass(cls);
//                        }
//                    }
//
//                    if (type == null) {
//                        type = "Object"; // NOI18N
//                    }
//                }

                if ((type != null) && (type.length() > 0)) {
                    // Possibly a class on the left hand side: try searching with the class as a qualifier.
                    // Try with the LHS + current FQN recursively. E.g. if we're in
                    // Test::Unit when there's a call to Foo.x, we'll try
                    // Test::Unit::Foo, and Test::Foo
// TODO JS - I don't have fqn                    
//                    while (methods.size() == 0) {
//                        methods = index.getInheritedMethods(fqn + "::" + type, prefix, kind);
//
//                        int f = fqn.lastIndexOf("::");
//
//                        if (f == -1) {
//                            break;
//                        } else {
//                            fqn = fqn.substring(0, f);
//                        }
//                    }
methods = new HashSet<IndexedFunction>();

                    // Add methods in the class (without an FQN)
                    Set<IndexedFunction> m = index.getFunctions(prefix, type, kind, JsIndex.ALL_SCOPE, result, true);

                    if (m.size() > 0) {
                        methods.addAll(m);
                    }
                }
            }

            // Try just the method call (e.g. across all classes). This is ignoring the 
            // left hand side because we can't resolve it.
            if ((methods.size() == 0) && (prefix.length() > 0 || type == null)) {
                methods = index.getFunctions(prefix, null, kind, JsIndex.ALL_SCOPE, result, true);
            }

            for (IndexedFunction method : methods) {
                // Skip constructors - you don't want to call
                //   x.Foo !
                if (method.getKind() == ElementKind.CONSTRUCTOR) {
                    continue;
                }
                
//                // Don't include private or protected methods on other objects
//                if (skipPrivate && (method.isPrivate() && !"new".equals(method.getName()))) {
//                    // TODO - "initialize" removal here should not be necessary since they should
//                    // be marked as private, but index doesn't contain that yet
//                    continue;
//                }
//
//                // We can only call static methods
//                if (skipInstanceMethods && !method.isStatic()) {
//                    continue;
//                }
//
//                if (method.isNoDoc()) {
//                    continue;
//                }
//
//                if (method.getMethodType() == IndexedFunction.MethodType.DBCOLUMN) {
//                    DbItem item = new DbItem(method.getName(), method.getIn(), anchor, request);
//                    proposals.add(item);
//                    continue;
//                }

                FunctionItem funcItem = new FunctionItem(method, request);
                // Exact matches
//                funcItem.setSmart(method.isSmart());
                proposals.add(funcItem);
            }

            return done;
        }

        return false;
    }
    
    
    /** Determine if we're trying to complete the name for a "new" (in which case
     * we show available constructors.
     */
    private boolean completeNew(List<CompletionProposal> proposals, CompletionRequest request) {
        JsIndex index = request.index;
        String prefix = request.prefix;
        int lexOffset = request.lexOffset;
        TokenHierarchy<Document> th = request.th;
        NameKind kind = request.kind;
        
        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, lexOffset);

        if ((index != null) && (ts != null)) {
            ts.move(lexOffset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return false;
            }

            if (ts.offset() == lexOffset) {
                // We're looking at the offset to the RIGHT of the caret
                // position, which could be whitespace, e.g.
                //  "def fo| " <-- looking at the whitespace
                ts.movePrevious();
            }

            Token<?extends JsTokenId> token = ts.token();

            if (token != null) {
                TokenId id = token.id();

                // See if we're in the identifier - "foo" in "def foo"
                // I could also be a keyword in case the prefix happens to currently
                // match a keyword, such as "next"
                if ((id == JsTokenId.IDENTIFIER) || (id == JsTokenId.CONSTANT) || id.primaryCategory().equals("keyword")) {
                    if (!ts.movePrevious()) {
                        return false;
                    }

                    token = ts.token();
                    id = token.id();
                }

                // If we're not in the identifier we need to be in the whitespace after "def"
                if (id != JsTokenId.WHITESPACE) {
                    // Do something about http://www.netbeans.org/issues/show_bug.cgi?id=100452 here
                    // In addition to checking for whitespace I should look for "Foo." here
                    return false;
                }

                // There may be more than one whitespace; skip them
                while (ts.movePrevious()) {
                    token = ts.token();

                    if (token.id() != JsTokenId.WHITESPACE) {
                        break;
                    }
                }

                if (token.id() == JsTokenId.NEW) {
                    Set<IndexedFunction> methods = index.getConstructors(prefix, kind, JsIndex.ALL_SCOPE);

                    for (IndexedFunction method : methods) {
                        // Hmmm, is this necessary? Filtering should happen in the getInheritedMEthods call
                        if ((prefix.length() > 0) && !method.getName().startsWith(prefix)) {
                            continue;
                        }

//                        // For def completion, skip local methods, only include superclass and included
//                        if ((fqn != null) && fqn.equals(method.getClz())) {
//                            continue;
//                        }
//                        
//                        if (method.isNoDoc()) {
//                            continue;
//                        }

                        // If a method is an "initialize" method I should do something special so that
                        // it shows up as a "constructor" (in a new() statement) but not as a directly
                        // callable initialize method (it should already be culled because it's private)
                        FunctionItem item = new FunctionItem(method, request);
                        // Exact matches
//                        item.setSmart(method.isSmart());
                        proposals.add(item);
                    }

                    return true;
//                } else if (token.id() == JsTokenId.IDENTIFIER && "include".equals(token.text().toString())) {
//                    // Module completion
//                    Set<IndexedClass> classes = index.getClasses(prefix, kind, false, true, false);
//                    for (IndexedClass clz : classes) {
//                        if (clz.isNoDoc()) {
//                            continue;
//                        }
//                        
//                        ClassItem item = new ClassItem(clz, anchor, request);
//                        item.setSmart(true);
//                        proposals.add(item);
//                    }     
//                    
//                    return true;
                }
            }
        }

        return false;
    }

    
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);
        
        // TODO - auto query on ' and " when you're in $() or $F()
        
        if (c == '\n' || c == '(' || c == '[' || c == '{') {
            return QueryType.STOP;
        }
        
        if (c != '.'/* && c != ':'*/) {
            return QueryType.NONE;
        }

        int offset = component.getCaretPosition();
        BaseDocument doc = (BaseDocument)component.getDocument();

        if (".".equals(typedText)) { // NOI18N
            // See if we're in Js context
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);
            if (ts == null) {
                return QueryType.NONE;
            }
            ts.move(offset);
            if (!ts.moveNext()) {
                if (!ts.movePrevious()) {
                    return QueryType.NONE;
                }
            }
            if (ts.offset() == offset && !ts.movePrevious()) {
                return QueryType.NONE;
            }
            Token<? extends JsTokenId> token = ts.token();
            TokenId id = token.id();
            
//            // ".." is a range, not dot completion
//            if (id == JsTokenId.RANGE) {
//                return QueryType.NONE;
//            }

            // TODO - handle embedded JavaScript
            if ("comment".equals(id.primaryCategory()) || // NOI18N
                    "string".equals(id.primaryCategory()) ||  // NOI18N
                    "regexp".equals(id.primaryCategory())) { // NOI18N
                return QueryType.NONE;
            }
            
            return QueryType.COMPLETION;
        }
        
//        if (":".equals(typedText)) { // NOI18N
//            // See if it was "::" and we're in ruby context
//            int dot = component.getSelectionStart();
//            try {
//                if ((dot > 1 && component.getText(dot-2, 1).charAt(0) == ':') && // NOI18N
//                        isJsContext(doc, dot-1)) {
//                    return QueryType.COMPLETION;
//                }
//            } catch (BadLocationException ble) {
//                Exceptions.printStackTrace(ble);
//            }
//        }
//        
        return QueryType.NONE;
    }
    
    public static boolean isJsContext(BaseDocument doc, int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }
        
        ts.move(offset);
        
        if (!ts.movePrevious() && !ts.moveNext()) {
            return true;
        }
        
        TokenId id = ts.token().id();
        if ("comment".equals(id.primaryCategory()) || "string".equals(id.primaryCategory()) || // NOI18N
                "regexp".equals(id.primaryCategory())) { // NOI18N
            return false;
        }
        
        return true;
    }

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset,
            String name, Map parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String document(CompilationInfo info, ElementHandle handle) {
        Element element = ElementUtilities.getElement(info, handle);
        if (element == null) {
            return null;
        }
        if (element instanceof KeywordElement) {
            return null; //getKeywordHelp(((KeywordElement)element).getName());
        } else if (element instanceof CommentElement) {
            // Text is packaged as the name
            String comment = element.getName();
            String[] comments = comment.split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 0, n = comments.length; i < n; i++) {
                String line = comments[i];
                if (line.startsWith("/**")) {
                    sb.append(line.substring(3));
                } else if (i == n-1 && line.trim().endsWith("*/")) {
                    sb.append(line.substring(0,line.length()-2));
                    continue;
                } else if (line.startsWith("//")) {
                    sb.append(line.substring(2));
                } else if (line.startsWith("/*")) {
                    sb.append(line.substring(2));
                } else if (line.startsWith("*")) {
                    sb.append(line.substring(1));
                } else {
                    sb.append(line);
                }
            }
            String html = sb.toString();
            return html;
        }

        List<String> comments = ElementUtilities.getComments(info, element);
        if (comments == null) {
            String html = ElementUtilities.getSignature(element) + "\n<hr>\n<i>" + NbBundle.getMessage(JsCodeCompletion.class, "NoCommentFound") +"</i>";

            return html;
        }

        JsCommentFormatter formatter = new JsCommentFormatter(comments);
        String name = element.getName();
        if (name != null && name.length() > 0) {
            formatter.setSeqName(name);
        }

        String html = formatter.toHtml();
        html = ElementUtilities.getSignature(element) + "\n<hr>\n" + html;
        return html;
    }
    
    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin,
            int selectionEnd) {
        return Collections.emptySet();
    }

    public ParameterInfo parameters(CompilationInfo info, int lexOffset,
            CompletionProposal proposal) {
        IndexedFunction[] methodHolder = new IndexedFunction[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int astOffset = AstUtilities.getAstOffset(info, lexOffset);
        if (!computeMethodCall(info, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, null)) {

            return ParameterInfo.NONE;
        }

        IndexedFunction method = methodHolder[0];
        if (method == null) {
            return ParameterInfo.NONE;
        }
        int index = paramIndexHolder[0];
        int anchorOffset = anchorOffsetHolder[0];


        // TODO: Make sure the caret offset is inside the arguments portion
        // (parameter hints shouldn't work on the method call name itself
        // See if we can find the method corresponding to this call
        //        if (proposal != null) {
        //            Element element = proposal.getElement();
        //            if (element instanceof IndexedFunction) {
        //                method = ((IndexedFunction)element);
        //            }
        //        }

        List<String> params = method.getParameters();

        if ((params != null) && (params.size() > 0)) {
            return new ParameterInfo(params, index, anchorOffset);
        }

        return ParameterInfo.NONE;
    }

    private static int callLineStart = -1;
    private static IndexedFunction callMethod;

    /** Compute the current method call at the given offset. Returns false if we're not in a method call. 
     * The argument index is returned in parameterIndexHolder[0] and the method being
     * called in methodHolder[0].
     */
    static boolean computeMethodCall(CompilationInfo info, int lexOffset, int astOffset,
            IndexedFunction[] methodHolder, int[] parameterIndexHolder, int[] anchorOffsetHolder,
            Set<IndexedFunction>[] alternativesHolder) {
        try {
            Node root = AstUtilities.getRoot(info);

            if (root == null) {
                return false;
            }

            IndexedFunction targetMethod = null;
            int index = -1;

            AstPath path = null;
            // Account for input sanitation
            // TODO - also back up over whitespace, and if I hit the method
            // I'm parameter number 0
            int originalAstOffset = astOffset;

            // Adjust offset to the left
            BaseDocument doc = (BaseDocument) info.getDocument();
            int newLexOffset = LexUtilities.findSpaceBegin(doc, lexOffset);
            if (newLexOffset < lexOffset) {
                astOffset -= (lexOffset-newLexOffset);
            }

            JsParseResult rpr = AstUtilities.getParseResult(info);
            OffsetRange range = rpr.getSanitizedRange();
            if (range != OffsetRange.NONE && range.containsInclusive(astOffset)) {
                if (astOffset != range.getStart()) {
                    astOffset = range.getStart()-1;
                    if (astOffset < 0) {
                        astOffset = 0;
                    }
                    path = new AstPath(root, astOffset);
                }
            }

            if (path == null) {
                path = new AstPath(root, astOffset);
            }

            int currentLineStart = Utilities.getRowStart(doc, lexOffset);
            if (callLineStart != -1 && currentLineStart == callLineStart) {
                // We know the method call
                targetMethod = callMethod;
                if (targetMethod != null) {
                    // Somehow figure out the argument index
                    // Perhaps I can keep the node tree around and look in it
                    // (This is all trying to deal with temporarily broken
                    // or ambiguous calls.
                }
            }
            // Compute the argument index

            Node call = null;
            int anchorOffset = -1;

            if (targetMethod != null) {
                Iterator<Node> it = path.leafToRoot();
                String name = targetMethod.getName();
                while (it.hasNext()) {
                    Node node = it.next();
//                    if (AstUtilities.isCall(node) &&
//                            name.equals(AstUtilities.getCallName(node))) {
//                        if (node.nodeId == NodeTypes.CALLNODE) {
//                            Node argsNode = ((CallNode)node).getArgsNode();
//
//                            if (argsNode != null) {
//                                index = AstUtilities.findArgumentIndex(argsNode, astOffset);
//
//                                if (index == -1 && astOffset < originalAstOffset) {
//                                    index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
//                                }
//
//                                if (index != -1) {
//                                    call = node;
//                                    anchorOffset = argsNode.getPosition().getStartOffset();
//                                }
//                            }
//                        } else if (node.nodeId == NodeTypes.FCALLNODE) {
//                            Node argsNode = ((FCallNode)node).getArgsNode();
//
//                            if (argsNode != null) {
//                                index = AstUtilities.findArgumentIndex(argsNode, astOffset);
//
//                                if (index == -1 && astOffset < originalAstOffset) {
//                                    index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
//                                }
//
//                                if (index != -1) {
//                                    call = node;
//                                    anchorOffset = argsNode.getPosition().getStartOffset();
//                                }
//                            }
//                        } else if (node.nodeId == NodeTypes.VCALLNODE) {
//                            // We might be completing at the end of a method call
//                            // and we don't have parameters yet so it just looks like
//                            // a vcall, e.g.
//                            //   create_table |
//                            // This is okay as long as the caret is outside and to
//                            // the right of this call. However
//                            final OffsetRange callRange = AstUtilities.getCallRange(node);
//                            AstUtilities.getCallName(node);
//                            if (originalAstOffset > callRange.getEnd()) {
//                                index = 0;
//                                call = node;
//                                anchorOffset = callRange.getEnd()+1;
//                            }
//                        }
//                        
//                        break;
//                    }
                }
            }

            boolean haveSanitizedComma = rpr.getSanitized() == Sanitize.EDITED_DOT ||
                    rpr.getSanitized() == Sanitize.ERROR_DOT;
            if (haveSanitizedComma) {
                // We only care about removed commas since that
                // affects the parameter count
                if (rpr.getSanitizedContents().indexOf(',') == -1) {
                    haveSanitizedComma = false;
                }
            }

            if (call == null) {
                // Find the call in around the caret. Beware of 
                // input sanitization which could have completely
                // removed the current parameter (e.g. with just
                // a comma, or something like ", @" or ", :")
                // where we accidentally end up in the previous
                // parameter.
                ListIterator<Node> it = path.leafToRoot();
             nodesearch:
                while (it.hasNext()) {
                    Node node = it.next();

                    if (node.getType() == org.mozilla.javascript.Token.CALL) {
                        call = node;
                        index = AstUtilities.findArgumentIndex(call, astOffset, path);
                        break;
                    }
//                    if (node.nodeId == NodeTypes.CALLNODE) {
//                        final OffsetRange callRange = AstUtilities.getCallRange(node);
//                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
//                            for (int i = 0; i < 3; i++) {
//                                // It's not really a peek in the sense
//                                // that there's no reason to retry these
//                                // nodes later
//                                Node peek = it.next();
//                                if (AstUtilities.isCall(peek) &&
//                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
//                                        Utilities.getRowStart(doc, lexOffset)) {
//                                    // Use the outer method call instead
//                                    it.previous();
//                                    continue nodesearch;
//                                }
//                            }
//                        }
//                        
//                        Node argsNode = ((CallNode)node).getArgsNode();
//
//                        if (argsNode != null) {
//                            index = AstUtilities.findArgumentIndex(argsNode, astOffset);
//
//                            if (index == -1 && astOffset < originalAstOffset) {
//                                index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
//                            }
//
//                            if (index != -1) {
//                                call = node;
//                                anchorOffset = argsNode.getPosition().getStartOffset();
//
//                                break;
//                            }
//                        } else {
//                            if (originalAstOffset > callRange.getEnd()) {
//                                index = 0;
//                                call = node;
//                                anchorOffset = callRange.getEnd()+1;
//                                break;
//                            }
//                        }
//                    } else if (node.nodeId == NodeTypes.FCALLNODE) {
//                        final OffsetRange callRange = AstUtilities.getCallRange(node);
//                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
//                            for (int i = 0; i < 3; i++) {
//                                // It's not really a peek in the sense
//                                // that there's no reason to retry these
//                                // nodes later
//                                Node peek = it.next();
//                                if (AstUtilities.isCall(peek) &&
//                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
//                                        Utilities.getRowStart(doc, lexOffset)) {
//                                    // Use the outer method call instead
//                                    it.previous();
//                                    continue nodesearch;
//                                }
//                            }
//                        }
//                        
//                        Node argsNode = ((FCallNode)node).getArgsNode();
//
//                        if (argsNode != null) {
//                            index = AstUtilities.findArgumentIndex(argsNode, astOffset);
//
//                            if (index == -1 && astOffset < originalAstOffset) {
//                                index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
//                            }
//
//                            if (index != -1) {
//                                call = node;
//                                anchorOffset = argsNode.getPosition().getStartOffset();
//
//                                break;
//                            }
//                        }
//                    } else if (node.nodeId == NodeTypes.VCALLNODE) {
//                        // We might be completing at the end of a method call
//                        // and we don't have parameters yet so it just looks like
//                        // a vcall, e.g.
//                        //   create_table |
//                        // This is okay as long as the caret is outside and to
//                        // the right of this call.
//                        
//                        final OffsetRange callRange = AstUtilities.getCallRange(node);
//                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
//                            for (int i = 0; i < 3; i++) {
//                                // It's not really a peek in the sense
//                                // that there's no reason to retry these
//                                // nodes later
//                                Node peek = it.next();
//                                if (AstUtilities.isCall(peek) &&
//                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
//                                        Utilities.getRowStart(doc, lexOffset)) {
//                                    // Use the outer method call instead
//                                    it.previous();
//                                    continue nodesearch;
//                                }
//                            }
//                        }
//                        
//                        if (originalAstOffset > callRange.getEnd()) {
//                            index = 0;
//                            call = node;
//                            anchorOffset = callRange.getEnd()+1;
//                            break;
//                        }
//                    }
                }
            }

            if (index != -1 && haveSanitizedComma && call != null) {
                Node an = null;
//                if (call.nodeId == NodeTypes.FCALLNODE) {
//                    an = ((FCallNode)call).getArgsNode();
//                } else if (call.nodeId == NodeTypes.CALLNODE) {
//                    an = ((CallNode)call).getArgsNode();
//                }
//                if (an != null && index < an.childNodes().size() &&
//                        ((Node)an.childNodes().get(index)).nodeId == NodeTypes.HASHNODE) {
//                    // We should stay within the hashnode, so counteract the
//                    // index++ which follows this if-block
//                    index--;
//                }

                // Adjust the index to account for our removed
                // comma
                index++;
            }
            
            if ((call == null) || (index == -1)) {
                callLineStart = -1;
                callMethod = null;
                return false;
            } else if (targetMethod == null) {
                // Look up the
                // See if we can find the method corresponding to this call
                targetMethod = new JsDeclarationFinder().findMethodDeclaration(info, call, path, 
                        alternativesHolder);
                if (targetMethod == null) {
                    return false;
                }
            }

            callLineStart = currentLineStart;
            callMethod = targetMethod;

            methodHolder[0] = callMethod;
            parameterIndexHolder[0] = index;

            if (anchorOffset == -1) {
                anchorOffset = call.getSourceStart(); // TODO - compute
            }
            anchorOffsetHolder[0] = anchorOffset;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return false;
        }

        return true;
    }
    
    private static class CompletionRequest {
        private TokenHierarchy<Document> th;
        private CompilationInfo info;
        private AstPath path;
        private Node node;
        private Node root;
        private int anchor;
        private int lexOffset;
        private int astOffset;
        private BaseDocument doc;
        private String prefix;
        private JsIndex index;
        private NameKind kind;
        private JsParseResult result;
        private QueryType queryType;
        private FileObject fileObject;
        private HtmlFormatter formatter;
        private Call call;
        private String fqn;
    }

    private abstract class JsCompletionItem implements CompletionProposal {
        protected CompletionRequest request;
        protected Element element;
        protected boolean smart;

        private JsCompletionItem(Element element, CompletionRequest request) {
            this.element = element;
            this.request = request;
        }

        public int getAnchorOffset() {
            return request.anchor;
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

        public ElementHandle getElement() {
            // XXX Is this called a lot? I shouldn't need it most of the time
            return element;
        }

        public ElementKind getKind() {
            return element.getKind();
        }

        public ImageIcon getIcon() {
            return null;
        }

        public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);

            return formatter.getText();
        }

        public String getRhsHtml() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        @Override
        public String toString() {
            String cls = this.getClass().getName();
            cls = cls.substring(cls.lastIndexOf('.') + 1);

            return cls + "(" + getKind() + "): " + getName();
        }

        void setSmart(boolean smart) {
            this.smart = smart;
        }

        public boolean isSmart() {
            return smart;
        }

        public List<String> getInsertParams() {
            return null;
        }
        
        public String[] getParamListDelimiters() {
            return new String[] { "(", ")" }; // NOI18N
        }

        public String getCustomInsertTemplate() {
            return null;
        }
    }

    private class FunctionItem extends JsCompletionItem {
        private IndexedFunction function;
        FunctionItem(IndexedFunction element, CompletionRequest request) {
            super(element, request);
            this.function = element;
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }
        
        @Override
        public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            boolean strike = !SupportedBrowsers.getInstance().isSupported(function.getCompatibility());
            if (strike) {
                formatter.deprecated(true);
            }
//            boolean emphasize = !method.isInherited();
    boolean emphasize = false;
            if (emphasize) {
                formatter.emphasis(true);
            }
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);
            if (emphasize) {
                formatter.emphasis(false);
            }

            Collection<String> parameters = function.getParameters();

            formatter.appendHtml("("); // NOI18N
            if ((parameters != null) && (parameters.size() > 0)) {

                Iterator<String> it = parameters.iterator();

                while (it.hasNext()) { // && tIt.hasNext()) {
                    formatter.parameters(true);
                    formatter.appendText(it.next());
                    formatter.parameters(false);

                    if (it.hasNext()) {
                        formatter.appendText(", "); // NOI18N
                    }
                }

            }
            formatter.appendHtml(")"); // NOI18N
            
//            if (method.hasBlock() && !method.isBlockOptional()) {
//                formatter.appendText(" { }");
//            }

            if (strike) {
                formatter.deprecated(false);
            }
            
            
            return formatter.getText();
        }

        @Override
        public String getRhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();

            // Top level methods (defined on Object) : print
            // the defining file instead
//            if (method.isTopLevel() && method.getRequire() != null) {
//                formatter.appendText(method.getRequire());
//
//                return formatter.getText();
//            }

            String in = function.getIn();

            if (in != null) {
                formatter.appendText(in);
                return formatter.getText();
            } else {
                String filename = function.getFilenameUrl();
                if (filename != null && filename.indexOf("jsstubs") == -1) { // NOI18N
                    int index = filename.lastIndexOf('/');
                    if (index != -1) {
                        filename = filename.substring(index+1);
                    }
                    formatter.appendText(filename);
                    return formatter.getText();
                }
                
                return null;
            }
        }

        @Override
        public List<String> getInsertParams() {
            return function.getParameters();
        }

        @Override
        public String getCustomInsertTemplate() {
            final String insertPrefix = getInsertPrefix();
            List<String> params = getInsertParams();
            String startDelimiter = "(";
            String endDelimiter = ")";
            int paramCount = params.size();
                
            StringBuilder sb = new StringBuilder();
            sb.append(insertPrefix);
            sb.append(startDelimiter);
            
            int id = 1;
            for (int i = 0; i < paramCount; i++) {
                String paramDesc = params.get(i);
                sb.append("${"); //NOI18N
                // Ensure that we don't use one of the "known" logical parameters
                // such that a parameter like "path" gets replaced with the source file
                // path!
                sb.append("js-cc-"); // NOI18N
                sb.append(Integer.toString(id++));
                sb.append(" default=\""); // NOI18N
                sb.append(paramDesc);
                sb.append("\""); // NOI18N
                sb.append("}"); //NOI18N
                if (i < paramCount-1) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(endDelimiter);
            
            sb.append("${cursor}"); // NOI18N
            
            // Facilitate method parameter completion on this item
            try {
                callLineStart = Utilities.getRowStart(request.doc, request.anchor);
                callMethod = function;
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
            
            return sb.toString();
        }
//        
//        @Override
//        public String[] getParamListDelimiters() {
//            // TODO - convert methods with NO parameters that take a block to insert { <here> }
//            String n = getName();
//            String in = element.getIn();
//            if ("Module".equals(in)) {
//                // Module.attr_ methods typically shouldn't use parentheses
//                if (n.startsWith("attr_"))  {
//                    return new String[] { " :", " " };
//                } else if (n.equals("include") || n.equals("import")) { // NOI18N
//                    return new String[] { " ", " " };
//                } else if (n.equals("include_package")) { // NOI18N
//                    return new String[] { " '", "'" }; // NOI18N
//                }
//            } else if ("Kernel".equals(in)) {
//                // Module.require: insert quotes!
//                if (n.equals("require")) { // NOI18N
//                    return new String[] { " '", "'" }; // NOI18N
//                } else if (n.equals("p")) {
//                    return new String[] { " ", " " }; // NOI18N
//                }
//            } else if ("Object".equals(in)) {
//                if (n.equals("include_class")) { // NOI18N
//                    return new String[] { " '", "'" }; // NOI18N
//                }
//            }
//            
//            if (forceCompletionSpaces()) {
//                // Can't have "" as the second arg because a bug causes pressing
//                // return to complete editing the last field (at he end of a buffer)
//                // such that the caret ends up BEFORE the last char instead of at the
//                // end of it
//                boolean ambiguous = false;
//                
//                AstPath path = request.path;
//                if (path != null) {
//                    Iterator<Node> it = path.leafToRoot();
//
//                    while (it.hasNext()) {
//                        Node node = it.next();
//
//                        if (AstUtilities.isCall(node)) {
//                            // We're in a call; see if it has parens
//                            // TODO - no problem with ambiguity if it's on a separate line, correct?
//                            
//                            // Is this the method we're trying to complete?
//                            if (node != request.node) {
//                                // See if the outer call has parentheses!
//                                ambiguous = true;
//                                break;
//                            }
//                        }
//                    }
//                }
//                
//                if (ambiguous) {
//                    return new String[] { "(", ")" }; // NOI18N
//                } else {
//                    return new String[] { " ", " " }; // NOI18N
//                }
//            }
//
//            if (element instanceof IndexedElement) {
//                List<String> comments = getComments(null, element);
//                if (comments != null && comments.size() > 0) {
//                    // Look through the comment, attempting to identify
//                    // a usage of the current method and determine whether it
//                    // is using parentheses or not.
//                    // We only look for comments that look like code; e.g. they
//                    // are indented according to rdoc conventions.
//                    String name = getName();
//                    boolean spaces = false;
//                    boolean parens = false;
//                    for (String line : comments) {
//                        if (line.startsWith("#  ")) { // NOI18N
//                            // Look for usages - there could be many
//                            int i = 0;
//                            int length = line.length();
//                            while (true) {
//                                int index = line.indexOf(name, i);
//                                if (index == -1) {
//                                    break;
//                                }
//                                index += name.length();
//                                i = index;
//                                if (index < length) {
//                                    char c = line.charAt(index);
//                                    if (c == ' ') {
//                                        spaces = true;
//                                    } else if (c == '(') {
//                                        parens = true;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    
//                    // Only use spaces if no parens were seen and we saw spaces
//                    if (!parens && spaces) {
//                        //return new String[] { " ", "" }; // NOI18N
//                        // HACK because live code template editing doesn't seem to work - it places the caret at theront of the word when the last param is in the text!                        
//                        return new String[] { " ", " " }; // NOI18N
//                    }
//                }
//                
//                // Take a look at the method definition itself and look for parens there
//                
//            }
//
//            // Default - (,)
//            return super.getParamListDelimiters();
//        }
//
//        @Override
//        public ElementKind getKind() {
//            if (method.getMethodType() == IndexedFunction.MethodType.ATTRIBUTE) {
//                return ElementKind.ATTRIBUTE;
//            }
//
//            return element.getKind();
//        }
    }

    private class KeywordItem extends JsCompletionItem {
        private static final String Js_KEYWORD = "org/netbeans/modules/javascript/editing/javascript.png"; //NOI18N
        private final String keyword;
        private final String description;

        KeywordItem(String keyword, String description, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
            this.description = description;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        //@Override
        //public String getLhsHtml() {
        //    // Override so we can put HTML contents in
        //    ElementKind kind = getKind();
        //    HtmlFormatter formatter = request.formatter;
        //    formatter.reset();
        //    formatter.name(kind, true);
        //    //formatter.appendText(getName());
        //    formatter.appendHtml(getName());
        //    formatter.name(kind, false);
        //
        //    return formatter.getText();
        //}

        @Override
        public String getRhsHtml() {
            if (description != null) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                //formatter.appendText(description);
                formatter.appendHtml(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = new ImageIcon(org.openide.util.Utilities.loadImage(Js_KEYWORD));
            }

            return keywordIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
        
        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return new KeywordElement(keyword);
        }
    }

    private class TagItem extends JsCompletionItem {
        private final String tag;
        private final String description;

        TagItem(String keyword, String description, CompletionRequest request) {
            super(null, request);
            this.tag = keyword;
            this.description = description;
        }

        @Override
        public String getName() {
            return tag;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.TAG;
        }

        //@Override
        //public String getLhsHtml() {
        //    // Override so we can put HTML contents in
        //    ElementKind kind = getKind();
        //    HtmlFormatter formatter = request.formatter;
        //    formatter.reset();
        //    formatter.name(kind, true);
        //    //formatter.appendText(getName());
        //    formatter.appendHtml(getName());
        //    formatter.name(kind, false);
        //
        //    return formatter.getText();
        //}

        @Override
        public String getRhsHtml() {
            if (description != null) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                //formatter.appendText(description);
                formatter.appendHtml("<i>");
                formatter.appendHtml(description);
                formatter.appendHtml("</i>");

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
        
        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return new KeywordElement(tag);
        }
    }
    
    private class PlainItem extends JsCompletionItem {
        PlainItem(Element element, CompletionRequest request) {
            super(element, request);
        }
    }

    public ElementHandle resolveLink(String link, ElementHandle elementHandle) {
        if (link.indexOf(':') != -1) {
            link = link.replace(':', '.');
            return new ElementHandle.UrlHandle(link);
        }
        return null;
    }
}
