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
import java.util.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * Support class for GridLayout.  This is an example of very simple layout
 * with no constraints; just basic drag & drop is implemented.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class GridLayoutSupport extends AbstractLayoutSupport
{
    /** Gets the supported layout manager class - GridLayout.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return GridLayout.class;
    }

    /** This method calculates position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged; not needed here
     * @param index position (index) of the component in its current container;
     *        not needed here
     * @param posInCont position of mouse in the container delegate
     * @param posInComp position of mouse in the dragged component;
     *        not needed here
     * @return index corresponding to the position of the component in the
     *         container
     */
    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(containerDelegate.getLayout() instanceof GridLayout))
            return -1;

        Component[] components = containerDelegate.getComponents();
        GridLayout layout = (GridLayout) containerDelegate.getLayout();
        int nrows = layout.getRows();
        int ncols = layout.getColumns();

        if (nrows <= 0 && ncols <= 0 || components.length == 0)
            return components.length;
        
        if (nrows != 0) {
            ncols = (components.length + nrows - 1) / nrows;
        }
        else {
            nrows = (components.length + ncols - 1) / ncols;
        }

        Dimension sz = containerDelegate.getSize();
        Insets insets = containerDelegate.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;

        int colwidth = sz.width / ncols;
        if (colwidth <= 0)
            return components.length;
        int col = (posInCont.x + colwidth / 2) / colwidth;
        
        int rowheight = sz.height / nrows;
        if (rowheight <= 0)
            return components.length;
        int row = posInCont.y / rowheight;

        int newIndex = row * ncols + col;
        return newIndex >= components.length ? components.length : newIndex;
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container;
     *        for layout managers we always use container delegate instead of
     *        the container
     * @param component the real component being dragged, not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for GridLayout
     * @param newIndex component's index position to be presented
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
        if (!(containerDelegate.getLayout() instanceof GridLayout))
            return false;

        Component[] components = containerDelegate.getComponents();
        GridLayout layout = (GridLayout) containerDelegate.getLayout();
        int dx = 12 + layout.getHgap() / 2;
        int x = 0, w = 24, y = 0, h = 0;
        
        if (newIndex <= 0) {
            if (components.length > 0) {
                Rectangle b = components[0].getBounds();
                x = b.x - dx ;
                y = b.y;
                h = b.height;
            }
            else {
                Insets ins = containerDelegate.getInsets();
                x = ins.left + 1;
                w = containerDelegate.getWidth() - ins.right - ins.left - 2;
                y = ins.top + 1;
                h = containerDelegate.getHeight() - ins.bottom - ins.top - 2;
            }
        }
        else if (newIndex >= components.length) {
            Rectangle b = components[components.length-1].getBounds();
            x = b.x + b.width - dx;
            y = b.y;
            h = b.height;
        }
        else {
            Rectangle b = components[newIndex].getBounds();
            x = b.x - dx;
            y = b.y;
            h = b.height;
        }

        g.drawRect(x, y, w, h);
        return true;
    }

    // ------------

    /** This method is called from readLayoutCode to read the layout manager
     * bean code (i.e. code for constructor and properties). This method is
     * overridden here because "rows" and "columns" properties are mutually
     * dependent (so not true JavaBean properties).
     * @param layoutExp CodeExpressin of the layout manager
     * @param initLayoutCode CodeGroup to be filled with relevant
     *        initialization code
     */
    protected void readInitLayoutCode(CodeExpression layoutExp,
                                      CodeGroup initLayoutCode)
    {
        CodeExpression[] params = layoutExp.getOrigin().getCreationParameters();
        if (params.length > 0) {
            Object rowsValue = params[0].getOrigin().getValue();
            if (rowsValue instanceof Integer
                && ((Integer)rowsValue).intValue() == 0)
            {   // number of rows is to be set to 0, we must preset
                // columns property to something else than 0
                try {
                    getProperty("columns").setValue(new Integer(1));
                }
                catch (Exception ex) {} // ignore
            }
        }

        super.readInitLayoutCode(layoutExp, initLayoutCode);
    }
}
