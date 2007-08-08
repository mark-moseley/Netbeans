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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.netbeans.updater.UpdateTracking;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallManager {
    
    // special directories in NB files layout
    static final String NBM_LIB = "lib"; // NOI18N
    static final String NBM_CORE = "core"; // NOI18N
    
    private static final Logger ERR = Logger.getLogger ("org.netbeans.modules.autoupdate.services.InstallManager");
    
    static File findTargetDirectory (UpdateElement installed, UpdateElementImpl update, boolean isGlobal) {
        File res = null;
        
        // #111384: fixed modules must be installed globally
        isGlobal |= update.isFixed ();
        
        // adjust isGlobal to forced global if present
        isGlobal |= update.getInstallInfo ().isGlobal () != null && update.getInstallInfo ().isGlobal ().booleanValue ();
        
        // global or local
        if (isGlobal) {
            // is global
            // new one or update?
            if (installed != null) {
                res = getInstallDir (installed, update);
            }
            if (res == null) {
                // does have a target cluster?
                String targetCluster = update.getInstallInfo ().getTargetCluster ();
                File firstPossible = null;
                for (File cluster : UpdateTracking.clusters (false)) {
                    if (firstPossible == null) {
                        if (! cluster.exists ()) {
                            cluster.mkdirs ();
                        }
                        if (cluster.canWrite ()) {
                            firstPossible = cluster;
                        }
                    }
                    
                    if (targetCluster != null && targetCluster.equals (cluster.getName ())) {
                        if (! cluster.exists ()) {
                            cluster.mkdirs ();
                        }
                        if (cluster.canWrite ()) {
                            res = cluster;
                            break;
                        } else {
                            ERR.log (Level.WARNING, "No write permision in target cluster " + targetCluster + 
                                    " for " + update.getUpdateElement ());
                        }
                    }
                }
                
                // handle non-existing clusters
                if (res == null && targetCluster != null) {
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
                                ERR.log (Level.FINE, "Was written new netbeans.dirs " + sb);
                                
                                res = possibleCluster;
                                break;
                                
                            } catch (IOException ioe) {
                                ERR.log (Level.INFO, ioe.getMessage (), ioe);
                            }
                        }
                    }
                }
                
                // target cluster not found
                if (res == null) {
                    assert firstPossible != null : "No writeable cluster found.";
                    res = firstPossible != null ? firstPossible : getUserDir ();
                    if (ERR.isLoggable (Level.INFO)) {
                        if (targetCluster != null) {
                            ERR.log (Level.INFO, "Declared target cluster " + targetCluster + 
                                    " in " + update.getUpdateElement () + " wasn't found. Will be used " + res);
                        }
                    }
                }
            }
        } else {
            // is local
            res = getUserDir ();
        }
        ERR.log (Level.FINEST, "UpdateElement " + update.getUpdateElement () + " has the target cluster " + res);
        return res;
    }
    
    // can be null for fixed modules
    private static File getInstallDir (UpdateElement installed, UpdateElementImpl update) {
        File res = null;
        UpdateElementImpl i = Trampoline.API.impl (installed);
        assert i instanceof ModuleUpdateElementImpl : "Impl of " + installed + " instanceof ModuleUpdateElementImpl";
        
        String configFileName = "config" + '/' + "Modules" + '/' + installed.getCodeName ().replace ('.', '-') + ".xml"; // NOI18N
        File configFile = InstalledFileLocator.getDefault ().locate (configFileName, installed.getCodeName (), false);
        if (configFile == null) {
            // only fixed module cannot be located
            ERR.log (Level.FINE, "No install dir for " + installed + " (It's ok for fixed). Is fixed? " + Trampoline.API.impl (installed).isFixed ());
            String targetCluster = update.getInstallInfo ().getTargetCluster ();
            if (targetCluster != null) {
                for (File cluster : UpdateTracking.clusters (false)) {
                    if (targetCluster.equals (cluster.getName ())) {
                        if (cluster.canWrite ()) {
                            res = cluster;
                            break;
                        } else {
                            ERR.log (Level.WARNING, "No write permision in target cluster " + targetCluster + 
                                    " for " + update.getUpdateElement ());
                        }
                    }
                }
            }
            if (res == null) {
                // go to platform if no cluster is known
                res = UpdateTracking.getPlatformDir ();
            }
        } else {
            FileObject searchForFO = FileUtil.toFileObject (configFile);
            for (File cluster : UpdateTracking.clusters (true)) {       
                cluster = FileUtil.normalizeFile(cluster);
                if (FileUtil.isParentOf (FileUtil.toFileObject (cluster), searchForFO)) {
                    res = cluster;
                    break;
                }
            }
            assert res != null : "Install cluster exists for UpdateElementImpl " + installed;
            ERR.log (Level.FINEST, "Install dir of " + installed + " is " + res);
        }

        return res;
    }
    
    static File getUserDir () {
        File userDir = new File (System.getProperty ("netbeans.user"));
        userDir = new File(userDir.toURI ().normalize ()).getAbsoluteFile ();
        
        return userDir;
    }
    

    static boolean needsRestart (boolean isUpdate, UpdateElementImpl update, File dest) {
        assert update.getInstallInfo () != null : "Each UpdateElement must know own InstallInfo but " + update;
        boolean isForcedGlobal = update.getInstallInfo ().needsRestart () != null && update.getInstallInfo ().needsRestart ().booleanValue ();
        boolean needsRestart = isForcedGlobal || isUpdate;
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
            for (JarEntry entry : Collections.list (jf.entries ())) {
                String entryName = entry.getName ();
                if (entryName.startsWith (NBM_CORE + "/") || entryName.startsWith (NBM_LIB + "/")) {
                    res = true;
                    break;
                }
            }
        } catch (IOException ioe) {
            ERR.log (Level.INFO, ioe.getMessage (), ioe);
        }
        
        return res;
    }
    
}
