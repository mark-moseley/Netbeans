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

import java.awt.Image;
import java.beans.*;

import org.openide.loaders.MultiFileLoader;
import org.openide.util.Utilities;

public class XMLDataObject {

    public static class LoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
            } catch (IntrospectionException ie) {
                org.openide.ErrorManager.getDefault().notify(ie);
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("org/openide/resources/xmlObject.gif"); // NOI18N
            } else {
                return Utilities.loadImage ("org/openide/resources/xmlObject32.gif"); // NOI18N
            }
        }

    }

}
