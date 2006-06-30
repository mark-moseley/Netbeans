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

package org.netbeans.modules.project.uiapi;

import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 * Permits {@link ProjectOpenedHook} methods to be called from a different package.
 * @author Jesse Glick
 */
public abstract class ProjectOpenedTrampoline {

    /** The trampoline singleton, defined by {@link ProjectOpenedHook}. */
    public static ProjectOpenedTrampoline DEFAULT;
    {
        Class c = ProjectOpenedHook.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /** Used by {@link ProjectOpenedHook}. */
    protected ProjectOpenedTrampoline() {}
    
    /** Delegates to {@link ProjectOpenedHook#projectOpened}. */
    public abstract void projectOpened(ProjectOpenedHook hook);
    
    /** Delegates to {@link ProjectOpenedHook#projectClosed}. */
    public abstract void projectClosed(ProjectOpenedHook hook);
    
}
