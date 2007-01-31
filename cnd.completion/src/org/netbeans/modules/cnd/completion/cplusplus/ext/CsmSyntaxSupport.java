/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.TextBatchProcessor;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.StringMap;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.ExtSyntaxSupport.DeclarationTokenProcessor;
import org.netbeans.editor.ext.ExtSyntaxSupport.VariableMapTokenProcessor;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
* Support methods for csm based syntax analyzes
*
* @author Vladimir Voskresensky
* @version 1.00
* implemented after JavaSyntaxSupport
*/

abstract public class CsmSyntaxSupport extends ExtSyntaxSupport {

    // Internal java declaration token processor states
    static final int INIT = 0;
    static final int AFTER_TYPE = 1;
    static final int AFTER_VARIABLE = 2;
    static final int AFTER_COMMA = 3;
    static final int AFTER_DOT = 4;
    static final int AFTER_TYPE_LSB = 5;
    static final int AFTER_MATCHING_VARIABLE_LSB = 6;
    static final int AFTER_MATCHING_VARIABLE = 7;
    static final int AFTER_EQUAL = 8; // in decl after "var ="
    static final int AFTER_ARROW = 9;
    static final int AFTER_SCOPE = 10;

    private static final TokenID[] COMMENT_TOKENS = new TokenID[] {
                CCTokenContext.LINE_COMMENT,
                CCTokenContext.BLOCK_COMMENT
            };

    private static final TokenID[] BRACKET_SKIP_TOKENS = new TokenID[] {
                CCTokenContext.LINE_COMMENT,
                CCTokenContext.BLOCK_COMMENT,
                CCTokenContext.CHAR_LITERAL,
                CCTokenContext.STRING_LITERAL
            };

    private static final char[] COMMAND_SEPARATOR_CHARS = new char[] {
                ';', '{', '}'
            };

    private CsmIncludeProcessor javaImport;

    /** Whether java 1.5 constructs are recognized. */
    private boolean java15;

    public CsmSyntaxSupport(BaseDocument doc) {
        super(doc);

        tokenNumericIDsValid = true;
    }

    abstract protected CsmFinder getFinder();
    
    protected CsmIncludeProcessor createIncludeProc(){
        return new CsmIncludeProcessor(this);
    }
    
    protected void documentModified(DocumentEvent evt) {
        super.documentModified(evt);
        classFieldMaps.clear();
        fileVariableMaps.clear();        
        if (javaImport != null) {
            javaImport.documentModifiedAtPosition(evt.getOffset(), getDocument());
        }
    }
    
    protected void setJava15(boolean java15) {
        this.java15 = java15;
    }

    public TokenID[] getCommentTokens() {
        return COMMENT_TOKENS;
    }

    public TokenID[] getBracketSkipTokens() {
        return BRACKET_SKIP_TOKENS;
    }

