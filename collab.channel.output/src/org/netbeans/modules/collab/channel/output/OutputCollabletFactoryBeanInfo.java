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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
