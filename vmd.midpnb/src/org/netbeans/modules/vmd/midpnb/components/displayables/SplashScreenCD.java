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

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class SplashScreenCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.SplashScreen"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/splash_screen_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/splash_screen_64.png"; // NOI18N

    public static final String PROP_TIMEOUT = "timeout"; //NOI18N
    public static final String PROP_ALLOW_TIMEOUT_INTERRUPT = "allowTimeoutInterrupt"; //NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(AbstractInfoScreenCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_TIMEOUT, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (5000), false, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_ALLOW_TIMEOUT_INTERRUPT, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(Boolean.FALSE), false, false, MidpVersionable.MIDP_2)
        );
    }

    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (AbstractInfoScreenCode.createDisplayParameter ())
            .addParameters (AbstractInfoScreenCode.createTimeoutParameter ())
            .addParameters (MidpParameter.create (PROP_ALLOW_TIMEOUT_INTERRUPT))
            .addParameters (AbstractInfoScreenCode.createSplashScreenCommandParameter ())
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (AbstractInfoScreenCode.PARAM_DISPLAY))
            .addSetters (MidpSetter.createSetter ("setTimeout", MidpVersionable.MIDP_2).addParameters (AbstractInfoScreenCode.PARAM_TIMEOUT))
            .addSetters (MidpSetter.createSetter ("setAllowTimeoutInterrupt", MidpVersionable.MIDP_2).addParameters (PROP_ALLOW_TIMEOUT_INTERRUPT));
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter () {
       return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
               .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                   .addProperty("Timeout", Integer.class, PROP_TIMEOUT)
                   .addProperty("Allow Timeout Interrupt", Boolean.class, PROP_ALLOW_TIMEOUT_INTERRUPT);
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter ()
        );
    }

}