    /** Return the position of the last command separator before
    * the given position.
    */
    public int getLastCommandSeparator(int pos)
    throws BadLocationException {
        if (pos == 0)
            return 0;
        TextBatchProcessor tbp = new TextBatchProcessor() {
                                     public int processTextBatch(BaseDocument doc, int startPos, int endPos,
                                                                 boolean lastBatch) {
                                         try {
                                             int[] blks = getCommentBlocks(endPos, startPos);
                                             FinderFactory.CharArrayBwdFinder cmdFinder
                                             = new FinderFactory.CharArrayBwdFinder(COMMAND_SEPARATOR_CHARS);
                                             int lastSeparatorOffset = findOutsideBlocks(cmdFinder, startPos, endPos, blks);
                                             if (lastSeparatorOffset<1) return lastSeparatorOffset;
                                             TokenID separatorID = getTokenID(lastSeparatorOffset);
                                             if (separatorID.getNumericID() == CCTokenContext.RBRACE_ID) {
                                                 int matchingBrkPos[] = findMatchingBlock(lastSeparatorOffset, true);
                                                 if (matchingBrkPos != null){
                                                     int prev = Utilities.getFirstNonWhiteBwd(getDocument(), matchingBrkPos[0]);
                                                     if (prev > -1 && getTokenID(prev).getNumericID() == CCTokenContext.RBRACKET_ID){
                                                         return getLastCommandSeparator(prev);
                                                     }
                                                 }
                                             }
                                             if (separatorID.getNumericID() != CCTokenContext.LBRACE_ID &&
                                                 separatorID.getNumericID() != CCTokenContext.RBRACE_ID &&
                                                 separatorID.getNumericID() != CCTokenContext.SEMICOLON_ID){
                                                     lastSeparatorOffset = processTextBatch(doc, lastSeparatorOffset, 0, lastBatch);
                                             }
                                             return lastSeparatorOffset;
                                         } catch (BadLocationException e) {
                                             e.printStackTrace();
                                             return -1;
                                         }
                                     }
                                 };
        int lastPos = getDocument().processText(tbp, pos, 0);
        
        //ensure we return last command separator from last 
        //block of java tokens from <startPos;endPos> offset interval
        //AFAIK this is currently needed only for JSP code completion
        TokenItem item = getTokenChain(pos - 1, pos);
        //go back throught the token chain and try to find last java token
        do {
            int tokenOffset = item.getOffset();
            if(lastPos != -1 && tokenOffset < lastPos) break; //stop backtracking if we met the lastPos
            //test token type
            if(!item.getTokenContextPath().contains(CCTokenContext.contextPath)) {
                //return offset of last java token - this token isn't already a java token so return offset of next token
                lastPos = item.getNext() != null ? item.getNext().getOffset() : item.getOffset() + item.getImage().length();
                break;
            }
        } while( (item = item.getPrevious()) != null);
        
        return lastPos;
    }

    /** Get the class from name. The import sections are consulted to find
    * the proper package for the name. If the search in import sections fails
    * the method can ask the finder to search just by the given name.
    * @param className name to resolve. It can be either the full name
    *   or just the name without the package.
    * @param searchByName if true and the resolving through the import sections fails
    *   the finder is asked to find the class just by the given name
    */
    public CsmClass getClassFromName(String className, boolean searchByName) {
        refreshJavaImport();
        // XXX handle primitive type
        CsmClass ret = null;
//        CsmClass ret = JavaCompletion.getPrimitiveClass(className);
//        if (ret == null) {
//            
//            ret = getIncludeProc().getClassifier(className);
//        }
        if (ret == null && searchByName) {
            if (isUnknownInclude(className)) return null;    
            List clsList = getFinder().findClasses(null, className, true);
            if (clsList != null && clsList.size() > 0) {
                if (clsList.size() > 0 &&
                    (clsList.get(0) instanceof CsmClass)) { // more matching classes
                    ret = (CsmClass)clsList.get(0); // get the first one
                }
            }

        }
        return ret;
    }
    
    public synchronized CsmIncludeProcessor getIncludeProc(){
        if (javaImport == null) {
            javaImport = createIncludeProc();
        }
        javaImport.update(getDocument());
        return javaImport;
    }
    
    protected boolean isUnknownInclude(String className){
        return getIncludeProc().isUnknownImport(className);
    }
    
    /** Returns all imports that aren't in Code Completion DB yet */
    protected List getUnknownIncludes(){
        return getIncludeProc().getUnknownImports();
    }
    
    /** Returns true if the given class is in the import statement directly or
     *  indirectly (package.name.*) */    
    public boolean isIcluded(CsmClass cls){
        return getIncludeProc().isIncluded(cls);
    }

    public void refreshJavaImport() {
        if (javaImport != null) {
            javaImport.update(getDocument());
        }
    }

    protected void refreshClassInfo() {
    }

    protected List getImportedInnerClasses(){
        refreshJavaImport();
        return getIncludeProc().getInnerClasses();
    }
    
    /** Get the class that belongs to the given position */
    public CsmClass getClass(int pos) {
        return CompletionUtilities.findClassOnPosition(getDocument(), pos);
    }

