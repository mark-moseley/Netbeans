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
package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class AbstractMethodController extends EjbMethodController {
    
    private final static int LOCAL = 0;
    private final static int REMOTE = 1;
    private final static int LOCAL_HOME = 2;
    private final static int REMOTE_HOME = 3;
    
    private final String ejbClass;
    private final MetadataModel<EjbJarMetadata> model;
    
    protected Set classesForSave;
    private final boolean simplified;
    private final String local;
    private final String remote;
    private final String localHome;
    private final String remoteHome;
    
    public AbstractMethodController(final String ejbClass, MetadataModel<EjbJarMetadata> model) {
        this.ejbClass = ejbClass;
        this.model = model;
        final String[] results = new String[4];
        BigDecimal version = null;
        try {
            version = model.runReadAction(new MetadataModelAction<EjbJarMetadata, BigDecimal>() {
                public BigDecimal run(EjbJarMetadata metadata) throws Exception {
                    EntityAndSession model = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                    results[LOCAL] = model.getLocal();
                    results[REMOTE] = model.getRemote();
                    results[LOCAL_HOME] = model.getLocalHome();
                    results[REMOTE_HOME] = model.getHome();
                    return metadata.getRoot().getVersion();
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        this.simplified = version == null ? true : (version.doubleValue() > 2.1);
        local = results[LOCAL];
        remote = results[REMOTE];
        localHome = results[LOCAL_HOME];
        remoteHome = results[REMOTE_HOME];
    }
    
    public interface GenerateFromImpl {
        void getInterfaceMethodFromImpl(MethodType methodType, String homeClass, String componentClass);
        String getDestinationInterface();
        MethodModel getInterfaceMethod();
    }
    
    public interface GenerateFromIntf {
        void getInterfaceMethodFromImpl(MethodType methodType);
        MethodModel getImplMethod();
        MethodModel getSecondaryMethod();
    }
    
    public abstract GenerateFromImpl createGenerateFromImpl();
    public abstract GenerateFromIntf createGenerateFromIntf();
    
    @Override
    public final MethodModel createAndAdd(MethodModel clientView, boolean isLocal, boolean isComponent) {
        String home = null;
        String component = null;
        if (isLocal) {
            home = localHome;
            component = findBusinessInterface(local);
        } else {
            home = remoteHome;
            component = findBusinessInterface(remote);
        }
        if (isComponent) {
            try {
                addMethodToClass(component, clientView);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }

        } else {
            try {
                addMethodToClass(home, clientView);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        if (hasJavaImplementation(clientView)) {
            for (MethodModel me : getImplementationMethods(clientView)) {
                try {
                    if (!findInClass(ejbClass, me)) {
                        addMethodToClass(ejbClass, me);
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        MethodModel result = clientView;
        if (!isLocal && !simplified) {
            result = addExceptionIfNecessary(clientView, RemoteException.class.getName());
        }
        return result;
            //TODO: RETOUCHE opening generated method in editor
//        if (methodToOpen != null) {
//            StatementBlock stBlock = null;
//            if (methodToOpen.isValid())
//                stBlock = methodToOpen.getBody();
//            if (stBlock != null)
//                JMIUtils.openInEditor(stBlock);
//        }
    }
    
    @Override
    public final void createAndAddInterface(MethodModel beanImpl, boolean isLocal) {
        MethodType methodType = getMethodTypeFromImpl(beanImpl);
        GenerateFromImpl generateFromImpl = createGenerateFromImpl();
        String home = null;
        String component = null;
        if (isLocal) {
            home = localHome;
            component = findBusinessInterface(local);
        } else {
            home = remoteHome;
            component = findBusinessInterface(remote);
        }
        generateFromImpl.getInterfaceMethodFromImpl(methodType, home, component);
        MethodModel method = generateFromImpl.getInterfaceMethod();
        if (!isLocal && !simplified) {
            method = addExceptionIfNecessary(method, RemoteException.class.getName());
        }
        method = MethodModel.create(
                method.getName(), 
                method.getReturnType(),
                method.getBody(),
                method.getParameters(),
                method.getExceptions(),
                Collections.<Modifier>emptySet()
                );

        String destinationInterface = generateFromImpl.getDestinationInterface();
        try {
            addMethodToClass(destinationInterface, method);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

    }
    
    @Override
    public final void createAndAddImpl(MethodModel intfView) {
        MethodType methodType = getMethodTypeFromInterface(intfView);
        GenerateFromIntf generateFromIntf = createGenerateFromIntf();
        generateFromIntf.getInterfaceMethodFromImpl(methodType);
        MethodModel method = generateFromIntf.getImplMethod();
        try {
            addMethodToClass(ejbClass, method);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

    }
    
    private List<MethodModel> getImplementationMethods(MethodModel intfView) {
        MethodType methodType = getMethodTypeFromInterface(intfView);
        GenerateFromIntf generateFromIntf = createGenerateFromIntf();
        generateFromIntf.getInterfaceMethodFromImpl(methodType);
        MethodModel primary = generateFromIntf.getImplMethod();
        MethodModel secondary = generateFromIntf.getSecondaryMethod();
        List<MethodModel> methods = null;
        if (secondary != null) {
            methods = Arrays.asList(new MethodModel[] {primary, secondary});
        } else {
            methods = Collections.singletonList(primary);
        }
        return methods;
    }
    
    @Override
    public final List<MethodModel> getImplementation(MethodModel intfView) {
        List<MethodModel> methods = getImplementationMethods(intfView);
        List<MethodModel> result = new ArrayList<MethodModel>(methods.size());
        for (MethodModel method : methods) {
            boolean exists = findInClass(getBeanClass(), method);
            if (exists) {
                result.add(method);
            }
        }
        return result;
    }
    
    @Override
    public final ClassMethodPair getInterface(MethodModel beanImpl, boolean isLocal) {
        MethodType methodType = getMethodTypeFromImpl(beanImpl);
        assert methodType != null: "method cannot be used in interface";
        GenerateFromImpl generateFromImpl = createGenerateFromImpl();
        String home = null;
        String component = null;
        if (isLocal) {
            home = localHome;
            component = findBusinessInterface(local);
        } else {
            home = remoteHome;
            component = findBusinessInterface(remote);
        }
        generateFromImpl.getInterfaceMethodFromImpl(methodType,home,component);
        MethodModel interfaceMethodModel = generateFromImpl.getInterfaceMethod();
        String destinationInterface = generateFromImpl.getDestinationInterface();
        boolean exists = findInClass(destinationInterface, interfaceMethodModel);
        return exists ? new ClassMethodPair(destinationInterface, interfaceMethodModel) : null;
    }
    
    
    /** Performs the check if the method is defined in apporpriate interface
     * @return false if the interface is found but does not contain matching method.
     */
    @Override
    public boolean hasMethodInInterface(MethodModel method, MethodType methodType, boolean isLocal) {
        String intf = null;
        MethodModel methodCopy = method;
        if (methodType.getKind() == MethodType.Kind.BUSINESS) {
            intf = findBusinessInterface(isLocal ? local : remote);
        } else if (methodType.getKind() == MethodType.Kind.CREATE) {
            String name = chopAndUpper(methodCopy.getName(), "ejb"); //NOI18N
            String type = isLocal ? local : remote;
            methodCopy = MethodModel.create(
                    name,
                    type,
                    methodCopy.getBody(),
                    methodCopy.getParameters(),
                    methodCopy.getExceptions(),
                    methodCopy.getModifiers()
                    );
            intf = isLocal ? localHome : remoteHome;
        }
        if (methodCopy.getName() == null || intf == null || methodCopy.getReturnType() == null) {
            return true;
        }
        if (findInClass(intf, methodCopy)) {
            return true;
        }
        return false;
    }
    
    private String chopAndUpper(String fullName, String chop) {
        StringBuffer stringBuffer = new StringBuffer(fullName);
        stringBuffer.delete(0, chop.length());
        stringBuffer.setCharAt(0, Character.toLowerCase(stringBuffer.charAt(0)));
        return stringBuffer.toString();
    }
    
    private MethodModel addExceptionIfNecessary(MethodModel method, String exceptionName) {
        if (!method.getExceptions().contains(exceptionName)) {
            List<String> exceptions = new ArrayList<String>(method.getExceptions());
            exceptions.add(exceptionName);
            return MethodModel.create(
                    method.getName(),
                    method.getReturnType(),
                    method.getBody(),
                    method.getParameters(),
                    exceptions,
                    method.getModifiers()
                    );
        }
        return method;
    }
    
    private String findBusinessInterface(String compInterfaceName) {
        if (compInterfaceName == null || ejbClass == null) {
            return null;
        }
        // get bean interfaces
        List<String> beanInterfaces = new ArrayList<String>();
        try {
            beanInterfaces = getInterfaces(ejbClass);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        // get method interfaces
        List<String> compInterfaces = new ArrayList<String>();
        try {
            compInterfaces = getInterfaces(compInterfaceName);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        // look for common candidates
        compInterfaces.retainAll(beanInterfaces);
        if (compInterfaces.isEmpty()) {
            return compInterfaceName;
        }
        String business = compInterfaces.get(0);
        return business == null ? compInterfaceName : business;
    }
    
    private List<String> getInterfaces(final String className) throws IOException {
        FileObject ejbClassFO = model.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
            public FileObject run(EjbJarMetadata metadata) throws Exception {
                return metadata.findResource(Utils.toResourceName(ejbClass));
            }
        });
        final List<String> result = new ArrayList<String>();
        if (ejbClassFO != null) {
            JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
            try {
                javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElement = controller.getElements().getTypeElement(className);
                        Types types = controller.getTypes();
                        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                            Element element = types.asElement(interfaceType);
                            String interfaceFqn = ((TypeElement) element).getQualifiedName().toString();
                            result.add(interfaceFqn);
                        }
                    }
                }, true);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return result;
    }
    
    @Override
    public final String getBeanClass() {
        return ejbClass;
    }
    
    @Override
    public final List<String> getLocalInterfaces() {
        if (!hasLocal()) {
            return Collections.<String>emptyList();
        }
        List<String> resultList = new ArrayList<String>(2);
        if (localHome != null) {
            resultList.add(localHome);
        }
        if (local != null) {
            resultList.add(findBusinessInterface(local));
        }
        
        return resultList;
    }
    
    @Override
    public final List<String> getRemoteInterfaces() {
        if (!hasRemote()) {
            return Collections.<String>emptyList();
        }
        List<String> resultList = new ArrayList<String>(2);
        if (remoteHome != null) {
            resultList.add(remoteHome);
        }
        if (remote != null) {
            resultList.add(findBusinessInterface(remote));
        }
        return resultList;
    }
    
    @Override
    public final void delete(MethodModel interfaceMethod, boolean local) {
        List<MethodModel> impls = getImplementation(interfaceMethod);
        boolean checkOther = local ? hasRemote() : hasLocal();
        if (!impls.isEmpty()) {
            for (MethodModel impl : impls) {
                if (impl != null) { // could be null here if the method is missing
                    ClassMethodPair classMethodPair = getInterface(impl, !local);
                    if (((checkOther &&  classMethodPair == null)) || !checkOther) {
                        try {
                            removeMethodFromClass(classMethodPair.getClassName(), classMethodPair.getMethodModel());
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }

                    }
                }
            }
            try {
                removeMethodFromClass(getBeanClass(), interfaceMethod);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }

        }
    }
    
    @Override
    public boolean hasRemote() {
        String intf = remoteHome;
        if (!simplified) {
            if (intf == null) {
                return false;
            }
        }
        intf = remote;
        if (intf == null || findBusinessInterface(intf) == null) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean hasLocal() {
        String intf = localHome;
        if (!simplified) {
            if (intf == null) {
                return false;
            }
        }
        intf = local;
        if (intf == null || findBusinessInterface(intf) == null) {
            return false;
        }
        return true;
    }
    
    @Override
    public MethodModel getPrimaryImplementation(MethodModel intfView) {
        List<MethodModel> impls = getImplementation(intfView);
        return impls.isEmpty() ? null : impls.get(0);
    }
    
    @Override
    public String getRemote() {
        return remote;
    }
    
    @Override
    public String getLocal() {
        return local;
    }
    
    public String getLocalHome() {
        return localHome;
    }
    
    public String getHome() {
        return remoteHome;
    }
    
    protected boolean isSimplified() {
        return simplified;
        
    }
    
//    public final MethodModel addMethod(MethodModel method, boolean local, boolean isComponent) {
//        TypeElement javaClass = getBeanInterface(local, isComponent);
//        assert javaClass != null;
//        addMethodToClass(javaClass, method);
//        if (!local) {
//            method = addExceptionIfNecessary(method, RemoteException.class.getName());
//        }
//        createBeanMethod(method);
//        return method;
//    }
    
    public String getBeanInterface(boolean isLocal, boolean isComponent) {
        if (isComponent) {
            return findBusinessInterface(isLocal ? local : remote);
        } else {
            String className = isLocal ? localHome : remoteHome;
            return className;
        }
    }
    
    private void createBeanMethod(MethodModel method) throws IOException {
        if (hasJavaImplementation(method)) {
            List<MethodModel> implMethods = getImplementationMethods(method);
            for (MethodModel me : implMethods) {
                if (!findInClass(ejbClass, me)) {
                    addMethodToClass(ejbClass, method);
                }
            }
        }
    }
    
    public final void removeMethod(MethodModel method, boolean local, boolean isComponent) {
        String clazz = getBeanInterface(local, isComponent);
        MethodModel methodCopy = method;
        assert clazz != null;
        if (!local) {
            methodCopy = addExceptionIfNecessary(methodCopy, RemoteException.class.getName());
        }
        try {
            removeMethodFromClass(clazz, methodCopy);
            createBeanMethod(methodCopy);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

    }

    // util candidates
    // -------------------------------------------------------------------------
    
    protected boolean findInClass(final String clazz, final MethodModel methodModel) {
        try {
            return methodFindInClass(clazz, methodModel);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return false;
        }

    }

    private boolean methodFindInClass(final String clazz, final MethodModel methodModel) throws IOException {
        if (clazz == null) {
            return false;
        }
        FileObject ejbClassFO = model.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
            public FileObject run(EjbJarMetadata metadata) throws Exception {
                return metadata.findResource(Utils.toResourceName(ejbClass));
            }
        });
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final boolean [] result = new boolean[] {false};
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(clazz);
                for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(controller, method, methodModel)) {
                        result[0] = true;
                        return;
                    }
                }
            }
        }, true);
        return result[0];
    }
    
    protected void addMethodToClass(final String className, final MethodModel method) throws IOException {
        FileObject fileObject = model.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
            public FileObject run(EjbJarMetadata metadata) throws Exception {
                return metadata.findResource(Utils.toResourceName(className));
            }
        });
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                Trees trees = workingCopy.getTrees();
                TypeElement clazz = workingCopy.getElements().getTypeElement(className);
                ClassTree classTree = trees.getTree(clazz);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, method);
                ClassTree modifiedClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();
    }

    protected void removeMethodFromClass(final String className, final MethodModel methodModel) throws IOException {
        FileObject fileObject = model.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
            public FileObject run(EjbJarMetadata metadata) throws Exception {
                return metadata.findResource(Utils.toResourceName(className));
            }
        });
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                if (methodFindInClass(className, methodModel)) {
                    TypeElement foundClass = workingCopy.getElements().getTypeElement(className);
                    Trees trees = workingCopy.getTrees();
                    ClassTree classTree = trees.getTree(foundClass);
                    MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                    ClassTree modifiedClassTree = workingCopy.getTreeMaker().removeClassMember(classTree, methodTree);
                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            }
        }).commit();
    }

}