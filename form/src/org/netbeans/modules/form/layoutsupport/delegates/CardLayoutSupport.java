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

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * Support class for CardLayout. This support uses fictive layout constraints
 * for holding the names of the cards. It also implements the "arranging
 * features" - for the user to be able to choose the card in the form designer.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class CardLayoutSupport extends AbstractLayoutSupport {

    private CardConstraints currentCard;

    /** Gets the supported layout manager class - CardLayout.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return CardLayout.class;
    }

    /** Adds new components to the layout. This is done just at the metadata
     * level, no real components but their CodeExpression representations
     * are added.
     * @param compExpressions array of CodeExpression objects representing the
     *        components to be accepted
     * @param constraints array of layout constraints of the components
     */
    public void addComponents(CodeExpression[] newCompExpressions,
                              LayoutConstraints[] newConstraints)
    {
        // same functionality as in AbstractLayoutSupport...
        super.addComponents(newCompExpressions, newConstraints);

        // ...just set the last component as the active card
        int count = getComponentCount();
        if (currentCard == null && count > 0)
            currentCard = (CardConstraints) getConstraints(count - 1);
    }

    /** This method is called when a component is selected in Component
     * Inspector.
     * @param index position (index) of the selected component in container
     */
    public void selectComponent(int index) {
        // set the active card according to index
        LayoutConstraints constraints = getConstraints(index);
        if (constraints instanceof CardConstraints)
            currentCard = (CardConstraints) constraints;
    }

    /** In this method, the layout delegate has a chance to "arrange" real
     * container instance additionally - some other way that cannot be
     * done through layout properties and added components.
     * @param container instance of a real container to be arranged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     */
    public void arrangeContainer(Container container,
                                 Container containerDelegate)
    {
        LayoutManager lm = containerDelegate.getLayout();
        if (!(lm instanceof CardLayout) || currentCard == null)
            return;

        // select the active card in real CardLayout
        ((CardLayout)lm).show(containerDelegate,
                              (String)currentCard.getConstraintsObject());
    }

    /** This method should calculate position (index) for a component dragged
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
     * @param posInCont position of mouse in the container delegate; not needed
     * @param posInComp position of mouse in the dragged component; not needed
     * @return index corresponding to the position of the component in the
     *         container; we just return the number of components here - as the
     *         drag&drop does not have much sense for CardLayout
     */
    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(containerDelegate.getLayout() instanceof CardLayout))
            return -1;
        return containerDelegate.getComponentCount();
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged; not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for CardLayout
     * @param newIndex component's index position to be presented; not needed
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
        if (!(containerDelegate.getLayout() instanceof CardLayout))
            return false;

        Dimension sz = containerDelegate.getSize();
        Insets insets = containerDelegate.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;
        
        g.drawRect(0, 0, sz.width, sz.height);
        return true;
    }

    // ---------

    /** This method is called from readComponentCode method to read layout
     * constraints of a component from code. It is just a simple String for
     * CardLayout (the name of the card).
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
        CardConstraints constr = new CardConstraints("card"); // NOI18N
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
        if (!(constr instanceof CardConstraints))
            return null; // should not happen

        return getCodeStructure().createExpression(
                   FormCodeSupport.createOrigin(constr.getProperties()[0]));
    }

    /** This method is called to get a default component layout constraints
     * metaobject in case it is not provided (e.g. in addComponents method).
     * @return the default LayoutConstraints object for the supported layout
     */
    protected LayoutConstraints createDefaultConstraints() {
        return new CardConstraints("card"+(getComponentCount()+1)); // NOI18N
    }

    // ----------------

    /** LayoutConstraints implementation holding name of a card in CardLayout.
     */
    public static class CardConstraints implements LayoutConstraints {
        private String card;

        private Node.Property[] properties;

        public CardConstraints(String card) {
            this.card = card;
        }

        public Node.Property[] getProperties() {
            if (properties == null)
                properties = new Node.Property[] {
                    new FormProperty("CardConstraints cardName", // NOI18N
                                     String.class,
                                 getBundle().getString("PROP_cardName"), // NOI18N
                                 getBundle().getString("HINT_cardName")) { // NOI18N

                        public Object getTargetValue() {
                            return card;
                        }

                        public void setTargetValue(Object value) {
                            card = (String)value;
                        }
                    }
                };

            return properties;
        }

        public Object getConstraintsObject() {
            return card;
        }

        public LayoutConstraints cloneConstraints() {
            return new CardConstraints(card);
        }
    }
}
