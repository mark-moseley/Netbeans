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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.insync.java;

import com.sun.rave.designtime.ContextMethod;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.BeanStructureScanner;
import org.netbeans.modules.visualweb.insync.beans.EventSet;
import org.netbeans.modules.visualweb.insync.beans.Property;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;

/**
 *
 * @author jdeva
 */
public class Method {
    final static String CTOR = "<init>";
    protected ElementHandle<ExecutableElement> execElementHandle;    
    protected JavaClass javaClass;    //Enclosing java class
    protected String name;
    
    public Method(ExecutableElement element, JavaClass javaClass) {
        execElementHandle = ElementHandle.create(element);        
        this.javaClass = javaClass;
        this.name = element.getSimpleName().toString();
    }
    
    /*
     *  Returns enclosing java class
     */ 
    public JavaClass getJavaClass() {
        return javaClass;
    }
    
    /*
     *  Returns enclosing java class
     */ 
    public String getName() {
        return name;
    }    
    
    public ElementHandle<ExecutableElement> getElementHandle() {
        return execElementHandle;
    }
    
    /*
     * Looks for a expression statement of the form a.b(arg1, ..); where a and b are the passed
     * in bName and mName respectively. Returns null if no such statement is found
     */     
    public Statement findPropertyStatement(final String beanName, final String methodName) {
        return (Statement)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                StatementTree stmtTree = findPropertyStatement(cinfo, beanName, methodName);
                if(stmtTree != null) {
                    return new Statement(TreePathHandle.create(TreeUtils.getTreePath(cinfo, stmtTree), cinfo), 
                            Method.this, beanName, methodName);
                }
                return null;
            }
        }, javaClass.getFileObject());    
    }
    
    /*
     * Looks for a expression statement of the form a.b(arg1, ..); where a and b are the passed
     * in bName and mName respectively. Returns null if no such statement is found
     */     
    StatementTree findPropertyStatement(CompilationInfo cinfo, String beanName, String methodName) {
        ExecutableElement execElement = execElementHandle.resolve(cinfo);
        BlockTree block = cinfo.getTrees().getTree(execElement).getBody();
        for(StatementTree statement : block.getStatements()) {
            if(statement.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree exprStatTree = (ExpressionStatementTree)statement;
                if(exprStatTree.getExpression().getKind() == Tree.Kind.METHOD_INVOCATION) {
                    MethodInvocationTree methInvkTree = (MethodInvocationTree)exprStatTree.getExpression();
                    if(methInvkTree.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                        MemberSelectTree memSelTree = (MemberSelectTree)methInvkTree.getMethodSelect();
                        ExpressionTree exprTree = memSelTree.getExpression();
                        if(exprTree.getKind() == Tree.Kind.IDENTIFIER &&
                                memSelTree.getIdentifier().toString().equals(methodName) &&
                                ((IdentifierTree)exprTree).getName().toString().equals(beanName)) {
                            return statement;
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
     * Adds a expression statement of the form a.b(arg1, ..);
     */ 
    private StatementTree addMethodInvocationStatement(WorkingCopy wc, MethodTree methodTree,
            String beanName, String methodName, List<ExpressionTree> args) {
         return addMethodInvocationStatement(wc, methodTree, 
                 TreeMakerUtils.createMethodInvocation(wc, beanName, methodName, args));
    }
    
    
    /*
     * Adds a expression statement of the form a.setFoo(new X(){});
     * 
     */ 
    public Statement addEventSetStatement(final String beanName, final String methodName, 
            final String adapterClassName) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                ExecutableElement elem = execElementHandle.resolve(wc);
                ArrayList<ExpressionTree> args = new ArrayList<ExpressionTree>();
                args.add(TreeMakerUtils.createNewClassExpression(wc, adapterClassName));
                addMethodInvocationStatement(wc, wc.getTrees().getTree(elem),
                        TreeMakerUtils.createMethodInvocation(wc, beanName, methodName, args));
                return null;
            }
        }, javaClass.getFileObject());
        return findPropertyStatement(beanName, methodName);
    }
    
    /*
     * Adds a expression statement of the form a.setFoo(arg);
     * 
     */ 
    public Statement addPropertyStatement(final String beanName, final String methodName, 
            final String valueSource) {
       WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                ExecutableElement elem = execElementHandle.resolve(wc);
                ArrayList<ExpressionTree> args = new ArrayList<ExpressionTree>();
                if(valueSource != null) {
                    SourcePositions[] positions = new SourcePositions[1];
                    args.add(wc.getTreeUtilities().parseExpression(valueSource, positions));
                }
                addMethodInvocationStatement(wc, wc.getTrees().getTree(elem),
                        TreeMakerUtils.createMethodInvocation(wc, beanName, methodName, args));
                return null;
            }
        }, javaClass.getFileObject());
        return findPropertyStatement(beanName, methodName);
    }
    
    public void addCleanupStatements(WorkingCopy wc, List<Bean> beans) {
        ExecutableElement elem = execElementHandle.resolve(wc);
        BlockTree oldBlockTree = wc.getTrees().getTree(elem).getBody();
        BlockTree newBlockTree = oldBlockTree;
        for (Bean bean : beans) {
            if (bean.getCleanupMethod() != null) {
                newBlockTree = addStatement(wc, newBlockTree, bean.getName(), bean.getCleanupMethod(), null);
            }
        }
        wc.rewrite(oldBlockTree, newBlockTree);
    }

    public void removeCleanupStatements(WorkingCopy wc, List<Bean> beans) {
        ExecutableElement elem = execElementHandle.resolve(wc);
        BlockTree oldBlockTree = wc.getTrees().getTree(elem).getBody();
        BlockTree newBlockTree = oldBlockTree;
        for (Bean bean : beans) {
            if (bean.getCleanupMethod() != null) {
                newBlockTree = removeStatement(wc, newBlockTree, bean.getName(), bean.getCleanupMethod());
            }
        }
        wc.rewrite(oldBlockTree, newBlockTree);
    }
    
    public void addPropertySetStatements(final Bean bean) {
        final List<Property> props = new ArrayList<Property>();
        for (Property prop : bean.getProperties()) {
            if (!prop.isMarkupProperty() && !prop.isInserted()) {
                props.add(prop);
            }
        }
        if(props.size() > 0) {
            WriteTaskWrapper.execute(new WriteTaskWrapper.Write() {
                public Object run(WorkingCopy wc) {
                    addPropertySetStatements(wc, bean, props);
                    return null;
                }
            }, javaClass.getFileObject());
        }
    }

    private void addPropertySetStatements(WorkingCopy wc, Bean bean, List<Property> props) {
        ExecutableElement elem = execElementHandle.resolve(wc);
        BlockTree oldBlockTree = wc.getTrees().getTree(elem).getBody();
        BlockTree newBlockTree = oldBlockTree;
        for (Property prop : props) {
            newBlockTree = addStatement(wc, newBlockTree, bean.getName(), prop.getWriteMethodName(), prop.getValueSource());
            prop.setInserted(true);
        }
        wc.rewrite(oldBlockTree, newBlockTree);
    }

    BlockTree addPropertySetStatements(WorkingCopy wc, Bean bean, BlockTree oldBlockTree) {
        if(oldBlockTree == null) {
            ExecutableElement elem = execElementHandle.resolve(wc);
            oldBlockTree = wc.getTrees().getTree(elem).getBody();
        }
        BlockTree newBlockTree = oldBlockTree;
        for (Property prop : bean.getProperties()) {
            if (!prop.isMarkupProperty() && !prop.isInserted()) {
                newBlockTree = addStatement(wc, newBlockTree, bean.getName(), prop.getWriteMethodName(), prop.getValueSource());
                prop.setInserted(true);
            }
        }
        if(oldBlockTree != newBlockTree) {
            wc.rewrite(oldBlockTree, newBlockTree);
        }
        return newBlockTree;
    }
   
    BlockTree removeSetStatements(WorkingCopy wc, Bean bean, BlockTree oldBlockTree) {
        if(oldBlockTree == null) {
            ExecutableElement elem = execElementHandle.resolve(wc);
            oldBlockTree = wc.getTrees().getTree(elem).getBody();
        }
        BlockTree newBlockTree = oldBlockTree;
        //Remove property set statements
        for (Property prop : bean.getProperties()) {
            if (!prop.isMarkupProperty() && prop.isInserted()) {
                newBlockTree = removeStatement(wc, newBlockTree, bean.getName(), prop.getWriteMethodName());
                prop.setInserted(false);
            }
        }
        //Remove event set statements
        for (EventSet eventSet : bean.getEventSets()) {
            if(eventSet.isInserted()) {
                newBlockTree = removeStatement(wc, newBlockTree, bean.getName(), eventSet.getAddListenerMethodName());
                eventSet.setInserted(false);
            }
        }
        if(oldBlockTree != newBlockTree) {
            wc.rewrite(oldBlockTree, newBlockTree);
        }
        
        return newBlockTree;
    }

    public void addEventSetStatements(Bean bean) {
        for (EventSet eventSet : bean.getEventSets()) {
            if (!eventSet.isInserted()) {
                eventSet.insertEntry();
            }
        }
    }

    private BlockTree addStatement(WorkingCopy wc, BlockTree blockTree, String beanName, String methodName, String valueSource) {
        TreeMaker make = wc.getTreeMaker();
        ExecutableElement elem = execElementHandle.resolve(wc);
        ArrayList<ExpressionTree> args = new ArrayList<ExpressionTree>();
        if (valueSource != null) {
            SourcePositions[] positions = new SourcePositions[1];
            args.add(wc.getTreeUtilities().parseExpression(valueSource, positions));
        }
        ExpressionTree exprTree = TreeMakerUtils.createMethodInvocation(wc, beanName, methodName, args);
        ExpressionStatementTree exprStatTree = wc.getTreeMaker().ExpressionStatement(exprTree);
        return wc.getTreeMaker().addBlockStatement(blockTree, exprStatTree);
    }

    /*
     * Removes a statement of the form a.setFoo(arg);
     * 
     */ 
    public void removeStatement(final String beanName, final String methodName) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                ExecutableElement elem = execElementHandle.resolve(wc);
                StatementTree stmtTree = findPropertyStatement(wc, beanName, methodName);
                if (stmtTree != null) {
                    removeStatement(wc, stmtTree);
                }
                return null;
            }
        }, javaClass.getFileObject());
    }    

   private BlockTree removeStatement(WorkingCopy wc, BlockTree blockTree, String beanName, String methodName) {
       TreeMaker make = wc.getTreeMaker();
       ExecutableElement elem = execElementHandle.resolve(wc);
       StatementTree stmtTree = findPropertyStatement(wc, beanName, methodName);
       if (stmtTree != null) {
           return wc.getTreeMaker().removeBlockStatement(blockTree, stmtTree);
       }
       return null;
   }
    
    /*
     * Adds a expression statement of the form a.b(arg1, ..);
     */     
    protected StatementTree addMethodInvocationStatement(WorkingCopy wc, MethodTree methodTree, 
            MethodInvocationTree exprTree) {
        ExpressionStatementTree exprStatTree = wc.getTreeMaker().ExpressionStatement(exprTree);
        addStatement(wc, methodTree.getBody(), exprStatTree);
        return exprStatTree;
    }   
    
    /*
     * Adds a return statement given a method and expression
     */     
    protected StatementTree addReturnStatement(WorkingCopy wc, MethodTree methodTree, ExpressionTree exprTree) {
        ReturnTree returnTree = wc.getTreeMaker().Return(exprTree);
        addStatement(wc, methodTree.getBody(), returnTree);
        return returnTree;
    }
    
    /*
     * Adds a given statement to the block
     */       
    private BlockTree addStatement(WorkingCopy wc, BlockTree blockTree, StatementTree stmtTree) {
        BlockTree newBlockTree = wc.getTreeMaker().addBlockStatement(blockTree, stmtTree);
        wc.rewrite(blockTree, newBlockTree);
        return newBlockTree;
    }

    
    /*
     * Replaces method body with a given text
     */ 
    public void replaceBody(final String bodyText) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                ExecutableElement execElement = execElementHandle.resolve(wc);
                MethodTree methodTree = wc.getTrees().getTree(execElement);
                MethodTree newMethodTree = wc.getTreeMaker().Method(methodTree.getModifiers(), methodTree.getName(),
                        methodTree.getReturnType(), methodTree.getTypeParameters(), methodTree.getParameters(),
                        methodTree.getThrows(), "{" + bodyText + "}", (ExpressionTree)methodTree.getDefaultValue());
                wc.rewrite(methodTree, newMethodTree);             
                return null;
            }
        }, javaClass.getFileObject());
    }    
    
    /*
     * Removes a statement given a method and statement to be removed
     */     
    boolean removeStatement(WorkingCopy wc, StatementTree stmtTree) {
        ExecutableElement execElement = execElementHandle.resolve(wc);
        if(execElement != null) {
            BlockTree blockTree = wc.getTrees().getTree(execElement).getBody();
            BlockTree newBlockTree = wc.getTreeMaker().removeBlockStatement(blockTree, stmtTree);
            wc.rewrite(blockTree, newBlockTree);           
            return true;
        }
        return false;
    }
    
    
    /*
     * Renames method name, (I think we have to refactor our model)
     */ 
    public void rename(final String name) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                ExecutableElement execElement = execElementHandle.resolve(wc);
                MethodTree oldTree = wc.getTrees().getTree(execElement);
                Tree newTree = wc.getTreeMaker().setLabel(oldTree, name);
                wc.rewrite(oldTree, newTree);
                return null;
            }
        }, javaClass.getFileObject());
        this.name = name;
    }
    
    /*
     * Update the method as per the passed in context method
     */ 
    public void update(final ContextMethod method) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                ExecutableElement execElement = execElementHandle.resolve(wc);
                MethodTree oldTree = wc.getTrees().getTree(execElement);
                MethodTree newTree = TreeMakerUtils.updateMethod(wc, method, oldTree);
                wc.rewrite(oldTree, newTree);
                return null;
            }
        }, javaClass.getFileObject());
    }

    /*
     * Removes the method from the enclosing class
     */ 
    public void remove() {
        javaClass.removeMethod(execElementHandle);
    }

    /*
     * Returns list of property set statements(i.e statements which looks like a.setFoo(arg1)
     * 
     * Should be called only on _init() and ctor
     * 
     */ 
    public List<Statement> getPropertySetStatements() {
        return (List<Statement>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                List<Statement> stmts = new ArrayList<Statement>();
                ExecutableElement execElement = execElementHandle.resolve(cinfo);
                BlockTree block = cinfo.getTrees().getTree(execElement).getBody();
                if(name.equals(BeanStructureScanner.CTOR)) {
                    //Look for property initializers in the first try catch block, this is
                    //to support the code generated in constructor prior to FCS
                    for(StatementTree stmtTree : block.getStatements()){
                        if(stmtTree.getKind() == Tree.Kind.TRY) {
                            block = ((TryTree)stmtTree).getBlock();
                        }
                    }             
                }
                for(StatementTree stmtTree : block.getStatements()){
                    if(Statement.IsPropertySetter(cinfo, stmtTree)) {
                        stmts.add(Statement.createStatementClass(cinfo, stmtTree, Method.this));
                    }
                }
                return stmts;
            }
        }, javaClass.getFileObject());
    }

    /*
     * Returns true if the method represents a constructor
     */ 
    public boolean isConstructor() {
        return name.equals(CTOR) ? true : false;
    }
    
    /**
     * Check if the method has the initialization block. Should revisit this implementation.
     * Copying the old logic for time being
     * 
     */ 
    public boolean hasInitBlock() {
        return (Boolean)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                ExecutableElement execElement = execElementHandle.resolve(cinfo);
                BlockTree block = cinfo.getTrees().getTree(execElement).getBody();
                for(StatementTree stmtTree : block.getStatements()){
                    if(stmtTree.getKind() == Tree.Kind.TRY) {
                        return true;
                    }
                }
                return false;
            }
        }, javaClass.getFileObject());
    }
        
    /*
     * Returns the body as text
     */ 
    public static String getBodyText(CompilationInfo cinfo, MethodTree tree) {
        SourcePositions sp = cinfo.getTrees().getSourcePositions();
        BlockTree body = tree.getBody();
        int start = (int) sp.getStartPosition(cinfo.getCompilationUnit(), body);
        int end = (int) sp.getEndPosition(cinfo.getCompilationUnit(), body);
        // get body text from source text
        return cinfo.getText().substring(start, end);
    }    

    /*
     * Returns the body as text
     */ 
    public String getBodyText() {
        return (String)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                ExecutableElement execElement = execElementHandle.resolve(cinfo);
                return getBodyText(cinfo, cinfo.getTrees().getTree(execElement));                
            }    
        }, javaClass.getFileObject());
    }

    public ExecutableElement getElement(CompilationInfo cinfo) {
        return execElementHandle.resolve(cinfo);
    }
    
    public String getCommentText(CompilationInfo cinfo, MethodTree tree) {
        return TreeUtils.getPrecedingImmediateCommentText(cinfo, tree);
    }

    public int getModifierFlags(MethodTree tree) {
        return (int)TreeUtils.getModifierFlags(tree.getModifiers());
    }
    
    /* 
     * Returns line and column numbers at which the cursor should be positioned when
     * the user selects this method in the designer
     */
    public int[] getCursorPosition(final boolean inserted) {
        return (int[])ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                List<Statement> stmts = new ArrayList<Statement>();
                ExecutableElement execElement = execElementHandle.resolve(cinfo);
                BlockTree block = cinfo.getTrees().getTree(execElement).getBody();
                SourcePositions sp = cinfo.getTrees().getSourcePositions();
                long offset = -1;
                int stmtsLen = block.getStatements().size();
                if(stmtsLen > 0) {
                    offset = sp.getStartPosition(cinfo.getCompilationUnit(), block.getStatements().get(0));
                }else {
                    offset = sp.getEndPosition(cinfo.getCompilationUnit(), block);
                }
                try {
                    DataObject od = DataObject.find(javaClass.getFileObject());
                    EditorCookie ec = od.getCookie(EditorCookie.class);
                    if (ec != null && offset != -1) {
                        StyledDocument doc = ec.getDocument();
                        if (doc != null) {
                            int line = NbDocument.findLineNumber(doc, (int)offset);
                            int col = NbDocument.findLineColumn(doc, (int)offset);
                            //Have the cursor in a blank line before the first statement if the
                            //method is newly inserted or when there are no statements                        
                            if(stmtsLen == 0) {
                                line -= 1;
                                col += 3;
                            }
                            if(inserted) {
                                line-=1;
                            }
                            return new int[]{line, col};
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return null;
            }
        }, javaClass.getFileObject());          
    }
}
