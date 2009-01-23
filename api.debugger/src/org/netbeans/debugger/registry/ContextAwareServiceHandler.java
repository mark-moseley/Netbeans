/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.debugger.registry;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Lookup;

/**
 * Handler of context aware services that implement one or more interface.
 * The services are registered through {@link DebuggerServiceRegistration} annotation.
 *
 * This handler guarantees that it creates only one service for given context and given set of interfaces.
 * Method {@link ContextAwareService#forContext(org.netbeans.spi.debugger.ContextProvider)}
 * returns the instance of the actual registered class. However, if necessary,
 * this can be changed to return another proxy implementation, that will delegate
 * to an instance of the actual registered class. This will allow to mask some
 * methods with attributes and also can prevent from too early class loading.
 *
 * @author Martin Entlicher
 */
public class ContextAwareServiceHandler implements InvocationHandler {

    public static final String SERVICE_NAME = "serviceName"; // NOI18N
    static final String SERVICE_CLASSES = "serviceClasses"; // NOI18N

    private String serviceName;
    private Class[] serviceClasses;
    private Map methodValues;
    //private ContextProvider context;
    private Object delegate;

    private Map<ContextProvider, Object> contextInstances = new WeakHashMap<ContextProvider, Object>();
    private WeakReference<Object> noContextInstance = new WeakReference<Object>(null);

    private ContextAwareServiceHandler(String serviceName, Class[] serviceClasses,
                                       Map methodValues) {
        this(serviceName, serviceClasses, methodValues, null);
    }

    private ContextAwareServiceHandler(String serviceName, Class[] serviceClasses,
                                       Map methodValues, ContextProvider context) {
        this.serviceName = serviceName;
        this.serviceClasses = serviceClasses;
        this.methodValues = methodValues;
        //this.context = context;
    }
    
    /*
    private synchronized Object getDelegate() {
        if (delegate == null) {
            delegate = ContextAwareSupport.createInstance(serviceName, context);
        }
        return delegate;
    }
    */

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("forContext")) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Have "+args.length+" arguments, expecting one argument.");
            }
            if (!(args[0] == null || args[0] instanceof ContextProvider)) {
                throw new IllegalArgumentException("Argument "+args[0]+" is not an instance of ContextProvider.");
            }
            ContextProvider context = (ContextProvider) args[0];
            /*if (context == this.context) {
                return proxy;
            }*/
            synchronized (this) {
                Object instance;
                if (context == null) {
                    instance = noContextInstance.get();
                } else {
                    instance = contextInstances.get(context);
                }
                if (instance == null) {
                    instance = ContextAwareSupport.createInstance(serviceName, context);
                    if (context == null) {
                        noContextInstance = new WeakReference(instance);
                    } else {
                        contextInstances.put(context, instance);
                    }
                }
                return instance;
            }
            /* If total laziness is necessary:
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            return Proxy.newProxyInstance(
                    cl,
                    serviceClasses,//new Class[] { serviceClass },
                    new ContextAwareServiceHandler(serviceName, serviceClasses, methodValues, context));
             */
        //} else if (methodValues.containsKey(methodName) && args.length == 0) {
        //    return methodValues.get(methodName);
        } else {
            /*
            try {
                return method.invoke(getDelegate(), args);
            } catch (IllegalArgumentException exc) {
                throw new UnsupportedOperationException(
                        "Method "+method.getName()+                             // NOI18N
                        " with arguments "+java.util.Arrays.asList(args)+       // NOI18N
                        " can not be called on this virtual object!", exc);     // NOI18N
            }
             */
            throw new UnsupportedOperationException(
                    "Method "+method.getName()+                             // NOI18N
                    " with arguments "+java.util.Arrays.asList(args)+       // NOI18N
                    " can not be called on this virtual object!");     // NOI18N
        }
    }

    /**
     * Creates instance of <code>ContextAwareService</code> based on layer.xml
     * attribute values
     *
     * @param attrs attributes loaded from layer.xml
     * @return new <code>ContextAwareService</code> instance
     */
    static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
        String serviceName = (String) attrs.get(SERVICE_NAME);
        String[] serviceClassNames = splitClasses((String) attrs.get(SERVICE_CLASSES));

        //Map methodValues = new HashMap(attrs); - MUST NOT DO THAT! Creates a loop initializing the entries from XML
        //methodValues.remove(SERVICE_NAME);
        //methodValues.remove(SERVICE_CLASS);

        ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
        Class[] classes = new Class[serviceClassNames.length + 1];
        classes[0] = ContextAwareService.class;
        for (int i = 0; i < serviceClassNames.length; i++) {
            classes[i+1] = Class.forName(serviceClassNames[i], true, cl);
        }
        return (ContextAwareService)
                Proxy.newProxyInstance(
                    cl,
                    classes,
                    new ContextAwareServiceHandler(serviceName, classes, Collections.EMPTY_MAP));
    }

    private static String[] splitClasses(String classes) {
        ArrayList<String> list = new ArrayList<String>();
        int i1 = 0;
        int i2;
        while ((i2 = classes.indexOf(',', i1)) > 0) {
            list.add(classes.substring(i1, i2).trim());
            i1 = i2 + 1;
        }
        list.add(classes.substring(i1).trim());
        return list.toArray(new String[] {});
    }

}
