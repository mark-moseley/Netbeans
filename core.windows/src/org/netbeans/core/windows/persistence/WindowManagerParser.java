/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.persistence;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.persistence.convert.ImportManager;
import org.netbeans.core.windows.persistence.convert.ImportedItem;

/**
 * Handle loading/saving of WindowManager configuration data.
 *
 * @author Marek Slama
 */

public class WindowManagerParser {
    
    private static final String INSTANCE_DTD_ID_1_0
        = "-//NetBeans//DTD Window Manager Properties 1.0//EN"; // NOI18N
    private static final String INSTANCE_DTD_ID_1_1
        = "-//NetBeans//DTD Window Manager Properties 1.1//EN"; // NOI18N
    private static final String INSTANCE_DTD_ID_2_0
        = "-//NetBeans//DTD Window Manager Properties 2.0//EN"; // NOI18N
    
    private static final boolean DEBUG = Debug.isLoggable(WindowManagerParser.class);
    
    /** Unique wm name */
    private String wmName;
    
    private PersistenceManager pm;
    
    private PropertyHandler propertyHandler;
    
    private InternalConfig internalConfig;
    
    private Map modeParserMap = new HashMap(19);
    
    private Map groupParserMap = new HashMap(19);
    
    //Set of <String>
    //Used to collect names of all localy stored wstcref files.
    private Set tcRefNameLocalSet = new HashSet(101);
    
    private static Object SAVING_LOCK = new Object();
    
    public WindowManagerParser(PersistenceManager pm, String wmName) {
        this.pm = pm;
        this.wmName = wmName;
    }

    /** Load window manager configuration including all modes and tcrefs. */
    WindowManagerConfig load() throws IOException {
        synchronized (SAVING_LOCK) {
            WindowManagerConfig wmc = new WindowManagerConfig();
            readProperties(wmc);
            readModes(wmc);
            readGroups(wmc);
            return wmc;
        }
    }
    
    /** Save window manager configuration including all modes and tcrefs. */
    void save (WindowManagerConfig wmc) throws IOException {
        synchronized (SAVING_LOCK) {
            writeProperties(wmc);
            writeModes(wmc);
            writeGroups(wmc);
        }
    }
    
