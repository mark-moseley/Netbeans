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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.util.ArrayList;
import org.netbeans.editor.EditorDebug;
import org.netbeans.editor.TokenID;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
* Expression generated by parsing text by java completion
*
* @author Miloslav Metelka
* @version 1.00
*/

public class CsmCompletionExpression {

    /** Invalid expression - this ID is used only internally */
    private static final int INVALID = -1;
    /** Constant - int/long/String/char etc. */
    public static final int CONSTANT = 0;
    /** Variable 'a' or 'a.b.c' */
    public static final int VARIABLE = 1;
    /** Operator '+' or '--' */
    public static final int OPERATOR = 2;
    /** Special value for unary operators */
    public static final int UNARY_OPERATOR = 3;
    /** Dot between method calls 'a().b()' or 'a().b.c.d(e, f)' */
    /** Dot and dereference between members 'member.*member' */
    public static final int DOT = 4;
    /** Dot between method calls and dot at the end 'a().b().' or 'a().b.c.d(e, f).' */
    /** Dot and dereference at the end 'member.*' */
    public static final int DOT_OPEN = 5;
    /** Opened array 'a[0' or 'a.b.c[d.e' */
    public static final int ARRAY_OPEN = 6;
    /** Array 'a[0]' or 'a.b.c[d.e]' */
    public static final int ARRAY = 7;
    /** Left opened parentheses */
    public static final int PARENTHESIS_OPEN = 8;
    /** Closed parenthesis holding the subexpression or conversion */
    public static final int PARENTHESIS = 9;
    /** Opened method 'a(' or 'a.b.c(d, e' */
    public static final int METHOD_OPEN = 10;
    /** Method closed by right parentheses 'a()' or 'a.b.c(d, e, f)' */
    public static final int METHOD = 11;
    /** Constructor closed by right parentheses 'new String()' or 'new String("hello")' */ // NOI18N
    public static final int CONSTRUCTOR = 12;
    /** Conversion '(int)a.b()' */
    public static final int CONVERSION = 13;
    /** Data type */
    public static final int TYPE = 14;
    /** 'new' keyword */
    public static final int NEW = 15;
    /** 'instanceof' operator */
    public static final int INSTANCEOF = 16;
    /**
     * Generic type in jdk 1.5.
     * <br>
     * It gets returned as expression with two or more parameters.
     * <br>
     * The first expression parameter is the type itself - VARIABLE
     * or DOT (or DOT_OPEN) expression.
     * <br>
     * The next expression parameters are type arguments (comma separated).
     * They are either VARIABLE, DOT, DOT_OPEN or GENERIC_TYPE.
     * <br>
     * The tokens added to the expression are
     * "&lt;" and zero or more "," and "&gt;".
     */
    // e.g.     List<String>
    // or       List<List<String>>
    // or       HashMap<Integer, String>
    public static final int GENERIC_TYPE = 17;
    /**
     * Unclosed generic type.
     * It's not closed by &gt;.
     */
    // e.g.     List<String
    // or       List<List<String>>
    // or       HashMap<Integer, String>
    public static final int GENERIC_TYPE_OPEN = 18;

    /**
     * '?' in generic type declarations
     * e.g. List<? extends Number>
     */
    public static final int GENERIC_WILD_CHAR = 19;

    /**
     * Annotation in jdk1.5.
     */
    public static final int ANNOTATION = 20;
    public static final int ANNOTATION_OPEN = 21;

    /** '#include' keyword */
    public static final int CPPINCLUDE = 22;

    /** '#include_next' keyword (a Sun Studio supported GNU extension) */
    public static final int CPPINCLUDE_NEXT = 23;

    /** 'case' keyword */
    public static final int CASE = 24;

    /** Arrow between method calls 'a()->b()' or 'a().b.c->d(e, f)' */
    /** arrow and dereference between members 'member->*member' */
    public static final int ARROW = 25;
    /** Arrow at the end 'a().b()->' or 'a().b.c.d(e, f)->' */
    /** arrow and dereference at the end 'member->*' */
    public static final int ARROW_OPEN = 26;

