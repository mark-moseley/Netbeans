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
package org.netbeans.modules.visualweb.faces.dt_1_2.component;

import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import java.beans.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.faces.*;
import com.sun.rave.designtime.markup.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.UIColumn;

/**
 * The UIColumnBeanInfo class provides design-time meta data for the
 * UIColumn component for use in a visual design tool.
 */
public class UIColumnBeanInfo extends HtmlNonGeneratedBeanInfoBase {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(UIColumnBeanInfo.class);

    /**
     * Construct a <code>UIColumnBeanInfoBase</code> instance
     */
    public UIColumnBeanInfo() {
        beanClass = UIColumn.class;
        iconFileName_C16 = "UIColumn_C16";  //NOI18N
        iconFileName_C32 = "UIColumn_C32";  //NOI18N
        iconFileName_M16 = "UIColumn_M16";  //NOI18N
        iconFileName_M32 = "UIColumn_M32";  //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/html");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "column");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "column");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.RESIZE_CONSTRAINTS, new Integer(Constants.ResizeConstraints.NONE));
            beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS, getFacetDescriptors());
        }
        return beanDescriptor;
    }

    public FacetDescriptor[] getFacetDescriptors() {
        return new FacetDescriptor[] {
            new FacetDescriptor("header"),  //NOI18N
            new FacetDescriptor("footer"),  //NOI18N
        };
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

                PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered", beanClass, "isRendered", "setRendered");  //NOI18N
                prop_rendered.setShortDescription(bundle.getMessage("rendPropShortDesc"));  //NOI18N
                AttributeDescriptor attrib_rendered = new AttributeDescriptor("rendered");  //NOI18N
                attrib_rendered.setBindable(true);
                prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_rendered);

                PropertyDescriptor prop_id = new PropertyDescriptor("id", beanClass, "getId", "setId");  //NOI18N
                prop_id.setShortDescription(bundle.getMessage("idPropShortDesc"));  //NOI18N
                prop_id.setHidden(true);
                AttributeDescriptor attrib_id = new AttributeDescriptor("id");  //NOI18N
                attrib_id.setBindable(false);
                prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_id);

                propertyDescriptors = new PropertyDescriptor[] {
                    prop_rendered,
                    prop_id,
                };

            }
            catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }

        return propertyDescriptors;
    }
}
