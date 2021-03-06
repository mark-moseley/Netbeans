/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.midp.components.sources;

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionController;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import org.netbeans.modules.vmd.midp.components.points.PointCD;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.general.AcceptContextResolver;
import org.netbeans.modules.vmd.midp.inspector.controllers.ChildrenByTypePC;
import org.netbeans.modules.vmd.midp.inspector.controllers.ComponentsCategoryPC;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.eventhandler.PropertyEditorEventHandler;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class EventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#EventSource"); // NOI18N

    public static final String PROP_EVENT_HANDLER = "eventHandler"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (null, EventSourceCD.TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor (PROP_EVENT_HANDLER, EventHandlerCD.TYPEID, PropertyValue.createNull (), true, false, Versionable.FOREVER)
        );
    }

    public DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_ACTION_PROPERTIES)
                .addProperty(NbBundle.getMessage(EventSourceCD.class, "DISP_EventSource_Action"), PropertyEditorEventHandler.createInstance(), PROP_EVENT_HANDLER) // NOI18N
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES);
    }

    private InspectorPositionController[] createPositionControllers() {
        return new InspectorPositionController[]{ new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_COMMANDS),
                                                  new ChildrenByTypePC(PointCD.TYPEID, MobileDeviceCD.TYPEID, ListElementEventSourceCD.TYPEID),
                                                };
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, false, false, true, true);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // properties
            createPropertiesPresenter (),
            //inspector
            new InspectorFolderComponentPresenter(true),
            InspectorPositionPresenter.create(createPositionControllers()),
            // accept
            new AcceptTypePresenter(EventHandlerCD.TYPEID) {
                protected boolean notifyAccepting (TypeID producerTypeID) {
                    return super.notifyAccepting (producerTypeID)  &&  AcceptContextResolver.resolveAcceptAllowance (getComponent (), producerTypeID);
                }
                protected void notifyCreated (DesignComponent component) {
                    MidpDocumentSupport.updateEventHandlerWithNew (getComponent (), component);
                }
            },
            // code
            new CodeMultiGuardedLevelPresenter() {
                protected void generateMultiGuardedSectionCode (MultiGuardedSection section) {
                    section.getWriter ().commit ();
                    section.switchToEditable (getComponent ().getComponentID () + "-preAction"); // NOI18N
                    section.getWriter ().write (" // write pre-action user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();

                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, EventSourceCD.getEventHandler (getComponent ()));

                    section.getWriter ().commit ();
                    section.switchToEditable (getComponent ().getComponentID () + "-postAction"); // NOI18N
                    section.getWriter ().write (" // write post-action user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();
                }
            }
        
        );
    }

    public static DesignComponent getEventHandler (DesignComponent eventSource) {
        return eventSource.readProperty (PROP_EVENT_HANDLER).getComponent ();
    }

}
