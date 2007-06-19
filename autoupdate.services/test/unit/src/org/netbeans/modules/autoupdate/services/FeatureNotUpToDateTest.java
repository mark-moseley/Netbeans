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

import org.netbeans.modules.autoupdate.updateprovider.*;
import org.netbeans.api.autoupdate.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class FeatureNotUpToDateTest extends NbTestCase

{

    private static File catalogFile;
    private static URL catalogURL;
    protected boolean modulesOnly = true;
    List<UpdateUnit> keepItNotToGC;

    public FeatureNotUpToDateTest (String testName) {
        super (testName);
    }

    public static class MyProvider implements UpdateProvider {

        public String getName () {
            return FeatureNotUpToDateTest.class.getName ();
        }

        public String getDisplayName () {
            return getName ();
        }

        public String getDescription () {
            return getName ();
        }

        public Map<String, UpdateItem> getUpdateItems () throws IOException {
            Map<String, UpdateItem> items = InstalledModuleProvider.getDefault ().getUpdateItems ();
            assertNotNull ("Installed modules must found.", items);
            int size = items.size ();
            assertTrue ("Count of installed modules are more then once.", size > 1);
            String pilotName = items.keySet ().iterator ().next ();
            assertNotNull (pilotName + "must found", items.get (pilotName));
            UpdateItem pilotItem = items.get (pilotName);
            assertNotNull ("Impl of " + pilotItem + " available", Trampoline.SPI.impl (pilotItem));
            UpdateItemImpl pilotItemImpl = Trampoline.SPI.impl (pilotItem);
            assertTrue ("Impl of " + pilotItem + "is ModuleItem", pilotItemImpl instanceof ModuleItem);
            ModuleItem pilotModuleItem = (ModuleItem) pilotItemImpl;
            SpecificationVersion pilotSV = new SpecificationVersion (pilotModuleItem.getSpecificationVersion ());
            assertTrue ("a dot is present in " + pilotSV, pilotSV.toString ().indexOf ('.') != -1);
            int dot = pilotSV.toString ().indexOf ('.');
            String postSpec = pilotSV.toString ().substring (dot + 1);
            String preSpec = pilotSV.toString ().substring (0, dot);
            Integer digit = 0;
            try {
                digit = Integer.parseInt (preSpec) + 1;
            } catch (NumberFormatException nfe) {
                fail (nfe.getLocalizedMessage ());
            }
            SpecificationVersion higherSV = new SpecificationVersion (digit + "." + postSpec);
            assertTrue (higherSV + " is more then " + pilotSV, higherSV.compareTo (pilotSV) > 0);
            String higherDep = pilotModuleItem.getModuleInfo ().getCodeNameBase () + " > " + higherSV;

            Set<String> deps = new HashSet<String> (items.size ());
            for (String id : items.keySet ()) {
                String dep;
                if (! pilotName.equals (id)) {
                    UpdateItem item = items.get (id);
                    assertNotNull ("Impl of " + item + " available", Trampoline.SPI.impl (item));
                    UpdateItemImpl itemImpl = Trampoline.SPI.impl (item);
                    assertTrue ("Impl of " + item + "is ModuleItem", itemImpl instanceof ModuleItem);
                    ModuleItem moduleItem = (ModuleItem) itemImpl;
                    dep = moduleItem.getModuleInfo ().getCodeNameBase () + " > " + moduleItem.getSpecificationVersion ();
                } else {
                    dep = higherDep;
                }
                deps.add (dep);
            }
            Map<String, UpdateItem> res = InstalledModuleProvider.getDefault ().getUpdateItems ();
            ModuleInfo info = pilotModuleItem.getModuleInfo ();
            UpdateItemImpl higherItemImpl = new InstalledModuleItem (
                    info.getCodeNameBase (),
                    higherSV.toString (),
                    new HackedModuleInfo (info, higherSV),
                    "",
                    null, // XXX author
                    null, // installed cluster
                    null);
            UpdateItem higherModuleItem = Utilities.createUpdateItem (higherItemImpl);
            
            res.put ("testFeatueVsStandaloneModules",
                    UpdateItem.createFeature (
                        "testFeatueVsStandaloneModules",
                        "1.0",
                        deps,
                        null,
                        null,
                        null));
            res.put (pilotName, higherModuleItem);
            return res;
        }

        public boolean refresh (boolean force) throws IOException {
            return true;
        }
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        this.clearWorkDir ();
        TestUtils.setUserDir (getWorkDirPath ());
        TestUtils.testInit ();
        MockServices.setServices (MyProvider.class);
        assert Lookup.getDefault ().lookup (MyProvider.class) != null;
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);
    }

    public void testNoUpToDateFeature () {
        assertNotNull ("A feature found.", UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.FEATURE));
        List<UpdateUnit> units = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.FEATURE);
        assertEquals ("Only once feature there.", 1, units.size ());
        UpdateUnit feature = units.get (0);
        assertNotNull (feature + " is installed.", feature.getInstalled ());
        assertFalse (feature + " has some available updates.", feature.getAvailableUpdates ().isEmpty ());
    }
    
    final static class HackedModuleInfo extends ModuleInfo {
        private ModuleInfo info;
        private SpecificationVersion hackedVersion;
        
        public HackedModuleInfo (ModuleInfo info, SpecificationVersion hackedVersion) {
            this.info = info;
            this.hackedVersion = hackedVersion;
        }

        public String getCodeNameBase () {
            return info.getCodeNameBase ();
        }

        public int getCodeNameRelease () {
            return info.getCodeNameRelease ();
        }

        public String getCodeName () {
            return info.getCodeName ();
        }

        public SpecificationVersion getSpecificationVersion () {
            return hackedVersion;
        }

        public boolean isEnabled () {
            return info.isEnabled ();
        }

        public Object getAttribute (String attr) {
            return info.getAttribute (attr);
        }

        public Object getLocalizedAttribute (String attr) {
            return info.getLocalizedAttribute (attr);
        }

        public Set<Dependency> getDependencies () {
            return info.getDependencies ();
        }

        public boolean owns (Class<?> clazz) {
            return info.owns (clazz);
        }
        
    }
}
