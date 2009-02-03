/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous
 */
public abstract class OperationsTestImpl extends DefaultTestCase {
    //{fileDataCreated, fileDeleted}
    private Boolean[] fileChanges = {false, false, false};
    private Thread[] fileChangeThreads = {null,null,null};
    private Exception[] exceptions = {null,null,null};
    
    private FileChangeListener fca;
    private FileObject modulesRoot;
    
    private Map<String, ModuleInfo> getModuleInfos () {
        return InstalledModuleProvider.getInstalledModules ();
    }
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        getModuleInfos ();
        FileUtil.getConfigRoot().getFileSystem().refresh (false);
        modulesRoot = FileUtil.getConfigFile("Modules"); // NOI18N
        fca = new FileChangeAdapter (){
            @Override
            public void fileDataCreated (FileEvent fe) {
                fileChanges[0] = true;
                fileChangeThreads[0] = Thread.currentThread ();
                exceptions[0] = new Exception ();
            }

            @Override
            public void fileChanged(FileEvent fe) {
                fileChanges[2] = true;
                fileChangeThreads[2] = Thread.currentThread ();
                exceptions[2] = new Exception ();
            }

            
            @Override
            public void fileDeleted (FileEvent fe) {
                fileChanges[1] = true;
                fileChangeThreads[1] = Thread.currentThread ();
                exceptions[1] = new Exception ();
            }
        };
        modulesRoot.addFileChangeListener (fca);
    }
    
    @Override
    protected void tearDown () throws Exception {
        super.tearDown ();
        if (modulesRoot != null && fca != null) {
            modulesRoot.removeFileChangeListener (fca);
        }
    }
    
    public OperationsTestImpl (String testName) {
        super (testName);
    }
    
    public abstract void testSelf () throws Exception;
    
    //    static List<UpdateUnit> getUpdateUnits() {
    //        UpdateManager mgr = UpdateManager.getDefault();
    //        assertNotNull(mgr);
    //        List<UpdateUnit> retval =  mgr.getUpdateUnits();
    //        assertNotNull(retval);
    //        assertTrue(retval.size() > 0);
    //        return retval;
    //    }
    //
    public UpdateElement installModule (UpdateUnit toInstall, UpdateElement installElement) throws Exception {
        return installModuleImpl (toInstall, installElement, true);
    }
    
    public UpdateElement installNativeComponent (UpdateUnit toInstall, UpdateElement installElement) throws Exception {
        installElement = (installElement != null) ? installElement : toInstall.getAvailableUpdates ().get (0);
        
        assertNotNull (toInstall);
        
        // XXX: assert same could be broken later
        assertSame (toInstall, Utilities.toUpdateUnit (toInstall.getCodeName ()));
        
        OperationContainer<OperationSupport> container = OperationContainer.createForCustomInstallComponent ();
        OperationContainer.OperationInfo<OperationSupport> info = container.add (installElement);
        assertNotNull (info);
        container.add (info.getRequiredElements ());
        assertEquals (0,container.listInvalid ().size ());
        
        OperationSupport support = container.getSupport ();
        assertNotNull (support);
        support.doOperation (null);
        
        return installElement;
    }

    boolean incrementNumberOfModuleConfigFiles() {
        return true;
    }
    
    /*public void installModuleDirect(UpdateUnit toInstall) throws Exception {
        installModuleImpl(toInstall, false);
    }*/
    
    
    private UpdateElement installModuleImpl (UpdateUnit toInstall, UpdateElement installElement, final boolean installSupport) throws Exception {
        fileChanges = new Boolean[]{false, false, false};
        installElement = (installElement != null) ? installElement : toInstall.getAvailableUpdates ().get (0);
        File f = InstallManager.findTargetDirectory(installElement.getUpdateUnit ().getInstalled (), Trampoline.API.impl(installElement),false);
        File configModules = new File (f, "config/Modules");
        File modules = new File (f, "modules");
        int configModulesSize = (configModules.listFiles () != null) ? configModules.listFiles ().length : 0;
        int modulesSize = (modules.listFiles () != null) ? modules.listFiles ().length : 0;
        assertFalse (fileChanges[0]);
        FileObject foConfigModules = FileUtil.getConfigFile("Modules");
        assertNotNull (foConfigModules);
        int foConfigModulesSize = foConfigModules.getChildren ().length;
        assertNull (getModuleInfos ().get (toInstall.getCodeName ()));
        
        assertNotNull (toInstall);
        
        assertSame (toInstall, Utilities.toUpdateUnit (toInstall.getCodeName ()));
        
        OperationContainer container2 = null;
        OperationSupport.Restarter r = null;
        if (installSupport) {
            OperationContainer<InstallSupport> container = OperationContainer.createForInstall ();
            container2 = container;
            OperationContainer.OperationInfo<InstallSupport> info = container.add (installElement);
            assertNotNull (info);
            container.add (info.getRequiredElements ());
            boolean hiddenUpdate = false;
            for (OperationInfo<InstallSupport> i : container.listAll ()) {
                if (i.getUpdateUnit ().getInstalled () != null) {
                    hiddenUpdate = true;
                    break;
                }
            }
            InstallSupport support = container.getSupport ();
            assertNotNull (support);
            
            InstallSupport.Validator v = support.doDownload (null, false);
            assertNotNull (v);
            InstallSupport.Installer i = support.doValidate (v, null);
            assertNotNull (i);
            assertNull (support.getCertificate (i, installElement)); // Test NBM is not signed nor certificate
            assertFalse (support.isTrusted (i, installElement));
            assertFalse (support.isSigned (i, installElement));
            if (hiddenUpdate) {
                try {
                    r = support.doInstall (i, null);
                } catch (OperationException ex) {
                    if (OperationException.ERROR_TYPE.INSTALL == ex.getErrorType ()) {
                        // can ingore
                        // module system cannot load the module either
                    } else {
                        fail (ex.toString ());
                    }
                }
                assertNotNull ("If there is hiddenUpdate then returns Restarer.", r);
            } else {
                try {
                    assertNull ("No Restarer when no hidden update.", support.doInstall (i, null));
                } catch (OperationException ex) {
                    if (OperationException.ERROR_TYPE.INSTALL == ex.getErrorType ()) {
                        // can ingore
                        // module system cannot load the module either
                    } else {
                        fail (ex.toString ());
                    }
                }
            }
        } else {
            OperationContainer<OperationSupport> container = OperationContainer.createForDirectInstall ();
            container2 = container;
            OperationContainer.OperationInfo<OperationSupport> info = container.add (installElement);
            assertNotNull (info);
            container.add (info.getRequiredElements ());
            assertEquals (0,container.listInvalid ().size ());
            List all =  container.listAll ();
            
            OperationSupport support = container.getSupport ();
            assertNotNull (support);
            support.doOperation (null);
        }
        
        assertNotNull (toInstall.getInstalled ());
        
        if (r == null) {
            assertTrue ("Config module files are more than before Install test, " + Arrays.asList (configModules.listFiles ()), configModules.listFiles ().length > configModulesSize);
            assertTrue ("Installed modules are more than before Install test, " + Arrays.asList (modules.listFiles ()), modules.listFiles ().length > modulesSize);
            if (incrementNumberOfModuleConfigFiles()) {
                assertTrue (foConfigModules.getPath (), foConfigModules.getChildren ().length > foConfigModulesSize);
                assertEquals (configModules.listFiles ()[0], FileUtil.toFile (foConfigModules.getChildren ()[0]));
            }

            if (incrementNumberOfModuleConfigFiles()) {
                assertTrue (fileChanges[0]);
            } else {
                assertTrue(fileChanges[2]);
            }
            fileChanges[0]=false;
        }
        
        //TODO: to know why Thread.sleep(3000) must be present in these tests
        /*if (!Thread.currentThread().equals(fileChangeThreads[0])) {
            exceptions[0].printStackTrace();
        }
        assertEquals(Thread.currentThread(),fileChangeThreads[0]);
         */
        
        fileChangeThreads[0]=null;
        //if (! customInstaller) Thread.sleep(3000);
        @SuppressWarnings("unchecked")
        List<OperationContainer.OperationInfo> all = container2.listAll ();
        for (OperationContainer.OperationInfo oi : all) {
            UpdateUnit toInstallUnit = oi.getUpdateUnit ();
            if (Trampoline.API.impl (toInstallUnit) instanceof ModuleUpdateUnitImpl) {
                assertInstalledModule (toInstallUnit);
            } else if (Trampoline.API.impl (toInstallUnit) instanceof FeatureUpdateUnitImpl) {
                FeatureUpdateUnitImpl fi = (FeatureUpdateUnitImpl) Trampoline.API.impl (toInstallUnit);
                assertNotNull ("Feature " + toInstallUnit + " is installed now.", fi.getInstalled ());
                FeatureUpdateElementImpl fe = (FeatureUpdateElementImpl) Trampoline.API.impl (fi.getInstalled ());
                for (ModuleUpdateElementImpl m : fe.getContainedModuleElements ()) {
                    assertInstalledModule (m.getUpdateUnit ());
                }
            }
        }
        return installElement;
    }
    
    private void assertInstalledModule (UpdateUnit toInstallUnit) throws InterruptedException {
        ModuleInfo info = getModuleInfos ().get (toInstallUnit.getCodeName ());
        assertNotNull (info);
        int timeout = 250;
        while (! info.isEnabled () && timeout-- > 0) {
            Thread.sleep (10);
        }
        assertTrue (info.getCodeNameBase (), info.isEnabled ());
        assertNotNull (Utilities.toModule (toInstallUnit.getCodeName (), null));
        assertTrue (Utilities.toModule (toInstallUnit.getCodeName (), null).isEnabled ());
    }
    
    final UpdateElement updateModule (UpdateUnit toUpdate) throws Exception {
        return updateModule (toUpdate, true);
    }
    
    final void updateModuleDirect (UpdateUnit toUpdate) throws Exception {
        updateModule (toUpdate, false);
    }
    
    private UpdateElement updateModule (UpdateUnit toUpdate, final boolean installlSupport) throws Exception {
        File configModules = new File (getWorkDir (), "config/Modules");
        File modules = new File (getWorkDir (), "modules");
        assertFalse (fileChanges[0]);
        FileObject foConfigModules = FileUtil.getConfigFile("Modules");
        assertNotNull (foConfigModules);
        assertTrue (configModules.listFiles () != null && configModules.listFiles ().length != 0);
        assertTrue (modules.listFiles () != null && modules.listFiles ().length != 0);
        assertFalse (fileChanges[0]);
        assertNotNull (getModuleInfos ().get (toUpdate.getCodeName ()));
        
        assertNotNull (toUpdate);
        
        assertSame (toUpdate, Utilities.toUpdateUnit (toUpdate.getCodeName ()));
        
        UpdateElement upEl =  toUpdate.getAvailableUpdates ().get (0);
        assertNotSame (toUpdate.getInstalled (), upEl);
        
        OperationContainer container2 = null;
        if (installlSupport) {
            OperationContainer<InstallSupport> container = OperationContainer.createForUpdate ();
            container2 = container;
            assertNotNull (container.add (upEl));
            InstallSupport support = container.getSupport ();
            assertNotNull (support);
            
            InstallSupport.Validator v = support.doDownload (null, false);
            assertNotNull (v);
            InstallSupport.Installer i = support.doValidate (v, null);
            assertNotNull (i);
            //assertNotNull(support.getCertificate(i, upEl));
            assertFalse (support.isTrusted (i, upEl));
            assertFalse (support.isSigned (i, upEl));
            OperationSupport.Restarter r = null;
            try {
                r = support.doInstall (i, null);
            } catch (OperationException ex) {
                if (OperationException.ERROR_TYPE.INSTALL == ex.getErrorType ()) {
                    // can ingore
                    // module system cannot load the module either
                } else {
                    fail (ex.toString ());
                }
            }
            if (r != null) {
                support.doRestartLater (r);
            }
        } else {
            OperationContainer<OperationSupport> container = OperationContainer.createForDirectUpdate ();
            container2 = container;
            assertNotNull (container.add (upEl));
            OperationSupport support = container.getSupport ();
            support.doOperation (null);
        }
        //UpdateUnitProviderFactory.getDefault ().refreshProviders (null, false);
        assertNotNull (toUpdate.getInstalled ());
        if (! toUpdate.isPending ()) {
            assertSame(toUpdate.getInstalled(), upEl);
        }
        // XXX need a separated test, mixing two tests together
        UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        UpdateUnit uu = UpdateManagerImpl.getInstance ().getUpdateUnit (toUpdate.getCodeName ());
        assertNotNull (uu);
        assertEquals (toUpdate.toString (), uu.toString ());
        assertTrue ("UpdateUnit before update and after update are equals.", toUpdate.equals (uu));
        if (! toUpdate.isPending ()) {
            assertTrue(toUpdate.getAvailableUpdates().isEmpty());
        }
        
        
        @SuppressWarnings("unchecked")
        List<OperationContainer.OperationInfo> all = container2.listAll ();
        for (OperationContainer.OperationInfo oi : all) {
            UpdateUnit toUpdateUnit = oi.getUpdateUnit ();
            assertNotNull (getModuleInfos ().get (toUpdateUnit.getCodeName ()));
            ModuleInfo info = getModuleInfos ().get (toUpdateUnit.getCodeName ());
            assertNotNull (info);
            assertTrue (info.isEnabled ());
            assertNotNull (Utilities.toModule (toUpdateUnit.getCodeName (), null));
            assertTrue (Utilities.toModule (toUpdateUnit.getCodeName (), null).isEnabled ());
        }
        
        return upEl;
    }
    
    void disableModule (UpdateUnit toDisable) throws Exception {
        FileObject fo = FileUtil.getConfigFile("Modules");
        File f = new File (getWorkDir (), "config/Modules");
        File f2 = new File (getWorkDir (), "modules");
        assertTrue (f.listFiles () != null && f.listFiles ().length != 0);
        assertTrue (f2.listFiles () != null && f2.listFiles ().length != 0);
        assertFalse (fileChanges[0]);
        assertNotNull (getModuleInfos ().get (toDisable.getCodeName ()));
        
        assertNotNull (toDisable);
        
        assertSame (toDisable, Utilities.toUpdateUnit (toDisable.getCodeName ()));
        
        OperationContainer<OperationSupport> container = OperationContainer.createForDirectDisable ();
        assertNotNull (container.add (toDisable.getInstalled ()));
        OperationSupport support = container.getSupport ();
        assertNotNull (support);
        support.doOperation (null);
        assertNotNull (toDisable.getInstalled ());
        
        assertTrue (f.listFiles () != null && f.listFiles ().length != 0);
        assertTrue (f2.listFiles () != null && f2.listFiles ().length != 0);
        assertEquals (1, fo.getChildren ().length);
        assertEquals (f.listFiles ()[0], FileUtil.toFile (fo.getChildren ()[0]));
        
        assertNotNull (getModuleInfos ().get (toDisable.getCodeName ()));
        ModuleInfo info = getModuleInfos ().get (toDisable.getCodeName ());
        assertNotNull (info);
        assertFalse (info.isEnabled ());
        assertNotNull (Utilities.toModule (toDisable.getCodeName (), null));
        assertFalse (Utilities.toModule (toDisable.getCodeName (), null).isEnabled ());
    }
    
    void enableModule (UpdateUnit toEnable) throws Exception {
        FileObject fo = FileUtil.getConfigFile("Modules");
        File f = new File (getWorkDir (), "config/Modules");
        File f2 = new File (getWorkDir (), "modules");
        assertTrue (f.listFiles () != null && f.listFiles ().length != 0);
        assertTrue (f2.listFiles () != null && f2.listFiles ().length != 0);
        assertFalse (fileChanges[0]);
        assertNotNull (getModuleInfos ().get (toEnable.getCodeName ()));
        
        assertNotNull (toEnable);
        
        assertSame (toEnable, Utilities.toUpdateUnit (toEnable.getCodeName ()));
        
        OperationContainer<OperationSupport> container = OperationContainer.createForEnable ();
        assertNotNull (container.add (toEnable.getInstalled ()));
        OperationSupport support = container.getSupport ();
        assertNotNull (support);
        support.doOperation (null);
        assertNotNull (toEnable.getInstalled ());
        
        assertTrue (f.listFiles () != null && f.listFiles ().length != 0);
        assertTrue (f2.listFiles () != null && f2.listFiles ().length != 0);
        assertEquals (1, fo.getChildren ().length);
        assertEquals (f.listFiles ()[0], FileUtil.toFile (fo.getChildren ()[0]));
        
        //Thread.sleep(3000);
        assertNotNull (getModuleInfos ().get (toEnable.getCodeName ()));
        ModuleInfo info = getModuleInfos ().get (toEnable.getCodeName ());
        assertNotNull (info);
        assertTrue (info.isEnabled ());
        assertNotNull (Utilities.toModule (toEnable.getCodeName (), null));
        assertTrue (Utilities.toModule (toEnable.getCodeName (), null).isEnabled ());
    }
    
    
    void unInstallModule (final UpdateUnit toUnInstall) throws Exception {
        File configModules = new File (getWorkDir (), "config/Modules");
        File modules = new File (getWorkDir (), "modules");
        int configModulesSize = (configModules.listFiles () != null) ? configModules.listFiles ().length : 0;
        int modulesSize = (modules.listFiles () != null) ? modules.listFiles ().length : 0;
        FileObject foConfigModules = FileUtil.getConfigFile("Modules");
        assertNotNull (foConfigModules);
        int foConfigModulesSize = foConfigModules.getChildren ().length;
        
        assertFalse (fileChanges[1]);
        FileObject fo = FileUtil.getConfigFile("Modules");
        assertNotNull (fo);
        assertTrue (fo.getChildren ().length > 0);
        assertNotNull (getModuleInfos ().get (toUnInstall.getCodeName ()));
        
        assertNotNull (toUnInstall);
        
        assertSame (toUnInstall, Utilities.toUpdateUnit (toUnInstall.getCodeName ()));
        UpdateElement installElement = toUnInstall.getInstalled();
        OperationContainer<OperationSupport> container = OperationContainer.createForDirectUninstall ();
        OperationContainer.OperationInfo operationInfo = container.add (toUnInstall.getInstalled ());
        assertNotNull (operationInfo);
        operationInfo.getRequiredElements ();
        OperationSupport support = container.getSupport ();
        assertNotNull (support);
        support.doOperation (null);
        assertNull (toUnInstall.getInstalled ());

        if (Trampoline.API.impl(installElement).getInstallInfo ().getTargetCluster () == null) {
            assertTrue (configModules.listFiles ().length < configModulesSize);
            //assertTrue(modules.listFiles().length < modulesSize);
            assertTrue (foConfigModules.getPath (), foConfigModules.getChildren ().length < foConfigModulesSize);
        }
        
        assertTrue (fileChanges[1]);
        fileChanges[1]=false;
        //TODO: to know why Thread.sleep(3000) must be present in these tests
        /*if (!Thread.currentThread().equals(fileChangeThreads[1])) {
            exceptions[1].printStackTrace();
        }
        assertEquals(Thread.currentThread(),fileChangeThreads[1]);
         */
        fileChangeThreads[1]=null;
        
        
        //Thread.sleep(3000);
        ModuleInfo info = getModuleInfos ().get (toUnInstall.getCodeName ());
        assertNull (info);
        assertNull (Utilities.toModule (toUnInstall.getCodeName (), null));
    }
}