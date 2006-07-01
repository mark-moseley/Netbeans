/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.actions;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Action for refresh of file systm
*
* @author Jaroslav Tulach
*/
public final class FileSystemRefreshAction extends CookieAction {

    protected Class[] cookieClasses () {
        return new Class[] { DataFolder.class };
    }

    protected void performAction (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            DataFolder df = (DataFolder)nodes[i].getCookie (DataFolder.class);
            if (df != null) {
                FileObject fo = df.getPrimaryFile ();
                fo.refresh ();
            }
        }
    }
    
    protected boolean asynchronous() {
        return false;
    }

    protected int mode () {
        return MODE_ALL;
    }

    public String getName () {
        return NbBundle.getBundle(org.openide.loaders.DataObject.class).getString ("LAB_Refresh");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (FileSystemRefreshAction.class);
    }

}
