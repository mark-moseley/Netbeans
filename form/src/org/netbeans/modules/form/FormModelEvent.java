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

import java.util.*;
import javax.swing.undo.*;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;

/**
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class FormModelEvent extends EventObject
{
    public static final int FORM_LOADED = 1;
    public static final int FORM_TO_BE_SAVED = 2;
    public static final int FORM_TO_BE_CLOSED = 3;
    public static final int CONTAINER_LAYOUT_EXCHANGED = 4;
    public static final int CONTAINER_LAYOUT_CHANGED = 5;
    public static final int COMPONENT_LAYOUT_CHANGED = 6;
    public static final int COMPONENT_ADDED = 7;
    public static final int COMPONENT_REMOVED = 8;
    public static final int COMPONENTS_REORDERED = 9;
    public static final int COMPONENT_PROPERTY_CHANGED = 10;
    public static final int SYNTHETIC_PROPERTY_CHANGED = 11;
    public static final int EVENT_HANDLER_ADDED = 12;
    public static final int EVENT_HANDLER_REMOVED = 13;
    public static final int EVENT_HANDLER_RENAMED = 14;
    public static final int OTHER_CHANGE = 15;

    private boolean createdDeleted;
    private RADComponent component;
    private ComponentContainer container;
    private LayoutConstraints constraints;
    private int componentIndex = -1;
    private int[] reordering;
    private Object codeUndoRedoStart;
    private Object codeUndoRedoEnd;
    private String propertyName;
    private Object oldPropertyValue;
    private Object newPropertyValue;
    private Event componentEvent;

    private int changeType;

    private UndoableEdit undoableEdit;

    // -----------

    private FormModelEvent additionalEvent; 
    private static List interestList; // events interested in additional events

    // -----------

    FormModelEvent(FormModel source, int changeType) {
        super(source);
        this.changeType = changeType;
        informInterestedEvents(this);
    }

    void setProperty(String propName, Object oldValue, Object newValue) {
        propertyName = propName;
        oldPropertyValue = oldValue;
        newPropertyValue = newValue;
    }

    void setComponentAndContainer(RADComponent metacomp,
                                  ComponentContainer metacont)
    {
        component = metacomp;
        container = metacont != null ? metacont : deriveContainer(metacomp);
    }

    void setLayout(RADVisualContainer metacont,
                   LayoutSupportDelegate oldLayoutSupp,
                   LayoutSupportDelegate newLayoutSupp)
    {
        component = metacont;
        container = metacont;
        oldPropertyValue = oldLayoutSupp;
        newPropertyValue = newLayoutSupp;
    }

    void setReordering(int[] perm) {
        reordering = perm;
    }

    void setAddData(RADComponent metacomp,
                    ComponentContainer metacont,
                    boolean addedNew)
    {
        setComponentAndContainer(metacomp, metacont);
        createdDeleted = addedNew;

        if (component instanceof RADVisualComponent
            && container instanceof RADVisualContainer)
        {
            componentIndex = container.getIndexOf(component);
            if (componentIndex >= 0) {
                LayoutSupportManager laysup =
                    ((RADVisualContainer)container).getLayoutSupport();
                constraints = laysup.getConstraints(componentIndex);
            }
        }
    }

    void setRemoveData(RADComponent metacomp,
                       ComponentContainer metacont,
                       int index,
                       boolean removedFromModel,
                       Object codeStructureMark1,
                       Object codeStructureMark2)
    {
        component = metacomp;
        container = metacont;
        componentIndex = index;
        codeUndoRedoStart = codeStructureMark1;
        codeUndoRedoEnd = codeStructureMark2;
        createdDeleted = removedFromModel;

        if (metacomp instanceof RADVisualComponent
            && metacont instanceof RADVisualContainer)
        {
            LayoutSupportManager laysup =
                ((RADVisualContainer)metacont).getLayoutSupport();
            constraints =
                laysup.getStoredConstraints((RADVisualComponent)metacomp);
        }
    }

    void setEvent(Event event, // may be null if the handler is just updated
                  EventHandler handler,
                  String bodyText,
                  boolean createdNew)
    {
        if (event != null)
            component = event.getComponent();
        componentEvent = event;
        propertyName = handler.getName();
        newPropertyValue = bodyText;
        createdDeleted = createdNew;
    }

    void setEvent(EventHandler handler, String oldName) {
        propertyName = handler.getName();
        oldPropertyValue = oldName;
        newPropertyValue = handler.getName();
    }

    void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    private static ComponentContainer deriveContainer(RADComponent comp) {
        if (comp == null)
            return null;
        if (comp.getParentComponent() instanceof ComponentContainer)
            return (ComponentContainer) comp.getParentComponent();
        else if (comp.getParentComponent() == null)
            return comp.getFormModel().getModelContainer();
        return null;
    }

    // -------

    public final FormModel getFormModel() {
        return (FormModel) getSource();
    }

    public final int getChangeType() {
        return changeType;
    }

    public final boolean isModifying() {
        return changeType != FORM_LOADED
               && changeType != FORM_TO_BE_SAVED
               && changeType != FORM_TO_BE_CLOSED
               && (changeType != EVENT_HANDLER_ADDED || componentEvent != null);
    }

    public final boolean getCreatedDeleted() {
        return createdDeleted;
    }

    public final ComponentContainer getContainer() {
        return container;
    }

    public final RADComponent getComponent() {
        return component;
    }

    public final LayoutConstraints getComponentLayoutConstraints() {
        return constraints;
    }

    public final int getComponentIndex() {
        return componentIndex;
    }

    public final String getPropertyName() {
        return propertyName;
    }

    public final RADProperty getComponentProperty() {
        if (component == null || propertyName == null)
            return null;

        FormProperty prop = component.getPropertyByName(propertyName);
        return prop instanceof RADProperty ? (RADProperty) prop : null;
    }

    public final Object getOldPropertyValue() {
        return oldPropertyValue instanceof FormProperty.ValueWithEditor ?
                 ((FormProperty.ValueWithEditor)oldPropertyValue).getValue() :
                 oldPropertyValue;
    }

    public final Object getNewPropertyValue() {
        return newPropertyValue instanceof FormProperty.ValueWithEditor ?
                 ((FormProperty.ValueWithEditor)newPropertyValue).getValue() :
                 newPropertyValue;
    }

    public final LayoutSupportDelegate getOldLayoutSupport() {
        return (LayoutSupportDelegate) oldPropertyValue;
    }

    public final LayoutSupportDelegate getNewLayoutSupport() {
        return (LayoutSupportDelegate) newPropertyValue;
    }

    public final int[] getReordering() {
        return reordering;
    }

    public final Event getComponentEvent() {
        return componentEvent;
    }

    public final EventHandler getEventHandler() {
        return getFormModel().getFormEventHandlers().getEventHandler(propertyName);
    }

    public final String getEventHandlerName() {
        return propertyName;
    }

    public final String getOldEventHandlerName() {
        return (String) oldPropertyValue;
    }

    public final String getNewEventHandlerName() {
        return (String) newPropertyValue;
    }

    public final String getNewEventHandlerContent() {
        return changeType == EVENT_HANDLER_ADDED
                   || changeType == EVENT_HANDLER_REMOVED ?
               (String) newPropertyValue : null;
    }

//    public final void setNewEventHandlerContent(String text) {
//        if (changeType == EVENT_HANDLER_ADDED
//                || changeType == EVENT_HANDLER_REMOVED)
//            newPropertyValue = text;
//    }

    public final String getOldEventHandlerContent() {
        if (changeType == EVENT_HANDLER_ADDED
            || changeType == EVENT_HANDLER_REMOVED)
        {
            if (additionalEvent != null) {
                if (additionalEvent.changeType == EVENT_HANDLER_REMOVED
                    || additionalEvent.changeType == EVENT_HANDLER_ADDED)
                {
                    oldPropertyValue = additionalEvent.oldPropertyValue;
                }
                additionalEvent = null;
            }
            return (String) oldPropertyValue;
        }
        return null;
    }

    public final void setOldEventHandlerContent(String text) {
        if (changeType == EVENT_HANDLER_ADDED
                || changeType == EVENT_HANDLER_REMOVED)
            oldPropertyValue = text;
    }

    // ----------

    UndoableEdit getUndoableEdit() {
        if (undoableEdit == null)
            undoableEdit = new FormUndoableEdit();
        return undoableEdit;
    }

    // ----------
    // methods for events interested in additional events occured
    // (used for undo/redo processing of event handlers)

    private static void addToInterestList(FormModelEvent ev) {
        if (interestList == null)
            interestList = new ArrayList();
        else
            interestList.remove(ev);

        interestList.add(ev);
    }

    private static void removeFromInterestList(FormModelEvent ev) {
        if (interestList != null)
            interestList.remove(ev);
    }

    private static void informInterestedEvents(FormModelEvent newEvent) {
        if (interestList != null)
            for (Iterator it=interestList.iterator(); it.hasNext(); )
                ((FormModelEvent)it.next()).newEventCreated(newEvent);
    }

    private void newEventCreated(FormModelEvent newEvent) {
        additionalEvent = newEvent;
    }

    // ----------

    private class FormUndoableEdit extends AbstractUndoableEdit {
        public void undo() throws CannotUndoException {
            super.undo();

            // turn off undo/redo monitoring in FormModel while undoing!
            boolean undoRedoOn = getFormModel().isUndoRedoRecording();
            if (undoRedoOn)
                getFormModel().setUndoRedoRecording(false);

            switch(changeType) {
                case CONTAINER_LAYOUT_EXCHANGED:
                    FormModel.t("UNDO: container layout change"); // NOI18N
                    undoContainerLayoutExchange();
                    break;
                case CONTAINER_LAYOUT_CHANGED:
                    FormModel.t("UNDO: container layout property change"); // NOI18N
                    undoContainerLayoutChange();
                    break;
                case COMPONENT_LAYOUT_CHANGED:
                    FormModel.t("UNDO: component layout constraints change"); // NOI18N
                    undoComponentLayoutChange();
                    break;
                case COMPONENTS_REORDERED:
                    FormModel.t("UNDO: components reorder"); // NOI18N
                    undoComponentsReorder();
                    break;
                case COMPONENT_ADDED:
                    FormModel.t("UNDO: component addition"); // NOI18N
                    undoComponentAddition();
                    break;
                case COMPONENT_REMOVED:
                    FormModel.t("UNDO: component removal"); // NOI18N
                    undoComponentRemoval();
                    break;
                case COMPONENT_PROPERTY_CHANGED:
                    FormModel.t("UNDO: component property change"); // NOI18N
                    undoComponentPropertyChange();
                    break;
                case SYNTHETIC_PROPERTY_CHANGED:
                    FormModel.t("UNDO: synthetic property change"); // NOI18N
                    undoSyntheticPropertyChange();
                    break;
                case EVENT_HANDLER_ADDED:
                    FormModel.t("UNDO: event handler addition"); // NOI18N
                    undoEventHandlerAddition();
                    break;
                case EVENT_HANDLER_REMOVED:
                    FormModel.t("UNDO: event handler removal"); // NOI18N
                    undoEventHandlerRemoval();
                    break;
                case EVENT_HANDLER_RENAMED:
                    FormModel.t("UNDO: event handler renaming"); // NOI18N
                    undoEventHandlerRenaming();
                    break;

                default: FormModel.t("UNDO: "+changeType); // NOI18N
                         break;
            }

            if (undoRedoOn) // turn on undo/redo monitoring again
                getFormModel().setUndoRedoRecording(true);
        }

        public void redo() throws CannotRedoException {
            super.redo();

            // turn off undo/redo monitoring in FormModel while redoing!
            boolean undoRedoOn = getFormModel().isUndoRedoRecording();
            if (undoRedoOn)
                getFormModel().setUndoRedoRecording(false);

            switch(changeType) {
                case CONTAINER_LAYOUT_EXCHANGED:
                    FormModel.t("REDO: container layout change"); // NOI18N
                    redoContainerLayoutExchange();
                    break;
                case CONTAINER_LAYOUT_CHANGED:
                    FormModel.t("REDO: container layout property change"); // NOI18N
                    redoContainerLayoutChange();
                    break;
                case COMPONENT_LAYOUT_CHANGED:
                    FormModel.t("REDO: component layout constraints change"); // NOI18N
                    redoComponentLayoutChange();
                    break;
                case COMPONENTS_REORDERED:
                    FormModel.t("REDO: components reorder"); // NOI18N
                    redoComponentsReorder();
                    break;
                case COMPONENT_ADDED:
                    FormModel.t("REDO: component addition"); // NOI18N
                    redoComponentAddition();
                    break;
                case COMPONENT_REMOVED:
                    FormModel.t("REDO: component removal"); // NOI18N
                    redoComponentRemoval();
                    break;
                case COMPONENT_PROPERTY_CHANGED:
                    FormModel.t("REDO: component property change"); // NOI18N
                    redoComponentPropertyChange();
                    break;
                case SYNTHETIC_PROPERTY_CHANGED:
                    FormModel.t("REDO: synthetic property change"); // NOI18N
                    redoSyntheticPropertyChange();
                    break;
                case EVENT_HANDLER_ADDED:
                    FormModel.t("REDO: event handler addition"); // NOI18N
                    redoEventHandlerAddition();
                    break;
                case EVENT_HANDLER_REMOVED:
                    FormModel.t("REDO: event handler removal"); // NOI18N
                    redoEventHandlerRemoval();
                    break;
                case EVENT_HANDLER_RENAMED:
                    FormModel.t("REDO: event handler renaming"); // NOI18N
                    redoEventHandlerRenaming();
                    break;

                default: FormModel.t("REDO: "+changeType); // NOI18N
                         break;
            }

            if (undoRedoOn) // turn on undo/redo monitoring again
                getFormModel().setUndoRedoRecording(true);
        }

        public String getUndoPresentationName() {
            return ""; // NOI18N
        }
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }

        public void die() {
            // it's very important to release undo changes from CodeStructure
            if (codeUndoRedoStart != null && codeUndoRedoEnd != null)
                getFormModel().getCodeStructure().releaseUndoableChanges(
                                       codeUndoRedoStart, codeUndoRedoEnd);
        }

        // -------------

        private void undoContainerLayoutExchange() {
            try {
                getFormModel().setContainerLayout(
                    (RADVisualContainer) getContainer(),
                    getOldLayoutSupport(),
                    null);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void redoContainerLayoutExchange() {
            try {
                getFormModel().setContainerLayout(
                    (RADVisualContainer)getContainer(),
                    getOldLayoutSupport(),
                    null);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void undoContainerLayoutChange() {
            LayoutSupportManager laysup =
                getComponent() instanceof RADVisualContainer ?
                    ((RADVisualContainer)getComponent()).getLayoutSupport() : null;
            if (laysup != null) {
                Node.Property prop = laysup.getLayoutProperty(getPropertyName());
                if (prop != null)
                    try {
                        prop.setValue(getOldPropertyValue());
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
            }
        }

        private void redoContainerLayoutChange() {
            LayoutSupportManager laysup =
                getComponent() instanceof RADVisualContainer ?
                    ((RADVisualContainer)getComponent()).getLayoutSupport() : null;
            if (laysup != null) {
                Node.Property prop = laysup.getLayoutProperty(getPropertyName());
                if (prop != null)
                    try {
                        prop.setValue(getNewPropertyValue());
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
            }
        }

        private void undoComponentLayoutChange() {
            if (getComponent() instanceof RADVisualComponent) {
                ((RADVisualComponent)getComponent()).getConstraintsProperties();
                FormProperty prop =
                    getComponent().getPropertyByName(getPropertyName());
                if (prop != null)
                    try {
                        prop.setValue(getOldPropertyValue());
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
            }
        }

        private void redoComponentLayoutChange() {
            if (getComponent() instanceof RADVisualComponent) {
                ((RADVisualComponent)getComponent()).getConstraintsProperties();
                FormProperty prop =
                    getComponent().getPropertyByName(getPropertyName());
                if (prop != null)
                    try {
                        prop.setValue(getNewPropertyValue());
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
            }
        }

        private void undoComponentAddition() {
            redoComponentRemoval();
        }

        private void redoComponentAddition() {
            undoComponentRemoval();
        }

        private void undoComponentRemoval() {
            if (codeUndoRedoStart != null // is null when called from redoComponentAddition()
                && !getFormModel().getCodeStructure().undoToMark(
                                               codeUndoRedoStart))
                return;

            RADComponent[] currentSubComps = getContainer().getSubBeans();
            RADComponent[] undoneSubComps =
                new RADComponent[currentSubComps.length+1];

            if (componentIndex < 0)
                componentIndex = currentSubComps.length;

            for (int i=0,j=0; j < undoneSubComps.length; i++,j++) {
                if (i == componentIndex) {
                    undoneSubComps[j] = getComponent();
                    if (i == currentSubComps.length)
                        break;
                    j++;
                }
                undoneSubComps[j] = currentSubComps[i];
            }

            getContainer().initSubComponents(undoneSubComps);

            if (getContainer() instanceof RADVisualContainer
                && getComponent() instanceof RADVisualComponent)
            {
                LayoutSupportManager layoutSupport =
                    ((RADVisualContainer)getContainer()).getLayoutSupport();
                layoutSupport.addComponents(
                    new RADVisualComponent[] { (RADVisualComponent)getComponent() },
                    new LayoutConstraints[] { getComponentLayoutConstraints() },
                    componentIndex);
            }

            if (getCreatedDeleted())
                FormModel.setInModelRecursively(getComponent(), true);

            getFormModel().fireComponentAdded(getComponent(), getCreatedDeleted());
        }

        private void redoComponentRemoval() {
            if (getCreatedDeleted())
                getFormModel().removeComponent(getComponent());
            else
                getFormModel().removeComponentFromContainer(getComponent());

            if (codeUndoRedoEnd != null)
                getFormModel().getCodeStructure().redoToMark(codeUndoRedoEnd);
        }

        private void undoComponentsReorder() {
            if (getContainer() != null && reordering != null) {
                int[] revPerm = new int[reordering.length];
                for (int i=0; i < reordering.length; i++)
                    revPerm[reordering[i]] = i;

                getContainer().reorderSubComponents(revPerm);
                getFormModel().fireComponentsReordered(getContainer(), revPerm);
            }
        }

        private void redoComponentsReorder() {
            if (getContainer() != null && reordering != null) {
                getContainer().reorderSubComponents(reordering);
                getFormModel().fireComponentsReordered(getContainer(),
                                                       reordering);
            }
        }

        private void undoComponentPropertyChange() {
            FormProperty prop =
                getComponent().getPropertyByName(getPropertyName());
            if (prop != null)
                try {
                    prop.setValue(getOldPropertyValue());
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                }
        }

        private void redoComponentPropertyChange() {
            FormProperty prop =
                getComponent().getPropertyByName(getPropertyName());
            if (prop != null)
                try {
                    prop.setValue(getNewPropertyValue());
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                }
        }

        private void undoSyntheticPropertyChange() {
            String propName = getPropertyName();
            if (propName.startsWith(RADProperty.SYNTH_PREFIX)) {
                // special case - pre/post init code of a property
                if (propName.startsWith(RADProperty.SYNTH_PRE_CODE)) {
                    FormProperty prop = getComponent().getPropertyByName(
                        propName.substring(RADProperty.SYNTH_PRE_CODE.length()));
                    prop.setPreCode((String)getOldPropertyValue());
                }
                else if (propName.startsWith(RADProperty.SYNTH_POST_CODE)) {
                    FormProperty prop = getComponent().getPropertyByName(
                        propName.substring(RADProperty.SYNTH_POST_CODE.length()));
                    prop.setPostCode((String)getOldPropertyValue());
                }
            }
            else { // component synthetic property
                Node.Property[] props = getComponent().getSyntheticProperties();
                for (int i=0; i < props.length; i++) {
                    if (props[i].getName().equals(propName)) {
                        try {
                            props[i].setValue(getOldPropertyValue());
                        }
                        catch (Exception ex) { // should not happen
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }

        private void redoSyntheticPropertyChange() {
            String propName = getPropertyName();
            if (propName.startsWith(RADProperty.SYNTH_PREFIX)) {
                // special case - pre/post init code of a property
                if (propName.startsWith(RADProperty.SYNTH_PRE_CODE)) {
                    FormProperty prop = getComponent().getPropertyByName(
                        propName.substring(RADProperty.SYNTH_PRE_CODE.length()));
                    prop.setPreCode((String)getNewPropertyValue());
                }
                else if (propName.startsWith(RADProperty.SYNTH_POST_CODE)) {
                    FormProperty prop = getComponent().getPropertyByName(
                        propName.substring(RADProperty.SYNTH_POST_CODE.length()));
                    prop.setPostCode((String)getNewPropertyValue());
                }
            }
            else { // component synthetic property
                Node.Property[] props = getComponent().getSyntheticProperties();
                for (int i=0; i < props.length; i++) {
                    if (props[i].getName().equals(propName)) {
                        try {
                            props[i].setValue(getNewPropertyValue());
                        }
                        catch (Exception ex) { // should not happen
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }

        private void undoEventHandlerAddition() {
            if (getComponentEvent() == null)
                return;

            addToInterestList(FormModelEvent.this);

            getFormModel().getFormEventHandlers()
                .removeEventHandler(getComponentEvent(),
                                    getEventHandlerName());

            removeFromInterestList(FormModelEvent.this);

            // fire property change on node explicitly to update event in
            // Component Inspector
            getComponent().getNodeReference().firePropertyChangeHelper(
                FormEditor.EVENT_PREFIX + getComponentEvent().getName(), null, null);
        }

        private void redoEventHandlerAddition() {
            if (getComponentEvent() == null)
                return;

            getFormModel().getFormEventHandlers()
                .addEventHandler(getComponentEvent(),
                                 getEventHandlerName(),
                                 getOldEventHandlerContent()); //handlerText

            // fire property change on node explicitly to update event in
            // Component Inspector
            getComponent().getNodeReference().firePropertyChangeHelper(
                FormEditor.EVENT_PREFIX + getComponentEvent().getName(), null, null);
        }

        private void undoEventHandlerRemoval() {
            if (getComponentEvent() == null)
                return;

            getFormModel().getFormEventHandlers()
                .addEventHandler(getComponentEvent(),
                                 getEventHandlerName(),
                                 getOldEventHandlerContent());

            // fire property change on node explicitly to update event in
            // Component Inspector
            if (getComponent().getNodeReference() != null)
                getComponent().getNodeReference().firePropertyChangeHelper(
                    FormEditor.EVENT_PREFIX + getComponentEvent().getName(),
                    null, null);
        }

        private void redoEventHandlerRemoval() {
            if (getComponentEvent() == null)
                return;

            addToInterestList(FormModelEvent.this);

            getFormModel().getFormEventHandlers()
                .removeEventHandler(getComponentEvent(),
                                    getEventHandlerName());

            removeFromInterestList(FormModelEvent.this);

            // fire property change on node explicitly to update event in
            // Component Inspector
            getComponent().getNodeReference().firePropertyChangeHelper(
                FormEditor.EVENT_PREFIX + getComponentEvent().getName(), null, null);
        }

        private void undoEventHandlerRenaming() {
            FormEventHandlers formHandlers = getFormModel().getFormEventHandlers();
            formHandlers.renameEventHandler(getNewEventHandlerName(),
                                            getOldEventHandlerName());

            // fire property change on nodes explicitly to update events in
            // Component Inspector
            java.util.Iterator events =
                formHandlers.getEventHandler(getOldEventHandlerName())
                    .getAttachedEvents().iterator();
            while (events.hasNext()) {
                Event event = (Event) events.next();
                event.getComponent().getNodeReference()
                    .firePropertyChangeHelper(
                        FormEditor.EVENT_PREFIX + event.getName(), null, null);
            }
        }

        private void redoEventHandlerRenaming() {
            FormEventHandlers formHandlers = getFormModel().getFormEventHandlers();
            formHandlers.renameEventHandler(getOldEventHandlerName(),
                                            getNewEventHandlerName());

            // fire property change on nodes explicitly to update events in
            // Component Inspector
            java.util.Iterator events =
                formHandlers.getEventHandler(getNewEventHandlerName())
                    .getAttachedEvents().iterator();
            while (events.hasNext()) {
                Event event = (Event) events.next();
                event.getComponent().getNodeReference()
                    .firePropertyChangeHelper(
                        FormEditor.EVENT_PREFIX + event.getName(), null, null);
            }
        }
    }
}
