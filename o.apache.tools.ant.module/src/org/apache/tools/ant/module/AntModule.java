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

package org.apache.tools.ant.module;

import org.apache.tools.ant.module.run.Hyperlink;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;

public class AntModule extends ModuleInstall {

    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.apache.tools.ant.module"); // NOI18N
    
    @Override
    public void uninstalled () {
        // #14804:
        Hyperlink.detachAllAnnotations();
    }
    
}