    /** Get the class or function definition that belongs to the given position */
    public CsmOffsetableDeclaration getDefinition(int pos) {
        return CompletionUtilities.findFunDefinitionOrClassOnPosition(getDocument(), pos);
    }
    
    public boolean isStaticBlock(int pos) {
        return false;
    }
    
    public boolean isAnnotation(int pos) {
        try {
            BaseDocument document = getDocument();
            int off = Utilities.getFirstNonWhiteBwd(document, pos);
            char ch = '*'; // NOI18N
            while (off > -1 && (ch = document.getChars(off, 1)[0]) == '.') { // NOI18N
                off = Utilities.getFirstNonWhiteBwd(document, off);
                if (off > -1)
                    off = Utilities.getPreviousWord(document, off);
                if (off > -1)
                    off = Utilities.getFirstNonWhiteBwd(document, off);
            }
            if (off > -1 && ch == '@') // NOI18N
                return true;
        } catch (BadLocationException e) {}
        return false;
    }    

    public int[] getFunctionBlock(int[] identifierBlock) throws BadLocationException {
        int[] retValue = super.getFunctionBlock(identifierBlock);
        if (!isAnnotation(identifierBlock[0]))
            return retValue;
        return null;
    }
    
    protected DeclarationTokenProcessor createDeclarationTokenProcessor(
        String varName, int startPos, int endPos) {
        return java15
            ? (DeclarationTokenProcessor)new CsmDeclarationProcessor(this, varName)
            : (DeclarationTokenProcessor)new CsmDeclarationTokenProcessor(this, varName);
    }

    protected VariableMapTokenProcessor createVariableMapTokenProcessor(
        int startPos, int endPos) {
        return java15
            ? (VariableMapTokenProcessor)new CsmDeclarationProcessor(this, null)
            : (VariableMapTokenProcessor)new CsmDeclarationTokenProcessor(this, null);
    }
    
    /** Checks, whether caret is inside method */
    private boolean insideMethod(JTextComponent textComp, int startPos){
        try{
            int level = 0;
            BaseDocument doc = (BaseDocument)textComp.getDocument();
            for(int i = startPos-1; i>0; i--){
                char ch = doc.getChars(i, 1)[0];
                if (ch == ';') return false;
                if (ch == ')') level++;
                if (ch == '('){
                    if (level == 0){
                        return true;
                    }else{
                        level--;
                    }
                }
            }
            return false;
        } catch (BadLocationException e) {
            return false;
        }
    }

    /** Check and possibly popup, hide or refresh the completion */
    public int checkCompletion(JTextComponent target, String typedText, boolean visible ) {
        if (!visible) { // pane not visible yet
            int dotPos = target.getCaret().getDot();                            
            switch (typedText.charAt(0)) {
                case ' ':
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    
                    if (dotPos >= 2) { // last char before inserted space
                        int pos = Math.max(dotPos - 8, 0);
                        try {
                            String txtBeforeSpace = doc.getText(pos, dotPos - pos);
                            
                            if ( txtBeforeSpace.endsWith("import ") // NOI18N
                                && !Character.isJavaIdentifierPart(txtBeforeSpace.charAt(0))) {
                                return ExtSyntaxSupport.COMPLETION_POPUP;
                            }
                            
                            if (txtBeforeSpace.endsWith(", ")) { // NOI18N
                                // autoPopup completion only if caret is inside method
                                if (insideMethod(target, dotPos)) return ExtSyntaxSupport.COMPLETION_POPUP;
                            }
                        } catch (BadLocationException e) {
                        }
                    }
                    break;

                case '.':
                    return ExtSyntaxSupport.COMPLETION_POPUP;
                case ',':
                    // autoPopup completion only if caret is inside method
                    if (insideMethod(target, dotPos)) return ExtSyntaxSupport.COMPLETION_POPUP;
                default:
                    if (Character.isJavaIdentifierStart(typedText.charAt(0))) {
                        if (dotPos >= 5) { // last char before inserted space
                            try {
                                String maybeNew = target.getDocument().getText(dotPos - 5, 4);
                                if (maybeNew.equals("new ")){ // NOI18N
                                    return ExtSyntaxSupport.COMPLETION_POPUP;
                                }
                            } catch (BadLocationException e) {
                            }
                        }
                    }
                }
                return ExtSyntaxSupport.COMPLETION_CANCEL;
                
        } else { // the pane is already visible
            switch (typedText.charAt(0)) {
                case '=':
                case '{':
                case ';':
                    return ExtSyntaxSupport.COMPLETION_HIDE;
                default:
                    return ExtSyntaxSupport.COMPLETION_POST_REFRESH;
            }
        }
    }
    
