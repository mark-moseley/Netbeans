/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.output;

import org.openide.util.*;

import java.awt.Image;

import java.beans.*;

import java.util.*;

import org.netbeans.modules.collab.core.Debug;


public class OutputCollabletFactoryBeanInfo extends SimpleBeanInfo {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private PropertyDescriptor[] descriptors;
    private Image smallIcon;
    private Image largeIcon;

    /**
     *
     *
     */
    public OutputCollabletFactoryBeanInfo() {
        super();
    }

    /**
     *
     *
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (descriptors == null) {
            List descriptorList = new LinkedList();

            try {
                PropertyDescriptor descriptor = null;

                // class
                descriptor = new PropertyDescriptor("class", // NOI18N
                        OutputCollabletFactory.class, "getClass", null
                    ); // NOI18N
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        OutputCollabletFactoryBeanInfo.class, "PROP_OutputCollabletFactoryBeanInfo_class_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        OutputCollabletFactoryBeanInfo.class, "PROP_OutputCollabletFactoryBeanInfo_class_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // displayName
                descriptor = new PropertyDescriptor(
                        "displayName", // NOI18N
                        OutputCollabletFactory.class, "getDisplayName", null
                    ); // NOI18N
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        OutputCollabletFactoryBeanInfo.class,
                        "PROP_OutputCollabletFactoryBeanInfo_displayName_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        OutputCollabletFactoryBeanInfo.class,
                        "PROP_OutputCollabletFactoryBeanInfo_displayName_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // identifier
                descriptor = new PropertyDescriptor(
                        "identifier", // NOI18N
                        OutputCollabletFactory.class, "getIdentifier", null
                    ); // NOI18N
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        OutputCollabletFactoryBeanInfo.class,
                        "PROP_OutputCollabletFactoryBeanInfo_identifier_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        OutputCollabletFactoryBeanInfo.class,
                        "PROP_OutputCollabletFactoryBeanInfo_identifier_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);
            } catch (IntrospectionException e) {
                Debug.debugNotify(e);
            }

            descriptors = (PropertyDescriptor[]) descriptorList.toArray(new PropertyDescriptor[descriptorList.size()]);
        }

        return descriptors;
    }

    /**
     *
     *
     */
    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (smallIcon == null) {
                smallIcon = Utilities.loadImage(
                    //					"org/netbeans/modules/collab/channel/output/resources/output_png.gif"); // NOI18N
                    "org/netbeans/core/resources/frames/output.gif"
                    ); //NOI18N
            }

            return smallIcon;
        } else {
            if (largeIcon == null) {
                largeIcon = Utilities.loadImage(
                    //					"org/netbeans/modules/collab/channel/output/resources/output_png.gif"); // NOI18N
                    "org/netbeans/core/resources/frames/output.gif"
                    ); //NOI18N
            }

            return largeIcon;
        }
    }
}
