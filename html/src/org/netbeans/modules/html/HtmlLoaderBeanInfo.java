/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.html;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Html data loader bean info.
*
* @author Dafe Simonek
*/
public class HtmlLoaderBeanInfo extends SimpleBeanInfo {

  /** Icons for image data loader. */
  private static Image icon;
  private static Image icon32;

  /** Propertydescriptors */
  private static PropertyDescriptor[] descriptors;

  /** Default constructor
  */
  public HtmlLoaderBeanInfo() {
  }

  /**
  * @return Returns an array of PropertyDescriptors
  * describing the editable properties supported by this bean.
  */
  public PropertyDescriptor[] getPropertyDescriptors () {
    if (descriptors == null) initializeDescriptors();
    return descriptors;
  }

  /** @param type Desired type of the icon
  * @return returns the Image loader's icon
  */
  public Image getIcon(final int type) {
    if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
        (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
      if (icon == null)
        icon = loadImage("/com/netbeans/developer/modules/loaders/html/htmlLoader.gif"); // NOI18N
      return icon;
    } else {
      if (icon32 == null)
        icon32 = loadImage ("/com/netbeans/developer/modules/loaders/html/htmlLoader32.gif"); // NOI18N
      return icon32;
    }
  }

  private static void initializeDescriptors () {
    try {
      final ResourceBundle bundle =
        NbBundle.getBundle(HtmlLoaderBeanInfo.class);

      descriptors =  new PropertyDescriptor[] {
        new PropertyDescriptor ("displayName", HtmlLoader.class, // NOI18N
                                "getDisplayName", null), // NOI18N
        new PropertyDescriptor ("extensions", HtmlLoader.class, // NOI18N
                                "getExtensions", "setExtensions") // NOI18N
      };
      descriptors[0].setDisplayName(bundle.getString("PROP_Name"));
      descriptors[0].setShortDescription(bundle.getString("HINT_Name"));
      descriptors[1].setDisplayName(bundle.getString("PROP_Extensions"));
      descriptors[1].setShortDescription(bundle.getString("HINT_Extensions"));
    } catch (IntrospectionException e) {
      e.printStackTrace ();
    }
  }

}

/*
* Log
*  5    Gandalf   1.4         1/13/00  Ian Formanek    NOI18N
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         2/11/99  Jan Jancura     
*  1    Gandalf   1.0         1/11/99  Jan Jancura     
* $
*/
