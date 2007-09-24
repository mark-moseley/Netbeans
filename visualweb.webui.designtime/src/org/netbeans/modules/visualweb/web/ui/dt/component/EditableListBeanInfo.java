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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.validator.Validator;
import com.sun.rave.designtime.Constants;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.EditableList} component.
 */
public class EditableListBeanInfo extends EditableListBeanInfoBase {

    /** Creates a new instance of EditableListBeanInfo */
    public EditableListBeanInfo() {
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        PropertyDescriptor[] descriptors = this.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals("valueChangeListener")) //NOI18N
                descriptors[i].setHidden(true);
        }
    }

    private EventSetDescriptor[] eventSetDescriptors;

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            if (eventSetDescriptors == null) {
                eventSetDescriptors = new EventSetDescriptor[] {
                        new EventSetDescriptor("valueChangeListener", ValueChangeListener.class,  //NOI18N
                                new Method[] {
                                    ValueChangeListener.class.getMethod("processValueChange",  //NOI18N
                                        new Class[] {ValueChangeEvent.class})},
                                null, null)
                };
            }
            PropertyDescriptor[] propertyDescriptors = this.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                if (propertyDescriptors[i].getName().equals("valueChangeListener")) { //NOI18N
                    eventSetDescriptors[0].setValue(Constants.EventSetDescriptor.BINDING_PROPERTY,
                            propertyDescriptors[i]);
                    eventSetDescriptors[0].setShortDescription(propertyDescriptors[i].getShortDescription());
                }
            }
            return eventSetDescriptors;
        } catch (Exception e) {
            return null;
        }
    }

    public int getDefaultEventIndex() {
        return 0;
    }
}
