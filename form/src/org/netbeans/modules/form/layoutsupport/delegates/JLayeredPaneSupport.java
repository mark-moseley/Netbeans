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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import javax.swing.*;
import java.beans.*;
import java.util.Iterator;
import java.lang.reflect.Method;

import org.openide.nodes.*;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * Dedicated layout support class for JLayeredPane. It is based on
 * AbsoluteLayoutSupport - similarly as NullLayoutSupport, but with one
 * additional constraints parameter - the layer.
 *
 * @author Tomas Pavek
 */

public class JLayeredPaneSupport extends AbsoluteLayoutSupport {

    private static Method setBoundsMethod;

    /** Gets the supported layout manager class - JLayeredPane.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return JLayeredPane.class;
    }

    /** This method is called when switching layout - giving an opportunity to
     * convert the previous constrainst of components to constraints of the new
     * layout (this layout). It should do nothing for JLayeredPane - but with
     * must override it from from AbsoluteLayoutSupport.
     * @param previousConstraints [input] layout constraints of components in
     *                                    the previous layout
     * @param currentConstraints [output] array of converted constraints for
     *                                    the new layout - to be filled
     * @param components [input] real components in a real container having the
     *                           previous layout
     */
    public void convertConstraints(LayoutConstraints[] previousConstraints,
                                   LayoutConstraints[] currentConstraints,
                                   Component[] components)
    {
        return; // not needed here (contrary to AbsoluteLayoutSupport)
    }

    /** Adds real components to given container (according to layout
     * constraints stored for the components).
     * @param container instance of a real container to be added to
     * @param containerDelegate effective container delegate of the container
     * @param components components to be added
     * @param index position at which to add the components to container
     */
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        if (!(container instanceof JLayeredPane))
            return;