    public boolean isAssignable(CsmType from, CsmType to) {
        CsmClassifier fromCls = from.getClassifier();
        CsmClassifier toCls = to.getClassifier();
        
        // XXX review!
        if (fromCls.equals(CsmCompletion.NULL_CLASS)) {
            return to.getArrayDepth() > 0 || !CsmCompletion.isPrimitiveClass(toCls);
        }
        
        if (toCls.equals(CsmCompletion.OBJECT_CLASS)) { // everything is object
            return (from.getArrayDepth() > to.getArrayDepth())
            || (from.getArrayDepth() == to.getArrayDepth()
            && !CsmCompletion.isPrimitiveClass(fromCls));
        }
        
        if (from.getArrayDepth() != to.getArrayDepth() ||
                from.getPointerDepth() != to.getPointerDepth()) {
            return false;
        }
        
        if (fromCls.equals(toCls)) {
            return true; // equal classes
        }
        
        // XXX
//        if (fromCls.isInterface()) {
//            return toCls.isInterface()
//            && (JCUtilities.getAllInterfaces(getFinder(), fromCls).indexOf(toCls) >= 0);
//        } else { // fromCls is a class
//            TokenID fromClsKwd = CCTokenContext.getKeyword(fromCls.getName());
//            if (fromClsKwd != null) { // primitive class
//                TokenID toClsKwd = CCTokenContext.getKeyword(toCls.getName());
//                return toClsKwd != null
//                && JCUtilities.getPrimitivesAssignable(fromClsKwd.getNumericID(), toClsKwd.getNumericID());
//            } else {
//                if (toCls.isInterface()) {
//                    return (JCUtilities.getAllInterfaces(getFinder(), fromCls).indexOf(toCls) >= 0);
//                } else { // toCls is a class
//                    return (JCUtilities.getSuperclasses(getFinder(), fromCls).indexOf(toCls) >= 0);
//                }
//            }
//        }
        return false;
    }
    
    public CsmType getCommonType(CsmType typ1, CsmType typ2) {
        if (typ1.equals(typ2)) {
            return typ1;
        }
        
        // The following part
        TokenID cls1Kwd = CCTokenContext.getKeyword(typ1.getClassifier().getName());
        TokenID cls2Kwd = CCTokenContext.getKeyword(typ2.getClassifier().getName());
        if (cls1Kwd == null && cls2Kwd == null) { // non-primitive classes
            if (isAssignable(typ1, typ2)) {
                return typ1;
            } else if (isAssignable(typ2, typ1)) {
                return typ2;
            } else {
                return null;
            }
        } else { // at least one primitive class
            if (typ1.getArrayDepth() != typ2.getArrayDepth()) {
                return null;
            }
            // XXX review
//            if (cls1Kwd != null && cls2Kwd != null) {
//                return JavaCompletion.getType(
//                JCUtilities.getPrimitivesCommonClass(cls1Kwd.getNumericID(), cls2Kwd.getNumericID()),
//                typ1.getArrayDepth());
//            } else { // one primitive but other not
//                return null;
//            }
            return null;
        }
    }
    
