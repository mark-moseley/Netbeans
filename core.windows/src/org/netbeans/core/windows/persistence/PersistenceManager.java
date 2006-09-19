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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.persistence;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.windows.Debug;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.io.SafeException;
import org.openide.windows.TopComponent;
import org.openide.xml.XMLUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Manages persistent data of window system, currently stored in XML format.
 * Default setting of layers is that reading is done through default file system
 * and writing into project layer.
 * 
 * @author Dafe Simonek
 */
public final class PersistenceManager implements PropertyChangeListener {
    /** logger of what is happening in persistance */
    static final Logger LOG = Logger.getLogger("org.netbeans.core.windows.persistence"); // NOI18N


    /** Constants for default root folder name for winsys data representation */
    static final String ROOT_MODULE_FOLDER = "Windows2"; // NOI18N
    public static final String ROOT_LOCAL_FOLDER = "Windows2Local"; // NOI18N
    static final String WINDOWMANAGER_FOLDER = "WindowManager"; // NOI18N
    static final String GROUPS_FOLDER = "Groups"; // NOI18N
    static final String MODES_FOLDER = "Modes"; // NOI18N
    private static final String COMPS_FOLDER = "Components"; // NOI18N
    
    /** Constants for file extensions that winsys uses */
    public static final String WINDOWMANAGER_EXT = "wswmgr"; // NOI18N
    public static final String WORKSPACE_EXT = "wswksp"; // NOI18N
    public static final String MODE_EXT = "wsmode"; // NOI18N
    public static final String TCREF_EXT = "wstcref"; // NOI18N
    public static final String GROUP_EXT = "wsgrp"; // NOI18N
    public static final String TCGROUP_EXT = "wstcgrp"; // NOI18N
    public static final String COMPONENT_EXT = "settings"; // NOI18N
    
    /** default base name for noname top components */
    private static final String DEFAULT_TC_NAME = "untitled_tc"; // NOI18N
    
    private static final boolean DEBUG = Debug.isLoggable(PersistenceManager.class);
    
    /** Root folder for win sys module */
    private FileObject rootModuleFolder;
    /** Root folder for win sys local */
    private FileObject rootLocalFolder;
    
    /** Components module folder */
    private FileObject compsModuleFolder;
    
    /** Groups folder */
    private FileObject groupsModuleFolder;
    /** Groups folder */
    private FileObject groupsLocalFolder;
    
    /** Module modes folder */
    private FileObject modesModuleFolder;
    /** Local modes folder */
    private FileObject modesLocalFolder;
    
    /** Loading/saving of window system configuration data */
    private WindowManagerParser windowManagerParser;
    
    /** Handler of changes in module folder */
    private ModuleChangeHandler changeHandler;
    
    /** Weak hash map between persistent TopComponents and their string IDs, used in lookup */
    //<key=TopComponent, value=String>
    private final Map<TopComponent, String> topComponent2IDMap = new WeakHashMap<TopComponent, String>(30);
    
    /** Weak hash map between nonpersistent TopComponents and their string IDs, used in lookup */
    //<key=TopComponent, value=String>
    private final Map<TopComponent, String> topComponentNonPersistent2IDMap = 
            new WeakHashMap<TopComponent, String>(30);
    
    /** Contains already used TopComponent ID. It is used to make sure unique
     * ID is created for every TopComponent instance */
    private Set<String> globalIDSet = new HashSet<String>(30);
    
    /** Map between string ids and weakly hold top components */
    private final Map<String, Reference<TopComponent>> id2TopComponentMap = 
            Collections.synchronizedMap(new HashMap<String, Reference<TopComponent>>(30));
    
    /** Map between string ids and weakly hold top components */
    private final Map<String, Reference<TopComponent>> id2TopComponentNonPersistentMap = 
            Collections.synchronizedMap(new HashMap<String, Reference<TopComponent>>(30));
    
    /** Weak map between data objects and top components, used to clean cache when module
     * owning tc is disabled. */
    private final Map<DataObject, String> dataobjectToTopComponentMap = 
            new WeakHashMap<DataObject, String>(30);
    
    /** A set of invalid Ids */
    private Set invalidIds = new HashSet(10);
    
    /** A set of used TcIds. Used to clean unused settings files
     * (ie. not referenced from tcRef or tcGroup). Cleaning is performed
     * when window system is loaded. */
    private final Set<String> usedTcIds = new HashSet<String>(10); // <String>
    
    /** Lock for synchronizing access to IDs. */
    private final Object LOCK_IDS = new Object();
    
    /** xml parser */
    private XMLReader parser;
    
    private static PersistenceManager defaultInstance;
    
    /** Creates new PersistenceManager */
    private PersistenceManager() {
    }
    
    /** Returns reference to singleton instance of PersistenceManager.
     */
    public static synchronized PersistenceManager getDefault() {
        if(defaultInstance == null) {
            defaultInstance = new PersistenceManager();
        }
        
        return defaultInstance;
    }

    /** Clears our internal state. */
    public void clear() { 
        rootModuleFolder = null;
        rootLocalFolder  = null;
        compsModuleFolder = null;
        groupsModuleFolder = null;
        groupsLocalFolder = null;
        modesModuleFolder = null;
        modesLocalFolder = null;
        windowManagerParser = null;
        if (changeHandler != null) {
            changeHandler.stopHandling();
        }
        changeHandler = null;
        topComponent2IDMap.clear();
        topComponentNonPersistent2IDMap.clear();
        globalIDSet = new HashSet<String>(30);
        id2TopComponentMap.clear();
        id2TopComponentNonPersistentMap.clear();
        dataobjectToTopComponentMap.clear();
        invalidIds = new HashSet(10);
        usedTcIds.clear();
    }
    
