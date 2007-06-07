/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning;

import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.diff.DiffSidebarManager;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

import java.io.File;
import java.util.*;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Top level versioning manager that mediates communitation between IDE and registered versioning systems.
 * 
 * @author Maros Sandor
 */
public class VersioningManager implements PropertyChangeListener, LookupListener, PreferenceChangeListener {
    
    /**
     * Indicates to the Versioning manager that the layout of versioned files may have changed. Previously unversioned 
     * files became versioned, versioned files became unversioned or the versioning system for some files changed.
     * The manager will flush any caches that may be holding such information.  
     * A versioning system usually needs to fire this after an Import action. 
     */
    public static final String EVENT_VERSIONED_ROOTS = "null VCS.VersionedFilesChanged";

    /**
     * The NEW value is a Set of Files whose versioning status changed. This event is used to re-annotate files, re-fetch
     * original content of files and generally refresh all components that are connected to these files.
     */
    public static final String EVENT_STATUS_CHANGED = "Set<File> VCS.StatusChanged";

    /**
     * Used to signal the Versioning manager that some annotations changed. Note that this event is NOT required in case
     * the status of the file changes in which case annotations are updated automatically. Use this event to force annotations
     * refresh in special cases, for example when the format of annotations changes.
     * Use null as new value to force refresh of all annotations.
     */
    public static final String EVENT_ANNOTATIONS_CHANGED = "Set<File> VCS.AnnotationsChanged";

    
    private static VersioningManager instance;

    public static synchronized VersioningManager getInstance() {
        if (instance == null) {
            instance = new VersioningManager();
            instance.init();
        }
        return instance;
    }

    // ======================================================================================================

    private final FilesystemInterceptor filesystemInterceptor;

    /**
     * Result of Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(VersioningSystem.class));
     */
    private final Lookup.Result<VersioningSystem> systemsLookupResult;
    
    /**
     * Holds all registered versioning systems.
     */
    private final Collection<VersioningSystem> versioningSystems = new ArrayList<VersioningSystem>(2);

    /**
     * What folder is versioned by what versioning system. 
     */
    private final Map<File, VersioningSystem> folderOwners = new WeakHashMap<File, VersioningSystem>(100);

    /**
     * Holds registered local history system.
     */
    private VersioningSystem localHistory;
    
    /**
     * What folders are managed by local history. 
     */
    private Map<File, Boolean> localHistoryFolders = new WeakHashMap<File, Boolean>(100);
    
    private final VersioningSystem NULL_OWNER = new VersioningSystem() {
    };
    
