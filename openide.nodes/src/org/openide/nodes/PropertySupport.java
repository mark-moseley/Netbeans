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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.nodes;

import java.beans.Beans;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** Support class for <code>Node.Property</code>.
*
* @author Jan Jancura, Jaroslav Tulach, Ian Formanek
*/
public abstract class PropertySupport<T> extends Node.Property<T> {
    /** flag whether the property is readable */
    private boolean canR;

    /** flag whether the property is writable */
    private boolean canW;

    /** Constructs a new support.
    * @param name        the name of the property
    * @param type        the class type of the property
    * @param displayName the display name of the property
    * @param canR        whether the property is readable
    * @param canW        whether the property is writable
    */
    public PropertySupport(
        String name, Class<T> type, String displayName, String shortDescription, boolean canR, boolean canW
    ) {
        super(type);
        this.setName(name);
        setDisplayName(displayName);
        setShortDescription(shortDescription);
        this.canR = canR;
        this.canW = canW;
    }

    /* Can read the value of the property.
    * Returns the value passed into constructor.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canRead() {
        return canR;
    }

    /* Can write the value of the property.
    * Returns the value passed into constructor.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canWrite() {
        return canW;
    }

    /**
     * Like {@link Class#cast} but handles primitive types.
     * See JDK #6456930.
     */
    static <T> T cast(Class<T> c, Object o) {
        if (c.isPrimitive()) {
            // Could try to actually type-check it, but never mind.
            return (T) o;
        } else {
            return c.cast(o);
        }
    }

    /** Support for properties from Java Reflection. */
    public static class Reflection<T> extends Node.Property<T> {
        /** Instance of a bean. */
        protected Object instance;

        /** setter method */
        private Method setter;

        /** getter method */
        private Method getter;

        /** class of property editor */
        private Class<? extends PropertyEditor> propertyEditorClass;

        /** Create a support with method objects specified.
        * The methods must be public.
        * @param instance (Bean) object to work on
        * @param valueType type of the property
        * @param getter getter method, can be <code>null</code>
        * @param setter setter method, can be <code>null</code>
        * @throws IllegalArgumentException if the methods are not public
        */
        public Reflection(Object instance, Class<T> valueType, Method getter, Method setter) {
            super(valueType);

            if ((getter != null) && !Modifier.isPublic(getter.getModifiers())) {
                throw new IllegalArgumentException("Cannot use a non-public getter " + getter); // NOI18N
            }

            if ((setter != null) && !Modifier.isPublic(setter.getModifiers())) {
                throw new IllegalArgumentException("Cannot use a non-public setter " + setter); // NOI18N
            }

            this.instance = instance;
            this.setter = setter;
            this.getter = getter;
        }

        /** Create a support with methods specified by name.
        * The instance class will be examined for the named methods.
        * But if the instance class is not public, the nearest public superclass
        * will be used instead, so that the getters and setters remain accessible.
        * @param instance (Bean) object to work on
        * @param valueType type of the property
        * @param getter name of getter method, can be <code>null</code>
        * @param setter name of setter method, can be <code>null</code>
        * @exception NoSuchMethodException if the getter or setter methods cannot be found
        */
        public Reflection(Object instance, Class<T> valueType, String getter, String setter)
        throws NoSuchMethodException {
            this(
                instance, valueType,
                (
            // find the getter ()
            getter == null) ? null : findAccessibleClass(instance.getClass()).getMethod(getter),
                (
            // find the setter (valueType)
            setter == null) ? null : findAccessibleClass(instance.getClass()).getMethod(
                    setter, new Class<?>[] { valueType }
                )
            );
        }

        // [PENDING] should use Beans API in case there is overriding BeanInfo  --jglick

        /** Create a support based on the property name.
        * The getter and setter methods are constructed by capitalizing the first
        * letter in the name of propety and prefixing it with <code>get</code> and
        * <code>set</code>, respectively.
        *
        * @param instance object to work on
        * @param valueType type of the property
        * @param property name of property
        * @exception NoSuchMethodException if the getter or setter methods cannot be found
        */
        public Reflection(Object instance, Class<T> valueType, String property)
        throws NoSuchMethodException {
            this(
                instance, valueType, findGetter(instance, valueType, property),
                findAccessibleClass(instance.getClass()).getMethod(
                    firstLetterToUpperCase(property, "set"), valueType
                )
            );
        }

        /** Find the nearest superclass (or same class) that is public to this one. */
        private static <C> Class<? super C> findAccessibleClass(Class<C> clazz) {
            if (Modifier.isPublic(clazz.getModifiers())) {
                return clazz;
            } else {
                Class<? super C> sup = clazz.getSuperclass();

                if (sup == null) {
                    return Object.class; // handle interfaces
                }

                return findAccessibleClass(sup);
            }
        }

        /** Helper method to convert the first letter of a string to uppercase.
        * And prefix the string with some next string.
        */
        private static String firstLetterToUpperCase(String s, String pref) {
            switch (s.length()) {
            case 0:
                return pref;

            case 1:
                return pref + Character.toUpperCase(s.charAt(0));

            default:
                return pref + Character.toUpperCase(s.charAt(0)) + s.substring(1);
            }
        }

        // Finds the proper getter
        private static Method findGetter(Object instance, Class valueType, String property)
        throws NoSuchMethodException {
            NoSuchMethodException nsme;

            try {
                return findAccessibleClass(instance.getClass()).getMethod(
                    firstLetterToUpperCase(property, "get")
                );
            } catch (NoSuchMethodException e) {
                if (valueType != boolean.class) {
                    throw e;
                } else {
                    nsme = e;
                }
            }

            // Is of type boolean and "get" getter does not exist
            try {
                return findAccessibleClass(instance.getClass()).getMethod(
                    firstLetterToUpperCase(property, "is")
                );
            } catch (NoSuchMethodException e) {
                throw e;
            }
        }

