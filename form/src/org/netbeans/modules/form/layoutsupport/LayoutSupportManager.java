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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.beans.*;
import java.util.*;

import org.openide.nodes.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.delegates.NullLayoutSupport;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * Main class of general layout support infrastructure. Connects form editor
 * metadata with specialized LayoutSupportDelegate implementations (layout
 * specific functionality is delegated to the right LayoutSupportDelegate).
 *
 * @author Tomas Pavek
 */

public final class LayoutSupportManager implements LayoutSupportContext {

    // possible component resizing directions (bit flag constants)
    public static final int RESIZE_UP = 1;
    public static final int RESIZE_DOWN = 2;
    public static final int RESIZE_LEFT = 4;
    public static final int RESIZE_RIGHT = 8;

    private LayoutSupportDelegate layoutDelegate;

    private Node.PropertySet[] propertySets;

    private LayoutListener layoutListener;

    private RADVisualContainer metaContainer;

    private Container primaryContainer; // bean instance from metaContainer
    private Container primaryContainerDelegate; // container delegate for it

    private CodeStructure codeStructure;

    private CodeExpression containerCodeExpression;
    private CodeExpression containerDelegateCodeExpression;

    // ----------
    // initialization

    // initialization for a new container, layout delegate is set to null
    public void initialize(RADVisualContainer container,
                           CodeStructure codeStructure)
    {
        if (layoutDelegate != null)
            removeLayoutDelegate(false);

        this.metaContainer = container;
        this.codeStructure = codeStructure;

        containerCodeExpression = metaContainer.getCodeExpression();
        containerDelegateCodeExpression = null;
    }

    // Creation and initialization of a layout delegate for a new container.
    // Method initialize(...) must be called first.
    public boolean initializeLayoutDelegate(boolean fromCode)
        throws Exception
    {
        LayoutSupportDelegate delegate = null;
        LayoutManager lmInstance = null;

        FormModel formModel = metaContainer.getFormModel();
        LayoutSupportRegistry layoutRegistry =
            LayoutSupportRegistry.getRegistry(formModel);

        // first try to find a dedicated layout delegate (for the container)
        Class layoutDelegateClass = layoutRegistry.getSupportClassForContainer(
                                                  metaContainer.getBeanClass());

        if (layoutDelegateClass != null) {
            delegate = layoutRegistry.createSupportInstance(layoutDelegateClass);
        }
        else {
            // find a general layout delegate (for LayoutManager of the container)
            if (fromCode) { // initialization from code
                Iterator it = CodeStructure.getDefinedStatementsIterator(
                                      getContainerDelegateCodeExpression());
                CodeStatement[] statements =
                    CodeStructure.filterStatements(
                        it, AbstractLayoutSupport.getSetLayoutMethod());

                if (statements.length > 0) { // setLayout method found
                    CodeExpressionOrigin layoutOrigin =
                        statements[0].getStatementParameters()[0].getOrigin();
                    delegate = layoutRegistry.createSupportForLayout(
                                                  layoutOrigin.getType());
                    // handle special case of null layout
                    if (delegate == null)
                        if (layoutOrigin.getType() == LayoutManager.class
                            && layoutOrigin.getCreationParameters().length == 0
                            && layoutOrigin.getParentExpression() == null
                            && "null".equals(layoutOrigin.getJavaCodeString( // NOI18N
                                                                  null, null)))
                        {
                            delegate = new NullLayoutSupport();
                        }
                        else return false;
                }
            }

            if (delegate == null) { // initialization from LayoutManager instance
                Container contDel = getPrimaryContainerDelegate();
                if (contDel.getComponentCount() == 0) {
                    // we can still handle only empty containers ...
                    lmInstance = contDel.getLayout();
                    delegate = lmInstance != null ?
                        layoutRegistry.createSupportForLayout(lmInstance.getClass()) :
                        new NullLayoutSupport();
                }
                else {
                    RuntimeException ex = new IllegalArgumentException();
                    org.openide.ErrorManager.getDefault().annotate(
                        ex, AbstractLayoutSupport.getBundle().getString(
                                            "MSG_ERR_NonEmptyContainer")); // NOI18N
                    throw ex;
                }
            }
        }

        if (delegate == null)
            return false;

        setLayoutDelegate(delegate, lmInstance, fromCode);

        return true;
    }

