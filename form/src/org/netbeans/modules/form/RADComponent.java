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

import com.netbeans.ide.nodes.*;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** RADComponent is a class which represents a single component used and instantiated
* during design time.  It provides its properties and events.
*
* @author Ian Formanek
*/
public class RADComponent {

// -----------------------------------------------------------------------------
// Private variables

  private Class beanClass;
  private Object beanInstance;
  private BeanInfo beanInfo;

  private String componentName;
  
  private Node.PropertySet[] beanPropertySets;
  private Node.Property[] beanProperties;
  private Node.Property[] beanExpertProperties;
  private Node.Property[] beanEvents;

  private HashMap auxValues;
  private HashMap changedPropertyValues;
  private Map defaultPropertyValues;

  private FormManager formManager;

// -----------------------------------------------------------------------------
// Constructors

  public RADComponent () {
    changedPropertyValues = new HashMap (30);
    auxValues = new HashMap (10);
  }

  public void initialize (FormManager formManager) {
    this.formManager = formManager;
  }

// -----------------------------------------------------------------------------
// Public interface

  public FormManager getFormManager () {
    return formManager;
  }
  
  public void setComponent (Class beanClass) {
    this.beanClass = beanClass;
    beanInstance = BeanSupport.createBeanInstance (beanClass);
    beanInfo = BeanSupport.createBeanInfo (beanClass);
    
    beanProperties = createBeanProperties ();
    beanExpertProperties = createBeanExpertProperties ();

    beanEvents = BeanSupport.createEventsProperties (beanInstance);

    changedPropertyValues = new HashMap ();
    defaultPropertyValues = BeanSupport.getDefaultPropertyValues (beanClass);
  }
  
  public Class getComponentClass () {
    return beanClass;
  }

  /** Getter for the Name property of the component - usually maps to variable declaration for holding the 
  * instance of the component
  * @return current value of the Name property
  */
  public String getName () {
    return componentName;
  }

  /** Setter for the Name property of the component - usually maps to variable declaration for holding the 
  * instance of the component
  * @param value new value of the Name property
  */
  public void setName (String value) {
    componentName = value;
    // [PENDING - fire change]
  }
  
  public Node.PropertySet[] getProperties () {
    if (beanPropertySets == null) {
      if (beanExpertProperties.length != 0) {
        // No expert properties
        beanPropertySets = new Node.PropertySet [] {
          new Node.PropertySet ("synthetic", "Synthetic", "Synthetic Properties") {
            public Node.Property[] getProperties () {
              return getSyntheticProperties ();
            }
          },
          new Node.PropertySet ("properties", "Properties", "Properties") {
            public Node.Property[] getProperties () {
              return getComponentProperties ();
            }
          },
          new Node.PropertySet ("events", "Events", "Events") {
            public Node.Property[] getProperties () {
              return getComponentEvents ();
            }
          }, 
        };
      } else {
        beanPropertySets = new Node.PropertySet [] {
          new Node.PropertySet ("synthetic", "Synthetic", "Synthetic Properties") {
            public Node.Property[] getProperties () {
              
              return getSyntheticProperties ();
            }
          },
          new Node.PropertySet ("properties", "Properties", "Properties") {
            public Node.Property[] getProperties () {
              return getComponentProperties ();
            }
          },
          new Node.PropertySet ("expert", "Expert", "Expert Properties") {
            public Node.Property[] getProperties () {
              return getComponentExpertProperties ();
            }
          },
          new Node.PropertySet ("events", "Events", "Events") {
            public Node.Property[] getProperties () {
              return getComponentEvents ();
            }
          }, 
        };
      }
    }
    return beanPropertySets;
  }
  
  public Map getChangedProperties () {
    return changedPropertyValues;
  }
  
  public void setAuxiliaryValue (String key, Object value) {
    auxValues.put (key, value);
  }

  public Object getAuxiliaryValue (String key) {
    return auxValues.get (key);
  }
  
// -----------------------------------------------------------------------------
// Parent-child

// -----------------------------------------------------------------------------
// Protected interface

  protected Node.Property[] getSyntheticProperties () {
    return getFormManager ().getCodeGenerator ().getSyntheticProperties (this);
  }

  protected Node.Property[] getComponentProperties () {
    return beanProperties;
  }
  
  protected Node.Property[] getComponentExpertProperties () {
    return beanExpertProperties;
  }

  protected Node.Property[] getComponentEvents () {
    return beanEvents;
  }

  protected Node.Property[] createBeanProperties () {
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();
    ArrayList nodeProps = new ArrayList ();
    for (int i = 0; i < props.length; i++) {
      if (!props[i].isHidden ()) {
        Node.Property prop = createProperty (props[i]);
        if (prop != null) // [PENDING - temporary]
          nodeProps.add (prop);
      }
    }

    Node.Property[] np = new Node.Property [nodeProps.size ()];
    nodeProps.toArray (np);

    return np;
  }

