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
package org.netbeans.modules.vmd.midp.components.points;

import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.sources.IfFalseEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.IfTrueEventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowIfPointPinOrderPresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorJavaString;
import org.netbeans.api.editor.guards.GuardedSection;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author David Kaspar
 */
public class IfPointCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#IfPoint"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/if_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/if_32.png"; // NOI18N

    public static final String PROP_CONDITION = "condition"; // NOI18N
    public static final String PROP_TRUE = "true"; // NOI18N
    public static final String PROP_FALSE = "false"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (MethodPointCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public java.util.List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor (PROP_CONDITION, MidpTypes.TYPEID_JAVA_CODE, MidpTypes.createJavaCodeValue ("true"), false, false, Versionable.FOREVER), // NOI18N
            new PropertyDescriptor (PROP_TRUE, IfTrueEventSourceCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER),
            new PropertyDescriptor (PROP_FALSE, IfFalseEventSourceCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER)
        );
    }

    public DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                    .addProperty (NbBundle.getMessage(IfPointCD.class, "DISP_IfPoint_Condition_Code"), PropertyEditorJavaString.createInstance(TYPEID), PROP_CONDITION);
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, true, true, true, true);
        super.gatherPresenters (presenters);
    }

    protected java.util.List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // flow
            new FlowIfPointPinOrderPresenter (),
            // general
            InfoPresenter.create (PointSupport.createInfoResolver (Utilities.loadImage (ICON_PATH), MethodPointCD.PROP_METHOD_NAME, NbBundle.getMessage(IfPointCD.class, "TYPE_IfPoint"))),
            new GoToSourcePresenter() {
                protected boolean matches (GuardedSection section) {
                    return MultiGuardedSection.matches(section, getComponent ().getComponentID () + "-if", 1); // NOI18N
                }
            },
            // properties
            createPropertiesPresenter (),
            // code
            new CodeClassLevelPresenter.Adapter () {
                @Override
                protected void generateClassBodyCode (StyledDocument document) {
                    DesignComponent component = getComponent ();
                    MultiGuardedSection section = MultiGuardedSection.create (document, component.getComponentID () + "-if"); // NOI18N
                    String methodName = CodeReferencePresenter.generateDirectAccessCode (component);
                    section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: " + methodName + " \">\n"); // NOI18N
                    section.getWriter ().write ("/**\n * Performs an action assigned to the " + methodName + " if-point.\n */\n"); // NOI18N
                    section.getWriter ().write ("public void " + methodName + " () {\n").commit (); // NOI18N
                    section.switchToEditable (component.getComponentID () + "-preIf"); // NOI18N
                    section.getWriter ().write (" // enter pre-if user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();

                    section.getWriter ().write ("if ("); // NOI18N
                    MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), component.readProperty (PROP_CONDITION));
                    section.getWriter ().write (") {\n"); // NOI18N
                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, component.readProperty (PROP_TRUE).getComponent ());
                    section.getWriter ().write ("} else {\n"); // NOI18N
                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, component.readProperty (PROP_FALSE).getComponent ());
                    section.getWriter ().write ("}\n").commit (); // NOI18N

                    section.switchToEditable (component.getComponentID () + "-postIf"); // NOI18N
                    section.getWriter ().write (" // enter post-if user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();
                    section.getWriter ().write ("}\n"); // NOI18N
                    section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
                    section.close ();
                }
            },
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_TRUE),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_FALSE)
        );
    }

}