    public void setLayoutDelegate(LayoutSupportDelegate newDelegate,
                                  LayoutManager lmInstance,
                                  boolean fromCode)
        throws Exception
    {
        LayoutConstraints[] oldConstraints;
        LayoutSupportDelegate oldDelegate = layoutDelegate;

        if (layoutDelegate != null
                && (layoutDelegate != newDelegate || !fromCode))
            oldConstraints = removeLayoutDelegate(true);
        else
            oldConstraints = null;

        layoutDelegate = newDelegate;
        propertySets = null;

        if (layoutDelegate != null) {
            try {
                layoutDelegate.initialize(this, lmInstance, fromCode);
                if (!fromCode)
                    fillLayout(oldConstraints);
                getPropertySets(); // force properties and listeners creation
            }
            catch (Exception ex) {
                removeLayoutDelegate(false);
                layoutDelegate = oldDelegate;
                if (layoutDelegate != null)
                    fillLayout(null);
                throw ex;
            }
        }
    }

    public LayoutSupportDelegate getLayoutDelegate() {
        return layoutDelegate;
    }

    public void setUnknownLayoutDelegate(boolean fromCode) {
        try {
            setLayoutDelegate(new UnknownLayoutSupport(), null, fromCode);
        }
        catch (Exception ex) { // nothing should happen, ignore
            ex.printStackTrace();
        }
    }

    public boolean isUnknownLayout() {
        return layoutDelegate == null
               || layoutDelegate instanceof UnknownLayoutSupport;
    }

    // copy layout delegate from another container
    public void copyLayoutDelegateFrom(
                    LayoutSupportManager sourceLayoutSupport,
                    RADVisualComponent[] newMetaComps)
    {
        LayoutSupportDelegate sourceDelegate =
            sourceLayoutSupport.getLayoutDelegate();

        int componentCount = sourceDelegate.getComponentCount();

        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();

        if (layoutDelegate != null)
            removeLayoutDelegate(false);

        CodeExpression[] compExps = new CodeExpression[componentCount];
        Component[] primaryComps = new Component[componentCount];

        for (int i=0; i < componentCount; i++) {
            RADVisualComponent metacomp = newMetaComps[i];
            compExps[i] = metacomp.getCodeExpression();
            primaryComps[i] = metacomp.getComponent();
            ensureFakePeerAttached(primaryComps[i]);
        }

        LayoutSupportDelegate newDelegate =
            sourceDelegate.cloneLayoutSupport(this, compExps);

        newDelegate.setLayoutToContainer(cont, contDel);
        newDelegate.addComponentsToContainer(cont, contDel, primaryComps, 0);

        layoutDelegate = newDelegate;
    }

    public void clearPrimaryContainer() {
        layoutDelegate.clearContainer(getPrimaryContainer(),
                                      getPrimaryContainerDelegate());
    }

    public RADVisualContainer getMetaContainer() {
        return metaContainer;
    }

//    public boolean supportsArranging() {
//        return layoutDelegate instanceof LayoutSupportArranging;
//    }

    private LayoutConstraints[] removeLayoutDelegate(
                                    boolean extractConstraints)
    {
        CodeGroup code = layoutDelegate.getLayoutCode();
        if (code != null)
            CodeStructure.removeStatements(code.getStatementsIterator());

        int componentCount = layoutDelegate.getComponentCount();
        LayoutConstraints[] constraints = null;

        if (componentCount > 0) {
            RADVisualComponent[] metacomps = metaContainer.getSubComponents();
            if (extractConstraints)
                constraints = new LayoutConstraints[componentCount];

            for (int i=0; i < componentCount; i++) {
                LayoutConstraints constr = layoutDelegate.getConstraints(i);
                if (extractConstraints)
                    constraints[i] = constr;
                if (constr != null)
                    metacomps[i].setLayoutConstraints(layoutDelegate.getClass(),
                                                      constr);
                code = layoutDelegate.getComponentCode(i);
                if (code != null)
                    CodeStructure.removeStatements(code.getStatementsIterator());
            }
        }

        layoutDelegate.removeAll();
        layoutDelegate.clearContainer(getPrimaryContainer(),
                                      getPrimaryContainerDelegate());
        layoutDelegate = null;

        return constraints;
    }

