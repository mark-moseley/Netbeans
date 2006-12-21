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
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowItemCommandPinOrderPresenter;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenterForwarder;

/**
 * @author David Kaspar
 */

public final class ItemCommandEventSourceCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#ItemCommandEventSource"); // NOI18N
    
    // HINT this is redundant property, could be resolved from parent
    public static final String PROP_ITEM = "item"; // NOI18N
    public static final String PROP_COMMAND = "command"; // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(EventSourceCD.TYPEID, TYPEID, true, false);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(ItemCommandEventSourceCD.PROP_ITEM, ItemCD.TYPEID, PropertyValue.createNull(), false, false, MidpVersionable.MIDP),
            new PropertyDescriptor(ItemCommandEventSourceCD.PROP_COMMAND, CommandCD.TYPEID, PropertyValue.createNull(), false, false, MidpVersionable.MIDP)
            );
    }
    
    public static DesignComponent getItemComponent(DesignComponent itemCommandEventSourceComponent) {
        return itemCommandEventSourceComponent.readProperty(ItemCommandEventSourceCD.PROP_ITEM).getComponent();
    }
    
    public static DesignComponent getFormComponent(DesignComponent itemCommandEventSourceComponent) {
        DesignComponent itemComponent = getItemComponent(itemCommandEventSourceComponent);
        return itemComponent != null ? itemComponent.getParentComponent() : null;
    }
    
    public static DesignComponent getCommandComponent(DesignComponent commandEventSourceComponent) {
        return commandEventSourceComponent.readProperty(ItemCommandEventSourceCD.PROP_COMMAND).getComponent();
    }
    
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        EventSourceSupport.addActionsPresentres(presenters);
        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // info
            InfoPresenter.create(EventSourceSupport.createItemCommandEventSourceInfoResolver()),
            //properties
            PropertiesPresenterForwarder.createByReference(PROP_COMMAND),
            // flow
            new FlowEventSourcePinPresenter() {
            protected DesignComponent getComponentForAttachingPin() {
                return getFormComponent(getComponent());
            }
            
            protected String getDisplayName() {
                DesignComponent command = getCommandComponent(getComponent());
                InfoPresenter presenter = command.getPresenter(InfoPresenter.class);
                return presenter.getDisplayName(InfoPresenter.NameType.PRIMARY);
                
            }
            
            protected String getOrder() {
                return FlowItemCommandPinOrderPresenter.CATEGORY_ID;
            }
            
            protected boolean canRename() {
                DesignComponent command = ItemCommandEventSourceCD.getCommandComponent(getComponent());
                return command != null;
            }
            
            protected String getRenameName() {
                DesignComponent command = ItemCommandEventSourceCD.getCommandComponent(getComponent());
                return (String) command.readProperty(CommandCD.PROP_LABEL).getValue();
            }
            
            protected void setRenameName(String name) {
                DesignComponent command = ItemCommandEventSourceCD.getCommandComponent(getComponent());
                command.writeProperty(CommandCD.PROP_LABEL, MidpTypes.createStringValue(name));
            }
            
            protected DesignEventFilter getEventFilter() {
                return super.getEventFilter().addDescentFilter(getComponent(), ItemCommandEventSourceCD.PROP_COMMAND);
            }
        },
            DeleteDependencyPresenter.createDependentOnPropertyPresenter(PROP_ITEM),
            DeleteDependencyPresenter.createDependentOnPropertyPresenter(PROP_COMMAND),
            new DeletePresenter() {
            protected void delete() {
                DesignComponent component = getComponent();
                DesignComponent item = component.readProperty(PROP_ITEM).getComponent();
                ArraySupport.remove(item, ItemCD.PROP_COMMANDS, component);
            }
        }
        );
    }
    
    
}
