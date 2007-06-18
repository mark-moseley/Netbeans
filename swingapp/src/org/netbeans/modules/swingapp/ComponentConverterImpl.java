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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.swingapp;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JRootPane;
import org.netbeans.modules.form.ComponentConverter;

/**
 *
 */
public class ComponentConverterImpl implements ComponentConverter {

    public Class getDesignClass(String componentClassName) {
//        if (application.SingleFrameApplication.SingleFrameApplicationView.class.getName()
//                .equals(componentClassName)) {
//            return SingleFrameApplicationView.class;
//        }
        return null;
    }

    public Class getDesignClass(Class componentClass) {
        if (application.View.class.isAssignableFrom(componentClass)) {
            return SingleFrameApplicationView.class;
        } else {
            return null;
        }
    }

    public static class SingleFrameApplicationView extends application.SingleFrameApplication.SingleFrameApplicationView {
        private JRootPane rootPane;

        public SingleFrameApplicationView() {
            super(null);
        }

        public JRootPane getRootPane() {
            if (rootPane == null) {
                rootPane = new JRootPane();
            }
            return rootPane;
        }
    }

    public static class SingleFrameApplicationViewBeanInfo extends SimpleBeanInfo {
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                return new PropertyDescriptor[] {
                    new PropertyDescriptor("component", SingleFrameApplicationView.class), // NOI18N
                    new PropertyDescriptor("menuBar", SingleFrameApplicationView.class), // NOI18N
                    new PropertyDescriptor("toolBar", SingleFrameApplicationView.class), // NOI18N
                    new PropertyDescriptor("statusBar", SingleFrameApplicationView.class) // NOI18N
                };
            } catch (IntrospectionException ex) {
                Logger.getLogger(ComponentConverterImpl.class.getName()).log(Level.INFO, null, ex);
                return null;
            }
        }
    }
}
