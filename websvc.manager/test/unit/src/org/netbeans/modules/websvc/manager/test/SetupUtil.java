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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.manager.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbCollections;

/**
 *
 * @author quynguyen
 */
public class SetupUtil {
    private static final String WORKDIR_SPACES = "user directory/config/WebServices";
    private static final String WORKDIR = "userdirectory/config/WebServices";
    private static final String TEST_WSDL = "../resources/uszip.asmx.wsdl";
    private static final String TEST_CATALOG_DIR = "../resources/uszip-asmx-catalog";
    
    public static SetupData commonSetUp(File workingDir) throws Exception {
        SetupData data = new SetupData();
        
        String workDirByOS = 
                System.getProperty("os.name").startsWith("Windows") ? WORKDIR_SPACES : WORKDIR;
        
        File websvcHome = new File(workingDir, workDirByOS);
        data.setWebsvcHome(websvcHome);
        
        WebServiceDescriptor.WEBSVC_HOME = websvcHome.getAbsolutePath();
        WebServiceManager.WEBSVC_HOME = websvcHome.getAbsolutePath();
        
        File websvcUserDir = new File(WebServiceManager.WEBSVC_HOME);
        websvcUserDir.mkdirs();
        
        File wsdlFile = new File(SetupUtil.class.getResource(TEST_WSDL).toURI());
        File catalogDir = new File(SetupUtil.class.getResource(TEST_CATALOG_DIR).toURI());
        
        copy(wsdlFile, websvcUserDir);
        copy(wsdlFile, workingDir);
        copy(catalogDir, websvcUserDir);
        
        System.getProperties().setProperty("netbeans.user", websvcUserDir.getParentFile().getParentFile().getAbsolutePath());
        
        data.setLocalWsdlFile(new File(websvcUserDir, wsdlFile.getName()));
        data.setLocalCatalogFile(new File(websvcUserDir, catalogDir.getName() + "/catalog.xml"));
        data.setLocalOriginalWsdl(new File(workingDir, wsdlFile.getName()));
        
        MainFS fs = new MainFS();
        fs.setConfigRootDir(websvcHome.getParentFile());
        TestRepository.defaultFileSystem = fs;
        
        MockServices.setServices(DialogDisplayerNotifier.class, InstalledFileLocatorImpl.class, TestRepository.class);
        
        InstalledFileLocatorImpl locator = (InstalledFileLocatorImpl)Lookup.getDefault().lookup(InstalledFileLocator.class);
        locator.setUserConfigRoot(websvcHome.getParentFile());
        
        return data;
    }

    public static void commonTearDown() throws Exception {
        DialogDisplayer dd = DialogDisplayer.getDefault();
        if (dd instanceof DialogDisplayerNotifier) {
            ((DialogDisplayerNotifier)dd).removeAllListeners();
        }
        
        MockServices.setServices();
    }
    
    public static void copy(File src, File target) throws Exception {        
        if (src.isFile()) {
            File targetFile = new File(target, src.getName());
            
            FileInputStream is = new FileInputStream(src);
            FileOutputStream os = new FileOutputStream(targetFile);
            
            FileChannel inputChannel = is.getChannel();
            FileChannel outputChannel = os.getChannel();
            
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            outputChannel.close();
        }else {
            File newDir = new File(target, src.getName());
            newDir.mkdirs();
            
            File[] dirFiles = src.listFiles();
            if (dirFiles != null) {
                for (int i = 0; i < dirFiles.length; i++) {
                    copy(dirFiles[i], newDir);
                }
            }
        }
    }
    
    public static final class TestRepository extends Repository {
        static FileSystem defaultFileSystem = null;
        
        public TestRepository() {
            super(defaultFileSystem);
        }
    }
    
    // Taken from org.openide.filesystems.ExternalUtil to allow layer files to be
    // loaded into the default filesystem (since core/startup is in the classpath
    // and registers a default Repository that we do not want)
    public static final class MainFS extends MultiFileSystem implements LookupListener {
        private final Lookup.Result<FileSystem> ALL = Lookup.getDefault().lookupResult(FileSystem.class);
        private final FileSystem MEMORY = FileUtil.createMemoryFileSystem();
        private final XMLFileSystem layers = new XMLFileSystem();
        
        private final LocalFileSystem configRoot = new LocalFileSystem();
        
        public void setConfigRootDir(File root) throws Exception {
            configRoot.setRootDirectory(root);
        }
        
        public MainFS() {
            ALL.addLookupListener(this);
            
            List<URL> layerUrls = new ArrayList<URL>();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            try {
                for (URL manifest : NbCollections.iterable(l.getResources("META-INF/MANIFEST.MF"))) { // NOI18N
                    InputStream is = manifest.openStream();
                    try {
                        Manifest mani = new Manifest(is);
                        String layerLoc = mani.getMainAttributes().getValue("OpenIDE-Module-Layer"); // NOI18N
                        if (layerLoc != null) {
                            URL layer = l.getResource(layerLoc);
                            if (layer != null) {
                                layerUrls.add(layer);
                            }
                        }
                    } finally {
                        is.close();
                    }
                }
                layers.setXmlUrls(layerUrls.toArray(new URL[layerUrls.size()]));
            } catch (Exception x) {
            }
            resultChanged(null); // run after add listener - see PN1 in #26338
        }
        
        private FileSystem[] computeDelegates() {
            List<FileSystem> arr = new ArrayList<FileSystem>();
            arr.add(MEMORY);
            arr.add(layers);
            arr.add(configRoot);
            arr.addAll(ALL.allInstances());
            return arr.toArray(new FileSystem[0]);
        }
    
        public void resultChanged(LookupEvent ev) {
            setDelegates(computeDelegates());
        }
    }
    
    
}
