/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.components.svg.form;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionController;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter.IconType;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter.NameType;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;

public class SVGListElementEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#SVGListelement"); // NOI18N
    public static final String PROP_INDEX = "index";

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(EventSourceCD.TYPEID, TYPEID, true, false);
    }

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    @Override
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_INDEX, MidpTypes.TYPEID_INT, PropertyValue.createNull(), false, false, MidpVersionable.MIDP));
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return null;
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, InspectorPositionPresenter.class);
        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // info
                InfoPresenter.create(new SVGListElementresolver()),
                //flow
                new FlowEventSourcePinPresenter() {

            @Override
            protected DesignComponent getComponentForAttachingPin() {
                return getComponent().getParentComponent().getParentComponent();
            }

            @Override
            protected String getDisplayName() {
                return getName(getComponent());
            }

            @Override
            protected String getOrder() {
                return SVGFormCD.SVGListElementOrderCategory.CATEGORY_ID;
            }
        },
                //inspector
                InspectorPositionPresenter.create(new InspectorPositionController[]{new SVGListElementEventSourcePositionController()}));

    }

    private static String getName(DesignComponent component) {
        List<PropertyValue> array = component.getParentComponent().readProperty(SVGListCD.PROP_MODEL).getArray();
        if (array == null || array.size() - 1 < (Integer) component.readProperty(PROP_INDEX).getPrimitiveValue()) {
            return "updating array"; //NOI18N
        }
        PropertyValue pv = array.get((Integer) component.readProperty(PROP_INDEX).getPrimitiveValue());

        return (String) pv.getPrimitiveValue();

    }

    private class SVGListElementresolver implements InfoPresenter.Resolver {

        public DesignEventFilter getEventFilter(DesignComponent component) {
            return new DesignEventFilter().setGlobal(true);
        }

        public String getDisplayName(DesignComponent component, NameType nameType) {
            return getName(component);
        }

        public boolean isEditable(DesignComponent component) {
            return true;
        }

        public String getEditableName(DesignComponent component) {
            return getName(component);
        }

        public void setEditableName(DesignComponent component, String enteredName) {
        }

        public Image getIcon(DesignComponent component, IconType iconType) {
            return null;
        }
    }

    private class SVGListElementEventSourcePositionController implements InspectorPositionController {

        public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
            if (path.getLastElement().getTypeID() == SVGListCD.TYPEID)
                return true;
            return false;
        }
    }
}



