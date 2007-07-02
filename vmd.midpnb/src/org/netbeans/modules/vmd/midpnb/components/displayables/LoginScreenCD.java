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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.DisplayablePC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.openide.util.Utilities;

/**
 * @author Karol Harezlak
 */

public final class LoginScreenCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.LoginScreen"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/login_screen_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/login_screen_32.png"; // NOI18N

    public static final String PROP_USERNAME = "username"; //NOI18N
    public static final String PROP_PASSWORD = "password"; //NOI18N
    public static final String PROP_USE_LOGIN_BUTTON = "useButton"; //NOI18N
    public static final String PROP_LOGIN_TITLE = "loginTitle"; //NOI18N 
    public static final String PROP_BGK_COLOR = "backgroundColor"; //NOI18N
    public static final String PROP_FRG_COLOR = "foregroungColor"; //NOI18N
    
    private static final String LOGIN_PROPERTIES = "Login Properties"; //NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_USERNAME, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_PASSWORD, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_USE_LOGIN_BUTTON, MidpTypes.TYPEID_BOOLEAN, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_LOGIN_TITLE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_BGK_COLOR, MidpTypes.TYPEID_INT, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_FRG_COLOR, MidpTypes.TYPEID_INT, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
       return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
               .addPropertiesCategory(LOGIN_PROPERTIES) 
                   .addProperty("Username", PropertyEditorString.createInstance(), PROP_USERNAME)
                   .addProperty("Password", PropertyEditorString.createInstance(), PROP_PASSWORD)
               .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES) 
                   .addProperty("Use Login Button", PropertyEditorBooleanUC.createInstance(false), PROP_USE_LOGIN_BUTTON) //NOI18N
                   .addProperty("Login Title", PropertyEditorString.createInstance(), PROP_LOGIN_TITLE)
                   .addProperty("Background Color", PropertyEditorNumber.createIntegerInstance(), PROP_BGK_COLOR)
                   .addProperty("Foreground Color", PropertyEditorNumber.createIntegerInstance(), PROP_FRG_COLOR);
    }
    
    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createDisplayParameter ())
            .addParameters(MidpParameter.create(PROP_BGK_COLOR, PROP_FRG_COLOR, PROP_USERNAME, PROP_PASSWORD, PROP_LOGIN_TITLE, PROP_USE_LOGIN_BUTTON))
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            .addSetters(MidpSetter.createSetter("setBGColor", MidpVersionable.MIDP).addParameters(PROP_BGK_COLOR))
            .addSetters(MidpSetter.createSetter("setFGColor", MidpVersionable.MIDP).addParameters(PROP_FRG_COLOR))
            .addSetters(MidpSetter.createSetter("setPassword", MidpVersionable.MIDP).addParameters(PROP_PASSWORD))
            .addSetters(MidpSetter.createSetter("setUsername", MidpVersionable.MIDP).addParameters(PROP_USERNAME))
            .addSetters(MidpSetter.createSetter("setLoginTitle", MidpVersionable.MIDP).addParameters(PROP_PASSWORD))
            .addSetters(MidpSetter.createSetter("setUseLoginButton", MidpVersionable.MIDP).addParameters(PROP_USE_LOGIN_BUTTON));
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            //properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter(),
            // actions
            AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, CommandCD.TYPEID),
            //inspector
            InspectorPositionPresenter.create(new DisplayablePC()),
            MidpCodePresenterSupport.createAddImportPresenter(),
            //screen
            new DisplayableDisplayPresenter(Utilities.loadImage(ICON_PATH))
        );
    }

    public void postInitialize(DesignComponent component) {
        super.postInitialize(component);
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY);
    }

    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, AddActionPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, DisplayableDisplayPresenter.class);
        MidpActionsSupport.addUnusedCommandsAddActionForDisplayable(presenters);
        super.gatherPresenters(presenters);
    }

}
