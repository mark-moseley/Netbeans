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

package org.netbeans.modules.form;

import java.util.*;
import java.beans.*;
import javax.accessibility.*;

import org.openide.nodes.*;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 *
 * @author Ian Formanek
 */

public class RADVisualComponent extends RADComponent {

    // -----------------------------------------------------------------------------
    // Private properties

    // [??]
    private HashMap constraints = new HashMap();
//    transient private RADVisualContainer parent;

    private Node.Property[] constraintsProperties;
    private ConstraintsListener constraintsListener;

    private MetaAccessibleContext accessibilityData;
    private FormProperty[] accessibilityProperties;

    // -----------------------------------------------------------------------------
    // Initialization

    public void setParentComponent(RADComponent parentComp) {
        super.setParentComponent(parentComp);
        if (parentComp != null)
            getConstraintsProperties();
    }

//    void initParent(RADVisualContainer parent) {
//        this.parent = parent;
//    }

/*    protected void setBeanInstance(Object beanInstance) {
        if (beanInstance instanceof java.awt.Component) {
            boolean attached = FakePeerSupport.attachFakePeer(
                                            (java.awt.Component)beanInstance);
            if (attached && beanInstance instanceof java.awt.Container)
                FakePeerSupport.attachFakePeerRecursively(
                                            (java.awt.Container)beanInstance);
        }

        super.setBeanInstance(beanInstance);
    } */

    // -----------------------------------------------------------------------------
    // Public interface

    /** @return The JavaBean visual component represented by this RADVisualComponent */
    public java.awt.Component getComponent() { // [is it needed ???]
        return (java.awt.Component) getBeanInstance();
    }

    public final RADVisualContainer getParentContainer() {
        return (RADVisualContainer) getParentComponent();
    }

    /** @return The index of this component within visual components of its parent */
    public final int getComponentIndex() {
        RADVisualContainer parent = (RADVisualContainer) getParentComponent();
        return parent != null ? parent.getIndexOf(this) : -1;
//        return ((ComponentContainer)getParentComponent()).getIndexOf(this);
    }

    final LayoutSupportManager getParentLayoutSupport() {
        RADVisualContainer parent = (RADVisualContainer) getParentComponent();
        return parent != null ? parent.getLayoutSupport() : null;
    }

    // -----------------------------------------------------------------------------
    // Layout constraints management

    /** Sets component's constraints description for given layout-support class. 
     */
    public void setLayoutConstraints(Class layoutDelegateClass,
                                     LayoutConstraints constr)
    {
        if (constr != null)
            constraints.put(layoutDelegateClass.getName(), constr);
    }

    /** Gets component's constraints description for given layout-support class.
     */
    public LayoutConstraints getLayoutConstraints(Class layoutDelegateClass) {
        return (LayoutConstraints)
               constraints.get(layoutDelegateClass.getName());
    }

    HashMap getConstraintsMap() {
        return constraints;
    }

