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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateInfoParserTest;
import org.netbeans.modules.autoupdate.updateprovider.LocalNBMsProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitFactoryTest extends NbTestCase {
    
    public UpdateUnitFactoryTest (String testName) {
        super (testName);
    }
    
    private UpdateProvider p = null;
    private static File NBM_FILE = null;
    
    protected void setUp () throws IOException, URISyntaxException {
        clearWorkDir ();
        System.setProperty ("netbeans.user", getWorkDirPath ());
        Lookup.getDefault ().lookup (ModuleInfo.class);
        try {
            p = new MyProvider ();
        } catch (Exception x) {
            x.printStackTrace ();
        }
        p.refresh (true);
        URL urlToFile = AutoupdateInfoParserTest.class.getResource ("data/org-yourorghere-depending.nbm");
        NBM_FILE = new File (urlToFile.toURI ());
        assertNotNull ("data/org-yourorghere-depending.nbm file must found.", NBM_FILE);
    }
    
    public void testAppendInstalledModule () {
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, ModuleInfo> modules = InstalledModuleProvider.getInstalledModules ();
        assertNotNull ("Some modules are installed.", modules);
        assertFalse ("Some modules are installed.", modules.isEmpty ());
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (unitImpls,InstalledModuleProvider.getDefault ());
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        assertEquals ("Same size of installed modules and UpdateUnit (except FeatureElement).", modules.size (), newImpls.size ());
    }
    
    public void testAppendUpdateItems () throws IOException {
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (unitImpls, p);
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        assertEquals ("Same size of installed modules and UpdateUnit", updates.size (), newImpls.size ());
    }
    
    public void testGroupInstalledAndUpdates () {
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateUnit> installedImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (unitImpls,InstalledModuleProvider.getDefault ());
        Map<String, UpdateUnit> updatedImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (installedImpls, p);
        boolean isInstalledAndHasUpdates = false;
        for (String id : updatedImpls.keySet ()) {
            UpdateUnit impl = updatedImpls.get (id);
            UpdateElement installed = impl.getInstalled ();
            List<UpdateElement> updates = impl.getAvailableUpdates ();
            isInstalledAndHasUpdates = isInstalledAndHasUpdates || installed != null && updates != null && ! updates.isEmpty ();
            if (installed != null && updates != null && ! updates.isEmpty ()) {
                assertTrue ("Updates of module " + id + " contain newer one.", updates.get (0).getSpecificationVersion ().compareTo (installed.getSpecificationVersion ()) > 0);
            }
        }
        assertTrue ("Some module is installed and has updates.", isInstalledAndHasUpdates);
    }
    
    public void testGetUpdateUnitsInNbmFile () {
        UpdateProvider localFilesProvider = new LocalNBMsProvider ("test-local-file-provider", NBM_FILE);
        assertNotNull ("LocalNBMsProvider found for file " + NBM_FILE, localFilesProvider);
        Map<String, UpdateUnit> units = UpdateUnitFactory.getDefault().getUpdateUnits (localFilesProvider);
        assertNotNull ("UpdateUnit found in provider " + localFilesProvider.getDisplayName (), units);
        assertEquals ("Provider providers only once unit in provider (XXX added a artificial feature!)" + localFilesProvider.getName (), 2, units.size ());
        String id = units.keySet ().iterator ().next ();
        assertNotNull (localFilesProvider.getName () + " gives UpdateUnit.", units.get (id));
        UpdateUnit u = units.get (id);
        assertNull ("Unit is not installed.", u.getInstalled ());
        assertNotNull ("Unit has update.", u.getAvailableUpdates ());
        assertEquals ("Unit has only one update.", 1, u.getAvailableUpdates ().size ());
        UpdateElement el = u.getAvailableUpdates ().get (0);
        assertEquals ("org.yourorghere.depending", el.getCodeName ());
        assertEquals ("1.0", el.getSpecificationVersion ());
        assertEquals (NBM_FILE.length(), el.getDownloadSize ());
    }
    
    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", UpdateUnitFactoryTest.class.getResource ("data/catalog.xml"));
        }
    }
    
}
