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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import java.beans.BeanDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.web.ui.component.Property;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.PropetySheetSection}
 * component.
 */
public class PropertySheetSectionBeanInfo extends PropertySheetSectionBeanInfoBase {

    /** Creates a new instance of PropertySheetSectionBeanInfo */
    public PropertySheetSectionBeanInfo() {
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        beanDescriptor.setValue(Constants.BeanDescriptor.PREFERRED_CHILD_TYPES,
                new String[] {Property.class.getName()});

        // Not yet working right
        //beanDescriptor.setValue(
        //    Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES,
        //    new String[] { "*label://div[@class='ConFldSetLgdDiv']" }); // NOI18N
    }

}
