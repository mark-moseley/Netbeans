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

package org.netbeans.modules.xml.tools.java.generator;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;

/**
 * <code>GenerationUtils</code> is a helper class for creating classes,
 * methods, variables, annotations and types using the Java source model.
 *
 * @author Andrei Badea
 */
public final class GenerationUtils {

    // PENDING use CharSequence instead of String where possible

    /**
     * The templates for regular Java class and interface.
     */
    static final String CLASS_TEMPLATE = "Templates/Classes/Class.java"; // NOI18N
    static final String INTERFACE_TEMPLATE = "Templates/Classes/Interface.java"; // NOI18N

    private final WorkingCopy copy;

    // <editor-fold desc="Constructors and factory methods">

    private GenerationUtils(WorkingCopy copy) {
        this.copy = copy;
    }

    /**
     * Creates a new instance of <code>GenerationUtils</code>.
     *
     * @param  copy a <code>WorkingCopy</code>. It must be in {@link Phase#RESOLVED}.
     * @return a new instance of <code>GenerationUtils</code>.
     */
    public static GenerationUtils newInstance(WorkingCopy copy) {
        Parameters.notNull("copy", copy); // NOI18N
        return new GenerationUtils(copy);
    }

    // </editor-fold>

    // <editor-fold desc="Public static methods">

    /**
     * Creates a new Java class based on the default template for classes.
     *
     * @param  targetFolder the folder the new class should be created in;
     *         cannot be null.
     * @param  className the name of the new class (a valid Java identifier);
     *         cannot be null.
     * @param  javadoc the new class's Javadoc; can be null.
     * @return the FileObject for the new Java class; never null.
     * @throws IOException if an error occurred while creating the class.
     */
    public static FileObject createClass(FileObject targetFolder, String className, final String javadoc) throws IOException{
        return createClass(CLASS_TEMPLATE, targetFolder, className, javadoc, Collections.emptyMap());
    }

    /**
     * Creates a new Java class based on the provided template.
     *
     * @param  template the template to base the new class on.
     * @param  targetFolder the folder the new class should be created in;
     *         cannot be null.
     * @param  className the name of the new class (a valid Java identifier);
     *         cannot be null.
     * @param  javadoc the new class's Javadoc; can be null.
     * @param  parameters map of named objects that are going to be used when creating the new object
     * @return the FileObject for the new Java class; never null.
     * @throws IOException if an error occurred while creating the class.
     */
    public static FileObject createClass(String template, FileObject targetFolder, String className, final String javadoc, Map parameters) throws IOException {
        Parameters.notNull("template", template); // NOI18N
        Parameters.notNull("targetFolder", targetFolder); // NOI18N
        Parameters.javaIdentifier("className", className); // NOI18N

        FileObject classFO = createDataObjectFromTemplate(template, targetFolder, className, parameters).getPrimaryFile();
        // JavaSource javaSource = JavaSource.forFileObject(classFO);
        // final boolean[] commit = { false };
        // ModificationResult modification = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
        //     public void run(WorkingCopy copy) throws IOException {
        //         GenerationUtils genUtils = GenerationUtils.newInstance(copy);
        //         if (javadoc != null) {
        //             genUtils.setJavadoc(copy, mainType, javadoc);
        //         }
        //     }
        // });
        // if (commit[0]) {
        //     modification.commit();
        // }

        return classFO;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public static methods">

    /**
     * Creates a data object from a given template path in the system
     * file system.
     *
     * @return the <code>DataObject</code> of the newly created file.
     * @throws IOException if an error occured while creating the file.
     */
    private static DataObject createDataObjectFromTemplate(String template, FileObject targetFolder, String targetName,
            Map parameters) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() >  0;

        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        return templateDO.createFromTemplate(dataFolder, targetName, parameters);
    }

    // </editor-fold>

    // <editor-fold desc="Public methods">

    public Tree createType(String typeName, TypeElement scope) {
        TreeMaker make = getTreeMaker();
        TypeKind primitiveTypeKind = null;
        if ("boolean".equals(typeName)) {           // NOI18N
            primitiveTypeKind = TypeKind.BOOLEAN;
        } else if ("byte".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.BYTE;
        } else if ("short".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.SHORT;
        } else if ("int".equals(typeName)) {        // NOI18N
            primitiveTypeKind = TypeKind.INT;
        } else if ("long".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.LONG;
        } else if ("char".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.CHAR;
        } else if ("float".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.FLOAT;
        } else if ("double".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.DOUBLE;
        } else if ("void".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.VOID;
        }
        if (primitiveTypeKind != null) {
            return getTreeMaker().PrimitiveType(primitiveTypeKind);
        }
        Tree typeTree = tryCreateQualIdent(typeName);
        if (typeTree == null) {
            // XXX does not handle imports; temporary until issue 102149 is fixed
            TypeMirror typeMirror = copy.getTreeUtilities().parseType(typeName, scope);
            typeTree = make.Type(typeMirror);
        }
        return typeTree;
    }

