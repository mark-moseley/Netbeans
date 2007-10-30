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

package org.netbeans.modules.websvc.api;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesSupportProviderTest extends NbTestCase {
    
    private FileObject datadir;
    private FileObject ws;
    private FileObject nows;
    
    static {
        CustomWebServicesSupportProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    /** Creates a new instance of CustomWebServicesSupportProviderTest */
    public CustomWebServicesSupportProviderTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File f = getWorkDir();
        assertTrue("work dir exists", f.exists());
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (f);
        Repository.getDefault ().addFileSystem (lfs);
        datadir = FileUtil.toFileObject(f);
        assertNotNull("no FileObject", datadir);
        ws = datadir.createData("custom", "ws");
        assertNotNull("no ws FileObject", ws);
        nows = datadir.createData("custom", "nows");
        assertNotNull("no ws FileObject", nows);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ws.delete();
        nows.delete();
    }
    
    public void testProviders() throws Exception {
        Lookup.Result res = Lookup.getDefault().lookup(new Lookup.Template(WebServicesSupportProvider.class));
        assertEquals("there should be 1 instance - the one from tests", 1, res.allInstances ().size ());
    }
    
    public void testGetWebServicesSupport() throws Exception {
        WebServicesSupport ws1 = WebServicesSupport.getWebServicesSupport(ws);
        assertNotNull("found ws support", ws1);
        WebServicesSupport ws2 = WebServicesSupport.getWebServicesSupport(nows);
        assertNull("not found ws support", ws2);
    }
}
