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

package org.netbeans.modules.beans;

import java.awt.Image;
import java.beans.*;

import org.openide.util.NbBundle;

/** Description of {@link PropertyActionSettings}.
 *
 * @author Petr Suchomel
 */
public class PropertyActionSettingsBeanInfo extends SimpleBeanInfo {
    
    private PropertyDescriptor[] descriptors = null;
    private BeanDescriptor descr = null;
    
    public BeanDescriptor getBeanDescriptor() {
        
        if( descr == null ){
            descr = new BeanDescriptor(org.netbeans.modules.beans.PropertyActionSettings.class);
            descr.setDisplayName(NbBundle.getBundle(PropertyActionSettingsBeanInfo.class).getString("PROP_Option_Menu"));
            descr.setValue ("version", "1.1"); // NOI18N            
            descr.setValue("global", Boolean.FALSE); // now it is global     
        }                
        return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        if( descriptors == null ){
            try {
                PropertyDescriptor access = new PropertyDescriptor("genAccess", PropertyActionSettings.class);
                access.setDisplayName(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "PROP_Option_Gen_Access"));
                access.setShortDescription(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "HINT_Option_Gen_Access"));
                access.setPropertyEditorClass (AccessEditor.class);

                PropertyDescriptor bound = new PropertyDescriptor("genBound", PropertyActionSettings.class);
                bound.setDisplayName(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "PROP_Option_Gen_Bound"));
                bound.setShortDescription(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "HINT_Option_Gen_Bound"));

                PropertyDescriptor constrained = new PropertyDescriptor("genConstrained", PropertyActionSettings.class);
                constrained.setDisplayName(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "PROP_Option_Gen_Constrained"));
                constrained.setShortDescription(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "HINT_Option_Gen_Constrained"));

                PropertyDescriptor indexed = new PropertyDescriptor("genIndexed", PropertyActionSettings.class);
                indexed.setDisplayName(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "PROP_Option_Gen_Indexed"));
                indexed.setShortDescription(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "HINT_Option_Gen_Indexed"));

                PropertyDescriptor inherit = new PropertyDescriptor("useInherit", PropertyActionSettings.class);
                inherit.setDisplayName(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "PROP_Option_Use_Inherit"));
                inherit.setShortDescription(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "HINT_Option_Use_Inherit"));
                
                PropertyDescriptor askBefore = new PropertyDescriptor("askBeforeGen", PropertyActionSettings.class);
                askBefore.setDisplayName(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "PROP_Option_Ask_Before_Generating"));
                askBefore.setShortDescription(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "HINT_Option_Ask_Before_Generating"));
                askBefore.setHidden(true);  //will be set to false ASAP I'll have right panel
                
                descriptors = new PropertyDescriptor[] { access, bound, constrained, indexed, inherit, askBefore};
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                    ie.printStackTrace ();
                return null;
            }
        }
        return descriptors;
    }

    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/beans/resources/beansSetting.gif");
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage("/org/netbeans/modules/beans/resources/beansSetting.gif");
            return icon32;
        }
    }

    public static class AccessEditor extends PropertyEditorSupport {

        private static final String[] tags = {
            NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "MSG_Option_Gen_Getter"),
            NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "MSG_Option_Gen_Setter"),
            NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "MSG_Option_Gen_Both")
        };

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            int type = ((Integer) getValue ()).intValue ();
            
            switch( type ){
                case PropertyPattern.READ_ONLY:
                    return tags[0];
                case PropertyPattern.WRITE_ONLY:
                    return tags[1];
                case PropertyPattern.READ_WRITE:
                    return tags[2];
            }
            return tags[0];
        }

        public void setAsText (String text) throws IllegalArgumentException {
            if (tags[0].equals (text))
                setValue (new Integer(PropertyPattern.READ_ONLY));
            else if (tags[1].equals (text))
                setValue (new Integer(PropertyPattern.WRITE_ONLY));
            else if (tags[2].equals (text))
                setValue (new Integer(PropertyPattern.READ_WRITE));
            else
                throw new IllegalArgumentException ();
        }

    }

}
