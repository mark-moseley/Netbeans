/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.j2ee.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/**
 * Used to call "Start in Debug Mode" popup menu item on server node or
 * "org.netbeans.modules.j2ee.deployment.impl.ui.actions.StartDebugAction".
 * 
 * @author Martin.Schovanek@sun.com
 * @see org.netbeans.jellytools.actions.Action
 */
public class StartDebugAction extends Action {
    
    private static final String popupPath = Bundle.getStringTrimmed(
            "org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle",
            "LBL_Debug");
    
    /** Creates new StartDebugAction instance. */
    public StartDebugAction() {
        super(null, popupPath, 
                "org.netbeans.modules.j2ee.deployment.impl.ui.actions.DebugAction");
    }
}
