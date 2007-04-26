/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import java.beans.*;
import java.util.*;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * Support class for BorderLayout. This is an example of support for layout
 * manager using simple component constraints (String).
 *
 * @author Tran Duc Trung, Tomas Pavek, Jan Stola
 */
// Expects ltr orientation of designer
public class BorderLayoutSupport extends AbstractLayoutSupport
{
    /** Gets the supported layout manager class - BorderLayout.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return BorderLayout.class;
    }

    /** This method calculates layout constraints for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged, can be null
     * @param index position (index) of the component in its container;
     *        not needed for BorderLayout
     * @param posInCont position of mouse in the container delegate
     * @param posInComp position of mouse in the dragged component; not needed
     *        for BorderLayout
     * @return new LayoutConstraints object corresponding to the position of
     *         the component in the container
     */
    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont,
                                               Point posInComp)
    {
        if (component != null && component.getParent() != containerDelegate)
            component = null;

        String primary = BorderLayout.CENTER;
        String alternateX = null;
        String alternateY = null;

        int w = containerDelegate.getSize().width;
        int h = containerDelegate.getSize().height;

        Insets contInsets = containerDelegate.getInsets();
        int marginW = getMargin(w - contInsets.left - contInsets.right);
        int marginH = getMargin(h - contInsets.top - contInsets.bottom);

        int xC = 1; // center by default (0 - left, 1 - center, 2 - right)
        int yC = 1; // center by default (0 - top, 1 - center, 2 - bottom)

        if (w > 25) {
            if (posInCont.x < contInsets.left+marginW) xC = 0; // left
            else if (posInCont.x >= w-marginW-contInsets.right) xC = 2; // right
        }
        if (h > 25) {
            if (posInCont.y < contInsets.top+marginH) yC = 0; // top
            else if (posInCont.y >= h-marginH-contInsets.bottom) yC = 2; // bottom
        }

        if (xC == 0) primary = BorderLayout.LINE_START;
        else if (xC == 2) primary = BorderLayout.LINE_END;
        else alternateX = posInCont.x - contInsets.left <
                            (w - contInsets.left - contInsets.right)/2 ?
                BorderLayout.LINE_START : BorderLayout.LINE_END;

        if (yC == 0) { // top
            alternateX = primary;
            primary = BorderLayout.PAGE_START;
        }
        else if (yC == 2) { // bottom
            alternateX = primary;
            primary = BorderLayout.PAGE_END;
        }
        else alternateY = posInCont.y - contInsets.top <
                            (h - contInsets.top - contInsets.bottom)/2 ?
                BorderLayout.PAGE_START : BorderLayout.PAGE_END;

        String[] suggested = new String[] { primary, alternateY, alternateX };
        String[] free = findFreePositions();

        for (int i=0; i < suggested.length; i++) {
            String str = suggested[i];
            if (str == null) continue;

            for (int j=0; j  < free.length; j++)
                if (free[j].equals(str)) {
                    if (isAWTContainer()) {
                        str = (String) toAbsolute(str);
                    }
                    assistantParams = str;
                    return new BorderConstraints(str);
                }

            if (component != null) {
                int idx = getComponentOnPosition(str);
                if (containerDelegate.getComponent(idx) == component) {
                    if (isAWTContainer()) {
                        str = (String) toAbsolute(str);
                    }
                    assistantParams = str;
                    return new BorderConstraints(str);
                }
            }
        }
        if (isAWTContainer()) {
            free[0] = (String) toAbsolute(free[0]);
        }
        assistantParams = free[0];
        return new BorderConstraints(free[0]);
    }

    private boolean isAWTContainer() {
        // hack for CDC: only use absolute constraints for AWT components
        Container cont = getLayoutContext().getPrimaryContainer();
        return cont != null
               && !(cont instanceof javax.swing.JComponent)
               && !(cont instanceof javax.swing.RootPaneContainer);
    }

    private String assistantParams;
    public String getAssistantContext() {
        return "borderLayout"; // NOI18N
    }

    public Object[] getAssistantParams() {
        return new Object[] {assistantParams};
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged, can be null
     * @param newConstraints component layout constraints to be presented
     * @param newIndex component's index position to be presented; not used
     *        for BorderLayout
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (true in this case)
     */
    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        String position = (String) newConstraints.getConstraintsObject();
        Component[] comps = containerDelegate.getComponents();
        int index;

        Dimension contSize = containerDelegate.getSize();
        Insets contInsets = containerDelegate.getInsets();
        Dimension compPrefSize =
            component != null ? component.getPreferredSize() : new Dimension(0,0);

        int x1, y1, x2, y2;
        int marginW = getMargin(contSize.width - contInsets.left - contInsets.right);
        int marginH = getMargin(contSize.height - contInsets.top - contInsets.bottom);

        if (BorderLayout.PAGE_START.equals(position) || BorderLayout.NORTH.equals(position)) {
            x1 = contInsets.left;
            x2 = contSize.width - contInsets.right;
            y1 = contInsets.top;
            y2 = contInsets.top + (compPrefSize.height > 0 ?
                                   compPrefSize.height : marginH);
        }
        else if (BorderLayout.PAGE_END.equals(position) || BorderLayout.SOUTH.equals(position)) {
            x1 = contInsets.left;
            x2 = contSize.width - contInsets.right;
            y1 = contSize.height - contInsets.bottom
                   - (compPrefSize.height > 0 ? compPrefSize.height : marginH);
            y2 = contSize.height - contInsets.bottom;
        }
        else { // LINE_START, LINE_END or CENTER
            if (BorderLayout.LINE_START.equals(position) || BorderLayout.WEST.equals(position)) {
                x1 = contInsets.left;
                x2 = contInsets.left + (compPrefSize.width > 0 ?
                                        compPrefSize.width : marginW);
            }
            else if (BorderLayout.LINE_END.equals(position) || BorderLayout.EAST.equals(position)) {
                x1 = contSize.width - contInsets.right
                       - (compPrefSize.width > 0 ? compPrefSize.width : marginW);
                x2 = contSize.width - contInsets.right;
            }
            else { // CENTER
                index = getComponentOnPosition(BorderLayout.LINE_START);
                x1 = contInsets.left;
                if (index >= 0)
                    x1 += comps[index].getSize().width;

                index = getComponentOnPosition(BorderLayout.LINE_END);
                x2 = contSize.width - contInsets.right;
                if (index >= 0)
                    x2 -= comps[index].getSize().width;
            }

            // y1 and y2 are the same for LINE_START, LINE_END and CENTER
            index = getComponentOnPosition(BorderLayout.PAGE_START);
            y1 = contInsets.top;
            if (index >= 0)
                y1 += comps[index].getSize().height;

            index = getComponentOnPosition(BorderLayout.PAGE_END);
            y2 = contSize.height - contInsets.bottom;
            if (index >= 0)
                y2 -= comps[index].getSize().height;
        }

        if (x1 >= x2) {
            x1 = contInsets.left;
            x2 = contSize.width - contInsets.right;
            if (x1 >= x2) return true; // container is too small
        }
        if (y1 >= y2) {
            y1 = contInsets.top;
            x2 = contSize.height - contInsets.bottom;
            if (y1 >= y2) return true; // container is too small
        }

        g.drawRect(x1, y1, x2-x1-1, y2-y1-1);

        return true;
    }

    // ----------

    /** This method is called from readComponentCode method to read layout
     * constraints of a component from code. It is just a simple String for
     * BorderLayout.
     * @param constrExp CodeExpression object of the constraints (taken from
     *        add method in the code)
     * @param constrCode CodeGroup to be filled with the relevant constraints
     *        initialization code; not needed here because String is just
     *        a single code expression
     * @param compExp CodeExpression of the component for which the constraints
     *        are read (not needed here)
     * @return LayoutConstraints based on information read form code
     */
    protected LayoutConstraints readConstraintsCode(CodeExpression constrExp,
                                                    CodeGroup constrCode,
                                                    CodeExpression compExp)
    {
        BorderConstraints constr = new BorderConstraints(BorderLayout.CENTER);
        FormCodeSupport.readPropertyExpression(constrExp,
                                               constr.getProperties()[0],
                                               false);
        return constr;
    }

    /** Called from createComponentCode method, creates code for a component
     * layout constraints (opposite to readConstraintsCode).
     * @param constrCode CodeGroup to be filled with constraints code; not
     *        needed here String (used as the constraints object) is just
     *        a single code expression
     * @param constr layout constraints metaobject representing the constraints
     * @param compExp CodeExpression object representing the component; not
     *        needed here
     * @return created CodeExpression representing the layout constraints
     */
    protected CodeExpression createConstraintsCode(CodeGroup constrCode,
                                                   LayoutConstraints constr,
                                                   CodeExpression compExp,
                                                   int index)
    {
        if (!(constr instanceof BorderConstraints))
            return null; // should not happen

        return getCodeStructure().createExpression(
                   FormCodeSupport.createOrigin(constr.getProperties()[0]));
    }

    /** This method is called to get a default component layout constraints
     * metaobject in case it is not provided (e.g. in addComponents method).
     * @return the default LayoutConstraints object for the supported layout
     */
    protected LayoutConstraints createDefaultConstraints() {
        String pos = findFreePositions()[0];
        if (isAWTContainer()) {
            pos = (String) toAbsolute(pos);
        }
        return new BorderConstraints(pos);
    }

    // ----------------

    private String[] findFreePositions() {
        ArrayList positions = new ArrayList(6);

        if (getComponentOnPosition(BorderLayout.CENTER) == -1)
            positions.add(BorderLayout.CENTER);
        if (getComponentOnPosition(BorderLayout.PAGE_START) == -1)
            positions.add(BorderLayout.PAGE_START);
        if (getComponentOnPosition(BorderLayout.PAGE_END) == -1)
            positions.add(BorderLayout.PAGE_END);
        if (getComponentOnPosition(BorderLayout.LINE_END) == -1)
            positions.add(BorderLayout.LINE_END);
        if (getComponentOnPosition(BorderLayout.LINE_START) == -1)
            positions.add(BorderLayout.LINE_START);
        if (positions.size() == 0)
            positions.add(BorderLayout.CENTER);

        String[] free = new String[positions.size()];
        positions.toArray(free);
        return free;
    }

    private int getComponentOnPosition(String position) {
        java.util.List constraints = getConstraintsList();
        if (constraints == null)
            return -1;
        
        position = (String)toAbsolute(position);
        for (int i=0, n=constraints.size(); i < n; i++) {
            LayoutConstraints constr = (LayoutConstraints) constraints.get(i);
            if (constr != null && position.equals(toAbsolute(constr.getConstraintsObject())))
                return i;
        }

        return -1;
    }

    private static Object toAbsolute(Object constraint) {
        if (BorderLayout.LINE_START.equals(constraint)) {
            constraint = BorderLayout.WEST;
        } else if (BorderLayout.LINE_END.equals(constraint)) {
            constraint = BorderLayout.EAST;
        } else if (BorderLayout.PAGE_START.equals(constraint)) {
            constraint = BorderLayout.NORTH;
        } else if (BorderLayout.PAGE_END.equals(constraint)) {
            constraint = BorderLayout.SOUTH;
        }
        return constraint;
    }

    private int getMargin(int size) {
        int margin = size/8;
        if (margin < 10) margin = 10;
        if (margin > 50) margin = 50;
        return margin;
    }

    // ----------------

    /** LayoutConstraints implementation class for component constraints of
     * BorderLayout.
     */
    public static class BorderConstraints implements LayoutConstraints {
        private String direction;

        private Node.Property[] properties;

        public BorderConstraints(String direction) {
            this.direction = direction;
        }

        public Node.Property[] getProperties() {
            if (properties == null) {
                properties = new FormProperty[] {
                    new FormProperty(
                            "BorderConstraints direction", // NOI18N
                            String.class,
                            getBundle().getString("PROP_direction"), // NOI18N
                            getBundle().getString("HINT_direction")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return direction;
                        }

                        public void setTargetValue(Object value) {
                            direction = (String)value;
                        }

                        public PropertyEditor getExpliciteEditor() {
                            return new BorderDirectionEditor();
                        }
                        public void setPropertyContext(
                            org.netbeans.modules.form.FormPropertyContext ctx)
                        { // disabling this method due to limited persistence
                        } // capabilities (compatibility with previous versions)
                    }
                };
                properties[0].setValue("NOI18N", Boolean.TRUE); // NOI18N
            }

            return properties;
        }

        public Object getConstraintsObject() {
            return direction;
        }

        public LayoutConstraints cloneConstraints() {
            return new BorderConstraints(direction);
        }
    }

    // ---------

    /** PropertyEditor for the BorderLayout constraints property.
     */
    static class BorderDirectionEditor extends PropertyEditorSupport {
        private final String[] values = {
            BorderLayout.CENTER,
            BorderLayout.LINE_START,
            BorderLayout.LINE_END,
            BorderLayout.PAGE_START,
            BorderLayout.PAGE_END,
            BorderLayout.WEST,
            BorderLayout.EAST,
            BorderLayout.NORTH,
            BorderLayout.SOUTH
        };
        private final String[] javaInitStrings = {
            "java.awt.BorderLayout.CENTER", // NOI18N
            "java.awt.BorderLayout.LINE_START", // NOI18N
            "java.awt.BorderLayout.LINE_END", // NOI18N
            "java.awt.BorderLayout.PAGE_START", // NOI18N
            "java.awt.BorderLayout.PAGE_END", // NOI18N
            "java.awt.BorderLayout.WEST", // NOI18N
            "java.awt.BorderLayout.EAST", // NOI18N
            "java.awt.BorderLayout.NORTH", // NOI18N
            "java.awt.BorderLayout.SOUTH" // NOI18N
        };

        public String[] getTags() {
            return values;
        }

        public String getAsText() {
            return (String)getValue();
        }

        public void setAsText(String str) {
            for (int i = 0; i < values.length; i++)
                if (str.equals(values[i])) {
                    setValue(str);
                    break;
                }
        }

        public String getJavaInitializationString() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return javaInitStrings[i];
            return null;
        }
    }
}
