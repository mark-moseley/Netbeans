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
package org.netbeans.modules.java.source.pretty;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeInfo;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** Estimate the printed width of a tree
 */
public class WidthEstimator extends JCTree.Visitor {
    private int width;
    private int prec;
    private int maxwidth;
    private final Symtab symbols;
    private final TreeInfo treeinfo;

    public WidthEstimator(Context context) {
	symbols = Symtab.instance(context);
	treeinfo = TreeInfo.instance(context);
    }

    public int estimateWidth(JCTree t, int maxwidth) {
	width = 0;
	this.maxwidth = maxwidth;
	t.accept(this);
	return width;
    }
    public int estimateWidth(JCTree t) {
	return estimateWidth(t,100);
    }
    public int estimateWidth(List<? extends JCTree> t, int maxwidth) {
	width = 0;
	this.maxwidth = maxwidth;
        while(t.nonEmpty() && this.width < this.maxwidth) {
	    t.head.accept(this);
            t = t.tail;
        }
	return width;
    }
    private void open(int contextPrec, int ownPrec) {
	if (ownPrec < contextPrec)
	    width += 2;
    }
    private void width(Name n) { width += n.getByteLength(); }
    private void width(String n) { width += n.length(); }
    private void width(JCTree n) { if(width<maxwidth) n.accept(this); }
    private void width(JCTree n, Type t) { if(t==null) width(n); else width(t); }
    private void width(Type ty) {
	    while(ty instanceof Type.ArrayType) {
		ty = ((Type.ArrayType)ty).elemtype;
		width+=2;
	    }
	    widthQ(ty.tsym);
	    if (ty instanceof Type.ClassType) {
		List < Type > typarams = ((Type.ClassType) ty).typarams_field;
		if (typarams != null && typarams.nonEmpty()) {
		    width++;
		    for (; typarams.nonEmpty(); typarams = typarams.tail) {
			width++;
			width(typarams.head);
		    }
		}
	    }
    }
    public void widthQ(Symbol t) {
	if (t.owner != null && t.owner != symbols.rootPackage && t.owner != symbols.unnamedPackage
	        && !(t.type instanceof Type.TypeVar)
		&& !(t.owner instanceof MethodSymbol)) {
	    width++;
	    widthQ(t.owner);
	}
	width(t.name);
    }
    private void width(List<? extends JCTree> n, int pad) {
	int nadd = 0;
	while(!n.isEmpty() && width<maxwidth) {
	    width(n.head);
	    n = n.tail;
	    nadd++;
	}
	if(nadd>1) width += pad*nadd;
    }
    private void width(List<? extends JCTree> n) {
	width(n, 2);
    }
    private void width(JCTree tree, int prec) {
	if (tree != null) {
	    int prevPrec = this.prec;
	    this.prec = prec;
	    tree.accept(this);
	    this.prec = prevPrec;
	}
    }
    public void visitTree(JCTree tree) {
System.err.println("Need width calc for "+tree);
	width = maxwidth;
    }
    public void visitParens(JCParens tree) {
	width+=2;
	width(tree.expr);
    }
    public void visitApply(JCMethodInvocation tree) {
	width+=2;
	width(tree.meth, TreeInfo.postfixPrec);
	width(tree.args);
    }
    public void visitNewClass(JCNewClass tree) {
	if (tree.encl != null) {
	    width(tree.encl);
	    width++;
	}
	width+=4;
	if (tree.encl == null)
	    width(tree.clazz, tree.clazz.type);
	else if (tree.clazz.type != null)
	    width(tree.clazz.type.tsym.name);
	else
	    width(tree.clazz);
	width+=2;
	width(tree.args, 2);
	if (tree.def != null) {
	    width+=4;
	    width(((JCClassDecl) tree.def).defs, 2);
	}
    }
    public void visitNewArray(JCNewArray tree) {
	if (tree.elemtype != null) {
	    width+=4;
	    JCTree elemtype = tree.elemtype;
	    while (elemtype.getTag() == JCTree.TYPEARRAY) {
		width+=2;
		elemtype = ((JCArrayTypeTree) elemtype).elemtype;
	    }
	    width(elemtype);
	    for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
		width+=2;
		width(l.head);
	    }
	}
	if (tree.elems != null) {
	    width+=4;
	    width(tree.elems);
	}
    }
    private void widthAnnotations(List<JCAnnotation> anns) {
	int nadd = 0;
	while(!anns.isEmpty() && width<maxwidth) {
            width++; // '@'
	    width(anns.head);
	    anns = anns.tail;
	    nadd++;
	}
	if(nadd>1) width += nadd;
        
    }
    private void widthFlags(long flags) {
	if ((flags & SYNTHETIC) != 0)
	    width+=14;
	width+=treeinfo.flagNames(flags).length();
	if ((flags & StandardFlags) != 0)
	    width++;
    }
    public void visitVarDef(JCVariableDecl tree) {
        widthAnnotations(tree.mods.annotations);
        if ((tree.mods.flags & Flags.ENUM) == 0) {
            widthFlags(tree.mods.flags);
            width(tree.vartype, tree.type);
            width++;
        }
        width(tree.name);
        if (tree.init != null && (tree.mods.flags & Flags.ENUM) == 0) {
            width+=3;
            width(tree.init);
        }
    }
    public void visitConditional(JCConditional tree) {
	open(prec, TreeInfo.condPrec);
	width+=6;
	width(tree.cond, TreeInfo.condPrec-1);
	width(tree.truepart, TreeInfo.condPrec);
	width(tree.falsepart, TreeInfo.condPrec);
    }

    public void visitAssignop(JCAssignOp tree) {
	open(prec, TreeInfo.assignopPrec);
	width+=3;
	width(treeinfo.operatorName(tree.getTag() - JCTree.ASGOffset));
	width(tree.lhs, TreeInfo.assignopPrec + 1);
	width(tree.rhs, TreeInfo.assignopPrec);
    }
    public void visitAssign(JCAssign tree) {
	open(prec, TreeInfo.assignPrec);
	width+=3;
	width(tree.lhs, TreeInfo.assignPrec + 1);
	width(tree.rhs, TreeInfo.assignPrec);
    }
    public void visitUnary(JCUnary tree) {
	int ownprec = treeinfo.opPrec(tree.getTag());
	Name opname = treeinfo.operatorName(tree.getTag());
	open(prec, ownprec);
	width(opname);
	width(tree.arg, ownprec);
    }
    public void visitBinary(JCBinary tree) {
	int ownprec = treeinfo.opPrec(tree.getTag());
	Name opname = treeinfo.operatorName(tree.getTag());
	open(prec, ownprec);
	width(opname);
	width+=2;
	width(tree.lhs, ownprec);
	width(tree.rhs, ownprec + 1);
    }
    public void visitTypeCast(JCTypeCast tree) {
	width+=2;
	open(prec, TreeInfo.prefixPrec);
	width(tree.clazz, tree.clazz.type);
	width(tree.expr, TreeInfo.prefixPrec);
    }

    public void visitTypeTest(JCInstanceOf tree) {
	open(prec, TreeInfo.ordPrec);
	width += 12;
	width(tree.expr, TreeInfo.ordPrec);
	width(tree.clazz, tree.clazz.type);
    }

    public void visitIndexed(JCArrayAccess tree) {
	width+=2;
	width(tree.indexed, TreeInfo.postfixPrec);
	width(tree.index);
    }

    public void visitSelect(JCFieldAccess tree) {
	if (tree.sym instanceof Symbol.ClassSymbol && tree.type != null) {
	    width(tree.type);
	} else {
	    width+=1;
	    width(tree.selected, TreeInfo.postfixPrec);
	    width(tree.name);
	}
    }

    public void visitIdent(JCIdent tree) {
	if (tree.sym instanceof Symbol.ClassSymbol)
	    width(tree.type);
	else width(tree.name);
    }

    public void visitLiteral(JCLiteral tree) {
	switch (tree.typetag) {
	  case LONG:
	  case FLOAT:
	    width++;
	    width(tree.value.toString());
	    break;
	  case CHAR:
	    width += 3;
	    break;
	  case CLASS:
	    width+=2;
	    width(tree.value.toString());
	    break;
          case BOOLEAN:
            width(((Number)tree.value).intValue() == 1 ? "true" : "false");
            break;
          case BOT:
            width("null");
            break;
	  default:
	    width(tree.value.toString());
	}
    }

    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
	width(symbols.typeOfTag[tree.typetag].tsym.name);
    }

    public void visitTypeArray(JCArrayTypeTree tree) {
	width(tree.elemtype);
	width+=2;
    }

}