    /** Filter the list of the methods (usually returned from
     * Finder.findMethods()) or the list of the constructors
     * by the given parameter specification.
     * @param methodList list of the methods. They should have the same
     *   name but in fact they don't have to.
     * @param parmTypes parameter types specification. If set to null, no filtering
     *   is performed and the same list is returned. If a particular
     * @param acceptMoreParameters useful for code completion to get
     *   even the methods with more parameters.
     */
    public List filterMethods(List methodList, List parmTypeList,
    boolean acceptMoreParameters) {
        assert (methodList != null);
        if (parmTypeList == null) {
            return methodList;
        }
        
        List ret = new ArrayList();
        int parmTypeCnt = parmTypeList.size();
        int cnt = methodList.size();
        for (int i = 0; i < cnt; i++) {
            // Use constructor conversion to allow to use it too for the constructors
            CsmFunction m = (CsmFunction)methodList.get(i);
            CsmParameter[] methodParms = (CsmParameter[]) m.getParameters().toArray(new CsmParameter[0]);
            if (methodParms.length == parmTypeCnt
            || (acceptMoreParameters && methodParms.length >= parmTypeCnt)
            ) {
                boolean accept = true;
                boolean bestMatch = !acceptMoreParameters;
                for (int j = 0; accept && j < parmTypeCnt; j++) {
                    CsmType mpt = methodParms[j].getType();
                    CsmType t = (CsmType)parmTypeList.get(j);
                    if (t != null) {
                        if (!t.equals(mpt)) {
                            bestMatch = false;
                            if (!isAssignable(t, mpt)) {
                                accept = false;
                                break;
                            }
                        }
                    } else { // type in list is null
                        bestMatch = false;
                    }
                }
                
                if (accept) {
                    if (bestMatch) {
                        ret.clear();
                    }
                    ret.add(m);
                    if (bestMatch) {
                        break;
                    }
                }
                
            }
        }
        return ret;
    }

    /**
     * Interface that can be implemented by the values (in the key-value Map terminology)
     * of the variableMap provided by VariableMapTokenProcessor implementations.
     */
    public interface JavaVariable {
        
        /**
         * Get type expression of the variable declaration.
         * <br>
         * For example for "List<String> l;" it would be an expression formed
         * from "List<String>".
         *
         * @return type expression for this variable.
         */
        public CsmCompletionExpression getTypeExpression();
        
        /**
         * Get variable expression of the variable declaration.
         * <br>
         * Typically it is just the declaration variable but for arrays
         * it can include array depths - for example
         * for "int i[];" it would be an expression formed
         * from "i[]".
         *
         * @return type expression for this variable.
         */
        public CsmCompletionExpression getVariableExpression();

    }