    /** Called from ModuleChangeHandler when wsmode file is deleted from module folder.
     * Do not remove ModeParser. Only set that it is not present in module folder.
     * @param modeName unique name of mode
     */
    void removeMode (String modeName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "removeMode" + " mo:" + modeName);
            ModeParser modeParser = (ModeParser) modeParserMap.get(modeName);
            if (modeParser != null) {
                modeParser.setInModuleFolder(false);
            }
            //deleteLocalMode(modeName);
        }
    }
    
    /** Called from ModuleChangeHandler when wsmode file is added to module folder.
     * Adds ModeParser.
     * @param modeName unique name of mode
     */
    ModeConfig addMode (String modeName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "addMode ENTER" + " mo:" + modeName);
            ModeParser modeParser = (ModeParser) modeParserMap.get(modeName);
            if (modeParser == null) {
                //Create new ModeParser if it does not exist.
                modeParser = new ModeParser(modeName,tcRefNameLocalSet);
                modeParserMap.put(modeName, modeParser);
            }
            FileObject modesModuleFolder = pm.getModesModuleFolder();
            modeParser.setModuleParentFolder(modesModuleFolder);
            modeParser.setInModuleFolder(true);
            ModeConfig modeConfig = null;
            try {
                modeConfig = modeParser.load();
            } catch (IOException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.addMode]" // NOI18N
                + " Warning: Cannot load mode " + modeName); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL, exc);
            }
            return modeConfig;
        }
    }
    
    /** Called from ModuleChangeHandler when wsgrp file is deleted from module folder.
     * Removes GroupParser and cleans wsgrp file from local folder
     * @param groupName unique name of group
     */
    void removeGroup (String groupName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.removeGroup" + " group:" + groupName);
            groupParserMap.remove(groupName);
            deleteLocalGroup(groupName);
        }
    }
    
    /** Called from ModuleChangeHandler when wsgrp file is added to module folder.
     * Adds GroupParser.
     * @param groupName unique name of group
     */
    GroupConfig addGroup (String groupName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addGroup ENTER" + " group:" + groupName);
            GroupParser groupParser = (GroupParser) groupParserMap.get(groupName);
            if (groupParser != null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.addGroup]" // NOI18N
                + " Warning: GroupParser " + groupName // NOI18N
                + " exists but it should not."); // NOI18N
                groupParserMap.remove(groupName);
            }
            groupParser = new GroupParser(groupName);
            FileObject groupsModuleFolder = pm.getGroupsModuleFolder();
            groupParser.setModuleParentFolder(groupsModuleFolder);
            groupParser.setInModuleFolder(true);
            //FileObject setsLocalFolder = pm.getGroupsLocalFolder();
            //groupParser.setLocalParentFolder(groupsLocalFolder);
            groupParserMap.put(groupName, groupParser);
            GroupConfig groupConfig = null;
            try {
                groupConfig = groupParser.load();
            } catch (IOException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.addGroup]" // NOI18N
                + " Warning: Cannot load group " + groupName); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL, exc);
            }
            return groupConfig;
        }
    }
    
    /** Called from ModuleChangeHandler when wstcref file is deleted from module folder
     * or from package convert when some imported TCRef is deleted from imported module folder.
     * Removes TCRefParser from ModeParser and cleans wstcref file from local folder
     * @param tcRefName unique name of tcRef
     */
    public boolean removeTCRef (String tcRefName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "removeTCRef ENTER" + " tcRef:" + tcRefName);
            ModeParser modeParser = findModeParser(tcRefName);
            if (modeParser == null) {
                //modeParser was already removed -> its local folder was cleaned
                if (DEBUG) Debug.log(WindowManagerParser.class, "removeTCRef LEAVE 1" + " tcRef:" + tcRefName);
                return false;
            }
            if (DEBUG) Debug.log(WindowManagerParser.class, "removeTCRef REMOVING tcRef:" + tcRefName
            + " FROM mo:" + modeParser.getName());  // NOI18N
            modeParser.removeTCRef(tcRefName);
            if (DEBUG) Debug.log(WindowManagerParser.class, "removeTCRef LEAVE 2" + " tcRef:" + tcRefName);
            return true;
        }
    }
    
    /** Called from ModuleChangeHandler when wstcref file is added to module folder.
     * Adds TCRefParser to ModeParser.     
     * @param tcRefName unique name of tcRef
     */
    TCRefConfig addTCRef (String modeName, String tcRefName, List tcRefNameList) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCRef ENTER" + " mo:" + modeName
            + " tcRef:" + tcRefName);  // NOI18N
            ModeParser modeParser = (ModeParser) modeParserMap.get(modeName);
            if (modeParser == null) {
                if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCRef LEAVE 1" + " mo:" + modeName
                + " tcRef:" + tcRefName);
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.addTCRef]" // NOI18N
                + " Warning: Cannot add tcRef " + tcRefName + ". ModeParser " + modeName + " not found."); // NOI18N
                return null;
            }
            TCRefConfig tcRefConfig = modeParser.addTCRef(tcRefName, tcRefNameList);
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCRef LEAVE 2" + " mo:" + modeName
            + " tcRef:" + tcRefName);  // NOI18N
            return tcRefConfig;
        }
    }
    
    /** Called from ModuleChangeHandler when wstcgrp file is deleted from module folder.
     * Removes TCGroupParser from GroupParser and cleans wstcgrp file from local folder
     * @param tcGroupName unique name of tcGroup
     */
    boolean removeTCGroup (String groupName, String tcGroupName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.removeTCGroup ENTER" + " group:" + groupName
            + " tcGroup:" + tcGroupName);  // NOI18N
            GroupParser groupParser = (GroupParser) groupParserMap.get(groupName);
            if (groupParser == null) {
                //groupParser was already removed -> its local folder was cleaned
                if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.removeTCGroup LEAVE 1" + " group:" + groupName
                + " tcGroup:" + tcGroupName);  // NOI18N
                return false;
            }
            groupParser.removeTCGroup(tcGroupName);
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.removeTCGroup LEAVE 2" + " group:" + groupName
            + " tcGroup:" + tcGroupName);  // NOI18N
            return true;
        }
    }
    
    /** Called from ModuleChangeHandler when wstcgrp file is added to module folder.
     * Adds TCGroupParser to GroupParser.     
     * @param tcGroupName unique name of tcGroup
     */
    TCGroupConfig addTCGroup (String groupName, String tcGroupName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCGroup ENTER" + " group:" + groupName
            + " tcGroup:" + tcGroupName);  // NOI18N
            GroupParser groupParser = (GroupParser) groupParserMap.get(groupName);
            if (groupParser == null) {
                if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCGroup LEAVE 1" + " group:" + groupName
                + " tcGroup:" + tcGroupName);  // NOI18N
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.addTCGroup]" // NOI18N
                + " Warning: Cannot add tcGroup " + tcGroupName + ". GroupParser " + groupName + " not found."); // NOI18N
                return null;
            }
            TCGroupConfig tcGroupConfig = groupParser.addTCGroup(tcGroupName);
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCGroup LEAVE 2" + " group:" + groupName
            + " tcGroup:" + tcGroupName);  // NOI18N
            return tcGroupConfig;
        }
    }
    
    /** Called from import to pass module info to new parser.
     * Adds TCRefParser to ModeParser.     
     * @param tcRefName unique name of tcRef
     */
    public void addTCRefImport (String modeName, String tcRefName, InternalConfig internalCfg) {
        if (DEBUG) Debug.log(WindowManagerParser.class, "addTCRefImport ENTER" + " mo:" + modeName
        + " tcRef:" + tcRefName);  // NOI18N
        ModeParser modeParser = (ModeParser) modeParserMap.get(modeName);
        if (modeParser == null) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "addTCRefImport LEAVE 1" + " mo:" + modeName
            + " tcRef:" + tcRefName);  // NOI18N
            ErrorManager.getDefault().log(ErrorManager.WARNING,
            "[WinSys.WindowManagerParser.addTCRef]" // NOI18N
            + " Warning: Cannot add tcRef " + tcRefName // NOI18N
            + ". ModeParser " + modeName + " not found."); // NOI18N
            return;
        }
        modeParser.addTCRefImport(tcRefName, internalCfg);
        if (DEBUG) Debug.log(WindowManagerParser.class, "addTCRefImport LEAVE 2" + " mo:" + modeName
        + " tcRef:" + tcRefName);  // NOI18N
    }
    
    /** Finds ModeParser containing TCRef with given ID. Returns null if such ModeParser
     * is not found.
     * @param tcRefName unique name of tcRef
     */
    ModeParser findModeParser (String tcRefName) {
        if (DEBUG) Debug.log(WindowManagerParser.class, "findModeParser ENTER" + " tcRef:" + tcRefName);
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            TCRefParser tcRefParser = modeParser.findTCRefParser(tcRefName);
            if (tcRefParser != null) {
                return modeParser;
            }
        }
        return null;
    }
    
    private void readProperties (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "readProperties ENTER");
        if (propertyHandler == null) {
            propertyHandler = new PropertyHandler();
        }
        internalConfig = new InternalConfig();
        propertyHandler.readData(wmc, internalConfig);
        if (DEBUG) Debug.log(WindowManagerParser.class, "readProperties LEAVE");
    }
    
    private void readModes (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "readModes ENTER");
        
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            modeParser.setInModuleFolder(false);
            modeParser.setInLocalFolder(false);
        }
        
        FileObject modesModuleFolder = pm.getRootModuleFolder().getFileObject(PersistenceManager.MODES_FOLDER);
        //if (DEBUG) Debug.log(WindowManagerParser.class, "modesModuleFolder: " + modesModuleFolder);
        if (modesModuleFolder != null) {
            FileObject [] files = modesModuleFolder.getChildren();
            for (int i = 0; i < files.length; i++) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "fo[" + i + "]: " + files[i]);
                if (!files[i].isFolder() && PersistenceManager.MODE_EXT.equals(files[i].getExt())) {
                    //wsmode file
                    ModeParser modeParser;
                    if (modeParserMap.containsKey(files[i].getName())) {
                        modeParser = (ModeParser) modeParserMap.get(files[i].getName());
                    } else {
                        modeParser = new ModeParser(files[i].getName(),tcRefNameLocalSet);
                        modeParserMap.put(files[i].getName(), modeParser);
                    }
                    modeParser.setInModuleFolder(true);
                    modeParser.setModuleParentFolder(modesModuleFolder);
                }
            }
        }
        
        FileObject modesLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.MODES_FOLDER);
        //if (DEBUG) Debug.log(WindowManagerParser.class, " modesLocalFolder: " + modesLocalFolder);
        tcRefNameLocalSet.clear();
        if (modesLocalFolder != null) {
            FileObject [] files = modesLocalFolder.getChildren();
            for (int i = 0; i < files.length; i++) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "fo[" + i + "]: " + files[i]);
                if (!files[i].isFolder() && PersistenceManager.MODE_EXT.equals(files[i].getExt())) {
                    //wsmode file
                    ModeParser modeParser;
                    if (modeParserMap.containsKey(files[i].getName())) {
                        modeParser = (ModeParser) modeParserMap.get(files[i].getName());
                    } else {
                        modeParser = new ModeParser(files[i].getName(),tcRefNameLocalSet);
                        modeParserMap.put(files[i].getName(), modeParser);
                    }
                    modeParser.setInLocalFolder(true);
                    modeParser.setLocalParentFolder(modesLocalFolder);
                }
                //Look for wstcref file in local folder
                if (files[i].isFolder()) {
                    FileObject [] subFiles = files[i].getChildren();
                    for (int j = 0; j < subFiles.length; j++) {
                        if (!subFiles[j].isFolder() && PersistenceManager.TCREF_EXT.equals(subFiles[j].getExt())) {
                            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- name: [" + files[i].getName() + "][" + subFiles[j].getName() + "]");
                            tcRefNameLocalSet.add(subFiles[j].getName());
                        }
                    }
                }
            }
        }
        
        /*for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            if (DEBUG) Debug.log(WindowManagerParser.class, "modeParser: " + modeParser.getName()
            + " isInModuleFolder:" + modeParser.isInModuleFolder()
            + " isInLocalFolder:" + modeParser.isInLocalFolder());
        }*/
        
        //Check if corresponding module is present and enabled.
        //We must load configuration data first because module info is stored in XML.
        List modeCfgList = new ArrayList(modeParserMap.size());
        List toRemove = new ArrayList(modeParserMap.size());
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            ModeConfig modeCfg;
            try {
                modeCfg = modeParser.load();
            } catch (IOException exc) {
                //If reading of one Mode fails we want to log message
                //and continue.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                continue;
            }
            boolean modeAccepted = acceptMode(modeParser);
            if (modeAccepted) {
                modeCfgList.add(modeCfg);
            } else {
                toRemove.add(modeParser);
                deleteLocalMode(modeParser.getName());
            }
        }
        for (int i = 0; i < toRemove.size(); i++) {
            ModeParser modeParser = (ModeParser) toRemove.get(i);
            modeParserMap.remove(modeParser.getName());
        }
        
        wmc.modes = (ModeConfig []) modeCfgList.toArray(new ModeConfig[modeCfgList.size()]);
        
        if (DEBUG) Debug.log(WindowManagerParser.class, "readModes LEAVE");
    }
    
    /** Checks if module for given mode exists.
     * @return true if mode is valid - its module exists
     */
    private boolean acceptMode (ModeParser modeParser) {
        InternalConfig cfg = modeParser.getInternalConfig();
        //Check module info
        if (cfg.moduleCodeNameBase != null) {
            ModuleInfo curModuleInfo = PersistenceManager.findModule
            (cfg.moduleCodeNameBase, cfg.moduleCodeNameRelease,
             cfg.moduleSpecificationVersion);
            if ((curModuleInfo != null) && curModuleInfo.isEnabled()) {
                //Module is present and is enabled
                return true;
            } else {
                //Module is NOT present (it could be deleted offline)
                //or is NOT enabled
                return false;
            }
        } else {
            //No module info
            return true;
        }
    }
    
    private void readGroups (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups ENTER");
        
        for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            groupParser.setInModuleFolder(false);
            groupParser.setInLocalFolder(false);
        }
        
        FileObject groupsModuleFolder = pm.getRootModuleFolder().getFileObject(PersistenceManager.GROUPS_FOLDER);
        //if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups groupsModuleFolder: " + groupsModuleFolder);
        
        if (groupsModuleFolder != null) {
            FileObject [] files;
            files = groupsModuleFolder.getChildren();
            for (int i = 0; i < files.length; i++) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups fo[" + i + "]: " + files[i]);
                if (!files[i].isFolder() && PersistenceManager.GROUP_EXT.equals(files[i].getExt())) {
                    GroupParser groupParser;
                    //wsgrp file
                    if (groupParserMap.containsKey(files[i].getName())) {
                        groupParser = (GroupParser) groupParserMap.get(files[i].getName());
                    } else {
                        groupParser = new GroupParser(files[i].getName());
                        groupParserMap.put(files[i].getName(), groupParser);
                    }
                    groupParser.setInModuleFolder(true);
                    groupParser.setModuleParentFolder(groupsModuleFolder);
                }
            }
        }
        
        FileObject groupsLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.GROUPS_FOLDER);
        if (groupsLocalFolder != null) {
            //if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups groupsLocalFolder: " + groupsLocalFolder);
            FileObject [] files = groupsLocalFolder.getChildren();
            for (int i = 0; i < files.length; i++) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups fo[" + i + "]: " + files[i]);
                if (!files[i].isFolder() && PersistenceManager.GROUP_EXT.equals(files[i].getExt())) {
                    //wsgrp file
                    GroupParser groupParser;
                    if (groupParserMap.containsKey(files[i].getName())) {
                        groupParser = (GroupParser) groupParserMap.get(files[i].getName());
                    } else {
                        groupParser = new GroupParser(files[i].getName());
                        groupParserMap.put(files[i].getName(), groupParser);
                    }
                    groupParser.setInLocalFolder(true);
                    groupParser.setLocalParentFolder(groupsLocalFolder);
                }
            }
        }
        
        /*for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups groupParser: " + groupParser.getName()
            + " isInModuleFolder:" + groupParser.isInModuleFolder()
            + " isInLocalFolder:" + groupParser.isInLocalFolder());
        }*/
        
        //Check if corresponding module is present and enabled.
        //We must load configuration data first because module info is stored in XML.
        List groupCfgList = new ArrayList(groupParserMap.size());
        List toRemove = new ArrayList(groupParserMap.size());
        for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            GroupConfig groupCfg;
            try {
                groupCfg = groupParser.load();
            } catch (IOException exc) {
                //If reading of one group fails we want to log message
                //and continue.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                continue;
            }
            boolean groupAccepted = acceptGroup(groupParser);
            if (groupAccepted) {
                groupCfgList.add(groupCfg);
            } else {
                toRemove.add(groupParser);
                deleteLocalGroup(groupParser.getName());
            }
        }
        for (int i = 0; i < toRemove.size(); i++) {
            GroupParser groupParser = (GroupParser) toRemove.get(i);
            groupParserMap.remove(groupParser.getName());
        }
        
        wmc.groups = (GroupConfig []) groupCfgList.toArray(new GroupConfig[groupCfgList.size()]);
        
        if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups LEAVE");
    }
    
    /** Checks if module for given group exists.
     * @return true if group is valid - its module exists
     */
    private boolean acceptGroup (GroupParser groupParser) {
        InternalConfig cfg = groupParser.getInternalConfig();
        //Check module info
        if (cfg.moduleCodeNameBase != null) {
            ModuleInfo curModuleInfo = PersistenceManager.findModule
            (cfg.moduleCodeNameBase, cfg.moduleCodeNameRelease,
             cfg.moduleSpecificationVersion);
            if ((curModuleInfo != null) && curModuleInfo.isEnabled()) {
                //Module is present and is enabled
                return true;
            } else {
                return false;
            }
        } else {
            //No module info
            return true;
        }
    }
    
    private void writeProperties (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeProperties ENTER");
        if (propertyHandler == null) {
            propertyHandler = new PropertyHandler();
        }
        propertyHandler.writeData(wmc);
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeProperties LEAVE");
    }
    
    private void writeModes (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeModes ENTER");
        //Step 1: Clean obsolete mode parsers
        HashMap modeConfigMap = new HashMap();
        for (int i = 0; i < wmc.modes.length; i++) {
            modeConfigMap.put(wmc.modes[i].name, wmc.modes[i]);
        }
        List toDelete = new ArrayList(10);
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            if (!modeConfigMap.containsKey(modeParser.getName())) {
                toDelete.add(modeParser.getName());
            }
        }
        for (int i = 0; i < toDelete.size(); i++) {
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeModes ** REMOVE FROM MAP modeParser: " + toDelete.get(i));
            modeParserMap.remove(toDelete.get(i));
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeModes ** DELETE modeParser: " + toDelete.get(i));
            deleteLocalMode((String) toDelete.get(i));
        }
        
        //Step 2: Create missing mode parsers
        for (int i = 0; i < wmc.modes.length; i++) {
            if (!modeParserMap.containsKey(wmc.modes[i].name)) {
                ModeParser modeParser = new ModeParser(wmc.modes[i].name,tcRefNameLocalSet);
                modeParserMap.put(wmc.modes[i].name, modeParser);
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeModes ** CREATE modeParser:" + modeParser.getName());
            }
        }
        
        FileObject modesLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.MODES_FOLDER);
        if ((modesLocalFolder == null) && (modeParserMap.size() > 0)) {
            modesLocalFolder = pm.getModesLocalFolder();
        }
        //Step 3: Save all modes
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            modeParser.setLocalParentFolder(modesLocalFolder);
            modeParser.setInLocalFolder(true);
            modeParser.save((ModeConfig) modeConfigMap.get(modeParser.getName()));
        }
        
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeModes LEAVE");
    }
    
    private void writeGroups (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups ENTER");
        //Step 1: Clean obsolete group parsers
        HashMap groupConfigMap = new HashMap();
        //if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups List of groups to be saved:");
        for (int i = 0; i < wmc.groups.length; i++) {
            //if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups group[" + i + "]: " + wmc.groups[i].name);
            groupConfigMap.put(wmc.groups[i].name, wmc.groups[i]);
        }
        List toDelete = new ArrayList(10);
        for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            if (!groupConfigMap.containsKey(groupParser.getName())) {
                toDelete.add(groupParser.getName());
            }
        }
        for (int i = 0; i < toDelete.size(); i++) {
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeGroups ** REMOVE FROM MAP groupParser: " + toDelete.get(i));
            groupParserMap.remove(toDelete.get(i));
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeGroups ** DELETE groupParser: " + toDelete.get(i));
            deleteLocalGroup((String) toDelete.get(i));
        }
        //Step 2: Create missing group parsers
        for (int i = 0; i < wmc.groups.length; i++) {
            if (!groupParserMap.containsKey(wmc.groups[i].name)) {
                GroupParser groupParser = new GroupParser(wmc.groups[i].name);
                groupParserMap.put(wmc.groups[i].name, groupParser);
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeGroups ** CREATE groupParser:" + groupParser.getName());
            }
        }
        //Step 3: Save all groups
        FileObject groupsLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.GROUPS_FOLDER);
        if ((groupsLocalFolder == null) && (groupParserMap.size() > 0)) {
            groupsLocalFolder = pm.getGroupsLocalFolder();
        }
        //if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups groupsLocalFolder:" + groupsLocalFolder);
        for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            groupParser.setLocalParentFolder(groupsLocalFolder);
            groupParser.setInLocalFolder(true);
            //if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups save group:" + groupParser.getName());
            groupParser.save((GroupConfig) groupConfigMap.get(groupParser.getName()));
        }
        
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups LEAVE");
    }
    
    private void deleteLocalMode (String modeName) {
        if (DEBUG) Debug.log(WindowManagerParser.class, "deleteLocalMode" + " mo:" + modeName);
        FileObject modesLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.MODES_FOLDER);
        if (modesLocalFolder == null) {
            return;
        }
        FileObject modeFO;
        modeFO = modesLocalFolder.getFileObject(modeName);
        if (modeFO != null) {
            PersistenceManager.deleteOneFO(modeFO);
        }
        modeFO = modesLocalFolder.getFileObject(modeName, PersistenceManager.MODE_EXT);
        if (modeFO != null) {
            PersistenceManager.deleteOneFO(modeFO);
        }
    }
    
    private void deleteLocalGroup (String groupName) {
        if (DEBUG) Debug.log(WindowManagerParser.class, "deleteLocalGroup" + " groupName:" + groupName);
        FileObject groupsLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.GROUPS_FOLDER);
        if (groupsLocalFolder == null) {
            return;
        }
        FileObject groupFO;
        groupFO = groupsLocalFolder.getFileObject(groupName);
        if (groupFO != null) {
            PersistenceManager.deleteOneFO(groupFO);
        }
        groupFO = groupsLocalFolder.getFileObject(groupName, PersistenceManager.GROUP_EXT);
        if (groupFO != null) {
            PersistenceManager.deleteOneFO(groupFO);
        }
    }
    
    String getName () {
        return wmName;
    }
    
    void log (String s) {
        if (DEBUG) {
            Debug.log(WindowManagerParser.class, s);
        }
    }
    
    private final class PropertyHandler extends DefaultHandler {
        
        /** WindowManager configuration data */
        private WindowManagerConfig winMgrConfig = null;
        
        /** Internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** List to store parsed path items */
        private List itemList = new ArrayList(10);
        
        /** List to store parsed tc-ids */
        private List tcIdList = new ArrayList(10);
        
        /** Map of imported tcref items */
        private Map tcRefMap;
        
        /** xml parser */
        private XMLReader parser;
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject rootFolder;

            rootFolder = pm.getRootLocalFolder();

            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " rootFolder:" + rootFolder);

            FileObject wmConfigFO;
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " looking for LOCAL");
            wmConfigFO = rootFolder.getFileObject
            (WindowManagerParser.this.getName(), PersistenceManager.WINDOWMANAGER_EXT);
            if (wmConfigFO != null) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " wmConfigFO LOCAL:" + wmConfigFO);
                return wmConfigFO;
            } else {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " LOCAL not found");
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " looking for MODULE");
                //Local data not found, try module
                rootFolder = pm.getRootModuleFolder();
                wmConfigFO = rootFolder.getFileObject
                (WindowManagerParser.this.getName(), PersistenceManager.WINDOWMANAGER_EXT);

                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " wmConfigFO MODULE:" + wmConfigFO);

                return wmConfigFO;
            }
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject rootFolder;
            rootFolder = pm.getRootLocalFolder();
            
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOOutput" + " rootFolder:" + rootFolder);
            
            FileObject wmConfigFO;
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOOutput" + " looking for LOCAL");
            wmConfigFO = rootFolder.getFileObject
            (WindowManagerParser.this.getName(), PersistenceManager.WINDOWMANAGER_EXT);
            if (wmConfigFO != null) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOOutput" + " wmConfigFO LOCAL:" + wmConfigFO);
                return wmConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(WindowManagerParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.WINDOWMANAGER_EXT);
                wmConfigFO = FileUtil.createData(rootFolder, buffer.toString());
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOOutput" + " LOCAL not found CREATE");
                return wmConfigFO;
            }
        }
        
        /** 
         Reads window manager configuration data from XML file. 
         Data are returned in output params.
         */
        void readData (WindowManagerConfig winMgrCfg, InternalConfig internalCfg)
        throws IOException {
            winMgrConfig = winMgrCfg;
            internalConfig = internalCfg;
            itemList.clear();
            tcIdList.clear();
            
            tcRefMap = ImportManager.getDefault().getImportedTCRefs();
            tcRefMap.clear();
            
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing Window Manager configuration file");
            }
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*InputStream is = cfgFOInput.getInputStream();
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    if (DEBUG) Debug.log(WindowManagerParser.class, "DUMP WindowManager:");
                    String s = new String(arr);
                    if (DEBUG) Debug.log(WindowManagerParser.class, s);*/
                    //DUMP END
