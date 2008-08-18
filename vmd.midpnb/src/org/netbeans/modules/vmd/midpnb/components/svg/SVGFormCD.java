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
package org.netbeans.modules.vmd.midpnb.components.svg;

import java.util.ArrayList;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.CodeClassInitHeaderFooterPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.codegen.SwitchDisplayableParameterPresenter;
import org.netbeans.modules.vmd.midp.components.MidpAcceptProducerKindPresenter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.PropertyEditorResource;
import org.netbeans.modules.vmd.midpnb.actions.EditSVGFileAction;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.components.SVGImageAcceptTrensferableKindPresenter;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGButtonCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGCheckBoxCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComboBoxCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormFileChangePresneter;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGLabelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGListCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGRadioButtonCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGSliderCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGSpinnerCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGTextFieldCD;
import org.netbeans.modules.vmd.midpnb.general.SVGFileAcceptPresenter;
import org.netbeans.modules.vmd.midpnb.propertyeditors.SVGFormEditorElement;
import org.netbeans.modules.vmd.midpnb.screen.display.SVGPlayerDisplayPresenter;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * This class represents Component Descriptor for SVG Form component.
 * 
 * @author Andrew Korostelev
 */
public class SVGFormCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGForm"); // NOI18N
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_form_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_form_32.png"; // NOI18N
    
    public static final String PROP_SVG_IMAGE = "svgImage"; //NOI18N
    public static final String PROP_START_ANIM_IMMEDIATELY = "startAnimationImmediately"; //NOI18N
    public static final String PROP_TIME_INCREMENT = "animationTimeIncrement"; //NOI18N
    public static final String PROP_RESET_ANIMATION_WHEN_STOPPED = "resetAnimationWhenStopped"; //NOI18N

    public static final String PROP_OLD_START_ANIM_IMMEDIATELY = "startAnimationImmideately"; //NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    
    @Override
    public void postInitialize(DesignComponent component) {
        component.writeProperty(PROP_START_ANIM_IMMEDIATELY, MidpTypes.createBooleanValue(true));
        MidpProjectSupport.addLibraryToProject (component.getDocument (), SVGPlayerCD.MIDP_NB_SVG_LIBRARY);
    }
    @Override
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_SVG_IMAGE, SVGImageCD.TYPEID, PropertyValue.createNull(), true, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_START_ANIM_IMMEDIATELY, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_TIME_INCREMENT, MidpTypes.TYPEID_FLOAT, MidpTypes.createFloatValue(0.1f), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_RESET_ANIMATION_WHEN_STOPPED, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), false, true, Versionable.FOREVER)
                );
    }
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters(presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_SVGImage"), //NOI18N
                    PropertyEditorResource.createInstance(new SVGFormEditorElement(),
                        NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NEW"), //NOI18N
                        NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NONE"), //NOI18N
                        NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_UCLABEL")), PROP_SVG_IMAGE) //NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_StartAnimationImmediately"), // NOI18N
                    PropertyEditorBooleanUC.createInstance(NbBundle.getMessage(SVGPlayerCD.class, "LBL_SVGPlayer_StartAnimationImmediately")), PROP_START_ANIM_IMMEDIATELY) // NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_AnimationTimeIncrement"), // NOI18N
                    PropertyEditorNumber.createFloatInstance(NbBundle.getMessage(SVGPlayerCD.class, "LBL_SVGPlayer_AnimationTimeIncrement")), PROP_TIME_INCREMENT) // NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_ResetAnimationWhenStopped"), // NOI18N
                    PropertyEditorBooleanUC.createInstance(NbBundle.getMessage(SVGPlayerCD.class, "LBL_SVGPlayer_ResetAnimationWhenStopped")), PROP_RESET_ANIMATION_WHEN_STOPPED); // NOI18N
    }

    private Presenter createSetterPresenter() {
        return new CodeSetterPresenter ()
                .addParameters(MidpCustomCodePresenterSupport.createDisplayParameter())
                .addParameters(MidpParameter.create(PROP_SVG_IMAGE, PROP_START_ANIM_IMMEDIATELY, PROP_TIME_INCREMENT, PROP_RESET_ANIMATION_WHEN_STOPPED))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(PROP_SVG_IMAGE, MidpCustomCodePresenterSupport.PARAM_DISPLAY))
                .addSetters(MidpSetter.createSetter("setTimeIncrement", MidpVersionable.MIDP_2).addParameters(PROP_TIME_INCREMENT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setStartAnimationImmediately", MidpVersionable.MIDP_2).addParameters(PROP_START_ANIM_IMMEDIATELY)) //NOI18N
                .addSetters(MidpSetter.createSetter("setResetAnimationWhenStopped", MidpVersionable.MIDP_2).addParameters(PROP_RESET_ANIMATION_WHEN_STOPPED)); //NOI18N
    }
    
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter (),
                //accept
                new SVGFileAcceptPresenter (),
                new MidpAcceptProducerKindPresenter ().addType(SVGImageCD.TYPEID, PROP_SVG_IMAGE),
                new SVGImageAcceptTrensferableKindPresenter().addType(SVGImageCD.TYPEID, PROP_SVG_IMAGE),
                // code
                MidpCodePresenterSupport.createAddImportPresenter(),
                new SwitchDisplayableParameterPresenter() {
                    public String generateSwitchDisplayableParameterCode() {
                        return CodeReferencePresenter.generateAccessCode(getComponent()) + ".getSvgCanvas ()"; // NOI18N
                    }
                },
                createSetterPresenter(),
                new SVGFormPresenterCodeClassInitHeaderFooterPresenter(),
                // delete
                DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_SVG_IMAGE),
                // screen
                new SVGPlayerDisplayPresenter (false),
                //actions
                ActionsPresenter.create(20, SystemAction.get(EditSVGFileAction.class)),
                //other
                new SVGFormFileChangePresneter()
                );
    }
    
    private class SVGFormPresenterCodeClassInitHeaderFooterPresenter extends CodeClassInitHeaderFooterPresenter {

        @Override
        public void generateClassInitializationHeader(MultiGuardedSection section) {
            //Do nothing
        }

        @Override
        public void generateClassInitializationFooter(MultiGuardedSection section) {
            Collection<DesignComponent> components = getComponent().getComponents();
            for (DesignComponent component : components) {
                if (component.getType() == SVGButtonCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGCheckBoxCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGComboBoxCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGLabelCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGListCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGSpinnerCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGListCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGRadioButtonCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGTextFieldCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGSliderCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                }
            }
        }
    }

    private static void generateSVGFormAddComponentCode(MultiGuardedSection section, DesignComponent svgForm, DesignComponent componentToAdd) {
        section.getWriter().write(CodeReferencePresenter.generateDirectAccessCode(svgForm));
        section.getWriter().write(".add(" + CodeReferencePresenter.generateAccessCode(componentToAdd) + ");\n"); //NOI18N

    }
    
    
}
