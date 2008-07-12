/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.saas.ui.wizards;

//import com.sun.tools.ws.processor.model.java.JavaMethod;
//import com.sun.tools.ws.processor.model.java.JavaParameter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.net.URL;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.util.TypeUtil;

/**
 *
 * @author  David Botterill
 */
public class ReflectionHelper {
    
    public static TypeNodeData createTypeData(String typeName, Object value) {
        return createTypeData(typeName, null, value);
    }
    
    public static TypeNodeData createTypeData(String typeName, String paramName) {
        return createTypeData(typeName, paramName, null);
    }
    
    public static TypeNodeData createTypeData(String typeName, String paramName, Object value) {
        int separator = separateGenericType(typeName);
        
        String typeClass = typeName.substring(0, separator);
        String genericType = null;
        if (separator != typeName.length()) {
            genericType = typeName.substring(separator+1, typeName.length()-1);
        }
        
        TypeNodeData result = new TypeNodeData(typeClass, genericType, paramName, value);
        if (isArray(typeClass)) {
            result.setGenericType(typeClass.substring(0, typeClass.indexOf("[]"))); // NOI18N
        }
        
        return result;
    }
    
    private static int separateGenericType(String typeName) {
        int length = typeName.length();
        
        if (length < 2 || typeName.charAt(length-1) != '>') { // NOI18N
            return length;
        }else {
            int depth = 1;
            for (int i = length - 2; i >= 0; i--) {
                if (typeName.charAt(i) == '>') {
                    depth += 1;
                }else if (typeName.charAt(i) == '<') {
                    depth -= 1;
                }
                
                if (depth == 0) {
                    return i;
                }
            }
            
            return length;
        }
    }
    
    public static boolean isArray(String className) {
        return className != null && className.indexOf("[]") >= 0; // NOI18N
    }
    
