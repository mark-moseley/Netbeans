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
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.undo.*;
import java.util.*;
import java.util.List;

import org.netbeans.modules.form.layoutsupport.*;

/**
 *
 * @author Tran Duc Trung
 */

class ComponentDragger
{
    private FormDesigner formDesigner;
    private HandleLayer handleLayer;
    private RADVisualComponent[] selectedComponents;
    private Point hotspot;
    private Point mousePosition;
    private int resizeType;

    private RADVisualContainer targetMetaContainer;
    private Container targetContainer;
    private Container targetContainerDel;

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = FormEditor.getFormSettings();


    ComponentDragger(FormDesigner formDesigner,
                     HandleLayer handleLayer,
                     RADVisualComponent[] selectedComponents,
                     Point hotspot,
                     int resizeType) {
        this.formDesigner = formDesigner;
        this.handleLayer = handleLayer;
        this.selectedComponents = selectedComponents;
        this.hotspot = hotspot;
        this.mousePosition = hotspot;
        this.resizeType = resizeType;
    }

    void mouseDragged(Point p) {
        mousePosition = p;
        handleLayer.repaint();
    }

    void paintDragFeedback(Graphics2D g) {
        Stroke oldStroke = g.getStroke();
        Stroke stroke = new BasicStroke((float) 2.0,
                                        BasicStroke.CAP_SQUARE,
                                        BasicStroke.JOIN_MITER,
                                        (float) 10.0,
                                        new float[] { (float) 1.0, (float) 4.0 },
                                        0 );
        g.setStroke(stroke);

        Color oldColor = g.getColor();
        g.setColor(formSettings.getSelectionBorderColor());

        List constraints = new ArrayList(selectedComponents.length);
        List indices = new ArrayList(selectedComponents.length);

        boolean constraintsOK = computeConstraints(mousePosition,
                                                   constraints, indices);

        Point contPos = null;
        LayoutSupportManager layoutSupport = null;
        if (constraintsOK) {
            contPos = SwingUtilities.convertPoint(targetContainerDel, 0, 0, handleLayer);
            layoutSupport = targetMetaContainer.getLayoutSupport();
            if (resizeType == 0)
                paintTargetContainerFeedback(g, targetContainerDel);
        }

        for (int i = 0; i < selectedComponents.length; i++) {
            RADVisualComponent metacomp = selectedComponents[i];
            boolean drawn = false;

            if (constraintsOK) {
                Component comp = (Component) formDesigner.getComponent(metacomp);
                LayoutConstraints constr = (LayoutConstraints) constraints.get(i);
                int index = ((Integer)indices.get(i)).intValue();

                if (constr != null || index >= 0) {
                    g.translate(contPos.x, contPos.y);
                    drawn = layoutSupport.paintDragFeedback(
                                targetContainer, targetContainerDel,
                                comp, constr, index, g);
                    g.translate(- contPos.x, - contPos.y);
                }
//                else continue;
            }

            if (!drawn)
                paintDragFeedback(g, metacomp);
        }

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    void dropComponents(Point point) {
        List constraints = new ArrayList(selectedComponents.length);
        List indices = new ArrayList(selectedComponents.length);

        if (!computeConstraints(point, constraints, indices))
            return;

        // adjust indices considering that some of dragged components
        // might be already in target container
        adjustIndices(indices);

        FormModel formModel = formDesigner.getModel();

        LayoutSupportManager layoutSupport =
                targetMetaContainer.getLayoutSupport();
        RADVisualComponent[] originalComponents =
                targetMetaContainer.getSubComponents();
        LayoutConstraints[] originalConstraints =
                new LayoutConstraints[originalComponents.length];

        List movedFromOutside = null; // components moved from other containers
        List movedWithinTarget = null; // components moved within the target container

        // collect components being dragged from other containers
        for (int i=0; i < selectedComponents.length; i++) {
            if (selectedComponents[i].getParentContainer() != targetMetaContainer) {
                if (movedFromOutside == null)
                    movedFromOutside = new ArrayList(selectedComponents.length);
                movedFromOutside.add(new Integer(i)); // remember index
            }
            else {
                if (movedWithinTarget == null)
                    movedWithinTarget = new ArrayList(selectedComponents.length);
                movedWithinTarget.add(selectedComponents[i]);
            }
        }

        // test whether target container accepts new components (dragged from
        // other containers)
        if (movedFromOutside != null && movedFromOutside.size() > 0) {
            int count = movedFromOutside.size();
            RADVisualComponent[] newComps = new RADVisualComponent[count];
            LayoutConstraints[] newConstr = new LayoutConstraints[count];

            for (int i=0; i < count; i++) {
                int index = ((Integer)movedFromOutside.get(i)).intValue();
                newComps[i] = selectedComponents[index];
                newConstr[i] = (LayoutConstraints) constraints.get(index);
            }

            try {
                layoutSupport.acceptNewComponents(newComps, newConstr);
            }
            catch (RuntimeException ex) {
                // layout support does not accept components
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
                return;
            }

            // layout support might adjust the constraints
            for (int i=0; i < count; i++) {
                int index = ((Integer)movedFromOutside.get(i)).intValue();
                constraints.set(index, newConstr[i]);
            }

            movedFromOutside.clear(); // for later use
        }

        // create list of components and constraints for target container
        int n = selectedComponents.length + originalComponents.length;
        List newComponents = new ArrayList(n);
        List newConstraints = new ArrayList(n);

        // fill enough empty space
        for (int i=0; i < n; i++) {
            newComponents.add(null);
            newConstraints.add(null);
        }

        // add dragged components requiring exact position (index)
        for (int i=0; i < selectedComponents.length; i++) {
            int index = ((Integer)indices.get(i)).intValue();
            if (index >= 0 && index < n && checkTarget(selectedComponents[i])) {
                while (newComponents.get(index) != null) // ensure free index
                    if (++index == n)
                        index = 0;

                newComponents.set(index, selectedComponents[i]);
//                newConstraints.set(index, constraints.get(i));
            }
        }

        int newI = 0;

        // add all components being already in the target container
        for (int i=0; i < originalComponents.length; i++) {
            RADVisualComponent metacomp = originalComponents[i];
            originalConstraints[i] = layoutSupport.getConstraints(metacomp);

            int index = newComponents.indexOf(metacomp);
            if (index < 0) { // not yet in newComponents
                while (newComponents.get(newI) != null) // ensure free index
                    newI++;

                newComponents.set(newI, metacomp);
                newConstraints.set(newI, originalConstraints[i]);
            }
        }

        // add the rest of dragged components (driven by constraints)
        for (int i=0; i < selectedComponents.length; i++) {
            RADVisualComponent metacomp = selectedComponents[i];
            int index = newComponents.indexOf(metacomp);
            if (index >= 0) { // already in newComponents
                newConstraints.set(index, constraints.get(i));
            }
            else if (checkTarget(metacomp)) { // not yet in newComponents
                while (newComponents.get(newI) != null) // ensure free index
                    newI++;

                newComponents.set(newI, metacomp);
                newConstraints.set(newI, constraints.get(i));
            }
        }
        // now we have lists of components and constraints in right order

        // start compound undoable edit
        boolean compoundUndoableEditStarted = formModel.isUndoRedoRecording()
                                              && formModel.startCompoundEdit();

        // remove components from source containers
        for (int i=0; i < n; i++) {
            RADVisualComponent metacomp = (RADVisualComponent) newComponents.get(i);
            if (metacomp != null) {
                RADVisualContainer parentCont = metacomp.getParentContainer();
                if (parentCont != targetMetaContainer) {
                    if (movedFromOutside == null)
                        movedFromOutside = new ArrayList(selectedComponents.length);
                    movedFromOutside.add(metacomp);

                    formModel.removeComponentFromContainer(metacomp);
                }

                if (newConstraints.get(i) == null)
                    newConstraints.set(i, parentCont.getLayoutSupport()
                                            .getStoredConstraints(metacomp));
            }
            else { // remove empty space
                newComponents.remove(i);
                newConstraints.remove(i);
                i--;
                n--;
            }
        }

        if (n == 0)
            return; // dragging not allowed

        // turn off undo/redo monitoring in FormModel as we provide our own
        // undoable edit for the rest of the operation
        boolean undoRedoOn = formModel.isUndoRedoRecording();
        if (undoRedoOn)
            formModel.setUndoRedoRecording(false);

        // clear the target layout
        layoutSupport.removeAll();

        // prepare arrays of components and constraints (from lists)
        RADVisualComponent[] newCompsArray = new RADVisualComponent[n];
        LayoutConstraints[] newConstrArray = new LayoutConstraints[n];
        for (int i=0; i < n; i++) {
            newCompsArray[i] = (RADVisualComponent) newComponents.get(i);
            newConstrArray[i] = (LayoutConstraints) newConstraints.get(i);
        }

        // and finally - add all new components to target container
        targetMetaContainer.initSubComponents(newCompsArray);
        layoutSupport.addComponents(newCompsArray, newConstrArray);

        // fire changes - adding new components
        RADVisualComponent[] compsMovedFromOutside;
        if (movedFromOutside != null) {
            n = movedFromOutside.size();
            compsMovedFromOutside = new RADVisualComponent[n];
            for (int i=0; i < n; i++) {
                compsMovedFromOutside[i] = (RADVisualComponent)
                                           movedFromOutside.get(i);
                formModel.fireComponentAdded(compsMovedFromOutside[i], false);
            }
        }
        else compsMovedFromOutside = new RADVisualComponent[0];

        // fire changes - changing components layout
        RADVisualComponent[] compsMovedWithinTarget;
        if (movedWithinTarget != null) {
            n = movedWithinTarget.size();
            compsMovedWithinTarget = new RADVisualComponent[n];
            for (int i=0; i < n; i++) {
                compsMovedWithinTarget[i] = (RADVisualComponent)
                                            movedWithinTarget.get(i);
                formModel.fireComponentLayoutChanged(compsMovedWithinTarget[i],
                                                     null, null, null);
            }
        }
        else compsMovedWithinTarget = new RADVisualComponent[0];

        // setup undoable edit
        if (compoundUndoableEditStarted) {
            DropUndoableEdit dropUndo = new DropUndoableEdit();
            dropUndo.targetContainer = targetMetaContainer;
            dropUndo.targetComponentsBeforeMove = originalComponents;
            dropUndo.targetConstraintsBeforeMove = originalConstraints;
            dropUndo.targetComponentsAfterMove = newCompsArray;
            dropUndo.targetConstraintsAfterMove = newConstrArray;
            dropUndo.componentsMovedFromOutside = compsMovedFromOutside;
            dropUndo.componentsMovedWithinTarget = compsMovedWithinTarget;

            formModel.addUndoableEdit(dropUndo);
        }

        // finish undoable edit
        if (undoRedoOn) // turn on undo/redo monitoring again
            formModel.setUndoRedoRecording(true);
        if (compoundUndoableEditStarted)
            formModel.endCompoundEdit();

        // select dropped components in designer
        if (formDesigner.getSelectedComponents().size() == 0) {
            for (int i=0; i < selectedComponents.length; i++)
                formDesigner.addComponentToSelection(selectedComponents[i]);
        }
    }

    // ------------

    // moves components to/within target container
    private void moveComponents(RADVisualComponent[] components,
                                LayoutConstraints[] constraints)
    {
        // remember current layout configuration of target container

        // start compound undoable edit

        // remove components comming from other containers from their
        // containers, remember stored constraints

        // turn off undo/redo monitoring in FormModel as we provide our own
        // undoable edit for second part of the operation

        // clear layout of target container
    }

    private boolean computeConstraints(Point p, List constraints, List indices) {
        if (selectedComponents == null || selectedComponents.length == 0)
            return false;

        targetMetaContainer = resizeType == 0 ?
                                handleLayer.getMetaContainerAt(p) :
                                selectedComponents[0].getParentContainer();
        if (targetMetaContainer == null)
            return false; // unknown meta-container

        RADVisualContainer fixTargetContainer = null;
        do {
            if (fixTargetContainer != null) {
                targetMetaContainer = fixTargetContainer;
                fixTargetContainer = null;
            }

            LayoutSupportManager layoutSupport = targetMetaContainer.getLayoutSupport();
//            if (layoutSupport == null)
//                return false; // no LayoutSupport (should not happen)

            targetContainer = (Container) formDesigner.getComponent(targetMetaContainer);
            if (targetContainer == null)
                return false; // container not in designer (should not happen)

            targetContainerDel = targetMetaContainer.getContainerDelegate(targetContainer);
            if (targetContainerDel == null)
                return false; // no container delegate (should not happen)

            Point posInCont = SwingUtilities.convertPoint(handleLayer, p, targetContainerDel);

            for (int i = 0; i < selectedComponents.length; i++) {
                LayoutConstraints constr = null;
                int index = -1;

                RADVisualComponent metacomp = selectedComponents[i];
                Component comp = (Component) formDesigner.getComponent(metacomp);

                if (comp != null) {
                    if (!checkTarget(metacomp)) {
                        fixTargetContainer = metacomp.getParentContainer();
                        constraints.clear();
                        indices.clear();
                        if (fixTargetContainer == null)
                            return false; // should not happen
                        break;
                    }

                    if (resizeType == 0) { // dragging
                        Point posInComp = SwingUtilities.convertPoint(
                                              handleLayer, hotspot, comp);
                        index = layoutSupport.getNewIndex(
                                    targetContainer, targetContainerDel,
                                    comp, metacomp.getComponentIndex(),
                                    posInCont, posInComp);
                        constr = layoutSupport.getNewConstraints(
                                    targetContainer, targetContainerDel,
                                    comp, metacomp.getComponentIndex(),
                                    posInCont, posInComp);
                    }
                    else { // resizing
                        int up = 0, down = 0, left = 0, right = 0;

                        if ((resizeType & LayoutSupportManager.RESIZE_DOWN) != 0)
                            down = p.y - hotspot.y;
                        else if ((resizeType & LayoutSupportManager.RESIZE_UP) != 0)
                            up = hotspot.y - p.y;
                        if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0)
                            right = p.x - hotspot.x;
                        else if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0)
                            left = hotspot.x - p.x;

                        Insets sizeChanges = new Insets(up, left, down, right);
                        constr = layoutSupport.getResizedConstraints(
                                     comp, metacomp.getComponentIndex(),
                                     sizeChanges);
                    }
                }

                constraints.add(constr);
                indices.add(new Integer(index));
            }
        }
        while (fixTargetContainer != null);

