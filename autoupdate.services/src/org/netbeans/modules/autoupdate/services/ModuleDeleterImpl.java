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

import java.io.BufferedInputStream;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.updater.ModuleDeactivator;
import org.netbeans.updater.UpdateTracking;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/** Control if the module's file can be deleted and can delete them from disk.
 * <p> Deletes all files what are installed together with given module, info about
 * these files read from <code>update_tracking</code> file corresponed to the module.
 * If this <code>update_tracking</code> doesn't exist the files cannot be deleted.
 * The Deleter waits until the module is enabled before start delete its files.
 *
 * @author  Jiri Rechtacek
 */
public final class ModuleDeleterImpl  {
    private static final ModuleDeleterImpl INSTANCE = new ModuleDeleterImpl();
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ELEMENT_VERSION = "module_version"; // NOI18N
    private static final String ATTR_LAST = "last"; // NOI18N
    private static final String ATTR_FILE_NAME = "name"; // NOI18N
    
    private Logger err = Logger.getLogger (ModuleDeleterImpl.class.getName ()); // NOI18N
    
    private Set<File> storageFilesForDelete = null;
    
    public static ModuleDeleterImpl getInstance() {
        return INSTANCE;
    }
    
    public boolean canDelete (ModuleInfo moduleInfo) {
        if (moduleInfo == null) { // XXX: how come that moduleInfo is null?
            return false;
        }
        if (Utilities.isEssentialModule (moduleInfo)) {
            err.log(Level.FINE,
                    "Cannot delete module because module " +
                    moduleInfo.getCodeName() + " isEssentialModule.");
            return false;
        } else {
            return foundUpdateTracking (moduleInfo);
        }
    }
    
    public Collection<File> markForDisable (Collection<ModuleInfo> modules, ProgressHandle handle) {
        if (modules == null) {
            throw new IllegalArgumentException ("ModuleInfo argument cannot be null.");
        }
        
        if (handle != null) {
            handle.switchToDeterminate (modules.size() + 1);
        }
        
        Collection<File> configs = new HashSet<File> ();
        int i = 0;
        for (ModuleInfo moduleInfo : modules) {
            File config = locateConfigFile (moduleInfo);
            assert config != null : "Located config file for " + moduleInfo.getCodeName ();
            assert config.exists () : config + " config file must exists for " + moduleInfo.getCodeName ();
            err.log(Level.FINE, "Locate config file of " + moduleInfo.getCodeNameBase () + ": " + config);
            if(config!=null) {
                configs.add (config);
            }
            if (handle != null) {
                handle.progress (++i);
            }
        }

        return configs;
    }
    
    public Collection<File> markForDelete (Collection<ModuleInfo> modules, ProgressHandle handle) throws IOException {
        storageFilesForDelete = null;
        if (modules == null) {
            throw new IllegalArgumentException ("ModuleInfo argument cannot be null.");
        }
        
        if (handle != null) {
            handle.switchToDeterminate (modules.size () * 2 + 1);
        }

        Collection<File> configFiles = new HashSet<File> ();
        int i = 0;
        for (ModuleInfo moduleInfo : modules) {
            Collection<File> configs = locateAllConfigFiles (moduleInfo);
            assert configs != null : "Located config files for " + moduleInfo.getCodeName ();
            assert ! configs.isEmpty () : configs + " config files must exists for " + moduleInfo.getCodeName ();
            configFiles.addAll (configs);
            err.log(Level.FINE, "Locate config files of " + moduleInfo.getCodeNameBase () + ": " + configs);
            if (handle != null) {
                handle.progress (++i);
            }
        }
        getStorageFilesForDelete ().addAll (configFiles);
        
        for (ModuleInfo moduleInfo : modules) {
            removeModuleFiles(moduleInfo, true); 
            if (handle != null) {
                handle.progress (++i);
            }

        }
        return getStorageFilesForDelete ();
    }
    
