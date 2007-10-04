/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.freeform;

import org.netbeans.modules.web.api.webmodule.*;
import org.openide.filesystems.FileObject;

/**
 * Check that the web module is found for various files and has correct properties.
 * @author Pavel Buzek
 */
public class ModulesWebTest extends TestBaseWeb {

    public ModulesWebTest (String name) {
        super(name);
    }

    public void testGetWebModule() throws Exception {
        WebModule forJsp = WebModule.getWebModule (helloWorldJsp);
        assertNotNull ("find web module for:" + helloWorldJsp.getPath (), forJsp);
        FileObject dd = jakarta.getProjectDirectory ().getFileObject ("web/WEB-INF/web.xml");
        WebModule forDD = WebModule.getWebModule (dd);
        assertNotNull ("find web module for:" + dd.getPath (), forDD);
        WebModule forServlet =  WebModule.getWebModule (helloWorldServlet);
        assertNotNull ("find web module for:" + helloWorldServlet.getPath (), forServlet);
        
        assertEquals ("same web modules for servlet ("+ forServlet.getDocumentBase ()+") and jsp ("+forJsp.getDocumentBase ()+")", forServlet.getDocumentBase (), forJsp.getDocumentBase ());
        assertEquals ("same web modules for servlet ("+ forServlet.getDocumentBase ()+") and we.xml ("+forDD.getDocumentBase ()+")", forServlet.getDocumentBase (), forDD.getDocumentBase ());
        assertEquals ("same web modules for jsp ("+ forJsp.getDocumentBase ()+") and web.xml ("+forDD.getDocumentBase ()+")", forJsp.getDocumentBase (), forDD.getDocumentBase ());
        WebModule forBuildXml = WebModule.getWebModule (jakarta.getProjectDirectory ().getFileObject ("build.xml"));
        assertNull ("WebModule found for build.xml which does not belong to web module", forBuildXml);
    }
    
    public void testWebModuleProperties () throws Exception {
        WebModule wm = WebModule.getWebModule (jakarta.getProjectDirectory ().getFileObject ("web"));
        assertNotNull ("find web module for doc root", wm);
	assertEquals ("correct j2ee version", WebModule.J2EE_14_LEVEL, wm.getJ2eePlatformVersion ());
        assertEquals ("correct context path", "/myapp", wm.getContextPath ());
        assertEquals ("correct context path", jakarta.getProjectDirectory ().getFileObject ("web"), wm.getDocumentBase ());
    }
    
}