        return true;
    }

    /** Checks whether metacomp is not a parent of target container (or the
     * target container itself). Used to avoid dragging to a sub-tree.
     * @return true if metacomp is OK
     */
    private boolean checkTarget(RADVisualComponent metacomp) {
        if (!(metacomp instanceof RADVisualContainer))
            return true;

        RADComponent targetCont = targetMetaContainer;
        while (targetCont != null) {
            if (targetCont == metacomp)
                return false;
            targetCont = targetCont.getParentComponent();
        }

        return true;
    }

    /** Modifies suggested indices of dragged components considering the fact
     * that some of the components might be in the target container.
     */
    private void adjustIndices(List indices) {
        int index;
        int correction;
        int prevIndex = -1;
        int prevCorrection = 0;

        for (int i=0; i < indices.size(); i++) {
            index = ((Integer)indices.get(i)).intValue();
            if (index >= 0) {
                if (index == prevIndex) {
                    correction = prevCorrection;
                }
                else {
                    correction = 0;
                    RADVisualComponent[] targetComps =
                                         targetMetaContainer.getSubComponents();
                    for (int j=0; j < index; j++) {
                        RADVisualComponent tComp = targetComps[j];
                        boolean isSelected = false;
                        for (int k=0; k < selectedComponents.length; k++)
                            if (tComp == selectedComponents[k]) {
                                isSelected = true;
                                break;
                            }

                        if (isSelected)
                            correction++;
                    }
                    prevIndex = index;
                    prevCorrection = correction;
                }

                if (correction != 0) {
                    index -= correction;
                    indices.set(i, new Integer(index));
                }
            }
        }
    }

    private void paintDragFeedback(Graphics2D g, RADVisualComponent metacomp) {
        Object comp = formDesigner.getComponent(metacomp);
        if (!(comp instanceof Component) || !((Component)comp).isShowing())
            return;

        Component component = (Component) comp;

        Rectangle rect = component.getBounds();
        rect = SwingUtilities.convertRectangle(component.getParent(),
                                               rect,
                                               handleLayer);

        rect.translate(mousePosition.x - hotspot.x,
                       mousePosition.y - hotspot.y);
        
        g.draw(new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height));

        if (metacomp instanceof RADVisualContainer) {
            RADVisualComponent[] children =
                ((RADVisualContainer)metacomp).getSubComponents();
            for (int i = 0; i < children.length; i++) {
                paintDragFeedback(g, children[i]);
            }
        }
    }

    private void paintTargetContainerFeedback(Graphics2D g, Container cont) {
        Stroke oldStroke = g.getStroke();
        Stroke stroke = new BasicStroke((float) 2.0,
                                        BasicStroke.CAP_SQUARE,
                                        BasicStroke.JOIN_MITER,
                                        (float) 10.0,
                                        new float[] { (float) 2.0, (float) 8.0 },
                                        0 );
        g.setStroke(stroke);

        Color oldColor = g.getColor();
        g.setColor(formSettings.getDragBorderColor());
        
        Rectangle rect = new Rectangle(new Point(0,0), cont.getSize());
        rect = SwingUtilities.convertRectangle(cont,
                                               rect,
                                               handleLayer);
        g.draw(new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height));
        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    // ---------

    private static class DropUndoableEdit extends AbstractUndoableEdit {

        RADVisualContainer targetContainer;
        RADVisualComponent[] targetComponentsBeforeMove;
        LayoutConstraints[] targetConstraintsBeforeMove;
        RADVisualComponent[] targetComponentsAfterMove;
        LayoutConstraints[] targetConstraintsAfterMove;
        RADVisualComponent[] componentsMovedFromOutside;
        RADVisualComponent[] componentsMovedWithinTarget;

        public void undo() throws CannotUndoException {
            super.undo();

            FormModel formModel = targetContainer.getFormModel();

            // turn off undo/redo monitoring in FormModel while undoing!
            boolean undoRedoOn = formModel.isUndoRedoRecording();
            if (undoRedoOn)
                formModel.setUndoRedoRecording(false);

            for (int i=0; i < componentsMovedFromOutside.length; i++)
                formModel.removeComponentFromContainer(
                                         componentsMovedFromOutside[i]);

            LayoutSupportManager layoutSupport =
                targetContainer.getLayoutSupport();
            layoutSupport.removeAll();
            targetContainer.initSubComponents(targetComponentsBeforeMove);
            layoutSupport.addComponents(targetComponentsBeforeMove,
                                        targetConstraintsBeforeMove);

            for (int i=0; i < componentsMovedWithinTarget.length; i++)
                formModel.fireComponentLayoutChanged(
                    componentsMovedWithinTarget[i], null, null, null);

            if (undoRedoOn) // turn on undo/redo monitoring again
                formModel.setUndoRedoRecording(true);
        }

        public void redo() throws CannotRedoException {
            super.redo();

            FormModel formModel = targetContainer.getFormModel();

            // turn off undo/redo monitoring in FormModel while redoing!
            boolean undoRedoOn = formModel.isUndoRedoRecording();
            if (undoRedoOn)
                formModel.setUndoRedoRecording(false);

            LayoutSupportManager layoutSupport =
                targetContainer.getLayoutSupport();
            layoutSupport.removeAll();
            targetContainer.initSubComponents(targetComponentsAfterMove);
            layoutSupport.addComponents(targetComponentsAfterMove,
                                        targetConstraintsAfterMove);
            
            for (int i=0; i < componentsMovedFromOutside.length; i++)
                formModel.fireComponentAdded(componentsMovedFromOutside[i],
                                             false);

            for (int i=0; i < componentsMovedWithinTarget.length; i++)
                formModel.fireComponentLayoutChanged(
                    componentsMovedWithinTarget[i], null, null, null);

            if (undoRedoOn) // turn on undo/redo monitoring again
                formModel.setUndoRedoRecording(true);
        }

        public String getUndoPresentationName() {
            return ""; // NOI18N
        }
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }
    }
}
