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
package org.netbeans.modules.vmd.midp.components.handlers;

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.flow.FlowSwitchDisplayableEventHandlerEdgePresenter;
import org.netbeans.modules.vmd.midp.flow.FlowSwitchDisplayableEventHandlerForwardEdgePresenter;
import org.netbeans.modules.vmd.midp.flow.FlowSwitchDisplayableEventHandlerPinPresenter;
import org.netbeans.modules.vmd.midp.general.AbstractEventHandlerCreatorPresenter;
import org.netbeans.modules.vmd.midp.codegen.SwitchDisplayableParameterPresenter;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class SwitchDisplayableEventHandlerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SwitchDisplayableEventHandler"); // NOI18N

    public static final String PROP_DISPLAYABLE = "displayable"; // NOI18N
    public static final String PROP_ALERT = "alert"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventHandlerCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor (PROP_DISPLAYABLE, DisplayableCD.TYPEID, PropertyValue.createNull (), false, false, MidpVersionable.MIDP),
            new PropertyDescriptor (PROP_ALERT, AlertCD.TYPEID, PropertyValue.createNull (), true, false, MidpVersionable.MIDP)
        );
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.create (EventHandlerSupport.getSwitchDisplayableEventHandlerInfoResolver ()),
            // flow
            new FlowSwitchDisplayableEventHandlerPinPresenter (),
            new FlowSwitchDisplayableEventHandlerEdgePresenter (),
            new FlowSwitchDisplayableEventHandlerForwardEdgePresenter (),
            // code
            new CodeMultiGuardedLevelPresenter() {
                protected void generateMultiGuardedSectionCode (MultiGuardedSection section) {
                    SwitchDisplayableParameterPresenter presenter;

                    DesignComponent alertComponent = getComponent ().readProperty (PROP_ALERT).getComponent ();
                    presenter = alertComponent != null ? alertComponent.getPresenter (SwitchDisplayableParameterPresenter.class) : null;
                    String alert = presenter != null ? presenter.generateSwitchDisplayableParameterCode () : CodeReferencePresenter.generateAccessCode (alertComponent);

                    DesignComponent displayableComponent = getComponent ().readProperty (PROP_DISPLAYABLE).getComponent ();
                    presenter = displayableComponent != null ? displayableComponent.getPresenter (SwitchDisplayableParameterPresenter.class) : null;
                    String displayable = presenter != null ? presenter.generateSwitchDisplayableParameterCode () : CodeReferencePresenter.generateAccessCode (displayableComponent);

                    section.getWriter ().write ("switchDisplayable (" + alert + ", " + displayable + ");\n"); // NOI18N
                }
            },
            // delete
            DeleteDependencyPresenter.createDependentOnPropertyPresenter (PROP_DISPLAYABLE),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_ALERT)
        );
    }

    public static AbstractEventHandlerCreatorPresenter createSwitchDisplayableEventHandlerCreatorPresenter () {
        return new AbstractEventHandlerCreatorPresenter() {
            public DesignComponent createReuseEventHandler (DesignComponent eventSource, DesignComponent currentEventHandler, DesignComponent targetComponent) {
                if (currentEventHandler == null || ! getComponent ().getDocument ().getDescriptorRegistry ().isInHierarchy (SwitchDisplayableEventHandlerCD.TYPEID, currentEventHandler.getType ()))
                    currentEventHandler = getComponent ().getDocument ().createComponent (SwitchDisplayableEventHandlerCD.TYPEID);
                currentEventHandler.writeProperty (PROP_ALERT, PropertyValue.createNull ());
                currentEventHandler.writeProperty (PROP_DISPLAYABLE, PropertyValue.createComponentReference (targetComponent));
                return currentEventHandler;
            }
        };
    }

    public static AbstractEventHandlerCreatorPresenter createSwitchAlertEventHandlerCreatorPresenter () {
        return new AbstractEventHandlerCreatorPresenter() {
            public DesignComponent createReuseEventHandler (DesignComponent eventSource, DesignComponent currentEventHandler, DesignComponent targetComponent) {
                DesignDocument document = getComponent ().getDocument ();
                DescriptorRegistry descriptorRegistry = document.getDescriptorRegistry ();
                if (currentEventHandler == null || ! descriptorRegistry.isInHierarchy (SwitchDisplayableEventHandlerCD.TYPEID, currentEventHandler.getType ()))
                    currentEventHandler = document.createComponent (SwitchDisplayableEventHandlerCD.TYPEID);

                DesignComponent possibleSourceDisplayable = eventSource.getParentComponent ();
                if (possibleSourceDisplayable != null  &&  descriptorRegistry.isInHierarchy (DisplayableCD.TYPEID, possibleSourceDisplayable.getType ())) {
                    currentEventHandler.writeProperty (PROP_ALERT, PropertyValue.createComponentReference (targetComponent));
                    if (currentEventHandler.readProperty (PROP_DISPLAYABLE).getComponent () == null)
                        currentEventHandler.writeProperty (PROP_DISPLAYABLE, PropertyValue.createComponentReference (possibleSourceDisplayable));
                } else {
                    currentEventHandler.writeProperty (PROP_ALERT, PropertyValue.createNull ());
                    currentEventHandler.writeProperty (PROP_DISPLAYABLE, PropertyValue.createComponentReference (targetComponent));
                }

                return currentEventHandler;
            }
        };
    }

}
