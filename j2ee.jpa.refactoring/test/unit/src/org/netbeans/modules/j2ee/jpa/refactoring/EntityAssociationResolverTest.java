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

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.EntityMappingsMetadataModelFactory;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Erno Mononen
 */
public class EntityAssociationResolverTest extends SourceTestSupport {
    
    private static final String PKG  = "entities.";
    private static final String CUSTOMER = PKG + "Customer";
    private static final String ORDER = PKG +  "Order";
    private static final String DEPARTMENT = PKG + "Department";
    private static final String EMPLOYEE = PKG + "Employee";
    private static final String USER = PKG + "User";
    private static final String GROUP = PKG + "Group";
    
    public EntityAssociationResolverTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    private String getPath(){
        return getDataDir().getAbsoluteFile().toString();
    }
    
    private FileObject getJavaFile(String name){
        return FileUtil.toFileObject(new File(getPath() +"/" + name.replace('.', '/') + ".java"));
    }
    
    protected FileObject[] getClassPathRoots(){
        return new FileObject[]{FileUtil.toFileObject(new File(getPath()))};
    }
    
    private TreePathHandle getTreePathHandle(final String fieldName, String className) throws IOException{
        return RefactoringUtil.getTreePathHandle(fieldName, className, getJavaFile(className));
    }
    
    private MetadataModel<EntityMappingsMetadata> createModel() throws IOException, InterruptedException{
        FileObject src = FileUtil.toFileObject(new File(getPath()));
        IndexingManager.getDefault().refreshIndexAndWait(src.getURL(), null);
        return  EntityMappingsMetadataModelFactory.createMetadataModel(
                ClassPath.getClassPath(src, ClassPath.BOOT),
                ClassPath.getClassPath(src, ClassPath.COMPILE),
                ClassPath.getClassPath(src, ClassPath.SOURCE));
    }

    /**
     * TODO, resolve fail
     * currently it fails in EntityAssociationResolver
     * on TypeElement te = info.getElements().getTypeElement(targetClass);
     * because te==null after this line, can't debug inside of com.sun.tools.javac, need additional invesigation
     * @throws Exception
     */
//    public void testGetTarget() throws Exception {
//        EntityAssociationResolver resolver = new EntityAssociationResolver(getTreePathHandle("customer", ORDER), createModel());
//        List<EntityAssociationResolver.Reference> orderRefs = resolver.getReferringProperties();
//        assertEquals(2, orderRefs.size());
//
//        EntityAssociationResolver.Reference fieldRef = orderRefs.get(0);
//        assertEquals(CUSTOMER, fieldRef.getClassName());
//        assertEquals("orders", fieldRef.getPropertyName());
//        assertEquals("customer", fieldRef.getSourceProperty());
//
//        EntityAssociationResolver.Reference propertyRef = orderRefs.get(1);
//        assertEquals(CUSTOMER, propertyRef.getClassName());
//        assertEquals("getOrders", propertyRef.getPropertyName());
//        assertEquals("customer", propertyRef.getSourceProperty());
//
//
//    }
    
   /**
     * TODO, resolve fail
     * @throws Exception
     */
//    public void testResolveReferences() throws Exception {
//        EntityAssociationResolver resolver = new EntityAssociationResolver(getTreePathHandle("customer", ORDER), createModel());
//        List<EntityAnnotationReference> result = resolver.resolveReferences();
//        assertEquals(1, result.size());
//        EntityAnnotationReference reference = result.get(0);
//        assertEquals(EntityAssociationResolver.ONE_TO_MANY, reference.getAnnotation());
//        assertEquals("entities.Customer", reference.getEntity());
//        assertEquals(EntityAssociationResolver.MAPPED_BY, reference.getAttribute());
//        assertEquals("customer", reference.getAttributeValue());
//
//    }
    
    public void testGetTreePathHandle() throws Exception{
        final TreePathHandle handle  = RefactoringUtil.getTreePathHandle("orders", CUSTOMER, getJavaFile(CUSTOMER));
        JavaSource source = JavaSource.forFileObject(handle.getFileObject());
        source.runUserActionTask(new CancellableTask<CompilationController>(){
            
            public void cancel() {
            }
            
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                Element element = handle.resolveElement(parameter);
                assertEquals("orders", element.getSimpleName().toString());
                for (AnnotationMirror annotation : element.getAnnotationMirrors()){
                    assertEquals(EntityAssociationResolver.ONE_TO_MANY, annotation.getAnnotationType().toString());
                }
            }
        }, true);
        
    }
    
}
