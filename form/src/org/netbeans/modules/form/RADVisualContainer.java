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
import javax.swing.*;
import java.util.ArrayList;
import java.lang.reflect.Method;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;


public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
    private ArrayList subComponents = new ArrayList(10);
    private LayoutSupportManager layoutSupport = new LayoutSupportManager();
    private LayoutNode layoutNode; // [move to LayoutSupportManager?]

    private RADMenuComponent containerMenu;

    public boolean initialize(FormModel formModel) {
        if (super.initialize(formModel)) {
            if (getBeanClass() != null)
                layoutSupport.initialize(this, formModel.getCodeStructure());
            return true;
        }
        return false;
    }

    protected void setBeanInstance(Object beanInstance) {
        super.setBeanInstance(beanInstance);
        layoutSupport.initialize(this, getFormModel().getCodeStructure());
    }

//    public void setInstance(Object beanInstance) {
//        super.setInstance(beanInstance);
//        initLayoutSupport(); // [??]
//    }

    public void initLayoutSupport() {
        LayoutSupportDelegate layoutDelegate =
            LayoutSupportRegistry.createLayoutDelegate(
                                      (Container) getBeanInstance(),
                                      getContainerDelegate(getBeanInstance()));

        setLayoutSupportDelegate(layoutDelegate);
    }

    public void setLayoutSupportDelegate(LayoutSupportDelegate layoutDelegate) {
        layoutSupport.setLayoutDelegate(layoutDelegate, false);
        setLayoutNodeReference(null);
    }

    public LayoutSupportManager getLayoutSupport() {
        return layoutSupport;
    }

    public boolean isLayoutSupportSet() {
        return layoutSupport.getLayoutDelegate() != null;
    }

    public String getContainerDelegateGetterName() {
        Object value = getBeanInfo().getBeanDescriptor()
            .getValue("containerDelegate"); // NOI18N
        
        if (value != null && value instanceof String)
            return (String) value;
        else
            return null;
    }

    public String getJavaContainerDelegateString() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            return getName() + "." + delegateGetter + "()"; // NOI18N
        }
        else
            return getName();
    }
    
    /**
     * @return The JavaBean visual container represented by this
     * RADVisualComponent
     */
    
    public Container getContainerDelegate(Object container) {
        if (container instanceof javax.swing.RootPaneContainer)
            return ((javax.swing.RootPaneContainer)container).getContentPane();
        else if (container instanceof javax.swing.JRootPane)
            return ((javax.swing.JRootPane)container).getContentPane();
        
        Container containerDelegate = (Container) container;
        
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            try {
                Method m = getBeanClass().getMethod(delegateGetter, new Class[0]);
                containerDelegate = (Container) m.invoke(container, new Object[0]);
            } catch (Exception e) {
                // IGNORE
            }
        }
        
        return containerDelegate;
    }

    public Method getContainerDelegateMethod() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            try {
                return getBeanClass().getMethod(delegateGetter, new Class[0]);
            }
            catch (NoSuchMethodException ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
            }
        }
        return null;
    }

    public void setLayoutNodeReference(LayoutNode node) {
        this.layoutNode = node;
    }

    public LayoutNode getLayoutNodeReference() {
        return layoutNode;
    }

    RADMenuComponent getContainerMenu() {
        return containerMenu;
    }

    boolean canHaveMenu(Class menuClass) {
        return (JMenuBar.class.isAssignableFrom(menuClass)
                  && RootPaneContainer.class.isAssignableFrom(getBeanClass()))
               ||
               (MenuBar.class.isAssignableFrom(menuClass)
                  && Frame.class.isAssignableFrom(getBeanClass())
                  && !JFrame.class.isAssignableFrom(getBeanClass()));
    }

    // -----------------------------------------------------------------------------
    // Layout Manager management

    /** Called to obtain a Java code to be used to generate code to access the
     * container for adding subcomponents.  It is expected that the returned
     * code is either ""(in which case the form is the container) or is a name
     * of variable or method call ending with
     * "."(e.g. "container.getContentPane().").
     * @return the prefix code for generating code to add subcomponents to this
     * container
     */
    public String getContainerGenName() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null)
            return getName() + "." + delegateGetter + "()."; // NOI18N
        else
            return getName() + "."; // NOI18N
    }

    // -----------------------------------------------------------------------------
    // SubComponents Management

    /** @return visual subcomponents (not the menu component) */
    public RADVisualComponent[] getSubComponents() {
        RADVisualComponent[] components = new RADVisualComponent[subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public RADVisualComponent getSubComponent(int index) {
        return (RADVisualComponent) subComponents.get(index);
    }

    // the following methods implement ComponentContainer interface

    /** @return all subcomponents (including the menu component) */
    public RADComponent[] getSubBeans() {
        int n = subComponents.size();
        if (containerMenu != null)
            n++;

        RADComponent[] components = new RADComponent[n];
        subComponents.toArray(components);
        if (containerMenu != null)
            components[n-1] = containerMenu;

        return components;
    }

    public void initSubComponents(RADComponent[] initComponents) {
        subComponents = new ArrayList(initComponents.length);
        for (int i = 0; i < initComponents.length; i++) {
            RADComponent comp = initComponents[i];

            if (comp instanceof RADVisualComponent)
                subComponents.add(comp);
            else if (comp instanceof RADMenuComponent)
                containerMenu = (RADMenuComponent) comp; // [what with the current menu?]
            else
                continue; // [just ignore?]

            comp.setParentComponent(this);
        }
    }

    public void reorderSubComponents(int[] perm) {
        layoutSupport.removeAll();

        for (int i = 0; i < perm.length; i++) {
            int from = i;
            int to = perm[i];
            if (from == to) continue;
            Object value = subComponents.remove(from);
            if (from < to) {
                subComponents.add(to - 1, value);
            } else {
                subComponents.add(to, value);
            }
        }

        RADVisualComponent[] components =
            new RADVisualComponent[subComponents.size()];
        LayoutConstraints[] constraints =
            new LayoutConstraints[subComponents.size()];

        for (int i=0; i < components.length; i++) {
            RADVisualComponent comp = (RADVisualComponent) subComponents.get(i);
            components[i] = comp;
            constraints[i] = layoutSupport.getStoredConstraints(comp);
        }

        layoutSupport.addComponents(components, constraints);
    }

    public void add(RADComponent comp) {
        if (comp instanceof RADVisualComponent)
            subComponents.add(comp);
        else if (comp instanceof RADMenuComponent)
            containerMenu = (RADMenuComponent) comp;  // [what with the current menu?]
        else
            return; // [just ignore?]

        comp.setParentComponent(this);
    }

    public void remove(RADComponent comp) {
        if (comp instanceof RADVisualComponent) {
            layoutSupport.removeComponent((RADVisualComponent) comp,
                                          subComponents.indexOf(comp));
            if (subComponents.remove(comp))
                comp.setParentComponent(null);
        }
        else if (comp == containerMenu) {
            containerMenu = null;
            comp.setParentComponent(null);
        }
        else return;
    }

    public int getIndexOf(RADComponent comp) {
        if (comp != null && comp == containerMenu)
            return subComponents.size();

        return subComponents.indexOf(comp);
    }
}
