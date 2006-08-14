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
package org.netbeans.modules.project.libraries;

import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.netbeans.spi.project.libraries.LibraryProvider;

/**
 * Ensures that all {@link LibraryProvider}s are actually loaded.
 * Some of them may perform initialization actions, such as updating
 * $userdir/build.properties with concrete values of some library paths.
 * This needs to happen before any Ant build is run.
 * @author Tomas Zezula
 */
public class LibrariesModule extends ModuleInstall {

    public void restored() {
        super.restored();
        for (LibraryProvider lp : Lookup.getDefault().lookupAll(LibraryProvider.class)) {
            //XXX: Workaround of lookup non reentrant issue (#49405)            
            //Library can not do an initialization in its constructor
            //For promo-E the LibraryProvider should be extended by init method
            lp.getLibraries();
        }
    }
    
}
