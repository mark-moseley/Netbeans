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
package org.netbeans.modules.j2ee.jpa.verification.fixes;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.awt.Dialog;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class CreateId implements Fix {
    private FileObject fileObject;
    private ElementHandle<TypeElement> classHandle;
    private AccessType accessType;
    
    public CreateId(FileObject fileObject, ElementHandle<TypeElement> classHandle, AccessType accessType) {
        this.classHandle = classHandle;
        this.fileObject = fileObject;
        this.accessType = accessType;
    }
    
    public ChangeInfo implement(){
        PickOrCreateFieldPanel pnlPickOrCreateField = new PickOrCreateFieldPanel();
        pnlPickOrCreateField.setAvailableFields(getAvailableFields());
        pnlPickOrCreateField.setFileObject(fileObject);
        
        DialogDescriptor ddesc = new DialogDescriptor(pnlPickOrCreateField,
                NbBundle.getMessage(CreateId.class, "LBL_AddIDAnnotationDlgTitle"));
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(ddesc);
        
        pnlPickOrCreateField.setDlgDescriptor(ddesc);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        
        if (ddesc.getValue() == DialogDescriptor.OK_OPTION){
            if (pnlPickOrCreateField.wasCreateNewFieldSelected()){
                createIDField(pnlPickOrCreateField.getNewIdName(),
                        pnlPickOrCreateField.getSelectedIdType());
            } else{
                // pick existing
                String fieldName = (String) pnlPickOrCreateField.getSelectedField();
                createIDField(fieldName, null);
            }
        }
        
        return null;
    }
    
    private String[] getAvailableFields(){
        String result[] = null;
        MetadataModel<EntityMappingsMetadata> emModel = ModelUtils.getModel(fileObject);
        
        if (emModel != null){
            try {
                result = emModel.runReadAction(new org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata, java.lang.String[]>() {
                    java.util.List<java.lang.String> fieldNames = new java.util.ArrayList<java.lang.String>();
                    
                    public java.lang.String[] run(org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata metadata) {
                        org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity entity = org.netbeans.modules.j2ee.jpa.model.ModelUtils.getEntity(metadata, classHandle.getQualifiedName());
                        if (entity != null) {
                            for (org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic basic : entity.getAttributes().getBasic()) {
                                fieldNames.add(basic.getName());
                            }
                        }
                        return fieldNames.toArray(new java.lang.String[0]);
                    }
                });
            } catch (IOException ex) {
                JPAProblemFinder.LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } else {
            JPAProblemFinder.LOG.severe("Could not read merged model");
        }
        
        return result;
    }
    
    public int hashCode(){
        return 1;
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    public String getText(){
        return NbBundle.getMessage(CreatePersistenceUnit.class, "MSG_MissingIDAnnotationHint");
    }
    
    private void createIDField(final String fieldName, final String typeName){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement clazz = classHandle.resolve(workingCopy);
                
                if (clazz != null){
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    ClassTree clazzTree = workingCopy.getTrees().getTree(clazz);
                    TreeMaker make = workingCopy.getTreeMaker();
                    boolean usedExistingField = true;
                    boolean usedExistingGetter = true;
                    
                    VariableTree fieldTree = null;
                    MethodTree accesorTree = null;
                    MethodTree mutatorTree = null;
                    Tree type;
                    if (typeName == null || "".equals(typeName) ) {
                        type = null;
                    } else {
                        type = genUtils.createType(typeName, clazz);
                    }
                    VariableElement fieldElem = ModelUtils.getField(clazz, fieldName);
                    
                    if (fieldElem != null){
                        fieldTree = (VariableTree) workingCopy.getTrees().getTree(fieldElem);
                        
                        if (type == null){
                            type = fieldTree.getType();
                        }
                    }
                    
                    ExecutableElement getterElement = ModelUtils.getAccesor(clazz, fieldName);
                    
                    if (getterElement != null){
                        accesorTree = (MethodTree) workingCopy.getTrees().getTree(getterElement);
                        
                        if (type == null){
                            type = accesorTree.getReturnType();
                        }
                    }
                    
                    if (fieldTree == null){
                        fieldTree = genUtils.createField(
                                clazz, make.Modifiers(Collections.singleton(Modifier.PRIVATE)),
                                fieldName, typeName, null);
                        
                        usedExistingField = false;
                    }
                    
                    if (accesorTree == null){
                        accesorTree = genUtils.createPropertyGetterMethod(
                                make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                                fieldName, type);
                        
                        mutatorTree = genUtils.createPropertySetterMethod(
                                make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                                fieldName, type);
                        
                        usedExistingGetter = false;
                    }
                    
                    ModifiersTree toAnnotate = accessType == AccessType.FIELD ?
                        fieldTree.getModifiers() : accesorTree.getModifiers();
                    
                    AnnotationTree idAnnotation = genUtils.createAnnotation(JPAAnnotations.ID);
                    
                    workingCopy.rewrite(toAnnotate, make.addModifiersAnnotation(toAnnotate, idAnnotation));
                    
                    ClassTree modifiedClazz = clazzTree;
                    
                    if (!usedExistingField){
                        modifiedClazz = genUtils.addClassFields(modifiedClazz,
                                Collections.singletonList(fieldTree));
                    }
                    
                    if (!usedExistingGetter){
                        modifiedClazz = make.addClassMember(modifiedClazz, accesorTree);
                        modifiedClazz = make.addClassMember(modifiedClazz, mutatorTree);
                    }
                    
                    if (modifiedClazz != clazzTree){
                        workingCopy.rewrite(clazzTree, modifiedClazz);
                    }
                }
            }
        };
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        
        try{
            javaSource.runModificationTask(task).commit();
        } catch (IOException e){
            JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
}
