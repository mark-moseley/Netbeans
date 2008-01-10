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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.autoupdate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import org.netbeans.api.autoupdate.TestUtils.CustomItemsProvider;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Radek Matous
 */
public class DefaultTestCase extends NbTestCase {
    private static File catalogFile;
    private static URL catalogURL;
    protected boolean modulesOnly = true;
    protected List<UpdateUnit> keepItNotToGC;
    public DefaultTestCase(String testName) {
        super(testName);
    }
        
    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", catalogURL, UpdateUnitProvider.CATEGORY.STANDARD);
        }
    }

    public void populateCatalog(InputStream is) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(catalogFile);
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
            os.close();
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir ();
        catalogFile = new File(getWorkDir(), "updates.xml");
        if (!catalogFile.exists()) {
            catalogFile.createNewFile();
        }
        catalogURL = catalogFile.toURI().toURL();        
        populateCatalog(TestUtils.class.getResourceAsStream("data/updates.xml"));
        
        TestUtils.setUserDir (getWorkDirPath ());
        TestUtils.testInit();
        
        MainLookup.register(new MyProvider());
        MainLookup.register(new CustomItemsProvider());
        MainLookup.register(new InstallIntoNewClusterTest.NetBeansClusterCreator());
        assert Lookup.getDefault().lookup(MyProvider.class) != null;
        assert Lookup.getDefault().lookup(CustomItemsProvider.class) != null;
        UpdateUnitProviderFactory.getDefault().refreshProviders (null, true);
        
        File pf = new File (new File (getWorkDir(), "platform"), "installdir");
        pf.mkdirs ();
        new File (pf, "config").mkdir();
        TestUtils.setPlatformDir (pf.toString ());
        if (modulesOnly) {
            keepItNotToGC = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        } else {
            keepItNotToGC = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        }
            
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
