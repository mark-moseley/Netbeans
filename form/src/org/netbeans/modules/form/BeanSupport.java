/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form;

import com.netbeans.ide.nodes.Node;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/** BeanSupport is a utility class with various static methods supporting 
* operations with JavaBeans.
*
* @author Ian Formanek
*/
public class BeanSupport {
// -----------------------------------------------------------------------------
// Private variables

  private static HashMap errorEmptyMap = new HashMap (10);
  private static HashMap valuesCache = new HashMap (30);

// -----------------------------------------------------------------------------
// Public methods

  /** Utility method to create an instance of given class. Returns null on error.
  * @param beanClass the class to create inctance of
  * @return new instance of specified class or null if an error occured during instantiation
  */
  public static Object createBeanInstance (Class beanClass) {
    try {
      return beanClass.newInstance ();
    } catch (IllegalAccessException e) {
      // problem => return null;
    } catch (InstantiationException e) {
      // problem => return null;
    }
    return null;
  }

  /** Utility method to obtain a BeanInfo of given JavaBean class. Returns null on error.
  * @param beanClass the class to obtain BeanInfo for
  * @return BeanInfo instance or null if an error occured or the BeanInfo cannot be found 
  *                  throughout the BeanInfoSearchPath
  */
  public static BeanInfo createBeanInfo (Class beanClass) {
    try {
      return Introspector.getBeanInfo (beanClass);
    } catch (IntrospectionException e) {
      return null;
    }
  }
  
  /** Utility method to obtain a default property values of specified JavaBean class.
  * The default values are property values immediately after the instance is created.
  * Because some AWT components initialize their properties only after the peer is
  * created, these are treated specially and default values for those properties
  * are provided explicitely (e.g. though the value of Font property of java.awt.Button
  * is null after an instance of Button is created, this method will return the
  * Font (Dialog, 12, PLAIN) as the default value).
  *
  * @param beanClass The Class of the JavaBean for which the default values are to be obtained
  * @returns Map containing pairs <PropertyName (String), value (Object)>
  * @see #getDefaultPropertyValue
  */
  public static Map getDefaultPropertyValues (Class beanClass) {
    Map defValues = (Map) valuesCache.get (beanClass);
    if (defValues == null) {
      Object beanInstance = createBeanInstance (beanClass);
      if (beanInstance == null)
        return errorEmptyMap;
      defValues = getPropertyValues (beanInstance);
      valuesCache.put (beanClass, defValues);
    }
    return defValues;
  }
  
  /** Utility method to obtain a default value of specified JavaBean class and property name.
  * The default values are property values immediately after the instance is created.
  * Because some AWT components initialize their properties only after the peer is
  * created, these are treated specially and default values for those properties
  * are provided explicitely (e.g. though the value of Font property of java.awt.Button
  * is null after an instance of Button is created, this method will return the
  * Font (Dialog, 12, PLAIN) as the default value).
  *
  * @param beanClass The Class of the JavaBean for which the default value is to be obtained
  * @param beanClass The name of the propertyn for which the default value is to be obtained
  * @returns The default property value for specified property on specified JavaBean class
  * @see #getDefaultPropertyValues
  */
  public Object getDefaultPropertyValue (Class beanClass, String propertyName) {
    return getDefaultPropertyValues (beanClass).get (propertyName);
  }

  /** Utility method to obtain a current property values of given JavaBean instance.
  * Only the properties specified in bean info (if it exists) are provided.
  *
  * @returns Map containing pairs <PropertyName (String), value (Object)>
  */
  public static Map getPropertyValues (Object beanInstance) {
    if (beanInstance == null) {
      return errorEmptyMap;
    }
    
    BeanInfo info = createBeanInfo (beanInstance.getClass ());
    PropertyDescriptor[] properties = info.getPropertyDescriptors ();
    HashMap defaultValues = new HashMap (properties.length * 2);
    
    for (int i = 0; i < properties.length; i++) {
      Method readMethod = properties[i].getReadMethod ();
      if (readMethod != null) {
        try {
          Object value = readMethod.invoke (beanInstance, new Object [0]);
          if (value == null)
            value = getSpecialDefaultAWTValue (beanInstance, properties[i].getName ());
          defaultValues.put (properties[i].getName (), value);
        } catch (Exception e) {
          // problem with reading property ==>> no default value
/*            if (ideSettings.getOutputLevel () != IDESettings.OUTPUT_MINIMUM) {
            notifyPropertyException (beanInstance.getClass (), properties [i].getName (), "component", e, true);
          } */
        } 
      } else { // the property does not have plain read method
        if (properties[i] instanceof IndexedPropertyDescriptor) {
//          [PENDING]
//          Method indexedReadMethod = ((IndexedPropertyDescriptor)properties[i]).getIndexedReadMethod ();
        } 
      }
    } 

    return defaultValues;
  }