    private VersioningManager() {
        systemsLookupResult = Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(VersioningSystem.class));
        filesystemInterceptor = new FilesystemInterceptor();
    }
    
    private void init() {
        systemsLookupResult.addLookupListener(this);
        refreshVersioningSystems();
        filesystemInterceptor.init(this);
        VersioningSupport.getPreferences().addPreferenceChangeListener(this);
    }

    /**
     * List of versioning systems changed.
     */
    private synchronized void refreshVersioningSystems() {
        unloadVersioningSystems();
        Collection<? extends VersioningSystem> systems = systemsLookupResult.allInstances();
        loadVersioningSystems(systems);
        flushFileOwnerCache();
        refreshDiffSidebars(null);
        VersioningAnnotationProvider.refreshAllAnnotations();
    }

    private void loadVersioningSystems(Collection<? extends VersioningSystem> systems) {
        assert versioningSystems.size() == 0;
        assert localHistory == null;
        versioningSystems.addAll(systems);
        for (VersioningSystem system : versioningSystems) {
            if (localHistory == null && Utils.isLocalHistory(system)) {
                localHistory = system;
            }
            system.addPropertyChangeListener(this);
        }
    }

    private void unloadVersioningSystems() {
        for (VersioningSystem system : versioningSystems) {
            system.removePropertyChangeListener(this);
        }
        versioningSystems.clear();
        localHistory = null;
    }

    InterceptionListener getInterceptionListener() {
        return filesystemInterceptor;
    }

    private void refreshDiffSidebars(Set<File> files) {
        // pushing the change ... DiffSidebarManager may as well listen for changes
        DiffSidebarManager.getInstance().refreshSidebars(files);
    }
    
    private synchronized void flushFileOwnerCache() {
        folderOwners.clear();
        localHistoryFolders.clear();
    }

    synchronized VersioningSystem[] getVersioningSystems() {
        return versioningSystems.toArray(new VersioningSystem[versioningSystems.size()]);
    }

    /**
     * Determines versioning systems that manage files in given context.
     * 
     * @param ctx VCSContext to examine
     * @return VersioningSystem systems that manage this context or an empty array if the context is not versioned
     */
    VersioningSystem[] getOwners(VCSContext ctx) {
        Set<File> files = ctx.getRootFiles();
        Set<VersioningSystem> owners = new HashSet<VersioningSystem>();
        for (File file : files) {
            VersioningSystem vs = getOwner(file);
            if (vs != null) {
                owners.add(vs);
            }
        }
        return (VersioningSystem[]) owners.toArray(new VersioningSystem[owners.size()]);
    }

    /**
     * Determines the versioning system that manages given file.
     * Owner of a file:
     * - annotates its label in explorers, editor tab, etc.
     * - provides menu actions for it
     * - supplies "original" content of the file
     * 
     * Owner of a file may change over time (one common example is the Import command). In such case, the appropriate 
     * Versioning System is expected to fire the PROP_VERSIONED_ROOTS property change. 
     * 
     * @param file a file
     * @return VersioningSystem owner of the file or null if the file is not under version control
     */
    public synchronized VersioningSystem getOwner(File file) {
        File folder = file;
        if (file.isFile()) {
            folder = file.getParentFile();
            if (folder == null) return null;
        }
        
        VersioningSystem owner = folderOwners.get(folder);
        if (owner == NULL_OWNER) return null;
        if (owner != null) return owner;
        
        File closestParent = null;
            for (VersioningSystem system : versioningSystems) {
                if (system != localHistory) {    // currently, local history is never an owner of a file
                    File topmost = system.getTopmostManagedAncestor(folder);                
                    if (topmost != null && (closestParent == null || Utils.isAncestorOrEqual(closestParent, topmost))) {
                        owner = system;
                        closestParent = topmost;
                    }                    
                }    
            }
                
        if (owner != null) {
            folderOwners.put(folder, owner);
        } else {
            folderOwners.put(folder, NULL_OWNER);
        }
        return owner;
    }

    /**
     * Returns local history module that handles the given file.
     * 
     * @param file the file to examine
     * @return VersioningSystem local history versioning system or null if there is no local history for the file
     */
    synchronized VersioningSystem getLocalHistory(File file) {
        if (localHistory == null) return null;
        File folder = file;
        if (file.isFile()) {
            folder = file.getParentFile();
            if (folder == null) return null;
        }
        
        Boolean isManagedByLocalHistory = localHistoryFolders.get(folder);
        if (isManagedByLocalHistory != null) {
            return isManagedByLocalHistory ? localHistory : null;
        }
                
        boolean isManaged = localHistory.getTopmostManagedAncestor(folder) != null;            
        if (isManaged) {
            localHistoryFolders.put(folder, Boolean.TRUE);
            return localHistory;
        } else {
            localHistoryFolders.put(folder, Boolean.FALSE);
            return null;
        }        
    }
    
    public void resultChanged(LookupEvent ev) {
        refreshVersioningSystems();
    }

    /**
     * Versioning status or other parameter changed. 
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (EVENT_STATUS_CHANGED.equals(evt.getPropertyName())) {
            Set<File> files = (Set<File>) evt.getNewValue();
            VersioningAnnotationProvider.instance.refreshAnnotations(files);
            refreshDiffSidebars(files);
        } else if (EVENT_ANNOTATIONS_CHANGED.equals(evt.getPropertyName())) {
            Set<File> files = (Set<File>) evt.getNewValue();
            VersioningAnnotationProvider.instance.refreshAnnotations(files);
        } else if (EVENT_VERSIONED_ROOTS.equals(evt.getPropertyName())) {
            flushFileOwnerCache();
            refreshDiffSidebars(null);
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        VersioningAnnotationProvider.instance.refreshAnnotations(null);
    }
}
