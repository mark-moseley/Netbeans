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

package com.netbeans.developer.impl.actions;

import com.netbeans.ide.TopManager;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.actions.ActionPerformer;
import com.netbeans.ide.util.actions.CallableSystemAction;

/** Opens a HTML Browser on the home URL specified in IDESettings.
* (Or activates last opened).
*
* @author Ian Formanek
*/
public class HTMLViewAction extends CallableSystemAction {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 281181711813174400L;

  /** Icon resource.
  * @return name of resource for icon
  */
  protected String iconResource () {
    return "/com/netbeans/developer/impl/resources/actions/htmlView.gif";
  }

  public void performAction() {
    TopManager tm = TopManager.getDefault();
    tm.setStatusText (NbBundle.getBundle(this).getString("CTL_OpeningBrowser"));
    tm.showUrl (com.netbeans.developer.impl.IDESettings.getRealHomeURL ());
    tm.setStatusText ("");
  }

  public String getName() {
    return NbBundle.getBundle(this).getString("HTMLView");
  }

  /** @return the action's help context */
  public HelpCtx getHelpCtx() {
    return new HelpCtx("com.netbeans.developer.docs.Users_Guide.usergd-action", "USERGD-ACTION-TABLE-4");
  }

}

/*
 * Log
 *  7    Gandalf   1.6         3/12/99  David Simonek   
 *  6    Gandalf   1.5         3/6/99   David Simonek   
 *  5    Gandalf   1.4         3/2/99   David Simonek   icons repair
 *  4    Gandalf   1.3         3/1/99   David Simonek   icons etc..
 *  3    Gandalf   1.2         1/21/99  David Simonek   Removed references to 
 *       "Actions" class
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
