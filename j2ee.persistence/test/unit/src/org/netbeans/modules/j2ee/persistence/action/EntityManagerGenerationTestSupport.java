/*
 * ContainerManagedJTANonInjectableInWebTest.java
 * JUnit based test
 *
 * Created on 1 November 2006, 16:00
 */

package org.netbeans.modules.j2ee.persistence.action;

import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import junit.framework.*;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Erno Mononen
 */
public abstract class EntityManagerGenerationTestSupport  extends SourceTestSupport{
    
    public EntityManagerGenerationTestSupport(String testName){
        super(testName);
    }
    
    
    protected FileObject generate(FileObject targetFo, final GenerationOptions options) throws IOException{
        
        JavaSource targetSource = JavaSource.forFileObject(targetFo);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            
            public void cancel() {
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (Tree.Kind.CLASS == typeDeclaration.getKind()){
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        ClassTree modifiedClazz = getStrategy(workingCopy, make, clazz, options).generate();
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
                
            }
        };
        targetSource.runModificationTask(task).commit();
        
        return targetFo;
        
    }

    /**
     * @return a persistence unit with name "MyPersistenceUnit". 
     */ 
    protected PersistenceUnit getPersistenceUnit(){
        PersistenceUnit punit = new PersistenceUnit();
        punit.setName("MyPersistenceUnit");
        return punit;
    }
    
    protected EntityManagerGenerationStrategy getStrategy(WorkingCopy workingCopy, TreeMaker make, ClassTree clazz, GenerationOptions options){
        
        EntityManagerGenerationStrategy result = null;
        
        try{
            result = getStrategyClass().newInstance();
            result.setClassTree(clazz);
            result.setWorkingCopy(workingCopy);
            result.setGenerationOptions(options);
            result.setTreeMaker(make);
            result.setPersistenceUnit(getPersistenceUnit());
        } catch (IllegalAccessException iae){
            throw new RuntimeException(iae);
        } catch (InstantiationException ie){
            throw new RuntimeException(ie);
        }
        
        return result;
    }

    
    protected abstract Class<? extends EntityManagerGenerationStrategy> getStrategyClass();

    
}

