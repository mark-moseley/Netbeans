/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo;

import java.beans.*;

import org.openide.ServiceType;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** BeanInfo for ServiceType. Has name property.
* @author Jesse Glick
*/
public class ServiceTypeBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor name = new PropertyDescriptor ("name", ServiceType.class); // NOI18N
            name.setDisplayName (NbBundle.getBundle (ServiceTypeBeanInfo.class).getString ("PROP_ServiceType_name"));
            name.setShortDescription (NbBundle.getBundle (ServiceTypeBeanInfo.class).getString ("HINT_ServiceType_name"));
            // Is there an easier way to prevent this from appearing??
            PropertyDescriptor helpCtx = new PropertyDescriptor ("helpCtx", ServiceType.class, "getHelpCtx", null); // NOI18N
            helpCtx.setHidden (true);
            return new PropertyDescriptor[] { name, helpCtx };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }

}
