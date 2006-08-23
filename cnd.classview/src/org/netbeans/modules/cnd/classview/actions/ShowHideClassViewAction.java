/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.classview.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.*;

import org.openide.util.HelpCtx;
//import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.cnd.classview.ClassViewTopComponent;

/**
 * Shows/Hides class view pane
 * @author Vladimir Kvashin
 */
public class ShowHideClassViewAction extends CallableSystemAction {
	
	public ShowHideClassViewAction() {
		putValue(NAME, I18n.getMessage("CTL_ClassViewAction")); // NOI18N
		putValue(SHORT_DESCRIPTION, I18n.getMessage("HINT_ClassViewAction")); // NOI18N
	}
	
    public String getName() {
		return (String) getValue(NAME);
    }

    public void actionPerformed(ActionEvent ev) {
		System.err.println("ShowHideClassViewAction.actionPerformed");
		performAction();
    }

    public void performAction() {
		System.err.println("ShowHideClassViewAction.performAction");
		TopComponent tc = ClassViewTopComponent.findDefault();
		tc.open();
		tc.requestActive();
	}

    public HelpCtx getHelpCtx() {
		return null;
    }

    protected String iconResource() {
		return "org/netbeans/modules/cnd/classview/resources/class_view.png";
    }
	
}