    public ModifiersTree createModifiers(Modifier modifier) {
        return getTreeMaker().Modifiers(EnumSet.of(modifier), Collections.emptyList());
    }

    
    public ClassTree ensureNoArgConstructor(ClassTree classTree) {
        TypeElement typeElement = SourceUtils.classTree2TypeElement(copy, classTree);
        if (typeElement == null) {
            throw new IllegalArgumentException("No TypeElement for ClassTree " + classTree.getSimpleName());
        }
        ExecutableElement constructor = SourceUtils.getNoArgConstructor(copy, typeElement);
        MethodTree constructorTree = constructor != null ? copy.getTrees().getTree(constructor) : null;
        MethodTree newConstructorTree = null;
        TreeMaker make = getTreeMaker();
        if (constructor != null) {
            if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                ModifiersTree oldModifiersTree = constructorTree.getModifiers();
                Set newModifiers = EnumSet.of(Modifier.PUBLIC);
           //     for (Modifier modifier : oldModifiersTree.getFlags()) {
             //       if (!Modifier.PROTECTED.equals(modifier) && !Modifier.PRIVATE.equals(modifier)) {
               //         newModifiers.add(modifier);
                 //   }
                //}
                newConstructorTree = make.Constructor(
                    make.Modifiers(newModifiers),
                    constructorTree.getTypeParameters(),
                    constructorTree.getParameters(),
                    constructorTree.getThrows(),
                    constructorTree.getBody());
            }
        } else {
            newConstructorTree = make.Constructor(
                    createModifiers(Modifier.PUBLIC),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "{ }"); // NOI18N
        }
        ClassTree newClassTree = classTree;
        if (newConstructorTree != null) {
            if (constructorTree != null) {
                newClassTree = make.removeClassMember(newClassTree, constructorTree);
            }
            newClassTree = make.addClassMember(newClassTree, newConstructorTree);
        }
        return newClassTree;
    }

    /**
     * Creates a constructor which assigns its parameters to fields with the
     * same names. For example it can be used to generate:
     *
     * <pre>
     * public void Constructor(String field1, Object field2) {
     *     this.field1 = field1;
     *     this.field2 = field2;
     * }
     * </pre>
     *
     * @param  modifiersTree the constructor modifiers.
     * @param  constructorName the constructor name; cannot be null.
     * @param  parameters the constructor parameters; cannot be null.
     * @return the new constructor; never null.
     */
    public MethodTree createAssignmentConstructor(ModifiersTree modifiersTree, String constructorName, List parameters) {
        Parameters.notNull("modifiersTree", modifiersTree);
        Parameters.javaIdentifier("constructorName", constructorName); // NOI18N
        Parameters.notNull("parameters", parameters); // NOI18N

        StringBuilder body = new StringBuilder(parameters.size() * 30);
        body.append("{"); // NOI18N
        for(int i=0; i < parameters.size();i++ ) {
            VariableTree parameter =  (VariableTree)parameters.get(i);
            String parameterName = parameter.getName().toString();
            body.append("this." + parameterName + " = " + parameterName + ";"); // NOI18N
        }
        body.append("}"); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Constructor(
                modifiersTree,
                Collections.emptyList(),
                parameters,
                Collections.emptyList(),
                body.toString());
    }

    /**
     * Creates a new field.
     *
     * @param  scope the scope in which to create the field (will be e.g. used
     *         to parse <code>fieldType</code>).
     * @param  modifiersTree the field modifiers; cannot be null.
     * @param  fieldType the fully-qualified name of the field type; cannot be null.
     * @param  fieldName the field name; cannot be null.
     * @param  expressionTree expression to initialize the field; can be null.
     * @return the new field; never null.
     */
    public VariableTree createField(TypeElement scope, ModifiersTree modifiersTree, String fieldName, String fieldType, ExpressionTree expressionTree) {
        Parameters.notNull("modifiersTree", modifiersTree); // NOI18N
        Parameters.javaIdentifier("fieldName", fieldName); // NOI18N
        Parameters.notNull("fieldType", fieldType); // NOI18N

        return getTreeMaker().Variable(
                modifiersTree,
                fieldName,
                createType(fieldType, scope),
                expressionTree);
    }

    /**
     * Creates a new variable (a <code>VariableTree</code> with no
     * modifiers nor initializer).
     *
     * @param  scope the scope in which to create the variable (will be e.g. used
     *         to parse <code>variableType</code>).
     * @param  variableType the fully-qualified name of the variable type; cannot be null.
     * @param  variableName the variable name; cannot be null.
     * @return the new variable; never null.
     */
    public VariableTree createVariable(TypeElement scope, String variableName, String variableType) {
        Parameters.javaIdentifier("variableName", variableName); // NOI18N
        Parameters.notNull("variableType", variableType); // NOI18N

        return createField(
                scope,
                createEmptyModifiers(),
                variableName,
                variableType,
                null);
    }

    /**
     * Creates a new variable (a <code>VariableTree</code> with no
     * modifiers nor initializer).
     *
     * @param  variableType the variable type; cannot be null.
     * @param  variableName the variable name; cannot be null.
     * @return the new variable; never null.
     */
    public VariableTree createVariable(String variableName, Tree variableType) {
        Parameters.javaIdentifier("variableName", variableName); // NOI18N
        Parameters.notNull("variableType", variableType); // NOI18N

        return getTreeMaker().Variable(
                createEmptyModifiers(),
                variableName,
                variableType,
                null);
    }

    
    
    /**
     * Inserts the given fields in the given class after any fields already existing
     * in the class (if any, otherwise the fields are inserted at the beginning
     * of the class).
     *
     * @param  classTree the class to add fields to; cannot be null.
     * @param  fieldTrees the fields to be added; cannot be null.
     * @return the class containing the new fields; never null.
     */
    public ClassTree addClassFields(ClassTree classTree, List fieldTrees) {
        Parameters.notNull("classTree", classTree); // NOI18N
        Parameters.notNull("fieldTrees", fieldTrees); // NOI18N

        int firstNonFieldIndex = 0;
        Iterator memberTrees = classTree.getMembers().iterator();
        while (memberTrees.hasNext() && ((Tree)memberTrees.next()).getKind() == Tree.Kind.VARIABLE) {
            firstNonFieldIndex++;
        }
        TreeMaker make = getTreeMaker();
        ClassTree newClassTree = classTree;
        for (int i=0; i < fieldTrees.size(); i++ ) {
            VariableTree fieldTree =  (VariableTree)fieldTrees.get(i);        
            newClassTree = make.insertClassMember(newClassTree, firstNonFieldIndex, fieldTree);
            firstNonFieldIndex++;
        }
        return newClassTree;
    }

    // PENDING addClassConstructors(), addClassMethods()

    /**
     * Adds the specified interface to the implements clause of
     * {@link #getClassTree()}.
     *
     * @param  classTree the class to add the implements clause to.
     * @param  interfaceType the fully-qualified name of the interface; cannot be null.
     * @return the class implementing the new interface.
     */
    public ClassTree addImplementsClause(ClassTree classTree, String interfaceType) {
        Parameters.notNull("classTree", classTree); // NOI18N
        Parameters.notNull("interfaceType", interfaceType); // NOI18N

        ExpressionTree interfaceTree = createQualIdent(interfaceType);
        return getTreeMaker().addClassImplementsClause(classTree, interfaceTree);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public methods">

    private TreeMaker getTreeMaker() {
        return copy.getTreeMaker();
    }

    private ModifiersTree createEmptyModifiers() {
        return getTreeMaker().Modifiers(Collections.emptySet(), Collections.emptyList());
    }

    private ExpressionTree tryCreateQualIdent(String typeName) {
        TypeElement typeElement = copy.getElements().getTypeElement(typeName);
        if (typeElement != null) {
            return getTreeMaker().QualIdent(typeElement);
        }
        return null;

    }

    private ExpressionTree createQualIdent(String typeName) {
        ExpressionTree qualIdent = tryCreateQualIdent(typeName);
        if (qualIdent == null) {
            throw new IllegalArgumentException("Cannot create a QualIdent for " + typeName); // NOI18N
        }
        return qualIdent;
    }

    
    // </editor-fold>
}