    /** Arrow between method calls 'NS::b()' or 'NS::CLASS::member' */
    public static final int SCOPE = 27;
    /** Arrow between method calls and dot at the end 'NS::' or 'NS::CLASS::' */
    public static final int SCOPE_OPEN = 28;  
    
    /** "const" as type prefix in the 'const A*'*/
    public static final int TYPE_PREFIX = 29;
    
    /** "const" as type postfix in the 'char* const'*/
    public static final int TYPE_POSTFIX = 30;
    
    /** "*" or "&" at type postfix in the 'char*' or 'int &'*/
    public static final int TYPE_REFERENCE = 31;
    
    /** dereference "*" or address-of "&" operators in the '*value' or '&value'*/
    public static final int MEMBER_POINTER = 32;
    
    /** dereference "*" or address-of "&" operators in the '((A)*' or '((A)&'*/
    public static final int MEMBER_POINTER_OPEN = 33;    

    /** Last used id of the expression ids. */
    private static final int LAST_ID = MEMBER_POINTER_OPEN;

    private static final int cppTokenIDsLength
        = CCTokenContext.context.getTokenIDs().length;

    /** Array that holds the precedence of the operator
    * and whether it's right associative or not.
    */
    private static final int[] OP = new int[cppTokenIDsLength + LAST_ID + 1];

    /** Is the operator right associative? */
    private static final int RIGHT_ASSOCIATIVE = 32;

    static {
        OP[CCTokenContext.EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.LT_ID] = 10;
        OP[CCTokenContext.GT_ID] = 10;
        OP[CCTokenContext.LSHIFT_ID] = 11;
        OP[CCTokenContext.RSSHIFT_ID] = 11;
//XXX        OP[CCTokenContext.RUSHIFT_ID] = 11;
        OP[CCTokenContext.PLUS_ID] = 12;
        OP[CCTokenContext.MINUS_ID] = 12;
        OP[CCTokenContext.MUL_ID] = 13;
        OP[CCTokenContext.DIV_ID] = 13;
        OP[CCTokenContext.AND_ID] = 8;
        OP[CCTokenContext.OR_ID] = 6;
        OP[CCTokenContext.XOR_ID] = 7;
        OP[CCTokenContext.MOD_ID] = 13;
        OP[CCTokenContext.NOT_ID] = 15;
        OP[CCTokenContext.NEG_ID] = 15;

        OP[CCTokenContext.EQ_EQ_ID] = 9;
        OP[CCTokenContext.LT_EQ_ID] = 10;
        OP[CCTokenContext.GT_EQ_ID] = 10;
        OP[CCTokenContext.LSHIFT_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.RSSHIFT_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
//XXX        OP[CCTokenContext.RUSHIFT_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.PLUS_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.MINUS_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.MUL_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.DIV_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.AND_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.OR_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.XOR_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.MOD_EQ_ID] = 2 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.NOT_EQ_ID] = 9;

        OP[CCTokenContext.DOT_ID] = 16;
        OP[CCTokenContext.DOTMBR_ID] = 14;
        OP[CCTokenContext.ARROW_ID] = 16;
        OP[CCTokenContext.ARROWMBR_ID] = 14;
        OP[CCTokenContext.SCOPE_ID] = 18;
        OP[CCTokenContext.COLON_ID] = 3 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.QUESTION_ID] = 3 | RIGHT_ASSOCIATIVE;
        OP[CCTokenContext.LBRACKET_ID] = 16;
        OP[CCTokenContext.RBRACKET_ID] = 0; // stop
        OP[CCTokenContext.PLUS_PLUS_ID] = 16;
        OP[CCTokenContext.MINUS_MINUS_ID] = 16;
        OP[CCTokenContext.AND_AND_ID] = 5;
        OP[CCTokenContext.OR_OR_ID] = 4;

        OP[CCTokenContext.COMMA_ID] = 0; // stop
        OP[CCTokenContext.SEMICOLON_ID] = 0; // not-recognized
        OP[CCTokenContext.LPAREN_ID] = 17;
        OP[CCTokenContext.RPAREN_ID] = 0; // not-recognized
        OP[CCTokenContext.LBRACE_ID] = 0; // not-recognized
        OP[CCTokenContext.RBRACE_ID] = 0; // not-recognized

        OP[cppTokenIDsLength + INVALID] = 0;
        OP[cppTokenIDsLength + CONSTANT] = 1;
        OP[cppTokenIDsLength + VARIABLE] = 1;
        OP[cppTokenIDsLength + UNARY_OPERATOR] = 15;
        OP[cppTokenIDsLength + DOT] = 1;
        OP[cppTokenIDsLength + DOT_OPEN] = 0; // stop
        OP[cppTokenIDsLength + ARROW] = 1;
        OP[cppTokenIDsLength + ARROW_OPEN] = 0; // stop
        OP[cppTokenIDsLength + SCOPE] = 17 ;
        OP[cppTokenIDsLength + SCOPE_OPEN] = 0; // stop
        OP[cppTokenIDsLength + ARRAY_OPEN] = 0; // stop
        OP[cppTokenIDsLength + ARRAY] = 1;
        OP[cppTokenIDsLength + PARENTHESIS_OPEN] = 0; // stop
        OP[cppTokenIDsLength + PARENTHESIS] = 1;
        OP[cppTokenIDsLength + METHOD_OPEN] = 0; // stop
        OP[cppTokenIDsLength + METHOD] = 1;
        OP[cppTokenIDsLength + CONSTRUCTOR] = 1;
        OP[cppTokenIDsLength + CONVERSION] = 15 | RIGHT_ASSOCIATIVE;
        OP[cppTokenIDsLength + TYPE] = 0; // stop
        OP[cppTokenIDsLength + NEW] = 0; // stop
        OP[cppTokenIDsLength + INSTANCEOF] = 10;
        OP[cppTokenIDsLength + CPPINCLUDE] = 0; // stop

        OP[cppTokenIDsLength + MEMBER_POINTER_OPEN] = 0; // stop
        OP[cppTokenIDsLength + MEMBER_POINTER] = 15 | RIGHT_ASSOCIATIVE; // as unary operators ?        
        OP[cppTokenIDsLength + TYPE_REFERENCE] = 1; // need to set correct value
        OP[cppTokenIDsLength + TYPE_POSTFIX] = 1; // need to set correct value
        OP[cppTokenIDsLength + TYPE_PREFIX] = 1; // need to set correct value
    }

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private CsmCompletionExpression parent;

