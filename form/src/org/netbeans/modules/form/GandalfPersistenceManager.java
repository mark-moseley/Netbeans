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

import java.beans.PropertyDescriptor;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/** 
*
* @author Ian Formanek
*/
public class GandalfPersistenceManager extends PersistenceManager {
  public static final String CURRENT_VERSION = "1.0";
  
  public static final String XML_FORM = "Form";
  public static final String XML_NON_VISUAL_COMPONENTS = "NonVisualComponents";
  public static final String XML_CONTAINER = "Container";
  public static final String XML_COMPONENT = "Component";
  public static final String XML_LAYOUT = "Layout";
  public static final String XML_CONSTRAINTS = "Constraints";
  public static final String XML_SUB_COMPONENTS = "SubComponents";
  public static final String XML_EVENTS = "Events";
  public static final String XML_EVENT = "EventHandler";
  public static final String XML_PROPERTIES = "Properties";
  public static final String XML_PROPERTY = "Property";
  
  public static final String ATTR_FORM_VERSION = "version";
  public static final String ATTR_COMPONENT_NAME = "name";
  public static final String ATTR_COMPONENT_CLASS = "class";
  public static final String ATTR_PROPERTY_NAME = "name";
  public static final String ATTR_PROPERTY_TYPE = "type";
  public static final String ATTR_PROPERTY_EDITOR = "editor";
  public static final String ATTR_PROPERTY_VALUE_TYPE = "valuetype";
  public static final String ATTR_PROPERTY_VALUE = "value";
  public static final String ATTR_EVENT_NAME = "event";
  public static final String ATTR_EVENT_HANDLER = "handler";

  public static final String VALUE_RAD_CONNECTION = "RADConnection";
  
  private static final String ONE_INDENT =  "  ";
  
  /** A method which allows the persistence manager to check whether it can read
  * given form format.
  * @return true if this PersistenceManager can load form stored in the specified form, false otherwise
  * @exception IOException if any problem occured when accessing the form
  */
  public boolean canLoadForm (FormDataObject formObject) throws IOException {
    /* try {
      InputStream is = formObject.getFormEntry ().getFile ().getInputStream();
      byte[] bytes = new byte[4];
      int len = is.read (bytes);
      return ((len == 4) && (bytes[0] == MAGIC_0) && (bytes[1] == MAGIC_1) && (bytes[2] == MAGIC_2) && (bytes[3] == MAGIC_3));
    } catch (Throwable t) {
      if (t instanceof ThreadDeath) {
        throw (ThreadDeath)t;
      }
      
      return false;
    }
*/
    return false;
  }

  /** Called to actually load the form stored in specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @return the FormManager2 representing the loaded form or null if some problem occured
  * @exception IOException if any problem occured when loading the form
  */
  public FormManager2 loadForm (FormDataObject formObject) throws IOException {
    FileObject formFile = formObject.getFormEntry ().getFile ();
    org.w3c.dom.Document doc = org.openide.loaders.XMLDataObject.parse (formFile.getURL ());
    return null;
  }

  /** Called to actually save the form represented by specified FormManager2 into specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @param manager the FormManager2 representing the form to be saved
  * @exception IOException if any problem occured when saving the form
  */
  public void saveForm (FormDataObject formObject, FormManager2 manager) throws IOException {
    FileObject formFile = formObject.getFormEntry ().getFile ();
    FileLock lock = null;
    java.io.OutputStream os = null;
    try {
      lock = formFile.lock ();
      StringBuffer buf = new StringBuffer ();
      
      // 1.store header
      buf.append ("<?xml version=\"1.0\"?>\n");
      buf.append ("\n");
      
      // 2.store body
      addElementOpenAttr (buf, XML_FORM, new String[] { ATTR_FORM_VERSION }, new String[] { CURRENT_VERSION });
      buf.append (ONE_INDENT); addElementOpen (buf, XML_NON_VISUAL_COMPONENTS);
      buf.append (ONE_INDENT); addElementClose (buf, XML_NON_VISUAL_COMPONENTS);
      buf.append ("\n");
      saveContainer ((ComponentContainer)manager.getRADForm ().getTopLevelComponent (), buf, ONE_INDENT);
      addElementClose (buf, XML_FORM);
      
      os = formFile.getOutputStream (lock); // [PENDING - first save to ByteArray for safety]
      os.write (buf.toString ().getBytes ());
    } finally {
      if (os != null) os.close ();
      if (lock != null) lock.releaseLock ();
    }
  }
  
