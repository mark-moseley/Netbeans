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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.lang.reflect.Method;

import org.openide.nodes.Node;
import org.openide.util.Utilities;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * Support for null layout manager.
 *
 * @author Tomas Pavek
 */

public class NullLayoutSupport extends AbsoluteLayoutSupport {

    private static Method setBoundsMethod;
    
    /** The icon for NullLayout. */
    private static String iconURL = "/org/netbeans/modules/form/layoutsupport/resources/NullLayout.gif"; // NOI18N
    /** The icon for NullLayout. */
    private static String icon32URL = "/org/netbeans/modules/form/layoutsupport/resources/NullLayout32.gif"; // NOI18N    

    /** Gets the supported layout manager class - this one is rather special,
     * it's null.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return null;
    }

    /** Provides an icon to be used for the layout node in Component
     * Inspector. Only 16x16 color icon is required.
     * @param type is one of BeanInfo constants: ICON_COLOR_16x16,
     *        ICON_COLOR_32x32, ICON_MONO_16x16, ICON_MONO_32x32
     * @return icon to be displayed for node in Component Inspector
     */
    public Image getIcon(int type) {
        switch (type) {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                return Utilities.loadImage(iconURL);
            default:
                return Utilities.loadImage(icon32URL);
        }
    }

    /** Sets up the layout (without adding components) on a real container,
     * according to the internal metadata representation.
     * @param container instance of a real container to be set
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     */
    public void setLayoutToContainer(Container container,
                                     Container containerDelegate)
    {
        containerDelegate.setLayout(null);
    }

    /** Adds real components to given container (according to layout
     * constraints stored for the components).
     * @param container instance of a real container to be added to
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param components components to be added
     * @param index position at which to add the components to container
     */
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        for (int i=0; i < components.length; i++) {
            LayoutConstraints constr = getConstraints(i + index);
            if (constr instanceof AbsoluteLayoutConstraints) {
                Component comp = components[i];
                Rectangle bounds = ((AbsoluteLayoutConstraints)constr).getBounds();
                if (bounds.width == -1 || bounds.height == -1) {
                    Dimension pref = comp.getPreferredSize();
                    if (bounds.width == -1)
                        bounds.width = pref.width;
                    if (bounds.height == -1)
                        bounds.height = pref.height;
                }
                containerDelegate.add(comp, i + index);
                comp.setBounds(bounds);
            }
        }
    }

    // ---------

    /** Creates code structures for a new layout manager (opposite to
     * readInitLayoutCode).
     * @param initLayoutCode CodeGroup to be filled with relevant
     *        initialization code
     * @return created CodeExpression representing the layout manager
     *         (so representing null value in this case)
     */
    protected CodeExpression createInitLayoutCode(CodeGroup layoutCode) {
        return getCodeStructure().createNullExpression(LayoutManager.class);
    }

    /** This method is used for scanning code structures and recognizing
     * components added to containers and their constraints. It's called from
     * initialize method. When a relevant code statement is found, then the
     * CodeExpression of component is get and added to component, and also the
     * layout constraints information is read. The special thing for null
     * layout is that components are initailized with setBounds call instead of
     * using constraints object, so we must override the reading process from
     * AbstractLayoutSupport.
     * @param statement CodeStatement to be tested if it contains relevant code
     * @param componentCode CodeGroup to be filled with all component code
     * @return CodeExpression representing found component; null if the
     *         statement is not relevant
     */
    protected CodeExpression readComponentCode(CodeStatement statement,
                                               CodeGroup componentCode)
    {
        if (getSimpleAddMethod().equals(statement.getMetaObject())) {
            CodeExpression compExp = statement.getStatementParameters()[0];
            componentCode.addStatement(statement);

            AbsoluteLayoutConstraints constr =
                new AbsoluteLayoutConstraints(0, 0, -1, -1);
            constr.nullMode = true;
//            constr.refComponent = getLayoutContext().getPrimaryComponent(index);

            // search for setBounds statement on component
            Iterator it = CodeStructure.getDefinedStatementsIterator(compExp);
            CodeStatement[] statements = CodeStructure.filterStatements(
                                                it, getSetBoundsMethod());
            if (statements.length > 0) {
                CodeStatement boundsStatement =
                    statements[statements.length-1];
                constr.readPropertyExpressions(
                    boundsStatement.getStatementParameters(), 0);
                componentCode.addStatement(boundsStatement);
            }
            getConstraintsList().add(constr);

            return compExp;
        }
        return null;
    }

    /** Creates code for a component added to the layout (opposite to
     * readComponentCode method). As well as for readComponentCode - null
     * layout requires the components to be initailized with setBounds call
     * instead of using constraints object, so this method must be overridden
     * (from AbstractLayoutSupport).
     * @param componentCode CodeGroup to be filled with complete component code
     *        (code for initializing the layout constraints and adding the
     *        component to the layout)
     * @param compExp CodeExpression object representing component
     * @param index position of the component in the layout
     */
    protected void createComponentCode(CodeGroup componentCode,
                                       CodeExpression compExp,
                                       int index)
    {
        // create code for "add" method
        componentCode.addStatement(
                CodeStructure.createStatement(
                        getActiveContainerCodeExpression(),
                        getSimpleAddMethod(),
                        new CodeExpression[] { compExp }));

        // create code for "setBounds" method
        LayoutConstraints constr = getConstraints(index);
        if (constr instanceof AbsoluteLayoutConstraints) {
            AbsoluteLayoutConstraints absConstr =
                (AbsoluteLayoutConstraints) constr;
            absConstr.nullMode = true;
            absConstr.refComponent = getLayoutContext().getPrimaryComponent(index);

            componentCode.addStatement(
                CodeStructure.createStatement(
                    compExp,
                    getSetBoundsMethod(),
                    absConstr.createPropertyExpressions(getCodeStructure(), 0)));
        }
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
}