    private void fillLayout(LayoutConstraints[] oldConstraints) {
        RADVisualComponent[] metacomps = metaContainer.getSubComponents();
        int componentCount = metacomps.length;

        CodeExpression[] compExps = new CodeExpression[componentCount];
        Component[] designComps = new Component[componentCount];
        Component[] primaryComps = new Component[componentCount];
        LayoutConstraints[] newConstraints = new LayoutConstraints[componentCount];

        FormDesigner designer = metaContainer.getFormModel().getFormDesigner();

        for (int i=0; i < componentCount; i++) {
            RADVisualComponent metacomp = metacomps[i];

            compExps[i] = metacomp.getCodeExpression();
            primaryComps[i] = metacomp.getComponent();
            ensureFakePeerAttached(primaryComps[i]);
            newConstraints[i] = metacomp.getLayoutConstraints(
                                             layoutDelegate.getClass());

            Component comp = (Component) designer.getComponent(metacomp);
            designComps[i] = comp != null ? comp : metacomp.getComponent();
        }

        if (oldConstraints != null)
            layoutDelegate.convertConstraints(oldConstraints,
                                              newConstraints,
                                              designComps);

        if (componentCount > 0) {
            layoutDelegate.acceptNewComponents(compExps, newConstraints, 0);
            layoutDelegate.addComponents(compExps, newConstraints, 0);

            for (int i=0; i < componentCount; i++)
                metacomps[i].resetConstraintsProperties();
        }

        // setup primary container
        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();
//        layoutDelegate.clearContainer(cont, contDel);
        layoutDelegate.setLayoutToContainer(cont, contDel);
        if (componentCount > 0)
            layoutDelegate.addComponentsToContainer(cont, contDel,
                                                    primaryComps, 0);
    }

    // ---------
    // public API delegated to LayoutSupportDelegate

    public boolean isDedicated() {
        return layoutDelegate.isDedicated();
    }

    // node presentation
    public boolean shouldHaveNode() {
        return layoutDelegate.shouldHaveNode();
    }

    public String getDisplayName() {
        return layoutDelegate.getDisplayName();
    }

    public Image getIcon(int type) {
        return layoutDelegate.getIcon(type);
    }

    // properties and customizer
    public Node.PropertySet[] getPropertySets() {
        if (propertySets == null) {
            propertySets = layoutDelegate.getPropertySets();

            for (int i=0; i < propertySets.length; i++) {
                Node.Property[] props = propertySets[i].getProperties();
                for (int j=0; j < props.length; j++)
                    if (props[j] instanceof FormProperty) {
                        FormProperty prop = (FormProperty) props[j];
                        prop.addVetoableChangeListener(getLayoutListener());
                        prop.addPropertyChangeListener(getLayoutListener());
                    }
            }
        }
        return propertySets;
    }

    public Node.Property[] getAllProperties() {
        if (layoutDelegate instanceof AbstractLayoutSupport)
            return ((AbstractLayoutSupport)layoutDelegate).getAllProperties();

        ArrayList allPropsList = new ArrayList();
        for (int i=0; i < propertySets.length; i++) {
            Node.Property[] props = propertySets[i].getProperties();
            for (int j=0; j < props.length; j++)
                allPropsList.add(props[j]);
        }

        Node.Property[] allProperties = new Node.Property[allPropsList.size()];
        allPropsList.toArray(allProperties);
        return allProperties;
    }

    public Node.Property getLayoutProperty(String name) {
        if (layoutDelegate instanceof AbstractLayoutSupport)
            return ((AbstractLayoutSupport)layoutDelegate).getProperty(name);

        Node.Property[] properties = getAllProperties();
        for (int i=0; i < properties.length; i++)
            if (name.equals(properties[i].getName()))
                return properties[i];

        return null;
    }

    public Class getCustomizerClass() {
        return layoutDelegate.getCustomizerClass();
    }

    public Component getSupportCustomizer() {
        return layoutDelegate.getSupportCustomizer();
    }

    // code meta data
    public CodeGroup getLayoutCode() {
        return layoutDelegate.getLayoutCode();
    }

    public CodeGroup getComponentCode(int index) {
        return layoutDelegate.getComponentCode(index);
    }

    public CodeGroup getComponentCode(RADVisualComponent metacomp) {
        int index = metaContainer.getIndexOf(metacomp);
        return index >= 0 && index < layoutDelegate.getComponentCount() ?
               layoutDelegate.getComponentCode(index) : null;
    }

    public int getComponentCount() {
        return layoutDelegate.getComponentCount();
    }

