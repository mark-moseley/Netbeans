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

package com.netbeans.developer.modules.loaders.form.actions;

import java.net.URL;

import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.actions.CallableSystemAction;

import com.netbeans.developer.modules.loaders.form.palette.BeanInstaller;

/** This action installs new bean into the system.
*
* @author   Petr Hamernik
*/
public class InstallBeanAction extends CallableSystemAction {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 7755319389083740521L;

  /** This method is called by one of the "invokers" as a result of
  * some user's action that should lead to actual "performing" of the action.
  */
  public void performAction() {
    BeanInstaller.installBean();
  }

  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName() {
    return NbBundle.getBundle (InstallBeanAction.class).getString("ACT_InstallBean");
  }

  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx() {
    //PENDING
    return new HelpCtx("com.netbeans.developer.docs.Users_Guide.usergd-action", "USERGD-ACTION-TABLE-3");
  }

  /** Icon resource.
  * @return name of resource for icon
  */
  protected String iconResource () {
    return "/com/netbeans/developer/modules/loaders/form/resources/installBean.gif";
  }
  
}

/*
 * Log
 *  1    Gandalf   1.0         5/17/99  Petr Hamernik   
 * $
 */
