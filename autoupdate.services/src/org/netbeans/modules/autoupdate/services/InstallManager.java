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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.layers.LocalFileSystemEx;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.netbeans.updater.ModuleDeactivator;
import org.netbeans.updater.UpdateTracking;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallManager extends InstalledFileLocator{
    
    // special directories in NB files layout
    static final String NBM_LIB = "lib"; // NOI18N
    static final String NBM_CORE = "core"; // NOI18N
    
    private static final Logger ERR = Logger.getLogger ("org.netbeans.modules.autoupdate.services.InstallManager");
    private static List<File> clusters = new ArrayList<File>();
    
    static File findTargetDirectory (UpdateElement installed, UpdateElementImpl update, boolean isGlobal) {
        File res = null;
        
        // if an update, overwrite the existing location, wherever that is.
        if (installed != null) {
            
            res = getInstallDir (installed, update);
            
        } else {

            // #111384: fixed modules must be installed globally
            isGlobal |= update.isFixed ();

            // adjust isGlobal to forced global if present
            isGlobal |= update.getInstallInfo ().isGlobal () != null && update.getInstallInfo ().isGlobal ().booleanValue ();
            
            final String targetCluster = update.getInstallInfo ().getTargetCluster ();

            // global or local
            if ((targetCluster != null && targetCluster.length () > 0) || isGlobal) {
                res = checkTargetCluster(update, targetCluster);
                
                // handle non-existing clusters
                if (res == null && targetCluster != null) {
                    res = createNonExistingCluster (targetCluster);
                    res = checkTargetCluster(update, targetCluster);
                }
                
                // target cluster still not found
                if (res == null) {
                    
                    res = createNonExistingCluster (UpdateTracking.EXTRA_CLUSTER_NAME);
                    res = checkTargetCluster(update, UpdateTracking.EXTRA_CLUSTER_NAME);
                    // no new cluster was created => use userdir
                    res = res == null? getUserDir () : res;
                    
                    // create UpdateTracking.EXTRA_CLUSTER_NAME
                    if (ERR.isLoggable (Level.INFO)) {
                        if (targetCluster != null) {
                            ERR.log (Level.INFO, "Declared target cluster " + targetCluster + 
                                    " in " + update.getUpdateElement () + " wasn't found. Will be used " + res);
                        }
                    }
                    
                }
                
                // if no writable => getUserDir()
                if (ERR.isLoggable (Level.INFO)) {
                    if (res == null || ! Utilities.canWriteInCluster (res)) {
                        ERR.log (Level.INFO, "Declared target cluster " + targetCluster + 
                                " in " + update.getUpdateElement () + " is not writable. Will be used " + res);
                    }
                }
                res = res == null || ! Utilities.canWriteInCluster (res) ? getUserDir () : res;

                
            } else {
                // is local
                res = getUserDir ();
            }
        }
        ERR.log (Level.FINEST, "UpdateElement " + update.getUpdateElement () + " has the target cluster " + res);
        return res;
    }

    private static File checkTargetCluster(UpdateElementImpl update, String targetCluster) {
        File res = null;
        // is global or
        // does have a target cluster?
        for (File cluster : UpdateTracking.clusters(true)) {
            if (targetCluster != null && targetCluster.equals(cluster.getName())) {
                if (!cluster.exists()) {
                    cluster.mkdirs();
                    extendSystemFileSystem(cluster);
                }
                if (Utilities.canWriteInCluster (cluster)) {
                    res = cluster;
                    break;
                } else {
                    ERR.log(Level.WARNING, "It's forbidden to write in target cluster " + targetCluster + " for " + update.getUpdateElement());
                }
            }
        }

        return res;
    }
    
    private static File createNonExistingCluster (String targetCluster) {
        File res = null;
        for (AutoupdateClusterCreator creator : Lookup.getDefault ().lookupAll (AutoupdateClusterCreator.class)) {
            File possibleCluster = Trampoline.SPI.findCluster (targetCluster, creator);
            if (possibleCluster != null) {
                try {
                    ERR.log (Level.FINE, "Found cluster candidate " + possibleCluster + " for declared target cluster " + targetCluster);
                    File[] dirs = Trampoline.SPI.registerCluster (targetCluster, possibleCluster, creator);

                    // it looks good, generate new netbeans.dirs
                    res = possibleCluster;

                    StringBuffer sb = new StringBuffer ();
                    String sep = "";
                    for (int i = 0; i < dirs.length; i++) {
                        sb.append (sep);
                        sb.append (dirs [i].getPath ());
                        sep = File.pathSeparator;
                    }

                    System.setProperty("netbeans.dirs", sb.toString ()); // NOI18N
                    File f = new File(new File(getUserDir(), InstallSupportImpl.DOWNLOAD_DIR), "netbeans.dirs");//NOI18N
                    if (!f.exists()) {
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                    }
                    OutputStream os = new FileOutputStream(f);
                    try {
                        os.write(sb.toString().getBytes());
                    } finally {
                        os.close();
                    }
                    ERR.log (Level.FINE, "Was written new netbeans.dirs " + sb);

                    break;

                } catch (IOException ioe) {
                    ERR.log (Level.INFO, ioe.getMessage (), ioe);
                }
            }
        }
        return res;
    }

    private static void extendSystemFileSystem(File cluster) {
        try {
            File extradir = new File(cluster, ModuleDeactivator.CONFIG);
            extradir.mkdir();
            LocalFileSystemEx lfse = new LocalFileSystemEx();
            lfse.setRootDirectory(extradir);
            MainLookup.register(lfse);
            synchronized (InstallManager.class) {
                clusters.add(cluster);
            }
        } catch (PropertyVetoException ioe) {
            ERR.log (Level.INFO, ioe.getMessage (), ioe);
        } catch (IOException ioe) {
            ERR.log (Level.INFO, ioe.getMessage (), ioe);
        }
    }
    
    // can be null for fixed modules
    private static File getInstallDir (UpdateElement installed, UpdateElementImpl update) {
        File res = null;
        UpdateElementImpl i = Trampoline.API.impl (installed);
        assert i instanceof ModuleUpdateElementImpl : "Impl of " + installed + " instanceof ModuleUpdateElementImpl";
        
        Module m = Utilities.toModule (((ModuleUpdateElementImpl) i).getModuleInfo ());
        File jarFile = m == null ? null : m.getJarFile ();
        
        if (jarFile == null) {
            // only fixed module cannot be located
            ERR.log (Level.FINE, "No install dir for " + installed + " (It's ok for fixed). Is fixed? " + Trampoline.API.impl (installed).isFixed ());
            String targetCluster = update.getInstallInfo ().getTargetCluster ();
            if (targetCluster != null) {
                for (File cluster : UpdateTracking.clusters (false)) {
                    if (targetCluster.equals (cluster.getName ())) {
                        res = cluster;
                        break;
                    }
                }
            }
            if (res == null) {
                // go to platform if no cluster is known
                res = UpdateTracking.getPlatformDir ();
            }
        } else {
            
            /* comment out for xtesting
            FileObject searchForFO = FileUtil.toFileObject (configFile);
            for (File cluster : UpdateTracking.clusters (true)) {       
                cluster = FileUtil.normalizeFile(cluster);
                if (FileUtil.isParentOf (FileUtil.toFileObject (cluster), searchForFO)) {
                    res = cluster;
                    break;
                }*/
            
            for (File cluster : UpdateTracking.clusters (true)) {       
                cluster = FileUtil.normalizeFile (cluster);
                if (isParentOf (cluster, jarFile)) {
                    res = cluster;
                    break;
                }
            }
        }

        if (res == null || ! Utilities.canWriteInCluster (res)) {
            // go to userdir if no writable cluster is known
            ERR.log (Level.WARNING, "It's forbidden to write in target cluster " + res + 
                    " for " + update.getUpdateElement ());
            res = UpdateTracking.getUserDir ();
        }
        ERR.log (Level.FINEST, "Install dir of " + installed + " is " + res);
        
        return res;
    }
    
    private static boolean isParentOf (File parent, File child) {
        File tmp = child.getParentFile ();
        while (tmp != null && ! parent.equals (tmp)) {
            tmp = tmp.getParentFile ();
        }
        return tmp != null;
    }
    
    static File getUserDir () {
        return UpdateTracking.getUserDir ();
    }
    
    static boolean needsRestart (boolean isUpdate, UpdateElementImpl update, File dest) {
        assert update.getInstallInfo () != null : "Each UpdateElement must know own InstallInfo but " + update;
        boolean isForcedRestart = update.getInstallInfo ().needsRestart () != null && update.getInstallInfo ().needsRestart ().booleanValue ();
        boolean needsRestart = isForcedRestart || isUpdate;
        if (! needsRestart) {
            // handle installation into core or lib directory
            needsRestart = willInstallInSystem (dest);
        }
        return needsRestart;
    }

    private static boolean willInstallInSystem (File nbmFile) {
        boolean res = false;
        try {
            JarFile jf = new JarFile (nbmFile);
            try {
                for (JarEntry entry : Collections.list (jf.entries ())) {
                    String entryName = entry.getName ();
                    if (entryName.startsWith (NBM_CORE + "/") || entryName.startsWith (NBM_LIB + "/")) {
                        res = true;
                        break;
                    }
                }
            } finally {
                jf.close();
            }
        } catch (IOException ioe) {
            ERR.log (Level.INFO, ioe.getMessage (), ioe);
        }
        
        return res;
    }

    public File locate(String relativePath, String codeNameBase, boolean localized) {
        if (relativePath.length() == 0) {
            throw new IllegalArgumentException("Cannot look up \"\" in InstalledFileLocator.locate"); // NOI18N
        }
        if (relativePath.charAt(0) == '/') {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not start with '/': " + relativePath); // NOI18N
        }
        int slashIdx = relativePath.lastIndexOf('/');
        if (slashIdx == relativePath.length() - 1) {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not end in '/': " + relativePath); // NOI18N
        }
        
        String prefix, name;
        if (slashIdx != -1) {
            prefix = relativePath.substring(0, slashIdx + 1);
            name = relativePath.substring(slashIdx + 1);
            assert name.length() > 0;
        } else {
            prefix = "";
            name = relativePath;
        }
            if (localized) {
                int i = name.lastIndexOf('.');
                String baseName, ext;
                if (i == -1) {
                    baseName = name;
                    ext = "";
                } else {
                    baseName = name.substring(0, i);
                    ext = name.substring(i);
                }
                String[] suffixes = org.netbeans.Util.getLocalizingSuffixesFast();
                for (int j = 0; j < suffixes.length; j++) {
                    String locName = baseName + suffixes[j] + ext;
                    File f = locateExactPath(prefix, locName);
                    if (f != null) {
                        return f;
                    }
                }
                return null;
            } else {
                return locateExactPath(prefix, name);
            }
        
    }
    
    /** Search all top dirs for a file. */
    private static File locateExactPath(String prefix, String name) {
        synchronized(InstallManager.class) {
            File[] dirs = clusters.toArray(new File[clusters.size()]);
            for (int i = 0; i < dirs.length; i++) {
                File f = makeFile(dirs[i], prefix, name);
                if (f.exists()) {                    
                    return f;
                }
            }            
        }        
        return null;
    }
    
    private static File makeFile(File dir, String prefix, String name) {        
        return FileUtil.normalizeFile(new File(dir, prefix.replace('/', File.separatorChar) + name));
    }
}
