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

package org.netbeans.modules.xml.wsdl.ui.actions;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLEditorSupport;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * Opens the WSDLDataObject in the WSDL editor tree view.
 *
 * @author Jeri Lockhart
 */
public class WSDLViewOpenAction extends NodeAction{
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
	protected void performAction(Node[] node) {
        if (node == null || node[0] == null) {
            return;
        }
        WSDLDataObject sdo = node[0].getLookup().lookup(
                WSDLDataObject.class);
        if (sdo != null) {
            WSDLEditorSupport wes = sdo.getWSDLEditorSupport();
            ViewComponentCookie svc = sdo.getCookie(
                    ViewComponentCookie.class);
            if (svc != null) {
                WSDLModel model= wes.getModel();
                Definitions def = null;
                ViewComponentCookie.View view = ViewComponentCookie.View.STRUCTURE;
                if (model.getState() ==  Model.State.VALID) {
                    def = model.getDefinitions();
                }
                if (def == null) {
                    view = ViewComponentCookie.View.SOURCE;
                }
                
                if (wes.getOpenedPanes() == null ||
                        wes.getOpenedPanes().length == 0) {
                    svc.view(view, def);
                } else {
                    wes.open();
                }                
            	return;
            }
        }
        // default to open cookie
        OpenCookie oc = node[0].getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }

    @Override
	protected boolean enable(Node[] node) {
        return true;
    }

    @Override
	public String getName() {
        return NbBundle.getMessage(WSDLViewOpenAction.class,
                "WSDLViewOpenAction_Name");
    }

    @Override
	public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
	protected boolean asynchronous() {
        return false;
    }
}
