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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.util.Map;

import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek
 */
public class ModuleProviderTest extends NbTestCase {
    
    public ModuleProviderTest (String testName) {
        super (testName);
    }
    
    public void testGetModules () {
        Map<String, ModuleInfo> map = InstalledModuleProvider.getInstalledModules ();
        assertNotNull ("Some modules found.", map);
        assertFalse ("Some modules, not empty", map.isEmpty ());
    }
    
}
