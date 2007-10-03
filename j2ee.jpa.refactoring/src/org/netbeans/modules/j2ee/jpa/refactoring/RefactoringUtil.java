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

package org.netbeans.modules.j2ee.jpa.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Utility methods needed in JPA refactoring, split from
 * <code>org.netbeans.modules.j2ee.refactoring.Utility</code>.
 *
 * @author Erno Mononen
 */
public abstract class RefactoringUtil {
    
    private static final String JAVA_MIME_TYPE = "text/x-java"; //NO18N
    
    /**
     * Constructs a new fully qualified name for the given <code>newName</code>.
     *
     * @param originalFullyQualifiedName the old fully qualified name of the class.
     * @param newName the new unqualified name of the class.
     *
     * @return the new fully qualified name of the class.
     */
    public static String renameClass(String originalFullyQualifiedName, String newName){
        Parameters.notEmpty("originalFullyQualifiedName", originalFullyQualifiedName); //NO18N
        Parameters.notEmpty("newName", newName); //NO18N
        int lastDot = originalFullyQualifiedName.lastIndexOf('.');
        return (lastDot <= 0) ? newName : originalFullyQualifiedName.substring(0, lastDot + 1) + newName;
    }
    
    /**
     * Sets the given <code>toAdd</code> as the following problem for
     * the given <code>existing</code> problem.
     *
     * @param toAdd the problem to add, may be null.
     * @param existing the problem whose following problem should be set, may be null.
     *
     * @return the existing problem with its following problem
     * set to the given problem or null if both of the params
     * were null.
     *
     */
    public static Problem addToEnd(Problem toAdd, Problem existing){
        if (existing == null){
            return toAdd;
        }
        if (toAdd == null){
            return existing;
        }
        
        Problem tail = existing;
        while(tail.getNext() != null){
            tail = tail.getNext();
        }
        tail.setNext(toAdd);
        
        return tail;
    }
    
    // copied from o.n.m.java.refactoring.RetoucheUtils
    public static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) return false;
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            if (p==opened[i]) {
                SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (int j = 0; j < gr.length; j++) {
                    if (fo==gr[j].getRootFolder()) return true;
                    if (FileUtil.isParentOf(gr[j].getRootFolder(), fo))
                        return true;
                }
                return false;
            }
        }
        return false;
    }
    
    // copied from o.n.m.java.refactoring.RetoucheUtils
    public static boolean isJavaFile(FileObject f) {
        return JAVA_MIME_TYPE.equals(f.getMIMEType()); //NOI18N
    }
    
    
    public static CompilationInfo getCompilationInfo(TreePathHandle handle, final AbstractRefactoring refactoring){
        
        CompilationInfo existing = refactoring.getRefactoringSource().lookup(CompilationInfo.class);
        if (existing != null){
            return existing;
        }
        final ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        JavaSource source = JavaSource.create(cpInfo, new FileObject[]{handle.getFileObject()});
        try{
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void run(CompilationController co) throws Exception {
                    co.toPhase(JavaSource.Phase.RESOLVED);
                    refactoring.getContext().add(co);
                }
                
                public void cancel() {
                }
                
            }, false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return refactoring.getContext().lookup(CompilationInfo.class);
    }
    
    /**
     * Gets the fully qualified name for the given <code>fileObject</code>. If it
     * represents a java package, will return the name of the package (with dots as separators).
     *
     *@param fileObject the file object whose FQN should be get. Must belong to
     * a project.
     *@return the FQN for the given file object.
     */
    public static String getQualifiedName(FileObject fileObject){
        Project project = FileOwnerQuery.getOwner(fileObject);
        assert project != null;
        ClassPathProvider classPathProvider = project.getLookup().lookup(ClassPathProvider.class);
        assert classPathProvider != null;
        return classPathProvider.findClassPath(fileObject, ClassPath.SOURCE).getResourceName(fileObject, '.', false);
        
    }
    
    /**
     * Gets the name of the property associated with the given accessor.
     *
     * @param accessor the name of the accessor method of the property. Must follow the JavaBeans
     * naming conventions, i.e. start with 'get/set/is' followed by an uppercase letter,
     * otherwise it is assumed that the name of the property directly matches with
     * the getter. Must not be null or empty.
     *
     * @return the property name resolved from the given <code>getter</code>, i.e.
     * if the given arg was <code>getProperty</code>, this method will return
     * <code>property</code>.
     */
    public static String getPropertyName(String accessor){
        Parameters.notEmpty("accessor", accessor); //NO18N
        
        int prefixLength = getPrefixLength(accessor);
        String withoutPrefix = accessor.substring(prefixLength);
        char firstChar = withoutPrefix.charAt(0);
        
        if (!Character.isUpperCase(firstChar)){
            return accessor;
        }
        
        return Character.toLowerCase(firstChar) + withoutPrefix.substring(1);
    }
    
    private static int getPrefixLength(String accessor){
        String[] accessorPrefixes = new String[]{"get", "set", "is"}; //NO18N
        for (String prefix : accessorPrefixes){
            if (accessor.startsWith(prefix)){
                return prefix.length();
            }
        }
        return 0;
    }
    
    /**
     * Resolves the TreePathHandle for the given refactoring.
     * @param refactoring the refactoring.
     * @return the TreePathHandle or null if no handle could be resolved.
     */
    public static TreePathHandle resolveTreePathHandle(AbstractRefactoring refactoring) throws IOException {
        Parameters.notNull("refactoring", refactoring); //NO18N
        
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            return tph;
        }
        
        final TreePathHandle[] result = new TreePathHandle[1];
        JavaSource source = JavaSource.forFileObject(refactoring.getRefactoringSource().lookup(FileObject.class));
        
        source.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController co) throws Exception {
                co.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = co.getCompilationUnit();
                result[0] = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
            }
            
        }, true);
        
        return result[0];
    }
    
    /**
     * Gets a TreePathHandle for the specified property.
     *
     * @param fieldName the name of the field.
     * @param className the FQN of the class.
     * @param file the file object representing the class.
     */
    public static TreePathHandle getTreePathHandle(final String fieldName,
            final String className, FileObject file) throws IOException{
        
        final TreePathHandle[] result = new TreePathHandle[1];
        JavaSource source = JavaSource.forFileObject(file);
        
        source.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                
                TypeElement te = info.getElements().getTypeElement(className);
                
                for (Element enclosed : te.getEnclosedElements()){
                    if (enclosed.getSimpleName().contentEquals(fieldName)){
                        TreePath propertyPath = info.getTrees().getPath(enclosed);
                        result[0] = TreePathHandle.create(propertyPath, info);
                    }
                }
            }
        }, true);
        
        return result[0];
    }
    
    
}
