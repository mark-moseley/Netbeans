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

package com.netbeans.developer.modules.openfile;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** Opens a file by file chooser. */
public class OpenFileAction extends CallableSystemAction {
  
static final long serialVersionUID =-3424129228987962529L;
  public String getName () {
    return SettingsBeanInfo.getString ("LBL_openFile");
  }
  
  public HelpCtx getHelpCtx () {
    return new HelpCtx (OpenFileAction.class);
  }
  
  protected String iconResource () {
    return "/com/netbeans/developer/modules/openfile/openFile.gif";
  }
  
  /** Last-used directory. */
  private static File currDir = null;
  public void performAction () {
    JFileChooser chooser = new JFileChooser ();
    chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
    if (currDir != null) chooser.setCurrentDirectory (currDir);
    if (chooser.showOpenDialog (null) == JFileChooser.APPROVE_OPTION) {
      OpenFile.open (chooser.getSelectedFile (), false, null, 0);
    }
    currDir = chooser.getCurrentDirectory ();
  }
  
}

/*
 * Log
 *  8    Gandalf   1.7         11/2/99  Jesse Glick     Commented out testing 
 *       code.
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         8/18/99  Jesse Glick     Compilation error.
 *  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         7/10/99  Jesse Glick     Open File module moved 
 *       to core.
 *  3    Gandalf   1.2         7/10/99  Jesse Glick     Splitting server from 
 *       opening functionality, etc.
 *  2    Gandalf   1.1         7/10/99  Jesse Glick     Sundry clean-ups (mostly
 *       bundle usage).
 *  1    Gandalf   1.0         6/25/99  Jesse Glick     
 * $
 */
