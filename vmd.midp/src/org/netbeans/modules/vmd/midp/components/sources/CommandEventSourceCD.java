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
package org.netbeans.modules.vmd.midp.components.sources;

import java.util.ArrayList;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenterForwarder;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.flow.FlowDisplayableCommandPinOrderPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class CommandEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#CommandEventSource"); // NOI18N

    // HINT this is redunance property, could be resolved from parent
    public static final String PROP_DISPLAYABLE = "displayable"; // NOI18N
    public static final String PROP_COMMAND = "command"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, CommandEventSourceCD.TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor (CommandEventSourceCD.PROP_DISPLAYABLE, DisplayableCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER),
            new PropertyDescriptor (CommandEventSourceCD.PROP_COMMAND, CommandCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER)
        );
    }

    public static DesignComponent getCommandComponent (DesignComponent commandEventSourceComponent) {
        return commandEventSourceComponent.readProperty (PROP_COMMAND).getComponent ();
    }
    
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        EventSourceSupport.addActionsPresentres(presenters);
        super.gatherPresenters(presenters);
    }
    
    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.create (EventSourceSupport.createCommandEventSourceInfoResolver ()),
            // flow
            new CommandEventSourceFlowPinPresenter (),
            // properties
            PropertiesPresenterForwarder.createByReference(PROP_COMMAND),
            // delete
            DeleteDependencyPresenter.createDependentOnPropertyPresenter (PROP_DISPLAYABLE),
            DeleteDependencyPresenter.createDependentOnPropertyPresenter (PROP_COMMAND),
            new DeletePresenter () {
                protected void delete () {
                    DesignComponent component = getComponent ();
                    DesignComponent displayable = component.readProperty (PROP_DISPLAYABLE).getComponent ();
                    ArraySupport.remove (displayable, DisplayableCD.PROP_COMMANDS, component);
                }
            }
        );
    }

    
    public static class CommandEventSourceFlowPinPresenter extends FlowEventSourcePinPresenter {

        protected DesignComponent getComponentForAttachingPin () {
            return getComponent ().readProperty (PROP_DISPLAYABLE).getComponent ();
        }

        protected String getDisplayName () {
            DesignComponent command = getCommandComponent (getComponent ());
            if(command == null || command.getParentComponent() == null)
                return null;
            InfoPresenter presenter = command.getPresenter (InfoPresenter.class);
            return presenter.getDisplayName (InfoPresenter.NameType.PRIMARY);
        }

        protected String getOrder () {
            return FlowDisplayableCommandPinOrderPresenter.CATEGORY_ID;
        }

        protected boolean canRename () {
            DesignComponent command = getCommandComponent (getComponent ());
            return command != null;
        }

        protected String getRenameName () {
            DesignComponent command = getCommandComponent (getComponent ());
            InfoPresenter presenter = command.getPresenter (InfoPresenter.class);
            return presenter.getEditableName ();
        }

        protected void setRenameName (String name) {
            DesignComponent command = getCommandComponent (getComponent ());
            InfoPresenter presenter = command.getPresenter (InfoPresenter.class);
            presenter.setEditableName (name);
        }

        protected DesignEventFilter getEventFilter () {
            return super.getEventFilter ().addDescentFilter (getComponent (), PROP_COMMAND).addDescentFilter (getComponent (), PROP_DISPLAYABLE);
        }

    }

}