    public static class CsmDeclarationTokenProcessor
        implements DeclarationTokenProcessor, VariableMapTokenProcessor {

        protected CsmSyntaxSupport sup;

        /** Position of the begining of the declaration to be returned */
        protected int decStartPos = -1;

        protected int decArrayDepth;

        /** Starting position of the declaration type */
        protected int typeStartPos;

        /** Position of the end of the type */
        protected int typeEndPos;

        /** Offset of the name of the variable */
        protected int decVarNameOffset;

        /** Length of the name of the variable */
        protected int decVarNameLen;

        /** Currently inside parenthesis, i.e. comma delimits declarations */
        protected int parenthesisCounter;

        /** Depth of the array when there is an array declaration */
        protected int arrayDepth;

        protected char[] buffer;

        protected int bufferStartPos;

        protected String varName;

        protected int state;

        /** Map filled with the [varName, type/classifier] pairs */
        protected HashMap varMap;


        /** Construct new token processor
        * @param varName it contains valid varName name or null to search
        *   for all variables and construct the variable map.
        */
        public CsmDeclarationTokenProcessor(CsmSyntaxSupport sup, String varName) {
            this.sup = sup;
            this.varName = varName;
            if (varName == null) {
                varMap = new HashMap();
            }
        }

        public int getDeclarationPosition() {
            return decStartPos;
        }

        public Map getVariableMap() {
            return varMap;
        }

        protected void processDeclaration() {
            // XXX review!
            if (varName == null) { // collect all variables
                String decType = new String(buffer, typeStartPos - bufferStartPos,
                                            typeEndPos - typeStartPos);
                if (decType.indexOf(' ') >= 0) {
                    decType = Analyzer.removeSpaces(decType);
                }
                String decVarName = new String(buffer, decVarNameOffset, decVarNameLen);
                
                // Maybe it's inner class. Stick an outerClass before it ...
                CsmClass innerClass = null;
                CsmClass outerCls = sup.getClass(decVarNameOffset);
                if (outerCls != null){
                    String outerClassName = outerCls.getQualifiedName();
                    CsmClassifier innerClassifier = sup.getFinder().getExactClassifier(outerClassName+CsmCompletion.SCOPE+decType);
                    innerClass = CsmKindUtilities.isClass(innerClassifier) ? (CsmClass)innerClassifier : null;
                    if (innerClass != null){
//                        varMap.put(decVarName, JavaCompletion.getType(innerClass, decArrayDepth));
                        varMap.put(decVarName, innerClass);
                    }
                }
                
                if (innerClass==null){
                    CsmClass cls = sup.getClassFromName(decType, true);
                    if (cls != null) {
//                        varMap.put(decVarName, JavaCompletion.getType(cls, decArrayDepth));
                        varMap.put(decVarName, cls);
                    }
                }

            } else {
                decStartPos = typeStartPos;
            }
        }

        public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
        int tokenOffset, int tokenLen) {
            int pos = bufferStartPos + tokenOffset;

	    // Check whether we are really recognizing the java tokens
	    if (!tokenContextPath.contains(CCTokenContext.contextPath)) {
		state = INIT;
		return true;
	    }

            switch (tokenID.getNumericID()) {
                case CCTokenContext.BOOLEAN_ID:
                case CCTokenContext.BYTE_ID:
                case CCTokenContext.CHAR_ID:
                case CCTokenContext.DOUBLE_ID:
                case CCTokenContext.FLOAT_ID:
                case CCTokenContext.INT_ID:
                case CCTokenContext.LONG_ID:
                case CCTokenContext.SHORT_ID:
                case CCTokenContext.VOID_ID:
                    typeStartPos = pos;
                    arrayDepth = 0;
                    typeEndPos = pos + tokenLen;
                    state = AFTER_TYPE;
                    break;

                case CCTokenContext.DOT_ID:
                    switch (state) {
                        case AFTER_TYPE: // allowed only inside type
                            state = AFTER_DOT;
                            typeEndPos = pos + tokenLen;
                            break;
                            
                        case AFTER_EQUAL:
                        case AFTER_VARIABLE:
                            break;

                        default:
                            state = INIT;
                            break;
                    }
                    break;

                case CCTokenContext.ARROW_ID:
                    switch (state) {
                        case AFTER_TYPE: // allowed only inside type
                            state = AFTER_ARROW;
                            typeEndPos = pos + tokenLen;
                            break;
                            
                        case AFTER_EQUAL:
                        case AFTER_VARIABLE:
                            break;

                        default:
                            state = INIT;
                            break;
                    }
                    break;

                case CCTokenContext.SCOPE_ID:
                    switch (state) {
                        case AFTER_TYPE: // allowed only inside type
                            state = AFTER_SCOPE;
                            typeEndPos = pos + tokenLen;
                            break;
                            
                        case AFTER_EQUAL:
                        case AFTER_VARIABLE:
                            break;

                        default:
                            state = INIT;
                            break;
                    }
                    break;                    
//XXX
//                case CCTokenContext.ELLIPSIS_ID:
//                    switch (state) {
//                        case AFTER_TYPE:
//                            arrayDepth++;
//                            break;
//
//                        default:
//                            state = INIT;
//                            break;
//                    }
//                    break;

                case CCTokenContext.LBRACKET_ID:
                    switch (state) {
                        case AFTER_TYPE:
                            state = AFTER_TYPE_LSB;
                            arrayDepth++;
                            break;

                        case AFTER_MATCHING_VARIABLE:
                            state = AFTER_MATCHING_VARIABLE_LSB;
                            decArrayDepth++;
                            break;

                        case AFTER_EQUAL:
                            break;
                            
                        default:
                            state = INIT;
                            break;
                    }
                    break;

                case CCTokenContext.RBRACKET_ID:
                    switch (state) {
                        case AFTER_TYPE_LSB:
                            state = AFTER_TYPE;
                            break;

                        case AFTER_MATCHING_VARIABLE_LSB:
                            state = AFTER_MATCHING_VARIABLE;
                            break;

                        case AFTER_EQUAL:
                            break;
                            
                        default:
                            state = INIT;
                            break;
                    }
                    break; // both in type and varName

                case CCTokenContext.LPAREN_ID:
                    parenthesisCounter++;
                    if (state != AFTER_EQUAL) {
                        state = INIT;
                    }
                    break;

                case CCTokenContext.RPAREN_ID:
                    if (state == AFTER_MATCHING_VARIABLE) {
                        processDeclaration();
                    }
                    if (parenthesisCounter > 0) {
                        parenthesisCounter--;
                    }
                    if (state != AFTER_EQUAL) {
                        state = INIT;
                    }
                    break;

                case CCTokenContext.LBRACE_ID:
                case CCTokenContext.RBRACE_ID:
                    if (parenthesisCounter > 0) {
                        parenthesisCounter--; // to tolerate opened parenthesis
                    }
                    state = INIT;
                    break;

                case CCTokenContext.COMMA_ID:
                    if (parenthesisCounter > 0) { // comma is declaration separator in parenthesis
                        if (parenthesisCounter == 1 && state == AFTER_MATCHING_VARIABLE) {
                            processDeclaration();
                        } 
                        if (state != AFTER_EQUAL) {
                            state = INIT;
                        }
                    } else { // not in parenthesis
                        switch (state) {
                            case AFTER_MATCHING_VARIABLE:
                                processDeclaration();
                                // let it flow to AFTER_VARIABLE
                            case AFTER_VARIABLE:
                            case AFTER_EQUAL:
                                state = AFTER_COMMA;
                                break;

                            default:
                                state = INIT;
                                break;
                        }
                    }
                    break;
                    

                case CCTokenContext.NEW_ID:
                    if (state != AFTER_EQUAL) {
                        state = INIT;
                    }
                    break;
                    
                case CCTokenContext.EQ_ID:
                    switch (state) {
                        case AFTER_MATCHING_VARIABLE:
                            processDeclaration();
                            // flow to AFTER_VARIABLE
                            
                        case AFTER_VARIABLE:
                            state = AFTER_EQUAL;
                            break;
                            
                        case AFTER_EQUAL:
                            break;
                            
                        default:
                            state = INIT;
                    }
                    break;

                case CCTokenContext.SEMICOLON_ID:
                    if (state == AFTER_MATCHING_VARIABLE) {
                        processDeclaration();
                    }
                    state = INIT;
                    break;

                case CCTokenContext.IDENTIFIER_ID:
                    switch (state) {
                        case AFTER_TYPE:
                        case AFTER_COMMA:
                            if (varName == null || Analyzer.equals(varName, buffer, tokenOffset, tokenLen)) {
                                decArrayDepth = arrayDepth;
                                decVarNameOffset = tokenOffset;
                                decVarNameLen = tokenLen;
                                state = AFTER_MATCHING_VARIABLE;
                            } else {
                                state = AFTER_VARIABLE;
                            }
                            break;

                        case AFTER_VARIABLE: // error
                            state = INIT;
                            break;
                            
                        case AFTER_EQUAL:
                            break;

                        case AFTER_DOT:
                            typeEndPos = pos + tokenLen;
                            state = AFTER_TYPE;
                            break;

                        case AFTER_ARROW:
                            typeEndPos = pos + tokenLen;
                            state = AFTER_VARIABLE;
                            break;

                        case AFTER_SCOPE: // only valid after type
                            typeEndPos = pos + tokenLen;
                            state = AFTER_TYPE;
                            break;
                            
                        case INIT:
                            typeStartPos = pos;
                            arrayDepth = 0;
                            typeEndPos = pos + tokenLen;
                            state = AFTER_TYPE;
                            break;

                        default:
                            state = INIT;
                            break;
                    }
                    break;

                case CCTokenContext.WHITESPACE_ID: // whitespace ignored
                    break;
                    
                case CCTokenContext.COLON_ID: // 1.5 enhanced for loop sysntax
                    processDeclaration();

//                case CCTokenContext.INSTANCEOF_ID:
                default:
                    state = INIT;
            }

            return true;
        }

