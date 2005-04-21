/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * NodePropertyModel.java
 *
 * Created on April 22, 2003, 5:09 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

import java.lang.reflect.InvocationTargetException;


/** Implementation of the <code>PropertyModel</code> interface keeping
 * a <code>Node.Property</code>.  Refactored from PropertyPanel.SimpleModel
 * as part of the property sheet rewrite.  */
class NodePropertyModel implements ExPropertyModel {
    //This class was originally PropertyPanel.SimpleModel up to
    //PropertyPanel 1.123

    /** Property to work with.  */
    private Node.Property prop;

    /** Array of beans(nodes) to which belong the property.  */
    private Object[] beans;

    /** Property change support.  */
    private PropertyChangeSupport sup = new PropertyChangeSupport(this);
    String beanName = null;

    /** Construct simple model instance.
     * @param property proeprty to work with
     * @param beans array of beans(nodes) to which belong the property
     */
    public NodePropertyModel(Node.Property property, Object[] beans) {
        this.prop = property;
        this.beans = beans;
    }

    String getBeanName() {
        if (beans != null) {
            if ((beans.length == 1) && beans[0] instanceof Node.Property) {
                return ((Node.Property) beans[0]).getDisplayName();
            }
        }

        return null;
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public Object getValue() throws InvocationTargetException {
        try {
            return prop.getValue();
        } catch (IllegalAccessException iae) {
            throw annotateException(iae);
        } catch (InvocationTargetException ite) {
            throw annotateException(ite);
        } catch (ProxyNode.DifferentValuesException dve) {
            return null;
        }
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public void setValue(Object v) throws InvocationTargetException {
        try {
            prop.setValue(v);
            sup.firePropertyChange(PropertyModel.PROP_VALUE, null, null);
        } catch (IllegalAccessException iae) {
            throw annotateException(iae);
        } catch (IllegalArgumentException iaae) {
            throw annotateException(iaae);
        } catch (InvocationTargetException ite) {
            throw annotateException(ite);
        }
    }

    /** Annotates specified exception. Helper method.
     * @param exception original exception to annotate
     * @return <code>IvocationTargetException</code> which annotates the
     *       original exception
     */
    private InvocationTargetException annotateException(Exception exception) {
        if (exception instanceof InvocationTargetException) {
            return (InvocationTargetException) exception;
        } else {
            return new InvocationTargetException(exception);
        }
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public Class getPropertyType() {
        return prop.getValueType();
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public Class getPropertyEditorClass() {
        Object ed = prop.getPropertyEditor();

        if (ed != null) {
            return ed.getClass();
        }

        return null;
    }

    /** Mainly a hack to avoid gratuitous calls to fetch property editors.
     *  @since 1.123.2.1 - branch propsheet_issue_29447
     */
    public PropertyEditor getPropertyEditor() {
        return PropUtils.getPropertyEditor(prop);
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        sup.addPropertyChangeListener(l);
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        sup.removePropertyChangeListener(l);
    }

    /** Implements <code>ExPropertyModel</code> interface.  */
    public Object[] getBeans() {
        return beans;
    }

    /** Implements <code>ExPropertyModel</code> interface.  */
    public FeatureDescriptor getFeatureDescriptor() {
        return prop;
    }

    void fireValueChanged() {
        sup.firePropertyChange(PropertyModel.PROP_VALUE, null, null);
    }

    /** Package private method to return the property, so error handling
     *  can use the display name in the dialog for the user if the user
     *  enters an invalid value  */
    Node.Property getProperty() {
        return prop;
    }
}
