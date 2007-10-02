//GEN-BEGIN:BeanInfo
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.dataprovider.designtime.impl;

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

import java.beans.SimpleBeanInfo;

/**
 * <p>Auto-generated design time metadata class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

abstract class ListDataProviderBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataprovider.designtime.impl.Bundle-JSF", Locale.getDefault(), ListDataProviderBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>ListDataProviderBeanInfoBase</code>.</p>
     */
    public ListDataProviderBeanInfoBase() {

        beanClass = com.sun.data.provider.impl.ListDataProvider.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/ListDataProvider_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/ListDataProvider_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/ListDataProvider_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/ListDataProvider_M32";

    }

    /**
     * <p>The bean class that this BeanInfo represents.
     */
    protected Class beanClass;

    /**
     * <p>The cached BeanDescriptor.</p>
     */
    protected BeanDescriptor beanDescriptor;

    /**
     * <p>The index of the default property.</p>
     */
    protected int defaultPropertyIndex = -2;

    /**
     * <p>The name of the default property.</p>
     */
    protected String defaultPropertyName;

    /**
     * <p>The 16x16 color icon.</p>
     */
    protected String iconFileName_C16;

    /**
     * <p>The 32x32 color icon.</p>
     */
    protected String iconFileName_C32;

    /**
     * <p>The 16x16 monochrome icon.</p>
     */
    protected String iconFileName_M16;

    /**
     * <p>The 32x32 monochrome icon.</p>
     */
    protected String iconFileName_M32;

    /**
     * <p>The cached property descriptors.</p>
     */
    protected PropertyDescriptor[] propDescriptors;

    /**
     * <p>Return the <code>BeanDescriptor</code> for this bean.</p>
     */
    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptor(beanClass);
        beanDescriptor.setDisplayName(resources.getString("ListDataProvider_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("ListDataProvider_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"listDataProvider");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());

        return beanDescriptor;

    }

    /**
     * <p>Return the <code>CategoryDescriptor</code> array for the property categories of this component.</p>
     */
    private CategoryDescriptor[] getCategoryDescriptors() {

        return com.sun.rave.designtime.base.CategoryDescriptors.getDefaultCategoryDescriptors();

    }

    /**
     * <p>Return the index of the default property, or
     * -1 if there is no default property.</p>
     */
    public int getDefaultPropertyIndex() {

        if (defaultPropertyIndex > -2) {
            return defaultPropertyIndex;
        } else {
            if (defaultPropertyName == null) {
                defaultPropertyIndex = -1;
            } else {
                PropertyDescriptor pd[] = getPropertyDescriptors();
                for (int i = 0; i < pd.length; i++) {
                    if (defaultPropertyName.equals(pd[i].getName())) {
                        defaultPropertyIndex = i;
                        break;
                    }
                }
            }
        }
        return defaultPropertyIndex;
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

    /**
     * <p>Return the specified image (if any)
     * for this component class.</p>
     */
    public Image getIcon(int kind) {

        String name;
        switch (kind) {
            case ICON_COLOR_16x16:
                name = iconFileName_C16;
                break;
            case ICON_COLOR_32x32:
                name = iconFileName_C32;
                break;
            case ICON_MONO_16x16:
                name = iconFileName_M16;
                break;
            case ICON_MONO_32x32:
                name = iconFileName_M32;
                break;
            default:
                name = null;
                break;
        }
        if (name == null) {
            return null;
        }

        Image image = loadImage(name + ".png");
        if (image == null) {
            image = loadImage(name + ".gif");
        }
        return image;

    }

    /**
     * <p>Return a class loaded by name via the class loader that loaded this class.</p>
     */
    private java.lang.Class loadClass(java.lang.String name) {

        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * <p>Return the <code>PropertyDescriptor</code>s for this bean.</p>
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propDescriptors != null) {
            return propDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_class = new PropertyDescriptor("class",beanClass,"getClass",null);
            prop_class.setDisplayName(resources.getString("ListDataProvider_class_DisplayName"));
            prop_class.setShortDescription(resources.getString("ListDataProvider_class_Description"));
            prop_class.setExpert(false);
            prop_class.setHidden(true);
            prop_class.setPreferred(false);
            prop_class.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_cursorRow = new PropertyDescriptor("cursorRow",beanClass,"getCursorRow","setCursorRow");
            prop_cursorRow.setDisplayName(resources.getString("ListDataProvider_cursorRow_DisplayName"));
            prop_cursorRow.setShortDescription(resources.getString("ListDataProvider_cursorRow_Description"));
            prop_cursorRow.setExpert(false);
            prop_cursorRow.setHidden(true);
            prop_cursorRow.setPreferred(false);
            prop_cursorRow.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_dataListeners = new PropertyDescriptor("dataListeners",beanClass,"getDataListeners",null);
            prop_dataListeners.setDisplayName(resources.getString("ListDataProvider_dataListeners_DisplayName"));
            prop_dataListeners.setShortDescription(resources.getString("ListDataProvider_dataListeners_Description"));
            prop_dataListeners.setExpert(false);
            prop_dataListeners.setHidden(true);
            prop_dataListeners.setPreferred(false);
            prop_dataListeners.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_fieldKeys = new PropertyDescriptor("fieldKeys",beanClass,"getFieldKeys",null);
            prop_fieldKeys.setDisplayName(resources.getString("ListDataProvider_fieldKeys_DisplayName"));
            prop_fieldKeys.setShortDescription(resources.getString("ListDataProvider_fieldKeys_Description"));
            prop_fieldKeys.setExpert(false);
            prop_fieldKeys.setHidden(true);
            prop_fieldKeys.setPreferred(false);
            prop_fieldKeys.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_list = new PropertyDescriptor("list",beanClass,"getList","setList");
            prop_list.setDisplayName(resources.getString("ListDataProvider_list_DisplayName"));
            prop_list.setShortDescription(resources.getString("ListDataProvider_list_Description"));
            prop_list.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_list.setExpert(false);
            prop_list.setHidden(false);
            prop_list.setPreferred(false);
            prop_list.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_list.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", loadClass("com.sun.rave.propertyeditors.domains.InstanceVariableDomain"));

            PropertyDescriptor prop_rowCount = new PropertyDescriptor("rowCount",beanClass,"getRowCount",null);
            prop_rowCount.setDisplayName(resources.getString("ListDataProvider_rowCount_DisplayName"));
            prop_rowCount.setShortDescription(resources.getString("ListDataProvider_rowCount_Description"));
            prop_rowCount.setExpert(false);
            prop_rowCount.setHidden(true);
            prop_rowCount.setPreferred(false);
            prop_rowCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_tableCursorListeners = new PropertyDescriptor("tableCursorListeners",beanClass,"getTableCursorListeners",null);
            prop_tableCursorListeners.setDisplayName(resources.getString("ListDataProvider_tableCursorListeners_DisplayName"));
            prop_tableCursorListeners.setShortDescription(resources.getString("ListDataProvider_tableCursorListeners_Description"));
            prop_tableCursorListeners.setExpert(false);
            prop_tableCursorListeners.setHidden(true);
            prop_tableCursorListeners.setPreferred(false);
            prop_tableCursorListeners.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_tableDataListeners = new PropertyDescriptor("tableDataListeners",beanClass,"getTableDataListeners",null);
            prop_tableDataListeners.setDisplayName(resources.getString("ListDataProvider_tableDataListeners_DisplayName"));
            prop_tableDataListeners.setShortDescription(resources.getString("ListDataProvider_tableDataListeners_Description"));
            prop_tableDataListeners.setExpert(false);
            prop_tableDataListeners.setHidden(true);
            prop_tableDataListeners.setPreferred(false);
            prop_tableDataListeners.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            propDescriptors = new PropertyDescriptor[] {
                prop_class,
                prop_cursorRow,
                prop_dataListeners,
                prop_fieldKeys,
                prop_list,
                prop_rowCount,
                prop_tableCursorListeners,
                prop_tableDataListeners,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
//GEN-END:BeanInfo
