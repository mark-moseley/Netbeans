package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 *
 * $Id$
 */

import antlr.collections.AST;

public interface ASTVisitor {
    public void visit(AST node);
}
