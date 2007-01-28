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

package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.faces.FacetDescriptor;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.faces.dt.BeanDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt_1_2.component.UIOutputBeanInfoBase;


public class HtmlOutputLinkBeanInfo extends UIOutputBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_2.component.html.Bundle-JSF", Locale.getDefault(), HtmlOutputLinkBeanInfo.class.getClassLoader());


    public HtmlOutputLinkBeanInfo() {
        beanClass = javax.faces.component.html.HtmlOutputLink.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlOutputLink_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlOutputLink_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlOutputLink_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlOutputLink_M32";
    }


    private BeanDescriptor beanDescriptor;

    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptorBase(beanClass);
        beanDescriptor.setDisplayName(resources.getString("HtmlOutputLink_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("HtmlOutputLink_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_jsfstd_hyperlink");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"hyperlink");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_propsheets_jsfstd_hyperlink_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"outputLink");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_PREFIX,"h");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI,"http://java.sun.com/jsf/html");

        return beanDescriptor;

    }


    private PropertyDescriptor[] propertyDescriptors;

    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_accesskey = new PropertyDescriptorBase("accesskey",beanClass,"getAccesskey","setAccesskey");
            prop_accesskey.setDisplayName(resources.getString("HtmlOutputLink_accesskey_DisplayName"));
            prop_accesskey.setShortDescription(resources.getString("HtmlOutputLink_accesskey_Description"));
            prop_accesskey.setExpert(false);
            prop_accesskey.setHidden(false);
            prop_accesskey.setPreferred(false);
            attrib = new AttributeDescriptor("accesskey",false,null,true);
            prop_accesskey.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_accesskey.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_charset = new PropertyDescriptorBase("charset",beanClass,"getCharset","setCharset");
            prop_charset.setDisplayName(resources.getString("HtmlOutputLink_charset_DisplayName"));
            prop_charset.setShortDescription(resources.getString("HtmlOutputLink_charset_Description"));
            prop_charset.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_charset.setExpert(false);
            prop_charset.setHidden(false);
            prop_charset.setPreferred(false);
            attrib = new AttributeDescriptor("charset",false,null,true);
            prop_charset.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_charset.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_charset.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.CharacterSetsDomain.class);

            PropertyDescriptor prop_coords = new PropertyDescriptorBase("coords",beanClass,"getCoords","setCoords");
            prop_coords.setDisplayName(resources.getString("HtmlOutputLink_coords_DisplayName"));
            prop_coords.setShortDescription(resources.getString("HtmlOutputLink_coords_Description"));
            prop_coords.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_coords.setExpert(false);
            prop_coords.setHidden(false);
            prop_coords.setPreferred(false);
            attrib = new AttributeDescriptor("coords",false,null,true);
            prop_coords.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_coords.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_dir = new PropertyDescriptorBase("dir",beanClass,"getDir","setDir");
            prop_dir.setDisplayName(resources.getString("HtmlOutputLink_dir_DisplayName"));
            prop_dir.setShortDescription(resources.getString("HtmlOutputLink_dir_Description"));
            prop_dir.setPropertyEditorClass(loadClass("com.sun.jsfcl.std.property.ChooseOneReferenceDataPropertyEditor"));
            prop_dir.setExpert(false);
            prop_dir.setHidden(false);
            prop_dir.setPreferred(false);
            attrib = new AttributeDescriptor("dir",false,null,true);
            prop_dir.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dir.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_dir.setValue("referenceDataDefiner", com.sun.jsfcl.std.reference.ReferenceDataManager.TEXT_DIRECTIONS);

            PropertyDescriptor prop_disabled = new PropertyDescriptorBase("disabled",beanClass,"isDisabled","setDisabled");
            prop_disabled.setDisplayName(resources.getString("HtmlOutputLink_disabled_DisplayName"));
            prop_disabled.setShortDescription(resources.getString("HtmlOutputLink_disabled_Description"));
            prop_disabled.setExpert(false);
            prop_disabled.setHidden(false);
            prop_disabled.setPreferred(false);
            attrib = new AttributeDescriptor("disabled",false,null,true);
            prop_disabled.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_disabled.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_hreflang = new PropertyDescriptorBase("hreflang",beanClass,"getHreflang","setHreflang");
            prop_hreflang.setDisplayName(resources.getString("HtmlOutputLink_hreflang_DisplayName"));
            prop_hreflang.setShortDescription(resources.getString("HtmlOutputLink_hreflang_Description"));
            prop_hreflang.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_hreflang.setExpert(false);
            prop_hreflang.setHidden(false);
            prop_hreflang.setPreferred(false);
            attrib = new AttributeDescriptor("hreflang",false,null,true);
            prop_hreflang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_hreflang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_hreflang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_lang = new PropertyDescriptorBase("lang",beanClass,"getLang","setLang");
            prop_lang.setDisplayName(resources.getString("HtmlOutputLink_lang_DisplayName"));
            prop_lang.setShortDescription(resources.getString("HtmlOutputLink_lang_Description"));
            prop_lang.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_lang.setExpert(false);
            prop_lang.setHidden(false);
            prop_lang.setPreferred(false);
            attrib = new AttributeDescriptor("lang",false,null,true);
            prop_lang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_lang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_lang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_rel = new PropertyDescriptorBase("rel",beanClass,"getRel","setRel");
            prop_rel.setDisplayName(resources.getString("HtmlOutputLink_rel_DisplayName"));
            prop_rel.setShortDescription(resources.getString("HtmlOutputLink_rel_Description"));
            prop_rel.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_rel.setExpert(false);
            prop_rel.setHidden(false);
            prop_rel.setPreferred(false);
            attrib = new AttributeDescriptor("rel",false,null,true);
            prop_rel.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rel.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_rel.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlLinkTypesDomain.class);

            PropertyDescriptor prop_rev = new PropertyDescriptorBase("rev",beanClass,"getRev","setRev");
            prop_rev.setDisplayName(resources.getString("HtmlOutputLink_rev_DisplayName"));
            prop_rev.setShortDescription(resources.getString("HtmlOutputLink_rev_Description"));
            prop_rev.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_rev.setExpert(false);
            prop_rev.setHidden(false);
            prop_rev.setPreferred(false);
            attrib = new AttributeDescriptor("rev",false,null,true);
            prop_rev.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rev.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_rev.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlLinkTypesDomain.class);

            PropertyDescriptor prop_shape = new PropertyDescriptorBase("shape",beanClass,"getShape","setShape");
            prop_shape.setDisplayName(resources.getString("HtmlOutputLink_shape_DisplayName"));
            prop_shape.setShortDescription(resources.getString("HtmlOutputLink_shape_Description"));
            prop_shape.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_shape.setExpert(false);
            prop_shape.setHidden(false);
            prop_shape.setPreferred(false);
            attrib = new AttributeDescriptor("shape",false,null,true);
            prop_shape.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_shape.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_shape.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlRegionShapesDomain.class);

            PropertyDescriptor prop_tabindex = new PropertyDescriptorBase("tabindex",beanClass,"getTabindex","setTabindex");
            prop_tabindex.setDisplayName(resources.getString("HtmlOutputLink_tabindex_DisplayName"));
            prop_tabindex.setShortDescription(resources.getString("HtmlOutputLink_tabindex_Description"));
            prop_tabindex.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_tabindex.setExpert(false);
            prop_tabindex.setHidden(false);
            prop_tabindex.setPreferred(false);
            attrib = new AttributeDescriptor("tabindex",false,null,true);
            prop_tabindex.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tabindex.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_tabindex.setValue("maxValue", new Integer(Short.MAX_VALUE));
            prop_tabindex.setValue("minValue", new Integer(0));

            PropertyDescriptor prop_target = new PropertyDescriptorBase("target",beanClass,"getTarget","setTarget");
            prop_target.setDisplayName(resources.getString("HtmlOutputLink_target_DisplayName"));
            prop_target.setShortDescription(resources.getString("HtmlOutputLink_target_Description"));
            prop_target.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_target.setExpert(false);
            prop_target.setHidden(false);
            prop_target.setPreferred(false);
            attrib = new AttributeDescriptor("target",false,null,true);
            prop_target.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_target.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);
            prop_target.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlFrameTargetsDomain.class);

            PropertyDescriptor prop_type = new PropertyDescriptorBase("type",beanClass,"getType","setType");
            prop_type.setDisplayName(resources.getString("HtmlOutputLink_type_DisplayName"));
            prop_type.setShortDescription(resources.getString("HtmlOutputLink_type_Description"));
            prop_type.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_type.setExpert(false);
            prop_type.setHidden(false);
            prop_type.setPreferred(false);
            attrib = new AttributeDescriptor("type",false,null,true);
            prop_type.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_type.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_type.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.MimeTypesDomain.class);

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_accesskey);
            propertyDescriptorList.add(prop_charset);
            propertyDescriptorList.add(prop_coords);
            propertyDescriptorList.add(prop_dir);
            propertyDescriptorList.add(prop_disabled);
            propertyDescriptorList.add(prop_hreflang);
            propertyDescriptorList.add(prop_lang);
            propertyDescriptorList.add(prop_rel);
            propertyDescriptorList.add(prop_rev);
            propertyDescriptorList.add(prop_shape);
            propertyDescriptorList.add(prop_tabindex);
            propertyDescriptorList.add(prop_target);
            propertyDescriptorList.add(prop_type);

            propertyDescriptorList.addAll(Properties.getVisualPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getKeyEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getMouseEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getClickEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getFocusEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}