        /* Can read the value of the property.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        public boolean canRead() {
            return getter != null;
        }

        /* Getter for the value.
        * @return the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        public T getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (getter == null) {
                throw new IllegalAccessException();
            }

            Object valideInstance = Beans.getInstanceOf(instance, getter.getDeclaringClass());

            try {
                try {
                    return cast(getValueType(), getter.invoke(valideInstance));
                } catch (IllegalAccessException ex) {
                    try {
                        getter.setAccessible(true);

                        return cast(getValueType(), getter.invoke(valideInstance));
                    } finally {
                        getter.setAccessible(false);
                    }
                }
            } catch (IllegalArgumentException iae) {
                //Provide a better message for debugging
                StringBuffer sb = new StringBuffer("Attempted to invoke method ");
                sb.append(getter.getName());
                sb.append(" from class ");
                sb.append(getter.getDeclaringClass().getName());
                sb.append(" on an instance of ");
                sb.append(valideInstance.getClass().getName());
                sb.append(" Problem:");
                sb.append(iae.getMessage());
                throw (IllegalArgumentException) new IllegalArgumentException(sb.toString()).initCause(iae);
            }
        }

        /* Can write the value of the property.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        public boolean canWrite() {
            return setter != null;
        }

        /* Setter for the value.
        * @param val the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        public void setValue(T val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (setter == null) {
                throw new IllegalAccessException();
            }

            Object valideInstance = Beans.getInstanceOf(instance, setter.getDeclaringClass());

            try {
                setter.invoke(valideInstance, val);
            } catch (IllegalAccessException ex) {
                try {
                    setter.setAccessible(true);
                    setter.invoke(valideInstance, val);
                } finally {
                    setter.setAccessible(false);
                }
            }
        }

        /* Returns property editor for this property.
        * @return the property editor or <CODE>null</CODE> if there should not be
        *    any editor.
        */
        public PropertyEditor getPropertyEditor() {
            if (propertyEditorClass != null) {
                try {
                    return propertyEditorClass.newInstance();
                } catch (InstantiationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalAccessException iex) {
                    Exceptions.printStackTrace(iex);
                }
            }

            return super.getPropertyEditor();
        }

        /** Set the property editor explicitly.
        * @param clazz class type of the property editor
        */
        public void setPropertyEditorClass(Class<? extends PropertyEditor> clazz) {
            propertyEditorClass = clazz;
        }
    }

    /** A simple read/write property.
    * Subclasses should implement
    * {@link #getValue} and {@link #setValue}.
    */
    public static abstract class ReadWrite<T> extends PropertySupport<T> {
        /** Construct a new support.
        * @param name        the name of the property
        * @param type        the class type of the property
        * @param displayName the display name of the property
        * @param shortDescription a short description of the property
        */
        public ReadWrite(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription, true, true);
        }
    }

    /** A simple read-only property.
    * Subclasses should implement {@link #getValue}.
    */
    public static abstract class ReadOnly<T> extends PropertySupport<T> {
        /** Construct a new support.
        * @param name        the name of the property
        * @param type        the class type of the property
        * @param displayName the display name of the property
        * @param shortDescription a short description of the property
        */
        public ReadOnly(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription, true, false);
        }

        /* Setter for the value.
        * @param val the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        @Override
        public void setValue(T val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            throw new IllegalAccessException("Cannot write to ReadOnly property"); // NOI18N
        }
    }

    /** A simple write-only property.
    * Subclasses should implement {@link #setValue}.
    */
    public static abstract class WriteOnly<T> extends PropertySupport<T> {
        /** Construct a new support.
        * @param name        the name of the property
        * @param type        the class type of the property
        * @param displayName the display name of the property
        * @param shortDescription a short description of the property
        */
        public WriteOnly(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription, false, true);
        }

        /* Getter for the value.
        * @return the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        @Override
        public T getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            throw new IllegalAccessException("Cannod read from WriteOnly property"); // NOI18N
        }
    }

    /** Support for the name property of a node. Delegates {@link #setValue} and {@link #getValue}
    * to {@link Node#setName} and {@link Node#getName}.
    */
    public static final class Name extends PropertySupport<String> {
        /** The node to which we delegate the work. */
        private final Node node;

        /** Create the name property for a node with the standard name and hint.
        * @param node the node
        */
        public Name(final Node node) {
            this(
                node, NbBundle.getBundle(PropertySupport.class).getString("CTL_StandardName"),
                NbBundle.getBundle(PropertySupport.class).getString("CTL_StandardHint")
            );
        }

        /** Create the name property for a node.
        * @param node the node
        * @param propName name of the "name" property
        * @param hint hint message for the "name" property
        */
        public Name(final Node node, final String propName, final String hint) {
            super(Node.PROP_NAME, String.class, propName, hint, true, node.canRename());
            this.node = node;
        }

        /* Getter for the value. Delegates to Node.getName().
        * @return the name
        */
        public String getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return node.getName();
        }

        /* Setter for the value. Delegates to Node.setName().
        * @param val new name
        */
        public void setValue(String val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Object oldName = node.getName();
            node.setName(val);
            node.firePropertyChange(Node.PROP_NAME, oldName, val);
        }
    }
     // end of Name inner class
}