  /** Utility method to obtain an icon of specified JavaBean class and property name.
  *
  * @param iconType The icon type as defined in BeanInfo (BeanInfo.ICON_COLOR_16x16, ...)
  * @returns The icon of specified JavaBean or null if not defined
  */
  public static Image getBeanIcon (Class beanClass, int iconType) {
    // [PENDING - icon according to instance data object]
    BeanInfo bi = createBeanInfo (beanClass);
    if (bi != null) {
      return bi.getIcon (iconType);
    }
    return null;
  }
  
  /** A utility method that returns a class of event adapter for
  * specified listener. It works only on known listeners from java.awt.event.
  * Null is returned for unknown listeners.
  * @return class of an adapter for specified listener or null if
  *               unknown/does not exist
  */
  public static Class getAdapterForListener (Class listener) {
    if (java.awt.event.ComponentListener.class.equals (listener))
      return java.awt.event.ComponentAdapter.class;
    else if (java.awt.event.ContainerListener.class.equals (listener))
      return java.awt.event.ContainerAdapter.class;
    else if (java.awt.event.FocusListener.class.equals (listener))
      return java.awt.event.FocusAdapter.class;
    else if (java.awt.event.KeyListener.class.equals (listener))
      return java.awt.event.KeyAdapter.class;
    else if (java.awt.event.MouseListener.class.equals (listener))
      return java.awt.event.MouseAdapter.class;
    else if (java.awt.event.MouseMotionListener.class.equals (listener))
      return java.awt.event.MouseMotionAdapter.class;
    else if (java.awt.event.WindowListener.class.equals (listener))
      return java.awt.event.WindowAdapter.class;
    else return null; // not found
  }

  public static Node.Property [] createBeanProperties (Object beanInstance) {
    BeanInfo beanInfo = createBeanInfo (beanInstance.getClass ());
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();
    ArrayList nodeProps = new ArrayList ();
    for (int i = 0; i < props.length; i++) {
    }

    Node.Property[] np = new Node.Property [nodeProps.size ()];
    nodeProps.toArray (np);

    return np;
  }
  
  public static Node.Property [] createBeanExpertProperties (Object beanInstance) {
    BeanInfo beanInfo = createBeanInfo (beanInstance.getClass ());
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();
    ArrayList nodeProps = new ArrayList ();
    for (int i = 0; i < props.length; i++) {
    }

    Node.Property[] np = new Node.Property [nodeProps.size ()];
    nodeProps.toArray (np);

    return np;
  }
  
// -----------------------------------------------------------------------------
// Private methods

  private static Object getSpecialDefaultAWTValue (Object beanObject, String propertyName) {
    if ((beanObject instanceof Label) ||
        (beanObject instanceof Button) ||
        (beanObject instanceof TextField) ||
        (beanObject instanceof TextArea) ||
        (beanObject instanceof Checkbox) ||
        (beanObject instanceof Choice) ||
        (beanObject instanceof List) ||
        (beanObject instanceof Scrollbar) ||
        (beanObject instanceof ScrollPane) ||
        (beanObject instanceof Panel)) {
      if ("background".equals (propertyName))
        return Color.lightGray;
      else if ("foreground".equals (propertyName))
        return Color.black;
      else if ("font".equals (propertyName))
        return new Font ("Dialog", Font.PLAIN, 12);
    }
    return null;
  }

}

/*
 * Log
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
