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
import javax.faces.convert.BigDecimalConverter;

public class BigDecimalConverterBeanInfo extends HtmlNonGeneratedBeanInfoBase {  // SimpleBeanInfo

    private static final ComponentBundle bundle = ComponentBundle.getBundle(BigDecimalConverterBeanInfo.class);

    /**
     * Construct a <code>FloatConverterBeanInfo</code> instance
     */
    public BigDecimalConverterBeanInfo() {
        beanClass = BigDecimalConverter.class;
        iconFileName_C16 = "BigDecimalConverter_C16";    //NOI18N
        iconFileName_C32 = "BigDecimalConverter_C32";    //NOI18N
        iconFileName_M16 = "BigDecimalConverter_M16";    //NOI18N
        iconFileName_M32 = "BigDecimalConverter_M32";    //NOI18N
    }


    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/core");    //NOI18N
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "convertFloat");    //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "bigDecimalConverter");    //NOI18N
            beanDescriptor.setDisplayName(bundle.getMessage("bigDecimalConvert"));    //NOI18N
            beanDescriptor.setShortDescription(bundle.getMessage("bigDecimalConvertShortDesc"));    //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_big_decimal_converter");
        }
        return beanDescriptor;
    }

}