//                    long time = System.currentTimeMillis();
                    getXMLParser().parse(new InputSource(cfgFOInput.getInputStream()));
//                    System.out.println("WindowManagerParser.readData "+(System.currentTimeMillis()-time));
                }
            } catch (SAXException exc) {
                //Turn into annotated IOException
                String msg = NbBundle.getMessage(WindowManagerParser.class,
                        "EXC_WindowManagerParse", cfgFOInput);
                IOException ioe = new IOException(msg);
                ErrorManager.getDefault().annotate(ioe, exc);
                throw ioe;
            }
            
            winMgrConfig.editorAreaConstraints =
                (SplitConstraint []) itemList.toArray(new SplitConstraint[itemList.size()]);
            winMgrConfig.tcIdViewList = 
                (String []) tcIdList.toArray(new String[tcIdList.size()]);
            winMgrCfg = winMgrConfig;
            internalCfg = internalConfig;
            ImportManager.getDefault().setImportedTCRefs(tcRefMap);
            
            tcRefMap = null;
            winMgrConfig = null;
            internalConfig = null;
        }
        
        public void startElement (String nameSpace, String name, String qname, Attributes attrs) throws SAXException {
            if ("windowmanager".equals(qname)) { // NOI18N
                handleWindowManager(attrs);
            } else if (internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) == 0) {
                //Parse version 2.0
                if ("main-window".equals(qname)) { // NOI18N
                    handleMainWindow(attrs);
                } else if ("joined-properties".equals(qname)) { // NOI18N
                    handleJoinedProperties(attrs);
                } else if ("separated-properties".equals(qname)) { // NOI18N
                    handleSeparatedProperties(attrs);
                } else if ("editor-area".equals(qname)) { // NOI18N
                    handleEditorArea(attrs);
                } else if ("constraints".equals(qname)) { // NOI18N
                    handleConstraints(attrs);
                } else if ("path".equals(qname)) { // NOI18N
                    handlePath(attrs);
                } else if ("bounds".equals(qname)) { // NOI18N
                    handleEditorAreaBounds(attrs);
                } else if ("relative-bounds".equals(qname)) { // NOI18N
                    handleEditorAreaRelativeBounds(attrs);
                } else if ("screen".equals(qname)) { // NOI18N
                    handleScreen(attrs);
                } else if ("active-mode".equals(qname)) { // NOI18N
                    handleActiveMode(attrs);
                } else if ("maximized-mode".equals(qname)) { // NOI18N
                    handleMaximizedMode(attrs);
                } else if ("toolbar".equals(qname)) { // NOI18N
                    handleToolbar(attrs);
                } else if ("tc-id".equals(qname)) { // NOI18N
                    handleTcId(attrs);
                } else if ("tcref-item".equals(qname)) { // NOI18N
                    handleTCRefItem(attrs);
                }
            } else {
                if (DEBUG) Debug.log(WindowManagerParser.class, "WMP.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }

        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }

        public void warning(SAXParseException ex) throws SAXException {
            // ignore
        }
        
        /** Reads element "windowmanager" */
        private void handleWindowManager (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleWindowManager]" // NOI18N
                + " Missing attribute \"version\" of element \"windowmanager\"."); // NOI18N
                internalConfig.specVersion = new SpecificationVersion("2.0"); // NOI18N
            }
        }
        
        /** Reads element "main-window" and updates window manager config content */
        private void handleMainWindow (Attributes attrs) {
        }
        
        /** Reads element "joined-properties" and updates window manager config content */
        private void handleJoinedProperties (Attributes attrs) {
            String s;
            try {
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    winMgrConfig.xJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.xJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"x\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.xJoined = -1;
            }
            
            try {
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    winMgrConfig.yJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.yJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"y\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.yJoined = -1;
            }
            
            try {
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    winMgrConfig.widthJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.widthJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"width\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.widthJoined = -1;
            }
            
            try {
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    winMgrConfig.heightJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.heightJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"height\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.heightJoined = -1;
            }
            
            try {
                s = attrs.getValue("relative-x"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeXJoined = floatParse(s);
                } else {
                    winMgrConfig.relativeXJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-x\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.relativeXJoined = -1;
            }
            
            try {
                s = attrs.getValue("relative-y"); // NOI18N
                
                if (s != null) {
                    winMgrConfig.relativeYJoined = floatParse(s);
                } else {
                    winMgrConfig.relativeYJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-y\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.relativeYJoined = -1;
            }
            
            try {
                s = attrs.getValue("relative-width"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeWidthJoined = floatParse(s);
                } else {
                    winMgrConfig.relativeWidthJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-width\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.relativeWidthJoined = -1;
            }
            
            try {
                s = attrs.getValue("relative-height"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeHeightJoined = floatParse(s);
                } else {
                    winMgrConfig.relativeHeightJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-height\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.relativeHeightJoined = -1;
            }
            
            s = attrs.getValue("centered-horizontally"); // NOI18N
            if (s != null) {
                if ("true".equals(s)) { // NOI18N
                    winMgrConfig.centeredHorizontallyJoined = true;
                } else if ("false".equals(s)) { // NOI18N
                    winMgrConfig.centeredHorizontallyJoined = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                    + " Warning: Invalid value of attribute \"centered-horizontally\"" // NOI18N
                    + " of element \"joined-properties\"."); // NOI18N
                    winMgrConfig.centeredHorizontallyJoined = false;
                }
            } else {
                winMgrConfig.centeredHorizontallyJoined = false;
            }
            
            s = attrs.getValue("centered-vertically"); // NOI18N
            if (s != null) {
                if ("true".equals(s)) { // NOI18N
                    winMgrConfig.centeredVerticallyJoined = true;
                } else if ("false".equals(s)) { // NOI18N
                    winMgrConfig.centeredVerticallyJoined = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                    + " Warning: Invalid value of attribute \"centered-vertically\"" // NOI18N
                    + " of element \"joined-properties\"."); // NOI18N
                    winMgrConfig.centeredVerticallyJoined = false;
                }
            } else {
                winMgrConfig.centeredVerticallyJoined = false;
            }
            
            try {
                s = attrs.getValue("maximize-if-width-below"); // NOI18N
                if (s != null) {
                    winMgrConfig.maximizeIfWidthBelowJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.maximizeIfWidthBelowJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"maximize-if-width-below\"" // NOI18N
                + " of element \"joined-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.maximizeIfWidthBelowJoined = -1;
            }
            
            try {
                s = attrs.getValue("maximize-if-height-below"); // NOI18N
                if (s != null) {
                    winMgrConfig.maximizeIfHeightBelowJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.maximizeIfHeightBelowJoined = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"maximize-if-height-below\"" // NOI18N
                + " of element \"joined-properties\".");
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.maximizeIfHeightBelowJoined = -1;
            }
            
            String frameState = attrs.getValue("frame-state"); // NOI18N
            if (frameState != null) {
                try {
                    winMgrConfig.mainWindowFrameStateJoined = Integer.parseInt(frameState);
                } catch (NumberFormatException exc) {
                    ErrorManager em = ErrorManager.getDefault();
                    em.log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                    + " Warning: Cannot read attribute \"frame-state\"" // NOI18N
                    + " of element \"joined-properties\"."); // NOI18N
                    em.notify(ErrorManager.INFORMATIONAL,exc);
                    winMgrConfig.mainWindowFrameStateJoined = Frame.NORMAL;
                }
            } else {
                winMgrConfig.mainWindowFrameStateJoined = Frame.NORMAL;
            }
        }
        
        /** Reads element "separated-properties" and updates window manager config content */
        private void handleSeparatedProperties (Attributes attrs) {
            String s;
            try {
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    winMgrConfig.xSeparated = Integer.parseInt(s);
                } else {
                    winMgrConfig.xSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"x\"" // NOI18N
                + " of element \"separated-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.xSeparated = -1;
            }
            
            try {
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    winMgrConfig.ySeparated = Integer.parseInt(s);
                } else {
                    winMgrConfig.ySeparated = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"y\"" // NOI18N
                + " of element \"separated-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.ySeparated = -1;
            }
            
            try {
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    winMgrConfig.widthSeparated = Integer.parseInt(s);
                } else {
                    winMgrConfig.widthSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"width\"" // NOI18N
                + " of element \"separated-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.widthSeparated = -1;
            }
            
            try {
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    winMgrConfig.heightSeparated = Integer.parseInt(s);
                } else {
                    winMgrConfig.heightSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"height\"" // NOI18N
                + " of element \"separated-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.heightSeparated = -1;
            }
            
            try {
                s = attrs.getValue("relative-x"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeXSeparated = floatParse(s);
                } else {
                    winMgrConfig.relativeXSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-x\"" // NOI18N
                + " of element \"separated-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.relativeXSeparated = -1;
            }
            
            try {
                s = attrs.getValue("relative-y"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeYSeparated = floatParse(s);
                } else {
                    winMgrConfig.relativeYSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-y\"" // NOI18N
                + " of element \"separated-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.relativeYSeparated = -1;
            }
            
            try {
                s = attrs.getValue("relative-width"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeWidthSeparated = floatParse(s);
                } else {
                    winMgrConfig.relativeWidthSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-width\"" // NOI18N
                + " of element \"separated-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.relativeWidthSeparated = -1;
            }
            
            try {
                s = attrs.getValue("relative-height"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeHeightSeparated = floatParse(s);
                } else {
                    winMgrConfig.relativeHeightSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-height\"" // NOI18N
                + " of element \"separated-properties\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                winMgrConfig.relativeHeightSeparated = -1;
            }
            
            s = attrs.getValue("centered-horizontally"); // NOI18N
            if (s != null) {
                if ("true".equals(s)) { // NOI18N
                    winMgrConfig.centeredHorizontallySeparated = true;
                } else if ("false".equals(s)) { // NOI18N
                    winMgrConfig.centeredHorizontallySeparated = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                    + " Warning: Invalid value of attribute \"centered-horizontally\"" // NOI18N
                    + " of element \"separated-properties\"."); // NOI18N
                    winMgrConfig.centeredHorizontallySeparated = false;
                }
            } else {
                winMgrConfig.centeredHorizontallySeparated = false;
            }
            
            s = attrs.getValue("centered-vertically"); // NOI18N
            if (s != null) {
                if ("true".equals(s)) { // NOI18N
                    winMgrConfig.centeredVerticallySeparated = true;
                } else if ("false".equals(s)) { // NOI18N
                    winMgrConfig.centeredVerticallySeparated = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                    + " Warning: Invalid value of attribute \"centered-vertically\"" // NOI18N
                    + " of element \"separated-properties\"."); // NOI18N
                    winMgrConfig.centeredVerticallySeparated = false;
                }
            } else {
                winMgrConfig.centeredVerticallySeparated = false;
            }
            
            String frameState = attrs.getValue("frame-state"); // NOI18N
            if (frameState != null) {
                try {
                    winMgrConfig.mainWindowFrameStateSeparated = Integer.parseInt(frameState);
                } catch (NumberFormatException exc) {
                    ErrorManager em = ErrorManager.getDefault();
                    em.log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                    + " Warning: Cannot read attribute \"frame-state\"" // NOI18N
                    + " of element \"separated-properties\"."); // NOI18N
                    em.notify(ErrorManager.INFORMATIONAL,exc);
                    winMgrConfig.mainWindowFrameStateSeparated = Frame.NORMAL;
                }
            } else {
                winMgrConfig.mainWindowFrameStateSeparated = Frame.NORMAL;
            }
        }
        
        /** Reads element "editor-area" */
        private void handleEditorArea (Attributes attrs) {
            String state = attrs.getValue("state"); // NOI18N
            if (state != null) {
                if ("joined".equals(state)) {
                    winMgrConfig.editorAreaState = Constants.EDITOR_AREA_JOINED;
                } else if ("separated".equals(state)) {
                    winMgrConfig.editorAreaState = Constants.EDITOR_AREA_SEPARATED;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorArea]" // NOI18N
                    + " Warning: Invalid value of attribute \"state\"" // NOI18N
                    + " of element \"editor-area\"."); // NOI18N
                    winMgrConfig.editorAreaState = Constants.EDITOR_AREA_JOINED;
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleEditorArea]" // NOI18N
                + " Warning: Missing value of attribute \"state\"" // NOI18N
                + " of element \"editor-area\"."); // NOI18N
                winMgrConfig.editorAreaState = Constants.EDITOR_AREA_JOINED;
            }
            String frameState = attrs.getValue("frame-state"); // NOI18N
            if (frameState != null) {
                try {
                    winMgrConfig.editorAreaFrameState = Integer.parseInt(frameState);
                } catch (NumberFormatException exc) {
                    ErrorManager em = ErrorManager.getDefault();
                    em.log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorArea]" // NOI18N
                    + " Warning: Cannot read attribute \"frame-state\"" // NOI18N
                    + " of element \"editor-area\"."); // NOI18N
                    em.notify(ErrorManager.INFORMATIONAL,exc);
                    winMgrConfig.editorAreaFrameState = Frame.NORMAL;
                }
            } else {
                winMgrConfig.editorAreaFrameState = Frame.NORMAL;
            }
        }
        
        /** Reads element "constraints" */
        private void handleConstraints (Attributes attrs) {
        }
        
        /** Reads element "path" */
        private void handlePath (Attributes attrs) {
            String s = attrs.getValue("orientation"); // NOI18N
            int orientation;
            if ("horizontal".equals(s)) { // NOI18N
                orientation = Constants.HORIZONTAL;
            } else if ("vertical".equals(s)) { // NOI18N
                orientation = Constants.VERTICAL;
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handlePath]" // NOI18N
                + " Invalid or missing value of attribute \"orientation\"."); // NOI18N
                orientation = Constants.VERTICAL;
            }
            
            int number;
            try {
                s = attrs.getValue("number"); // NOI18N
                if (s != null) {
                    number = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handlePath]" // NOI18N
                    + " Missing value of attribute \"number\"."); // NOI18N
                    number = 0;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.INFORMATIONAL,
                "[WinSys.WindowManagerParser.handlePath]" // NOI18N
                + " Cannot read element \"path\", attribute \"number\""); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
                number = 0;
            }
            
            double weight;
            try {
                s = attrs.getValue("weight"); // NOI18N
                if (s != null) {
                    weight = Double.parseDouble(s);
                } else {
                    //Not required attribute, provide default value
                    weight = 0.5;
                }
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handlePath]" // NOI18N
                + " Warning: Cannot read element \"path\", attribute \"weight\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL, exc);
                weight = 0.5;
            }
            SplitConstraint item = new SplitConstraint(orientation, number, weight);
            itemList.add(item);
        }
        
        /** Reads element "screen" and updates window manager config content */
        private void handleScreen (Attributes attrs) {
            try {
                String s;
                winMgrConfig.screenSize = null;
                int width, height;
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleScreen]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"screen\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleScreen]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"screen\"."); // NOI18N
                    return;
                }
                winMgrConfig.screenSize = new Dimension(width, height);
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleScreen]" // NOI18N
                + " Warning: Cannot read element \"screen\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
            }
        }
        
        /** Reads element "bounds" of editor area and updates window manager config content */
        private void handleEditorAreaBounds (Attributes attrs) {
            try {
                String s;
                int x, y, width, height;
                
                winMgrConfig.editorAreaBounds = null;
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    x = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                    + " Warning: Missing attribute \"x\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    y = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                    + " Warning: Missing attribute \"y\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"bounds\"."); // NOI18N
                    return;
                }
                winMgrConfig.editorAreaBounds = new Rectangle(x, y, width, height);
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                + " Warning: Cannot read element \"bounds\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
            }
        }
        
        /** Reads element "relative-bounds" of editor area and updates window manager config content */
        private void handleEditorAreaRelativeBounds (Attributes attrs) {
            try {
                String s;
                int x, y, width, height;
                
                winMgrConfig.editorAreaRelativeBounds = null;
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    x = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"x\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    y = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"y\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                winMgrConfig.editorAreaRelativeBounds = new Rectangle(x, y, width, height);
            } catch (NumberFormatException exc) {
                ErrorManager em = ErrorManager.getDefault();
                em.log(ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                + " Warning: Cannot read element \"relative-bounds\"."); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL,exc);
            }
        }
        
        /** Reads element "active-mode" and updates window manager config content */
        private void handleActiveMode (Attributes attrs) {
            String name = attrs.getValue("name"); // NOI18N
            if (name != null) {
                winMgrConfig.activeModeName = name;
            } else {
                winMgrConfig.activeModeName = ""; // NOI18N
            }
        }
        
        /** Reads element "maximized-mode" and updates window manager config content */
        private void handleMaximizedMode (Attributes attrs) {
            String name = attrs.getValue("name"); // NOI18N
            if (name != null) {
                winMgrConfig.maximizedModeName = name;
            } else {
                winMgrConfig.maximizedModeName = ""; // NOI18N
            }
        }
        
        /** Reads element "toolbar" and updates window manager config content */
        private void handleToolbar (Attributes attrs) {
            String configuration = attrs.getValue("configuration"); // NOI18N
            if (configuration != null) {
                winMgrConfig.toolbarConfiguration = configuration;
            } else {
                winMgrConfig.toolbarConfiguration = "";  // NOI18N
            }
        }
        
        /** Reads element "tc-id" and updates window manager config content */
        private void handleTcId (Attributes attrs) {
            String id = attrs.getValue("id"); // NOI18N
            if (id != null) {
                if (!"".equals(id)) {
                    tcIdList.add(id);
                } else {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleTcId]" // NOI18N
                    + " Warning: Empty required attribute \"id\" of element \"tc-id\"."); // NOI18N
                }
            } else {
                ErrorManager.getDefault().log
                (ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleTcId]" // NOI18N
                + " Warning: Missing required attribute \"id\" of element \"tc-id\"."); // NOI18N
            }
        }
        
        /** Reads element "tcref-item" */
        private void handleTCRefItem (Attributes attrs) {
            String workspaceName = attrs.getValue("workspace"); // NOI18N
            String modeName = attrs.getValue("mode"); // NOI18N
            String tc_id = attrs.getValue("id"); // NOI18N
            
            if (workspaceName != null) {
                if ("".equals(workspaceName)) {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                    + " Warning: Empty required attribute \"workspace\" of element \"tcref-item\"."); // NOI18N
                    return;
                }
            } else {
                ErrorManager.getDefault().log
                (ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                + " Warning: Missing required attribute \"workspace\" of element \"tcref-item\"."); // NOI18N
                return;
            }
            if (modeName != null) {
                if ("".equals(modeName)) {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                    + " Warning: Empty required attribute \"mode\" of element \"tcref-item\"."); // NOI18N
                    return;
                }
            } else {
                ErrorManager.getDefault().log
                (ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                + " Warning: Missing required attribute \"mode\" of element \"tcref-item\"."); // NOI18N
                return;
            }
            if (tc_id != null) {
                if ("".equals(tc_id)) {
                    ErrorManager.getDefault().log
                    (ErrorManager.WARNING,
                    "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                    + " Warning: Empty required attribute \"id\" of element \"tcref-item\"."); // NOI18N
                    return;
                }
            } else {
                ErrorManager.getDefault().log
                (ErrorManager.WARNING,
                "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                + " Warning: Missing required attribute \"id\" of element \"tcref-item\"."); // NOI18N
                return;
            }
            //Attributes are checked, add new item to map
            //log("handleTCRefItem ADD ITEM [" + workspaceName + ","
            //+ modeName + "," + tc_id + "]");
            
            ImportedItem item = new ImportedItem(workspaceName,modeName,tc_id);
            tcRefMap.put(tc_id,item);
        }
        
        public void endDocument() throws org.xml.sax.SAXException {
        }
        
        public void ignorableWhitespace(char[] values, int param, int param2) throws org.xml.sax.SAXException {
        }
        
        public void endElement(java.lang.String str, java.lang.String str1, java.lang.String str2) throws org.xml.sax.SAXException {
        }
        
        public void skippedEntity(java.lang.String str) throws org.xml.sax.SAXException {
        }
        
        public void processingInstruction(java.lang.String str, java.lang.String str1) throws org.xml.sax.SAXException {
        }
                
        public void endPrefixMapping(java.lang.String str) throws org.xml.sax.SAXException {
        }
        
        public void startPrefixMapping(java.lang.String str, java.lang.String str1) throws org.xml.sax.SAXException {
        }
        
        public void characters(char[] values, int param, int param2) throws org.xml.sax.SAXException {
        }
        
        public void setDocumentLocator(org.xml.sax.Locator locator) {
        }
        
        public void startDocument() throws SAXException {
        }
        
        /** Writes data from asociated window manager to the xml representation */
        void writeData (WindowManagerConfig wmc) throws IOException {
            final StringBuffer buff = fillBuffer(wmc);
            synchronized (RW_LOCK) {
                FileObject cfgFOOutput = getConfigFOOutput();
                FileLock lock = cfgFOOutput.lock();
                OutputStreamWriter osw = null;
                try {
                    OutputStream os = cfgFOOutput.getOutputStream(lock);
                    osw = new OutputStreamWriter(os, "UTF-8"); // NOI18N
                    osw.write(buff.toString());
                    //if (DEBUG) Debug.log(WindowManagerParser.class, "-- DUMP WindowManager:");
                    //if (DEBUG) Debug.log(WindowManagerParser.class, buff.toString());
                } finally {
                    if (osw != null) {
                        osw.close();
                    }
                    lock.releaseLock();
                }
            }
        }
        
        /** Returns xml content in StringBuffer
         */
        private StringBuffer fillBuffer (WindowManagerConfig wmc) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            String curValue = null;
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"). // NOI18N
            /*buff.append("<!DOCTYPE windowmanager PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Window Manager Properties 2.0//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/windowmanager-properties2_0.dtd\">\n\n"); // NOI18N*/
                append("<windowmanager version=\"2.0\">\n"); // NOI18N
            
            appendMainWindow(wmc, buff);
            appendEditorArea(wmc, buff);
            appendScreen(wmc, buff);
            appendActiveMode(wmc, buff);
            appendMaximizedMode(wmc, buff);
            appendToolbar(wmc, buff);
            appendRecentViewList(wmc, buff);
            appendImportedData(buff);
            
            buff.append("</windowmanager>\n"); // NOI18N
            return buff;
        }
        

        private void appendMainWindow (WindowManagerConfig wmc, StringBuffer buff) {
            buff.append("    <main-window>\n  <joined-properties\n"). // NOI18N
                append("   x=\"").append(wmc.xJoined).append("\"\n"). // NOI18N
                append("   y=\"").append(wmc.yJoined).append("\"\n"). // NOI18N
                append("   width=\"").append(wmc.widthJoined).append("\"\n"). // NOI18N
                append("   height=\"").append(wmc.heightJoined).append("\"\n"). // NOI18N
                append("   relative-x=\"").append(wmc.relativeXJoined).append("\"\n"). // NOI18N
                append("   relative-y=\"").append(wmc.relativeYJoined).append("\"\n"). // NOI18N
                append("   relative-width=\"").append(wmc.relativeWidthJoined).append("\"\n"). // NOI18N
                append("   relative-height=\"").append(wmc.relativeHeightJoined).append("\"\n"). // NOI18N
                append("   centered-horizontally=\"").append(wmc.centeredHorizontallyJoined).append("\"\n"). // NOI18N
                append("   centered-vertically=\"").append(wmc.centeredVerticallyJoined).append("\"\n"). // NOI18N
                append("   maximize-if-width-below=\"").append(wmc.maximizeIfWidthBelowJoined).append("\"\n"). // NOI18N
                append("   maximize-if-height-below=\"").append(wmc.maximizeIfHeightBelowJoined).append("\"\n"). // NOI18N
                append("   frame-state=\"").append(wmc.mainWindowFrameStateJoined).append("\"\n/>\n"). // NOI18N
            
            //SEPARATED
                append("  <separated-properties\n").  // NOI18N
                append("   x=\"").append(wmc.xSeparated).append("\"\n"). // NOI18N
                append("   y=\"").append(wmc.ySeparated).append("\"\n"). // NOI18N
                append("   width=\"").append(wmc.widthSeparated).append("\"\n"). // NOI18N
                append("   height=\"").append(wmc.heightSeparated).append("\"\n"). // NOI18N
                append("   relative-x=\"").append(wmc.relativeXSeparated).append("\"\n"). // NOI18N
                append("   relative-y=\"").append(wmc.relativeYSeparated).append("\"\n"). // NOI18N
                append("   relative-width=\"").append(wmc.relativeWidthSeparated).append("\"\n"). // NOI18N
                append("   relative-height=\"").append(wmc.relativeHeightSeparated).append("\"\n"). // NOI18N
                append("   centered-horizontally=\"").append(wmc.centeredHorizontallySeparated).append("\"\n"). // NOI18N
                append("   centered-vertically=\"").append(wmc.centeredVerticallySeparated).append("\"\n"). // NOI18N
                append("   frame-state=\"").append(wmc.mainWindowFrameStateSeparated).append("\"\n"). // NOI18N
                append("/>\n  </main-window>\n"); // NOI18N
        }
        
        private void appendEditorArea (WindowManagerConfig wmc, StringBuffer buff) {
            buff.append("    <editor-area state=\""); // NOI18N
            if (wmc.editorAreaState == Constants.EDITOR_AREA_JOINED) {
                buff.append("joined"); // NOI18N
            } else {
                buff.append("separated"); // NOI18N
            }
            buff.append("\" frame-state=\"").append(wmc.editorAreaFrameState).append("\">\n"); // NOI18N
            
            //BEGIN Write constraints
            buff.append("  <constraints>\n"); // NOI18N
            for (int i = 0; i < wmc.editorAreaConstraints.length; i++) {
                SplitConstraint item = wmc.editorAreaConstraints[i];
                buff.append("  <path orientation=\""); // NOI18N
                if (item.orientation == Constants.HORIZONTAL) {
                    buff.append("horizontal"); // NOI18N
                } else {
                    buff.append("vertical"); // NOI18N
                }
                buff.append("\" number=\"").append(item.index).append("\" weight=\"").append(item.splitWeight).append("\" />\n"); // NOI18N
            }
            buff.append("  </constraints>\n"); // NOI18N
            //END Write constraints
            //BEGIN bounds or relative bounds
            if (wmc.editorAreaBounds != null) {
                buff.append("  <bounds x=\"").append(wmc.editorAreaBounds.x).
                    append("\" y=\"").append(wmc.editorAreaBounds.y).
                    append("\" width=\"").append(wmc.editorAreaBounds.width).append("\" height=\""); // NOI18N
                buff.append(wmc.editorAreaBounds.height).append("\" />\n"); // NOI18N
            } else if (wmc.editorAreaRelativeBounds != null) {
                buff.append("  <relative-bounds x=\"").append(wmc.editorAreaRelativeBounds.x).
                    append("\" y=\"").append(wmc.editorAreaRelativeBounds.y).
                    append("\" width=\"").append(wmc.editorAreaRelativeBounds.width).
                    append("\" height=\"").append(wmc.editorAreaRelativeBounds.height).append("\"/>\n"); // NOI18N
            }
            //END
            buff.append("    </editor-area>\n"); // NOI18N
        }
        
        private void appendScreen (WindowManagerConfig wmc, StringBuffer buff) {
            buff.append("    <screen width=\"").append(wmc.screenSize.width).  // NOI18N
                append("\" height=\"").append(wmc.screenSize.height).append("\"/>\n"); // NOI18N
        }
        
        private void appendActiveMode (WindowManagerConfig wmc, StringBuffer buff) {
            if ((wmc.activeModeName != null) && !"".equals(wmc.activeModeName)) {
                buff.append("    <active-mode name=\"").append(wmc.activeModeName).append("\"/>\n"); // NOI18N
            }
        }
        
        private void appendMaximizedMode (WindowManagerConfig wmc, StringBuffer buff) {
            if ((wmc.maximizedModeName != null) && !"".equals(wmc.maximizedModeName)) {
                buff.append("    <maximized-mode name=\"").append(wmc.maximizedModeName).append("\"/>\n"); // NOI18N
            }
        }
        
        private void appendToolbar (WindowManagerConfig wmc, StringBuffer buff) {
            if ((wmc.toolbarConfiguration != null) && !"".equals(wmc.toolbarConfiguration)) {
                buff.append("    <toolbar configuration=\"").append(wmc.toolbarConfiguration).append("\"/>\n"); // NOI18N
            }
        }
        
        private void appendRecentViewList (WindowManagerConfig wmc, StringBuffer buff) {
            if (wmc.tcIdViewList.length == 0) {
                return;
            }
            buff.append("    <tc-list>\n"); // NOI18N
            for (int i = 0; i < wmc.tcIdViewList.length; i++) {
                buff.append("  <tc-id id=\"").append(wmc.tcIdViewList[i]).append("\"/>\n"); // NOI18N
            }
            buff.append("    </tc-list>\n"); // NOI18N
        }
        
        private void appendImportedData (StringBuffer buff) {
            Map tcRefMap = ImportManager.getDefault().getImportedTCRefs();
            if (tcRefMap.size() == 0) {
                return;
            }
            buff.append("    <imported-tcrefs>\n"); // NOI18N
            for (Iterator it =  tcRefMap.keySet().iterator(); it.hasNext(); ) {
                ImportedItem item = (ImportedItem) tcRefMap.get(it.next());
                buff.append("  <tcref-item workspace=\"").append(item.workspaceName).append("\" mode=\""). // NOI18N
                    append(item.modeName).append("\" id=\"").append(item.tc_id).append("\"/>\n"); // NOI18N
            }
            buff.append("    </imported-tcrefs>\n"); // NOI18N
        }

        /** @return Newly created parser with set content handler, errror handler
         * and entity resolver
         */
        private XMLReader getXMLParser () throws SAXException {
            if (parser == null) {
                // get non validating, not namespace aware parser
                parser = XMLUtil.createXMLReader();
                parser.setContentHandler(this);
                parser.setErrorHandler(this);
                parser.setEntityResolver(this);
            }
            return parser;
        }

        /** Implementation of entity resolver. Points to the local DTD
         * for our public ID */
        public InputSource resolveEntity (String publicId, String systemId)
        throws SAXException {
            if (INSTANCE_DTD_ID_1_0.equals(publicId)
             || INSTANCE_DTD_ID_1_1.equals(publicId)
             || INSTANCE_DTD_ID_2_0.equals(publicId)) {
                InputStream is = new ByteArrayInputStream(new byte[0]);
                //getClass().getResourceAsStream(INSTANCE_DTD_LOCAL);
//                if (is == null) {
//                    throw new IllegalStateException ("Entity cannot be resolved."); // NOI18N
//                }
                return new InputSource(is);
            }
            return null; // i.e. follow advice of systemID
        }
    }
    
    /** Float.parseFloat() shows up as a startup hotspot (classloading?), so this uses a 
     * lookup table for common values */
    private static final float floatParse (String s) throws NumberFormatException {
        int i = Arrays.binarySearch(floatStrings, s);
        if (i >= 0) {
            return floatVals[i];
        }
        return Float.parseFloat(s);
    }
    
    private static final String[] floatStrings = new String[] {
        "0", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", // NOI18N
        "0.9","1","1.0" // NOI18N
    };
    
    private static final float[] floatVals = new float[] {
        0f, 0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f,
        1f, 1f
    };
}
