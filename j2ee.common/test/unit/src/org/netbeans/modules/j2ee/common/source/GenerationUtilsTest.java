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

package org.netbeans.modules.j2ee.common.source;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Andrei Badea
 */
public class GenerationUtilsTest extends NbTestCase {

    private FileObject workDir;
    private FileObject testFO;

    static {
        // set the lookup which will be returned by Lookup.getDefault()
        System.setProperty("org.openide.util.Lookup",Lkp.class.getName());
        assertEquals("Unable to set the default lookup!",Lkp.class, Lookup.getDefault().getClass());
        assertEquals(RepositoryImpl.class, Lookup.getDefault().lookup(Repository.class).getClass());
        assertEquals("The default Repository is not our repository!", RepositoryImpl.class, Repository.getDefault().getClass());
    }

    public GenerationUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {

        clearWorkDir();
        TestUtilities.setCacheFolder(getWorkDir());
        workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
        
        ClassPathProviderImpl classPathProvider = new ClassPathProviderImpl(new FileObject[]{FileUtil.toFileObject(getWorkDir())});
        setLookups(
                classPathProvider,
                new FakeJavaDataLoaderPool(),
                new TestSourceLevelQueryImplementation()
                );
        
    }

    public void testNewInstance() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = copy.getElements().getTypeElement("foo.TestClass");
                GenerationUtils genUtils = GenerationUtils.newInstance(copy, typeElement);
                assertSame(typeElement, genUtils.getTypeElement());
                assertEquals(copy.getTrees().getTree(typeElement), genUtils.getClassTree());

                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                genUtils = GenerationUtils.newInstance(copy, classTree);
                assertSame(classTree, genUtils.getClassTree());
                TreePath classTreePath = copy.getTrees().getPath(copy.getCompilationUnit(), classTree);
                typeElement = (TypeElement)copy.getTrees().getElement(classTreePath);
                assertEquals(typeElement, genUtils.getTypeElement());

