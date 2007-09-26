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
package org.netbeans.updatecenters.install;

import org.netbeans.api.autoupdate.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Jaromir Uhrik
 */

public class InstallAllPluginsTest extends NbTestCase {

    private static String primaryCatalogLocation = null;
    private static String secondaryCatalogLocation = null;

    private static URL primaryURL = null; //static URL representation of primaryCatalogLocation
    private static URL secondaryURL = null; //static URL representation of secondaryCatalogLocation
    static List<UpdateUnit> availableUnits = null;

    
    static List<UpdateElement> installedPlugins = null;
    static List<UpdateElement> newPlugins = null;
    static List<UpdateElement> updatePlugins = null;
    private static boolean useConfigFile = false;//false by default
    
    UpdateElement currentElementToInstall = null;
            
    static {
        setUpProperties();
    }

    public static class PrimaryCatalogProvider extends AutoupdateCatalogProvider {

        public PrimaryCatalogProvider() {
            super("primary uc", "Primary Update Center", primaryURL);
        }
    }
    public static class SecondaryCatalogProvider extends AutoupdateCatalogProvider {

        public SecondaryCatalogProvider() {
            super("secondary uc", "Secondary Update Center", secondaryURL);
        }
    }
    
    public static void setUpProperties() {
        String useThisConfig = System.getProperty("useThisConfig");
        if(useThisConfig != null){
            if(useThisConfig.equalsIgnoreCase("true") || useThisConfig.equalsIgnoreCase("yes")){
                useConfigFile = true;
                primaryCatalogLocation = System.getProperty("primaryCatalogLocation");
                secondaryCatalogLocation = System.getProperty("secondaryCatalogLocation");
            }
        }
        acceptConfigFile(useConfigFile);
        if (primaryCatalogLocation == null) {
            fail("System property 'primaryCatalogLocation' hasn't been set");
        }
        try {
            primaryURL = new URL(primaryCatalogLocation);
            if (secondaryCatalogLocation != null) {
                secondaryURL = new URL(secondaryCatalogLocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InstallAllPluginsTest(String testName, UpdateElement element) {
        super(testName);
        this.currentElementToInstall = element;
    }



    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        TestUtils.setUserDir(getWorkDirPath());
        File pf = new File(new File(getWorkDir(), "platform"), "installdir");
        pf.mkdirs();
        new File(pf, "config").mkdir();
        TestUtils.setPlatformDir(pf.toString());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        log("\n******* AFTER INSTALL *******");
        logElementsInfo();
    }

    public static NbTestSuite suite() throws Exception {
        TestUtils.testInit();
        if(secondaryURL == null){
            MockServices.setServices(PrimaryCatalogProvider.class);
        }else{
            MockServices.setServices(PrimaryCatalogProvider.class, SecondaryCatalogProvider.class);
        }
        assert Lookup.getDefault().lookup(PrimaryCatalogProvider.class) != null;        
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
        availableUnits = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        initLists();
        readLists();
        NbTestSuite suite = new NbTestSuite();
        int testNumber = 0;
        for (UpdateElement updateElement : newPlugins) {
            //if (updateElement.getDisplayName().startsWith("Maven Embedder library")){//i < 10) {
                suite.addTest(new InstallAllPluginsTest("test" + getModuleCanonicalName(updateElement, testNumber), updateElement));
            //}
            testNumber++;
        }
        return suite;
    }

    @Override
    protected void runTest() throws Throwable {
        log("\n*******BEFORE INSTALL********");
        logElementsInfo();
        log("running test for: " + currentElementToInstall.getDisplayName());
        log("plugin codename: " + currentElementToInstall.getCodeName());
        installPlugin(currentElementToInstall);
    }

    public static void initLists() {
        installedPlugins = null;
        installedPlugins = new ArrayList<UpdateElement>();
        newPlugins = null;
        newPlugins = new ArrayList<UpdateElement>();
        updatePlugins = null;
        updatePlugins = new ArrayList<UpdateElement>();
    }

    public static void readLists() {
        for (UpdateUnit updateUnit : availableUnits) {
            UpdateElement element = updateUnit.getInstalled();
            if (updateUnit.getInstalled() != null) {
                //all plugins that are installed or available updates
                List<UpdateElement> availableUpdates = updateUnit.getAvailableUpdates();
                if (!availableUpdates.isEmpty()) {
                    //updatePlugins
                    updatePlugins.add(availableUpdates.get(0));
                } else {
                    //installedPlugins
                    installedPlugins.add(element);
                }
            } else {
                //newPlugins (available not installed plugins)
                List<UpdateElement> availableUpdates = updateUnit.getAvailableUpdates();
                if (!availableUpdates.isEmpty()) {
                    newPlugins.add(availableUpdates.get(0));
                }
            }
        }
    }


    public void installPlugin(UpdateElement updateElement) {
        if (!newPlugins.contains(updateElement)) {
            if (installedPlugins.contains(updateElement)) {
                return;
            } else {
                fail("Plugin '" + updateElement.getDisplayName() + "' is not available for install.");
                return;
            }
        }
        OperationContainer<InstallSupport> install = OperationContainer.createForInstall();
        OperationInfo info = install.add(updateElement);
        log("Broken dependencies:");
        log(info.getBrokenDependencies().toString());
        moveElement(newPlugins, installedPlugins, updateElement);
        Set<UpdateElement> reqElements = info.getRequiredElements();
        log("List of required plugins is:");            
        for (UpdateElement reqElm : reqElements) {
            if (newPlugins.contains(reqElm)) {
                moveElement(newPlugins, installedPlugins, reqElm);
                info = install.add(reqElm);
                log(reqElm.getDisplayName());
            } else {
                if (updatePlugins.contains(reqElm)) {
                    info = install.add(reqElm);
                    moveElement(updatePlugins, installedPlugins, reqElm);
                } else {
                    if (installedPlugins.contains(reqElm)) {
                        info = install.add(reqElm);
                    } else {
                        fail("Required element '" + reqElm + "' is not available");
                    }
                }
            }
        }


        assertTrue("Dependencies broken for plugin '" + updateElement.getDisplayName() + "'." + info.getBrokenDependencies().toString(), info.getBrokenDependencies().size() == 0);
        assertTrue("List of invalid is empty.", install.listInvalid().isEmpty());
        InstallSupport is = install.getSupport();
        try {
            Validator v = is.doDownload(null, false);
            Installer installer;
            try {
                installer = is.doValidate(v, null);
                Restarter r = is.doInstall(installer, null);
                if (r == null) {
                } else {
                    is.doRestartLater(r); //is.doRestart(r, null);
                }
            } catch (OperationException operationException) {
                operationException.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void moveElement(List<UpdateElement> from, List<UpdateElement> to, UpdateElement element) {
        from.remove(element);
        to.add(element);
    }

    private static String getModuleCanonicalName(UpdateElement element, int number) {
        String displayName = element.getDisplayName();
        StringBuffer displayNameBuffer = new StringBuffer(displayName);
        for (int i = 0; i < displayNameBuffer.length(); i++) {
            if (displayNameBuffer.charAt(i) < 65 || displayNameBuffer.charAt(i) > 122) {
                displayNameBuffer.setCharAt(i, '_');
            }
        }
        return "_" + number + "_" + displayNameBuffer.toString();
    }

    public static void main(java.lang.String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    
    //-----------------------------------------------------------------------
    //following code  is just for purpose of running inside of NetBeans IDE
    //-----------------------------------------------------------------------
    public static void acceptConfigFile(boolean accept) {
        if (accept) {
            return;
        }
        System.out.println("");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!! CONFIG FILE IS NOT USED FOR THIS RUN !!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("");

        primaryCatalogLocation = "http://www.netbeans.org/updates/dev_1.22_.xml.gz";
        secondaryCatalogLocation = "http://bits.netbeans.org/download/6.0/nightly/latest/uc/catalog.xml.gz";
    }

    public void logElementsInfo() {
        log("NEW ELEMENTS:" + newPlugins.size());
        log("INSTALLED ELEMENTS:" + installedPlugins.size());
        log("UPDATE ELEMENTS:" + updatePlugins.size());
    }
}
