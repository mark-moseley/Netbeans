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
package org.netbeans.modules.visualweb.faces.dt_1_1.component;

import java.beans.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.base.CategoryDescriptors;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.UIParameter;

public class UIParameterBeanInfo extends HtmlNonGeneratedBeanInfoBase {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(UIParameterBeanInfo.class);

    public UIParameterBeanInfo() {
        super();
        beanClass = UIParameter.class;
        iconFileName_C16 = "UIParameter_C16.gif"; //NOI18N
        iconFileName_C32 = "UIParameter_C32.gif"; //NOI18N
        iconFileName_M16 = "UIParameter_M16.gif"; //NOI18N
        iconFileName_M32 = "UIParameter_M32.gif"; //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            beanDescriptor.setDisplayName(bundle.getMessage("UIParameter_DisplayName")); //NOI18N
            beanDescriptor.setShortDescription(bundle.getMessage("UIParameter_Description")); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/core"); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_PREFIX, "f");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "param"); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "param"); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER, Boolean.FALSE);
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_f_param");
        }
        return beanDescriptor;
    }

    PropertyDescriptor[] propertyDescriptors;

    /**
     * Returns the PropertyDescriptor array which describes
     * the property meta-data for this JavaBean
     *
     * @return An array of PropertyDescriptor objects
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            try {
                PropertyDescriptor prop_id = new PropertyDescriptor("id", beanClass, "getId", "setId"); //NOI18N
                prop_id.setHidden(true);
                AttributeDescriptor attrib_id = new AttributeDescriptor("id"); //NOI18N
                attrib_id.setBindable(false);
                prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_id);

                PropertyDescriptor prop_name = new PropertyDescriptor("name", beanClass, "getName", "setName"); //NOI18N
                AttributeDescriptor attrib_name = new AttributeDescriptor("name"); //NOI18N
                attrib_name.setBindable(true);
                prop_name.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_name);
                prop_name.setPropertyEditorClass(com.sun.rave.propertyeditors.StringPropertyEditor.class);
                prop_name.setValue(Constants.PropertyDescriptor.CATEGORY, CategoryDescriptors.DATA);

                PropertyDescriptor prop_value = new PropertyDescriptor("value", beanClass, "getValue", "setValue"); //NOI18N
                AttributeDescriptor attrib_value = new AttributeDescriptor("value"); //NOI18N
                attrib_value.setBindable(true);
                prop_value.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_value);
                prop_value.setPropertyEditorClass(com.sun.rave.propertyeditors.StringPropertyEditor.class);
                prop_value.setValue(Constants.PropertyDescriptor.CATEGORY, CategoryDescriptors.DATA);
                
                propertyDescriptors = new PropertyDescriptor[] {
                    prop_id,
                    prop_name,
                    prop_value
                };
            } catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }
        
        return propertyDescriptors;
    }
    
}
