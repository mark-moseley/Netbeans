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

package com.netbeans.developer.impl;

import com.netbeans.ide.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.util.NotImplementedException;
import com.netbeans.ide.nodes.*;
import com.netbeans.developer.impl.desktop.DesktopPoolContext;

/** Important places in the system.
*
* @author Jaroslav Tulach
*/
final class NbPlaces extends Object implements Places, Places.Nodes, Places.Folders {
  /** default */
  private static NbPlaces places;

  /** No instance outside this class.
  */
  private NbPlaces() {
  }

  /** @return the default implementation of places */
  public static NbPlaces getDefault () {
    if (places == null) {
      places = new NbPlaces ();
    }
    return places;
  }

  /** Interesting places for nodes.
  * @return object that holds "node places"
  */
  public Places.Nodes nodes () {
    return this;
  }

  /** Interesting places for data objects.
  * @return interface that provides access to data objects' places
  */
  public Places.Folders folders () {
    return this;
  }

  /** Repository node.
  */
  public Node repository () {
    return DataSystem.getDataSystem ();
  }

  /** Repository node with given DataFilter. */
  public Node repository(DataFilter f) {
    return DataSystem.getDataSystem (f);
  }

  /** Node with all installed loaders.
  */
  public Node loaderPool () {
    return LoaderPoolNode.getLoaderPoolNode ();
  }

  /** Environment node. Place for all transient information about
  * the IDE.
  */
  public Node environment () {
    return EnvironmentNode.getDefault ();
  }
  
  /** Control panel
  */
  public Node controlPanel () {
    return ControlPanelNode.getDefault ();
  }

  /** Node with all desktops */
  public Node desktops () {
    return DesktopPoolContext.getDefault ();
  }

  /** Repository settings */
  public Node repositorySettings () {
    return FSPoolNode.getFSPoolNode ();
  }

  /** Desktop node for current project. This node can change when project changes.
  */
  public Node projectDesktop () {
    return NbProjectOperation.getProject ().projectDesktop ();
  }


  /** Default folder for templates.
  */
  public DataFolder templates () {
    return findSessionFolder ("Templates");
  }

  /** Default folder for toolbars.
  */
  public DataFolder toolbars () {
    return findSessionFolder ("Toolbars");
  }

  /** Default folder for menus.
  */
  public DataFolder menus () {
    return findSessionFolder ("Menus");
  }
    
  /**
   * Returns a DataFolder subfolder of the session folder.  In the DataFolder
   * folders go first (sorted by name) followed by the rest of objects sorted
   * by name.
   */
  private DataFolder findSessionFolder (String name) {
    DataFolder df = null;
    try {
      FileObject fo = FileSystemPool.getDefault().findResource(name);
      df = DataFolder.findFolder(fo);
      df.setSortMode(DataFolder.SortMode.FOLDER_NAMES);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return df;
  }

}

/*
* Log
*  6    Gandalf   1.5         1/25/99  Jaroslav Tulach Added default project, 
*       its desktop and changed default explorer in Main.
*  5    Gandalf   1.4         1/25/99  David Peroutka  support for menus and 
*       toolbars
*  4    Gandalf   1.3         1/20/99  Jaroslav Tulach 
*  3    Gandalf   1.2         1/20/99  David Peroutka  
*  2    Gandalf   1.1         1/6/99   Jan Jancura     
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
