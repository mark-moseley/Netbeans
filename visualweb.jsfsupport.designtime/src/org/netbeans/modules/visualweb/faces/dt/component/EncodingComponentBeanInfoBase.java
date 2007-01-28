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
package org.netbeans.modules.visualweb.faces.dt.component;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.ResourceBundle;

import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.faces.FacetDescriptor;
import com.sun.rave.designtime.markup.AttributeDescriptor;

import org.netbeans.modules.visualweb.faces.dt.HtmlBeanInfoBase;
import org.netbeans.modules.visualweb.faces.dt.BeanDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;


abstract class EncodingComponentBeanInfoBase extends HtmlBeanInfoBase {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt.component.Bundle-JSF-base", Locale.getDefault(), EncodingComponentBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>EncodingComponentBeanInfoBase</code>.</p>
     */
    public EncodingComponentBeanInfoBase() {

        beanClass =  com.sun.rave.faces.component.EncodingComponent.class;

        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt/component/EncodingComponent_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt/component/EncodingComponent_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt/component/EncodingComponent_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt/component/EncodingComponent_M32";

    }

    private BeanDescriptor beanDescriptor;

    /**
     * <p>Return the <code>BeanDescriptor</code> for this bean.</p>
     */
    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptorBase(beanClass);
        beanDescriptor.setDisplayName(resources.getString("EncodingComponent_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("EncodingComponent_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_jsf-val-conv_set_encoding_component");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"encoding");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.MARKUP_SECTION,"head");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"encoding");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_PREFIX,"j");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI,"http://www.sun.com/creator/rave.faces");
        beanDescriptor.setValue(Constants.BeanDescriptor.TRAY_COMPONENT,Boolean.TRUE);
        return beanDescriptor;

    }

    /**
     * <p>Return the <code>CategoryDescriptor</code> array for the property categories of this component.</p>
     */
    private CategoryDescriptor[] getCategoryDescriptors() {

        return com.sun.rave.designtime.base.CategoryDescriptors.getDefaultCategoryDescriptors();

    }

    /**
     * <p>The cached facet descriptors.</p>
     */
    protected FacetDescriptor[] facetDescriptors;

    /**
     * <p>Return the <code>FacetDescriptor</code>s for this bean.</p>
     */
    public FacetDescriptor[] getFacetDescriptors() {

        if (facetDescriptors != null) {
            return facetDescriptors;
        }
        facetDescriptors = new FacetDescriptor[] {
        };
        return facetDescriptors;

    }


    private PropertyDescriptor[] propDescriptors;

    /**
     * <p>Return the <code>PropertyDescriptor</code>s for this bean.</p>
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propDescriptors != null) {
            return propDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_value = new PropertyDescriptorBase("value",beanClass,"getValue","setValue");
            prop_value.setDisplayName(resources.getString("EncodingComponent_value_DisplayName"));
            prop_value.setShortDescription(resources.getString("EncodingComponent_value_Description"));
            prop_value.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_value.setExpert(false);
            prop_value.setHidden(false);
            prop_value.setPreferred(false);
            attrib = new AttributeDescriptor("value",false,null,true);
            prop_value.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_value.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_value.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.CharacterSetsDomain.class);

            propDescriptors = new PropertyDescriptor[] {
                prop_value,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
