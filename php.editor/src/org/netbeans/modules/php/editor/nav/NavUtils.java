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

package org.netbeans.modules.php.editor.nav;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class NavUtils {

    public static List<ASTNode> underCaret(CompilationInfo info, final int offset) {
        class Result extends Error {
            private Stack<ASTNode> result;
            public Result(Stack<ASTNode> result) {
                this.result = result;
            }
            @Override
            public Throwable fillInStackTrace() {
                return this;
            }
        }
        try {
            new DefaultVisitor() {
                private Stack<ASTNode> s = new Stack<ASTNode>();
                @Override
                public void scan(ASTNode node) {
                    if (node == null) {
                        return ;
                    }
                    
                    if (node.getStartOffset() <= offset && offset <= node.getEndOffset()) {
                        s.push(node);
                        super.scan(node);
                        throw new Result(s);
                    }
                }
            }.scan(Utils.getRoot(info));
        } catch (Result r) {
            return new LinkedList<ASTNode>(r.result);
        }
        
        return Collections.emptyList();
    }
    
    public static AttributedElement findElement(CompilationInfo info, List<ASTNode> path, int offset, SemiAttribute a) {
        if (path.size() == 0) {
            return null;
        }

        path = new LinkedList<ASTNode>(path);

        Collections.reverse(path);

        AttributedElement result = null;
        ASTNode previous = null;

        for (final ASTNode leaf : path) {
            if (leaf instanceof Variable && !(leaf instanceof ArrayAccess)) {
                result = a.getElement(leaf);
                previous = leaf;
                continue;
            }

            if (leaf instanceof ArrayAccess && result == null) {
                return a.getElement(leaf);
            }
            
            if (leaf instanceof FunctionInvocation) {
                FunctionInvocation i = (FunctionInvocation) leaf;

                if (i.getFunctionName().getStartOffset() <= offset && offset <= i.getFunctionName().getEndOffset()) {
                    return a.getElement(leaf);
                }
            }

            if (leaf instanceof ClassInstanceCreation) {
                return a.getElement(leaf);
            }
            
            if (leaf instanceof Scalar) {
                AttributedElement e = a.getElement(leaf);
                
                if (e != null) {
                    return e;
                }
            }
            
            if (leaf instanceof FunctionDeclaration && ((FunctionDeclaration) leaf).getFunctionName() == previous) {
                return a.getElement(leaf);
            }

            if (leaf instanceof ClassDeclaration && ((ClassDeclaration) leaf).getName() == previous) {
                return a.getElement(leaf);
            }
            
            if (result != null) {
                return result;
            }
            
            previous = leaf;
        }

        return null;
    }
    
    public static boolean isQuoted(String value) {
        return value.length() >= 2 &&
               (value.startsWith("\"") || value.startsWith("'")) &&
               (value.endsWith("\"") || value.endsWith("'"));
    }
    
    public static String dequote(String value) {
        assert isQuoted(value);
        
        return value.substring(1, value.length() - 1);
    }
    
    public static FileObject resolveInclude(CompilationInfo info, Include include) {
        Expression e = include.getExpression();

        if (e instanceof ParenthesisExpression) {
            e = ((ParenthesisExpression) e).getExpression();
        }

        if (e instanceof Scalar) {
            Scalar s = (Scalar) e;

            if (Type.STRING == s.getScalarType()) {
                String fileName = s.getStringValue();
                fileName = fileName.length() >= 2 ? fileName.substring(1, fileName.length() - 1) : fileName;//TODO: not nice

                return info.getFileObject().getParent().getFileObject(fileName);
            }
        }
        
        return null;
    }
    
    public static FileObject getFile(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);
        
        if (o instanceof DataObject) {
            DataObject od = (DataObject) o;
            
            return od.getPrimaryFile();
        }
        
        return null;
    }
    
}