    /** ID of the expression */
    private int expID;

    /** Result type name */
    private String type;

    /** Current token count * 3 */
    private int tokenCountM3;

    /** token info blocks containing tokenID
    * token's text and the position of the token in the document
    */
    private Object[] tokenBlocks = EMPTY_OBJECT_ARRAY;

    /** List of parameters */
    private ArrayList prmList;

    /**
     * Construct CsmCompletionExpression instance.
     *
     * @param expID valid expression id.
     */
    CsmCompletionExpression(int expID) {
        if (expID < 0 || expID > LAST_ID) {
            throw new IllegalArgumentException("expID=" + expID); // NOI18N
        }

        this.expID = expID;
    }
    
    /** Create empty variable. */
    static CsmCompletionExpression createEmptyVariable(int pos) {
        CsmCompletionExpression empty = new CsmCompletionExpression(VARIABLE);
        empty.addToken(CCTokenContext.IDENTIFIER, pos, "");
        return empty;
    }

    /** Return id of the operator or 'new' or 'instance' keywords
    * or -1 for the rest.
    */
    static int getOperatorID(TokenID tokenID) {
        int id = -1;

        if (tokenID.getCategory() == CCTokenContext.OPERATORS) {
            id = tokenID.getNumericID();

        } else {
            switch (tokenID.getNumericID()) {
                case CCTokenContext.NEW_ID:
                    id = cppTokenIDsLength + NEW;
                    break;

//                case CCTokenContext.INSTANCEOF_ID:
//                    id = javaTokenIDsLength + INSTANCEOF;
//                    break;

            }
        }

        return id;
    }

