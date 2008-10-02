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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.php.editor.PredefinedSymbols.MagicIndexedFunction;
import org.netbeans.modules.php.editor.CompletionContextFinder.KeywordCompletionType;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import static org.netbeans.modules.php.editor.CompletionContextFinder.CompletionContext;
import static org.netbeans.modules.php.editor.CompletionContextFinder.lexerToASTOffset;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPCodeCompletion implements CodeCompletionHandler {
    private static final Logger LOGGER = Logger.getLogger(PHPCodeCompletion.class.getName());


    final static Map<String,KeywordCompletionType> PHP_KEYWORDS = new HashMap<String, KeywordCompletionType>();
    static {
        PHP_KEYWORDS.put("__FILE__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("__LINE__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("__FUNCTION__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("__CLASS__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("__METHOD__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("use", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("php_user_filter", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("class", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("const", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("continue", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("function", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("new", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("static", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("var", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("final", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("interface", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("instanceof", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("implements", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("extends", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("public", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("private", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("protected", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("abstract", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("clone", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("global", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("throw", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("if", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("switch", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("for", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("array", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("die", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("eval", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("exit", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("empty", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("foreach", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("isset", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("list", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("print", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("unset", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("while", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("catch", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("try", KeywordCompletionType.ENDS_WITH_CURLY_BRACKETS);
        PHP_KEYWORDS.put("endif", KeywordCompletionType.ENDS_WITH_SEMICOLON);
        PHP_KEYWORDS.put("case", KeywordCompletionType.ENDS_WITH_COLON);
    }

    private final static String[] PHP_KEYWORD_FUNCTIONS = {
        "echo", "include", "include_once", "require", "require_once"}; //NOI18N

    final static String[] PHP_CLASS_KEYWORDS = {
        "$this->", "self::", "parent::"
    };

    private final static Collection<Character> AUTOPOPUP_STOP_CHARS = new TreeSet<Character>(
            Arrays.asList(' ', '=', ';', '+', '-', '*', '/',
                '%', '(', ')', '[', ']', '{', '}', '?'));

    private static final List<String> INVALID_PROPOSALS_FOR_CLS_MEMBERS =
            Arrays.asList(new String[] {"__construct","__destruct"});//NOI18N

    private static final List<String> CLASS_CONTEXT_KEYWORD_PROPOSAL =
            Arrays.asList(new String[] {"abstract","const","function", "private", "final",
            "protected", "public", "static", "var"});//NOI18N

    private static final List<String> INHERITANCE_KEYWORDS =
            Arrays.asList(new String[] {"extends","implements"});//NOI18N

    private boolean caseSensitive;
    private NameKind nameKind;



    public CodeCompletionResult complete(CodeCompletionContext completionContext) {
         long startTime = 0;

        if (LOGGER.isLoggable(Level.FINE)){
            startTime = System.currentTimeMillis();
        }

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        BaseDocument doc = (BaseDocument) completionContext.getInfo().getDocument();

        // TODO: separate the code that uses informatiom from lexer
        // and avoid running the index/ast analysis under read lock
        // in order to improve responsiveness
        doc.readLock();

        try{
            CompilationInfo info = completionContext.getInfo();
            int caretOffset = completionContext.getCaretOffset();
            String prefix = completionContext.getPrefix();
            this.caseSensitive = completionContext.isCaseSensitive();
            this.nameKind = caseSensitive ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX;

            PHPParseResult result = (PHPParseResult) info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, caretOffset);

            if (result.getProgram() == null){
                return CodeCompletionResult.NONE;
            }

            CompletionContext context = CompletionContextFinder.findCompletionContext(info, caretOffset);
            LOGGER.fine("CC context: " + context);

            if (context == CompletionContext.NONE){
                return CodeCompletionResult.NONE;
            }

            PHPCompletionItem.CompletionRequest request = new PHPCompletionItem.CompletionRequest();
            request.anchor = caretOffset - prefix.length();
            request.result = result;
            request.info = info;
            request.prefix = prefix;
            request.index = PHPIndex.get(request.info.getIndex(PHPLanguage.PHP_MIME_TYPE));

            try {
                request.currentlyEditedFileURL = result.getFile().getFileObject().getURL().toString();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }


            switch(context){
                case EXPRESSION:
                    autoCompleteExpression(proposals, request);
                    break;
                case HTML:
                    proposals.add(new PHPCompletionItem.KeywordItem("<?php", request)); //NOI18N
                    proposals.add(new PHPCompletionItem.KeywordItem("<?=", request)); //NOI18N
                    break;
                case CLASS_NAME:
                    autoCompleteClassNames(proposals, request,false);
                    break;
                case INTERFACE_NAME:
                    autoCompleteInterfaceNames(proposals, request);
                    break;
                case TYPE_NAME:
                    autoCompleteClassNames(proposals, request,false);
                    autoCompleteInterfaceNames(proposals, request);
                    break;
                case STRING:
                    // LOCAL VARIABLES
                    proposals.addAll(getVariableProposals(request.result.getProgram(), request));
                    break;
                case CLASS_MEMBER:
                    autoCompleteClassMembers(proposals, request, false);
                    break;
                case STATIC_CLASS_MEMBER:
                    autoCompleteClassMembers(proposals, request, true);
                    break;
                case PHPDOC:
                    if (PHPDOCCodeCompletion.isTypeCtx(request)){
                        autoCompleteClassNames(proposals, request,false);
                        autoCompleteInterfaceNames(proposals, request);
                    } else {
                        PHPDOCCodeCompletion.complete(proposals, request);
                    }
                    break;
                case CLASS_CONTEXT_KEYWORDS:
                    autoCompleteInClassContext(info, caretOffset, proposals, request);
                    break;
                case METHOD_NAME:
                    autoCompleteMethodName(info, caretOffset, proposals, request);
                    break;
                case IMPLEMENTS:
                    autoCompleteKeywords(proposals, request, Collections.singletonList("implements"));//NOI18N
                    break;
                case EXTENDS:
                    autoCompleteKeywords(proposals, request, Collections.singletonList("extends"));//NOI18N
                    break;
                case INHERITANCE:
                    autoCompleteKeywords(proposals, request, INHERITANCE_KEYWORDS);
                    break;
                case SERVER_ENTRY_CONSTANTS:
                    //TODO: probably better PHPCompletionItem instance should be used
                    //autoCompleteMagicItems(proposals, request, PredefinedSymbols.SERVER_ENTRY_CONSTANTS);
                    for (String keyword : PredefinedSymbols.SERVER_ENTRY_CONSTANTS) {
                        if (keyword.startsWith(request.prefix)) {
                            proposals.add(new PHPCompletionItem.KeywordItem(keyword, request) {
                                @Override
                                public ImageIcon getIcon() {
                                    return null;
                                }
                            });
                        }
                    }

                    break;
            }
        } finally {
            doc.readUnlock();
        }

        if (LOGGER.isLoggable(Level.FINE)){
            long time = System.currentTimeMillis() - startTime;
            LOGGER.fine(String.format("complete() took %d ms, result contains %d items", time, proposals.size()));
        }

        return new PHPCompletionResult(completionContext, proposals);
    }

    private void autoCompleteClassNames(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request,boolean endWithDoubleColon) {
        for (IndexedClass clazz : request.index.getClasses(request.result, request.prefix, nameKind)) {
            proposals.add(new PHPCompletionItem.ClassItem(clazz, request, endWithDoubleColon));
        }
    }

    private void autoCompleteInterfaceNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        for (IndexedInterface iface : request.index.getInterfaces(request.result, request.prefix, nameKind)) {
            proposals.add(new PHPCompletionItem.InterfaceItem(iface, request));
        }
    }

    private void autoCompleteMagicItems(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request,final Collection<String> proposedTexts,
            boolean completeNameAndBodyOnly, Set<String> insideNames) {
        for (String keyword : proposedTexts) {
            if (keyword.startsWith(request.prefix) && !insideNames.contains(keyword)) {
                IndexedFunction magicFunc = PredefinedSymbols.MAGIC_METHODS.get(keyword);
                if (magicFunc != null) {
                    if (completeNameAndBodyOnly) {
                        proposals.add(new PHPCompletionItem.MagicMethodNameItem(magicFunc, request));
                    } else {
                        proposals.add(new PHPCompletionItem.MagicMethodItem(magicFunc, request));
                    }
                }
            }
        }
    //autoCompleteKeywords(proposals, request, METHOD_NAME_PROPOSALS);
    }
    private void autoCompleteKeywords(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request, List<String> keywordList) {
        for (String keyword : keywordList) {
            if (keyword.startsWith(request.prefix)) {
                proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

    }

    private static final Collection<PHPTokenId> CTX_DELIMITERS = Arrays.asList(
            PHPTokenId.PHP_SEMICOLON, PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_CURLY_CLOSE,
            PHPTokenId.PHP_RETURN, PHPTokenId.PHP_OPERATOR, PHPTokenId.PHP_ECHO,
            PHPTokenId.PHP_EVAL, PHPTokenId.PHP_NEW, PHPTokenId.PHP_NOT,PHPTokenId.PHP_CASE,
            PHPTokenId.PHP_IF,PHPTokenId.PHP_ELSE,PHPTokenId.PHP_ELSEIF, 
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH,PHPTokenId.PHP_WHILE,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_LINE_COMMENT
            );

    private String findLHSExpressionType(TokenSequence<PHPTokenId> tokenSequence,
            PHPCompletionItem.CompletionRequest request){

        // find the beginning of the left hand side expression

        while (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
            if (!tokenSequence.movePrevious()){
                return null;
            }
        }

        int rightExpressionBoundary = tokenSequence.offset();

        while (findLHSExpressionType_skipArgs(tokenSequence, true)
                && !CTX_DELIMITERS.contains(tokenSequence.token().id())
                && tokenSequence.token().id() != PHPTokenId.PHP_TOKEN){
            if (!tokenSequence.movePrevious()) {
                break;
            }
        }
        //move forward to the first text
        do {
            if (!tokenSequence.moveNext()){
                return null;
            }
            findLHSExpressionType_skipArgs(tokenSequence, false);
        } while (tokenSequence.token().id() == PHPTokenId.WHITESPACE);

        int leftExpressionBoundary = tokenSequence.offset(); // dbg only

        if (LOGGER.isLoggable(Level.FINE)){
            try {
                LOGGER.fine("evaluating expression '" + request.info.getDocument().getText(
                        leftExpressionBoundary, rightExpressionBoundary - leftExpressionBoundary) + "'");

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        String preceedingType = null;
        boolean staticContex = false;
        PHPTokenId tokenID = tokenSequence.token().id();
        String varName = "";//NOI18N
        switch (tokenID) {
            case PHP_SELF:
            case PHP_PARENT:
                staticContex = true;
                 {
                    ClassDeclaration classDecl = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));

                     if (classDecl != null) {
                         if (tokenSequence.token().id() == PHPTokenId.PHP_PARENT) {
                             Identifier superIdentifier = classDecl.getSuperClass();

                             if (superIdentifier != null) {
                                 preceedingType = superIdentifier.getName();
                             }
                         } else {
                            preceedingType = classDecl.getName().getName();
                        }
                    }
                }

                break;
            case PHP_STRING: //class name in static invokation or function name
                String functionName = findLHSideExpressionType_extractFunctionNameFromCall(tokenSequence);

                if (functionName != null) {
                    for (IndexedFunction func : request.index.getFunctions(request.result,
                            functionName, NameKind.EXACT_NAME)) {

                        preceedingType = func.getReturnType();
                    }
                } else {
                    // class name or a special var in a static method call
                    preceedingType = tokenSequence.token().text().toString();
                }

                break;
            case PHP_NEW:
                tokenSequence.moveNext();
                tokenSequence.moveNext(); // skip the whitespace
                preceedingType = tokenSequence.token().text().toString();
                break;

            case PHP_VARIABLE:
                varName = tokenSequence.token().text().toString();

                if ("$this".equalsIgnoreCase(varName)) { //NOI18N
                    ClassDeclaration classDecl = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
                    if (classDecl != null) {
                        preceedingType = classDecl.getName().getName();
                    }

                } else {
                    Collection<IndexedConstant> vars = getVariables(request.result, request.index,
                            varName, request.anchor, request.currentlyEditedFileURL);

                    if (vars != null) {
                        for (IndexedConstant var : vars) {
                            if (var.getName().equals(varName)) { // could be just a prefix
                                preceedingType = var.getTypeName();
                                break;
                            }
                        }
                    }
                }
                break;
        }

        do {
            if (!tokenSequence.moveNext()){
                return null;
            }
        } while (tokenSequence.token().id() == PHPTokenId.WHITESPACE);

        if ((preceedingType == null || preceedingType.length() == 0) && tokenID == PHPTokenId.PHP_VARIABLE && varName != null && varName.length() > 0) {
            VarTypeResolver typeResolver = VarTypeResolver.getInstance(request, varName);
            preceedingType = typeResolver.resolveType();
            if (preceedingType != null) {
                return preceedingType;
            }
        }
        if (preceedingType == null || tokenSequence.offset() == rightExpressionBoundary){
            return preceedingType;
        }

        if (rightExpressionBoundary < tokenSequence.offset()){
            if (LOGGER.isLoggable(Level.FINE)){
                String errorMsg = String.format("Error finding expression boundary: " +
                        "caretOffset=%d, leftExpressionBoundary=%d," +
                        " rightExpressionBoundary=%d, tokenSequence:\n%s",
                        request.anchor, leftExpressionBoundary, rightExpressionBoundary, tokenSequence.toString()); //NOI18N

                LOGGER.fine(errorMsg);
            }

            return null;
        }

        return findLHSExpressionType_recursive(tokenSequence, request,
                preceedingType, staticContex, rightExpressionBoundary);
    }

    private boolean findLHSExpressionType_skipArgs(TokenSequence<PHPTokenId> tokenSequence, boolean backwards) {

        String startingSymbol = "(";
        String closingSymbol = ")";

        if (backwards){
            startingSymbol = ")"; //NOI18N
            closingSymbol = "("; //NOI18N
        }

        if (tokenSequence.token().id() == PHPTokenId.PHP_TOKEN 
                && startingSymbol.equals(tokenSequence.token().text().toString())) {

            int balance = 1;
            boolean endingParenthesis;

            do {
                if (backwards && !tokenSequence.movePrevious() 
                        || !backwards && !tokenSequence.moveNext()) {
                    
                    return true;
                }

                if (tokenSequence.token().id() == PHPTokenId.PHP_TOKEN 
                        && startingSymbol.equals(tokenSequence.token().text().toString())) {
                    balance++;
                }

                endingParenthesis = tokenSequence.token().id() == PHPTokenId.PHP_TOKEN 
                        && closingSymbol.equals(tokenSequence.token().text().toString()); //NOI18N

                if (endingParenthesis) {
                    balance--;
                }

            } while (!(endingParenthesis && balance == 0));

            if (backwards){
                tokenSequence.movePrevious();
            } else {
                tokenSequence.moveNext();
            }
        }

        return true;
    }

    private String findLHSExpressionType_recursive(TokenSequence<PHPTokenId> tokenSequence,
            PHPCompletionItem.CompletionRequest request,
            String preceedingType, boolean staticContext, int rightExpressionBoundary){
        String type = null;

        do {
            if (!tokenSequence.moveNext()){
                return null;
            }
        } while (tokenSequence.token().id() == PHPTokenId.WHITESPACE);

        String methodName = findLHSideExpressionType_extractFunctionNameFromCall(tokenSequence);

        if (methodName != null){
            for (IndexedFunction func : request.index.getAllMethods(request.result, preceedingType,
                    methodName, NameKind.EXACT_NAME, Integer.MAX_VALUE)) {

                type = func.getReturnType();
            }
        } else {
            String fieldName = tokenSequence.token().text().toString();
            if (fieldName.startsWith("$")) {//NOI18N
                fieldName = fieldName.substring(1);
            }

            for (IndexedConstant field : request.index.getAllFields(request.result, preceedingType,
                    fieldName, NameKind.EXACT_NAME, Integer.MAX_VALUE)) {

                type = field.getTypeName();
            }
            if (type == null && preceedingType != null && fieldName != null) {
                VarTypeResolver typeResolver = VarTypeResolver.getInstance(request, preceedingType+"::"+fieldName);//NOI18N
                type = typeResolver.resolveType();
            }
        }

        do {
            if (!tokenSequence.moveNext()){
                return null;
            }
        } while (tokenSequence.token().id() == PHPTokenId.WHITESPACE);

        if (type == null || tokenSequence.offset() == rightExpressionBoundary)
        {
            return type;
        }

        if (rightExpressionBoundary < tokenSequence.offset()){
            if (LOGGER.isLoggable(Level.FINE)){
                String errorMsg = String.format("Error finding expression boundary: caretOffset=%d, " +
                        "rightExpressionBoundary=%d, tokenSequence:\n%s",
                        request.anchor, rightExpressionBoundary, tokenSequence.toString()); //NOI18N

                LOGGER.fine(errorMsg);
            }

            return null;
        }

        return findLHSExpressionType_recursive(tokenSequence, request,
                type, staticContext, rightExpressionBoundary);
    }

    private String findLHSideExpressionType_extractFunctionNameFromCall(TokenSequence tokenSequence) {
        String functionName = tokenSequence.token().text().toString();
        int orgPos = tokenSequence.offset();

        do {
            tokenSequence.moveNext();
        }  while (tokenSequence.token().id() == PHPTokenId.WHITESPACE);

        if (tokenSequence.token().id() == PHPTokenId.PHP_TOKEN) {
            CharSequence tokenTxt = tokenSequence.token().text();

            // function call
            if (tokenTxt.length() == 1 && tokenTxt.charAt(0) == '(') {
                // confirmed, it is a function call
                // position the token sequence after the call
                findLHSExpressionType_skipArgs(tokenSequence, false);
                tokenSequence.movePrevious();
            } else {
                functionName = null;
            }
        } else {
            functionName = null;
        }

        if (functionName == null) {
            tokenSequence.move(orgPos);
            tokenSequence.moveNext();
        }

        return functionName;
    }

    private void autoCompleteMethodName(CompilationInfo info, int caretOffset, List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request) {
        ClassDeclaration enclosingClass = findEnclosingClass(info, lexerToASTOffset(info, caretOffset));
        if (enclosingClass != null) {
            String clsName = enclosingClass.getName().getName();
            Set<String> insideNames = new HashSet<String>();
            Collection<IndexedFunction> methods = request.index.getMethods(
                    request.result, clsName, request.prefix,
                    NameKind.CASE_INSENSITIVE_PREFIX, PHPIndex.ANY_ATTR);
            for (IndexedFunction meth : methods) {
                insideNames.add(meth.getName());
            }
            autoCompleteMagicItems(proposals, request, PredefinedSymbols.MAGIC_METHODS.keySet(),true, insideNames);
        }

    }
    private void autoCompleteInClassContext(CompilationInfo info, int caretOffset, List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request) {
        Document document = info.getDocument();
        TokenHierarchy<?> th = TokenHierarchy.get(document);
        TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence(PHPTokenId.language());
        assert tokenSequence != null;

        tokenSequence.move(caretOffset);
        boolean offerMagicAndInherited = true;
        if (!(!tokenSequence.moveNext() && !tokenSequence.movePrevious())) {
            Token<PHPTokenId> token = tokenSequence.token();
            int tokenIdOffset = tokenSequence.token().offset(th);
            offerMagicAndInherited = !CompletionContextFinder.lineContainsAny(token, caretOffset - tokenIdOffset, tokenSequence, Arrays.asList(new PHPTokenId[]{
                        PHPTokenId.PHP_PRIVATE,
                        PHPTokenId.PHP_PUBLIC,
                        PHPTokenId.PHP_PROTECTED,
                        PHPTokenId.PHP_ABSTRACT,
                        PHPTokenId.PHP_VAR,
                        PHPTokenId.PHP_STATIC,
                        PHPTokenId.PHP_CONST
                    }));
        }

        autoCompleteKeywords(proposals, request, CLASS_CONTEXT_KEYWORD_PROPOSAL);
        ClassDeclaration enclosingClass = findEnclosingClass(info, lexerToASTOffset(info, caretOffset));
        Set<String> insideNames = new HashSet<String>();
        Set<String> methodNames = new HashSet<String>();
        if (enclosingClass != null) {
            String clsName = enclosingClass.getName().getName();
            Collection<IndexedFunction> methods = request.index.getMethods(
                    request.result, clsName, request.prefix,
                    NameKind.CASE_INSENSITIVE_PREFIX, PHPIndex.ANY_ATTR);
            for (IndexedFunction meth : methods) {
                insideNames.add(meth.getName());
                methodNames.add(meth.getName());
            }
        }
        if (enclosingClass != null && offerMagicAndInherited) {
            Identifier superClass = enclosingClass.getSuperClass();
            if (superClass != null) {
                String superClsName = superClass.getName();
                Collection<IndexedFunction> superMethods = request.index.getAllMethods(
                        request.result, superClsName, request.prefix,
                        NameKind.CASE_INSENSITIVE_PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
                for (IndexedFunction superMeth : superMethods) {
                    if (superMeth.getName().startsWith(request.prefix) &&
                            !superMeth.isFinal() &&
                            !insideNames.contains(superMeth.getName()) &&
                            !methodNames.contains(superMeth.getName())) {
                        for (int i = 0; i <= superMeth.getOptionalArgs().length; i++) {
                            methodNames.add(superMeth.getName());
                            proposals.add(new PHPCompletionItem.FunctionDeclarationItem(superMeth, request, i, false));
                        }
                    }
                }
            }
            List<Identifier> interfaces = enclosingClass.getInterfaes();
            for (Identifier identifier : interfaces) {
                String ifaceName = identifier.getName();
                Collection<IndexedFunction> superMethods = request.index.getAllMethods(
                        request.result, ifaceName, request.prefix,
                        NameKind.CASE_INSENSITIVE_PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
                for (IndexedFunction ifaceMeth : superMethods) {
                    if (ifaceMeth.getName().startsWith(request.prefix) && !ifaceMeth.isFinal() && !methodNames.contains(ifaceMeth.getName())) {
                        for (int i = 0; i <= ifaceMeth.getOptionalArgs().length; i++) {
                            methodNames.add(ifaceMeth.getName());
                            proposals.add(new PHPCompletionItem.FunctionDeclarationItem(ifaceMeth, request, i, true));
                        }
                    }
                }

            }
            List<String> magicMethods = new ArrayList<String>();
            for (String name : PredefinedSymbols.MAGIC_METHODS.keySet()) {
                if (!methodNames.contains(name)) {
                    methodNames.add(name);
                    magicMethods.add(name);
                }
            }
            autoCompleteMagicItems(proposals, request, magicMethods, false, insideNames);
        }

    }

    private void autoCompleteClassMembers(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request, boolean staticContext) {
        Document document = request.info.getDocument();
        if (document == null) {
            return;
        }

        // TODO: remove duplicate/redundant code from here

        //TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(document, request.anchor);

        if (tokenSequence == null){
            return;
        }

        tokenSequence.move(request.anchor);
        if (tokenSequence.movePrevious())
        {
            boolean instanceContext = !staticContext;
            boolean includeInherited = true;
            boolean moreTokens = true;
            int attrMask = Modifier.PUBLIC;

            if (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
                moreTokens = tokenSequence.movePrevious();
            }

            moreTokens = tokenSequence.movePrevious();

            String varName = tokenSequence.token().text().toString();
            String typeName = null;
            List<String> invalidProposalsForClsMembers = INVALID_PROPOSALS_FOR_CLS_MEMBERS;
            if (varName.equals("self")) { //NOI18N
                ClassDeclaration classDecl = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
                if (classDecl != null) {
                    typeName = classDecl.getName().getName();
                    staticContext = true;
                    includeInherited = true;
                    attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                }
            } else if (varName.equals("parent")) { //NOI18N
                invalidProposalsForClsMembers = Collections.emptyList();

                ClassDeclaration classDecl = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
                if (classDecl != null) {
                    Identifier superIdentifier = classDecl.getSuperClass();
                    if (superIdentifier != null) {
                        typeName = superIdentifier.getName();
                        staticContext = instanceContext = true;
                        attrMask |= Modifier.PROTECTED;
                    }
                }
            } else if (varName.equals("$this")) { //NOI18N
                ClassDeclaration classDecl = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
                if (staticContext) {
                    return;
                }
                if (classDecl != null) {
                    typeName = classDecl.getName().getName();
                    staticContext = false;
                    instanceContext = true;
                    attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                }
            } else {
                if (staticContext) {
                    if (varName.startsWith("$")) {//NOI18N
                        return;
                    }
                    typeName = varName;
                } else {
                    assert typeName == null;
                    Collection<IndexedConstant> vars = getVariables(request.result, request.index,
                            varName, request.anchor, request.currentlyEditedFileURL);

                    if (vars != null) {
                        for (IndexedConstant var : vars) {
                            if (var.getName().equals(varName)) { // can be just a prefix
                                typeName = var.getTypeName();
                                break;
                            }
                        }
                    }
                }
            }

            // end of a cluster of concentrated duplicated/redundant code

            tokenSequence.move(request.anchor);

            if (tokenSequence.movePrevious()){
                // typeName is unconditionally overriden on purpose!
                typeName = findLHSExpressionType(tokenSequence, request);
            }

            if (typeName != null){
                Collection<IndexedFunction> methods = includeInherited ?
                    request.index.getAllMethods(request.result, typeName, request.prefix, nameKind, attrMask) :
                    request.index.getMethods(request.result, typeName, request.prefix, nameKind, attrMask);

                for (IndexedFunction method : methods){
                    if (staticContext && method.isStatic() || instanceContext && !method.isStatic()) {
                        for (int i = 0; i <= method.getOptionalArgs().length; i ++){
                            if (!invalidProposalsForClsMembers.contains(method.getName())) {
                                proposals.add(new PHPCompletionItem.FunctionItem(method, request, i));
                            }
                        }
                    }
                }

                String prefix = (staticContext && request.prefix.startsWith("$")) //NOI18N
                        ? request.prefix.substring(1) : request.prefix;
                Collection<IndexedConstant> properties = includeInherited ?
                    request.index.getAllFields(request.result, typeName, prefix, nameKind, attrMask) :
                    request.index.getFields(request.result, typeName, prefix, nameKind, attrMask);

                for (IndexedConstant prop : properties){
                    if (staticContext && prop.isStatic() || instanceContext && !prop.isStatic()) {
                        PHPCompletionItem.VariableItem item = new PHPCompletionItem.VariableItem(prop, request);

                        if (!staticContext) {
                            item.doNotInsertDollarPrefix();
                        }

                        proposals.add(item);
                    }
                }

                if (staticContext) {
                    Collection<IndexedConstant> classConstants = request.index.getAllClassConstants(
                            request.result, typeName, request.prefix, nameKind);
                    for (IndexedConstant constant : classConstants) {
                        proposals.add(new PHPCompletionItem.ClassConstantItem(constant, request));
                    }
                }
            }
        }
    }

    private static ClassDeclaration findEnclosingClass(CompilationInfo info, int offset) {
        List<ASTNode> nodes = NavUtils.underCaret(info, offset);
        for(ASTNode node : nodes) {
            if (node instanceof ClassDeclaration) {
                return (ClassDeclaration) node;
            }
        }
        return null;
    }

    private void autoCompleteExpression(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        // KEYWORDS
        for (String keyword : PHP_KEYWORDS.keySet()) {
            if (startsWith(keyword, request.prefix)) {
                proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

        for (String keyword : PHP_KEYWORD_FUNCTIONS) {
            if (startsWith(keyword, request.prefix)) {
                proposals.add(new PHPCompletionItem.SpecialFunctionItem(keyword, request));
            }
        }

        if (startsWith("return", request.prefix)){ //NOI18N
            proposals.add(new PHPCompletionItem.ReturnItem(request));
        }

        // end: KEYWORDS

        PHPIndex index = request.index;
        // FUNCTIONS
        for (IndexedFunction function : index.getFunctions(request.result, request.prefix, nameKind)) {
            for (int i = 0; i <= function.getOptionalArgs().length; i++) {
                proposals.add(new PHPCompletionItem.FunctionItem(function, request, i));
            }
        }

        // CONSTANTS
        for (IndexedConstant constant : index.getConstants(request.result, request.prefix, nameKind)) {
            proposals.add(new PHPCompletionItem.ConstantItem(constant, request));
        }

        // CLASS NAMES
        // TODO only show classes with static elements
        autoCompleteClassNames(proposals, request,true);

        // LOCAL VARIABLES
        proposals.addAll(getVariableProposals(request.result.getProgram(), request));

        // Special keywords applicable only inside a class
        ClassDeclaration classDecl = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
        if (classDecl != null) {
            for (String keyword : PHP_CLASS_KEYWORDS) {
                if (startsWith(keyword, request.prefix)) {
                    proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
                }
            }
        }
    }

    private Collection<CompletionProposal> getVariableProposals(Program program,
            PHPCompletionItem.CompletionRequest request){

        Collection<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        Collection<IndexedConstant> allVars = getVariables(request.result, request.index,
                request.prefix, request.anchor, request.currentlyEditedFileURL);

        for (IndexedConstant localVar : allVars){
            CompletionProposal proposal = new PHPCompletionItem.VariableItem(localVar, request);
            proposals.add(proposal);
        }

        for (String name : PredefinedSymbols.SUPERGLOBALS){
            if (isPrefix("$" + name, request.prefix)) { //NOI18N
                CompletionProposal proposal = new PHPCompletionItem.SuperGlobalItem(request, name);
                proposals.add(proposal);
            }
        }

        return proposals;
    }

    public Collection<IndexedConstant> getVariables(PHPParseResult context,  PHPIndex index,
            String namePrefix, int position, String localFileURL){

        LocalVariables localVars = getLocalVariables(context, namePrefix, position, localFileURL);
        Map<String, IndexedConstant> allVars = new LinkedHashMap<String, IndexedConstant>();

        for (IndexedConstant var : localVars.vars){
            allVars.put(var.getName(), var);
        }

        if (localVars.globalContext){
            for (IndexedConstant topLevelVar : index.getTopLevelVariables(context, namePrefix, NameKind.PREFIX)){
                if (!localFileURL.equals(topLevelVar.getFilenameUrl())){
                    IndexedConstant localVar = allVars.get(topLevelVar.getName());

                     if (localVar == null || localVar.getOffset() != topLevelVar.getOffset()){
                        IndexedConstant original = allVars.put(topLevelVar.getName(), topLevelVar);
                        if (original != null && localVars.vars.contains(original)) {
                            allVars.put(original.getName(), original);
                        }
                     }
                }
            }
        }

        for (IndexedConstant var : allVars.values()){
            CodeUtils.resolveFunctionType(context, index, allVars, var);
        }

        return allVars.values();
    }

    private void getLocalVariables_indexVariable(Variable var,
            Map<String, IndexedConstant> localVars,
            String namePrefix, String localFileURL, String type) {

        String varName = CodeUtils.extractVariableName(var);
        if (varName != null) {
            String varNameNoDollar = varName.startsWith("$") ? varName.substring(1) : varName;

            if (isPrefix(varName, namePrefix) && !PredefinedSymbols.isSuperGlobalName(varNameNoDollar)) {
                IndexedConstant ic = new IndexedConstant(varName, null,
                        null, localFileURL, var.getStartOffset(), 0, type);

                localVars.put(varName, ic);
            }
        }
    }

    private boolean isPrefix(String name, String prefix){
        return name != null && (name.startsWith(prefix)
                || nameKind == NameKind.CASE_INSENSITIVE_PREFIX && name.toLowerCase().startsWith(prefix.toLowerCase()));
    }

    private void getLocalVariables_indexVariableInAssignment(Expression expr,
            Map<String, IndexedConstant> localVars,
            String namePrefix, String localFileURL) {

        if (expr instanceof Assignment) {
            Assignment assignment = (Assignment) expr;

            if (assignment.getLeftHandSide() instanceof Variable) {
                Variable variable = (Variable) assignment.getLeftHandSide();
                String varType = CodeUtils.extractVariableTypeFromAssignment(assignment);

                getLocalVariables_indexVariable(variable, localVars, namePrefix,
                        localFileURL, varType);
            }

            if (assignment.getRightHandSide() instanceof Assignment){
                getLocalVariables_indexVariableInAssignment(assignment.getRightHandSide(),
                        localVars, namePrefix, localFileURL);
            }
        }
    }

    private class VarFinder extends DefaultVisitor {
        private Map<String, IndexedConstant> localVars = null;
        private String namePrefix;
        private String localFileURL;

        VarFinder(Map<String, IndexedConstant> localVars, String namePrefix, String localFileURL) {
            this.localVars = localVars;
            this.localFileURL = localFileURL;
            this.namePrefix = namePrefix;
        }

        @Override
        public void visit(Assignment node) {
            getLocalVariables_indexVariableInAssignment(node, localVars, namePrefix, localFileURL);
            super.visit(node);
        }

        @Override
        public void visit(GlobalStatement node) {
            for (Variable var : node.getVariables()) {
                getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
            }
            super.visit(node);
        }

        @Override
        public void visit(StaticStatement node) {
            for (Variable var : node.getVariables()) {
                getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
            }
            super.visit(node);
        }

        @Override
        public void visit(ForEachStatement forEachStatement) {

            if (forEachStatement.getKey() instanceof Variable) {
                Variable var = (Variable) forEachStatement.getKey();
                getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
            }

            if (forEachStatement.getValue() instanceof Variable) {
                Variable var = (Variable) forEachStatement.getValue();
                getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
            }
            super.visit(forEachStatement);
        }

        @Override
        public void visit(FunctionDeclaration node) {
            // do not enter!
        }
    }

    private class LocalVariables{
        Collection<IndexedConstant> vars;
        boolean globalContext;
    }

    private LocalVariables getLocalVariables(PHPParseResult context, String namePrefix, int position, String localFileURL){
        Map<String, IndexedConstant> localVars = new HashMap<String, IndexedConstant>();
        boolean globalContext = true;
        ASTNode varScopeNode = context.getProgram();

        ASTNode hierarchy[] = Utils.getNodeHierarchyAtOffset(context.getProgram(), lexerToASTOffset(context, position));

        //getNodeHierarchyAtOffset obviously return null
        if (hierarchy == null) {
            LocalVariables result = new LocalVariables();
            result.globalContext = globalContext;
            result.vars = localVars.values();
            return result;
        }
        for (ASTNode node : hierarchy){
            if (node instanceof FunctionDeclaration){
                varScopeNode = node;
                break;
            }
        }

        if (varScopeNode instanceof FunctionDeclaration) {
            FunctionDeclaration functionDeclaration = (FunctionDeclaration) varScopeNode;
            globalContext = false;
            // add parameters to the result

            Map<String, String> typeByParamName = new TreeMap<String, String>();
            Comment comment = Utils.getCommentForNode(context.getProgram(), functionDeclaration);

            if (comment instanceof PHPDocBlock) {
                PHPDocBlock phpDoc = (PHPDocBlock) comment;

                for (PHPDocTag tag : phpDoc.getTags()){
                    if (tag.getKind() == PHPDocTag.Type.PARAM){
                        PHPDocParamTagData paramData = new PHPDocParamTagData(tag.getValue());
                        typeByParamName.put(paramData.name, paramData.type);
                    }
                }
            }

            for (FormalParameter param : functionDeclaration.getFormalParameters()) {
                Expression parameterName = param.getParameterName();

                if (parameterName instanceof Reference) {
                    Reference ref = (Reference) parameterName;
                    parameterName = ref.getExpression();
                }

                if (parameterName instanceof Variable) {
                    String varName = CodeUtils.extractVariableName((Variable) parameterName);
                    if (varName != null) {
                        String type = param.getParameterType() != null ? param.getParameterType().getName() : null;

                        if (type == null){
                            type = typeByParamName.get(varName);
                        }

                        if (isPrefix(varName, namePrefix)) {
                            IndexedConstant ic = new IndexedConstant(varName, null,
                                    null, localFileURL, -1, 0, type);

                            localVars.put(varName, ic);
                        }
                    }
                }
            }

            varScopeNode = functionDeclaration.getBody();
        }

        VarFinder varFinder = new VarFinder(localVars, namePrefix, localFileURL);
        varScopeNode.accept(varFinder);
        LocalVariables result = new LocalVariables();
        result.globalContext = globalContext;
        result.vars = localVars.values();

        return result;
    }

    public String document(CompilationInfo info, ElementHandle element) {
        return (element instanceof MagicIndexedFunction) ? null :
            DocRenderer.document(info, element);
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    private static final boolean isPHPIdentifierPart(char c){
        return Character.isJavaIdentifierPart(c) || c == '@';
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument) info.getDocument();
            if (doc == null) {
                return null;
            }

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
                            if (!isPHPIdentifierPart(c)) {
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
                        int lastIndexOfDollar = prefix.lastIndexOf('$');//NOI18N
                        if (lastIndexOfDollar > 0) {
                            prefix = prefix.substring(lastIndexOfDollar);
                        }
                    } else {
                        if (lineOffset == line.length()) {
                            prefix = line.substring(start);
                        } else {
                            int n = line.length();
                            int end = lineOffset;
                            for (int j = lineOffset; j < n; j++) {
                                char d = line.charAt(j);
                                // Try to accept Foo::Bar as well
                                if (!isPHPIdentifierPart(d)) {
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
                            if (!(isPHPIdentifierPart(c) || c == '@' || c == '$' || c == ':')) {
                                return null;
                            }
                        } else {
                            for (int i = prefix.length() - 2; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?

                                char c = prefix.charAt(i);
                                if (i == 0 && c == ':') {
                                    // : is okay at the begining of prefixes
                                } else if (!(isPHPIdentifierPart(c) || c == '@' || c == '$')) {
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
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        if(typedText.length() == 0) {
            return QueryType.NONE;
        }
        char lastChar = typedText.charAt(typedText.length() - 1);

        if (AUTOPOPUP_STOP_CHARS.contains(Character.valueOf(lastChar))){
            return QueryType.STOP;
        }

        Document document = component.getDocument();
        //TokenHierarchy th = TokenHierarchy.get(document);
        int offset = component.getCaretPosition();
        TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(document, offset);
        if (ts == null) {
            return QueryType.STOP;
        }
        int diff = ts.move(offset);
        if(diff > 0 && ts.moveNext() || ts.movePrevious()) {
            Token t = ts.token();
            if(t.id() == PHPTokenId.PHP_OBJECT_OPERATOR
                    || t.id() == PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM
                    || t.id() == PHPTokenId.PHP_TOKEN && lastChar == '$'
                    || t.id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING && lastChar == '$'
                    || t.id() == PHPTokenId.PHPDOC_COMMENT && lastChar == '@') {
                return QueryType.ALL_COMPLETION;
            }
        }
        return QueryType.NONE;
    }



    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return null;
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return null;
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        //TODO: return the info for functions and methods
        return ParameterInfo.NONE;
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }
 }
