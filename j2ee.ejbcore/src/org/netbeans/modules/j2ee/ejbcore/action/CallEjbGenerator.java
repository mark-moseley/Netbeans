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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.naming.NamingException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.ServiceLocatorStrategy;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class CallEjbGenerator {

    private final EjbReference ejbReference;
    private final String ejbName;
    private final boolean isSimplified;
    private final boolean isSession;

    private CallEjbGenerator(final EjbReference ejbReference) {
        
        this.ejbReference = ejbReference;
        final boolean[] iofSession = new boolean[] { false };
        final String[] ejbNameArr = new String[1];
        BigDecimal version = null;
        try {
            MetadataModel<EjbJarMetadata> metadataModel = ejbReference.getEjbModule().getMetadataModel();
            version = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, BigDecimal>() {
                public BigDecimal run(EjbJarMetadata metadata) throws Exception {
                    Ejb ejb = metadata.findByEjbClass(ejbReference.getEjbClass());
                    iofSession[0] = ejb instanceof Session;
                    ejbNameArr[0] = ejb.getEjbName();
                    return metadata.getRoot().getVersion();
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        this.ejbName = ejbNameArr[0];
        this.isSimplified = version == null ? true : (version.doubleValue() > 2.1);
        this.isSession = iofSession[0];
        
    }
    
    public static CallEjbGenerator create(EjbReference ejbReference) {
        return new CallEjbGenerator(ejbReference);
    }
    
    public void generateServiceLocatorLookup(FileObject fileObject, String className, String serviceLocatorName, boolean isLocal, boolean throwExceptions) {
        try {
            generateServiceLocatorJNDI(
                    fileObject, 
                    className, 
                    isLocal ? ejbReference.getLocalHome() : ejbReference.getRemoteHome(), 
                    ejbName, 
                    true, 
                    isLocal ? ejbReference.getLocal() : ejbReference.getRemote(), 
                    throwExceptions, 
                    serviceLocatorName
                    );
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    public void generateReferenceCode(FileObject fileObject, String className, boolean isLocal, boolean throwExceptions) {
        try {
            boolean isInjectionTarget = InjectionTargetQuery.isInjectionTarget(fileObject, className);
            if (isInjectionTarget) {
                generateInjection(
                        fileObject, 
                        className, 
                        ejbName, 
                        isLocal ? ejbReference.getLocal() : ejbReference.getRemote()
                        );
            } else {
                generateJNDI(
                        fileObject, 
                        className, 
                        isLocal ? ejbReference.getLocalHome() : ejbReference.getRemoteHome(), 
                        ejbName, 
                        isLocal ? ejbReference.getLocal() : ejbReference.getRemote(), 
                        throwExceptions,
                        isLocal
                        );
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    // private stuff ===========================================================
    
    private static final String LOG_STATEMENT =
            "java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\" ,{0});\n";
    
    private static final String JNDI_LOOKUP_LOCAL =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "{1} rv = ({1}) c.lookup(\"java:comp/env/{0}\");\n" +
            "return rv{2};\n";
    
    private static final String JNDI_LOOKUP_REMOTE =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "Object remote = c.lookup(\"java:comp/env/{0}\");\n" +
            "{1} rv = ({1}) javax.rmi.PortableRemoteObject.narrow(remote, {1}.class);\n" +
            "return rv{2};\n";

    /**
     * Lookup code for EJB 2.x beans in Java SE environments.
     */
    private static final String JNDI_LOOKUP_REMOTE_JAVASE =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "Object remote = c.lookup(\"{0}\");\n" +
            "{1} rv = ({1}) javax.rmi.PortableRemoteObject.narrow(remote, {1}.class);\n" +
            "return rv{2};\n";
    
    private static final String JNDI_LOOKUP_EJB3 =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "return ({1}) c.lookup(\"java:comp/env/{0}\");\n";
    
    /**
     * Lookup code for EJB3 beans in Java SE environments.
     */
    private static final String JNDI_LOOKUP_EJB3_JAVASE =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "return ({1}) c.lookup(\"{0}\");\n";
    
    private void generateInjection(FileObject fileObject, final String className, String refName, final String fieldTypeClass) throws IOException {
        String strippedRefName = refName.substring(refName.lastIndexOf('/') + 1);
        String name = Character.toLowerCase(strippedRefName.charAt(0)) + strippedRefName.substring(1);
        _RetoucheUtil.generateAnnotatedField(
                fileObject, 
                className, 
                "javax.ejb.EJB", 
                name,
                fieldTypeClass, 
                null, 
                InjectionTargetQuery.isStaticReferenceRequired(fileObject, className)
                );
    }
    
    private void generateJNDI(FileObject fileObject, final String className, String homeName, String refName, 
            String componentName, boolean throwCheckedExceptions, boolean isLocal) throws IOException {
        String name = "lookup"+refName.substring(refName.lastIndexOf('/')+1);
        String body = null;
        List<String> exceptions = new ArrayList<String>();
        boolean isTargetJavaSE = Utils.isTargetJavaSE(fileObject, className);
        String sessionCreate = "";
        if (isSession) {
            sessionCreate = ".create()";
        }
        if (isSimplified && isTargetJavaSE){
            body = MessageFormat.format(JNDI_LOOKUP_EJB3_JAVASE, new Object[] {ejbReference.getEjbClass(), componentName});
        } else if (isSimplified) {
            body = MessageFormat.format(JNDI_LOOKUP_EJB3, new Object[] {refName, componentName});
        } else if (isTargetJavaSE){
            body = MessageFormat.format(JNDI_LOOKUP_REMOTE_JAVASE, new Object[] {homeName, homeName, sessionCreate});
        } else if (!isLocal) {
            body = MessageFormat.format(JNDI_LOOKUP_REMOTE, new Object[] {refName, homeName, sessionCreate});
        } else {
            body = MessageFormat.format(JNDI_LOOKUP_LOCAL, new Object[] {refName, homeName, sessionCreate});
        }
        String returnType = isSimplified ? componentName : homeName;
        exceptions.add(NamingException.class.getName());
        if (isSession) {
            returnType = componentName;
            if (!isSimplified) {
                exceptions.add("javax.ejb.CreateException");
            }
            if (!isSimplified && !isLocal) {
                exceptions.add("java.rmi.RemoteException");
            }
        }
        if (!throwCheckedExceptions) {
            Iterator exIt = exceptions.iterator();
            StringBuffer catchBody = new StringBuffer("try {\n" + body + "}\n"); // NOI18N
            while (exIt.hasNext()) {
                String exceptionName = (String) exIt.next();
                catchBody.append("catch("); // NOI18N
                catchBody.append(exceptionName);
                catchBody.append(' ');  //NOI18N
                String capitalLetters = extractAllCapitalLetters(exceptionName);
                catchBody.append(capitalLetters);
                catchBody.append(") {\n"); //NOI18N
                catchBody.append(MessageFormat.format(LOG_STATEMENT,
                        new Object[] {capitalLetters}));
                catchBody.append("throw new RuntimeException("+capitalLetters+");\n");
                catchBody.append("}\n"); //NOI18N
            }
            body = catchBody.toString();
            exceptions = Collections.<String>emptyList();
        }

        final MethodModel methodModel = MethodModel.create(
                _RetoucheUtil.uniqueMemberName(fileObject, className, name, "ejb"),
                returnType,
                body,
                Collections.<MethodModel.Variable>emptyList(),
                exceptions,
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }
    
    
    private Object generateServiceLocatorJNDI(FileObject fileObject, final String className, String homeName, String refName,
        boolean narrow, String componentName, boolean throwCheckedExceptions, String serviceLocatorName) throws IOException {
        String name = "lookup"+refName.substring(refName.lastIndexOf('/')+1);
        String body = null;
        List<String> exceptions = new ArrayList<String>(3);
        exceptions.add(NamingException.class.getName());
        String returnType = isSimplified ? componentName : homeName;
        boolean genCreate = isSession;
        if (genCreate) {
            returnType = componentName;
            exceptions.add("javax.ejb.CreateException"); //NOI18N
            if (narrow) {
                exceptions.add("java.rmi.RemoteException"); //NOI18N
            }
        }
        Project enterpriseProject = FileOwnerQuery.getOwner(fileObject);
        ServiceLocatorStrategy sls = ServiceLocatorStrategy.create(enterpriseProject, fileObject, serviceLocatorName);
        if (narrow) {
            body = sls.genRemoteEjbStringLookup(refName, homeName, fileObject, className, genCreate);
        } else {
            body = sls.genLocalEjbStringLookup(refName, homeName, fileObject, className, genCreate);
        }
        if (!throwCheckedExceptions) {
            Iterator exIt = exceptions.iterator();
            StringBuffer catchBody = new StringBuffer("try {\n" + body + "\n}"); // NOI18N
            while (exIt.hasNext()) {
                String exceptionName = (String) exIt.next();
                catchBody.append(" catch("); // NOI18N
                catchBody.append(exceptionName);
                catchBody.append(' ');  //NOI18N
                String capitalLetters = extractAllCapitalLetters(exceptionName);
                catchBody.append(capitalLetters);
                catchBody.append(") {\n"); //NOI18N
                catchBody.append(MessageFormat.format(LOG_STATEMENT, new Object[] {capitalLetters}));
                catchBody.append("throw new RuntimeException("+capitalLetters+");\n");
                catchBody.append('}');
                body = catchBody.toString();
                exceptions = Collections.<String>emptyList();
            }
        }
            
        final MethodModel methodModel = MethodModel.create(
                _RetoucheUtil.uniqueMemberName(fileObject, className, name, "ejb"),
                returnType,
                body,
                Collections.<MethodModel.Variable>emptyList(),
                exceptions,
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        //TODO: RETOUCHE return generated method
        return null;
    }
    
    private String extractAllCapitalLetters(String word) {
        StringBuffer caps = new StringBuffer(4);
        for (int i =0; i < word.length(); i++) {
            char character = word.charAt(i);
            if (Character.isUpperCase(character)) {
                caps.append(Character.toLowerCase(character));
            }
        }
        return caps.toString();
    }
    
}