    // data validation
    public void acceptNewComponents(RADVisualComponent[] components,
                                    LayoutConstraints[] constraints,
                                    int index)
    {
        CodeExpression[] compExps = new CodeExpression[components.length];
        for (int i=0; i < components.length; i++)
            compExps[i] = components[i].getCodeExpression();

        layoutDelegate.acceptNewComponents(compExps, constraints, index);
    }

    // components adding/removing
    public void addComponents(RADVisualComponent[] components,
                              LayoutConstraints[] constraints,
                              int index)
    {
        CodeExpression[] compExps = new CodeExpression[components.length];
        Component[] comps = new Component[components.length];

        for (int i=0; i < components.length; i++) {
            compExps[i] = components[i].getCodeExpression();
            comps[i] = components[i].getComponent();
            ensureFakePeerAttached(comps[i]);
        }

        if (index < 0)
            index = layoutDelegate.getComponentCount();

        layoutDelegate.addComponents(compExps, constraints, index);

        for (int i=0; i < components.length; i++)
            components[i].resetConstraintsProperties();

        layoutDelegate.addComponentsToContainer(getPrimaryContainer(),
                                                getPrimaryContainerDelegate(),
                                                comps, index);
    }

    public void removeComponent(RADVisualComponent metacomp, int index) {
        // first store constraints in the meta component
        LayoutConstraints constr = layoutDelegate.getConstraints(index);
        if (constr != null)
            metacomp.setLayoutConstraints(layoutDelegate.getClass(), constr);

        // remove code
        CodeStructure.removeStatements(
            layoutDelegate.getComponentCode(index).getStatementsIterator());

        // remove the component from layout
        layoutDelegate.removeComponent(index);

        // remove the component instance from the primary container instance
        if (!layoutDelegate.removeComponentFromContainer(
                                getPrimaryContainer(),
                                getPrimaryContainerDelegate(),
                                metacomp.getComponent()))
        {   // layout delegate does not support removing individual components,
            // so we clear the container and add the remaining components again
            layoutDelegate.clearContainer(getPrimaryContainer(),
                                          getPrimaryContainerDelegate());

            RADVisualComponent[] metacomps = metaContainer.getSubComponents();
            if (metacomps.length > 1) {
                // we rely on that metacomp was not removed from the model yet
                Component[] comps = new Component[metacomps.length-1];
                for (int i=0; i < metacomps.length; i++) {
                    if (i != index) {
                        Component comp = metacomps[i].getComponent();
                        ensureFakePeerAttached(comp);
                        comps[i < index ? i : i-1] = comp;
                    }
                }
                layoutDelegate.addComponentsToContainer(
                                   getPrimaryContainer(),
                                   getPrimaryContainerDelegate(),
                                   comps,
                                   0);
            }
        }
    }

    public void removeAll() {
        // first store constraints in meta components
        RADVisualComponent[] components = metaContainer.getSubComponents();
        for (int i=0; i < components.length; i++) {
            LayoutConstraints constr =
                layoutDelegate.getConstraints(i);
            if (constr != null)
                components[i].setLayoutConstraints(layoutDelegate.getClass(),
                                                   constr);
        }

        // remove code of all components
        for (int i=0, n=layoutDelegate.getComponentCount(); i < n; i++)
            CodeStructure.removeStatements(
                layoutDelegate.getComponentCode(i).getStatementsIterator());

        // remove components from layout
        layoutDelegate.removeAll();

        // clear the primary container instance
        layoutDelegate.clearContainer(getPrimaryContainer(),
                                      getPrimaryContainerDelegate());
    }

    public boolean isLayoutChanged() {
        Container defaultContainer = (Container)
                BeanSupport.getDefaultInstance(metaContainer.getBeanClass());
        Container defaultContDelegate =
                metaContainer.getContainerDelegate(defaultContainer);

        return layoutDelegate.isLayoutChanged(defaultContainer,
                                              defaultContDelegate);
    }

    // managing constraints
    public LayoutConstraints getConstraints(int index) {
        return layoutDelegate.getConstraints(index);
    }

    public LayoutConstraints getConstraints(RADVisualComponent metacomp) {
        if (layoutDelegate == null)
            return null;

        int index = metaContainer.getIndexOf(metacomp);
        return index >= 0 && index < layoutDelegate.getComponentCount() ?
               layoutDelegate.getConstraints(index) : null;
    }