        public int eot(int offset) {
            return 0;
        }

        public void nextBuffer(char[] buffer, int offset, int len,
                               int startPos, int preScan, boolean lastBuffer) {
            this.buffer = buffer;
            bufferStartPos = startPos - offset;
        }

    }

    ////////////////////////////////////////////////
    // overriden functions to resolve expressions
    /////////////////////////////////////////////////

    /** Map holding the [position, class-fields-map] pairs */
    private HashMap classFieldMaps = new HashMap();
    
    /** Map holding the [position, class-fields-map] pairs */
    private HashMap fileVariableMaps = new HashMap();
    
    /** Find the type of the variable. The default behavior is to first
    * search for the local variable declaration and then possibly for
    * the global declaration and if the declaration position is found
    * to get the first word on that position.
    * @return it returns Object to enable the custom implementations
    *   to return the appropriate instances.
    */
    public Object findType(String varName, int varPos) {
        CsmType type = null;
        Map varMap = getLocalVariableMap(varPos); // first try local vars
        if (varMap != null) {
            type = (CsmType) varMap.get(varName);
        }

        // then try class fields
        if (type == null) {
            varMap = getClassFieldMap(varPos); // try class fields
            if (varMap != null) {
                type = (CsmType) varMap.get(varName);
            }
        }       

        // then try file local vars
        if (type == null) {
            varMap = getFileVariableMap(varPos); // try file local vars
            if (varMap != null) {
                type = (CsmType) varMap.get(varName);
            }
        }
        
        // at the end - globals
        if (type == null) {
            varMap = getGlobalVariableMap(varPos); // try global vars
            if (varMap != null) {
                type = (CsmType) varMap.get(varName);
            }
        }

        return type;
    }    
    
    public Map getClassFieldMap(int offset) {
        Integer posI = new Integer(offset);
        Map varMap = (Map)classFieldMaps.get(posI);
        if (varMap == null) {
            varMap = buildClassFieldMap(offset);
            classFieldMaps.put(posI, varMap);
        }
        return varMap;
    }
    
    public Map getFileVariableMap(int offset) {
        Integer posI = new Integer(offset);
        Map varMap = (Map)fileVariableMaps.get(posI);
        if (varMap == null) {
            varMap = buildFileVariableMap(offset);
            fileVariableMaps.put(posI, varMap);
        }
        return varMap;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //                  build variable maps
    ///////////////////////////////////////////////////////////////////////////
    
    protected Map buildLocalVariableMap(int offset) {
        int methodStartPos = getMethodStartPosition(offset);
        if (methodStartPos >= 0 && methodStartPos < offset) {
            List res  = CompletionUtilities.findFunctionLocalVariables(getDocument(), offset);
            return list2Map(res);
        }
        return null;
    }
    
    protected Map buildGlobalVariableMap(int offset) {
        List res = CompletionUtilities.findGlobalVariables(getDocument(), offset);
        return list2Map(res);
    }    
 
    protected Map buildClassFieldMap(int offset) {
        List res = CompletionUtilities.findClassFields(getDocument(), offset);
        return list2Map(res);
    }   
    
    protected Map buildFileVariableMap(int offset) {
        List res = CompletionUtilities.findFileVariables(getDocument(), offset);
        return list2Map(res);
    }   
    
    // utitlies
    
    private Map/*<var-name, CsmType>*/ list2Map(List/*<CsmVariable>*/ vars) {
        if (vars == null || vars.size() == 0) {
            return null;
        }
        Map res = new StringMap();
        for (Iterator it = vars.iterator(); it.hasNext();) {
            Object elem = it.next();
            if (elem instanceof CsmVariable) {
                CsmVariable var = (CsmVariable) elem;
                res.put(var.getName(), var.getType());
            }
        }
        return res;
    }
}
