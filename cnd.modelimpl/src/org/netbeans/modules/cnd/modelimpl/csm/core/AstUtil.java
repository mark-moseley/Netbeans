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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import antlr.ASTVisitor;
import antlr.collections.AST;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.cache.impl.CacheUtil;

/**
 * Miscellaneous AST-related static utility functions
 * @author Vladimir Kvasihn
 */
public class AstUtil {

    public static boolean isEmpty(AST ast, boolean hasFakeChild) {
	if( isEmpty(ast) ) {
	    return true;
	}
	else {
	    return hasFakeChild ? isEmpty(ast.getFirstChild()) : false;
	}
    }

    private static boolean isEmpty(AST ast) {
	return (ast == null || ast.getType() == CPPTokenTypes.EOF);
    }
    
    public static String[] getRawNameInChildren(AST ast) {
        return getRawName(findIdToken(ast));
    }

    public static String[] getRawName(AST token) {
        List/*<String>*/ l = new ArrayList/*<String>*/();
        for( ; token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.ID:
                    l.add(token.getText());
                    break;
                case CPPTokenTypes.SCOPE:
                    break;
                default:
                    //TODO: process templates
                    break;
            }
        }
        return (String[]) l.toArray(new String[l.size()]);
    }
    
    private static AST findIdToken(AST ast) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.ID ) {
                return token;
            }
            else if( token.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                return token.getFirstChild();
            }
        }
        return null;
    }
    
    public static String findId(AST ast) {
        return findId(ast, -1);
    }
    
    /**
     * Finds ID (either CPPTokenTypes.CSM_QUALIFIED_ID or CPPTokenTypes.ID)
     * in direct children of the given AST tree
     *
     * @param ast tree to secarch ID in 
     *
     * @param limitingTokenType type of token that, if being found, stops search
     *        -1 means that there is no such token.
     *        This parameter allows, for example, searching until "}" is encountered
     */
    public static String findId(AST ast, int limitingTokenType) {
	return findId(ast, limitingTokenType, false);
    }
    
    /**
     * Finds ID (either CPPTokenTypes.CSM_QUALIFIED_ID or CPPTokenTypes.ID)
     * in direct children of the given AST tree
     *
     * @param ast tree to secarch ID in 
     *
     * @param limitingTokenType type of token that, if being found, stops search
     *        -1 means that there is no such token.
     *        This parameter allows, for example, searching until "}" is encountered
     */
    public static String findId(AST ast, int limitingTokenType, boolean qualified) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            int type = token.getType();
            if( type == limitingTokenType && limitingTokenType >= 0 ) {
                return null;
            }
            else if( type == CPPTokenTypes.ID ) {
                return token.getText();
            }
            else if( type == CPPTokenTypes.CSM_QUALIFIED_ID ) {
		if( qualified ) {
		    return token.getText();
		}
                AST last = getLastChild(token);
                if( last != null) {
                    if( last.getType() == CPPTokenTypes.ID ) {
                        return last.getText();
                    }
                    else {
                        AST first = token.getFirstChild();
                        if( first.getType() == CPPTokenTypes.LITERAL_OPERATOR ) {
                            StringBuilder sb = new StringBuilder(first.getText());
                            sb.append(' ');
                            AST next = first.getNextSibling();
                            if( next != null ) {
                                sb.append(next.getText());
                            }
                            return sb.toString();
                        } else if (first.getType() == CPPTokenTypes.ID){
                            return first.getText();
                        }
                    }
                }                
            }
        }
        return "";
    }
  
    public static AST findMethodName(AST ast){
        AST type = ast.getFirstChild(); // type
        AST qn = null;
        int i = 0;
        while(type != null){
            switch(type.getType()){
                case CPPTokenTypes.LESSTHAN:
                    i++;
                    type = type.getNextSibling();
                    continue;
                case CPPTokenTypes.GREATERTHAN:
                    i--;
                    type = type.getNextSibling();
                    continue;
                case CPPTokenTypes.CSM_TYPE_BUILTIN:
                case CPPTokenTypes.CSM_TYPE_COMPOUND:
                    type = type.getNextSibling();
                    if (i == 0){
                        qn = type;
                    }
                    continue;
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                    if (i == 0){
                        qn = type;
                    }
                    type = type.getNextSibling();
                    continue;
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY:
                case CPPTokenTypes.COLON:
                    break;
                default:
                    type = type.getNextSibling();
                    continue;
            }
            break;
        }
        return qn;
    }

    public static boolean hasChildOfType(AST ast, int type) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == type ) {
                return true;
            }
        }
        return false;
    }
    
    public static AST findChildOfType(AST ast, int type) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == type ) {
                return token;
            }
        }
        return null;
    }
    
    public static AST findSiblingOfType(AST ast, int type) {
        for( AST token = ast; token != null; token = token.getNextSibling() ) {
            if( token.getType() == type ) {
                return token;
            }
        }
        return null;
    }
    
    public static AST getLastChild(AST token) {
        if( token == null ) {
            return null;
        }
        AST child = token.getFirstChild();
        if( child != null ) {
            while( child.getNextSibling() != null ) {
                child = child.getNextSibling();
            }
            return child;
        }
        return null;
    }
    
    public static AST getLastChildRecursively(AST token) {
        if( token == null ) {
            return null;
        }
        if( token.getFirstChild() == null ) {
            return token;
        }
        else {
            AST child = getLastChild(token);
            return getLastChildRecursively(child);
        }
    }
    
    public static void toStream(AST ast, final PrintStream ps) {
        ASTVisitor impl = new ASTVisitor() {
            public void visit(AST node) {
		print(node, ps);
                for( AST node2 = node; node2 != null; node2 = node2.getNextSibling() ) {
                    if (node2.getFirstChild() != null) {
			ps.print('>');
                        visit(node2.getFirstChild());
			ps.print('<');
                    }
                }
            }
        };
        impl.visit(ast);
    }    
    
    private static void print(AST ast, PrintStream ps) {
        ps.print('[');
        ps.print(ast.getText());
        ps.print('(');
        ps.print(ast.getType());
        ps.print(')');
        ps.print(ast.getLine());
        ps.print(':');
        ps.print(ast.getColumn());
        ps.print(']');
        //ps.print('\n');
    }
    
    private static int fileIndex = 0;
    public static AST testASTSerialization(FileBuffer buffer, AST ast) {
        AST astRead = null;
        File file = buffer.getFile();
        // testing caching ast
        String prefix = "cnd_modelimpl_"+(fileIndex++); // NOI18N
        String suffix = file.getName();
        try {
            File out = File.createTempFile(prefix, suffix);                
            if (false) System.err.println("...saving AST of file " + file.getAbsolutePath() + " into tmp file " + out); // NOI18N
            long astTime = System.currentTimeMillis();
            // write
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(out), TraceFlags.BUF_SIZE));
            try {
                CacheUtil.writeAST(oos, ast);
            } finally {
                oos.close();
            }
            long writeTime = System.currentTimeMillis() - astTime;
            if (false) System.err.println("saved AST of file " + file.getAbsolutePath() + " withing " + writeTime + "ms"); // NOI18N
            astTime = System.currentTimeMillis();
            // read
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(out), TraceFlags.BUF_SIZE));
            try {
                astRead = CacheUtil.readAST(ois);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } finally {
                ois.close();                
            }
            long readTime = System.currentTimeMillis() - astTime;
            if (false) System.err.println("read AST of file " + file.getAbsolutePath() + " withing " + readTime + "ms"); // NOI18N
            out.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return astRead;
    }
}
 
