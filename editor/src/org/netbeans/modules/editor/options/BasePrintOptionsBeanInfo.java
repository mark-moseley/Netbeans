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

package com.netbeans.developer.modules.text.options;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka, Ales Novak
*/
public class BasePrintOptionsBeanInfo extends SimpleBeanInfo {

  /** Prefix of the icon location. */
  private String iconPrefix;

  /** Icons for compiler settings objects. */
  private Image icon;
  private Image icon32;

  /** Propertydescriptors */
  private static PropertyDescriptor[] descriptors;

  public BasePrintOptionsBeanInfo() {
    this("/com/netbeans/developer/modules/text/resources/baseOptions");
  }

  public BasePrintOptionsBeanInfo(String iconPrefix) {
    this.iconPrefix = iconPrefix;
  }

  /*
  * @return Returns an array of PropertyDescriptors
  * describing the editable properties supported by this bean.
  */
  public PropertyDescriptor[] getPropertyDescriptors () {
    if (descriptors == null) {
      ResourceBundle bundle = NbBundle.getBundle(PlainOptionsBeanInfo.class);
      String[] propNames = getPropNames();
      try {
        descriptors = new PropertyDescriptor[propNames.length];
        
        for (int i = 0; i < propNames.length; i++) {
          descriptors[i] = new PropertyDescriptor(propNames[i], getBeanClass());
          descriptors[i].setDisplayName(bundle.getString("PROP_" + propNames[i]));
          descriptors[i].setShortDescription(bundle.getString("HINT_" + propNames[i]));
        }

        getPD(BasePrintOptions.PRINT_SYSTEM_COLORING_ARRAY_PROP).setPropertyEditorClass(ColoringArrayEditor.class);
        getPD(BasePrintOptions.PRINT_TOKEN_COLORING_ARRAY_PROP).setPropertyEditorClass(ColoringArrayEditor.class);

      } catch (IntrospectionException e) {
        descriptors = new PropertyDescriptor[0];
      }
    }
    return descriptors;
  }

  Class getBeanClass() {
    return BasePrintOptions.class;
  }
  
  String[] getPropNames() {
    return BasePrintOptions.BASE_PROP_NAMES;
  }
  
  PropertyDescriptor getPD(String prop) {
    String[] propNames = getPropNames();
    for (int i = 0; i < descriptors.length; i++) {
      if (prop.equals(propNames[i])) {
        return descriptors[i];
      }
    }
    return null;
  }

  /* @param type Desired type of the icon
  * @return returns the Java loader's icon
  */
  public Image getIcon(final int type) {
    if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
      if (icon == null)
        icon = loadImage(iconPrefix + ".gif");
      return icon;
    }
    else {
      if (icon32 == null)
        icon32 = loadImage(iconPrefix + "32.gif");
      return icon32;
    }
  }
}

/*
* Log
*  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
* $
*/
