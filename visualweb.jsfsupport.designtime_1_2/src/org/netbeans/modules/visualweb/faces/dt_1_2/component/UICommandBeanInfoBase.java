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

package org.netbeans.modules.visualweb.faces.dt_1_2.component;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;
import com.sun.rave.faces.event.Action;
import java.beans.EventSetDescriptor;
import java.lang.reflect.Method;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;


public abstract class UICommandBeanInfoBase extends UIComponentBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_2.component.Bundle", Locale.getDefault(),
            UICommandBeanInfoBase.class.getClassLoader());

    public UICommandBeanInfoBase() {
        beanClass = javax.faces.component.UICommand.class;
        defaultPropertyName = "value";
        iconFileName_C16 = "/com/sun/rave/faces/dt_1_2/component/UICommand_C16";
        iconFileName_C32 = "/com/sun/rave/faces/dt_1_2/component/UICommand_C32";
        iconFileName_M16 = "/com/sun/rave/faces/dt_1_2/component/UICommand_M16";
        iconFileName_M32 = "/com/sun/rave/faces/dt_1_2/component/UICommand_M32";
    }


    private PropertyDescriptor[] propertyDescriptors;

    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_actionExpression = new PropertyDescriptorBase("actionExpression",beanClass,"getAction","setAction");
            prop_actionExpression.setDisplayName(resources.getString("UICommand_actionExpression_DisplayName"));
            prop_actionExpression.setShortDescription(resources.getString("UICommand_actionExpression_Description"));
            prop_actionExpression.setPropertyEditorClass(com.sun.rave.propertyeditors.MethodBindingPropertyEditor.class);
            prop_actionExpression.setExpert(false);
            prop_actionExpression.setHidden(true);
            prop_actionExpression.setPreferred(false);
            attrib = new AttributeDescriptor("action",false,null,true);
            prop_actionExpression.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_actionExpression.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_actionListener = new PropertyDescriptorBase("actionListener",beanClass,"getActionListener","setActionListener");
            prop_actionListener.setDisplayName(resources.getString("UICommand_actionListener_DisplayName"));
            prop_actionListener.setShortDescription(resources.getString("UICommand_actionListener_Description"));
            prop_actionListener.setPropertyEditorClass(com.sun.rave.propertyeditors.MethodBindingPropertyEditor.class);
            prop_actionListener.setExpert(false);
            prop_actionListener.setHidden(false);
            prop_actionListener.setPreferred(false);
            attrib = new AttributeDescriptor("actionListener",false,null,true);
            prop_actionListener.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_actionListener.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_immediate = new PropertyDescriptorBase("immediate",beanClass,"isImmediate","setImmediate");
            prop_immediate.setDisplayName(resources.getString("UICommand_immediate_DisplayName"));
            prop_immediate.setShortDescription(resources.getString("UICommand_immediate_Description"));
            prop_immediate.setExpert(false);
            prop_immediate.setHidden(false);
            prop_immediate.setPreferred(false);
            attrib = new AttributeDescriptor("immediate",false,null,true);
            prop_immediate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_immediate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_value = new PropertyDescriptorBase("value",beanClass,"getValue","setValue");
            prop_value.setDisplayName(resources.getString("UICommand_value_DisplayName"));
            prop_value.setShortDescription(resources.getString("UICommand_value_Description"));
            prop_value.setPropertyEditorClass(org.netbeans.modules.visualweb.faces.dt.std.ValueBindingPropertyEditor.class);
            prop_value.setExpert(false);
            prop_value.setHidden(false);
            prop_value.setPreferred(false);
            attrib = new AttributeDescriptor("value",false,null,true);
            prop_value.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_value.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_value.setValue("ignoreIsBound", "true");

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_actionExpression);
            propertyDescriptorList.add(prop_actionListener);
            propertyDescriptorList.add(prop_immediate);
            propertyDescriptorList.add(prop_value);

            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }
    
    private EventSetDescriptor[] eventSetDescriptors;
    
    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSetDescriptors == null) {
            try {
                eventSetDescriptors = new EventSetDescriptor[] {
                    new EventSetDescriptor("action", Action.class,  //NOI18N
                            new Method[] {Action.class.getMethod("action", new Class[] {})},  //NOI18N
                            null, null),
                    new EventSetDescriptor("actionListener", ActionListener.class,  //NOI18N
                            new Method[] {ActionListener.class.getMethod("processAction", new Class[] {ActionEvent.class})},  //NOI18N
                            null, null)
                };
                eventSetDescriptors[0].setValue(Constants.EventSetDescriptor.BINDING_PROPERTY, getPropertyDescriptor("actionExpression"));  //NOI18N
                String defaultHandler = resources.getString("UICommand_actionHandler"); // NOI18N
                eventSetDescriptors[0].setValue(Constants.EventDescriptor.DEFAULT_EVENT_BODY, defaultHandler);
                eventSetDescriptors[1].setValue(Constants.EventSetDescriptor.BINDING_PROPERTY, getPropertyDescriptor("actionListener"));  //NOI18N
                eventSetDescriptors[1].setHidden(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return eventSetDescriptors;
    }

}

