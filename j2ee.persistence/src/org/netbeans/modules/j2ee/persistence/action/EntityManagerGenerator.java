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

package org.netbeans.modules.j2ee.persistence.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Generates appropriate code for retrieving and invoking <code>javax.persistence.EntityManager</code>.
 * The generated code depends on the target class' enviroment.
 *
 * TODO: move this class to different package if anybody else wants to use it
 * @author Martin Adamek, Erno Mononen
 */

public class EntityManagerGenerator {
    
    private static final String COMMENT_TODO =
            "// TODO:\n" +
            "// ";
    
    /**
     * The fully qualified name of the target class.
     */
    private final String fqn;
    /**
     *  The target java source file.
     */
    private final JavaSource targetSource;
    /**
     * The file object of the target source file.
     */
    private final FileObject targetFo;
    
    
    /**
     * Creates a new EntityManagerGenerator.
     * @param targetFo the file object of the target java source file.
     * @param fqn the fully qualified name of the target java class.
     */
    public EntityManagerGenerator(FileObject targetFo, String fqn) {
        this.fqn = fqn;
        this.targetFo = targetFo;
        this.targetSource = JavaSource.forFileObject(targetFo);
    }
    
    /**
     * Generates the code needed for retrieving and invoking
     * <code>javax.persistence.EntityManager</code>. The generated code depends
     * on the target class' environment.
     * @param options the options for the generation.
     * @return the modified file object of the target java class.
     */
    public FileObject generate(final GenerationOptions options) throws IOException{
        
        AbstractTask task = new AbstractTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (Tree.Kind.CLASS == typeDeclaration.getKind()){
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        EntityManagerGenerationStrategy strategy = getStrategy(workingCopy, make, clazz, options);
                        if (strategy != null){
                            ClassTree modifiedClazz = getStrategy(workingCopy, make, clazz, options).generate();
                            workingCopy.rewrite(clazz, modifiedClazz);
                        } else {
                            NotifyDescriptor d = new NotifyDescriptor.Message(
                                    NbBundle.getMessage(EntityManagerGenerator.class, "ERR_NotSupportedAMJTA"), NotifyDescriptor.INFORMATION_MESSAGE);
                            DialogDisplayer.getDefault().notify(d);
                        }
                    }
                }
            }
        };
        targetSource.runModificationTask(task).commit();
        
        return targetFo;
    }
    
    
    private EntityManagerGenerationStrategy getStrategy(WorkingCopy workingCopy, TreeMaker make, ClassTree clazz, GenerationOptions options){
        J2eeModule j2eeModule = null;
        
        // try to get J2eeModule
        Project project = FileOwnerQuery.getOwner(targetFo);
        J2eeModuleProvider j2eeModuleProvider = null;
        if (project != null){
            j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        }
        if (j2eeModuleProvider != null) {
            j2eeModule = j2eeModuleProvider.getJ2eeModule();
        }
        
        if (j2eeModule == null) {
            // Application-managed persistence context in J2SE project (Resource-transaction)
            return new ApplicationManagedResourceTransactionInJ2SE(workingCopy, make, clazz, options);
        } else {
            PersistenceUnit pu = getPersistenceUnit();
            // it is Web or EJB, let's get all needed information
            String jtaDataSource = pu.getJtaDataSource();
            String nonJtaDataSource = pu.getNonJtaDataSource();
            String transactionType = pu.getTransactionType();
            boolean isInjectionTarget = true; //!checkInjection || InjectionTargetQuery.isInjectionTarget(workingCopy.get));
            boolean isContainerManaged = (jtaDataSource != null && !jtaDataSource.equals("")) && (transactionType != null && transactionType.equals("JTA"));
            boolean isJTA = (transactionType == null || transactionType.equals("JTA")); // JTA is default value for transaction type in non-J2SE projects
            
            if (j2eeModule.getModuleType().equals(J2eeModule.WAR)) { // Web project
                if (isContainerManaged) { // Container-managed persistence context
                    if (isInjectionTarget) { // servlet, JSF managed bean ...
                        return new ContainerManagedJTAInjectableInEJB(workingCopy, make, clazz, options);
                    } else { // other classes
                        return new ContainerManagedJTAInjectableInWeb(workingCopy, make, clazz, options);
                    }
                } else { // Application-managed persistence context (Resource-transaction)
                    if (isJTA) { // JTA
                        if (isInjectionTarget) { // servlet, JSF managed bean ...
                            // not supported
                            return null;
                        } else { // other classes
                            // not supported
                            return null;
                        }
                    } else { // Resource-transaction
                        if (isInjectionTarget) { // servlet, JSF managed bean ...
                            return new ApplicationManagedResourceTransactionInjectableInWeb(workingCopy, make, clazz, options);
                        } else { // other classes
                            return new ApplicationManagedResourceTransactionNonInjectableInWeb(workingCopy, make, clazz, options);
                        }
                    }
                }
            } else if (j2eeModule.getModuleType().equals(J2eeModule.EJB)) { // EJB project
                if (isContainerManaged) { // Container-managed persistence context
                    if (isInjectionTarget) { // session, MessageDriven
                        return new ContainerManagedJTAInjectableInEJB(workingCopy, make, clazz, options);
                    } else { // other classes
                        // ???
                        return null;
                    }
                } else { // Application-managed persistence context
                    if (isJTA) { // JTA
                        if (isInjectionTarget) { // session, MDB
                            // not supported
                            return null;
                        } else { // other classes
                            // not supported
                            return null;
                        }
                    } else { // Resource-transaction
                        if (isInjectionTarget) { // session, MDB
                            return new ApplicationManagedResourceTransactionInjectableInEJB(workingCopy, make, clazz, options);
                        } else { // other classes
                            return new ApplicationManagedResourceTransactionNonInjectableInEJB(workingCopy, make,clazz, options);
                        }
                    }
                }
            }
        }
        // not supported
        return null;
    }
    
    private PersistenceUnit getPersistenceUnit() {
        PersistenceScope persistenceScope = PersistenceScope.getPersistenceScope(targetFo);
        try {
            // TODO: fix ASAP! 1st PU is taken, needs to find the one which realy owns given file
            return PersistenceMetadata.getDefault().getRoot(persistenceScope.getPersistenceXml()).getPersistenceUnit(0);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }
    
}
