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

import java.beans.*;
import java.util.*;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;

/**
 * Property editor allowing to choose a component from all components in form
 * (FormModel). Choice can be restricted to certain bean types.
 *
 * @author Tomas Pavek
 */

public class ComponentChooserEditor implements PropertyEditor,
                                               FormAwareEditor,
                                               XMLPropertyEditor
{
    public static final int ALL_COMPONENTS = 0;
    public static final int VISUAL_COMPONENTS = 1;
    public static final int OTHER_COMPONENTS = 2;

    private static final String NULL_REF = "null"; // NOI18N
    private static final String INVALID_REF = "default"; // NOI18N

    private static String noneText = null;
    private static String invalidText = null;

    private FormModel formModel;
    private List components;
    private Class[] beanTypes = null;
    private int componentCategory = 0;

    private ComponentRef value;

    private Vector listeners;

    public ComponentChooserEditor() {
    }

    public ComponentChooserEditor(Class[] componentTypes) {
        beanTypes = componentTypes;
    }

    // --------------
    // PropertyEditor implementation

    public void setValue(Object value) {
        if (value == null || value instanceof ComponentRef)
            this.value = (ComponentRef) value;
        else if (value instanceof RADComponent)
            this.value = new ComponentRef((RADComponent)value);
        else if (value instanceof String)
            this.value = new ComponentRef((String)value);
        else
            return;

        firePropertyChange();
    }

    public Object getValue() {
        if (value != null && INVALID_REF.equals(value.getDescription()))
            return BeanSupport.NO_VALUE; // special - invalid value was loaded

        return value;
    }

    public String[] getTags() {
        List compList = getComponents();

        int count = compList.size() + 1;
        String[] names = new String[count];
        names[0] = noneString();

        if (count > 1) {
            for (int i=1; i < count; i++)
                names[i] = ((RADComponent)compList.get(i-1)).getName();
            Arrays.sort(names, 1, count);
        }

        return names;
    }

    public String getAsText() {
        if (value == null)
            return noneString();
        if (value.getComponent() == null)
            return invalidString();

        String str = value.getDescription();
        return NULL_REF.equals(str) ? noneString() : str;
    }

    public void setAsText(String str) {
        if (str == null || str.equals("") || str.equals(noneString())) // NOI18N
            setValue(null);
        else
            setValue(str);
    }

    public String getJavaInitializationString() {
        return value != null ? value.getJavaInitString() : null;
    }

    public synchronized void addPropertyChangeListener(
                                 PropertyChangeListener listener)
    {
	if (listeners == null)
	    listeners = new java.util.Vector();
	listeners.addElement(listener);
    }

    public synchronized void removePropertyChangeListener(
                                 PropertyChangeListener listener)
    {
	if (listeners != null)
            listeners.removeElement(listener);
    }

    public boolean isPaintable() {
        return false;
    }

    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
    }

    public java.awt.Component getCustomEditor() {
        return null;
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    // ----------------

    // FormAwareEditor implementation
    public void setFormModel(FormModel model) {
        formModel = model;
    }

    public FormModel getFormModel() {
        return formModel;
    }

    public void setBeanTypes(Class[] types) {
        beanTypes = types;
    }

    public Class[] getBeanTypes() {
        return beanTypes;
    }

    public void setComponentCategory(int cat) {
        componentCategory = cat;
    }

    public int getComponentCategory() {
        return componentCategory;
    }

    // ----------------
    // XMLPropertyEditor implementation

    private static final String XML_COMPONENT = "ComponentRef"; // NOI18N
    private static final String ATTR_NAME = "name"; // NOI18N

    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        String nameStr;
        if (value != null)
            nameStr = value.getComponent() != null ?
                      value.getDescription() : INVALID_REF;
        else
            nameStr = NULL_REF;
        
        org.w3c.dom.Element el = doc.createElement(XML_COMPONENT);
        el.setAttribute(ATTR_NAME, nameStr);
        return el;
    }

    public void readFromXML(org.w3c.dom.Node element)
        throws java.io.IOException
    {
        if (!XML_COMPONENT.equals(element.getNodeName()))
            throw new java.io.IOException();

        org.w3c.dom.NamedNodeMap attributes;
        org.w3c.dom.Node nameAttr;
        String name;

        if ((attributes = element.getAttributes()) != null
              && (nameAttr = attributes.getNamedItem(ATTR_NAME)) != null
              && (name = nameAttr.getNodeValue()) != null)
        {
            value = new ComponentRef(name);
        }
    }

    // ---------

    protected List getComponents() {
        if (components == null)
            components = new ArrayList();
        else
            components.clear();

        if (formModel != null) {
            RADComponent[] comps;
            if (componentCategory == VISUAL_COMPONENTS)
                comps = formModel.getVisualComponents();
            else if (componentCategory == OTHER_COMPONENTS)
                comps = formModel.getOtherComponents(true);
            else {
                java.util.List allComps = formModel.getMetaComponents();
                comps = (RADComponent[])
                        allComps.toArray(new RADComponent[allComps.size()]);
            }

            for (int i=0; i < comps.length; i++)
                if (acceptBean(comps[i]))
                    components.add(comps[i]);
        }

        return components;
    }

    protected boolean acceptBean(RADComponent comp) {
        if (beanTypes == null)
            return true;

        boolean match = false;
        for (int i=0; i < beanTypes.length && !match; i++)
            match = beanTypes[i].isAssignableFrom(comp.getBeanClass());

        return match;
    }

    protected String noneString() {
        if (noneText == null)
            noneText = FormUtils.getBundleString("CTL_NoComponent"); // NOI18N
        return noneText;
    }

    protected String invalidString() {
        if (invalidText == null)
            invalidText = FormUtils.getBundleString("CTL_InvalidReference"); // NOI18N
        return invalidText;
    }

    // ------

    protected final void firePropertyChange() {
	Vector targets;
	synchronized (this) {
	    if (listeners == null)
	    	return;
	    targets = (Vector) listeners.clone();
	}

        PropertyChangeEvent evt = new PropertyChangeEvent(
                                          this, null, null, null);

	for (int i=0; i < targets.size(); i++)
            ((PropertyChangeListener)targets.elementAt(i)).propertyChange(evt);
    }

    // ------------

    private class ComponentRef implements RADComponent.ComponentReference,
                                          FormDesignValue
    {
        private String componentName;
        private RADComponent component;

        ComponentRef(String name) {
            componentName = name;
        }

        ComponentRef(RADComponent metacomp) {
            componentName = metacomp.getName();
            component = metacomp;
        }

        public boolean equals(Object obj) {
            return this == obj
                   || (obj instanceof RADComponent
                       && obj == component);
        }

        String getJavaInitString() {
            checkComponent();

            if (component != null) {
                if (component == component.getFormModel().getTopRADComponent())
                    return "this"; // NOI18N
            }
            else if (!NULL_REF.equals(componentName))
                return null; // invalid reference

            return componentName;
        }

        public RADComponent getComponent() {
            checkComponent();
            return component;
        }

        /** FormDesignValue implementation. */
        public String getDescription() {
            checkComponent();
            return componentName;
        }

        /** FormDesignValue implementation. */
        public Object getDesignValue() {
            checkComponent();
            return component != null ?
                   component.getBeanInstance(): IGNORED_VALUE;
        }

        private void checkComponent() {
            if (component == null
                && !NULL_REF.equals(componentName)
                && !INVALID_REF.equals(componentName))
            {
                List compList = getComponents();
                Iterator it = compList.iterator();
                while (it.hasNext()) {
                    RADComponent comp = (RADComponent) it.next();
                    if (comp.getName().equals(componentName)) {
                        if (comp.isInModel())
                            component = comp;
                        break;
                    }
                }
            }
            else if (component != null) {
                if (!component.isInModel())
                    component = null;
                else
                    componentName = component.getName();
            }
        }
    }
}