  private void saveContainer (ComponentContainer container, StringBuffer buf, String indent) {
    if (container instanceof RADVisualContainer) {
      saveVisualComponent ((RADVisualComponent)container, buf, indent);
      buf.append ("\n");
      buf.append (indent); addElementOpen (buf, XML_LAYOUT);
      buf.append (indent); addElementClose (buf, XML_LAYOUT);
    } else {
      saveComponent ((RADComponent)container, buf, indent);
    }
    buf.append ("\n");
    buf.append (indent); addElementOpen (buf, XML_SUB_COMPONENTS);
    RADComponent[] children = container.getSubBeans ();
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof ComponentContainer) {
        buf.append (indent + ONE_INDENT); 
        addElementOpenAttr (
            buf, 
            XML_CONTAINER, 
            new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME }, 
            new String[] { children[i].getComponentClass ().getName (), children[i].getName () }
        );
        saveContainer ((ComponentContainer)children[i], buf, indent + ONE_INDENT + ONE_INDENT);
        buf.append (indent + ONE_INDENT); addElementClose (buf, XML_CONTAINER);
      } else {
        buf.append (indent + ONE_INDENT); 
        addElementOpenAttr (
            buf, 
            XML_COMPONENT, 
            new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME }, 
            new String[] { children[i].getComponentClass ().getName (), children[i].getName () }
        );
        if (children[i] instanceof RADVisualComponent) {
          saveVisualComponent ((RADVisualComponent)children[i], buf, indent + ONE_INDENT + ONE_INDENT);
        } else {
          saveComponent (children[i], buf, indent + ONE_INDENT + ONE_INDENT);
        }
        buf.append (indent + ONE_INDENT); addElementClose (buf, XML_COMPONENT);
      }
    }
    buf.append (indent); addElementClose (buf, XML_SUB_COMPONENTS);
  }

  private void saveVisualComponent (RADVisualComponent component, StringBuffer buf, String indent) {
    saveComponent (component, buf, indent);
    buf.append ("\n");
    buf.append (indent); addElementOpen (buf, XML_CONSTRAINTS);
    saveConstraints (component, buf, indent + ONE_INDENT);
    buf.append (indent); addElementClose (buf, XML_CONSTRAINTS);
  }
  
  private void saveComponent (RADComponent component, StringBuffer buf, String indent) {
    buf.append (indent); addElementOpen (buf, XML_PROPERTIES);
    saveProperties (component.getChangedProperties (), buf, indent + ONE_INDENT);
    buf.append (indent); addElementClose (buf, XML_PROPERTIES);
    buf.append ("\n");
    buf.append (indent); addElementOpen (buf, XML_EVENTS);
    saveEvents (component.getEventsList ().getEventNames (), buf, indent + ONE_INDENT);
    buf.append (indent); addElementClose (buf, XML_EVENTS);
  }

  private void saveProperties (Map changedProperties, StringBuffer buf, String indent) {
    for (Iterator it = changedProperties.keySet ().iterator (); it.hasNext (); ) {
      RADComponent.RADProperty prop = (RADComponent.RADProperty) it.next ();
      PropertyDescriptor desc = prop.getPropertyDescriptor ();
      Object value = changedProperties.get (prop);
      String valueType = value.getClass ().getName ();
      if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
        valueType = VALUE_RAD_CONNECTION;
      }
      buf.append (indent); 
      addElementOpenAttr (
          buf, 
          XML_PROPERTY, 
          new String[] { 
            ATTR_PROPERTY_NAME, 
            ATTR_PROPERTY_TYPE, 
            ATTR_PROPERTY_EDITOR, 
            ATTR_PROPERTY_VALUE_TYPE, 
            ATTR_PROPERTY_VALUE },
          new String[] { 
            desc.getName (), 
            desc.getPropertyType ().getName (), 
            prop.getCurrentEditor ().getClass ().getName (), 
            valueType, 
            encodeValue (value) 
          }
      );
      buf.append (indent); addElementClose (buf, XML_PROPERTY);
    }
  }

  private void saveEvents (Hashtable events, StringBuffer buf, String indent) {
    for (Iterator it = events.keySet ().iterator (); it.hasNext (); ) {
      String eventName = (String)it.next ();
      String handlerName = (String)events.get (eventName);
      
      buf.append (indent); 
      addElementOpenAttr (
          buf, 
          XML_EVENT, 
          new String[] { 
            ATTR_EVENT_NAME, 
            ATTR_EVENT_HANDLER 
          },
          new String[] { 
            eventName, 
            handlerName, 
          }
      );
      buf.append (indent); addElementClose (buf, XML_EVENT);
    }
  }
    
  private void saveConstraints (RADVisualComponent component, StringBuffer buf, String indent) {
  }
  