    FileObject getRootModuleFolder () {
        try {
            if (rootModuleFolder == null) {
                rootModuleFolder = FileUtil.createFolder(
                    Repository.getDefault().getDefaultFileSystem().getRoot(), ROOT_MODULE_FOLDER
                );
            }
            return rootModuleFolder;
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_RootFolder", ROOT_MODULE_FOLDER);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
        return null;
    }
    
    FileObject getRootLocalFolder () {
        try {
            if (rootLocalFolder == null) {
                rootLocalFolder = FileUtil.createFolder(
                    Repository.getDefault().getDefaultFileSystem().getRoot(), ROOT_LOCAL_FOLDER
                );
            }
            return rootLocalFolder;
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_RootFolder", ROOT_LOCAL_FOLDER);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
        return null;
    }
    
    /** Sets root module folder for window system configuration data. It is used
     * by tests to be able to test on test data.
     */
    void setRootModuleFolder (FileObject rootModuleFolder) {
        this.rootModuleFolder = rootModuleFolder;
    }
    
    /** Sets root local folder for window system configuration data. It is used
     * by tests to be able to test on test data.
     */
    void setRootLocalFolder (FileObject rootLocalFolder) {
        this.rootLocalFolder = rootLocalFolder;
    }

    /** @return Module folder for TopComponents */
    public FileObject getComponentsModuleFolder () {
        try {
            if (compsModuleFolder == null) {
                compsModuleFolder = FileUtil.createFolder(
                    getRootModuleFolder(), COMPS_FOLDER
                );
            }
            return compsModuleFolder;
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_CompsFolder", COMPS_FOLDER);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
        return null;
    }
    
    /** @return Local folder for TopComponents. Do not cache ti because it can change
     * during project switch. */
    public FileObject getComponentsLocalFolder () {
        try {
            FileObject compsLocalFolder = FileUtil.createFolder(
                getRootLocalFolder(), COMPS_FOLDER
            );
            return compsLocalFolder;
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_CompsFolder", COMPS_FOLDER);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
        return null;
    }
    
    /** @return Module folder for groups */
    public FileObject getGroupsModuleFolder () {
        try {
            if (groupsModuleFolder == null) {
                groupsModuleFolder = FileUtil.createFolder(
                    getRootModuleFolder(), GROUPS_FOLDER
                );
            }
            return groupsModuleFolder;
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_GroupsFolder", GROUPS_FOLDER);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
        return null;
    }
    
    /** @return Folder for groups */
    public FileObject getGroupsLocalFolder () {
        try {
            if (groupsLocalFolder == null) {
                groupsLocalFolder = FileUtil.createFolder(
                    getRootLocalFolder(), GROUPS_FOLDER
                );
            }
            return groupsLocalFolder;
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_GroupsFolder", GROUPS_FOLDER);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
        return null;
    }
    
    /** @return Module folder for modes */
    public FileObject getModesModuleFolder () {
        try {
            if (modesModuleFolder == null) {
                modesModuleFolder = FileUtil.createFolder(
                    getRootModuleFolder(), MODES_FOLDER
                );
            }
            return modesModuleFolder;
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_ModesFolder", MODES_FOLDER);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
        return null;
    }
    
    /** @return Local folder for modes */
    public FileObject getModesLocalFolder () {
        try {
            if (modesLocalFolder == null) {
                modesLocalFolder = FileUtil.createFolder(
                    getRootLocalFolder(), MODES_FOLDER
                );
            }
            return modesLocalFolder;
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_ModesFolder", MODES_FOLDER);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
        return null;
    }
    
    /** Listens to property changes in InstanceDataObject. Used to clean top component
     * cache when module owning given top component is disabled. */
    public void propertyChange (PropertyChangeEvent evt) {
        if (DataObject.PROP_COOKIE.equals(evt.getPropertyName())) {
            Object obj = evt.getSource();
            removeTopComponentForDataObject((DataObject)obj);
        }
    }
    
    private void removeTopComponentForDataObject(DataObject dob) {
        //System.out.println("PM.removeTopComponentForDataObject ENTER"
        //+ " dob:" + dob.getName());
        InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
        //Remove corresponding tc from cache because its module was disabled
        if (ic == null) {
            synchronized(LOCK_IDS) {
                String tc_id = (String) dataobjectToTopComponentMap.remove(dob);
                if (tc_id != null) {
                    /*System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                    System.out.println("-- -- PM.removeTopComponentForDataObject"
                    + " tc_id:" + tc_id);
                    System.out.println("-- -- dob:" + dob.getClass().getName()
                    + " isValid:" + dob.isValid());*/
                    //Thread.dumpStack();
                    WeakReference result = (WeakReference)id2TopComponentMap.remove(tc_id);
                    if (result != null) {
                        TopComponent tc = (TopComponent)result.get();
                        if (tc != null) {
                            topComponent2IDMap.remove(tc);
                        }
                    }
                }
            }
        }
    }
    
    /** Returns unique TopComponent ID for given TopComponent both persistent
     * and non persistent.
     * @param tc TopComponent the component for which is ID returned
     * @param preferredID first approximation used for creation of unique ID
     * @return unique TopComponent ID
     */
    public String getGlobalTopComponentID (TopComponent tc, String preferredID) {
        synchronized(LOCK_IDS) {
            //First check caches
            String result = (String) topComponent2IDMap.get(tc);
            if (result != null) {
                if (isInvalidId(result)) {
                    restorePair(tc, result);
                }
                return result;
            }
            result = (String) topComponentNonPersistent2IDMap.get(tc);
            if (result != null) {
                return result;
            }
        }
        
        //Not found create new TopComponent Id
        if (isTopComponentProbablyPersistent(tc)) {
            return createTopComponentPersistentID(tc, preferredID);
        } else {
            return createTopComponentNonPersistentID(tc, preferredID);
        }
    }
    
    /** performance related, TCs get GCed anyway, but the strings were kept in the maps.
     * make sure you call this only on TCs that are NOT TC.PERSISTENT_ALWAYS
     */
    public void removeGlobalTopComponentID(String id) {
//        System.out.println("removing id=" + id);
        synchronized(LOCK_IDS) {
            globalIDSet.remove(id.toUpperCase(Locale.ENGLISH));
            WeakReference result = (WeakReference)id2TopComponentMap.remove(id);
            if (result != null) {
                TopComponent tc = (TopComponent)result.get();
                if (tc != null) {
                    topComponent2IDMap.remove(tc);
                }
            }
            result = (WeakReference)id2TopComponentNonPersistentMap.remove(id);
            if (result != null) {
                TopComponent tc = (TopComponent)result.get();
                if (tc != null) {
                    topComponentNonPersistent2IDMap.remove(tc);
                }
            }
        }
    }
    
    /** @return Searches for top component with given string id and returns
     * found lookup item.
     */
    private TopComponent getTopComponentPersistentForID(String stringId) {
        synchronized(LOCK_IDS) {
            //Search in cache first
            WeakReference result = (WeakReference)id2TopComponentMap.get(stringId);
            if (result != null) {
                TopComponent tc = (TopComponent)result.get();
                if (tc != null) {
                    return tc;
                } else {
                    //TopComponent instance was garbage collected.
                    id2TopComponentMap.remove(stringId);
                }
            }
        }
        // search on disk
        IOException resultExc = null;
        try {
            DataObject dob = findTopComponentDataObject(getComponentsLocalFolder(), stringId);
            if (dob != null) {
                InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
                if (ic != null) {
                    TopComponent tc = (TopComponent)ic.instanceCreate();
                    synchronized(LOCK_IDS) {
                        topComponent2IDMap.put(tc, stringId);
                        id2TopComponentMap.put(stringId, new WeakReference<TopComponent>(tc));
                        dataobjectToTopComponentMap.put(dob, stringId);
                    }
                    dob.addPropertyChangeListener(this);
                    return tc;
                } else {
                    // no instance cookie, which means that module which owned top
                    // component is gone or versions of data and module are incompatible
                    String excAnnotation = NbBundle.getMessage(
                            PersistenceManager.class, "EXC_BrokenTCSetting", 
                            stringId);
                    resultExc = new SafeException(new IOException(excAnnotation));
                    LOG.log(Level.WARNING,
                        "[WinSys.PersistenceManager.getTopComponentForID]" // NOI18N
                        + " Problem when deserializing TopComponent for tcID:'" + stringId + "'. Reason: " // NOI18N
                        + excAnnotation, resultExc);
                }
            }
            else {
                // not found
                String excAnnotation = NbBundle.getMessage(PersistenceManager.class,
                        "EXC_FailedLocateTC",  stringId);
                resultExc = new FileNotFoundException(excAnnotation);
                LOG.warning(
                    "[WinSys.PersistenceManager.getTopComponentForID]" // NOI18N
                    + " Problem when deserializing TopComponent for tcID:'" + stringId + "'. Reason: " // NOI18N
                    + excAnnotation);
// can happen quite often when switching projects (in pre-40 codebase).. TC are project layer based while the winmanager+mainwindow are session based.
// IMHO not really a problem. (mkleint) - issue #40244                
// with new projects should not happen, since projects are not switched in winsys anymore.                
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, resultExc);
            }
        } catch (NoClassDefFoundError ndfe) { // TEMP>>
            LOG.log(Level.WARNING,
                "[WinSys.PersistenceManager.getTopComponentForID]" // NOI18N
                + " Problem when deserializing TopComponent for tcID:'" + stringId + "'. Reason: " // NOI18N
                + ndfe.getMessage(), ndfe);
        } catch (InvalidObjectException ioe) {
            LOG.log(Level.WARNING,
                "[WinSys.PersistenceManager.getTopComponentForID]" // NOI18N
                + " Problem when deserializing TopComponent for tcID:'" + stringId + "'. Reason: " // NOI18N
                + ioe.getMessage(), ioe);
        } catch (DataObjectNotFoundException dnfe) {
            LOG.log(Level.WARNING,
                "[WinSys.PersistenceManager.getTopComponentForID]" // NOI18N
                + " Problem when deserializing TopComponent for tcID:'" + stringId + "'. Reason: " // NOI18N
                + " Object not found: " + dnfe.getMessage() // NOI18N
                + ". It was probably deleted.", dnfe); // NOI18N
        } catch (ClassNotFoundException exc) {
            // ignore, will result in IOException fail below, annotate
            // and turn into IOExc
            LOG.log(Level.WARNING,
                "[WinSys.PersistenceManager.getTopComponentForID]" // NOI18N
                + " Problem when deserializing TopComponent for tcID:'" + stringId + "'. Reason: " // NOI18N
                + exc.getMessage(), exc);
        } catch (ClassCastException exc) {
            // instance is not top component (is broken), annotate and
            // turn into IOExc
            LOG.log(Level.WARNING,
                "[WinSys.PersistenceManager.getTopComponentForID]" // NOI18N
                + " Problem when deserializing TopComponent for tcID:'" + stringId + "'. Reason: " // NOI18N
                + exc.getMessage(), exc);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING,
                "[WinSys.PersistenceManager.getTopComponentForID]" // NOI18N
                + " Problem when deserializing TopComponent for tcID:'" + stringId + "'. Reason: " // NOI18N
                + ioe.getMessage(), ioe);
        }
        return null;
    }
    
    /** @return Searches for TopComponent with given string id and returns
     * found lookup item.
     */
    private TopComponent getTopComponentNonPersistentForID (String stringId) {
        synchronized(LOCK_IDS) {
            //Search in cache first
            WeakReference result = (WeakReference) id2TopComponentNonPersistentMap.get(stringId);
            if (result != null) {
                TopComponent tc = (TopComponent) result.get();
                if (tc != null) {
                    return tc;
                } else {
                    //TopComponent instance was garbage collected.
                    id2TopComponentNonPersistentMap.remove(stringId);
                }
            }
            return null;
        }
    }
    
    /** @return Searches for top component with given string id and returns
     * found lookup item.
     */
    public TopComponent getTopComponentForID (String stringId) {
        TopComponent tc = getTopComponentNonPersistentForID(stringId);
        if (tc == null) {
            return getTopComponentPersistentForID(stringId);
        } else {
            return tc;
        }
    }
    
    /** Try to find the data object representing a top component ref in some folder.
     * Only the TC name is known, but we can guess at some likely filenames before
     * doing an exhaustive search. Produces either a FileObject or DataObject,
     * DataObject if possible but FileObject if it would not need to recognize it.
     */
    private static DataObject findTopComponentDataObject(FileObject folder, String name) throws IOException {
        // First try the usual suspects.
        FileObject fo = folder.getFileObject(name, "settings"); // NOI18N
        if (fo == null) {
            fo = folder.getFileObject(name, "ser"); // NOI18N
        }
        if (fo == null) {
            fo = folder.getFileObject(name, "xml"); // NOI18N
        }
        
        if (fo != null) {
            return DataObject.find(fo);
        }
        
        // Don't accept name.instance so easily: if it has a 'name' attr etc., the
        // DataObject.name may not in fact be name. For the three extensions above,
        // the data object should in fact have the expected name. So now try to find
        // a file object with that name and check the DataObject.name.
        Enumeration e = folder.getChildren(false);
        while (e.hasMoreElements()) {
            fo = (FileObject)e.nextElement();
            DataObject dob = DataObject.find(fo);
            if (dob.getName().equals(name)) return dob;
        }
        // Finally look for any data object in this folder with the right name.
        // Slow but should not happen often.
        DataFolder dfolder = DataFolder.findFolder(folder);
        e = dfolder.children();
        while (e.hasMoreElements()) {
            DataObject dob = (DataObject)e.nextElement();
            if (dob.getName().equals(name)) return dob;
        }
        // Oh well.
        return null;
    }
    
    /** Tests if given top component with specified stringId is persistent.
     * This is used to split TopComponents to 2 groups:
     * First group contains all persistent TopComponents (default) and all 
     * TopComponents which could be persistent client property is set 
     * to persistent only when opened.
     * Second group contains TopComponents which are never persistent.
     * @param tc top component in question
     * @return true if component is persistent (which is by default) or it can be
     * persistent when opened.
     */
    private boolean isTopComponentProbablyPersistent (TopComponent tc) {
        int persistenceType = tc.getPersistenceType();
        if (TopComponent.PERSISTENCE_NEVER == persistenceType) {
            return false;
        }
        return true;
    }
    
    /** Tests if given top component with specified stringId is persistent.
     * This method is used for saving of TopComponent.
     * @param tc top component in question
     * @return true if component is persistent (which is by default), false
     * otherwise - top component's property exists saying "don't make me persistent"
     */
    public boolean isTopComponentPersistent (TopComponent tc) {
        int persistenceType = tc.getPersistenceType();
        if ((TopComponent.PERSISTENCE_NEVER == persistenceType)
        || ((TopComponent.PERSISTENCE_ONLY_OPENED == persistenceType) && !tc.isOpened())) {
            return false;
        }
        return true;
    }
    
    /** Asks all top components active in the system to save their current state.
     */
    private void saveTopComponents (WindowManagerConfig wmc) {
        DataFolder compsFolder = DataFolder.findFolder(getComponentsLocalFolder());
        Map<String, Reference<TopComponent>> copyIdToTopComponentMap;
        // must be synced, as Hashmap constructor iterates over original map
        synchronized(LOCK_IDS) {
            copyIdToTopComponentMap = new HashMap<String, Reference<TopComponent>>(id2TopComponentMap);
        }

        for (Map.Entry<String, Reference<TopComponent>> curEntry: copyIdToTopComponentMap.entrySet()) {
            TopComponent curTC = curEntry.getValue().get();
            if (curTC != null) {
                try {
                    // bugfix #21223 top components are stored by IDO.SaveCookie
                    FileObject fo = compsFolder.getPrimaryFile ().getFileObject
                            (curEntry.getKey (), "settings");  // NOI18N
                    DataObject ido = null;
                    if (fo != null) {
                        ido = DataObject.find(fo);
                    }
                    if (ido == null) {
                        // create new settings file
                        InstanceDataObject.create(
                            compsFolder, unescape(curEntry.getKey()), curTC, null
                          );
                    } else {
                        // save to settings file if there is already
                        SaveCookie sc = (SaveCookie)ido.getCookie(SaveCookie.class);
                        if (sc != null) {
                            sc.save();
                        } else {
                            ido.delete();
                            InstanceDataObject.create(
                            compsFolder, unescape((String)curEntry.getKey()), curTC, null
                            );
                        }
                    }
                } catch (NotSerializableException nse) {
                    // #36916: Handle case when TC is not serializable.
                    String id = (String) topComponent2IDMap.get(curTC);
                    // #75247: Log warning when TC is not serializable.
                    LOG.log(Level.WARNING, "TopComponent " + id + " is not serializable.", nse); //NOI18N
                    removeTCFromConfig(wmc,id);
                } catch (IOException exc) {
                    // some problem with saving of top component, log warning
                    LOG.log(Level.WARNING, null, exc);
                    String id = (String) topComponent2IDMap.get(curTC);
                    removeTCFromConfig(wmc,id);
                } catch (RuntimeException exc) {
                    //Bugfix #19688: Catch all other exceptions to be able to continue with saving process
                    String annotation = NbBundle.getMessage(
                            PersistenceManager.class,"EXC_CannotSaveTCSettings",
                            curTC.getName());
                    Exceptions.attachLocalizedMessage(exc, annotation);
                    LOG.log(Level.WARNING, null, exc);
                    String id = (String) topComponent2IDMap.get(curTC);
                    removeTCFromConfig(wmc,id);
                } catch (LinkageError le) {
                    String annotation = NbBundle.getMessage(
                            PersistenceManager.class,"EXC_CannotSaveTCSettings",
                            curTC.getName());
                    Exceptions.attachLocalizedMessage(le, annotation);
                    LOG.log(Level.WARNING, null, le);
                    String id = (String) topComponent2IDMap.get(curTC);
                    removeTCFromConfig(wmc,id);
                }
            }
        }
    }

    /** Recursive method searching for file object with given name */
    private static FileObject findTopComponentRefFile (FileObject folder, String tcId) {
        FileObject result = folder.getFileObject(tcId, TCREF_EXT);
        if (result != null) {
            return result;
        }
        FileObject[] childrenArray = folder.getChildren();
        for (int i = 0; i < childrenArray.length; i++) {
            if (childrenArray[i].isFolder()) {
                result = findTopComponentRefFile(childrenArray[i], tcId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /** compute filename in the same manner as InstanceDataObject.create
     * [PENDING] in next version this should be replaced by public support
     * likely from FileUtil
     * @see issue #17142
     */
    private static String escape(String name) {
        try {
            Method escape = 
                InstanceDataObject.class.getDeclaredMethod(
                    "escapeAndCut", new Class[] {String.class}); //NOI18N
            escape.setAccessible(true);
            return (String) escape.invoke(null, name);
        } catch (Exception ex) {
            LOG.log(Level.WARNING,
                "Escape support failed", ex); // NOI18N
            return name;
        }
    }
    
    /** compute filename in the same manner as InstanceDataObject.create
     * [PENDING] in next version this should be replaced by public support
     * likely from FileUtil
     * @see issue #17142
     */
    private static String unescape(String name) {
        try {
            Method unescape =
            InstanceDataObject.class.getDeclaredMethod(
            "unescape", new Class[] {String.class}); //NOI18N
            unescape.setAccessible(true);
            return (String) unescape.invoke(null, name);
        } catch (Exception ex) {
            LOG.log(Level.WARNING,
            "Escape support failed", ex); // NOI18N
            return name;
        }
    }
    
    private String createTopComponentNonPersistentID (TopComponent tc, String preferredID) {
        String compName = preferredID != null ? preferredID : null;
        // be prepared for null names, empty names and convert to filesystem friendly name
        if ((compName == null) || (compName.length() == 0)) {
            compName = DEFAULT_TC_NAME;
        }
        //Check if component id is not already present in cache of invalid ids
        boolean isUsed = true;
        compName = escape(compName);
        String srcName = compName;
        int i = 1;
        synchronized(LOCK_IDS) {
            while (isUsed) {
                isUsed = false;
                if (globalIDSet.contains(srcName.toUpperCase(Locale.ENGLISH))) {
                    isUsed = true;
                    srcName = compName + "_" + i;
                    i++;
                }
            }

            topComponentNonPersistent2IDMap.put(tc, srcName);
            id2TopComponentNonPersistentMap.put(srcName, new WeakReference<TopComponent>(tc));
            globalIDSet.add(srcName.toUpperCase(Locale.ENGLISH));
        }
        
        return srcName;
    }
    
    private String createTopComponentPersistentID (TopComponent tc, String preferredID) {
        String compName = preferredID != null ? preferredID : null;
        // be prepared for null names, empty names and convert to filesystem friendly name
        if ((compName == null) || (compName.length() == 0)) {
            compName = DEFAULT_TC_NAME;
        }
        //Check if component id is not already present in cache of invalid ids
        boolean isUsed = true;
        String origName = compName;
        compName = escape(compName);
        String srcName = compName;
        int i = 1;
        synchronized(LOCK_IDS) {
            while (isUsed) {
                isUsed = false;
                String uniqueName = FileUtil.findFreeFileName(
                    getComponentsLocalFolder(), srcName, "settings" // NOI18N
                );

                if (!srcName.equals(uniqueName) || globalIDSet.contains(uniqueName.toUpperCase(Locale.ENGLISH))) {
                    isUsed = true;
                    // #44293 - proper escaping to keep name synced with InstanceDataObject naming
                    srcName = escape(origName + "_" + i);
                    i++;
                }
                
            }

            topComponent2IDMap.put(tc, srcName);
            id2TopComponentMap.put(srcName, new WeakReference<TopComponent>(tc));
            globalIDSet.add(srcName.toUpperCase(Locale.ENGLISH));
        }
        
        return srcName;
    }
    
    /** Reuses existing settings files */
    private String restorePair (TopComponent tc, String id) {
        //System.out.println("++ PM.restorePair ENTER"
        //+ " tc:" + tc.getName() + " id:" + id);
        FileObject fo = getComponentsLocalFolder().getFileObject(id, "settings");
        if (fo != null) {
            //System.out.println("++ PM.restorePair tc:" + tc.getName()
            //+ " id:" + id);
            //Thread.dumpStack();
            synchronized(LOCK_IDS) {
                id2TopComponentMap.put(id, new WeakReference<TopComponent>(tc));
                validateId(id);
            }
            return id;
        } else {
            return null;
        }
    }
    
    private boolean isInvalidId (String id) {
        return invalidIds.contains(id);
    }
    
    private void validateId (String id) {
        if (invalidIds != null) {
            invalidIds.remove(id);
        }
    }
    
    /** map of exceptions to names of badly persistenced top components,
     * serves as additional annotation of main exception */
    private Map<Exception, String> failedCompsMap;
    
    /** Annotate persistence exception. Exception is added to the exception
     * list, which is displayed at once when whole persistence process
     * (either serialization or deserialization) is about to finish.
     */
    public void annotatePersistenceError(Exception exc, String tcName) {
        if (failedCompsMap == null) {
            failedCompsMap = new HashMap<Exception, String>();
        }
        failedCompsMap.put(exc, tcName);
    }
    
    /** Checks for some persistence errors and notifies the user if some
     * persistence errors occured. Shouild be called after serialization
     * and deserialization of window manager.
     */
    public void checkPersistenceErrors(boolean reading) {
        if(failedCompsMap == null || failedCompsMap.isEmpty()) {
            return;
        }

        for(Iterator it = failedCompsMap.keySet().iterator(); it.hasNext(); ) {
            Exception e = (Exception)it.next();
            Object name = failedCompsMap.get(e);
            // create message
            String message = NbBundle.getMessage(PersistenceManager.class, 
                    (reading ? "FMT_TCReadError" : "FMT_TCWriteError"),
                    new Object[] {name});
            Exceptions.attachLocalizedMessage(e, message);
            LOG.log(Level.WARNING, null, e);
        }
        
        // clear for futher use
        failedCompsMap = null;
    }
    
    /** Accessor to WindowManagerParser instance. */
    public WindowManagerParser getWindowManagerParser () {
        if (windowManagerParser == null) {
            windowManagerParser = new WindowManagerParser(this, WINDOWMANAGER_FOLDER);
        }
        return windowManagerParser;
    }
    
    /** Returns a XML parser. The same parser can be returned assuming that config files
     * are parser sequentially.
     *
     * @return XML parser with set content handler, errror handler
     * and entity resolver.
     */
    public XMLReader getXMLParser (DefaultHandler h) throws SAXException {
        if (parser == null) {
            // get non validating, not namespace aware parser
            parser = XMLUtil.createXMLReader();
            parser.setEntityResolver(new EntityResolver () {
                /** Implementation of entity resolver. Points to the local DTD
                 * for our public ID */
                public InputSource resolveEntity (String publicId, String systemId)
                throws SAXException {
                    if (ModeParser.INSTANCE_DTD_ID_1_0.equals(publicId)
                    || ModeParser.INSTANCE_DTD_ID_1_1.equals(publicId)
                    || ModeParser.INSTANCE_DTD_ID_1_2.equals(publicId)
                    || ModeParser.INSTANCE_DTD_ID_2_0.equals(publicId)
                    || ModeParser.INSTANCE_DTD_ID_2_1.equals(publicId)
                    || GroupParser.INSTANCE_DTD_ID_2_0.equals(publicId)
                    || TCGroupParser.INSTANCE_DTD_ID_2_0.equals(publicId)
                    || TCRefParser.INSTANCE_DTD_ID_1_0.equals(publicId)
                    || TCRefParser.INSTANCE_DTD_ID_2_0.equals(publicId)
                    || TCRefParser.INSTANCE_DTD_ID_2_1.equals(publicId)
                    || WindowManagerParser.INSTANCE_DTD_ID_1_0.equals(publicId)
                    || WindowManagerParser.INSTANCE_DTD_ID_1_1.equals(publicId)
                    || WindowManagerParser.INSTANCE_DTD_ID_2_0.equals(publicId)
                    || WindowManagerParser.INSTANCE_DTD_ID_2_1.equals(publicId)) {
                        InputStream is = new ByteArrayInputStream(new byte[0]);
                        return new InputSource(is);
                    }
                    return null; // i.e. follow advice of systemID
                }
            });
        }
        parser.setContentHandler(h);
        parser.setErrorHandler(h);
        return parser;
    }
    
    /** Adds TopComponent Id to set of used Ids. Called from ModeParser and
     * SetParser when loading tcRefs and tcGroups.
     */
    public void addUsedTCId (String tc_id) {
        synchronized(LOCK_IDS) {
            usedTcIds.add(tc_id);
        }
    }
    
    /** Removes TopComponent Id from set of used Ids. Called when module defining
     * TopComponent is disabled.
     */
    public void removeUsedTCId (String tc_id) {
        synchronized(LOCK_IDS) {
            usedTcIds.remove(tc_id);
        }
    }
    
    /** Returns true if given TopComponent ID is used as name of settings file.
     */
    public boolean isUsedTCId (String tc_id) {
        synchronized(LOCK_IDS) {
            return usedTcIds.contains(tc_id);
        }
    }
    
    /** Checks used TopComponent Ids. If TopComponent Id is not used its settings
     * file is deleted from local component folder.
     */
    private void checkUsedTCId () {
        FileObject [] files = getComponentsLocalFolder().getChildren();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isFolder() && "settings".equals(files[i].getExt())) { // NOI18N
                String tc_id = files[i].getName();
                boolean contains;
                synchronized(LOCK_IDS) {
                    contains = usedTcIds.contains(tc_id);
                }
                if (!contains) {
                    deleteOneFO(files[i]);
                }
            }
        }
        //Fill global set of used TopComponent IDs
        files = getComponentsLocalFolder().getChildren();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isFolder() && "settings".equals(files[i].getExt())) { // NOI18N
                String tc_id = files[i].getName();
                synchronized(LOCK_IDS) {
                    globalIDSet.add(tc_id.toUpperCase(Locale.ENGLISH));
                }
            }
        }
    }
    
    /** Loads window system configuration from disk.
     * @return window system configuration
     */
    public WindowManagerConfig loadWindowSystem() {
//        long start = System.currentTimeMillis();
        
        //Clear set of used tc_id
        synchronized (LOCK_IDS) {
            usedTcIds.clear();
        }
        
        copySettingsFiles();
        
        WindowManagerParser wmParser = getWindowManagerParser();
        WindowManagerConfig wmc = null;
        try {
            wmc = wmParser.load();
        } catch (IOException exc) {
            LOG.log(Level.WARNING, null, exc);
        }
        
        //Check used TcIds
        checkUsedTCId();
        
        if (changeHandler == null) {
            changeHandler = new ModuleChangeHandler();
            changeHandler.startHandling();
        }
        parser = null; // clear the ref to XML parser
        
//        long end = System.currentTimeMillis();
//        long diff = end - start;
//        System.out.println("Loading of window system takes " + diff + " ms");
        return wmc;
    }
    
    /** Saves window system configuration to disk.
     * @param wmc snapshot of windoes system configuration
     */
    public void saveWindowSystem (WindowManagerConfig wmc) {
        //long start, end, diff;
        WindowManagerParser wmParser = getWindowManagerParser();
        try {
            //start = System.currentTimeMillis();
            saveTopComponents(wmc);
            //end = System.currentTimeMillis();
            //diff = end - start;
            //System.out.println("Saving of top components takes " + diff + " ms");
            
            //start = System.currentTimeMillis();
            wmParser.save(wmc);
            //end = System.currentTimeMillis();
            //diff = end - start;
            //System.out.println("Saving of window system takes " + diff + " ms");
        } catch (IOException exc) {
            LOG.log(Level.WARNING, null, exc);
        }
    }
    
    /** Removes any occurence of TC id from configuration. It is necessary when
     * serialization of some TC fails ie. tc throws NotSerializableException.
     */
    private void removeTCFromConfig (WindowManagerConfig wmc, String id) {
        boolean removeFromRecent = false;
        for (int i = 0; i < wmc.tcIdViewList.length; i++) {
            if (id.equals(wmc.tcIdViewList[i])) {
                removeFromRecent = true;
                break;
            }
        }
        if (removeFromRecent) {
            List<String> l = new ArrayList<String>(wmc.tcIdViewList.length);
            for (int i = 0; i < wmc.tcIdViewList.length; i++) {
                if (!id.equals(wmc.tcIdViewList[i])) {
                    l.add(wmc.tcIdViewList[i]);
                }
            }
            wmc.tcIdViewList = l.toArray(new String[l.size()]);
        }
        for (int i = 0; i < wmc.modes.length; i++) {
            ModeConfig mc = wmc.modes[i];
            if (id.equals(mc.selectedTopComponentID)) {
                mc.selectedTopComponentID = "";
            }
            boolean removeFromMode = false;
            for (int j = 0; j < mc.tcRefConfigs.length; j++) {
                if (id.equals(mc.tcRefConfigs[j].tc_id)) {
                    removeFromMode = true;
                    break;
                }
            }
            if (removeFromMode) {
                List<TCRefConfig> l = new ArrayList<TCRefConfig>(mc.tcRefConfigs.length);
                for (int j = 0; j < mc.tcRefConfigs.length; j++) {
                    if (!id.equals(mc.tcRefConfigs[j].tc_id)) {
                        l.add(mc.tcRefConfigs[j]);
                    }
                }
                mc.tcRefConfigs = l.toArray(new TCRefConfig[l.size()]);
            }
        }
        for (int i = 0; i < wmc.groups.length; i++) {
            GroupConfig gc = wmc.groups[i];
            boolean removeFromGroup = false;
            for (int j = 0; j < gc.tcGroupConfigs.length; j++) {
                if (id.equals(gc.tcGroupConfigs[j].tc_id)) {
                    removeFromGroup = true;
                    break;
                }
            }
            if (removeFromGroup) {
                List<TCGroupConfig> l = new ArrayList<TCGroupConfig>(gc.tcGroupConfigs.length);
                for (int j = 0; j < gc.tcGroupConfigs.length; j++) {
                    if (!id.equals(gc.tcGroupConfigs[j].tc_id)) {
                        l.add(gc.tcGroupConfigs[j]);
                    }
                }
                gc.tcGroupConfigs = l.toArray(new TCGroupConfig[l.size()]);
            }
        }
    }
    
    /** Copy all settings files from module folder to local folder. */
    private void copySettingsFiles () {
        //long start, end, diff;
        //start = System.currentTimeMillis();
        if (DEBUG) Debug.log(PersistenceManager.class, "copySettingsFiles ENTER");
        Set<String> localSet = new HashSet<String>(100);
        FileObject [] filesLocal = getComponentsLocalFolder().getChildren();
        for (int i = 0; i < filesLocal.length; i++) {
            if (!filesLocal[i].isFolder() && "settings".equals(filesLocal[i].getExt())) { // NOI18N
                localSet.add(filesLocal[i].getName());
            }
        }
        
        FileObject [] filesModule = getComponentsModuleFolder().getChildren();
        for (int i = 0; i < filesModule.length; i++) {
            if (!filesModule[i].isFolder() && "settings".equals(filesModule[i].getExt())) { // NOI18N
                if (!localSet.contains(filesModule[i].getName())) {
                    copySettingsFile(filesModule[i]);
                }
            }
        }
        if (DEBUG) Debug.log(PersistenceManager.class, "copySettingsFiles LEAVE");
        //end = System.currentTimeMillis();
        //diff = end - start;
        //System.out.println("Copying of settings files takes " + diff + " ms");
    }
    
    /** Copy settings file from Module Components module folder (Windows2/Components)
     * to Local Components folder (Windows2Local/Components). */
    private void copySettingsFile (FileObject fo) {
        if (DEBUG) Debug.log(PersistenceManager.class, "copySettingsFile fo:" + fo);
        FileObject destFolder = getComponentsLocalFolder();
        try {
            fo.copy(destFolder,fo.getName(),fo.getExt());
        } catch (IOException exc) {
            String annotation = NbBundle.getMessage(PersistenceManager.class,
                "EXC_CopyFails", destFolder);
            Exceptions.attachLocalizedMessage(exc, annotation);
            LOG.log(Level.WARNING, null, exc);
        }
    }

    /** Copies given file object into Local Components folder (Windows2Local/Components)
     * if it doesn't exist already
     */
    void copySettingsFileIfNeeded (FileObject fo) {
        FileObject localSettingsFO = getComponentsLocalFolder().getFileObject(fo.getNameExt());
        if (localSettingsFO == null) {
            copySettingsFile(fo);
        }
    }
    
    /** Deletes specified file object */
    public static void deleteOneFO (FileObject fo) {
        FileLock lock = null;
        if (fo.isValid()) {
            try {
                lock = fo.lock();
                fo.delete(lock);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
    }


    /** Searches for module with given code name and specification version.
     * @param codeNameBase unique string base name of the module
     * (without release number)
     * @param spec string form of specification version of the module, null if
     * not important
     * @param strRelease release number of the module or null if not important
     *
     * @return module info of found module or null if module not found
     * (not installed).
     * @deprecated will be replaced by similar method in Modules Open APIs in
     * future releases
     */
    static final ModuleInfo findModule (String codeNameBase, String strRelease, String strSpec) {
        SpecificationVersion spec = null;
        int release = -1;
        
        if(strRelease != null) {
            try {
                release = Integer.parseInt(strRelease);
            } catch(NumberFormatException nfe) {
                LOG.log(Level.WARNING, null, nfe);
            }
        }
        if(strSpec != null) {
            spec = new SpecificationVersion(strSpec);
        } 
        
        Lookup.Result<ModuleInfo> modulesResult = 
            Lookup.getDefault().lookup(new Lookup.Template<ModuleInfo>(ModuleInfo.class));
        for (ModuleInfo curInfo: modulesResult.allInstances()) {
            // search for equal base name and then compare release and
            // spec numbers, if present
            if (curInfo.getCodeNameBase().equals(codeNameBase)) {
                if((release < 0 && spec == null)
                || (curInfo.getCodeNameRelease() > release)) {
                    return curInfo;
                } else if(release < 0
                || curInfo.getCodeNameRelease() == release) {
                    if(spec == null
                    || curInfo.getSpecificationVersion().compareTo(spec) >= 0) {
                        return curInfo;
                    }
                }
            }
        }
        return null;
    }
    
    /** 
     * #69505 - handle chars like & and ' in names of top components
     */
    public static String escapeTcId4XmlContent (String tcName) {
        if (tcName.indexOf('&') != -1 || tcName.indexOf('\'') != -1) {
            tcName = tcName.replaceAll("&", "&amp;");
            tcName = tcName.replaceAll("'", "&apos;");
        }
        return tcName;
    }

}
