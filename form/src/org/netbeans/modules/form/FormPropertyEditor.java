/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.lang.ref.WeakReference;
import java.security.*;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.nodes.*;

/** A multiplexing PropertyEditor used in the form editor.
 * It allows multiple editors to be used with one currently selected.
 *
 * @author Ian Formanek
 */

public class FormPropertyEditor implements PropertyEditor,
                                           PropertyChangeListener,
                                           EnhancedPropertyEditor,
                                           ExPropertyEditor
{
    private Object value = BeanSupport.NO_VALUE;
    private FormProperty property;
    private WeakReference propertyEnv;

    private PropertyEditor[] allEditors;
    private ArrayList listeners;


    /** Crates a new FormPropertyEditor */
    FormPropertyEditor(FormProperty property) {
        this.property = property;
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null) {
            prEd.addPropertyChangeListener(this);
            value = prEd.getValue();
        }
    }

    Class getPropertyType() {
        return property.getValueType();
    }

    FormProperty getProperty() {
        return property;
    }

    FormPropertyContext getPropertyContext() {
        return property.getPropertyContext();
    }

    PropertyEnv getPropertyEnv() {
        return propertyEnv != null ? (PropertyEnv) propertyEnv.get() : null;
    }

    PropertyEditor getCurrentEditor() {
        return property.getCurrentEditor();
    }

    void setCurrentEditor(PropertyEditor newEditor) {
        property.setCurrentEditor(newEditor);
    }

    // -----------------------------------------------------------------------------
    // PropertyChangeListener implementation

    public void propertyChange(PropertyChangeEvent evt) {
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null)
            value = prEd.getValue();

        // we run this as privileged to avoid security problems - because
        // the property change can be fired from untrusted property editor code
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                FormPropertyEditor.this.firePropertyChange();
                return null;
            }
        });
    }

    // -----------------------------------------------------------------------------
    // PropertyEditor implementation

    /**
     * Set(or change) the object that is to be edited.
     * @param value The new target object to be edited.  Note that this
     *     object should not be modified by the PropertyEditor, rather 
     *     the PropertyEditor should create a new object to hold any
     *     modified value.
     */
    public void setValue(Object newValue) {
        value = newValue;

        PropertyEditor prEd = property.getCurrentEditor();
        if (value != BeanSupport.NO_VALUE && prEd != null)
            prEd.setValue(value);
    }

    /**
     * Gets the value of the property.
     *
     * @return The value of the property.
     */
    public Object getValue() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getValue() : value;
    }

    // -----------------------------------------------------------------------------

    /**
     * Determines whether the class will honor the painValue method.
     *
     * @return  True if the class will honor the paintValue method.
     */
    public boolean isPaintable() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.isPaintable() : false;
    }

    /**
     * Paint a representation of the value into a given area of screen
     * real estate.  Note that the propertyEditor is responsible for doing
     * its own clipping so that it fits into the given rectangle.
     * <p>
     * If the PropertyEditor doesn't honor paint requests(see isPaintable)
     * this method should be a silent noop.
     *
     * @param gfx  Graphics object to paint into.
     * @param box  Rectangle within graphics object into which we should paint.
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null)
            prEd.paintValue(gfx, box);
    }

    // -----------------------------------------------------------------------------

    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *   	current value.
     */
    public String getJavaInitializationString() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getJavaInitializationString() : null;
    }

    // -----------------------------------------------------------------------------

    /**
     * Gets the property value as a string suitable for presentation
     * to a human to edit.
     *
     * @return The property value as a string suitable for presentation
     *       to a human to edit.
     * <p>   Returns "null" is the value can't be expressed as a string.
     * <p>   If a non-null value is returned, then the PropertyEditor should
     *	     be prepared to parse that string back in setAsText().
     */
    public String getAsText() {
        if (value == BeanSupport.NO_VALUE)
            return FormEditor.getFormBundle().getString("CTL_ValueNotSet"); // NOI18N

        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getAsText() : null;
    }

    /**
     * Sets the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     *
     * @param text  The string to be parsed.
     */
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null)
            prEd.setAsText(text);
    }

    // -----------------------------------------------------------------------------

    /**
     * If the property value must be one of a set of known tagged values, 
     * then this method should return an array of the tag values.  This can
     * be used to represent(for example) enum values.  If a PropertyEditor
     * supports tags, then it should support the use of setAsText with
     * a tag value as a way of setting the value.
     *
     * @return The tag values for this property.  May be null if this 
     *   property cannot be represented as a tagged value.
     *	
     */
    public String[] getTags() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getTags() : null;
    }

    // -----------------------------------------------------------------------------

    /**
     * A PropertyEditor may chose to make available a full custom Component
     * that edits its property value.  It is the responsibility of the
     * PropertyEditor to hook itself up to its editor Component itself and
     * to report property value changes by firing a PropertyChange event.
     * <P>
     * The higher-level code that calls getCustomEditor may either embed
     * the Component in some larger property sheet, or it may put it in
     * its own individual dialog, or ...
     *
     * @return A java.awt.Component that will allow a human to directly
     *      edit the current property value.  May be null if this is
     *	    not supported.
     */

    public Component getCustomEditor() {
        Component customEditor;

        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null && prEd.supportsCustomEditor()) {
            customEditor = prEd.getCustomEditor();
            if (customEditor instanceof Window)
                return customEditor;
        }
        else customEditor = null;

        return new FormCustomEditor(this, customEditor);
    }

    /**
     * Determines whether the propertyEditor can provide a custom editor.
     *
     * @return  True if the propertyEditor can provide a custom editor.
     */
    public boolean supportsCustomEditor() {
        PropertyEditor[] editors = getAllEditors();
        if (editors.length > 1)
            return true; // we must  at least allow to choose the editor
        if (editors.length == 1)
            return editors[0].supportsCustomEditor();
        return false;
    }

    synchronized PropertyEditor[] getAllEditors() {
        if (allEditors == null) {
            PropertyEditor expliciteEditor = property.getExpliciteEditor();
            PropertyEditor[] typeEditors = FormPropertyEditorManager
                                        .getAllEditors(property.getValueType());
            if (expliciteEditor != null) {
                // expliciteEditor could be already in typeEditors
                for (int i=0; i < typeEditors.length; i++)
                    if (expliciteEditor.getClass().equals(typeEditors[i].getClass())) {
                        typeEditors[i] = expliciteEditor;
                        expliciteEditor = null;
                        break;
                    }
            }
            if (expliciteEditor != null) {
                allEditors = new PropertyEditor[typeEditors.length+1];
                allEditors[0] = expliciteEditor;
                System.arraycopy(typeEditors, 0, allEditors, 1, typeEditors.length);
            }
            else allEditors = typeEditors;
        }
        return allEditors;
    }

    // -----------------------------------------------------------------------------
    // EnhancedPropertyEditor implementation

    /** Get an in-place editor.
     * @return a custom property editor to be shown inside the property
     *         sheet
     */
    public Component getInPlaceCustomEditor() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd instanceof EnhancedPropertyEditor ?
               ((EnhancedPropertyEditor)prEd).getInPlaceCustomEditor() : null;
    }

    /** Test for support of in-place custom editors.
     * @return <code>true</code> if supported
     */
    public boolean hasInPlaceCustomEditor() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd instanceof EnhancedPropertyEditor ?
               ((EnhancedPropertyEditor)prEd).hasInPlaceCustomEditor() : false;
    }

    /** Test for support of editing of tagged values.
     * Must also accept custom strings, otherwise you may may specify a standard property editor accepting only tagged values.
     * @return <code>true</code> if supported
     */
    public boolean supportsEditingTaggedValues() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd instanceof EnhancedPropertyEditor ?
               ((EnhancedPropertyEditor)prEd).supportsEditingTaggedValues() : false;
    }

    // -------------------------------------------------------------
    // FormPropertyContainer implementation
    
