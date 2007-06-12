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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.actions;

import java.io.IOException;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.ErrorManager;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Jeri Lockhart
 */
public class SchemaSourceViewOpenAction extends OpenAction{
    private static final long serialVersionUID = 1L;

    /**
     * SchemaSourceViewOpenAction is like OpenAction
     *  but also opens the source view to the line
     *  of the schema component
     *  The name of the action is Edit, not Open
     *
     *  See SchemaViewOpenAction.java for the action named "Open"
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(SchemaSourceViewOpenAction.class, "Edit");
    }

    protected void performAction(Node[] node) {
        if (node == null || node[0] == null){
            return;
        }
        SchemaDataObject sdo = node[0].getLookup().lookup(SchemaDataObject.class);
		if(sdo!=null)
		{
			ViewComponentCookie svc = sdo.getCookie(
					ViewComponentCookie.class);
			if(svc!=null)
			{
				try
				{
					svc.view(ViewComponentCookie.View.SOURCE,
							sdo.getSchemaEditorSupport().getModel().getSchema());
					return;
				}
				catch (IOException ex)
				{
					ErrorManager.getDefault().notify(ex);
				}
			}
		}
		// default to open cookie
        OpenCookie oc = node[0].getCookie(OpenCookie.class);
        if (oc != null){
            oc.open();
        }
    }
}
