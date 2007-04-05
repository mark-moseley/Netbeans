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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.ServiceLocatorStrategy;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class UseDatabaseGenerator {
    
    public UseDatabaseGenerator() {
    }

    public void generate(final FileObject fileObject, final ElementHandle<TypeElement> elementHandle, 
                         final J2eeModuleProvider j2eeModuleProvider, final String datasourceReferenceName, 
                         final Datasource datasource, final boolean createServerResources, String serviceLocator) 
                         throws IOException, ConfigurationException
    {
        Project project = FileOwnerQuery.getOwner(fileObject);
        ServiceLocatorStrategy serviceLocatorStrategy = (serviceLocator == null) ? null : 
            ServiceLocatorStrategy.create(project, fileObject, serviceLocator);
        EnterpriseReferenceContainer erc = project.getLookup().lookup(EnterpriseReferenceContainer.class);
        String className = elementHandle.getQualifiedName();
        if (Utils.isJavaEE5orHigher(project) && serviceLocatorStrategy == null &&
                InjectionTargetQuery.isInjectionTarget(fileObject, className)) {
            boolean isStatic = InjectionTargetQuery.isStaticReferenceRequired(fileObject, className);
            String jndiName = datasource.getJndiName();
            String fieldName = Utils.jndiNameToCamelCase(jndiName, true, null);
            _RetoucheUtil.generateAnnotatedField(fileObject, className, "javax.annotation.Resource", fieldName, 
                    "javax.sql.DataSource", Collections.singletonMap("name", jndiName), isStatic);
        } else {
            String jndiName = generateJNDILookup(datasource.getJndiName(), erc, 
                    fileObject, className, datasource.getUrl(), createServerResources);
            if (jndiName != null) {
                generateLookupMethod(fileObject, className, jndiName, serviceLocatorStrategy);
            }
        }
        
if (System.getProperties().getProperty("resource-api-redesign") != null) {
    J2eeModule module = j2eeModuleProvider.getJ2eeModule();
    if (isWebModule(module)) {
        bindDataSourceReference(j2eeModuleProvider, datasourceReferenceName, datasource);
    }
    else if (isEjbModule(module)) {
        bindDataSourceReferenceForEjb(j2eeModuleProvider, datasourceReferenceName, datasource, fileObject, elementHandle);
    }
}
        
        if (serviceLocator != null) {
            erc.setServiceLocatorName(serviceLocator);
        }
    }
    
    private void bindDataSourceReference(J2eeModuleProvider j2eeModuleProvider, String dsRefName, Datasource datasource) 
    throws ConfigurationException {

        String dsJndiName = datasource.getJndiName();
        j2eeModuleProvider.getConfigSupport().bindDatasourceReference(dsRefName, dsJndiName);
    }
    
    private void bindDataSourceReferenceForEjb(J2eeModuleProvider j2eeModuleProvider, String dsRefName, Datasource datasource,
            FileObject fileObject, ElementHandle<TypeElement> elementHandle) throws ConfigurationException {

        String dsJndiName = datasource.getJndiName();

        EjbJar dd = null;
        try {
            dd = findDDRoot(fileObject);
        }
        catch (IOException ioe) {
            // TODO
        }
        if (dd == null) {
            return;
        }
        
        EnterpriseBeans beans = dd.getEnterpriseBeans();
        if (beans == null) {
            return;
        }
        
        String ejbName = getEjbName(beans, elementHandle.getQualifiedName());
        if (ejbName == null) {
            return;
        }
        
        String ejbType = getEjbType(beans, elementHandle.getQualifiedName());
        if (ejbType == null) {
            return;
        }
        
        j2eeModuleProvider.getConfigSupport().bindDatasourceReferenceForEjb(ejbName, ejbType, dsRefName, dsJndiName);
    }
    
    private EjbJar findDDRoot(FileObject fileObject) throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJar = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        assert ejbJar != null;
        return DDProvider.getDefault().getMergedDDRoot(ejbJar.getMetadataUnit());
    }
    
    private boolean isWebModule(J2eeModule module) {
        return module.getModuleType().equals(J2eeModule.WAR);
    }
    
    private boolean isEjbModule(J2eeModule module) {
        return module.getModuleType().equals(J2eeModule.EJB);
    }
    
    private String getEjbName(EnterpriseBeans beans, String className) {
        Ejb ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.SESSION, Ejb.EJB_CLASS, className);
        if (ejb == null) {
            ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.ENTITY, Ejb.EJB_CLASS, className);
        }
        if (ejb == null) {
            ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.MESSAGE_DRIVEN, Ejb.EJB_CLASS, className);
        }

        return ejb.getEjbName();
    }
    
    private String getEjbType(EnterpriseBeans beans, String className) {
        String type = null;
        
        if (beans.findBeanByName(EnterpriseBeans.SESSION, Ejb.EJB_CLASS, className) != null) {
            type = EnterpriseBeans.SESSION;
        }
        else
        if (beans.findBeanByName(EnterpriseBeans.ENTITY, Ejb.EJB_CLASS, className) != null) {
            type = EnterpriseBeans.ENTITY;
        }
        else
        if (beans.findBeanByName(EnterpriseBeans.MESSAGE_DRIVEN, Ejb.EJB_CLASS, className) != null) {
            type = EnterpriseBeans.MESSAGE_DRIVEN;
        }

        return type;
    }
    
    private String generateJNDILookup(String jndiName, EnterpriseReferenceContainer enterpriseReferenceContainer, 
            FileObject fileObject, String className, String nodeName, boolean createServerResources) throws IOException {
        String result = null;
        ResourceRef ref = enterpriseReferenceContainer.createResourceRef(className);
        if (ref != null) {
            if (createServerResources) {
                ref.setDescription(nodeName);
            }
            ref.setResRefName(jndiName);
            ref.setResAuth(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_AUTH_CONTAINER);
            ref.setResSharingScope(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_SHARING_SCOPE_SHAREABLE);
            ref.setResType(javax.sql.DataSource.class.getName());
            result = enterpriseReferenceContainer.addResourceRef(ref, fileObject, className);
        }
        return result;
    }
    
    private void generateLookupMethod(FileObject fileObject, final String className, final String jndiName, 
            final ServiceLocatorStrategy slStrategy) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final String body = slStrategy == null ? getLookupCode(jndiName) : getLookupCode(jndiName, slStrategy, fileObject, className);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                String methodName = "get" + Utils.jndiNameToCamelCase(jndiName, false, null); //NO18N
                MethodModel methodModel = MethodModel.create(
                        methodName,
                        javax.sql.DataSource.class.getName(),
                        body,
                        Collections.<MethodModel.Variable>emptyList(),
                        Collections.singletonList(javax.naming.NamingException.class.getName()),
                        Collections.singleton(Modifier.PRIVATE)
                        );
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree modifiedClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();
    }
    
    private String getLookupCode(String jndiName, ServiceLocatorStrategy serviceLocatorStrategy, FileObject fileObject, String className) {
        String jdbcLookupString = serviceLocatorStrategy.genDataSource(jndiName, fileObject, className);
        return "return (javax.sql.DataSource) " + jdbcLookupString + ";\n"; // NOI18N
    }
    
    private String getLookupCode(String jndiName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
                "return (javax.sql.DataSource) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
                new Object[] { jndiName });
    }
    
}