  protected Node.Property[] createBeanExpertProperties () {
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();
    ArrayList nodeProps = new ArrayList ();
    for (int i = 0; i < props.length; i++) {
      if (!props[i].isHidden ()) {
        Node.Property prop = createProperty (props[i]);
        if (prop != null) // [PENDING - temporary]
          nodeProps.add (prop);
      }
    }

    Node.Property[] np = new Node.Property [nodeProps.size ()];
    nodeProps.toArray (np);

    return np;
  }

  private Node.Property createProperty (final PropertyDescriptor desc) {
    Node.Property prop;
    if (desc instanceof IndexedPropertyDescriptor) {
      return null;
/*      IndexedPropertyDescriptor idesc = (IndexedPropertyDescriptor)desc;

      prop =  new IndexedPropertySupport (
        bean, idesc.getPropertyType (),
        idesc.getIndexedPropertyType(), idesc.getReadMethod (), idesc.getWriteMethod (),
        idesc.getIndexedReadMethod (), idesc.getIndexedWriteMethod ()
      );
      prop.setName (desc.getName ());
      prop.setDisplayName (desc.getDisplayName ());
      prop.setShortDescription (desc.getShortDescription ()); */
    } else { 
      prop = new Node.Property (desc.getPropertyType ()) {
        /** Test whether the property is readable.
        * @return <CODE>true</CODE> if it is
        */
        public boolean canRead () {
          return (desc.getReadMethod () != null);
        }

        /** Get the value.
        * @return the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        public Object getValue () throws
        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
          Method readMethod = desc.getReadMethod ();
          if (readMethod == null)
            throw new IllegalAccessException ();
          return readMethod.invoke (beanInstance, new Object[0]);
        }

        /** Test whether the property is writable.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        public boolean canWrite () {
          return (desc.getWriteMethod () != null);
        }

        /** Set the value.
        * @param val the new value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        public void setValue (Object val) throws IllegalAccessException,
        IllegalArgumentException, InvocationTargetException {
          Object old = null;
          if (canRead ()) {
            try {
              old = getValue ();
            } catch (IllegalArgumentException e) {  // no problem -> keep null
            } catch (IllegalAccessException e) {    // no problem -> keep null
            } catch (InvocationTargetException e) { // no problem -> keep null
            }
          }
          Method writeMethod = desc.getWriteMethod ();
          if (writeMethod == null)
            throw new IllegalAccessException ();
          writeMethod.invoke (beanInstance, new Object[] { val });
          Object defValue = defaultPropertyValues.get (desc.getName ());
          if ((defValue != null) && (val != null) && (defValue.equals (val))) {
            // resetting to default value
            changedPropertyValues.remove (desc);
          } else {
            // add the property to the list of changed properties
            changedPropertyValues.put (desc, val);
          }
          debugChangedValues ();
          getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), old, val);
        }

        /** Test whether the property had a default value.
        * @return <code>true</code> if it does
        */
        public boolean supportsDefaultValue () {
          return true;
        }

        /** Restore this property to its default value, if supported.
        */
        public void restoreDefaultValue () {
          // 1. remove the property from list of changed values, so that the code for it is not generated
          changedPropertyValues.remove (desc);
          
          // 2. restore the default property value
          Object def = defaultPropertyValues.get (desc.getName ());
          if (def != null) {
            try {
              setValue (def);
            } catch (IllegalAccessException e) {
              // what to do, ignore...
            } catch (IllegalArgumentException e) {
              // what to do, ignore...
            } catch (InvocationTargetException e) {
              // what to do, ignore...
            }
          }
          // [PENDING - test]
        }
      };
      

      prop.setName (desc.getName ());
      prop.setDisplayName (desc.getDisplayName ());
      prop.setShortDescription (desc.getShortDescription ());
//      prop.setPropertyEditorClass (desc.getPropertyEditorClass ());
    }
    return prop;
  }

  public void debugChangedValues () {
    if (System.getProperty ("netbeans.debug.form") != null) {
      System.out.println("-- debug.form: Changed property values in: "+this+" -------------------------");
      for (java.util.Iterator it = changedPropertyValues.keySet ().iterator (); it.hasNext ();) {
        PropertyDescriptor next = (PropertyDescriptor)it.next ();
        System.out.println("Changed Property: "+next.getName ()+", value: "+changedPropertyValues.get (next));
      }
      System.out.println("--------------------------------------------------------------------------------------");
    }
  }
}

/*
 * Log
 *  5    Gandalf   1.4         5/5/99   Ian Formanek    
 *  4    Gandalf   1.3         5/4/99   Ian Formanek    Package change
 *  3    Gandalf   1.2         4/29/99  Ian Formanek    
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