    static int getOperatorID(CsmCompletionExpression exp) {
        int expID = (exp != null) ? exp.getExpID() : INVALID;
        switch (expID) {
            case OPERATOR:
                return exp.getTokenID(0).getNumericID();
        }
        return cppTokenIDsLength + expID;
    }

    static int getOperatorPrecedence(int opID) {
        return OP[opID] & 31;
    }

    static boolean isOperatorRightAssociative(int opID) {
        return (OP[opID] & RIGHT_ASSOCIATIVE) != 0;
    }

    /** Is the expression a valid type. It can be either datatype
    * or array.
    */
    static boolean isValidType(CsmCompletionExpression exp) {
        switch (exp.getExpID()) {
        case ARRAY:
            if (exp.getParameterCount() == 1) {
                return isValidType(exp.getParameter(0));
            }
            return false;

        case TYPE_POSTFIX:
        case TYPE_PREFIX:
        case TYPE_REFERENCE:    
            if (exp.getParameterCount() >= 1) {
                return isValidType(exp.getParameter(0));
            }
            return false;
            
        case DOT:
        case ARROW:
        case SCOPE:
            int prmCnt = exp.getParameterCount();
            for (int i = 0; i < prmCnt; i++) {
                if (exp.getParameter(i).getExpID() != VARIABLE) {
                    return false;
                }
            }
            return true;

        case GENERIC_TYPE: // make no further analysis here
        case TYPE:
        case VARIABLE:
            return true;
        }

        return false;
    }


    /** Get expression ID */
    public int getExpID() {
        return expID;
    }

    /** Set expression ID */
    void setExpID(int expID) {
        this.expID = expID;
    }

    public CsmCompletionExpression getParent() {
        return parent;
    }

    void setParent(CsmCompletionExpression parent) {
        this.parent = parent;
    }

    public String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    public int getTokenCount() {
        return tokenCountM3 / 3;
    }

    public String getTokenText(int tokenInd) {
        tokenInd *= 3;
        return (String)tokenBlocks[tokenInd + 2];
    }

    public int getTokenOffset(int tokenInd) {
        tokenInd *= 3;
        return ((Integer)tokenBlocks[tokenInd + 1]).intValue();
    }

    public int getTokenLength(int tokenInd) {
        tokenInd *= 3;
        return ((String)tokenBlocks[tokenInd + 2]).length();
    }

    public TokenID getTokenID(int tokenInd) {
        tokenInd *= 3;
        return (TokenID)tokenBlocks[tokenInd];
    }

    void addToken(TokenID tokenID, int tokenOffset, String tokenText) {
        if (tokenCountM3 == tokenBlocks.length) {
            Object[] tmp = new Object[Math.max(3, tokenBlocks.length * 2)];
            if (tokenBlocks.length > 0) {
                System.arraycopy(tokenBlocks, 0, tmp, 0, tokenBlocks.length);
            }
            tokenBlocks = tmp;
        }

        tokenBlocks[tokenCountM3++] = tokenID;
        tokenBlocks[tokenCountM3++] = new Integer(tokenOffset);
        tokenBlocks[tokenCountM3++] = tokenText;
    }

    public int getParameterCount() {
        return (prmList != null) ? prmList.size() : 0;
    }

    public CsmCompletionExpression getParameter(int index) {
        return (CsmCompletionExpression)prmList.get(index);
    }

    void addParameter(CsmCompletionExpression prm) {
        if (prmList == null) {
            prmList = new ArrayList();
        }
        prm.setParent(this);
        prmList.add(prm);
    }

    void swapOperatorParms() {
        if ((expID == OPERATOR || expID == INSTANCEOF) && getParameterCount() == 2) {
            CsmCompletionExpression exp1 = (CsmCompletionExpression)prmList.remove(0);
            prmList.add(exp1);
            exp1.swapOperatorParms();
            ((CsmCompletionExpression)prmList.get(0)).swapOperatorParms();
        }
    }

    private static void appendSpaces(StringBuilder sb, int spaceCount) {
        while (--spaceCount >= 0) {
            sb.append(' '); //NOI18N
        }
    }

