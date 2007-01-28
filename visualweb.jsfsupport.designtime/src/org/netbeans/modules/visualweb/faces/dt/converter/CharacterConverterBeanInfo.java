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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.faces.dt.converter;

import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import java.beans.*;
import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.convert.CharacterConverter;

public class CharacterConverterBeanInfo extends HtmlNonGeneratedBeanInfoBase {  // SimpleBeanInfo

    private static final ComponentBundle bundle = ComponentBundle.getBundle(CharacterConverterBeanInfo.class);

    /**
     * Construct a <code>CharacterConverterBeanInfo</code> instance
     */
    public CharacterConverterBeanInfo() {
        beanClass = CharacterConverter.class;
        iconFileName_C16 = "CharacterConverter_C16";
        iconFileName_C32 = "CharacterConverter_C32";
        iconFileName_M16 = "CharacterConverter_M16";
        iconFileName_M32 = "CharacterConverter_M32";
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/core");
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "convertCharacter");
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "characterConverter");
            beanDescriptor.setDisplayName(bundle.getMessage("charConvert"));
            beanDescriptor.setShortDescription(bundle.getMessage("charConvertShortDesc"));
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_char_converter");
        }
        return beanDescriptor;
    }

    /**
     * Returns the PropertyDescriptor array which describes
     * the property meta-data for this JavaBean
     *
     * @return An array of PropertyDescriptor objects
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return null;
    }
}