        for (int i=0; i < components.length; i++) {
            LayoutConstraints constraints = getConstraints(i + index);
            if (constraints instanceof LayeredConstraints) {
                Component comp = components[i];
                container.add(comp, constraints.getConstraintsObject(), i + index);

                Rectangle bounds = ((LayeredConstraints)constraints).getBounds();
                if (bounds.width == -1 || bounds.height == -1) {
                    Dimension pref = comp.getPreferredSize();
                    if (bounds.width == -1)
                        bounds.width = pref.width;
                    if (bounds.height == -1)
                        bounds.height = pref.height;
                }
                comp.setBounds(bounds);
            }
        }
    }

    // ------

    /** This method is called from readComponentCode method to read layout
     * constraints of a component from code.
     * @param constrExp CodeExpression object of the constraints (taken from
     *        add method in the code)
     * @param constrCode CodeGroup to be filled with the relevant constraints
     *        initialization code
     * @param compExp CodeExpression of the component for which the constraints
     *        are read
     * @return LayoutConstraints based on information read form code
     */
    protected LayoutConstraints readConstraintsCode(CodeExpression constrExp,
                                                    CodeGroup constrCode,
                                                    CodeExpression compExp)
    {
        LayeredConstraints constr = new LayeredConstraints(0, 0, 0, -1, -1);
//        constr.refComponent = getLayoutContext().getPrimaryComponent(index);

        Iterator it = CodeStructure.getDefinedStatementsIterator(compExp);
        CodeStatement[] statements = CodeStructure.filterStatements(
                                            it, getSetBoundsMethod());
        if (statements.length > 0) {
            CodeStatement boundsStatement = statements[statements.length-1];
            constr.readPropertyExpressions(
                       boundsStatement.getStatementParameters(), 1);
            constrCode.addStatement(boundsStatement);
        }

        FormCodeSupport.readPropertyExpression(constrExp,
                                               constr.getProperties()[0],
                                               false);

        return constr;
    }

    /** Creates code for a component added to the layout (opposite to
     * readComponentCode method).
     * @param componentCode CodeGroup to be filled with complete component code
     *        (code for initializing the layout constraints and adding the
     *        component to the layout)
     * @param compExp CodeExpression object representing component
     * @param index position of the component in the layout
     */
    protected CodeExpression createConstraintsCode(CodeGroup constrCode,
                                                   LayoutConstraints constr,
                                                   CodeExpression compExp,
                                                   int index)
    {
        if (!(constr instanceof LayeredConstraints))
            return null;

        LayeredConstraints layerConstr = (LayeredConstraints) constr;
        layerConstr.refComponent = getLayoutContext().getPrimaryComponent(index);

        CodeStructure codeStructure = getCodeStructure();

        CodeStatement boundsStatement = CodeStructure.createStatement(
                          compExp,
                          getSetBoundsMethod(),
                          layerConstr.createPropertyExpressions(codeStructure, 1));
        constrCode.addStatement(boundsStatement);

        return codeStructure.createExpression(
                 FormCodeSupport.createOrigin(layerConstr.getProperties()[0]));
    }

    /** This method is called to get a default component layout constraints
     * metaobject in case it is not provided (e.g. in addComponents method).
     * @return the default LayoutConstraints object for the supported layout;
     *         null if no component constraints are used
     */
    protected LayoutConstraints createDefaultConstraints() {
        return new LayeredConstraints(0, 0, 0, -1, -1);
    }

    // ----------

    // overriding AbsoluteLayoutSupport
    protected LayoutConstraints createNewConstraints(
                                    LayoutConstraints currentConstr,
                                    int x, int y, int w, int h)
    {
        int layer = currentConstr instanceof LayeredConstraints ?
                    ((LayeredConstraints)currentConstr).getLayer() : 0;

        return new LayeredConstraints(layer, x, y, w, h);
    }

    private static Method getSetBoundsMethod() {
        if (setBoundsMethod == null) {
            try {
                setBoundsMethod = Component.class.getMethod(
                                    "setBounds", // NOI18N
                                    new Class[] { Integer.TYPE, Integer.TYPE,
                                                  Integer.TYPE, Integer.TYPE });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return setBoundsMethod;
    }

    // ----------

    /** Extended AbsoluteLayoutConstraints class - with additional layer
     * property.
     */
    public static class LayeredConstraints extends AbsoluteLayoutConstraints {
        private int layer;

        public LayeredConstraints(int layer, int x, int y, int w, int h) {
            super(x, y, w, h);
            this.layer = layer;
            nullMode = true;
        }

        public int getLayer() {
            return layer;
        }

        // ------

        public Object getConstraintsObject() {
            return new Integer(layer);
        }

        public LayoutConstraints cloneConstraints() {
            return new LayeredConstraints(layer, x, y, w, h);
        }

        // -------

        protected Node.Property[] createProperties() {
            Node.Property[] props = super.createProperties();
            Node.Property[] layeredProps = new Node.Property[props.length + 1];

            layeredProps[0] =
                new FormProperty("LayeredConstraints layer", // NOI18N
                                 Integer.TYPE,
                             getBundle().getString("PROP_layer"), // NOI18N
                             getBundle().getString("HINT_layer")) { // NOI18N

                    public Object getTargetValue() {
                        return new Integer(layer);
                    }
                    public void setTargetValue(Object value) {
                        layer = ((Integer)value).intValue();
                    }
                    public boolean supportsDefaultValue () {
                        return true;
                    }
                    public Object getDefaultValue() {
                        return new Integer(0);
                    }
                    public PropertyEditor getExpliciteEditor() {
                        return new LayerEditor();
                    }
                    public Object getValue(String key) {
                        if ("canEditAsText".equals(key)) // NOI18N
                            return Boolean.TRUE;
                        return super.getValue(key);
                    }
                    public void setPropertyContext(
                        org.netbeans.modules.form.FormPropertyContext ctx)
                    { // disabling this method due to limited persistence
                    } // capabilities (compatibility with previous versions)
                };

            for (int i=0; i < props.length; i++)
                layeredProps[i+1] = props[i];

            return layeredProps;
        }
    }

    // ---------

    public static final class LayerEditor extends PropertyEditorSupport {

        final String[] tags = {
            "DEFAULT_LAYER", // NOI18N
            "PALETTE_LAYER", // NOI18N
            "MODAL_LAYER", // NOI18N
            "POPUP_LAYER", // NOI18N
            "DRAG_LAYER" // NOI18N
        };

        final Integer[] values = {
            JLayeredPane.DEFAULT_LAYER,
            JLayeredPane.PALETTE_LAYER,
            JLayeredPane.MODAL_LAYER,
            JLayeredPane.POPUP_LAYER,
            JLayeredPane.DRAG_LAYER
        };

        final String[] javaInitStrings = {
            "javax.swing.JLayeredPane.DEFAULT_LAYER", // NOI18N
            "javax.swing.JLayeredPane.PALETTE_LAYER", // NOI18N
            "javax.swing.JLayeredPane.MODAL_LAYER", // NOI18N
            "javax.swing.JLayeredPane.POPUP_LAYER", // NOI18N
            "javax.swing.JLayeredPane.DRAG_LAYER" // NOI18N
        };

        public String[] getTags() {
            return tags;
        }

        public String getAsText() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return tags[i];

            return value.toString();
        }

        public void setAsText(String str) {
            for (int i=0; i < tags.length; i++)
                if (tags[i].equals(str)) {
                    setValue(values[i]);
                    return;
                }

            try {
                setValue(new Integer(Integer.parseInt(str)));
            } 
            catch (NumberFormatException e) {} // ignore
        }

        public String getJavaInitializationString() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return javaInitStrings[i];

            return value != null ? 
                       "new Integer(" + value.toString() + ")" // NOI18N
                       : null;
        }
    }
}