    public static LayoutConstraints storeConstraints(
                                        RADVisualComponent metacomp)
    {
        RADVisualContainer parent = metacomp.getParentContainer();
        if (parent == null)
            return null;

        LayoutSupportManager layoutSupport = parent.getLayoutSupport();
        LayoutConstraints constr = layoutSupport.getConstraints(metacomp);
        if (constr != null)
            metacomp.setLayoutConstraints(
                         layoutSupport.getLayoutDelegate().getClass(),
                         constr);
        return constr;
    }

    public LayoutConstraints getStoredConstraints(RADVisualComponent metacomp) {
        return metacomp.getLayoutConstraints(layoutDelegate.getClass());
    }

    // managing live components
    public void setLayoutToContainer(Container container,
                                     Container containerDelegate)
    {
        layoutDelegate.setLayoutToContainer(container, containerDelegate);
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        layoutDelegate.addComponentsToContainer(container, containerDelegate,
                                                components, index);
    }

    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component)
    {
        return layoutDelegate.removeComponentFromContainer(
                            container, containerDelegate, component);
    }

    public boolean clearContainer(Container container,
                                  Container containerDelegate)
    {
        return layoutDelegate.clearContainer(container, containerDelegate);
    }

    // drag and drop support
    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont,
                                               Point posInComp)
    {
        return layoutDelegate.getNewConstraints(container, containerDelegate,
                                                component, index,
                                                posInCont, posInComp);
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        return layoutDelegate.getNewIndex(container, containerDelegate,
                                          component, index,
                                          posInCont, posInComp);
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        return layoutDelegate.paintDragFeedback(container, containerDelegate,
                                                component,
                                                newConstraints, newIndex,
                                                g);
    }

    // resizing support
    public int getResizableDirections(Container container,
                                      Container containerDelegate,
                                      Component component,
                                      int index)
    {
        return layoutDelegate.getResizableDirections(container,
                                                     containerDelegate,
                                                     component, index);
    }

    public LayoutConstraints getResizedConstraints(Container container,
                                                   Container containerDelegate,
                                                   Component component,
                                                   int index,
                                                   Insets sizeChanges,
                                                   Point posInCont)
    {
        return layoutDelegate.getResizedConstraints(container,
                                                    containerDelegate,
                                                    component, index,
                                                    sizeChanges,
                                                    posInCont);
    }

    // arranging support
    public void processMouseClick(Point p,
                                  Container cont,
                                  Container contDelegate)
    {
        layoutDelegate.processMouseClick(p, cont, contDelegate);
    }

    // arranging support
    public void selectComponent(int index) {
        layoutDelegate.selectComponent(index);
    }

    // arranging support
    public void arrangeContainer(Container container,
                                 Container containerDelegate)
    {
        layoutDelegate.arrangeContainer(container, containerDelegate);
    }

    // -----------
    // API for layout delegates (LayoutSupportContext implementation)

    public CodeStructure getCodeStructure() {
        return codeStructure;
    }

    public CodeExpression getContainerCodeExpression() {
        return containerCodeExpression;
    }

    public CodeExpression getContainerDelegateCodeExpression() {
        if (containerDelegateCodeExpression == null) {
            java.lang.reflect.Method delegateGetter =
                metaContainer.getContainerDelegateMethod();

            if (delegateGetter != null) { // there should be a container delegate
                Iterator it = CodeStructure.getDefinedExpressionsIterator(
                                                  containerCodeExpression);
                CodeExpression[] expressions = CodeStructure.filterExpressions(
                                                            it, delegateGetter);
                if (expressions.length > 0) {
                    // the expresion for the container delegate already exists
                    containerDelegateCodeExpression = expressions[0];
                }
                else { // create a new expresion for the container delegate
                    CodeExpressionOrigin origin = CodeStructure.createOrigin(
                                                    containerCodeExpression,
                                                    delegateGetter,
                                                    null);
                    containerDelegateCodeExpression =
                        codeStructure.createExpression(origin);
                }
            }
            else // no special container delegate
                containerDelegateCodeExpression = containerCodeExpression;
        }

        return containerDelegateCodeExpression;
    }

    // return container instance of meta container
    public Container getPrimaryContainer() {
        return (Container) metaContainer.getBeanInstance();
    }

    // return container delegate of container instance of meta container
    public Container getPrimaryContainerDelegate() {
        Container defCont = (Container) metaContainer.getBeanInstance();
        if (primaryContainerDelegate == null || primaryContainer != defCont) {
            primaryContainer = defCont;
            primaryContainerDelegate =
                metaContainer.getContainerDelegate(defCont);
        }
        return primaryContainerDelegate;
    }

    // return component instance of meta component
    public Component getPrimaryComponent(int index) {
        return metaContainer.getSubComponent(index).getComponent();
    }

    public void updatePrimaryContainer() {
        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();

        layoutDelegate.clearContainer(cont, contDel);
        layoutDelegate.setLayoutToContainer(cont, contDel);

        RADVisualComponent[] components = metaContainer.getSubComponents();
        if (components.length > 0) {
            Component[] comps = new Component[components.length];
            for (int i=0; i < components.length; i++) {
                comps[i] = components[i].getComponent();
                ensureFakePeerAttached(comps[i]);
            }

            layoutDelegate.addComponentsToContainer(cont, contDel, comps, 0);
        }
    }

    public void containerLayoutChanged(PropertyChangeEvent ev)
        throws PropertyVetoException
    {
        if (ev != null && ev.getPropertyName() != null) {
            layoutDelegate.acceptContainerLayoutChange(getEventWithValues(ev));

            FormModel formModel = metaContainer.getFormModel();
            formModel.fireContainerLayoutChanged(metaContainer,
                                                 ev.getPropertyName(),
                                                 ev.getOldValue(),
                                                 ev.getNewValue());
        }
        else propertySets = null;

        LayoutNode node = metaContainer.getLayoutNodeReference();
        if (node != null) {
            // propagate the change to node
            if (ev != null && ev.getPropertyName() != null)
                node.fireLayoutPropertiesChange();
            else
                node.fireLayoutPropertySetsChange();
        }
    }

    public void componentLayoutChanged(int index, PropertyChangeEvent ev)
        throws PropertyVetoException
    {
        RADVisualComponent metacomp = metaContainer.getSubComponent(index);

        if (ev != null && ev.getPropertyName() != null) {
            layoutDelegate.acceptComponentLayoutChange(index,
                                                       getEventWithValues(ev));

            FormModel formModel = metaContainer.getFormModel();
            formModel.fireComponentLayoutChanged(metacomp,
                                                 ev.getPropertyName(),
                                                 ev.getOldValue(),
                                                 ev.getNewValue());

            if (metacomp.getNodeReference() != null) // propagate the change to node
                metacomp.getNodeReference().firePropertyChangeHelper(
                                                     null, null, null);
//                                              ev.getPropertyName(),
//                                              ev.getOldValue(),
//                                              ev.getNewValue());
        }
        else {
            if (metacomp.getNodeReference() != null) // propagate the change to node
                metacomp.getNodeReference().fireComponentPropertySetsChange();
            metacomp.resetConstraintsProperties();
        }
    }

    private static PropertyChangeEvent getEventWithValues(PropertyChangeEvent ev) {
        Object oldVal = ev.getOldValue();
        Object newVal = ev.getNewValue();
        if (oldVal instanceof FormProperty.ValueWithEditor)
            ev = new PropertyChangeEvent(
                         ev.getSource(),
                         ev.getPropertyName(),
                         ((FormProperty.ValueWithEditor)oldVal).getValue(),
                         ((FormProperty.ValueWithEditor)newVal).getValue());
        return ev;
    }

    // ---------

    private LayoutListener getLayoutListener() {
        if (layoutListener == null)
            layoutListener = new LayoutListener();
        return layoutListener;
    }

    private class LayoutListener implements VetoableChangeListener,
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
                ev = new PropertyChangeEvent(layoutDelegate,
                                             ((FormProperty)source).getName(),
                                             ev.getOldValue(),
                                             ev.getNewValue());

                containerLayoutChanged(ev);
            }
        }

        public void propertyChange(PropertyChangeEvent ev) {
            Object source = ev.getSource();
            if (source instanceof FormProperty
                && FormProperty.CURRENT_EDITOR.equals(ev.getPropertyName()))
            {
                ev = new PropertyChangeEvent(layoutDelegate,
                                             null, null, null);
                try {
                    containerLayoutChanged(ev);
                }
                catch (PropertyVetoException ex) {} // should not happen
            }
        }
    }

    private static void ensureFakePeerAttached(Component comp) {
        FakePeerSupport.attachFakePeer(comp);
        if (comp instanceof Container)
            FakePeerSupport.attachFakePeerRecursively((Container)comp);
    }
}