//    public Node.Property[] getProperties() {
//        if (modifiedEditor instanceof FormPropertyContainer)
//            return ((FormPropertyContainer)modifiedEditor).getProperties();
//        else
//            return null;
//    }

    // -----------------------------------------------------------------------------

    /**
     * Register a listener for the PropertyChange event.  The class will
     * fire a PropertyChange value whenever the value is updated.
     *
     * @param listener  An object to be invoked when a PropertyChange
     *		event is fired.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (listeners == null)
            listeners = new ArrayList();

        listeners.add(l);
    }

    /**
     * Remove a listener for the PropertyChange event.
     *
     * @param listener  The PropertyChange listener to be removed.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    /**
     * Report that we have been modified to any interested listeners.
     *
     * @param source  The PropertyEditor that caused the event.
     */
    void firePropertyChange() {
        java.util.List targets;
        synchronized(this) {
            if (listeners == null)
                return;
            targets = (java.util.ArrayList) listeners;
        }

        PropertyChangeEvent evt = new PropertyChangeEvent(this, null, null, null);

        for (int i = 0; i < targets.size(); i++) {
            PropertyChangeListener target = (PropertyChangeListener)
                                            targets.get(i);
            target.propertyChange(evt);
        }
    }

    // -------------
    // ExPropertyEditor implementation

    /** 
     * This method is called by the IDE to pass
     * the environment to the property editor.
     */
    public void attachEnv(PropertyEnv env) {
        propertyEnv = new WeakReference(env);
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd instanceof ExPropertyEditor)
            ((ExPropertyEditor)prEd).attachEnv(env);
    }

    // ---------
    // delegating hashCode() and equals(Object) methods to modifiedEditor - for
    // PropertyPanel mapping property editors to PropertyEnv

    public int hashCode() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.hashCode() : super.hashCode();
    }

    public boolean equals(Object obj) {
        return obj != null ? hashCode() == obj.hashCode() : false;
    }
}