                genUtils = GenerationUtils.newInstance(copy);
                assertSame(genUtils.getTypeElement(), typeElement);
                assertSame(genUtils.getClassTree(), classTree);
            }
        });
    }

    public void testPhase() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                assertEquals(JavaSource.Phase.ELEMENTS_RESOLVED, copy.getPhase());
            }
        });
    }

    public void testCreateClass() throws Exception {
        FileObject javaFO = GenerationUtils.createClass(workDir, "NewTestClass", "Javadoc");
        runUserActionTask(javaFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertEquals(ElementKind.CLASS, srcUtils.getTypeElement().getKind());
                assertTrue(srcUtils.getNoArgConstructor() != null);
                // TODO assert for Javadoc
            }
        });
    }

    public void testCreateInterface() throws Exception {
        FileObject javaFO = GenerationUtils.createInterface(workDir, "NewTestClass", "Javadoc");
        runUserActionTask(javaFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertEquals(ElementKind.INTERFACE, srcUtils.getTypeElement().getKind());
                // TODO assert for Javadoc
            }
        });
    }

    public void testEnsureNoArgConstructor() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree newClassTree = genUtils.ensureNoArgConstructor(genUtils.getClassTree());
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                assertTrue(SourceUtils.newInstance(controller).getNoArgConstructor() != null);
            }
        });
    }

    public void testEnsureNoArgConstructorMakesConstructorPublic() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "    private TestClass() {" +
                "    }" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree newClassTree = genUtils.ensureNoArgConstructor(genUtils.getClassTree());
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                assertTrue(SourceUtils.newInstance(controller).getNoArgConstructor().getModifiers().contains(Modifier.PUBLIC));
            }
        });
    }

    public void testPrimitiveTypes() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                assertEquals(TypeKind.BOOLEAN, ((PrimitiveTypeTree)genUtils.createType("boolean")).getPrimitiveTypeKind());
                assertEquals(TypeKind.BYTE, ((PrimitiveTypeTree)genUtils.createType("byte")).getPrimitiveTypeKind());
                assertEquals(TypeKind.SHORT, ((PrimitiveTypeTree)genUtils.createType("short")).getPrimitiveTypeKind());
                assertEquals(TypeKind.INT, ((PrimitiveTypeTree)genUtils.createType("int")).getPrimitiveTypeKind());
                assertEquals(TypeKind.LONG, ((PrimitiveTypeTree)genUtils.createType("long")).getPrimitiveTypeKind());
                assertEquals(TypeKind.CHAR, ((PrimitiveTypeTree)genUtils.createType("char")).getPrimitiveTypeKind());
                assertEquals(TypeKind.FLOAT, ((PrimitiveTypeTree)genUtils.createType("float")).getPrimitiveTypeKind());
                assertEquals(TypeKind.DOUBLE, ((PrimitiveTypeTree)genUtils.createType("double")).getPrimitiveTypeKind());
            }
        });
    }

    public void testCreateAnnotation() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                AnnotationTree annotationTree = genUtils.createAnnotation("java.lang.SuppressWarnings",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, "unchecked")));
                ClassTree newClassTree = genUtils.addAnnotation(genUtils.getClassTree(), annotationTree);
                annotationTree = genUtils.createAnnotation("java.lang.annotation.Retention",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, "java.lang.annotation.RetentionPolicy", "RUNTIME")));
                newClassTree = genUtils.addAnnotation(newClassTree, annotationTree);
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertEquals(2, srcUtils.getTypeElement().getAnnotationMirrors().size());
                SuppressWarnings suppressWarnings = srcUtils.getTypeElement().getAnnotation(SuppressWarnings.class);
                assertNotNull(suppressWarnings);
                assertEquals(1, suppressWarnings.value().length);
                assertEquals("unchecked", suppressWarnings.value()[0]);
                Retention retention = srcUtils.getTypeElement().getAnnotation(Retention.class);
                assertNotNull(retention);
                assertEquals(RetentionPolicy.RUNTIME, retention.value());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void testCreateAnnotationArrayArgument() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "@interface NamedQueries {" +
                "   NamedQuery[] value();" +
                "}" +
                "@interface NamedQuery {" +
                "   String name();" +
                "   String query();" +
                "}" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ExpressionTree namedQueryAnnotation0 = genUtils.createAnnotation("foo.NamedQuery", Arrays.asList(
                        genUtils.createAnnotationArgument("name", "foo0"),
                        genUtils.createAnnotationArgument("query", "q0")));
                ExpressionTree namedQueryAnnotation1 = genUtils.createAnnotation("foo.NamedQuery", Arrays.asList(
                        genUtils.createAnnotationArgument("name", "foo1"),
                        genUtils.createAnnotationArgument("query", "q1")));
                ExpressionTree namedQueriesAnnValue = genUtils.createAnnotationArgument("value", Arrays.asList(namedQueryAnnotation0, namedQueryAnnotation1));
                AnnotationTree namedQueriesAnnotation = genUtils.createAnnotation("foo.NamedQueries", Collections.singletonList(namedQueriesAnnValue));
                ClassTree newClassTree = genUtils.addAnnotation(genUtils.getClassTree(), namedQueriesAnnotation);
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                List<? extends AnnotationMirror> annotations = srcUtils.getTypeElement().getAnnotationMirrors();
                Map<? extends ExecutableElement, ? extends AnnotationValue> namedQueriesAnnElements = annotations.get(0).getElementValues();
                List<? extends AnnotationMirror> namedQueriesAnnValue = (List<? extends AnnotationMirror>)namedQueriesAnnElements.values().iterator().next().getValue();
                assertEquals(2, namedQueriesAnnValue.size());
                int outer = 0;
                for (AnnotationMirror namedQueryAnn : namedQueriesAnnValue) {
                    int inner = 0;
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> namedQueryAnnElement : namedQueryAnn.getElementValues().entrySet()) {
                        String namedQueryAnnElementName = namedQueryAnnElement.getKey().getSimpleName().toString();
                        String namedQueryAnnElementValue = (String)namedQueryAnnElement.getValue().getValue();
                        switch (inner) {
                            case 0:
                                assertEquals("name", namedQueryAnnElementName);
                                assertEquals("foo" + outer, namedQueryAnnElementValue);
                                break;
                            case 1:
                                assertEquals("query", namedQueryAnnElementName);
                                assertEquals("q" + outer, namedQueryAnnElementValue);
                                break;
                            default:
                                fail();
                        }
                        inner++;
                    }
                    outer++;
                }
            }
        });
    }

    public void testCreateAnnotationBooleanArgumentIssue89230() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "@interface Column {" +
                "   boolean nullable();" +
                "}" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                AnnotationTree annotationTree = genUtils.createAnnotation("foo.Column", Collections.singletonList(genUtils.createAnnotationArgument("nullable", true)));
                ClassTree newClassTree = genUtils.addAnnotation(genUtils.getClassTree(), annotationTree);
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertEquals(1, srcUtils.getTypeElement().getAnnotationMirrors().size());
                AnnotationMirror columnAnn = srcUtils.getTypeElement().getAnnotationMirrors().get(0);
                assertEquals(1, columnAnn.getElementValues().size());
                Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> columnAnnNullableElement = columnAnn.getElementValues().entrySet().iterator().next();
                assertEquals("nullable", columnAnnNullableElement.getKey().getSimpleName().toString());
                assertEquals(true, columnAnn.getElementValues().values().iterator().next().getValue());
            }
        });
    }

    public void testCreateAnnotationArgumentWithNullName() throws Exception {
        FileObject annotationFO = workDir.createData("Annotations.java");
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                TreeMaker make = copy.getTreeMaker();
                AnnotationTree annWithLiteralArgument = genUtils.createAnnotation("java.lang.SuppressWarnings",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, "unchecked")));
                AnnotationTree annWithArrayArgument = genUtils.createAnnotation("java.lang.annotation.Target",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, Collections.<ExpressionTree>emptyList())));
                AnnotationTree annWithMemberSelectArgument = genUtils.createAnnotation("java.lang.annotation.Retention",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, "java.lang.annotation.RetentionPolicy", "RUNTIME")));
                ClassTree newClassTree = genUtils.addAnnotation(genUtils.getClassTree(), annWithLiteralArgument);
                newClassTree = genUtils.addAnnotation(newClassTree, annWithArrayArgument);
                newClassTree = genUtils.addAnnotation(newClassTree, annWithMemberSelectArgument);
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        assertFalse(TestUtilities.copyFileObjectToString(testFO).contains("value"));
    }

    public void testCreateProperty() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   private Object x;" +
                "   public TestClass() {" +
                "   }" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                VariableTree field = genUtils.createField(genUtils.createModifiers(Modifier.PRIVATE), "someProp", "java.lang.String");
                MethodTree getter = genUtils.createPropertyGetterMethod(genUtils.createModifiers(Modifier.PUBLIC), "someProp", "java.lang.String");
                MethodTree setter = genUtils.createPropertySetterMethod(genUtils.createModifiers(Modifier.PUBLIC), "someProp", "java.lang.String");
                TreeMaker make = copy.getTreeMaker();
                ClassTree newClassTree = genUtils.getClassTree();
                newClassTree = make.insertClassMember(newClassTree, 0, field);
                newClassTree = make.addClassMember(newClassTree, getter);
                newClassTree = make.addClassMember(newClassTree, setter);
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        // TODO check the field and methods
    }

    public void testAddImplementsClause() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree newClassTree = genUtils.addImplementsClause(genUtils.getClassTree(), "java.io.Serializable");
                newClassTree = genUtils.addImplementsClause(newClassTree, "java.lang.Cloneable");
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertImplements(controller, srcUtils.getTypeElement(), "java.io.Serializable");
                assertImplements(controller, srcUtils.getTypeElement(), "java.lang.Cloneable");
            }
        });
    }

    private static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }

    private static ModificationResult runModificationTask(FileObject javaFile, CancellableTask<WorkingCopy> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        return javaSource.runModificationTask(taskToTest);
    }

    private static void assertImplements(CompilationController controller, TypeElement typeElement, String interfaceName) {
        TypeMirror interfaceType = controller.getElements().getTypeElement("java.io.Serializable").asType();
        for (TypeMirror type : typeElement.getInterfaces()) {
            if (controller.getTypes().isSameType(interfaceType, type)) {
                return;
            }
        }
        fail("Type " + typeElement + " does not implement " + interfaceName);
    }
    

    private static void setLookups(Object... lookups) {
        ((Lkp)Lookup.getDefault()).setProxyLookups(Lookups.fixed(lookups));
    }

    public static final class Lkp extends ProxyLookup {
        
        private final Repository repository = new RepositoryImpl();
        
        public Lkp() {
            setProxyLookups(new Lookup[0]);
        }
        
        private void setProxyLookups(Lookup... lookups) {
            Lookup[] allLookups = new Lookup[lookups.length + 3];
            ClassLoader classLoader = GenerationUtilsTest.class.getClassLoader();
            allLookups[0] = Lookups.singleton(classLoader);
            allLookups[1] = Lookups.singleton(repository);
            System.arraycopy(lookups, 0, allLookups, 2, lookups.length);
            allLookups[allLookups.length - 1] = Lookups.metaInfServices(classLoader);
            setLookups(allLookups);
        }
    }
    
    public static final class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {
        
        public String getSourceLevel(FileObject javaFile) {
            return "1.5";
        }

    }

}