    void setConstraintsMap(Map map) {
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            Object layoutClassName = it.next();
            constraints.put(layoutClassName, map.get(layoutClassName));
        }
    }

    // ---------------
    // Properties

    protected void createPropertySets(List propSets) {
        super.createPropertySets(propSets);

        if (constraintsProperties == null)
            createConstraintsProperties();

        if (constraintsProperties.length > 0)
            propSets.add(propSets.size() - 1,
                         new Node.PropertySet("layout", // NOI18N
                    FormUtils.getBundleString("CTL_LayoutTab"), // NOI18N
                    FormUtils.getBundleString("CTL_LayoutTabHint")) // NOI18N
            {
                public Node.Property[] getProperties() {
                    return getConstraintsProperties();
                }
            });

        if (accessibilityProperties == null)
            createAccessibilityProperties();

        if (accessibilityProperties.length > 0)
            propSets.add(new Node.PropertySet(
                "accessibility", // NOI18N
                FormUtils.getBundleString("CTL_AccessibilityTab"), // NOI18N
                FormUtils.getBundleString("CTL_AccessibilityTabHint")) // NOI18N
            {
                public Node.Property[] getProperties() {
                    return getAccessibilityProperties();
                }
            });
    }

    public Node.Property getPropertyByName(String name,
                                           Class propertyType,
                                           boolean fromAll)
    {
        if (fromAll && accessibilityProperties == null)
            createAccessibilityProperties();
        return super.getPropertyByName(name, propertyType, fromAll);
    }

    /** Called to modify original properties obtained from BeanInfo.
     * Properties may be added, removed etc. - due to specific needs
     * of subclasses. Here used for adding ButtonGroupProperty.
     */
    protected void changePropertiesExplicitly(List prefProps,
                                              List normalProps,
                                              List expertProps) {

        super.changePropertiesExplicitly(prefProps, normalProps, expertProps);

        if (getBeanInstance() instanceof java.awt.TextComponent) {
            // hack for AWT text components - "text" property should be first
            for (int i=0, n=normalProps.size(); i < n; i++) {
                RADProperty prop = (RADProperty) normalProps.get(i);
                if ("text".equals(prop.getName())) { // NOI18N
                    normalProps.remove(i);
                    normalProps.add(0, prop);
                    break;
                }
            }
        }

        // hack for buttons - add a fake property for ButtonGroup
//        if (getBeanInstance() instanceof javax.swing.AbstractButton)
//            try {
//                Node.Property prop = new ButtonGroupProperty(this);
//                nameToProperty.put(prop.getName(), prop);
//                if (getBeanInstance() instanceof javax.swing.JToggleButton)
//                    prefProps.add(prop);
//                else
//                    normalProps.add(prop);
//            }
//            catch (IntrospectionException ex) {} // should not happen

//        if (getBeanInstance() instanceof javax.swing.JLabel)
//            try {
//                PropertyDescriptor pd = new PropertyDescriptor("displayedMnemonic",
//                    javax.swing.JLabel.class, "getDisplayedMnemonic", "setDisplayedMnemonic");
//                normalProps.add(createProperty(pd));
//            }
//            catch (IntrospectionException ex) {} // should not happen
    }

    protected void clearProperties() {
        super.clearProperties();
        constraintsProperties = null;
        accessibilityData = null;
        accessibilityProperties = null;
    }

    // ---------
    // constraints properties

    public Node.Property[] getConstraintsProperties() {
        if (constraintsProperties == null)
            createConstraintsProperties();
        return constraintsProperties;
    }

    public void resetConstraintsProperties() {
        if (constraintsProperties != null) {
            for (int i=0; i < constraintsProperties.length; i++)
                nameToProperty.remove(constraintsProperties[i].getName());

            constraintsProperties = null;
            propertySets = null;

            RADComponentNode node = getNodeReference();
            if (node != null)
                node.fireComponentPropertySetsChange();
        }
    }

    private void createConstraintsProperties() {
        constraintsProperties = null;

        LayoutSupportManager layoutSupport = getParentLayoutSupport();
        if (layoutSupport != null) {
            LayoutConstraints constr = layoutSupport.getConstraints(this);
            if (constr != null)
                constraintsProperties = constr.getProperties();
        }

        if (constraintsProperties == null) {
            constraintsProperties = NO_PROPERTIES;
            return;
        }

        for (int i=0; i < constraintsProperties.length; i++) {
            if (constraintsProperties[i] instanceof FormProperty) {
                FormProperty prop = (FormProperty)constraintsProperties[i];

                // we suppose the constraint property is not a RADProperty...
                prop.addVetoableChangeListener(getConstraintsListener());
                prop.addPropertyChangeListener(getConstraintsListener());

                prop.setPropertyContext(
                    new RADProperty.RADPropertyContext(this));

                if (isReadOnly()) {
                    int type = prop.getAccessType() | FormProperty.NO_WRITE;
                    prop.setAccessType(type);
                }

                nameToProperty.put(prop.getName(), prop);
            }
        }
    }

    private ConstraintsListener getConstraintsListener() {
        if (constraintsListener == null)
            constraintsListener = new ConstraintsListener();
        return constraintsListener;
    }

    private class ConstraintsListener implements VetoableChangeListener,
                                                 PropertyChangeListener
    {
        public void vetoableChange(PropertyChangeEvent ev)
            throws PropertyVetoException
        {
            Object source = ev.getSource();
            String eventName = ev.getPropertyName();
            if (source instanceof FormProperty
                && (FormProperty.PROP_VALUE.equals(eventName)
                    || FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName)))
            {
                LayoutSupportManager layoutSupport = getParentLayoutSupport();
                int index = getComponentIndex();
                LayoutConstraints constraints =
                    layoutSupport.getConstraints(index);

                ev = new PropertyChangeEvent(constraints,
                                             ((FormProperty)source).getName(),
                                             ev.getOldValue(),
                                             ev.getNewValue());

                layoutSupport.componentLayoutChanged(index, ev);
            }
        }

        public void propertyChange(PropertyChangeEvent ev) {
            Object source = ev.getSource();
            if (source instanceof FormProperty
                && FormProperty.CURRENT_EDITOR.equals(ev.getPropertyName()))
            {
                LayoutSupportManager layoutSupport = getParentLayoutSupport();
                int index = getComponentIndex();
                LayoutConstraints constraints =
                    layoutSupport.getConstraints(index);

                ev = new PropertyChangeEvent(constraints, null, null, null);

                try {
                    layoutSupport.componentLayoutChanged(index, ev);
                }
                catch (PropertyVetoException ex) {} // should not happen
            }
        }
    }

    // ----------
    // accessibility properties

    public FormProperty[] getAccessibilityProperties() {
        if (accessibilityProperties == null)
            createAccessibilityProperties();
        return accessibilityProperties;
    }

    private void createAccessibilityProperties() {
        Object comp = getBeanInstance();
        if (comp instanceof Accessible
            && ((Accessible)comp).getAccessibleContext() != null)
        {
            if (accessibilityData == null)
                accessibilityData = new MetaAccessibleContext();
            accessibilityProperties = accessibilityData.getProperties();

            for (int i=0; i < accessibilityProperties.length; i++) {
                FormProperty prop = accessibilityProperties[i];
                setPropertyListener(prop);
                prop.setPropertyContext(
                    new RADProperty.RADPropertyContext(this));
                nameToProperty.put(prop.getName(), prop);
            }
        }
        else {
            accessibilityData = null;
            accessibilityProperties = NO_PROPERTIES;
        }
    }

    private class MetaAccessibleContext {
        private Object accName = BeanSupport.NO_VALUE;
        private Object accDescription = BeanSupport.NO_VALUE;
        private Object accParent = BeanSupport.NO_VALUE;

        private FormProperty[] properties;

        FormProperty[] getProperties() {
            if (properties == null) {
                properties = new FormProperty[] {
                    new FormProperty(
                        "AccessibleContext.accessibleName", // NOI18N
                        String.class,
                        FormUtils.getBundleString("PROP_AccessibleName"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleName")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return accName != BeanSupport.NO_VALUE ?
                                       accName : getDefaultValue();
                        }
                        public void setTargetValue(Object value) {
                            accName = (String) value;
                        }
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        public Object getDefaultValue() {
                            return getAccessibleContext().getAccessibleName();
                        }
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accName = BeanSupport.NO_VALUE;
                        }
                        String getPartialSetterCode() {
                            return "getAccessibleContext().setAccessibleName(" // NOI18N
                                   + getJavaInitializationString() + ")"; // NOI18N
                        }
                    },

                    new FormProperty(
                        "AccessibleContext.accessibleDescription", // NOI18N
                        String.class,
                        FormUtils.getBundleString("PROP_AccessibleDescription"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleDescription")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return accDescription != BeanSupport.NO_VALUE ?
                                       accDescription : getDefaultValue();
                        }
                        public void setTargetValue(Object value) {
                            accDescription = (String) value;
                        }
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        public Object getDefaultValue() {
                            return getAccessibleContext().getAccessibleDescription();
                        }
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accDescription = BeanSupport.NO_VALUE;
                        }
                        String getPartialSetterCode() {
                            return
                              "getAccessibleContext().setAccessibleDescription(" // NOI18N
                              + getJavaInitializationString() + ")"; // NOI18N
                        }
                    },

                    new FormProperty(
                        "AccessibleContext.accessibleParent", // NOI18N
                        Accessible.class,
                        FormUtils.getBundleString("PROP_AccessibleParent"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleParent")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return accParent != BeanSupport.NO_VALUE ?
                                       accParent : getDefaultValue();
                        }
                        public void setTargetValue(Object value) {
                            accParent = (Accessible) value;
                        }
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        public Object getDefaultValue() {
                            Object acP = getAccessibleContext()
                                             .getAccessibleParent();
                            if (acP != null) {
                                RADVisualContainer metacont = getParentContainer();
                                if (metacont != null) {
                                    Object cont = metacont.getContainerDelegate(
                                                    metacont.getBeanInstance());
                                    if (cont == acP)
                                        return metacont;
                                }
                            }
                            return acP;
                        }
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accParent = BeanSupport.NO_VALUE;
                        }
                        public PropertyEditor getExpliciteEditor() {
                            return new AccessibleParentEditor();
                        }
                        String getPartialSetterCode() {
                            String str = getJavaInitializationString();
                            return str == null ? null :
                                "getAccessibleContext().setAccessibleParent(" // NOI18N
                                + str + ")"; // NOI18N
                        }
                    }
                };
            }
            return properties;
        }

        private AccessibleContext getAccessibleContext() {
            return ((Accessible)getBeanInstance()).getAccessibleContext();
        }
    }

    public static class AccessibleParentEditor extends ComponentChooserEditor {
        public AccessibleParentEditor() {
            super();
            setBeanTypes(new Class[] { Accessible.class });
        }
    }
}
