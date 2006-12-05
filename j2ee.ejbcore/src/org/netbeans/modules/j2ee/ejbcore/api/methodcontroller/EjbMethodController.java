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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * This class encapsulates functionality required for working with EJB methods.
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class EjbMethodController {
    
    public static EjbMethodController createFromClass(FileObject ejbClassFO, String className) {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(ejbClassFO);
        if (ejbModule == null) {
            return null;
        }
        DDProvider provider = DDProvider.getDefault();
        EjbJar ejbJar = null;
        EjbMethodController controller = null;
        try {
            ejbJar = provider.getMergedDDRoot(ejbModule.getMetadataUnit());
            EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
            if (beans != null) {
                Session session = (Session) beans.findBeanByName(EnterpriseBeans.SESSION, Ejb.EJB_CLASS, className);
                if (session != null) {
                    controller = new SessionMethodController(ejbClassFO, session);
                    // TODO EJB3: on Java EE 5.0 this always sets controller to null
                    if (!controller.hasLocal() && !controller.hasRemote()) {
                        // this is either an error or a web service 
                        controller = null;
                    }
                } else {
                    Entity entity = (Entity) beans.findBeanByName(EnterpriseBeans.ENTITY, Ejb.EJB_CLASS, className);
                    if (entity != null) {
                        controller = new EntityMethodController(ejbClassFO, entity, ejbJar);
                    }
                }
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return controller;
    }
    
    /**
     * Find the implementation methods
     * @return MethodElement representing the implementation method or null.
     */
    public abstract List getImplementation(MethodModel intfView);
    public abstract MethodModel getPrimaryImplementation(MethodModel intfView);
    /**
     * @return true if intfView has a java implementation.
     */
    public abstract boolean hasJavaImplementation(MethodModel intfView);
    public abstract boolean hasJavaImplementation(MethodType methodType);
    
    /**
     * return interface method in the requested interface. 
     * @param beanImpl implementation method
     * @param local true if local method should be returned false otherwise
     */
    public abstract ClassMethodPair getInterface(MethodModel beanImpl, boolean local);
    
    /** Return if the passed method is implementation of method defined 
     * in local or remote interface.
     * @param m Method from bean class.
     * @param methodType Type of method to define the search algorithm
     * @param local If <code>true</code> the local interface is searched,
     *              if <code>false</code> the remote interface is searched.
     */
    public abstract boolean hasMethodInInterface(MethodModel method, MethodType methodType, boolean local);
    
    /**
     * @param clientView of the method
     */
    public abstract MethodType getMethodTypeFromInterface(MethodModel clientView);
    public abstract MethodType getMethodTypeFromImpl(MethodModel implView);
    
    public abstract String getBeanClass();
    public abstract String getLocal();
    public abstract String getRemote();
    public abstract Collection<String> getLocalInterfaces();
    public abstract Collection<String> getRemoteInterfaces();
    public abstract boolean hasLocal();
    public abstract boolean hasRemote();
    public void addEjbQl(MethodModel clientView, String ejbql, FileObject ddFileObject) throws IOException {
        assert false: "ejbql not supported for this bean type";
    }
    
    public String createDefaultQL(MethodType methodType) {
        return null;
    }
    
    /**
     * create interface signature based on the given implementation
     */
    public abstract void createAndAddInterface(MethodModel beanImpl, boolean local);
    
    /**
     * create implementation methods based on the client method. 
     * @param clientView method which will be inserted into an interface
     * @param intf interface where element will be inserted. This can be the
     * use the business interface pattern.
     */
    public abstract void createAndAddImpl(MethodModel clientView);
    
    public abstract void delete(MethodModel interfaceMethod, boolean local);
    
    /** Checks if given method type is supported by controller.
     * @param type One of <code>METHOD_</code> constants in @link{MethodType}
     */
    public abstract boolean supportsMethodType(MethodType.Kind type);
    public abstract MethodModel createAndAdd(MethodModel clientView, boolean local, boolean component);
    
    
    /** Immutable type representing method and its enclosing class */
    protected static final class ClassMethodPair {
        
        private final String className;
        private final MethodModel methodModel;
        
        public ClassMethodPair(String className, MethodModel methodModel) {
            this.className = className;
            this.methodModel = methodModel;
        }
        
        public String getClassName() {
            return className;
        }
        
        public MethodModel getMethodModel() {
            return methodModel;
        }
        
    }
}
