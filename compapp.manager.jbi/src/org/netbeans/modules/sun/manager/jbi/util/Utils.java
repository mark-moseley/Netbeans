/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.sun.manager.jbi.util;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;

import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author jqian
 */
public class Utils {
    
    public static Image getBadgedIcon(Class clazz, String iconName,
            String internalBadgeIconName, String externalBadgeIconName) {
        
        Image ret = new ImageIcon(clazz.getResource(iconName)).getImage();
        
        if (internalBadgeIconName != null) {
            Image internalBadgeImg = new ImageIcon(clazz.getResource(internalBadgeIconName)).getImage();
            ret = Utilities.mergeImages(ret, internalBadgeImg, 7, 7);
        }
        
        if (externalBadgeIconName != null) {
            Image externalBadgeImg = new ImageIcon(clazz.getResource(externalBadgeIconName)).getImage();
            ret = Utilities.mergeImages(ret, externalBadgeImg, 15, 8);
        }
        
        return ret;
    }
    
    /**
     * Ensure that the specified ruannable task will run only in the event dispatch
     * thread.
     */
    public static void runInEventDispatchThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
    
    public static Map<Attribute, MBeanAttributeInfo> getIntrospectedPropertyMap(Object bean) {
        return getIntrospectedPropertyMap(bean, false);
    }
    
    public static Map<Attribute, MBeanAttributeInfo> getIntrospectedPropertyMap(Object bean, boolean sort) {
        
        Class beanClass = bean.getClass();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(beanClass, Object.class);
        } catch (IntrospectionException ex) {
            System.err.println("Couldn't introspect " + beanClass.getName()); // NOI18N
            return null;
        }
        
//        Map map = sort ? new TreeMap() : new HashMap();   // NB IDE BUG
        
        Map<Attribute, MBeanAttributeInfo> map = null;
        if (sort) {
            map = new TreeMap<Attribute, MBeanAttributeInfo>();
        } else {
            map = new HashMap<Attribute, MBeanAttributeInfo>();
        }
        
        PropertyDescriptor[] propDescriptors = beanInfo.getPropertyDescriptors();
        
        for (int i = 0; i < propDescriptors.length; i++) {
            Class propertyTypeClass = propDescriptors[i].getPropertyType();
            Method readMethod = propDescriptors[i].getReadMethod();
            Method writeMethod = propDescriptors[i].getWriteMethod();
            
            if (readMethod != null) {
                String propertyType = propertyTypeClass.getName();
                String propertyName = propDescriptors[i].getName();
                String propertyDesc = propDescriptors[i].getShortDescription();
                Object propertyValue = null;
                try {
                    propertyValue = readMethod.invoke(bean, (Object[])null);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                Attribute attr = new Attribute(propertyName, propertyValue);
                if (sort) {
                    attr = new ComparableAttribute(attr);
                }
                map.put(attr,
                        new MBeanAttributeInfo(propertyName, propertyType,
                        propertyDesc,
                        readMethod != null, writeMethod != null,
                        readMethod.getName().startsWith("is"))); // NOI18N
            }
        }
        
        return map;
    }
    
    private static Document getDocument(String xmlString) {
        try {
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(xmlString)));
            
        } catch (Exception e) {
            System.out.println("Error parsing XML string: " + e); // NOI18N
            return null;
        }
    }
}
