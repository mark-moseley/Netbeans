/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common.method;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.StatementTree;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.FakeJavaDataLoaderPool;
import org.netbeans.modules.j2ee.common.source.RepositoryImpl;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.common.source.TestUtilities;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class MethodModelSupportTest extends NbTestCase {
    
    private FileObject testFO;

    public MethodModelSupportTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaDataLoaderPool.class, RepositoryImpl.class);

        clearWorkDir();
        
        File file = new File(getWorkDir(),"cache");	//NOI18N
        file.mkdirs();
        IndexUtil.setCacheFolder(file);

        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }
    
    public void testCreateMethodModel() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "    private boolean method1() throws java.io.IOException {" +
                "        return false;" +
                "    }" +
                "    public static void method2(String name, int age, String[] interests) {" +
                "        return null;" +
                "    }" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.newInstance(controller).getTypeElement();
                for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, method);
                    if (method.getSimpleName().contentEquals("method1")) {
                        assertEquals("boolean", methodModel.getReturnType());
                        assertEquals("method1", methodModel.getName());
                        assertTrue(methodModel.getModifiers().size() == 1);
                        assertTrue(methodModel.getModifiers().contains(Modifier.PRIVATE));
                        assertTrue(methodModel.getParameters().size() == 0);
                        assertTrue(methodModel.getExceptions().size() == 1);
                        assertTrue(methodModel.getExceptions().contains("java.io.IOException"));
                        //TODO: RETOUCHE test method body, see #90926
                    } else if (method.getSimpleName().contentEquals("method2")) {
                        assertEquals("void", methodModel.getReturnType());
                        assertEquals("method2", methodModel.getName());
                        assertTrue(methodModel.getModifiers().size() == 2);
                        assertTrue(methodModel.getModifiers().contains(Modifier.PUBLIC));
                        assertTrue(methodModel.getModifiers().contains(Modifier.STATIC));
                        MethodModel.Variable nameVariable = null;
                        MethodModel.Variable ageVariable = null;
                        MethodModel.Variable interestsVariable = null;
                        List<MethodModel.Variable> variables = methodModel.getParameters();
                        assertTrue(variables.size() == 3);
                        for (MethodModel.Variable variable : variables) {
                            if ("name".equals(variable.getName())) {
                                nameVariable = variable;
                            } else if ("age".equals(variable.getName())) {
                                ageVariable = variable;
                            } else if ("interests".equals(variable.getName())) {
                                interestsVariable = variable;
                            }
                        }
                        assertNotNull(nameVariable);
                        assertNotNull(ageVariable);
                        assertNotNull(interestsVariable);
                        assertEquals("java.lang.String", nameVariable.getType());
                        assertEquals("int", ageVariable.getType());
                        assertEquals("java.lang.String[]", interestsVariable.getType());
                        assertTrue(methodModel.getExceptions().size() == 0);
                        //TODO: RETOUCHE test method body, see #90926
                    }
                    
                }
            }
        });
    }
    
    public void testGetTypeName() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Elements elements = controller.getElements();
                Types types = controller.getTypes();
                
                String typeName = String.class.getName();
                String resolvedTypeName = MethodModelSupport.getTypeName(controller, elements.getTypeElement(typeName).asType());
                assertEquals(typeName, resolvedTypeName);
                
                typeName = InputStream.class.getName();
                resolvedTypeName = MethodModelSupport.getTypeName(controller, elements.getTypeElement(typeName).asType());
                assertEquals(typeName, resolvedTypeName);
                
                resolvedTypeName = MethodModelSupport.getTypeName(controller, types.getPrimitiveType(TypeKind.INT));
                assertEquals("int", resolvedTypeName);

                typeName = String.class.getName();
                resolvedTypeName = MethodModelSupport.getTypeName(controller, types.getArrayType(elements.getTypeElement(typeName).asType()));
                assertEquals("java.lang.String[]", resolvedTypeName);
            }
        });
    }
    
    public void testCreateMethodTree() throws Exception {
        final MethodModel methodModel = MethodModel.create(
                "method",
                "void",
                "{ String name; }", // for now, Retouche requires those parenthesis (they won't appear in file)
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                assertEquals(0, methodTree.getModifiers().getFlags().size());
                PrimitiveTypeTree returnTypeTree = (PrimitiveTypeTree) methodTree.getReturnType();
                assertTrue(TypeKind.VOID == returnTypeTree.getPrimitiveTypeKind());
                assertTrue(methodTree.getName().contentEquals("method"));
                assertEquals(0, methodTree.getParameters().size());
                assertEquals(0, methodTree.getThrows().size());
                List<? extends StatementTree> statements = methodTree.getBody().getStatements();
                assertEquals(1, statements.size());
            }
        });
    }
    
    public void testCreateVariable() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "  private String name;" +
                "  private final String address;" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                TypeElement typeElement = SourceUtils.newInstance(controller).getTypeElement();
                List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
                MethodModel.Variable nonFinalVariable = MethodModelSupport.createVariable(controller, fields.get(0));
                assertEquals("java.lang.String", nonFinalVariable.getType());
                assertEquals("name", nonFinalVariable.getName());
                assertFalse(nonFinalVariable.getFinalModifier());
                MethodModel.Variable finalVariable = MethodModelSupport.createVariable(controller, fields.get(1));
                assertEquals("java.lang.String", finalVariable.getType());
                assertEquals("address", finalVariable.getName());
                assertTrue(finalVariable.getFinalModifier());
            }
        });
    }
    
    private static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }

    private static ModificationResult runModificationTask(FileObject javaFile, CancellableTask<WorkingCopy> taskToTest) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        return javaSource.runModificationTask(taskToTest);
    }

}
