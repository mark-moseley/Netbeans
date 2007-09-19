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

package org.netbeans.modules.form.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.windows.WindowManager;
import org.openide.windows.TopComponent;
import org.netbeans.modules.form.FormEditorSupport;

/**
 * Action that invokes reloading of the currently active form. Presented only
 * in contextual menus within the Form Editor.
 *
 * @author Tomas Pavek
 */

public class ReloadAction extends CallableSystemAction {

    private static String name;

    public ReloadAction() {
        setEnabled(true);
    }

    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(ReloadAction.class)
                     .getString("ACT_ReloadForm"); // NOI18N
        return name;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.quickref"); // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public void performAction() {
        WindowManager wm = WindowManager.getDefault();        
        TopComponent activeTC = wm.getRegistry().getActivated();
        if(activeTC==null) {
            return;
        }
        
        FormEditorSupport fes = FormEditorSupport.getFormEditor(activeTC);
        if (fes != null)
            fes.reloadForm();
    }
}
