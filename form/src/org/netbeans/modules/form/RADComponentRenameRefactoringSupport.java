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
package org.netbeans.modules.form;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Class used to hook form component/variables renaming into refactoring.
 * @author Wade Chandler
 * @version 1.0
 */
public class RADComponentRenameRefactoringSupport {
    private String newName = "";
    private RADComponent component = null;

    public RADComponent getComponent() {
        return component;
    }

    public void setComponent(RADComponent component) {
        this.component = component;
    }

    /**
     * Creates a new instance of support.
     * @param newName the new name of the field being set
     */
    public RADComponentRenameRefactoringSupport(String newName) {
        this.newName = newName;
    }
    
    private static class MemberVisitor
            extends TreePathScanner<Void, Void>
            implements CancellableTask<CompilationController>{
        
        private CompilationInfo info;
        private String member = null;
        private TreePathHandle handle = null;
        
        public TreePathHandle getHandle() {
            return handle;
        }
        
        public void setHandle(TreePathHandle handle) {
            this.handle = handle;
        }
        
        public MemberVisitor(CompilationInfo info, String member) {
            this.info = info;
            this.member = member;
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            List<? extends Tree> members = (List<? extends Tree>) t.getMembers();
            Iterator<? extends Tree> it = members.iterator();
            while(it.hasNext()){
                Tree tr = it.next();
                if (tr.getKind() == Tree.Kind.VARIABLE) {
                    Trees trees = info.getTrees();
                    TreePath path = new TreePath(getCurrentPath(), tr);
                    Element el = trees.getElement(path);
                    String sname = el.getSimpleName().toString();
                    if(sname.equals(this.member)){
                        this.handle = TreePathHandle.create(path, info);
                    }
                } // TODO also need to check local variables in initComponents
            }
            return null;
        }
        
        public void cancel() {
        }
        
        public void run(CompilationController parameter) throws IOException {
            this.info = parameter;
            parameter.toPhase(Phase.ELEMENTS_RESOLVED);
            this.scan(parameter.getCompilationUnit(), null);
        }
    }
    
    /**
     * Method used to perform the actual refactoring. This method will use the oldName to locate the
     * elemnent/field needing renamed. It will then setup the correct Java infrastructure path
     * for refactoring to use. Currently no UI besides gather the user variable is used. If found at
     * some point people are using component fields outside of the forms and this is very slow then
     * we can add better UI support.
     */
    public void doRenameRefactoring(){
        //now we need to get into the Java phase and get a TreePathHandle
        //to the field we are renaming. We can then kick off a RenameRefactoring
        FormDataObject dao = FormEditor.getFormDataObject(this.component.getFormModel());
        if(dao!=null){
            //we should be able to
            JavaSource js = JavaSource.forFileObject(dao.getPrimaryFile());
            try {
                MemberVisitor visitor = new MemberVisitor(null, component.getName());
                js.runUserActionTask(visitor, true);
                if(visitor.getHandle()==null){
                    //this would only happen if setName were called without the correct component being
                    //selected some how...
                    return;
                }
                FormEditorSupport fes = dao.getFormEditorSupport();
                if (fes.isModified()) {
                    fes.saveDocument();
                }
                //ok, so we are now ready to actually setup our RenameRefactoring...we need the element TreePathHandle
                Lookup rnl = Lookups.singleton(visitor.getHandle());
                RefactoringSession renameSession = RefactoringSession.create("Change variable name");//NOI18N
                RenameRefactoring refactoring = new RenameRefactoring(rnl);
                Problem pre = refactoring.preCheck();
                if(pre!=null&&pre.isFatal()){
                    Logger.getLogger("global").log(Level.WARNING, "There were problems trying to perform the refactoring.");
                }
                
                Problem p = null;
                
                if( (!(pre!=null&&pre.isFatal())) && !emptyOrWhite(newName) ){
                    refactoring.setNewName(newName);
                    p = refactoring.prepare(renameSession);
                }
                
                if( (!(p!=null&&p.isFatal())) && !emptyOrWhite(newName) ){
                    renameSession.doRefactoring(true);
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
    
    private static boolean emptyOrWhite(String s){
        if(s==null){
            return true;
        }
        if(s.trim().length()<0){
            return true;
        }
        return false;
    }
    
}
