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
package org.netbeans.modules.visualweb.web.ui.dt.faces;

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

abstract class ValueBindingSortCriteriaBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.faces.Bundle-JSF", Locale.getDefault(), ValueBindingSortCriteriaBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>ValueBindingSortCriteriaBeanInfoBase</code>.</p>
     */
    public ValueBindingSortCriteriaBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.faces.ValueBindingSortCriteria.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/faces/ValueBindingSortCriteria_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/faces/ValueBindingSortCriteria_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/faces/ValueBindingSortCriteria_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/faces/ValueBindingSortCriteria_M32";

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
        beanDescriptor.setDisplayName(resources.getString("ValueBindingSortCriteria_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("ValueBindingSortCriteria_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"valueBindingSortCriteria");
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

            PropertyDescriptor prop_ascending = new PropertyDescriptor("ascending",beanClass,"isAscending","setAscending");
            prop_ascending.setDisplayName(resources.getString("ValueBindingSortCriteria_ascending_DisplayName"));
            prop_ascending.setShortDescription(resources.getString("ValueBindingSortCriteria_ascending_Description"));
            prop_ascending.setExpert(false);
            prop_ascending.setHidden(false);
            prop_ascending.setPreferred(false);
            attrib = new AttributeDescriptor("ascending",false,null,true);
            prop_ascending.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_ascending.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_class = new PropertyDescriptor("class",beanClass,"getClass",null);
            prop_class.setDisplayName(resources.getString("ValueBindingSortCriteria_class_DisplayName"));
            prop_class.setShortDescription(resources.getString("ValueBindingSortCriteria_class_Description"));
            prop_class.setExpert(false);
            prop_class.setHidden(true);
            prop_class.setPreferred(false);
            prop_class.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_criteriaKey = new PropertyDescriptor("criteriaKey",beanClass,"getCriteriaKey",null);
            prop_criteriaKey.setDisplayName(resources.getString("ValueBindingSortCriteria_criteriaKey_DisplayName"));
            prop_criteriaKey.setShortDescription(resources.getString("ValueBindingSortCriteria_criteriaKey_Description"));
            prop_criteriaKey.setExpert(false);
            prop_criteriaKey.setHidden(true);
            prop_criteriaKey.setPreferred(false);
            prop_criteriaKey.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_displayName = new PropertyDescriptor("displayName",beanClass,"getDisplayName","setDisplayName");
            prop_displayName.setDisplayName(resources.getString("ValueBindingSortCriteria_displayName_DisplayName"));
            prop_displayName.setShortDescription(resources.getString("ValueBindingSortCriteria_displayName_Description"));
            prop_displayName.setExpert(false);
            prop_displayName.setHidden(false);
            prop_displayName.setPreferred(false);
            prop_displayName.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_requestMapKey = new PropertyDescriptor("requestMapKey",beanClass,"getRequestMapKey","setRequestMapKey");
            prop_requestMapKey.setDisplayName(resources.getString("ValueBindingSortCriteria_requestMapKey_DisplayName"));
            prop_requestMapKey.setShortDescription(resources.getString("ValueBindingSortCriteria_requestMapKey_Description"));
            prop_requestMapKey.setExpert(false);
            prop_requestMapKey.setHidden(false);
            prop_requestMapKey.setPreferred(false);
            prop_requestMapKey.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_valueBinding = new PropertyDescriptor("valueBinding",beanClass,"getValueBinding","setValueBinding");
            prop_valueBinding.setDisplayName(resources.getString("ValueBindingSortCriteria_valueBinding_DisplayName"));
            prop_valueBinding.setShortDescription(resources.getString("ValueBindingSortCriteria_valueBinding_Description"));
            prop_valueBinding.setExpert(false);
            prop_valueBinding.setHidden(false);
            prop_valueBinding.setPreferred(false);
            prop_valueBinding.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            propDescriptors = new PropertyDescriptor[] {
                prop_ascending,
                prop_class,
                prop_criteriaKey,
                prop_displayName,
                prop_requestMapKey,
                prop_valueBinding,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
//GEN-END:BeanInfo