    static String getIDName(int expID) {
        switch (expID) {
        case CONSTANT:
            return "CONSTANT"; // NOI18N
        case VARIABLE:
            return "VARIABLE"; // NOI18N
        case OPERATOR:
            return "OPERATOR"; // NOI18N
        case UNARY_OPERATOR:
            return "UNARY_OPERATOR"; // NOI18N
        case DOT:
            return "DOT"; // NOI18N
        case DOT_OPEN:
            return "DOT_OPEN"; // NOI18N
        case ARROW:
            return "ARROW"; // NOI18N
        case ARROW_OPEN:
            return "ARROW_OPEN"; // NOI18N
        case SCOPE:
            return "SCOPE"; // NOI18N
        case SCOPE_OPEN:
            return "SCOPE_OPEN"; // NOI18N
        case ARRAY:
            return "ARRAY"; // NOI18N
        case ARRAY_OPEN:
            return "ARRAY_OPEN"; // NOI18N
        case PARENTHESIS_OPEN:
            return "PARENTHESIS_OPEN"; // NOI18N
        case PARENTHESIS:
            return "PARENTHESIS"; // NOI18N
        case METHOD_OPEN:
            return "METHOD_OPEN"; // NOI18N
        case METHOD:
            return "METHOD"; // NOI18N
        case CONSTRUCTOR:
            return "CONSTRUCTOR"; // NOI18N
        case CONVERSION:
            return "CONVERSION"; // NOI18N
        case TYPE:
            return "TYPE"; // NOI18N
        case NEW:
            return "NEW"; // NOI18N
        case INSTANCEOF:
            return "INSTANCEOF"; // NOI18N
        case GENERIC_TYPE:
            return "GENERIC_TYPE"; // NOI18N
        case GENERIC_TYPE_OPEN:
            return "GENERIC_TYPE_OPEN"; // NOI18N
        case GENERIC_WILD_CHAR:
            return "GENERIC_WILD_CHAR"; // NOI18N
        case ANNOTATION:
            return "ANNOTATION"; // NOI18N
        case ANNOTATION_OPEN:
            return "ANNOTATION_OPEN"; // NOI18N
        case CPPINCLUDE:
            return "INCLUDE"; // NOI18N
        case CASE:
            return "CASE"; // NOI18N
        case TYPE_PREFIX:
            return "TYPE_PREFIX"; // NOI18N
        case TYPE_POSTFIX:
            return "TYPE_POSTFIX"; // NOI18N
        case TYPE_REFERENCE:
            return "TYPE_REFERENCE"; // NOI18N
        case MEMBER_POINTER:
            return "MEMBER_POINTER"; // NOI18N            
        case MEMBER_POINTER_OPEN:
            return "MEMBER_POINTER_OPEN"; // NOI18N  
        default:
            return "Unknown expID " + expID; // NOI18N
        }
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        appendSpaces(sb, indent);
        sb.append("expID=" + getIDName(expID)); // NOI18N

        if (type != null) {
            sb.append(", result type="); // NOI18N
            sb.append(type);
        }

        // Debug tokens
        int tokenCnt = getTokenCount();
        sb.append(", token count="); // NOI18N
        sb.append(tokenCnt);
        if (tokenCnt > 0) {
            for (int i = 0; i < tokenCountM3;) {
                TokenID tokenID = (TokenID)tokenBlocks[i++];
                int tokenOffset = ((Integer)tokenBlocks[i++]).intValue();
                String tokenText = (String)tokenBlocks[i++];
                sb.append(", token" + (i / 3 - 1) + "='" + EditorDebug.debugString(tokenText) + "'"); // NOI18N
            }
        }

        // Debug parameters
        int parmCnt = getParameterCount();
        sb.append(", parm count="); // NOI18N
        sb.append(parmCnt);
        if (parmCnt > 0) {
            for (int i = 0; i < parmCnt; i++) {
                sb.append('\n'); //NOI18N
                appendSpaces(sb, indent + 4);
                sb.append("parm" + i + "=[" + getParameter(i).toString(indent + 4) + "]"); // NOI18N
            }
        }
        return sb.toString();
    }

    public String toString() {
        return toString(0);
    }
}