    public static boolean isComplexType(String className, ClassLoader runtimeClassLoader) {
        try {
            Class type = Class.forName(className, true, runtimeClassLoader);
            Class xmlType = Class.forName(XmlType.class.getName(), true, runtimeClassLoader);

            Annotation xmlAnnotation = type.getAnnotation(xmlType);
            boolean isEnumeration = isEnumeration(className, runtimeClassLoader);
            
            return xmlAnnotation != null && !isEnumeration;
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }
    
    public static boolean isCollection(String className, ClassLoader runtimeClassLoader) {
        try {
            Class cls = Class.forName(className, true, runtimeClassLoader);
            return Collection.class.isAssignableFrom(cls);
        }catch (ClassNotFoundException cnfe) {
            return false;
        }
    }
    
    public static boolean isHolder(String className) {
        return "javax.xml.ws.Holder".equals(className); // NOI18N
    }
    
    public static boolean isJAXBElement(String className) {
        return "javax.xml.bind.JAXBElement".equals(className); // NOI18N
    }
    
    public static boolean isEnumeration(String className, ClassLoader loader) {
        try {
            Class cls = Class.forName(className, true, loader);
            Class enumClass = Class.forName(Enum.class.getName(), true, loader);
            
            return enumClass.isAssignableFrom(cls);
        }catch (ClassNotFoundException cnfe) {
            return false;
        }
    }
    
    public static boolean isSimpleType(String className, ClassLoader loader) {
        try {
            if (isPrimitiveClass(className)) {
                return true;
            }
            
            // Make sure the class is valid
            Class.forName(className, true, loader);
            
            return !isCollection(className, loader) && !isHolder(className) && 
                    !isComplexType(className, loader) && !isEnumeration(className, loader) &&!isJAXBElement(className);
        }catch (ClassNotFoundException cnfe) {
            return false;
        }
    }

    public static Object makeGenericArray(String componentType, int length, ClassLoader loader)
            throws WebServiceReflectionException {
        try {
            Class componentClass = null;
            if (isPrimitiveClass(componentType)) {
                componentClass = getPrimitiveClass(componentType);
            }else {
                componentClass = Class.forName(componentType, true, loader);
            }
            
            return Array.newInstance(componentClass, length);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static Object makeJAXBElement(String valueType, String localPart, Object value, ClassLoader loader)
            throws WebServiceReflectionException {
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (valueType == null || loader == null) {
                return null;
            }
            
            Thread.currentThread().setContextClassLoader(loader);
            Class declaredClass = null;
            
            if (isPrimitiveClass(valueType)) {
                declaredClass = getPrimitiveClass(valueType);
            }else {
                declaredClass = Class.forName(valueType, true, loader);
            }
            
            Class qNameClass = Class.forName(QName.class.getName(), true, loader);
            Class jaxBClass = Class.forName(JAXBElement.class.getName(), true, loader);
            
            Constructor qNameConstr = qNameClass.getConstructor(new Class[] { String.class } );
            Object qName = qNameConstr.newInstance(localPart);
            
            Constructor jaxBConstr = jaxBClass.getConstructor(new Class[] { qNameClass, Class.class, Object.class });
            return jaxBConstr.newInstance(qName, declaredClass, value);
        }catch (ClassNotFoundException cnfe) {
            throw new WebServiceReflectionException("ClassNotFoundException", cnfe);
        }catch (InstantiationException ie) {
            throw new WebServiceReflectionException("InstantiationException", ie);
        }catch (IllegalAccessException iae) {
            throw new WebServiceReflectionException("IllegalAccessException", iae);
        }catch (InvocationTargetException ite) {
            throw new WebServiceReflectionException("InvocationTargetException", ite);
        }catch (NoSuchMethodException nsme) {
            throw new WebServiceReflectionException("NoSuchMethodException", nsme);
        }finally {
            if (savedLoader != null)
                Thread.currentThread().setContextClassLoader(savedLoader);
        }        
    }
    
    public static Object makeEnumeration(String enumeration, ClassLoader loader)
            throws WebServiceReflectionException {
        try {
            List<String> enumerationValues = getEnumerationValues(enumeration, loader);
            Class enumClass = Class.forName(enumeration, true, loader);
            
            return Enum.valueOf(enumClass, enumerationValues.get(0));
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static Object getEnumeration(String enumeration, String name, ClassLoader loader) 
            throws WebServiceReflectionException {
        try {
            Class enumClass = Class.forName(enumeration, true, loader);
            return Enum.valueOf(enumClass, name);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static Object makeHolder(ClassLoader loader) throws WebServiceReflectionException {
        return makeComplexType("javax.xml.ws.Holder", loader); // NOI18N
    }
    
    public static Object makeCollection(String className, ClassLoader loader) 
            throws WebServiceReflectionException {
        
        ClassLoader savedLoader = null;
        try {
            if (!isCollection(className, loader)) {
                return null;
            }else {
                Class cls = Class.forName(className, true, loader);
                if (cls.isInterface()) {
                    return new ArrayList();
                }else {
                    savedLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(loader);
                    
                    Object result = cls.newInstance();
                    return result;
                }
            }
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }finally {
            if (savedLoader != null) {
                Thread.currentThread().setContextClassLoader(savedLoader);
            }
        }
    }
    
    public static Object makeComplexType(String typeName, ClassLoader loader) 
    throws WebServiceReflectionException {
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (typeName == null || loader == null) {
                return null;
            }
            
            Thread.currentThread().setContextClassLoader(loader);
            Class typeClass = Class.forName(typeName, true, loader);
            Object result = typeClass.newInstance();
            
            return result;
        }catch (ClassNotFoundException cnfe) {
            throw new WebServiceReflectionException("ClassNotFoundException", cnfe);
        }catch (InstantiationException ie) {
            throw new WebServiceReflectionException("InstantiationException", ie);
        }catch (IllegalAccessException iae) {
            throw new WebServiceReflectionException("IllegalAccessException", iae);
        }finally {
            if (savedLoader != null)
                Thread.currentThread().setContextClassLoader(savedLoader);
        }
    }
    
    public static List<String> getEnumerationValues(String enumeration, ClassLoader loader) 
            throws WebServiceReflectionException {
        try {
            List<String> enumerations = new ArrayList<String>();
            Class enumerClass = Class.forName(enumeration, true, loader);
            
            Field[] fields = enumerClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field nextField = fields[i];
                if (nextField.isEnumConstant()) {
                    enumerations.add(nextField.getName());
                }
            }
            
            return enumerations;
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static List<String> getPropertyNames(String complexType, ClassLoader loader)
            throws WebServiceReflectionException {
        ClassLoader savedLoader = null;
        try {
            List<String> properties = new ArrayList<String>();
            savedLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            
            Class nextClass = Class.forName(complexType, true, loader);
            Class xmlTypeClass = Class.forName(XmlType.class.getName(), true, loader);
            
            for ( ; nextClass != null; nextClass = nextClass.getSuperclass()) {
                Annotation annotation = nextClass.getAnnotation(xmlTypeClass);
                if (annotation == null) {
                    break;
                }
                
                try {
                    Method m = annotation.getClass().getMethod("propOrder", new Class[0]); // NOI18N
                    String[] props = (String[])m.invoke(annotation, null);
                    
                    for (int i = 0; props != null && i < props.length; i++) {
                        if (props[i] != null && props[i].length() > 0)
                            properties.add(props[i]);
                    }
                }catch (Exception ex) {
                }
            }
            
            return properties;
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }finally {
            if (savedLoader != null) {
                Thread.currentThread().setContextClassLoader(savedLoader);
            }
        }
    }

    public static String getPropertyType(String type, String propName, ClassLoader loader) 
        throws WebServiceReflectionException {
        ClassLoader savedLoader = null;
        try {
            Class typeClass = Class.forName(type, true, loader);
            
            char[] name = propName.toCharArray();
            String propCaps = null;
            
            Method method = null;
            
            for (int i = 0; i < propName.length() && method == null; i++ ) {
                name[i] = Character.toUpperCase(name[i]);
                propCaps = new String(name);
                try {
                    method = typeClass.getMethod("get" + propCaps, new Class[0]); // NOI18N
                } catch (NoSuchMethodException ex) {
                    try {
                        method = typeClass.getMethod("is" + propCaps, new Class[0]); // NOI18N
                    } catch (NoSuchMethodException nsme) {
                        continue;
                    }
                }
            }
            if (method == null) {
                throw new NoSuchMethodException("Method not found for property " + propName + " in class " + type);
            }
            
            return TypeUtil.typeToString(method.getGenericReturnType());
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }finally {
            if (savedLoader != null) {
                Thread.currentThread().setContextClassLoader(savedLoader);
            }            
        }
    }
    
    public static Object getHolderValue(Object holder) throws WebServiceReflectionException {
        try {
            Field valueField = holder.getClass().getField("value"); // NO18N
            return valueField.get(holder);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static void setHolderValue(Object holder, Object value) throws WebServiceReflectionException {
        try {
            Field valueField = holder.getClass().getField("value"); // NO18N
            valueField.set(holder, value);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static Object getJAXBElementValue(Object jaxBElement) throws WebServiceReflectionException {
        try {
            Method m = jaxBElement.getClass().getMethod("getValue", new Class[0]); // NOI18N
            return m.invoke(jaxBElement);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static String getQNameLocalPart(Object jaxBElement) throws WebServiceReflectionException {
        try {
            Method getName = jaxBElement.getClass().getMethod("getName", new Class[0]); // NOI18N
            Object qName = getName.invoke(jaxBElement);
            
            Method getLocalPart = qName.getClass().getMethod("getLocalPart", new Class[0]); // NOI18N
            return (String)getLocalPart.invoke(qName);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }        
    }
    
    public static void setJAXBElementValue(Object jaxBElement, Object value) throws WebServiceReflectionException {
        try {
            Method m = jaxBElement.getClass().getMethod("setValue", new Class[] { Object.class });
            m.invoke(jaxBElement, value);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static boolean isPropertySettable(String className, String propName, ClassLoader classLoader)
            throws WebServiceReflectionException {
        ClassLoader savedLoader = null;
        try {
            char[] name = propName.toCharArray();
            name[0] = Character.toUpperCase(name[0]);
            String capitalProp = new String(name);

            String writeMethod = "set" + capitalProp; // NOI18N
            
            savedLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            
            Class structClass = Class.forName(className, true, classLoader);
            Method[] methods = structClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method curMethod = methods[i];
                
                if (curMethod.getName().equals(writeMethod) && curMethod.getParameterTypes().length == 1) {
                    return true;
                }
            }
            
            return false;
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }finally {
            if (savedLoader != null) {
                Thread.currentThread().setContextClassLoader(savedLoader);
            }
        }
    }
    
    public static void setPropertyValue(Object objValue, String propName, 
            String propType, Object propValue, ClassLoader classLoader) throws WebServiceReflectionException {
        ClassLoader savedLoader = null;
        try {
            Class typeClass = objValue.getClass();
            
            Class propClass = null;
            savedLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            
            try {
                if (isPrimitiveClass(propType)) {
                    propClass = referenceClass2PrimitiveClass(propValue.getClass());
                }else {
                    propClass = Class.forName(propType, true, classLoader);
                }
                
                Class[] params = new Class[] { propClass };
                
                char[] name = propName.toCharArray();
                String methodName = null;
                Method method = null;
                
                for (int i = 0; i < propName.length() && method == null; i++) {
                    name[i] = Character.toUpperCase(name[i]);
                    methodName = "set" + new String(name); // NOI18N
                    
                    try {
                        method = typeClass.getMethod(methodName, params);
                    }catch (NoSuchMethodException nsme) {
                        continue;
                    }
                }
                
                if (method == null) {
                    throw new NoSuchMethodException("Method setter for property " + propName + " not found in class " + typeClass);
                }
                
                Object[] args = new Object[] { propValue };
                method.invoke(objValue, args);
            }catch (ClassNotFoundException cnfe) {
                throw new WebServiceReflectionException("ClassNotFoundException", cnfe);
            }catch (NoSuchMethodException nsme) {
                throw new WebServiceReflectionException("NoSuchMethodException", nsme);
            }catch (IllegalAccessException iae) {
                throw new WebServiceReflectionException("IllegalAccessException", iae);
            }catch (InvocationTargetException ite) {
                throw new WebServiceReflectionException("InvocationTargetException", ite);
            }
            
        }finally {
            if (savedLoader != null) {
                Thread.currentThread().setContextClassLoader(savedLoader);
            }
        }
    }

    public static Object getPropertyValue(Object obj, String propertyName,
            ClassLoader classLoader) throws WebServiceReflectionException {
        ClassLoader savedLoader = null;
        try {
            Class typeClass = obj.getClass();
            Method method = null;
            
            savedLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            
            char[] name = propertyName.toCharArray();
            String propCaps = null;
            for (int i = 0; i < propertyName.length() && method == null; i++ ) {
                name[i] = Character.toUpperCase(name[i]);
                propCaps = new String(name);
                try {
                    method = typeClass.getMethod("get" + propCaps, new Class[0]); // NOI18N
                } catch (NoSuchMethodException ex) {
                    try {
                        method = typeClass.getMethod("is" + propCaps, new Class[0]); // NOI18N
                    } catch (NoSuchMethodException nsme) {
                        continue;
                    }
                }
            }
            
            try {
                if (method == null) {
                    throw new NoSuchMethodException("Method not found for property " + propertyName + " in class " + typeClass);
                }
                
                return method.invoke(obj,new Object[0]);
            }catch (Exception ex) {
                throw new WebServiceReflectionException(ex.getClass().getName(), ex);
            }
        }finally {
            if (savedLoader != null) {
                Thread.currentThread().setContextClassLoader(savedLoader);
            }
        }
    }
    
    public static Object getArrayValue(Object array, int index) throws WebServiceReflectionException {
        try {
            return Array.get(array, index);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }
    
    public static int getArrayLength(Object array) throws WebServiceReflectionException {
        try {
            return Array.getLength(array);
        }catch (Exception ex) {
            throw new WebServiceReflectionException(ex.getClass().getName(), ex);
        }
    }    
    
    //FIXME - Refactor
//    public static Object callMethodWithParams(
//            String inClassName, LinkedList inParamList, JavaMethod inMethod,
//            URLClassLoader urlClassLoader, WsdlData wsData, WSPort port) throws WebServiceReflectionException {
//
//        Class clazz = null;
//        Class serviceClass = null;
//        if(null == urlClassLoader) return null;
//        
//        /**
//         * We need to save off the current classLoader and set the context to the one passed in for
//         * executing the method.
//         */        
//        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
//        try {
//            /**
//             * Now set the new classLoader to the one passed in.
//             */
//            Thread.currentThread().setContextClassLoader(urlClassLoader);
//
//            /**
//             * Get an instance of the Class
//             */
//            try {
//                serviceClass = Class.forName(inClassName, true, urlClassLoader);
//            } catch (ClassNotFoundException cnfe) {
//                throw new WebServiceReflectionException("ClassNotFoundException", cnfe);
//            }
//
//            /**
//             * Instantiate the Class so we can call the method on it.
//             */
//            Object classInstance = null;
//            try {
//                
//                URL wsdlUrl = wsData.getJaxWsDescriptor().getWsdlUrl();
//                String urlPath = wsdlUrl.getPath();
//                int start;
//                if (wsdlUrl.getProtocol().toLowerCase().startsWith("file")) {
//                    // NOI18N
//                    start = urlPath.lastIndexOf(System.getProperty("path.separator"));
//                    start = (start < 0) ? urlPath.lastIndexOf("/") : start;
//                } else {
//                    start = urlPath.lastIndexOf("/");
//                }
//                start = (start < 0 || start >= urlPath.length() - 1) ? 0 : start + 1;
//
//                String wsdlFileName = urlPath.substring(start);
//                String namespace = wsData.getJaxWsDescriptor().getModel().getNamespaceURI();
//                String qname = wsData.getJaxWsDescriptor().getName();
//
//                URL jarWsdlUrl = serviceClass.getResource(wsdlFileName);
//                QName name = new QName(namespace, qname);
//
//                Constructor constructor = serviceClass.getConstructor(java.net.URL.class, javax.xml.namespace.QName.class);
//                
//                Object serviceObject = constructor.newInstance(jarWsdlUrl, name);
//
//                String portGetter = port.getPortGetter();
//                Method getPort = serviceObject.getClass().getMethod(portGetter);
//                
//                classInstance = getPort.invoke(serviceObject);
//                clazz = classInstance.getClass();
//            } catch (InstantiationException ia) {
//                throw new WebServiceReflectionException("InstantiationExceptoin", ia);
//            } catch (IllegalAccessException iae) {
//                throw new WebServiceReflectionException("IllegalAccessException", iae);
//            } catch (NoSuchMethodException nsme) {
//                throw new WebServiceReflectionException("NoSuchMethodException", nsme);
//            } catch (InvocationTargetException ite) {
//                throw new WebServiceReflectionException("InvocationTargetException", ite);
//            }
//
//
//            Method method = null;
//            Object[] paramValues = inParamList.toArray();
//            /**
//             * Take the parameters and make an array of Classes based on the type of each Object.
//             * For each parameter, we need to have the type of the original parameter for the JavaMethod
//             * and do the following conversions:
//             * 1. from ArrayList to a typed array. (done prior)
//             * 2. from objects to primitives
//             */
//            LinkedList classList = new LinkedList();
//            List parameterList = inMethod.getParametersList();
//            for (int ii = 0; null != paramValues && ii < paramValues.length; ii++) {
//
//                /**
//                 * If the parameter type is a primitive, we've stored the value as a reference
//                 * type and need to convert it back to a primitive.
//                 */
//                Class classToAdd = null;
//                if (null != parameterList && ii < parameterList.size()) {
//                    JavaParameter actualParameter = (JavaParameter) parameterList.get(ii);
//                    if (isPrimitiveClass(actualParameter.getType().getFormalName())) {
//                        classToAdd = referenceClass2PrimitiveClass(paramValues[ii].getClass());
//                    } else if (actualParameter.getType().getFormalName().equals("java.util.Calendar") && !actualParameter.isHolder()) {
//                        classToAdd = java.util.Calendar.class;
//                    }else if (paramValues[ii] == null) {
//                        try {
//                            classToAdd = Class.forName(actualParameter.getType().getFormalName(), true, urlClassLoader);
//                        }catch (Exception ex) {
//                            throw new WebServiceReflectionException("Exception", ex);
//                        }
//                    } else {
//                        classToAdd = paramValues[ii].getClass();
//                    }
//                }
//                classList.add(classToAdd);
//            }
//            Class[] paramClasses = (Class[]) classList.toArray(new Class[0]);
//
//            /**
//             * Now instantiate the method to call.
//             */
//            try {
//                method = clazz.getMethod(inMethod.getName(), paramClasses);
//            } catch (NoSuchMethodException nsme) {
//                throw new WebServiceReflectionException("NoSuchMethodException", nsme);
//            }
//
//
//            Object returnObject = null;
//            try {
//                returnObject = method.invoke(classInstance, paramValues);
//            } catch (InvocationTargetException ite) {
//                throw new WebServiceReflectionException("InvocationTargetException", ite);
//            } catch (IllegalArgumentException ia) {
//                throw new WebServiceReflectionException("IllegalArgumentException", ia);
//            } catch (IllegalAccessException iae) {
//                throw new WebServiceReflectionException("IllegalAccessException", iae);
//            } catch (Exception e) {
//                throw new WebServiceReflectionException("Exception", e);
//            }
//
//            return returnObject;
//        }finally {
//            // Reset the classloader
//            Thread.currentThread().setContextClassLoader(savedLoader);
//        }
//        
//    }

    public static boolean isPrimitiveClass(String inType) {
        if(inType.equalsIgnoreCase("int")) {
            return true;
        } else if(inType.equalsIgnoreCase("byte")) {
            return true;
        } else if(inType.equalsIgnoreCase("boolean")) {
            return true;
        } else if(inType.equalsIgnoreCase("float")) {
            return true;
        } else if(inType.equalsIgnoreCase("double")) {
            return true;
        } else if(inType.equalsIgnoreCase("long")) {
            return true;
        } else if(inType.equalsIgnoreCase("short")) {
            return true;
        } else return false;
    }
    
    public static Class getPrimitiveClass(String inType) {
        if(inType.equalsIgnoreCase("int")) {
            return int.class;
        } else if(inType.equalsIgnoreCase("byte")) {
            return byte.class;
        } else if(inType.equalsIgnoreCase("boolean")) {
            return boolean.class;
        } else if(inType.equalsIgnoreCase("float")) {
            return float.class;
        } else if(inType.equalsIgnoreCase("double")) {
            return double.class;
        } else if(inType.equalsIgnoreCase("long")) {
            return long.class;
        } else if(inType.equalsIgnoreCase("short")) {
            return short.class;
        } else return null;
    }
    
    public static Class referenceClass2PrimitiveClass(Class inClass) {
        if(null == inClass) return inClass;
        if(inClass.getName().equalsIgnoreCase("java.lang.Boolean")) {
            return boolean.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Byte")) {
            return byte.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Double")) {
            return double.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Float")) {
            return float.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Integer")) {
            return int.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Long")) {
            return long.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Short")) {
            return short.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Character")) {
            return char.class;
        } else return inClass;
    }
    
}
