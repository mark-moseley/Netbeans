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

package org.netbeans.installer.event.dialog.console;


import com.installshield.event.*;
import com.installshield.event.ui.*;
import com.installshield.wizard.console.*;
import com.installshield.wizard.service.system.*;
import com.installshield.util.*;

public class PanelLogoutConsoleImpl {

    private static final String LOGOUT_TEXT =
        "$L(com.installshield.wizardx.i18n.WizardXResources, LogoutPanel.text)";

    public void queryEnterLogout(ISQueryContext context) {

        SystemUtilService service = null;
        try {
            service =
                (SystemUtilService)context.getService(SystemUtilService.NAME);
            context.setReturnValue(service.isLogoutRequired());
        }
        catch (Throwable e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    public void consoleInteractionLogout(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        tty.printLine();
        tty.printLine(
            context.getServices().resolveString(LOGOUT_TEXT));
    }

}
