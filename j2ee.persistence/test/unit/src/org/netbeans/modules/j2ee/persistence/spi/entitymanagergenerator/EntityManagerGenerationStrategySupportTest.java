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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import org.netbeans.modules.j2ee.persistence.action.*;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.File;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.jackpot.test.TestUtilities;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.openide.filesystems.FileUtil;

/**
 * Tests for the helper methods in EntityManagerGenerationStrategy.
 *
 * @author Erno Mononen
 */
public class EntityManagerGenerationStrategySupportTest extends EntityManagerGenerationTestSupport{
    
    public EntityManagerGenerationStrategySupportTest(String testName) {
        super(testName);
    }
    
    public void testGetAnnotationOnClass() throws Exception{
        
        final String annotation = "java.lang.Deprecated"; //some annotation
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "@" + annotation + "\n" +
                "public class Test {\n" +
                "}"
                );
        
        searchAnnotation(testFile, annotation, true);
    }
    
    public void testGetAnnotationOnField() throws Exception{
        
        final String annotation = "java.lang.Deprecated"; //some annotation
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "@" + annotation + "\n" +
                "Object myField;\n" +
                "}"
                );
        
        searchAnnotation(testFile, annotation, true);
        
    }
    
    public void testGetAnnotationOnMethod() throws Exception{
        
        final String annotation = "java.lang.Deprecated"; //some annotation
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "@" + annotation + "\n" +
                "Object method(){\n" +
                "return null;\n" +
                "}\n" +
                "}"
                );
        
        searchAnnotation(testFile, annotation, true);
        
    }
    
    
    public void testGetField() throws Exception{
        
        final String field = "java.lang.String"; //some field
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "private " + field + " myField;\n" +
                "}"
                );
        
        searchField(testFile, field, true);
        // test for searching a field that does not exist
        searchField(testFile, "java.lang.Object", false);
        
    }
    
    private void searchAnnotation(File testFile, final String annotation, final boolean expectSuccess) throws Exception {
        JavaSource targetSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        CancellableTask task = new TaskSupport() {
            void doAsserts(EntityManagerGenerationStrategySupport strategy) {
                Element result = strategy.getAnnotation(annotation);
                if (expectSuccess){
                    assertNotNull(result);
                    assertTrue(((TypeElement)result).getQualifiedName().contentEquals(annotation));
                } else {
                    assertNull(result);
                }
            }
        };
        
        targetSource.runModificationTask(task);
    }
    
    private void searchField(File testFile, final String field, final boolean expectSuccess) throws Exception {
        JavaSource targetSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        CancellableTask task = new TaskSupport() {
            void doAsserts(EntityManagerGenerationStrategySupport strategy) {
                Element result = strategy.getField(field);
                if (expectSuccess){
                    assertNotNull(result);
                    assertTrue(((TypeElement)result).getQualifiedName().contentEquals(field));
                } else {
                    assertNull(result);
                }
            }
        };
        
        targetSource.runModificationTask(task);
    }
    
    // a helper class for avoiding some duplicate code
    private abstract class TaskSupport extends AbstractTask<WorkingCopy> {
        
        public void run(WorkingCopy workingCopy) throws Exception {
            
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            
            for (Tree typeDeclaration : cut.getTypeDecls()){
                if (Tree.Kind.CLASS == typeDeclaration.getKind()){
                    ClassTree clazz = (ClassTree) typeDeclaration;
                    EntityManagerGenerationStrategySupport strategy = 
                            (EntityManagerGenerationStrategySupport) getStrategy(workingCopy, make, clazz, new GenerationOptions());
                    doAsserts(strategy);
                } else {
                    fail("No class found"); // should not happen
                }
            }
        }
        
        abstract void doAsserts(EntityManagerGenerationStrategySupport strategy);
        
    }
    
    public static class StubEntityManagerGenerationStrategy extends EntityManagerGenerationStrategySupport{
        
        public ClassTree generate() {
            return null;
        }
        
    }

    protected Class<? extends EntityManagerGenerationStrategy> getStrategyClass() {
        return StubEntityManagerGenerationStrategy.class;
    }
}
