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

import org.openide.loaders.DataLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Utilities;

public class DataLoaderPool {

    /** Method used in [Folder|Instance]LoaderBeanInfo for making 'extensions' property r/o 
     * instead of r/w as it is declaredi in UniFileDataLoaderBeanInfo. The rest of property
     * descriptors declared in UniFileDataLoaderBeanInfo are untouched.
     */
    private static PropertyDescriptor[] supportGetPropertyDescriptors () {
        PropertyDescriptor[] descriptors = null;

        try {
            SimpleBeanInfo uniFileLoader = (SimpleBeanInfo) Introspector.getBeanInfo (UniFileLoader.class);
            if (uniFileLoader != null) {
                descriptors = uniFileLoader.getPropertyDescriptors();
                if (descriptors != null) {
                    for (int i = 0; i < descriptors.length; i++) {
                        if (descriptors[i].getName().equals("extensions")) { // NOI18N
                            descriptors[i].setWriteMethod(null);
                        }
                    }
                }
            }
        } 
        catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
        return descriptors;
    }
    
    public static class FolderLoaderBeanInfo extends SimpleBeanInfo {
        
        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                // FolderLoader bean info uses MultiFileLoader's bean info instead of
                // UniFileLoader's one. That is why it is necessary to remove 'extensions'
                // property (declared in UniFileLoaderBeanInfo).
                // Currently this property is only addition to MultiFileLoader bean info
                // provided by UniFileLoaderBeanInfo.
                return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public PropertyDescriptor[] getPropertyDescriptors () {
            return supportGetPropertyDescriptors();
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("/org/openide/resources/defaultFolder.gif"); // NOI18N
            } else {
                return Utilities.loadImage("/org/openide/resources/defaultFolder32.gif"); // NOI18N
            }
        }
    }

    public static class InstanceLoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                // InstanceLoader bean info uses MultiFileLoader's bean info instead of
                // UniFileLoader's one. That is why it is necessary to change 'extensions'
                // property from r/w (declared in UniFileLoaderBeanInfo) to r/o property.
                // Currently this property is only addition to MultiFileLoader bean info
                // provided by UniFileLoaderBeanInfo.
                return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public PropertyDescriptor[] getPropertyDescriptors () {
            return supportGetPropertyDescriptors();
        }
        
        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("/org/netbeans/core/resources/action.gif"); // NOI18N
            } else {
                return Utilities.loadImage ("/org/netbeans/core/resources/action32.gif"); // NOI18N
            }
        }

    }

    public static class DefaultLoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage ("/org/openide/resources/pending.gif"); // NOI18N
            } else {
                return Utilities.loadImage ("/org/openide/resources/pending32.gif"); // NOI18N
            }
        }

    }

    public static class ShadowLoaderBeanInfo extends SimpleBeanInfo {
        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public PropertyDescriptor[] getPropertyDescriptors () {
            try {
                // Hide the actions property from users, since shadows inherit actions anyway:
                Class c = Class.forName ("org.openide.loaders.DataLoaderPool$ShadowLoader"); // NOI18N
                PropertyDescriptor actions = new PropertyDescriptor ("actions", c); // NOI18N
                actions.setHidden (true);
                return new PropertyDescriptor[] { actions };
            } catch (ClassNotFoundException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("/org/openide/resources/actions/copy.gif"); // NOI18N
            } else {
                // [PENDING]
                //return Utilities.loadImage ("/org/openide/resources/actions/copy32.gif"); // NOI18N
                return null;
            }
        }

    }

}