    public void delete (final ModuleInfo[] modules, ProgressHandle handle) throws IOException {
        storageFilesForDelete = null;
        if (modules == null) {
            throw new IllegalArgumentException ("ModuleInfo argument cannot be null.");
        }
        
        if (handle != null) {
            handle.switchToDeterminate (modules.length + 1);
        }
        int i = 0;
        
        for (ModuleInfo moduleInfo : modules) {
            err.log(Level.FINE,"Locate and remove config file of " + moduleInfo.getCodeNameBase ());
            removeControlModuleFile(moduleInfo, false);
        }

        if (handle != null) {
            handle.progress (++i);
        }
        
        refreshModuleList ();
        
        int rerunWaitCount = 0;
        for (ModuleInfo moduleInfo : modules) {
            err.log(Level.FINE, "Locate and remove config file of " + moduleInfo.getCodeNameBase ());                       
            if (handle != null) {
                handle.progress (moduleInfo.getDisplayName (), ++i);
            }
            for (; rerunWaitCount < 100 && !isModuleUninstalled(moduleInfo); rerunWaitCount++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    err.log (Level.INFO, "Overflow checks of uninstalled module " + moduleInfo.getCodeName ());
                    Thread.currentThread().interrupt();
                }
            }
            removeModuleFiles(moduleInfo, false); 
        }
    }
    
    private boolean isModuleUninstalled(ModuleInfo moduleInfo) {
        return (InstalledModuleProvider.getInstalledModules ().get (moduleInfo.getCodeNameBase()) == null);
    }

    private File locateConfigFile (ModuleInfo m) {
        String configFile = ModuleDeactivator.CONFIG + '/' + ModuleDeactivator.MODULES + '/' + m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        return InstalledFileLocator.getDefault ().locate (configFile, m.getCodeNameBase (), false);
    }
    
    private Collection<File> locateAllConfigFiles (ModuleInfo m) {
        Collection<File> configFiles = new HashSet<File> ();
        String configFileName = m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        for (File cluster : UpdateTracking.clusters (true)) {
            File configFile = new File (new File (new File (cluster, ModuleDeactivator.CONFIG), ModuleDeactivator.MODULES), configFileName);
            if (configFile.exists ()) {
                configFiles.add (configFile);
            }
        }
        return configFiles;
    }
    
    private void removeControlModuleFile (ModuleInfo m, boolean markForDelete) throws IOException {
        File configFile = null;
        while ((configFile = locateConfigFile (m)) != null && ! getStorageFilesForDelete ().contains (configFile)) {
            if (configFile != null && configFile.exists ()) {
                //FileUtil.toFileObject (configFile).delete ();
                if (markForDelete) {
                    err.log(Level.FINE, "Control file " + configFile + " is marked for delete.");
                    getStorageFilesForDelete ().add (configFile);
                } else {
                    err.log(Level.FINE, "Try delete the config File " + configFile);
                    configFile.delete ();
                    err.log(Level.FINE, "Control file " + configFile + " is deleted.");
                }
            } else {
                err.log(Level.FINE,
                        "Warning: Config File " + configFile + " doesn\'t exist!");
            }
        }
    }
    
    private boolean foundUpdateTracking (ModuleInfo moduleInfo) {
        File updateTracking = Utilities.locateUpdateTracking (moduleInfo);
        if (updateTracking != null && updateTracking.exists ()) {
            //err.log ("Find UPDATE_TRACKING: " + updateTracking + " found.");
            // check the write permission
            if (! Utilities.canWrite (updateTracking)) {
                err.log(Level.FINE,
                        "Cannot delete module " + moduleInfo.getCodeName() +
                        " because is forbidden to write in directory " +
                        updateTracking.getParentFile ().getParent ());
                return false;
            } else {
                return true;
            }
        } else {
            err.log(Level.FINE,
                    "Cannot delete module " + moduleInfo.getCodeName() +
                    " because no update_tracking file found.");
            return false;
        }
    }
            
    private void removeModuleFiles (ModuleInfo m, boolean markForDelete) throws IOException {
        err.log (Level.FINE, "Entry removing files of module " + m);
        File updateTracking = null;
        while ((updateTracking = Utilities.locateUpdateTracking (m)) != null && ! getStorageFilesForDelete ().contains (updateTracking)) {
            removeModuleFilesInCluster (m, updateTracking, markForDelete);
        }
        err.log (Level.FINE, "Exit removing files of module " + m);
    }
    
    private void removeModuleFilesInCluster (ModuleInfo moduleInfo, File updateTracking, boolean markForDelete) throws IOException {
        err.log(Level.FINE, "Read update_tracking " + updateTracking + " file.");
        Set<String> moduleFiles = readModuleFiles (getModuleConfiguration (updateTracking));
        String configFile = ModuleDeactivator.CONFIG + '/' + ModuleDeactivator.MODULES + '/' + moduleInfo.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        
        if (moduleFiles.contains (configFile)) {
            File file = InstalledFileLocator.getDefault ().locate (configFile, moduleInfo.getCodeNameBase (), false);
            assert file == null || ! file.exists () ||
                    getStorageFilesForDelete ().contains (file): "Config file " + configFile + " must be already removed or marked for remove.";
        }
        
        for (String fileName : moduleFiles) {
            if (fileName.equals (configFile)) {
                continue;
            }
            File file = InstalledFileLocator.getDefault ().locate (fileName, moduleInfo.getCodeNameBase (), false);
            if (file == null) {
                err.log (Level.WARNING, "InstalledFileLocator doesn't locate file " + fileName + " for module " + moduleInfo.getCodeNameBase ());
                continue;
            }
            if (file.equals (updateTracking)) {
                continue;
            }
            assert file.exists () : "File " + file + " exists.";
            if (file.exists ()) {
                if (markForDelete) {
                    err.log(Level.FINE, "File " + file + " is marked for delete.");
                    getStorageFilesForDelete ().add (file);
                } else {
                    try {
                        FileObject fo = FileUtil.toFileObject (file);
                        //assert fo != null || !file.exists() : file.getAbsolutePath();
                        if (fo != null) {
                            fo.lock().releaseLock();
                        }
                        File f = file;
                        while (f.delete()) {
                            f = f.getParentFile(); // remove empty dirs too
                        }
                    } catch (IOException ioe) {
                        assert false : "Waring: IOException " + ioe.getMessage () + " was caught. Propably file lock on the file.";
                        err.log(Level.FINE,
                                "Waring: IOException " + ioe.getMessage() +
                                " was caught. Propably file lock on the file.");
                        err.log(Level.FINE,
                                "Try call File.deleteOnExit() on " + file);
                        file.deleteOnExit ();
                    }
                    err.log(Level.FINE, "File " + file + " is deleted.");
                }
            }
        }
        
        FileObject trackingFo = FileUtil.toFileObject (updateTracking);
        FileLock lock = null;
        
        try {
            lock = (trackingFo != null) ? trackingFo.lock() : null;        
            if (markForDelete) {
                err.log(Level.FINE, "Tracking file " + updateTracking + " is marked for delete.");
                getStorageFilesForDelete ().add (updateTracking);
            } else {
                updateTracking.delete ();
                err.log(Level.FINE, "Tracking file " + updateTracking + " is deleted.");
            }
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
        err.log(Level.FINE, "File " + updateTracking + " is deleted.");
    }
    
    private Node getModuleConfiguration (File moduleUpdateTracking) {
        Document document = null;
        InputStream is=null;
        try {
            is = new BufferedInputStream (new FileInputStream (moduleUpdateTracking));
            InputSource xmlInputSource = new InputSource (is);
            document = XMLUtil.parse (xmlInputSource, false, false, null, org.openide.xml.EntityCatalog.getDefault ());
            if (is != null) {
                is.close ();
            }
        } catch (SAXException saxe) {
            err.log(Level.WARNING, "SAXException when reading " + moduleUpdateTracking, saxe);
            //for issue #158186 investigation purpose need to add additional logging to see what is corrupted and how
            FileReader reader=null;
            try {
                reader=new FileReader(moduleUpdateTracking);
                char[] text=new char[1024];
                String fileContent="";
                while(reader.read(text)>0)
                {
                    fileContent+=String.copyValueOf(text);
                }
                err.log(Level.WARNING, "SAXException in file:\n------FILE START------\n " + fileContent+"\n------FILE END-----\n");
            }
            catch(Exception ex)
            {
                //don't need to fail in logging
            }
            finally
            {
                if(reader!=null)
                {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        //don't need any info from logging fail
                    }
                }
            }
            return null;
        } catch (IOException ioe) {
            err.log(Level.WARNING, "IOException when reading " + moduleUpdateTracking, ioe);
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
        return getModuleElement (document.getDocumentElement ());
    }
    
    private Node getModuleElement (Element element) {
        Node lastElement = null;
        assert ELEMENT_MODULE.equals (element.getTagName ()) : "The root element is: " + ELEMENT_MODULE + " but was: " + element.getTagName ();
        NodeList listModuleVersions = element.getElementsByTagName (ELEMENT_VERSION);
        for (int i = 0; i < listModuleVersions.getLength (); i++) {
            lastElement = getModuleLastVersion (listModuleVersions.item (i));
            if (lastElement != null) {
                break;
            }
        }
        return lastElement;
    }
    
    private Node getModuleLastVersion (Node version) {
        Node attrLast = version.getAttributes ().getNamedItem (ATTR_LAST);
        assert attrLast != null : "ELEMENT_VERSION must contain ATTR_LAST attribute.";
        if (Boolean.valueOf (attrLast.getNodeValue ()).booleanValue ()) {
            return version;
        } else {
            return null;
        }
    }
    
    private Set<String> readModuleFiles (Node version) {
        Set<String> files = new HashSet<String> ();
        NodeList fileNodes = version.getChildNodes ();
        for (int i = 0; i < fileNodes.getLength (); i++) {
            if (fileNodes.item (i).hasAttributes ()) {
                NamedNodeMap map = fileNodes.item (i).getAttributes ();
                files.add (map.getNamedItem (ATTR_FILE_NAME).getNodeValue ());
                err.log(Level.FINE,
                        "Mark to delete: " +
                        map.getNamedItem(ATTR_FILE_NAME).getNodeValue());
            }
        }
        return files;
    }

    private void refreshModuleList () {
        // XXX: the modules list should be delete automatically when config/Modules/module.xml is removed
        FileObject modulesRoot = FileUtil.getConfigFile(ModuleDeactivator.MODULES); // NOI18N
        err.log (Level.FINE, "Call refresh on " + modulesRoot + " file object.");
        if (modulesRoot != null) {
            modulesRoot.refresh ();
        }
    }
    
    private Set<File> getStorageFilesForDelete () {
        if (storageFilesForDelete == null) {
            storageFilesForDelete = new HashSet<File> ();
        }
        return storageFilesForDelete;
    }
}
