/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html;

import org.openide.actions.OpenAction;
import org.openide.actions.ViewAction;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Node that represents HTML data object.
 *
 * @author  Radim Kubacki
 */
public class HtmlDataNode extends org.openide.loaders.DataNode {
    
    /** Creates new HtmlDataNode */
    public HtmlDataNode (DataObject dobj, Children ch) {
        super (dobj, ch);
        setShortDescription (NbBundle.getMessage(HtmlDataNode.class, "LBL_htmlNodeShortDesc"));
    }

    /** Get the default action for this node - 
     *  no action for templates, 
     *  ViewAction when data object is mounted on javadoc filesystem,
     *  OpenAction otherwise.
     *
     * @return default action, or <code>null</code> if there should be none
     */
    public SystemAction getDefaultAction () {
        if (getDataObject ().isTemplate ())
            return null;
            
        try {
            if (getDataObject ().getPrimaryFile ().getFileSystem ().getCapability ().capableOf (FileSystemCapability.DOC))
                return SystemAction.get (ViewAction.class);
            else
                return SystemAction.get (OpenAction.class);
        }
        catch (FileStateInvalidException exc) {
            return SystemAction.get (OpenAction.class);
        }
    }
}
