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

package com.netbeans.developer.modules.loaders.properties;

import com.netbeans.ide.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.windows.*;
import com.netbeans.ide.actions.OpenAction;
import com.netbeans.ide.text.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.Node;
import com.netbeans.ide.nodes.Children;


/** Object that provides main functionality for properties data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Ian Formanek
*/
public final class PropertiesDataObject extends MultiDataObject {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 4795737295255253334L;


  /** Icon base for the PropertiesNode node */
  private static final String PROPERTIES_ICON_BASE =
    "com/netbeans/developer/modules/loaders/properties/propertiesObject";
  
  public PropertiesDataObject (final FileObject obj, final MultiFileLoader loader)
                       throws DataObjectExistsException {
    super(obj, loader);
    // use editor support
    EditorSupport es = new EditorSupport(getPrimaryEntry());
    es.setMIMEType ("text/plain");
    getCookieSet().add(es);
  }

  /** Provides node that should represent this data object. When a node for representation
  * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
  * with only parent changed. This implementation creates instance
  * <CODE>DataNode</CODE>.
  * <P>
  * This method is called only once.
  *
  * @return the node representation for this data object
  * @see DataNode
  */
  protected Node createNodeDelegate () {
    DataNode dn = new DataNode(this, Children.LEAF);
    dn.setIconBase(PROPERTIES_ICON_BASE);
    dn.setDefaultAction (SystemAction.get(OpenAction.class));
    return dn;
  }

  /** Help context for this object.
  * @return help context
  */
  public com.netbeans.ide.util.HelpCtx getHelpCtx () {
    return new com.netbeans.ide.util.HelpCtx ("com.netbeans.developer.docs.Users_Guide.usergd-using-div-12", "USERGD-USING-TABLE-2");
  }

}

/*
 * <<Log>>
 *  6    Gandalf   1.5         5/11/99  Ian Formanek    Undone last change to 
 *       compile
 *  5    Gandalf   1.4         5/11/99  Petr Jiricka    
 *  4    Gandalf   1.3         3/9/99   Ian Formanek    Moved images to this 
 *       package
 *  3    Gandalf   1.2         2/3/99   Jaroslav Tulach Inner class for node is 
 *       not needed
 *  2    Gandalf   1.1         1/22/99  Ian Formanek    
 *  1    Gandalf   1.0         1/22/99  Ian Formanek    
 * $
 */
