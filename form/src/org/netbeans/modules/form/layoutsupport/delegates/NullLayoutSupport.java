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
 * @author Tomas Pavek
 */

public class NullLayoutSupport extends AbsoluteLayoutSupport {

    private static Image layoutIcon;
    private static Image layoutIcon32;

    private static Method setBoundsMethod;

    public Class getSupportedClass() {
        return null;
    }

    public Image getIcon(int type) {
        switch (type) {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                if (layoutIcon == null)
                    layoutIcon = Utilities.loadImage(
                        "org/netbeans/modules/form/layoutsupport/resources/NullLayout.gif"); // NOI18N
                return layoutIcon;

            default:
                if (layoutIcon32 == null)
                    layoutIcon32 = Utilities.loadImage(
                        "org/netbeans/modules/form/layoutsupport/resources/NullLayout32.gif"); // NOI18N
                return layoutIcon32;
        }
    }

    public void setLayoutToContainer(Container container,
                                     Container containerDelegate)
    {
        containerDelegate.setLayout(null);
    }

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

    protected CodeElement createInitLayoutCode(CodeConnectionGroup layoutCode) {
        return getCodeStructure().createNullElement(LayoutManager.class);
    }

    protected CodeElement readComponentCode(CodeConnection connection,
                                            CodeConnectionGroup componentCode)
    {
        if (getSimpleAddMethod().equals(connection.getConnectingObject())) {
            CodeElement compElement = connection.getConnectionParameters()[0];
            componentCode.addConnection(connection);

            AbsoluteLayoutConstraints constr =
                new AbsoluteLayoutConstraints(0, 0, -1, -1);
            constr.nullMode = true;
//            constr.refComponent = getLayoutContext().getPrimaryComponent(index);

            CodeConnection[] connections = CodeStructure.getConnections(
                                           compElement, getSetBoundsMethod());
            if (connections.length > 0) {
                CodeConnection boundsConnection =
                    connections[connections.length-1];
                constr.readPropertyElements(
                    boundsConnection.getConnectionParameters(), 0);
                componentCode.addConnection(boundsConnection);
            }
            getConstraintsList().add(constr);

            return compElement;
        }
        return null;
    }

    protected void createComponentCode(CodeConnectionGroup componentCode,
                                       CodeElement compElement,
                                       int index)
    {
        // create code for "add" method
        componentCode.addConnection(
                CodeStructure.createConnection(
                        getActiveContainerCodeElement(),
                        getSimpleAddMethod(),
                        new CodeElement[] { compElement }));

        // create code for "setBounds" method
        LayoutConstraints constr = getConstraints(index);
        if (constr instanceof AbsoluteLayoutConstraints) {
            AbsoluteLayoutConstraints absConstr =
                (AbsoluteLayoutConstraints) constr;
            absConstr.nullMode = true;
            absConstr.refComponent = getLayoutContext().getPrimaryComponent(index);

            componentCode.addConnection(
                CodeStructure.createConnection(
                    compElement,
                    getSetBoundsMethod(),
                    absConstr.createPropertyElements(getCodeStructure(), 0)));
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
