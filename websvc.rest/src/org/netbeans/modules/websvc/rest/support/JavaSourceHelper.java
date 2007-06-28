/*
 * JavaSourceHelper.java
 *
 * Created on March 19, 2007, 11:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.support;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.rmi.CORBA.Util;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
//import org.netbeans.api.java.source.query.Query;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Parameters;

/**
 *
 * @author PeterLiu
 */
public class JavaSourceHelper {
    
    static final String CLASS_TEMPLATE = "Templates/Classes/Class.java"; // NOI18N
    static final String INTERFACE_TEMPLATE = "Templates/Classes/Interface.java"; // NOI18N
    static final String JAVA_EXT = "java";                  //NOI18N
    
    public static List<JavaSource> getJavaSources(Project project) {
        List<JavaSource> result = new ArrayList<JavaSource>();
        SourceGroup[] groups = SourceGroupSupport.getJavaSourceGroups(project);
        
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            Enumeration<? extends FileObject> files = root.getData(true);
            
            while(files.hasMoreElements()) {
                FileObject fobj = files.nextElement();
                
                if (fobj.getExt().equals(JAVA_EXT)) {
                    JavaSource source = JavaSource.forFileObject(fobj);
                    result.add(source);
                }
            }
        }
        
        return result;
    }
    
    public static List<JavaSource> getEntityClasses(Project project) {
        List<JavaSource> sources = getJavaSources(project);
        List<JavaSource> entityClasses = new ArrayList<JavaSource>();
        
        for (JavaSource source : sources) {
            if (isEntity(source)) {
                entityClasses.add(source);
            }
        }
        
        return entityClasses;
    }
    
    public static boolean isEntity(JavaSource source) {
        final boolean[] isBoolean = new boolean[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller)
                        throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    
                    TypeElement classElement = getTopLevelClassElement(controller);
                    if (classElement == null) {
                        return;
                    }
                    
                    List<? extends AnnotationMirror> annotations =
                            controller.getElements().getAllAnnotationMirrors(classElement);
                    
                    for (AnnotationMirror annotation : annotations) {
                        if (annotation.toString().equals("@javax.persistence.Entity")) {    //NOI18N
                            isBoolean[0] = true;
                            
                            break;
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return isBoolean[0];
    }
    
    /**
     *
     * @param source
     * @return
     */
    public static String getClassNameQuietly(JavaSource source) {
        try {
            return getClassName(source);
        } catch(IOException ioe) {
            Logger.getLogger(JavaSourceHelper.class.getName()).log(Level.WARNING, ioe.getLocalizedMessage());
        }
        return null;
    }
    public static String getClassName(JavaSource source) throws IOException {
        return getTypeElement(source).getSimpleName().toString();
    }
    
    public static String getClassType(JavaSource source) throws IOException {
        return getTypeElement(source).getQualifiedName().toString();
    }
    
    public static String getPackageName(JavaSource source) {
        final String[] packageName = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ExpressionTree packageTree = controller.getCompilationUnit().getPackageName();
                    packageName[0] = packageTree.toString();
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return packageName[0];
    }
    
    public static String getIdFieldName(JavaSource source) {
        final String[] fieldName = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    TypeElement classElement = getTopLevelClassElement(controller);
                    List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());
                    
                    for (VariableElement field : fields) {
                        List<? extends AnnotationMirror> annotations = field.getAnnotationMirrors();
                        
                        for (AnnotationMirror annotation : annotations) {
                            if (annotation.toString().equals("@javax.persistence.Id")) {     //NOI18N
                                fieldName[0] = field.getSimpleName().toString();
                                return;
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return fieldName[0];
    }
    
    
    
    public static ClassTree getTopLevelClassTree(CompilationController controller) {
        String className = controller.getFileObject().getName();
        
        CompilationUnitTree cu = controller.getCompilationUnit();
        if (cu != null) {
            List<? extends Tree> decls = cu.getTypeDecls();
            for (Tree decl : decls) {
                if (decl.getKind() != Tree.Kind.CLASS) {
                    continue;
                }
                
                ClassTree classTree = (ClassTree) decl;
                
                if (classTree.getSimpleName().contentEquals(className) &&
                        classTree.getModifiers().getFlags().contains(Modifier.PUBLIC))
                    return classTree;
            }
        }
        return null;
    }
    
    public static TypeElement getTopLevelClassElement(CompilationController controller) {
        ClassTree classTree = getTopLevelClassTree(controller);
        if (classTree == null) {
            return null;
        }
        Trees trees = controller.getTrees();
        TreePath path = trees.getPath(controller.getCompilationUnit(), classTree);
        
        return (TypeElement) trees.getElement(path);
    }
    
    public static MethodTree getDefaultConstructor(CompilationController controller) {
        TypeElement classElement = getTopLevelClassElement(controller);
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(classElement.getEnclosedElements());
        
        for (ExecutableElement constructor : constructors) {
            if (constructor.getParameters().size() == 0) {
                return controller.getTrees().getTree(constructor);
            }
        }
        
        return null;
    }
    
    public static MethodTree getMethodByName(CompilationController controller, String methodName) {
        TypeElement classElement = getTopLevelClassElement(controller);
        List<ExecutableElement> methods = ElementFilter.methodsIn(classElement.getEnclosedElements());
        List<MethodTree> found = new ArrayList<MethodTree>();
        for (ExecutableElement method : methods) {
            if (method.getSimpleName().toString().equals(methodName)) {
                found.add(controller.getTrees().getTree(method));
            }
        }
        if (found.size() > 1) {
            throw new IllegalArgumentException("Unexpected overloading methods of '"+ methodName + "' found.");
        } else if (found.size() == 1) {
            return found.get(0);
        }
        return null;
    }
    
    public static VariableTree getField(CompilationController controller, String fieldName) {
        TypeElement classElement = getTopLevelClassElement(controller);
        List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());
        
        for (VariableElement field : fields) {
            if (field.getSimpleName().toString().equals(fieldName)) {
                return (VariableTree) controller.getTrees().getTree(field);
            }
        }
        
        return null;
    }
    
    public static JavaSource createJavaSource(FileObject targetFolder,
            String packageName, String className) {
        return createJavaSource(CLASS_TEMPLATE, targetFolder,
                packageName, className);
    }
    
    public static JavaSource createJavaSource(String template, FileObject targetFolder,
            String packageName, String className) {
        try {
            FileObject fobj = createDataObjectFromTemplate(template,
                    targetFolder, packageName, className).getPrimaryFile();
            return JavaSource.forFileObject(fobj);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return null;
    }
    
    private static DataObject createDataObjectFromTemplate(String template,
            FileObject targetFolder, String packageName, String targetName) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() >  0;
        
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("package", packageName);
        
        return templateDO.createFromTemplate(dataFolder, targetName, params);
    }
    
    public static void addClassAnnotation(WorkingCopy copy, String[] annotations,
            Object[] annotationAttrs) {
        TreeMaker maker = copy.getTreeMaker();
        ClassTree tree = getTopLevelClassTree(copy);
        
        ModifiersTree modifiers = tree.getModifiers();
        
        for (int i = 0; i < annotations.length; i++) {
            List<ExpressionTree> attrTrees = null;
            Object attr = annotationAttrs[i];
            
            if (attr != null) {
                attrTrees = new ArrayList<ExpressionTree>();
                
                if (attr instanceof ExpressionTree) {
                    attrTrees.add((ExpressionTree) attr);
                } else {
                    attrTrees.add(maker.Literal(attr));
                }
            } else {
                attrTrees = Collections.<ExpressionTree>emptyList();
            }
            
            AnnotationTree newAnnotation = maker.Annotation(
                    maker.Identifier(annotations[i]),
                    attrTrees);
            
            if (modifiers != null) {
                modifiers = maker.addModifiersAnnotation(
                        modifiers, newAnnotation);
            }
        }
        
        copy.rewrite(tree.getModifiers(), modifiers);
    }
    
    public static void addImports(WorkingCopy copy, String[] imports) {
        TreeMaker maker = copy.getTreeMaker();
        
        CompilationUnitTree tree = copy.getCompilationUnit();
        CompilationUnitTree modifiedTree = tree;
        
        for (String imp : imports) {
            modifiedTree = maker.addCompUnitImport(modifiedTree,
                    maker.Import(maker.Identifier(imp), false));
        }
        
        copy.rewrite(tree, modifiedTree);
    }
    
    public static ClassTree addField(WorkingCopy copy, ClassTree tree,
            Modifier[] modifiers, String[] annotations, Object[] annotationAttrs,
            String name, Object type) {
        return addField(copy, tree, modifiers, annotations, annotationAttrs, name, type, null);
    }
    
    public static ClassTree addField(WorkingCopy copy, ClassTree tree,
            Modifier[] modifiers, String[] annotations, Object[] annotationAttrs,
            String name, Object type, Object initialValue) {
        
        TreeMaker maker = copy.getTreeMaker();
        ClassTree modifiedTree = tree;
        
        Tree typeTree = createTypeTree(copy, type);
        
        ModifiersTree modifiersTree = createModifiersTree(copy, modifiers,
                annotations, annotationAttrs);
        
        ExpressionTree init = initialValue == null ? null : maker.Literal(initialValue);
        
        VariableTree variableTree = maker.Variable(modifiersTree, name,
                typeTree, init);
        
        return maker.insertClassMember(modifiedTree, 0, variableTree);
    }
    
    public static void addFields(WorkingCopy copy, String[] names, Object[] types) {
        Object[] initValues = new Object[types.length];
        for (int i=0; i<types.length; i++) {
            Object type = types[i];
            if (String.class.equals(type) || String.class.getName().equals(type)) {
                initValues[i] = "";
            } else if (type instanceof Class && Number.class.isAssignableFrom((Class)type)) {
                initValues[i] = 0;
            } else {
                initValues[i] = null;
            }
        }
        addFields(copy, names, types, initValues);
    }
    
    public static void addFields(WorkingCopy copy,
            String[] names, Object[] types, Object[] initialValues) {
        
        TreeMaker maker = copy.getTreeMaker();
        ClassTree classTree = getTopLevelClassTree(copy);
        ClassTree modifiedTree = classTree;
        Modifier[] modifiers = Constants.PRIVATE;
        String[] annotations = new String[0];
        Object[] annotationAttrs = new Object[0];
        
        for (int i=0; i<names.length; i++) {
            String name = names[i];
            Object type = types[i];
            Object initialValue = initialValues[i];
            Tree typeTree = createTypeTree(copy, type);
            
            ModifiersTree modifiersTree = createModifiersTree(copy, modifiers,
                    annotations, annotationAttrs);
            ExpressionTree init = initialValue == null ? null : maker.Literal(initialValue);
            VariableTree variableTree = maker.Variable(modifiersTree, name,
                    typeTree, init);
            modifiedTree = maker.insertClassMember(modifiedTree, 0, variableTree);
        }
        copy.rewrite(classTree, modifiedTree);
    }
    
    public static void addConstructor(WorkingCopy copy, String[] parameters, Object[] paramTypes) {
        ClassTree classTree = getTopLevelClassTree(copy);
        String bodyText = "{" + getThisFieldEqualParamStatements(parameters) + "}"; //NOI18N
        String comment = "Create an instance of " + classTree.getSimpleName().toString();
        ClassTree modifiedTree = addConstructor(copy, classTree, Constants.PUBLIC, parameters, paramTypes, bodyText, comment);
        copy.rewrite(classTree, modifiedTree);
    }
    
    public static String getThisFieldEqualParamStatements(String[] params) {
        StringBuilder sb = new StringBuilder();
        String template = "this.$PARAM$ = $PARAM$;\n"; //NOI18N
        for (int i=0; i<params.length; i++) {
            sb.append(template.replace("$PARAM$", params[i])); //NOI18N
        }
        return sb.toString();
    }
    
    public static String getParamEqualThisFieldStatements(String[] params, String[] paramTypes) {
        StringBuilder sb = new StringBuilder();
        String template = "if ($PARAM$ == null) { $PARAM$ = this.$PARAM$; }\n"; //NOI18N
        
        for (int i=0; i<params.length; i++) {
            if (isNotPrimitive(paramTypes[i])) {
                sb.append(template.replace("$PARAM$", params[i])); //NOI18N
            }
        }
        return sb.toString();
    }
    
    public static boolean isNotPrimitive(String qualifiedTypeName) {
        return qualifiedTypeName.indexOf('.') > 0;
    }
    
    public static ClassTree addConstructor(WorkingCopy copy, ClassTree tree,
            Modifier[] modifiers, String[] parameters,
            Object[] paramTypes, String bodyText, String comment) {
        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree modifiersTree = createModifiersTree(copy, modifiers, null, null);
        ModifiersTree paramModTree = maker.Modifiers(Collections.<Modifier>emptySet());
        List<VariableTree> paramTrees = new ArrayList<VariableTree>();
        
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                paramTrees.add(maker.Variable(paramModTree,
                        parameters[i], createTypeTree(copy, paramTypes[i]), null));
            }
        }
        
        MethodTree methodTree = maker.Constructor(modifiersTree,
                Collections.<TypeParameterTree>emptyList(),
                paramTrees,
                Collections.<ExpressionTree>emptyList(),
                bodyText);
        
        if (comment != null) {
            maker.addComment(methodTree, createJavaDocComment(comment), true);
        }
        
        return maker.addClassMember(tree, methodTree);
    }
    
    public static void replaceFieldValue(WorkingCopy copy, VariableTree tree,
            String value) {
        TreeMaker maker = copy.getTreeMaker();
        
        VariableTree modifiedTree = maker.Variable(
                tree.getModifiers(),
                tree.getName(),
                tree.getType(),
                maker.Literal(value));
        
        copy.rewrite(tree, modifiedTree);
    }
    
    public static void replaceMethodBody(WorkingCopy copy, MethodTree tree,
            String body) {
        TreeMaker maker = copy.getTreeMaker();
        MethodTree modifiedTree = maker.Method(
                tree.getModifiers(),
                tree.getName(),
                tree.getReturnType(),
                tree.getTypeParameters(),
                tree.getParameters(),
                tree.getThrows(),
                body,
                null);
        
        copy.rewrite(tree, modifiedTree);
    }
    
    public static ClassTree addMethod(WorkingCopy copy, ClassTree tree,
            Modifier[] modifiers, String[] annotations, Object[] annotationAttrs,
            String name, Object returnType,
            String[] parameters, Object[] paramTypes,
            String[] paramAnnotations, Object[] paramAnnotationAttrs,
            String bodyText, String comment) {
        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree modifiersTree = createModifiersTree(copy, modifiers,
                annotations, annotationAttrs);
        
        Tree returnTypeTree = createTypeTree(copy, returnType);
        
        List<VariableTree> paramTrees = new ArrayList<VariableTree>();
        
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                ModifiersTree paramModTree = maker.Modifiers(Collections.<Modifier>emptySet());
                
                if (paramAnnotations != null && i < paramAnnotations.length) {
                    String annotation = paramAnnotations[i];
                    Object annotationAttr = paramAnnotationAttrs[i];
                    
                    if (annotation != null) {
                        paramModTree = createModifiersTree(copy, new Modifier[]{},
                                new String[] {annotation}, new Object[] {annotationAttr});
                    }
                }
                
                paramTrees.add(maker.Variable(paramModTree,
                        parameters[i], createTypeTree(copy, paramTypes[i]), null));
            }
        }
        
        
        MethodTree methodTree = maker.Method(modifiersTree,
                name, returnTypeTree,
                Collections.<TypeParameterTree>emptyList(),
                paramTrees,
                Collections.<ExpressionTree>emptyList(),
                bodyText,
                null);
        
        if (comment != null) {
            maker.addComment(methodTree, createJavaDocComment(comment), true);
        }
        
        return maker.addClassMember(tree, methodTree);
    }
    
    public static AssignmentTree createAssignmentTree(WorkingCopy copy, String variable,
            Object value) {
        TreeMaker maker = copy.getTreeMaker();
        
        return maker.Assignment(maker.Identifier(variable), maker.Literal(value));
    }
    
    private static Tree createTypeTree(WorkingCopy copy, Object type) {
        if (type instanceof String) {
            TypeElement element = copy.getElements().getTypeElement((String) type);
            if (element != null) {
                return copy.getTreeMaker().QualIdent(element);
            } else {
                return copy.getTreeMaker().Identifier((String) type);
            }
        } else {
            return (Tree) type;
        }
    }
    
    public static Tree createIdentifierTree(WorkingCopy copy, String value) {
        return copy.getTreeMaker().Identifier(value);
    }
    
    public static Tree createParameterizedTypeTree(WorkingCopy copy,
            String type, String[] typeArgs) {
        TreeMaker maker = copy.getTreeMaker();
        Tree typeTree = createTypeTree(copy, type);
        List<ExpressionTree> typeArgTrees = new ArrayList<ExpressionTree>();
        
        for (String arg : typeArgs) {
            typeArgTrees.add((ExpressionTree)  createTypeTree(copy, arg));
        }
        
        return maker.ParameterizedType(typeTree, typeArgTrees);
    }
    
    private static ModifiersTree createModifiersTree(WorkingCopy copy,
            Modifier[] modifiers, String[] annotations,
            Object[] annotationAttrs) {
        TreeMaker maker = copy.getTreeMaker();
        Set<Modifier> modifierSet = new HashSet<Modifier>();
        
        for (Modifier modifier : modifiers) {
            modifierSet.add(modifier);
        }
        
        List<AnnotationTree> annotationTrees = createAnnotationTrees(copy,
                annotations, annotationAttrs);
        
        return maker.Modifiers(modifierSet, annotationTrees);
    }
    
    private static List<AnnotationTree> createAnnotationTrees(WorkingCopy copy,
            String[] annotations, Object[] annotationAttrs) {
        TreeMaker maker = copy.getTreeMaker();
        List<AnnotationTree> annotationTrees = null;
        
        if (annotations != null) {
            annotationTrees = new ArrayList<AnnotationTree>();
            
            for (int i = 0; i < annotations.length; i++) {
                String annotation = annotations[i];
                
                List<ExpressionTree> expressionTrees = Collections.<ExpressionTree>emptyList();
                
                if (annotationAttrs != null) {
                    Object attr = annotationAttrs[i];
                    
                    if (attr != null) {
                        expressionTrees = new ArrayList<ExpressionTree>();
                        
                        if (attr instanceof ExpressionTree) {
                            expressionTrees.add((ExpressionTree) attr);
                        } else {
                            expressionTrees.add(maker.Literal(attr));
                        }
                    }
                }
                
                annotationTrees.add(maker.Annotation(maker.Identifier(annotation),
                        expressionTrees));
            }
        } else {
            annotationTrees = Collections.<AnnotationTree>emptyList();
        }
        
        return annotationTrees;
    }
    
    private static Comment createJavaDocComment(String text) {
        
        return Comment.create(Style.JAVADOC, -2, -2, -2, text);
    }
    
    /**
     * Finds the first public top-level type in the compilation unit given by the
     * given <code>CompilationController</code>.
     *
     * This method assumes the restriction that there is at most a public
     * top-level type declaration in a compilation unit, as described in the
     * section 7.6 of the JLS.
     */
    public static ClassTree findPublicTopLevelClass(CompilationController controller) throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);
        
        final String mainElementName = controller.getFileObject().getName();
        for (Tree tree : controller.getCompilationUnit().getTypeDecls()) {
            if (tree.getKind() != Tree.Kind.CLASS) {
                continue;
            }
            ClassTree classTree = (ClassTree)tree;
            if (!classTree.getSimpleName().contentEquals(mainElementName)) {
                continue;
            }
            if (!classTree.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                continue;
            }
            return classTree;
        }
        return null;
    }
    
    public static boolean isInjectionTarget(CompilationController controller) throws IOException {
        Parameters.notNull("controller", controller); // NOI18N
        
        ClassTree classTree = findPublicTopLevelClass(controller);
        if (classTree == null) {
            throw new IllegalArgumentException();
        }
        
        return isInjectionTarget(controller, getTypeElement(controller, classTree));
    }
    
    public static boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        FileObject fo = controller.getFileObject();
        Project project = FileOwnerQuery.getOwner(fo);
        if (ElementKind.INTERFACE != typeElement.getKind()) {
            List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
            boolean found = false;
            
            for (AnnotationMirror m : annotations) {
                Name qualifiedName = ((TypeElement)m.getAnnotationType().asElement()).getQualifiedName();
                if (qualifiedName.contentEquals("javax.jws.WebService")) { //NOI18N
                    found = true;
                    break;
                }
                if (qualifiedName.contentEquals("javax.jws.WebServiceProvider")) { //NOI18N
                    found = true;
                    break;
                }
            }
            if (found) return true;
        }
        return false;
    }
    
    public static TypeElement getTypeElement(CompilationController controller, ClassTree classTree) {
        TreePath classTreePath = controller.getTrees().getPath(controller.getCompilationUnit(), classTree);
        return (TypeElement) controller.getTrees().getElement(classTreePath);
    }
    
    public static TypeElement getTypeElement(CompilationController controller, TreePath treePath) {
        return (TypeElement) controller.getTrees().getElement(treePath);
    }
    
    public static ClassTree getClassTree(CompilationController controller, TypeElement typeElement) {
        return controller.getTrees().getTree(typeElement);
    }
    
    public static void saveSource(FileObject[] files) throws IOException {
        for (FileObject f : files) {
            try {
                DataObject dobj = DataObject.find(f);
                SaveCookie sc = dobj.getCookie(SaveCookie.class);
                if (sc != null) {
                    sc.save();
                }
            } catch(DataObjectNotFoundException dex) {
                // something really wrong but continue trying to save others
            }
        }
    }
    
    public static boolean isOfAnnotationType(AnnotationMirror am, String annotationType) {
        return am.getAnnotationType().asElement().getSimpleName().contentEquals(annotationType);
    }
    
    public static AnnotationMirror findAnnotation(List<? extends AnnotationMirror> anmirs, String annotationString) {
        for (AnnotationMirror am : anmirs) {
            if (isOfAnnotationType(am, annotationString)) {
                return am;
            }
        }
        return null;
    }
    
    public static boolean annotationHasAttributeValue(AnnotationMirror am, String attr, String value) {
        return value.equals(am.getElementValues().get(attr));
    }
    
    public static boolean annotationHasAttributeValue(AnnotationMirror am, String value) {
        for (AnnotationValue av : am.getElementValues().values()) {
            if (value.equals(av.getValue())) {
                return true;
            }
        }
        return false;
    }
    
    public static TypeElement getXmlRepresentationClass(TypeElement typeElement) {
        List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
        for (ExecutableElement method : methods) {
            List<? extends AnnotationMirror> anmirs = method.getAnnotationMirrors();
            
            AnnotationMirror mirrorHttpMethod = findAnnotation(anmirs, Constants.HTTP_METHOD_ANNOTATION);
            AnnotationMirror mirrorProduceMime = findAnnotation(anmirs, Constants.PRODUCE_MIME_ANNOTATION);
            
            if (annotationHasAttributeValue(mirrorHttpMethod, Constants.HTTP_GET_METHOD) &&
                    annotationHasAttributeValue(mirrorProduceMime, Constants.MIME_TYPE_XML)) {
                TypeMirror tm = method.getReturnType();
                if (tm.getKind() == TypeKind.DECLARED) {
                    return (TypeElement) ((DeclaredType) tm).asElement();
                }
            }
        }
        return null;
    }
    
    public static TypeElement getTypeElement(JavaSource source) throws IOException {
        final TypeElement[] results = new TypeElement[1];
        
        source.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                results[0] = getTopLevelClassElement(controller);
            }
        }, true);
        
        return results[0];
    }
    
    public static JavaSource forTypeElement(TypeElement typeElement, Project project) throws IOException {
        for (JavaSource js : getJavaSources(project)) {
            String className = getClassType(js);
            if (typeElement.getQualifiedName().contentEquals(className)) {
                return js;
            }
        }
        return null;
    }
}
