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

package org.netbeans.modules.editor.settings.storage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.editor.settings.storage.fontscolors.ColoringStorage;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class SettingsType {
    
    public static <K extends Object, V extends Object> StorageDescription<K, V> find(String id) {
        assert id != null : "The parameter id can't be null"; //NOI18N
        
        @SuppressWarnings("unchecked")
        StorageDescription<K, V> sd = Cache.getInstance().find(id);
        return sd;
    }
    
    public static Locator getLocator(StorageDescription sd) {
        assert sd != null : "The parameter sd can't be null"; //NOI18N
        
        Locator locator;

        if (ColoringStorage.ID.equals(sd.getId())) {
            locator = new FontsColorsLocator(sd.getId(), sd.isUsingProfiles(), sd.getMimeType(), sd.getLegacyFileName());
        } else if (KeyMapsStorage.ID.equals(sd.getId())) {
            locator = new KeybindingsLocator(sd.getId(), sd.isUsingProfiles(), sd.getMimeType(), sd.getLegacyFileName());
        } else {
            locator = new DefaultLocator(sd.getId(), sd.isUsingProfiles(), sd.getMimeType(), sd.getLegacyFileName());
        }

        return locator;
    }
    
    public static interface Locator {
        public void scan(FileObject baseFolder, String mimeType, String profileId, boolean fullScan, boolean scanModules, boolean scanUsers, Map<String, List<Object []>> results);
        public String getWritableFileName(String mimeType, String profileId, String fileId, boolean modulesFile);
        public boolean isUsingProfiles();
    }
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SettingsType.class.getName());
    
    private SettingsType() {
        // no-op
    }
    
    /* package */ static final class Cache implements LookupListener {
        
        public static synchronized Cache getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new Cache();
            }
            return INSTANCE;
        }

        public StorageDescription find(String id) {
            synchronized (cache) {
                return cache.get(id);
            }
        }
        
        // ------------------------------------------------------------------
        // LookupListener implementation
        // ------------------------------------------------------------------
        
        public void resultChanged(LookupEvent ev) {
            rebuild();
        }
        
        // ------------------------------------------------------------------
        // private implementation
        // ------------------------------------------------------------------
        
        private static Cache INSTANCE = null;
        private final Lookup.Result<StorageDescription> lookupResult;
        private final Map<String, StorageDescription> cache = new HashMap<String, StorageDescription>();

        private Cache() {
            this.lookupResult = Lookup.getDefault().lookupResult(StorageDescription.class);
            rebuild();
            this.lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, this.lookupResult));
        }
        
        private void rebuild() {
            synchronized (cache) {
                Collection<? extends StorageDescription> allInstances = lookupResult.allInstances();
                
                // determine all IDs
                Collection<String> allIds = new HashSet<String>();
                for(StorageDescription sd : allInstances) {
                    allIds.add(sd.getId());
                }

                // remove descriptions that are no longer in lookupResult
                cache.keySet().retainAll(allIds);
                
                // add new descriptions that appeared in lookupResult
                for(StorageDescription sd : allInstances) {
                    if (!cache.containsKey(sd.getId())) {
                        cache.put(sd.getId(), sd);
                    }
                }
            }            
        }
    } // End of Cache class
    
    // ------------------------------------------------------------------
    // Locators
    // ------------------------------------------------------------------
    
    private static class DefaultLocator implements Locator {

        protected static final String MODULE_FILES_FOLDER = "Defaults"; //NOI18N
        protected static final String DEFAULT_PROFILE_NAME = EditorSettingsImpl.DEFAULT_PROFILE;

        private static final String WRITABLE_FILE_PREFIX = "org-netbeans-modules-editor-settings-Custom"; //NOI18N
        private static final String WRITABLE_FILE_SUFFIX = ".xml"; //NOI18N
        private static final String FA_TARGET_OS = "nbeditor-settings-targetOS"; //NOI18N
        
        protected final String settingTypeId;
        protected final boolean isUsingProfiles;
        protected final String mimeType;
        protected final String legacyFileName;
        protected final String writableFilePrefix;
        protected final String modulesWritableFilePrefix;
        
        public DefaultLocator(String settingTypeId, boolean hasProfiles, String mimeType, String legacyFileName) {
            this.settingTypeId = settingTypeId;
            this.isUsingProfiles = hasProfiles;
            this.mimeType = mimeType;
            this.legacyFileName = legacyFileName;
            
            this.writableFilePrefix = WRITABLE_FILE_PREFIX + settingTypeId;
            this.modulesWritableFilePrefix = MODULE_FILES_FOLDER + "/" + writableFilePrefix; //NOI18N
        }
        
        public final void scan(
            FileObject baseFolder, 
            String mimeType,
            String profileId,
            boolean fullScan, 
            boolean scanModules,
            boolean scanUsers,
            Map<String, List<Object []>> results
        ) {
            assert results != null : "The parameter results can't be null"; //NOI18N

            FileObject mimeFolder = null;
            FileObject legacyMimeFolder = null;

            if (baseFolder != null) {
                mimeFolder = getMimeFolder(baseFolder, mimeType);
                legacyMimeFolder = getLegacyMimeFolder(baseFolder, mimeType);
            }
            
            if (scanModules) {
                if (legacyMimeFolder != null && legacyMimeFolder.isFolder()) {
                    addModulesLegacyFiles(legacyMimeFolder, profileId, fullScan, results);
                }
                if (mimeFolder != null && mimeFolder.isFolder()) {
                    addModulesFiles(mimeFolder, profileId, fullScan, results);
                }
            }

            if (scanUsers) {
                if (legacyMimeFolder != null && legacyMimeFolder.isFolder()) {
                    addUsersLegacyFiles(legacyMimeFolder, profileId, fullScan, results);
                }
                if (mimeFolder != null && mimeFolder.isFolder()) {
                    addUsersFiles(mimeFolder, profileId, fullScan, results);
                }
            }
        }

        public final String getWritableFileName(String mimeType, String profileId, String fileId, boolean modulesFile) {
            StringBuilder part = new StringBuilder(127);
            
            if (mimeType == null || mimeType.length() == 0) {
                part.append(settingTypeId).append('/'); //NOI18N
            } else {
                part.append(mimeType).append('/').append(settingTypeId).append('/'); //NOI18N
            }
            
            if (isUsingProfiles) {
                assert profileId != null : "The profileId parameter must not be null"; //NOI18N
                part.append(profileId).append('/'); //NOI18N
            }
            
            if (modulesFile) {
                part.append(modulesWritableFilePrefix);
            } else {
                part.append(writableFilePrefix);
            }
            
            if (fileId != null && fileId.length() != 0) {
                part.append(fileId);
            }
            
            part.append(WRITABLE_FILE_SUFFIX);
            
            return part.toString();
        }
        
        public final boolean isUsingProfiles() {
            return isUsingProfiles;
        }
        
        protected FileObject getLegacyMimeFolder(FileObject baseFolder, String mimeType) {
            return mimeType == null ? baseFolder : baseFolder.getFileObject(mimeType);
        }

        protected void addModulesLegacyFiles(FileObject mimeFolder, String profileId, boolean fullScan, Map<String, List<Object []>> files) {
            if (legacyFileName != null) {
                addLegacyFiles(
                    mimeFolder, 
                    profileId, 
                    MODULE_FILES_FOLDER + "/" + legacyFileName, //NOI18N
                    files, 
                    true
                );
            }
        }
        
        protected void addUsersLegacyFiles(FileObject mimeFolder, String profileId, boolean fullScan, Map<String, List<Object []>> files) {
            if (legacyFileName != null) {
                addLegacyFiles(
                    mimeFolder, 
                    profileId, 
                    legacyFileName, 
                    files, 
                    true
                );
            }
        }

        private FileObject getMimeFolder(FileObject baseFolder, String mimeType) {
            return mimeType == null ? baseFolder : baseFolder.getFileObject(mimeType);
        }

        private void addModulesFiles(FileObject mimeFolder, String profileId, boolean fullScan, Map<String, List<Object []>> files) {
            if (profileId == null) {
                FileObject settingHome = mimeFolder.getFileObject(settingTypeId);
                if (settingHome != null && settingHome.isFolder()) {
                    if (isUsingProfiles) {
                        FileObject [] profileHomes = settingHome.getChildren();
                        for(FileObject f : profileHomes) {
                            if (!f.isFolder()) {
                                continue;
                            }

                            String id = f.getNameExt();
                            FileObject folder = f.getFileObject(MODULE_FILES_FOLDER);
                            if (folder != null && folder.isFolder()) {
                                addFiles(folder, fullScan, files, id, f, true);
                            }
                        }
                    } else {
                        FileObject folder = settingHome.getFileObject(MODULE_FILES_FOLDER);
                        if (folder != null && folder.isFolder()) {
                            addFiles(folder, fullScan, files, null, null, true);
                        }
                    }
                }
            } else {
                FileObject folder = mimeFolder.getFileObject(settingTypeId + "/" + profileId + "/" + MODULE_FILES_FOLDER); //NOI18N
                if (folder != null && folder.isFolder()) {
                    addFiles(folder, fullScan, files, profileId, folder.getParent(), true);
                }
            }
        }
        
        private void addUsersFiles(FileObject mimeFolder, String profileId, boolean fullScan, Map<String, List<Object []>> files) {
            if (profileId == null) {
                FileObject settingHome = mimeFolder.getFileObject(settingTypeId);
                if (settingHome != null && settingHome.isFolder()) {
                    if (isUsingProfiles) {
                        FileObject [] profileHomes = settingHome.getChildren();
                        for(FileObject f : profileHomes) {
                            if (f.isFolder()) {
                                String id = f.getNameExt();
                                addFiles(f, fullScan, files, id, f, false);
                            }
                        }
                    } else {
                        addFiles(settingHome, fullScan, files, null, null, false);
                    }
                }
            } else {
                FileObject folder = mimeFolder.getFileObject(settingTypeId + "/" + profileId); //NOI18N
                if (folder != null && folder.isFolder()) {
                    addFiles(folder, fullScan, files, profileId, folder, false);
                }
            }
        }
        
        private final void addFiles(FileObject folder, boolean fullScan, Map<String, List<Object []>> files, String profileId, FileObject profileHome, boolean moduleFiles) {
            List<Object []> writableFiles = new ArrayList<Object []>();
            List<Object []> osSpecificFiles = new ArrayList<Object []>();
            
            FileObject [] ff = getOrderedChildren(folder);
            for(FileObject f : ff) {
                if (!f.isData()) {
                    continue;
                }
                
                if (f.getMIMEType().equals(mimeType)) {
                    Object targetOs = f.getAttribute(FA_TARGET_OS);
                    if (targetOs != null) {
                        try {
                            if (!isApplicableForThisTargetOs(targetOs)) {
                                LOG.fine("Ignoring OS specific file: '" + f.getPath() + "', it's targetted for '" + targetOs + "'"); //NOI18N
                                continue;
                            }
                        } catch (Exception e) {
                            LOG.log(Level.WARNING, "Ignoring editor settings file with invalid OS type mask '" + targetOs + "' file: '" + f.getPath() + "'"); //NOI18N
                            continue;
                        }
                    }
                    
                    List<Object []> infos = files.get(profileId);
                    if (infos == null) {
                        infos = new ArrayList<Object[]>();
                        files.put(profileId, infos);
                    }
                    Object [] oo = new Object [] { profileHome, f, moduleFiles };
                    
                    // There can be a writable file in the modules folder and it
                    // needs to be added last so that it does not get hidden by
                    // other module files.
                    if (moduleFiles) {
                        if (f.getNameExt().startsWith(writableFilePrefix)) {
                            writableFiles.add(oo);
                        } else if (targetOs != null) {
                            osSpecificFiles.add(oo);
                        } else {
                            infos.add(oo);
                        }
                    } else {
                        infos.add(oo);
                    }

                    // Stop scanning if this is not a full scan mode
                    if (!fullScan) {
                        break;
                    }
                } else {
                    LOG.fine("Ignoring file: '" + f.getPath() + "' of type " + f.getMIMEType()); //NOI18N
                }
            }

            if (!osSpecificFiles.isEmpty()) {
                List<Object []> infos = files.get(profileId);
                infos.addAll(osSpecificFiles);
            }
            
            // Add the writable file if there is any
            if (!writableFiles.isEmpty()) {
                List<Object []> infos = files.get(profileId);
                infos.addAll(writableFiles);
            }
        }
        
        private boolean isApplicableForThisTargetOs(Object targetOs) throws NoSuchFieldException, IllegalAccessException {
            if (targetOs instanceof Boolean) {
                return ((Boolean) targetOs).booleanValue();
            } else if (targetOs instanceof String) {
                Field field = Utilities.class.getDeclaredField((String) targetOs);
                int targetOsMask = field.getInt(null);
                int currentOsId = Utilities.getOperatingSystem();
                return (currentOsId & targetOsMask) != 0;
            } else {
                return false;
            }
        }
        
        protected static FileObject [] getOrderedChildren(FileObject folder) {
            // Collect all children
            Map<String, FileObject> children = new HashMap<String, FileObject>();
            for (FileObject f : folder.getChildren()) {
                String name = f.getNameExt();
                children.put(name, f);
            }

            // Collect all edges
            Map<FileObject, Set<FileObject>> edges = new HashMap<FileObject, Set<FileObject>>();
            for (Enumeration<String> attrNames = folder.getAttributes(); attrNames.hasMoreElements(); ) {
                String attrName = attrNames.nextElement();
                Object attrValue = folder.getAttribute(attrName);

                // Check whether the attribute affects sorting
                int slashIdx = attrName.indexOf('/'); //NOI18N
                if (slashIdx == -1 || !(attrValue instanceof Boolean)) {
                    continue;
                }

                // Get the file names
                String name1 = attrName.substring(0, slashIdx);
                String name2 = attrName.substring(slashIdx + 1);
                if (!((Boolean) attrValue).booleanValue()) {
                    // Swap the names
                    String s = name1;
                    name1 = name2;
                    name2 = s;
                }

                // Get the files and add them among the edges
                FileObject from = children.get(name1);
                FileObject to = children.get(name2);

                if (from != null && to != null) {
                    Set<FileObject> vertices = edges.get(from);
                    if (vertices == null) {
                        vertices = new HashSet<FileObject>();
                        edges.put(from, vertices);
                    }
                    vertices.add(to);
                }
            }
            
            // Sort the children
            List<FileObject> sorted;
            
            try {
                sorted = Utilities.topologicalSort(children.values(), edges);
            } catch (TopologicalSortException e) {
                LOG.log(Level.WARNING, "Can't sort folder children.", e); //NOI18N
                @SuppressWarnings("unchecked")
                List<FileObject> whyTheHellDoINeedToDoThis = e.partialSort();
                sorted = whyTheHellDoINeedToDoThis;
            }
            
            return sorted.toArray(new FileObject[sorted.size()]);
        }
        
        private void addLegacyFiles(FileObject mimeFolder, String profileId, String filePath, Map<String, List<Object []>> files, boolean moduleFiles) {
            if (profileId == null) {
                String defaultProfileId;
                
                if (isUsingProfiles) {
                    FileObject [] profileHomes = mimeFolder.getChildren();
                    for(FileObject f : profileHomes) {
                        if (!f.isFolder() || f.getNameExt().equals(MODULE_FILES_FOLDER)) {
                            continue;
                        }

                        String id = f.getNameExt();
                        FileObject legacyFile = f.getFileObject(filePath);
                        if (legacyFile != null) {
                            addFile(legacyFile, files, id, f, true);
                        }
                    }
                    defaultProfileId = DEFAULT_PROFILE_NAME;
                } else {
                    defaultProfileId = null;
                }
                
                FileObject legacyFile = mimeFolder.getFileObject(filePath);
                if (legacyFile != null) {
                    addFile(legacyFile, files, defaultProfileId, null, true);
                }
            } else {
                if (profileId.equals(DEFAULT_PROFILE_NAME)) {
                    FileObject file = mimeFolder.getFileObject(filePath); //NOI18N
                    if (file != null) {
                        addFile(file, files, profileId, null, moduleFiles);
                    }
                } else {
                    FileObject profileHome = mimeFolder.getFileObject(profileId);
                    if (profileHome != null && profileHome.isFolder()) {
                        FileObject file = profileHome.getFileObject(filePath);
                        if (file != null) {
                            addFile(file, files, profileId, profileHome, moduleFiles);
                        }
                    }
                }
            }
        }
        
        private void addFile(FileObject file, Map<String, List<Object []>> files, String profileId, FileObject profileHome, boolean moduleFiles) {
            List<Object []> pair = files.get(profileId);
            if (pair == null) {
                pair = new ArrayList<Object[]>();
                files.put(profileId, pair);
            }
            pair.add(new Object [] { profileHome, file, moduleFiles });
            
            if (LOG.isLoggable(Level.INFO)) {
                Utils.logOnce(LOG, Level.INFO, settingTypeId + " settings " + //NOI18N
                    "should reside in '" + settingTypeId + "' subfolder, " + //NOI18N
                    "see #90403 for details. Offending file '" + file.getPath() + "'", null); //NOI18N
            }
        }
    } // End of DefaultLocator class
    
    private static final class FontsColorsLocator extends DefaultLocator {
        
        private static final String [] M_LEGACY_FILE_NAMES = new String [] {
            MODULE_FILES_FOLDER + "/defaultColoring.xml", // NOI18N
            MODULE_FILES_FOLDER + "/coloring.xml", // NOI18N
            MODULE_FILES_FOLDER + "/editorColoring.xml", // NOI18N
        };
        
        private static final String [] U_LEGACY_FILE_NAMES = new String [] {
            "defaultColoring.xml", // NOI18N
            "coloring.xml", // NOI18N
            "editorColoring.xml", // NOI18N
        };
        
        public FontsColorsLocator(String settingTypeId, boolean hasProfiles, String mimeType, String legacyFileName) {
            super(settingTypeId, hasProfiles, mimeType, legacyFileName);
        }
        
        @Override
        protected void addModulesLegacyFiles(
            FileObject mimeFolder,
            String profileId,
            boolean fullScan,
            Map<String, List<Object []>> files
        ) {
            addFiles(mimeFolder, profileId, fullScan, M_LEGACY_FILE_NAMES, files, true);
        }

        @Override
        protected void addUsersLegacyFiles(
            FileObject mimeFolder,
            String profileId,
            boolean fullScan,
            Map<String, List<Object []>> files
        ) {
            addFiles(mimeFolder, profileId, fullScan, U_LEGACY_FILE_NAMES, files, false);
        }

        private void addFiles(
            FileObject mimeFolder,
            String profileId,
            boolean fullScan,
            String [] filePaths,
            Map<String, List<Object []>> files,
            boolean moduleFiles
        ) {
            if (profileId == null) {
                FileObject [] profileHomes = mimeFolder.getChildren();
                for(FileObject f : profileHomes) {
                    if (!f.isFolder()) {
                        continue;
                    }
                    
                    String id = f.getNameExt();
                    addFiles(f, filePaths, fullScan, files, id, f, moduleFiles); //NOI18N
                }
            } else {
                FileObject profileHome = mimeFolder.getFileObject(profileId);
                if (profileHome != null && profileHome.isFolder()) {
                    addFiles(profileHome, filePaths, fullScan, files, profileId, profileHome, moduleFiles);
                }
            }
        }
        
        private void addFiles(FileObject folder, String [] filePaths, boolean fullScan, Map<String, List<Object []>> files, String profileId, FileObject profileHome, boolean moduleFiles) {
            for(String filePath : filePaths) {
                FileObject f = folder.getFileObject(filePath);
                if (f != null) {
                    List<Object []> pair = files.get(profileId);
                    if (pair == null) {
                        pair = new ArrayList<Object[]>();
                        files.put(profileId, pair);
                    }
                    pair.add(new Object [] { profileHome, f, moduleFiles });

                    if (LOG.isLoggable(Level.INFO)) {
                        Utils.logOnce(LOG, Level.INFO, settingTypeId + " settings " + //NOI18N
                            "should reside in '" + settingTypeId + "' subfolder, " + //NOI18N
                            "see #90403 for details. Offending file '" + f.getPath() + "'", null); //NOI18N
                    }
                    
                    if (!fullScan) {
                        break;
                    }
                }
            }
        }
    } // End of FontsColorsLocator class

    private static final class KeybindingsLocator extends DefaultLocator {
        
        public KeybindingsLocator(String settingTypeId, boolean hasProfiles, String mimeType, String legacyFileName) {
            super(settingTypeId, hasProfiles, mimeType, legacyFileName);
        }
        
        @Override
        protected FileObject getLegacyMimeFolder(FileObject baseFolder, String mimeType) {
            if (mimeType == null || mimeType.length() == 0) {
                return baseFolder.getFileObject(EditorSettingsImpl.TEXT_BASE_MIME_TYPE);
            } else {
                return super.getMimeFolder(baseFolder, mimeType);
            }
        }
    } // End of KeybindingsLocator class
}