// --------------------------------------------------------------------------------------
// Utility formatting methods
  
  private String encodeValue (Object value) {
    if (value == null) return "null";
   
    if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
      RADConnectionPropertyEditor.RADConnectionDesignValue radConn = (RADConnectionPropertyEditor.RADConnectionDesignValue) value;
      StringBuffer sb = new StringBuffer ();
      switch (radConn.type) {
        case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_PROPERTY:  
          sb.append ("type=property"); 
          sb.append (";component=");
          sb.append (radConn.radComponentName);
          sb.append (";name=");
          sb.append (radConn.propertyName);
          break;
        case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_METHOD:  
          sb.append ("type=method"); 
          sb.append (";component=");
          sb.append (radConn.radComponentName);
          sb.append (";name=");
          sb.append (radConn.propertyName);
          break;
        case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE:  
          sb.append ("type=code"); 
          sb.append (";code=");
          sb.append (Utilities.replaceString (radConn.userCode, "\n", "\\n"));
          break;
        case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_VALUE:  
          sb.append ("type=value"); 
          sb.append (";value=");
          sb.append (radConn.value);
          break;
      }
      return sb.toString ();
    }
    
    if ((value instanceof Integer) || 
        (value instanceof Short) ||
        (value instanceof Byte) ||
        (value instanceof Long) ||
        (value instanceof Float) ||
        (value instanceof Double) ||
        (value instanceof Boolean) ||
        (value instanceof Character) ||
        (value instanceof String)) {
       return value.toString ();
     } 
     
     if (value instanceof Class) {
       return ((Class)value).getName ();
     }
     
     // [PENDING - Color, Font, ...]     
     
     ByteArrayOutputStream bos = new ByteArrayOutputStream ();
     try {
       ObjectOutputStream oos = new ObjectOutputStream (bos);
       oos.writeObject (value);
       oos.close ();
     } catch (Exception e) {
       return "null"; // problem during serialization
     }
     return bos.toString ();
  }
  
  private void addElementOpen (StringBuffer buf, String elementName) {
    buf.append ("<");
    buf.append (elementName);
    buf.append (">\n");
  }

  private void addElementOpenAttr (StringBuffer buf, String elementName, String[] attrNames, String[] attrValues) {
    buf.append ("<");
    buf.append (elementName);
    for (int i = 0; i < attrNames.length; i++) {
      buf.append (" ");
      buf.append (attrNames[i]);
      buf.append ("=\"");
      buf.append (attrValues[i]);
      buf.append ("\"");
    }
    buf.append (">\n");
  }
  
  private void addElementClose (StringBuffer buf, String elementName) {
    buf.append ("</");
    buf.append (elementName);
    buf.append (">\n");
  }
}

/*
 * Log
 *  5    Gandalf   1.4         6/28/99  Ian Formanek    First cut of XML 
 *       persistence
 *  4    Gandalf   1.3         6/24/99  Ian Formanek    
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/7/99   Ian Formanek    
 *  1    Gandalf   1.0         5/30/99  Ian Formanek    
 * $
 */
