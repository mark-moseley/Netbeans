/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ProjectWebModuleProvider implements WebModuleProvider {
    
    public ProjectWebModuleProvider () {
    }
    
    public WebModule findWebModule (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof WebProject) {
            WebProject wp = (WebProject) project;
            FileObject src = wp.getSourceDirectory ();
            FileObject web = wp.getWebModule ().getDocumentBase ();
            if (src.equals (file) || web.equals (file) || FileUtil.isParentOf (src, file) || FileUtil.isParentOf (web, file)) {
                return WebModuleFactory.createWebModule (wp.getWebModule ());
            }
            FileObject build = wp.getWebModule().getBuildDirectory();
            if (build != null) {
                if (build.equals (file) || FileUtil.isParentOf (build, file)) {
                    return WebModuleFactory.createWebModule (wp.getWebModule ());
                }
            }
        }
        return null;
    }
    
}
