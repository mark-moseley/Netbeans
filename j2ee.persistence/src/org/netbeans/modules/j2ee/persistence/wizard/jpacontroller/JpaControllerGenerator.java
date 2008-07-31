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

package org.netbeans.modules.j2ee.persistence.wizard.jpacontroller;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.EmbeddedPkSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.AnnotationInfo;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.TypeInfo;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.MethodInfo;

/**
 *
 * @author mbohm
 */
public class JpaControllerGenerator {
    
    public static void generateJpaController(Project project, final String entityClass, final String controllerClass, String exceptionPackage, FileObject pkg, FileObject controllerFileObject, final EmbeddedPkSupport embeddedPkSupport) throws IOException {
        final boolean isInjection = true;//Util.isSupportedJavaEEVersion(project);
        final String simpleEntityName = JpaControllerUtil.simpleClassName(entityClass);
        String persistenceUnit = getPersistenceUnitAsString(project);
        final String fieldName = JpaControllerUtil.fieldFromClassName(simpleEntityName);

        final List<ElementHandle<ExecutableElement>> idGetter = new ArrayList<ElementHandle<ExecutableElement>>();
        final FileObject[] arrEntityClassFO = new FileObject[1];
        final List<ElementHandle<ExecutableElement>> toOneRelMethods = new ArrayList<ElementHandle<ExecutableElement>>();
        final List<ElementHandle<ExecutableElement>> toManyRelMethods = new ArrayList<ElementHandle<ExecutableElement>>();
        final boolean[] fieldAccess = new boolean[] { false };
        final String[] idProperty = new String[1];

        //detect access type
        final ClasspathInfo classpathInfo = ClasspathInfo.create(pkg);
        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement jc = controller.getElements().getTypeElement(entityClass);
                arrEntityClassFO[0] = org.netbeans.api.java.source.SourceUtils.getFile(jc, controller.getClasspathInfo());
                fieldAccess[0] = JpaControllerUtil.isFieldAccess(jc);
                for (ExecutableElement method : JpaControllerUtil.getEntityMethods(jc)) {
                    String methodName = method.getSimpleName().toString();
                    if (methodName.startsWith("get")) {
                        Element f = fieldAccess[0] ? JpaControllerUtil.guessField(controller, method) : method;
                        if (f != null) {
                            if (JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.Id") ||
                                    JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.EmbeddedId")) {
                                idGetter.add(ElementHandle.create(method));
                                idProperty[0] = JpaControllerUtil.getPropNameFromMethod(methodName);
                            } else if (JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.OneToOne") ||
                                    JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.ManyToOne")) {
                                toOneRelMethods.add(ElementHandle.create(method));
                            } else if (JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.OneToMany") ||
                                    JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.ManyToMany")) {
                                toManyRelMethods.add(ElementHandle.create(method));
                            }
                        }
                    }
                }
            }
        }, true);
        
        if (idGetter.size() < 1) {
            String msg = entityClass + ": " + NbBundle.getMessage(JpaControllerGenerator.class, "ERR_GenJsfPages_CouldNotFindIdProperty"); //NOI18N
            if (fieldAccess[0]) {
                msg += " " + NbBundle.getMessage(JpaControllerGenerator.class, "ERR_GenJsfPages_EnsureSimpleIdNaming"); //NOI18N
            }
            throw new IOException(msg);
        }
        
        if (arrEntityClassFO[0] != null) {
            addImplementsClause(arrEntityClassFO[0], entityClass, "java.io.Serializable"); //NOI18N
        }
        
        controllerFileObject = generateJpaController(fieldName, pkg, idGetter.get(0), persistenceUnit, controllerClass, exceptionPackage,
                entityClass, simpleEntityName, toOneRelMethods, toManyRelMethods, isInjection, fieldAccess[0], controllerFileObject, embeddedPkSupport);
    }
    
    private static String getPersistenceUnitAsString(Project project) throws IOException {
        String persistenceUnit = null;
        PersistenceScope persistenceScopes[] = PersistenceUtils.getPersistenceScopes(project);
        if (persistenceScopes.length > 0) {
            FileObject persXml = persistenceScopes[0].getPersistenceXml();
            if (persXml != null) {
                Persistence persistence = PersistenceMetadata.getDefault().getRoot(persXml);
                PersistenceUnit units[] = persistence.getPersistenceUnit();
                if (units.length > 0) {
                    persistenceUnit = units[0].getName();
                }
            }
        }
        return persistenceUnit;
    }
    
    private static void addImplementsClause(FileObject fileObject, final String className, final String interfaceName) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] modified = new boolean[] { false };
        ModificationResult modificationResult = javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                TypeMirror interfaceType = workingCopy.getElements().getTypeElement(interfaceName).asType();
                if (!workingCopy.getTypes().isSubtype(typeElement.asType(), interfaceType)) {
                    ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                    GenerationUtils.newInstance(workingCopy).addImplementsClause(classTree, interfaceName);
                    modified[0] = true;
                }
            }
        });
        if (modified[0]) {
            modificationResult.commit();
        }
    }
    
    private static FileObject generateJpaController(
            final String fieldName, 
            final FileObject pkg, 
            final ElementHandle<ExecutableElement> idGetter, 
            final String persistenceUnit, 
            final String controllerClass,
            final String exceptionPackage, 
            final String entityClass, 
            final String simpleEntityName,
            final List<ElementHandle<ExecutableElement>> toOneRelMethods,
            final List<ElementHandle<ExecutableElement>> toManyRelMethods,
            final boolean isInjection,
            final boolean isFieldAccess,
            final FileObject controllerFileObject, 
            final EmbeddedPkSupport embeddedPkSupport) throws IOException {
        
            final String[] idPropertyType = new String[1];
            final String[] idGetterName = new String[1];
            final boolean[] embeddable = new boolean[] { false };
            
            JavaSource controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
            controllerJavaSource.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    
                    ExecutableElement idGetterElement = idGetter.resolve(workingCopy);
                    idGetterName[0] = idGetterElement.getSimpleName().toString();
                    TypeMirror idType = idGetterElement.getReturnType();
                    TypeElement idClass = null;
                    if (TypeKind.DECLARED == idType.getKind()) {
                        DeclaredType declaredType = (DeclaredType) idType;
                        idClass = (TypeElement) declaredType.asElement();
                        embeddable[0] = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                        idPropertyType[0] = idClass.getQualifiedName().toString();
                    }
                    
                    String simpleIdPropertyType = JpaControllerUtil.simpleClassName(idPropertyType[0]);
                    
                    TreeMaker make = workingCopy.getTreeMaker();
                    
                    TypeElement controllerTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                    ClassTree classTree = workingCopy.getTrees().getTree(controllerTypeElement);
                    ClassTree modifiedClassTree = classTree;
                    
                    int privateModifier = java.lang.reflect.Modifier.PRIVATE;
                    int publicModifier = java.lang.reflect.Modifier.PUBLIC;
                    
                    AnnotationInfo[] annotations = null;
                    if (isInjection) {
                        annotations = new AnnotationInfo[1];
                        annotations[0] = new AnnotationInfo("javax.annotation.Resource");
                        modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "utx", "javax.transaction.UserTransaction", privateModifier, null, annotations);
                        
                        if (persistenceUnit == null) {
                            annotations[0] = new AnnotationInfo("javax.persistence.PersistenceUnit");
                        } else {
                            annotations[0] = new AnnotationInfo("javax.persistence.PersistenceUnit", new String[]{"unitName"}, new Object[]{persistenceUnit});
                        }
                    } else {
//                        Set<Modifier> publicModifierSet = new HashSet<Modifier>();
//                        publicModifierSet.add(Modifier.PUBLIC);
//                        MethodTree modifiedConstructor = make.Method(
//                                make.Modifiers(publicModifierSet), // public
//                                "<init>",
//                                null, // return type
//                                Collections.<TypeParameterTree>emptyList(), // type parameters - none
//                                Collections.<VariableTree>emptyList(), // arguments - none
//                                Collections.<ExpressionTree>emptyList(), // throws 
//                                "{ emf = Persistence.createEntityManagerFactory(\"" + persistenceUnit + "\"); }", // body text
//                                null // default value - not applicable here, used by annotations
//                            );
//                        MethodTree constructor = null;
//                        for(Tree tree : modifiedClassTree.getMembers()) {
//                            if(Tree.Kind.METHOD == tree.getKind()) {
//                                MethodTree mtree = (MethodTree)tree;
//                                List<? extends VariableTree> mTreeParameters = mtree.getParameters();
//                                if(mtree.getName().toString().equals("<init>") &&
//                                        (mTreeParameters == null || mTreeParameters.size() == 0) &&
//                                        !workingCopy.getTreeUtilities().isSynthetic(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree))) {
//                                        constructor = mtree;
//                                        break;
//                                }
//                            }
//                        }
//                        if (constructor == null) {
//                            modifiedClassTree = make.addClassMember(modifiedClassTree, modifiedConstructor);
//                        }
//                        else {
//                            workingCopy.rewrite(constructor, modifiedConstructor);
//                        }
                        MethodInfo mi = new MethodInfo("<init>", publicModifier, "void", null, null, null, "{ emf = Persistence.createEntityManagerFactory(\"" + persistenceUnit + "\"); }", null, null);
                        modifiedClassTree = JpaControllerUtil.TreeMakerUtils.modifyDefaultConstructor(classTree, modifiedClassTree, workingCopy, mi);
                    }
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "emf", "javax.persistence.EntityManagerFactory", privateModifier, null, annotations);
                    
                    MethodInfo methodInfo = new MethodInfo("getEntityManager", publicModifier, "javax.persistence.EntityManager", null, null, null, "return emf.createEntityManager();", null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    String bodyText;
                    StringBuffer updateRelatedInCreate = new StringBuffer();
                    StringBuffer updateRelatedInEditPre = new StringBuffer();
                    StringBuffer attachRelatedInEdit = new StringBuffer();
                    StringBuffer updateRelatedInEditPost = new StringBuffer();
                    StringBuffer updateRelatedInDestroy = new StringBuffer();
                    StringBuffer initRelatedInCreate = new StringBuffer();
                    StringBuffer illegalOrphansInCreate = new StringBuffer();
                    StringBuffer illegalOrphansInEdit = new StringBuffer();
                    StringBuffer illegalOrphansInDestroy = new StringBuffer();
                    StringBuffer initCollectionsInCreate = new StringBuffer();  //useful in case user removes listbox from New.jsp

                    List<ElementHandle<ExecutableElement>> allRelMethods = new ArrayList<ElementHandle<ExecutableElement>>(toOneRelMethods);
                    allRelMethods.addAll(toManyRelMethods);
                    
                    String[] importFqs = {"javax.persistence.Query",
                                "javax.persistence.EntityNotFoundException"                              
                    };
                    
                    CompilationUnitTree modifiedImportCut = null;
                    for (String importFq : importFqs) {
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                    }

                    String oldMe = null;
            
                    // <editor-fold desc=" all relations ">
                    for(Iterator<ElementHandle<ExecutableElement>> it = allRelMethods.iterator(); it.hasNext();) {
                        ElementHandle<ExecutableElement> handle = it.next();
                        ExecutableElement m = handle.resolve(workingCopy);
                        int multiplicity = JpaControllerUtil.isRelationship(workingCopy, m, isFieldAccess);
                        ExecutableElement otherSide = JpaControllerUtil.getOtherSideOfRelation(workingCopy, m, isFieldAccess);

                        if (otherSide != null) {
                            TypeElement relClass = (TypeElement)otherSide.getEnclosingElement();
                            boolean isRelFieldAccess = JpaControllerUtil.isFieldAccess(relClass);
                            int otherSideMultiplicity = JpaControllerUtil.isRelationship(workingCopy, otherSide, isRelFieldAccess);
                            TypeMirror t = m.getReturnType();
                            Types types = workingCopy.getTypes();
                            TypeMirror tstripped = JpaControllerUtil.stripCollection(t, types);
                            boolean isCollection = t != tstripped;
                            String simpleCollectionTypeName = null;
                            String collectionTypeClass = null;
                            if (isCollection) {
                                TypeElement tAsElement = (TypeElement) types.asElement(t);
                                simpleCollectionTypeName = tAsElement.getSimpleName().toString();
                                collectionTypeClass = tAsElement.getQualifiedName().toString();
                            }
                            String relType = tstripped.toString();
                            String simpleRelType = JpaControllerUtil.simpleClassName(relType); //just "Pavilion"
                            String relTypeReference = simpleRelType;
                            String mName = m.getSimpleName().toString();
                            String otherName = otherSide.getSimpleName().toString();
                            String relFieldName = JpaControllerUtil.getPropNameFromMethod(mName);
                            String otherFieldName = JpaControllerUtil.getPropNameFromMethod(otherName);
                            
                            boolean columnNullable = JpaControllerUtil.isFieldOptionalAndNullable(workingCopy, m, isFieldAccess);
                            boolean relColumnNullable = JpaControllerUtil.isFieldOptionalAndNullable(workingCopy, otherSide, isFieldAccess);
                            
                            String relFieldToAttach = isCollection ? relFieldName + relTypeReference + "ToAttach" : relFieldName;
                            String scalarRelFieldName = isCollection ? relFieldName + relTypeReference : relFieldName;
                            
                            if (!controllerClass.startsWith(entityClass + "JpaController")) {
                                modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, relType);
                            }
                            
                            ExecutableElement relIdGetterElement = JpaControllerUtil.getIdGetter(workingCopy, isFieldAccess, relClass);
                            String refOrMergeString = getRefOrMergeString(relIdGetterElement, relFieldToAttach);
                            
                            if (isCollection) {
                                initCollectionsInCreate.append("if (" + fieldName + "." + mName + "() == null) {\n" +
                                        fieldName + ".s" + mName.substring(1) + "(new ArrayList<" + relTypeReference + ">());\n" +
                                        "}\n");

                                
                                modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "java.util.ArrayList");
                                
                                initRelatedInCreate.append("List<" + relTypeReference + "> attached" + mName.substring(3) + " = new ArrayList<" + relTypeReference + ">();\n" +
                                        "for (" + relTypeReference + " " + relFieldToAttach + " : " + fieldName + "." + mName + "()) {\n" +
                                        relFieldToAttach + " = " + refOrMergeString +
                                        "attached" + mName.substring(3) + ".add(" + relFieldToAttach + ");\n" +
                                        "}\n" +
                                        fieldName + ".s" + mName.substring(1) + "(attached" + mName.substring(3) + ");\n"
                                        );
                            }
                            else {
                                initRelatedInCreate.append(relTypeReference + " " + scalarRelFieldName + " = " + fieldName + "." + mName +"();\n" +
                                    "if (" + scalarRelFieldName + " != null) {\n" +
                                    scalarRelFieldName + " = " + refOrMergeString +
                                    fieldName + ".s" + mName.substring(1) + "(" + scalarRelFieldName + ");\n" +
                                    "}\n");
                            }
                            
                            String relrelInstanceName = "old" + otherName.substring(3) + "Of" + scalarRelFieldName.substring(0, 1).toUpperCase() + (scalarRelFieldName.length() > 1 ? scalarRelFieldName.substring(1) : "");
                            String relrelGetterName = otherName;
                            
                            if (!columnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && multiplicity == JpaControllerUtil.REL_TO_ONE) {
                                illegalOrphansInCreate.append(
                                        relTypeReference + " " + scalarRelFieldName + "OrphanCheck = " + fieldName + "." + mName +"();\n" +
                                                            "if (" + scalarRelFieldName + "OrphanCheck != null) {\n");
                                illegalOrphansInCreate.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "OrphanCheck." + relrelGetterName + "();\n");
                                illegalOrphansInCreate.append("if (" + relrelInstanceName + " != null) {\n" + 
                                        "if (illegalOrphanMessages == null) {\n" +
                                        "illegalOrphanMessages = new ArrayList<String>();\n" +
                                        "}\n" +
                                        "illegalOrphanMessages.add(\"The " + relTypeReference + " \" + " + scalarRelFieldName + "OrphanCheck + \" already has an item of type " + simpleEntityName + " whose " + scalarRelFieldName + " column cannot be null. Please make another selection for the " + scalarRelFieldName + " field.\");\n" +
                                        "}\n");
                                illegalOrphansInCreate.append("}\n");
                            }
                            
                            updateRelatedInCreate.append( (isCollection ? "for(" + relTypeReference + " " + scalarRelFieldName + " : " + fieldName + "." + mName + "()){\n" :
                                                            "if (" + scalarRelFieldName + " != null) {\n"));
                                                            //if 1:1, be sure to orphan the related entity's current related entity
                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE){
                                if (multiplicity != JpaControllerUtil.REL_TO_ONE || columnNullable) { //no need to declare relrelInstanceName if we have already examined it in the 1:1 orphan check
                                    updateRelatedInCreate.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "." + relrelGetterName + "();\n");
                                }
                                if (multiplicity == JpaControllerUtil.REL_TO_ONE) {
                                    if (columnNullable) {
                                        updateRelatedInCreate.append("if (" + relrelInstanceName + " != null) {\n" + 
                                        relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" + 
                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" + 
                                        "}\n");    
                                    }
                                }
                            }
                            
                            updateRelatedInCreate.append( ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                                            scalarRelFieldName + "." + otherName + "().add(" + fieldName +");\n") +
                                                        scalarRelFieldName + " = em.merge(" + scalarRelFieldName +");\n");
                            if (multiplicity == JpaControllerUtil.REL_TO_MANY && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE){
                                updateRelatedInCreate.append("if " + relrelInstanceName + " != null) {\n" +
                                        relrelInstanceName + "." + mName + "().remove(" + scalarRelFieldName + ");\n" +
                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" +
                                        "}\n");
                            }
                            updateRelatedInCreate.append("}\n");
                            
                            if (oldMe == null) {
                                oldMe = "persistent" + simpleEntityName;
                                String oldMeStatement = simpleEntityName + " " + oldMe + " = em.find(" +
                                simpleEntityName + ".class, " + fieldName + "." + idGetterName[0] + "());\n";
                                updateRelatedInEditPre.append("\n " + oldMeStatement);
                            }
                            
                            if (isCollection) {
                                String relFieldOld = relFieldName + "Old";
                                String relFieldNew = relFieldName + "New";
                                String oldScalarRelFieldName = relFieldOld + relTypeReference;
                                String newScalarRelFieldName = relFieldNew + relTypeReference;
                                String oldOfNew = "old" + otherName.substring(3) + "Of" + newScalarRelFieldName.substring(0, 1).toUpperCase() + newScalarRelFieldName.substring(1);
                                updateRelatedInEditPre.append("\n " + simpleCollectionTypeName + "<" + relTypeReference + "> " + relFieldOld + " = " + oldMe + "." + mName + "();\n");
                                updateRelatedInEditPre.append(simpleCollectionTypeName + " <" + relTypeReference + "> " + relFieldNew + " = " + fieldName + "." + mName + "();\n");
                                if (!relColumnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
                                    illegalOrphansInEdit.append(
                                            "for(" + relTypeReference + " " + oldScalarRelFieldName + " : " + relFieldOld + ") {\n" +
                                            "if (!" + relFieldNew + ".contains(" + oldScalarRelFieldName + ")) {\n" +
                                            "if (illegalOrphanMessages == null) {\n" +
                                            "illegalOrphanMessages = new ArrayList<String>();\n" +
                                            "}\n" +
                                            "illegalOrphanMessages.add(\"You must retain " + relTypeReference + " \" + " + oldScalarRelFieldName + " + \" since its " + otherFieldName + " field is not nullable.\");\n" +
                                            "}\n" +
                                            "}\n");
                                }
                                String relFieldToAttachInEdit = newScalarRelFieldName + "ToAttach";
                                String refOrMergeStringInEdit = getRefOrMergeString(relIdGetterElement, relFieldToAttachInEdit);
                                String attachedRelFieldNew = "attached" + mName.substring(3) + "New";
                                attachRelatedInEdit.append("List<" + relTypeReference + "> " + attachedRelFieldNew + " = new ArrayList<" + relTypeReference + ">();\n" +
                                        "for (" + relTypeReference + " " + relFieldToAttachInEdit + " : " + relFieldNew + ") {\n" +
                                        relFieldToAttachInEdit + " = " + refOrMergeStringInEdit +
                                        attachedRelFieldNew + ".add(" + relFieldToAttachInEdit + ");\n" +
                                        "}\n" +
                                        relFieldNew + " = " + attachedRelFieldNew + ";\n" +
                                        fieldName + ".s" + mName.substring(1) + "(" + relFieldNew + ");\n"
                                        );
                                if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
                                    updateRelatedInEditPost.append(
                                        "for (" + relTypeReference + " " + oldScalarRelFieldName + " : " + relFieldOld + ") {\n" +
                                        "if (!" + relFieldNew + ".contains(" + oldScalarRelFieldName + ")) {\n" +
                                        ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? oldScalarRelFieldName + ".s" + otherName.substring(1) + "(null);\n" :
                                            oldScalarRelFieldName + "." + otherName + "().remove(" + fieldName + ");\n") +
                                        oldScalarRelFieldName + " = em.merge(" + oldScalarRelFieldName + ");\n" +
                                        "}\n" +
                                        "}\n");
                                }
                                updateRelatedInEditPost.append("for (" + relTypeReference + " " + newScalarRelFieldName + " : " + relFieldNew + ") {\n" +
                                "if (!" + relFieldOld + ".contains(" + newScalarRelFieldName + ")) {\n" +
                                ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? simpleEntityName + " " + oldOfNew + " = " + newScalarRelFieldName + "." + relrelGetterName + "();\n" +
                                    newScalarRelFieldName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                    newScalarRelFieldName + "." + otherName + "().add(" + fieldName +");\n") +
                                newScalarRelFieldName + " = em.merge(" + newScalarRelFieldName + ");\n");
                                if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
                                    updateRelatedInEditPost.append("if " + oldOfNew + " != null && !" + oldOfNew + ".equals(" + fieldName + ")) {\n" +
                                        oldOfNew + "." + mName + "().remove(" + newScalarRelFieldName + ");\n" +
                                        oldOfNew + " = em.merge(" + oldOfNew + ");\n" +
                                        "}\n");
                                }
                                updateRelatedInEditPost.append("}\n}\n");
                            } else {
                                updateRelatedInEditPre.append("\n" + relTypeReference + " " + scalarRelFieldName + "Old = " + oldMe + "." + mName + "();\n");
                                updateRelatedInEditPre.append(relTypeReference + " " + scalarRelFieldName + "New = " + fieldName + "." + mName +"();\n");
                                if (!relColumnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
                                    illegalOrphansInEdit.append(
                                        "if(" + scalarRelFieldName + "Old != null && !" + scalarRelFieldName + "Old.equals(" + scalarRelFieldName + "New)) {\n" +
                                        "if (illegalOrphanMessages == null) {\n" +
                                        "illegalOrphanMessages = new ArrayList<String>();\n" +
                                        "}\n" +
                                        "illegalOrphanMessages.add(\"You must retain " + relTypeReference + " \" + " + scalarRelFieldName + "Old + \" since its " + otherFieldName + " field is not nullable.\");\n" +
                                        "}\n");
                                }
                                String refOrMergeStringInEdit = getRefOrMergeString(relIdGetterElement, scalarRelFieldName + "New"); 
                                attachRelatedInEdit.append("if (" + scalarRelFieldName + "New != null) {\n" +
                                    scalarRelFieldName + "New = " + refOrMergeStringInEdit +
                                    fieldName + ".s" + mName.substring(1) + "(" + scalarRelFieldName + "New);\n" +
                                    "}\n");
                                if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
                                     updateRelatedInEditPost.append(   
                                        "if(" + scalarRelFieldName + "Old != null && !" + scalarRelFieldName + "Old.equals(" + scalarRelFieldName + "New)) {\n" +
                                        ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + "Old.s" + otherName.substring(1) + "(null);\n" :
                                            scalarRelFieldName + "Old." + otherName + "().remove(" + fieldName +");\n") +
                                        scalarRelFieldName + "Old = em.merge(" + scalarRelFieldName +"Old);\n}\n");
                                }
                                if (multiplicity == JpaControllerUtil.REL_TO_ONE && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && !columnNullable) {
                                    illegalOrphansInEdit.append(
                                        "if(" + scalarRelFieldName + "New != null && !" + scalarRelFieldName + "New.equals(" + scalarRelFieldName + "Old)) {\n");
                                    illegalOrphansInEdit.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "New." + relrelGetterName + "();\n" + 
                                                "if (" + relrelInstanceName + " != null) {\n" + 
                                                "if (illegalOrphanMessages == null) {\n" +
                                                "illegalOrphanMessages = new ArrayList<String>();\n" +
                                                "}\n" +
                                                "illegalOrphanMessages.add(\"The " + relTypeReference + " \" + " + scalarRelFieldName + "New + \" already has an item of type " + simpleEntityName + " whose " + scalarRelFieldName + " column cannot be null. Please make another selection for the " + scalarRelFieldName + " field.\");\n" +
                                                "}\n");
                                    illegalOrphansInEdit.append("}\n");
                                }
                                updateRelatedInEditPost.append(
                                    "if(" + scalarRelFieldName + "New != null && !" + scalarRelFieldName + "New.equals(" + scalarRelFieldName + "Old)) {\n");
                                if (multiplicity == JpaControllerUtil.REL_TO_ONE && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && columnNullable) {
                                    updateRelatedInEditPost.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "New." + relrelGetterName + "();\n" + 
                                            "if (" + relrelInstanceName + " != null) {\n" + 
                                            relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" + 
                                            relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" + 
                                            "}\n");
                                }
                                updateRelatedInEditPost.append(
                                    ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + "New.s" + otherName.substring(1) + "(" + fieldName + ");\n" :
                                        scalarRelFieldName + "New." + otherName + "().add(" + fieldName +");\n") +
                                    scalarRelFieldName + "New = em.merge(" + scalarRelFieldName + "New);\n}\n"
                                    );
                            } 
                            
                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && !relColumnNullable) {
                                String orphanCheckCollection = relFieldName + "OrphanCheck";
                                String orphanCheckScalar = isCollection ? orphanCheckCollection + relTypeReference : relFieldName + "OrphanCheck";
                                illegalOrphansInDestroy.append(
                                        (isCollection ? simpleCollectionTypeName + "<" + relTypeReference + "> " + orphanCheckCollection : relTypeReference + " " + orphanCheckScalar) + " = " + fieldName + "." + mName +"();\n" +
                                        (isCollection ? "for(" + relTypeReference + " " + orphanCheckScalar + " : " + orphanCheckCollection : "if (" + orphanCheckScalar + " != null") + ") {\n" +
                                        "if (illegalOrphanMessages == null) {\n" +
                                        "illegalOrphanMessages = new ArrayList<String>();\n" +
                                        "}\n" +
                                        "illegalOrphanMessages.add(\"This " + simpleEntityName + " (\" + " +  fieldName + " + \") cannot be destroyed since the " + relTypeReference + " \" + " + orphanCheckScalar + " + \" in its " + relFieldName + " field has a non-nullable " + otherFieldName + " field.\");\n" +
                                        "}\n");
                            }
                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
                                updateRelatedInDestroy.append( (isCollection ? simpleCollectionTypeName + "<" + relTypeReference + "> " + relFieldName : relTypeReference + " " + scalarRelFieldName) + " = " + fieldName + "." + mName +"();\n" +
                                        (isCollection ? "for(" + relTypeReference + " " + scalarRelFieldName + " : " + relFieldName : "if (" + scalarRelFieldName + " != null") + ") {\n" +
                                        ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + ".s" + otherName.substring(1) + "(null);\n" :
                                            scalarRelFieldName + "." + otherName + "().remove(" + fieldName +");\n") +
                                        scalarRelFieldName + " = em.merge(" + scalarRelFieldName +");\n}\n\n");
                            }
                            
                            if (collectionTypeClass != null) { //(multiplicity == JpaControllerUtil.REL_TO_MANY) {
                                importFqs = new String[]{
                                    collectionTypeClass //"java.util.Collection"
                                  };
                                for (String importFq : importFqs) {
                                    modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                                }
                            }
                            
                        } else {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot detect other side of a relationship.");
                        }

                    }
                    // </editor-fold>
                    
                    String BEGIN = isInjection ? "utx.begin();" : "em.getTransaction().begin();";
                    String COMMIT = isInjection ? "utx.commit();" : "em.getTransaction().commit();";
                    String ROLLBACK = isInjection ? "utx.rollback();" : "em.getTransaction().rollback();";
                    
                    if (illegalOrphansInCreate.length() > 0 || illegalOrphansInEdit.length() > 0 || illegalOrphansInDestroy.length() > 0) {
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "java.util.ArrayList");
                    }
                    
                    if (illegalOrphansInCreate.length() > 0) {
                        illegalOrphansInCreate.insert(0, "List<String> illegalOrphanMessages = null;\n");
                        illegalOrphansInCreate.append("if (illegalOrphanMessages != null) {\n" +
                                "throw new IllegalOrphanException(illegalOrphanMessages);\n" +
                                "}\n");
                    }
                    
                    TypeElement entityType = workingCopy.getElements().getTypeElement(entityClass);
                    StringBuffer codeToPopulatePkFields = new StringBuffer();
                    if (embeddable[0]) {
                        for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(workingCopy, entityType)) {
                            if (embeddedPkSupport.isRedundantWithRelationshipField(workingCopy, entityType, pkMethod)) {
                                codeToPopulatePkFields.append(fieldName + "." +idGetterName[0] + "().s" + pkMethod.getSimpleName().toString().substring(1) + "(" +  //NOI18N
                                    fieldName + "." + embeddedPkSupport.getCodeToPopulatePkField(workingCopy, entityType, pkMethod) + ");\n");
                            }
                        }
                    }
                    
                    boolean isGenerated = JpaControllerUtil.isGenerated(workingCopy, idGetterElement, isFieldAccess);
                    bodyText = (embeddable[0] ? "if (" + fieldName + "." + idGetterName[0] + "() == null) {\n" +
                            fieldName + ".s" + idGetterName[0].substring(1) + "(new " + idClass.getSimpleName() + "());\n" + 
                            "}\n" : "") +
                            initCollectionsInCreate.toString() +
                            codeToPopulatePkFields.toString() +
                            illegalOrphansInCreate.toString() +
                            "EntityManager em = null;\n" + 
                            "try {\n " + BEGIN + "\n " + 
                            "em = getEntityManager();\n" +
                            initRelatedInCreate.toString() + "em.persist(" + fieldName + ");\n" + updateRelatedInCreate.toString() + COMMIT + "\n" +   //NOI18N
                            "} catch (Exception ex) {\n try {\n" +
                            ROLLBACK + 
                            "\n} catch (Exception re) {\n" +
                            "throw new RollbackFailureException(\"An error occurred attempting to roll back the transaction.\", re);\n" +
                            "}\n" +
                            (isGenerated ? "" : 
                            "if (find" + simpleEntityName + "(" + fieldName + "." + idGetterName[0] + "()) != null) {\n" +
                            "throw new PreexistingEntityException(\"" + simpleEntityName + " \" + " + fieldName + " + \" already exists.\", ex);\n" +
                            "}\n") +
                            "throw ex;\n" +
                            "} finally {\n if (em != null) {\nem.close();\n}\n}";
                    
                    String[] createExceptionTypes = (illegalOrphansInCreate.length() > 0 ? new String[]{exceptionPackage + ".IllegalOrphanException", exceptionPackage + ".PreexistingEntityException", exceptionPackage + ".RollbackFailureException", "java.lang.Exception"} : new String[]{exceptionPackage + ".PreexistingEntityException", exceptionPackage + ".RollbackFailureException", "java.lang.Exception"});
                    methodInfo = new MethodInfo("create", publicModifier, "void", createExceptionTypes, new String[]{entityClass}, new String[]{fieldName}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    if (illegalOrphansInEdit.length() > 0) {
                        illegalOrphansInEdit.insert(0, "List<String> illegalOrphanMessages = null;\n");
                        illegalOrphansInEdit.append("if (illegalOrphanMessages != null) {\n" +
                                "throw new IllegalOrphanException(illegalOrphanMessages);\n" +
                                "}\n");
                    }          
                    
                    bodyText = codeToPopulatePkFields.toString() +
                        "EntityManager em = null;\n" + 
                        "try {\n " + BEGIN + "\n" + 
                        "em = getEntityManager();\n" +
                        updateRelatedInEditPre.toString() + illegalOrphansInEdit.toString() + attachRelatedInEdit.toString() +
                        fieldName + " = em.merge(" + fieldName + ");\n " + 
                        updateRelatedInEditPost.toString() + COMMIT + "\n" +   //NOI18N
                        "} catch (Exception ex) {\n try {\n" +
                        ROLLBACK + 
                        "\n} catch (Exception re) {\n" +
                        "throw new RollbackFailureException(\"An error occurred attempting to roll back the transaction.\", re);\n" +
                        "}\n" +
                        "String msg = ex.getLocalizedMessage();\n" + 
                        "if (msg == null || msg.length() == 0) {\n" +
                        simpleIdPropertyType + " id = " + fieldName + "." + idGetterName[0] + "();\n" +
                        "if (find" + simpleEntityName + "(id) == null) {\n" +
                        "throw new NonexistentEntityException(\"The " + simpleEntityName.substring(0, 1).toLowerCase() + simpleEntityName.substring(1) + " with id \" + id + \" no longer exists.\");\n" +
                        "}\n" +
                        "}\n" +
                        "throw ex;\n} " +   //NOI18N
                        "finally {\n if (em != null) {\nem.close();\n}\n }";
                    String[] editExceptionTypes = (illegalOrphansInEdit.length() > 0 ? new String[]{exceptionPackage + ".IllegalOrphanException", exceptionPackage + ".NonexistentEntityException", exceptionPackage + ".RollbackFailureException", "java.lang.Exception"} : new String[]{exceptionPackage + ".NonexistentEntityException", exceptionPackage + ".RollbackFailureException", "java.lang.Exception"});
                    methodInfo = new MethodInfo("edit", publicModifier, "void", editExceptionTypes, new String[]{entityClass}, new String[]{fieldName}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    if (illegalOrphansInDestroy.length() > 0) {
                        illegalOrphansInDestroy.insert(0, "List<String> illegalOrphanMessages = null;\n");
                        illegalOrphansInDestroy.append("if (illegalOrphanMessages != null) {\n" +
                                "throw new IllegalOrphanException(illegalOrphanMessages);\n" +
                                "}\n");
                    }
                    
                    String refOrMergeStringInDestroy = "em.merge(" + fieldName + ");\n";
                    if (idGetterElement != null) {
                        refOrMergeStringInDestroy = "em.getReference(" + simpleEntityName + ".class, id);\n";
                    }
                    bodyText = "EntityManager em = null;\n" + 
                        "try {\n " + BEGIN + "\n" + 
                        "em = getEntityManager();\n" +
                        simpleEntityName + " " + fieldName + ";\n" +
                        "try {\n " + 
                        fieldName + " = " + refOrMergeStringInDestroy + 
                        fieldName + "." + idGetterName[0] + "();\n" +
                        "} catch (EntityNotFoundException enfe) {\n" +
                        "throw new NonexistentEntityException(\"The " + fieldName + " with id \" + id + \" no longer exists.\", enfe);\n" +
                        "}\n" + 
                        illegalOrphansInDestroy.toString() +
                        updateRelatedInDestroy.toString() + 
                        "em.remove(" + fieldName + ");\n " + COMMIT + "\n" +   //NOI18N
                        "} catch (Exception ex) {\n" +
                        "try {\n" +
                        ROLLBACK + 
                        "\n} catch (Exception re) {\n" +
                        "throw new RollbackFailureException(\"An error occurred attempting to roll back the transaction.\", re);\n" +
                        "}\n" +
                        "throw ex;\n" +
                        "} finally {\n if (em != null) {\nem.close();\n}\n }";  //NOI18N
                    String[] destroyExceptionTypes = (illegalOrphansInDestroy.length() > 0 ? new String[]{exceptionPackage + ".IllegalOrphanException", exceptionPackage + ".NonexistentEntityException", exceptionPackage + ".RollbackFailureException", "java.lang.Exception"} : new String[]{exceptionPackage + ".NonexistentEntityException", exceptionPackage + ".RollbackFailureException", "java.lang.Exception"});
                    methodInfo = new MethodInfo("destroy", publicModifier, "void", destroyExceptionTypes, idPropertyType, new String[]{"id"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                    
                    TypeInfo listOfEntityType = new TypeInfo("java.util.List", new String[]{entityClass});
                    
                    bodyText = "return find" + simpleEntityName + "Entities(true, -1, -1);";
                    methodInfo = new MethodInfo("find" + simpleEntityName + "Entities", publicModifier, listOfEntityType, null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return find" + simpleEntityName + "Entities(false, maxResults, firstResult);";
                    methodInfo = new MethodInfo("find" + simpleEntityName + "Entities", publicModifier, listOfEntityType, null, TypeInfo.fromStrings(new String[]{"int", "int"}), new String[]{"maxResults", "firstResult"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
                        "Query q = em.createQuery(\"select object(o) from " + simpleEntityName +" as o\");\n" + 
                        "if (!all) {\n" +
                        "q.setMaxResults(maxResults);\n" + 
                        "q.setFirstResult(firstResult);\n" + 
                        "}\n" +
                        "return q.getResultList();\n" + 
                        "} finally {\n em.close();\n}\n";
                    methodInfo = new MethodInfo("find" + simpleEntityName + "Entities", privateModifier, listOfEntityType, null, TypeInfo.fromStrings(new String[]{"boolean", "int", "int"}), new String[]{"all", "maxResults", "firstResult"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    

                    //getter for converter
                    bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
                        "return em.find(" + simpleEntityName + ".class, id);\n" + 
                        "} finally {\n em.close();\n}\n";
                    methodInfo = new MethodInfo("find" + simpleEntityName, publicModifier, entityClass, null, idPropertyType, new String[]{"id"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    
                    bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
                        "return ((Long) em.createQuery(\"select count(o) from " + simpleEntityName + " as o\").getSingleResult()).intValue();\n" + 
                        "} finally {\n em.close();\n}";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "Count", publicModifier, "int", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            }).commit();
    
        return controllerFileObject;
    }
    
    //mbohm: probably needs to be private; make public temporarily during refactoring.
    public static String getRefOrMergeString(ExecutableElement relIdGetterElement, String relFieldToAttach) {
        String refOrMergeString = "em.merge(" + relFieldToAttach + ");\n";
        if (relIdGetterElement != null) {
            String relIdGetter = relIdGetterElement.getSimpleName().toString();
            refOrMergeString = "em.getReference(" + relFieldToAttach + ".getClass(), " + relFieldToAttach + "." + relIdGetter + "());\n";
        }
        return refOrMergeString;
    }
}

